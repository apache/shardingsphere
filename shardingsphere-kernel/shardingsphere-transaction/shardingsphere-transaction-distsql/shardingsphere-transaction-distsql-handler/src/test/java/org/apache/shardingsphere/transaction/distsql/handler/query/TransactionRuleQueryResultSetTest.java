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

import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.parser.statement.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class TransactionRuleQueryResultSetTest {
    
    @Test
    public void assertExecuteWithXA() {
        TransactionRuleQueryResultSet resultSet = new TransactionRuleQueryResultSet();
        ShardingSphereRuleMetaData ruleMetaData = createGlobalRuleMetaData("XA", "Atomikos", createProperties());
        resultSet.init(ruleMetaData, mock(ShowTransactionRuleStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        Iterator<Object> rowData = actual.iterator();
        assertThat(actual.size(), is(3));
        assertThat(rowData.next(), is("XA"));
        assertThat(rowData.next(), is("Atomikos"));
        String props = (String) rowData.next();
        assertTrue(props.contains("host=127.0.0.1"));
        assertTrue(props.contains("databaseName=jbossts"));
    }
    
    @Test
    public void assertExecuteWithLocal() {
        TransactionRuleQueryResultSet resultSet = new TransactionRuleQueryResultSet();
        ShardingSphereRuleMetaData ruleMetaData = createGlobalRuleMetaData("LOCAL", null, null);
        resultSet.init(ruleMetaData, mock(ShowTransactionRuleStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(3));
        assertTrue(actual.contains("LOCAL"));
        assertTrue(actual.contains(""));
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData(final String defaultType, final String providerType, final Properties props) {
        TransactionRule rule = new TransactionRule(new TransactionRuleConfiguration(defaultType, providerType, props), Collections.emptyMap(), mock(InstanceContext.class));
        return new ShardingSphereRuleMetaData(Collections.singleton(rule));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("host", "127.0.0.1");
        result.setProperty("databaseName", "jbossts");
        return result;
    }
}
