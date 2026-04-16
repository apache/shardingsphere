/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDatabaseIdentifierTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType oracleDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    private final DatabaseType mySQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertContainsSchemaByString() {
        assertTrue(createDatabase(postgreSQLDatabaseType, "foo_schema").containsSchema("FOO_SCHEMA"));
    }
    
    @Test
    void assertGetSchemaByString() {
        ShardingSphereSchema schema = createSchema("foo_schema", postgreSQLDatabaseType);
        assertThat(createDatabase(postgreSQLDatabaseType, schema).getSchema("FOO_SCHEMA"), is(schema));
    }
    
    @Test
    void assertAddSchema() {
        ShardingSphereSchema schema = createSchema("foo_schema", postgreSQLDatabaseType);
        ShardingSphereDatabase database = createDatabase(postgreSQLDatabaseType);
        database.addSchema(schema);
        assertThat(database.getSchema("FOO_SCHEMA"), is(schema));
    }
    
    @Test
    void assertDropSchema() {
        ShardingSphereDatabase database = createDatabase(postgreSQLDatabaseType, "foo_schema");
        database.dropSchema("FOO_SCHEMA");
        assertFalse(database.containsSchema("foo_schema"));
    }
    
    @Test
    void assertContainsSchemaWithOracleRule() {
        assertTrue(createDatabase(oracleDatabaseType, "FOO_SCHEMA").containsSchema("foo_schema"));
    }
    
    @Test
    void assertContainsProtocolDefaultSchemaWithOracleStorageRule() {
        ShardingSphereSchema schema = new ShardingSphereSchema("test_db", mySQLDatabaseType,
                Collections.singleton(new ShardingSphereTable("T_ORDER", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("test_db", mySQLDatabaseType, createOracleResourceMetaData(),
                new RuleMetaData(Collections.emptyList()), Collections.singleton(schema), new ConfigurationProperties(new Properties()));
        assertTrue(database.containsSchema("TEST_DB"));
        assertTrue(database.getSchema("test_db").containsTable("t_order"));
    }
    
    @Test
    void assertAttachIdentifierContextToSchema() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType,
                Collections.singleton(new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList());
        ShardingSphereDatabase database = createDatabase(postgreSQLDatabaseType, schema);
        assertFalse(database.getSchema("foo_schema").containsTable("FOO_TBL"));
    }
    
    @Test
    void assertContainsSchemaWithSensitiveProps() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), MetadataIdentifierCaseSensitivity.SENSITIVE.name());
        ShardingSphereDatabase database =
                createDatabase(postgreSQLDatabaseType, new ConfigurationProperties(props), createSchema("foo_schema", postgreSQLDatabaseType));
        assertFalse(database.containsSchema("FOO_SCHEMA"));
    }
    
    @Test
    void assertDefaultPropsUseInsensitiveLookup() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", postgreSQLDatabaseType, createResourceMetaData(),
                new RuleMetaData(Collections.emptyList()), Collections.singleton(createSchema("foo_schema", postgreSQLDatabaseType)), new ConfigurationProperties(new Properties()));
        assertTrue(database.containsSchema("FOO_SCHEMA"));
    }
    
    @Test
    void assertRefreshIdentifierContext() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", java.sql.Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        ShardingSphereDatabase database = createDatabase(postgreSQLDatabaseType, schema);
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), MetadataIdentifierCaseSensitivity.SENSITIVE.name());
        database.refreshIdentifierContext(new ConfigurationProperties(props));
        assertFalse(database.containsSchema("FOO_SCHEMA"));
        assertFalse(database.getSchema("foo_schema").containsTable("FOO_TBL"));
        assertFalse(database.getSchema("foo_schema").getTable("foo_tbl").containsColumn("FOO_COL"));
    }
    
    private ShardingSphereDatabase createDatabase(final DatabaseType databaseType, final String schemaName) {
        return createDatabase(databaseType, createSchema(schemaName, databaseType));
    }
    
    private ShardingSphereDatabase createDatabase(final DatabaseType databaseType, final ShardingSphereSchema schema) {
        return createDatabase(databaseType, new ConfigurationProperties(new Properties()), schema);
    }
    
    private ShardingSphereDatabase createDatabase(final DatabaseType databaseType, final ConfigurationProperties props, final ShardingSphereSchema schema) {
        return new ShardingSphereDatabase("foo_db", databaseType, createResourceMetaData(), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema), props);
    }
    
    private ShardingSphereDatabase createDatabase(final DatabaseType databaseType) {
        return new ShardingSphereDatabase("foo_db", databaseType, createResourceMetaData(),
                new RuleMetaData(Collections.emptyList()), Collections.emptyList(), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereSchema createSchema(final String schemaName, final DatabaseType databaseType) {
        return new ShardingSphereSchema(schemaName, databaseType);
    }
    
    private ResourceMetaData createResourceMetaData() {
        return new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
    }
    
    private ResourceMetaData createOracleResourceMetaData() {
        Map<String, Object> props = new LinkedHashMap<>(2, 1F);
        props.put("url", "jdbc:oracle:thin:@localhost:1521:xe");
        props.put("username", "root");
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(1, 1F);
        storageUnits.put("ds_0", new StorageUnit(new StorageNode("ds_0"), new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props), new FixtureDataSource()));
        return new ResourceMetaData(Collections.emptyMap(), storageUnits);
    }
    
    private static final class FixtureDataSource implements DataSource {
        
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLFeatureNotSupportedException("Not supported in fixture.");
        }
        
        @Override
        public Connection getConnection(final String username, final String password) throws SQLException {
            throw new SQLFeatureNotSupportedException("Not supported in fixture.");
        }
        
        @Override
        public PrintWriter getLogWriter() {
            return null;
        }
        
        @Override
        public void setLogWriter(final PrintWriter out) {
        }
        
        @Override
        public void setLoginTimeout(final int seconds) {
        }
        
        @Override
        public int getLoginTimeout() {
            return 0;
        }
        
        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            throw new SQLFeatureNotSupportedException("Not supported in fixture.");
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) {
            return false;
        }
    }
}
