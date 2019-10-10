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

package org.apache.shardingsphere.core.strategy.encrypt.impl;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.api.config.encrypt.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
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
    public void assertGetLogicColumn() {
        assertNotNull(encryptTable.getLogicColumn("cipherColumn"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetLogicColumnShardingExceptionThrownWhenCipherColumnAbsent() {
        encryptTable.getLogicColumn("___cipherColumn");
    }
    
    @Test
    public void assertGetLogicColumns() {
        assertFalse(encryptTable.getLogicColumns().isEmpty());
    }
    
    
    @Test
    public void assertGetLogicColumns() {
        assertFalse(encryptTable.getLogicColumns().isEmpty());
    }
}
