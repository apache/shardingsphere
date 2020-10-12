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
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcDataSourceFactory;
import org.h2.jdbcx.JdbcXAConnection;
import org.h2.message.TraceObject;

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
    
    private static final Constructor<JdbcXAConnection> CONSTRUCTOR = getH2JdbcXAConstructor();
    
    private static final Method NEXT_ID = getNextIdMethod();
    
    private static final JdbcDataSourceFactory FACTORY = new JdbcDataSourceFactory();
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Constructor<JdbcXAConnection> getH2JdbcXAConstructor() {
        Constructor<JdbcXAConnection> result = JdbcXAConnection.class.getDeclaredConstructor(JdbcDataSourceFactory.class, Integer.TYPE, JdbcConnection.class);
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Method getNextIdMethod() {
        Method result = TraceObject.class.getDeclaredMethod("getNextId", Integer.TYPE);
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows({SQLException.class, ReflectiveOperationException.class})
    @Override
    public XAConnection wrap(final XADataSource xaDataSource, final Connection connection) {
        Connection physicalConnection = connection.unwrap(JdbcConnection.class);
        return CONSTRUCTOR.newInstance(FACTORY, NEXT_ID.invoke(null, XA_DATA_SOURCE), physicalConnection);
    }
}
