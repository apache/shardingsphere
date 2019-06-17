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

package org.apache.shardingsphere.core.strategy.encrypt;

import com.google.common.base.Optional;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingEncryptorStrategyTest {
    
    @Test
    public void assertValidConstructor() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("test", "test.pwd1, test.pwd2", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getShardingEncryptor("test", "pwd2").get(), instanceOf(ShardingEncryptor.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInvalidConstructor() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", "test.pwd1, test.pwd2", "test.pwd1_index", new Properties());
        new ShardingEncryptorStrategy(encryptorRuleConfiguration);
    }
    
    @Test
    public void assertGetAssistedQueryColumn() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", "test.pwd1, test.pwd2", "test.pwd1_index,test.pwd2_index", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getAssistedQueryColumn("test", "pwd1"), is(Optional.of("pwd1_index")));
        assertThat(actual.getAssistedQueryColumn("test", "pwd3"), is(Optional.<String>absent()));
    }
    
    @Test
    public void assertGetAssistedQueryColumnWithoutResult() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("test", "test.pwd1, test.pwd2", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getAssistedQueryColumn("test", "pwd1"), is(Optional.<String>absent()));
    }
    
    @Test
    public void assertGetAssistedQueryCount() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", "test.pwd1, test.pwd2", "test.pwd1_index,test.pwd2_index", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getAssistedQueryColumnCount("test"), is(2));
    }
    
    @Test
    public void assertGetAssistedQueryColumnCountWithoutResult() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("test", "test.pwd1, test.pwd2", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getAssistedQueryColumnCount("test"), is(0));
        assertThat(actual.getAssistedQueryColumnCount("test1"), is(0));
    }
    
    @Test
    public void assertGetEncryptTableNames() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", "test.pwd1, test.pwd2", "test.pwd1_index,test.pwd2_index", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getEncryptTableNames().size(), is(1));
    }
    
    @Test
    public void assertIsHasShardingQueryAssistedEncryptor() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("assistedTest", "test.pwd1, test.pwd2", "test.pwd1_index,test.pwd2_index", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertTrue(actual.isHasShardingQueryAssistedEncryptor("test"));
    }
}
