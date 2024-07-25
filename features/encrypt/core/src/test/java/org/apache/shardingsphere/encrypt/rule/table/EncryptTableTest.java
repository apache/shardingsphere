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

package org.apache.shardingsphere.encrypt.rule.table;

import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLogicColumnNotFoundException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class EncryptTableTest {
    
    private EncryptTable encryptTable;
    
    @BeforeEach
    void setUp() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("logicColumn", new EncryptColumnItemRuleConfiguration("cipherColumn", "myEncryptor"));
        columnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("assistedQueryColumn", "foo_assist_query_encryptor"));
        columnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("likeQueryColumn", "foo_like_encryptor"));
        Map<String, EncryptAlgorithm> encryptors = new HashMap<>(3, 1F);
        encryptors.put("myEncryptor", mock(EncryptAlgorithm.class));
        encryptors.put("foo_assist_query_encryptor", mock(EncryptAlgorithm.class));
        encryptors.put("foo_like_encryptor", mock(EncryptAlgorithm.class));
        encryptTable = new EncryptTable(new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig)), encryptors);
    }
    
    @Test
    void assertFindEncryptorName() {
        assertTrue(encryptTable.findEncryptor("logicColumn").isPresent());
    }
    
    @Test
    void assertNotFindEncryptorName() {
        assertFalse(encryptTable.findEncryptor("notExistLogicColumn").isPresent());
    }
    
    @Test
    void assertGetLogicColumns() {
        assertThat(encryptTable.getLogicColumns(), is(Collections.singleton("logicColumn")));
    }
    
    @Test
    void assertIsCipherColumn() {
        assertTrue(encryptTable.isCipherColumn("CipherColumn"));
    }
    
    @Test
    void assertIsNotCipherColumn() {
        assertFalse(encryptTable.isCipherColumn("logicColumn"));
    }
    
    @Test
    void assertIsEncryptColumn() {
        assertTrue(encryptTable.isEncryptColumn("logicColumn"));
    }
    
    @Test
    void assertGetLogicColumnByCipherColumn() {
        assertThat(encryptTable.getLogicColumnByCipherColumn("cipherColumn"), is("logicColumn"));
    }
    
    @Test
    void assertGetEncryptColumn() {
        assertNotNull(encryptTable.getEncryptColumn("logicColumn"));
    }
    
    @Test
    void assertGetLogicColumnByCipherColumnWhenNotFind() {
        assertThrows(EncryptLogicColumnNotFoundException.class, () -> encryptTable.getLogicColumnByCipherColumn("invalidColumn"));
    }
    
    @Test
    void assertFindQueryEncryptor() {
        assertTrue(encryptTable.getEncryptColumn("logicColumn").getAssistedQuery().isPresent());
        assertThat(encryptTable.findQueryEncryptor("logicColumn"), is(Optional.of(encryptTable.getEncryptColumn("logicColumn").getAssistedQuery().get().getEncryptor())));
    }
    
    @Test
    void assertFindQueryEncryptorWithNotEncryptColumn() {
        assertThat(encryptTable.findQueryEncryptor("invalidColumn"), is(Optional.empty()));
    }
}
