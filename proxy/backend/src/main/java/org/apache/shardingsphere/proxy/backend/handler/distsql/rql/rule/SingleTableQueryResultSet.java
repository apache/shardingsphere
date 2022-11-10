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
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Query result set for show single table.
 */
public final class SingleTableQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<DataNode> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShowSingleTableStatement showSingleTableStatement = (ShowSingleTableStatement) sqlStatement;
        Stream<DataNode> singleTableRules = database.getRuleMetaData().getRules().stream().filter(each -> each instanceof SingleTableRule)
                .map(each -> (SingleTableRule) each).map(each -> each.getSingleTableDataNodes().values()).flatMap(Collection::stream).filter(Objects::nonNull).map(each -> each.iterator().next());
        if (null != showSingleTableStatement.getTableName()) {
            singleTableRules = singleTableRules.filter(each -> showSingleTableStatement.getTableName().equals(each.getTableName()));
        }
        data = singleTableRules.sorted(Comparator.comparing(DataNode::getTableName)).collect(Collectors.toList()).iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "resource_name");
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
