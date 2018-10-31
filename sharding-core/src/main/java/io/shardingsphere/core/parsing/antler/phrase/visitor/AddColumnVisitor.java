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

package io.shardingsphere.core.parsing.antler.phrase.visitor;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.utils.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class AddColumnVisitor extends ColumnDefinitionVisitor {

    /** Visit add column node.
     * @param ancestorNode ancestor node of ast
     * @param statement sql statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        List<ParserRuleContext> addColumnCtxs = TreeUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.ADD_COLUMN);
        if (null == addColumnCtxs) {
            return;
        }

        for (ParserRuleContext each : addColumnCtxs) {
            visitAddColumn(each, alterStatement);
        }
    }

    /**Visit add column context.
     * @param addColumnCtx add column contxt
     * @param alterStatement alter table statement
     */
    public void visitAddColumn(final ParserRuleContext addColumnCtx, final AlterTableStatement alterStatement) {
        List<ParserRuleContext> columnDefinitionCtxs = TreeUtils.getAllDescendantByRuleName(addColumnCtx, RuleNameConstants.COLUMN_DEFINITION);
        if (null == columnDefinitionCtxs) {
            return;
        }

        for (ParserRuleContext columnDefinitionCtx : columnDefinitionCtxs) {
            ColumnDefinition column = VisitorUtils.visitColumnDefinition(columnDefinitionCtx);
            if (null != column) {
                if(null != alterStatement.getExistColumn(column.getName())) {
                    return;
                }
                alterStatement.getAddColumns().add(column);
                postVisitColumnDefinition(addColumnCtx, alterStatement, column.getName());
            }
        }
    }
    
    protected void postVisitColumnDefinition(final ParseTree ancestorNode, final SQLStatement statement,
                                             final String columnName) {
    }
}
