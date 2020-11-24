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
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.JDBCDumper;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.LogDumper;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.DataSourceChecker;

/**
 * Scaling entry.
 */
public interface ScalingEntry extends DatabaseTypeAwareSPI {
    
    /**
     * Get JDBC dumper type.
     *
     * @return JDBC dumper type
     */
    Class<? extends JDBCDumper> getJdbcDumperClass();
    
    /**
     * Get log dumper type.
     *
     * @return log dumper type
     */
    Class<? extends LogDumper> getLogDumperClass();

    /**
     * Get position manager type.
     *
     * @return position manager type
     */
    Class<? extends PositionManager> getPositionManager();
    
    /**
     * Get importer type.
     *
     * @return importer type
     */
    Class<? extends Importer> getImporterClass();

    /**
     * Get data source checker.
     *
     * @return data source checker type
     */
    Class<? extends DataSourceChecker> getDataSourceCheckerClass();
    
    /**
     * Get data consistency checker.
     *
     * @return data consistency checker type
     */
    Class<? extends DataConsistencyChecker> getDataConsistencyCheckerClass();
}
