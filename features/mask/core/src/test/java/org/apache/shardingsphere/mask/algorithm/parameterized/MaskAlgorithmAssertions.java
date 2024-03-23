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

package org.apache.shardingsphere.mask.algorithm.parameterized;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaskAlgorithmAssertions {
    
    /**
     * Assert init failed with invalid properties.
     * 
     * @param type mask algorithm type
     * @param props mask algorithm props
     */
    public static void assertInitFailedWithInvalidProperties(final String type, final Properties props) {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(MaskAlgorithm.class, type, props));
    }
    
    /**
     * Assert mask.
     *
     * @param type mask algorithm type
     * @param props mask algorithm props
     * @param plainValue plain value
     * @param maskedValue masked value
     */
    @SuppressWarnings("unchecked")
    public static void assertMask(final String type, final Properties props, final Object plainValue, final Object maskedValue) {
        assertThat(TypedSPILoader.getService(MaskAlgorithm.class, type, props).mask(plainValue), is(maskedValue));
    }
}
