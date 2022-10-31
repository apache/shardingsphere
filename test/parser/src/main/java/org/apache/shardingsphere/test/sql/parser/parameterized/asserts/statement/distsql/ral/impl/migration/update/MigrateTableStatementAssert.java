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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.migration.update;

import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.migration.MigrateTableStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Migrate table statement assert.
 */
public final class MigrateTableStatementAssert {
    
    /**
     * Assert migrate table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual migrate table statement
     * @param expected expected migrate table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MigrateTableStatement actual, final MigrateTableStatementTestCase expected) {
        assertThat(assertContext.getText("source database name does not match"), actual.getSourceResourceName(), is(expected.getSourceResourceName()));
        assertThat(assertContext.getText("source schema name does not match"), actual.getSourceSchemaName(), is(expected.getSourceSchemaName()));
        assertThat(assertContext.getText("source table name does not match"), actual.getSourceTableName(), is(expected.getSourceTableName()));
        assertThat(assertContext.getText("target database name does not match"), actual.getTargetDatabaseName(), is(expected.getTargetDatabaseName()));
        assertThat(assertContext.getText("target table name does not match"), actual.getTargetTableName(), is(expected.getTargetTableName()));
    }
}
