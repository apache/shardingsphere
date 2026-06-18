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

package org.apache.shardingsphere.parser.rule.builder;

import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.constant.SQLParserOrder;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;

/**
 * Default SQL parser rule configuration builder.
 */
public final class DefaultSQLParserRuleConfigurationBuilder implements DefaultGlobalRuleConfigurationBuilder<SQLParserRuleConfiguration, SQLParserRuleBuilder> {
    
    public static final CacheOption PARSE_TREE_CACHE_OPTION = new CacheOption(128, 1024L);
    
    public static final CacheOption SQL_STATEMENT_CACHE_OPTION = new CacheOption(2000, 65535L);
    
    @Override
    public SQLParserRuleConfiguration build() {
        return new SQLParserRuleConfiguration(PARSE_TREE_CACHE_OPTION, SQL_STATEMENT_CACHE_OPTION);
    }
    
    @Override
    public int getOrder() {
        return SQLParserOrder.ORDER;
    }
    
    @Override
    public Class<SQLParserRuleBuilder> getTypeClass() {
        return SQLParserRuleBuilder.class;
    }
}
