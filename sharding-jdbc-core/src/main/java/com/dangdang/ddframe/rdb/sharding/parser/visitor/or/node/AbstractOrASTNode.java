/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.visitor.or.node;

import java.util.ArrayList;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * 抽象的OR语法树节点.
 * 
 * @author gaohongtao
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractOrASTNode {
    
    private final List<AbstractOrASTNode> subNodes = new ArrayList<>();
    
    private final List<List<Condition>> nestedConditions = new ArrayList<>();
    
    public final void addSubNode(final AbstractOrASTNode node) {
        subNodes.add(node);
    }
    
    protected final void addNestedConditions(final ConditionContext conditionContext) {
        nestedConditions.add(Lists.newArrayList(conditionContext.getAllConditions()));
    }
    
    /**
     * 使用该节点作为根节点生成抽象语法树.
     * 
     * <p>
     * 使用深度优先后续的方式生成语法树.
     * 其中后续遍历是由于DRUID进行SQL语法解析时产生的行为.
     * </p>
     */
    public abstract void createOrASTAsRootNode();
    
    /**
     * 获取解析结果需要的条件.
     * 
     * @return 解析后的条件
     */
    public final List<ConditionContext> getCondition() {
        return Lists.transform(nestedConditions, new Function<List<Condition>, ConditionContext>() {
            
            @Override
            public ConditionContext apply(final List<Condition> input) {
                ConditionContext result = new ConditionContext();
                for (Condition each : input) {
                    result.add(each);
                }
                return result;
            }
        });
    }
    
    /**
     * 多个子节点之间做笛卡尔积.
     */
    protected final void mergeSubConditions() {
        if (subNodes.isEmpty()) {
            return;
        }
        List<List<Condition>> result = new ArrayList<>();
        result.addAll(subNodes.get(0).getNestedConditions());
        for (int i = 1; i < subNodes.size(); i++) {
            result = cartesianNestedConditions(result, subNodes.get(i).getNestedConditions());
        }
        nestedConditions.addAll(result);
    }
    
    private List<List<Condition>> cartesianNestedConditions(final List<List<Condition>> oneNestedConditions, final List<List<Condition>> anotherNestedConditions) {
        List<List<Condition>> result = new ArrayList<>();
        for (List<Condition> oneNestedCondition : oneNestedConditions) {
            for (List<Condition> anotherNestedCondition : anotherNestedConditions) {
                List<Condition> mergedConditions = new ArrayList<>();
                mergedConditions.addAll(oneNestedCondition);
                mergedConditions.addAll(anotherNestedCondition);
                result.add(mergedConditions);
            }
        }
        return result;
    }
}
