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

package org.apache.shardingsphere.sql.parser.statement.mysql.dal;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

/**
 * Kill statement for MySQL.
 */
@Getter
public final class MySQLKillStatement extends DALStatement {
    
    private final String processId;
    
    private final String scope;
    
    public MySQLKillStatement(final DatabaseType databaseType, final String processId, final String scope) {
        super(databaseType);
        this.processId = processId;
        this.scope = scope;
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return new SQLStatementAttributes(new AllowNotUseDatabaseSQLStatementAttribute(true));
    }
}
