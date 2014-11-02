package mq.restful.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import mq.restful.model.TreeRoot;
import mq.restful.util.MQPrettyPrinter;
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
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Controller
@RequestMapping(value = "/res")
public class ResResource {

	private final String ResourceNotExist = "请检查Resource Name,该Resource不存在.";
	private final String CsvNoOneToMore = "CsvMapper不支持一对多关系.";

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
			@RequestParam(value = "_groupby", required = false) String groupby,
			@RequestParam(value = "_visible", required = false) String visible,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;
		// System.out.println(resName);
		try {
			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);

			processResponse(response, sqlResource, output);

			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				RequestSQLParams requestSQLParams = genRequestSQLParams(
						resName, filter, groupby, orderby, limit, offset,
						visible);

				Object result = getCache().getIfPresent(requestSQLParams);

				if (null == result) {

					try {
						result = sqlResource.read(requestSQLParams);
					} catch (DataAccessException | CQLException
							| FilterToSQLException e) {
						writer.print(genErrorMessage(e.getMessage()));
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

						processBuilder(builder, sqlResource, visible);

						CsvSchema schema = processCsvSchema(sqlResource,
								builder);
						((CsvMapper) mapper).writer(schema).writeValue(writer,
								result);
					} else {
						writer.print(genErrorMessage(CsvNoOneToMore));
					}
				} else {
					mapper.writeValue(writer, result);
				}

			}

		} catch (UnsupportedEncodingException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		}

		catch (SqlResourceFactoryException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}

	}

	private void processBuilder(Builder builder, SqlResource sqlResource,
			String visible) {
		for (ColumnMetaData columnData : sqlResource.getMetaData()
				.getParentReadColumns()) {
			if (!columnData.isNonqueriedForeignKey()) {
				boolean addNode = true;
				if (visible != null && visible.length() > 0) {
					if (columnData.getColumnNumber() <= visible.length()
							&& visible.charAt(columnData.getColumnNumber() - 1) == '0') {
						addNode = false;
					}

				}
				if (addNode)
					builder.addColumn(columnData.getColumnLabel());
			}
		}

	}

	MQPrettyPrinter pp = new MQPrettyPrinter();

	@RequestMapping(value = "/pretty/{resName:.+}", method = RequestMethod.GET)
	public void getPretty(@PathVariable final String resName,
			@RequestParam(value = "_filter", required = false) String filter,
			@RequestParam(value = "_orderby", required = false) String orderby,
			@RequestParam(value = "_limit", required = false) Integer limit,
			@RequestParam(value = "_offset", required = false) Integer offset,
			@RequestParam(value = "_output", required = false) String output,
			@RequestParam(value = "_groupby", required = false) String groupby,
			@RequestParam(value = "_visible", required = false) String visible,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;

		try {

			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);

			processResponse(response, sqlResource, output);

			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				RequestSQLParams requestSQLParams = genRequestSQLParams(
						resName, filter, groupby, orderby, limit, offset,
						visible);

				Object result = getCache().getIfPresent(requestSQLParams);

				if (null == result) {

					try {
						result = sqlResource.read(requestSQLParams);
					} catch (DataAccessException | CQLException
							| FilterToSQLException e) {
						writer.print(genErrorMessage(e.getMessage()));
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
						processBuilder(builder, sqlResource, visible);

						CsvSchema schema = processCsvSchema(sqlResource,
								builder);
						((CsvMapper) mapper).writer(schema).writeValue(writer,
								result);
					} else {
						writer.print(genErrorMessage(CsvNoOneToMore));
					}
				} else if (mapper instanceof XmlMapper) {
					mapper.writeValue(writer, result);
				} else {
					mapper.writer(pp).writeValue(writer, result);
				}

			}

		} catch (UnsupportedEncodingException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (SqlResourceFactoryException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}

	}

	@RequestMapping(value = "/jsonp/{resName:.+}", method = RequestMethod.GET)
	public void getJsonp(
			@PathVariable final String resName,
			@RequestParam(value = "_filter", required = false) String filter,
			@RequestParam(value = "_orderby", required = false) String orderby,
			@RequestParam(value = "_limit", required = false) Integer limit,
			@RequestParam(value = "_offset", required = false) Integer offset,
			@RequestParam(value = "_output", required = false) String output,
			@RequestParam(value = "_groupby", required = false) String groupby,
			@RequestParam(value = "_visible", required = false) String visible,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;
		// System.out.println(resName);
		output = "json";
		if (null == callback) {
			callback = "success";
		}
		try {

			writer = response.getWriter();

			SqlResource sqlResource = sqlResourceFactory
					.getSqlResource(resName);

			processResponse(response, sqlResource, output);

			if (null == sqlResource) {
				writer.print(genErrorMessage(ResourceNotExist));
			} else {

				RequestSQLParams requestSQLParams = genRequestSQLParams(
						resName, filter, groupby, orderby, limit, offset,
						visible);

				Object result = getCache().getIfPresent(requestSQLParams);

				if (null == result) {

					try {
						result = sqlResource.read(requestSQLParams);
					} catch (DataAccessException | CQLException
							| FilterToSQLException e) {
						writer.print(genErrorMessage(e.getMessage()));
						logger.error(e.getMessage());
					}

					if (null != result) {
						cache.put(requestSQLParams, result);
					}
				}

				ObjectMapper mapper = RestUtil.getObjectMapper(output);

				mapper.writeValue(writer, new JSONPObject(callback, result));

			}

		} catch (UnsupportedEncodingException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		}

		catch (SqlResourceFactoryException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (SqlResourceFactoryException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (SqlResourceFactoryException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (SqlResourceException e) {

			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
				response.getOutputStream().print(
						genErrorMessage(e.getMessage()));
			} catch (IOException e1) {
				logger.error(genErrorMessage(e1.getMessage()));
			}
			logger.error(e.getMessage());

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {

					logger.error(genErrorMessage(e.getMessage()));
				}
			}
		}

	}

	@RequestMapping(value = "/clean/result", method = RequestMethod.GET)
	public void clearResultCache(HttpServletRequest request,
			HttpServletResponse response) {
		getCache().invalidateAll();
		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();

			writer.print(genSuccessMessage("Clean Result Cache Complete."));

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
		getCache().invalidateAll();

		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();

			writer.print(genSuccessMessage("Clean Resource Cache Complete."));

		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
		getCache().invalidateAll();
		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();
			sqlResourceFactory.reloadSqlResource(resName);

			writer.print(genSuccessMessage(new StringBuilder("Reload [")
					.append(resName).append("] Complete.").toString()));
		} catch (SqlResourceFactoryException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (SqlResourceException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
		getCache().invalidateAll();
		PrintWriter writer = null;

		try {
			// set CHARSET & ContentType
			response.setCharacterEncoding(config
					.getProperty(Config.KEY_RESPONSE_CHARSET));

			writer = response.getWriter();
			sqlResourceFactory.reloadAllSqlResource();

			writer.print(genSuccessMessage("Reload ALL Resource Complete."));
		} catch (SqlResourceFactoryException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (SqlResourceException e) {
			writer.print(genErrorMessage(e.getMessage()));
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(genErrorMessage(e.getMessage()));

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
			logger.error(genErrorMessage(e.getMessage()));

		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	private String genErrorMessage(String s) {
		return new StringBuilder(
				config.getProperty(Config.KEY_RESPONSE_ERROR_PREFIX)).append(s)
				.toString();
	}

	private String genSuccessMessage(String s) {
		return new StringBuilder(
				config.getProperty(Config.KEY_RESPONSE_SUCCESS_PREFIX)).append(
				s).toString();
	}

	private RequestSQLParams genRequestSQLParams(String resName, String filter,
			String groupby, String orderby, Integer limit, Integer offset,
			String visible) throws UnsupportedEncodingException {

		RequestSQLParams params = new RequestSQLParams();
		params.setResName(resName);

		if (null != filter) {
			params.setFilter(new String(filter.getBytes("ISO-8859-1"), "UTF-8"));
		}

		params.setOrderby(orderby);
		params.setGroupby(groupby);

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

		if (null != visible) {
			params.setVisible(visible);
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

	private void processResponse(HttpServletResponse response,
			SqlResource sqlResource, String output) {
		// set CHARSET & ContentType
		response.setCharacterEncoding(config
				.getProperty(Config.KEY_RESPONSE_CHARSET));
		response.setContentType(RestUtil.getContentType(output,
				config.getProperty(Config.KEY_RESPONSE_CHARSET)));

		String cacheControl = null;
		String accessControl = null;
		if (null != sqlResource
				&& null != sqlResource.getDefinition().getRestConfig()
				&& null != sqlResource.getDefinition().getRestConfig()
						.getHttpResponse()) {

			cacheControl = sqlResource.getDefinition().getRestConfig()
					.getHttpResponse().getCacheControl();

			accessControl = sqlResource.getDefinition().getRestConfig()
					.getHttpResponse().getAccessControl();

		}
		if (null == cacheControl) {
			cacheControl = config.getProperty(Config.KEY_HTTP_CACHE_CONTROL);
		}

		if (null == accessControl) {
			accessControl = config.getProperty(Config.KEY_HTTP_ACCESS_CONTROL);
		}

		if (RestUtil.stringNotNullOrEmpty(cacheControl)) {
			response.addHeader("Cache-Control", cacheControl);
		}

		if (RestUtil.stringNotNullOrEmpty(accessControl)) {
			response.addHeader("Access-Control-Allow-Origin", accessControl);
		}

	}

	private CsvSchema processCsvSchema(SqlResource sqlResource, Builder builder) {
		String lineSeparator = config
				.getProperty(Config.KEY_RESPONSE_CSV_LINE_SEPATATOR);
		char columnSeparator = config
				.getProperty(Config.KEY_RESPONSE_CSV_COLUMN_SEPATATOR).trim()
				.charAt(0);
		if (null != sqlResource
				&& null != sqlResource.getDefinition().getRestConfig()
				&& null != sqlResource.getDefinition().getRestConfig()
						.getCsvConfig()) {
			if (RestUtil.stringNotNullOrEmpty(sqlResource.getDefinition()
					.getRestConfig().getCsvConfig().getLineSeparator())) {
				lineSeparator = sqlResource.getDefinition().getRestConfig()
						.getCsvConfig().getLineSeparator()
						.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r");
			}

			if (RestUtil.stringNotNullOrEmpty(sqlResource.getDefinition()
					.getRestConfig().getCsvConfig().getColumnSeparator())) {
				columnSeparator = sqlResource.getDefinition().getRestConfig()
						.getCsvConfig().getColumnSeparator().trim().charAt(0);
			}
		}

		CsvSchema schema = builder.build().withLineSeparator(lineSeparator)
				.withColumnSeparator(columnSeparator);

		if (null != sqlResource
				&& null != sqlResource.getDefinition().getRestConfig()
				&& null != sqlResource.getDefinition().getRestConfig()
						.getCsvConfig()) {
			if (null != sqlResource.getDefinition().getRestConfig()
					.getCsvConfig().getUseHeader()
					&& sqlResource.getDefinition().getRestConfig()
							.getCsvConfig().getUseHeader()) {
				return schema.withHeader();
			}

		}

		return schema;

	}
}
