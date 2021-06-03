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

package org.apache.shardingsphere.scaling.mysql;

import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.mysql.component.MySQLImporter;
import org.apache.shardingsphere.scaling.mysql.component.MySQLIncrementalDumper;
import org.apache.shardingsphere.scaling.mysql.component.MySQLInventoryDumper;
import org.apache.shardingsphere.scaling.mysql.component.MySQLPositionInitializer;
import org.apache.shardingsphere.scaling.mysql.component.checker.MySQLDataConsistencyChecker;
import org.apache.shardingsphere.scaling.mysql.component.checker.MySQLDataSourceChecker;
import org.apache.shardingsphere.scaling.mysql.component.checker.MySQLEnvironmentChecker;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLScalingEntryTest {
    
    @Test
    public void assertGetScalingEntryByDatabaseType() throws IllegalAccessException, InstantiationException {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance("MySQL");
        assertTrue(scalingEntry instanceof MySQLScalingEntry);
        assertThat(scalingEntry.getPositionInitializerClass(), equalTo(MySQLPositionInitializer.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass(), equalTo(MySQLEnvironmentChecker.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass().newInstance().getDataSourceCheckerClass(), equalTo(MySQLDataSourceChecker.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass().newInstance().getDataConsistencyCheckerClass(), equalTo(MySQLDataConsistencyChecker.class));
        assertThat(scalingEntry.getImporterClass(), equalTo(MySQLImporter.class));
        assertThat(scalingEntry.getInventoryDumperClass(), equalTo(MySQLInventoryDumper.class));
        assertThat(scalingEntry.getIncrementalDumperClass(), equalTo(MySQLIncrementalDumper.class));
    }
}
