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

package org.apache.shardingsphere.sql.parser.binder.metadata.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JdbcUtil {
    
    /**
     * Get schema.
     *
     * @param connection connection
     * @param databaseType database type
     * @return schema
     */
    public static String getSchema(final Connection connection, final String databaseType) {
        String result = null;
        try {
            if ("Oracle".equals(databaseType)) {
                DatabaseMetaData metaData = connection.getMetaData();
                if (null != metaData) {
                    return Optional.ofNullable(metaData.getUserName()).map(String::toUpperCase).orElse(null);
                } else {
                    return null;
                }
            }
            result = connection.getSchema();
        } catch (final SQLException ignore) {
        }
        return result;
    }
}
