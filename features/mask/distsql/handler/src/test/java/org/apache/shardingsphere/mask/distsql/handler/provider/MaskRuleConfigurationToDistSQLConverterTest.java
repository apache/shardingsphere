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

package org.apache.shardingsphere.mask.distsql.handler.provider;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskRuleConfigurationToDistSQLConverterTest {
    
    @Test
    void assertConvertWithEmptyTables() {
        MaskRuleConfiguration maskRuleConfiguration = mock(MaskRuleConfiguration.class);
        when(maskRuleConfiguration.getTables()).thenReturn(Collections.emptyList());
        MaskRuleConfigurationToDistSQLConverter maskRuleConfigurationToDistSQLConverter = new MaskRuleConfigurationToDistSQLConverter();
        assertThat(maskRuleConfigurationToDistSQLConverter.convert(maskRuleConfiguration), is(""));
    }
    
    @Test
    void assertConvert() {
        MaskRuleConfiguration maskRuleConfiguration = getMaskRuleConfiguration();
        MaskRuleConfigurationToDistSQLConverter maskRuleConfigurationToDistSQLConverter = new MaskRuleConfigurationToDistSQLConverter();
        assertThat(maskRuleConfigurationToDistSQLConverter.convert(maskRuleConfiguration),
                is("CREATE MASK RULE t_mask (" + System.lineSeparator() + "COLUMNS(" + System.lineSeparator() + "(NAME=user_id, TYPE(NAME='md5'))" + System.lineSeparator() + "),;"));
    }
    
    @Test
    void assertGetType() {
        MaskRuleConfigurationToDistSQLConverter maskRuleConfigurationToDistSQLConverter = new MaskRuleConfigurationToDistSQLConverter();
        assertThat(maskRuleConfigurationToDistSQLConverter.getType().getName(), is("org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration"));
    }
    
    private MaskRuleConfiguration getMaskRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("user_id", "t_mask_user_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("t_mask", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_user_id_md5", algorithmConfig));
    }
}
