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

package org.apache.shardingsphere.data.pipeline.core.task.progress;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Incremental task progress.
 */
public final class IncrementalTaskProgress implements TaskProgress {
    
    private final AtomicReference<IngestPosition> position = new AtomicReference<>();
    
    private final AtomicReference<IncrementalTaskDelay> incrementalTaskDelay = new AtomicReference<>();
    
    public IncrementalTaskProgress(final IngestPosition position) {
        this.position.set(position);
        incrementalTaskDelay.set(new IncrementalTaskDelay());
    }
    
    @Override
    public IngestPosition getPosition() {
        return position.get();
    }
    
    /**
     * Set position.
     * 
     * @param position position
     */
    public void setPosition(final IngestPosition position) {
        this.position.set(position);
    }
    
    /**
     * Get incremental task delay.
     * 
     * @return incremental task delay
     */
    public IncrementalTaskDelay getIncrementalTaskDelay() {
        return incrementalTaskDelay.get();
    }
    
    /**
     * Set incremental task delay.
     * 
     * @param incrementalTaskDelay incremental task delay
     */
    public void setIncrementalTaskDelay(final IncrementalTaskDelay incrementalTaskDelay) {
        this.incrementalTaskDelay.set(incrementalTaskDelay);
    }
}
