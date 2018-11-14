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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.dialect.oracle;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ASTExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.DropPrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;

/**
 * Drop primary key extract handler for Oracle.
 *
 * @author duhongjun
 */
public final class OracleDropPrimaryKeyExtractHandler implements ASTExtractHandler {

    @Override
    public ExtractResult extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> dropConstraintNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.DROP_CONSTRAINT_CLAUSE);
        if (!dropConstraintNode.isPresent()) {
            return null;
        }
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildNode(dropConstraintNode.get(), RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return null;
        }
        return new DropPrimaryKeyExtractResult(true);
    }
}
