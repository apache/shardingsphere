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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskAlgorithmPropertiesCheckerTest {
    
    @Test
    void assertCheckSingleCharConfigWithLengthOne() {
        MaskAlgorithmPropertiesChecker.checkSingleCharConfiguration(PropertiesBuilder.build(new Property("singleChar", "1")), "singleChar", mockMaskAlgorithm());
    }
    
    @Test
    void assertCheckSingleCharConfigWithEmptyString() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkSingleCharConfiguration(PropertiesBuilder.build(new Property("singleChar", "")), "singleChar1", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckSingleCharConfigWithDifferentKey() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkSingleCharConfiguration(PropertiesBuilder.build(new Property("singleChar", "1")), "singleChar1", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckSingleCharConfigWithLengthMoreThanOne() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkSingleCharConfiguration(PropertiesBuilder.build(new Property("singleChar", "123")), "singleChar", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckSingleCharConfigWithNull() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleCharConfiguration(PropertiesBuilder.build(), "singleChar", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithLengthOne() {
        MaskAlgorithmPropertiesChecker.checkAtLeastOneCharConfiguration(PropertiesBuilder.build(new Property("AtLeastOneChar", "1")), "AtLeastOneChar", mockMaskAlgorithm());
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithLengthMoreThanOne() {
        MaskAlgorithmPropertiesChecker.checkAtLeastOneCharConfiguration(PropertiesBuilder.build(new Property("AtLeastOneChar", "1234")), "AtLeastOneChar", mockMaskAlgorithm());
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithEmptyString() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneCharConfiguration(PropertiesBuilder.build(new Property("AtLeastOneChar", "")), "AtLeastOneChar", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithNull() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneCharConfiguration(PropertiesBuilder.build(), "AtLeastOneChar", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithDifferentKey() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneCharConfiguration(PropertiesBuilder.build(new Property("singleChar", "123")), "AtLeastOneChar", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithInteger() {
        MaskAlgorithmPropertiesChecker.checkPositiveIntegerConfiguration(PropertiesBuilder.build(new Property("integerTypeConfigKey", "123")), "integerTypeConfigKey", mockMaskAlgorithm());
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithDifferentKey() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveIntegerConfiguration(
                PropertiesBuilder.build(new Property("integerTypeConfigKey", "123")), "integerTypeConfigKey1", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithNotInteger() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveIntegerConfiguration(
                PropertiesBuilder.build(new Property("integerTypeConfigKey", "123abc")), "integerTypeConfigKey", mockMaskAlgorithm()));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithNull() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveIntegerConfiguration(
                PropertiesBuilder.build(), "integerTypeConfigKey", mockMaskAlgorithm()));
    }
    
    private MaskAlgorithm<?, ?> mockMaskAlgorithm() {
        MaskAlgorithm<?, ?> result = mock(MaskAlgorithm.class);
        when(result.getType()).thenReturn("maskType");
        return result;
    }
}
