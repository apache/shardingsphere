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

package org.apache.shardingsphere.agent.core.mock.material;

import java.io.IOException;
import java.util.List;

/**
 * Have to redefine this class dynamic, so never add `final` modifier.
 */
public class InstanceMaterial {
    
    /**
     * Mock method for testing.
     *
     * @param queues queues
     * @return result
     */
    public String mock(final List<String> queues) {
        queues.add("on");
        return "invocation";
    }
    
    /**
     * Mock method for testing with exception.
     *
     * @param queues queues
     * @throws IOException IO Exception
     */
    public void mockWithException(final List<String> queues) throws IOException {
        throw new IOException();
    }
}
