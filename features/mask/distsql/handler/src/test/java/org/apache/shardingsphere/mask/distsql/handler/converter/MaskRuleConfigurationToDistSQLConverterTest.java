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

package org.apache.shardingsphere.mask.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskRuleConfigurationToDistSQLConverterTest {
    
    @SuppressWarnings("unchecked")
    private final RuleConfigurationToDistSQLConverter<MaskRuleConfiguration> converter = TypedSPILoader.getService(RuleConfigurationToDistSQLConverter.class, MaskRuleConfiguration.class);
    
    @Test
    void assertConvertWithEmptyTables() {
        MaskRuleConfiguration ruleConfig = new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        assertThat(converter.convert(ruleConfig), is(""));
    }
    
    @Test
    void assertConvert() {
        MaskRuleConfiguration ruleConfig = createRuleConfiguration();
        assertThat(converter.convert(ruleConfig), is("CREATE MASK RULE"
                + " foo_tbl (" + System.lineSeparator() + "COLUMNS(" + System.lineSeparator() + "(NAME=foo_col_1, TYPE(NAME='md5'))," + System.lineSeparator() + "(NAME=foo_col_2, TYPE(NAME='md5'))"
                + System.lineSeparator() + "))," + System.lineSeparator()
                + " bar_tbl (" + System.lineSeparator() + "COLUMNS(" + System.lineSeparator() + "(NAME=bar_col, TYPE(NAME='md5'))" + System.lineSeparator() + "));"));
    }
    
    private MaskRuleConfiguration createRuleConfiguration() {
        MaskTableRuleConfiguration fooTableRuleConfig = new MaskTableRuleConfiguration("foo_tbl",
                Arrays.asList(new MaskColumnRuleConfiguration("foo_col_1", "md5_algo"), new MaskColumnRuleConfiguration("foo_col_2", "md5_algo")));
        MaskTableRuleConfiguration barTableRuleConfig = new MaskTableRuleConfiguration("bar_tbl", Collections.singleton(new MaskColumnRuleConfiguration("bar_col", "md5_algo")));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Arrays.asList(fooTableRuleConfig, barTableRuleConfig), Collections.singletonMap("md5_algo", algorithmConfig));
    }
}
