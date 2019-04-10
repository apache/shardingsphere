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
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingEncryptorStrategyTest {
    
    @Test
    public void assertValidConstructor() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("test", "pwd1, pwd2", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getShardingEncryptor("test", "pwd2"), instanceOf(ShardingEncryptor.class));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertInvalidConstructor() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("test", "pwd1, pwd2", "pwd1_index", new Properties());
        new ShardingEncryptorStrategy(encryptorRuleConfiguration);
    }
    
    @Test
    public void assertGetAssistedQueryColumn() {
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("test", "pwd1, pwd2", "pwd1_index,pwd2_index", new Properties());
        ShardingEncryptorStrategy actual = new ShardingEncryptorStrategy(encryptorRuleConfiguration);
        assertThat(actual.getAssistedQueryColumn("test", "pwd1"), is(Optional.of("pwd1_index")));
    }
}
