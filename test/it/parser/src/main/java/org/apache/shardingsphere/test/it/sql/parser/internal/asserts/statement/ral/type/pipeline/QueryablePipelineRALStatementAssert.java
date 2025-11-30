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
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingListStatement;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingRuleStatement;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingStatusStatement;
import org.apache.shardingsphere.data.pipeline.distsql.statement.queryable.QueryablePipelineRALStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationCheckStatusStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationListStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationSourceStorageUnitsStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationStatusStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.cdc.ShowStreamingStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.query.ShowMigrationCheckStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.query.ShowMigrationStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.ShowStreamingStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.ShowMigrationCheckStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.ShowMigrationStatusStatementTestCase;

/**
 * Queryable pipeline RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryablePipelineRALStatementAssert {
    
    /**
     * Assert query pipeline RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual queryable pipeline RAL statement
     * @param expected expected queryable pipeline RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final QueryablePipelineRALStatement actual, final SQLParserTestCase expected) {
        // TODO add more test case
        if (actual instanceof ShowMigrationListStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowMigrationCheckStatusStatement) {
            ShowMigrationCheckStatusStatementAssert.assertIs(assertContext, (ShowMigrationCheckStatusStatement) actual, (ShowMigrationCheckStatusStatementTestCase) expected);
        } else if (actual instanceof ShowMigrationStatusStatement) {
            ShowMigrationStatusStatementAssert.assertIs(assertContext, (ShowMigrationStatusStatement) actual, (ShowMigrationStatusStatementTestCase) expected);
        } else if (actual instanceof ShowMigrationSourceStorageUnitsStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowStreamingListStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowStreamingStatusStatement) {
            ShowStreamingStatusStatementAssert.assertIs(assertContext, (ShowStreamingStatusStatement) actual, (ShowStreamingStatusStatementTestCase) expected);
        } else if (actual instanceof ShowStreamingRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        }
    }
}
