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

package org.apache.shardingsphere.shardingjdbc.spi;

import org.apache.shardingsphere.core.route.spi.TimeService;
import org.apache.shardingsphere.core.spi.database.MySQLDatabaseType;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContextHolder;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;

/**
 * JDBC MySQL timeService.
 *
 * @author chenchuangliu
 */
public final class JDBCMySQLTimeService implements TimeService {

    @Override
    public Date getTime() {
        Collection<ShardingRuntimeContext> runtimeContextWrapper = RuntimeContextHolder.getInstance().getShardingRuntimeContexts();
        if (runtimeContextWrapper.isEmpty()) {
            return new Date();
        }
        for (ShardingRuntimeContext context : runtimeContextWrapper) {
            if (!(context.getDatabaseType() instanceof MySQLDatabaseType)) {
                continue;
            }
            try (Connection connection = context.getCachedDatabaseMetaData().getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select now()");
                resultSet.next();
                return (Date) resultSet.getObject(1);
            } catch (SQLException ignore) {

            }
        }
        return new Date();
    }
}

