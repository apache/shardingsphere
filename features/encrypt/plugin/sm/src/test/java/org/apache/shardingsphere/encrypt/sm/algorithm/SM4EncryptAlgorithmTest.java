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
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public final class SM4EncryptAlgorithmTest {
    
    @Test(expected = EncryptAlgorithmInitializationException.class)
    public void assertInitWithoutKey() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createECBProperties()), EncryptAlgorithm.class);
        algorithm.init(PropertiesBuilder.build(new Property("sm4-mode", "ECB"), new Property("sm4-padding", "PKCS5Padding")));
    }
    
    @Test
    public void assertEncryptNullValue() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createECBProperties()), EncryptAlgorithm.class);
        assertNull(algorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    public void assertEncryptWithECBMode() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createECBProperties()), EncryptAlgorithm.class);
        assertThat(algorithm.encrypt("test", mock(EncryptContext.class)), is("028654f2ca4f575dee9e1faae85dadde"));
    }
    
    @Test
    public void assertDecryptNullValue() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createECBProperties()), EncryptAlgorithm.class);
        assertNull(algorithm.decrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    public void assertDecryptWithECBMode() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createECBProperties()), EncryptAlgorithm.class);
        assertThat(algorithm.decrypt("028654f2ca4f575dee9e1faae85dadde", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    private Properties createECBProperties() {
        return PropertiesBuilder.build(new Property("sm4-key", "4D744E003D713D054E7E407C350E447E"), new Property("sm4-mode", "ECB"), new Property("sm4-padding", "PKCS5Padding"));
    }
    
    @Test
    public void assertEncryptWithCBCMode() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createCBCProperties()), EncryptAlgorithm.class);
        assertThat(algorithm.encrypt("test", mock(EncryptContext.class)), is("dca2127b57ba8cac36a0914e0208dc11"));
    }
    
    @Test
    public void assertDecrypt() {
        StandardEncryptAlgorithm<Object, String> algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("SM4", createCBCProperties()), EncryptAlgorithm.class);
        assertThat(algorithm.decrypt("dca2127b57ba8cac36a0914e0208dc11", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    private Properties createCBCProperties() {
        return PropertiesBuilder.build(
                new Property("sm4-key", "f201326119911788cFd30575b81059ac"), new Property("sm4-iv", "e166c3391294E69cc4c620f594fe00d7"),
                new Property("sm4-mode", "CBC"), new Property("sm4-padding", "PKCS7Padding"));
    }
}
