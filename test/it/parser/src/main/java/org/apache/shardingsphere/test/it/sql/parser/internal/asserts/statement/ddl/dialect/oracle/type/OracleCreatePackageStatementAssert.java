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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg.OracleCreatePackageStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.packages.PackageAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.plsql.RoutineNameAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedRoutineName;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleCreatePackageStatementTestCase;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create package statement assert for Oracle.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OracleCreatePackageStatementAssert {
    
    /**
     * Assert create package statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create package statement
     * @param expected expected create package statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OracleCreatePackageStatement actual, final OracleCreatePackageStatementTestCase expected) {
        PackageAssert.assertIs(assertContext, actual.getPackageName(), expected.getPackageName());
        if (null == expected.getPackageEndName()) {
            assertFalse(actual.getPackageEndName().isPresent(), assertContext.getText("Actual package end name should not exist."));
        } else {
            assertTrue(actual.getPackageEndName().isPresent(), assertContext.getText("Actual package end name should exist."));
            PackageAssert.assertIs(assertContext, actual.getPackageEndName().get(), expected.getPackageEndName());
        }
        assertThat(assertContext.getText("Package body assertion error: "), actual.isBody(), is(expected.isBody()));
        assertThat(assertContext.getText("Package replacement assertion error: "), actual.isReplace(), is(expected.isReplace()));
        assertThat(assertContext.getText("Package IF NOT EXISTS assertion error: "), actual.isIfNotExists(), is(expected.isIfNotExists()));
        assertThat(assertContext.getText("Package edition assertion error: "), actual.getEdition().map(Enum::name).orElse(null), is(expected.getEdition()));
        assertThat(assertContext.getText("Package authorization assertion error: "), actual.getAuthorization().map(Enum::name).orElse(null), is(expected.getAuthorization()));
        assertThat(assertContext.getText("Package initialization assertion error: "), actual.getInitialization().isPresent(), is(expected.isHasInitialization()));
        if (null != expected.getSqlStatementCount()) {
            assertThat(assertContext.getText("Package SQL statements size assertion error: "), actual.getSqlStatements().size(), is(expected.getSqlStatementCount()));
        }
        if (!expected.getTables().isEmpty()) {
            TableAssert.assertIs(assertContext, actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables(), expected.getTables());
        }
        if (!expected.getColumns().isEmpty()) {
            ColumnAssert.assertIs(assertContext, actual.getColumns(), expected.getColumns());
        }
        assertPackageRoutineNames(assertContext, actual.getPackageRoutineNames(), expected.getPackageRoutineNames());
    }
    
    private static void assertPackageRoutineNames(final SQLCaseAssertContext assertContext, final Collection<FunctionNameSegment> actual, final Collection<ExpectedRoutineName> expected) {
        if (expected.isEmpty()) {
            return;
        }
        assertThat(assertContext.getText("Package routine names size mismatched: "), actual.size(), is(expected.size()));
        Iterator<FunctionNameSegment> actualIterator = actual.iterator();
        for (ExpectedRoutineName each : expected) {
            RoutineNameAssert.assertIs(assertContext, actualIterator.next(), each);
        }
    }
}
