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

package org.apache.shardingsphere.core.rewrite.token;

import org.apache.shardingsphere.core.rewrite.token.generator.AggregationDistinctTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.IndexTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.InsertGeneratedKeyNameTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.InsertSetGeneratedKeyColumnTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.OffsetTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.OrderByTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.RowCountTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.SelectItemPrefixTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.SelectItemsTokenGenerator;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator for sharding.
 *
 * @author zhangliang
 */
public final class ShardingTokenGenerateEngine extends SQLTokenGenerateEngine<ShardingRule> {
    
    private static final Collection<SQLTokenGenerator> SQL_TOKEN_GENERATORS = new LinkedList<>();
    
    static {
        SQL_TOKEN_GENERATORS.add(new SelectItemPrefixTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new SelectItemsTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new OrderByTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new AggregationDistinctTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new IndexTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new OffsetTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new RowCountTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new InsertGeneratedKeyNameTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new InsertSetGeneratedKeyColumnTokenGenerator());
    }
    
    @Override
    protected Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        return SQL_TOKEN_GENERATORS;
    }
}
