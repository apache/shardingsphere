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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.driver.jdbc.adapter.WrapperAdapter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Unsupported {@code Datasource} methods.
 */
public abstract class AbstractUnsupportedOperationDataSource extends WrapperAdapter implements DataSource {
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Setter
    @Getter
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    @Override
    public final int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("unsupported getLoginTimeout()");
    }
    
    @Override
    public final void setLoginTimeout(final int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("unsupported setLoginTimeout(int seconds)");
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
}
