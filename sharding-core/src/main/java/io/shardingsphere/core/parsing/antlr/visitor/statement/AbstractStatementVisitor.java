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

package io.shardingsphere.core.parsing.antlr.visitor.statement;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.visitor.phrase.PhraseVisitor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract statement visitor, get information by each phrase visitor.
 * 
 * @author duhongjun
 */
public abstract class AbstractStatementVisitor implements StatementVisitor {
    
    private List<PhraseVisitor> visitors = new ArrayList<>();
    
    @Override
    public final SQLStatement visit(final ParserRuleContext rootNode, final ShardingTableMetaData shardingTableMetaData) {
        SQLStatement result = newStatement(shardingTableMetaData);
        for (PhraseVisitor each : visitors) {
            each.visit(rootNode, result);
        }
        postVisit(result);
        return result;
    }
    
    protected void postVisit(final SQLStatement statement) {
    }
    
    /**
     * Add visitor.
     * 
     * @param visitor phrase visitor for filling statement
     */
    public final void addVisitor(final PhraseVisitor visitor) {
        visitors.add(visitor);
    }
    
    protected abstract SQLStatement newStatement();
    
    protected SQLStatement newStatement(final ShardingTableMetaData shardingTableMetaData) {
        return newStatement();
    }
}
