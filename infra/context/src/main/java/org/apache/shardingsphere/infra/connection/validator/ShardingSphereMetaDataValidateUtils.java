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

package org.apache.shardingsphere.infra.connection.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.Collection;
import java.util.HashSet;

/**
 * ShardingSphere meta data validate utility class.
 */
// TODO consider add common ShardingSphereMetaDataValidateEngine for all features
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereMetaDataValidateUtils {
    
    private static final Collection<String> EXCLUDE_VALIDATE_TABLES = new HashSet<>(1, 1F);
    
    static {
        EXCLUDE_VALIDATE_TABLES.add("DUAL");
    }
    
    /**
     * Validate table exist.
     *
     * @param sqlStatementContext sql statement context
     * @param database database
     * @throws NoSuchTableException no such table exception
     */
    public static void validateTableExist(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName());
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            if (!EXCLUDE_VALIDATE_TABLES.contains(each.toUpperCase()) && !schema.containsTable(each)) {
                throw new NoSuchTableException(each);
            }
        }
    }
}
