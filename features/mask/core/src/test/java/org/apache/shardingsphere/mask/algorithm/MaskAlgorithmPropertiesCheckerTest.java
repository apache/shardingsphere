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

class MaskAlgorithmPropertiesCheckerTest {
    
    @Test
    void assertCheckSingleCharConfigWithLengthOne() {
        MaskAlgorithmPropertiesChecker.checkSingleChar(PropertiesBuilder.build(new Property("singleChar", "1")), "singleChar", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckSingleCharConfigWithEmptyString() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkSingleChar(PropertiesBuilder.build(new Property("singleChar", "")), "singleChar1", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharConfigWithDifferentKey() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkSingleChar(PropertiesBuilder.build(new Property("singleChar", "1")), "singleChar1", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharConfigWithLengthMoreThanOne() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkSingleChar(PropertiesBuilder.build(new Property("singleChar", "123")), "singleChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckSingleCharConfigWithNull() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkSingleChar(PropertiesBuilder.build(), "singleChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithLengthOne() {
        MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(PropertiesBuilder.build(new Property("AtLeastOneChar", "1")), "AtLeastOneChar", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithLengthMoreThanOne() {
        MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(PropertiesBuilder.build(new Property("AtLeastOneChar", "1234")), "AtLeastOneChar", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithEmptyString() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(PropertiesBuilder.build(new Property("AtLeastOneChar", "")), "AtLeastOneChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithNull() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(PropertiesBuilder.build(), "AtLeastOneChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithDifferentKey() {
        assertThrows(AlgorithmInitializationException.class,
                () -> MaskAlgorithmPropertiesChecker.checkAtLeastOneChar(PropertiesBuilder.build(new Property("singleChar", "123")), "AtLeastOneChar", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithInteger() {
        MaskAlgorithmPropertiesChecker.checkPositiveInteger(PropertiesBuilder.build(new Property("integerTypeConfigKey", "123")), "integerTypeConfigKey", mock(MaskAlgorithm.class));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithDifferentKey() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(
                PropertiesBuilder.build(new Property("integerTypeConfigKey", "123")), "integerTypeConfigKey1", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithNotInteger() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(
                PropertiesBuilder.build(new Property("integerTypeConfigKey", "123abc")), "integerTypeConfigKey", mock(MaskAlgorithm.class)));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithNull() {
        assertThrows(AlgorithmInitializationException.class, () -> MaskAlgorithmPropertiesChecker.checkPositiveInteger(PropertiesBuilder.build(), "integerTypeConfigKey", mock(MaskAlgorithm.class)));
    }
}
