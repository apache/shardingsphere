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

package org.apache.shardingsphere.scaling.core.common.datasource;

import org.apache.shardingsphere.cdc.core.datasource.DataSourceManager;
import org.apache.shardingsphere.cdc.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.cdc.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
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
        DataSource actual = dataSourceManager.getDataSource(jobConfig.getRuleConfig().getSource().unwrap());
        assertThat(actual, instanceOf(DataSourceWrapper.class));
    }
    
    @Test
    public void assertClose() throws NoSuchFieldException, IllegalAccessException {
        try (DataSourceManager dataSourceManager = new DataSourceManager()) {
            dataSourceManager.createSourceDataSource(jobConfig.getRuleConfig().getSource().unwrap());
            dataSourceManager.createTargetDataSource(jobConfig.getRuleConfig().getTarget().unwrap());
            Map<?, ?> cachedDataSources = ReflectionUtil.getFieldValue(dataSourceManager, "cachedDataSources", Map.class);
            assertNotNull(cachedDataSources);
            assertThat(cachedDataSources.size(), is(2));
            dataSourceManager.close();
            assertTrue(cachedDataSources.isEmpty());
        }
    }
}
