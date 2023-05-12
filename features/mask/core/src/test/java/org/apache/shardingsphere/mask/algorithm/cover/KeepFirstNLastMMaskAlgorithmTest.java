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

class KeepFirstNLastMMaskAlgorithmTest {
    
    private KeepFirstNLastMMaskAlgorithm maskAlgorithm;
    
    private KeepFirstNLastMMaskAlgorithm sameFirstNLastMMaskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = new KeepFirstNLastMMaskAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("first-n", "3"), new Property("last-m", "5"), new Property("replace-char", "*")));
        sameFirstNLastMMaskAlgorithm = new KeepFirstNLastMMaskAlgorithm();
        sameFirstNLastMMaskAlgorithm.init(PropertiesBuilder.build(new Property("first-n", "5"), new Property("last-m", "5"), new Property("replace-char", "*")));
    }
    
    @Test
    void assertMask() {
        assertThat(maskAlgorithm.mask("abc123456"), is("abc*23456"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc123456789"), is("abc12**56789"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanFirstN() {
        assertThat(maskAlgorithm.mask("ab"), is("ab"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc"), is("abc"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsFirstN() {
        assertThat(maskAlgorithm.mask("abc"), is("abc"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc12"), is("abc12"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanLastM() {
        assertThat(maskAlgorithm.mask("abc1"), is("abc1"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc1"), is("abc1"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsLastM() {
        assertThat(maskAlgorithm.mask("abc12"), is("abc12"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc12"), is("abc12"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthLessThanFirstNPlusLastM() {
        assertThat(maskAlgorithm.mask("abc1234"), is("abc1234"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc123456"), is("abc123456"));
    }
    
    @Test
    void assertMaskWhenPlainValueLengthEqualsFirstNPlusLastM() {
        assertThat(maskAlgorithm.mask("abc12345"), is("abc12345"));
        assertThat(sameFirstNLastMMaskAlgorithm.mask("abc1234567"), is("abc1234567"));
    }
    
    @Test
    void assertInitWhenFirstNIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("first-n", ""), new Property("last-m", "5"), new Property("replace-char", "*"))));
    }
    
    @Test
    void assertInitWhenLastMIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", ""), new Property("replace-char", "*"))));
    }
    
    @Test
    void assertInitWhenReplaceCharIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", "5"), new Property("replace-char", ""))));
    }
}
