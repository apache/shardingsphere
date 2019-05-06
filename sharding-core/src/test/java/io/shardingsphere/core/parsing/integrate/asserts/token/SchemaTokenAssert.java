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
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedSchemaToken;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.core.parsing.parser.token.SchemaToken;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Schema token assert.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
final class SchemaTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertSchemaTokens(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        List<SchemaToken> schemaTokens = getSchemaTokens(actual);
        assertThat(assertMessage.getFullAssertMessage("Schema tokens size error: "), schemaTokens.size(), is(expected.getSchemaTokens().size()));
        int count = 0;
        for (ExpectedSchemaToken each : expected.getSchemaTokens()) {
            assertSchemaToken(schemaTokens.get(count), each);
            count++;
        }
    }
    
    private void assertSchemaToken(final SchemaToken actual, final ExpectedSchemaToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Schema tokens begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Schema tokens original literals assertion error: "), actual.getOriginalLiterals(), is(expected.getOriginalLiterals()));
        assertThat(assertMessage.getFullAssertMessage("Schema tokens table name assertion error: "), actual.getTableName(), is(expected.getTableName()));
    }
    
    private List<SchemaToken> getSchemaTokens(final Collection<SQLToken> actual) {
        List<SchemaToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof SchemaToken) {
                result.add((SchemaToken) each);
            }
        }
        return result;
    }
}
