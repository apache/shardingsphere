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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.owner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.value.IdentifierValueAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedOwner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Owner assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OwnerAssert {
    
    /**
     * Assert actual owner segment is correct with expected owner.
     *
     * @param assertContext assert context
     * @param actual actual owner segment
     * @param expected expected owner
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OwnerSegment actual, final ExpectedOwner expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getIdentifier(), expected, "Owner");
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        if (null == expected.getOwner()) {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getOwner().isPresent());
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
    }
}
