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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.table.ExpectedAlterTable;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.token.ExpectedColumnDefinition;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.token.ExpectedColumnPosition;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterTableAssert {
    
    /**
     * Assert actual alter table statement is correct with expected alter table.
     * 
     * @param assertContext assert context
     * @param actual actual alter table statement
     * @param expected expected alter table
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final ExpectedAlterTable expected) {
        assertThat(assertContext.getText("Drop names assertion error: "), Joiner.on(",").join(actual.getDroppedColumnNames()), is(expected.getDropColumns()));
        assertAddColumns(assertContext, actual, expected.getAddColumns());
        assertColumnPositions(assertContext, actual.getChangedPositionColumns(), expected.getPositionChangedColumns());
    }
    
    private static void assertAddColumns(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final List<ExpectedColumnDefinition> expected) {
        assertThat(assertContext.getText("Add column size error: "), actual.getAddedColumnDefinitions().size(), is(expected.size()));
        int count = 0;
        for (ColumnDefinitionSegment each : actual.getAddedColumnDefinitions()) {
            assertColumnDefinition(assertContext, each, expected.get(count));
            count++;
        }
    }
    
    private static void assertColumnDefinition(final SQLCaseAssertContext assertContext, final ColumnDefinitionSegment actual, final ExpectedColumnDefinition expected) {
        assertThat(assertContext.getText("Column name assertion error: "), actual.getColumnName(), is(expected.getName()));
        assertThat(assertContext.getText("Column " + actual.getColumnName() + " type assertion error: "), actual.getDataType(), is(expected.getType()));
    }
    
    private static void assertColumnPositions(final SQLCaseAssertContext assertContext, final Collection<ColumnPositionSegment> actual, final List<ExpectedColumnPosition> expected) {
        if (null == expected) {
            return;
        }
        assertThat(assertContext.getText("Alter column position size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (ColumnPositionSegment each : actual) {
            assertColumnPosition(assertContext, each, expected.get(count));
            count++;
        }
    }
    
    private static void assertColumnPosition(final SQLCaseAssertContext assertContext, final ColumnPositionSegment actual, final ExpectedColumnPosition expected) {
        assertThat(assertContext.getText("Alter column position name assertion error: "), actual.getColumnName(), is(expected.getColumnName()));
        assertThat(assertContext.getText("Alter column [" + actual.getColumnName() + "]position startIndex assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        if (actual instanceof ColumnAfterPositionSegment) {
            assertThat(assertContext.getText("Alter column [" + actual.getColumnName() + "]position afterColumnName assertion error: "), 
                    ((ColumnAfterPositionSegment) actual).getAfterColumnName(), is(expected.getAfterColumn()));
        }
    }
}
