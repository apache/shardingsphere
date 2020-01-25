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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.insert.ExpectedInsertColumnsAndValues;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.insert.ExpectedInsertValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertNamesAndValuesAssert {
    
    /**
     * Assert actual insert statement is correct with expected insert columns and values.
     *
     * @param assertMessage assert message
     * @param actual actual insert statement
     * @param expected expected insert names and values
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLCaseAssertMessage assertMessage, final InsertStatement actual, final ExpectedInsertColumnsAndValues expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Insert column names assertion error: "), Joiner.on(",").join(Collections2.transform(actual.getColumns(), new Function<ColumnSegment, Object>() {
            
            @Override
            public Object apply(final ColumnSegment input) {
                return input.getName();
            }
        })), is(expected.getColumnNames()));
        assertThat(assertMessage.getText("Insert values size assertion error: "), actual.getValues().size(), is(expected.getValues().size()));
        assertInsertValues(assertMessage, actual.getValues(), expected.getValues(), sqlCaseType);
    }
    
    private static void assertInsertValues(final SQLCaseAssertMessage assertMessage, 
                                           final Collection<InsertValuesSegment> actual, final Collection<ExpectedInsertValue> expected, final SQLCaseType sqlCaseType) {
        assertThat(actual.size(), is(expected.size()));
        Iterator<ExpectedInsertValue> expectedIterator = expected.iterator();
        for (InsertValuesSegment each : actual) {
            assertInsertValue(assertMessage, each, expectedIterator.next(), sqlCaseType);
        }
    }
    
    private static void assertInsertValue(final SQLCaseAssertMessage assertMessage, final InsertValuesSegment actual, final ExpectedInsertValue expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Assignments size assertion error: "), actual.getValues().size(), is(expected.getAssignments().size()));
        int i = 0;
        for (ExpressionSegment each : actual.getValues()) {
            AssignmentAssert.assertIs(assertMessage, each, expected.getAssignments().get(i++), sqlCaseType);
        }
    }
}
