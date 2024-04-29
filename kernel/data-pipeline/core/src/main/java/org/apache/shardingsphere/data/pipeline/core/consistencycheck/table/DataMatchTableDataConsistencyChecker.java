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

import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.RecordSingleTableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.SingleTableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.annotation.SPIDescription;

import java.util.Collection;
import java.util.Properties;

/**
 * Data match table data consistency checker.
 */
@SPIDescription("Match raw data of records.")
public final class DataMatchTableDataConsistencyChecker implements TableDataConsistencyChecker {
    
    private static final String CHUNK_SIZE_KEY = "chunk-size";
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    private int chunkSize;
    
    @Override
    public void init(final Properties props) {
        chunkSize = getChunkSize(props);
    }
    
    private int getChunkSize(final Properties props) {
        String chunkSizeText = props.getProperty(CHUNK_SIZE_KEY);
        if (Strings.isNullOrEmpty(chunkSizeText)) {
            return DEFAULT_CHUNK_SIZE;
        }
        int result;
        try {
            result = Integer.parseInt(chunkSizeText);
        } catch (final NumberFormatException ignore) {
            throw new PipelineInvalidParameterException("'chunk-size' is not a valid number: `" + chunkSizeText + "`");
        }
        if (result <= 0) {
            throw new PipelineInvalidParameterException("Invalid 'chunk-size': " + result);
        }
        return result;
    }
    
    @Override
    public TableInventoryChecker buildTableInventoryChecker(final TableInventoryCheckParameter param) {
        return new DataMatchTableInventoryChecker(param, chunkSize);
    }
    
    @Override
    public Collection<DatabaseType> getSupportedDatabaseTypes() {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "DATA_MATCH";
    }
    
    private static final class DataMatchTableInventoryChecker extends MatchingTableInventoryChecker {
        
        private final int chunkSize;
        
        DataMatchTableInventoryChecker(final TableInventoryCheckParameter param, final int chunkSize) {
            super(param);
            this.chunkSize = chunkSize;
        }
        
        @Override
        protected SingleTableInventoryCalculator buildSingleTableInventoryCalculator() {
            return new RecordSingleTableInventoryCalculator(chunkSize);
        }
    }
}
