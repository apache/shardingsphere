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
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class KeepFirstNLastMMaskAlgorithmTest {
    
    private KeepFirstNLastMMaskAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new KeepFirstNLastMMaskAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", "5"), new Property("replace-char", "*")));
    }
    
    @Test
    public void assertMask() {
        assertThat(maskAlgorithm.mask("abc123456"), is("ab**23456"));
    }
    
    @Test
    public void assertMaskWhenPlainValueLengthLessThenFirstNLastMSum() {
        assertThat(maskAlgorithm.mask("abc"), is("abc"));
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertInitWhenFirstNIsEmpty() {
        new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("first-n", ""), new Property("last-m", "5"), new Property("replace-char", "*")));
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertInitWhenLastMIsEmpty() {
        new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", ""), new Property("replace-char", "*")));
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertInitWhenReplaceCharIsEmpty() {
        new KeepFirstNLastMMaskAlgorithm().init(PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", "5"), new Property("replace-char", "")));
    }
}
