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

package io.shardingsphere.proxy.backend.common.jdbc.execute.memory;

import io.shardingsphere.proxy.backend.common.jdbc.execute.stream.MemoryStrictlyExecuteEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Momory strictly execute engine for JDBC text protocol.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class TextMemoryStrictlyExecuteEngine extends MemoryStrictlyExecuteEngine {
    
    @Override
    protected Statement createStatement(final Connection connection, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return connection.createStatement();
    }
    
    @Override
    protected boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}
