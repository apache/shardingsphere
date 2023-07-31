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

import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SM4EncryptAlgorithmTest {
    
    @Test
    void assertInitWithoutKey() {
        assertThrows(EncryptAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", PropertiesBuilder.build(new Property("sm4-mode", "ECB"), new Property("sm4-padding", "PKCS5Padding"))));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertEncryptNullValue() {
        StandardEncryptAlgorithm<Object, String> algorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", createECBProperties());
        assertNull(algorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertEncryptWithECBMode() {
        StandardEncryptAlgorithm<Object, String> algorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", createECBProperties());
        assertThat(algorithm.encrypt("test", mock(EncryptContext.class)), is("028654f2ca4f575dee9e1faae85dadde"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDecryptNullValue() {
        StandardEncryptAlgorithm<Object, String> algorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", createECBProperties());
        assertNull(algorithm.decrypt(null, mock(EncryptContext.class)));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDecryptWithECBMode() {
        StandardEncryptAlgorithm<Object, String> algorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", createECBProperties());
        assertThat(algorithm.decrypt("028654f2ca4f575dee9e1faae85dadde", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    private Properties createECBProperties() {
        return PropertiesBuilder.build(new Property("sm4-key", "4D744E003D713D054E7E407C350E447E"), new Property("sm4-mode", "ECB"), new Property("sm4-padding", "PKCS5Padding"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertEncryptWithCBCMode() {
        StandardEncryptAlgorithm<Object, String> algorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", createCBCProperties());
        assertThat(algorithm.encrypt("test", mock(EncryptContext.class)), is("dca2127b57ba8cac36a0914e0208dc11"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDecrypt() {
        StandardEncryptAlgorithm<Object, String> algorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM4", createCBCProperties());
        assertThat(algorithm.decrypt("dca2127b57ba8cac36a0914e0208dc11", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    private Properties createCBCProperties() {
        return PropertiesBuilder.build(
                new Property("sm4-key", "f201326119911788cFd30575b81059ac"), new Property("sm4-iv", "e166c3391294E69cc4c620f594fe00d7"),
                new Property("sm4-mode", "CBC"), new Property("sm4-padding", "PKCS7Padding"));
    }
}
