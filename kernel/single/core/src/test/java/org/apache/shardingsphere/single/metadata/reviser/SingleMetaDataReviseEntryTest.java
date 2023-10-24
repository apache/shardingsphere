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

package org.apache.shardingsphere.single.metadata.reviser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.metadata.reviser.constraint.SingleConstraintReviser;
import org.apache.shardingsphere.single.metadata.reviser.index.SingleIndexReviser;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SingleMetaDataReviseEntryTest {
    
    private SingleMetaDataReviseEntry reviseEntry;
    
    @BeforeEach
    public void setUp() {
        reviseEntry = new SingleMetaDataReviseEntry();
    }
    
    @Test
    public void testGetIndexReviser() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        SingleRule rule = new SingleRule(ruleConfig, "test_database", null, new HashMap<String, DataSource>(), Collections.emptyList());
        String tableName = "test_table";
        Optional<SingleIndexReviser> indexReviser = reviseEntry.getIndexReviser(rule, tableName);
        assertTrue(indexReviser.isPresent());
        assertEquals(SingleIndexReviser.class, indexReviser.get().getClass());
    }
    
    @Test
    public void testGetConstraintReviser() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        SingleRule rule = new SingleRule(ruleConfig, "test_database", null, new HashMap<String, DataSource>(), Collections.emptyList());
        String tableName = "test_table";
        Optional<SingleConstraintReviser> constraintReviser = reviseEntry.getConstraintReviser(rule, tableName);
        assertTrue(constraintReviser.isPresent());
        assertEquals(SingleConstraintReviser.class, constraintReviser.get().getClass());
    }
    
    @Test
    public void testGetOrder() {
        assertEquals(SingleOrder.ORDER, reviseEntry.getOrder());
    }
    
    @Test
    public void testGetTypeClass() {
        assertEquals(SingleRule.class, reviseEntry.getTypeClass());
    }
}
