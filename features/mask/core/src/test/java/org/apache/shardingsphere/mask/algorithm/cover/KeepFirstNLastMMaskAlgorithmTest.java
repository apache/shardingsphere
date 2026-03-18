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

package org.apache.shardingsphere.mask.algorithm.cover;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mask.algorithm.parameterized.MaskAlgorithmAssertions;
import org.apache.shardingsphere.mask.algorithm.parameterized.execute.MaskAlgorithmExecuteArgumentsProvider;
import org.apache.shardingsphere.mask.algorithm.parameterized.execute.MaskAlgorithmExecuteCaseAssert;
import org.apache.shardingsphere.mask.algorithm.parameterized.init.MaskAlgorithmInitArgumentsProvider;
import org.apache.shardingsphere.mask.algorithm.parameterized.init.MaskAlgorithmInitCaseAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

class KeepFirstNLastMMaskAlgorithmTest {
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmInitArgumentsProvider.class)
    void assertInit(final String type, @SuppressWarnings("unused") final String name, final Properties props) {
        MaskAlgorithmAssertions.assertInitFailedWithInvalidProperties(type, props);
    }
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmMaskExecuteArgumentsProvider.class)
    void assertMask(final String type, @SuppressWarnings("unused") final String name, final Properties props, final Object plainValue, final Object maskedValue) {
        MaskAlgorithmAssertions.assertMask(type, props, plainValue, maskedValue);
    }
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmMaskExecuteWithSameNMArgumentsProvider.class)
    void assertMaskWithSameNM(final String type, @SuppressWarnings("unused") final String name, final Properties props, final Object plainValue, final Object maskedValue) {
        MaskAlgorithmAssertions.assertMask(type, props, plainValue, maskedValue);
    }
    
    private static final class AlgorithmInitArgumentsProvider extends MaskAlgorithmInitArgumentsProvider {
        
        AlgorithmInitArgumentsProvider() {
            super("KEEP_FIRST_N_LAST_M");
        }
        
        @Override
        protected Collection<MaskAlgorithmInitCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmInitCaseAssert("empty_first_N", PropertiesBuilder.build(new Property("first-n", ""), new Property("last-m", "5"), new Property("replace-char", "*"))),
                    new MaskAlgorithmInitCaseAssert("empty_last_N", PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", ""), new Property("replace-char", "*"))),
                    new MaskAlgorithmInitCaseAssert("empty_replace_char", PropertiesBuilder.build(new Property("first-n", "2"), new Property("last-m", "5"), new Property("replace-char", ""))));
        }
    }
    
    private static final class AlgorithmMaskExecuteArgumentsProvider extends MaskAlgorithmExecuteArgumentsProvider {
        
        AlgorithmMaskExecuteArgumentsProvider() {
            super("KEEP_FIRST_N_LAST_M", PropertiesBuilder.build(new Property("first-n", "3"), new Property("last-m", "5"), new Property("replace-char", "*")));
        }
        
        @Override
        protected Collection<MaskAlgorithmExecuteCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmExecuteCaseAssert("null_value", null, null),
                    new MaskAlgorithmExecuteCaseAssert("empty_string", "", ""),
                    new MaskAlgorithmExecuteCaseAssert("normal", "abc123456", "abc*23456"),
                    new MaskAlgorithmExecuteCaseAssert("length_less_than_first_n", "ab", "ab"),
                    new MaskAlgorithmExecuteCaseAssert("length_equals_first_n", "abc", "abc"),
                    new MaskAlgorithmExecuteCaseAssert("length_less_than_last_m", "abc1", "abc1"),
                    new MaskAlgorithmExecuteCaseAssert("length_equals_last_m", "abc12", "abc12"),
                    new MaskAlgorithmExecuteCaseAssert("length_less_than_first_n_plus_last_m", "abc1234", "abc1234"),
                    new MaskAlgorithmExecuteCaseAssert("length_equals_first_n_plus_last_m", "abc12345", "abc12345"));
        }
    }
    
    private static final class AlgorithmMaskExecuteWithSameNMArgumentsProvider extends MaskAlgorithmExecuteArgumentsProvider {
        
        AlgorithmMaskExecuteWithSameNMArgumentsProvider() {
            super("KEEP_FIRST_N_LAST_M", PropertiesBuilder.build(new Property("first-n", "5"), new Property("last-m", "5"), new Property("replace-char", "*")));
        }
        
        @Override
        protected Collection<MaskAlgorithmExecuteCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmExecuteCaseAssert("normal", "abc123456789", "abc12**56789"),
                    new MaskAlgorithmExecuteCaseAssert("length_less_than_first_n", "abc", "abc"),
                    new MaskAlgorithmExecuteCaseAssert("length_equals_first_n", "abc12", "abc12"),
                    new MaskAlgorithmExecuteCaseAssert("length_less_than_last_m", "abc1", "abc1"),
                    new MaskAlgorithmExecuteCaseAssert("length_equals_last_m", "abc12", "abc12"),
                    new MaskAlgorithmExecuteCaseAssert("length_less_than_first_n_plus_last_m", "abc123456", "abc123456"),
                    new MaskAlgorithmExecuteCaseAssert("length_equals_first_n_plus_last_m", "abc1234567", "abc1234567"));
        }
    }
}
