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

package org.apache.shardingsphere.agent.api.result;

import lombok.Getter;

/**
 * The advice method invocation result.
 */
@Getter
public final class MethodInvocationResult {
    
    private boolean rebased;
    
    private Object result;
    
    /**
     * To replace the origin result.
     *
     * @param result rebase the origin result
     */
    public void rebase(final Object result) {
        rebased = true;
        this.result = result;
    }
}
