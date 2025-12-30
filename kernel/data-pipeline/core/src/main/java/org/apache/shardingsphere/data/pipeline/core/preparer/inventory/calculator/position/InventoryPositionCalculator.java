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
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.InventoryDataSparsenessCalculator;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.estimated.InventoryPositionEstimatedCalculator;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact.IntegerPositionHandler;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact.InventoryPositionExactCalculator;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact.StringPositionHandler;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * Inventory position calculator.
 */
@RequiredArgsConstructor
public final class InventoryPositionCalculator {
    
    private final PipelineDataSource dataSource;
    
    private final QualifiedTable qualifiedTable;
    
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
            return getIntegerPositions();
        }
        if (dataTypeOption.isStringDataType(firstColumnDataType)) {
            return getStringPositions();
        }
        return Collections.singletonList(new UnsupportedKeyIngestPosition());
    }
    
    private List<IngestPosition> getIntegerPositions() {
        String uniqueKey = uniqueKeyColumns.get(0).getName();
        Range<BigInteger> uniqueKeyValuesRange = InventoryPositionEstimatedCalculator.getIntegerUniqueKeyValuesRange(qualifiedTable, uniqueKey, dataSource);
        if (InventoryDataSparsenessCalculator.isIntegerUniqueKeyDataSparse(tableRecordsCount, uniqueKeyValuesRange)) {
            return InventoryPositionExactCalculator.getPositions(qualifiedTable, uniqueKey, shardingSize, dataSource, new IntegerPositionHandler());
        }
        return InventoryPositionEstimatedCalculator.getIntegerPositions(tableRecordsCount, uniqueKeyValuesRange, shardingSize);
    }
    
    private List<IngestPosition> getStringPositions() {
        String uniqueKey = uniqueKeyColumns.get(0).getName();
        return InventoryPositionExactCalculator.getPositions(qualifiedTable, uniqueKey, shardingSize, dataSource, new StringPositionHandler());
    }
}
