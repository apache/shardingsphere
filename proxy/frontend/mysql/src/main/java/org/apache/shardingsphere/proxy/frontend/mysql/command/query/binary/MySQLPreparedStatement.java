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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Binary prepared statement for MySQL.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class MySQLPreparedStatement implements PreparedStatement {
    
    private final String sql;
    
    private final SQLStatement sqlStatement;
    
    @NonNull
    private final SQLStatementContext<?> sqlStatementContext;
    
    private List<MySQLPreparedStatementParameterType> parameterTypes = Collections.emptyList();
    
    private final Map<Integer, byte[]> longData = new ConcurrentHashMap<>();
    
    @Override
    public Optional<SQLStatementContext<?>> getSqlStatementContext() {
        return Optional.of(sqlStatementContext);
    }
}
