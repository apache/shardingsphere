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

package org.apache.shardingsphere.encrypt.algorithm.assisted;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MD5AssistedEncryptAlgorithmTest {
    
    private EncryptAlgorithm encryptAlgorithm;
    
    @BeforeEach
    void setUp() {
        encryptAlgorithm = TypedSPILoader.getService(EncryptAlgorithm.class, "MD5");
    }
    
    @Test
    void assertEncrypt() {
        assertThat(encryptAlgorithm.encrypt("test", mock(AlgorithmSQLContext.class)), is("098f6bcd4621d373cade4e832627b4f6"));
    }
    
    @Test
    void assertDecrypt() {
        assertThrows(UnsupportedOperationException.class, () -> encryptAlgorithm.decrypt("test", mock(AlgorithmSQLContext.class)));
    }
    
    @Test
    void assertToConfiguration() {
        AlgorithmConfiguration actual = encryptAlgorithm.toConfiguration();
        assertThat(actual.getType(), is("MD5"));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().getProperty("salt"), is(""));
    }
}
