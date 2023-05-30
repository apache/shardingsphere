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

import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelephoneRandomReplaceAlgorithmTest {
    
    private static final Collection<String> DEFAULT_NETWORK_NUMBERS = Arrays.asList("130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "150", "151", "152", "153", "155", "156",
            "157", "158", "159", "166", "170", "176", "177", "178", "180", "181", "182", "183", "184", "185", "186", "187", "188", "189", "191", "198", "199");
    
    private TelephoneRandomReplaceAlgorithm maskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = new TelephoneRandomReplaceAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("network-numbers", "130, 130, 155,1702")));
    }
    
    @Test
    void assertInitWithEmptyProps() {
        maskAlgorithm.init(new Properties());
        Optional<Object> actual = ReflectionUtils.getFieldValue(maskAlgorithm, "networkNumbers");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(DEFAULT_NETWORK_NUMBERS));
    }
    
    @Test
    void assertMaskWithNullPlaintext() {
        assertNull(maskAlgorithm.mask(null));
    }
    
    @Test
    void assertMask() {
        assertThat(maskAlgorithm.mask(""), is(""));
        assertThat(maskAlgorithm.mask("13012345678"), not("13012345678"));
    }
    
    @Test
    void assertInitWhenConfigNotNumberProps() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> maskAlgorithm.init(PropertiesBuilder.build(new Property("network-numbers", "130, x130, 155,1702"))));
    }
}
