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

package org.apache.shardingsphere.encrypt.rewrite.token.pojo;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EncryptPredicateFunctionRightValueTokenTest {
    
    @Test
    void assertToStringWithSimpleFunction() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>(3, 1F);
        indexValues.put(0, "%");
        indexValues.put(1, "abc");
        indexValues.put(2, "%");
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "CONCAT", "('%','abc','%')");
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "%"));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "abc"));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "%"));
        EncryptPredicateFunctionRightValueToken actual =
                new EncryptPredicateFunctionRightValueToken(0, 0, functionSegment.getFunctionName(), functionSegment.getParameters(), indexValues, Collections.emptyList());
        assertThat(actual.toString(), is("CONCAT('%', 'abc', '%')"));
    }
    
    @Test
    void assertToStringWithNestedFunction() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>(3, 1F);
        indexValues.put(0, "%");
        indexValues.put(1, "abc");
        indexValues.put(2, "%");
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "CONCAT", "('%',CONCAT('abc','%'))");
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "%"));
        FunctionSegment nestedFunctionSegment = new FunctionSegment(0, 0, "CONCAT", "('abc','%')");
        nestedFunctionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "abc"));
        nestedFunctionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "%"));
        functionSegment.getParameters().add(nestedFunctionSegment);
        EncryptPredicateFunctionRightValueToken actual =
                new EncryptPredicateFunctionRightValueToken(0, 0, functionSegment.getFunctionName(), functionSegment.getParameters(), indexValues, Collections.emptyList());
        assertThat(actual.toString(), is("CONCAT('%', CONCAT('abc', '%'))"));
    }
}
