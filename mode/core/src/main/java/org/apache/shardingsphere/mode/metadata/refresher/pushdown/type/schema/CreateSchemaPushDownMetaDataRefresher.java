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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.schema;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Create schema push down meta data refresher.
 */
public final class CreateSchemaPushDownMetaDataRefresher implements PushDownMetaDataRefresher<CreateSchemaStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final CreateSchemaStatement sqlStatement, final ConfigurationProperties props) {
        getSchemaName(sqlStatement).ifPresent(optional -> metaDataManagerPersistService.createSchema(database, optional.getValue().toLowerCase()));
    }
    
    private static Optional<IdentifierValue> getSchemaName(final CreateSchemaStatement sqlStatement) {
        return sqlStatement.getSchemaName().isPresent() ? sqlStatement.getSchemaName() : sqlStatement.getUsername();
    }
    
    @Override
    public Class<CreateSchemaStatement> getType() {
        return CreateSchemaStatement.class;
    }
}
