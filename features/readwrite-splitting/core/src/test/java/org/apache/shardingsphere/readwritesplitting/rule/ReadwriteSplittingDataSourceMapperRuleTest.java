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

package org.apache.shardingsphere.readwritesplitting.rule;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ReadwriteSplittingDataSourceMapperRuleTest {
    
    @Test
    void assertGetDataSourceMapper() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        Map<String, Collection<String>> actual = readwriteSplittingRule.getDataSourceMapperRule().getDataSourceMapper();
        Map<String, Collection<String>> expected = Collections.singletonMap("readwrite", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"));
        assertThat(actual, is(expected));
    }
    
    private ReadwriteSplittingRule createReadwriteSplittingRule() {
        ReadwriteSplittingDataSourceRuleConfiguration config =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random");
        return new ReadwriteSplittingRule("logic_db", new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("random", new AlgorithmConfiguration("RANDOM", new Properties()))), mock(InstanceContext.class));
    }
}
