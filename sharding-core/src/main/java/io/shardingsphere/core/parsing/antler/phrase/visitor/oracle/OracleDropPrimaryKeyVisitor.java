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

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.phrase.visitor.PhraseVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class OracleDropPrimaryKeyVisitor implements PhraseVisitor {

    /** Visit drop primary key node.
     * @param rootNode ancestor node of ast
     * @param statement sql statement
     */
    @Override
    public void visit(final ParserRuleContext rootNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;

        ParserRuleContext dropConstraintCtx = TreeUtils.getFirstChildByRuleName(rootNode, RuleNameConstants.DROP_CONSTRAINT_CLAUSE);
        if (null != dropConstraintCtx) {
            ParserRuleContext primaryKeyCtx = TreeUtils.getFirstChildByRuleName(dropConstraintCtx, RuleNameConstants.PRIMARY_KEY);
            if (null != primaryKeyCtx) {
                alterStatement.setDropPrimaryKey(true);
            }
        }
    }

}
