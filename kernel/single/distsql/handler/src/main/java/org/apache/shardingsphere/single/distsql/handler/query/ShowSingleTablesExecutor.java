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

package org.apache.shardingsphere.single.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTablesStatement;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show single tables executor.
 */
@Setter
public final class ShowSingleTablesExecutor implements DistSQLQueryExecutor<ShowSingleTablesStatement>, DistSQLExecutorRuleAware<SingleRule>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    private SingleRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowSingleTablesStatement sqlStatement) {
        return new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()
                ? Arrays.asList("table_name", "storage_unit_name", "schema_name")
                : Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSingleTablesStatement sqlStatement, final ContextManager contextManager) {
        Collection<DataNode> resultDataNodes = getPattern(sqlStatement)
                .map(optional -> getDataNodesWithLikePattern(rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes(), optional))
                .orElseGet(() -> getDataNodes(rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes()));
        Collection<DataNode> sortedDataNodes = resultDataNodes.stream().sorted(Comparator.comparing(DataNode::getTableName)).collect(Collectors.toList());
        boolean isSchemaAvailable = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable();
        return sortedDataNodes.stream().map(each -> isSchemaAvailable
                ? new LocalDataQueryResultRow(each.getTableName(), each.getDataSourceName(), each.getSchemaName())
                : new LocalDataQueryResultRow(each.getTableName(), each.getDataSourceName())).collect(Collectors.toList());
    }
    
    private Optional<Pattern> getPattern(final ShowSingleTablesStatement sqlStatement) {
        return sqlStatement.getLikePattern().isPresent()
                ? Optional.of(Pattern.compile(RegexUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get()), Pattern.CASE_INSENSITIVE))
                : Optional.empty();
    }
    
    private Collection<DataNode> getDataNodesWithLikePattern(final Map<String, Collection<DataNode>> singleTableNodes, final Pattern pattern) {
        Collection<DataNode> result = new LinkedList<>();
        for (Entry<String, Collection<DataNode>> entry : singleTableNodes.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches()) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
    
    private Collection<DataNode> getDataNodes(final Map<String, Collection<DataNode>> singleTableNodes) {
        Collection<DataNode> result = new LinkedList<>();
        for (Collection<DataNode> each : singleTableNodes.values()) {
            result.addAll(each);
        }
        return result;
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
    
    @Override
    public Class<ShowSingleTablesStatement> getType() {
        return ShowSingleTablesStatement.class;
    }
}
