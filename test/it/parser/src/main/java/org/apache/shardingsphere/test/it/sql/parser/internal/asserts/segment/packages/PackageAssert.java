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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.packages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.packages.ExpectedPackage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Package assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PackageAssert {
    
    /**
     * Assert actual package segment is correct with expected package.
     *
     * @param assertContext assert context
     * @param actual actual package segment
     * @param expected expected package
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final PackageSegment actual, final ExpectedPackage expected) {
        assertNotNull(expected, assertContext.getText("Package should exist."));
        IdentifierValueAssert.assertIs(assertContext, actual.getIdentifier(), expected, "Package");
        if (null == expected.getOwner()) {
            assertFalse(actual.getOwner().isPresent(), assertContext.getText("Actual owner should not exist."));
        } else {
            assertTrue(actual.getOwner().isPresent(), assertContext.getText("Actual owner should exist."));
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
