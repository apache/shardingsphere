// Generated from TransferRule.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TransferRuleParser}.
 */
public interface TransferRuleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TransferRuleParser#sentences}.
	 * @param ctx the parse tree
	 */
	void enterSentences(TransferRuleParser.SentencesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransferRuleParser#sentences}.
	 * @param ctx the parse tree
	 */
	void exitSentences(TransferRuleParser.SentencesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code KeywordsRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterKeywordsRule(TransferRuleParser.KeywordsRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code KeywordsRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitKeywordsRule(TransferRuleParser.KeywordsRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SQRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterSQRule(TransferRuleParser.SQRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SQRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitSQRule(TransferRuleParser.SQRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LBERule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterLBERule(TransferRuleParser.LBERuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LBERule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitLBERule(TransferRuleParser.LBERuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LBEMultiRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterLBEMultiRule(TransferRuleParser.LBEMultiRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LBEMultiRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitLBEMultiRule(TransferRuleParser.LBEMultiRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LBTMultiRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterLBTMultiRule(TransferRuleParser.LBTMultiRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LBTMultiRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitLBTMultiRule(TransferRuleParser.LBTMultiRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LBTRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterLBTRule(TransferRuleParser.LBTRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LBTRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitLBTRule(TransferRuleParser.LBTRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IdentifierRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierRule(TransferRuleParser.IdentifierRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IdentifierRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierRule(TransferRuleParser.IdentifierRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LPRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterLPRule(TransferRuleParser.LPRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LPRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitLPRule(TransferRuleParser.LPRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ATRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterATRule(TransferRuleParser.ATRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ATRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitATRule(TransferRuleParser.ATRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void enterEqualRule(TransferRuleParser.EqualRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualRule}
	 * labeled alternative in {@link TransferRuleParser#rules}.
	 * @param ctx the parse tree
	 */
	void exitEqualRule(TransferRuleParser.EqualRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransferRuleParser#identifiers}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiers(TransferRuleParser.IdentifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransferRuleParser#identifiers}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiers(TransferRuleParser.IdentifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransferRuleParser#keyWords}.
	 * @param ctx the parse tree
	 */
	void enterKeyWords(TransferRuleParser.KeyWordsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransferRuleParser#keyWords}.
	 * @param ctx the parse tree
	 */
	void exitKeyWords(TransferRuleParser.KeyWordsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransferRuleParser#rbtMulti}.
	 * @param ctx the parse tree
	 */
	void enterRbtMulti(TransferRuleParser.RbtMultiContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransferRuleParser#rbtMulti}.
	 * @param ctx the parse tree
	 */
	void exitRbtMulti(TransferRuleParser.RbtMultiContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransferRuleParser#rbeMulti}.
	 * @param ctx the parse tree
	 */
	void enterRbeMulti(TransferRuleParser.RbeMultiContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransferRuleParser#rbeMulti}.
	 * @param ctx the parse tree
	 */
	void exitRbeMulti(TransferRuleParser.RbeMultiContext ctx);
}