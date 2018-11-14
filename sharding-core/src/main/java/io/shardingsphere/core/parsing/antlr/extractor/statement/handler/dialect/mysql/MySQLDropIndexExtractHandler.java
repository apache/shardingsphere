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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.dialect.mysql;

import java.util.Collection;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.SQLTokenExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Drop index extract for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLDropIndexExtractHandler implements ASTExtractHandler {
    
    @Override
    public ExtractResult extract(ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> result = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.DROP_INDEX_REF);
        if(result.isEmpty()) {
            return null;
        }
        SQLTokenExtractResult extractResult = new SQLTokenExtractResult();
        for (ParserRuleContext each : result) {
            int childCnt = each.getChildCount();
            if (0 == childCnt) {
                continue;
            }
            ParseTree lastChild = each.getChild(childCnt - 1);
            if (!(lastChild instanceof ParserRuleContext)) {
                continue;
            }
            ParserRuleContext indexNameNode = (ParserRuleContext) lastChild;
            extractResult.getSqlTokens().add(new IndexToken(indexNameNode.getStop().getStartIndex(), SQLUtil.getNameWithoutSchema(indexNameNode.getText()), null));
         }
        return extractResult;
    }
}
