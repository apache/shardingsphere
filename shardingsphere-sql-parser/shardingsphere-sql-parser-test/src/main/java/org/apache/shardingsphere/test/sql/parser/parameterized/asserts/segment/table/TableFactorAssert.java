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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.TableReferencesAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.ExpectedTableReference;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedTableFactor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableFactorSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;

import java.util.List;

/**
 * TableFactor assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableFactorAssert {
    
    /**
     * Assert actual TableFactor segments is correct with expected TableFactor.
     *
     * @param assertContext assert context
     * @param actual actual TableFactor
     * @param expected expected TableFactor
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableFactorSegment actual, final ExpectedTableFactor expected) {
        if (null != actual.getTable()) {
            if (actual.getTable() instanceof SimpleTableSegment) {
                TableAssert.assertIs(assertContext, (SimpleTableSegment) actual.getTable(), expected.getTable());
            } else if (actual.getTable() instanceof SubqueryTableSegment) {
                SelectStatementAssert.assertIs(assertContext, ((SubqueryTableSegment) actual.getTable()).getSubquery().getSelect(), expected.getSubqueryTable().getSubquery().getSelectTestCases());
            }
        }
        TableReferencesAssert.assertIs(assertContext, (List<TableReferenceSegment>) actual.getTableReferences(), (List<ExpectedTableReference>) expected.getExpectedTableReferences());
    }
}
