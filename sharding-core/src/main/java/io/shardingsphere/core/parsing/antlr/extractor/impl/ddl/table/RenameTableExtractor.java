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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.table;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.TableNameExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.table.RenameTableSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Rename table extractor.
 *
 * @author duhongjun
 */
public final class RenameTableExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<RenameTableSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> renameTableNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.RENAME_TABLE);
        if (!renameTableNode.isPresent()) {
            return Optional.absent();
        }
        Optional<TableSegment> tableSegment = new TableNameExtractor().extract(renameTableNode.get());
        if (tableSegment.isPresent()) {
            return Optional.of(new RenameTableSegment(tableSegment.get().getName()));
        }
        return Optional.absent();
    }
}
