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

package org.apache.shardingsphere.infra.metadata.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.mapper.type.CreateUserStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.type.GrantStatementEventMapper;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * SQL statement event mapper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementEventMapperFactory {
    
    private static final Map<Class<?>, SQLStatementEventMapper> REGISTRY = new HashMap<>();
    
    static {
        REGISTRY.put(GrantStatement.class, new GrantStatementEventMapper());
        REGISTRY.put(CreateUserStatement.class, new CreateUserStatementEventMapper());
    }
    
    /**
     * Create new instance of SQL statement event mapper.
     * 
     * @param sqlStatement SQL statement
     * @return instance of SQL statement event mapper
     */
    public static Optional<SQLStatementEventMapper> newInstance(final SQLStatement sqlStatement) {
        return REGISTRY.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(sqlStatement.getClass())).findFirst().map(Entry::getValue);
    }
}
