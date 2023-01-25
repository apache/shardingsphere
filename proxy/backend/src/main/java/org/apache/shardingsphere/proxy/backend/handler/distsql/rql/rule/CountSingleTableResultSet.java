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

import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSingleTableStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Result set for single table.
 */
public final class CountSingleTableResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Entry<String, Integer>> data = Collections.emptyIterator();
    
    private String databaseName;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("database", "count");
    }
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<SingleRule> rule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        databaseName = database.getName();
        Map<String, Integer> result = new LinkedHashMap<>();
        rule.ifPresent(optional -> addSingleTableData(result, databaseName, rule.get()));
        data = result.entrySet().iterator();
    }
    
    private void addSingleTableData(final Map<String, Integer> rowMap, final String databaseName, final SingleRule rule) {
        rowMap.put(databaseName, rule.getAllTables().size());
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, Integer> entry = data.next();
        return Arrays.asList(entry.getKey(), entry.getValue());
    }
    
    @Override
    public String getType() {
        return CountSingleTableStatement.class.getName();
    }
}
