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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import org.apache.shardingsphere.scaling.core.job.position.PositionManager;

import java.util.Map;

/**
 * Resume from break-point manager interface.
 */
public interface ResumeBreakPointManager {
    
    /**
     * If has resumable data.
     *
     * @return is resumable
     */
    boolean isResumable();
    
    /**
     * Get inventory position map.
     *
     * @return inventory position map
     */
    Map<String, PositionManager> getInventoryPositionManagerMap();
    
    /**
     * Get incremental position map.
     *
     * @return incremental position map
     */
    Map<String, PositionManager> getIncrementalPositionManagerMap();
    
    /**
     * Get position.
     *
     * @param path path
     * @return data
     */
    String getPosition(String path);
    
    /**
     * Persist position immediately.
     */
    void persistPosition();
    
    /**
     * Persist position.
     *
     * @param path path
     * @param data data
     */
    void persistPosition(String path, String data);
    
    /**
     * Close this manager.
     */
    void close();
}
