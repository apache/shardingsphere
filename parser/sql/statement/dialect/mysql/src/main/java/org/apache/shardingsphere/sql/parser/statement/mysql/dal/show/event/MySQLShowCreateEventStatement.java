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

package org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.event;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

/**
 * Show create event statement for MySQL.
 */
@Getter
public final class MySQLShowCreateEventStatement extends DALStatement {
    
    private SQLStatementAttributes attributes;
    
    private final String eventName;
    
    public MySQLShowCreateEventStatement(final DatabaseType databaseType, final String eventName) {
        super(databaseType);
        this.eventName = eventName;
    }
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(new AllowNotUseDatabaseSQLStatementAttribute(true));
    }
}
