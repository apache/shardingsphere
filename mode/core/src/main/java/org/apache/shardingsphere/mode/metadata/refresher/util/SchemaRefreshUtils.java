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

package org.apache.shardingsphere.mode.metadata.refresher.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Schema refresh utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaRefreshUtils {
    
    /**
     * Get schema name.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @return schema name
     */
    public static String getSchemaName(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        return getRawSchemaName(database, sqlStatementContext).getValue().toLowerCase();
    }
    
    /**
     * Get actual schema name.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @return actual schema name
     */
    public static String getActualSchemaName(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        return getActualSchemaName(database, getRawSchemaName(database, sqlStatementContext));
    }
    
    /**
     * Get actual schema name.
     *
     * @param database database
     * @param schemaIdentifier schema identifier
     * @return actual schema name
     */
    public static String getActualSchemaName(final ShardingSphereDatabase database, final IdentifierValue schemaIdentifier) {
        IdentifierCasePolicy policy = database.getIdentifierContext().getPolicy(IdentifierScope.SCHEMA);
        Optional<String> matchedSchemaName = database.getAllSchemas().stream().map(ShardingSphereSchema::getName)
                .filter(each -> policy.matches(each, schemaIdentifier.getValue(), schemaIdentifier.getQuoteCharacter())).findFirst();
        return matchedSchemaName.orElseGet(() -> QuoteCharacter.NONE == schemaIdentifier.getQuoteCharacter() && LookupMode.NORMALIZED == policy.getLookupMode(schemaIdentifier.getQuoteCharacter())
                ? policy.normalize(schemaIdentifier.getValue())
                : schemaIdentifier.getValue());
    }
    
    /**
     * Get actual schema names.
     *
     * @param database database
     * @param schemaIdentifiers schema identifiers
     * @return actual schema names
     */
    public static Collection<String> getActualSchemaNames(final ShardingSphereDatabase database, final Collection<IdentifierValue> schemaIdentifiers) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : schemaIdentifiers) {
            String actualSchemaName = getActualSchemaName(database, each);
            if (null != actualSchemaName) {
                result.add(actualSchemaName);
            }
        }
        return result;
    }
    
    private static IdentifierValue getRawSchemaName(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getTablesContext().getIdentifierSchemaName()
                .orElseGet(() -> new IdentifierValue(new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName())));
    }
}
