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

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedGeneratedKeyToken;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parse.parser.token.GeneratedKeyToken;
import org.apache.shardingsphere.core.parse.parser.token.SQLToken;
import org.apache.shardingsphere.test.sql.SQLCaseType;

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
            assertThat(assertMessage.getFullAssertMessage("Generated key token begin position assertion error: "), actual.getStartIndex(), is(expected.getPlaceholderBeginPosition()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Generated key token begin position assertion error: "), actual.getStartIndex(), is(expected.getLiteralBeginPosition()));
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
