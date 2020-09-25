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

package org.apache.shardingsphere.scaling.core.util;

import com.google.gson.Gson;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.utils.SyncConfigurationUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SyncConfigurationUtilTest {
    
    private static final Gson GSON = new Gson();
    
    private ScalingConfiguration scalingConfiguration;
    
    @Before
    public void setUp() {
        initConfig("/config.json");
    }
    
    @Test
    public void assertFilterByShardingDataSourceTables() {
        List<SyncConfiguration> syncConfigurations = (List<SyncConfiguration>) SyncConfigurationUtil.toSyncConfigurations(scalingConfiguration);
        assertThat(syncConfigurations.get(0).getDumperConfiguration().getTableNameMap().size(), is(1));
    }
    
    private void initConfig(final String configFile) {
        InputStream fileInputStream = SyncConfigurationUtilTest.class.getResourceAsStream(configFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        scalingConfiguration = GSON.fromJson(inputStreamReader, ScalingConfiguration.class);
    }
}
