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

package org.apache.shardingsphere.data.pipeline.core.check.consistency;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;

import java.util.Map;

/**
 * Data consistency checker interface.
 */
public interface DataConsistencyChecker {
    
    /**
     * Check whether each table's records count is valid.
     *
     * @return records count check result. key is logic table name, value is check result.
     */
    Map<String, DataConsistencyCheckResult> checkRecordsCount();
    
    /**
     * Check whether each table's records content is valid.
     *
     * @param checkAlgorithm check algorithm
     * @return records content check result. key is logic table name, value is check result.
     */
    Map<String, Boolean> checkRecordsContent(DataConsistencyCheckAlgorithm checkAlgorithm);
}
