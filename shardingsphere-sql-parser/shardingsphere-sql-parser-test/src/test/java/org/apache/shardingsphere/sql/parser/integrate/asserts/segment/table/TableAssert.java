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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.schema.SchemaAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.segment.impl.table.ExpectedTable;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.segment.impl.table.ExpectedTableOwner;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Table assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableAssert {
    
    /**
     * Assert actual table segments is correct with expected tables.
     * 
     * @param assertContext assert context
     * @param actual actual tables
     * @param expected expected tables
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Collection<TableSegment> actual, final List<ExpectedTable> expected) {
        assertThat(assertContext.getText("Tables size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (TableSegment each : actual) {
            assertIs(assertContext, each, expected.get(count));
            count++;
        }
    }
    
    /**
     * Assert actual table segment is correct with expected table.
     *
     * @param assertContext assert context
     * @param actual actual table
     * @param expected expected table
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableSegment actual, final ExpectedTable expected) {
        assertThat(assertContext.getText("Table name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertContext.getText("Table alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        if (null != expected.getOwner()) {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getOwner().isPresent());
            SchemaAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        } else {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        }
        assertThat(assertContext.getText("Table start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Table end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert actual table segment is correct with expected table owner.
     *
     * @param assertContext assert context
     * @param actual actual table segment
     * @param expected expected table owner
     */
    public static void assertOwner(final SQLCaseAssertContext assertContext, final TableSegment actual, final ExpectedTableOwner expected) {
        assertThat(assertContext.getText("Owner name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertContext.getText("Owner name start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Owner name end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
