// Generated from MySQLDDL.g4 by ANTLR 4.7.1

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
public class MySQLDDLParser extends Parser {
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
		RULE_createIndex = 0, RULE_dropIndex = 1, RULE_dropTable = 2, RULE_truncateTable = 3, 
		RULE_createTableOptions = 4, RULE_createTableBasic = 5, RULE_createDefinitions = 6, 
		RULE_createDefinition = 7, RULE_checkExpr = 8, RULE_createTableSelect = 9, 
		RULE_createTableLike = 10, RULE_likeTable = 11, RULE_alterSpecifications = 12, 
		RULE_alterSpecification = 13, RULE_changeColumn = 14, RULE_changeColumnOp = 15, 
		RULE_dropColumn = 16, RULE_dropIndexDef = 17, RULE_dropPrimaryKey = 18, 
		RULE_renameIndex = 19, RULE_renameTable = 20, RULE_modifyColumn = 21, 
		RULE_algorithmOption = 22, RULE_lockOption = 23, RULE_indexDefinition = 24, 
		RULE_indexAndKey = 25, RULE_indexDefOption = 26, RULE_singleColumn = 27, 
		RULE_multiColumn = 28, RULE_constraintDefinition = 29, RULE_primaryKeyOption = 30, 
		RULE_uniqueOption = 31, RULE_foreignKeyOption = 32, RULE_tableOptions = 33, 
		RULE_tableOption = 34, RULE_columnNameAndDefinition = 35, RULE_columnDefinition = 36, 
		RULE_dataType = 37, RULE_timestampType = 38, RULE_currentTimestampType = 39, 
		RULE_dataTypeLength = 40, RULE_dataTypeLengthWithPrecision = 41, RULE_numberTypeSuffix = 42, 
		RULE_dataTypeOption = 43, RULE_dataTypeGenerated = 44, RULE_referenceDefinition = 45, 
		RULE_referenceOption = 46, RULE_symbol = 47, RULE_fkSymbol = 48, RULE_keyParts = 49, 
		RULE_defaultValue = 50, RULE_keyPart = 51, RULE_indexName = 52, RULE_indexType = 53, 
		RULE_indexOption = 54, RULE_parserName = 55, RULE_engineName = 56, RULE_partitionNames = 57, 
		RULE_partitionName = 58, RULE_partitionOptions = 59, RULE_linearPartition = 60, 
		RULE_yearFunctionExpr = 61, RULE_keyColumnList = 62, RULE_exprWithParen = 63, 
		RULE_rangeOrListPartition = 64, RULE_exprOrColumns = 65, RULE_partitionDefinitions = 66, 
		RULE_partitionDefinition = 67, RULE_lessThanPartition = 68, RULE_subpartitionDefinition = 69, 
		RULE_value = 70, RULE_valueList = 71, RULE_valueListWithParen = 72, RULE_schemaName = 73, 
		RULE_tableName = 74, RULE_columnName = 75, RULE_tablespaceName = 76, RULE_collationName = 77, 
		RULE_alias = 78, RULE_cteName = 79, RULE_idList = 80, RULE_rangeClause = 81, 
		RULE_columnList = 82, RULE_selectSpec = 83, RULE_caseExpress = 84, RULE_caseComp = 85, 
		RULE_caseWhenComp = 86, RULE_caseCond = 87, RULE_whenResult = 88, RULE_elseResult = 89, 
		RULE_caseResult = 90, RULE_idListWithEmpty = 91, RULE_tableReferences = 92, 
		RULE_tableReference = 93, RULE_tableFactor = 94, RULE_joinTable = 95, 
		RULE_joinCondition = 96, RULE_indexHintList = 97, RULE_indexHint = 98, 
		RULE_expr = 99, RULE_booleanPrimary = 100, RULE_comparisonOperator = 101, 
		RULE_predicate = 102, RULE_bitExpr = 103, RULE_simpleExpr = 104, RULE_liter = 105, 
		RULE_characterAndCollate = 106, RULE_characterSet = 107, RULE_collateClause = 108, 
		RULE_charsetName = 109, RULE_characterAndCollateWithEqual = 110, RULE_characterSetWithEqual = 111, 
		RULE_collateClauseWithEqual = 112, RULE_selectExpr = 113, RULE_select = 114, 
		RULE_withClause = 115, RULE_cteClause = 116, RULE_unionSelect = 117, RULE_selectExpression = 118, 
		RULE_selectClause = 119, RULE_fromClause = 120, RULE_whereClause = 121, 
		RULE_groupByClause = 122, RULE_havingClause = 123, RULE_orderByClause = 124, 
		RULE_groupByItem = 125, RULE_limitClause = 126, RULE_partitionClause = 127, 
		RULE_functionCall = 128, RULE_selectExprs = 129, RULE_subquery = 130, 
		RULE_execute = 131, RULE_alterTable = 132, RULE_prefixTableName = 133, 
		RULE_createTable = 134;
	public static final String[] ruleNames = {
		"createIndex", "dropIndex", "dropTable", "truncateTable", "createTableOptions", 
		"createTableBasic", "createDefinitions", "createDefinition", "checkExpr", 
		"createTableSelect", "createTableLike", "likeTable", "alterSpecifications", 
		"alterSpecification", "changeColumn", "changeColumnOp", "dropColumn", 
		"dropIndexDef", "dropPrimaryKey", "renameIndex", "renameTable", "modifyColumn", 
		"algorithmOption", "lockOption", "indexDefinition", "indexAndKey", "indexDefOption", 
		"singleColumn", "multiColumn", "constraintDefinition", "primaryKeyOption", 
		"uniqueOption", "foreignKeyOption", "tableOptions", "tableOption", "columnNameAndDefinition", 
		"columnDefinition", "dataType", "timestampType", "currentTimestampType", 
		"dataTypeLength", "dataTypeLengthWithPrecision", "numberTypeSuffix", "dataTypeOption", 
		"dataTypeGenerated", "referenceDefinition", "referenceOption", "symbol", 
		"fkSymbol", "keyParts", "defaultValue", "keyPart", "indexName", "indexType", 
		"indexOption", "parserName", "engineName", "partitionNames", "partitionName", 
		"partitionOptions", "linearPartition", "yearFunctionExpr", "keyColumnList", 
		"exprWithParen", "rangeOrListPartition", "exprOrColumns", "partitionDefinitions", 
		"partitionDefinition", "lessThanPartition", "subpartitionDefinition", 
		"value", "valueList", "valueListWithParen", "schemaName", "tableName", 
		"columnName", "tablespaceName", "collationName", "alias", "cteName", "idList", 
		"rangeClause", "columnList", "selectSpec", "caseExpress", "caseComp", 
		"caseWhenComp", "caseCond", "whenResult", "elseResult", "caseResult", 
		"idListWithEmpty", "tableReferences", "tableReference", "tableFactor", 
		"joinTable", "joinCondition", "indexHintList", "indexHint", "expr", "booleanPrimary", 
		"comparisonOperator", "predicate", "bitExpr", "simpleExpr", "liter", "characterAndCollate", 
		"characterSet", "collateClause", "charsetName", "characterAndCollateWithEqual", 
		"characterSetWithEqual", "collateClauseWithEqual", "selectExpr", "select", 
		"withClause", "cteClause", "unionSelect", "selectExpression", "selectClause", 
		"fromClause", "whereClause", "groupByClause", "havingClause", "orderByClause", 
		"groupByItem", "limitClause", "partitionClause", "functionCall", "selectExprs", 
		"subquery", "execute", "alterTable", "prefixTableName", "createTable"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'LZ4'", null, null, null, null, null, null, null, null, null, null, 
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
	public String getGrammarFileName() { return "MySQLDDL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MySQLDDLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class CreateIndexContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(MySQLDDLParser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public TerminalNode ON() { return getToken(MySQLDDLParser.ON, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public KeyPartsContext keyParts() {
			return getRuleContext(KeyPartsContext.class,0);
		}
		public IndexTypeContext indexType() {
			return getRuleContext(IndexTypeContext.class,0);
		}
		public IndexOptionContext indexOption() {
			return getRuleContext(IndexOptionContext.class,0);
		}
		public List<AlgorithmOptionContext> algorithmOption() {
			return getRuleContexts(AlgorithmOptionContext.class);
		}
		public AlgorithmOptionContext algorithmOption(int i) {
			return getRuleContext(AlgorithmOptionContext.class,i);
		}
		public List<LockOptionContext> lockOption() {
			return getRuleContexts(LockOptionContext.class);
		}
		public LockOptionContext lockOption(int i) {
			return getRuleContext(LockOptionContext.class,i);
		}
		public TerminalNode UNIQUE() { return getToken(MySQLDDLParser.UNIQUE, 0); }
		public TerminalNode FULLTEXT() { return getToken(MySQLDDLParser.FULLTEXT, 0); }
		public TerminalNode SPATIAL() { return getToken(MySQLDDLParser.SPATIAL, 0); }
		public CreateIndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createIndex; }
	}

	public final CreateIndexContext createIndex() throws RecognitionException {
		CreateIndexContext _localctx = new CreateIndexContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_createIndex);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			match(CREATE);
			setState(272);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SPATIAL || _la==UNIQUE || _la==FULLTEXT) {
				{
				setState(271);
				_la = _input.LA(1);
				if ( !(_la==SPATIAL || _la==UNIQUE || _la==FULLTEXT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(274);
			match(INDEX);
			setState(275);
			indexName();
			setState(277);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(276);
				indexType();
				}
			}

			setState(279);
			match(ON);
			setState(280);
			tableName();
			setState(281);
			keyParts();
			setState(283);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY_BLOCK_SIZE || _la==COMMENT || _la==WITH || _la==USING) {
				{
				setState(282);
				indexOption();
				}
			}

			setState(289);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ALGORITHM || _la==LOCK) {
				{
				setState(287);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ALGORITHM:
					{
					setState(285);
					algorithmOption();
					}
					break;
				case LOCK:
					{
					setState(286);
					lockOption();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(291);
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

	public static class DropIndexContext extends ParserRuleContext {
		public DropIndexDefContext dropIndexDef() {
			return getRuleContext(DropIndexDefContext.class,0);
		}
		public TerminalNode ON() { return getToken(MySQLDDLParser.ON, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public List<AlgorithmOptionContext> algorithmOption() {
			return getRuleContexts(AlgorithmOptionContext.class);
		}
		public AlgorithmOptionContext algorithmOption(int i) {
			return getRuleContext(AlgorithmOptionContext.class,i);
		}
		public List<LockOptionContext> lockOption() {
			return getRuleContexts(LockOptionContext.class);
		}
		public LockOptionContext lockOption(int i) {
			return getRuleContext(LockOptionContext.class,i);
		}
		public DropIndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropIndex; }
	}

	public final DropIndexContext dropIndex() throws RecognitionException {
		DropIndexContext _localctx = new DropIndexContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_dropIndex);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(292);
			dropIndexDef();
			setState(293);
			match(ON);
			setState(294);
			tableName();
			setState(299);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ALGORITHM || _la==LOCK) {
				{
				setState(297);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ALGORITHM:
					{
					setState(295);
					algorithmOption();
					}
					break;
				case LOCK:
					{
					setState(296);
					lockOption();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(301);
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

	public static class DropTableContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(MySQLDDLParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(MySQLDDLParser.TABLE, 0); }
		public List<TableNameContext> tableName() {
			return getRuleContexts(TableNameContext.class);
		}
		public TableNameContext tableName(int i) {
			return getRuleContext(TableNameContext.class,i);
		}
		public TerminalNode TEMPORARY() { return getToken(MySQLDDLParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(MySQLDDLParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(MySQLDDLParser.EXISTS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public TerminalNode RESTRICT() { return getToken(MySQLDDLParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(MySQLDDLParser.CASCADE, 0); }
		public DropTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropTable; }
	}

	public final DropTableContext dropTable() throws RecognitionException {
		DropTableContext _localctx = new DropTableContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_dropTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(302);
			match(DROP);
			setState(304);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(303);
				match(TEMPORARY);
				}
			}

			setState(306);
			match(TABLE);
			setState(309);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(307);
				match(IF);
				setState(308);
				match(EXISTS);
				}
			}

			setState(311);
			tableName();
			setState(316);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(312);
				match(COMMA);
				setState(313);
				tableName();
				}
				}
				setState(318);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(320);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RESTRICT || _la==CASCADE) {
				{
				setState(319);
				_la = _input.LA(1);
				if ( !(_la==RESTRICT || _la==CASCADE) ) {
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

	public static class TruncateTableContext extends ParserRuleContext {
		public TerminalNode TRUNCATE() { return getToken(MySQLDDLParser.TRUNCATE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(MySQLDDLParser.TABLE, 0); }
		public TruncateTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_truncateTable; }
	}

	public final TruncateTableContext truncateTable() throws RecognitionException {
		TruncateTableContext _localctx = new TruncateTableContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_truncateTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(322);
			match(TRUNCATE);
			setState(324);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TABLE) {
				{
				setState(323);
				match(TABLE);
				}
			}

			setState(326);
			tableName();
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

	public static class CreateTableOptionsContext extends ParserRuleContext {
		public CreateTableBasicContext createTableBasic() {
			return getRuleContext(CreateTableBasicContext.class,0);
		}
		public CreateTableSelectContext createTableSelect() {
			return getRuleContext(CreateTableSelectContext.class,0);
		}
		public CreateTableLikeContext createTableLike() {
			return getRuleContext(CreateTableLikeContext.class,0);
		}
		public CreateTableOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableOptions; }
	}

	public final CreateTableOptionsContext createTableOptions() throws RecognitionException {
		CreateTableOptionsContext _localctx = new CreateTableOptionsContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_createTableOptions);
		try {
			setState(331);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(328);
				createTableBasic();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(329);
				createTableSelect();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(330);
				createTableLike();
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

	public static class CreateTableBasicContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public CreateDefinitionsContext createDefinitions() {
			return getRuleContext(CreateDefinitionsContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TableOptionsContext tableOptions() {
			return getRuleContext(TableOptionsContext.class,0);
		}
		public PartitionOptionsContext partitionOptions() {
			return getRuleContext(PartitionOptionsContext.class,0);
		}
		public CreateTableBasicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableBasic; }
	}

	public final CreateTableBasicContext createTableBasic() throws RecognitionException {
		CreateTableBasicContext _localctx = new CreateTableBasicContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_createTableBasic);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			match(LEFT_PAREN);
			setState(334);
			createDefinitions();
			setState(335);
			match(RIGHT_PAREN);
			setState(337);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CHARACTER) | (1L << CHARSET) | (1L << COLLATE) | (1L << KEY_BLOCK_SIZE) | (1L << COMMENT) | (1L << AUTO_INCREMENT) | (1L << AVG_ROW_LENGTH) | (1L << CHECKSUM) | (1L << COMPRESSION) | (1L << CONNECTION) | (1L << DATA) | (1L << DELAY_KEY_WRITE) | (1L << ENCRYPTION) | (1L << ENGINE) | (1L << INSERT_METHOD) | (1L << MAX_ROWS) | (1L << MIN_ROWS) | (1L << PACK_KEYS) | (1L << PASSWORD) | (1L << ROW_FORMAT) | (1L << STATS_AUTO_RECALC) | (1L << STATS_PERSISTENT) | (1L << STATS_SAMPLE_PAGES))) != 0) || ((((_la - 174)) & ~0x3f) == 0 && ((1L << (_la - 174)) & ((1L << (TABLESPACE - 174)) | (1L << (INDEX - 174)) | (1L << (UNION - 174)) | (1L << (DEFAULT - 174)))) != 0) || _la==CHAR) {
				{
				setState(336);
				tableOptions();
				}
			}

			setState(340);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(339);
				partitionOptions();
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

	public static class CreateDefinitionsContext extends ParserRuleContext {
		public List<CreateDefinitionContext> createDefinition() {
			return getRuleContexts(CreateDefinitionContext.class);
		}
		public CreateDefinitionContext createDefinition(int i) {
			return getRuleContext(CreateDefinitionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public CreateDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createDefinitions; }
	}

	public final CreateDefinitionsContext createDefinitions() throws RecognitionException {
		CreateDefinitionsContext _localctx = new CreateDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_createDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			createDefinition();
			setState(347);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(343);
				match(COMMA);
				setState(344);
				createDefinition();
				}
				}
				setState(349);
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

	public static class CreateDefinitionContext extends ParserRuleContext {
		public ColumnNameAndDefinitionContext columnNameAndDefinition() {
			return getRuleContext(ColumnNameAndDefinitionContext.class,0);
		}
		public ConstraintDefinitionContext constraintDefinition() {
			return getRuleContext(ConstraintDefinitionContext.class,0);
		}
		public IndexDefinitionContext indexDefinition() {
			return getRuleContext(IndexDefinitionContext.class,0);
		}
		public CheckExprContext checkExpr() {
			return getRuleContext(CheckExprContext.class,0);
		}
		public CreateDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createDefinition; }
	}

	public final CreateDefinitionContext createDefinition() throws RecognitionException {
		CreateDefinitionContext _localctx = new CreateDefinitionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_createDefinition);
		try {
			setState(356);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(350);
				columnNameAndDefinition();
				}
				}
				break;
			case SPATIAL:
			case CONSTRAINT:
			case UNIQUE:
			case FULLTEXT:
			case FOREIGN:
			case CHECK:
			case PRIMARY:
			case INDEX:
			case KEY:
				enterOuterAlt(_localctx, 2);
				{
				setState(354);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CONSTRAINT:
				case UNIQUE:
				case FOREIGN:
				case PRIMARY:
					{
					setState(351);
					constraintDefinition();
					}
					break;
				case SPATIAL:
				case FULLTEXT:
				case INDEX:
				case KEY:
					{
					setState(352);
					indexDefinition();
					}
					break;
				case CHECK:
					{
					setState(353);
					checkExpr();
					}
					break;
				default:
					throw new NoViableAltException(this);
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

	public static class CheckExprContext extends ParserRuleContext {
		public TerminalNode CHECK() { return getToken(MySQLDDLParser.CHECK, 0); }
		public ExprWithParenContext exprWithParen() {
			return getRuleContext(ExprWithParenContext.class,0);
		}
		public CheckExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_checkExpr; }
	}

	public final CheckExprContext checkExpr() throws RecognitionException {
		CheckExprContext _localctx = new CheckExprContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_checkExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(358);
			match(CHECK);
			setState(359);
			exprWithParen();
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

	public static class CreateTableSelectContext extends ParserRuleContext {
		public UnionSelectContext unionSelect() {
			return getRuleContext(UnionSelectContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public CreateDefinitionsContext createDefinitions() {
			return getRuleContext(CreateDefinitionsContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TableOptionsContext tableOptions() {
			return getRuleContext(TableOptionsContext.class,0);
		}
		public PartitionOptionsContext partitionOptions() {
			return getRuleContext(PartitionOptionsContext.class,0);
		}
		public TerminalNode AS() { return getToken(MySQLDDLParser.AS, 0); }
		public TerminalNode IGNORE() { return getToken(MySQLDDLParser.IGNORE, 0); }
		public TerminalNode REPLACE() { return getToken(MySQLDDLParser.REPLACE, 0); }
		public CreateTableSelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableSelect; }
	}

	public final CreateTableSelectContext createTableSelect() throws RecognitionException {
		CreateTableSelectContext _localctx = new CreateTableSelectContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_createTableSelect);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(365);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(361);
				match(LEFT_PAREN);
				setState(362);
				createDefinitions();
				setState(363);
				match(RIGHT_PAREN);
				}
			}

			setState(368);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CHARACTER) | (1L << CHARSET) | (1L << COLLATE) | (1L << KEY_BLOCK_SIZE) | (1L << COMMENT) | (1L << AUTO_INCREMENT) | (1L << AVG_ROW_LENGTH) | (1L << CHECKSUM) | (1L << COMPRESSION) | (1L << CONNECTION) | (1L << DATA) | (1L << DELAY_KEY_WRITE) | (1L << ENCRYPTION) | (1L << ENGINE) | (1L << INSERT_METHOD) | (1L << MAX_ROWS) | (1L << MIN_ROWS) | (1L << PACK_KEYS) | (1L << PASSWORD) | (1L << ROW_FORMAT) | (1L << STATS_AUTO_RECALC) | (1L << STATS_PERSISTENT) | (1L << STATS_SAMPLE_PAGES))) != 0) || ((((_la - 174)) & ~0x3f) == 0 && ((1L << (_la - 174)) & ((1L << (TABLESPACE - 174)) | (1L << (INDEX - 174)) | (1L << (UNION - 174)) | (1L << (DEFAULT - 174)))) != 0) || _la==CHAR) {
				{
				setState(367);
				tableOptions();
				}
			}

			setState(371);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1+1:
				{
				setState(370);
				partitionOptions();
				}
				break;
			}
			setState(374);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IGNORE || _la==REPLACE) {
				{
				setState(373);
				_la = _input.LA(1);
				if ( !(_la==IGNORE || _la==REPLACE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(376);
				match(AS);
				}
			}

			setState(379);
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

	public static class CreateTableLikeContext extends ParserRuleContext {
		public LikeTableContext likeTable() {
			return getRuleContext(LikeTableContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public CreateTableLikeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableLike; }
	}

	public final CreateTableLikeContext createTableLike() throws RecognitionException {
		CreateTableLikeContext _localctx = new CreateTableLikeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_createTableLike);
		try {
			setState(386);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LIKE:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(381);
				likeTable();
				}
				}
				break;
			case LEFT_PAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(382);
				match(LEFT_PAREN);
				setState(383);
				likeTable();
				setState(384);
				match(RIGHT_PAREN);
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

	public static class LikeTableContext extends ParserRuleContext {
		public TerminalNode LIKE() { return getToken(MySQLDDLParser.LIKE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public LikeTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_likeTable; }
	}

	public final LikeTableContext likeTable() throws RecognitionException {
		LikeTableContext _localctx = new LikeTableContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_likeTable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			match(LIKE);
			setState(389);
			tableName();
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

	public static class AlterSpecificationsContext extends ParserRuleContext {
		public List<AlterSpecificationContext> alterSpecification() {
			return getRuleContexts(AlterSpecificationContext.class);
		}
		public AlterSpecificationContext alterSpecification(int i) {
			return getRuleContext(AlterSpecificationContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public AlterSpecificationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterSpecifications; }
	}

	public final AlterSpecificationsContext alterSpecifications() throws RecognitionException {
		AlterSpecificationsContext _localctx = new AlterSpecificationsContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_alterSpecifications);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(391);
			alterSpecification();
			setState(396);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(392);
				match(COMMA);
				setState(393);
				alterSpecification();
				}
				}
				setState(398);
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

	public static class AlterSpecificationContext extends ParserRuleContext {
		public TableOptionsContext tableOptions() {
			return getRuleContext(TableOptionsContext.class,0);
		}
		public TerminalNode ADD() { return getToken(MySQLDDLParser.ADD, 0); }
		public SingleColumnContext singleColumn() {
			return getRuleContext(SingleColumnContext.class,0);
		}
		public MultiColumnContext multiColumn() {
			return getRuleContext(MultiColumnContext.class,0);
		}
		public TerminalNode COLUMN() { return getToken(MySQLDDLParser.COLUMN, 0); }
		public IndexDefinitionContext indexDefinition() {
			return getRuleContext(IndexDefinitionContext.class,0);
		}
		public ConstraintDefinitionContext constraintDefinition() {
			return getRuleContext(ConstraintDefinitionContext.class,0);
		}
		public AlgorithmOptionContext algorithmOption() {
			return getRuleContext(AlgorithmOptionContext.class,0);
		}
		public TerminalNode ALTER() { return getToken(MySQLDDLParser.ALTER, 0); }
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode SET() { return getToken(MySQLDDLParser.SET, 0); }
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public TerminalNode DROP() { return getToken(MySQLDDLParser.DROP, 0); }
		public ChangeColumnContext changeColumn() {
			return getRuleContext(ChangeColumnContext.class,0);
		}
		public CharacterAndCollateWithEqualContext characterAndCollateWithEqual() {
			return getRuleContext(CharacterAndCollateWithEqualContext.class,0);
		}
		public TerminalNode CONVERT() { return getToken(MySQLDDLParser.CONVERT, 0); }
		public TerminalNode TO() { return getToken(MySQLDDLParser.TO, 0); }
		public CharacterAndCollateContext characterAndCollate() {
			return getRuleContext(CharacterAndCollateContext.class,0);
		}
		public TerminalNode KEYS() { return getToken(MySQLDDLParser.KEYS, 0); }
		public TerminalNode DISABLE() { return getToken(MySQLDDLParser.DISABLE, 0); }
		public TerminalNode ENABLE() { return getToken(MySQLDDLParser.ENABLE, 0); }
		public TerminalNode TABLESPACE() { return getToken(MySQLDDLParser.TABLESPACE, 0); }
		public TerminalNode DISCARD() { return getToken(MySQLDDLParser.DISCARD, 0); }
		public TerminalNode IMPORT_() { return getToken(MySQLDDLParser.IMPORT_, 0); }
		public DropColumnContext dropColumn() {
			return getRuleContext(DropColumnContext.class,0);
		}
		public DropIndexDefContext dropIndexDef() {
			return getRuleContext(DropIndexDefContext.class,0);
		}
		public DropPrimaryKeyContext dropPrimaryKey() {
			return getRuleContext(DropPrimaryKeyContext.class,0);
		}
		public TerminalNode FOREIGN() { return getToken(MySQLDDLParser.FOREIGN, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public FkSymbolContext fkSymbol() {
			return getRuleContext(FkSymbolContext.class,0);
		}
		public TerminalNode FORCE() { return getToken(MySQLDDLParser.FORCE, 0); }
		public LockOptionContext lockOption() {
			return getRuleContext(LockOptionContext.class,0);
		}
		public ModifyColumnContext modifyColumn() {
			return getRuleContext(ModifyColumnContext.class,0);
		}
		public List<TerminalNode> ORDER() { return getTokens(MySQLDDLParser.ORDER); }
		public TerminalNode ORDER(int i) {
			return getToken(MySQLDDLParser.ORDER, i);
		}
		public List<TerminalNode> BY() { return getTokens(MySQLDDLParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(MySQLDDLParser.BY, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public RenameIndexContext renameIndex() {
			return getRuleContext(RenameIndexContext.class,0);
		}
		public RenameTableContext renameTable() {
			return getRuleContext(RenameTableContext.class,0);
		}
		public TerminalNode VALIDATION() { return getToken(MySQLDDLParser.VALIDATION, 0); }
		public TerminalNode WITHOUT() { return getToken(MySQLDDLParser.WITHOUT, 0); }
		public List<TerminalNode> WITH() { return getTokens(MySQLDDLParser.WITH); }
		public TerminalNode WITH(int i) {
			return getToken(MySQLDDLParser.WITH, i);
		}
		public TerminalNode PARTITION() { return getToken(MySQLDDLParser.PARTITION, 0); }
		public PartitionDefinitionsContext partitionDefinitions() {
			return getRuleContext(PartitionDefinitionsContext.class,0);
		}
		public PartitionNamesContext partitionNames() {
			return getRuleContext(PartitionNamesContext.class,0);
		}
		public TerminalNode ALL() { return getToken(MySQLDDLParser.ALL, 0); }
		public TerminalNode TRUNCATE() { return getToken(MySQLDDLParser.TRUNCATE, 0); }
		public TerminalNode COALESCE() { return getToken(MySQLDDLParser.COALESCE, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode REORGANIZE() { return getToken(MySQLDDLParser.REORGANIZE, 0); }
		public TerminalNode INTO() { return getToken(MySQLDDLParser.INTO, 0); }
		public TerminalNode EXCHANGE() { return getToken(MySQLDDLParser.EXCHANGE, 0); }
		public PartitionNameContext partitionName() {
			return getRuleContext(PartitionNameContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(MySQLDDLParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode ANALYZE() { return getToken(MySQLDDLParser.ANALYZE, 0); }
		public TerminalNode CHECK() { return getToken(MySQLDDLParser.CHECK, 0); }
		public TerminalNode OPTIMIZE() { return getToken(MySQLDDLParser.OPTIMIZE, 0); }
		public TerminalNode REBUILD() { return getToken(MySQLDDLParser.REBUILD, 0); }
		public TerminalNode REPAIR() { return getToken(MySQLDDLParser.REPAIR, 0); }
		public TerminalNode REMOVE() { return getToken(MySQLDDLParser.REMOVE, 0); }
		public TerminalNode PARTITIONING() { return getToken(MySQLDDLParser.PARTITIONING, 0); }
		public TerminalNode UPGRADE() { return getToken(MySQLDDLParser.UPGRADE, 0); }
		public AlterSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterSpecification; }
	}

	public final AlterSpecificationContext alterSpecification() throws RecognitionException {
		AlterSpecificationContext _localctx = new AlterSpecificationContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_alterSpecification);
		int _la;
		try {
			int _alt;
			setState(543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(399);
				tableOptions();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(400);
				match(ADD);
				setState(402);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLUMN) {
					{
					setState(401);
					match(COLUMN);
					}
				}

				setState(406);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(404);
					singleColumn();
					}
					break;
				case LEFT_PAREN:
					{
					setState(405);
					multiColumn();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(408);
				match(ADD);
				setState(409);
				indexDefinition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(410);
				match(ADD);
				setState(411);
				constraintDefinition();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(412);
				algorithmOption();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(413);
				match(ALTER);
				setState(415);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLUMN) {
					{
					setState(414);
					match(COLUMN);
					}
				}

				setState(417);
				columnName();
				setState(422);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SET:
					{
					setState(418);
					match(SET);
					setState(419);
					match(DEFAULT);
					}
					break;
				case DROP:
					{
					setState(420);
					match(DROP);
					setState(421);
					match(DEFAULT);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(424);
				changeColumn();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(426);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(425);
					match(DEFAULT);
					}
				}

				setState(428);
				characterAndCollateWithEqual();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(429);
				match(CONVERT);
				setState(430);
				match(TO);
				setState(431);
				characterAndCollate();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(432);
				_la = _input.LA(1);
				if ( !(_la==ENABLE || _la==DISABLE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(433);
				match(KEYS);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(434);
				_la = _input.LA(1);
				if ( !(_la==DISCARD || _la==IMPORT_) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(435);
				match(TABLESPACE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(436);
				dropColumn();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(437);
				dropIndexDef();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(438);
				dropPrimaryKey();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(439);
				match(DROP);
				setState(440);
				match(FOREIGN);
				setState(441);
				match(KEY);
				setState(442);
				fkSymbol();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(443);
				match(FORCE);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(444);
				lockOption();
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(445);
				modifyColumn();
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(456); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(446);
					match(ORDER);
					setState(447);
					match(BY);
					setState(448);
					columnName();
					setState(453);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(449);
							match(COMMA);
							setState(450);
							columnName();
							}
							} 
						}
						setState(455);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
					}
					}
					}
					setState(458); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==ORDER );
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(460);
				renameIndex();
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(461);
				renameTable();
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(462);
				_la = _input.LA(1);
				if ( !(_la==WITH || _la==WITHOUT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(463);
				match(VALIDATION);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(464);
				match(ADD);
				setState(465);
				match(PARTITION);
				setState(466);
				partitionDefinitions();
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(467);
				match(DROP);
				setState(468);
				match(PARTITION);
				setState(469);
				partitionNames();
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(470);
				match(DISCARD);
				setState(471);
				match(PARTITION);
				setState(474);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(472);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(473);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(476);
				match(TABLESPACE);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(477);
				match(IMPORT_);
				setState(478);
				match(PARTITION);
				setState(481);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(479);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(480);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(483);
				match(TABLESPACE);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(484);
				match(TRUNCATE);
				setState(485);
				match(PARTITION);
				setState(488);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(486);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(487);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(490);
				match(COALESCE);
				setState(491);
				match(PARTITION);
				setState(492);
				match(NUMBER);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(493);
				match(REORGANIZE);
				setState(494);
				match(PARTITION);
				setState(495);
				partitionNames();
				setState(496);
				match(INTO);
				setState(497);
				partitionDefinitions();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(499);
				match(EXCHANGE);
				setState(500);
				match(PARTITION);
				setState(501);
				partitionName();
				setState(502);
				match(WITH);
				setState(503);
				match(TABLE);
				setState(504);
				tableName();
				setState(507);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH || _la==WITHOUT) {
					{
					setState(505);
					_la = _input.LA(1);
					if ( !(_la==WITH || _la==WITHOUT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(506);
					match(VALIDATION);
					}
				}

				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(509);
				match(ANALYZE);
				setState(510);
				match(PARTITION);
				setState(513);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(511);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(512);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(515);
				match(CHECK);
				setState(516);
				match(PARTITION);
				setState(519);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(517);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(518);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(521);
				match(OPTIMIZE);
				setState(522);
				match(PARTITION);
				setState(525);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(523);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(524);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(527);
				match(REBUILD);
				setState(528);
				match(PARTITION);
				setState(531);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(529);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(530);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(533);
				match(REPAIR);
				setState(534);
				match(PARTITION);
				setState(537);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(535);
					partitionNames();
					}
					break;
				case ALL:
					{
					setState(536);
					match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(539);
				match(REMOVE);
				setState(540);
				match(PARTITIONING);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(541);
				match(UPGRADE);
				setState(542);
				match(PARTITIONING);
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

	public static class ChangeColumnContext extends ParserRuleContext {
		public ChangeColumnOpContext changeColumnOp() {
			return getRuleContext(ChangeColumnOpContext.class,0);
		}
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public ColumnDefinitionContext columnDefinition() {
			return getRuleContext(ColumnDefinitionContext.class,0);
		}
		public TerminalNode FIRST() { return getToken(MySQLDDLParser.FIRST, 0); }
		public TerminalNode AFTER() { return getToken(MySQLDDLParser.AFTER, 0); }
		public ChangeColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_changeColumn; }
	}

	public final ChangeColumnContext changeColumn() throws RecognitionException {
		ChangeColumnContext _localctx = new ChangeColumnContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_changeColumn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(545);
			changeColumnOp();
			setState(546);
			columnName();
			setState(547);
			columnName();
			setState(548);
			columnDefinition();
			setState(552);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				{
				setState(549);
				match(FIRST);
				}
				break;
			case AFTER:
				{
				setState(550);
				match(AFTER);
				setState(551);
				columnName();
				}
				break;
			case EOF:
			case COMMA:
			case PARTITION:
				break;
			default:
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

	public static class ChangeColumnOpContext extends ParserRuleContext {
		public TerminalNode CHANGE() { return getToken(MySQLDDLParser.CHANGE, 0); }
		public TerminalNode COLUMN() { return getToken(MySQLDDLParser.COLUMN, 0); }
		public ChangeColumnOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_changeColumnOp; }
	}

	public final ChangeColumnOpContext changeColumnOp() throws RecognitionException {
		ChangeColumnOpContext _localctx = new ChangeColumnOpContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_changeColumnOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(554);
			match(CHANGE);
			setState(556);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLUMN) {
				{
				setState(555);
				match(COLUMN);
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

	public static class DropColumnContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(MySQLDDLParser.DROP, 0); }
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode COLUMN() { return getToken(MySQLDDLParser.COLUMN, 0); }
		public DropColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropColumn; }
	}

	public final DropColumnContext dropColumn() throws RecognitionException {
		DropColumnContext _localctx = new DropColumnContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_dropColumn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(558);
			match(DROP);
			setState(560);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLUMN) {
				{
				setState(559);
				match(COLUMN);
				}
			}

			setState(562);
			columnName();
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

	public static class DropIndexDefContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(MySQLDDLParser.DROP, 0); }
		public IndexAndKeyContext indexAndKey() {
			return getRuleContext(IndexAndKeyContext.class,0);
		}
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public DropIndexDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropIndexDef; }
	}

	public final DropIndexDefContext dropIndexDef() throws RecognitionException {
		DropIndexDefContext _localctx = new DropIndexDefContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_dropIndexDef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(564);
			match(DROP);
			setState(565);
			indexAndKey();
			setState(566);
			indexName();
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

	public static class DropPrimaryKeyContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(MySQLDDLParser.DROP, 0); }
		public TerminalNode PRIMARY() { return getToken(MySQLDDLParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public DropPrimaryKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropPrimaryKey; }
	}

	public final DropPrimaryKeyContext dropPrimaryKey() throws RecognitionException {
		DropPrimaryKeyContext _localctx = new DropPrimaryKeyContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_dropPrimaryKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(568);
			match(DROP);
			setState(569);
			match(PRIMARY);
			setState(570);
			match(KEY);
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

	public static class RenameIndexContext extends ParserRuleContext {
		public TerminalNode RENAME() { return getToken(MySQLDDLParser.RENAME, 0); }
		public IndexAndKeyContext indexAndKey() {
			return getRuleContext(IndexAndKeyContext.class,0);
		}
		public List<IndexNameContext> indexName() {
			return getRuleContexts(IndexNameContext.class);
		}
		public IndexNameContext indexName(int i) {
			return getRuleContext(IndexNameContext.class,i);
		}
		public TerminalNode TO() { return getToken(MySQLDDLParser.TO, 0); }
		public RenameIndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_renameIndex; }
	}

	public final RenameIndexContext renameIndex() throws RecognitionException {
		RenameIndexContext _localctx = new RenameIndexContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_renameIndex);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(572);
			match(RENAME);
			setState(573);
			indexAndKey();
			setState(574);
			indexName();
			setState(575);
			match(TO);
			setState(576);
			indexName();
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

	public static class RenameTableContext extends ParserRuleContext {
		public TerminalNode RENAME() { return getToken(MySQLDDLParser.RENAME, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode TO() { return getToken(MySQLDDLParser.TO, 0); }
		public TerminalNode AS() { return getToken(MySQLDDLParser.AS, 0); }
		public RenameTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_renameTable; }
	}

	public final RenameTableContext renameTable() throws RecognitionException {
		RenameTableContext _localctx = new RenameTableContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_renameTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(578);
			match(RENAME);
			setState(580);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS || _la==TO) {
				{
				setState(579);
				_la = _input.LA(1);
				if ( !(_la==AS || _la==TO) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(582);
			tableName();
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

	public static class ModifyColumnContext extends ParserRuleContext {
		public TerminalNode MODIFY() { return getToken(MySQLDDLParser.MODIFY, 0); }
		public ColumnNameAndDefinitionContext columnNameAndDefinition() {
			return getRuleContext(ColumnNameAndDefinitionContext.class,0);
		}
		public TerminalNode COLUMN() { return getToken(MySQLDDLParser.COLUMN, 0); }
		public TerminalNode FIRST() { return getToken(MySQLDDLParser.FIRST, 0); }
		public TerminalNode AFTER() { return getToken(MySQLDDLParser.AFTER, 0); }
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public ModifyColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifyColumn; }
	}

	public final ModifyColumnContext modifyColumn() throws RecognitionException {
		ModifyColumnContext _localctx = new ModifyColumnContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_modifyColumn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(584);
			match(MODIFY);
			setState(586);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLUMN) {
				{
				setState(585);
				match(COLUMN);
				}
			}

			setState(588);
			columnNameAndDefinition();
			setState(592);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				{
				setState(589);
				match(FIRST);
				}
				break;
			case AFTER:
				{
				setState(590);
				match(AFTER);
				setState(591);
				columnName();
				}
				break;
			case EOF:
			case COMMA:
			case PARTITION:
				break;
			default:
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

	public static class AlgorithmOptionContext extends ParserRuleContext {
		public TerminalNode ALGORITHM() { return getToken(MySQLDDLParser.ALGORITHM, 0); }
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public TerminalNode INPLACE() { return getToken(MySQLDDLParser.INPLACE, 0); }
		public TerminalNode COPY() { return getToken(MySQLDDLParser.COPY, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public AlgorithmOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_algorithmOption; }
	}

	public final AlgorithmOptionContext algorithmOption() throws RecognitionException {
		AlgorithmOptionContext _localctx = new AlgorithmOptionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_algorithmOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(594);
			match(ALGORITHM);
			setState(596);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_OR_ASSIGN) {
				{
				setState(595);
				match(EQ_OR_ASSIGN);
				}
			}

			setState(598);
			_la = _input.LA(1);
			if ( !(_la==INPLACE || _la==COPY || _la==DEFAULT) ) {
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

	public static class LockOptionContext extends ParserRuleContext {
		public TerminalNode LOCK() { return getToken(MySQLDDLParser.LOCK, 0); }
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public TerminalNode NONE() { return getToken(MySQLDDLParser.NONE, 0); }
		public TerminalNode SHARED() { return getToken(MySQLDDLParser.SHARED, 0); }
		public TerminalNode EXCLUSIVE() { return getToken(MySQLDDLParser.EXCLUSIVE, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public LockOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lockOption; }
	}

	public final LockOptionContext lockOption() throws RecognitionException {
		LockOptionContext _localctx = new LockOptionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_lockOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(600);
			match(LOCK);
			setState(602);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_OR_ASSIGN) {
				{
				setState(601);
				match(EQ_OR_ASSIGN);
				}
			}

			setState(604);
			_la = _input.LA(1);
			if ( !(_la==SHARED || _la==EXCLUSIVE || _la==NONE || _la==DEFAULT) ) {
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

	public static class IndexDefinitionContext extends ParserRuleContext {
		public IndexDefOptionContext indexDefOption() {
			return getRuleContext(IndexDefOptionContext.class,0);
		}
		public IndexAndKeyContext indexAndKey() {
			return getRuleContext(IndexAndKeyContext.class,0);
		}
		public TerminalNode FULLTEXT() { return getToken(MySQLDDLParser.FULLTEXT, 0); }
		public TerminalNode SPATIAL() { return getToken(MySQLDDLParser.SPATIAL, 0); }
		public IndexDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexDefinition; }
	}

	public final IndexDefinitionContext indexDefinition() throws RecognitionException {
		IndexDefinitionContext _localctx = new IndexDefinitionContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_indexDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(611);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPATIAL:
			case FULLTEXT:
				{
				{
				setState(606);
				_la = _input.LA(1);
				if ( !(_la==SPATIAL || _la==FULLTEXT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(608);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INDEX || _la==KEY) {
					{
					setState(607);
					indexAndKey();
					}
				}

				}
				}
				break;
			case INDEX:
			case KEY:
				{
				setState(610);
				indexAndKey();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(613);
			indexDefOption();
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

	public static class IndexAndKeyContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public IndexAndKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexAndKey; }
	}

	public final IndexAndKeyContext indexAndKey() throws RecognitionException {
		IndexAndKeyContext _localctx = new IndexAndKeyContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_indexAndKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(615);
			_la = _input.LA(1);
			if ( !(_la==INDEX || _la==KEY) ) {
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

	public static class IndexDefOptionContext extends ParserRuleContext {
		public KeyPartsContext keyParts() {
			return getRuleContext(KeyPartsContext.class,0);
		}
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public IndexTypeContext indexType() {
			return getRuleContext(IndexTypeContext.class,0);
		}
		public IndexOptionContext indexOption() {
			return getRuleContext(IndexOptionContext.class,0);
		}
		public IndexDefOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexDefOption; }
	}

	public final IndexDefOptionContext indexDefOption() throws RecognitionException {
		IndexDefOptionContext _localctx = new IndexDefOptionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_indexDefOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(618);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(617);
				indexName();
				}
			}

			setState(621);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(620);
				indexType();
				}
			}

			setState(623);
			keyParts();
			setState(625);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY_BLOCK_SIZE || _la==COMMENT || _la==WITH || _la==USING) {
				{
				setState(624);
				indexOption();
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

	public static class SingleColumnContext extends ParserRuleContext {
		public ColumnNameAndDefinitionContext columnNameAndDefinition() {
			return getRuleContext(ColumnNameAndDefinitionContext.class,0);
		}
		public TerminalNode FIRST() { return getToken(MySQLDDLParser.FIRST, 0); }
		public TerminalNode AFTER() { return getToken(MySQLDDLParser.AFTER, 0); }
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public SingleColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleColumn; }
	}

	public final SingleColumnContext singleColumn() throws RecognitionException {
		SingleColumnContext _localctx = new SingleColumnContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_singleColumn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			columnNameAndDefinition();
			setState(631);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FIRST:
				{
				setState(628);
				match(FIRST);
				}
				break;
			case AFTER:
				{
				setState(629);
				match(AFTER);
				setState(630);
				columnName();
				}
				break;
			case EOF:
			case COMMA:
			case PARTITION:
				break;
			default:
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

	public static class MultiColumnContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<ColumnNameAndDefinitionContext> columnNameAndDefinition() {
			return getRuleContexts(ColumnNameAndDefinitionContext.class);
		}
		public ColumnNameAndDefinitionContext columnNameAndDefinition(int i) {
			return getRuleContext(ColumnNameAndDefinitionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public MultiColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiColumn; }
	}

	public final MultiColumnContext multiColumn() throws RecognitionException {
		MultiColumnContext _localctx = new MultiColumnContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_multiColumn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(633);
			match(LEFT_PAREN);
			setState(634);
			columnNameAndDefinition();
			setState(639);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(635);
				match(COMMA);
				setState(636);
				columnNameAndDefinition();
				}
				}
				setState(641);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(642);
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

	public static class ConstraintDefinitionContext extends ParserRuleContext {
		public PrimaryKeyOptionContext primaryKeyOption() {
			return getRuleContext(PrimaryKeyOptionContext.class,0);
		}
		public UniqueOptionContext uniqueOption() {
			return getRuleContext(UniqueOptionContext.class,0);
		}
		public ForeignKeyOptionContext foreignKeyOption() {
			return getRuleContext(ForeignKeyOptionContext.class,0);
		}
		public TerminalNode CONSTRAINT() { return getToken(MySQLDDLParser.CONSTRAINT, 0); }
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public ConstraintDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintDefinition; }
	}

	public final ConstraintDefinitionContext constraintDefinition() throws RecognitionException {
		ConstraintDefinitionContext _localctx = new ConstraintDefinitionContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_constraintDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(648);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONSTRAINT) {
				{
				setState(644);
				match(CONSTRAINT);
				setState(646);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(645);
					symbol();
					}
				}

				}
			}

			setState(653);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PRIMARY:
				{
				setState(650);
				primaryKeyOption();
				}
				break;
			case UNIQUE:
				{
				setState(651);
				uniqueOption();
				}
				break;
			case FOREIGN:
				{
				setState(652);
				foreignKeyOption();
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

	public static class PrimaryKeyOptionContext extends ParserRuleContext {
		public TerminalNode PRIMARY() { return getToken(MySQLDDLParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public KeyPartsContext keyParts() {
			return getRuleContext(KeyPartsContext.class,0);
		}
		public IndexTypeContext indexType() {
			return getRuleContext(IndexTypeContext.class,0);
		}
		public IndexOptionContext indexOption() {
			return getRuleContext(IndexOptionContext.class,0);
		}
		public PrimaryKeyOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryKeyOption; }
	}

	public final PrimaryKeyOptionContext primaryKeyOption() throws RecognitionException {
		PrimaryKeyOptionContext _localctx = new PrimaryKeyOptionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_primaryKeyOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(655);
			match(PRIMARY);
			setState(656);
			match(KEY);
			setState(658);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(657);
				indexType();
				}
			}

			setState(660);
			keyParts();
			setState(662);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY_BLOCK_SIZE || _la==COMMENT || _la==WITH || _la==USING) {
				{
				setState(661);
				indexOption();
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

	public static class UniqueOptionContext extends ParserRuleContext {
		public TerminalNode UNIQUE() { return getToken(MySQLDDLParser.UNIQUE, 0); }
		public KeyPartsContext keyParts() {
			return getRuleContext(KeyPartsContext.class,0);
		}
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public IndexTypeContext indexType() {
			return getRuleContext(IndexTypeContext.class,0);
		}
		public IndexOptionContext indexOption() {
			return getRuleContext(IndexOptionContext.class,0);
		}
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public UniqueOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uniqueOption; }
	}

	public final UniqueOptionContext uniqueOption() throws RecognitionException {
		UniqueOptionContext _localctx = new UniqueOptionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_uniqueOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(664);
			match(UNIQUE);
			setState(666);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEX || _la==KEY) {
				{
				setState(665);
				_la = _input.LA(1);
				if ( !(_la==INDEX || _la==KEY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(669);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(668);
				indexName();
				}
			}

			setState(672);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(671);
				indexType();
				}
			}

			setState(674);
			keyParts();
			setState(676);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KEY_BLOCK_SIZE || _la==COMMENT || _la==WITH || _la==USING) {
				{
				setState(675);
				indexOption();
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

	public static class ForeignKeyOptionContext extends ParserRuleContext {
		public TerminalNode FOREIGN() { return getToken(MySQLDDLParser.FOREIGN, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public ReferenceDefinitionContext referenceDefinition() {
			return getRuleContext(ReferenceDefinitionContext.class,0);
		}
		public IndexNameContext indexName() {
			return getRuleContext(IndexNameContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public ForeignKeyOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_foreignKeyOption; }
	}

	public final ForeignKeyOptionContext foreignKeyOption() throws RecognitionException {
		ForeignKeyOptionContext _localctx = new ForeignKeyOptionContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_foreignKeyOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(678);
			match(FOREIGN);
			setState(679);
			match(KEY);
			setState(681);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(680);
				indexName();
				}
			}

			setState(683);
			match(LEFT_PAREN);
			setState(684);
			columnName();
			setState(689);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(685);
				match(COMMA);
				setState(686);
				columnName();
				}
				}
				setState(691);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(692);
			match(RIGHT_PAREN);
			setState(693);
			referenceDefinition();
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

	public static class TableOptionsContext extends ParserRuleContext {
		public List<TableOptionContext> tableOption() {
			return getRuleContexts(TableOptionContext.class);
		}
		public TableOptionContext tableOption(int i) {
			return getRuleContext(TableOptionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public TableOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableOptions; }
	}

	public final TableOptionsContext tableOptions() throws RecognitionException {
		TableOptionsContext _localctx = new TableOptionsContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_tableOptions);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(695);
			tableOption();
			setState(702);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(697);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(696);
						match(COMMA);
						}
					}

					setState(699);
					tableOption();
					}
					} 
				}
				setState(704);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
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

	public static class TableOptionContext extends ParserRuleContext {
		public TerminalNode AUTO_INCREMENT() { return getToken(MySQLDDLParser.AUTO_INCREMENT, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public TerminalNode AVG_ROW_LENGTH() { return getToken(MySQLDDLParser.AVG_ROW_LENGTH, 0); }
		public CharacterSetWithEqualContext characterSetWithEqual() {
			return getRuleContext(CharacterSetWithEqualContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public TerminalNode CHECKSUM() { return getToken(MySQLDDLParser.CHECKSUM, 0); }
		public CollateClauseWithEqualContext collateClauseWithEqual() {
			return getRuleContext(CollateClauseWithEqualContext.class,0);
		}
		public TerminalNode COMMENT() { return getToken(MySQLDDLParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(MySQLDDLParser.STRING, 0); }
		public TerminalNode COMPRESSION() { return getToken(MySQLDDLParser.COMPRESSION, 0); }
		public TerminalNode ZLIB() { return getToken(MySQLDDLParser.ZLIB, 0); }
		public TerminalNode NONE() { return getToken(MySQLDDLParser.NONE, 0); }
		public TerminalNode CONNECTION() { return getToken(MySQLDDLParser.CONNECTION, 0); }
		public TerminalNode DIRECTORY() { return getToken(MySQLDDLParser.DIRECTORY, 0); }
		public TerminalNode DATA() { return getToken(MySQLDDLParser.DATA, 0); }
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public TerminalNode DELAY_KEY_WRITE() { return getToken(MySQLDDLParser.DELAY_KEY_WRITE, 0); }
		public TerminalNode ENCRYPTION() { return getToken(MySQLDDLParser.ENCRYPTION, 0); }
		public TerminalNode ENGINE() { return getToken(MySQLDDLParser.ENGINE, 0); }
		public EngineNameContext engineName() {
			return getRuleContext(EngineNameContext.class,0);
		}
		public TerminalNode INSERT_METHOD() { return getToken(MySQLDDLParser.INSERT_METHOD, 0); }
		public TerminalNode NO() { return getToken(MySQLDDLParser.NO, 0); }
		public TerminalNode FIRST() { return getToken(MySQLDDLParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(MySQLDDLParser.LAST, 0); }
		public TerminalNode KEY_BLOCK_SIZE() { return getToken(MySQLDDLParser.KEY_BLOCK_SIZE, 0); }
		public TerminalNode MAX_ROWS() { return getToken(MySQLDDLParser.MAX_ROWS, 0); }
		public TerminalNode MIN_ROWS() { return getToken(MySQLDDLParser.MIN_ROWS, 0); }
		public TerminalNode PACK_KEYS() { return getToken(MySQLDDLParser.PACK_KEYS, 0); }
		public TerminalNode PASSWORD() { return getToken(MySQLDDLParser.PASSWORD, 0); }
		public TerminalNode ROW_FORMAT() { return getToken(MySQLDDLParser.ROW_FORMAT, 0); }
		public TerminalNode DYNAMIC() { return getToken(MySQLDDLParser.DYNAMIC, 0); }
		public TerminalNode FIXED() { return getToken(MySQLDDLParser.FIXED, 0); }
		public TerminalNode COMPRESSED() { return getToken(MySQLDDLParser.COMPRESSED, 0); }
		public TerminalNode REDUNDANT() { return getToken(MySQLDDLParser.REDUNDANT, 0); }
		public TerminalNode COMPACT() { return getToken(MySQLDDLParser.COMPACT, 0); }
		public TerminalNode STATS_AUTO_RECALC() { return getToken(MySQLDDLParser.STATS_AUTO_RECALC, 0); }
		public TerminalNode STATS_PERSISTENT() { return getToken(MySQLDDLParser.STATS_PERSISTENT, 0); }
		public TerminalNode STATS_SAMPLE_PAGES() { return getToken(MySQLDDLParser.STATS_SAMPLE_PAGES, 0); }
		public TerminalNode TABLESPACE() { return getToken(MySQLDDLParser.TABLESPACE, 0); }
		public TablespaceNameContext tablespaceName() {
			return getRuleContext(TablespaceNameContext.class,0);
		}
		public TerminalNode STORAGE() { return getToken(MySQLDDLParser.STORAGE, 0); }
		public TerminalNode DISK() { return getToken(MySQLDDLParser.DISK, 0); }
		public TerminalNode MEMORY() { return getToken(MySQLDDLParser.MEMORY, 0); }
		public TerminalNode UNION() { return getToken(MySQLDDLParser.UNION, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TableOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableOption; }
	}

	public final TableOptionContext tableOption() throws RecognitionException {
		TableOptionContext _localctx = new TableOptionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_tableOption);
		int _la;
		try {
			setState(825);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(705);
				match(AUTO_INCREMENT);
				setState(707);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(706);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(709);
				match(NUMBER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(710);
				match(AVG_ROW_LENGTH);
				setState(712);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(711);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(714);
				match(NUMBER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(716);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(715);
					match(DEFAULT);
					}
				}

				setState(718);
				characterSetWithEqual();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(719);
				match(CHECKSUM);
				setState(721);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(720);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(723);
				match(NUMBER);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(725);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(724);
					match(DEFAULT);
					}
				}

				setState(727);
				collateClauseWithEqual();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(728);
				match(COMMENT);
				setState(730);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(729);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(732);
				match(STRING);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(733);
				match(COMPRESSION);
				setState(735);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(734);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(737);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==ZLIB || _la==NONE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(738);
				match(CONNECTION);
				setState(740);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(739);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(742);
				match(STRING);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(743);
				_la = _input.LA(1);
				if ( !(_la==DATA || _la==INDEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(744);
				match(DIRECTORY);
				setState(746);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(745);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(748);
				match(STRING);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(749);
				match(DELAY_KEY_WRITE);
				setState(751);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(750);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(753);
				match(NUMBER);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(754);
				match(ENCRYPTION);
				setState(756);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(755);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(758);
				match(STRING);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(759);
				match(ENGINE);
				setState(761);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(760);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(763);
				engineName();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(764);
				match(INSERT_METHOD);
				setState(766);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(765);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(768);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FIRST) | (1L << NO) | (1L << LAST))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(769);
				match(KEY_BLOCK_SIZE);
				setState(771);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(770);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(773);
				match(NUMBER);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(774);
				match(MAX_ROWS);
				setState(776);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(775);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(778);
				match(NUMBER);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(779);
				match(MIN_ROWS);
				setState(781);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(780);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(783);
				match(NUMBER);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(784);
				match(PACK_KEYS);
				setState(786);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(785);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(788);
				_la = _input.LA(1);
				if ( !(_la==NUMBER || _la==DEFAULT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(789);
				match(PASSWORD);
				setState(791);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(790);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(793);
				match(STRING);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(794);
				match(ROW_FORMAT);
				setState(796);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(795);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(798);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DYNAMIC) | (1L << FIXED) | (1L << COMPRESSED) | (1L << REDUNDANT) | (1L << COMPACT))) != 0) || _la==DEFAULT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(799);
				match(STATS_AUTO_RECALC);
				setState(801);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(800);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(803);
				_la = _input.LA(1);
				if ( !(_la==NUMBER || _la==DEFAULT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(804);
				match(STATS_PERSISTENT);
				setState(806);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(805);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(808);
				_la = _input.LA(1);
				if ( !(_la==NUMBER || _la==DEFAULT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(809);
				match(STATS_SAMPLE_PAGES);
				setState(811);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(810);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(813);
				match(NUMBER);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(814);
				match(TABLESPACE);
				setState(815);
				tablespaceName();
				setState(818);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORAGE) {
					{
					setState(816);
					match(STORAGE);
					setState(817);
					_la = _input.LA(1);
					if ( !(_la==DISK || _la==MEMORY || _la==DEFAULT) ) {
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
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(820);
				match(UNION);
				setState(822);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(821);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(824);
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

	public static class ColumnNameAndDefinitionContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public ColumnDefinitionContext columnDefinition() {
			return getRuleContext(ColumnDefinitionContext.class,0);
		}
		public ColumnNameAndDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnNameAndDefinition; }
	}

	public final ColumnNameAndDefinitionContext columnNameAndDefinition() throws RecognitionException {
		ColumnNameAndDefinitionContext _localctx = new ColumnNameAndDefinitionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_columnNameAndDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(827);
			columnName();
			setState(828);
			columnDefinition();
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

	public static class ColumnDefinitionContext extends ParserRuleContext {
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public DataTypeOptionContext dataTypeOption() {
			return getRuleContext(DataTypeOptionContext.class,0);
		}
		public DataTypeGeneratedContext dataTypeGenerated() {
			return getRuleContext(DataTypeGeneratedContext.class,0);
		}
		public ColumnDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnDefinition; }
	}

	public final ColumnDefinitionContext columnDefinition() throws RecognitionException {
		ColumnDefinitionContext _localctx = new ColumnDefinitionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_columnDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(830);
			dataType();
			setState(833);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
			case FIRST:
			case AFTER:
			case COMMENT:
			case STORAGE:
			case COLUMN_FORMAT:
			case REFERENCES:
			case RIGHT_PAREN:
			case COMMA:
			case PARTITION:
			case UNIQUE:
			case PRIMARY:
			case KEY:
				{
				setState(831);
				dataTypeOption();
				}
				break;
			case GENERATED:
			case AS:
				{
				setState(832);
				dataTypeGenerated();
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

	public static class DataTypeContext extends ParserRuleContext {
		public TerminalNode BIT() { return getToken(MySQLDDLParser.BIT, 0); }
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public NumberTypeSuffixContext numberTypeSuffix() {
			return getRuleContext(NumberTypeSuffixContext.class,0);
		}
		public TerminalNode TINYINT() { return getToken(MySQLDDLParser.TINYINT, 0); }
		public TerminalNode SMALLINT() { return getToken(MySQLDDLParser.SMALLINT, 0); }
		public TerminalNode MEDIUMINT() { return getToken(MySQLDDLParser.MEDIUMINT, 0); }
		public TerminalNode INT() { return getToken(MySQLDDLParser.INT, 0); }
		public TerminalNode INTEGER() { return getToken(MySQLDDLParser.INTEGER, 0); }
		public TerminalNode BIGINT() { return getToken(MySQLDDLParser.BIGINT, 0); }
		public List<TerminalNode> NULL() { return getTokens(MySQLDDLParser.NULL); }
		public TerminalNode NULL(int i) {
			return getToken(MySQLDDLParser.NULL, i);
		}
		public TerminalNode AUTO_INCREMENT() { return getToken(MySQLDDLParser.AUTO_INCREMENT, 0); }
		public TerminalNode NOT() { return getToken(MySQLDDLParser.NOT, 0); }
		public TerminalNode REAL() { return getToken(MySQLDDLParser.REAL, 0); }
		public TerminalNode DOUBLE() { return getToken(MySQLDDLParser.DOUBLE, 0); }
		public TerminalNode FLOAT() { return getToken(MySQLDDLParser.FLOAT, 0); }
		public TerminalNode DECIMAL() { return getToken(MySQLDDLParser.DECIMAL, 0); }
		public TerminalNode NUMERIC() { return getToken(MySQLDDLParser.NUMERIC, 0); }
		public DataTypeLengthWithPrecisionContext dataTypeLengthWithPrecision() {
			return getRuleContext(DataTypeLengthWithPrecisionContext.class,0);
		}
		public TerminalNode DATE() { return getToken(MySQLDDLParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(MySQLDDLParser.TIME, 0); }
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public List<TerminalNode> STRING() { return getTokens(MySQLDDLParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(MySQLDDLParser.STRING, i);
		}
		public TimestampTypeContext timestampType() {
			return getRuleContext(TimestampTypeContext.class,0);
		}
		public TerminalNode DATETIME() { return getToken(MySQLDDLParser.DATETIME, 0); }
		public TerminalNode ON() { return getToken(MySQLDDLParser.ON, 0); }
		public TerminalNode UPDATE() { return getToken(MySQLDDLParser.UPDATE, 0); }
		public List<CurrentTimestampTypeContext> currentTimestampType() {
			return getRuleContexts(CurrentTimestampTypeContext.class);
		}
		public CurrentTimestampTypeContext currentTimestampType(int i) {
			return getRuleContext(CurrentTimestampTypeContext.class,i);
		}
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode YEAR() { return getToken(MySQLDDLParser.YEAR, 0); }
		public TerminalNode CHAR() { return getToken(MySQLDDLParser.CHAR, 0); }
		public TerminalNode VARCHAR() { return getToken(MySQLDDLParser.VARCHAR, 0); }
		public CharacterSetContext characterSet() {
			return getRuleContext(CharacterSetContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public TerminalNode BINARY() { return getToken(MySQLDDLParser.BINARY, 0); }
		public TerminalNode VARBINARY() { return getToken(MySQLDDLParser.VARBINARY, 0); }
		public TerminalNode TINYBLOB() { return getToken(MySQLDDLParser.TINYBLOB, 0); }
		public TerminalNode BLOB() { return getToken(MySQLDDLParser.BLOB, 0); }
		public TerminalNode MEDIUMBLOB() { return getToken(MySQLDDLParser.MEDIUMBLOB, 0); }
		public TerminalNode LONGBLOB() { return getToken(MySQLDDLParser.LONGBLOB, 0); }
		public TerminalNode JSON() { return getToken(MySQLDDLParser.JSON, 0); }
		public TerminalNode TINYTEXT() { return getToken(MySQLDDLParser.TINYTEXT, 0); }
		public TerminalNode TEXT() { return getToken(MySQLDDLParser.TEXT, 0); }
		public TerminalNode MEDIUMTEXT() { return getToken(MySQLDDLParser.MEDIUMTEXT, 0); }
		public TerminalNode LONGTEXT() { return getToken(MySQLDDLParser.LONGTEXT, 0); }
		public TerminalNode ENUM() { return getToken(MySQLDDLParser.ENUM, 0); }
		public TerminalNode SET() { return getToken(MySQLDDLParser.SET, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_dataType);
		int _la;
		try {
			setState(1002);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(835);
				match(BIT);
				setState(837);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(836);
					dataTypeLength();
					}
				}

				}
				break;
			case TINYINT:
			case SMALLINT:
			case MEDIUMINT:
			case INT:
			case INTEGER:
			case BIGINT:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(839);
				_la = _input.LA(1);
				if ( !(((((_la - 246)) & ~0x3f) == 0 && ((1L << (_la - 246)) & ((1L << (TINYINT - 246)) | (1L << (SMALLINT - 246)) | (1L << (MEDIUMINT - 246)) | (1L << (INT - 246)) | (1L << (INTEGER - 246)) | (1L << (BIGINT - 246)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(841);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(840);
					dataTypeLength();
					}
				}

				setState(847);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(844);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(843);
						match(NOT);
						}
					}

					setState(846);
					match(NULL);
					}
				}

				setState(850);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AUTO_INCREMENT) {
					{
					setState(849);
					match(AUTO_INCREMENT);
					}
				}

				setState(852);
				numberTypeSuffix();
				}
				}
				break;
			case REAL:
			case DOUBLE:
			case FLOAT:
			case DECIMAL:
			case NUMERIC:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(853);
				_la = _input.LA(1);
				if ( !(((((_la - 252)) & ~0x3f) == 0 && ((1L << (_la - 252)) & ((1L << (REAL - 252)) | (1L << (DOUBLE - 252)) | (1L << (FLOAT - 252)) | (1L << (DECIMAL - 252)) | (1L << (NUMERIC - 252)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(855);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(854);
					dataTypeLengthWithPrecision();
					}
				}

				setState(861);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(858);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(857);
						match(NOT);
						}
					}

					setState(860);
					match(NULL);
					}
				}

				setState(864);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AUTO_INCREMENT) {
					{
					setState(863);
					match(AUTO_INCREMENT);
					}
				}

				setState(866);
				numberTypeSuffix();
				}
				}
				break;
			case DATE:
			case TIME:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(867);
				_la = _input.LA(1);
				if ( !(_la==DATE || _la==TIME) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(872);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(869);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(868);
						match(NOT);
						}
					}

					setState(871);
					match(NULL);
					}
				}

				setState(876);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(874);
					match(DEFAULT);
					setState(875);
					_la = _input.LA(1);
					if ( !(_la==STRING || _la==NULL) ) {
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
				break;
			case TIMESTAMP:
			case DATETIME:
				enterOuterAlt(_localctx, 5);
				{
				{
				setState(880);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case TIMESTAMP:
					{
					setState(878);
					timestampType();
					}
					break;
				case DATETIME:
					{
					setState(879);
					match(DATETIME);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(886);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(883);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(882);
						match(NOT);
						}
					}

					setState(885);
					match(NULL);
					}
				}

				setState(895);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(888);
					match(DEFAULT);
					setState(893);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case CURRENT_TIMESTAMP:
						{
						setState(889);
						currentTimestampType();
						}
						break;
					case NUMBER:
						{
						setState(890);
						match(NUMBER);
						}
						break;
					case STRING:
						{
						setState(891);
						match(STRING);
						}
						break;
					case NULL:
						{
						setState(892);
						match(NULL);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
				}

				setState(900);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(897);
					match(ON);
					setState(898);
					match(UPDATE);
					setState(899);
					currentTimestampType();
					}
				}

				}
				}
				break;
			case YEAR:
				enterOuterAlt(_localctx, 6);
				{
				{
				setState(902);
				match(YEAR);
				setState(904);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(903);
					dataTypeLength();
					}
				}

				setState(910);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(907);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(906);
						match(NOT);
						}
					}

					setState(909);
					match(NULL);
					}
				}

				setState(914);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(912);
					match(DEFAULT);
					setState(913);
					_la = _input.LA(1);
					if ( !(_la==STRING || _la==NUMBER || _la==NULL) ) {
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
				break;
			case CHAR:
			case VARCHAR:
				enterOuterAlt(_localctx, 7);
				{
				{
				setState(916);
				_la = _input.LA(1);
				if ( !(_la==CHAR || _la==VARCHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(918);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(917);
					dataTypeLength();
					}
				}

				setState(924);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(921);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(920);
						match(NOT);
						}
					}

					setState(923);
					match(NULL);
					}
				}

				setState(927);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHARACTER || _la==CHARSET || _la==CHAR) {
					{
					setState(926);
					characterSet();
					}
				}

				setState(930);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(929);
					collateClause();
					}
				}

				setState(934);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(932);
					match(DEFAULT);
					setState(933);
					_la = _input.LA(1);
					if ( !(_la==STRING || _la==NULL) ) {
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
				break;
			case BINARY:
			case VARBINARY:
				enterOuterAlt(_localctx, 8);
				{
				{
				setState(936);
				_la = _input.LA(1);
				if ( !(_la==BINARY || _la==VARBINARY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(938);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(937);
					dataTypeLength();
					}
				}

				setState(944);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
				case 1:
					{
					setState(941);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(940);
						match(NOT);
						}
					}

					setState(943);
					match(NULL);
					}
					break;
				}
				setState(950);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case DEFAULT:
					{
					setState(946);
					match(DEFAULT);
					setState(947);
					match(NUMBER);
					}
					break;
				case STRING:
					{
					setState(948);
					match(STRING);
					}
					break;
				case NULL:
					{
					setState(949);
					match(NULL);
					}
					break;
				case EOF:
				case FIRST:
				case AFTER:
				case COMMENT:
				case STORAGE:
				case GENERATED:
				case COLUMN_FORMAT:
				case REFERENCES:
				case RIGHT_PAREN:
				case COMMA:
				case PARTITION:
				case AS:
				case UNIQUE:
				case PRIMARY:
				case KEY:
					break;
				default:
					break;
				}
				}
				}
				break;
			case TINYBLOB:
			case BLOB:
			case MEDIUMBLOB:
			case LONGBLOB:
			case JSON:
				enterOuterAlt(_localctx, 9);
				{
				setState(952);
				_la = _input.LA(1);
				if ( !(((((_la - 267)) & ~0x3f) == 0 && ((1L << (_la - 267)) & ((1L << (TINYBLOB - 267)) | (1L << (BLOB - 267)) | (1L << (MEDIUMBLOB - 267)) | (1L << (LONGBLOB - 267)) | (1L << (JSON - 267)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(957);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(954);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(953);
						match(NOT);
						}
					}

					setState(956);
					match(NULL);
					}
				}

				}
				break;
			case TINYTEXT:
			case TEXT:
			case MEDIUMTEXT:
			case LONGTEXT:
				enterOuterAlt(_localctx, 10);
				{
				{
				setState(959);
				_la = _input.LA(1);
				if ( !(((((_la - 271)) & ~0x3f) == 0 && ((1L << (_la - 271)) & ((1L << (TINYTEXT - 271)) | (1L << (TEXT - 271)) | (1L << (MEDIUMTEXT - 271)) | (1L << (LONGTEXT - 271)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(964);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(961);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(960);
						match(NOT);
						}
					}

					setState(963);
					match(NULL);
					}
				}

				setState(967);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BINARY) {
					{
					setState(966);
					match(BINARY);
					}
				}

				setState(970);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHARACTER || _la==CHARSET || _la==CHAR) {
					{
					setState(969);
					characterSet();
					}
				}

				setState(973);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(972);
					collateClause();
					}
				}

				}
				}
				break;
			case SET:
			case ENUM:
				enterOuterAlt(_localctx, 11);
				{
				{
				setState(975);
				_la = _input.LA(1);
				if ( !(_la==SET || _la==ENUM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				{
				setState(976);
				match(LEFT_PAREN);
				setState(977);
				match(STRING);
				setState(982);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(978);
					match(COMMA);
					setState(979);
					match(STRING);
					}
					}
					setState(984);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(985);
				match(RIGHT_PAREN);
				setState(990);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT || _la==NULL) {
					{
					setState(987);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOT) {
						{
						setState(986);
						match(NOT);
						}
					}

					setState(989);
					match(NULL);
					}
				}

				setState(994);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(992);
					match(DEFAULT);
					setState(993);
					_la = _input.LA(1);
					if ( !(_la==STRING || _la==NULL) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(997);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHARACTER || _la==CHARSET || _la==CHAR) {
					{
					setState(996);
					characterSet();
					}
				}

				setState(1000);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(999);
					collateClause();
					}
				}

				}
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

	public static class TimestampTypeContext extends ParserRuleContext {
		public TerminalNode TIMESTAMP() { return getToken(MySQLDDLParser.TIMESTAMP, 0); }
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public TimestampTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timestampType; }
	}

	public final TimestampTypeContext timestampType() throws RecognitionException {
		TimestampTypeContext _localctx = new TimestampTypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_timestampType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1004);
			match(TIMESTAMP);
			setState(1006);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(1005);
				dataTypeLength();
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

	public static class CurrentTimestampTypeContext extends ParserRuleContext {
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(MySQLDDLParser.CURRENT_TIMESTAMP, 0); }
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public CurrentTimestampTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_currentTimestampType; }
	}

	public final CurrentTimestampTypeContext currentTimestampType() throws RecognitionException {
		CurrentTimestampTypeContext _localctx = new CurrentTimestampTypeContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_currentTimestampType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1008);
			match(CURRENT_TIMESTAMP);
			setState(1010);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(1009);
				dataTypeLength();
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

	public static class DataTypeLengthContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public DataTypeLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeLength; }
	}

	public final DataTypeLengthContext dataTypeLength() throws RecognitionException {
		DataTypeLengthContext _localctx = new DataTypeLengthContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_dataTypeLength);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1012);
			match(LEFT_PAREN);
			setState(1013);
			match(NUMBER);
			setState(1014);
			match(RIGHT_PAREN);
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

	public static class DataTypeLengthWithPrecisionContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(MySQLDDLParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(MySQLDDLParser.NUMBER, i);
		}
		public TerminalNode COMMA() { return getToken(MySQLDDLParser.COMMA, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public DataTypeLengthWithPrecisionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeLengthWithPrecision; }
	}

	public final DataTypeLengthWithPrecisionContext dataTypeLengthWithPrecision() throws RecognitionException {
		DataTypeLengthWithPrecisionContext _localctx = new DataTypeLengthWithPrecisionContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_dataTypeLengthWithPrecision);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1016);
			match(LEFT_PAREN);
			setState(1017);
			match(NUMBER);
			setState(1018);
			match(COMMA);
			setState(1019);
			match(NUMBER);
			setState(1020);
			match(RIGHT_PAREN);
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

	public static class NumberTypeSuffixContext extends ParserRuleContext {
		public TerminalNode UNSIGNED() { return getToken(MySQLDDLParser.UNSIGNED, 0); }
		public TerminalNode ZEROFILL() { return getToken(MySQLDDLParser.ZEROFILL, 0); }
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode STRING() { return getToken(MySQLDDLParser.STRING, 0); }
		public TerminalNode NULL() { return getToken(MySQLDDLParser.NULL, 0); }
		public NumberTypeSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberTypeSuffix; }
	}

	public final NumberTypeSuffixContext numberTypeSuffix() throws RecognitionException {
		NumberTypeSuffixContext _localctx = new NumberTypeSuffixContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_numberTypeSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1023);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNSIGNED) {
				{
				setState(1022);
				match(UNSIGNED);
				}
			}

			setState(1026);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ZEROFILL) {
				{
				setState(1025);
				match(ZEROFILL);
				}
			}

			setState(1030);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEFAULT) {
				{
				setState(1028);
				match(DEFAULT);
				setState(1029);
				_la = _input.LA(1);
				if ( !(_la==STRING || _la==NUMBER || _la==NULL) ) {
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

	public static class DataTypeOptionContext extends ParserRuleContext {
		public TerminalNode UNIQUE() { return getToken(MySQLDDLParser.UNIQUE, 0); }
		public List<TerminalNode> KEY() { return getTokens(MySQLDDLParser.KEY); }
		public TerminalNode KEY(int i) {
			return getToken(MySQLDDLParser.KEY, i);
		}
		public TerminalNode COMMENT() { return getToken(MySQLDDLParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(MySQLDDLParser.STRING, 0); }
		public TerminalNode COLUMN_FORMAT() { return getToken(MySQLDDLParser.COLUMN_FORMAT, 0); }
		public TerminalNode STORAGE() { return getToken(MySQLDDLParser.STORAGE, 0); }
		public ReferenceDefinitionContext referenceDefinition() {
			return getRuleContext(ReferenceDefinitionContext.class,0);
		}
		public TerminalNode FIXED() { return getToken(MySQLDDLParser.FIXED, 0); }
		public TerminalNode DYNAMIC() { return getToken(MySQLDDLParser.DYNAMIC, 0); }
		public List<TerminalNode> DEFAULT() { return getTokens(MySQLDDLParser.DEFAULT); }
		public TerminalNode DEFAULT(int i) {
			return getToken(MySQLDDLParser.DEFAULT, i);
		}
		public TerminalNode DISK() { return getToken(MySQLDDLParser.DISK, 0); }
		public TerminalNode MEMORY() { return getToken(MySQLDDLParser.MEMORY, 0); }
		public TerminalNode PRIMARY() { return getToken(MySQLDDLParser.PRIMARY, 0); }
		public DataTypeOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeOption; }
	}

	public final DataTypeOptionContext dataTypeOption() throws RecognitionException {
		DataTypeOptionContext _localctx = new DataTypeOptionContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_dataTypeOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1036);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNIQUE) {
				{
				setState(1032);
				match(UNIQUE);
				setState(1034);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
				case 1:
					{
					setState(1033);
					match(KEY);
					}
					break;
				}
				}
			}

			setState(1042);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PRIMARY || _la==KEY) {
				{
				setState(1039);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PRIMARY) {
					{
					setState(1038);
					match(PRIMARY);
					}
				}

				setState(1041);
				match(KEY);
				}
			}

			setState(1046);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(1044);
				match(COMMENT);
				setState(1045);
				match(STRING);
				}
			}

			setState(1050);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLUMN_FORMAT) {
				{
				setState(1048);
				match(COLUMN_FORMAT);
				setState(1049);
				_la = _input.LA(1);
				if ( !(_la==DYNAMIC || _la==FIXED || _la==DEFAULT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1054);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STORAGE) {
				{
				setState(1052);
				match(STORAGE);
				setState(1053);
				_la = _input.LA(1);
				if ( !(_la==DISK || _la==MEMORY || _la==DEFAULT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1057);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REFERENCES) {
				{
				setState(1056);
				referenceDefinition();
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

	public static class DataTypeGeneratedContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(MySQLDDLParser.AS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TerminalNode GENERATED() { return getToken(MySQLDDLParser.GENERATED, 0); }
		public TerminalNode ALWAYS() { return getToken(MySQLDDLParser.ALWAYS, 0); }
		public TerminalNode NOT() { return getToken(MySQLDDLParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(MySQLDDLParser.NULL, 0); }
		public TerminalNode UNIQUE() { return getToken(MySQLDDLParser.UNIQUE, 0); }
		public List<TerminalNode> KEY() { return getTokens(MySQLDDLParser.KEY); }
		public TerminalNode KEY(int i) {
			return getToken(MySQLDDLParser.KEY, i);
		}
		public TerminalNode COMMENT() { return getToken(MySQLDDLParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(MySQLDDLParser.STRING, 0); }
		public TerminalNode VIRTUAL() { return getToken(MySQLDDLParser.VIRTUAL, 0); }
		public TerminalNode STORED() { return getToken(MySQLDDLParser.STORED, 0); }
		public TerminalNode PRIMARY() { return getToken(MySQLDDLParser.PRIMARY, 0); }
		public DataTypeGeneratedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeGenerated; }
	}

	public final DataTypeGeneratedContext dataTypeGenerated() throws RecognitionException {
		DataTypeGeneratedContext _localctx = new DataTypeGeneratedContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_dataTypeGenerated);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1061);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GENERATED) {
				{
				setState(1059);
				match(GENERATED);
				setState(1060);
				match(ALWAYS);
				}
			}

			setState(1063);
			match(AS);
			setState(1064);
			match(LEFT_PAREN);
			setState(1065);
			expr(0);
			setState(1066);
			match(RIGHT_PAREN);
			setState(1068);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VIRTUAL || _la==STORED) {
				{
				setState(1067);
				_la = _input.LA(1);
				if ( !(_la==VIRTUAL || _la==STORED) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1073);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				setState(1070);
				match(NOT);
				setState(1071);
				match(NULL);
				}
				break;
			case NULL:
				{
				setState(1072);
				match(NULL);
				}
				break;
			case EOF:
			case FIRST:
			case AFTER:
			case COMMENT:
			case RIGHT_PAREN:
			case COMMA:
			case PARTITION:
			case UNIQUE:
			case PRIMARY:
			case KEY:
				break;
			default:
				break;
			}
			setState(1079);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNIQUE) {
				{
				setState(1075);
				match(UNIQUE);
				setState(1077);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
				case 1:
					{
					setState(1076);
					match(KEY);
					}
					break;
				}
				}
			}

			setState(1085);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PRIMARY || _la==KEY) {
				{
				setState(1082);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PRIMARY) {
					{
					setState(1081);
					match(PRIMARY);
					}
				}

				setState(1084);
				match(KEY);
				}
			}

			setState(1089);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(1087);
				match(COMMENT);
				setState(1088);
				match(STRING);
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

	public static class ReferenceDefinitionContext extends ParserRuleContext {
		public TerminalNode REFERENCES() { return getToken(MySQLDDLParser.REFERENCES, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public KeyPartsContext keyParts() {
			return getRuleContext(KeyPartsContext.class,0);
		}
		public TerminalNode MATCH() { return getToken(MySQLDDLParser.MATCH, 0); }
		public TerminalNode FULL() { return getToken(MySQLDDLParser.FULL, 0); }
		public TerminalNode PARTIAL() { return getToken(MySQLDDLParser.PARTIAL, 0); }
		public TerminalNode SIMPLE() { return getToken(MySQLDDLParser.SIMPLE, 0); }
		public List<TerminalNode> ON() { return getTokens(MySQLDDLParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(MySQLDDLParser.ON, i);
		}
		public TerminalNode UPDATE() { return getToken(MySQLDDLParser.UPDATE, 0); }
		public List<ReferenceOptionContext> referenceOption() {
			return getRuleContexts(ReferenceOptionContext.class);
		}
		public ReferenceOptionContext referenceOption(int i) {
			return getRuleContext(ReferenceOptionContext.class,i);
		}
		public TerminalNode DELETE() { return getToken(MySQLDDLParser.DELETE, 0); }
		public ReferenceDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceDefinition; }
	}

	public final ReferenceDefinitionContext referenceDefinition() throws RecognitionException {
		ReferenceDefinitionContext _localctx = new ReferenceDefinitionContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_referenceDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1091);
			match(REFERENCES);
			setState(1092);
			tableName();
			setState(1093);
			keyParts();
			setState(1100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
			case 1:
				{
				setState(1094);
				match(MATCH);
				setState(1095);
				match(FULL);
				}
				break;
			case 2:
				{
				setState(1096);
				match(MATCH);
				setState(1097);
				match(PARTIAL);
				}
				break;
			case 3:
				{
				setState(1098);
				match(MATCH);
				setState(1099);
				match(SIMPLE);
				}
				break;
			}
			setState(1122);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				{
				{
				setState(1105);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,164,_ctx) ) {
				case 1:
					{
					setState(1102);
					match(ON);
					setState(1103);
					match(UPDATE);
					setState(1104);
					referenceOption();
					}
					break;
				}
				setState(1110);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(1107);
					match(ON);
					setState(1108);
					match(DELETE);
					setState(1109);
					referenceOption();
					}
				}

				}
				}
				break;
			case 2:
				{
				{
				setState(1115);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
				case 1:
					{
					setState(1112);
					match(ON);
					setState(1113);
					match(DELETE);
					setState(1114);
					referenceOption();
					}
					break;
				}
				setState(1120);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(1117);
					match(ON);
					setState(1118);
					match(UPDATE);
					setState(1119);
					referenceOption();
					}
				}

				}
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

	public static class ReferenceOptionContext extends ParserRuleContext {
		public TerminalNode RESTRICT() { return getToken(MySQLDDLParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(MySQLDDLParser.CASCADE, 0); }
		public TerminalNode SET() { return getToken(MySQLDDLParser.SET, 0); }
		public TerminalNode NULL() { return getToken(MySQLDDLParser.NULL, 0); }
		public TerminalNode NO() { return getToken(MySQLDDLParser.NO, 0); }
		public TerminalNode ACTION() { return getToken(MySQLDDLParser.ACTION, 0); }
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
		public ReferenceOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceOption; }
	}

	public final ReferenceOptionContext referenceOption() throws RecognitionException {
		ReferenceOptionContext _localctx = new ReferenceOptionContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_referenceOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1132);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
			case 1:
				{
				setState(1124);
				match(RESTRICT);
				}
				break;
			case 2:
				{
				setState(1125);
				match(CASCADE);
				}
				break;
			case 3:
				{
				setState(1126);
				match(SET);
				setState(1127);
				match(NULL);
				}
				break;
			case 4:
				{
				setState(1128);
				match(NO);
				setState(1129);
				match(ACTION);
				}
				break;
			case 5:
				{
				setState(1130);
				match(SET);
				setState(1131);
				match(DEFAULT);
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

	public static class SymbolContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public SymbolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_symbol; }
	}

	public final SymbolContext symbol() throws RecognitionException {
		SymbolContext _localctx = new SymbolContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_symbol);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1134);
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

	public static class FkSymbolContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public FkSymbolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fkSymbol; }
	}

	public final FkSymbolContext fkSymbol() throws RecognitionException {
		FkSymbolContext _localctx = new FkSymbolContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_fkSymbol);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1136);
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

	public static class KeyPartsContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<KeyPartContext> keyPart() {
			return getRuleContexts(KeyPartContext.class);
		}
		public KeyPartContext keyPart(int i) {
			return getRuleContext(KeyPartContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public KeyPartsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyParts; }
	}

	public final KeyPartsContext keyParts() throws RecognitionException {
		KeyPartsContext _localctx = new KeyPartsContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_keyParts);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1138);
			match(LEFT_PAREN);
			setState(1139);
			keyPart();
			setState(1144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1140);
				match(COMMA);
				setState(1141);
				keyPart();
				}
				}
				setState(1146);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1147);
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

	public static class DefaultValueContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(MySQLDDLParser.NULL, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public DefaultValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValue; }
	}

	public final DefaultValueContext defaultValue() throws RecognitionException {
		DefaultValueContext _localctx = new DefaultValueContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_defaultValue);
		try {
			setState(1151);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1149);
				match(NULL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1150);
				simpleExpr(0);
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

	public static class KeyPartContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TerminalNode ASC() { return getToken(MySQLDDLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(MySQLDDLParser.DESC, 0); }
		public KeyPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyPart; }
	}

	public final KeyPartContext keyPart() throws RecognitionException {
		KeyPartContext _localctx = new KeyPartContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_keyPart);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1153);
			columnName();
			setState(1157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(1154);
				match(LEFT_PAREN);
				setState(1155);
				match(NUMBER);
				setState(1156);
				match(RIGHT_PAREN);
				}
			}

			setState(1160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(1159);
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

	public static class IndexNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public IndexNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexName; }
	}

	public final IndexNameContext indexName() throws RecognitionException {
		IndexNameContext _localctx = new IndexNameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1162);
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

	public static class IndexTypeContext extends ParserRuleContext {
		public TerminalNode USING() { return getToken(MySQLDDLParser.USING, 0); }
		public TerminalNode BTREE() { return getToken(MySQLDDLParser.BTREE, 0); }
		public TerminalNode HASH() { return getToken(MySQLDDLParser.HASH, 0); }
		public IndexTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexType; }
	}

	public final IndexTypeContext indexType() throws RecognitionException {
		IndexTypeContext _localctx = new IndexTypeContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_indexType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1164);
			match(USING);
			setState(1165);
			_la = _input.LA(1);
			if ( !(_la==BTREE || _la==HASH) ) {
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

	public static class IndexOptionContext extends ParserRuleContext {
		public TerminalNode KEY_BLOCK_SIZE() { return getToken(MySQLDDLParser.KEY_BLOCK_SIZE, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public IndexTypeContext indexType() {
			return getRuleContext(IndexTypeContext.class,0);
		}
		public TerminalNode WITH() { return getToken(MySQLDDLParser.WITH, 0); }
		public TerminalNode PARSER() { return getToken(MySQLDDLParser.PARSER, 0); }
		public ParserNameContext parserName() {
			return getRuleContext(ParserNameContext.class,0);
		}
		public TerminalNode COMMENT() { return getToken(MySQLDDLParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(MySQLDDLParser.STRING, 0); }
		public IndexOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexOption; }
	}

	public final IndexOptionContext indexOption() throws RecognitionException {
		IndexOptionContext _localctx = new IndexOptionContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_indexOption);
		try {
			setState(1178);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KEY_BLOCK_SIZE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1167);
				match(KEY_BLOCK_SIZE);
				setState(1169);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
				case 1:
					{
					setState(1168);
					match(EQ_OR_ASSIGN);
					}
					break;
				}
				setState(1171);
				value();
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(1172);
				indexType();
				}
				break;
			case WITH:
				enterOuterAlt(_localctx, 3);
				{
				setState(1173);
				match(WITH);
				setState(1174);
				match(PARSER);
				setState(1175);
				parserName();
				}
				break;
			case COMMENT:
				enterOuterAlt(_localctx, 4);
				{
				setState(1176);
				match(COMMENT);
				setState(1177);
				match(STRING);
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

	public static class ParserNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public ParserNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parserName; }
	}

	public final ParserNameContext parserName() throws RecognitionException {
		ParserNameContext _localctx = new ParserNameContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_parserName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1180);
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

	public static class EngineNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TerminalNode MEMORY() { return getToken(MySQLDDLParser.MEMORY, 0); }
		public EngineNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_engineName; }
	}

	public final EngineNameContext engineName() throws RecognitionException {
		EngineNameContext _localctx = new EngineNameContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_engineName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1182);
			_la = _input.LA(1);
			if ( !(_la==MEMORY || _la==ID) ) {
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

	public static class PartitionNamesContext extends ParserRuleContext {
		public List<PartitionNameContext> partitionName() {
			return getRuleContexts(PartitionNameContext.class);
		}
		public PartitionNameContext partitionName(int i) {
			return getRuleContext(PartitionNameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public PartitionNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionNames; }
	}

	public final PartitionNamesContext partitionNames() throws RecognitionException {
		PartitionNamesContext _localctx = new PartitionNamesContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_partitionNames);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1184);
			partitionName();
			setState(1189);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1185);
					match(COMMA);
					setState(1186);
					partitionName();
					}
					} 
				}
				setState(1191);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
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

	public static class PartitionNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public PartitionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionName; }
	}

	public final PartitionNameContext partitionName() throws RecognitionException {
		PartitionNameContext _localctx = new PartitionNameContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_partitionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1192);
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

	public static class PartitionOptionsContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(MySQLDDLParser.PARTITION, 0); }
		public List<TerminalNode> BY() { return getTokens(MySQLDDLParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(MySQLDDLParser.BY, i);
		}
		public List<LinearPartitionContext> linearPartition() {
			return getRuleContexts(LinearPartitionContext.class);
		}
		public LinearPartitionContext linearPartition(int i) {
			return getRuleContext(LinearPartitionContext.class,i);
		}
		public RangeOrListPartitionContext rangeOrListPartition() {
			return getRuleContext(RangeOrListPartitionContext.class,0);
		}
		public TerminalNode PARTITIONS() { return getToken(MySQLDDLParser.PARTITIONS, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(MySQLDDLParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(MySQLDDLParser.NUMBER, i);
		}
		public TerminalNode SUBPARTITION() { return getToken(MySQLDDLParser.SUBPARTITION, 0); }
		public PartitionDefinitionsContext partitionDefinitions() {
			return getRuleContext(PartitionDefinitionsContext.class,0);
		}
		public TerminalNode SUBPARTITIONS() { return getToken(MySQLDDLParser.SUBPARTITIONS, 0); }
		public PartitionOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionOptions; }
	}

	public final PartitionOptionsContext partitionOptions() throws RecognitionException {
		PartitionOptionsContext _localctx = new PartitionOptionsContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_partitionOptions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1194);
			match(PARTITION);
			setState(1195);
			match(BY);
			setState(1198);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HASH:
			case LINEAR:
			case KEY:
				{
				setState(1196);
				linearPartition();
				}
				break;
			case RANGE:
			case LIST:
				{
				setState(1197);
				rangeOrListPartition();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1202);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITIONS) {
				{
				setState(1200);
				match(PARTITIONS);
				setState(1201);
				match(NUMBER);
				}
			}

			setState(1211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUBPARTITION) {
				{
				setState(1204);
				match(SUBPARTITION);
				setState(1205);
				match(BY);
				setState(1206);
				linearPartition();
				setState(1209);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SUBPARTITIONS) {
					{
					setState(1207);
					match(SUBPARTITIONS);
					setState(1208);
					match(NUMBER);
					}
				}

				}
			}

			setState(1214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(1213);
				partitionDefinitions();
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

	public static class LinearPartitionContext extends ParserRuleContext {
		public KeyColumnListContext keyColumnList() {
			return getRuleContext(KeyColumnListContext.class,0);
		}
		public TerminalNode LINEAR() { return getToken(MySQLDDLParser.LINEAR, 0); }
		public TerminalNode HASH() { return getToken(MySQLDDLParser.HASH, 0); }
		public YearFunctionExprContext yearFunctionExpr() {
			return getRuleContext(YearFunctionExprContext.class,0);
		}
		public ExprWithParenContext exprWithParen() {
			return getRuleContext(ExprWithParenContext.class,0);
		}
		public LinearPartitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_linearPartition; }
	}

	public final LinearPartitionContext linearPartition() throws RecognitionException {
		LinearPartitionContext _localctx = new LinearPartitionContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_linearPartition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1217);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LINEAR) {
				{
				setState(1216);
				match(LINEAR);
				}
			}

			setState(1225);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HASH:
				{
				{
				setState(1219);
				match(HASH);
				setState(1222);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
				case 1:
					{
					setState(1220);
					yearFunctionExpr();
					}
					break;
				case 2:
					{
					setState(1221);
					exprWithParen();
					}
					break;
				}
				}
				}
				break;
			case KEY:
				{
				setState(1224);
				keyColumnList();
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

	public static class YearFunctionExprContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode YEAR() { return getToken(MySQLDDLParser.YEAR, 0); }
		public ExprWithParenContext exprWithParen() {
			return getRuleContext(ExprWithParenContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public YearFunctionExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yearFunctionExpr; }
	}

	public final YearFunctionExprContext yearFunctionExpr() throws RecognitionException {
		YearFunctionExprContext _localctx = new YearFunctionExprContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_yearFunctionExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1227);
			match(LEFT_PAREN);
			setState(1228);
			match(YEAR);
			setState(1229);
			exprWithParen();
			setState(1230);
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

	public static class KeyColumnListContext extends ParserRuleContext {
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public TerminalNode ALGORITHM() { return getToken(MySQLDDLParser.ALGORITHM, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public KeyColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyColumnList; }
	}

	public final KeyColumnListContext keyColumnList() throws RecognitionException {
		KeyColumnListContext _localctx = new KeyColumnListContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_keyColumnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1232);
			match(KEY);
			setState(1236);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ALGORITHM) {
				{
				setState(1233);
				match(ALGORITHM);
				setState(1234);
				match(EQ_OR_ASSIGN);
				setState(1235);
				match(NUMBER);
				}
			}

			setState(1238);
			columnList();
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

	public static class ExprWithParenContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public ExprWithParenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprWithParen; }
	}

	public final ExprWithParenContext exprWithParen() throws RecognitionException {
		ExprWithParenContext _localctx = new ExprWithParenContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_exprWithParen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1240);
			match(LEFT_PAREN);
			setState(1241);
			expr(0);
			setState(1242);
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

	public static class RangeOrListPartitionContext extends ParserRuleContext {
		public ExprOrColumnsContext exprOrColumns() {
			return getRuleContext(ExprOrColumnsContext.class,0);
		}
		public TerminalNode RANGE() { return getToken(MySQLDDLParser.RANGE, 0); }
		public TerminalNode LIST() { return getToken(MySQLDDLParser.LIST, 0); }
		public RangeOrListPartitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeOrListPartition; }
	}

	public final RangeOrListPartitionContext rangeOrListPartition() throws RecognitionException {
		RangeOrListPartitionContext _localctx = new RangeOrListPartitionContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_rangeOrListPartition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1244);
			_la = _input.LA(1);
			if ( !(_la==RANGE || _la==LIST) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1245);
			exprOrColumns();
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

	public static class ExprOrColumnsContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TerminalNode COLUMNS() { return getToken(MySQLDDLParser.COLUMNS, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public ExprOrColumnsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprOrColumns; }
	}

	public final ExprOrColumnsContext exprOrColumns() throws RecognitionException {
		ExprOrColumnsContext _localctx = new ExprOrColumnsContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_exprOrColumns);
		try {
			setState(1253);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LEFT_PAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1247);
				match(LEFT_PAREN);
				setState(1248);
				expr(0);
				setState(1249);
				match(RIGHT_PAREN);
				}
				break;
			case COLUMNS:
				enterOuterAlt(_localctx, 2);
				{
				setState(1251);
				match(COLUMNS);
				setState(1252);
				columnList();
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

	public static class PartitionDefinitionsContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<PartitionDefinitionContext> partitionDefinition() {
			return getRuleContexts(PartitionDefinitionContext.class);
		}
		public PartitionDefinitionContext partitionDefinition(int i) {
			return getRuleContext(PartitionDefinitionContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public PartitionDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionDefinitions; }
	}

	public final PartitionDefinitionsContext partitionDefinitions() throws RecognitionException {
		PartitionDefinitionsContext _localctx = new PartitionDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_partitionDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1255);
			match(LEFT_PAREN);
			setState(1256);
			partitionDefinition();
			setState(1261);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1257);
				match(COMMA);
				setState(1258);
				partitionDefinition();
				}
				}
				setState(1263);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1264);
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

	public static class PartitionDefinitionContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(MySQLDDLParser.PARTITION, 0); }
		public PartitionNameContext partitionName() {
			return getRuleContext(PartitionNameContext.class,0);
		}
		public TerminalNode VALUES() { return getToken(MySQLDDLParser.VALUES, 0); }
		public TerminalNode ENGINE() { return getToken(MySQLDDLParser.ENGINE, 0); }
		public EngineNameContext engineName() {
			return getRuleContext(EngineNameContext.class,0);
		}
		public TerminalNode COMMENT() { return getToken(MySQLDDLParser.COMMENT, 0); }
		public List<TerminalNode> STRING() { return getTokens(MySQLDDLParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(MySQLDDLParser.STRING, i);
		}
		public TerminalNode DATA() { return getToken(MySQLDDLParser.DATA, 0); }
		public List<TerminalNode> DIRECTORY() { return getTokens(MySQLDDLParser.DIRECTORY); }
		public TerminalNode DIRECTORY(int i) {
			return getToken(MySQLDDLParser.DIRECTORY, i);
		}
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public TerminalNode MAX_ROWS() { return getToken(MySQLDDLParser.MAX_ROWS, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(MySQLDDLParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(MySQLDDLParser.NUMBER, i);
		}
		public TerminalNode MIN_ROWS() { return getToken(MySQLDDLParser.MIN_ROWS, 0); }
		public TerminalNode TABLESPACE() { return getToken(MySQLDDLParser.TABLESPACE, 0); }
		public TablespaceNameContext tablespaceName() {
			return getRuleContext(TablespaceNameContext.class,0);
		}
		public List<SubpartitionDefinitionContext> subpartitionDefinition() {
			return getRuleContexts(SubpartitionDefinitionContext.class);
		}
		public SubpartitionDefinitionContext subpartitionDefinition(int i) {
			return getRuleContext(SubpartitionDefinitionContext.class,i);
		}
		public LessThanPartitionContext lessThanPartition() {
			return getRuleContext(LessThanPartitionContext.class,0);
		}
		public TerminalNode IN() { return getToken(MySQLDDLParser.IN, 0); }
		public ValueListWithParenContext valueListWithParen() {
			return getRuleContext(ValueListWithParenContext.class,0);
		}
		public TerminalNode STORAGE() { return getToken(MySQLDDLParser.STORAGE, 0); }
		public List<TerminalNode> EQ_OR_ASSIGN() { return getTokens(MySQLDDLParser.EQ_OR_ASSIGN); }
		public TerminalNode EQ_OR_ASSIGN(int i) {
			return getToken(MySQLDDLParser.EQ_OR_ASSIGN, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public PartitionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionDefinition; }
	}

	public final PartitionDefinitionContext partitionDefinition() throws RecognitionException {
		PartitionDefinitionContext _localctx = new PartitionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_partitionDefinition);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1266);
			match(PARTITION);
			setState(1267);
			partitionName();
			setState(1274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUES) {
				{
				setState(1268);
				match(VALUES);
				setState(1272);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LESS:
					{
					setState(1269);
					lessThanPartition();
					}
					break;
				case IN:
					{
					setState(1270);
					match(IN);
					setState(1271);
					valueListWithParen();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
			}

			setState(1284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ENGINE || _la==STORAGE) {
				{
				setState(1277);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORAGE) {
					{
					setState(1276);
					match(STORAGE);
					}
				}

				setState(1279);
				match(ENGINE);
				setState(1281);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1280);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1283);
				engineName();
				}
			}

			setState(1291);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(1286);
				match(COMMENT);
				setState(1288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1287);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1290);
				match(STRING);
				}
			}

			setState(1299);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DATA) {
				{
				setState(1293);
				match(DATA);
				setState(1294);
				match(DIRECTORY);
				setState(1296);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1295);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1298);
				match(STRING);
				}
			}

			setState(1307);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEX) {
				{
				setState(1301);
				match(INDEX);
				setState(1302);
				match(DIRECTORY);
				setState(1304);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1303);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1306);
				match(STRING);
				}
			}

			setState(1314);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MAX_ROWS) {
				{
				setState(1309);
				match(MAX_ROWS);
				setState(1311);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1310);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1313);
				match(NUMBER);
				}
			}

			setState(1321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MIN_ROWS) {
				{
				setState(1316);
				match(MIN_ROWS);
				setState(1318);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1317);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1320);
				match(NUMBER);
				}
			}

			setState(1328);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TABLESPACE) {
				{
				setState(1323);
				match(TABLESPACE);
				setState(1325);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1324);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1327);
				tablespaceName();
				}
			}

			setState(1338);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUBPARTITION) {
				{
				setState(1330);
				subpartitionDefinition();
				setState(1335);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1331);
						match(COMMA);
						setState(1332);
						subpartitionDefinition();
						}
						} 
					}
					setState(1337);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
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

	public static class LessThanPartitionContext extends ParserRuleContext {
		public TerminalNode LESS() { return getToken(MySQLDDLParser.LESS, 0); }
		public TerminalNode THAN() { return getToken(MySQLDDLParser.THAN, 0); }
		public TerminalNode MAXVALUE() { return getToken(MySQLDDLParser.MAXVALUE, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public LessThanPartitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lessThanPartition; }
	}

	public final LessThanPartitionContext lessThanPartition() throws RecognitionException {
		LessThanPartitionContext _localctx = new LessThanPartitionContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_lessThanPartition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1340);
			match(LESS);
			setState(1341);
			match(THAN);
			setState(1350);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LEFT_PAREN:
				{
				{
				setState(1342);
				match(LEFT_PAREN);
				setState(1345);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(1343);
					expr(0);
					}
					break;
				case 2:
					{
					setState(1344);
					valueList();
					}
					break;
				}
				setState(1347);
				match(RIGHT_PAREN);
				}
				}
				break;
			case MAXVALUE:
				{
				setState(1349);
				match(MAXVALUE);
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

	public static class SubpartitionDefinitionContext extends ParserRuleContext {
		public TerminalNode SUBPARTITION() { return getToken(MySQLDDLParser.SUBPARTITION, 0); }
		public PartitionNameContext partitionName() {
			return getRuleContext(PartitionNameContext.class,0);
		}
		public TerminalNode ENGINE() { return getToken(MySQLDDLParser.ENGINE, 0); }
		public EngineNameContext engineName() {
			return getRuleContext(EngineNameContext.class,0);
		}
		public TerminalNode COMMENT() { return getToken(MySQLDDLParser.COMMENT, 0); }
		public List<TerminalNode> STRING() { return getTokens(MySQLDDLParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(MySQLDDLParser.STRING, i);
		}
		public TerminalNode DATA() { return getToken(MySQLDDLParser.DATA, 0); }
		public List<TerminalNode> DIRECTORY() { return getTokens(MySQLDDLParser.DIRECTORY); }
		public TerminalNode DIRECTORY(int i) {
			return getToken(MySQLDDLParser.DIRECTORY, i);
		}
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public TerminalNode MAX_ROWS() { return getToken(MySQLDDLParser.MAX_ROWS, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(MySQLDDLParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(MySQLDDLParser.NUMBER, i);
		}
		public TerminalNode MIN_ROWS() { return getToken(MySQLDDLParser.MIN_ROWS, 0); }
		public TerminalNode TABLESPACE() { return getToken(MySQLDDLParser.TABLESPACE, 0); }
		public TablespaceNameContext tablespaceName() {
			return getRuleContext(TablespaceNameContext.class,0);
		}
		public TerminalNode STORAGE() { return getToken(MySQLDDLParser.STORAGE, 0); }
		public List<TerminalNode> EQ_OR_ASSIGN() { return getTokens(MySQLDDLParser.EQ_OR_ASSIGN); }
		public TerminalNode EQ_OR_ASSIGN(int i) {
			return getToken(MySQLDDLParser.EQ_OR_ASSIGN, i);
		}
		public SubpartitionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subpartitionDefinition; }
	}

	public final SubpartitionDefinitionContext subpartitionDefinition() throws RecognitionException {
		SubpartitionDefinitionContext _localctx = new SubpartitionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_subpartitionDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1352);
			match(SUBPARTITION);
			setState(1353);
			partitionName();
			setState(1362);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ENGINE || _la==STORAGE) {
				{
				setState(1355);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STORAGE) {
					{
					setState(1354);
					match(STORAGE);
					}
				}

				setState(1357);
				match(ENGINE);
				setState(1359);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1358);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1361);
				engineName();
				}
			}

			setState(1369);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(1364);
				match(COMMENT);
				setState(1366);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1365);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1368);
				match(STRING);
				}
			}

			setState(1377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DATA) {
				{
				setState(1371);
				match(DATA);
				setState(1372);
				match(DIRECTORY);
				setState(1374);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1373);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1376);
				match(STRING);
				}
			}

			setState(1385);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INDEX) {
				{
				setState(1379);
				match(INDEX);
				setState(1380);
				match(DIRECTORY);
				setState(1382);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1381);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1384);
				match(STRING);
				}
			}

			setState(1392);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MAX_ROWS) {
				{
				setState(1387);
				match(MAX_ROWS);
				setState(1389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1388);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1391);
				match(NUMBER);
				}
			}

			setState(1399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MIN_ROWS) {
				{
				setState(1394);
				match(MIN_ROWS);
				setState(1396);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1395);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1398);
				match(NUMBER);
				}
			}

			setState(1406);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TABLESPACE) {
				{
				setState(1401);
				match(TABLESPACE);
				setState(1403);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1402);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1405);
				tablespaceName();
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
		public TerminalNode DEFAULT() { return getToken(MySQLDDLParser.DEFAULT, 0); }
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
		enterRule(_localctx, 140, RULE_value);
		try {
			setState(1410);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,224,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1408);
				match(DEFAULT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1409);
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
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public ValueListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueList; }
	}

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1412);
			value();
			setState(1417);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1413);
				match(COMMA);
				setState(1414);
				value();
				}
				}
				setState(1419);
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
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public ValueListWithParenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueListWithParen; }
	}

	public final ValueListWithParenContext valueListWithParen() throws RecognitionException {
		ValueListWithParenContext _localctx = new ValueListWithParenContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_valueListWithParen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1420);
			match(LEFT_PAREN);
			setState(1421);
			valueList();
			setState(1422);
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

	public static class SchemaNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public SchemaNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaName; }
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1424);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1426);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1428);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TablespaceNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablespaceName; }
	}

	public final TablespaceNameContext tablespaceName() throws RecognitionException {
		TablespaceNameContext _localctx = new TablespaceNameContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_tablespaceName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1430);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public CollationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collationName; }
	}

	public final CollationNameContext collationName() throws RecognitionException {
		CollationNameContext _localctx = new CollationNameContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_collationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1432);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1434);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public CteNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cteName; }
	}

	public final CteNameContext cteName() throws RecognitionException {
		CteNameContext _localctx = new CteNameContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_cteName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1436);
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
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<TerminalNode> ID() { return getTokens(MySQLDDLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(MySQLDDLParser.ID, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public IdListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idList; }
	}

	public final IdListContext idList() throws RecognitionException {
		IdListContext _localctx = new IdListContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_idList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1438);
			match(LEFT_PAREN);
			setState(1439);
			match(ID);
			setState(1444);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1440);
				match(COMMA);
				setState(1441);
				match(ID);
				}
				}
				setState(1446);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1447);
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
		public List<TerminalNode> NUMBER() { return getTokens(MySQLDDLParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(MySQLDDLParser.NUMBER, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public TerminalNode OFFSET() { return getToken(MySQLDDLParser.OFFSET, 0); }
		public RangeClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeClause; }
	}

	public final RangeClauseContext rangeClause() throws RecognitionException {
		RangeClauseContext _localctx = new RangeClauseContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_rangeClause);
		int _la;
		try {
			setState(1460);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1449);
				match(NUMBER);
				setState(1454);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1450);
					match(COMMA);
					setState(1451);
					match(NUMBER);
					}
					}
					setState(1456);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1457);
				match(NUMBER);
				setState(1458);
				match(OFFSET);
				setState(1459);
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
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1462);
			match(LEFT_PAREN);
			setState(1463);
			columnName();
			setState(1468);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1464);
				match(COMMA);
				setState(1465);
				columnName();
				}
				}
				setState(1470);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1471);
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

	public static class SelectSpecContext extends ParserRuleContext {
		public TerminalNode HIGH_PRIORITY() { return getToken(MySQLDDLParser.HIGH_PRIORITY, 0); }
		public TerminalNode STRAIGHT_JOIN() { return getToken(MySQLDDLParser.STRAIGHT_JOIN, 0); }
		public TerminalNode SQL_SMALL_RESULT() { return getToken(MySQLDDLParser.SQL_SMALL_RESULT, 0); }
		public TerminalNode SQL_BIG_RESULT() { return getToken(MySQLDDLParser.SQL_BIG_RESULT, 0); }
		public TerminalNode SQL_BUFFER_RESULT() { return getToken(MySQLDDLParser.SQL_BUFFER_RESULT, 0); }
		public TerminalNode SQL_CALC_FOUND_ROWS() { return getToken(MySQLDDLParser.SQL_CALC_FOUND_ROWS, 0); }
		public TerminalNode ALL() { return getToken(MySQLDDLParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(MySQLDDLParser.DISTINCT, 0); }
		public TerminalNode DISTINCTROW() { return getToken(MySQLDDLParser.DISTINCTROW, 0); }
		public TerminalNode SQL_CACHE() { return getToken(MySQLDDLParser.SQL_CACHE, 0); }
		public TerminalNode SQL_NO_CACHE() { return getToken(MySQLDDLParser.SQL_NO_CACHE, 0); }
		public SelectSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectSpec; }
	}

	public final SelectSpecContext selectSpec() throws RecognitionException {
		SelectSpecContext _localctx = new SelectSpecContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_selectSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1474);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				{
				setState(1473);
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
			setState(1477);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,231,_ctx) ) {
			case 1:
				{
				setState(1476);
				match(HIGH_PRIORITY);
				}
				break;
			}
			setState(1480);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(1479);
				match(STRAIGHT_JOIN);
				}
				break;
			}
			setState(1483);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,233,_ctx) ) {
			case 1:
				{
				setState(1482);
				match(SQL_SMALL_RESULT);
				}
				break;
			}
			setState(1486);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,234,_ctx) ) {
			case 1:
				{
				setState(1485);
				match(SQL_BIG_RESULT);
				}
				break;
			}
			setState(1489);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				{
				setState(1488);
				match(SQL_BUFFER_RESULT);
				}
				break;
			}
			setState(1492);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,236,_ctx) ) {
			case 1:
				{
				setState(1491);
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
			setState(1495);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,237,_ctx) ) {
			case 1:
				{
				setState(1494);
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
		enterRule(_localctx, 168, RULE_caseExpress);
		try {
			setState(1499);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,238,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1497);
				caseCond();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1498);
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
		public TerminalNode CASE() { return getToken(MySQLDDLParser.CASE, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public TerminalNode END() { return getToken(MySQLDDLParser.END, 0); }
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
		enterRule(_localctx, 170, RULE_caseComp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1501);
			match(CASE);
			setState(1502);
			simpleExpr(0);
			setState(1504); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1503);
				caseWhenComp();
				}
				}
				setState(1506); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(1509);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(1508);
				elseResult();
				}
			}

			setState(1511);
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
		public TerminalNode WHEN() { return getToken(MySQLDDLParser.WHEN, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public TerminalNode THEN() { return getToken(MySQLDDLParser.THEN, 0); }
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
		enterRule(_localctx, 172, RULE_caseWhenComp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1513);
			match(WHEN);
			setState(1514);
			simpleExpr(0);
			setState(1515);
			match(THEN);
			setState(1516);
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
		public TerminalNode CASE() { return getToken(MySQLDDLParser.CASE, 0); }
		public TerminalNode END() { return getToken(MySQLDDLParser.END, 0); }
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
		enterRule(_localctx, 174, RULE_caseCond);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1518);
			match(CASE);
			setState(1520); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1519);
				whenResult();
				}
				}
				setState(1522); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(1525);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(1524);
				elseResult();
				}
			}

			setState(1527);
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
		public TerminalNode WHEN() { return getToken(MySQLDDLParser.WHEN, 0); }
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public TerminalNode THEN() { return getToken(MySQLDDLParser.THEN, 0); }
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
		enterRule(_localctx, 176, RULE_whenResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1529);
			match(WHEN);
			setState(1530);
			booleanPrimary(0);
			setState(1531);
			match(THEN);
			setState(1532);
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
		public TerminalNode ELSE() { return getToken(MySQLDDLParser.ELSE, 0); }
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
		enterRule(_localctx, 178, RULE_elseResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1534);
			match(ELSE);
			setState(1535);
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
		enterRule(_localctx, 180, RULE_caseResult);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1537);
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

	public static class IdListWithEmptyContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
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
		enterRule(_localctx, 182, RULE_idListWithEmpty);
		try {
			setState(1542);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(1539);
				match(LEFT_PAREN);
				setState(1540);
				match(RIGHT_PAREN);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1541);
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
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public TableReferencesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReferences; }
	}

	public final TableReferencesContext tableReferences() throws RecognitionException {
		TableReferencesContext _localctx = new TableReferencesContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_tableReferences);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1544);
			tableReference();
			setState(1549);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1545);
				match(COMMA);
				setState(1546);
				tableReference();
				}
				}
				setState(1551);
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
		enterRule(_localctx, 186, RULE_tableReference);
		int _la;
		try {
			setState(1566);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,247,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1555); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1552);
					tableFactor();
					setState(1553);
					joinTable();
					}
					}
					setState(1557); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==LEFT_PAREN || _la==ID );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1559);
				tableFactor();
				setState(1561); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1560);
					joinTable();
					}
					}
					setState(1563); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==STRAIGHT_JOIN || ((((_la - 199)) & ~0x3f) == 0 && ((1L << (_la - 199)) & ((1L << (INNER - 199)) | (1L << (CROSS - 199)) | (1L << (JOIN - 199)) | (1L << (LEFT - 199)) | (1L << (RIGHT - 199)) | (1L << (NATURAL - 199)))) != 0) );
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1565);
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
		public TerminalNode PARTITION() { return getToken(MySQLDDLParser.PARTITION, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public IndexHintListContext indexHintList() {
			return getRuleContext(IndexHintListContext.class,0);
		}
		public TerminalNode AS() { return getToken(MySQLDDLParser.AS, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TableFactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableFactor; }
	}

	public final TableFactorContext tableFactor() throws RecognitionException {
		TableFactorContext _localctx = new TableFactorContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_tableFactor);
		int _la;
		try {
			setState(1592);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,253,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1568);
				tableName();
				setState(1571);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(1569);
					match(PARTITION);
					setState(1570);
					idList();
					}
				}

				setState(1577);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,250,_ctx) ) {
				case 1:
					{
					setState(1574);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(1573);
						match(AS);
						}
					}

					setState(1576);
					alias();
					}
					break;
				}
				setState(1580);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USE || _la==IGNORE) {
					{
					setState(1579);
					indexHintList();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1582);
				subquery();
				setState(1584);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(1583);
					match(AS);
					}
				}

				setState(1586);
				alias();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1588);
				match(LEFT_PAREN);
				setState(1589);
				tableReferences();
				setState(1590);
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
		public TerminalNode JOIN() { return getToken(MySQLDDLParser.JOIN, 0); }
		public TableFactorContext tableFactor() {
			return getRuleContext(TableFactorContext.class,0);
		}
		public JoinConditionContext joinCondition() {
			return getRuleContext(JoinConditionContext.class,0);
		}
		public TerminalNode INNER() { return getToken(MySQLDDLParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(MySQLDDLParser.CROSS, 0); }
		public TerminalNode STRAIGHT_JOIN() { return getToken(MySQLDDLParser.STRAIGHT_JOIN, 0); }
		public TerminalNode LEFT() { return getToken(MySQLDDLParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(MySQLDDLParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(MySQLDDLParser.OUTER, 0); }
		public TerminalNode NATURAL() { return getToken(MySQLDDLParser.NATURAL, 0); }
		public JoinTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinTable; }
	}

	public final JoinTableContext joinTable() throws RecognitionException {
		JoinTableContext _localctx = new JoinTableContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_joinTable);
		int _la;
		try {
			setState(1624);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,258,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1595);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER || _la==CROSS) {
					{
					setState(1594);
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

				setState(1597);
				match(JOIN);
				setState(1598);
				tableFactor();
				setState(1600);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON || _la==USING) {
					{
					setState(1599);
					joinCondition();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1602);
				match(STRAIGHT_JOIN);
				setState(1603);
				tableFactor();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1604);
				match(STRAIGHT_JOIN);
				setState(1605);
				tableFactor();
				setState(1606);
				joinCondition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1608);
				_la = _input.LA(1);
				if ( !(_la==LEFT || _la==RIGHT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1610);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(1609);
					match(OUTER);
					}
				}

				setState(1612);
				match(JOIN);
				setState(1613);
				tableFactor();
				setState(1614);
				joinCondition();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1616);
				match(NATURAL);
				setState(1620);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case INNER:
					{
					setState(1617);
					match(INNER);
					}
					break;
				case LEFT:
				case RIGHT:
					{
					setState(1618);
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
					setState(1619);
					match(OUTER);
					}
					}
					break;
				case JOIN:
					break;
				default:
					break;
				}
				setState(1622);
				match(JOIN);
				setState(1623);
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
		public TerminalNode ON() { return getToken(MySQLDDLParser.ON, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode USING() { return getToken(MySQLDDLParser.USING, 0); }
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
		enterRule(_localctx, 192, RULE_joinCondition);
		try {
			setState(1630);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(1626);
				match(ON);
				setState(1627);
				expr(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(1628);
				match(USING);
				setState(1629);
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
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public IndexHintListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexHintList; }
	}

	public final IndexHintListContext indexHintList() throws RecognitionException {
		IndexHintListContext _localctx = new IndexHintListContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_indexHintList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1632);
			indexHint();
			setState(1637);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1633);
					match(COMMA);
					setState(1634);
					indexHint();
					}
					} 
				}
				setState(1639);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
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
		public TerminalNode USE() { return getToken(MySQLDDLParser.USE, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TerminalNode INDEX() { return getToken(MySQLDDLParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(MySQLDDLParser.KEY, 0); }
		public List<TerminalNode> FOR() { return getTokens(MySQLDDLParser.FOR); }
		public TerminalNode FOR(int i) {
			return getToken(MySQLDDLParser.FOR, i);
		}
		public List<TerminalNode> JOIN() { return getTokens(MySQLDDLParser.JOIN); }
		public TerminalNode JOIN(int i) {
			return getToken(MySQLDDLParser.JOIN, i);
		}
		public List<TerminalNode> ORDER() { return getTokens(MySQLDDLParser.ORDER); }
		public TerminalNode ORDER(int i) {
			return getToken(MySQLDDLParser.ORDER, i);
		}
		public List<TerminalNode> BY() { return getTokens(MySQLDDLParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(MySQLDDLParser.BY, i);
		}
		public List<TerminalNode> GROUP() { return getTokens(MySQLDDLParser.GROUP); }
		public TerminalNode GROUP(int i) {
			return getToken(MySQLDDLParser.GROUP, i);
		}
		public TerminalNode IGNORE() { return getToken(MySQLDDLParser.IGNORE, 0); }
		public IndexHintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexHint; }
	}

	public final IndexHintContext indexHint() throws RecognitionException {
		IndexHintContext _localctx = new IndexHintContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_indexHint);
		int _la;
		try {
			setState(1672);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case USE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1640);
				match(USE);
				setState(1641);
				_la = _input.LA(1);
				if ( !(_la==INDEX || _la==KEY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1652);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FOR) {
					{
					{
					setState(1642);
					match(FOR);
					setState(1648);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case JOIN:
						{
						setState(1643);
						match(JOIN);
						}
						break;
					case ORDER:
						{
						setState(1644);
						match(ORDER);
						setState(1645);
						match(BY);
						}
						break;
					case GROUP:
						{
						setState(1646);
						match(GROUP);
						setState(1647);
						match(BY);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					}
					setState(1654);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1655);
				idList();
				}
				break;
			case IGNORE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1656);
				match(IGNORE);
				setState(1657);
				_la = _input.LA(1);
				if ( !(_la==INDEX || _la==KEY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1668);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FOR) {
					{
					{
					setState(1658);
					match(FOR);
					setState(1664);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case JOIN:
						{
						setState(1659);
						match(JOIN);
						}
						break;
					case ORDER:
						{
						setState(1660);
						match(ORDER);
						setState(1661);
						match(BY);
						}
						break;
					case GROUP:
						{
						setState(1662);
						match(GROUP);
						setState(1663);
						match(BY);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					}
					setState(1670);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1671);
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
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TerminalNode NOT() { return getToken(MySQLDDLParser.NOT, 0); }
		public TerminalNode NOT_SYM() { return getToken(MySQLDDLParser.NOT_SYM, 0); }
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public TerminalNode OR() { return getToken(MySQLDDLParser.OR, 0); }
		public TerminalNode OR_SYM() { return getToken(MySQLDDLParser.OR_SYM, 0); }
		public TerminalNode XOR() { return getToken(MySQLDDLParser.XOR, 0); }
		public TerminalNode AND() { return getToken(MySQLDDLParser.AND, 0); }
		public TerminalNode AND_SYM() { return getToken(MySQLDDLParser.AND_SYM, 0); }
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
		int _startState = 198;
		enterRecursionRule(_localctx, 198, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1684);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,266,_ctx) ) {
			case 1:
				{
				setState(1675);
				match(LEFT_PAREN);
				setState(1676);
				expr(0);
				setState(1677);
				match(RIGHT_PAREN);
				}
				break;
			case 2:
				{
				setState(1679);
				match(NOT);
				setState(1680);
				expr(3);
				}
				break;
			case 3:
				{
				setState(1681);
				match(NOT_SYM);
				setState(1682);
				expr(2);
				}
				break;
			case 4:
				{
				setState(1683);
				booleanPrimary(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1703);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,268,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1701);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,267,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(1686);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(1687);
						match(OR);
						setState(1688);
						expr(10);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(1689);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(1690);
						match(OR_SYM);
						setState(1691);
						expr(9);
						}
						break;
					case 3:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(1692);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(1693);
						match(XOR);
						setState(1694);
						expr(8);
						}
						break;
					case 4:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(1695);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(1696);
						match(AND);
						setState(1697);
						expr(7);
						}
						break;
					case 5:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(1698);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(1699);
						match(AND_SYM);
						setState(1700);
						expr(6);
						}
						break;
					}
					} 
				}
				setState(1705);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,268,_ctx);
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
		public TerminalNode IS() { return getToken(MySQLDDLParser.IS, 0); }
		public TerminalNode TRUE() { return getToken(MySQLDDLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MySQLDDLParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(MySQLDDLParser.UNKNOWN, 0); }
		public TerminalNode NULL() { return getToken(MySQLDDLParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(MySQLDDLParser.NOT, 0); }
		public TerminalNode SAFE_EQ() { return getToken(MySQLDDLParser.SAFE_EQ, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode ALL() { return getToken(MySQLDDLParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(MySQLDDLParser.ANY, 0); }
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
		int _startState = 200;
		enterRecursionRule(_localctx, 200, RULE_booleanPrimary, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1707);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(1729);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1727);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,270,_ctx) ) {
					case 1:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(1709);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(1710);
						match(IS);
						setState(1712);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(1711);
							match(NOT);
							}
						}

						setState(1714);
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
						setState(1715);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(1716);
						match(SAFE_EQ);
						setState(1717);
						predicate();
						}
						break;
					case 3:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(1718);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1719);
						comparisonOperator();
						setState(1720);
						predicate();
						}
						break;
					case 4:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(1722);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1723);
						comparisonOperator();
						setState(1724);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1725);
						subquery();
						}
						break;
					}
					} 
				}
				setState(1731);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
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
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public TerminalNode GTE() { return getToken(MySQLDDLParser.GTE, 0); }
		public TerminalNode GT() { return getToken(MySQLDDLParser.GT, 0); }
		public TerminalNode LTE() { return getToken(MySQLDDLParser.LTE, 0); }
		public TerminalNode LT() { return getToken(MySQLDDLParser.LT, 0); }
		public TerminalNode NEQ_SYM() { return getToken(MySQLDDLParser.NEQ_SYM, 0); }
		public TerminalNode NEQ() { return getToken(MySQLDDLParser.NEQ, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1732);
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
		public TerminalNode IN() { return getToken(MySQLDDLParser.IN, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode NOT() { return getToken(MySQLDDLParser.NOT, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public TerminalNode BETWEEN() { return getToken(MySQLDDLParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(MySQLDDLParser.AND, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode SOUNDS() { return getToken(MySQLDDLParser.SOUNDS, 0); }
		public TerminalNode LIKE() { return getToken(MySQLDDLParser.LIKE, 0); }
		public List<TerminalNode> ESCAPE() { return getTokens(MySQLDDLParser.ESCAPE); }
		public TerminalNode ESCAPE(int i) {
			return getToken(MySQLDDLParser.ESCAPE, i);
		}
		public TerminalNode REGEXP() { return getToken(MySQLDDLParser.REGEXP, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_predicate);
		int _la;
		try {
			int _alt;
			setState(1792);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,279,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1734);
				bitExpr(0);
				setState(1736);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1735);
					match(NOT);
					}
				}

				setState(1738);
				match(IN);
				setState(1739);
				subquery();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1741);
				bitExpr(0);
				setState(1743);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1742);
					match(NOT);
					}
				}

				setState(1745);
				match(IN);
				setState(1746);
				match(LEFT_PAREN);
				setState(1747);
				simpleExpr(0);
				setState(1752);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1748);
					match(COMMA);
					setState(1749);
					simpleExpr(0);
					}
					}
					setState(1754);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1755);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1757);
				bitExpr(0);
				setState(1759);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1758);
					match(NOT);
					}
				}

				setState(1761);
				match(BETWEEN);
				setState(1762);
				simpleExpr(0);
				setState(1763);
				match(AND);
				setState(1764);
				predicate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1766);
				bitExpr(0);
				setState(1767);
				match(SOUNDS);
				setState(1768);
				match(LIKE);
				setState(1769);
				simpleExpr(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1771);
				bitExpr(0);
				setState(1773);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1772);
					match(NOT);
					}
				}

				setState(1775);
				match(LIKE);
				setState(1776);
				simpleExpr(0);
				setState(1781);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,277,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1777);
						match(ESCAPE);
						setState(1778);
						simpleExpr(0);
						}
						} 
					}
					setState(1783);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,277,_ctx);
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1784);
				bitExpr(0);
				setState(1786);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1785);
					match(NOT);
					}
				}

				setState(1788);
				match(REGEXP);
				setState(1789);
				simpleExpr(0);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1791);
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
		public TerminalNode BIT_INCLUSIVE_OR() { return getToken(MySQLDDLParser.BIT_INCLUSIVE_OR, 0); }
		public TerminalNode BIT_AND() { return getToken(MySQLDDLParser.BIT_AND, 0); }
		public TerminalNode SIGNED_LEFT_SHIFT() { return getToken(MySQLDDLParser.SIGNED_LEFT_SHIFT, 0); }
		public TerminalNode SIGNED_RIGHT_SHIFT() { return getToken(MySQLDDLParser.SIGNED_RIGHT_SHIFT, 0); }
		public TerminalNode PLUS() { return getToken(MySQLDDLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(MySQLDDLParser.MINUS, 0); }
		public TerminalNode ASTERISK() { return getToken(MySQLDDLParser.ASTERISK, 0); }
		public TerminalNode SLASH() { return getToken(MySQLDDLParser.SLASH, 0); }
		public TerminalNode DIV() { return getToken(MySQLDDLParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(MySQLDDLParser.MOD, 0); }
		public TerminalNode MOD_SYM() { return getToken(MySQLDDLParser.MOD_SYM, 0); }
		public TerminalNode BIT_EXCLUSIVE_OR() { return getToken(MySQLDDLParser.BIT_EXCLUSIVE_OR, 0); }
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
		int _startState = 206;
		enterRecursionRule(_localctx, 206, RULE_bitExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1795);
			simpleExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(1835);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,281,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1833);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,280,_ctx) ) {
					case 1:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1797);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(1798);
						match(BIT_INCLUSIVE_OR);
						setState(1799);
						bitExpr(14);
						}
						break;
					case 2:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1800);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(1801);
						match(BIT_AND);
						setState(1802);
						bitExpr(13);
						}
						break;
					case 3:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1803);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(1804);
						match(SIGNED_LEFT_SHIFT);
						setState(1805);
						bitExpr(12);
						}
						break;
					case 4:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1806);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(1807);
						match(SIGNED_RIGHT_SHIFT);
						setState(1808);
						bitExpr(11);
						}
						break;
					case 5:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1809);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(1810);
						match(PLUS);
						setState(1811);
						bitExpr(10);
						}
						break;
					case 6:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1812);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(1813);
						match(MINUS);
						setState(1814);
						bitExpr(9);
						}
						break;
					case 7:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1815);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(1816);
						match(ASTERISK);
						setState(1817);
						bitExpr(8);
						}
						break;
					case 8:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1818);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(1819);
						match(SLASH);
						setState(1820);
						bitExpr(7);
						}
						break;
					case 9:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1821);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(1822);
						match(DIV);
						setState(1823);
						bitExpr(6);
						}
						break;
					case 10:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1824);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(1825);
						match(MOD);
						setState(1826);
						bitExpr(5);
						}
						break;
					case 11:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1827);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1828);
						match(MOD_SYM);
						setState(1829);
						bitExpr(4);
						}
						break;
					case 12:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(1830);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1831);
						match(BIT_EXCLUSIVE_OR);
						setState(1832);
						bitExpr(3);
						}
						break;
					}
					} 
				}
				setState(1837);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,281,_ctx);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TerminalNode PLUS() { return getToken(MySQLDDLParser.PLUS, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode MINUS() { return getToken(MySQLDDLParser.MINUS, 0); }
		public TerminalNode UNARY_BIT_COMPLEMENT() { return getToken(MySQLDDLParser.UNARY_BIT_COMPLEMENT, 0); }
		public TerminalNode NOT_SYM() { return getToken(MySQLDDLParser.NOT_SYM, 0); }
		public TerminalNode BINARY() { return getToken(MySQLDDLParser.BINARY, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public TerminalNode ROW() { return getToken(MySQLDDLParser.ROW, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(MySQLDDLParser.EXISTS, 0); }
		public TerminalNode AND_SYM() { return getToken(MySQLDDLParser.AND_SYM, 0); }
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
		int _startState = 208;
		enterRecursionRule(_localctx, 208, RULE_simpleExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1871);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,283,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(1839);
				functionCall();
				}
				break;
			case 3:
				{
				setState(1840);
				liter();
				}
				break;
			case 4:
				{
				setState(1841);
				match(ID);
				}
				break;
			case 5:
				{
				setState(1842);
				match(PLUS);
				setState(1843);
				simpleExpr(9);
				}
				break;
			case 6:
				{
				setState(1844);
				match(MINUS);
				setState(1845);
				simpleExpr(8);
				}
				break;
			case 7:
				{
				setState(1846);
				match(UNARY_BIT_COMPLEMENT);
				setState(1847);
				simpleExpr(7);
				}
				break;
			case 8:
				{
				setState(1848);
				match(NOT_SYM);
				setState(1849);
				simpleExpr(6);
				}
				break;
			case 9:
				{
				setState(1850);
				match(BINARY);
				setState(1851);
				simpleExpr(5);
				}
				break;
			case 10:
				{
				setState(1852);
				match(LEFT_PAREN);
				setState(1853);
				expr(0);
				setState(1854);
				match(RIGHT_PAREN);
				}
				break;
			case 11:
				{
				setState(1856);
				match(ROW);
				setState(1857);
				match(LEFT_PAREN);
				setState(1858);
				simpleExpr(0);
				setState(1863);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1859);
					match(COMMA);
					setState(1860);
					simpleExpr(0);
					}
					}
					setState(1865);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1866);
				match(RIGHT_PAREN);
				}
				break;
			case 12:
				{
				setState(1868);
				subquery();
				}
				break;
			case 13:
				{
				setState(1869);
				match(EXISTS);
				setState(1870);
				subquery();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1880);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,285,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1878);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,284,_ctx) ) {
					case 1:
						{
						_localctx = new SimpleExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
						setState(1873);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(1874);
						match(AND_SYM);
						setState(1875);
						simpleExpr(11);
						}
						break;
					case 2:
						{
						_localctx = new SimpleExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
						setState(1876);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(1877);
						collateClause();
						}
						break;
					}
					} 
				}
				setState(1882);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,285,_ctx);
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
		public TerminalNode QUESTION() { return getToken(MySQLDDLParser.QUESTION, 0); }
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public TerminalNode TRUE() { return getToken(MySQLDDLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MySQLDDLParser.FALSE, 0); }
		public TerminalNode NULL() { return getToken(MySQLDDLParser.NULL, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(MySQLDDLParser.LEFT_BRACE, 0); }
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TerminalNode STRING() { return getToken(MySQLDDLParser.STRING, 0); }
		public TerminalNode RIGHT_BRACE() { return getToken(MySQLDDLParser.RIGHT_BRACE, 0); }
		public TerminalNode HEX_DIGIT() { return getToken(MySQLDDLParser.HEX_DIGIT, 0); }
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public TerminalNode DATE() { return getToken(MySQLDDLParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(MySQLDDLParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(MySQLDDLParser.TIMESTAMP, 0); }
		public TerminalNode BIT_NUM() { return getToken(MySQLDDLParser.BIT_NUM, 0); }
		public LiterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_liter; }
	}

	public final LiterContext liter() throws RecognitionException {
		LiterContext _localctx = new LiterContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_liter);
		int _la;
		try {
			setState(1909);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1883);
				match(QUESTION);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1884);
				match(NUMBER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1885);
				match(TRUE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1886);
				match(FALSE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1887);
				match(NULL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1888);
				match(LEFT_BRACE);
				setState(1889);
				match(ID);
				setState(1890);
				match(STRING);
				setState(1891);
				match(RIGHT_BRACE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1892);
				match(HEX_DIGIT);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1894);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(1893);
					match(ID);
					}
				}

				setState(1896);
				match(STRING);
				setState(1898);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,287,_ctx) ) {
				case 1:
					{
					setState(1897);
					collateClause();
					}
					break;
				}
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1900);
				_la = _input.LA(1);
				if ( !(((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (DATE - 257)) | (1L << (TIME - 257)) | (1L << (TIMESTAMP - 257)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1901);
				match(STRING);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1903);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(1902);
					match(ID);
					}
				}

				setState(1905);
				match(BIT_NUM);
				setState(1907);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,289,_ctx) ) {
				case 1:
					{
					setState(1906);
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
		enterRule(_localctx, 212, RULE_characterAndCollate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1911);
			characterSet();
			setState(1912);
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
		public TerminalNode SET() { return getToken(MySQLDDLParser.SET, 0); }
		public CharsetNameContext charsetName() {
			return getRuleContext(CharsetNameContext.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(MySQLDDLParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(MySQLDDLParser.CHAR, 0); }
		public TerminalNode CHARSET() { return getToken(MySQLDDLParser.CHARSET, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public CharacterSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSet; }
	}

	public final CharacterSetContext characterSet() throws RecognitionException {
		CharacterSetContext _localctx = new CharacterSetContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_characterSet);
		int _la;
		try {
			setState(1922);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CHARACTER:
			case CHAR:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(1914);
				_la = _input.LA(1);
				if ( !(_la==CHARACTER || _la==CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1915);
				match(SET);
				setState(1916);
				charsetName();
				}
				}
				break;
			case CHARSET:
				enterOuterAlt(_localctx, 2);
				{
				setState(1917);
				match(CHARSET);
				setState(1919);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1918);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1921);
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
		public TerminalNode COLLATE() { return getToken(MySQLDDLParser.COLLATE, 0); }
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public CollateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClause; }
	}

	public final CollateClauseContext collateClause() throws RecognitionException {
		CollateClauseContext _localctx = new CollateClauseContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_collateClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1924);
			match(COLLATE);
			setState(1925);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TerminalNode BINARY() { return getToken(MySQLDDLParser.BINARY, 0); }
		public CharsetNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charsetName; }
	}

	public final CharsetNameContext charsetName() throws RecognitionException {
		CharsetNameContext _localctx = new CharsetNameContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_charsetName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1927);
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
		enterRule(_localctx, 220, RULE_characterAndCollateWithEqual);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1929);
			characterSetWithEqual();
			setState(1930);
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
		public TerminalNode SET() { return getToken(MySQLDDLParser.SET, 0); }
		public CharsetNameContext charsetName() {
			return getRuleContext(CharsetNameContext.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(MySQLDDLParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(MySQLDDLParser.CHAR, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public TerminalNode CHARSET() { return getToken(MySQLDDLParser.CHARSET, 0); }
		public CharacterSetWithEqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSetWithEqual; }
	}

	public final CharacterSetWithEqualContext characterSetWithEqual() throws RecognitionException {
		CharacterSetWithEqualContext _localctx = new CharacterSetWithEqualContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_characterSetWithEqual);
		int _la;
		try {
			setState(1943);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CHARACTER:
			case CHAR:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(1932);
				_la = _input.LA(1);
				if ( !(_la==CHARACTER || _la==CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1933);
				match(SET);
				setState(1935);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1934);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1937);
				charsetName();
				}
				}
				break;
			case CHARSET:
				enterOuterAlt(_localctx, 2);
				{
				setState(1938);
				match(CHARSET);
				setState(1940);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EQ_OR_ASSIGN) {
					{
					setState(1939);
					match(EQ_OR_ASSIGN);
					}
				}

				setState(1942);
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
		public TerminalNode COLLATE() { return getToken(MySQLDDLParser.COLLATE, 0); }
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TerminalNode EQ_OR_ASSIGN() { return getToken(MySQLDDLParser.EQ_OR_ASSIGN, 0); }
		public CollateClauseWithEqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClauseWithEqual; }
	}

	public final CollateClauseWithEqualContext collateClauseWithEqual() throws RecognitionException {
		CollateClauseWithEqualContext _localctx = new CollateClauseWithEqualContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_collateClauseWithEqual);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1945);
			match(COLLATE);
			setState(1947);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_OR_ASSIGN) {
				{
				setState(1946);
				match(EQ_OR_ASSIGN);
				}
			}

			setState(1949);
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

	public static class SelectExprContext extends ParserRuleContext {
		public BitExprContext bitExpr() {
			return getRuleContext(BitExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(MySQLDDLParser.AS, 0); }
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
		enterRule(_localctx, 226, RULE_selectExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1951);
			bitExpr(0);
			setState(1953);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(1952);
				match(AS);
				}
			}

			setState(1956);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(1955);
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
		enterRule(_localctx, 228, RULE_select);
		try {
			setState(1960);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WITH:
				enterOuterAlt(_localctx, 1);
				{
				setState(1958);
				withClause();
				}
				break;
			case SELECT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1959);
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
		public TerminalNode WITH() { return getToken(MySQLDDLParser.WITH, 0); }
		public List<CteClauseContext> cteClause() {
			return getRuleContexts(CteClauseContext.class);
		}
		public CteClauseContext cteClause(int i) {
			return getRuleContext(CteClauseContext.class,i);
		}
		public UnionSelectContext unionSelect() {
			return getRuleContext(UnionSelectContext.class,0);
		}
		public TerminalNode RECURSIVE() { return getToken(MySQLDDLParser.RECURSIVE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public WithClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_withClause; }
	}

	public final WithClauseContext withClause() throws RecognitionException {
		WithClauseContext _localctx = new WithClauseContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_withClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1962);
			match(WITH);
			setState(1964);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RECURSIVE) {
				{
				setState(1963);
				match(RECURSIVE);
				}
			}

			setState(1966);
			cteClause();
			setState(1971);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1967);
				match(COMMA);
				setState(1968);
				cteClause();
				}
				}
				setState(1973);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1974);
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
		public TerminalNode AS() { return getToken(MySQLDDLParser.AS, 0); }
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
		enterRule(_localctx, 232, RULE_cteClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1976);
			cteName();
			setState(1978);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(1977);
				idList();
				}
			}

			setState(1980);
			match(AS);
			setState(1981);
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
		public List<TerminalNode> UNION() { return getTokens(MySQLDDLParser.UNION); }
		public TerminalNode UNION(int i) {
			return getToken(MySQLDDLParser.UNION, i);
		}
		public List<TerminalNode> ALL() { return getTokens(MySQLDDLParser.ALL); }
		public TerminalNode ALL(int i) {
			return getToken(MySQLDDLParser.ALL, i);
		}
		public UnionSelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionSelect; }
	}

	public final UnionSelectContext unionSelect() throws RecognitionException {
		UnionSelectContext _localctx = new UnionSelectContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_unionSelect);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1983);
			selectExpression();
			setState(1991);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==UNION) {
				{
				{
				setState(1984);
				match(UNION);
				setState(1986);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(1985);
					match(ALL);
					}
				}

				setState(1988);
				selectExpression();
				}
				}
				setState(1993);
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
		enterRule(_localctx, 236, RULE_selectExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1994);
			selectClause();
			setState(1996);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(1995);
				fromClause();
				}
			}

			setState(1999);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(1998);
				whereClause();
				}
			}

			setState(2002);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(2001);
				groupByClause();
				}
			}

			setState(2005);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(2004);
				orderByClause();
				}
			}

			setState(2008);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(2007);
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
		public TerminalNode SELECT() { return getToken(MySQLDDLParser.SELECT, 0); }
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
		enterRule(_localctx, 238, RULE_selectClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2010);
			match(SELECT);
			setState(2011);
			selectSpec();
			setState(2012);
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
		public TerminalNode FROM() { return getToken(MySQLDDLParser.FROM, 0); }
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
		enterRule(_localctx, 240, RULE_fromClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2014);
			match(FROM);
			setState(2015);
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
		public TerminalNode WHERE() { return getToken(MySQLDDLParser.WHERE, 0); }
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
		enterRule(_localctx, 242, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2017);
			match(WHERE);
			setState(2018);
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
		public TerminalNode GROUP() { return getToken(MySQLDDLParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(MySQLDDLParser.BY, 0); }
		public List<GroupByItemContext> groupByItem() {
			return getRuleContexts(GroupByItemContext.class);
		}
		public GroupByItemContext groupByItem(int i) {
			return getRuleContext(GroupByItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public TerminalNode WITH() { return getToken(MySQLDDLParser.WITH, 0); }
		public TerminalNode ROLLUP() { return getToken(MySQLDDLParser.ROLLUP, 0); }
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
		enterRule(_localctx, 244, RULE_groupByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2020);
			match(GROUP);
			setState(2021);
			match(BY);
			setState(2022);
			groupByItem();
			setState(2027);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2023);
				match(COMMA);
				setState(2024);
				groupByItem();
				}
				}
				setState(2029);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2032);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(2030);
				match(WITH);
				setState(2031);
				match(ROLLUP);
				}
			}

			setState(2035);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(2034);
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
		public TerminalNode HAVING() { return getToken(MySQLDDLParser.HAVING, 0); }
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
		enterRule(_localctx, 246, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2037);
			match(HAVING);
			setState(2038);
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
		public TerminalNode ORDER() { return getToken(MySQLDDLParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(MySQLDDLParser.BY, 0); }
		public List<GroupByItemContext> groupByItem() {
			return getRuleContexts(GroupByItemContext.class);
		}
		public GroupByItemContext groupByItem(int i) {
			return getRuleContext(GroupByItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2040);
			match(ORDER);
			setState(2041);
			match(BY);
			setState(2042);
			groupByItem();
			setState(2047);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2043);
				match(COMMA);
				setState(2044);
				groupByItem();
				}
				}
				setState(2049);
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
		public TerminalNode NUMBER() { return getToken(MySQLDDLParser.NUMBER, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ASC() { return getToken(MySQLDDLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(MySQLDDLParser.DESC, 0); }
		public GroupByItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByItem; }
	}

	public final GroupByItemContext groupByItem() throws RecognitionException {
		GroupByItemContext _localctx = new GroupByItemContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_groupByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2053);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,314,_ctx) ) {
			case 1:
				{
				setState(2050);
				columnName();
				}
				break;
			case 2:
				{
				setState(2051);
				match(NUMBER);
				}
				break;
			case 3:
				{
				setState(2052);
				expr(0);
				}
				break;
			}
			setState(2056);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(2055);
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
		public TerminalNode LIMIT() { return getToken(MySQLDDLParser.LIMIT, 0); }
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
		enterRule(_localctx, 252, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2058);
			match(LIMIT);
			setState(2059);
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
		public TerminalNode PARTITION() { return getToken(MySQLDDLParser.PARTITION, 0); }
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
		enterRule(_localctx, 254, RULE_partitionClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2061);
			match(PARTITION);
			setState(2062);
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
		public TerminalNode ID() { return getToken(MySQLDDLParser.ID, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2064);
			match(ID);
			setState(2065);
			match(LEFT_PAREN);
			setState(2075);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,317,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(2067);
				expr(0);
				setState(2072);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2068);
					match(COMMA);
					setState(2069);
					expr(0);
					}
					}
					setState(2074);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(2077);
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
		public TerminalNode ASTERISK() { return getToken(MySQLDDLParser.ASTERISK, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MySQLDDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MySQLDDLParser.COMMA, i);
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
		enterRule(_localctx, 258, RULE_selectExprs);
		int _la;
		try {
			setState(2099);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,321,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(2079);
				match(ASTERISK);
				setState(2084);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2080);
					match(COMMA);
					setState(2081);
					selectExpr();
					}
					}
					setState(2086);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2087);
				selectExpr();
				setState(2090);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,319,_ctx) ) {
				case 1:
					{
					setState(2088);
					match(COMMA);
					setState(2089);
					match(ASTERISK);
					}
					break;
				}
				setState(2096);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2092);
					match(COMMA);
					setState(2093);
					selectExpr();
					}
					}
					setState(2098);
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
		public TerminalNode LEFT_PAREN() { return getToken(MySQLDDLParser.LEFT_PAREN, 0); }
		public UnionSelectContext unionSelect() {
			return getRuleContext(UnionSelectContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(MySQLDDLParser.RIGHT_PAREN, 0); }
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2101);
			match(LEFT_PAREN);
			setState(2102);
			unionSelect();
			setState(2103);
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
		public DropIndexContext dropIndex() {
			return getRuleContext(DropIndexContext.class,0);
		}
		public CreateIndexContext createIndex() {
			return getRuleContext(CreateIndexContext.class,0);
		}
		public DropTableContext dropTable() {
			return getRuleContext(DropTableContext.class,0);
		}
		public TruncateTableContext truncateTable() {
			return getRuleContext(TruncateTableContext.class,0);
		}
		public AlterTableContext alterTable() {
			return getRuleContext(AlterTableContext.class,0);
		}
		public CreateTableContext createTable() {
			return getRuleContext(CreateTableContext.class,0);
		}
		public ExecuteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execute; }
	}

	public final ExecuteContext execute() throws RecognitionException {
		ExecuteContext _localctx = new ExecuteContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_execute);
		try {
			setState(2111);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,322,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2105);
				dropIndex();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2106);
				createIndex();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2107);
				dropTable();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2108);
				truncateTable();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2109);
				alterTable();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2110);
				createTable();
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

	public static class AlterTableContext extends ParserRuleContext {
		public TerminalNode ALTER() { return getToken(MySQLDDLParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(MySQLDDLParser.TABLE, 0); }
		public PrefixTableNameContext prefixTableName() {
			return getRuleContext(PrefixTableNameContext.class,0);
		}
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public AlterSpecificationsContext alterSpecifications() {
			return getRuleContext(AlterSpecificationsContext.class,0);
		}
		public PartitionOptionsContext partitionOptions() {
			return getRuleContext(PartitionOptionsContext.class,0);
		}
		public AlterTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterTable; }
	}

	public final AlterTableContext alterTable() throws RecognitionException {
		AlterTableContext _localctx = new AlterTableContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_alterTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2113);
			match(ALTER);
			setState(2114);
			match(TABLE);
			setState(2115);
			prefixTableName();
			setState(2116);
			tableName();
			setState(2118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALGORITHM) | (1L << CHANGE) | (1L << DISCARD) | (1L << CHARACTER) | (1L << CHARSET) | (1L << COLLATE) | (1L << CONVERT) | (1L << REORGANIZE) | (1L << EXCHANGE) | (1L << KEY_BLOCK_SIZE) | (1L << COMMENT) | (1L << AUTO_INCREMENT) | (1L << AVG_ROW_LENGTH) | (1L << CHECKSUM) | (1L << COMPRESSION) | (1L << CONNECTION) | (1L << DATA) | (1L << DELAY_KEY_WRITE) | (1L << ENCRYPTION) | (1L << ENGINE) | (1L << INSERT_METHOD) | (1L << MAX_ROWS) | (1L << MIN_ROWS) | (1L << PACK_KEYS) | (1L << PASSWORD) | (1L << ROW_FORMAT) | (1L << STATS_AUTO_RECALC) | (1L << STATS_PERSISTENT) | (1L << STATS_SAMPLE_PAGES))) != 0) || ((((_la - 146)) & ~0x3f) == 0 && ((1L << (_la - 146)) & ((1L << (WITH - 146)) | (1L << (ORDER - 146)) | (1L << (ALTER - 146)) | (1L << (ADD - 146)) | (1L << (DROP - 146)) | (1L << (ENABLE - 146)) | (1L << (DISABLE - 146)) | (1L << (MODIFY - 146)) | (1L << (RENAME - 146)) | (1L << (IMPORT_ - 146)) | (1L << (TABLESPACE - 146)) | (1L << (TRUNCATE - 146)) | (1L << (ANALYZE - 146)) | (1L << (CHECK - 146)) | (1L << (OPTIMIZE - 146)) | (1L << (REBUILD - 146)) | (1L << (REPAIR - 146)) | (1L << (REMOVE - 146)) | (1L << (UPGRADE - 146)) | (1L << (WITHOUT - 146)) | (1L << (COALESCE - 146)) | (1L << (LOCK - 146)) | (1L << (INDEX - 146)))) != 0) || ((((_la - 212)) & ~0x3f) == 0 && ((1L << (_la - 212)) & ((1L << (FORCE - 212)) | (1L << (UNION - 212)) | (1L << (DEFAULT - 212)) | (1L << (CHAR - 212)))) != 0)) {
				{
				setState(2117);
				alterSpecifications();
				}
			}

			setState(2121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(2120);
				partitionOptions();
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

	public static class PrefixTableNameContext extends ParserRuleContext {
		public PrefixTableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixTableName; }
	}

	public final PrefixTableNameContext prefixTableName() throws RecognitionException {
		PrefixTableNameContext _localctx = new PrefixTableNameContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_prefixTableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
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

	public static class CreateTableContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(MySQLDDLParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(MySQLDDLParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public CreateTableOptionsContext createTableOptions() {
			return getRuleContext(CreateTableOptionsContext.class,0);
		}
		public TerminalNode TEMPORARY() { return getToken(MySQLDDLParser.TEMPORARY, 0); }
		public TerminalNode IF() { return getToken(MySQLDDLParser.IF, 0); }
		public TerminalNode NOT() { return getToken(MySQLDDLParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(MySQLDDLParser.EXISTS, 0); }
		public CreateTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTable; }
	}

	public final CreateTableContext createTable() throws RecognitionException {
		CreateTableContext _localctx = new CreateTableContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_createTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2125);
			match(CREATE);
			setState(2127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMPORARY) {
				{
				setState(2126);
				match(TEMPORARY);
				}
			}

			setState(2129);
			match(TABLE);
			setState(2133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(2130);
				match(IF);
				setState(2131);
				match(NOT);
				setState(2132);
				match(EXISTS);
				}
			}

			setState(2135);
			tableName();
			setState(2136);
			createTableOptions();
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
		case 99:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 100:
			return booleanPrimary_sempred((BooleanPrimaryContext)_localctx, predIndex);
		case 103:
			return bitExpr_sempred((BitExprContext)_localctx, predIndex);
		case 104:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0118\u085d\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t"+
		"k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\4"+
		"w\tw\4x\tx\4y\ty\4z\tz\4{\t{\4|\t|\4}\t}\4~\t~\4\177\t\177\4\u0080\t\u0080"+
		"\4\u0081\t\u0081\4\u0082\t\u0082\4\u0083\t\u0083\4\u0084\t\u0084\4\u0085"+
		"\t\u0085\4\u0086\t\u0086\4\u0087\t\u0087\4\u0088\t\u0088\3\2\3\2\5\2\u0113"+
		"\n\2\3\2\3\2\3\2\5\2\u0118\n\2\3\2\3\2\3\2\3\2\5\2\u011e\n\2\3\2\3\2\7"+
		"\2\u0122\n\2\f\2\16\2\u0125\13\2\3\3\3\3\3\3\3\3\3\3\7\3\u012c\n\3\f\3"+
		"\16\3\u012f\13\3\3\4\3\4\5\4\u0133\n\4\3\4\3\4\3\4\5\4\u0138\n\4\3\4\3"+
		"\4\3\4\7\4\u013d\n\4\f\4\16\4\u0140\13\4\3\4\5\4\u0143\n\4\3\5\3\5\5\5"+
		"\u0147\n\5\3\5\3\5\3\6\3\6\3\6\5\6\u014e\n\6\3\7\3\7\3\7\3\7\5\7\u0154"+
		"\n\7\3\7\5\7\u0157\n\7\3\b\3\b\3\b\7\b\u015c\n\b\f\b\16\b\u015f\13\b\3"+
		"\t\3\t\3\t\3\t\5\t\u0165\n\t\5\t\u0167\n\t\3\n\3\n\3\n\3\13\3\13\3\13"+
		"\3\13\5\13\u0170\n\13\3\13\5\13\u0173\n\13\3\13\5\13\u0176\n\13\3\13\5"+
		"\13\u0179\n\13\3\13\5\13\u017c\n\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\5\f"+
		"\u0185\n\f\3\r\3\r\3\r\3\16\3\16\3\16\7\16\u018d\n\16\f\16\16\16\u0190"+
		"\13\16\3\17\3\17\3\17\5\17\u0195\n\17\3\17\3\17\5\17\u0199\n\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\5\17\u01a2\n\17\3\17\3\17\3\17\3\17\3\17"+
		"\5\17\u01a9\n\17\3\17\3\17\5\17\u01ad\n\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\7\17\u01c6\n\17\f\17\16\17\u01c9\13\17\6\17\u01cb\n"+
		"\17\r\17\16\17\u01cc\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\5\17\u01dd\n\17\3\17\3\17\3\17\3\17\3\17\5\17\u01e4"+
		"\n\17\3\17\3\17\3\17\3\17\3\17\5\17\u01eb\n\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u01fe"+
		"\n\17\3\17\3\17\3\17\3\17\5\17\u0204\n\17\3\17\3\17\3\17\3\17\5\17\u020a"+
		"\n\17\3\17\3\17\3\17\3\17\5\17\u0210\n\17\3\17\3\17\3\17\3\17\5\17\u0216"+
		"\n\17\3\17\3\17\3\17\3\17\5\17\u021c\n\17\3\17\3\17\3\17\3\17\5\17\u0222"+
		"\n\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u022b\n\20\3\21\3\21\5\21"+
		"\u022f\n\21\3\22\3\22\5\22\u0233\n\22\3\22\3\22\3\23\3\23\3\23\3\23\3"+
		"\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\5\26\u0247"+
		"\n\26\3\26\3\26\3\27\3\27\5\27\u024d\n\27\3\27\3\27\3\27\3\27\5\27\u0253"+
		"\n\27\3\30\3\30\5\30\u0257\n\30\3\30\3\30\3\31\3\31\5\31\u025d\n\31\3"+
		"\31\3\31\3\32\3\32\5\32\u0263\n\32\3\32\5\32\u0266\n\32\3\32\3\32\3\33"+
		"\3\33\3\34\5\34\u026d\n\34\3\34\5\34\u0270\n\34\3\34\3\34\5\34\u0274\n"+
		"\34\3\35\3\35\3\35\3\35\5\35\u027a\n\35\3\36\3\36\3\36\3\36\7\36\u0280"+
		"\n\36\f\36\16\36\u0283\13\36\3\36\3\36\3\37\3\37\5\37\u0289\n\37\5\37"+
		"\u028b\n\37\3\37\3\37\3\37\5\37\u0290\n\37\3 \3 \3 \5 \u0295\n \3 \3 "+
		"\5 \u0299\n \3!\3!\5!\u029d\n!\3!\5!\u02a0\n!\3!\5!\u02a3\n!\3!\3!\5!"+
		"\u02a7\n!\3\"\3\"\3\"\5\"\u02ac\n\"\3\"\3\"\3\"\3\"\7\"\u02b2\n\"\f\""+
		"\16\"\u02b5\13\"\3\"\3\"\3\"\3#\3#\5#\u02bc\n#\3#\7#\u02bf\n#\f#\16#\u02c2"+
		"\13#\3$\3$\5$\u02c6\n$\3$\3$\3$\5$\u02cb\n$\3$\3$\5$\u02cf\n$\3$\3$\3"+
		"$\5$\u02d4\n$\3$\3$\5$\u02d8\n$\3$\3$\3$\5$\u02dd\n$\3$\3$\3$\5$\u02e2"+
		"\n$\3$\3$\3$\5$\u02e7\n$\3$\3$\3$\3$\5$\u02ed\n$\3$\3$\3$\5$\u02f2\n$"+
		"\3$\3$\3$\5$\u02f7\n$\3$\3$\3$\5$\u02fc\n$\3$\3$\3$\5$\u0301\n$\3$\3$"+
		"\3$\5$\u0306\n$\3$\3$\3$\5$\u030b\n$\3$\3$\3$\5$\u0310\n$\3$\3$\3$\5$"+
		"\u0315\n$\3$\3$\3$\5$\u031a\n$\3$\3$\3$\5$\u031f\n$\3$\3$\3$\5$\u0324"+
		"\n$\3$\3$\3$\5$\u0329\n$\3$\3$\3$\5$\u032e\n$\3$\3$\3$\3$\3$\5$\u0335"+
		"\n$\3$\3$\5$\u0339\n$\3$\5$\u033c\n$\3%\3%\3%\3&\3&\3&\5&\u0344\n&\3\'"+
		"\3\'\5\'\u0348\n\'\3\'\3\'\5\'\u034c\n\'\3\'\5\'\u034f\n\'\3\'\5\'\u0352"+
		"\n\'\3\'\5\'\u0355\n\'\3\'\3\'\3\'\5\'\u035a\n\'\3\'\5\'\u035d\n\'\3\'"+
		"\5\'\u0360\n\'\3\'\5\'\u0363\n\'\3\'\3\'\3\'\5\'\u0368\n\'\3\'\5\'\u036b"+
		"\n\'\3\'\3\'\5\'\u036f\n\'\3\'\3\'\5\'\u0373\n\'\3\'\5\'\u0376\n\'\3\'"+
		"\5\'\u0379\n\'\3\'\3\'\3\'\3\'\3\'\5\'\u0380\n\'\5\'\u0382\n\'\3\'\3\'"+
		"\3\'\5\'\u0387\n\'\3\'\3\'\5\'\u038b\n\'\3\'\5\'\u038e\n\'\3\'\5\'\u0391"+
		"\n\'\3\'\3\'\5\'\u0395\n\'\3\'\3\'\5\'\u0399\n\'\3\'\5\'\u039c\n\'\3\'"+
		"\5\'\u039f\n\'\3\'\5\'\u03a2\n\'\3\'\5\'\u03a5\n\'\3\'\3\'\5\'\u03a9\n"+
		"\'\3\'\3\'\5\'\u03ad\n\'\3\'\5\'\u03b0\n\'\3\'\5\'\u03b3\n\'\3\'\3\'\3"+
		"\'\3\'\5\'\u03b9\n\'\3\'\3\'\5\'\u03bd\n\'\3\'\5\'\u03c0\n\'\3\'\3\'\5"+
		"\'\u03c4\n\'\3\'\5\'\u03c7\n\'\3\'\5\'\u03ca\n\'\3\'\5\'\u03cd\n\'\3\'"+
		"\5\'\u03d0\n\'\3\'\3\'\3\'\3\'\3\'\7\'\u03d7\n\'\f\'\16\'\u03da\13\'\3"+
		"\'\3\'\5\'\u03de\n\'\3\'\5\'\u03e1\n\'\3\'\3\'\5\'\u03e5\n\'\3\'\5\'\u03e8"+
		"\n\'\3\'\5\'\u03eb\n\'\5\'\u03ed\n\'\3(\3(\5(\u03f1\n(\3)\3)\5)\u03f5"+
		"\n)\3*\3*\3*\3*\3+\3+\3+\3+\3+\3+\3,\5,\u0402\n,\3,\5,\u0405\n,\3,\3,"+
		"\5,\u0409\n,\3-\3-\5-\u040d\n-\5-\u040f\n-\3-\5-\u0412\n-\3-\5-\u0415"+
		"\n-\3-\3-\5-\u0419\n-\3-\3-\5-\u041d\n-\3-\3-\5-\u0421\n-\3-\5-\u0424"+
		"\n-\3.\3.\5.\u0428\n.\3.\3.\3.\3.\3.\5.\u042f\n.\3.\3.\3.\5.\u0434\n."+
		"\3.\3.\5.\u0438\n.\5.\u043a\n.\3.\5.\u043d\n.\3.\5.\u0440\n.\3.\3.\5."+
		"\u0444\n.\3/\3/\3/\3/\3/\3/\3/\3/\3/\5/\u044f\n/\3/\3/\3/\5/\u0454\n/"+
		"\3/\3/\3/\5/\u0459\n/\3/\3/\3/\5/\u045e\n/\3/\3/\3/\5/\u0463\n/\5/\u0465"+
		"\n/\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\60\5\60\u046f\n\60\3\61\3\61"+
		"\3\62\3\62\3\63\3\63\3\63\3\63\7\63\u0479\n\63\f\63\16\63\u047c\13\63"+
		"\3\63\3\63\3\64\3\64\5\64\u0482\n\64\3\65\3\65\3\65\3\65\5\65\u0488\n"+
		"\65\3\65\5\65\u048b\n\65\3\66\3\66\3\67\3\67\3\67\38\38\58\u0494\n8\3"+
		"8\38\38\38\38\38\38\58\u049d\n8\39\39\3:\3:\3;\3;\3;\7;\u04a6\n;\f;\16"+
		";\u04a9\13;\3<\3<\3=\3=\3=\3=\5=\u04b1\n=\3=\3=\5=\u04b5\n=\3=\3=\3=\3"+
		"=\3=\5=\u04bc\n=\5=\u04be\n=\3=\5=\u04c1\n=\3>\5>\u04c4\n>\3>\3>\3>\5"+
		">\u04c9\n>\3>\5>\u04cc\n>\3?\3?\3?\3?\3?\3@\3@\3@\3@\5@\u04d7\n@\3@\3"+
		"@\3A\3A\3A\3A\3B\3B\3B\3C\3C\3C\3C\3C\3C\5C\u04e8\nC\3D\3D\3D\3D\7D\u04ee"+
		"\nD\fD\16D\u04f1\13D\3D\3D\3E\3E\3E\3E\3E\3E\5E\u04fb\nE\5E\u04fd\nE\3"+
		"E\5E\u0500\nE\3E\3E\5E\u0504\nE\3E\5E\u0507\nE\3E\3E\5E\u050b\nE\3E\5"+
		"E\u050e\nE\3E\3E\3E\5E\u0513\nE\3E\5E\u0516\nE\3E\3E\3E\5E\u051b\nE\3"+
		"E\5E\u051e\nE\3E\3E\5E\u0522\nE\3E\5E\u0525\nE\3E\3E\5E\u0529\nE\3E\5"+
		"E\u052c\nE\3E\3E\5E\u0530\nE\3E\5E\u0533\nE\3E\3E\3E\7E\u0538\nE\fE\16"+
		"E\u053b\13E\5E\u053d\nE\3F\3F\3F\3F\3F\5F\u0544\nF\3F\3F\3F\5F\u0549\n"+
		"F\3G\3G\3G\5G\u054e\nG\3G\3G\5G\u0552\nG\3G\5G\u0555\nG\3G\3G\5G\u0559"+
		"\nG\3G\5G\u055c\nG\3G\3G\3G\5G\u0561\nG\3G\5G\u0564\nG\3G\3G\3G\5G\u0569"+
		"\nG\3G\5G\u056c\nG\3G\3G\5G\u0570\nG\3G\5G\u0573\nG\3G\3G\5G\u0577\nG"+
		"\3G\5G\u057a\nG\3G\3G\5G\u057e\nG\3G\5G\u0581\nG\3H\3H\5H\u0585\nH\3I"+
		"\3I\3I\7I\u058a\nI\fI\16I\u058d\13I\3J\3J\3J\3J\3K\3K\3L\3L\3M\3M\3N\3"+
		"N\3O\3O\3P\3P\3Q\3Q\3R\3R\3R\3R\7R\u05a5\nR\fR\16R\u05a8\13R\3R\3R\3S"+
		"\3S\3S\7S\u05af\nS\fS\16S\u05b2\13S\3S\3S\3S\5S\u05b7\nS\3T\3T\3T\3T\7"+
		"T\u05bd\nT\fT\16T\u05c0\13T\3T\3T\3U\5U\u05c5\nU\3U\5U\u05c8\nU\3U\5U"+
		"\u05cb\nU\3U\5U\u05ce\nU\3U\5U\u05d1\nU\3U\5U\u05d4\nU\3U\5U\u05d7\nU"+
		"\3U\5U\u05da\nU\3V\3V\5V\u05de\nV\3W\3W\3W\6W\u05e3\nW\rW\16W\u05e4\3"+
		"W\5W\u05e8\nW\3W\3W\3X\3X\3X\3X\3X\3Y\3Y\6Y\u05f3\nY\rY\16Y\u05f4\3Y\5"+
		"Y\u05f8\nY\3Y\3Y\3Z\3Z\3Z\3Z\3Z\3[\3[\3[\3\\\3\\\3]\3]\3]\5]\u0609\n]"+
		"\3^\3^\3^\7^\u060e\n^\f^\16^\u0611\13^\3_\3_\3_\6_\u0616\n_\r_\16_\u0617"+
		"\3_\3_\6_\u061c\n_\r_\16_\u061d\3_\5_\u0621\n_\3`\3`\3`\5`\u0626\n`\3"+
		"`\5`\u0629\n`\3`\5`\u062c\n`\3`\5`\u062f\n`\3`\3`\5`\u0633\n`\3`\3`\3"+
		"`\3`\3`\3`\5`\u063b\n`\3a\5a\u063e\na\3a\3a\3a\5a\u0643\na\3a\3a\3a\3"+
		"a\3a\3a\3a\3a\5a\u064d\na\3a\3a\3a\3a\3a\3a\3a\3a\5a\u0657\na\3a\3a\5"+
		"a\u065b\na\3b\3b\3b\3b\5b\u0661\nb\3c\3c\3c\7c\u0666\nc\fc\16c\u0669\13"+
		"c\3d\3d\3d\3d\3d\3d\3d\3d\5d\u0673\nd\7d\u0675\nd\fd\16d\u0678\13d\3d"+
		"\3d\3d\3d\3d\3d\3d\3d\3d\5d\u0683\nd\7d\u0685\nd\fd\16d\u0688\13d\3d\5"+
		"d\u068b\nd\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\5e\u0697\ne\3e\3e\3e\3e\3e\3"+
		"e\3e\3e\3e\3e\3e\3e\3e\3e\3e\7e\u06a8\ne\fe\16e\u06ab\13e\3f\3f\3f\3f"+
		"\3f\3f\5f\u06b3\nf\3f\3f\3f\3f\3f\3f\3f\3f\3f\3f\3f\3f\3f\7f\u06c2\nf"+
		"\ff\16f\u06c5\13f\3g\3g\3h\3h\5h\u06cb\nh\3h\3h\3h\3h\3h\5h\u06d2\nh\3"+
		"h\3h\3h\3h\3h\7h\u06d9\nh\fh\16h\u06dc\13h\3h\3h\3h\3h\5h\u06e2\nh\3h"+
		"\3h\3h\3h\3h\3h\3h\3h\3h\3h\3h\3h\5h\u06f0\nh\3h\3h\3h\3h\7h\u06f6\nh"+
		"\fh\16h\u06f9\13h\3h\3h\5h\u06fd\nh\3h\3h\3h\3h\5h\u0703\nh\3i\3i\3i\3"+
		"i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3"+
		"i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\7i\u072c\ni\fi\16i\u072f\13i\3j"+
		"\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\3j\7j"+
		"\u0748\nj\fj\16j\u074b\13j\3j\3j\3j\3j\3j\5j\u0752\nj\3j\3j\3j\3j\3j\7"+
		"j\u0759\nj\fj\16j\u075c\13j\3k\3k\3k\3k\3k\3k\3k\3k\3k\3k\3k\5k\u0769"+
		"\nk\3k\3k\5k\u076d\nk\3k\3k\3k\5k\u0772\nk\3k\3k\5k\u0776\nk\5k\u0778"+
		"\nk\3l\3l\3l\3m\3m\3m\3m\3m\5m\u0782\nm\3m\5m\u0785\nm\3n\3n\3n\3o\3o"+
		"\3p\3p\3p\3q\3q\3q\5q\u0792\nq\3q\3q\3q\5q\u0797\nq\3q\5q\u079a\nq\3r"+
		"\3r\5r\u079e\nr\3r\3r\3s\3s\5s\u07a4\ns\3s\5s\u07a7\ns\3t\3t\5t\u07ab"+
		"\nt\3u\3u\5u\u07af\nu\3u\3u\3u\7u\u07b4\nu\fu\16u\u07b7\13u\3u\3u\3v\3"+
		"v\5v\u07bd\nv\3v\3v\3v\3w\3w\3w\5w\u07c5\nw\3w\7w\u07c8\nw\fw\16w\u07cb"+
		"\13w\3x\3x\5x\u07cf\nx\3x\5x\u07d2\nx\3x\5x\u07d5\nx\3x\5x\u07d8\nx\3"+
		"x\5x\u07db\nx\3y\3y\3y\3y\3z\3z\3z\3{\3{\3{\3|\3|\3|\3|\3|\7|\u07ec\n"+
		"|\f|\16|\u07ef\13|\3|\3|\5|\u07f3\n|\3|\5|\u07f6\n|\3}\3}\3}\3~\3~\3~"+
		"\3~\3~\7~\u0800\n~\f~\16~\u0803\13~\3\177\3\177\3\177\5\177\u0808\n\177"+
		"\3\177\5\177\u080b\n\177\3\u0080\3\u0080\3\u0080\3\u0081\3\u0081\3\u0081"+
		"\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\7\u0082\u0819\n\u0082"+
		"\f\u0082\16\u0082\u081c\13\u0082\5\u0082\u081e\n\u0082\3\u0082\3\u0082"+
		"\3\u0083\3\u0083\3\u0083\7\u0083\u0825\n\u0083\f\u0083\16\u0083\u0828"+
		"\13\u0083\3\u0083\3\u0083\3\u0083\5\u0083\u082d\n\u0083\3\u0083\3\u0083"+
		"\7\u0083\u0831\n\u0083\f\u0083\16\u0083\u0834\13\u0083\5\u0083\u0836\n"+
		"\u0083\3\u0084\3\u0084\3\u0084\3\u0084\3\u0085\3\u0085\3\u0085\3\u0085"+
		"\3\u0085\3\u0085\5\u0085\u0842\n\u0085\3\u0086\3\u0086\3\u0086\3\u0086"+
		"\3\u0086\5\u0086\u0849\n\u0086\3\u0086\5\u0086\u084c\n\u0086\3\u0087\3"+
		"\u0087\3\u0088\3\u0088\5\u0088\u0852\n\u0088\3\u0088\3\u0088\3\u0088\3"+
		"\u0088\5\u0088\u0858\n\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0175\6"+
		"\u00c8\u00ca\u00d0\u00d2\u0089\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36"+
		" \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082"+
		"\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a"+
		"\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2"+
		"\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca"+
		"\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2"+
		"\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa"+
		"\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108\u010a\u010c\u010e\2-\4\2\6"+
		"\6\u00a8\u00a9\3\2OP\4\2\u00d5\u00d5\u0117\u0117\3\2\u00a5\u00a6\4\2\t"+
		"\t\u00af\u00af\4\2\u0094\u0094\u00bd\u00bd\4\2\u0099\u0099\u00b9\u00b9"+
		"\5\2<<\u00ba\u00ba\u00d8\u00d8\5\2\16\17\u00ab\u00ab\u00d8\u00d8\4\2\6"+
		"\6\u00a9\u00a9\3\2\u00d3\u00d4\5\2\3\3\33\33\u00ab\u00ab\4\2\36\36\u00d3"+
		"\u00d3\4\2\4\4$%\4\2\u0083\u0083\u00d8\u00d8\4\2+/\u00d8\u00d8\4\2\64"+
		"\65\u00d8\u00d8\3\2\u00f8\u00fd\3\2\u00fe\u0102\3\2\u0103\u0104\4\2\u0082"+
		"\u0082\u00e6\u00e6\4\2\u0082\u0083\u00e6\u00e6\3\2\u0109\u010a\3\2\u010b"+
		"\u010c\4\2\u010d\u0110\u0116\u0116\3\2\u0111\u0114\4\2\u00bf\u00bf\u0115"+
		"\u0115\4\2+,\u00d8\u00d8\3\2:;\3\2\u0092\u0093\3\2\22\23\4\2\65\65\u0118"+
		"\u0118\3\2TU\5\2AA\u008a\u008a\u008c\u008c\3\2GH\3\2\u00c9\u00ca\3\2\u00cd"+
		"\u00ce\3\2\u00e6\u00e9\3\2\u008a\u008b\3\2qw\3\2\u0103\u0105\4\2\n\n\u0109"+
		"\u0109\4\2\u010b\u010b\u0118\u0118\2\u09a6\2\u0110\3\2\2\2\4\u0126\3\2"+
		"\2\2\6\u0130\3\2\2\2\b\u0144\3\2\2\2\n\u014d\3\2\2\2\f\u014f\3\2\2\2\16"+
		"\u0158\3\2\2\2\20\u0166\3\2\2\2\22\u0168\3\2\2\2\24\u016f\3\2\2\2\26\u0184"+
		"\3\2\2\2\30\u0186\3\2\2\2\32\u0189\3\2\2\2\34\u0221\3\2\2\2\36\u0223\3"+
		"\2\2\2 \u022c\3\2\2\2\"\u0230\3\2\2\2$\u0236\3\2\2\2&\u023a\3\2\2\2(\u023e"+
		"\3\2\2\2*\u0244\3\2\2\2,\u024a\3\2\2\2.\u0254\3\2\2\2\60\u025a\3\2\2\2"+
		"\62\u0265\3\2\2\2\64\u0269\3\2\2\2\66\u026c\3\2\2\28\u0275\3\2\2\2:\u027b"+
		"\3\2\2\2<\u028a\3\2\2\2>\u0291\3\2\2\2@\u029a\3\2\2\2B\u02a8\3\2\2\2D"+
		"\u02b9\3\2\2\2F\u033b\3\2\2\2H\u033d\3\2\2\2J\u0340\3\2\2\2L\u03ec\3\2"+
		"\2\2N\u03ee\3\2\2\2P\u03f2\3\2\2\2R\u03f6\3\2\2\2T\u03fa\3\2\2\2V\u0401"+
		"\3\2\2\2X\u040e\3\2\2\2Z\u0427\3\2\2\2\\\u0445\3\2\2\2^\u046e\3\2\2\2"+
		"`\u0470\3\2\2\2b\u0472\3\2\2\2d\u0474\3\2\2\2f\u0481\3\2\2\2h\u0483\3"+
		"\2\2\2j\u048c\3\2\2\2l\u048e\3\2\2\2n\u049c\3\2\2\2p\u049e\3\2\2\2r\u04a0"+
		"\3\2\2\2t\u04a2\3\2\2\2v\u04aa\3\2\2\2x\u04ac\3\2\2\2z\u04c3\3\2\2\2|"+
		"\u04cd\3\2\2\2~\u04d2\3\2\2\2\u0080\u04da\3\2\2\2\u0082\u04de\3\2\2\2"+
		"\u0084\u04e7\3\2\2\2\u0086\u04e9\3\2\2\2\u0088\u04f4\3\2\2\2\u008a\u053e"+
		"\3\2\2\2\u008c\u054a\3\2\2\2\u008e\u0584\3\2\2\2\u0090\u0586\3\2\2\2\u0092"+
		"\u058e\3\2\2\2\u0094\u0592\3\2\2\2\u0096\u0594\3\2\2\2\u0098\u0596\3\2"+
		"\2\2\u009a\u0598\3\2\2\2\u009c\u059a\3\2\2\2\u009e\u059c\3\2\2\2\u00a0"+
		"\u059e\3\2\2\2\u00a2\u05a0\3\2\2\2\u00a4\u05b6\3\2\2\2\u00a6\u05b8\3\2"+
		"\2\2\u00a8\u05c4\3\2\2\2\u00aa\u05dd\3\2\2\2\u00ac\u05df\3\2\2\2\u00ae"+
		"\u05eb\3\2\2\2\u00b0\u05f0\3\2\2\2\u00b2\u05fb\3\2\2\2\u00b4\u0600\3\2"+
		"\2\2\u00b6\u0603\3\2\2\2\u00b8\u0608\3\2\2\2\u00ba\u060a\3\2\2\2\u00bc"+
		"\u0620\3\2\2\2\u00be\u063a\3\2\2\2\u00c0\u065a\3\2\2\2\u00c2\u0660\3\2"+
		"\2\2\u00c4\u0662\3\2\2\2\u00c6\u068a\3\2\2\2\u00c8\u0696\3\2\2\2\u00ca"+
		"\u06ac\3\2\2\2\u00cc\u06c6\3\2\2\2\u00ce\u0702\3\2\2\2\u00d0\u0704\3\2"+
		"\2\2\u00d2\u0751\3\2\2\2\u00d4\u0777\3\2\2\2\u00d6\u0779\3\2\2\2\u00d8"+
		"\u0784\3\2\2\2\u00da\u0786\3\2\2\2\u00dc\u0789\3\2\2\2\u00de\u078b\3\2"+
		"\2\2\u00e0\u0799\3\2\2\2\u00e2\u079b\3\2\2\2\u00e4\u07a1\3\2\2\2\u00e6"+
		"\u07aa\3\2\2\2\u00e8\u07ac\3\2\2\2\u00ea\u07ba\3\2\2\2\u00ec\u07c1\3\2"+
		"\2\2\u00ee\u07cc\3\2\2\2\u00f0\u07dc\3\2\2\2\u00f2\u07e0\3\2\2\2\u00f4"+
		"\u07e3\3\2\2\2\u00f6\u07e6\3\2\2\2\u00f8\u07f7\3\2\2\2\u00fa\u07fa\3\2"+
		"\2\2\u00fc\u0807\3\2\2\2\u00fe\u080c\3\2\2\2\u0100\u080f\3\2\2\2\u0102"+
		"\u0812\3\2\2\2\u0104\u0835\3\2\2\2\u0106\u0837\3\2\2\2\u0108\u0841\3\2"+
		"\2\2\u010a\u0843\3\2\2\2\u010c\u084d\3\2\2\2\u010e\u084f\3\2\2\2\u0110"+
		"\u0112\7\u009f\2\2\u0111\u0113\t\2\2\2\u0112\u0111\3\2\2\2\u0112\u0113"+
		"\3\2\2\2\u0113\u0114\3\2\2\2\u0114\u0115\7\u00d3\2\2\u0115\u0117\5j\66"+
		"\2\u0116\u0118\5l\67\2\u0117\u0116\3\2\2\2\u0117\u0118\3\2\2\2\u0118\u0119"+
		"\3\2\2\2\u0119\u011a\7\u00cc\2\2\u011a\u011b\5\u0096L\2\u011b\u011d\5"+
		"d\63\2\u011c\u011e\5n8\2\u011d\u011c\3\2\2\2\u011d\u011e\3\2\2\2\u011e"+
		"\u0123\3\2\2\2\u011f\u0122\5.\30\2\u0120\u0122\5\60\31\2\u0121\u011f\3"+
		"\2\2\2\u0121\u0120\3\2\2\2\u0122\u0125\3\2\2\2\u0123\u0121\3\2\2\2\u0123"+
		"\u0124\3\2\2\2\u0124\3\3\2\2\2\u0125\u0123\3\2\2\2\u0126\u0127\5$\23\2"+
		"\u0127\u0128\7\u00cc\2\2\u0128\u012d\5\u0096L\2\u0129\u012c\5.\30\2\u012a"+
		"\u012c\5\60\31\2\u012b\u0129\3\2\2\2\u012b\u012a\3\2\2\2\u012c\u012f\3"+
		"\2\2\2\u012d\u012b\3\2\2\2\u012d\u012e\3\2\2\2\u012e\5\3\2\2\2\u012f\u012d"+
		"\3\2\2\2\u0130\u0132\7\u00a4\2\2\u0131\u0133\7\u00a0\2\2\u0132\u0131\3"+
		"\2\2\2\u0132\u0133\3\2\2\2\u0133\u0134\3\2\2\2\u0134\u0137\7\u00a1\2\2"+
		"\u0135\u0136\7\u00f4\2\2\u0136\u0138\7\u00df\2\2\u0137\u0135\3\2\2\2\u0137"+
		"\u0138\3\2\2\2\u0138\u0139\3\2\2\2\u0139\u013e\5\u0096L\2\u013a\u013b"+
		"\7|\2\2\u013b\u013d\5\u0096L\2\u013c\u013a\3\2\2\2\u013d\u0140\3\2\2\2"+
		"\u013e\u013c\3\2\2\2\u013e\u013f\3\2\2\2\u013f\u0142\3\2\2\2\u0140\u013e"+
		"\3\2\2\2\u0141\u0143\t\3\2\2\u0142\u0141\3\2\2\2\u0142\u0143\3\2\2\2\u0143"+
		"\7\3\2\2\2\u0144\u0146\7\u00b1\2\2\u0145\u0147\7\u00a1\2\2\u0146\u0145"+
		"\3\2\2\2\u0146\u0147\3\2\2\2\u0147\u0148\3\2\2\2\u0148\u0149\5\u0096L"+
		"\2\u0149\t\3\2\2\2\u014a\u014e\5\f\7\2\u014b\u014e\5\24\13\2\u014c\u014e"+
		"\5\26\f\2\u014d\u014a\3\2\2\2\u014d\u014b\3\2\2\2\u014d\u014c\3\2\2\2"+
		"\u014e\13\3\2\2\2\u014f\u0150\7x\2\2\u0150\u0151\5\16\b\2\u0151\u0153"+
		"\7y\2\2\u0152\u0154\5D#\2\u0153\u0152\3\2\2\2\u0153\u0154\3\2\2\2\u0154"+
		"\u0156\3\2\2\2\u0155\u0157\5x=\2\u0156\u0155\3\2\2\2\u0156\u0157\3\2\2"+
		"\2\u0157\r\3\2\2\2\u0158\u015d\5\20\t\2\u0159\u015a\7|\2\2\u015a\u015c"+
		"\5\20\t\2\u015b\u0159\3\2\2\2\u015c\u015f\3\2\2\2\u015d\u015b\3\2\2\2"+
		"\u015d\u015e\3\2\2\2\u015e\17\3\2\2\2\u015f\u015d\3\2\2\2\u0160\u0167"+
		"\5H%\2\u0161\u0165\5<\37\2\u0162\u0165\5\62\32\2\u0163\u0165\5\22\n\2"+
		"\u0164\u0161\3\2\2\2\u0164\u0162\3\2\2\2\u0164\u0163\3\2\2\2\u0165\u0167"+
		"\3\2\2\2\u0166\u0160\3\2\2\2\u0166\u0164\3\2\2\2\u0167\21\3\2\2\2\u0168"+
		"\u0169\7\u00b3\2\2\u0169\u016a\5\u0080A\2\u016a\23\3\2\2\2\u016b\u016c"+
		"\7x\2\2\u016c\u016d\5\16\b\2\u016d\u016e\7y\2\2\u016e\u0170\3\2\2\2\u016f"+
		"\u016b\3\2\2\2\u016f\u0170\3\2\2\2\u0170\u0172\3\2\2\2\u0171\u0173\5D"+
		"#\2\u0172\u0171\3\2\2\2\u0172\u0173\3\2\2\2\u0173\u0175\3\2\2\2\u0174"+
		"\u0176\5x=\2\u0175\u0176\3\2\2\2\u0175\u0174\3\2\2\2\u0176\u0178\3\2\2"+
		"\2\u0177\u0179\t\4\2\2\u0178\u0177\3\2\2\2\u0178\u0179\3\2\2\2\u0179\u017b"+
		"\3\2\2\2\u017a\u017c\7\u0099\2\2\u017b\u017a\3\2\2\2\u017b\u017c\3\2\2"+
		"\2\u017c\u017d\3\2\2\2\u017d\u017e\5\u00ecw\2\u017e\25\3\2\2\2\u017f\u0185"+
		"\5\30\r\2\u0180\u0181\7x\2\2\u0181\u0182\5\30\r\2\u0182\u0183\7y\2\2\u0183"+
		"\u0185\3\2\2\2\u0184\u017f\3\2\2\2\u0184\u0180\3\2\2\2\u0185\27\3\2\2"+
		"\2\u0186\u0187\7\u00eb\2\2\u0187\u0188\5\u0096L\2\u0188\31\3\2\2\2\u0189"+
		"\u018e\5\34\17\2\u018a\u018b\7|\2\2\u018b\u018d\5\34\17\2\u018c\u018a"+
		"\3\2\2\2\u018d\u0190\3\2\2\2\u018e\u018c\3\2\2\2\u018e\u018f\3\2\2\2\u018f"+
		"\33\3\2\2\2\u0190\u018e\3\2\2\2\u0191\u0222\5D#\2\u0192\u0194\7\u00a3"+
		"\2\2\u0193\u0195\7\u00a2\2\2\u0194\u0193\3\2\2\2\u0194\u0195\3\2\2\2\u0195"+
		"\u0198\3\2\2\2\u0196\u0199\58\35\2\u0197\u0199\5:\36\2\u0198\u0196\3\2"+
		"\2\2\u0198\u0197\3\2\2\2\u0199\u0222\3\2\2\2\u019a\u019b\7\u00a3\2\2\u019b"+
		"\u0222\5\62\32\2\u019c\u019d\7\u00a3\2\2\u019d\u0222\5<\37\2\u019e\u0222"+
		"\5.\30\2\u019f\u01a1\7\u009e\2\2\u01a0\u01a2\7\u00a2\2\2\u01a1\u01a0\3"+
		"\2\2\2\u01a1\u01a2\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3\u01a8\5\u0098M\2"+
		"\u01a4\u01a5\7\u00bf\2\2\u01a5\u01a9\7\u00d8\2\2\u01a6\u01a7\7\u00a4\2"+
		"\2\u01a7\u01a9\7\u00d8\2\2\u01a8\u01a4\3\2\2\2\u01a8\u01a6\3\2\2\2\u01a9"+
		"\u0222\3\2\2\2\u01aa\u0222\5\36\20\2\u01ab\u01ad\7\u00d8\2\2\u01ac\u01ab"+
		"\3\2\2\2\u01ac\u01ad\3\2\2\2\u01ad\u01ae\3\2\2\2\u01ae\u0222\5\u00dep"+
		"\2\u01af\u01b0\7\r\2\2\u01b0\u01b1\7\u00b9\2\2\u01b1\u0222\5\u00d6l\2"+
		"\u01b2\u01b3\t\5\2\2\u01b3\u0222\7\u00bc\2\2\u01b4\u01b5\t\6\2\2\u01b5"+
		"\u0222\7\u00b0\2\2\u01b6\u0222\5\"\22\2\u01b7\u0222\5$\23\2\u01b8\u0222"+
		"\5&\24\2\u01b9\u01ba\7\u00a4\2\2\u01ba\u01bb\7\u00aa\2\2\u01bb\u01bc\7"+
		"\u00d4\2\2\u01bc\u0222\5b\62\2\u01bd\u0222\7\u00d6\2\2\u01be\u0222\5\60"+
		"\31\2\u01bf\u0222\5,\27\2\u01c0\u01c1\7\u009a\2\2\u01c1\u01c2\7\u0091"+
		"\2\2\u01c2\u01c7\5\u0098M\2\u01c3\u01c4\7|\2\2\u01c4\u01c6\5\u0098M\2"+
		"\u01c5\u01c3\3\2\2\2\u01c6\u01c9\3\2\2\2\u01c7\u01c5\3\2\2\2\u01c7\u01c8"+
		"\3\2\2\2\u01c8\u01cb\3\2\2\2\u01c9\u01c7\3\2\2\2\u01ca\u01c0\3\2\2\2\u01cb"+
		"\u01cc\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cc\u01cd\3\2\2\2\u01cd\u0222\3\2"+
		"\2\2\u01ce\u0222\5(\25\2\u01cf\u0222\5*\26\2\u01d0\u01d1\t\7\2\2\u01d1"+
		"\u0222\7\u00ae\2\2\u01d2\u01d3\7\u00a3\2\2\u01d3\u01d4\7\u008e\2\2\u01d4"+
		"\u0222\5\u0086D\2\u01d5\u01d6\7\u00a4\2\2\u01d6\u01d7\7\u008e\2\2\u01d7"+
		"\u0222\5t;\2\u01d8\u01d9\7\t\2\2\u01d9\u01dc\7\u008e\2\2\u01da\u01dd\5"+
		"t;\2\u01db\u01dd\7\u008a\2\2\u01dc\u01da\3\2\2\2\u01dc\u01db\3\2\2\2\u01dd"+
		"\u01de\3\2\2\2\u01de\u0222\7\u00b0\2\2\u01df\u01e0\7\u00af\2\2\u01e0\u01e3"+
		"\7\u008e\2\2\u01e1\u01e4\5t;\2\u01e2\u01e4\7\u008a\2\2\u01e3\u01e1\3\2"+
		"\2\2\u01e3\u01e2\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5\u0222\7\u00b0\2\2\u01e6"+
		"\u01e7\7\u00b1\2\2\u01e7\u01ea\7\u008e\2\2\u01e8\u01eb\5t;\2\u01e9\u01eb"+
		"\7\u008a\2\2\u01ea\u01e8\3\2\2\2\u01ea\u01e9\3\2\2\2\u01eb\u0222\3\2\2"+
		"\2\u01ec\u01ed\7\u00be\2\2\u01ed\u01ee\7\u008e\2\2\u01ee\u0222\7\u0083"+
		"\2\2\u01ef\u01f0\7\20\2\2\u01f0\u01f1\7\u008e\2\2\u01f1\u01f2\5t;\2\u01f2"+
		"\u01f3\7\u009d\2\2\u01f3\u01f4\5\u0086D\2\u01f4\u0222\3\2\2\2\u01f5\u01f6"+
		"\7\21\2\2\u01f6\u01f7\7\u008e\2\2\u01f7\u01f8\5v<\2\u01f8\u01f9\7\u0094"+
		"\2\2\u01f9\u01fa\7\u00a1\2\2\u01fa\u01fd\5\u0096L\2\u01fb\u01fc\t\7\2"+
		"\2\u01fc\u01fe\7\u00ae\2\2\u01fd\u01fb\3\2\2\2\u01fd\u01fe\3\2\2\2\u01fe"+
		"\u0222\3\2\2\2\u01ff\u0200\7\u00b2\2\2\u0200\u0203\7\u008e\2\2\u0201\u0204"+
		"\5t;\2\u0202\u0204\7\u008a\2\2\u0203\u0201\3\2\2\2\u0203\u0202\3\2\2\2"+
		"\u0204\u0222\3\2\2\2\u0205\u0206\7\u00b3\2\2\u0206\u0209\7\u008e\2\2\u0207"+
		"\u020a\5t;\2\u0208\u020a\7\u008a\2\2\u0209\u0207\3\2\2\2\u0209\u0208\3"+
		"\2\2\2\u020a\u0222\3\2\2\2\u020b\u020c\7\u00b4\2\2\u020c\u020f\7\u008e"+
		"\2\2\u020d\u0210\5t;\2\u020e\u0210\7\u008a\2\2\u020f\u020d\3\2\2\2\u020f"+
		"\u020e\3\2\2\2\u0210\u0222\3\2\2\2\u0211\u0212\7\u00b5\2\2\u0212\u0215"+
		"\7\u008e\2\2\u0213\u0216\5t;\2\u0214\u0216\7\u008a\2\2\u0215\u0213\3\2"+
		"\2\2\u0215\u0214\3\2\2\2\u0216\u0222\3\2\2\2\u0217\u0218\7\u00b6\2\2\u0218"+
		"\u021b\7\u008e\2\2\u0219\u021c\5t;\2\u021a\u021c\7\u008a\2\2\u021b\u0219"+
		"\3\2\2\2\u021b\u021a\3\2\2\2\u021c\u0222\3\2\2\2\u021d\u021e\7\u00b7\2"+
		"\2\u021e\u0222\7\66\2\2\u021f\u0220\7\u00b8\2\2\u0220\u0222\7\66\2\2\u0221"+
		"\u0191\3\2\2\2\u0221\u0192\3\2\2\2\u0221\u019a\3\2\2\2\u0221\u019c\3\2"+
		"\2\2\u0221\u019e\3\2\2\2\u0221\u019f\3\2\2\2\u0221\u01aa\3\2\2\2\u0221"+
		"\u01ac\3\2\2\2\u0221\u01af\3\2\2\2\u0221\u01b2\3\2\2\2\u0221\u01b4\3\2"+
		"\2\2\u0221\u01b6\3\2\2\2\u0221\u01b7\3\2\2\2\u0221\u01b8\3\2\2\2\u0221"+
		"\u01b9\3\2\2\2\u0221\u01bd\3\2\2\2\u0221\u01be\3\2\2\2\u0221\u01bf\3\2"+
		"\2\2\u0221\u01ca\3\2\2\2\u0221\u01ce\3\2\2\2\u0221\u01cf\3\2\2\2\u0221"+
		"\u01d0\3\2\2\2\u0221\u01d2\3\2\2\2\u0221\u01d5\3\2\2\2\u0221\u01d8\3\2"+
		"\2\2\u0221\u01df\3\2\2\2\u0221\u01e6\3\2\2\2\u0221\u01ec\3\2\2\2\u0221"+
		"\u01ef\3\2\2\2\u0221\u01f5\3\2\2\2\u0221\u01ff\3\2\2\2\u0221\u0205\3\2"+
		"\2\2\u0221\u020b\3\2\2\2\u0221\u0211\3\2\2\2\u0221\u0217\3\2\2\2\u0221"+
		"\u021d\3\2\2\2\u0221\u021f\3\2\2\2\u0222\35\3\2\2\2\u0223\u0224\5 \21"+
		"\2\u0224\u0225\5\u0098M\2\u0225\u0226\5\u0098M\2\u0226\u022a\5J&\2\u0227"+
		"\u022b\7\4\2\2\u0228\u0229\7\5\2\2\u0229\u022b\5\u0098M\2\u022a\u0227"+
		"\3\2\2\2\u022a\u0228\3\2\2\2\u022a\u022b\3\2\2\2\u022b\37\3\2\2\2\u022c"+
		"\u022e\7\b\2\2\u022d\u022f\7\u00a2\2\2\u022e\u022d\3\2\2\2\u022e\u022f"+
		"\3\2\2\2\u022f!\3\2\2\2\u0230\u0232\7\u00a4\2\2\u0231\u0233\7\u00a2\2"+
		"\2\u0232\u0231\3\2\2\2\u0232\u0233\3\2\2\2\u0233\u0234\3\2\2\2\u0234\u0235"+
		"\5\u0098M\2\u0235#\3\2\2\2\u0236\u0237\7\u00a4\2\2\u0237\u0238\5\64\33"+
		"\2\u0238\u0239\5j\66\2\u0239%\3\2\2\2\u023a\u023b\7\u00a4\2\2\u023b\u023c"+
		"\7\u00bb\2\2\u023c\u023d\7\u00d4\2\2\u023d\'\3\2\2\2\u023e\u023f\7\u00ad"+
		"\2\2\u023f\u0240\5\64\33\2\u0240\u0241\5j\66\2\u0241\u0242\7\u00b9\2\2"+
		"\u0242\u0243\5j\66\2\u0243)\3\2\2\2\u0244\u0246\7\u00ad\2\2\u0245\u0247"+
		"\t\b\2\2\u0246\u0245\3\2\2\2\u0246\u0247\3\2\2\2\u0247\u0248\3\2\2\2\u0248"+
		"\u0249\5\u0096L\2\u0249+\3\2\2\2\u024a\u024c\7\u00ac\2\2\u024b\u024d\7"+
		"\u00a2\2\2\u024c\u024b\3\2\2\2\u024c\u024d\3\2\2\2\u024d\u024e\3\2\2\2"+
		"\u024e\u0252\5H%\2\u024f\u0253\7\4\2\2\u0250\u0251\7\5\2\2\u0251\u0253"+
		"\5\u0098M\2\u0252\u024f\3\2\2\2\u0252\u0250\3\2\2\2\u0252\u0253\3\2\2"+
		"\2\u0253-\3\2\2\2\u0254\u0256\7\7\2\2\u0255\u0257\7q\2\2\u0256\u0255\3"+
		"\2\2\2\u0256\u0257\3\2\2\2\u0257\u0258\3\2\2\2\u0258\u0259\t\t\2\2\u0259"+
		"/\3\2\2\2\u025a\u025c\7\u00c6\2\2\u025b\u025d\7q\2\2\u025c\u025b\3\2\2"+
		"\2\u025c\u025d\3\2\2\2\u025d\u025e\3\2\2\2\u025e\u025f\t\n\2\2\u025f\61"+
		"\3\2\2\2\u0260\u0262\t\13\2\2\u0261\u0263\5\64\33\2\u0262\u0261\3\2\2"+
		"\2\u0262\u0263\3\2\2\2\u0263\u0266\3\2\2\2\u0264\u0266\5\64\33\2\u0265"+
		"\u0260\3\2\2\2\u0265\u0264\3\2\2\2\u0266\u0267\3\2\2\2\u0267\u0268\5\66"+
		"\34\2\u0268\63\3\2\2\2\u0269\u026a\t\f\2\2\u026a\65\3\2\2\2\u026b\u026d"+
		"\5j\66\2\u026c\u026b\3\2\2\2\u026c\u026d\3\2\2\2\u026d\u026f\3\2\2\2\u026e"+
		"\u0270\5l\67\2\u026f\u026e\3\2\2\2\u026f\u0270\3\2\2\2\u0270\u0271\3\2"+
		"\2\2\u0271\u0273\5d\63\2\u0272\u0274\5n8\2\u0273\u0272\3\2\2\2\u0273\u0274"+
		"\3\2\2\2\u0274\67\3\2\2\2\u0275\u0279\5H%\2\u0276\u027a\7\4\2\2\u0277"+
		"\u0278\7\5\2\2\u0278\u027a\5\u0098M\2\u0279\u0276\3\2\2\2\u0279\u0277"+
		"\3\2\2\2\u0279\u027a\3\2\2\2\u027a9\3\2\2\2\u027b\u027c\7x\2\2\u027c\u0281"+
		"\5H%\2\u027d\u027e\7|\2\2\u027e\u0280\5H%\2\u027f\u027d\3\2\2\2\u0280"+
		"\u0283\3\2\2\2\u0281\u027f\3\2\2\2\u0281\u0282\3\2\2\2\u0282\u0284\3\2"+
		"\2\2\u0283\u0281\3\2\2\2\u0284\u0285\7y\2\2\u0285;\3\2\2\2\u0286\u0288"+
		"\7\u00a7\2\2\u0287\u0289\5`\61\2\u0288\u0287\3\2\2\2\u0288\u0289\3\2\2"+
		"\2\u0289\u028b\3\2\2\2\u028a\u0286\3\2\2\2\u028a\u028b\3\2\2\2\u028b\u028f"+
		"\3\2\2\2\u028c\u0290\5> \2\u028d\u0290\5@!\2\u028e\u0290\5B\"\2\u028f"+
		"\u028c\3\2\2\2\u028f\u028d\3\2\2\2\u028f\u028e\3\2\2\2\u0290=\3\2\2\2"+
		"\u0291\u0292\7\u00bb\2\2\u0292\u0294\7\u00d4\2\2\u0293\u0295\5l\67\2\u0294"+
		"\u0293\3\2\2\2\u0294\u0295\3\2\2\2\u0295\u0296\3\2\2\2\u0296\u0298\5d"+
		"\63\2\u0297\u0299\5n8\2\u0298\u0297\3\2\2\2\u0298\u0299\3\2\2\2\u0299"+
		"?\3\2\2\2\u029a\u029c\7\u00a8\2\2\u029b\u029d\t\f\2\2\u029c\u029b\3\2"+
		"\2\2\u029c\u029d\3\2\2\2\u029d\u029f\3\2\2\2\u029e\u02a0\5j\66\2\u029f"+
		"\u029e\3\2\2\2\u029f\u02a0\3\2\2\2\u02a0\u02a2\3\2\2\2\u02a1\u02a3\5l"+
		"\67\2\u02a2\u02a1\3\2\2\2\u02a2\u02a3\3\2\2\2\u02a3\u02a4\3\2\2\2\u02a4"+
		"\u02a6\5d\63\2\u02a5\u02a7\5n8\2\u02a6\u02a5\3\2\2\2\u02a6\u02a7\3\2\2"+
		"\2\u02a7A\3\2\2\2\u02a8\u02a9\7\u00aa\2\2\u02a9\u02ab\7\u00d4\2\2\u02aa"+
		"\u02ac\5j\66\2\u02ab\u02aa\3\2\2\2\u02ab\u02ac\3\2\2\2\u02ac\u02ad\3\2"+
		"\2\2\u02ad\u02ae\7x\2\2\u02ae\u02b3\5\u0098M\2\u02af\u02b0\7|\2\2\u02b0"+
		"\u02b2\5\u0098M\2\u02b1\u02af\3\2\2\2\u02b2\u02b5\3\2\2\2\u02b3\u02b1"+
		"\3\2\2\2\u02b3\u02b4\3\2\2\2\u02b4\u02b6\3\2\2\2\u02b5\u02b3\3\2\2\2\u02b6"+
		"\u02b7\7y\2\2\u02b7\u02b8\5\\/\2\u02b8C\3\2\2\2\u02b9\u02c0\5F$\2\u02ba"+
		"\u02bc\7|\2\2\u02bb\u02ba\3\2\2\2\u02bb\u02bc\3\2\2\2\u02bc\u02bd\3\2"+
		"\2\2\u02bd\u02bf\5F$\2\u02be\u02bb\3\2\2\2\u02bf\u02c2\3\2\2\2\u02c0\u02be"+
		"\3\2\2\2\u02c0\u02c1\3\2\2\2\u02c1E\3\2\2\2\u02c2\u02c0\3\2\2\2\u02c3"+
		"\u02c5\7\27\2\2\u02c4\u02c6\7q\2\2\u02c5\u02c4\3\2\2\2\u02c5\u02c6\3\2"+
		"\2\2\u02c6\u02c7\3\2\2\2\u02c7\u033c\7\u0083\2\2\u02c8\u02ca\7\30\2\2"+
		"\u02c9\u02cb\7q\2\2\u02ca\u02c9\3\2\2\2\u02ca\u02cb\3\2\2\2\u02cb\u02cc"+
		"\3\2\2\2\u02cc\u033c\7\u0083\2\2\u02cd\u02cf\7\u00d8\2\2\u02ce\u02cd\3"+
		"\2\2\2\u02ce\u02cf\3\2\2\2\u02cf\u02d0\3\2\2\2\u02d0\u033c\5\u00e0q\2"+
		"\u02d1\u02d3\7\31\2\2\u02d2\u02d4\7q\2\2\u02d3\u02d2\3\2\2\2\u02d3\u02d4"+
		"\3\2\2\2\u02d4\u02d5\3\2\2\2\u02d5\u033c\7\u0083\2\2\u02d6\u02d8\7\u00d8"+
		"\2\2\u02d7\u02d6\3\2\2\2\u02d7\u02d8\3\2\2\2\u02d8\u02d9\3\2\2\2\u02d9"+
		"\u033c\5\u00e2r\2\u02da\u02dc\7\26\2\2\u02db\u02dd\7q\2\2\u02dc\u02db"+
		"\3\2\2\2\u02dc\u02dd\3\2\2\2\u02dd\u02de\3\2\2\2\u02de\u033c\7\u0082\2"+
		"\2\u02df\u02e1\7\32\2\2\u02e0\u02e2\7q\2\2\u02e1\u02e0\3\2\2\2\u02e1\u02e2"+
		"\3\2\2\2\u02e2\u02e3\3\2\2\2\u02e3\u033c\t\r\2\2\u02e4\u02e6\7\35\2\2"+
		"\u02e5\u02e7\7q\2\2\u02e6\u02e5\3\2\2\2\u02e6\u02e7\3\2\2\2\u02e7\u02e8"+
		"\3\2\2\2\u02e8\u033c\7\u0082\2\2\u02e9\u02ea\t\16\2\2\u02ea\u02ec\7\37"+
		"\2\2\u02eb\u02ed\7q\2\2\u02ec\u02eb\3\2\2\2\u02ec\u02ed\3\2\2\2\u02ed"+
		"\u02ee\3\2\2\2\u02ee\u033c\7\u0082\2\2\u02ef\u02f1\7 \2\2\u02f0\u02f2"+
		"\7q\2\2\u02f1\u02f0\3\2\2\2\u02f1\u02f2\3\2\2\2\u02f2\u02f3\3\2\2\2\u02f3"+
		"\u033c\7\u0083\2\2\u02f4\u02f6\7!\2\2\u02f5\u02f7\7q\2\2\u02f6\u02f5\3"+
		"\2\2\2\u02f6\u02f7\3\2\2\2\u02f7\u02f8\3\2\2\2\u02f8\u033c\7\u0082\2\2"+
		"\u02f9\u02fb\7\"\2\2\u02fa\u02fc\7q\2\2\u02fb\u02fa\3\2\2\2\u02fb\u02fc"+
		"\3\2\2\2\u02fc\u02fd\3\2\2\2\u02fd\u033c\5r:\2\u02fe\u0300\7#\2\2\u02ff"+
		"\u0301\7q\2\2\u0300\u02ff\3\2\2\2\u0300\u0301\3\2\2\2\u0301\u0302\3\2"+
		"\2\2\u0302\u033c\t\17\2\2\u0303\u0305\7\24\2\2\u0304\u0306\7q\2\2\u0305"+
		"\u0304\3\2\2\2\u0305\u0306\3\2\2\2\u0306\u0307\3\2\2\2\u0307\u033c\7\u0083"+
		"\2\2\u0308\u030a\7&\2\2\u0309\u030b\7q\2\2\u030a\u0309\3\2\2\2\u030a\u030b"+
		"\3\2\2\2\u030b\u030c\3\2\2\2\u030c\u033c\7\u0083\2\2\u030d\u030f\7\'\2"+
		"\2\u030e\u0310\7q\2\2\u030f\u030e\3\2\2\2\u030f\u0310\3\2\2\2\u0310\u0311"+
		"\3\2\2\2\u0311\u033c\7\u0083\2\2\u0312\u0314\7(\2\2\u0313\u0315\7q\2\2"+
		"\u0314\u0313\3\2\2\2\u0314\u0315\3\2\2\2\u0315\u0316\3\2\2\2\u0316\u033c"+
		"\t\20\2\2\u0317\u0319\7)\2\2\u0318\u031a\7q\2\2\u0319\u0318\3\2\2\2\u0319"+
		"\u031a\3\2\2\2\u031a\u031b\3\2\2\2\u031b\u033c\7\u0082\2\2\u031c\u031e"+
		"\7*\2\2\u031d\u031f\7q\2\2\u031e\u031d\3\2\2\2\u031e\u031f\3\2\2\2\u031f"+
		"\u0320\3\2\2\2\u0320\u033c\t\21\2\2\u0321\u0323\7\60\2\2\u0322\u0324\7"+
		"q\2\2\u0323\u0322\3\2\2\2\u0323\u0324\3\2\2\2\u0324\u0325\3\2\2\2\u0325"+
		"\u033c\t\20\2\2\u0326\u0328\7\61\2\2\u0327\u0329\7q\2\2\u0328\u0327\3"+
		"\2\2\2\u0328\u0329\3\2\2\2\u0329\u032a\3\2\2\2\u032a\u033c\t\20\2\2\u032b"+
		"\u032d\7\62\2\2\u032c\u032e\7q\2\2\u032d\u032c\3\2\2\2\u032d\u032e\3\2"+
		"\2\2\u032e\u032f\3\2\2\2\u032f\u033c\7\u0083\2\2\u0330\u0331\7\u00b0\2"+
		"\2\u0331\u0334\5\u009aN\2\u0332\u0333\7\63\2\2\u0333\u0335\t\22\2\2\u0334"+
		"\u0332\3\2\2\2\u0334\u0335\3\2\2\2\u0335\u033c\3\2\2\2\u0336\u0338\7\u00d7"+
		"\2\2\u0337\u0339\7q\2\2\u0338\u0337\3\2\2\2\u0338\u0339\3\2\2\2\u0339"+
		"\u033a\3\2\2\2\u033a\u033c\5\u00a2R\2\u033b\u02c3\3\2\2\2\u033b\u02c8"+
		"\3\2\2\2\u033b\u02ce\3\2\2\2\u033b\u02d1\3\2\2\2\u033b\u02d7\3\2\2\2\u033b"+
		"\u02da\3\2\2\2\u033b\u02df\3\2\2\2\u033b\u02e4\3\2\2\2\u033b\u02e9\3\2"+
		"\2\2\u033b\u02ef\3\2\2\2\u033b\u02f4\3\2\2\2\u033b\u02f9\3\2\2\2\u033b"+
		"\u02fe\3\2\2\2\u033b\u0303\3\2\2\2\u033b\u0308\3\2\2\2\u033b\u030d\3\2"+
		"\2\2\u033b\u0312\3\2\2\2\u033b\u0317\3\2\2\2\u033b\u031c\3\2\2\2\u033b"+
		"\u0321\3\2\2\2\u033b\u0326\3\2\2\2\u033b\u032b\3\2\2\2\u033b\u0330\3\2"+
		"\2\2\u033b\u0336\3\2\2\2\u033cG\3\2\2\2\u033d\u033e\5\u0098M\2\u033e\u033f"+
		"\5J&\2\u033fI\3\2\2\2\u0340\u0343\5L\'\2\u0341\u0344\5X-\2\u0342\u0344"+
		"\5Z.\2\u0343\u0341\3\2\2\2\u0343\u0342\3\2\2\2\u0344K\3\2\2\2\u0345\u0347"+
		"\7\u00f7\2\2\u0346\u0348\5R*\2\u0347\u0346\3\2\2\2\u0347\u0348\3\2\2\2"+
		"\u0348\u03ed\3\2\2\2\u0349\u034b\t\23\2\2\u034a\u034c\5R*\2\u034b\u034a"+
		"\3\2\2\2\u034b\u034c\3\2\2\2\u034c\u0351\3\2\2\2\u034d\u034f\7\u00e4\2"+
		"\2\u034e\u034d\3\2\2\2\u034e\u034f\3\2\2\2\u034f\u0350\3\2\2\2\u0350\u0352"+
		"\7\u00e6\2\2\u0351\u034e\3\2\2\2\u0351\u0352\3\2\2\2\u0352\u0354\3\2\2"+
		"\2\u0353\u0355\7\27\2\2\u0354\u0353\3\2\2\2\u0354\u0355\3\2\2\2\u0355"+
		"\u0356\3\2\2\2\u0356\u03ed\5V,\2\u0357\u0359\t\24\2\2\u0358\u035a\5T+"+
		"\2\u0359\u0358\3\2\2\2\u0359\u035a\3\2\2\2\u035a\u035f\3\2\2\2\u035b\u035d"+
		"\7\u00e4\2\2\u035c\u035b\3\2\2\2\u035c\u035d\3\2\2\2\u035d\u035e\3\2\2"+
		"\2\u035e\u0360\7\u00e6\2\2\u035f\u035c\3\2\2\2\u035f\u0360\3\2\2\2\u0360"+
		"\u0362\3\2\2\2\u0361\u0363\7\27\2\2\u0362\u0361\3\2\2\2\u0362\u0363\3"+
		"\2\2\2\u0363\u0364\3\2\2\2\u0364\u03ed\5V,\2\u0365\u036a\t\25\2\2\u0366"+
		"\u0368\7\u00e4\2\2\u0367\u0366\3\2\2\2\u0367\u0368\3\2\2\2\u0368\u0369"+
		"\3\2\2\2\u0369\u036b\7\u00e6\2\2\u036a\u0367\3\2\2\2\u036a\u036b\3\2\2"+
		"\2\u036b\u036e\3\2\2\2\u036c\u036d\7\u00d8\2\2\u036d\u036f\t\26\2\2\u036e"+
		"\u036c\3\2\2\2\u036e\u036f\3\2\2\2\u036f\u03ed\3\2\2\2\u0370\u0373\5N"+
		"(\2\u0371\u0373\7\u0107\2\2\u0372\u0370\3\2\2\2\u0372\u0371\3\2\2\2\u0373"+
		"\u0378\3\2\2\2\u0374\u0376\7\u00e4\2\2\u0375\u0374\3\2\2\2\u0375\u0376"+
		"\3\2\2\2\u0376\u0377\3\2\2\2\u0377\u0379\7\u00e6\2\2\u0378\u0375\3\2\2"+
		"\2\u0378\u0379\3\2\2\2\u0379\u0381\3\2\2\2\u037a\u037f\7\u00d8\2\2\u037b"+
		"\u0380\5P)\2\u037c\u0380\7\u0083\2\2\u037d\u0380\7\u0082\2\2\u037e\u0380"+
		"\7\u00e6\2\2\u037f\u037b\3\2\2\2\u037f\u037c\3\2\2\2\u037f\u037d\3\2\2"+
		"\2\u037f\u037e\3\2\2\2\u0380\u0382\3\2\2\2\u0381\u037a\3\2\2\2\u0381\u0382"+
		"\3\2\2\2\u0382\u0386\3\2\2\2\u0383\u0384\7\u00cc\2\2\u0384\u0385\7\u00c1"+
		"\2\2\u0385\u0387\5P)\2\u0386\u0383\3\2\2\2\u0386\u0387\3\2\2\2\u0387\u03ed"+
		"\3\2\2\2\u0388\u038a\7\u0108\2\2\u0389\u038b\5R*\2\u038a\u0389\3\2\2\2"+
		"\u038a\u038b\3\2\2\2\u038b\u0390\3\2\2\2\u038c\u038e\7\u00e4\2\2\u038d"+
		"\u038c\3\2\2\2\u038d\u038e\3\2\2\2\u038e\u038f\3\2\2\2\u038f\u0391\7\u00e6"+
		"\2\2\u0390\u038d\3\2\2\2\u0390\u0391\3\2\2\2\u0391\u0394\3\2\2\2\u0392"+
		"\u0393\7\u00d8\2\2\u0393\u0395\t\27\2\2\u0394\u0392\3\2\2\2\u0394\u0395"+
		"\3\2\2\2\u0395\u03ed\3\2\2\2\u0396\u0398\t\30\2\2\u0397\u0399\5R*\2\u0398"+
		"\u0397\3\2\2\2\u0398\u0399\3\2\2\2\u0399\u039e\3\2\2\2\u039a\u039c\7\u00e4"+
		"\2\2\u039b\u039a\3\2\2\2\u039b\u039c\3\2\2\2\u039c\u039d\3\2\2\2\u039d"+
		"\u039f\7\u00e6\2\2\u039e\u039b\3\2\2\2\u039e\u039f\3\2\2\2\u039f\u03a1"+
		"\3\2\2\2\u03a0\u03a2\5\u00d8m\2\u03a1\u03a0\3\2\2\2\u03a1\u03a2\3\2\2"+
		"\2\u03a2\u03a4\3\2\2\2\u03a3\u03a5\5\u00dan\2\u03a4\u03a3\3\2\2\2\u03a4"+
		"\u03a5\3\2\2\2\u03a5\u03a8\3\2\2\2\u03a6\u03a7\7\u00d8\2\2\u03a7\u03a9"+
		"\t\26\2\2\u03a8\u03a6\3\2\2\2\u03a8\u03a9\3\2\2\2\u03a9\u03ed\3\2\2\2"+
		"\u03aa\u03ac\t\31\2\2\u03ab\u03ad\5R*\2\u03ac\u03ab\3\2\2\2\u03ac\u03ad"+
		"\3\2\2\2\u03ad\u03b2\3\2\2\2\u03ae\u03b0\7\u00e4\2\2\u03af\u03ae\3\2\2"+
		"\2\u03af\u03b0\3\2\2\2\u03b0\u03b1\3\2\2\2\u03b1\u03b3\7\u00e6\2\2\u03b2"+
		"\u03af\3\2\2\2\u03b2\u03b3\3\2\2\2\u03b3\u03b8\3\2\2\2\u03b4\u03b5\7\u00d8"+
		"\2\2\u03b5\u03b9\7\u0083\2\2\u03b6\u03b9\7\u0082\2\2\u03b7\u03b9\7\u00e6"+
		"\2\2\u03b8\u03b4\3\2\2\2\u03b8\u03b6\3\2\2\2\u03b8\u03b7\3\2\2\2\u03b8"+
		"\u03b9\3\2\2\2\u03b9\u03ed\3\2\2\2\u03ba\u03bf\t\32\2\2\u03bb\u03bd\7"+
		"\u00e4\2\2\u03bc\u03bb\3\2\2\2\u03bc\u03bd\3\2\2\2\u03bd\u03be\3\2\2\2"+
		"\u03be\u03c0\7\u00e6\2\2\u03bf\u03bc\3\2\2\2\u03bf\u03c0\3\2\2\2\u03c0"+
		"\u03ed\3\2\2\2\u03c1\u03c6\t\33\2\2\u03c2\u03c4\7\u00e4\2\2\u03c3\u03c2"+
		"\3\2\2\2\u03c3\u03c4\3\2\2\2\u03c4\u03c5\3\2\2\2\u03c5\u03c7\7\u00e6\2"+
		"\2\u03c6\u03c3\3\2\2\2\u03c6\u03c7\3\2\2\2\u03c7\u03c9\3\2\2\2\u03c8\u03ca"+
		"\7\u010b\2\2\u03c9\u03c8\3\2\2\2\u03c9\u03ca\3\2\2\2\u03ca\u03cc\3\2\2"+
		"\2\u03cb\u03cd\5\u00d8m\2\u03cc\u03cb\3\2\2\2\u03cc\u03cd\3\2\2\2\u03cd"+
		"\u03cf\3\2\2\2\u03ce\u03d0\5\u00dan\2\u03cf\u03ce\3\2\2\2\u03cf\u03d0"+
		"\3\2\2\2\u03d0\u03ed\3\2\2\2\u03d1\u03d2\t\34\2\2\u03d2\u03d3\7x\2\2\u03d3"+
		"\u03d8\7\u0082\2\2\u03d4\u03d5\7|\2\2\u03d5\u03d7\7\u0082\2\2\u03d6\u03d4"+
		"\3\2\2\2\u03d7\u03da\3\2\2\2\u03d8\u03d6\3\2\2\2\u03d8\u03d9\3\2\2\2\u03d9"+
		"\u03db\3\2\2\2\u03da\u03d8\3\2\2\2\u03db\u03e0\7y\2\2\u03dc\u03de\7\u00e4"+
		"\2\2\u03dd\u03dc\3\2\2\2\u03dd\u03de\3\2\2\2\u03de\u03df\3\2\2\2\u03df"+
		"\u03e1\7\u00e6\2\2\u03e0\u03dd\3\2\2\2\u03e0\u03e1\3\2\2\2\u03e1\u03e4"+
		"\3\2\2\2\u03e2\u03e3\7\u00d8\2\2\u03e3\u03e5\t\26\2\2\u03e4\u03e2\3\2"+
		"\2\2\u03e4\u03e5\3\2\2\2\u03e5\u03e7\3\2\2\2\u03e6\u03e8\5\u00d8m\2\u03e7"+
		"\u03e6\3\2\2\2\u03e7\u03e8\3\2\2\2\u03e8\u03ea\3\2\2\2\u03e9\u03eb\5\u00da"+
		"n\2\u03ea\u03e9\3\2\2\2\u03ea\u03eb\3\2\2\2\u03eb\u03ed\3\2\2\2\u03ec"+
		"\u0345\3\2\2\2\u03ec\u0349\3\2\2\2\u03ec\u0357\3\2\2\2\u03ec\u0365\3\2"+
		"\2\2\u03ec\u0372\3\2\2\2\u03ec\u0388\3\2\2\2\u03ec\u0396\3\2\2\2\u03ec"+
		"\u03aa\3\2\2\2\u03ec\u03ba\3\2\2\2\u03ec\u03c1\3\2\2\2\u03ec\u03d1\3\2"+
		"\2\2\u03edM\3\2\2\2\u03ee\u03f0\7\u0105\2\2\u03ef\u03f1\5R*\2\u03f0\u03ef"+
		"\3\2\2\2\u03f0\u03f1\3\2\2\2\u03f1O\3\2\2\2\u03f2\u03f4\7\u0106\2\2\u03f3"+
		"\u03f5\5R*\2\u03f4\u03f3\3\2\2\2\u03f4\u03f5\3\2\2\2\u03f5Q\3\2\2\2\u03f6"+
		"\u03f7\7x\2\2\u03f7\u03f8\7\u0083\2\2\u03f8\u03f9\7y\2\2\u03f9S\3\2\2"+
		"\2\u03fa\u03fb\7x\2\2\u03fb\u03fc\7\u0083\2\2\u03fc\u03fd\7|\2\2\u03fd"+
		"\u03fe\7\u0083\2\2\u03fe\u03ff\7y\2\2\u03ffU\3\2\2\2\u0400\u0402\7X\2"+
		"\2\u0401\u0400\3\2\2\2\u0401\u0402\3\2\2\2\u0402\u0404\3\2\2\2\u0403\u0405"+
		"\7Y\2\2\u0404\u0403\3\2\2\2\u0404\u0405\3\2\2\2\u0405\u0408\3\2\2\2\u0406"+
		"\u0407\7\u00d8\2\2\u0407\u0409\t\27\2\2\u0408\u0406\3\2\2\2\u0408\u0409"+
		"\3\2\2\2\u0409W\3\2\2\2\u040a\u040c\7\u00a8\2\2\u040b\u040d\7\u00d4\2"+
		"\2\u040c\u040b\3\2\2\2\u040c\u040d\3\2\2\2\u040d\u040f\3\2\2\2\u040e\u040a"+
		"\3\2\2\2\u040e\u040f\3\2\2\2\u040f\u0414\3\2\2\2\u0410\u0412\7\u00bb\2"+
		"\2\u0411\u0410\3\2\2\2\u0411\u0412\3\2\2\2\u0412\u0413\3\2\2\2\u0413\u0415"+
		"\7\u00d4\2\2\u0414\u0411\3\2\2\2\u0414\u0415\3\2\2\2\u0415\u0418\3\2\2"+
		"\2\u0416\u0417\7\26\2\2\u0417\u0419\7\u0082\2\2\u0418\u0416\3\2\2\2\u0418"+
		"\u0419\3\2\2\2\u0419\u041c\3\2\2\2\u041a\u041b\79\2\2\u041b\u041d\t\35"+
		"\2\2\u041c\u041a\3\2\2\2\u041c\u041d\3\2\2\2\u041d\u0420\3\2\2\2\u041e"+
		"\u041f\7\63\2\2\u041f\u0421\t\22\2\2\u0420\u041e\3\2\2\2\u0420\u0421\3"+
		"\2\2\2\u0421\u0423\3\2\2\2\u0422\u0424\5\\/\2\u0423\u0422\3\2\2\2\u0423"+
		"\u0424\3\2\2\2\u0424Y\3\2\2\2\u0425\u0426\7\67\2\2\u0426\u0428\78\2\2"+
		"\u0427\u0425\3\2\2\2\u0427\u0428\3\2\2\2\u0428\u0429\3\2\2\2\u0429\u042a"+
		"\7\u0099\2\2\u042a\u042b\7x\2\2\u042b\u042c\5\u00c8e\2\u042c\u042e\7y"+
		"\2\2\u042d\u042f\t\36\2\2\u042e\u042d\3\2\2\2\u042e\u042f\3\2\2\2\u042f"+
		"\u0433\3\2\2\2\u0430\u0431\7\u00e4\2\2\u0431\u0434\7\u00e6\2\2\u0432\u0434"+
		"\7\u00e6\2\2\u0433\u0430\3\2\2\2\u0433\u0432\3\2\2\2\u0433\u0434\3\2\2"+
		"\2\u0434\u0439\3\2\2\2\u0435\u0437\7\u00a8\2\2\u0436\u0438\7\u00d4\2\2"+
		"\u0437\u0436\3\2\2\2\u0437\u0438\3\2\2\2\u0438\u043a\3\2\2\2\u0439\u0435"+
		"\3\2\2\2\u0439\u043a\3\2\2\2\u043a\u043f\3\2\2\2\u043b\u043d\7\u00bb\2"+
		"\2\u043c\u043b\3\2\2\2\u043c\u043d\3\2\2\2\u043d\u043e\3\2\2\2\u043e\u0440"+
		"\7\u00d4\2\2\u043f\u043c\3\2\2\2\u043f\u0440\3\2\2\2\u0440\u0443\3\2\2"+
		"\2\u0441\u0442\7\26\2\2\u0442\u0444\7\u0082\2\2\u0443\u0441\3\2\2\2\u0443"+
		"\u0444\3\2\2\2\u0444[\3\2\2\2\u0445\u0446\7J\2\2\u0446\u0447\5\u0096L"+
		"\2\u0447\u044e\5d\63\2\u0448\u0449\7K\2\2\u0449\u044f\7L\2\2\u044a\u044b"+
		"\7K\2\2\u044b\u044f\7M\2\2\u044c\u044d\7K\2\2\u044d\u044f\7N\2\2\u044e"+
		"\u0448\3\2\2\2\u044e\u044a\3\2\2\2\u044e\u044c\3\2\2\2\u044e\u044f\3\2"+
		"\2\2\u044f\u0464\3\2\2\2\u0450\u0451\7\u00cc\2\2\u0451\u0452\7\u00c1\2"+
		"\2\u0452\u0454\5^\60\2\u0453\u0450\3\2\2\2\u0453\u0454\3\2\2\2\u0454\u0458"+
		"\3\2\2\2\u0455\u0456\7\u00cc\2\2\u0456\u0457\7\u00d9\2\2\u0457\u0459\5"+
		"^\60\2\u0458\u0455\3\2\2\2\u0458\u0459\3\2\2\2\u0459\u0465\3\2\2\2\u045a"+
		"\u045b\7\u00cc\2\2\u045b\u045c\7\u00d9\2\2\u045c\u045e\5^\60\2\u045d\u045a"+
		"\3\2\2\2\u045d\u045e\3\2\2\2\u045e\u0462\3\2\2\2\u045f\u0460\7\u00cc\2"+
		"\2\u0460\u0461\7\u00c1\2\2\u0461\u0463\5^\60\2\u0462\u045f\3\2\2\2\u0462"+
		"\u0463\3\2\2\2\u0463\u0465\3\2\2\2\u0464\u0453\3\2\2\2\u0464\u045d\3\2"+
		"\2\2\u0465]\3\2\2\2\u0466\u046f\7O\2\2\u0467\u046f\7P\2\2\u0468\u0469"+
		"\7\u00bf\2\2\u0469\u046f\7\u00e6\2\2\u046a\u046b\7$\2\2\u046b\u046f\7"+
		"Q\2\2\u046c\u046d\7\u00bf\2\2\u046d\u046f\7\u00d8\2\2\u046e\u0466\3\2"+
		"\2\2\u046e\u0467\3\2\2\2\u046e\u0468\3\2\2\2\u046e\u046a\3\2\2\2\u046e"+
		"\u046c\3\2\2\2\u046f_\3\2\2\2\u0470\u0471\7\u0118\2\2\u0471a\3\2\2\2\u0472"+
		"\u0473\7\u0118\2\2\u0473c\3\2\2\2\u0474\u0475\7x\2\2\u0475\u047a\5h\65"+
		"\2\u0476\u0477\7|\2\2\u0477\u0479\5h\65\2\u0478\u0476\3\2\2\2\u0479\u047c"+
		"\3\2\2\2\u047a\u0478\3\2\2\2\u047a\u047b\3\2\2\2\u047b\u047d\3\2\2\2\u047c"+
		"\u047a\3\2\2\2\u047d\u047e\7y\2\2\u047ee\3\2\2\2\u047f\u0482\7\u00e6\2"+
		"\2\u0480\u0482\5\u00d2j\2\u0481\u047f\3\2\2\2\u0481\u0480\3\2\2\2\u0482"+
		"g\3\2\2\2\u0483\u0487\5\u0098M\2\u0484\u0485\7x\2\2\u0485\u0486\7\u0083"+
		"\2\2\u0486\u0488\7y\2\2\u0487\u0484\3\2\2\2\u0487\u0488\3\2\2\2\u0488"+
		"\u048a\3\2\2\2\u0489\u048b\t\37\2\2\u048a\u0489\3\2\2\2\u048a\u048b\3"+
		"\2\2\2\u048bi\3\2\2\2\u048c\u048d\7\u0118\2\2\u048dk\3\2\2\2\u048e\u048f"+
		"\7\u00d1\2\2\u048f\u0490\t \2\2\u0490m\3\2\2\2\u0491\u0493\7\24\2\2\u0492"+
		"\u0494\7q\2\2\u0493\u0492\3\2\2\2\u0493\u0494\3\2\2\2\u0494\u0495\3\2"+
		"\2\2\u0495\u049d\5\u008eH\2\u0496\u049d\5l\67\2\u0497\u0498\7\u0094\2"+
		"\2\u0498\u0499\7\25\2\2\u0499\u049d\5p9\2\u049a\u049b\7\26\2\2\u049b\u049d"+
		"\7\u0082\2\2\u049c\u0491\3\2\2\2\u049c\u0496\3\2\2\2\u049c\u0497\3\2\2"+
		"\2\u049c\u049a\3\2\2\2\u049do\3\2\2\2\u049e\u049f\7\u0118\2\2\u049fq\3"+
		"\2\2\2\u04a0\u04a1\t!\2\2\u04a1s\3\2\2\2\u04a2\u04a7\5v<\2\u04a3\u04a4"+
		"\7|\2\2\u04a4\u04a6\5v<\2\u04a5\u04a3\3\2\2\2\u04a6\u04a9\3\2\2\2\u04a7"+
		"\u04a5\3\2\2\2\u04a7\u04a8\3\2\2\2\u04a8u\3\2\2\2\u04a9\u04a7\3\2\2\2"+
		"\u04aa\u04ab\7\u0118\2\2\u04abw\3\2\2\2\u04ac\u04ad\7\u008e\2\2\u04ad"+
		"\u04b0\7\u0091\2\2\u04ae\u04b1\5z>\2\u04af\u04b1\5\u0082B\2\u04b0\u04ae"+
		"\3\2\2\2\u04b0\u04af\3\2\2\2\u04b1\u04b4\3\2\2\2\u04b2\u04b3\7V\2\2\u04b3"+
		"\u04b5\7\u0083\2\2\u04b4\u04b2\3\2\2\2\u04b4\u04b5\3\2\2\2\u04b5\u04bd"+
		"\3\2\2\2\u04b6\u04b7\7=\2\2\u04b7\u04b8\7\u0091\2\2\u04b8\u04bb\5z>\2"+
		"\u04b9\u04ba\7W\2\2\u04ba\u04bc\7\u0083\2\2\u04bb\u04b9\3\2\2\2\u04bb"+
		"\u04bc\3\2\2\2\u04bc\u04be\3\2\2\2\u04bd\u04b6\3\2\2\2\u04bd\u04be\3\2"+
		"\2\2\u04be\u04c0\3\2\2\2\u04bf\u04c1\5\u0086D\2\u04c0\u04bf\3\2\2\2\u04c0"+
		"\u04c1\3\2\2\2\u04c1y\3\2\2\2\u04c2\u04c4\7R\2\2\u04c3\u04c2\3\2\2\2\u04c3"+
		"\u04c4\3\2\2\2\u04c4\u04cb\3\2\2\2\u04c5\u04c8\7\23\2\2\u04c6\u04c9\5"+
		"|?\2\u04c7\u04c9\5\u0080A\2\u04c8\u04c6\3\2\2\2\u04c8\u04c7\3\2\2\2\u04c9"+
		"\u04cc\3\2\2\2\u04ca\u04cc\5~@\2\u04cb\u04c5\3\2\2\2\u04cb\u04ca\3\2\2"+
		"\2\u04cc{\3\2\2\2\u04cd\u04ce\7x\2\2\u04ce\u04cf\7\u0108\2\2\u04cf\u04d0"+
		"\5\u0080A\2\u04d0\u04d1\7y\2\2\u04d1}\3\2\2\2\u04d2\u04d6\7\u00d4\2\2"+
		"\u04d3\u04d4\7\7\2\2\u04d4\u04d5\7q\2\2\u04d5\u04d7\7\u0083\2\2\u04d6"+
		"\u04d3\3\2\2\2\u04d6\u04d7\3\2\2\2\u04d7\u04d8\3\2\2\2\u04d8\u04d9\5\u00a6"+
		"T\2\u04d9\177\3\2\2\2\u04da\u04db\7x\2\2\u04db\u04dc\5\u00c8e\2\u04dc"+
		"\u04dd\7y\2\2\u04dd\u0081\3\2\2\2\u04de\u04df\t\"\2\2\u04df\u04e0\5\u0084"+
		"C\2\u04e0\u0083\3\2\2\2\u04e1\u04e2\7x\2\2\u04e2\u04e3\5\u00c8e\2\u04e3"+
		"\u04e4\7y\2\2\u04e4\u04e8\3\2\2\2\u04e5\u04e6\7S\2\2\u04e6\u04e8\5\u00a6"+
		"T\2\u04e7\u04e1\3\2\2\2\u04e7\u04e5\3\2\2\2\u04e8\u0085\3\2\2\2\u04e9"+
		"\u04ea\7x\2\2\u04ea\u04ef\5\u0088E\2\u04eb\u04ec\7|\2\2\u04ec\u04ee\5"+
		"\u0088E\2\u04ed\u04eb\3\2\2\2\u04ee\u04f1\3\2\2\2\u04ef\u04ed\3\2\2\2"+
		"\u04ef\u04f0\3\2\2\2\u04f0\u04f2\3\2\2\2\u04f1\u04ef\3\2\2\2\u04f2\u04f3"+
		"\7y\2\2\u04f3\u0087\3\2\2\2\u04f4\u04f5\7\u008e\2\2\u04f5\u04fc\5v<\2"+
		"\u04f6\u04fa\7\u00dc\2\2\u04f7\u04fb\5\u008aF\2\u04f8\u04f9\7\u00c7\2"+
		"\2\u04f9\u04fb\5\u0092J\2\u04fa\u04f7\3\2\2\2\u04fa\u04f8\3\2\2\2\u04fb"+
		"\u04fd\3\2\2\2\u04fc\u04f6\3\2\2\2\u04fc\u04fd\3\2\2\2\u04fd\u0506\3\2"+
		"\2\2\u04fe\u0500\7\63\2\2\u04ff\u04fe\3\2\2\2\u04ff\u0500\3\2\2\2\u0500"+
		"\u0501\3\2\2\2\u0501\u0503\7\"\2\2\u0502\u0504\7q\2\2\u0503\u0502\3\2"+
		"\2\2\u0503\u0504\3\2\2\2\u0504\u0505\3\2\2\2\u0505\u0507\5r:\2\u0506\u04ff"+
		"\3\2\2\2\u0506\u0507\3\2\2\2\u0507\u050d\3\2\2\2\u0508\u050a\7\26\2\2"+
		"\u0509\u050b\7q\2\2\u050a\u0509\3\2\2\2\u050a\u050b\3\2\2\2\u050b\u050c"+
		"\3\2\2\2\u050c\u050e\7\u0082\2\2\u050d\u0508\3\2\2\2\u050d\u050e\3\2\2"+
		"\2\u050e\u0515\3\2\2\2\u050f\u0510\7\36\2\2\u0510\u0512\7\37\2\2\u0511"+
		"\u0513\7q\2\2\u0512\u0511\3\2\2\2\u0512\u0513\3\2\2\2\u0513\u0514\3\2"+
		"\2\2\u0514\u0516\7\u0082\2\2\u0515\u050f\3\2\2\2\u0515\u0516\3\2\2\2\u0516"+
		"\u051d\3\2\2\2\u0517\u0518\7\u00d3\2\2\u0518\u051a\7\37\2\2\u0519\u051b"+
		"\7q\2\2\u051a\u0519\3\2\2\2\u051a\u051b\3\2\2\2\u051b\u051c\3\2\2\2\u051c"+
		"\u051e\7\u0082\2\2\u051d\u0517\3\2\2\2\u051d\u051e\3\2\2\2\u051e\u0524"+
		"\3\2\2\2\u051f\u0521\7&\2\2\u0520\u0522\7q\2\2\u0521\u0520\3\2\2\2\u0521"+
		"\u0522\3\2\2\2\u0522\u0523\3\2\2\2\u0523\u0525\7\u0083\2\2\u0524\u051f"+
		"\3\2\2\2\u0524\u0525\3\2\2\2\u0525\u052b\3\2\2\2\u0526\u0528\7\'\2\2\u0527"+
		"\u0529\7q\2\2\u0528\u0527\3\2\2\2\u0528\u0529\3\2\2\2\u0529\u052a\3\2"+
		"\2\2\u052a\u052c\7\u0083\2\2\u052b\u0526\3\2\2\2\u052b\u052c\3\2\2\2\u052c"+
		"\u0532\3\2\2\2\u052d\u052f\7\u00b0\2\2\u052e\u0530\7q\2\2\u052f\u052e"+
		"\3\2\2\2\u052f\u0530\3\2\2\2\u0530\u0531\3\2\2\2\u0531\u0533\5\u009aN"+
		"\2\u0532\u052d\3\2\2\2\u0532\u0533\3\2\2\2\u0533\u053c\3\2\2\2\u0534\u0539"+
		"\5\u008cG\2\u0535\u0536\7|\2\2\u0536\u0538\5\u008cG\2\u0537\u0535\3\2"+
		"\2\2\u0538\u053b\3\2\2\2\u0539\u0537\3\2\2\2\u0539\u053a\3\2\2\2\u053a"+
		"\u053d\3\2\2\2\u053b\u0539\3\2\2\2\u053c\u0534\3\2\2\2\u053c\u053d\3\2"+
		"\2\2\u053d\u0089\3\2\2\2\u053e\u053f\7?\2\2\u053f\u0548\7@\2\2\u0540\u0543"+
		"\7x\2\2\u0541\u0544\5\u00c8e\2\u0542\u0544\5\u0090I\2\u0543\u0541\3\2"+
		"\2\2\u0543\u0542\3\2\2\2\u0544\u0545\3\2\2\2\u0545\u0546\7y\2\2\u0546"+
		"\u0549\3\2\2\2\u0547\u0549\7>\2\2\u0548\u0540\3\2\2\2\u0548\u0547\3\2"+
		"\2\2\u0549\u008b\3\2\2\2\u054a\u054b\7=\2\2\u054b\u0554\5v<\2\u054c\u054e"+
		"\7\63\2\2\u054d\u054c\3\2\2\2\u054d\u054e\3\2\2\2\u054e\u054f\3\2\2\2"+
		"\u054f\u0551\7\"\2\2\u0550\u0552\7q\2\2\u0551\u0550\3\2\2\2\u0551\u0552"+
		"\3\2\2\2\u0552\u0553\3\2\2\2\u0553\u0555\5r:\2\u0554\u054d\3\2\2\2\u0554"+
		"\u0555\3\2\2\2\u0555\u055b\3\2\2\2\u0556\u0558\7\26\2\2\u0557\u0559\7"+
		"q\2\2\u0558\u0557\3\2\2\2\u0558\u0559\3\2\2\2\u0559\u055a\3\2\2\2\u055a"+
		"\u055c\7\u0082\2\2\u055b\u0556\3\2\2\2\u055b\u055c\3\2\2\2\u055c\u0563"+
		"\3\2\2\2\u055d\u055e\7\36\2\2\u055e\u0560\7\37\2\2\u055f\u0561\7q\2\2"+
		"\u0560\u055f\3\2\2\2\u0560\u0561\3\2\2\2\u0561\u0562\3\2\2\2\u0562\u0564"+
		"\7\u0082\2\2\u0563\u055d\3\2\2\2\u0563\u0564\3\2\2\2\u0564\u056b\3\2\2"+
		"\2\u0565\u0566\7\u00d3\2\2\u0566\u0568\7\37\2\2\u0567\u0569\7q\2\2\u0568"+
		"\u0567\3\2\2\2\u0568\u0569\3\2\2\2\u0569\u056a\3\2\2\2\u056a\u056c\7\u0082"+
		"\2\2\u056b\u0565\3\2\2\2\u056b\u056c\3\2\2\2\u056c\u0572\3\2\2\2\u056d"+
		"\u056f\7&\2\2\u056e\u0570\7q\2\2\u056f\u056e\3\2\2\2\u056f\u0570\3\2\2"+
		"\2\u0570\u0571\3\2\2\2\u0571\u0573\7\u0083\2\2\u0572\u056d\3\2\2\2\u0572"+
		"\u0573\3\2\2\2\u0573\u0579\3\2\2\2\u0574\u0576\7\'\2\2\u0575\u0577\7q"+
		"\2\2\u0576\u0575\3\2\2\2\u0576\u0577\3\2\2\2\u0577\u0578\3\2\2\2\u0578"+
		"\u057a\7\u0083\2\2\u0579\u0574\3\2\2\2\u0579\u057a\3\2\2\2\u057a\u0580"+
		"\3\2\2\2\u057b\u057d\7\u00b0\2\2\u057c\u057e\7q\2\2\u057d\u057c\3\2\2"+
		"\2\u057d\u057e\3\2\2\2\u057e\u057f\3\2\2\2\u057f\u0581\5\u009aN\2\u0580"+
		"\u057b\3\2\2\2\u0580\u0581\3\2\2\2\u0581\u008d\3\2\2\2\u0582\u0585\7\u00d8"+
		"\2\2\u0583\u0585\5\u00c8e\2\u0584\u0582\3\2\2\2\u0584\u0583\3\2\2\2\u0585"+
		"\u008f\3\2\2\2\u0586\u058b\5\u008eH\2\u0587\u0588\7|\2\2\u0588\u058a\5"+
		"\u008eH\2\u0589\u0587\3\2\2\2\u058a\u058d\3\2\2\2\u058b\u0589\3\2\2\2"+
		"\u058b\u058c\3\2\2\2\u058c\u0091\3\2\2\2\u058d\u058b\3\2\2\2\u058e\u058f"+
		"\7x\2\2\u058f\u0590\5\u0090I\2\u0590\u0591\7y\2\2\u0591\u0093\3\2\2\2"+
		"\u0592\u0593\7\u0118\2\2\u0593\u0095\3\2\2\2\u0594\u0595\7\u0118\2\2\u0595"+
		"\u0097\3\2\2\2\u0596\u0597\7\u0118\2\2\u0597\u0099\3\2\2\2\u0598\u0599"+
		"\7\u0118\2\2\u0599\u009b\3\2\2\2\u059a\u059b\7\u0118\2\2\u059b\u009d\3"+
		"\2\2\2\u059c\u059d\7\u0118\2\2\u059d\u009f\3\2\2\2\u059e\u059f\7\u0118"+
		"\2\2\u059f\u00a1\3\2\2\2\u05a0\u05a1\7x\2\2\u05a1\u05a6\7\u0118\2\2\u05a2"+
		"\u05a3\7|\2\2\u05a3\u05a5\7\u0118\2\2\u05a4\u05a2\3\2\2\2\u05a5\u05a8"+
		"\3\2\2\2\u05a6\u05a4\3\2\2\2\u05a6\u05a7\3\2\2\2\u05a7\u05a9\3\2\2\2\u05a8"+
		"\u05a6\3\2\2\2\u05a9\u05aa\7y\2\2\u05aa\u00a3\3\2\2\2\u05ab\u05b0\7\u0083"+
		"\2\2\u05ac\u05ad\7|\2\2\u05ad\u05af\7\u0083\2\2\u05ae\u05ac\3\2\2\2\u05af"+
		"\u05b2\3\2\2\2\u05b0\u05ae\3\2\2\2\u05b0\u05b1\3\2\2\2\u05b1\u05b7\3\2"+
		"\2\2\u05b2\u05b0\3\2\2\2\u05b3\u05b4\7\u0083\2\2\u05b4\u05b5\7\u009c\2"+
		"\2\u05b5\u05b7\7\u0083\2\2\u05b6\u05ab\3\2\2\2\u05b6\u05b3\3\2\2\2\u05b7"+
		"\u00a5\3\2\2\2\u05b8\u05b9\7x\2\2\u05b9\u05be\5\u0098M\2\u05ba\u05bb\7"+
		"|\2\2\u05bb\u05bd\5\u0098M\2\u05bc\u05ba\3\2\2\2\u05bd\u05c0\3\2\2\2\u05be"+
		"\u05bc\3\2\2\2\u05be\u05bf\3\2\2\2\u05bf\u05c1\3\2\2\2\u05c0\u05be\3\2"+
		"\2\2\u05c1\u05c2\7y\2\2\u05c2\u00a7\3\2\2\2\u05c3\u05c5\t#\2\2\u05c4\u05c3"+
		"\3\2\2\2\u05c4\u05c5\3\2\2\2\u05c5\u05c7\3\2\2\2\u05c6\u05c8\7B\2\2\u05c7"+
		"\u05c6\3\2\2\2\u05c7\u05c8\3\2\2\2\u05c8\u05ca\3\2\2\2\u05c9\u05cb\7C"+
		"\2\2\u05ca\u05c9\3\2\2\2\u05ca\u05cb\3\2\2\2\u05cb\u05cd\3\2\2\2\u05cc"+
		"\u05ce\7D\2\2\u05cd\u05cc\3\2\2\2\u05cd\u05ce\3\2\2\2\u05ce\u05d0\3\2"+
		"\2\2\u05cf\u05d1\7E\2\2\u05d0\u05cf\3\2\2\2\u05d0\u05d1\3\2\2\2\u05d1"+
		"\u05d3\3\2\2\2\u05d2\u05d4\7F\2\2\u05d3\u05d2\3\2\2\2\u05d3\u05d4\3\2"+
		"\2\2\u05d4\u05d6\3\2\2\2\u05d5\u05d7\t$\2\2\u05d6\u05d5\3\2\2\2\u05d6"+
		"\u05d7\3\2\2\2\u05d7\u05d9\3\2\2\2\u05d8\u05da\7I\2\2\u05d9\u05d8\3\2"+
		"\2\2\u05d9\u05da\3\2\2\2\u05da\u00a9\3\2\2\2\u05db\u05de\5\u00b0Y\2\u05dc"+
		"\u05de\5\u00acW\2\u05dd\u05db\3\2\2\2\u05dd\u05dc\3\2\2\2\u05de\u00ab"+
		"\3\2\2\2\u05df\u05e0\7\u00f1\2\2\u05e0\u05e2\5\u00d2j\2\u05e1\u05e3\5"+
		"\u00aeX\2\u05e2\u05e1\3\2\2\2\u05e3\u05e4\3\2\2\2\u05e4\u05e2\3\2\2\2"+
		"\u05e4\u05e5\3\2\2\2\u05e5\u05e7\3\2\2\2\u05e6\u05e8\5\u00b4[\2\u05e7"+
		"\u05e6\3\2\2\2\u05e7\u05e8\3\2\2\2\u05e8\u05e9\3\2\2\2\u05e9\u05ea\7\u00f6"+
		"\2\2\u05ea\u00ad\3\2\2\2\u05eb\u05ec\7\u00f2\2\2\u05ec\u05ed\5\u00d2j"+
		"\2\u05ed\u05ee\7\u00f3\2\2\u05ee\u05ef\5\u00b6\\\2\u05ef\u00af\3\2\2\2"+
		"\u05f0\u05f2\7\u00f1\2\2\u05f1\u05f3\5\u00b2Z\2\u05f2\u05f1\3\2\2\2\u05f3"+
		"\u05f4\3\2\2\2\u05f4\u05f2\3\2\2\2\u05f4\u05f5\3\2\2\2\u05f5\u05f7\3\2"+
		"\2\2\u05f6\u05f8\5\u00b4[\2\u05f7\u05f6\3\2\2\2\u05f7\u05f8\3\2\2\2\u05f8"+
		"\u05f9\3\2\2\2\u05f9\u05fa\7\u00f6\2\2\u05fa\u00b1\3\2\2\2\u05fb\u05fc"+
		"\7\u00f2\2\2\u05fc\u05fd\5\u00caf\2\u05fd\u05fe\7\u00f3\2\2\u05fe\u05ff"+
		"\5\u00b6\\\2\u05ff\u00b3\3\2\2\2\u0600\u0601\7\u00f5\2\2\u0601\u0602\5"+
		"\u00b6\\\2\u0602\u00b5\3\2\2\2\u0603\u0604\5\u00c8e\2\u0604\u00b7\3\2"+
		"\2\2\u0605\u0606\7x\2\2\u0606\u0609\7y\2\2\u0607\u0609\5\u00a2R\2\u0608"+
		"\u0605\3\2\2\2\u0608\u0607\3\2\2\2\u0609\u00b9\3\2\2\2\u060a\u060f\5\u00bc"+
		"_\2\u060b\u060c\7|\2\2\u060c\u060e\5\u00bc_\2\u060d\u060b\3\2\2\2\u060e"+
		"\u0611\3\2\2\2\u060f\u060d\3\2\2\2\u060f\u0610\3\2\2\2\u0610\u00bb\3\2"+
		"\2\2\u0611\u060f\3\2\2\2\u0612\u0613\5\u00be`\2\u0613\u0614\5\u00c0a\2"+
		"\u0614\u0616\3\2\2\2\u0615\u0612\3\2\2\2\u0616\u0617\3\2\2\2\u0617\u0615"+
		"\3\2\2\2\u0617\u0618\3\2\2\2\u0618\u0621\3\2\2\2\u0619\u061b\5\u00be`"+
		"\2\u061a\u061c\5\u00c0a\2\u061b\u061a\3\2\2\2\u061c\u061d\3\2\2\2\u061d"+
		"\u061b\3\2\2\2\u061d\u061e\3\2\2\2\u061e\u0621\3\2\2\2\u061f\u0621\5\u00be"+
		"`\2\u0620\u0615\3\2\2\2\u0620\u0619\3\2\2\2\u0620\u061f\3\2\2\2\u0621"+
		"\u00bd\3\2\2\2\u0622\u0625\5\u0096L\2\u0623\u0624\7\u008e\2\2\u0624\u0626"+
		"\5\u00a2R\2\u0625\u0623\3\2\2\2\u0625\u0626\3\2\2\2\u0626\u062b\3\2\2"+
		"\2\u0627\u0629\7\u0099\2\2\u0628\u0627\3\2\2\2\u0628\u0629\3\2\2\2\u0629"+
		"\u062a\3\2\2\2\u062a\u062c\5\u009eP\2\u062b\u0628\3\2\2\2\u062b\u062c"+
		"\3\2\2\2\u062c\u062e\3\2\2\2\u062d\u062f\5\u00c4c\2\u062e\u062d\3\2\2"+
		"\2\u062e\u062f\3\2\2\2\u062f\u063b\3\2\2\2\u0630\u0632\5\u0106\u0084\2"+
		"\u0631\u0633\7\u0099\2\2\u0632\u0631\3\2\2\2\u0632\u0633\3\2\2\2\u0633"+
		"\u0634\3\2\2\2\u0634\u0635\5\u009eP\2\u0635\u063b\3\2\2\2\u0636\u0637"+
		"\7x\2\2\u0637\u0638\5\u00ba^\2\u0638\u0639\7y\2\2\u0639\u063b\3\2\2\2"+
		"\u063a\u0622\3\2\2\2\u063a\u0630\3\2\2\2\u063a\u0636\3\2\2\2\u063b\u00bf"+
		"\3\2\2\2\u063c\u063e\t%\2\2\u063d\u063c\3\2\2\2\u063d\u063e\3\2\2\2\u063e"+
		"\u063f\3\2\2\2\u063f\u0640\7\u00cb\2\2\u0640\u0642\5\u00be`\2\u0641\u0643"+
		"\5\u00c2b\2\u0642\u0641\3\2\2\2\u0642\u0643\3\2\2\2\u0643\u065b\3\2\2"+
		"\2\u0644\u0645\7C\2\2\u0645\u065b\5\u00be`\2\u0646\u0647\7C\2\2\u0647"+
		"\u0648\5\u00be`\2\u0648\u0649\5\u00c2b\2\u0649\u065b\3\2\2\2\u064a\u064c"+
		"\t&\2\2\u064b\u064d\7\u00cf\2\2\u064c\u064b\3\2\2\2\u064c\u064d\3\2\2"+
		"\2\u064d\u064e\3\2\2\2\u064e\u064f\7\u00cb\2\2\u064f\u0650\5\u00be`\2"+
		"\u0650\u0651\5\u00c2b\2\u0651\u065b\3\2\2\2\u0652\u0656\7\u00d0\2\2\u0653"+
		"\u0657\7\u00c9\2\2\u0654\u0655\t&\2\2\u0655\u0657\7\u00cf\2\2\u0656\u0653"+
		"\3\2\2\2\u0656\u0654\3\2\2\2\u0656\u0657\3\2\2\2\u0657\u0658\3\2\2\2\u0658"+
		"\u0659\7\u00cb\2\2\u0659\u065b\5\u00be`\2\u065a\u063d\3\2\2\2\u065a\u0644"+
		"\3\2\2\2\u065a\u0646\3\2\2\2\u065a\u064a\3\2\2\2\u065a\u0652\3\2\2\2\u065b"+
		"\u00c1\3\2\2\2\u065c\u065d\7\u00cc\2\2\u065d\u0661\5\u00c8e\2\u065e\u065f"+
		"\7\u00d1\2\2\u065f\u0661\5\u00a2R\2\u0660\u065c\3\2\2\2\u0660\u065e\3"+
		"\2\2\2\u0661\u00c3\3\2\2\2\u0662\u0667\5\u00c6d\2\u0663\u0664\7|\2\2\u0664"+
		"\u0666\5\u00c6d\2\u0665\u0663\3\2\2\2\u0666\u0669\3\2\2\2\u0667\u0665"+
		"\3\2\2\2\u0667\u0668\3\2\2\2\u0668\u00c5\3\2\2\2\u0669\u0667\3\2\2\2\u066a"+
		"\u066b\7\u00d2\2\2\u066b\u0676\t\f\2\2\u066c\u0672\7\u00c0\2\2\u066d\u0673"+
		"\7\u00cb\2\2\u066e\u066f\7\u009a\2\2\u066f\u0673\7\u0091\2\2\u0670\u0671"+
		"\7\u0090\2\2\u0671\u0673\7\u0091\2\2\u0672\u066d\3\2\2\2\u0672\u066e\3"+
		"\2\2\2\u0672\u0670\3\2\2\2\u0673\u0675\3\2\2\2\u0674\u066c\3\2\2\2\u0675"+
		"\u0678\3\2\2\2\u0676\u0674\3\2\2\2\u0676\u0677\3\2\2\2\u0677\u0679\3\2"+
		"\2\2\u0678\u0676\3\2\2\2\u0679\u068b\5\u00a2R\2\u067a\u067b\7\u00d5\2"+
		"\2\u067b\u0686\t\f\2\2\u067c\u0682\7\u00c0\2\2\u067d\u0683\7\u00cb\2\2"+
		"\u067e\u067f\7\u009a\2\2\u067f\u0683\7\u0091\2\2\u0680\u0681\7\u0090\2"+
		"\2\u0681\u0683\7\u0091\2\2\u0682\u067d\3\2\2\2\u0682\u067e\3\2\2\2\u0682"+
		"\u0680\3\2\2\2\u0683\u0685\3\2\2\2\u0684\u067c\3\2\2\2\u0685\u0688\3\2"+
		"\2\2\u0686\u0684\3\2\2\2\u0686\u0687\3\2\2\2\u0687\u0689\3\2\2\2\u0688"+
		"\u0686\3\2\2\2\u0689\u068b\5\u00a2R\2\u068a\u066a\3\2\2\2\u068a\u067a"+
		"\3\2\2\2\u068b\u00c7\3\2\2\2\u068c\u068d\be\1\2\u068d\u068e\7x\2\2\u068e"+
		"\u068f\5\u00c8e\2\u068f\u0690\7y\2\2\u0690\u0697\3\2\2\2\u0691\u0692\7"+
		"\u00e4\2\2\u0692\u0697\5\u00c8e\5\u0693\u0694\7b\2\2\u0694\u0697\5\u00c8"+
		"e\4\u0695\u0697\5\u00caf\2\u0696\u068c\3\2\2\2\u0696\u0691\3\2\2\2\u0696"+
		"\u0693\3\2\2\2\u0696\u0695\3\2\2\2\u0697\u06a9\3\2\2\2\u0698\u0699\f\13"+
		"\2\2\u0699\u069a\7\u00e2\2\2\u069a\u06a8\5\u00c8e\f\u069b\u069c\f\n\2"+
		"\2\u069c\u069d\7a\2\2\u069d\u06a8\5\u00c8e\13\u069e\u069f\f\t\2\2\u069f"+
		"\u06a0\7\u00e3\2\2\u06a0\u06a8\5\u00c8e\n\u06a1\u06a2\f\b\2\2\u06a2\u06a3"+
		"\7\u00e1\2\2\u06a3\u06a8\5\u00c8e\t\u06a4\u06a5\f\7\2\2\u06a5\u06a6\7"+
		"`\2\2\u06a6\u06a8\5\u00c8e\b\u06a7\u0698\3\2\2\2\u06a7\u069b\3\2\2\2\u06a7"+
		"\u069e\3\2\2\2\u06a7\u06a1\3\2\2\2\u06a7\u06a4\3\2\2\2\u06a8\u06ab\3\2"+
		"\2\2\u06a9\u06a7\3\2\2\2\u06a9\u06aa\3\2\2\2\u06aa\u00c9\3\2\2\2\u06ab"+
		"\u06a9\3\2\2\2\u06ac\u06ad\bf\1\2\u06ad\u06ae\5\u00ceh\2\u06ae\u06c3\3"+
		"\2\2\2\u06af\u06b0\f\7\2\2\u06b0\u06b2\7\u00e0\2\2\u06b1\u06b3\7\u00e4"+
		"\2\2\u06b2\u06b1\3\2\2\2\u06b2\u06b3\3\2\2\2\u06b3\u06b4\3\2\2\2\u06b4"+
		"\u06c2\t\'\2\2\u06b5\u06b6\f\6\2\2\u06b6\u06b7\7o\2\2\u06b7\u06c2\5\u00ce"+
		"h\2\u06b8\u06b9\f\5\2\2\u06b9\u06ba\5\u00ccg\2\u06ba\u06bb\5\u00ceh\2"+
		"\u06bb\u06c2\3\2\2\2\u06bc\u06bd\f\4\2\2\u06bd\u06be\5\u00ccg\2\u06be"+
		"\u06bf\t(\2\2\u06bf\u06c0\5\u0106\u0084\2\u06c0\u06c2\3\2\2\2\u06c1\u06af"+
		"\3\2\2\2\u06c1\u06b5\3\2\2\2\u06c1\u06b8\3\2\2\2\u06c1\u06bc\3\2\2\2\u06c2"+
		"\u06c5\3\2\2\2\u06c3\u06c1\3\2\2\2\u06c3\u06c4\3\2\2\2\u06c4\u00cb\3\2"+
		"\2\2\u06c5\u06c3\3\2\2\2\u06c6\u06c7\t)\2\2\u06c7\u00cd\3\2\2\2\u06c8"+
		"\u06ca\5\u00d0i\2\u06c9\u06cb\7\u00e4\2\2\u06ca\u06c9\3\2\2\2\u06ca\u06cb"+
		"\3\2\2\2\u06cb\u06cc\3\2\2\2\u06cc\u06cd\7\u00c7\2\2\u06cd\u06ce\5\u0106"+
		"\u0084\2\u06ce\u0703\3\2\2\2\u06cf\u06d1\5\u00d0i\2\u06d0\u06d2\7\u00e4"+
		"\2\2\u06d1\u06d0\3\2\2\2\u06d1\u06d2\3\2\2\2\u06d2\u06d3\3\2\2\2\u06d3"+
		"\u06d4\7\u00c7\2\2\u06d4\u06d5\7x\2\2\u06d5\u06da\5\u00d2j\2\u06d6\u06d7"+
		"\7|\2\2\u06d7\u06d9\5\u00d2j\2\u06d8\u06d6\3\2\2\2\u06d9\u06dc\3\2\2\2"+
		"\u06da\u06d8\3\2\2\2\u06da\u06db\3\2\2\2\u06db\u06dd\3\2\2\2\u06dc\u06da"+
		"\3\2\2\2\u06dd\u06de\7y\2\2\u06de\u0703\3\2\2\2\u06df\u06e1\5\u00d0i\2"+
		"\u06e0\u06e2\7\u00e4\2\2\u06e1\u06e0\3\2\2\2\u06e1\u06e2\3\2\2\2\u06e2"+
		"\u06e3\3\2\2\2\u06e3\u06e4\7\u00e5\2\2\u06e4\u06e5\5\u00d2j\2\u06e5\u06e6"+
		"\7\u00e1\2\2\u06e6\u06e7\5\u00ceh\2\u06e7\u0703\3\2\2\2\u06e8\u06e9\5"+
		"\u00d0i\2\u06e9\u06ea\7\u00ea\2\2\u06ea\u06eb\7\u00eb\2\2\u06eb\u06ec"+
		"\5\u00d2j\2\u06ec\u0703\3\2\2\2\u06ed\u06ef\5\u00d0i\2\u06ee\u06f0\7\u00e4"+
		"\2\2\u06ef\u06ee\3\2\2\2\u06ef\u06f0\3\2\2\2\u06f0\u06f1\3\2\2\2\u06f1"+
		"\u06f2\7\u00eb\2\2\u06f2\u06f7\5\u00d2j\2\u06f3\u06f4\7\u00ef\2\2\u06f4"+
		"\u06f6\5\u00d2j\2\u06f5\u06f3\3\2\2\2\u06f6\u06f9\3\2\2\2\u06f7\u06f5"+
		"\3\2\2\2\u06f7\u06f8\3\2\2\2\u06f8\u0703\3\2\2\2\u06f9\u06f7\3\2\2\2\u06fa"+
		"\u06fc\5\u00d0i\2\u06fb\u06fd\7\u00e4\2\2\u06fc\u06fb\3\2\2\2\u06fc\u06fd"+
		"\3\2\2\2\u06fd\u06fe\3\2\2\2\u06fe\u06ff\7\u00f0\2\2\u06ff\u0700\5\u00d2"+
		"j\2\u0700\u0703\3\2\2\2\u0701\u0703\5\u00d0i\2\u0702\u06c8\3\2\2\2\u0702"+
		"\u06cf\3\2\2\2\u0702\u06df\3\2\2\2\u0702\u06e8\3\2\2\2\u0702\u06ed\3\2"+
		"\2\2\u0702\u06fa\3\2\2\2\u0702\u0701\3\2\2\2\u0703\u00cf\3\2\2\2\u0704"+
		"\u0705\bi\1\2\u0705\u0706\5\u00d2j\2\u0706\u072d\3\2\2\2\u0707\u0708\f"+
		"\17\2\2\u0708\u0709\7d\2\2\u0709\u072c\5\u00d0i\20\u070a\u070b\f\16\2"+
		"\2\u070b\u070c\7e\2\2\u070c\u072c\5\u00d0i\17\u070d\u070e\f\r\2\2\u070e"+
		"\u070f\7f\2\2\u070f\u072c\5\u00d0i\16\u0710\u0711\f\f\2\2\u0711\u0712"+
		"\7g\2\2\u0712\u072c\5\u00d0i\r\u0713\u0714\f\13\2\2\u0714\u0715\7j\2\2"+
		"\u0715\u072c\5\u00d0i\f\u0716\u0717\f\n\2\2\u0717\u0718\7k\2\2\u0718\u072c"+
		"\5\u00d0i\13\u0719\u071a\f\t\2\2\u071a\u071b\7l\2\2\u071b\u072c\5\u00d0"+
		"i\n\u071c\u071d\f\b\2\2\u071d\u071e\7m\2\2\u071e\u072c\5\u00d0i\t\u071f"+
		"\u0720\f\7\2\2\u0720\u0721\7\u00ec\2\2\u0721\u072c\5\u00d0i\b\u0722\u0723"+
		"\f\6\2\2\u0723\u0724\7\u00ed\2\2\u0724\u072c\5\u00d0i\7\u0725\u0726\f"+
		"\5\2\2\u0726\u0727\7i\2\2\u0727\u072c\5\u00d0i\6\u0728\u0729\f\4\2\2\u0729"+
		"\u072a\7h\2\2\u072a\u072c\5\u00d0i\5\u072b\u0707\3\2\2\2\u072b\u070a\3"+
		"\2\2\2\u072b\u070d\3\2\2\2\u072b\u0710\3\2\2\2\u072b\u0713\3\2\2\2\u072b"+
		"\u0716\3\2\2\2\u072b\u0719\3\2\2\2\u072b\u071c\3\2\2\2\u072b\u071f\3\2"+
		"\2\2\u072b\u0722\3\2\2\2\u072b\u0725\3\2\2\2\u072b\u0728\3\2\2\2\u072c"+
		"\u072f\3\2\2\2\u072d\u072b\3\2\2\2\u072d\u072e\3\2\2\2\u072e\u00d1\3\2"+
		"\2\2\u072f\u072d\3\2\2\2\u0730\u0752\bj\1\2\u0731\u0752\5\u0102\u0082"+
		"\2\u0732\u0752\5\u00d4k\2\u0733\u0752\7\u0118\2\2\u0734\u0735\7j\2\2\u0735"+
		"\u0752\5\u00d2j\13\u0736\u0737\7k\2\2\u0737\u0752\5\u00d2j\n\u0738\u0739"+
		"\7c\2\2\u0739\u0752\5\u00d2j\t\u073a\u073b\7b\2\2\u073b\u0752\5\u00d2"+
		"j\b\u073c\u073d\7\u010b\2\2\u073d\u0752\5\u00d2j\7\u073e\u073f\7x\2\2"+
		"\u073f\u0740\5\u00c8e\2\u0740\u0741\7y\2\2\u0741\u0752\3\2\2\2\u0742\u0743"+
		"\7\u00ee\2\2\u0743\u0744\7x\2\2\u0744\u0749\5\u00d2j\2\u0745\u0746\7|"+
		"\2\2\u0746\u0748\5\u00d2j\2\u0747\u0745\3\2\2\2\u0748\u074b\3\2\2\2\u0749"+
		"\u0747\3\2\2\2\u0749\u074a\3\2\2\2\u074a\u074c\3\2\2\2\u074b\u0749\3\2"+
		"\2\2\u074c\u074d\7y\2\2\u074d\u0752\3\2\2\2\u074e\u0752\5\u0106\u0084"+
		"\2\u074f\u0750\7\u00df\2\2\u0750\u0752\5\u0106\u0084\2\u0751\u0730\3\2"+
		"\2\2\u0751\u0731\3\2\2\2\u0751\u0732\3\2\2\2\u0751\u0733\3\2\2\2\u0751"+
		"\u0734\3\2\2\2\u0751\u0736\3\2\2\2\u0751\u0738\3\2\2\2\u0751\u073a\3\2"+
		"\2\2\u0751\u073c\3\2\2\2\u0751\u073e\3\2\2\2\u0751\u0742\3\2\2\2\u0751"+
		"\u074e\3\2\2\2\u0751\u074f\3\2\2\2\u0752\u075a\3\2\2\2\u0753\u0754\f\f"+
		"\2\2\u0754\u0755\7`\2\2\u0755\u0759\5\u00d2j\r\u0756\u0757\f\r\2\2\u0757"+
		"\u0759\5\u00dan\2\u0758\u0753\3\2\2\2\u0758\u0756\3\2\2\2\u0759\u075c"+
		"\3\2\2\2\u075a\u0758\3\2\2\2\u075a\u075b\3\2\2\2\u075b\u00d3\3\2\2\2\u075c"+
		"\u075a\3\2\2\2\u075d\u0778\7\u0081\2\2\u075e\u0778\7\u0083\2\2\u075f\u0778"+
		"\7\u00e7\2\2\u0760\u0778\7\u00e8\2\2\u0761\u0778\7\u00e6\2\2\u0762\u0763"+
		"\7z\2\2\u0763\u0764\7\u0118\2\2\u0764\u0765\7\u0082\2\2\u0765\u0778\7"+
		"{\2\2\u0766\u0778\7\u0086\2\2\u0767\u0769\7\u0118\2\2\u0768\u0767\3\2"+
		"\2\2\u0768\u0769\3\2\2\2\u0769\u076a\3\2\2\2\u076a\u076c\7\u0082\2\2\u076b"+
		"\u076d\5\u00dan\2\u076c\u076b\3\2\2\2\u076c\u076d\3\2\2\2\u076d\u0778"+
		"\3\2\2\2\u076e\u076f\t*\2\2\u076f\u0778\7\u0082\2\2\u0770\u0772\7\u0118"+
		"\2\2\u0771\u0770\3\2\2\2\u0771\u0772\3\2\2\2\u0772\u0773\3\2\2\2\u0773"+
		"\u0775\7\u0087\2\2\u0774\u0776\5\u00dan\2\u0775\u0774\3\2\2\2\u0775\u0776"+
		"\3\2\2\2\u0776\u0778\3\2\2\2\u0777\u075d\3\2\2\2\u0777\u075e\3\2\2\2\u0777"+
		"\u075f\3\2\2\2\u0777\u0760\3\2\2\2\u0777\u0761\3\2\2\2\u0777\u0762\3\2"+
		"\2\2\u0777\u0766\3\2\2\2\u0777\u0768\3\2\2\2\u0777\u076e\3\2\2\2\u0777"+
		"\u0771\3\2\2\2\u0778\u00d5\3\2\2\2\u0779\u077a\5\u00d8m\2\u077a\u077b"+
		"\5\u00dan\2\u077b\u00d7\3\2\2\2\u077c\u077d\t+\2\2\u077d\u077e\7\u00bf"+
		"\2\2\u077e\u0785\5\u00dco\2\u077f\u0781\7\13\2\2\u0780\u0782\7q\2\2\u0781"+
		"\u0780\3\2\2\2\u0781\u0782\3\2\2\2\u0782\u0783\3\2\2\2\u0783\u0785\5\u00dc"+
		"o\2\u0784\u077c\3\2\2\2\u0784\u077f\3\2\2\2\u0785\u00d9\3\2\2\2\u0786"+
		"\u0787\7\f\2\2\u0787\u0788\7\u0118\2\2\u0788\u00db\3\2\2\2\u0789\u078a"+
		"\t,\2\2\u078a\u00dd\3\2\2\2\u078b\u078c\5\u00e0q\2\u078c\u078d\5\u00e2"+
		"r\2\u078d\u00df\3\2\2\2\u078e\u078f\t+\2\2\u078f\u0791\7\u00bf\2\2\u0790"+
		"\u0792\7q\2\2\u0791\u0790\3\2\2\2\u0791\u0792\3\2\2\2\u0792\u0793\3\2"+
		"\2\2\u0793\u079a\5\u00dco\2\u0794\u0796\7\13\2\2\u0795\u0797\7q\2\2\u0796"+
		"\u0795\3\2\2\2\u0796\u0797\3\2\2\2\u0797\u0798\3\2\2\2\u0798\u079a\5\u00dc"+
		"o\2\u0799\u078e\3\2\2\2\u0799\u0794\3\2\2\2\u079a\u00e1\3\2\2\2\u079b"+
		"\u079d\7\f\2\2\u079c\u079e\7q\2\2\u079d\u079c\3\2\2\2\u079d\u079e\3\2"+
		"\2\2\u079e\u079f\3\2\2\2\u079f\u07a0\7\u0118\2\2\u07a0\u00e3\3\2\2\2\u07a1"+
		"\u07a3\5\u00d0i\2\u07a2\u07a4\7\u0099\2\2\u07a3\u07a2\3\2\2\2\u07a3\u07a4"+
		"\3\2\2\2\u07a4\u07a6\3\2\2\2\u07a5\u07a7\5\u009eP\2\u07a6\u07a5\3\2\2"+
		"\2\u07a6\u07a7\3\2\2\2\u07a7\u00e5\3\2\2\2\u07a8\u07ab\5\u00e8u\2\u07a9"+
		"\u07ab\5\u00ecw\2\u07aa\u07a8\3\2\2\2\u07aa\u07a9\3\2\2\2\u07ab\u00e7"+
		"\3\2\2\2\u07ac\u07ae\7\u0094\2\2\u07ad\u07af\7\u0095\2\2\u07ae\u07ad\3"+
		"\2\2\2\u07ae\u07af\3\2\2\2\u07af\u07b0\3\2\2\2\u07b0\u07b5\5\u00eav\2"+
		"\u07b1\u07b2\7|\2\2\u07b2\u07b4\5\u00eav\2\u07b3\u07b1\3\2\2\2\u07b4\u07b7"+
		"\3\2\2\2\u07b5\u07b3\3\2\2\2\u07b5\u07b6\3\2\2\2\u07b6\u07b8\3\2\2\2\u07b7"+
		"\u07b5\3\2\2\2\u07b8\u07b9\5\u00ecw\2\u07b9\u00e9\3\2\2\2\u07ba\u07bc"+
		"\5\u00a0Q\2\u07bb\u07bd\5\u00a2R\2\u07bc\u07bb\3\2\2\2\u07bc\u07bd\3\2"+
		"\2\2\u07bd\u07be\3\2\2\2\u07be\u07bf\7\u0099\2\2\u07bf\u07c0\5\u0106\u0084"+
		"\2\u07c0\u00eb\3\2\2\2\u07c1\u07c9\5\u00eex\2\u07c2\u07c4\7\u00d7\2\2"+
		"\u07c3\u07c5\7\u008a\2\2\u07c4\u07c3\3\2\2\2\u07c4\u07c5\3\2\2\2\u07c5"+
		"\u07c6\3\2\2\2\u07c6\u07c8\5\u00eex\2\u07c7\u07c2\3\2\2\2\u07c8\u07cb"+
		"\3\2\2\2\u07c9\u07c7\3\2\2\2\u07c9\u07ca\3\2\2\2\u07ca\u00ed\3\2\2\2\u07cb"+
		"\u07c9\3\2\2\2\u07cc\u07ce\5\u00f0y\2\u07cd\u07cf\5\u00f2z\2\u07ce\u07cd"+
		"\3\2\2\2\u07ce\u07cf\3\2\2\2\u07cf\u07d1\3\2\2\2\u07d0\u07d2\5\u00f4{"+
		"\2\u07d1\u07d0\3\2\2\2\u07d1\u07d2\3\2\2\2\u07d2\u07d4\3\2\2\2\u07d3\u07d5"+
		"\5\u00f6|\2\u07d4\u07d3\3\2\2\2\u07d4\u07d5\3\2\2\2\u07d5\u07d7\3\2\2"+
		"\2\u07d6\u07d8\5\u00fa~\2\u07d7\u07d6\3\2\2\2\u07d7\u07d8\3\2\2\2\u07d8"+
		"\u07da\3\2\2\2\u07d9\u07db\5\u00fe\u0080\2\u07da\u07d9\3\2\2\2\u07da\u07db"+
		"\3\2\2\2\u07db\u00ef\3\2\2\2\u07dc\u07dd\7\u0089\2\2\u07dd\u07de\5\u00a8"+
		"U\2\u07de\u07df\5\u0104\u0083\2\u07df\u00f1\3\2\2\2\u07e0\u07e1\7\u008d"+
		"\2\2\u07e1\u07e2\5\u00ba^\2\u07e2\u00f3\3\2\2\2\u07e3\u07e4\7\u008f\2"+
		"\2\u07e4\u07e5\5\u00c8e\2\u07e5\u00f5\3\2\2\2\u07e6\u07e7\7\u0090\2\2"+
		"\u07e7\u07e8\7\u0091\2\2\u07e8\u07ed\5\u00fc\177\2\u07e9\u07ea\7|\2\2"+
		"\u07ea\u07ec\5\u00fc\177\2\u07eb\u07e9\3\2\2\2\u07ec\u07ef\3\2\2\2\u07ed"+
		"\u07eb\3\2\2\2\u07ed\u07ee\3\2\2\2\u07ee\u07f2\3\2\2\2\u07ef\u07ed\3\2"+
		"\2\2\u07f0\u07f1\7\u0094\2\2\u07f1\u07f3\7\u0096\2\2\u07f2\u07f0\3\2\2"+
		"\2\u07f2\u07f3\3\2\2\2\u07f3\u07f5\3\2\2\2\u07f4\u07f6\5\u00f8}\2\u07f5"+
		"\u07f4\3\2\2\2\u07f5\u07f6\3\2\2\2\u07f6\u00f7\3\2\2\2\u07f7\u07f8\7\u0097"+
		"\2\2\u07f8\u07f9\5\u00c8e\2\u07f9\u00f9\3\2\2\2\u07fa\u07fb\7\u009a\2"+
		"\2\u07fb\u07fc\7\u0091\2\2\u07fc\u0801\5\u00fc\177\2\u07fd\u07fe\7|\2"+
		"\2\u07fe\u0800\5\u00fc\177\2\u07ff\u07fd\3\2\2\2\u0800\u0803\3\2\2\2\u0801"+
		"\u07ff\3\2\2\2\u0801\u0802\3\2\2\2\u0802\u00fb\3\2\2\2\u0803\u0801\3\2"+
		"\2\2\u0804\u0808\5\u0098M\2\u0805\u0808\7\u0083\2\2\u0806\u0808\5\u00c8"+
		"e\2\u0807\u0804\3\2\2\2\u0807\u0805\3\2\2\2\u0807\u0806\3\2\2\2\u0808"+
		"\u080a\3\2\2\2\u0809\u080b\t\37\2\2\u080a\u0809\3\2\2\2\u080a\u080b\3"+
		"\2\2\2\u080b\u00fd\3\2\2\2\u080c\u080d\7\u009b\2\2\u080d\u080e\5\u00a4"+
		"S\2\u080e\u00ff\3\2\2\2\u080f\u0810\7\u008e\2\2\u0810\u0811\5\u00a2R\2"+
		"\u0811\u0101\3\2\2\2\u0812\u0813\7\u0118\2\2\u0813\u081d\7x\2\2\u0814"+
		"\u081e\3\2\2\2\u0815\u081a\5\u00c8e\2\u0816\u0817\7|\2\2\u0817\u0819\5"+
		"\u00c8e\2\u0818\u0816\3\2\2\2\u0819\u081c\3\2\2\2\u081a\u0818\3\2\2\2"+
		"\u081a\u081b\3\2\2\2\u081b\u081e\3\2\2\2\u081c\u081a\3\2\2\2\u081d\u0814"+
		"\3\2\2\2\u081d\u0815\3\2\2\2\u081e\u081f\3\2\2\2\u081f\u0820\7y\2\2\u0820"+
		"\u0103\3\2\2\2\u0821\u0826\7l\2\2\u0822\u0823\7|\2\2\u0823\u0825\5\u00e4"+
		"s\2\u0824\u0822\3\2\2\2\u0825\u0828\3\2\2\2\u0826\u0824\3\2\2\2\u0826"+
		"\u0827\3\2\2\2\u0827\u0836\3\2\2\2\u0828\u0826\3\2\2\2\u0829\u082c\5\u00e4"+
		"s\2\u082a\u082b\7|\2\2\u082b\u082d\7l\2\2\u082c\u082a\3\2\2\2\u082c\u082d"+
		"\3\2\2\2\u082d\u0832\3\2\2\2\u082e\u082f\7|\2\2\u082f\u0831\5\u00e4s\2"+
		"\u0830\u082e\3\2\2\2\u0831\u0834\3\2\2\2\u0832\u0830\3\2\2\2\u0832\u0833"+
		"\3\2\2\2\u0833\u0836\3\2\2\2\u0834\u0832\3\2\2\2\u0835\u0821\3\2\2\2\u0835"+
		"\u0829\3\2\2\2\u0836\u0105\3\2\2\2\u0837\u0838\7x\2\2\u0838\u0839\5\u00ec"+
		"w\2\u0839\u083a\7y\2\2\u083a\u0107\3\2\2\2\u083b\u0842\5\4\3\2\u083c\u0842"+
		"\5\2\2\2\u083d\u0842\5\6\4\2\u083e\u0842\5\b\5\2\u083f\u0842\5\u010a\u0086"+
		"\2\u0840\u0842\5\u010e\u0088\2\u0841\u083b\3\2\2\2\u0841\u083c\3\2\2\2"+
		"\u0841\u083d\3\2\2\2\u0841\u083e\3\2\2\2\u0841\u083f\3\2\2\2\u0841\u0840"+
		"\3\2\2\2\u0842\u0109\3\2\2\2\u0843\u0844\7\u009e\2\2\u0844\u0845\7\u00a1"+
		"\2\2\u0845\u0846\5\u010c\u0087\2\u0846\u0848\5\u0096L\2\u0847\u0849\5"+
		"\32\16\2\u0848\u0847\3\2\2\2\u0848\u0849\3\2\2\2\u0849\u084b\3\2\2\2\u084a"+
		"\u084c\5x=\2\u084b\u084a\3\2\2\2\u084b\u084c\3\2\2\2\u084c\u010b\3\2\2"+
		"\2\u084d\u084e\3\2\2\2\u084e\u010d\3\2\2\2\u084f\u0851\7\u009f\2\2\u0850"+
		"\u0852\7\u00a0\2\2\u0851\u0850\3\2\2\2\u0851\u0852\3\2\2\2\u0852\u0853"+
		"\3\2\2\2\u0853\u0857\7\u00a1\2\2\u0854\u0855\7\u00f4\2\2\u0855\u0856\7"+
		"\u00e4\2\2\u0856\u0858\7\u00df\2\2\u0857\u0854\3\2\2\2\u0857\u0858\3\2"+
		"\2\2\u0858\u0859\3\2\2\2\u0859\u085a\5\u0096L\2\u085a\u085b\5\n\6\2\u085b"+
		"\u010f\3\2\2\2\u0149\u0112\u0117\u011d\u0121\u0123\u012b\u012d\u0132\u0137"+
		"\u013e\u0142\u0146\u014d\u0153\u0156\u015d\u0164\u0166\u016f\u0172\u0175"+
		"\u0178\u017b\u0184\u018e\u0194\u0198\u01a1\u01a8\u01ac\u01c7\u01cc\u01dc"+
		"\u01e3\u01ea\u01fd\u0203\u0209\u020f\u0215\u021b\u0221\u022a\u022e\u0232"+
		"\u0246\u024c\u0252\u0256\u025c\u0262\u0265\u026c\u026f\u0273\u0279\u0281"+
		"\u0288\u028a\u028f\u0294\u0298\u029c\u029f\u02a2\u02a6\u02ab\u02b3\u02bb"+
		"\u02c0\u02c5\u02ca\u02ce\u02d3\u02d7\u02dc\u02e1\u02e6\u02ec\u02f1\u02f6"+
		"\u02fb\u0300\u0305\u030a\u030f\u0314\u0319\u031e\u0323\u0328\u032d\u0334"+
		"\u0338\u033b\u0343\u0347\u034b\u034e\u0351\u0354\u0359\u035c\u035f\u0362"+
		"\u0367\u036a\u036e\u0372\u0375\u0378\u037f\u0381\u0386\u038a\u038d\u0390"+
		"\u0394\u0398\u039b\u039e\u03a1\u03a4\u03a8\u03ac\u03af\u03b2\u03b8\u03bc"+
		"\u03bf\u03c3\u03c6\u03c9\u03cc\u03cf\u03d8\u03dd\u03e0\u03e4\u03e7\u03ea"+
		"\u03ec\u03f0\u03f4\u0401\u0404\u0408\u040c\u040e\u0411\u0414\u0418\u041c"+
		"\u0420\u0423\u0427\u042e\u0433\u0437\u0439\u043c\u043f\u0443\u044e\u0453"+
		"\u0458\u045d\u0462\u0464\u046e\u047a\u0481\u0487\u048a\u0493\u049c\u04a7"+
		"\u04b0\u04b4\u04bb\u04bd\u04c0\u04c3\u04c8\u04cb\u04d6\u04e7\u04ef\u04fa"+
		"\u04fc\u04ff\u0503\u0506\u050a\u050d\u0512\u0515\u051a\u051d\u0521\u0524"+
		"\u0528\u052b\u052f\u0532\u0539\u053c\u0543\u0548\u054d\u0551\u0554\u0558"+
		"\u055b\u0560\u0563\u0568\u056b\u056f\u0572\u0576\u0579\u057d\u0580\u0584"+
		"\u058b\u05a6\u05b0\u05b6\u05be\u05c4\u05c7\u05ca\u05cd\u05d0\u05d3\u05d6"+
		"\u05d9\u05dd\u05e4\u05e7\u05f4\u05f7\u0608\u060f\u0617\u061d\u0620\u0625"+
		"\u0628\u062b\u062e\u0632\u063a\u063d\u0642\u064c\u0656\u065a\u0660\u0667"+
		"\u0672\u0676\u0682\u0686\u068a\u0696\u06a7\u06a9\u06b2\u06c1\u06c3\u06ca"+
		"\u06d1\u06da\u06e1\u06ef\u06f7\u06fc\u0702\u072b\u072d\u0749\u0751\u0758"+
		"\u075a\u0768\u076c\u0771\u0775\u0777\u0781\u0784\u0791\u0796\u0799\u079d"+
		"\u07a3\u07a6\u07aa\u07ae\u07b5\u07bc\u07c4\u07c9\u07ce\u07d1\u07d4\u07d7"+
		"\u07da\u07ed\u07f2\u07f5\u0801\u0807\u080a\u081a\u081d\u0826\u082c\u0832"+
		"\u0835\u0841\u0848\u084b\u0851\u0857";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}