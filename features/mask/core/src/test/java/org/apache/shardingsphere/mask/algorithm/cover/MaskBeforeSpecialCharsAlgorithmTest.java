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

class MaskBeforeSpecialCharsAlgorithmTest {
    
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
    
    private static final class AlgorithmInitArgumentsProvider extends MaskAlgorithmInitArgumentsProvider {
        
        AlgorithmInitArgumentsProvider() {
            super("MASK_BEFORE_SPECIAL_CHARS");
        }
        
        @Override
        protected Collection<MaskAlgorithmInitCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmInitCaseAssert("empty_special_char", PropertiesBuilder.build(new Property("special-chars", ""))),
                    new MaskAlgorithmInitCaseAssert("empty_replace_char", PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", ""))));
        }
    }
    
    private static final class AlgorithmMaskExecuteArgumentsProvider extends MaskAlgorithmExecuteArgumentsProvider {
        
        AlgorithmMaskExecuteArgumentsProvider() {
            super("MASK_BEFORE_SPECIAL_CHARS", PropertiesBuilder.build(new Property("special-chars", "d1"), new Property("replace-char", "*")));
        }
        
        @Override
        protected Collection<MaskAlgorithmExecuteCaseAssert> getCaseAsserts() {
            return Arrays.asList(
                    new MaskAlgorithmExecuteCaseAssert("null_value", null, null),
                    new MaskAlgorithmExecuteCaseAssert("empty_string", "", ""),
                    new MaskAlgorithmExecuteCaseAssert("normal", "abcd134", "***d134"),
                    new MaskAlgorithmExecuteCaseAssert("match_multiple_special_chars", "abcd1234d1234", "***d1234d1234"),
                    new MaskAlgorithmExecuteCaseAssert("not_match_special_chars", "abcd234", "abcd234"));
        }
    }
}
