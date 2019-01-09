/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.jta.connection.dialect;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnectionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcDataSourceFactory;
import org.h2.jdbcx.JdbcXAConnection;
import org.h2.message.TraceObject;

import javax.sql.XADataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * H2 sharding XA connection wrapper.
 *
 * @author zhaojun
 */
@Slf4j
public final class H2ShardingXAConnectionWrapper implements ShardingXAConnectionWrapper {
    
    private static final int XA_DATA_SOURCE = 13;
    
    private static final Constructor<JdbcXAConnection> CONSTRUCTOR = getH2JdbcXAConstructor();
    
    private static final Method NEXT_ID = getNextIdMethod();
    
    private static final JdbcDataSourceFactory FACTORY = new JdbcDataSourceFactory();
    
    private static Constructor<JdbcXAConnection> getH2JdbcXAConstructor() {
        try {
            Constructor<JdbcXAConnection> h2XAConnectionConstructor = JdbcXAConnection.class.getDeclaredConstructor(JdbcDataSourceFactory.class, Integer.TYPE, JdbcConnection.class);
            h2XAConnectionConstructor.setAccessible(true);
            return h2XAConnectionConstructor;
        } catch (final NoSuchMethodException ex) {
            throw new ShardingException("Could not find constructor of H2 XA connection");
        }
    }
    
    private static Method getNextIdMethod() {
        try {
            Method method = TraceObject.class.getDeclaredMethod("getNextId", Integer.TYPE);
            method.setAccessible(true);
            return method;
        } catch (final NoSuchMethodException ex) {
            throw new ShardingException("Could not find getNextId of H2 XA DataSource");
        }
    }
    
    @Override
    public ShardingXAConnection wrap(final String resourceName, final XADataSource xaDataSource, final Connection connection) {
        try {
            Connection h2PhysicalConnection = (Connection) connection.unwrap(Class.forName("org.h2.jdbc.JdbcConnection"));
            JdbcXAConnection jdbcXAConnection = CONSTRUCTOR.newInstance(FACTORY, NEXT_ID.invoke(null, XA_DATA_SOURCE), h2PhysicalConnection);
            return new ShardingXAConnection(resourceName, jdbcXAConnection);
        } catch (final ClassNotFoundException | SQLException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            log.error("Failed to wrap a connection to ShardingXAConnection");
            throw new ShardingException(ex);
        }
    }
}
