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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.CommitMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.RegisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.migration.distsql.statement.RollbackMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.StartMigrationCheckStatement;
import org.apache.shardingsphere.migration.distsql.statement.StartMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationCheckStatement;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.UnregisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.CheckMigrationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.CommitMigrationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.MigrateTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.RegisterMigrationSourceStorageUnitStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.RollbackMigrationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.StartMigrationCheckStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.StartMigrationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.StopMigrationCheckStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.StopMigrationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.update.UnregisterMigrationSourceStorageUnitStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.CheckMigrationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.CommitMigrationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.MigrateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.RegisterMigrationSourceStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.RollbackMigrationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.StartMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.StartMigrationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.StopMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.StopMigrationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.UnregisterMigrationSourceStorageUnitStatementTestCase;

/**
 * Updatable Scaling RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdatableScalingRALStatementAssert {
    
    /**
     * Assert updatable scaling RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual updatable scaling RAL statement
     * @param expected expected updatable scaling RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdatableScalingRALStatement actual, final SQLParserTestCase expected) {
        // TODO add more test case
        if (actual instanceof MigrateTableStatement) {
            MigrateTableStatementAssert.assertIs(assertContext, (MigrateTableStatement) actual, (MigrateTableStatementTestCase) expected);
        } else if (actual instanceof StopMigrationStatement) {
            StopMigrationStatementAssert.assertIs(assertContext, (StopMigrationStatement) actual, (StopMigrationStatementTestCase) expected);
        } else if (actual instanceof CommitMigrationStatement) {
            CommitMigrationStatementAssert.assertIs(assertContext, (CommitMigrationStatement) actual, (CommitMigrationStatementTestCase) expected);
        } else if (actual instanceof RollbackMigrationStatement) {
            RollbackMigrationStatementAssert.assertIs(assertContext, (RollbackMigrationStatement) actual, (RollbackMigrationStatementTestCase) expected);
        } else if (actual instanceof StartMigrationStatement) {
            StartMigrationStatementAssert.assertIs(assertContext, (StartMigrationStatement) actual, (StartMigrationStatementTestCase) expected);
        } else if (actual instanceof RegisterMigrationSourceStorageUnitStatement) {
            RegisterMigrationSourceStorageUnitStatementAssert.assertIs(
                    assertContext, (RegisterMigrationSourceStorageUnitStatement) actual, (RegisterMigrationSourceStorageUnitStatementTestCase) expected);
        } else if (actual instanceof UnregisterMigrationSourceStorageUnitStatement) {
            UnregisterMigrationSourceStorageUnitStatementAssert.assertIs(
                    assertContext, (UnregisterMigrationSourceStorageUnitStatement) actual, (UnregisterMigrationSourceStorageUnitStatementTestCase) expected);
        } else if (actual instanceof CheckMigrationStatement) {
            CheckMigrationStatementAssert.assertIs(assertContext, (CheckMigrationStatement) actual, (CheckMigrationStatementTestCase) expected);
        } else if (actual instanceof StartMigrationCheckStatement) {
            StartMigrationCheckStatementAssert.assertIs(assertContext, (StartMigrationCheckStatement) actual, (StartMigrationCheckStatementTestCase) expected);
        } else if (actual instanceof StopMigrationCheckStatement) {
            StopMigrationCheckStatementAssert.assertIs(assertContext, (StopMigrationCheckStatement) actual, (StopMigrationCheckStatementTestCase) expected);
        }
    }
}
