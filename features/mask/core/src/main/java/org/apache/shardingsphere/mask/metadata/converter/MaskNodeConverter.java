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

package org.apache.shardingsphere.mask.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mask node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaskNodeConverter {
    
    private static final String TABLES = "tables";
    
    private static final String MASK_ALGORITHMS = "mask_algorithms";
    
    /**
     * Get table name path.
     * 
     * @param tableName table name
     * @return table name path
     */
    public static String getTableNamePath(final String tableName) {
        return String.join("/", TABLES, tableName);
    }
    
    /**
     * Get mask algorithm path.
     * 
     * @param maskAlgorithmName mask algorithm name
     * @return mask algorithm path
     */
    public static String getMaskAlgorithmNamePath(final String maskAlgorithmName) {
        return String.join("/", MASK_ALGORITHMS, maskAlgorithmName);
    }
}
