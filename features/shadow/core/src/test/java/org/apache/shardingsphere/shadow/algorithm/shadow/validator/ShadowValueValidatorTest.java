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

package org.apache.shardingsphere.shadow.algorithm.shadow.validator;

import org.apache.shardingsphere.shadow.exception.data.UnsupportedShadowColumnTypeException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ShadowValueValidatorTest {
    
    @Test
    void assertValidateDateType() {
        assertThrows(UnsupportedShadowColumnTypeException.class, () -> ShadowValueValidator.validate("tbl", "col", new Date()));
    }
    
    @Test
    void assertValidateEnumType() {
        assertThrows(UnsupportedShadowColumnTypeException.class, () -> ShadowValueValidator.validate("tbl", "col", mock(Enum.class)));
    }
    
    @Test
    void assertValidateAcceptedType() {
        ShadowValueValidator.validate("tbl", "col", "");
    }
}
