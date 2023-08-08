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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.RecordSingleTableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.SingleTableInventoryCalculator;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.annotation.SPIDescription;

import java.util.Collection;
import java.util.Properties;

/**
 * Data match table data consistency checker.
 */
@SPIDescription("Match raw data of records.")
@Slf4j
public final class DataMatchTableDataConsistencyChecker extends MatchingTableDataConsistencyChecker {
    
    private static final String CHUNK_SIZE_KEY = "chunk-size";
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    private SingleTableInventoryCalculator calculator;
    
    @Override
    public void init(final Properties props) {
        calculator = new RecordSingleTableInventoryCalculator(getChunkSize(props));
    }
    
    private int getChunkSize(final Properties props) {
        int result;
        try {
            result = Integer.parseInt(props.getProperty(CHUNK_SIZE_KEY, Integer.toString(DEFAULT_CHUNK_SIZE)));
        } catch (final NumberFormatException ignore) {
            log.warn("'chunk-size' is not a valid number, use default value {}", DEFAULT_CHUNK_SIZE);
            return DEFAULT_CHUNK_SIZE;
        }
        if (result <= 0) {
            log.warn("Invalid 'chunk-size': {}, use default value {}", result, DEFAULT_CHUNK_SIZE);
            return DEFAULT_CHUNK_SIZE;
        }
        return result;
    }
    
    @Override
    protected SingleTableInventoryCalculator getSingleTableInventoryCalculator() {
        return calculator;
    }
    
    @Override
    public Collection<DatabaseType> getSupportedDatabaseTypes() {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
    }
    
    @Override
    public String getType() {
        return "DATA_MATCH";
    }
}
