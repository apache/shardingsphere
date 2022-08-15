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

package org.apache.shardingsphere.encrypt.distsql.handler.query;

import org.apache.shardingsphere.encrypt.distsql.parser.statement.CountEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Query result set for count encrypt rule.
 */
public final class CountEncryptRuleQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final String ENCRYPT = "encrypt";
    
    private Iterator<Entry<String, LinkedList<Object>>> data = Collections.emptyIterator();
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "database", "count");
    }
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        Map<String, LinkedList<Object>> result = new LinkedHashMap<>();
        rule.ifPresent(optional -> addEncryptData(result, database.getName(), rule.get()));
        data = result.entrySet().iterator();
    }
    
    private void addEncryptData(final Map<String, LinkedList<Object>> rowMap, final String databaseName, final EncryptRule rule) {
        addData(rowMap, ENCRYPT, databaseName, () -> rule.getTables().size());
    }
    
    private void addData(final Map<String, LinkedList<Object>> rowMap, final String dataKey, final String databaseName, final Supplier<Integer> apply) {
        rowMap.compute(dataKey, (key, value) -> buildRow(value, databaseName, apply.get()));
    }
    
    private LinkedList<Object> buildRow(final LinkedList<Object> value, final String databaseName, final int count) {
        if (null == value) {
            return new LinkedList<>(Arrays.asList(databaseName, count));
        } else {
            value.set(1, (Integer) value.get(1) + count);
            return value;
        }
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, LinkedList<Object>> entry = data.next();
        entry.getValue().addFirst(entry.getKey());
        return entry.getValue();
    }
    
    @Override
    public String getType() {
        return CountEncryptRuleStatement.class.getName();
    }
}
