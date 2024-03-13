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
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Single rule.
 */
public final class SingleRule implements DatabaseRule {
    
    @Getter
    private final SingleRuleConfiguration configuration;
    
    private final String defaultDataSource;
    
    @Getter
    private final Collection<String> dataSourceNames;
    
    private final Map<String, Collection<DataNode>> singleTableDataNodes;
    
    private final DatabaseType protocolType;
    
    private final SingleMutableDataNodeRuleAttribute mutableDataNodeRuleAttribute;
    
    @Getter
    private final RuleAttributes attributes;
    
    public SingleRule(final SingleRuleConfiguration ruleConfig, final String databaseName,
                      final DatabaseType protocolType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        defaultDataSource = ruleConfig.getDefaultDataSource().orElse(null);
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSources(databaseName, dataSourceMap);
        Map<String, DataSource> aggregateDataSourceMap = SingleTableLoadUtils.getAggregatedDataSourceMap(enabledDataSources, builtRules);
        dataSourceNames = aggregateDataSourceMap.keySet();
        this.protocolType = protocolType;
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseName, protocolType, aggregateDataSourceMap, builtRules, configuration.getTables());
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        mutableDataNodeRuleAttribute = new SingleMutableDataNodeRuleAttribute(configuration, dataSourceNames, singleTableDataNodes, protocolType, tableMapperRuleAttribute);
        attributes = new RuleAttributes(
                new SingleDataNodeRuleAttribute(singleTableDataNodes), tableMapperRuleAttribute, new SingleExportableRuleAttribute(tableMapperRuleAttribute), mutableDataNodeRuleAttribute);
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
        Optional<DataNode> sampleDataNode = mutableDataNodeRuleAttribute.findTableDataNode(sampleTable.getSchemaName(), sampleTable.getTableName());
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
            Optional<DataNode> dataNode = mutableDataNodeRuleAttribute.findTableDataNode(each.getSchemaName(), each.getTableName());
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
}
