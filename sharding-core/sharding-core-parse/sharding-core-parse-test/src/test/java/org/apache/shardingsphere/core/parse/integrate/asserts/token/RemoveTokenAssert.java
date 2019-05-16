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
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedRemoveToken;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.RemoveToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Remove token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class RemoveTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertRemoveTokens(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        List<RemoveToken> removeTokens = getRemoveTokens(actual);
        assertThat(assertMessage.getFullAssertMessage("Remove tokens size error: "), removeTokens.size(), is(expected.getRemoveTokens().size()));
        int count = 0;
        for (ExpectedRemoveToken each : expected.getRemoveTokens()) {
            assertRemoveToken(removeTokens.get(count), each);
            count++;
        }
    }
    
    private void assertRemoveToken(final RemoveToken actual, final ExpectedRemoveToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Remove token's start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getFullAssertMessage("Remove token's stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
    
    private List<RemoveToken> getRemoveTokens(final Collection<SQLToken> actual) {
        List<RemoveToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof RemoveToken) {
                result.add((RemoveToken) each);
            }
        }
        return result;
    }
}
