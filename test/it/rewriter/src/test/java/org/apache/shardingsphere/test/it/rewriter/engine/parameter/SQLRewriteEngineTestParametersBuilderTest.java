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

package org.apache.shardingsphere.test.it.rewriter.engine.parameter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class SQLRewriteEngineTestParametersBuilderTest {
    
    @Test
    void assertCreateInputParameterWithRegularInteger() throws Exception {
        assertThat(invokeCreateInputParameter("1"), is(1));
    }
    
    @Test
    void assertCreateInputParameterWithLargeNumber() throws Exception {
        assertThat(invokeCreateInputParameter("1000"), is(1000));
    }
    
    @Test
    void assertCreateInputParameterWithLeadingZeroDigits() throws Exception {
        Object result = invokeCreateInputParameter("04844448888");
        assertThat(result, isA(Long.class));
        assertThat(result, is(4844448888L));
    }
    
    @Test
    void assertCreateInputParameterWithLeadingZeroValidOctalLikeDigits() throws Exception {
        Object result = invokeCreateInputParameter("04100001111");
        assertThat(result, isA(Long.class));
        assertThat(result, is(4100001111L));
    }
    
    @Test
    void assertCreateInputParameterWithSingleZero() throws Exception {
        assertThat(invokeCreateInputParameter("0"), is(0));
    }
    
    @Test
    void assertCreateInputParameterWithString() throws Exception {
        assertThat(invokeCreateInputParameter("aaa"), is("aaa"));
    }
    
    @Test
    void assertCreateInputParameterWithNull() throws Exception {
        assertThat(invokeCreateInputParameter("NULL"), is(org.hamcrest.Matchers.nullValue()));
    }
    
    @Test
    void assertCreateInputParameterWithLongOverflow() throws Exception {
        Object result = invokeCreateInputParameter("99999999999999999999");
        assertThat(result, isA(String.class));
    }
    
    private static Object invokeCreateInputParameter(final String inputParam) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = SQLRewriteEngineTestParametersBuilder.class.getDeclaredMethod("createInputParameter", String.class);
        method.setAccessible(true);
        return method.invoke(null, inputParam);
    }
}
