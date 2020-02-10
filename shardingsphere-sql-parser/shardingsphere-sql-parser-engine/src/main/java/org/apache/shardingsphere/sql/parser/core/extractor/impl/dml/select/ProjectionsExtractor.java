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

package org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.sql.parser.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item.ProjectionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.sql.parser.core.extractor.util.RuleName;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

/**
 * Projections extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class ProjectionsExtractor implements OptionalSQLSegmentExtractor {
    
    // TODO recognize database type, only oracle and sqlserver can use row number
    private final Collection<String> rowNumberIdentifiers;
    
    private final ProjectionExtractor projectionExtractor = new ProjectionExtractor();
    
    public ProjectionsExtractor() {
        rowNumberIdentifiers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        rowNumberIdentifiers.add("rownum");
        rowNumberIdentifiers.add("ROW_NUMBER");
    }
    
    @Override
    public Optional<ProjectionsSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        ParserRuleContext projectionNode = ExtractorUtils.getFirstChildNode(findMainQueryNode(ancestorNode), RuleName.PROJECTIONS);
        ProjectionsSegment result = new ProjectionsSegment(projectionNode.getStart().getStartIndex(), projectionNode.getStop().getStopIndex());
        result.setDistinctRow(extractDistinct(ancestorNode));
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(projectionNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            setUnqualifiedShorthandProjectionSegment(unqualifiedShorthandNode.get(), result, parameterMarkerIndexes);
        }
        setProjectionSegment(projectionNode, result, parameterMarkerIndexes);
        return Optional.of(result);
    }
    
    private void setUnqualifiedShorthandProjectionSegment(final ParserRuleContext unqualifiedShorthandNode,
                                                          final ProjectionsSegment projectionsSegment, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<? extends ProjectionSegment> unqualifiedShorthandProjectionSegment = projectionExtractor.extract(unqualifiedShorthandNode, parameterMarkerIndexes);
        if (unqualifiedShorthandProjectionSegment.isPresent()) {
            projectionsSegment.getProjections().add(unqualifiedShorthandProjectionSegment.get());
        }
    }
    
    private void setProjectionSegment(final ParserRuleContext projectionsNode, final ProjectionsSegment projectionsSegment, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(projectionsNode, RuleName.PROJECTION)) {
            Optional<? extends ProjectionSegment> projectionSegment = projectionExtractor.extract(each, parameterMarkerIndexes);
            if (projectionSegment.isPresent()) {
                projectionsSegment.getProjections().add(projectionSegment.get());
            }
        }
    }
    
    private boolean extractDistinct(final ParserRuleContext projectionsNode) {
        Optional<ParserRuleContext> duplicateSpecificationNode = ExtractorUtils.findFirstChildNode(projectionsNode, RuleName.DUPLICATE_SPECIFICATION);
        if (duplicateSpecificationNode.isPresent()) {
            String text = duplicateSpecificationNode.get().getText();
            return "DISTINCT".equalsIgnoreCase(text) || "DISTINCTROW".equalsIgnoreCase(text);
        }
        return false;
    }
    
    private ParserRuleContext findMainQueryNode(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> tableReferencesNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCES);
        if (!tableReferencesNode.isPresent()) {
            return ancestorNode;
        }
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findSingleNodeFromFirstDescendant(tableReferencesNode.get(), RuleName.SUBQUERY);
        if (subqueryNode.isPresent()) {
            return findMainQueryNode(subqueryNode.get());
        }
        return ancestorNode;
    }
}
