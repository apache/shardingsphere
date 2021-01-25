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

package org.apache.shardingsphere.driver.jdbc.adapter.invocation;

import org.junit.Test;

public final class JdbcMethodInvocationTest {
    
    @Test
    public void assertInvokeSuccess() throws NoSuchMethodException {
        JdbcMethodInvocation actual = new JdbcMethodInvocation(String.class.getMethod("length"), new Object[] {});
        actual.invoke("");
    }
    
    @Test(expected = IllegalAccessException.class)
    public void assertInvokeFailure() throws NoSuchMethodException {
        JdbcMethodInvocation actual = new JdbcMethodInvocation(String.class.getDeclaredMethod("indexOfSupplementary", int.class, int.class), new Object[] {1, 1});
        actual.invoke("");
    }
}
