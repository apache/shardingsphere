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

package org.apache.shardingsphere.mode.manager.standalone.exclusive;

import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorContext;
import org.apache.shardingsphere.mode.retry.RetryExecutor;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Standalone lock context.
 */
public final class StandaloneExclusiveOperatorContext implements ExclusiveOperatorContext {
    
    private final Collection<String> exclusiveOperationKeys = new CopyOnWriteArraySet<>();
    
    @Override
    public boolean start(final String operationKey, final long timeoutMillis) {
        return new RetryExecutor(timeoutMillis, 50L).execute(arg -> exclusiveOperationKeys.add(operationKey), null);
    }
    
    @Override
    public void stop(final String operationKey) {
        exclusiveOperationKeys.remove(operationKey);
    }
}
