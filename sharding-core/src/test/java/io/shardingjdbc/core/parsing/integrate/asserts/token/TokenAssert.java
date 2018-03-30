/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.integrate.asserts.token;

import io.shardingjdbc.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.test.sql.SQLCaseType;

import java.util.List;

/**
 * Token assert.
 *
 * @author zhangliang
 */
public final class TokenAssert {
    
    private final TableTokenAssert tableTokenAssert;
    
    private final IndexTokenAssert indexTokenAssert;
    
    private final ItemsTokenAssert itemsTokenAssert;
    
    private final GeneratedKeyTokenAssert generatedKeyTokenAssert;
    
    private final MultipleInsertValuesTokenAssert multipleInsertValuesTokenAssert;
    
    private final OrderByTokenAssert orderByTokenAssert;
    
    private final OffsetTokenAssert offsetTokenAssert;
    
    private final RowCountTokenAssert rowCountTokenAssert;
    
    public TokenAssert(final SQLCaseType sqlCaseType, final SQLStatementAssertMessage assertMessage) {
        tableTokenAssert = new TableTokenAssert(assertMessage);
        indexTokenAssert = new IndexTokenAssert(assertMessage);
        itemsTokenAssert = new ItemsTokenAssert(assertMessage);
        generatedKeyTokenAssert = new GeneratedKeyTokenAssert(sqlCaseType, assertMessage);
        multipleInsertValuesTokenAssert = new MultipleInsertValuesTokenAssert(assertMessage);
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
    public void assertTokens(final List<SQLToken> actual, final ExpectedTokens expected) {
        tableTokenAssert.assertTableTokens(actual, expected);
        indexTokenAssert.assertIndexToken(actual, expected);
        itemsTokenAssert.assertItemsToken(actual, expected);
        generatedKeyTokenAssert.assertGeneratedKeyToken(actual, expected);
        multipleInsertValuesTokenAssert.assertMultipleInsertValuesToken(actual, expected);
        orderByTokenAssert.assertOrderByToken(actual, expected);
        offsetTokenAssert.assertOffsetToken(actual, expected);
        rowCountTokenAssert.assertRowCountToken(actual, expected);
    }
}
