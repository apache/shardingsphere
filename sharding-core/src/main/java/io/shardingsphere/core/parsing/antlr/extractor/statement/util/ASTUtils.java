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

package io.shardingsphere.core.parsing.antlr.extractor.statement.util;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.RuleName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * AST utility.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ASTUtils {
    
    /**
     * Find first child node.
     *
     * @param node start node
     * @param ruleName rule name
     * @return matched node
     */
    public static Optional<ParserRuleContext> findFirstChildNode(final ParserRuleContext node, final RuleName ruleName) {
        if (null == node) {
            return Optional.absent();
        }
        if (isMatchedNode(node, ruleName)) {
            return Optional.of(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (!(child instanceof ParserRuleContext)) {
                continue;
            }
            Optional<ParserRuleContext> result = findFirstChildNode((ParserRuleContext) child, ruleName);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get all descendant nodes.
     *
     * @param node start node
     * @param ruleName rule name
     * @return all descendant nodes
     */
    public static Collection<ParserRuleContext> getAllDescendantNodes(final ParserRuleContext node, final RuleName ruleName) {
        if (null == node) {
            return Collections.emptyList();
        }
        List<ParserRuleContext> result = new LinkedList<>();
        if (isMatchedNode(node, ruleName)) {
            result.add(node);
        }
        for (ParserRuleContext each : getChildrenNodes(node)) {
            result.addAll(getAllDescendantNodes(each, ruleName));
        }
        return result;
    }
    
    private static boolean isMatchedNode(final ParserRuleContext node, final RuleName ruleName) {
        return ruleName.getName().equals(node.getClass().getSimpleName());
    }
    
    private static List<ParserRuleContext> getChildrenNodes(final ParserRuleContext node) {
        List<ParserRuleContext> result = new LinkedList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof ParserRuleContext) {
                result.add((ParserRuleContext) child);
            }
        }
        return result;
    }
}
