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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.partition.PartitionNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.tablespace.TablespaceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterIndexStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.MovePartitionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.RenamePartitionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.tablespace.TablespaceAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.tablespace.ExpectedTablespace;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.AlterIndexStatementTestCase;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Alter index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterIndexStatementAssert {
    
    /**
     * Assert alter index statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual alter index statement
     * @param expected expected alter index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
        assertMovePartition(assertContext, actual, expected);
        assertRenamePartition(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        Optional<SimpleTableSegment> tableSegment = AlterIndexStatementHandler.getSimpleTableSegment(actual);
        if (null == expected.getTable()) {
            assertFalse(tableSegment.isPresent(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertTrue(tableSegment.isPresent(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, tableSegment.get(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        // TODO should assert index for all databases(mysql and sqlserver do not parse index right now)
        if (actual instanceof OracleAlterIndexStatement && actual.getIndex().isPresent()) {
            IndexAssert.assertIs(assertContext, actual.getIndex().get(), expected.getIndex());
        }
    }
    
    private static void assertMovePartition(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        if (actual instanceof OpenGaussAlterIndexStatement) {
            OpenGaussAlterIndexStatement ogActual = (OpenGaussAlterIndexStatement) actual;
            if (null == expected.getMovePartition()) {
                assertFalse(ogActual.getMovePartition().isPresent(), assertContext.getText("Actual move partition should not exist."));
            } else {
                assertTrue(ogActual.getMovePartition().isPresent(), assertContext.getText("Actual move partition should exist."));
                MovePartitionSegment actualMovePartition = ogActual.getMovePartition().get();
                assertPartition(assertContext, actualMovePartition.getPartitionName(), expected.getMovePartition().getPartition());
                assertTablespace(assertContext, actualMovePartition.getTablespace(), expected.getMovePartition().getTablespace());
                SQLSegmentAssert.assertIs(assertContext, actualMovePartition, expected.getMovePartition());
            }
        }
    }
    
    private static void assertPartition(final SQLCaseAssertContext assertContext, final PartitionNameSegment actual, final ExpectedPartition expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual partition name should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual partition name should exist."));
            assertThat(assertContext.getText("partition name assertion error: "), actual.getIdentifier().getValue(), is(expected.getName()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    private static void assertTablespace(final SQLCaseAssertContext assertContext, final TablespaceSegment actual, final ExpectedTablespace expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual tablespace segments should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual tablespace segments should exist."));
            TablespaceAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    private static void assertRenamePartition(final SQLCaseAssertContext assertContext, final AlterIndexStatement actual, final AlterIndexStatementTestCase expected) {
        if (actual instanceof OpenGaussAlterIndexStatement) {
            OpenGaussAlterIndexStatement ogActual = (OpenGaussAlterIndexStatement) actual;
            if (null == expected.getRenamePartition()) {
                assertFalse(ogActual.getRenamePartition().isPresent(), assertContext.getText("Actual rename partition should not exist."));
            } else {
                assertTrue(ogActual.getRenamePartition().isPresent(), assertContext.getText("Actual rename partition should exist."));
                RenamePartitionSegment renamePartition = ogActual.getRenamePartition().get();
                assertPartition(assertContext, renamePartition.getOldPartition(), expected.getRenamePartition().getOldPartition());
                assertPartition(assertContext, renamePartition.getNewPartition(), expected.getRenamePartition().getNewPartition());
                SQLSegmentAssert.assertIs(assertContext, renamePartition, expected.getRenamePartition());
            }
        }
    }
}
