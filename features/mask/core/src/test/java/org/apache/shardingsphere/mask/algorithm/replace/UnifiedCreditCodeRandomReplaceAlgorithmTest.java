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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnifiedCreditCodeRandomReplaceAlgorithmTest {
    
    private UnifiedCreditCodeRandomReplaceAlgorithm maskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = new UnifiedCreditCodeRandomReplaceAlgorithm();
    }
    
    @Test
    void assertMask() {
        maskAlgorithm.init(PropertiesBuilder.build(new Property("registration-department-codes", "1,2,3,4"), new Property("category-codes", "1,2,3,4"),
                new Property("administrative-division-codes", "100000,200000,300000")));
        assertThat(maskAlgorithm.mask("123456781234567890"), not("123456781234567890"));
        assertThat(maskAlgorithm.mask("123456781234567890").length(), is(18));
    }
    
    @Test
    void assertInitWhenConfigIsNull() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> maskAlgorithm.init(PropertiesBuilder.build(new Property("registration-department-codes", "1,2,3,4"))));
    }
    
    @Test
    void assertInitWhenConfigIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> maskAlgorithm.init(PropertiesBuilder.build()));
    }
    
    @Test
    void assertInitWhenRegistrationDepartmentCodesIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> maskAlgorithm.init(PropertiesBuilder.build(
                new Property("registration-department-codes", ""),
                new Property("category-codes", "1,2,3,4"),
                new Property("administrative-division-codes", "100000,200000,300000"))));
    }
    
    @Test
    void assertInitWhenCategoryCodesIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> maskAlgorithm.init(PropertiesBuilder.build(
                new Property("registration-department-codes", "1,2,3,4"),
                new Property("category-codes", ""),
                new Property("administrative-division-codes", "100000,200000,300000"))));
    }
    
    @Test
    void assertInitWhenAdministrativeDivisionCodesIsEmpty() {
        assertThrows(MaskAlgorithmInitializationException.class, () -> maskAlgorithm.init(PropertiesBuilder.build(
                new Property("registration-department-codes", "1,2,3,4"),
                new Property("category-codes", "1,2,3,4"),
                new Property("administrative-division-codes", ""))));
    }
}
