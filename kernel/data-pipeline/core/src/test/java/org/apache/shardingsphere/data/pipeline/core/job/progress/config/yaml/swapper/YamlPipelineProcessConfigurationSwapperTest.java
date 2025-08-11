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

package org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class YamlPipelineProcessConfigurationSwapperTest {
    
    @Test
    void assertSwapToObject() {
        PipelineProcessConfiguration actual = new YamlPipelineProcessConfigurationSwapper().swapToObject(createYamlConfiguration());
        assertThat(actual.getRead().getWorkerThread(), is(20));
        assertThat(actual.getRead().getBatchSize(), is(1000));
        assertThat(actual.getRead().getShardingSize(), is(10000000));
        assertThat(actual.getRead().getRateLimiter().getType(), is("INPUT"));
        assertThat(actual.getRead().getRateLimiter().getProps().getProperty("batch-size"), is("1000"));
        assertThat(actual.getRead().getRateLimiter().getProps().getProperty("qps"), is("500"));
        assertThat(actual.getWrite().getWorkerThread(), is(20));
        assertThat(actual.getWrite().getBatchSize(), is(1000));
        assertThat(actual.getWrite().getRateLimiter().getType(), is("OUTPUT"));
        assertThat(actual.getWrite().getRateLimiter().getProps().getProperty("batch-size"), is("1000"));
        assertThat(actual.getWrite().getRateLimiter().getProps().getProperty("tps"), is("2000"));
        assertThat(actual.getStreamChannel().getType(), is("MEMORY"));
        assertThat(actual.getStreamChannel().getProps().getProperty("block-queue-size"), is("2000"));
    }
    
    private YamlPipelineProcessConfiguration createYamlConfiguration() {
        YamlPipelineReadConfiguration yamlReadConfig = new YamlPipelineReadConfiguration();
        YamlAlgorithmConfiguration yamlReadRateLimiterConfig = new YamlAlgorithmConfiguration();
        yamlReadRateLimiterConfig.setType("INPUT");
        yamlReadRateLimiterConfig.setProps(PropertiesBuilder.build(new Property("batch-size", "1000"), new Property("qps", "500")));
        yamlReadConfig.setRateLimiter(yamlReadRateLimiterConfig);
        YamlPipelineProcessConfiguration result = new YamlPipelineProcessConfiguration();
        result.setRead(yamlReadConfig);
        YamlPipelineWriteConfiguration yamlWriteConfig = new YamlPipelineWriteConfiguration();
        YamlAlgorithmConfiguration yamlWriteRateLimiterConfig = new YamlAlgorithmConfiguration();
        yamlWriteRateLimiterConfig.setType("OUTPUT");
        yamlWriteRateLimiterConfig.setProps(PropertiesBuilder.build(new Property("batch-size", "1000"), new Property("tps", "2000")));
        yamlWriteConfig.setRateLimiter(yamlWriteRateLimiterConfig);
        result.setWrite(yamlWriteConfig);
        YamlAlgorithmConfiguration streamChannelConfig = new YamlAlgorithmConfiguration();
        streamChannelConfig.setType("MEMORY");
        streamChannelConfig.setProps(PropertiesBuilder.build(new Property("block-queue-size", "2000")));
        result.setStreamChannel(streamChannelConfig);
        return result;
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        PipelineReadConfiguration readConfig = new PipelineReadConfiguration(40, 1000, 10000000,
                new AlgorithmConfiguration("INPUT", PropertiesBuilder.build(new Property("batch-size", "1000"), new Property("qps", "50"))));
        PipelineWriteConfiguration writeConfig = new PipelineWriteConfiguration(40, 1000,
                new AlgorithmConfiguration("OUTPUT", PropertiesBuilder.build(new Property("batch-size", "1000"), new Property("tps", "2000"))));
        PipelineProcessConfiguration config = new PipelineProcessConfiguration(readConfig, writeConfig,
                new AlgorithmConfiguration("MEMORY", PropertiesBuilder.build(new Property("block-queue-size", "2000"))));
        YamlPipelineProcessConfiguration actual = new YamlPipelineProcessConfigurationSwapper().swapToYamlConfiguration(config);
        assertThat(actual.getRead().getWorkerThread(), is(40));
        assertThat(actual.getRead().getBatchSize(), is(1000));
        assertThat(actual.getRead().getShardingSize(), is(10000000));
        assertThat(actual.getRead().getRateLimiter().getType(), is("INPUT"));
        assertThat(actual.getRead().getRateLimiter().getProps().getProperty("batch-size"), is("1000"));
        assertThat(actual.getRead().getRateLimiter().getProps().getProperty("qps"), is("50"));
        assertThat(actual.getWrite().getWorkerThread(), is(40));
        assertThat(actual.getWrite().getBatchSize(), is(1000));
        assertThat(actual.getWrite().getRateLimiter().getType(), is("OUTPUT"));
        assertThat(actual.getWrite().getRateLimiter().getProps().getProperty("batch-size"), is("1000"));
        assertThat(actual.getWrite().getRateLimiter().getProps().getProperty("tps"), is("2000"));
        assertThat(actual.getStreamChannel().getType(), is("MEMORY"));
        assertThat(actual.getStreamChannel().getProps().getProperty("block-queue-size"), is("2000"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithNull() {
        assertNull(new YamlPipelineProcessConfigurationSwapper().swapToYamlConfiguration(null));
    }
    
    @Test
    void assertSwapToObjectWithNull() {
        assertNull(new YamlPipelineProcessConfigurationSwapper().swapToObject(null));
    }
}
