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

package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLRewriteEngineTest {
    
    private ShardingRule shardingRule;
    
    private Map<String, String> tableTokens;
    
    @Before
    public void setUp() {
        shardingRule = new ShardingRuleMockBuilder().addShardingColumns("sharding_value").addGenerateKeyColumn("table_x", "id").build();
        tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_1");
    }
    
    @Test
    public void assertRewriteWithoutChange() {
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT table_y.id FROM table_y WHERE table_y.id=?", new SelectStatement());
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
}
