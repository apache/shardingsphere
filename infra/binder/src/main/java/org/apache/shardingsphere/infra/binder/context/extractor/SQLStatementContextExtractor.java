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

package org.apache.shardingsphere.infra.binder.context.extractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementContextExtractor {
    
    /**
     * Get table names.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @return table names
     */
    public static Collection<String> getTableNames(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames() : Collections.emptyList();
        if (!tableNames.isEmpty()) {
            return tableNames;
        }
        return sqlStatementContext instanceof IndexAvailable
                ? getTableNames(database, sqlStatementContext.getDatabaseType(), ((IndexAvailable) sqlStatementContext).getIndexes())
                : Collections.emptyList();
    }
    
    private static Collection<String> getTableNames(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<IndexSegment> indexes) {
        Collection<String> result = new LinkedList<>();
        for (QualifiedTable each : IndexMetaDataUtils.getTableNames(database, databaseType, indexes)) {
            result.add(each.getTableName());
        }
        return result;
    }
}
