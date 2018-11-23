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

package io.shardingsphere.core.parsing.antlr.extractor.sql.segment;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.TableExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.util.ASTUtils;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *  Table name extractor.
 *
 * @author duhongjun
 */
public final class TableNameExtractor implements SQLSegmentExtractor<Optional<TableExtractResult>> {
    
    @Override
    public Optional<TableExtractResult> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> tableNameNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_NAME);
        if (!tableNameNode.isPresent()) {
            return Optional.absent();
        }
        String tableText = tableNameNode.get().getText();
        int dotPosition = tableText.contains(Symbol.DOT.getLiterals()) ? tableText.lastIndexOf(Symbol.DOT.getLiterals()) : 0;
        String tableName = tableText;
        Optional<String> schemaName;
        if (0 < dotPosition) {
            tableName = tableText.substring(dotPosition + 1);
            String schemaText = tableText.substring(0, dotPosition);
            dotPosition = schemaText.contains(Symbol.DOT.getLiterals()) ? schemaText.lastIndexOf(Symbol.DOT.getLiterals()) : 0;
            schemaName = Optional.of(tableText.substring(dotPosition + 1));
        } else {
            schemaName = Optional.absent();
        }
        Optional<ParserRuleContext> aliasNode = ASTUtils.findFirstChildNode(tableNameNode.get(), RuleName.ALIAS);
        Optional<String> alias;
        if (aliasNode.isPresent()) {
            alias = Optional.of(aliasNode.get().getText());
        } else {
            alias = Optional.absent();
        }
        TableToken tableToken = new TableToken(tableNameNode.get().getStart().getStartIndex(), dotPosition, tableName);
        return Optional.of(new TableExtractResult(SQLUtil.getExactlyValue(tableName), alias, schemaName, tableToken));
    }
}
