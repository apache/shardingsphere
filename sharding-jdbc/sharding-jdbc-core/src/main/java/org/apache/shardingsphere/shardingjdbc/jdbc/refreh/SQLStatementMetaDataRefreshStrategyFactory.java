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

package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SQL statement meta data refresh strategy factory.
 */
public final class SQLStatementMetaDataRefreshStrategyFactory {
    
    private static final Map<Class<?>, SQLStatementMetaDataRefreshStrategy<? extends SQLStatement>> REFRESH_MAP = new HashMap<>();
    
    static {
        REFRESH_MAP.put(CreateTableStatementContext.class, new CreateTableStatementMetaDataRefreshStrategy());
        REFRESH_MAP.put(AlterTableStatementContext.class, new AlterTableStatementMetaDataRefreshStrategy());
        REFRESH_MAP.put(DropTableStatementContext.class, new DropTableStatementMetaDataRefreshStrategy());
        REFRESH_MAP.put(CreateIndexStatementContext.class, new CreateIndexStatementMetaDataRefreshStrategy());
        REFRESH_MAP.put(DropIndexStatementContext.class, new DropIndexStatementMetaDataRefreshStrategy());
    }
    
    /**
     * New instance of SQL statement meta data refresh strategy.
     *
     * @param sqlStatementContext SQL statement context
     * @return SQL statement meta data refresh strategy
     */
    public static Optional<SQLStatementMetaDataRefreshStrategy<? extends SQLStatement>> newInstance(final SQLStatementContext sqlStatementContext) {
        return Optional.ofNullable(REFRESH_MAP.get(sqlStatementContext.getClass()));
    }
}
