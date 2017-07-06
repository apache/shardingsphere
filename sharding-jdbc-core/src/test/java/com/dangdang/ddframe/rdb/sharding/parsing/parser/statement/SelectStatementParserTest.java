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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;
import com.dangdang.ddframe.rdb.sharding.routing.router.ParsingSQLRouter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public final class SelectStatementParserTest extends AbstractStatementParserTest {

    @Test
    public void parseCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = " select * from (SELECT"
            + "        t.*"
            + "        FROM `TABLE_XXX` t"
            + "        where t.`field1` is not null and (t.`field1` < ? or t.`field1` >= ?)) d "
            + "        where d.id=1";
        SQLStatement
            sqlStatement = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule).parse();
        System.out.println(sqlStatement);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        calendar.set(Calendar.MONTH, 4);
        List<Object> params = new ArrayList();
        params.add(calendar.getTime());
        calendar.set(Calendar.MONTH, 1);
        params.add(calendar.getTime());
        SQLRouteResult route = new ParsingSQLRouter(new ShardingContext(shardingRule, DatabaseType.MySQL, null))
            .route(sql, params, sqlStatement);
        Set<SQLExecutionUnit> executionUnits = route.getExecutionUnits();
        for (SQLExecutionUnit executionUnit : executionUnits) {
            System.out.println(executionUnit.getSql());
        }
    }

    @Test
    public void parseCondition2() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = " select * from (SELECT"
            + "        t.*"
            + "        FROM `TABLE_XXX` t, `ACCOUNT` acc"
            + "        where t.`field1` is not null and ((t.type in ('1') and t.to_id = acc.id) "
            + "     or (t.type in ('2') and t.from_id = acc.id))) d "
            + "        where d.id=1";
        SQLStatement sqlStatement = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule).parse();
        List<Object> params = new ArrayList();
        System.out.println(sqlStatement);

        SQLRouteResult route = new ParsingSQLRouter(new ShardingContext(shardingRule, DatabaseType.MySQL, null))
            .route(sql, params, sqlStatement);
        Set<SQLExecutionUnit> executionUnits = route.getExecutionUnits();
        for (SQLExecutionUnit executionUnit : executionUnits) {
            System.out.println(executionUnit.getSql());
        }
    }

    @Test
    public void parseJoinCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = " select * from (SELECT"
            + "        TX.*"
            + "        FROM `TABLE_XXX` TX ,  "
            + "      LEFT OUTER JOIN ACCOUNT FROM_ACC ON (FROM_ACC.ID = TX.FROM_ID)"
            + "      LEFT OUTER JOIN ACCOUNT TO_ACC ON (TO_ACC.ID = TX.TO_ID)"
            + "        where (TX.`field1` is not null or TX.`filed1`>'1')) d "
            + "        where d.id=1";
        SQLStatement
            sqlStatement = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule).parse();
        System.out.println(sqlStatement);
        List<Object> params = new ArrayList();
        SQLRouteResult route = new ParsingSQLRouter(new ShardingContext(shardingRule, DatabaseType.MySQL, null))
            .route(sql, params, sqlStatement);
        Set<SQLExecutionUnit> executionUnits = route.getExecutionUnits();
        for (SQLExecutionUnit executionUnit : executionUnits) {
            System.out.println(executionUnit.getSql());
        }
    }

    

}
