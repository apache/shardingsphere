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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisAlterRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisCreateRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisPauseRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisResumeRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisStopRoutineLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisAlterRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisCreateRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisPauseRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisResumeRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisStopRoutineLoadStatementTestCase;

/**
 * Doris DML statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDMLStatementAssert {
    
    /**
     * Assert Doris DML statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DML statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DMLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof DorisCreateRoutineLoadStatement) {
            DorisCreateRoutineLoadStatementAssert.assertIs(assertContext, (DorisCreateRoutineLoadStatement) actual, (DorisCreateRoutineLoadStatementTestCase) expected);
        } else if (actual instanceof DorisAlterRoutineLoadStatement) {
            DorisAlterRoutineLoadStatementAssert.assertIs(assertContext, (DorisAlterRoutineLoadStatement) actual, (DorisAlterRoutineLoadStatementTestCase) expected);
        } else if (actual instanceof DorisPauseRoutineLoadStatement) {
            DorisPauseRoutineLoadStatementAssert.assertIs(assertContext, (DorisPauseRoutineLoadStatement) actual, (DorisPauseRoutineLoadStatementTestCase) expected);
        } else if (actual instanceof DorisResumeRoutineLoadStatement) {
            DorisResumeRoutineLoadStatementAssert.assertIs(assertContext, (DorisResumeRoutineLoadStatement) actual, (DorisResumeRoutineLoadStatementTestCase) expected);
        } else if (actual instanceof DorisStopRoutineLoadStatement) {
            DorisStopRoutineLoadStatementAssert.assertIs(assertContext, (DorisStopRoutineLoadStatement) actual, (DorisStopRoutineLoadStatementTestCase) expected);
        }
    }
}
