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

package org.apache.shardingsphere.shardingscaling.core.spi;

import org.apache.shardingsphere.shardingscaling.core.execute.executor.checker.DataSourceChecker;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.JDBCReader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManager;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.LogReader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;
import org.apache.shardingsphere.spi.database.type.DatabaseTypeAwareSPI;

/**
 * Scaling entry.
 */
public interface ScalingEntry extends DatabaseTypeAwareSPI {
    
    /**
     * Get JDBC reader type.
     *
     * @return JDBC reader type
     */
    Class<? extends JDBCReader> getJdbcReaderClass();
    
    /**
     * Get log reader type.
     *
     * @return log reader type
     */
    Class<? extends LogReader> getLogReaderClass();

    /**
     * Get log position manager type.
     *
     * @return log manager type
     */
    Class<? extends LogPositionManager> getLogPositionManager();
    
    /**
     * Get writer type.
     *
     * @return writer type
     */
    Class<? extends Writer> getWriterClass();

    /**
     * Get checker type.
     * @return checker type
     */
    Class<? extends DataSourceChecker> getCheckerClass();
}
