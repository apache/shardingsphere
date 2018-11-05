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
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.meta.ExpectedTableMetaData;
import io.shardingsphere.core.parsing.integrate.jaxb.table.ExpectedAlterTable;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnDefinition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnPosition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedUpdateColumnDefinition;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
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
        assertThat(assertMessage.getFullAssertMessage("Drop names assertion error: "), Joiner.on(",").join(actual.getDropColumns()), is(expected.getDropColumns()));
        assertSame(assertMessage.getFullAssertMessage("Drop primary key assertion error: "), actual.isDropPrimaryKey(), expected.isDropPrimaryKey());
        
        assertThat(assertMessage.getFullAssertMessage("Rename new table name assertion error: "), actual.getNewTableName(), is(expected.getNewTableName()));
        assertAddColumns(actual, expected.getAddColumns()); 
        
        assertUpdateColumns(actual, expected.getUpdateColumns()); 
        
        if (null != expected.getNewMeta()) {
            assertNewMeta(actual.getTableMetaData(), expected.getNewMeta());
        }
        
        if (actual instanceof MySQLAlterTableStatement) {
            MySQLAlterTableStatement mysqlAlter = (MySQLAlterTableStatement) actual;
            assertColumnPositions(mysqlAlter.getPositionChangedColumns(), expected.getPositionChangedColumns());
        }
            
    }
    
    private void assertAddColumns(final AlterTableStatement actual, final List<ExpectedColumnDefinition> expected) {
        assertThat(assertMessage.getFullAssertMessage("Add column size error: "), actual.getAddColumns().size(), is(expected.size()));
        int count = 0;
        for (ColumnDefinition each : actual.getAddColumns()) {
            assertColumnDefinition(each, expected.get(count));
            count++;
        }
    }
    
    private void assertColumnDefinition(final ColumnDefinition actual, final ExpectedColumnDefinition expected) {
        assertThat(assertMessage.getFullAssertMessage("Column name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getFullAssertMessage("Column " + actual.getName() + " type assertion error: "), actual.getType(), is(expected.getType()));
        assertThat(assertMessage.getFullAssertMessage("Column " + actual.getName() + " length assertion error: "), actual.getLength(), is(expected.getLength()));
    }
    
    private void assertUpdateColumns(final AlterTableStatement actual, final List<ExpectedUpdateColumnDefinition> expected) {
        assertThat(assertMessage.getFullAssertMessage("Update column size error: "), actual.getUpdateColumns().size(), is(expected.size()));
        int count = 0;
        for (Entry<String, ColumnDefinition> each : actual.getUpdateColumns().entrySet()) {
            assertUpdateColumnDefinition(each, expected.get(count));
            count++;
        }
    }
    
    private void assertUpdateColumnDefinition(final Entry<String, ColumnDefinition> actual, final ExpectedUpdateColumnDefinition expected) {
        assertThat(assertMessage.getFullAssertMessage("Origin column name assertion error: "), actual.getKey(), is(expected.getOriginColumnName()));
        assertColumnDefinition(actual.getValue(), expected);
    }
    
    private void assertColumnPositions(final List<ColumnPosition> actual, final List<ExpectedColumnPosition> expected) {
        if (null == expected) {
            return;
        }
        
        assertThat(assertMessage.getFullAssertMessage("Alter column position size error: "), actual.size(), is(expected.size()));
        
        int count = 0;
        for (ColumnPosition each : actual) {
            assertColumnPosition(each, expected.get(count));
            count++;
        }
    }
    
    private void assertColumnPosition(final ColumnPosition actual, final ExpectedColumnPosition expected) {
        assertThat(assertMessage.getFullAssertMessage("Alter column position name assertion error: "), actual.getColumnName(), is(expected.getColumnName()));
        assertThat(assertMessage.getFullAssertMessage("Alter column [" + actual.getColumnName() + "]position startIndex assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getFullAssertMessage("Alter column [" + actual.getColumnName() + "]position firstColumn assertion error: "), actual.getFirstColumn(), is(expected.getFirstColumn()));
        assertThat(assertMessage.getFullAssertMessage("Alter column [" + actual.getColumnName() + "]position afterColumn assertion error: "), actual.getAfterColumn(), is(expected.getAfterColumn()));
    }
    
    private void assertNewMeta(final TableMetaData actual, final ExpectedTableMetaData expected) {
        assertFalse(assertMessage.getFullAssertMessage("Table new mata should exist: "), actual == null);
        List<String> columnNames = new ArrayList<>();
        List<String> columnTypes = new ArrayList<>();
        List<String> primaryColumns = new ArrayList<>();
        for (ColumnMetaData each :actual.getColumnMetaData()) {
            columnNames.add(each.getColumnName());
            columnTypes.add(each.getColumnType());
            if (each.isPrimaryKey()) {
                primaryColumns.add(each.getColumnName());
            }
        }
        assertFalse(assertMessage.getFullAssertMessage("Table new mata columnNames should exist: "), columnNames.isEmpty());
        assertFalse(assertMessage.getFullAssertMessage("Table new mata columnTypes should exist: "), columnTypes.isEmpty());
        assertFalse(assertMessage.getFullAssertMessage("Column names should exist: "), columnNames.isEmpty());
        assertThat(assertMessage.getFullAssertMessage("Column names assertion error: "), Joiner.on(",").join(columnNames), is(expected.getColumnNames()));
        assertFalse(assertMessage.getFullAssertMessage("Column types should exist: "), columnTypes.isEmpty());
        assertThat(assertMessage.getFullAssertMessage("Column types assertion error: "), Joiner.on(",").join(columnTypes), is(expected.getColumnTypes()));
        assertThat(assertMessage.getFullAssertMessage("Column primary key columns assertion error: "), Joiner.on(",").join(primaryColumns), is(expected.getPrimaryKeyColumns()));
    }
}
