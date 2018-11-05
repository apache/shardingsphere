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

package io.shardingsphere.core.parsing.antlr.visitor.phrase.dialect.mysql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.antlr.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.util.RuleNameConstants;
import io.shardingsphere.core.parsing.antlr.util.VisitorUtils;
import io.shardingsphere.core.parsing.antlr.visitor.phrase.PhraseVisitor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Visit MySQL change column phrase.
 * 
 * @author duhongjun
 */
public final class MySQLChangeColumnVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        MySQLAlterTableStatement alterStatement = (MySQLAlterTableStatement) statement;
        Optional<ParserRuleContext> changeColumnContext = ASTUtils.findFirstChildByRuleName(ancestorNode, RuleNameConstants.CHANGE_COLUMN);
        if (!changeColumnContext.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> oldColumnContext = ASTUtils.findFirstChildByRuleName(changeColumnContext.get(), RuleNameConstants.COLUMN_NAME);
        if (!oldColumnContext.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> columnDefinitionContext = ASTUtils.findFirstChildByRuleName(changeColumnContext.get(), RuleNameConstants.COLUMN_DEFINITION);
        if (!columnDefinitionContext.isPresent()) {
            return;
        }
        Optional<ColumnDefinition> column = VisitorUtils.visitColumnDefinition(columnDefinitionContext.get());
        if (column.isPresent()) {
            alterStatement.getUpdateColumns().put(oldColumnContext.get().getText(), column.get());
            Optional<ColumnPosition> columnPosition = VisitorUtils.visitFirstOrAfterColumn(changeColumnContext.get(), column.get().getName());
            if (columnPosition.isPresent()) {
                alterStatement.getPositionChangedColumns().add(columnPosition.get());
            }
        }
    }
}
