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

package org.apache.shardingsphere.infra.metadata.schema.refresher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.CreateIndexStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.CreateTableStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.CreateViewStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.DropIndexStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.AlterTableStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.DropTableStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresher.impl.DropViewStatementSchemaRefreshStrategy;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Meta data refresh strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataRefreshStrategyFactory {
    
    private static final Map<Class<?>, SchemaRefreshStrategy<?>> REGISTRY = new HashMap<>();
    
    static {
        REGISTRY.put(CreateTableStatement.class, new CreateTableStatementSchemaRefreshStrategy());
        REGISTRY.put(AlterTableStatement.class, new AlterTableStatementSchemaRefreshStrategy());
        REGISTRY.put(DropTableStatement.class, new DropTableStatementSchemaRefreshStrategy());
        REGISTRY.put(CreateIndexStatement.class, new CreateIndexStatementSchemaRefreshStrategy());
        REGISTRY.put(DropIndexStatement.class, new DropIndexStatementSchemaRefreshStrategy());
        REGISTRY.put(CreateViewStatement.class, new CreateViewStatementSchemaRefreshStrategy());
        REGISTRY.put(DropViewStatement.class, new DropViewStatementSchemaRefreshStrategy());
    }
    
    /**
     * Create new instance of meta data refresh strategy.
     *
     * @param sqlStatement SQL statement
     * @return meta data refresh strategy
     */
    public static Optional<SchemaRefreshStrategy> newInstance(final SQLStatement sqlStatement) {
        return REGISTRY.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(sqlStatement.getClass())).findFirst().map(Entry::getValue);
    }
}
