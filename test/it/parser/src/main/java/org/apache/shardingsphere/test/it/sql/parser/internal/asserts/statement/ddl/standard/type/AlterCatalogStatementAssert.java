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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.catalog.AlterCatalogStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.catalog.CatalogAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.catalog.AlterCatalogStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Alter catalog statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterCatalogStatementAssert {
    
    /**
     * Assert alter catalog statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter catalog statement
     * @param expected expected alter catalog statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterCatalogStatement actual, final AlterCatalogStatementTestCase expected) {
        assertCatalogName(assertContext, actual, expected);
        assertRenameCatalog(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
        assertCatalogComment(assertContext, actual, expected);
    }
    
    private static void assertCatalogName(final SQLCaseAssertContext assertContext, final AlterCatalogStatement actual, final AlterCatalogStatementTestCase expected) {
        if (null != expected.getCatalogName()) {
            CatalogAssert.assertCatalogName(assertContext, actual.getCatalogName(), expected.getCatalogName());
        }
    }
    
    private static void assertRenameCatalog(final SQLCaseAssertContext assertContext, final AlterCatalogStatement actual, final AlterCatalogStatementTestCase expected) {
        if (null != expected.getRenameCatalog()) {
            assertTrue(actual.getNewCatalogName().isPresent(), assertContext.getText("Actual new catalog name should exist."));
            CatalogAssert.assertRenameCatalog(assertContext, actual.getNewCatalogName().get(), expected.getRenameCatalog());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final AlterCatalogStatement actual, final AlterCatalogStatementTestCase expected) {
        if (null != expected.getProperties()) {
            CatalogAssert.assertProperties(assertContext, actual.getProperties(), expected.getProperties());
        }
    }
    
    private static void assertCatalogComment(final SQLCaseAssertContext assertContext, final AlterCatalogStatement actual, final AlterCatalogStatementTestCase expected) {
        if (null != expected.getCatalogComment()) {
            assertTrue(actual.getComment().isPresent(), assertContext.getText("Actual catalog comment should exist."));
            CatalogAssert.assertCatalogComment(assertContext, actual.getComment().get(), expected.getCatalogComment());
        }
    }
}
