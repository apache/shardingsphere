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

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedMultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingjdbc.core.parsing.parser.token.MultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Multiple insert values token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
class MultipleInsertValuesTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertMultipleInsertValuesToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<MultipleInsertValuesToken> multipleInsertValuesToken = getMultipleInsertValuesToken(actual);
        if (multipleInsertValuesToken.isPresent()) {
            assertMultipleInsertValuesToken(multipleInsertValuesToken.get(), expected.getMultipleInsertValuesToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Multiple insert values token should not exist: "), expected.getMultipleInsertValuesToken());
        }
    }
    
    private void assertMultipleInsertValuesToken(final MultipleInsertValuesToken actual, final ExpectedMultipleInsertValuesToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Multiple insert values token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Multiple insert values token values assertion error: "), actual.getValues(), is(expected.getValues()));
    }
    
    private Optional<MultipleInsertValuesToken> getMultipleInsertValuesToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof MultipleInsertValuesToken) {
                return Optional.of((MultipleInsertValuesToken) each);
            }
        }
        return Optional.absent();
    }
}
