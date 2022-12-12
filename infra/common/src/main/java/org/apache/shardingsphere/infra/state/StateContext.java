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

package org.apache.shardingsphere.infra.state;

import java.util.Collections;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * State context.
 */
public final class StateContext {
    
    private final Deque<StateType> currentState = new ConcurrentLinkedDeque<>(Collections.singleton(StateType.OK));
    
    /**
     * Switch state.
     *
     * @param type state type
     * @param on true if state type is valid, false if not
     */
    public void switchState(final StateType type, final boolean on) {
        if (on) {
            currentState.push(type);
        } else {
            if (getCurrentState().equals(type)) {
                recoverState();
            }
        }
    }
    
    private void recoverState() {
        currentState.pop();
    }
    
    /**
     * Get current state.
     * 
     * @return current state type
     */
    public StateType getCurrentState() {
        return Optional.ofNullable(currentState.peek()).orElse(StateType.OK);
    }
}
