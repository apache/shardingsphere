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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Joiner;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.table.ExpectedAlterTable;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnDefinition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedUpdateColumnDefinition;
import lombok.RequiredArgsConstructor;

/**
 * Table assert.
 *
 * @author
 */
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
        assertSame(assertMessage.getFullAssertMessage("Drop primary key assertion error: "), Boolean.valueOf(actual.isDropPrimaryKey()), Boolean.valueOf(expected.isDropPrimaryKey()));
        
        assertThat(assertMessage.getFullAssertMessage("Rename table name assertion error: "), actual.getNewTableName(), is(expected.getNewTableName()));
        assertAddColumns(actual, expected.getAddColumns()); 
        
        assertUpdateColumns(actual, expected.getUpdateColumns()); 
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
        assertThat(assertMessage.getFullAssertMessage("Column name assertion error: "), actual.getType(), is(expected.getType()));
        assertThat(assertMessage.getFullAssertMessage("Column name assertion error: "), actual.getLength(), is(expected.getLength()));
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
}
