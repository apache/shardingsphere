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

package org.apache.shardingsphere.sql.parser.core.extractor.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Extractor utility.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtractorUtils {
    
    /**
     * Get first child node.
     *
     * @param node start node
     * @param ruleName rule name
     * @return matched node
     */
    public static ParserRuleContext getFirstChildNode(final ParserRuleContext node, final RuleName ruleName) {
        Optional<ParserRuleContext> result = findFirstChildNode(node, ruleName);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
    
    /**
     * Find first child node.
     *
     * @param node start node
     * @param ruleName rule name
     * @return matched node
     */
    public static Optional<ParserRuleContext> findFirstChildNode(final ParserRuleContext node, final RuleName ruleName) {
        Queue<ParserRuleContext> parserRuleContexts = new LinkedList<>();
        parserRuleContexts.add(node);
        ParserRuleContext parserRuleContext;
        while (null != (parserRuleContext = parserRuleContexts.poll())) {
            if (isMatchedNode(parserRuleContext, ruleName)) {
                return Optional.of(parserRuleContext);
            }
            for (int i = 0; i < parserRuleContext.getChildCount(); i++) {
                if (parserRuleContext.getChild(i) instanceof ParserRuleContext) {
                    parserRuleContexts.add((ParserRuleContext) parserRuleContext.getChild(i));
                }
            }
        }
        return Optional.absent();
    }
    
    /**
     * Find first child node none recursive.
     * 
     * @param node start node
     * @param ruleName rule name
     * @return matched node
     */
    public static Optional<ParserRuleContext> findFirstChildNodeNoneRecursive(final ParserRuleContext node, final RuleName ruleName) {
        if (isMatchedNode(node, ruleName)) {
            return Optional.of(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i) instanceof ParserRuleContext) {
                ParserRuleContext child = (ParserRuleContext) node.getChild(i);
                if (isMatchedNode(child, ruleName)) {
                    return Optional.of(child);
                }
            }
        }
        return Optional.absent();
    }
    
    /**
     * Find single node from first descendant which only has one child.
     *
     * @param node start node
     * @param ruleName rule name
     * @return matched node
     */
    public static Optional<ParserRuleContext> findSingleNodeFromFirstDescendant(final ParserRuleContext node, final RuleName ruleName) {
        ParserRuleContext nextNode = node;
        do {
            if (isMatchedNode(nextNode, ruleName)) {
                return Optional.of(nextNode);
            }
            if (1 != nextNode.getChildCount() || !(nextNode.getChild(0) instanceof ParserRuleContext)) {
                return Optional.absent();
            }
            nextNode = (ParserRuleContext) nextNode.getChild(0);
        } while (null != nextNode);
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
        Collection<ParserRuleContext> result = new LinkedList<>();
        for (ParserRuleContext each : getAllNodes(node)) {
            if (isMatchedNode(each, ruleName)) {
                result.add(each);
            }
        }
        return result;
    }

    /**
     * Get all nodes.
     *
     * @param node start node
     * @return all nodes
     */
    private static Collection<ParserRuleContext> getAllNodes(final ParserRuleContext node) {
        Collection<ParserRuleContext> result = new LinkedList<>();
        LinkedList<ParserRuleContext> stack = new LinkedList<>();
        stack.add(node);
        while (!stack.isEmpty()) {
            ParserRuleContext each = stack.pop();
            result.add(each);
            ParserRuleContext[] childNodes = getChildrenNodes(each).toArray(new ParserRuleContext[0]);
            for (int i = childNodes.length - 1; i >= 0; i--) {
                stack.push(childNodes[i]);
            }
        }
        return result;
    }
    
    private static boolean isMatchedNode(final ParserRuleContext node, final RuleName ruleName) {
        return ruleName.getName().equals(node.getClass().getSimpleName());
    }
    
    private static Collection<ParserRuleContext> getChildrenNodes(final ParserRuleContext node) {
        Collection<ParserRuleContext> result = new LinkedList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof ParserRuleContext) {
                result.add((ParserRuleContext) child);
            }
        }
        return result;
    }
}
