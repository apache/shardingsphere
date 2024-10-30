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

package org.apache.shardingsphere.single.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SingleRuleConfigurationToDistSQLConverterTest {
    
    @SuppressWarnings("unchecked")
    private final RuleConfigurationToDistSQLConverter<SingleRuleConfiguration> converter = TypedSPILoader.getService(RuleConfigurationToDistSQLConverter.class, SingleRuleConfiguration.class);
    
    @Test
    void assertConvertWithEmptyTablesAndDefaultDatasource() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        assertThat(converter.convert(ruleConfig), is(""));
    }
    
    @Test
    void assertConvert() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Arrays.asList("foo_tbl", "bar_tbl"), "foo_ds");
        assertThat(converter.convert(ruleConfig),
                is("LOAD SINGLE TABLE foo_tbl,bar_tbl;" + System.lineSeparator() + System.lineSeparator() + "SET DEFAULT SINGLE TABLE STORAGE UNIT = foo_ds;"));
    }
    
    @Test
    void assertConvertWithDefaultDatasourceOnly() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Collections.emptyList(), "foo_ds");
        assertThat(converter.convert(ruleConfig), is("SET DEFAULT SINGLE TABLE STORAGE UNIT = foo_ds;"));
    }
}
