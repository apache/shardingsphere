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

package org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinSpecificationSegment;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.ExpectedJoinSpecification;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * JoinSpecification assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinSpecificationAssert {
    
    /**
     * Assert actual JoinSpecification segments is correct with expected JoinSpecification.
     *
     * @param assertContext assert context
     * @param actual actual JoinSpecification
     * @param expected expected JoinSpecification
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final JoinSpecificationSegment actual, final ExpectedJoinSpecification expected) {
        assertThat(assertContext.getText("Start index assert error"), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertContext.getText("Stop index assert error"), actual.getStopIndex(), is(expected.getStopIndex()));
        PredicatesAssert.assertIs(assertContext, actual.getAndPredicates(), expected.getAndPredicates());
    }
}
