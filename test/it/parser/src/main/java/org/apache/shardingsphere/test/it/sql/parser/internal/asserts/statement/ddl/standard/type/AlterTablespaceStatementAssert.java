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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.tablespace.AlterTablespaceStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Alter tablespace statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterTablespaceStatementAssert {
    
    /**
     * Assert alter tablespace statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter tablespace statement
     * @param expected expected alter tablespace statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterTablespaceStatement actual, final AlterTablespaceStatementTestCase expected) {
        assertTablespace(assertContext, actual, expected);
        assertRenameTablespace(assertContext, actual, expected);
    }
    
    private static void assertTablespace(final SQLCaseAssertContext assertContext, final AlterTablespaceStatement actual, final AlterTablespaceStatementTestCase expected) {
        if (null == expected.getTablespace()) {
            assertNull(actual.getTablespaceSegment(), assertContext.getText("Actual tablespace should not exist."));
        } else {
            assertNotNull(actual.getTablespaceSegment(), assertContext.getText("Actual tablespace should exist."));
            IdentifierValueAssert.assertIs(assertContext, actual.getTablespaceSegment().getIdentifier(), expected.getTablespace(), "Tablespace");
            SQLSegmentAssert.assertIs(assertContext, actual.getTablespaceSegment(), expected.getTablespace());
        }
    }
    
    private static void assertRenameTablespace(final SQLCaseAssertContext assertContext, final AlterTablespaceStatement actual, final AlterTablespaceStatementTestCase expected) {
        if (null == expected.getRenameTablespace()) {
            assertNull(actual.getRenameTablespaceSegment(), assertContext.getText("Actual rename tablespace should not exist."));
        } else {
            assertNotNull(actual.getRenameTablespaceSegment(), assertContext.getText("Actual rename tablespace should exist."));
            IdentifierValueAssert.assertIs(assertContext, actual.getRenameTablespaceSegment().getIdentifier(), expected.getRenameTablespace(), "Tablespace");
            SQLSegmentAssert.assertIs(assertContext, actual.getRenameTablespaceSegment(), expected.getRenameTablespace());
        }
    }
}
