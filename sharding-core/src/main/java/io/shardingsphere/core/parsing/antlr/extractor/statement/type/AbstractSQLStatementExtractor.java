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

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.SQLStatementExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Abstract SQL statement extractor.
 * 
 * @author duhongjun
 */
public abstract class AbstractSQLStatementExtractor implements SQLStatementExtractor {
    
    private final Collection<ASTExtractHandler> extractHandlers = new LinkedList<>();
    
    @Override
    public final SQLStatement extract(final ParserRuleContext rootNode, final ShardingTableMetaData shardingTableMetaData) {
        SQLStatement result = createStatement(shardingTableMetaData);
        for (ASTExtractHandler each : extractHandlers) {
            each.extract(rootNode, result);
        }
        postExtract(result);
        return result;
    }
    
    protected abstract SQLStatement createStatement(ShardingTableMetaData shardingTableMetaData);
    
    protected void postExtract(final SQLStatement statement) {
    }
    
    protected final void addExtractHandler(final ASTExtractHandler handler) {
        extractHandlers.add(handler);
    }
}
