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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.database.event;

import lombok.Getter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;

/**
 * Database ack locked event.
 */
@Getter
public final class DatabaseAckLockedEvent implements GovernanceEvent {
    
    private final String database;
    
    private final String lockedInstance;
    
    public DatabaseAckLockedEvent(final String ackLockedName) {
        String[] databaseInstance = LockNodeUtil.parseAckLockName(ackLockedName);
        this.database = databaseInstance[0];
        this.lockedInstance = databaseInstance[1];
    }
}
