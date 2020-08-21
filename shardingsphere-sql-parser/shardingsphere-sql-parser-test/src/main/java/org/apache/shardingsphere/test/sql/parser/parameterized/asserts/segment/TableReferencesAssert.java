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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinedTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.JoinTableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableFactorAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.ExpectedTableReference;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedJoinTable;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * TableReferences assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableReferencesAssert {
    
    /**
     * Assert actual TableReferences segments is correct with expected TableReferences.
     *
     * @param assertContext assert context
     * @param actual actual TableReferences
     * @param expected expected TableReferences
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final List<TableReferenceSegment> actual, final List<ExpectedTableReference> expected) {
        assertThat(assertContext.getText("TableReferences assert error"), actual.size(), is(null == expected ? 0 : expected.size()));
        for (int i = 0; i < actual.size(); i++) {
            TableFactorAssert.assertIs(assertContext, actual.get(i).getTableFactor(), expected.get(i).getTableFactor());
            JoinTableAssert.assertIs(assertContext, (List<JoinedTableSegment>) actual.get(i).getJoinedTables(), (List<ExpectedJoinTable>) expected.get(i).getJoinTables());
        }
    }
}
