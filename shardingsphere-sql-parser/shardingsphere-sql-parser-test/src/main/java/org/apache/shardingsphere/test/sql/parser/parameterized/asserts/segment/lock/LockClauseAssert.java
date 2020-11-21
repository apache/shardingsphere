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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.lock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.lock.ExpectedLockClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTable;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Lock clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockClauseAssert {

    /**
     * Assert lock segment index and forTables.
     * @param assertContext assert context
     * @param actual actual lock
     * @param expected expected lock
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final LockSegment actual, final ExpectedLockClause expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        List<SimpleTableSegment> actualTables = actual.getTables();
        List<ExpectedSimpleTable> expectedTables = expected.getTables();
        if (actualTables.isEmpty()) {
            assertTrue(assertContext.getText("lock tables should not exist."), expectedTables.isEmpty());
            return;
        }
        TableAssert.assertIs(assertContext, actualTables, expectedTables);
    }
}
