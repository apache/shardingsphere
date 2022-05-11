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

package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml;

import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.util.ConfigurationFileUtil;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public final class JobProgressYamlSwapperTest {
    
    private static final JobProgressYamlSwapper JOB_PROGRESS_YAML_SWAPPER = new JobProgressYamlSwapper();
    
    private JobProgress getJobProgress(final String data) {
        return JOB_PROGRESS_YAML_SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlJobProgress.class));
    }
    
    @Test
    public void assertFullSwapToYaml() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml"));
        YamlJobProgress actual = JOB_PROGRESS_YAML_SWAPPER.swapToYaml(jobProgress);
        assertThat(actual.getStatus(), is("RUNNING"));
        assertThat(actual.getSourceDatabaseType(), is("H2"));
        assertThat(actual.getInventory().getFinished().length, is(2));
        assertArrayEquals(actual.getInventory().getFinished(), new String[]{"ds0.t_2", "ds0.t_1"});
        assertThat(actual.getInventory().getUnfinished().size(), is(2));
        assertThat(actual.getInventory().getUnfinished().get("ds1.t_2"), is("i,1,2"));
        assertThat(actual.getInventory().getUnfinished().get("ds1.t_1"), is(""));
        assertThat(actual.getIncremental().size(), is(1));
        assertTrue(actual.getIncremental().containsKey("ds0"));
        assertNull(actual.getIncremental().get("position"));
    }
    
    @Test
    public void assertNullIncremental() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress-no-finished.yaml"));
        YamlJobProgress actual = JOB_PROGRESS_YAML_SWAPPER.swapToYaml(jobProgress);
        assertTrue(actual.getIncremental().isEmpty());
    }
    
    @Test
    public void assertNullInventory() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress-no-inventory.yaml"));
        YamlJobProgress actual = JOB_PROGRESS_YAML_SWAPPER.swapToYaml(jobProgress);
        assertThat(actual.getInventory().getFinished().length, is(0));
    }
}
