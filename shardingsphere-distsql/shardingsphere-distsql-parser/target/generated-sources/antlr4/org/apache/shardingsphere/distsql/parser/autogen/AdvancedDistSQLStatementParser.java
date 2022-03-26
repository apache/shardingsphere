// Generated from org/apache/shardingsphere/distsql/parser/autogen/AdvancedDistSQLStatement.g4 by ANTLR 4.9.2
package org.apache.shardingsphere.distsql.parser.autogen;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class AdvancedDistSQLStatementParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, NOT=3, TILDE=4, VERTICALBAR=5, AMPERSAND=6, SIGNEDLEFTSHIFT=7, 
		SIGNEDRIGHTSHIFT=8, CARET=9, MOD=10, COLON=11, PLUS=12, MINUS=13, ASTERISK=14, 
		SLASH=15, BACKSLASH=16, DOT=17, DOTASTERISK=18, SAFEEQ=19, DEQ=20, EQ=21, 
		NEQ=22, GT=23, GTE=24, LT=25, LTE=26, POUND=27, LP=28, RP=29, LBE=30, 
		RBE=31, LBT=32, RBT=33, COMMA=34, DQ=35, SQ=36, BQ=37, QUESTION=38, AT=39, 
		SEMI=40, JSONSEPARATOR=41, UL=42, DL=43, WS=44, PREVIEW=45, PARSE=46, 
		FORMAT=47, SQLString=48, FOR_GENERATOR=49;
	public static final int
		RULE_execute = 0, RULE_previewSQL = 1, RULE_parseSQL = 2, RULE_formatSQL = 3, 
		RULE_sql = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"execute", "previewSQL", "parseSQL", "formatSQL", "sql"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", 
			"'%'", "':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", 
			"'=='", "'='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", "')'", 
			"'{'", "'}'", "'['", "']'", "','", "'\"'", "'''", "'`'", "'?'", "'@'", 
			"';'", "'->>'", "'_'", "'$'", null, null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "AND", "OR", "NOT", "TILDE", "VERTICALBAR", "AMPERSAND", "SIGNEDLEFTSHIFT", 
			"SIGNEDRIGHTSHIFT", "CARET", "MOD", "COLON", "PLUS", "MINUS", "ASTERISK", 
			"SLASH", "BACKSLASH", "DOT", "DOTASTERISK", "SAFEEQ", "DEQ", "EQ", "NEQ", 
			"GT", "GTE", "LT", "LTE", "POUND", "LP", "RP", "LBE", "RBE", "LBT", "RBT", 
			"COMMA", "DQ", "SQ", "BQ", "QUESTION", "AT", "SEMI", "JSONSEPARATOR", 
			"UL", "DL", "WS", "PREVIEW", "PARSE", "FORMAT", "SQLString", "FOR_GENERATOR"
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
	public String getGrammarFileName() { return "AdvancedDistSQLStatement.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public AdvancedDistSQLStatementParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ExecuteContext extends ParserRuleContext {
		public PreviewSQLContext previewSQL() {
			return getRuleContext(PreviewSQLContext.class,0);
		}
		public ParseSQLContext parseSQL() {
			return getRuleContext(ParseSQLContext.class,0);
		}
		public FormatSQLContext formatSQL() {
			return getRuleContext(FormatSQLContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(AdvancedDistSQLStatementParser.SEMI, 0); }
		public ExecuteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execute; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AdvancedDistSQLStatementVisitor ) return ((AdvancedDistSQLStatementVisitor<? extends T>)visitor).visitExecute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExecuteContext execute() throws RecognitionException {
		ExecuteContext _localctx = new ExecuteContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_execute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(13);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PREVIEW:
				{
				setState(10);
				previewSQL();
				}
				break;
			case PARSE:
				{
				setState(11);
				parseSQL();
				}
				break;
			case FORMAT:
				{
				setState(12);
				formatSQL();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(15);
				match(SEMI);
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

	public static class PreviewSQLContext extends ParserRuleContext {
		public TerminalNode PREVIEW() { return getToken(AdvancedDistSQLStatementParser.PREVIEW, 0); }
		public SqlContext sql() {
			return getRuleContext(SqlContext.class,0);
		}
		public PreviewSQLContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_previewSQL; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AdvancedDistSQLStatementVisitor ) return ((AdvancedDistSQLStatementVisitor<? extends T>)visitor).visitPreviewSQL(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PreviewSQLContext previewSQL() throws RecognitionException {
		PreviewSQLContext _localctx = new PreviewSQLContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_previewSQL);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(18);
			match(PREVIEW);
			setState(19);
			sql();
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

	public static class ParseSQLContext extends ParserRuleContext {
		public TerminalNode PARSE() { return getToken(AdvancedDistSQLStatementParser.PARSE, 0); }
		public SqlContext sql() {
			return getRuleContext(SqlContext.class,0);
		}
		public ParseSQLContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parseSQL; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AdvancedDistSQLStatementVisitor ) return ((AdvancedDistSQLStatementVisitor<? extends T>)visitor).visitParseSQL(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseSQLContext parseSQL() throws RecognitionException {
		ParseSQLContext _localctx = new ParseSQLContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_parseSQL);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			match(PARSE);
			setState(22);
			sql();
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

	public static class FormatSQLContext extends ParserRuleContext {
		public TerminalNode FORMAT() { return getToken(AdvancedDistSQLStatementParser.FORMAT, 0); }
		public SqlContext sql() {
			return getRuleContext(SqlContext.class,0);
		}
		public FormatSQLContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formatSQL; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AdvancedDistSQLStatementVisitor ) return ((AdvancedDistSQLStatementVisitor<? extends T>)visitor).visitFormatSQL(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormatSQLContext formatSQL() throws RecognitionException {
		FormatSQLContext _localctx = new FormatSQLContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_formatSQL);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24);
			match(FORMAT);
			setState(25);
			sql();
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

	public static class SqlContext extends ParserRuleContext {
		public TerminalNode SQLString() { return getToken(AdvancedDistSQLStatementParser.SQLString, 0); }
		public SqlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sql; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AdvancedDistSQLStatementVisitor ) return ((AdvancedDistSQLStatementVisitor<? extends T>)visitor).visitSql(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SqlContext sql() throws RecognitionException {
		SqlContext _localctx = new SqlContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_sql);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(27);
			match(SQLString);
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\63 \4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\5\2\20\n\2\3\2\5\2\23\n\2\3\3\3"+
		"\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\2\2\7\2\4\6\b\n\2\2\2\35\2"+
		"\17\3\2\2\2\4\24\3\2\2\2\6\27\3\2\2\2\b\32\3\2\2\2\n\35\3\2\2\2\f\20\5"+
		"\4\3\2\r\20\5\6\4\2\16\20\5\b\5\2\17\f\3\2\2\2\17\r\3\2\2\2\17\16\3\2"+
		"\2\2\20\22\3\2\2\2\21\23\7*\2\2\22\21\3\2\2\2\22\23\3\2\2\2\23\3\3\2\2"+
		"\2\24\25\7/\2\2\25\26\5\n\6\2\26\5\3\2\2\2\27\30\7\60\2\2\30\31\5\n\6"+
		"\2\31\7\3\2\2\2\32\33\7\61\2\2\33\34\5\n\6\2\34\t\3\2\2\2\35\36\7\62\2"+
		"\2\36\13\3\2\2\2\4\17\22";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}