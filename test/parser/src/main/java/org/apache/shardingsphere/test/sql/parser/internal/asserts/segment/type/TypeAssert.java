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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.type.ExpectedType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Type assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeAssert {
    
    /**
     * Assert actual type segment is correct with expected type.
     *
     * @param assertContext assert context
     * @param actual actual type segment
     * @param expected expected type
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TypeSegment actual, final ExpectedType expected) {
        assertNotNull(assertContext.getText("Type should exist."), expected);
        IdentifierValueAssert.assertIs(assertContext, actual.getIdentifier(), expected, "Type");
        if (null == expected.getOwner()) {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getOwner().isPresent());
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
