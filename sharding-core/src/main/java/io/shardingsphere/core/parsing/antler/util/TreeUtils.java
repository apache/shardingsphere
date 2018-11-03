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

package io.shardingsphere.core.parsing.antler.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Visit AST utils.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TreeUtils {
    
    private static final String RULE_SUFFIX = "Context";
    
    /**
     * Find first child node whose rule name is ${name}.
     *
     * @param node start node
     * @param name rule name
     * @return matched node
     */
    public static ParserRuleContext getFirstChildByRuleName(final ParserRuleContext node, final String name) {
        if (null == node) {
            return null;
        }
        String ruleName = name;
        if (!name.contains(RULE_SUFFIX)) {
            ruleName = Character.toUpperCase(name.charAt(0)) + name.substring(1) + RULE_SUFFIX;
        }
        if (ruleName.equals(node.getClass().getSimpleName())) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (!(child instanceof ParserRuleContext)) {
                continue;
            }
            ParserRuleContext retNode = getFirstChildByRuleName((ParserRuleContext) child, name);
            if (null != retNode) {
                return retNode;
            }
        }
        return null;
    }
    
    /**
     * Find all children node whose rule name is ${name}.
     *
     * @param node start node
     * @param name rule name
     * @return matched nodes
     */
    public static List<ParserRuleContext> getAllDescendantByRuleName(final ParserRuleContext node, final String name) {
        if (null == node) {
            return null;
        }
        String ruleName = name;
        if (!name.contains(RULE_SUFFIX)) {
            ruleName = Character.toUpperCase(name.charAt(0)) + name.substring(1) + RULE_SUFFIX;
        }
        List<ParserRuleContext> result = new ArrayList<>();
        if (ruleName.equals(node.getClass().getSimpleName())) {
            result.add(node);
        }
        int count = node.getChildCount();
        if (0 == count) {
            return result;
        }
        List<ParserRuleContext> childNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof ParserRuleContext) {
                childNodes.add((ParserRuleContext) child);
            }
        }
        for (ParserRuleContext each : childNodes) {
            List<ParserRuleContext> retChilds = getAllDescendantByRuleName(each, name);
            if (null != retChilds) {
                result.addAll(retChilds);
            }
        }
        return result;
    }
}
