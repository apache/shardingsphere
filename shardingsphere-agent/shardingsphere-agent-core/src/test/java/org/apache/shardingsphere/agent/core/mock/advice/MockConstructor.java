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

package org.apache.shardingsphere.agent.core.mock.advice;

import org.apache.shardingsphere.agent.api.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.advice.TargetObject;

import java.util.List;

public final class MockConstructor implements ConstructorAdvice {
    
    private final List<String> queue;
    
    public MockConstructor() {
        this(null);
    }
    
    public MockConstructor(final List<String> queue) {
        this.queue = queue;
    }
    
    @Override
    public void onConstructor(final TargetObject target, final Object[] args) {
        final List<String> list;
        if (queue == null) {
            list = (List<String>) args[0];
        } else {
            list = queue;
        }
        list.add("on constructor");
    }
    
}
