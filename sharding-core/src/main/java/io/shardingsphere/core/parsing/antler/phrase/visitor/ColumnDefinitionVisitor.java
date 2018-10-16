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

import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.utils.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

public class ColumnDefinitionVisitor implements PhraseVisitor {

    /** Visit column definition node.
     * @param ancestorNode ancestor node of ast
     * @param statement sql statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        CreateTableStatement createStatement = (CreateTableStatement) statement;

        List<ParserRuleContext> columnDefinitions = TreeUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.COLUMN_DEFINITION);
        if (null == columnDefinitions) {
            return;
        }

        for (final ParserRuleContext each : columnDefinitions) {
            ColumnDefinition column = VisitorUtils.visitColumnDefinition(each);
            if (null == column) {
                continue;
            }

            createStatement.getColumnNames().add(SQLUtil.getExactlyValue(column.getName()));
            createStatement.getColumnTypes().add(column.getType());
            if (column.isPrimaryKey()) {
                createStatement.getPrimaryKeyColumns().add(column.getName());
            }
        }
    }
}
