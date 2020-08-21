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

package org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.column;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.column.ExpectedColumn;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Column assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnAssert {
    
    /**
     * Assert actual column segment is correct with expected column.
     *
     * @param assertContext assert context
     * @param actual actual column segment
     * @param expected expected column
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ColumnSegment actual, final ExpectedColumn expected) {
        assertThat(assertContext.getText("Column name assertion error: "), actual.getIdentifier().getValue(), is(expected.getName()));
        if (null != expected.getOwner()) {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getOwner().isPresent());
            // TODO OwnerAssert is needed.
            OwnerSegment owner = actual.getOwner().get();
            TableAssert.assertOwner(assertContext, new SimpleTableSegment(owner.getStartIndex(), owner.getStopIndex(), owner.getIdentifier()), expected.getOwner());
        } else {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        }
        assertThat(assertContext.getText("Column start delimiter assertion error: "), actual.getIdentifier().getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Column end delimiter assertion error: "), actual.getIdentifier().getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
