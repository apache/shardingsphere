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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

/**
 * Data consistency calculate algorithm chooser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataConsistencyCalculateAlgorithmChooser {
    
    /**
     * Choose data consistency calculate algorithm when it's not defined.
     *
     * @param databaseType database type
     * @param peerDatabaseType peer database type
     * @return algorithm
     */
    public static DataConsistencyCalculateAlgorithm choose(final DatabaseType databaseType, final DatabaseType peerDatabaseType) {
        String algorithmType;
        if (!databaseType.getType().equalsIgnoreCase(peerDatabaseType.getType())) {
            algorithmType = "DATA_MATCH";
        } else if (databaseType instanceof MySQLDatabaseType) {
            algorithmType = "CRC32_MATCH";
        } else {
            algorithmType = "DATA_MATCH";
        }
        return DataConsistencyCalculateAlgorithmFactory.newInstance(algorithmType, null);
    }
}
