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

package org.apache.shardingsphere.data.pipeline.common.job.progress.yaml;

import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.util.ConfigurationFileUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YamlInventoryIncrementalJobItemProgressSwapperTest {
    
    private static final YamlInventoryIncrementalJobItemProgressSwapper SWAPPER = new YamlInventoryIncrementalJobItemProgressSwapper();
    
    @Test
    void assertFullSwapToYamlConfiguration() {
        InventoryIncrementalJobItemProgress progress = SWAPPER.swapToObject(YamlEngine.unmarshal(ConfigurationFileUtils.readFile("job-progress.yaml"), YamlInventoryIncrementalJobItemProgress.class));
        YamlInventoryIncrementalJobItemProgress actual = SWAPPER.swapToYamlConfiguration(progress);
        assertThat(actual.getStatus(), is("RUNNING"));
        assertThat(actual.getSourceDatabaseType(), is("H2"));
        assertThat(actual.getDataSourceName(), is("ds_0"));
        assertThat(actual.getInventory().getFinished().length, is(2));
        assertThat(actual.getInventory().getFinished(), is(new String[]{"ds0.t_2", "ds0.t_1"}));
        assertThat(actual.getInventory().getUnfinished().size(), is(2));
        assertThat(actual.getInventory().getUnfinished().get("ds1.t_2"), is("i,1,2"));
        assertThat(actual.getInventory().getUnfinished().get("ds1.t_1"), is(""));
        assertThat(actual.getIncremental().getPosition().length(), is(0));
    }
    
    @Test
    void assertSwapWithFullConfig() {
        YamlInventoryIncrementalJobItemProgress yamlProgress = YamlEngine.unmarshal(ConfigurationFileUtils.readFile("job-progress.yaml"), YamlInventoryIncrementalJobItemProgress.class);
        YamlInventoryIncrementalJobItemProgress actual = SWAPPER.swapToYamlConfiguration(SWAPPER.swapToObject(yamlProgress));
        assertThat(YamlEngine.marshal(actual), is(YamlEngine.marshal(yamlProgress)));
    }
    
    @Test
    void assertSwapWithoutInventoryIncremental() {
        YamlInventoryIncrementalJobItemProgress yamlProgress = YamlEngine.unmarshal(ConfigurationFileUtils.readFile("job-progress-failure.yaml"), YamlInventoryIncrementalJobItemProgress.class);
        InventoryIncrementalJobItemProgress progress = SWAPPER.swapToObject(yamlProgress);
        assertNotNull(progress.getInventory());
        assertNotNull(progress.getIncremental());
        assertThat(progress.getDataSourceName(), is("ds_0"));
        assertThat(progress.getIncremental().getIncrementalLatestActiveTimeMillis(), is(0L));
        YamlInventoryIncrementalJobItemProgress actual = SWAPPER.swapToYamlConfiguration(progress);
        assertNotNull(actual.getInventory());
        assertNotNull(actual.getIncremental());
        assertThat(YamlEngine.marshal(actual), is(YamlEngine.marshal(yamlProgress)));
    }
    
    @Test
    void assertSwapWithRunningConfig() {
        YamlInventoryIncrementalJobItemProgress yamlProgress = YamlEngine.unmarshal(ConfigurationFileUtils.readFile("job-progress-running.yaml"), YamlInventoryIncrementalJobItemProgress.class);
        InventoryIncrementalJobItemProgress progress = SWAPPER.swapToObject(yamlProgress);
        assertNotNull(progress.getInventory());
        assertNotNull(progress.getIncremental());
        assertThat(progress.getDataSourceName(), is("ds_0"));
        assertThat(progress.getIncremental().getIncrementalLatestActiveTimeMillis(), is(0L));
        YamlInventoryIncrementalJobItemProgress actual = SWAPPER.swapToYamlConfiguration(progress);
        assertNotNull(actual.getInventory());
        assertNotNull(actual.getIncremental());
        assertThat(YamlEngine.marshal(actual), is(YamlEngine.marshal(yamlProgress)));
    }
}
