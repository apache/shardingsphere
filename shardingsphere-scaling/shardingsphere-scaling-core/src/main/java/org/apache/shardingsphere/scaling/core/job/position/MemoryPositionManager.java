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

package org.apache.shardingsphere.scaling.core.job.position;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory implement of position manager.
 */
public class MemoryPositionManager extends AbstractPositionManager {

    private static final Map<String, Position> POSITION_MAP = new ConcurrentHashMap<>();

    public MemoryPositionManager(final String taskId) {
        super(taskId);
    }

    @Override
    protected Position getPosition(final String taskId) {
        return POSITION_MAP.get(taskId);
    }

    @Override
    protected void updatePosition(final String taskId, final Position position) {
        POSITION_MAP.put(taskId, position);
    }
}
