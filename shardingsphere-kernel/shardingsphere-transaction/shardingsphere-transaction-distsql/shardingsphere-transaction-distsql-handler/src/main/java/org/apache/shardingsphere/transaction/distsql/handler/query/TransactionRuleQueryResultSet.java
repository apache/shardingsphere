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

package org.apache.shardingsphere.transaction.distsql.handler.query;

import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.transaction.distsql.parser.statement.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Query result set for transaction rule.
 */
public final class TransactionRuleQueryResultSet implements GlobalRuleDistSQLResultSet {
    
    private static final String DEFAULT_TYPE = "default_type";
    
    private static final String PROVIDER_TYPE = "provider_type";
    
    private static final String PROPS = "props";
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatement sqlStatement) {
        ruleMetaData.findSingleRule(TransactionRule.class).ifPresent(optional -> data = buildData(optional).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final TransactionRule rule) {
        return Collections.singleton(Arrays.asList(
                rule.getDefaultType().name(), null != rule.getProviderType() ? rule.getProviderType() : "", null != rule.getProps() ? PropertiesConverter.convert(rule.getProps()) : ""));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(DEFAULT_TYPE, PROVIDER_TYPE, PROPS);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowTransactionRuleStatement.class.getName();
    }
}
