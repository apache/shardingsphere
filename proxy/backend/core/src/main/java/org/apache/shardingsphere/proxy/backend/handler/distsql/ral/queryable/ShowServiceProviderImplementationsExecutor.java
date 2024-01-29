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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.handler.type.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowServiceProviderImplementationsStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.exception.InvalidValueException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Show service provider implementations executor.
 */
public final class ShowServiceProviderImplementationsExecutor implements DistSQLQueryExecutor<ShowServiceProviderImplementationsStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "class_path");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowServiceProviderImplementationsStatement sqlStatement, final ContextManager contextManager) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Object each : ShardingSphereServiceLoader.getServiceInstances(getServiceProviderImplementationClass(sqlStatement))) {
            TypedSPI implementation = (TypedSPI) each;
            result.add(new LocalDataQueryResultRow(implementation.getClass().getSimpleName(), implementation.getType(), implementation.getClass().getName()));
        }
        return result;
    }
    
    private static Class<?> getServiceProviderImplementationClass(final ShowServiceProviderImplementationsStatement sqlStatement) {
        try {
            return Class.forName(sqlStatement.getServiceProviderInterface());
        } catch (final ClassNotFoundException ex) {
            throw new InvalidValueException(sqlStatement.getServiceProviderInterface());
        }
    }
    
    @Override
    public Class<ShowServiceProviderImplementationsStatement> getType() {
        return ShowServiceProviderImplementationsStatement.class;
    }
}
