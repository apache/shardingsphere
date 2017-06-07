/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.prepared;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 在PreparedStatement复用的时候，用Connection对象和SQL来区分同一个库的会话.
 * 
 * @author gaohongtao
 */
public final class BackendPreparedStatementWrapper {
    
    private final String sql;
    
    private final Connection connection;
    
    @Getter
    private final PreparedStatement preparedStatement;
    
    public BackendPreparedStatementWrapper(final PreparedStatement preparedStatement, final String sql) throws SQLException {
        this.sql = sql;
        connection = preparedStatement.getConnection();
        this.preparedStatement = preparedStatement;
    }
    
    public boolean isBelongTo(final Connection connection, final String sql) {
        return this.connection == connection && this.sql.equals(sql);
    }
}
