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

package org.apache.shardingsphere.data.pipeline.core.metadata;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class PipelineProcessConfigurationPersistServiceTest {
    
    private final Map<String, BiConsumer<YamlPipelineProcessConfiguration, YamlPipelineProcessConfiguration>> typesAssertionMap = ImmutableMap.of(
            "READ", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), expected.getRead());
                assertYamlConfiguration(actual.getWrite(), new YamlPipelineWriteConfiguration());
                assertNull(actual.getStreamChannel());
            },
            "WRITE", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), new YamlPipelineReadConfiguration());
                assertYamlConfiguration(actual.getWrite(), expected.getWrite());
                assertNull(actual.getStreamChannel());
            },
            "STREAM_CHANNEL", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), new YamlPipelineReadConfiguration());
                assertYamlConfiguration(actual.getWrite(), new YamlPipelineWriteConfiguration());
                assertYamlConfiguration(actual.getStreamChannel(), expected.getStreamChannel());
            },
            "READ-WRITE", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), expected.getRead());
                assertYamlConfiguration(actual.getWrite(), expected.getWrite());
                assertNull(actual.getStreamChannel());
            },
            "READ-STREAM_CHANNEL", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), expected.getRead());
                assertYamlConfiguration(actual.getWrite(), new YamlPipelineWriteConfiguration());
                assertYamlConfiguration(actual.getStreamChannel(), expected.getStreamChannel());
            },
            "WRITE-STREAM_CHANNEL", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), new YamlPipelineReadConfiguration());
                assertYamlConfiguration(actual.getWrite(), expected.getWrite());
                assertYamlConfiguration(actual.getStreamChannel(), expected.getStreamChannel());
            },
            "READ-WRITE-STREAM_CHANNEL", (actual, expected) -> {
                assertYamlConfiguration(actual.getRead(), expected.getRead());
                assertYamlConfiguration(actual.getWrite(), expected.getWrite());
                assertYamlConfiguration(actual.getStreamChannel(), expected.getStreamChannel());
            });
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"MIGRATION", "STREAMING"})
    void assertPersistAndLoad(final String jobType) {
        for (String each : typesAssertionMap.keySet()) {
            YamlPipelineProcessConfiguration expected = createYamlPipelineProcessConfiguration(each);
            YamlPipelineProcessConfiguration actual = persistAndLoad(jobType, expected);
            typesAssertionMap.get(each).accept(actual, expected);
        }
    }
    
    private YamlPipelineProcessConfiguration createYamlPipelineProcessConfiguration(final String types) {
        YamlPipelineProcessConfiguration result = new YamlPipelineProcessConfiguration();
        // Ref AlterTransmissionRuleExecutor
        result.setRead(null);
        result.setWrite(null);
        result.setStreamChannel(null);
        for (String each : types.split("-")) {
            switch (each.trim()) {
                case "READ":
                    result.setRead(createYamlPipelineReadConfiguration());
                    break;
                case "WRITE":
                    result.setWrite(createYamlPipelineWriteConfiguration());
                    break;
                case "STREAM_CHANNEL":
                    result.setStreamChannel(createYamlStreamChannelConfiguration());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type: " + each);
            }
        }
        return result;
    }
    
    private YamlPipelineReadConfiguration createYamlPipelineReadConfiguration() {
        YamlPipelineReadConfiguration result = new YamlPipelineReadConfiguration();
        result.setWorkerThread(10);
        result.setBatchSize(1000);
        result.setShardingSize(10000000);
        YamlAlgorithmConfiguration rateLimiter = new YamlAlgorithmConfiguration();
        result.setRateLimiter(rateLimiter);
        rateLimiter.setType("QPS");
        Properties props = new Properties();
        rateLimiter.setProps(props);
        props.setProperty("qps", "500");
        return result;
    }
    
    private YamlPipelineWriteConfiguration createYamlPipelineWriteConfiguration() {
        YamlPipelineWriteConfiguration result = new YamlPipelineWriteConfiguration();
        result.setWorkerThread(10);
        result.setBatchSize(1000);
        YamlAlgorithmConfiguration rateLimiter = new YamlAlgorithmConfiguration();
        result.setRateLimiter(rateLimiter);
        rateLimiter.setType("TPS");
        Properties props = new Properties();
        rateLimiter.setProps(props);
        props.setProperty("tps", "2000");
        return result;
    }
    
    private YamlAlgorithmConfiguration createYamlStreamChannelConfiguration() {
        YamlAlgorithmConfiguration result = new YamlAlgorithmConfiguration();
        result.setType("MEMORY");
        Properties props = new Properties();
        result.setProps(props);
        props.setProperty("block-queue-size", "1000");
        return result;
    }
    
    YamlPipelineProcessConfiguration persistAndLoad(final String jobType, final YamlPipelineProcessConfiguration yamlProcessConfig) {
        PipelineProcessConfigurationPersistService persistService = new PipelineProcessConfigurationPersistService();
        YamlPipelineProcessConfigurationSwapper swapper = new YamlPipelineProcessConfigurationSwapper();
        persistService.persist(PipelineContextUtils.getContextKey(), jobType, swapper.swapToObject(yamlProcessConfig));
        return swapper.swapToYamlConfiguration(persistService.load(PipelineContextUtils.getContextKey(), jobType));
    }
    
    private void assertYamlConfiguration(final YamlConfiguration actual, final YamlConfiguration expected) {
        assertThat(YamlEngine.marshal(actual), is(YamlEngine.marshal(expected)));
    }
}
