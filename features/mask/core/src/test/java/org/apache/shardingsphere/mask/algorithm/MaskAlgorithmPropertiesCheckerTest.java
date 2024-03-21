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
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MaskAlgorithmPropertiesCheckerTest {
    
    @Test
    void assertCheckSingleCharWithLengthOne() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "1"));
        assertDoesNotThrow(() -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "singleChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharWithEmptyString() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", ""));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "singleChar1", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharWithDifferentKey() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "1"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "singleChar1", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharWithLengthMoreThanOne() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "123"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "singleChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharWithNull() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(props, "singleChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharWithLengthOne() {
        Properties props = PropertiesBuilder.build(new Property("AtLeastOneChar", "1"));
        MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "AtLeastOneChar", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckAtLeastOneCharWithLengthMoreThanOne() {
        Properties props = PropertiesBuilder.build(new Property("AtLeastOneChar", "1234"));
        MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "AtLeastOneChar", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckAtLeastOneCharWithEmptyString() {
        Properties props = PropertiesBuilder.build(new Property("AtLeastOneChar", ""));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "AtLeastOneChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharWithNull() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "AtLeastOneChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharWithDifferentKey() {
        Properties props = PropertiesBuilder.build(new Property("singleChar", "123"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(props, "AtLeastOneChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckIntegerTypeWithInteger() {
        Properties props = PropertiesBuilder.build(new Property("integerTypeConfigKey", "123"));
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "integerTypeConfigKey", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckIntegerTypeWithDifferentKey() {
        Properties props = PropertiesBuilder.build(new Property("integerTypeConfigKey", "123"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "integerTypeConfigKey1", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckIntegerTypeWithNotInteger() {
        Properties props = PropertiesBuilder.build(new Property("integerTypeConfigKey", "123abc"));
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "integerTypeConfigKey", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckIntegerTypeWithNull() {
        Properties props = new Properties();
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(props, "integerTypeConfigKey", mock(MaskAlgorithm.class)));
    }
}
