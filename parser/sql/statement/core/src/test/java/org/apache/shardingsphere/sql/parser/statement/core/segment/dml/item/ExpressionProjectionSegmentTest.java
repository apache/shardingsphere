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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item;

import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExpressionProjectionSegmentTest {
    
    @ParameterizedTest
    @MethodSource("columnLabels")
    void assertColumnLabel(final IdentifierValue defaultLabel, final AliasSegment alias, final String expected) {
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(7, 16, "expression");
        projection.setDefaultColumnLabel(defaultLabel);
        projection.setAlias(alias);
        assertThat(projection.getColumnLabel(), is(expected));
    }
    
    private static Stream<Arguments> columnLabels() {
        return Stream.of(
                Arguments.of(null, null, "expression"),
                Arguments.of(new IdentifierValue("column1"), null, "column1"),
                Arguments.of(new IdentifierValue("column1"), new AliasSegment(20, 24, new IdentifierValue("alias")), "alias"));
    }
    
    @Test
    void assertDefaultLabelDoesNotCreateSourceAlias() {
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(7, 8, "42");
        projection.setDefaultColumnLabel(new IdentifierValue("column1"));
        assertFalse(projection.getAlias().isPresent());
        assertFalse(projection.getAliasName().isPresent());
        assertFalse(projection.getAliasSegment().isPresent());
        assertThat(projection.getStartIndex(), is(7));
        assertThat(projection.getStopIndex(), is(8));
    }
}
