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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.returning;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedReturningClause;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Returning clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReturningClauseAssert {
    
    /**
     * Assert actual returning segment is correct with expected returning clause.
     *
     * @param assertContext assert context
     * @param actual actual returning segment
     * @param expected expected returning clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ReturningSegment actual, final ExpectedReturningClause expected) {
        assertAlias(actual.getOldAlias(), expected.getOldAlias(), expected.getOldAliasStartIndex(), expected.getOldAliasStopIndex());
        assertAlias(actual.getNewAlias(), expected.getNewAlias(), expected.getNewAliasStartIndex(), expected.getNewAliasStopIndex());
        ProjectionsSegment actualProjections = actual.getProjections();
        if (null != actualProjections.getProjections() || expected.getProjections().getSize() > 0) {
            ProjectionAssert.assertIs(assertContext, actualProjections, expected.getProjections());
        }
    }
    
    private static void assertAlias(final AliasSegment actual, final String expectedName, final Integer expectedStartIndex, final Integer expectedStopIndex) {
        if (null == expectedName) {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertThat(actual.getIdentifier().getValue(), is(expectedName));
        assertThat(actual.getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getStopIndex(), is(expectedStopIndex));
    }
}
