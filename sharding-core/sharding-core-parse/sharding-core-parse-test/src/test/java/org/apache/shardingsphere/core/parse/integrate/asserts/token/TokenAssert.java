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

package org.apache.shardingsphere.core.parse.integrate.asserts.token;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;

/**
 * Token assert.
 *
 * @author zhangliang
 */
public final class TokenAssert {
    
    private final TableTokenAssert tableTokenAssert;
    
    private final IndexTokenAssert indexTokenAssert;
    
    private final InsertValuesTokenAssert insertValuesTokenAssert;
    
    private final OffsetTokenAssert offsetTokenAssert;
    
    private final RowCountTokenAssert rowCountTokenAssert;
    
    private final AggregationDistinctTokenAssert aggregationDistinctTokenAssert;
    
    private final EncryptColumnTokenAssert encryptColumnTokenAssert;
    
    private final RemoveTokenAssert removeTokenAssert;
    
    private final DatabaseType databaseType;
    
    public TokenAssert(final SQLCaseType sqlCaseType, final SQLStatementAssertMessage assertMessage, final DatabaseType databaseType) {
        tableTokenAssert = new TableTokenAssert(assertMessage);
        indexTokenAssert = new IndexTokenAssert(assertMessage);
        insertValuesTokenAssert = new InsertValuesTokenAssert(assertMessage);
        offsetTokenAssert = new OffsetTokenAssert(sqlCaseType, assertMessage);
        rowCountTokenAssert = new RowCountTokenAssert(sqlCaseType, assertMessage);
        aggregationDistinctTokenAssert = new AggregationDistinctTokenAssert(assertMessage);
        encryptColumnTokenAssert = new EncryptColumnTokenAssert(sqlCaseType, assertMessage);
        removeTokenAssert = new RemoveTokenAssert(assertMessage);
        this.databaseType = databaseType;
    }
    
    /**
     * Assert tokens.
     * 
     * @param actual actual tokens
     * @param expected expected tokens
     */
    public void assertTokens(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        tableTokenAssert.assertTableTokens(actual, expected);
        indexTokenAssert.assertIndexToken(actual, expected);
        insertValuesTokenAssert.assertInsertValuesToken(actual, expected);
        offsetTokenAssert.assertOffsetToken(actual, expected);
        rowCountTokenAssert.assertRowCountToken(actual, expected);
        aggregationDistinctTokenAssert.assertAggregationDistinctTokens(actual, expected);
        if (DatabaseType.MySQL == databaseType) {
            encryptColumnTokenAssert.assertEncryptColumnsToken(actual, expected);
        }
        removeTokenAssert.assertRemoveTokens(actual, expected);
    }
}
