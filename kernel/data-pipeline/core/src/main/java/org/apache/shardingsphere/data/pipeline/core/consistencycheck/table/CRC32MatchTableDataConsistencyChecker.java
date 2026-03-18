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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.CRC32TableInventoryCheckCalculator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.TableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.util.DatabaseTypeUtils;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.annotation.SPIDescription;

import java.util.Collection;

/**
 * CRC32 match table data consistency checker.
 */
@SPIDescription("Match CRC32 of records.")
public final class CRC32MatchTableDataConsistencyChecker implements TableDataConsistencyChecker {
    
    @Override
    public TableInventoryChecker buildTableInventoryChecker(final TableInventoryCheckParameter param) {
        return new CRC32MatchTableInventoryChecker(param);
    }
    
    @Override
    public Collection<DatabaseType> getSupportedDatabaseTypes() {
        return DatabaseTypeUtils.getTypeAndBranchTypes("MySQL");
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "CRC32_MATCH";
    }
    
    private static final class CRC32MatchTableInventoryChecker extends MatchingTableInventoryChecker {
        
        CRC32MatchTableInventoryChecker(final TableInventoryCheckParameter param) {
            super(param);
        }
        
        @Override
        protected TableInventoryCalculator<TableInventoryCheckCalculatedResult> buildSingleTableInventoryCalculator() {
            return new CRC32TableInventoryCheckCalculator();
        }
    }
}
