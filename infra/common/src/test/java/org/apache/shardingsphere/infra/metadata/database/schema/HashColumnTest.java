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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HashColumnTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("equalsArguments")
    void assertEqualsWithComparisonValue(final String name,
                                         final boolean compareHashColumn, final String compareName, final String compareTableName, final boolean compareCaseSensitive, final boolean expectedMatched) {
        assertThat(new HashColumn("col", "tbl", false).equals(compareHashColumn ? new HashColumn(compareName, compareTableName, compareCaseSensitive) : new Object()), is(expectedMatched));
    }
    
    @Test
    void assertHashCode() {
        assertThat(new HashColumn("col", "tbl", false).hashCode(), is(Objects.hash("COL", "TBL")));
    }
    
    private static Stream<Arguments> equalsArguments() {
        return Stream.of(
                Arguments.of("non-hash-column", false, "", "", false, false),
                Arguments.of("different-name", true, "col1", "tbl", false, false),
                Arguments.of("different-table", true, "col", "tbl1", false, false),
                Arguments.of("different-case-sensitive", true, "COL", "TBL", true, false),
                Arguments.of("same-value-ignoring-case", true, "COL", "TBL", false, true));
    }
}
