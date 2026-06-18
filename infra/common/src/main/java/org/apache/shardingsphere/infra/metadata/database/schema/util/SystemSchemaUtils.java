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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;

import java.util.Collection;
import java.util.Optional;

/**
 * System schema utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemSchemaUtils {
    
    /**
     * Judge whether SQL statement contains system schema.
     *
     * @param databaseType database type
     * @param schemaNames schema names
     * @param database database
     * @return contains system schema or not
     */
    public static boolean containsSystemSchema(final DatabaseType databaseType, final Collection<String> schemaNames, final ShardingSphereDatabase database) {
        DialectSchemaOption schemaOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption();
        if (database.isComplete() && !schemaOption.getDefaultSchema().isPresent()) {
            return false;
        }
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        for (String each : schemaNames) {
            if (systemDatabase.getSystemSchemas().contains(each)) {
                return true;
            }
        }
        return !schemaOption.getDefaultSchema().isPresent() && systemDatabase.getSystemSchemas().contains(database.getName());
    }
    
    /**
     * Judge whether schema is system schema.
     *
     * @param database database
     * @return is system schema or not
     */
    public static boolean isSystemSchema(final ShardingSphereDatabase database) {
        DialectSchemaOption schemaOption = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData().getSchemaOption();
        return (!database.isComplete() || schemaOption.getDefaultSchema().isPresent()) && new SystemDatabase(database.getProtocolType()).getSystemSchemas().contains(database.getName());
    }
    
    /**
     * Judge whether query system catalog from driver or not.
     *
     * @param databaseType database type
     * @param projections projections
     * @return whether query or not
     */
    public static boolean isDriverQuerySystemCatalog(final DatabaseType databaseType, final Collection<ProjectionSegment> projections) {
        if (1 != projections.size()) {
            return false;
        }
        ProjectionSegment firstProjection = projections.iterator().next();
        if (!(firstProjection instanceof ExpressionProjectionSegment)) {
            return false;
        }
        Optional<DialectDriverQuerySystemCatalogOption> driverQuerySystemCatalogOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getDriverQuerySystemCatalogOption();
        return driverQuerySystemCatalogOption.map(optional -> optional.isSystemCatalogQueryExpressions(((ExpressionProjectionSegment) firstProjection).getText())).orElse(false);
    }
}
