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
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateFunctionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.type.ExpectedDataTypeSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.CreateFunctionStatementTestCase;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create function statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateFunctionStatementAssert {
    
    /**
     * Assert create function statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create function statement
     * @param expected expected create function statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        assertFunctionName(assertContext, actual, expected);
        assertGlobal(assertContext, actual, expected);
        assertFunctionType(assertContext, actual, expected);
        assertParameterDataTypes(assertContext, actual, expected);
        assertReturnType(assertContext, actual, expected);
        assertIntermediateType(assertContext, actual, expected);
        assertWithParameters(assertContext, actual, expected);
        assertAliasExpression(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertFunctionName(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (null != expected.getFunctionName()) {
            assertTrue(actual.getFunctionName().isPresent(), assertContext.getText("Function name should not be null"));
            assertThat(assertContext.getText("Function name assertion error: "),
                    actual.getFunctionName().get().getIdentifier().getValue(), is(expected.getFunctionName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getFunctionName().get(), expected.getFunctionName());
        }
    }
    
    private static void assertGlobal(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (null != expected.getGlobal()) {
            assertThat(assertContext.getText("Global flag assertion error: "), actual.isGlobal(), is(expected.getGlobal()));
        }
    }
    
    private static void assertFunctionType(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (null != expected.getFunctionType()) {
            assertNotNull(actual.getFunctionType(), assertContext.getText("Function type should not be null"));
            assertThat(assertContext.getText("Function type assertion error: "),
                    actual.getFunctionType().name(), is(expected.getFunctionType()));
        }
    }
    
    private static void assertParameterDataTypes(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual,
                                                 final CreateFunctionStatementTestCase expected) {
        if (!expected.getParameterDataTypes().isEmpty()) {
            List<DataTypeSegment> actualParams = actual.getParameterDataTypes();
            List<ExpectedDataTypeSegment> expectedParams = expected.getParameterDataTypes();
            assertThat(assertContext.getText("Parameter count assertion error: "),
                    actualParams.size(), is(expectedParams.size()));
            for (int i = 0; i < actualParams.size(); i++) {
                assertThat(assertContext.getText(String.format("Parameter %d type assertion error: ", i)),
                        actualParams.get(i).getDataTypeName(), is(expectedParams.get(i).getName()));
            }
        }
    }
    
    private static void assertReturnType(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (null != expected.getReturnType()) {
            assertNotNull(actual.getReturnType(), assertContext.getText("Return type should not be null"));
            assertThat(assertContext.getText("Return type assertion error: "),
                    actual.getReturnType().getDataTypeName(), is(expected.getReturnType().getName()));
        }
    }
    
    private static void assertIntermediateType(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (null != expected.getIntermediateType()) {
            assertNotNull(actual.getIntermediateType(), assertContext.getText("Intermediate type should not be null"));
            assertThat(assertContext.getText("Intermediate type assertion error: "),
                    actual.getIntermediateType().getDataTypeName(), is(expected.getIntermediateType().getName()));
        }
    }
    
    private static void assertWithParameters(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (!expected.getWithParameters().isEmpty()) {
            List<IdentifierValue> actualWithParams = actual.getWithParameters();
            List<String> expectedWithParams = expected.getWithParameters();
            assertThat(assertContext.getText("WITH PARAMETER count assertion error: "),
                    actualWithParams.size(), is(expectedWithParams.size()));
            for (int i = 0; i < actualWithParams.size(); i++) {
                assertThat(assertContext.getText(String.format("WITH PARAMETER %d assertion error: ", i)),
                        actualWithParams.get(i).getValue(), is(expectedWithParams.get(i)));
            }
        }
    }
    
    private static void assertAliasExpression(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (null != expected.getAliasExpression()) {
            assertNotNull(actual.getAliasExpression(), assertContext.getText("Alias expression should not be null"));
            ExpressionAssert.assertExpression(assertContext, actual.getAliasExpression(), expected.getAliasExpression());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisCreateFunctionStatement actual, final CreateFunctionStatementTestCase expected) {
        if (!expected.getProperties().isEmpty()) {
            Map<String, String> actualProps = actual.getProperties();
            List<ExpectedProperty> expectedProps = expected.getProperties();
            assertFalse(actualProps.isEmpty(), assertContext.getText("Properties should not be empty"));
            for (ExpectedProperty expectedProp : expectedProps) {
                assertTrue(actualProps.containsKey(expectedProp.getKey()),
                        assertContext.getText(String.format("Property key '%s' not found", expectedProp.getKey())));
                assertThat(assertContext.getText(String.format("Property '%s' value assertion error: ", expectedProp.getKey())),
                        actualProps.get(expectedProp.getKey()), is(expectedProp.getValue()));
            }
        }
    }
}
