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
import org.apache.shardingsphere.scaling.mysql.component.MySQLBinlogDumper;
import org.apache.shardingsphere.scaling.mysql.component.MySQLDataConsistencyChecker;
import org.apache.shardingsphere.scaling.mysql.component.MySQLDataSourceChecker;
import org.apache.shardingsphere.scaling.mysql.component.MySQLImporter;
import org.apache.shardingsphere.scaling.mysql.component.MySQLInventoryDumper;
import org.apache.shardingsphere.scaling.mysql.component.MySQLPositionInitializer;
import org.apache.shardingsphere.scaling.mysql.component.MySQLScalingSQLBuilder;

/**
 * MySQL scaling entry.
 */
public final class MySQLScalingEntry implements ScalingEntry {
    
    @Override
    public Class<MySQLInventoryDumper> getInventoryDumperClass() {
        return MySQLInventoryDumper.class;
    }
    
    @Override
    public Class<MySQLBinlogDumper> getIncrementalDumperClass() {
        return MySQLBinlogDumper.class;
    }
    
    @Override
    public Class<MySQLPositionInitializer> getPositionInitializer() {
        return MySQLPositionInitializer.class;
    }
    
    @Override
    public Class<MySQLImporter> getImporterClass() {
        return MySQLImporter.class;
    }
    
    @Override
    public Class<MySQLDataSourceChecker> getDataSourceCheckerClass() {
        return MySQLDataSourceChecker.class;
    }
    
    @Override
    public Class<MySQLDataConsistencyChecker> getDataConsistencyCheckerClass() {
        return MySQLDataConsistencyChecker.class;
    }
    
    @Override
    public Class<MySQLScalingSQLBuilder> getSQLBuilderClass() {
        return MySQLScalingSQLBuilder.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
