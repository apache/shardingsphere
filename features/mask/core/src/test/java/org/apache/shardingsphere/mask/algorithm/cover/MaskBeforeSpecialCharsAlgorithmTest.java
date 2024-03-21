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

import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmArgumentsProvider;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmAssertions;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmCaseAssert;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

class MaskBeforeSpecialCharsAlgorithmTest {
    
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
            super("MASK_BEFORE_SPECIAL_CHARS");
        }
        
        @Override
        protected Collection<MaskAlgorithmCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmCaseAssert("empty_special_char", PropertiesBuilder.build(new Property("special-chars", ""))),
                    new MaskAlgorithmCaseAssert("empty_replace_char", PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", ""))));
        }
    }
    
    private static class AlgorithmMaskArgumentsProvider extends MaskAlgorithmArgumentsProvider {
        
        AlgorithmMaskArgumentsProvider() {
            super("MASK_BEFORE_SPECIAL_CHARS");
        }
        
        @Override
        protected Collection<MaskAlgorithmCaseAssert> getCaseAsserts() {
            Properties props = PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", "*"));
            return Arrays.asList(
                    new MaskAlgorithmCaseAssert("null_value", props, null, null),
                    new MaskAlgorithmCaseAssert("empty_string", props, "", ""),
                    new MaskAlgorithmCaseAssert("normal", props, "abcd134", "***d134"),
                    new MaskAlgorithmCaseAssert("match_multiple_special_chars", props, "abcd1234d1234", "***d1234d1234"),
                    new MaskAlgorithmCaseAssert("not_match_special_chars", props, "abcd234", "abcd234"));
        }
    }
}
