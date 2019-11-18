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

package info.avalon566.shardingscaling.core.execute.reader;

import info.avalon566.shardingscaling.core.config.RdbmsConfiguration;
import info.avalon566.shardingscaling.core.spi.ScalingEntry;
import info.avalon566.shardingscaling.core.spi.ScalingEntryLoader;
import lombok.SneakyThrows;

/**
 * reader factory.
 *
 * @author yangyi
 */
public final class ReaderFactory {
    
    /**
     * New instance of JDBC reader.
     *
     * @param rdbmsConfiguration rdbms configuration
     * @return JDBC reader
     */
    @SneakyThrows
    public static JdbcReader newInstanceJdbcReader(final RdbmsConfiguration rdbmsConfiguration) {
        return newInstanceJdbcReader(rdbmsConfiguration.getDataSourceConfiguration().getDatabaseType().getName(), rdbmsConfiguration);
    }
    
    /**
     * New instance of JDBC reader.
     *
     * @param databaseType database type
     * @param rdbmsConfiguration rdbms configuration
     * @return JDBC reader
     */
    @SneakyThrows
    public static JdbcReader newInstanceJdbcReader(final String databaseType, final RdbmsConfiguration rdbmsConfiguration) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getJdbcReaderClass().getConstructor(RdbmsConfiguration.class).newInstance(rdbmsConfiguration);
    }
    
    /**
     * New instance of log reader.
     *
     * @param rdbmsConfiguration rdbms configuration
     * @param position log position
     * @return log reader
     */
    @SneakyThrows
    public static LogReader newInstanceLogReader(final RdbmsConfiguration rdbmsConfiguration, final LogPosition position) {
        return newInstanceLogReader(rdbmsConfiguration.getDataSourceConfiguration().getDatabaseType().getName(), rdbmsConfiguration, position);
    }
    
    /**
     * New instance of log reader.
     *
     * @param databaseType database type
     * @param rdbmsConfiguration rdbms configuration
     * @param position log position
     * @return log reader
     */
    @SneakyThrows
    public static LogReader newInstanceLogReader(final String databaseType, final RdbmsConfiguration rdbmsConfiguration, final LogPosition position) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getLogReaderClass().getConstructor(RdbmsConfiguration.class, LogPosition.class).newInstance(rdbmsConfiguration, position);
    }
}
