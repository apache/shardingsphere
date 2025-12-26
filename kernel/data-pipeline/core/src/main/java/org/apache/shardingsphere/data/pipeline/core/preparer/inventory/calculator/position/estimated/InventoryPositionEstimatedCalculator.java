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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.estimated;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByUniqueKeyException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.IntervalToRangeIterator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelinePrepareSQLBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Inventory position estimated calculator.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class InventoryPositionEstimatedCalculator {
    
    /**
     * Get integer unique key values range.
     *
     * @param qualifiedTable qualified table
     * @param uniqueKey unique key
     * @param dataSource data source
     * @return unique key values range
     * @throws SplitPipelineJobByUniqueKeyException if an error occurs while getting unique key values range
     */
    public static Range<Long> getIntegerUniqueKeyValuesRange(final QualifiedTable qualifiedTable, final String uniqueKey, final PipelineDataSource dataSource) {
        PipelinePrepareSQLBuilder pipelineSQLBuilder = new PipelinePrepareSQLBuilder(dataSource.getDatabaseType());
        String sql = pipelineSQLBuilder.buildUniqueKeyMinMaxValuesSQL(qualifiedTable.getSchemaName(), qualifiedTable.getTableName(), uniqueKey);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return Range.closed(resultSet.getLong(1), resultSet.getLong(2));
        } catch (final SQLException ex) {
            throw new SplitPipelineJobByUniqueKeyException(qualifiedTable.getTableName(), uniqueKey, ex);
        }
    }
    
    /**
     * Get positions by integer unique key range.
     *
     * @param tableRecordsCount table records count
     * @param uniqueKeyValuesRange unique key values range
     * @param shardingSize sharding size
     * @return positions
     */
    public static List<IngestPosition> getIntegerPositions(final long tableRecordsCount, final Range<Long> uniqueKeyValuesRange, final long shardingSize) {
        Long minimum = uniqueKeyValuesRange.getLowerBound();
        Long maximum = uniqueKeyValuesRange.getUpperBound();
        if (0 == tableRecordsCount || null == minimum || null == maximum) {
            return Collections.singletonList(new IntegerPrimaryKeyIngestPosition(null, null));
        }
        List<IngestPosition> result = new LinkedList<>();
        long splitCount = tableRecordsCount / shardingSize + (tableRecordsCount % shardingSize > 0 ? 1 : 0);
        long interval = BigInteger.valueOf(maximum).subtract(BigInteger.valueOf(minimum)).divide(BigInteger.valueOf(splitCount)).longValue();
        IntervalToRangeIterator rangeIterator = new IntervalToRangeIterator(minimum, maximum, interval);
        while (rangeIterator.hasNext()) {
            org.apache.commons.lang3.Range<Long> range = rangeIterator.next();
            result.add(new IntegerPrimaryKeyIngestPosition(range.getMinimum(), range.getMaximum()));
        }
        return result;
    }
}
