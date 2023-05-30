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

import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GenericTableRandomReplaceAlgorithmTest {
    
    private GenericTableRandomReplaceAlgorithm maskAlgorithm;
    
    @BeforeEach
    void setUp() {
        this.maskAlgorithm = new GenericTableRandomReplaceAlgorithm();
    }
    
    @Test
    void assertMask() {
        maskAlgorithm.init(PropertiesBuilder.build(new Property("uppercase-letter-codes", "A,B,C,D"), new Property("lowercase-letter-codes", "a,b,c,d"), new Property("digital-codes", "1,2,3,4"),
                new Property("special-codes", "~!@#")));
        assertThat(maskAlgorithm.mask(""), is(""));
        assertThat(maskAlgorithm.mask("Ab1!").charAt(0), anyOf(is('A'), is('B'), is('C'), is('D')));
        assertThat(maskAlgorithm.mask("Ab1!").charAt(1), anyOf(is('a'), is('b'), is('c'), is('d')));
        assertThat(maskAlgorithm.mask("Ab1!").charAt(2), anyOf(is('1'), is('2'), is('3'), is('4')));
        assertThat(maskAlgorithm.mask("Ab1!").charAt(3), anyOf(is('~'), is('!'), is('@'), is('#')));
    }
    
    @Test
    void assertInitWithEmptyProps() {
        maskAlgorithm.init(new Properties());
        assertThat(maskAlgorithm.mask("Ab1!").substring(0, 1), anyOf(Arrays.stream("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")).map(CoreMatchers::is).collect(Collectors.toList())));
        assertThat(maskAlgorithm.mask("Ab1!").substring(1, 2), anyOf(Arrays.stream("abcdefghijklmnopqrstuvwxyz".split("")).map(CoreMatchers::is).collect(Collectors.toList())));
        assertThat(maskAlgorithm.mask("Ab1!").substring(2, 3), anyOf(Arrays.stream("0123456789".split("")).map(CoreMatchers::is).collect(Collectors.toList())));
        assertThat(maskAlgorithm.mask("Ab1!").substring(3, 4), anyOf(Arrays.stream("~!@#$%^&*:<>|".split("")).map(CoreMatchers::is).collect(Collectors.toList())));
    }
}
