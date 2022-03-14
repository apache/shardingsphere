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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MethodInvocationRecorderTest {
    
    @Test
    public void assertRecordMethodInvocationSuccess() {
        MethodInvocationRecorder methodInvocationRecorder = new MethodInvocationRecorder();
        methodInvocationRecorder.record(List.class, "isEmpty", new Class[]{}, new Object[]{});
        methodInvocationRecorder.replay(Collections.emptyList());
    }
    
    @Test(expected = NoSuchMethodException.class)
    public void assertRecordMethodInvocationFailure() {
        new MethodInvocationRecorder().record(String.class, "none", new Class[]{}, new Object[]{});
    }
    
    @Test
    public void assertRecordSameMethodTwice() {
        MethodInvocationRecorder methodInvocationRecorder = new MethodInvocationRecorder();
        methodInvocationRecorder.record(List.class, "add", new Class[]{Object.class}, new Object[]{1});
        methodInvocationRecorder.record(List.class, "add", new Class[]{Object.class}, new Object[]{2});
        List<Integer> actual = new ArrayList<>();
        methodInvocationRecorder.replay(actual);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(2));
    }
}
