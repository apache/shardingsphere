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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Index meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexMetaDataUtils {
    
    private static final String UNDERLINE = "_";
    
    private static final String GENERATED_LOGIC_INDEX_NAME_SUFFIX = "idx";
    
    /**
     * Get logic index name.
     * 
     * @param actualIndexName actual index name
     * @param actualTableName actual table name
     * @return logic index name
     */
    public static String getLogicIndexName(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = UNDERLINE + actualTableName;
        return actualIndexName.endsWith(indexNameSuffix) ? actualIndexName.substring(0, actualIndexName.lastIndexOf(indexNameSuffix)) : actualIndexName;
    }
    
    /**
     * Get actual index name.
     *
     * @param logicIndexName logic index name
     * @param actualTableName actual table name
     * @return actual index name
     */
    public static String getActualIndexName(final String logicIndexName, final String actualTableName) {
        return Strings.isNullOrEmpty(actualTableName) ? logicIndexName : logicIndexName + UNDERLINE + actualTableName;
    }
    
    /**
     * Get generated logic index name.
     *
     * @param columns column segments 
     * @return generated logic index name
     */
    public static String getGeneratedLogicIndexName(final Collection<ColumnSegment> columns) {
        StringBuilder builder = new StringBuilder();
        for (ColumnSegment each : columns) {
            builder.append(each.getIdentifier().getValue()).append(UNDERLINE);
        }
        return builder.append(GENERATED_LOGIC_INDEX_NAME_SUFFIX).toString();
    }
    
    /**
     * Get table names.
     *
     * @param database database
     * @param indexes indexes
     * @param protocolType protocol type
     * @return table names
     */
    public static Collection<QualifiedTable> getTableNames(final ShardingSphereDatabase database, final DatabaseType protocolType, final Collection<IndexSegment> indexes) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(protocolType, database.getName());
        for (IndexSegment each : indexes) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            findLogicTableNameFromMetaData(database.getSchema(actualSchemaName),
                    each.getIndexName().getIdentifier().getValue()).ifPresent(optional -> result.add(new QualifiedTable(actualSchemaName, optional)));
        }
        return result;
    }
    
    private static Optional<String> findLogicTableNameFromMetaData(final ShardingSphereSchema schema, final String logicIndexName) {
        for (String each : schema.getAllTableNames()) {
            if (schema.getTable(each).containsIndex(logicIndexName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
