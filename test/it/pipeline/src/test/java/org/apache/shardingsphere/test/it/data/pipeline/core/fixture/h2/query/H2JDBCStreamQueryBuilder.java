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

package org.apache.shardingsphere.test.it.data.pipeline.core.fixture.h2.query;

import org.apache.shardingsphere.data.pipeline.core.query.DialectJDBCStreamQueryBuilder;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC stream query builder for H2.
 */
public final class H2JDBCStreamQueryBuilder implements DialectJDBCStreamQueryBuilder {
    
    @Override
    public PreparedStatement build(final DatabaseType databaseType, final Connection connection, final String sql) throws SQLException {
        return connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
