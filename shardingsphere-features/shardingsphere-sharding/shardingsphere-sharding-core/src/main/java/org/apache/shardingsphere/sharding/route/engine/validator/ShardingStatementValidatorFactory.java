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
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterIndexStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterTableStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterViewStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateFunctionStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateIndexStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateProcedureStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateTableStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateViewStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingDropIndexStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingDropTableStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingPrepareStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingRenameTableStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingDeleteStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingInsertStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingSelectStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingUpdateStatementValidator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
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
     * @param shardingConditions sharding conditions
     * @return instance of sharding statement validator
     */
    public static Optional<ShardingStatementValidator> newInstance(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        if (sqlStatement instanceof DDLStatement) {
            return getDDLStatementValidator(sqlStatement);
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLStatementValidator(sqlStatement, shardingConditions);
        }
        return Optional.empty();
    }
    
    private static Optional<ShardingStatementValidator> getDDLStatementValidator(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            return Optional.of(new ShardingCreateTableStatementValidator());
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return Optional.of(new ShardingCreateFunctionStatementValidator());
        }
        if (sqlStatement instanceof CreateProcedureStatement) {
            return Optional.of(new ShardingCreateProcedureStatementValidator());
        }
        if (sqlStatement instanceof CreateViewStatement) {
            return Optional.of(new ShardingCreateViewStatementValidator());
        }
        if (sqlStatement instanceof CreateIndexStatement) {
            return Optional.of(new ShardingCreateIndexStatementValidator());
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return Optional.of(new ShardingAlterTableStatementValidator());
        }
        if (sqlStatement instanceof RenameTableStatement) {
            return Optional.of(new ShardingRenameTableStatementValidator());
        }
        if (sqlStatement instanceof AlterViewStatement) {
            return Optional.of(new ShardingAlterViewStatementValidator());
        }
        if (sqlStatement instanceof AlterIndexStatement) {
            return Optional.of(new ShardingAlterIndexStatementValidator());
        }
        if (sqlStatement instanceof DropTableStatement) {
            return Optional.of(new ShardingDropTableStatementValidator());
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return Optional.of(new ShardingDropIndexStatementValidator());
        }
        if (sqlStatement instanceof PrepareStatement) {
            return Optional.of(new ShardingPrepareStatementValidator());
        }
        return Optional.empty();
    }
    
    private static Optional<ShardingStatementValidator> getDMLStatementValidator(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        if (sqlStatement instanceof InsertStatement) {
            return Optional.of(new ShardingInsertStatementValidator(shardingConditions));
        }
        if (sqlStatement instanceof UpdateStatement) {
            return Optional.of(new ShardingUpdateStatementValidator());
        }
        if (sqlStatement instanceof DeleteStatement) {
            return Optional.of(new ShardingDeleteStatementValidator());
        }
        if (sqlStatement instanceof SelectStatement) {
            return Optional.of(new ShardingSelectStatementValidator());
        }
        return Optional.empty();
    }
}
