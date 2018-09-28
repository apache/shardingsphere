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

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.phrase.visitor.ColumnDefinitionVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class OracleModifyColumnVisitor extends ColumnDefinitionVisitor {

    @Override
    public void visit(final ParserRuleContext rootNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        
        ParserRuleContext modifyColumnCtx = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(rootNode,
                "modifyColumn");
        if (null == modifyColumnCtx) {
            return;
        }

        List<ParseTree> columnNodes = TreeUtils.getAllDescendantByRuleName(modifyColumnCtx, "modifyColProperties");
        if (null == columnNodes) {
            return;
        }

        for (final ParseTree each : columnNodes) {
            // it`s not columndefinition, but can call this method
            ColumnDefinition column = parseColumnDefinition(each);
            if (null != column) {
                alterStatement.getUpdateColumns().put(column.getName(), column);
            }
        }
    }

}
