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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.dialect.sqlserver;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Primary key extract handler for SQLServer.
 * 
 * @author duhongjun
 */
public final class SQLServerAddPrimaryKeyExtractHandler implements ASTExtractHandler {
    
    @Override
    public void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        Optional<ParserRuleContext> addColumnNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.ADD_COLUMN);
        if (!addColumnNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> tableConstraintNode = ASTUtils.findFirstChildNode(addColumnNode.get(), RuleName.TABLE_CONSTRAINT);
        if (!tableConstraintNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildNode(tableConstraintNode.get(), RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return;
        }
        for (ParseTree each : ASTUtils.getAllDescendantNodes(tableConstraintNode.get(), RuleName.COLUMN_NAME)) {
            String columnName = each.getText();
            Optional<ColumnDefinition> updateColumn = alterStatement.getColumnDefinitionByName(columnName);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                alterStatement.getUpdateColumns().put(columnName, updateColumn.get());
            } else {
                alterStatement.getUpdateColumns().put(columnName, new ColumnDefinition(each.getText(), null, null, true));
            }
        }
    }
}
