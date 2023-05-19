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

import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeepFromXToYMaskAlgorithmTest {
    
    private KeepFromXToYMaskAlgorithm maskAlgorithm;
    
    private KeepFromXToYMaskAlgorithm sameFromXToYMaskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = new KeepFromXToYMaskAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "5"), new Property("replace-char", "*")));
        sameFromXToYMaskAlgorithm = new KeepFromXToYMaskAlgorithm();
        sameFromXToYMaskAlgorithm.init(PropertiesBuilder.build(new Property("from-x", "5"), new Property("to-y", "5"), new Property("replace-char", "*")));
    }
    
    @Test
    void assertMask() {
        assertThat(maskAlgorithm.mask("abc123456"), is("***123***"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc123456"), is("*****3***"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanFromXPlusOne() {
        assertThat(maskAlgorithm.mask("abc"), is("***"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc"), is("***"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsFromXPlusOne() {
        assertThat(maskAlgorithm.mask("abc1"), is("***1"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc123"), is("*****3"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanToYPlusOne() {
        assertThat(maskAlgorithm.mask("abc12"), is("***12"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc12"), is("*****"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsToYPlusOne() {
        assertThat(maskAlgorithm.mask("abc123"), is("***123"));
        assertThat(sameFromXToYMaskAlgorithm.mask("abc123"), is("*****3"));
    }
    
    @Test
    void assertInitWhenFromXIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", ""), new Property("to-y", "5"), new Property("replace-char", "*"))));
    }
    
    @Test
    void assertInitWhenToYIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "2"), new Property("to-y", ""), new Property("replace-char", "*"))));
    }
    
    @Test
    void assertInitWhenReplaceCharIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "2"), new Property("to-y", "5"), new Property("replace-char", ""))));
    }
    
    @Test
    void assertInitWhenFromXIsNotPositive() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new MaskFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "-3"), new Property("to-y", "5"), new Property("replace-char", "*"))));
    }
    
    @Test
    void assertInitWhenToYIsNotPositive() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new MaskFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "-5"), new Property("replace-char", "*"))));
    }
    
    @Test
    void assertInitWhenFromXGreaterThanToY() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "5"), new Property("to-y", "2"), new Property("replace-char", ""))));
    }
}
