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

package io.shardingsphere.core.parsing.antlr.extractor.statement.type.dialect.mysql;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ConditionExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.FromClauseExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.GroupByExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.LimitClauseExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.OrderByExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.SelectExprExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.TableNameExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.type.AbstractSQLStatementExtractor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;

/**
 * MySQL select extractor.
 * 
 * @author duhongjun
 */
public class MySQLSelectExtractor extends AbstractSQLStatementExtractor {
    
    public MySQLSelectExtractor() {
        addExtractHandler(new TableNameExtractHandler());
        addExtractHandler(new SelectExprExtractHandler());
        addExtractHandler(new FromClauseExtractHandler());
        addExtractHandler(new GroupByExtractHandler());
        addExtractHandler(new OrderByExtractHandler());
        addExtractHandler(new LimitClauseExtractHandler());
    }
    
    @Override
    protected SQLStatement createStatement() {
        return new SelectStatement();
    }
}
