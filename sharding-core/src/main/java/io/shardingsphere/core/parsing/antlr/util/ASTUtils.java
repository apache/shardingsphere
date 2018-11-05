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

package io.shardingsphere.core.parsing.antlr.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
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
    
    private static final String RULE_SUFFIX = "Context";
    
    /**
     * Find first child node whose rule name is ${name}.
     *
     * @param node start node
     * @param name rule name
     * @return matched node
     */
    public static Optional<ParserRuleContext> findFirstChildByRuleName(final ParserRuleContext node, final String name) {
        if (null == node) {
            return Optional.absent();
        }
        if (isMatchedNode(node, name)) {
            return Optional.of(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (!(child instanceof ParserRuleContext)) {
                continue;
            }
            Optional<ParserRuleContext> result = findFirstChildByRuleName((ParserRuleContext) child, name);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    /**
     * Find all children node by rule name.
     *
     * @param node start node
     * @param name rule name
     * @return matched nodes
     */
    public static Collection<ParserRuleContext> getAllDescendantByRuleName(final ParserRuleContext node, final String name) {
        if (null == node) {
            return Collections.emptyList();
        }
        List<ParserRuleContext> result = new LinkedList<>();
        if (isMatchedNode(node, name)) {
            result.add(node);
        }
        for (ParserRuleContext each : getChildrenNodes(node)) {
            result.addAll(getAllDescendantByRuleName(each, name));
        }
        return result;
    }
    
    private static boolean isMatchedNode(final ParserRuleContext node, final String name) {
        return getRuleName(name).equals(node.getClass().getSimpleName());
    }
    
    private static String getRuleName(final String name) {
        return name.contains(RULE_SUFFIX) ? name : CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name + RULE_SUFFIX);
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
