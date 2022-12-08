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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.database.ExpectedDatabase;

/**
 * Database assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAssert {
    
    /**
     * Assert actual database segment is correct with expected database.
     *
     * @param assertContext assert context
     * @param actual actual database segment
     * @param expected expected database
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DatabaseSegment actual, final ExpectedDatabase expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getIdentifier(), expected, "Database");
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
