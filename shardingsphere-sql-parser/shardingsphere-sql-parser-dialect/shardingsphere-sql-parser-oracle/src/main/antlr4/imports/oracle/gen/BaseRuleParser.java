// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-oracle/src/main/antlr4/imports/oracle/BaseRule.g4 by ANTLR 4.9.1
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
		DOLLAR_=42, WS=43, SELECT=44, INSERT=45, UPDATE=46, DELETE=47, CREATE=48, 
		ALTER=49, DROP=50, TRUNCATE=51, SCHEMA=52, GRANT=53, REVOKE=54, ADD=55, 
		SET=56, TABLE=57, COLUMN=58, INDEX=59, CONSTRAINT=60, PRIMARY=61, UNIQUE=62, 
		FOREIGN=63, KEY=64, POSITION=65, PRECISION=66, FUNCTION=67, TRIGGER=68, 
		PROCEDURE=69, VIEW=70, INTO=71, VALUES=72, WITH=73, UNION=74, DISTINCT=75, 
		CASE=76, WHEN=77, CAST=78, TRIM=79, SUBSTRING=80, FROM=81, NATURAL=82, 
		JOIN=83, FULL=84, INNER=85, OUTER=86, LEFT=87, RIGHT=88, CROSS=89, USING=90, 
		WHERE=91, AS=92, ON=93, IF=94, ELSE=95, THEN=96, FOR=97, TO=98, AND=99, 
		OR=100, IS=101, NOT=102, NULL=103, TRUE=104, FALSE=105, EXISTS=106, BETWEEN=107, 
		IN=108, ALL=109, ANY=110, LIKE=111, ORDER=112, GROUP=113, BY=114, ASC=115, 
		DESC=116, HAVING=117, LIMIT=118, OFFSET=119, BEGIN=120, COMMIT=121, ROLLBACK=122, 
		SAVEPOINT=123, BOOLEAN=124, DOUBLE=125, CHAR=126, CHARACTER=127, ARRAY=128, 
		INTERVAL=129, DATE=130, TIME=131, TIMEOUT=132, TIMESTAMP=133, LOCALTIME=134, 
		LOCALTIMESTAMP=135, YEAR=136, QUARTER=137, MONTH=138, WEEK=139, DAY=140, 
		HOUR=141, MINUTE=142, SECOND=143, MICROSECOND=144, MAX=145, MIN=146, SUM=147, 
		COUNT=148, AVG=149, DEFAULT=150, CURRENT=151, ENABLE=152, DISABLE=153, 
		CALL=154, INSTANCE=155, PRESERVE=156, DO=157, DEFINER=158, CURRENT_USER=159, 
		SQL=160, CASCADED=161, LOCAL=162, CLOSE=163, OPEN=164, NEXT=165, NAME=166, 
		COLLATION=167, NAMES=168, INTEGER=169, REAL=170, DECIMAL=171, TYPE=172, 
		INT=173, SMALLINT=174, NUMERIC=175, FLOAT=176, TRIGGERS=177, GLOBAL_NAME=178, 
		FOR_GENERATOR=179, BINARY=180, ESCAPE=181, MOD=182, XOR=183, ROW=184, 
		ROWS=185, UNKNOWN=186, ALWAYS=187, CASCADE=188, CHECK=189, GENERATED=190, 
		PRIVILEGES=191, READ=192, WRITE=193, REFERENCES=194, START=195, TRANSACTION=196, 
		USER=197, ROLE=198, VISIBLE=199, INVISIBLE=200, EXECUTE=201, USE=202, 
		DEBUG=203, UNDER=204, FLASHBACK=205, ARCHIVE=206, REFRESH=207, QUERY=208, 
		REWRITE=209, KEEP=210, SEQUENCE=211, INHERIT=212, TRANSLATE=213, MERGE=214, 
		AT=215, BITMAP=216, CACHE=217, NOCACHE=218, CHECKPOINT=219, CONNECT=220, 
		CONSTRAINTS=221, CYCLE=222, NOCYCLE=223, DBTIMEZONE=224, ENCRYPT=225, 
		DECRYPT=226, DEFERRABLE=227, DEFERRED=228, DIRECTORY=229, EDITION=230, 
		ELEMENT=231, END=232, EXCEPT=233, EXCEPTIONS=234, FORCE=235, GLOBAL=236, 
		IDENTIFIED=237, IDENTITY=238, IMMEDIATE=239, INCREMENT=240, INITIALLY=241, 
		INVALIDATE=242, JAVA=243, LEVELS=244, MAXVALUE=245, MINVALUE=246, NOMAXVALUE=247, 
		NOMINVALUE=248, NOSORT=249, MINING=250, MODEL=251, MODIFY=252, NATIONAL=253, 
		NEW=254, NOORDER=255, NORELY=256, OF=257, ONLY=258, PRIOR=259, PROFILE=260, 
		REF=261, REKEY=262, RELY=263, RENAME=264, REPLACE=265, RESOURCE=266, REVERSE=267, 
		ROWID=268, SALT=269, SCOPE=270, SORT=271, SOURCE=272, SUBSTITUTABLE=273, 
		TABLESPACE=274, TEMPORARY=275, TRANSLATION=276, TREAT=277, NO=278, UNUSED=279, 
		VALIDATE=280, NOVALIDATE=281, VALUE=282, VARYING=283, VIRTUAL=284, ZONE=285, 
		PUBLIC=286, SESSION=287, COMMENT=288, LOCK=289, ADVISOR=290, ADMINISTER=291, 
		TUNING=292, MANAGE=293, MANAGEMENT=294, OBJECT=295, CLUSTER=296, CONTEXT=297, 
		EXEMPT=298, REDACTION=299, POLICY=300, DATABASE=301, SYSTEM=302, AUDIT=303, 
		LINK=304, ANALYZE=305, DICTIONARY=306, DIMENSION=307, INDEXTYPE=308, EXTERNAL=309, 
		JOB=310, CLASS=311, PROGRAM=312, SCHEDULER=313, LIBRARY=314, LOGMINING=315, 
		MATERIALIZED=316, CUBE=317, MEASURE=318, FOLDER=319, BUILD=320, PROCESS=321, 
		OPERATOR=322, OUTLINE=323, PLUGGABLE=324, CONTAINER=325, SEGMENT=326, 
		RESTRICTED=327, COST=328, SYNONYM=329, BACKUP=330, UNLIMITED=331, BECOME=332, 
		CHANGE=333, NOTIFICATION=334, ACCESS=335, PRIVILEGE=336, PURGE=337, RESUMABLE=338, 
		SYSGUID=339, SYSBACKUP=340, SYSDBA=341, SYSDG=342, SYSKM=343, SYSOPER=344, 
		DBA_RECYCLEBIN=345, FIRST=346, NCHAR=347, RAW=348, VARCHAR=349, VARCHAR2=350, 
		NVARCHAR2=351, LONG=352, BLOB=353, CLOB=354, NCLOB=355, BINARY_FLOAT=356, 
		BINARY_DOUBLE=357, PLS_INTEGER=358, BINARY_INTEGER=359, NUMBER=360, NATURALN=361, 
		POSITIVE=362, POSITIVEN=363, SIGNTYPE=364, SIMPLE_INTEGER=365, BFILE=366, 
		MLSLABEL=367, UROWID=368, JSON=369, DEC=370, SHARING=371, PRIVATE=372, 
		SHARDED=373, SHARD=374, DUPLICATED=375, METADATA=376, DATA=377, EXTENDED=378, 
		NONE=379, MEMOPTIMIZE=380, PARENT=381, IDENTIFIER=382, WORK=383, CONTAINER_MAP=384, 
		CONTAINERS_DEFAULT=385, WAIT=386, NOWAIT=387, BATCH=388, BLOCK=389, REBUILD=390, 
		INVALIDATION=391, COMPILE=392, USABLE=393, UNUSABLE=394, ONLINE=395, MONITORING=396, 
		NOMONITORING=397, USAGE=398, COALESCE=399, CLEANUP=400, PARALLEL=401, 
		NOPARALLEL=402, LOG=403, REUSE=404, STORAGE=405, MATCHED=406, ERRORS=407, 
		REJECT=408, RETENTION=409, CHUNK=410, PCTVERSION=411, FREEPOOLS=412, AUTO=413, 
		DEDUPLICATE=414, KEEP_DUPLICATES=415, COMPRESS=416, HIGH=417, MEDIUM=418, 
		LOW=419, NOCOMPRESS=420, READS=421, CREATION=422, PCTFREE=423, PCTUSED=424, 
		INITRANS=425, LOGGING=426, NOLOGGING=427, FILESYSTEM_LIKE_LOGGING=428, 
		INITIAL=429, MINEXTENTS=430, MAXEXTENTS=431, BASIC=432, ADVANCED=433, 
		PCTINCREASE=434, FREELISTS=435, DML=436, DDL=437, CAPACITY=438, FREELIST=439, 
		GROUPS=440, OPTIMAL=441, BUFFER_POOL=442, RECYCLE=443, FLASH_CACHE=444, 
		CELL_FLASH_CACHE=445, MAXSIZE=446, STORE=447, LEVEL=448, LOCKING=449, 
		INMEMORY=450, MEMCOMPRESS=451, PRIORITY=452, CRITICAL=453, DISTRIBUTE=454, 
		RANGE=455, PARTITION=456, SUBPARTITION=457, SERVICE=458, DUPLICATE=459, 
		ILM=460, DELETE_ALL=461, ENABLE_ALL=462, DISABLE_ALL=463, AFTER=464, MODIFICATION=465, 
		DAYS=466, MONTHS=467, YEARS=468, TIER=469, ORGANIZATION=470, HEAP=471, 
		PCTTHRESHOLD=472, PARAMETERS=473, LOCATION=474, MAPPING=475, NOMAPPING=476, 
		INCLUDING=477, OVERFLOW=478, ATTRIBUTES=479, RESULT_CACHE=480, ROWDEPENDENCIES=481, 
		NOROWDEPENDENCIES=482, ARCHIVAL=483, EXCHANGE=484, INDEXING=485, OFF=486, 
		LESS=487, INTERNAL=488, VARRAY=489, NESTED=490, COLUMN_VALUE=491, RETURN=492, 
		LOCATOR=493, MODE=494, LOB=495, SECUREFILE=496, BASICFILE=497, THAN=498, 
		LIST=499, AUTOMATIC=500, HASH=501, PARTITIONS=502, SUBPARTITIONS=503, 
		TEMPLATE=504, PARTITIONSET=505, REFERENCE=506, CONSISTENT=507, CLUSTERING=508, 
		LINEAR=509, INTERLEAVED=510, YES=511, LOAD=512, MOVEMENT=513, ZONEMAP=514, 
		WITHOUT=515, XMLTYPE=516, RELATIONAL=517, XML=518, VARRAYS=519, LOBS=520, 
		TABLES=521, ALLOW=522, DISALLOW=523, NONSCHEMA=524, ANYSCHEMA=525, XMLSCHEMA=526, 
		COLUMNS=527, OIDINDEX=528, EDITIONABLE=529, NONEDITIONABLE=530, DEPENDENT=531, 
		INDEXES=532, SHRINK=533, SPACE=534, COMPACT=535, SUPPLEMENTAL=536, ADVISE=537, 
		NOTHING=538, GUARD=539, SYNC=540, VISIBILITY=541, ACTIVE=542, DEFAULT_COLLATION=543, 
		MOUNT=544, STANDBY=545, CLONE=546, RESETLOGS=547, NORESETLOGS=548, UPGRADE=549, 
		DOWNGRADE=550, RECOVER=551, LOGFILE=552, TEST=553, CORRUPTION=554, CONTINUE=555, 
		CANCEL=556, UNTIL=557, CONTROLFILE=558, SNAPSHOT=559, DATAFILE=560, MANAGED=561, 
		ARCHIVED=562, DISCONNECT=563, NODELAY=564, INSTANCES=565, FINISH=566, 
		LOGICAL=567, FILE=568, SIZE=569, AUTOEXTEND=570, BLOCKSIZE=571, OFFLINE=572, 
		RESIZE=573, TEMPFILE=574, DATAFILES=575, ARCHIVELOG=576, MANUAL=577, NOARCHIVELOG=578, 
		AVAILABILITY=579, PERFORMANCE=580, CLEAR=581, UNARCHIVED=582, UNRECOVERABLE=583, 
		THREAD=584, MEMBER=585, PHYSICAL=586, FAR=587, TRACE=588, DISTRIBUTED=589, 
		RECOVERY=590, FLUSH=591, NOREPLY=592, SWITCH=593, LOGFILES=594, PROCEDURAL=595, 
		REPLICATION=596, SUBSET=597, ACTIVATE=598, APPLY=599, MAXIMIZE=600, PROTECTION=601, 
		SUSPEND=602, RESUME=603, QUIESCE=604, UNQUIESCE=605, SHUTDOWN=606, REGISTER=607, 
		PREPARE=608, SWITCHOVER=609, FAILED=610, SKIP_SYMBOL=611, STOP=612, ABORT=613, 
		VERIFY=614, CONVERT=615, FAILOVER=616, BIGFILE=617, SMALLFILE=618, TRACKING=619, 
		CACHING=620, CONTAINERS=621, TARGET=622, UNDO=623, MOVE=624, MIRROR=625, 
		COPY=626, UNPROTECTED=627, REDUNDANCY=628, REMOVE=629, LOST=630, LEAD_CDB=631, 
		LEAD_CDB_URI=632, PROPERTY=633, DEFAULT_CREDENTIAL=634, TIME_ZONE=635, 
		RESET=636, RELOCATE=637, CLIENT=638, PASSWORDFILE_METADATA_CACHE=639, 
		NOSWITCH=640, POST_TRANSACTION=641, KILL=642, ROLLING=643, MIGRATION=644, 
		PATCH=645, ENCRYPTION=646, WALLET=647, AFFINITY=648, MEMORY=649, SPFILE=650, 
		BOTH=651, SID=652, SHARED_POOL=653, BUFFER_CACHE=654, REDO=655, CONFIRM=656, 
		MIGRATE=657, USE_STORED_OUTLINES=658, GLOBAL_TOPIC_ENABLED=659, INTERSECT=660, 
		MINUS=661, LOCKED=662, FETCH=663, PERCENT=664, TIES=665, SIBLINGS=666, 
		NULLS=667, LAST=668, ISOLATION=669, SERIALIZABLE=670, COMMITTED=671, FILTER=672, 
		FACT=673, DETERMINISTIC=674, PIPELINED=675, PARALLEL_ENABLE=676, OUT=677, 
		NOCOPY=678, ACCESSIBLE=679, PACKAGE=680, USING_NLS_COMP=681, AUTHID=682, 
		SEARCH=683, DEPTH=684, BREADTH=685, ANALYTIC=686, HIERARCHIES=687, MEASURES=688, 
		OVER=689, LAG=690, LAG_DIFF=691, LAG_DIF_PERCENT=692, LEAD=693, LEAD_DIFF=694, 
		LEAD_DIFF_PERCENT=695, HIERARCHY=696, WITHIN=697, ACROSS=698, ANCESTOR=699, 
		BEGINNING=700, UNBOUNDED=701, PRECEDING=702, FOLLOWING=703, RANK=704, 
		DENSE_RANK=705, AVERAGE_RANK=706, ROW_NUMBER=707, SHARE_OF=708, HIER_ANCESTOR=709, 
		HIER_PARENT=710, HIER_LEAD=711, HIER_LAG=712, QUALIFY=713, HIER_CAPTION=714, 
		HIER_DEPTH=715, HIER_DESCRIPTION=716, HIER_LEVEL=717, HIER_MEMBER_NAME=718, 
		HIER_MEMBER_UNIQUE_NAME=719, CHAINED=720, STATISTICS=721, DANGLING=722, 
		STRUCTURE=723, FAST=724, COMPLETE=725, IDENTIFIER_=726, STRING_=727, INTEGER_=728, 
		NUMBER_=729, HEX_DIGIT_=730, BIT_NUM_=731;
	public static final int
		RULE_parameterMarker = 0, RULE_literals = 1, RULE_stringLiterals = 2, 
		RULE_numberLiterals = 3, RULE_dateTimeLiterals = 4, RULE_hexadecimalLiterals = 5, 
		RULE_bitValueLiterals = 6, RULE_booleanLiterals = 7, RULE_nullValueLiterals = 8, 
		RULE_identifier = 9, RULE_unreservedWord = 10, RULE_schemaName = 11, RULE_tableName = 12, 
		RULE_viewName = 13, RULE_columnName = 14, RULE_objectName = 15, RULE_clusterName = 16, 
		RULE_indexName = 17, RULE_constraintName = 18, RULE_savepointName = 19, 
		RULE_synonymName = 20, RULE_owner = 21, RULE_name = 22, RULE_tablespaceName = 23, 
		RULE_tablespaceSetName = 24, RULE_serviceName = 25, RULE_ilmPolicyName = 26, 
		RULE_functionName = 27, RULE_dbLink = 28, RULE_parameterValue = 29, RULE_directoryName = 30, 
		RULE_dispatcherName = 31, RULE_clientId = 32, RULE_opaqueFormatSpec = 33, 
		RULE_accessDriverType = 34, RULE_type = 35, RULE_varrayItem = 36, RULE_nestedItem = 37, 
		RULE_storageTable = 38, RULE_lobSegname = 39, RULE_locationSpecifier = 40, 
		RULE_xmlSchemaURLName = 41, RULE_elementName = 42, RULE_subpartitionName = 43, 
		RULE_parameterName = 44, RULE_editionName = 45, RULE_containerName = 46, 
		RULE_partitionName = 47, RULE_partitionSetName = 48, RULE_partitionKeyValue = 49, 
		RULE_subpartitionKeyValue = 50, RULE_zonemapName = 51, RULE_flashbackArchiveName = 52, 
		RULE_roleName = 53, RULE_password = 54, RULE_logGroupName = 55, RULE_columnNames = 56, 
		RULE_tableNames = 57, RULE_oracleId = 58, RULE_collationName = 59, RULE_columnCollationName = 60, 
		RULE_alias = 61, RULE_dataTypeLength = 62, RULE_primaryKey = 63, RULE_exprs = 64, 
		RULE_exprList = 65, RULE_expr = 66, RULE_logicalOperator = 67, RULE_notOperator = 68, 
		RULE_booleanPrimary = 69, RULE_comparisonOperator = 70, RULE_predicate = 71, 
		RULE_bitExpr = 72, RULE_simpleExpr = 73, RULE_functionCall = 74, RULE_aggregationFunction = 75, 
		RULE_aggregationFunctionName = 76, RULE_distinct = 77, RULE_specialFunction = 78, 
		RULE_castFunction = 79, RULE_charFunction = 80, RULE_regularFunction = 81, 
		RULE_regularFunctionName = 82, RULE_caseExpression = 83, RULE_caseWhen = 84, 
		RULE_caseElse = 85, RULE_subquery = 86, RULE_orderByClause = 87, RULE_orderByItem = 88, 
		RULE_attributeName = 89, RULE_indexTypeName = 90, RULE_simpleExprs = 91, 
		RULE_lobItem = 92, RULE_lobItems = 93, RULE_lobItemList = 94, RULE_dataType = 95, 
		RULE_specialDatatype = 96, RULE_dataTypeName = 97, RULE_datetimeTypeSuffix = 98, 
		RULE_treatFunction = 99, RULE_privateExprOfDb = 100, RULE_caseExpr = 101, 
		RULE_simpleCaseExpr = 102, RULE_searchedCaseExpr = 103, RULE_elseClause = 104, 
		RULE_intervalExpression = 105, RULE_objectAccessExpression = 106, RULE_constructorExpr = 107, 
		RULE_ignoredIdentifier = 108, RULE_ignoredIdentifiers = 109, RULE_matchNone = 110, 
		RULE_hashSubpartitionQuantity = 111, RULE_odciParameters = 112, RULE_databaseName = 113, 
		RULE_locationName = 114, RULE_fileName = 115, RULE_asmFileName = 116, 
		RULE_fileNumber = 117, RULE_instanceName = 118, RULE_logminerSessionName = 119, 
		RULE_tablespaceGroupName = 120, RULE_copyName = 121, RULE_mirrorName = 122, 
		RULE_uriString = 123, RULE_qualifiedCredentialName = 124, RULE_pdbName = 125, 
		RULE_diskgroupName = 126, RULE_templateName = 127, RULE_aliasName = 128, 
		RULE_domain = 129, RULE_dateValue = 130, RULE_sessionId = 131, RULE_serialNumber = 132, 
		RULE_instanceId = 133, RULE_sqlId = 134, RULE_logFileName = 135, RULE_logFileGroupsArchivedLocationName = 136, 
		RULE_asmVersion = 137, RULE_walletPassword = 138, RULE_hsmAuthString = 139, 
		RULE_targetDbName = 140, RULE_certificateId = 141, RULE_categoryName = 142, 
		RULE_offset = 143, RULE_rowcount = 144, RULE_percent = 145, RULE_rollbackSegment = 146, 
		RULE_queryName = 147, RULE_cycleValue = 148, RULE_noCycleValue = 149, 
		RULE_orderingColumn = 150, RULE_subavName = 151, RULE_baseAvName = 152, 
		RULE_measName = 153, RULE_levelRef = 154, RULE_offsetExpr = 155, RULE_memberKeyExpr = 156, 
		RULE_depthExpression = 157, RULE_unitName = 158, RULE_procedureName = 159;
	private static String[] makeRuleNames() {
		return new String[] {
			"parameterMarker", "literals", "stringLiterals", "numberLiterals", "dateTimeLiterals", 
			"hexadecimalLiterals", "bitValueLiterals", "booleanLiterals", "nullValueLiterals", 
			"identifier", "unreservedWord", "schemaName", "tableName", "viewName", 
			"columnName", "objectName", "clusterName", "indexName", "constraintName", 
			"savepointName", "synonymName", "owner", "name", "tablespaceName", "tablespaceSetName", 
			"serviceName", "ilmPolicyName", "functionName", "dbLink", "parameterValue", 
			"directoryName", "dispatcherName", "clientId", "opaqueFormatSpec", "accessDriverType", 
			"type", "varrayItem", "nestedItem", "storageTable", "lobSegname", "locationSpecifier", 
			"xmlSchemaURLName", "elementName", "subpartitionName", "parameterName", 
			"editionName", "containerName", "partitionName", "partitionSetName", 
			"partitionKeyValue", "subpartitionKeyValue", "zonemapName", "flashbackArchiveName", 
			"roleName", "password", "logGroupName", "columnNames", "tableNames", 
			"oracleId", "collationName", "columnCollationName", "alias", "dataTypeLength", 
			"primaryKey", "exprs", "exprList", "expr", "logicalOperator", "notOperator", 
			"booleanPrimary", "comparisonOperator", "predicate", "bitExpr", "simpleExpr", 
			"functionCall", "aggregationFunction", "aggregationFunctionName", "distinct", 
			"specialFunction", "castFunction", "charFunction", "regularFunction", 
			"regularFunctionName", "caseExpression", "caseWhen", "caseElse", "subquery", 
			"orderByClause", "orderByItem", "attributeName", "indexTypeName", "simpleExprs", 
			"lobItem", "lobItems", "lobItemList", "dataType", "specialDatatype", 
			"dataTypeName", "datetimeTypeSuffix", "treatFunction", "privateExprOfDb", 
			"caseExpr", "simpleCaseExpr", "searchedCaseExpr", "elseClause", "intervalExpression", 
			"objectAccessExpression", "constructorExpr", "ignoredIdentifier", "ignoredIdentifiers", 
			"matchNone", "hashSubpartitionQuantity", "odciParameters", "databaseName", 
			"locationName", "fileName", "asmFileName", "fileNumber", "instanceName", 
			"logminerSessionName", "tablespaceGroupName", "copyName", "mirrorName", 
			"uriString", "qualifiedCredentialName", "pdbName", "diskgroupName", "templateName", 
			"aliasName", "domain", "dateValue", "sessionId", "serialNumber", "instanceId", 
			"sqlId", "logFileName", "logFileGroupsArchivedLocationName", "asmVersion", 
			"walletPassword", "hsmAuthString", "targetDbName", "certificateId", "categoryName", 
			"offset", "rowcount", "percent", "rollbackSegment", "queryName", "cycleValue", 
			"noCycleValue", "orderingColumn", "subavName", "baseAvName", "measName", 
			"levelRef", "offsetExpr", "memberKeyExpr", "depthExpression", "unitName", 
			"procedureName"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Default does not match anything'", "'&&'", "'||'", "'!'", "'~'", 
			"'|'", "'&'", "'<<'", "'>>'", "'^'", "'%'", "':'", "'+'", "'-'", "'*'", 
			"'/'", "'\\'", "'.'", "'.*'", "'<=>'", "'=='", "'='", null, "'>'", "'>='", 
			"'<'", "'<='", "'#'", "'('", "')'", "'{'", "'}'", "'['", "']'", "','", 
			"'\"'", "'''", "'`'", "'?'", "'@'", "';'", "'$'", null, null, null, null, 
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
			"'DO NOT MATCH ANY THING, JUST FOR GENERATOR'"
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
			"BQ_", "QUESTION_", "AT_", "SEMI_", "DOLLAR_", "WS", "SELECT", "INSERT", 
			"UPDATE", "DELETE", "CREATE", "ALTER", "DROP", "TRUNCATE", "SCHEMA", 
			"GRANT", "REVOKE", "ADD", "SET", "TABLE", "COLUMN", "INDEX", "CONSTRAINT", 
			"PRIMARY", "UNIQUE", "FOREIGN", "KEY", "POSITION", "PRECISION", "FUNCTION", 
			"TRIGGER", "PROCEDURE", "VIEW", "INTO", "VALUES", "WITH", "UNION", "DISTINCT", 
			"CASE", "WHEN", "CAST", "TRIM", "SUBSTRING", "FROM", "NATURAL", "JOIN", 
			"FULL", "INNER", "OUTER", "LEFT", "RIGHT", "CROSS", "USING", "WHERE", 
			"AS", "ON", "IF", "ELSE", "THEN", "FOR", "TO", "AND", "OR", "IS", "NOT", 
			"NULL", "TRUE", "FALSE", "EXISTS", "BETWEEN", "IN", "ALL", "ANY", "LIKE", 
			"ORDER", "GROUP", "BY", "ASC", "DESC", "HAVING", "LIMIT", "OFFSET", "BEGIN", 
			"COMMIT", "ROLLBACK", "SAVEPOINT", "BOOLEAN", "DOUBLE", "CHAR", "CHARACTER", 
			"ARRAY", "INTERVAL", "DATE", "TIME", "TIMEOUT", "TIMESTAMP", "LOCALTIME", 
			"LOCALTIMESTAMP", "YEAR", "QUARTER", "MONTH", "WEEK", "DAY", "HOUR", 
			"MINUTE", "SECOND", "MICROSECOND", "MAX", "MIN", "SUM", "COUNT", "AVG", 
			"DEFAULT", "CURRENT", "ENABLE", "DISABLE", "CALL", "INSTANCE", "PRESERVE", 
			"DO", "DEFINER", "CURRENT_USER", "SQL", "CASCADED", "LOCAL", "CLOSE", 
			"OPEN", "NEXT", "NAME", "COLLATION", "NAMES", "INTEGER", "REAL", "DECIMAL", 
			"TYPE", "INT", "SMALLINT", "NUMERIC", "FLOAT", "TRIGGERS", "GLOBAL_NAME", 
			"FOR_GENERATOR", "BINARY", "ESCAPE", "MOD", "XOR", "ROW", "ROWS", "UNKNOWN", 
			"ALWAYS", "CASCADE", "CHECK", "GENERATED", "PRIVILEGES", "READ", "WRITE", 
			"REFERENCES", "START", "TRANSACTION", "USER", "ROLE", "VISIBLE", "INVISIBLE", 
			"EXECUTE", "USE", "DEBUG", "UNDER", "FLASHBACK", "ARCHIVE", "REFRESH", 
			"QUERY", "REWRITE", "KEEP", "SEQUENCE", "INHERIT", "TRANSLATE", "MERGE", 
			"AT", "BITMAP", "CACHE", "NOCACHE", "CHECKPOINT", "CONNECT", "CONSTRAINTS", 
			"CYCLE", "NOCYCLE", "DBTIMEZONE", "ENCRYPT", "DECRYPT", "DEFERRABLE", 
			"DEFERRED", "DIRECTORY", "EDITION", "ELEMENT", "END", "EXCEPT", "EXCEPTIONS", 
			"FORCE", "GLOBAL", "IDENTIFIED", "IDENTITY", "IMMEDIATE", "INCREMENT", 
			"INITIALLY", "INVALIDATE", "JAVA", "LEVELS", "MAXVALUE", "MINVALUE", 
			"NOMAXVALUE", "NOMINVALUE", "NOSORT", "MINING", "MODEL", "MODIFY", "NATIONAL", 
			"NEW", "NOORDER", "NORELY", "OF", "ONLY", "PRIOR", "PROFILE", "REF", 
			"REKEY", "RELY", "RENAME", "REPLACE", "RESOURCE", "REVERSE", "ROWID", 
			"SALT", "SCOPE", "SORT", "SOURCE", "SUBSTITUTABLE", "TABLESPACE", "TEMPORARY", 
			"TRANSLATION", "TREAT", "NO", "UNUSED", "VALIDATE", "NOVALIDATE", "VALUE", 
			"VARYING", "VIRTUAL", "ZONE", "PUBLIC", "SESSION", "COMMENT", "LOCK", 
			"ADVISOR", "ADMINISTER", "TUNING", "MANAGE", "MANAGEMENT", "OBJECT", 
			"CLUSTER", "CONTEXT", "EXEMPT", "REDACTION", "POLICY", "DATABASE", "SYSTEM", 
			"AUDIT", "LINK", "ANALYZE", "DICTIONARY", "DIMENSION", "INDEXTYPE", "EXTERNAL", 
			"JOB", "CLASS", "PROGRAM", "SCHEDULER", "LIBRARY", "LOGMINING", "MATERIALIZED", 
			"CUBE", "MEASURE", "FOLDER", "BUILD", "PROCESS", "OPERATOR", "OUTLINE", 
			"PLUGGABLE", "CONTAINER", "SEGMENT", "RESTRICTED", "COST", "SYNONYM", 
			"BACKUP", "UNLIMITED", "BECOME", "CHANGE", "NOTIFICATION", "ACCESS", 
			"PRIVILEGE", "PURGE", "RESUMABLE", "SYSGUID", "SYSBACKUP", "SYSDBA", 
			"SYSDG", "SYSKM", "SYSOPER", "DBA_RECYCLEBIN", "FIRST", "NCHAR", "RAW", 
			"VARCHAR", "VARCHAR2", "NVARCHAR2", "LONG", "BLOB", "CLOB", "NCLOB", 
			"BINARY_FLOAT", "BINARY_DOUBLE", "PLS_INTEGER", "BINARY_INTEGER", "NUMBER", 
			"NATURALN", "POSITIVE", "POSITIVEN", "SIGNTYPE", "SIMPLE_INTEGER", "BFILE", 
			"MLSLABEL", "UROWID", "JSON", "DEC", "SHARING", "PRIVATE", "SHARDED", 
			"SHARD", "DUPLICATED", "METADATA", "DATA", "EXTENDED", "NONE", "MEMOPTIMIZE", 
			"PARENT", "IDENTIFIER", "WORK", "CONTAINER_MAP", "CONTAINERS_DEFAULT", 
			"WAIT", "NOWAIT", "BATCH", "BLOCK", "REBUILD", "INVALIDATION", "COMPILE", 
			"USABLE", "UNUSABLE", "ONLINE", "MONITORING", "NOMONITORING", "USAGE", 
			"COALESCE", "CLEANUP", "PARALLEL", "NOPARALLEL", "LOG", "REUSE", "STORAGE", 
			"MATCHED", "ERRORS", "REJECT", "RETENTION", "CHUNK", "PCTVERSION", "FREEPOOLS", 
			"AUTO", "DEDUPLICATE", "KEEP_DUPLICATES", "COMPRESS", "HIGH", "MEDIUM", 
			"LOW", "NOCOMPRESS", "READS", "CREATION", "PCTFREE", "PCTUSED", "INITRANS", 
			"LOGGING", "NOLOGGING", "FILESYSTEM_LIKE_LOGGING", "INITIAL", "MINEXTENTS", 
			"MAXEXTENTS", "BASIC", "ADVANCED", "PCTINCREASE", "FREELISTS", "DML", 
			"DDL", "CAPACITY", "FREELIST", "GROUPS", "OPTIMAL", "BUFFER_POOL", "RECYCLE", 
			"FLASH_CACHE", "CELL_FLASH_CACHE", "MAXSIZE", "STORE", "LEVEL", "LOCKING", 
			"INMEMORY", "MEMCOMPRESS", "PRIORITY", "CRITICAL", "DISTRIBUTE", "RANGE", 
			"PARTITION", "SUBPARTITION", "SERVICE", "DUPLICATE", "ILM", "DELETE_ALL", 
			"ENABLE_ALL", "DISABLE_ALL", "AFTER", "MODIFICATION", "DAYS", "MONTHS", 
			"YEARS", "TIER", "ORGANIZATION", "HEAP", "PCTTHRESHOLD", "PARAMETERS", 
			"LOCATION", "MAPPING", "NOMAPPING", "INCLUDING", "OVERFLOW", "ATTRIBUTES", 
			"RESULT_CACHE", "ROWDEPENDENCIES", "NOROWDEPENDENCIES", "ARCHIVAL", "EXCHANGE", 
			"INDEXING", "OFF", "LESS", "INTERNAL", "VARRAY", "NESTED", "COLUMN_VALUE", 
			"RETURN", "LOCATOR", "MODE", "LOB", "SECUREFILE", "BASICFILE", "THAN", 
			"LIST", "AUTOMATIC", "HASH", "PARTITIONS", "SUBPARTITIONS", "TEMPLATE", 
			"PARTITIONSET", "REFERENCE", "CONSISTENT", "CLUSTERING", "LINEAR", "INTERLEAVED", 
			"YES", "LOAD", "MOVEMENT", "ZONEMAP", "WITHOUT", "XMLTYPE", "RELATIONAL", 
			"XML", "VARRAYS", "LOBS", "TABLES", "ALLOW", "DISALLOW", "NONSCHEMA", 
			"ANYSCHEMA", "XMLSCHEMA", "COLUMNS", "OIDINDEX", "EDITIONABLE", "NONEDITIONABLE", 
			"DEPENDENT", "INDEXES", "SHRINK", "SPACE", "COMPACT", "SUPPLEMENTAL", 
			"ADVISE", "NOTHING", "GUARD", "SYNC", "VISIBILITY", "ACTIVE", "DEFAULT_COLLATION", 
			"MOUNT", "STANDBY", "CLONE", "RESETLOGS", "NORESETLOGS", "UPGRADE", "DOWNGRADE", 
			"RECOVER", "LOGFILE", "TEST", "CORRUPTION", "CONTINUE", "CANCEL", "UNTIL", 
			"CONTROLFILE", "SNAPSHOT", "DATAFILE", "MANAGED", "ARCHIVED", "DISCONNECT", 
			"NODELAY", "INSTANCES", "FINISH", "LOGICAL", "FILE", "SIZE", "AUTOEXTEND", 
			"BLOCKSIZE", "OFFLINE", "RESIZE", "TEMPFILE", "DATAFILES", "ARCHIVELOG", 
			"MANUAL", "NOARCHIVELOG", "AVAILABILITY", "PERFORMANCE", "CLEAR", "UNARCHIVED", 
			"UNRECOVERABLE", "THREAD", "MEMBER", "PHYSICAL", "FAR", "TRACE", "DISTRIBUTED", 
			"RECOVERY", "FLUSH", "NOREPLY", "SWITCH", "LOGFILES", "PROCEDURAL", "REPLICATION", 
			"SUBSET", "ACTIVATE", "APPLY", "MAXIMIZE", "PROTECTION", "SUSPEND", "RESUME", 
			"QUIESCE", "UNQUIESCE", "SHUTDOWN", "REGISTER", "PREPARE", "SWITCHOVER", 
			"FAILED", "SKIP_SYMBOL", "STOP", "ABORT", "VERIFY", "CONVERT", "FAILOVER", 
			"BIGFILE", "SMALLFILE", "TRACKING", "CACHING", "CONTAINERS", "TARGET", 
			"UNDO", "MOVE", "MIRROR", "COPY", "UNPROTECTED", "REDUNDANCY", "REMOVE", 
			"LOST", "LEAD_CDB", "LEAD_CDB_URI", "PROPERTY", "DEFAULT_CREDENTIAL", 
			"TIME_ZONE", "RESET", "RELOCATE", "CLIENT", "PASSWORDFILE_METADATA_CACHE", 
			"NOSWITCH", "POST_TRANSACTION", "KILL", "ROLLING", "MIGRATION", "PATCH", 
			"ENCRYPTION", "WALLET", "AFFINITY", "MEMORY", "SPFILE", "BOTH", "SID", 
			"SHARED_POOL", "BUFFER_CACHE", "REDO", "CONFIRM", "MIGRATE", "USE_STORED_OUTLINES", 
			"GLOBAL_TOPIC_ENABLED", "INTERSECT", "MINUS", "LOCKED", "FETCH", "PERCENT", 
			"TIES", "SIBLINGS", "NULLS", "LAST", "ISOLATION", "SERIALIZABLE", "COMMITTED", 
			"FILTER", "FACT", "DETERMINISTIC", "PIPELINED", "PARALLEL_ENABLE", "OUT", 
			"NOCOPY", "ACCESSIBLE", "PACKAGE", "USING_NLS_COMP", "AUTHID", "SEARCH", 
			"DEPTH", "BREADTH", "ANALYTIC", "HIERARCHIES", "MEASURES", "OVER", "LAG", 
			"LAG_DIFF", "LAG_DIF_PERCENT", "LEAD", "LEAD_DIFF", "LEAD_DIFF_PERCENT", 
			"HIERARCHY", "WITHIN", "ACROSS", "ANCESTOR", "BEGINNING", "UNBOUNDED", 
			"PRECEDING", "FOLLOWING", "RANK", "DENSE_RANK", "AVERAGE_RANK", "ROW_NUMBER", 
			"SHARE_OF", "HIER_ANCESTOR", "HIER_PARENT", "HIER_LEAD", "HIER_LAG", 
			"QUALIFY", "HIER_CAPTION", "HIER_DEPTH", "HIER_DESCRIPTION", "HIER_LEVEL", 
			"HIER_MEMBER_NAME", "HIER_MEMBER_UNIQUE_NAME", "CHAINED", "STATISTICS", 
			"DANGLING", "STRUCTURE", "FAST", "COMPLETE", "IDENTIFIER_", "STRING_", 
			"INTEGER_", "NUMBER_", "HEX_DIGIT_", "BIT_NUM_"
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
			setState(320);
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
			setState(329);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_:
				enterOuterAlt(_localctx, 1);
				{
				setState(322);
				stringLiterals();
				}
				break;
			case PLUS_:
			case MINUS_:
			case INTEGER_:
			case NUMBER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(323);
				numberLiterals();
				}
				break;
			case LBE_:
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 3);
				{
				setState(324);
				dateTimeLiterals();
				}
				break;
			case HEX_DIGIT_:
				enterOuterAlt(_localctx, 4);
				{
				setState(325);
				hexadecimalLiterals();
				}
				break;
			case BIT_NUM_:
				enterOuterAlt(_localctx, 5);
				{
				setState(326);
				bitValueLiterals();
				}
				break;
			case TRUE:
			case FALSE:
				enterOuterAlt(_localctx, 6);
				{
				setState(327);
				booleanLiterals();
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 7);
				{
				setState(328);
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
			setState(331);
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
		public TerminalNode INTEGER_() { return getToken(BaseRuleParser.INTEGER_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
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
			setState(334);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS_ || _la==MINUS_) {
				{
				setState(333);
				_la = _input.LA(1);
				if ( !(_la==PLUS_ || _la==MINUS_) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(336);
			_la = _input.LA(1);
			if ( !(_la==INTEGER_ || _la==NUMBER_) ) {
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
			setState(345);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 1);
				{
				setState(338);
				_la = _input.LA(1);
				if ( !(((((_la - 130)) & ~0x3f) == 0 && ((1L << (_la - 130)) & ((1L << (DATE - 130)) | (1L << (TIME - 130)) | (1L << (TIMESTAMP - 130)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(339);
				match(STRING_);
				}
				break;
			case LBE_:
				enterOuterAlt(_localctx, 2);
				{
				setState(340);
				match(LBE_);
				setState(341);
				identifier();
				setState(342);
				match(STRING_);
				setState(343);
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
			setState(347);
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
			setState(349);
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
			setState(351);
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
			setState(353);
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
			setState(357);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(355);
				match(IDENTIFIER_);
				}
				break;
			case TRUNCATE:
			case SCHEMA:
			case FUNCTION:
			case PROCEDURE:
			case CASE:
			case WHEN:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case NATURAL:
			case JOIN:
			case FULL:
			case INNER:
			case OUTER:
			case LEFT:
			case RIGHT:
			case CROSS:
			case USING:
			case IF:
			case TRUE:
			case FALSE:
			case LIMIT:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHARACTER:
			case ARRAY:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case PRESERVE:
			case DO:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case REAL:
			case TYPE:
			case BINARY:
			case ESCAPE:
			case MOD:
			case XOR:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case PRIVILEGES:
			case READ:
			case WRITE:
			case REFERENCES:
			case TRANSACTION:
			case ROLE:
			case VISIBLE:
			case INVISIBLE:
			case EXECUTE:
			case USE:
			case DEBUG:
			case UNDER:
			case FLASHBACK:
			case ARCHIVE:
			case REFRESH:
			case QUERY:
			case REWRITE:
			case KEEP:
			case SEQUENCE:
			case INHERIT:
			case TRANSLATE:
			case MERGE:
			case AT:
			case BITMAP:
			case CACHE:
			case NOCACHE:
			case CHECKPOINT:
			case CONSTRAINTS:
			case CYCLE:
			case NOCYCLE:
			case DBTIMEZONE:
			case ENCRYPT:
			case DECRYPT:
			case DEFERRABLE:
			case DEFERRED:
			case EDITION:
			case ELEMENT:
			case END:
			case EXCEPTIONS:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case INITIALLY:
			case INVALIDATE:
			case JAVA:
			case LEVELS:
			case MAXVALUE:
			case MINVALUE:
			case NOMAXVALUE:
			case NOMINVALUE:
			case MINING:
			case MODEL:
			case NATIONAL:
			case NEW:
			case NOORDER:
			case NORELY:
			case ONLY:
			case PROFILE:
			case REF:
			case REKEY:
			case RELY:
			case REPLACE:
			case SALT:
			case SCOPE:
			case SORT:
			case SOURCE:
			case SUBSTITUTABLE:
			case TABLESPACE:
			case TEMPORARY:
			case TRANSLATION:
			case TREAT:
			case NO:
			case UNUSED:
			case NOVALIDATE:
			case VALUE:
			case VARYING:
			case VIRTUAL:
			case ZONE:
			case ADVISOR:
			case ADMINISTER:
			case TUNING:
			case MANAGE:
			case MANAGEMENT:
			case OBJECT:
			case CONTEXT:
			case EXEMPT:
			case REDACTION:
			case POLICY:
			case DATABASE:
			case SYSTEM:
			case LINK:
			case ANALYZE:
			case DICTIONARY:
			case DIMENSION:
			case INDEXTYPE:
			case EXTERNAL:
			case JOB:
			case CLASS:
			case PROGRAM:
			case SCHEDULER:
			case LIBRARY:
			case LOGMINING:
			case MATERIALIZED:
			case CUBE:
			case MEASURE:
			case FOLDER:
			case BUILD:
			case PROCESS:
			case OPERATOR:
			case OUTLINE:
			case PLUGGABLE:
			case CONTAINER:
			case SEGMENT:
			case RESTRICTED:
			case COST:
			case BACKUP:
			case UNLIMITED:
			case BECOME:
			case CHANGE:
			case NOTIFICATION:
			case PRIVILEGE:
			case PURGE:
			case RESUMABLE:
			case SYSGUID:
			case SYSBACKUP:
			case SYSDBA:
			case SYSDG:
			case SYSKM:
			case SYSOPER:
			case DBA_RECYCLEBIN:
			case FIRST:
				enterOuterAlt(_localctx, 2);
				{
				setState(356);
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
		public TerminalNode PROCEDURE() { return getToken(BaseRuleParser.PROCEDURE, 0); }
		public TerminalNode CASE() { return getToken(BaseRuleParser.CASE, 0); }
		public TerminalNode WHEN() { return getToken(BaseRuleParser.WHEN, 0); }
		public TerminalNode CAST() { return getToken(BaseRuleParser.CAST, 0); }
		public TerminalNode TRIM() { return getToken(BaseRuleParser.TRIM, 0); }
		public TerminalNode SUBSTRING() { return getToken(BaseRuleParser.SUBSTRING, 0); }
		public TerminalNode NATURAL() { return getToken(BaseRuleParser.NATURAL, 0); }
		public TerminalNode JOIN() { return getToken(BaseRuleParser.JOIN, 0); }
		public TerminalNode FULL() { return getToken(BaseRuleParser.FULL, 0); }
		public TerminalNode INNER() { return getToken(BaseRuleParser.INNER, 0); }
		public TerminalNode OUTER() { return getToken(BaseRuleParser.OUTER, 0); }
		public TerminalNode LEFT() { return getToken(BaseRuleParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(BaseRuleParser.RIGHT, 0); }
		public TerminalNode CROSS() { return getToken(BaseRuleParser.CROSS, 0); }
		public TerminalNode USING() { return getToken(BaseRuleParser.USING, 0); }
		public TerminalNode IF() { return getToken(BaseRuleParser.IF, 0); }
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public TerminalNode LIMIT() { return getToken(BaseRuleParser.LIMIT, 0); }
		public TerminalNode OFFSET() { return getToken(BaseRuleParser.OFFSET, 0); }
		public TerminalNode BEGIN() { return getToken(BaseRuleParser.BEGIN, 0); }
		public TerminalNode COMMIT() { return getToken(BaseRuleParser.COMMIT, 0); }
		public TerminalNode ROLLBACK() { return getToken(BaseRuleParser.ROLLBACK, 0); }
		public TerminalNode SAVEPOINT() { return getToken(BaseRuleParser.SAVEPOINT, 0); }
		public TerminalNode BOOLEAN() { return getToken(BaseRuleParser.BOOLEAN, 0); }
		public TerminalNode DOUBLE() { return getToken(BaseRuleParser.DOUBLE, 0); }
		public TerminalNode CHARACTER() { return getToken(BaseRuleParser.CHARACTER, 0); }
		public TerminalNode ARRAY() { return getToken(BaseRuleParser.ARRAY, 0); }
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(BaseRuleParser.TIMESTAMP, 0); }
		public TerminalNode LOCALTIME() { return getToken(BaseRuleParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(BaseRuleParser.LOCALTIMESTAMP, 0); }
		public TerminalNode YEAR() { return getToken(BaseRuleParser.YEAR, 0); }
		public TerminalNode QUARTER() { return getToken(BaseRuleParser.QUARTER, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public TerminalNode WEEK() { return getToken(BaseRuleParser.WEEK, 0); }
		public TerminalNode DAY() { return getToken(BaseRuleParser.DAY, 0); }
		public TerminalNode HOUR() { return getToken(BaseRuleParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(BaseRuleParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public TerminalNode MICROSECOND() { return getToken(BaseRuleParser.MICROSECOND, 0); }
		public TerminalNode MAX() { return getToken(BaseRuleParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(BaseRuleParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(BaseRuleParser.SUM, 0); }
		public TerminalNode COUNT() { return getToken(BaseRuleParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(BaseRuleParser.AVG, 0); }
		public TerminalNode ENABLE() { return getToken(BaseRuleParser.ENABLE, 0); }
		public TerminalNode DISABLE() { return getToken(BaseRuleParser.DISABLE, 0); }
		public TerminalNode BINARY() { return getToken(BaseRuleParser.BINARY, 0); }
		public TerminalNode ESCAPE() { return getToken(BaseRuleParser.ESCAPE, 0); }
		public TerminalNode MOD() { return getToken(BaseRuleParser.MOD, 0); }
		public TerminalNode UNKNOWN() { return getToken(BaseRuleParser.UNKNOWN, 0); }
		public TerminalNode XOR() { return getToken(BaseRuleParser.XOR, 0); }
		public TerminalNode ALWAYS() { return getToken(BaseRuleParser.ALWAYS, 0); }
		public TerminalNode CASCADE() { return getToken(BaseRuleParser.CASCADE, 0); }
		public TerminalNode GENERATED() { return getToken(BaseRuleParser.GENERATED, 0); }
		public TerminalNode PRIVILEGES() { return getToken(BaseRuleParser.PRIVILEGES, 0); }
		public TerminalNode READ() { return getToken(BaseRuleParser.READ, 0); }
		public TerminalNode WRITE() { return getToken(BaseRuleParser.WRITE, 0); }
		public TerminalNode REFERENCES() { return getToken(BaseRuleParser.REFERENCES, 0); }
		public TerminalNode TRANSACTION() { return getToken(BaseRuleParser.TRANSACTION, 0); }
		public TerminalNode ROLE() { return getToken(BaseRuleParser.ROLE, 0); }
		public TerminalNode VISIBLE() { return getToken(BaseRuleParser.VISIBLE, 0); }
		public TerminalNode INVISIBLE() { return getToken(BaseRuleParser.INVISIBLE, 0); }
		public TerminalNode EXECUTE() { return getToken(BaseRuleParser.EXECUTE, 0); }
		public TerminalNode USE() { return getToken(BaseRuleParser.USE, 0); }
		public TerminalNode DEBUG() { return getToken(BaseRuleParser.DEBUG, 0); }
		public TerminalNode UNDER() { return getToken(BaseRuleParser.UNDER, 0); }
		public TerminalNode FLASHBACK() { return getToken(BaseRuleParser.FLASHBACK, 0); }
		public TerminalNode ARCHIVE() { return getToken(BaseRuleParser.ARCHIVE, 0); }
		public TerminalNode REFRESH() { return getToken(BaseRuleParser.REFRESH, 0); }
		public TerminalNode QUERY() { return getToken(BaseRuleParser.QUERY, 0); }
		public TerminalNode REWRITE() { return getToken(BaseRuleParser.REWRITE, 0); }
		public TerminalNode KEEP() { return getToken(BaseRuleParser.KEEP, 0); }
		public TerminalNode SEQUENCE() { return getToken(BaseRuleParser.SEQUENCE, 0); }
		public TerminalNode INHERIT() { return getToken(BaseRuleParser.INHERIT, 0); }
		public TerminalNode TRANSLATE() { return getToken(BaseRuleParser.TRANSLATE, 0); }
		public TerminalNode SQL() { return getToken(BaseRuleParser.SQL, 0); }
		public TerminalNode MERGE() { return getToken(BaseRuleParser.MERGE, 0); }
		public TerminalNode AT() { return getToken(BaseRuleParser.AT, 0); }
		public TerminalNode BITMAP() { return getToken(BaseRuleParser.BITMAP, 0); }
		public TerminalNode CACHE() { return getToken(BaseRuleParser.CACHE, 0); }
		public TerminalNode CHECKPOINT() { return getToken(BaseRuleParser.CHECKPOINT, 0); }
		public TerminalNode CONSTRAINTS() { return getToken(BaseRuleParser.CONSTRAINTS, 0); }
		public TerminalNode CYCLE() { return getToken(BaseRuleParser.CYCLE, 0); }
		public TerminalNode DBTIMEZONE() { return getToken(BaseRuleParser.DBTIMEZONE, 0); }
		public TerminalNode ENCRYPT() { return getToken(BaseRuleParser.ENCRYPT, 0); }
		public TerminalNode DECRYPT() { return getToken(BaseRuleParser.DECRYPT, 0); }
		public TerminalNode DEFERRABLE() { return getToken(BaseRuleParser.DEFERRABLE, 0); }
		public TerminalNode DEFERRED() { return getToken(BaseRuleParser.DEFERRED, 0); }
		public TerminalNode EDITION() { return getToken(BaseRuleParser.EDITION, 0); }
		public TerminalNode ELEMENT() { return getToken(BaseRuleParser.ELEMENT, 0); }
		public TerminalNode END() { return getToken(BaseRuleParser.END, 0); }
		public TerminalNode EXCEPTIONS() { return getToken(BaseRuleParser.EXCEPTIONS, 0); }
		public TerminalNode FORCE() { return getToken(BaseRuleParser.FORCE, 0); }
		public TerminalNode GLOBAL() { return getToken(BaseRuleParser.GLOBAL, 0); }
		public TerminalNode IDENTITY() { return getToken(BaseRuleParser.IDENTITY, 0); }
		public TerminalNode INITIALLY() { return getToken(BaseRuleParser.INITIALLY, 0); }
		public TerminalNode INVALIDATE() { return getToken(BaseRuleParser.INVALIDATE, 0); }
		public TerminalNode JAVA() { return getToken(BaseRuleParser.JAVA, 0); }
		public TerminalNode LEVELS() { return getToken(BaseRuleParser.LEVELS, 0); }
		public TerminalNode LOCAL() { return getToken(BaseRuleParser.LOCAL, 0); }
		public TerminalNode MAXVALUE() { return getToken(BaseRuleParser.MAXVALUE, 0); }
		public TerminalNode MINVALUE() { return getToken(BaseRuleParser.MINVALUE, 0); }
		public TerminalNode NOMAXVALUE() { return getToken(BaseRuleParser.NOMAXVALUE, 0); }
		public TerminalNode NOMINVALUE() { return getToken(BaseRuleParser.NOMINVALUE, 0); }
		public TerminalNode MINING() { return getToken(BaseRuleParser.MINING, 0); }
		public TerminalNode MODEL() { return getToken(BaseRuleParser.MODEL, 0); }
		public TerminalNode NATIONAL() { return getToken(BaseRuleParser.NATIONAL, 0); }
		public TerminalNode NEW() { return getToken(BaseRuleParser.NEW, 0); }
		public TerminalNode NOCACHE() { return getToken(BaseRuleParser.NOCACHE, 0); }
		public TerminalNode NOCYCLE() { return getToken(BaseRuleParser.NOCYCLE, 0); }
		public TerminalNode NOORDER() { return getToken(BaseRuleParser.NOORDER, 0); }
		public TerminalNode NORELY() { return getToken(BaseRuleParser.NORELY, 0); }
		public TerminalNode NOVALIDATE() { return getToken(BaseRuleParser.NOVALIDATE, 0); }
		public TerminalNode ONLY() { return getToken(BaseRuleParser.ONLY, 0); }
		public TerminalNode PRESERVE() { return getToken(BaseRuleParser.PRESERVE, 0); }
		public TerminalNode PROFILE() { return getToken(BaseRuleParser.PROFILE, 0); }
		public TerminalNode REF() { return getToken(BaseRuleParser.REF, 0); }
		public TerminalNode REKEY() { return getToken(BaseRuleParser.REKEY, 0); }
		public TerminalNode RELY() { return getToken(BaseRuleParser.RELY, 0); }
		public TerminalNode REPLACE() { return getToken(BaseRuleParser.REPLACE, 0); }
		public TerminalNode SOURCE() { return getToken(BaseRuleParser.SOURCE, 0); }
		public TerminalNode SALT() { return getToken(BaseRuleParser.SALT, 0); }
		public TerminalNode SCOPE() { return getToken(BaseRuleParser.SCOPE, 0); }
		public TerminalNode SORT() { return getToken(BaseRuleParser.SORT, 0); }
		public TerminalNode SUBSTITUTABLE() { return getToken(BaseRuleParser.SUBSTITUTABLE, 0); }
		public TerminalNode TABLESPACE() { return getToken(BaseRuleParser.TABLESPACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(BaseRuleParser.TEMPORARY, 0); }
		public TerminalNode TRANSLATION() { return getToken(BaseRuleParser.TRANSLATION, 0); }
		public TerminalNode TREAT() { return getToken(BaseRuleParser.TREAT, 0); }
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode TYPE() { return getToken(BaseRuleParser.TYPE, 0); }
		public TerminalNode UNUSED() { return getToken(BaseRuleParser.UNUSED, 0); }
		public TerminalNode VALUE() { return getToken(BaseRuleParser.VALUE, 0); }
		public TerminalNode VARYING() { return getToken(BaseRuleParser.VARYING, 0); }
		public TerminalNode VIRTUAL() { return getToken(BaseRuleParser.VIRTUAL, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode ADVISOR() { return getToken(BaseRuleParser.ADVISOR, 0); }
		public TerminalNode ADMINISTER() { return getToken(BaseRuleParser.ADMINISTER, 0); }
		public TerminalNode TUNING() { return getToken(BaseRuleParser.TUNING, 0); }
		public TerminalNode MANAGE() { return getToken(BaseRuleParser.MANAGE, 0); }
		public TerminalNode MANAGEMENT() { return getToken(BaseRuleParser.MANAGEMENT, 0); }
		public TerminalNode OBJECT() { return getToken(BaseRuleParser.OBJECT, 0); }
		public TerminalNode CONTEXT() { return getToken(BaseRuleParser.CONTEXT, 0); }
		public TerminalNode EXEMPT() { return getToken(BaseRuleParser.EXEMPT, 0); }
		public TerminalNode REDACTION() { return getToken(BaseRuleParser.REDACTION, 0); }
		public TerminalNode POLICY() { return getToken(BaseRuleParser.POLICY, 0); }
		public TerminalNode DATABASE() { return getToken(BaseRuleParser.DATABASE, 0); }
		public TerminalNode SYSTEM() { return getToken(BaseRuleParser.SYSTEM, 0); }
		public TerminalNode LINK() { return getToken(BaseRuleParser.LINK, 0); }
		public TerminalNode ANALYZE() { return getToken(BaseRuleParser.ANALYZE, 0); }
		public TerminalNode DICTIONARY() { return getToken(BaseRuleParser.DICTIONARY, 0); }
		public TerminalNode DIMENSION() { return getToken(BaseRuleParser.DIMENSION, 0); }
		public TerminalNode INDEXTYPE() { return getToken(BaseRuleParser.INDEXTYPE, 0); }
		public TerminalNode EXTERNAL() { return getToken(BaseRuleParser.EXTERNAL, 0); }
		public TerminalNode JOB() { return getToken(BaseRuleParser.JOB, 0); }
		public TerminalNode CLASS() { return getToken(BaseRuleParser.CLASS, 0); }
		public TerminalNode PROGRAM() { return getToken(BaseRuleParser.PROGRAM, 0); }
		public TerminalNode SCHEDULER() { return getToken(BaseRuleParser.SCHEDULER, 0); }
		public TerminalNode LIBRARY() { return getToken(BaseRuleParser.LIBRARY, 0); }
		public TerminalNode LOGMINING() { return getToken(BaseRuleParser.LOGMINING, 0); }
		public TerminalNode MATERIALIZED() { return getToken(BaseRuleParser.MATERIALIZED, 0); }
		public TerminalNode CUBE() { return getToken(BaseRuleParser.CUBE, 0); }
		public TerminalNode MEASURE() { return getToken(BaseRuleParser.MEASURE, 0); }
		public TerminalNode FOLDER() { return getToken(BaseRuleParser.FOLDER, 0); }
		public TerminalNode BUILD() { return getToken(BaseRuleParser.BUILD, 0); }
		public TerminalNode PROCESS() { return getToken(BaseRuleParser.PROCESS, 0); }
		public TerminalNode OPERATOR() { return getToken(BaseRuleParser.OPERATOR, 0); }
		public TerminalNode OUTLINE() { return getToken(BaseRuleParser.OUTLINE, 0); }
		public TerminalNode PLUGGABLE() { return getToken(BaseRuleParser.PLUGGABLE, 0); }
		public TerminalNode CONTAINER() { return getToken(BaseRuleParser.CONTAINER, 0); }
		public TerminalNode SEGMENT() { return getToken(BaseRuleParser.SEGMENT, 0); }
		public TerminalNode RESTRICTED() { return getToken(BaseRuleParser.RESTRICTED, 0); }
		public TerminalNode COST() { return getToken(BaseRuleParser.COST, 0); }
		public TerminalNode BACKUP() { return getToken(BaseRuleParser.BACKUP, 0); }
		public TerminalNode UNLIMITED() { return getToken(BaseRuleParser.UNLIMITED, 0); }
		public TerminalNode BECOME() { return getToken(BaseRuleParser.BECOME, 0); }
		public TerminalNode CHANGE() { return getToken(BaseRuleParser.CHANGE, 0); }
		public TerminalNode NOTIFICATION() { return getToken(BaseRuleParser.NOTIFICATION, 0); }
		public TerminalNode PRIVILEGE() { return getToken(BaseRuleParser.PRIVILEGE, 0); }
		public TerminalNode PURGE() { return getToken(BaseRuleParser.PURGE, 0); }
		public TerminalNode RESUMABLE() { return getToken(BaseRuleParser.RESUMABLE, 0); }
		public TerminalNode SYSGUID() { return getToken(BaseRuleParser.SYSGUID, 0); }
		public TerminalNode SYSBACKUP() { return getToken(BaseRuleParser.SYSBACKUP, 0); }
		public TerminalNode SYSDBA() { return getToken(BaseRuleParser.SYSDBA, 0); }
		public TerminalNode SYSDG() { return getToken(BaseRuleParser.SYSDG, 0); }
		public TerminalNode SYSKM() { return getToken(BaseRuleParser.SYSKM, 0); }
		public TerminalNode SYSOPER() { return getToken(BaseRuleParser.SYSOPER, 0); }
		public TerminalNode DBA_RECYCLEBIN() { return getToken(BaseRuleParser.DBA_RECYCLEBIN, 0); }
		public TerminalNode SCHEMA() { return getToken(BaseRuleParser.SCHEMA, 0); }
		public TerminalNode DO() { return getToken(BaseRuleParser.DO, 0); }
		public TerminalNode DEFINER() { return getToken(BaseRuleParser.DEFINER, 0); }
		public TerminalNode CURRENT_USER() { return getToken(BaseRuleParser.CURRENT_USER, 0); }
		public TerminalNode CASCADED() { return getToken(BaseRuleParser.CASCADED, 0); }
		public TerminalNode CLOSE() { return getToken(BaseRuleParser.CLOSE, 0); }
		public TerminalNode OPEN() { return getToken(BaseRuleParser.OPEN, 0); }
		public TerminalNode NEXT() { return getToken(BaseRuleParser.NEXT, 0); }
		public TerminalNode NAME() { return getToken(BaseRuleParser.NAME, 0); }
		public TerminalNode NAMES() { return getToken(BaseRuleParser.NAMES, 0); }
		public TerminalNode COLLATION() { return getToken(BaseRuleParser.COLLATION, 0); }
		public TerminalNode REAL() { return getToken(BaseRuleParser.REAL, 0); }
		public TerminalNode FIRST() { return getToken(BaseRuleParser.FIRST, 0); }
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
			setState(359);
			_la = _input.LA(1);
			if ( !(((((_la - 51)) & ~0x3f) == 0 && ((1L << (_la - 51)) & ((1L << (TRUNCATE - 51)) | (1L << (SCHEMA - 51)) | (1L << (FUNCTION - 51)) | (1L << (PROCEDURE - 51)) | (1L << (CASE - 51)) | (1L << (WHEN - 51)) | (1L << (CAST - 51)) | (1L << (TRIM - 51)) | (1L << (SUBSTRING - 51)) | (1L << (NATURAL - 51)) | (1L << (JOIN - 51)) | (1L << (FULL - 51)) | (1L << (INNER - 51)) | (1L << (OUTER - 51)) | (1L << (LEFT - 51)) | (1L << (RIGHT - 51)) | (1L << (CROSS - 51)) | (1L << (USING - 51)) | (1L << (IF - 51)) | (1L << (TRUE - 51)) | (1L << (FALSE - 51)))) != 0) || ((((_la - 118)) & ~0x3f) == 0 && ((1L << (_la - 118)) & ((1L << (LIMIT - 118)) | (1L << (OFFSET - 118)) | (1L << (BEGIN - 118)) | (1L << (COMMIT - 118)) | (1L << (ROLLBACK - 118)) | (1L << (SAVEPOINT - 118)) | (1L << (BOOLEAN - 118)) | (1L << (DOUBLE - 118)) | (1L << (CHARACTER - 118)) | (1L << (ARRAY - 118)) | (1L << (INTERVAL - 118)) | (1L << (TIME - 118)) | (1L << (TIMESTAMP - 118)) | (1L << (LOCALTIME - 118)) | (1L << (LOCALTIMESTAMP - 118)) | (1L << (YEAR - 118)) | (1L << (QUARTER - 118)) | (1L << (MONTH - 118)) | (1L << (WEEK - 118)) | (1L << (DAY - 118)) | (1L << (HOUR - 118)) | (1L << (MINUTE - 118)) | (1L << (SECOND - 118)) | (1L << (MICROSECOND - 118)) | (1L << (MAX - 118)) | (1L << (MIN - 118)) | (1L << (SUM - 118)) | (1L << (COUNT - 118)) | (1L << (AVG - 118)) | (1L << (ENABLE - 118)) | (1L << (DISABLE - 118)) | (1L << (PRESERVE - 118)) | (1L << (DO - 118)) | (1L << (DEFINER - 118)) | (1L << (CURRENT_USER - 118)) | (1L << (SQL - 118)) | (1L << (CASCADED - 118)) | (1L << (LOCAL - 118)) | (1L << (CLOSE - 118)) | (1L << (OPEN - 118)) | (1L << (NEXT - 118)) | (1L << (NAME - 118)) | (1L << (COLLATION - 118)) | (1L << (NAMES - 118)) | (1L << (REAL - 118)) | (1L << (TYPE - 118)) | (1L << (BINARY - 118)) | (1L << (ESCAPE - 118)))) != 0) || ((((_la - 182)) & ~0x3f) == 0 && ((1L << (_la - 182)) & ((1L << (MOD - 182)) | (1L << (XOR - 182)) | (1L << (UNKNOWN - 182)) | (1L << (ALWAYS - 182)) | (1L << (CASCADE - 182)) | (1L << (GENERATED - 182)) | (1L << (PRIVILEGES - 182)) | (1L << (READ - 182)) | (1L << (WRITE - 182)) | (1L << (REFERENCES - 182)) | (1L << (TRANSACTION - 182)) | (1L << (ROLE - 182)) | (1L << (VISIBLE - 182)) | (1L << (INVISIBLE - 182)) | (1L << (EXECUTE - 182)) | (1L << (USE - 182)) | (1L << (DEBUG - 182)) | (1L << (UNDER - 182)) | (1L << (FLASHBACK - 182)) | (1L << (ARCHIVE - 182)) | (1L << (REFRESH - 182)) | (1L << (QUERY - 182)) | (1L << (REWRITE - 182)) | (1L << (KEEP - 182)) | (1L << (SEQUENCE - 182)) | (1L << (INHERIT - 182)) | (1L << (TRANSLATE - 182)) | (1L << (MERGE - 182)) | (1L << (AT - 182)) | (1L << (BITMAP - 182)) | (1L << (CACHE - 182)) | (1L << (NOCACHE - 182)) | (1L << (CHECKPOINT - 182)) | (1L << (CONSTRAINTS - 182)) | (1L << (CYCLE - 182)) | (1L << (NOCYCLE - 182)) | (1L << (DBTIMEZONE - 182)) | (1L << (ENCRYPT - 182)) | (1L << (DECRYPT - 182)) | (1L << (DEFERRABLE - 182)) | (1L << (DEFERRED - 182)) | (1L << (EDITION - 182)) | (1L << (ELEMENT - 182)) | (1L << (END - 182)) | (1L << (EXCEPTIONS - 182)) | (1L << (FORCE - 182)) | (1L << (GLOBAL - 182)) | (1L << (IDENTITY - 182)) | (1L << (INITIALLY - 182)) | (1L << (INVALIDATE - 182)) | (1L << (JAVA - 182)) | (1L << (LEVELS - 182)) | (1L << (MAXVALUE - 182)))) != 0) || ((((_la - 246)) & ~0x3f) == 0 && ((1L << (_la - 246)) & ((1L << (MINVALUE - 246)) | (1L << (NOMAXVALUE - 246)) | (1L << (NOMINVALUE - 246)) | (1L << (MINING - 246)) | (1L << (MODEL - 246)) | (1L << (NATIONAL - 246)) | (1L << (NEW - 246)) | (1L << (NOORDER - 246)) | (1L << (NORELY - 246)) | (1L << (ONLY - 246)) | (1L << (PROFILE - 246)) | (1L << (REF - 246)) | (1L << (REKEY - 246)) | (1L << (RELY - 246)) | (1L << (REPLACE - 246)) | (1L << (SALT - 246)) | (1L << (SCOPE - 246)) | (1L << (SORT - 246)) | (1L << (SOURCE - 246)) | (1L << (SUBSTITUTABLE - 246)) | (1L << (TABLESPACE - 246)) | (1L << (TEMPORARY - 246)) | (1L << (TRANSLATION - 246)) | (1L << (TREAT - 246)) | (1L << (NO - 246)) | (1L << (UNUSED - 246)) | (1L << (NOVALIDATE - 246)) | (1L << (VALUE - 246)) | (1L << (VARYING - 246)) | (1L << (VIRTUAL - 246)) | (1L << (ZONE - 246)) | (1L << (ADVISOR - 246)) | (1L << (ADMINISTER - 246)) | (1L << (TUNING - 246)) | (1L << (MANAGE - 246)) | (1L << (MANAGEMENT - 246)) | (1L << (OBJECT - 246)) | (1L << (CONTEXT - 246)) | (1L << (EXEMPT - 246)) | (1L << (REDACTION - 246)) | (1L << (POLICY - 246)) | (1L << (DATABASE - 246)) | (1L << (SYSTEM - 246)) | (1L << (LINK - 246)) | (1L << (ANALYZE - 246)) | (1L << (DICTIONARY - 246)) | (1L << (DIMENSION - 246)) | (1L << (INDEXTYPE - 246)) | (1L << (EXTERNAL - 246)))) != 0) || ((((_la - 310)) & ~0x3f) == 0 && ((1L << (_la - 310)) & ((1L << (JOB - 310)) | (1L << (CLASS - 310)) | (1L << (PROGRAM - 310)) | (1L << (SCHEDULER - 310)) | (1L << (LIBRARY - 310)) | (1L << (LOGMINING - 310)) | (1L << (MATERIALIZED - 310)) | (1L << (CUBE - 310)) | (1L << (MEASURE - 310)) | (1L << (FOLDER - 310)) | (1L << (BUILD - 310)) | (1L << (PROCESS - 310)) | (1L << (OPERATOR - 310)) | (1L << (OUTLINE - 310)) | (1L << (PLUGGABLE - 310)) | (1L << (CONTAINER - 310)) | (1L << (SEGMENT - 310)) | (1L << (RESTRICTED - 310)) | (1L << (COST - 310)) | (1L << (BACKUP - 310)) | (1L << (UNLIMITED - 310)) | (1L << (BECOME - 310)) | (1L << (CHANGE - 310)) | (1L << (NOTIFICATION - 310)) | (1L << (PRIVILEGE - 310)) | (1L << (PURGE - 310)) | (1L << (RESUMABLE - 310)) | (1L << (SYSGUID - 310)) | (1L << (SYSBACKUP - 310)) | (1L << (SYSDBA - 310)) | (1L << (SYSDG - 310)) | (1L << (SYSKM - 310)) | (1L << (SYSOPER - 310)) | (1L << (DBA_RECYCLEBIN - 310)) | (1L << (FIRST - 310)))) != 0)) ) {
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
			setState(361);
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
			setState(366);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(363);
				owner();
				setState(364);
				match(DOT_);
				}
				break;
			}
			setState(368);
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
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public ViewNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_viewName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterViewName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitViewName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitViewName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ViewNameContext viewName() throws RecognitionException {
		ViewNameContext _localctx = new ViewNameContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_viewName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(373);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(370);
				owner();
				setState(371);
				match(DOT_);
				}
				break;
			}
			setState(375);
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
		enterRule(_localctx, 28, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(380);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(377);
				owner();
				setState(378);
				match(DOT_);
				}
				break;
			}
			setState(382);
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

	public static class ObjectNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public ObjectNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterObjectName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitObjectName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitObjectName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectNameContext objectName() throws RecognitionException {
		ObjectNameContext _localctx = new ObjectNameContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_objectName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(387);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(384);
				owner();
				setState(385);
				match(DOT_);
				}
				break;
			}
			setState(389);
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

	public static class ClusterNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public ClusterNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clusterName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterClusterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitClusterName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitClusterName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClusterNameContext clusterName() throws RecognitionException {
		ClusterNameContext _localctx = new ClusterNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_clusterName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(394);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(391);
				owner();
				setState(392);
				match(DOT_);
				}
				break;
			}
			setState(396);
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

	public static class IndexNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
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
		enterRule(_localctx, 34, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(401);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(398);
				owner();
				setState(399);
				match(DOT_);
				}
				break;
			}
			setState(403);
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

	public static class ConstraintNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ConstraintNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConstraintName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConstraintName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConstraintName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintNameContext constraintName() throws RecognitionException {
		ConstraintNameContext _localctx = new ConstraintNameContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_constraintName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
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

	public static class SavepointNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SavepointNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_savepointName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSavepointName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSavepointName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSavepointName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SavepointNameContext savepointName() throws RecognitionException {
		SavepointNameContext _localctx = new SavepointNameContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_savepointName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(407);
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

	public static class SynonymNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SynonymNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_synonymName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSynonymName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSynonymName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSynonymName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SynonymNameContext synonymName() throws RecognitionException {
		SynonymNameContext _localctx = new SynonymNameContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_synonymName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
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
		enterRule(_localctx, 42, RULE_owner);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(411);
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
		enterRule(_localctx, 44, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
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

	public static class TablespaceNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TablespaceNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablespaceName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTablespaceName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTablespaceName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTablespaceName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablespaceNameContext tablespaceName() throws RecognitionException {
		TablespaceNameContext _localctx = new TablespaceNameContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_tablespaceName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(415);
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

	public static class TablespaceSetNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TablespaceSetNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablespaceSetName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTablespaceSetName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTablespaceSetName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTablespaceSetName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablespaceSetNameContext tablespaceSetName() throws RecognitionException {
		TablespaceSetNameContext _localctx = new TablespaceSetNameContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_tablespaceSetName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
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

	public static class ServiceNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ServiceNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_serviceName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterServiceName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitServiceName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitServiceName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ServiceNameContext serviceName() throws RecognitionException {
		ServiceNameContext _localctx = new ServiceNameContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_serviceName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
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

	public static class IlmPolicyNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public IlmPolicyNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ilmPolicyName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIlmPolicyName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIlmPolicyName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIlmPolicyName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IlmPolicyNameContext ilmPolicyName() throws RecognitionException {
		IlmPolicyNameContext _localctx = new IlmPolicyNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_ilmPolicyName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
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

	public static class FunctionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_functionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(423);
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

	public static class DbLinkContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DbLinkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dbLink; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDbLink(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDbLink(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDbLink(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DbLinkContext dbLink() throws RecognitionException {
		DbLinkContext _localctx = new DbLinkContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_dbLink);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(425);
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

	public static class ParameterValueContext extends ParserRuleContext {
		public LiteralsContext literals() {
			return getRuleContext(LiteralsContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ParameterValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterParameterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitParameterValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitParameterValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterValueContext parameterValue() throws RecognitionException {
		ParameterValueContext _localctx = new ParameterValueContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_parameterValue);
		try {
			setState(429);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(427);
				literals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(428);
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

	public static class DirectoryNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DirectoryNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directoryName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDirectoryName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDirectoryName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDirectoryName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectoryNameContext directoryName() throws RecognitionException {
		DirectoryNameContext _localctx = new DirectoryNameContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_directoryName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(431);
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

	public static class DispatcherNameContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public DispatcherNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dispatcherName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDispatcherName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDispatcherName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDispatcherName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DispatcherNameContext dispatcherName() throws RecognitionException {
		DispatcherNameContext _localctx = new DispatcherNameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_dispatcherName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(433);
			stringLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClientIdContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public ClientIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clientId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterClientId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitClientId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitClientId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClientIdContext clientId() throws RecognitionException {
		ClientIdContext _localctx = new ClientIdContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_clientId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(435);
			stringLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OpaqueFormatSpecContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public OpaqueFormatSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_opaqueFormatSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOpaqueFormatSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOpaqueFormatSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOpaqueFormatSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpaqueFormatSpecContext opaqueFormatSpec() throws RecognitionException {
		OpaqueFormatSpecContext _localctx = new OpaqueFormatSpecContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_opaqueFormatSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(437);
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

	public static class AccessDriverTypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public AccessDriverTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_accessDriverType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAccessDriverType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAccessDriverType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAccessDriverType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AccessDriverTypeContext accessDriverType() throws RecognitionException {
		AccessDriverTypeContext _localctx = new AccessDriverTypeContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_accessDriverType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(439);
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

	public static class TypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
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

	public static class VarrayItemContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public VarrayItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varrayItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterVarrayItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitVarrayItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitVarrayItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarrayItemContext varrayItem() throws RecognitionException {
		VarrayItemContext _localctx = new VarrayItemContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_varrayItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
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

	public static class NestedItemContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public NestedItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNestedItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNestedItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNestedItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedItemContext nestedItem() throws RecognitionException {
		NestedItemContext _localctx = new NestedItemContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_nestedItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445);
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

	public static class StorageTableContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public StorageTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_storageTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterStorageTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitStorageTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitStorageTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StorageTableContext storageTable() throws RecognitionException {
		StorageTableContext _localctx = new StorageTableContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_storageTable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
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

	public static class LobSegnameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LobSegnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lobSegname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLobSegname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLobSegname(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLobSegname(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LobSegnameContext lobSegname() throws RecognitionException {
		LobSegnameContext _localctx = new LobSegnameContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_lobSegname);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
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

	public static class LocationSpecifierContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LocationSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_locationSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLocationSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLocationSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLocationSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocationSpecifierContext locationSpecifier() throws RecognitionException {
		LocationSpecifierContext _localctx = new LocationSpecifierContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_locationSpecifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
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

	public static class XmlSchemaURLNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public XmlSchemaURLNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlSchemaURLName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlSchemaURLName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlSchemaURLName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlSchemaURLName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlSchemaURLNameContext xmlSchemaURLName() throws RecognitionException {
		XmlSchemaURLNameContext _localctx = new XmlSchemaURLNameContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_xmlSchemaURLName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(453);
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

	public static class ElementNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ElementNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterElementName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitElementName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitElementName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementNameContext elementName() throws RecognitionException {
		ElementNameContext _localctx = new ElementNameContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_elementName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
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

	public static class SubpartitionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SubpartitionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subpartitionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubpartitionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubpartitionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubpartitionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubpartitionNameContext subpartitionName() throws RecognitionException {
		SubpartitionNameContext _localctx = new SubpartitionNameContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_subpartitionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(457);
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

	public static class ParameterNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ParameterNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterParameterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitParameterName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitParameterName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterNameContext parameterName() throws RecognitionException {
		ParameterNameContext _localctx = new ParameterNameContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_parameterName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(459);
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

	public static class EditionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public EditionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_editionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEditionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEditionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEditionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EditionNameContext editionName() throws RecognitionException {
		EditionNameContext _localctx = new EditionNameContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_editionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(461);
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

	public static class ContainerNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ContainerNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_containerName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterContainerName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitContainerName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitContainerName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContainerNameContext containerName() throws RecognitionException {
		ContainerNameContext _localctx = new ContainerNameContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_containerName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
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

	public static class PartitionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public PartitionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionNameContext partitionName() throws RecognitionException {
		PartitionNameContext _localctx = new PartitionNameContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_partitionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(465);
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

	public static class PartitionSetNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public PartitionSetNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionSetName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionSetName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionSetName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionSetName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionSetNameContext partitionSetName() throws RecognitionException {
		PartitionSetNameContext _localctx = new PartitionSetNameContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_partitionSetName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(467);
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

	public static class PartitionKeyValueContext extends ParserRuleContext {
		public TerminalNode INTEGER_() { return getToken(BaseRuleParser.INTEGER_, 0); }
		public DateTimeLiteralsContext dateTimeLiterals() {
			return getRuleContext(DateTimeLiteralsContext.class,0);
		}
		public PartitionKeyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionKeyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionKeyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionKeyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionKeyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionKeyValueContext partitionKeyValue() throws RecognitionException {
		PartitionKeyValueContext _localctx = new PartitionKeyValueContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_partitionKeyValue);
		try {
			setState(471);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(469);
				match(INTEGER_);
				}
				break;
			case LBE_:
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 2);
				{
				setState(470);
				dateTimeLiterals();
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

	public static class SubpartitionKeyValueContext extends ParserRuleContext {
		public TerminalNode INTEGER_() { return getToken(BaseRuleParser.INTEGER_, 0); }
		public DateTimeLiteralsContext dateTimeLiterals() {
			return getRuleContext(DateTimeLiteralsContext.class,0);
		}
		public SubpartitionKeyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subpartitionKeyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubpartitionKeyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubpartitionKeyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubpartitionKeyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubpartitionKeyValueContext subpartitionKeyValue() throws RecognitionException {
		SubpartitionKeyValueContext _localctx = new SubpartitionKeyValueContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_subpartitionKeyValue);
		try {
			setState(475);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(473);
				match(INTEGER_);
				}
				break;
			case LBE_:
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 2);
				{
				setState(474);
				dateTimeLiterals();
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

	public static class ZonemapNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ZonemapNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_zonemapName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterZonemapName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitZonemapName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitZonemapName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ZonemapNameContext zonemapName() throws RecognitionException {
		ZonemapNameContext _localctx = new ZonemapNameContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_zonemapName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(477);
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

	public static class FlashbackArchiveNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FlashbackArchiveNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flashbackArchiveName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFlashbackArchiveName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFlashbackArchiveName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFlashbackArchiveName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlashbackArchiveNameContext flashbackArchiveName() throws RecognitionException {
		FlashbackArchiveNameContext _localctx = new FlashbackArchiveNameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_flashbackArchiveName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(479);
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

	public static class RoleNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RoleNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_roleName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRoleName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRoleName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRoleName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoleNameContext roleName() throws RecognitionException {
		RoleNameContext _localctx = new RoleNameContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_roleName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(481);
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

	public static class PasswordContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public PasswordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_password; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPassword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPassword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPassword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PasswordContext password() throws RecognitionException {
		PasswordContext _localctx = new PasswordContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_password);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(483);
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

	public static class LogGroupNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LogGroupNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logGroupName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLogGroupName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLogGroupName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLogGroupName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogGroupNameContext logGroupName() throws RecognitionException {
		LogGroupNameContext _localctx = new LogGroupNameContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_logGroupName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(485);
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
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
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
		enterRule(_localctx, 112, RULE_columnNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(487);
				match(LP_);
				}
			}

			setState(490);
			columnName();
			setState(495);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(491);
				match(COMMA_);
				setState(492);
				columnName();
				}
				}
				setState(497);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(499);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RP_) {
				{
				setState(498);
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
		enterRule(_localctx, 114, RULE_tableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(502);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(501);
				match(LP_);
				}
			}

			setState(504);
			tableName();
			setState(509);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(505);
				match(COMMA_);
				setState(506);
				tableName();
				}
				}
				setState(511);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(513);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RP_) {
				{
				setState(512);
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

	public static class OracleIdContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public List<TerminalNode> STRING_() { return getTokens(BaseRuleParser.STRING_); }
		public TerminalNode STRING_(int i) {
			return getToken(BaseRuleParser.STRING_, i);
		}
		public List<TerminalNode> DOT_() { return getTokens(BaseRuleParser.DOT_); }
		public TerminalNode DOT_(int i) {
			return getToken(BaseRuleParser.DOT_, i);
		}
		public OracleIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_oracleId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOracleId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOracleId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOracleId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OracleIdContext oracleId() throws RecognitionException {
		OracleIdContext _localctx = new OracleIdContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_oracleId);
		try {
			int _alt;
			setState(524);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(515);
				match(IDENTIFIER_);
				}
				break;
			case STRING_:
				enterOuterAlt(_localctx, 2);
				{
				setState(520);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(516);
						match(STRING_);
						setState(517);
						match(DOT_);
						}
						} 
					}
					setState(522);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				setState(523);
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
		enterRule(_localctx, 118, RULE_collationName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(526);
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

	public static class ColumnCollationNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ColumnCollationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnCollationName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnCollationName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnCollationName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnCollationName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnCollationNameContext columnCollationName() throws RecognitionException {
		ColumnCollationNameContext _localctx = new ColumnCollationNameContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_columnCollationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528);
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

	public static class AliasContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
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
		enterRule(_localctx, 122, RULE_alias);
		try {
			setState(532);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUNCATE:
			case SCHEMA:
			case FUNCTION:
			case PROCEDURE:
			case CASE:
			case WHEN:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case NATURAL:
			case JOIN:
			case FULL:
			case INNER:
			case OUTER:
			case LEFT:
			case RIGHT:
			case CROSS:
			case USING:
			case IF:
			case TRUE:
			case FALSE:
			case LIMIT:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHARACTER:
			case ARRAY:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case PRESERVE:
			case DO:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case REAL:
			case TYPE:
			case BINARY:
			case ESCAPE:
			case MOD:
			case XOR:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case PRIVILEGES:
			case READ:
			case WRITE:
			case REFERENCES:
			case TRANSACTION:
			case ROLE:
			case VISIBLE:
			case INVISIBLE:
			case EXECUTE:
			case USE:
			case DEBUG:
			case UNDER:
			case FLASHBACK:
			case ARCHIVE:
			case REFRESH:
			case QUERY:
			case REWRITE:
			case KEEP:
			case SEQUENCE:
			case INHERIT:
			case TRANSLATE:
			case MERGE:
			case AT:
			case BITMAP:
			case CACHE:
			case NOCACHE:
			case CHECKPOINT:
			case CONSTRAINTS:
			case CYCLE:
			case NOCYCLE:
			case DBTIMEZONE:
			case ENCRYPT:
			case DECRYPT:
			case DEFERRABLE:
			case DEFERRED:
			case EDITION:
			case ELEMENT:
			case END:
			case EXCEPTIONS:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case INITIALLY:
			case INVALIDATE:
			case JAVA:
			case LEVELS:
			case MAXVALUE:
			case MINVALUE:
			case NOMAXVALUE:
			case NOMINVALUE:
			case MINING:
			case MODEL:
			case NATIONAL:
			case NEW:
			case NOORDER:
			case NORELY:
			case ONLY:
			case PROFILE:
			case REF:
			case REKEY:
			case RELY:
			case REPLACE:
			case SALT:
			case SCOPE:
			case SORT:
			case SOURCE:
			case SUBSTITUTABLE:
			case TABLESPACE:
			case TEMPORARY:
			case TRANSLATION:
			case TREAT:
			case NO:
			case UNUSED:
			case NOVALIDATE:
			case VALUE:
			case VARYING:
			case VIRTUAL:
			case ZONE:
			case ADVISOR:
			case ADMINISTER:
			case TUNING:
			case MANAGE:
			case MANAGEMENT:
			case OBJECT:
			case CONTEXT:
			case EXEMPT:
			case REDACTION:
			case POLICY:
			case DATABASE:
			case SYSTEM:
			case LINK:
			case ANALYZE:
			case DICTIONARY:
			case DIMENSION:
			case INDEXTYPE:
			case EXTERNAL:
			case JOB:
			case CLASS:
			case PROGRAM:
			case SCHEDULER:
			case LIBRARY:
			case LOGMINING:
			case MATERIALIZED:
			case CUBE:
			case MEASURE:
			case FOLDER:
			case BUILD:
			case PROCESS:
			case OPERATOR:
			case OUTLINE:
			case PLUGGABLE:
			case CONTAINER:
			case SEGMENT:
			case RESTRICTED:
			case COST:
			case BACKUP:
			case UNLIMITED:
			case BECOME:
			case CHANGE:
			case NOTIFICATION:
			case PRIVILEGE:
			case PURGE:
			case RESUMABLE:
			case SYSGUID:
			case SYSBACKUP:
			case SYSDBA:
			case SYSDG:
			case SYSKM:
			case SYSOPER:
			case DBA_RECYCLEBIN:
			case FIRST:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(530);
				identifier();
				}
				break;
			case STRING_:
				enterOuterAlt(_localctx, 2);
				{
				setState(531);
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

	public static class DataTypeLengthContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> INTEGER_() { return getTokens(BaseRuleParser.INTEGER_); }
		public TerminalNode INTEGER_(int i) {
			return getToken(BaseRuleParser.INTEGER_, i);
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
		enterRule(_localctx, 124, RULE_dataTypeLength);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534);
			match(LP_);
			setState(540);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTEGER_) {
				{
				setState(535);
				match(INTEGER_);
				setState(538);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(536);
					match(COMMA_);
					setState(537);
					match(INTEGER_);
					}
				}

				}
			}

			setState(542);
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
		enterRule(_localctx, 126, RULE_primaryKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(545);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PRIMARY) {
				{
				setState(544);
				match(PRIMARY);
				}
			}

			setState(547);
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

	public static class ExprsContext extends ParserRuleContext {
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
		public ExprsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterExprs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitExprs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitExprs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprsContext exprs() throws RecognitionException {
		ExprsContext _localctx = new ExprsContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_exprs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(549);
			expr(0);
			setState(554);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(550);
				match(COMMA_);
				setState(551);
				expr(0);
				}
				}
				setState(556);
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

	public static class ExprListContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprsContext exprs() {
			return getRuleContext(ExprsContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ExprListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterExprList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitExprList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitExprList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprListContext exprList() throws RecognitionException {
		ExprListContext _localctx = new ExprListContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_exprList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(557);
			match(LP_);
			setState(558);
			exprs();
			setState(559);
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
		int _startState = 132;
		enterRecursionRule(_localctx, 132, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(570);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(562);
				notOperator();
				setState(563);
				expr(3);
				}
				break;
			case 2:
				{
				setState(565);
				match(LP_);
				setState(566);
				expr(0);
				setState(567);
				match(RP_);
				}
				break;
			case 3:
				{
				setState(569);
				booleanPrimary(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(578);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(572);
					if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
					setState(573);
					logicalOperator();
					setState(574);
					expr(5);
					}
					} 
				}
				setState(580);
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
		enterRule(_localctx, 134, RULE_logicalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(581);
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
		enterRule(_localctx, 136, RULE_notOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(583);
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
		int _startState = 138;
		enterRecursionRule(_localctx, 138, RULE_booleanPrimary, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(586);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(608);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(606);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
					case 1:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(588);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(589);
						match(IS);
						setState(591);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(590);
							match(NOT);
							}
						}

						setState(593);
						_la = _input.LA(1);
						if ( !(((((_la - 103)) & ~0x3f) == 0 && ((1L << (_la - 103)) & ((1L << (NULL - 103)) | (1L << (TRUE - 103)) | (1L << (FALSE - 103)))) != 0) || _la==UNKNOWN) ) {
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
						setState(594);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(595);
						match(SAFE_EQ_);
						setState(596);
						predicate();
						}
						break;
					case 3:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(597);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(598);
						comparisonOperator();
						setState(599);
						predicate();
						}
						break;
					case 4:
						{
						_localctx = new BooleanPrimaryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary);
						setState(601);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(602);
						comparisonOperator();
						setState(603);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(604);
						subquery();
						}
						break;
					}
					} 
				}
				setState(610);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
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
		enterRule(_localctx, 140, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(611);
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
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode BETWEEN() { return getToken(BaseRuleParser.BETWEEN, 0); }
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
		enterRule(_localctx, 142, RULE_predicate);
		int _la;
		try {
			setState(674);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(613);
				bitExpr(0);
				setState(615);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(614);
					match(NOT);
					}
				}

				setState(617);
				match(IN);
				setState(618);
				subquery();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(620);
				bitExpr(0);
				setState(622);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(621);
					match(NOT);
					}
				}

				setState(624);
				match(IN);
				setState(625);
				match(LP_);
				setState(626);
				expr(0);
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(627);
					match(COMMA_);
					setState(628);
					expr(0);
					}
					}
					setState(633);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(634);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(636);
				bitExpr(0);
				setState(638);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(637);
					match(NOT);
					}
				}

				setState(640);
				match(IN);
				setState(641);
				match(LP_);
				setState(642);
				expr(0);
				setState(647);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(643);
					match(COMMA_);
					setState(644);
					expr(0);
					}
					}
					setState(649);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(650);
				match(RP_);
				setState(651);
				match(AND);
				setState(652);
				predicate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(654);
				bitExpr(0);
				setState(656);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(655);
					match(NOT);
					}
				}

				setState(658);
				match(BETWEEN);
				setState(659);
				bitExpr(0);
				setState(660);
				match(AND);
				setState(661);
				predicate();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(663);
				bitExpr(0);
				setState(665);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(664);
					match(NOT);
					}
				}

				setState(667);
				match(LIKE);
				setState(668);
				simpleExpr(0);
				setState(671);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
				case 1:
					{
					setState(669);
					match(ESCAPE);
					setState(670);
					simpleExpr(0);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(673);
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
		int _startState = 144;
		enterRecursionRule(_localctx, 144, RULE_bitExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(677);
			simpleExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(711);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(709);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
					case 1:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(679);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(680);
						match(VERTICAL_BAR_);
						setState(681);
						bitExpr(12);
						}
						break;
					case 2:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(682);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(683);
						match(AMPERSAND_);
						setState(684);
						bitExpr(11);
						}
						break;
					case 3:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(685);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(686);
						match(SIGNED_LEFT_SHIFT_);
						setState(687);
						bitExpr(10);
						}
						break;
					case 4:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(688);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(689);
						match(SIGNED_RIGHT_SHIFT_);
						setState(690);
						bitExpr(9);
						}
						break;
					case 5:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(691);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(692);
						match(PLUS_);
						setState(693);
						bitExpr(8);
						}
						break;
					case 6:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(694);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(695);
						match(MINUS_);
						setState(696);
						bitExpr(7);
						}
						break;
					case 7:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(697);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(698);
						match(ASTERISK_);
						setState(699);
						bitExpr(6);
						}
						break;
					case 8:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(700);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(701);
						match(SLASH_);
						setState(702);
						bitExpr(5);
						}
						break;
					case 9:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(703);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(704);
						match(MOD_);
						setState(705);
						bitExpr(4);
						}
						break;
					case 10:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(706);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(707);
						match(CARET_);
						setState(708);
						bitExpr(3);
						}
						break;
					}
					} 
				}
				setState(713);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
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
		int _startState = 146;
		enterRecursionRule(_localctx, 146, RULE_simpleExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(746);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				{
				setState(715);
				functionCall();
				}
				break;
			case 2:
				{
				setState(716);
				parameterMarker();
				}
				break;
			case 3:
				{
				setState(717);
				literals();
				}
				break;
			case 4:
				{
				setState(718);
				columnName();
				}
				break;
			case 5:
				{
				setState(719);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_))) != 0) || _la==BINARY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(720);
				simpleExpr(6);
				}
				break;
			case 6:
				{
				setState(722);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(721);
					match(ROW);
					}
				}

				setState(724);
				match(LP_);
				setState(725);
				expr(0);
				setState(730);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(726);
					match(COMMA_);
					setState(727);
					expr(0);
					}
					}
					setState(732);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(733);
				match(RP_);
				}
				break;
			case 7:
				{
				setState(736);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXISTS) {
					{
					setState(735);
					match(EXISTS);
					}
				}

				setState(738);
				subquery();
				}
				break;
			case 8:
				{
				setState(739);
				match(LBE_);
				setState(740);
				identifier();
				setState(741);
				expr(0);
				setState(742);
				match(RBE_);
				}
				break;
			case 9:
				{
				setState(744);
				caseExpression();
				}
				break;
			case 10:
				{
				setState(745);
				privateExprOfDb();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(753);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new SimpleExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
					setState(748);
					if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
					setState(749);
					match(OR_);
					setState(750);
					simpleExpr(8);
					}
					} 
				}
				setState(755);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
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
		enterRule(_localctx, 148, RULE_functionCall);
		try {
			setState(759);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(756);
				aggregationFunction();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(757);
				specialFunction();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(758);
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
		enterRule(_localctx, 150, RULE_aggregationFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761);
			aggregationFunctionName();
			setState(762);
			match(LP_);
			setState(764);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DISTINCT) {
				{
				setState(763);
				distinct();
				}
			}

			setState(775);
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
			case SCHEMA:
			case FUNCTION:
			case PROCEDURE:
			case CASE:
			case WHEN:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case NATURAL:
			case JOIN:
			case FULL:
			case INNER:
			case OUTER:
			case LEFT:
			case RIGHT:
			case CROSS:
			case USING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIMIT:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHAR:
			case CHARACTER:
			case ARRAY:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case PRESERVE:
			case DO:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case REAL:
			case TYPE:
			case BINARY:
			case ESCAPE:
			case MOD:
			case XOR:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case PRIVILEGES:
			case READ:
			case WRITE:
			case REFERENCES:
			case TRANSACTION:
			case ROLE:
			case VISIBLE:
			case INVISIBLE:
			case EXECUTE:
			case USE:
			case DEBUG:
			case UNDER:
			case FLASHBACK:
			case ARCHIVE:
			case REFRESH:
			case QUERY:
			case REWRITE:
			case KEEP:
			case SEQUENCE:
			case INHERIT:
			case TRANSLATE:
			case MERGE:
			case AT:
			case BITMAP:
			case CACHE:
			case NOCACHE:
			case CHECKPOINT:
			case CONSTRAINTS:
			case CYCLE:
			case NOCYCLE:
			case DBTIMEZONE:
			case ENCRYPT:
			case DECRYPT:
			case DEFERRABLE:
			case DEFERRED:
			case EDITION:
			case ELEMENT:
			case END:
			case EXCEPTIONS:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case INITIALLY:
			case INVALIDATE:
			case JAVA:
			case LEVELS:
			case MAXVALUE:
			case MINVALUE:
			case NOMAXVALUE:
			case NOMINVALUE:
			case MINING:
			case MODEL:
			case NATIONAL:
			case NEW:
			case NOORDER:
			case NORELY:
			case ONLY:
			case PROFILE:
			case REF:
			case REKEY:
			case RELY:
			case REPLACE:
			case SALT:
			case SCOPE:
			case SORT:
			case SOURCE:
			case SUBSTITUTABLE:
			case TABLESPACE:
			case TEMPORARY:
			case TRANSLATION:
			case TREAT:
			case NO:
			case UNUSED:
			case NOVALIDATE:
			case VALUE:
			case VARYING:
			case VIRTUAL:
			case ZONE:
			case ADVISOR:
			case ADMINISTER:
			case TUNING:
			case MANAGE:
			case MANAGEMENT:
			case OBJECT:
			case CONTEXT:
			case EXEMPT:
			case REDACTION:
			case POLICY:
			case DATABASE:
			case SYSTEM:
			case LINK:
			case ANALYZE:
			case DICTIONARY:
			case DIMENSION:
			case INDEXTYPE:
			case EXTERNAL:
			case JOB:
			case CLASS:
			case PROGRAM:
			case SCHEDULER:
			case LIBRARY:
			case LOGMINING:
			case MATERIALIZED:
			case CUBE:
			case MEASURE:
			case FOLDER:
			case BUILD:
			case PROCESS:
			case OPERATOR:
			case OUTLINE:
			case PLUGGABLE:
			case CONTAINER:
			case SEGMENT:
			case RESTRICTED:
			case COST:
			case BACKUP:
			case UNLIMITED:
			case BECOME:
			case CHANGE:
			case NOTIFICATION:
			case PRIVILEGE:
			case PURGE:
			case RESUMABLE:
			case SYSGUID:
			case SYSBACKUP:
			case SYSDBA:
			case SYSDG:
			case SYSKM:
			case SYSOPER:
			case DBA_RECYCLEBIN:
			case FIRST:
			case IDENTIFIER_:
			case STRING_:
			case INTEGER_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(766);
				expr(0);
				setState(771);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(767);
					match(COMMA_);
					setState(768);
					expr(0);
					}
					}
					setState(773);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(774);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(777);
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
		enterRule(_localctx, 152, RULE_aggregationFunctionName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(779);
			_la = _input.LA(1);
			if ( !(((((_la - 145)) & ~0x3f) == 0 && ((1L << (_la - 145)) & ((1L << (MAX - 145)) | (1L << (MIN - 145)) | (1L << (SUM - 145)) | (1L << (COUNT - 145)) | (1L << (AVG - 145)))) != 0)) ) {
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
		enterRule(_localctx, 154, RULE_distinct);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(781);
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
		enterRule(_localctx, 156, RULE_specialFunction);
		try {
			setState(785);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CAST:
				enterOuterAlt(_localctx, 1);
				{
				setState(783);
				castFunction();
				}
				break;
			case CHAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(784);
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
		enterRule(_localctx, 158, RULE_castFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(787);
			match(CAST);
			setState(788);
			match(LP_);
			setState(789);
			expr(0);
			setState(790);
			match(AS);
			setState(791);
			dataType();
			setState(792);
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
		enterRule(_localctx, 160, RULE_charFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(794);
			match(CHAR);
			setState(795);
			match(LP_);
			setState(796);
			expr(0);
			setState(801);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(797);
				match(COMMA_);
				setState(798);
				expr(0);
				}
				}
				setState(803);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(806);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(804);
				match(USING);
				setState(805);
				ignoredIdentifier();
				}
			}

			setState(808);
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
		enterRule(_localctx, 162, RULE_regularFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(810);
			regularFunctionName();
			setState(811);
			match(LP_);
			setState(821);
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
			case SCHEMA:
			case FUNCTION:
			case PROCEDURE:
			case CASE:
			case WHEN:
			case CAST:
			case TRIM:
			case SUBSTRING:
			case NATURAL:
			case JOIN:
			case FULL:
			case INNER:
			case OUTER:
			case LEFT:
			case RIGHT:
			case CROSS:
			case USING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIMIT:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHAR:
			case CHARACTER:
			case ARRAY:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case ENABLE:
			case DISABLE:
			case PRESERVE:
			case DO:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case REAL:
			case TYPE:
			case BINARY:
			case ESCAPE:
			case MOD:
			case XOR:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case PRIVILEGES:
			case READ:
			case WRITE:
			case REFERENCES:
			case TRANSACTION:
			case ROLE:
			case VISIBLE:
			case INVISIBLE:
			case EXECUTE:
			case USE:
			case DEBUG:
			case UNDER:
			case FLASHBACK:
			case ARCHIVE:
			case REFRESH:
			case QUERY:
			case REWRITE:
			case KEEP:
			case SEQUENCE:
			case INHERIT:
			case TRANSLATE:
			case MERGE:
			case AT:
			case BITMAP:
			case CACHE:
			case NOCACHE:
			case CHECKPOINT:
			case CONSTRAINTS:
			case CYCLE:
			case NOCYCLE:
			case DBTIMEZONE:
			case ENCRYPT:
			case DECRYPT:
			case DEFERRABLE:
			case DEFERRED:
			case EDITION:
			case ELEMENT:
			case END:
			case EXCEPTIONS:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case INITIALLY:
			case INVALIDATE:
			case JAVA:
			case LEVELS:
			case MAXVALUE:
			case MINVALUE:
			case NOMAXVALUE:
			case NOMINVALUE:
			case MINING:
			case MODEL:
			case NATIONAL:
			case NEW:
			case NOORDER:
			case NORELY:
			case ONLY:
			case PROFILE:
			case REF:
			case REKEY:
			case RELY:
			case REPLACE:
			case SALT:
			case SCOPE:
			case SORT:
			case SOURCE:
			case SUBSTITUTABLE:
			case TABLESPACE:
			case TEMPORARY:
			case TRANSLATION:
			case TREAT:
			case NO:
			case UNUSED:
			case NOVALIDATE:
			case VALUE:
			case VARYING:
			case VIRTUAL:
			case ZONE:
			case ADVISOR:
			case ADMINISTER:
			case TUNING:
			case MANAGE:
			case MANAGEMENT:
			case OBJECT:
			case CONTEXT:
			case EXEMPT:
			case REDACTION:
			case POLICY:
			case DATABASE:
			case SYSTEM:
			case LINK:
			case ANALYZE:
			case DICTIONARY:
			case DIMENSION:
			case INDEXTYPE:
			case EXTERNAL:
			case JOB:
			case CLASS:
			case PROGRAM:
			case SCHEDULER:
			case LIBRARY:
			case LOGMINING:
			case MATERIALIZED:
			case CUBE:
			case MEASURE:
			case FOLDER:
			case BUILD:
			case PROCESS:
			case OPERATOR:
			case OUTLINE:
			case PLUGGABLE:
			case CONTAINER:
			case SEGMENT:
			case RESTRICTED:
			case COST:
			case BACKUP:
			case UNLIMITED:
			case BECOME:
			case CHANGE:
			case NOTIFICATION:
			case PRIVILEGE:
			case PURGE:
			case RESUMABLE:
			case SYSGUID:
			case SYSBACKUP:
			case SYSDBA:
			case SYSDG:
			case SYSKM:
			case SYSOPER:
			case DBA_RECYCLEBIN:
			case FIRST:
			case IDENTIFIER_:
			case STRING_:
			case INTEGER_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(812);
				expr(0);
				setState(817);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(813);
					match(COMMA_);
					setState(814);
					expr(0);
					}
					}
					setState(819);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(820);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(823);
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
		enterRule(_localctx, 164, RULE_regularFunctionName);
		try {
			setState(830);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(825);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(826);
				match(IF);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(827);
				match(LOCALTIME);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(828);
				match(LOCALTIMESTAMP);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(829);
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
		enterRule(_localctx, 166, RULE_caseExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(832);
			match(CASE);
			setState(834);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(833);
				simpleExpr(0);
				}
				break;
			}
			setState(837); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(836);
					caseWhen();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(839); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(842);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(841);
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
		enterRule(_localctx, 168, RULE_caseWhen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(844);
			match(WHEN);
			setState(845);
			expr(0);
			setState(846);
			match(THEN);
			setState(847);
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
		enterRule(_localctx, 170, RULE_caseElse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(849);
			match(ELSE);
			setState(850);
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
		enterRule(_localctx, 172, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(852);
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
		public TerminalNode SIBLINGS() { return getToken(BaseRuleParser.SIBLINGS, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
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
		enterRule(_localctx, 174, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(854);
			match(ORDER);
			setState(856);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SIBLINGS) {
				{
				setState(855);
				match(SIBLINGS);
				}
			}

			setState(858);
			match(BY);
			setState(859);
			orderByItem();
			setState(864);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(860);
				match(COMMA_);
				setState(861);
				orderByItem();
				}
				}
				setState(866);
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
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode NULLS() { return getToken(BaseRuleParser.NULLS, 0); }
		public TerminalNode FIRST() { return getToken(BaseRuleParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(BaseRuleParser.LAST, 0); }
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
		enterRule(_localctx, 176, RULE_orderByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(870);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				{
				setState(867);
				columnName();
				}
				break;
			case 2:
				{
				setState(868);
				numberLiterals();
				}
				break;
			case 3:
				{
				setState(869);
				expr(0);
				}
				break;
			}
			setState(873);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(872);
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

			setState(879);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
			case 1:
				{
				setState(875);
				match(NULLS);
				setState(876);
				match(FIRST);
				}
				break;
			case 2:
				{
				setState(877);
				match(NULLS);
				setState(878);
				match(LAST);
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

	public static class AttributeNameContext extends ParserRuleContext {
		public OracleIdContext oracleId() {
			return getRuleContext(OracleIdContext.class,0);
		}
		public AttributeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAttributeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAttributeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAttributeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeNameContext attributeName() throws RecognitionException {
		AttributeNameContext _localctx = new AttributeNameContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_attributeName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(881);
			oracleId();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexTypeNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public IndexTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexTypeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndexTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndexTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndexTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexTypeNameContext indexTypeName() throws RecognitionException {
		IndexTypeNameContext _localctx = new IndexTypeNameContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_indexTypeName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(883);
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

	public static class SimpleExprsContext extends ParserRuleContext {
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public SimpleExprsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleExprs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSimpleExprs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSimpleExprs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSimpleExprs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleExprsContext simpleExprs() throws RecognitionException {
		SimpleExprsContext _localctx = new SimpleExprsContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_simpleExprs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(885);
			simpleExpr(0);
			setState(890);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(886);
				match(COMMA_);
				setState(887);
				simpleExpr(0);
				}
				}
				setState(892);
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

	public static class LobItemContext extends ParserRuleContext {
		public AttributeNameContext attributeName() {
			return getRuleContext(AttributeNameContext.class,0);
		}
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public LobItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lobItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLobItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLobItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLobItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LobItemContext lobItem() throws RecognitionException {
		LobItemContext _localctx = new LobItemContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_lobItem);
		try {
			setState(895);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(893);
				attributeName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(894);
				columnName();
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

	public static class LobItemsContext extends ParserRuleContext {
		public List<LobItemContext> lobItem() {
			return getRuleContexts(LobItemContext.class);
		}
		public LobItemContext lobItem(int i) {
			return getRuleContext(LobItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public LobItemsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lobItems; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLobItems(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLobItems(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLobItems(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LobItemsContext lobItems() throws RecognitionException {
		LobItemsContext _localctx = new LobItemsContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_lobItems);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(897);
			lobItem();
			setState(902);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(898);
				match(COMMA_);
				setState(899);
				lobItem();
				}
				}
				setState(904);
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

	public static class LobItemListContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public LobItemsContext lobItems() {
			return getRuleContext(LobItemsContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public LobItemListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lobItemList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLobItemList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLobItemList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLobItemList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LobItemListContext lobItemList() throws RecognitionException {
		LobItemListContext _localctx = new LobItemListContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_lobItemList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(905);
			match(LP_);
			setState(906);
			lobItems();
			setState(907);
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

	public static class DataTypeContext extends ParserRuleContext {
		public DataTypeNameContext dataTypeName() {
			return getRuleContext(DataTypeNameContext.class,0);
		}
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public SpecialDatatypeContext specialDatatype() {
			return getRuleContext(SpecialDatatypeContext.class,0);
		}
		public DatetimeTypeSuffixContext datetimeTypeSuffix() {
			return getRuleContext(DatetimeTypeSuffixContext.class,0);
		}
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
		enterRule(_localctx, 190, RULE_dataType);
		int _la;
		try {
			setState(920);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(909);
				dataTypeName();
				setState(911);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(910);
					dataTypeLength();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(913);
				specialDatatype();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(914);
				dataTypeName();
				setState(916);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(915);
					dataTypeLength();
					}
				}

				setState(918);
				datetimeTypeSuffix();
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

	public static class SpecialDatatypeContext extends ParserRuleContext {
		public DataTypeNameContext dataTypeName() {
			return getRuleContext(DataTypeNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode NATIONAL() { return getToken(BaseRuleParser.NATIONAL, 0); }
		public TerminalNode VARYING() { return getToken(BaseRuleParser.VARYING, 0); }
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public SpecialDatatypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialDatatype; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSpecialDatatype(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSpecialDatatype(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSpecialDatatype(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialDatatypeContext specialDatatype() throws RecognitionException {
		SpecialDatatypeContext _localctx = new SpecialDatatypeContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_specialDatatype);
		int _la;
		try {
			setState(945);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(922);
				dataTypeName();
				{
				setState(923);
				match(LP_);
				setState(924);
				match(NUMBER_);
				setState(925);
				match(CHAR);
				setState(926);
				match(RP_);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(928);
				match(NATIONAL);
				setState(929);
				dataTypeName();
				setState(931);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VARYING) {
					{
					setState(930);
					match(VARYING);
					}
				}

				setState(933);
				match(LP_);
				setState(934);
				match(NUMBER_);
				setState(935);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(937);
				dataTypeName();
				setState(939);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(938);
					match(LP_);
					}
				}

				setState(941);
				columnName();
				setState(943);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
				case 1:
					{
					setState(942);
					match(RP_);
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

	public static class DataTypeNameContext extends ParserRuleContext {
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode NCHAR() { return getToken(BaseRuleParser.NCHAR, 0); }
		public TerminalNode RAW() { return getToken(BaseRuleParser.RAW, 0); }
		public TerminalNode VARCHAR() { return getToken(BaseRuleParser.VARCHAR, 0); }
		public TerminalNode VARCHAR2() { return getToken(BaseRuleParser.VARCHAR2, 0); }
		public TerminalNode NVARCHAR2() { return getToken(BaseRuleParser.NVARCHAR2, 0); }
		public TerminalNode LONG() { return getToken(BaseRuleParser.LONG, 0); }
		public TerminalNode BLOB() { return getToken(BaseRuleParser.BLOB, 0); }
		public TerminalNode CLOB() { return getToken(BaseRuleParser.CLOB, 0); }
		public TerminalNode NCLOB() { return getToken(BaseRuleParser.NCLOB, 0); }
		public TerminalNode BINARY_FLOAT() { return getToken(BaseRuleParser.BINARY_FLOAT, 0); }
		public TerminalNode BINARY_DOUBLE() { return getToken(BaseRuleParser.BINARY_DOUBLE, 0); }
		public TerminalNode BOOLEAN() { return getToken(BaseRuleParser.BOOLEAN, 0); }
		public TerminalNode PLS_INTEGER() { return getToken(BaseRuleParser.PLS_INTEGER, 0); }
		public TerminalNode BINARY_INTEGER() { return getToken(BaseRuleParser.BINARY_INTEGER, 0); }
		public TerminalNode INTEGER() { return getToken(BaseRuleParser.INTEGER, 0); }
		public TerminalNode NUMBER() { return getToken(BaseRuleParser.NUMBER, 0); }
		public TerminalNode NATURAL() { return getToken(BaseRuleParser.NATURAL, 0); }
		public TerminalNode NATURALN() { return getToken(BaseRuleParser.NATURALN, 0); }
		public TerminalNode POSITIVE() { return getToken(BaseRuleParser.POSITIVE, 0); }
		public TerminalNode POSITIVEN() { return getToken(BaseRuleParser.POSITIVEN, 0); }
		public TerminalNode SIGNTYPE() { return getToken(BaseRuleParser.SIGNTYPE, 0); }
		public TerminalNode SIMPLE_INTEGER() { return getToken(BaseRuleParser.SIMPLE_INTEGER, 0); }
		public TerminalNode BFILE() { return getToken(BaseRuleParser.BFILE, 0); }
		public TerminalNode MLSLABEL() { return getToken(BaseRuleParser.MLSLABEL, 0); }
		public TerminalNode UROWID() { return getToken(BaseRuleParser.UROWID, 0); }
		public TerminalNode DATE() { return getToken(BaseRuleParser.DATE, 0); }
		public TerminalNode TIMESTAMP() { return getToken(BaseRuleParser.TIMESTAMP, 0); }
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode LOCAL() { return getToken(BaseRuleParser.LOCAL, 0); }
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public TerminalNode DAY() { return getToken(BaseRuleParser.DAY, 0); }
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public TerminalNode YEAR() { return getToken(BaseRuleParser.YEAR, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public TerminalNode JSON() { return getToken(BaseRuleParser.JSON, 0); }
		public TerminalNode FLOAT() { return getToken(BaseRuleParser.FLOAT, 0); }
		public TerminalNode REAL() { return getToken(BaseRuleParser.REAL, 0); }
		public TerminalNode DOUBLE() { return getToken(BaseRuleParser.DOUBLE, 0); }
		public TerminalNode PRECISION() { return getToken(BaseRuleParser.PRECISION, 0); }
		public TerminalNode INT() { return getToken(BaseRuleParser.INT, 0); }
		public TerminalNode SMALLINT() { return getToken(BaseRuleParser.SMALLINT, 0); }
		public TerminalNode DECIMAL() { return getToken(BaseRuleParser.DECIMAL, 0); }
		public TerminalNode NUMERIC() { return getToken(BaseRuleParser.NUMERIC, 0); }
		public TerminalNode DEC() { return getToken(BaseRuleParser.DEC, 0); }
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public TerminalNode XMLTYPE() { return getToken(BaseRuleParser.XMLTYPE, 0); }
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
		enterRule(_localctx, 194, RULE_dataTypeName);
		try {
			setState(1006);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(947);
				match(CHAR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(948);
				match(NCHAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(949);
				match(RAW);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(950);
				match(VARCHAR);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(951);
				match(VARCHAR2);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(952);
				match(NVARCHAR2);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(953);
				match(LONG);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(954);
				match(LONG);
				setState(955);
				match(RAW);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(956);
				match(BLOB);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(957);
				match(CLOB);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(958);
				match(NCLOB);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(959);
				match(BINARY_FLOAT);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(960);
				match(BINARY_DOUBLE);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(961);
				match(BOOLEAN);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(962);
				match(PLS_INTEGER);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(963);
				match(BINARY_INTEGER);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(964);
				match(INTEGER);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(965);
				match(NUMBER);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(966);
				match(NATURAL);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(967);
				match(NATURALN);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(968);
				match(POSITIVE);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(969);
				match(POSITIVEN);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(970);
				match(SIGNTYPE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(971);
				match(SIMPLE_INTEGER);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(972);
				match(BFILE);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(973);
				match(MLSLABEL);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(974);
				match(UROWID);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(975);
				match(DATE);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(976);
				match(TIMESTAMP);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(977);
				match(TIMESTAMP);
				setState(978);
				match(WITH);
				setState(979);
				match(TIME);
				setState(980);
				match(ZONE);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(981);
				match(TIMESTAMP);
				setState(982);
				match(WITH);
				setState(983);
				match(LOCAL);
				setState(984);
				match(TIME);
				setState(985);
				match(ZONE);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(986);
				match(INTERVAL);
				setState(987);
				match(DAY);
				setState(988);
				match(TO);
				setState(989);
				match(SECOND);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(990);
				match(INTERVAL);
				setState(991);
				match(YEAR);
				setState(992);
				match(TO);
				setState(993);
				match(MONTH);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(994);
				match(JSON);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(995);
				match(FLOAT);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(996);
				match(REAL);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(997);
				match(DOUBLE);
				setState(998);
				match(PRECISION);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(999);
				match(INT);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1000);
				match(SMALLINT);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1001);
				match(DECIMAL);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1002);
				match(NUMERIC);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1003);
				match(DEC);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1004);
				match(IDENTIFIER_);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1005);
				match(XMLTYPE);
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

	public static class DatetimeTypeSuffixContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode LOCAL() { return getToken(BaseRuleParser.LOCAL, 0); }
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public DatetimeTypeSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datetimeTypeSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDatetimeTypeSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDatetimeTypeSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDatetimeTypeSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatetimeTypeSuffixContext datetimeTypeSuffix() throws RecognitionException {
		DatetimeTypeSuffixContext _localctx = new DatetimeTypeSuffixContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_datetimeTypeSuffix);
		int _la;
		try {
			setState(1025);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1014);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(1008);
					match(WITH);
					setState(1010);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LOCAL) {
						{
						setState(1009);
						match(LOCAL);
						}
					}

					setState(1012);
					match(TIME);
					setState(1013);
					match(ZONE);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1016);
				match(TO);
				setState(1017);
				match(MONTH);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1018);
				match(TO);
				setState(1019);
				match(SECOND);
				setState(1023);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(1020);
					match(LP_);
					setState(1021);
					match(NUMBER_);
					setState(1022);
					match(RP_);
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

	public static class TreatFunctionContext extends ParserRuleContext {
		public TerminalNode TREAT() { return getToken(BaseRuleParser.TREAT, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public DataTypeNameContext dataTypeName() {
			return getRuleContext(DataTypeNameContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode REF() { return getToken(BaseRuleParser.REF, 0); }
		public TreatFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_treatFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTreatFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTreatFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTreatFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TreatFunctionContext treatFunction() throws RecognitionException {
		TreatFunctionContext _localctx = new TreatFunctionContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_treatFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1027);
			match(TREAT);
			setState(1028);
			match(LP_);
			setState(1029);
			expr(0);
			setState(1030);
			match(AS);
			setState(1032);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REF) {
				{
				setState(1031);
				match(REF);
				}
			}

			setState(1034);
			dataTypeName();
			setState(1035);
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

	public static class PrivateExprOfDbContext extends ParserRuleContext {
		public TreatFunctionContext treatFunction() {
			return getRuleContext(TreatFunctionContext.class,0);
		}
		public CaseExprContext caseExpr() {
			return getRuleContext(CaseExprContext.class,0);
		}
		public IntervalExpressionContext intervalExpression() {
			return getRuleContext(IntervalExpressionContext.class,0);
		}
		public ObjectAccessExpressionContext objectAccessExpression() {
			return getRuleContext(ObjectAccessExpressionContext.class,0);
		}
		public ConstructorExprContext constructorExpr() {
			return getRuleContext(ConstructorExprContext.class,0);
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
		enterRule(_localctx, 200, RULE_privateExprOfDb);
		try {
			setState(1042);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1037);
				treatFunction();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1038);
				caseExpr();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1039);
				intervalExpression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1040);
				objectAccessExpression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1041);
				constructorExpr();
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

	public static class CaseExprContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(BaseRuleParser.CASE, 0); }
		public TerminalNode END() { return getToken(BaseRuleParser.END, 0); }
		public SimpleCaseExprContext simpleCaseExpr() {
			return getRuleContext(SimpleCaseExprContext.class,0);
		}
		public SearchedCaseExprContext searchedCaseExpr() {
			return getRuleContext(SearchedCaseExprContext.class,0);
		}
		public ElseClauseContext elseClause() {
			return getRuleContext(ElseClauseContext.class,0);
		}
		public CaseExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCaseExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCaseExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCaseExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseExprContext caseExpr() throws RecognitionException {
		CaseExprContext _localctx = new CaseExprContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_caseExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1044);
			match(CASE);
			setState(1047);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				{
				setState(1045);
				simpleCaseExpr();
				}
				break;
			case 2:
				{
				setState(1046);
				searchedCaseExpr();
				}
				break;
			}
			setState(1050);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(1049);
				elseClause();
				}
			}

			setState(1052);
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

	public static class SimpleCaseExprContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<SearchedCaseExprContext> searchedCaseExpr() {
			return getRuleContexts(SearchedCaseExprContext.class);
		}
		public SearchedCaseExprContext searchedCaseExpr(int i) {
			return getRuleContext(SearchedCaseExprContext.class,i);
		}
		public SimpleCaseExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleCaseExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSimpleCaseExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSimpleCaseExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSimpleCaseExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleCaseExprContext simpleCaseExpr() throws RecognitionException {
		SimpleCaseExprContext _localctx = new SimpleCaseExprContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_simpleCaseExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1054);
			expr(0);
			setState(1056); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1055);
				searchedCaseExpr();
				}
				}
				setState(1058); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SearchedCaseExprContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(BaseRuleParser.WHEN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode THEN() { return getToken(BaseRuleParser.THEN, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public SearchedCaseExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchedCaseExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSearchedCaseExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSearchedCaseExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSearchedCaseExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SearchedCaseExprContext searchedCaseExpr() throws RecognitionException {
		SearchedCaseExprContext _localctx = new SearchedCaseExprContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_searchedCaseExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1060);
			match(WHEN);
			setState(1061);
			expr(0);
			setState(1062);
			match(THEN);
			setState(1063);
			simpleExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElseClauseContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(BaseRuleParser.ELSE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ElseClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elseClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterElseClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitElseClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitElseClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElseClauseContext elseClause() throws RecognitionException {
		ElseClauseContext _localctx = new ElseClauseContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_elseClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1065);
			match(ELSE);
			setState(1066);
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
		public List<TerminalNode> LP_() { return getTokens(BaseRuleParser.LP_); }
		public TerminalNode LP_(int i) {
			return getToken(BaseRuleParser.LP_, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public List<TerminalNode> RP_() { return getTokens(BaseRuleParser.RP_); }
		public TerminalNode RP_(int i) {
			return getToken(BaseRuleParser.RP_, i);
		}
		public TerminalNode DAY() { return getToken(BaseRuleParser.DAY, 0); }
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public TerminalNode YEAR() { return getToken(BaseRuleParser.YEAR, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(BaseRuleParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(BaseRuleParser.NUMBER_, i);
		}
		public IntervalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIntervalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIntervalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIntervalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalExpressionContext intervalExpression() throws RecognitionException {
		IntervalExpressionContext _localctx = new IntervalExpressionContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_intervalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1068);
			match(LP_);
			setState(1069);
			expr(0);
			setState(1070);
			match(MINUS_);
			setState(1071);
			expr(0);
			setState(1072);
			match(RP_);
			setState(1094);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DAY:
				{
				setState(1073);
				match(DAY);
				setState(1077);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(1074);
					match(LP_);
					setState(1075);
					match(NUMBER_);
					setState(1076);
					match(RP_);
					}
				}

				setState(1079);
				match(TO);
				setState(1080);
				match(SECOND);
				setState(1084);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(1081);
					match(LP_);
					setState(1082);
					match(NUMBER_);
					setState(1083);
					match(RP_);
					}
					break;
				}
				}
				break;
			case YEAR:
				{
				setState(1086);
				match(YEAR);
				setState(1090);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(1087);
					match(LP_);
					setState(1088);
					match(NUMBER_);
					setState(1089);
					match(RP_);
					}
				}

				setState(1092);
				match(TO);
				setState(1093);
				match(MONTH);
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

	public static class ObjectAccessExpressionContext extends ParserRuleContext {
		public List<TerminalNode> DOT_() { return getTokens(BaseRuleParser.DOT_); }
		public TerminalNode DOT_(int i) {
			return getToken(BaseRuleParser.DOT_, i);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TreatFunctionContext treatFunction() {
			return getRuleContext(TreatFunctionContext.class,0);
		}
		public List<AttributeNameContext> attributeName() {
			return getRuleContexts(AttributeNameContext.class);
		}
		public AttributeNameContext attributeName(int i) {
			return getRuleContext(AttributeNameContext.class,i);
		}
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public ObjectAccessExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectAccessExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterObjectAccessExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitObjectAccessExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitObjectAccessExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectAccessExpressionContext objectAccessExpression() throws RecognitionException {
		ObjectAccessExpressionContext _localctx = new ObjectAccessExpressionContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_objectAccessExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1101);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LP_:
				{
				setState(1096);
				match(LP_);
				setState(1097);
				simpleExpr(0);
				setState(1098);
				match(RP_);
				}
				break;
			case TREAT:
				{
				setState(1100);
				treatFunction();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1103);
			match(DOT_);
			setState(1117);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				{
				setState(1104);
				attributeName();
				setState(1109);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1105);
						match(DOT_);
						setState(1106);
						attributeName();
						}
						} 
					}
					setState(1111);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
				}
				setState(1114);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(1112);
					match(DOT_);
					setState(1113);
					functionCall();
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(1116);
				functionCall();
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

	public static class ConstructorExprContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(BaseRuleParser.NEW, 0); }
		public DataTypeNameContext dataTypeName() {
			return getRuleContext(DataTypeNameContext.class,0);
		}
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public ConstructorExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConstructorExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConstructorExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConstructorExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstructorExprContext constructorExpr() throws RecognitionException {
		ConstructorExprContext _localctx = new ConstructorExprContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_constructorExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1119);
			match(NEW);
			setState(1120);
			dataTypeName();
			setState(1121);
			exprList();
			}
		}
		catch (RecognitionException re) {
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
		enterRule(_localctx, 216, RULE_ignoredIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1123);
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
		enterRule(_localctx, 218, RULE_ignoredIdentifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1125);
			ignoredIdentifier();
			setState(1130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1126);
				match(COMMA_);
				setState(1127);
				ignoredIdentifier();
				}
				}
				setState(1132);
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
		enterRule(_localctx, 220, RULE_matchNone);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1133);
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

	public static class HashSubpartitionQuantityContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(BaseRuleParser.NUMBER, 0); }
		public HashSubpartitionQuantityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hashSubpartitionQuantity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterHashSubpartitionQuantity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitHashSubpartitionQuantity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitHashSubpartitionQuantity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HashSubpartitionQuantityContext hashSubpartitionQuantity() throws RecognitionException {
		HashSubpartitionQuantityContext _localctx = new HashSubpartitionQuantityContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_hashSubpartitionQuantity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1135);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OdciParametersContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public OdciParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_odciParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOdciParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOdciParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOdciParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OdciParametersContext odciParameters() throws RecognitionException {
		OdciParametersContext _localctx = new OdciParametersContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_odciParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1137);
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

	public static class DatabaseNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DatabaseNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_databaseName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDatabaseName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDatabaseName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDatabaseName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatabaseNameContext databaseName() throws RecognitionException {
		DatabaseNameContext _localctx = new DatabaseNameContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_databaseName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1139);
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

	public static class LocationNameContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public LocationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_locationName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLocationName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLocationName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLocationName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocationNameContext locationName() throws RecognitionException {
		LocationNameContext _localctx = new LocationNameContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_locationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1141);
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

	public static class FileNameContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public FileNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fileName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFileName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFileName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFileName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileNameContext fileName() throws RecognitionException {
		FileNameContext _localctx = new FileNameContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_fileName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1143);
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

	public static class AsmFileNameContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public AsmFileNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmFileName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAsmFileName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAsmFileName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAsmFileName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmFileNameContext asmFileName() throws RecognitionException {
		AsmFileNameContext _localctx = new AsmFileNameContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_asmFileName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1145);
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

	public static class FileNumberContext extends ParserRuleContext {
		public TerminalNode INTEGER_() { return getToken(BaseRuleParser.INTEGER_, 0); }
		public FileNumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fileNumber; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFileNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFileNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFileNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileNumberContext fileNumber() throws RecognitionException {
		FileNumberContext _localctx = new FileNumberContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_fileNumber);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1147);
			match(INTEGER_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InstanceNameContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public InstanceNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instanceName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterInstanceName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitInstanceName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitInstanceName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InstanceNameContext instanceName() throws RecognitionException {
		InstanceNameContext _localctx = new InstanceNameContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_instanceName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1149);
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

	public static class LogminerSessionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LogminerSessionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logminerSessionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLogminerSessionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLogminerSessionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLogminerSessionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogminerSessionNameContext logminerSessionName() throws RecognitionException {
		LogminerSessionNameContext _localctx = new LogminerSessionNameContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_logminerSessionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1151);
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

	public static class TablespaceGroupNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TablespaceGroupNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablespaceGroupName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTablespaceGroupName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTablespaceGroupName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTablespaceGroupName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablespaceGroupNameContext tablespaceGroupName() throws RecognitionException {
		TablespaceGroupNameContext _localctx = new TablespaceGroupNameContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_tablespaceGroupName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1153);
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

	public static class CopyNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public CopyNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_copyName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCopyName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCopyName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCopyName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CopyNameContext copyName() throws RecognitionException {
		CopyNameContext _localctx = new CopyNameContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_copyName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1155);
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

	public static class MirrorNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MirrorNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mirrorName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterMirrorName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitMirrorName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitMirrorName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MirrorNameContext mirrorName() throws RecognitionException {
		MirrorNameContext _localctx = new MirrorNameContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_mirrorName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1157);
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

	public static class UriStringContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public UriStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uriString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterUriString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitUriString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitUriString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UriStringContext uriString() throws RecognitionException {
		UriStringContext _localctx = new UriStringContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_uriString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1159);
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

	public static class QualifiedCredentialNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public QualifiedCredentialNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedCredentialName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterQualifiedCredentialName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitQualifiedCredentialName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitQualifiedCredentialName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedCredentialNameContext qualifiedCredentialName() throws RecognitionException {
		QualifiedCredentialNameContext _localctx = new QualifiedCredentialNameContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_qualifiedCredentialName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1161);
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

	public static class PdbNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public PdbNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pdbName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPdbName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPdbName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPdbName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PdbNameContext pdbName() throws RecognitionException {
		PdbNameContext _localctx = new PdbNameContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_pdbName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1163);
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

	public static class DiskgroupNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DiskgroupNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_diskgroupName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDiskgroupName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDiskgroupName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDiskgroupName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DiskgroupNameContext diskgroupName() throws RecognitionException {
		DiskgroupNameContext _localctx = new DiskgroupNameContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_diskgroupName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1165);
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

	public static class TemplateNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TemplateNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_templateName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTemplateName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTemplateName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTemplateName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TemplateNameContext templateName() throws RecognitionException {
		TemplateNameContext _localctx = new TemplateNameContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_templateName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1167);
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

	public static class AliasNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public AliasNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aliasName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAliasName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAliasName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAliasName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasNameContext aliasName() throws RecognitionException {
		AliasNameContext _localctx = new AliasNameContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_aliasName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1169);
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

	public static class DomainContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DomainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_domain; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDomain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDomain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDomain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DomainContext domain() throws RecognitionException {
		DomainContext _localctx = new DomainContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_domain);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1171);
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

	public static class DateValueContext extends ParserRuleContext {
		public DateTimeLiteralsContext dateTimeLiterals() {
			return getRuleContext(DateTimeLiteralsContext.class,0);
		}
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public DateValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDateValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDateValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDateValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateValueContext dateValue() throws RecognitionException {
		DateValueContext _localctx = new DateValueContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_dateValue);
		try {
			setState(1177);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1173);
				dateTimeLiterals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1174);
				stringLiterals();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1175);
				numberLiterals();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1176);
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

	public static class SessionIdContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public SessionIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sessionId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSessionId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSessionId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSessionId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SessionIdContext sessionId() throws RecognitionException {
		SessionIdContext _localctx = new SessionIdContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_sessionId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1179);
			numberLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SerialNumberContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public SerialNumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_serialNumber; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSerialNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSerialNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSerialNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SerialNumberContext serialNumber() throws RecognitionException {
		SerialNumberContext _localctx = new SerialNumberContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_serialNumber);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1181);
			numberLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InstanceIdContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public InstanceIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instanceId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterInstanceId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitInstanceId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitInstanceId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InstanceIdContext instanceId() throws RecognitionException {
		InstanceIdContext _localctx = new InstanceIdContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_instanceId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1183);
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

	public static class SqlIdContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SqlIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sqlId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSqlId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSqlId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSqlId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SqlIdContext sqlId() throws RecognitionException {
		SqlIdContext _localctx = new SqlIdContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_sqlId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1185);
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

	public static class LogFileNameContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public LogFileNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logFileName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLogFileName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLogFileName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLogFileName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogFileNameContext logFileName() throws RecognitionException {
		LogFileNameContext _localctx = new LogFileNameContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_logFileName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1187);
			stringLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LogFileGroupsArchivedLocationNameContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public LogFileGroupsArchivedLocationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logFileGroupsArchivedLocationName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLogFileGroupsArchivedLocationName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLogFileGroupsArchivedLocationName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLogFileGroupsArchivedLocationName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogFileGroupsArchivedLocationNameContext logFileGroupsArchivedLocationName() throws RecognitionException {
		LogFileGroupsArchivedLocationNameContext _localctx = new LogFileGroupsArchivedLocationNameContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_logFileGroupsArchivedLocationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1189);
			stringLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AsmVersionContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public AsmVersionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmVersion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAsmVersion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAsmVersion(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAsmVersion(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmVersionContext asmVersion() throws RecognitionException {
		AsmVersionContext _localctx = new AsmVersionContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_asmVersion);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1191);
			stringLiterals();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WalletPasswordContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public WalletPasswordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_walletPassword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWalletPassword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWalletPassword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWalletPassword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WalletPasswordContext walletPassword() throws RecognitionException {
		WalletPasswordContext _localctx = new WalletPasswordContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_walletPassword);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1193);
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

	public static class HsmAuthStringContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public HsmAuthStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hsmAuthString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterHsmAuthString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitHsmAuthString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitHsmAuthString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HsmAuthStringContext hsmAuthString() throws RecognitionException {
		HsmAuthStringContext _localctx = new HsmAuthStringContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_hsmAuthString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1195);
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

	public static class TargetDbNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TargetDbNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_targetDbName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTargetDbName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTargetDbName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTargetDbName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TargetDbNameContext targetDbName() throws RecognitionException {
		TargetDbNameContext _localctx = new TargetDbNameContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_targetDbName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1197);
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

	public static class CertificateIdContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public CertificateIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_certificateId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCertificateId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCertificateId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCertificateId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CertificateIdContext certificateId() throws RecognitionException {
		CertificateIdContext _localctx = new CertificateIdContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_certificateId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1199);
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

	public static class CategoryNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public CategoryNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_categoryName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCategoryName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCategoryName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCategoryName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CategoryNameContext categoryName() throws RecognitionException {
		CategoryNameContext _localctx = new CategoryNameContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_categoryName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1201);
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

	public static class OffsetContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NullValueLiteralsContext nullValueLiterals() {
			return getRuleContext(NullValueLiteralsContext.class,0);
		}
		public OffsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOffset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOffset(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOffset(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OffsetContext offset() throws RecognitionException {
		OffsetContext _localctx = new OffsetContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_offset);
		try {
			setState(1206);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1203);
				numberLiterals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1204);
				expr(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1205);
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

	public static class RowcountContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NullValueLiteralsContext nullValueLiterals() {
			return getRuleContext(NullValueLiteralsContext.class,0);
		}
		public RowcountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rowcount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRowcount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRowcount(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRowcount(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowcountContext rowcount() throws RecognitionException {
		RowcountContext _localctx = new RowcountContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_rowcount);
		try {
			setState(1211);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1208);
				numberLiterals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1209);
				expr(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1210);
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

	public static class PercentContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NullValueLiteralsContext nullValueLiterals() {
			return getRuleContext(NullValueLiteralsContext.class,0);
		}
		public PercentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_percent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPercent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPercent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPercent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PercentContext percent() throws RecognitionException {
		PercentContext _localctx = new PercentContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_percent);
		try {
			setState(1216);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1213);
				numberLiterals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1214);
				expr(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1215);
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

	public static class RollbackSegmentContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RollbackSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rollbackSegment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRollbackSegment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRollbackSegment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRollbackSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RollbackSegmentContext rollbackSegment() throws RecognitionException {
		RollbackSegmentContext _localctx = new RollbackSegmentContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_rollbackSegment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1218);
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

	public static class QueryNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public QueryNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterQueryName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitQueryName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitQueryName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryNameContext queryName() throws RecognitionException {
		QueryNameContext _localctx = new QueryNameContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_queryName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1223);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(1220);
				owner();
				setState(1221);
				match(DOT_);
				}
				break;
			}
			setState(1225);
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

	public static class CycleValueContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public CycleValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cycleValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCycleValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCycleValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCycleValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CycleValueContext cycleValue() throws RecognitionException {
		CycleValueContext _localctx = new CycleValueContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_cycleValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1227);
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

	public static class NoCycleValueContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public NoCycleValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_noCycleValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNoCycleValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNoCycleValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNoCycleValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NoCycleValueContext noCycleValue() throws RecognitionException {
		NoCycleValueContext _localctx = new NoCycleValueContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_noCycleValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1229);
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

	public static class OrderingColumnContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public OrderingColumnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderingColumn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOrderingColumn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOrderingColumn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOrderingColumn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderingColumnContext orderingColumn() throws RecognitionException {
		OrderingColumnContext _localctx = new OrderingColumnContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_orderingColumn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1231);
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

	public static class SubavNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public SubavNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subavName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubavName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubavName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubavName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubavNameContext subavName() throws RecognitionException {
		SubavNameContext _localctx = new SubavNameContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_subavName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1236);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(1233);
				owner();
				setState(1234);
				match(DOT_);
				}
				break;
			}
			setState(1238);
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

	public static class BaseAvNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public BaseAvNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_baseAvName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBaseAvName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBaseAvName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBaseAvName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BaseAvNameContext baseAvName() throws RecognitionException {
		BaseAvNameContext _localctx = new BaseAvNameContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_baseAvName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1243);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				{
				setState(1240);
				owner();
				setState(1241);
				match(DOT_);
				}
				break;
			}
			setState(1245);
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

	public static class MeasNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MeasNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_measName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterMeasName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitMeasName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitMeasName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MeasNameContext measName() throws RecognitionException {
		MeasNameContext _localctx = new MeasNameContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_measName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1247);
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

	public static class LevelRefContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LevelRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_levelRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLevelRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLevelRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLevelRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LevelRefContext levelRef() throws RecognitionException {
		LevelRefContext _localctx = new LevelRefContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_levelRef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1249);
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

	public static class OffsetExprContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public OffsetExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offsetExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOffsetExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOffsetExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOffsetExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OffsetExprContext offsetExpr() throws RecognitionException {
		OffsetExprContext _localctx = new OffsetExprContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_offsetExpr);
		try {
			setState(1253);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1251);
				expr(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1252);
				numberLiterals();
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

	public static class MemberKeyExprContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MemberKeyExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberKeyExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterMemberKeyExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitMemberKeyExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitMemberKeyExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberKeyExprContext memberKeyExpr() throws RecognitionException {
		MemberKeyExprContext _localctx = new MemberKeyExprContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_memberKeyExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1255);
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

	public static class DepthExpressionContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DepthExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_depthExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDepthExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDepthExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDepthExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DepthExpressionContext depthExpression() throws RecognitionException {
		DepthExpressionContext _localctx = new DepthExpressionContext(_ctx, getState());
		enterRule(_localctx, 314, RULE_depthExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1257);
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

	public static class UnitNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public OwnerContext owner() {
			return getRuleContext(OwnerContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public UnitNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unitName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterUnitName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitUnitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitUnitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnitNameContext unitName() throws RecognitionException {
		UnitNameContext _localctx = new UnitNameContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_unitName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1262);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
			case 1:
				{
				setState(1259);
				owner();
				setState(1260);
				match(DOT_);
				}
				break;
			}
			setState(1264);
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

	public static class ProcedureNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ProcedureNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_procedureName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterProcedureName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitProcedureName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitProcedureName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProcedureNameContext procedureName() throws RecognitionException {
		ProcedureNameContext _localctx = new ProcedureNameContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_procedureName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1266);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 66:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 69:
			return booleanPrimary_sempred((BooleanPrimaryContext)_localctx, predIndex);
		case 72:
			return bitExpr_sempred((BitExprContext)_localctx, predIndex);
		case 73:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u02dd\u04f7\4\2\t"+
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
		"\t\u0085\4\u0086\t\u0086\4\u0087\t\u0087\4\u0088\t\u0088\4\u0089\t\u0089"+
		"\4\u008a\t\u008a\4\u008b\t\u008b\4\u008c\t\u008c\4\u008d\t\u008d\4\u008e"+
		"\t\u008e\4\u008f\t\u008f\4\u0090\t\u0090\4\u0091\t\u0091\4\u0092\t\u0092"+
		"\4\u0093\t\u0093\4\u0094\t\u0094\4\u0095\t\u0095\4\u0096\t\u0096\4\u0097"+
		"\t\u0097\4\u0098\t\u0098\4\u0099\t\u0099\4\u009a\t\u009a\4\u009b\t\u009b"+
		"\4\u009c\t\u009c\4\u009d\t\u009d\4\u009e\t\u009e\4\u009f\t\u009f\4\u00a0"+
		"\t\u00a0\4\u00a1\t\u00a1\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\u014c"+
		"\n\3\3\4\3\4\3\5\5\5\u0151\n\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6"+
		"\u015c\n\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\5\13\u0168\n\13\3"+
		"\f\3\f\3\r\3\r\3\16\3\16\3\16\5\16\u0171\n\16\3\16\3\16\3\17\3\17\3\17"+
		"\5\17\u0178\n\17\3\17\3\17\3\20\3\20\3\20\5\20\u017f\n\20\3\20\3\20\3"+
		"\21\3\21\3\21\5\21\u0186\n\21\3\21\3\21\3\22\3\22\3\22\5\22\u018d\n\22"+
		"\3\22\3\22\3\23\3\23\3\23\5\23\u0194\n\23\3\23\3\23\3\24\3\24\3\25\3\25"+
		"\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34"+
		"\3\35\3\35\3\36\3\36\3\37\3\37\5\37\u01b0\n\37\3 \3 \3!\3!\3\"\3\"\3#"+
		"\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3"+
		".\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\5\63\u01da\n\63\3\64\3"+
		"\64\5\64\u01de\n\64\3\65\3\65\3\66\3\66\3\67\3\67\38\38\39\39\3:\5:\u01eb"+
		"\n:\3:\3:\3:\7:\u01f0\n:\f:\16:\u01f3\13:\3:\5:\u01f6\n:\3;\5;\u01f9\n"+
		";\3;\3;\3;\7;\u01fe\n;\f;\16;\u0201\13;\3;\5;\u0204\n;\3<\3<\3<\7<\u0209"+
		"\n<\f<\16<\u020c\13<\3<\5<\u020f\n<\3=\3=\3>\3>\3?\3?\5?\u0217\n?\3@\3"+
		"@\3@\3@\5@\u021d\n@\5@\u021f\n@\3@\3@\3A\5A\u0224\nA\3A\3A\3B\3B\3B\7"+
		"B\u022b\nB\fB\16B\u022e\13B\3C\3C\3C\3C\3D\3D\3D\3D\3D\3D\3D\3D\3D\5D"+
		"\u023d\nD\3D\3D\3D\3D\7D\u0243\nD\fD\16D\u0246\13D\3E\3E\3F\3F\3G\3G\3"+
		"G\3G\3G\3G\5G\u0252\nG\3G\3G\3G\3G\3G\3G\3G\3G\3G\3G\3G\3G\3G\7G\u0261"+
		"\nG\fG\16G\u0264\13G\3H\3H\3I\3I\5I\u026a\nI\3I\3I\3I\3I\3I\5I\u0271\n"+
		"I\3I\3I\3I\3I\3I\7I\u0278\nI\fI\16I\u027b\13I\3I\3I\3I\3I\5I\u0281\nI"+
		"\3I\3I\3I\3I\3I\7I\u0288\nI\fI\16I\u028b\13I\3I\3I\3I\3I\3I\3I\5I\u0293"+
		"\nI\3I\3I\3I\3I\3I\3I\3I\5I\u029c\nI\3I\3I\3I\3I\5I\u02a2\nI\3I\5I\u02a5"+
		"\nI\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J"+
		"\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\7J\u02c8\nJ\fJ\16J\u02cb\13J\3K\3K\3"+
		"K\3K\3K\3K\3K\3K\5K\u02d5\nK\3K\3K\3K\3K\7K\u02db\nK\fK\16K\u02de\13K"+
		"\3K\3K\3K\5K\u02e3\nK\3K\3K\3K\3K\3K\3K\3K\3K\5K\u02ed\nK\3K\3K\3K\7K"+
		"\u02f2\nK\fK\16K\u02f5\13K\3L\3L\3L\5L\u02fa\nL\3M\3M\3M\5M\u02ff\nM\3"+
		"M\3M\3M\7M\u0304\nM\fM\16M\u0307\13M\3M\5M\u030a\nM\3M\3M\3N\3N\3O\3O"+
		"\3P\3P\5P\u0314\nP\3Q\3Q\3Q\3Q\3Q\3Q\3Q\3R\3R\3R\3R\3R\7R\u0322\nR\fR"+
		"\16R\u0325\13R\3R\3R\5R\u0329\nR\3R\3R\3S\3S\3S\3S\3S\7S\u0332\nS\fS\16"+
		"S\u0335\13S\3S\5S\u0338\nS\3S\3S\3T\3T\3T\3T\3T\5T\u0341\nT\3U\3U\5U\u0345"+
		"\nU\3U\6U\u0348\nU\rU\16U\u0349\3U\5U\u034d\nU\3V\3V\3V\3V\3V\3W\3W\3"+
		"W\3X\3X\3Y\3Y\5Y\u035b\nY\3Y\3Y\3Y\3Y\7Y\u0361\nY\fY\16Y\u0364\13Y\3Z"+
		"\3Z\3Z\5Z\u0369\nZ\3Z\5Z\u036c\nZ\3Z\3Z\3Z\3Z\5Z\u0372\nZ\3[\3[\3\\\3"+
		"\\\3]\3]\3]\7]\u037b\n]\f]\16]\u037e\13]\3^\3^\5^\u0382\n^\3_\3_\3_\7"+
		"_\u0387\n_\f_\16_\u038a\13_\3`\3`\3`\3`\3a\3a\5a\u0392\na\3a\3a\3a\5a"+
		"\u0397\na\3a\3a\5a\u039b\na\3b\3b\3b\3b\3b\3b\3b\3b\3b\5b\u03a6\nb\3b"+
		"\3b\3b\3b\3b\3b\5b\u03ae\nb\3b\3b\5b\u03b2\nb\5b\u03b4\nb\3c\3c\3c\3c"+
		"\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c"+
		"\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c\3c"+
		"\3c\3c\3c\3c\3c\3c\3c\3c\3c\5c\u03f1\nc\3d\3d\5d\u03f5\nd\3d\3d\5d\u03f9"+
		"\nd\3d\3d\3d\3d\3d\3d\3d\5d\u0402\nd\5d\u0404\nd\3e\3e\3e\3e\3e\5e\u040b"+
		"\ne\3e\3e\3e\3f\3f\3f\3f\3f\5f\u0415\nf\3g\3g\3g\5g\u041a\ng\3g\5g\u041d"+
		"\ng\3g\3g\3h\3h\6h\u0423\nh\rh\16h\u0424\3i\3i\3i\3i\3i\3j\3j\3j\3k\3"+
		"k\3k\3k\3k\3k\3k\3k\3k\5k\u0438\nk\3k\3k\3k\3k\3k\5k\u043f\nk\3k\3k\3"+
		"k\3k\5k\u0445\nk\3k\3k\5k\u0449\nk\3l\3l\3l\3l\3l\5l\u0450\nl\3l\3l\3"+
		"l\3l\7l\u0456\nl\fl\16l\u0459\13l\3l\3l\5l\u045d\nl\3l\5l\u0460\nl\3m"+
		"\3m\3m\3m\3n\3n\3o\3o\3o\7o\u046b\no\fo\16o\u046e\13o\3p\3p\3q\3q\3r\3"+
		"r\3s\3s\3t\3t\3u\3u\3v\3v\3w\3w\3x\3x\3y\3y\3z\3z\3{\3{\3|\3|\3}\3}\3"+
		"~\3~\3\177\3\177\3\u0080\3\u0080\3\u0081\3\u0081\3\u0082\3\u0082\3\u0083"+
		"\3\u0083\3\u0084\3\u0084\3\u0084\3\u0084\5\u0084\u049c\n\u0084\3\u0085"+
		"\3\u0085\3\u0086\3\u0086\3\u0087\3\u0087\3\u0088\3\u0088\3\u0089\3\u0089"+
		"\3\u008a\3\u008a\3\u008b\3\u008b\3\u008c\3\u008c\3\u008d\3\u008d\3\u008e"+
		"\3\u008e\3\u008f\3\u008f\3\u0090\3\u0090\3\u0091\3\u0091\3\u0091\5\u0091"+
		"\u04b9\n\u0091\3\u0092\3\u0092\3\u0092\5\u0092\u04be\n\u0092\3\u0093\3"+
		"\u0093\3\u0093\5\u0093\u04c3\n\u0093\3\u0094\3\u0094\3\u0095\3\u0095\3"+
		"\u0095\5\u0095\u04ca\n\u0095\3\u0095\3\u0095\3\u0096\3\u0096\3\u0097\3"+
		"\u0097\3\u0098\3\u0098\3\u0099\3\u0099\3\u0099\5\u0099\u04d7\n\u0099\3"+
		"\u0099\3\u0099\3\u009a\3\u009a\3\u009a\5\u009a\u04de\n\u009a\3\u009a\3"+
		"\u009a\3\u009b\3\u009b\3\u009c\3\u009c\3\u009d\3\u009d\5\u009d\u04e8\n"+
		"\u009d\3\u009e\3\u009e\3\u009f\3\u009f\3\u00a0\3\u00a0\3\u00a0\5\u00a0"+
		"\u04f1\n\u00a0\3\u00a0\3\u00a0\3\u00a1\3\u00a1\3\u00a1\2\6\u0086\u008c"+
		"\u0092\u0094\u00a2\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60"+
		"\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086"+
		"\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e"+
		"\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6"+
		"\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce"+
		"\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6"+
		"\u00e8\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe"+
		"\u0100\u0102\u0104\u0106\u0108\u010a\u010c\u010e\u0110\u0112\u0114\u0116"+
		"\u0118\u011a\u011c\u011e\u0120\u0122\u0124\u0126\u0128\u012a\u012c\u012e"+
		"\u0130\u0132\u0134\u0136\u0138\u013a\u013c\u013e\u0140\2\20\3\2\17\20"+
		"\3\2\u02da\u02db\4\2\u0084\u0085\u0087\u0087\3\2jk\'\2\65\66EEGGNRT\\"+
		"``jkx\177\u0081\u0083\u0085\u0085\u0087\u0097\u009a\u009b\u009e\u00aa"+
		"\u00ac\u00ac\u00ae\u00ae\u00b6\u00b9\u00bc\u00be\u00c0\u00c4\u00c6\u00c6"+
		"\u00c8\u00dd\u00df\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0\u00f0\u00f3\u00fa"+
		"\u00fc\u00fd\u00ff\u0102\u0104\u0104\u0106\u0109\u010b\u010b\u010f\u0119"+
		"\u011b\u011f\u0124\u0129\u012b\u0130\u0132\u014a\u014c\u0150\u0152\u015c"+
		"\3\2\u02d8\u02d9\4\2\4\5ef\4\2\6\6hh\4\2ik\u00bc\u00bc\3\2op\3\2\30\35"+
		"\5\2\6\7\17\20\u00b6\u00b6\3\2\u0093\u0097\3\2uv\2\u0516\2\u0142\3\2\2"+
		"\2\4\u014b\3\2\2\2\6\u014d\3\2\2\2\b\u0150\3\2\2\2\n\u015b\3\2\2\2\f\u015d"+
		"\3\2\2\2\16\u015f\3\2\2\2\20\u0161\3\2\2\2\22\u0163\3\2\2\2\24\u0167\3"+
		"\2\2\2\26\u0169\3\2\2\2\30\u016b\3\2\2\2\32\u0170\3\2\2\2\34\u0177\3\2"+
		"\2\2\36\u017e\3\2\2\2 \u0185\3\2\2\2\"\u018c\3\2\2\2$\u0193\3\2\2\2&\u0197"+
		"\3\2\2\2(\u0199\3\2\2\2*\u019b\3\2\2\2,\u019d\3\2\2\2.\u019f\3\2\2\2\60"+
		"\u01a1\3\2\2\2\62\u01a3\3\2\2\2\64\u01a5\3\2\2\2\66\u01a7\3\2\2\28\u01a9"+
		"\3\2\2\2:\u01ab\3\2\2\2<\u01af\3\2\2\2>\u01b1\3\2\2\2@\u01b3\3\2\2\2B"+
		"\u01b5\3\2\2\2D\u01b7\3\2\2\2F\u01b9\3\2\2\2H\u01bb\3\2\2\2J\u01bd\3\2"+
		"\2\2L\u01bf\3\2\2\2N\u01c1\3\2\2\2P\u01c3\3\2\2\2R\u01c5\3\2\2\2T\u01c7"+
		"\3\2\2\2V\u01c9\3\2\2\2X\u01cb\3\2\2\2Z\u01cd\3\2\2\2\\\u01cf\3\2\2\2"+
		"^\u01d1\3\2\2\2`\u01d3\3\2\2\2b\u01d5\3\2\2\2d\u01d9\3\2\2\2f\u01dd\3"+
		"\2\2\2h\u01df\3\2\2\2j\u01e1\3\2\2\2l\u01e3\3\2\2\2n\u01e5\3\2\2\2p\u01e7"+
		"\3\2\2\2r\u01ea\3\2\2\2t\u01f8\3\2\2\2v\u020e\3\2\2\2x\u0210\3\2\2\2z"+
		"\u0212\3\2\2\2|\u0216\3\2\2\2~\u0218\3\2\2\2\u0080\u0223\3\2\2\2\u0082"+
		"\u0227\3\2\2\2\u0084\u022f\3\2\2\2\u0086\u023c\3\2\2\2\u0088\u0247\3\2"+
		"\2\2\u008a\u0249\3\2\2\2\u008c\u024b\3\2\2\2\u008e\u0265\3\2\2\2\u0090"+
		"\u02a4\3\2\2\2\u0092\u02a6\3\2\2\2\u0094\u02ec\3\2\2\2\u0096\u02f9\3\2"+
		"\2\2\u0098\u02fb\3\2\2\2\u009a\u030d\3\2\2\2\u009c\u030f\3\2\2\2\u009e"+
		"\u0313\3\2\2\2\u00a0\u0315\3\2\2\2\u00a2\u031c\3\2\2\2\u00a4\u032c\3\2"+
		"\2\2\u00a6\u0340\3\2\2\2\u00a8\u0342\3\2\2\2\u00aa\u034e\3\2\2\2\u00ac"+
		"\u0353\3\2\2\2\u00ae\u0356\3\2\2\2\u00b0\u0358\3\2\2\2\u00b2\u0368\3\2"+
		"\2\2\u00b4\u0373\3\2\2\2\u00b6\u0375\3\2\2\2\u00b8\u0377\3\2\2\2\u00ba"+
		"\u0381\3\2\2\2\u00bc\u0383\3\2\2\2\u00be\u038b\3\2\2\2\u00c0\u039a\3\2"+
		"\2\2\u00c2\u03b3\3\2\2\2\u00c4\u03f0\3\2\2\2\u00c6\u0403\3\2\2\2\u00c8"+
		"\u0405\3\2\2\2\u00ca\u0414\3\2\2\2\u00cc\u0416\3\2\2\2\u00ce\u0420\3\2"+
		"\2\2\u00d0\u0426\3\2\2\2\u00d2\u042b\3\2\2\2\u00d4\u042e\3\2\2\2\u00d6"+
		"\u044f\3\2\2\2\u00d8\u0461\3\2\2\2\u00da\u0465\3\2\2\2\u00dc\u0467\3\2"+
		"\2\2\u00de\u046f\3\2\2\2\u00e0\u0471\3\2\2\2\u00e2\u0473\3\2\2\2\u00e4"+
		"\u0475\3\2\2\2\u00e6\u0477\3\2\2\2\u00e8\u0479\3\2\2\2\u00ea\u047b\3\2"+
		"\2\2\u00ec\u047d\3\2\2\2\u00ee\u047f\3\2\2\2\u00f0\u0481\3\2\2\2\u00f2"+
		"\u0483\3\2\2\2\u00f4\u0485\3\2\2\2\u00f6\u0487\3\2\2\2\u00f8\u0489\3\2"+
		"\2\2\u00fa\u048b\3\2\2\2\u00fc\u048d\3\2\2\2\u00fe\u048f\3\2\2\2\u0100"+
		"\u0491\3\2\2\2\u0102\u0493\3\2\2\2\u0104\u0495\3\2\2\2\u0106\u049b\3\2"+
		"\2\2\u0108\u049d\3\2\2\2\u010a\u049f\3\2\2\2\u010c\u04a1\3\2\2\2\u010e"+
		"\u04a3\3\2\2\2\u0110\u04a5\3\2\2\2\u0112\u04a7\3\2\2\2\u0114\u04a9\3\2"+
		"\2\2\u0116\u04ab\3\2\2\2\u0118\u04ad\3\2\2\2\u011a\u04af\3\2\2\2\u011c"+
		"\u04b1\3\2\2\2\u011e\u04b3\3\2\2\2\u0120\u04b8\3\2\2\2\u0122\u04bd\3\2"+
		"\2\2\u0124\u04c2\3\2\2\2\u0126\u04c4\3\2\2\2\u0128\u04c9\3\2\2\2\u012a"+
		"\u04cd\3\2\2\2\u012c\u04cf\3\2\2\2\u012e\u04d1\3\2\2\2\u0130\u04d6\3\2"+
		"\2\2\u0132\u04dd\3\2\2\2\u0134\u04e1\3\2\2\2\u0136\u04e3\3\2\2\2\u0138"+
		"\u04e7\3\2\2\2\u013a\u04e9\3\2\2\2\u013c\u04eb\3\2\2\2\u013e\u04f0\3\2"+
		"\2\2\u0140\u04f4\3\2\2\2\u0142\u0143\7)\2\2\u0143\3\3\2\2\2\u0144\u014c"+
		"\5\6\4\2\u0145\u014c\5\b\5\2\u0146\u014c\5\n\6\2\u0147\u014c\5\f\7\2\u0148"+
		"\u014c\5\16\b\2\u0149\u014c\5\20\t\2\u014a\u014c\5\22\n\2\u014b\u0144"+
		"\3\2\2\2\u014b\u0145\3\2\2\2\u014b\u0146\3\2\2\2\u014b\u0147\3\2\2\2\u014b"+
		"\u0148\3\2\2\2\u014b\u0149\3\2\2\2\u014b\u014a\3\2\2\2\u014c\5\3\2\2\2"+
		"\u014d\u014e\7\u02d9\2\2\u014e\7\3\2\2\2\u014f\u0151\t\2\2\2\u0150\u014f"+
		"\3\2\2\2\u0150\u0151\3\2\2\2\u0151\u0152\3\2\2\2\u0152\u0153\t\3\2\2\u0153"+
		"\t\3\2\2\2\u0154\u0155\t\4\2\2\u0155\u015c\7\u02d9\2\2\u0156\u0157\7!"+
		"\2\2\u0157\u0158\5\24\13\2\u0158\u0159\7\u02d9\2\2\u0159\u015a\7\"\2\2"+
		"\u015a\u015c\3\2\2\2\u015b\u0154\3\2\2\2\u015b\u0156\3\2\2\2\u015c\13"+
		"\3\2\2\2\u015d\u015e\7\u02dc\2\2\u015e\r\3\2\2\2\u015f\u0160\7\u02dd\2"+
		"\2\u0160\17\3\2\2\2\u0161\u0162\t\5\2\2\u0162\21\3\2\2\2\u0163\u0164\7"+
		"i\2\2\u0164\23\3\2\2\2\u0165\u0168\7\u02d8\2\2\u0166\u0168\5\26\f\2\u0167"+
		"\u0165\3\2\2\2\u0167\u0166\3\2\2\2\u0168\25\3\2\2\2\u0169\u016a\t\6\2"+
		"\2\u016a\27\3\2\2\2\u016b\u016c\5\24\13\2\u016c\31\3\2\2\2\u016d\u016e"+
		"\5,\27\2\u016e\u016f\7\24\2\2\u016f\u0171\3\2\2\2\u0170\u016d\3\2\2\2"+
		"\u0170\u0171\3\2\2\2\u0171\u0172\3\2\2\2\u0172\u0173\5.\30\2\u0173\33"+
		"\3\2\2\2\u0174\u0175\5,\27\2\u0175\u0176\7\24\2\2\u0176\u0178\3\2\2\2"+
		"\u0177\u0174\3\2\2\2\u0177\u0178\3\2\2\2\u0178\u0179\3\2\2\2\u0179\u017a"+
		"\5.\30\2\u017a\35\3\2\2\2\u017b\u017c\5,\27\2\u017c\u017d\7\24\2\2\u017d"+
		"\u017f\3\2\2\2\u017e\u017b\3\2\2\2\u017e\u017f\3\2\2\2\u017f\u0180\3\2"+
		"\2\2\u0180\u0181\5.\30\2\u0181\37\3\2\2\2\u0182\u0183\5,\27\2\u0183\u0184"+
		"\7\24\2\2\u0184\u0186\3\2\2\2\u0185\u0182\3\2\2\2\u0185\u0186\3\2\2\2"+
		"\u0186\u0187\3\2\2\2\u0187\u0188\5.\30\2\u0188!\3\2\2\2\u0189\u018a\5"+
		",\27\2\u018a\u018b\7\24\2\2\u018b\u018d\3\2\2\2\u018c\u0189\3\2\2\2\u018c"+
		"\u018d\3\2\2\2\u018d\u018e\3\2\2\2\u018e\u018f\5.\30\2\u018f#\3\2\2\2"+
		"\u0190\u0191\5,\27\2\u0191\u0192\7\24\2\2\u0192\u0194\3\2\2\2\u0193\u0190"+
		"\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0195\3\2\2\2\u0195\u0196\5.\30\2\u0196"+
		"%\3\2\2\2\u0197\u0198\5\24\13\2\u0198\'\3\2\2\2\u0199\u019a\5\24\13\2"+
		"\u019a)\3\2\2\2\u019b\u019c\5\24\13\2\u019c+\3\2\2\2\u019d\u019e\5\24"+
		"\13\2\u019e-\3\2\2\2\u019f\u01a0\5\24\13\2\u01a0/\3\2\2\2\u01a1\u01a2"+
		"\5\24\13\2\u01a2\61\3\2\2\2\u01a3\u01a4\5\24\13\2\u01a4\63\3\2\2\2\u01a5"+
		"\u01a6\5\24\13\2\u01a6\65\3\2\2\2\u01a7\u01a8\5\24\13\2\u01a8\67\3\2\2"+
		"\2\u01a9\u01aa\5\24\13\2\u01aa9\3\2\2\2\u01ab\u01ac\5\24\13\2\u01ac;\3"+
		"\2\2\2\u01ad\u01b0\5\4\3\2\u01ae\u01b0\5\24\13\2\u01af\u01ad\3\2\2\2\u01af"+
		"\u01ae\3\2\2\2\u01b0=\3\2\2\2\u01b1\u01b2\5\24\13\2\u01b2?\3\2\2\2\u01b3"+
		"\u01b4\5\6\4\2\u01b4A\3\2\2\2\u01b5\u01b6\5\6\4\2\u01b6C\3\2\2\2\u01b7"+
		"\u01b8\5\24\13\2\u01b8E\3\2\2\2\u01b9\u01ba\5\24\13\2\u01baG\3\2\2\2\u01bb"+
		"\u01bc\5\24\13\2\u01bcI\3\2\2\2\u01bd\u01be\5\24\13\2\u01beK\3\2\2\2\u01bf"+
		"\u01c0\5\24\13\2\u01c0M\3\2\2\2\u01c1\u01c2\5\24\13\2\u01c2O\3\2\2\2\u01c3"+
		"\u01c4\5\24\13\2\u01c4Q\3\2\2\2\u01c5\u01c6\5\24\13\2\u01c6S\3\2\2\2\u01c7"+
		"\u01c8\5\24\13\2\u01c8U\3\2\2\2\u01c9\u01ca\5\24\13\2\u01caW\3\2\2\2\u01cb"+
		"\u01cc\5\24\13\2\u01ccY\3\2\2\2\u01cd\u01ce\5\24\13\2\u01ce[\3\2\2\2\u01cf"+
		"\u01d0\5\24\13\2\u01d0]\3\2\2\2\u01d1\u01d2\5\24\13\2\u01d2_\3\2\2\2\u01d3"+
		"\u01d4\5\24\13\2\u01d4a\3\2\2\2\u01d5\u01d6\5\24\13\2\u01d6c\3\2\2\2\u01d7"+
		"\u01da\7\u02da\2\2\u01d8\u01da\5\n\6\2\u01d9\u01d7\3\2\2\2\u01d9\u01d8"+
		"\3\2\2\2\u01dae\3\2\2\2\u01db\u01de\7\u02da\2\2\u01dc\u01de\5\n\6\2\u01dd"+
		"\u01db\3\2\2\2\u01dd\u01dc\3\2\2\2\u01deg\3\2\2\2\u01df\u01e0\5\24\13"+
		"\2\u01e0i\3\2\2\2\u01e1\u01e2\5\24\13\2\u01e2k\3\2\2\2\u01e3\u01e4\5\24"+
		"\13\2\u01e4m\3\2\2\2\u01e5\u01e6\5\24\13\2\u01e6o\3\2\2\2\u01e7\u01e8"+
		"\5\24\13\2\u01e8q\3\2\2\2\u01e9\u01eb\7\37\2\2\u01ea\u01e9\3\2\2\2\u01ea"+
		"\u01eb\3\2\2\2\u01eb\u01ec\3\2\2\2\u01ec\u01f1\5\36\20\2\u01ed\u01ee\7"+
		"%\2\2\u01ee\u01f0\5\36\20\2\u01ef\u01ed\3\2\2\2\u01f0\u01f3\3\2\2\2\u01f1"+
		"\u01ef\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2\u01f5\3\2\2\2\u01f3\u01f1\3\2"+
		"\2\2\u01f4\u01f6\7 \2\2\u01f5\u01f4\3\2\2\2\u01f5\u01f6\3\2\2\2\u01f6"+
		"s\3\2\2\2\u01f7\u01f9\7\37\2\2\u01f8\u01f7\3\2\2\2\u01f8\u01f9\3\2\2\2"+
		"\u01f9\u01fa\3\2\2\2\u01fa\u01ff\5\32\16\2\u01fb\u01fc\7%\2\2\u01fc\u01fe"+
		"\5\32\16\2\u01fd\u01fb\3\2\2\2\u01fe\u0201\3\2\2\2\u01ff\u01fd\3\2\2\2"+
		"\u01ff\u0200\3\2\2\2\u0200\u0203\3\2\2\2\u0201\u01ff\3\2\2\2\u0202\u0204"+
		"\7 \2\2\u0203\u0202\3\2\2\2\u0203\u0204\3\2\2\2\u0204u\3\2\2\2\u0205\u020f"+
		"\7\u02d8\2\2\u0206\u0207\7\u02d9\2\2\u0207\u0209\7\24\2\2\u0208\u0206"+
		"\3\2\2\2\u0209\u020c\3\2\2\2\u020a\u0208\3\2\2\2\u020a\u020b\3\2\2\2\u020b"+
		"\u020d\3\2\2\2\u020c\u020a\3\2\2\2\u020d\u020f\7\u02d9\2\2\u020e\u0205"+
		"\3\2\2\2\u020e\u020a\3\2\2\2\u020fw\3\2\2\2\u0210\u0211\t\7\2\2\u0211"+
		"y\3\2\2\2\u0212\u0213\5\24\13\2\u0213{\3\2\2\2\u0214\u0217\5\24\13\2\u0215"+
		"\u0217\7\u02d9\2\2\u0216\u0214\3\2\2\2\u0216\u0215\3\2\2\2\u0217}\3\2"+
		"\2\2\u0218\u021e\7\37\2\2\u0219\u021c\7\u02da\2\2\u021a\u021b\7%\2\2\u021b"+
		"\u021d\7\u02da\2\2\u021c\u021a\3\2\2\2\u021c\u021d\3\2\2\2\u021d\u021f"+
		"\3\2\2\2\u021e\u0219\3\2\2\2\u021e\u021f\3\2\2\2\u021f\u0220\3\2\2\2\u0220"+
		"\u0221\7 \2\2\u0221\177\3\2\2\2\u0222\u0224\7?\2\2\u0223\u0222\3\2\2\2"+
		"\u0223\u0224\3\2\2\2\u0224\u0225\3\2\2\2\u0225\u0226\7B\2\2\u0226\u0081"+
		"\3\2\2\2\u0227\u022c\5\u0086D\2\u0228\u0229\7%\2\2\u0229\u022b\5\u0086"+
		"D\2\u022a\u0228\3\2\2\2\u022b\u022e\3\2\2\2\u022c\u022a\3\2\2\2\u022c"+
		"\u022d\3\2\2\2\u022d\u0083\3\2\2\2\u022e\u022c\3\2\2\2\u022f\u0230\7\37"+
		"\2\2\u0230\u0231\5\u0082B\2\u0231\u0232\7 \2\2\u0232\u0085\3\2\2\2\u0233"+
		"\u0234\bD\1\2\u0234\u0235\5\u008aF\2\u0235\u0236\5\u0086D\5\u0236\u023d"+
		"\3\2\2\2\u0237\u0238\7\37\2\2\u0238\u0239\5\u0086D\2\u0239\u023a\7 \2"+
		"\2\u023a\u023d\3\2\2\2\u023b\u023d\5\u008cG\2\u023c\u0233\3\2\2\2\u023c"+
		"\u0237\3\2\2\2\u023c\u023b\3\2\2\2\u023d\u0244\3\2\2\2\u023e\u023f\f\6"+
		"\2\2\u023f\u0240\5\u0088E\2\u0240\u0241\5\u0086D\7\u0241\u0243\3\2\2\2"+
		"\u0242\u023e\3\2\2\2\u0243\u0246\3\2\2\2\u0244\u0242\3\2\2\2\u0244\u0245"+
		"\3\2\2\2\u0245\u0087\3\2\2\2\u0246\u0244\3\2\2\2\u0247\u0248\t\b\2\2\u0248"+
		"\u0089\3\2\2\2\u0249\u024a\t\t\2\2\u024a\u008b\3\2\2\2\u024b\u024c\bG"+
		"\1\2\u024c\u024d\5\u0090I\2\u024d\u0262\3\2\2\2\u024e\u024f\f\7\2\2\u024f"+
		"\u0251\7g\2\2\u0250\u0252\7h\2\2\u0251\u0250\3\2\2\2\u0251\u0252\3\2\2"+
		"\2\u0252\u0253\3\2\2\2\u0253\u0261\t\n\2\2\u0254\u0255\f\6\2\2\u0255\u0256"+
		"\7\26\2\2\u0256\u0261\5\u0090I\2\u0257\u0258\f\5\2\2\u0258\u0259\5\u008e"+
		"H\2\u0259\u025a\5\u0090I\2\u025a\u0261\3\2\2\2\u025b\u025c\f\4\2\2\u025c"+
		"\u025d\5\u008eH\2\u025d\u025e\t\13\2\2\u025e\u025f\5\u00aeX\2\u025f\u0261"+
		"\3\2\2\2\u0260\u024e\3\2\2\2\u0260\u0254\3\2\2\2\u0260\u0257\3\2\2\2\u0260"+
		"\u025b\3\2\2\2\u0261\u0264\3\2\2\2\u0262\u0260\3\2\2\2\u0262\u0263\3\2"+
		"\2\2\u0263\u008d\3\2\2\2\u0264\u0262\3\2\2\2\u0265\u0266\t\f\2\2\u0266"+
		"\u008f\3\2\2\2\u0267\u0269\5\u0092J\2\u0268\u026a\7h\2\2\u0269\u0268\3"+
		"\2\2\2\u0269\u026a\3\2\2\2\u026a\u026b\3\2\2\2\u026b\u026c\7n\2\2\u026c"+
		"\u026d\5\u00aeX\2\u026d\u02a5\3\2\2\2\u026e\u0270\5\u0092J\2\u026f\u0271"+
		"\7h\2\2\u0270\u026f\3\2\2\2\u0270\u0271\3\2\2\2\u0271\u0272\3\2\2\2\u0272"+
		"\u0273\7n\2\2\u0273\u0274\7\37\2\2\u0274\u0279\5\u0086D\2\u0275\u0276"+
		"\7%\2\2\u0276\u0278\5\u0086D\2\u0277\u0275\3\2\2\2\u0278\u027b\3\2\2\2"+
		"\u0279\u0277\3\2\2\2\u0279\u027a\3\2\2\2\u027a\u027c\3\2\2\2\u027b\u0279"+
		"\3\2\2\2\u027c\u027d\7 \2\2\u027d\u02a5\3\2\2\2\u027e\u0280\5\u0092J\2"+
		"\u027f\u0281\7h\2\2\u0280\u027f\3\2\2\2\u0280\u0281\3\2\2\2\u0281\u0282"+
		"\3\2\2\2\u0282\u0283\7n\2\2\u0283\u0284\7\37\2\2\u0284\u0289\5\u0086D"+
		"\2\u0285\u0286\7%\2\2\u0286\u0288\5\u0086D\2\u0287\u0285\3\2\2\2\u0288"+
		"\u028b\3\2\2\2\u0289\u0287\3\2\2\2\u0289\u028a\3\2\2\2\u028a\u028c\3\2"+
		"\2\2\u028b\u0289\3\2\2\2\u028c\u028d\7 \2\2\u028d\u028e\7e\2\2\u028e\u028f"+
		"\5\u0090I\2\u028f\u02a5\3\2\2\2\u0290\u0292\5\u0092J\2\u0291\u0293\7h"+
		"\2\2\u0292\u0291\3\2\2\2\u0292\u0293\3\2\2\2\u0293\u0294\3\2\2\2\u0294"+
		"\u0295\7m\2\2\u0295\u0296\5\u0092J\2\u0296\u0297\7e\2\2\u0297\u0298\5"+
		"\u0090I\2\u0298\u02a5\3\2\2\2\u0299\u029b\5\u0092J\2\u029a\u029c\7h\2"+
		"\2\u029b\u029a\3\2\2\2\u029b\u029c\3\2\2\2\u029c\u029d\3\2\2\2\u029d\u029e"+
		"\7q\2\2\u029e\u02a1\5\u0094K\2\u029f\u02a0\7\u00b7\2\2\u02a0\u02a2\5\u0094"+
		"K\2\u02a1\u029f\3\2\2\2\u02a1\u02a2\3\2\2\2\u02a2\u02a5\3\2\2\2\u02a3"+
		"\u02a5\5\u0092J\2\u02a4\u0267\3\2\2\2\u02a4\u026e\3\2\2\2\u02a4\u027e"+
		"\3\2\2\2\u02a4\u0290\3\2\2\2\u02a4\u0299\3\2\2\2\u02a4\u02a3\3\2\2\2\u02a5"+
		"\u0091\3\2\2\2\u02a6\u02a7\bJ\1\2\u02a7\u02a8\5\u0094K\2\u02a8\u02c9\3"+
		"\2\2\2\u02a9\u02aa\f\r\2\2\u02aa\u02ab\7\b\2\2\u02ab\u02c8\5\u0092J\16"+
		"\u02ac\u02ad\f\f\2\2\u02ad\u02ae\7\t\2\2\u02ae\u02c8\5\u0092J\r\u02af"+
		"\u02b0\f\13\2\2\u02b0\u02b1\7\n\2\2\u02b1\u02c8\5\u0092J\f\u02b2\u02b3"+
		"\f\n\2\2\u02b3\u02b4\7\13\2\2\u02b4\u02c8\5\u0092J\13\u02b5\u02b6\f\t"+
		"\2\2\u02b6\u02b7\7\17\2\2\u02b7\u02c8\5\u0092J\n\u02b8\u02b9\f\b\2\2\u02b9"+
		"\u02ba\7\20\2\2\u02ba\u02c8\5\u0092J\t\u02bb\u02bc\f\7\2\2\u02bc\u02bd"+
		"\7\21\2\2\u02bd\u02c8\5\u0092J\b\u02be\u02bf\f\6\2\2\u02bf\u02c0\7\22"+
		"\2\2\u02c0\u02c8\5\u0092J\7\u02c1\u02c2\f\5\2\2\u02c2\u02c3\7\r\2\2\u02c3"+
		"\u02c8\5\u0092J\6\u02c4\u02c5\f\4\2\2\u02c5\u02c6\7\f\2\2\u02c6\u02c8"+
		"\5\u0092J\5\u02c7\u02a9\3\2\2\2\u02c7\u02ac\3\2\2\2\u02c7\u02af\3\2\2"+
		"\2\u02c7\u02b2\3\2\2\2\u02c7\u02b5\3\2\2\2\u02c7\u02b8\3\2\2\2\u02c7\u02bb"+
		"\3\2\2\2\u02c7\u02be\3\2\2\2\u02c7\u02c1\3\2\2\2\u02c7\u02c4\3\2\2\2\u02c8"+
		"\u02cb\3\2\2\2\u02c9\u02c7\3\2\2\2\u02c9\u02ca\3\2\2\2\u02ca\u0093\3\2"+
		"\2\2\u02cb\u02c9\3\2\2\2\u02cc\u02cd\bK\1\2\u02cd\u02ed\5\u0096L\2\u02ce"+
		"\u02ed\5\2\2\2\u02cf\u02ed\5\4\3\2\u02d0\u02ed\5\36\20\2\u02d1\u02d2\t"+
		"\r\2\2\u02d2\u02ed\5\u0094K\b\u02d3\u02d5\7\u00ba\2\2\u02d4\u02d3\3\2"+
		"\2\2\u02d4\u02d5\3\2\2\2\u02d5\u02d6\3\2\2\2\u02d6\u02d7\7\37\2\2\u02d7"+
		"\u02dc\5\u0086D\2\u02d8\u02d9\7%\2\2\u02d9\u02db\5\u0086D\2\u02da\u02d8"+
		"\3\2\2\2\u02db\u02de\3\2\2\2\u02dc\u02da\3\2\2\2\u02dc\u02dd\3\2\2\2\u02dd"+
		"\u02df\3\2\2\2\u02de\u02dc\3\2\2\2\u02df\u02e0\7 \2\2\u02e0\u02ed\3\2"+
		"\2\2\u02e1\u02e3\7l\2\2\u02e2\u02e1\3\2\2\2\u02e2\u02e3\3\2\2\2\u02e3"+
		"\u02e4\3\2\2\2\u02e4\u02ed\5\u00aeX\2\u02e5\u02e6\7!\2\2\u02e6\u02e7\5"+
		"\24\13\2\u02e7\u02e8\5\u0086D\2\u02e8\u02e9\7\"\2\2\u02e9\u02ed\3\2\2"+
		"\2\u02ea\u02ed\5\u00a8U\2\u02eb\u02ed\5\u00caf\2\u02ec\u02cc\3\2\2\2\u02ec"+
		"\u02ce\3\2\2\2\u02ec\u02cf\3\2\2\2\u02ec\u02d0\3\2\2\2\u02ec\u02d1\3\2"+
		"\2\2\u02ec\u02d4\3\2\2\2\u02ec\u02e2\3\2\2\2\u02ec\u02e5\3\2\2\2\u02ec"+
		"\u02ea\3\2\2\2\u02ec\u02eb\3\2\2\2\u02ed\u02f3\3\2\2\2\u02ee\u02ef\f\t"+
		"\2\2\u02ef\u02f0\7\5\2\2\u02f0\u02f2\5\u0094K\n\u02f1\u02ee\3\2\2\2\u02f2"+
		"\u02f5\3\2\2\2\u02f3\u02f1\3\2\2\2\u02f3\u02f4\3\2\2\2\u02f4\u0095\3\2"+
		"\2\2\u02f5\u02f3\3\2\2\2\u02f6\u02fa\5\u0098M\2\u02f7\u02fa\5\u009eP\2"+
		"\u02f8\u02fa\5\u00a4S\2\u02f9\u02f6\3\2\2\2\u02f9\u02f7\3\2\2\2\u02f9"+
		"\u02f8\3\2\2\2\u02fa\u0097\3\2\2\2\u02fb\u02fc\5\u009aN\2\u02fc\u02fe"+
		"\7\37\2\2\u02fd\u02ff\5\u009cO\2\u02fe\u02fd\3\2\2\2\u02fe\u02ff\3\2\2"+
		"\2\u02ff\u0309\3\2\2\2\u0300\u0305\5\u0086D\2\u0301\u0302\7%\2\2\u0302"+
		"\u0304\5\u0086D\2\u0303\u0301\3\2\2\2\u0304\u0307\3\2\2\2\u0305\u0303"+
		"\3\2\2\2\u0305\u0306\3\2\2\2\u0306\u030a\3\2\2\2\u0307\u0305\3\2\2\2\u0308"+
		"\u030a\7\21\2\2\u0309\u0300\3\2\2\2\u0309\u0308\3\2\2\2\u0309\u030a\3"+
		"\2\2\2\u030a\u030b\3\2\2\2\u030b\u030c\7 \2\2\u030c\u0099\3\2\2\2\u030d"+
		"\u030e\t\16\2\2\u030e\u009b\3\2\2\2\u030f\u0310\7M\2\2\u0310\u009d\3\2"+
		"\2\2\u0311\u0314\5\u00a0Q\2\u0312\u0314\5\u00a2R\2\u0313\u0311\3\2\2\2"+
		"\u0313\u0312\3\2\2\2\u0314\u009f\3\2\2\2\u0315\u0316\7P\2\2\u0316\u0317"+
		"\7\37\2\2\u0317\u0318\5\u0086D\2\u0318\u0319\7^\2\2\u0319\u031a\5\u00c0"+
		"a\2\u031a\u031b\7 \2\2\u031b\u00a1\3\2\2\2\u031c\u031d\7\u0080\2\2\u031d"+
		"\u031e\7\37\2\2\u031e\u0323\5\u0086D\2\u031f\u0320\7%\2\2\u0320\u0322"+
		"\5\u0086D\2\u0321\u031f\3\2\2\2\u0322\u0325\3\2\2\2\u0323\u0321\3\2\2"+
		"\2\u0323\u0324\3\2\2\2\u0324\u0328\3\2\2\2\u0325\u0323\3\2\2\2\u0326\u0327"+
		"\7\\\2\2\u0327\u0329\5\u00dan\2\u0328\u0326\3\2\2\2\u0328\u0329\3\2\2"+
		"\2\u0329\u032a\3\2\2\2\u032a\u032b\7 \2\2\u032b\u00a3\3\2\2\2\u032c\u032d"+
		"\5\u00a6T\2\u032d\u0337\7\37\2\2\u032e\u0333\5\u0086D\2\u032f\u0330\7"+
		"%\2\2\u0330\u0332\5\u0086D\2\u0331\u032f\3\2\2\2\u0332\u0335\3\2\2\2\u0333"+
		"\u0331\3\2\2\2\u0333\u0334\3\2\2\2\u0334\u0338\3\2\2\2\u0335\u0333\3\2"+
		"\2\2\u0336\u0338\7\21\2\2\u0337\u032e\3\2\2\2\u0337\u0336\3\2\2\2\u0337"+
		"\u0338\3\2\2\2\u0338\u0339\3\2\2\2\u0339\u033a\7 \2\2\u033a\u00a5\3\2"+
		"\2\2\u033b\u0341\5\24\13\2\u033c\u0341\7`\2\2\u033d\u0341\7\u0088\2\2"+
		"\u033e\u0341\7\u0089\2\2\u033f\u0341\7\u0083\2\2\u0340\u033b\3\2\2\2\u0340"+
		"\u033c\3\2\2\2\u0340\u033d\3\2\2\2\u0340\u033e\3\2\2\2\u0340\u033f\3\2"+
		"\2\2\u0341\u00a7\3\2\2\2\u0342\u0344\7N\2\2\u0343\u0345\5\u0094K\2\u0344"+
		"\u0343\3\2\2\2\u0344\u0345\3\2\2\2\u0345\u0347\3\2\2\2\u0346\u0348\5\u00aa"+
		"V\2\u0347\u0346\3\2\2\2\u0348\u0349\3\2\2\2\u0349\u0347\3\2\2\2\u0349"+
		"\u034a\3\2\2\2\u034a\u034c\3\2\2\2\u034b\u034d\5\u00acW\2\u034c\u034b"+
		"\3\2\2\2\u034c\u034d\3\2\2\2\u034d\u00a9\3\2\2\2\u034e\u034f\7O\2\2\u034f"+
		"\u0350\5\u0086D\2\u0350\u0351\7b\2\2\u0351\u0352\5\u0086D\2\u0352\u00ab"+
		"\3\2\2\2\u0353\u0354\7a\2\2\u0354\u0355\5\u0086D\2\u0355\u00ad\3\2\2\2"+
		"\u0356\u0357\5\u00dep\2\u0357\u00af\3\2\2\2\u0358\u035a\7r\2\2\u0359\u035b"+
		"\7\u029c\2\2\u035a\u0359\3\2\2\2\u035a\u035b\3\2\2\2\u035b\u035c\3\2\2"+
		"\2\u035c\u035d\7t\2\2\u035d\u0362\5\u00b2Z\2\u035e\u035f\7%\2\2\u035f"+
		"\u0361\5\u00b2Z\2\u0360\u035e\3\2\2\2\u0361\u0364\3\2\2\2\u0362\u0360"+
		"\3\2\2\2\u0362\u0363\3\2\2\2\u0363\u00b1\3\2\2\2\u0364\u0362\3\2\2\2\u0365"+
		"\u0369\5\36\20\2\u0366\u0369\5\b\5\2\u0367\u0369\5\u0086D\2\u0368\u0365"+
		"\3\2\2\2\u0368\u0366\3\2\2\2\u0368\u0367\3\2\2\2\u0369\u036b\3\2\2\2\u036a"+
		"\u036c\t\17\2\2\u036b\u036a\3\2\2\2\u036b\u036c\3\2\2\2\u036c\u0371\3"+
		"\2\2\2\u036d\u036e\7\u029d\2\2\u036e\u0372\7\u015c\2\2\u036f\u0370\7\u029d"+
		"\2\2\u0370\u0372\7\u029e\2\2\u0371\u036d\3\2\2\2\u0371\u036f\3\2\2\2\u0371"+
		"\u0372\3\2\2\2\u0372\u00b3\3\2\2\2\u0373\u0374\5v<\2\u0374\u00b5\3\2\2"+
		"\2\u0375\u0376\7\u02d8\2\2\u0376\u00b7\3\2\2\2\u0377\u037c\5\u0094K\2"+
		"\u0378\u0379\7%\2\2\u0379\u037b\5\u0094K\2\u037a\u0378\3\2\2\2\u037b\u037e"+
		"\3\2\2\2\u037c\u037a\3\2\2\2\u037c\u037d\3\2\2\2\u037d\u00b9\3\2\2\2\u037e"+
		"\u037c\3\2\2\2\u037f\u0382\5\u00b4[\2\u0380\u0382\5\36\20\2\u0381\u037f"+
		"\3\2\2\2\u0381\u0380\3\2\2\2\u0382\u00bb\3\2\2\2\u0383\u0388\5\u00ba^"+
		"\2\u0384\u0385\7%\2\2\u0385\u0387\5\u00ba^\2\u0386\u0384\3\2\2\2\u0387"+
		"\u038a\3\2\2\2\u0388\u0386\3\2\2\2\u0388\u0389\3\2\2\2\u0389\u00bd\3\2"+
		"\2\2\u038a\u0388\3\2\2\2\u038b\u038c\7\37\2\2\u038c\u038d\5\u00bc_\2\u038d"+
		"\u038e\7 \2\2\u038e\u00bf\3\2\2\2\u038f\u0391\5\u00c4c\2\u0390\u0392\5"+
		"~@\2\u0391\u0390\3\2\2\2\u0391\u0392\3\2\2\2\u0392\u039b\3\2\2\2\u0393"+
		"\u039b\5\u00c2b\2\u0394\u0396\5\u00c4c\2\u0395\u0397\5~@\2\u0396\u0395"+
		"\3\2\2\2\u0396\u0397\3\2\2\2\u0397\u0398\3\2\2\2\u0398\u0399\5\u00c6d"+
		"\2\u0399\u039b\3\2\2\2\u039a\u038f\3\2\2\2\u039a\u0393\3\2\2\2\u039a\u0394"+
		"\3\2\2\2\u039b\u00c1\3\2\2\2\u039c\u039d\5\u00c4c\2\u039d\u039e\7\37\2"+
		"\2\u039e\u039f\7\u02db\2\2\u039f\u03a0\7\u0080\2\2\u03a0\u03a1\7 \2\2"+
		"\u03a1\u03b4\3\2\2\2\u03a2\u03a3\7\u00ff\2\2\u03a3\u03a5\5\u00c4c\2\u03a4"+
		"\u03a6\7\u011d\2\2\u03a5\u03a4\3\2\2\2\u03a5\u03a6\3\2\2\2\u03a6\u03a7"+
		"\3\2\2\2\u03a7\u03a8\7\37\2\2\u03a8\u03a9\7\u02db\2\2\u03a9\u03aa\7 \2"+
		"\2\u03aa\u03b4\3\2\2\2\u03ab\u03ad\5\u00c4c\2\u03ac\u03ae\7\37\2\2\u03ad"+
		"\u03ac\3\2\2\2\u03ad\u03ae\3\2\2\2\u03ae\u03af\3\2\2\2\u03af\u03b1\5\36"+
		"\20\2\u03b0\u03b2\7 \2\2\u03b1\u03b0\3\2\2\2\u03b1\u03b2\3\2\2\2\u03b2"+
		"\u03b4\3\2\2\2\u03b3\u039c\3\2\2\2\u03b3\u03a2\3\2\2\2\u03b3\u03ab\3\2"+
		"\2\2\u03b4\u00c3\3\2\2\2\u03b5\u03f1\7\u0080\2\2\u03b6\u03f1\7\u015d\2"+
		"\2\u03b7\u03f1\7\u015e\2\2\u03b8\u03f1\7\u015f\2\2\u03b9\u03f1\7\u0160"+
		"\2\2\u03ba\u03f1\7\u0161\2\2\u03bb\u03f1\7\u0162\2\2\u03bc\u03bd\7\u0162"+
		"\2\2\u03bd\u03f1\7\u015e\2\2\u03be\u03f1\7\u0163\2\2\u03bf\u03f1\7\u0164"+
		"\2\2\u03c0\u03f1\7\u0165\2\2\u03c1\u03f1\7\u0166\2\2\u03c2\u03f1\7\u0167"+
		"\2\2\u03c3\u03f1\7~\2\2\u03c4\u03f1\7\u0168\2\2\u03c5\u03f1\7\u0169\2"+
		"\2\u03c6\u03f1\7\u00ab\2\2\u03c7\u03f1\7\u016a\2\2\u03c8\u03f1\7T\2\2"+
		"\u03c9\u03f1\7\u016b\2\2\u03ca\u03f1\7\u016c\2\2\u03cb\u03f1\7\u016d\2"+
		"\2\u03cc\u03f1\7\u016e\2\2\u03cd\u03f1\7\u016f\2\2\u03ce\u03f1\7\u0170"+
		"\2\2\u03cf\u03f1\7\u0171\2\2\u03d0\u03f1\7\u0172\2\2\u03d1\u03f1\7\u0084"+
		"\2\2\u03d2\u03f1\7\u0087\2\2\u03d3\u03d4\7\u0087\2\2\u03d4\u03d5\7K\2"+
		"\2\u03d5\u03d6\7\u0085\2\2\u03d6\u03f1\7\u011f\2\2\u03d7\u03d8\7\u0087"+
		"\2\2\u03d8\u03d9\7K\2\2\u03d9\u03da\7\u00a4\2\2\u03da\u03db\7\u0085\2"+
		"\2\u03db\u03f1\7\u011f\2\2\u03dc\u03dd\7\u0083\2\2\u03dd\u03de\7\u008e"+
		"\2\2\u03de\u03df\7d\2\2\u03df\u03f1\7\u0091\2\2\u03e0\u03e1\7\u0083\2"+
		"\2\u03e1\u03e2\7\u008a\2\2\u03e2\u03e3\7d\2\2\u03e3\u03f1\7\u008c\2\2"+
		"\u03e4\u03f1\7\u0173\2\2\u03e5\u03f1\7\u00b2\2\2\u03e6\u03f1\7\u00ac\2"+
		"\2\u03e7\u03e8\7\177\2\2\u03e8\u03f1\7D\2\2\u03e9\u03f1\7\u00af\2\2\u03ea"+
		"\u03f1\7\u00b0\2\2\u03eb\u03f1\7\u00ad\2\2\u03ec\u03f1\7\u00b1\2\2\u03ed"+
		"\u03f1\7\u0174\2\2\u03ee\u03f1\7\u02d8\2\2\u03ef\u03f1\7\u0206\2\2\u03f0"+
		"\u03b5\3\2\2\2\u03f0\u03b6\3\2\2\2\u03f0\u03b7\3\2\2\2\u03f0\u03b8\3\2"+
		"\2\2\u03f0\u03b9\3\2\2\2\u03f0\u03ba\3\2\2\2\u03f0\u03bb\3\2\2\2\u03f0"+
		"\u03bc\3\2\2\2\u03f0\u03be\3\2\2\2\u03f0\u03bf\3\2\2\2\u03f0\u03c0\3\2"+
		"\2\2\u03f0\u03c1\3\2\2\2\u03f0\u03c2\3\2\2\2\u03f0\u03c3\3\2\2\2\u03f0"+
		"\u03c4\3\2\2\2\u03f0\u03c5\3\2\2\2\u03f0\u03c6\3\2\2\2\u03f0\u03c7\3\2"+
		"\2\2\u03f0\u03c8\3\2\2\2\u03f0\u03c9\3\2\2\2\u03f0\u03ca\3\2\2\2\u03f0"+
		"\u03cb\3\2\2\2\u03f0\u03cc\3\2\2\2\u03f0\u03cd\3\2\2\2\u03f0\u03ce\3\2"+
		"\2\2\u03f0\u03cf\3\2\2\2\u03f0\u03d0\3\2\2\2\u03f0\u03d1\3\2\2\2\u03f0"+
		"\u03d2\3\2\2\2\u03f0\u03d3\3\2\2\2\u03f0\u03d7\3\2\2\2\u03f0\u03dc\3\2"+
		"\2\2\u03f0\u03e0\3\2\2\2\u03f0\u03e4\3\2\2\2\u03f0\u03e5\3\2\2\2\u03f0"+
		"\u03e6\3\2\2\2\u03f0\u03e7\3\2\2\2\u03f0\u03e9\3\2\2\2\u03f0\u03ea\3\2"+
		"\2\2\u03f0\u03eb\3\2\2\2\u03f0\u03ec\3\2\2\2\u03f0\u03ed\3\2\2\2\u03f0"+
		"\u03ee\3\2\2\2\u03f0\u03ef\3\2\2\2\u03f1\u00c5\3\2\2\2\u03f2\u03f4\7K"+
		"\2\2\u03f3\u03f5\7\u00a4\2\2\u03f4\u03f3\3\2\2\2\u03f4\u03f5\3\2\2\2\u03f5"+
		"\u03f6\3\2\2\2\u03f6\u03f7\7\u0085\2\2\u03f7\u03f9\7\u011f\2\2\u03f8\u03f2"+
		"\3\2\2\2\u03f8\u03f9\3\2\2\2\u03f9\u0404\3\2\2\2\u03fa\u03fb\7d\2\2\u03fb"+
		"\u0404\7\u008c\2\2\u03fc\u03fd\7d\2\2\u03fd\u0401\7\u0091\2\2\u03fe\u03ff"+
		"\7\37\2\2\u03ff\u0400\7\u02db\2\2\u0400\u0402\7 \2\2\u0401\u03fe\3\2\2"+
		"\2\u0401\u0402\3\2\2\2\u0402\u0404\3\2\2\2\u0403\u03f8\3\2\2\2\u0403\u03fa"+
		"\3\2\2\2\u0403\u03fc\3\2\2\2\u0404\u00c7\3\2\2\2\u0405\u0406\7\u0117\2"+
		"\2\u0406\u0407\7\37\2\2\u0407\u0408\5\u0086D\2\u0408\u040a\7^\2\2\u0409"+
		"\u040b\7\u0107\2\2\u040a\u0409\3\2\2\2\u040a\u040b\3\2\2\2\u040b\u040c"+
		"\3\2\2\2\u040c\u040d\5\u00c4c\2\u040d\u040e\7 \2\2\u040e\u00c9\3\2\2\2"+
		"\u040f\u0415\5\u00c8e\2\u0410\u0415\5\u00ccg\2\u0411\u0415\5\u00d4k\2"+
		"\u0412\u0415\5\u00d6l\2\u0413\u0415\5\u00d8m\2\u0414\u040f\3\2\2\2\u0414"+
		"\u0410\3\2\2\2\u0414\u0411\3\2\2\2\u0414\u0412\3\2\2\2\u0414\u0413\3\2"+
		"\2\2\u0415\u00cb\3\2\2\2\u0416\u0419\7N\2\2\u0417\u041a\5\u00ceh\2\u0418"+
		"\u041a\5\u00d0i\2\u0419\u0417\3\2\2\2\u0419\u0418\3\2\2\2\u041a\u041c"+
		"\3\2\2\2\u041b\u041d\5\u00d2j\2\u041c\u041b\3\2\2\2\u041c\u041d\3\2\2"+
		"\2\u041d\u041e\3\2\2\2\u041e\u041f\7\u00ea\2\2\u041f\u00cd\3\2\2\2\u0420"+
		"\u0422\5\u0086D\2\u0421\u0423\5\u00d0i\2\u0422\u0421\3\2\2\2\u0423\u0424"+
		"\3\2\2\2\u0424\u0422\3\2\2\2\u0424\u0425\3\2\2\2\u0425\u00cf\3\2\2\2\u0426"+
		"\u0427\7O\2\2\u0427\u0428\5\u0086D\2\u0428\u0429\7b\2\2\u0429\u042a\5"+
		"\u0094K\2\u042a\u00d1\3\2\2\2\u042b\u042c\7a\2\2\u042c\u042d\5\u0086D"+
		"\2\u042d\u00d3\3\2\2\2\u042e\u042f\7\37\2\2\u042f\u0430\5\u0086D\2\u0430"+
		"\u0431\7\20\2\2\u0431\u0432\5\u0086D\2\u0432\u0448\7 \2\2\u0433\u0437"+
		"\7\u008e\2\2\u0434\u0435\7\37\2\2\u0435\u0436\7\u02db\2\2\u0436\u0438"+
		"\7 \2\2\u0437\u0434\3\2\2\2\u0437\u0438\3\2\2\2\u0438\u0439\3\2\2\2\u0439"+
		"\u043a\7d\2\2\u043a\u043e\7\u0091\2\2\u043b\u043c\7\37\2\2\u043c\u043d"+
		"\7\u02db\2\2\u043d\u043f\7 \2\2\u043e\u043b\3\2\2\2\u043e\u043f\3\2\2"+
		"\2\u043f\u0449\3\2\2\2\u0440\u0444\7\u008a\2\2\u0441\u0442\7\37\2\2\u0442"+
		"\u0443\7\u02db\2\2\u0443\u0445\7 \2\2\u0444\u0441\3\2\2\2\u0444\u0445"+
		"\3\2\2\2\u0445\u0446\3\2\2\2\u0446\u0447\7d\2\2\u0447\u0449\7\u008c\2"+
		"\2\u0448\u0433\3\2\2\2\u0448\u0440\3\2\2\2\u0449\u00d5\3\2\2\2\u044a\u044b"+
		"\7\37\2\2\u044b\u044c\5\u0094K\2\u044c\u044d\7 \2\2\u044d\u0450\3\2\2"+
		"\2\u044e\u0450\5\u00c8e\2\u044f\u044a\3\2\2\2\u044f\u044e\3\2\2\2\u0450"+
		"\u0451\3\2\2\2\u0451\u045f\7\24\2\2\u0452\u0457\5\u00b4[\2\u0453\u0454"+
		"\7\24\2\2\u0454\u0456\5\u00b4[\2\u0455\u0453\3\2\2\2\u0456\u0459\3\2\2"+
		"\2\u0457\u0455\3\2\2\2\u0457\u0458\3\2\2\2\u0458\u045c\3\2\2\2\u0459\u0457"+
		"\3\2\2\2\u045a\u045b\7\24\2\2\u045b\u045d\5\u0096L\2\u045c\u045a\3\2\2"+
		"\2\u045c\u045d\3\2\2\2\u045d\u0460\3\2\2\2\u045e\u0460\5\u0096L\2\u045f"+
		"\u0452\3\2\2\2\u045f\u045e\3\2\2\2\u0460\u00d7\3\2\2\2\u0461\u0462\7\u0100"+
		"\2\2\u0462\u0463\5\u00c4c\2\u0463\u0464\5\u0084C\2\u0464\u00d9\3\2\2\2"+
		"\u0465\u0466\7\u02d8\2\2\u0466\u00db\3\2\2\2\u0467\u046c\5\u00dan\2\u0468"+
		"\u0469\7%\2\2\u0469\u046b\5\u00dan\2\u046a\u0468\3\2\2\2\u046b\u046e\3"+
		"\2\2\2\u046c\u046a\3\2\2\2\u046c\u046d\3\2\2\2\u046d\u00dd\3\2\2\2\u046e"+
		"\u046c\3\2\2\2\u046f\u0470\7\3\2\2\u0470\u00df\3\2\2\2\u0471\u0472\7\u016a"+
		"\2\2\u0472\u00e1\3\2\2\2\u0473\u0474\5\24\13\2\u0474\u00e3\3\2\2\2\u0475"+
		"\u0476\5\24\13\2\u0476\u00e5\3\2\2\2\u0477\u0478\7\u02d9\2\2\u0478\u00e7"+
		"\3\2\2\2\u0479\u047a\7\u02d9\2\2\u047a\u00e9\3\2\2\2\u047b\u047c\7\u02d9"+
		"\2\2\u047c\u00eb\3\2\2\2\u047d\u047e\7\u02da\2\2\u047e\u00ed\3\2\2\2\u047f"+
		"\u0480\7\u02d9\2\2\u0480\u00ef\3\2\2\2\u0481\u0482\5\24\13\2\u0482\u00f1"+
		"\3\2\2\2\u0483\u0484\5\24\13\2\u0484\u00f3\3\2\2\2\u0485\u0486\5\24\13"+
		"\2\u0486\u00f5\3\2\2\2\u0487\u0488\5\24\13\2\u0488\u00f7\3\2\2\2\u0489"+
		"\u048a\5\24\13\2\u048a\u00f9\3\2\2\2\u048b\u048c\5\24\13\2\u048c\u00fb"+
		"\3\2\2\2\u048d\u048e\5\24\13\2\u048e\u00fd\3\2\2\2\u048f\u0490\5\24\13"+
		"\2\u0490\u00ff\3\2\2\2\u0491\u0492\5\24\13\2\u0492\u0101\3\2\2\2\u0493"+
		"\u0494\5\24\13\2\u0494\u0103\3\2\2\2\u0495\u0496\5\24\13\2\u0496\u0105"+
		"\3\2\2\2\u0497\u049c\5\n\6\2\u0498\u049c\5\6\4\2\u0499\u049c\5\b\5\2\u049a"+
		"\u049c\5\u0086D\2\u049b\u0497\3\2\2\2\u049b\u0498\3\2\2\2\u049b\u0499"+
		"\3\2\2\2\u049b\u049a\3\2\2\2\u049c\u0107\3\2\2\2\u049d\u049e\5\b\5\2\u049e"+
		"\u0109\3\2\2\2\u049f\u04a0\5\b\5\2\u04a0\u010b\3\2\2\2\u04a1\u04a2\7\u02db"+
		"\2\2\u04a2\u010d\3\2\2\2\u04a3\u04a4\5\24\13\2\u04a4\u010f\3\2\2\2\u04a5"+
		"\u04a6\5\6\4\2\u04a6\u0111\3\2\2\2\u04a7\u04a8\5\6\4\2\u04a8\u0113\3\2"+
		"\2\2\u04a9\u04aa\5\6\4\2\u04aa\u0115\3\2\2\2\u04ab\u04ac\5\24\13\2\u04ac"+
		"\u0117\3\2\2\2\u04ad\u04ae\5\24\13\2\u04ae\u0119\3\2\2\2\u04af\u04b0\5"+
		"\24\13\2\u04b0\u011b\3\2\2\2\u04b1\u04b2\5\24\13\2\u04b2\u011d\3\2\2\2"+
		"\u04b3\u04b4\5\24\13\2\u04b4\u011f\3\2\2\2\u04b5\u04b9\5\b\5\2\u04b6\u04b9"+
		"\5\u0086D\2\u04b7\u04b9\5\22\n\2\u04b8\u04b5\3\2\2\2\u04b8\u04b6\3\2\2"+
		"\2\u04b8\u04b7\3\2\2\2\u04b9\u0121\3\2\2\2\u04ba\u04be\5\b\5\2\u04bb\u04be"+
		"\5\u0086D\2\u04bc\u04be\5\22\n\2\u04bd\u04ba\3\2\2\2\u04bd\u04bb\3\2\2"+
		"\2\u04bd\u04bc\3\2\2\2\u04be\u0123\3\2\2\2\u04bf\u04c3\5\b\5\2\u04c0\u04c3"+
		"\5\u0086D\2\u04c1\u04c3\5\22\n\2\u04c2\u04bf\3\2\2\2\u04c2\u04c0\3\2\2"+
		"\2\u04c2\u04c1\3\2\2\2\u04c3\u0125\3\2\2\2\u04c4\u04c5\5\24\13\2\u04c5"+
		"\u0127\3\2\2\2\u04c6\u04c7\5,\27\2\u04c7\u04c8\7\24\2\2\u04c8\u04ca\3"+
		"\2\2\2\u04c9\u04c6\3\2\2\2\u04c9\u04ca\3\2\2\2\u04ca\u04cb\3\2\2\2\u04cb"+
		"\u04cc\5.\30\2\u04cc\u0129\3\2\2\2\u04cd\u04ce\7\u02d9\2\2\u04ce\u012b"+
		"\3\2\2\2\u04cf\u04d0\7\u02d9\2\2\u04d0\u012d\3\2\2\2\u04d1\u04d2\5\36"+
		"\20\2\u04d2\u012f\3\2\2\2\u04d3\u04d4\5,\27\2\u04d4\u04d5\7\24\2\2\u04d5"+
		"\u04d7\3\2\2\2\u04d6\u04d3\3\2\2\2\u04d6\u04d7\3\2\2\2\u04d7\u04d8\3\2"+
		"\2\2\u04d8\u04d9\5.\30\2\u04d9\u0131\3\2\2\2\u04da\u04db\5,\27\2\u04db"+
		"\u04dc\7\24\2\2\u04dc\u04de\3\2\2\2\u04dd\u04da\3\2\2\2\u04dd\u04de\3"+
		"\2\2\2\u04de\u04df\3\2\2\2\u04df\u04e0\5.\30\2\u04e0\u0133\3\2\2\2\u04e1"+
		"\u04e2\5\24\13\2\u04e2\u0135\3\2\2\2\u04e3\u04e4\5\24\13\2\u04e4\u0137"+
		"\3\2\2\2\u04e5\u04e8\5\u0086D\2\u04e6\u04e8\5\b\5\2\u04e7\u04e5\3\2\2"+
		"\2\u04e7\u04e6\3\2\2\2\u04e8\u0139\3\2\2\2\u04e9\u04ea\5\24\13\2\u04ea"+
		"\u013b\3\2\2\2\u04eb\u04ec\5\24\13\2\u04ec\u013d\3\2\2\2\u04ed\u04ee\5"+
		",\27\2\u04ee\u04ef\7\24\2\2\u04ef\u04f1\3\2\2\2\u04f0\u04ed\3\2\2\2\u04f0"+
		"\u04f1\3\2\2\2\u04f1\u04f2\3\2\2\2\u04f2\u04f3\5.\30\2\u04f3\u013f\3\2"+
		"\2\2\u04f4\u04f5\5\24\13\2\u04f5\u0141\3\2\2\2i\u014b\u0150\u015b\u0167"+
		"\u0170\u0177\u017e\u0185\u018c\u0193\u01af\u01d9\u01dd\u01ea\u01f1\u01f5"+
		"\u01f8\u01ff\u0203\u020a\u020e\u0216\u021c\u021e\u0223\u022c\u023c\u0244"+
		"\u0251\u0260\u0262\u0269\u0270\u0279\u0280\u0289\u0292\u029b\u02a1\u02a4"+
		"\u02c7\u02c9\u02d4\u02dc\u02e2\u02ec\u02f3\u02f9\u02fe\u0305\u0309\u0313"+
		"\u0323\u0328\u0333\u0337\u0340\u0344\u0349\u034c\u035a\u0362\u0368\u036b"+
		"\u0371\u037c\u0381\u0388\u0391\u0396\u039a\u03a5\u03ad\u03b1\u03b3\u03f0"+
		"\u03f4\u03f8\u0401\u0403\u040a\u0414\u0419\u041c\u0424\u0437\u043e\u0444"+
		"\u0448\u044f\u0457\u045c\u045f\u046c\u049b\u04b8\u04bd\u04c2\u04c9\u04d6"+
		"\u04dd\u04e7\u04f0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}