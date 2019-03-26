/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.antlr.extractor.impl;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.table.TableSegment;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.List;

/**
 *  Table name extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class TableNameExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<TableSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> tableNameNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_NAME);
        if (!tableNameNode.isPresent()) {
            return Optional.absent();
        }
        String nodeText = tableNameNode.get().getText();
        String tableName;
        Optional<String> schemaName;
        int skippedSchemaNameLength;
        if (nodeText.contains(Symbol.DOT.getLiterals())) {
            List<String> nodeTextSegments = Splitter.on(Symbol.DOT.getLiterals()).splitToList(nodeText);
            tableName = nodeTextSegments.get(nodeTextSegments.size() - 1);
            schemaName = Optional.of(nodeTextSegments.get(nodeTextSegments.size() - 2));
            skippedSchemaNameLength = nodeText.lastIndexOf(Symbol.DOT.getLiterals()) + 1;
        } else {
            tableName = nodeText;
            schemaName = Optional.absent();
            skippedSchemaNameLength = 0;
        }
        TableToken token = new TableToken(tableNameNode.get().getStart().getStartIndex(), skippedSchemaNameLength, SQLUtil.getExactlyValue(tableName), QuoteCharacter.getQuoteCharacter(tableName));
        TableSegment result = new TableSegment(token, schemaName.orNull());
        setAlias(tableNameNode.get(), result);
        return Optional.of(result);
    }
    
    private void setAlias(final ParserRuleContext tableNameNode, final TableSegment tableSegment) {
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(tableNameNode.getParent(), RuleName.ALIAS);
        if (aliasNode.isPresent()) {
            tableSegment.setAlias(aliasNode.get().getText());
            tableSegment.setAliasStartIndex(aliasNode.get().getStart().getStartIndex());
        }
    }
}
