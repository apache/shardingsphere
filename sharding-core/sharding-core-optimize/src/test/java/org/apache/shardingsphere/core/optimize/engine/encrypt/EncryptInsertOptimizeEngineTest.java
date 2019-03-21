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

package org.apache.shardingsphere.core.optimize.engine.encrypt;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.junit.Before;

import java.util.Properties;

public final class EncryptInsertOptimizeEngineTest {
    
    private EncryptRule encryptRule;
    
    @Before
    public void setUp() {
        EncryptorConfiguration encryptorConfig = new EncryptorConfiguration("test", "col1, col2", new Properties());
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptTableRuleConfig.setTable("t_encrypt");
        encryptTableRuleConfig.setEncryptorConfig(encryptorConfig);
        EncryptorConfiguration encryptorQueryConfig = new EncryptorConfiguration("assistedTest", "col3, col4", "query3, query4", new Properties());
        EncryptTableRuleConfiguration encryptQueryTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptQueryTableRuleConfig.setTable("t_query_encrypt");
        encryptQueryTableRuleConfig.setEncryptorConfig(encryptorQueryConfig);
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration();
        encryptRuleConfig.getTableRuleConfigs().add(encryptTableRuleConfig);
        encryptRuleConfig.getTableRuleConfigs().add(encryptQueryTableRuleConfig);
    }
}
