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

import io.shardingsphere.core.parsing.antler.util.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.util.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * Visit create table primary key  phrase.
 * 
 * @author duhongjun
 */
public final class CreatePrimaryKeyVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        CreateTableStatement createStatement = (CreateTableStatement) statement;
        ParserRuleContext primaryKeyContext = TreeUtils.getFirstChildByRuleName(ancestorNode, RuleNameConstants.PRIMARY_KEY);
        if (null == primaryKeyContext) {
            return;
        }
        ParserRuleContext columnListContext = TreeUtils.getFirstChildByRuleName(primaryKeyContext.getParent().getParent(), RuleNameConstants.COLUMN_LIST);
        if (null == columnListContext) {
            return;
        }
        List<ParserRuleContext> columnNodes = TreeUtils.getAllDescendantByRuleName(columnListContext, RuleNameConstants.COLUMN_NAME);
        if (null == columnNodes) {
            return;
        }
        for (ParserRuleContext each : columnNodes) {
            if (!createStatement.getPrimaryKeyColumns().contains(each.getText())) {
                createStatement.getPrimaryKeyColumns().add(each.getText());
            }
        }
    }
}
