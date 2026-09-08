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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.util.function.Function;

/**
 * Pipeline SQL segment builder.
 */
public final class PipelineSQLSegmentBuilder {
    
    private final DatabaseTypeRegistry databaseTypeRegistry;
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    public PipelineSQLSegmentBuilder(final DatabaseType databaseType) {
        databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        dialectDatabaseMetaData = databaseTypeRegistry.getDialectDatabaseMetaData();
    }
    
    /**
     * Get escaped identifier.
     *
     * @param identifier identifier to be processed
     * @return escaped identifier
     */
    public String getEscapedIdentifier(final String identifier) {
        return "*".equals(identifier) ? identifier : dialectDatabaseMetaData.getQuoteCharacter().wrap(databaseTypeRegistry.formatIdentifierPattern(identifier));
    }
    
    /**
     * Get escaped actual identifier.
     *
     * @param identifier actual identifier to be processed
     * @return escaped actual identifier
     */
    public String getEscapedActualIdentifier(final String identifier) {
        return "*".equals(identifier) ? identifier : dialectDatabaseMetaData.getQuoteCharacter().wrap(identifier);
    }
    
    /**
     * Get qualified table name.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return qualified table name
     */
    public String getQualifiedTableName(final String schemaName, final String tableName) {
        return buildQualifiedTableName(schemaName, tableName, this::getEscapedIdentifier);
    }
    
    /**
     * Get qualified table name.
     *
     * @param qualifiedTable qualified table
     * @return qualified table name
     */
    public String getQualifiedTableName(final QualifiedTable qualifiedTable) {
        return getQualifiedTableName(qualifiedTable.getSchemaName(), qualifiedTable.getTableName());
    }
    
    /**
     * Get qualified actual table name.
     *
     * @param schemaName actual schema name
     * @param tableName actual table name
     * @return qualified actual table name
     */
    public String getQualifiedActualTableName(final String schemaName, final String tableName) {
        return buildQualifiedTableName(schemaName, tableName, this::getEscapedActualIdentifier);
    }
    
    private String buildQualifiedTableName(final String schemaName, final String tableName, final Function<String, String> identifierEscaper) {
        StringBuilder result = new StringBuilder();
        if (dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable() && !Strings.isNullOrEmpty(schemaName)) {
            result.append(identifierEscaper.apply(schemaName)).append('.');
        }
        result.append(identifierEscaper.apply(tableName));
        return result.toString();
    }
}
