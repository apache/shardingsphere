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

package org.apache.shardingsphere.infra.config.datasource.closer.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.shardingsphere.infra.config.datasource.closer.DataSourceCloser;

/**
 * Close hikari data source.
 */
public final class DefaultDataSourceCloser implements DataSourceCloser {

    @Override
    public String getType() {
        return "Default";
    }

    @Override
    public void closeDataSource(final DataSource dataSource) throws SQLException {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (final Exception ex) {
                throw new SQLException(ex);
            }
        }
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
