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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.segment.MigrationSourceTargetSegment;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.MigrateTableStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.MigrateTableStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Migrate table statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MigrateTableStatementAssert {
    
    /**
     * Assert migrate table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual migrate table statement
     * @param expected expected migrate table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MigrateTableStatement actual, final MigrateTableStatementTestCase expected) {
        assertThat(assertContext.getText("target database name does not match"), actual.getTargetDatabaseName(), is(expected.getTargetDatabaseName()));
        assertThat(actual.getSourceTargetEntries().size(), is(1));
        for (MigrationSourceTargetSegment each : actual.getSourceTargetEntries()) {
            DataNode dataNode = new DataNode(each.getSourceDatabaseName(), each.getSourceSchemaName(), each.getSourceTableName());
            assertThat(assertContext.getText("source database name does not match"), dataNode.getDataSourceName(), is(expected.getSourceResourceName()));
            assertThat(assertContext.getText("source schema name does not match"), dataNode.getSchemaName(), is(expected.getSourceSchemaName()));
            assertThat(assertContext.getText("source table name does not match"), dataNode.getTableName(), is(expected.getSourceTableName()));
            assertThat(assertContext.getText("target table name does not match"), each.getTargetTableName(), is(expected.getTargetTableName()));
        }
    }
}
