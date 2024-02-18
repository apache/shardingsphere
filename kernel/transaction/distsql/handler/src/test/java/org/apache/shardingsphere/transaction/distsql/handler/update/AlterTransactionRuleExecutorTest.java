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

package org.apache.shardingsphere.transaction.distsql.handler.update;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.handler.fixture.ShardingSphereTransactionManagerFixture;
import org.apache.shardingsphere.transaction.distsql.segment.TransactionProviderSegment;
import org.apache.shardingsphere.transaction.distsql.statement.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
class AlterTransactionRuleExecutorTest {
    
    @Test
    void assertExecuteWithXA() {
        when(ShardingSphereServiceLoader.getServiceInstances(ShardingSphereTransactionManager.class)).thenReturn(Collections.singleton(new ShardingSphereTransactionManagerFixture()));
        AlterTransactionRuleExecutor executor = new AlterTransactionRuleExecutor();
        AlterTransactionRuleStatement sqlStatement = new AlterTransactionRuleStatement(
                "XA", new TransactionProviderSegment("Atomikos", PropertiesBuilder.build(new Property("host", "127.0.0.1"), new Property("databaseName", "jbossts"))));
        TransactionRule rule = mock(TransactionRule.class);
        executor.setRule(rule);
        TransactionRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(actual.getDefaultType(), is("XA"));
        assertThat(actual.getProviderType(), is("Atomikos"));
        assertThat(actual.getProps().size(), is(2));
        assertThat(actual.getProps().getProperty("host"), is("127.0.0.1"));
        assertThat(actual.getProps().getProperty("databaseName"), is("jbossts"));
    }
    
    @Test
    void assertExecuteWithLocal() {
        AlterTransactionRuleExecutor executor = new AlterTransactionRuleExecutor();
        TransactionRule rule = mock(TransactionRule.class);
        executor.setRule(rule);
        TransactionRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(new AlterTransactionRuleStatement("LOCAL", new TransactionProviderSegment("", new Properties())));
        assertThat(actual.getDefaultType(), is("LOCAL"));
        assertThat(actual.getProviderType(), is(""));
    }
}
