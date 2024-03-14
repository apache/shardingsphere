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
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show single table executor.
 */
@Setter
public final class ShowSingleTableExecutor implements DistSQLQueryExecutor<ShowSingleTableStatement>, DistSQLExecutorRuleAware<SingleRule> {
    
    private SingleRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowSingleTableStatement sqlStatement) {
        return Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSingleTableStatement sqlStatement, final ContextManager contextManager) {
        Collection<DataNode> resultDataNodes = getPattern(sqlStatement)
                .map(optional -> getDataNodesWithLikePattern(rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes(), optional))
                .orElseGet(() -> getDataNodes(rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes(), sqlStatement));
        Collection<DataNode> sortedDataNodes = resultDataNodes.stream().sorted(Comparator.comparing(DataNode::getTableName)).collect(Collectors.toList());
        return sortedDataNodes.stream().map(each -> new LocalDataQueryResultRow(each.getTableName(), each.getDataSourceName())).collect(Collectors.toList());
    }
    
    private Optional<Pattern> getPattern(final ShowSingleTableStatement sqlStatement) {
        return sqlStatement.getLikePattern().isPresent()
                ? Optional.of(Pattern.compile(RegexUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get()), Pattern.CASE_INSENSITIVE))
                : Optional.empty();
    }
    
    private Collection<DataNode> getDataNodesWithLikePattern(final Map<String, Collection<DataNode>> singleTableNodes, final Pattern pattern) {
        return singleTableNodes.entrySet().stream().filter(entry -> pattern.matcher(entry.getKey()).matches()).map(entry -> entry.getValue().iterator().next()).collect(Collectors.toList());
    }
    
    private Collection<DataNode> getDataNodes(final Map<String, Collection<DataNode>> singleTableNodes, final ShowSingleTableStatement sqlStatement) {
        return singleTableNodes.entrySet().stream().filter(entry -> !sqlStatement.getTableName().isPresent() || sqlStatement.getTableName().get().equalsIgnoreCase(entry.getKey()))
                .map(entry -> entry.getValue().iterator().next()).collect(Collectors.toList());
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
    
    @Override
    public Class<ShowSingleTableStatement> getType() {
        return ShowSingleTableStatement.class;
    }
}
