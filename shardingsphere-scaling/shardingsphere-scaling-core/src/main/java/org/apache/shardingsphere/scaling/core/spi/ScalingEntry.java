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

package org.apache.shardingsphere.scaling.core.spi;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeAwareSPI;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilder;
import org.apache.shardingsphere.scaling.core.executor.dumper.IncrementalDumper;
import org.apache.shardingsphere.scaling.core.executor.dumper.InventoryDumper;
import org.apache.shardingsphere.scaling.core.executor.importer.Importer;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentChecker;
import org.apache.shardingsphere.scaling.core.job.position.PositionInitializer;

/**
 * Scaling entry.
 */
public interface ScalingEntry extends DatabaseTypeAwareSPI {
    
    /**
     * Get inventory dumper type.
     *
     * @return inventory dumper type
     */
    Class<? extends InventoryDumper> getInventoryDumperClass();
    
    /**
     * Get incremental dumper type.
     *
     * @return incremental dumper type
     */
    Class<? extends IncrementalDumper> getIncrementalDumperClass();
    
    /**
     * Get position initializer type.
     *
     * @return position initializer type
     */
    Class<? extends PositionInitializer> getPositionInitializerClass();
    
    /**
     * Get importer type.
     *
     * @return importer type
     */
    Class<? extends Importer> getImporterClass();
    
    /**
     * Get environment checker type.
     *
     * @return environment checker type
     */
    Class<? extends EnvironmentChecker> getEnvironmentCheckerClass();
    
    /**
     * Get SQL builder class.
     *
     * @return SQL builder type
     */
    Class<? extends ScalingSQLBuilder> getSQLBuilderClass();
}
