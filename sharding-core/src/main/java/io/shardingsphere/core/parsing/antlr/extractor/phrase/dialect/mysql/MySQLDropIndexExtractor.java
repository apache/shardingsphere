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

package io.shardingsphere.core.parsing.antlr.extractor.phrase.dialect.mysql;

import io.shardingsphere.core.parsing.antlr.extractor.phrase.PhraseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.phrase.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Extract MySQL drop index phrase.
 * 
 * @author duhongjun
 */
public final class MySQLDropIndexExtractor implements PhraseExtractor {
    
    @Override
    public void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        for (ParserRuleContext each : ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.DROP_INDEX_REF)) {
            int childCnt = each.getChildCount();
            if (0 == childCnt) {
                continue;
            }
            ParseTree lastChild = each.getChild(childCnt - 1);
            if (!(lastChild instanceof ParserRuleContext)) {
                continue;
            }
            ParserRuleContext indexNameNode = (ParserRuleContext) lastChild;
            statement.getSQLTokens().add(ExtractorUtils.extractIndex(indexNameNode, statement.getTables().getSingleTableName()));
        }
    }
}
