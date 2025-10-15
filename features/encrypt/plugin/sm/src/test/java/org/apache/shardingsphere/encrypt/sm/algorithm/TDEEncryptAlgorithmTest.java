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

package org.apache.shardingsphere.encrypt.sm.algorithm;

import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class TDEEncryptAlgorithmTest {
    
    private TDEEncryptAlgorithm algorithm;
    
    private EncryptContext encryptContext;
    
    @BeforeEach
    void setUp() {
        algorithm = new TDEEncryptAlgorithm();
        encryptContext = new EncryptContext("test_db", "test_schema", "test_table", "test_column");
    }
    
    @Test
    void assertInitWithValidProperties() {
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1");
        algorithm.init(props);
        assertThat(algorithm.getType(), is("SM4_FPE"));
    }
    
    @Test
    void assertEncryptAndDecryptWithNumbers() {
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1"); // 数字字符集
        algorithm.init(props);
        
        String plainValue = "13812345678";
        String encryptedValue = algorithm.encrypt(plainValue, encryptContext);
        String decryptedValue = (String) algorithm.decrypt(encryptedValue, encryptContext);
        
        assertThat(encryptedValue, not(plainValue));
        assertThat(decryptedValue, is(plainValue));
        assertThat(encryptedValue.length(), is(plainValue.length())); // 格式保留
    }
    
    @Test
    void assertEncryptAndDecryptWithMixedCharacters() {
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1,2,3"); // 数字+小写+大写字母
        algorithm.init(props);
        
        String plainValue = "Test123";
        String encryptedValue = algorithm.encrypt(plainValue, encryptContext);
        String decryptedValue = (String) algorithm.decrypt(encryptedValue, encryptContext);
        
        assertThat(encryptedValue, not(plainValue));
        assertThat(decryptedValue, is(plainValue));
        assertThat(encryptedValue.length(), is(plainValue.length())); // 格式保留
    }
    
    @Test
    void assertEncryptWithNullValue() {
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1");
        algorithm.init(props);
        
        assertNull(algorithm.encrypt(null, encryptContext));
    }
    
    @Test
    void assertDecryptWithNullValue() {
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1");
        algorithm.init(props);
        
        assertNull(algorithm.decrypt(null, encryptContext));
    }
    
    @Test
    void assertGetType() {
        assertThat(algorithm.getType(), is("SM4_FPE"));
    }
}