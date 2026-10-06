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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Pipeline SQL segment builder.
 */
public final class PipelineSQLSegmentBuilder {
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    @Nullable
    private final DatabaseIdentifierContext identifierContext;
    
    /**
     * Create a builder for resolved actual identifiers without resolving a storage policy.
     *
     * @param databaseType database type
     */
    public PipelineSQLSegmentBuilder(final DatabaseType databaseType) {
        this(databaseType, null);
    }
    
    /**
     * Create a builder using the SQL endpoint's identifier context.
     *
     * @param databaseType database type
     * @param identifierContext endpoint context, or null for actual identifiers only
     */
    public PipelineSQLSegmentBuilder(final DatabaseType databaseType, @Nullable final DatabaseIdentifierContext identifierContext) {
        dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        this.identifierContext = identifierContext;
    }
    
    /**
     * Get escaped identifier.
     *
     * @param identifierScope identifier scope
     * @param identifier reference identifier, retaining its quote characters
     * @return escaped identifier
     */
    @HighFrequencyInvocation
    public String getEscapedIdentifier(final IdentifierScope identifierScope, final String identifier) {
        return "*".equals(identifier) ? identifier : getEscapedActualIdentifier(normalizeStorageIdentifier(identifierScope, identifier));
    }
    
    /**
     * Normalize a reference for storage, including names persisted for later cleanup.
     *
     * @param identifierScope identifier scope
     * @param identifier reference identifier
     * @return storage identifier
     */
    @HighFrequencyInvocation
    public String normalizeStorageIdentifier(final IdentifierScope identifierScope, final String identifier) {
        return identifierContext.normalizeStorage(identifierScope, new IdentifierValue(identifier));
    }
    
    /**
     * Get escaped actual identifier.
     *
     * @param identifier actual identifier to be processed
     * @return escaped actual identifier
     */
    @HighFrequencyInvocation
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
    @HighFrequencyInvocation
    public String getQualifiedTableName(final String schemaName, final String tableName) {
        String escapedTableName = getEscapedIdentifier(IdentifierScope.TABLE, tableName);
        return dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable() && !Strings.isNullOrEmpty(schemaName)
                ? String.join(".", getEscapedIdentifier(IdentifierScope.SCHEMA, schemaName), escapedTableName)
                : escapedTableName;
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
