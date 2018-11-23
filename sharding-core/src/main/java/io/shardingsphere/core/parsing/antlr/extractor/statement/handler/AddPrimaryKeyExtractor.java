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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.PrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;

/**
 * Add primary key clause extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class AddPrimaryKeyExtractor implements SQLClauseExtractor<Optional<PrimaryKeyExtractResult>> {
    
    private final RuleName ruleName;
    
    @Override
    public Optional<PrimaryKeyExtractResult> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> modifyColumnNode = ASTUtils.findFirstChildNode(ancestorNode, ruleName);
        if (!modifyColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildNode(modifyColumnNode.get(), RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> columnNameNodes = ASTUtils.getAllDescendantNodes(modifyColumnNode.get(), RuleName.COLUMN_NAME);
        if (null == columnNameNodes) {
            return Optional.absent();
        }
        PrimaryKeyExtractResult result = new PrimaryKeyExtractResult();
        for (ParserRuleContext each : columnNameNodes) {
            result.getPrimaryKeyColumnNames().add(each.getText());
        }
        return Optional.of(result);
    }
}
