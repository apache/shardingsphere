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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.util.ConfigurationLogger;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.EncryptRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.spi.database.DatabaseType;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Encrypt data source.
 *
 * @author panjuan
 */
@Getter
@Setter
public class EncryptDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final DataSource dataSource;
    
    private final EncryptRuntimeContext runtimeContext;
    
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    public EncryptDataSource(final DataSource dataSource, final EncryptRule encryptRule, final Properties props) throws SQLException {
        ConfigurationLogger.log(encryptRule.getEncryptRuleConfig());
        ConfigurationLogger.log(props);
        this.dataSource = dataSource;
        runtimeContext = new EncryptRuntimeContext(dataSource, encryptRule, props, getDatabaseType());
    }
    
    private DatabaseType getDatabaseType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypes.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
    }
    
    @Override
    public EncryptConnection getConnection() throws SQLException {
        return new EncryptConnection(dataSource.getConnection(), runtimeContext);
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public void close() {
        try {
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
