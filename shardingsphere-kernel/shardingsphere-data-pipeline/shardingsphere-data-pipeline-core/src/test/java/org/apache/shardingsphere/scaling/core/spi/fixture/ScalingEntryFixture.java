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

package org.apache.shardingsphere.scaling.core.spi.fixture;

import org.apache.shardingsphere.data.pipeline.spi.importer.Importer;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentChecker;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;

public final class ScalingEntryFixture implements ScalingEntry {
    
    @Override
    public Class<? extends InventoryDumper> getInventoryDumperClass() {
        return InventoryDumper.class;
    }
    
    @Override
    public Class<? extends IncrementalDumper> getIncrementalDumperClass() {
        return IncrementalDumper.class;
    }
    
    @Override
    public Class<? extends Importer> getImporterClass() {
        return Importer.class;
    }
    
    @Override
    public Class<? extends EnvironmentChecker> getEnvironmentCheckerClass() {
        return EnvironmentChecker.class;
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
