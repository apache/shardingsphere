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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler;

import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Add column extract handler.
 * 
 * @author duhongjun
 */
public class AddColumnExtractHandler implements ASTExtractHandler {
    
    @Override
    public final void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        for (ParserRuleContext each : ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.ADD_COLUMN)) {
            extractAddColumn(each, (AlterTableStatement) statement);
        }
    }
    
    private void extractAddColumn(final ParserRuleContext addColumnNode, final AlterTableStatement alterStatement) {
        for (ParserRuleContext each : ASTUtils.getAllDescendantNodes(addColumnNode, RuleName.COLUMN_DEFINITION)) {
            ColumnDefinition column = ExtractorUtils.extractColumnDefinition(each);
            if (!alterStatement.findColumnDefinition(column.getName()).isPresent()) {
                alterStatement.getAddColumns().add(column);
                postVisitColumnDefinition(addColumnNode, alterStatement, column.getName());
            }
        }
    }
    
    protected void postVisitColumnDefinition(final ParseTree ancestorNode, final SQLStatement statement, final String columnName) {
    }
}
