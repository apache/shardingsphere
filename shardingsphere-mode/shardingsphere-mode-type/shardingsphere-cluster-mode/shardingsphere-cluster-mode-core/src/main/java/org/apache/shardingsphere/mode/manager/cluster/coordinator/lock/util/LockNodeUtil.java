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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Lock node util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockNodeUtil {
    
    private static final String LOCK_NAME_DELIMITER = "#@#";
    
    /**
     * Generate Lock leases node path.
     *
     * @param lockName lock name
     * @return lock leases
     */
    public static String generateLockLeasesNodePath(final String lockName) {
        return lockName + "/leases";
    }
    
    /**
     * Generate Lock sequence node path.
     *
     * @param lockName lock name
     * @return lock leases
     */
    public static String generateLockSequenceNodePath(final String lockName) {
        return lockName + "/sequence";
    }
    
    /**
     * generate ack path name.
     *
     * @param lockName lock name
     * @return ack path name
     */
    public static String generateAckPathName(final String lockName) {
        return lockName + "/ack";
    }
    
    /**
     * Parse ack locked name.
     *
     * @param ackLockedName ack locked name
     * @return string array of locked name and instance id
     */
    public static String[] parseAckLockName(final String ackLockedName) {
        return ackLockedName.trim().split(LOCK_NAME_DELIMITER);
    }
}
