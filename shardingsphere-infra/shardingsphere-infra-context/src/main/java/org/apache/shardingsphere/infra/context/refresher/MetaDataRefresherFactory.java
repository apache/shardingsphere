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

package org.apache.shardingsphere.infra.context.refresher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.AlterIndexStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.AlterTableStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.CreateIndexStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.CreateTableStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.CreateViewStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.DropIndexStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.DropTableStatementSchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.type.DropViewStatementSchemaRefresher;
import org.apache.shardingsphere.infra.federation.metadata.refresher.type.AlterTableFederationMetaDataRefresher;
import org.apache.shardingsphere.infra.federation.metadata.refresher.type.CreateTableFederationMetaDataRefresher;
import org.apache.shardingsphere.infra.federation.metadata.refresher.type.DropTableFederationMetaDataRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ShardingSphere meta data refresher factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataRefresherFactory {
    
    private static final Map<Class<?>, Collection<MetaDataRefresher>> REGISTRY = new HashMap<>();
    
    static {
        REGISTRY.put(CreateTableStatement.class, new LinkedHashSet<>());
        REGISTRY.put(AlterTableStatement.class, new LinkedHashSet<>());
        REGISTRY.put(DropTableStatement.class, new LinkedHashSet<>());
        REGISTRY.put(CreateIndexStatement.class, new LinkedHashSet<>());
        REGISTRY.put(AlterIndexStatement.class, new LinkedHashSet<>());
        REGISTRY.put(DropIndexStatement.class, new LinkedHashSet<>());
        REGISTRY.put(CreateViewStatement.class, new LinkedHashSet<>());
        REGISTRY.put(DropViewStatement.class, new LinkedHashSet<>());
        REGISTRY.get(CreateTableStatement.class).add(new CreateTableStatementSchemaRefresher());
        REGISTRY.get(CreateTableStatement.class).add(new CreateTableFederationMetaDataRefresher());
        REGISTRY.get(AlterTableStatement.class).add(new AlterTableStatementSchemaRefresher());
        REGISTRY.get(AlterTableStatement.class).add(new AlterTableFederationMetaDataRefresher());
        REGISTRY.get(DropTableStatement.class).add(new DropTableStatementSchemaRefresher());
        REGISTRY.get(DropTableStatement.class).add(new DropTableFederationMetaDataRefresher());
        REGISTRY.get(CreateIndexStatement.class).add(new CreateIndexStatementSchemaRefresher());
        REGISTRY.get(AlterIndexStatement.class).add(new AlterIndexStatementSchemaRefresher());
        REGISTRY.get(DropIndexStatement.class).add(new DropIndexStatementSchemaRefresher());
        REGISTRY.get(CreateViewStatement.class).add(new CreateViewStatementSchemaRefresher());
        REGISTRY.get(DropViewStatement.class).add(new DropViewStatementSchemaRefresher());
    }
    
    /**
     * Create new instance of schema refresher.
     *†
     * @param sqlStatement SQL statement
     * @return instance of schema refresher
     */
    public static Collection<MetaDataRefresher> newInstance(final SQLStatement sqlStatement) {
        for (Entry<Class<?>, Collection<MetaDataRefresher>> entry : REGISTRY.entrySet()) {
            if (entry.getKey().isAssignableFrom(sqlStatement.getClass())) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }
}
