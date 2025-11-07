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

package org.apache.shardingsphere.infra.route.mysql;

import org.apache.shardingsphere.infra.route.engine.tableless.DialectDALStatementBroadcastRouteDecider;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLAlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLDropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLSetResourceGroupStatement;

import java.util.Optional;

/**
 * Dialect DAL statement broadcast route decider for MySQL.
 */
public final class MySQLDALStatementBroadcastRouteDecider implements DialectDALStatementBroadcastRouteDecider {
    
    @Override
    public boolean isDataSourceBroadcastRoute(final DALStatement sqlStatement) {
        return false;
    }
    
    @Override
    public boolean isInstanceBroadcastRoute(final DALStatement sqlStatement) {
        Optional<AllowNotUseDatabaseSQLStatementAttribute> attribute = sqlStatement.getAttributes().findAttribute(AllowNotUseDatabaseSQLStatementAttribute.class);
        return isResourceGroupStatement(sqlStatement) || attribute.isPresent() && attribute.get().isAllowNotUseDatabase();
    }
    
    private boolean isResourceGroupStatement(final DALStatement sqlStatement) {
        return sqlStatement instanceof MySQLCreateResourceGroupStatement || sqlStatement instanceof MySQLAlterResourceGroupStatement || sqlStatement instanceof MySQLDropResourceGroupStatement
                || sqlStatement instanceof MySQLSetResourceGroupStatement;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
