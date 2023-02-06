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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.handler.fixture.ShardingSphereTransactionManagerFixture;
import org.apache.shardingsphere.transaction.distsql.parser.segment.TransactionProviderSegment;
import org.apache.shardingsphere.transaction.distsql.parser.statement.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class AlterTransactionRuleStatementUpdaterTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertExecuteWithXA() {
        try (MockedStatic<ShardingSphereServiceLoader> shardingSphereServiceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            shardingSphereServiceLoader.when(
                    () -> ShardingSphereServiceLoader.getServiceInstances(ShardingSphereTransactionManager.class)).thenReturn(Collections.singleton(new ShardingSphereTransactionManagerFixture()));
            AlterTransactionRuleStatementUpdater updater = new AlterTransactionRuleStatementUpdater();
            ShardingSphereMetaData metaData = createMetaData();
            updater.executeUpdate(metaData, new AlterTransactionRuleStatement("XA",
                    new TransactionProviderSegment("Atomikos", PropertiesBuilder.build(new Property("host", "127.0.0.1"), new Property("databaseName", "jbossts")))));
            TransactionRule updatedRule = metaData.getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
            assertThat(updatedRule.getDefaultType(), is(TransactionType.XA));
            assertThat(updatedRule.getProviderType(), is("Atomikos"));
            assertTrue(updatedRule.getDatabases().containsKey("foo_db"));
            assertTrue(null != updatedRule.getProps() && !updatedRule.getProps().isEmpty());
            String props = PropertiesConverter.convert(updatedRule.getProps());
            assertTrue(props.contains("host=127.0.0.1"));
            assertTrue(props.contains("databaseName=jbossts"));
        }
    }
    
    @Test
    public void assertExecuteWithLocal() {
        AlterTransactionRuleStatementUpdater updater = new AlterTransactionRuleStatementUpdater();
        ShardingSphereMetaData metaData = createMetaData();
        updater.executeUpdate(metaData, new AlterTransactionRuleStatement("LOCAL", new TransactionProviderSegment("", new Properties())));
        TransactionRule updatedRule = metaData.getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        assertThat(updatedRule.getDefaultType(), is(TransactionType.LOCAL));
        assertThat(updatedRule.getProviderType(), is(""));
        assertTrue(updatedRule.getDatabases().containsKey("foo_db"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", mockDatabase()));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(createTransactionRule(databases))));
        return new ShardingSphereMetaData(databases, ruleMetaData, new ConfigurationProperties(new Properties()));
    }
    
    private TransactionRule createTransactionRule(final Map<String, ShardingSphereDatabase> databases) {
        return new TransactionRule(new TransactionRuleConfiguration("BASE", null, new Properties()), databases);
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", mock(DataSource.class, RETURNS_DEEP_STUBS)));
        return result;
    }
}
