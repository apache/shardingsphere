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

package org.apache.shardingsphere.data.pipeline.core.job.progress.config;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineProcessConfigurationUtilsTest {
    
    @Test
    void assertFillInDefaultValueWithNullConfiguration() {
        PipelineProcessConfiguration actual = PipelineProcessConfigurationUtils.fillInDefaultValue(null);
        assertThat(actual.getRead().getWorkerThread(), is(20));
        assertThat(actual.getRead().getBatchSize(), is(1000));
        assertThat(actual.getRead().getShardingSize(), is(10000000));
        assertNull(actual.getRead().getRateLimiter());
        assertThat(actual.getWrite().getWorkerThread(), is(20));
        assertThat(actual.getWrite().getBatchSize(), is(1000));
        assertNull(actual.getWrite().getRateLimiter());
        assertThat(actual.getStreamChannel().getType(), is("MEMORY"));
        assertThat(actual.getStreamChannel().getProps(), is(PropertiesBuilder.build(new Property("block-queue-size", "2000"))));
    }
    
    @ParameterizedTest(name = "Missing {0}")
    @MethodSource("getPartialConfigurations")
    void assertFillInDefaultValueWithMissingSection(final String section, final PipelineProcessConfiguration originalConfig, final PipelineProcessConfiguration expected) {
        PipelineProcessConfiguration actual = PipelineProcessConfigurationUtils.fillInDefaultValue(originalConfig);
        assertThat(actual.getRead().getWorkerThread(), is(expected.getRead().getWorkerThread()));
        assertThat(actual.getRead().getBatchSize(), is(expected.getRead().getBatchSize()));
        assertThat(actual.getRead().getShardingSize(), is(expected.getRead().getShardingSize()));
        assertNull(actual.getRead().getRateLimiter());
        assertThat(actual.getWrite().getWorkerThread(), is(expected.getWrite().getWorkerThread()));
        assertThat(actual.getWrite().getBatchSize(), is(expected.getWrite().getBatchSize()));
        assertNull(actual.getWrite().getRateLimiter());
        assertThat(actual.getStreamChannel(), is(expected.getStreamChannel()));
    }
    
    private static Stream<Arguments> getPartialConfigurations() {
        PipelineReadConfiguration readConfig = new PipelineReadConfiguration(40, 500, 6000000, null);
        PipelineWriteConfiguration writeConfig = new PipelineWriteConfiguration(10, 250, null);
        AlgorithmConfiguration streamChannel = new AlgorithmConfiguration("CUSTOM", PropertiesBuilder.build(new Property("foo_key", "foo_value")));
        PipelineReadConfiguration defaultReadConfig = new PipelineReadConfiguration(20, 1000, 10000000, null);
        PipelineWriteConfiguration defaultWriteConfig = new PipelineWriteConfiguration(20, 1000, null);
        AlgorithmConfiguration expectedStreamChannel = new AlgorithmConfiguration("CUSTOM", PropertiesBuilder.build(new Property("foo_key", "foo_value")));
        AlgorithmConfiguration defaultStreamChannel = new AlgorithmConfiguration("MEMORY", PropertiesBuilder.build(new Property("block-queue-size", "2000")));
        return Stream.of(
                Arguments.of("read", new PipelineProcessConfiguration(null, writeConfig, streamChannel), new PipelineProcessConfiguration(defaultReadConfig, writeConfig, expectedStreamChannel)),
                Arguments.of("write", new PipelineProcessConfiguration(readConfig, null, streamChannel), new PipelineProcessConfiguration(readConfig, defaultWriteConfig, expectedStreamChannel)),
                Arguments.of("streamChannel", new PipelineProcessConfiguration(readConfig, writeConfig, null), new PipelineProcessConfiguration(readConfig, writeConfig, defaultStreamChannel)));
    }
    
    @Test
    void assertFillInDefaultValueWithCustomConfiguration() {
        PipelineReadConfiguration readConfig = new PipelineReadConfiguration(40, 500, 6000000, new AlgorithmConfiguration("INPUT", PropertiesBuilder.build(new Property("qps", "50"))));
        PipelineWriteConfiguration writeConfig = new PipelineWriteConfiguration(10, 250, new AlgorithmConfiguration("OUTPUT", PropertiesBuilder.build(new Property("tps", "75"))));
        AlgorithmConfiguration streamChannel = new AlgorithmConfiguration("CUSTOM", PropertiesBuilder.build(new Property("topic", "foo_topic")));
        PipelineProcessConfiguration originalConfig = new PipelineProcessConfiguration(readConfig, writeConfig, streamChannel);
        PipelineProcessConfiguration actual = PipelineProcessConfigurationUtils.fillInDefaultValue(originalConfig);
        assertThat(actual.getRead().getWorkerThread(), is(40));
        assertThat(actual.getRead().getBatchSize(), is(500));
        assertThat(actual.getRead().getShardingSize(), is(6000000));
        assertThat(actual.getRead().getRateLimiter().getType(), is("INPUT"));
        assertThat(actual.getRead().getRateLimiter().getProps(), is(PropertiesBuilder.build(new Property("qps", "50"))));
        assertThat(actual.getWrite().getWorkerThread(), is(10));
        assertThat(actual.getWrite().getBatchSize(), is(250));
        assertThat(actual.getWrite().getRateLimiter().getType(), is("OUTPUT"));
        assertThat(actual.getWrite().getRateLimiter().getProps(), is(PropertiesBuilder.build(new Property("tps", "75"))));
        assertThat(actual.getStreamChannel().getType(), is("CUSTOM"));
        assertThat(actual.getStreamChannel().getProps(), is(PropertiesBuilder.build(new Property("topic", "foo_topic"))));
        assertThat(originalConfig.getRead().getRateLimiter().getProps().getProperty("qps"), is("50"));
        assertThat(originalConfig.getWrite().getRateLimiter().getProps().getProperty("tps"), is("75"));
        assertThat(originalConfig.getStreamChannel().getProps().getProperty("topic"), is("foo_topic"));
    }
    
    @Test
    void assertFillInDefaultValueWithZeroValues() {
        PipelineProcessConfiguration originalConfig = new PipelineProcessConfiguration(new PipelineReadConfiguration(0, 0, 0, null), new PipelineWriteConfiguration(0, 0, null),
                new AlgorithmConfiguration("MEMORY", PropertiesBuilder.build()));
        PipelineProcessConfiguration actual = PipelineProcessConfigurationUtils.fillInDefaultValue(originalConfig);
        assertThat(actual.getRead().getWorkerThread(), is(0));
        assertThat(actual.getRead().getBatchSize(), is(0));
        assertThat(actual.getRead().getShardingSize(), is(0));
        assertThat(actual.getWrite().getWorkerThread(), is(0));
        assertThat(actual.getWrite().getBatchSize(), is(0));
        assertTrue(actual.getStreamChannel().getProps().isEmpty());
    }
    
    @Test
    void assertFillInDefaultValueWithUnchangedInput() {
        PipelineProcessConfiguration originalConfig = new PipelineProcessConfiguration(null, null, null);
        PipelineProcessConfiguration actual = PipelineProcessConfigurationUtils.fillInDefaultValue(originalConfig);
        assertNotNull(actual.getRead());
        assertNotNull(actual.getWrite());
        assertNotNull(actual.getStreamChannel());
        assertNull(originalConfig.getRead());
        assertNull(originalConfig.getWrite());
        assertNull(originalConfig.getStreamChannel());
    }
    
    @Test
    void assertFillInDefaultValueWithIndependentDefaults() {
        PipelineProcessConfiguration actualFirst = PipelineProcessConfigurationUtils.fillInDefaultValue(null);
        PipelineProcessConfiguration actualSecond = PipelineProcessConfigurationUtils.fillInDefaultValue(null);
        actualFirst.getStreamChannel().getProps().setProperty("block-queue-size", "10");
        assertThat(actualSecond.getStreamChannel().getProps().getProperty("block-queue-size"), is("2000"));
    }
}
