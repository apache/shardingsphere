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

package org.apache.shardingsphere.mask.algorithm.hash;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mask.algorithm.parameterized.MaskAlgorithmAssertions;
import org.apache.shardingsphere.mask.algorithm.parameterized.execute.MaskAlgorithmExecuteArgumentsProvider;
import org.apache.shardingsphere.mask.algorithm.parameterized.execute.MaskAlgorithmExecuteCaseAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

class MD5MaskAlgorithmTest {
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmMaskExecuteArgumentsProvider.class)
    void assertMask(final String type, @SuppressWarnings("unused") final String name, final Properties props, final Object plainValue, final Object maskedValue) {
        MaskAlgorithmAssertions.assertMask(type, props, plainValue, maskedValue);
    }
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmMaskExecuteWithSaltArgumentsProvider.class)
    void assertMaskWithSalt(final String type, @SuppressWarnings("unused") final String name, final Properties props, final Object plainValue, final Object maskedValue) {
        MaskAlgorithmAssertions.assertMask(type, props, plainValue, maskedValue);
    }
    
    private static final class AlgorithmMaskExecuteArgumentsProvider extends MaskAlgorithmExecuteArgumentsProvider {
        
        AlgorithmMaskExecuteArgumentsProvider() {
            super("MD5", new Properties());
        }
        
        @Override
        protected Collection<MaskAlgorithmExecuteCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmExecuteCaseAssert("null_value", null, null),
                    new MaskAlgorithmExecuteCaseAssert("without_salt", "abc123456", "0659c7992e268962384eb17fafe88364"));
        }
    }
    
    private static final class AlgorithmMaskExecuteWithSaltArgumentsProvider extends MaskAlgorithmExecuteArgumentsProvider {
        
        AlgorithmMaskExecuteWithSaltArgumentsProvider() {
            super("MD5", PropertiesBuilder.build(new Property("salt", "202cb962ac5907")));
        }
        
        @Override
        protected Collection<MaskAlgorithmExecuteCaseAssert> getCaseAsserts() {
            return Collections.singleton(new MaskAlgorithmExecuteCaseAssert("with_salt", "abc123456", "02d44390e9354b72dd2aa78d55016f7f"));
        }
    }
}
