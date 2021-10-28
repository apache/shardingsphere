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

package org.apache.shardingsphere.scaling.core.api;

/**
 * Single table data calculator interface for SPI.
 * <p>
 * SPI implementation will be initialized as new instance every time.
 * </p>
 */
public interface SingleTableDataCalculator {
    
    /**
     * Get algorithm type.
     *
     * @return algorithm type
     */
    String getAlgorithmType();
    
    /**
     * Get database type.
     *
     * @return database type
     */
    String getDatabaseType();
    
    /**
     * Calculate table data, usually checksum.
     *
     * @param dataCalculateParameter data calculate parameter
     * @return calculated result, it will be used to check equality.
     */
    Object dataCalculate(DataCalculateParameter dataCalculateParameter);
}
