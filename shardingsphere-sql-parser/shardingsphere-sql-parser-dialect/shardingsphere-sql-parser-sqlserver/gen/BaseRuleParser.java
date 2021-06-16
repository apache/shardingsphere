// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sqlserver/src/main/antlr4/imports/sqlserver/BaseRule.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BaseRuleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, AND_=2, OR_=3, NOT_=4, TILDE_=5, VERTICAL_BAR_=6, AMPERSAND_=7, 
		SIGNED_LEFT_SHIFT_=8, SIGNED_RIGHT_SHIFT_=9, CARET_=10, MOD_=11, COLON_=12, 
		PLUS_=13, MINUS_=14, ASTERISK_=15, SLASH_=16, BACKSLASH_=17, DOT_=18, 
		DOT_ASTERISK_=19, SAFE_EQ_=20, DEQ_=21, EQ_=22, NEQ_=23, GT_=24, GTE_=25, 
		LT_=26, LTE_=27, POUND_=28, LP_=29, RP_=30, LBE_=31, RBE_=32, LBT_=33, 
		RBT_=34, COMMA_=35, DQ_=36, SQ_=37, BQ_=38, QUESTION_=39, AT_=40, SEMI_=41, 
		WS=42, SELECT=43, INSERT=44, UPDATE=45, DELETE=46, CREATE=47, ALTER=48, 
		DROP=49, TRUNCATE=50, SCHEMA=51, GRANT=52, REVOKE=53, ADD=54, SET=55, 
		TABLE=56, COLUMN=57, INDEX=58, CONSTRAINT=59, PRIMARY=60, UNIQUE=61, FOREIGN=62, 
		KEY=63, POSITION=64, PRECISION=65, FUNCTION=66, TRIGGER=67, PROCEDURE=68, 
		VIEW=69, INTO=70, VALUES=71, WITH=72, UNION=73, DISTINCT=74, CASE=75, 
		WHEN=76, CAST=77, TRIM=78, SUBSTRING=79, FROM=80, NATURAL=81, JOIN=82, 
		FULL=83, INNER=84, OUTER=85, LEFT=86, RIGHT=87, CROSS=88, USING=89, WHERE=90, 
		AS=91, ON=92, IF=93, ELSE=94, THEN=95, FOR=96, TO=97, AND=98, OR=99, IS=100, 
		NOT=101, NULL=102, TRUE=103, FALSE=104, EXISTS=105, BETWEEN=106, IN=107, 
		ALL=108, ANY=109, LIKE=110, ORDER=111, GROUP=112, BY=113, ASC=114, DESC=115, 
		HAVING=116, LIMIT=117, OFFSET=118, BEGIN=119, COMMIT=120, ROLLBACK=121, 
		SAVEPOINT=122, BOOLEAN=123, DOUBLE=124, CHAR=125, CHARACTER=126, ARRAY=127, 
		INTERVAL=128, DATE=129, TIME=130, TIMESTAMP=131, LOCALTIME=132, LOCALTIMESTAMP=133, 
		YEAR=134, QUARTER=135, MONTH=136, WEEK=137, DAY=138, HOUR=139, MINUTE=140, 
		SECOND=141, MICROSECOND=142, MAX=143, MIN=144, SUM=145, COUNT=146, AVG=147, 
		DEFAULT=148, CURRENT=149, ENABLE=150, DISABLE=151, CALL=152, INSTANCE=153, 
		PRESERVE=154, DO=155, DEFINER=156, CURRENT_USER=157, SQL=158, CASCADED=159, 
		LOCAL=160, CLOSE=161, OPEN=162, NEXT=163, NAME=164, COLLATION=165, NAMES=166, 
		INTEGER=167, REAL=168, DECIMAL=169, TYPE=170, BIT=171, SMALLINT=172, INT=173, 
		TINYINT=174, NUMERIC=175, FLOAT=176, BIGINT=177, TEXT=178, VARCHAR=179, 
		PERCENT=180, TIES=181, EXCEPT=182, INTERSECT=183, USE=184, MERGE=185, 
		LOOP=186, EXPAND=187, VIEWS=188, FAST=189, FORCE=190, KEEP=191, PLAN=192, 
		OPTIMIZE=193, SIMPLE=194, FORCED=195, HINT=196, FOR_GENERATOR=197, BINARY=198, 
		ESCAPE=199, HIDDEN_=200, MOD=201, PARTITION=202, PARTITIONS=203, TOP=204, 
		ROW=205, ROWS=206, UNKNOWN=207, XOR=208, ALWAYS=209, CASCADE=210, CHECK=211, 
		GENERATED=212, NO=213, OPTION=214, PRIVILEGES=215, REFERENCES=216, USER=217, 
		ROLE=218, START=219, TRANSACTION=220, ACTION=221, ALGORITHM=222, AUTO=223, 
		BLOCKERS=224, CLUSTERED=225, NONCLUSTERED=226, COLLATE=227, COLUMNSTORE=228, 
		CONTENT=229, CONVERT=230, DATABASE=231, YEARS=232, MONTHS=233, WEEKS=234, 
		DAYS=235, MINUTES=236, DENY=237, DETERMINISTIC=238, DISTRIBUTION=239, 
		DOCUMENT=240, DURABILITY=241, ENCRYPTED=242, END=243, FILESTREAM=244, 
		FILETABLE=245, FILLFACTOR=246, FOLLOWING=247, HASH=248, HEAP=249, IDENTITY=250, 
		INBOUND=251, OUTBOUND=252, UNBOUNDED=253, INFINITE=254, LOGIN=255, MASKED=256, 
		MAXDOP=257, MOVE=258, NOCHECK=259, NONE=260, OBJECT=261, OFF=262, ONLINE=263, 
		OVER=264, PAGE=265, PAUSED=266, PERIOD=267, PERSISTED=268, PRECEDING=269, 
		RANDOMIZED=270, RANGE=271, REBUILD=272, REPLICATE=273, REPLICATION=274, 
		RESUMABLE=275, ROWGUIDCOL=276, SAVE=277, SELF=278, SPARSE=279, SWITCH=280, 
		TRAN=281, TRANCOUNT=282, ZONE=283, EXECUTE=284, SESSION=285, CONNECT=286, 
		CONNECTION=287, CATALOG=288, CONTROL=289, CONCAT=290, TAKE=291, OWNERSHIP=292, 
		DEFINITION=293, APPLICATION=294, ASSEMBLY=295, SYMMETRIC=296, ASYMMETRIC=297, 
		SERVER=298, RECEIVE=299, CHANGE=300, TRACE=301, TRACKING=302, RESOURCES=303, 
		SETTINGS=304, STATE=305, AVAILABILITY=306, CREDENTIAL=307, ENDPOINT=308, 
		EVENT=309, NOTIFICATION=310, LINKED=311, AUDIT=312, DDL=313, XML=314, 
		IMPERSONATE=315, SECURABLES=316, AUTHENTICATE=317, EXTERNAL=318, ACCESS=319, 
		ADMINISTER=320, BULK=321, OPERATIONS=322, UNSAFE=323, SHUTDOWN=324, SCOPED=325, 
		CONFIGURATION=326, DATASPACE=327, SERVICE=328, CERTIFICATE=329, CONTRACT=330, 
		ENCRYPTION=331, MASTER=332, DATA=333, SOURCE=334, FILE=335, FORMAT=336, 
		LIBRARY=337, FULLTEXT=338, MASK=339, UNMASK=340, MESSAGE=341, REMOTE=342, 
		BINDING=343, ROUTE=344, SECURITY=345, POLICY=346, AGGREGATE=347, QUEUE=348, 
		RULE=349, SYNONYM=350, COLLECTION=351, SCRIPT=352, KILL=353, BACKUP=354, 
		LOG=355, SHOWPLAN=356, SUBSCRIBE=357, QUERY=358, NOTIFICATIONS=359, CHECKPOINT=360, 
		SEQUENCE=361, ABORT_AFTER_WAIT=362, ALLOW_PAGE_LOCKS=363, ALLOW_ROW_LOCKS=364, 
		ALL_SPARSE_COLUMNS=365, BUCKET_COUNT=366, COLUMNSTORE_ARCHIVE=367, COLUMN_ENCRYPTION_KEY=368, 
		COLUMN_SET=369, COMPRESSION_DELAY=370, DATABASE_DEAULT=371, DATA_COMPRESSION=372, 
		DATA_CONSISTENCY_CHECK=373, ENCRYPTION_TYPE=374, SYSTEM_TIME=375, SYSTEM_VERSIONING=376, 
		TEXTIMAGE_ON=377, WAIT_AT_LOW_PRIORITY=378, STATISTICS_INCREMENTAL=379, 
		STATISTICS_NORECOMPUTE=380, ROUND_ROBIN=381, SCHEMA_AND_DATA=382, SCHEMA_ONLY=383, 
		SORT_IN_TEMPDB=384, IGNORE_DUP_KEY=385, IMPLICIT_TRANSACTIONS=386, MAX_DURATION=387, 
		MEMORY_OPTIMIZED=388, MIGRATION_STATE=389, PAD_INDEX=390, REMOTE_DATA_ARCHIVE=391, 
		FILESTREAM_ON=392, FILETABLE_COLLATE_FILENAME=393, FILETABLE_DIRECTORY=394, 
		FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME=395, FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME=396, 
		FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME=397, FILTER_PREDICATE=398, HISTORY_RETENTION_PERIOD=399, 
		HISTORY_TABLE=400, LOCK_ESCALATION=401, DROP_EXISTING=402, ROW_NUMBER=403, 
		FETCH=404, FIRST=405, ONLY=406, MONEY=407, SMALLMONEY=408, DATETIMEOFFSET=409, 
		DATETIME=410, DATETIME2=411, SMALLDATETIME=412, NCHAR=413, NVARCHAR=414, 
		NTEXT=415, VARBINARY=416, IMAGE=417, SQL_VARIANT=418, UNIQUEIDENTIFIER=419, 
		HIERARCHYID=420, GEOMETRY=421, GEOGRAPHY=422, OUTPUT=423, INSERTED=424, 
		DELETED=425, ASSUME_JOIN_PREDICATE_DEPENDS_ON_FILTERS=426, ASSUME_MIN_SELECTIVITY_FOR_FILTER_ESTIMATES=427, 
		DISABLE_BATCH_MODE_ADAPTIVE_JOINS=428, DISABLE_BATCH_MODE_MEMORY_GRANT_FEEDBACK=429, 
		DISABLE_DEFERRED_COMPILATION_TV=430, DISABLE_INTERLEAVED_EXECUTION_TVF=431, 
		DISABLE_OPTIMIZED_NESTED_LOOP=432, DISABLE_OPTIMIZER_ROWGOAL=433, DISABLE_PARAMETER_SNIFFING=434, 
		DISABLE_ROW_MODE_MEMORY_GRANT_FEEDBACK=435, DISABLE_TSQL_SCALAR_UDF_INLINING=436, 
		DISALLOW_BATCH_MODE=437, ENABLE_HIST_AMENDMENT_FOR_ASC_KEYS=438, ENABLE_QUERY_OPTIMIZER_HOTFIXES=439, 
		FORCE_DEFAULT_CARDINALITY_ESTIMATION=440, FORCE_LEGACY_CARDINALITY_ESTIMATION=441, 
		QUERY_OPTIMIZER_COMPATIBILITY_LEVEL_n=442, QUERY_PLAN_PROFILE=443, EXTERNALPUSHDOWN=444, 
		SCALEOUTEXECUTION=445, IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX=446, KEEPFIXED=447, 
		MAX_GRANT_PERCENT=448, MIN_GRANT_PERCENT=449, MAXRECURSION=450, NO_PERFORMANCE_SPOOL=451, 
		PARAMETERIZATION=452, QUERYTRACEON=453, RECOMPILE=454, ROBUST=455, OPTIMIZE_FOR_SEQUENTIAL_KEY=456, 
		DATA_DELETION=457, FILTER_COLUMN=458, RETENTION_PERIOD=459, IDENTIFIER_=460, 
		STRING_=461, NUMBER_=462, INT_NUM_=463, FLOAT_NUM_=464, DECIMAL_NUM_=465, 
		HEX_DIGIT_=466, BIT_NUM_=467, NCHAR_TEXT=468;
	public static final int
		RULE_parameterMarker = 0, RULE_literals = 1, RULE_stringLiterals = 2, 
		RULE_numberLiterals = 3, RULE_dateTimeLiterals = 4, RULE_hexadecimalLiterals = 5, 
		RULE_bitValueLiterals = 6, RULE_booleanLiterals = 7, RULE_nullValueLiterals = 8, 
		RULE_identifier = 9, RULE_unreservedWord = 10, RULE_schemaName = 11, RULE_tableName = 12, 
		RULE_columnName = 13, RULE_owner = 14, RULE_name = 15, RULE_columnNames = 16, 
		RULE_columnNamesWithSort = 17, RULE_tableNames = 18, RULE_indexName = 19, 
		RULE_collationName = 20, RULE_alias = 21, RULE_dataTypeLength = 22, RULE_primaryKey = 23, 
		RULE_expr = 24, RULE_logicalOperator = 25, RULE_notOperator = 26, RULE_booleanPrimary = 27, 
		RULE_comparisonOperator = 28, RULE_predicate = 29, RULE_bitExpr = 30, 
		RULE_simpleExpr = 31, RULE_functionCall = 32, RULE_aggregationFunction = 33, 
		RULE_aggregationFunctionName = 34, RULE_distinct = 35, RULE_specialFunction = 36, 
		RULE_castFunction = 37, RULE_charFunction = 38, RULE_regularFunction = 39, 
		RULE_regularFunctionName = 40, RULE_caseExpression = 41, RULE_caseWhen = 42, 
		RULE_caseElse = 43, RULE_privateExprOfDb = 44, RULE_subquery = 45, RULE_orderByClause = 46, 
		RULE_orderByItem = 47, RULE_dataType = 48, RULE_dataTypeName = 49, RULE_atTimeZoneExpr = 50, 
		RULE_castExpr = 51, RULE_convertExpr = 52, RULE_windowedFunction = 53, 
		RULE_overClause = 54, RULE_partitionByClause = 55, RULE_rowRangeClause = 56, 
		RULE_windowFrameExtent = 57, RULE_windowFrameBetween = 58, RULE_windowFrameBound = 59, 
		RULE_windowFramePreceding = 60, RULE_windowFrameFollowing = 61, RULE_columnNameWithSort = 62, 
		RULE_indexOption = 63, RULE_compressionOption = 64, RULE_eqTime = 65, 
		RULE_eqOnOffOption = 66, RULE_eqKey = 67, RULE_eqOnOff = 68, RULE_onPartitionClause = 69, 
		RULE_partitionExpressions = 70, RULE_partitionExpression = 71, RULE_numberRange = 72, 
		RULE_lowPriorityLockWait = 73, RULE_onLowPriorLockWait = 74, RULE_ignoredIdentifier = 75, 
		RULE_ignoredIdentifiers = 76, RULE_matchNone = 77;
	private static String[] makeRuleNames() {
		return new String[] {
			"parameterMarker", "literals", "stringLiterals", "numberLiterals", "dateTimeLiterals", 
			"hexadecimalLiterals", "bitValueLiterals", "booleanLiterals", "nullValueLiterals", 
			"identifier", "unreservedWord", "schemaName", "tableName", "columnName", 
			"owner", "name", "columnNames", "columnNamesWithSort", "tableNames", 
			"indexName", "collationName", "alias", "dataTypeLength", "primaryKey", 
			"expr", "logicalOperator", "notOperator", "booleanPrimary", "comparisonOperator", 
			"predicate", "bitExpr", "simpleExpr", "functionCall", "aggregationFunction", 
			"aggregationFunctionName", "distinct", "specialFunction", "castFunction", 
			"charFunction", "regularFunction", "regularFunctionName", "caseExpression", 
			"caseWhen", "caseElse", "privateExprOfDb", "subquery", "orderByClause", 
			"orderByItem", "dataType", "dataTypeName", "atTimeZoneExpr", "castExpr", 
			"convertExpr", "windowedFunction", "overClause", "partitionByClause", 
			"rowRangeClause", "windowFrameExtent", "windowFrameBetween", "windowFrameBound", 
			"windowFramePreceding", "windowFrameFollowing", "columnNameWithSort", 
			"indexOption", "compressionOption", "eqTime", "eqOnOffOption", "eqKey", 
			"eqOnOff", "onPartitionClause", "partitionExpressions", "partitionExpression", 
			"numberRange", "lowPriorityLockWait", "onLowPriorLockWait", "ignoredIdentifier", 
			"ignoredIdentifiers", "matchNone"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Default does not match anything'", "'&&'", "'||'", "'!'", "'~'", 
			"'|'", "'&'", "'<<'", "'>>'", "'^'", "'%'", "':'", "'+'", "'-'", "'*'", 
			"'/'", "'\\'", "'.'", "'.*'", "'<=>'", "'=='", "'='", null, "'>'", "'>='", 
			"'<'", "'<='", "'#'", "'('", "')'", "'{'", "'}'", "'['", "']'", "','", 
			"'\"'", "'''", "'`'", "'?'", "'@'", "';'", null, null, null, null, null, 
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
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", "POUND_", 
			"LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", "SQ_", 
			"BQ_", "QUESTION_", "AT_", "SEMI_", "WS", "SELECT", "INSERT", "UPDATE", 
			"DELETE", "CREATE", "ALTER", "DROP", "TRUNCATE", "SCHEMA", "GRANT", "REVOKE", 
			"ADD", "SET", "TABLE", "COLUMN", "INDEX", "CONSTRAINT", "PRIMARY", "UNIQUE", 
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
			"NAME", "COLLATION", "NAMES", "INTEGER", "REAL", "DECIMAL", "TYPE", "BIT", 
			"SMALLINT", "INT", "TINYINT", "NUMERIC", "FLOAT", "BIGINT", "TEXT", "VARCHAR", 
			"PERCENT", "TIES", "EXCEPT", "INTERSECT", "USE", "MERGE", "LOOP", "EXPAND", 
			"VIEWS", "FAST", "FORCE", "KEEP", "PLAN", "OPTIMIZE", "SIMPLE", "FORCED", 
			"HINT", "FOR_GENERATOR", "BINARY", "ESCAPE", "HIDDEN_", "MOD", "PARTITION", 
			"PARTITIONS", "TOP", "ROW", "ROWS", "UNKNOWN", "XOR", "ALWAYS", "CASCADE", 
			"CHECK", "GENERATED", "NO", "OPTION", "PRIVILEGES", "REFERENCES", "USER", 
			"ROLE", "START", "TRANSACTION", "ACTION", "ALGORITHM", "AUTO", "BLOCKERS", 
			"CLUSTERED", "NONCLUSTERED", "COLLATE", "COLUMNSTORE", "CONTENT", "CONVERT", 
			"DATABASE", "YEARS", "MONTHS", "WEEKS", "DAYS", "MINUTES", "DENY", "DETERMINISTIC", 
			"DISTRIBUTION", "DOCUMENT", "DURABILITY", "ENCRYPTED", "END", "FILESTREAM", 
			"FILETABLE", "FILLFACTOR", "FOLLOWING", "HASH", "HEAP", "IDENTITY", "INBOUND", 
			"OUTBOUND", "UNBOUNDED", "INFINITE", "LOGIN", "MASKED", "MAXDOP", "MOVE", 
			"NOCHECK", "NONE", "OBJECT", "OFF", "ONLINE", "OVER", "PAGE", "PAUSED", 
			"PERIOD", "PERSISTED", "PRECEDING", "RANDOMIZED", "RANGE", "REBUILD", 
			"REPLICATE", "REPLICATION", "RESUMABLE", "ROWGUIDCOL", "SAVE", "SELF", 
			"SPARSE", "SWITCH", "TRAN", "TRANCOUNT", "ZONE", "EXECUTE", "SESSION", 
			"CONNECT", "CONNECTION", "CATALOG", "CONTROL", "CONCAT", "TAKE", "OWNERSHIP", 
			"DEFINITION", "APPLICATION", "ASSEMBLY", "SYMMETRIC", "ASYMMETRIC", "SERVER", 
			"RECEIVE", "CHANGE", "TRACE", "TRACKING", "RESOURCES", "SETTINGS", "STATE", 
			"AVAILABILITY", "CREDENTIAL", "ENDPOINT", "EVENT", "NOTIFICATION", "LINKED", 
			"AUDIT", "DDL", "XML", "IMPERSONATE", "SECURABLES", "AUTHENTICATE", "EXTERNAL", 
			"ACCESS", "ADMINISTER", "BULK", "OPERATIONS", "UNSAFE", "SHUTDOWN", "SCOPED", 
			"CONFIGURATION", "DATASPACE", "SERVICE", "CERTIFICATE", "CONTRACT", "ENCRYPTION", 
			"MASTER", "DATA", "SOURCE", "FILE", "FORMAT", "LIBRARY", "FULLTEXT", 
			"MASK", "UNMASK", "MESSAGE", "REMOTE", "BINDING", "ROUTE", "SECURITY", 
			"POLICY", "AGGREGATE", "QUEUE", "RULE", "SYNONYM", "COLLECTION", "SCRIPT", 
			"KILL", "BACKUP", "LOG", "SHOWPLAN", "SUBSCRIBE", "QUERY", "NOTIFICATIONS", 
			"CHECKPOINT", "SEQUENCE", "ABORT_AFTER_WAIT", "ALLOW_PAGE_LOCKS", "ALLOW_ROW_LOCKS", 
			"ALL_SPARSE_COLUMNS", "BUCKET_COUNT", "COLUMNSTORE_ARCHIVE", "COLUMN_ENCRYPTION_KEY", 
			"COLUMN_SET", "COMPRESSION_DELAY", "DATABASE_DEAULT", "DATA_COMPRESSION", 
			"DATA_CONSISTENCY_CHECK", "ENCRYPTION_TYPE", "SYSTEM_TIME", "SYSTEM_VERSIONING", 
			"TEXTIMAGE_ON", "WAIT_AT_LOW_PRIORITY", "STATISTICS_INCREMENTAL", "STATISTICS_NORECOMPUTE", 
			"ROUND_ROBIN", "SCHEMA_AND_DATA", "SCHEMA_ONLY", "SORT_IN_TEMPDB", "IGNORE_DUP_KEY", 
			"IMPLICIT_TRANSACTIONS", "MAX_DURATION", "MEMORY_OPTIMIZED", "MIGRATION_STATE", 
			"PAD_INDEX", "REMOTE_DATA_ARCHIVE", "FILESTREAM_ON", "FILETABLE_COLLATE_FILENAME", 
			"FILETABLE_DIRECTORY", "FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME", "FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME", 
			"FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME", "FILTER_PREDICATE", "HISTORY_RETENTION_PERIOD", 
			"HISTORY_TABLE", "LOCK_ESCALATION", "DROP_EXISTING", "ROW_NUMBER", "FETCH", 
			"FIRST", "ONLY", "MONEY", "SMALLMONEY", "DATETIMEOFFSET", "DATETIME", 
			"DATETIME2", "SMALLDATETIME", "NCHAR", "NVARCHAR", "NTEXT", "VARBINARY", 
			"IMAGE", "SQL_VARIANT", "UNIQUEIDENTIFIER", "HIERARCHYID", "GEOMETRY", 
			"GEOGRAPHY", "OUTPUT", "INSERTED", "DELETED", "ASSUME_JOIN_PREDICATE_DEPENDS_ON_FILTERS", 
			"ASSUME_MIN_SELECTIVITY_FOR_FILTER_ESTIMATES", "DISABLE_BATCH_MODE_ADAPTIVE_JOINS", 
			"DISABLE_BATCH_MODE_MEMORY_GRANT_FEEDBACK", "DISABLE_DEFERRED_COMPILATION_TV", 
			"DISABLE_INTERLEAVED_EXECUTION_TVF", "DISABLE_OPTIMIZED_NESTED_LOOP", 
			"DISABLE_OPTIMIZER_ROWGOAL", "DISABLE_PARAMETER_SNIFFING", "DISABLE_ROW_MODE_MEMORY_GRANT_FEEDBACK", 
			"DISABLE_TSQL_SCALAR_UDF_INLINING", "DISALLOW_BATCH_MODE", "ENABLE_HIST_AMENDMENT_FOR_ASC_KEYS", 
			"ENABLE_QUERY_OPTIMIZER_HOTFIXES", "FORCE_DEFAULT_CARDINALITY_ESTIMATION", 
			"FORCE_LEGACY_CARDINALITY_ESTIMATION", "QUERY_OPTIMIZER_COMPATIBILITY_LEVEL_n", 
			"QUERY_PLAN_PROFILE", "EXTERNALPUSHDOWN", "SCALEOUTEXECUTION", "IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX", 
			"KEEPFIXED", "MAX_GRANT_PERCENT", "MIN_GRANT_PERCENT", "MAXRECURSION", 
			"NO_PERFORMANCE_SPOOL", "PARAMETERIZATION", "QUERYTRACEON", "RECOMPILE", 
			"ROBUST", "OPTIMIZE_FOR_SEQUENTIAL_KEY", "DATA_DELETION", "FILTER_COLUMN", 
			"RETENTION_PERIOD", "IDENTIFIER_", "STRING_", "NUMBER_", "INT_NUM_", 
			"FLOAT_NUM_", "DECIMAL_NUM_", "HEX_DIGIT_", "BIT_NUM_", "NCHAR_TEXT"
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
	public String getGrammarFileName() { return "BaseRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public BaseRuleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ParameterMarkerContext extends ParserRuleContext {
		public TerminalNode QUESTION_() { return getToken(BaseRuleParser.QUESTION_, 0); }
		public ParameterMarkerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterMarker; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterParameterMarker(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitParameterMarker(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitParameterMarker(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterMarkerContext parameterMarker() throws RecognitionException {
		ParameterMarkerContext _localctx = new ParameterMarkerContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parameterMarker);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralsContext literals() throws RecognitionException {
		LiteralsContext _localctx = new LiteralsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_literals);
		try {
			setState(165);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_:
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				stringLiterals();
				}
				break;
			case MINUS_:
			case NUMBER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(159);
				numberLiterals();
				}
				break;
			case LBE_:
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 3);
				{
				setState(160);
				dateTimeLiterals();
				}
				break;
			case HEX_DIGIT_:
				enterOuterAlt(_localctx, 4);
				{
				setState(161);
				hexadecimalLiterals();
				}
				break;
			case BIT_NUM_:
				enterOuterAlt(_localctx, 5);
				{
				setState(162);
				bitValueLiterals();
				}
				break;
			case TRUE:
			case FALSE:
				enterOuterAlt(_localctx, 6);
				{
				setState(163);
				booleanLiterals();
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 7);
				{
				setState(164);
				nullValueLiterals();
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

	public static class StringLiteralsContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public StringLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterStringLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitStringLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitStringLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralsContext stringLiterals() throws RecognitionException {
		StringLiteralsContext _localctx = new StringLiteralsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_stringLiterals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
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

	public static class NumberLiteralsContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public NumberLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNumberLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNumberLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNumberLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberLiteralsContext numberLiterals() throws RecognitionException {
		NumberLiteralsContext _localctx = new NumberLiteralsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_numberLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS_) {
				{
				setState(169);
				match(MINUS_);
				}
			}

			setState(172);
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
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public TerminalNode DATE() { return getToken(BaseRuleParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(BaseRuleParser.TIMESTAMP, 0); }
		public TerminalNode LBE_() { return getToken(BaseRuleParser.LBE_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RBE_() { return getToken(BaseRuleParser.RBE_, 0); }
		public DateTimeLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDateTimeLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDateTimeLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDateTimeLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeLiteralsContext dateTimeLiterals() throws RecognitionException {
		DateTimeLiteralsContext _localctx = new DateTimeLiteralsContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dateTimeLiterals);
		int _la;
		try {
			setState(181);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 1);
				{
				setState(174);
				_la = _input.LA(1);
				if ( !(((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & ((1L << (DATE - 129)) | (1L << (TIME - 129)) | (1L << (TIMESTAMP - 129)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(175);
				match(STRING_);
				}
				break;
			case LBE_:
				enterOuterAlt(_localctx, 2);
				{
				setState(176);
				match(LBE_);
				setState(177);
				identifier();
				setState(178);
				match(STRING_);
				setState(179);
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
		public TerminalNode HEX_DIGIT_() { return getToken(BaseRuleParser.HEX_DIGIT_, 0); }
		public HexadecimalLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hexadecimalLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterHexadecimalLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitHexadecimalLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitHexadecimalLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HexadecimalLiteralsContext hexadecimalLiterals() throws RecognitionException {
		HexadecimalLiteralsContext _localctx = new HexadecimalLiteralsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_hexadecimalLiterals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(HEX_DIGIT_);
			}
		}
		catch (RecognitionException re) {
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
		public TerminalNode BIT_NUM_() { return getToken(BaseRuleParser.BIT_NUM_, 0); }
		public BitValueLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitValueLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBitValueLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBitValueLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBitValueLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitValueLiteralsContext bitValueLiterals() throws RecognitionException {
		BitValueLiteralsContext _localctx = new BitValueLiteralsContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_bitValueLiterals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			match(BIT_NUM_);
			}
		}
		catch (RecognitionException re) {
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
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public BooleanLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBooleanLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBooleanLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBooleanLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralsContext booleanLiterals() throws RecognitionException {
		BooleanLiteralsContext _localctx = new BooleanLiteralsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_booleanLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
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
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public NullValueLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullValueLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNullValueLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNullValueLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNullValueLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullValueLiteralsContext nullValueLiterals() throws RecognitionException {
		NullValueLiteralsContext _localctx = new NullValueLiteralsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_nullValueLiterals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
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
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public UnreservedWordContext unreservedWord() {
			return getRuleContext(UnreservedWordContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_identifier);
		try {
			setState(193);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(191);
				match(IDENTIFIER_);
				}
				break;
			case TRUNCATE:
			case FUNCTION:
			case TRIGGER:
			case LIMIT:
			case OFFSET:
			case SAVEPOINT:
			case BOOLEAN:
			case ARRAY:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case QUARTER:
			case WEEK:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case NEXT:
			case NAME:
			case INTEGER:
			case TYPE:
			case BINARY:
			case HIDDEN_:
			case MOD:
			case PARTITION:
			case PARTITIONS:
			case TOP:
			case ROW:
			case XOR:
			case ALWAYS:
			case ROLE:
			case START:
			case ALGORITHM:
			case AUTO:
			case BLOCKERS:
			case CLUSTERED:
			case NONCLUSTERED:
			case COLUMNSTORE:
			case CONTENT:
			case DATABASE:
			case YEARS:
			case MONTHS:
			case WEEKS:
			case DAYS:
			case MINUTES:
			case DENY:
			case DETERMINISTIC:
			case DISTRIBUTION:
			case DOCUMENT:
			case DURABILITY:
			case ENCRYPTED:
			case FILESTREAM:
			case FILETABLE:
			case FILLFACTOR:
			case FOLLOWING:
			case HASH:
			case HEAP:
			case INBOUND:
			case OUTBOUND:
			case UNBOUNDED:
			case INFINITE:
			case LOGIN:
			case MASKED:
			case MAXDOP:
			case MOVE:
			case NOCHECK:
			case OBJECT:
			case OFF:
			case ONLINE:
			case OVER:
			case PAGE:
			case PAUSED:
			case PERIOD:
			case PERSISTED:
			case PRECEDING:
			case RANDOMIZED:
			case RANGE:
			case REBUILD:
			case REPLICATE:
			case REPLICATION:
			case RESUMABLE:
			case ROWGUIDCOL:
			case SAVE:
			case SELF:
			case SPARSE:
			case SWITCH:
			case TRAN:
			case TRANCOUNT:
			case CONTROL:
			case CONCAT:
			case TAKE:
			case OWNERSHIP:
			case DEFINITION:
			case APPLICATION:
			case ASSEMBLY:
			case SYMMETRIC:
			case ASYMMETRIC:
			case SERVER:
			case RECEIVE:
			case CHANGE:
			case TRACE:
			case TRACKING:
			case RESOURCES:
			case SETTINGS:
			case STATE:
			case AVAILABILITY:
			case CREDENTIAL:
			case ENDPOINT:
			case EVENT:
			case NOTIFICATION:
			case LINKED:
			case AUDIT:
			case DDL:
			case XML:
			case IMPERSONATE:
			case SECURABLES:
			case AUTHENTICATE:
			case EXTERNAL:
			case ACCESS:
			case ADMINISTER:
			case BULK:
			case OPERATIONS:
			case UNSAFE:
			case SHUTDOWN:
			case SCOPED:
			case CONFIGURATION:
			case DATASPACE:
			case SERVICE:
			case CERTIFICATE:
			case CONTRACT:
			case ENCRYPTION:
			case MASTER:
			case DATA:
			case SOURCE:
			case FILE:
			case FORMAT:
			case LIBRARY:
			case FULLTEXT:
			case MASK:
			case UNMASK:
			case MESSAGE:
			case REMOTE:
			case BINDING:
			case ROUTE:
			case SECURITY:
			case POLICY:
			case AGGREGATE:
			case QUEUE:
			case RULE:
			case SYNONYM:
			case COLLECTION:
			case SCRIPT:
			case KILL:
			case BACKUP:
			case LOG:
			case SHOWPLAN:
			case SUBSCRIBE:
			case QUERY:
			case NOTIFICATIONS:
			case CHECKPOINT:
			case SEQUENCE:
			case ABORT_AFTER_WAIT:
			case ALLOW_PAGE_LOCKS:
			case ALLOW_ROW_LOCKS:
			case ALL_SPARSE_COLUMNS:
			case BUCKET_COUNT:
			case COLUMNSTORE_ARCHIVE:
			case COLUMN_ENCRYPTION_KEY:
			case COLUMN_SET:
			case COMPRESSION_DELAY:
			case DATABASE_DEAULT:
			case DATA_COMPRESSION:
			case DATA_CONSISTENCY_CHECK:
			case ENCRYPTION_TYPE:
			case SYSTEM_TIME:
			case SYSTEM_VERSIONING:
			case TEXTIMAGE_ON:
			case WAIT_AT_LOW_PRIORITY:
			case STATISTICS_INCREMENTAL:
			case STATISTICS_NORECOMPUTE:
			case ROUND_ROBIN:
			case SCHEMA_AND_DATA:
			case SCHEMA_ONLY:
			case SORT_IN_TEMPDB:
			case IGNORE_DUP_KEY:
			case IMPLICIT_TRANSACTIONS:
			case MAX_DURATION:
			case MEMORY_OPTIMIZED:
			case MIGRATION_STATE:
			case PAD_INDEX:
			case REMOTE_DATA_ARCHIVE:
			case FILESTREAM_ON:
			case FILETABLE_COLLATE_FILENAME:
			case FILETABLE_DIRECTORY:
			case FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME:
			case FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME:
			case FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME:
			case FILTER_PREDICATE:
			case HISTORY_RETENTION_PERIOD:
			case HISTORY_TABLE:
			case LOCK_ESCALATION:
			case DROP_EXISTING:
			case ROW_NUMBER:
			case FIRST:
			case DATETIME2:
			case OUTPUT:
			case INSERTED:
			case DELETED:
				enterOuterAlt(_localctx, 2);
				{
				setState(192);
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
		public TerminalNode TRUNCATE() { return getToken(BaseRuleParser.TRUNCATE, 0); }
		public TerminalNode FUNCTION() { return getToken(BaseRuleParser.FUNCTION, 0); }
		public TerminalNode TRIGGER() { return getToken(BaseRuleParser.TRIGGER, 0); }
		public TerminalNode LIMIT() { return getToken(BaseRuleParser.LIMIT, 0); }
		public TerminalNode OFFSET() { return getToken(BaseRuleParser.OFFSET, 0); }
		public TerminalNode SAVEPOINT() { return getToken(BaseRuleParser.SAVEPOINT, 0); }
		public TerminalNode BOOLEAN() { return getToken(BaseRuleParser.BOOLEAN, 0); }
		public TerminalNode ARRAY() { return getToken(BaseRuleParser.ARRAY, 0); }
		public TerminalNode LOCALTIME() { return getToken(BaseRuleParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(BaseRuleParser.LOCALTIMESTAMP, 0); }
		public TerminalNode QUARTER() { return getToken(BaseRuleParser.QUARTER, 0); }
		public TerminalNode WEEK() { return getToken(BaseRuleParser.WEEK, 0); }
		public TerminalNode MICROSECOND() { return getToken(BaseRuleParser.MICROSECOND, 0); }
		public TerminalNode ENABLE() { return getToken(BaseRuleParser.ENABLE, 0); }
		public TerminalNode DISABLE() { return getToken(BaseRuleParser.DISABLE, 0); }
		public TerminalNode BINARY() { return getToken(BaseRuleParser.BINARY, 0); }
		public TerminalNode HIDDEN_() { return getToken(BaseRuleParser.HIDDEN_, 0); }
		public TerminalNode MOD() { return getToken(BaseRuleParser.MOD, 0); }
		public TerminalNode PARTITION() { return getToken(BaseRuleParser.PARTITION, 0); }
		public TerminalNode TOP() { return getToken(BaseRuleParser.TOP, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public TerminalNode XOR() { return getToken(BaseRuleParser.XOR, 0); }
		public TerminalNode ALWAYS() { return getToken(BaseRuleParser.ALWAYS, 0); }
		public TerminalNode ROLE() { return getToken(BaseRuleParser.ROLE, 0); }
		public TerminalNode START() { return getToken(BaseRuleParser.START, 0); }
		public TerminalNode ALGORITHM() { return getToken(BaseRuleParser.ALGORITHM, 0); }
		public TerminalNode AUTO() { return getToken(BaseRuleParser.AUTO, 0); }
		public TerminalNode BLOCKERS() { return getToken(BaseRuleParser.BLOCKERS, 0); }
		public TerminalNode CLUSTERED() { return getToken(BaseRuleParser.CLUSTERED, 0); }
		public TerminalNode COLUMNSTORE() { return getToken(BaseRuleParser.COLUMNSTORE, 0); }
		public TerminalNode CONTENT() { return getToken(BaseRuleParser.CONTENT, 0); }
		public TerminalNode CONCAT() { return getToken(BaseRuleParser.CONCAT, 0); }
		public TerminalNode DATABASE() { return getToken(BaseRuleParser.DATABASE, 0); }
		public TerminalNode DAYS() { return getToken(BaseRuleParser.DAYS, 0); }
		public TerminalNode DENY() { return getToken(BaseRuleParser.DENY, 0); }
		public TerminalNode DETERMINISTIC() { return getToken(BaseRuleParser.DETERMINISTIC, 0); }
		public TerminalNode DISTRIBUTION() { return getToken(BaseRuleParser.DISTRIBUTION, 0); }
		public TerminalNode DOCUMENT() { return getToken(BaseRuleParser.DOCUMENT, 0); }
		public TerminalNode DURABILITY() { return getToken(BaseRuleParser.DURABILITY, 0); }
		public TerminalNode ENCRYPTED() { return getToken(BaseRuleParser.ENCRYPTED, 0); }
		public TerminalNode FILESTREAM() { return getToken(BaseRuleParser.FILESTREAM, 0); }
		public TerminalNode FILETABLE() { return getToken(BaseRuleParser.FILETABLE, 0); }
		public TerminalNode FOLLOWING() { return getToken(BaseRuleParser.FOLLOWING, 0); }
		public TerminalNode HASH() { return getToken(BaseRuleParser.HASH, 0); }
		public TerminalNode HEAP() { return getToken(BaseRuleParser.HEAP, 0); }
		public TerminalNode INBOUND() { return getToken(BaseRuleParser.INBOUND, 0); }
		public TerminalNode INFINITE() { return getToken(BaseRuleParser.INFINITE, 0); }
		public TerminalNode LOGIN() { return getToken(BaseRuleParser.LOGIN, 0); }
		public TerminalNode MASKED() { return getToken(BaseRuleParser.MASKED, 0); }
		public TerminalNode MAXDOP() { return getToken(BaseRuleParser.MAXDOP, 0); }
		public TerminalNode MINUTES() { return getToken(BaseRuleParser.MINUTES, 0); }
		public TerminalNode MONTHS() { return getToken(BaseRuleParser.MONTHS, 0); }
		public TerminalNode MOVE() { return getToken(BaseRuleParser.MOVE, 0); }
		public TerminalNode NOCHECK() { return getToken(BaseRuleParser.NOCHECK, 0); }
		public TerminalNode NONCLUSTERED() { return getToken(BaseRuleParser.NONCLUSTERED, 0); }
		public TerminalNode OBJECT() { return getToken(BaseRuleParser.OBJECT, 0); }
		public TerminalNode OFF() { return getToken(BaseRuleParser.OFF, 0); }
		public TerminalNode ONLINE() { return getToken(BaseRuleParser.ONLINE, 0); }
		public TerminalNode OUTBOUND() { return getToken(BaseRuleParser.OUTBOUND, 0); }
		public TerminalNode OVER() { return getToken(BaseRuleParser.OVER, 0); }
		public TerminalNode PAGE() { return getToken(BaseRuleParser.PAGE, 0); }
		public TerminalNode PARTITIONS() { return getToken(BaseRuleParser.PARTITIONS, 0); }
		public TerminalNode PAUSED() { return getToken(BaseRuleParser.PAUSED, 0); }
		public TerminalNode PERIOD() { return getToken(BaseRuleParser.PERIOD, 0); }
		public TerminalNode PERSISTED() { return getToken(BaseRuleParser.PERSISTED, 0); }
		public TerminalNode PRECEDING() { return getToken(BaseRuleParser.PRECEDING, 0); }
		public TerminalNode RANDOMIZED() { return getToken(BaseRuleParser.RANDOMIZED, 0); }
		public TerminalNode RANGE() { return getToken(BaseRuleParser.RANGE, 0); }
		public TerminalNode REBUILD() { return getToken(BaseRuleParser.REBUILD, 0); }
		public TerminalNode REPLICATE() { return getToken(BaseRuleParser.REPLICATE, 0); }
		public TerminalNode REPLICATION() { return getToken(BaseRuleParser.REPLICATION, 0); }
		public TerminalNode RESUMABLE() { return getToken(BaseRuleParser.RESUMABLE, 0); }
		public TerminalNode ROWGUIDCOL() { return getToken(BaseRuleParser.ROWGUIDCOL, 0); }
		public TerminalNode SAVE() { return getToken(BaseRuleParser.SAVE, 0); }
		public TerminalNode SELF() { return getToken(BaseRuleParser.SELF, 0); }
		public TerminalNode SPARSE() { return getToken(BaseRuleParser.SPARSE, 0); }
		public TerminalNode SWITCH() { return getToken(BaseRuleParser.SWITCH, 0); }
		public TerminalNode TRAN() { return getToken(BaseRuleParser.TRAN, 0); }
		public TerminalNode TRANCOUNT() { return getToken(BaseRuleParser.TRANCOUNT, 0); }
		public TerminalNode UNBOUNDED() { return getToken(BaseRuleParser.UNBOUNDED, 0); }
		public TerminalNode YEARS() { return getToken(BaseRuleParser.YEARS, 0); }
		public TerminalNode WEEKS() { return getToken(BaseRuleParser.WEEKS, 0); }
		public TerminalNode ABORT_AFTER_WAIT() { return getToken(BaseRuleParser.ABORT_AFTER_WAIT, 0); }
		public TerminalNode ALLOW_PAGE_LOCKS() { return getToken(BaseRuleParser.ALLOW_PAGE_LOCKS, 0); }
		public TerminalNode ALLOW_ROW_LOCKS() { return getToken(BaseRuleParser.ALLOW_ROW_LOCKS, 0); }
		public TerminalNode ALL_SPARSE_COLUMNS() { return getToken(BaseRuleParser.ALL_SPARSE_COLUMNS, 0); }
		public TerminalNode BUCKET_COUNT() { return getToken(BaseRuleParser.BUCKET_COUNT, 0); }
		public TerminalNode COLUMNSTORE_ARCHIVE() { return getToken(BaseRuleParser.COLUMNSTORE_ARCHIVE, 0); }
		public TerminalNode COLUMN_ENCRYPTION_KEY() { return getToken(BaseRuleParser.COLUMN_ENCRYPTION_KEY, 0); }
		public TerminalNode COLUMN_SET() { return getToken(BaseRuleParser.COLUMN_SET, 0); }
		public TerminalNode COMPRESSION_DELAY() { return getToken(BaseRuleParser.COMPRESSION_DELAY, 0); }
		public TerminalNode DATABASE_DEAULT() { return getToken(BaseRuleParser.DATABASE_DEAULT, 0); }
		public TerminalNode DATA_COMPRESSION() { return getToken(BaseRuleParser.DATA_COMPRESSION, 0); }
		public TerminalNode DATA_CONSISTENCY_CHECK() { return getToken(BaseRuleParser.DATA_CONSISTENCY_CHECK, 0); }
		public TerminalNode ENCRYPTION_TYPE() { return getToken(BaseRuleParser.ENCRYPTION_TYPE, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(BaseRuleParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSIONING() { return getToken(BaseRuleParser.SYSTEM_VERSIONING, 0); }
		public TerminalNode TEXTIMAGE_ON() { return getToken(BaseRuleParser.TEXTIMAGE_ON, 0); }
		public TerminalNode WAIT_AT_LOW_PRIORITY() { return getToken(BaseRuleParser.WAIT_AT_LOW_PRIORITY, 0); }
		public TerminalNode STATISTICS_INCREMENTAL() { return getToken(BaseRuleParser.STATISTICS_INCREMENTAL, 0); }
		public TerminalNode STATISTICS_NORECOMPUTE() { return getToken(BaseRuleParser.STATISTICS_NORECOMPUTE, 0); }
		public TerminalNode ROUND_ROBIN() { return getToken(BaseRuleParser.ROUND_ROBIN, 0); }
		public TerminalNode SCHEMA_AND_DATA() { return getToken(BaseRuleParser.SCHEMA_AND_DATA, 0); }
		public TerminalNode SCHEMA_ONLY() { return getToken(BaseRuleParser.SCHEMA_ONLY, 0); }
		public TerminalNode SORT_IN_TEMPDB() { return getToken(BaseRuleParser.SORT_IN_TEMPDB, 0); }
		public TerminalNode IGNORE_DUP_KEY() { return getToken(BaseRuleParser.IGNORE_DUP_KEY, 0); }
		public TerminalNode IMPLICIT_TRANSACTIONS() { return getToken(BaseRuleParser.IMPLICIT_TRANSACTIONS, 0); }
		public TerminalNode MAX_DURATION() { return getToken(BaseRuleParser.MAX_DURATION, 0); }
		public TerminalNode MEMORY_OPTIMIZED() { return getToken(BaseRuleParser.MEMORY_OPTIMIZED, 0); }
		public TerminalNode MIGRATION_STATE() { return getToken(BaseRuleParser.MIGRATION_STATE, 0); }
		public TerminalNode PAD_INDEX() { return getToken(BaseRuleParser.PAD_INDEX, 0); }
		public TerminalNode REMOTE_DATA_ARCHIVE() { return getToken(BaseRuleParser.REMOTE_DATA_ARCHIVE, 0); }
		public TerminalNode FILESTREAM_ON() { return getToken(BaseRuleParser.FILESTREAM_ON, 0); }
		public TerminalNode FILETABLE_COLLATE_FILENAME() { return getToken(BaseRuleParser.FILETABLE_COLLATE_FILENAME, 0); }
		public TerminalNode FILETABLE_DIRECTORY() { return getToken(BaseRuleParser.FILETABLE_DIRECTORY, 0); }
		public TerminalNode FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME() { return getToken(BaseRuleParser.FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME, 0); }
		public TerminalNode FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME() { return getToken(BaseRuleParser.FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME, 0); }
		public TerminalNode FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME() { return getToken(BaseRuleParser.FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME, 0); }
		public TerminalNode FILLFACTOR() { return getToken(BaseRuleParser.FILLFACTOR, 0); }
		public TerminalNode FILTER_PREDICATE() { return getToken(BaseRuleParser.FILTER_PREDICATE, 0); }
		public TerminalNode HISTORY_RETENTION_PERIOD() { return getToken(BaseRuleParser.HISTORY_RETENTION_PERIOD, 0); }
		public TerminalNode HISTORY_TABLE() { return getToken(BaseRuleParser.HISTORY_TABLE, 0); }
		public TerminalNode LOCK_ESCALATION() { return getToken(BaseRuleParser.LOCK_ESCALATION, 0); }
		public TerminalNode DROP_EXISTING() { return getToken(BaseRuleParser.DROP_EXISTING, 0); }
		public TerminalNode ROW_NUMBER() { return getToken(BaseRuleParser.ROW_NUMBER, 0); }
		public TerminalNode CONTROL() { return getToken(BaseRuleParser.CONTROL, 0); }
		public TerminalNode TAKE() { return getToken(BaseRuleParser.TAKE, 0); }
		public TerminalNode OWNERSHIP() { return getToken(BaseRuleParser.OWNERSHIP, 0); }
		public TerminalNode DEFINITION() { return getToken(BaseRuleParser.DEFINITION, 0); }
		public TerminalNode APPLICATION() { return getToken(BaseRuleParser.APPLICATION, 0); }
		public TerminalNode ASSEMBLY() { return getToken(BaseRuleParser.ASSEMBLY, 0); }
		public TerminalNode SYMMETRIC() { return getToken(BaseRuleParser.SYMMETRIC, 0); }
		public TerminalNode ASYMMETRIC() { return getToken(BaseRuleParser.ASYMMETRIC, 0); }
		public TerminalNode SERVER() { return getToken(BaseRuleParser.SERVER, 0); }
		public TerminalNode RECEIVE() { return getToken(BaseRuleParser.RECEIVE, 0); }
		public TerminalNode CHANGE() { return getToken(BaseRuleParser.CHANGE, 0); }
		public TerminalNode TRACE() { return getToken(BaseRuleParser.TRACE, 0); }
		public TerminalNode TRACKING() { return getToken(BaseRuleParser.TRACKING, 0); }
		public TerminalNode RESOURCES() { return getToken(BaseRuleParser.RESOURCES, 0); }
		public TerminalNode SETTINGS() { return getToken(BaseRuleParser.SETTINGS, 0); }
		public TerminalNode STATE() { return getToken(BaseRuleParser.STATE, 0); }
		public TerminalNode AVAILABILITY() { return getToken(BaseRuleParser.AVAILABILITY, 0); }
		public TerminalNode CREDENTIAL() { return getToken(BaseRuleParser.CREDENTIAL, 0); }
		public TerminalNode ENDPOINT() { return getToken(BaseRuleParser.ENDPOINT, 0); }
		public TerminalNode EVENT() { return getToken(BaseRuleParser.EVENT, 0); }
		public TerminalNode NOTIFICATION() { return getToken(BaseRuleParser.NOTIFICATION, 0); }
		public TerminalNode LINKED() { return getToken(BaseRuleParser.LINKED, 0); }
		public TerminalNode AUDIT() { return getToken(BaseRuleParser.AUDIT, 0); }
		public TerminalNode DDL() { return getToken(BaseRuleParser.DDL, 0); }
		public TerminalNode SQL() { return getToken(BaseRuleParser.SQL, 0); }
		public TerminalNode XML() { return getToken(BaseRuleParser.XML, 0); }
		public TerminalNode IMPERSONATE() { return getToken(BaseRuleParser.IMPERSONATE, 0); }
		public TerminalNode SECURABLES() { return getToken(BaseRuleParser.SECURABLES, 0); }
		public TerminalNode AUTHENTICATE() { return getToken(BaseRuleParser.AUTHENTICATE, 0); }
		public TerminalNode EXTERNAL() { return getToken(BaseRuleParser.EXTERNAL, 0); }
		public TerminalNode ACCESS() { return getToken(BaseRuleParser.ACCESS, 0); }
		public TerminalNode ADMINISTER() { return getToken(BaseRuleParser.ADMINISTER, 0); }
		public TerminalNode BULK() { return getToken(BaseRuleParser.BULK, 0); }
		public TerminalNode OPERATIONS() { return getToken(BaseRuleParser.OPERATIONS, 0); }
		public TerminalNode UNSAFE() { return getToken(BaseRuleParser.UNSAFE, 0); }
		public TerminalNode SHUTDOWN() { return getToken(BaseRuleParser.SHUTDOWN, 0); }
		public TerminalNode SCOPED() { return getToken(BaseRuleParser.SCOPED, 0); }
		public TerminalNode CONFIGURATION() { return getToken(BaseRuleParser.CONFIGURATION, 0); }
		public TerminalNode DATASPACE() { return getToken(BaseRuleParser.DATASPACE, 0); }
		public TerminalNode SERVICE() { return getToken(BaseRuleParser.SERVICE, 0); }
		public TerminalNode CERTIFICATE() { return getToken(BaseRuleParser.CERTIFICATE, 0); }
		public TerminalNode CONTRACT() { return getToken(BaseRuleParser.CONTRACT, 0); }
		public TerminalNode ENCRYPTION() { return getToken(BaseRuleParser.ENCRYPTION, 0); }
		public TerminalNode MASTER() { return getToken(BaseRuleParser.MASTER, 0); }
		public TerminalNode DATA() { return getToken(BaseRuleParser.DATA, 0); }
		public TerminalNode SOURCE() { return getToken(BaseRuleParser.SOURCE, 0); }
		public TerminalNode FILE() { return getToken(BaseRuleParser.FILE, 0); }
		public TerminalNode FORMAT() { return getToken(BaseRuleParser.FORMAT, 0); }
		public TerminalNode LIBRARY() { return getToken(BaseRuleParser.LIBRARY, 0); }
		public TerminalNode FULLTEXT() { return getToken(BaseRuleParser.FULLTEXT, 0); }
		public TerminalNode MASK() { return getToken(BaseRuleParser.MASK, 0); }
		public TerminalNode UNMASK() { return getToken(BaseRuleParser.UNMASK, 0); }
		public TerminalNode MESSAGE() { return getToken(BaseRuleParser.MESSAGE, 0); }
		public TerminalNode TYPE() { return getToken(BaseRuleParser.TYPE, 0); }
		public TerminalNode REMOTE() { return getToken(BaseRuleParser.REMOTE, 0); }
		public TerminalNode BINDING() { return getToken(BaseRuleParser.BINDING, 0); }
		public TerminalNode ROUTE() { return getToken(BaseRuleParser.ROUTE, 0); }
		public TerminalNode SECURITY() { return getToken(BaseRuleParser.SECURITY, 0); }
		public TerminalNode POLICY() { return getToken(BaseRuleParser.POLICY, 0); }
		public TerminalNode AGGREGATE() { return getToken(BaseRuleParser.AGGREGATE, 0); }
		public TerminalNode QUEUE() { return getToken(BaseRuleParser.QUEUE, 0); }
		public TerminalNode RULE() { return getToken(BaseRuleParser.RULE, 0); }
		public TerminalNode SYNONYM() { return getToken(BaseRuleParser.SYNONYM, 0); }
		public TerminalNode COLLECTION() { return getToken(BaseRuleParser.COLLECTION, 0); }
		public TerminalNode SCRIPT() { return getToken(BaseRuleParser.SCRIPT, 0); }
		public TerminalNode KILL() { return getToken(BaseRuleParser.KILL, 0); }
		public TerminalNode BACKUP() { return getToken(BaseRuleParser.BACKUP, 0); }
		public TerminalNode LOG() { return getToken(BaseRuleParser.LOG, 0); }
		public TerminalNode SHOWPLAN() { return getToken(BaseRuleParser.SHOWPLAN, 0); }
		public TerminalNode SUBSCRIBE() { return getToken(BaseRuleParser.SUBSCRIBE, 0); }
		public TerminalNode QUERY() { return getToken(BaseRuleParser.QUERY, 0); }
		public TerminalNode NOTIFICATIONS() { return getToken(BaseRuleParser.NOTIFICATIONS, 0); }
		public TerminalNode CHECKPOINT() { return getToken(BaseRuleParser.CHECKPOINT, 0); }
		public TerminalNode SEQUENCE() { return getToken(BaseRuleParser.SEQUENCE, 0); }
		public TerminalNode INSTANCE() { return getToken(BaseRuleParser.INSTANCE, 0); }
		public TerminalNode DO() { return getToken(BaseRuleParser.DO, 0); }
		public TerminalNode DEFINER() { return getToken(BaseRuleParser.DEFINER, 0); }
		public TerminalNode LOCAL() { return getToken(BaseRuleParser.LOCAL, 0); }
		public TerminalNode CASCADED() { return getToken(BaseRuleParser.CASCADED, 0); }
		public TerminalNode NEXT() { return getToken(BaseRuleParser.NEXT, 0); }
		public TerminalNode NAME() { return getToken(BaseRuleParser.NAME, 0); }
		public TerminalNode INTEGER() { return getToken(BaseRuleParser.INTEGER, 0); }
		public TerminalNode MAX() { return getToken(BaseRuleParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(BaseRuleParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(BaseRuleParser.SUM, 0); }
		public TerminalNode COUNT() { return getToken(BaseRuleParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(BaseRuleParser.AVG, 0); }
		public TerminalNode FIRST() { return getToken(BaseRuleParser.FIRST, 0); }
		public TerminalNode DATETIME2() { return getToken(BaseRuleParser.DATETIME2, 0); }
		public TerminalNode OUTPUT() { return getToken(BaseRuleParser.OUTPUT, 0); }
		public TerminalNode INSERTED() { return getToken(BaseRuleParser.INSERTED, 0); }
		public TerminalNode DELETED() { return getToken(BaseRuleParser.DELETED, 0); }
		public UnreservedWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unreservedWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterUnreservedWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitUnreservedWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitUnreservedWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnreservedWordContext unreservedWord() throws RecognitionException {
		UnreservedWordContext _localctx = new UnreservedWordContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_unreservedWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(195);
			_la = _input.LA(1);
			if ( !(((((_la - 50)) & ~0x3f) == 0 && ((1L << (_la - 50)) & ((1L << (TRUNCATE - 50)) | (1L << (FUNCTION - 50)) | (1L << (TRIGGER - 50)))) != 0) || ((((_la - 117)) & ~0x3f) == 0 && ((1L << (_la - 117)) & ((1L << (LIMIT - 117)) | (1L << (OFFSET - 117)) | (1L << (SAVEPOINT - 117)) | (1L << (BOOLEAN - 117)) | (1L << (ARRAY - 117)) | (1L << (LOCALTIME - 117)) | (1L << (LOCALTIMESTAMP - 117)) | (1L << (QUARTER - 117)) | (1L << (WEEK - 117)) | (1L << (MICROSECOND - 117)) | (1L << (MAX - 117)) | (1L << (MIN - 117)) | (1L << (SUM - 117)) | (1L << (COUNT - 117)) | (1L << (AVG - 117)) | (1L << (ENABLE - 117)) | (1L << (DISABLE - 117)) | (1L << (INSTANCE - 117)) | (1L << (DO - 117)) | (1L << (DEFINER - 117)) | (1L << (SQL - 117)) | (1L << (CASCADED - 117)) | (1L << (LOCAL - 117)) | (1L << (NEXT - 117)) | (1L << (NAME - 117)) | (1L << (INTEGER - 117)) | (1L << (TYPE - 117)))) != 0) || ((((_la - 198)) & ~0x3f) == 0 && ((1L << (_la - 198)) & ((1L << (BINARY - 198)) | (1L << (HIDDEN_ - 198)) | (1L << (MOD - 198)) | (1L << (PARTITION - 198)) | (1L << (PARTITIONS - 198)) | (1L << (TOP - 198)) | (1L << (ROW - 198)) | (1L << (XOR - 198)) | (1L << (ALWAYS - 198)) | (1L << (ROLE - 198)) | (1L << (START - 198)) | (1L << (ALGORITHM - 198)) | (1L << (AUTO - 198)) | (1L << (BLOCKERS - 198)) | (1L << (CLUSTERED - 198)) | (1L << (NONCLUSTERED - 198)) | (1L << (COLUMNSTORE - 198)) | (1L << (CONTENT - 198)) | (1L << (DATABASE - 198)) | (1L << (YEARS - 198)) | (1L << (MONTHS - 198)) | (1L << (WEEKS - 198)) | (1L << (DAYS - 198)) | (1L << (MINUTES - 198)) | (1L << (DENY - 198)) | (1L << (DETERMINISTIC - 198)) | (1L << (DISTRIBUTION - 198)) | (1L << (DOCUMENT - 198)) | (1L << (DURABILITY - 198)) | (1L << (ENCRYPTED - 198)) | (1L << (FILESTREAM - 198)) | (1L << (FILETABLE - 198)) | (1L << (FILLFACTOR - 198)) | (1L << (FOLLOWING - 198)) | (1L << (HASH - 198)) | (1L << (HEAP - 198)) | (1L << (INBOUND - 198)) | (1L << (OUTBOUND - 198)) | (1L << (UNBOUNDED - 198)) | (1L << (INFINITE - 198)) | (1L << (LOGIN - 198)) | (1L << (MASKED - 198)) | (1L << (MAXDOP - 198)) | (1L << (MOVE - 198)) | (1L << (NOCHECK - 198)) | (1L << (OBJECT - 198)))) != 0) || ((((_la - 262)) & ~0x3f) == 0 && ((1L << (_la - 262)) & ((1L << (OFF - 262)) | (1L << (ONLINE - 262)) | (1L << (OVER - 262)) | (1L << (PAGE - 262)) | (1L << (PAUSED - 262)) | (1L << (PERIOD - 262)) | (1L << (PERSISTED - 262)) | (1L << (PRECEDING - 262)) | (1L << (RANDOMIZED - 262)) | (1L << (RANGE - 262)) | (1L << (REBUILD - 262)) | (1L << (REPLICATE - 262)) | (1L << (REPLICATION - 262)) | (1L << (RESUMABLE - 262)) | (1L << (ROWGUIDCOL - 262)) | (1L << (SAVE - 262)) | (1L << (SELF - 262)) | (1L << (SPARSE - 262)) | (1L << (SWITCH - 262)) | (1L << (TRAN - 262)) | (1L << (TRANCOUNT - 262)) | (1L << (CONTROL - 262)) | (1L << (CONCAT - 262)) | (1L << (TAKE - 262)) | (1L << (OWNERSHIP - 262)) | (1L << (DEFINITION - 262)) | (1L << (APPLICATION - 262)) | (1L << (ASSEMBLY - 262)) | (1L << (SYMMETRIC - 262)) | (1L << (ASYMMETRIC - 262)) | (1L << (SERVER - 262)) | (1L << (RECEIVE - 262)) | (1L << (CHANGE - 262)) | (1L << (TRACE - 262)) | (1L << (TRACKING - 262)) | (1L << (RESOURCES - 262)) | (1L << (SETTINGS - 262)) | (1L << (STATE - 262)) | (1L << (AVAILABILITY - 262)) | (1L << (CREDENTIAL - 262)) | (1L << (ENDPOINT - 262)) | (1L << (EVENT - 262)) | (1L << (NOTIFICATION - 262)) | (1L << (LINKED - 262)) | (1L << (AUDIT - 262)) | (1L << (DDL - 262)) | (1L << (XML - 262)) | (1L << (IMPERSONATE - 262)) | (1L << (SECURABLES - 262)) | (1L << (AUTHENTICATE - 262)) | (1L << (EXTERNAL - 262)) | (1L << (ACCESS - 262)) | (1L << (ADMINISTER - 262)) | (1L << (BULK - 262)) | (1L << (OPERATIONS - 262)) | (1L << (UNSAFE - 262)) | (1L << (SHUTDOWN - 262)) | (1L << (SCOPED - 262)))) != 0) || ((((_la - 326)) & ~0x3f) == 0 && ((1L << (_la - 326)) & ((1L << (CONFIGURATION - 326)) | (1L << (DATASPACE - 326)) | (1L << (SERVICE - 326)) | (1L << (CERTIFICATE - 326)) | (1L << (CONTRACT - 326)) | (1L << (ENCRYPTION - 326)) | (1L << (MASTER - 326)) | (1L << (DATA - 326)) | (1L << (SOURCE - 326)) | (1L << (FILE - 326)) | (1L << (FORMAT - 326)) | (1L << (LIBRARY - 326)) | (1L << (FULLTEXT - 326)) | (1L << (MASK - 326)) | (1L << (UNMASK - 326)) | (1L << (MESSAGE - 326)) | (1L << (REMOTE - 326)) | (1L << (BINDING - 326)) | (1L << (ROUTE - 326)) | (1L << (SECURITY - 326)) | (1L << (POLICY - 326)) | (1L << (AGGREGATE - 326)) | (1L << (QUEUE - 326)) | (1L << (RULE - 326)) | (1L << (SYNONYM - 326)) | (1L << (COLLECTION - 326)) | (1L << (SCRIPT - 326)) | (1L << (KILL - 326)) | (1L << (BACKUP - 326)) | (1L << (LOG - 326)) | (1L << (SHOWPLAN - 326)) | (1L << (SUBSCRIBE - 326)) | (1L << (QUERY - 326)) | (1L << (NOTIFICATIONS - 326)) | (1L << (CHECKPOINT - 326)) | (1L << (SEQUENCE - 326)) | (1L << (ABORT_AFTER_WAIT - 326)) | (1L << (ALLOW_PAGE_LOCKS - 326)) | (1L << (ALLOW_ROW_LOCKS - 326)) | (1L << (ALL_SPARSE_COLUMNS - 326)) | (1L << (BUCKET_COUNT - 326)) | (1L << (COLUMNSTORE_ARCHIVE - 326)) | (1L << (COLUMN_ENCRYPTION_KEY - 326)) | (1L << (COLUMN_SET - 326)) | (1L << (COMPRESSION_DELAY - 326)) | (1L << (DATABASE_DEAULT - 326)) | (1L << (DATA_COMPRESSION - 326)) | (1L << (DATA_CONSISTENCY_CHECK - 326)) | (1L << (ENCRYPTION_TYPE - 326)) | (1L << (SYSTEM_TIME - 326)) | (1L << (SYSTEM_VERSIONING - 326)) | (1L << (TEXTIMAGE_ON - 326)) | (1L << (WAIT_AT_LOW_PRIORITY - 326)) | (1L << (STATISTICS_INCREMENTAL - 326)) | (1L << (STATISTICS_NORECOMPUTE - 326)) | (1L << (ROUND_ROBIN - 326)) | (1L << (SCHEMA_AND_DATA - 326)) | (1L << (SCHEMA_ONLY - 326)) | (1L << (SORT_IN_TEMPDB - 326)) | (1L << (IGNORE_DUP_KEY - 326)) | (1L << (IMPLICIT_TRANSACTIONS - 326)) | (1L << (MAX_DURATION - 326)) | (1L << (MEMORY_OPTIMIZED - 326)) | (1L << (MIGRATION_STATE - 326)))) != 0) || ((((_la - 390)) & ~0x3f) == 0 && ((1L << (_la - 390)) & ((1L << (PAD_INDEX - 390)) | (1L << (REMOTE_DATA_ARCHIVE - 390)) | (1L << (FILESTREAM_ON - 390)) | (1L << (FILETABLE_COLLATE_FILENAME - 390)) | (1L << (FILETABLE_DIRECTORY - 390)) | (1L << (FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME - 390)) | (1L << (FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME - 390)) | (1L << (FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME - 390)) | (1L << (FILTER_PREDICATE - 390)) | (1L << (HISTORY_RETENTION_PERIOD - 390)) | (1L << (HISTORY_TABLE - 390)) | (1L << (LOCK_ESCALATION - 390)) | (1L << (DROP_EXISTING - 390)) | (1L << (ROW_NUMBER - 390)) | (1L << (FIRST - 390)) | (1L << (DATETIME2 - 390)) | (1L << (OUTPUT - 390)) | (1L << (INSERTED - 390)) | (1L << (DELETED - 390)))) != 0)) ) {
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSchemaName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSchemaName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSchemaName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
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
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(199);
				owner();
				setState(200);
				match(DOT_);
				}
				break;
			}
			setState(204);
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
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(206);
				owner();
				setState(207);
				match(DOT_);
				}
				break;
			}
			setState(211);
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOwner(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOwner(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOwner(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OwnerContext owner() throws RecognitionException {
		OwnerContext _localctx = new OwnerContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_owner);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213);
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
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
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public ColumnNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnNames; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnNames(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnNames(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnNames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNamesContext columnNames() throws RecognitionException {
		ColumnNamesContext _localctx = new ColumnNamesContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_columnNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			match(LP_);
			setState(218);
			columnName();
			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(219);
				match(COMMA_);
				setState(220);
				columnName();
				}
				}
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(226);
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

	public static class ColumnNamesWithSortContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<ColumnNameWithSortContext> columnNameWithSort() {
			return getRuleContexts(ColumnNameWithSortContext.class);
		}
		public ColumnNameWithSortContext columnNameWithSort(int i) {
			return getRuleContext(ColumnNameWithSortContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public ColumnNamesWithSortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnNamesWithSort; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnNamesWithSort(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnNamesWithSort(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnNamesWithSort(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNamesWithSortContext columnNamesWithSort() throws RecognitionException {
		ColumnNamesWithSortContext _localctx = new ColumnNamesWithSortContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_columnNamesWithSort);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			match(LP_);
			setState(229);
			columnNameWithSort();
			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(230);
				match(COMMA_);
				setState(231);
				columnNameWithSort();
				}
				}
				setState(236);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(237);
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

	public static class TableNamesContext extends ParserRuleContext {
		public List<TableNameContext> tableName() {
			return getRuleContexts(TableNameContext.class);
		}
		public TableNameContext tableName(int i) {
			return getRuleContext(TableNameContext.class,i);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TableNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableNames; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTableNames(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTableNames(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTableNames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNamesContext tableNames() throws RecognitionException {
		TableNamesContext _localctx = new TableNamesContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_tableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(239);
				match(LP_);
				}
			}

			setState(242);
			tableName();
			setState(247);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(243);
				match(COMMA_);
				setState(244);
				tableName();
				}
				}
				setState(249);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(251);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RP_) {
				{
				setState(250);
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

	public static class IndexNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public IndexNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndexName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndexName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndexName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexNameContext indexName() throws RecognitionException {
		IndexNameContext _localctx = new IndexNameContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(253);
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

	public static class CollationNameContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public CollationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collationName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCollationName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCollationName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCollationName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CollationNameContext collationName() throws RecognitionException {
		CollationNameContext _localctx = new CollationNameContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_collationName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(255);
			_la = _input.LA(1);
			if ( !(_la==IDENTIFIER_ || _la==STRING_) ) {
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

	public static class AliasContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
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

	public static class DataTypeLengthContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(BaseRuleParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(BaseRuleParser.NUMBER_, i);
		}
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public DataTypeLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDataTypeLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDataTypeLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDataTypeLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeLengthContext dataTypeLength() throws RecognitionException {
		DataTypeLengthContext _localctx = new DataTypeLengthContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_dataTypeLength);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			match(LP_);
			setState(265);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NUMBER_) {
				{
				setState(260);
				match(NUMBER_);
				setState(263);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(261);
					match(COMMA_);
					setState(262);
					match(NUMBER_);
					}
				}

				}
			}

			setState(267);
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

	public static class PrimaryKeyContext extends ParserRuleContext {
		public TerminalNode KEY() { return getToken(BaseRuleParser.KEY, 0); }
		public TerminalNode PRIMARY() { return getToken(BaseRuleParser.PRIMARY, 0); }
		public PrimaryKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPrimaryKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPrimaryKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPrimaryKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryKeyContext primaryKey() throws RecognitionException {
		PrimaryKeyContext _localctx = new PrimaryKeyContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_primaryKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PRIMARY) {
				{
				setState(269);
				match(PRIMARY);
				}
			}

			setState(272);
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
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitExpr(this);
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
		int _startState = 48;
		enterRecursionRule(_localctx, 48, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(283);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(275);
				notOperator();
				setState(276);
				expr(3);
				}
				break;
			case 2:
				{
				setState(278);
				match(LP_);
				setState(279);
				expr(0);
				setState(280);
				match(RP_);
				}
				break;
			case 3:
				{
				setState(282);
				booleanPrimary(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(291);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(285);
					if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
					setState(286);
					logicalOperator();
					setState(287);
					expr(5);
					}
					} 
				}
				setState(293);
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

	public static class LogicalOperatorContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(BaseRuleParser.OR, 0); }
		public TerminalNode OR_() { return getToken(BaseRuleParser.OR_, 0); }
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public TerminalNode AND_() { return getToken(BaseRuleParser.AND_, 0); }
		public LogicalOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLogicalOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLogicalOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLogicalOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOperatorContext logicalOperator() throws RecognitionException {
		LogicalOperatorContext _localctx = new LogicalOperatorContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_logicalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(294);
			_la = _input.LA(1);
			if ( !(_la==AND_ || _la==OR_ || _la==AND || _la==OR) ) {
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
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode NOT_() { return getToken(BaseRuleParser.NOT_, 0); }
		public NotOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNotOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNotOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNotOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotOperatorContext notOperator() throws RecognitionException {
		NotOperatorContext _localctx = new NotOperatorContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_notOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(296);
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
		public TerminalNode IS() { return getToken(BaseRuleParser.IS, 0); }
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(BaseRuleParser.UNKNOWN, 0); }
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode SAFE_EQ_() { return getToken(BaseRuleParser.SAFE_EQ_, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(BaseRuleParser.ANY, 0); }
		public BooleanPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanPrimary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBooleanPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBooleanPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBooleanPrimary(this);
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
		int _startState = 54;
		enterRecursionRule(_localctx, 54, RULE_booleanPrimary, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(299);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(321);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(319);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
					case 1:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(301);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(302);
						match(IS);
						setState(304);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(303);
							match(NOT);
							}
						}

						setState(306);
						_la = _input.LA(1);
						if ( !(((((_la - 102)) & ~0x3f) == 0 && ((1L << (_la - 102)) & ((1L << (NULL - 102)) | (1L << (TRUE - 102)) | (1L << (FALSE - 102)))) != 0) || _la==UNKNOWN) ) {
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
						setState(307);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(308);
						match(SAFE_EQ_);
						setState(309);
						predicate();
						}
						break;
					case 3:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(310);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(311);
						comparisonOperator();
						setState(312);
						predicate();
						}
						break;
					case 4:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(314);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(315);
						comparisonOperator();
						setState(316);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(317);
						subquery();
						}
						break;
					}
					} 
				}
				setState(323);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
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
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode GTE_() { return getToken(BaseRuleParser.GTE_, 0); }
		public TerminalNode GT_() { return getToken(BaseRuleParser.GT_, 0); }
		public TerminalNode LTE_() { return getToken(BaseRuleParser.LTE_, 0); }
		public TerminalNode LT_() { return getToken(BaseRuleParser.LT_, 0); }
		public TerminalNode NEQ_() { return getToken(BaseRuleParser.NEQ_, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
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
		public TerminalNode IN() { return getToken(BaseRuleParser.IN, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TerminalNode BETWEEN() { return getToken(BaseRuleParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode LIKE() { return getToken(BaseRuleParser.LIKE, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode ESCAPE() { return getToken(BaseRuleParser.ESCAPE, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_predicate);
		int _la;
		try {
			setState(369);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(326);
				bitExpr(0);
				setState(328);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(327);
					match(NOT);
					}
				}

				setState(330);
				match(IN);
				setState(331);
				subquery();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(333);
				bitExpr(0);
				setState(335);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(334);
					match(NOT);
					}
				}

				setState(337);
				match(IN);
				setState(338);
				match(LP_);
				setState(339);
				expr(0);
				setState(344);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(340);
					match(COMMA_);
					setState(341);
					expr(0);
					}
					}
					setState(346);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(347);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(349);
				bitExpr(0);
				setState(351);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(350);
					match(NOT);
					}
				}

				setState(353);
				match(BETWEEN);
				setState(354);
				bitExpr(0);
				setState(355);
				match(AND);
				setState(356);
				predicate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(358);
				bitExpr(0);
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(359);
					match(NOT);
					}
				}

				setState(362);
				match(LIKE);
				setState(363);
				simpleExpr(0);
				setState(366);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(364);
					match(ESCAPE);
					setState(365);
					simpleExpr(0);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(368);
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
		public TerminalNode VERTICAL_BAR_() { return getToken(BaseRuleParser.VERTICAL_BAR_, 0); }
		public TerminalNode AMPERSAND_() { return getToken(BaseRuleParser.AMPERSAND_, 0); }
		public TerminalNode SIGNED_LEFT_SHIFT_() { return getToken(BaseRuleParser.SIGNED_LEFT_SHIFT_, 0); }
		public TerminalNode SIGNED_RIGHT_SHIFT_() { return getToken(BaseRuleParser.SIGNED_RIGHT_SHIFT_, 0); }
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public TerminalNode SLASH_() { return getToken(BaseRuleParser.SLASH_, 0); }
		public TerminalNode MOD_() { return getToken(BaseRuleParser.MOD_, 0); }
		public TerminalNode CARET_() { return getToken(BaseRuleParser.CARET_, 0); }
		public BitExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBitExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBitExpr(this);
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
		int _startState = 60;
		enterRecursionRule(_localctx, 60, RULE_bitExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(372);
			simpleExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(406);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(404);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
					case 1:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(374);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(375);
						match(VERTICAL_BAR_);
						setState(376);
						bitExpr(12);
						}
						break;
					case 2:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(377);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(378);
						match(AMPERSAND_);
						setState(379);
						bitExpr(11);
						}
						break;
					case 3:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(380);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(381);
						match(SIGNED_LEFT_SHIFT_);
						setState(382);
						bitExpr(10);
						}
						break;
					case 4:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(383);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(384);
						match(SIGNED_RIGHT_SHIFT_);
						setState(385);
						bitExpr(9);
						}
						break;
					case 5:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(386);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(387);
						match(PLUS_);
						setState(388);
						bitExpr(8);
						}
						break;
					case 6:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(389);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(390);
						match(MINUS_);
						setState(391);
						bitExpr(7);
						}
						break;
					case 7:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(392);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(393);
						match(ASTERISK_);
						setState(394);
						bitExpr(6);
						}
						break;
					case 8:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(395);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(396);
						match(SLASH_);
						setState(397);
						bitExpr(5);
						}
						break;
					case 9:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(398);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(399);
						match(MOD_);
						setState(400);
						bitExpr(4);
						}
						break;
					case 10:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(401);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(402);
						match(CARET_);
						setState(403);
						bitExpr(3);
						}
						break;
					}
					} 
				}
				setState(408);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
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
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public TerminalNode TILDE_() { return getToken(BaseRuleParser.TILDE_, 0); }
		public TerminalNode NOT_() { return getToken(BaseRuleParser.NOT_, 0); }
		public TerminalNode BINARY() { return getToken(BaseRuleParser.BINARY, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(BaseRuleParser.EXISTS, 0); }
		public TerminalNode LBE_() { return getToken(BaseRuleParser.LBE_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RBE_() { return getToken(BaseRuleParser.RBE_, 0); }
		public CaseExpressionContext caseExpression() {
			return getRuleContext(CaseExpressionContext.class,0);
		}
		public PrivateExprOfDbContext privateExprOfDb() {
			return getRuleContext(PrivateExprOfDbContext.class,0);
		}
		public TerminalNode OR_() { return getToken(BaseRuleParser.OR_, 0); }
		public SimpleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSimpleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSimpleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSimpleExpr(this);
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
		int _startState = 62;
		enterRecursionRule(_localctx, 62, RULE_simpleExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				{
				setState(410);
				functionCall();
				}
				break;
			case 2:
				{
				setState(411);
				parameterMarker();
				}
				break;
			case 3:
				{
				setState(412);
				literals();
				}
				break;
			case 4:
				{
				setState(413);
				columnName();
				}
				break;
			case 5:
				{
				setState(414);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_))) != 0) || _la==BINARY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(415);
				simpleExpr(6);
				}
				break;
			case 6:
				{
				setState(417);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(416);
					match(ROW);
					}
				}

				setState(419);
				match(LP_);
				setState(420);
				expr(0);
				setState(425);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(421);
					match(COMMA_);
					setState(422);
					expr(0);
					}
					}
					setState(427);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(428);
				match(RP_);
				}
				break;
			case 7:
				{
				setState(431);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXISTS) {
					{
					setState(430);
					match(EXISTS);
					}
				}

				setState(433);
				subquery();
				}
				break;
			case 8:
				{
				setState(434);
				match(LBE_);
				setState(435);
				identifier();
				setState(436);
				expr(0);
				setState(437);
				match(RBE_);
				}
				break;
			case 9:
				{
				setState(439);
				caseExpression();
				}
				break;
			case 10:
				{
				setState(440);
				privateExprOfDb();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(448);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new SimpleExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
					setState(443);
					if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
					setState(444);
					match(OR_);
					setState(445);
					simpleExpr(8);
					}
					} 
				}
				setState(450);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_functionCall);
		try {
			setState(454);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(451);
				aggregationFunction();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(452);
				specialFunction();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(453);
				regularFunction();
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

	public static class AggregationFunctionContext extends ParserRuleContext {
		public AggregationFunctionNameContext aggregationFunctionName() {
			return getRuleContext(AggregationFunctionNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public DistinctContext distinct() {
			return getRuleContext(DistinctContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public AggregationFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAggregationFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAggregationFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAggregationFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionContext aggregationFunction() throws RecognitionException {
		AggregationFunctionContext _localctx = new AggregationFunctionContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_aggregationFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(456);
			aggregationFunctionName();
			setState(457);
			match(LP_);
			setState(459);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DISTINCT) {
				{
				setState(458);
				distinct();
				}
			}

			setState(470);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case TRUNCATE:
			case FUNCTION:
			case TRIGGER:
			case CASE:
			case CAST:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIMIT:
			case OFFSET:
			case SAVEPOINT:
			case BOOLEAN:
			case CHAR:
			case ARRAY:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case QUARTER:
			case WEEK:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case NEXT:
			case NAME:
			case INTEGER:
			case TYPE:
			case BINARY:
			case HIDDEN_:
			case MOD:
			case PARTITION:
			case PARTITIONS:
			case TOP:
			case ROW:
			case XOR:
			case ALWAYS:
			case ROLE:
			case START:
			case ALGORITHM:
			case AUTO:
			case BLOCKERS:
			case CLUSTERED:
			case NONCLUSTERED:
			case COLUMNSTORE:
			case CONTENT:
			case CONVERT:
			case DATABASE:
			case YEARS:
			case MONTHS:
			case WEEKS:
			case DAYS:
			case MINUTES:
			case DENY:
			case DETERMINISTIC:
			case DISTRIBUTION:
			case DOCUMENT:
			case DURABILITY:
			case ENCRYPTED:
			case FILESTREAM:
			case FILETABLE:
			case FILLFACTOR:
			case FOLLOWING:
			case HASH:
			case HEAP:
			case INBOUND:
			case OUTBOUND:
			case UNBOUNDED:
			case INFINITE:
			case LOGIN:
			case MASKED:
			case MAXDOP:
			case MOVE:
			case NOCHECK:
			case OBJECT:
			case OFF:
			case ONLINE:
			case OVER:
			case PAGE:
			case PAUSED:
			case PERIOD:
			case PERSISTED:
			case PRECEDING:
			case RANDOMIZED:
			case RANGE:
			case REBUILD:
			case REPLICATE:
			case REPLICATION:
			case RESUMABLE:
			case ROWGUIDCOL:
			case SAVE:
			case SELF:
			case SPARSE:
			case SWITCH:
			case TRAN:
			case TRANCOUNT:
			case CONTROL:
			case CONCAT:
			case TAKE:
			case OWNERSHIP:
			case DEFINITION:
			case APPLICATION:
			case ASSEMBLY:
			case SYMMETRIC:
			case ASYMMETRIC:
			case SERVER:
			case RECEIVE:
			case CHANGE:
			case TRACE:
			case TRACKING:
			case RESOURCES:
			case SETTINGS:
			case STATE:
			case AVAILABILITY:
			case CREDENTIAL:
			case ENDPOINT:
			case EVENT:
			case NOTIFICATION:
			case LINKED:
			case AUDIT:
			case DDL:
			case XML:
			case IMPERSONATE:
			case SECURABLES:
			case AUTHENTICATE:
			case EXTERNAL:
			case ACCESS:
			case ADMINISTER:
			case BULK:
			case OPERATIONS:
			case UNSAFE:
			case SHUTDOWN:
			case SCOPED:
			case CONFIGURATION:
			case DATASPACE:
			case SERVICE:
			case CERTIFICATE:
			case CONTRACT:
			case ENCRYPTION:
			case MASTER:
			case DATA:
			case SOURCE:
			case FILE:
			case FORMAT:
			case LIBRARY:
			case FULLTEXT:
			case MASK:
			case UNMASK:
			case MESSAGE:
			case REMOTE:
			case BINDING:
			case ROUTE:
			case SECURITY:
			case POLICY:
			case AGGREGATE:
			case QUEUE:
			case RULE:
			case SYNONYM:
			case COLLECTION:
			case SCRIPT:
			case KILL:
			case BACKUP:
			case LOG:
			case SHOWPLAN:
			case SUBSCRIBE:
			case QUERY:
			case NOTIFICATIONS:
			case CHECKPOINT:
			case SEQUENCE:
			case ABORT_AFTER_WAIT:
			case ALLOW_PAGE_LOCKS:
			case ALLOW_ROW_LOCKS:
			case ALL_SPARSE_COLUMNS:
			case BUCKET_COUNT:
			case COLUMNSTORE_ARCHIVE:
			case COLUMN_ENCRYPTION_KEY:
			case COLUMN_SET:
			case COMPRESSION_DELAY:
			case DATABASE_DEAULT:
			case DATA_COMPRESSION:
			case DATA_CONSISTENCY_CHECK:
			case ENCRYPTION_TYPE:
			case SYSTEM_TIME:
			case SYSTEM_VERSIONING:
			case TEXTIMAGE_ON:
			case WAIT_AT_LOW_PRIORITY:
			case STATISTICS_INCREMENTAL:
			case STATISTICS_NORECOMPUTE:
			case ROUND_ROBIN:
			case SCHEMA_AND_DATA:
			case SCHEMA_ONLY:
			case SORT_IN_TEMPDB:
			case IGNORE_DUP_KEY:
			case IMPLICIT_TRANSACTIONS:
			case MAX_DURATION:
			case MEMORY_OPTIMIZED:
			case MIGRATION_STATE:
			case PAD_INDEX:
			case REMOTE_DATA_ARCHIVE:
			case FILESTREAM_ON:
			case FILETABLE_COLLATE_FILENAME:
			case FILETABLE_DIRECTORY:
			case FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME:
			case FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME:
			case FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME:
			case FILTER_PREDICATE:
			case HISTORY_RETENTION_PERIOD:
			case HISTORY_TABLE:
			case LOCK_ESCALATION:
			case DROP_EXISTING:
			case ROW_NUMBER:
			case FIRST:
			case DATETIME2:
			case OUTPUT:
			case INSERTED:
			case DELETED:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(461);
				expr(0);
				setState(466);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(462);
					match(COMMA_);
					setState(463);
					expr(0);
					}
					}
					setState(468);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(469);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(472);
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
		public TerminalNode MAX() { return getToken(BaseRuleParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(BaseRuleParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(BaseRuleParser.SUM, 0); }
		public TerminalNode COUNT() { return getToken(BaseRuleParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(BaseRuleParser.AVG, 0); }
		public AggregationFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunctionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAggregationFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAggregationFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAggregationFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionNameContext aggregationFunctionName() throws RecognitionException {
		AggregationFunctionNameContext _localctx = new AggregationFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_aggregationFunctionName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(474);
			_la = _input.LA(1);
			if ( !(((((_la - 143)) & ~0x3f) == 0 && ((1L << (_la - 143)) & ((1L << (MAX - 143)) | (1L << (MIN - 143)) | (1L << (SUM - 143)) | (1L << (COUNT - 143)) | (1L << (AVG - 143)))) != 0)) ) {
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
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public DistinctContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distinct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDistinct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDistinct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDistinct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistinctContext distinct() throws RecognitionException {
		DistinctContext _localctx = new DistinctContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_distinct);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(476);
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
		public CharFunctionContext charFunction() {
			return getRuleContext(CharFunctionContext.class,0);
		}
		public SpecialFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSpecialFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSpecialFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSpecialFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialFunctionContext specialFunction() throws RecognitionException {
		SpecialFunctionContext _localctx = new SpecialFunctionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_specialFunction);
		try {
			setState(480);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CAST:
				enterOuterAlt(_localctx, 1);
				{
				setState(478);
				castFunction();
				}
				break;
			case CHAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(479);
				charFunction();
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
		public TerminalNode CAST() { return getToken(BaseRuleParser.CAST, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public CastFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCastFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCastFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCastFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastFunctionContext castFunction() throws RecognitionException {
		CastFunctionContext _localctx = new CastFunctionContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_castFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
			match(CAST);
			setState(483);
			match(LP_);
			setState(484);
			expr(0);
			setState(485);
			match(AS);
			setState(486);
			dataType();
			setState(487);
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

	public static class CharFunctionContext extends ParserRuleContext {
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TerminalNode USING() { return getToken(BaseRuleParser.USING, 0); }
		public IgnoredIdentifierContext ignoredIdentifier() {
			return getRuleContext(IgnoredIdentifierContext.class,0);
		}
		public CharFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCharFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCharFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCharFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharFunctionContext charFunction() throws RecognitionException {
		CharFunctionContext _localctx = new CharFunctionContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_charFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(489);
			match(CHAR);
			setState(490);
			match(LP_);
			setState(491);
			expr(0);
			setState(496);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(492);
				match(COMMA_);
				setState(493);
				expr(0);
				}
				}
				setState(498);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(501);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(499);
				match(USING);
				setState(500);
				ignoredIdentifier();
				}
			}

			setState(503);
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
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public RegularFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRegularFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRegularFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRegularFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularFunctionContext regularFunction() throws RecognitionException {
		RegularFunctionContext _localctx = new RegularFunctionContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_regularFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(505);
			regularFunctionName();
			setState(506);
			match(LP_);
			setState(516);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case TRUNCATE:
			case FUNCTION:
			case TRIGGER:
			case CASE:
			case CAST:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIMIT:
			case OFFSET:
			case SAVEPOINT:
			case BOOLEAN:
			case CHAR:
			case ARRAY:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case QUARTER:
			case WEEK:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case NEXT:
			case NAME:
			case INTEGER:
			case TYPE:
			case BINARY:
			case HIDDEN_:
			case MOD:
			case PARTITION:
			case PARTITIONS:
			case TOP:
			case ROW:
			case XOR:
			case ALWAYS:
			case ROLE:
			case START:
			case ALGORITHM:
			case AUTO:
			case BLOCKERS:
			case CLUSTERED:
			case NONCLUSTERED:
			case COLUMNSTORE:
			case CONTENT:
			case CONVERT:
			case DATABASE:
			case YEARS:
			case MONTHS:
			case WEEKS:
			case DAYS:
			case MINUTES:
			case DENY:
			case DETERMINISTIC:
			case DISTRIBUTION:
			case DOCUMENT:
			case DURABILITY:
			case ENCRYPTED:
			case FILESTREAM:
			case FILETABLE:
			case FILLFACTOR:
			case FOLLOWING:
			case HASH:
			case HEAP:
			case INBOUND:
			case OUTBOUND:
			case UNBOUNDED:
			case INFINITE:
			case LOGIN:
			case MASKED:
			case MAXDOP:
			case MOVE:
			case NOCHECK:
			case OBJECT:
			case OFF:
			case ONLINE:
			case OVER:
			case PAGE:
			case PAUSED:
			case PERIOD:
			case PERSISTED:
			case PRECEDING:
			case RANDOMIZED:
			case RANGE:
			case REBUILD:
			case REPLICATE:
			case REPLICATION:
			case RESUMABLE:
			case ROWGUIDCOL:
			case SAVE:
			case SELF:
			case SPARSE:
			case SWITCH:
			case TRAN:
			case TRANCOUNT:
			case CONTROL:
			case CONCAT:
			case TAKE:
			case OWNERSHIP:
			case DEFINITION:
			case APPLICATION:
			case ASSEMBLY:
			case SYMMETRIC:
			case ASYMMETRIC:
			case SERVER:
			case RECEIVE:
			case CHANGE:
			case TRACE:
			case TRACKING:
			case RESOURCES:
			case SETTINGS:
			case STATE:
			case AVAILABILITY:
			case CREDENTIAL:
			case ENDPOINT:
			case EVENT:
			case NOTIFICATION:
			case LINKED:
			case AUDIT:
			case DDL:
			case XML:
			case IMPERSONATE:
			case SECURABLES:
			case AUTHENTICATE:
			case EXTERNAL:
			case ACCESS:
			case ADMINISTER:
			case BULK:
			case OPERATIONS:
			case UNSAFE:
			case SHUTDOWN:
			case SCOPED:
			case CONFIGURATION:
			case DATASPACE:
			case SERVICE:
			case CERTIFICATE:
			case CONTRACT:
			case ENCRYPTION:
			case MASTER:
			case DATA:
			case SOURCE:
			case FILE:
			case FORMAT:
			case LIBRARY:
			case FULLTEXT:
			case MASK:
			case UNMASK:
			case MESSAGE:
			case REMOTE:
			case BINDING:
			case ROUTE:
			case SECURITY:
			case POLICY:
			case AGGREGATE:
			case QUEUE:
			case RULE:
			case SYNONYM:
			case COLLECTION:
			case SCRIPT:
			case KILL:
			case BACKUP:
			case LOG:
			case SHOWPLAN:
			case SUBSCRIBE:
			case QUERY:
			case NOTIFICATIONS:
			case CHECKPOINT:
			case SEQUENCE:
			case ABORT_AFTER_WAIT:
			case ALLOW_PAGE_LOCKS:
			case ALLOW_ROW_LOCKS:
			case ALL_SPARSE_COLUMNS:
			case BUCKET_COUNT:
			case COLUMNSTORE_ARCHIVE:
			case COLUMN_ENCRYPTION_KEY:
			case COLUMN_SET:
			case COMPRESSION_DELAY:
			case DATABASE_DEAULT:
			case DATA_COMPRESSION:
			case DATA_CONSISTENCY_CHECK:
			case ENCRYPTION_TYPE:
			case SYSTEM_TIME:
			case SYSTEM_VERSIONING:
			case TEXTIMAGE_ON:
			case WAIT_AT_LOW_PRIORITY:
			case STATISTICS_INCREMENTAL:
			case STATISTICS_NORECOMPUTE:
			case ROUND_ROBIN:
			case SCHEMA_AND_DATA:
			case SCHEMA_ONLY:
			case SORT_IN_TEMPDB:
			case IGNORE_DUP_KEY:
			case IMPLICIT_TRANSACTIONS:
			case MAX_DURATION:
			case MEMORY_OPTIMIZED:
			case MIGRATION_STATE:
			case PAD_INDEX:
			case REMOTE_DATA_ARCHIVE:
			case FILESTREAM_ON:
			case FILETABLE_COLLATE_FILENAME:
			case FILETABLE_DIRECTORY:
			case FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME:
			case FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME:
			case FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME:
			case FILTER_PREDICATE:
			case HISTORY_RETENTION_PERIOD:
			case HISTORY_TABLE:
			case LOCK_ESCALATION:
			case DROP_EXISTING:
			case ROW_NUMBER:
			case FIRST:
			case DATETIME2:
			case OUTPUT:
			case INSERTED:
			case DELETED:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(507);
				expr(0);
				setState(512);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(508);
					match(COMMA_);
					setState(509);
					expr(0);
					}
					}
					setState(514);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(515);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(518);
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
		public TerminalNode IF() { return getToken(BaseRuleParser.IF, 0); }
		public TerminalNode LOCALTIME() { return getToken(BaseRuleParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(BaseRuleParser.LOCALTIMESTAMP, 0); }
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public RegularFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularFunctionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRegularFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRegularFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRegularFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularFunctionNameContext regularFunctionName() throws RecognitionException {
		RegularFunctionNameContext _localctx = new RegularFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_regularFunctionName);
		try {
			setState(525);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(520);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(521);
				match(IF);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(522);
				match(LOCALTIME);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(523);
				match(LOCALTIMESTAMP);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(524);
				match(INTERVAL);
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

	public static class CaseExpressionContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(BaseRuleParser.CASE, 0); }
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
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCaseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCaseExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCaseExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseExpressionContext caseExpression() throws RecognitionException {
		CaseExpressionContext _localctx = new CaseExpressionContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_caseExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(527);
			match(CASE);
			setState(529);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_) | (1L << LP_) | (1L << LBE_) | (1L << QUESTION_) | (1L << TRUNCATE))) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & ((1L << (FUNCTION - 66)) | (1L << (TRIGGER - 66)) | (1L << (CASE - 66)) | (1L << (CAST - 66)) | (1L << (IF - 66)) | (1L << (NULL - 66)) | (1L << (TRUE - 66)) | (1L << (FALSE - 66)) | (1L << (EXISTS - 66)) | (1L << (LIMIT - 66)) | (1L << (OFFSET - 66)) | (1L << (SAVEPOINT - 66)) | (1L << (BOOLEAN - 66)) | (1L << (CHAR - 66)) | (1L << (ARRAY - 66)) | (1L << (INTERVAL - 66)) | (1L << (DATE - 66)))) != 0) || ((((_la - 130)) & ~0x3f) == 0 && ((1L << (_la - 130)) & ((1L << (TIME - 130)) | (1L << (TIMESTAMP - 130)) | (1L << (LOCALTIME - 130)) | (1L << (LOCALTIMESTAMP - 130)) | (1L << (QUARTER - 130)) | (1L << (WEEK - 130)) | (1L << (MICROSECOND - 130)) | (1L << (MAX - 130)) | (1L << (MIN - 130)) | (1L << (SUM - 130)) | (1L << (COUNT - 130)) | (1L << (AVG - 130)) | (1L << (ENABLE - 130)) | (1L << (DISABLE - 130)) | (1L << (INSTANCE - 130)) | (1L << (DO - 130)) | (1L << (DEFINER - 130)) | (1L << (SQL - 130)) | (1L << (CASCADED - 130)) | (1L << (LOCAL - 130)) | (1L << (NEXT - 130)) | (1L << (NAME - 130)) | (1L << (INTEGER - 130)) | (1L << (TYPE - 130)))) != 0) || ((((_la - 198)) & ~0x3f) == 0 && ((1L << (_la - 198)) & ((1L << (BINARY - 198)) | (1L << (HIDDEN_ - 198)) | (1L << (MOD - 198)) | (1L << (PARTITION - 198)) | (1L << (PARTITIONS - 198)) | (1L << (TOP - 198)) | (1L << (ROW - 198)) | (1L << (XOR - 198)) | (1L << (ALWAYS - 198)) | (1L << (ROLE - 198)) | (1L << (START - 198)) | (1L << (ALGORITHM - 198)) | (1L << (AUTO - 198)) | (1L << (BLOCKERS - 198)) | (1L << (CLUSTERED - 198)) | (1L << (NONCLUSTERED - 198)) | (1L << (COLUMNSTORE - 198)) | (1L << (CONTENT - 198)) | (1L << (CONVERT - 198)) | (1L << (DATABASE - 198)) | (1L << (YEARS - 198)) | (1L << (MONTHS - 198)) | (1L << (WEEKS - 198)) | (1L << (DAYS - 198)) | (1L << (MINUTES - 198)) | (1L << (DENY - 198)) | (1L << (DETERMINISTIC - 198)) | (1L << (DISTRIBUTION - 198)) | (1L << (DOCUMENT - 198)) | (1L << (DURABILITY - 198)) | (1L << (ENCRYPTED - 198)) | (1L << (FILESTREAM - 198)) | (1L << (FILETABLE - 198)) | (1L << (FILLFACTOR - 198)) | (1L << (FOLLOWING - 198)) | (1L << (HASH - 198)) | (1L << (HEAP - 198)) | (1L << (INBOUND - 198)) | (1L << (OUTBOUND - 198)) | (1L << (UNBOUNDED - 198)) | (1L << (INFINITE - 198)) | (1L << (LOGIN - 198)) | (1L << (MASKED - 198)) | (1L << (MAXDOP - 198)) | (1L << (MOVE - 198)) | (1L << (NOCHECK - 198)) | (1L << (OBJECT - 198)))) != 0) || ((((_la - 262)) & ~0x3f) == 0 && ((1L << (_la - 262)) & ((1L << (OFF - 262)) | (1L << (ONLINE - 262)) | (1L << (OVER - 262)) | (1L << (PAGE - 262)) | (1L << (PAUSED - 262)) | (1L << (PERIOD - 262)) | (1L << (PERSISTED - 262)) | (1L << (PRECEDING - 262)) | (1L << (RANDOMIZED - 262)) | (1L << (RANGE - 262)) | (1L << (REBUILD - 262)) | (1L << (REPLICATE - 262)) | (1L << (REPLICATION - 262)) | (1L << (RESUMABLE - 262)) | (1L << (ROWGUIDCOL - 262)) | (1L << (SAVE - 262)) | (1L << (SELF - 262)) | (1L << (SPARSE - 262)) | (1L << (SWITCH - 262)) | (1L << (TRAN - 262)) | (1L << (TRANCOUNT - 262)) | (1L << (CONTROL - 262)) | (1L << (CONCAT - 262)) | (1L << (TAKE - 262)) | (1L << (OWNERSHIP - 262)) | (1L << (DEFINITION - 262)) | (1L << (APPLICATION - 262)) | (1L << (ASSEMBLY - 262)) | (1L << (SYMMETRIC - 262)) | (1L << (ASYMMETRIC - 262)) | (1L << (SERVER - 262)) | (1L << (RECEIVE - 262)) | (1L << (CHANGE - 262)) | (1L << (TRACE - 262)) | (1L << (TRACKING - 262)) | (1L << (RESOURCES - 262)) | (1L << (SETTINGS - 262)) | (1L << (STATE - 262)) | (1L << (AVAILABILITY - 262)) | (1L << (CREDENTIAL - 262)) | (1L << (ENDPOINT - 262)) | (1L << (EVENT - 262)) | (1L << (NOTIFICATION - 262)) | (1L << (LINKED - 262)) | (1L << (AUDIT - 262)) | (1L << (DDL - 262)) | (1L << (XML - 262)) | (1L << (IMPERSONATE - 262)) | (1L << (SECURABLES - 262)) | (1L << (AUTHENTICATE - 262)) | (1L << (EXTERNAL - 262)) | (1L << (ACCESS - 262)) | (1L << (ADMINISTER - 262)) | (1L << (BULK - 262)) | (1L << (OPERATIONS - 262)) | (1L << (UNSAFE - 262)) | (1L << (SHUTDOWN - 262)) | (1L << (SCOPED - 262)))) != 0) || ((((_la - 326)) & ~0x3f) == 0 && ((1L << (_la - 326)) & ((1L << (CONFIGURATION - 326)) | (1L << (DATASPACE - 326)) | (1L << (SERVICE - 326)) | (1L << (CERTIFICATE - 326)) | (1L << (CONTRACT - 326)) | (1L << (ENCRYPTION - 326)) | (1L << (MASTER - 326)) | (1L << (DATA - 326)) | (1L << (SOURCE - 326)) | (1L << (FILE - 326)) | (1L << (FORMAT - 326)) | (1L << (LIBRARY - 326)) | (1L << (FULLTEXT - 326)) | (1L << (MASK - 326)) | (1L << (UNMASK - 326)) | (1L << (MESSAGE - 326)) | (1L << (REMOTE - 326)) | (1L << (BINDING - 326)) | (1L << (ROUTE - 326)) | (1L << (SECURITY - 326)) | (1L << (POLICY - 326)) | (1L << (AGGREGATE - 326)) | (1L << (QUEUE - 326)) | (1L << (RULE - 326)) | (1L << (SYNONYM - 326)) | (1L << (COLLECTION - 326)) | (1L << (SCRIPT - 326)) | (1L << (KILL - 326)) | (1L << (BACKUP - 326)) | (1L << (LOG - 326)) | (1L << (SHOWPLAN - 326)) | (1L << (SUBSCRIBE - 326)) | (1L << (QUERY - 326)) | (1L << (NOTIFICATIONS - 326)) | (1L << (CHECKPOINT - 326)) | (1L << (SEQUENCE - 326)) | (1L << (ABORT_AFTER_WAIT - 326)) | (1L << (ALLOW_PAGE_LOCKS - 326)) | (1L << (ALLOW_ROW_LOCKS - 326)) | (1L << (ALL_SPARSE_COLUMNS - 326)) | (1L << (BUCKET_COUNT - 326)) | (1L << (COLUMNSTORE_ARCHIVE - 326)) | (1L << (COLUMN_ENCRYPTION_KEY - 326)) | (1L << (COLUMN_SET - 326)) | (1L << (COMPRESSION_DELAY - 326)) | (1L << (DATABASE_DEAULT - 326)) | (1L << (DATA_COMPRESSION - 326)) | (1L << (DATA_CONSISTENCY_CHECK - 326)) | (1L << (ENCRYPTION_TYPE - 326)) | (1L << (SYSTEM_TIME - 326)) | (1L << (SYSTEM_VERSIONING - 326)) | (1L << (TEXTIMAGE_ON - 326)) | (1L << (WAIT_AT_LOW_PRIORITY - 326)) | (1L << (STATISTICS_INCREMENTAL - 326)) | (1L << (STATISTICS_NORECOMPUTE - 326)) | (1L << (ROUND_ROBIN - 326)) | (1L << (SCHEMA_AND_DATA - 326)) | (1L << (SCHEMA_ONLY - 326)) | (1L << (SORT_IN_TEMPDB - 326)) | (1L << (IGNORE_DUP_KEY - 326)) | (1L << (IMPLICIT_TRANSACTIONS - 326)) | (1L << (MAX_DURATION - 326)) | (1L << (MEMORY_OPTIMIZED - 326)) | (1L << (MIGRATION_STATE - 326)))) != 0) || ((((_la - 390)) & ~0x3f) == 0 && ((1L << (_la - 390)) & ((1L << (PAD_INDEX - 390)) | (1L << (REMOTE_DATA_ARCHIVE - 390)) | (1L << (FILESTREAM_ON - 390)) | (1L << (FILETABLE_COLLATE_FILENAME - 390)) | (1L << (FILETABLE_DIRECTORY - 390)) | (1L << (FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME - 390)) | (1L << (FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME - 390)) | (1L << (FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME - 390)) | (1L << (FILTER_PREDICATE - 390)) | (1L << (HISTORY_RETENTION_PERIOD - 390)) | (1L << (HISTORY_TABLE - 390)) | (1L << (LOCK_ESCALATION - 390)) | (1L << (DROP_EXISTING - 390)) | (1L << (ROW_NUMBER - 390)) | (1L << (FIRST - 390)) | (1L << (DATETIME2 - 390)) | (1L << (OUTPUT - 390)) | (1L << (INSERTED - 390)) | (1L << (DELETED - 390)))) != 0) || ((((_la - 460)) & ~0x3f) == 0 && ((1L << (_la - 460)) & ((1L << (IDENTIFIER_ - 460)) | (1L << (STRING_ - 460)) | (1L << (NUMBER_ - 460)) | (1L << (HEX_DIGIT_ - 460)) | (1L << (BIT_NUM_ - 460)))) != 0)) {
				{
				setState(528);
				simpleExpr(0);
				}
			}

			setState(532); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(531);
					caseWhen();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(534); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(537);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				{
				setState(536);
				caseElse();
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

	public static class CaseWhenContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(BaseRuleParser.WHEN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode THEN() { return getToken(BaseRuleParser.THEN, 0); }
		public CaseWhenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCaseWhen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCaseWhen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCaseWhen(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseWhenContext caseWhen() throws RecognitionException {
		CaseWhenContext _localctx = new CaseWhenContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_caseWhen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(539);
			match(WHEN);
			setState(540);
			expr(0);
			setState(541);
			match(THEN);
			setState(542);
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
		public TerminalNode ELSE() { return getToken(BaseRuleParser.ELSE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public CaseElseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseElse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCaseElse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCaseElse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCaseElse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseElseContext caseElse() throws RecognitionException {
		CaseElseContext _localctx = new CaseElseContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_caseElse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(544);
			match(ELSE);
			setState(545);
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

	public static class PrivateExprOfDbContext extends ParserRuleContext {
		public WindowedFunctionContext windowedFunction() {
			return getRuleContext(WindowedFunctionContext.class,0);
		}
		public AtTimeZoneExprContext atTimeZoneExpr() {
			return getRuleContext(AtTimeZoneExprContext.class,0);
		}
		public CastExprContext castExpr() {
			return getRuleContext(CastExprContext.class,0);
		}
		public ConvertExprContext convertExpr() {
			return getRuleContext(ConvertExprContext.class,0);
		}
		public PrivateExprOfDbContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_privateExprOfDb; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPrivateExprOfDb(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPrivateExprOfDb(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPrivateExprOfDb(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrivateExprOfDbContext privateExprOfDb() throws RecognitionException {
		PrivateExprOfDbContext _localctx = new PrivateExprOfDbContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_privateExprOfDb);
		try {
			setState(551);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(547);
				windowedFunction();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(548);
				atTimeZoneExpr();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(549);
				castExpr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(550);
				convertExpr();
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
		public MatchNoneContext matchNone() {
			return getRuleContext(MatchNoneContext.class,0);
		}
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(553);
			matchNone();
			}
		}
		catch (RecognitionException re) {
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
		public TerminalNode ORDER() { return getToken(BaseRuleParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public List<OrderByItemContext> orderByItem() {
			return getRuleContexts(OrderByItemContext.class);
		}
		public OrderByItemContext orderByItem(int i) {
			return getRuleContext(OrderByItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TerminalNode OFFSET() { return getToken(BaseRuleParser.OFFSET, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> ROW() { return getTokens(BaseRuleParser.ROW); }
		public TerminalNode ROW(int i) {
			return getToken(BaseRuleParser.ROW, i);
		}
		public List<TerminalNode> ROWS() { return getTokens(BaseRuleParser.ROWS); }
		public TerminalNode ROWS(int i) {
			return getToken(BaseRuleParser.ROWS, i);
		}
		public TerminalNode FETCH() { return getToken(BaseRuleParser.FETCH, 0); }
		public TerminalNode ONLY() { return getToken(BaseRuleParser.ONLY, 0); }
		public TerminalNode FIRST() { return getToken(BaseRuleParser.FIRST, 0); }
		public TerminalNode NEXT() { return getToken(BaseRuleParser.NEXT, 0); }
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOrderByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOrderByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOrderByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(555);
			match(ORDER);
			setState(556);
			match(BY);
			setState(557);
			orderByItem();
			setState(562);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(558);
				match(COMMA_);
				setState(559);
				orderByItem();
				}
				}
				setState(564);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(576);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OFFSET) {
				{
				setState(565);
				match(OFFSET);
				setState(566);
				expr(0);
				setState(567);
				_la = _input.LA(1);
				if ( !(_la==ROW || _la==ROWS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(574);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FETCH) {
					{
					setState(568);
					match(FETCH);
					setState(569);
					_la = _input.LA(1);
					if ( !(_la==NEXT || _la==FIRST) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(570);
					expr(0);
					setState(571);
					_la = _input.LA(1);
					if ( !(_la==ROW || _la==ROWS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(572);
					match(ONLY);
					}
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

	public static class OrderByItemContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode COLLATE() { return getToken(BaseRuleParser.COLLATE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASC() { return getToken(BaseRuleParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(BaseRuleParser.DESC, 0); }
		public OrderByItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOrderByItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOrderByItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOrderByItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByItemContext orderByItem() throws RecognitionException {
		OrderByItemContext _localctx = new OrderByItemContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_orderByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(581);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				{
				setState(578);
				columnName();
				}
				break;
			case 2:
				{
				setState(579);
				numberLiterals();
				}
				break;
			case 3:
				{
				setState(580);
				expr(0);
				}
				break;
			}
			setState(585);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLLATE) {
				{
				setState(583);
				match(COLLATE);
				setState(584);
				identifier();
				}
			}

			setState(588);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(587);
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
		public List<IgnoredIdentifierContext> ignoredIdentifier() {
			return getRuleContexts(IgnoredIdentifierContext.class);
		}
		public IgnoredIdentifierContext ignoredIdentifier(int i) {
			return getRuleContext(IgnoredIdentifierContext.class,i);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode MAX() { return getToken(BaseRuleParser.MAX, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode CONTENT() { return getToken(BaseRuleParser.CONTENT, 0); }
		public TerminalNode DOCUMENT() { return getToken(BaseRuleParser.DOCUMENT, 0); }
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_dataType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(593);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				{
				setState(590);
				ignoredIdentifier();
				setState(591);
				match(DOT_);
				}
				break;
			}
			setState(595);
			dataTypeName();
			setState(607);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(596);
				dataTypeLength();
				}
				break;
			case 2:
				{
				setState(597);
				match(LP_);
				setState(598);
				match(MAX);
				setState(599);
				match(RP_);
				}
				break;
			case 3:
				{
				setState(600);
				match(LP_);
				setState(602);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CONTENT || _la==DOCUMENT) {
					{
					setState(601);
					_la = _input.LA(1);
					if ( !(_la==CONTENT || _la==DOCUMENT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(604);
				ignoredIdentifier();
				setState(605);
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

	public static class DataTypeNameContext extends ParserRuleContext {
		public TerminalNode BIGINT() { return getToken(BaseRuleParser.BIGINT, 0); }
		public TerminalNode NUMERIC() { return getToken(BaseRuleParser.NUMERIC, 0); }
		public TerminalNode BIT() { return getToken(BaseRuleParser.BIT, 0); }
		public TerminalNode SMALLINT() { return getToken(BaseRuleParser.SMALLINT, 0); }
		public TerminalNode DECIMAL() { return getToken(BaseRuleParser.DECIMAL, 0); }
		public TerminalNode SMALLMONEY() { return getToken(BaseRuleParser.SMALLMONEY, 0); }
		public TerminalNode INT() { return getToken(BaseRuleParser.INT, 0); }
		public TerminalNode TINYINT() { return getToken(BaseRuleParser.TINYINT, 0); }
		public TerminalNode MONEY() { return getToken(BaseRuleParser.MONEY, 0); }
		public TerminalNode FLOAT() { return getToken(BaseRuleParser.FLOAT, 0); }
		public TerminalNode REAL() { return getToken(BaseRuleParser.REAL, 0); }
		public TerminalNode DATE() { return getToken(BaseRuleParser.DATE, 0); }
		public TerminalNode DATETIMEOFFSET() { return getToken(BaseRuleParser.DATETIMEOFFSET, 0); }
		public TerminalNode SMALLDATETIME() { return getToken(BaseRuleParser.SMALLDATETIME, 0); }
		public TerminalNode DATETIME() { return getToken(BaseRuleParser.DATETIME, 0); }
		public TerminalNode DATETIME2() { return getToken(BaseRuleParser.DATETIME2, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode VARCHAR() { return getToken(BaseRuleParser.VARCHAR, 0); }
		public TerminalNode TEXT() { return getToken(BaseRuleParser.TEXT, 0); }
		public TerminalNode NCHAR() { return getToken(BaseRuleParser.NCHAR, 0); }
		public TerminalNode NVARCHAR() { return getToken(BaseRuleParser.NVARCHAR, 0); }
		public TerminalNode NTEXT() { return getToken(BaseRuleParser.NTEXT, 0); }
		public TerminalNode BINARY() { return getToken(BaseRuleParser.BINARY, 0); }
		public TerminalNode VARBINARY() { return getToken(BaseRuleParser.VARBINARY, 0); }
		public TerminalNode IMAGE() { return getToken(BaseRuleParser.IMAGE, 0); }
		public TerminalNode SQL_VARIANT() { return getToken(BaseRuleParser.SQL_VARIANT, 0); }
		public TerminalNode XML() { return getToken(BaseRuleParser.XML, 0); }
		public TerminalNode UNIQUEIDENTIFIER() { return getToken(BaseRuleParser.UNIQUEIDENTIFIER, 0); }
		public TerminalNode HIERARCHYID() { return getToken(BaseRuleParser.HIERARCHYID, 0); }
		public TerminalNode GEOMETRY() { return getToken(BaseRuleParser.GEOMETRY, 0); }
		public TerminalNode GEOGRAPHY() { return getToken(BaseRuleParser.GEOGRAPHY, 0); }
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public DataTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDataTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDataTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDataTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeNameContext dataTypeName() throws RecognitionException {
		DataTypeNameContext _localctx = new DataTypeNameContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_dataTypeName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(609);
			_la = _input.LA(1);
			if ( !(((((_la - 125)) & ~0x3f) == 0 && ((1L << (_la - 125)) & ((1L << (CHAR - 125)) | (1L << (DATE - 125)) | (1L << (TIME - 125)) | (1L << (REAL - 125)) | (1L << (DECIMAL - 125)) | (1L << (BIT - 125)) | (1L << (SMALLINT - 125)) | (1L << (INT - 125)) | (1L << (TINYINT - 125)) | (1L << (NUMERIC - 125)) | (1L << (FLOAT - 125)) | (1L << (BIGINT - 125)) | (1L << (TEXT - 125)) | (1L << (VARCHAR - 125)))) != 0) || _la==BINARY || _la==XML || ((((_la - 407)) & ~0x3f) == 0 && ((1L << (_la - 407)) & ((1L << (MONEY - 407)) | (1L << (SMALLMONEY - 407)) | (1L << (DATETIMEOFFSET - 407)) | (1L << (DATETIME - 407)) | (1L << (DATETIME2 - 407)) | (1L << (SMALLDATETIME - 407)) | (1L << (NCHAR - 407)) | (1L << (NVARCHAR - 407)) | (1L << (NTEXT - 407)) | (1L << (VARBINARY - 407)) | (1L << (IMAGE - 407)) | (1L << (SQL_VARIANT - 407)) | (1L << (UNIQUEIDENTIFIER - 407)) | (1L << (HIERARCHYID - 407)) | (1L << (GEOMETRY - 407)) | (1L << (GEOGRAPHY - 407)) | (1L << (IDENTIFIER_ - 407)))) != 0)) ) {
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

	public static class AtTimeZoneExprContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public AtTimeZoneExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atTimeZoneExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAtTimeZoneExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAtTimeZoneExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAtTimeZoneExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtTimeZoneExprContext atTimeZoneExpr() throws RecognitionException {
		AtTimeZoneExprContext _localctx = new AtTimeZoneExprContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_atTimeZoneExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(611);
			match(IDENTIFIER_);
			setState(615);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(612);
				match(WITH);
				setState(613);
				match(TIME);
				setState(614);
				match(ZONE);
				}
			}

			setState(617);
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

	public static class CastExprContext extends ParserRuleContext {
		public TerminalNode CAST() { return getToken(BaseRuleParser.CAST, 0); }
		public List<TerminalNode> LP_() { return getTokens(BaseRuleParser.LP_); }
		public TerminalNode LP_(int i) {
			return getToken(BaseRuleParser.LP_, i);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public List<TerminalNode> RP_() { return getTokens(BaseRuleParser.RP_); }
		public TerminalNode RP_(int i) {
			return getToken(BaseRuleParser.RP_, i);
		}
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public CastExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCastExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCastExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCastExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastExprContext castExpr() throws RecognitionException {
		CastExprContext _localctx = new CastExprContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_castExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(619);
			match(CAST);
			setState(620);
			match(LP_);
			setState(621);
			expr(0);
			setState(622);
			match(AS);
			setState(623);
			dataType();
			setState(627);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(624);
				match(LP_);
				setState(625);
				match(NUMBER_);
				setState(626);
				match(RP_);
				}
			}

			setState(629);
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

	public static class ConvertExprContext extends ParserRuleContext {
		public TerminalNode CONVERT() { return getToken(BaseRuleParser.CONVERT, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(BaseRuleParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(BaseRuleParser.NUMBER_, i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ConvertExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_convertExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConvertExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConvertExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConvertExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConvertExprContext convertExpr() throws RecognitionException {
		ConvertExprContext _localctx = new ConvertExprContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_convertExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(631);
			match(CONVERT);
			{
			setState(632);
			dataType();
			setState(636);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(633);
				match(LP_);
				setState(634);
				match(NUMBER_);
				setState(635);
				match(RP_);
				}
			}

			setState(638);
			match(COMMA_);
			setState(639);
			expr(0);
			setState(642);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(640);
				match(COMMA_);
				setState(641);
				match(NUMBER_);
				}
				break;
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

	public static class WindowedFunctionContext extends ParserRuleContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public OverClauseContext overClause() {
			return getRuleContext(OverClauseContext.class,0);
		}
		public WindowedFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowedFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowedFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowedFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowedFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowedFunctionContext windowedFunction() throws RecognitionException {
		WindowedFunctionContext _localctx = new WindowedFunctionContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_windowedFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(644);
			functionCall();
			setState(645);
			overClause();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OverClauseContext extends ParserRuleContext {
		public TerminalNode OVER() { return getToken(BaseRuleParser.OVER, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public PartitionByClauseContext partitionByClause() {
			return getRuleContext(PartitionByClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public RowRangeClauseContext rowRangeClause() {
			return getRuleContext(RowRangeClauseContext.class,0);
		}
		public OverClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_overClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOverClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOverClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOverClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OverClauseContext overClause() throws RecognitionException {
		OverClauseContext _localctx = new OverClauseContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_overClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			match(OVER);
			setState(648);
			match(LP_);
			setState(650);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(649);
				partitionByClause();
				}
			}

			setState(653);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(652);
				orderByClause();
				}
			}

			setState(656);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROWS || _la==RANGE) {
				{
				setState(655);
				rowRangeClause();
				}
			}

			setState(658);
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

	public static class PartitionByClauseContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(BaseRuleParser.PARTITION, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public PartitionByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionByClauseContext partitionByClause() throws RecognitionException {
		PartitionByClauseContext _localctx = new PartitionByClauseContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_partitionByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(660);
			match(PARTITION);
			setState(661);
			match(BY);
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
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RowRangeClauseContext extends ParserRuleContext {
		public WindowFrameExtentContext windowFrameExtent() {
			return getRuleContext(WindowFrameExtentContext.class,0);
		}
		public TerminalNode ROWS() { return getToken(BaseRuleParser.ROWS, 0); }
		public TerminalNode RANGE() { return getToken(BaseRuleParser.RANGE, 0); }
		public RowRangeClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rowRangeClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRowRangeClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRowRangeClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRowRangeClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowRangeClauseContext rowRangeClause() throws RecognitionException {
		RowRangeClauseContext _localctx = new RowRangeClauseContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_rowRangeClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(670);
			_la = _input.LA(1);
			if ( !(_la==ROWS || _la==RANGE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(671);
			windowFrameExtent();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowFrameExtentContext extends ParserRuleContext {
		public WindowFramePrecedingContext windowFramePreceding() {
			return getRuleContext(WindowFramePrecedingContext.class,0);
		}
		public WindowFrameBetweenContext windowFrameBetween() {
			return getRuleContext(WindowFrameBetweenContext.class,0);
		}
		public WindowFrameExtentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrameExtent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowFrameExtent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowFrameExtent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowFrameExtent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameExtentContext windowFrameExtent() throws RecognitionException {
		WindowFrameExtentContext _localctx = new WindowFrameExtentContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_windowFrameExtent);
		try {
			setState(675);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CURRENT:
			case UNBOUNDED:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(673);
				windowFramePreceding();
				}
				break;
			case BETWEEN:
				enterOuterAlt(_localctx, 2);
				{
				setState(674);
				windowFrameBetween();
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

	public static class WindowFrameBetweenContext extends ParserRuleContext {
		public TerminalNode BETWEEN() { return getToken(BaseRuleParser.BETWEEN, 0); }
		public List<WindowFrameBoundContext> windowFrameBound() {
			return getRuleContexts(WindowFrameBoundContext.class);
		}
		public WindowFrameBoundContext windowFrameBound(int i) {
			return getRuleContext(WindowFrameBoundContext.class,i);
		}
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public WindowFrameBetweenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrameBetween; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowFrameBetween(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowFrameBetween(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowFrameBetween(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameBetweenContext windowFrameBetween() throws RecognitionException {
		WindowFrameBetweenContext _localctx = new WindowFrameBetweenContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_windowFrameBetween);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(677);
			match(BETWEEN);
			setState(678);
			windowFrameBound();
			setState(679);
			match(AND);
			setState(680);
			windowFrameBound();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowFrameBoundContext extends ParserRuleContext {
		public WindowFramePrecedingContext windowFramePreceding() {
			return getRuleContext(WindowFramePrecedingContext.class,0);
		}
		public WindowFrameFollowingContext windowFrameFollowing() {
			return getRuleContext(WindowFrameFollowingContext.class,0);
		}
		public WindowFrameBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrameBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowFrameBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowFrameBound(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowFrameBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameBoundContext windowFrameBound() throws RecognitionException {
		WindowFrameBoundContext _localctx = new WindowFrameBoundContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_windowFrameBound);
		try {
			setState(684);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(682);
				windowFramePreceding();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(683);
				windowFrameFollowing();
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

	public static class WindowFramePrecedingContext extends ParserRuleContext {
		public TerminalNode UNBOUNDED() { return getToken(BaseRuleParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(BaseRuleParser.PRECEDING, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode CURRENT() { return getToken(BaseRuleParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public WindowFramePrecedingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFramePreceding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowFramePreceding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowFramePreceding(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowFramePreceding(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFramePrecedingContext windowFramePreceding() throws RecognitionException {
		WindowFramePrecedingContext _localctx = new WindowFramePrecedingContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_windowFramePreceding);
		try {
			setState(692);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNBOUNDED:
				enterOuterAlt(_localctx, 1);
				{
				setState(686);
				match(UNBOUNDED);
				setState(687);
				match(PRECEDING);
				}
				break;
			case NUMBER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(688);
				match(NUMBER_);
				setState(689);
				match(PRECEDING);
				}
				break;
			case CURRENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(690);
				match(CURRENT);
				setState(691);
				match(ROW);
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

	public static class WindowFrameFollowingContext extends ParserRuleContext {
		public TerminalNode UNBOUNDED() { return getToken(BaseRuleParser.UNBOUNDED, 0); }
		public TerminalNode FOLLOWING() { return getToken(BaseRuleParser.FOLLOWING, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode CURRENT() { return getToken(BaseRuleParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public WindowFrameFollowingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFrameFollowing; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowFrameFollowing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowFrameFollowing(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowFrameFollowing(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFrameFollowingContext windowFrameFollowing() throws RecognitionException {
		WindowFrameFollowingContext _localctx = new WindowFrameFollowingContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_windowFrameFollowing);
		try {
			setState(700);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNBOUNDED:
				enterOuterAlt(_localctx, 1);
				{
				setState(694);
				match(UNBOUNDED);
				setState(695);
				match(FOLLOWING);
				}
				break;
			case NUMBER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(696);
				match(NUMBER_);
				setState(697);
				match(FOLLOWING);
				}
				break;
			case CURRENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(698);
				match(CURRENT);
				setState(699);
				match(ROW);
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

	public static class ColumnNameWithSortContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode ASC() { return getToken(BaseRuleParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(BaseRuleParser.DESC, 0); }
		public ColumnNameWithSortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnNameWithSort; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnNameWithSort(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnNameWithSort(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnNameWithSort(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameWithSortContext columnNameWithSort() throws RecognitionException {
		ColumnNameWithSortContext _localctx = new ColumnNameWithSortContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_columnNameWithSort);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(702);
			columnName();
			setState(704);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(703);
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

	public static class IndexOptionContext extends ParserRuleContext {
		public TerminalNode FILLFACTOR() { return getToken(BaseRuleParser.FILLFACTOR, 0); }
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public EqOnOffOptionContext eqOnOffOption() {
			return getRuleContext(EqOnOffOptionContext.class,0);
		}
		public EqTimeContext eqTime() {
			return getRuleContext(EqTimeContext.class,0);
		}
		public TerminalNode COMPRESSION_DELAY() { return getToken(BaseRuleParser.COMPRESSION_DELAY, 0); }
		public TerminalNode MAX_DURATION() { return getToken(BaseRuleParser.MAX_DURATION, 0); }
		public TerminalNode MAXDOP() { return getToken(BaseRuleParser.MAXDOP, 0); }
		public CompressionOptionContext compressionOption() {
			return getRuleContext(CompressionOptionContext.class,0);
		}
		public OnPartitionClauseContext onPartitionClause() {
			return getRuleContext(OnPartitionClauseContext.class,0);
		}
		public IndexOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndexOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndexOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndexOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexOptionContext indexOption() throws RecognitionException {
		IndexOptionContext _localctx = new IndexOptionContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_indexOption);
		int _la;
		try {
			setState(719);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(706);
				match(FILLFACTOR);
				setState(707);
				match(EQ_);
				setState(708);
				match(NUMBER_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(709);
				eqOnOffOption();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(710);
				_la = _input.LA(1);
				if ( !(_la==COMPRESSION_DELAY || _la==MAX_DURATION) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(711);
				eqTime();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(712);
				match(MAXDOP);
				setState(713);
				match(EQ_);
				setState(714);
				match(NUMBER_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(715);
				compressionOption();
				setState(717);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(716);
					onPartitionClause();
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

	public static class CompressionOptionContext extends ParserRuleContext {
		public TerminalNode DATA_COMPRESSION() { return getToken(BaseRuleParser.DATA_COMPRESSION, 0); }
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode NONE() { return getToken(BaseRuleParser.NONE, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public TerminalNode PAGE() { return getToken(BaseRuleParser.PAGE, 0); }
		public TerminalNode COLUMNSTORE() { return getToken(BaseRuleParser.COLUMNSTORE, 0); }
		public TerminalNode COLUMNSTORE_ARCHIVE() { return getToken(BaseRuleParser.COLUMNSTORE_ARCHIVE, 0); }
		public CompressionOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compressionOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCompressionOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCompressionOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCompressionOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompressionOptionContext compressionOption() throws RecognitionException {
		CompressionOptionContext _localctx = new CompressionOptionContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_compressionOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(721);
			match(DATA_COMPRESSION);
			setState(722);
			match(EQ_);
			setState(723);
			_la = _input.LA(1);
			if ( !(((((_la - 205)) & ~0x3f) == 0 && ((1L << (_la - 205)) & ((1L << (ROW - 205)) | (1L << (COLUMNSTORE - 205)) | (1L << (NONE - 205)) | (1L << (PAGE - 205)))) != 0) || _la==COLUMNSTORE_ARCHIVE) ) {
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

	public static class EqTimeContext extends ParserRuleContext {
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode MINUTES() { return getToken(BaseRuleParser.MINUTES, 0); }
		public EqTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eqTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEqTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEqTime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEqTime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqTimeContext eqTime() throws RecognitionException {
		EqTimeContext _localctx = new EqTimeContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_eqTime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(725);
			match(EQ_);
			setState(726);
			match(NUMBER_);
			setState(728);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUTES) {
				{
				setState(727);
				match(MINUTES);
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

	public static class EqOnOffOptionContext extends ParserRuleContext {
		public EqKeyContext eqKey() {
			return getRuleContext(EqKeyContext.class,0);
		}
		public EqOnOffContext eqOnOff() {
			return getRuleContext(EqOnOffContext.class,0);
		}
		public EqOnOffOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eqOnOffOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEqOnOffOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEqOnOffOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEqOnOffOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqOnOffOptionContext eqOnOffOption() throws RecognitionException {
		EqOnOffOptionContext _localctx = new EqOnOffOptionContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_eqOnOffOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(730);
			eqKey();
			setState(731);
			eqOnOff();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EqKeyContext extends ParserRuleContext {
		public TerminalNode PAD_INDEX() { return getToken(BaseRuleParser.PAD_INDEX, 0); }
		public TerminalNode SORT_IN_TEMPDB() { return getToken(BaseRuleParser.SORT_IN_TEMPDB, 0); }
		public TerminalNode IGNORE_DUP_KEY() { return getToken(BaseRuleParser.IGNORE_DUP_KEY, 0); }
		public TerminalNode STATISTICS_NORECOMPUTE() { return getToken(BaseRuleParser.STATISTICS_NORECOMPUTE, 0); }
		public TerminalNode STATISTICS_INCREMENTAL() { return getToken(BaseRuleParser.STATISTICS_INCREMENTAL, 0); }
		public TerminalNode DROP_EXISTING() { return getToken(BaseRuleParser.DROP_EXISTING, 0); }
		public TerminalNode ONLINE() { return getToken(BaseRuleParser.ONLINE, 0); }
		public TerminalNode RESUMABLE() { return getToken(BaseRuleParser.RESUMABLE, 0); }
		public TerminalNode ALLOW_ROW_LOCKS() { return getToken(BaseRuleParser.ALLOW_ROW_LOCKS, 0); }
		public TerminalNode ALLOW_PAGE_LOCKS() { return getToken(BaseRuleParser.ALLOW_PAGE_LOCKS, 0); }
		public TerminalNode COMPRESSION_DELAY() { return getToken(BaseRuleParser.COMPRESSION_DELAY, 0); }
		public TerminalNode OPTIMIZE_FOR_SEQUENTIAL_KEY() { return getToken(BaseRuleParser.OPTIMIZE_FOR_SEQUENTIAL_KEY, 0); }
		public EqKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eqKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEqKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEqKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEqKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqKeyContext eqKey() throws RecognitionException {
		EqKeyContext _localctx = new EqKeyContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_eqKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(733);
			_la = _input.LA(1);
			if ( !(_la==ONLINE || _la==RESUMABLE || ((((_la - 363)) & ~0x3f) == 0 && ((1L << (_la - 363)) & ((1L << (ALLOW_PAGE_LOCKS - 363)) | (1L << (ALLOW_ROW_LOCKS - 363)) | (1L << (COMPRESSION_DELAY - 363)) | (1L << (STATISTICS_INCREMENTAL - 363)) | (1L << (STATISTICS_NORECOMPUTE - 363)) | (1L << (SORT_IN_TEMPDB - 363)) | (1L << (IGNORE_DUP_KEY - 363)) | (1L << (PAD_INDEX - 363)) | (1L << (DROP_EXISTING - 363)))) != 0) || _la==OPTIMIZE_FOR_SEQUENTIAL_KEY) ) {
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

	public static class EqOnOffContext extends ParserRuleContext {
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode OFF() { return getToken(BaseRuleParser.OFF, 0); }
		public EqOnOffContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eqOnOff; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEqOnOff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEqOnOff(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEqOnOff(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqOnOffContext eqOnOff() throws RecognitionException {
		EqOnOffContext _localctx = new EqOnOffContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_eqOnOff);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(735);
			match(EQ_);
			setState(736);
			_la = _input.LA(1);
			if ( !(_la==ON || _la==OFF) ) {
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

	public static class OnPartitionClauseContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode PARTITIONS() { return getToken(BaseRuleParser.PARTITIONS, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public PartitionExpressionsContext partitionExpressions() {
			return getRuleContext(PartitionExpressionsContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public OnPartitionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_onPartitionClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOnPartitionClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOnPartitionClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOnPartitionClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OnPartitionClauseContext onPartitionClause() throws RecognitionException {
		OnPartitionClauseContext _localctx = new OnPartitionClauseContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_onPartitionClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(738);
			match(ON);
			setState(739);
			match(PARTITIONS);
			setState(740);
			match(LP_);
			setState(741);
			partitionExpressions();
			setState(742);
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

	public static class PartitionExpressionsContext extends ParserRuleContext {
		public List<PartitionExpressionContext> partitionExpression() {
			return getRuleContexts(PartitionExpressionContext.class);
		}
		public PartitionExpressionContext partitionExpression(int i) {
			return getRuleContext(PartitionExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public PartitionExpressionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionExpressions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionExpressions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionExpressions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionExpressions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionExpressionsContext partitionExpressions() throws RecognitionException {
		PartitionExpressionsContext _localctx = new PartitionExpressionsContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_partitionExpressions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(744);
			partitionExpression();
			setState(749);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(745);
				match(COMMA_);
				setState(746);
				partitionExpression();
				}
				}
				setState(751);
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

	public static class PartitionExpressionContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public NumberRangeContext numberRange() {
			return getRuleContext(NumberRangeContext.class,0);
		}
		public PartitionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionExpressionContext partitionExpression() throws RecognitionException {
		PartitionExpressionContext _localctx = new PartitionExpressionContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_partitionExpression);
		try {
			setState(754);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(752);
				match(NUMBER_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(753);
				numberRange();
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

	public static class NumberRangeContext extends ParserRuleContext {
		public List<TerminalNode> NUMBER_() { return getTokens(BaseRuleParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(BaseRuleParser.NUMBER_, i);
		}
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public NumberRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNumberRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNumberRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNumberRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberRangeContext numberRange() throws RecognitionException {
		NumberRangeContext _localctx = new NumberRangeContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_numberRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(756);
			match(NUMBER_);
			setState(757);
			match(TO);
			setState(758);
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

	public static class LowPriorityLockWaitContext extends ParserRuleContext {
		public TerminalNode WAIT_AT_LOW_PRIORITY() { return getToken(BaseRuleParser.WAIT_AT_LOW_PRIORITY, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode MAX_DURATION() { return getToken(BaseRuleParser.MAX_DURATION, 0); }
		public List<TerminalNode> EQ_() { return getTokens(BaseRuleParser.EQ_); }
		public TerminalNode EQ_(int i) {
			return getToken(BaseRuleParser.EQ_, i);
		}
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public TerminalNode ABORT_AFTER_WAIT() { return getToken(BaseRuleParser.ABORT_AFTER_WAIT, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode NONE() { return getToken(BaseRuleParser.NONE, 0); }
		public TerminalNode SELF() { return getToken(BaseRuleParser.SELF, 0); }
		public TerminalNode BLOCKERS() { return getToken(BaseRuleParser.BLOCKERS, 0); }
		public TerminalNode MINUTES() { return getToken(BaseRuleParser.MINUTES, 0); }
		public LowPriorityLockWaitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lowPriorityLockWait; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLowPriorityLockWait(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLowPriorityLockWait(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLowPriorityLockWait(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LowPriorityLockWaitContext lowPriorityLockWait() throws RecognitionException {
		LowPriorityLockWaitContext _localctx = new LowPriorityLockWaitContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_lowPriorityLockWait);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(760);
			match(WAIT_AT_LOW_PRIORITY);
			setState(761);
			match(LP_);
			setState(762);
			match(MAX_DURATION);
			setState(763);
			match(EQ_);
			setState(764);
			match(NUMBER_);
			setState(766);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUTES) {
				{
				setState(765);
				match(MINUTES);
				}
			}

			setState(768);
			match(COMMA_);
			setState(769);
			match(ABORT_AFTER_WAIT);
			setState(770);
			match(EQ_);
			setState(771);
			_la = _input.LA(1);
			if ( !(((((_la - 224)) & ~0x3f) == 0 && ((1L << (_la - 224)) & ((1L << (BLOCKERS - 224)) | (1L << (NONE - 224)) | (1L << (SELF - 224)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(772);
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

	public static class OnLowPriorLockWaitContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public LowPriorityLockWaitContext lowPriorityLockWait() {
			return getRuleContext(LowPriorityLockWaitContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public OnLowPriorLockWaitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_onLowPriorLockWait; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOnLowPriorLockWait(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOnLowPriorLockWait(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOnLowPriorLockWait(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OnLowPriorLockWaitContext onLowPriorLockWait() throws RecognitionException {
		OnLowPriorLockWaitContext _localctx = new OnLowPriorLockWaitContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_onLowPriorLockWait);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(774);
			match(ON);
			setState(779);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(775);
				match(LP_);
				setState(776);
				lowPriorityLockWait();
				setState(777);
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

	public static class IgnoredIdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public IgnoredIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ignoredIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIgnoredIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIgnoredIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIgnoredIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IgnoredIdentifierContext ignoredIdentifier() throws RecognitionException {
		IgnoredIdentifierContext _localctx = new IgnoredIdentifierContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_ignoredIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(781);
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

	public static class IgnoredIdentifiersContext extends ParserRuleContext {
		public List<IgnoredIdentifierContext> ignoredIdentifier() {
			return getRuleContexts(IgnoredIdentifierContext.class);
		}
		public IgnoredIdentifierContext ignoredIdentifier(int i) {
			return getRuleContext(IgnoredIdentifierContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public IgnoredIdentifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ignoredIdentifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIgnoredIdentifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIgnoredIdentifiers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIgnoredIdentifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IgnoredIdentifiersContext ignoredIdentifiers() throws RecognitionException {
		IgnoredIdentifiersContext _localctx = new IgnoredIdentifiersContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_ignoredIdentifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(783);
			ignoredIdentifier();
			setState(788);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(784);
				match(COMMA_);
				setState(785);
				ignoredIdentifier();
				}
				}
				setState(790);
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

	public static class MatchNoneContext extends ParserRuleContext {
		public MatchNoneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchNone; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterMatchNone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitMatchNone(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitMatchNone(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchNoneContext matchNone() throws RecognitionException {
		MatchNoneContext _localctx = new MatchNoneContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_matchNone);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(791);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
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
		case 24:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 27:
			return booleanPrimary_sempred((BooleanPrimaryContext)_localctx, predIndex);
		case 30:
			return bitExpr_sempred((BitExprContext)_localctx, predIndex);
		case 31:
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
			return precpred(_ctx, 11);
		case 6:
			return precpred(_ctx, 10);
		case 7:
			return precpred(_ctx, 9);
		case 8:
			return precpred(_ctx, 8);
		case 9:
			return precpred(_ctx, 7);
		case 10:
			return precpred(_ctx, 6);
		case 11:
			return precpred(_ctx, 5);
		case 12:
			return precpred(_ctx, 4);
		case 13:
			return precpred(_ctx, 3);
		case 14:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean simpleExpr_sempred(SimpleExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 15:
			return precpred(_ctx, 7);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u01d6\u031c\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\5\3\u00a8\n\3\3\4\3\4\3\5\5\5\u00ad\n\5\3\5\3\5\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\5\6\u00b8\n\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\5"+
		"\13\u00c4\n\13\3\f\3\f\3\r\3\r\3\16\3\16\3\16\5\16\u00cd\n\16\3\16\3\16"+
		"\3\17\3\17\3\17\5\17\u00d4\n\17\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22"+
		"\3\22\3\22\7\22\u00e0\n\22\f\22\16\22\u00e3\13\22\3\22\3\22\3\23\3\23"+
		"\3\23\3\23\7\23\u00eb\n\23\f\23\16\23\u00ee\13\23\3\23\3\23\3\24\5\24"+
		"\u00f3\n\24\3\24\3\24\3\24\7\24\u00f8\n\24\f\24\16\24\u00fb\13\24\3\24"+
		"\5\24\u00fe\n\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\30\5\30"+
		"\u010a\n\30\5\30\u010c\n\30\3\30\3\30\3\31\5\31\u0111\n\31\3\31\3\31\3"+
		"\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u011e\n\32\3\32\3\32"+
		"\3\32\3\32\7\32\u0124\n\32\f\32\16\32\u0127\13\32\3\33\3\33\3\34\3\34"+
		"\3\35\3\35\3\35\3\35\3\35\3\35\5\35\u0133\n\35\3\35\3\35\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\7\35\u0142\n\35\f\35\16\35\u0145"+
		"\13\35\3\36\3\36\3\37\3\37\5\37\u014b\n\37\3\37\3\37\3\37\3\37\3\37\5"+
		"\37\u0152\n\37\3\37\3\37\3\37\3\37\3\37\7\37\u0159\n\37\f\37\16\37\u015c"+
		"\13\37\3\37\3\37\3\37\3\37\5\37\u0162\n\37\3\37\3\37\3\37\3\37\3\37\3"+
		"\37\3\37\5\37\u016b\n\37\3\37\3\37\3\37\3\37\5\37\u0171\n\37\3\37\5\37"+
		"\u0174\n\37\3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3"+
		" \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \7 \u0197\n \f \16 \u019a\13 "+
		"\3!\3!\3!\3!\3!\3!\3!\3!\5!\u01a4\n!\3!\3!\3!\3!\7!\u01aa\n!\f!\16!\u01ad"+
		"\13!\3!\3!\3!\5!\u01b2\n!\3!\3!\3!\3!\3!\3!\3!\3!\5!\u01bc\n!\3!\3!\3"+
		"!\7!\u01c1\n!\f!\16!\u01c4\13!\3\"\3\"\3\"\5\"\u01c9\n\"\3#\3#\3#\5#\u01ce"+
		"\n#\3#\3#\3#\7#\u01d3\n#\f#\16#\u01d6\13#\3#\5#\u01d9\n#\3#\3#\3$\3$\3"+
		"%\3%\3&\3&\5&\u01e3\n&\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\7(\u01f1"+
		"\n(\f(\16(\u01f4\13(\3(\3(\5(\u01f8\n(\3(\3(\3)\3)\3)\3)\3)\7)\u0201\n"+
		")\f)\16)\u0204\13)\3)\5)\u0207\n)\3)\3)\3*\3*\3*\3*\3*\5*\u0210\n*\3+"+
		"\3+\5+\u0214\n+\3+\6+\u0217\n+\r+\16+\u0218\3+\5+\u021c\n+\3,\3,\3,\3"+
		",\3,\3-\3-\3-\3.\3.\3.\3.\5.\u022a\n.\3/\3/\3\60\3\60\3\60\3\60\3\60\7"+
		"\60\u0233\n\60\f\60\16\60\u0236\13\60\3\60\3\60\3\60\3\60\3\60\3\60\3"+
		"\60\3\60\3\60\5\60\u0241\n\60\5\60\u0243\n\60\3\61\3\61\3\61\5\61\u0248"+
		"\n\61\3\61\3\61\5\61\u024c\n\61\3\61\5\61\u024f\n\61\3\62\3\62\3\62\5"+
		"\62\u0254\n\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\5\62\u025d\n\62\3\62"+
		"\3\62\3\62\5\62\u0262\n\62\3\63\3\63\3\64\3\64\3\64\3\64\5\64\u026a\n"+
		"\64\3\64\3\64\3\65\3\65\3\65\3\65\3\65\3\65\3\65\3\65\5\65\u0276\n\65"+
		"\3\65\3\65\3\66\3\66\3\66\3\66\3\66\5\66\u027f\n\66\3\66\3\66\3\66\3\66"+
		"\5\66\u0285\n\66\3\67\3\67\3\67\38\38\38\58\u028d\n8\38\58\u0290\n8\3"+
		"8\58\u0293\n8\38\38\39\39\39\39\39\79\u029c\n9\f9\169\u029f\139\3:\3:"+
		"\3:\3;\3;\5;\u02a6\n;\3<\3<\3<\3<\3<\3=\3=\5=\u02af\n=\3>\3>\3>\3>\3>"+
		"\3>\5>\u02b7\n>\3?\3?\3?\3?\3?\3?\5?\u02bf\n?\3@\3@\5@\u02c3\n@\3A\3A"+
		"\3A\3A\3A\3A\3A\3A\3A\3A\3A\5A\u02d0\nA\5A\u02d2\nA\3B\3B\3B\3B\3C\3C"+
		"\3C\5C\u02db\nC\3D\3D\3D\3E\3E\3F\3F\3F\3G\3G\3G\3G\3G\3G\3H\3H\3H\7H"+
		"\u02ee\nH\fH\16H\u02f1\13H\3I\3I\5I\u02f5\nI\3J\3J\3J\3J\3K\3K\3K\3K\3"+
		"K\3K\5K\u0301\nK\3K\3K\3K\3K\3K\3K\3L\3L\3L\3L\3L\5L\u030e\nL\3M\3M\3"+
		"N\3N\3N\7N\u0315\nN\fN\16N\u0318\13N\3O\3O\3O\2\6\628>@P\2\4\6\b\n\f\16"+
		"\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bd"+
		"fhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092"+
		"\u0094\u0096\u0098\u009a\u009c\2\30\3\2\u0083\u0085\3\2ij \2\64\64DEw"+
		"x|}\u0081\u0081\u0086\u0087\u0089\u0089\u008b\u008b\u0090\u0095\u0098"+
		"\u0099\u009b\u009b\u009d\u009e\u00a0\u00a2\u00a5\u00a6\u00a9\u00a9\u00ac"+
		"\u00ac\u00c8\u00c8\u00ca\u00cf\u00d2\u00d3\u00dc\u00dd\u00e0\u00e4\u00e6"+
		"\u00e7\u00e9\u00f4\u00f6\u00fb\u00fd\u0105\u0107\u011c\u0123\u0195\u0197"+
		"\u0197\u019d\u019d\u01a9\u01ab\3\2\u01ce\u01cf\4\2\4\5de\4\2\6\6gg\4\2"+
		"hj\u00d1\u00d1\3\2no\3\2\30\35\5\2\6\7\17\20\u00c8\u00c8\3\2\u0091\u0095"+
		"\3\2\u00cf\u00d0\4\2\u00a5\u00a5\u0197\u0197\3\2tu\4\2\u00e7\u00e7\u00f2"+
		"\u00f2\n\2\177\177\u0083\u0084\u00aa\u00ab\u00ad\u00b5\u00c8\u00c8\u013c"+
		"\u013c\u0199\u01a8\u01ce\u01ce\4\2\u00d0\u00d0\u0111\u0111\4\2\u0174\u0174"+
		"\u0185\u0185\7\2\u00cf\u00cf\u00e6\u00e6\u0106\u0106\u010b\u010b\u0171"+
		"\u0171\13\2\u0109\u0109\u0115\u0115\u016d\u016e\u0174\u0174\u017d\u017e"+
		"\u0182\u0183\u0188\u0188\u0194\u0194\u01ca\u01ca\4\2^^\u0108\u0108\5\2"+
		"\u00e2\u00e2\u0106\u0106\u0118\u0118\2\u0345\2\u009e\3\2\2\2\4\u00a7\3"+
		"\2\2\2\6\u00a9\3\2\2\2\b\u00ac\3\2\2\2\n\u00b7\3\2\2\2\f\u00b9\3\2\2\2"+
		"\16\u00bb\3\2\2\2\20\u00bd\3\2\2\2\22\u00bf\3\2\2\2\24\u00c3\3\2\2\2\26"+
		"\u00c5\3\2\2\2\30\u00c7\3\2\2\2\32\u00cc\3\2\2\2\34\u00d3\3\2\2\2\36\u00d7"+
		"\3\2\2\2 \u00d9\3\2\2\2\"\u00db\3\2\2\2$\u00e6\3\2\2\2&\u00f2\3\2\2\2"+
		"(\u00ff\3\2\2\2*\u0101\3\2\2\2,\u0103\3\2\2\2.\u0105\3\2\2\2\60\u0110"+
		"\3\2\2\2\62\u011d\3\2\2\2\64\u0128\3\2\2\2\66\u012a\3\2\2\28\u012c\3\2"+
		"\2\2:\u0146\3\2\2\2<\u0173\3\2\2\2>\u0175\3\2\2\2@\u01bb\3\2\2\2B\u01c8"+
		"\3\2\2\2D\u01ca\3\2\2\2F\u01dc\3\2\2\2H\u01de\3\2\2\2J\u01e2\3\2\2\2L"+
		"\u01e4\3\2\2\2N\u01eb\3\2\2\2P\u01fb\3\2\2\2R\u020f\3\2\2\2T\u0211\3\2"+
		"\2\2V\u021d\3\2\2\2X\u0222\3\2\2\2Z\u0229\3\2\2\2\\\u022b\3\2\2\2^\u022d"+
		"\3\2\2\2`\u0247\3\2\2\2b\u0253\3\2\2\2d\u0263\3\2\2\2f\u0265\3\2\2\2h"+
		"\u026d\3\2\2\2j\u0279\3\2\2\2l\u0286\3\2\2\2n\u0289\3\2\2\2p\u0296\3\2"+
		"\2\2r\u02a0\3\2\2\2t\u02a5\3\2\2\2v\u02a7\3\2\2\2x\u02ae\3\2\2\2z\u02b6"+
		"\3\2\2\2|\u02be\3\2\2\2~\u02c0\3\2\2\2\u0080\u02d1\3\2\2\2\u0082\u02d3"+
		"\3\2\2\2\u0084\u02d7\3\2\2\2\u0086\u02dc\3\2\2\2\u0088\u02df\3\2\2\2\u008a"+
		"\u02e1\3\2\2\2\u008c\u02e4\3\2\2\2\u008e\u02ea\3\2\2\2\u0090\u02f4\3\2"+
		"\2\2\u0092\u02f6\3\2\2\2\u0094\u02fa\3\2\2\2\u0096\u0308\3\2\2\2\u0098"+
		"\u030f\3\2\2\2\u009a\u0311\3\2\2\2\u009c\u0319\3\2\2\2\u009e\u009f\7)"+
		"\2\2\u009f\3\3\2\2\2\u00a0\u00a8\5\6\4\2\u00a1\u00a8\5\b\5\2\u00a2\u00a8"+
		"\5\n\6\2\u00a3\u00a8\5\f\7\2\u00a4\u00a8\5\16\b\2\u00a5\u00a8\5\20\t\2"+
		"\u00a6\u00a8\5\22\n\2\u00a7\u00a0\3\2\2\2\u00a7\u00a1\3\2\2\2\u00a7\u00a2"+
		"\3\2\2\2\u00a7\u00a3\3\2\2\2\u00a7\u00a4\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a7"+
		"\u00a6\3\2\2\2\u00a8\5\3\2\2\2\u00a9\u00aa\7\u01cf\2\2\u00aa\7\3\2\2\2"+
		"\u00ab\u00ad\7\20\2\2\u00ac\u00ab\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00ae"+
		"\3\2\2\2\u00ae\u00af\7\u01d0\2\2\u00af\t\3\2\2\2\u00b0\u00b1\t\2\2\2\u00b1"+
		"\u00b8\7\u01cf\2\2\u00b2\u00b3\7!\2\2\u00b3\u00b4\5\24\13\2\u00b4\u00b5"+
		"\7\u01cf\2\2\u00b5\u00b6\7\"\2\2\u00b6\u00b8\3\2\2\2\u00b7\u00b0\3\2\2"+
		"\2\u00b7\u00b2\3\2\2\2\u00b8\13\3\2\2\2\u00b9\u00ba\7\u01d4\2\2\u00ba"+
		"\r\3\2\2\2\u00bb\u00bc\7\u01d5\2\2\u00bc\17\3\2\2\2\u00bd\u00be\t\3\2"+
		"\2\u00be\21\3\2\2\2\u00bf\u00c0\7h\2\2\u00c0\23\3\2\2\2\u00c1\u00c4\7"+
		"\u01ce\2\2\u00c2\u00c4\5\26\f\2\u00c3\u00c1\3\2\2\2\u00c3\u00c2\3\2\2"+
		"\2\u00c4\25\3\2\2\2\u00c5\u00c6\t\4\2\2\u00c6\27\3\2\2\2\u00c7\u00c8\5"+
		"\24\13\2\u00c8\31\3\2\2\2\u00c9\u00ca\5\36\20\2\u00ca\u00cb\7\24\2\2\u00cb"+
		"\u00cd\3\2\2\2\u00cc\u00c9\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cd\u00ce\3\2"+
		"\2\2\u00ce\u00cf\5 \21\2\u00cf\33\3\2\2\2\u00d0\u00d1\5\36\20\2\u00d1"+
		"\u00d2\7\24\2\2\u00d2\u00d4\3\2\2\2\u00d3\u00d0\3\2\2\2\u00d3\u00d4\3"+
		"\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\5 \21\2\u00d6\35\3\2\2\2\u00d7"+
		"\u00d8\5\24\13\2\u00d8\37\3\2\2\2\u00d9\u00da\5\24\13\2\u00da!\3\2\2\2"+
		"\u00db\u00dc\7\37\2\2\u00dc\u00e1\5\34\17\2\u00dd\u00de\7%\2\2\u00de\u00e0"+
		"\5\34\17\2\u00df\u00dd\3\2\2\2\u00e0\u00e3\3\2\2\2\u00e1\u00df\3\2\2\2"+
		"\u00e1\u00e2\3\2\2\2\u00e2\u00e4\3\2\2\2\u00e3\u00e1\3\2\2\2\u00e4\u00e5"+
		"\7 \2\2\u00e5#\3\2\2\2\u00e6\u00e7\7\37\2\2\u00e7\u00ec\5~@\2\u00e8\u00e9"+
		"\7%\2\2\u00e9\u00eb\5~@\2\u00ea\u00e8\3\2\2\2\u00eb\u00ee\3\2\2\2\u00ec"+
		"\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00ef\3\2\2\2\u00ee\u00ec\3\2"+
		"\2\2\u00ef\u00f0\7 \2\2\u00f0%\3\2\2\2\u00f1\u00f3\7\37\2\2\u00f2\u00f1"+
		"\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00f9\5\32\16\2"+
		"\u00f5\u00f6\7%\2\2\u00f6\u00f8\5\32\16\2\u00f7\u00f5\3\2\2\2\u00f8\u00fb"+
		"\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fd\3\2\2\2\u00fb"+
		"\u00f9\3\2\2\2\u00fc\u00fe\7 \2\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe\3\2"+
		"\2\2\u00fe\'\3\2\2\2\u00ff\u0100\5\24\13\2\u0100)\3\2\2\2\u0101\u0102"+
		"\t\5\2\2\u0102+\3\2\2\2\u0103\u0104\7\u01ce\2\2\u0104-\3\2\2\2\u0105\u010b"+
		"\7\37\2\2\u0106\u0109\7\u01d0\2\2\u0107\u0108\7%\2\2\u0108\u010a\7\u01d0"+
		"\2\2\u0109\u0107\3\2\2\2\u0109\u010a\3\2\2\2\u010a\u010c\3\2\2\2\u010b"+
		"\u0106\3\2\2\2\u010b\u010c\3\2\2\2\u010c\u010d\3\2\2\2\u010d\u010e\7 "+
		"\2\2\u010e/\3\2\2\2\u010f\u0111\7>\2\2\u0110\u010f\3\2\2\2\u0110\u0111"+
		"\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0113\7A\2\2\u0113\61\3\2\2\2\u0114"+
		"\u0115\b\32\1\2\u0115\u0116\5\66\34\2\u0116\u0117\5\62\32\5\u0117\u011e"+
		"\3\2\2\2\u0118\u0119\7\37\2\2\u0119\u011a\5\62\32\2\u011a\u011b\7 \2\2"+
		"\u011b\u011e\3\2\2\2\u011c\u011e\58\35\2\u011d\u0114\3\2\2\2\u011d\u0118"+
		"\3\2\2\2\u011d\u011c\3\2\2\2\u011e\u0125\3\2\2\2\u011f\u0120\f\6\2\2\u0120"+
		"\u0121\5\64\33\2\u0121\u0122\5\62\32\7\u0122\u0124\3\2\2\2\u0123\u011f"+
		"\3\2\2\2\u0124\u0127\3\2\2\2\u0125\u0123\3\2\2\2\u0125\u0126\3\2\2\2\u0126"+
		"\63\3\2\2\2\u0127\u0125\3\2\2\2\u0128\u0129\t\6\2\2\u0129\65\3\2\2\2\u012a"+
		"\u012b\t\7\2\2\u012b\67\3\2\2\2\u012c\u012d\b\35\1\2\u012d\u012e\5<\37"+
		"\2\u012e\u0143\3\2\2\2\u012f\u0130\f\7\2\2\u0130\u0132\7f\2\2\u0131\u0133"+
		"\7g\2\2\u0132\u0131\3\2\2\2\u0132\u0133\3\2\2\2\u0133\u0134\3\2\2\2\u0134"+
		"\u0142\t\b\2\2\u0135\u0136\f\6\2\2\u0136\u0137\7\26\2\2\u0137\u0142\5"+
		"<\37\2\u0138\u0139\f\5\2\2\u0139\u013a\5:\36\2\u013a\u013b\5<\37\2\u013b"+
		"\u0142\3\2\2\2\u013c\u013d\f\4\2\2\u013d\u013e\5:\36\2\u013e\u013f\t\t"+
		"\2\2\u013f\u0140\5\\/\2\u0140\u0142\3\2\2\2\u0141\u012f\3\2\2\2\u0141"+
		"\u0135\3\2\2\2\u0141\u0138\3\2\2\2\u0141\u013c\3\2\2\2\u0142\u0145\3\2"+
		"\2\2\u0143\u0141\3\2\2\2\u0143\u0144\3\2\2\2\u01449\3\2\2\2\u0145\u0143"+
		"\3\2\2\2\u0146\u0147\t\n\2\2\u0147;\3\2\2\2\u0148\u014a\5> \2\u0149\u014b"+
		"\7g\2\2\u014a\u0149\3\2\2\2\u014a\u014b\3\2\2\2\u014b\u014c\3\2\2\2\u014c"+
		"\u014d\7m\2\2\u014d\u014e\5\\/\2\u014e\u0174\3\2\2\2\u014f\u0151\5> \2"+
		"\u0150\u0152\7g\2\2\u0151\u0150\3\2\2\2\u0151\u0152\3\2\2\2\u0152\u0153"+
		"\3\2\2\2\u0153\u0154\7m\2\2\u0154\u0155\7\37\2\2\u0155\u015a\5\62\32\2"+
		"\u0156\u0157\7%\2\2\u0157\u0159\5\62\32\2\u0158\u0156\3\2\2\2\u0159\u015c"+
		"\3\2\2\2\u015a\u0158\3\2\2\2\u015a\u015b\3\2\2\2\u015b\u015d\3\2\2\2\u015c"+
		"\u015a\3\2\2\2\u015d\u015e\7 \2\2\u015e\u0174\3\2\2\2\u015f\u0161\5> "+
		"\2\u0160\u0162\7g\2\2\u0161\u0160\3\2\2\2\u0161\u0162\3\2\2\2\u0162\u0163"+
		"\3\2\2\2\u0163\u0164\7l\2\2\u0164\u0165\5> \2\u0165\u0166\7d\2\2\u0166"+
		"\u0167\5<\37\2\u0167\u0174\3\2\2\2\u0168\u016a\5> \2\u0169\u016b\7g\2"+
		"\2\u016a\u0169\3\2\2\2\u016a\u016b\3\2\2\2\u016b\u016c\3\2\2\2\u016c\u016d"+
		"\7p\2\2\u016d\u0170\5@!\2\u016e\u016f\7\u00c9\2\2\u016f\u0171\5@!\2\u0170"+
		"\u016e\3\2\2\2\u0170\u0171\3\2\2\2\u0171\u0174\3\2\2\2\u0172\u0174\5>"+
		" \2\u0173\u0148\3\2\2\2\u0173\u014f\3\2\2\2\u0173\u015f\3\2\2\2\u0173"+
		"\u0168\3\2\2\2\u0173\u0172\3\2\2\2\u0174=\3\2\2\2\u0175\u0176\b \1\2\u0176"+
		"\u0177\5@!\2\u0177\u0198\3\2\2\2\u0178\u0179\f\r\2\2\u0179\u017a\7\b\2"+
		"\2\u017a\u0197\5> \16\u017b\u017c\f\f\2\2\u017c\u017d\7\t\2\2\u017d\u0197"+
		"\5> \r\u017e\u017f\f\13\2\2\u017f\u0180\7\n\2\2\u0180\u0197\5> \f\u0181"+
		"\u0182\f\n\2\2\u0182\u0183\7\13\2\2\u0183\u0197\5> \13\u0184\u0185\f\t"+
		"\2\2\u0185\u0186\7\17\2\2\u0186\u0197\5> \n\u0187\u0188\f\b\2\2\u0188"+
		"\u0189\7\20\2\2\u0189\u0197\5> \t\u018a\u018b\f\7\2\2\u018b\u018c\7\21"+
		"\2\2\u018c\u0197\5> \b\u018d\u018e\f\6\2\2\u018e\u018f\7\22\2\2\u018f"+
		"\u0197\5> \7\u0190\u0191\f\5\2\2\u0191\u0192\7\r\2\2\u0192\u0197\5> \6"+
		"\u0193\u0194\f\4\2\2\u0194\u0195\7\f\2\2\u0195\u0197\5> \5\u0196\u0178"+
		"\3\2\2\2\u0196\u017b\3\2\2\2\u0196\u017e\3\2\2\2\u0196\u0181\3\2\2\2\u0196"+
		"\u0184\3\2\2\2\u0196\u0187\3\2\2\2\u0196\u018a\3\2\2\2\u0196\u018d\3\2"+
		"\2\2\u0196\u0190\3\2\2\2\u0196\u0193\3\2\2\2\u0197\u019a\3\2\2\2\u0198"+
		"\u0196\3\2\2\2\u0198\u0199\3\2\2\2\u0199?\3\2\2\2\u019a\u0198\3\2\2\2"+
		"\u019b\u019c\b!\1\2\u019c\u01bc\5B\"\2\u019d\u01bc\5\2\2\2\u019e\u01bc"+
		"\5\4\3\2\u019f\u01bc\5\34\17\2\u01a0\u01a1\t\13\2\2\u01a1\u01bc\5@!\b"+
		"\u01a2\u01a4\7\u00cf\2\2\u01a3\u01a2\3\2\2\2\u01a3\u01a4\3\2\2\2\u01a4"+
		"\u01a5\3\2\2\2\u01a5\u01a6\7\37\2\2\u01a6\u01ab\5\62\32\2\u01a7\u01a8"+
		"\7%\2\2\u01a8\u01aa\5\62\32\2\u01a9\u01a7\3\2\2\2\u01aa\u01ad\3\2\2\2"+
		"\u01ab\u01a9\3\2\2\2\u01ab\u01ac\3\2\2\2\u01ac\u01ae\3\2\2\2\u01ad\u01ab"+
		"\3\2\2\2\u01ae\u01af\7 \2\2\u01af\u01bc\3\2\2\2\u01b0\u01b2\7k\2\2\u01b1"+
		"\u01b0\3\2\2\2\u01b1\u01b2\3\2\2\2\u01b2\u01b3\3\2\2\2\u01b3\u01bc\5\\"+
		"/\2\u01b4\u01b5\7!\2\2\u01b5\u01b6\5\24\13\2\u01b6\u01b7\5\62\32\2\u01b7"+
		"\u01b8\7\"\2\2\u01b8\u01bc\3\2\2\2\u01b9\u01bc\5T+\2\u01ba\u01bc\5Z.\2"+
		"\u01bb\u019b\3\2\2\2\u01bb\u019d\3\2\2\2\u01bb\u019e\3\2\2\2\u01bb\u019f"+
		"\3\2\2\2\u01bb\u01a0\3\2\2\2\u01bb\u01a3\3\2\2\2\u01bb\u01b1\3\2\2\2\u01bb"+
		"\u01b4\3\2\2\2\u01bb\u01b9\3\2\2\2\u01bb\u01ba\3\2\2\2\u01bc\u01c2\3\2"+
		"\2\2\u01bd\u01be\f\t\2\2\u01be\u01bf\7\5\2\2\u01bf\u01c1\5@!\n\u01c0\u01bd"+
		"\3\2\2\2\u01c1\u01c4\3\2\2\2\u01c2\u01c0\3\2\2\2\u01c2\u01c3\3\2\2\2\u01c3"+
		"A\3\2\2\2\u01c4\u01c2\3\2\2\2\u01c5\u01c9\5D#\2\u01c6\u01c9\5J&\2\u01c7"+
		"\u01c9\5P)\2\u01c8\u01c5\3\2\2\2\u01c8\u01c6\3\2\2\2\u01c8\u01c7\3\2\2"+
		"\2\u01c9C\3\2\2\2\u01ca\u01cb\5F$\2\u01cb\u01cd\7\37\2\2\u01cc\u01ce\5"+
		"H%\2\u01cd\u01cc\3\2\2\2\u01cd\u01ce\3\2\2\2\u01ce\u01d8\3\2\2\2\u01cf"+
		"\u01d4\5\62\32\2\u01d0\u01d1\7%\2\2\u01d1\u01d3\5\62\32\2\u01d2\u01d0"+
		"\3\2\2\2\u01d3\u01d6\3\2\2\2\u01d4\u01d2\3\2\2\2\u01d4\u01d5\3\2\2\2\u01d5"+
		"\u01d9\3\2\2\2\u01d6\u01d4\3\2\2\2\u01d7\u01d9\7\21\2\2\u01d8\u01cf\3"+
		"\2\2\2\u01d8\u01d7\3\2\2\2\u01d8\u01d9\3\2\2\2\u01d9\u01da\3\2\2\2\u01da"+
		"\u01db\7 \2\2\u01dbE\3\2\2\2\u01dc\u01dd\t\f\2\2\u01ddG\3\2\2\2\u01de"+
		"\u01df\7L\2\2\u01dfI\3\2\2\2\u01e0\u01e3\5L\'\2\u01e1\u01e3\5N(\2\u01e2"+
		"\u01e0\3\2\2\2\u01e2\u01e1\3\2\2\2\u01e3K\3\2\2\2\u01e4\u01e5\7O\2\2\u01e5"+
		"\u01e6\7\37\2\2\u01e6\u01e7\5\62\32\2\u01e7\u01e8\7]\2\2\u01e8\u01e9\5"+
		"b\62\2\u01e9\u01ea\7 \2\2\u01eaM\3\2\2\2\u01eb\u01ec\7\177\2\2\u01ec\u01ed"+
		"\7\37\2\2\u01ed\u01f2\5\62\32\2\u01ee\u01ef\7%\2\2\u01ef\u01f1\5\62\32"+
		"\2\u01f0\u01ee\3\2\2\2\u01f1\u01f4\3\2\2\2\u01f2\u01f0\3\2\2\2\u01f2\u01f3"+
		"\3\2\2\2\u01f3\u01f7\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f5\u01f6\7[\2\2\u01f6"+
		"\u01f8\5\u0098M\2\u01f7\u01f5\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8\u01f9"+
		"\3\2\2\2\u01f9\u01fa\7 \2\2\u01faO\3\2\2\2\u01fb\u01fc\5R*\2\u01fc\u0206"+
		"\7\37\2\2\u01fd\u0202\5\62\32\2\u01fe\u01ff\7%\2\2\u01ff\u0201\5\62\32"+
		"\2\u0200\u01fe\3\2\2\2\u0201\u0204\3\2\2\2\u0202\u0200\3\2\2\2\u0202\u0203"+
		"\3\2\2\2\u0203\u0207\3\2\2\2\u0204\u0202\3\2\2\2\u0205\u0207\7\21\2\2"+
		"\u0206\u01fd\3\2\2\2\u0206\u0205\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u0208"+
		"\3\2\2\2\u0208\u0209\7 \2\2\u0209Q\3\2\2\2\u020a\u0210\5\24\13\2\u020b"+
		"\u0210\7_\2\2\u020c\u0210\7\u0086\2\2\u020d\u0210\7\u0087\2\2\u020e\u0210"+
		"\7\u0082\2\2\u020f\u020a\3\2\2\2\u020f\u020b\3\2\2\2\u020f\u020c\3\2\2"+
		"\2\u020f\u020d\3\2\2\2\u020f\u020e\3\2\2\2\u0210S\3\2\2\2\u0211\u0213"+
		"\7M\2\2\u0212\u0214\5@!\2\u0213\u0212\3\2\2\2\u0213\u0214\3\2\2\2\u0214"+
		"\u0216\3\2\2\2\u0215\u0217\5V,\2\u0216\u0215\3\2\2\2\u0217\u0218\3\2\2"+
		"\2\u0218\u0216\3\2\2\2\u0218\u0219\3\2\2\2\u0219\u021b\3\2\2\2\u021a\u021c"+
		"\5X-\2\u021b\u021a\3\2\2\2\u021b\u021c\3\2\2\2\u021cU\3\2\2\2\u021d\u021e"+
		"\7N\2\2\u021e\u021f\5\62\32\2\u021f\u0220\7a\2\2\u0220\u0221\5\62\32\2"+
		"\u0221W\3\2\2\2\u0222\u0223\7`\2\2\u0223\u0224\5\62\32\2\u0224Y\3\2\2"+
		"\2\u0225\u022a\5l\67\2\u0226\u022a\5f\64\2\u0227\u022a\5h\65\2\u0228\u022a"+
		"\5j\66\2\u0229\u0225\3\2\2\2\u0229\u0226\3\2\2\2\u0229\u0227\3\2\2\2\u0229"+
		"\u0228\3\2\2\2\u022a[\3\2\2\2\u022b\u022c\5\u009cO\2\u022c]\3\2\2\2\u022d"+
		"\u022e\7q\2\2\u022e\u022f\7s\2\2\u022f\u0234\5`\61\2\u0230\u0231\7%\2"+
		"\2\u0231\u0233\5`\61\2\u0232\u0230\3\2\2\2\u0233\u0236\3\2\2\2\u0234\u0232"+
		"\3\2\2\2\u0234\u0235\3\2\2\2\u0235\u0242\3\2\2\2\u0236\u0234\3\2\2\2\u0237"+
		"\u0238\7x\2\2\u0238\u0239\5\62\32\2\u0239\u0240\t\r\2\2\u023a\u023b\7"+
		"\u0196\2\2\u023b\u023c\t\16\2\2\u023c\u023d\5\62\32\2\u023d\u023e\t\r"+
		"\2\2\u023e\u023f\7\u0198\2\2\u023f\u0241\3\2\2\2\u0240\u023a\3\2\2\2\u0240"+
		"\u0241\3\2\2\2\u0241\u0243\3\2\2\2\u0242\u0237\3\2\2\2\u0242\u0243\3\2"+
		"\2\2\u0243_\3\2\2\2\u0244\u0248\5\34\17\2\u0245\u0248\5\b\5\2\u0246\u0248"+
		"\5\62\32\2\u0247\u0244\3\2\2\2\u0247\u0245\3\2\2\2\u0247\u0246\3\2\2\2"+
		"\u0248\u024b\3\2\2\2\u0249\u024a\7\u00e5\2\2\u024a\u024c\5\24\13\2\u024b"+
		"\u0249\3\2\2\2\u024b\u024c\3\2\2\2\u024c\u024e\3\2\2\2\u024d\u024f\t\17"+
		"\2\2\u024e\u024d\3\2\2\2\u024e\u024f\3\2\2\2\u024fa\3\2\2\2\u0250\u0251"+
		"\5\u0098M\2\u0251\u0252\7\24\2\2\u0252\u0254\3\2\2\2\u0253\u0250\3\2\2"+
		"\2\u0253\u0254\3\2\2\2\u0254\u0255\3\2\2\2\u0255\u0261\5d\63\2\u0256\u0262"+
		"\5.\30\2\u0257\u0258\7\37\2\2\u0258\u0259\7\u0091\2\2\u0259\u0262\7 \2"+
		"\2\u025a\u025c\7\37\2\2\u025b\u025d\t\20\2\2\u025c\u025b\3\2\2\2\u025c"+
		"\u025d\3\2\2\2\u025d\u025e\3\2\2\2\u025e\u025f\5\u0098M\2\u025f\u0260"+
		"\7 \2\2\u0260\u0262\3\2\2\2\u0261\u0256\3\2\2\2\u0261\u0257\3\2\2\2\u0261"+
		"\u025a\3\2\2\2\u0261\u0262\3\2\2\2\u0262c\3\2\2\2\u0263\u0264\t\21\2\2"+
		"\u0264e\3\2\2\2\u0265\u0269\7\u01ce\2\2\u0266\u0267\7J\2\2\u0267\u0268"+
		"\7\u0084\2\2\u0268\u026a\7\u011d\2\2\u0269\u0266\3\2\2\2\u0269\u026a\3"+
		"\2\2\2\u026a\u026b\3\2\2\2\u026b\u026c\7\u01cf\2\2\u026cg\3\2\2\2\u026d"+
		"\u026e\7O\2\2\u026e\u026f\7\37\2\2\u026f\u0270\5\62\32\2\u0270\u0271\7"+
		"]\2\2\u0271\u0275\5b\62\2\u0272\u0273\7\37\2\2\u0273\u0274\7\u01d0\2\2"+
		"\u0274\u0276\7 \2\2\u0275\u0272\3\2\2\2\u0275\u0276\3\2\2\2\u0276\u0277"+
		"\3\2\2\2\u0277\u0278\7 \2\2\u0278i\3\2\2\2\u0279\u027a\7\u00e8\2\2\u027a"+
		"\u027e\5b\62\2\u027b\u027c\7\37\2\2\u027c\u027d\7\u01d0\2\2\u027d\u027f"+
		"\7 \2\2\u027e\u027b\3\2\2\2\u027e\u027f\3\2\2\2\u027f\u0280\3\2\2\2\u0280"+
		"\u0281\7%\2\2\u0281\u0284\5\62\32\2\u0282\u0283\7%\2\2\u0283\u0285\7\u01d0"+
		"\2\2\u0284\u0282\3\2\2\2\u0284\u0285\3\2\2\2\u0285k\3\2\2\2\u0286\u0287"+
		"\5B\"\2\u0287\u0288\5n8\2\u0288m\3\2\2\2\u0289\u028a\7\u010a\2\2\u028a"+
		"\u028c\7\37\2\2\u028b\u028d\5p9\2\u028c\u028b\3\2\2\2\u028c\u028d\3\2"+
		"\2\2\u028d\u028f\3\2\2\2\u028e\u0290\5^\60\2\u028f\u028e\3\2\2\2\u028f"+
		"\u0290\3\2\2\2\u0290\u0292\3\2\2\2\u0291\u0293\5r:\2\u0292\u0291\3\2\2"+
		"\2\u0292\u0293\3\2\2\2\u0293\u0294\3\2\2\2\u0294\u0295\7 \2\2\u0295o\3"+
		"\2\2\2\u0296\u0297\7\u00cc\2\2\u0297\u0298\7s\2\2\u0298\u029d\5\62\32"+
		"\2\u0299\u029a\7%\2\2\u029a\u029c\5\62\32\2\u029b\u0299\3\2\2\2\u029c"+
		"\u029f\3\2\2\2\u029d\u029b\3\2\2\2\u029d\u029e\3\2\2\2\u029eq\3\2\2\2"+
		"\u029f\u029d\3\2\2\2\u02a0\u02a1\t\22\2\2\u02a1\u02a2\5t;\2\u02a2s\3\2"+
		"\2\2\u02a3\u02a6\5z>\2\u02a4\u02a6\5v<\2\u02a5\u02a3\3\2\2\2\u02a5\u02a4"+
		"\3\2\2\2\u02a6u\3\2\2\2\u02a7\u02a8\7l\2\2\u02a8\u02a9\5x=\2\u02a9\u02aa"+
		"\7d\2\2\u02aa\u02ab\5x=\2\u02abw\3\2\2\2\u02ac\u02af\5z>\2\u02ad\u02af"+
		"\5|?\2\u02ae\u02ac\3\2\2\2\u02ae\u02ad\3\2\2\2\u02afy\3\2\2\2\u02b0\u02b1"+
		"\7\u00ff\2\2\u02b1\u02b7\7\u010f\2\2\u02b2\u02b3\7\u01d0\2\2\u02b3\u02b7"+
		"\7\u010f\2\2\u02b4\u02b5\7\u0097\2\2\u02b5\u02b7\7\u00cf\2\2\u02b6\u02b0"+
		"\3\2\2\2\u02b6\u02b2\3\2\2\2\u02b6\u02b4\3\2\2\2\u02b7{\3\2\2\2\u02b8"+
		"\u02b9\7\u00ff\2\2\u02b9\u02bf\7\u00f9\2\2\u02ba\u02bb\7\u01d0\2\2\u02bb"+
		"\u02bf\7\u00f9\2\2\u02bc\u02bd\7\u0097\2\2\u02bd\u02bf\7\u00cf\2\2\u02be"+
		"\u02b8\3\2\2\2\u02be\u02ba\3\2\2\2\u02be\u02bc\3\2\2\2\u02bf}\3\2\2\2"+
		"\u02c0\u02c2\5\34\17\2\u02c1\u02c3\t\17\2\2\u02c2\u02c1\3\2\2\2\u02c2"+
		"\u02c3\3\2\2\2\u02c3\177\3\2\2\2\u02c4\u02c5\7\u00f8\2\2\u02c5\u02c6\7"+
		"\30\2\2\u02c6\u02d2\7\u01d0\2\2\u02c7\u02d2\5\u0086D\2\u02c8\u02c9\t\23"+
		"\2\2\u02c9\u02d2\5\u0084C\2\u02ca\u02cb\7\u0103\2\2\u02cb\u02cc\7\30\2"+
		"\2\u02cc\u02d2\7\u01d0\2\2\u02cd\u02cf\5\u0082B\2\u02ce\u02d0\5\u008c"+
		"G\2\u02cf\u02ce\3\2\2\2\u02cf\u02d0\3\2\2\2\u02d0\u02d2\3\2\2\2\u02d1"+
		"\u02c4\3\2\2\2\u02d1\u02c7\3\2\2\2\u02d1\u02c8\3\2\2\2\u02d1\u02ca\3\2"+
		"\2\2\u02d1\u02cd\3\2\2\2\u02d2\u0081\3\2\2\2\u02d3\u02d4\7\u0176\2\2\u02d4"+
		"\u02d5\7\30\2\2\u02d5\u02d6\t\24\2\2\u02d6\u0083\3\2\2\2\u02d7\u02d8\7"+
		"\30\2\2\u02d8\u02da\7\u01d0\2\2\u02d9\u02db\7\u00ee\2\2\u02da\u02d9\3"+
		"\2\2\2\u02da\u02db\3\2\2\2\u02db\u0085\3\2\2\2\u02dc\u02dd\5\u0088E\2"+
		"\u02dd\u02de\5\u008aF\2\u02de\u0087\3\2\2\2\u02df\u02e0\t\25\2\2\u02e0"+
		"\u0089\3\2\2\2\u02e1\u02e2\7\30\2\2\u02e2\u02e3\t\26\2\2\u02e3\u008b\3"+
		"\2\2\2\u02e4\u02e5\7^\2\2\u02e5\u02e6\7\u00cd\2\2\u02e6\u02e7\7\37\2\2"+
		"\u02e7\u02e8\5\u008eH\2\u02e8\u02e9\7 \2\2\u02e9\u008d\3\2\2\2\u02ea\u02ef"+
		"\5\u0090I\2\u02eb\u02ec\7%\2\2\u02ec\u02ee\5\u0090I\2\u02ed\u02eb\3\2"+
		"\2\2\u02ee\u02f1\3\2\2\2\u02ef\u02ed\3\2\2\2\u02ef\u02f0\3\2\2\2\u02f0"+
		"\u008f\3\2\2\2\u02f1\u02ef\3\2\2\2\u02f2\u02f5\7\u01d0\2\2\u02f3\u02f5"+
		"\5\u0092J\2\u02f4\u02f2\3\2\2\2\u02f4\u02f3\3\2\2\2\u02f5\u0091\3\2\2"+
		"\2\u02f6\u02f7\7\u01d0\2\2\u02f7\u02f8\7c\2\2\u02f8\u02f9\7\u01d0\2\2"+
		"\u02f9\u0093\3\2\2\2\u02fa\u02fb\7\u017c\2\2\u02fb\u02fc\7\37\2\2\u02fc"+
		"\u02fd\7\u0185\2\2\u02fd\u02fe\7\30\2\2\u02fe\u0300\7\u01d0\2\2\u02ff"+
		"\u0301\7\u00ee\2\2\u0300\u02ff\3\2\2\2\u0300\u0301\3\2\2\2\u0301\u0302"+
		"\3\2\2\2\u0302\u0303\7%\2\2\u0303\u0304\7\u016c\2\2\u0304\u0305\7\30\2"+
		"\2\u0305\u0306\t\27\2\2\u0306\u0307\7 \2\2\u0307\u0095\3\2\2\2\u0308\u030d"+
		"\7^\2\2\u0309\u030a\7\37\2\2\u030a\u030b\5\u0094K\2\u030b\u030c\7 \2\2"+
		"\u030c\u030e\3\2\2\2\u030d\u0309\3\2\2\2\u030d\u030e\3\2\2\2\u030e\u0097"+
		"\3\2\2\2\u030f\u0310\7\u01ce\2\2\u0310\u0099\3\2\2\2\u0311\u0316\5\u0098"+
		"M\2\u0312\u0313\7%\2\2\u0313\u0315\5\u0098M\2\u0314\u0312\3\2\2\2\u0315"+
		"\u0318\3\2\2\2\u0316\u0314\3\2\2\2\u0316\u0317\3\2\2\2\u0317\u009b\3\2"+
		"\2\2\u0318\u0316\3\2\2\2\u0319\u031a\7\3\2\2\u031a\u009d\3\2\2\2O\u00a7"+
		"\u00ac\u00b7\u00c3\u00cc\u00d3\u00e1\u00ec\u00f2\u00f9\u00fd\u0109\u010b"+
		"\u0110\u011d\u0125\u0132\u0141\u0143\u014a\u0151\u015a\u0161\u016a\u0170"+
		"\u0173\u0196\u0198\u01a3\u01ab\u01b1\u01bb\u01c2\u01c8\u01cd\u01d4\u01d8"+
		"\u01e2\u01f2\u01f7\u0202\u0206\u020f\u0213\u0218\u021b\u0229\u0234\u0240"+
		"\u0242\u0247\u024b\u024e\u0253\u025c\u0261\u0269\u0275\u027e\u0284\u028c"+
		"\u028f\u0292\u029d\u02a5\u02ae\u02b6\u02be\u02c2\u02cf\u02d1\u02da\u02ef"+
		"\u02f4\u0300\u030d\u0316";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}