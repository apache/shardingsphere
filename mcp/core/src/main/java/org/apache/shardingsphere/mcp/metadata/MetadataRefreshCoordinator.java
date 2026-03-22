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

package org.apache.shardingsphere.mcp.metadata;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinate metadata visibility refresh timing.
 */
public final class MetadataRefreshCoordinator {
    
    private static final long GLOBAL_VISIBILITY_SLA_MILLIS = 60000L;
    
    private final Map<String, Long> globalRefreshTimes = new ConcurrentHashMap<>();
    
    private final Map<String, Set<String>> sessionVisibleDatabases = new ConcurrentHashMap<>();
    
    /**
     * Mark one committed structure change.
     *
     * @param sessionId session identifier
     * @param database logical database name
     */
    public void markStructureChangeCommitted(final String sessionId, final String database) {
        markCommittedChange(sessionId, database, System.currentTimeMillis());
    }
    
    /**
     * Mark one committed DCL change.
     *
     * @param sessionId session identifier
     * @param database logical database name
     */
    public void markDclChangeCommitted(final String sessionId, final String database) {
        markCommittedChange(sessionId, database, System.currentTimeMillis());
    }
    
    /**
     * Determine whether the current session should see the refreshed metadata immediately.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @return {@code true} when the session-local view is refreshed
     */
    public boolean isVisibleToSession(final String sessionId, final String database) {
        return sessionVisibleDatabases.getOrDefault(normalize(sessionId, "sessionId"), Set.of()).contains(normalize(database, "database"));
    }
    
    /**
     * Determine whether the current global refresh time remains within the 60-second SLA.
     *
     * @param database logical database name
     * @param nowMillis current time
     * @return {@code true} when the last committed change remains within the SLA window
     */
    public boolean isGlobalVisibilityWithinSla(final String database, final long nowMillis) {
        Long refreshedAt = globalRefreshTimes.get(normalize(database, "database"));
        return null != refreshedAt && nowMillis - refreshedAt <= GLOBAL_VISIBILITY_SLA_MILLIS;
    }
    
    /**
     * Get the last global refresh time for one logical database.
     *
     * @param database logical database name
     * @return last refresh timestamp or zero when absent
     */
    public long getLastGlobalRefreshTimeMillis(final String database) {
        Long result = globalRefreshTimes.get(normalize(database, "database"));
        return null == result ? 0L : result;
    }
    
    /**
     * Clear session-local refresh visibility state.
     *
     * @param sessionId session identifier
     */
    public void clearSession(final String sessionId) {
        sessionVisibleDatabases.remove(normalize(sessionId, "sessionId"));
    }
    
    private void markCommittedChange(final String sessionId, final String database, final long refreshTimeMillis) {
        String actualSessionId = normalize(sessionId, "sessionId");
        String actualDatabase = normalize(database, "database");
        sessionVisibleDatabases.computeIfAbsent(actualSessionId, key -> ConcurrentHashMap.newKeySet()).add(actualDatabase);
        globalRefreshTimes.put(actualDatabase, refreshTimeMillis);
    }
    
    private String normalize(final String value, final String fieldName) {
        return Objects.requireNonNull(value, fieldName + " cannot be null").trim();
    }
}
