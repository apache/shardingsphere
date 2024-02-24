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

package org.apache.shardingsphere.fixture.targeted;

import java.util.List;

public final class TargetObjectFixture {
    
    public TargetObjectFixture(final List<String> queue) {
        queue.add("on constructor");
    }
    
    /**
     * Call instance method.
     *
     * @param queue queue
     */
    public void call(final List<String> queue) {
        queue.add("on instance method");
    }
    
    /**
     * Call instance method when exception thrown.
     *
     * @param queue queue
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public void callWhenExceptionThrown(final List<String> queue) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Call static method.
     *
     * @param queue queue
     */
    public static void staticCall(final List<String> queue) {
        queue.add("on static method");
    }
    
    /**
     * Call static method when exception thrown.
     *
     * @param queue queue
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public static void staticCallWhenExceptionThrown(final List<String> queue) {
        throw new UnsupportedOperationException();
    }
}
