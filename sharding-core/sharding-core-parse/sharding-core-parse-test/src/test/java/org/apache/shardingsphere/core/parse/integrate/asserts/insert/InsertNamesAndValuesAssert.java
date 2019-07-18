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

package org.apache.shardingsphere.core.parse.integrate.asserts.insert;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.insert.ExpectedInsertColumnsAndValues;
import org.apache.shardingsphere.core.parse.integrate.jaxb.insert.ExpectedInsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InsertNamesAndValuesAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    private final AssignmentAssert assignmentAssert;
    
    public InsertNamesAndValuesAssert(final SQLStatementAssertMessage assertMessage, final SQLCaseType sqlCaseType) {
        this.assertMessage = assertMessage;
        this.assignmentAssert = new AssignmentAssert(assertMessage, sqlCaseType);
    }
    
    /**
     * Assert insert names and values.
     *
     * @param actual actual insert statement
     * @param expected expected insert names and values
     */
    public void assertInsertNamesAndValues(final InsertStatement actual, final ExpectedInsertColumnsAndValues expected) {
        assertThat(assertMessage.getFullAssertMessage("Insert column names assertion error: "), Joiner.on(",").join(Collections2.transform(actual.getColumns(), new Function<ColumnSegment, Object>() {
            
            @Override
            public Object apply(final ColumnSegment input) {
                return input.getName();
            }
        })), is(expected.getColumnNames()));
        assertThat(assertMessage.getFullAssertMessage("Insert values size assertion error: "), actual.getValues().size(), is(expected.getValues().size()));
        assertInsertValues(actual.getValues(), expected.getValues());
    }
    
    private void assertInsertValues(final Collection<InsertValuesSegment> actual, final Collection<ExpectedInsertValue> expected) {
        assertThat(actual.size(), is(expected.size()));
        Iterator<ExpectedInsertValue> expectedIterator = expected.iterator();
        for (InsertValuesSegment each : actual) {
            assertInsertValue(each, expectedIterator.next());
        }
    }
    
    private void assertInsertValue(final InsertValuesSegment actual, final ExpectedInsertValue expected) {
        assertThat(assertMessage.getFullAssertMessage("Assignments size assertion error: "), actual.getValues().size(), is(expected.getAssignments().size()));
        int i = 0;
        for (ExpressionSegment each : actual.getValues()) {
            assignmentAssert.assertAssignment(each, expected.getAssignments().get(i++));
        }
    }
}
