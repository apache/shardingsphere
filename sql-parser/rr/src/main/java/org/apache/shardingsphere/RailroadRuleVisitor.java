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

package org.apache.shardingsphere;

import lombok.Getter;
import nl.bigo.rrdantlr4.ANTLRv4Parser.LexerAtomContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.LexerRuleContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.ParserRuleSpecContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.RulerefContext;
import nl.bigo.rrdantlr4.RuleVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Railroad rule visitor
 */
public class RailroadRuleVisitor extends RuleVisitor {

    @Getter
    private Map<String, Set<String>> rulesRelation = new HashMap<>();

    @Override
    public String visitRuleref(@NotNull RulerefContext ctx) {
        buildRelation(ctx);
        return super.visitRuleref(ctx);
    }

    @Override
    public String visitLexerAtom(@NotNull LexerAtomContext ctx) {
        if (ctx.RULE_REF() != null) {{
            buildRelation(ctx);
        }}
        return super.visitLexerAtom(ctx);
    }

    private void buildRelation(RulerefContext ctx){
        ParserRuleContext context = ctx.getParent();
        while (null != context){
            if(context instanceof ParserRuleSpecContext){
                String ruleName = ((ParserRuleSpecContext) context).RULE_REF().getText();
                if(rulesRelation.containsKey(ruleName)){
                    rulesRelation.get(ruleName).add(ctx.RULE_REF().getText());
                }else {
                    Set<String> temp = new HashSet<>();
                    temp.add(ctx.RULE_REF().getText());
                    rulesRelation.put(ruleName,temp);
                }
                break;
            }
            context = context.getParent();
        }
    }

    private void buildRelation(LexerAtomContext ctx){
        ParserRuleContext context = ctx.getParent();
        while (null != context){
            if(context instanceof LexerRuleContext){
                String ruleName = ((LexerRuleContext) context).TOKEN_REF().getText();
                if(rulesRelation.containsKey(ruleName)){
                    rulesRelation.get(ruleName).add(ctx.RULE_REF().getText());
                }else {
                    Set<String> temp = new HashSet<>();
                    temp.add(ctx.RULE_REF().getText());
                    rulesRelation.put(ruleName,temp);
                }
                break;
            }
            context = context.getParent();
        }
    }
}
