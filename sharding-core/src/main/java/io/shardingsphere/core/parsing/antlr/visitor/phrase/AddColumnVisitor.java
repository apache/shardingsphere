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

package io.shardingsphere.core.parsing.antlr.visitor.phrase;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.util.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Visit add column phrase.
 * 
 * @author duhongjun
 */
public class AddColumnVisitor extends ColumnDefinitionVisitor {
    
    @Override
    public final void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        for (ParserRuleContext each : ASTUtils.getAllDescendantByRuleName(ancestorNode, RuleName.ADD_COLUMN)) {
            visitAddColumn(each, (AlterTableStatement) statement);
        }
    }
    
    private void visitAddColumn(final ParserRuleContext addColumnContext, final AlterTableStatement alterStatement) {
        for (ParserRuleContext each : ASTUtils.getAllDescendantByRuleName(addColumnContext, RuleName.COLUMN_DEFINITION)) {
            Optional<ColumnDefinition> column = VisitorUtils.visitColumnDefinition(each);
            if (column.isPresent()) {
                if (null != alterStatement.getExistColumn(column.get().getName())) {
                    return;
                }
                alterStatement.getAddColumns().add(column.get());
                postVisitColumnDefinition(addColumnContext, alterStatement, column.get().getName());
            }
        }
    }
    
    protected void postVisitColumnDefinition(final ParseTree ancestorNode, final SQLStatement statement, final String columnName) {
    }
}
