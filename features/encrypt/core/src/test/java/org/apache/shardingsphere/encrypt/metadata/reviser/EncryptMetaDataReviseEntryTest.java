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

package org.apache.shardingsphere.encrypt.metadata.reviser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.metadata.reviser.column.EncryptColumnExistedReviser;
import org.apache.shardingsphere.encrypt.metadata.reviser.column.EncryptColumnNameReviser;
import org.apache.shardingsphere.encrypt.metadata.reviser.index.EncryptIndexReviser;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.junit.jupiter.api.Test;

public class EncryptMetaDataReviseEntryTest {
    
    private final EncryptMetaDataReviseEntry reviseEntry = new EncryptMetaDataReviseEntry();
    
    private final EncryptRule rule = createEncryptRule();
    
    private EncryptRule createEncryptRule() {
        EncryptRuleConfiguration ruleConfig = new EncryptRuleConfiguration(Collections.singleton(
                new EncryptTableRuleConfiguration("test_table", Collections.emptyList())), new HashMap<>());
        
        return new EncryptRule("test_database", ruleConfig);
    }
    
    @Test
    void assertGetIndexReviser() {
        Optional<EncryptIndexReviser> indexReviser = reviseEntry.getIndexReviser(rule, "test_table");
        assertTrue(indexReviser.isPresent());
        assertThat(indexReviser.get().getClass(), is(EncryptIndexReviser.class));
    }
    
    @Test
    void assertGetColumnNameReviser() {
        Optional<EncryptColumnNameReviser> columnNameReviser = reviseEntry.getColumnNameReviser(rule, "test_table");
        assertTrue(columnNameReviser.isPresent());
        assertThat(columnNameReviser.get().getClass(), is(EncryptColumnNameReviser.class));
        
    }
    
    @Test
    void assertGetColumnExistedReviser() {
        Optional<EncryptColumnExistedReviser> columnExistedReviser = reviseEntry.getColumnExistedReviser(rule, "test_table");
        assertTrue(columnExistedReviser.isPresent());
        assertThat(columnExistedReviser.get().getClass(), is(EncryptColumnExistedReviser.class));
    }
}
