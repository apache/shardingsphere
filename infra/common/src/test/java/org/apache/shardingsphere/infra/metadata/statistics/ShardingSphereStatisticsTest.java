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

package org.apache.shardingsphere.infra.metadata.statistics;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereStatisticsTest {
    
    public static final String TEST_DATABASE_NAME = "TEST_DATABASE_NAME";
    
    public static final String TEST_DATABASE_NAME_2 = "TEST_DATABASE_NAME_2";
    
    public static final String NON_EXISTENT_DATABASE_NAME = "NON_EXISTENT_DATABASE_NAME";
    
    @Test
    void assertGetDatabase() {
        ShardingSphereStatistics shardingSphereStatistics = new ShardingSphereStatistics();
        ShardingSphereDatabaseData shardingSphereDatabaseData = new ShardingSphereDatabaseData();
        shardingSphereStatistics.putDatabase(TEST_DATABASE_NAME, shardingSphereDatabaseData);
        ShardingSphereDatabaseData databaseData = shardingSphereStatistics.getDatabase(TEST_DATABASE_NAME);
        assertEquals(databaseData, shardingSphereDatabaseData);
        assertNull(shardingSphereStatistics.getDatabase(NON_EXISTENT_DATABASE_NAME));
    }
    
    @Test
    void assertPutDatabase() {
        ShardingSphereStatistics shardingSphereStatistics = new ShardingSphereStatistics();
        ShardingSphereDatabaseData shardingSphereDatabaseData = new ShardingSphereDatabaseData();
        shardingSphereStatistics.putDatabase(TEST_DATABASE_NAME, shardingSphereDatabaseData);
        assertThat(shardingSphereStatistics.getDatabaseData().size(), is(1));
        assertFalse(shardingSphereStatistics.containsDatabase(TEST_DATABASE_NAME_2));
        ShardingSphereDatabaseData newShardingSphereDatabaseData = new ShardingSphereDatabaseData();
        shardingSphereStatistics.putDatabase(TEST_DATABASE_NAME_2, newShardingSphereDatabaseData);
        assertThat(shardingSphereStatistics.getDatabaseData().size(), is(2));
        assertTrue(shardingSphereStatistics.containsDatabase(TEST_DATABASE_NAME_2));
    }
    
    @Test
    void assertDropDatabase() {
        ShardingSphereStatistics shardingSphereStatistics = new ShardingSphereStatistics();
        ShardingSphereDatabaseData shardingSphereDatabaseData = new ShardingSphereDatabaseData();
        shardingSphereStatistics.putDatabase(TEST_DATABASE_NAME, shardingSphereDatabaseData);
        assertTrue(shardingSphereStatistics.containsDatabase(TEST_DATABASE_NAME));
        shardingSphereStatistics.dropDatabase(TEST_DATABASE_NAME);
        assertThat(shardingSphereStatistics.getDatabaseData().size(), is(0));
        assertFalse(shardingSphereStatistics.containsDatabase(TEST_DATABASE_NAME));
    }
    
    @Test
    void assertContainsTable() {
        ShardingSphereStatistics shardingSphereStatistics = new ShardingSphereStatistics();
        ShardingSphereDatabaseData shardingSphereDatabaseData = new ShardingSphereDatabaseData();
        shardingSphereStatistics.putDatabase(TEST_DATABASE_NAME, shardingSphereDatabaseData);
        assertTrue(shardingSphereStatistics.containsDatabase(TEST_DATABASE_NAME));
        assertFalse(shardingSphereStatistics.containsDatabase(NON_EXISTENT_DATABASE_NAME));
    }
}
