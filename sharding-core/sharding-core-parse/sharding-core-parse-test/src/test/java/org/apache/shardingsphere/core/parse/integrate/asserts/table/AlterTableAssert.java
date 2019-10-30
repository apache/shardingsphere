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

package org.apache.shardingsphere.core.parse.integrate.asserts.table;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.table.ExpectedAlterTable;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedColumnDefinition;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedColumnPosition;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RequiredArgsConstructor
public final class AlterTableAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert alter table.
     * 
     * @param actual actual alter table statement
     * @param expected expected alter table
     */
    public void assertAlterTable(final AlterTableStatement actual, final ExpectedAlterTable expected) {
        assertThat(assertMessage.getFullAssertMessage("Drop names assertion error: "), Joiner.on(",").join(actual.getDroppedColumnNames()), is(expected.getDropColumns()));
        assertAddColumns(actual, expected.getAddColumns());
        assertColumnPositions(actual.getChangedPositionColumns(), expected.getPositionChangedColumns());
    }
    
    private void assertAddColumns(final AlterTableStatement actual, final List<ExpectedColumnDefinition> expected) {
        assertThat(assertMessage.getFullAssertMessage("Add column size error: "), actual.getAddedColumnDefinitions().size(), is(expected.size()));
        int count = 0;
        for (ColumnDefinitionSegment each : actual.getAddedColumnDefinitions()) {
            assertColumnDefinition(each, expected.get(count));
            count++;
        }
    }
    
    private void assertColumnDefinition(final ColumnDefinitionSegment actual, final ExpectedColumnDefinition expected) {
        assertThat(assertMessage.getFullAssertMessage("Column name assertion error: "), actual.getColumnName(), is(expected.getName()));
        assertThat(assertMessage.getFullAssertMessage("Column " + actual.getColumnName() + " type assertion error: "), actual.getDataType(), is(expected.getType()));
    }
    
    private void assertColumnPositions(final Collection<ColumnPositionSegment> actual, final List<ExpectedColumnPosition> expected) {
        if (null == expected) {
            return;
        }
        assertThat(assertMessage.getFullAssertMessage("Alter column position size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (ColumnPositionSegment each : actual) {
            assertColumnPosition(each, expected.get(count));
            count++;
        }
    }
    
    private void assertColumnPosition(final ColumnPositionSegment actual, final ExpectedColumnPosition expected) {
        assertThat(assertMessage.getFullAssertMessage("Alter column position name assertion error: "), actual.getColumnName(), is(expected.getColumnName()));
        assertThat(assertMessage.getFullAssertMessage("Alter column [" + actual.getColumnName() + "]position startIndex assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        if (actual instanceof ColumnAfterPositionSegment) {
            assertThat(assertMessage.getFullAssertMessage("Alter column [" + actual.getColumnName() + "]position afterColumnName assertion error: "), 
                    ((ColumnAfterPositionSegment) actual).getAfterColumnName(), is(expected.getAfterColumn()));
        }
    }
}
