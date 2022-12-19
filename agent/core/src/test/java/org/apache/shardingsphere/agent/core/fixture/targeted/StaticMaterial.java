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

package org.apache.shardingsphere.agent.core.fixture.targeted;

import java.io.IOException;
import java.util.List;

public final class StaticMaterial {
    
    /**
     * Mock static method for testing.
     *
     * @param queue queue
     * @return result
     */
    public static String staticMock(final List<String> queue) {
        queue.add("on");
        return "static invocation";
    }
    
    /**
     * Mock static method for testing with exception.
     *
     * @param queue queue
     * @throws IOException IO Exception
     */
    public static void staticMockWithException(final List<String> queue) throws IOException {
        throw new IOException();
    }
}
