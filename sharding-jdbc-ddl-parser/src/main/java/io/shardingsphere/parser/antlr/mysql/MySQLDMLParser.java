// Generated from MySQLDML.g4 by ANTLR 4.7.1
package io.shardingsphere.parser.antlr.mysql;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MySQLDMLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, FIRST=2, AFTER=3, SPATIAL=4, ALGORITHM=5, CHANGE=6, DISCARD=7, 
		CHARACTER=8, CHARSET=9, COLLATE=10, CONVERT=11, SHARED=12, EXCLUSIVE=13, 
		REORGANIZE=14, EXCHANGE=15, BTREE=16, HASH=17, KEY_BLOCK_SIZE=18, PARSER=19, 
		COMMENT=20, AUTO_INCREMENT=21, AVG_ROW_LENGTH=22, CHECKSUM=23, COMPRESSION=24, 
		ZLIB=25, LZ=26, CONNECTION=27, DATA=28, DIRECTORY=29, DELAY_KEY_WRITE=30, 
		ENCRYPTION=31, ENGINE=32, INSERT_METHOD=33, NO=34, LAST=35, MAX_ROWS=36, 
		MIN_ROWS=37, PACK_KEYS=38, PASSWORD=39, ROW_FORMAT=40, DYNAMIC=41, FIXED=42, 
		COMPRESSED=43, REDUNDANT=44, COMPACT=45, STATS_AUTO_RECALC=46, STATS_PERSISTENT=47, 
		STATS_SAMPLE_PAGES=48, STORAGE=49, DISK=50, MEMORY=51, PARTITIONING=52, 
		GENERATED=53, ALWAYS=54, COLUMN_FORMAT=55, VIRTUAL=56, STORED=57, INPLACE=58, 
		SUBPARTITION=59, MAXVALUE=60, LESS=61, THAN=62, DISTINCTROW=63, HIGH_PRIORITY=64, 
		STRAIGHT_JOIN=65, SQL_SMALL_RESULT=66, SQL_BIG_RESULT=67, SQL_BUFFER_RESULT=68, 
		SQL_CACHE=69, SQL_NO_CACHE=70, SQL_CALC_FOUND_ROWS=71, REFERENCES=72, 
		MATCH=73, FULL=74, PARTIAL=75, SIMPLE=76, RESTRICT=77, CASCADE=78, ACTION=79, 
		LINEAR=80, COLUMNS=81, RANGE=82, LIST=83, PARTITIONS=84, SUBPARTITIONS=85, 
		UNSIGNED=86, ZEROFILL=87, OUTFILE=88, DUMPFILE=89, SKIP_=90, OJ=91, LOW_PRIORITY=92, 
		DELAYED=93, AND_SYM=94, OR_SYM=95, NOT_SYM=96, UNARY_BIT_COMPLEMENT=97, 
		BIT_INCLUSIVE_OR=98, BIT_AND=99, SIGNED_LEFT_SHIFT=100, SIGNED_RIGHT_SHIFT=101, 
		BIT_EXCLUSIVE_OR=102, MOD_SYM=103, PLUS=104, MINUS=105, ASTERISK=106, 
		SLASH=107, DOT=108, SAFE_EQ=109, EQ=110, EQ_OR_ASSIGN=111, NEQ=112, NEQ_SYM=113, 
		GT=114, GTE=115, LT=116, LTE=117, LEFT_PAREN=118, RIGHT_PAREN=119, LEFT_BRACE=120, 
		RIGHT_BRACE=121, COMMA=122, DOUBLE_QUOTA=123, SINGLE_QUOTA=124, BACK_QUOTA=125, 
		UL_=126, QUESTION=127, STRING=128, NUMBER=129, INT_=130, EXP=131, HEX_DIGIT=132, 
		BIT_NUM=133, WS=134, SELECT=135, ALL=136, ANY=137, DISTINCT=138, FROM=139, 
		PARTITION=140, WHERE=141, GROUP=142, BY=143, ASC=144, DESC=145, WITH=146, 
		RECURSIVE=147, ROLLUP=148, HAVING=149, WINDOW=150, AS=151, ORDER=152, 
		LIMIT=153, OFFSET=154, INTO=155, ALTER=156, CREATE=157, TEMPORARY=158, 
		TABLE=159, COLUMN=160, ADD=161, DROP=162, ENABLE=163, DISABLE=164, CONSTRAINT=165, 
		UNIQUE=166, FULLTEXT=167, FOREIGN=168, NONE=169, MODIFY=170, RENAME=171, 
		VALIDATION=172, IMPORT_=173, TABLESPACE=174, TRUNCATE=175, ANALYZE=176, 
		CHECK=177, OPTIMIZE=178, REBUILD=179, REPAIR=180, REMOVE=181, UPGRADE=182, 
		TO=183, COPY=184, PRIMARY=185, KEYS=186, WITHOUT=187, COALESCE=188, SET=189, 
		FOR=190, UPDATE=191, SHARE=192, OF=193, NOWAIT=194, LOCKED=195, LOCK=196, 
		IN=197, MODE=198, INNER=199, CROSS=200, JOIN=201, ON=202, LEFT=203, RIGHT=204, 
		OUTER=205, NATURAL=206, USING=207, USE=208, INDEX=209, KEY=210, IGNORE=211, 
		FORCE=212, UNION=213, DEFAULT=214, DELETE=215, QUICK=216, INSERT=217, 
		VALUES=218, VALUE=219, DUPLICATE=220, EXISTS=221, IS=222, AND=223, OR=224, 
		XOR=225, NOT=226, BETWEEN=227, NULL=228, TRUE=229, FALSE=230, UNKNOWN=231, 
		SOUNDS=232, LIKE=233, DIV=234, MOD=235, ROW=236, ESCAPE=237, REGEXP=238, 
		CASE=239, WHEN=240, THEN=241, IF=242, ELSE=243, END=244, BIT=245, TINYINT=246, 
		SMALLINT=247, MEDIUMINT=248, INT=249, INTEGER=250, BIGINT=251, REAL=252, 
		DOUBLE=253, FLOAT=254, DECIMAL=255, NUMERIC=256, DATE=257, TIME=258, TIMESTAMP=259, 
		CURRENT_TIMESTAMP=260, DATETIME=261, YEAR=262, CHAR=263, VARCHAR=264, 
		BINARY=265, VARBINARY=266, TINYBLOB=267, BLOB=268, MEDIUMBLOB=269, LONGBLOB=270, 
		TINYTEXT=271, TEXT=272, MEDIUMTEXT=273, LONGTEXT=274, ENUM=275, JSON=276, 
		REPLACE=277, ID=278;
	public static final int
		RULE_selectSpec = 0, RULE_caseExpress = 1, RULE_caseComp = 2, RULE_caseWhenComp = 3, 
		RULE_caseCond = 4, RULE_whenResult = 5, RULE_elseResult = 6, RULE_caseResult = 7, 
		RULE_selectExpr = 8, RULE_deleteClause = 9, RULE_fromSingle = 10, RULE_fromMulti = 11, 
		RULE_deleteSpec = 12, RULE_insert = 13, RULE_insertClause = 14, RULE_insertSpec = 15, 
		RULE_columnClause = 16, RULE_valueClause = 17, RULE_setClause = 18, RULE_onDuplicateClause = 19, 
		RULE_itemListWithEmpty = 20, RULE_assignmentList = 21, RULE_assignment = 22, 
		RULE_updateClause = 23, RULE_updateSpec = 24, RULE_schemaName = 25, RULE_tableName = 26, 
		RULE_columnName = 27, RULE_tablespaceName = 28, RULE_collationName = 29, 
		RULE_indexName = 30, RULE_alias = 31, RULE_cteName = 32, RULE_idList = 33, 
		RULE_rangeClause = 34, RULE_columnList = 35, RULE_idListWithEmpty = 36, 
		RULE_tableReferences = 37, RULE_tableReference = 38, RULE_tableFactor = 39, 
		RULE_joinTable = 40, RULE_joinCondition = 41, RULE_indexHintList = 42, 
		RULE_indexHint = 43, RULE_expr = 44, RULE_booleanPrimary = 45, RULE_comparisonOperator = 46, 
		RULE_predicate = 47, RULE_bitExpr = 48, RULE_simpleExpr = 49, RULE_liter = 50, 
		RULE_characterAndCollate = 51, RULE_characterSet = 52, RULE_collateClause = 53, 
		RULE_charsetName = 54, RULE_characterAndCollateWithEqual = 55, RULE_characterSetWithEqual = 56, 
		RULE_collateClauseWithEqual = 57, RULE_select = 58, RULE_withClause = 59, 
		RULE_cteClause = 60, RULE_unionSelect = 61, RULE_selectExpression = 62, 
		RULE_selectClause = 63, RULE_fromClause = 64, RULE_whereClause = 65, RULE_groupByClause = 66, 
		RULE_havingClause = 67, RULE_orderByClause = 68, RULE_groupByItem = 69, 
		RULE_limitClause = 70, RULE_partitionClause = 71, RULE_functionCall = 72, 
		RULE_selectExprs = 73, RULE_subquery = 74, RULE_execute = 75, RULE_delete = 76, 
		RULE_update = 77, RULE_value = 78, RULE_valueList = 79, RULE_valueListWithParen = 80;
	public static final String[] ruleNames = {
		"selectSpec", "caseExpress", "caseComp", "caseWhenComp", "caseCond", "whenResult", 
		"elseResult", "caseResult", "selectExpr", "deleteClause", "fromSingle", 
		"fromMulti", "deleteSpec", "insert", "insertClause", "insertSpec", "columnClause", 
		"valueClause", "setClause", "onDuplicateClause", "itemListWithEmpty", 
		"assignmentList", "assignment", "updateClause", "updateSpec", "schemaName", 
		"tableName", "columnName", "tablespaceName", "collationName", "indexName", 
		"alias", "cteName", "idList", "rangeClause", "columnList", "idListWithEmpty", 
		"tableReferences", "tableReference", "tableFactor", "joinTable", "joinCondition", 
		"indexHintList", "indexHint", "expr", "booleanPrimary", "comparisonOperator", 
		"predicate", "bitExpr", "simpleExpr", "liter", "characterAndCollate", 
		"characterSet", "collateClause", "charsetName", "characterAndCollateWithEqual", 
		"characterSetWithEqual", "collateClauseWithEqual", "select", "withClause", 
		"cteClause", "unionSelect", "selectExpression", "selectClause", "fromClause", 
		"whereClause", "groupByClause", "havingClause", "orderByClause", "groupByItem", 
		"limitClause", "partitionClause", "functionCall", "selectExprs", "subquery", 
		"execute", "delete", "update", "value", "valueList", "valueListWithParen"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'.*'", null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, "'&&'", "'||'", 
		"'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", "'%'", "'+'", "'-'", 
		"'*'", "'/'", "'.'", "'<=>'", "'=='", "'='", "'!='", "'<>'", "'>'", "'>='", 
		"'<'", "'<='", "'('", "')'", "'{'", "'}'", "','", "'\"'", "'''", "'`'", 
		"'_'", "'?'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "FIRST", "AFTER", "SPATIAL", "ALGORITHM", "CHANGE", "DISCARD", 
		"CHARACTER", "CHARSET", "COLLATE", "CONVERT", "SHARED", "EXCLUSIVE", "REORGANIZE", 
		"EXCHANGE", "BTREE", "HASH", "KEY_BLOCK_SIZE", "PARSER", "COMMENT", "AUTO_INCREMENT", 
		"AVG_ROW_LENGTH", "CHECKSUM", "COMPRESSION", "ZLIB", "LZ", "CONNECTION", 
		"DATA", "DIRECTORY", "DELAY_KEY_WRITE", "ENCRYPTION", "ENGINE", "INSERT_METHOD", 
		"NO", "LAST", "MAX_ROWS", "MIN_ROWS", "PACK_KEYS", "PASSWORD", "ROW_FORMAT", 
		"DYNAMIC", "FIXED", "COMPRESSED", "REDUNDANT", "COMPACT", "STATS_AUTO_RECALC", 
		"STATS_PERSISTENT", "STATS_SAMPLE_PAGES", "STORAGE", "DISK", "MEMORY", 
		"PARTITIONING", "GENERATED", "ALWAYS", "COLUMN_FORMAT", "VIRTUAL", "STORED", 
		"INPLACE", "SUBPARTITION", "MAXVALUE", "LESS", "THAN", "DISTINCTROW", 
		"HIGH_PRIORITY", "STRAIGHT_JOIN", "SQL_SMALL_RESULT", "SQL_BIG_RESULT", 
		"SQL_BUFFER_RESULT", "SQL_CACHE", "SQL_NO_CACHE", "SQL_CALC_FOUND_ROWS", 
		"REFERENCES", "MATCH", "FULL", "PARTIAL", "SIMPLE", "RESTRICT", "CASCADE", 
		"ACTION", "LINEAR", "COLUMNS", "RANGE", "LIST", "PARTITIONS", "SUBPARTITIONS", 
		"UNSIGNED", "ZEROFILL", "OUTFILE", "DUMPFILE", "SKIP_", "OJ", "LOW_PRIORITY", 
		"DELAYED", "AND_SYM", "OR_SYM", "NOT_SYM", "UNARY_BIT_COMPLEMENT", "BIT_INCLUSIVE_OR", 
		"BIT_AND", "SIGNED_LEFT_SHIFT", "SIGNED_RIGHT_SHIFT", "BIT_EXCLUSIVE_OR", 
		"MOD_SYM", "PLUS", "MINUS", "ASTERISK", "SLASH", "DOT", "SAFE_EQ", "EQ", 
		"EQ_OR_ASSIGN", "NEQ", "NEQ_SYM", "GT", "GTE", "LT", "LTE", "LEFT_PAREN", 
		"RIGHT_PAREN", "LEFT_BRACE", "RIGHT_BRACE", "COMMA", "DOUBLE_QUOTA", "SINGLE_QUOTA", 
		"BACK_QUOTA", "UL_", "QUESTION", "STRING", "NUMBER", "INT_", "EXP", "HEX_DIGIT", 
		"BIT_NUM", "WS", "SELECT", "ALL", "ANY", "DISTINCT", "FROM", "PARTITION", 
		"WHERE", "GROUP", "BY", "ASC", "DESC", "WITH", "RECURSIVE", "ROLLUP", 
		"HAVING", "WINDOW", "AS", "ORDER", "LIMIT", "OFFSET", "INTO", "ALTER", 
		"CREATE", "TEMPORARY", "TABLE", "COLUMN", "ADD", "DROP", "ENABLE", "DISABLE", 
		"CONSTRAINT", "UNIQUE", "FULLTEXT", "FOREIGN", "NONE", "MODIFY", "RENAME", 
		"VALIDATION", "IMPORT_", "TABLESPACE", "TRUNCATE", "ANALYZE", "CHECK", 
		"OPTIMIZE", "REBUILD", "REPAIR", "REMOVE", "UPGRADE", "TO", "COPY", "PRIMARY", 
		"KEYS", "WITHOUT", "COALESCE", "SET", "FOR", "UPDATE", "SHARE", "OF", 
		"NOWAIT", "LOCKED", "LOCK", "IN", "MODE", "INNER", "CROSS", "JOIN", "ON", 
		"LEFT", "RIGHT", "OUTER", "NATURAL", "USING", "USE", "INDEX", "KEY", "IGNORE", 
		"FORCE", "UNION", "DEFAULT", "DELETE", "QUICK", "INSERT", "VALUES", "VALUE", 
		"DUPLICATE", "EXISTS", "IS", "AND", "OR", "XOR", "NOT", "BETWEEN", "NULL", 
		"TRUE", "FALSE", "UNKNOWN", "SOUNDS", "LIKE", "DIV", "MOD", "ROW", "ESCAPE", 
		"REGEXP", "CASE", "WHEN", "THEN", "IF", "ELSE", "END", "BIT", "TINYINT", 
		"SMALLINT", "MEDIUMINT", "INT", "INTEGER", "BIGINT", "REAL", "DOUBLE", 
		"FLOAT", "DECIMAL", "NUMERIC", "DATE", "TIME", "TIMESTAMP", "CURRENT_TIMESTAMP", 
		"DATETIME", "YEAR", "CHAR", "VARCHAR", "BINARY", "VARBINARY", "TINYBLOB", 
		"BLOB", "MEDIUMBLOB", "LONGBLOB", "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT", 
		"ENUM", "JSON", "REPLACE", "ID"
	};
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
	public String getGrammarFileName() { return "MySQLDML.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MySQLDMLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class SelectSpecContext extends ParserRuleContext {
		public TerminalNode HIGH_PRIORITY() { return getToken(MySQLDMLParser.HIGH_PRIORITY, 0); }
		public TerminalNode STRAIGHT_JOIN() { return getToken(MySQLDMLParser.STRAIGHT_JOIN, 0); }
		public TerminalNode SQL_SMALL_RESULT() { return getToken(MySQLDMLParser.SQL_SMALL_RESULT, 0); }
		public TerminalNode SQL_BIG_RESULT() { return getToken(MySQLDMLParser.SQL_BIG_RESULT, 0); }
		public TerminalNode SQL_BUFFER_RESULT() { return getToken(MySQLDMLParser.SQL_BUFFER_RESULT, 0); }
		public TerminalNode SQL_CALC_FOUND_ROWS() { return getToken(MySQLDMLParser.SQL_CALC_FOUND_ROWS, 0); }
		public TerminalNode ALL() { return getToken(MySQLDMLParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(MySQLDMLParser.DISTINCT, 0); }
		public TerminalNode DISTINCTROW() { return getToken(MySQLDMLParser.DISTINCTROW, 0); }
		public TerminalNode SQL_CACHE() { return getToken(MySQLDMLParser.SQL_CACHE, 0); }
		public TerminalNode SQL_NO_CACHE() { return getToken(MySQLDMLParser.SQL_NO_CACHE, 0); }
		public SelectSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectSpec; }
	}

	public final SelectSpecContext selectSpec() throws RecognitionException {
		SelectSpecContext _localctx = new SelectSpecContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_selectSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(162);
				_la = _input.LA(1);
				if ( !(_la==DISTINCTROW || _la==ALL || _la==DISTINCT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			setState(166);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(165);
				match(HIGH_PRIORITY);
				}
				break;
			}
			setState(169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(168);
				match(STRAIGHT_JOIN);
				}
				break;
			}
			setState(172);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(171);
				match(SQL_SMALL_RESULT);
				}
				break;
			}
			setState(175);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(174);
				match(SQL_BIG_RESULT);
				}
				break;
			}
			setState(178);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(177);
				match(SQL_BUFFER_RESULT);
				}
				break;
			}
			setState(181);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(180);
				_la = _input.LA(1);
				if ( !(_la==SQL_CACHE || _la==SQL_NO_CACHE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			setState(184);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(183);
				match(SQL_CALC_FOUND_ROWS);
				}
				break;
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

	public static class CaseExpressContext extends ParserRuleContext {
		public CaseCondContext caseCond() {
			return getRuleContext(CaseCondContext.class,0);
		}
		public CaseCompContext caseComp() {
			return getRuleContext(CaseCompContext.class,0);
		}
		public CaseExpressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseExpress; }
	}

	public final CaseExpressContext caseExpress() throws RecognitionException {
		CaseExpressContext _localctx = new CaseExpressContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_caseExpress);
		try {
			setState(188);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(186);
				caseCond();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(187);
				caseComp();
				}
				break;
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

	public static class CaseCompContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(MySQLDMLParser.CASE, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public TerminalNode END() { return getToken(MySQLDMLParser.END, 0); }
		public List<CaseWhenCompContext> caseWhenComp() {
			return getRuleContexts(CaseWhenCompContext.class);
		}
		public CaseWhenCompContext caseWhenComp(int i) {
			return getRuleContext(CaseWhenCompContext.class,i);
		}
		public ElseResultContext elseResult() {
			return getRuleContext(ElseResultContext.class,0);
		}
		public CaseCompContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseComp; }
	}

	public final CaseCompContext caseComp() throws RecognitionException {
		CaseCompContext _localctx = new CaseCompContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_caseComp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			match(CASE);
			setState(191);
			simpleExpr(0);
			setState(193); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(192);
				caseWhenComp();
				}
				}
				setState(195); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(197);
				elseResult();
				}
			}

			setState(200);
			match(END);
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

	public static class CaseWhenCompContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(MySQLDMLParser.WHEN, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public TerminalNode THEN() { return getToken(MySQLDMLParser.THEN, 0); }
		public CaseResultContext caseResult() {
			return getRuleContext(CaseResultContext.class,0);
		}
		public CaseWhenCompContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhenComp; }
	}

	public final CaseWhenCompContext caseWhenComp() throws RecognitionException {
		CaseWhenCompContext _localctx = new CaseWhenCompContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_caseWhenComp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			match(WHEN);
			setState(203);
			simpleExpr(0);
			setState(204);
			match(THEN);
			setState(205);
			caseResult();
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

	public static class CaseCondContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(MySQLDMLParser.CASE, 0); }
		public TerminalNode END() { return getToken(MySQLDMLParser.END, 0); }
		public List<WhenResultContext> whenResult() {
			return getRuleContexts(WhenResultContext.class);
		}
		public WhenResultContext whenResult(int i) {
			return getRuleContext(WhenResultContext.class,i);
		}
		public ElseResultContext elseResult() {
			return getRuleContext(ElseResultContext.class,0);
		}
		public CaseCondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseCond; }
	}

	public final CaseCondContext caseCond() throws RecognitionException {
		CaseCondContext _localctx = new CaseCondContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_caseCond);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			match(CASE);
			setState(209); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(208);
				whenResult();
				}
				}
				setState(211); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(213);
				elseResult();
				}
			}

			setState(216);
			match(END);
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

	public static class WhenResultContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(MySQLDMLParser.WHEN, 0); }
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public TerminalNode THEN() { return getToken(MySQLDMLParser.THEN, 0); }
		public CaseResultContext caseResult() {
			return getRuleContext(CaseResultContext.class,0);
		}
		public WhenResultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenResult; }
	}

	public final WhenResultContext whenResult() throws RecognitionException {
		WhenResultContext _localctx = new WhenResultContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_whenResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			match(WHEN);
			setState(219);
			booleanPrimary(0);
			setState(220);
			match(THEN);
			setState(221);
			caseResult();
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

	public static class ElseResultContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(MySQLDMLParser.ELSE, 0); }
		public CaseResultContext caseResult() {
			return getRuleContext(CaseResultContext.class,0);
		}
		public ElseResultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elseResult; }
	}

	public final ElseResultContext elseResult() throws RecognitionException {
		ElseResultContext _localctx = new ElseResultContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_elseResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(ELSE);
			setState(224);
			caseResult();
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

	public static class CaseResultContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public CaseResultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseResult; }
	}

	public final CaseResultContext caseResult() throws RecognitionException {
		CaseResultContext _localctx = new CaseResultContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_caseResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			expr(0);
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

	public static class SelectExprContext extends ParserRuleContext {
		public BitExprContext bitExpr() {
			return getRuleContext(BitExprContext.class,0);
		}
		public CaseExpressContext caseExpress() {
			return getRuleContext(CaseExpressContext.class,0);
		}
		public TerminalNode AS() { return getToken(MySQLDMLParser.AS, 0); }
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public SelectExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectExpr; }
	}

	public final SelectExprContext selectExpr() throws RecognitionException {
		SelectExprContext _localctx = new SelectExprContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_selectExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(228);
				bitExpr(0);
				}
				break;
			case 2:
				{
				setState(229);
				caseExpress();
				}
				break;
			}
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(232);
				match(AS);
				}
			}

			setState(236);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(235);
				alias();
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

	public static class DeleteClauseContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(MySQLDMLParser.DELETE, 0); }
		public DeleteSpecContext deleteSpec() {
			return getRuleContext(DeleteSpecContext.class,0);
		}
		public FromMultiContext fromMulti() {
			return getRuleContext(FromMultiContext.class,0);
		}
		public FromSingleContext fromSingle() {
			return getRuleContext(FromSingleContext.class,0);
		}
		public DeleteClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteClause; }
	}

	public final DeleteClauseContext deleteClause() throws RecognitionException {
		DeleteClauseContext _localctx = new DeleteClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_deleteClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			match(DELETE);
			setState(239);
			deleteSpec();
			setState(242);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(240);
				fromMulti();
				}
				break;
			case 2:
				{
				setState(241);
				fromSingle();
				}
				break;
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

	public static class FromSingleContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(MySQLDMLParser.FROM, 0); }
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public PartitionClauseContext partitionClause() {
			return getRuleContext(PartitionClauseContext.class,0);
		}
		public FromSingleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromSingle; }
	}

	public final FromSingleContext fromSingle() throws RecognitionException {
		FromSingleContext _localctx = new FromSingleContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_fromSingle);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			match(FROM);
			setState(245);
			match(ID);
			setState(247);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(246);
				partitionClause();
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

	public static class FromMultiContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(MySQLDMLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MySQLDMLParser.ID, i);
		}
		public TerminalNode FROM() { return getToken(MySQLDMLParser.FROM, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public TerminalNode USING() { return getToken(MySQLDMLParser.USING, 0); }
		public FromMultiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromMulti; }
	}

	public final FromMultiContext fromMulti() throws RecognitionException {
		FromMultiContext _localctx = new FromMultiContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_fromMulti);
		int _la;
		try {
			setState(282);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(249);
				match(ID);
				setState(251);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(250);
					match(T__0);
					}
				}

				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(253);
					match(COMMA);
					setState(254);
					match(ID);
					setState(256);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==T__0) {
						{
						setState(255);
						match(T__0);
						}
					}

					}
					}
					setState(262);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(263);
				match(FROM);
				setState(264);
				tableReferences();
				}
				}
				break;
			case FROM:
				enterOuterAlt(_localctx, 2);
				{
				setState(265);
				match(FROM);
				{
				setState(266);
				match(ID);
				setState(268);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(267);
					match(T__0);
					}
				}

				setState(277);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(270);
					match(COMMA);
					setState(271);
					match(ID);
					setState(273);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==T__0) {
						{
						setState(272);
						match(T__0);
						}
					}

					}
					}
					setState(279);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(280);
				match(USING);
				setState(281);
				tableReferences();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class DeleteSpecContext extends ParserRuleContext {
		public TerminalNode LOW_PRIORITY() { return getToken(MySQLDMLParser.LOW_PRIORITY, 0); }
		public TerminalNode QUICK() { return getToken(MySQLDMLParser.QUICK, 0); }
		public TerminalNode IGNORE() { return getToken(MySQLDMLParser.IGNORE, 0); }
		public DeleteSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteSpec; }
	}

	public final DeleteSpecContext deleteSpec() throws RecognitionException {
		DeleteSpecContext _localctx = new DeleteSpecContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_deleteSpec);
		int _la;
		try {
			setState(293);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(285);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LOW_PRIORITY) {
					{
					setState(284);
					match(LOW_PRIORITY);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUICK) {
					{
					setState(287);
					match(QUICK);
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(291);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IGNORE) {
					{
					setState(290);
					match(IGNORE);
					}
				}

				}
				break;
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

	public static class InsertContext extends ParserRuleContext {
		public InsertClauseContext insertClause() {
			return getRuleContext(InsertClauseContext.class,0);
		}
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public SetClauseContext setClause() {
			return getRuleContext(SetClauseContext.class,0);
		}
		public ColumnClauseContext columnClause() {
			return getRuleContext(ColumnClauseContext.class,0);
		}
		public TerminalNode INTO() { return getToken(MySQLDMLParser.INTO, 0); }
		public PartitionClauseContext partitionClause() {
			return getRuleContext(PartitionClauseContext.class,0);
		}
		public OnDuplicateClauseContext onDuplicateClause() {
			return getRuleContext(OnDuplicateClauseContext.class,0);
		}
		public InsertContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insert; }
	}

	public final InsertContext insert() throws RecognitionException {
		InsertContext _localctx = new InsertContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_insert);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(295);
			insertClause();
			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTO) {
				{
				setState(296);
				match(INTO);
				}
			}

			setState(299);
			match(ID);
			setState(301);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(300);
				partitionClause();
				}
			}

			setState(305);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SET:
				{
				setState(303);
				setClause();
				}
				break;
			case LEFT_PAREN:
			case SELECT:
			case WITH:
			case VALUES:
			case VALUE:
				{
				setState(304);
				columnClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(308);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON) {
				{
				setState(307);
				onDuplicateClause();
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

	public static class InsertClauseContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(MySQLDMLParser.INSERT, 0); }
		public InsertSpecContext insertSpec() {
			return getRuleContext(InsertSpecContext.class,0);
		}
		public InsertClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertClause; }
	}

	public final InsertClauseContext insertClause() throws RecognitionException {
		InsertClauseContext _localctx = new InsertClauseContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_insertClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			match(INSERT);
			setState(312);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (HIGH_PRIORITY - 64)) | (1L << (LOW_PRIORITY - 64)) | (1L << (DELAYED - 64)))) != 0)) {
				{
				setState(311);
				insertSpec();
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

	public static class InsertSpecContext extends ParserRuleContext {
		public TerminalNode LOW_PRIORITY() { return getToken(MySQLDMLParser.LOW_PRIORITY, 0); }
		public TerminalNode DELAYED() { return getToken(MySQLDMLParser.DELAYED, 0); }
		public TerminalNode HIGH_PRIORITY() { return getToken(MySQLDMLParser.HIGH_PRIORITY, 0); }
		public TerminalNode IGNORE() { return getToken(MySQLDMLParser.IGNORE, 0); }
		public InsertSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertSpec; }
	}

	public final InsertSpecContext insertSpec() throws RecognitionException {
		InsertSpecContext _localctx = new InsertSpecContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_insertSpec);
		try {
			setState(318);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LOW_PRIORITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(314);
				match(LOW_PRIORITY);
				}
				break;
			case DELAYED:
				enterOuterAlt(_localctx, 2);
				{
				setState(315);
				match(DELAYED);
				}
				break;
			case HIGH_PRIORITY:
				enterOuterAlt(_localctx, 3);
				{
				setState(316);
				match(HIGH_PRIORITY);
				setState(317);
				match(IGNORE);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class ColumnClauseContext extends ParserRuleContext {
		public ValueClauseContext valueClause() {
			return getRuleContext(ValueClauseContext.class,0);
		}
		public SelectContext select() {
			return getRuleContext(SelectContext.class,0);
		}
		public IdListWithEmptyContext idListWithEmpty() {
			return getRuleContext(IdListWithEmptyContext.class,0);
		}
		public ColumnClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnClause; }
	}

	public final ColumnClauseContext columnClause() throws RecognitionException {
		ColumnClauseContext _localctx = new ColumnClauseContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_columnClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(320);
				idListWithEmpty();
				}
			}

			setState(325);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUES:
			case VALUE:
				{
				setState(323);
				valueClause();
				}
				break;
			case SELECT:
			case WITH:
				{
				setState(324);
				select();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class ValueClauseContext extends ParserRuleContext {
		public List<ValueListWithParenContext> valueListWithParen() {
			return getRuleContexts(ValueListWithParenContext.class);
		}
		public ValueListWithParenContext valueListWithParen(int i) {
			return getRuleContext(ValueListWithParenContext.class,i);
		}
		public TerminalNode VALUES() { return getToken(MySQLDMLParser.VALUES, 0); }
		public TerminalNode VALUE() { return getToken(MySQLDMLParser.VALUE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public ValueClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueClause; }
	}

	public final ValueClauseContext valueClause() throws RecognitionException {
		ValueClauseContext _localctx = new ValueClauseContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_valueClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			_la = _input.LA(1);
			if ( !(_la==VALUES || _la==VALUE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(328);
			valueListWithParen();
			setState(333);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(329);
				match(COMMA);
				setState(330);
				valueListWithParen();
				}
				}
				setState(335);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class SetClauseContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(MySQLDMLParser.SET, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public SetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setClause; }
	}

	public final SetClauseContext setClause() throws RecognitionException {
		SetClauseContext _localctx = new SetClauseContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_setClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
			match(SET);
			setState(337);
			assignmentList();
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

	public static class OnDuplicateClauseContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(MySQLDMLParser.ON, 0); }
		public TerminalNode DUPLICATE() { return getToken(MySQLDMLParser.DUPLICATE, 0); }
		public TerminalNode KEY() { return getToken(MySQLDMLParser.KEY, 0); }
		public TerminalNode UPDATE() { return getToken(MySQLDMLParser.UPDATE, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public OnDuplicateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_onDuplicateClause; }
	}

	public final OnDuplicateClauseContext onDuplicateClause() throws RecognitionException {
		OnDuplicateClauseContext _localctx = new OnDuplicateClauseContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_onDuplicateClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(339);
			match(ON);
			setState(340);
			match(DUPLICATE);
			setState(341);
			match(KEY);
			setState(342);
			match(UPDATE);
			setState(343);
			assignmentList();
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

	public static class ItemListWithEmptyContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public ItemListWithEmptyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_itemListWithEmpty; }
	}

	public final ItemListWithEmptyContext itemListWithEmpty() throws RecognitionException {
		ItemListWithEmptyContext _localctx = new ItemListWithEmptyContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_itemListWithEmpty);
		try {
			setState(348);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(345);
				match(LEFT_PAREN);
				setState(346);
				match(RIGHT_PAREN);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(347);
				idList();
				}
				break;
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

	public static class AssignmentListContext extends ParserRuleContext {
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public AssignmentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentList; }
	}

	public final AssignmentListContext assignmentList() throws RecognitionException {
		AssignmentListContext _localctx = new AssignmentListContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(350);
			assignment();
			setState(355);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(351);
				match(COMMA);
				setState(352);
				assignment();
				}
				}
				setState(357);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class AssignmentContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDMLParser.EQ_OR_ASSIGN, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(358);
			columnName();
			setState(359);
			match(EQ_OR_ASSIGN);
			setState(360);
			value();
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

	public static class UpdateClauseContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(MySQLDMLParser.UPDATE, 0); }
		public UpdateSpecContext updateSpec() {
			return getRuleContext(UpdateSpecContext.class,0);
		}
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public UpdateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateClause; }
	}

	public final UpdateClauseContext updateClause() throws RecognitionException {
		UpdateClauseContext _localctx = new UpdateClauseContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_updateClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(362);
			match(UPDATE);
			setState(363);
			updateSpec();
			setState(364);
			tableReferences();
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

	public static class UpdateSpecContext extends ParserRuleContext {
		public TerminalNode LOW_PRIORITY() { return getToken(MySQLDMLParser.LOW_PRIORITY, 0); }
		public TerminalNode IGNORE() { return getToken(MySQLDMLParser.IGNORE, 0); }
		public UpdateSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateSpec; }
	}

	public final UpdateSpecContext updateSpec() throws RecognitionException {
		UpdateSpecContext _localctx = new UpdateSpecContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_updateSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(367);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOW_PRIORITY) {
				{
				setState(366);
				match(LOW_PRIORITY);
				}
			}

			setState(370);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IGNORE) {
				{
				setState(369);
				match(IGNORE);
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

	public static class SchemaNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public SchemaNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaName; }
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(372);
			match(ID);
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

	public static class TableNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			match(ID);
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

	public static class ColumnNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(376);
			match(ID);
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

	public static class TablespaceNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TablespaceNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablespaceName; }
	}

	public final TablespaceNameContext tablespaceName() throws RecognitionException {
		TablespaceNameContext _localctx = new TablespaceNameContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_tablespaceName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(378);
			match(ID);
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

	public static class CollationNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public CollationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collationName; }
	}

	public final CollationNameContext collationName() throws RecognitionException {
		CollationNameContext _localctx = new CollationNameContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_collationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(380);
			match(ID);
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

	public static class IndexNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public IndexNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexName; }
	}

	public final IndexNameContext indexName() throws RecognitionException {
		IndexNameContext _localctx = new IndexNameContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(382);
			match(ID);
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

	public static class AliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(384);
			match(ID);
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

	public static class CteNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public CteNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cteName; }
	}

	public final CteNameContext cteName() throws RecognitionException {
		CteNameContext _localctx = new CteNameContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_cteName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			match(ID);
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

	public static class IdListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public List<TerminalNode> ID() { return getTokens(MySQLDMLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MySQLDMLParser.ID, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public IdListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idList; }
	}

	public final IdListContext idList() throws RecognitionException {
		IdListContext _localctx = new IdListContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_idList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			match(LEFT_PAREN);
			setState(389);
			match(ID);
			setState(394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(390);
				match(COMMA);
				setState(391);
				match(ID);
				}
				}
				setState(396);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(397);
			match(RIGHT_PAREN);
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

	public static class RangeClauseContext extends ParserRuleContext {
		public List<TerminalNode> NUMBER() { return getTokens(MySQLDMLParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(MySQLDMLParser.NUMBER, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public TerminalNode OFFSET() { return getToken(MySQLDMLParser.OFFSET, 0); }
		public RangeClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeClause; }
	}

	public final RangeClauseContext rangeClause() throws RecognitionException {
		RangeClauseContext _localctx = new RangeClauseContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_rangeClause);
		int _la;
		try {
			setState(410);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(399);
				match(NUMBER);
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(400);
					match(COMMA);
					setState(401);
					match(NUMBER);
					}
					}
					setState(406);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(407);
				match(NUMBER);
				setState(408);
				match(OFFSET);
				setState(409);
				match(NUMBER);
				}
				break;
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

	public static class ColumnListContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412);
			match(LEFT_PAREN);
			setState(413);
			columnName();
			setState(418);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(414);
				match(COMMA);
				setState(415);
				columnName();
				}
				}
				setState(420);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(421);
			match(RIGHT_PAREN);
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

	public static class IdListWithEmptyContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public IdListWithEmptyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idListWithEmpty; }
	}

	public final IdListWithEmptyContext idListWithEmpty() throws RecognitionException {
		IdListWithEmptyContext _localctx = new IdListWithEmptyContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_idListWithEmpty);
		try {
			setState(426);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(423);
				match(LEFT_PAREN);
				setState(424);
				match(RIGHT_PAREN);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(425);
				idList();
				}
				break;
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

	public static class TableReferencesContext extends ParserRuleContext {
		public List<TableReferenceContext> tableReference() {
			return getRuleContexts(TableReferenceContext.class);
		}
		public TableReferenceContext tableReference(int i) {
			return getRuleContext(TableReferenceContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public TableReferencesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReferences; }
	}

	public final TableReferencesContext tableReferences() throws RecognitionException {
		TableReferencesContext _localctx = new TableReferencesContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_tableReferences);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
			tableReference();
			setState(433);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(429);
				match(COMMA);
				setState(430);
				tableReference();
				}
				}
				setState(435);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class TableReferenceContext extends ParserRuleContext {
		public List<TableFactorContext> tableFactor() {
			return getRuleContexts(TableFactorContext.class);
		}
		public TableFactorContext tableFactor(int i) {
			return getRuleContext(TableFactorContext.class,i);
		}
		public List<JoinTableContext> joinTable() {
			return getRuleContexts(JoinTableContext.class);
		}
		public JoinTableContext joinTable(int i) {
			return getRuleContext(JoinTableContext.class,i);
		}
		public TableReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReference; }
	}

	public final TableReferenceContext tableReference() throws RecognitionException {
		TableReferenceContext _localctx = new TableReferenceContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_tableReference);
		int _la;
		try {
			setState(450);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(439); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(436);
					tableFactor();
					setState(437);
					joinTable();
					}
					}
					setState(441); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==LEFT_PAREN || _la==ID );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(443);
				tableFactor();
				setState(445); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(444);
					joinTable();
					}
					}
					setState(447); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==STRAIGHT_JOIN || ((((_la - 199)) & ~0x3f) == 0 && ((1L << (_la - 199)) & ((1L << (INNER - 199)) | (1L << (CROSS - 199)) | (1L << (JOIN - 199)) | (1L << (LEFT - 199)) | (1L << (RIGHT - 199)) | (1L << (NATURAL - 199)))) != 0) );
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(449);
				tableFactor();
				}
				break;
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

	public static class TableFactorContext extends ParserRuleContext {
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode PARTITION() { return getToken(MySQLDMLParser.PARTITION, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public IndexHintListContext indexHintList() {
			return getRuleContext(IndexHintListContext.class,0);
		}
		public TerminalNode AS() { return getToken(MySQLDMLParser.AS, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public TableFactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableFactor; }
	}

	public final TableFactorContext tableFactor() throws RecognitionException {
		TableFactorContext _localctx = new TableFactorContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_tableFactor);
		int _la;
		try {
			setState(476);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(452);
				tableName();
				setState(455);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(453);
					match(PARTITION);
					setState(454);
					idList();
					}
				}

				setState(461);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
				case 1:
					{
					setState(458);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(457);
						match(AS);
						}
					}

					setState(460);
					alias();
					}
					break;
				}
				setState(464);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USE || _la==IGNORE) {
					{
					setState(463);
					indexHintList();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(466);
				subquery();
				setState(468);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(467);
					match(AS);
					}
				}

				setState(470);
				alias();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(472);
				match(LEFT_PAREN);
				setState(473);
				tableReferences();
				setState(474);
				match(RIGHT_PAREN);
				}
				break;
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

	public static class JoinTableContext extends ParserRuleContext {
		public TerminalNode JOIN() { return getToken(MySQLDMLParser.JOIN, 0); }
		public TableFactorContext tableFactor() {
			return getRuleContext(TableFactorContext.class,0);
		}
		public JoinConditionContext joinCondition() {
			return getRuleContext(JoinConditionContext.class,0);
		}
		public TerminalNode INNER() { return getToken(MySQLDMLParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(MySQLDMLParser.CROSS, 0); }
		public TerminalNode STRAIGHT_JOIN() { return getToken(MySQLDMLParser.STRAIGHT_JOIN, 0); }
		public TerminalNode LEFT() { return getToken(MySQLDMLParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(MySQLDMLParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(MySQLDMLParser.OUTER, 0); }
		public TerminalNode NATURAL() { return getToken(MySQLDMLParser.NATURAL, 0); }
		public JoinTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinTable; }
	}

	public final JoinTableContext joinTable() throws RecognitionException {
		JoinTableContext _localctx = new JoinTableContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_joinTable);
		int _la;
		try {
			setState(508);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(479);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER || _la==CROSS) {
					{
					setState(478);
					_la = _input.LA(1);
					if ( !(_la==INNER || _la==CROSS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(481);
				match(JOIN);
				setState(482);
				tableFactor();
				setState(484);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
				case 1:
					{
					setState(483);
					joinCondition();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(486);
				match(STRAIGHT_JOIN);
				setState(487);
				tableFactor();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(488);
				match(STRAIGHT_JOIN);
				setState(489);
				tableFactor();
				setState(490);
				joinCondition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(492);
				_la = _input.LA(1);
				if ( !(_la==LEFT || _la==RIGHT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(494);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(493);
					match(OUTER);
					}
				}

				setState(496);
				match(JOIN);
				setState(497);
				tableFactor();
				setState(498);
				joinCondition();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(500);
				match(NATURAL);
				setState(504);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case INNER:
					{
					setState(501);
					match(INNER);
					}
					break;
				case LEFT:
				case RIGHT:
					{
					setState(502);
					_la = _input.LA(1);
					if ( !(_la==LEFT || _la==RIGHT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					{
					setState(503);
					match(OUTER);
					}
					}
					break;
				case JOIN:
					break;
				default:
					break;
				}
				setState(506);
				match(JOIN);
				setState(507);
				tableFactor();
				}
				break;
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

	public static class JoinConditionContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(MySQLDMLParser.ON, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode USING() { return getToken(MySQLDMLParser.USING, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public JoinConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCondition; }
	}

	public final JoinConditionContext joinCondition() throws RecognitionException {
		JoinConditionContext _localctx = new JoinConditionContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_joinCondition);
		try {
			setState(514);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(510);
				match(ON);
				setState(511);
				expr(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(512);
				match(USING);
				setState(513);
				idList();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class IndexHintListContext extends ParserRuleContext {
		public List<IndexHintContext> indexHint() {
			return getRuleContexts(IndexHintContext.class);
		}
		public IndexHintContext indexHint(int i) {
			return getRuleContext(IndexHintContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public IndexHintListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexHintList; }
	}

	public final IndexHintListContext indexHintList() throws RecognitionException {
		IndexHintListContext _localctx = new IndexHintListContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_indexHintList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			indexHint();
			setState(521);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,63,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(517);
					match(COMMA);
					setState(518);
					indexHint();
					}
					} 
				}
				setState(523);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,63,_ctx);
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

	public static class IndexHintContext extends ParserRuleContext {
		public TerminalNode USE() { return getToken(MySQLDMLParser.USE, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TerminalNode INDEX() { return getToken(MySQLDMLParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(MySQLDMLParser.KEY, 0); }
		public List<TerminalNode> FOR() { return getTokens(MySQLDMLParser.FOR); }
		public TerminalNode FOR(int i) {
			return getToken(MySQLDMLParser.FOR, i);
		}
		public List<TerminalNode> JOIN() { return getTokens(MySQLDMLParser.JOIN); }
		public TerminalNode JOIN(int i) {
			return getToken(MySQLDMLParser.JOIN, i);
		}
		public List<TerminalNode> ORDER() { return getTokens(MySQLDMLParser.ORDER); }
		public TerminalNode ORDER(int i) {
			return getToken(MySQLDMLParser.ORDER, i);
		}
		public List<TerminalNode> BY() { return getTokens(MySQLDMLParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(MySQLDMLParser.BY, i);
		}
		public List<TerminalNode> GROUP() { return getTokens(MySQLDMLParser.GROUP); }
		public TerminalNode GROUP(int i) {
			return getToken(MySQLDMLParser.GROUP, i);
		}
		public TerminalNode IGNORE() { return getToken(MySQLDMLParser.IGNORE, 0); }
		public IndexHintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexHint; }
	}

	public final IndexHintContext indexHint() throws RecognitionException {
		IndexHintContext _localctx = new IndexHintContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_indexHint);
		int _la;
		try {
			setState(556);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case USE:
				enterOuterAlt(_localctx, 1);
				{
				setState(524);
				match(USE);
				setState(525);
				_la = _input.LA(1);
				if ( !(_la==INDEX || _la==KEY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(536);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FOR) {
					{
					{
					setState(526);
					match(FOR);
					setState(532);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case JOIN:
						{
						setState(527);
						match(JOIN);
						}
						break;
					case ORDER:
						{
						setState(528);
						match(ORDER);
						setState(529);
						match(BY);
						}
						break;
					case GROUP:
						{
						setState(530);
						match(GROUP);
						setState(531);
						match(BY);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					}
					setState(538);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(539);
				idList();
				}
				break;
			case IGNORE:
				enterOuterAlt(_localctx, 2);
				{
				setState(540);
				match(IGNORE);
				setState(541);
				_la = _input.LA(1);
				if ( !(_la==INDEX || _la==KEY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(552);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FOR) {
					{
					{
					setState(542);
					match(FOR);
					setState(548);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case JOIN:
						{
						setState(543);
						match(JOIN);
						}
						break;
					case ORDER:
						{
						setState(544);
						match(ORDER);
						setState(545);
						match(BY);
						}
						break;
					case GROUP:
						{
						setState(546);
						match(GROUP);
						setState(547);
						match(BY);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					}
					setState(554);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(555);
				idList();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class ExprContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public TerminalNode NOT() { return getToken(MySQLDMLParser.NOT, 0); }
		public TerminalNode NOT_SYM() { return getToken(MySQLDMLParser.NOT_SYM, 0); }
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public TerminalNode OR() { return getToken(MySQLDMLParser.OR, 0); }
		public TerminalNode OR_SYM() { return getToken(MySQLDMLParser.OR_SYM, 0); }
		public TerminalNode XOR() { return getToken(MySQLDMLParser.XOR, 0); }
		public TerminalNode AND() { return getToken(MySQLDMLParser.AND, 0); }
		public TerminalNode AND_SYM() { return getToken(MySQLDMLParser.AND_SYM, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 88;
		enterRecursionRule(_localctx, 88, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(568);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(559);
				match(LEFT_PAREN);
				setState(560);
				expr(0);
				setState(561);
				match(RIGHT_PAREN);
				}
				break;
			case 2:
				{
				setState(563);
				match(NOT);
				setState(564);
				expr(3);
				}
				break;
			case 3:
				{
				setState(565);
				match(NOT_SYM);
				setState(566);
				expr(2);
				}
				break;
			case 4:
				{
				setState(567);
				booleanPrimary(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(587);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(585);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(570);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(571);
						match(OR);
						setState(572);
						expr(10);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(573);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(574);
						match(OR_SYM);
						setState(575);
						expr(9);
						}
						break;
					case 3:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(576);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(577);
						match(XOR);
						setState(578);
						expr(8);
						}
						break;
					case 4:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(579);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(580);
						match(AND);
						setState(581);
						expr(7);
						}
						break;
					case 5:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(582);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(583);
						match(AND_SYM);
						setState(584);
						expr(6);
						}
						break;
					}
					} 
				}
				setState(589);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
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

	public static class BooleanPrimaryContext extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public TerminalNode IS() { return getToken(MySQLDMLParser.IS, 0); }
		public TerminalNode TRUE() { return getToken(MySQLDMLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MySQLDMLParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(MySQLDMLParser.UNKNOWN, 0); }
		public TerminalNode NULL() { return getToken(MySQLDMLParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(MySQLDMLParser.NOT, 0); }
		public TerminalNode SAFE_EQ() { return getToken(MySQLDMLParser.SAFE_EQ, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode ALL() { return getToken(MySQLDMLParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(MySQLDMLParser.ANY, 0); }
		public BooleanPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanPrimary; }
	}

	public final BooleanPrimaryContext booleanPrimary() throws RecognitionException {
		return booleanPrimary(0);
	}

	private BooleanPrimaryContext booleanPrimary(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanPrimaryContext _localctx = new BooleanPrimaryContext(_ctx, _parentState);
		BooleanPrimaryContext _prevctx = _localctx;
		int _startState = 90;
		enterRecursionRule(_localctx, 90, RULE_booleanPrimary, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(591);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(613);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(611);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
					case 1:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(593);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(594);
						match(IS);
						setState(596);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(595);
							match(NOT);
							}
						}

						setState(598);
						_la = _input.LA(1);
						if ( !(((((_la - 228)) & ~0x3f) == 0 && ((1L << (_la - 228)) & ((1L << (NULL - 228)) | (1L << (TRUE - 228)) | (1L << (FALSE - 228)) | (1L << (UNKNOWN - 228)))) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 2:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(599);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(600);
						match(SAFE_EQ);
						setState(601);
						predicate();
						}
						break;
					case 3:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(602);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(603);
						comparisonOperator();
						setState(604);
						predicate();
						}
						break;
					case 4:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(606);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(607);
						comparisonOperator();
						setState(608);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(609);
						subquery();
						}
						break;
					}
					} 
				}
				setState(615);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
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

	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDMLParser.EQ_OR_ASSIGN, 0); }
		public TerminalNode GTE() { return getToken(MySQLDMLParser.GTE, 0); }
		public TerminalNode GT() { return getToken(MySQLDMLParser.GT, 0); }
		public TerminalNode LTE() { return getToken(MySQLDMLParser.LTE, 0); }
		public TerminalNode LT() { return getToken(MySQLDMLParser.LT, 0); }
		public TerminalNode NEQ_SYM() { return getToken(MySQLDMLParser.NEQ_SYM, 0); }
		public TerminalNode NEQ() { return getToken(MySQLDMLParser.NEQ, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(616);
			_la = _input.LA(1);
			if ( !(((((_la - 111)) & ~0x3f) == 0 && ((1L << (_la - 111)) & ((1L << (EQ_OR_ASSIGN - 111)) | (1L << (NEQ - 111)) | (1L << (NEQ_SYM - 111)) | (1L << (GT - 111)) | (1L << (GTE - 111)) | (1L << (LT - 111)) | (1L << (LTE - 111)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class PredicateContext extends ParserRuleContext {
		public BitExprContext bitExpr() {
			return getRuleContext(BitExprContext.class,0);
		}
		public TerminalNode IN() { return getToken(MySQLDMLParser.IN, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode NOT() { return getToken(MySQLDMLParser.NOT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public TerminalNode BETWEEN() { return getToken(MySQLDMLParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(MySQLDMLParser.AND, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode SOUNDS() { return getToken(MySQLDMLParser.SOUNDS, 0); }
		public TerminalNode LIKE() { return getToken(MySQLDMLParser.LIKE, 0); }
		public List<TerminalNode> ESCAPE() { return getTokens(MySQLDMLParser.ESCAPE); }
		public TerminalNode ESCAPE(int i) {
			return getToken(MySQLDMLParser.ESCAPE, i);
		}
		public TerminalNode REGEXP() { return getToken(MySQLDMLParser.REGEXP, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_predicate);
		int _la;
		try {
			int _alt;
			setState(676);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(618);
				bitExpr(0);
				setState(620);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(619);
					match(NOT);
					}
				}

				setState(622);
				match(IN);
				setState(623);
				subquery();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(625);
				bitExpr(0);
				setState(627);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(626);
					match(NOT);
					}
				}

				setState(629);
				match(IN);
				setState(630);
				match(LEFT_PAREN);
				setState(631);
				simpleExpr(0);
				setState(636);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(632);
					match(COMMA);
					setState(633);
					simpleExpr(0);
					}
					}
					setState(638);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(639);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(641);
				bitExpr(0);
				setState(643);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(642);
					match(NOT);
					}
				}

				setState(645);
				match(BETWEEN);
				setState(646);
				simpleExpr(0);
				setState(647);
				match(AND);
				setState(648);
				predicate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(650);
				bitExpr(0);
				setState(651);
				match(SOUNDS);
				setState(652);
				match(LIKE);
				setState(653);
				simpleExpr(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(655);
				bitExpr(0);
				setState(657);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(656);
					match(NOT);
					}
				}

				setState(659);
				match(LIKE);
				setState(660);
				simpleExpr(0);
				setState(665);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(661);
						match(ESCAPE);
						setState(662);
						simpleExpr(0);
						}
						} 
					}
					setState(667);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(668);
				bitExpr(0);
				setState(670);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(669);
					match(NOT);
					}
				}

				setState(672);
				match(REGEXP);
				setState(673);
				simpleExpr(0);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(675);
				bitExpr(0);
				}
				break;
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

	public static class BitExprContext extends ParserRuleContext {
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public List<BitExprContext> bitExpr() {
			return getRuleContexts(BitExprContext.class);
		}
		public BitExprContext bitExpr(int i) {
			return getRuleContext(BitExprContext.class,i);
		}
		public TerminalNode BIT_INCLUSIVE_OR() { return getToken(MySQLDMLParser.BIT_INCLUSIVE_OR, 0); }
		public TerminalNode BIT_AND() { return getToken(MySQLDMLParser.BIT_AND, 0); }
		public TerminalNode SIGNED_LEFT_SHIFT() { return getToken(MySQLDMLParser.SIGNED_LEFT_SHIFT, 0); }
		public TerminalNode SIGNED_RIGHT_SHIFT() { return getToken(MySQLDMLParser.SIGNED_RIGHT_SHIFT, 0); }
		public TerminalNode PLUS() { return getToken(MySQLDMLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(MySQLDMLParser.MINUS, 0); }
		public TerminalNode ASTERISK() { return getToken(MySQLDMLParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(MySQLDMLParser.SLASH, 0); }
		public TerminalNode DIV() { return getToken(MySQLDMLParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(MySQLDMLParser.MOD, 0); }
		public TerminalNode MOD_SYM() { return getToken(MySQLDMLParser.MOD_SYM, 0); }
		public TerminalNode BIT_EXCLUSIVE_OR() { return getToken(MySQLDMLParser.BIT_EXCLUSIVE_OR, 0); }
		public BitExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitExpr; }
	}

	public final BitExprContext bitExpr() throws RecognitionException {
		return bitExpr(0);
	}

	private BitExprContext bitExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BitExprContext _localctx = new BitExprContext(_ctx, _parentState);
		BitExprContext _prevctx = _localctx;
		int _startState = 96;
		enterRecursionRule(_localctx, 96, RULE_bitExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(679);
			simpleExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(719);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,84,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(717);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
					case 1:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(681);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(682);
						match(BIT_INCLUSIVE_OR);
						setState(683);
						bitExpr(14);
						}
						break;
					case 2:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(684);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(685);
						match(BIT_AND);
						setState(686);
						bitExpr(13);
						}
						break;
					case 3:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(687);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(688);
						match(SIGNED_LEFT_SHIFT);
						setState(689);
						bitExpr(12);
						}
						break;
					case 4:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(690);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(691);
						match(SIGNED_RIGHT_SHIFT);
						setState(692);
						bitExpr(11);
						}
						break;
					case 5:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(693);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(694);
						match(PLUS);
						setState(695);
						bitExpr(10);
						}
						break;
					case 6:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(696);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(697);
						match(MINUS);
						setState(698);
						bitExpr(9);
						}
						break;
					case 7:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(699);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(700);
						match(ASTERISK);
						setState(701);
						bitExpr(8);
						}
						break;
					case 8:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(702);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(703);
						match(SLASH);
						setState(704);
						bitExpr(7);
						}
						break;
					case 9:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(705);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(706);
						match(DIV);
						setState(707);
						bitExpr(6);
						}
						break;
					case 10:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(708);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(709);
						match(MOD);
						setState(710);
						bitExpr(5);
						}
						break;
					case 11:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(711);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(712);
						match(MOD_SYM);
						setState(713);
						bitExpr(4);
						}
						break;
					case 12:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(714);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(715);
						match(BIT_EXCLUSIVE_OR);
						setState(716);
						bitExpr(3);
						}
						break;
					}
					} 
				}
				setState(721);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,84,_ctx);
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

	public static class SimpleExprContext extends ParserRuleContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public LiterContext liter() {
			return getRuleContext(LiterContext.class,0);
		}
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TerminalNode PLUS() { return getToken(MySQLDMLParser.PLUS, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode MINUS() { return getToken(MySQLDMLParser.MINUS, 0); }
		public TerminalNode UNARY_BIT_COMPLEMENT() { return getToken(MySQLDMLParser.UNARY_BIT_COMPLEMENT, 0); }
		public TerminalNode NOT_SYM() { return getToken(MySQLDMLParser.NOT_SYM, 0); }
		public TerminalNode BINARY() { return getToken(MySQLDMLParser.BINARY, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public TerminalNode ROW() { return getToken(MySQLDMLParser.ROW, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(MySQLDMLParser.EXISTS, 0); }
		public TerminalNode AND_SYM() { return getToken(MySQLDMLParser.AND_SYM, 0); }
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public SimpleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleExpr; }
	}

	public final SimpleExprContext simpleExpr() throws RecognitionException {
		return simpleExpr(0);
	}

	private SimpleExprContext simpleExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		SimpleExprContext _localctx = new SimpleExprContext(_ctx, _parentState);
		SimpleExprContext _prevctx = _localctx;
		int _startState = 98;
		enterRecursionRule(_localctx, 98, RULE_simpleExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(755);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(723);
				functionCall();
				}
				break;
			case 3:
				{
				setState(724);
				liter();
				}
				break;
			case 4:
				{
				setState(725);
				match(ID);
				}
				break;
			case 5:
				{
				setState(726);
				match(PLUS);
				setState(727);
				simpleExpr(9);
				}
				break;
			case 6:
				{
				setState(728);
				match(MINUS);
				setState(729);
				simpleExpr(8);
				}
				break;
			case 7:
				{
				setState(730);
				match(UNARY_BIT_COMPLEMENT);
				setState(731);
				simpleExpr(7);
				}
				break;
			case 8:
				{
				setState(732);
				match(NOT_SYM);
				setState(733);
				simpleExpr(6);
				}
				break;
			case 9:
				{
				setState(734);
				match(BINARY);
				setState(735);
				simpleExpr(5);
				}
				break;
			case 10:
				{
				setState(736);
				match(LEFT_PAREN);
				setState(737);
				expr(0);
				setState(738);
				match(RIGHT_PAREN);
				}
				break;
			case 11:
				{
				setState(740);
				match(ROW);
				setState(741);
				match(LEFT_PAREN);
				setState(742);
				simpleExpr(0);
				setState(747);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(743);
					match(COMMA);
					setState(744);
					simpleExpr(0);
					}
					}
					setState(749);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(750);
				match(RIGHT_PAREN);
				}
				break;
			case 12:
				{
				setState(752);
				subquery();
				}
				break;
			case 13:
				{
				setState(753);
				match(EXISTS);
				setState(754);
				subquery();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(764);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(762);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
					case 1:
						{
						_localctx = new SimpleExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
						setState(757);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(758);
						match(AND_SYM);
						setState(759);
						simpleExpr(11);
						}
						break;
					case 2:
						{
						_localctx = new SimpleExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
						setState(760);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(761);
						collateClause();
						}
						break;
					}
					} 
				}
				setState(766);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
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

	public static class LiterContext extends ParserRuleContext {
		public TerminalNode QUESTION() { return getToken(MySQLDMLParser.QUESTION, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDMLParser.NUMBER, 0); }
		public TerminalNode TRUE() { return getToken(MySQLDMLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MySQLDMLParser.FALSE, 0); }
		public TerminalNode NULL() { return getToken(MySQLDMLParser.NULL, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(MySQLDMLParser.LEFT_BRACE, 0); }
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TerminalNode STRING() { return getToken(MySQLDMLParser.STRING, 0); }
		public TerminalNode RIGHT_BRACE() { return getToken(MySQLDMLParser.RIGHT_BRACE, 0); }
		public TerminalNode HEX_DIGIT() { return getToken(MySQLDMLParser.HEX_DIGIT, 0); }
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public TerminalNode DATE() { return getToken(MySQLDMLParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(MySQLDMLParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(MySQLDMLParser.TIMESTAMP, 0); }
		public TerminalNode BIT_NUM() { return getToken(MySQLDMLParser.BIT_NUM, 0); }
		public LiterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_liter; }
	}

	public final LiterContext liter() throws RecognitionException {
		LiterContext _localctx = new LiterContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_liter);
		int _la;
		try {
			setState(793);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(767);
				match(QUESTION);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(768);
				match(NUMBER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(769);
				match(TRUE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(770);
				match(FALSE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(771);
				match(NULL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(772);
				match(LEFT_BRACE);
				setState(773);
				match(ID);
				setState(774);
				match(STRING);
				setState(775);
				match(RIGHT_BRACE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(776);
				match(HEX_DIGIT);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(778);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(777);
					match(ID);
					}
				}

				setState(780);
				match(STRING);
				setState(782);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(781);
					collateClause();
					}
					break;
				}
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(784);
				_la = _input.LA(1);
				if ( !(((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (DATE - 257)) | (1L << (TIME - 257)) | (1L << (TIMESTAMP - 257)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(785);
				match(STRING);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(787);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(786);
					match(ID);
					}
				}

				setState(789);
				match(BIT_NUM);
				setState(791);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
				case 1:
					{
					setState(790);
					collateClause();
					}
					break;
				}
				}
				break;
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

	public static class CharacterAndCollateContext extends ParserRuleContext {
		public CharacterSetContext characterSet() {
			return getRuleContext(CharacterSetContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public CharacterAndCollateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterAndCollate; }
	}

	public final CharacterAndCollateContext characterAndCollate() throws RecognitionException {
		CharacterAndCollateContext _localctx = new CharacterAndCollateContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_characterAndCollate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(795);
			characterSet();
			setState(796);
			collateClause();
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

	public static class CharacterSetContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(MySQLDMLParser.SET, 0); }
		public CharsetNameContext charsetName() {
			return getRuleContext(CharsetNameContext.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(MySQLDMLParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(MySQLDMLParser.CHAR, 0); }
		public TerminalNode CHARSET() { return getToken(MySQLDMLParser.CHARSET, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDMLParser.EQ_OR_ASSIGN, 0); }
		public CharacterSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSet; }
	}

	public final CharacterSetContext characterSet() throws RecognitionException {
		CharacterSetContext _localctx = new CharacterSetContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_characterSet);
		int _la;
		try {
			setState(806);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CHARACTER:
			case CHAR:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(798);
				_la = _input.LA(1);
				if ( !(_la==CHARACTER || _la==CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(799);
				match(SET);
				setState(800);
				charsetName();
				}
				}
				break;
			case CHARSET:
				enterOuterAlt(_localctx, 2);
				{
				setState(801);
				match(CHARSET);
				setState(803);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(802);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(805);
				charsetName();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class CollateClauseContext extends ParserRuleContext {
		public TerminalNode COLLATE() { return getToken(MySQLDMLParser.COLLATE, 0); }
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public CollateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClause; }
	}

	public final CollateClauseContext collateClause() throws RecognitionException {
		CollateClauseContext _localctx = new CollateClauseContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_collateClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(808);
			match(COLLATE);
			setState(809);
			match(ID);
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

	public static class CharsetNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TerminalNode BINARY() { return getToken(MySQLDMLParser.BINARY, 0); }
		public CharsetNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charsetName; }
	}

	public final CharsetNameContext charsetName() throws RecognitionException {
		CharsetNameContext _localctx = new CharsetNameContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_charsetName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(811);
			_la = _input.LA(1);
			if ( !(_la==BINARY || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class CharacterAndCollateWithEqualContext extends ParserRuleContext {
		public CharacterSetWithEqualContext characterSetWithEqual() {
			return getRuleContext(CharacterSetWithEqualContext.class,0);
		}
		public CollateClauseWithEqualContext collateClauseWithEqual() {
			return getRuleContext(CollateClauseWithEqualContext.class,0);
		}
		public CharacterAndCollateWithEqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterAndCollateWithEqual; }
	}

	public final CharacterAndCollateWithEqualContext characterAndCollateWithEqual() throws RecognitionException {
		CharacterAndCollateWithEqualContext _localctx = new CharacterAndCollateWithEqualContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_characterAndCollateWithEqual);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(813);
			characterSetWithEqual();
			setState(814);
			collateClauseWithEqual();
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

	public static class CharacterSetWithEqualContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(MySQLDMLParser.SET, 0); }
		public CharsetNameContext charsetName() {
			return getRuleContext(CharsetNameContext.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(MySQLDMLParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(MySQLDMLParser.CHAR, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDMLParser.EQ_OR_ASSIGN, 0); }
		public TerminalNode CHARSET() { return getToken(MySQLDMLParser.CHARSET, 0); }
		public CharacterSetWithEqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSetWithEqual; }
	}

	public final CharacterSetWithEqualContext characterSetWithEqual() throws RecognitionException {
		CharacterSetWithEqualContext _localctx = new CharacterSetWithEqualContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_characterSetWithEqual);
		int _la;
		try {
			setState(827);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CHARACTER:
			case CHAR:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(816);
				_la = _input.LA(1);
				if ( !(_la==CHARACTER || _la==CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(817);
				match(SET);
				setState(819);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(818);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(821);
				charsetName();
				}
				}
				break;
			case CHARSET:
				enterOuterAlt(_localctx, 2);
				{
				setState(822);
				match(CHARSET);
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(823);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(826);
				charsetName();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class CollateClauseWithEqualContext extends ParserRuleContext {
		public TerminalNode COLLATE() { return getToken(MySQLDMLParser.COLLATE, 0); }
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDMLParser.EQ_OR_ASSIGN, 0); }
		public CollateClauseWithEqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClauseWithEqual; }
	}

	public final CollateClauseWithEqualContext collateClauseWithEqual() throws RecognitionException {
		CollateClauseWithEqualContext _localctx = new CollateClauseWithEqualContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_collateClauseWithEqual);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(829);
			match(COLLATE);
			setState(831);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_OR_ASSIGN) {
				{
				setState(830);
				match(EQ_OR_ASSIGN);
				}
			}

			setState(833);
			match(ID);
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

	public static class SelectContext extends ParserRuleContext {
		public WithClauseContext withClause() {
			return getRuleContext(WithClauseContext.class,0);
		}
		public UnionSelectContext unionSelect() {
			return getRuleContext(UnionSelectContext.class,0);
		}
		public SelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select; }
	}

	public final SelectContext select() throws RecognitionException {
		SelectContext _localctx = new SelectContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_select);
		try {
			setState(837);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WITH:
				enterOuterAlt(_localctx, 1);
				{
				setState(835);
				withClause();
				}
				break;
			case SELECT:
				enterOuterAlt(_localctx, 2);
				{
				setState(836);
				unionSelect();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class WithClauseContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(MySQLDMLParser.WITH, 0); }
		public List<CteClauseContext> cteClause() {
			return getRuleContexts(CteClauseContext.class);
		}
		public CteClauseContext cteClause(int i) {
			return getRuleContext(CteClauseContext.class,i);
		}
		public UnionSelectContext unionSelect() {
			return getRuleContext(UnionSelectContext.class,0);
		}
		public TerminalNode RECURSIVE() { return getToken(MySQLDMLParser.RECURSIVE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public WithClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_withClause; }
	}

	public final WithClauseContext withClause() throws RecognitionException {
		WithClauseContext _localctx = new WithClauseContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_withClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(839);
			match(WITH);
			setState(841);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECURSIVE) {
				{
				setState(840);
				match(RECURSIVE);
				}
			}

			setState(843);
			cteClause();
			setState(848);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(844);
				match(COMMA);
				setState(845);
				cteClause();
				}
				}
				setState(850);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(851);
			unionSelect();
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

	public static class CteClauseContext extends ParserRuleContext {
		public CteNameContext cteName() {
			return getRuleContext(CteNameContext.class,0);
		}
		public TerminalNode AS() { return getToken(MySQLDMLParser.AS, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public CteClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cteClause; }
	}

	public final CteClauseContext cteClause() throws RecognitionException {
		CteClauseContext _localctx = new CteClauseContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_cteClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(853);
			cteName();
			setState(855);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(854);
				idList();
				}
			}

			setState(857);
			match(AS);
			setState(858);
			subquery();
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

	public static class UnionSelectContext extends ParserRuleContext {
		public List<SelectExpressionContext> selectExpression() {
			return getRuleContexts(SelectExpressionContext.class);
		}
		public SelectExpressionContext selectExpression(int i) {
			return getRuleContext(SelectExpressionContext.class,i);
		}
		public List<TerminalNode> UNION() { return getTokens(MySQLDMLParser.UNION); }
		public TerminalNode UNION(int i) {
			return getToken(MySQLDMLParser.UNION, i);
		}
		public List<TerminalNode> ALL() { return getTokens(MySQLDMLParser.ALL); }
		public TerminalNode ALL(int i) {
			return getToken(MySQLDMLParser.ALL, i);
		}
		public UnionSelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionSelect; }
	}

	public final UnionSelectContext unionSelect() throws RecognitionException {
		UnionSelectContext _localctx = new UnionSelectContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_unionSelect);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(860);
			selectExpression();
			setState(868);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==UNION) {
				{
				{
				setState(861);
				match(UNION);
				setState(863);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(862);
					match(ALL);
					}
				}

				setState(865);
				selectExpression();
				}
				}
				setState(870);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class SelectExpressionContext extends ParserRuleContext {
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public SelectExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectExpression; }
	}

	public final SelectExpressionContext selectExpression() throws RecognitionException {
		SelectExpressionContext _localctx = new SelectExpressionContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_selectExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(871);
			selectClause();
			setState(873);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(872);
				fromClause();
				}
			}

			setState(876);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(875);
				whereClause();
				}
			}

			setState(879);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(878);
				groupByClause();
				}
			}

			setState(882);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(881);
				orderByClause();
				}
			}

			setState(885);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(884);
				limitClause();
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

	public static class SelectClauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(MySQLDMLParser.SELECT, 0); }
		public SelectSpecContext selectSpec() {
			return getRuleContext(SelectSpecContext.class,0);
		}
		public SelectExprsContext selectExprs() {
			return getRuleContext(SelectExprsContext.class,0);
		}
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_selectClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(887);
			match(SELECT);
			setState(888);
			selectSpec();
			setState(889);
			selectExprs();
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

	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(MySQLDMLParser.FROM, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_fromClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(891);
			match(FROM);
			setState(892);
			tableReferences();
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

	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(MySQLDMLParser.WHERE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(894);
			match(WHERE);
			setState(895);
			expr(0);
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

	public static class GroupByClauseContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(MySQLDMLParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(MySQLDMLParser.BY, 0); }
		public List<GroupByItemContext> groupByItem() {
			return getRuleContexts(GroupByItemContext.class);
		}
		public GroupByItemContext groupByItem(int i) {
			return getRuleContext(GroupByItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public TerminalNode WITH() { return getToken(MySQLDMLParser.WITH, 0); }
		public TerminalNode ROLLUP() { return getToken(MySQLDMLParser.ROLLUP, 0); }
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_groupByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(897);
			match(GROUP);
			setState(898);
			match(BY);
			setState(899);
			groupByItem();
			setState(904);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(900);
				match(COMMA);
				setState(901);
				groupByItem();
				}
				}
				setState(906);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(909);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(907);
				match(WITH);
				setState(908);
				match(ROLLUP);
				}
			}

			setState(912);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(911);
				havingClause();
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

	public static class HavingClauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(MySQLDMLParser.HAVING, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(914);
			match(HAVING);
			setState(915);
			expr(0);
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

	public static class OrderByClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(MySQLDMLParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(MySQLDMLParser.BY, 0); }
		public List<GroupByItemContext> groupByItem() {
			return getRuleContexts(GroupByItemContext.class);
		}
		public GroupByItemContext groupByItem(int i) {
			return getRuleContext(GroupByItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(917);
			match(ORDER);
			setState(918);
			match(BY);
			setState(919);
			groupByItem();
			setState(924);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(920);
				match(COMMA);
				setState(921);
				groupByItem();
				}
				}
				setState(926);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class GroupByItemContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode NUMBER() { return getToken(MySQLDMLParser.NUMBER, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ASC() { return getToken(MySQLDMLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(MySQLDMLParser.DESC, 0); }
		public GroupByItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByItem; }
	}

	public final GroupByItemContext groupByItem() throws RecognitionException {
		GroupByItemContext _localctx = new GroupByItemContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_groupByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(930);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(927);
				columnName();
				}
				break;
			case 2:
				{
				setState(928);
				match(NUMBER);
				}
				break;
			case 3:
				{
				setState(929);
				expr(0);
				}
				break;
			}
			setState(933);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(932);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
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

	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode LIMIT() { return getToken(MySQLDMLParser.LIMIT, 0); }
		public RangeClauseContext rangeClause() {
			return getRuleContext(RangeClauseContext.class,0);
		}
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(935);
			match(LIMIT);
			setState(936);
			rangeClause();
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

	public static class PartitionClauseContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(MySQLDMLParser.PARTITION, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public PartitionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionClause; }
	}

	public final PartitionClauseContext partitionClause() throws RecognitionException {
		PartitionClauseContext _localctx = new PartitionClauseContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_partitionClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(938);
			match(PARTITION);
			setState(939);
			idList();
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

	public static class FunctionCallContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDMLParser.ID, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(941);
			match(ID);
			setState(942);
			match(LEFT_PAREN);
			setState(952);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(944);
				expr(0);
				setState(949);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(945);
					match(COMMA);
					setState(946);
					expr(0);
					}
					}
					setState(951);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(954);
			match(RIGHT_PAREN);
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

	public static class SelectExprsContext extends ParserRuleContext {
		public TerminalNode ASTERISK() { return getToken(MySQLDMLParser.ASTERISK, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public List<SelectExprContext> selectExpr() {
			return getRuleContexts(SelectExprContext.class);
		}
		public SelectExprContext selectExpr(int i) {
			return getRuleContext(SelectExprContext.class,i);
		}
		public SelectExprsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectExprs; }
	}

	public final SelectExprsContext selectExprs() throws RecognitionException {
		SelectExprsContext _localctx = new SelectExprsContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_selectExprs);
		int _la;
		try {
			setState(976);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(956);
				match(ASTERISK);
				setState(961);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(957);
					match(COMMA);
					setState(958);
					selectExpr();
					}
					}
					setState(963);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(964);
				selectExpr();
				setState(967);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
				case 1:
					{
					setState(965);
					match(COMMA);
					setState(966);
					match(ASTERISK);
					}
					break;
				}
				setState(973);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(969);
					match(COMMA);
					setState(970);
					selectExpr();
					}
					}
					setState(975);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
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

	public static class SubqueryContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public UnionSelectContext unionSelect() {
			return getRuleContext(UnionSelectContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(978);
			match(LEFT_PAREN);
			setState(979);
			unionSelect();
			setState(980);
			match(RIGHT_PAREN);
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

	public static class ExecuteContext extends ParserRuleContext {
		public SelectContext select() {
			return getRuleContext(SelectContext.class,0);
		}
		public InsertContext insert() {
			return getRuleContext(InsertContext.class,0);
		}
		public UpdateContext update() {
			return getRuleContext(UpdateContext.class,0);
		}
		public DeleteContext delete() {
			return getRuleContext(DeleteContext.class,0);
		}
		public ExecuteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execute; }
	}

	public final ExecuteContext execute() throws RecognitionException {
		ExecuteContext _localctx = new ExecuteContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_execute);
		try {
			setState(986);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
			case WITH:
				enterOuterAlt(_localctx, 1);
				{
				setState(982);
				select();
				}
				break;
			case INSERT:
				enterOuterAlt(_localctx, 2);
				{
				setState(983);
				insert();
				}
				break;
			case UPDATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(984);
				update();
				}
				break;
			case DELETE:
				enterOuterAlt(_localctx, 4);
				{
				setState(985);
				delete();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class DeleteContext extends ParserRuleContext {
		public DeleteClauseContext deleteClause() {
			return getRuleContext(DeleteClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public DeleteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_delete; }
	}

	public final DeleteContext delete() throws RecognitionException {
		DeleteContext _localctx = new DeleteContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_delete);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(988);
			deleteClause();
			setState(990);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(989);
				whereClause();
				}
			}

			setState(993);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(992);
				orderByClause();
				}
			}

			setState(996);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(995);
				limitClause();
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

	public static class UpdateContext extends ParserRuleContext {
		public UpdateClauseContext updateClause() {
			return getRuleContext(UpdateClauseContext.class,0);
		}
		public SetClauseContext setClause() {
			return getRuleContext(SetClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public UpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_update; }
	}

	public final UpdateContext update() throws RecognitionException {
		UpdateContext _localctx = new UpdateContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_update);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(998);
			updateClause();
			setState(999);
			setClause();
			setState(1001);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(1000);
				whereClause();
				}
			}

			setState(1004);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(1003);
				orderByClause();
				}
			}

			setState(1007);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(1006);
				limitClause();
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

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(MySQLDMLParser.DEFAULT, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_value);
		try {
			setState(1011);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1009);
				match(DEFAULT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1010);
				expr(0);
				}
				break;
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

	public static class ValueListContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDMLParser.COMMA, i);
		}
		public ValueListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueList; }
	}

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1013);
			value();
			setState(1018);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1014);
				match(COMMA);
				setState(1015);
				value();
				}
				}
				setState(1020);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class ValueListWithParenContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDMLParser.LEFT_PAREN, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDMLParser.RIGHT_PAREN, 0); }
		public ValueListWithParenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueListWithParen; }
	}

	public final ValueListWithParenContext valueListWithParen() throws RecognitionException {
		ValueListWithParenContext _localctx = new ValueListWithParenContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_valueListWithParen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1021);
			match(LEFT_PAREN);
			setState(1022);
			valueList();
			setState(1023);
			match(RIGHT_PAREN);
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
		case 44:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 45:
			return booleanPrimary_sempred((BooleanPrimaryContext)_localctx, predIndex);
		case 48:
			return bitExpr_sempred((BitExprContext)_localctx, predIndex);
		case 49:
			return simpleExpr_sempred((SimpleExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 9);
		case 1:
			return precpred(_ctx, 8);
		case 2:
			return precpred(_ctx, 7);
		case 3:
			return precpred(_ctx, 6);
		case 4:
			return precpred(_ctx, 5);
		}
		return true;
	}
	private boolean booleanPrimary_sempred(BooleanPrimaryContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 5);
		case 6:
			return precpred(_ctx, 4);
		case 7:
			return precpred(_ctx, 3);
		case 8:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean bitExpr_sempred(BitExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 9:
			return precpred(_ctx, 13);
		case 10:
			return precpred(_ctx, 12);
		case 11:
			return precpred(_ctx, 11);
		case 12:
			return precpred(_ctx, 10);
		case 13:
			return precpred(_ctx, 9);
		case 14:
			return precpred(_ctx, 8);
		case 15:
			return precpred(_ctx, 7);
		case 16:
			return precpred(_ctx, 6);
		case 17:
			return precpred(_ctx, 5);
		case 18:
			return precpred(_ctx, 4);
		case 19:
			return precpred(_ctx, 3);
		case 20:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean simpleExpr_sempred(SimpleExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 21:
			return precpred(_ctx, 10);
		case 22:
			return precpred(_ctx, 11);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0118\u0404\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\3\2\5\2\u00a6"+
		"\n\2\3\2\5\2\u00a9\n\2\3\2\5\2\u00ac\n\2\3\2\5\2\u00af\n\2\3\2\5\2\u00b2"+
		"\n\2\3\2\5\2\u00b5\n\2\3\2\5\2\u00b8\n\2\3\2\5\2\u00bb\n\2\3\3\3\3\5\3"+
		"\u00bf\n\3\3\4\3\4\3\4\6\4\u00c4\n\4\r\4\16\4\u00c5\3\4\5\4\u00c9\n\4"+
		"\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\6\6\u00d4\n\6\r\6\16\6\u00d5\3\6"+
		"\5\6\u00d9\n\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n"+
		"\5\n\u00e9\n\n\3\n\5\n\u00ec\n\n\3\n\5\n\u00ef\n\n\3\13\3\13\3\13\3\13"+
		"\5\13\u00f5\n\13\3\f\3\f\3\f\5\f\u00fa\n\f\3\r\3\r\5\r\u00fe\n\r\3\r\3"+
		"\r\3\r\5\r\u0103\n\r\7\r\u0105\n\r\f\r\16\r\u0108\13\r\3\r\3\r\3\r\3\r"+
		"\3\r\5\r\u010f\n\r\3\r\3\r\3\r\5\r\u0114\n\r\7\r\u0116\n\r\f\r\16\r\u0119"+
		"\13\r\3\r\3\r\5\r\u011d\n\r\3\16\5\16\u0120\n\16\3\16\5\16\u0123\n\16"+
		"\3\16\5\16\u0126\n\16\5\16\u0128\n\16\3\17\3\17\5\17\u012c\n\17\3\17\3"+
		"\17\5\17\u0130\n\17\3\17\3\17\5\17\u0134\n\17\3\17\5\17\u0137\n\17\3\20"+
		"\3\20\5\20\u013b\n\20\3\21\3\21\3\21\3\21\5\21\u0141\n\21\3\22\5\22\u0144"+
		"\n\22\3\22\3\22\5\22\u0148\n\22\3\23\3\23\3\23\3\23\7\23\u014e\n\23\f"+
		"\23\16\23\u0151\13\23\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\26"+
		"\3\26\3\26\5\26\u015f\n\26\3\27\3\27\3\27\7\27\u0164\n\27\f\27\16\27\u0167"+
		"\13\27\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32\5\32\u0172\n\32\3"+
		"\32\5\32\u0175\n\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37"+
		"\3 \3 \3!\3!\3\"\3\"\3#\3#\3#\3#\7#\u018b\n#\f#\16#\u018e\13#\3#\3#\3"+
		"$\3$\3$\7$\u0195\n$\f$\16$\u0198\13$\3$\3$\3$\5$\u019d\n$\3%\3%\3%\3%"+
		"\7%\u01a3\n%\f%\16%\u01a6\13%\3%\3%\3&\3&\3&\5&\u01ad\n&\3\'\3\'\3\'\7"+
		"\'\u01b2\n\'\f\'\16\'\u01b5\13\'\3(\3(\3(\6(\u01ba\n(\r(\16(\u01bb\3("+
		"\3(\6(\u01c0\n(\r(\16(\u01c1\3(\5(\u01c5\n(\3)\3)\3)\5)\u01ca\n)\3)\5"+
		")\u01cd\n)\3)\5)\u01d0\n)\3)\5)\u01d3\n)\3)\3)\5)\u01d7\n)\3)\3)\3)\3"+
		")\3)\3)\5)\u01df\n)\3*\5*\u01e2\n*\3*\3*\3*\5*\u01e7\n*\3*\3*\3*\3*\3"+
		"*\3*\3*\3*\5*\u01f1\n*\3*\3*\3*\3*\3*\3*\3*\3*\5*\u01fb\n*\3*\3*\5*\u01ff"+
		"\n*\3+\3+\3+\3+\5+\u0205\n+\3,\3,\3,\7,\u020a\n,\f,\16,\u020d\13,\3-\3"+
		"-\3-\3-\3-\3-\3-\3-\5-\u0217\n-\7-\u0219\n-\f-\16-\u021c\13-\3-\3-\3-"+
		"\3-\3-\3-\3-\3-\3-\5-\u0227\n-\7-\u0229\n-\f-\16-\u022c\13-\3-\5-\u022f"+
		"\n-\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\5.\u023b\n.\3.\3.\3.\3.\3.\3.\3.\3."+
		"\3.\3.\3.\3.\3.\3.\3.\7.\u024c\n.\f.\16.\u024f\13.\3/\3/\3/\3/\3/\3/\5"+
		"/\u0257\n/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\7/\u0266\n/\f/\16/\u0269"+
		"\13/\3\60\3\60\3\61\3\61\5\61\u026f\n\61\3\61\3\61\3\61\3\61\3\61\5\61"+
		"\u0276\n\61\3\61\3\61\3\61\3\61\3\61\7\61\u027d\n\61\f\61\16\61\u0280"+
		"\13\61\3\61\3\61\3\61\3\61\5\61\u0286\n\61\3\61\3\61\3\61\3\61\3\61\3"+
		"\61\3\61\3\61\3\61\3\61\3\61\3\61\5\61\u0294\n\61\3\61\3\61\3\61\3\61"+
		"\7\61\u029a\n\61\f\61\16\61\u029d\13\61\3\61\3\61\5\61\u02a1\n\61\3\61"+
		"\3\61\3\61\3\61\5\61\u02a7\n\61\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"+
		"\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"+
		"\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"+
		"\3\62\3\62\3\62\7\62\u02d0\n\62\f\62\16\62\u02d3\13\62\3\63\3\63\3\63"+
		"\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63"+
		"\3\63\3\63\3\63\3\63\3\63\3\63\7\63\u02ec\n\63\f\63\16\63\u02ef\13\63"+
		"\3\63\3\63\3\63\3\63\3\63\5\63\u02f6\n\63\3\63\3\63\3\63\3\63\3\63\7\63"+
		"\u02fd\n\63\f\63\16\63\u0300\13\63\3\64\3\64\3\64\3\64\3\64\3\64\3\64"+
		"\3\64\3\64\3\64\3\64\5\64\u030d\n\64\3\64\3\64\5\64\u0311\n\64\3\64\3"+
		"\64\3\64\5\64\u0316\n\64\3\64\3\64\5\64\u031a\n\64\5\64\u031c\n\64\3\65"+
		"\3\65\3\65\3\66\3\66\3\66\3\66\3\66\5\66\u0326\n\66\3\66\5\66\u0329\n"+
		"\66\3\67\3\67\3\67\38\38\39\39\39\3:\3:\3:\5:\u0336\n:\3:\3:\3:\5:\u033b"+
		"\n:\3:\5:\u033e\n:\3;\3;\5;\u0342\n;\3;\3;\3<\3<\5<\u0348\n<\3=\3=\5="+
		"\u034c\n=\3=\3=\3=\7=\u0351\n=\f=\16=\u0354\13=\3=\3=\3>\3>\5>\u035a\n"+
		">\3>\3>\3>\3?\3?\3?\5?\u0362\n?\3?\7?\u0365\n?\f?\16?\u0368\13?\3@\3@"+
		"\5@\u036c\n@\3@\5@\u036f\n@\3@\5@\u0372\n@\3@\5@\u0375\n@\3@\5@\u0378"+
		"\n@\3A\3A\3A\3A\3B\3B\3B\3C\3C\3C\3D\3D\3D\3D\3D\7D\u0389\nD\fD\16D\u038c"+
		"\13D\3D\3D\5D\u0390\nD\3D\5D\u0393\nD\3E\3E\3E\3F\3F\3F\3F\3F\7F\u039d"+
		"\nF\fF\16F\u03a0\13F\3G\3G\3G\5G\u03a5\nG\3G\5G\u03a8\nG\3H\3H\3H\3I\3"+
		"I\3I\3J\3J\3J\3J\3J\3J\7J\u03b6\nJ\fJ\16J\u03b9\13J\5J\u03bb\nJ\3J\3J"+
		"\3K\3K\3K\7K\u03c2\nK\fK\16K\u03c5\13K\3K\3K\3K\5K\u03ca\nK\3K\3K\7K\u03ce"+
		"\nK\fK\16K\u03d1\13K\5K\u03d3\nK\3L\3L\3L\3L\3M\3M\3M\3M\5M\u03dd\nM\3"+
		"N\3N\5N\u03e1\nN\3N\5N\u03e4\nN\3N\5N\u03e7\nN\3O\3O\3O\5O\u03ec\nO\3"+
		"O\5O\u03ef\nO\3O\5O\u03f2\nO\3P\3P\5P\u03f6\nP\3Q\3Q\3Q\7Q\u03fb\nQ\f"+
		"Q\16Q\u03fe\13Q\3R\3R\3R\3R\3R\2\6Z\\bdS\2\4\6\b\n\f\16\20\22\24\26\30"+
		"\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080"+
		"\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098"+
		"\u009a\u009c\u009e\u00a0\u00a2\2\17\5\2AA\u008a\u008a\u008c\u008c\3\2"+
		"GH\3\2\u00dc\u00dd\3\2\u00c9\u00ca\3\2\u00cd\u00ce\3\2\u00d3\u00d4\3\2"+
		"\u00e6\u00e9\3\2\u008a\u008b\3\2qw\3\2\u0103\u0105\4\2\n\n\u0109\u0109"+
		"\4\2\u010b\u010b\u0118\u0118\3\2\u0092\u0093\2\u046c\2\u00a5\3\2\2\2\4"+
		"\u00be\3\2\2\2\6\u00c0\3\2\2\2\b\u00cc\3\2\2\2\n\u00d1\3\2\2\2\f\u00dc"+
		"\3\2\2\2\16\u00e1\3\2\2\2\20\u00e4\3\2\2\2\22\u00e8\3\2\2\2\24\u00f0\3"+
		"\2\2\2\26\u00f6\3\2\2\2\30\u011c\3\2\2\2\32\u0127\3\2\2\2\34\u0129\3\2"+
		"\2\2\36\u0138\3\2\2\2 \u0140\3\2\2\2\"\u0143\3\2\2\2$\u0149\3\2\2\2&\u0152"+
		"\3\2\2\2(\u0155\3\2\2\2*\u015e\3\2\2\2,\u0160\3\2\2\2.\u0168\3\2\2\2\60"+
		"\u016c\3\2\2\2\62\u0171\3\2\2\2\64\u0176\3\2\2\2\66\u0178\3\2\2\28\u017a"+
		"\3\2\2\2:\u017c\3\2\2\2<\u017e\3\2\2\2>\u0180\3\2\2\2@\u0182\3\2\2\2B"+
		"\u0184\3\2\2\2D\u0186\3\2\2\2F\u019c\3\2\2\2H\u019e\3\2\2\2J\u01ac\3\2"+
		"\2\2L\u01ae\3\2\2\2N\u01c4\3\2\2\2P\u01de\3\2\2\2R\u01fe\3\2\2\2T\u0204"+
		"\3\2\2\2V\u0206\3\2\2\2X\u022e\3\2\2\2Z\u023a\3\2\2\2\\\u0250\3\2\2\2"+
		"^\u026a\3\2\2\2`\u02a6\3\2\2\2b\u02a8\3\2\2\2d\u02f5\3\2\2\2f\u031b\3"+
		"\2\2\2h\u031d\3\2\2\2j\u0328\3\2\2\2l\u032a\3\2\2\2n\u032d\3\2\2\2p\u032f"+
		"\3\2\2\2r\u033d\3\2\2\2t\u033f\3\2\2\2v\u0347\3\2\2\2x\u0349\3\2\2\2z"+
		"\u0357\3\2\2\2|\u035e\3\2\2\2~\u0369\3\2\2\2\u0080\u0379\3\2\2\2\u0082"+
		"\u037d\3\2\2\2\u0084\u0380\3\2\2\2\u0086\u0383\3\2\2\2\u0088\u0394\3\2"+
		"\2\2\u008a\u0397\3\2\2\2\u008c\u03a4\3\2\2\2\u008e\u03a9\3\2\2\2\u0090"+
		"\u03ac\3\2\2\2\u0092\u03af\3\2\2\2\u0094\u03d2\3\2\2\2\u0096\u03d4\3\2"+
		"\2\2\u0098\u03dc\3\2\2\2\u009a\u03de\3\2\2\2\u009c\u03e8\3\2\2\2\u009e"+
		"\u03f5\3\2\2\2\u00a0\u03f7\3\2\2\2\u00a2\u03ff\3\2\2\2\u00a4\u00a6\t\2"+
		"\2\2\u00a5\u00a4\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a8\3\2\2\2\u00a7"+
		"\u00a9\7B\2\2\u00a8\u00a7\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00ab\3\2"+
		"\2\2\u00aa\u00ac\7C\2\2\u00ab\u00aa\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac"+
		"\u00ae\3\2\2\2\u00ad\u00af\7D\2\2\u00ae\u00ad\3\2\2\2\u00ae\u00af\3\2"+
		"\2\2\u00af\u00b1\3\2\2\2\u00b0\u00b2\7E\2\2\u00b1\u00b0\3\2\2\2\u00b1"+
		"\u00b2\3\2\2\2\u00b2\u00b4\3\2\2\2\u00b3\u00b5\7F\2\2\u00b4\u00b3\3\2"+
		"\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b7\3\2\2\2\u00b6\u00b8\t\3\2\2\u00b7"+
		"\u00b6\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8\u00ba\3\2\2\2\u00b9\u00bb\7I"+
		"\2\2\u00ba\u00b9\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\3\3\2\2\2\u00bc\u00bf"+
		"\5\n\6\2\u00bd\u00bf\5\6\4\2\u00be\u00bc\3\2\2\2\u00be\u00bd\3\2\2\2\u00bf"+
		"\5\3\2\2\2\u00c0\u00c1\7\u00f1\2\2\u00c1\u00c3\5d\63\2\u00c2\u00c4\5\b"+
		"\5\2\u00c3\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c5"+
		"\u00c6\3\2\2\2\u00c6\u00c8\3\2\2\2\u00c7\u00c9\5\16\b\2\u00c8\u00c7\3"+
		"\2\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\u00cb\7\u00f6\2\2"+
		"\u00cb\7\3\2\2\2\u00cc\u00cd\7\u00f2\2\2\u00cd\u00ce\5d\63\2\u00ce\u00cf"+
		"\7\u00f3\2\2\u00cf\u00d0\5\20\t\2\u00d0\t\3\2\2\2\u00d1\u00d3\7\u00f1"+
		"\2\2\u00d2\u00d4\5\f\7\2\u00d3\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5"+
		"\u00d3\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d8\3\2\2\2\u00d7\u00d9\5\16"+
		"\b\2\u00d8\u00d7\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00da\3\2\2\2\u00da"+
		"\u00db\7\u00f6\2\2\u00db\13\3\2\2\2\u00dc\u00dd\7\u00f2\2\2\u00dd\u00de"+
		"\5\\/\2\u00de\u00df\7\u00f3\2\2\u00df\u00e0\5\20\t\2\u00e0\r\3\2\2\2\u00e1"+
		"\u00e2\7\u00f5\2\2\u00e2\u00e3\5\20\t\2\u00e3\17\3\2\2\2\u00e4\u00e5\5"+
		"Z.\2\u00e5\21\3\2\2\2\u00e6\u00e9\5b\62\2\u00e7\u00e9\5\4\3\2\u00e8\u00e6"+
		"\3\2\2\2\u00e8\u00e7\3\2\2\2\u00e9\u00eb\3\2\2\2\u00ea\u00ec\7\u0099\2"+
		"\2\u00eb\u00ea\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ee\3\2\2\2\u00ed\u00ef"+
		"\5@!\2\u00ee\u00ed\3\2\2\2\u00ee\u00ef\3\2\2\2\u00ef\23\3\2\2\2\u00f0"+
		"\u00f1\7\u00d9\2\2\u00f1\u00f4\5\32\16\2\u00f2\u00f5\5\30\r\2\u00f3\u00f5"+
		"\5\26\f\2\u00f4\u00f2\3\2\2\2\u00f4\u00f3\3\2\2\2\u00f5\25\3\2\2\2\u00f6"+
		"\u00f7\7\u008d\2\2\u00f7\u00f9\7\u0118\2\2\u00f8\u00fa\5\u0090I\2\u00f9"+
		"\u00f8\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\27\3\2\2\2\u00fb\u00fd\7\u0118"+
		"\2\2\u00fc\u00fe\7\3\2\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe"+
		"\u0106\3\2\2\2\u00ff\u0100\7|\2\2\u0100\u0102\7\u0118\2\2\u0101\u0103"+
		"\7\3\2\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2\2\u0103\u0105\3\2\2\2\u0104"+
		"\u00ff\3\2\2\2\u0105\u0108\3\2\2\2\u0106\u0104\3\2\2\2\u0106\u0107\3\2"+
		"\2\2\u0107\u0109\3\2\2\2\u0108\u0106\3\2\2\2\u0109\u010a\7\u008d\2\2\u010a"+
		"\u011d\5L\'\2\u010b\u010c\7\u008d\2\2\u010c\u010e\7\u0118\2\2\u010d\u010f"+
		"\7\3\2\2\u010e\u010d\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u0117\3\2\2\2\u0110"+
		"\u0111\7|\2\2\u0111\u0113\7\u0118\2\2\u0112\u0114\7\3\2\2\u0113\u0112"+
		"\3\2\2\2\u0113\u0114\3\2\2\2\u0114\u0116\3\2\2\2\u0115\u0110\3\2\2\2\u0116"+
		"\u0119\3\2\2\2\u0117\u0115\3\2\2\2\u0117\u0118\3\2\2\2\u0118\u011a\3\2"+
		"\2\2\u0119\u0117\3\2\2\2\u011a\u011b\7\u00d1\2\2\u011b\u011d\5L\'\2\u011c"+
		"\u00fb\3\2\2\2\u011c\u010b\3\2\2\2\u011d\31\3\2\2\2\u011e\u0120\7^\2\2"+
		"\u011f\u011e\3\2\2\2\u011f\u0120\3\2\2\2\u0120\u0128\3\2\2\2\u0121\u0123"+
		"\7\u00da\2\2\u0122\u0121\3\2\2\2\u0122\u0123\3\2\2\2\u0123\u0128\3\2\2"+
		"\2\u0124\u0126\7\u00d5\2\2\u0125\u0124\3\2\2\2\u0125\u0126\3\2\2\2\u0126"+
		"\u0128\3\2\2\2\u0127\u011f\3\2\2\2\u0127\u0122\3\2\2\2\u0127\u0125\3\2"+
		"\2\2\u0128\33\3\2\2\2\u0129\u012b\5\36\20\2\u012a\u012c\7\u009d\2\2\u012b"+
		"\u012a\3\2\2\2\u012b\u012c\3\2\2\2\u012c\u012d\3\2\2\2\u012d\u012f\7\u0118"+
		"\2\2\u012e\u0130\5\u0090I\2\u012f\u012e\3\2\2\2\u012f\u0130\3\2\2\2\u0130"+
		"\u0133\3\2\2\2\u0131\u0134\5&\24\2\u0132\u0134\5\"\22\2\u0133\u0131\3"+
		"\2\2\2\u0133\u0132\3\2\2\2\u0134\u0136\3\2\2\2\u0135\u0137\5(\25\2\u0136"+
		"\u0135\3\2\2\2\u0136\u0137\3\2\2\2\u0137\35\3\2\2\2\u0138\u013a\7\u00db"+
		"\2\2\u0139\u013b\5 \21\2\u013a\u0139\3\2\2\2\u013a\u013b\3\2\2\2\u013b"+
		"\37\3\2\2\2\u013c\u0141\7^\2\2\u013d\u0141\7_\2\2\u013e\u013f\7B\2\2\u013f"+
		"\u0141\7\u00d5\2\2\u0140\u013c\3\2\2\2\u0140\u013d\3\2\2\2\u0140\u013e"+
		"\3\2\2\2\u0141!\3\2\2\2\u0142\u0144\5J&\2\u0143\u0142\3\2\2\2\u0143\u0144"+
		"\3\2\2\2\u0144\u0147\3\2\2\2\u0145\u0148\5$\23\2\u0146\u0148\5v<\2\u0147"+
		"\u0145\3\2\2\2\u0147\u0146\3\2\2\2\u0148#\3\2\2\2\u0149\u014a\t\4\2\2"+
		"\u014a\u014f\5\u00a2R\2\u014b\u014c\7|\2\2\u014c\u014e\5\u00a2R\2\u014d"+
		"\u014b\3\2\2\2\u014e\u0151\3\2\2\2\u014f\u014d\3\2\2\2\u014f\u0150\3\2"+
		"\2\2\u0150%\3\2\2\2\u0151\u014f\3\2\2\2\u0152\u0153\7\u00bf\2\2\u0153"+
		"\u0154\5,\27\2\u0154\'\3\2\2\2\u0155\u0156\7\u00cc\2\2\u0156\u0157\7\u00de"+
		"\2\2\u0157\u0158\7\u00d4\2\2\u0158\u0159\7\u00c1\2\2\u0159\u015a\5,\27"+
		"\2\u015a)\3\2\2\2\u015b\u015c\7x\2\2\u015c\u015f\7y\2\2\u015d\u015f\5"+
		"D#\2\u015e\u015b\3\2\2\2\u015e\u015d\3\2\2\2\u015f+\3\2\2\2\u0160\u0165"+
		"\5.\30\2\u0161\u0162\7|\2\2\u0162\u0164\5.\30\2\u0163\u0161\3\2\2\2\u0164"+
		"\u0167\3\2\2\2\u0165\u0163\3\2\2\2\u0165\u0166\3\2\2\2\u0166-\3\2\2\2"+
		"\u0167\u0165\3\2\2\2\u0168\u0169\58\35\2\u0169\u016a\7q\2\2\u016a\u016b"+
		"\5\u009eP\2\u016b/\3\2\2\2\u016c\u016d\7\u00c1\2\2\u016d\u016e\5\62\32"+
		"\2\u016e\u016f\5L\'\2\u016f\61\3\2\2\2\u0170\u0172\7^\2\2\u0171\u0170"+
		"\3\2\2\2\u0171\u0172\3\2\2\2\u0172\u0174\3\2\2\2\u0173\u0175\7\u00d5\2"+
		"\2\u0174\u0173\3\2\2\2\u0174\u0175\3\2\2\2\u0175\63\3\2\2\2\u0176\u0177"+
		"\7\u0118\2\2\u0177\65\3\2\2\2\u0178\u0179\7\u0118\2\2\u0179\67\3\2\2\2"+
		"\u017a\u017b\7\u0118\2\2\u017b9\3\2\2\2\u017c\u017d\7\u0118\2\2\u017d"+
		";\3\2\2\2\u017e\u017f\7\u0118\2\2\u017f=\3\2\2\2\u0180\u0181\7\u0118\2"+
		"\2\u0181?\3\2\2\2\u0182\u0183\7\u0118\2\2\u0183A\3\2\2\2\u0184\u0185\7"+
		"\u0118\2\2\u0185C\3\2\2\2\u0186\u0187\7x\2\2\u0187\u018c\7\u0118\2\2\u0188"+
		"\u0189\7|\2\2\u0189\u018b\7\u0118\2\2\u018a\u0188\3\2\2\2\u018b\u018e"+
		"\3\2\2\2\u018c\u018a\3\2\2\2\u018c\u018d\3\2\2\2\u018d\u018f\3\2\2\2\u018e"+
		"\u018c\3\2\2\2\u018f\u0190\7y\2\2\u0190E\3\2\2\2\u0191\u0196\7\u0083\2"+
		"\2\u0192\u0193\7|\2\2\u0193\u0195\7\u0083\2\2\u0194\u0192\3\2\2\2\u0195"+
		"\u0198\3\2\2\2\u0196\u0194\3\2\2\2\u0196\u0197\3\2\2\2\u0197\u019d\3\2"+
		"\2\2\u0198\u0196\3\2\2\2\u0199\u019a\7\u0083\2\2\u019a\u019b\7\u009c\2"+
		"\2\u019b\u019d\7\u0083\2\2\u019c\u0191\3\2\2\2\u019c\u0199\3\2\2\2\u019d"+
		"G\3\2\2\2\u019e\u019f\7x\2\2\u019f\u01a4\58\35\2\u01a0\u01a1\7|\2\2\u01a1"+
		"\u01a3\58\35\2\u01a2\u01a0\3\2\2\2\u01a3\u01a6\3\2\2\2\u01a4\u01a2\3\2"+
		"\2\2\u01a4\u01a5\3\2\2\2\u01a5\u01a7\3\2\2\2\u01a6\u01a4\3\2\2\2\u01a7"+
		"\u01a8\7y\2\2\u01a8I\3\2\2\2\u01a9\u01aa\7x\2\2\u01aa\u01ad\7y\2\2\u01ab"+
		"\u01ad\5D#\2\u01ac\u01a9\3\2\2\2\u01ac\u01ab\3\2\2\2\u01adK\3\2\2\2\u01ae"+
		"\u01b3\5N(\2\u01af\u01b0\7|\2\2\u01b0\u01b2\5N(\2\u01b1\u01af\3\2\2\2"+
		"\u01b2\u01b5\3\2\2\2\u01b3\u01b1\3\2\2\2\u01b3\u01b4\3\2\2\2\u01b4M\3"+
		"\2\2\2\u01b5\u01b3\3\2\2\2\u01b6\u01b7\5P)\2\u01b7\u01b8\5R*\2\u01b8\u01ba"+
		"\3\2\2\2\u01b9\u01b6\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb\u01b9\3\2\2\2\u01bb"+
		"\u01bc\3\2\2\2\u01bc\u01c5\3\2\2\2\u01bd\u01bf\5P)\2\u01be\u01c0\5R*\2"+
		"\u01bf\u01be\3\2\2\2\u01c0\u01c1\3\2\2\2\u01c1\u01bf\3\2\2\2\u01c1\u01c2"+
		"\3\2\2\2\u01c2\u01c5\3\2\2\2\u01c3\u01c5\5P)\2\u01c4\u01b9\3\2\2\2\u01c4"+
		"\u01bd\3\2\2\2\u01c4\u01c3\3\2\2\2\u01c5O\3\2\2\2\u01c6\u01c9\5\66\34"+
		"\2\u01c7\u01c8\7\u008e\2\2\u01c8\u01ca\5D#\2\u01c9\u01c7\3\2\2\2\u01c9"+
		"\u01ca\3\2\2\2\u01ca\u01cf\3\2\2\2\u01cb\u01cd\7\u0099\2\2\u01cc\u01cb"+
		"\3\2\2\2\u01cc\u01cd\3\2\2\2\u01cd\u01ce\3\2\2\2\u01ce\u01d0\5@!\2\u01cf"+
		"\u01cc\3\2\2\2\u01cf\u01d0\3\2\2\2\u01d0\u01d2\3\2\2\2\u01d1\u01d3\5V"+
		",\2\u01d2\u01d1\3\2\2\2\u01d2\u01d3\3\2\2\2\u01d3\u01df\3\2\2\2\u01d4"+
		"\u01d6\5\u0096L\2\u01d5\u01d7\7\u0099\2\2\u01d6\u01d5\3\2\2\2\u01d6\u01d7"+
		"\3\2\2\2\u01d7\u01d8\3\2\2\2\u01d8\u01d9\5@!\2\u01d9\u01df\3\2\2\2\u01da"+
		"\u01db\7x\2\2\u01db\u01dc\5L\'\2\u01dc\u01dd\7y\2\2\u01dd\u01df\3\2\2"+
		"\2\u01de\u01c6\3\2\2\2\u01de\u01d4\3\2\2\2\u01de\u01da\3\2\2\2\u01dfQ"+
		"\3\2\2\2\u01e0\u01e2\t\5\2\2\u01e1\u01e0\3\2\2\2\u01e1\u01e2\3\2\2\2\u01e2"+
		"\u01e3\3\2\2\2\u01e3\u01e4\7\u00cb\2\2\u01e4\u01e6\5P)\2\u01e5\u01e7\5"+
		"T+\2\u01e6\u01e5\3\2\2\2\u01e6\u01e7\3\2\2\2\u01e7\u01ff\3\2\2\2\u01e8"+
		"\u01e9\7C\2\2\u01e9\u01ff\5P)\2\u01ea\u01eb\7C\2\2\u01eb\u01ec\5P)\2\u01ec"+
		"\u01ed\5T+\2\u01ed\u01ff\3\2\2\2\u01ee\u01f0\t\6\2\2\u01ef\u01f1\7\u00cf"+
		"\2\2\u01f0\u01ef\3\2\2\2\u01f0\u01f1\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2"+
		"\u01f3\7\u00cb\2\2\u01f3\u01f4\5P)\2\u01f4\u01f5\5T+\2\u01f5\u01ff\3\2"+
		"\2\2\u01f6\u01fa\7\u00d0\2\2\u01f7\u01fb\7\u00c9\2\2\u01f8\u01f9\t\6\2"+
		"\2\u01f9\u01fb\7\u00cf\2\2\u01fa\u01f7\3\2\2\2\u01fa\u01f8\3\2\2\2\u01fa"+
		"\u01fb\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc\u01fd\7\u00cb\2\2\u01fd\u01ff"+
		"\5P)\2\u01fe\u01e1\3\2\2\2\u01fe\u01e8\3\2\2\2\u01fe\u01ea\3\2\2\2\u01fe"+
		"\u01ee\3\2\2\2\u01fe\u01f6\3\2\2\2\u01ffS\3\2\2\2\u0200\u0201\7\u00cc"+
		"\2\2\u0201\u0205\5Z.\2\u0202\u0203\7\u00d1\2\2\u0203\u0205\5D#\2\u0204"+
		"\u0200\3\2\2\2\u0204\u0202\3\2\2\2\u0205U\3\2\2\2\u0206\u020b\5X-\2\u0207"+
		"\u0208\7|\2\2\u0208\u020a\5X-\2\u0209\u0207\3\2\2\2\u020a\u020d\3\2\2"+
		"\2\u020b\u0209\3\2\2\2\u020b\u020c\3\2\2\2\u020cW\3\2\2\2\u020d\u020b"+
		"\3\2\2\2\u020e\u020f\7\u00d2\2\2\u020f\u021a\t\7\2\2\u0210\u0216\7\u00c0"+
		"\2\2\u0211\u0217\7\u00cb\2\2\u0212\u0213\7\u009a\2\2\u0213\u0217\7\u0091"+
		"\2\2\u0214\u0215\7\u0090\2\2\u0215\u0217\7\u0091\2\2\u0216\u0211\3\2\2"+
		"\2\u0216\u0212\3\2\2\2\u0216\u0214\3\2\2\2\u0217\u0219\3\2\2\2\u0218\u0210"+
		"\3\2\2\2\u0219\u021c\3\2\2\2\u021a\u0218\3\2\2\2\u021a\u021b\3\2\2\2\u021b"+
		"\u021d\3\2\2\2\u021c\u021a\3\2\2\2\u021d\u022f\5D#\2\u021e\u021f\7\u00d5"+
		"\2\2\u021f\u022a\t\7\2\2\u0220\u0226\7\u00c0\2\2\u0221\u0227\7\u00cb\2"+
		"\2\u0222\u0223\7\u009a\2\2\u0223\u0227\7\u0091\2\2\u0224\u0225\7\u0090"+
		"\2\2\u0225\u0227\7\u0091\2\2\u0226\u0221\3\2\2\2\u0226\u0222\3\2\2\2\u0226"+
		"\u0224\3\2\2\2\u0227\u0229\3\2\2\2\u0228\u0220\3\2\2\2\u0229\u022c\3\2"+
		"\2\2\u022a\u0228\3\2\2\2\u022a\u022b\3\2\2\2\u022b\u022d\3\2\2\2\u022c"+
		"\u022a\3\2\2\2\u022d\u022f\5D#\2\u022e\u020e\3\2\2\2\u022e\u021e\3\2\2"+
		"\2\u022fY\3\2\2\2\u0230\u0231\b.\1\2\u0231\u0232\7x\2\2\u0232\u0233\5"+
		"Z.\2\u0233\u0234\7y\2\2\u0234\u023b\3\2\2\2\u0235\u0236\7\u00e4\2\2\u0236"+
		"\u023b\5Z.\5\u0237\u0238\7b\2\2\u0238\u023b\5Z.\4\u0239\u023b\5\\/\2\u023a"+
		"\u0230\3\2\2\2\u023a\u0235\3\2\2\2\u023a\u0237\3\2\2\2\u023a\u0239\3\2"+
		"\2\2\u023b\u024d\3\2\2\2\u023c\u023d\f\13\2\2\u023d\u023e\7\u00e2\2\2"+
		"\u023e\u024c\5Z.\f\u023f\u0240\f\n\2\2\u0240\u0241\7a\2\2\u0241\u024c"+
		"\5Z.\13\u0242\u0243\f\t\2\2\u0243\u0244\7\u00e3\2\2\u0244\u024c\5Z.\n"+
		"\u0245\u0246\f\b\2\2\u0246\u0247\7\u00e1\2\2\u0247\u024c\5Z.\t\u0248\u0249"+
		"\f\7\2\2\u0249\u024a\7`\2\2\u024a\u024c\5Z.\b\u024b\u023c\3\2\2\2\u024b"+
		"\u023f\3\2\2\2\u024b\u0242\3\2\2\2\u024b\u0245\3\2\2\2\u024b\u0248\3\2"+
		"\2\2\u024c\u024f\3\2\2\2\u024d\u024b\3\2\2\2\u024d\u024e\3\2\2\2\u024e"+
		"[\3\2\2\2\u024f\u024d\3\2\2\2\u0250\u0251\b/\1\2\u0251\u0252\5`\61\2\u0252"+
		"\u0267\3\2\2\2\u0253\u0254\f\7\2\2\u0254\u0256\7\u00e0\2\2\u0255\u0257"+
		"\7\u00e4\2\2\u0256\u0255\3\2\2\2\u0256\u0257\3\2\2\2\u0257\u0258\3\2\2"+
		"\2\u0258\u0266\t\b\2\2\u0259\u025a\f\6\2\2\u025a\u025b\7o\2\2\u025b\u0266"+
		"\5`\61\2\u025c\u025d\f\5\2\2\u025d\u025e\5^\60\2\u025e\u025f\5`\61\2\u025f"+
		"\u0266\3\2\2\2\u0260\u0261\f\4\2\2\u0261\u0262\5^\60\2\u0262\u0263\t\t"+
		"\2\2\u0263\u0264\5\u0096L\2\u0264\u0266\3\2\2\2\u0265\u0253\3\2\2\2\u0265"+
		"\u0259\3\2\2\2\u0265\u025c\3\2\2\2\u0265\u0260\3\2\2\2\u0266\u0269\3\2"+
		"\2\2\u0267\u0265\3\2\2\2\u0267\u0268\3\2\2\2\u0268]\3\2\2\2\u0269\u0267"+
		"\3\2\2\2\u026a\u026b\t\n\2\2\u026b_\3\2\2\2\u026c\u026e\5b\62\2\u026d"+
		"\u026f\7\u00e4\2\2\u026e\u026d\3\2\2\2\u026e\u026f\3\2\2\2\u026f\u0270"+
		"\3\2\2\2\u0270\u0271\7\u00c7\2\2\u0271\u0272\5\u0096L\2\u0272\u02a7\3"+
		"\2\2\2\u0273\u0275\5b\62\2\u0274\u0276\7\u00e4\2\2\u0275\u0274\3\2\2\2"+
		"\u0275\u0276\3\2\2\2\u0276\u0277\3\2\2\2\u0277\u0278\7\u00c7\2\2\u0278"+
		"\u0279\7x\2\2\u0279\u027e\5d\63\2\u027a\u027b\7|\2\2\u027b\u027d\5d\63"+
		"\2\u027c\u027a\3\2\2\2\u027d\u0280\3\2\2\2\u027e\u027c\3\2\2\2\u027e\u027f"+
		"\3\2\2\2\u027f\u0281\3\2\2\2\u0280\u027e\3\2\2\2\u0281\u0282\7y\2\2\u0282"+
		"\u02a7\3\2\2\2\u0283\u0285\5b\62\2\u0284\u0286\7\u00e4\2\2\u0285\u0284"+
		"\3\2\2\2\u0285\u0286\3\2\2\2\u0286\u0287\3\2\2\2\u0287\u0288\7\u00e5\2"+
		"\2\u0288\u0289\5d\63\2\u0289\u028a\7\u00e1\2\2\u028a\u028b\5`\61\2\u028b"+
		"\u02a7\3\2\2\2\u028c\u028d\5b\62\2\u028d\u028e\7\u00ea\2\2\u028e\u028f"+
		"\7\u00eb\2\2\u028f\u0290\5d\63\2\u0290\u02a7\3\2\2\2\u0291\u0293\5b\62"+
		"\2\u0292\u0294\7\u00e4\2\2\u0293\u0292\3\2\2\2\u0293\u0294\3\2\2\2\u0294"+
		"\u0295\3\2\2\2\u0295\u0296\7\u00eb\2\2\u0296\u029b\5d\63\2\u0297\u0298"+
		"\7\u00ef\2\2\u0298\u029a\5d\63\2\u0299\u0297\3\2\2\2\u029a\u029d\3\2\2"+
		"\2\u029b\u0299\3\2\2\2\u029b\u029c\3\2\2\2\u029c\u02a7\3\2\2\2\u029d\u029b"+
		"\3\2\2\2\u029e\u02a0\5b\62\2\u029f\u02a1\7\u00e4\2\2\u02a0\u029f\3\2\2"+
		"\2\u02a0\u02a1\3\2\2\2\u02a1\u02a2\3\2\2\2\u02a2\u02a3\7\u00f0\2\2\u02a3"+
		"\u02a4\5d\63\2\u02a4\u02a7\3\2\2\2\u02a5\u02a7\5b\62\2\u02a6\u026c\3\2"+
		"\2\2\u02a6\u0273\3\2\2\2\u02a6\u0283\3\2\2\2\u02a6\u028c\3\2\2\2\u02a6"+
		"\u0291\3\2\2\2\u02a6\u029e\3\2\2\2\u02a6\u02a5\3\2\2\2\u02a7a\3\2\2\2"+
		"\u02a8\u02a9\b\62\1\2\u02a9\u02aa\5d\63\2\u02aa\u02d1\3\2\2\2\u02ab\u02ac"+
		"\f\17\2\2\u02ac\u02ad\7d\2\2\u02ad\u02d0\5b\62\20\u02ae\u02af\f\16\2\2"+
		"\u02af\u02b0\7e\2\2\u02b0\u02d0\5b\62\17\u02b1\u02b2\f\r\2\2\u02b2\u02b3"+
		"\7f\2\2\u02b3\u02d0\5b\62\16\u02b4\u02b5\f\f\2\2\u02b5\u02b6\7g\2\2\u02b6"+
		"\u02d0\5b\62\r\u02b7\u02b8\f\13\2\2\u02b8\u02b9\7j\2\2\u02b9\u02d0\5b"+
		"\62\f\u02ba\u02bb\f\n\2\2\u02bb\u02bc\7k\2\2\u02bc\u02d0\5b\62\13\u02bd"+
		"\u02be\f\t\2\2\u02be\u02bf\7l\2\2\u02bf\u02d0\5b\62\n\u02c0\u02c1\f\b"+
		"\2\2\u02c1\u02c2\7m\2\2\u02c2\u02d0\5b\62\t\u02c3\u02c4\f\7\2\2\u02c4"+
		"\u02c5\7\u00ec\2\2\u02c5\u02d0\5b\62\b\u02c6\u02c7\f\6\2\2\u02c7\u02c8"+
		"\7\u00ed\2\2\u02c8\u02d0\5b\62\7\u02c9\u02ca\f\5\2\2\u02ca\u02cb\7i\2"+
		"\2\u02cb\u02d0\5b\62\6\u02cc\u02cd\f\4\2\2\u02cd\u02ce\7h\2\2\u02ce\u02d0"+
		"\5b\62\5\u02cf\u02ab\3\2\2\2\u02cf\u02ae\3\2\2\2\u02cf\u02b1\3\2\2\2\u02cf"+
		"\u02b4\3\2\2\2\u02cf\u02b7\3\2\2\2\u02cf\u02ba\3\2\2\2\u02cf\u02bd\3\2"+
		"\2\2\u02cf\u02c0\3\2\2\2\u02cf\u02c3\3\2\2\2\u02cf\u02c6\3\2\2\2\u02cf"+
		"\u02c9\3\2\2\2\u02cf\u02cc\3\2\2\2\u02d0\u02d3\3\2\2\2\u02d1\u02cf\3\2"+
		"\2\2\u02d1\u02d2\3\2\2\2\u02d2c\3\2\2\2\u02d3\u02d1\3\2\2\2\u02d4\u02f6"+
		"\b\63\1\2\u02d5\u02f6\5\u0092J\2\u02d6\u02f6\5f\64\2\u02d7\u02f6\7\u0118"+
		"\2\2\u02d8\u02d9\7j\2\2\u02d9\u02f6\5d\63\13\u02da\u02db\7k\2\2\u02db"+
		"\u02f6\5d\63\n\u02dc\u02dd\7c\2\2\u02dd\u02f6\5d\63\t\u02de\u02df\7b\2"+
		"\2\u02df\u02f6\5d\63\b\u02e0\u02e1\7\u010b\2\2\u02e1\u02f6\5d\63\7\u02e2"+
		"\u02e3\7x\2\2\u02e3\u02e4\5Z.\2\u02e4\u02e5\7y\2\2\u02e5\u02f6\3\2\2\2"+
		"\u02e6\u02e7\7\u00ee\2\2\u02e7\u02e8\7x\2\2\u02e8\u02ed\5d\63\2\u02e9"+
		"\u02ea\7|\2\2\u02ea\u02ec\5d\63\2\u02eb\u02e9\3\2\2\2\u02ec\u02ef\3\2"+
		"\2\2\u02ed\u02eb\3\2\2\2\u02ed\u02ee\3\2\2\2\u02ee\u02f0\3\2\2\2\u02ef"+
		"\u02ed\3\2\2\2\u02f0\u02f1\7y\2\2\u02f1\u02f6\3\2\2\2\u02f2\u02f6\5\u0096"+
		"L\2\u02f3\u02f4\7\u00df\2\2\u02f4\u02f6\5\u0096L\2\u02f5\u02d4\3\2\2\2"+
		"\u02f5\u02d5\3\2\2\2\u02f5\u02d6\3\2\2\2\u02f5\u02d7\3\2\2\2\u02f5\u02d8"+
		"\3\2\2\2\u02f5\u02da\3\2\2\2\u02f5\u02dc\3\2\2\2\u02f5\u02de\3\2\2\2\u02f5"+
		"\u02e0\3\2\2\2\u02f5\u02e2\3\2\2\2\u02f5\u02e6\3\2\2\2\u02f5\u02f2\3\2"+
		"\2\2\u02f5\u02f3\3\2\2\2\u02f6\u02fe\3\2\2\2\u02f7\u02f8\f\f\2\2\u02f8"+
		"\u02f9\7`\2\2\u02f9\u02fd\5d\63\r\u02fa\u02fb\f\r\2\2\u02fb\u02fd\5l\67"+
		"\2\u02fc\u02f7\3\2\2\2\u02fc\u02fa\3\2\2\2\u02fd\u0300\3\2\2\2\u02fe\u02fc"+
		"\3\2\2\2\u02fe\u02ff\3\2\2\2\u02ffe\3\2\2\2\u0300\u02fe\3\2\2\2\u0301"+
		"\u031c\7\u0081\2\2\u0302\u031c\7\u0083\2\2\u0303\u031c\7\u00e7\2\2\u0304"+
		"\u031c\7\u00e8\2\2\u0305\u031c\7\u00e6\2\2\u0306\u0307\7z\2\2\u0307\u0308"+
		"\7\u0118\2\2\u0308\u0309\7\u0082\2\2\u0309\u031c\7{\2\2\u030a\u031c\7"+
		"\u0086\2\2\u030b\u030d\7\u0118\2\2\u030c\u030b\3\2\2\2\u030c\u030d\3\2"+
		"\2\2\u030d\u030e\3\2\2\2\u030e\u0310\7\u0082\2\2\u030f\u0311\5l\67\2\u0310"+
		"\u030f\3\2\2\2\u0310\u0311\3\2\2\2\u0311\u031c\3\2\2\2\u0312\u0313\t\13"+
		"\2\2\u0313\u031c\7\u0082\2\2\u0314\u0316\7\u0118\2\2\u0315\u0314\3\2\2"+
		"\2\u0315\u0316\3\2\2\2\u0316\u0317\3\2\2\2\u0317\u0319\7\u0087\2\2\u0318"+
		"\u031a\5l\67\2\u0319\u0318\3\2\2\2\u0319\u031a\3\2\2\2\u031a\u031c\3\2"+
		"\2\2\u031b\u0301\3\2\2\2\u031b\u0302\3\2\2\2\u031b\u0303\3\2\2\2\u031b"+
		"\u0304\3\2\2\2\u031b\u0305\3\2\2\2\u031b\u0306\3\2\2\2\u031b\u030a\3\2"+
		"\2\2\u031b\u030c\3\2\2\2\u031b\u0312\3\2\2\2\u031b\u0315\3\2\2\2\u031c"+
		"g\3\2\2\2\u031d\u031e\5j\66\2\u031e\u031f\5l\67\2\u031fi\3\2\2\2\u0320"+
		"\u0321\t\f\2\2\u0321\u0322\7\u00bf\2\2\u0322\u0329\5n8\2\u0323\u0325\7"+
		"\13\2\2\u0324\u0326\7q\2\2\u0325\u0324\3\2\2\2\u0325\u0326\3\2\2\2\u0326"+
		"\u0327\3\2\2\2\u0327\u0329\5n8\2\u0328\u0320\3\2\2\2\u0328\u0323\3\2\2"+
		"\2\u0329k\3\2\2\2\u032a\u032b\7\f\2\2\u032b\u032c\7\u0118\2\2\u032cm\3"+
		"\2\2\2\u032d\u032e\t\r\2\2\u032eo\3\2\2\2\u032f\u0330\5r:\2\u0330\u0331"+
		"\5t;\2\u0331q\3\2\2\2\u0332\u0333\t\f\2\2\u0333\u0335\7\u00bf\2\2\u0334"+
		"\u0336\7q\2\2\u0335\u0334\3\2\2\2\u0335\u0336\3\2\2\2\u0336\u0337\3\2"+
		"\2\2\u0337\u033e\5n8\2\u0338\u033a\7\13\2\2\u0339\u033b\7q\2\2\u033a\u0339"+
		"\3\2\2\2\u033a\u033b\3\2\2\2\u033b\u033c\3\2\2\2\u033c\u033e\5n8\2\u033d"+
		"\u0332\3\2\2\2\u033d\u0338\3\2\2\2\u033es\3\2\2\2\u033f\u0341\7\f\2\2"+
		"\u0340\u0342\7q\2\2\u0341\u0340\3\2\2\2\u0341\u0342\3\2\2\2\u0342\u0343"+
		"\3\2\2\2\u0343\u0344\7\u0118\2\2\u0344u\3\2\2\2\u0345\u0348\5x=\2\u0346"+
		"\u0348\5|?\2\u0347\u0345\3\2\2\2\u0347\u0346\3\2\2\2\u0348w\3\2\2\2\u0349"+
		"\u034b\7\u0094\2\2\u034a\u034c\7\u0095\2\2\u034b\u034a\3\2\2\2\u034b\u034c"+
		"\3\2\2\2\u034c\u034d\3\2\2\2\u034d\u0352\5z>\2\u034e\u034f\7|\2\2\u034f"+
		"\u0351\5z>\2\u0350\u034e\3\2\2\2\u0351\u0354\3\2\2\2\u0352\u0350\3\2\2"+
		"\2\u0352\u0353\3\2\2\2\u0353\u0355\3\2\2\2\u0354\u0352\3\2\2\2\u0355\u0356"+
		"\5|?\2\u0356y\3\2\2\2\u0357\u0359\5B\"\2\u0358\u035a\5D#\2\u0359\u0358"+
		"\3\2\2\2\u0359\u035a\3\2\2\2\u035a\u035b\3\2\2\2\u035b\u035c\7\u0099\2"+
		"\2\u035c\u035d\5\u0096L\2\u035d{\3\2\2\2\u035e\u0366\5~@\2\u035f\u0361"+
		"\7\u00d7\2\2\u0360\u0362\7\u008a\2\2\u0361\u0360\3\2\2\2\u0361\u0362\3"+
		"\2\2\2\u0362\u0363\3\2\2\2\u0363\u0365\5~@\2\u0364\u035f\3\2\2\2\u0365"+
		"\u0368\3\2\2\2\u0366\u0364\3\2\2\2\u0366\u0367\3\2\2\2\u0367}\3\2\2\2"+
		"\u0368\u0366\3\2\2\2\u0369\u036b\5\u0080A\2\u036a\u036c\5\u0082B\2\u036b"+
		"\u036a\3\2\2\2\u036b\u036c\3\2\2\2\u036c\u036e\3\2\2\2\u036d\u036f\5\u0084"+
		"C\2\u036e\u036d\3\2\2\2\u036e\u036f\3\2\2\2\u036f\u0371\3\2\2\2\u0370"+
		"\u0372\5\u0086D\2\u0371\u0370\3\2\2\2\u0371\u0372\3\2\2\2\u0372\u0374"+
		"\3\2\2\2\u0373\u0375\5\u008aF\2\u0374\u0373\3\2\2\2\u0374\u0375\3\2\2"+
		"\2\u0375\u0377\3\2\2\2\u0376\u0378\5\u008eH\2\u0377\u0376\3\2\2\2\u0377"+
		"\u0378\3\2\2\2\u0378\177\3\2\2\2\u0379\u037a\7\u0089\2\2\u037a\u037b\5"+
		"\2\2\2\u037b\u037c\5\u0094K\2\u037c\u0081\3\2\2\2\u037d\u037e\7\u008d"+
		"\2\2\u037e\u037f\5L\'\2\u037f\u0083\3\2\2\2\u0380\u0381\7\u008f\2\2\u0381"+
		"\u0382\5Z.\2\u0382\u0085\3\2\2\2\u0383\u0384\7\u0090\2\2\u0384\u0385\7"+
		"\u0091\2\2\u0385\u038a\5\u008cG\2\u0386\u0387\7|\2\2\u0387\u0389\5\u008c"+
		"G\2\u0388\u0386\3\2\2\2\u0389\u038c\3\2\2\2\u038a\u0388\3\2\2\2\u038a"+
		"\u038b\3\2\2\2\u038b\u038f\3\2\2\2\u038c\u038a\3\2\2\2\u038d\u038e\7\u0094"+
		"\2\2\u038e\u0390\7\u0096\2\2\u038f\u038d\3\2\2\2\u038f\u0390\3\2\2\2\u0390"+
		"\u0392\3\2\2\2\u0391\u0393\5\u0088E\2\u0392\u0391\3\2\2\2\u0392\u0393"+
		"\3\2\2\2\u0393\u0087\3\2\2\2\u0394\u0395\7\u0097\2\2\u0395\u0396\5Z.\2"+
		"\u0396\u0089\3\2\2\2\u0397\u0398\7\u009a\2\2\u0398\u0399\7\u0091\2\2\u0399"+
		"\u039e\5\u008cG\2\u039a\u039b\7|\2\2\u039b\u039d\5\u008cG\2\u039c\u039a"+
		"\3\2\2\2\u039d\u03a0\3\2\2\2\u039e\u039c\3\2\2\2\u039e\u039f\3\2\2\2\u039f"+
		"\u008b\3\2\2\2\u03a0\u039e\3\2\2\2\u03a1\u03a5\58\35\2\u03a2\u03a5\7\u0083"+
		"\2\2\u03a3\u03a5\5Z.\2\u03a4\u03a1\3\2\2\2\u03a4\u03a2\3\2\2\2\u03a4\u03a3"+
		"\3\2\2\2\u03a5\u03a7\3\2\2\2\u03a6\u03a8\t\16\2\2\u03a7\u03a6\3\2\2\2"+
		"\u03a7\u03a8\3\2\2\2\u03a8\u008d\3\2\2\2\u03a9\u03aa\7\u009b\2\2\u03aa"+
		"\u03ab\5F$\2\u03ab\u008f\3\2\2\2\u03ac\u03ad\7\u008e\2\2\u03ad\u03ae\5"+
		"D#\2\u03ae\u0091\3\2\2\2\u03af\u03b0\7\u0118\2\2\u03b0\u03ba\7x\2\2\u03b1"+
		"\u03bb\3\2\2\2\u03b2\u03b7\5Z.\2\u03b3\u03b4\7|\2\2\u03b4\u03b6\5Z.\2"+
		"\u03b5\u03b3\3\2\2\2\u03b6\u03b9\3\2\2\2\u03b7\u03b5\3\2\2\2\u03b7\u03b8"+
		"\3\2\2\2\u03b8\u03bb\3\2\2\2\u03b9\u03b7\3\2\2\2\u03ba\u03b1\3\2\2\2\u03ba"+
		"\u03b2\3\2\2\2\u03bb\u03bc\3\2\2\2\u03bc\u03bd\7y\2\2\u03bd\u0093\3\2"+
		"\2\2\u03be\u03c3\7l\2\2\u03bf\u03c0\7|\2\2\u03c0\u03c2\5\22\n\2\u03c1"+
		"\u03bf\3\2\2\2\u03c2\u03c5\3\2\2\2\u03c3\u03c1\3\2\2\2\u03c3\u03c4\3\2"+
		"\2\2\u03c4\u03d3\3\2\2\2\u03c5\u03c3\3\2\2\2\u03c6\u03c9\5\22\n\2\u03c7"+
		"\u03c8\7|\2\2\u03c8\u03ca\7l\2\2\u03c9\u03c7\3\2\2\2\u03c9\u03ca\3\2\2"+
		"\2\u03ca\u03cf\3\2\2\2\u03cb\u03cc\7|\2\2\u03cc\u03ce\5\22\n\2\u03cd\u03cb"+
		"\3\2\2\2\u03ce\u03d1\3\2\2\2\u03cf\u03cd\3\2\2\2\u03cf\u03d0\3\2\2\2\u03d0"+
		"\u03d3\3\2\2\2\u03d1\u03cf\3\2\2\2\u03d2\u03be\3\2\2\2\u03d2\u03c6\3\2"+
		"\2\2\u03d3\u0095\3\2\2\2\u03d4\u03d5\7x\2\2\u03d5\u03d6\5|?\2\u03d6\u03d7"+
		"\7y\2\2\u03d7\u0097\3\2\2\2\u03d8\u03dd\5v<\2\u03d9\u03dd\5\34\17\2\u03da"+
		"\u03dd\5\u009cO\2\u03db\u03dd\5\u009aN\2\u03dc\u03d8\3\2\2\2\u03dc\u03d9"+
		"\3\2\2\2\u03dc\u03da\3\2\2\2\u03dc\u03db\3\2\2\2\u03dd\u0099\3\2\2\2\u03de"+
		"\u03e0\5\24\13\2\u03df\u03e1\5\u0084C\2\u03e0\u03df\3\2\2\2\u03e0\u03e1"+
		"\3\2\2\2\u03e1\u03e3\3\2\2\2\u03e2\u03e4\5\u008aF\2\u03e3\u03e2\3\2\2"+
		"\2\u03e3\u03e4\3\2\2\2\u03e4\u03e6\3\2\2\2\u03e5\u03e7\5\u008eH\2\u03e6"+
		"\u03e5\3\2\2\2\u03e6\u03e7\3\2\2\2\u03e7\u009b\3\2\2\2\u03e8\u03e9\5\60"+
		"\31\2\u03e9\u03eb\5&\24\2\u03ea\u03ec\5\u0084C\2\u03eb\u03ea\3\2\2\2\u03eb"+
		"\u03ec\3\2\2\2\u03ec\u03ee\3\2\2\2\u03ed\u03ef\5\u008aF\2\u03ee\u03ed"+
		"\3\2\2\2\u03ee\u03ef\3\2\2\2\u03ef\u03f1\3\2\2\2\u03f0\u03f2\5\u008eH"+
		"\2\u03f1\u03f0\3\2\2\2\u03f1\u03f2\3\2\2\2\u03f2\u009d\3\2\2\2\u03f3\u03f6"+
		"\7\u00d8\2\2\u03f4\u03f6\5Z.\2\u03f5\u03f3\3\2\2\2\u03f5\u03f4\3\2\2\2"+
		"\u03f6\u009f\3\2\2\2\u03f7\u03fc\5\u009eP\2\u03f8\u03f9\7|\2\2\u03f9\u03fb"+
		"\5\u009eP\2\u03fa\u03f8\3\2\2\2\u03fb\u03fe\3\2\2\2\u03fc\u03fa\3\2\2"+
		"\2\u03fc\u03fd\3\2\2\2\u03fd\u00a1\3\2\2\2\u03fe\u03fc\3\2\2\2\u03ff\u0400"+
		"\7x\2\2\u0400\u0401\5\u00a0Q\2\u0401\u0402\7y\2\2\u0402\u00a3\3\2\2\2"+
		"\u0086\u00a5\u00a8\u00ab\u00ae\u00b1\u00b4\u00b7\u00ba\u00be\u00c5\u00c8"+
		"\u00d5\u00d8\u00e8\u00eb\u00ee\u00f4\u00f9\u00fd\u0102\u0106\u010e\u0113"+
		"\u0117\u011c\u011f\u0122\u0125\u0127\u012b\u012f\u0133\u0136\u013a\u0140"+
		"\u0143\u0147\u014f\u015e\u0165\u0171\u0174\u018c\u0196\u019c\u01a4\u01ac"+
		"\u01b3\u01bb\u01c1\u01c4\u01c9\u01cc\u01cf\u01d2\u01d6\u01de\u01e1\u01e6"+
		"\u01f0\u01fa\u01fe\u0204\u020b\u0216\u021a\u0226\u022a\u022e\u023a\u024b"+
		"\u024d\u0256\u0265\u0267\u026e\u0275\u027e\u0285\u0293\u029b\u02a0\u02a6"+
		"\u02cf\u02d1\u02ed\u02f5\u02fc\u02fe\u030c\u0310\u0315\u0319\u031b\u0325"+
		"\u0328\u0335\u033a\u033d\u0341\u0347\u034b\u0352\u0359\u0361\u0366\u036b"+
		"\u036e\u0371\u0374\u0377\u038a\u038f\u0392\u039e\u03a4\u03a7\u03b7\u03ba"+
		"\u03c3\u03c9\u03cf\u03d2\u03dc\u03e0\u03e3\u03e6\u03eb\u03ee\u03f1\u03f5"+
		"\u03fc";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}