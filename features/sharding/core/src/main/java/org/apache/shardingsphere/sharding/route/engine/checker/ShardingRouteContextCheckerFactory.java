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

package org.apache.shardingsphere.sharding.route.engine.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingAlterTableRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingCreateTableRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingCreateViewRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingDropIndexRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingDropTableRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingPrepareRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.ddl.ShardingRenameTableRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.dml.ShardingDeleteRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.dml.ShardingInsertRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.dml.ShardingSelectRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.dml.ShardingUpdateRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Optional;

/**
 * Sharding route context checker factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteContextCheckerFactory {
    
    /**
     * New instance of sharding route context checker.
     *
     * @param sqlStatement SQL statement
     * @param shardingConditions sharding conditions
     * @return created instance
     */
    public static Optional<ShardingRouteContextChecker> newInstance(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRouteContextChecker(sqlStatement);
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLRouteContextChecker(sqlStatement, shardingConditions);
        }
        return Optional.empty();
    }
    
    private static Optional<ShardingRouteContextChecker> getDDLRouteContextChecker(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            return Optional.of(new ShardingCreateTableRouteContextChecker());
        }
        if (sqlStatement instanceof CreateViewStatement) {
            return Optional.of(new ShardingCreateViewRouteContextChecker());
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return Optional.of(new ShardingAlterTableRouteContextChecker());
        }
        if (sqlStatement instanceof RenameTableStatement) {
            return Optional.of(new ShardingRenameTableRouteContextChecker());
        }
        if (sqlStatement instanceof DropTableStatement) {
            return Optional.of(new ShardingDropTableRouteContextChecker());
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return Optional.of(new ShardingDropIndexRouteContextChecker());
        }
        if (sqlStatement instanceof PrepareStatement) {
            return Optional.of(new ShardingPrepareRouteContextChecker());
        }
        return Optional.empty();
    }
    
    private static Optional<ShardingRouteContextChecker> getDMLRouteContextChecker(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        if (sqlStatement instanceof InsertStatement) {
            return Optional.of(new ShardingInsertRouteContextChecker(shardingConditions));
        }
        if (sqlStatement instanceof UpdateStatement) {
            return Optional.of(new ShardingUpdateRouteContextChecker());
        }
        if (sqlStatement instanceof DeleteStatement) {
            return Optional.of(new ShardingDeleteRouteContextChecker());
        }
        if (sqlStatement instanceof SelectStatement) {
            return Optional.of(new ShardingSelectRouteContextChecker());
        }
        return Optional.empty();
    }
}
