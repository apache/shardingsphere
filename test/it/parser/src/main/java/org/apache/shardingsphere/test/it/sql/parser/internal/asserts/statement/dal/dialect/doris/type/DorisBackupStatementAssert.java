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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.BackupTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisBackupStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.backup.ExpectedBackupTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisBackupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Backup statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisBackupStatementAssert {
    
    /**
     * Assert backup statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual backup statement
     * @param expected expected backup statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisBackupStatement actual, final DorisBackupStatementTestCase expected) {
        assertThat(assertContext.getText("global flag does not match: "), actual.isGlobal(), is(expected.isGlobal()));
        assertThat(assertContext.getText("snapshot name does not match: "), actual.getSnapshotName(), is(expected.getSnapshotName()));
        assertDatabase(assertContext, actual, expected);
        assertRepositoryName(assertContext, actual, expected);
        assertThat(assertContext.getText("exclude mode does not match: "), actual.isExcludeMode(), is(expected.isExcludeMode()));
        assertBackupTables(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertDatabase(final SQLCaseAssertContext assertContext, final DorisBackupStatement actual, final DorisBackupStatementTestCase expected) {
        if (null != expected.getDatabase()) {
            assertNotNull(actual.getDatabase(), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabase(), expected.getDatabase());
        }
    }
    
    private static void assertRepositoryName(final SQLCaseAssertContext assertContext, final DorisBackupStatement actual, final DorisBackupStatementTestCase expected) {
        if (null != expected.getRepositoryName()) {
            assertNotNull(actual.getRepositoryName(), assertContext.getText("Actual repository name should exist."));
            IdentifierValueAssert.assertIs(assertContext, actual.getRepositoryName().getIdentifier(), expected.getRepositoryName(), "Repository name");
            SQLSegmentAssert.assertIs(assertContext, actual.getRepositoryName(), expected.getRepositoryName());
        }
    }
    
    private static void assertBackupTables(final SQLCaseAssertContext assertContext, final DorisBackupStatement actual, final DorisBackupStatementTestCase expected) {
        assertThat(assertContext.getText("Backup tables size does not match: "), actual.getTables().size(), is(expected.getTables().size()));
        int count = 0;
        for (BackupTableSegment each : actual.getTables()) {
            ExpectedBackupTable expectedTable = expected.getTables().get(count);
            TableAssert.assertIs(assertContext, each.getTable(), expectedTable.getTable());
            SQLSegmentAssert.assertIs(assertContext, each, expectedTable);
            assertPartitions(assertContext, new ArrayList<>(each.getPartitions()), expectedTable.getPartitions());
            count++;
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final List<PartitionSegment> actual, final List<ExpectedPartition> expected) {
        assertThat(assertContext.getText("Partitions size does not match: "), actual.size(), is(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            IdentifierValueAssert.assertIs(assertContext, actual.get(i).getName(), expected.get(i), "Partition");
            SQLSegmentAssert.assertIs(assertContext, actual.get(i), expected.get(i));
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisBackupStatement actual, final DorisBackupStatementTestCase expected) {
        if (!expected.getProperties().isEmpty()) {
            assertNotNull(actual.getProperties(), assertContext.getText("properties should not be null"));
            assertThat(assertContext.getText("Properties size does not match: "), actual.getProperties().getProperties().size(), is(expected.getProperties().size()));
            for (int i = 0; i < expected.getProperties().size(); i++) {
                assertProperty(assertContext, actual.getProperties().getProperties().get(i), expected.getProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
