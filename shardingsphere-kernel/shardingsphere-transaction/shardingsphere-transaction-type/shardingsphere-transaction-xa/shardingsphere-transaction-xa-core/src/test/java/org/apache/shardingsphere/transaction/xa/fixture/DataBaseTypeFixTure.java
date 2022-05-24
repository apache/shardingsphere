package org.apache.shardingsphere.transaction.xa.fixture;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DataBaseTypeFixTure implements DatabaseType {

	@Override
	public QuoteCharacter getQuoteCharacter() {
		return null;
	}

	@Override
	public Collection<String> getJdbcUrlPrefixes() {
		return null;
	}

	@Override
	public DataSourceMetaData getDataSourceMetaData(String url, String username) {
		return null;
	}

	@Override
	public Optional<String> getDataSourceClassName() {
		return Optional.empty();
	}

	@Override
	public Map<String, Collection<String>> getSystemDatabaseSchemaMap() {
		return null;
	}

	@Override
	public Collection<String> getSystemSchemas() {
		return null;
	}

	@Override
	public String getType() {
		return "TEST";
	}
}
