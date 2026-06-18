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

package org.apache.shardingsphere.agent.core.preconditions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Agent preconditions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentPreconditions {
    
    /**
     * Check state.
     *
     * @param state state
     * @param errorMessage error message
     * @throws IllegalStateException illegal state exception
     */
    public static void checkState(final boolean state, final String errorMessage) {
        if (!state) {
            throw new IllegalStateException(errorMessage);
        }
    }
    
    /**
     * Check argument.
     *
     * @param condition condition
     * @param errorMessage error message
     * @throws IllegalArgumentException illegal argument exception
     */
    public static void checkArgument(final boolean condition, final String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
