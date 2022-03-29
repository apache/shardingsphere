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

package org.apache.shardingsphere.test.integration.framework.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.framework.database.impl.PostgreSQLDatabaseAssertionMetaData;

import java.util.Optional;

/**
 * Database assertion meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAssertionMetaDataFactory {
    
    /**
     * Create new instance of database assertion meta data.
     *
     * @param databaseType database type
     * @return new instance of database assertion meta data
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static Optional<DatabaseAssertionMetaData> newInstance(final DatabaseType databaseType) {
        switch (databaseType.getName()) {
            case "PostgreSQL":
                return Optional.of(new PostgreSQLDatabaseAssertionMetaData());
            default:
                return Optional.empty();
        }
    }
}
