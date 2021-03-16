// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/antlr4/imports/sql92/DMLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DMLStatementParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND_=1, CONCAT_=2, NOT_=3, TILDE_=4, VERTICAL_BAR_=5, AMPERSAND_=6, SIGNED_LEFT_SHIFT_=7, 
		SIGNED_RIGHT_SHIFT_=8, CARET_=9, MOD_=10, COLON_=11, PLUS_=12, MINUS_=13, 
		ASTERISK_=14, SLASH_=15, BACKSLASH_=16, DOT_=17, DOT_ASTERISK_=18, SAFE_EQ_=19, 
		DEQ_=20, EQ_=21, NEQ_=22, GT_=23, GTE_=24, LT_=25, LTE_=26, POUND_=27, 
		LP_=28, RP_=29, LBE_=30, RBE_=31, LBT_=32, RBT_=33, COMMA_=34, DQ_=35, 
		SQ_=36, QUESTION_=37, AT_=38, SEMI_=39, WS=40, SELECT=41, INSERT=42, UPDATE=43, 
		DELETE=44, CREATE=45, ALTER=46, DROP=47, TRUNCATE=48, SCHEMA=49, GRANT=50, 
		REVOKE=51, ADD=52, SET=53, TABLE=54, COLUMN=55, INDEX=56, CONSTRAINT=57, 
		PRIMARY=58, UNIQUE=59, FOREIGN=60, KEY=61, POSITION=62, PRECISION=63, 
		FUNCTION=64, TRIGGER=65, PROCEDURE=66, VIEW=67, INTO=68, VALUES=69, WITH=70, 
		UNION=71, DISTINCT=72, CASE=73, WHEN=74, CAST=75, TRIM=76, SUBSTRING=77, 
		FROM=78, NATURAL=79, JOIN=80, FULL=81, INNER=82, OUTER=83, LEFT=84, RIGHT=85, 
		CROSS=86, USING=87, WHERE=88, AS=89, ON=90, IF=91, ELSE=92, THEN=93, FOR=94, 
		TO=95, AND=96, OR=97, IS=98, NOT=99, NULL=100, TRUE=101, FALSE=102, EXISTS=103, 
		BETWEEN=104, IN=105, ALL=106, ANY=107, LIKE=108, ORDER=109, GROUP=110, 
		BY=111, ASC=112, DESC=113, HAVING=114, LIMIT=115, OFFSET=116, BEGIN=117, 
		COMMIT=118, ROLLBACK=119, SAVEPOINT=120, BOOLEAN=121, DOUBLE=122, CHAR=123, 
		CHARACTER=124, ARRAY=125, INTERVAL=126, DATE=127, TIME=128, TIMESTAMP=129, 
		LOCALTIME=130, LOCALTIMESTAMP=131, YEAR=132, QUARTER=133, MONTH=134, WEEK=135, 
		DAY=136, HOUR=137, MINUTE=138, SECOND=139, MICROSECOND=140, MAX=141, MIN=142, 
		SUM=143, COUNT=144, AVG=145, DEFAULT=146, CURRENT=147, ENABLE=148, DISABLE=149, 
		CALL=150, INSTANCE=151, PRESERVE=152, DO=153, DEFINER=154, CURRENT_USER=155, 
		SQL=156, CASCADED=157, LOCAL=158, CLOSE=159, OPEN=160, NEXT=161, NAME=162, 
		COLLATION=163, NAMES=164, INTEGER=165, REAL=166, DECIMAL=167, TYPE=168, 
		VARCHAR=169, FLOAT=170, FOR_GENERATOR=171, ADA=172, C92=173, CATALOG_NAME=174, 
		CHARACTER_SET_CATALOG=175, CHARACTER_SET_NAME=176, CHARACTER_SET_SCHEMA=177, 
		CLASS_ORIGIN=178, COBOL=179, COLLATION_CATALOG=180, COLLATION_NAME=181, 
		COLLATION_SCHEMA=182, COLUMN_NAME=183, COMMAND_FUNCTION=184, COMMITTED=185, 
		CONDITION_NUMBER=186, CONNECTION_NAME=187, CONSTRAINT_CATALOG=188, CONSTRAINT_NAME=189, 
		CONSTRAINT_SCHEMA=190, CURSOR_NAME=191, DATA=192, DATETIME_INTERVAL_CODE=193, 
		DATETIME_INTERVAL_PRECISION=194, DYNAMIC_FUNCTION=195, FORTRAN=196, LENGTH=197, 
		MESSAGE_LENGTH=198, MESSAGE_OCTET_LENGTH=199, MESSAGE_TEXT=200, MORE92=201, 
		MUMPS=202, NULLABLE=203, NUMBER=204, PASCAL=205, PLI=206, REPEATABLE=207, 
		RETURNED_LENGTH=208, RETURNED_OCTET_LENGTH=209, RETURNED_SQLSTATE=210, 
		ROW_COUNT=211, SCALE=212, SCHEMA_NAME=213, SERIALIZABLE=214, SERVER_NAME=215, 
		SUBCLASS_ORIGIN=216, TABLE_NAME=217, UNCOMMITTED=218, UNNAMED=219, ABSOLUTE=220, 
		ACTION=221, ALLOCATE=222, ARE=223, ASSERTION=224, AT=225, AUTHORIZATION=226, 
		BIT=227, BIT_LENGTH=228, BOTH=229, CASCADE=230, CATALOG=231, CHAR_LENGTH=232, 
		CHARACTER_LENGTH=233, CHECK=234, COALESCE=235, COLLATE=236, CONNECT=237, 
		CONNECTION=238, CONSTRAINTS=239, CONTINUE=240, CONVERT=241, CORRESPONDING=242, 
		CURRENT_DATE=243, CURRENT_TIME=244, CURRENT_TIMESTAMP=245, CURSOR=246, 
		DEALLOCATE=247, DEC=248, DECLARE=249, DEFERRABLE=250, DEFERRED=251, DESCRIBE=252, 
		DESCRIPTOR=253, DIAGNOSTICS=254, DISCONNECT=255, DOMAIN=256, END=257, 
		END_EXEC=258, ESCAPE=259, EXCEPT=260, EXCEPTION=261, EXEC=262, EXECUTE=263, 
		EXTERNAL=264, EXTRACT=265, FETCH=266, FIRST=267, FOUND=268, GET=269, GLOBAL=270, 
		GO=271, GOTO=272, IDENTITY=273, IMMEDIATE=274, INDICATOR=275, INITIALLY=276, 
		INPUT=277, INSENSITIVE=278, INTERSECT=279, ISOLATION=280, LANGUAGE=281, 
		LAST=282, LEADING=283, LEVEL=284, LOWER=285, MATCH=286, MODULE=287, NATIONAL=288, 
		NCHAR=289, NO=290, NULLIF=291, NUMERIC=292, OCTET_LENGTH=293, OF=294, 
		ONLY=295, OPTION=296, OUTPUT=297, OVERLAPS=298, PAD=299, PARTIAL=300, 
		PREPARE=301, PRIOR=302, PRIVILEGES=303, PUBLIC=304, READ=305, REFERENCES=306, 
		RELATIVE=307, RESTRICT=308, ROWS=309, SCROLL=310, SECTION=311, SESSION=312, 
		SESSION_USER=313, SIZE=314, SMALLINT=315, SOME=316, SPACE=317, SQLCODE=318, 
		SQLERROR=319, SQLSTATE=320, SYSTEM_USER=321, TEMPORARY=322, TIMEZONE_HOUR=323, 
		TIMEZONE_MINUTE=324, TRAILING=325, TRANSACTION=326, TRANSLATE=327, TRANSLATION=328, 
		UNKNOWN=329, UPPER=330, USAGE=331, USER=332, VALUE=333, VARYING=334, WHENEVER=335, 
		WORK=336, WRITE=337, ZONE=338, IDENTIFIER_=339, STRING_=340, NUMBER_=341, 
		HEX_DIGIT_=342, BIT_NUM_=343;
	public static final int
		RULE_insert = 0, RULE_insertValuesClause = 1, RULE_insertSelectClause = 2, 
		RULE_update = 3, RULE_assignment = 4, RULE_setAssignmentsClause = 5, RULE_assignmentValues = 6, 
		RULE_assignmentValue = 7, RULE_blobValue = 8, RULE_delete = 9, RULE_singleTableClause = 10, 
		RULE_select = 11, RULE_unionClause = 12, RULE_selectClause = 13, RULE_selectSpecification = 14, 
		RULE_duplicateSpecification = 15, RULE_projections = 16, RULE_projection = 17, 
		RULE_alias = 18, RULE_unqualifiedShorthand = 19, RULE_qualifiedShorthand = 20, 
		RULE_fromClause = 21, RULE_tableReferences = 22, RULE_escapedTableReference = 23, 
		RULE_tableReference = 24, RULE_tableFactor = 25, RULE_joinedTable = 26, 
		RULE_joinSpecification = 27, RULE_whereClause = 28, RULE_groupByClause = 29, 
		RULE_havingClause = 30, RULE_limitClause = 31, RULE_limitRowCount = 32, 
		RULE_limitOffset = 33, RULE_subquery = 34, RULE_parameterMarker = 35, 
		RULE_literals = 36, RULE_stringLiterals = 37, RULE_numberLiterals = 38, 
		RULE_dateTimeLiterals = 39, RULE_hexadecimalLiterals = 40, RULE_bitValueLiterals = 41, 
		RULE_booleanLiterals = 42, RULE_nullValueLiterals = 43, RULE_identifier = 44, 
		RULE_unreservedWord = 45, RULE_variable = 46, RULE_schemaName = 47, RULE_tableName = 48, 
		RULE_columnName = 49, RULE_viewName = 50, RULE_owner = 51, RULE_name = 52, 
		RULE_columnNames = 53, RULE_tableNames = 54, RULE_characterSetName = 55, 
		RULE_expr = 56, RULE_logicalOperator = 57, RULE_notOperator = 58, RULE_booleanPrimary = 59, 
		RULE_comparisonOperator = 60, RULE_predicate = 61, RULE_bitExpr = 62, 
		RULE_simpleExpr = 63, RULE_functionCall = 64, RULE_aggregationFunction = 65, 
		RULE_aggregationFunctionName = 66, RULE_distinct = 67, RULE_specialFunction = 68, 
		RULE_castFunction = 69, RULE_convertFunction = 70, RULE_positionFunction = 71, 
		RULE_substringFunction = 72, RULE_extractFunction = 73, RULE_trimFunction = 74, 
		RULE_regularFunction = 75, RULE_regularFunctionName = 76, RULE_matchExpression = 77, 
		RULE_caseExpression = 78, RULE_caseWhen = 79, RULE_caseElse = 80, RULE_intervalExpression = 81, 
		RULE_intervalUnit = 82, RULE_orderByClause = 83, RULE_orderByItem = 84, 
		RULE_dataType = 85, RULE_dataTypeName = 86, RULE_dataTypeLength = 87, 
		RULE_characterSet = 88, RULE_collateClause = 89, RULE_ignoredIdentifier = 90, 
		RULE_dropBehaviour = 91;
	private static String[] makeRuleNames() {
		return new String[] {
			"insert", "insertValuesClause", "insertSelectClause", "update", "assignment", 
			"setAssignmentsClause", "assignmentValues", "assignmentValue", "blobValue", 
			"delete", "singleTableClause", "select", "unionClause", "selectClause", 
			"selectSpecification", "duplicateSpecification", "projections", "projection", 
			"alias", "unqualifiedShorthand", "qualifiedShorthand", "fromClause", 
			"tableReferences", "escapedTableReference", "tableReference", "tableFactor", 
			"joinedTable", "joinSpecification", "whereClause", "groupByClause", "havingClause", 
			"limitClause", "limitRowCount", "limitOffset", "subquery", "parameterMarker", 
			"literals", "stringLiterals", "numberLiterals", "dateTimeLiterals", "hexadecimalLiterals", 
			"bitValueLiterals", "booleanLiterals", "nullValueLiterals", "identifier", 
			"unreservedWord", "variable", "schemaName", "tableName", "columnName", 
			"viewName", "owner", "name", "columnNames", "tableNames", "characterSetName", 
			"expr", "logicalOperator", "notOperator", "booleanPrimary", "comparisonOperator", 
			"predicate", "bitExpr", "simpleExpr", "functionCall", "aggregationFunction", 
			"aggregationFunctionName", "distinct", "specialFunction", "castFunction", 
			"convertFunction", "positionFunction", "substringFunction", "extractFunction", 
			"trimFunction", "regularFunction", "regularFunctionName", "matchExpression", 
			"caseExpression", "caseWhen", "caseElse", "intervalExpression", "intervalUnit", 
			"orderByClause", "orderByItem", "dataType", "dataTypeName", "dataTypeLength", 
			"characterSet", "collateClause", "ignoredIdentifier", "dropBehaviour"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", 
			"'%'", "':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", 
			"'=='", "'='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", "')'", 
			"'{'", "'}'", "'['", "']'", "','", "'\"'", "'''", "'?'", "'@'", "';'", 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "AND_", "CONCAT_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", "POUND_", 
			"LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", "SQ_", 
			"QUESTION_", "AT_", "SEMI_", "WS", "SELECT", "INSERT", "UPDATE", "DELETE", 
			"CREATE", "ALTER", "DROP", "TRUNCATE", "SCHEMA", "GRANT", "REVOKE", "ADD", 
			"SET", "TABLE", "COLUMN", "INDEX", "CONSTRAINT", "PRIMARY", "UNIQUE", 
			"FOREIGN", "KEY", "POSITION", "PRECISION", "FUNCTION", "TRIGGER", "PROCEDURE", 
			"VIEW", "INTO", "VALUES", "WITH", "UNION", "DISTINCT", "CASE", "WHEN", 
			"CAST", "TRIM", "SUBSTRING", "FROM", "NATURAL", "JOIN", "FULL", "INNER", 
			"OUTER", "LEFT", "RIGHT", "CROSS", "USING", "WHERE", "AS", "ON", "IF", 
			"ELSE", "THEN", "FOR", "TO", "AND", "OR", "IS", "NOT", "NULL", "TRUE", 
			"FALSE", "EXISTS", "BETWEEN", "IN", "ALL", "ANY", "LIKE", "ORDER", "GROUP", 
			"BY", "ASC", "DESC", "HAVING", "LIMIT", "OFFSET", "BEGIN", "COMMIT", 
			"ROLLBACK", "SAVEPOINT", "BOOLEAN", "DOUBLE", "CHAR", "CHARACTER", "ARRAY", 
			"INTERVAL", "DATE", "TIME", "TIMESTAMP", "LOCALTIME", "LOCALTIMESTAMP", 
			"YEAR", "QUARTER", "MONTH", "WEEK", "DAY", "HOUR", "MINUTE", "SECOND", 
			"MICROSECOND", "MAX", "MIN", "SUM", "COUNT", "AVG", "DEFAULT", "CURRENT", 
			"ENABLE", "DISABLE", "CALL", "INSTANCE", "PRESERVE", "DO", "DEFINER", 
			"CURRENT_USER", "SQL", "CASCADED", "LOCAL", "CLOSE", "OPEN", "NEXT", 
			"NAME", "COLLATION", "NAMES", "INTEGER", "REAL", "DECIMAL", "TYPE", "VARCHAR", 
			"FLOAT", "FOR_GENERATOR", "ADA", "C92", "CATALOG_NAME", "CHARACTER_SET_CATALOG", 
			"CHARACTER_SET_NAME", "CHARACTER_SET_SCHEMA", "CLASS_ORIGIN", "COBOL", 
			"COLLATION_CATALOG", "COLLATION_NAME", "COLLATION_SCHEMA", "COLUMN_NAME", 
			"COMMAND_FUNCTION", "COMMITTED", "CONDITION_NUMBER", "CONNECTION_NAME", 
			"CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CURSOR_NAME", 
			"DATA", "DATETIME_INTERVAL_CODE", "DATETIME_INTERVAL_PRECISION", "DYNAMIC_FUNCTION", 
			"FORTRAN", "LENGTH", "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH", "MESSAGE_TEXT", 
			"MORE92", "MUMPS", "NULLABLE", "NUMBER", "PASCAL", "PLI", "REPEATABLE", 
			"RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE", "ROW_COUNT", 
			"SCALE", "SCHEMA_NAME", "SERIALIZABLE", "SERVER_NAME", "SUBCLASS_ORIGIN", 
			"TABLE_NAME", "UNCOMMITTED", "UNNAMED", "ABSOLUTE", "ACTION", "ALLOCATE", 
			"ARE", "ASSERTION", "AT", "AUTHORIZATION", "BIT", "BIT_LENGTH", "BOTH", 
			"CASCADE", "CATALOG", "CHAR_LENGTH", "CHARACTER_LENGTH", "CHECK", "COALESCE", 
			"COLLATE", "CONNECT", "CONNECTION", "CONSTRAINTS", "CONTINUE", "CONVERT", 
			"CORRESPONDING", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", 
			"CURSOR", "DEALLOCATE", "DEC", "DECLARE", "DEFERRABLE", "DEFERRED", "DESCRIBE", 
			"DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DOMAIN", "END", "END_EXEC", 
			"ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXTERNAL", "EXTRACT", 
			"FETCH", "FIRST", "FOUND", "GET", "GLOBAL", "GO", "GOTO", "IDENTITY", 
			"IMMEDIATE", "INDICATOR", "INITIALLY", "INPUT", "INSENSITIVE", "INTERSECT", 
			"ISOLATION", "LANGUAGE", "LAST", "LEADING", "LEVEL", "LOWER", "MATCH", 
			"MODULE", "NATIONAL", "NCHAR", "NO", "NULLIF", "NUMERIC", "OCTET_LENGTH", 
			"OF", "ONLY", "OPTION", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "PREPARE", 
			"PRIOR", "PRIVILEGES", "PUBLIC", "READ", "REFERENCES", "RELATIVE", "RESTRICT", 
			"ROWS", "SCROLL", "SECTION", "SESSION", "SESSION_USER", "SIZE", "SMALLINT", 
			"SOME", "SPACE", "SQLCODE", "SQLERROR", "SQLSTATE", "SYSTEM_USER", "TEMPORARY", 
			"TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TRAILING", "TRANSACTION", "TRANSLATE", 
			"TRANSLATION", "UNKNOWN", "UPPER", "USAGE", "USER", "VALUE", "VARYING", 
			"WHENEVER", "WORK", "WRITE", "ZONE", "IDENTIFIER_", "STRING_", "NUMBER_", 
			"HEX_DIGIT_", "BIT_NUM_"
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
	public String getGrammarFileName() { return "DMLStatement.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DMLStatementParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class InsertContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(DMLStatementParser.INSERT, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public InsertValuesClauseContext insertValuesClause() {
			return getRuleContext(InsertValuesClauseContext.class,0);
		}
		public InsertSelectClauseContext insertSelectClause() {
			return getRuleContext(InsertSelectClauseContext.class,0);
		}
		public TerminalNode INTO() { return getToken(DMLStatementParser.INTO, 0); }
		public InsertContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insert; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterInsert(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitInsert(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitInsert(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertContext insert() throws RecognitionException {
		InsertContext _localctx = new InsertContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_insert);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(INSERT);
			setState(186);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTO) {
				{
				setState(185);
				match(INTO);
				}
			}

			setState(188);
			tableName();
			setState(191);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(189);
				insertValuesClause();
				}
				break;
			case 2:
				{
				setState(190);
				insertSelectClause();
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

	public static class InsertValuesClauseContext extends ParserRuleContext {
		public List<AssignmentValuesContext> assignmentValues() {
			return getRuleContexts(AssignmentValuesContext.class);
		}
		public AssignmentValuesContext assignmentValues(int i) {
			return getRuleContext(AssignmentValuesContext.class,i);
		}
		public TerminalNode VALUES() { return getToken(DMLStatementParser.VALUES, 0); }
		public TerminalNode VALUE() { return getToken(DMLStatementParser.VALUE, 0); }
		public ColumnNamesContext columnNames() {
			return getRuleContext(ColumnNamesContext.class,0);
		}
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public InsertValuesClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertValuesClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterInsertValuesClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitInsertValuesClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitInsertValuesClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertValuesClauseContext insertValuesClause() throws RecognitionException {
		InsertValuesClauseContext _localctx = new InsertValuesClauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_insertValuesClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_ || ((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0) || _la==IDENTIFIER_) {
				{
				setState(193);
				columnNames();
				}
			}

			setState(196);
			_la = _input.LA(1);
			if ( !(_la==VALUES || _la==VALUE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(197);
			assignmentValues();
			setState(202);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(198);
				match(COMMA_);
				setState(199);
				assignmentValues();
				}
				}
				setState(204);
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

	public static class InsertSelectClauseContext extends ParserRuleContext {
		public SelectContext select() {
			return getRuleContext(SelectContext.class,0);
		}
		public ColumnNamesContext columnNames() {
			return getRuleContext(ColumnNamesContext.class,0);
		}
		public InsertSelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertSelectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterInsertSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitInsertSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitInsertSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertSelectClauseContext insertSelectClause() throws RecognitionException {
		InsertSelectClauseContext _localctx = new InsertSelectClauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_insertSelectClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_ || ((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0) || _la==IDENTIFIER_) {
				{
				setState(205);
				columnNames();
				}
			}

			setState(208);
			select();
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
		public TerminalNode UPDATE() { return getToken(DMLStatementParser.UPDATE, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public SetAssignmentsClauseContext setAssignmentsClause() {
			return getRuleContext(SetAssignmentsClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public UpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_update; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterUpdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitUpdate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitUpdate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpdateContext update() throws RecognitionException {
		UpdateContext _localctx = new UpdateContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_update);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			match(UPDATE);
			setState(211);
			tableReferences();
			setState(212);
			setAssignmentsClause();
			setState(214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(213);
				whereClause();
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

	public static class AssignmentContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode EQ_() { return getToken(DMLStatementParser.EQ_, 0); }
		public AssignmentValueContext assignmentValue() {
			return getRuleContext(AssignmentValueContext.class,0);
		}
		public TerminalNode VALUES() { return getToken(DMLStatementParser.VALUES, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_assignment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216);
			columnName();
			setState(217);
			match(EQ_);
			setState(219);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUES) {
				{
				setState(218);
				match(VALUES);
				}
			}

			setState(222);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(221);
				match(LP_);
				}
				break;
			}
			setState(224);
			assignmentValue();
			setState(226);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RP_) {
				{
				setState(225);
				match(RP_);
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

	public static class SetAssignmentsClauseContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(DMLStatementParser.SET, 0); }
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public SetAssignmentsClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setAssignmentsClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSetAssignmentsClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSetAssignmentsClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSetAssignmentsClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetAssignmentsClauseContext setAssignmentsClause() throws RecognitionException {
		SetAssignmentsClauseContext _localctx = new SetAssignmentsClauseContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_setAssignmentsClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			match(SET);
			setState(229);
			assignment();
			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(230);
				match(COMMA_);
				setState(231);
				assignment();
				}
				}
				setState(236);
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

	public static class AssignmentValuesContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<AssignmentValueContext> assignmentValue() {
			return getRuleContexts(AssignmentValueContext.class);
		}
		public AssignmentValueContext assignmentValue(int i) {
			return getRuleContext(AssignmentValueContext.class,i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public AssignmentValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentValues; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterAssignmentValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitAssignmentValues(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitAssignmentValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentValuesContext assignmentValues() throws RecognitionException {
		AssignmentValuesContext _localctx = new AssignmentValuesContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_assignmentValues);
		int _la;
		try {
			setState(250);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				match(LP_);
				setState(238);
				assignmentValue();
				setState(243);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(239);
					match(COMMA_);
					setState(240);
					assignmentValue();
					}
					}
					setState(245);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(246);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(248);
				match(LP_);
				setState(249);
				match(RP_);
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

	public static class AssignmentValueContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(DMLStatementParser.DEFAULT, 0); }
		public BlobValueContext blobValue() {
			return getRuleContext(BlobValueContext.class,0);
		}
		public AssignmentValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentValueContext assignmentValue() throws RecognitionException {
		AssignmentValueContext _localctx = new AssignmentValueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_assignmentValue);
		try {
			setState(255);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(252);
				expr(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
				match(DEFAULT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(254);
				blobValue();
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

	public static class BlobValueContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(DMLStatementParser.STRING_, 0); }
		public BlobValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blobValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterBlobValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitBlobValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitBlobValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlobValueContext blobValue() throws RecognitionException {
		BlobValueContext _localctx = new BlobValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_blobValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
			match(STRING_);
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
		public TerminalNode DELETE() { return getToken(DMLStatementParser.DELETE, 0); }
		public SingleTableClauseContext singleTableClause() {
			return getRuleContext(SingleTableClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public DeleteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_delete; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDelete(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDelete(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDelete(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeleteContext delete() throws RecognitionException {
		DeleteContext _localctx = new DeleteContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_delete);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			match(DELETE);
			setState(260);
			singleTableClause();
			setState(262);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(261);
				whereClause();
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

	public static class SingleTableClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(DMLStatementParser.FROM, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(DMLStatementParser.AS, 0); }
		public SingleTableClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleTableClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSingleTableClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSingleTableClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSingleTableClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleTableClauseContext singleTableClause() throws RecognitionException {
		SingleTableClauseContext _localctx = new SingleTableClauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_singleTableClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
			match(FROM);
			setState(265);
			tableName();
			setState(270);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS || ((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0) || _la==IDENTIFIER_ || _la==STRING_) {
				{
				setState(267);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(266);
					match(AS);
					}
				}

				setState(269);
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
		public UnionClauseContext unionClause() {
			return getRuleContext(UnionClauseContext.class,0);
		}
		public SelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSelect(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSelect(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSelect(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectContext select() throws RecognitionException {
		SelectContext _localctx = new SelectContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_select);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			unionClause();
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

	public static class UnionClauseContext extends ParserRuleContext {
		public List<SelectClauseContext> selectClause() {
			return getRuleContexts(SelectClauseContext.class);
		}
		public SelectClauseContext selectClause(int i) {
			return getRuleContext(SelectClauseContext.class,i);
		}
		public List<TerminalNode> UNION() { return getTokens(DMLStatementParser.UNION); }
		public TerminalNode UNION(int i) {
			return getToken(DMLStatementParser.UNION, i);
		}
		public List<TerminalNode> ALL() { return getTokens(DMLStatementParser.ALL); }
		public TerminalNode ALL(int i) {
			return getToken(DMLStatementParser.ALL, i);
		}
		public UnionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterUnionClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitUnionClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitUnionClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionClauseContext unionClause() throws RecognitionException {
		UnionClauseContext _localctx = new UnionClauseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_unionClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			selectClause();
			setState(282);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==UNION) {
				{
				{
				setState(275);
				match(UNION);
				setState(277);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(276);
					match(ALL);
					}
				}

				setState(279);
				selectClause();
				}
				}
				setState(284);
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

	public static class SelectClauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(DMLStatementParser.SELECT, 0); }
		public ProjectionsContext projections() {
			return getRuleContext(ProjectionsContext.class,0);
		}
		public List<SelectSpecificationContext> selectSpecification() {
			return getRuleContexts(SelectSpecificationContext.class);
		}
		public SelectSpecificationContext selectSpecification(int i) {
			return getRuleContext(SelectSpecificationContext.class,i);
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
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_selectClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285);
			match(SELECT);
			setState(289);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DISTINCT || _la==ALL) {
				{
				{
				setState(286);
				selectSpecification();
				}
				}
				setState(291);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(292);
			projections();
			setState(294);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(293);
				fromClause();
				}
			}

			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(296);
				whereClause();
				}
			}

			setState(300);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(299);
				groupByClause();
				}
			}

			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(302);
				havingClause();
				}
			}

			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(305);
				orderByClause();
				}
			}

			setState(309);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(308);
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

	public static class SelectSpecificationContext extends ParserRuleContext {
		public DuplicateSpecificationContext duplicateSpecification() {
			return getRuleContext(DuplicateSpecificationContext.class,0);
		}
		public SelectSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectSpecification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSelectSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSelectSpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSelectSpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectSpecificationContext selectSpecification() throws RecognitionException {
		SelectSpecificationContext _localctx = new SelectSpecificationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_selectSpecification);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			duplicateSpecification();
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

	public static class DuplicateSpecificationContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(DMLStatementParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(DMLStatementParser.DISTINCT, 0); }
		public DuplicateSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_duplicateSpecification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDuplicateSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDuplicateSpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDuplicateSpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DuplicateSpecificationContext duplicateSpecification() throws RecognitionException {
		DuplicateSpecificationContext _localctx = new DuplicateSpecificationContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_duplicateSpecification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			_la = _input.LA(1);
			if ( !(_la==DISTINCT || _la==ALL) ) {
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

	public static class ProjectionsContext extends ParserRuleContext {
		public UnqualifiedShorthandContext unqualifiedShorthand() {
			return getRuleContext(UnqualifiedShorthandContext.class,0);
		}
		public List<ProjectionContext> projection() {
			return getRuleContexts(ProjectionContext.class);
		}
		public ProjectionContext projection(int i) {
			return getRuleContext(ProjectionContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public ProjectionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_projections; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterProjections(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitProjections(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitProjections(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProjectionsContext projections() throws RecognitionException {
		ProjectionsContext _localctx = new ProjectionsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_projections);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ASTERISK_:
				{
				setState(315);
				unqualifiedShorthand();
				}
				break;
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case POSITION:
			case CASE:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case LOCAL:
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case CONVERT:
			case CURRENT_TIMESTAMP:
			case EXTRACT:
			case GLOBAL:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(316);
				projection();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(323);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(319);
				match(COMMA_);
				setState(320);
				projection();
				}
				}
				setState(325);
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

	public static class ProjectionContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(DMLStatementParser.AS, 0); }
		public QualifiedShorthandContext qualifiedShorthand() {
			return getRuleContext(QualifiedShorthandContext.class,0);
		}
		public ProjectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_projection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterProjection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitProjection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitProjection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProjectionContext projection() throws RecognitionException {
		ProjectionContext _localctx = new ProjectionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_projection);
		int _la;
		try {
			setState(337);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(328);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(326);
					columnName();
					}
					break;
				case 2:
					{
					setState(327);
					expr(0);
					}
					break;
				}
				setState(334);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || ((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0) || _la==IDENTIFIER_ || _la==STRING_) {
					{
					setState(331);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(330);
						match(AS);
						}
					}

					setState(333);
					alias();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(336);
				qualifiedShorthand();
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

	public static class AliasContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING_() { return getToken(DMLStatementParser.STRING_, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_alias);
		try {
			setState(341);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(339);
				identifier();
				}
				break;
			case STRING_:
				enterOuterAlt(_localctx, 2);
				{
				setState(340);
				match(STRING_);
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

	public static class UnqualifiedShorthandContext extends ParserRuleContext {
		public TerminalNode ASTERISK_() { return getToken(DMLStatementParser.ASTERISK_, 0); }
		public UnqualifiedShorthandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unqualifiedShorthand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterUnqualifiedShorthand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitUnqualifiedShorthand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitUnqualifiedShorthand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnqualifiedShorthandContext unqualifiedShorthand() throws RecognitionException {
		UnqualifiedShorthandContext _localctx = new UnqualifiedShorthandContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_unqualifiedShorthand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(343);
			match(ASTERISK_);
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

	public static class QualifiedShorthandContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode DOT_ASTERISK_() { return getToken(DMLStatementParser.DOT_ASTERISK_, 0); }
		public QualifiedShorthandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedShorthand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterQualifiedShorthand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitQualifiedShorthand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitQualifiedShorthand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedShorthandContext qualifiedShorthand() throws RecognitionException {
		QualifiedShorthandContext _localctx = new QualifiedShorthandContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_qualifiedShorthand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(345);
			identifier();
			setState(346);
			match(DOT_ASTERISK_);
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
		public TerminalNode FROM() { return getToken(DMLStatementParser.FROM, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_fromClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			match(FROM);
			setState(349);
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

	public static class TableReferencesContext extends ParserRuleContext {
		public List<EscapedTableReferenceContext> escapedTableReference() {
			return getRuleContexts(EscapedTableReferenceContext.class);
		}
		public EscapedTableReferenceContext escapedTableReference(int i) {
			return getRuleContext(EscapedTableReferenceContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public TableReferencesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReferences; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterTableReferences(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitTableReferences(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitTableReferences(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableReferencesContext tableReferences() throws RecognitionException {
		TableReferencesContext _localctx = new TableReferencesContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_tableReferences);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(351);
			escapedTableReference();
			setState(356);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(352);
				match(COMMA_);
				setState(353);
				escapedTableReference();
				}
				}
				setState(358);
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

	public static class EscapedTableReferenceContext extends ParserRuleContext {
		public TableReferenceContext tableReference() {
			return getRuleContext(TableReferenceContext.class,0);
		}
		public TerminalNode LBE_() { return getToken(DMLStatementParser.LBE_, 0); }
		public TerminalNode RBE_() { return getToken(DMLStatementParser.RBE_, 0); }
		public EscapedTableReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_escapedTableReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterEscapedTableReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitEscapedTableReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitEscapedTableReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EscapedTableReferenceContext escapedTableReference() throws RecognitionException {
		EscapedTableReferenceContext _localctx = new EscapedTableReferenceContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_escapedTableReference);
		try {
			setState(364);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LP_:
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(359);
				tableReference();
				}
				break;
			case LBE_:
				enterOuterAlt(_localctx, 2);
				{
				setState(360);
				match(LBE_);
				setState(361);
				tableReference();
				setState(362);
				match(RBE_);
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

	public static class TableReferenceContext extends ParserRuleContext {
		public TableFactorContext tableFactor() {
			return getRuleContext(TableFactorContext.class,0);
		}
		public List<JoinedTableContext> joinedTable() {
			return getRuleContexts(JoinedTableContext.class);
		}
		public JoinedTableContext joinedTable(int i) {
			return getRuleContext(JoinedTableContext.class,i);
		}
		public TableReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterTableReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitTableReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitTableReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableReferenceContext tableReference() throws RecognitionException {
		TableReferenceContext _localctx = new TableReferenceContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_tableReference);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			tableFactor();
			setState(370);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (NATURAL - 79)) | (1L << (JOIN - 79)) | (1L << (INNER - 79)) | (1L << (LEFT - 79)) | (1L << (RIGHT - 79)) | (1L << (CROSS - 79)))) != 0)) {
				{
				{
				setState(367);
				joinedTable();
				}
				}
				setState(372);
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

	public static class TableFactorContext extends ParserRuleContext {
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(DMLStatementParser.AS, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public ColumnNamesContext columnNames() {
			return getRuleContext(ColumnNamesContext.class,0);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public TableFactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableFactor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterTableFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitTableFactor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitTableFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableFactorContext tableFactor() throws RecognitionException {
		TableFactorContext _localctx = new TableFactorContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_tableFactor);
		int _la;
		try {
			setState(392);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(373);
				tableName();
				setState(378);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || ((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0) || _la==IDENTIFIER_ || _la==STRING_) {
					{
					setState(375);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(374);
						match(AS);
						}
					}

					setState(377);
					alias();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(380);
				subquery();
				setState(382);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(381);
					match(AS);
					}
				}

				setState(384);
				alias();
				setState(386);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_ || ((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0) || _la==IDENTIFIER_) {
					{
					setState(385);
					columnNames();
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(388);
				match(LP_);
				setState(389);
				tableReferences();
				setState(390);
				match(RP_);
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

	public static class JoinedTableContext extends ParserRuleContext {
		public TableFactorContext tableFactor() {
			return getRuleContext(TableFactorContext.class,0);
		}
		public TerminalNode JOIN() { return getToken(DMLStatementParser.JOIN, 0); }
		public JoinSpecificationContext joinSpecification() {
			return getRuleContext(JoinSpecificationContext.class,0);
		}
		public TerminalNode INNER() { return getToken(DMLStatementParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(DMLStatementParser.CROSS, 0); }
		public TerminalNode LEFT() { return getToken(DMLStatementParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(DMLStatementParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(DMLStatementParser.OUTER, 0); }
		public TerminalNode NATURAL() { return getToken(DMLStatementParser.NATURAL, 0); }
		public JoinedTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinedTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterJoinedTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitJoinedTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitJoinedTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinedTableContext joinedTable() throws RecognitionException {
		JoinedTableContext _localctx = new JoinedTableContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_joinedTable);
		int _la;
		try {
			setState(419);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case JOIN:
			case INNER:
			case CROSS:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(395);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER || _la==CROSS) {
					{
					setState(394);
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

				setState(397);
				match(JOIN);
				}
				setState(399);
				tableFactor();
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING || _la==ON) {
					{
					setState(400);
					joinSpecification();
					}
				}

				}
				break;
			case LEFT:
			case RIGHT:
				enterOuterAlt(_localctx, 2);
				{
				setState(403);
				_la = _input.LA(1);
				if ( !(_la==LEFT || _la==RIGHT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(405);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(404);
					match(OUTER);
					}
				}

				setState(407);
				match(JOIN);
				setState(408);
				tableFactor();
				setState(409);
				joinSpecification();
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(411);
				match(NATURAL);
				setState(415);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case INNER:
					{
					setState(412);
					match(INNER);
					}
					break;
				case LEFT:
				case RIGHT:
					{
					setState(413);
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
					setState(414);
					match(OUTER);
					}
					}
					break;
				case JOIN:
					break;
				default:
					break;
				}
				setState(417);
				match(JOIN);
				setState(418);
				tableFactor();
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

	public static class JoinSpecificationContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(DMLStatementParser.ON, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode USING() { return getToken(DMLStatementParser.USING, 0); }
		public ColumnNamesContext columnNames() {
			return getRuleContext(ColumnNamesContext.class,0);
		}
		public JoinSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinSpecification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterJoinSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitJoinSpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitJoinSpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinSpecificationContext joinSpecification() throws RecognitionException {
		JoinSpecificationContext _localctx = new JoinSpecificationContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_joinSpecification);
		try {
			setState(425);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				setState(421);
				match(ON);
				setState(422);
				expr(0);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(423);
				match(USING);
				setState(424);
				columnNames();
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

	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(DMLStatementParser.WHERE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(427);
			match(WHERE);
			setState(428);
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
		public TerminalNode GROUP() { return getToken(DMLStatementParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(DMLStatementParser.BY, 0); }
		public List<OrderByItemContext> orderByItem() {
			return getRuleContexts(OrderByItemContext.class);
		}
		public OrderByItemContext orderByItem(int i) {
			return getRuleContext(OrderByItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_groupByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			match(GROUP);
			setState(431);
			match(BY);
			setState(432);
			orderByItem();
			setState(437);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(433);
				match(COMMA_);
				setState(434);
				orderByItem();
				}
				}
				setState(439);
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

	public static class HavingClauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(DMLStatementParser.HAVING, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitHavingClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitHavingClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(440);
			match(HAVING);
			setState(441);
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

	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode LIMIT() { return getToken(DMLStatementParser.LIMIT, 0); }
		public LimitRowCountContext limitRowCount() {
			return getRuleContext(LimitRowCountContext.class,0);
		}
		public TerminalNode OFFSET() { return getToken(DMLStatementParser.OFFSET, 0); }
		public LimitOffsetContext limitOffset() {
			return getRuleContext(LimitOffsetContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(DMLStatementParser.COMMA_, 0); }
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterLimitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitLimitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitLimitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
			match(LIMIT);
			setState(454);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				setState(447);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
				case 1:
					{
					setState(444);
					limitOffset();
					setState(445);
					match(COMMA_);
					}
					break;
				}
				setState(449);
				limitRowCount();
				}
				break;
			case 2:
				{
				setState(450);
				limitRowCount();
				setState(451);
				match(OFFSET);
				setState(452);
				limitOffset();
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

	public static class LimitRowCountContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public LimitRowCountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitRowCount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterLimitRowCount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitLimitRowCount(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitLimitRowCount(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitRowCountContext limitRowCount() throws RecognitionException {
		LimitRowCountContext _localctx = new LimitRowCountContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_limitRowCount);
		try {
			setState(458);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(456);
				numberLiterals();
				}
				break;
			case QUESTION_:
				enterOuterAlt(_localctx, 2);
				{
				setState(457);
				parameterMarker();
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

	public static class LimitOffsetContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public LimitOffsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitOffset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterLimitOffset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitLimitOffset(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitLimitOffset(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitOffsetContext limitOffset() throws RecognitionException {
		LimitOffsetContext _localctx = new LimitOffsetContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_limitOffset);
		try {
			setState(462);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(460);
				numberLiterals();
				}
				break;
			case QUESTION_:
				enterOuterAlt(_localctx, 2);
				{
				setState(461);
				parameterMarker();
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

	public static class SubqueryContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public UnionClauseContext unionClause() {
			return getRuleContext(UnionClauseContext.class,0);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSubquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(464);
			match(LP_);
			setState(465);
			unionClause();
			setState(466);
			match(RP_);
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

	public static class ParameterMarkerContext extends ParserRuleContext {
		public TerminalNode QUESTION_() { return getToken(DMLStatementParser.QUESTION_, 0); }
		public ParameterMarkerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterMarker; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterParameterMarker(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitParameterMarker(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitParameterMarker(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterMarkerContext parameterMarker() throws RecognitionException {
		ParameterMarkerContext _localctx = new ParameterMarkerContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_parameterMarker);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(468);
			match(QUESTION_);
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

	public static class LiteralsContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public DateTimeLiteralsContext dateTimeLiterals() {
			return getRuleContext(DateTimeLiteralsContext.class,0);
		}
		public HexadecimalLiteralsContext hexadecimalLiterals() {
			return getRuleContext(HexadecimalLiteralsContext.class,0);
		}
		public BitValueLiteralsContext bitValueLiterals() {
			return getRuleContext(BitValueLiteralsContext.class,0);
		}
		public BooleanLiteralsContext booleanLiterals() {
			return getRuleContext(BooleanLiteralsContext.class,0);
		}
		public NullValueLiteralsContext nullValueLiterals() {
			return getRuleContext(NullValueLiteralsContext.class,0);
		}
		public LiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralsContext literals() throws RecognitionException {
		LiteralsContext _localctx = new LiteralsContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_literals);
		try {
			setState(477);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(470);
				stringLiterals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(471);
				numberLiterals();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(472);
				dateTimeLiterals();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(473);
				hexadecimalLiterals();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(474);
				bitValueLiterals();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(475);
				booleanLiterals();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(476);
				nullValueLiterals();
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

	public static class StringLiteralsContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(DMLStatementParser.STRING_, 0); }
		public CharacterSetNameContext characterSetName() {
			return getRuleContext(CharacterSetNameContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public StringLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterStringLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitStringLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitStringLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralsContext stringLiterals() throws RecognitionException {
		StringLiteralsContext _localctx = new StringLiteralsContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_stringLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(480);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER_) {
				{
				setState(479);
				characterSetName();
				}
			}

			setState(482);
			match(STRING_);
			setState(484);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				{
				setState(483);
				collateClause();
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

	public static class NumberLiteralsContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(DMLStatementParser.NUMBER_, 0); }
		public TerminalNode MINUS_() { return getToken(DMLStatementParser.MINUS_, 0); }
		public NumberLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterNumberLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitNumberLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitNumberLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberLiteralsContext numberLiterals() throws RecognitionException {
		NumberLiteralsContext _localctx = new NumberLiteralsContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_numberLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(487);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS_) {
				{
				setState(486);
				match(MINUS_);
				}
			}

			setState(489);
			match(NUMBER_);
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

	public static class DateTimeLiteralsContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(DMLStatementParser.STRING_, 0); }
		public TerminalNode DATE() { return getToken(DMLStatementParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(DMLStatementParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(DMLStatementParser.TIMESTAMP, 0); }
		public TerminalNode LBE_() { return getToken(DMLStatementParser.LBE_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RBE_() { return getToken(DMLStatementParser.RBE_, 0); }
		public DateTimeLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDateTimeLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDateTimeLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDateTimeLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeLiteralsContext dateTimeLiterals() throws RecognitionException {
		DateTimeLiteralsContext _localctx = new DateTimeLiteralsContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_dateTimeLiterals);
		int _la;
		try {
			setState(498);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 1);
				{
				setState(491);
				_la = _input.LA(1);
				if ( !(((((_la - 127)) & ~0x3f) == 0 && ((1L << (_la - 127)) & ((1L << (DATE - 127)) | (1L << (TIME - 127)) | (1L << (TIMESTAMP - 127)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(492);
				match(STRING_);
				}
				break;
			case LBE_:
				enterOuterAlt(_localctx, 2);
				{
				setState(493);
				match(LBE_);
				setState(494);
				identifier();
				setState(495);
				match(STRING_);
				setState(496);
				match(RBE_);
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

	public static class HexadecimalLiteralsContext extends ParserRuleContext {
		public TerminalNode HEX_DIGIT_() { return getToken(DMLStatementParser.HEX_DIGIT_, 0); }
		public CharacterSetNameContext characterSetName() {
			return getRuleContext(CharacterSetNameContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public HexadecimalLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hexadecimalLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterHexadecimalLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitHexadecimalLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitHexadecimalLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HexadecimalLiteralsContext hexadecimalLiterals() throws RecognitionException {
		HexadecimalLiteralsContext _localctx = new HexadecimalLiteralsContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_hexadecimalLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(501);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER_) {
				{
				setState(500);
				characterSetName();
				}
			}

			setState(503);
			match(HEX_DIGIT_);
			setState(505);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(504);
				collateClause();
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

	public static class BitValueLiteralsContext extends ParserRuleContext {
		public TerminalNode BIT_NUM_() { return getToken(DMLStatementParser.BIT_NUM_, 0); }
		public CharacterSetNameContext characterSetName() {
			return getRuleContext(CharacterSetNameContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public BitValueLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitValueLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterBitValueLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitBitValueLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitBitValueLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitValueLiteralsContext bitValueLiterals() throws RecognitionException {
		BitValueLiteralsContext _localctx = new BitValueLiteralsContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_bitValueLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(508);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER_) {
				{
				setState(507);
				characterSetName();
				}
			}

			setState(510);
			match(BIT_NUM_);
			setState(512);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(511);
				collateClause();
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

	public static class BooleanLiteralsContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(DMLStatementParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(DMLStatementParser.FALSE, 0); }
		public BooleanLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterBooleanLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitBooleanLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitBooleanLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralsContext booleanLiterals() throws RecognitionException {
		BooleanLiteralsContext _localctx = new BooleanLiteralsContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_booleanLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(514);
			_la = _input.LA(1);
			if ( !(_la==TRUE || _la==FALSE) ) {
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

	public static class NullValueLiteralsContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(DMLStatementParser.NULL, 0); }
		public NullValueLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullValueLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterNullValueLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitNullValueLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitNullValueLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullValueLiteralsContext nullValueLiterals() throws RecognitionException {
		NullValueLiteralsContext _localctx = new NullValueLiteralsContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_nullValueLiterals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			match(NULL);
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

	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(DMLStatementParser.IDENTIFIER_, 0); }
		public UnreservedWordContext unreservedWord() {
			return getRuleContext(UnreservedWordContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_identifier);
		try {
			setState(520);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(518);
				match(IDENTIFIER_);
				}
				break;
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
				enterOuterAlt(_localctx, 2);
				{
				setState(519);
				unreservedWord();
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

	public static class UnreservedWordContext extends ParserRuleContext {
		public TerminalNode ADA() { return getToken(DMLStatementParser.ADA, 0); }
		public TerminalNode C92() { return getToken(DMLStatementParser.C92, 0); }
		public TerminalNode CATALOG_NAME() { return getToken(DMLStatementParser.CATALOG_NAME, 0); }
		public TerminalNode CHARACTER_SET_CATALOG() { return getToken(DMLStatementParser.CHARACTER_SET_CATALOG, 0); }
		public TerminalNode CHARACTER_SET_NAME() { return getToken(DMLStatementParser.CHARACTER_SET_NAME, 0); }
		public TerminalNode CHARACTER_SET_SCHEMA() { return getToken(DMLStatementParser.CHARACTER_SET_SCHEMA, 0); }
		public TerminalNode CLASS_ORIGIN() { return getToken(DMLStatementParser.CLASS_ORIGIN, 0); }
		public TerminalNode COBOL() { return getToken(DMLStatementParser.COBOL, 0); }
		public TerminalNode COLLATION_CATALOG() { return getToken(DMLStatementParser.COLLATION_CATALOG, 0); }
		public TerminalNode COLLATION_NAME() { return getToken(DMLStatementParser.COLLATION_NAME, 0); }
		public TerminalNode COLLATION_SCHEMA() { return getToken(DMLStatementParser.COLLATION_SCHEMA, 0); }
		public TerminalNode COLUMN_NAME() { return getToken(DMLStatementParser.COLUMN_NAME, 0); }
		public TerminalNode COMMAND_FUNCTION() { return getToken(DMLStatementParser.COMMAND_FUNCTION, 0); }
		public TerminalNode COMMITTED() { return getToken(DMLStatementParser.COMMITTED, 0); }
		public TerminalNode CONDITION_NUMBER() { return getToken(DMLStatementParser.CONDITION_NUMBER, 0); }
		public TerminalNode CONNECTION_NAME() { return getToken(DMLStatementParser.CONNECTION_NAME, 0); }
		public TerminalNode CONSTRAINT_CATALOG() { return getToken(DMLStatementParser.CONSTRAINT_CATALOG, 0); }
		public TerminalNode CONSTRAINT_NAME() { return getToken(DMLStatementParser.CONSTRAINT_NAME, 0); }
		public TerminalNode CONSTRAINT_SCHEMA() { return getToken(DMLStatementParser.CONSTRAINT_SCHEMA, 0); }
		public TerminalNode CURSOR_NAME() { return getToken(DMLStatementParser.CURSOR_NAME, 0); }
		public TerminalNode DATA() { return getToken(DMLStatementParser.DATA, 0); }
		public TerminalNode DATETIME_INTERVAL_CODE() { return getToken(DMLStatementParser.DATETIME_INTERVAL_CODE, 0); }
		public TerminalNode DATETIME_INTERVAL_PRECISION() { return getToken(DMLStatementParser.DATETIME_INTERVAL_PRECISION, 0); }
		public TerminalNode DYNAMIC_FUNCTION() { return getToken(DMLStatementParser.DYNAMIC_FUNCTION, 0); }
		public TerminalNode FORTRAN() { return getToken(DMLStatementParser.FORTRAN, 0); }
		public TerminalNode LENGTH() { return getToken(DMLStatementParser.LENGTH, 0); }
		public TerminalNode MESSAGE_LENGTH() { return getToken(DMLStatementParser.MESSAGE_LENGTH, 0); }
		public TerminalNode MESSAGE_OCTET_LENGTH() { return getToken(DMLStatementParser.MESSAGE_OCTET_LENGTH, 0); }
		public TerminalNode MESSAGE_TEXT() { return getToken(DMLStatementParser.MESSAGE_TEXT, 0); }
		public TerminalNode MORE92() { return getToken(DMLStatementParser.MORE92, 0); }
		public TerminalNode MUMPS() { return getToken(DMLStatementParser.MUMPS, 0); }
		public TerminalNode NAME() { return getToken(DMLStatementParser.NAME, 0); }
		public TerminalNode NULLABLE() { return getToken(DMLStatementParser.NULLABLE, 0); }
		public TerminalNode NUMBER() { return getToken(DMLStatementParser.NUMBER, 0); }
		public TerminalNode PASCAL() { return getToken(DMLStatementParser.PASCAL, 0); }
		public TerminalNode PLI() { return getToken(DMLStatementParser.PLI, 0); }
		public TerminalNode REPEATABLE() { return getToken(DMLStatementParser.REPEATABLE, 0); }
		public TerminalNode RETURNED_LENGTH() { return getToken(DMLStatementParser.RETURNED_LENGTH, 0); }
		public TerminalNode RETURNED_OCTET_LENGTH() { return getToken(DMLStatementParser.RETURNED_OCTET_LENGTH, 0); }
		public TerminalNode RETURNED_SQLSTATE() { return getToken(DMLStatementParser.RETURNED_SQLSTATE, 0); }
		public TerminalNode ROW_COUNT() { return getToken(DMLStatementParser.ROW_COUNT, 0); }
		public TerminalNode SCALE() { return getToken(DMLStatementParser.SCALE, 0); }
		public TerminalNode SCHEMA_NAME() { return getToken(DMLStatementParser.SCHEMA_NAME, 0); }
		public TerminalNode SERIALIZABLE() { return getToken(DMLStatementParser.SERIALIZABLE, 0); }
		public TerminalNode SERVER_NAME() { return getToken(DMLStatementParser.SERVER_NAME, 0); }
		public TerminalNode SUBCLASS_ORIGIN() { return getToken(DMLStatementParser.SUBCLASS_ORIGIN, 0); }
		public TerminalNode TABLE_NAME() { return getToken(DMLStatementParser.TABLE_NAME, 0); }
		public TerminalNode TYPE() { return getToken(DMLStatementParser.TYPE, 0); }
		public TerminalNode UNCOMMITTED() { return getToken(DMLStatementParser.UNCOMMITTED, 0); }
		public TerminalNode UNNAMED() { return getToken(DMLStatementParser.UNNAMED, 0); }
		public UnreservedWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unreservedWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterUnreservedWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitUnreservedWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitUnreservedWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnreservedWordContext unreservedWord() throws RecognitionException {
		UnreservedWordContext _localctx = new UnreservedWordContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_unreservedWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			_la = _input.LA(1);
			if ( !(((((_la - 162)) & ~0x3f) == 0 && ((1L << (_la - 162)) & ((1L << (NAME - 162)) | (1L << (TYPE - 162)) | (1L << (ADA - 162)) | (1L << (C92 - 162)) | (1L << (CATALOG_NAME - 162)) | (1L << (CHARACTER_SET_CATALOG - 162)) | (1L << (CHARACTER_SET_NAME - 162)) | (1L << (CHARACTER_SET_SCHEMA - 162)) | (1L << (CLASS_ORIGIN - 162)) | (1L << (COBOL - 162)) | (1L << (COLLATION_CATALOG - 162)) | (1L << (COLLATION_NAME - 162)) | (1L << (COLLATION_SCHEMA - 162)) | (1L << (COLUMN_NAME - 162)) | (1L << (COMMAND_FUNCTION - 162)) | (1L << (COMMITTED - 162)) | (1L << (CONDITION_NUMBER - 162)) | (1L << (CONNECTION_NAME - 162)) | (1L << (CONSTRAINT_CATALOG - 162)) | (1L << (CONSTRAINT_NAME - 162)) | (1L << (CONSTRAINT_SCHEMA - 162)) | (1L << (CURSOR_NAME - 162)) | (1L << (DATA - 162)) | (1L << (DATETIME_INTERVAL_CODE - 162)) | (1L << (DATETIME_INTERVAL_PRECISION - 162)) | (1L << (DYNAMIC_FUNCTION - 162)) | (1L << (FORTRAN - 162)) | (1L << (LENGTH - 162)) | (1L << (MESSAGE_LENGTH - 162)) | (1L << (MESSAGE_OCTET_LENGTH - 162)) | (1L << (MESSAGE_TEXT - 162)) | (1L << (MORE92 - 162)) | (1L << (MUMPS - 162)) | (1L << (NULLABLE - 162)) | (1L << (NUMBER - 162)) | (1L << (PASCAL - 162)) | (1L << (PLI - 162)) | (1L << (REPEATABLE - 162)) | (1L << (RETURNED_LENGTH - 162)) | (1L << (RETURNED_OCTET_LENGTH - 162)) | (1L << (RETURNED_SQLSTATE - 162)) | (1L << (ROW_COUNT - 162)) | (1L << (SCALE - 162)) | (1L << (SCHEMA_NAME - 162)) | (1L << (SERIALIZABLE - 162)) | (1L << (SERVER_NAME - 162)) | (1L << (SUBCLASS_ORIGIN - 162)) | (1L << (TABLE_NAME - 162)) | (1L << (UNCOMMITTED - 162)) | (1L << (UNNAMED - 162)))) != 0)) ) {
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

	public static class VariableContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TerminalNode> AT_() { return getTokens(DMLStatementParser.AT_); }
		public TerminalNode AT_(int i) {
			return getToken(DMLStatementParser.AT_, i);
		}
		public TerminalNode DOT_() { return getToken(DMLStatementParser.DOT_, 0); }
		public TerminalNode GLOBAL() { return getToken(DMLStatementParser.GLOBAL, 0); }
		public TerminalNode LOCAL() { return getToken(DMLStatementParser.LOCAL, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_variable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT_) {
				{
				setState(525);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
				case 1:
					{
					setState(524);
					match(AT_);
					}
					break;
				}
				setState(527);
				match(AT_);
				}
			}

			setState(531);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LOCAL || _la==GLOBAL) {
				{
				setState(530);
				_la = _input.LA(1);
				if ( !(_la==LOCAL || _la==GLOBAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(534);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT_) {
				{
				setState(533);
				match(DOT_);
				}
			}

			setState(536);
			identifier();
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
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SchemaNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSchemaName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSchemaName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSchemaName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(538);
			identifier();
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
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(DMLStatementParser.DOT_, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(540);
				owner();
				setState(541);
				match(DOT_);
				}
				break;
			}
			setState(545);
			name();
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
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(DMLStatementParser.DOT_, 0); }
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(550);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				{
				setState(547);
				owner();
				setState(548);
				match(DOT_);
				}
				break;
			}
			setState(552);
			name();
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

	public static class ViewNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(DMLStatementParser.DOT_, 0); }
		public ViewNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_viewName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterViewName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitViewName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitViewName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ViewNameContext viewName() throws RecognitionException {
		ViewNameContext _localctx = new ViewNameContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_viewName);
		try {
			setState(561);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(554);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(558);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
				case 1:
					{
					setState(555);
					owner();
					setState(556);
					match(DOT_);
					}
					break;
				}
				setState(560);
				identifier();
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

	public static class OwnerContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public OwnerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_owner; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterOwner(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitOwner(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitOwner(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OwnerContext owner() throws RecognitionException {
		OwnerContext _localctx = new OwnerContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_owner);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			identifier();
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

	public static class NameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(565);
			identifier();
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

	public static class ColumnNamesContext extends ParserRuleContext {
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public ColumnNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnNames; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterColumnNames(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitColumnNames(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitColumnNames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNamesContext columnNames() throws RecognitionException {
		ColumnNamesContext _localctx = new ColumnNamesContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_columnNames);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(568);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(567);
				match(LP_);
				}
			}

			setState(570);
			columnName();
			setState(575);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(571);
					match(COMMA_);
					setState(572);
					columnName();
					}
					} 
				}
				setState(577);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			}
			setState(579);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				{
				setState(578);
				match(RP_);
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

	public static class TableNamesContext extends ParserRuleContext {
		public List<TableNameContext> tableName() {
			return getRuleContexts(TableNameContext.class);
		}
		public TableNameContext tableName(int i) {
			return getRuleContext(TableNameContext.class,i);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public TableNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableNames; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterTableNames(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitTableNames(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitTableNames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNamesContext tableNames() throws RecognitionException {
		TableNamesContext _localctx = new TableNamesContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_tableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(582);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(581);
				match(LP_);
				}
			}

			setState(584);
			tableName();
			setState(589);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(585);
				match(COMMA_);
				setState(586);
				tableName();
				}
				}
				setState(591);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(593);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RP_) {
				{
				setState(592);
				match(RP_);
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

	public static class CharacterSetNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(DMLStatementParser.IDENTIFIER_, 0); }
		public CharacterSetNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSetName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCharacterSetName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCharacterSetName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCharacterSetName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterSetNameContext characterSetName() throws RecognitionException {
		CharacterSetNameContext _localctx = new CharacterSetNameContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_characterSetName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(595);
			match(IDENTIFIER_);
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
		public NotOperatorContext notOperator() {
			return getRuleContext(NotOperatorContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public LogicalOperatorContext logicalOperator() {
			return getRuleContext(LogicalOperatorContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 112;
		enterRecursionRule(_localctx, 112, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(606);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				{
				setState(598);
				notOperator();
				setState(599);
				expr(3);
				}
				break;
			case 2:
				{
				setState(601);
				match(LP_);
				setState(602);
				expr(0);
				setState(603);
				match(RP_);
				}
				break;
			case 3:
				{
				setState(605);
				booleanPrimary(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(614);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(608);
					if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
					setState(609);
					logicalOperator();
					setState(610);
					expr(5);
					}
					} 
				}
				setState(616);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
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

	public static class LogicalOperatorContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(DMLStatementParser.OR, 0); }
		public TerminalNode AND() { return getToken(DMLStatementParser.AND, 0); }
		public TerminalNode AND_() { return getToken(DMLStatementParser.AND_, 0); }
		public LogicalOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterLogicalOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitLogicalOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitLogicalOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOperatorContext logicalOperator() throws RecognitionException {
		LogicalOperatorContext _localctx = new LogicalOperatorContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_logicalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(617);
			_la = _input.LA(1);
			if ( !(_la==AND_ || _la==AND || _la==OR) ) {
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

	public static class NotOperatorContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(DMLStatementParser.NOT, 0); }
		public TerminalNode NOT_() { return getToken(DMLStatementParser.NOT_, 0); }
		public NotOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterNotOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitNotOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitNotOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotOperatorContext notOperator() throws RecognitionException {
		NotOperatorContext _localctx = new NotOperatorContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_notOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(619);
			_la = _input.LA(1);
			if ( !(_la==NOT_ || _la==NOT) ) {
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

	public static class BooleanPrimaryContext extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public BooleanPrimaryContext booleanPrimary() {
			return getRuleContext(BooleanPrimaryContext.class,0);
		}
		public TerminalNode IS() { return getToken(DMLStatementParser.IS, 0); }
		public TerminalNode TRUE() { return getToken(DMLStatementParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(DMLStatementParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(DMLStatementParser.UNKNOWN, 0); }
		public TerminalNode NULL() { return getToken(DMLStatementParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(DMLStatementParser.NOT, 0); }
		public TerminalNode SAFE_EQ_() { return getToken(DMLStatementParser.SAFE_EQ_, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode ALL() { return getToken(DMLStatementParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(DMLStatementParser.ANY, 0); }
		public BooleanPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanPrimary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterBooleanPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitBooleanPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitBooleanPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanPrimaryContext booleanPrimary() throws RecognitionException {
		return booleanPrimary(0);
	}

	private BooleanPrimaryContext booleanPrimary(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanPrimaryContext _localctx = new BooleanPrimaryContext(_ctx, _parentState);
		BooleanPrimaryContext _prevctx = _localctx;
		int _startState = 118;
		enterRecursionRule(_localctx, 118, RULE_booleanPrimary, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(622);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(644);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(642);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
					case 1:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(624);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(625);
						match(IS);
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
						_la = _input.LA(1);
						if ( !(((((_la - 100)) & ~0x3f) == 0 && ((1L << (_la - 100)) & ((1L << (NULL - 100)) | (1L << (TRUE - 100)) | (1L << (FALSE - 100)))) != 0) || _la==UNKNOWN) ) {
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
						setState(630);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(631);
						match(SAFE_EQ_);
						setState(632);
						predicate();
						}
						break;
					case 3:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(633);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(634);
						comparisonOperator();
						setState(635);
						predicate();
						}
						break;
					case 4:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(637);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(638);
						comparisonOperator();
						setState(639);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(640);
						subquery();
						}
						break;
					}
					} 
				}
				setState(646);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
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
		public TerminalNode EQ_() { return getToken(DMLStatementParser.EQ_, 0); }
		public TerminalNode GTE_() { return getToken(DMLStatementParser.GTE_, 0); }
		public TerminalNode GT_() { return getToken(DMLStatementParser.GT_, 0); }
		public TerminalNode LTE_() { return getToken(DMLStatementParser.LTE_, 0); }
		public TerminalNode LT_() { return getToken(DMLStatementParser.LT_, 0); }
		public TerminalNode NEQ_() { return getToken(DMLStatementParser.NEQ_, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ_) | (1L << NEQ_) | (1L << GT_) | (1L << GTE_) | (1L << LT_) | (1L << LTE_))) != 0)) ) {
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
		public List<BitExprContext> bitExpr() {
			return getRuleContexts(BitExprContext.class);
		}
		public BitExprContext bitExpr(int i) {
			return getRuleContext(BitExprContext.class,i);
		}
		public TerminalNode IN() { return getToken(DMLStatementParser.IN, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode NOT() { return getToken(DMLStatementParser.NOT, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public TerminalNode BETWEEN() { return getToken(DMLStatementParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(DMLStatementParser.AND, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode LIKE() { return getToken(DMLStatementParser.LIKE, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode ESCAPE() { return getToken(DMLStatementParser.ESCAPE, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_predicate);
		int _la;
		try {
			setState(692);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(649);
				bitExpr(0);
				setState(651);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(650);
					match(NOT);
					}
				}

				setState(653);
				match(IN);
				setState(654);
				subquery();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(656);
				bitExpr(0);
				setState(658);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(657);
					match(NOT);
					}
				}

				setState(660);
				match(IN);
				setState(661);
				match(LP_);
				setState(662);
				expr(0);
				setState(667);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(663);
					match(COMMA_);
					setState(664);
					expr(0);
					}
					}
					setState(669);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(670);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(672);
				bitExpr(0);
				setState(674);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(673);
					match(NOT);
					}
				}

				setState(676);
				match(BETWEEN);
				setState(677);
				bitExpr(0);
				setState(678);
				match(AND);
				setState(679);
				predicate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(681);
				bitExpr(0);
				setState(683);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(682);
					match(NOT);
					}
				}

				setState(685);
				match(LIKE);
				setState(686);
				simpleExpr(0);
				setState(689);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
				case 1:
					{
					setState(687);
					match(ESCAPE);
					setState(688);
					simpleExpr(0);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(691);
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
		public TerminalNode VERTICAL_BAR_() { return getToken(DMLStatementParser.VERTICAL_BAR_, 0); }
		public TerminalNode AMPERSAND_() { return getToken(DMLStatementParser.AMPERSAND_, 0); }
		public TerminalNode SIGNED_LEFT_SHIFT_() { return getToken(DMLStatementParser.SIGNED_LEFT_SHIFT_, 0); }
		public TerminalNode SIGNED_RIGHT_SHIFT_() { return getToken(DMLStatementParser.SIGNED_RIGHT_SHIFT_, 0); }
		public TerminalNode PLUS_() { return getToken(DMLStatementParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(DMLStatementParser.MINUS_, 0); }
		public TerminalNode ASTERISK_() { return getToken(DMLStatementParser.ASTERISK_, 0); }
		public TerminalNode SLASH_() { return getToken(DMLStatementParser.SLASH_, 0); }
		public TerminalNode MOD_() { return getToken(DMLStatementParser.MOD_, 0); }
		public TerminalNode CARET_() { return getToken(DMLStatementParser.CARET_, 0); }
		public IntervalExpressionContext intervalExpression() {
			return getRuleContext(IntervalExpressionContext.class,0);
		}
		public BitExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterBitExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitBitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitBitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitExprContext bitExpr() throws RecognitionException {
		return bitExpr(0);
	}

	private BitExprContext bitExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BitExprContext _localctx = new BitExprContext(_ctx, _parentState);
		BitExprContext _prevctx = _localctx;
		int _startState = 124;
		enterRecursionRule(_localctx, 124, RULE_bitExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(695);
			simpleExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(735);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(733);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
					case 1:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(697);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(698);
						match(VERTICAL_BAR_);
						setState(699);
						bitExpr(14);
						}
						break;
					case 2:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(700);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(701);
						match(AMPERSAND_);
						setState(702);
						bitExpr(13);
						}
						break;
					case 3:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(703);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(704);
						match(SIGNED_LEFT_SHIFT_);
						setState(705);
						bitExpr(12);
						}
						break;
					case 4:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(706);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(707);
						match(SIGNED_RIGHT_SHIFT_);
						setState(708);
						bitExpr(11);
						}
						break;
					case 5:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(709);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(710);
						match(PLUS_);
						setState(711);
						bitExpr(10);
						}
						break;
					case 6:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(712);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(713);
						match(MINUS_);
						setState(714);
						bitExpr(9);
						}
						break;
					case 7:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(715);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(716);
						match(ASTERISK_);
						setState(717);
						bitExpr(8);
						}
						break;
					case 8:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(718);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(719);
						match(SLASH_);
						setState(720);
						bitExpr(7);
						}
						break;
					case 9:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(721);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(722);
						match(MOD_);
						setState(723);
						bitExpr(6);
						}
						break;
					case 10:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(724);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(725);
						match(CARET_);
						setState(726);
						bitExpr(5);
						}
						break;
					case 11:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(727);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(728);
						match(PLUS_);
						setState(729);
						intervalExpression();
						}
						break;
					case 12:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(730);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(731);
						match(MINUS_);
						setState(732);
						intervalExpression();
						}
						break;
					}
					} 
				}
				setState(737);
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

	public static class SimpleExprContext extends ParserRuleContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public LiteralsContext literals() {
			return getRuleContext(LiteralsContext.class,0);
		}
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public TerminalNode PLUS_() { return getToken(DMLStatementParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(DMLStatementParser.MINUS_, 0); }
		public TerminalNode TILDE_() { return getToken(DMLStatementParser.TILDE_, 0); }
		public TerminalNode NOT_() { return getToken(DMLStatementParser.NOT_, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(DMLStatementParser.EXISTS, 0); }
		public TerminalNode LBE_() { return getToken(DMLStatementParser.LBE_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RBE_() { return getToken(DMLStatementParser.RBE_, 0); }
		public MatchExpressionContext matchExpression() {
			return getRuleContext(MatchExpressionContext.class,0);
		}
		public CaseExpressionContext caseExpression() {
			return getRuleContext(CaseExpressionContext.class,0);
		}
		public IntervalExpressionContext intervalExpression() {
			return getRuleContext(IntervalExpressionContext.class,0);
		}
		public TerminalNode COLLATE() { return getToken(DMLStatementParser.COLLATE, 0); }
		public TerminalNode STRING_() { return getToken(DMLStatementParser.STRING_, 0); }
		public SimpleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSimpleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSimpleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSimpleExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleExprContext simpleExpr() throws RecognitionException {
		return simpleExpr(0);
	}

	private SimpleExprContext simpleExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		SimpleExprContext _localctx = new SimpleExprContext(_ctx, _parentState);
		SimpleExprContext _prevctx = _localctx;
		int _startState = 126;
		enterRecursionRule(_localctx, 126, RULE_simpleExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(769);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				{
				setState(739);
				functionCall();
				}
				break;
			case 2:
				{
				setState(740);
				parameterMarker();
				}
				break;
			case 3:
				{
				setState(741);
				literals();
				}
				break;
			case 4:
				{
				setState(742);
				columnName();
				}
				break;
			case 5:
				{
				setState(743);
				variable();
				}
				break;
			case 6:
				{
				setState(744);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(745);
				simpleExpr(7);
				}
				break;
			case 7:
				{
				setState(746);
				match(LP_);
				setState(747);
				expr(0);
				setState(752);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(748);
					match(COMMA_);
					setState(749);
					expr(0);
					}
					}
					setState(754);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(755);
				match(RP_);
				}
				break;
			case 8:
				{
				setState(758);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXISTS) {
					{
					setState(757);
					match(EXISTS);
					}
				}

				setState(760);
				subquery();
				}
				break;
			case 9:
				{
				setState(761);
				match(LBE_);
				setState(762);
				identifier();
				setState(763);
				expr(0);
				setState(764);
				match(RBE_);
				}
				break;
			case 10:
				{
				setState(766);
				matchExpression();
				}
				break;
			case 11:
				{
				setState(767);
				caseExpression();
				}
				break;
			case 12:
				{
				setState(768);
				intervalExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(779);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new SimpleExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
					setState(771);
					if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
					setState(772);
					match(COLLATE);
					setState(775);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case STRING_:
						{
						setState(773);
						match(STRING_);
						}
						break;
					case NAME:
					case TYPE:
					case ADA:
					case C92:
					case CATALOG_NAME:
					case CHARACTER_SET_CATALOG:
					case CHARACTER_SET_NAME:
					case CHARACTER_SET_SCHEMA:
					case CLASS_ORIGIN:
					case COBOL:
					case COLLATION_CATALOG:
					case COLLATION_NAME:
					case COLLATION_SCHEMA:
					case COLUMN_NAME:
					case COMMAND_FUNCTION:
					case COMMITTED:
					case CONDITION_NUMBER:
					case CONNECTION_NAME:
					case CONSTRAINT_CATALOG:
					case CONSTRAINT_NAME:
					case CONSTRAINT_SCHEMA:
					case CURSOR_NAME:
					case DATA:
					case DATETIME_INTERVAL_CODE:
					case DATETIME_INTERVAL_PRECISION:
					case DYNAMIC_FUNCTION:
					case FORTRAN:
					case LENGTH:
					case MESSAGE_LENGTH:
					case MESSAGE_OCTET_LENGTH:
					case MESSAGE_TEXT:
					case MORE92:
					case MUMPS:
					case NULLABLE:
					case NUMBER:
					case PASCAL:
					case PLI:
					case REPEATABLE:
					case RETURNED_LENGTH:
					case RETURNED_OCTET_LENGTH:
					case RETURNED_SQLSTATE:
					case ROW_COUNT:
					case SCALE:
					case SCHEMA_NAME:
					case SERIALIZABLE:
					case SERVER_NAME:
					case SUBCLASS_ORIGIN:
					case TABLE_NAME:
					case UNCOMMITTED:
					case UNNAMED:
					case IDENTIFIER_:
						{
						setState(774);
						identifier();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					} 
				}
				setState(781);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
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

	public static class FunctionCallContext extends ParserRuleContext {
		public AggregationFunctionContext aggregationFunction() {
			return getRuleContext(AggregationFunctionContext.class,0);
		}
		public SpecialFunctionContext specialFunction() {
			return getRuleContext(SpecialFunctionContext.class,0);
		}
		public RegularFunctionContext regularFunction() {
			return getRuleContext(RegularFunctionContext.class,0);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_functionCall);
		try {
			setState(785);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
				enterOuterAlt(_localctx, 1);
				{
				setState(782);
				aggregationFunction();
				}
				break;
			case POSITION:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case CONVERT:
			case EXTRACT:
				enterOuterAlt(_localctx, 2);
				{
				setState(783);
				specialFunction();
				}
				break;
			case IF:
			case INTERVAL:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case CURRENT_TIMESTAMP:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 3);
				{
				setState(784);
				regularFunction();
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

	public static class AggregationFunctionContext extends ParserRuleContext {
		public AggregationFunctionNameContext aggregationFunctionName() {
			return getRuleContext(AggregationFunctionNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public DistinctContext distinct() {
			return getRuleContext(DistinctContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(DMLStatementParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public AggregationFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterAggregationFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitAggregationFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitAggregationFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionContext aggregationFunction() throws RecognitionException {
		AggregationFunctionContext _localctx = new AggregationFunctionContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_aggregationFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(787);
			aggregationFunctionName();
			setState(788);
			match(LP_);
			setState(790);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DISTINCT) {
				{
				setState(789);
				distinct();
				}
			}

			setState(801);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case POSITION:
			case CASE:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case LOCAL:
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case CONVERT:
			case CURRENT_TIMESTAMP:
			case EXTRACT:
			case GLOBAL:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(792);
				expr(0);
				setState(797);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(793);
					match(COMMA_);
					setState(794);
					expr(0);
					}
					}
					setState(799);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(800);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(803);
			match(RP_);
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

	public static class AggregationFunctionNameContext extends ParserRuleContext {
		public TerminalNode MAX() { return getToken(DMLStatementParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(DMLStatementParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(DMLStatementParser.SUM, 0); }
		public TerminalNode COUNT() { return getToken(DMLStatementParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(DMLStatementParser.AVG, 0); }
		public AggregationFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunctionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterAggregationFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitAggregationFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitAggregationFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionNameContext aggregationFunctionName() throws RecognitionException {
		AggregationFunctionNameContext _localctx = new AggregationFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_aggregationFunctionName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(805);
			_la = _input.LA(1);
			if ( !(((((_la - 141)) & ~0x3f) == 0 && ((1L << (_la - 141)) & ((1L << (MAX - 141)) | (1L << (MIN - 141)) | (1L << (SUM - 141)) | (1L << (COUNT - 141)) | (1L << (AVG - 141)))) != 0)) ) {
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

	public static class DistinctContext extends ParserRuleContext {
		public TerminalNode DISTINCT() { return getToken(DMLStatementParser.DISTINCT, 0); }
		public DistinctContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distinct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDistinct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDistinct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDistinct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistinctContext distinct() throws RecognitionException {
		DistinctContext _localctx = new DistinctContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_distinct);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(807);
			match(DISTINCT);
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

	public static class SpecialFunctionContext extends ParserRuleContext {
		public CastFunctionContext castFunction() {
			return getRuleContext(CastFunctionContext.class,0);
		}
		public ConvertFunctionContext convertFunction() {
			return getRuleContext(ConvertFunctionContext.class,0);
		}
		public PositionFunctionContext positionFunction() {
			return getRuleContext(PositionFunctionContext.class,0);
		}
		public SubstringFunctionContext substringFunction() {
			return getRuleContext(SubstringFunctionContext.class,0);
		}
		public ExtractFunctionContext extractFunction() {
			return getRuleContext(ExtractFunctionContext.class,0);
		}
		public TrimFunctionContext trimFunction() {
			return getRuleContext(TrimFunctionContext.class,0);
		}
		public SpecialFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSpecialFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSpecialFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSpecialFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialFunctionContext specialFunction() throws RecognitionException {
		SpecialFunctionContext _localctx = new SpecialFunctionContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_specialFunction);
		try {
			setState(815);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CAST:
				enterOuterAlt(_localctx, 1);
				{
				setState(809);
				castFunction();
				}
				break;
			case CONVERT:
				enterOuterAlt(_localctx, 2);
				{
				setState(810);
				convertFunction();
				}
				break;
			case POSITION:
				enterOuterAlt(_localctx, 3);
				{
				setState(811);
				positionFunction();
				}
				break;
			case SUBSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(812);
				substringFunction();
				}
				break;
			case EXTRACT:
				enterOuterAlt(_localctx, 5);
				{
				setState(813);
				extractFunction();
				}
				break;
			case TRIM:
				enterOuterAlt(_localctx, 6);
				{
				setState(814);
				trimFunction();
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

	public static class CastFunctionContext extends ParserRuleContext {
		public TerminalNode CAST() { return getToken(DMLStatementParser.CAST, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public TerminalNode AS() { return getToken(DMLStatementParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode NULL() { return getToken(DMLStatementParser.NULL, 0); }
		public CastFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCastFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCastFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCastFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastFunctionContext castFunction() throws RecognitionException {
		CastFunctionContext _localctx = new CastFunctionContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_castFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(817);
			match(CAST);
			setState(818);
			match(LP_);
			setState(821);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(819);
				expr(0);
				}
				break;
			case 2:
				{
				setState(820);
				match(NULL);
				}
				break;
			}
			setState(823);
			match(AS);
			setState(824);
			dataType();
			setState(825);
			match(RP_);
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

	public static class ConvertFunctionContext extends ParserRuleContext {
		public TerminalNode CONVERT() { return getToken(DMLStatementParser.CONVERT, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode USING() { return getToken(DMLStatementParser.USING, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public ConvertFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_convertFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterConvertFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitConvertFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitConvertFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConvertFunctionContext convertFunction() throws RecognitionException {
		ConvertFunctionContext _localctx = new ConvertFunctionContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_convertFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(827);
			match(CONVERT);
			setState(828);
			match(LP_);
			setState(829);
			expr(0);
			setState(830);
			match(USING);
			setState(831);
			identifier();
			setState(832);
			match(RP_);
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

	public static class PositionFunctionContext extends ParserRuleContext {
		public TerminalNode POSITION() { return getToken(DMLStatementParser.POSITION, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode IN() { return getToken(DMLStatementParser.IN, 0); }
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public PositionFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positionFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterPositionFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitPositionFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitPositionFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PositionFunctionContext positionFunction() throws RecognitionException {
		PositionFunctionContext _localctx = new PositionFunctionContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_positionFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(834);
			match(POSITION);
			setState(835);
			match(LP_);
			setState(836);
			expr(0);
			setState(837);
			match(IN);
			setState(838);
			expr(0);
			setState(839);
			match(RP_);
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

	public static class SubstringFunctionContext extends ParserRuleContext {
		public TerminalNode SUBSTRING() { return getToken(DMLStatementParser.SUBSTRING, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode FROM() { return getToken(DMLStatementParser.FROM, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(DMLStatementParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(DMLStatementParser.NUMBER_, i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public TerminalNode FOR() { return getToken(DMLStatementParser.FOR, 0); }
		public SubstringFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substringFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterSubstringFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitSubstringFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitSubstringFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubstringFunctionContext substringFunction() throws RecognitionException {
		SubstringFunctionContext _localctx = new SubstringFunctionContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_substringFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(841);
			match(SUBSTRING);
			setState(842);
			match(LP_);
			setState(843);
			expr(0);
			setState(844);
			match(FROM);
			setState(845);
			match(NUMBER_);
			setState(848);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FOR) {
				{
				setState(846);
				match(FOR);
				setState(847);
				match(NUMBER_);
				}
			}

			setState(850);
			match(RP_);
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

	public static class ExtractFunctionContext extends ParserRuleContext {
		public TerminalNode EXTRACT() { return getToken(DMLStatementParser.EXTRACT, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(DMLStatementParser.FROM, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public ExtractFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extractFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterExtractFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitExtractFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitExtractFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtractFunctionContext extractFunction() throws RecognitionException {
		ExtractFunctionContext _localctx = new ExtractFunctionContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_extractFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(852);
			match(EXTRACT);
			setState(853);
			match(LP_);
			setState(854);
			identifier();
			setState(855);
			match(FROM);
			setState(856);
			expr(0);
			setState(857);
			match(RP_);
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

	public static class TrimFunctionContext extends ParserRuleContext {
		public TerminalNode TRIM() { return getToken(DMLStatementParser.TRIM, 0); }
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<TerminalNode> STRING_() { return getTokens(DMLStatementParser.STRING_); }
		public TerminalNode STRING_(int i) {
			return getToken(DMLStatementParser.STRING_, i);
		}
		public TerminalNode FROM() { return getToken(DMLStatementParser.FROM, 0); }
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public TerminalNode LEADING() { return getToken(DMLStatementParser.LEADING, 0); }
		public TerminalNode BOTH() { return getToken(DMLStatementParser.BOTH, 0); }
		public TerminalNode TRAILING() { return getToken(DMLStatementParser.TRAILING, 0); }
		public TrimFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trimFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterTrimFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitTrimFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitTrimFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TrimFunctionContext trimFunction() throws RecognitionException {
		TrimFunctionContext _localctx = new TrimFunctionContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_trimFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(859);
			match(TRIM);
			setState(860);
			match(LP_);
			setState(861);
			_la = _input.LA(1);
			if ( !(_la==BOTH || _la==LEADING || _la==TRAILING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(862);
			match(STRING_);
			setState(863);
			match(FROM);
			setState(864);
			match(STRING_);
			setState(865);
			match(RP_);
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

	public static class RegularFunctionContext extends ParserRuleContext {
		public RegularFunctionNameContext regularFunctionName() {
			return getRuleContext(RegularFunctionNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(DMLStatementParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public RegularFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterRegularFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitRegularFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitRegularFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularFunctionContext regularFunction() throws RecognitionException {
		RegularFunctionContext _localctx = new RegularFunctionContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_regularFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(867);
			regularFunctionName();
			setState(868);
			match(LP_);
			setState(878);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case POSITION:
			case CASE:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case LOCAL:
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case CONVERT:
			case CURRENT_TIMESTAMP:
			case EXTRACT:
			case GLOBAL:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(869);
				expr(0);
				setState(874);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(870);
					match(COMMA_);
					setState(871);
					expr(0);
					}
					}
					setState(876);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(877);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(880);
			match(RP_);
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

	public static class RegularFunctionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode IF() { return getToken(DMLStatementParser.IF, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(DMLStatementParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode LOCALTIME() { return getToken(DMLStatementParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(DMLStatementParser.LOCALTIMESTAMP, 0); }
		public TerminalNode INTERVAL() { return getToken(DMLStatementParser.INTERVAL, 0); }
		public RegularFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularFunctionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterRegularFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitRegularFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitRegularFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularFunctionNameContext regularFunctionName() throws RecognitionException {
		RegularFunctionNameContext _localctx = new RegularFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_regularFunctionName);
		try {
			setState(888);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(882);
				identifier();
				}
				break;
			case IF:
				enterOuterAlt(_localctx, 2);
				{
				setState(883);
				match(IF);
				}
				break;
			case CURRENT_TIMESTAMP:
				enterOuterAlt(_localctx, 3);
				{
				setState(884);
				match(CURRENT_TIMESTAMP);
				}
				break;
			case LOCALTIME:
				enterOuterAlt(_localctx, 4);
				{
				setState(885);
				match(LOCALTIME);
				}
				break;
			case LOCALTIMESTAMP:
				enterOuterAlt(_localctx, 5);
				{
				setState(886);
				match(LOCALTIMESTAMP);
				}
				break;
			case INTERVAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(887);
				match(INTERVAL);
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

	public static class MatchExpressionContext extends ParserRuleContext {
		public LiteralsContext literals() {
			return getRuleContext(LiteralsContext.class,0);
		}
		public TerminalNode MATCH() { return getToken(DMLStatementParser.MATCH, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode PARTIAL() { return getToken(DMLStatementParser.PARTIAL, 0); }
		public TerminalNode FULL() { return getToken(DMLStatementParser.FULL, 0); }
		public TerminalNode UNIQUE() { return getToken(DMLStatementParser.UNIQUE, 0); }
		public MatchExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterMatchExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitMatchExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitMatchExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchExpressionContext matchExpression() throws RecognitionException {
		MatchExpressionContext _localctx = new MatchExpressionContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_matchExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(890);
			literals();
			setState(891);
			match(MATCH);
			setState(893);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNIQUE) {
				{
				setState(892);
				match(UNIQUE);
				}
			}

			setState(895);
			_la = _input.LA(1);
			if ( !(_la==FULL || _la==PARTIAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(896);
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

	public static class CaseExpressionContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(DMLStatementParser.CASE, 0); }
		public TerminalNode END() { return getToken(DMLStatementParser.END, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public List<CaseWhenContext> caseWhen() {
			return getRuleContexts(CaseWhenContext.class);
		}
		public CaseWhenContext caseWhen(int i) {
			return getRuleContext(CaseWhenContext.class,i);
		}
		public CaseElseContext caseElse() {
			return getRuleContext(CaseElseContext.class,0);
		}
		public CaseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCaseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCaseExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCaseExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseExpressionContext caseExpression() throws RecognitionException {
		CaseExpressionContext _localctx = new CaseExpressionContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_caseExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(898);
			match(CASE);
			setState(900);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_) | (1L << DOT_) | (1L << LP_) | (1L << LBE_) | (1L << QUESTION_) | (1L << AT_) | (1L << POSITION))) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & ((1L << (CASE - 73)) | (1L << (CAST - 73)) | (1L << (TRIM - 73)) | (1L << (SUBSTRING - 73)) | (1L << (IF - 73)) | (1L << (NULL - 73)) | (1L << (TRUE - 73)) | (1L << (FALSE - 73)) | (1L << (EXISTS - 73)) | (1L << (INTERVAL - 73)) | (1L << (DATE - 73)) | (1L << (TIME - 73)) | (1L << (TIMESTAMP - 73)) | (1L << (LOCALTIME - 73)) | (1L << (LOCALTIMESTAMP - 73)))) != 0) || ((((_la - 141)) & ~0x3f) == 0 && ((1L << (_la - 141)) & ((1L << (MAX - 141)) | (1L << (MIN - 141)) | (1L << (SUM - 141)) | (1L << (COUNT - 141)) | (1L << (AVG - 141)) | (1L << (LOCAL - 141)) | (1L << (NAME - 141)) | (1L << (TYPE - 141)) | (1L << (ADA - 141)) | (1L << (C92 - 141)) | (1L << (CATALOG_NAME - 141)) | (1L << (CHARACTER_SET_CATALOG - 141)) | (1L << (CHARACTER_SET_NAME - 141)) | (1L << (CHARACTER_SET_SCHEMA - 141)) | (1L << (CLASS_ORIGIN - 141)) | (1L << (COBOL - 141)) | (1L << (COLLATION_CATALOG - 141)) | (1L << (COLLATION_NAME - 141)) | (1L << (COLLATION_SCHEMA - 141)) | (1L << (COLUMN_NAME - 141)) | (1L << (COMMAND_FUNCTION - 141)) | (1L << (COMMITTED - 141)) | (1L << (CONDITION_NUMBER - 141)) | (1L << (CONNECTION_NAME - 141)) | (1L << (CONSTRAINT_CATALOG - 141)) | (1L << (CONSTRAINT_NAME - 141)) | (1L << (CONSTRAINT_SCHEMA - 141)) | (1L << (CURSOR_NAME - 141)) | (1L << (DATA - 141)) | (1L << (DATETIME_INTERVAL_CODE - 141)) | (1L << (DATETIME_INTERVAL_PRECISION - 141)) | (1L << (DYNAMIC_FUNCTION - 141)) | (1L << (FORTRAN - 141)) | (1L << (LENGTH - 141)) | (1L << (MESSAGE_LENGTH - 141)) | (1L << (MESSAGE_OCTET_LENGTH - 141)) | (1L << (MESSAGE_TEXT - 141)) | (1L << (MORE92 - 141)) | (1L << (MUMPS - 141)) | (1L << (NULLABLE - 141)) | (1L << (NUMBER - 141)))) != 0) || ((((_la - 205)) & ~0x3f) == 0 && ((1L << (_la - 205)) & ((1L << (PASCAL - 205)) | (1L << (PLI - 205)) | (1L << (REPEATABLE - 205)) | (1L << (RETURNED_LENGTH - 205)) | (1L << (RETURNED_OCTET_LENGTH - 205)) | (1L << (RETURNED_SQLSTATE - 205)) | (1L << (ROW_COUNT - 205)) | (1L << (SCALE - 205)) | (1L << (SCHEMA_NAME - 205)) | (1L << (SERIALIZABLE - 205)) | (1L << (SERVER_NAME - 205)) | (1L << (SUBCLASS_ORIGIN - 205)) | (1L << (TABLE_NAME - 205)) | (1L << (UNCOMMITTED - 205)) | (1L << (UNNAMED - 205)) | (1L << (CONVERT - 205)) | (1L << (CURRENT_TIMESTAMP - 205)) | (1L << (EXTRACT - 205)))) != 0) || _la==GLOBAL || ((((_la - 339)) & ~0x3f) == 0 && ((1L << (_la - 339)) & ((1L << (IDENTIFIER_ - 339)) | (1L << (STRING_ - 339)) | (1L << (NUMBER_ - 339)) | (1L << (HEX_DIGIT_ - 339)) | (1L << (BIT_NUM_ - 339)))) != 0)) {
				{
				setState(899);
				simpleExpr(0);
				}
			}

			setState(903); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(902);
				caseWhen();
				}
				}
				setState(905); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(908);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(907);
				caseElse();
				}
			}

			setState(910);
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

	public static class CaseWhenContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(DMLStatementParser.WHEN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode THEN() { return getToken(DMLStatementParser.THEN, 0); }
		public CaseWhenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCaseWhen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCaseWhen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCaseWhen(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseWhenContext caseWhen() throws RecognitionException {
		CaseWhenContext _localctx = new CaseWhenContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_caseWhen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(912);
			match(WHEN);
			setState(913);
			expr(0);
			setState(914);
			match(THEN);
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

	public static class CaseElseContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(DMLStatementParser.ELSE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public CaseElseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseElse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCaseElse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCaseElse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCaseElse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseElseContext caseElse() throws RecognitionException {
		CaseElseContext _localctx = new CaseElseContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_caseElse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(917);
			match(ELSE);
			setState(918);
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

	public static class IntervalExpressionContext extends ParserRuleContext {
		public TerminalNode INTERVAL() { return getToken(DMLStatementParser.INTERVAL, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public IntervalUnitContext intervalUnit() {
			return getRuleContext(IntervalUnitContext.class,0);
		}
		public IntervalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterIntervalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitIntervalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitIntervalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalExpressionContext intervalExpression() throws RecognitionException {
		IntervalExpressionContext _localctx = new IntervalExpressionContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_intervalExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(920);
			match(INTERVAL);
			setState(921);
			expr(0);
			setState(922);
			intervalUnit();
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

	public static class IntervalUnitContext extends ParserRuleContext {
		public TerminalNode MICROSECOND() { return getToken(DMLStatementParser.MICROSECOND, 0); }
		public TerminalNode SECOND() { return getToken(DMLStatementParser.SECOND, 0); }
		public TerminalNode MINUTE() { return getToken(DMLStatementParser.MINUTE, 0); }
		public TerminalNode HOUR() { return getToken(DMLStatementParser.HOUR, 0); }
		public TerminalNode DAY() { return getToken(DMLStatementParser.DAY, 0); }
		public TerminalNode WEEK() { return getToken(DMLStatementParser.WEEK, 0); }
		public TerminalNode MONTH() { return getToken(DMLStatementParser.MONTH, 0); }
		public TerminalNode QUARTER() { return getToken(DMLStatementParser.QUARTER, 0); }
		public TerminalNode YEAR() { return getToken(DMLStatementParser.YEAR, 0); }
		public IntervalUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterIntervalUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitIntervalUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitIntervalUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalUnitContext intervalUnit() throws RecognitionException {
		IntervalUnitContext _localctx = new IntervalUnitContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_intervalUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(924);
			_la = _input.LA(1);
			if ( !(((((_la - 132)) & ~0x3f) == 0 && ((1L << (_la - 132)) & ((1L << (YEAR - 132)) | (1L << (QUARTER - 132)) | (1L << (MONTH - 132)) | (1L << (WEEK - 132)) | (1L << (DAY - 132)) | (1L << (HOUR - 132)) | (1L << (MINUTE - 132)) | (1L << (SECOND - 132)) | (1L << (MICROSECOND - 132)))) != 0)) ) {
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

	public static class OrderByClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(DMLStatementParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(DMLStatementParser.BY, 0); }
		public List<OrderByItemContext> orderByItem() {
			return getRuleContexts(OrderByItemContext.class);
		}
		public OrderByItemContext orderByItem(int i) {
			return getRuleContext(OrderByItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterOrderByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitOrderByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitOrderByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(926);
			match(ORDER);
			setState(927);
			match(BY);
			setState(928);
			orderByItem();
			setState(933);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(929);
				match(COMMA_);
				setState(930);
				orderByItem();
				}
				}
				setState(935);
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

	public static class OrderByItemContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public TerminalNode ASC() { return getToken(DMLStatementParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(DMLStatementParser.DESC, 0); }
		public OrderByItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterOrderByItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitOrderByItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitOrderByItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByItemContext orderByItem() throws RecognitionException {
		OrderByItemContext _localctx = new OrderByItemContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_orderByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(938);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case IDENTIFIER_:
				{
				setState(936);
				columnName();
				}
				break;
			case MINUS_:
			case NUMBER_:
				{
				setState(937);
				numberLiterals();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(941);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(940);
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

	public static class DataTypeContext extends ParserRuleContext {
		public DataTypeNameContext dataTypeName() {
			return getRuleContext(DataTypeNameContext.class,0);
		}
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public CharacterSetContext characterSet() {
			return getRuleContext(CharacterSetContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<TerminalNode> STRING_() { return getTokens(DMLStatementParser.STRING_); }
		public TerminalNode STRING_(int i) {
			return getToken(DMLStatementParser.STRING_, i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(DMLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(DMLStatementParser.COMMA_, i);
		}
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_dataType);
		int _la;
		try {
			setState(970);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(943);
				dataTypeName();
				setState(945);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(944);
					dataTypeLength();
					}
				}

				setState(948);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHAR || _la==CHARACTER) {
					{
					setState(947);
					characterSet();
					}
				}

				setState(951);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(950);
					collateClause();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(953);
				dataTypeName();
				setState(954);
				match(LP_);
				setState(955);
				match(STRING_);
				setState(960);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(956);
					match(COMMA_);
					setState(957);
					match(STRING_);
					}
					}
					setState(962);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(963);
				match(RP_);
				setState(965);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHAR || _la==CHARACTER) {
					{
					setState(964);
					characterSet();
					}
				}

				setState(968);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(967);
					collateClause();
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

	public static class DataTypeNameContext extends ParserRuleContext {
		public TerminalNode CHARACTER() { return getToken(DMLStatementParser.CHARACTER, 0); }
		public TerminalNode VARYING() { return getToken(DMLStatementParser.VARYING, 0); }
		public TerminalNode NATIONAL() { return getToken(DMLStatementParser.NATIONAL, 0); }
		public TerminalNode CHAR() { return getToken(DMLStatementParser.CHAR, 0); }
		public TerminalNode VARCHAR() { return getToken(DMLStatementParser.VARCHAR, 0); }
		public TerminalNode NCHAR() { return getToken(DMLStatementParser.NCHAR, 0); }
		public TerminalNode BIT() { return getToken(DMLStatementParser.BIT, 0); }
		public TerminalNode NUMERIC() { return getToken(DMLStatementParser.NUMERIC, 0); }
		public TerminalNode DECIMAL() { return getToken(DMLStatementParser.DECIMAL, 0); }
		public TerminalNode DEC() { return getToken(DMLStatementParser.DEC, 0); }
		public TerminalNode INTEGER() { return getToken(DMLStatementParser.INTEGER, 0); }
		public TerminalNode SMALLINT() { return getToken(DMLStatementParser.SMALLINT, 0); }
		public TerminalNode FLOAT() { return getToken(DMLStatementParser.FLOAT, 0); }
		public TerminalNode REAL() { return getToken(DMLStatementParser.REAL, 0); }
		public TerminalNode DOUBLE() { return getToken(DMLStatementParser.DOUBLE, 0); }
		public TerminalNode PRECISION() { return getToken(DMLStatementParser.PRECISION, 0); }
		public TerminalNode DATE() { return getToken(DMLStatementParser.DATE, 0); }
		public List<TerminalNode> TIME() { return getTokens(DMLStatementParser.TIME); }
		public TerminalNode TIME(int i) {
			return getToken(DMLStatementParser.TIME, i);
		}
		public TerminalNode TIMESTAMP() { return getToken(DMLStatementParser.TIMESTAMP, 0); }
		public TerminalNode INTERVAL() { return getToken(DMLStatementParser.INTERVAL, 0); }
		public TerminalNode WITH() { return getToken(DMLStatementParser.WITH, 0); }
		public TerminalNode ZONE() { return getToken(DMLStatementParser.ZONE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DataTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDataTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDataTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDataTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeNameContext dataTypeName() throws RecognitionException {
		DataTypeNameContext _localctx = new DataTypeNameContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_dataTypeName);
		try {
			setState(1013);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(972);
				match(CHARACTER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(973);
				match(CHARACTER);
				setState(974);
				match(VARYING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(975);
				match(NATIONAL);
				setState(976);
				match(CHARACTER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(977);
				match(NATIONAL);
				setState(978);
				match(CHARACTER);
				setState(979);
				match(VARYING);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(980);
				match(CHAR);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(981);
				match(VARCHAR);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(982);
				match(NCHAR);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(983);
				match(NATIONAL);
				setState(984);
				match(CHAR);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(985);
				match(NATIONAL);
				setState(986);
				match(CHAR);
				setState(987);
				match(VARYING);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(988);
				match(BIT);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(989);
				match(BIT);
				setState(990);
				match(VARYING);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(991);
				match(NUMERIC);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(992);
				match(DECIMAL);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(993);
				match(DEC);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(994);
				match(INTEGER);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(995);
				match(SMALLINT);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(996);
				match(FLOAT);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(997);
				match(REAL);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(998);
				match(DOUBLE);
				setState(999);
				match(PRECISION);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1000);
				match(DATE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1001);
				match(TIME);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1002);
				match(TIMESTAMP);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1003);
				match(INTERVAL);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1004);
				match(TIME);
				setState(1005);
				match(WITH);
				setState(1006);
				match(TIME);
				setState(1007);
				match(ZONE);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1008);
				match(TIMESTAMP);
				setState(1009);
				match(WITH);
				setState(1010);
				match(TIME);
				setState(1011);
				match(ZONE);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1012);
				identifier();
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

	public static class DataTypeLengthContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(DMLStatementParser.LP_, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(DMLStatementParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(DMLStatementParser.NUMBER_, i);
		}
		public TerminalNode RP_() { return getToken(DMLStatementParser.RP_, 0); }
		public TerminalNode COMMA_() { return getToken(DMLStatementParser.COMMA_, 0); }
		public DataTypeLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDataTypeLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDataTypeLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDataTypeLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeLengthContext dataTypeLength() throws RecognitionException {
		DataTypeLengthContext _localctx = new DataTypeLengthContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_dataTypeLength);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1015);
			match(LP_);
			setState(1016);
			match(NUMBER_);
			setState(1019);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA_) {
				{
				setState(1017);
				match(COMMA_);
				setState(1018);
				match(NUMBER_);
				}
			}

			setState(1021);
			match(RP_);
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
		public TerminalNode SET() { return getToken(DMLStatementParser.SET, 0); }
		public IgnoredIdentifierContext ignoredIdentifier() {
			return getRuleContext(IgnoredIdentifierContext.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(DMLStatementParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(DMLStatementParser.CHAR, 0); }
		public TerminalNode EQ_() { return getToken(DMLStatementParser.EQ_, 0); }
		public CharacterSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCharacterSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCharacterSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCharacterSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterSetContext characterSet() throws RecognitionException {
		CharacterSetContext _localctx = new CharacterSetContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_characterSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1023);
			_la = _input.LA(1);
			if ( !(_la==CHAR || _la==CHARACTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1024);
			match(SET);
			setState(1026);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_) {
				{
				setState(1025);
				match(EQ_);
				}
			}

			setState(1028);
			ignoredIdentifier();
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
		public TerminalNode COLLATE() { return getToken(DMLStatementParser.COLLATE, 0); }
		public TerminalNode STRING_() { return getToken(DMLStatementParser.STRING_, 0); }
		public IgnoredIdentifierContext ignoredIdentifier() {
			return getRuleContext(IgnoredIdentifierContext.class,0);
		}
		public TerminalNode EQ_() { return getToken(DMLStatementParser.EQ_, 0); }
		public CollateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterCollateClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitCollateClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitCollateClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CollateClauseContext collateClause() throws RecognitionException {
		CollateClauseContext _localctx = new CollateClauseContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_collateClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1030);
			match(COLLATE);
			setState(1032);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_) {
				{
				setState(1031);
				match(EQ_);
				}
			}

			setState(1036);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_:
				{
				setState(1034);
				match(STRING_);
				}
				break;
			case NAME:
			case TYPE:
			case ADA:
			case C92:
			case CATALOG_NAME:
			case CHARACTER_SET_CATALOG:
			case CHARACTER_SET_NAME:
			case CHARACTER_SET_SCHEMA:
			case CLASS_ORIGIN:
			case COBOL:
			case COLLATION_CATALOG:
			case COLLATION_NAME:
			case COLLATION_SCHEMA:
			case COLUMN_NAME:
			case COMMAND_FUNCTION:
			case COMMITTED:
			case CONDITION_NUMBER:
			case CONNECTION_NAME:
			case CONSTRAINT_CATALOG:
			case CONSTRAINT_NAME:
			case CONSTRAINT_SCHEMA:
			case CURSOR_NAME:
			case DATA:
			case DATETIME_INTERVAL_CODE:
			case DATETIME_INTERVAL_PRECISION:
			case DYNAMIC_FUNCTION:
			case FORTRAN:
			case LENGTH:
			case MESSAGE_LENGTH:
			case MESSAGE_OCTET_LENGTH:
			case MESSAGE_TEXT:
			case MORE92:
			case MUMPS:
			case NULLABLE:
			case NUMBER:
			case PASCAL:
			case PLI:
			case REPEATABLE:
			case RETURNED_LENGTH:
			case RETURNED_OCTET_LENGTH:
			case RETURNED_SQLSTATE:
			case ROW_COUNT:
			case SCALE:
			case SCHEMA_NAME:
			case SERIALIZABLE:
			case SERVER_NAME:
			case SUBCLASS_ORIGIN:
			case TABLE_NAME:
			case UNCOMMITTED:
			case UNNAMED:
			case IDENTIFIER_:
				{
				setState(1035);
				ignoredIdentifier();
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

	public static class IgnoredIdentifierContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode DOT_() { return getToken(DMLStatementParser.DOT_, 0); }
		public IgnoredIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ignoredIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterIgnoredIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitIgnoredIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitIgnoredIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IgnoredIdentifierContext ignoredIdentifier() throws RecognitionException {
		IgnoredIdentifierContext _localctx = new IgnoredIdentifierContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_ignoredIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1038);
			identifier();
			setState(1041);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
			case 1:
				{
				setState(1039);
				match(DOT_);
				setState(1040);
				identifier();
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

	public static class DropBehaviourContext extends ParserRuleContext {
		public TerminalNode CASCADE() { return getToken(DMLStatementParser.CASCADE, 0); }
		public TerminalNode RESTRICT() { return getToken(DMLStatementParser.RESTRICT, 0); }
		public DropBehaviourContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropBehaviour; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).enterDropBehaviour(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DMLStatementListener ) ((DMLStatementListener)listener).exitDropBehaviour(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DMLStatementVisitor ) return ((DMLStatementVisitor<? extends T>)visitor).visitDropBehaviour(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropBehaviourContext dropBehaviour() throws RecognitionException {
		DropBehaviourContext _localctx = new DropBehaviourContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_dropBehaviour);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1044);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CASCADE || _la==RESTRICT) {
				{
				setState(1043);
				_la = _input.LA(1);
				if ( !(_la==CASCADE || _la==RESTRICT) ) {
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 56:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 59:
			return booleanPrimary_sempred((BooleanPrimaryContext)_localctx, predIndex);
		case 62:
			return bitExpr_sempred((BitExprContext)_localctx, predIndex);
		case 63:
			return simpleExpr_sempred((SimpleExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		}
		return true;
	}
	private boolean booleanPrimary_sempred(BooleanPrimaryContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 5);
		case 2:
			return precpred(_ctx, 4);
		case 3:
			return precpred(_ctx, 3);
		case 4:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean bitExpr_sempred(BitExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 13);
		case 6:
			return precpred(_ctx, 12);
		case 7:
			return precpred(_ctx, 11);
		case 8:
			return precpred(_ctx, 10);
		case 9:
			return precpred(_ctx, 9);
		case 10:
			return precpred(_ctx, 8);
		case 11:
			return precpred(_ctx, 7);
		case 12:
			return precpred(_ctx, 6);
		case 13:
			return precpred(_ctx, 5);
		case 14:
			return precpred(_ctx, 4);
		case 15:
			return precpred(_ctx, 3);
		case 16:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean simpleExpr_sempred(SimpleExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 17:
			return precpred(_ctx, 9);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0159\u0419\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\3\2\3\2\5\2\u00bd"+
		"\n\2\3\2\3\2\3\2\5\2\u00c2\n\2\3\3\5\3\u00c5\n\3\3\3\3\3\3\3\3\3\7\3\u00cb"+
		"\n\3\f\3\16\3\u00ce\13\3\3\4\5\4\u00d1\n\4\3\4\3\4\3\5\3\5\3\5\3\5\5\5"+
		"\u00d9\n\5\3\6\3\6\3\6\5\6\u00de\n\6\3\6\5\6\u00e1\n\6\3\6\3\6\5\6\u00e5"+
		"\n\6\3\7\3\7\3\7\3\7\7\7\u00eb\n\7\f\7\16\7\u00ee\13\7\3\b\3\b\3\b\3\b"+
		"\7\b\u00f4\n\b\f\b\16\b\u00f7\13\b\3\b\3\b\3\b\3\b\5\b\u00fd\n\b\3\t\3"+
		"\t\3\t\5\t\u0102\n\t\3\n\3\n\3\13\3\13\3\13\5\13\u0109\n\13\3\f\3\f\3"+
		"\f\5\f\u010e\n\f\3\f\5\f\u0111\n\f\3\r\3\r\3\16\3\16\3\16\5\16\u0118\n"+
		"\16\3\16\7\16\u011b\n\16\f\16\16\16\u011e\13\16\3\17\3\17\7\17\u0122\n"+
		"\17\f\17\16\17\u0125\13\17\3\17\3\17\5\17\u0129\n\17\3\17\5\17\u012c\n"+
		"\17\3\17\5\17\u012f\n\17\3\17\5\17\u0132\n\17\3\17\5\17\u0135\n\17\3\17"+
		"\5\17\u0138\n\17\3\20\3\20\3\21\3\21\3\22\3\22\5\22\u0140\n\22\3\22\3"+
		"\22\7\22\u0144\n\22\f\22\16\22\u0147\13\22\3\23\3\23\5\23\u014b\n\23\3"+
		"\23\5\23\u014e\n\23\3\23\5\23\u0151\n\23\3\23\5\23\u0154\n\23\3\24\3\24"+
		"\5\24\u0158\n\24\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\27\3\30\3\30\3\30"+
		"\7\30\u0165\n\30\f\30\16\30\u0168\13\30\3\31\3\31\3\31\3\31\3\31\5\31"+
		"\u016f\n\31\3\32\3\32\7\32\u0173\n\32\f\32\16\32\u0176\13\32\3\33\3\33"+
		"\5\33\u017a\n\33\3\33\5\33\u017d\n\33\3\33\3\33\5\33\u0181\n\33\3\33\3"+
		"\33\5\33\u0185\n\33\3\33\3\33\3\33\3\33\5\33\u018b\n\33\3\34\5\34\u018e"+
		"\n\34\3\34\3\34\3\34\3\34\5\34\u0194\n\34\3\34\3\34\5\34\u0198\n\34\3"+
		"\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u01a2\n\34\3\34\3\34\5\34"+
		"\u01a6\n\34\3\35\3\35\3\35\3\35\5\35\u01ac\n\35\3\36\3\36\3\36\3\37\3"+
		"\37\3\37\3\37\3\37\7\37\u01b6\n\37\f\37\16\37\u01b9\13\37\3 \3 \3 \3!"+
		"\3!\3!\3!\5!\u01c2\n!\3!\3!\3!\3!\3!\5!\u01c9\n!\3\"\3\"\5\"\u01cd\n\""+
		"\3#\3#\5#\u01d1\n#\3$\3$\3$\3$\3%\3%\3&\3&\3&\3&\3&\3&\3&\5&\u01e0\n&"+
		"\3\'\5\'\u01e3\n\'\3\'\3\'\5\'\u01e7\n\'\3(\5(\u01ea\n(\3(\3(\3)\3)\3"+
		")\3)\3)\3)\3)\5)\u01f5\n)\3*\5*\u01f8\n*\3*\3*\5*\u01fc\n*\3+\5+\u01ff"+
		"\n+\3+\3+\5+\u0203\n+\3,\3,\3-\3-\3.\3.\5.\u020b\n.\3/\3/\3\60\5\60\u0210"+
		"\n\60\3\60\5\60\u0213\n\60\3\60\5\60\u0216\n\60\3\60\5\60\u0219\n\60\3"+
		"\60\3\60\3\61\3\61\3\62\3\62\3\62\5\62\u0222\n\62\3\62\3\62\3\63\3\63"+
		"\3\63\5\63\u0229\n\63\3\63\3\63\3\64\3\64\3\64\3\64\5\64\u0231\n\64\3"+
		"\64\5\64\u0234\n\64\3\65\3\65\3\66\3\66\3\67\5\67\u023b\n\67\3\67\3\67"+
		"\3\67\7\67\u0240\n\67\f\67\16\67\u0243\13\67\3\67\5\67\u0246\n\67\38\5"+
		"8\u0249\n8\38\38\38\78\u024e\n8\f8\168\u0251\138\38\58\u0254\n8\39\39"+
		"\3:\3:\3:\3:\3:\3:\3:\3:\3:\5:\u0261\n:\3:\3:\3:\3:\7:\u0267\n:\f:\16"+
		":\u026a\13:\3;\3;\3<\3<\3=\3=\3=\3=\3=\3=\5=\u0276\n=\3=\3=\3=\3=\3=\3"+
		"=\3=\3=\3=\3=\3=\3=\3=\7=\u0285\n=\f=\16=\u0288\13=\3>\3>\3?\3?\5?\u028e"+
		"\n?\3?\3?\3?\3?\3?\5?\u0295\n?\3?\3?\3?\3?\3?\7?\u029c\n?\f?\16?\u029f"+
		"\13?\3?\3?\3?\3?\5?\u02a5\n?\3?\3?\3?\3?\3?\3?\3?\5?\u02ae\n?\3?\3?\3"+
		"?\3?\5?\u02b4\n?\3?\5?\u02b7\n?\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3"+
		"@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3"+
		"@\3@\3@\3@\7@\u02e0\n@\f@\16@\u02e3\13@\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A"+
		"\3A\3A\7A\u02f1\nA\fA\16A\u02f4\13A\3A\3A\3A\5A\u02f9\nA\3A\3A\3A\3A\3"+
		"A\3A\3A\3A\3A\5A\u0304\nA\3A\3A\3A\3A\5A\u030a\nA\7A\u030c\nA\fA\16A\u030f"+
		"\13A\3B\3B\3B\5B\u0314\nB\3C\3C\3C\5C\u0319\nC\3C\3C\3C\7C\u031e\nC\f"+
		"C\16C\u0321\13C\3C\5C\u0324\nC\3C\3C\3D\3D\3E\3E\3F\3F\3F\3F\3F\3F\5F"+
		"\u0332\nF\3G\3G\3G\3G\5G\u0338\nG\3G\3G\3G\3G\3H\3H\3H\3H\3H\3H\3H\3I"+
		"\3I\3I\3I\3I\3I\3I\3J\3J\3J\3J\3J\3J\3J\5J\u0353\nJ\3J\3J\3K\3K\3K\3K"+
		"\3K\3K\3K\3L\3L\3L\3L\3L\3L\3L\3L\3M\3M\3M\3M\3M\7M\u036b\nM\fM\16M\u036e"+
		"\13M\3M\5M\u0371\nM\3M\3M\3N\3N\3N\3N\3N\3N\5N\u037b\nN\3O\3O\3O\5O\u0380"+
		"\nO\3O\3O\3O\3P\3P\5P\u0387\nP\3P\6P\u038a\nP\rP\16P\u038b\3P\5P\u038f"+
		"\nP\3P\3P\3Q\3Q\3Q\3Q\3Q\3R\3R\3R\3S\3S\3S\3S\3T\3T\3U\3U\3U\3U\3U\7U"+
		"\u03a6\nU\fU\16U\u03a9\13U\3V\3V\5V\u03ad\nV\3V\5V\u03b0\nV\3W\3W\5W\u03b4"+
		"\nW\3W\5W\u03b7\nW\3W\5W\u03ba\nW\3W\3W\3W\3W\3W\7W\u03c1\nW\fW\16W\u03c4"+
		"\13W\3W\3W\5W\u03c8\nW\3W\5W\u03cb\nW\5W\u03cd\nW\3X\3X\3X\3X\3X\3X\3"+
		"X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3"+
		"X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\5X\u03f8\nX\3Y\3Y\3Y\3Y\5Y\u03fe\n"+
		"Y\3Y\3Y\3Z\3Z\3Z\5Z\u0405\nZ\3Z\3Z\3[\3[\5[\u040b\n[\3[\3[\5[\u040f\n"+
		"[\3\\\3\\\3\\\5\\\u0414\n\\\3]\5]\u0417\n]\3]\2\6rx~\u0080^\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\2\27\4\2GG\u014f\u014f"+
		"\4\2JJll\4\2TTXX\3\2VW\3\2\u0081\u0083\3\2gh\5\2\u00a4\u00a4\u00aa\u00aa"+
		"\u00ae\u00dd\4\2\u00a0\u00a0\u0110\u0110\4\2\3\3bc\4\2\5\5ee\4\2fh\u014b"+
		"\u014b\3\2lm\3\2\27\34\4\2\5\6\16\17\3\2\u008f\u0093\5\2\u00e7\u00e7\u011d"+
		"\u011d\u0147\u0147\4\2SS\u012e\u012e\3\2\u0086\u008e\3\2rs\3\2}~\4\2\u00e8"+
		"\u00e8\u0136\u0136\2\u047f\2\u00ba\3\2\2\2\4\u00c4\3\2\2\2\6\u00d0\3\2"+
		"\2\2\b\u00d4\3\2\2\2\n\u00da\3\2\2\2\f\u00e6\3\2\2\2\16\u00fc\3\2\2\2"+
		"\20\u0101\3\2\2\2\22\u0103\3\2\2\2\24\u0105\3\2\2\2\26\u010a\3\2\2\2\30"+
		"\u0112\3\2\2\2\32\u0114\3\2\2\2\34\u011f\3\2\2\2\36\u0139\3\2\2\2 \u013b"+
		"\3\2\2\2\"\u013f\3\2\2\2$\u0153\3\2\2\2&\u0157\3\2\2\2(\u0159\3\2\2\2"+
		"*\u015b\3\2\2\2,\u015e\3\2\2\2.\u0161\3\2\2\2\60\u016e\3\2\2\2\62\u0170"+
		"\3\2\2\2\64\u018a\3\2\2\2\66\u01a5\3\2\2\28\u01ab\3\2\2\2:\u01ad\3\2\2"+
		"\2<\u01b0\3\2\2\2>\u01ba\3\2\2\2@\u01bd\3\2\2\2B\u01cc\3\2\2\2D\u01d0"+
		"\3\2\2\2F\u01d2\3\2\2\2H\u01d6\3\2\2\2J\u01df\3\2\2\2L\u01e2\3\2\2\2N"+
		"\u01e9\3\2\2\2P\u01f4\3\2\2\2R\u01f7\3\2\2\2T\u01fe\3\2\2\2V\u0204\3\2"+
		"\2\2X\u0206\3\2\2\2Z\u020a\3\2\2\2\\\u020c\3\2\2\2^\u0212\3\2\2\2`\u021c"+
		"\3\2\2\2b\u0221\3\2\2\2d\u0228\3\2\2\2f\u0233\3\2\2\2h\u0235\3\2\2\2j"+
		"\u0237\3\2\2\2l\u023a\3\2\2\2n\u0248\3\2\2\2p\u0255\3\2\2\2r\u0260\3\2"+
		"\2\2t\u026b\3\2\2\2v\u026d\3\2\2\2x\u026f\3\2\2\2z\u0289\3\2\2\2|\u02b6"+
		"\3\2\2\2~\u02b8\3\2\2\2\u0080\u0303\3\2\2\2\u0082\u0313\3\2\2\2\u0084"+
		"\u0315\3\2\2\2\u0086\u0327\3\2\2\2\u0088\u0329\3\2\2\2\u008a\u0331\3\2"+
		"\2\2\u008c\u0333\3\2\2\2\u008e\u033d\3\2\2\2\u0090\u0344\3\2\2\2\u0092"+
		"\u034b\3\2\2\2\u0094\u0356\3\2\2\2\u0096\u035d\3\2\2\2\u0098\u0365\3\2"+
		"\2\2\u009a\u037a\3\2\2\2\u009c\u037c\3\2\2\2\u009e\u0384\3\2\2\2\u00a0"+
		"\u0392\3\2\2\2\u00a2\u0397\3\2\2\2\u00a4\u039a\3\2\2\2\u00a6\u039e\3\2"+
		"\2\2\u00a8\u03a0\3\2\2\2\u00aa\u03ac\3\2\2\2\u00ac\u03cc\3\2\2\2\u00ae"+
		"\u03f7\3\2\2\2\u00b0\u03f9\3\2\2\2\u00b2\u0401\3\2\2\2\u00b4\u0408\3\2"+
		"\2\2\u00b6\u0410\3\2\2\2\u00b8\u0416\3\2\2\2\u00ba\u00bc\7,\2\2\u00bb"+
		"\u00bd\7F\2\2\u00bc\u00bb\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be\3\2"+
		"\2\2\u00be\u00c1\5b\62\2\u00bf\u00c2\5\4\3\2\u00c0\u00c2\5\6\4\2\u00c1"+
		"\u00bf\3\2\2\2\u00c1\u00c0\3\2\2\2\u00c2\3\3\2\2\2\u00c3\u00c5\5l\67\2"+
		"\u00c4\u00c3\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c7"+
		"\t\2\2\2\u00c7\u00cc\5\16\b\2\u00c8\u00c9\7$\2\2\u00c9\u00cb\5\16\b\2"+
		"\u00ca\u00c8\3\2\2\2\u00cb\u00ce\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cc\u00cd"+
		"\3\2\2\2\u00cd\5\3\2\2\2\u00ce\u00cc\3\2\2\2\u00cf\u00d1\5l\67\2\u00d0"+
		"\u00cf\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2\u00d3\5\30"+
		"\r\2\u00d3\7\3\2\2\2\u00d4\u00d5\7-\2\2\u00d5\u00d6\5.\30\2\u00d6\u00d8"+
		"\5\f\7\2\u00d7\u00d9\5:\36\2\u00d8\u00d7\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9"+
		"\t\3\2\2\2\u00da\u00db\5d\63\2\u00db\u00dd\7\27\2\2\u00dc\u00de\7G\2\2"+
		"\u00dd\u00dc\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00e0\3\2\2\2\u00df\u00e1"+
		"\7\36\2\2\u00e0\u00df\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e2\3\2\2\2"+
		"\u00e2\u00e4\5\20\t\2\u00e3\u00e5\7\37\2\2\u00e4\u00e3\3\2\2\2\u00e4\u00e5"+
		"\3\2\2\2\u00e5\13\3\2\2\2\u00e6\u00e7\7\67\2\2\u00e7\u00ec\5\n\6\2\u00e8"+
		"\u00e9\7$\2\2\u00e9\u00eb\5\n\6\2\u00ea\u00e8\3\2\2\2\u00eb\u00ee\3\2"+
		"\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\r\3\2\2\2\u00ee\u00ec"+
		"\3\2\2\2\u00ef\u00f0\7\36\2\2\u00f0\u00f5\5\20\t\2\u00f1\u00f2\7$\2\2"+
		"\u00f2\u00f4\5\20\t\2\u00f3\u00f1\3\2\2\2\u00f4\u00f7\3\2\2\2\u00f5\u00f3"+
		"\3\2\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f8\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8"+
		"\u00f9\7\37\2\2\u00f9\u00fd\3\2\2\2\u00fa\u00fb\7\36\2\2\u00fb\u00fd\7"+
		"\37\2\2\u00fc\u00ef\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd\17\3\2\2\2\u00fe"+
		"\u0102\5r:\2\u00ff\u0102\7\u0094\2\2\u0100\u0102\5\22\n\2\u0101\u00fe"+
		"\3\2\2\2\u0101\u00ff\3\2\2\2\u0101\u0100\3\2\2\2\u0102\21\3\2\2\2\u0103"+
		"\u0104\7\u0156\2\2\u0104\23\3\2\2\2\u0105\u0106\7.\2\2\u0106\u0108\5\26"+
		"\f\2\u0107\u0109\5:\36\2\u0108\u0107\3\2\2\2\u0108\u0109\3\2\2\2\u0109"+
		"\25\3\2\2\2\u010a\u010b\7P\2\2\u010b\u0110\5b\62\2\u010c\u010e\7[\2\2"+
		"\u010d\u010c\3\2\2\2\u010d\u010e\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u0111"+
		"\5&\24\2\u0110\u010d\3\2\2\2\u0110\u0111\3\2\2\2\u0111\27\3\2\2\2\u0112"+
		"\u0113\5\32\16\2\u0113\31\3\2\2\2\u0114\u011c\5\34\17\2\u0115\u0117\7"+
		"I\2\2\u0116\u0118\7l\2\2\u0117\u0116\3\2\2\2\u0117\u0118\3\2\2\2\u0118"+
		"\u0119\3\2\2\2\u0119\u011b\5\34\17\2\u011a\u0115\3\2\2\2\u011b\u011e\3"+
		"\2\2\2\u011c\u011a\3\2\2\2\u011c\u011d\3\2\2\2\u011d\33\3\2\2\2\u011e"+
		"\u011c\3\2\2\2\u011f\u0123\7+\2\2\u0120\u0122\5\36\20\2\u0121\u0120\3"+
		"\2\2\2\u0122\u0125\3\2\2\2\u0123\u0121\3\2\2\2\u0123\u0124\3\2\2\2\u0124"+
		"\u0126\3\2\2\2\u0125\u0123\3\2\2\2\u0126\u0128\5\"\22\2\u0127\u0129\5"+
		",\27\2\u0128\u0127\3\2\2\2\u0128\u0129\3\2\2\2\u0129\u012b\3\2\2\2\u012a"+
		"\u012c\5:\36\2\u012b\u012a\3\2\2\2\u012b\u012c\3\2\2\2\u012c\u012e\3\2"+
		"\2\2\u012d\u012f\5<\37\2\u012e\u012d\3\2\2\2\u012e\u012f\3\2\2\2\u012f"+
		"\u0131\3\2\2\2\u0130\u0132\5> \2\u0131\u0130\3\2\2\2\u0131\u0132\3\2\2"+
		"\2\u0132\u0134\3\2\2\2\u0133\u0135\5\u00a8U\2\u0134\u0133\3\2\2\2\u0134"+
		"\u0135\3\2\2\2\u0135\u0137\3\2\2\2\u0136\u0138\5@!\2\u0137\u0136\3\2\2"+
		"\2\u0137\u0138\3\2\2\2\u0138\35\3\2\2\2\u0139\u013a\5 \21\2\u013a\37\3"+
		"\2\2\2\u013b\u013c\t\3\2\2\u013c!\3\2\2\2\u013d\u0140\5(\25\2\u013e\u0140"+
		"\5$\23\2\u013f\u013d\3\2\2\2\u013f\u013e\3\2\2\2\u0140\u0145\3\2\2\2\u0141"+
		"\u0142\7$\2\2\u0142\u0144\5$\23\2\u0143\u0141\3\2\2\2\u0144\u0147\3\2"+
		"\2\2\u0145\u0143\3\2\2\2\u0145\u0146\3\2\2\2\u0146#\3\2\2\2\u0147\u0145"+
		"\3\2\2\2\u0148\u014b\5d\63\2\u0149\u014b\5r:\2\u014a\u0148\3\2\2\2\u014a"+
		"\u0149\3\2\2\2\u014b\u0150\3\2\2\2\u014c\u014e\7[\2\2\u014d\u014c\3\2"+
		"\2\2\u014d\u014e\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u0151\5&\24\2\u0150"+
		"\u014d\3\2\2\2\u0150\u0151\3\2\2\2\u0151\u0154\3\2\2\2\u0152\u0154\5*"+
		"\26\2\u0153\u014a\3\2\2\2\u0153\u0152\3\2\2\2\u0154%\3\2\2\2\u0155\u0158"+
		"\5Z.\2\u0156\u0158\7\u0156\2\2\u0157\u0155\3\2\2\2\u0157\u0156\3\2\2\2"+
		"\u0158\'\3\2\2\2\u0159\u015a\7\20\2\2\u015a)\3\2\2\2\u015b\u015c\5Z.\2"+
		"\u015c\u015d\7\24\2\2\u015d+\3\2\2\2\u015e\u015f\7P\2\2\u015f\u0160\5"+
		".\30\2\u0160-\3\2\2\2\u0161\u0166\5\60\31\2\u0162\u0163\7$\2\2\u0163\u0165"+
		"\5\60\31\2\u0164\u0162\3\2\2\2\u0165\u0168\3\2\2\2\u0166\u0164\3\2\2\2"+
		"\u0166\u0167\3\2\2\2\u0167/\3\2\2\2\u0168\u0166\3\2\2\2\u0169\u016f\5"+
		"\62\32\2\u016a\u016b\7 \2\2\u016b\u016c\5\62\32\2\u016c\u016d\7!\2\2\u016d"+
		"\u016f\3\2\2\2\u016e\u0169\3\2\2\2\u016e\u016a\3\2\2\2\u016f\61\3\2\2"+
		"\2\u0170\u0174\5\64\33\2\u0171\u0173\5\66\34\2\u0172\u0171\3\2\2\2\u0173"+
		"\u0176\3\2\2\2\u0174\u0172\3\2\2\2\u0174\u0175\3\2\2\2\u0175\63\3\2\2"+
		"\2\u0176\u0174\3\2\2\2\u0177\u017c\5b\62\2\u0178\u017a\7[\2\2\u0179\u0178"+
		"\3\2\2\2\u0179\u017a\3\2\2\2\u017a\u017b\3\2\2\2\u017b\u017d\5&\24\2\u017c"+
		"\u0179\3\2\2\2\u017c\u017d\3\2\2\2\u017d\u018b\3\2\2\2\u017e\u0180\5F"+
		"$\2\u017f\u0181\7[\2\2\u0180\u017f\3\2\2\2\u0180\u0181\3\2\2\2\u0181\u0182"+
		"\3\2\2\2\u0182\u0184\5&\24\2\u0183\u0185\5l\67\2\u0184\u0183\3\2\2\2\u0184"+
		"\u0185\3\2\2\2\u0185\u018b\3\2\2\2\u0186\u0187\7\36\2\2\u0187\u0188\5"+
		".\30\2\u0188\u0189\7\37\2\2\u0189\u018b\3\2\2\2\u018a\u0177\3\2\2\2\u018a"+
		"\u017e\3\2\2\2\u018a\u0186\3\2\2\2\u018b\65\3\2\2\2\u018c\u018e\t\4\2"+
		"\2\u018d\u018c\3\2\2\2\u018d\u018e\3\2\2\2\u018e\u018f\3\2\2\2\u018f\u0190"+
		"\7R\2\2\u0190\u0191\3\2\2\2\u0191\u0193\5\64\33\2\u0192\u0194\58\35\2"+
		"\u0193\u0192\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u01a6\3\2\2\2\u0195\u0197"+
		"\t\5\2\2\u0196\u0198\7U\2\2\u0197\u0196\3\2\2\2\u0197\u0198\3\2\2\2\u0198"+
		"\u0199\3\2\2\2\u0199\u019a\7R\2\2\u019a\u019b\5\64\33\2\u019b\u019c\5"+
		"8\35\2\u019c\u01a6\3\2\2\2\u019d\u01a1\7Q\2\2\u019e\u01a2\7T\2\2\u019f"+
		"\u01a0\t\5\2\2\u01a0\u01a2\7U\2\2\u01a1\u019e\3\2\2\2\u01a1\u019f\3\2"+
		"\2\2\u01a1\u01a2\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3\u01a4\7R\2\2\u01a4"+
		"\u01a6\5\64\33\2\u01a5\u018d\3\2\2\2\u01a5\u0195\3\2\2\2\u01a5\u019d\3"+
		"\2\2\2\u01a6\67\3\2\2\2\u01a7\u01a8\7\\\2\2\u01a8\u01ac\5r:\2\u01a9\u01aa"+
		"\7Y\2\2\u01aa\u01ac\5l\67\2\u01ab\u01a7\3\2\2\2\u01ab\u01a9\3\2\2\2\u01ac"+
		"9\3\2\2\2\u01ad\u01ae\7Z\2\2\u01ae\u01af\5r:\2\u01af;\3\2\2\2\u01b0\u01b1"+
		"\7p\2\2\u01b1\u01b2\7q\2\2\u01b2\u01b7\5\u00aaV\2\u01b3\u01b4\7$\2\2\u01b4"+
		"\u01b6\5\u00aaV\2\u01b5\u01b3\3\2\2\2\u01b6\u01b9\3\2\2\2\u01b7\u01b5"+
		"\3\2\2\2\u01b7\u01b8\3\2\2\2\u01b8=\3\2\2\2\u01b9\u01b7\3\2\2\2\u01ba"+
		"\u01bb\7t\2\2\u01bb\u01bc\5r:\2\u01bc?\3\2\2\2\u01bd\u01c8\7u\2\2\u01be"+
		"\u01bf\5D#\2\u01bf\u01c0\7$\2\2\u01c0\u01c2\3\2\2\2\u01c1\u01be\3\2\2"+
		"\2\u01c1\u01c2\3\2\2\2\u01c2\u01c3\3\2\2\2\u01c3\u01c9\5B\"\2\u01c4\u01c5"+
		"\5B\"\2\u01c5\u01c6\7v\2\2\u01c6\u01c7\5D#\2\u01c7\u01c9\3\2\2\2\u01c8"+
		"\u01c1\3\2\2\2\u01c8\u01c4\3\2\2\2\u01c9A\3\2\2\2\u01ca\u01cd\5N(\2\u01cb"+
		"\u01cd\5H%\2\u01cc\u01ca\3\2\2\2\u01cc\u01cb\3\2\2\2\u01cdC\3\2\2\2\u01ce"+
		"\u01d1\5N(\2\u01cf\u01d1\5H%\2\u01d0\u01ce\3\2\2\2\u01d0\u01cf\3\2\2\2"+
		"\u01d1E\3\2\2\2\u01d2\u01d3\7\36\2\2\u01d3\u01d4\5\32\16\2\u01d4\u01d5"+
		"\7\37\2\2\u01d5G\3\2\2\2\u01d6\u01d7\7\'\2\2\u01d7I\3\2\2\2\u01d8\u01e0"+
		"\5L\'\2\u01d9\u01e0\5N(\2\u01da\u01e0\5P)\2\u01db\u01e0\5R*\2\u01dc\u01e0"+
		"\5T+\2\u01dd\u01e0\5V,\2\u01de\u01e0\5X-\2\u01df\u01d8\3\2\2\2\u01df\u01d9"+
		"\3\2\2\2\u01df\u01da\3\2\2\2\u01df\u01db\3\2\2\2\u01df\u01dc\3\2\2\2\u01df"+
		"\u01dd\3\2\2\2\u01df\u01de\3\2\2\2\u01e0K\3\2\2\2\u01e1\u01e3\5p9\2\u01e2"+
		"\u01e1\3\2\2\2\u01e2\u01e3\3\2\2\2\u01e3\u01e4\3\2\2\2\u01e4\u01e6\7\u0156"+
		"\2\2\u01e5\u01e7\5\u00b4[\2\u01e6\u01e5\3\2\2\2\u01e6\u01e7\3\2\2\2\u01e7"+
		"M\3\2\2\2\u01e8\u01ea\7\17\2\2\u01e9\u01e8\3\2\2\2\u01e9\u01ea\3\2\2\2"+
		"\u01ea\u01eb\3\2\2\2\u01eb\u01ec\7\u0157\2\2\u01ecO\3\2\2\2\u01ed\u01ee"+
		"\t\6\2\2\u01ee\u01f5\7\u0156\2\2\u01ef\u01f0\7 \2\2\u01f0\u01f1\5Z.\2"+
		"\u01f1\u01f2\7\u0156\2\2\u01f2\u01f3\7!\2\2\u01f3\u01f5\3\2\2\2\u01f4"+
		"\u01ed\3\2\2\2\u01f4\u01ef\3\2\2\2\u01f5Q\3\2\2\2\u01f6\u01f8\5p9\2\u01f7"+
		"\u01f6\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8\u01f9\3\2\2\2\u01f9\u01fb\7\u0158"+
		"\2\2\u01fa\u01fc\5\u00b4[\2\u01fb\u01fa\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc"+
		"S\3\2\2\2\u01fd\u01ff\5p9\2\u01fe\u01fd\3\2\2\2\u01fe\u01ff\3\2\2\2\u01ff"+
		"\u0200\3\2\2\2\u0200\u0202\7\u0159\2\2\u0201\u0203\5\u00b4[\2\u0202\u0201"+
		"\3\2\2\2\u0202\u0203\3\2\2\2\u0203U\3\2\2\2\u0204\u0205\t\7\2\2\u0205"+
		"W\3\2\2\2\u0206\u0207\7f\2\2\u0207Y\3\2\2\2\u0208\u020b\7\u0155\2\2\u0209"+
		"\u020b\5\\/\2\u020a\u0208\3\2\2\2\u020a\u0209\3\2\2\2\u020b[\3\2\2\2\u020c"+
		"\u020d\t\b\2\2\u020d]\3\2\2\2\u020e\u0210\7(\2\2\u020f\u020e\3\2\2\2\u020f"+
		"\u0210\3\2\2\2\u0210\u0211\3\2\2\2\u0211\u0213\7(\2\2\u0212\u020f\3\2"+
		"\2\2\u0212\u0213\3\2\2\2\u0213\u0215\3\2\2\2\u0214\u0216\t\t\2\2\u0215"+
		"\u0214\3\2\2\2\u0215\u0216\3\2\2\2\u0216\u0218\3\2\2\2\u0217\u0219\7\23"+
		"\2\2\u0218\u0217\3\2\2\2\u0218\u0219\3\2\2\2\u0219\u021a\3\2\2\2\u021a"+
		"\u021b\5Z.\2\u021b_\3\2\2\2\u021c\u021d\5Z.\2\u021da\3\2\2\2\u021e\u021f"+
		"\5h\65\2\u021f\u0220\7\23\2\2\u0220\u0222\3\2\2\2\u0221\u021e\3\2\2\2"+
		"\u0221\u0222\3\2\2\2\u0222\u0223\3\2\2\2\u0223\u0224\5j\66\2\u0224c\3"+
		"\2\2\2\u0225\u0226\5h\65\2\u0226\u0227\7\23\2\2\u0227\u0229\3\2\2\2\u0228"+
		"\u0225\3\2\2\2\u0228\u0229\3\2\2\2\u0229\u022a\3\2\2\2\u022a\u022b\5j"+
		"\66\2\u022be\3\2\2\2\u022c\u0234\5Z.\2\u022d\u022e\5h\65\2\u022e\u022f"+
		"\7\23\2\2\u022f\u0231\3\2\2\2\u0230\u022d\3\2\2\2\u0230\u0231\3\2\2\2"+
		"\u0231\u0232\3\2\2\2\u0232\u0234\5Z.\2\u0233\u022c\3\2\2\2\u0233\u0230"+
		"\3\2\2\2\u0234g\3\2\2\2\u0235\u0236\5Z.\2\u0236i\3\2\2\2\u0237\u0238\5"+
		"Z.\2\u0238k\3\2\2\2\u0239\u023b\7\36\2\2\u023a\u0239\3\2\2\2\u023a\u023b"+
		"\3\2\2\2\u023b\u023c\3\2\2\2\u023c\u0241\5d\63\2\u023d\u023e\7$\2\2\u023e"+
		"\u0240\5d\63\2\u023f\u023d\3\2\2\2\u0240\u0243\3\2\2\2\u0241\u023f\3\2"+
		"\2\2\u0241\u0242\3\2\2\2\u0242\u0245\3\2\2\2\u0243\u0241\3\2\2\2\u0244"+
		"\u0246\7\37\2\2\u0245\u0244\3\2\2\2\u0245\u0246\3\2\2\2\u0246m\3\2\2\2"+
		"\u0247\u0249\7\36\2\2\u0248\u0247\3\2\2\2\u0248\u0249\3\2\2\2\u0249\u024a"+
		"\3\2\2\2\u024a\u024f\5b\62\2\u024b\u024c\7$\2\2\u024c\u024e\5b\62\2\u024d"+
		"\u024b\3\2\2\2\u024e\u0251\3\2\2\2\u024f\u024d\3\2\2\2\u024f\u0250\3\2"+
		"\2\2\u0250\u0253\3\2\2\2\u0251\u024f\3\2\2\2\u0252\u0254\7\37\2\2\u0253"+
		"\u0252\3\2\2\2\u0253\u0254\3\2\2\2\u0254o\3\2\2\2\u0255\u0256\7\u0155"+
		"\2\2\u0256q\3\2\2\2\u0257\u0258\b:\1\2\u0258\u0259\5v<\2\u0259\u025a\5"+
		"r:\5\u025a\u0261\3\2\2\2\u025b\u025c\7\36\2\2\u025c\u025d\5r:\2\u025d"+
		"\u025e\7\37\2\2\u025e\u0261\3\2\2\2\u025f\u0261\5x=\2\u0260\u0257\3\2"+
		"\2\2\u0260\u025b\3\2\2\2\u0260\u025f\3\2\2\2\u0261\u0268\3\2\2\2\u0262"+
		"\u0263\f\6\2\2\u0263\u0264\5t;\2\u0264\u0265\5r:\7\u0265\u0267\3\2\2\2"+
		"\u0266\u0262\3\2\2\2\u0267\u026a\3\2\2\2\u0268\u0266\3\2\2\2\u0268\u0269"+
		"\3\2\2\2\u0269s\3\2\2\2\u026a\u0268\3\2\2\2\u026b\u026c\t\n\2\2\u026c"+
		"u\3\2\2\2\u026d\u026e\t\13\2\2\u026ew\3\2\2\2\u026f\u0270\b=\1\2\u0270"+
		"\u0271\5|?\2\u0271\u0286\3\2\2\2\u0272\u0273\f\7\2\2\u0273\u0275\7d\2"+
		"\2\u0274\u0276\7e\2\2\u0275\u0274\3\2\2\2\u0275\u0276\3\2\2\2\u0276\u0277"+
		"\3\2\2\2\u0277\u0285\t\f\2\2\u0278\u0279\f\6\2\2\u0279\u027a\7\25\2\2"+
		"\u027a\u0285\5|?\2\u027b\u027c\f\5\2\2\u027c\u027d\5z>\2\u027d\u027e\5"+
		"|?\2\u027e\u0285\3\2\2\2\u027f\u0280\f\4\2\2\u0280\u0281\5z>\2\u0281\u0282"+
		"\t\r\2\2\u0282\u0283\5F$\2\u0283\u0285\3\2\2\2\u0284\u0272\3\2\2\2\u0284"+
		"\u0278\3\2\2\2\u0284\u027b\3\2\2\2\u0284\u027f\3\2\2\2\u0285\u0288\3\2"+
		"\2\2\u0286\u0284\3\2\2\2\u0286\u0287\3\2\2\2\u0287y\3\2\2\2\u0288\u0286"+
		"\3\2\2\2\u0289\u028a\t\16\2\2\u028a{\3\2\2\2\u028b\u028d\5~@\2\u028c\u028e"+
		"\7e\2\2\u028d\u028c\3\2\2\2\u028d\u028e\3\2\2\2\u028e\u028f\3\2\2\2\u028f"+
		"\u0290\7k\2\2\u0290\u0291\5F$\2\u0291\u02b7\3\2\2\2\u0292\u0294\5~@\2"+
		"\u0293\u0295\7e\2\2\u0294\u0293\3\2\2\2\u0294\u0295\3\2\2\2\u0295\u0296"+
		"\3\2\2\2\u0296\u0297\7k\2\2\u0297\u0298\7\36\2\2\u0298\u029d\5r:\2\u0299"+
		"\u029a\7$\2\2\u029a\u029c\5r:\2\u029b\u0299\3\2\2\2\u029c\u029f\3\2\2"+
		"\2\u029d\u029b\3\2\2\2\u029d\u029e\3\2\2\2\u029e\u02a0\3\2\2\2\u029f\u029d"+
		"\3\2\2\2\u02a0\u02a1\7\37\2\2\u02a1\u02b7\3\2\2\2\u02a2\u02a4\5~@\2\u02a3"+
		"\u02a5\7e\2\2\u02a4\u02a3\3\2\2\2\u02a4\u02a5\3\2\2\2\u02a5\u02a6\3\2"+
		"\2\2\u02a6\u02a7\7j\2\2\u02a7\u02a8\5~@\2\u02a8\u02a9\7b\2\2\u02a9\u02aa"+
		"\5|?\2\u02aa\u02b7\3\2\2\2\u02ab\u02ad\5~@\2\u02ac\u02ae\7e\2\2\u02ad"+
		"\u02ac\3\2\2\2\u02ad\u02ae\3\2\2\2\u02ae\u02af\3\2\2\2\u02af\u02b0\7n"+
		"\2\2\u02b0\u02b3\5\u0080A\2\u02b1\u02b2\7\u0105\2\2\u02b2\u02b4\5\u0080"+
		"A\2\u02b3\u02b1\3\2\2\2\u02b3\u02b4\3\2\2\2\u02b4\u02b7\3\2\2\2\u02b5"+
		"\u02b7\5~@\2\u02b6\u028b\3\2\2\2\u02b6\u0292\3\2\2\2\u02b6\u02a2\3\2\2"+
		"\2\u02b6\u02ab\3\2\2\2\u02b6\u02b5\3\2\2\2\u02b7}\3\2\2\2\u02b8\u02b9"+
		"\b@\1\2\u02b9\u02ba\5\u0080A\2\u02ba\u02e1\3\2\2\2\u02bb\u02bc\f\17\2"+
		"\2\u02bc\u02bd\7\7\2\2\u02bd\u02e0\5~@\20\u02be\u02bf\f\16\2\2\u02bf\u02c0"+
		"\7\b\2\2\u02c0\u02e0\5~@\17\u02c1\u02c2\f\r\2\2\u02c2\u02c3\7\t\2\2\u02c3"+
		"\u02e0\5~@\16\u02c4\u02c5\f\f\2\2\u02c5\u02c6\7\n\2\2\u02c6\u02e0\5~@"+
		"\r\u02c7\u02c8\f\13\2\2\u02c8\u02c9\7\16\2\2\u02c9\u02e0\5~@\f\u02ca\u02cb"+
		"\f\n\2\2\u02cb\u02cc\7\17\2\2\u02cc\u02e0\5~@\13\u02cd\u02ce\f\t\2\2\u02ce"+
		"\u02cf\7\20\2\2\u02cf\u02e0\5~@\n\u02d0\u02d1\f\b\2\2\u02d1\u02d2\7\21"+
		"\2\2\u02d2\u02e0\5~@\t\u02d3\u02d4\f\7\2\2\u02d4\u02d5\7\f\2\2\u02d5\u02e0"+
		"\5~@\b\u02d6\u02d7\f\6\2\2\u02d7\u02d8\7\13\2\2\u02d8\u02e0\5~@\7\u02d9"+
		"\u02da\f\5\2\2\u02da\u02db\7\16\2\2\u02db\u02e0\5\u00a4S\2\u02dc\u02dd"+
		"\f\4\2\2\u02dd\u02de\7\17\2\2\u02de\u02e0\5\u00a4S\2\u02df\u02bb\3\2\2"+
		"\2\u02df\u02be\3\2\2\2\u02df\u02c1\3\2\2\2\u02df\u02c4\3\2\2\2\u02df\u02c7"+
		"\3\2\2\2\u02df\u02ca\3\2\2\2\u02df\u02cd\3\2\2\2\u02df\u02d0\3\2\2\2\u02df"+
		"\u02d3\3\2\2\2\u02df\u02d6\3\2\2\2\u02df\u02d9\3\2\2\2\u02df\u02dc\3\2"+
		"\2\2\u02e0\u02e3\3\2\2\2\u02e1\u02df\3\2\2\2\u02e1\u02e2\3\2\2\2\u02e2"+
		"\177\3\2\2\2\u02e3\u02e1\3\2\2\2\u02e4\u02e5\bA\1\2\u02e5\u0304\5\u0082"+
		"B\2\u02e6\u0304\5H%\2\u02e7\u0304\5J&\2\u02e8\u0304\5d\63\2\u02e9\u0304"+
		"\5^\60\2\u02ea\u02eb\t\17\2\2\u02eb\u0304\5\u0080A\t\u02ec\u02ed\7\36"+
		"\2\2\u02ed\u02f2\5r:\2\u02ee\u02ef\7$\2\2\u02ef\u02f1\5r:\2\u02f0\u02ee"+
		"\3\2\2\2\u02f1\u02f4\3\2\2\2\u02f2\u02f0\3\2\2\2\u02f2\u02f3\3\2\2\2\u02f3"+
		"\u02f5\3\2\2\2\u02f4\u02f2\3\2\2\2\u02f5\u02f6\7\37\2\2\u02f6\u0304\3"+
		"\2\2\2\u02f7\u02f9\7i\2\2\u02f8\u02f7\3\2\2\2\u02f8\u02f9\3\2\2\2\u02f9"+
		"\u02fa\3\2\2\2\u02fa\u0304\5F$\2\u02fb\u02fc\7 \2\2\u02fc\u02fd\5Z.\2"+
		"\u02fd\u02fe\5r:\2\u02fe\u02ff\7!\2\2\u02ff\u0304\3\2\2\2\u0300\u0304"+
		"\5\u009cO\2\u0301\u0304\5\u009eP\2\u0302\u0304\5\u00a4S\2\u0303\u02e4"+
		"\3\2\2\2\u0303\u02e6\3\2\2\2\u0303\u02e7\3\2\2\2\u0303\u02e8\3\2\2\2\u0303"+
		"\u02e9\3\2\2\2\u0303\u02ea\3\2\2\2\u0303\u02ec\3\2\2\2\u0303\u02f8\3\2"+
		"\2\2\u0303\u02fb\3\2\2\2\u0303\u0300\3\2\2\2\u0303\u0301\3\2\2\2\u0303"+
		"\u0302\3\2\2\2\u0304\u030d\3\2\2\2\u0305\u0306\f\13\2\2\u0306\u0309\7"+
		"\u00ee\2\2\u0307\u030a\7\u0156\2\2\u0308\u030a\5Z.\2\u0309\u0307\3\2\2"+
		"\2\u0309\u0308\3\2\2\2\u030a\u030c\3\2\2\2\u030b\u0305\3\2\2\2\u030c\u030f"+
		"\3\2\2\2\u030d\u030b\3\2\2\2\u030d\u030e\3\2\2\2\u030e\u0081\3\2\2\2\u030f"+
		"\u030d\3\2\2\2\u0310\u0314\5\u0084C\2\u0311\u0314\5\u008aF\2\u0312\u0314"+
		"\5\u0098M\2\u0313\u0310\3\2\2\2\u0313\u0311\3\2\2\2\u0313\u0312\3\2\2"+
		"\2\u0314\u0083\3\2\2\2\u0315\u0316\5\u0086D\2\u0316\u0318\7\36\2\2\u0317"+
		"\u0319\5\u0088E\2\u0318\u0317\3\2\2\2\u0318\u0319\3\2\2\2\u0319\u0323"+
		"\3\2\2\2\u031a\u031f\5r:\2\u031b\u031c\7$\2\2\u031c\u031e\5r:\2\u031d"+
		"\u031b\3\2\2\2\u031e\u0321\3\2\2\2\u031f\u031d\3\2\2\2\u031f\u0320\3\2"+
		"\2\2\u0320\u0324\3\2\2\2\u0321\u031f\3\2\2\2\u0322\u0324\7\20\2\2\u0323"+
		"\u031a\3\2\2\2\u0323\u0322\3\2\2\2\u0323\u0324\3\2\2\2\u0324\u0325\3\2"+
		"\2\2\u0325\u0326\7\37\2\2\u0326\u0085\3\2\2\2\u0327\u0328\t\20\2\2\u0328"+
		"\u0087\3\2\2\2\u0329\u032a\7J\2\2\u032a\u0089\3\2\2\2\u032b\u0332\5\u008c"+
		"G\2\u032c\u0332\5\u008eH\2\u032d\u0332\5\u0090I\2\u032e\u0332\5\u0092"+
		"J\2\u032f\u0332\5\u0094K\2\u0330\u0332\5\u0096L\2\u0331\u032b\3\2\2\2"+
		"\u0331\u032c\3\2\2\2\u0331\u032d\3\2\2\2\u0331\u032e\3\2\2\2\u0331\u032f"+
		"\3\2\2\2\u0331\u0330\3\2\2\2\u0332\u008b\3\2\2\2\u0333\u0334\7M\2\2\u0334"+
		"\u0337\7\36\2\2\u0335\u0338\5r:\2\u0336\u0338\7f\2\2\u0337\u0335\3\2\2"+
		"\2\u0337\u0336\3\2\2\2\u0338\u0339\3\2\2\2\u0339\u033a\7[\2\2\u033a\u033b"+
		"\5\u00acW\2\u033b\u033c\7\37\2\2\u033c\u008d\3\2\2\2\u033d\u033e\7\u00f3"+
		"\2\2\u033e\u033f\7\36\2\2\u033f\u0340\5r:\2\u0340\u0341\7Y\2\2\u0341\u0342"+
		"\5Z.\2\u0342\u0343\7\37\2\2\u0343\u008f\3\2\2\2\u0344\u0345\7@\2\2\u0345"+
		"\u0346\7\36\2\2\u0346\u0347\5r:\2\u0347\u0348\7k\2\2\u0348\u0349\5r:\2"+
		"\u0349\u034a\7\37\2\2\u034a\u0091\3\2\2\2\u034b\u034c\7O\2\2\u034c\u034d"+
		"\7\36\2\2\u034d\u034e\5r:\2\u034e\u034f\7P\2\2\u034f\u0352\7\u0157\2\2"+
		"\u0350\u0351\7`\2\2\u0351\u0353\7\u0157\2\2\u0352\u0350\3\2\2\2\u0352"+
		"\u0353\3\2\2\2\u0353\u0354\3\2\2\2\u0354\u0355\7\37\2\2\u0355\u0093\3"+
		"\2\2\2\u0356\u0357\7\u010b\2\2\u0357\u0358\7\36\2\2\u0358\u0359\5Z.\2"+
		"\u0359\u035a\7P\2\2\u035a\u035b\5r:\2\u035b\u035c\7\37\2\2\u035c\u0095"+
		"\3\2\2\2\u035d\u035e\7N\2\2\u035e\u035f\7\36\2\2\u035f\u0360\t\21\2\2"+
		"\u0360\u0361\7\u0156\2\2\u0361\u0362\7P\2\2\u0362\u0363\7\u0156\2\2\u0363"+
		"\u0364\7\37\2\2\u0364\u0097\3\2\2\2\u0365\u0366\5\u009aN\2\u0366\u0370"+
		"\7\36\2\2\u0367\u036c\5r:\2\u0368\u0369\7$\2\2\u0369\u036b\5r:\2\u036a"+
		"\u0368\3\2\2\2\u036b\u036e\3\2\2\2\u036c\u036a\3\2\2\2\u036c\u036d\3\2"+
		"\2\2\u036d\u0371\3\2\2\2\u036e\u036c\3\2\2\2\u036f\u0371\7\20\2\2\u0370"+
		"\u0367\3\2\2\2\u0370\u036f\3\2\2\2\u0370\u0371\3\2\2\2\u0371\u0372\3\2"+
		"\2\2\u0372\u0373\7\37\2\2\u0373\u0099\3\2\2\2\u0374\u037b\5Z.\2\u0375"+
		"\u037b\7]\2\2\u0376\u037b\7\u00f7\2\2\u0377\u037b\7\u0084\2\2\u0378\u037b"+
		"\7\u0085\2\2\u0379\u037b\7\u0080\2\2\u037a\u0374\3\2\2\2\u037a\u0375\3"+
		"\2\2\2\u037a\u0376\3\2\2\2\u037a\u0377\3\2\2\2\u037a\u0378\3\2\2\2\u037a"+
		"\u0379\3\2\2\2\u037b\u009b\3\2\2\2\u037c\u037d\5J&\2\u037d\u037f\7\u0120"+
		"\2\2\u037e\u0380\7=\2\2\u037f\u037e\3\2\2\2\u037f\u0380\3\2\2\2\u0380"+
		"\u0381\3\2\2\2\u0381\u0382\t\22\2\2\u0382\u0383\5F$\2\u0383\u009d\3\2"+
		"\2\2\u0384\u0386\7K\2\2\u0385\u0387\5\u0080A\2\u0386\u0385\3\2\2\2\u0386"+
		"\u0387\3\2\2\2\u0387\u0389\3\2\2\2\u0388\u038a\5\u00a0Q\2\u0389\u0388"+
		"\3\2\2\2\u038a\u038b\3\2\2\2\u038b\u0389\3\2\2\2\u038b\u038c\3\2\2\2\u038c"+
		"\u038e\3\2\2\2\u038d\u038f\5\u00a2R\2\u038e\u038d\3\2\2\2\u038e\u038f"+
		"\3\2\2\2\u038f\u0390\3\2\2\2\u0390\u0391\7\u0103\2\2\u0391\u009f\3\2\2"+
		"\2\u0392\u0393\7L\2\2\u0393\u0394\5r:\2\u0394\u0395\7_\2\2\u0395\u0396"+
		"\5r:\2\u0396\u00a1\3\2\2\2\u0397\u0398\7^\2\2\u0398\u0399\5r:\2\u0399"+
		"\u00a3\3\2\2\2\u039a\u039b\7\u0080\2\2\u039b\u039c\5r:\2\u039c\u039d\5"+
		"\u00a6T\2\u039d\u00a5\3\2\2\2\u039e\u039f\t\23\2\2\u039f\u00a7\3\2\2\2"+
		"\u03a0\u03a1\7o\2\2\u03a1\u03a2\7q\2\2\u03a2\u03a7\5\u00aaV\2\u03a3\u03a4"+
		"\7$\2\2\u03a4\u03a6\5\u00aaV\2\u03a5\u03a3\3\2\2\2\u03a6\u03a9\3\2\2\2"+
		"\u03a7\u03a5\3\2\2\2\u03a7\u03a8\3\2\2\2\u03a8\u00a9\3\2\2\2\u03a9\u03a7"+
		"\3\2\2\2\u03aa\u03ad\5d\63\2\u03ab\u03ad\5N(\2\u03ac\u03aa\3\2\2\2\u03ac"+
		"\u03ab\3\2\2\2\u03ad\u03af\3\2\2\2\u03ae\u03b0\t\24\2\2\u03af\u03ae\3"+
		"\2\2\2\u03af\u03b0\3\2\2\2\u03b0\u00ab\3\2\2\2\u03b1\u03b3\5\u00aeX\2"+
		"\u03b2\u03b4\5\u00b0Y\2\u03b3\u03b2\3\2\2\2\u03b3\u03b4\3\2\2\2\u03b4"+
		"\u03b6\3\2\2\2\u03b5\u03b7\5\u00b2Z\2\u03b6\u03b5\3\2\2\2\u03b6\u03b7"+
		"\3\2\2\2\u03b7\u03b9\3\2\2\2\u03b8\u03ba\5\u00b4[\2\u03b9\u03b8\3\2\2"+
		"\2\u03b9\u03ba\3\2\2\2\u03ba\u03cd\3\2\2\2\u03bb\u03bc\5\u00aeX\2\u03bc"+
		"\u03bd\7\36\2\2\u03bd\u03c2\7\u0156\2\2\u03be\u03bf\7$\2\2\u03bf\u03c1"+
		"\7\u0156\2\2\u03c0\u03be\3\2\2\2\u03c1\u03c4\3\2\2\2\u03c2\u03c0\3\2\2"+
		"\2\u03c2\u03c3\3\2\2\2\u03c3\u03c5\3\2\2\2\u03c4\u03c2\3\2\2\2\u03c5\u03c7"+
		"\7\37\2\2\u03c6\u03c8\5\u00b2Z\2\u03c7\u03c6\3\2\2\2\u03c7\u03c8\3\2\2"+
		"\2\u03c8\u03ca\3\2\2\2\u03c9\u03cb\5\u00b4[\2\u03ca\u03c9\3\2\2\2\u03ca"+
		"\u03cb\3\2\2\2\u03cb\u03cd\3\2\2\2\u03cc\u03b1\3\2\2\2\u03cc\u03bb\3\2"+
		"\2\2\u03cd\u00ad\3\2\2\2\u03ce\u03f8\7~\2\2\u03cf\u03d0\7~\2\2\u03d0\u03f8"+
		"\7\u0150\2\2\u03d1\u03d2\7\u0122\2\2\u03d2\u03f8\7~\2\2\u03d3\u03d4\7"+
		"\u0122\2\2\u03d4\u03d5\7~\2\2\u03d5\u03f8\7\u0150\2\2\u03d6\u03f8\7}\2"+
		"\2\u03d7\u03f8\7\u00ab\2\2\u03d8\u03f8\7\u0123\2\2\u03d9\u03da\7\u0122"+
		"\2\2\u03da\u03f8\7}\2\2\u03db\u03dc\7\u0122\2\2\u03dc\u03dd\7}\2\2\u03dd"+
		"\u03f8\7\u0150\2\2\u03de\u03f8\7\u00e5\2\2\u03df\u03e0\7\u00e5\2\2\u03e0"+
		"\u03f8\7\u0150\2\2\u03e1\u03f8\7\u0126\2\2\u03e2\u03f8\7\u00a9\2\2\u03e3"+
		"\u03f8\7\u00fa\2\2\u03e4\u03f8\7\u00a7\2\2\u03e5\u03f8\7\u013d\2\2\u03e6"+
		"\u03f8\7\u00ac\2\2\u03e7\u03f8\7\u00a8\2\2\u03e8\u03e9\7|\2\2\u03e9\u03f8"+
		"\7A\2\2\u03ea\u03f8\7\u0081\2\2\u03eb\u03f8\7\u0082\2\2\u03ec\u03f8\7"+
		"\u0083\2\2\u03ed\u03f8\7\u0080\2\2\u03ee\u03ef\7\u0082\2\2\u03ef\u03f0"+
		"\7H\2\2\u03f0\u03f1\7\u0082\2\2\u03f1\u03f8\7\u0154\2\2\u03f2\u03f3\7"+
		"\u0083\2\2\u03f3\u03f4\7H\2\2\u03f4\u03f5\7\u0082\2\2\u03f5\u03f8\7\u0154"+
		"\2\2\u03f6\u03f8\5Z.\2\u03f7\u03ce\3\2\2\2\u03f7\u03cf\3\2\2\2\u03f7\u03d1"+
		"\3\2\2\2\u03f7\u03d3\3\2\2\2\u03f7\u03d6\3\2\2\2\u03f7\u03d7\3\2\2\2\u03f7"+
		"\u03d8\3\2\2\2\u03f7\u03d9\3\2\2\2\u03f7\u03db\3\2\2\2\u03f7\u03de\3\2"+
		"\2\2\u03f7\u03df\3\2\2\2\u03f7\u03e1\3\2\2\2\u03f7\u03e2\3\2\2\2\u03f7"+
		"\u03e3\3\2\2\2\u03f7\u03e4\3\2\2\2\u03f7\u03e5\3\2\2\2\u03f7\u03e6\3\2"+
		"\2\2\u03f7\u03e7\3\2\2\2\u03f7\u03e8\3\2\2\2\u03f7\u03ea\3\2\2\2\u03f7"+
		"\u03eb\3\2\2\2\u03f7\u03ec\3\2\2\2\u03f7\u03ed\3\2\2\2\u03f7\u03ee\3\2"+
		"\2\2\u03f7\u03f2\3\2\2\2\u03f7\u03f6\3\2\2\2\u03f8\u00af\3\2\2\2\u03f9"+
		"\u03fa\7\36\2\2\u03fa\u03fd\7\u0157\2\2\u03fb\u03fc\7$\2\2\u03fc\u03fe"+
		"\7\u0157\2\2\u03fd\u03fb\3\2\2\2\u03fd\u03fe\3\2\2\2\u03fe\u03ff\3\2\2"+
		"\2\u03ff\u0400\7\37\2\2\u0400\u00b1\3\2\2\2\u0401\u0402\t\25\2\2\u0402"+
		"\u0404\7\67\2\2\u0403\u0405\7\27\2\2\u0404\u0403\3\2\2\2\u0404\u0405\3"+
		"\2\2\2\u0405\u0406\3\2\2\2\u0406\u0407\5\u00b6\\\2\u0407\u00b3\3\2\2\2"+
		"\u0408\u040a\7\u00ee\2\2\u0409\u040b\7\27\2\2\u040a\u0409\3\2\2\2\u040a"+
		"\u040b\3\2\2\2\u040b\u040e\3\2\2\2\u040c\u040f\7\u0156\2\2\u040d\u040f"+
		"\5\u00b6\\\2\u040e\u040c\3\2\2\2\u040e\u040d\3\2\2\2\u040f\u00b5\3\2\2"+
		"\2\u0410\u0413\5Z.\2\u0411\u0412\7\23\2\2\u0412\u0414\5Z.\2\u0413\u0411"+
		"\3\2\2\2\u0413\u0414\3\2\2\2\u0414\u00b7\3\2\2\2\u0415\u0417\t\26\2\2"+
		"\u0416\u0415\3\2\2\2\u0416\u0417\3\2\2\2\u0417\u00b9\3\2\2\2\177\u00bc"+
		"\u00c1\u00c4\u00cc\u00d0\u00d8\u00dd\u00e0\u00e4\u00ec\u00f5\u00fc\u0101"+
		"\u0108\u010d\u0110\u0117\u011c\u0123\u0128\u012b\u012e\u0131\u0134\u0137"+
		"\u013f\u0145\u014a\u014d\u0150\u0153\u0157\u0166\u016e\u0174\u0179\u017c"+
		"\u0180\u0184\u018a\u018d\u0193\u0197\u01a1\u01a5\u01ab\u01b7\u01c1\u01c8"+
		"\u01cc\u01d0\u01df\u01e2\u01e6\u01e9\u01f4\u01f7\u01fb\u01fe\u0202\u020a"+
		"\u020f\u0212\u0215\u0218\u0221\u0228\u0230\u0233\u023a\u0241\u0245\u0248"+
		"\u024f\u0253\u0260\u0268\u0275\u0284\u0286\u028d\u0294\u029d\u02a4\u02ad"+
		"\u02b3\u02b6\u02df\u02e1\u02f2\u02f8\u0303\u0309\u030d\u0313\u0318\u031f"+
		"\u0323\u0331\u0337\u0352\u036c\u0370\u037a\u037f\u0386\u038b\u038e\u03a7"+
		"\u03ac\u03af\u03b3\u03b6\u03b9\u03c2\u03c7\u03ca\u03cc\u03f7\u03fd\u0404"+
		"\u040a\u040e\u0413\u0416";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}