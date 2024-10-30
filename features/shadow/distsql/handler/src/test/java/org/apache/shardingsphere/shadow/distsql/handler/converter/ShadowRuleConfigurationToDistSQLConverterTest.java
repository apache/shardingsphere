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

package org.apache.shardingsphere.shadow.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShadowRuleConfigurationToDistSQLConverterTest {
    
    @SuppressWarnings("unchecked")
    private final RuleConfigurationToDistSQLConverter<ShadowRuleConfiguration> converter = TypedSPILoader.getService(RuleConfigurationToDistSQLConverter.class, ShadowRuleConfiguration.class);
    
    @Test
    void assertConvertWithoutDataSources() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        assertThat(converter.convert(ruleConfig), is(""));
    }
    
    @Test
    void assertConvert() {
        ShadowRuleConfiguration ruleConfig = createRuleConfiguration();
        assertThat(converter.convert(ruleConfig),
                is("CREATE SHADOW RULE shadow_rule(" + System.lineSeparator() + "SOURCE=source," + System.lineSeparator() + "SHADOW=shadow," + System.lineSeparator()
                        + "t_order(TYPE(NAME='regex_match'))," + System.lineSeparator() + "t_order_item(TYPE(NAME='regex_match'))" + System.lineSeparator() + ");"));
    }
    
    private ShadowRuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("shadow_rule", "source", "shadow"));
        result.getShadowAlgorithms().put("user_id_select_match_algorithm", new AlgorithmConfiguration("REGEX_MATCH", new Properties()));
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.singleton("shadow_rule"), Collections.singleton("user_id_select_match_algorithm")));
        result.getTables().put("t_order_item", new ShadowTableConfiguration(Collections.singleton("shadow_rule"), Collections.singleton("user_id_select_match_algorithm")));
        return result;
    }
}
