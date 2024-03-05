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

package org.apache.shardingsphere.single.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.IndexAvailable;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RuleIdentifiers;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Single rule.
 */
public final class SingleRule implements DatabaseRule, MutableDataNodeRule, ExportableRule {
    
    @Getter
    private final SingleRuleConfiguration configuration;
    
    private final String defaultDataSource;
    
    @Getter
    private final Collection<String> dataSourceNames;
    
    private final Map<String, Collection<DataNode>> singleTableDataNodes;
    
    private final DatabaseType protocolType;
    
    private final SingleTableMapperRule tableMapperRule;
    
    @Getter
    private final RuleIdentifiers ruleIdentifiers;
    
    public SingleRule(final SingleRuleConfiguration ruleConfig, final String databaseName,
                      final DatabaseType protocolType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        defaultDataSource = ruleConfig.getDefaultDataSource().orElse(null);
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSources(databaseName, dataSourceMap);
        Map<String, DataSource> aggregateDataSourceMap = SingleTableLoadUtils.getAggregatedDataSourceMap(enabledDataSources, builtRules);
        dataSourceNames = aggregateDataSourceMap.keySet();
        this.protocolType = protocolType;
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseName, protocolType, aggregateDataSourceMap, builtRules, configuration.getTables());
        tableMapperRule = new SingleTableMapperRule(singleTableDataNodes.values());
        ruleIdentifiers = new RuleIdentifiers(new SingleDataNodeRule(singleTableDataNodes), tableMapperRule);
    }
    
    /**
     * Assign new data source name.
     *
     * @return assigned data source name
     */
    public String assignNewDataSourceName() {
        return null == defaultDataSource ? new ArrayList<>(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size())) : defaultDataSource;
    }
    
    /**
     * Judge whether all tables are in same compute node or not.
     * 
     * @param dataNodes data nodes
     * @param singleTables single tables
     * @return whether all tables are in same compute node or not
     */
    public boolean isAllTablesInSameComputeNode(final Collection<DataNode> dataNodes, final Collection<QualifiedTable> singleTables) {
        if (!isSingleTablesInSameComputeNode(singleTables)) {
            return false;
        }
        QualifiedTable sampleTable = singleTables.iterator().next();
        Optional<DataNode> sampleDataNode = findTableDataNode(sampleTable.getSchemaName(), sampleTable.getTableName());
        if (sampleDataNode.isPresent()) {
            for (DataNode each : dataNodes) {
                if (!isSameComputeNode(sampleDataNode.get().getDataSourceName(), each.getDataSourceName())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isSameComputeNode(final String sampleDataSourceName, final String dataSourceName) {
        return sampleDataSourceName.equalsIgnoreCase(dataSourceName);
    }
    
    private boolean isSingleTablesInSameComputeNode(final Collection<QualifiedTable> singleTables) {
        String sampleDataSourceName = null;
        for (QualifiedTable each : singleTables) {
            Optional<DataNode> dataNode = findTableDataNode(each.getSchemaName(), each.getTableName());
            if (!dataNode.isPresent()) {
                continue;
            }
            if (null == sampleDataSourceName) {
                sampleDataSourceName = dataNode.get().getDataSourceName();
                continue;
            }
            if (!isSameComputeNode(sampleDataSourceName, dataNode.get().getDataSourceName())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get single table names.
     *
     * @param qualifiedTables qualified tables
     * @return single table names
     */
    public Collection<QualifiedTable> getSingleTables(final Collection<QualifiedTable> qualifiedTables) {
        Collection<QualifiedTable> result = new LinkedList<>();
        for (QualifiedTable each : qualifiedTables) {
            Collection<DataNode> dataNodes = singleTableDataNodes.getOrDefault(each.getTableName().toLowerCase(), new LinkedList<>());
            if (!dataNodes.isEmpty() && containsDataNode(each, dataNodes)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean containsDataNode(final QualifiedTable qualifiedTable, final Collection<DataNode> dataNodes) {
        for (DataNode each : dataNodes) {
            if (qualifiedTable.getSchemaName().equalsIgnoreCase(each.getSchemaName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get qualified tables.
     *
     * @param sqlStatementContext sql statement context
     * @param database database
     * @return qualified tables
     */
    public Collection<QualifiedTable> getQualifiedTables(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        Collection<QualifiedTable> result = getQualifiedTables(database, protocolType, sqlStatementContext.getTablesContext().getSimpleTableSegments());
        if (result.isEmpty() && sqlStatementContext instanceof IndexAvailable) {
            result = IndexMetaDataUtils.getTableNames(database, protocolType, ((IndexAvailable) sqlStatementContext).getIndexes());
        }
        return result;
    }
    
    private Collection<QualifiedTable> getQualifiedTables(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<SimpleTableSegment> tableSegments) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(database.getName());
        for (SimpleTableSegment each : tableSegments) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            result.add(new QualifiedTable(actualSchemaName, each.getTableName().getIdentifier().getValue()));
        }
        return result;
    }
    
    @Override
    public void put(final String dataSourceName, final String schemaName, final String tableName) {
        if (dataSourceNames.contains(dataSourceName)) {
            Collection<DataNode> dataNodes = singleTableDataNodes.computeIfAbsent(tableName.toLowerCase(), key -> new LinkedHashSet<>());
            DataNode dataNode = new DataNode(dataSourceName, tableName);
            dataNode.setSchemaName(schemaName);
            dataNodes.add(dataNode);
            tableMapperRule.getLogicTableMapper().put(tableName);
            addTableConfiguration(dataSourceName, schemaName, tableName);
        }
    }
    
    private void addTableConfiguration(final String dataSourceName, final String schemaName, final String tableName) {
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(configuration.getTables());
        if (splitTables.contains(SingleTableLoadUtils.getAllTablesNodeStr(protocolType))
                || splitTables.contains(SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(protocolType, dataSourceName, schemaName))) {
            return;
        }
        String dataNodeString = SingleTableLoadUtils.getDataNodeString(protocolType, dataSourceName, schemaName, tableName);
        if (!configuration.getTables().contains(dataNodeString)) {
            configuration.getTables().add(dataNodeString);
        }
    }
    
    @Override
    public void remove(final String schemaName, final String tableName) {
        remove(Collections.singleton(schemaName.toLowerCase()), tableName);
    }
    
    @Override
    public void remove(final Collection<String> schemaNames, final String tableName) {
        if (!singleTableDataNodes.containsKey(tableName.toLowerCase())) {
            return;
        }
        Collection<DataNode> dataNodes = singleTableDataNodes.get(tableName.toLowerCase());
        Iterator<DataNode> iterator = dataNodes.iterator();
        while (iterator.hasNext()) {
            DataNode each = iterator.next();
            if (schemaNames.contains(each.getSchemaName().toLowerCase())) {
                iterator.remove();
                configuration.getTables().remove(SingleTableLoadUtils.getDataNodeString(protocolType, each.getDataSourceName(), each.getSchemaName(), tableName));
            }
        }
        if (dataNodes.isEmpty()) {
            singleTableDataNodes.remove(tableName.toLowerCase());
            tableMapperRule.getLogicTableMapper().remove(tableName);
        }
    }
    
    @Override
    public Optional<DataNode> findTableDataNode(final String schemaName, final String tableName) {
        Collection<DataNode> dataNodes = singleTableDataNodes.getOrDefault(tableName.toLowerCase(), new LinkedHashSet<>());
        for (DataNode each : dataNodes) {
            if (schemaName.equalsIgnoreCase(each.getSchemaName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public ShardingSphereRule reloadRule(final RuleConfiguration config, final String databaseName, final Map<String, DataSource> dataSourceMap,
                                         final Collection<ShardingSphereRule> builtRules) {
        return new SingleRule((SingleRuleConfiguration) config, databaseName, protocolType, dataSourceMap, builtRules);
    }
    
    @Override
    public Map<String, Object> getExportData() {
        return Collections.singletonMap(ExportableConstants.EXPORT_SINGLE_TABLES, tableMapperRule.getLogicTableMapper().getTableNames());
    }
}
