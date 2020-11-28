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

package org.apache.shardingsphere.scaling.postgresql;

import org.apache.shardingsphere.scaling.core.execute.executor.dumper.JDBCDumper;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.LogDumper;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.DataSourceChecker;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLDataConsistencyChecker;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLDataSourceChecker;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLImporter;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLJdbcDumper;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLPositionManager;
import org.apache.shardingsphere.scaling.postgresql.component.PostgreSQLWalDumper;

/**
 * PostgreSQL scaling entry.
 */
public final class PostgreSQLScalingEntry implements ScalingEntry {
    
    @Override
    public Class<? extends JDBCDumper> getJdbcDumperClass() {
        return PostgreSQLJdbcDumper.class;
    }
    
    @Override
    public Class<? extends LogDumper> getLogDumperClass() {
        return PostgreSQLWalDumper.class;
    }
    
    @Override
    public Class<? extends PositionManager> getPositionManager() {
        return PostgreSQLPositionManager.class;
    }
    
    @Override
    public Class<? extends Importer> getImporterClass() {
        return PostgreSQLImporter.class;
    }
    
    @Override
    public Class<? extends DataSourceChecker> getDataSourceCheckerClass() {
        return PostgreSQLDataSourceChecker.class;
    }
    
    @Override
    public Class<? extends DataConsistencyChecker> getDataConsistencyCheckerClass() {
        return PostgreSQLDataConsistencyChecker.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
