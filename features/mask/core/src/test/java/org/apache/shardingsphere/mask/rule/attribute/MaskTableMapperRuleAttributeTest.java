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

package org.apache.shardingsphere.mask.rule.attribute;

import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskTableMapperRuleAttributeTest {
    
    private final MaskTableMapperRuleAttribute ruleAttribute = new MaskTableMapperRuleAttribute(Collections.singleton(new MaskTableRuleConfiguration("foo_tbl", Collections.emptyList())));
    
    @Test
    void assertGetLogicTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getLogicTableNames()), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetDistributedTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getDistributedTableNames()), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetEnhancedTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getEnhancedTableNames()), is(Collections.emptyList()));
    }
}
