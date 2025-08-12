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

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MaskAlgorithmPropertiesCheckerTest {
    
    @Test
    void assertCheckSingleCharSuccess() {
        Properties props = PropertiesBuilder.build(new Property("key", "1"));
        assertDoesNotThrow(() -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharFailedWithoutKey() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharFailedWithMultipleChars() {
        Properties props = PropertiesBuilder.build(new Property("key", "111"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharSuccess() {
        Properties props = PropertiesBuilder.build(new Property("key", "1"));
        assertDoesNotThrow(() -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharFailedWithoutKey() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharFailedWithEmptyChar() {
        Properties props = PropertiesBuilder.build(new Property("key", ""));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckPositiveIntegerSuccess() {
        Properties props = PropertiesBuilder.build(new Property("key", "123"));
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "key", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckPositiveIntegerFailedWithoutKey() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckPositiveIntegerFailedWithZero() {
        Properties props = PropertiesBuilder.build(new Property("key", "0"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "key", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckPositiveIntegerFailedWithNotInteger() {
        Properties props = PropertiesBuilder.build(new Property("key", "123.0"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "key", mock(MaskAlgorithm.class)));
    }
}
