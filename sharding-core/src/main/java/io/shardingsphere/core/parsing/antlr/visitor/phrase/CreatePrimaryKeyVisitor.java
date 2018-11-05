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
import io.shardingsphere.core.parsing.antlr.RuleName;
import io.shardingsphere.core.parsing.antlr.util.ASTUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Visit create table primary key  phrase.
 * 
 * @author duhongjun
 */
public final class CreatePrimaryKeyVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        CreateTableStatement createStatement = (CreateTableStatement) statement;
        Optional<ParserRuleContext> primaryKeyContext = ASTUtils.findFirstChildNode(ancestorNode, RuleName.PRIMARY_KEY);
        if (!primaryKeyContext.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> columnListContext = ASTUtils.findFirstChildNode(primaryKeyContext.get().getParent().getParent(), RuleName.COLUMN_LIST);
        if (!columnListContext.isPresent()) {
            return;
        }
        for (ParserRuleContext each : ASTUtils.getAllDescendantNodes(columnListContext.get(), RuleName.COLUMN_NAME)) {
            if (!createStatement.getPrimaryKeyColumns().contains(each.getText())) {
                createStatement.getPrimaryKeyColumns().add(each.getText());
            }
        }
    }
}
