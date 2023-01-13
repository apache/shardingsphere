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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.util.RegularUtil;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Result set for show single table.
 */
public final class SingleTableResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<DataNode> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShowSingleTableStatement showSingleTableStatement = (ShowSingleTableStatement) sqlStatement;
        Stream<DataNode> singleTableNodes = database.getRuleMetaData().getRules().stream().filter(each -> each instanceof SingleRule)
                .map(each -> (SingleRule) each).map(each -> each.getSingleTableDataNodes().values()).flatMap(Collection::stream).filter(Objects::nonNull).map(each -> each.iterator().next());
        if (showSingleTableStatement.getTableName().isPresent()) {
            singleTableNodes = singleTableNodes.filter(each -> showSingleTableStatement.getTableName().get().equals(each.getTableName()));
        }
        if (showSingleTableStatement.getLikePattern().isPresent()) {
            String pattern = SQLUtil.convertLikePatternToRegex(showSingleTableStatement.getLikePattern().get());
            singleTableNodes = singleTableNodes.filter(each -> RegularUtil.matchesCaseInsensitive(pattern, each.getTableName())).collect(Collectors.toList()).stream();
        }
        data = singleTableNodes.sorted(Comparator.comparing(DataNode::getTableName)).collect(Collectors.toList()).iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "storage_unit_name");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        DataNode next = data.next();
        return Arrays.asList(next.getTableName(), next.getDataSourceName());
    }
    
    @Override
    public String getType() {
        return ShowSingleTableStatement.class.getName();
    }
}
