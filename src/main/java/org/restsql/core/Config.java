package org.restsql.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

import mq.restful.util.RestUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

	public static final String MYSQL_DB_DIALECT = "mysql";
	public static final String POSTGRESQL_DB_DIALECT = "postgresql";

	public static final String DEFAULT_SQLRESOURCES_DIR = "/sqlresources";
	public static final String DEFAULT_DATABASE_TYPE = "postgresql";
	public static final String DEFAULT_HTTP_CACHE_CONTROL = "no-cache, no-transform";
	public static final String DEFAULT_HTTP_ACCESS_CONTROL = "*";

	public static final String DEFAULT_RESPONSE_SQL_LIMIT = "500";
	public static final String DEFAULT_RESPONSE_SQL_MAX_LIMIT = "5000";
	public static final String DEFAULT_RESPONSE_SQL_OFFSET = "-1";
	
	public static final String DEFAULT_RESPONSE_CSV_LINE_SEPATATOR = "\n";
	public static final String DEFAULT_RESPONSE_CSV_COLUMN_SEPATATOR = ",";
	public static final String DEFAULT_RESPONSE_CSV_USE_HEADER = "false";
	
	public static final String DEFAULT_RESPONSE_OUTPUT_TYPE = "json";
	public static final String DEFAULT_RESPONSE_CHARSET = "UTF-8";
	public static final String DEFAULT_RESPONSE_ERROR_PREFIX = "[Error]";
	public static final String DEFAULT_RESPONSE_SUCCESS_PREFIX = "[Success]";

	
	public static final String DEFAULT_CACHE_RESULT_MAX_CACHE_SIZE = "1000";
	public static final String DEFAULT_CACHE_RESULT_EXPIRE_AFTER_WRITE = "60";

	public static final String KEY_SQLRESOURCES_DIR = "sqlresources.dir";
	public static final String KEY_DATABASE_TYPE = "database.type";
	public static final String KEY_HTTP_CACHE_CONTROL = "http.response.cacheControl";
	public static final String KEY_HTTP_ACCESS_CONTROL = "http.response.accessControl";

	public static final String KEY_RESPONSE_SQL_LIMIT = "response.sql.limit";
	public static final String KEY_RESPONSE_SQL_MAX_LIMIT = "response.sql.maxLimit";
	public static final String KEY_RESPONSE_SQL_OFFSET = "response.sql.offset";
	
	public static final String KEY_RESPONSE_CSV_LINE_SEPATATOR = "response.cvs.lineSeparator";
	public static final String KEY_RESPONSE_CSV_COLUMN_SEPATATOR = "response.cvs.columnSeparator";
	public static final String KEY_RESPONSE_CSV_USE_HEADER = "response.cvs.useHeader";
	
	public static final String KEY_RESPONSE_OUTPUT_TYPE = "response.outputType";
	public static final String KEY_RESPONSE_CHARSET = "response.charset";
	public static final String KEY_RESPONSE_ERROR_PREFIX = "response.error.prefix";
	public static final String KEY_RESPONSE_SUCCESS_PREFIX = "response.success.prefix";


	public static final String KEY_CACHE_RESULT_MAX_CACHE_SIZE = "cache.result.maxCacheSize";
	public static final String KEY_CACHE_RESULT_EXPIRE_AFTER_WRITE = "cache.result.expireAfterWrite";

	private Properties properties = new Properties();

	Logger logger = LoggerFactory.getLogger(Config.class);

	public Config(String restsqlPropertiesFileName) {

		if (RestUtil.stringNotNullOrEmpty(restsqlPropertiesFileName)) {
			InputStream inputStream = null;

			try {
				File file = new File(restsqlPropertiesFileName);
				if (file.exists()) {
					inputStream = new FileInputStream(file);
				}

				if (inputStream != null) {
					properties.load(inputStream);
				}
			} catch (final Exception exception) {
				logger.error("Error loading properties from {}:{}",
						restsqlPropertiesFileName, exception.toString());
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (final IOException ignored) {
					}
				}
			}
		} else {
			InputStream inputStream = null;

			try {

				File file = new File(Config.class.getClassLoader()
						.getResource("").getPath()
						+ "restsql.properties");
				if (file.exists()) {
					inputStream = new FileInputStream(file);
				}
				if (inputStream != null) {
					properties.load(inputStream);
				}
			} catch (final Exception exception) {
				logger.error("Error loading restsql.properties");
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (final IOException ignored) {
					}
				}
			}

		}

		for (final Field field : Config.class.getFields()) {
			final String keyFieldName = field.getName();
			if (keyFieldName.startsWith("KEY")) {
				String keyFieldValue;
				try {
					keyFieldValue = (String) field.get(null);
					if (!properties.containsKey(keyFieldValue)) {

						final Field valueField = Config.class
								.getField("DEFAULT_"
										+ keyFieldName.substring(4));
						properties.put(keyFieldValue,
								(String) valueField.get(null));

					}
				} catch (final Exception exception) {
					logger.error("Error dumping config:{}", exception); // this
																		// should
																		// never
																		// happen
				}
			}
		}

	}

	public void setSqlResourcesDir(String dir) {
		if (RestUtil.stringNotNullOrEmpty(dir))
			this.properties.put(KEY_SQLRESOURCES_DIR, dir);
	}

	public String getProperty(final String key, final String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public String getProperty(final String key) {
		return properties.getProperty(key);
	}

	public Set<Object> keySet() {
		return properties.keySet();
	}

	public boolean containsKey(final String key) {
		return properties.containsKey(key);
	}

}
