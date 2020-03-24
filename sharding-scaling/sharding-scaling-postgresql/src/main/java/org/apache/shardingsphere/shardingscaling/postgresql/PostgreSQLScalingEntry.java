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

package org.apache.shardingsphere.shardingscaling.postgresql;

import org.apache.shardingsphere.shardingscaling.core.execute.executor.checker.DataSourceChecker;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManager;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.JDBCReader;
import org.apache.shardingsphere.shardingscaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.LogReader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;

/**
 * PostgreSQL scaling entry.
 */
public final class PostgreSQLScalingEntry implements ScalingEntry {
    
    @Override
    public Class<? extends JDBCReader> getJdbcReaderClass() {
        return PostgreSQLJdbcReader.class;
    }
    
    @Override
    public Class<? extends LogReader> getLogReaderClass() {
        return PostgreSQLWalReader.class;
    }
    
    @Override
    public Class<? extends LogPositionManager> getLogPositionManager() {
        return PostgreSQLLogPositionManager.class;
    }
    
    @Override
    public Class<? extends Writer> getWriterClass() {
        return PostgreSQLWriter.class;
    }
    
    @Override
    public Class<? extends DataSourceChecker> getCheckerClass() {
        return PostgreSQLDataSourceChecker.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
