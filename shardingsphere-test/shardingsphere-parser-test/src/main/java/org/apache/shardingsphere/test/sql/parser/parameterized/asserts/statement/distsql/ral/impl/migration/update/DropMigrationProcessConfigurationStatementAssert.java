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

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DropMigrationProcessConfigurationStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.migration.DropMigrationProcessConfigurationStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Drop migration process configuration statement assert.
 */
public final class DropMigrationProcessConfigurationStatementAssert {
    
    /**
     * Assert drop migration process configuration statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropMigrationProcessConfigurationStatement actual, final DropMigrationProcessConfigurationStatementTestCase expected) {
        assertThat(assertContext.getText("conf path does not match"), actual.getConfPath(), is(expected.getConfPath()));
    }
}
