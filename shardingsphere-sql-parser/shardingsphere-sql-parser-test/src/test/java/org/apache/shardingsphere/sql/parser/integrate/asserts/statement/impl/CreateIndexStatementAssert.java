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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;

import java.util.Collections;

/**
 * Create index statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateIndexStatementAssert {
    
    /**
     * Assert create index statement is correct with expected parser result.
     * 
     * @param assertContext Assert context
     * @param actual actual create index statement
     * @param expected expected create index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.findSQLSegments(TableSegment.class), Collections.singletonList(expected.getTable()));
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        IndexAssert.assertIs(assertContext, actual.getIndex(), expected.getIndex());
    }
}
