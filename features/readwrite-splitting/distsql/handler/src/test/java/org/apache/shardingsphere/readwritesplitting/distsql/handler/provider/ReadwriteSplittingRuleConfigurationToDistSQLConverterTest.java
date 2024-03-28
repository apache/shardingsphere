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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.provider;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingRuleConfigurationToDistSQLConverterTest {
    
    @Test
    void assertConvertWithEmptyDataSources() {
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = mock(ReadwriteSplittingRuleConfiguration.class);
        when(readwriteSplittingRuleConfiguration.getDataSources()).thenReturn(Collections.emptyList());
        ReadwriteSplittingRuleConfigurationToDistSQLConverter readwriteSplittingRuleConfigurationToDistSQLConverter = new ReadwriteSplittingRuleConfigurationToDistSQLConverter();
        assertThat(readwriteSplittingRuleConfigurationToDistSQLConverter.convert(readwriteSplittingRuleConfiguration), is(""));
    }
    
    @Test
    void assertConvert() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "test");
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig),
                Collections.singletonMap("test", new AlgorithmConfiguration("random", PropertiesBuilder.build(new PropertiesBuilder.Property("read_weight", "2:1")))));
        ReadwriteSplittingRuleConfigurationToDistSQLConverter readwriteSplittingRuleConfigurationToDistSQLConverter = new ReadwriteSplittingRuleConfigurationToDistSQLConverter();
        assertThat(readwriteSplittingRuleConfigurationToDistSQLConverter.convert(readwriteSplittingRuleConfiguration),
                is("CREATE READWRITE_SPLITTING RULE readwrite_ds (" + System.lineSeparator() + "WRITE_STORAGE_UNIT=ds_primary," + System.lineSeparator() + "READ_STORAGE_UNITS(ds_slave_0,ds_slave_1),"
                        + System.lineSeparator() + "TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC'," + System.lineSeparator() + "TYPE(NAME='random', PROPERTIES('read_weight'='2:1'))"
                        + System.lineSeparator() + ");"));
    }
    
    @Test
    void assertGetType() {
        ReadwriteSplittingRuleConfigurationToDistSQLConverter readwriteSplittingRuleConfigurationToDistSQLConverter = new ReadwriteSplittingRuleConfigurationToDistSQLConverter();
        assertThat(readwriteSplittingRuleConfigurationToDistSQLConverter.getType().getName(), is("org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration"));
    }
}
