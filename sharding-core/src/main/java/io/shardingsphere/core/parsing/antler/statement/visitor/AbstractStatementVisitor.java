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

package io.shardingsphere.core.parsing.antler.statement.visitor;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antler.phrase.visitor.PhraseVisitor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public abstract class AbstractStatementVisitor implements StatementVisitor {
    private List<PhraseVisitor> visitors = new ArrayList<>();

    /** Visit ast, generate statement.
     * @param rootNode root node of ast
     * @param shardingTableMetaData table metadata
     * @return sql statement
     */
    @Override
    public SQLStatement visit(final ParserRuleContext rootNode, final ShardingTableMetaData shardingTableMetaData) {
        SQLStatement statement = newStatement(shardingTableMetaData);

        for (PhraseVisitor each : visitors) {
            each.visit(rootNode, statement);
        }
        
        postVisit(statement);
        return statement;
    }
    
    /** process after visit.
     * @param statement sql statement
     */
    protected void postVisit(final SQLStatement statement) {

    }

    /** Add visitor.
     * @param visitor phrase visitor for filling statement
     */
    public void addVisitor(final PhraseVisitor visitor) {
        visitors.add(visitor);
    }

    /** Create statement.
     * @return empty sql statment
     */
    protected abstract SQLStatement newStatement();
    
    
    /** Use shardingTableMetaData create SQLStatement.
     * @param shardingTableMetaData table metadata
     * @return sql statement info
     */
    protected SQLStatement newStatement(final ShardingTableMetaData shardingTableMetaData) {
        return newStatement();
    }
}
