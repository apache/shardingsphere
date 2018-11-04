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

package io.shardingsphere.core.parsing.antler.phrase.visitor.oracle;

import io.shardingsphere.core.parsing.antler.phrase.visitor.PhraseVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.util.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.util.TreeUtils;
import io.shardingsphere.core.parsing.antler.util.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * Visit oracle modify column phrase.
 * 
 * @author duhongjun
 */
public final class OracleModifyColumnVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        List<ParserRuleContext> modifyColumnContexts = TreeUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.MODIFY_COLUMN);
        if (null == modifyColumnContexts) {
            return;
        }
        for (ParserRuleContext modifyColumnContext : modifyColumnContexts) {
            List<ParserRuleContext> columnNodes = TreeUtils.getAllDescendantByRuleName(modifyColumnContext, RuleNameConstants.MODIFY_COL_PROPERTIES);
            if (null == columnNodes) {
                return;
            }
            for (final ParserRuleContext each : columnNodes) {
                // it`s not column definition, but can call this method
                ColumnDefinition column = VisitorUtils.visitColumnDefinition(each);
                if (null != column) {
                    alterStatement.getUpdateColumns().put(column.getName(), column);
                }
            }
        }
    }
}
