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

package org.apache.shardingsphere.transaction.xa.jta.connection.dialect;

import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * XA connection wrapper for Firebird.
 */
public final class FirebirdXAConnectionWrapper implements XAConnectionWrapper {
    
    private Class<Connection> jdbcConnectionClass;
    
    private Constructor<?> xaConnectionConstructor;
    
    @Override
    public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) throws SQLException {
        return createXAConnection(connection.unwrap(jdbcConnectionClass));
    }
    
    @Override
    public void init(final Properties props) {
        loadReflection();
    }
    
    private void loadReflection() {
        jdbcConnectionClass = getJDBCConnectionClass();
        xaConnectionConstructor = getXAConnectionConstructor();
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Class<Connection> getJDBCConnectionClass() {
        return (Class<Connection>) Class.forName("org.firebirdsql.jdbc.FBConnection");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Constructor<?> getXAConnectionConstructor() {
        return Class.forName("org.firebirdsql.ds.FBXAConnection").getDeclaredConstructor(Class.forName("org.firebirdsql.jdbc.FBConnection"));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private XAConnection createXAConnection(final Connection connection) {
        xaConnectionConstructor.setAccessible(true);
        return (XAConnection) xaConnectionConstructor.newInstance(connection);
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
