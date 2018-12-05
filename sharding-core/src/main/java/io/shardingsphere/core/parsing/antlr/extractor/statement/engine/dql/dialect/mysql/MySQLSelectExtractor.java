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

package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dql.dialect.mysql;

import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.FromWhereExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.GroupByExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.IndexNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.LimitExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.OrderByExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.SelectClauseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.TableNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.AbstractSQLSegmentsExtractor;

/**
 * Select extractor for MySQL.
 *
 * @author duhongjun
 */
public final class MySQLSelectExtractor extends AbstractSQLSegmentsExtractor {
    
    public MySQLSelectExtractor() {
        addSQLSegmentExtractor(new TableNamesExtractor());
        addSQLSegmentExtractor(new IndexNamesExtractor());
        addSQLSegmentExtractor(new SelectClauseExtractor());
        addSQLSegmentExtractor(new FromWhereExtractor());
        addSQLSegmentExtractor(new GroupByExtractor());
        addSQLSegmentExtractor(new OrderByExtractor());
        addSQLSegmentExtractor(new LimitExtractor());
    }
}
