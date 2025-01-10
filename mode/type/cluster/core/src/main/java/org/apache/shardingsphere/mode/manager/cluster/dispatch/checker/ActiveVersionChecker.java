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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Active version checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActiveVersionChecker {
    
    /**
     * Check active version.
     *
     * @param contextManager context manager
     * @param event data changed event
     */
    public static void checkActiveVersion(final ContextManager contextManager, final DataChangedEvent event) {
        ShardingSpherePreconditions.checkState(event.getValue().equals(contextManager.getPersistServiceFacade().getRepository().query(event.getKey())),
                () -> new IllegalStateException(String.format("Invalid active version: %s of key: %s", event.getValue(), event.getKey())));
    }
}
