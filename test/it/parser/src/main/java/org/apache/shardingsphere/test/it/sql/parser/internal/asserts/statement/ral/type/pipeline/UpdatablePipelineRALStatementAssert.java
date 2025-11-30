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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.updatable.DropStreamingStatement;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.UpdatablePipelineRALStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.CheckMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.CommitMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.MigrateTableStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.RegisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.RollbackMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StartMigrationCheckStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StartMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StopMigrationCheckStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StopMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.UnregisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.cdc.DropStreamingStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.CheckMigrationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.CommitMigrationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.MigrateTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.RegisterMigrationSourceStorageUnitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.RollbackMigrationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.StartMigrationCheckStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.StartMigrationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.StopMigrationCheckStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.StopMigrationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update.UnregisterMigrationSourceStorageUnitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterTransmissionRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.DropStreamingStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.CheckMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.CommitMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.MigrateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.RegisterMigrationSourceStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.RollbackMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StartMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StartMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StopMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StopMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.UnregisterMigrationSourceStorageUnitStatementTestCase;

/**
 * Updatable pipeline RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdatablePipelineRALStatementAssert {
    
    /**
     * Assert updatable pipeline RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual updatable pipeline RAL statement
     * @param expected expected updatable pipeline RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdatablePipelineRALStatement actual, final SQLParserTestCase expected) {
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
        } else if (actual instanceof AlterTransmissionRuleStatement) {
            AlterTransmissionRuleStatementAssert.assertIs(assertContext, (AlterTransmissionRuleStatement) actual, (AlterTransmissionRuleStatementTestCase) expected);
        } else if (actual instanceof DropStreamingStatement) {
            DropStreamingStatementAssert.assertIs(assertContext, (DropStreamingStatement) actual, (DropStreamingStatementTestCase) expected);
        }
    }
}
