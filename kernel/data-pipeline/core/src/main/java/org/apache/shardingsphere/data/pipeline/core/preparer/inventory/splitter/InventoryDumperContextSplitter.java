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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.splitter;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Range;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.StringPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.estimated.InventoryPositionEstimatedCalculator;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.InventoryRecordsCountCalculator;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory dumper context splitter.
 */
@RequiredArgsConstructor
public final class InventoryDumperContextSplitter {
    
    private final PipelineDataSource sourceDataSource;
    
    private final InventoryDumperContext dumperContext;
    
    /**
     * Split inventory dumper context.
     *
     * @param jobItemContext job item context
     * @return inventory dumper contexts
     */
    public Collection<InventoryDumperContext> split(final TransmissionJobItemContext jobItemContext) {
        return splitByTable().stream().flatMap(each -> splitByPrimaryKey(each, jobItemContext).stream()).collect(Collectors.toList());
    }
    
    private Collection<InventoryDumperContext> splitByTable() {
        return dumperContext.getCommonContext().getTableNameMapper().getTableNameMap().entrySet()
                .stream().map(entry -> createTableSplitDumperContext(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
    
    private InventoryDumperContext createTableSplitDumperContext(final ShardingSphereIdentifier actualTableName, final ShardingSphereIdentifier logicTableName) {
        InventoryDumperContext result = new InventoryDumperContext(dumperContext.getCommonContext());
        result.setActualTableName(actualTableName.toString());
        result.setLogicTableName(logicTableName.toString());
        result.getCommonContext().setPosition(new IngestPlaceholderPosition());
        result.setInsertColumnNames(dumperContext.getInsertColumnNames());
        result.setUniqueKeyColumns(dumperContext.getUniqueKeyColumns());
        return result;
    }
    
    private Collection<InventoryDumperContext> splitByPrimaryKey(final InventoryDumperContext dumperContext, final TransmissionJobItemContext jobItemContext) {
        if (null == dumperContext.getUniqueKeyColumns()) {
            dumperContext.setUniqueKeyColumns(getTableUniqueKeys(dumperContext, jobItemContext));
        }
        Collection<InventoryDumperContext> result = new LinkedList<>();
        TransmissionProcessContext jobProcessContext = jobItemContext.getJobProcessContext();
        int batchSize = jobProcessContext.getProcessConfiguration().getRead().getBatchSize();
        JobRateLimitAlgorithm rateLimitAlgorithm = jobProcessContext.getReadRateLimitAlgorithm();
        int i = 0;
        for (IngestPosition each : getInventoryPositions(dumperContext, jobItemContext)) {
            result.add(createPrimaryKeySplitDumperContext(dumperContext, each, i++, batchSize, rateLimitAlgorithm));
        }
        return result;
    }
    
    private List<PipelineColumnMetaData> getTableUniqueKeys(final InventoryDumperContext dumperContext, final TransmissionJobItemContext jobItemContext) {
        String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
        return PipelineTableMetaDataUtils.getUniqueKeyColumns(schemaName, dumperContext.getActualTableName(), jobItemContext.getSourceMetaDataLoader());
    }
    
    private Collection<IngestPosition> getInventoryPositions(final InventoryDumperContext dumperContext, final TransmissionJobItemContext jobItemContext) {
        TransmissionJobItemProgress initProgress = jobItemContext.getInitProgress();
        if (null != initProgress) {
            // Do NOT filter FinishedPosition here, since whole inventory tasks are required in job progress when persisting to register center.
            Collection<IngestPosition> result = initProgress.getInventory().getInventoryPosition(dumperContext.getActualTableName()).values();
            if (!result.isEmpty()) {
                return result;
            }
        }
        long tableRecordsCount = InventoryRecordsCountCalculator.getTableRecordsCount(dumperContext, sourceDataSource);
        jobItemContext.updateInventoryRecordsCount(tableRecordsCount);
        if (!dumperContext.hasUniqueKey()) {
            return Collections.singleton(new UnsupportedKeyIngestPosition());
        }
        DialectDataTypeOption dataTypeOption = new DatabaseTypeRegistry(sourceDataSource.getDatabaseType()).getDialectDatabaseMetaData().getDataTypeOption();
        List<PipelineColumnMetaData> uniqueKeyColumns = dumperContext.getUniqueKeyColumns();
        int firstColumnDataType = uniqueKeyColumns.get(0).getDataType();
        if (dataTypeOption.isIntegerDataType(firstColumnDataType)) {
            String schemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(dumperContext.getLogicTableName());
            String uniqueKey = dumperContext.getUniqueKeyColumns().get(0).getName();
            Range<Long> uniqueKeyValuesRange = InventoryPositionEstimatedCalculator.getIntegerUniqueKeyValuesRange(sourceDataSource, schemaName, dumperContext.getActualTableName(), uniqueKey);
            int shardingSize = jobItemContext.getJobProcessContext().getProcessConfiguration().getRead().getShardingSize();
            return InventoryPositionEstimatedCalculator.getIntegerPositions(tableRecordsCount, uniqueKeyValuesRange, shardingSize);
        }
        if (1 == uniqueKeyColumns.size() && dataTypeOption.isStringDataType(firstColumnDataType)) {
            return Collections.singleton(new StringPrimaryKeyIngestPosition(null, null));
        }
        return Collections.singleton(new UnsupportedKeyIngestPosition());
    }
    
    private InventoryDumperContext createPrimaryKeySplitDumperContext(final InventoryDumperContext dumperContext, final IngestPosition position,
                                                                      final int shardingItem, final int batchSize, final JobRateLimitAlgorithm rateLimitAlgorithm) {
        InventoryDumperContext result = new InventoryDumperContext(dumperContext.getCommonContext());
        result.getCommonContext().setPosition(position);
        result.setShardingItem(shardingItem);
        result.setActualTableName(dumperContext.getActualTableName());
        result.setLogicTableName(dumperContext.getLogicTableName());
        result.setUniqueKeyColumns(dumperContext.getUniqueKeyColumns());
        result.setInsertColumnNames(dumperContext.getInsertColumnNames());
        result.setBatchSize(batchSize);
        result.setRateLimitAlgorithm(rateLimitAlgorithm);
        return result;
    }
}
