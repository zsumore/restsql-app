package org.restsql.core;

import java.io.InputStream;
import java.util.List;

public interface SqlResourceFactory {

	public SqlResource getSqlResource(final String resName)
			throws SqlResourceFactoryException, SqlResourceException;

	public InputStream getSqlResourceDefinition(String resName)
			throws SqlResourceFactoryException;

	public List<String> getSqlResourceNames()
			throws SqlResourceFactoryException;

	public String getSqlResourcesDir();

	public void reloadSqlResource(String resName)
			throws SqlResourceFactoryException, SqlResourceException;

	public void reloadAllSqlResource() throws SqlResourceFactoryException,
			SqlResourceException;

	public void cleanAllSqlResource();

	public boolean isSqlResourceLoaded(final String name);

	public Config getConfig();

	public InputStream getInputStream(final String resName)
			throws SqlResourceFactoryException;

}
