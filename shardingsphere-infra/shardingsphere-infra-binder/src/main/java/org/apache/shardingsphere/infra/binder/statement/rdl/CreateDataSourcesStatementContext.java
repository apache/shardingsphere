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

package org.apache.shardingsphere.infra.binder.statement.rdl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.statement.rdl.util.DataSourceConnectionUrlUtil;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.distsql.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.DataSourceConnectionSegment;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;

/**
 * Create dataSource statement context.
 */
@Getter
public final class CreateDataSourcesStatementContext extends CommonSQLStatementContext<CreateDataSourcesStatement> {
    
    private final DatabaseType databaseType;
    
    public CreateDataSourcesStatementContext(final CreateDataSourcesStatement sqlStatement, final DatabaseType databaseType) {
        super(sqlStatement);
        this.databaseType = databaseType;
    }
    
    /**
     * Get URL.
     *
     * @param segment segment
     * @return URL
     */
    public String getUrl(final DataSourceConnectionSegment segment) {
        return DataSourceConnectionUrlUtil.getUrl(segment, databaseType);
    }
}
