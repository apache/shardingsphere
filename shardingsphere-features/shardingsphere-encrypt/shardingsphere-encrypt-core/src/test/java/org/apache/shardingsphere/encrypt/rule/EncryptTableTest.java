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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptTableTest {
    
    private EncryptTable encryptTable;
    
    @Before
    public void setUp() {
        encryptTable = new EncryptTable(new EncryptTableRuleConfiguration("t_encrypt", 
                Collections.singleton(new EncryptColumnRuleConfiguration("logicColumn", "cipherColumn", "assistedQueryColumn", "plainColumn", "myEncryptor"))));
    }
    
    @Test
    public void assertFindEncryptorName() {
        assertTrue(encryptTable.findEncryptorName("logicColumn").isPresent());
    }
    
    @Test
    public void assertNotFindEncryptorName() {
        assertFalse(encryptTable.findEncryptorName("notExistLogicColumn").isPresent());
    }
    
    @Test
    public void assertGetLogicColumns() {
        assertThat(encryptTable.getLogicColumns(), is(Collections.singleton("logicColumn")));
    }
    
    @Test
    public void assertGetLogicColumn() {
        assertNotNull(encryptTable.getLogicColumn("cipherColumn"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertGetLogicColumnWhenNotFind() {
        encryptTable.getLogicColumn("invalidColumn");
    }
    
    @Test
    public void assertIsCipherColumn() {
        assertTrue(encryptTable.isCipherColumn("CipherColumn"));
    }
    
    @Test
    public void assertIsNotCipherColumn() {
        assertFalse(encryptTable.isCipherColumn("logicColumn"));
    }
    
    @Test
    public void assertGetCipherColumn() {
        assertThat(encryptTable.getCipherColumn("LogicColumn"), is("cipherColumn"));
    }
    
    @Test
    public void assertGetAssistedQueryColumns() {
        assertThat(encryptTable.getAssistedQueryColumns(), is(Collections.singletonList("assistedQueryColumn")));
    }
    
    @Test
    public void assertFindAssistedQueryColumn() {
        Optional<String> actual = encryptTable.findAssistedQueryColumn("logicColumn");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("assistedQueryColumn"));
    }
    
    @Test
    public void assertNotFindAssistedQueryColumn() {
        assertFalse(encryptTable.findAssistedQueryColumn("notExistLogicColumn").isPresent());
    }
    
    @Test
    public void assertGetPlainColumns() {
        assertThat(encryptTable.getPlainColumns(), is(Collections.singletonList("plainColumn")));
    }
    
    @Test
    public void assertFindPlainColumn() {
        Optional<String> actual = encryptTable.findPlainColumn("logicColumn");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("plainColumn"));
    }
    
    @Test
    public void assertNotFindPlainColumn() {
        assertFalse(encryptTable.findPlainColumn("notExistLogicColumn").isPresent());
    }
    
    @Test
    public void assertGetLogicAndCipherColumns() {
        assertThat(encryptTable.getLogicAndCipherColumns(), is(ImmutableMap.of("logicColumn", "cipherColumn")));
    }
}
