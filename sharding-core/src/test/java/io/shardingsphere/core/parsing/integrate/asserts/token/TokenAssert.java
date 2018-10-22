/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.integrate.asserts.token;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;

/**
 * Token assert.
 *
 * @author zhangliang
 */
public final class TokenAssert {
    
    private final TableTokenAssert tableTokenAssert;
    
    private final SchemaTokenAssert schemaTokenAssert;
    
    private final IndexTokenAssert indexTokenAssert;
    
    private final ItemsTokenAssert itemsTokenAssert;
    
    private final GeneratedKeyTokenAssert generatedKeyTokenAssert;
    
    private final InsertValuesTokenAssert insertValuesTokenAssert;
    
    private final OrderByTokenAssert orderByTokenAssert;
    
    private final OffsetTokenAssert offsetTokenAssert;
    
    private final RowCountTokenAssert rowCountTokenAssert;
    
    public TokenAssert(final SQLCaseType sqlCaseType, final SQLStatementAssertMessage assertMessage) {
        tableTokenAssert = new TableTokenAssert(assertMessage);
        schemaTokenAssert = new SchemaTokenAssert(assertMessage);
        indexTokenAssert = new IndexTokenAssert(assertMessage);
        itemsTokenAssert = new ItemsTokenAssert(assertMessage);
        generatedKeyTokenAssert = new GeneratedKeyTokenAssert(sqlCaseType, assertMessage);
        insertValuesTokenAssert = new InsertValuesTokenAssert(assertMessage);
        orderByTokenAssert = new OrderByTokenAssert(sqlCaseType, assertMessage);
        offsetTokenAssert = new OffsetTokenAssert(sqlCaseType, assertMessage);
        rowCountTokenAssert = new RowCountTokenAssert(sqlCaseType, assertMessage);
    }
    
    /**
     * Assert tokens.
     * 
     * @param actual actual tokens
     * @param expected expected tokens
     */
    public void assertTokens(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        tableTokenAssert.assertTableTokens(actual, expected);
        schemaTokenAssert.assertSchemaTokens(actual, expected);
        indexTokenAssert.assertIndexToken(actual, expected);
        itemsTokenAssert.assertItemsToken(actual, expected);
        generatedKeyTokenAssert.assertGeneratedKeyToken(actual, expected);
        insertValuesTokenAssert.assertInsertValuesToken(actual, expected);
        orderByTokenAssert.assertOrderByToken(actual, expected);
        offsetTokenAssert.assertOffsetToken(actual, expected);
        rowCountTokenAssert.assertRowCountToken(actual, expected);
    }
}
