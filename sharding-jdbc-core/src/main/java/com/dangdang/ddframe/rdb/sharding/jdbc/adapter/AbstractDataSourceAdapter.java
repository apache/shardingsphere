/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 数据源适配类.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractDataSourceAdapter extends AbstractUnsupportedOperationDataSource {
    
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    @Override
    public final PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }
    
    @Override
    public final void setLogWriter(final PrintWriter out) throws SQLException {
        this.logWriter = out;
    }
    
    @Override
    public final Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
}
