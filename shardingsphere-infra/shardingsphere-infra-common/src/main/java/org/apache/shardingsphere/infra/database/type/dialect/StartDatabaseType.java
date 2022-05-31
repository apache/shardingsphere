package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.SQL92DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class StartDatabaseType implements DatabaseType {

    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.NONE;
    }

    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singletonList("jdbc:start-db:");
    }

    @Override
    public DataSourceMetaData getDataSourceMetaData(String url, String username) {
        return new SQL92DataSourceMetaData(url);
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
        return Collections.singletonList("START-DB");
    }

    @Override
    public String getType() {
        return "MYSQL";
    }
}
