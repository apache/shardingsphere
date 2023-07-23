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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;

import java.util.Collection;
import java.util.HashSet;

/**
 * System schema utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemSchemaUtils {
    
    private static final Collection<String> SYSTEM_CATALOG_QUERY_EXPRESSIONS = new HashSet<>(3, 1F);
    
    static {
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("version()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("intervaltonum(gs_password_deadline())");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("gs_password_notifytime()");
    }
    
    /**
     * Judge whether SQL statement contains system schema or not.
     *
     * @param databaseType database type
     * @param schemaNames schema names
     * @param database database
     * @return whether SQL statement contains system schema or not
     */
    public static boolean containsSystemSchema(final DatabaseType databaseType, final Collection<String> schemaNames, final ShardingSphereDatabase database) {
        if (database.isComplete() && !databaseType.getDefaultSchema().isPresent()) {
            return false;
        }
        for (String each : schemaNames) {
            if (databaseType.getSystemSchemas().contains(each)) {
                return true;
            }
        }
        return !databaseType.getDefaultSchema().isPresent() && databaseType.getSystemSchemas().contains(database.getName());
    }
    
    /**
     * Judge whether query is openGauss system catalog query or not.
     * 
     * @param databaseType database type
     * @param projections projections
     * @return whether query is openGauss system catalog query or not
     */
    public static boolean isOpenGaussSystemCatalogQuery(final DatabaseType databaseType, final Collection<ProjectionSegment> projections) {
        if (!(databaseType instanceof OpenGaussDatabaseType)) {
            return false;
        }
        return 1 == projections.size() && projections.iterator().next() instanceof ExpressionProjectionSegment
                && SYSTEM_CATALOG_QUERY_EXPRESSIONS.contains(((ExpressionProjectionSegment) projections.iterator().next()).getText().toLowerCase());
    }
}
