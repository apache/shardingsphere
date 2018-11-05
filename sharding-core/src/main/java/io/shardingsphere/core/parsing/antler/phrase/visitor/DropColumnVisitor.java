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

import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.util.ASTUtils;
import io.shardingsphere.core.parsing.antler.util.RuleNameConstants;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Visit drop column phrase.
 * 
 * @author duhongjun
 */
public final class DropColumnVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        for (ParserRuleContext each : ASTUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.DROP_COLUMN)) {
            for (ParseTree columnNode : ASTUtils.getAllDescendantByRuleName(each, RuleNameConstants.COLUMN_NAME)) {
                alterStatement.getDropColumns().add(SQLUtil.getExactlyValue(columnNode.getText()));
            }
        }
    }
}
