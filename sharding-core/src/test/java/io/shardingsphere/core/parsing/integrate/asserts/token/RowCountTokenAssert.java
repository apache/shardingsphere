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
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedRowCountToken;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.RowCountToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.test.sql.SQLCaseType;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Row count token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class RowCountTokenAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertRowCountToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        Optional<RowCountToken> rowCountToken = getRowCountToken(actual);
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertFalse(assertMessage.getFullAssertMessage("Row count token should not exist: "), rowCountToken.isPresent());
            return;
        }
        if (rowCountToken.isPresent()) {
            assertRowCountToken(rowCountToken.get(), expected.getRowCountToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Row count token should not exist: "), expected.getRowCountToken());
        }
    }
    
    private void assertRowCountToken(final RowCountToken actual, final ExpectedRowCountToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Row count token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Row count token row count assertion error: "), actual.getRowCount(), is(expected.getRowCount()));
    }
    
    private Optional<RowCountToken> getRowCountToken(final Collection<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof RowCountToken) {
                return Optional.of((RowCountToken) each);
            }
        }
        return Optional.absent();
    }
}
