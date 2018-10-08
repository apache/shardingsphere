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

package io.shardingsphere.core.parsing.antler.utils;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class TreeUtils {
    public static final String RULE_SUFFIX = "Context";


    /**
     * Find first child node whose rule name is ${name}.
     *
     * @param node start node
     * @param name rule name
     * @return match node
     */
    public static ParserRuleContext getFirstChildByRuleName(final ParserRuleContext node, final String name) {
        if (null == node) {
            return null;
        }

        String ruleName = name;
        if (name.indexOf(RULE_SUFFIX) < 0) {
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
     * @return match nodes
     */
    public static List<ParserRuleContext> getAllDescendantByRuleName(final ParserRuleContext node, final String name) {
        if (null == node) {
            return null;
        }

        String ruleName = name;
        if (name.indexOf(RULE_SUFFIX) < 0) {
            ruleName = Character.toUpperCase(name.charAt(0)) + name.substring(1) + RULE_SUFFIX;
        }

        List<ParserRuleContext> childs = new ArrayList<>();
        if (ruleName.equals(node.getClass().getSimpleName())) {
            childs.add(node);
        }

        int count = node.getChildCount();
        if (0 == count) {
            return childs;
        }

        List<ParserRuleContext> childNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof ParserRuleContext) {
                childNodes.add((ParserRuleContext) child);
            }
        }

        for (final ParserRuleContext child : childNodes) {
            List<ParserRuleContext> retChilds = getAllDescendantByRuleName(child, name);
            if (retChilds != null) {
                childs.addAll(retChilds);
            }
        }

        return childs;
    }


    /**
     * Determine whether the two class is compatible.
     *
     * @param c1 first param
     * @param c2 second param
     * @return true is compatible
     */
    public static boolean isCompatible(final Class<?> c1, final Class<?> c2) {
        return c1 == c2 || c2.isAssignableFrom(c1);
    }
}
