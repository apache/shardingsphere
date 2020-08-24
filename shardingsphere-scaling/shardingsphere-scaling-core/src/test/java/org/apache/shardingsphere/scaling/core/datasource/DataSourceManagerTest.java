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

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.utils.SyncConfigurationUtil;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DataSourceManagerTest {
    
    private static final Gson GSON = new Gson();
    
    private List<SyncConfiguration> syncConfigurations;
    
    @Before
    public void setUp() {
        initConfig("/config.json");
    }
    
    @Test
    public void assertCreateWithConfiguration() throws NoSuchFieldException, IllegalAccessException {
        DataSourceManager dataSourceManager = new DataSourceManager(syncConfigurations);
        Map cachedDataSources = ReflectionUtil.getFieldValueFromClass(dataSourceManager, "cachedDataSources", Map.class);
        assertNotNull(cachedDataSources);
        assertThat(cachedDataSources.size(), is(2));
    }
    
    @Test
    public void assertGetDataSource() {
        DataSourceManager dataSourceManager = new DataSourceManager();
        DataSource actual = dataSourceManager.getDataSource(syncConfigurations.get(0).getDumperConfiguration().getDataSourceConfiguration());
        assertThat(actual, instanceOf(HikariDataSource.class));
    }
    
    @Test
    public void assertClose() throws NoSuchFieldException, IllegalAccessException {
        DataSourceManager dataSourceManager = new DataSourceManager(syncConfigurations);
        dataSourceManager.close();
        Map cachedDataSources = ReflectionUtil.getFieldValueFromClass(dataSourceManager, "cachedDataSources", Map.class);
        assertNotNull(cachedDataSources);
        assertThat(cachedDataSources.size(), is(0));
    }
    
    private void initConfig(final String configFile) {
        InputStream fileInputStream = DataSourceManagerTest.class.getResourceAsStream(configFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        ScalingConfiguration scalingConfiguration = GSON.fromJson(inputStreamReader, ScalingConfiguration.class);
        syncConfigurations = (List<SyncConfiguration>) SyncConfigurationUtil.toSyncConfigurations(scalingConfiguration);
    }
}
