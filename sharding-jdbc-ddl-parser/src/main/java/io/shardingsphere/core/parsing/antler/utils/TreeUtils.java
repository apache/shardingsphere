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
import org.antlr.v4.runtime.tree.TerminalNode;

public class TreeUtils {
    public static final String RULE_SUFFIX = "Context";

    /**Find ancestor node whose class is ${clazz}.
     * 
     * @param node start node
     * @param clazz return node class
     * @return match node
     */
    public static ParseTree getAncestorByClass(final ParseTree node, final Class<?> clazz) {
        if (null == node) {
            return null;
        }

        ParseTree parentNode = node.getParent();
        while (null != parentNode) {
            if (isCompatible(parentNode.getClass(), clazz)) {
                return parentNode;
            }
            parentNode = parentNode.getParent();
        }
        return null;
    }

    /**Find first child node whose class is ${clazz}.
     * 
     * @param node start node
     * @param clazz return node class
     * @return match node
     */
    public static ParseTree getFirstChildByClass(final ParseTree node, final Class<?> clazz) {
        if (null == node) {
            return null;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            ParseTree retNode = getFirstChildByClass(child, clazz);
            if (null != retNode) {
                return retNode;
            }
        }

        return null;
    }

    /**Find first child node whose rule name is ${name}.
     * 
     * @param node start node
     * @param name rule name
     * @return match node
     */
    public static ParseTree getFirstChildByRuleName(final ParseTree node, final String name) {
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
            ParseTree retNode = getFirstChildByRuleName(child, name);
            if (null != retNode) {
                return retNode;
            }
        }

