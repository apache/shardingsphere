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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.oracle.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.OracleAlterSessionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterSessionStatementTestCase;

/**
 * Alter session statement assert for Oracle.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OracleAlterSessionStatementAssert {
    
    /**
     * Assert alter session statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter session statement
     * @param expected expected alter session statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OracleAlterSessionStatement actual, final OracleAlterSessionStatementTestCase expected) {
    }
}
