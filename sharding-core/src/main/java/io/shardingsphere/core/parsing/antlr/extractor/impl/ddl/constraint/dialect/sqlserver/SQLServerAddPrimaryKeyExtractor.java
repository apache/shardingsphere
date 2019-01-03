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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.constraint.dialect.sqlserver;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.constraint.ConstraintDefinitionSegment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;

/**
 * Add primary key extractor for SQLServer.
 *
 * @author duhongjun
 */
public final class SQLServerAddPrimaryKeyExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ConstraintDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> addColumnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ADD_COLUMN);
        if (!addColumnNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> tableConstraintNode = ExtractorUtils.findFirstChildNode(addColumnNode.get(), RuleName.TABLE_CONSTRAINT);
        if (!tableConstraintNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> primaryKeyNode = ExtractorUtils.findFirstChildNode(tableConstraintNode.get(), RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> columnNameNodes = ExtractorUtils.getAllDescendantNodes(tableConstraintNode.get(), RuleName.COLUMN_NAME);
        if (columnNameNodes.isEmpty()) {
            return Optional.absent();
        }
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment();
        for (ParseTree each : columnNameNodes) {
            result.getPrimaryKeyColumnNames().add(each.getText());
        }
        return Optional.of(result);
    }
}
