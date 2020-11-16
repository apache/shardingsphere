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

package org.apache.shardingsphere.scaling.core.datasource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DataSourceManagerTest {
    
    private List<SyncConfiguration> syncConfigurations;
    
    @Before
    @SneakyThrows(IOException.class)
    public void setUp() {
        syncConfigurations = ScalingConfigurationUtil.initJob("/config.json").getSyncConfigurations();
    }
    
    @Test
    public void assertGetDataSource() {
        DataSourceManager dataSourceManager = new DataSourceManager();
        DataSource actual = dataSourceManager.getDataSource(syncConfigurations.get(0).getDumperConfiguration().getDataSourceConfiguration());
        assertThat(actual, instanceOf(DataSourceWrapper.class));
    }
    
    @Test
    public void assertClose() throws NoSuchFieldException, IllegalAccessException {
        DataSourceManager dataSourceManager = new DataSourceManager(syncConfigurations);
        Map<?, ?> cachedDataSources = ReflectionUtil.getFieldValueFromClass(dataSourceManager, "cachedDataSources", Map.class);
        assertNotNull(cachedDataSources);
        assertThat(cachedDataSources.size(), is(2));
        dataSourceManager.close();
        assertThat(cachedDataSources.size(), is(0));
    }
}
