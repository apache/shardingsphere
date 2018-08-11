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

package io.shardingsphere.proxy.backend.jdbc.connection;

import com.google.common.collect.Lists;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.backend.BackendExecutorContext;
import io.shardingsphere.proxy.config.RuleRegistry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Backend connection.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class BackendConnection implements AutoCloseable {
    
    private static final RuleRegistry RULE_REGISTRY = RuleRegistry.getInstance();
    
    private final ExecutorService executorService = BackendExecutorContext.getInstance().getExecutorService();
    
    private final Collection<Object> cachedConnections = new CopyOnWriteArrayList<>();
    
    private final Collection<Object> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<Object> cachedResultSets = new CopyOnWriteArrayList<>();
    
    /**
     * Get connection of current thread datasource.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        Connection result = RULE_REGISTRY.getBackendDataSource().getConnection(dataSourceName);
        cachedConnections.add(result);
        return result;
    }
    
    /**
     * Add statement.
     *
     * @param statement statement to be added
     */
    public void add(final Statement statement) {
        cachedStatements.add(statement);
    }
    
    /**
     * Add result set.
     *
     * @param resultSet result set to be added
     */
    public void add(final ResultSet resultSet) {
        cachedResultSets.add(resultSet);
    }
    
    @Override
    public void close() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        exceptions.addAll(closeItems(cachedResultSets));
        exceptions.addAll(closeItems(cachedStatements));
        exceptions.addAll(closeItems(cachedConnections));
        MasterVisitedManager.clear();
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    private Collection<SQLException> closeItems(final Collection<Object> objects) {
        Collection<SQLException> result = new LinkedList<>();
        Iterator<Object> objectIterator = objects.iterator();
        if (!objectIterator.hasNext()) {
            return result;
        }
        Object firstObject = objectIterator.next();
        List<Future<SQLException>> futureList = asyncCloseResources(Lists.newArrayList(objectIterator));
        syncCloseResource(result, firstObject);
        getSQLExceptionResults(result, futureList);
        return result;
    }
    
    private List<Future<SQLException>> asyncCloseResources(final Collection<Object> objects) {
        List<Future<SQLException>> result = new LinkedList<>();
        for (Object each : objects) {
            final Object object = each;
            result.add(executorService.submit(new Callable<SQLException>() {
                
                @Override
                public SQLException call() {
                    try {
                        closeResources(object);
                    } catch (final SQLException ex) {
                        return ex;
                    }
                    return null;
                }
            }));
        }
        return result;
    }
    
    private void syncCloseResource(final Collection<SQLException> result, final Object object) {
        try {
            closeResources(object);
        } catch (final SQLException ex) {
            result.add(ex);
        }
    }
    
    private void closeResources(final Object object) throws SQLException {
        if (object instanceof ResultSet) {
            ((ResultSet) object).close();
        } else if (object instanceof Statement) {
            ((Statement) object).close();
        } else if (object instanceof Connection) {
            ((Connection) object).close();
        }
    }
    
    private void getSQLExceptionResults(final Collection<SQLException> result, final List<Future<SQLException>> futureList) {
        for (Future<SQLException> each : futureList) {
            try {
                SQLException sqlException = each.get();
                if (null != sqlException) {
                    result.add(sqlException);
                }
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
    }
    
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException();
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
}