        return null;
    }

    /**Find all children node whose rule name is ${name}.
     * 
     * @param node start node
     * @param name rule name
     * @return match nodes
     */
    public static List<ParseTree> getAllDescendantByRuleName(final ParseTree node, final String name) {
        if (null == node) {
            return null;
        }

        String ruleName = name;
        if (name.indexOf(RULE_SUFFIX) < 0) {
            ruleName = Character.toUpperCase(name.charAt(0)) + name.substring(1) + RULE_SUFFIX;
        }

        List<ParseTree> childs = new ArrayList<>();
        if (ruleName.equals(node.getClass().getSimpleName())) {
            childs.add(node);
        }

        int count = node.getChildCount();
        if (0 == count) {
            return childs;
        }

        List<ParseTree> childNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);
            childNodes.add(child);
        }

        for (final ParseTree child : childNodes) {
            List<ParseTree> retChilds = getAllDescendantByRuleName(child, name);
            if (retChilds != null) {
                childs.addAll(retChilds);
            }
        }

        return childs;
    }

    /**Find all children node whose class is ${clazz}.
     * 
     * @param node start node
     * @param clazz return node class
     * @return match nodes
     */
    public static List<ParserRuleContext> getChildByClass(final ParseTree node, final Class<?> clazz) {
        if (null == node) {
            return null;
        }

        List<ParserRuleContext> childs = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (!isCompatible(child.getClass(), clazz)) {
                continue;
            }
            
            if (child instanceof ParserRuleContext) {
                childs.add((ParserRuleContext) child);
            }
        }

        return childs;
    }

    /**Find all children node whose class is ${clazz}.
     * 
     * @param node start node
     * @param clazz return node class
     * @return match nodes
     */
    public static List<ParseTree> getAllDescendantByClass(final ParseTree node, final Class<?> clazz) {
        if (null == node) {
            return null;
        }

        List<ParseTree> childs = new ArrayList<>();
        if (isCompatible(node.getClass(), clazz)) {
            childs.add(node);
        }

        int count = node.getChildCount();
        if (0 == count) {
            return childs;
        }

        List<ParseTree> childNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);
            childNodes.add(child);
        }

        for (final ParseTree child : childNodes) {
            List<ParseTree> retChilds = getAllDescendantByClass(child, clazz);
            if (retChilds != null) {
                childs.addAll(retChilds);
            }
        }

        return childs;
    }

    /**Find all top level children node whose class is ${clazz}.
     * 
     * @param node start node
     * @param clazz return node class
     * @return match nodes
     */
    public static List<ParseTree> getAllTopDescendantByClass(final ParseTree node, final Class<?> clazz) {
        List<ParseTree> childs = new ArrayList<>();

        if (null == node) {
            return childs;
        }

        if (isCompatible(node.getClass(), clazz)) {
            childs.add(node);
            return childs;
        }

        int count = node.getChildCount();
        if (0 == count) {
            return childs;
        }

        List<ParseTree> childNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);
            if (isCompatible(child.getClass(), clazz)) {
                childs.add(child);
            } else {
                childNodes.add(child);
            }
        }

        for (final ParseTree child : childNodes) {
            List<? extends ParseTree> retChilds = getAllTopDescendantByClass(child, clazz);
            if (retChilds != null) {
                childs.addAll(retChilds);
            }
        }

        return childs;
    }

    /**Find first children node whose class is ${clazz}.
     * 
     * @param node start node
     * @param type return node class
     * @param onlyChild  true only find child node, false find all descendant node
     * @return match node
     */
    public static ParseTree getFirstDescendant(final ParseTree node, final Class<?> type, final boolean onlyChild) {
        if (null == node) {
            return null;
        }

        if (isCompatible(node.getClass(), type)) {
            return node;
        }

        int count = node.getChildCount();
        if (0 == count) {
            return null;
        }

        List<ParseTree> childNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);

            if (isCompatible(child.getClass(), type)) {
                return child;
            }

            if (!onlyChild) {
                childNodes.add(child);
            }
        }

        if (!onlyChild) {
            for (final ParseTree childNode : childNodes) {
                ParseTree retNode = getFirstDescendant(childNode, type, onlyChild);
                if (null != retNode) {
                    return retNode;
                }
            }
        }

        return null;
    }

    /**Find first descendant node whose type is ${type}.
     * 
     * @param node start node
     * @param name terminal node name
     * @return match node
     */
    public static TerminalNode getFirstTerminalByType(final ParseTree node, final String name) {
        if (null == node) {
            return null;
        }

        if (node instanceof TerminalNode) {
            TerminalNode terminal = (TerminalNode) node;
            if (terminal.getSymbol().getText().equals(name)) {
                return terminal;
            } else {
                return null;
            }
        }

        int count = node.getChildCount();
        if (0 == count) {
            return null;
        }

        List<ParseTree> nonterminalChildNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);

            if (child instanceof TerminalNode) {
                TerminalNode terminal = (TerminalNode) child;
                if (terminal.getSymbol().getText().equals(name)) {
                    return terminal;
                }
            } else {
                nonterminalChildNodes.add(child);
            }
        }

        for (final ParseTree nonterminalNode : nonterminalChildNodes) {
            TerminalNode retNode = getFirstTerminalByType(nonterminalNode, name);
            if (null != retNode) {
                return retNode;
            }
        }

        return null;
    }

    /**Find descendant nodes whose type is ${type}.
     * 
     * @param node start node
     * @param type terminal node type
     * @return match nodes
     */
    public static List<TerminalNode> getAllTerminalByType(final ParseTree node, final int type) {
        List<TerminalNode> retNodes = new ArrayList<>();
        if (null == node) {
            return retNodes;
        }

        if (node instanceof TerminalNode) {
            TerminalNode terminal = (TerminalNode) node;
            if (terminal.getSymbol().getType() == type) {
                retNodes.add(terminal);
            } else {
                return retNodes;
            }
        }

        int count = node.getChildCount();
        if (0 == count) {
            return retNodes;
        }

        List<ParseTree> nonTerminalChilds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNode) {
                TerminalNode terminal = (TerminalNode) child;
                if (terminal.getSymbol().getType() == type) {
                    retNodes.add(terminal);
                }
            } else {
                nonTerminalChilds.add(child);
            }
        }

        int childCount = nonTerminalChilds.size();
        for (int j = 0; j < childCount; j++) {
            List<TerminalNode> childRetNodes = getAllTerminalByType(nonTerminalChilds.get(j), type);
            if (childRetNodes != null) {
                retNodes.addAll(childRetNodes);
            }
        }

        return retNodes;
    }

    /**Determine whether the two class is compatible.
     * 
     * @param c1 first param
     * @param c2 second param
     * @return true is compatible
     */
    public static boolean isCompatible(final Class<?> c1, final Class<?> c2) {
        return c1 == c2 || c2.isAssignableFrom(c1);
    }
}
