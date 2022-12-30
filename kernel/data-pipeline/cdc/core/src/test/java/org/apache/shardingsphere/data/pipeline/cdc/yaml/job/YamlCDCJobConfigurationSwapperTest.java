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

package org.apache.shardingsphere.data.pipeline.cdc.yaml.job;

import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlCDCJobConfigurationSwapperTest {
    
    @Test
    public void assertSwapToObject() {
        YamlCDCJobConfiguration yamlJobConfig = new YamlCDCJobConfiguration();
        yamlJobConfig.setJobId("j51017f973ac82cb1edea4f5238a258c25e89");
        yamlJobConfig.setDatabase("test_db");
        yamlJobConfig.setTableNames(Arrays.asList("t_order", "t_order_item"));
        yamlJobConfig.setSubscriptionName("test_name");
        yamlJobConfig.setSubscriptionMode("FULL");
        CDCJobConfiguration actual = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        assertThat(actual.getJobId(), is("j51017f973ac82cb1edea4f5238a258c25e89"));
        assertThat(actual.getDatabase(), is("test_db"));
        assertThat(actual.getTableNames(), is(Arrays.asList("t_order", "t_order_item")));
        assertThat(actual.getSubscriptionName(), is("test_name"));
        assertThat(actual.getSubscriptionMode(), is("FULL"));
    }
}
