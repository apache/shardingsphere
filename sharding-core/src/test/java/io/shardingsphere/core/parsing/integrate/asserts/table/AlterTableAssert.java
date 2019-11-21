/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.integrate.asserts.table;

import com.google.common.base.Joiner;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnAfterPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.table.ExpectedAlterTable;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnDefinition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnPosition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedUpdateColumnDefinition;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
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
        assertSame(assertMessage.getFullAssertMessage("Drop primary key assertion error: "), actual.isDropPrimaryKey(), expected.isDropPrimaryKey());
        assertThat(assertMessage.getFullAssertMessage("Rename new table name assertion error: "), actual.getNewTableName().orNull(), is(expected.getNewTableName()));
        assertAddColumns(actual, expected.getAddColumns());
        assertUpdateColumns(actual, expected.getUpdateColumns());
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
    
    private void assertUpdateColumns(final AlterTableStatement actual, final List<ExpectedUpdateColumnDefinition> expected) {
        assertThat(assertMessage.getFullAssertMessage("Update column size error: "), actual.getModifiedColumnDefinitions().size(), is(expected.size()));
        int count = 0;
        for (Entry<String, ColumnDefinitionSegment> each : actual.getModifiedColumnDefinitions().entrySet()) {
            assertUpdateColumnDefinition(each, expected.get(count));
            count++;
        }
    }
    
    private void assertUpdateColumnDefinition(final Entry<String, ColumnDefinitionSegment> actual, final ExpectedUpdateColumnDefinition expected) {
        assertThat(assertMessage.getFullAssertMessage("Origin column name assertion error: "), actual.getKey(), is(expected.getOriginColumnName()));
        assertColumnDefinition(actual.getValue(), expected);
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
