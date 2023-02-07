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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByRangeException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Pipeline table meta data util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineTableMetaDataUtil {
    
    /**
     * Get unique key column.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param metaDataLoader meta data loader
     * @return pipeline column meta data
     */
    public static Optional<PipelineColumnMetaData> getUniqueKeyColumn(final String schemaName, final String tableName, final PipelineTableMetaDataLoader metaDataLoader) {
        PipelineTableMetaData pipelineTableMetaData = metaDataLoader.getTableMetaData(schemaName, tableName);
        return Optional.ofNullable(getAnAppropriateUniqueKeyColumn(pipelineTableMetaData, tableName));
    }
    
    private static PipelineColumnMetaData getAnAppropriateUniqueKeyColumn(final PipelineTableMetaData tableMetaData, final String tableName) {
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new SplitPipelineJobByRangeException(tableName, "Can not get table meta data"));
        List<String> primaryKeys = tableMetaData.getPrimaryKeyColumns();
        if (1 == primaryKeys.size()) {
            return tableMetaData.getColumnMetaData(tableMetaData.getPrimaryKeyColumns().get(0));
        }
        Collection<PipelineIndexMetaData> uniqueIndexes = tableMetaData.getUniqueIndexes();
        if (uniqueIndexes.isEmpty() && primaryKeys.isEmpty()) {
            return null;
        }
        if (1 == uniqueIndexes.size() && 1 == uniqueIndexes.iterator().next().getColumns().size()) {
            PipelineColumnMetaData column = uniqueIndexes.iterator().next().getColumns().get(0);
            if (!column.isNullable()) {
                return column;
            }
        }
        throw new SplitPipelineJobByRangeException(tableName, "table contains multiple unique index or unique index contains nullable/multiple column(s)");
    }
}
