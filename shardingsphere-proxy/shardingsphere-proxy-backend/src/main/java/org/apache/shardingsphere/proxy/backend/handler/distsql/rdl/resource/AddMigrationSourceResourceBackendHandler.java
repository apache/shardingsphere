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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddMigrationResourceStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.DatabaseRequiredBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Map;

/**
 * Add resource backend handler.
 */
@Slf4j
public final class AddMigrationSourceResourceBackendHandler extends DatabaseRequiredBackendHandler<AddMigrationResourceStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidator validator;
    
    public AddMigrationSourceResourceBackendHandler(final AddMigrationResourceStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        databaseType = connectionSession.getDatabaseType();
        validator = new DataSourcePropertiesValidator();
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final AddMigrationResourceStatement sqlStatement) throws DistSQLException {
        checkSQLStatement(databaseName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources());
        DatabaseType storeType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResource().getDatabaseType();
        validator.validate(dataSourcePropsMap, storeType);
        // TODO update resource later
        return new UpdateResponseHeader(sqlStatement);
    }
    
    // TODO need to check sql statement
    private void checkSQLStatement(final String databaseName, final AddMigrationResourceStatement sqlStatement) {
    }
}
