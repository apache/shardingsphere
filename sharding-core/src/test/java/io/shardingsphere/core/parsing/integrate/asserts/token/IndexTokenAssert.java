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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedIndexToken;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import lombok.RequiredArgsConstructor;

/**
 * Index token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class IndexTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertIndexToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        List<IndexToken> indexTokens = getIndexTokens(actual);
        
        assertThat(assertMessage.getFullAssertMessage("Index tokens size error: "), indexTokens.size(), is(expected.getIndexTokens().size()));
        int count = 0;
        for (ExpectedIndexToken each : expected.getIndexTokens()) {
            assertIndexToken(indexTokens.get(count), each);
            count++;
        }
    }
    
    private void assertIndexToken(final IndexToken actual, final ExpectedIndexToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Index token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Index token original literals assertion error: "), actual.getOriginalLiterals(), is(expected.getOriginalLiterals()));
        assertThat(assertMessage.getFullAssertMessage("Index token table name assertion error: "), actual.getTableName(), is(expected.getTableName()));
    }
    
    private List<IndexToken> getIndexTokens(final Collection<SQLToken> actual) {
        List<IndexToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof IndexToken) {
                result.add((IndexToken) each);
            }
        }
        return result;
    }
}
