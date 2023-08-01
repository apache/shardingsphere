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

package org.apache.shardingsphere.data.pipeline.common.metadata.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.job.SplitPipelineJobByRangeException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline table meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineTableMetaDataUtils {
    
    /**
     * Get unique key columns.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param metaDataLoader meta data loader
     * @return pipeline columns meta data
     */
    public static List<PipelineColumnMetaData> getUniqueKeyColumns(final String schemaName, final String tableName, final PipelineTableMetaDataLoader metaDataLoader) {
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(schemaName, tableName);
        ShardingSpherePreconditions.checkNotNull(tableMetaData, () -> new SplitPipelineJobByRangeException(tableName, "Can not get table meta data"));
        List<String> primaryKeys = tableMetaData.getPrimaryKeyColumns();
        if (!primaryKeys.isEmpty()) {
            return primaryKeys.stream().map(tableMetaData::getColumnMetaData).collect(Collectors.toList());
        }
        Collection<PipelineIndexMetaData> uniqueIndexes = tableMetaData.getUniqueIndexes();
        if (uniqueIndexes.isEmpty()) {
            return new LinkedList<>();
        }
        for (PipelineIndexMetaData each : uniqueIndexes) {
            if (each.getColumns().stream().anyMatch(PipelineColumnMetaData::isNullable)) {
                continue;
            }
            return each.getColumns();
        }
        return new LinkedList<>();
    }
}
