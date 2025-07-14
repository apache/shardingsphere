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

package org.apache.shardingsphere.distsql.handler.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;

import java.util.Optional;

/**
 * Database name utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseNameUtils {
    
    /**
     * Get database name.
     *
     * @param sqlStatement SQL statement
     * @param currentDatabaseName current database name
     * @return database name
     */
    public static String getDatabaseName(final SQLStatement sqlStatement, final String currentDatabaseName) {
        Optional<FromDatabaseSQLStatementAttribute> fromDatabaseAttribute = sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class);
        return fromDatabaseAttribute.isPresent()
                ? fromDatabaseAttribute.get().getFromDatabase().map(FromDatabaseSegment::getDatabase).map(optional -> optional.getIdentifier().getValue()).orElse(currentDatabaseName)
                : currentDatabaseName;
    }
}
