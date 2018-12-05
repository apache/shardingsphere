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

package io.shardingsphere.core.parsing.antlr.extractor.statement.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.SQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLSegmentsExtractor;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Abstract SQL segments extractor.
 *
 * @author duhongjun
 */
public abstract class AbstractSQLSegmentsExtractor implements SQLSegmentsExtractor {
    
    private final Collection<SQLSegmentExtractor> sqlSegmentExtractors = new LinkedList<>();
    
    protected final void addSQLSegmentExtractor(final SQLSegmentExtractor sqlSegmentExtractor) {
        sqlSegmentExtractors.add(sqlSegmentExtractor);
    }
    
    @Override
    public final Collection<SQLSegment> extract(final ParserRuleContext rootNode, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        Collection<SQLSegment> result = new LinkedList<>();
        for (SQLSegmentExtractor each : sqlSegmentExtractors) {
            if (each instanceof OptionalSQLSegmentExtractor) {
                Optional<? extends SQLSegment> sqlSegment = ((OptionalSQLSegmentExtractor) each).extract(rootNode);
                if (sqlSegment.isPresent()) {
                    result.add(sqlSegment.get());
                }
            }
            if (each instanceof CollectionSQLSegmentExtractor) {
                result.addAll(((CollectionSQLSegmentExtractor) each).extract(rootNode));
            }
        }
        return result;
    }
    
    @Override
    public void postExtract(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
    }
}
