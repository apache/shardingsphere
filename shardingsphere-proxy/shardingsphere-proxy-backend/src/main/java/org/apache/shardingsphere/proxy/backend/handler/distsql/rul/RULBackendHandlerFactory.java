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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rul.RULStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.FormatStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.ParseStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.PreviewStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql.FormatSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql.ParseDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql.PreviewHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * RUL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RULBackendHandlerFactory {
    
    private static final Map<Class<? extends RULStatement>, Class<? extends RULBackendHandler<?>>> HANDLERS = new HashMap<>();
    
    static {
        HANDLERS.put(ParseStatement.class, ParseDistSQLHandler.class);
        HANDLERS.put(PreviewStatement.class, PreviewHandler.class);
        HANDLERS.put(FormatStatement.class, FormatSQLHandler.class);
    }
    
    /**
     * Create new instance of RUL backend handler.
     *
     * @param sqlStatement RUL statement
     * @param connectionSession connection session
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static ProxyBackendHandler newInstance(final RULStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        return createRULBackendHandler(sqlStatement, connectionSession);
    }
    
    private static RULBackendHandler<?> newInstance(final Class<? extends RULBackendHandler<?>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new UnsupportedOperationException(String.format("Can not find public constructor for class `%s`", clazz.getName()));
        }
    }
    
    private static RULBackendHandler<?> createRULBackendHandler(final RULStatement sqlStatement, final ConnectionSession connectionSession) {
        Class<? extends RULBackendHandler<?>> clazz = HANDLERS.get(sqlStatement.getClass());
        if (null == clazz) {
            throw new UnsupportedOperationException(String.format("Unsupported SQL statement : %s", sqlStatement.getClass().getCanonicalName()));
        }
        RULBackendHandler<?> result = newInstance(clazz);
        result.init(sqlStatement, connectionSession);
        return result;
    }
}
