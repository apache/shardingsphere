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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.AlterTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CreateIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.CreateTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.DropIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.DropTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl.TruncateStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.TruncateStatementTestCase;

/**
 * DDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DDLStatementAssert {
    
    /**
     * Assert DDL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual DDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof CreateTableStatement) {
            CreateTableStatementAssert.assertIs(assertContext, (CreateTableStatement) actual, (CreateTableStatementTestCase) expected);
        } else if (actual instanceof AlterTableStatement) {
            AlterTableStatementAssert.assertIs(assertContext, (AlterTableStatement) actual, (AlterTableStatementTestCase) expected);
        } else if (actual instanceof DropTableStatement) {
            DropTableStatementAssert.assertIs(assertContext, (DropTableStatement) actual, (DropTableStatementTestCase) expected);
        } else if (actual instanceof TruncateStatement) {
            TruncateStatementAssert.assertIs(assertContext, (TruncateStatement) actual, (TruncateStatementTestCase) expected);
        } else if (actual instanceof CreateIndexStatement) {
            CreateIndexStatementAssert.assertIs(assertContext, (CreateIndexStatement) actual, (CreateIndexStatementTestCase) expected);
        } else if (actual instanceof AlterIndexStatement) {
            AlterIndexStatementAssert.assertIs(assertContext, (AlterIndexStatement) actual, (AlterIndexStatementTestCase) expected);
        } else if (actual instanceof DropIndexStatement) {
            DropIndexStatementAssert.assertIs(assertContext, (DropIndexStatement) actual, (DropIndexStatementTestCase) expected);
        }
    }
}
