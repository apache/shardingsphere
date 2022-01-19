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

package org.apache.shardingsphere.scaling.distsql.handler.query;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingScalingRulesQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        ShardingScalingRulesQueryResultSet resultSet = new ShardingScalingRulesQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingAlgorithmsStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(6));
        assertThat(actual.get(0), is("scaling_name"));
        assertThat(actual.get(1).toString(), containsString("\"workerThread\":10"));
        assertThat(actual.get(1).toString(), containsString("\"batchSize\":100"));
        assertThat(actual.get(1).toString(), containsString("\"rateLimiter\":{\"type\":\"QPS\",\"props\":{\"qps\":\"50\"}}"));
        assertThat(actual.get(2).toString(), containsString("\"workerThread\":10"));
        assertThat(actual.get(2).toString(), containsString("\"batchSize\":100"));
        assertThat(actual.get(2).toString(), containsString("\"rateLimiter\":{\"type\":\"TPS\",\"props\":{\"tps\":\"2000\"}}"));
        assertThat(actual.get(3).toString(), containsString("\"type\":\"MEMORY\",\"props\":{\"block-queue-size\":\"10000\"}"));
        assertThat(actual.get(4).toString(), containsString("\"type\":\"IDLE\",\"props\":{\"incremental-task-idle-minute-threshold\":\"30\"}"));
        assertThat(actual.get(5).toString(), containsString("\"type\":\"DATA_MATCH\",\"props\":{\"chunk-size\":\"1000\"}"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        Map<String, OnRuleAlteredActionConfiguration> scalingRuleConfigurationMap = new HashMap<>(1, 1);
        scalingRuleConfigurationMap.put("scaling_name", buildCompleteConfiguration());
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setScaling(scalingRuleConfigurationMap);
        return result;
    }
    
    private OnRuleAlteredActionConfiguration buildCompleteConfiguration() {
        InputConfiguration inputConfiguration = createInputConfiguration("QPS", newProperties("qps", "50"));
        OutputConfiguration outputConfiguration = createOutputConfiguration("TPS", newProperties("tps", "2000"));
        ShardingSphereAlgorithmConfiguration streamChannel = createAlgorithm("MEMORY", newProperties("block-queue-size", "10000"));
        ShardingSphereAlgorithmConfiguration completionDetector = createAlgorithm("IDLE", newProperties("incremental-task-idle-minute-threshold", "30"));
        ShardingSphereAlgorithmConfiguration dataConsistencyChecker = createAlgorithm("DATA_MATCH", newProperties("chunk-size", "1000"));
        return new OnRuleAlteredActionConfiguration(inputConfiguration, outputConfiguration, streamChannel, completionDetector, dataConsistencyChecker);
    }
    
    private InputConfiguration createInputConfiguration(final String type, final Properties props) {
        return new InputConfiguration(10, 100, createAlgorithm(type, props));
    }
    
    private OutputConfiguration createOutputConfiguration(final String type, final Properties props) {
        return new OutputConfiguration(10, 100, createAlgorithm(type, props));
    }
    
    private ShardingSphereAlgorithmConfiguration createAlgorithm(final String type, final Properties props) {
        return new ShardingSphereAlgorithmConfiguration(type, props);
    }
    
    private Properties newProperties(final String key, final String value) {
        Properties result = new Properties();
        result.setProperty(key, value);
        return result;
    }
}
