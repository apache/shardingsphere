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

package org.apache.shardingsphere.scaling.opengauss;

import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.opengauss.component.OpenGaussImporter;
import org.apache.shardingsphere.scaling.opengauss.component.OpenGaussPositionInitializer;
import org.apache.shardingsphere.scaling.opengauss.component.OpenGaussWalDumper;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLInventoryDumper;
import org.apache.shardingsphere.scaling.postgresql.component.checker.PostgreSQLDataConsistencyChecker;
import org.apache.shardingsphere.scaling.postgresql.component.checker.PostgreSQLDataSourceChecker;
import org.apache.shardingsphere.scaling.postgresql.component.checker.PostgreSQLEnvironmentChecker;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OpenGaussScalingEntryTest {
    
    @Test
    public void assertGetScalingEntryByDatabaseType() throws IllegalAccessException, InstantiationException {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance("openGauss");
        assertTrue(scalingEntry instanceof OpenGaussScalingEntry);
        assertThat(scalingEntry.getPositionInitializerClass(), equalTo(OpenGaussPositionInitializer.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass(), equalTo(PostgreSQLEnvironmentChecker.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass().newInstance().getDataSourceCheckerClass(), equalTo(PostgreSQLDataSourceChecker.class));
        assertThat(scalingEntry.getEnvironmentCheckerClass().newInstance().getDataConsistencyCheckerClass(), equalTo(PostgreSQLDataConsistencyChecker.class));
        assertThat(scalingEntry.getImporterClass(), equalTo(OpenGaussImporter.class));
        assertThat(scalingEntry.getInventoryDumperClass(), equalTo(PostgreSQLInventoryDumper.class));
        assertThat(scalingEntry.getIncrementalDumperClass(), equalTo(OpenGaussWalDumper.class));
    }
}
