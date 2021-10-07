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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AbstractDataSourceAdapterTest {
    
    private ShardingSphereDataSource shardingSphereDataSource;
    
    @Before
    public void setUp() throws SQLException {
        shardingSphereDataSource = new ShardingSphereDataSource(DefaultSchema.LOGIC_NAME, null, getDataSource(), getRuleConfigurations(), new Properties());
    }
    
    private Map<String, DataSource> getDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class, RETURNS_DEEP_STUBS));
        when(dataSource.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        return Collections.singletonMap("ds", dataSource);
    }
    
    private Collection<RuleConfiguration> getRuleConfigurations() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("table", "ds" + "." + "table");
        shardingRuleConfig.setTables(Collections.singletonList(shardingTableRuleConfig));
        return Collections.singleton(shardingRuleConfig);
    }
    
    @Test
    public void assertSetLogWriter() {
        assertThat(shardingSphereDataSource.getLogWriter(), instanceOf(PrintWriter.class));
        shardingSphereDataSource.setLogWriter(null);
        assertNull(shardingSphereDataSource.getLogWriter());
    }
    
    @Test
    public void assertGetParentLogger() {
        assertThat(shardingSphereDataSource.getParentLogger().getName(), is(Logger.GLOBAL_LOGGER_NAME));
    }
}
