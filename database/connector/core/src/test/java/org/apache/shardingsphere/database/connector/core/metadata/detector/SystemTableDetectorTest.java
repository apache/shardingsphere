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

package org.apache.shardingsphere.database.connector.core.metadata.detector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemTableDetectorTest {
    
    @Test
    void assertIsSystemTable() {
        assertTrue(SystemTableDetector.isSystemTable("PostgreSQL", "pg_catalog", "pg_aggregate"));
        assertTrue(SystemTableDetector.isSystemTable("PostgreSQL", "information_schema", "domain_udt_usage"));
        assertFalse(SystemTableDetector.isSystemTable("PostgreSQL", "public", "t_order"));
        assertTrue(SystemTableDetector.isSystemTable("MySQL", "information_schema", "applicable_roles"));
        assertTrue(SystemTableDetector.isSystemTable("MySQL", "performance_schema", "accounts"));
        assertTrue(SystemTableDetector.isSystemTable("MySQL", "mysql", "columns_priv"));
        assertTrue(SystemTableDetector.isSystemTable("MySQL", "sys", "host_summary_by_stages"));
        assertFalse(SystemTableDetector.isSystemTable("MySQL", "app", "t_order"));
        assertTrue(SystemTableDetector.isSystemTable("openGauss", "pg_catalog", "get_global_prepared_xacts"));
        assertTrue(SystemTableDetector.isSystemTable("openGauss", "information_schema", "_pg_foreign_data_wrappers"));
        assertFalse(SystemTableDetector.isSystemTable("openGauss", "public", "t_order"));
        assertTrue(SystemTableDetector.isSystemTable("PostgreSQL", "shardingsphere", "cluster_information"));
        assertTrue(SystemTableDetector.isSystemTable("Firebird", "system_tables", "mon$attachments"));
        assertFalse(SystemTableDetector.isSystemTable("Firebird", "system_tables", "nonexistent"));
    }
}
