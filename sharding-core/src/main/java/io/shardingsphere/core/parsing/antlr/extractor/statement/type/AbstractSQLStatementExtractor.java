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

package io.shardingsphere.core.parsing.antlr.extractor.statement.type;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.SQLStatementExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.filler.HandlerResultFiller;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.filler.HandlerResultFillerRegistry;
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
    
    private final Collection<ASTExtractHandler<?>> extractHandlers = new LinkedList<>();
    
    @Override
    public final SQLStatement extract(final ParserRuleContext rootNode, final ShardingTableMetaData shardingTableMetaData) {
        SQLStatement result = createStatement();
        List<Object> extractResults = new LinkedList<>();
        for (ASTExtractHandler each : extractHandlers) {
            Object extractResult = each.extract(rootNode);
            if (extractResult instanceof Optional) {
                if (((Optional) extractResult).isPresent()) {
                    extractResults.add(((Optional) extractResult).get());
                }
            } else if (extractResult instanceof Collection) {
                if (!((Collection) extractResult).isEmpty()) {
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
    
    protected abstract SQLStatement createStatement();
    
    protected void postExtract(final SQLStatement statement, final ShardingTableMetaData shardingTableMetaData) {
    }
    
    protected final void addExtractHandler(final ASTExtractHandler handler) {
        extractHandlers.add(handler);
    }
}
