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

import org.apache.shardingsphere.encrypt.factory.EncryptAlgorithmFactory;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public final class CharDigestFuzzyEncryptAlgorithmTest {
    
    private EncryptAlgorithm<Object, String> encryptAlgorithm;
    
    private EncryptAlgorithm<Object, String> chineseEncryptAlgorithm;
    
    private EncryptAlgorithm<Object, String> koreanEncryptAlgorithm;
    
    @Before
    public void setUp() {
        encryptAlgorithm = EncryptAlgorithmFactory.newInstance(new AlgorithmConfiguration("CHAR_DIGEST_FUZZY", new Properties()));
        chineseEncryptAlgorithm = EncryptAlgorithmFactory.newInstance(new AlgorithmConfiguration("CHAR_DIGEST_FUZZY", new Properties()));
        koreanEncryptAlgorithm = EncryptAlgorithmFactory.newInstance(new AlgorithmConfiguration("CHAR_DIGEST_FUZZY", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("dict", "한국어시험");
        result.setProperty("start", "44032");
        return result;
    }
    
    @Test
    public void assertEncode() {
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("5$45"));
    }
    
    @Test
    public void assertChineseEncode() {
        assertThat(chineseEncryptAlgorithm.encrypt("中国", mock(EncryptContext.class)), is("娝侰"));
    }
    
    @Test
    public void assertKoreanEncode() {
        assertThat(koreanEncryptAlgorithm.encrypt("한국", mock(EncryptContext.class)), is("각가"));
    }
    
    @Test
    public void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    public void assertDecode() {
        assertThat(encryptAlgorithm.decrypt("test", mock(EncryptContext.class)).toString(), is("test"));
    }
}
