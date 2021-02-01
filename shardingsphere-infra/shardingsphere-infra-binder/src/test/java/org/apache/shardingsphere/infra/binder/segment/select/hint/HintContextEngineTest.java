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

package org.apache.shardingsphere.infra.binder.segment.select.hint;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.DataBaseHintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.HintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.ShardingHintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.ShardingHintValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Assert;
import org.junit.Test;

/**
 * HintContextEngineTest.
 */
public final class HintContextEngineTest {

    @Test
    public void testCreateHintContextForCommonSql() {
        HintContext hintContext = new HintContextEngine().createHintContext(new MySQLSelectStatement());
        Assert.assertEquals(HintType.NONE, hintContext.getHintType());
    }

    @Test
    public void testCreateHintContextForDataBaseHint() {
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        mySQLSelectStatement.setHint(new HintSegment(0, 52, new DataBaseHintSegment(3, 50, new IdentifierValue("ds_0"))));
        HintContext hintContext = new HintContextEngine().createHintContext(mySQLSelectStatement);
        Assert.assertEquals(HintType.DATABASE_HINT, hintContext.getHintType());
    }

    @Test
    public void testCreateHintContextForShardingValueHint() {
        ShardingHintValueSegment shardingDatabaseHintValueSegment = new ShardingHintValueSegment(10, 50);
        shardingDatabaseHintValueSegment.getValues().add(new LiteralExpressionSegment(20, 40, 1));
        ShardingHintValueSegment shardingTableHintValueSegment = new ShardingHintValueSegment(50, 90);
        shardingTableHintValueSegment.getValues().add(new LiteralExpressionSegment(60, 80, 1));
        ShardingHintSegment shardingHintSegment = new ShardingHintSegment(10, 90);
        shardingHintSegment.setShardingDatabaseHintValueSegment(shardingDatabaseHintValueSegment);
        shardingHintSegment.setShardingTableHintValueSegment(shardingTableHintValueSegment);
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        mySQLSelectStatement.setHint(new HintSegment(0, 100, shardingHintSegment));
        HintContext hintContext = new HintContextEngine().createHintContext(mySQLSelectStatement);
        Assert.assertEquals(HintType.SHARDING_HINT, hintContext.getHintType());
    }
}
