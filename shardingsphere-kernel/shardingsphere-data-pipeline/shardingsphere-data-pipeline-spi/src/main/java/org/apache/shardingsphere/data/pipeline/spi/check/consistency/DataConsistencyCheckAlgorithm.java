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

package org.apache.shardingsphere.data.pipeline.spi.check.consistency;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmPostProcessor;

import java.util.Collection;

/**
 * Data consistency check algorithm, SPI.
 */
public interface DataConsistencyCheckAlgorithm extends ShardingSphereAlgorithm, ShardingSphereAlgorithmPostProcessor {
    
    /**
     * Get algorithm description.
     *
     * @return algorithm description
     */
    String getDescription();
    
    /**
     * Get supported database types.
     *
     * @return supported database types
     */
    Collection<String> getSupportedDatabaseTypes();
    
    /**
     * Get algorithm provider.
     *
     * @return algorithm provider
     */
    String getProvider();
    
    /**
     * Get single table data calculator.
     *
     * @param supportedDatabaseType supported database type
     * @return single table data calculator
     */
    SingleTableDataCalculator getSingleTableDataCalculator(String supportedDatabaseType);
}
