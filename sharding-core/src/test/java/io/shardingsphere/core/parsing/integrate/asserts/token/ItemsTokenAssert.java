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
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedItemsToken;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Items token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class ItemsTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertItemsToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        Optional<ItemsToken> itemsToken = getItemsToken(actual);
        if (itemsToken.isPresent()) {
            assertItemsToken(itemsToken.get(), expected.getItemsToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Items token should not exist: "), expected.getItemsToken());
        }
    }
    
    private void assertItemsToken(final ItemsToken actual, final ExpectedItemsToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Items token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Items token items assertion error: "), actual.getItems(), is(expected.getItems()));
    }
    
    private Optional<ItemsToken> getItemsToken(final Collection<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof ItemsToken) {
                return Optional.of((ItemsToken) each);
            }
        }
        return Optional.absent();
    }
}
