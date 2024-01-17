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

import org.apache.shardingsphere.distsql.handler.type.rql.RQLExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

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
 * Show single table executor.
 */
public final class ShowSingleTableExecutor implements RQLExecutor<ShowSingleTableStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowSingleTableStatement sqlStatement) {
        SingleRule singleRule = database.getRuleMetaData().getSingleRule(SingleRule.class);
        Map<String, Collection<DataNode>> singleTableNodes = singleRule.getSingleTableDataNodes();
        return getRows(singleTableNodes, sqlStatement) ;
    }
    
    private Collection<LocalDataQueryResultRow> getRows(final Map<String, Collection<DataNode>> singleTableNodes, final ShowSingleTableStatement sqlStatement) {
        Collection<DataNode> resultDataNodes = new LinkedList<>();
        Optional<Pattern> pattern = getPattern(sqlStatement);
        for (final Entry<String, Collection<DataNode>> entry : singleTableNodes.entrySet()) {
            if (pattern.isPresent()) {
                if (pattern.get().matcher(entry.getKey()).matches()) {
                    resultDataNodes.add(entry.getValue().iterator().next());
                }
            } else {
                if (!sqlStatement.getTableName().isPresent() || sqlStatement.getTableName().get().equalsIgnoreCase(entry.getKey())) {
                    resultDataNodes.add(entry.getValue().iterator().next());
                }
            }
        }
        Collection<DataNode> sortedDataNodes = resultDataNodes.stream().sorted(Comparator.comparing(DataNode::getTableName)).collect(Collectors.toList());
        return sortedDataNodes.stream().map(each -> new LocalDataQueryResultRow(each.getTableName(), each.getDataSourceName())).collect(Collectors.toList());
    }
    
    private Optional<Pattern> getPattern(final ShowSingleTableStatement sqlStatement) {
        return sqlStatement.getLikePattern().isPresent()
                ? Optional.of(Pattern.compile(SQLUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get()), Pattern.CASE_INSENSITIVE))
                : Optional.empty();
    }
    
    @Override
    public Class<ShowSingleTableStatement> getType() {
        return ShowSingleTableStatement.class;
    }
}
