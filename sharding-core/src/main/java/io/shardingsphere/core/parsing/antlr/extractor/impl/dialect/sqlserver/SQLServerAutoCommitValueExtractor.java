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

package io.shardingsphere.core.parsing.antlr.extractor.impl.dialect.sqlserver;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.TransactionOperationTypeSegment;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Auto commit value clause extractor for SQLServer.
 *
 * @author maxiaoguang
 */
public final class SQLServerAutoCommitValueExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<TransactionOperationTypeSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> autoCommitValueNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.AUTO_COMMIT_VALUE);
        Preconditions.checkState(autoCommitValueNode.isPresent(), "Auto commit value is necessary.");
        if (DefaultKeyword.ON.name().equalsIgnoreCase(SQLUtil.getExactlyValue(autoCommitValueNode.get().getText()))) {
            return Optional.of(new TransactionOperationTypeSegment(TransactionOperationType.IGNORE));
        } else {
            return Optional.of(new TransactionOperationTypeSegment(TransactionOperationType.BEGIN));
        }
    }
}
