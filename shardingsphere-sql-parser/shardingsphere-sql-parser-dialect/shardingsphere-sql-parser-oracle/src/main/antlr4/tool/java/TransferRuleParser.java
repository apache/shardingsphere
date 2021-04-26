// Generated from TransferRule.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TransferRuleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		KEYWORD_=1, IDENTIFIER_=2, WS=3, FOR_GENERATOR=4, LP_=5, RP_=6, LBE_=7, 
		RBE_=8, LBT_=9, RBT_=10, SEMI_=11, MLT_=12, DOT_=13, VERTICAL_BAR_=14, 
		EQ_=15, COMMA_=16, SQ_=17, AT_=18;
	public static final int
		RULE_sentences = 0, RULE_rules = 1, RULE_identifiers = 2, RULE_keyWords = 3, 
		RULE_rbtMulti = 4, RULE_rbeMulti = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"sentences", "rules", "identifiers", "keyWords", "rbtMulti", "rbeMulti"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'", 
			"'('", "')'", "'{'", "'}'", "'['", "']'", "';'", "'...'", "'.'", "'|'", 
			"'='", "','", "'''", "'@'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "KEYWORD_", "IDENTIFIER_", "WS", "FOR_GENERATOR", "LP_", "RP_", 
			"LBE_", "RBE_", "LBT_", "RBT_", "SEMI_", "MLT_", "DOT_", "VERTICAL_BAR_", 
			"EQ_", "COMMA_", "SQ_", "AT_"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "TransferRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TransferRuleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SentencesContext extends ParserRuleContext {
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public TerminalNode SEMI_() { return getToken(TransferRuleParser.SEMI_, 0); }
		public SentencesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sentences; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterSentences(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitSentences(this);
		}
	}

	public final SentencesContext sentences() throws RecognitionException {
		SentencesContext _localctx = new SentencesContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_sentences);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(13); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(12);
				rules(0);
				}
				}
				setState(15); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KEYWORD_) | (1L << IDENTIFIER_) | (1L << LP_) | (1L << LBE_) | (1L << LBT_) | (1L << SQ_) | (1L << AT_))) != 0) );
			setState(18);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI_) {
				{
				setState(17);
				match(SEMI_);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RulesContext extends ParserRuleContext {
		public RulesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rules; }
	 
		public RulesContext() { }
		public void copyFrom(RulesContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class KeywordsRuleContext extends RulesContext {
		public KeyWordsContext keyWords() {
			return getRuleContext(KeyWordsContext.class,0);
		}
		public KeywordsRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterKeywordsRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitKeywordsRule(this);
		}
	}
	public static class SQRuleContext extends RulesContext {
		public List<TerminalNode> SQ_() { return getTokens(TransferRuleParser.SQ_); }
		public TerminalNode SQ_(int i) {
			return getToken(TransferRuleParser.SQ_, i);
		}
		public RulesContext rules() {
			return getRuleContext(RulesContext.class,0);
		}
		public SQRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterSQRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitSQRule(this);
		}
	}
	public static class LBERuleContext extends RulesContext {
		public TerminalNode LBE_() { return getToken(TransferRuleParser.LBE_, 0); }
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public TerminalNode RBE_() { return getToken(TransferRuleParser.RBE_, 0); }
		public TerminalNode COMMA_() { return getToken(TransferRuleParser.COMMA_, 0); }
		public List<TerminalNode> VERTICAL_BAR_() { return getTokens(TransferRuleParser.VERTICAL_BAR_); }
		public TerminalNode VERTICAL_BAR_(int i) {
			return getToken(TransferRuleParser.VERTICAL_BAR_, i);
		}
		public LBERuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterLBERule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitLBERule(this);
		}
	}
	public static class LBEMultiRuleContext extends RulesContext {
		public TerminalNode LBE_() { return getToken(TransferRuleParser.LBE_, 0); }
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public RbeMultiContext rbeMulti() {
			return getRuleContext(RbeMultiContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(TransferRuleParser.COMMA_, 0); }
		public List<TerminalNode> VERTICAL_BAR_() { return getTokens(TransferRuleParser.VERTICAL_BAR_); }
		public TerminalNode VERTICAL_BAR_(int i) {
			return getToken(TransferRuleParser.VERTICAL_BAR_, i);
		}
		public LBEMultiRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterLBEMultiRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitLBEMultiRule(this);
		}
	}
	public static class LBTMultiRuleContext extends RulesContext {
		public TerminalNode LBT_() { return getToken(TransferRuleParser.LBT_, 0); }
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public RbtMultiContext rbtMulti() {
			return getRuleContext(RbtMultiContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(TransferRuleParser.COMMA_, 0); }
		public List<TerminalNode> VERTICAL_BAR_() { return getTokens(TransferRuleParser.VERTICAL_BAR_); }
		public TerminalNode VERTICAL_BAR_(int i) {
			return getToken(TransferRuleParser.VERTICAL_BAR_, i);
		}
		public LBTMultiRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterLBTMultiRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitLBTMultiRule(this);
		}
	}
	public static class LBTRuleContext extends RulesContext {
		public TerminalNode LBT_() { return getToken(TransferRuleParser.LBT_, 0); }
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public TerminalNode RBT_() { return getToken(TransferRuleParser.RBT_, 0); }
		public TerminalNode COMMA_() { return getToken(TransferRuleParser.COMMA_, 0); }
		public List<TerminalNode> VERTICAL_BAR_() { return getTokens(TransferRuleParser.VERTICAL_BAR_); }
		public TerminalNode VERTICAL_BAR_(int i) {
			return getToken(TransferRuleParser.VERTICAL_BAR_, i);
		}
		public LBTRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterLBTRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitLBTRule(this);
		}
	}
	public static class IdentifierRuleContext extends RulesContext {
		public IdentifiersContext identifiers() {
			return getRuleContext(IdentifiersContext.class,0);
		}
		public IdentifierRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterIdentifierRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitIdentifierRule(this);
		}
	}
	public static class LPRuleContext extends RulesContext {
		public TerminalNode LP_() { return getToken(TransferRuleParser.LP_, 0); }
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public TerminalNode RP_() { return getToken(TransferRuleParser.RP_, 0); }
		public TerminalNode COMMA_() { return getToken(TransferRuleParser.COMMA_, 0); }
		public List<TerminalNode> VERTICAL_BAR_() { return getTokens(TransferRuleParser.VERTICAL_BAR_); }
		public TerminalNode VERTICAL_BAR_(int i) {
			return getToken(TransferRuleParser.VERTICAL_BAR_, i);
		}
		public LPRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterLPRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitLPRule(this);
		}
	}
	public static class ATRuleContext extends RulesContext {
		public TerminalNode AT_() { return getToken(TransferRuleParser.AT_, 0); }
		public RulesContext rules() {
			return getRuleContext(RulesContext.class,0);
		}
		public ATRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterATRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitATRule(this);
		}
	}
	public static class EqualRuleContext extends RulesContext {
		public List<RulesContext> rules() {
			return getRuleContexts(RulesContext.class);
		}
		public RulesContext rules(int i) {
			return getRuleContext(RulesContext.class,i);
		}
		public List<TerminalNode> EQ_() { return getTokens(TransferRuleParser.EQ_); }
		public TerminalNode EQ_(int i) {
			return getToken(TransferRuleParser.EQ_, i);
		}
		public EqualRuleContext(RulesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterEqualRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitEqualRule(this);
		}
	}

	public final RulesContext rules() throws RecognitionException {
		return rules(0);
	}

	private RulesContext rules(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		RulesContext _localctx = new RulesContext(_ctx, _parentState);
		RulesContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_rules, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				_localctx = new IdentifierRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(21);
				identifiers();
				}
				break;
			case 2:
				{
				_localctx = new KeywordsRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(22);
				keyWords();
				}
				break;
			case 3:
				{
				_localctx = new LBEMultiRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(23);
				match(LBE_);
				setState(25);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(24);
					match(COMMA_);
					}
				}

				setState(27);
				rules(0);
				setState(32);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==VERTICAL_BAR_) {
					{
					{
					setState(28);
					match(VERTICAL_BAR_);
					setState(29);
					rules(0);
					}
					}
					setState(34);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(35);
				rbeMulti();
				}
				break;
			case 4:
				{
				_localctx = new LBERuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(37);
				match(LBE_);
				setState(39);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(38);
					match(COMMA_);
					}
				}

				setState(41);
				rules(0);
				setState(46);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==VERTICAL_BAR_) {
					{
					{
					setState(42);
					match(VERTICAL_BAR_);
					setState(43);
					rules(0);
					}
					}
					setState(48);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(49);
				match(RBE_);
				}
				break;
			case 5:
				{
				_localctx = new LBTMultiRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(51);
				match(LBT_);
				setState(53);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(52);
					match(COMMA_);
					}
				}

				setState(55);
				rules(0);
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==VERTICAL_BAR_) {
					{
					{
					setState(56);
					match(VERTICAL_BAR_);
					setState(57);
					rules(0);
					}
					}
					setState(62);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(63);
				rbtMulti();
				}
				break;
			case 6:
				{
				_localctx = new LBTRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(65);
				match(LBT_);
				setState(67);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(66);
					match(COMMA_);
					}
				}

				setState(69);
				rules(0);
				setState(74);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==VERTICAL_BAR_) {
					{
					{
					setState(70);
					match(VERTICAL_BAR_);
					setState(71);
					rules(0);
					}
					}
					setState(76);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(77);
				match(RBT_);
				}
				break;
			case 7:
				{
				_localctx = new LPRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(79);
				match(LP_);
				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(80);
					match(COMMA_);
					}
				}

				setState(83);
				rules(0);
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==VERTICAL_BAR_) {
					{
					{
					setState(84);
					match(VERTICAL_BAR_);
					setState(85);
					rules(0);
					}
					}
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(91);
				match(RP_);
				}
				break;
			case 8:
				{
				_localctx = new SQRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(93);
				match(SQ_);
				setState(94);
				rules(0);
				setState(95);
				match(SQ_);
				}
				break;
			case 9:
				{
				_localctx = new ATRuleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(97);
				match(AT_);
				setState(98);
				rules(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(112);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new EqualRuleContext(new RulesContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_rules);
					setState(101);
					if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
					setState(106); 
					_errHandler.sync(this);
					_alt = 1;
					do {
						switch (_alt) {
						case 1:
							{
							{
							setState(103);
							_errHandler.sync(this);
							_la = _input.LA(1);
							if (_la==EQ_) {
								{
								setState(102);
								match(EQ_);
								}
							}

							setState(105);
							rules(0);
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(108); 
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
					} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
					}
					} 
				}
				setState(114);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class IdentifiersContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER_() { return getTokens(TransferRuleParser.IDENTIFIER_); }
		public TerminalNode IDENTIFIER_(int i) {
			return getToken(TransferRuleParser.IDENTIFIER_, i);
		}
		public List<TerminalNode> DOT_() { return getTokens(TransferRuleParser.DOT_); }
		public TerminalNode DOT_(int i) {
			return getToken(TransferRuleParser.DOT_, i);
		}
		public IdentifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterIdentifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitIdentifiers(this);
		}
	}

	public final IdentifiersContext identifiers() throws RecognitionException {
		IdentifiersContext _localctx = new IdentifiersContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_identifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(122); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(115);
					match(IDENTIFIER_);
					setState(117);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
					case 1:
						{
						setState(116);
						match(DOT_);
						}
						break;
					}
					setState(120);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
					case 1:
						{
						setState(119);
						match(IDENTIFIER_);
						}
						break;
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(124); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyWordsContext extends ParserRuleContext {
		public List<TerminalNode> KEYWORD_() { return getTokens(TransferRuleParser.KEYWORD_); }
		public TerminalNode KEYWORD_(int i) {
			return getToken(TransferRuleParser.KEYWORD_, i);
		}
		public KeyWordsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyWords; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterKeyWords(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitKeyWords(this);
		}
	}

	public final KeyWordsContext keyWords() throws RecognitionException {
		KeyWordsContext _localctx = new KeyWordsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_keyWords);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(127); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(126);
					match(KEYWORD_);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(129); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RbtMultiContext extends ParserRuleContext {
		public TerminalNode RBT_() { return getToken(TransferRuleParser.RBT_, 0); }
		public TerminalNode MLT_() { return getToken(TransferRuleParser.MLT_, 0); }
		public RbtMultiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rbtMulti; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterRbtMulti(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitRbtMulti(this);
		}
	}

	public final RbtMultiContext rbtMulti() throws RecognitionException {
		RbtMultiContext _localctx = new RbtMultiContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_rbtMulti);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			match(RBT_);
			setState(132);
			match(MLT_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RbeMultiContext extends ParserRuleContext {
		public TerminalNode RBE_() { return getToken(TransferRuleParser.RBE_, 0); }
		public TerminalNode MLT_() { return getToken(TransferRuleParser.MLT_, 0); }
		public RbeMultiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rbeMulti; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).enterRbeMulti(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransferRuleListener ) ((TransferRuleListener)listener).exitRbeMulti(this);
		}
	}

	public final RbeMultiContext rbeMulti() throws RecognitionException {
		RbeMultiContext _localctx = new RbeMultiContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_rbeMulti);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(RBE_);
			setState(135);
			match(MLT_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return rules_sempred((RulesContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean rules_sempred(RulesContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 8);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\24\u008c\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\6\2\20\n\2\r\2\16\2\21\3"+
		"\2\5\2\25\n\2\3\3\3\3\3\3\3\3\3\3\5\3\34\n\3\3\3\3\3\3\3\7\3!\n\3\f\3"+
		"\16\3$\13\3\3\3\3\3\3\3\3\3\5\3*\n\3\3\3\3\3\3\3\7\3/\n\3\f\3\16\3\62"+
		"\13\3\3\3\3\3\3\3\3\3\5\38\n\3\3\3\3\3\3\3\7\3=\n\3\f\3\16\3@\13\3\3\3"+
		"\3\3\3\3\3\3\5\3F\n\3\3\3\3\3\3\3\7\3K\n\3\f\3\16\3N\13\3\3\3\3\3\3\3"+
		"\3\3\5\3T\n\3\3\3\3\3\3\3\7\3Y\n\3\f\3\16\3\\\13\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\5\3f\n\3\3\3\3\3\5\3j\n\3\3\3\6\3m\n\3\r\3\16\3n\7\3q\n\3"+
		"\f\3\16\3t\13\3\3\4\3\4\5\4x\n\4\3\4\5\4{\n\4\6\4}\n\4\r\4\16\4~\3\5\6"+
		"\5\u0082\n\5\r\5\16\5\u0083\3\6\3\6\3\6\3\7\3\7\3\7\3\7\2\3\4\b\2\4\6"+
		"\b\n\f\2\2\2\u00a0\2\17\3\2\2\2\4e\3\2\2\2\6|\3\2\2\2\b\u0081\3\2\2\2"+
		"\n\u0085\3\2\2\2\f\u0088\3\2\2\2\16\20\5\4\3\2\17\16\3\2\2\2\20\21\3\2"+
		"\2\2\21\17\3\2\2\2\21\22\3\2\2\2\22\24\3\2\2\2\23\25\7\r\2\2\24\23\3\2"+
		"\2\2\24\25\3\2\2\2\25\3\3\2\2\2\26\27\b\3\1\2\27f\5\6\4\2\30f\5\b\5\2"+
		"\31\33\7\t\2\2\32\34\7\22\2\2\33\32\3\2\2\2\33\34\3\2\2\2\34\35\3\2\2"+
		"\2\35\"\5\4\3\2\36\37\7\20\2\2\37!\5\4\3\2 \36\3\2\2\2!$\3\2\2\2\" \3"+
		"\2\2\2\"#\3\2\2\2#%\3\2\2\2$\"\3\2\2\2%&\5\f\7\2&f\3\2\2\2\')\7\t\2\2"+
		"(*\7\22\2\2)(\3\2\2\2)*\3\2\2\2*+\3\2\2\2+\60\5\4\3\2,-\7\20\2\2-/\5\4"+
		"\3\2.,\3\2\2\2/\62\3\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\63\3\2\2\2\62"+
		"\60\3\2\2\2\63\64\7\n\2\2\64f\3\2\2\2\65\67\7\13\2\2\668\7\22\2\2\67\66"+
		"\3\2\2\2\678\3\2\2\289\3\2\2\29>\5\4\3\2:;\7\20\2\2;=\5\4\3\2<:\3\2\2"+
		"\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?A\3\2\2\2@>\3\2\2\2AB\5\n\6\2Bf\3\2\2"+
		"\2CE\7\13\2\2DF\7\22\2\2ED\3\2\2\2EF\3\2\2\2FG\3\2\2\2GL\5\4\3\2HI\7\20"+
		"\2\2IK\5\4\3\2JH\3\2\2\2KN\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MO\3\2\2\2NL\3\2"+
		"\2\2OP\7\f\2\2Pf\3\2\2\2QS\7\7\2\2RT\7\22\2\2SR\3\2\2\2ST\3\2\2\2TU\3"+
		"\2\2\2UZ\5\4\3\2VW\7\20\2\2WY\5\4\3\2XV\3\2\2\2Y\\\3\2\2\2ZX\3\2\2\2Z"+
		"[\3\2\2\2[]\3\2\2\2\\Z\3\2\2\2]^\7\b\2\2^f\3\2\2\2_`\7\23\2\2`a\5\4\3"+
		"\2ab\7\23\2\2bf\3\2\2\2cd\7\24\2\2df\5\4\3\3e\26\3\2\2\2e\30\3\2\2\2e"+
		"\31\3\2\2\2e\'\3\2\2\2e\65\3\2\2\2eC\3\2\2\2eQ\3\2\2\2e_\3\2\2\2ec\3\2"+
		"\2\2fr\3\2\2\2gl\f\n\2\2hj\7\21\2\2ih\3\2\2\2ij\3\2\2\2jk\3\2\2\2km\5"+
		"\4\3\2li\3\2\2\2mn\3\2\2\2nl\3\2\2\2no\3\2\2\2oq\3\2\2\2pg\3\2\2\2qt\3"+
		"\2\2\2rp\3\2\2\2rs\3\2\2\2s\5\3\2\2\2tr\3\2\2\2uw\7\4\2\2vx\7\17\2\2w"+
		"v\3\2\2\2wx\3\2\2\2xz\3\2\2\2y{\7\4\2\2zy\3\2\2\2z{\3\2\2\2{}\3\2\2\2"+
		"|u\3\2\2\2}~\3\2\2\2~|\3\2\2\2~\177\3\2\2\2\177\7\3\2\2\2\u0080\u0082"+
		"\7\3\2\2\u0081\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0081\3\2\2\2\u0083"+
		"\u0084\3\2\2\2\u0084\t\3\2\2\2\u0085\u0086\7\f\2\2\u0086\u0087\7\16\2"+
		"\2\u0087\13\3\2\2\2\u0088\u0089\7\n\2\2\u0089\u008a\7\16\2\2\u008a\r\3"+
		"\2\2\2\26\21\24\33\")\60\67>ELSZeinrwz~\u0083";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}