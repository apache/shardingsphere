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

package org.apache.shardingsphere.rdl.parser.binder.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.rdl.parser.binder.util.DataSourceConnectionUrlUtil;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.DataSourceConnectionSegment;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create dataSource statement context.
 */
@Getter
public final class CreateDataSourcesStatementContext extends CommonSQLStatementContext<CreateDataSourcesStatement> {
    
    private final Collection<DataSourceConnectionUrl> urls;
    
    public CreateDataSourcesStatementContext(final CreateDataSourcesStatement sqlStatement, final DatabaseType databaseType) {
        super(sqlStatement);
        urls = new LinkedList<>();
        for (DataSourceConnectionSegment each : sqlStatement.getConnectionInfos()) {
            urls.add(new DataSourceConnectionUrl(each.getName(), DataSourceConnectionUrlUtil.getUrl(each, databaseType), each.getUser(), each.getPassword()));
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class DataSourceConnectionUrl {
        
        private final String name;
        
        private final String url;
        
        private final String userName;
        
        private final String password;
    }
}
