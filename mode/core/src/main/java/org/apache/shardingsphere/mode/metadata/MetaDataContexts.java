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

package org.apache.shardingsphere.mode.metadata;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Meta data contexts.
 */
public final class MetaDataContexts {
    
    private final AtomicReference<ShardingSphereMetaData> metaData = new AtomicReference<>();
    
    private final AtomicReference<ShardingSphereStatistics> statistics = new AtomicReference<>();
    
    public MetaDataContexts(final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        this.metaData.set(metaData);
        this.statistics.set(statistics);
    }
    
    /**
     * Get ShardingSphere meta data.
     *
     * @return got meta data
     */
    public ShardingSphereMetaData getMetaData() {
        return metaData.get();
    }
    
    /**
     * Set ShardingSphere meta data.
     *
     * @param metaData meta data
     */
    public void setMetaData(final ShardingSphereMetaData metaData) {
        this.metaData.set(metaData);
    }
    
    /**
     * Get ShardingSphere statistics.
     *
     * @return got meta data statistics
     */
    public ShardingSphereStatistics getStatistics() {
        return statistics.get();
    }
    
    /**
     * Set ShardingSphere statistics.
     *
     * @param statistics statistics
     */
    public void setStatistics(final ShardingSphereStatistics statistics) {
        this.statistics.set(statistics);
    }
}
