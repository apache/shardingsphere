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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReadwriteSplittingRuleConfigurationToDistSQLConverterTest {
    
    @SuppressWarnings("unchecked")
    private final RuleConfigurationToDistSQLConverter<ReadwriteSplittingRuleConfiguration> converter = TypedSPILoader.getService(
            RuleConfigurationToDistSQLConverter.class, ReadwriteSplittingRuleConfiguration.class);
    
    @Test
    void assertConvertWithEmptyDataSources() {
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig = new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        assertThat(converter.convert(readwriteSplittingRuleConfig), is(""));
    }
    
    @Test
    void assertConvert() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createRuleConfiguration();
        assertThat(converter.convert(ruleConfig),
                is("CREATE READWRITE_SPLITTING RULE foo_ds ("
                        + System.lineSeparator()
                        + "WRITE_STORAGE_UNIT=ds_primary,"
                        + System.lineSeparator()
                        + "READ_STORAGE_UNITS(ds_slave_0,ds_slave_1),"
                        + System.lineSeparator()
                        + "TRANSACTIONAL_READ_QUERY_STRATEGY='PRIMARY',"
                        + System.lineSeparator()
                        + "TYPE(NAME='random', PROPERTIES('read_weight'='2:1'))"
                        + System.lineSeparator()
                        + "), bar_ds ("
                        + System.lineSeparator()
                        + "WRITE_STORAGE_UNIT=ds_primary,"
                        + System.lineSeparator()
                        + "READ_STORAGE_UNITS(ds_slave_0,ds_slave_1),"
                        + System.lineSeparator()
                        + "TRANSACTIONAL_READ_QUERY_STRATEGY='PRIMARY'"
                        + System.lineSeparator()
                        + ");"));
    }
    
    private ReadwriteSplittingRuleConfiguration createRuleConfiguration() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig0 = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_ds", "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "test");
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig1 = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "bar_ds", "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "not_existed");
        return new ReadwriteSplittingRuleConfiguration(Arrays.asList(dataSourceGroupConfig0, dataSourceGroupConfig1),
                Collections.singletonMap("test", new AlgorithmConfiguration("random", PropertiesBuilder.build(new Property("read_weight", "2:1")))));
    }
}
