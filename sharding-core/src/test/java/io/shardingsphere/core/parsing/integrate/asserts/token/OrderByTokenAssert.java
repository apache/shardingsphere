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
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedOrderByToken;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.test.sql.SQLCaseType;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Order by token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class OrderByTokenAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertOrderByToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        Optional<OrderByToken> orderByToken = getOrderByToken(actual);
        if (orderByToken.isPresent()) {
            assertOrderByToken(orderByToken.get(), expected.getOrderByToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Order by token should not exist: "), expected.getOrderByToken());
        }
    }
    
    private void assertOrderByToken(final OrderByToken actual, final ExpectedOrderByToken expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Order by token begin position assertion error: "), actual.getBeginPosition(), is(expected.getPlaceholderBeginPosition()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Order by token begin position assertion error: "), actual.getBeginPosition(), is(expected.getLiteralBeginPosition()));
        }
    }
    
    private Optional<OrderByToken> getOrderByToken(final Collection<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof OrderByToken) {
                return Optional.of((OrderByToken) each);
            }
        }
        return Optional.absent();
    }
}
