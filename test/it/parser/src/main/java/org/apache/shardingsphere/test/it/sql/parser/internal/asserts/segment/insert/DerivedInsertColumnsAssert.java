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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedDerivedInsertColumns;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Derived insert column assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DerivedInsertColumnsAssert {
    
    /**
     * Assert actual derived insert columns is correct with expected derived insert columns.
     *
     * @param assertContext assert context
     * @param actual actual derived insert columns
     * @param expected expected derived insert columns
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Collection<ColumnSegment> actual, final ExpectedDerivedInsertColumns expected) {
        assertThat(assertContext.getText("Derived insert column size assertion error: "), actual.size(), is(expected.getColumns().size()));
        int count = 0;
        for (ColumnSegment each : actual) {
            ColumnAssert.assertIs(assertContext, each, expected.getColumns().get(count));
            count++;
        }
    }
}
