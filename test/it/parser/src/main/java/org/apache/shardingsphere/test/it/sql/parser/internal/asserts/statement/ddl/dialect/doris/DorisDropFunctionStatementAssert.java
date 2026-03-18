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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropFunctionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.type.ExpectedDataTypeSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisDropFunctionStatementTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drop function statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDropFunctionStatementAssert {
    
    /**
     * Assert drop function statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop function statement
     * @param expected expected drop function statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisDropFunctionStatement actual, final DorisDropFunctionStatementTestCase expected) {
        assertFunctionName(assertContext, actual, expected);
        assertGlobal(assertContext, actual, expected);
        assertParameterDataTypes(assertContext, actual, expected);
    }
    
    private static void assertFunctionName(final SQLCaseAssertContext assertContext, final DorisDropFunctionStatement actual, final DorisDropFunctionStatementTestCase expected) {
        if (null != expected.getFunctionName()) {
            assertTrue(actual.getFunctionName().isPresent(), assertContext.getText("Function name should not be null"));
            assertThat(assertContext.getText("Function name assertion error: "), actual.getFunctionName().get().getIdentifier().getValue(), is(expected.getFunctionName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getFunctionName().get(), expected.getFunctionName());
        }
    }
    
    private static void assertGlobal(final SQLCaseAssertContext assertContext, final DorisDropFunctionStatement actual, final DorisDropFunctionStatementTestCase expected) {
        if (null != expected.getGlobal()) {
            assertThat(assertContext.getText("Global flag assertion error: "), actual.isGlobal(), is(expected.getGlobal()));
        }
    }
    
    private static void assertParameterDataTypes(final SQLCaseAssertContext assertContext, final DorisDropFunctionStatement actual, final DorisDropFunctionStatementTestCase expected) {
        if (!expected.getParameterDataTypes().isEmpty()) {
            List<DataTypeSegment> actualParams = actual.getParameterDataTypes();
            List<ExpectedDataTypeSegment> expectedParams = expected.getParameterDataTypes();
            assertThat(assertContext.getText("Parameter count assertion error: "), actualParams.size(), is(expectedParams.size()));
            for (int i = 0; i < actualParams.size(); i++) {
                assertThat(assertContext.getText(String.format("Parameter %d type assertion error: ", i)), actualParams.get(i).getDataTypeName(), is(expectedParams.get(i).getName()));
            }
        }
    }
}
