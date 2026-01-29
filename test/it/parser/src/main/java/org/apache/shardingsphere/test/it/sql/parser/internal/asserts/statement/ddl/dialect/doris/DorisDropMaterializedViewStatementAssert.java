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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropMaterializedViewStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisDropMaterializedViewStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Drop materialized view statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDropMaterializedViewStatementAssert {
    
    /**
     * Assert drop materialized view statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop materialized view statement
     * @param expected expected drop materialized view statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisDropMaterializedViewStatement actual, final DorisDropMaterializedViewStatementTestCase expected) {
        if (null != expected.getMaterializedViewName()) {
            assertThat(assertContext.getText("Materialized view name does not match: "), actual.getMaterializedViewName(), is(expected.getMaterializedViewName()));
        }
        if (null != expected.getTableName()) {
            TableAssert.assertIs(assertContext, actual.getTableName(), expected.getTableName());
        }
        if (null != expected.getIfExists()) {
            assertThat(assertContext.getText("If exists flag does not match: "), actual.isIfExists(), is(expected.getIfExists()));
        }
    }
}
