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

import org.apache.shardingsphere.mask.algorithm.parameterized.MaskAlgorithmArgumentsProvider;
import org.apache.shardingsphere.mask.algorithm.parameterized.MaskAlgorithmAssertions;
import org.apache.shardingsphere.mask.algorithm.parameterized.MaskAlgorithmCaseAssert;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

class KeepFromXToYMaskAlgorithmTest {
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmInitArgumentsProvider.class)
    void assertInit(final String type, @SuppressWarnings("unused") final String name, final Properties props) {
        MaskAlgorithmAssertions.assertInitFailedWithInvalidProperties(type, props);
    }
    
    @ParameterizedTest(name = "{0}: {1}")
    @ArgumentsSource(AlgorithmMaskArgumentsProvider.class)
    void assertMask(final String type, @SuppressWarnings("unused") final String name, final Properties props, final Object plainValue, final Object maskedValue) {
        MaskAlgorithmAssertions.assertMask(type, props, plainValue, maskedValue);
    }
    
    private static class AlgorithmInitArgumentsProvider extends MaskAlgorithmArgumentsProvider {
        
        AlgorithmInitArgumentsProvider() {
            super("KEEP_FROM_X_TO_Y");
        }
        
        @Override
        protected Collection<MaskAlgorithmCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmCaseAssert("from_x_is_empty", PropertiesBuilder.build(new Property("from-x", ""), new Property("to-y", "5"), new Property("replace-char", "*"))),
                    new MaskAlgorithmCaseAssert("to_y_is_empty", PropertiesBuilder.build(new Property("from-x", "2"), new Property("to-y", ""), new Property("replace-char", "*"))),
                    new MaskAlgorithmCaseAssert("replace_char_is_empty", PropertiesBuilder.build(new Property("from-x", "2"), new Property("to-y", "5"), new Property("replace-char", ""))),
                    new MaskAlgorithmCaseAssert("from_x_is_not_positive", PropertiesBuilder.build(new Property("from-x", "-3"), new Property("to-y", "5"), new Property("replace-char", "*"))),
                    new MaskAlgorithmCaseAssert("to_y_is_not_positive", PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "-5"), new Property("replace-char", "*"))),
                    new MaskAlgorithmCaseAssert("from_x_greater_than_to_y", PropertiesBuilder.build(new Property("from-x", "5"), new Property("to-y", "2"), new Property("replace-char", ""))));
        }
    }
    
    private static class AlgorithmMaskArgumentsProvider extends MaskAlgorithmArgumentsProvider {
        
        AlgorithmMaskArgumentsProvider() {
            super("KEEP_FROM_X_TO_Y");
        }
        
        @Override
        protected Collection<MaskAlgorithmCaseAssert> getCaseAsserts() {
            Properties diffProps = PropertiesBuilder.build(new Property("from-x", "3"), new Property("to-y", "5"), new Property("replace-char", "*"));
            Properties sameProps = PropertiesBuilder.build(new Property("from-x", "5"), new Property("to-y", "5"), new Property("replace-char", "*"));
            return Arrays.asList(
                    new MaskAlgorithmCaseAssert("null_value", diffProps, null, null),
                    new MaskAlgorithmCaseAssert("empty_string", diffProps, "", ""),
                    new MaskAlgorithmCaseAssert("normal_with_diff", diffProps, "abc123456", "***123***"),
                    new MaskAlgorithmCaseAssert("normal_with_same", sameProps, "abc123456", "*****3***"),
                    new MaskAlgorithmCaseAssert("plain_value_length_less_than_from_X_plus_one_with_diff", diffProps, "abc", "***"),
                    new MaskAlgorithmCaseAssert("plain_value_length_less_than_from_X_plus_one_with_same", sameProps, "abc", "***"),
                    new MaskAlgorithmCaseAssert("plain_value_length_equals_from_X_plus_one_with_diff", diffProps, "abc1", "***1"),
                    new MaskAlgorithmCaseAssert("plain_value_length_equals_from_X_plus_one_with_same", sameProps, "abc123", "*****3"),
                    new MaskAlgorithmCaseAssert("plain_value_length_equals_from_X_plus_one_with_diff", diffProps, "abc1", "***1"),
                    new MaskAlgorithmCaseAssert("plain_value_length_equals_from_X_plus_one_with_same", sameProps, "abc123", "*****3"),
                    new MaskAlgorithmCaseAssert("plain_value_length_less_than_to_Y_plus_one_with_diff", diffProps, "abc12", "***12"),
                    new MaskAlgorithmCaseAssert("plain_value_length_less_than_to_Y_plus_one_with_same", sameProps, "abc12", "*****"),
                    new MaskAlgorithmCaseAssert("plain_value_length_equals_to_Y_plus_one_with_diff", diffProps, "abc123", "***123"),
                    new MaskAlgorithmCaseAssert("plain_value_length_equals_to_Y_plus_one_with_same", sameProps, "abc123", "*****3"));
        }
    }
}
