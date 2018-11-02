/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.routing.RouteUnit;
import lombok.Getter;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Execute unit for JDBC statement.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
public final class StatementExecuteUnit {
    
    private final RouteUnit routeUnit;
    
    private final Statement statement;
    
    private final ConnectionMode connectionMode;
    
    private final DatabaseMetaData databaseMetaData;
    
    public StatementExecuteUnit(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        this.routeUnit = routeUnit;
        this.statement = statement;
        this.connectionMode = connectionMode;
        this.databaseMetaData = statement.getConnection().getMetaData();
    }
}
