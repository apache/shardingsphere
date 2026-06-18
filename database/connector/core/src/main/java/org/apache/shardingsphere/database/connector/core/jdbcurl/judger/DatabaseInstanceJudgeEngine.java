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

package org.apache.shardingsphere.database.connector.core.jdbcurl.judger;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

/**
 * Database instance judger.
 */
@RequiredArgsConstructor
public final class DatabaseInstanceJudgeEngine {
    
    private final DatabaseType databaseType;
    
    /**
     * Judge whether two of JDBC URLs are in the same database instance.
     *
     * @param connectionProps1 connection properties 1
     * @param connectionProps2 connection properties 2
     * @return JDBC URLs are in the same database instance or not
     */
    public boolean isInSameDatabaseInstance(final ConnectionProperties connectionProps1, final ConnectionProperties connectionProps2) {
        return DatabaseTypedSPILoader.findService(DialectDatabaseInstanceJudger.class, databaseType)
                .map(optional -> optional.isInSameDatabaseInstance(connectionProps1, connectionProps2))
                .orElseGet(() -> DatabaseInstanceJudger.isInSameDatabaseInstance(connectionProps1, connectionProps2));
    }
}
