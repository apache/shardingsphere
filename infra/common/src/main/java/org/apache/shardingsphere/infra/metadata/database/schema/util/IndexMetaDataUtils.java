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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Index meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexMetaDataUtils {
    
    private static final String UNDERLINE = "_";
    
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
     * Get table names.
     *
     * @param database database
     * @param indexes indexes
     * @param protocolType protocol type
     * @return table names
     */
    public static Collection<QualifiedTable> getTableNames(final ShardingSphereDatabase database, final DatabaseType protocolType, final Collection<IndexSegment> indexes) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(database.getName());
        for (IndexSegment each : indexes) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            findLogicTableNameFromMetaData(database.getSchema(actualSchemaName), each.getIndexName().getIdentifier().getValue())
                    .ifPresent(optional -> result.add(new QualifiedTable(actualSchemaName, optional)));
        }
        return result;
    }
    
    private static Optional<String> findLogicTableNameFromMetaData(final ShardingSphereSchema schema, final String logicIndexName) {
        return schema.getAllTables().stream().filter(table -> table.containsIndex(logicIndexName)).findFirst().map(ShardingSphereTable::getName);
    }
}
