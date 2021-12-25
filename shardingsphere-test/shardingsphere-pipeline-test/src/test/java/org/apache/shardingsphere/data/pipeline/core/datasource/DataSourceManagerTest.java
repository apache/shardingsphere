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

package org.apache.shardingsphere.data.pipeline.core.datasource;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfigurationFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourceManagerTest {
    
    private JobConfiguration jobConfig;
    
    @Before
    public void setUp() {
        jobConfig = ResourceUtil.mockJobConfig();
    }
    
    @Test
    public void assertGetDataSource() {
        DataSourceManager dataSourceManager = new DataSourceManager();
        DataSource actual = dataSourceManager.getDataSource(
                JDBCDataSourceConfigurationFactory.newInstance(jobConfig.getRuleConfig().getSource().getType(), jobConfig.getRuleConfig().getSource().getParameter()));
        assertThat(actual, instanceOf(DataSourceWrapper.class));
    }
    
    @Test
    public void assertClose() throws NoSuchFieldException, IllegalAccessException {
        try (DataSourceManager dataSourceManager = new DataSourceManager()) {
            dataSourceManager.createSourceDataSource(
                    JDBCDataSourceConfigurationFactory.newInstance(jobConfig.getRuleConfig().getSource().getType(), jobConfig.getRuleConfig().getSource().getParameter()));
            dataSourceManager.createTargetDataSource(
                    JDBCDataSourceConfigurationFactory.newInstance(jobConfig.getRuleConfig().getTarget().getType(), jobConfig.getRuleConfig().getTarget().getParameter()));
            Map<?, ?> cachedDataSources = ReflectionUtil.getFieldValue(dataSourceManager, "cachedDataSources", Map.class);
            assertNotNull(cachedDataSources);
            assertThat(cachedDataSources.size(), is(2));
            dataSourceManager.close();
            assertTrue(cachedDataSources.isEmpty());
        }
    }
}
