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

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedGeneratedKeyToken;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.test.sql.SQLCaseType;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Generated key token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class GeneratedKeyTokenAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertGeneratedKeyToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        Optional<GeneratedKeyToken> generatedKeyToken = getGeneratedKeyToken(actual);
        if (generatedKeyToken.isPresent()) {
            assertGeneratedKeyToken(generatedKeyToken.get(), expected.getGeneratedKeyToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Generated key token should not exist: "), expected.getGeneratedKeyToken());
        }
    }
    
    private void assertGeneratedKeyToken(final GeneratedKeyToken actual, final ExpectedGeneratedKeyToken expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Generated key token begin position assertion error: "), actual.getBeginPosition(), is(expected.getPlaceholderBeginPosition()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Generated key token begin position assertion error: "), actual.getBeginPosition(), is(expected.getLiteralBeginPosition()));
        }
    }
    
    private Optional<GeneratedKeyToken> getGeneratedKeyToken(final Collection<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof GeneratedKeyToken) {
                return Optional.of((GeneratedKeyToken) each);
            }
        }
        return Optional.absent();
    }
}
