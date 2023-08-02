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

package org.apache.shardingsphere.encrypt.algorithm.like;

import org.apache.shardingsphere.encrypt.api.encrypt.like.LikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class CharDigestLikeEncryptAlgorithmTest {
    
    private LikeEncryptAlgorithm<Object, String> englishLikeEncryptAlgorithm;
    
    private LikeEncryptAlgorithm<Object, String> chineseLikeEncryptAlgorithm;
    
    private LikeEncryptAlgorithm<Object, String> koreanLikeEncryptAlgorithm;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        englishLikeEncryptAlgorithm = (LikeEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "CHAR_DIGEST_LIKE");
        chineseLikeEncryptAlgorithm = (LikeEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "CHAR_DIGEST_LIKE");
        koreanLikeEncryptAlgorithm = (LikeEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class,
                "CHAR_DIGEST_LIKE", PropertiesBuilder.build(new Property("dict", "한국어시험"), new Property("start", "44032")));
    }
    
    @Test
    void assertEncrypt() {
        assertThat(englishLikeEncryptAlgorithm.encrypt("1234567890%abcdefghijklmnopqrstuvwxyz%ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                mock(EncryptContext.class)), is("0145458981%`adedehihilmlmpqpqtutuxyxy%@ADEDEHIHILMLMPQPQTUTUXYXY"));
        assertThat(englishLikeEncryptAlgorithm.encrypt("_1234__5678__", mock(EncryptContext.class)), is("_0145__4589__"));
    }
    
    @Test
    void assertEncryptWithChineseChar() {
        assertThat(chineseLikeEncryptAlgorithm.encrypt("中国", mock(EncryptContext.class)), is("婝估"));
    }
    
    @Test
    void assertEncryptWithKoreanChar() {
        assertThat(koreanLikeEncryptAlgorithm.encrypt("한국", mock(EncryptContext.class)), is("각가"));
    }
    
    @Test
    void assertEncryptWithNullPlaintext() {
        assertNull(englishLikeEncryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
}
