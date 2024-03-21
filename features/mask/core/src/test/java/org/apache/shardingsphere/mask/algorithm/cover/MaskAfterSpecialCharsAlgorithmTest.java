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

package org.apache.shardingsphere.mask.algorithm.cover;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaskAfterSpecialCharsAlgorithmTest {
    
    private MaskAfterSpecialCharsAlgorithm maskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = (MaskAfterSpecialCharsAlgorithm) TypedSPILoader.getService(MaskAlgorithm.class, "MASK_AFTER_SPECIAL_CHARS",
                PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", "*")));
    }
    
    @Test
    void assertMaskWithNullValue() {
        assertNull(maskAlgorithm.mask(null));
    }
    
    @Test
    void assertMask() {
        assertThat(maskAlgorithm.mask("abcd134"), is("abcd1**"));
    }
    
    @Test
    void assertMaskWhenPlainValueMatchedMultipleSpecialChars() {
        assertThat(maskAlgorithm.mask("abcd1234d1234"), is("abcd1********"));
    }
    
    @Test
    void assertMaskEmptyString() {
        assertThat(maskAlgorithm.mask(""), is(""));
    }
    
    @Test
    void assertMaskNull() {
        assertThat(maskAlgorithm.mask(null), is(nullValue()));
    }
    
    @Test
    void assertMaskWhenPlainValueNotMatchedSpecialChars() {
        assertThat(maskAlgorithm.mask("abcd234"), is("abcd234"));
    }
    
    @Test
    void assertInitWhenSpecialCharsIsEmpty() {
        Properties props = PropertiesBuilder.build(new Property("special-chars", ""), new Property("replace-char", "*"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_AFTER_SPECIAL_CHARS", props));
    }
    
    @Test
    void assertInitWhenReplaceCharIsEmpty() {
        Properties props = PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", ""));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_AFTER_SPECIAL_CHARS", props));
    }
    
    @Test
    void assertInitWhenReplaceCharIsMissing() {
        Properties props = PropertiesBuilder.build(new Property("special-chars", "d1"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_AFTER_SPECIAL_CHARS", props));
    }
    
    @Test
    void assertInitWhenPropertiesAreEmpty() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_AFTER_SPECIAL_CHARS", props));
    }
    
    @Test
    void assertInitWhenValidPropertiesAreSet() {
        MaskBeforeSpecialCharsAlgorithm algorithm = new MaskBeforeSpecialCharsAlgorithm();
        assertDoesNotThrow(() -> algorithm.init(PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", "*"))));
    }
}
