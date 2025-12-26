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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.StringPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.estimated.InventoryPositionEstimatedCalculator;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import java.util.Collections;
import java.util.List;

/**
 * Inventory position calculator.
 */
@RequiredArgsConstructor
public final class InventoryPositionCalculator {
    
    private final PipelineDataSource dataSource;
    
    private final String schemaName;
    
    private final String tableName;
    
    private final List<PipelineColumnMetaData> uniqueKeyColumns;
    
    private final long tableRecordsCount;
    
    private final int shardingSize;
    
    /**
     * Get positions.
     *
     * @return positions
     */
    public List<IngestPosition> getPositions() {
        DialectDataTypeOption dataTypeOption = new DatabaseTypeRegistry(dataSource.getDatabaseType()).getDialectDatabaseMetaData().getDataTypeOption();
        int firstColumnDataType = uniqueKeyColumns.get(0).getDataType();
        if (dataTypeOption.isIntegerDataType(firstColumnDataType)) {
            String uniqueKey = uniqueKeyColumns.get(0).getName();
            QueryRange uniqueKeyValuesRange = InventoryPositionEstimatedCalculator.getIntegerUniqueKeyValuesRange(schemaName, tableName, uniqueKey, dataSource);
            return InventoryPositionEstimatedCalculator.getIntegerPositions(tableRecordsCount, uniqueKeyValuesRange, shardingSize);
        }
        if (1 == uniqueKeyColumns.size() && dataTypeOption.isStringDataType(firstColumnDataType)) {
            return Collections.singletonList(new StringPrimaryKeyIngestPosition(null, null));
        }
        return Collections.singletonList(new UnsupportedKeyIngestPosition());
    }
}
