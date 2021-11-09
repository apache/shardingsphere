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

package org.apache.shardingsphere.encrypt.algorithm;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CustomizedEncryptAlgorithmTest {
    
    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    private EncryptAlgorithm<Integer, Integer> encryptAlgorithm;
    
    @Before
    public void setUp() {
        encryptAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("CUSTOMIZED", new Properties()), EncryptAlgorithm.class);
    }
    
    @Test
    public void assertEncrypt() {
        assertThat(encryptAlgorithm.encrypt(1000), is(1765228022));
    }
    
    @Test
    public void assertDecrypt() {
        assertThat(encryptAlgorithm.decrypt(1765228022), is(1000));
    }
}
