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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.type.AlterRuleStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.type.CreateRuleStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.type.DropRuleStatement;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.parser.distsql.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.AlterGlobalClockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.AlterSQLParserRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.AlterSQLTranslatorRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.AlterRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.AlterStorageUnitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.CreateRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.RegisterStorageUnitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.DropRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.UnregisterStorageUnitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterGlobalClockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterSQLTranslatorRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.AlterStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.RegisterStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.UnregisterStorageUnitStatementTestCase;
import org.apache.shardingsphere.transaction.distsql.statement.updatable.AlterTransactionRuleStatement;

/**
 * RDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLStatementAssert {
    
    /**
     * Assert SQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual RDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof CreateRuleStatement) {
            CreateRuleStatementAssert.assertIs(assertContext, (CreateRuleStatement) actual, expected);
        } else if (actual instanceof AlterRuleStatement) {
            AlterRuleStatementAssert.assertIs(assertContext, (AlterRuleStatement) actual, expected);
        } else if (actual instanceof DropRuleStatement) {
            DropRuleStatementAssert.assertIs(assertContext, (DropRuleStatement) actual, expected);
        } else if (actual instanceof RegisterStorageUnitStatement) {
            RegisterStorageUnitStatementAssert.assertIs(assertContext, (RegisterStorageUnitStatement) actual, (RegisterStorageUnitStatementTestCase) expected);
        } else if (actual instanceof AlterStorageUnitStatement) {
            AlterStorageUnitStatementAssert.assertIs(assertContext, (AlterStorageUnitStatement) actual, (AlterStorageUnitStatementTestCase) expected);
        } else if (actual instanceof UnregisterStorageUnitStatement) {
            UnregisterStorageUnitStatementAssert.assertIs(assertContext, (UnregisterStorageUnitStatement) actual, (UnregisterStorageUnitStatementTestCase) expected);
        } else if (actual instanceof AlterSQLParserRuleStatement) {
            AlterSQLParserRuleStatementAssert.assertIs(assertContext, (AlterSQLParserRuleStatement) actual, (AlterSQLParserRuleStatementTestCase) expected);
        } else if (actual instanceof AlterSQLTranslatorRuleStatement) {
            AlterSQLTranslatorRuleStatementAssert.assertIs(assertContext, (AlterSQLTranslatorRuleStatement) actual, (AlterSQLTranslatorRuleStatementTestCase) expected);
        } else if (actual instanceof AlterTransactionRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof AlterGlobalClockRuleStatement) {
            AlterGlobalClockRuleStatementAssert.assertIs(assertContext, (AlterGlobalClockRuleStatement) actual, (AlterGlobalClockRuleStatementTestCase) expected);
        }
    }
}
