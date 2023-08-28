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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.parser.statement.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShowTransactionRuleExecutorTest {
    
    @Test
    void assertExecuteWithXA() {
        ShowTransactionRuleExecutor executor = new ShowTransactionRuleExecutor();
        RuleMetaData ruleMetaData = mockGlobalRuleMetaData(TransactionType.XA.name(), "Atomikos",
                PropertiesBuilder.build(new Property("host", "127.0.0.1"), new Property("databaseName", "jbossts")));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new LinkedHashMap<>(), mock(ResourceMetaData.class), ruleMetaData, new ConfigurationProperties(new Properties()));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, mock(ShowTransactionRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is(TransactionType.XA.name()));
        assertThat(row.getCell(2), is("Atomikos"));
        String props = (String) row.getCell(3);
        assertTrue(props.contains("databaseName=jbossts"));
        assertTrue(props.contains("host=127.0.0.1"));
    }
    
    @Test
    void assertExecuteWithLocal() {
        ShowTransactionRuleExecutor executor = new ShowTransactionRuleExecutor();
        RuleMetaData ruleMetaData = mockGlobalRuleMetaData(TransactionType.LOCAL.name(), null, new Properties());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new LinkedHashMap<>(), mock(ResourceMetaData.class), ruleMetaData, new ConfigurationProperties(new Properties()));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, mock(ShowTransactionRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is(TransactionType.LOCAL.name()));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
    }
    
    @Test
    void assertGetColumnNames() {
        ShowTransactionRuleExecutor executor = new ShowTransactionRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("default_type"));
        assertThat(iterator.next(), is("provider_type"));
        assertThat(iterator.next(), is("props"));
    }
    
    private RuleMetaData mockGlobalRuleMetaData(final String defaultType, final String providerType, final Properties props) {
        TransactionRule transactionRule = new TransactionRule(createTransactionRuleConfiguration(defaultType, providerType, props), Collections.emptyMap());
        return new RuleMetaData(Collections.singleton(transactionRule));
    }
    
    private TransactionRuleConfiguration createTransactionRuleConfiguration(final String defaultType, final String providerType, final Properties props) {
        return new TransactionRuleConfiguration(defaultType, providerType, props);
    }
}
