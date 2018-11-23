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

package io.shardingsphere.core.parsing.antlr.extractor.sql.statement;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.SQLStatementExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.SQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.filler.HandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.filler.HandlerResultFillerRegistry;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract SQL statement extractor.
 *
 * @author duhongjun
 */
public abstract class AbstractSQLStatementExtractor implements SQLStatementExtractor {
    
    private final Collection<SQLSegmentExtractor> sqlSegmentExtractors = new LinkedList<>();
    
    @Override
    public final SQLStatement extract(final ParserRuleContext rootNode, final ShardingTableMetaData shardingTableMetaData) {
        SQLStatement result = createStatement();
        List<Object> extractResults = new LinkedList<>();
        for (SQLSegmentExtractor each : sqlSegmentExtractors) {
            if (each instanceof OptionalSQLSegmentExtractor) {
                Optional<?> extractResult = ((OptionalSQLSegmentExtractor) each).extract(rootNode);
                if (extractResult.isPresent()) {
                    extractResults.add(((Optional) extractResult).get());
                }
            }
            if (each instanceof CollectionSQLSegmentExtractor) {
                Collection<?> extractResult = ((CollectionSQLSegmentExtractor) each).extract(rootNode);
                if (!extractResult.isEmpty()) {
                    extractResults.add(extractResult);
                }
            }
        }
        for (Object each : extractResults) {
            HandlerResultFiller filler = HandlerResultFillerRegistry.getFiller(each);
            if (null != filler) {
                filler.fill(each, result, shardingTableMetaData);
            }
        }
        postExtract(result, shardingTableMetaData);
        return result;
    }
    
    protected final void addSQLSegmentExtractor(final SQLSegmentExtractor extractor) {
        sqlSegmentExtractors.add(extractor);
    }
    
    protected abstract SQLStatement createStatement();
    
    protected void postExtract(final SQLStatement statement, final ShardingTableMetaData shardingTableMetaData) {
    }
}
