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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.table;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.table.TableNameReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CreateTablePushDownMetaDataRefresherTest {
    
    private static final String LOGIC_DATA_SOURCE_NAME = "logic_ds";
    
    private static final String SCHEMA_NAME = "PUBLIC";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertRefreshCreatesLoadedActualTable() throws SQLException {
        JdbcDataSource dataSource = createDataSource("create_table");
        executeUpdate(dataSource, "CREATE TABLE \"Foo_Tbl\" (id INT)");
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        ShardingSphereDatabase database = createDatabase(dataSource);
        CreateTableStatement sqlStatement = CreateTableStatement.builder()
                .databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("\"Foo_Tbl\""))))
                .build();
        new CreateTablePushDownMetaDataRefresher().refresh(persistService, database, LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME,
                databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(persistService.getCreatedTableSchemaName(), is(SCHEMA_NAME));
        assertThat(persistService.getCreatedTable().getName(), is("Foo_Tbl"));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertRefreshCreatedShardingTableRestoresTruncatedNamedIndexFromCreateTableStatementCandidate() throws SQLException {
        String logicTableName = "tbl";
        String firstActualTableName = "tbl_0";
        String secondActualTableName = "tbl_1";
        String logicIndexName = "named_index_boundary_case_abcdefghijklmnopqrstuvwxyz_0123456789";
        DatabaseType postgreSQLDatabaseType = mock(DatabaseType.class);
        when(postgreSQLDatabaseType.getType()).thenReturn("PostgreSQL");
        String firstActualIndexName = IndexMetaDataUtils.getActualIndexName(logicIndexName, firstActualTableName, postgreSQLDatabaseType);
        String secondActualIndexName = IndexMetaDataUtils.getActualIndexName(logicIndexName, secondActualTableName, postgreSQLDatabaseType);
        JdbcDataSource dataSource = createDataSource("create_sharding_table_with_truncated_index");
        executeUpdate(dataSource,
                String.format("CREATE TABLE \"%s\" (\"foo_col\" INT)", firstActualTableName),
                String.format("CREATE INDEX \"%s\" ON \"%s\" (\"foo_col\")", firstActualIndexName, firstActualTableName),
                String.format("CREATE TABLE \"%s\" (\"foo_col\" INT)", secondActualTableName),
                String.format("CREATE INDEX \"%s\" ON \"%s\" (\"foo_col\")", secondActualIndexName, secondActualTableName));
        CreateTableCandidateRule rule = new CreateTableCandidateRule(logicTableName, Arrays.asList(
                new DataNode(LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME, firstActualTableName), new DataNode(LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME, secondActualTableName)));
        Map<ShardingSphereRule, MetaDataReviseEntry<?>> reviseEntries = Collections.singletonMap(rule, new CreateTableCandidateMetaDataReviseEntry());
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        try (MockedStatic<OrderedSPILoader> mockedLoader = mockStatic(OrderedSPILoader.class, CALLS_REAL_METHODS)) {
            mockedLoader.when(() -> OrderedSPILoader.getServices(eq(MetaDataReviseEntry.class), anyCollection())).thenReturn((Map) reviseEntries);
            new CreateTablePushDownMetaDataRefresher().refresh(persistService, createDatabase(dataSource, new RuleMetaData(Collections.singleton(rule))), LOGIC_DATA_SOURCE_NAME, SCHEMA_NAME,
                    databaseType, createCreateTableStatement(logicTableName, logicIndexName), createPropertiesWithCheckMetaDataEnabled());
        }
        assertThat(persistService.getCreatedTableSchemaName(), is(SCHEMA_NAME));
        assertThat(persistService.getCreatedTable().getName(), is(logicTableName));
        assertThat(persistService.getCreatedTable().getAllIndexes().iterator().next().getName(), is(logicIndexName));
    }
    
    private ShardingSphereDatabase createDatabase(final JdbcDataSource dataSource) {
        return createDatabase(dataSource, new RuleMetaData(Collections.emptyList()));
    }
    
    private ShardingSphereDatabase createDatabase(final JdbcDataSource dataSource, final RuleMetaData ruleMetaData) {
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.singletonMap(LOGIC_DATA_SOURCE_NAME, dataSource)),
                ruleMetaData, Collections.emptyList(), new ConfigurationProperties(new Properties()));
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
    
    private CreateTableStatement createCreateTableStatement(final String logicTableName, final String logicIndexName) {
        ConstraintDefinitionSegment constraintDefinition = new ConstraintDefinitionSegment(0, 0);
        constraintDefinition.setIndexName(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue(logicIndexName))));
        constraintDefinition.getIndexColumns().add(new ColumnSegment(0, 0, new IdentifierValue("foo_col")));
        return CreateTableStatement.builder()
                .databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(logicTableName))))
                .constraintDefinitions(Collections.singleton(constraintDefinition))
                .build();
    }
    
    private ConfigurationProperties createPropertiesWithCheckMetaDataEnabled() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString());
        return new ConfigurationProperties(result);
    }
    
    private static final class CreateTableCandidateRule implements ShardingSphereRule {
        
        private final String logicTableName;
        
        private final CreateTableCandidateRuleAttribute ruleAttribute;
        
        private CreateTableCandidateRule(final String logicTableName, final Collection<DataNode> dataNodes) {
            this.logicTableName = logicTableName;
            ruleAttribute = new CreateTableCandidateRuleAttribute(logicTableName, dataNodes);
        }
        
        @Override
        public RuleConfiguration getConfiguration() {
            return new CreateTableCandidateRuleConfiguration();
        }
        
        @Override
        public RuleAttributes getAttributes() {
            return new RuleAttributes(ruleAttribute);
        }
        
        @Override
        public int getOrder() {
            return 0;
        }
        
        private String reviseTableName(final String tableName) {
            return ruleAttribute.findLogicTableByActualTable(tableName).orElse(tableName);
        }
    }
    
    private static final class CreateTableCandidateRuleConfiguration implements RuleConfiguration {
    }
    
    private static final class CreateTableCandidateRuleAttribute implements TableMapperRuleAttribute, DataNodeRuleAttribute {
        
        private final String logicTableName;
        
        private final Map<String, Collection<DataNode>> dataNodes;
        
        private CreateTableCandidateRuleAttribute(final String logicTableName, final Collection<DataNode> dataNodes) {
            this.logicTableName = logicTableName;
            this.dataNodes = new LinkedHashMap<>(1, 1F);
            this.dataNodes.put(logicTableName, dataNodes);
        }
        
        @Override
        public Collection<String> getLogicTableNames() {
            return Collections.singleton(logicTableName);
        }
        
        @Override
        public Collection<String> getDistributedTableNames() {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<String> getEnhancedTableNames() {
            return Collections.emptyList();
        }
        
        @Override
        public Map<String, Collection<DataNode>> getAllDataNodes() {
            return dataNodes;
        }
        
        @Override
        public Collection<DataNode> getDataNodesByTableName(final String tableName) {
            return dataNodes.getOrDefault(tableName, Collections.emptyList());
        }
        
        @Override
        public Optional<String> findFirstActualTable(final String logicTable) {
            return getDataNodesByTableName(logicTable).stream().findFirst().map(DataNode::getTableName);
        }
        
        @Override
        public boolean isNeedAccumulate(final Collection<String> tables) {
            return false;
        }
        
        @Override
        public Optional<String> findLogicTableByActualTable(final String actualTable) {
            return dataNodes.entrySet().stream().filter(entry -> entry.getValue().stream().map(DataNode::getTableName).anyMatch(each -> each.equalsIgnoreCase(actualTable)))
                    .map(Entry::getKey).findFirst();
        }
        
        @Override
        public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
            return findFirstActualTable(logicTable);
        }
        
        @Override
        public boolean isReplicaBasedDistribution() {
            return false;
        }
    }
    
    private static final class CreateTableCandidateMetaDataReviseEntry implements MetaDataReviseEntry<CreateTableCandidateRule> {
        
        @Override
        public Optional<TableNameReviser<CreateTableCandidateRule>> getTableNameReviser() {
            return Optional.of((originalName, rule) -> rule.reviseTableName(originalName));
        }
        
        @Override
        public Optional<IndexReviser<CreateTableCandidateRule>> getIndexReviser(final CreateTableCandidateRule rule, final String tableName) {
            return Optional.of(new CreateTableCandidateIndexReviser());
        }
        
        @Override
        public int getOrder() {
            return 0;
        }
        
        @Override
        public Class<CreateTableCandidateRule> getTypeClass() {
            return CreateTableCandidateRule.class;
        }
    }
    
    private static final class CreateTableCandidateIndexReviser implements IndexReviser<CreateTableCandidateRule> {
        
        @Override
        public Optional<IndexMetaData> revise(final String tableName, final IndexMetaData originalMetaData, final Collection<TableMetaData> originalTableMetaDataList,
                                              final Collection<TableMetaData> indexNameRecoveryCandidateTableMetaDataList, final CreateTableCandidateRule rule) {
            Collection<String> candidateIndexNames = indexNameRecoveryCandidateTableMetaDataList.stream()
                    .filter(each -> rule.logicTableName.equalsIgnoreCase(each.getName())).flatMap(each -> each.getIndexes().stream()).map(IndexMetaData::getName).collect(Collectors.toSet());
            IndexMetaData result = new IndexMetaData(IndexMetaDataUtils.findGeneratedLogicIndexName(
                    originalMetaData.getName(), tableName, candidateIndexNames).orElse(originalMetaData.getName()), originalMetaData.getColumns());
            result.setUnique(originalMetaData.isUnique());
            return Optional.of(result);
        }
    }
}
