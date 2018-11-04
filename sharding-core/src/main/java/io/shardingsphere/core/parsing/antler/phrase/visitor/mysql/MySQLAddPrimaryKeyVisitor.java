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

package io.shardingsphere.core.parsing.antler.phrase.visitor.mysql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antler.phrase.visitor.PhraseVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.util.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.util.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

/**
 * Visit MySQL add primary key phrase.
 * 
 * @author duhongjun
 */
public final class MySQLAddPrimaryKeyVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        Optional<ParserRuleContext> constraintDefinitionNode = TreeUtils.findFirstChildByRuleName(ancestorNode, RuleNameConstants.ADD_CONSTRAINT);
        if (!constraintDefinitionNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> primaryKeyOptionNode = TreeUtils.findFirstChildByRuleName(ancestorNode, RuleNameConstants.PRIMARY_KEY_OPTION);
        if (!primaryKeyOptionNode.isPresent()) {
            return;
        }
        List<ParserRuleContext> keyPartNodes = TreeUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.KEY_PART);
        if (null == keyPartNodes) {
            return;
        }
        for (ParseTree each : keyPartNodes) {
            String columnName = each.getChild(0).getText();
            ColumnDefinition updateColumn = alterStatement.getColumnDefinitionByName(columnName);
            if (null != updateColumn) {
                updateColumn.setPrimaryKey(true);
                alterStatement.getUpdateColumns().put(columnName, updateColumn);
            }
        }
    }
}
