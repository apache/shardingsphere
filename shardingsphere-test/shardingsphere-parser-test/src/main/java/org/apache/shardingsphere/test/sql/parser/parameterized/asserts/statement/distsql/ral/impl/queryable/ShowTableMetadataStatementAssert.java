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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTableMetadataStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Show table metadata statement assert.
 */
public final class ShowTableMetadataStatementAssert {
    
    /**
     * Assert show table metadata statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show table metadata statement
     * @param expected expected show table metadata statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowTableMetadataStatement actual,
                                final ShowTableMetadataStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            DatabaseAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getDatabase());
            assertThat(assertContext.getText("Table assertion error:"), actual.getTableNames(), is(expected.getTableNames()));
        }
    }
}
