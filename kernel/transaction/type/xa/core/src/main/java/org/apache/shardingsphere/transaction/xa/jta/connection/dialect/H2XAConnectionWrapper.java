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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * XA connection wrapper for H2.
 */
public final class H2XAConnectionWrapper implements XAConnectionWrapper {
    
    private static final int XA_DATA_SOURCE_TRACE_TYPE_ID = 13;
    
    private static volatile Class<Connection> jdbcConnectionClass;
    
    private static volatile Constructor<?> xaConnectionConstructor;
    
    private static volatile Method nextIdMethod;
    
    private static volatile Object dataSourceFactory;
    
    private static volatile boolean initialized;
    
    @Override
    public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) throws SQLException {
        if (!initialized) {
            loadReflection();
            initialized = true;
        }
        return createXAConnection(connection.unwrap(jdbcConnectionClass));
    }
    
    private void loadReflection() {
        jdbcConnectionClass = getJDBCConnectionClass();
        xaConnectionConstructor = getXAConnectionConstructor();
        nextIdMethod = getNextIdMethod();
        dataSourceFactory = createDataSourceFactory();
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Class<Connection> getJDBCConnectionClass() {
        return (Class<Connection>) Class.forName("org.h2.jdbc.JdbcConnection");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Constructor<?> getXAConnectionConstructor() {
        Constructor<?> result = Class.forName("org.h2.jdbcx.JdbcXAConnection").getDeclaredConstructor(
                Class.forName("org.h2.jdbcx.JdbcDataSourceFactory"), Integer.TYPE, Class.forName("org.h2.jdbc.JdbcConnection"));
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Method getNextIdMethod() {
        Method result = Class.forName("org.h2.message.TraceObject").getDeclaredMethod("getNextId", Integer.TYPE);
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object createDataSourceFactory() {
        return Class.forName("org.h2.jdbcx.JdbcDataSourceFactory").getDeclaredConstructor().newInstance();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private XAConnection createXAConnection(final Connection connection) {
        return (XAConnection) xaConnectionConstructor.newInstance(dataSourceFactory, nextIdMethod.invoke(null, XA_DATA_SOURCE_TRACE_TYPE_ID), connection);
    }
    
    @Override
    public String getType() {
        return "H2";
    }
}
