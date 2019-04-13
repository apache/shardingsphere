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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedEncryptColumnToken;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
@RequiredArgsConstructor
public final class EncryptColumnTokenAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertEncryptColumnsToken(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        List<EncryptColumnToken> encryptColumnTokens = getEncryptColumnTokens(actual);
        assertThat(assertMessage.getFullAssertMessage("Encrypt column tokens size error: "), encryptColumnTokens.size(), is(expected.getEncryptColumnTokens().size()));
        int count = 0;
        for (ExpectedEncryptColumnToken each : expected.getEncryptColumnTokens()) {
            assertEncryptColumnToken(encryptColumnTokens.get(count), each);
            count++;
        }
    }
    
    private void assertEncryptColumnToken(final EncryptColumnToken actual, final ExpectedEncryptColumnToken expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Encrypt column start index for placeholder assertion error: "), actual.getStartIndex(), is(expected.getStartIndexForPlaceholder()));
            assertThat(assertMessage.getFullAssertMessage("Encrypt column stop index for placeholder assertion error: "), actual.getStopIndex(), is(expected.getStopIndexForPlaceholder()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Encrypt column start index for literal assertion error: "), actual.getStartIndex(), is(expected.getStartIndexForLiteral()));
            assertThat(assertMessage.getFullAssertMessage("Encrypt column stop index for literal assertion error: "), actual.getStopIndex(), is(expected.getStopIndexForLiteral()));
        }
        assertNotNull(assertMessage.getFullAssertMessage("Encrypt column does not exist assertion error: "), expected.getColumn());
        assertTrue(assertMessage.getFullAssertMessage("Missing encrypt column assertion error: "), actual.getColumn() != null);
        assertThat(assertMessage.getFullAssertMessage("Encrypt column name assertion error: "), actual.getColumn().getName(), is(expected.getColumn().getName()));
        assertThat(assertMessage.getFullAssertMessage("Encrypt column table name assertion error: "), actual.getColumn().getTableName(), is(expected.getColumn().getTableName()));
        assertThat(assertMessage.getFullAssertMessage("Encrypt column isInWhere assertion error: "), actual.isInWhere(), is(expected.isInWhere()));
    }
    
    private List<EncryptColumnToken> getEncryptColumnTokens(final Collection<SQLToken> actual) {
        List<EncryptColumnToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof EncryptColumnToken) {
                result.add((EncryptColumnToken) each);
            }
        }
        return result;
    }
}
