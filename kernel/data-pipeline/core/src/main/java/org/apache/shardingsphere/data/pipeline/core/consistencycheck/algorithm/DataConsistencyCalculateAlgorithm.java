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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.algorithm.ShardingSphereAlgorithm;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Data consistency calculate algorithm.
 */
public interface DataConsistencyCalculateAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Calculate data for consistency check.
     *
     * @param param data consistency calculate parameter
     * @return calculated result
     */
    Iterable<DataConsistencyCalculatedResult> calculate(DataConsistencyCalculateParameter param);
    
    /**
     * Cancel calculation.
     *
     * @throws SQLException SQL exception if cancel underlying SQL execution failure
     */
    void cancel() throws SQLException;
    
    /**
     * Is calculation canceling.
     *
     * @return canceling or not
     */
    boolean isCanceling();
    
    /**
     * Get supported database types.
     * 
     * @return supported database types
     */
    Collection<DatabaseType> getSupportedDatabaseTypes();
}
