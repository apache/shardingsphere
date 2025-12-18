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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterSQLTranslatorRuleStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Alter SQL translator rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterSQLTranslatorRuleStatementAssert {
    
    /**
     * Assert alter SQL translator rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter SQL translator rule statement
     * @param expected expected alter SQL translator rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterSQLTranslatorRuleStatement actual, final AlterSQLTranslatorRuleStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            AlgorithmAssert.assertIs(assertContext, actual.getProvider(), expected.getProvider());
            assertUseOriginalSQL(assertContext, actual.getUseOriginalSQLWhenTranslatingFailed(), expected.getUseOriginalSQLWhenTranslatingFailed());
        }
    }
    
    private static void assertUseOriginalSQL(final SQLCaseAssertContext assertContext, final Boolean actual, final Boolean expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual useOriginalSQLWhenTranslatingFailed should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual useOriginalSQLWhenTranslatingFailed should exist."));
            assertThat(assertContext.getText(String.format("`useOriginalSQLWhenTranslatingFailed` assertion error: %s", expected)), actual, is(expected));
        }
    }
}
