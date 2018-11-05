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
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.util.RuleNameConstants;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Visit rename table phrase.
 * 
 * @author duhongjun
 */
public final class RenameTableVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        Optional<ParserRuleContext> renameTableNode = ASTUtils.findFirstChildByRuleName(ancestorNode, RuleNameConstants.RENAME_TABLE);
        if (renameTableNode.isPresent() && 0 < renameTableNode.get().getChildCount()) {
            alterStatement.setNewTableName(renameTableNode.get().getChild(renameTableNode.get().getChildCount() - 1).getText());
        }
    }
}
