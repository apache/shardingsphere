package org.apache.shardingsphere;

import lombok.Getter;
import nl.bigo.rrdantlr4.ANTLRv4Parser.IdContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.LexerAtomContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.LexerRuleContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.ParserRuleSpecContext;
import nl.bigo.rrdantlr4.ANTLRv4Parser.RulerefContext;
import nl.bigo.rrdantlr4.RuleVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RailroadRuleVistor extends RuleVisitor {

    @Getter
    private Map<String, String> rulesRelation = new HashMap<>();

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
                rulesRelation.put(((ParserRuleSpecContext) context).RULE_REF().getText(),ctx.RULE_REF().getText());
                break;
            }
            context = context.getParent();
        }
    }

    private void buildRelation(LexerAtomContext ctx){
        ParserRuleContext context = ctx.getParent();
        while (null != context){
            if(context instanceof LexerRuleContext){
                rulesRelation.put(((LexerRuleContext) context).TOKEN_REF().getText(),ctx.RULE_REF().getText());
                break;
            }
            context = context.getParent();
        }
    }
}
