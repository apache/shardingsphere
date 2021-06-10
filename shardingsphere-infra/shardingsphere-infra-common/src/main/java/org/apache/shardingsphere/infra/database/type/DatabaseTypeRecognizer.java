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

package org.apache.shardingsphere.infra.database.type;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Database type recognizer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeRecognizer {
    
    /**
     * Get database type.
     * 
     * @param dataSources data sources
     * @return database type
     */
    public static DatabaseType getDatabaseType(final Collection<DataSource> dataSources) {
        DatabaseType result = null;
        for (DataSource each : dataSources) {
            DatabaseType databaseType = getDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, String.format("Database type inconsistent with '%s' and '%s'", result, databaseType));
            result = databaseType;
        }
        return null == result ? DatabaseTypeRegistry.getDefaultDatabaseType() : result;
    }
    
    @SuppressWarnings("ReturnOfNull")
    private static DatabaseType getDatabaseType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            return null;
        }
    }
}
