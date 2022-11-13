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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.RefreshTableMetadataStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Refresh table metadata statement assert.
 */
public final class RefreshTableMetadataStatementAssert {
    
    /**
     * Assert refresh table metadata statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual refresh table metadata statement
     * @param expected expected refresh table metadata statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RefreshTableMetadataStatement actual, final RefreshTableMetadataStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            if (null != expected.getTableName()) {
                assertThat(assertContext.getText("Table name assertion error"), actual.getTableName().get(), is(expected.getTableName()));
            }
            if (null != expected.getStorageUnitName()) {
                assertThat(assertContext.getText("Storage unit name assertion error"), actual.getStorageUnitName().get(), is(expected.getStorageUnitName()));
            }
            if (null != expected.getSchemaName()) {
                assertThat(assertContext.getText("Schema name assertion error"), actual.getSchemaName().get(), is(expected.getSchemaName()));
            }
        }
    }
}
