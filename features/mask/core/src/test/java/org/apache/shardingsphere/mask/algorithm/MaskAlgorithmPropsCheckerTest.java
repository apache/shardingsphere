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

import org.apache.shardingsphere.mask.exception.algorithm.MaskAlgorithmInitializationException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MaskAlgorithmPropsCheckerTest {
    
    @Test
    void assertCheckSingleCharConfigWithLengthOne() {
        MaskAlgorithmPropsChecker.checkSingleCharConfig(PropertiesBuilder.build(new Property("singleChar", "1")), "singleChar", "maskType");
    }
    
    @Test
    void assertCheckSingleCharConfigWithEmptyString() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkSingleCharConfig(PropertiesBuilder.build(new Property("singleChar", "")), "singleChar1", "maskType"));
    }
    
    @Test
    void assertCheckSingleCharConfigWithDifferentKey() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkSingleCharConfig(PropertiesBuilder.build(new Property("singleChar", "1")), "singleChar1", "maskType"));
    }
    
    @Test
    void assertCheckSingleCharConfigWithLengthMoreThanOne() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkSingleCharConfig(PropertiesBuilder.build(new Property("singleChar", "123")), "singleChar", "maskType"));
    }
    
    @Test
    void assertCheckSingleCharConfigWithNull() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> MaskAlgorithmPropsChecker.checkSingleCharConfig(PropertiesBuilder.build(), "singleChar", "maskType"));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithLengthOne() {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(PropertiesBuilder.build(new Property("AtLeastOneChar", "1")), "AtLeastOneChar", "maskType");
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithLengthMoreThanOne() {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(PropertiesBuilder.build(new Property("AtLeastOneChar", "1234")), "AtLeastOneChar", "maskType");
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithEmptyString() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(PropertiesBuilder.build(new Property("AtLeastOneChar", "")), "AtLeastOneChar", "maskType"));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithNull() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(PropertiesBuilder.build(), "AtLeastOneChar", "maskType"));
    }
    
    @Test
    void assertCheckAtLeastOneCharConfigWithDifferentKey() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(PropertiesBuilder.build(new Property("singleChar", "123")), "AtLeastOneChar", "maskType"));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithInteger() {
        MaskAlgorithmPropsChecker.checkPositiveIntegerConfig(PropertiesBuilder.build(new Property("integerTypeConfigKey", "123")), "integerTypeConfigKey", "maskType");
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithDifferentKey() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkPositiveIntegerConfig(PropertiesBuilder.build(new Property("integerTypeConfigKey", "123")), "integerTypeConfigKey1", "maskType"));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithNotInteger() {
        assertThrows(MaskAlgorithmInitializationException.class,
                () -> MaskAlgorithmPropsChecker.checkPositiveIntegerConfig(PropertiesBuilder.build(new Property("integerTypeConfigKey", "123abc")), "integerTypeConfigKey", "maskType"));
    }
    
    @Test
    void assertCheckIntegerTypeConfigWithNull() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> MaskAlgorithmPropsChecker.checkPositiveIntegerConfig(PropertiesBuilder.build(), "integerTypeConfigKey", "maskType"));
    }
}
