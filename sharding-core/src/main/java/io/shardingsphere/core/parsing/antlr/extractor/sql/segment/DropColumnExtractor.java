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
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.DropColumnExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.util.ASTUtils;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;

/**
 * Drop column clause extractor.
 *
 * @author duhongjun
 */
public final class DropColumnExtractor implements SQLSegmentExtractor<Optional<DropColumnExtractResult>> {
    
    @Override
    public Optional<DropColumnExtractResult> extract(final ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> dropColumnNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.DROP_COLUMN);
        if (dropColumnNodes.isEmpty()) {
            return Optional.absent();
        }
        DropColumnExtractResult result = new DropColumnExtractResult();
        for (ParserRuleContext each : dropColumnNodes) {
            for (ParseTree columnNode : ASTUtils.getAllDescendantNodes(each, RuleName.COLUMN_NAME)) {
                result.getDropColumnNames().add(SQLUtil.getExactlyValue(columnNode.getText()));
            }
        }
        return Optional.of(result);
    }
}
