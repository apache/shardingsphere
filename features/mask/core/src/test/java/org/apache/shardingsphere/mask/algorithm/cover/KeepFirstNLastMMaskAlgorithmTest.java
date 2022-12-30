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
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class KeepFirstNLastMMaskAlgorithmTest {
    
    private KeepFirstNLastMMaskAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new KeepFirstNLastMMaskAlgorithm();
        maskAlgorithm.init(createProperties("2", "5", "*"));
    }
    
    private Properties createProperties(final String firstN, final String lastM, final String replaceChar) {
        Properties result = new Properties();
        result.setProperty("first-n", firstN);
        result.setProperty("last-m", lastM);
        result.setProperty("replace-char", replaceChar);
        return result;
    }
    
    @Test
    public void assertMask() {
        String actual = maskAlgorithm.mask("abc123456");
        assertThat(actual, is("ab**23456"));
    }
    
    @Test
    public void assertMaskWhenPlainValueLengthLessThenFirstNLastMSum() {
        String actual = maskAlgorithm.mask("abc");
        assertThat(actual, is("abc"));
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertInitWhenConfigWrongProps() {
        KeepFirstNLastMMaskAlgorithm maskAlgorithm = new KeepFirstNLastMMaskAlgorithm();
        maskAlgorithm.init(createProperties("", "3", "+"));
        maskAlgorithm.init(createProperties("2", "", "+"));
        maskAlgorithm.init(createProperties("2", "5", ""));
    }
}
