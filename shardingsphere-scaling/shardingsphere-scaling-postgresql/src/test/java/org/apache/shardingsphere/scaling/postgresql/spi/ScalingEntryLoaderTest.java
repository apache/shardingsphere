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

package org.apache.shardingsphere.scaling.postgresql.spi;

import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.postgresql.PostgreSQLDataSourceChecker;
import org.apache.shardingsphere.scaling.postgresql.PostgreSQLImporter;
import org.apache.shardingsphere.scaling.postgresql.PostgreSQLJdbcDumper;
import org.apache.shardingsphere.scaling.postgresql.PostgreSQLPositionManager;
import org.apache.shardingsphere.scaling.postgresql.PostgreSQLScalingEntry;
import org.apache.shardingsphere.scaling.postgresql.PostgreSQLWalDumper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ScalingEntryLoaderTest {
    
    @Test
    public void assertGetScalingEntryByDatabaseType() {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType("PostgreSQL");
        assertTrue(scalingEntry instanceof PostgreSQLScalingEntry);
        assertThat(scalingEntry.getPositionManager(), equalTo(PostgreSQLPositionManager.class));
        assertThat(scalingEntry.getCheckerClass(), equalTo(PostgreSQLDataSourceChecker.class));
        assertThat(scalingEntry.getImporterClass(), equalTo(PostgreSQLImporter.class));
        assertThat(scalingEntry.getJdbcDumperClass(), equalTo(PostgreSQLJdbcDumper.class));
        assertThat(scalingEntry.getLogDumperClass(), equalTo(PostgreSQLWalDumper.class));
    }
}
