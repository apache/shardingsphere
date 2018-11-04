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
import io.shardingsphere.core.parsing.antler.util.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.util.TreeUtils;
import io.shardingsphere.core.parsing.antler.util.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * Visit MySQL add index phrase.
 * 
 * @author duhongjun
 */
public final class MySQLAddIndexVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        List<ParserRuleContext> addIndexContexts = TreeUtils.getAllDescendantByRuleName(ancestorNode, RuleNameConstants.ADD_INDEX);
        if (null == addIndexContexts) {
            return;
        }
        for (ParserRuleContext each : addIndexContexts) {
            Optional<ParserRuleContext> indexNameNode = TreeUtils.findFirstChildByRuleName(each, RuleNameConstants.INDEX_NAME);
            if (indexNameNode.isPresent()) {
                statement.getSQLTokens().add(VisitorUtils.visitIndex(indexNameNode.get(), statement.getTables().getSingleTableName()));
            }
        }
    }
}
