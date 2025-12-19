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

package org.apache.shardingsphere.sql.parser.statement.mysql.dal.show;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.DatabaseSelectRequiredSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TablelessDataSourceBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

/**
 * Show transaction statement for MySQL.
 */
@Getter
@Setter
public final class MySQLShowTransactionStatement extends DALStatement {
    
    private FromDatabaseSegment fromDatabase;
    
    private WhereSegment where;
    
    public MySQLShowTransactionStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        String databaseName = null == fromDatabase ? null : fromDatabase.getDatabase().getIdentifier().getValue();
        return new SQLStatementAttributes(new DatabaseSelectRequiredSQLStatementAttribute(), new FromDatabaseSQLStatementAttribute(fromDatabase),
                new TablelessDataSourceBroadcastRouteSQLStatementAttribute(), new AllowNotUseDatabaseSQLStatementAttribute(true, databaseName));
    }
}
