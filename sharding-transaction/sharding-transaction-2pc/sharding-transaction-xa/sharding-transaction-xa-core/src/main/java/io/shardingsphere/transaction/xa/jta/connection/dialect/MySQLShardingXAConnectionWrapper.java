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
import io.shardingsphere.core.util.ReflectiveUtil;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnectionWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * MySQL sharding XA connection wrapper.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Slf4j
public final class MySQLShardingXAConnectionWrapper implements ShardingXAConnectionWrapper {
    
    @Override
    public ShardingXAConnection wrap(final String resourceName, final XADataSource xaDataSource, final Connection connection) {
        try {
            Connection physicalConnection = (Connection) connection.unwrap(Class.forName("com.mysql.jdbc.Connection"));
            Method wrapConnectionMethod = ReflectiveUtil.findMethod(xaDataSource, "wrapConnection", Connection.class);
            XAConnection xaConnection = (XAConnection) wrapConnectionMethod.invoke(xaDataSource, physicalConnection);
            return new ShardingXAConnection(resourceName, xaConnection);
        } catch (final Exception ex) {
            log.error("Failed to wrap a connection to ShardingXAConnection.");
            throw new ShardingException(ex);
        }
    }
}
