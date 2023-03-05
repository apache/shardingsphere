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
import static org.junit.Assert.assertThrows;

public final class MaskFromXToYMaskAlgorithmTest {
    
    private MaskFromXToYMaskAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new MaskFromXToYMaskAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "5"), new Property("replace-char", "*")));
    }
    
    @Test
    public void assertMask() {
        assertThat(maskAlgorithm.mask("abc12345"), is("abc***45"));
    }
    
    @Test
    public void assertMaskWhenPlainValueLengthLessThanFromX() {
        assertThat(maskAlgorithm.mask("ab"), is("ab"));
    }
    
    @Test
    public void assertMaskWhenPlainValueLengthLessThanToY() {
        assertThat(maskAlgorithm.mask("abc1"), is("abc*"));
    }
    
    @Test
    public void assertInitWhenFromXIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new MaskFromXToYMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", ""), new Property("to-y", "5"), new Property("replace-char", "*"))));
    }
    
    @Test
    public void assertInitWhenToYIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new MaskFromXToYMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", ""), new Property("replace-char", "*"))));
    }
    
    @Test
    public void assertInitWhenReplaceCharIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> new MaskFromXToYMaskAlgorithm().init(PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "5"), new Property("replace-char", ""))));
    }
}
