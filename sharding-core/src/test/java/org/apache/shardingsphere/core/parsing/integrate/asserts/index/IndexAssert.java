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

package org.apache.shardingsphere.core.parsing.integrate.asserts.index;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Index assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class IndexAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert parameters index.
     * 
     * @param actual actual parameters index
     * @param expected expected parameters index
     */
    public void assertParametersIndex(final int actual, final int expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Parameters index assertion error: "), actual, is(expected));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Parameters index assertion error: "), actual, is(0));
        }
    }
}
