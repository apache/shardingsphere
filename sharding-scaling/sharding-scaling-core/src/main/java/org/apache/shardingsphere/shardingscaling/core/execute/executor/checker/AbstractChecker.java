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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.checker;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.shardingscaling.core.exception.DatasourceCheckFailedException;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

import java.sql.SQLException;

/**
 * generic checker implement.
 *
 * @author ssxlulu
 */
public abstract class AbstractChecker implements Checker {

    @Getter(AccessLevel.PROTECTED)
    private final DataSourceFactory dataSourceFactory;

    public AbstractChecker(final DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public final void checkConnection() {
        try {
            for (HikariDataSource hikariDataSource : dataSourceFactory.getCachedDataSources().values()) {
                hikariDataSource.getConnection();
            }
        } catch (SQLException e) {
            throw new DatasourceCheckFailedException("Datasources check failed!");
        }
    }
}
