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

package org.apache.shardingsphere.encrypt.rule;

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLogicColumnNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptTableTest {
    
    private EncryptTable encryptTable;
    
    @BeforeEach
    void setUp() {
        encryptTable = new EncryptTable(new EncryptTableRuleConfiguration("t_encrypt",
                Collections.singleton(new EncryptColumnRuleConfiguration("logicColumn", "cipherColumn", "assistedQueryColumn", "likeQueryColumn", "myEncryptor"))));
    }
    
    @Test
    void assertFindEncryptorName() {
        assertTrue(encryptTable.findEncryptorName("logicColumn").isPresent());
    }
    
    @Test
    void assertNotFindEncryptorName() {
        assertFalse(encryptTable.findEncryptorName("notExistLogicColumn").isPresent());
    }
    
    @Test
    void assertGetLogicColumns() {
        assertThat(encryptTable.getLogicColumns(), is(Collections.singleton("logicColumn")));
    }
    
    @Test
    void assertGetLogicColumnByCipherColumn() {
        assertNotNull(encryptTable.getLogicColumnByCipherColumn("cipherColumn"));
    }
    
    @Test
    void assertGetLogicColumnByCipherColumnWhenNotFind() {
        assertThrows(EncryptLogicColumnNotFoundException.class, () -> encryptTable.getLogicColumnByCipherColumn("invalidColumn"));
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
    void assertGetCipherColumn() {
        assertThat(encryptTable.getCipherColumn("LogicColumn"), is("cipherColumn"));
    }
    
    @Test
    void assertGetAssistedQueryColumns() {
        assertThat(encryptTable.getAssistedQueryColumns(), is(Collections.singletonList("assistedQueryColumn")));
    }
    
    @Test
    void assertFindAssistedQueryColumn() {
        Optional<String> actual = encryptTable.findAssistedQueryColumn("logicColumn");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("assistedQueryColumn"));
    }
    
    @Test
    void assertFindLikeQueryColumn() {
        Optional<String> actual = encryptTable.findLikeQueryColumn("logicColumn");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("likeQueryColumn"));
    }
    
    @Test
    void assertNotFindAssistedQueryColumn() {
        assertFalse(encryptTable.findAssistedQueryColumn("notExistLogicColumn").isPresent());
    }
    
    @Test
    void assertNotFindLikeQueryColumn() {
        assertFalse(encryptTable.findAssistedQueryColumn("notExistLikeQueryColumn").isPresent());
    }
    
    @Test
    void assertGetLogicAndCipherColumns() {
        assertThat(encryptTable.getLogicAndCipherColumns(), is(Collections.singletonMap("logicColumn", "cipherColumn")));
        assertTrue(encryptTable.getLogicAndCipherColumns().containsKey("LOGICCOLUMN"));
    }
}
