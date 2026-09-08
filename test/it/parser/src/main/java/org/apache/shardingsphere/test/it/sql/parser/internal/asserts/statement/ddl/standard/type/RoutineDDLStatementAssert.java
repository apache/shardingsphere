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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.AlterPackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.DropPackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.AlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.DropTriggerStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.packages.PackageAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.plsql.RoutineNameAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleDropPackageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.AlterPackageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.AlterFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.DropFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.trigger.AlterTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.trigger.DropTriggerStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Routine DDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutineDDLStatementAssert {
    
    /**
     * Assert alter package statement.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected test case
     */
    public static void assertAlterPackage(final SQLCaseAssertContext assertContext, final AlterPackageStatement actual, final AlterPackageStatementTestCase expected) {
        if (null != expected.getPackageName()) {
            assertTrue(actual.getPackageName().isPresent(), assertContext.getText("Actual package name should exist."));
            PackageAssert.assertIs(assertContext, actual.getPackageName().get(), expected.getPackageName());
        }
    }
    
    /**
     * Assert drop package statement.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected test case
     */
    public static void assertDropPackage(final SQLCaseAssertContext assertContext, final DropPackageStatement actual, final OracleDropPackageStatementTestCase expected) {
        if (null != expected.getPackageName()) {
            assertTrue(actual.getPackageName().isPresent(), assertContext.getText("Actual package name should exist."));
            PackageAssert.assertIs(assertContext, actual.getPackageName().get(), expected.getPackageName());
        }
        if (null != expected.getBody()) {
            assertThat(assertContext.getText("Package body assertion error: "), actual.isBody(), is(expected.getBody()));
        }
    }
    
    /**
     * Assert alter function statement.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected test case
     */
    public static void assertAlterFunction(final SQLCaseAssertContext assertContext, final AlterFunctionStatement actual, final AlterFunctionStatementTestCase expected) {
        if (null != expected.getFunctionName()) {
            assertTrue(actual.getFunctionName().isPresent(), assertContext.getText("Actual function name should exist."));
            RoutineNameAssert.assertIs(assertContext, actual.getFunctionName().get(), expected.getFunctionName());
        }
    }
    
    /**
     * Assert drop function statement.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected test case
     */
    public static void assertDropFunction(final SQLCaseAssertContext assertContext, final DropFunctionStatement actual, final DropFunctionStatementTestCase expected) {
        if (null != expected.getFunctionName()) {
            assertTrue(actual.getFunctionName().isPresent(), assertContext.getText("Actual function name should exist."));
            RoutineNameAssert.assertIs(assertContext, actual.getFunctionName().get(), expected.getFunctionName());
        }
    }
    
    /**
     * Assert alter trigger statement.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected test case
     */
    public static void assertAlterTrigger(final SQLCaseAssertContext assertContext, final AlterTriggerStatement actual, final AlterTriggerStatementTestCase expected) {
        if (null != expected.getTriggerName()) {
            assertTrue(actual.getTriggerName().isPresent(), assertContext.getText("Actual trigger name should exist."));
            RoutineNameAssert.assertIs(assertContext, actual.getTriggerName().get(), expected.getTriggerName());
        }
    }
    
    /**
     * Assert drop trigger statement.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected test case
     */
    public static void assertDropTrigger(final SQLCaseAssertContext assertContext, final DropTriggerStatement actual, final DropTriggerStatementTestCase expected) {
        if (null != expected.getTriggerName()) {
            assertTrue(actual.getTriggerName().isPresent(), assertContext.getText("Actual trigger name should exist."));
            RoutineNameAssert.assertIs(assertContext, actual.getTriggerName().get(), expected.getTriggerName());
        }
    }
}
