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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.distsql.statement.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowTablesStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowDatabaseRulesStatement;
import org.apache.shardingsphere.parser.distsql.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sqltranslator.distsql.statement.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.type.ShowRulesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.type.ShowStorageUnitsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.type.ShowTablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.transaction.distsql.statement.queryable.ShowTransactionRuleStatement;

/**
 * RQL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RQLStatementAssert {
    
    /**
     * Assert RQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual RQL statement
     * @param expected expected RQL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RQLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof ShowDatabaseRulesStatement) {
            ShowRulesStatementAssert.assertIs(assertContext, (ShowDatabaseRulesStatement) actual, expected);
        } else if (actual instanceof ShowTablesStatement) {
            ShowTablesStatementAssert.assertIs(assertContext, (ShowTablesStatement) actual, expected);
        } else if (actual instanceof ShowStorageUnitsStatement) {
            ShowStorageUnitsStatementAssert.assertIs(assertContext, (ShowStorageUnitsStatement) actual, expected);
        } else if (actual instanceof ShowAuthorityRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowTransactionRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowSQLParserRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowSQLTranslatorRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowStatusFromReadwriteSplittingRulesStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        }
    }
}
