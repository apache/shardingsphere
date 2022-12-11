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

package org.apache.shardingsphere.mask.algorithm;

import org.apache.shardingsphere.mask.algorithm.cover.KeepFirstNLastMMaskAlgorithm;
import org.apache.shardingsphere.mask.algorithm.cover.KeepFromXToYMaskAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * KEEP_FROM_X_TO_Y test.
 */
public final class KeepFromXToYMaskAlgorithmTest {
    
    private KeepFromXToYMaskAlgorithm keepFromXToYMaskAlgorithm;
    
    @Before
    public void setUp() {
        keepFromXToYMaskAlgorithm = new KeepFromXToYMaskAlgorithm();
        keepFromXToYMaskAlgorithm.init(initProperties());
    }
    
    private Properties initProperties() {
        Properties properties = new Properties();
        properties.setProperty("x", "2");
        properties.setProperty("y", "5");
        properties.setProperty("replace-char", "*");
        return properties;
    }
    
    @Test
    public void testMask() {
        String actual = keepFromXToYMaskAlgorithm.mask("abc123456");
        assertThat(actual, is("**c123***"));
    }
    
    @Test
    public void testMaskIfPlainValueIsLess() {
        String actual = keepFromXToYMaskAlgorithm.mask("abc");
        assertThat(actual, is("**c"));
    }
    
    @Test
    public void testMaskIfPlainValueIsOne() {
        String actual = keepFromXToYMaskAlgorithm.mask("a");
        assertThat(actual, is("a"));
    }
    
    @Test
    public void testNotSetStartIndex() {
        KeepFirstNLastMMaskAlgorithm keepFirstNLastMMaskAlgorithm1 = new KeepFirstNLastMMaskAlgorithm();
        Properties wrongProperties = new Properties();
        wrongProperties.setProperty("m", "5");
        wrongProperties.setProperty("replace-char", "*");
        assertThrows(IllegalArgumentException.class, () -> {
            keepFirstNLastMMaskAlgorithm1.init(wrongProperties);
        });
    }
}
