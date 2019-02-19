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

package org.apache.shardingsphere.core.parsing.integrate.asserts.token;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedInsertValuesToken;
import org.apache.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken.InsertColumnValue;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Multiple insert values token assert.
 *
 * @author maxiaoguang
 */
@RequiredArgsConstructor
final class InsertValuesTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertInsertValuesToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        Optional<InsertValuesToken> insertValuesToken = getInsertValuesToken(actual);
        if (insertValuesToken.isPresent()) {
            assertInsertValuesToken(insertValuesToken.get(), expected.getInsertValuesToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Insert values token should not exist: "), expected.getInsertValuesToken());
        }
    }
    
    private void assertInsertValuesToken(final InsertValuesToken actual, final ExpectedInsertValuesToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Insert values token begin position assertion error: "), actual.getStartIndex(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Insert values type assertion error: "), actual.getType().name(), is(expected.getType()));
        assertThat(assertMessage.getFullAssertMessage("Insert values column names assertion error: "), Joiner.on(", ").join(actual.getColumnNames()), is(expected.getColumnNames()));
        for (int i = 0; i < actual.getColumnValues().size(); i++) {
            assertThat(assertMessage.getFullAssertMessage("Insert column values assertion error: "), 
                    getInsertValues(actual.getColumnValues().get(i), actual.getColumnNames().size()), is(expected.getInsertValues().get(i).getValues()));
        }
    }
    
    private String getInsertValues(final InsertColumnValue insertColumnValue, final int columnSize) {
        List<String> result = new LinkedList<>();
        for (int i = 0; i < columnSize; i++) {
            result.add(insertColumnValue.getColumnValue(0));
        }
        return Joiner.on(", ").join(result);
    }
    
    private Optional<InsertValuesToken> getInsertValuesToken(final Collection<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof InsertValuesToken) {
                return Optional.of((InsertValuesToken) each);
            }
        }
        return Optional.absent();
    }
}
