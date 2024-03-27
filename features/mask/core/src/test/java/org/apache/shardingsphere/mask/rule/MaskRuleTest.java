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

package org.apache.shardingsphere.mask.rule;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleTest {
    
    private MaskRule maskRule;
    
    @BeforeEach
    void setUp() {
        maskRule = new MaskRule(createMaskRuleConfiguration());
    }
    
    private MaskRuleConfiguration createMaskRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("user_id", "t_mask_user_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("t_mask", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_user_id_md5", algorithmConfig));
    }
    
    @Test
    void assertFindMaskTableWhenTableNameExists() {
        assertTrue(maskRule.findMaskTable("t_mask").isPresent());
    }
    
    @Test
    void assertFindMaskTableWhenTableNameDoesNotExist() {
        assertFalse(maskRule.findMaskTable("non_existent_table").isPresent());
    }
}
