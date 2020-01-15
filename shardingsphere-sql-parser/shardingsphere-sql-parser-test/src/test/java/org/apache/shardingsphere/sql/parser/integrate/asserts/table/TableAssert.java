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

package org.apache.shardingsphere.sql.parser.integrate.asserts.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.table.ExpectedTable;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.table.ExpectedTableOwner;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Table assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert tables.
     * 
     * @param actual actual tables
     * @param expected expected tables
     */
    public void assertTables(final Collection<TableSegment> actual, final List<ExpectedTable> expected) {
        assertThat(assertMessage.getText("Tables size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (TableSegment each : actual) {
            assertTable(each, expected.get(count));
            count++;
        }
    }
    
    private void assertTable(final TableSegment actual, final ExpectedTable expected) {
        assertThat(assertMessage.getText("Table name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertMessage.getText("Table alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        if (actual.getOwner().isPresent()) {
            assertOwner(actual.getOwner().get(), expected.getOwner());
        }
        assertThat(assertMessage.getText("Table start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Table stop delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        assertThat(assertMessage.getText("Table start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getText("Table stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
    
    private void assertOwner(final SchemaSegment actual, final ExpectedTableOwner expected) {
        assertThat(assertMessage.getText("Table owner name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getText("Table owner start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Table owner stop delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        assertThat(assertMessage.getText("Table owner start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getText("Table owner stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
}
