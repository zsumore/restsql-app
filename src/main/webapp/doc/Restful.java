package mq.restful.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import mq.restful.model.TreeRoot;
import mq.restful.util.RestUtil;

import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.filter.text.cql2.CQLException;
import org.restsql.core.ColumnMetaData;
import org.restsql.core.Config;
import org.restsql.core.RequestSQLParams;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;
import org.restsql.core.SqlResourceFactory;
import org.restsql.core.SqlResourceFactoryException;
import org.restsql.core.impl.AbstractSqlResourceMetaData;
import org.restsql.core.sqlresource.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Controller
@RequestMapping(value = "/res")
public class ResResource {

	private final String ResourceNotExist = "请检查Resource Name,该Resource不存在.";

	@Autowired
	private SqlResourceFactory sqlResourceFactory;
	@Autowired
	private Config config;
	@Autowired
	private ResTreeService resTreeService;

	private Cache<RequestSQLParams, Object> cache;

	JAXBContext context;
	Marshaller marshaller;

	ObjectMapper mapper;

	final Logger logger = LoggerFactory.getLogger(ResResource.class);

	@RequestMapping(value = "/{resName:.+}", method = RequestMethod.GET)
	public void get(@PathVariable final String resName,
			@RequestParam(value = "_filter", required = false) String filter,
			@RequestParam(value = "_orderby", required = false) String orderby,
			@RequestParam(value = "_limit", required = false) Integer limit,
			@RequestParam(value = "_offset", required = false) Integer offset,
			@RequestParam(value = "_output", required = false) String output,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;
		// System.out.println(resName);
		try {

			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));
			response.setContentType(RestUtil.getContentType(output,
					config.getProperty(Config.KEY_RESPONSE_CHARSET)));
			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);
			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				// set Cache-Control
				String cacheControl = null;
				if (null != sqlResource.getDefinition().getHttp()
						&& RestUtil.stringNotNullOrEmpty(sqlResource
								.getDefinition().getHttp().getResponse()
								.getCacheControl())) {
					cacheControl = sqlResource.getDefinition().getHttp()
							.getResponse().getCacheControl();

				} else {
					cacheControl = config
							.getProperty(Config.KEY_HTTP_CACHE_CONTROL);

				}
				if (RestUtil.stringNotNullOrEmpty(cacheControl)) {
					response.addHeader("Cache-Control", cacheControl);
				}

				RequestSQLParams requestSQLParams = genRequestSQLParams(
						resName, filter, orderby, limit, offset);

				Object result = getCache().getIfPresent(requestSQLParams);

				if (null == result) {

					try {
						result = sqlResource.read(requestSQLParams);
					} catch (DataAccessException | CQLException
							| FilterToSQLException e) {
						writer.print(genThrowableMessage(e));
						logger.error(e.getMessage());
					}

					if (null != result) {
						cache.put(requestSQLParams, result);
					}
				}

				ObjectMapper mapper = RestUtil.getObjectMapper(output);

				if (mapper instanceof CsvMapper) {
					if (!sqlResource.getMetaData().isHierarchical()) {
						Builder builder = CsvSchema.builder();
						for (ColumnMetaData meta : sqlResource.getMetaData()
								.getParentReadColumns()) {
							builder.addColumn(meta.getColumnLabel());
						}
						CsvSchema schema = builder
								.build()
								.withLineSeparator(
										config.getProperty(Config.KEY_RESPONSE_CSV_LINE_SEPATATOR))
								.withColumnSeparator(
										config.getProperty(
												Config.KEY_RESPONSE_CSV_COLUMN_SEPATATOR)
												.trim().charAt(0));
						((CsvMapper) mapper).writer(schema).writeValue(writer,
								result);
					} else {
						writer.print(genErrorMessage("CsvMapper不支持一对多关系."));
					}
				} else {
					mapper.writeValue(writer, result);
				}

			}

		} catch (SqlResourceFactoryException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genThrowableMessage(e));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}

	}
	
	@RequestMapping(value = "/pretty/{resName:.+}", method = RequestMethod.GET)
	public void getPretty(@PathVariable final String resName,
			@RequestParam(value = "_filter", required = false) String filter,
			@RequestParam(value = "_orderby", required = false) String orderby,
			@RequestParam(value = "_limit", required = false) Integer limit,
			@RequestParam(value = "_offset", required = false) Integer offset,
			@RequestParam(value = "_output", required = false) String output,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;
		// System.out.println(resName);
		try {

			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));
			response.setContentType(RestUtil.getContentType(output,
					config.getProperty(Config.KEY_RESPONSE_CHARSET)));
			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);
			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				// set Cache-Control
				String cacheControl = null;
				if (null != sqlResource.getDefinition().getHttp()
						&& RestUtil.stringNotNullOrEmpty(sqlResource
								.getDefinition().getHttp().getResponse()
								.getCacheControl())) {
					cacheControl = sqlResource.getDefinition().getHttp()
							.getResponse().getCacheControl();

				} else {
					cacheControl = config
							.getProperty(Config.KEY_HTTP_CACHE_CONTROL);

				}
				if (RestUtil.stringNotNullOrEmpty(cacheControl)) {
					response.addHeader("Cache-Control", cacheControl);
				}

				RequestSQLParams requestSQLParams = genRequestSQLParams(
						resName, filter, orderby, limit, offset);

				Object result = getCache().getIfPresent(requestSQLParams);

				if (null == result) {

					try {
						result = sqlResource.read(requestSQLParams);
					} catch (DataAccessException | CQLException
							| FilterToSQLException e) {
						writer.print(genThrowableMessage(e));
						logger.error(e.getMessage());
					}

					if (null != result) {
						cache.put(requestSQLParams, result);
					}
				}

				ObjectMapper mapper = RestUtil.getPrettyObjectMapper(output);

				if (mapper instanceof CsvMapper) {
					if (!sqlResource.getMetaData().isHierarchical()) {
						Builder builder = CsvSchema.builder();
						for (ColumnMetaData meta : sqlResource.getMetaData()
								.getParentReadColumns()) {
							builder.addColumn(meta.getColumnLabel());
						}
						CsvSchema schema = builder
								.build()
								.withLineSeparator(
										config.getProperty(Config.KEY_RESPONSE_CSV_LINE_SEPATATOR))
								.withColumnSeparator(
										config.getProperty(
												Config.KEY_RESPONSE_CSV_COLUMN_SEPATATOR)
												.trim().charAt(0));
						((CsvMapper) mapper).writer(schema).writeValue(writer,
								result);
					} else {
						writer.print(genErrorMessage("CsvMapper不支持一对多关系."));
					}
				} else {
					mapper.writeValue(writer, result);
				}

			}

		} catch (SqlResourceFactoryException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genThrowableMessage(e));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}

	}

	@RequestMapping(value = "/metadata/{resName:.+}", method = RequestMethod.GET)
	public void getSqlResourceMetadata(@PathVariable final String resName,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType

			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));
			response.setContentType(RestUtil.getContentType("xml",
					config.getProperty(Config.KEY_RESPONSE_CHARSET)));
			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);
			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				if (null == marshaller) {
					JAXBContext context = JAXBContext.newInstance(
							ObjectFactory.class,
							AbstractSqlResourceMetaData.class);

					marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
							Boolean.TRUE);
				}

				marshaller
						.marshal((AbstractSqlResourceMetaData) sqlResource
								.getMetaData(), writer);

			}

		} catch (JAXBException e) {

			e.printStackTrace();
		} catch (SqlResourceFactoryException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genThrowableMessage(e));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}

	}

	@RequestMapping(value = "/definition/{resName:.+}", method = RequestMethod.GET)
	public void getSqlResourceDefinition(@PathVariable final String resName,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType

			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));
			response.setContentType(RestUtil.getContentType("xml",
					config.getProperty(Config.KEY_RESPONSE_CHARSET)));
			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);
			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				if (null == marshaller) {
					JAXBContext context = JAXBContext.newInstance(
							ObjectFactory.class,
							AbstractSqlResourceMetaData.class);

					marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
							Boolean.TRUE);

				}

				marshaller.marshal(sqlResource.getDefinition(), writer);

			}

		} catch (JAXBException e) {

			e.printStackTrace();
		} catch (SqlResourceFactoryException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genThrowableMessage(e));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}

	}

	@RequestMapping(value = "/file/{resName:.+}", method = RequestMethod.GET)
	public void getSqlResourceFile(@PathVariable final String resName,
			HttpServletRequest request, HttpServletResponse response) {

		InputStream inputStream = null;

		try {
			// set CHARSET & ContentType

			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));
			response.setContentType(RestUtil.getContentType("xml",
					config.getProperty(Config.KEY_RESPONSE_CHARSET)));

			inputStream = sqlResourceFactory.getInputStream(resName);

			if (null == inputStream) {
				response.getOutputStream().print(
						genErrorMessage(ResourceNotExist));
			} else {

				StreamUtils.copy(inputStream, response.getOutputStream());

			}
		}

		catch (SqlResourceFactoryException e) {

			try {
				response.getOutputStream().print(genThrowableMessage(e));
			} catch (IOException e1) {
				logger.error(genThrowableMessage(e1));
			}
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genThrowableMessage(e));

		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {

					logger.error(genThrowableMessage(e));
				}
			}
		}

	}

	@RequestMapping(value = "/clean/result", method = RequestMethod.GET)
	public void clearResultCache(HttpServletRequest request,
			HttpServletResponse response) {
		getCache().cleanUp();
		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();

			writer.print(config.getProperty(Config.KEY_RESPONSE_SUCCESS_PREFIX)
					+ "Clean Result Cache Complete.");

		} catch (IOException e) {
			logger.error(config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)
					+ e.getMessage());

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	@RequestMapping(value = "/clean/resource", method = RequestMethod.GET)
	public void clearResourceCache(HttpServletRequest request,
			HttpServletResponse response) {
		sqlResourceFactory.cleanAllSqlResource();
		getCache().cleanUp();

		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();

			writer.print(config.getProperty(Config.KEY_RESPONSE_SUCCESS_PREFIX)
					+ "Clean Resource Cache Complete.");

		} catch (IOException e) {
			logger.error(config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)
					+ e.getMessage());

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	@RequestMapping(value = "/reload/{resName:.+}", method = RequestMethod.GET)
	public void reloadDefinition(@PathVariable final String resName,
			HttpServletRequest request, HttpServletResponse response) {
		getCache().cleanUp();
		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();
			sqlResourceFactory.reloadSqlResource(resName);

			writer.print(config.getProperty(Config.KEY_RESPONSE_SUCCESS_PREFIX)
					+ "Reload [" + resName + "] Complete.");
		} catch (SqlResourceFactoryException e) {
			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());
		} catch (SqlResourceException e) {
			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)
					+ e.getMessage());

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	@RequestMapping(value = "/reload/all", method = RequestMethod.GET)
	public void reloadAllDefinition(HttpServletRequest request,
			HttpServletResponse response) {
		getCache().cleanUp();
		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();
			sqlResourceFactory.reloadAllSqlResource();

			writer.print(config.getProperty(Config.KEY_RESPONSE_SUCCESS_PREFIX)
					+ "Reload ALL Resource Complete.");
		} catch (SqlResourceFactoryException e) {
			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());
		} catch (SqlResourceException e) {
			writer.print(genThrowableMessage(e));
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)
					+ e.getMessage());

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	@RequestMapping(value = "/load/resourceTree", method = RequestMethod.GET)
	public void getResourceTree(HttpServletRequest request,
			HttpServletResponse response) {

		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();

			TreeRoot treeRoot = new TreeRoot(
					resTreeService.getResourceTree(config
							.getProperty(Config.KEY_SQLRESOURCES_DIR)));
			if (null == mapper) {
				mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Include.NON_NULL);
			}
			mapper.writeValue(writer, treeRoot);
		} catch (IOException e) {
			logger.error(config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)
					+ e.getMessage());

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	private String genThrowableMessage(Throwable t) {
		return config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)
				+ t.getMessage();
	}

	private String genErrorMessage(String s) {
		return config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX) + s;
	}

	private RequestSQLParams genRequestSQLParams(String resName, String filter,
			String orderby, Integer limit, Integer offset) {

		RequestSQLParams params = new RequestSQLParams();
		params.setResName(resName);

		params.setFilter(filter);
		params.setOrderby(orderby);

		if (null == limit) {
			params.setLimit(Integer.valueOf(sqlResourceFactory.getConfig()
					.getProperty(Config.KEY_RESPONSE_SQL_LIMIT)));
			
		} else if (limit > 0
				&& limit <= Integer.valueOf(sqlResourceFactory.getConfig()
						.getProperty(Config.KEY_RESPONSE_SQL_MAX_LIMIT))) {
			params.setLimit(limit);
		} else {
			params.setLimit(Integer.valueOf(sqlResourceFactory.getConfig()
					.getProperty(Config.KEY_RESPONSE_SQL_MAX_LIMIT)));
		}

		if (null != offset) {
			params.setOffset(offset);
		}

		return params;
	}

	private Cache<RequestSQLParams, Object> getCache() {
		if (null == cache)
			cache = CacheBuilder
					.newBuilder()
					.maximumSize(
							Integer.valueOf(config
									.getProperty(Config.KEY_CACHE_RESULT_MAX_CACHE_SIZE)))
					.expireAfterWrite(
							Long.valueOf(config
									.getProperty(Config.KEY_CACHE_RESULT_EXPIRE_AFTER_WRITE)),
							TimeUnit.SECONDS).build();
		return cache;
	}

}
