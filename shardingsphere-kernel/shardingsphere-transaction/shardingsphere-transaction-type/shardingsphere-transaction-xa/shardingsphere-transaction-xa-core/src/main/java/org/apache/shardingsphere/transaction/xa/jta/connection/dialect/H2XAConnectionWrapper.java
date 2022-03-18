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
    
    private static final int XA_DATA_SOURCE = 13;
    
    private static final Constructor<?> CONSTRUCTOR = getH2JdbcXAConstructor();
    
    private static final Method NEXT_ID = getNextIdMethod();
    
    private static final Object FACTORY = newFactory();
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Constructor<?> getH2JdbcXAConstructor() {
        Constructor<?> result = Class.forName("org.h2.jdbcx.JdbcXAConnection").getDeclaredConstructor(
                Class.forName("org.h2.jdbcx.JdbcDataSourceFactory"), Integer.TYPE, Class.forName("org.h2.jdbc.JdbcConnection"));
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Method getNextIdMethod() {
        Method result = Class.forName("org.h2.message.TraceObject").getDeclaredMethod("getNextId", Integer.TYPE);
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object newFactory() {
        return Class.forName("org.h2.jdbcx.JdbcDataSourceFactory").getDeclaredConstructor().newInstance();
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows({SQLException.class, ReflectiveOperationException.class})
    @Override
    public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) {
        Connection physicalConnection = connection.unwrap((Class<Connection>) Class.forName("org.h2.jdbc.JdbcConnection"));
        return (XAConnection) CONSTRUCTOR.newInstance(FACTORY, NEXT_ID.invoke(null, XA_DATA_SOURCE), physicalConnection);
    }
}
