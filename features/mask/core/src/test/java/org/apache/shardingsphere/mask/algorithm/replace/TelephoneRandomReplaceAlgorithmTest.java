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

package org.apache.shardingsphere.mask.algorithm.replace;

import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public final class TelephoneRandomReplaceAlgorithmTest {
    
    private TelephoneRandomReplaceAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new TelephoneRandomReplaceAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("network-numbers", "130, 130, 155,1702")));
    }
    
    @Test
    public void assertMaskWithNullPlaintext() {
        assertNull(maskAlgorithm.mask(null));
    }
    
    @Test
    public void assertMask() {
        assertThat(maskAlgorithm.mask("13012345678").substring(0, 3), is("130"));
        assertThat(maskAlgorithm.mask("13012345678").substring(3, 11), not("12345678"));
        assertThat(maskAlgorithm.mask("13012345678"), not("13012345678"));
    }
    
    @Test(expected = MaskAlgorithmInitializationException.class)
    public void assertInitWhenConfigNotNumberProps() {
        maskAlgorithm.init(PropertiesBuilder.build(new Property("network-numbers", "130, x130, 155,1702")));
    }
}
