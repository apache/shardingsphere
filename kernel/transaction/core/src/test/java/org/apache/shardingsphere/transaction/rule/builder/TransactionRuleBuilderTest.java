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

package org.apache.shardingsphere.transaction.rule.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class TransactionRuleBuilderTest {
    
    @Test
    void assertBuild() {
        TransactionRuleConfiguration ruleConfig = new TransactionRuleConfiguration("LOCAL", "provider", new Properties());
        ShardingSphereDatabase database = new ShardingSphereDatabase("logic_db", null, new ShardingSphereResourceMetaData("db", createDataSourceMap()),
                new ShardingSphereRuleMetaData(Collections.singletonList(mock(ShardingSphereRule.class))), Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
        TransactionRule rule = new TransactionRuleBuilder().build(ruleConfig, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ConfigurationProperties.class));
        assertNotNull(rule.getConfiguration());
        assertThat(rule.getDatabases().get("logic_db").getResourceMetaData().getDataSources().size(), is(2));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        result.put("not_change", new MockedDataSource());
        result.put("replace", new MockedDataSource());
        return result;
    }
}
