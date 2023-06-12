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

package org.apache.shardingsphere.infra.state.instance;

import java.util.Collections;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Instance state context.
 */
public final class InstanceStateContext {
    
    private final Deque<InstanceState> currentState = new ConcurrentLinkedDeque<>(Collections.singleton(InstanceState.OK));
    
    /**
     * Switch state.
     * 
     * @param state state
     */
    public void switchToValidState(final InstanceState state) {
        currentState.push(state);
    }
    
    /**
     * Switch state.
     *
     * @param state state
     */
    public void switchToInvalidState(final InstanceState state) {
        if (getCurrentState() == state) {
            recoverState();
        }
    }
    
    private void recoverState() {
        currentState.pop();
    }
    
    /**
     * Get current state.
     * 
     * @return current state
     */
    public InstanceState getCurrentState() {
        return Optional.ofNullable(currentState.peek()).orElse(InstanceState.OK);
    }
}
