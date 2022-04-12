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

package org.apache.shardingsphere.readwritesplitting.spi;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmPostProcessor;
import org.apache.shardingsphere.spi.type.required.RequiredSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Readwrite splitting type.
 */
public interface ReadwriteSplittingType extends ShardingSphereAlgorithm, RequiredSPI, ShardingSphereAlgorithmPostProcessor {
    
    /**
     * Get write data source.
     *
     * @return write data source
     */
    String getWriteDataSource();
    
    /**
     * Get read data sources.
     * @return read data sources
     */
    List<String> getReadDataSources();
    
    /**
     * Get data sources.
     *
     * @return data sources
     */
    Map<String, String> getDataSources();
    
    /**
     * Get data source mapper.
     *
     * @param name name
     * @return data source mapper
     */
    Map<String, Collection<String>> getDataSourceMapper(String name);
}
