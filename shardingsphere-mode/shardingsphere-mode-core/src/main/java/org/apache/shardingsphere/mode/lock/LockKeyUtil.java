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

package org.apache.shardingsphere.mode.lock;

/**
 * Lock key util.
 */
public final class LockKeyUtil {
    
    private static final String PATH_DELIMITER = "/";
    
    private static final String LOCK_ROOT = "lock";
    
    private static final String LOCKS_NODE = "locks";
    
    private static final String LOCKS_NODE_EXCLUSIVE = "exclusive";
    
    /**
     * Generate exclusive lock key.
     *
     * @param lockName locks name
     * @return locks name
     */
    public static String generateExclusiveLockKey(final String lockName) {
        return generateLocksNodePath(LOCKS_NODE_EXCLUSIVE) + "/" + lockName;
    }
    
    private static String generateLocksNodePath(final String lockPath) {
        return PATH_DELIMITER + LOCK_ROOT + PATH_DELIMITER + lockPath + PATH_DELIMITER + LOCKS_NODE;
    }
    
    /**
     * Generate lock key leases.
     *
     * @param lockKey lock key
     * @return locks name
     */
    public static String generateLockKeyLeases(final String lockKey) {
        return lockKey + "/leases";
    }
}
