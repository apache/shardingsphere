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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MemoryPipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineProcessConfigurationPersistServiceTest {
    
    private static final YamlPipelineProcessConfigurationSwapper PROCESS_CONFIG_SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertLoadAndPersist() {
        YamlPipelineProcessConfiguration yamlProcessConfig = new YamlPipelineProcessConfiguration();
        YamlPipelineReadConfiguration yamlReadConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlReadConfig.fillInNullFieldsWithDefaultValue();
        yamlReadConfig.setShardingSize(10);
        yamlProcessConfig.setRead(yamlReadConfig);
        YamlPipelineWriteConfiguration yamlWriteConfig = YamlPipelineWriteConfiguration.buildWithDefaultValue();
        yamlProcessConfig.setWrite(yamlWriteConfig);
        YamlAlgorithmConfiguration yamlStreamChannel = new YamlAlgorithmConfiguration(MemoryPipelineChannelCreator.TYPE, new Properties());
        yamlProcessConfig.setStreamChannel(yamlStreamChannel);
        String expectedYamlText = YamlEngine.marshal(yamlProcessConfig);
        PipelineProcessConfiguration processConfig = PROCESS_CONFIG_SWAPPER.swapToObject(yamlProcessConfig);
        PipelineProcessConfigurationPersistService persistService = new PipelineProcessConfigurationPersistService();
        JobType jobType = JobType.MIGRATION;
        persistService.persist(jobType, processConfig);
        String actualYamlText = YamlEngine.marshal(PROCESS_CONFIG_SWAPPER.swapToYamlConfiguration(persistService.load(jobType)));
        assertThat(actualYamlText, is(expectedYamlText));
    }
}
