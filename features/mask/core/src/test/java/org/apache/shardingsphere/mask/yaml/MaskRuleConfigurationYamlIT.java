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

package org.apache.shardingsphere.mask.yaml;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    MaskRuleConfigurationYamlIT() {
        super("yaml/mask-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static MaskRuleConfiguration getExpectedRuleConfiguration() {
        Collection<MaskTableRuleConfiguration> tables = Collections.singletonList(new MaskTableRuleConfiguration("t_user",
                Arrays.asList(new MaskColumnRuleConfiguration("telephone", "keep_first_n_last_m_mask"), new MaskColumnRuleConfiguration("password", "md5_mask"))));
        Map<String, AlgorithmConfiguration> maskAlgorithms = new LinkedHashMap<>(2, 1F);
        maskAlgorithms.put("keep_first_n_last_m_mask",
                new AlgorithmConfiguration("KEEP_FIRST_N_LAST_M", PropertiesBuilder.build(new Property("first-n", 3), new Property("replace-char", "*"), new Property("last-m", 4))));
        maskAlgorithms.put("md5_mask", new AlgorithmConfiguration("MD5", new Properties()));
        return new MaskRuleConfiguration(tables, maskAlgorithms);
    }
    
    @Override
    protected boolean assertYamlConfiguration(final YamlRuleConfiguration actual) {
        assertMaskRule((YamlMaskRuleConfiguration) actual);
        return true;
    }
    
    private void assertMaskRule(final YamlMaskRuleConfiguration actual) {
        assertTables(actual.getTables());
        assertMaskAlgorithm(actual.getMaskAlgorithms());
    }
    
    private void assertTables(final Map<String, YamlMaskTableRuleConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("t_user").getColumns().size(), is(2));
        assertThat(actual.get("t_user").getColumns().get("telephone").getMaskAlgorithm(), is("keep_first_n_last_m_mask"));
        assertThat(actual.get("t_user").getColumns().get("password").getMaskAlgorithm(), is("md5_mask"));
    }
    
    private void assertMaskAlgorithm(final Map<String, YamlAlgorithmConfiguration> actual) {
        assertThat(actual.size(), is(2));
        assertThat(actual.get("keep_first_n_last_m_mask").getType(), is("KEEP_FIRST_N_LAST_M"));
        assertThat(actual.get("keep_first_n_last_m_mask").getProps().getProperty("replace-char"), is("*"));
        assertThat(actual.get("md5_mask").getType(), is("MD5"));
        assertTrue(actual.get("md5_mask").getProps().isEmpty());
    }
}
