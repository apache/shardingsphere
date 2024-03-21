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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaskFromXToYMaskAlgorithmTest {
    
    private MaskFromXToYMaskAlgorithm maskAlgorithm;
    
    private MaskFromXToYMaskAlgorithm sameFromXToYMaskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = (MaskFromXToYMaskAlgorithm) TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y",
                PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "5"), new Property("replace-char", "*")));
        sameFromXToYMaskAlgorithm = (MaskFromXToYMaskAlgorithm) TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y",
                PropertiesBuilder.build(new Property("from-x", "5"), new Property("to-y", "5"), new Property("replace-char", "*")));
    }
    
    @Test
    void assertMaskWithNullValue() {
        assertNull(maskAlgorithm.mask(null));
    }
    
    @Test
    void assertMask() {
        assertThat(maskAlgorithm.mask("abc123456"), is("abc***456"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc123456"), is("abc12*456"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanFromXPlusOne() {
        assertThat(maskAlgorithm.mask("abc"), is("abc"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc"), is("abc"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsFromXPlusOne() {
        assertThat(maskAlgorithm.mask("abc1"), is("abc*"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc123"), is("abc12*"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanToYPlusOne() {
        assertThat(maskAlgorithm.mask("abc12"), is("abc**"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc12"), is("abc12"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsToYPlusOne() {
        assertThat(maskAlgorithm.mask("abc123"), is("abc***"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc123"), is("abc12*"));
    }
    
    @Test
    void assertInitWhenFromXIsEmpty() {
        Properties props = PropertiesBuilder.build(new Property("from-x", ""), new Property("to-y", "5"), new Property("replace-char", "*"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y", props));
    }
    
    @Test
    void assertInitWhenToYIsEmpty() {
        Properties props = PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", ""), new Property("replace-char", "*"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y", props));
    }
    
    @Test
    void assertInitWhenReplaceCharIsEmpty() {
        Properties props = PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "5"), new Property("replace-char", ""));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y", props));
    }
    
    @Test
    void assertInitWhenFromXIsNotPositive() {
        Properties props = PropertiesBuilder.build(new Property("from-x", "-3"), new Property("to-y", "5"), new Property("replace-char", "*"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y", props));
    }
    
    @Test
    void assertInitWhenToYIsNotPositive() {
        Properties props = PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "-5"), new Property("replace-char", "*"));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y", props));
    }
    
    @Test
    void assertInitWhenFromXGreaterThanToY() {
        Properties props = PropertiesBuilder.build(new Property("from-x", "5"), new Property("to-y", "2"), new Property("replace-char", ""));
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y", props));
    }
}
