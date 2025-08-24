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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Binary prepared statement for MySQL.
 * This class may be accessed serially in different threads due to MySQL Proxy using a shared unbound thread pool.
 */
@RequiredArgsConstructor
@Getter
public final class MySQLServerPreparedStatement implements ServerPreparedStatement {
    
    private final String sql;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext;
    
    private final List<Integer> parameterColumnDefinitionFlags;
    
    private final List<MySQLPreparedStatementParameterType> parameterTypes = new CopyOnWriteArrayList<>();
    
    private final Map<Integer, byte[]> longData = new ConcurrentHashMap<>();
}
