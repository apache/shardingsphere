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

package io.shardingsphere.core.parsing.integrate.asserts.transaction;

import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import lombok.RequiredArgsConstructor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Transaction assert.
 *
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class TransactionAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert transaction operation type.
     *
     * @param actual actual transaction operation type
     * @param expected expected transaction operation type
     */
    public void assertTransactionOperationType(final TransactionOperationType actual, final TransactionOperationType expected) {
        assertThat(assertMessage.getFullAssertMessage("Transaction operation type assertion error: "), actual, is(expected));
    }
}
