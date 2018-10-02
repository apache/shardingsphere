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
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddPrimaryKeyVisitor implements PhraseVisitor {
    private final String ruleName;

    /** Visit add primary key node.
     * @param ancestorNode ancestor node of ast
     * @param statement sql statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        
        ParserRuleContext modifyColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode, ruleName);
        if (null == modifyColumnCtx) {
            return;
        }

        ParseTree primaryKeyCtx = TreeUtils.getFirstChildByRuleName(modifyColumnCtx, "primaryKey");
        if (null == primaryKeyCtx) {
            return;
        }

        List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(modifyColumnCtx, "columnName");
        if (null == columnNodes) {
            return;
        }
        for (final ParseTree each : columnNodes) {
            String columnName = each.getText();
            ColumnDefinition updateColumn = alterStatement.getUpdateColumns().get(columnName);
            if (null == updateColumn) {
                updateColumn = new ColumnDefinition(columnName, null, null, true);
                alterStatement.getUpdateColumns().put(columnName, updateColumn);
            } else {
                updateColumn.setPrimaryKey(true);
            }
            alterStatement.getUpdateColumns().put(columnName, new ColumnDefinition(columnName, null, null, true));
        }
    }

}
