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

package com.sphereex.dbplusengine.data.pipeline.distsql.handler.ddl.query;

import com.sphereex.dbplusengine.data.pipeline.ddl.distsql.statement.CheckTableMetadataStatement;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.consistency.metadata.DDLStatementTableMetadataChecker;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.consistency.metadata.MetadataCheckResult;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.consistency.util.DDLTaskUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.broadcast.rule.attribute.BroadcastDataNodeRuleAttribute;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SchemaMetaDataUtils;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Check table metadata executor.
 */
@Setter
public final class CheckTableMetadataExecutor implements DistSQLQueryExecutor<CheckTableMetadataStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final CheckTableMetadataStatement sqlStatement) {
        return Arrays.asList("table", "type", "is_consistent", "sample", "inconsistent_nodes", "total_count", "inconsistent_count", "is_consistent_with_logic_table", "message");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final CheckTableMetadataStatement sqlStatement, final ContextManager contextManager) throws SQLException {
        ComputeNodeInstanceContext instanceContext = contextManager.getComputeNodeInstanceContext();
        ConfigurationProperties props = getCheckTableMetadataConfigurationProperties();
        return getRows(sqlStatement, getShardingRule(contextManager), getBroadcastRule(), getSingleRule(), props, instanceContext);
    }
    
    private Collection<LocalDataQueryResultRow> getRows(final CheckTableMetadataStatement sqlStatement, final ShardingRule shardingRule, final BroadcastRule broadcastRule,
                                                        final SingleRule singleRule, final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        String defaultSchemaName = new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
        ShardingSphereSchema schema = database.getSchema(defaultSchemaName);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        if (null == schema) {
            return result;
        }
        Collection<String> tableNames = sqlStatement.isCheckAll() ? schema.getAllTableNames() : sqlStatement.getTables();
        for (String each : tableNames) {
            if (shardingRule.isShardingTable(each)) {
                if (schema.containsTable(each)) {
                    shardingRule.getShardingTable(each);
                    result.add(buildRow("sharding", each, getTotalNodesCount(shardingRule, each), props, instanceContext, schema.getTable(each), defaultSchemaName));
                } else {
                    result.add(buildTableNotExistRow("sharding", each));
                }
            } else if (broadcastRule.isAllBroadcastTables(Collections.singleton(each))) {
                if (schema.containsTable(each)) {
                    result.add(buildRow("broadcast", each, getTotalNodesCount(broadcastRule, each), props, instanceContext, schema.getTable(each), defaultSchemaName));
                } else {
                    result.add(buildTableNotExistRow("broadcast", each));
                }
            } else if (singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains(each)) {
                if (schema.containsTable(each)) {
                    result.add(buildSingleRow(each, props, instanceContext, schema.getTable(each), defaultSchemaName));
                } else {
                    result.add(buildTableNotExistRow("single", each));
                }
            } else {
                result.add(buildTableNotExistRow("unknown", each));
            }
        }
        return result;
    }
    
    private int getTotalNodesCount(final ShardingRule shardingRule, final String tableName) {
        return shardingRule.getShardingTable(tableName).getActualDataNodes().size();
    }
    
    private int getTotalNodesCount(final BroadcastRule broadcastRule, final String tableName) {
        return broadcastRule.getAttributes().getAttribute(BroadcastDataNodeRuleAttribute.class).getDataNodesByTableName(tableName).size();
    }
    
    private LocalDataQueryResultRow buildRow(final String type, final String tableName, final int totalNodesCount, final ConfigurationProperties props,
                                             final ComputeNodeInstanceContext instanceContext, final ShardingSphereTable logicTable, final String defaultSchemaName) throws SQLException {
        CheckResult checkResult = checkMetadata(tableName, props, instanceContext, logicTable, defaultSchemaName);
        return new LocalDataQueryResultRow(tableName, type, checkResult.getViolationNames().isEmpty(), checkResult.getSampleName(), checkResult.getViolationNames(),
                totalNodesCount, checkResult.getViolationNames().size(), checkResult.isConsistentWithLogicTable,
                checkResult.getViolationNames().isEmpty() ? checkResult.getLogicTableMessage() : checkResult.getReason());
    }
    
    private LocalDataQueryResultRow buildSingleRow(final String tableName, final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext,
                                                   final ShardingSphereTable logicTable, final String defaultSchemaName) throws SQLException {
        CheckResult checkResult = checkMetadata(tableName, props, instanceContext, logicTable, defaultSchemaName);
        return new LocalDataQueryResultRow(tableName, "single", "true", "", "", "1", "0", checkResult.isConsistentWithLogicTable, checkResult.getLogicTableMessage());
    }
    
    private LocalDataQueryResultRow buildTableNotExistRow(final String type, final String tableName) {
        return new LocalDataQueryResultRow(tableName, type, "", "", "", "", "", "false", "Logic table does not exist");
    }
    
    private ConfigurationProperties getCheckTableMetadataConfigurationProperties() {
        Properties innerProps = new Properties();
        innerProps.setProperty(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), String.valueOf(true));
        return new ConfigurationProperties(innerProps);
    }
    
    private CheckResult checkMetadata(final String logicTableName, final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext,
                                      final ShardingSphereTable logicTable, final String defaultSchemaName) throws SQLException {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(), database.getResourceMetaData().getStorageUnits(),
                database.getRuleMetaData().getRules(), props, defaultSchemaName, instanceContext);
        Collection<MetaDataLoaderMaterial> materials = SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton(logicTableName), material, true);
        try {
            Map<String, SchemaMetaData> schemaMetaDataMap = materials.isEmpty() ? Collections.emptyMap() : MetaDataLoader.load(materials);
            new MetaDataReviseEngine(database.getRuleMetaData().getRules()).revise(schemaMetaDataMap, material);
            if (schemaMetaDataMap.isEmpty()) {
                return new CheckResult(true, null);
            }
            MetadataCheckResult metadataCheckResult = isConsistentWithLogicTable(logicTable, schemaMetaDataMap);
            return new CheckResult(metadataCheckResult.isConsistent(), metadataCheckResult.getMessage());
        } catch (final RuleAndStorageMetaDataMismatchedException ex) {
            return new CheckResult(ex.getSampleName(), ex.getViolationNames(), ex.getReason(), null, null);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new SQLException("", ex);
        }
    }
    
    private MetadataCheckResult isConsistentWithLogicTable(final ShardingSphereTable logicTable, final Map<String, SchemaMetaData> schemaMetaDataMap) {
        Collection<TableMetaData> tableMetaDataList = schemaMetaDataMap.values().iterator().next().getTables();
        TableMetaData tableMetaData = tableMetaDataList.iterator().next();
        ShardingSphereTable physicalTable = new ShardingSphereTable(tableMetaData.getName(), DDLTaskUtils.convertToColumns(tableMetaData.getColumns()),
                DDLTaskUtils.convertToIndexes(tableMetaData.getIndexes()), DDLTaskUtils.convertToConstraints(tableMetaData.getConstraints()));
        return DDLStatementTableMetadataChecker.checkTableMetadata(logicTable, physicalTable, tableMetaData.getStorageUnitName());
    }
    
    private ShardingRule getShardingRule(final ContextManager contextManager) {
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        return shardingRule.orElseGet(() -> new ShardingRule(new ShardingRuleConfiguration(), database.getResourceMetaData().getDataSourceMap(), contextManager.getComputeNodeInstanceContext()));
    }
    
    private BroadcastRule getBroadcastRule() {
        Optional<BroadcastRule> broadcastRule = database.getRuleMetaData().findSingleRule(BroadcastRule.class);
        return broadcastRule.orElseGet(() -> new BroadcastRule(new BroadcastRuleConfiguration(Collections.emptyList()),
                database.getName(), database.getResourceMetaData().getDataSourceMap(), Collections.emptyList()));
    }
    
    private SingleRule getSingleRule() {
        Optional<SingleRule> singleRule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        return singleRule.orElseGet(() -> new SingleRule(new SingleRuleConfiguration(),
                database.getName(), database.getProtocolType(), database.getResourceMetaData().getDataSourceMap(), Collections.emptyList()));
    }
    
    @Override
    public Class<CheckTableMetadataStatement> getType() {
        return CheckTableMetadataStatement.class;
    }
    
    @AllArgsConstructor
    @Getter
    private static final class CheckResult {
        
        private final String sampleName;
        
        private final Collection<String> violationNames;
        
        private final String reason;
        
        private final Boolean isConsistentWithLogicTable;
        
        private final String logicTableMessage;
        
        private CheckResult(final Boolean isConsistentWithLogicTable, final String logicTableMessage) {
            this(null, Collections.emptyList(), null, isConsistentWithLogicTable, logicTableMessage);
        }
    }
}
