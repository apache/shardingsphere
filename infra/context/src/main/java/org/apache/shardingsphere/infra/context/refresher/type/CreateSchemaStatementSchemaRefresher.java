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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.event.AddSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateSchemaStatementHandler;

import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for create schema statement.
 */
public final class CreateSchemaStatementSchemaRefresher implements MetaDataRefresher<CreateSchemaStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final CreateSchemaStatement sqlStatement, final ConfigurationProperties props) {
        Optional<IdentifierValue> schema = sqlStatement.getSchemaName().isPresent() ? sqlStatement.getSchemaName() : CreateSchemaStatementHandler.getUsername(sqlStatement);
        if (!schema.isPresent()) {
            return Optional.empty();
        }
        String actualSchemaName = schema.get().getValue().toLowerCase();
        database.putSchema(actualSchemaName, new ShardingSphereSchema());
        AddSchemaEvent event = new AddSchemaEvent(database.getName(), actualSchemaName);
        return Optional.of(event);
    }
    
    @Override
    public String getType() {
        return CreateSchemaStatement.class.getName();
    }
}
