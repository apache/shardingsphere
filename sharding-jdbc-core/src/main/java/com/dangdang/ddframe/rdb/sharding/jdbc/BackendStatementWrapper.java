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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import lombok.AccessLevel;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Statement对象包装类.
 * 在Statement复用的时候，用Connection对象来区分同一个库的会话
 * 
 * @author gaohongtao
 */
class BackendStatementWrapper {
    
    private final Connection connection;
    
    @Getter(AccessLevel.PACKAGE)
    private final Statement statement;
    
    BackendStatementWrapper(final Statement statement) throws SQLException {
        this.statement = statement;
        this.connection = statement.getConnection();
    }
    
    boolean isBelongTo(final Connection connection, final String sql) {
        return Objects.equals(connection, this.connection);
    }
}
