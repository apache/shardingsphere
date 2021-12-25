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

package org.apache.shardingsphere.data.pipeline.postgresql;

import org.apache.shardingsphere.data.pipeline.postgresql.check.datasource.PostgreSQLDataSourceChecker;
import org.apache.shardingsphere.data.pipeline.postgresql.importer.PostgreSQLImporter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLInventoryDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLPositionInitializer;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLWalDumper;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PostgreSQLScalingEntryTest {
    
    @Test
    public void assertGetScalingEntryByDatabaseType() throws IllegalAccessException, InstantiationException {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance("PostgreSQL");
        assertTrue(scalingEntry instanceof PostgreSQLScalingEntry);
        assertThat(scalingEntry.getPositionInitializerClass(), equalTo(PostgreSQLPositionInitializer.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass(), equalTo(PostgreSQLEnvironmentChecker.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass().newInstance().getDataSourceCheckerClass(), equalTo(PostgreSQLDataSourceChecker.class));
        assertThat(scalingEntry.getImporterClass(), equalTo(PostgreSQLImporter.class));
        assertThat(scalingEntry.getInventoryDumperClass(), equalTo(PostgreSQLInventoryDumper.class));
        assertThat(scalingEntry.getIncrementalDumperClass(), equalTo(PostgreSQLWalDumper.class));
    }
}
