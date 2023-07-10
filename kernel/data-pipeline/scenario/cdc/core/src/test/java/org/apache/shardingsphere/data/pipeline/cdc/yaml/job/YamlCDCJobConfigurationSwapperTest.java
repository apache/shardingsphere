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
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.config.YamlCDCJobConfiguration.YamlSinkConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlCDCJobConfigurationSwapperTest {
    
    @Test
    void assertSwapToObject() {
        YamlCDCJobConfiguration yamlJobConfig = new YamlCDCJobConfiguration();
        yamlJobConfig.setJobId("j51017f973ac82cb1edea4f5238a258c25e89");
        yamlJobConfig.setDatabaseName("test_db");
        yamlJobConfig.setSchemaTableNames(Arrays.asList("test.t_order", "t_order_item"));
        yamlJobConfig.setFull(true);
        yamlJobConfig.setSourceDatabaseType("MySQL");
        YamlSinkConfiguration sinkConfig = new YamlSinkConfiguration();
        sinkConfig.setSinkType(CDCSinkType.SOCKET.name());
        yamlJobConfig.setSinkConfig(sinkConfig);
        CDCJobConfiguration actual = new YamlCDCJobConfigurationSwapper().swapToObject(yamlJobConfig);
        assertThat(actual.getJobId(), is("j51017f973ac82cb1edea4f5238a258c25e89"));
        assertThat(actual.getDatabaseName(), is("test_db"));
        assertThat(actual.getSchemaTableNames(), is(Arrays.asList("test.t_order", "t_order_item")));
        assertTrue(actual.isFull());
    }
}
