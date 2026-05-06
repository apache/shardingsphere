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
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.rule.attribute.MaskTableMapperRuleAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleTest {
    
    private MaskRule maskRule;
    
    private MaskRuleConfiguration maskRuleConfiguration;
    
    @BeforeEach
    void setUp() {
        maskRuleConfiguration = createMaskRuleConfiguration();
        maskRule = new MaskRule(maskRuleConfiguration);
    }
    
    private MaskRuleConfiguration createMaskRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("foo_id", "t_mask_foo_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("KEEP_FIRST_N_LAST_M", createMaskAlgorithmProperties("2", "2", "*"));
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_foo_id_md5", algorithmConfig));
    }
    
    private Properties createMaskAlgorithmProperties(final String firstN, final String lastM, final String replaceChar) {
        Properties result = new Properties();
        result.setProperty("first-n", firstN);
        result.setProperty("last-m", lastM);
        result.setProperty("replace-char", replaceChar);
        return result;
    }
    
    @Test
    void assertFindMaskTableWhenTableNameExists() {
        assertTrue(maskRule.findMaskTable("foo_tbl").isPresent());
    }
    
    @Test
    void assertFindMaskTableWhenTableNameCaseInsensitive() {
        assertTrue(maskRule.findMaskTable("FOO_TBL").isPresent());
    }
    
    @Test
    void assertFindMaskTableWhenTableNameDoesNotExist() {
        assertFalse(maskRule.findMaskTable("non_existent_table").isPresent());
    }
    
    @Test
    void assertGetConfiguration() {
        assertThat(maskRule.getConfiguration(), is(maskRuleConfiguration));
    }
    
    @Test
    void assertGetAttributes() {
        Optional<MaskTableMapperRuleAttribute> actual = maskRule.getAttributes().findAttribute(MaskTableMapperRuleAttribute.class);
        assertTrue(actual.isPresent());
        assertThat(new LinkedList<>(actual.get().getLogicTableNames()), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertUpdateConfiguration() {
        MaskRuleConfiguration toBeUpdatedRuleConfig = createPartialAddTablesMaskRuleConfiguration();
        maskRule.updateConfiguration(toBeUpdatedRuleConfig);
        assertThat(maskRule.getConfiguration(), is(toBeUpdatedRuleConfig));
    }
    
    @Test
    void assertPartialUpdateWithToBeAddedTables() {
        assertFalse(maskRule.partialUpdate(createPartialAddTablesMaskRuleConfiguration()));
        assertTrue(maskRule.findMaskTable("bar_tbl").isPresent());
    }
    
    private MaskRuleConfiguration createPartialAddTablesMaskRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("bar_id", "t_mask_bar_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("bar_tbl", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_bar_id_md5", algorithmConfig));
    }
    
    @Test
    void assertPartialUpdateWithToBeRemovedTables() {
        assertFalse(maskRule.partialUpdate(createPartialRemoveTablesMaskRuleConfiguration()));
        assertFalse(maskRule.findMaskTable("foo_tbl").isPresent());
    }
    
    private MaskRuleConfiguration createPartialRemoveTablesMaskRuleConfiguration() {
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Collections.emptyList(), Collections.singletonMap("t_mask_bar_id_md5", algorithmConfig));
    }
    
    @Test
    void assertPartialUpdateWithToBeRemovedAlgorithms() {
        assertFalse(maskRule.partialUpdate(createPartialRemovedAlgorithmsMaskRuleConfiguration()));
        Optional<MaskTable> table = maskRule.findMaskTable("foo_tbl");
        assertTrue(table.isPresent());
        assertFalse(table.get().findAlgorithm("t_mask_foo_id_md5").isPresent());
    }
    
    private MaskRuleConfiguration createPartialRemovedAlgorithmsMaskRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("foo_id", "t_mask_foo_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(maskColumnRuleConfig));
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.emptyMap());
    }
    
    @Test
    void assertPartialUpdateWithToBeUpdatedAlgorithms() {
        Optional<MaskTable> tableBeforeUpdate = maskRule.findMaskTable("foo_tbl");
        assertTrue(tableBeforeUpdate.isPresent());
        Object maskedValueBeforeUpdate = tableBeforeUpdate.get().findAlgorithm("foo_id").get().mask("123456");
        assertFalse(maskRule.partialUpdate(createPartialUpdatedAlgorithmsMaskRuleConfiguration()));
        Optional<MaskTable> tableAfterUpdate = maskRule.findMaskTable("foo_tbl");
        assertTrue(tableAfterUpdate.isPresent());
        Object maskedValueAfterUpdate = tableAfterUpdate.get().findAlgorithm("foo_id").get().mask("123456");
        assertThat(maskedValueBeforeUpdate, is("12**56"));
        assertThat(maskedValueAfterUpdate, is("1####6"));
    }
    
    private MaskRuleConfiguration createPartialUpdatedAlgorithmsMaskRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("foo_id", "t_mask_foo_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("KEEP_FIRST_N_LAST_M", createMaskAlgorithmProperties("1", "1", "#"));
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_foo_id_md5", algorithmConfig));
    }
    
    @Test
    void assertGetOrder() {
        assertThat(maskRule.getOrder(), is(MaskOrder.ORDER));
    }
}
