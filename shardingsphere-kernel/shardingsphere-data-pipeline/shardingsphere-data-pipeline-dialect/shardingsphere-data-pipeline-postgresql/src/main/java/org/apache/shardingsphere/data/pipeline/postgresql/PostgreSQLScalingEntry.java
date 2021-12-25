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

import org.apache.shardingsphere.data.pipeline.postgresql.importer.PostgreSQLImporter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLInventoryDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLPositionInitializer;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLWalDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.PostgreSQLPipelineSQLBuilder;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;

/**
 * PostgreSQL scaling entry.
 */
public final class PostgreSQLScalingEntry implements ScalingEntry {
    
    @Override
    public Class<PostgreSQLInventoryDumper> getInventoryDumperClass() {
        return PostgreSQLInventoryDumper.class;
    }
    
    @Override
    public Class<PostgreSQLWalDumper> getIncrementalDumperClass() {
        return PostgreSQLWalDumper.class;
    }
    
    @Override
    public Class<PostgreSQLPositionInitializer> getPositionInitializerClass() {
        return PostgreSQLPositionInitializer.class;
    }
    
    @Override
    public Class<PostgreSQLImporter> getImporterClass() {
        return PostgreSQLImporter.class;
    }
    
    @Override
    public Class<PostgreSQLEnvironmentChecker> getEnvironmentCheckerClass() {
        return PostgreSQLEnvironmentChecker.class;
    }
    
    @Override
    public Class<PostgreSQLPipelineSQLBuilder> getSQLBuilderClass() {
        return PostgreSQLPipelineSQLBuilder.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
