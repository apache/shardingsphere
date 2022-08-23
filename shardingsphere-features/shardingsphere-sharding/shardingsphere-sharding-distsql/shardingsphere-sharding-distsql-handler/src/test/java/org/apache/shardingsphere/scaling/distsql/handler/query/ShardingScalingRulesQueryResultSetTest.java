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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineReadConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineWriteConfiguration;
import org.apache.shardingsphere.infra.config.rule.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.migration.distsql.handler.query.ShardingScalingRulesQueryResultSet;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingAlgorithmsQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingScalingRulesQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingScalingRulesQueryResultSet resultSet = new ShardingScalingRulesQueryResultSet();
        resultSet.init(mockDatabase(), mock(ShowShardingAlgorithmsStatement.class));
        assertTrue(resultSet.next());
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
        assertThat(actual.get(4).toString(), containsString("\"type\":\"IDLE\",\"props\":{\"incremental-task-idle-seconds-threshold\":\"1800\"}"));
        assertThat(actual.get(5).toString(), containsString("\"type\":\"DATA_MATCH\",\"props\":{\"chunk-size\":\"1000\"}"));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(result.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setScaling(Collections.singletonMap("scaling_name", buildCompleteConfiguration()));
        return result;
    }
    
    private OnRuleAlteredActionConfiguration buildCompleteConfiguration() {
        PipelineReadConfiguration inputConfig = new PipelineReadConfiguration(10, 100, 10, new AlgorithmConfiguration("QPS", createProperties("qps", "50")));
        PipelineWriteConfiguration outputConfig = new PipelineWriteConfiguration(10, 100, new AlgorithmConfiguration("TPS", createProperties("tps", "2000")));
        AlgorithmConfiguration streamChannel = new AlgorithmConfiguration("MEMORY", createProperties("block-queue-size", "10000"));
        AlgorithmConfiguration completionDetector = new AlgorithmConfiguration("IDLE", createProperties("incremental-task-idle-seconds-threshold", "1800"));
        AlgorithmConfiguration dataConsistencyChecker = new AlgorithmConfiguration("DATA_MATCH", createProperties("chunk-size", "1000"));
        return new OnRuleAlteredActionConfiguration(inputConfig, outputConfig, streamChannel, completionDetector, dataConsistencyChecker);
    }
    
    private Properties createProperties(final String key, final String value) {
        Properties result = new Properties();
        result.setProperty(key, value);
        return result;
    }
    
    @Test
    public void assertGetRowDataWithoutShardingRule() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.empty());
        ShardingAlgorithmsQueryResultSet resultSet = new ShardingAlgorithmsQueryResultSet();
        resultSet.init(database, mock(ShowShardingAlgorithmsStatement.class));
        assertFalse(resultSet.next());
    }
}
