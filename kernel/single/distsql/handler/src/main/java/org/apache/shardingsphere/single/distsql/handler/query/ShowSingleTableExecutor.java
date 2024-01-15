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

import org.apache.shardingsphere.distsql.handler.type.rql.rule.RuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Show single table executor.
 */
public final class ShowSingleTableExecutor extends RuleAwareRQLExecutor<ShowSingleTableStatement, SingleRule> {
    
    public ShowSingleTableExecutor() {
        super(SingleRule.class);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowSingleTableStatement sqlStatement, final SingleRule rule) {
        return getDataNodes(sqlStatement, rule).stream().map(each -> new LocalDataQueryResultRow(each.getTableName(), each.getDataSourceName())).collect(Collectors.toList());
    }
    
    private Collection<DataNode> getDataNodes(final ShowSingleTableStatement sqlStatement, final SingleRule rule) {
        Stream<DataNode> singleTableNodes = rule.getSingleTableDataNodes().values().stream().filter(Objects::nonNull).map(each -> each.iterator().next());
        if (sqlStatement.getTableName().isPresent()) {
            singleTableNodes = singleTableNodes.filter(each -> sqlStatement.getTableName().get().equals(each.getTableName()));
        }
        if (sqlStatement.getLikePattern().isPresent()) {
            String pattern = SQLUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get());
            singleTableNodes = singleTableNodes.filter(each -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(each.getTableName()).matches()).collect(Collectors.toList()).stream();
        }
        return singleTableNodes.sorted(Comparator.comparing(DataNode::getTableName)).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShowSingleTableStatement> getType() {
        return ShowSingleTableStatement.class;
    }
}
