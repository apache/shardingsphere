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

package org.apache.shardingsphere.sharding.api.config.validator;

import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingTableRuleConfigurationValidatorTest {
    
    private final ShardingTableRuleConfigurationValidator validator = new ShardingTableRuleConfigurationValidator();
    
    @Mock
    private InlineExpressionParser parser;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validDataNodesArguments")
    void assertValidDataNodes(final String name, final List<String> actualDataNodes) {
        try (MockedStatic<InlineExpressionParserFactory> mockedFactory = mockStatic(InlineExpressionParserFactory.class)) {
            mockedFactory.when(() -> InlineExpressionParserFactory.newInstance("data_nodes")).thenReturn(parser);
            when(parser.splitAndEvaluate()).thenReturn(actualDataNodes);
            assertTrue(validator.isValid(new ShardingTableRuleConfiguration("foo_tbl", "data_nodes"), context));
            verifyNoInteractions(context);
        }
    }
    
    private static Stream<Arguments> validDataNodesArguments() {
        return Stream.of(
                Arguments.of("no data nodes", Collections.emptyList()),
                Arguments.of("omitted schema", Collections.singletonList("ds_0.foo_tbl_0")),
                Arguments.of("same schema for same data source", Arrays.asList("ds_0.foo_schema.foo_tbl_0", "ds_0.foo_schema.foo_tbl_1")),
                Arguments.of("same schema in different cases", Arrays.asList("ds_0.foo_schema.foo_tbl_0", "ds_0.FOO_SCHEMA.foo_tbl_1")),
                Arguments.of("omitted schema before explicit schema", Arrays.asList("ds_0.foo_tbl_0", "ds_0.foo_schema.foo_tbl_1")),
                Arguments.of("omitted schema after explicit schema", Arrays.asList("ds_0.foo_schema.foo_tbl_0", "ds_0.foo_tbl_1")),
                Arguments.of("different schemas for different data sources", Arrays.asList("ds_0.foo_schema.foo_tbl_0", "ds_1.bar_schema.foo_tbl_1")),
                Arguments.of("data source names remain case sensitive", Arrays.asList("ds_0.foo_schema.foo_tbl_0", "DS_0.bar_schema.foo_tbl_1")));
    }
    
    @Test
    void assertInvalidDataNodes() {
        try (MockedStatic<InlineExpressionParserFactory> mockedFactory = mockStatic(InlineExpressionParserFactory.class)) {
            mockedFactory.when(() -> InlineExpressionParserFactory.newInstance("data_nodes")).thenReturn(parser);
            when(parser.splitAndEvaluate()).thenReturn(Arrays.asList("ds_0.foo_schema.foo_tbl_0", "ds_0.bar_schema.foo_tbl_1"));
            assertFalse(validator.isValid(new ShardingTableRuleConfiguration("foo_tbl", "data_nodes"), context));
            verify(context).disableDefaultConstraintViolation();
            verify(context.buildConstraintViolationWithTemplate("contains multiple schemas for storage unit `ds_0` in sharding table `foo_tbl`")
                    .addPropertyNode("actualDataNodes")).addConstraintViolation();
        }
    }
}
