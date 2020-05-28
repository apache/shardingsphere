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

package org.apache.shardingsphere.encrypt.strategy.impl;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.strategy.EncryptTable;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptTableTest {
    
    private EncryptTable encryptTable;
    
    @Before
    public void setUp() {
        EncryptTableRuleConfiguration config = mock(EncryptTableRuleConfiguration.class);
        EncryptColumnRuleConfiguration encryptColumnRuleConfiguration = mock(EncryptColumnRuleConfiguration.class);
        when(config.getColumns()).thenReturn(ImmutableMap.of("key", encryptColumnRuleConfiguration));
        when(encryptColumnRuleConfiguration.getCipherColumn()).thenReturn("cipherColumn");
        when(encryptColumnRuleConfiguration.getAssistedQueryColumn()).thenReturn("assistedQueryColumn");
        when(encryptColumnRuleConfiguration.getPlainColumn()).thenReturn("plainColumn");
        when(encryptColumnRuleConfiguration.getEncryptor()).thenReturn("encryptor");
        encryptTable = new EncryptTable(config);
    }
    
    @Test
    public void assertGetLogicColumnOfCipher() {
        assertNotNull(encryptTable.getLogicColumnOfCipher("cipherColumn"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertGetLogicColumnShardingExceptionThrownWhenCipherColumnAbsent() {
        encryptTable.getLogicColumnOfCipher("___cipherColumn");
    }
    
    @Test
    public void assertGetLogicColumns() {
        assertFalse(encryptTable.getLogicColumns().isEmpty());
    }
    
    @Test
    public void assertFindPlainColumn() {
        assertFalse(encryptTable.findPlainColumn("logicColumn").isPresent());
    }
    
    @Test
    public void assertGetLogicAndCipherColumns() {
        assertFalse(encryptTable.getLogicAndCipherColumns().isEmpty());
    }
    
    @Test
    public void assertGetLogicAndPlainColumns() {
        assertFalse(encryptTable.getLogicAndPlainColumns().isEmpty());
    }

    @Test
    public void assertGetEncryptor() {
        assertTrue(encryptTable.findEncryptor("key").isPresent());
        assertFalse(encryptTable.findEncryptor("notExistLogicColumn").isPresent());
    }
}
