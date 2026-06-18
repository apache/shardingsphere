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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.RenameTableStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Rename table statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenameTableStatementAssert {
    
    /**
     * Assert rename table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual rename table statement
     * @param expected expected rename table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RenameTableStatement actual, final RenameTableStatementTestCase expected) {
        if (null == expected.getRenames()) {
            assertNull(actual.getRenameTables(), assertContext.getText("Actual rename table segment should exist."));
        } else {
            assertNotNull(actual.getRenameTables(), assertContext.getText("Actual rename table segment should exist."));
            int count = 0;
            for (RenameTableDefinitionSegment each : actual.getRenameTables()) {
                SQLSegmentAssert.assertIs(assertContext, each, expected.getRenames().get(count));
                TableAssert.assertIs(assertContext, each.getTable(), expected.getRenames().get(count).getTable());
                TableAssert.assertIs(assertContext, each.getRenameTable(), expected.getRenames().get(count).getRenameTable());
                count++;
            }
        }
    }
}
