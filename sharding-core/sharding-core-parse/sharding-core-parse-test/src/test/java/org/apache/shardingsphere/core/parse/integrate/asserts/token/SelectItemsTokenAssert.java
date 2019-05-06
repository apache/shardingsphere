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
import org.apache.shardingsphere.core.parse.antlr.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SelectItemsToken;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedItemsToken;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Select items token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class SelectItemsTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertSelectItemsToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        Optional<SelectItemsToken> itemsToken = getSelectItemsToken(actual);
        if (itemsToken.isPresent()) {
            assertSelectItemsToken(itemsToken.get(), expected.getItemsToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Select items token should not exist: "), expected.getItemsToken());
        }
    }
    
    private void assertSelectItemsToken(final SelectItemsToken actual, final ExpectedItemsToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Select items token begin position assertion error: "), actual.getStartIndex(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Select items token items assertion error: "), actual.getItems(), is(expected.getItems()));
    }
    
    private Optional<SelectItemsToken> getSelectItemsToken(final Collection<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof SelectItemsToken) {
                return Optional.of((SelectItemsToken) each);
            }
        }
        return Optional.absent();
    }
}
