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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MaskBeforeSpecialCharsAlgorithmTest {
    
    private MaskBeforeSpecialCharsAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new MaskBeforeSpecialCharsAlgorithm();
        maskAlgorithm.init(createProperties("d1"));
    }
    
    private Properties createProperties(final String specialChars) {
        Properties result = new Properties();
        result.setProperty("special-chars", specialChars);
        result.setProperty("replace-char", "*");
        return result;
    }
    
    @Test
    public void assertMask() {
        String actual = maskAlgorithm.mask("abcd134");
        assertThat(actual, is("***d134"));
    }
    
    @Test
    public void assertMaskWhenPlainValueMatchedMultipleSpecialChars() {
        String actual = maskAlgorithm.mask("abcd1234d1234");
        assertThat(actual, is("***d1234d1234"));
    }
    
    @Test
    public void assertMaskEmptyString() {
        String actual = maskAlgorithm.mask("");
        assertThat(actual, is(""));
    }
    
    @Test
    public void assertMaskNull() {
        String actual = maskAlgorithm.mask(null);
        assertThat(actual, is(nullValue()));
    }
    
    @Test
    public void assertMaskWhenPlainValueNotMatchedSpecialChars() {
        String actual = maskAlgorithm.mask("abcd234");
        assertThat(actual, is("abcd234"));
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertInitWhenConfigWrongProps() {
        MaskBeforeSpecialCharsAlgorithm maskAlgorithm = new MaskBeforeSpecialCharsAlgorithm();
        maskAlgorithm.init(createProperties(""));
    }
}
