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

package org.apache.shardingsphere.scaling.mysql.spi;

import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.mysql.MySQLBinlogDumper;
import org.apache.shardingsphere.scaling.mysql.MySQLDataConsistencyChecker;
import org.apache.shardingsphere.scaling.mysql.MySQLDataSourceChecker;
import org.apache.shardingsphere.scaling.mysql.MySQLImporter;
import org.apache.shardingsphere.scaling.mysql.MySQLJdbcDumper;
import org.apache.shardingsphere.scaling.mysql.MySQLPositionManager;
import org.apache.shardingsphere.scaling.mysql.MySQLScalingEntry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ScalingEntryLoaderTest {
    
    @Test
    public void assertGetScalingEntryByDatabaseType() {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType("MySQL");
        assertTrue(scalingEntry instanceof MySQLScalingEntry);
        assertThat(scalingEntry.getPositionManager(), equalTo(MySQLPositionManager.class));
        assertThat(scalingEntry.getDataSourceCheckerClass(), equalTo(MySQLDataSourceChecker.class));
        assertThat(scalingEntry.getImporterClass(), equalTo(MySQLImporter.class));
        assertThat(scalingEntry.getJdbcDumperClass(), equalTo(MySQLJdbcDumper.class));
        assertThat(scalingEntry.getLogDumperClass(), equalTo(MySQLBinlogDumper.class));
        assertThat(scalingEntry.getDataConsistencyCheckerClass(), equalTo(MySQLDataConsistencyChecker.class));
    }
}
