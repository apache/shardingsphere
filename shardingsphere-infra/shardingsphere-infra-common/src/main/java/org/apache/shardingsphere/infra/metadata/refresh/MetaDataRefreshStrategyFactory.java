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

package org.apache.shardingsphere.infra.metadata.refresh;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.refresh.impl.AlterTableStatementMetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.impl.CreateIndexStatementMetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.impl.CreateTableStatementMetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.impl.DropIndexStatementMetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.impl.DropTableStatementMetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropTableStatementContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Meta data refresh strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataRefreshStrategyFactory {
    
    private static final Map<Class<?>, MetaDataRefreshStrategy> REGISTRY = new HashMap<>();
    
    static {
        REGISTRY.put(CreateTableStatementContext.class, new CreateTableStatementMetaDataRefreshStrategy());
        REGISTRY.put(AlterTableStatementContext.class, new AlterTableStatementMetaDataRefreshStrategy());
        REGISTRY.put(DropTableStatementContext.class, new DropTableStatementMetaDataRefreshStrategy());
        REGISTRY.put(CreateIndexStatementContext.class, new CreateIndexStatementMetaDataRefreshStrategy());
        REGISTRY.put(DropIndexStatementContext.class, new DropIndexStatementMetaDataRefreshStrategy());
    }
    
    /**
     * Create new instance of meta data refresh strategy.
     *
     * @param sqlStatementContext SQL statement context
     * @return meta data refresh strategy
     */
    public static Optional<MetaDataRefreshStrategy> newInstance(final SQLStatementContext<?> sqlStatementContext) {
        return Optional.ofNullable(REGISTRY.get(sqlStatementContext.getClass()));
    }
}
