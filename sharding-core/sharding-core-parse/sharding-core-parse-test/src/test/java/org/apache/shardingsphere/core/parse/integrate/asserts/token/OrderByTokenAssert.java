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
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedOrderByToken;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parse.parser.token.OrderByToken;
import org.apache.shardingsphere.core.parse.parser.token.SQLToken;
import org.apache.shardingsphere.test.sql.SQLCaseType;

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
            assertThat(assertMessage.getFullAssertMessage("Order by token begin position assertion error: "), actual.getStartIndex(), is(expected.getPlaceholderBeginPosition()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Order by token begin position assertion error: "), actual.getStartIndex(), is(expected.getLiteralBeginPosition()));
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
