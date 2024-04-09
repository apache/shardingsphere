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

package org.apache.shardingsphere.shadow.distsql.provider;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.provider.ShadowRuleConfigurationToDistSQLConverter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowRuleConfigurationToDistSQLConverterTest {
    
    @Test
    void assertConvertWithoutDataSources() {
        ShadowRuleConfiguration shadowRuleConfiguration = mock(ShadowRuleConfiguration.class);
        when(shadowRuleConfiguration.getDataSources()).thenReturn(Collections.emptyList());
        ShadowRuleConfigurationToDistSQLConverter shadowRuleConfigurationToDistSQLConverter = new ShadowRuleConfigurationToDistSQLConverter();
        assertThat(shadowRuleConfigurationToDistSQLConverter.convert(shadowRuleConfiguration), is(""));
    }
    
    @Test
    void assertConvert() {
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration();
        shadowRuleConfiguration.getDataSources().add(new ShadowDataSourceConfiguration("shadow_rule", "source", "shadow"));
        shadowRuleConfiguration.getShadowAlgorithms().put("user_id_select_match_algorithm", new AlgorithmConfiguration("REGEX_MATCH", new Properties()));
        shadowRuleConfiguration.getTables().put("t_order", new ShadowTableConfiguration(Collections.singleton("shadow_rule"), Collections.singleton("user_id_select_match_algorithm")));
        shadowRuleConfiguration.getTables().put("t_order_item", new ShadowTableConfiguration(Collections.singleton("shadow_rule"), Collections.singleton("user_id_select_match_algorithm")));
        ShadowRuleConfigurationToDistSQLConverter shadowRuleConfigurationToDistSQLConverter = new ShadowRuleConfigurationToDistSQLConverter();
        assertThat(shadowRuleConfigurationToDistSQLConverter.convert(shadowRuleConfiguration),
                is("CREATE SHADOW RULE shadow_rule(" + System.lineSeparator() + "SOURCE=source," + System.lineSeparator() + "SHADOW=shadow," + System.lineSeparator()
                        + "t_order(TYPE(NAME='regex_match'))," + System.lineSeparator() + "t_order_item(TYPE(NAME='regex_match'))" + System.lineSeparator() + ");"));
    }
    
    @Test
    void assertGetType() {
        ShadowRuleConfigurationToDistSQLConverter shadowRuleConfigurationToDistSQLConverter = new ShadowRuleConfigurationToDistSQLConverter();
        assertThat(shadowRuleConfigurationToDistSQLConverter.getType().getName(), is("org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration"));
    }
}
