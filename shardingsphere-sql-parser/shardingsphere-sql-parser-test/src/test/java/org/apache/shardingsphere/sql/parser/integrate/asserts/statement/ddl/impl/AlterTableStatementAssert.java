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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.definition.ColumnDefinitionAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.definition.ColumnPositionAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Alter table statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterTableStatementAssert {
    
    /**
     * Assert alter table statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual alter table statement
     * @param expected expected alter table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertAddColumnDefinitions(assertContext, actual, expected);
        assertChangeColumnPositions(assertContext, actual, expected);
        assertDropColumns(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        // TODO split old table and new table
        TableAssert.assertIs(assertContext, actual.findSQLSegments(TableSegment.class), expected.getTables());
    }
    
    private static void assertAddColumnDefinitions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Added column definitions size assertion error: "), actual.getAddedColumnDefinitions().size(), is(expected.getAddColumns().size()));
        int count = 0;
        for (ColumnDefinitionSegment each : actual.getAddedColumnDefinitions()) {
            ColumnDefinitionAssert.assertIs(assertContext, each, expected.getAddColumns().get(count));
            count++;
        }
    }
    
    private static void assertChangeColumnPositions(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Changed column positions size assertion error: "), actual.getChangedPositionColumns().size(), is(expected.getPositionChangedColumns().size()));
        int count = 0;
        for (ColumnPositionSegment each : actual.getChangedPositionColumns()) {
            ColumnPositionAssert.assertIs(assertContext, each, expected.getPositionChangedColumns().get(count));
            count++;
        }
    }
    
    private static void assertDropColumns(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final AlterTableStatementTestCase expected) {
        assertThat(assertContext.getText("Drop columns size assertion error: "), actual.getDroppedColumnNames().size(), is(expected.getDropColumns().size()));
        int count = 0;
        for (String each : actual.getDroppedColumnNames()) {
            assertThat(assertContext.getText("Drop column name assertion error: "), each, is(expected.getDropColumns().get(count).getName()));
            // TODO assert column
            count++;
        }
    }
}
