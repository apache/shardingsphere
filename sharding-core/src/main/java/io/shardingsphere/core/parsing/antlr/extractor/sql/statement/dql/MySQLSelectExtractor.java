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

package io.shardingsphere.core.parsing.antlr.extractor.sql.statement.dql;

import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.FromWhereExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.GroupByClauseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.LimitClauseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.OrderByClauseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.SelectExpressionExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.TableNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.AbstractSQLStatementExtractor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;

/**
 * MySQL select extractor.
 * 
 * @author duhongjun
 */
public class MySQLSelectExtractor extends AbstractSQLStatementExtractor {
    
    public MySQLSelectExtractor() {
        addSQLSegmentExtractor(new TableNamesExtractor());
        addSQLSegmentExtractor(new SelectExpressionExtractor());
        addSQLSegmentExtractor(new FromWhereExtractor());
        addSQLSegmentExtractor(new GroupByClauseExtractor());
        addSQLSegmentExtractor(new OrderByClauseExtractor());
        addSQLSegmentExtractor(new LimitClauseExtractor());
    }
    
    @Override
    protected SQLStatement createStatement() {
        return new SelectStatement();
    }
}
