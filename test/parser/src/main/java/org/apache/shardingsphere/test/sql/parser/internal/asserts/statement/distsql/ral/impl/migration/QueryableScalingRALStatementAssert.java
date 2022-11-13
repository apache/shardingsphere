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
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.QueryableScalingRALStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationCheckAlgorithmsStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationCheckStatusStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationListStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationSourceStorageUnitsStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationStatusStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.query.ShowMigrationCheckAlgorithmsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.query.ShowMigrationCheckStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.query.ShowMigrationListStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.query.ShowMigrationSourceStorageUnitsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.migration.query.ShowMigrationStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.ShowMigrationListStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.ShowMigrationCheckAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.ShowMigrationCheckStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.ShowMigrationSourceStorageUnitsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.ral.migration.ShowMigrationStatusStatementTestCase;

/**
 * Queryable RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryableScalingRALStatementAssert {
    
    /**
     * Assert query RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual queryable RAL statement
     * @param expected expected queryable RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final QueryableScalingRALStatement actual, final SQLParserTestCase expected) {
        // TODO add more test case
        if (actual instanceof ShowMigrationListStatement) {
            ShowMigrationListStatementAssert.assertIs(assertContext, (ShowMigrationListStatement) actual, (ShowMigrationListStatementTestCase) expected);
        } else if (actual instanceof ShowMigrationCheckAlgorithmsStatement) {
            ShowMigrationCheckAlgorithmsStatementAssert.assertIs(assertContext, (ShowMigrationCheckAlgorithmsStatement) actual, (ShowMigrationCheckAlgorithmsStatementTestCase) expected);
        } else if (actual instanceof ShowMigrationCheckStatusStatement) {
            ShowMigrationCheckStatusStatementAssert.assertIs(assertContext, (ShowMigrationCheckStatusStatement) actual, (ShowMigrationCheckStatusStatementTestCase) expected);
        } else if (actual instanceof ShowMigrationStatusStatement) {
            ShowMigrationStatusStatementAssert.assertIs(assertContext, (ShowMigrationStatusStatement) actual, (ShowMigrationStatusStatementTestCase) expected);
        } else if (actual instanceof ShowMigrationSourceStorageUnitsStatement) {
            ShowMigrationSourceStorageUnitsStatementAssert.assertIs(assertContext, (ShowMigrationSourceStorageUnitsStatement) actual, (ShowMigrationSourceStorageUnitsStatementTestCase) expected);
        }
    }
}
