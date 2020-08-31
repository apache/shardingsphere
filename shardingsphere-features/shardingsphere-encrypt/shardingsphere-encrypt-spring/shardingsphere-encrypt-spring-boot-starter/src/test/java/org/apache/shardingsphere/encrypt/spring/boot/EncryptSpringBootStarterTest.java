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

package org.apache.shardingsphere.encrypt.spring.boot;

import org.apache.shardingsphere.encrypt.algorithm.AESEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EncryptSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("encrypt")
public class EncryptSpringBootStarterTest {
    
    @Resource
    private AESEncryptAlgorithm aesEncryptor;
    
    @Resource
    private AlgorithmProvidedEncryptRuleConfiguration encryptRuleConfiguration;
    
    @Test
    public void assertAesEncryptor() {
        assertThat(aesEncryptor.getProps().getProperty("aes.key.value"), is("123456"));
    }
    
    @Test
    public void assertEncryptRuleConfiguration() {
        // TODO assert Encrypt Rule Configuration
    }
}
