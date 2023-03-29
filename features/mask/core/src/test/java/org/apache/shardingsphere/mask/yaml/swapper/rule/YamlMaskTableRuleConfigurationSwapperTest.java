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

package org.apache.shardingsphere.mask.yaml.swapper.rule;

import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlMaskTableRuleConfigurationSwapperTest {
    
    private final YamlMaskTableRuleConfigurationSwapper swapper = new YamlMaskTableRuleConfigurationSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        Collection<MaskColumnRuleConfiguration> encryptColumnRuleConfigs = Arrays.asList(
                new MaskColumnRuleConfiguration("mask_column_1", "md5_mask"),
                new MaskColumnRuleConfiguration("mask_column_2", "keep_from_x_to_y"),
                new MaskColumnRuleConfiguration("mask_column_3", "keep_first_m_last_m"));
        MaskTableRuleConfiguration encryptTableRuleConfig = new MaskTableRuleConfiguration("test_table", encryptColumnRuleConfigs);
        YamlMaskTableRuleConfiguration actualYamlMaskTableRuleConfig = swapper.swapToYamlConfiguration(encryptTableRuleConfig);
        assertThat(actualYamlMaskTableRuleConfig.getName(), is("test_table"));
        Map<String, YamlMaskColumnRuleConfiguration> actualColumns = actualYamlMaskTableRuleConfig.getColumns();
        assertThat(actualColumns.size(), is(3));
        YamlMaskColumnRuleConfiguration actualYamlMaskColumnRuleConfigFirst = actualColumns.get("mask_column_1");
        assertThat(actualYamlMaskColumnRuleConfigFirst.getMaskAlgorithm(), is("md5_mask"));
        YamlMaskColumnRuleConfiguration actualYamlMaskColumnRuleConfigSecond = actualColumns.get("mask_column_2");
        assertThat(actualYamlMaskColumnRuleConfigSecond.getMaskAlgorithm(), is("keep_from_x_to_y"));
        YamlMaskColumnRuleConfiguration actualYamlMaskColumnRuleConfigThird = actualColumns.get("mask_column_3");
        assertThat(actualYamlMaskColumnRuleConfigThird.getMaskAlgorithm(), is("keep_first_m_last_m"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlMaskColumnRuleConfiguration encryptColumnRuleConfig = new YamlMaskColumnRuleConfiguration();
        encryptColumnRuleConfig.setLogicColumn("mask_column");
        encryptColumnRuleConfig.setMaskAlgorithm("md5_mask");
        Map<String, YamlMaskColumnRuleConfiguration> columns = new LinkedHashMap<>(1);
        columns.put("mask_column", encryptColumnRuleConfig);
        YamlMaskTableRuleConfiguration yamlMaskTableRuleConfig = new YamlMaskTableRuleConfiguration();
        yamlMaskTableRuleConfig.setName("test_table");
        yamlMaskTableRuleConfig.setColumns(columns);
        MaskTableRuleConfiguration actualMaskTableRuleConfig = swapper.swapToObject(yamlMaskTableRuleConfig);
        assertThat(actualMaskTableRuleConfig.getName(), is("test_table"));
        Collection<MaskColumnRuleConfiguration> actualColumns = actualMaskTableRuleConfig.getColumns();
        assertThat(actualColumns.size(), is(1));
        MaskColumnRuleConfiguration actualMaskColumnRuleConfig = actualColumns.iterator().next();
        assertThat(actualMaskColumnRuleConfig.getLogicColumn(), is("mask_column"));
        assertThat(actualMaskColumnRuleConfig.getMaskAlgorithm(), is("md5_mask"));
    }
}
