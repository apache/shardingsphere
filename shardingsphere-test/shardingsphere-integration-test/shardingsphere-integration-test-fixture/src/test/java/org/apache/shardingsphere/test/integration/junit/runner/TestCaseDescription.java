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

package org.apache.shardingsphere.test.integration.junit.runner;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.common.ExecutionMode;
import org.apache.shardingsphere.test.integration.junit.annotation.ShardingSphereITStorageType;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;

/**
 * Test case description.
 */
@RequiredArgsConstructor
@Builder
@Getter
public final class TestCaseDescription {
    
    @NonNull
    private final String database;
    
    @NonNull
    private final String scenario;
    
    @NonNull
    private final String adapter;
    
    @NonNull
    private final SQLCommandType sqlCommandType;
    
    @NonNull
    private final ExecutionMode executionMode;
    
    /**
     * Get storage type.
     *
     * @return storage type
     */
    public ShardingSphereITStorageType getStorageType() {
        return ShardingSphereITStorageType.valueOf(database);
    }
    
    /**
     * Get database type.
     *
     * @return database type
     */
    public DatabaseType getDatabaseType() {
        return DatabaseTypeRegistry.getActualDatabaseType(database);
    }
    
    /**
     * Create a instance builder from System properties.
     *
     * @param testCaseSpec test csae specification
     * @return test instance
     */
    public static TestCaseDescriptionBuilder fromSystemProps(final TestCaseSpec testCaseSpec) {
        return builder()
                .adapter(System.getProperty("it.adapter"))
                .database(System.getProperty("it.database"))
                .scenario(System.getProperty("it.scenario"))
                .executionMode(testCaseSpec.executionMode())
                .sqlCommandType(testCaseSpec.sqlCommandType());
    }
}
