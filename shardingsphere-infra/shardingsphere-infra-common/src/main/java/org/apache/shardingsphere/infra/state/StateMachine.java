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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * State machine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachine {
    
    private static final AtomicReference<StateType> CURRENT_STATE = new AtomicReference<>(StateType.OK);
    
    /**
     * Switch state.
     *
     * @param event state event
     */
    public static void switchState(final StateEvent event) {
        if (StateType.CIRCUIT_BREAK == event.getType() && event.isOn()) {
            CURRENT_STATE.set(StateType.CIRCUIT_BREAK);
            return;
        }
        // TODO check lock state
        CURRENT_STATE.set(StateType.OK);
    }
    
    /**
     * Get current state.
     * 
     * @return current state
     */
    public static StateType getCurrentState() {
        return CURRENT_STATE.get();
    }
}
