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

package org.apache.shardingsphere.sharding.route.engine.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.route.engine.validator.impl.ShardingCreateFunctionStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.impl.ShardingCreateProcedureStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.impl.ShardingCreateTableStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.impl.ShardingDeleteStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.impl.ShardingInsertStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.impl.ShardingUpdateStatementValidator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.util.Optional;

/**
 * Sharding statement validator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingStatementValidatorFactory {
    
    /**
     * New instance of sharding statement validator.
     * 
     * @param sqlStatement SQL statement
     * @return instance of sharding statement validator
     */
    public static Optional<ShardingStatementValidator> newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            return Optional.of(new ShardingInsertStatementValidator());
        }
        if (sqlStatement instanceof UpdateStatement) {
            return Optional.of(new ShardingUpdateStatementValidator());
        }
        if (sqlStatement instanceof DeleteStatement) {
            return Optional.of(new ShardingDeleteStatementValidator());
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return Optional.of(new ShardingCreateTableStatementValidator());
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return Optional.of(new ShardingCreateFunctionStatementValidator());
        }
        if (sqlStatement instanceof CreateProcedureStatement) {
            return Optional.of(new ShardingCreateProcedureStatementValidator());
        }
        return Optional.empty();
    }
}
