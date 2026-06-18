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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.view;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateViewPushDownMetaDataRefresherTest {
    
    private static final String LOGIC_DATA_SOURCE_NAME = "logic_ds";
    
    private static final String SCHEMA_NAME = "PUBLIC";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertRefreshCreateViewUsesLoadedActualViewTable() throws SQLException {
        JdbcDataSource dataSource = createDataSource("create_view");
        executeUpdate(dataSource, "CREATE VIEW \"Foo_View\" AS SELECT 1");
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        ShardingSphereDatabase database = createDatabase(dataSource, new RuleMetaData(Collections.emptyList()));
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("\"Foo_View\""))));
        new CreateViewPushDownMetaDataRefresher().refresh(persistService, database, LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME,
                databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getAlteredTableSchemaName(), is(SCHEMA_NAME));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("Foo_View"));
        assertThat(persistService.getAlteredViewSchemaName(), is(SCHEMA_NAME));
        assertThat(persistService.getAlteredViews().iterator().next().getName(), is("Foo_View"));
        assertThat(persistService.getAlteredViews().iterator().next().getViewDefinition(), is(sqlStatement.getViewDefinition()));
    }
    
    @Test
    void assertRefreshAlterSingleRuleConfigurationWhenSingleTableNeedsRefresh() throws SQLException {
        JdbcDataSource dataSource = createDataSource("create_view_single_rule");
        executeUpdate(dataSource, "CREATE VIEW \"Foo_View\" AS SELECT 1");
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(new SingleTableRule(new DataNode(LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME, "Foo_View"))));
        ShardingSphereDatabase database = createDatabase(dataSource, ruleMetaData);
        RecordingMetaDataManagerPersistService persistService = new RecordingMetaDataManagerPersistService();
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("\"Foo_View\""))));
        new CreateViewPushDownMetaDataRefresher().refresh(persistService, database, LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME,
                databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertTrue(persistService.isAlterSingleRuleConfigurationCalled());
        assertThat(persistService.getAlterSingleRuleConfigurationDatabase(), is(database));
        assertThat(persistService.getAlterSingleRuleConfigurationRuleMetaData(), is(ruleMetaData));
    }
    
    @Test
    void assertRefreshDoesNotAlterSingleRuleConfigurationWhenViewIsNotSingleTable() throws SQLException {
        JdbcDataSource dataSource = createDataSource("create_view_distributed");
        executeUpdate(dataSource, "CREATE VIEW \"Foo_View\" AS SELECT 1");
        ShardingSphereDatabase database = createDatabase(dataSource, new RuleMetaData(Collections.singleton(new DistributedTableRule("Foo_View"))));
        RecordingMetaDataManagerPersistService persistService = new RecordingMetaDataManagerPersistService();
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("\"Foo_View\""))));
        new CreateViewPushDownMetaDataRefresher().refresh(persistService, database, LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME,
                databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertFalse(persistService.isAlterSingleRuleConfigurationCalled());
    }
    
    private ShardingSphereDatabase createDatabase(final JdbcDataSource dataSource, final RuleMetaData ruleMetaData) {
        return new ShardingSphereDatabase("foo_db", databaseType,
                new ResourceMetaData(Collections.singletonMap(LOGIC_DATA_SOURCE_NAME, dataSource)), ruleMetaData, Collections.emptyList(), new ConfigurationProperties(new Properties()));
    }
    
    private JdbcDataSource createDataSource(final String databaseName) {
        JdbcDataSource result = new JdbcDataSource();
        result.setURL("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
    
    private void executeUpdate(final JdbcDataSource dataSource, final String... sqls) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
    
    private static final class SingleTableRule implements ShardingSphereRule {
        
        private final RuleConfiguration configuration = new SingleRuleConfiguration();
        
        private final RuleAttributes attributes;
        
        private SingleTableRule(final DataNode dataNode) {
            attributes = new RuleAttributes(new SingleTableMutableDataNodeRuleAttribute(dataNode));
        }
        
        @Override
        public RuleConfiguration getConfiguration() {
            return configuration;
        }
        
        @Override
        public RuleAttributes getAttributes() {
            return attributes;
        }
        
        @Override
        public int getOrder() {
            return 0;
        }
    }
    
    private static final class SingleTableMutableDataNodeRuleAttribute implements MutableDataNodeRuleAttribute {
        
        private final DataNode dataNode;
        
        private SingleTableMutableDataNodeRuleAttribute(final DataNode dataNode) {
            this.dataNode = dataNode;
        }
        
        @Override
        public void put(final String dataSourceName, final String schemaName, final String tableName) {
        }
        
        @Override
        public void remove(final String schemaName, final String tableName) {
        }
        
        @Override
        public void remove(final Collection<String> schemaNames, final String tableName) {
        }
        
        @Override
        public Optional<DataNode> findTableDataNode(final String schemaName, final String tableName) {
            return Optional.of(dataNode);
        }
        
        @Override
        public ShardingSphereRule reloadRule(final RuleConfiguration ruleConfig, final String databaseName,
                                             final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class DistributedTableRule implements ShardingSphereRule {
        
        private final RuleAttributes attributes;
        
        private DistributedTableRule(final String distributedTableName) {
            attributes = new RuleAttributes(new DistributedTableMapperRuleAttribute(distributedTableName));
        }
        
        @Override
        public RuleConfiguration getConfiguration() {
            return new SingleRuleConfiguration();
        }
        
        @Override
        public RuleAttributes getAttributes() {
            return attributes;
        }
        
        @Override
        public int getOrder() {
            return 0;
        }
    }
    
    private static final class DistributedTableMapperRuleAttribute implements TableMapperRuleAttribute {
        
        private final String distributedTableName;
        
        private DistributedTableMapperRuleAttribute(final String distributedTableName) {
            this.distributedTableName = distributedTableName;
        }
        
        @Override
        public Collection<String> getLogicTableNames() {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<String> getDistributedTableNames() {
            return Collections.singleton(distributedTableName);
        }
        
        @Override
        public Collection<String> getEnhancedTableNames() {
            return Collections.emptyList();
        }
    }
    
    private static final class RecordingMetaDataManagerPersistService implements MetaDataManagerPersistService {
        
        private boolean alterSingleRuleConfigurationCalled;
        
        private ShardingSphereDatabase alterSingleRuleConfigurationDatabase;
        
        private RuleMetaData alterSingleRuleConfigurationRuleMetaData;
        
        @Override
        public void createDatabase(final String databaseName) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void dropDatabase(final ShardingSphereDatabase database) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void createSchema(final ShardingSphereDatabase database, final String schemaName) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void renameSchema(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void dropSchema(final ShardingSphereDatabase database, final Collection<String> schemaNames) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void createTable(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereTable table) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void dropTables(final ShardingSphereDatabase database, final String schemaName, final Collection<String> tableNames) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void alterTables(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereTable> alteredTables) {
        }
        
        @Override
        public void alterViews(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereView> alteredViews) {
        }
        
        @Override
        public void dropViews(final ShardingSphereDatabase database, final String schemaName, final Collection<String> droppedViews) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void alterStorageUnits(final ShardingSphereDatabase database, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void unregisterStorageUnits(final ShardingSphereDatabase database, final Collection<String> toBeDroppedStorageUnitNames) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void alterSingleRuleConfiguration(final ShardingSphereDatabase database, final RuleMetaData ruleMetaData) {
            alterSingleRuleConfigurationCalled = true;
            alterSingleRuleConfigurationDatabase = database;
            alterSingleRuleConfigurationRuleMetaData = ruleMetaData;
        }
        
        @Override
        public void alterRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeAlteredRuleConfig) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void removeRuleConfigurationItem(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleItemConfig) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void removeRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleConfig, final String ruleType) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void alterGlobalRuleConfiguration(final RuleConfiguration globalRuleConfig) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void alterProperties(final Properties props) {
            throw new UnsupportedOperationException();
        }
        
        private boolean isAlterSingleRuleConfigurationCalled() {
            return alterSingleRuleConfigurationCalled;
        }
        
        private ShardingSphereDatabase getAlterSingleRuleConfigurationDatabase() {
            return alterSingleRuleConfigurationDatabase;
        }
        
        private RuleMetaData getAlterSingleRuleConfigurationRuleMetaData() {
            return alterSingleRuleConfigurationRuleMetaData;
        }
    }
}
