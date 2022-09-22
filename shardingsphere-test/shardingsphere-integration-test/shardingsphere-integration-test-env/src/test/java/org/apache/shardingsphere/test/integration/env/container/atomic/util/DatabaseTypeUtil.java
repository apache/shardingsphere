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

package org.apache.shardingsphere.test.integration.env.container.atomic.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeUtil {
    
    /**
     * Check MySQL database type.
     *
     * @param databaseType database type
     * @return true if database type is MySQL, false otherwise
     */
    public static boolean isMySQL(final DatabaseType databaseType) {
        return databaseType instanceof MySQLDatabaseType;
    }
    
    /**
     * Check PostgreSQL database type.
     *
     * @param databaseType database type
     * @return true if database type is PostgreSQL, false otherwise
     */
    public static boolean isPostgreSQL(final DatabaseType databaseType) {
        return databaseType instanceof PostgreSQLDatabaseType;
    }
    
    /**
     * Check openGauss database type.
     *
     * @param databaseType database type
     * @return true if database type is openGauss, false otherwise
     */
    public static boolean isOpenGauss(final DatabaseType databaseType) {
        return databaseType instanceof OpenGaussDatabaseType;
    }
    
    /**
     * Get storage container image major version.
     *
     * @param storageContainerImage storage container image
     * @return major version
     */
    public static String parseMajorVersion(final String storageContainerImage) {
        if (StringUtils.isBlank(storageContainerImage)) {
            return "";
        }
        String version = storageContainerImage.split(":")[1];
        return version.split("\\.")[0];
    }
}
