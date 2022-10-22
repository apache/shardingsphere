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

package org.apache.shardingsphere.infra.context.cursor;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cursor connection context.
 */
@Getter
public final class CursorConnectionContext implements AutoCloseable {
    
    private final Map<String, List<FetchGroup>> orderByValueGroups = new ConcurrentHashMap<>();
    
    private final Map<String, Long> minGroupRowCounts = new ConcurrentHashMap<>();
    
    private final Map<String, CursorDefinition> cursorDefinitions = new ConcurrentHashMap<>();
    
    private final Map<String, Boolean> executedAllDirections = new ConcurrentHashMap<>();
    
    @Override
    public void close() {
        orderByValueGroups.clear();
        minGroupRowCounts.clear();
        cursorDefinitions.clear();
        executedAllDirections.clear();
    }
    
    /**
     * Remove cursor name.
     * 
     * @param name cursor name
     */
    public void removeCursorName(final String name) {
        orderByValueGroups.remove(name);
        minGroupRowCounts.remove(name);
        cursorDefinitions.remove(name);
        executedAllDirections.remove(name);
    }
}
