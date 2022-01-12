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

package org.apache.shardingsphere.data.pipeline.core.datasource.creator;

import org.apache.shardingsphere.spi.singleton.SingletonSPIRegistry;

import java.util.Map;

/**
 * Pipeline data source creator factory.
 */
public final class PipelineDataSourceCreatorFactory {
    
    private static final Map<String, PipelineDataSourceCreator> DATA_SOURCE_CREATOR_MAP = SingletonSPIRegistry.getTypedSingletonInstancesMap(PipelineDataSourceCreator.class);
    
    /**
     * Get pipeline data source creator instance.
     * 
     * @param type pipeline data source creator type
     * @return pipeline data source creator instance
     */
    public static PipelineDataSourceCreator getInstance(final String type) {
        return DATA_SOURCE_CREATOR_MAP.get(type);
    }
}
