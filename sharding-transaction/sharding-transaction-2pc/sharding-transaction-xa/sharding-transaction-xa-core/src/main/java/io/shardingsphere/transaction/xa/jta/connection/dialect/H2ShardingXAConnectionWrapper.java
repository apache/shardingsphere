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

import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnectionWrapper;
import lombok.SneakyThrows;
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcDataSourceFactory;
import org.h2.jdbcx.JdbcXAConnection;
import org.h2.message.TraceObject;

import javax.sql.XADataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * H2 sharding XA connection wrapper.
 *
 * @author zhaojun
 */
public final class H2ShardingXAConnectionWrapper implements ShardingXAConnectionWrapper {
    
    private static final int XA_DATA_SOURCE = 13;
    
    private static final Constructor<JdbcXAConnection> CONSTRUCTOR = getH2JdbcXAConstructor();
    
    private static final Method NEXT_ID = getNextIdMethod();
    
    private static final JdbcDataSourceFactory FACTORY = new JdbcDataSourceFactory();
    
    @SneakyThrows
    private static Constructor<JdbcXAConnection> getH2JdbcXAConstructor() {
        Constructor<JdbcXAConnection> result = JdbcXAConnection.class.getDeclaredConstructor(JdbcDataSourceFactory.class, Integer.TYPE, JdbcConnection.class);
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows
    private static Method getNextIdMethod() {
        Method result = TraceObject.class.getDeclaredMethod("getNextId", Integer.TYPE);
        result.setAccessible(true);
        return result;
    }
    
    @SneakyThrows
    @Override
    public ShardingXAConnection wrap(final String resourceName, final XADataSource xaDataSource, final Connection connection) {
        Connection h2PhysicalConnection = (Connection) connection.unwrap(Class.forName("org.h2.jdbc.JdbcConnection"));
        JdbcXAConnection jdbcXAConnection = CONSTRUCTOR.newInstance(FACTORY, NEXT_ID.invoke(null, XA_DATA_SOURCE), h2PhysicalConnection);
        return new ShardingXAConnection(resourceName, jdbcXAConnection);
    }
}
