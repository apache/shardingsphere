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

package org.apache.shardingsphere.sharding.check.mysql;

import org.apache.shardingsphere.sharding.checker.sql.dml.DialectUnsupportedShardingSQLStatementProvider;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadDataStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadXMLStatement;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unsupported sharding SQL statement provider for MySQL.
 */
public final class MySQLUnsupportedShardingSQLStatementProvider implements DialectUnsupportedShardingSQLStatementProvider {
    
    private static final Collection<Class<? extends SQLStatement>> UNSUPPORTED_SQL_STATEMENT_TYPES = Arrays.asList(MySQLLoadDataStatement.class, MySQLLoadXMLStatement.class);
    
    @Override
    public Collection<Class<? extends SQLStatement>> getUnsupportedSQLStatementTypes() {
        return UNSUPPORTED_SQL_STATEMENT_TYPES;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
