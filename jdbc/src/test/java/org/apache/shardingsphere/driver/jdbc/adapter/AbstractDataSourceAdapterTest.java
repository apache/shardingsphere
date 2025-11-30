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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;

class AbstractDataSourceAdapterTest {
    
    private ShardingSphereDataSource shardingSphereDataSource;
    
    @BeforeEach
    void setUp() throws SQLException {
        shardingSphereDataSource = new ShardingSphereDataSource("foo_db", null, Collections.singletonMap("ds", new MockedDataSource()), getRuleConfigurations(), new Properties());
    }
    
    private Collection<RuleConfiguration> getRuleConfigurations() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("table", "ds" + "." + "table");
        shardingRuleConfig.setTables(Collections.singletonList(shardingTableRuleConfig));
        return Collections.singleton(shardingRuleConfig);
    }
    
    @Test
    void assertSetLogWriter() {
        assertThat(shardingSphereDataSource.getLogWriter(), isA(PrintWriter.class));
        shardingSphereDataSource.setLogWriter(null);
        assertNull(shardingSphereDataSource.getLogWriter());
    }
    
    @Test
    void assertGetParentLogger() {
        assertThat(shardingSphereDataSource.getParentLogger().getName(), is(Logger.GLOBAL_LOGGER_NAME));
    }
}
