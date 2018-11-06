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

package io.shardingsphere.core.parsing.antlr.ast;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.ast.dialect.MySQLShardingASTBuilder;
import io.shardingsphere.core.parsing.antlr.ast.dialect.OracleShardingASTBuilder;
import io.shardingsphere.core.parsing.antlr.ast.dialect.PostgreSQLShardingASTBuilder;
import io.shardingsphere.core.parsing.antlr.ast.dialect.SQLServerShardingASTBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding AST builder factory.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingASTBuilderFactory {
    
    /** 
     * New instance of {@code ShardingASTBuilder}.
     * 
     * @param dbType database type
     * @return instance of {@code ShardingASTBuilder}
     */
    public static ShardingASTBuilder newInstance(final DatabaseType dbType) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLShardingASTBuilder();
            case PostgreSQL:
                return new PostgreSQLShardingASTBuilder();
            case SQLServer:
                return new SQLServerShardingASTBuilder();
            case Oracle:
                return new OracleShardingASTBuilder();
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", dbType.name()));
        }
    }
}
