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

package org.apache.shardingsphere.proxy.backend.state.impl;

import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.proxy.backend.exception.UnavailableException;
import org.apache.shardingsphere.proxy.backend.state.spi.ProxyClusterState;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

/**
 * Unavailable proxy state.
 */
public final class UnavailableProxyState implements ProxyClusterState {
    
    @Override
    public void check(final SQLStatement sqlStatement) {
        if (isSupportedStatement(sqlStatement)) {
            return;
        }
        throw new UnavailableException();
    }
    
    private boolean isSupportedStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof ImportMetaDataStatement || sqlStatement instanceof ShowStatement || sqlStatement instanceof QueryableRALStatement || sqlStatement instanceof RQLStatement
                || sqlStatement instanceof UnlockClusterStatement || sqlStatement instanceof MySQLShowDatabasesStatement || sqlStatement instanceof MySQLUseStatement;
    }
    
    @Override
    public String getType() {
        return "UNAVAILABLE";
    }
}
