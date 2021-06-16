// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/BaseRule.g4 by ANTLR 4.9.1
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
		T__0=1, T__1=2, T__2=3, WS=4, SELECT=5, INSERT=6, UPDATE=7, DELETE=8, 
		CREATE=9, ALTER=10, DROP=11, TRUNCATE=12, SCHEMA=13, GRANT=14, REVOKE=15, 
		ADD=16, SET=17, TABLE=18, COLUMN=19, INDEX=20, CONSTRAINT=21, PRIMARY=22, 
		UNIQUE=23, FOREIGN=24, KEY=25, POSITION=26, PRECISION=27, FUNCTION=28, 
		TRIGGER=29, PROCEDURE=30, VIEW=31, INTO=32, VALUES=33, WITH=34, UNION=35, 
		DISTINCT=36, CASE=37, WHEN=38, CAST=39, TRIM=40, SUBSTRING=41, FROM=42, 
		NATURAL=43, JOIN=44, FULL=45, INNER=46, OUTER=47, LEFT=48, RIGHT=49, CROSS=50, 
		USING=51, WHERE=52, AS=53, ON=54, IF=55, ELSE=56, THEN=57, FOR=58, TO=59, 
		AND=60, OR=61, IS=62, NOT=63, NULL=64, TRUE=65, FALSE=66, EXISTS=67, BETWEEN=68, 
		IN=69, ALL=70, ANY=71, LIKE=72, ORDER=73, GROUP=74, BY=75, ASC=76, DESC=77, 
		HAVING=78, LIMIT=79, OFFSET=80, BEGIN=81, COMMIT=82, ROLLBACK=83, SAVEPOINT=84, 
		BOOLEAN=85, DOUBLE=86, CHAR=87, CHARACTER=88, ARRAY=89, INTERVAL=90, DATE=91, 
		TIME=92, TIMESTAMP=93, LOCALTIME=94, LOCALTIMESTAMP=95, YEAR=96, QUARTER=97, 
		MONTH=98, WEEK=99, DAY=100, HOUR=101, MINUTE=102, SECOND=103, MICROSECOND=104, 
		DEFAULT=105, CURRENT=106, ENABLE=107, DISABLE=108, CALL=109, INSTANCE=110, 
		PRESERVE=111, DO=112, DEFINER=113, CURRENT_USER=114, SQL=115, CASCADED=116, 
		LOCAL=117, CLOSE=118, OPEN=119, NEXT=120, NAME=121, COLLATION=122, NAMES=123, 
		INTEGER=124, REAL=125, DECIMAL=126, TYPE=127, SMALLINT=128, BIGINT=129, 
		NUMERIC=130, TEXT=131, REPEATABLE=132, CURRENT_DATE=133, CURRENT_TIME=134, 
		CURRENT_TIMESTAMP=135, NULLIF=136, VARYING=137, NATIONAL=138, NCHAR=139, 
		VALUE=140, BOTH=141, LEADING=142, TRAILING=143, COALESCE=144, INTERSECT=145, 
		EXCEPT=146, TIES=147, FETCH=148, CUBE=149, GROUPING=150, SETS=151, WINDOW=152, 
		OTHERS=153, OVERLAPS=154, SOME=155, AT=156, DEC=157, END=158, FOR_GENERATOR=159, 
		ADMIN=160, BINARY=161, ESCAPE=162, EXCLUDE=163, MOD=164, PARTITION=165, 
		ROW=166, UNKNOWN=167, ALWAYS=168, CASCADE=169, CHECK=170, GENERATED=171, 
		ISOLATION=172, LEVEL=173, NO=174, OPTION=175, PRIVILEGES=176, READ=177, 
		REFERENCES=178, ROLE=179, ROWS=180, START=181, TRANSACTION=182, USER=183, 
		ACTION=184, CACHE=185, CHARACTERISTICS=186, CLUSTER=187, COLLATE=188, 
		COMMENTS=189, CONCURRENTLY=190, CONNECT=191, CONSTRAINTS=192, CYCLE=193, 
		DATA=194, DATABASE=195, DEFAULTS=196, DEFERRABLE=197, DEFERRED=198, DEPENDS=199, 
		DOMAIN=200, EXCLUDING=201, EXECUTE=202, EXTENDED=203, EXTENSION=204, EXTERNAL=205, 
		EXTRACT=206, FILTER=207, FIRST=208, FOLLOWING=209, FORCE=210, GLOBAL=211, 
		IDENTITY=212, IMMEDIATE=213, INCLUDING=214, INCREMENT=215, INDEXES=216, 
		INHERIT=217, INHERITS=218, INITIALLY=219, INCLUDE=220, LANGUAGE=221, LARGE=222, 
		LAST=223, LOGGED=224, MAIN=225, MATCH=226, MAXVALUE=227, MINVALUE=228, 
		NOTHING=229, NULLS=230, OBJECT=231, OIDS=232, ONLY=233, OVER=234, OWNED=235, 
		OWNER=236, PARTIAL=237, PLAIN=238, PRECEDING=239, RANGE=240, RENAME=241, 
		REPLICA=242, RESET=243, RESTART=244, RESTRICT=245, ROUTINE=246, RULE=247, 
		SECURITY=248, SEQUENCE=249, SESSION=250, SESSION_USER=251, SHOW=252, SIMPLE=253, 
		STATISTICS=254, STORAGE=255, TABLESPACE=256, TEMP=257, TEMPORARY=258, 
		UNBOUNDED=259, UNLOGGED=260, USAGE=261, VALID=262, VALIDATE=263, WITHIN=264, 
		WITHOUT=265, ZONE=266, OF=267, UESCAPE=268, GROUPS=269, RECURSIVE=270, 
		INT=271, INT2=272, INT4=273, INT8=274, FLOAT=275, FLOAT4=276, FLOAT8=277, 
		SMALLSERIAL=278, SERIAL=279, BIGSERIAL=280, MONEY=281, VARCHAR=282, BYTEA=283, 
		ENUM=284, POINT=285, LINE=286, LSEG=287, BOX=288, PATH=289, POLYGON=290, 
		CIRCLE=291, CIDR=292, INET=293, MACADDR=294, MACADDR8=295, BIT=296, VARBIT=297, 
		TSVECTOR=298, TSQUERY=299, XML=300, JSON=301, INT4RANGE=302, INT8RANGE=303, 
		NUMRANGE=304, TSRANGE=305, TSTZRANGE=306, DATERANGE=307, TABLESAMPLE=308, 
		ORDINALITY=309, CURRENT_ROLE=310, CURRENT_CATALOG=311, CURRENT_SCHEMA=312, 
		NORMALIZE=313, OVERLAY=314, XMLCONCAT=315, XMLELEMENT=316, XMLEXISTS=317, 
		XMLFOREST=318, XMLPARSE=319, XMLPI=320, XMLROOT=321, XMLSERIALIZE=322, 
		TREAT=323, SETOF=324, NFC=325, NFD=326, NFKC=327, NFKD=328, XMLATTRIBUTES=329, 
		REF=330, PASSING=331, VERSION=332, YES=333, STANDALONE=334, GREATEST=335, 
		LEAST=336, MATERIALIZED=337, OPERATOR=338, SHARE=339, ROLLUP=340, ILIKE=341, 
		SIMILAR=342, ISNULL=343, NOTNULL=344, SYMMETRIC=345, DOCUMENT=346, NORMALIZED=347, 
		ASYMMETRIC=348, VARIADIC=349, NOWAIT=350, LOCKED=351, XMLTABLE=352, COLUMNS=353, 
		CONTENT=354, STRIP=355, WHITESPACE=356, XMLNAMESPACES=357, PLACING=358, 
		RETURNING=359, LATERAL=360, NONE=361, ANALYSE=362, ANALYZE=363, CONFLICT=364, 
		OVERRIDING=365, SYSTEM=366, ABORT=367, ABSOLUTE=368, ACCESS=369, AFTER=370, 
		AGGREGATE=371, ALSO=372, ATTACH=373, ATTRIBUTE=374, BACKWARD=375, BEFORE=376, 
		ASSERTION=377, ASSIGNMENT=378, CONTINUE=379, CONVERSION=380, COPY=381, 
		COST=382, CSV=383, CALLED=384, CATALOG=385, CHAIN=386, CHECKPOINT=387, 
		CLASS=388, CONFIGURATION=389, COMMENT=390, DETACH=391, DICTIONARY=392, 
		EXPRESSION=393, INSENSITIVE=394, DISCARD=395, OFF=396, INSTEAD=397, EXPLAIN=398, 
		INPUT=399, INLINE=400, PARALLEL=401, LEAKPROOF=402, COMMITTED=403, ENCODING=404, 
		IMPLICIT=405, DELIMITER=406, CURSOR=407, EACH=408, EVENT=409, DEALLOCATE=410, 
		CONNECTION=411, DECLARE=412, FAMILY=413, FORWARD=414, EXCLUSIVE=415, FUNCTIONS=416, 
		LOCATION=417, LABEL=418, DELIMITERS=419, HANDLER=420, HEADER=421, IMMUTABLE=422, 
		GRANTED=423, HOLD=424, MAPPING=425, OLD=426, METHOD=427, LOAD=428, LISTEN=429, 
		MODE=430, MOVE=431, PROCEDURAL=432, PARSER=433, PROCEDURES=434, ENCRYPTED=435, 
		PUBLICATION=436, PROGRAM=437, REFERENCING=438, PLANS=439, REINDEX=440, 
		PRIOR=441, PASSWORD=442, RELATIVE=443, QUOTE=444, ROUTINES=445, REPLACE=446, 
		SNAPSHOT=447, REFRESH=448, PREPARE=449, OPTIONS=450, IMPORT=451, INVOKER=452, 
		NEW=453, PREPARED=454, SCROLL=455, SEQUENCES=456, SYSID=457, REASSIGN=458, 
		SERVER=459, SUBSCRIPTION=460, SEARCH=461, SCHEMAS=462, RECHECK=463, POLICY=464, 
		NOTIFY=465, LOCK=466, RELEASE=467, SERIALIZABLE=468, RETURNS=469, STATEMENT=470, 
		STDIN=471, STDOUT=472, TABLES=473, SUPPORT=474, STABLE=475, TEMPLATE=476, 
		UNENCRYPTED=477, VIEWS=478, UNCOMMITTED=479, TRANSFORM=480, UNLISTEN=481, 
		TRUSTED=482, VALIDATOR=483, UNTIL=484, VACUUM=485, VOLATILE=486, STORED=487, 
		WRITE=488, STRICT=489, TYPES=490, WRAPPER=491, WORK=492, FREEZE=493, AUTHORIZATION=494, 
		VERBOSE=495, PARAM=496, OUT=497, INOUT=498, AND_=499, OR_=500, NOT_=501, 
		TILDE_=502, VERTICAL_BAR_=503, AMPERSAND_=504, SIGNED_LEFT_SHIFT_=505, 
		SIGNED_RIGHT_SHIFT_=506, CARET_=507, MOD_=508, COLON_=509, PLUS_=510, 
		MINUS_=511, ASTERISK_=512, SLASH_=513, BACKSLASH_=514, DOT_=515, DOT_ASTERISK_=516, 
		SAFE_EQ_=517, DEQ_=518, EQ_=519, CQ_=520, NEQ_=521, GT_=522, GTE_=523, 
		LT_=524, LTE_=525, POUND_=526, LP_=527, RP_=528, LBE_=529, RBE_=530, LBT_=531, 
		RBT_=532, COMMA_=533, DQ_=534, SQ_=535, BQ_=536, QUESTION_=537, AT_=538, 
		SEMI_=539, TILDE_TILDE_=540, NOT_TILDE_TILDE_=541, TYPE_CAST_=542, ILIKE_=543, 
		NOT_ILIKE_=544, JSON_EXTRACT_=545, JSON_EXTRACT_TEXT_=546, JSON_PATH_EXTRACT_=547, 
		JSON_PATH_EXTRACT_TEXT_=548, JSONB_CONTAIN_RIGHT_=549, JSONB_CONTAIN_LEFT_=550, 
		JSONB_CONTAIN_ALL_TOP_KEY_=551, JSONB_PATH_DELETE_=552, JSONB_PATH_CONTAIN_ANY_VALUE_=553, 
		JSONB_PATH_PREDICATE_CHECK_=554, IDENTIFIER_=555, STRING_=556, NUMBER_=557, 
		HEX_DIGIT_=558, BIT_NUM_=559;
	public static final int
		RULE_parameterMarker = 0, RULE_reservedKeyword = 1, RULE_numberLiterals = 2, 
		RULE_literalsType = 3, RULE_identifier = 4, RULE_unicodeEscapes = 5, RULE_uescape = 6, 
		RULE_unreservedWord = 7, RULE_typeFuncNameKeyword = 8, RULE_schemaName = 9, 
		RULE_tableName = 10, RULE_columnName = 11, RULE_owner = 12, RULE_name = 13, 
		RULE_tableNames = 14, RULE_columnNames = 15, RULE_collationName = 16, 
		RULE_indexName = 17, RULE_alias = 18, RULE_primaryKey = 19, RULE_logicalOperator = 20, 
		RULE_comparisonOperator = 21, RULE_patternMatchingOperator = 22, RULE_cursorName = 23, 
		RULE_aExpr = 24, RULE_bExpr = 25, RULE_cExpr = 26, RULE_indirection = 27, 
		RULE_optIndirection = 28, RULE_indirectionEl = 29, RULE_sliceBound = 30, 
		RULE_inExpr = 31, RULE_caseExpr = 32, RULE_whenClauseList = 33, RULE_whenClause = 34, 
		RULE_caseDefault = 35, RULE_caseArg = 36, RULE_columnref = 37, RULE_qualOp = 38, 
		RULE_subqueryOp = 39, RULE_allOp = 40, RULE_op = 41, RULE_mathOperator = 42, 
		RULE_jsonOperator = 43, RULE_qualAllOp = 44, RULE_ascDesc = 45, RULE_anyOperator = 46, 
		RULE_frameClause = 47, RULE_frameExtent = 48, RULE_frameBound = 49, RULE_windowExclusionClause = 50, 
		RULE_row = 51, RULE_explicitRow = 52, RULE_implicitRow = 53, RULE_subType = 54, 
		RULE_arrayExpr = 55, RULE_arrayExprList = 56, RULE_funcArgList = 57, RULE_paramName = 58, 
		RULE_funcArgExpr = 59, RULE_typeList = 60, RULE_funcApplication = 61, 
		RULE_funcName = 62, RULE_aexprConst = 63, RULE_qualifiedName = 64, RULE_colId = 65, 
		RULE_typeFunctionName = 66, RULE_functionTable = 67, RULE_xmlTable = 68, 
		RULE_xmlTableColumnList = 69, RULE_xmlTableColumnEl = 70, RULE_xmlTableColumnOptionList = 71, 
		RULE_xmlTableColumnOptionEl = 72, RULE_xmlNamespaceList = 73, RULE_xmlNamespaceEl = 74, 
		RULE_funcExpr = 75, RULE_withinGroupClause = 76, RULE_filterClause = 77, 
		RULE_functionExprWindowless = 78, RULE_ordinality = 79, RULE_functionExprCommonSubexpr = 80, 
		RULE_typeName = 81, RULE_simpleTypeName = 82, RULE_exprList = 83, RULE_extractList = 84, 
		RULE_extractArg = 85, RULE_genericType = 86, RULE_typeModifiers = 87, 
		RULE_numeric = 88, RULE_constDatetime = 89, RULE_timezone = 90, RULE_character = 91, 
		RULE_characterWithLength = 92, RULE_characterWithoutLength = 93, RULE_characterClause = 94, 
		RULE_optFloat = 95, RULE_attrs = 96, RULE_attrName = 97, RULE_colLable = 98, 
		RULE_bit = 99, RULE_bitWithLength = 100, RULE_bitWithoutLength = 101, 
		RULE_constInterval = 102, RULE_optInterval = 103, RULE_optArrayBounds = 104, 
		RULE_intervalSecond = 105, RULE_unicodeNormalForm = 106, RULE_trimList = 107, 
		RULE_overlayList = 108, RULE_overlayPlacing = 109, RULE_substrFrom = 110, 
		RULE_substrFor = 111, RULE_positionList = 112, RULE_substrList = 113, 
		RULE_xmlAttributes = 114, RULE_xmlAttributeList = 115, RULE_xmlAttributeEl = 116, 
		RULE_xmlExistsArgument = 117, RULE_xmlPassingMech = 118, RULE_documentOrContent = 119, 
		RULE_xmlWhitespaceOption = 120, RULE_xmlRootVersion = 121, RULE_xmlRootStandalone = 122, 
		RULE_rowsFromItem = 123, RULE_rowsFromList = 124, RULE_columnDefList = 125, 
		RULE_tableFuncElementList = 126, RULE_tableFuncElement = 127, RULE_collateClause = 128, 
		RULE_anyName = 129, RULE_aliasClause = 130, RULE_nameList = 131, RULE_funcAliasClause = 132, 
		RULE_tablesampleClause = 133, RULE_repeatableClause = 134, RULE_allOrDistinct = 135, 
		RULE_sortClause = 136, RULE_sortbyList = 137, RULE_sortby = 138, RULE_nullsOrder = 139, 
		RULE_distinctClause = 140, RULE_distinct = 141, RULE_overClause = 142, 
		RULE_windowSpecification = 143, RULE_windowName = 144, RULE_partitionClause = 145, 
		RULE_indexParams = 146, RULE_indexElemOptions = 147, RULE_indexElem = 148, 
		RULE_collate = 149, RULE_optClass = 150, RULE_reloptions = 151, RULE_reloptionList = 152, 
		RULE_reloptionElem = 153, RULE_defArg = 154, RULE_funcType = 155, RULE_selectWithParens = 156, 
		RULE_dataType = 157, RULE_dataTypeName = 158, RULE_dataTypeLength = 159, 
		RULE_characterSet = 160, RULE_ignoredIdentifier = 161, RULE_ignoredIdentifiers = 162, 
		RULE_signedIconst = 163, RULE_booleanOrString = 164, RULE_nonReservedWord = 165, 
		RULE_colNameKeyword = 166, RULE_databaseName = 167, RULE_roleSpec = 168, 
		RULE_varName = 169, RULE_varList = 170, RULE_varValue = 171, RULE_zoneValue = 172, 
		RULE_numericOnly = 173, RULE_isoLevel = 174, RULE_columnDef = 175, RULE_colQualList = 176, 
		RULE_colConstraint = 177, RULE_constraintAttr = 178, RULE_colConstraintElem = 179, 
		RULE_parenthesizedSeqOptList = 180, RULE_seqOptList = 181, RULE_seqOptElem = 182, 
		RULE_optColumnList = 183, RULE_columnElem = 184, RULE_columnList = 185, 
		RULE_generatedWhen = 186, RULE_noInherit = 187, RULE_consTableSpace = 188, 
		RULE_definition = 189, RULE_defList = 190, RULE_defElem = 191, RULE_colLabel = 192, 
		RULE_keyActions = 193, RULE_keyDelete = 194, RULE_keyUpdate = 195, RULE_keyAction = 196, 
		RULE_keyMatch = 197, RULE_createGenericOptions = 198, RULE_genericOptionList = 199, 
		RULE_genericOptionElem = 200, RULE_genericOptionArg = 201, RULE_genericOptionName = 202, 
		RULE_replicaIdentity = 203, RULE_operArgtypes = 204, RULE_funcArg = 205, 
		RULE_argClass = 206, RULE_funcArgsList = 207, RULE_nonReservedWordOrSconst = 208, 
		RULE_fileName = 209, RULE_roleList = 210, RULE_setResetClause = 211, RULE_setRest = 212, 
		RULE_transactionModeList = 213, RULE_transactionModeItem = 214, RULE_setRestMore = 215, 
		RULE_encoding = 216, RULE_genericSet = 217, RULE_variableResetStmt = 218, 
		RULE_resetRest = 219, RULE_genericReset = 220, RULE_relationExprList = 221, 
		RULE_relationExpr = 222, RULE_commonFuncOptItem = 223, RULE_functionSetResetClause = 224, 
		RULE_rowSecurityCmd = 225, RULE_event = 226, RULE_typeNameList = 227;
	private static String[] makeRuleNames() {
		return new String[] {
			"parameterMarker", "reservedKeyword", "numberLiterals", "literalsType", 
			"identifier", "unicodeEscapes", "uescape", "unreservedWord", "typeFuncNameKeyword", 
			"schemaName", "tableName", "columnName", "owner", "name", "tableNames", 
			"columnNames", "collationName", "indexName", "alias", "primaryKey", "logicalOperator", 
			"comparisonOperator", "patternMatchingOperator", "cursorName", "aExpr", 
			"bExpr", "cExpr", "indirection", "optIndirection", "indirectionEl", "sliceBound", 
			"inExpr", "caseExpr", "whenClauseList", "whenClause", "caseDefault", 
			"caseArg", "columnref", "qualOp", "subqueryOp", "allOp", "op", "mathOperator", 
			"jsonOperator", "qualAllOp", "ascDesc", "anyOperator", "frameClause", 
			"frameExtent", "frameBound", "windowExclusionClause", "row", "explicitRow", 
			"implicitRow", "subType", "arrayExpr", "arrayExprList", "funcArgList", 
			"paramName", "funcArgExpr", "typeList", "funcApplication", "funcName", 
			"aexprConst", "qualifiedName", "colId", "typeFunctionName", "functionTable", 
			"xmlTable", "xmlTableColumnList", "xmlTableColumnEl", "xmlTableColumnOptionList", 
			"xmlTableColumnOptionEl", "xmlNamespaceList", "xmlNamespaceEl", "funcExpr", 
			"withinGroupClause", "filterClause", "functionExprWindowless", "ordinality", 
			"functionExprCommonSubexpr", "typeName", "simpleTypeName", "exprList", 
			"extractList", "extractArg", "genericType", "typeModifiers", "numeric", 
			"constDatetime", "timezone", "character", "characterWithLength", "characterWithoutLength", 
			"characterClause", "optFloat", "attrs", "attrName", "colLable", "bit", 
			"bitWithLength", "bitWithoutLength", "constInterval", "optInterval", 
			"optArrayBounds", "intervalSecond", "unicodeNormalForm", "trimList", 
			"overlayList", "overlayPlacing", "substrFrom", "substrFor", "positionList", 
			"substrList", "xmlAttributes", "xmlAttributeList", "xmlAttributeEl", 
			"xmlExistsArgument", "xmlPassingMech", "documentOrContent", "xmlWhitespaceOption", 
			"xmlRootVersion", "xmlRootStandalone", "rowsFromItem", "rowsFromList", 
			"columnDefList", "tableFuncElementList", "tableFuncElement", "collateClause", 
			"anyName", "aliasClause", "nameList", "funcAliasClause", "tablesampleClause", 
			"repeatableClause", "allOrDistinct", "sortClause", "sortbyList", "sortby", 
			"nullsOrder", "distinctClause", "distinct", "overClause", "windowSpecification", 
			"windowName", "partitionClause", "indexParams", "indexElemOptions", "indexElem", 
			"collate", "optClass", "reloptions", "reloptionList", "reloptionElem", 
			"defArg", "funcType", "selectWithParens", "dataType", "dataTypeName", 
			"dataTypeLength", "characterSet", "ignoredIdentifier", "ignoredIdentifiers", 
			"signedIconst", "booleanOrString", "nonReservedWord", "colNameKeyword", 
			"databaseName", "roleSpec", "varName", "varList", "varValue", "zoneValue", 
			"numericOnly", "isoLevel", "columnDef", "colQualList", "colConstraint", 
			"constraintAttr", "colConstraintElem", "parenthesizedSeqOptList", "seqOptList", 
			"seqOptElem", "optColumnList", "columnElem", "columnList", "generatedWhen", 
			"noInherit", "consTableSpace", "definition", "defList", "defElem", "colLabel", 
			"keyActions", "keyDelete", "keyUpdate", "keyAction", "keyMatch", "createGenericOptions", 
			"genericOptionList", "genericOptionElem", "genericOptionArg", "genericOptionName", 
			"replicaIdentity", "operArgtypes", "funcArg", "argClass", "funcArgsList", 
			"nonReservedWordOrSconst", "fileName", "roleList", "setResetClause", 
			"setRest", "transactionModeList", "transactionModeItem", "setRestMore", 
			"encoding", "genericSet", "variableResetStmt", "resetRest", "genericReset", 
			"relationExprList", "relationExpr", "commonFuncOptItem", "functionSetResetClause", 
			"rowSecurityCmd", "event", "typeNameList"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'U'", "'u'", "'Default does not match anything'", null, null, 
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
			null, null, null, null, null, null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'", 
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
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, "'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", 
			"'>>'", "'^'", "'%'", "':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", 
			"'.*'", "'<=>'", "'=='", "'='", "':='", null, "'>'", "'>='", "'<'", "'<='", 
			"'#'", "'('", "')'", "'{'", "'}'", "'['", "']'", "','", "'\"'", "'''", 
			"'`'", "'?'", "'@'", "';'", "'~~'", "'!~~'", "'::'", "'~~*'", "'!~~*'", 
			"'->'", "'->>'", "'#>'", "'#>>'", "'@>'", "'<@'", "'?&'", "'#-'", "'@?'", 
			"'@@'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "WS", "SELECT", "INSERT", "UPDATE", "DELETE", 
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
			"MICROSECOND", "DEFAULT", "CURRENT", "ENABLE", "DISABLE", "CALL", "INSTANCE", 
			"PRESERVE", "DO", "DEFINER", "CURRENT_USER", "SQL", "CASCADED", "LOCAL", 
			"CLOSE", "OPEN", "NEXT", "NAME", "COLLATION", "NAMES", "INTEGER", "REAL", 
			"DECIMAL", "TYPE", "SMALLINT", "BIGINT", "NUMERIC", "TEXT", "REPEATABLE", 
			"CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "NULLIF", "VARYING", 
			"NATIONAL", "NCHAR", "VALUE", "BOTH", "LEADING", "TRAILING", "COALESCE", 
			"INTERSECT", "EXCEPT", "TIES", "FETCH", "CUBE", "GROUPING", "SETS", "WINDOW", 
			"OTHERS", "OVERLAPS", "SOME", "AT", "DEC", "END", "FOR_GENERATOR", "ADMIN", 
			"BINARY", "ESCAPE", "EXCLUDE", "MOD", "PARTITION", "ROW", "UNKNOWN", 
			"ALWAYS", "CASCADE", "CHECK", "GENERATED", "ISOLATION", "LEVEL", "NO", 
			"OPTION", "PRIVILEGES", "READ", "REFERENCES", "ROLE", "ROWS", "START", 
			"TRANSACTION", "USER", "ACTION", "CACHE", "CHARACTERISTICS", "CLUSTER", 
			"COLLATE", "COMMENTS", "CONCURRENTLY", "CONNECT", "CONSTRAINTS", "CYCLE", 
			"DATA", "DATABASE", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEPENDS", 
			"DOMAIN", "EXCLUDING", "EXECUTE", "EXTENDED", "EXTENSION", "EXTERNAL", 
			"EXTRACT", "FILTER", "FIRST", "FOLLOWING", "FORCE", "GLOBAL", "IDENTITY", 
			"IMMEDIATE", "INCLUDING", "INCREMENT", "INDEXES", "INHERIT", "INHERITS", 
			"INITIALLY", "INCLUDE", "LANGUAGE", "LARGE", "LAST", "LOGGED", "MAIN", 
			"MATCH", "MAXVALUE", "MINVALUE", "NOTHING", "NULLS", "OBJECT", "OIDS", 
			"ONLY", "OVER", "OWNED", "OWNER", "PARTIAL", "PLAIN", "PRECEDING", "RANGE", 
			"RENAME", "REPLICA", "RESET", "RESTART", "RESTRICT", "ROUTINE", "RULE", 
			"SECURITY", "SEQUENCE", "SESSION", "SESSION_USER", "SHOW", "SIMPLE", 
			"STATISTICS", "STORAGE", "TABLESPACE", "TEMP", "TEMPORARY", "UNBOUNDED", 
			"UNLOGGED", "USAGE", "VALID", "VALIDATE", "WITHIN", "WITHOUT", "ZONE", 
			"OF", "UESCAPE", "GROUPS", "RECURSIVE", "INT", "INT2", "INT4", "INT8", 
			"FLOAT", "FLOAT4", "FLOAT8", "SMALLSERIAL", "SERIAL", "BIGSERIAL", "MONEY", 
			"VARCHAR", "BYTEA", "ENUM", "POINT", "LINE", "LSEG", "BOX", "PATH", "POLYGON", 
			"CIRCLE", "CIDR", "INET", "MACADDR", "MACADDR8", "BIT", "VARBIT", "TSVECTOR", 
			"TSQUERY", "XML", "JSON", "INT4RANGE", "INT8RANGE", "NUMRANGE", "TSRANGE", 
			"TSTZRANGE", "DATERANGE", "TABLESAMPLE", "ORDINALITY", "CURRENT_ROLE", 
			"CURRENT_CATALOG", "CURRENT_SCHEMA", "NORMALIZE", "OVERLAY", "XMLCONCAT", 
			"XMLELEMENT", "XMLEXISTS", "XMLFOREST", "XMLPARSE", "XMLPI", "XMLROOT", 
			"XMLSERIALIZE", "TREAT", "SETOF", "NFC", "NFD", "NFKC", "NFKD", "XMLATTRIBUTES", 
			"REF", "PASSING", "VERSION", "YES", "STANDALONE", "GREATEST", "LEAST", 
			"MATERIALIZED", "OPERATOR", "SHARE", "ROLLUP", "ILIKE", "SIMILAR", "ISNULL", 
			"NOTNULL", "SYMMETRIC", "DOCUMENT", "NORMALIZED", "ASYMMETRIC", "VARIADIC", 
			"NOWAIT", "LOCKED", "XMLTABLE", "COLUMNS", "CONTENT", "STRIP", "WHITESPACE", 
			"XMLNAMESPACES", "PLACING", "RETURNING", "LATERAL", "NONE", "ANALYSE", 
			"ANALYZE", "CONFLICT", "OVERRIDING", "SYSTEM", "ABORT", "ABSOLUTE", "ACCESS", 
			"AFTER", "AGGREGATE", "ALSO", "ATTACH", "ATTRIBUTE", "BACKWARD", "BEFORE", 
			"ASSERTION", "ASSIGNMENT", "CONTINUE", "CONVERSION", "COPY", "COST", 
			"CSV", "CALLED", "CATALOG", "CHAIN", "CHECKPOINT", "CLASS", "CONFIGURATION", 
			"COMMENT", "DETACH", "DICTIONARY", "EXPRESSION", "INSENSITIVE", "DISCARD", 
			"OFF", "INSTEAD", "EXPLAIN", "INPUT", "INLINE", "PARALLEL", "LEAKPROOF", 
			"COMMITTED", "ENCODING", "IMPLICIT", "DELIMITER", "CURSOR", "EACH", "EVENT", 
			"DEALLOCATE", "CONNECTION", "DECLARE", "FAMILY", "FORWARD", "EXCLUSIVE", 
			"FUNCTIONS", "LOCATION", "LABEL", "DELIMITERS", "HANDLER", "HEADER", 
			"IMMUTABLE", "GRANTED", "HOLD", "MAPPING", "OLD", "METHOD", "LOAD", "LISTEN", 
			"MODE", "MOVE", "PROCEDURAL", "PARSER", "PROCEDURES", "ENCRYPTED", "PUBLICATION", 
			"PROGRAM", "REFERENCING", "PLANS", "REINDEX", "PRIOR", "PASSWORD", "RELATIVE", 
			"QUOTE", "ROUTINES", "REPLACE", "SNAPSHOT", "REFRESH", "PREPARE", "OPTIONS", 
			"IMPORT", "INVOKER", "NEW", "PREPARED", "SCROLL", "SEQUENCES", "SYSID", 
			"REASSIGN", "SERVER", "SUBSCRIPTION", "SEARCH", "SCHEMAS", "RECHECK", 
			"POLICY", "NOTIFY", "LOCK", "RELEASE", "SERIALIZABLE", "RETURNS", "STATEMENT", 
			"STDIN", "STDOUT", "TABLES", "SUPPORT", "STABLE", "TEMPLATE", "UNENCRYPTED", 
			"VIEWS", "UNCOMMITTED", "TRANSFORM", "UNLISTEN", "TRUSTED", "VALIDATOR", 
			"UNTIL", "VACUUM", "VOLATILE", "STORED", "WRITE", "STRICT", "TYPES", 
			"WRAPPER", "WORK", "FREEZE", "AUTHORIZATION", "VERBOSE", "PARAM", "OUT", 
			"INOUT", "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "CQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", 
			"POUND_", "LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", 
			"SQ_", "BQ_", "QUESTION_", "AT_", "SEMI_", "TILDE_TILDE_", "NOT_TILDE_TILDE_", 
			"TYPE_CAST_", "ILIKE_", "NOT_ILIKE_", "JSON_EXTRACT_", "JSON_EXTRACT_TEXT_", 
			"JSON_PATH_EXTRACT_", "JSON_PATH_EXTRACT_TEXT_", "JSONB_CONTAIN_RIGHT_", 
			"JSONB_CONTAIN_LEFT_", "JSONB_CONTAIN_ALL_TOP_KEY_", "JSONB_PATH_DELETE_", 
			"JSONB_PATH_CONTAIN_ANY_VALUE_", "JSONB_PATH_PREDICATE_CHECK_", "IDENTIFIER_", 
			"STRING_", "NUMBER_", "HEX_DIGIT_", "BIT_NUM_"
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
		public LiteralsTypeContext literalsType() {
			return getRuleContext(LiteralsTypeContext.class,0);
		}
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
			setState(456);
			match(QUESTION_);
			setState(458);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(457);
				literalsType();
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

	public static class ReservedKeywordContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public TerminalNode ANALYSE() { return getToken(BaseRuleParser.ANALYSE, 0); }
		public TerminalNode ANALYZE() { return getToken(BaseRuleParser.ANALYZE, 0); }
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public TerminalNode ANY() { return getToken(BaseRuleParser.ANY, 0); }
		public TerminalNode ARRAY() { return getToken(BaseRuleParser.ARRAY, 0); }
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public TerminalNode ASC() { return getToken(BaseRuleParser.ASC, 0); }
		public TerminalNode ASYMMETRIC() { return getToken(BaseRuleParser.ASYMMETRIC, 0); }
		public TerminalNode BOTH() { return getToken(BaseRuleParser.BOTH, 0); }
		public TerminalNode CASE() { return getToken(BaseRuleParser.CASE, 0); }
		public TerminalNode CAST() { return getToken(BaseRuleParser.CAST, 0); }
		public TerminalNode CHECK() { return getToken(BaseRuleParser.CHECK, 0); }
		public TerminalNode COLLATE() { return getToken(BaseRuleParser.COLLATE, 0); }
		public TerminalNode COLUMN() { return getToken(BaseRuleParser.COLUMN, 0); }
		public TerminalNode CONSTRAINT() { return getToken(BaseRuleParser.CONSTRAINT, 0); }
		public TerminalNode CREATE() { return getToken(BaseRuleParser.CREATE, 0); }
		public TerminalNode CURRENT_CATALOG() { return getToken(BaseRuleParser.CURRENT_CATALOG, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(BaseRuleParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_ROLE() { return getToken(BaseRuleParser.CURRENT_ROLE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(BaseRuleParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(BaseRuleParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(BaseRuleParser.CURRENT_USER, 0); }
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public TerminalNode DEFERRABLE() { return getToken(BaseRuleParser.DEFERRABLE, 0); }
		public TerminalNode DESC() { return getToken(BaseRuleParser.DESC, 0); }
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public TerminalNode DO() { return getToken(BaseRuleParser.DO, 0); }
		public TerminalNode ELSE() { return getToken(BaseRuleParser.ELSE, 0); }
		public TerminalNode END() { return getToken(BaseRuleParser.END, 0); }
		public TerminalNode EXCEPT() { return getToken(BaseRuleParser.EXCEPT, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public TerminalNode FETCH() { return getToken(BaseRuleParser.FETCH, 0); }
		public TerminalNode FOR() { return getToken(BaseRuleParser.FOR, 0); }
		public TerminalNode FOREIGN() { return getToken(BaseRuleParser.FOREIGN, 0); }
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public TerminalNode GRANT() { return getToken(BaseRuleParser.GRANT, 0); }
		public TerminalNode GROUP() { return getToken(BaseRuleParser.GROUP, 0); }
		public TerminalNode HAVING() { return getToken(BaseRuleParser.HAVING, 0); }
		public TerminalNode IN() { return getToken(BaseRuleParser.IN, 0); }
		public TerminalNode INITIALLY() { return getToken(BaseRuleParser.INITIALLY, 0); }
		public TerminalNode INTERSECT() { return getToken(BaseRuleParser.INTERSECT, 0); }
		public TerminalNode INTO() { return getToken(BaseRuleParser.INTO, 0); }
		public TerminalNode LATERAL() { return getToken(BaseRuleParser.LATERAL, 0); }
		public TerminalNode LEADING() { return getToken(BaseRuleParser.LEADING, 0); }
		public TerminalNode LIMIT() { return getToken(BaseRuleParser.LIMIT, 0); }
		public TerminalNode LOCALTIME() { return getToken(BaseRuleParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(BaseRuleParser.LOCALTIMESTAMP, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public TerminalNode OFFSET() { return getToken(BaseRuleParser.OFFSET, 0); }
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode ONLY() { return getToken(BaseRuleParser.ONLY, 0); }
		public TerminalNode OR() { return getToken(BaseRuleParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(BaseRuleParser.ORDER, 0); }
		public TerminalNode PLACING() { return getToken(BaseRuleParser.PLACING, 0); }
		public TerminalNode PRIMARY() { return getToken(BaseRuleParser.PRIMARY, 0); }
		public TerminalNode REFERENCES() { return getToken(BaseRuleParser.REFERENCES, 0); }
		public TerminalNode RETURNING() { return getToken(BaseRuleParser.RETURNING, 0); }
		public TerminalNode SELECT() { return getToken(BaseRuleParser.SELECT, 0); }
		public TerminalNode SESSION_USER() { return getToken(BaseRuleParser.SESSION_USER, 0); }
		public TerminalNode SOME() { return getToken(BaseRuleParser.SOME, 0); }
		public TerminalNode SYMMETRIC() { return getToken(BaseRuleParser.SYMMETRIC, 0); }
		public TerminalNode TABLE() { return getToken(BaseRuleParser.TABLE, 0); }
		public TerminalNode THEN() { return getToken(BaseRuleParser.THEN, 0); }
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public TerminalNode TRAILING() { return getToken(BaseRuleParser.TRAILING, 0); }
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode UNION() { return getToken(BaseRuleParser.UNION, 0); }
		public TerminalNode UNIQUE() { return getToken(BaseRuleParser.UNIQUE, 0); }
		public TerminalNode USER() { return getToken(BaseRuleParser.USER, 0); }
		public TerminalNode USING() { return getToken(BaseRuleParser.USING, 0); }
		public TerminalNode VARIADIC() { return getToken(BaseRuleParser.VARIADIC, 0); }
		public TerminalNode WHEN() { return getToken(BaseRuleParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(BaseRuleParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(BaseRuleParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public ReservedKeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reservedKeyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterReservedKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitReservedKeyword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitReservedKeyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReservedKeywordContext reservedKeyword() throws RecognitionException {
		ReservedKeywordContext _localctx = new ReservedKeywordContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_reservedKeyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(460);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SELECT) | (1L << CREATE) | (1L << GRANT) | (1L << TABLE) | (1L << COLUMN) | (1L << CONSTRAINT) | (1L << PRIMARY) | (1L << UNIQUE) | (1L << FOREIGN) | (1L << INTO) | (1L << WITH) | (1L << UNION) | (1L << DISTINCT) | (1L << CASE) | (1L << WHEN) | (1L << CAST) | (1L << FROM) | (1L << USING) | (1L << WHERE) | (1L << AS) | (1L << ON) | (1L << ELSE) | (1L << THEN) | (1L << FOR) | (1L << TO) | (1L << AND) | (1L << OR) | (1L << NOT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (NULL - 64)) | (1L << (TRUE - 64)) | (1L << (FALSE - 64)) | (1L << (IN - 64)) | (1L << (ALL - 64)) | (1L << (ANY - 64)) | (1L << (ORDER - 64)) | (1L << (GROUP - 64)) | (1L << (ASC - 64)) | (1L << (DESC - 64)) | (1L << (HAVING - 64)) | (1L << (LIMIT - 64)) | (1L << (OFFSET - 64)) | (1L << (ARRAY - 64)) | (1L << (LOCALTIME - 64)) | (1L << (LOCALTIMESTAMP - 64)) | (1L << (DEFAULT - 64)) | (1L << (DO - 64)) | (1L << (CURRENT_USER - 64)))) != 0) || ((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (CURRENT_DATE - 133)) | (1L << (CURRENT_TIME - 133)) | (1L << (CURRENT_TIMESTAMP - 133)) | (1L << (BOTH - 133)) | (1L << (LEADING - 133)) | (1L << (TRAILING - 133)) | (1L << (INTERSECT - 133)) | (1L << (EXCEPT - 133)) | (1L << (FETCH - 133)) | (1L << (WINDOW - 133)) | (1L << (SOME - 133)) | (1L << (END - 133)) | (1L << (CHECK - 133)) | (1L << (REFERENCES - 133)) | (1L << (USER - 133)) | (1L << (COLLATE - 133)))) != 0) || ((((_la - 197)) & ~0x3f) == 0 && ((1L << (_la - 197)) & ((1L << (DEFERRABLE - 197)) | (1L << (INITIALLY - 197)) | (1L << (ONLY - 197)) | (1L << (SESSION_USER - 197)))) != 0) || ((((_la - 310)) & ~0x3f) == 0 && ((1L << (_la - 310)) & ((1L << (CURRENT_ROLE - 310)) | (1L << (CURRENT_CATALOG - 310)) | (1L << (SYMMETRIC - 310)) | (1L << (ASYMMETRIC - 310)) | (1L << (VARIADIC - 310)) | (1L << (PLACING - 310)) | (1L << (RETURNING - 310)) | (1L << (LATERAL - 310)) | (1L << (ANALYSE - 310)) | (1L << (ANALYZE - 310)))) != 0)) ) {
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

	public static class NumberLiteralsContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public LiteralsTypeContext literalsType() {
			return getRuleContext(LiteralsTypeContext.class,0);
		}
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
		enterRule(_localctx, 4, RULE_numberLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS_) {
				{
				setState(462);
				match(MINUS_);
				}
			}

			setState(465);
			match(NUMBER_);
			setState(467);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TYPE_CAST_) {
				{
				setState(466);
				literalsType();
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

	public static class LiteralsTypeContext extends ParserRuleContext {
		public TerminalNode TYPE_CAST_() { return getToken(BaseRuleParser.TYPE_CAST_, 0); }
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public LiteralsTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalsType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterLiteralsType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitLiteralsType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitLiteralsType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralsTypeContext literalsType() throws RecognitionException {
		LiteralsTypeContext _localctx = new LiteralsTypeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_literalsType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(469);
			match(TYPE_CAST_);
			setState(470);
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

	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(BaseRuleParser.IDENTIFIER_, 0); }
		public UnicodeEscapesContext unicodeEscapes() {
			return getRuleContext(UnicodeEscapesContext.class,0);
		}
		public UescapeContext uescape() {
			return getRuleContext(UescapeContext.class,0);
		}
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
		enterRule(_localctx, 8, RULE_identifier);
		int _la;
		try {
			setState(480);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(473);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0 || _la==T__1) {
					{
					setState(472);
					unicodeEscapes();
					}
				}

				setState(475);
				match(IDENTIFIER_);
				setState(477);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(476);
					uescape();
					}
					break;
				}
				}
				break;
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case IF:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case VARYING:
			case VALUE:
			case TIES:
			case CUBE:
			case SETS:
			case OTHERS:
			case AT:
			case ADMIN:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case ORDINALITY:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
				enterOuterAlt(_localctx, 2);
				{
				setState(479);
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

	public static class UnicodeEscapesContext extends ParserRuleContext {
		public TerminalNode AMPERSAND_() { return getToken(BaseRuleParser.AMPERSAND_, 0); }
		public UnicodeEscapesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unicodeEscapes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterUnicodeEscapes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitUnicodeEscapes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitUnicodeEscapes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnicodeEscapesContext unicodeEscapes() throws RecognitionException {
		UnicodeEscapesContext _localctx = new UnicodeEscapesContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_unicodeEscapes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
			_la = _input.LA(1);
			if ( !(_la==T__0 || _la==T__1) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(483);
			match(AMPERSAND_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UescapeContext extends ParserRuleContext {
		public TerminalNode UESCAPE() { return getToken(BaseRuleParser.UESCAPE, 0); }
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public UescapeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uescape; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterUescape(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitUescape(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitUescape(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UescapeContext uescape() throws RecognitionException {
		UescapeContext _localctx = new UescapeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_uescape);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(485);
			match(UESCAPE);
			setState(486);
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

	public static class UnreservedWordContext extends ParserRuleContext {
		public TerminalNode ABORT() { return getToken(BaseRuleParser.ABORT, 0); }
		public TerminalNode ABSOLUTE() { return getToken(BaseRuleParser.ABSOLUTE, 0); }
		public TerminalNode ACCESS() { return getToken(BaseRuleParser.ACCESS, 0); }
		public TerminalNode ACTION() { return getToken(BaseRuleParser.ACTION, 0); }
		public TerminalNode ADD() { return getToken(BaseRuleParser.ADD, 0); }
		public TerminalNode ADMIN() { return getToken(BaseRuleParser.ADMIN, 0); }
		public TerminalNode AFTER() { return getToken(BaseRuleParser.AFTER, 0); }
		public TerminalNode AGGREGATE() { return getToken(BaseRuleParser.AGGREGATE, 0); }
		public TerminalNode ALSO() { return getToken(BaseRuleParser.ALSO, 0); }
		public TerminalNode ALTER() { return getToken(BaseRuleParser.ALTER, 0); }
		public TerminalNode ALWAYS() { return getToken(BaseRuleParser.ALWAYS, 0); }
		public TerminalNode ASSERTION() { return getToken(BaseRuleParser.ASSERTION, 0); }
		public TerminalNode ASSIGNMENT() { return getToken(BaseRuleParser.ASSIGNMENT, 0); }
		public TerminalNode AT() { return getToken(BaseRuleParser.AT, 0); }
		public TerminalNode ATTACH() { return getToken(BaseRuleParser.ATTACH, 0); }
		public TerminalNode ATTRIBUTE() { return getToken(BaseRuleParser.ATTRIBUTE, 0); }
		public TerminalNode BACKWARD() { return getToken(BaseRuleParser.BACKWARD, 0); }
		public TerminalNode BEFORE() { return getToken(BaseRuleParser.BEFORE, 0); }
		public TerminalNode BEGIN() { return getToken(BaseRuleParser.BEGIN, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(BaseRuleParser.CACHE, 0); }
		public TerminalNode CALL() { return getToken(BaseRuleParser.CALL, 0); }
		public TerminalNode CALLED() { return getToken(BaseRuleParser.CALLED, 0); }
		public TerminalNode CASCADE() { return getToken(BaseRuleParser.CASCADE, 0); }
		public TerminalNode CASCADED() { return getToken(BaseRuleParser.CASCADED, 0); }
		public TerminalNode CATALOG() { return getToken(BaseRuleParser.CATALOG, 0); }
		public TerminalNode CHAIN() { return getToken(BaseRuleParser.CHAIN, 0); }
		public TerminalNode CHARACTERISTICS() { return getToken(BaseRuleParser.CHARACTERISTICS, 0); }
		public TerminalNode CHECKPOINT() { return getToken(BaseRuleParser.CHECKPOINT, 0); }
		public TerminalNode CLASS() { return getToken(BaseRuleParser.CLASS, 0); }
		public TerminalNode CLOSE() { return getToken(BaseRuleParser.CLOSE, 0); }
		public TerminalNode CLUSTER() { return getToken(BaseRuleParser.CLUSTER, 0); }
		public TerminalNode COLUMNS() { return getToken(BaseRuleParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(BaseRuleParser.COMMENT, 0); }
		public TerminalNode COMMENTS() { return getToken(BaseRuleParser.COMMENTS, 0); }
		public TerminalNode COMMIT() { return getToken(BaseRuleParser.COMMIT, 0); }
		public TerminalNode COMMITTED() { return getToken(BaseRuleParser.COMMITTED, 0); }
		public TerminalNode CONFIGURATION() { return getToken(BaseRuleParser.CONFIGURATION, 0); }
		public TerminalNode CONFLICT() { return getToken(BaseRuleParser.CONFLICT, 0); }
		public TerminalNode CONNECTION() { return getToken(BaseRuleParser.CONNECTION, 0); }
		public TerminalNode CONSTRAINTS() { return getToken(BaseRuleParser.CONSTRAINTS, 0); }
		public TerminalNode CONTENT() { return getToken(BaseRuleParser.CONTENT, 0); }
		public TerminalNode CONTINUE() { return getToken(BaseRuleParser.CONTINUE, 0); }
		public TerminalNode CONVERSION() { return getToken(BaseRuleParser.CONVERSION, 0); }
		public TerminalNode COPY() { return getToken(BaseRuleParser.COPY, 0); }
		public TerminalNode COST() { return getToken(BaseRuleParser.COST, 0); }
		public TerminalNode CSV() { return getToken(BaseRuleParser.CSV, 0); }
		public TerminalNode CUBE() { return getToken(BaseRuleParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(BaseRuleParser.CURRENT, 0); }
		public TerminalNode CURSOR() { return getToken(BaseRuleParser.CURSOR, 0); }
		public TerminalNode CYCLE() { return getToken(BaseRuleParser.CYCLE, 0); }
		public TerminalNode DATA() { return getToken(BaseRuleParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(BaseRuleParser.DATABASE, 0); }
		public TerminalNode DAY() { return getToken(BaseRuleParser.DAY, 0); }
		public TerminalNode DEALLOCATE() { return getToken(BaseRuleParser.DEALLOCATE, 0); }
		public TerminalNode DECLARE() { return getToken(BaseRuleParser.DECLARE, 0); }
		public TerminalNode DEFAULTS() { return getToken(BaseRuleParser.DEFAULTS, 0); }
		public TerminalNode DEFERRED() { return getToken(BaseRuleParser.DEFERRED, 0); }
		public TerminalNode DEFINER() { return getToken(BaseRuleParser.DEFINER, 0); }
		public TerminalNode DELETE() { return getToken(BaseRuleParser.DELETE, 0); }
		public TerminalNode DELIMITER() { return getToken(BaseRuleParser.DELIMITER, 0); }
		public TerminalNode DELIMITERS() { return getToken(BaseRuleParser.DELIMITERS, 0); }
		public TerminalNode DEPENDS() { return getToken(BaseRuleParser.DEPENDS, 0); }
		public TerminalNode DETACH() { return getToken(BaseRuleParser.DETACH, 0); }
		public TerminalNode DICTIONARY() { return getToken(BaseRuleParser.DICTIONARY, 0); }
		public TerminalNode DISABLE() { return getToken(BaseRuleParser.DISABLE, 0); }
		public TerminalNode DISCARD() { return getToken(BaseRuleParser.DISCARD, 0); }
		public TerminalNode DOCUMENT() { return getToken(BaseRuleParser.DOCUMENT, 0); }
		public TerminalNode DOMAIN() { return getToken(BaseRuleParser.DOMAIN, 0); }
		public TerminalNode DOUBLE() { return getToken(BaseRuleParser.DOUBLE, 0); }
		public TerminalNode DROP() { return getToken(BaseRuleParser.DROP, 0); }
		public TerminalNode EACH() { return getToken(BaseRuleParser.EACH, 0); }
		public TerminalNode ENABLE() { return getToken(BaseRuleParser.ENABLE, 0); }
		public TerminalNode ENCODING() { return getToken(BaseRuleParser.ENCODING, 0); }
		public TerminalNode ENCRYPTED() { return getToken(BaseRuleParser.ENCRYPTED, 0); }
		public TerminalNode ENUM() { return getToken(BaseRuleParser.ENUM, 0); }
		public TerminalNode ESCAPE() { return getToken(BaseRuleParser.ESCAPE, 0); }
		public TerminalNode EVENT() { return getToken(BaseRuleParser.EVENT, 0); }
		public TerminalNode EXCLUDE() { return getToken(BaseRuleParser.EXCLUDE, 0); }
		public TerminalNode EXCLUDING() { return getToken(BaseRuleParser.EXCLUDING, 0); }
		public TerminalNode EXCLUSIVE() { return getToken(BaseRuleParser.EXCLUSIVE, 0); }
		public TerminalNode EXECUTE() { return getToken(BaseRuleParser.EXECUTE, 0); }
		public TerminalNode EXPLAIN() { return getToken(BaseRuleParser.EXPLAIN, 0); }
		public TerminalNode EXPRESSION() { return getToken(BaseRuleParser.EXPRESSION, 0); }
		public TerminalNode EXTENSION() { return getToken(BaseRuleParser.EXTENSION, 0); }
		public TerminalNode EXTERNAL() { return getToken(BaseRuleParser.EXTERNAL, 0); }
		public TerminalNode FAMILY() { return getToken(BaseRuleParser.FAMILY, 0); }
		public TerminalNode FILTER() { return getToken(BaseRuleParser.FILTER, 0); }
		public TerminalNode FIRST() { return getToken(BaseRuleParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(BaseRuleParser.FOLLOWING, 0); }
		public TerminalNode FORCE() { return getToken(BaseRuleParser.FORCE, 0); }
		public TerminalNode FORWARD() { return getToken(BaseRuleParser.FORWARD, 0); }
		public TerminalNode FUNCTION() { return getToken(BaseRuleParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(BaseRuleParser.FUNCTIONS, 0); }
		public TerminalNode GENERATED() { return getToken(BaseRuleParser.GENERATED, 0); }
		public TerminalNode GLOBAL() { return getToken(BaseRuleParser.GLOBAL, 0); }
		public TerminalNode GRANTED() { return getToken(BaseRuleParser.GRANTED, 0); }
		public TerminalNode GROUPS() { return getToken(BaseRuleParser.GROUPS, 0); }
		public TerminalNode HANDLER() { return getToken(BaseRuleParser.HANDLER, 0); }
		public TerminalNode HEADER() { return getToken(BaseRuleParser.HEADER, 0); }
		public TerminalNode HOLD() { return getToken(BaseRuleParser.HOLD, 0); }
		public TerminalNode HOUR() { return getToken(BaseRuleParser.HOUR, 0); }
		public TerminalNode IDENTITY() { return getToken(BaseRuleParser.IDENTITY, 0); }
		public TerminalNode IF() { return getToken(BaseRuleParser.IF, 0); }
		public TerminalNode IMMEDIATE() { return getToken(BaseRuleParser.IMMEDIATE, 0); }
		public TerminalNode IMMUTABLE() { return getToken(BaseRuleParser.IMMUTABLE, 0); }
		public TerminalNode IMPLICIT() { return getToken(BaseRuleParser.IMPLICIT, 0); }
		public TerminalNode IMPORT() { return getToken(BaseRuleParser.IMPORT, 0); }
		public TerminalNode INCLUDE() { return getToken(BaseRuleParser.INCLUDE, 0); }
		public TerminalNode INCLUDING() { return getToken(BaseRuleParser.INCLUDING, 0); }
		public TerminalNode INCREMENT() { return getToken(BaseRuleParser.INCREMENT, 0); }
		public TerminalNode INDEX() { return getToken(BaseRuleParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(BaseRuleParser.INDEXES, 0); }
		public TerminalNode INHERIT() { return getToken(BaseRuleParser.INHERIT, 0); }
		public TerminalNode INHERITS() { return getToken(BaseRuleParser.INHERITS, 0); }
		public TerminalNode INLINE() { return getToken(BaseRuleParser.INLINE, 0); }
		public TerminalNode INPUT() { return getToken(BaseRuleParser.INPUT, 0); }
		public TerminalNode INSENSITIVE() { return getToken(BaseRuleParser.INSENSITIVE, 0); }
		public TerminalNode INSERT() { return getToken(BaseRuleParser.INSERT, 0); }
		public TerminalNode INSTEAD() { return getToken(BaseRuleParser.INSTEAD, 0); }
		public TerminalNode INVOKER() { return getToken(BaseRuleParser.INVOKER, 0); }
		public TerminalNode ISOLATION() { return getToken(BaseRuleParser.ISOLATION, 0); }
		public TerminalNode KEY() { return getToken(BaseRuleParser.KEY, 0); }
		public TerminalNode LABEL() { return getToken(BaseRuleParser.LABEL, 0); }
		public TerminalNode LANGUAGE() { return getToken(BaseRuleParser.LANGUAGE, 0); }
		public TerminalNode LARGE() { return getToken(BaseRuleParser.LARGE, 0); }
		public TerminalNode LAST() { return getToken(BaseRuleParser.LAST, 0); }
		public TerminalNode LEAKPROOF() { return getToken(BaseRuleParser.LEAKPROOF, 0); }
		public TerminalNode LEVEL() { return getToken(BaseRuleParser.LEVEL, 0); }
		public TerminalNode LISTEN() { return getToken(BaseRuleParser.LISTEN, 0); }
		public TerminalNode LOAD() { return getToken(BaseRuleParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(BaseRuleParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(BaseRuleParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(BaseRuleParser.LOCK, 0); }
		public TerminalNode LOCKED() { return getToken(BaseRuleParser.LOCKED, 0); }
		public TerminalNode LOGGED() { return getToken(BaseRuleParser.LOGGED, 0); }
		public TerminalNode MAPPING() { return getToken(BaseRuleParser.MAPPING, 0); }
		public TerminalNode MATCH() { return getToken(BaseRuleParser.MATCH, 0); }
		public TerminalNode MATERIALIZED() { return getToken(BaseRuleParser.MATERIALIZED, 0); }
		public TerminalNode MAXVALUE() { return getToken(BaseRuleParser.MAXVALUE, 0); }
		public TerminalNode METHOD() { return getToken(BaseRuleParser.METHOD, 0); }
		public TerminalNode MINUTE() { return getToken(BaseRuleParser.MINUTE, 0); }
		public TerminalNode MINVALUE() { return getToken(BaseRuleParser.MINVALUE, 0); }
		public TerminalNode MODE() { return getToken(BaseRuleParser.MODE, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public TerminalNode MOVE() { return getToken(BaseRuleParser.MOVE, 0); }
		public TerminalNode NAME() { return getToken(BaseRuleParser.NAME, 0); }
		public TerminalNode NAMES() { return getToken(BaseRuleParser.NAMES, 0); }
		public TerminalNode NEW() { return getToken(BaseRuleParser.NEW, 0); }
		public TerminalNode NEXT() { return getToken(BaseRuleParser.NEXT, 0); }
		public TerminalNode NFC() { return getToken(BaseRuleParser.NFC, 0); }
		public TerminalNode NFD() { return getToken(BaseRuleParser.NFD, 0); }
		public TerminalNode NFKC() { return getToken(BaseRuleParser.NFKC, 0); }
		public TerminalNode NFKD() { return getToken(BaseRuleParser.NFKD, 0); }
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode NORMALIZED() { return getToken(BaseRuleParser.NORMALIZED, 0); }
		public TerminalNode NOTHING() { return getToken(BaseRuleParser.NOTHING, 0); }
		public TerminalNode NOTIFY() { return getToken(BaseRuleParser.NOTIFY, 0); }
		public TerminalNode NOWAIT() { return getToken(BaseRuleParser.NOWAIT, 0); }
		public TerminalNode NULLS() { return getToken(BaseRuleParser.NULLS, 0); }
		public TerminalNode OBJECT() { return getToken(BaseRuleParser.OBJECT, 0); }
		public TerminalNode OF() { return getToken(BaseRuleParser.OF, 0); }
		public TerminalNode OFF() { return getToken(BaseRuleParser.OFF, 0); }
		public TerminalNode OIDS() { return getToken(BaseRuleParser.OIDS, 0); }
		public TerminalNode OLD() { return getToken(BaseRuleParser.OLD, 0); }
		public TerminalNode OPERATOR() { return getToken(BaseRuleParser.OPERATOR, 0); }
		public TerminalNode OPTION() { return getToken(BaseRuleParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(BaseRuleParser.OPTIONS, 0); }
		public TerminalNode ORDINALITY() { return getToken(BaseRuleParser.ORDINALITY, 0); }
		public TerminalNode OTHERS() { return getToken(BaseRuleParser.OTHERS, 0); }
		public TerminalNode OVER() { return getToken(BaseRuleParser.OVER, 0); }
		public TerminalNode OVERRIDING() { return getToken(BaseRuleParser.OVERRIDING, 0); }
		public TerminalNode OWNED() { return getToken(BaseRuleParser.OWNED, 0); }
		public TerminalNode OWNER() { return getToken(BaseRuleParser.OWNER, 0); }
		public TerminalNode PARALLEL() { return getToken(BaseRuleParser.PARALLEL, 0); }
		public TerminalNode PARSER() { return getToken(BaseRuleParser.PARSER, 0); }
		public TerminalNode PARTIAL() { return getToken(BaseRuleParser.PARTIAL, 0); }
		public TerminalNode PARTITION() { return getToken(BaseRuleParser.PARTITION, 0); }
		public TerminalNode PASSING() { return getToken(BaseRuleParser.PASSING, 0); }
		public TerminalNode PASSWORD() { return getToken(BaseRuleParser.PASSWORD, 0); }
		public TerminalNode PLANS() { return getToken(BaseRuleParser.PLANS, 0); }
		public TerminalNode POLICY() { return getToken(BaseRuleParser.POLICY, 0); }
		public TerminalNode PRECEDING() { return getToken(BaseRuleParser.PRECEDING, 0); }
		public TerminalNode PREPARE() { return getToken(BaseRuleParser.PREPARE, 0); }
		public TerminalNode PREPARED() { return getToken(BaseRuleParser.PREPARED, 0); }
		public TerminalNode PRESERVE() { return getToken(BaseRuleParser.PRESERVE, 0); }
		public TerminalNode PRIOR() { return getToken(BaseRuleParser.PRIOR, 0); }
		public TerminalNode PRIVILEGES() { return getToken(BaseRuleParser.PRIVILEGES, 0); }
		public TerminalNode PROCEDURAL() { return getToken(BaseRuleParser.PROCEDURAL, 0); }
		public TerminalNode PROCEDURE() { return getToken(BaseRuleParser.PROCEDURE, 0); }
		public TerminalNode PROCEDURES() { return getToken(BaseRuleParser.PROCEDURES, 0); }
		public TerminalNode PROGRAM() { return getToken(BaseRuleParser.PROGRAM, 0); }
		public TerminalNode PUBLICATION() { return getToken(BaseRuleParser.PUBLICATION, 0); }
		public TerminalNode QUOTE() { return getToken(BaseRuleParser.QUOTE, 0); }
		public TerminalNode RANGE() { return getToken(BaseRuleParser.RANGE, 0); }
		public TerminalNode READ() { return getToken(BaseRuleParser.READ, 0); }
		public TerminalNode REASSIGN() { return getToken(BaseRuleParser.REASSIGN, 0); }
		public TerminalNode RECHECK() { return getToken(BaseRuleParser.RECHECK, 0); }
		public TerminalNode RECURSIVE() { return getToken(BaseRuleParser.RECURSIVE, 0); }
		public TerminalNode REF() { return getToken(BaseRuleParser.REF, 0); }
		public TerminalNode REFERENCING() { return getToken(BaseRuleParser.REFERENCING, 0); }
		public TerminalNode REFRESH() { return getToken(BaseRuleParser.REFRESH, 0); }
		public TerminalNode REINDEX() { return getToken(BaseRuleParser.REINDEX, 0); }
		public TerminalNode RELATIVE() { return getToken(BaseRuleParser.RELATIVE, 0); }
		public TerminalNode RELEASE() { return getToken(BaseRuleParser.RELEASE, 0); }
		public TerminalNode RENAME() { return getToken(BaseRuleParser.RENAME, 0); }
		public TerminalNode REPEATABLE() { return getToken(BaseRuleParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(BaseRuleParser.REPLACE, 0); }
		public TerminalNode REPLICA() { return getToken(BaseRuleParser.REPLICA, 0); }
		public TerminalNode RESET() { return getToken(BaseRuleParser.RESET, 0); }
		public TerminalNode RESTART() { return getToken(BaseRuleParser.RESTART, 0); }
		public TerminalNode RESTRICT() { return getToken(BaseRuleParser.RESTRICT, 0); }
		public TerminalNode RETURNS() { return getToken(BaseRuleParser.RETURNS, 0); }
		public TerminalNode REVOKE() { return getToken(BaseRuleParser.REVOKE, 0); }
		public TerminalNode ROLE() { return getToken(BaseRuleParser.ROLE, 0); }
		public TerminalNode ROLLBACK() { return getToken(BaseRuleParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(BaseRuleParser.ROLLUP, 0); }
		public TerminalNode ROUTINE() { return getToken(BaseRuleParser.ROUTINE, 0); }
		public TerminalNode ROUTINES() { return getToken(BaseRuleParser.ROUTINES, 0); }
		public TerminalNode ROWS() { return getToken(BaseRuleParser.ROWS, 0); }
		public TerminalNode RULE() { return getToken(BaseRuleParser.RULE, 0); }
		public TerminalNode SAVEPOINT() { return getToken(BaseRuleParser.SAVEPOINT, 0); }
		public TerminalNode SCHEMA() { return getToken(BaseRuleParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(BaseRuleParser.SCHEMAS, 0); }
		public TerminalNode SCROLL() { return getToken(BaseRuleParser.SCROLL, 0); }
		public TerminalNode SEARCH() { return getToken(BaseRuleParser.SEARCH, 0); }
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public TerminalNode SECURITY() { return getToken(BaseRuleParser.SECURITY, 0); }
		public TerminalNode SEQUENCE() { return getToken(BaseRuleParser.SEQUENCE, 0); }
		public TerminalNode SEQUENCES() { return getToken(BaseRuleParser.SEQUENCES, 0); }
		public TerminalNode SERIALIZABLE() { return getToken(BaseRuleParser.SERIALIZABLE, 0); }
		public TerminalNode SERVER() { return getToken(BaseRuleParser.SERVER, 0); }
		public TerminalNode SESSION() { return getToken(BaseRuleParser.SESSION, 0); }
		public TerminalNode SET() { return getToken(BaseRuleParser.SET, 0); }
		public TerminalNode SETS() { return getToken(BaseRuleParser.SETS, 0); }
		public TerminalNode SHARE() { return getToken(BaseRuleParser.SHARE, 0); }
		public TerminalNode SHOW() { return getToken(BaseRuleParser.SHOW, 0); }
		public TerminalNode SIMPLE() { return getToken(BaseRuleParser.SIMPLE, 0); }
		public TerminalNode SNAPSHOT() { return getToken(BaseRuleParser.SNAPSHOT, 0); }
		public TerminalNode SQL() { return getToken(BaseRuleParser.SQL, 0); }
		public TerminalNode STABLE() { return getToken(BaseRuleParser.STABLE, 0); }
		public TerminalNode STANDALONE() { return getToken(BaseRuleParser.STANDALONE, 0); }
		public TerminalNode START() { return getToken(BaseRuleParser.START, 0); }
		public TerminalNode STATEMENT() { return getToken(BaseRuleParser.STATEMENT, 0); }
		public TerminalNode STATISTICS() { return getToken(BaseRuleParser.STATISTICS, 0); }
		public TerminalNode STDIN() { return getToken(BaseRuleParser.STDIN, 0); }
		public TerminalNode STDOUT() { return getToken(BaseRuleParser.STDOUT, 0); }
		public TerminalNode STORAGE() { return getToken(BaseRuleParser.STORAGE, 0); }
		public TerminalNode STORED() { return getToken(BaseRuleParser.STORED, 0); }
		public TerminalNode STRICT() { return getToken(BaseRuleParser.STRICT, 0); }
		public TerminalNode STRIP() { return getToken(BaseRuleParser.STRIP, 0); }
		public TerminalNode SUBSCRIPTION() { return getToken(BaseRuleParser.SUBSCRIPTION, 0); }
		public TerminalNode SUPPORT() { return getToken(BaseRuleParser.SUPPORT, 0); }
		public TerminalNode SYSID() { return getToken(BaseRuleParser.SYSID, 0); }
		public TerminalNode SYSTEM() { return getToken(BaseRuleParser.SYSTEM, 0); }
		public TerminalNode TABLES() { return getToken(BaseRuleParser.TABLES, 0); }
		public TerminalNode TABLESPACE() { return getToken(BaseRuleParser.TABLESPACE, 0); }
		public TerminalNode TEMP() { return getToken(BaseRuleParser.TEMP, 0); }
		public TerminalNode TEMPLATE() { return getToken(BaseRuleParser.TEMPLATE, 0); }
		public TerminalNode TEMPORARY() { return getToken(BaseRuleParser.TEMPORARY, 0); }
		public TerminalNode TEXT() { return getToken(BaseRuleParser.TEXT, 0); }
		public TerminalNode TIES() { return getToken(BaseRuleParser.TIES, 0); }
		public TerminalNode TRANSACTION() { return getToken(BaseRuleParser.TRANSACTION, 0); }
		public TerminalNode TRANSFORM() { return getToken(BaseRuleParser.TRANSFORM, 0); }
		public TerminalNode TRIGGER() { return getToken(BaseRuleParser.TRIGGER, 0); }
		public TerminalNode TRUNCATE() { return getToken(BaseRuleParser.TRUNCATE, 0); }
		public TerminalNode TRUSTED() { return getToken(BaseRuleParser.TRUSTED, 0); }
		public TerminalNode TYPE() { return getToken(BaseRuleParser.TYPE, 0); }
		public TerminalNode TYPES() { return getToken(BaseRuleParser.TYPES, 0); }
		public TerminalNode UESCAPE() { return getToken(BaseRuleParser.UESCAPE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(BaseRuleParser.UNBOUNDED, 0); }
		public TerminalNode UNCOMMITTED() { return getToken(BaseRuleParser.UNCOMMITTED, 0); }
		public TerminalNode UNENCRYPTED() { return getToken(BaseRuleParser.UNENCRYPTED, 0); }
		public TerminalNode UNKNOWN() { return getToken(BaseRuleParser.UNKNOWN, 0); }
		public TerminalNode UNLISTEN() { return getToken(BaseRuleParser.UNLISTEN, 0); }
		public TerminalNode UNLOGGED() { return getToken(BaseRuleParser.UNLOGGED, 0); }
		public TerminalNode UNTIL() { return getToken(BaseRuleParser.UNTIL, 0); }
		public TerminalNode UPDATE() { return getToken(BaseRuleParser.UPDATE, 0); }
		public TerminalNode VACUUM() { return getToken(BaseRuleParser.VACUUM, 0); }
		public TerminalNode VALID() { return getToken(BaseRuleParser.VALID, 0); }
		public TerminalNode VALIDATE() { return getToken(BaseRuleParser.VALIDATE, 0); }
		public TerminalNode VALIDATOR() { return getToken(BaseRuleParser.VALIDATOR, 0); }
		public TerminalNode VALUE() { return getToken(BaseRuleParser.VALUE, 0); }
		public TerminalNode VARYING() { return getToken(BaseRuleParser.VARYING, 0); }
		public TerminalNode VERSION() { return getToken(BaseRuleParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(BaseRuleParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(BaseRuleParser.VIEWS, 0); }
		public TerminalNode VOLATILE() { return getToken(BaseRuleParser.VOLATILE, 0); }
		public TerminalNode WHITESPACE() { return getToken(BaseRuleParser.WHITESPACE, 0); }
		public TerminalNode WITHIN() { return getToken(BaseRuleParser.WITHIN, 0); }
		public TerminalNode WITHOUT() { return getToken(BaseRuleParser.WITHOUT, 0); }
		public TerminalNode WORK() { return getToken(BaseRuleParser.WORK, 0); }
		public TerminalNode WRAPPER() { return getToken(BaseRuleParser.WRAPPER, 0); }
		public TerminalNode WRITE() { return getToken(BaseRuleParser.WRITE, 0); }
		public TerminalNode XML() { return getToken(BaseRuleParser.XML, 0); }
		public TerminalNode YEAR() { return getToken(BaseRuleParser.YEAR, 0); }
		public TerminalNode YES() { return getToken(BaseRuleParser.YES, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode JSON() { return getToken(BaseRuleParser.JSON, 0); }
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
		enterRule(_localctx, 14, RULE_unreservedWord);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INSERT) | (1L << UPDATE) | (1L << DELETE) | (1L << ALTER) | (1L << DROP) | (1L << TRUNCATE) | (1L << SCHEMA) | (1L << REVOKE) | (1L << ADD) | (1L << SET) | (1L << INDEX) | (1L << KEY) | (1L << FUNCTION) | (1L << TRIGGER) | (1L << PROCEDURE) | (1L << VIEW) | (1L << IF))) != 0) || ((((_la - 75)) & ~0x3f) == 0 && ((1L << (_la - 75)) & ((1L << (BY - 75)) | (1L << (BEGIN - 75)) | (1L << (COMMIT - 75)) | (1L << (ROLLBACK - 75)) | (1L << (SAVEPOINT - 75)) | (1L << (DOUBLE - 75)) | (1L << (YEAR - 75)) | (1L << (MONTH - 75)) | (1L << (DAY - 75)) | (1L << (HOUR - 75)) | (1L << (MINUTE - 75)) | (1L << (SECOND - 75)) | (1L << (CURRENT - 75)) | (1L << (ENABLE - 75)) | (1L << (DISABLE - 75)) | (1L << (CALL - 75)) | (1L << (PRESERVE - 75)) | (1L << (DEFINER - 75)) | (1L << (SQL - 75)) | (1L << (CASCADED - 75)) | (1L << (LOCAL - 75)) | (1L << (CLOSE - 75)) | (1L << (NEXT - 75)) | (1L << (NAME - 75)) | (1L << (NAMES - 75)) | (1L << (TYPE - 75)) | (1L << (TEXT - 75)) | (1L << (REPEATABLE - 75)) | (1L << (VARYING - 75)))) != 0) || ((((_la - 140)) & ~0x3f) == 0 && ((1L << (_la - 140)) & ((1L << (VALUE - 140)) | (1L << (TIES - 140)) | (1L << (CUBE - 140)) | (1L << (SETS - 140)) | (1L << (OTHERS - 140)) | (1L << (AT - 140)) | (1L << (ADMIN - 140)) | (1L << (ESCAPE - 140)) | (1L << (EXCLUDE - 140)) | (1L << (PARTITION - 140)) | (1L << (UNKNOWN - 140)) | (1L << (ALWAYS - 140)) | (1L << (CASCADE - 140)) | (1L << (GENERATED - 140)) | (1L << (ISOLATION - 140)) | (1L << (LEVEL - 140)) | (1L << (NO - 140)) | (1L << (OPTION - 140)) | (1L << (PRIVILEGES - 140)) | (1L << (READ - 140)) | (1L << (ROLE - 140)) | (1L << (ROWS - 140)) | (1L << (START - 140)) | (1L << (TRANSACTION - 140)) | (1L << (ACTION - 140)) | (1L << (CACHE - 140)) | (1L << (CHARACTERISTICS - 140)) | (1L << (CLUSTER - 140)) | (1L << (COMMENTS - 140)) | (1L << (CONSTRAINTS - 140)) | (1L << (CYCLE - 140)) | (1L << (DATA - 140)) | (1L << (DATABASE - 140)) | (1L << (DEFAULTS - 140)) | (1L << (DEFERRED - 140)) | (1L << (DEPENDS - 140)) | (1L << (DOMAIN - 140)) | (1L << (EXCLUDING - 140)) | (1L << (EXECUTE - 140)))) != 0) || ((((_la - 204)) & ~0x3f) == 0 && ((1L << (_la - 204)) & ((1L << (EXTENSION - 204)) | (1L << (EXTERNAL - 204)) | (1L << (FILTER - 204)) | (1L << (FIRST - 204)) | (1L << (FOLLOWING - 204)) | (1L << (FORCE - 204)) | (1L << (GLOBAL - 204)) | (1L << (IDENTITY - 204)) | (1L << (IMMEDIATE - 204)) | (1L << (INCLUDING - 204)) | (1L << (INCREMENT - 204)) | (1L << (INDEXES - 204)) | (1L << (INHERIT - 204)) | (1L << (INHERITS - 204)) | (1L << (INCLUDE - 204)) | (1L << (LANGUAGE - 204)) | (1L << (LARGE - 204)) | (1L << (LAST - 204)) | (1L << (LOGGED - 204)) | (1L << (MATCH - 204)) | (1L << (MAXVALUE - 204)) | (1L << (MINVALUE - 204)) | (1L << (NOTHING - 204)) | (1L << (NULLS - 204)) | (1L << (OBJECT - 204)) | (1L << (OIDS - 204)) | (1L << (OVER - 204)) | (1L << (OWNED - 204)) | (1L << (OWNER - 204)) | (1L << (PARTIAL - 204)) | (1L << (PRECEDING - 204)) | (1L << (RANGE - 204)) | (1L << (RENAME - 204)) | (1L << (REPLICA - 204)) | (1L << (RESET - 204)) | (1L << (RESTART - 204)) | (1L << (RESTRICT - 204)) | (1L << (ROUTINE - 204)) | (1L << (RULE - 204)) | (1L << (SECURITY - 204)) | (1L << (SEQUENCE - 204)) | (1L << (SESSION - 204)) | (1L << (SHOW - 204)) | (1L << (SIMPLE - 204)) | (1L << (STATISTICS - 204)) | (1L << (STORAGE - 204)) | (1L << (TABLESPACE - 204)) | (1L << (TEMP - 204)) | (1L << (TEMPORARY - 204)) | (1L << (UNBOUNDED - 204)) | (1L << (UNLOGGED - 204)) | (1L << (VALID - 204)) | (1L << (VALIDATE - 204)) | (1L << (WITHIN - 204)) | (1L << (WITHOUT - 204)) | (1L << (ZONE - 204)) | (1L << (OF - 204)))) != 0) || ((((_la - 268)) & ~0x3f) == 0 && ((1L << (_la - 268)) & ((1L << (UESCAPE - 268)) | (1L << (GROUPS - 268)) | (1L << (RECURSIVE - 268)) | (1L << (ENUM - 268)) | (1L << (XML - 268)) | (1L << (JSON - 268)) | (1L << (ORDINALITY - 268)) | (1L << (NFC - 268)) | (1L << (NFD - 268)) | (1L << (NFKC - 268)) | (1L << (NFKD - 268)) | (1L << (REF - 268)) | (1L << (PASSING - 268)))) != 0) || ((((_la - 332)) & ~0x3f) == 0 && ((1L << (_la - 332)) & ((1L << (VERSION - 332)) | (1L << (YES - 332)) | (1L << (STANDALONE - 332)) | (1L << (MATERIALIZED - 332)) | (1L << (OPERATOR - 332)) | (1L << (SHARE - 332)) | (1L << (ROLLUP - 332)) | (1L << (DOCUMENT - 332)) | (1L << (NORMALIZED - 332)) | (1L << (NOWAIT - 332)) | (1L << (LOCKED - 332)) | (1L << (COLUMNS - 332)) | (1L << (CONTENT - 332)) | (1L << (STRIP - 332)) | (1L << (WHITESPACE - 332)) | (1L << (CONFLICT - 332)) | (1L << (OVERRIDING - 332)) | (1L << (SYSTEM - 332)) | (1L << (ABORT - 332)) | (1L << (ABSOLUTE - 332)) | (1L << (ACCESS - 332)) | (1L << (AFTER - 332)) | (1L << (AGGREGATE - 332)) | (1L << (ALSO - 332)) | (1L << (ATTACH - 332)) | (1L << (ATTRIBUTE - 332)) | (1L << (BACKWARD - 332)) | (1L << (BEFORE - 332)) | (1L << (ASSERTION - 332)) | (1L << (ASSIGNMENT - 332)) | (1L << (CONTINUE - 332)) | (1L << (CONVERSION - 332)) | (1L << (COPY - 332)) | (1L << (COST - 332)) | (1L << (CSV - 332)) | (1L << (CALLED - 332)) | (1L << (CATALOG - 332)) | (1L << (CHAIN - 332)) | (1L << (CHECKPOINT - 332)) | (1L << (CLASS - 332)) | (1L << (CONFIGURATION - 332)) | (1L << (COMMENT - 332)) | (1L << (DETACH - 332)) | (1L << (DICTIONARY - 332)) | (1L << (EXPRESSION - 332)) | (1L << (INSENSITIVE - 332)) | (1L << (DISCARD - 332)))) != 0) || ((((_la - 396)) & ~0x3f) == 0 && ((1L << (_la - 396)) & ((1L << (OFF - 396)) | (1L << (INSTEAD - 396)) | (1L << (EXPLAIN - 396)) | (1L << (INPUT - 396)) | (1L << (INLINE - 396)) | (1L << (PARALLEL - 396)) | (1L << (LEAKPROOF - 396)) | (1L << (COMMITTED - 396)) | (1L << (ENCODING - 396)) | (1L << (IMPLICIT - 396)) | (1L << (DELIMITER - 396)) | (1L << (CURSOR - 396)) | (1L << (EACH - 396)) | (1L << (EVENT - 396)) | (1L << (DEALLOCATE - 396)) | (1L << (CONNECTION - 396)) | (1L << (DECLARE - 396)) | (1L << (FAMILY - 396)) | (1L << (FORWARD - 396)) | (1L << (EXCLUSIVE - 396)) | (1L << (FUNCTIONS - 396)) | (1L << (LOCATION - 396)) | (1L << (LABEL - 396)) | (1L << (DELIMITERS - 396)) | (1L << (HANDLER - 396)) | (1L << (HEADER - 396)) | (1L << (IMMUTABLE - 396)) | (1L << (GRANTED - 396)) | (1L << (HOLD - 396)) | (1L << (MAPPING - 396)) | (1L << (OLD - 396)) | (1L << (METHOD - 396)) | (1L << (LOAD - 396)) | (1L << (LISTEN - 396)) | (1L << (MODE - 396)) | (1L << (MOVE - 396)) | (1L << (PROCEDURAL - 396)) | (1L << (PARSER - 396)) | (1L << (PROCEDURES - 396)) | (1L << (ENCRYPTED - 396)) | (1L << (PUBLICATION - 396)) | (1L << (PROGRAM - 396)) | (1L << (REFERENCING - 396)) | (1L << (PLANS - 396)) | (1L << (REINDEX - 396)) | (1L << (PRIOR - 396)) | (1L << (PASSWORD - 396)) | (1L << (RELATIVE - 396)) | (1L << (QUOTE - 396)) | (1L << (ROUTINES - 396)) | (1L << (REPLACE - 396)) | (1L << (SNAPSHOT - 396)) | (1L << (REFRESH - 396)) | (1L << (PREPARE - 396)) | (1L << (OPTIONS - 396)) | (1L << (IMPORT - 396)) | (1L << (INVOKER - 396)) | (1L << (NEW - 396)) | (1L << (PREPARED - 396)) | (1L << (SCROLL - 396)) | (1L << (SEQUENCES - 396)) | (1L << (SYSID - 396)) | (1L << (REASSIGN - 396)) | (1L << (SERVER - 396)))) != 0) || ((((_la - 460)) & ~0x3f) == 0 && ((1L << (_la - 460)) & ((1L << (SUBSCRIPTION - 460)) | (1L << (SEARCH - 460)) | (1L << (SCHEMAS - 460)) | (1L << (RECHECK - 460)) | (1L << (POLICY - 460)) | (1L << (NOTIFY - 460)) | (1L << (LOCK - 460)) | (1L << (RELEASE - 460)) | (1L << (SERIALIZABLE - 460)) | (1L << (RETURNS - 460)) | (1L << (STATEMENT - 460)) | (1L << (STDIN - 460)) | (1L << (STDOUT - 460)) | (1L << (TABLES - 460)) | (1L << (SUPPORT - 460)) | (1L << (STABLE - 460)) | (1L << (TEMPLATE - 460)) | (1L << (UNENCRYPTED - 460)) | (1L << (VIEWS - 460)) | (1L << (UNCOMMITTED - 460)) | (1L << (TRANSFORM - 460)) | (1L << (UNLISTEN - 460)) | (1L << (TRUSTED - 460)) | (1L << (VALIDATOR - 460)) | (1L << (UNTIL - 460)) | (1L << (VACUUM - 460)) | (1L << (VOLATILE - 460)) | (1L << (STORED - 460)) | (1L << (WRITE - 460)) | (1L << (STRICT - 460)) | (1L << (TYPES - 460)) | (1L << (WRAPPER - 460)) | (1L << (WORK - 460)))) != 0)) ) {
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

	public static class TypeFuncNameKeywordContext extends ParserRuleContext {
		public TerminalNode AUTHORIZATION() { return getToken(BaseRuleParser.AUTHORIZATION, 0); }
		public TerminalNode BINARY() { return getToken(BaseRuleParser.BINARY, 0); }
		public TerminalNode COLLATION() { return getToken(BaseRuleParser.COLLATION, 0); }
		public TerminalNode CONCURRENTLY() { return getToken(BaseRuleParser.CONCURRENTLY, 0); }
		public TerminalNode CROSS() { return getToken(BaseRuleParser.CROSS, 0); }
		public TerminalNode CURRENT_SCHEMA() { return getToken(BaseRuleParser.CURRENT_SCHEMA, 0); }
		public TerminalNode FREEZE() { return getToken(BaseRuleParser.FREEZE, 0); }
		public TerminalNode FULL() { return getToken(BaseRuleParser.FULL, 0); }
		public TerminalNode ILIKE() { return getToken(BaseRuleParser.ILIKE, 0); }
		public TerminalNode INNER() { return getToken(BaseRuleParser.INNER, 0); }
		public TerminalNode IS() { return getToken(BaseRuleParser.IS, 0); }
		public TerminalNode ISNULL() { return getToken(BaseRuleParser.ISNULL, 0); }
		public TerminalNode JOIN() { return getToken(BaseRuleParser.JOIN, 0); }
		public TerminalNode LEFT() { return getToken(BaseRuleParser.LEFT, 0); }
		public TerminalNode LIKE() { return getToken(BaseRuleParser.LIKE, 0); }
		public TerminalNode NATURAL() { return getToken(BaseRuleParser.NATURAL, 0); }
		public TerminalNode NOTNULL() { return getToken(BaseRuleParser.NOTNULL, 0); }
		public TerminalNode OUTER() { return getToken(BaseRuleParser.OUTER, 0); }
		public TerminalNode OVERLAPS() { return getToken(BaseRuleParser.OVERLAPS, 0); }
		public TerminalNode RIGHT() { return getToken(BaseRuleParser.RIGHT, 0); }
		public TerminalNode SIMILAR() { return getToken(BaseRuleParser.SIMILAR, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(BaseRuleParser.TABLESAMPLE, 0); }
		public TerminalNode VERBOSE() { return getToken(BaseRuleParser.VERBOSE, 0); }
		public TypeFuncNameKeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeFuncNameKeyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTypeFuncNameKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTypeFuncNameKeyword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTypeFuncNameKeyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeFuncNameKeywordContext typeFuncNameKeyword() throws RecognitionException {
		TypeFuncNameKeywordContext _localctx = new TypeFuncNameKeywordContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_typeFuncNameKeyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(490);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NATURAL) | (1L << JOIN) | (1L << FULL) | (1L << INNER) | (1L << OUTER) | (1L << LEFT) | (1L << RIGHT) | (1L << CROSS) | (1L << IS))) != 0) || _la==LIKE || _la==COLLATION || ((((_la - 154)) & ~0x3f) == 0 && ((1L << (_la - 154)) & ((1L << (OVERLAPS - 154)) | (1L << (BINARY - 154)) | (1L << (CONCURRENTLY - 154)))) != 0) || ((((_la - 308)) & ~0x3f) == 0 && ((1L << (_la - 308)) & ((1L << (TABLESAMPLE - 308)) | (1L << (CURRENT_SCHEMA - 308)) | (1L << (ILIKE - 308)) | (1L << (SIMILAR - 308)) | (1L << (ISNULL - 308)) | (1L << (NOTNULL - 308)))) != 0) || ((((_la - 493)) & ~0x3f) == 0 && ((1L << (_la - 493)) & ((1L << (FREEZE - 493)) | (1L << (AUTHORIZATION - 493)) | (1L << (VERBOSE - 493)))) != 0)) ) {
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
		enterRule(_localctx, 18, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(492);
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
		enterRule(_localctx, 20, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(497);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(494);
				owner();
				setState(495);
				match(DOT_);
				}
				break;
			}
			setState(499);
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
		enterRule(_localctx, 22, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(504);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(501);
				owner();
				setState(502);
				match(DOT_);
				}
				break;
			}
			setState(506);
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
		enterRule(_localctx, 24, RULE_owner);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(508);
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
		enterRule(_localctx, 26, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(510);
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
		enterRule(_localctx, 28, RULE_tableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(513);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(512);
				match(LP_);
				}
			}

			setState(515);
			tableName();
			setState(520);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(516);
				match(COMMA_);
				setState(517);
				tableName();
				}
				}
				setState(522);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(524);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RP_) {
				{
				setState(523);
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
		enterRule(_localctx, 30, RULE_columnNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(526);
			match(LP_);
			setState(527);
			columnName();
			setState(532);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(528);
				match(COMMA_);
				setState(529);
				columnName();
				}
				}
				setState(534);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(535);
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

	public static class CollationNameContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
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
		enterRule(_localctx, 32, RULE_collationName);
		try {
			setState(539);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_:
				enterOuterAlt(_localctx, 1);
				{
				setState(537);
				match(STRING_);
				}
				break;
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case IF:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case VARYING:
			case VALUE:
			case TIES:
			case CUBE:
			case SETS:
			case OTHERS:
			case AT:
			case ADMIN:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case ORDINALITY:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(538);
				identifier();
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
		enterRule(_localctx, 34, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(541);
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
		enterRule(_localctx, 36, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
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
		enterRule(_localctx, 38, RULE_primaryKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(546);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PRIMARY) {
				{
				setState(545);
				match(PRIMARY);
				}
			}

			setState(548);
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
		enterRule(_localctx, 40, RULE_logicalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(550);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==OR || _la==AND_ || _la==OR_) ) {
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
		enterRule(_localctx, 42, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(552);
			_la = _input.LA(1);
			if ( !(((((_la - 519)) & ~0x3f) == 0 && ((1L << (_la - 519)) & ((1L << (EQ_ - 519)) | (1L << (NEQ_ - 519)) | (1L << (GT_ - 519)) | (1L << (GTE_ - 519)) | (1L << (LT_ - 519)) | (1L << (LTE_ - 519)))) != 0)) ) {
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

	public static class PatternMatchingOperatorContext extends ParserRuleContext {
		public TerminalNode LIKE() { return getToken(BaseRuleParser.LIKE, 0); }
		public TerminalNode TILDE_TILDE_() { return getToken(BaseRuleParser.TILDE_TILDE_, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode NOT_TILDE_TILDE_() { return getToken(BaseRuleParser.NOT_TILDE_TILDE_, 0); }
		public TerminalNode ILIKE() { return getToken(BaseRuleParser.ILIKE, 0); }
		public TerminalNode ILIKE_() { return getToken(BaseRuleParser.ILIKE_, 0); }
		public TerminalNode NOT_ILIKE_() { return getToken(BaseRuleParser.NOT_ILIKE_, 0); }
		public TerminalNode SIMILAR() { return getToken(BaseRuleParser.SIMILAR, 0); }
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public TerminalNode TILDE_() { return getToken(BaseRuleParser.TILDE_, 0); }
		public TerminalNode NOT_() { return getToken(BaseRuleParser.NOT_, 0); }
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public PatternMatchingOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_patternMatchingOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPatternMatchingOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPatternMatchingOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPatternMatchingOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternMatchingOperatorContext patternMatchingOperator() throws RecognitionException {
		PatternMatchingOperatorContext _localctx = new PatternMatchingOperatorContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_patternMatchingOperator);
		try {
			setState(577);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(554);
				match(LIKE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(555);
				match(TILDE_TILDE_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(556);
				match(NOT);
				setState(557);
				match(LIKE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(558);
				match(NOT_TILDE_TILDE_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(559);
				match(ILIKE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(560);
				match(ILIKE_);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(561);
				match(NOT);
				setState(562);
				match(ILIKE);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(563);
				match(NOT_ILIKE_);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(564);
				match(SIMILAR);
				setState(565);
				match(TO);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(566);
				match(NOT);
				setState(567);
				match(SIMILAR);
				setState(568);
				match(TO);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(569);
				match(TILDE_);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(570);
				match(NOT_);
				setState(571);
				match(TILDE_);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(572);
				match(TILDE_);
				setState(573);
				match(ASTERISK_);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(574);
				match(NOT_);
				setState(575);
				match(TILDE_);
				setState(576);
				match(ASTERISK_);
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

	public static class CursorNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public CursorNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cursorName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCursorName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCursorName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCursorName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CursorNameContext cursorName() throws RecognitionException {
		CursorNameContext _localctx = new CursorNameContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_cursorName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(579);
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

	public static class AExprContext extends ParserRuleContext {
		public CExprContext cExpr() {
			return getRuleContext(CExprContext.class,0);
		}
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public List<AExprContext> aExpr() {
			return getRuleContexts(AExprContext.class);
		}
		public AExprContext aExpr(int i) {
			return getRuleContext(AExprContext.class,i);
		}
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public QualOpContext qualOp() {
			return getRuleContext(QualOpContext.class,0);
		}
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public List<RowContext> row() {
			return getRuleContexts(RowContext.class);
		}
		public RowContext row(int i) {
			return getRuleContext(RowContext.class,i);
		}
		public TerminalNode OVERLAPS() { return getToken(BaseRuleParser.OVERLAPS, 0); }
		public TerminalNode UNIQUE() { return getToken(BaseRuleParser.UNIQUE, 0); }
		public SelectWithParensContext selectWithParens() {
			return getRuleContext(SelectWithParensContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public TerminalNode AT() { return getToken(BaseRuleParser.AT, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public TerminalNode SLASH_() { return getToken(BaseRuleParser.SLASH_, 0); }
		public TerminalNode MOD_() { return getToken(BaseRuleParser.MOD_, 0); }
		public TerminalNode CARET_() { return getToken(BaseRuleParser.CARET_, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public PatternMatchingOperatorContext patternMatchingOperator() {
			return getRuleContext(PatternMatchingOperatorContext.class,0);
		}
		public TerminalNode ESCAPE() { return getToken(BaseRuleParser.ESCAPE, 0); }
		public TerminalNode IS() { return getToken(BaseRuleParser.IS, 0); }
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public TerminalNode BETWEEN() { return getToken(BaseRuleParser.BETWEEN, 0); }
		public BExprContext bExpr() {
			return getRuleContext(BExprContext.class,0);
		}
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public TerminalNode ASYMMETRIC() { return getToken(BaseRuleParser.ASYMMETRIC, 0); }
		public TerminalNode SYMMETRIC() { return getToken(BaseRuleParser.SYMMETRIC, 0); }
		public LogicalOperatorContext logicalOperator() {
			return getRuleContext(LogicalOperatorContext.class,0);
		}
		public TerminalNode TYPE_CAST_() { return getToken(BaseRuleParser.TYPE_CAST_, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode COLLATE() { return getToken(BaseRuleParser.COLLATE, 0); }
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public TerminalNode ISNULL() { return getToken(BaseRuleParser.ISNULL, 0); }
		public TerminalNode NOTNULL() { return getToken(BaseRuleParser.NOTNULL, 0); }
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(BaseRuleParser.UNKNOWN, 0); }
		public TerminalNode OF() { return getToken(BaseRuleParser.OF, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode IN() { return getToken(BaseRuleParser.IN, 0); }
		public InExprContext inExpr() {
			return getRuleContext(InExprContext.class,0);
		}
		public SubqueryOpContext subqueryOp() {
			return getRuleContext(SubqueryOpContext.class,0);
		}
		public SubTypeContext subType() {
			return getRuleContext(SubTypeContext.class,0);
		}
		public TerminalNode DOCUMENT() { return getToken(BaseRuleParser.DOCUMENT, 0); }
		public TerminalNode NORMALIZED() { return getToken(BaseRuleParser.NORMALIZED, 0); }
		public UnicodeNormalFormContext unicodeNormalForm() {
			return getRuleContext(UnicodeNormalFormContext.class,0);
		}
		public AExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AExprContext aExpr() throws RecognitionException {
		return aExpr(0);
	}

	private AExprContext aExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		AExprContext _localctx = new AExprContext(_ctx, _parentState);
		AExprContext _prevctx = _localctx;
		int _startState = 48;
		enterRecursionRule(_localctx, 48, RULE_aExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(599);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(582);
				cExpr();
				}
				break;
			case 2:
				{
				setState(583);
				match(PLUS_);
				setState(584);
				aExpr(47);
				}
				break;
			case 3:
				{
				setState(585);
				match(MINUS_);
				setState(586);
				aExpr(46);
				}
				break;
			case 4:
				{
				setState(587);
				qualOp();
				setState(588);
				aExpr(38);
				}
				break;
			case 5:
				{
				setState(590);
				match(NOT);
				setState(591);
				aExpr(35);
				}
				break;
			case 6:
				{
				setState(592);
				row();
				setState(593);
				match(OVERLAPS);
				setState(594);
				row();
				}
				break;
			case 7:
				{
				setState(596);
				match(UNIQUE);
				setState(597);
				selectWithParens();
				}
				break;
			case 8:
				{
				setState(598);
				match(DEFAULT);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(792);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(790);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
					case 1:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(601);
						if (!(precpred(_ctx, 48))) throw new FailedPredicateException(this, "precpred(_ctx, 48)");
						setState(602);
						match(AT);
						setState(603);
						match(TIME);
						setState(604);
						match(ZONE);
						setState(605);
						aExpr(49);
						}
						break;
					case 2:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(606);
						if (!(precpred(_ctx, 45))) throw new FailedPredicateException(this, "precpred(_ctx, 45)");
						setState(607);
						match(PLUS_);
						setState(608);
						aExpr(46);
						}
						break;
					case 3:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(609);
						if (!(precpred(_ctx, 44))) throw new FailedPredicateException(this, "precpred(_ctx, 44)");
						setState(610);
						match(MINUS_);
						setState(611);
						aExpr(45);
						}
						break;
					case 4:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(612);
						if (!(precpred(_ctx, 43))) throw new FailedPredicateException(this, "precpred(_ctx, 43)");
						setState(613);
						match(ASTERISK_);
						setState(614);
						aExpr(44);
						}
						break;
					case 5:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(615);
						if (!(precpred(_ctx, 42))) throw new FailedPredicateException(this, "precpred(_ctx, 42)");
						setState(616);
						match(SLASH_);
						setState(617);
						aExpr(43);
						}
						break;
					case 6:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(618);
						if (!(precpred(_ctx, 41))) throw new FailedPredicateException(this, "precpred(_ctx, 41)");
						setState(619);
						match(MOD_);
						setState(620);
						aExpr(42);
						}
						break;
					case 7:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(621);
						if (!(precpred(_ctx, 40))) throw new FailedPredicateException(this, "precpred(_ctx, 40)");
						setState(622);
						match(CARET_);
						setState(623);
						aExpr(41);
						}
						break;
					case 8:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(624);
						if (!(precpred(_ctx, 39))) throw new FailedPredicateException(this, "precpred(_ctx, 39)");
						setState(625);
						qualOp();
						setState(626);
						aExpr(40);
						}
						break;
					case 9:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(628);
						if (!(precpred(_ctx, 36))) throw new FailedPredicateException(this, "precpred(_ctx, 36)");
						setState(629);
						comparisonOperator();
						setState(630);
						aExpr(37);
						}
						break;
					case 10:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(632);
						if (!(precpred(_ctx, 34))) throw new FailedPredicateException(this, "precpred(_ctx, 34)");
						setState(633);
						patternMatchingOperator();
						setState(634);
						aExpr(35);
						}
						break;
					case 11:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(636);
						if (!(precpred(_ctx, 33))) throw new FailedPredicateException(this, "precpred(_ctx, 33)");
						setState(637);
						patternMatchingOperator();
						setState(638);
						aExpr(0);
						setState(639);
						match(ESCAPE);
						setState(640);
						aExpr(34);
						}
						break;
					case 12:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(642);
						if (!(precpred(_ctx, 21))) throw new FailedPredicateException(this, "precpred(_ctx, 21)");
						setState(643);
						match(IS);
						setState(644);
						match(DISTINCT);
						setState(645);
						match(FROM);
						setState(646);
						aExpr(22);
						}
						break;
					case 13:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(647);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(648);
						match(IS);
						setState(649);
						match(NOT);
						setState(650);
						match(DISTINCT);
						setState(651);
						match(FROM);
						setState(652);
						aExpr(21);
						}
						break;
					case 14:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(653);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(654);
						match(BETWEEN);
						setState(656);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ASYMMETRIC) {
							{
							setState(655);
							match(ASYMMETRIC);
							}
						}

						setState(658);
						bExpr(0);
						setState(659);
						match(AND);
						setState(660);
						aExpr(18);
						}
						break;
					case 15:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(662);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(663);
						match(NOT);
						setState(664);
						match(BETWEEN);
						setState(666);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ASYMMETRIC) {
							{
							setState(665);
							match(ASYMMETRIC);
							}
						}

						setState(668);
						bExpr(0);
						setState(669);
						match(AND);
						setState(670);
						aExpr(17);
						}
						break;
					case 16:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(672);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(673);
						match(BETWEEN);
						setState(674);
						match(SYMMETRIC);
						setState(675);
						bExpr(0);
						setState(676);
						match(AND);
						setState(677);
						aExpr(16);
						}
						break;
					case 17:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(679);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(680);
						match(NOT);
						setState(681);
						match(BETWEEN);
						setState(682);
						match(SYMMETRIC);
						setState(683);
						bExpr(0);
						setState(684);
						match(AND);
						setState(685);
						aExpr(15);
						}
						break;
					case 18:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(687);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(688);
						logicalOperator();
						setState(689);
						aExpr(3);
						}
						break;
					case 19:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(691);
						if (!(precpred(_ctx, 50))) throw new FailedPredicateException(this, "precpred(_ctx, 50)");
						setState(692);
						match(TYPE_CAST_);
						setState(693);
						typeName();
						}
						break;
					case 20:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(694);
						if (!(precpred(_ctx, 49))) throw new FailedPredicateException(this, "precpred(_ctx, 49)");
						setState(695);
						match(COLLATE);
						setState(696);
						anyName();
						}
						break;
					case 21:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(697);
						if (!(precpred(_ctx, 37))) throw new FailedPredicateException(this, "precpred(_ctx, 37)");
						setState(698);
						qualOp();
						}
						break;
					case 22:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(699);
						if (!(precpred(_ctx, 32))) throw new FailedPredicateException(this, "precpred(_ctx, 32)");
						setState(700);
						match(IS);
						setState(701);
						match(NULL);
						}
						break;
					case 23:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(702);
						if (!(precpred(_ctx, 31))) throw new FailedPredicateException(this, "precpred(_ctx, 31)");
						setState(703);
						match(ISNULL);
						}
						break;
					case 24:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(704);
						if (!(precpred(_ctx, 30))) throw new FailedPredicateException(this, "precpred(_ctx, 30)");
						setState(705);
						match(IS);
						setState(706);
						match(NOT);
						setState(707);
						match(NULL);
						}
						break;
					case 25:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(708);
						if (!(precpred(_ctx, 29))) throw new FailedPredicateException(this, "precpred(_ctx, 29)");
						setState(709);
						match(NOTNULL);
						}
						break;
					case 26:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(710);
						if (!(precpred(_ctx, 27))) throw new FailedPredicateException(this, "precpred(_ctx, 27)");
						setState(711);
						match(IS);
						setState(712);
						match(TRUE);
						}
						break;
					case 27:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(713);
						if (!(precpred(_ctx, 26))) throw new FailedPredicateException(this, "precpred(_ctx, 26)");
						setState(714);
						match(IS);
						setState(715);
						match(NOT);
						setState(716);
						match(TRUE);
						}
						break;
					case 28:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(717);
						if (!(precpred(_ctx, 25))) throw new FailedPredicateException(this, "precpred(_ctx, 25)");
						setState(718);
						match(IS);
						setState(719);
						match(FALSE);
						}
						break;
					case 29:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(720);
						if (!(precpred(_ctx, 24))) throw new FailedPredicateException(this, "precpred(_ctx, 24)");
						setState(721);
						match(IS);
						setState(722);
						match(NOT);
						setState(723);
						match(FALSE);
						}
						break;
					case 30:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(724);
						if (!(precpred(_ctx, 23))) throw new FailedPredicateException(this, "precpred(_ctx, 23)");
						setState(725);
						match(IS);
						setState(726);
						match(UNKNOWN);
						}
						break;
					case 31:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(727);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(728);
						match(IS);
						setState(729);
						match(NOT);
						setState(730);
						match(UNKNOWN);
						}
						break;
					case 32:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(731);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(732);
						match(IS);
						setState(733);
						match(OF);
						setState(734);
						match(LP_);
						setState(735);
						typeList();
						setState(736);
						match(RP_);
						}
						break;
					case 33:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(738);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(739);
						match(IS);
						setState(740);
						match(NOT);
						setState(741);
						match(OF);
						setState(742);
						match(LP_);
						setState(743);
						typeList();
						setState(744);
						match(RP_);
						}
						break;
					case 34:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(746);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(747);
						match(IN);
						setState(748);
						inExpr();
						}
						break;
					case 35:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(749);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(750);
						match(NOT);
						setState(751);
						match(IN);
						setState(752);
						inExpr();
						}
						break;
					case 36:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(753);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(754);
						subqueryOp();
						setState(755);
						subType();
						setState(756);
						selectWithParens();
						}
						break;
					case 37:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(758);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(759);
						subqueryOp();
						setState(760);
						subType();
						setState(761);
						match(LP_);
						setState(762);
						aExpr(0);
						setState(763);
						match(RP_);
						}
						break;
					case 38:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(765);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(766);
						match(IS);
						setState(767);
						match(DOCUMENT);
						}
						break;
					case 39:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(768);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(769);
						match(IS);
						setState(770);
						match(NOT);
						setState(771);
						match(DOCUMENT);
						}
						break;
					case 40:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(772);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(773);
						match(IS);
						setState(774);
						match(NORMALIZED);
						}
						break;
					case 41:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(775);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(776);
						match(IS);
						setState(777);
						unicodeNormalForm();
						setState(778);
						match(NORMALIZED);
						}
						break;
					case 42:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(780);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(781);
						match(IS);
						setState(782);
						match(NOT);
						setState(783);
						match(NORMALIZED);
						}
						break;
					case 43:
						{
						_localctx = new AExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_aExpr);
						setState(784);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(785);
						match(IS);
						setState(786);
						match(NOT);
						setState(787);
						unicodeNormalForm();
						setState(788);
						match(NORMALIZED);
						}
						break;
					}
					} 
				}
				setState(794);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
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

	public static class BExprContext extends ParserRuleContext {
		public CExprContext cExpr() {
			return getRuleContext(CExprContext.class,0);
		}
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public List<BExprContext> bExpr() {
			return getRuleContexts(BExprContext.class);
		}
		public BExprContext bExpr(int i) {
			return getRuleContext(BExprContext.class,i);
		}
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public QualOpContext qualOp() {
			return getRuleContext(QualOpContext.class,0);
		}
		public TerminalNode IS() { return getToken(BaseRuleParser.IS, 0); }
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode TYPE_CAST_() { return getToken(BaseRuleParser.TYPE_CAST_, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode OF() { return getToken(BaseRuleParser.OF, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode DOCUMENT() { return getToken(BaseRuleParser.DOCUMENT, 0); }
		public BExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BExprContext bExpr() throws RecognitionException {
		return bExpr(0);
	}

	private BExprContext bExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BExprContext _localctx = new BExprContext(_ctx, _parentState);
		BExprContext _prevctx = _localctx;
		int _startState = 50;
		enterRecursionRule(_localctx, 50, RULE_bExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(804);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				setState(796);
				cExpr();
				}
				break;
			case 2:
				{
				setState(797);
				match(PLUS_);
				setState(798);
				bExpr(11);
				}
				break;
			case 3:
				{
				setState(799);
				match(MINUS_);
				setState(800);
				bExpr(10);
				}
				break;
			case 4:
				{
				setState(801);
				qualOp();
				setState(802);
				bExpr(8);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(850);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(848);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
					case 1:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(806);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(807);
						qualOp();
						setState(808);
						bExpr(10);
						}
						break;
					case 2:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(810);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(811);
						match(IS);
						setState(812);
						match(DISTINCT);
						setState(813);
						match(FROM);
						setState(814);
						bExpr(7);
						}
						break;
					case 3:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(815);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(816);
						match(IS);
						setState(817);
						match(NOT);
						setState(818);
						match(DISTINCT);
						setState(819);
						match(FROM);
						setState(820);
						bExpr(6);
						}
						break;
					case 4:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(821);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(822);
						match(TYPE_CAST_);
						setState(823);
						typeName();
						}
						break;
					case 5:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(824);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(825);
						qualOp();
						}
						break;
					case 6:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(826);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(827);
						match(IS);
						setState(828);
						match(OF);
						setState(829);
						match(LP_);
						setState(830);
						typeList();
						setState(831);
						match(RP_);
						}
						break;
					case 7:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(833);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(834);
						match(IS);
						setState(835);
						match(NOT);
						setState(836);
						match(OF);
						setState(837);
						match(LP_);
						setState(838);
						typeList();
						setState(839);
						match(RP_);
						}
						break;
					case 8:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(841);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(842);
						match(IS);
						setState(843);
						match(DOCUMENT);
						}
						break;
					case 9:
						{
						_localctx = new BExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bExpr);
						setState(844);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(845);
						match(IS);
						setState(846);
						match(NOT);
						setState(847);
						match(DOCUMENT);
						}
						break;
					}
					} 
				}
				setState(852);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
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

	public static class CExprContext extends ParserRuleContext {
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public ColumnrefContext columnref() {
			return getRuleContext(ColumnrefContext.class,0);
		}
		public AexprConstContext aexprConst() {
			return getRuleContext(AexprConstContext.class,0);
		}
		public TerminalNode PARAM() { return getToken(BaseRuleParser.PARAM, 0); }
		public IndirectionElContext indirectionEl() {
			return getRuleContext(IndirectionElContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public OptIndirectionContext optIndirection() {
			return getRuleContext(OptIndirectionContext.class,0);
		}
		public CaseExprContext caseExpr() {
			return getRuleContext(CaseExprContext.class,0);
		}
		public FuncExprContext funcExpr() {
			return getRuleContext(FuncExprContext.class,0);
		}
		public SelectWithParensContext selectWithParens() {
			return getRuleContext(SelectWithParensContext.class,0);
		}
		public IndirectionContext indirection() {
			return getRuleContext(IndirectionContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(BaseRuleParser.EXISTS, 0); }
		public TerminalNode ARRAY() { return getToken(BaseRuleParser.ARRAY, 0); }
		public ArrayExprContext arrayExpr() {
			return getRuleContext(ArrayExprContext.class,0);
		}
		public ExplicitRowContext explicitRow() {
			return getRuleContext(ExplicitRowContext.class,0);
		}
		public ImplicitRowContext implicitRow() {
			return getRuleContext(ImplicitRowContext.class,0);
		}
		public TerminalNode GROUPING() { return getToken(BaseRuleParser.GROUPING, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public CExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CExprContext cExpr() throws RecognitionException {
		CExprContext _localctx = new CExprContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_cExpr);
		try {
			setState(884);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(853);
				parameterMarker();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(854);
				columnref();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(855);
				aexprConst();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(856);
				match(PARAM);
				setState(858);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(857);
					indirectionEl();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(860);
				match(LP_);
				setState(861);
				aExpr(0);
				setState(862);
				match(RP_);
				setState(863);
				optIndirection(0);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(865);
				caseExpr();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(866);
				funcExpr();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(867);
				selectWithParens();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(868);
				selectWithParens();
				setState(869);
				indirection(0);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(871);
				match(EXISTS);
				setState(872);
				selectWithParens();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(873);
				match(ARRAY);
				setState(874);
				selectWithParens();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(875);
				match(ARRAY);
				setState(876);
				arrayExpr();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(877);
				explicitRow();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(878);
				implicitRow();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(879);
				match(GROUPING);
				setState(880);
				match(LP_);
				setState(881);
				exprList(0);
				setState(882);
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

	public static class IndirectionContext extends ParserRuleContext {
		public IndirectionElContext indirectionEl() {
			return getRuleContext(IndirectionElContext.class,0);
		}
		public IndirectionContext indirection() {
			return getRuleContext(IndirectionContext.class,0);
		}
		public IndirectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indirection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndirection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndirection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndirection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndirectionContext indirection() throws RecognitionException {
		return indirection(0);
	}

	private IndirectionContext indirection(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		IndirectionContext _localctx = new IndirectionContext(_ctx, _parentState);
		IndirectionContext _prevctx = _localctx;
		int _startState = 54;
		enterRecursionRule(_localctx, 54, RULE_indirection, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(887);
			indirectionEl();
			}
			_ctx.stop = _input.LT(-1);
			setState(893);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new IndirectionContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_indirection);
					setState(889);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(890);
					indirectionEl();
					}
					} 
				}
				setState(895);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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

	public static class OptIndirectionContext extends ParserRuleContext {
		public OptIndirectionContext optIndirection() {
			return getRuleContext(OptIndirectionContext.class,0);
		}
		public IndirectionElContext indirectionEl() {
			return getRuleContext(IndirectionElContext.class,0);
		}
		public OptIndirectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optIndirection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOptIndirection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOptIndirection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOptIndirection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptIndirectionContext optIndirection() throws RecognitionException {
		return optIndirection(0);
	}

	private OptIndirectionContext optIndirection(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		OptIndirectionContext _localctx = new OptIndirectionContext(_ctx, _parentState);
		OptIndirectionContext _prevctx = _localctx;
		int _startState = 56;
		enterRecursionRule(_localctx, 56, RULE_optIndirection, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			}
			_ctx.stop = _input.LT(-1);
			setState(901);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new OptIndirectionContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_optIndirection);
					setState(897);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(898);
					indirectionEl();
					}
					} 
				}
				setState(903);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
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

	public static class IndirectionElContext extends ParserRuleContext {
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public AttrNameContext attrName() {
			return getRuleContext(AttrNameContext.class,0);
		}
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public TerminalNode LBT_() { return getToken(BaseRuleParser.LBT_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RBT_() { return getToken(BaseRuleParser.RBT_, 0); }
		public TerminalNode COLON_() { return getToken(BaseRuleParser.COLON_, 0); }
		public List<SliceBoundContext> sliceBound() {
			return getRuleContexts(SliceBoundContext.class);
		}
		public SliceBoundContext sliceBound(int i) {
			return getRuleContext(SliceBoundContext.class,i);
		}
		public IndirectionElContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indirectionEl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndirectionEl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndirectionEl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndirectionEl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndirectionElContext indirectionEl() throws RecognitionException {
		IndirectionElContext _localctx = new IndirectionElContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_indirectionEl);
		int _la;
		try {
			setState(921);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(904);
				match(DOT_);
				setState(905);
				attrName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(906);
				match(DOT_);
				setState(907);
				match(ASTERISK_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(908);
				match(LBT_);
				setState(909);
				aExpr(0);
				setState(910);
				match(RBT_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(912);
				match(LBT_);
				setState(914);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << INSERT) | (1L << UPDATE) | (1L << DELETE) | (1L << ALTER) | (1L << DROP) | (1L << TRUNCATE) | (1L << SCHEMA) | (1L << REVOKE) | (1L << ADD) | (1L << SET) | (1L << INDEX) | (1L << UNIQUE) | (1L << KEY) | (1L << POSITION) | (1L << FUNCTION) | (1L << TRIGGER) | (1L << PROCEDURE) | (1L << VIEW) | (1L << CASE) | (1L << CAST) | (1L << TRIM) | (1L << SUBSTRING) | (1L << NATURAL) | (1L << JOIN) | (1L << FULL) | (1L << INNER) | (1L << OUTER) | (1L << LEFT) | (1L << RIGHT) | (1L << CROSS) | (1L << IF) | (1L << IS) | (1L << NOT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (NULL - 64)) | (1L << (TRUE - 64)) | (1L << (FALSE - 64)) | (1L << (EXISTS - 64)) | (1L << (LIKE - 64)) | (1L << (BY - 64)) | (1L << (BEGIN - 64)) | (1L << (COMMIT - 64)) | (1L << (ROLLBACK - 64)) | (1L << (SAVEPOINT - 64)) | (1L << (DOUBLE - 64)) | (1L << (ARRAY - 64)) | (1L << (LOCALTIME - 64)) | (1L << (LOCALTIMESTAMP - 64)) | (1L << (YEAR - 64)) | (1L << (MONTH - 64)) | (1L << (DAY - 64)) | (1L << (HOUR - 64)) | (1L << (MINUTE - 64)) | (1L << (SECOND - 64)) | (1L << (DEFAULT - 64)) | (1L << (CURRENT - 64)) | (1L << (ENABLE - 64)) | (1L << (DISABLE - 64)) | (1L << (CALL - 64)) | (1L << (PRESERVE - 64)) | (1L << (DEFINER - 64)) | (1L << (CURRENT_USER - 64)) | (1L << (SQL - 64)) | (1L << (CASCADED - 64)) | (1L << (LOCAL - 64)) | (1L << (CLOSE - 64)) | (1L << (NEXT - 64)) | (1L << (NAME - 64)) | (1L << (COLLATION - 64)) | (1L << (NAMES - 64)) | (1L << (TYPE - 64)))) != 0) || ((((_la - 131)) & ~0x3f) == 0 && ((1L << (_la - 131)) & ((1L << (TEXT - 131)) | (1L << (REPEATABLE - 131)) | (1L << (CURRENT_DATE - 131)) | (1L << (CURRENT_TIME - 131)) | (1L << (CURRENT_TIMESTAMP - 131)) | (1L << (NULLIF - 131)) | (1L << (VARYING - 131)) | (1L << (VALUE - 131)) | (1L << (COALESCE - 131)) | (1L << (TIES - 131)) | (1L << (CUBE - 131)) | (1L << (GROUPING - 131)) | (1L << (SETS - 131)) | (1L << (OTHERS - 131)) | (1L << (OVERLAPS - 131)) | (1L << (AT - 131)) | (1L << (ADMIN - 131)) | (1L << (BINARY - 131)) | (1L << (ESCAPE - 131)) | (1L << (EXCLUDE - 131)) | (1L << (PARTITION - 131)) | (1L << (ROW - 131)) | (1L << (UNKNOWN - 131)) | (1L << (ALWAYS - 131)) | (1L << (CASCADE - 131)) | (1L << (GENERATED - 131)) | (1L << (ISOLATION - 131)) | (1L << (LEVEL - 131)) | (1L << (NO - 131)) | (1L << (OPTION - 131)) | (1L << (PRIVILEGES - 131)) | (1L << (READ - 131)) | (1L << (ROLE - 131)) | (1L << (ROWS - 131)) | (1L << (START - 131)) | (1L << (TRANSACTION - 131)) | (1L << (USER - 131)) | (1L << (ACTION - 131)) | (1L << (CACHE - 131)) | (1L << (CHARACTERISTICS - 131)) | (1L << (CLUSTER - 131)) | (1L << (COMMENTS - 131)) | (1L << (CONCURRENTLY - 131)) | (1L << (CONSTRAINTS - 131)) | (1L << (CYCLE - 131)) | (1L << (DATA - 131)))) != 0) || ((((_la - 195)) & ~0x3f) == 0 && ((1L << (_la - 195)) & ((1L << (DATABASE - 195)) | (1L << (DEFAULTS - 195)) | (1L << (DEFERRED - 195)) | (1L << (DEPENDS - 195)) | (1L << (DOMAIN - 195)) | (1L << (EXCLUDING - 195)) | (1L << (EXECUTE - 195)) | (1L << (EXTENSION - 195)) | (1L << (EXTERNAL - 195)) | (1L << (EXTRACT - 195)) | (1L << (FILTER - 195)) | (1L << (FIRST - 195)) | (1L << (FOLLOWING - 195)) | (1L << (FORCE - 195)) | (1L << (GLOBAL - 195)) | (1L << (IDENTITY - 195)) | (1L << (IMMEDIATE - 195)) | (1L << (INCLUDING - 195)) | (1L << (INCREMENT - 195)) | (1L << (INDEXES - 195)) | (1L << (INHERIT - 195)) | (1L << (INHERITS - 195)) | (1L << (INCLUDE - 195)) | (1L << (LANGUAGE - 195)) | (1L << (LARGE - 195)) | (1L << (LAST - 195)) | (1L << (LOGGED - 195)) | (1L << (MATCH - 195)) | (1L << (MAXVALUE - 195)) | (1L << (MINVALUE - 195)) | (1L << (NOTHING - 195)) | (1L << (NULLS - 195)) | (1L << (OBJECT - 195)) | (1L << (OIDS - 195)) | (1L << (OVER - 195)) | (1L << (OWNED - 195)) | (1L << (OWNER - 195)) | (1L << (PARTIAL - 195)) | (1L << (PRECEDING - 195)) | (1L << (RANGE - 195)) | (1L << (RENAME - 195)) | (1L << (REPLICA - 195)) | (1L << (RESET - 195)) | (1L << (RESTART - 195)) | (1L << (RESTRICT - 195)) | (1L << (ROUTINE - 195)) | (1L << (RULE - 195)) | (1L << (SECURITY - 195)) | (1L << (SEQUENCE - 195)) | (1L << (SESSION - 195)) | (1L << (SESSION_USER - 195)) | (1L << (SHOW - 195)) | (1L << (SIMPLE - 195)) | (1L << (STATISTICS - 195)) | (1L << (STORAGE - 195)) | (1L << (TABLESPACE - 195)) | (1L << (TEMP - 195)) | (1L << (TEMPORARY - 195)))) != 0) || ((((_la - 259)) & ~0x3f) == 0 && ((1L << (_la - 259)) & ((1L << (UNBOUNDED - 259)) | (1L << (UNLOGGED - 259)) | (1L << (VALID - 259)) | (1L << (VALIDATE - 259)) | (1L << (WITHIN - 259)) | (1L << (WITHOUT - 259)) | (1L << (ZONE - 259)) | (1L << (OF - 259)) | (1L << (UESCAPE - 259)) | (1L << (GROUPS - 259)) | (1L << (RECURSIVE - 259)) | (1L << (ENUM - 259)) | (1L << (XML - 259)) | (1L << (JSON - 259)) | (1L << (TABLESAMPLE - 259)) | (1L << (ORDINALITY - 259)) | (1L << (CURRENT_ROLE - 259)) | (1L << (CURRENT_CATALOG - 259)) | (1L << (CURRENT_SCHEMA - 259)) | (1L << (NORMALIZE - 259)) | (1L << (OVERLAY - 259)) | (1L << (XMLCONCAT - 259)) | (1L << (XMLELEMENT - 259)) | (1L << (XMLEXISTS - 259)) | (1L << (XMLFOREST - 259)) | (1L << (XMLPARSE - 259)) | (1L << (XMLPI - 259)) | (1L << (XMLROOT - 259)) | (1L << (XMLSERIALIZE - 259)))) != 0) || ((((_la - 323)) & ~0x3f) == 0 && ((1L << (_la - 323)) & ((1L << (TREAT - 323)) | (1L << (NFC - 323)) | (1L << (NFD - 323)) | (1L << (NFKC - 323)) | (1L << (NFKD - 323)) | (1L << (REF - 323)) | (1L << (PASSING - 323)) | (1L << (VERSION - 323)) | (1L << (YES - 323)) | (1L << (STANDALONE - 323)) | (1L << (GREATEST - 323)) | (1L << (LEAST - 323)) | (1L << (MATERIALIZED - 323)) | (1L << (OPERATOR - 323)) | (1L << (SHARE - 323)) | (1L << (ROLLUP - 323)) | (1L << (ILIKE - 323)) | (1L << (SIMILAR - 323)) | (1L << (ISNULL - 323)) | (1L << (NOTNULL - 323)) | (1L << (DOCUMENT - 323)) | (1L << (NORMALIZED - 323)) | (1L << (NOWAIT - 323)) | (1L << (LOCKED - 323)) | (1L << (COLUMNS - 323)) | (1L << (CONTENT - 323)) | (1L << (STRIP - 323)) | (1L << (WHITESPACE - 323)) | (1L << (CONFLICT - 323)) | (1L << (OVERRIDING - 323)) | (1L << (SYSTEM - 323)) | (1L << (ABORT - 323)) | (1L << (ABSOLUTE - 323)) | (1L << (ACCESS - 323)) | (1L << (AFTER - 323)) | (1L << (AGGREGATE - 323)) | (1L << (ALSO - 323)) | (1L << (ATTACH - 323)) | (1L << (ATTRIBUTE - 323)) | (1L << (BACKWARD - 323)) | (1L << (BEFORE - 323)) | (1L << (ASSERTION - 323)) | (1L << (ASSIGNMENT - 323)) | (1L << (CONTINUE - 323)) | (1L << (CONVERSION - 323)) | (1L << (COPY - 323)) | (1L << (COST - 323)) | (1L << (CSV - 323)) | (1L << (CALLED - 323)) | (1L << (CATALOG - 323)) | (1L << (CHAIN - 323)))) != 0) || ((((_la - 387)) & ~0x3f) == 0 && ((1L << (_la - 387)) & ((1L << (CHECKPOINT - 387)) | (1L << (CLASS - 387)) | (1L << (CONFIGURATION - 387)) | (1L << (COMMENT - 387)) | (1L << (DETACH - 387)) | (1L << (DICTIONARY - 387)) | (1L << (EXPRESSION - 387)) | (1L << (INSENSITIVE - 387)) | (1L << (DISCARD - 387)) | (1L << (OFF - 387)) | (1L << (INSTEAD - 387)) | (1L << (EXPLAIN - 387)) | (1L << (INPUT - 387)) | (1L << (INLINE - 387)) | (1L << (PARALLEL - 387)) | (1L << (LEAKPROOF - 387)) | (1L << (COMMITTED - 387)) | (1L << (ENCODING - 387)) | (1L << (IMPLICIT - 387)) | (1L << (DELIMITER - 387)) | (1L << (CURSOR - 387)) | (1L << (EACH - 387)) | (1L << (EVENT - 387)) | (1L << (DEALLOCATE - 387)) | (1L << (CONNECTION - 387)) | (1L << (DECLARE - 387)) | (1L << (FAMILY - 387)) | (1L << (FORWARD - 387)) | (1L << (EXCLUSIVE - 387)) | (1L << (FUNCTIONS - 387)) | (1L << (LOCATION - 387)) | (1L << (LABEL - 387)) | (1L << (DELIMITERS - 387)) | (1L << (HANDLER - 387)) | (1L << (HEADER - 387)) | (1L << (IMMUTABLE - 387)) | (1L << (GRANTED - 387)) | (1L << (HOLD - 387)) | (1L << (MAPPING - 387)) | (1L << (OLD - 387)) | (1L << (METHOD - 387)) | (1L << (LOAD - 387)) | (1L << (LISTEN - 387)) | (1L << (MODE - 387)) | (1L << (MOVE - 387)) | (1L << (PROCEDURAL - 387)) | (1L << (PARSER - 387)) | (1L << (PROCEDURES - 387)) | (1L << (ENCRYPTED - 387)) | (1L << (PUBLICATION - 387)) | (1L << (PROGRAM - 387)) | (1L << (REFERENCING - 387)) | (1L << (PLANS - 387)) | (1L << (REINDEX - 387)) | (1L << (PRIOR - 387)) | (1L << (PASSWORD - 387)) | (1L << (RELATIVE - 387)) | (1L << (QUOTE - 387)) | (1L << (ROUTINES - 387)) | (1L << (REPLACE - 387)) | (1L << (SNAPSHOT - 387)) | (1L << (REFRESH - 387)) | (1L << (PREPARE - 387)) | (1L << (OPTIONS - 387)))) != 0) || ((((_la - 451)) & ~0x3f) == 0 && ((1L << (_la - 451)) & ((1L << (IMPORT - 451)) | (1L << (INVOKER - 451)) | (1L << (NEW - 451)) | (1L << (PREPARED - 451)) | (1L << (SCROLL - 451)) | (1L << (SEQUENCES - 451)) | (1L << (SYSID - 451)) | (1L << (REASSIGN - 451)) | (1L << (SERVER - 451)) | (1L << (SUBSCRIPTION - 451)) | (1L << (SEARCH - 451)) | (1L << (SCHEMAS - 451)) | (1L << (RECHECK - 451)) | (1L << (POLICY - 451)) | (1L << (NOTIFY - 451)) | (1L << (LOCK - 451)) | (1L << (RELEASE - 451)) | (1L << (SERIALIZABLE - 451)) | (1L << (RETURNS - 451)) | (1L << (STATEMENT - 451)) | (1L << (STDIN - 451)) | (1L << (STDOUT - 451)) | (1L << (TABLES - 451)) | (1L << (SUPPORT - 451)) | (1L << (STABLE - 451)) | (1L << (TEMPLATE - 451)) | (1L << (UNENCRYPTED - 451)) | (1L << (VIEWS - 451)) | (1L << (UNCOMMITTED - 451)) | (1L << (TRANSFORM - 451)) | (1L << (UNLISTEN - 451)) | (1L << (TRUSTED - 451)) | (1L << (VALIDATOR - 451)) | (1L << (UNTIL - 451)) | (1L << (VACUUM - 451)) | (1L << (VOLATILE - 451)) | (1L << (STORED - 451)) | (1L << (WRITE - 451)) | (1L << (STRICT - 451)) | (1L << (TYPES - 451)) | (1L << (WRAPPER - 451)) | (1L << (WORK - 451)) | (1L << (FREEZE - 451)) | (1L << (AUTHORIZATION - 451)) | (1L << (VERBOSE - 451)) | (1L << (PARAM - 451)) | (1L << (OR_ - 451)) | (1L << (PLUS_ - 451)) | (1L << (MINUS_ - 451)))) != 0) || ((((_la - 527)) & ~0x3f) == 0 && ((1L << (_la - 527)) & ((1L << (LP_ - 527)) | (1L << (QUESTION_ - 527)) | (1L << (JSON_EXTRACT_ - 527)) | (1L << (JSON_EXTRACT_TEXT_ - 527)) | (1L << (JSON_PATH_EXTRACT_ - 527)) | (1L << (JSON_PATH_EXTRACT_TEXT_ - 527)) | (1L << (JSONB_CONTAIN_RIGHT_ - 527)) | (1L << (JSONB_CONTAIN_LEFT_ - 527)) | (1L << (JSONB_CONTAIN_ALL_TOP_KEY_ - 527)) | (1L << (JSONB_PATH_DELETE_ - 527)) | (1L << (JSONB_PATH_CONTAIN_ANY_VALUE_ - 527)) | (1L << (JSONB_PATH_PREDICATE_CHECK_ - 527)) | (1L << (IDENTIFIER_ - 527)) | (1L << (STRING_ - 527)) | (1L << (NUMBER_ - 527)))) != 0)) {
					{
					setState(913);
					sliceBound();
					}
				}

				setState(916);
				match(COLON_);
				setState(918);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << INSERT) | (1L << UPDATE) | (1L << DELETE) | (1L << ALTER) | (1L << DROP) | (1L << TRUNCATE) | (1L << SCHEMA) | (1L << REVOKE) | (1L << ADD) | (1L << SET) | (1L << INDEX) | (1L << UNIQUE) | (1L << KEY) | (1L << POSITION) | (1L << FUNCTION) | (1L << TRIGGER) | (1L << PROCEDURE) | (1L << VIEW) | (1L << CASE) | (1L << CAST) | (1L << TRIM) | (1L << SUBSTRING) | (1L << NATURAL) | (1L << JOIN) | (1L << FULL) | (1L << INNER) | (1L << OUTER) | (1L << LEFT) | (1L << RIGHT) | (1L << CROSS) | (1L << IF) | (1L << IS) | (1L << NOT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (NULL - 64)) | (1L << (TRUE - 64)) | (1L << (FALSE - 64)) | (1L << (EXISTS - 64)) | (1L << (LIKE - 64)) | (1L << (BY - 64)) | (1L << (BEGIN - 64)) | (1L << (COMMIT - 64)) | (1L << (ROLLBACK - 64)) | (1L << (SAVEPOINT - 64)) | (1L << (DOUBLE - 64)) | (1L << (ARRAY - 64)) | (1L << (LOCALTIME - 64)) | (1L << (LOCALTIMESTAMP - 64)) | (1L << (YEAR - 64)) | (1L << (MONTH - 64)) | (1L << (DAY - 64)) | (1L << (HOUR - 64)) | (1L << (MINUTE - 64)) | (1L << (SECOND - 64)) | (1L << (DEFAULT - 64)) | (1L << (CURRENT - 64)) | (1L << (ENABLE - 64)) | (1L << (DISABLE - 64)) | (1L << (CALL - 64)) | (1L << (PRESERVE - 64)) | (1L << (DEFINER - 64)) | (1L << (CURRENT_USER - 64)) | (1L << (SQL - 64)) | (1L << (CASCADED - 64)) | (1L << (LOCAL - 64)) | (1L << (CLOSE - 64)) | (1L << (NEXT - 64)) | (1L << (NAME - 64)) | (1L << (COLLATION - 64)) | (1L << (NAMES - 64)) | (1L << (TYPE - 64)))) != 0) || ((((_la - 131)) & ~0x3f) == 0 && ((1L << (_la - 131)) & ((1L << (TEXT - 131)) | (1L << (REPEATABLE - 131)) | (1L << (CURRENT_DATE - 131)) | (1L << (CURRENT_TIME - 131)) | (1L << (CURRENT_TIMESTAMP - 131)) | (1L << (NULLIF - 131)) | (1L << (VARYING - 131)) | (1L << (VALUE - 131)) | (1L << (COALESCE - 131)) | (1L << (TIES - 131)) | (1L << (CUBE - 131)) | (1L << (GROUPING - 131)) | (1L << (SETS - 131)) | (1L << (OTHERS - 131)) | (1L << (OVERLAPS - 131)) | (1L << (AT - 131)) | (1L << (ADMIN - 131)) | (1L << (BINARY - 131)) | (1L << (ESCAPE - 131)) | (1L << (EXCLUDE - 131)) | (1L << (PARTITION - 131)) | (1L << (ROW - 131)) | (1L << (UNKNOWN - 131)) | (1L << (ALWAYS - 131)) | (1L << (CASCADE - 131)) | (1L << (GENERATED - 131)) | (1L << (ISOLATION - 131)) | (1L << (LEVEL - 131)) | (1L << (NO - 131)) | (1L << (OPTION - 131)) | (1L << (PRIVILEGES - 131)) | (1L << (READ - 131)) | (1L << (ROLE - 131)) | (1L << (ROWS - 131)) | (1L << (START - 131)) | (1L << (TRANSACTION - 131)) | (1L << (USER - 131)) | (1L << (ACTION - 131)) | (1L << (CACHE - 131)) | (1L << (CHARACTERISTICS - 131)) | (1L << (CLUSTER - 131)) | (1L << (COMMENTS - 131)) | (1L << (CONCURRENTLY - 131)) | (1L << (CONSTRAINTS - 131)) | (1L << (CYCLE - 131)) | (1L << (DATA - 131)))) != 0) || ((((_la - 195)) & ~0x3f) == 0 && ((1L << (_la - 195)) & ((1L << (DATABASE - 195)) | (1L << (DEFAULTS - 195)) | (1L << (DEFERRED - 195)) | (1L << (DEPENDS - 195)) | (1L << (DOMAIN - 195)) | (1L << (EXCLUDING - 195)) | (1L << (EXECUTE - 195)) | (1L << (EXTENSION - 195)) | (1L << (EXTERNAL - 195)) | (1L << (EXTRACT - 195)) | (1L << (FILTER - 195)) | (1L << (FIRST - 195)) | (1L << (FOLLOWING - 195)) | (1L << (FORCE - 195)) | (1L << (GLOBAL - 195)) | (1L << (IDENTITY - 195)) | (1L << (IMMEDIATE - 195)) | (1L << (INCLUDING - 195)) | (1L << (INCREMENT - 195)) | (1L << (INDEXES - 195)) | (1L << (INHERIT - 195)) | (1L << (INHERITS - 195)) | (1L << (INCLUDE - 195)) | (1L << (LANGUAGE - 195)) | (1L << (LARGE - 195)) | (1L << (LAST - 195)) | (1L << (LOGGED - 195)) | (1L << (MATCH - 195)) | (1L << (MAXVALUE - 195)) | (1L << (MINVALUE - 195)) | (1L << (NOTHING - 195)) | (1L << (NULLS - 195)) | (1L << (OBJECT - 195)) | (1L << (OIDS - 195)) | (1L << (OVER - 195)) | (1L << (OWNED - 195)) | (1L << (OWNER - 195)) | (1L << (PARTIAL - 195)) | (1L << (PRECEDING - 195)) | (1L << (RANGE - 195)) | (1L << (RENAME - 195)) | (1L << (REPLICA - 195)) | (1L << (RESET - 195)) | (1L << (RESTART - 195)) | (1L << (RESTRICT - 195)) | (1L << (ROUTINE - 195)) | (1L << (RULE - 195)) | (1L << (SECURITY - 195)) | (1L << (SEQUENCE - 195)) | (1L << (SESSION - 195)) | (1L << (SESSION_USER - 195)) | (1L << (SHOW - 195)) | (1L << (SIMPLE - 195)) | (1L << (STATISTICS - 195)) | (1L << (STORAGE - 195)) | (1L << (TABLESPACE - 195)) | (1L << (TEMP - 195)) | (1L << (TEMPORARY - 195)))) != 0) || ((((_la - 259)) & ~0x3f) == 0 && ((1L << (_la - 259)) & ((1L << (UNBOUNDED - 259)) | (1L << (UNLOGGED - 259)) | (1L << (VALID - 259)) | (1L << (VALIDATE - 259)) | (1L << (WITHIN - 259)) | (1L << (WITHOUT - 259)) | (1L << (ZONE - 259)) | (1L << (OF - 259)) | (1L << (UESCAPE - 259)) | (1L << (GROUPS - 259)) | (1L << (RECURSIVE - 259)) | (1L << (ENUM - 259)) | (1L << (XML - 259)) | (1L << (JSON - 259)) | (1L << (TABLESAMPLE - 259)) | (1L << (ORDINALITY - 259)) | (1L << (CURRENT_ROLE - 259)) | (1L << (CURRENT_CATALOG - 259)) | (1L << (CURRENT_SCHEMA - 259)) | (1L << (NORMALIZE - 259)) | (1L << (OVERLAY - 259)) | (1L << (XMLCONCAT - 259)) | (1L << (XMLELEMENT - 259)) | (1L << (XMLEXISTS - 259)) | (1L << (XMLFOREST - 259)) | (1L << (XMLPARSE - 259)) | (1L << (XMLPI - 259)) | (1L << (XMLROOT - 259)) | (1L << (XMLSERIALIZE - 259)))) != 0) || ((((_la - 323)) & ~0x3f) == 0 && ((1L << (_la - 323)) & ((1L << (TREAT - 323)) | (1L << (NFC - 323)) | (1L << (NFD - 323)) | (1L << (NFKC - 323)) | (1L << (NFKD - 323)) | (1L << (REF - 323)) | (1L << (PASSING - 323)) | (1L << (VERSION - 323)) | (1L << (YES - 323)) | (1L << (STANDALONE - 323)) | (1L << (GREATEST - 323)) | (1L << (LEAST - 323)) | (1L << (MATERIALIZED - 323)) | (1L << (OPERATOR - 323)) | (1L << (SHARE - 323)) | (1L << (ROLLUP - 323)) | (1L << (ILIKE - 323)) | (1L << (SIMILAR - 323)) | (1L << (ISNULL - 323)) | (1L << (NOTNULL - 323)) | (1L << (DOCUMENT - 323)) | (1L << (NORMALIZED - 323)) | (1L << (NOWAIT - 323)) | (1L << (LOCKED - 323)) | (1L << (COLUMNS - 323)) | (1L << (CONTENT - 323)) | (1L << (STRIP - 323)) | (1L << (WHITESPACE - 323)) | (1L << (CONFLICT - 323)) | (1L << (OVERRIDING - 323)) | (1L << (SYSTEM - 323)) | (1L << (ABORT - 323)) | (1L << (ABSOLUTE - 323)) | (1L << (ACCESS - 323)) | (1L << (AFTER - 323)) | (1L << (AGGREGATE - 323)) | (1L << (ALSO - 323)) | (1L << (ATTACH - 323)) | (1L << (ATTRIBUTE - 323)) | (1L << (BACKWARD - 323)) | (1L << (BEFORE - 323)) | (1L << (ASSERTION - 323)) | (1L << (ASSIGNMENT - 323)) | (1L << (CONTINUE - 323)) | (1L << (CONVERSION - 323)) | (1L << (COPY - 323)) | (1L << (COST - 323)) | (1L << (CSV - 323)) | (1L << (CALLED - 323)) | (1L << (CATALOG - 323)) | (1L << (CHAIN - 323)))) != 0) || ((((_la - 387)) & ~0x3f) == 0 && ((1L << (_la - 387)) & ((1L << (CHECKPOINT - 387)) | (1L << (CLASS - 387)) | (1L << (CONFIGURATION - 387)) | (1L << (COMMENT - 387)) | (1L << (DETACH - 387)) | (1L << (DICTIONARY - 387)) | (1L << (EXPRESSION - 387)) | (1L << (INSENSITIVE - 387)) | (1L << (DISCARD - 387)) | (1L << (OFF - 387)) | (1L << (INSTEAD - 387)) | (1L << (EXPLAIN - 387)) | (1L << (INPUT - 387)) | (1L << (INLINE - 387)) | (1L << (PARALLEL - 387)) | (1L << (LEAKPROOF - 387)) | (1L << (COMMITTED - 387)) | (1L << (ENCODING - 387)) | (1L << (IMPLICIT - 387)) | (1L << (DELIMITER - 387)) | (1L << (CURSOR - 387)) | (1L << (EACH - 387)) | (1L << (EVENT - 387)) | (1L << (DEALLOCATE - 387)) | (1L << (CONNECTION - 387)) | (1L << (DECLARE - 387)) | (1L << (FAMILY - 387)) | (1L << (FORWARD - 387)) | (1L << (EXCLUSIVE - 387)) | (1L << (FUNCTIONS - 387)) | (1L << (LOCATION - 387)) | (1L << (LABEL - 387)) | (1L << (DELIMITERS - 387)) | (1L << (HANDLER - 387)) | (1L << (HEADER - 387)) | (1L << (IMMUTABLE - 387)) | (1L << (GRANTED - 387)) | (1L << (HOLD - 387)) | (1L << (MAPPING - 387)) | (1L << (OLD - 387)) | (1L << (METHOD - 387)) | (1L << (LOAD - 387)) | (1L << (LISTEN - 387)) | (1L << (MODE - 387)) | (1L << (MOVE - 387)) | (1L << (PROCEDURAL - 387)) | (1L << (PARSER - 387)) | (1L << (PROCEDURES - 387)) | (1L << (ENCRYPTED - 387)) | (1L << (PUBLICATION - 387)) | (1L << (PROGRAM - 387)) | (1L << (REFERENCING - 387)) | (1L << (PLANS - 387)) | (1L << (REINDEX - 387)) | (1L << (PRIOR - 387)) | (1L << (PASSWORD - 387)) | (1L << (RELATIVE - 387)) | (1L << (QUOTE - 387)) | (1L << (ROUTINES - 387)) | (1L << (REPLACE - 387)) | (1L << (SNAPSHOT - 387)) | (1L << (REFRESH - 387)) | (1L << (PREPARE - 387)) | (1L << (OPTIONS - 387)))) != 0) || ((((_la - 451)) & ~0x3f) == 0 && ((1L << (_la - 451)) & ((1L << (IMPORT - 451)) | (1L << (INVOKER - 451)) | (1L << (NEW - 451)) | (1L << (PREPARED - 451)) | (1L << (SCROLL - 451)) | (1L << (SEQUENCES - 451)) | (1L << (SYSID - 451)) | (1L << (REASSIGN - 451)) | (1L << (SERVER - 451)) | (1L << (SUBSCRIPTION - 451)) | (1L << (SEARCH - 451)) | (1L << (SCHEMAS - 451)) | (1L << (RECHECK - 451)) | (1L << (POLICY - 451)) | (1L << (NOTIFY - 451)) | (1L << (LOCK - 451)) | (1L << (RELEASE - 451)) | (1L << (SERIALIZABLE - 451)) | (1L << (RETURNS - 451)) | (1L << (STATEMENT - 451)) | (1L << (STDIN - 451)) | (1L << (STDOUT - 451)) | (1L << (TABLES - 451)) | (1L << (SUPPORT - 451)) | (1L << (STABLE - 451)) | (1L << (TEMPLATE - 451)) | (1L << (UNENCRYPTED - 451)) | (1L << (VIEWS - 451)) | (1L << (UNCOMMITTED - 451)) | (1L << (TRANSFORM - 451)) | (1L << (UNLISTEN - 451)) | (1L << (TRUSTED - 451)) | (1L << (VALIDATOR - 451)) | (1L << (UNTIL - 451)) | (1L << (VACUUM - 451)) | (1L << (VOLATILE - 451)) | (1L << (STORED - 451)) | (1L << (WRITE - 451)) | (1L << (STRICT - 451)) | (1L << (TYPES - 451)) | (1L << (WRAPPER - 451)) | (1L << (WORK - 451)) | (1L << (FREEZE - 451)) | (1L << (AUTHORIZATION - 451)) | (1L << (VERBOSE - 451)) | (1L << (PARAM - 451)) | (1L << (OR_ - 451)) | (1L << (PLUS_ - 451)) | (1L << (MINUS_ - 451)))) != 0) || ((((_la - 527)) & ~0x3f) == 0 && ((1L << (_la - 527)) & ((1L << (LP_ - 527)) | (1L << (QUESTION_ - 527)) | (1L << (JSON_EXTRACT_ - 527)) | (1L << (JSON_EXTRACT_TEXT_ - 527)) | (1L << (JSON_PATH_EXTRACT_ - 527)) | (1L << (JSON_PATH_EXTRACT_TEXT_ - 527)) | (1L << (JSONB_CONTAIN_RIGHT_ - 527)) | (1L << (JSONB_CONTAIN_LEFT_ - 527)) | (1L << (JSONB_CONTAIN_ALL_TOP_KEY_ - 527)) | (1L << (JSONB_PATH_DELETE_ - 527)) | (1L << (JSONB_PATH_CONTAIN_ANY_VALUE_ - 527)) | (1L << (JSONB_PATH_PREDICATE_CHECK_ - 527)) | (1L << (IDENTIFIER_ - 527)) | (1L << (STRING_ - 527)) | (1L << (NUMBER_ - 527)))) != 0)) {
					{
					setState(917);
					sliceBound();
					}
				}

				setState(920);
				match(RBT_);
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

	public static class SliceBoundContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public SliceBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sliceBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSliceBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSliceBound(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSliceBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SliceBoundContext sliceBound() throws RecognitionException {
		SliceBoundContext _localctx = new SliceBoundContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_sliceBound);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(923);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InExprContext extends ParserRuleContext {
		public SelectWithParensContext selectWithParens() {
			return getRuleContext(SelectWithParensContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public InExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterInExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitInExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitInExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InExprContext inExpr() throws RecognitionException {
		InExprContext _localctx = new InExprContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_inExpr);
		try {
			setState(930);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__2:
				enterOuterAlt(_localctx, 1);
				{
				setState(925);
				selectWithParens();
				}
				break;
			case LP_:
				enterOuterAlt(_localctx, 2);
				{
				setState(926);
				match(LP_);
				setState(927);
				exprList(0);
				setState(928);
				match(RP_);
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

	public static class CaseExprContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(BaseRuleParser.CASE, 0); }
		public WhenClauseListContext whenClauseList() {
			return getRuleContext(WhenClauseListContext.class,0);
		}
		public TerminalNode END() { return getToken(BaseRuleParser.END, 0); }
		public CaseArgContext caseArg() {
			return getRuleContext(CaseArgContext.class,0);
		}
		public CaseDefaultContext caseDefault() {
			return getRuleContext(CaseDefaultContext.class,0);
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
		enterRule(_localctx, 64, RULE_caseExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(932);
			match(CASE);
			setState(934);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << INSERT) | (1L << UPDATE) | (1L << DELETE) | (1L << ALTER) | (1L << DROP) | (1L << TRUNCATE) | (1L << SCHEMA) | (1L << REVOKE) | (1L << ADD) | (1L << SET) | (1L << INDEX) | (1L << UNIQUE) | (1L << KEY) | (1L << POSITION) | (1L << FUNCTION) | (1L << TRIGGER) | (1L << PROCEDURE) | (1L << VIEW) | (1L << CASE) | (1L << CAST) | (1L << TRIM) | (1L << SUBSTRING) | (1L << NATURAL) | (1L << JOIN) | (1L << FULL) | (1L << INNER) | (1L << OUTER) | (1L << LEFT) | (1L << RIGHT) | (1L << CROSS) | (1L << IF) | (1L << IS) | (1L << NOT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (NULL - 64)) | (1L << (TRUE - 64)) | (1L << (FALSE - 64)) | (1L << (EXISTS - 64)) | (1L << (LIKE - 64)) | (1L << (BY - 64)) | (1L << (BEGIN - 64)) | (1L << (COMMIT - 64)) | (1L << (ROLLBACK - 64)) | (1L << (SAVEPOINT - 64)) | (1L << (DOUBLE - 64)) | (1L << (ARRAY - 64)) | (1L << (LOCALTIME - 64)) | (1L << (LOCALTIMESTAMP - 64)) | (1L << (YEAR - 64)) | (1L << (MONTH - 64)) | (1L << (DAY - 64)) | (1L << (HOUR - 64)) | (1L << (MINUTE - 64)) | (1L << (SECOND - 64)) | (1L << (DEFAULT - 64)) | (1L << (CURRENT - 64)) | (1L << (ENABLE - 64)) | (1L << (DISABLE - 64)) | (1L << (CALL - 64)) | (1L << (PRESERVE - 64)) | (1L << (DEFINER - 64)) | (1L << (CURRENT_USER - 64)) | (1L << (SQL - 64)) | (1L << (CASCADED - 64)) | (1L << (LOCAL - 64)) | (1L << (CLOSE - 64)) | (1L << (NEXT - 64)) | (1L << (NAME - 64)) | (1L << (COLLATION - 64)) | (1L << (NAMES - 64)) | (1L << (TYPE - 64)))) != 0) || ((((_la - 131)) & ~0x3f) == 0 && ((1L << (_la - 131)) & ((1L << (TEXT - 131)) | (1L << (REPEATABLE - 131)) | (1L << (CURRENT_DATE - 131)) | (1L << (CURRENT_TIME - 131)) | (1L << (CURRENT_TIMESTAMP - 131)) | (1L << (NULLIF - 131)) | (1L << (VARYING - 131)) | (1L << (VALUE - 131)) | (1L << (COALESCE - 131)) | (1L << (TIES - 131)) | (1L << (CUBE - 131)) | (1L << (GROUPING - 131)) | (1L << (SETS - 131)) | (1L << (OTHERS - 131)) | (1L << (OVERLAPS - 131)) | (1L << (AT - 131)) | (1L << (ADMIN - 131)) | (1L << (BINARY - 131)) | (1L << (ESCAPE - 131)) | (1L << (EXCLUDE - 131)) | (1L << (PARTITION - 131)) | (1L << (ROW - 131)) | (1L << (UNKNOWN - 131)) | (1L << (ALWAYS - 131)) | (1L << (CASCADE - 131)) | (1L << (GENERATED - 131)) | (1L << (ISOLATION - 131)) | (1L << (LEVEL - 131)) | (1L << (NO - 131)) | (1L << (OPTION - 131)) | (1L << (PRIVILEGES - 131)) | (1L << (READ - 131)) | (1L << (ROLE - 131)) | (1L << (ROWS - 131)) | (1L << (START - 131)) | (1L << (TRANSACTION - 131)) | (1L << (USER - 131)) | (1L << (ACTION - 131)) | (1L << (CACHE - 131)) | (1L << (CHARACTERISTICS - 131)) | (1L << (CLUSTER - 131)) | (1L << (COMMENTS - 131)) | (1L << (CONCURRENTLY - 131)) | (1L << (CONSTRAINTS - 131)) | (1L << (CYCLE - 131)) | (1L << (DATA - 131)))) != 0) || ((((_la - 195)) & ~0x3f) == 0 && ((1L << (_la - 195)) & ((1L << (DATABASE - 195)) | (1L << (DEFAULTS - 195)) | (1L << (DEFERRED - 195)) | (1L << (DEPENDS - 195)) | (1L << (DOMAIN - 195)) | (1L << (EXCLUDING - 195)) | (1L << (EXECUTE - 195)) | (1L << (EXTENSION - 195)) | (1L << (EXTERNAL - 195)) | (1L << (EXTRACT - 195)) | (1L << (FILTER - 195)) | (1L << (FIRST - 195)) | (1L << (FOLLOWING - 195)) | (1L << (FORCE - 195)) | (1L << (GLOBAL - 195)) | (1L << (IDENTITY - 195)) | (1L << (IMMEDIATE - 195)) | (1L << (INCLUDING - 195)) | (1L << (INCREMENT - 195)) | (1L << (INDEXES - 195)) | (1L << (INHERIT - 195)) | (1L << (INHERITS - 195)) | (1L << (INCLUDE - 195)) | (1L << (LANGUAGE - 195)) | (1L << (LARGE - 195)) | (1L << (LAST - 195)) | (1L << (LOGGED - 195)) | (1L << (MATCH - 195)) | (1L << (MAXVALUE - 195)) | (1L << (MINVALUE - 195)) | (1L << (NOTHING - 195)) | (1L << (NULLS - 195)) | (1L << (OBJECT - 195)) | (1L << (OIDS - 195)) | (1L << (OVER - 195)) | (1L << (OWNED - 195)) | (1L << (OWNER - 195)) | (1L << (PARTIAL - 195)) | (1L << (PRECEDING - 195)) | (1L << (RANGE - 195)) | (1L << (RENAME - 195)) | (1L << (REPLICA - 195)) | (1L << (RESET - 195)) | (1L << (RESTART - 195)) | (1L << (RESTRICT - 195)) | (1L << (ROUTINE - 195)) | (1L << (RULE - 195)) | (1L << (SECURITY - 195)) | (1L << (SEQUENCE - 195)) | (1L << (SESSION - 195)) | (1L << (SESSION_USER - 195)) | (1L << (SHOW - 195)) | (1L << (SIMPLE - 195)) | (1L << (STATISTICS - 195)) | (1L << (STORAGE - 195)) | (1L << (TABLESPACE - 195)) | (1L << (TEMP - 195)) | (1L << (TEMPORARY - 195)))) != 0) || ((((_la - 259)) & ~0x3f) == 0 && ((1L << (_la - 259)) & ((1L << (UNBOUNDED - 259)) | (1L << (UNLOGGED - 259)) | (1L << (VALID - 259)) | (1L << (VALIDATE - 259)) | (1L << (WITHIN - 259)) | (1L << (WITHOUT - 259)) | (1L << (ZONE - 259)) | (1L << (OF - 259)) | (1L << (UESCAPE - 259)) | (1L << (GROUPS - 259)) | (1L << (RECURSIVE - 259)) | (1L << (ENUM - 259)) | (1L << (XML - 259)) | (1L << (JSON - 259)) | (1L << (TABLESAMPLE - 259)) | (1L << (ORDINALITY - 259)) | (1L << (CURRENT_ROLE - 259)) | (1L << (CURRENT_CATALOG - 259)) | (1L << (CURRENT_SCHEMA - 259)) | (1L << (NORMALIZE - 259)) | (1L << (OVERLAY - 259)) | (1L << (XMLCONCAT - 259)) | (1L << (XMLELEMENT - 259)) | (1L << (XMLEXISTS - 259)) | (1L << (XMLFOREST - 259)) | (1L << (XMLPARSE - 259)) | (1L << (XMLPI - 259)) | (1L << (XMLROOT - 259)) | (1L << (XMLSERIALIZE - 259)))) != 0) || ((((_la - 323)) & ~0x3f) == 0 && ((1L << (_la - 323)) & ((1L << (TREAT - 323)) | (1L << (NFC - 323)) | (1L << (NFD - 323)) | (1L << (NFKC - 323)) | (1L << (NFKD - 323)) | (1L << (REF - 323)) | (1L << (PASSING - 323)) | (1L << (VERSION - 323)) | (1L << (YES - 323)) | (1L << (STANDALONE - 323)) | (1L << (GREATEST - 323)) | (1L << (LEAST - 323)) | (1L << (MATERIALIZED - 323)) | (1L << (OPERATOR - 323)) | (1L << (SHARE - 323)) | (1L << (ROLLUP - 323)) | (1L << (ILIKE - 323)) | (1L << (SIMILAR - 323)) | (1L << (ISNULL - 323)) | (1L << (NOTNULL - 323)) | (1L << (DOCUMENT - 323)) | (1L << (NORMALIZED - 323)) | (1L << (NOWAIT - 323)) | (1L << (LOCKED - 323)) | (1L << (COLUMNS - 323)) | (1L << (CONTENT - 323)) | (1L << (STRIP - 323)) | (1L << (WHITESPACE - 323)) | (1L << (CONFLICT - 323)) | (1L << (OVERRIDING - 323)) | (1L << (SYSTEM - 323)) | (1L << (ABORT - 323)) | (1L << (ABSOLUTE - 323)) | (1L << (ACCESS - 323)) | (1L << (AFTER - 323)) | (1L << (AGGREGATE - 323)) | (1L << (ALSO - 323)) | (1L << (ATTACH - 323)) | (1L << (ATTRIBUTE - 323)) | (1L << (BACKWARD - 323)) | (1L << (BEFORE - 323)) | (1L << (ASSERTION - 323)) | (1L << (ASSIGNMENT - 323)) | (1L << (CONTINUE - 323)) | (1L << (CONVERSION - 323)) | (1L << (COPY - 323)) | (1L << (COST - 323)) | (1L << (CSV - 323)) | (1L << (CALLED - 323)) | (1L << (CATALOG - 323)) | (1L << (CHAIN - 323)))) != 0) || ((((_la - 387)) & ~0x3f) == 0 && ((1L << (_la - 387)) & ((1L << (CHECKPOINT - 387)) | (1L << (CLASS - 387)) | (1L << (CONFIGURATION - 387)) | (1L << (COMMENT - 387)) | (1L << (DETACH - 387)) | (1L << (DICTIONARY - 387)) | (1L << (EXPRESSION - 387)) | (1L << (INSENSITIVE - 387)) | (1L << (DISCARD - 387)) | (1L << (OFF - 387)) | (1L << (INSTEAD - 387)) | (1L << (EXPLAIN - 387)) | (1L << (INPUT - 387)) | (1L << (INLINE - 387)) | (1L << (PARALLEL - 387)) | (1L << (LEAKPROOF - 387)) | (1L << (COMMITTED - 387)) | (1L << (ENCODING - 387)) | (1L << (IMPLICIT - 387)) | (1L << (DELIMITER - 387)) | (1L << (CURSOR - 387)) | (1L << (EACH - 387)) | (1L << (EVENT - 387)) | (1L << (DEALLOCATE - 387)) | (1L << (CONNECTION - 387)) | (1L << (DECLARE - 387)) | (1L << (FAMILY - 387)) | (1L << (FORWARD - 387)) | (1L << (EXCLUSIVE - 387)) | (1L << (FUNCTIONS - 387)) | (1L << (LOCATION - 387)) | (1L << (LABEL - 387)) | (1L << (DELIMITERS - 387)) | (1L << (HANDLER - 387)) | (1L << (HEADER - 387)) | (1L << (IMMUTABLE - 387)) | (1L << (GRANTED - 387)) | (1L << (HOLD - 387)) | (1L << (MAPPING - 387)) | (1L << (OLD - 387)) | (1L << (METHOD - 387)) | (1L << (LOAD - 387)) | (1L << (LISTEN - 387)) | (1L << (MODE - 387)) | (1L << (MOVE - 387)) | (1L << (PROCEDURAL - 387)) | (1L << (PARSER - 387)) | (1L << (PROCEDURES - 387)) | (1L << (ENCRYPTED - 387)) | (1L << (PUBLICATION - 387)) | (1L << (PROGRAM - 387)) | (1L << (REFERENCING - 387)) | (1L << (PLANS - 387)) | (1L << (REINDEX - 387)) | (1L << (PRIOR - 387)) | (1L << (PASSWORD - 387)) | (1L << (RELATIVE - 387)) | (1L << (QUOTE - 387)) | (1L << (ROUTINES - 387)) | (1L << (REPLACE - 387)) | (1L << (SNAPSHOT - 387)) | (1L << (REFRESH - 387)) | (1L << (PREPARE - 387)) | (1L << (OPTIONS - 387)))) != 0) || ((((_la - 451)) & ~0x3f) == 0 && ((1L << (_la - 451)) & ((1L << (IMPORT - 451)) | (1L << (INVOKER - 451)) | (1L << (NEW - 451)) | (1L << (PREPARED - 451)) | (1L << (SCROLL - 451)) | (1L << (SEQUENCES - 451)) | (1L << (SYSID - 451)) | (1L << (REASSIGN - 451)) | (1L << (SERVER - 451)) | (1L << (SUBSCRIPTION - 451)) | (1L << (SEARCH - 451)) | (1L << (SCHEMAS - 451)) | (1L << (RECHECK - 451)) | (1L << (POLICY - 451)) | (1L << (NOTIFY - 451)) | (1L << (LOCK - 451)) | (1L << (RELEASE - 451)) | (1L << (SERIALIZABLE - 451)) | (1L << (RETURNS - 451)) | (1L << (STATEMENT - 451)) | (1L << (STDIN - 451)) | (1L << (STDOUT - 451)) | (1L << (TABLES - 451)) | (1L << (SUPPORT - 451)) | (1L << (STABLE - 451)) | (1L << (TEMPLATE - 451)) | (1L << (UNENCRYPTED - 451)) | (1L << (VIEWS - 451)) | (1L << (UNCOMMITTED - 451)) | (1L << (TRANSFORM - 451)) | (1L << (UNLISTEN - 451)) | (1L << (TRUSTED - 451)) | (1L << (VALIDATOR - 451)) | (1L << (UNTIL - 451)) | (1L << (VACUUM - 451)) | (1L << (VOLATILE - 451)) | (1L << (STORED - 451)) | (1L << (WRITE - 451)) | (1L << (STRICT - 451)) | (1L << (TYPES - 451)) | (1L << (WRAPPER - 451)) | (1L << (WORK - 451)) | (1L << (FREEZE - 451)) | (1L << (AUTHORIZATION - 451)) | (1L << (VERBOSE - 451)) | (1L << (PARAM - 451)) | (1L << (OR_ - 451)) | (1L << (PLUS_ - 451)) | (1L << (MINUS_ - 451)))) != 0) || ((((_la - 527)) & ~0x3f) == 0 && ((1L << (_la - 527)) & ((1L << (LP_ - 527)) | (1L << (QUESTION_ - 527)) | (1L << (JSON_EXTRACT_ - 527)) | (1L << (JSON_EXTRACT_TEXT_ - 527)) | (1L << (JSON_PATH_EXTRACT_ - 527)) | (1L << (JSON_PATH_EXTRACT_TEXT_ - 527)) | (1L << (JSONB_CONTAIN_RIGHT_ - 527)) | (1L << (JSONB_CONTAIN_LEFT_ - 527)) | (1L << (JSONB_CONTAIN_ALL_TOP_KEY_ - 527)) | (1L << (JSONB_PATH_DELETE_ - 527)) | (1L << (JSONB_PATH_CONTAIN_ANY_VALUE_ - 527)) | (1L << (JSONB_PATH_PREDICATE_CHECK_ - 527)) | (1L << (IDENTIFIER_ - 527)) | (1L << (STRING_ - 527)) | (1L << (NUMBER_ - 527)))) != 0)) {
				{
				setState(933);
				caseArg();
				}
			}

			setState(936);
			whenClauseList();
			setState(938);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(937);
				caseDefault();
				}
			}

			setState(940);
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

	public static class WhenClauseListContext extends ParserRuleContext {
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public WhenClauseListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClauseList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWhenClauseList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWhenClauseList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWhenClauseList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseListContext whenClauseList() throws RecognitionException {
		WhenClauseListContext _localctx = new WhenClauseListContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_whenClauseList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(943); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(942);
				whenClause();
				}
				}
				setState(945); 
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

	public static class WhenClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(BaseRuleParser.WHEN, 0); }
		public List<AExprContext> aExpr() {
			return getRuleContexts(AExprContext.class);
		}
		public AExprContext aExpr(int i) {
			return getRuleContext(AExprContext.class,i);
		}
		public TerminalNode THEN() { return getToken(BaseRuleParser.THEN, 0); }
		public WhenClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWhenClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWhenClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWhenClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(947);
			match(WHEN);
			setState(948);
			aExpr(0);
			setState(949);
			match(THEN);
			setState(950);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CaseDefaultContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(BaseRuleParser.ELSE, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public CaseDefaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseDefault; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCaseDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCaseDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCaseDefault(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseDefaultContext caseDefault() throws RecognitionException {
		CaseDefaultContext _localctx = new CaseDefaultContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_caseDefault);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(952);
			match(ELSE);
			setState(953);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CaseArgContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public CaseArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCaseArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCaseArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCaseArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseArgContext caseArg() throws RecognitionException {
		CaseArgContext _localctx = new CaseArgContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_caseArg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(955);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnrefContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public IndirectionContext indirection() {
			return getRuleContext(IndirectionContext.class,0);
		}
		public ColumnrefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnref(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnref(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnrefContext columnref() throws RecognitionException {
		ColumnrefContext _localctx = new ColumnrefContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_columnref);
		try {
			setState(961);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(957);
				colId();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(958);
				colId();
				setState(959);
				indirection(0);
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

	public static class QualOpContext extends ParserRuleContext {
		public JsonOperatorContext jsonOperator() {
			return getRuleContext(JsonOperatorContext.class,0);
		}
		public TerminalNode OPERATOR() { return getToken(BaseRuleParser.OPERATOR, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AnyOperatorContext anyOperator() {
			return getRuleContext(AnyOperatorContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public QualOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterQualOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitQualOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitQualOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualOpContext qualOp() throws RecognitionException {
		QualOpContext _localctx = new QualOpContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_qualOp);
		try {
			setState(969);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OR_:
			case MINUS_:
			case QUESTION_:
			case JSON_EXTRACT_:
			case JSON_EXTRACT_TEXT_:
			case JSON_PATH_EXTRACT_:
			case JSON_PATH_EXTRACT_TEXT_:
			case JSONB_CONTAIN_RIGHT_:
			case JSONB_CONTAIN_LEFT_:
			case JSONB_CONTAIN_ALL_TOP_KEY_:
			case JSONB_PATH_DELETE_:
			case JSONB_PATH_CONTAIN_ANY_VALUE_:
			case JSONB_PATH_PREDICATE_CHECK_:
				enterOuterAlt(_localctx, 1);
				{
				setState(963);
				jsonOperator();
				}
				break;
			case OPERATOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(964);
				match(OPERATOR);
				setState(965);
				match(LP_);
				setState(966);
				anyOperator();
				setState(967);
				match(RP_);
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

	public static class SubqueryOpContext extends ParserRuleContext {
		public AllOpContext allOp() {
			return getRuleContext(AllOpContext.class,0);
		}
		public TerminalNode OPERATOR() { return getToken(BaseRuleParser.OPERATOR, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AnyOperatorContext anyOperator() {
			return getRuleContext(AnyOperatorContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode LIKE() { return getToken(BaseRuleParser.LIKE, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode TILDE_() { return getToken(BaseRuleParser.TILDE_, 0); }
		public TerminalNode NOT_() { return getToken(BaseRuleParser.NOT_, 0); }
		public SubqueryOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subqueryOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubqueryOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubqueryOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubqueryOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubqueryOpContext subqueryOp() throws RecognitionException {
		SubqueryOpContext _localctx = new SubqueryOpContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_subqueryOp);
		try {
			setState(983);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(971);
				allOp();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(972);
				match(OPERATOR);
				setState(973);
				match(LP_);
				setState(974);
				anyOperator();
				setState(975);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(977);
				match(LIKE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(978);
				match(NOT);
				setState(979);
				match(LIKE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(980);
				match(TILDE_);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(981);
				match(NOT_);
				setState(982);
				match(TILDE_);
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

	public static class AllOpContext extends ParserRuleContext {
		public OpContext op() {
			return getRuleContext(OpContext.class,0);
		}
		public MathOperatorContext mathOperator() {
			return getRuleContext(MathOperatorContext.class,0);
		}
		public AllOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAllOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAllOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAllOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllOpContext allOp() throws RecognitionException {
		AllOpContext _localctx = new AllOpContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_allOp);
		try {
			setState(987);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(985);
				op();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(986);
				mathOperator();
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

	public static class OpContext extends ParserRuleContext {
		public List<TerminalNode> AND_() { return getTokens(BaseRuleParser.AND_); }
		public TerminalNode AND_(int i) {
			return getToken(BaseRuleParser.AND_, i);
		}
		public List<TerminalNode> OR_() { return getTokens(BaseRuleParser.OR_); }
		public TerminalNode OR_(int i) {
			return getToken(BaseRuleParser.OR_, i);
		}
		public List<TerminalNode> NOT_() { return getTokens(BaseRuleParser.NOT_); }
		public TerminalNode NOT_(int i) {
			return getToken(BaseRuleParser.NOT_, i);
		}
		public List<TerminalNode> TILDE_() { return getTokens(BaseRuleParser.TILDE_); }
		public TerminalNode TILDE_(int i) {
			return getToken(BaseRuleParser.TILDE_, i);
		}
		public List<TerminalNode> VERTICAL_BAR_() { return getTokens(BaseRuleParser.VERTICAL_BAR_); }
		public TerminalNode VERTICAL_BAR_(int i) {
			return getToken(BaseRuleParser.VERTICAL_BAR_, i);
		}
		public List<TerminalNode> AMPERSAND_() { return getTokens(BaseRuleParser.AMPERSAND_); }
		public TerminalNode AMPERSAND_(int i) {
			return getToken(BaseRuleParser.AMPERSAND_, i);
		}
		public List<TerminalNode> SIGNED_LEFT_SHIFT_() { return getTokens(BaseRuleParser.SIGNED_LEFT_SHIFT_); }
		public TerminalNode SIGNED_LEFT_SHIFT_(int i) {
			return getToken(BaseRuleParser.SIGNED_LEFT_SHIFT_, i);
		}
		public List<TerminalNode> SIGNED_RIGHT_SHIFT_() { return getTokens(BaseRuleParser.SIGNED_RIGHT_SHIFT_); }
		public TerminalNode SIGNED_RIGHT_SHIFT_(int i) {
			return getToken(BaseRuleParser.SIGNED_RIGHT_SHIFT_, i);
		}
		public List<TerminalNode> CARET_() { return getTokens(BaseRuleParser.CARET_); }
		public TerminalNode CARET_(int i) {
			return getToken(BaseRuleParser.CARET_, i);
		}
		public List<TerminalNode> MOD_() { return getTokens(BaseRuleParser.MOD_); }
		public TerminalNode MOD_(int i) {
			return getToken(BaseRuleParser.MOD_, i);
		}
		public List<TerminalNode> COLON_() { return getTokens(BaseRuleParser.COLON_); }
		public TerminalNode COLON_(int i) {
			return getToken(BaseRuleParser.COLON_, i);
		}
		public List<TerminalNode> PLUS_() { return getTokens(BaseRuleParser.PLUS_); }
		public TerminalNode PLUS_(int i) {
			return getToken(BaseRuleParser.PLUS_, i);
		}
		public List<TerminalNode> MINUS_() { return getTokens(BaseRuleParser.MINUS_); }
		public TerminalNode MINUS_(int i) {
			return getToken(BaseRuleParser.MINUS_, i);
		}
		public List<TerminalNode> ASTERISK_() { return getTokens(BaseRuleParser.ASTERISK_); }
		public TerminalNode ASTERISK_(int i) {
			return getToken(BaseRuleParser.ASTERISK_, i);
		}
		public List<TerminalNode> SLASH_() { return getTokens(BaseRuleParser.SLASH_); }
		public TerminalNode SLASH_(int i) {
			return getToken(BaseRuleParser.SLASH_, i);
		}
		public List<TerminalNode> BACKSLASH_() { return getTokens(BaseRuleParser.BACKSLASH_); }
		public TerminalNode BACKSLASH_(int i) {
			return getToken(BaseRuleParser.BACKSLASH_, i);
		}
		public List<TerminalNode> DOT_() { return getTokens(BaseRuleParser.DOT_); }
		public TerminalNode DOT_(int i) {
			return getToken(BaseRuleParser.DOT_, i);
		}
		public List<TerminalNode> DOT_ASTERISK_() { return getTokens(BaseRuleParser.DOT_ASTERISK_); }
		public TerminalNode DOT_ASTERISK_(int i) {
			return getToken(BaseRuleParser.DOT_ASTERISK_, i);
		}
		public List<TerminalNode> SAFE_EQ_() { return getTokens(BaseRuleParser.SAFE_EQ_); }
		public TerminalNode SAFE_EQ_(int i) {
			return getToken(BaseRuleParser.SAFE_EQ_, i);
		}
		public List<TerminalNode> DEQ_() { return getTokens(BaseRuleParser.DEQ_); }
		public TerminalNode DEQ_(int i) {
			return getToken(BaseRuleParser.DEQ_, i);
		}
		public List<TerminalNode> EQ_() { return getTokens(BaseRuleParser.EQ_); }
		public TerminalNode EQ_(int i) {
			return getToken(BaseRuleParser.EQ_, i);
		}
		public List<TerminalNode> CQ_() { return getTokens(BaseRuleParser.CQ_); }
		public TerminalNode CQ_(int i) {
			return getToken(BaseRuleParser.CQ_, i);
		}
		public List<TerminalNode> NEQ_() { return getTokens(BaseRuleParser.NEQ_); }
		public TerminalNode NEQ_(int i) {
			return getToken(BaseRuleParser.NEQ_, i);
		}
		public List<TerminalNode> GT_() { return getTokens(BaseRuleParser.GT_); }
		public TerminalNode GT_(int i) {
			return getToken(BaseRuleParser.GT_, i);
		}
		public List<TerminalNode> GTE_() { return getTokens(BaseRuleParser.GTE_); }
		public TerminalNode GTE_(int i) {
			return getToken(BaseRuleParser.GTE_, i);
		}
		public List<TerminalNode> LT_() { return getTokens(BaseRuleParser.LT_); }
		public TerminalNode LT_(int i) {
			return getToken(BaseRuleParser.LT_, i);
		}
		public List<TerminalNode> LTE_() { return getTokens(BaseRuleParser.LTE_); }
		public TerminalNode LTE_(int i) {
			return getToken(BaseRuleParser.LTE_, i);
		}
		public List<TerminalNode> POUND_() { return getTokens(BaseRuleParser.POUND_); }
		public TerminalNode POUND_(int i) {
			return getToken(BaseRuleParser.POUND_, i);
		}
		public List<TerminalNode> LP_() { return getTokens(BaseRuleParser.LP_); }
		public TerminalNode LP_(int i) {
			return getToken(BaseRuleParser.LP_, i);
		}
		public List<TerminalNode> RP_() { return getTokens(BaseRuleParser.RP_); }
		public TerminalNode RP_(int i) {
			return getToken(BaseRuleParser.RP_, i);
		}
		public List<TerminalNode> LBE_() { return getTokens(BaseRuleParser.LBE_); }
		public TerminalNode LBE_(int i) {
			return getToken(BaseRuleParser.LBE_, i);
		}
		public List<TerminalNode> RBE_() { return getTokens(BaseRuleParser.RBE_); }
		public TerminalNode RBE_(int i) {
			return getToken(BaseRuleParser.RBE_, i);
		}
		public List<TerminalNode> LBT_() { return getTokens(BaseRuleParser.LBT_); }
		public TerminalNode LBT_(int i) {
			return getToken(BaseRuleParser.LBT_, i);
		}
		public List<TerminalNode> RBT_() { return getTokens(BaseRuleParser.RBT_); }
		public TerminalNode RBT_(int i) {
			return getToken(BaseRuleParser.RBT_, i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public List<TerminalNode> DQ_() { return getTokens(BaseRuleParser.DQ_); }
		public TerminalNode DQ_(int i) {
			return getToken(BaseRuleParser.DQ_, i);
		}
		public List<TerminalNode> SQ_() { return getTokens(BaseRuleParser.SQ_); }
		public TerminalNode SQ_(int i) {
			return getToken(BaseRuleParser.SQ_, i);
		}
		public List<TerminalNode> BQ_() { return getTokens(BaseRuleParser.BQ_); }
		public TerminalNode BQ_(int i) {
			return getToken(BaseRuleParser.BQ_, i);
		}
		public List<TerminalNode> QUESTION_() { return getTokens(BaseRuleParser.QUESTION_); }
		public TerminalNode QUESTION_(int i) {
			return getToken(BaseRuleParser.QUESTION_, i);
		}
		public List<TerminalNode> AT_() { return getTokens(BaseRuleParser.AT_); }
		public TerminalNode AT_(int i) {
			return getToken(BaseRuleParser.AT_, i);
		}
		public List<TerminalNode> SEMI_() { return getTokens(BaseRuleParser.SEMI_); }
		public TerminalNode SEMI_(int i) {
			return getToken(BaseRuleParser.SEMI_, i);
		}
		public List<TerminalNode> TILDE_TILDE_() { return getTokens(BaseRuleParser.TILDE_TILDE_); }
		public TerminalNode TILDE_TILDE_(int i) {
			return getToken(BaseRuleParser.TILDE_TILDE_, i);
		}
		public List<TerminalNode> NOT_TILDE_TILDE_() { return getTokens(BaseRuleParser.NOT_TILDE_TILDE_); }
		public TerminalNode NOT_TILDE_TILDE_(int i) {
			return getToken(BaseRuleParser.NOT_TILDE_TILDE_, i);
		}
		public List<TerminalNode> TYPE_CAST_() { return getTokens(BaseRuleParser.TYPE_CAST_); }
		public TerminalNode TYPE_CAST_(int i) {
			return getToken(BaseRuleParser.TYPE_CAST_, i);
		}
		public OpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpContext op() throws RecognitionException {
		OpContext _localctx = new OpContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_op);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(990); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(989);
					_la = _input.LA(1);
					if ( !(((((_la - 499)) & ~0x3f) == 0 && ((1L << (_la - 499)) & ((1L << (AND_ - 499)) | (1L << (OR_ - 499)) | (1L << (NOT_ - 499)) | (1L << (TILDE_ - 499)) | (1L << (VERTICAL_BAR_ - 499)) | (1L << (AMPERSAND_ - 499)) | (1L << (SIGNED_LEFT_SHIFT_ - 499)) | (1L << (SIGNED_RIGHT_SHIFT_ - 499)) | (1L << (CARET_ - 499)) | (1L << (MOD_ - 499)) | (1L << (COLON_ - 499)) | (1L << (PLUS_ - 499)) | (1L << (MINUS_ - 499)) | (1L << (ASTERISK_ - 499)) | (1L << (SLASH_ - 499)) | (1L << (BACKSLASH_ - 499)) | (1L << (DOT_ - 499)) | (1L << (DOT_ASTERISK_ - 499)) | (1L << (SAFE_EQ_ - 499)) | (1L << (DEQ_ - 499)) | (1L << (EQ_ - 499)) | (1L << (CQ_ - 499)) | (1L << (NEQ_ - 499)) | (1L << (GT_ - 499)) | (1L << (GTE_ - 499)) | (1L << (LT_ - 499)) | (1L << (LTE_ - 499)) | (1L << (POUND_ - 499)) | (1L << (LP_ - 499)) | (1L << (RP_ - 499)) | (1L << (LBE_ - 499)) | (1L << (RBE_ - 499)) | (1L << (LBT_ - 499)) | (1L << (RBT_ - 499)) | (1L << (COMMA_ - 499)) | (1L << (DQ_ - 499)) | (1L << (SQ_ - 499)) | (1L << (BQ_ - 499)) | (1L << (QUESTION_ - 499)) | (1L << (AT_ - 499)) | (1L << (SEMI_ - 499)) | (1L << (TILDE_TILDE_ - 499)) | (1L << (NOT_TILDE_TILDE_ - 499)) | (1L << (TYPE_CAST_ - 499)))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(992); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
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

	public static class MathOperatorContext extends ParserRuleContext {
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public TerminalNode SLASH_() { return getToken(BaseRuleParser.SLASH_, 0); }
		public TerminalNode MOD_() { return getToken(BaseRuleParser.MOD_, 0); }
		public TerminalNode CARET_() { return getToken(BaseRuleParser.CARET_, 0); }
		public TerminalNode LT_() { return getToken(BaseRuleParser.LT_, 0); }
		public TerminalNode GT_() { return getToken(BaseRuleParser.GT_, 0); }
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode LTE_() { return getToken(BaseRuleParser.LTE_, 0); }
		public TerminalNode GTE_() { return getToken(BaseRuleParser.GTE_, 0); }
		public TerminalNode NEQ_() { return getToken(BaseRuleParser.NEQ_, 0); }
		public MathOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterMathOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitMathOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitMathOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MathOperatorContext mathOperator() throws RecognitionException {
		MathOperatorContext _localctx = new MathOperatorContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_mathOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(994);
			_la = _input.LA(1);
			if ( !(((((_la - 507)) & ~0x3f) == 0 && ((1L << (_la - 507)) & ((1L << (CARET_ - 507)) | (1L << (MOD_ - 507)) | (1L << (PLUS_ - 507)) | (1L << (MINUS_ - 507)) | (1L << (ASTERISK_ - 507)) | (1L << (SLASH_ - 507)) | (1L << (EQ_ - 507)) | (1L << (NEQ_ - 507)) | (1L << (GT_ - 507)) | (1L << (GTE_ - 507)) | (1L << (LT_ - 507)) | (1L << (LTE_ - 507)))) != 0)) ) {
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

	public static class JsonOperatorContext extends ParserRuleContext {
		public JsonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonOperator; }
	 
		public JsonOperatorContext() { }
		public void copyFrom(JsonOperatorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class JsonPathExtractContext extends JsonOperatorContext {
		public TerminalNode JSON_PATH_EXTRACT_() { return getToken(BaseRuleParser.JSON_PATH_EXTRACT_, 0); }
		public JsonPathExtractContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonPathExtract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonPathExtract(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonPathExtract(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbPathDeleteContext extends JsonOperatorContext {
		public TerminalNode JSONB_PATH_DELETE_() { return getToken(BaseRuleParser.JSONB_PATH_DELETE_, 0); }
		public JsonbPathDeleteContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbPathDelete(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbPathDelete(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbPathDelete(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbContainTopKeyContext extends JsonOperatorContext {
		public TerminalNode QUESTION_() { return getToken(BaseRuleParser.QUESTION_, 0); }
		public JsonbContainTopKeyContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbContainTopKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbContainTopKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbContainTopKey(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbConcatContext extends JsonOperatorContext {
		public TerminalNode OR_() { return getToken(BaseRuleParser.OR_, 0); }
		public JsonbConcatContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbConcat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbConcat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbConcat(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbContainLeftContext extends JsonOperatorContext {
		public TerminalNode JSONB_CONTAIN_LEFT_() { return getToken(BaseRuleParser.JSONB_CONTAIN_LEFT_, 0); }
		public JsonbContainLeftContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbContainLeft(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbContainLeft(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbContainLeft(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbDeleteContext extends JsonOperatorContext {
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public JsonbDeleteContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbDelete(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbDelete(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbDelete(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonExtractContext extends JsonOperatorContext {
		public TerminalNode JSON_EXTRACT_() { return getToken(BaseRuleParser.JSON_EXTRACT_, 0); }
		public JsonExtractContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonExtract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonExtract(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonExtract(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbContainRightContext extends JsonOperatorContext {
		public TerminalNode JSONB_CONTAIN_RIGHT_() { return getToken(BaseRuleParser.JSONB_CONTAIN_RIGHT_, 0); }
		public JsonbContainRightContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbContainRight(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbContainRight(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbContainRight(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonExtractTextContext extends JsonOperatorContext {
		public TerminalNode JSON_EXTRACT_TEXT_() { return getToken(BaseRuleParser.JSON_EXTRACT_TEXT_, 0); }
		public JsonExtractTextContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonExtractText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonExtractText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonExtractText(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbPathPredicateCheckContext extends JsonOperatorContext {
		public TerminalNode JSONB_PATH_PREDICATE_CHECK_() { return getToken(BaseRuleParser.JSONB_PATH_PREDICATE_CHECK_, 0); }
		public JsonbPathPredicateCheckContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbPathPredicateCheck(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbPathPredicateCheck(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbPathPredicateCheck(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbPathContainAnyValueContext extends JsonOperatorContext {
		public TerminalNode JSONB_PATH_CONTAIN_ANY_VALUE_() { return getToken(BaseRuleParser.JSONB_PATH_CONTAIN_ANY_VALUE_, 0); }
		public JsonbPathContainAnyValueContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbPathContainAnyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbPathContainAnyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbPathContainAnyValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbContainAnyTopKeyContext extends JsonOperatorContext {
		public TerminalNode QUESTION_() { return getToken(BaseRuleParser.QUESTION_, 0); }
		public TerminalNode VERTICAL_BAR_() { return getToken(BaseRuleParser.VERTICAL_BAR_, 0); }
		public JsonbContainAnyTopKeyContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbContainAnyTopKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbContainAnyTopKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbContainAnyTopKey(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonPathExtractTextContext extends JsonOperatorContext {
		public TerminalNode JSON_PATH_EXTRACT_TEXT_() { return getToken(BaseRuleParser.JSON_PATH_EXTRACT_TEXT_, 0); }
		public JsonPathExtractTextContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonPathExtractText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonPathExtractText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonPathExtractText(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class JsonbContainAllTopKeyContext extends JsonOperatorContext {
		public TerminalNode JSONB_CONTAIN_ALL_TOP_KEY_() { return getToken(BaseRuleParser.JSONB_CONTAIN_ALL_TOP_KEY_, 0); }
		public JsonbContainAllTopKeyContext(JsonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterJsonbContainAllTopKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitJsonbContainAllTopKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitJsonbContainAllTopKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JsonOperatorContext jsonOperator() throws RecognitionException {
		JsonOperatorContext _localctx = new JsonOperatorContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_jsonOperator);
		try {
			setState(1011);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				_localctx = new JsonExtractContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(996);
				match(JSON_EXTRACT_);
				}
				break;
			case 2:
				_localctx = new JsonExtractTextContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(997);
				match(JSON_EXTRACT_TEXT_);
				}
				break;
			case 3:
				_localctx = new JsonPathExtractContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(998);
				match(JSON_PATH_EXTRACT_);
				}
				break;
			case 4:
				_localctx = new JsonPathExtractTextContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(999);
				match(JSON_PATH_EXTRACT_TEXT_);
				}
				break;
			case 5:
				_localctx = new JsonbContainRightContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1000);
				match(JSONB_CONTAIN_RIGHT_);
				}
				break;
			case 6:
				_localctx = new JsonbContainLeftContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1001);
				match(JSONB_CONTAIN_LEFT_);
				}
				break;
			case 7:
				_localctx = new JsonbContainTopKeyContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1002);
				match(QUESTION_);
				}
				break;
			case 8:
				_localctx = new JsonbContainAnyTopKeyContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1003);
				match(QUESTION_);
				setState(1004);
				match(VERTICAL_BAR_);
				}
				break;
			case 9:
				_localctx = new JsonbContainAllTopKeyContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1005);
				match(JSONB_CONTAIN_ALL_TOP_KEY_);
				}
				break;
			case 10:
				_localctx = new JsonbConcatContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1006);
				match(OR_);
				}
				break;
			case 11:
				_localctx = new JsonbDeleteContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1007);
				match(MINUS_);
				}
				break;
			case 12:
				_localctx = new JsonbPathDeleteContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1008);
				match(JSONB_PATH_DELETE_);
				}
				break;
			case 13:
				_localctx = new JsonbPathContainAnyValueContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1009);
				match(JSONB_PATH_CONTAIN_ANY_VALUE_);
				}
				break;
			case 14:
				_localctx = new JsonbPathPredicateCheckContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1010);
				match(JSONB_PATH_PREDICATE_CHECK_);
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

	public static class QualAllOpContext extends ParserRuleContext {
		public AllOpContext allOp() {
			return getRuleContext(AllOpContext.class,0);
		}
		public TerminalNode OPERATOR() { return getToken(BaseRuleParser.OPERATOR, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AnyOperatorContext anyOperator() {
			return getRuleContext(AnyOperatorContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public QualAllOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualAllOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterQualAllOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitQualAllOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitQualAllOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualAllOpContext qualAllOp() throws RecognitionException {
		QualAllOpContext _localctx = new QualAllOpContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_qualAllOp);
		try {
			setState(1019);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AND_:
			case OR_:
			case NOT_:
			case TILDE_:
			case VERTICAL_BAR_:
			case AMPERSAND_:
			case SIGNED_LEFT_SHIFT_:
			case SIGNED_RIGHT_SHIFT_:
			case CARET_:
			case MOD_:
			case COLON_:
			case PLUS_:
			case MINUS_:
			case ASTERISK_:
			case SLASH_:
			case BACKSLASH_:
			case DOT_:
			case DOT_ASTERISK_:
			case SAFE_EQ_:
			case DEQ_:
			case EQ_:
			case CQ_:
			case NEQ_:
			case GT_:
			case GTE_:
			case LT_:
			case LTE_:
			case POUND_:
			case LP_:
			case RP_:
			case LBE_:
			case RBE_:
			case LBT_:
			case RBT_:
			case COMMA_:
			case DQ_:
			case SQ_:
			case BQ_:
			case QUESTION_:
			case AT_:
			case SEMI_:
			case TILDE_TILDE_:
			case NOT_TILDE_TILDE_:
			case TYPE_CAST_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1013);
				allOp();
				}
				break;
			case OPERATOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(1014);
				match(OPERATOR);
				setState(1015);
				match(LP_);
				setState(1016);
				anyOperator();
				setState(1017);
				match(RP_);
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

	public static class AscDescContext extends ParserRuleContext {
		public TerminalNode ASC() { return getToken(BaseRuleParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(BaseRuleParser.DESC, 0); }
		public AscDescContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ascDesc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAscDesc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAscDesc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAscDesc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AscDescContext ascDesc() throws RecognitionException {
		AscDescContext _localctx = new AscDescContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_ascDesc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1021);
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
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyOperatorContext extends ParserRuleContext {
		public AllOpContext allOp() {
			return getRuleContext(AllOpContext.class,0);
		}
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public AnyOperatorContext anyOperator() {
			return getRuleContext(AnyOperatorContext.class,0);
		}
		public AnyOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAnyOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAnyOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAnyOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyOperatorContext anyOperator() throws RecognitionException {
		AnyOperatorContext _localctx = new AnyOperatorContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_anyOperator);
		try {
			setState(1028);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AND_:
			case OR_:
			case NOT_:
			case TILDE_:
			case VERTICAL_BAR_:
			case AMPERSAND_:
			case SIGNED_LEFT_SHIFT_:
			case SIGNED_RIGHT_SHIFT_:
			case CARET_:
			case MOD_:
			case COLON_:
			case PLUS_:
			case MINUS_:
			case ASTERISK_:
			case SLASH_:
			case BACKSLASH_:
			case DOT_:
			case DOT_ASTERISK_:
			case SAFE_EQ_:
			case DEQ_:
			case EQ_:
			case CQ_:
			case NEQ_:
			case GT_:
			case GTE_:
			case LT_:
			case LTE_:
			case POUND_:
			case LP_:
			case RP_:
			case LBE_:
			case RBE_:
			case LBT_:
			case RBT_:
			case COMMA_:
			case DQ_:
			case SQ_:
			case BQ_:
			case QUESTION_:
			case AT_:
			case SEMI_:
			case TILDE_TILDE_:
			case NOT_TILDE_TILDE_:
			case TYPE_CAST_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1023);
				allOp();
				}
				break;
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case IF:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case VARYING:
			case VALUE:
			case TIES:
			case CUBE:
			case SETS:
			case OTHERS:
			case AT:
			case ADMIN:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case ORDINALITY:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(1024);
				colId();
				setState(1025);
				match(DOT_);
				setState(1026);
				anyOperator();
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

	public static class FrameClauseContext extends ParserRuleContext {
		public FrameExtentContext frameExtent() {
			return getRuleContext(FrameExtentContext.class,0);
		}
		public TerminalNode RANGE() { return getToken(BaseRuleParser.RANGE, 0); }
		public TerminalNode ROWS() { return getToken(BaseRuleParser.ROWS, 0); }
		public TerminalNode GROUPS() { return getToken(BaseRuleParser.GROUPS, 0); }
		public WindowExclusionClauseContext windowExclusionClause() {
			return getRuleContext(WindowExclusionClauseContext.class,0);
		}
		public FrameClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFrameClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFrameClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFrameClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameClauseContext frameClause() throws RecognitionException {
		FrameClauseContext _localctx = new FrameClauseContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_frameClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1030);
			_la = _input.LA(1);
			if ( !(_la==ROWS || _la==RANGE || _la==GROUPS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1031);
			frameExtent();
			setState(1033);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXCLUDE) {
				{
				setState(1032);
				windowExclusionClause();
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

	public static class FrameExtentContext extends ParserRuleContext {
		public List<FrameBoundContext> frameBound() {
			return getRuleContexts(FrameBoundContext.class);
		}
		public FrameBoundContext frameBound(int i) {
			return getRuleContext(FrameBoundContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(BaseRuleParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(BaseRuleParser.AND, 0); }
		public FrameExtentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameExtent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFrameExtent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFrameExtent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFrameExtent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameExtentContext frameExtent() throws RecognitionException {
		FrameExtentContext _localctx = new FrameExtentContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_frameExtent);
		try {
			setState(1041);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case UNIQUE:
			case KEY:
			case POSITION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case CASE:
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
			case IF:
			case IS:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case ARRAY:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case DEFAULT:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case CURRENT_DATE:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case NULLIF:
			case VARYING:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case USER:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SESSION_USER:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_ROLE:
			case CURRENT_CATALOG:
			case CURRENT_SCHEMA:
			case NORMALIZE:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case PARAM:
			case OR_:
			case PLUS_:
			case MINUS_:
			case LP_:
			case QUESTION_:
			case JSON_EXTRACT_:
			case JSON_EXTRACT_TEXT_:
			case JSON_PATH_EXTRACT_:
			case JSON_PATH_EXTRACT_TEXT_:
			case JSONB_CONTAIN_RIGHT_:
			case JSONB_CONTAIN_LEFT_:
			case JSONB_CONTAIN_ALL_TOP_KEY_:
			case JSONB_PATH_DELETE_:
			case JSONB_PATH_CONTAIN_ANY_VALUE_:
			case JSONB_PATH_PREDICATE_CHECK_:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1035);
				frameBound();
				}
				break;
			case BETWEEN:
				enterOuterAlt(_localctx, 2);
				{
				setState(1036);
				match(BETWEEN);
				setState(1037);
				frameBound();
				setState(1038);
				match(AND);
				setState(1039);
				frameBound();
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

	public static class FrameBoundContext extends ParserRuleContext {
		public TerminalNode UNBOUNDED() { return getToken(BaseRuleParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(BaseRuleParser.PRECEDING, 0); }
		public TerminalNode FOLLOWING() { return getToken(BaseRuleParser.FOLLOWING, 0); }
		public TerminalNode CURRENT() { return getToken(BaseRuleParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public FrameBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFrameBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFrameBound(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFrameBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameBoundContext frameBound() throws RecognitionException {
		FrameBoundContext _localctx = new FrameBoundContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_frameBound);
		try {
			setState(1055);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1043);
				match(UNBOUNDED);
				setState(1044);
				match(PRECEDING);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1045);
				match(UNBOUNDED);
				setState(1046);
				match(FOLLOWING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1047);
				match(CURRENT);
				setState(1048);
				match(ROW);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1049);
				aExpr(0);
				setState(1050);
				match(PRECEDING);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1052);
				aExpr(0);
				setState(1053);
				match(FOLLOWING);
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

	public static class WindowExclusionClauseContext extends ParserRuleContext {
		public TerminalNode EXCLUDE() { return getToken(BaseRuleParser.EXCLUDE, 0); }
		public TerminalNode CURRENT() { return getToken(BaseRuleParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public TerminalNode GROUP() { return getToken(BaseRuleParser.GROUP, 0); }
		public TerminalNode TIES() { return getToken(BaseRuleParser.TIES, 0); }
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode OTHERS() { return getToken(BaseRuleParser.OTHERS, 0); }
		public WindowExclusionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowExclusionClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowExclusionClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowExclusionClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowExclusionClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowExclusionClauseContext windowExclusionClause() throws RecognitionException {
		WindowExclusionClauseContext _localctx = new WindowExclusionClauseContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_windowExclusionClause);
		try {
			setState(1067);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1057);
				match(EXCLUDE);
				setState(1058);
				match(CURRENT);
				setState(1059);
				match(ROW);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1060);
				match(EXCLUDE);
				setState(1061);
				match(GROUP);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1062);
				match(EXCLUDE);
				setState(1063);
				match(TIES);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1064);
				match(EXCLUDE);
				setState(1065);
				match(NO);
				setState(1066);
				match(OTHERS);
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

	public static class RowContext extends ParserRuleContext {
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public RowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_row; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowContext row() throws RecognitionException {
		RowContext _localctx = new RowContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_row);
		try {
			setState(1083);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1069);
				match(ROW);
				setState(1070);
				match(LP_);
				setState(1071);
				exprList(0);
				setState(1072);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1074);
				match(ROW);
				setState(1075);
				match(LP_);
				setState(1076);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1077);
				match(LP_);
				setState(1078);
				exprList(0);
				setState(1079);
				match(COMMA_);
				setState(1080);
				aExpr(0);
				setState(1081);
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

	public static class ExplicitRowContext extends ParserRuleContext {
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ExplicitRowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitRow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterExplicitRow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitExplicitRow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitExplicitRow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitRowContext explicitRow() throws RecognitionException {
		ExplicitRowContext _localctx = new ExplicitRowContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_explicitRow);
		try {
			setState(1093);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1085);
				match(ROW);
				setState(1086);
				match(LP_);
				setState(1087);
				exprList(0);
				setState(1088);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1090);
				match(ROW);
				setState(1091);
				match(LP_);
				setState(1092);
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

	public static class ImplicitRowContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ImplicitRowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implicitRow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterImplicitRow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitImplicitRow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitImplicitRow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplicitRowContext implicitRow() throws RecognitionException {
		ImplicitRowContext _localctx = new ImplicitRowContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_implicitRow);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1095);
			match(LP_);
			setState(1096);
			exprList(0);
			setState(1097);
			match(COMMA_);
			setState(1098);
			aExpr(0);
			setState(1099);
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

	public static class SubTypeContext extends ParserRuleContext {
		public TerminalNode ANY() { return getToken(BaseRuleParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(BaseRuleParser.SOME, 0); }
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public SubTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubTypeContext subType() throws RecognitionException {
		SubTypeContext _localctx = new SubTypeContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_subType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1101);
			_la = _input.LA(1);
			if ( !(_la==ALL || _la==ANY || _la==SOME) ) {
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

	public static class ArrayExprContext extends ParserRuleContext {
		public TerminalNode LBT_() { return getToken(BaseRuleParser.LBT_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RBT_() { return getToken(BaseRuleParser.RBT_, 0); }
		public ArrayExprListContext arrayExprList() {
			return getRuleContext(ArrayExprListContext.class,0);
		}
		public ArrayExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterArrayExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitArrayExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitArrayExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayExprContext arrayExpr() throws RecognitionException {
		ArrayExprContext _localctx = new ArrayExprContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_arrayExpr);
		try {
			setState(1113);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1103);
				match(LBT_);
				setState(1104);
				exprList(0);
				setState(1105);
				match(RBT_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1107);
				match(LBT_);
				setState(1108);
				arrayExprList();
				setState(1109);
				match(RBT_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1111);
				match(LBT_);
				setState(1112);
				match(RBT_);
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

	public static class ArrayExprListContext extends ParserRuleContext {
		public List<ArrayExprContext> arrayExpr() {
			return getRuleContexts(ArrayExprContext.class);
		}
		public ArrayExprContext arrayExpr(int i) {
			return getRuleContext(ArrayExprContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public ArrayExprListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayExprList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterArrayExprList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitArrayExprList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitArrayExprList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayExprListContext arrayExprList() throws RecognitionException {
		ArrayExprListContext _localctx = new ArrayExprListContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_arrayExprList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1115);
			arrayExpr();
			setState(1120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1116);
				match(COMMA_);
				setState(1117);
				arrayExpr();
				}
				}
				setState(1122);
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

	public static class FuncArgListContext extends ParserRuleContext {
		public List<FuncArgExprContext> funcArgExpr() {
			return getRuleContexts(FuncArgExprContext.class);
		}
		public FuncArgExprContext funcArgExpr(int i) {
			return getRuleContext(FuncArgExprContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public FuncArgListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcArgList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncArgList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncArgList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncArgList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncArgListContext funcArgList() throws RecognitionException {
		FuncArgListContext _localctx = new FuncArgListContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_funcArgList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1123);
			funcArgExpr();
			setState(1128);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1124);
					match(COMMA_);
					setState(1125);
					funcArgExpr();
					}
					} 
				}
				setState(1130);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
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

	public static class ParamNameContext extends ParserRuleContext {
		public TypeFunctionNameContext typeFunctionName() {
			return getRuleContext(TypeFunctionNameContext.class,0);
		}
		public ParamNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterParamName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitParamName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitParamName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParamNameContext paramName() throws RecognitionException {
		ParamNameContext _localctx = new ParamNameContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_paramName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1131);
			typeFunctionName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FuncArgExprContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public ParamNameContext paramName() {
			return getRuleContext(ParamNameContext.class,0);
		}
		public TerminalNode CQ_() { return getToken(BaseRuleParser.CQ_, 0); }
		public TerminalNode GTE_() { return getToken(BaseRuleParser.GTE_, 0); }
		public FuncArgExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcArgExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncArgExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncArgExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncArgExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncArgExprContext funcArgExpr() throws RecognitionException {
		FuncArgExprContext _localctx = new FuncArgExprContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_funcArgExpr);
		try {
			setState(1142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1133);
				aExpr(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1134);
				paramName();
				setState(1135);
				match(CQ_);
				setState(1136);
				aExpr(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1138);
				paramName();
				setState(1139);
				match(GTE_);
				setState(1140);
				aExpr(0);
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

	public static class TypeListContext extends ParserRuleContext {
		public List<TypeNameContext> typeName() {
			return getRuleContexts(TypeNameContext.class);
		}
		public TypeNameContext typeName(int i) {
			return getRuleContext(TypeNameContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1144);
			typeName();
			setState(1149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1145);
				match(COMMA_);
				setState(1146);
				typeName();
				}
				}
				setState(1151);
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

	public static class FuncApplicationContext extends ParserRuleContext {
		public FuncNameContext funcName() {
			return getRuleContext(FuncNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public FuncArgListContext funcArgList() {
			return getRuleContext(FuncArgListContext.class,0);
		}
		public SortClauseContext sortClause() {
			return getRuleContext(SortClauseContext.class,0);
		}
		public TerminalNode VARIADIC() { return getToken(BaseRuleParser.VARIADIC, 0); }
		public FuncArgExprContext funcArgExpr() {
			return getRuleContext(FuncArgExprContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public FuncApplicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcApplication; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncApplication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncApplication(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncApplication(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncApplicationContext funcApplication() throws RecognitionException {
		FuncApplicationContext _localctx = new FuncApplicationContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_funcApplication);
		int _la;
		try {
			setState(1207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1152);
				funcName();
				setState(1153);
				match(LP_);
				setState(1154);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1156);
				funcName();
				setState(1157);
				match(LP_);
				setState(1158);
				funcArgList();
				setState(1160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(1159);
					sortClause();
					}
				}

				setState(1162);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1164);
				funcName();
				setState(1165);
				match(LP_);
				setState(1166);
				match(VARIADIC);
				setState(1167);
				funcArgExpr();
				setState(1169);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(1168);
					sortClause();
					}
				}

				setState(1171);
				match(RP_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1173);
				funcName();
				setState(1174);
				match(LP_);
				setState(1175);
				funcArgList();
				setState(1176);
				match(COMMA_);
				setState(1177);
				match(VARIADIC);
				setState(1178);
				funcArgExpr();
				setState(1180);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(1179);
					sortClause();
					}
				}

				setState(1182);
				match(RP_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1184);
				funcName();
				setState(1185);
				match(LP_);
				setState(1186);
				match(ALL);
				setState(1187);
				funcArgList();
				setState(1189);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(1188);
					sortClause();
					}
				}

				setState(1191);
				match(RP_);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1193);
				funcName();
				setState(1194);
				match(LP_);
				setState(1195);
				match(DISTINCT);
				setState(1196);
				funcArgList();
				setState(1198);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(1197);
					sortClause();
					}
				}

				setState(1200);
				match(RP_);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1202);
				funcName();
				setState(1203);
				match(LP_);
				setState(1204);
				match(ASTERISK_);
				setState(1205);
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

	public static class FuncNameContext extends ParserRuleContext {
		public TypeFunctionNameContext typeFunctionName() {
			return getRuleContext(TypeFunctionNameContext.class,0);
		}
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public IndirectionContext indirection() {
			return getRuleContext(IndirectionContext.class,0);
		}
		public FuncNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncNameContext funcName() throws RecognitionException {
		FuncNameContext _localctx = new FuncNameContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_funcName);
		try {
			setState(1213);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1209);
				typeFunctionName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1210);
				colId();
				setState(1211);
				indirection(0);
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

	public static class AexprConstContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public FuncNameContext funcName() {
			return getRuleContext(FuncNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public FuncArgListContext funcArgList() {
			return getRuleContext(FuncArgListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public SortClauseContext sortClause() {
			return getRuleContext(SortClauseContext.class,0);
		}
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public AexprConstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aexprConst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAexprConst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAexprConst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAexprConst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AexprConstContext aexprConst() throws RecognitionException {
		AexprConstContext _localctx = new AexprConstContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_aexprConst);
		int _la;
		try {
			setState(1232);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1215);
				match(NUMBER_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1216);
				match(STRING_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1217);
				funcName();
				setState(1218);
				match(STRING_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1220);
				funcName();
				setState(1221);
				match(LP_);
				setState(1222);
				funcArgList();
				setState(1224);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(1223);
					sortClause();
					}
				}

				setState(1226);
				match(RP_);
				setState(1227);
				match(STRING_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1229);
				match(TRUE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1230);
				match(FALSE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1231);
				match(NULL);
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

	public static class QualifiedNameContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public IndirectionContext indirection() {
			return getRuleContext(IndirectionContext.class,0);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_qualifiedName);
		try {
			setState(1238);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1234);
				colId();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1235);
				colId();
				setState(1236);
				indirection(0);
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

	public static class ColIdContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ColIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColIdContext colId() throws RecognitionException {
		ColIdContext _localctx = new ColIdContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_colId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1240);
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

	public static class TypeFunctionNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public UnreservedWordContext unreservedWord() {
			return getRuleContext(UnreservedWordContext.class,0);
		}
		public TypeFuncNameKeywordContext typeFuncNameKeyword() {
			return getRuleContext(TypeFuncNameKeywordContext.class,0);
		}
		public TypeFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeFunctionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTypeFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTypeFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTypeFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeFunctionNameContext typeFunctionName() throws RecognitionException {
		TypeFunctionNameContext _localctx = new TypeFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_typeFunctionName);
		try {
			setState(1245);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1242);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1243);
				unreservedWord();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1244);
				typeFuncNameKeyword();
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

	public static class FunctionTableContext extends ParserRuleContext {
		public FunctionExprWindowlessContext functionExprWindowless() {
			return getRuleContext(FunctionExprWindowlessContext.class,0);
		}
		public OrdinalityContext ordinality() {
			return getRuleContext(OrdinalityContext.class,0);
		}
		public TerminalNode ROWS() { return getToken(BaseRuleParser.ROWS, 0); }
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public RowsFromListContext rowsFromList() {
			return getRuleContext(RowsFromListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public FunctionTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFunctionTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFunctionTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFunctionTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTableContext functionTable() throws RecognitionException {
		FunctionTableContext _localctx = new FunctionTableContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_functionTable);
		int _la;
		try {
			setState(1259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1247);
				functionExprWindowless();
				setState(1249);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(1248);
					ordinality();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1251);
				match(ROWS);
				setState(1252);
				match(FROM);
				setState(1253);
				match(LP_);
				setState(1254);
				rowsFromList();
				setState(1255);
				match(RP_);
				setState(1257);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(1256);
					ordinality();
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

	public static class XmlTableContext extends ParserRuleContext {
		public TerminalNode XMLTABLE() { return getToken(BaseRuleParser.XMLTABLE, 0); }
		public List<TerminalNode> LP_() { return getTokens(BaseRuleParser.LP_); }
		public TerminalNode LP_(int i) {
			return getToken(BaseRuleParser.LP_, i);
		}
		public CExprContext cExpr() {
			return getRuleContext(CExprContext.class,0);
		}
		public XmlExistsArgumentContext xmlExistsArgument() {
			return getRuleContext(XmlExistsArgumentContext.class,0);
		}
		public TerminalNode COLUMNS() { return getToken(BaseRuleParser.COLUMNS, 0); }
		public XmlTableColumnListContext xmlTableColumnList() {
			return getRuleContext(XmlTableColumnListContext.class,0);
		}
		public List<TerminalNode> RP_() { return getTokens(BaseRuleParser.RP_); }
		public TerminalNode RP_(int i) {
			return getToken(BaseRuleParser.RP_, i);
		}
		public TerminalNode XMLNAMESPACES() { return getToken(BaseRuleParser.XMLNAMESPACES, 0); }
		public XmlNamespaceListContext xmlNamespaceList() {
			return getRuleContext(XmlNamespaceListContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public XmlTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlTableContext xmlTable() throws RecognitionException {
		XmlTableContext _localctx = new XmlTableContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_xmlTable);
		try {
			setState(1282);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1261);
				match(XMLTABLE);
				setState(1262);
				match(LP_);
				setState(1263);
				cExpr();
				setState(1264);
				xmlExistsArgument();
				setState(1265);
				match(COLUMNS);
				setState(1266);
				xmlTableColumnList();
				setState(1267);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1269);
				match(XMLTABLE);
				setState(1270);
				match(LP_);
				setState(1271);
				match(XMLNAMESPACES);
				setState(1272);
				match(LP_);
				setState(1273);
				xmlNamespaceList();
				setState(1274);
				match(RP_);
				setState(1275);
				match(COMMA_);
				setState(1276);
				cExpr();
				setState(1277);
				xmlExistsArgument();
				setState(1278);
				match(COLUMNS);
				setState(1279);
				xmlTableColumnList();
				setState(1280);
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

	public static class XmlTableColumnListContext extends ParserRuleContext {
		public List<XmlTableColumnElContext> xmlTableColumnEl() {
			return getRuleContexts(XmlTableColumnElContext.class);
		}
		public XmlTableColumnElContext xmlTableColumnEl(int i) {
			return getRuleContext(XmlTableColumnElContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public XmlTableColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlTableColumnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlTableColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlTableColumnList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlTableColumnList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlTableColumnListContext xmlTableColumnList() throws RecognitionException {
		XmlTableColumnListContext _localctx = new XmlTableColumnListContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_xmlTableColumnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1284);
			xmlTableColumnEl();
			setState(1289);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1285);
				match(COMMA_);
				setState(1286);
				xmlTableColumnEl();
				}
				}
				setState(1291);
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

	public static class XmlTableColumnElContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public XmlTableColumnOptionListContext xmlTableColumnOptionList() {
			return getRuleContext(XmlTableColumnOptionListContext.class,0);
		}
		public TerminalNode FOR() { return getToken(BaseRuleParser.FOR, 0); }
		public TerminalNode ORDINALITY() { return getToken(BaseRuleParser.ORDINALITY, 0); }
		public XmlTableColumnElContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlTableColumnEl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlTableColumnEl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlTableColumnEl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlTableColumnEl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlTableColumnElContext xmlTableColumnEl() throws RecognitionException {
		XmlTableColumnElContext _localctx = new XmlTableColumnElContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_xmlTableColumnEl);
		try {
			setState(1303);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1292);
				colId();
				setState(1293);
				typeName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1295);
				colId();
				setState(1296);
				typeName();
				setState(1297);
				xmlTableColumnOptionList(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1299);
				colId();
				setState(1300);
				match(FOR);
				setState(1301);
				match(ORDINALITY);
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

	public static class XmlTableColumnOptionListContext extends ParserRuleContext {
		public XmlTableColumnOptionElContext xmlTableColumnOptionEl() {
			return getRuleContext(XmlTableColumnOptionElContext.class,0);
		}
		public XmlTableColumnOptionListContext xmlTableColumnOptionList() {
			return getRuleContext(XmlTableColumnOptionListContext.class,0);
		}
		public XmlTableColumnOptionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlTableColumnOptionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlTableColumnOptionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlTableColumnOptionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlTableColumnOptionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlTableColumnOptionListContext xmlTableColumnOptionList() throws RecognitionException {
		return xmlTableColumnOptionList(0);
	}

	private XmlTableColumnOptionListContext xmlTableColumnOptionList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		XmlTableColumnOptionListContext _localctx = new XmlTableColumnOptionListContext(_ctx, _parentState);
		XmlTableColumnOptionListContext _prevctx = _localctx;
		int _startState = 142;
		enterRecursionRule(_localctx, 142, RULE_xmlTableColumnOptionList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1306);
			xmlTableColumnOptionEl();
			}
			_ctx.stop = _input.LT(-1);
			setState(1312);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new XmlTableColumnOptionListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_xmlTableColumnOptionList);
					setState(1308);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(1309);
					xmlTableColumnOptionEl();
					}
					} 
				}
				setState(1314);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
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

	public static class XmlTableColumnOptionElContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public BExprContext bExpr() {
			return getRuleContext(BExprContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public XmlTableColumnOptionElContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlTableColumnOptionEl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlTableColumnOptionEl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlTableColumnOptionEl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlTableColumnOptionEl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlTableColumnOptionElContext xmlTableColumnOptionEl() throws RecognitionException {
		XmlTableColumnOptionElContext _localctx = new XmlTableColumnOptionElContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_xmlTableColumnOptionEl);
		try {
			setState(1323);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case IF:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case VARYING:
			case VALUE:
			case TIES:
			case CUBE:
			case SETS:
			case OTHERS:
			case AT:
			case ADMIN:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case ORDINALITY:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1315);
				identifier();
				setState(1316);
				bExpr(0);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1318);
				match(DEFAULT);
				setState(1319);
				bExpr(0);
				}
				break;
			case NOT:
				enterOuterAlt(_localctx, 3);
				{
				setState(1320);
				match(NOT);
				setState(1321);
				match(NULL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 4);
				{
				setState(1322);
				match(NULL);
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

	public static class XmlNamespaceListContext extends ParserRuleContext {
		public List<XmlNamespaceElContext> xmlNamespaceEl() {
			return getRuleContexts(XmlNamespaceElContext.class);
		}
		public XmlNamespaceElContext xmlNamespaceEl(int i) {
			return getRuleContext(XmlNamespaceElContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public XmlNamespaceListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlNamespaceList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlNamespaceList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlNamespaceList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlNamespaceList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlNamespaceListContext xmlNamespaceList() throws RecognitionException {
		XmlNamespaceListContext _localctx = new XmlNamespaceListContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_xmlNamespaceList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1325);
			xmlNamespaceEl();
			setState(1330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1326);
				match(COMMA_);
				setState(1327);
				xmlNamespaceEl();
				}
				}
				setState(1332);
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

	public static class XmlNamespaceElContext extends ParserRuleContext {
		public BExprContext bExpr() {
			return getRuleContext(BExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public XmlNamespaceElContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlNamespaceEl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlNamespaceEl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlNamespaceEl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlNamespaceEl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlNamespaceElContext xmlNamespaceEl() throws RecognitionException {
		XmlNamespaceElContext _localctx = new XmlNamespaceElContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_xmlNamespaceEl);
		try {
			setState(1339);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case POSITION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case CASE:
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
			case IF:
			case IS:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case ARRAY:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case CURRENT_DATE:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case NULLIF:
			case VARYING:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case USER:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SESSION_USER:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_ROLE:
			case CURRENT_CATALOG:
			case CURRENT_SCHEMA:
			case NORMALIZE:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case PARAM:
			case OR_:
			case PLUS_:
			case MINUS_:
			case LP_:
			case QUESTION_:
			case JSON_EXTRACT_:
			case JSON_EXTRACT_TEXT_:
			case JSON_PATH_EXTRACT_:
			case JSON_PATH_EXTRACT_TEXT_:
			case JSONB_CONTAIN_RIGHT_:
			case JSONB_CONTAIN_LEFT_:
			case JSONB_CONTAIN_ALL_TOP_KEY_:
			case JSONB_PATH_DELETE_:
			case JSONB_PATH_CONTAIN_ANY_VALUE_:
			case JSONB_PATH_PREDICATE_CHECK_:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1333);
				bExpr(0);
				setState(1334);
				match(AS);
				setState(1335);
				identifier();
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1337);
				match(DEFAULT);
				setState(1338);
				bExpr(0);
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

	public static class FuncExprContext extends ParserRuleContext {
		public FuncApplicationContext funcApplication() {
			return getRuleContext(FuncApplicationContext.class,0);
		}
		public WithinGroupClauseContext withinGroupClause() {
			return getRuleContext(WithinGroupClauseContext.class,0);
		}
		public FilterClauseContext filterClause() {
			return getRuleContext(FilterClauseContext.class,0);
		}
		public OverClauseContext overClause() {
			return getRuleContext(OverClauseContext.class,0);
		}
		public FunctionExprCommonSubexprContext functionExprCommonSubexpr() {
			return getRuleContext(FunctionExprCommonSubexprContext.class,0);
		}
		public FuncExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncExprContext funcExpr() throws RecognitionException {
		FuncExprContext _localctx = new FuncExprContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_funcExpr);
		try {
			setState(1352);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1341);
				funcApplication();
				setState(1343);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
				case 1:
					{
					setState(1342);
					withinGroupClause();
					}
					break;
				}
				setState(1346);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
				case 1:
					{
					setState(1345);
					filterClause();
					}
					break;
				}
				setState(1349);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
				case 1:
					{
					setState(1348);
					overClause();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1351);
				functionExprCommonSubexpr();
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

	public static class WithinGroupClauseContext extends ParserRuleContext {
		public TerminalNode WITHIN() { return getToken(BaseRuleParser.WITHIN, 0); }
		public TerminalNode GROUP() { return getToken(BaseRuleParser.GROUP, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public SortClauseContext sortClause() {
			return getRuleContext(SortClauseContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public WithinGroupClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_withinGroupClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWithinGroupClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWithinGroupClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWithinGroupClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WithinGroupClauseContext withinGroupClause() throws RecognitionException {
		WithinGroupClauseContext _localctx = new WithinGroupClauseContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_withinGroupClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1354);
			match(WITHIN);
			setState(1355);
			match(GROUP);
			setState(1356);
			match(LP_);
			setState(1357);
			sortClause();
			setState(1358);
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

	public static class FilterClauseContext extends ParserRuleContext {
		public TerminalNode FILTER() { return getToken(BaseRuleParser.FILTER, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode WHERE() { return getToken(BaseRuleParser.WHERE, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public FilterClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFilterClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFilterClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFilterClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterClauseContext filterClause() throws RecognitionException {
		FilterClauseContext _localctx = new FilterClauseContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_filterClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1360);
			match(FILTER);
			setState(1361);
			match(LP_);
			setState(1362);
			match(WHERE);
			setState(1363);
			aExpr(0);
			setState(1364);
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

	public static class FunctionExprWindowlessContext extends ParserRuleContext {
		public FuncApplicationContext funcApplication() {
			return getRuleContext(FuncApplicationContext.class,0);
		}
		public FunctionExprCommonSubexprContext functionExprCommonSubexpr() {
			return getRuleContext(FunctionExprCommonSubexprContext.class,0);
		}
		public FunctionExprWindowlessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionExprWindowless; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFunctionExprWindowless(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFunctionExprWindowless(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFunctionExprWindowless(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionExprWindowlessContext functionExprWindowless() throws RecognitionException {
		FunctionExprWindowlessContext _localctx = new FunctionExprWindowlessContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_functionExprWindowless);
		try {
			setState(1368);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1366);
				funcApplication();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1367);
				functionExprCommonSubexpr();
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

	public static class OrdinalityContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public TerminalNode ORDINALITY() { return getToken(BaseRuleParser.ORDINALITY, 0); }
		public OrdinalityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ordinality; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOrdinality(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOrdinality(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOrdinality(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrdinalityContext ordinality() throws RecognitionException {
		OrdinalityContext _localctx = new OrdinalityContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_ordinality);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1370);
			match(WITH);
			setState(1371);
			match(ORDINALITY);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionExprCommonSubexprContext extends ParserRuleContext {
		public TerminalNode COLLATION() { return getToken(BaseRuleParser.COLLATION, 0); }
		public TerminalNode FOR() { return getToken(BaseRuleParser.FOR, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<AExprContext> aExpr() {
			return getRuleContexts(AExprContext.class);
		}
		public AExprContext aExpr(int i) {
			return getRuleContext(AExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(BaseRuleParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(BaseRuleParser.CURRENT_TIME, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(BaseRuleParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode LOCALTIME() { return getToken(BaseRuleParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(BaseRuleParser.LOCALTIMESTAMP, 0); }
		public TerminalNode CURRENT_ROLE() { return getToken(BaseRuleParser.CURRENT_ROLE, 0); }
		public TerminalNode CURRENT_USER() { return getToken(BaseRuleParser.CURRENT_USER, 0); }
		public TerminalNode SESSION_USER() { return getToken(BaseRuleParser.SESSION_USER, 0); }
		public TerminalNode USER() { return getToken(BaseRuleParser.USER, 0); }
		public TerminalNode CURRENT_CATALOG() { return getToken(BaseRuleParser.CURRENT_CATALOG, 0); }
		public TerminalNode CURRENT_SCHEMA() { return getToken(BaseRuleParser.CURRENT_SCHEMA, 0); }
		public TerminalNode CAST() { return getToken(BaseRuleParser.CAST, 0); }
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode EXTRACT() { return getToken(BaseRuleParser.EXTRACT, 0); }
		public ExtractListContext extractList() {
			return getRuleContext(ExtractListContext.class,0);
		}
		public TerminalNode NORMALIZE() { return getToken(BaseRuleParser.NORMALIZE, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public UnicodeNormalFormContext unicodeNormalForm() {
			return getRuleContext(UnicodeNormalFormContext.class,0);
		}
		public TerminalNode OVERLAY() { return getToken(BaseRuleParser.OVERLAY, 0); }
		public OverlayListContext overlayList() {
			return getRuleContext(OverlayListContext.class,0);
		}
		public TerminalNode POSITION() { return getToken(BaseRuleParser.POSITION, 0); }
		public PositionListContext positionList() {
			return getRuleContext(PositionListContext.class,0);
		}
		public TerminalNode SUBSTRING() { return getToken(BaseRuleParser.SUBSTRING, 0); }
		public SubstrListContext substrList() {
			return getRuleContext(SubstrListContext.class,0);
		}
		public TerminalNode TREAT() { return getToken(BaseRuleParser.TREAT, 0); }
		public TerminalNode TRIM() { return getToken(BaseRuleParser.TRIM, 0); }
		public TerminalNode BOTH() { return getToken(BaseRuleParser.BOTH, 0); }
		public TrimListContext trimList() {
			return getRuleContext(TrimListContext.class,0);
		}
		public TerminalNode LEADING() { return getToken(BaseRuleParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(BaseRuleParser.TRAILING, 0); }
		public TerminalNode NULLIF() { return getToken(BaseRuleParser.NULLIF, 0); }
		public TerminalNode COALESCE() { return getToken(BaseRuleParser.COALESCE, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode GREATEST() { return getToken(BaseRuleParser.GREATEST, 0); }
		public TerminalNode LEAST() { return getToken(BaseRuleParser.LEAST, 0); }
		public TerminalNode XMLCONCAT() { return getToken(BaseRuleParser.XMLCONCAT, 0); }
		public TerminalNode XMLELEMENT() { return getToken(BaseRuleParser.XMLELEMENT, 0); }
		public TerminalNode NAME() { return getToken(BaseRuleParser.NAME, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public XmlAttributesContext xmlAttributes() {
			return getRuleContext(XmlAttributesContext.class,0);
		}
		public TerminalNode XMLEXISTS() { return getToken(BaseRuleParser.XMLEXISTS, 0); }
		public CExprContext cExpr() {
			return getRuleContext(CExprContext.class,0);
		}
		public XmlExistsArgumentContext xmlExistsArgument() {
			return getRuleContext(XmlExistsArgumentContext.class,0);
		}
		public TerminalNode XMLFOREST() { return getToken(BaseRuleParser.XMLFOREST, 0); }
		public XmlAttributeListContext xmlAttributeList() {
			return getRuleContext(XmlAttributeListContext.class,0);
		}
		public TerminalNode XMLPARSE() { return getToken(BaseRuleParser.XMLPARSE, 0); }
		public DocumentOrContentContext documentOrContent() {
			return getRuleContext(DocumentOrContentContext.class,0);
		}
		public XmlWhitespaceOptionContext xmlWhitespaceOption() {
			return getRuleContext(XmlWhitespaceOptionContext.class,0);
		}
		public TerminalNode XMLPI() { return getToken(BaseRuleParser.XMLPI, 0); }
		public TerminalNode XMLROOT() { return getToken(BaseRuleParser.XMLROOT, 0); }
		public XmlRootVersionContext xmlRootVersion() {
			return getRuleContext(XmlRootVersionContext.class,0);
		}
		public XmlRootStandaloneContext xmlRootStandalone() {
			return getRuleContext(XmlRootStandaloneContext.class,0);
		}
		public TerminalNode XMLSERIALIZE() { return getToken(BaseRuleParser.XMLSERIALIZE, 0); }
		public SimpleTypeNameContext simpleTypeName() {
			return getRuleContext(SimpleTypeNameContext.class,0);
		}
		public FunctionExprCommonSubexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionExprCommonSubexpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFunctionExprCommonSubexpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFunctionExprCommonSubexpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFunctionExprCommonSubexpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionExprCommonSubexprContext functionExprCommonSubexpr() throws RecognitionException {
		FunctionExprCommonSubexprContext _localctx = new FunctionExprCommonSubexprContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_functionExprCommonSubexpr);
		int _la;
		try {
			setState(1585);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1373);
				match(COLLATION);
				setState(1374);
				match(FOR);
				setState(1375);
				match(LP_);
				setState(1376);
				aExpr(0);
				setState(1377);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1379);
				match(CURRENT_DATE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1380);
				match(CURRENT_TIME);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1381);
				match(CURRENT_TIME);
				setState(1382);
				match(LP_);
				setState(1383);
				match(NUMBER_);
				setState(1384);
				match(RP_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1385);
				match(CURRENT_TIMESTAMP);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1386);
				match(CURRENT_TIMESTAMP);
				setState(1387);
				match(LP_);
				setState(1388);
				match(NUMBER_);
				setState(1389);
				match(RP_);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1390);
				match(LOCALTIME);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1391);
				match(LOCALTIME);
				setState(1392);
				match(LP_);
				setState(1393);
				match(NUMBER_);
				setState(1394);
				match(RP_);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1395);
				match(LOCALTIMESTAMP);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1396);
				match(LOCALTIMESTAMP);
				setState(1397);
				match(LP_);
				setState(1398);
				match(NUMBER_);
				setState(1399);
				match(RP_);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1400);
				match(CURRENT_ROLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1401);
				match(CURRENT_USER);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1402);
				match(SESSION_USER);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1403);
				match(USER);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1404);
				match(CURRENT_CATALOG);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1405);
				match(CURRENT_SCHEMA);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1406);
				match(CAST);
				setState(1407);
				match(LP_);
				setState(1408);
				aExpr(0);
				setState(1409);
				match(AS);
				setState(1410);
				typeName();
				setState(1411);
				match(RP_);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1413);
				match(EXTRACT);
				setState(1414);
				match(LP_);
				setState(1416);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << INSERT) | (1L << UPDATE) | (1L << DELETE) | (1L << ALTER) | (1L << DROP) | (1L << TRUNCATE) | (1L << SCHEMA) | (1L << REVOKE) | (1L << ADD) | (1L << SET) | (1L << INDEX) | (1L << KEY) | (1L << FUNCTION) | (1L << TRIGGER) | (1L << PROCEDURE) | (1L << VIEW) | (1L << IF))) != 0) || ((((_la - 75)) & ~0x3f) == 0 && ((1L << (_la - 75)) & ((1L << (BY - 75)) | (1L << (BEGIN - 75)) | (1L << (COMMIT - 75)) | (1L << (ROLLBACK - 75)) | (1L << (SAVEPOINT - 75)) | (1L << (DOUBLE - 75)) | (1L << (YEAR - 75)) | (1L << (MONTH - 75)) | (1L << (DAY - 75)) | (1L << (HOUR - 75)) | (1L << (MINUTE - 75)) | (1L << (SECOND - 75)) | (1L << (CURRENT - 75)) | (1L << (ENABLE - 75)) | (1L << (DISABLE - 75)) | (1L << (CALL - 75)) | (1L << (PRESERVE - 75)) | (1L << (DEFINER - 75)) | (1L << (SQL - 75)) | (1L << (CASCADED - 75)) | (1L << (LOCAL - 75)) | (1L << (CLOSE - 75)) | (1L << (NEXT - 75)) | (1L << (NAME - 75)) | (1L << (NAMES - 75)) | (1L << (TYPE - 75)) | (1L << (TEXT - 75)) | (1L << (REPEATABLE - 75)) | (1L << (VARYING - 75)))) != 0) || ((((_la - 140)) & ~0x3f) == 0 && ((1L << (_la - 140)) & ((1L << (VALUE - 140)) | (1L << (TIES - 140)) | (1L << (CUBE - 140)) | (1L << (SETS - 140)) | (1L << (OTHERS - 140)) | (1L << (AT - 140)) | (1L << (ADMIN - 140)) | (1L << (ESCAPE - 140)) | (1L << (EXCLUDE - 140)) | (1L << (PARTITION - 140)) | (1L << (UNKNOWN - 140)) | (1L << (ALWAYS - 140)) | (1L << (CASCADE - 140)) | (1L << (GENERATED - 140)) | (1L << (ISOLATION - 140)) | (1L << (LEVEL - 140)) | (1L << (NO - 140)) | (1L << (OPTION - 140)) | (1L << (PRIVILEGES - 140)) | (1L << (READ - 140)) | (1L << (ROLE - 140)) | (1L << (ROWS - 140)) | (1L << (START - 140)) | (1L << (TRANSACTION - 140)) | (1L << (ACTION - 140)) | (1L << (CACHE - 140)) | (1L << (CHARACTERISTICS - 140)) | (1L << (CLUSTER - 140)) | (1L << (COMMENTS - 140)) | (1L << (CONSTRAINTS - 140)) | (1L << (CYCLE - 140)) | (1L << (DATA - 140)) | (1L << (DATABASE - 140)) | (1L << (DEFAULTS - 140)) | (1L << (DEFERRED - 140)) | (1L << (DEPENDS - 140)) | (1L << (DOMAIN - 140)) | (1L << (EXCLUDING - 140)) | (1L << (EXECUTE - 140)))) != 0) || ((((_la - 204)) & ~0x3f) == 0 && ((1L << (_la - 204)) & ((1L << (EXTENSION - 204)) | (1L << (EXTERNAL - 204)) | (1L << (FILTER - 204)) | (1L << (FIRST - 204)) | (1L << (FOLLOWING - 204)) | (1L << (FORCE - 204)) | (1L << (GLOBAL - 204)) | (1L << (IDENTITY - 204)) | (1L << (IMMEDIATE - 204)) | (1L << (INCLUDING - 204)) | (1L << (INCREMENT - 204)) | (1L << (INDEXES - 204)) | (1L << (INHERIT - 204)) | (1L << (INHERITS - 204)) | (1L << (INCLUDE - 204)) | (1L << (LANGUAGE - 204)) | (1L << (LARGE - 204)) | (1L << (LAST - 204)) | (1L << (LOGGED - 204)) | (1L << (MATCH - 204)) | (1L << (MAXVALUE - 204)) | (1L << (MINVALUE - 204)) | (1L << (NOTHING - 204)) | (1L << (NULLS - 204)) | (1L << (OBJECT - 204)) | (1L << (OIDS - 204)) | (1L << (OVER - 204)) | (1L << (OWNED - 204)) | (1L << (OWNER - 204)) | (1L << (PARTIAL - 204)) | (1L << (PRECEDING - 204)) | (1L << (RANGE - 204)) | (1L << (RENAME - 204)) | (1L << (REPLICA - 204)) | (1L << (RESET - 204)) | (1L << (RESTART - 204)) | (1L << (RESTRICT - 204)) | (1L << (ROUTINE - 204)) | (1L << (RULE - 204)) | (1L << (SECURITY - 204)) | (1L << (SEQUENCE - 204)) | (1L << (SESSION - 204)) | (1L << (SHOW - 204)) | (1L << (SIMPLE - 204)) | (1L << (STATISTICS - 204)) | (1L << (STORAGE - 204)) | (1L << (TABLESPACE - 204)) | (1L << (TEMP - 204)) | (1L << (TEMPORARY - 204)) | (1L << (UNBOUNDED - 204)) | (1L << (UNLOGGED - 204)) | (1L << (VALID - 204)) | (1L << (VALIDATE - 204)) | (1L << (WITHIN - 204)) | (1L << (WITHOUT - 204)) | (1L << (ZONE - 204)) | (1L << (OF - 204)))) != 0) || ((((_la - 268)) & ~0x3f) == 0 && ((1L << (_la - 268)) & ((1L << (UESCAPE - 268)) | (1L << (GROUPS - 268)) | (1L << (RECURSIVE - 268)) | (1L << (ENUM - 268)) | (1L << (XML - 268)) | (1L << (JSON - 268)) | (1L << (ORDINALITY - 268)) | (1L << (NFC - 268)) | (1L << (NFD - 268)) | (1L << (NFKC - 268)) | (1L << (NFKD - 268)) | (1L << (REF - 268)) | (1L << (PASSING - 268)))) != 0) || ((((_la - 332)) & ~0x3f) == 0 && ((1L << (_la - 332)) & ((1L << (VERSION - 332)) | (1L << (YES - 332)) | (1L << (STANDALONE - 332)) | (1L << (MATERIALIZED - 332)) | (1L << (OPERATOR - 332)) | (1L << (SHARE - 332)) | (1L << (ROLLUP - 332)) | (1L << (DOCUMENT - 332)) | (1L << (NORMALIZED - 332)) | (1L << (NOWAIT - 332)) | (1L << (LOCKED - 332)) | (1L << (COLUMNS - 332)) | (1L << (CONTENT - 332)) | (1L << (STRIP - 332)) | (1L << (WHITESPACE - 332)) | (1L << (CONFLICT - 332)) | (1L << (OVERRIDING - 332)) | (1L << (SYSTEM - 332)) | (1L << (ABORT - 332)) | (1L << (ABSOLUTE - 332)) | (1L << (ACCESS - 332)) | (1L << (AFTER - 332)) | (1L << (AGGREGATE - 332)) | (1L << (ALSO - 332)) | (1L << (ATTACH - 332)) | (1L << (ATTRIBUTE - 332)) | (1L << (BACKWARD - 332)) | (1L << (BEFORE - 332)) | (1L << (ASSERTION - 332)) | (1L << (ASSIGNMENT - 332)) | (1L << (CONTINUE - 332)) | (1L << (CONVERSION - 332)) | (1L << (COPY - 332)) | (1L << (COST - 332)) | (1L << (CSV - 332)) | (1L << (CALLED - 332)) | (1L << (CATALOG - 332)) | (1L << (CHAIN - 332)) | (1L << (CHECKPOINT - 332)) | (1L << (CLASS - 332)) | (1L << (CONFIGURATION - 332)) | (1L << (COMMENT - 332)) | (1L << (DETACH - 332)) | (1L << (DICTIONARY - 332)) | (1L << (EXPRESSION - 332)) | (1L << (INSENSITIVE - 332)) | (1L << (DISCARD - 332)))) != 0) || ((((_la - 396)) & ~0x3f) == 0 && ((1L << (_la - 396)) & ((1L << (OFF - 396)) | (1L << (INSTEAD - 396)) | (1L << (EXPLAIN - 396)) | (1L << (INPUT - 396)) | (1L << (INLINE - 396)) | (1L << (PARALLEL - 396)) | (1L << (LEAKPROOF - 396)) | (1L << (COMMITTED - 396)) | (1L << (ENCODING - 396)) | (1L << (IMPLICIT - 396)) | (1L << (DELIMITER - 396)) | (1L << (CURSOR - 396)) | (1L << (EACH - 396)) | (1L << (EVENT - 396)) | (1L << (DEALLOCATE - 396)) | (1L << (CONNECTION - 396)) | (1L << (DECLARE - 396)) | (1L << (FAMILY - 396)) | (1L << (FORWARD - 396)) | (1L << (EXCLUSIVE - 396)) | (1L << (FUNCTIONS - 396)) | (1L << (LOCATION - 396)) | (1L << (LABEL - 396)) | (1L << (DELIMITERS - 396)) | (1L << (HANDLER - 396)) | (1L << (HEADER - 396)) | (1L << (IMMUTABLE - 396)) | (1L << (GRANTED - 396)) | (1L << (HOLD - 396)) | (1L << (MAPPING - 396)) | (1L << (OLD - 396)) | (1L << (METHOD - 396)) | (1L << (LOAD - 396)) | (1L << (LISTEN - 396)) | (1L << (MODE - 396)) | (1L << (MOVE - 396)) | (1L << (PROCEDURAL - 396)) | (1L << (PARSER - 396)) | (1L << (PROCEDURES - 396)) | (1L << (ENCRYPTED - 396)) | (1L << (PUBLICATION - 396)) | (1L << (PROGRAM - 396)) | (1L << (REFERENCING - 396)) | (1L << (PLANS - 396)) | (1L << (REINDEX - 396)) | (1L << (PRIOR - 396)) | (1L << (PASSWORD - 396)) | (1L << (RELATIVE - 396)) | (1L << (QUOTE - 396)) | (1L << (ROUTINES - 396)) | (1L << (REPLACE - 396)) | (1L << (SNAPSHOT - 396)) | (1L << (REFRESH - 396)) | (1L << (PREPARE - 396)) | (1L << (OPTIONS - 396)) | (1L << (IMPORT - 396)) | (1L << (INVOKER - 396)) | (1L << (NEW - 396)) | (1L << (PREPARED - 396)) | (1L << (SCROLL - 396)) | (1L << (SEQUENCES - 396)) | (1L << (SYSID - 396)) | (1L << (REASSIGN - 396)) | (1L << (SERVER - 396)))) != 0) || ((((_la - 460)) & ~0x3f) == 0 && ((1L << (_la - 460)) & ((1L << (SUBSCRIPTION - 460)) | (1L << (SEARCH - 460)) | (1L << (SCHEMAS - 460)) | (1L << (RECHECK - 460)) | (1L << (POLICY - 460)) | (1L << (NOTIFY - 460)) | (1L << (LOCK - 460)) | (1L << (RELEASE - 460)) | (1L << (SERIALIZABLE - 460)) | (1L << (RETURNS - 460)) | (1L << (STATEMENT - 460)) | (1L << (STDIN - 460)) | (1L << (STDOUT - 460)) | (1L << (TABLES - 460)) | (1L << (SUPPORT - 460)) | (1L << (STABLE - 460)) | (1L << (TEMPLATE - 460)) | (1L << (UNENCRYPTED - 460)) | (1L << (VIEWS - 460)) | (1L << (UNCOMMITTED - 460)) | (1L << (TRANSFORM - 460)) | (1L << (UNLISTEN - 460)) | (1L << (TRUSTED - 460)) | (1L << (VALIDATOR - 460)) | (1L << (UNTIL - 460)) | (1L << (VACUUM - 460)) | (1L << (VOLATILE - 460)) | (1L << (STORED - 460)) | (1L << (WRITE - 460)) | (1L << (STRICT - 460)) | (1L << (TYPES - 460)) | (1L << (WRAPPER - 460)) | (1L << (WORK - 460)))) != 0) || _la==IDENTIFIER_) {
					{
					setState(1415);
					extractList();
					}
				}

				setState(1418);
				match(RP_);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1419);
				match(NORMALIZE);
				setState(1420);
				match(LP_);
				setState(1421);
				aExpr(0);
				setState(1422);
				match(RP_);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(1424);
				match(NORMALIZE);
				setState(1425);
				match(LP_);
				setState(1426);
				aExpr(0);
				setState(1427);
				match(COMMA_);
				setState(1428);
				unicodeNormalForm();
				setState(1429);
				match(RP_);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(1431);
				match(OVERLAY);
				setState(1432);
				match(LP_);
				setState(1433);
				overlayList();
				setState(1434);
				match(RP_);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(1436);
				match(POSITION);
				setState(1437);
				match(LP_);
				setState(1438);
				positionList();
				setState(1439);
				match(RP_);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(1441);
				match(SUBSTRING);
				setState(1442);
				match(LP_);
				setState(1443);
				substrList();
				setState(1444);
				match(RP_);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(1446);
				match(TREAT);
				setState(1447);
				match(LP_);
				setState(1448);
				aExpr(0);
				setState(1449);
				match(AS);
				setState(1450);
				typeName();
				setState(1451);
				match(RP_);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(1453);
				match(TRIM);
				setState(1454);
				match(LP_);
				setState(1455);
				match(BOTH);
				setState(1456);
				trimList();
				setState(1457);
				match(RP_);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(1459);
				match(TRIM);
				setState(1460);
				match(LP_);
				setState(1461);
				match(LEADING);
				setState(1462);
				trimList();
				setState(1463);
				match(RP_);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(1465);
				match(TRIM);
				setState(1466);
				match(LP_);
				setState(1467);
				match(TRAILING);
				setState(1468);
				trimList();
				setState(1469);
				match(RP_);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(1471);
				match(TRIM);
				setState(1472);
				match(LP_);
				setState(1473);
				trimList();
				setState(1474);
				match(RP_);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(1476);
				match(NULLIF);
				setState(1477);
				match(LP_);
				setState(1478);
				aExpr(0);
				setState(1479);
				match(COMMA_);
				setState(1480);
				aExpr(0);
				setState(1481);
				match(RP_);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(1483);
				match(COALESCE);
				setState(1484);
				match(LP_);
				setState(1485);
				exprList(0);
				setState(1486);
				match(RP_);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(1488);
				match(GREATEST);
				setState(1489);
				match(LP_);
				setState(1490);
				exprList(0);
				setState(1491);
				match(RP_);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(1493);
				match(LEAST);
				setState(1494);
				match(LP_);
				setState(1495);
				exprList(0);
				setState(1496);
				match(RP_);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(1498);
				match(XMLCONCAT);
				setState(1499);
				match(LP_);
				setState(1500);
				exprList(0);
				setState(1501);
				match(RP_);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(1503);
				match(XMLELEMENT);
				setState(1504);
				match(LP_);
				setState(1505);
				match(NAME);
				setState(1506);
				identifier();
				setState(1507);
				match(RP_);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(1509);
				match(XMLELEMENT);
				setState(1510);
				match(LP_);
				setState(1511);
				match(NAME);
				setState(1512);
				identifier();
				setState(1513);
				match(COMMA_);
				setState(1514);
				xmlAttributes();
				setState(1515);
				match(RP_);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(1517);
				match(XMLELEMENT);
				setState(1518);
				match(LP_);
				setState(1519);
				match(NAME);
				setState(1520);
				identifier();
				setState(1521);
				match(COMMA_);
				setState(1522);
				exprList(0);
				setState(1523);
				match(RP_);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(1525);
				match(XMLELEMENT);
				setState(1526);
				match(LP_);
				setState(1527);
				match(NAME);
				setState(1528);
				identifier();
				setState(1529);
				match(COMMA_);
				setState(1530);
				xmlAttributes();
				setState(1531);
				match(COMMA_);
				setState(1532);
				exprList(0);
				setState(1533);
				match(RP_);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(1535);
				match(XMLEXISTS);
				setState(1536);
				match(LP_);
				setState(1537);
				cExpr();
				setState(1538);
				xmlExistsArgument();
				setState(1539);
				match(RP_);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(1541);
				match(XMLFOREST);
				setState(1542);
				match(LP_);
				setState(1543);
				xmlAttributeList();
				setState(1544);
				match(RP_);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(1546);
				match(XMLPARSE);
				setState(1547);
				match(LP_);
				setState(1548);
				documentOrContent();
				setState(1549);
				aExpr(0);
				setState(1550);
				xmlWhitespaceOption();
				setState(1551);
				match(RP_);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(1553);
				match(XMLPI);
				setState(1554);
				match(LP_);
				setState(1555);
				match(NAME);
				setState(1556);
				identifier();
				setState(1557);
				match(RP_);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(1559);
				match(XMLPI);
				setState(1560);
				match(LP_);
				setState(1561);
				match(NAME);
				setState(1562);
				identifier();
				setState(1563);
				match(COMMA_);
				setState(1564);
				aExpr(0);
				setState(1565);
				match(RP_);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(1567);
				match(XMLROOT);
				setState(1568);
				match(LP_);
				setState(1569);
				aExpr(0);
				setState(1570);
				match(COMMA_);
				setState(1571);
				xmlRootVersion();
				setState(1573);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(1572);
					xmlRootStandalone();
					}
				}

				setState(1575);
				match(RP_);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(1577);
				match(XMLSERIALIZE);
				setState(1578);
				match(LP_);
				setState(1579);
				documentOrContent();
				setState(1580);
				aExpr(0);
				setState(1581);
				match(AS);
				setState(1582);
				simpleTypeName();
				setState(1583);
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

	public static class TypeNameContext extends ParserRuleContext {
		public SimpleTypeNameContext simpleTypeName() {
			return getRuleContext(SimpleTypeNameContext.class,0);
		}
		public OptArrayBoundsContext optArrayBounds() {
			return getRuleContext(OptArrayBoundsContext.class,0);
		}
		public TerminalNode SETOF() { return getToken(BaseRuleParser.SETOF, 0); }
		public TerminalNode ARRAY() { return getToken(BaseRuleParser.ARRAY, 0); }
		public TerminalNode LBT_() { return getToken(BaseRuleParser.LBT_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RBT_() { return getToken(BaseRuleParser.RBT_, 0); }
		public TypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeNameContext typeName() throws RecognitionException {
		TypeNameContext _localctx = new TypeNameContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_typeName);
		try {
			setState(1614);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1587);
				simpleTypeName();
				setState(1588);
				optArrayBounds(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1590);
				match(SETOF);
				setState(1591);
				simpleTypeName();
				setState(1592);
				optArrayBounds(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1594);
				simpleTypeName();
				setState(1595);
				match(ARRAY);
				setState(1596);
				match(LBT_);
				setState(1597);
				match(NUMBER_);
				setState(1598);
				match(RBT_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1600);
				match(SETOF);
				setState(1601);
				simpleTypeName();
				setState(1602);
				match(ARRAY);
				setState(1603);
				match(LBT_);
				setState(1604);
				match(NUMBER_);
				setState(1605);
				match(RBT_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1607);
				simpleTypeName();
				setState(1608);
				match(ARRAY);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1610);
				match(SETOF);
				setState(1611);
				simpleTypeName();
				setState(1612);
				match(ARRAY);
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

	public static class SimpleTypeNameContext extends ParserRuleContext {
		public GenericTypeContext genericType() {
			return getRuleContext(GenericTypeContext.class,0);
		}
		public NumericContext numeric() {
			return getRuleContext(NumericContext.class,0);
		}
		public BitContext bit() {
			return getRuleContext(BitContext.class,0);
		}
		public CharacterContext character() {
			return getRuleContext(CharacterContext.class,0);
		}
		public ConstDatetimeContext constDatetime() {
			return getRuleContext(ConstDatetimeContext.class,0);
		}
		public ConstIntervalContext constInterval() {
			return getRuleContext(ConstIntervalContext.class,0);
		}
		public OptIntervalContext optInterval() {
			return getRuleContext(OptIntervalContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public SimpleTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleTypeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSimpleTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSimpleTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSimpleTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleTypeNameContext simpleTypeName() throws RecognitionException {
		SimpleTypeNameContext _localctx = new SimpleTypeNameContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_simpleTypeName);
		try {
			setState(1629);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1616);
				genericType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1617);
				numeric();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1618);
				bit();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1619);
				character();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1620);
				constDatetime();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1621);
				constInterval();
				setState(1622);
				optInterval();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1624);
				constInterval();
				setState(1625);
				match(LP_);
				setState(1626);
				match(NUMBER_);
				setState(1627);
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

	public static class ExprListContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
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
		return exprList(0);
	}

	private ExprListContext exprList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprListContext _localctx = new ExprListContext(_ctx, _parentState);
		ExprListContext _prevctx = _localctx;
		int _startState = 166;
		enterRecursionRule(_localctx, 166, RULE_exprList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1632);
			aExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(1639);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,84,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_exprList);
					setState(1634);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(1635);
					match(COMMA_);
					setState(1636);
					aExpr(0);
					}
					} 
				}
				setState(1641);
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

	public static class ExtractListContext extends ParserRuleContext {
		public ExtractArgContext extractArg() {
			return getRuleContext(ExtractArgContext.class,0);
		}
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public ExtractListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extractList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterExtractList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitExtractList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitExtractList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtractListContext extractList() throws RecognitionException {
		ExtractListContext _localctx = new ExtractListContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_extractList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1642);
			extractArg();
			setState(1643);
			match(FROM);
			setState(1644);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtractArgContext extends ParserRuleContext {
		public TerminalNode YEAR() { return getToken(BaseRuleParser.YEAR, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public TerminalNode DAY() { return getToken(BaseRuleParser.DAY, 0); }
		public TerminalNode HOUR() { return getToken(BaseRuleParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(BaseRuleParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ExtractArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extractArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterExtractArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitExtractArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitExtractArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtractArgContext extractArg() throws RecognitionException {
		ExtractArgContext _localctx = new ExtractArgContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_extractArg);
		try {
			setState(1653);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1646);
				match(YEAR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1647);
				match(MONTH);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1648);
				match(DAY);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1649);
				match(HOUR);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1650);
				match(MINUTE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1651);
				match(SECOND);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1652);
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

	public static class GenericTypeContext extends ParserRuleContext {
		public TypeFunctionNameContext typeFunctionName() {
			return getRuleContext(TypeFunctionNameContext.class,0);
		}
		public TypeModifiersContext typeModifiers() {
			return getRuleContext(TypeModifiersContext.class,0);
		}
		public AttrsContext attrs() {
			return getRuleContext(AttrsContext.class,0);
		}
		public GenericTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericTypeContext genericType() throws RecognitionException {
		GenericTypeContext _localctx = new GenericTypeContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_genericType);
		try {
			setState(1664);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1655);
				typeFunctionName();
				setState(1657);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(1656);
					typeModifiers();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1659);
				typeFunctionName();
				setState(1660);
				attrs(0);
				setState(1662);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(1661);
					typeModifiers();
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

	public static class TypeModifiersContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TypeModifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeModifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTypeModifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTypeModifiers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTypeModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeModifiersContext typeModifiers() throws RecognitionException {
		TypeModifiersContext _localctx = new TypeModifiersContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_typeModifiers);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1666);
			match(LP_);
			setState(1667);
			exprList(0);
			setState(1668);
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

	public static class NumericContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(BaseRuleParser.INT, 0); }
		public TerminalNode INTEGER() { return getToken(BaseRuleParser.INTEGER, 0); }
		public TerminalNode SMALLINT() { return getToken(BaseRuleParser.SMALLINT, 0); }
		public TerminalNode BIGINT() { return getToken(BaseRuleParser.BIGINT, 0); }
		public TerminalNode REAL() { return getToken(BaseRuleParser.REAL, 0); }
		public TerminalNode FLOAT() { return getToken(BaseRuleParser.FLOAT, 0); }
		public OptFloatContext optFloat() {
			return getRuleContext(OptFloatContext.class,0);
		}
		public TerminalNode DOUBLE() { return getToken(BaseRuleParser.DOUBLE, 0); }
		public TerminalNode PRECISION() { return getToken(BaseRuleParser.PRECISION, 0); }
		public TerminalNode DECIMAL() { return getToken(BaseRuleParser.DECIMAL, 0); }
		public TypeModifiersContext typeModifiers() {
			return getRuleContext(TypeModifiersContext.class,0);
		}
		public TerminalNode DEC() { return getToken(BaseRuleParser.DEC, 0); }
		public TerminalNode NUMERIC() { return getToken(BaseRuleParser.NUMERIC, 0); }
		public TerminalNode BOOLEAN() { return getToken(BaseRuleParser.BOOLEAN, 0); }
		public TerminalNode FLOAT8() { return getToken(BaseRuleParser.FLOAT8, 0); }
		public TerminalNode FLOAT4() { return getToken(BaseRuleParser.FLOAT4, 0); }
		public TerminalNode INT2() { return getToken(BaseRuleParser.INT2, 0); }
		public TerminalNode INT4() { return getToken(BaseRuleParser.INT4, 0); }
		public TerminalNode INT8() { return getToken(BaseRuleParser.INT8, 0); }
		public NumericContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numeric; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNumeric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNumeric(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNumeric(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericContext numeric() throws RecognitionException {
		NumericContext _localctx = new NumericContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_numeric);
		try {
			setState(1697);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(1670);
				match(INT);
				}
				break;
			case INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(1671);
				match(INTEGER);
				}
				break;
			case SMALLINT:
				enterOuterAlt(_localctx, 3);
				{
				setState(1672);
				match(SMALLINT);
				}
				break;
			case BIGINT:
				enterOuterAlt(_localctx, 4);
				{
				setState(1673);
				match(BIGINT);
				}
				break;
			case REAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(1674);
				match(REAL);
				}
				break;
			case FLOAT:
				enterOuterAlt(_localctx, 6);
				{
				setState(1675);
				match(FLOAT);
				setState(1676);
				optFloat();
				}
				break;
			case DOUBLE:
				enterOuterAlt(_localctx, 7);
				{
				setState(1677);
				match(DOUBLE);
				setState(1678);
				match(PRECISION);
				}
				break;
			case DECIMAL:
				enterOuterAlt(_localctx, 8);
				{
				setState(1679);
				match(DECIMAL);
				setState(1681);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
				case 1:
					{
					setState(1680);
					typeModifiers();
					}
					break;
				}
				}
				break;
			case DEC:
				enterOuterAlt(_localctx, 9);
				{
				setState(1683);
				match(DEC);
				setState(1685);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(1684);
					typeModifiers();
					}
					break;
				}
				}
				break;
			case NUMERIC:
				enterOuterAlt(_localctx, 10);
				{
				setState(1687);
				match(NUMERIC);
				setState(1689);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(1688);
					typeModifiers();
					}
					break;
				}
				}
				break;
			case BOOLEAN:
				enterOuterAlt(_localctx, 11);
				{
				setState(1691);
				match(BOOLEAN);
				}
				break;
			case FLOAT8:
				enterOuterAlt(_localctx, 12);
				{
				setState(1692);
				match(FLOAT8);
				}
				break;
			case FLOAT4:
				enterOuterAlt(_localctx, 13);
				{
				setState(1693);
				match(FLOAT4);
				}
				break;
			case INT2:
				enterOuterAlt(_localctx, 14);
				{
				setState(1694);
				match(INT2);
				}
				break;
			case INT4:
				enterOuterAlt(_localctx, 15);
				{
				setState(1695);
				match(INT4);
				}
				break;
			case INT8:
				enterOuterAlt(_localctx, 16);
				{
				setState(1696);
				match(INT8);
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

	public static class ConstDatetimeContext extends ParserRuleContext {
		public TerminalNode TIMESTAMP() { return getToken(BaseRuleParser.TIMESTAMP, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TimezoneContext timezone() {
			return getRuleContext(TimezoneContext.class,0);
		}
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode DATE() { return getToken(BaseRuleParser.DATE, 0); }
		public ConstDatetimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constDatetime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConstDatetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConstDatetime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConstDatetime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstDatetimeContext constDatetime() throws RecognitionException {
		ConstDatetimeContext _localctx = new ConstDatetimeContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_constDatetime);
		try {
			setState(1722);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1699);
				match(TIMESTAMP);
				setState(1700);
				match(LP_);
				setState(1701);
				match(NUMBER_);
				setState(1702);
				match(RP_);
				setState(1704);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
				case 1:
					{
					setState(1703);
					timezone();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1706);
				match(TIMESTAMP);
				setState(1708);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
				case 1:
					{
					setState(1707);
					timezone();
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1710);
				match(TIME);
				setState(1711);
				match(LP_);
				setState(1712);
				match(NUMBER_);
				setState(1713);
				match(RP_);
				setState(1715);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
				case 1:
					{
					setState(1714);
					timezone();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1717);
				match(TIME);
				setState(1719);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
				case 1:
					{
					setState(1718);
					timezone();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1721);
				match(DATE);
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

	public static class TimezoneContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode WITHOUT() { return getToken(BaseRuleParser.WITHOUT, 0); }
		public TimezoneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timezone; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTimezone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTimezone(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTimezone(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimezoneContext timezone() throws RecognitionException {
		TimezoneContext _localctx = new TimezoneContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_timezone);
		try {
			setState(1730);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WITH:
				enterOuterAlt(_localctx, 1);
				{
				setState(1724);
				match(WITH);
				setState(1725);
				match(TIME);
				setState(1726);
				match(ZONE);
				}
				break;
			case WITHOUT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1727);
				match(WITHOUT);
				setState(1728);
				match(TIME);
				setState(1729);
				match(ZONE);
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

	public static class CharacterContext extends ParserRuleContext {
		public CharacterWithLengthContext characterWithLength() {
			return getRuleContext(CharacterWithLengthContext.class,0);
		}
		public CharacterWithoutLengthContext characterWithoutLength() {
			return getRuleContext(CharacterWithoutLengthContext.class,0);
		}
		public CharacterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_character; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCharacter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCharacter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCharacter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterContext character() throws RecognitionException {
		CharacterContext _localctx = new CharacterContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_character);
		try {
			setState(1734);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1732);
				characterWithLength();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1733);
				characterWithoutLength();
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

	public static class CharacterWithLengthContext extends ParserRuleContext {
		public CharacterClauseContext characterClause() {
			return getRuleContext(CharacterClauseContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public CharacterWithLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterWithLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCharacterWithLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCharacterWithLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCharacterWithLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterWithLengthContext characterWithLength() throws RecognitionException {
		CharacterWithLengthContext _localctx = new CharacterWithLengthContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_characterWithLength);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1736);
			characterClause();
			setState(1737);
			match(LP_);
			setState(1738);
			match(NUMBER_);
			setState(1739);
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

	public static class CharacterWithoutLengthContext extends ParserRuleContext {
		public CharacterClauseContext characterClause() {
			return getRuleContext(CharacterClauseContext.class,0);
		}
		public CharacterWithoutLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterWithoutLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCharacterWithoutLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCharacterWithoutLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCharacterWithoutLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterWithoutLengthContext characterWithoutLength() throws RecognitionException {
		CharacterWithoutLengthContext _localctx = new CharacterWithoutLengthContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_characterWithoutLength);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1741);
			characterClause();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharacterClauseContext extends ParserRuleContext {
		public TerminalNode CHARACTER() { return getToken(BaseRuleParser.CHARACTER, 0); }
		public TerminalNode VARYING() { return getToken(BaseRuleParser.VARYING, 0); }
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode VARCHAR() { return getToken(BaseRuleParser.VARCHAR, 0); }
		public TerminalNode NATIONAL() { return getToken(BaseRuleParser.NATIONAL, 0); }
		public TerminalNode NCHAR() { return getToken(BaseRuleParser.NCHAR, 0); }
		public CharacterClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCharacterClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCharacterClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCharacterClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterClauseContext characterClause() throws RecognitionException {
		CharacterClauseContext _localctx = new CharacterClauseContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_characterClause);
		try {
			setState(1766);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1743);
				match(CHARACTER);
				setState(1745);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
				case 1:
					{
					setState(1744);
					match(VARYING);
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1747);
				match(CHAR);
				setState(1749);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
				case 1:
					{
					setState(1748);
					match(VARYING);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1751);
				match(VARCHAR);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1752);
				match(NATIONAL);
				setState(1753);
				match(CHARACTER);
				setState(1755);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
				case 1:
					{
					setState(1754);
					match(VARYING);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1757);
				match(NATIONAL);
				setState(1758);
				match(CHAR);
				setState(1760);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
				case 1:
					{
					setState(1759);
					match(VARYING);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1762);
				match(NCHAR);
				setState(1764);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,104,_ctx) ) {
				case 1:
					{
					setState(1763);
					match(VARYING);
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

	public static class OptFloatContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public OptFloatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optFloat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOptFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOptFloat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOptFloat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptFloatContext optFloat() throws RecognitionException {
		OptFloatContext _localctx = new OptFloatContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_optFloat);
		try {
			setState(1772);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,106,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1768);
				match(LP_);
				setState(1769);
				match(NUMBER_);
				setState(1770);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
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

	public static class AttrsContext extends ParserRuleContext {
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public AttrNameContext attrName() {
			return getRuleContext(AttrNameContext.class,0);
		}
		public AttrsContext attrs() {
			return getRuleContext(AttrsContext.class,0);
		}
		public AttrsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attrs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAttrs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAttrs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAttrs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttrsContext attrs() throws RecognitionException {
		return attrs(0);
	}

	private AttrsContext attrs(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		AttrsContext _localctx = new AttrsContext(_ctx, _parentState);
		AttrsContext _prevctx = _localctx;
		int _startState = 192;
		enterRecursionRule(_localctx, 192, RULE_attrs, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1775);
			match(DOT_);
			setState(1776);
			attrName();
			}
			_ctx.stop = _input.LT(-1);
			setState(1783);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new AttrsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_attrs);
					setState(1778);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(1779);
					match(DOT_);
					setState(1780);
					attrName();
					}
					} 
				}
				setState(1785);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
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

	public static class AttrNameContext extends ParserRuleContext {
		public ColLableContext colLable() {
			return getRuleContext(ColLableContext.class,0);
		}
		public AttrNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attrName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAttrName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAttrName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAttrName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttrNameContext attrName() throws RecognitionException {
		AttrNameContext _localctx = new AttrNameContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_attrName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1786);
			colLable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColLableContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ColNameKeywordContext colNameKeyword() {
			return getRuleContext(ColNameKeywordContext.class,0);
		}
		public TypeFuncNameKeywordContext typeFuncNameKeyword() {
			return getRuleContext(TypeFuncNameKeywordContext.class,0);
		}
		public ReservedKeywordContext reservedKeyword() {
			return getRuleContext(ReservedKeywordContext.class,0);
		}
		public ColLableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colLable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColLable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColLable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColLable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColLableContext colLable() throws RecognitionException {
		ColLableContext _localctx = new ColLableContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_colLable);
		try {
			setState(1792);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case IF:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case VARYING:
			case VALUE:
			case TIES:
			case CUBE:
			case SETS:
			case OTHERS:
			case AT:
			case ADMIN:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case ORDINALITY:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1788);
				identifier();
				}
				break;
			case POSITION:
			case PRECISION:
			case VALUES:
			case TRIM:
			case SUBSTRING:
			case EXISTS:
			case BETWEEN:
			case BOOLEAN:
			case CHAR:
			case CHARACTER:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case INTEGER:
			case REAL:
			case DECIMAL:
			case SMALLINT:
			case BIGINT:
			case NUMERIC:
			case NULLIF:
			case NATIONAL:
			case NCHAR:
			case COALESCE:
			case GROUPING:
			case DEC:
			case ROW:
			case EXTRACT:
			case INT:
			case FLOAT:
			case VARCHAR:
			case BIT:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case SETOF:
			case XMLATTRIBUTES:
			case GREATEST:
			case LEAST:
			case XMLTABLE:
			case XMLNAMESPACES:
			case NONE:
			case OUT:
			case INOUT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1789);
				colNameKeyword();
				}
				break;
			case NATURAL:
			case JOIN:
			case FULL:
			case INNER:
			case OUTER:
			case LEFT:
			case RIGHT:
			case CROSS:
			case IS:
			case LIKE:
			case COLLATION:
			case OVERLAPS:
			case BINARY:
			case CONCURRENTLY:
			case TABLESAMPLE:
			case CURRENT_SCHEMA:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1790);
				typeFuncNameKeyword();
				}
				break;
			case SELECT:
			case CREATE:
			case GRANT:
			case TABLE:
			case COLUMN:
			case CONSTRAINT:
			case PRIMARY:
			case UNIQUE:
			case FOREIGN:
			case INTO:
			case WITH:
			case UNION:
			case DISTINCT:
			case CASE:
			case WHEN:
			case CAST:
			case FROM:
			case USING:
			case WHERE:
			case AS:
			case ON:
			case ELSE:
			case THEN:
			case FOR:
			case TO:
			case AND:
			case OR:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case IN:
			case ALL:
			case ANY:
			case ORDER:
			case GROUP:
			case ASC:
			case DESC:
			case HAVING:
			case LIMIT:
			case OFFSET:
			case ARRAY:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case DEFAULT:
			case DO:
			case CURRENT_USER:
			case CURRENT_DATE:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case BOTH:
			case LEADING:
			case TRAILING:
			case INTERSECT:
			case EXCEPT:
			case FETCH:
			case WINDOW:
			case SOME:
			case END:
			case CHECK:
			case REFERENCES:
			case USER:
			case COLLATE:
			case DEFERRABLE:
			case INITIALLY:
			case ONLY:
			case SESSION_USER:
			case CURRENT_ROLE:
			case CURRENT_CATALOG:
			case SYMMETRIC:
			case ASYMMETRIC:
			case VARIADIC:
			case PLACING:
			case RETURNING:
			case LATERAL:
			case ANALYSE:
			case ANALYZE:
				enterOuterAlt(_localctx, 4);
				{
				setState(1791);
				reservedKeyword();
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

	public static class BitContext extends ParserRuleContext {
		public BitWithLengthContext bitWithLength() {
			return getRuleContext(BitWithLengthContext.class,0);
		}
		public BitWithoutLengthContext bitWithoutLength() {
			return getRuleContext(BitWithoutLengthContext.class,0);
		}
		public BitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitContext bit() throws RecognitionException {
		BitContext _localctx = new BitContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_bit);
		try {
			setState(1796);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1794);
				bitWithLength();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1795);
				bitWithoutLength();
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

	public static class BitWithLengthContext extends ParserRuleContext {
		public TerminalNode BIT() { return getToken(BaseRuleParser.BIT, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode VARYING() { return getToken(BaseRuleParser.VARYING, 0); }
		public BitWithLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitWithLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBitWithLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBitWithLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBitWithLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitWithLengthContext bitWithLength() throws RecognitionException {
		BitWithLengthContext _localctx = new BitWithLengthContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_bitWithLength);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1798);
			match(BIT);
			setState(1800);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VARYING) {
				{
				setState(1799);
				match(VARYING);
				}
			}

			setState(1802);
			match(LP_);
			setState(1803);
			exprList(0);
			setState(1804);
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

	public static class BitWithoutLengthContext extends ParserRuleContext {
		public TerminalNode BIT() { return getToken(BaseRuleParser.BIT, 0); }
		public TerminalNode VARYING() { return getToken(BaseRuleParser.VARYING, 0); }
		public BitWithoutLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitWithoutLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBitWithoutLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBitWithoutLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBitWithoutLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitWithoutLengthContext bitWithoutLength() throws RecognitionException {
		BitWithoutLengthContext _localctx = new BitWithoutLengthContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_bitWithoutLength);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1806);
			match(BIT);
			setState(1808);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
			case 1:
				{
				setState(1807);
				match(VARYING);
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

	public static class ConstIntervalContext extends ParserRuleContext {
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public ConstIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConstInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConstInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConstInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstIntervalContext constInterval() throws RecognitionException {
		ConstIntervalContext _localctx = new ConstIntervalContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_constInterval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1810);
			match(INTERVAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OptIntervalContext extends ParserRuleContext {
		public TerminalNode YEAR() { return getToken(BaseRuleParser.YEAR, 0); }
		public TerminalNode MONTH() { return getToken(BaseRuleParser.MONTH, 0); }
		public TerminalNode DAY() { return getToken(BaseRuleParser.DAY, 0); }
		public TerminalNode HOUR() { return getToken(BaseRuleParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(BaseRuleParser.MINUTE, 0); }
		public IntervalSecondContext intervalSecond() {
			return getRuleContext(IntervalSecondContext.class,0);
		}
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public OptIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOptInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOptInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOptInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptIntervalContext optInterval() throws RecognitionException {
		OptIntervalContext _localctx = new OptIntervalContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_optInterval);
		try {
			setState(1840);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1812);
				match(YEAR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1813);
				match(MONTH);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1814);
				match(DAY);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1815);
				match(HOUR);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1816);
				match(MINUTE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1817);
				intervalSecond();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1818);
				match(YEAR);
				setState(1819);
				match(TO);
				setState(1820);
				match(MONTH);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1821);
				match(DAY);
				setState(1822);
				match(TO);
				setState(1823);
				match(HOUR);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1824);
				match(DAY);
				setState(1825);
				match(TO);
				setState(1826);
				match(MINUTE);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1827);
				match(DAY);
				setState(1828);
				match(TO);
				setState(1829);
				intervalSecond();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1830);
				match(HOUR);
				setState(1831);
				match(TO);
				setState(1832);
				match(MINUTE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1833);
				match(HOUR);
				setState(1834);
				match(TO);
				setState(1835);
				intervalSecond();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1836);
				match(MINUTE);
				setState(1837);
				match(TO);
				setState(1838);
				intervalSecond();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
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

	public static class OptArrayBoundsContext extends ParserRuleContext {
		public OptArrayBoundsContext optArrayBounds() {
			return getRuleContext(OptArrayBoundsContext.class,0);
		}
		public TerminalNode LBT_() { return getToken(BaseRuleParser.LBT_, 0); }
		public TerminalNode RBT_() { return getToken(BaseRuleParser.RBT_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public OptArrayBoundsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optArrayBounds; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOptArrayBounds(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOptArrayBounds(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOptArrayBounds(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptArrayBoundsContext optArrayBounds() throws RecognitionException {
		return optArrayBounds(0);
	}

	private OptArrayBoundsContext optArrayBounds(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		OptArrayBoundsContext _localctx = new OptArrayBoundsContext(_ctx, _parentState);
		OptArrayBoundsContext _prevctx = _localctx;
		int _startState = 208;
		enterRecursionRule(_localctx, 208, RULE_optArrayBounds, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			}
			_ctx.stop = _input.LT(-1);
			setState(1852);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,114,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1850);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
					case 1:
						{
						_localctx = new OptArrayBoundsContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_optArrayBounds);
						setState(1843);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1844);
						match(LBT_);
						setState(1845);
						match(RBT_);
						}
						break;
					case 2:
						{
						_localctx = new OptArrayBoundsContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_optArrayBounds);
						setState(1846);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1847);
						match(LBT_);
						setState(1848);
						match(NUMBER_);
						setState(1849);
						match(RBT_);
						}
						break;
					}
					} 
				}
				setState(1854);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,114,_ctx);
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

	public static class IntervalSecondContext extends ParserRuleContext {
		public TerminalNode SECOND() { return getToken(BaseRuleParser.SECOND, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public IntervalSecondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalSecond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIntervalSecond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIntervalSecond(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIntervalSecond(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalSecondContext intervalSecond() throws RecognitionException {
		IntervalSecondContext _localctx = new IntervalSecondContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_intervalSecond);
		try {
			setState(1860);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1855);
				match(SECOND);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1856);
				match(SECOND);
				setState(1857);
				match(LP_);
				setState(1858);
				match(NUMBER_);
				setState(1859);
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

	public static class UnicodeNormalFormContext extends ParserRuleContext {
		public TerminalNode NFC() { return getToken(BaseRuleParser.NFC, 0); }
		public TerminalNode NFD() { return getToken(BaseRuleParser.NFD, 0); }
		public TerminalNode NFKC() { return getToken(BaseRuleParser.NFKC, 0); }
		public TerminalNode NFKD() { return getToken(BaseRuleParser.NFKD, 0); }
		public UnicodeNormalFormContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unicodeNormalForm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterUnicodeNormalForm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitUnicodeNormalForm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitUnicodeNormalForm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnicodeNormalFormContext unicodeNormalForm() throws RecognitionException {
		UnicodeNormalFormContext _localctx = new UnicodeNormalFormContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_unicodeNormalForm);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1862);
			_la = _input.LA(1);
			if ( !(((((_la - 325)) & ~0x3f) == 0 && ((1L << (_la - 325)) & ((1L << (NFC - 325)) | (1L << (NFD - 325)) | (1L << (NFKC - 325)) | (1L << (NFKD - 325)))) != 0)) ) {
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

	public static class TrimListContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TrimListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trimList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTrimList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTrimList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTrimList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TrimListContext trimList() throws RecognitionException {
		TrimListContext _localctx = new TrimListContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_trimList);
		try {
			setState(1871);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1864);
				aExpr(0);
				setState(1865);
				match(FROM);
				setState(1866);
				exprList(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1868);
				match(FROM);
				setState(1869);
				exprList(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1870);
				exprList(0);
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

	public static class OverlayListContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public OverlayPlacingContext overlayPlacing() {
			return getRuleContext(OverlayPlacingContext.class,0);
		}
		public SubstrFromContext substrFrom() {
			return getRuleContext(SubstrFromContext.class,0);
		}
		public SubstrForContext substrFor() {
			return getRuleContext(SubstrForContext.class,0);
		}
		public OverlayListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_overlayList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOverlayList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOverlayList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOverlayList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OverlayListContext overlayList() throws RecognitionException {
		OverlayListContext _localctx = new OverlayListContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_overlayList);
		try {
			setState(1882);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1873);
				aExpr(0);
				setState(1874);
				overlayPlacing();
				setState(1875);
				substrFrom();
				setState(1876);
				substrFor();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1878);
				aExpr(0);
				setState(1879);
				overlayPlacing();
				setState(1880);
				substrFrom();
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

	public static class OverlayPlacingContext extends ParserRuleContext {
		public TerminalNode PLACING() { return getToken(BaseRuleParser.PLACING, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public OverlayPlacingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_overlayPlacing; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOverlayPlacing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOverlayPlacing(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOverlayPlacing(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OverlayPlacingContext overlayPlacing() throws RecognitionException {
		OverlayPlacingContext _localctx = new OverlayPlacingContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_overlayPlacing);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1884);
			match(PLACING);
			setState(1885);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubstrFromContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public SubstrFromContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substrFrom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubstrFrom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubstrFrom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubstrFrom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubstrFromContext substrFrom() throws RecognitionException {
		SubstrFromContext _localctx = new SubstrFromContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_substrFrom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1887);
			match(FROM);
			setState(1888);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubstrForContext extends ParserRuleContext {
		public TerminalNode FOR() { return getToken(BaseRuleParser.FOR, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public SubstrForContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substrFor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubstrFor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubstrFor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubstrFor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubstrForContext substrFor() throws RecognitionException {
		SubstrForContext _localctx = new SubstrForContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_substrFor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1890);
			match(FOR);
			setState(1891);
			aExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PositionListContext extends ParserRuleContext {
		public List<BExprContext> bExpr() {
			return getRuleContexts(BExprContext.class);
		}
		public BExprContext bExpr(int i) {
			return getRuleContext(BExprContext.class,i);
		}
		public TerminalNode IN() { return getToken(BaseRuleParser.IN, 0); }
		public PositionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPositionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPositionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPositionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PositionListContext positionList() throws RecognitionException {
		PositionListContext _localctx = new PositionListContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_positionList);
		try {
			setState(1898);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case POSITION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case CASE:
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
			case IF:
			case IS:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case ARRAY:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case CURRENT_USER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case CURRENT_DATE:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case NULLIF:
			case VARYING:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case USER:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SESSION_USER:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_ROLE:
			case CURRENT_CATALOG:
			case CURRENT_SCHEMA:
			case NORMALIZE:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case PARAM:
			case OR_:
			case PLUS_:
			case MINUS_:
			case LP_:
			case QUESTION_:
			case JSON_EXTRACT_:
			case JSON_EXTRACT_TEXT_:
			case JSON_PATH_EXTRACT_:
			case JSON_PATH_EXTRACT_TEXT_:
			case JSONB_CONTAIN_RIGHT_:
			case JSONB_CONTAIN_LEFT_:
			case JSONB_CONTAIN_ALL_TOP_KEY_:
			case JSONB_PATH_DELETE_:
			case JSONB_PATH_CONTAIN_ANY_VALUE_:
			case JSONB_PATH_PREDICATE_CHECK_:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(1893);
				bExpr(0);
				setState(1894);
				match(IN);
				setState(1895);
				bExpr(0);
				}
				break;
			case RP_:
				enterOuterAlt(_localctx, 2);
				{
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

	public static class SubstrListContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public SubstrFromContext substrFrom() {
			return getRuleContext(SubstrFromContext.class,0);
		}
		public SubstrForContext substrFor() {
			return getRuleContext(SubstrForContext.class,0);
		}
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public SubstrListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substrList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSubstrList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSubstrList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSubstrList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubstrListContext substrList() throws RecognitionException {
		SubstrListContext _localctx = new SubstrListContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_substrList);
		try {
			setState(1916);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1900);
				aExpr(0);
				setState(1901);
				substrFrom();
				setState(1902);
				substrFor();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1904);
				aExpr(0);
				setState(1905);
				substrFor();
				setState(1906);
				substrFrom();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1908);
				aExpr(0);
				setState(1909);
				substrFrom();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1911);
				aExpr(0);
				setState(1912);
				substrFor();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1914);
				exprList(0);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
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

	public static class XmlAttributesContext extends ParserRuleContext {
		public TerminalNode XMLATTRIBUTES() { return getToken(BaseRuleParser.XMLATTRIBUTES, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public XmlAttributeListContext xmlAttributeList() {
			return getRuleContext(XmlAttributeListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public XmlAttributesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlAttributes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlAttributes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlAttributes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlAttributes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlAttributesContext xmlAttributes() throws RecognitionException {
		XmlAttributesContext _localctx = new XmlAttributesContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_xmlAttributes);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1918);
			match(XMLATTRIBUTES);
			setState(1919);
			match(LP_);
			setState(1920);
			xmlAttributeList();
			setState(1921);
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

	public static class XmlAttributeListContext extends ParserRuleContext {
		public List<XmlAttributeElContext> xmlAttributeEl() {
			return getRuleContexts(XmlAttributeElContext.class);
		}
		public XmlAttributeElContext xmlAttributeEl(int i) {
			return getRuleContext(XmlAttributeElContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public XmlAttributeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlAttributeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlAttributeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlAttributeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlAttributeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlAttributeListContext xmlAttributeList() throws RecognitionException {
		XmlAttributeListContext _localctx = new XmlAttributeListContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_xmlAttributeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1923);
			xmlAttributeEl();
			setState(1928);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1924);
				match(COMMA_);
				setState(1925);
				xmlAttributeEl();
				}
				}
				setState(1930);
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

	public static class XmlAttributeElContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public XmlAttributeElContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlAttributeEl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlAttributeEl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlAttributeEl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlAttributeEl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlAttributeElContext xmlAttributeEl() throws RecognitionException {
		XmlAttributeElContext _localctx = new XmlAttributeElContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_xmlAttributeEl);
		try {
			setState(1936);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1931);
				aExpr(0);
				setState(1932);
				match(AS);
				setState(1933);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1935);
				aExpr(0);
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

	public static class XmlExistsArgumentContext extends ParserRuleContext {
		public TerminalNode PASSING() { return getToken(BaseRuleParser.PASSING, 0); }
		public CExprContext cExpr() {
			return getRuleContext(CExprContext.class,0);
		}
		public List<XmlPassingMechContext> xmlPassingMech() {
			return getRuleContexts(XmlPassingMechContext.class);
		}
		public XmlPassingMechContext xmlPassingMech(int i) {
			return getRuleContext(XmlPassingMechContext.class,i);
		}
		public XmlExistsArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlExistsArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlExistsArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlExistsArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlExistsArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlExistsArgumentContext xmlExistsArgument() throws RecognitionException {
		XmlExistsArgumentContext _localctx = new XmlExistsArgumentContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_xmlExistsArgument);
		try {
			setState(1953);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1938);
				match(PASSING);
				setState(1939);
				cExpr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1940);
				match(PASSING);
				setState(1941);
				cExpr();
				setState(1942);
				xmlPassingMech();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1944);
				match(PASSING);
				setState(1945);
				xmlPassingMech();
				setState(1946);
				cExpr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1948);
				match(PASSING);
				setState(1949);
				xmlPassingMech();
				setState(1950);
				cExpr();
				setState(1951);
				xmlPassingMech();
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

	public static class XmlPassingMechContext extends ParserRuleContext {
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public TerminalNode REF() { return getToken(BaseRuleParser.REF, 0); }
		public TerminalNode VALUE() { return getToken(BaseRuleParser.VALUE, 0); }
		public XmlPassingMechContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlPassingMech; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlPassingMech(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlPassingMech(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlPassingMech(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlPassingMechContext xmlPassingMech() throws RecognitionException {
		XmlPassingMechContext _localctx = new XmlPassingMechContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_xmlPassingMech);
		try {
			setState(1959);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1955);
				match(BY);
				setState(1956);
				match(REF);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1957);
				match(BY);
				setState(1958);
				match(VALUE);
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

	public static class DocumentOrContentContext extends ParserRuleContext {
		public TerminalNode DOCUMENT() { return getToken(BaseRuleParser.DOCUMENT, 0); }
		public TerminalNode CONTENT() { return getToken(BaseRuleParser.CONTENT, 0); }
		public DocumentOrContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_documentOrContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDocumentOrContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDocumentOrContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDocumentOrContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocumentOrContentContext documentOrContent() throws RecognitionException {
		DocumentOrContentContext _localctx = new DocumentOrContentContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_documentOrContent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1961);
			_la = _input.LA(1);
			if ( !(_la==DOCUMENT || _la==CONTENT) ) {
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

	public static class XmlWhitespaceOptionContext extends ParserRuleContext {
		public TerminalNode PRESERVE() { return getToken(BaseRuleParser.PRESERVE, 0); }
		public TerminalNode WHITESPACE() { return getToken(BaseRuleParser.WHITESPACE, 0); }
		public TerminalNode STRIP() { return getToken(BaseRuleParser.STRIP, 0); }
		public XmlWhitespaceOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlWhitespaceOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlWhitespaceOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlWhitespaceOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlWhitespaceOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlWhitespaceOptionContext xmlWhitespaceOption() throws RecognitionException {
		XmlWhitespaceOptionContext _localctx = new XmlWhitespaceOptionContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_xmlWhitespaceOption);
		try {
			setState(1968);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PRESERVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1963);
				match(PRESERVE);
				setState(1964);
				match(WHITESPACE);
				}
				break;
			case STRIP:
				enterOuterAlt(_localctx, 2);
				{
				setState(1965);
				match(STRIP);
				setState(1966);
				match(WHITESPACE);
				}
				break;
			case RP_:
				enterOuterAlt(_localctx, 3);
				{
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

	public static class XmlRootVersionContext extends ParserRuleContext {
		public TerminalNode VERSION() { return getToken(BaseRuleParser.VERSION, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode VALUE() { return getToken(BaseRuleParser.VALUE, 0); }
		public XmlRootVersionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlRootVersion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlRootVersion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlRootVersion(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlRootVersion(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlRootVersionContext xmlRootVersion() throws RecognitionException {
		XmlRootVersionContext _localctx = new XmlRootVersionContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_xmlRootVersion);
		try {
			setState(1975);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1970);
				match(VERSION);
				setState(1971);
				aExpr(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1972);
				match(VERSION);
				setState(1973);
				match(NO);
				setState(1974);
				match(VALUE);
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

	public static class XmlRootStandaloneContext extends ParserRuleContext {
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public TerminalNode STANDALONE() { return getToken(BaseRuleParser.STANDALONE, 0); }
		public TerminalNode YES() { return getToken(BaseRuleParser.YES, 0); }
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode VALUE() { return getToken(BaseRuleParser.VALUE, 0); }
		public XmlRootStandaloneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xmlRootStandalone; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterXmlRootStandalone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitXmlRootStandalone(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitXmlRootStandalone(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XmlRootStandaloneContext xmlRootStandalone() throws RecognitionException {
		XmlRootStandaloneContext _localctx = new XmlRootStandaloneContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_xmlRootStandalone);
		try {
			setState(1987);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1977);
				match(COMMA_);
				setState(1978);
				match(STANDALONE);
				setState(1979);
				match(YES);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1980);
				match(COMMA_);
				setState(1981);
				match(STANDALONE);
				setState(1982);
				match(NO);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1983);
				match(COMMA_);
				setState(1984);
				match(STANDALONE);
				setState(1985);
				match(NO);
				setState(1986);
				match(VALUE);
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

	public static class RowsFromItemContext extends ParserRuleContext {
		public FunctionExprWindowlessContext functionExprWindowless() {
			return getRuleContext(FunctionExprWindowlessContext.class,0);
		}
		public ColumnDefListContext columnDefList() {
			return getRuleContext(ColumnDefListContext.class,0);
		}
		public RowsFromItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rowsFromItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRowsFromItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRowsFromItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRowsFromItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowsFromItemContext rowsFromItem() throws RecognitionException {
		RowsFromItemContext _localctx = new RowsFromItemContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_rowsFromItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1989);
			functionExprWindowless();
			setState(1990);
			columnDefList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RowsFromListContext extends ParserRuleContext {
		public List<RowsFromItemContext> rowsFromItem() {
			return getRuleContexts(RowsFromItemContext.class);
		}
		public RowsFromItemContext rowsFromItem(int i) {
			return getRuleContext(RowsFromItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public RowsFromListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rowsFromList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRowsFromList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRowsFromList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRowsFromList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowsFromListContext rowsFromList() throws RecognitionException {
		RowsFromListContext _localctx = new RowsFromListContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_rowsFromList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1992);
			rowsFromItem();
			setState(1997);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1993);
				match(COMMA_);
				setState(1994);
				rowsFromItem();
				}
				}
				setState(1999);
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

	public static class ColumnDefListContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TableFuncElementListContext tableFuncElementList() {
			return getRuleContext(TableFuncElementListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ColumnDefListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnDefList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnDefList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnDefList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnDefList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnDefListContext columnDefList() throws RecognitionException {
		ColumnDefListContext _localctx = new ColumnDefListContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_columnDefList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2000);
			match(AS);
			setState(2001);
			match(LP_);
			setState(2002);
			tableFuncElementList();
			setState(2003);
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

	public static class TableFuncElementListContext extends ParserRuleContext {
		public List<TableFuncElementContext> tableFuncElement() {
			return getRuleContexts(TableFuncElementContext.class);
		}
		public TableFuncElementContext tableFuncElement(int i) {
			return getRuleContext(TableFuncElementContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TableFuncElementListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableFuncElementList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTableFuncElementList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTableFuncElementList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTableFuncElementList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableFuncElementListContext tableFuncElementList() throws RecognitionException {
		TableFuncElementListContext _localctx = new TableFuncElementListContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_tableFuncElementList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2005);
			tableFuncElement();
			setState(2010);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2006);
				match(COMMA_);
				setState(2007);
				tableFuncElement();
				}
				}
				setState(2012);
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

	public static class TableFuncElementContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public CollateClauseContext collateClause() {
			return getRuleContext(CollateClauseContext.class,0);
		}
		public TableFuncElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableFuncElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTableFuncElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTableFuncElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTableFuncElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableFuncElementContext tableFuncElement() throws RecognitionException {
		TableFuncElementContext _localctx = new TableFuncElementContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_tableFuncElement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2013);
			colId();
			setState(2014);
			typeName();
			setState(2016);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLLATE) {
				{
				setState(2015);
				collateClause();
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

	public static class CollateClauseContext extends ParserRuleContext {
		public TerminalNode COLLATE() { return getToken(BaseRuleParser.COLLATE, 0); }
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public CollateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCollateClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCollateClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCollateClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CollateClauseContext collateClause() throws RecognitionException {
		CollateClauseContext _localctx = new CollateClauseContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_collateClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2018);
			match(COLLATE);
			setState(2020);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_) {
				{
				setState(2019);
				match(EQ_);
				}
			}

			setState(2022);
			anyName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyNameContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public AttrsContext attrs() {
			return getRuleContext(AttrsContext.class,0);
		}
		public AnyNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAnyName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAnyName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAnyName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyNameContext anyName() throws RecognitionException {
		AnyNameContext _localctx = new AnyNameContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_anyName);
		try {
			setState(2028);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2024);
				colId();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2025);
				colId();
				setState(2026);
				attrs(0);
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

	public static class AliasClauseContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public NameListContext nameList() {
			return getRuleContext(NameListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public AliasClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aliasClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAliasClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAliasClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAliasClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasClauseContext aliasClause() throws RecognitionException {
		AliasClauseContext _localctx = new AliasClauseContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_aliasClause);
		try {
			setState(2044);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2030);
				match(AS);
				setState(2031);
				colId();
				setState(2032);
				match(LP_);
				setState(2033);
				nameList(0);
				setState(2034);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2036);
				match(AS);
				setState(2037);
				colId();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2038);
				colId();
				setState(2039);
				match(LP_);
				setState(2040);
				nameList(0);
				setState(2041);
				match(RP_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2043);
				colId();
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

	public static class NameListContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public NameListContext nameList() {
			return getRuleContext(NameListContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public NameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameListContext nameList() throws RecognitionException {
		return nameList(0);
	}

	private NameListContext nameList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		NameListContext _localctx = new NameListContext(_ctx, _parentState);
		NameListContext _prevctx = _localctx;
		int _startState = 262;
		enterRecursionRule(_localctx, 262, RULE_nameList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(2047);
			name();
			}
			_ctx.stop = _input.LT(-1);
			setState(2054);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new NameListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_nameList);
					setState(2049);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(2050);
					match(COMMA_);
					setState(2051);
					name();
					}
					} 
				}
				setState(2056);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
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

	public static class FuncAliasClauseContext extends ParserRuleContext {
		public AliasClauseContext aliasClause() {
			return getRuleContext(AliasClauseContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TableFuncElementListContext tableFuncElementList() {
			return getRuleContext(TableFuncElementListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public FuncAliasClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcAliasClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncAliasClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncAliasClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncAliasClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncAliasClauseContext funcAliasClause() throws RecognitionException {
		FuncAliasClauseContext _localctx = new FuncAliasClauseContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_funcAliasClause);
		try {
			setState(2074);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2057);
				aliasClause();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2058);
				match(AS);
				setState(2059);
				match(LP_);
				setState(2060);
				tableFuncElementList();
				setState(2061);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2063);
				match(AS);
				setState(2064);
				colId();
				setState(2065);
				match(LP_);
				setState(2066);
				tableFuncElementList();
				setState(2067);
				match(RP_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2069);
				colId();
				setState(2070);
				match(LP_);
				setState(2071);
				tableFuncElementList();
				setState(2072);
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

	public static class TablesampleClauseContext extends ParserRuleContext {
		public TerminalNode TABLESAMPLE() { return getToken(BaseRuleParser.TABLESAMPLE, 0); }
		public FuncNameContext funcName() {
			return getRuleContext(FuncNameContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public RepeatableClauseContext repeatableClause() {
			return getRuleContext(RepeatableClauseContext.class,0);
		}
		public TablesampleClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablesampleClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTablesampleClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTablesampleClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTablesampleClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablesampleClauseContext tablesampleClause() throws RecognitionException {
		TablesampleClauseContext _localctx = new TablesampleClauseContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_tablesampleClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2076);
			match(TABLESAMPLE);
			setState(2077);
			funcName();
			setState(2078);
			match(LP_);
			setState(2079);
			exprList(0);
			setState(2080);
			match(RP_);
			setState(2082);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REPEATABLE) {
				{
				setState(2081);
				repeatableClause();
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

	public static class RepeatableClauseContext extends ParserRuleContext {
		public TerminalNode REPEATABLE() { return getToken(BaseRuleParser.REPEATABLE, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public RepeatableClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repeatableClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRepeatableClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRepeatableClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRepeatableClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RepeatableClauseContext repeatableClause() throws RecognitionException {
		RepeatableClauseContext _localctx = new RepeatableClauseContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_repeatableClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2084);
			match(REPEATABLE);
			setState(2085);
			match(LP_);
			setState(2086);
			aExpr(0);
			setState(2087);
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

	public static class AllOrDistinctContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public AllOrDistinctContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allOrDistinct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterAllOrDistinct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitAllOrDistinct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitAllOrDistinct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllOrDistinctContext allOrDistinct() throws RecognitionException {
		AllOrDistinctContext _localctx = new AllOrDistinctContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_allOrDistinct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2089);
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

	public static class SortClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(BaseRuleParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public SortbyListContext sortbyList() {
			return getRuleContext(SortbyListContext.class,0);
		}
		public SortClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSortClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSortClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSortClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortClauseContext sortClause() throws RecognitionException {
		SortClauseContext _localctx = new SortClauseContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_sortClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2091);
			match(ORDER);
			setState(2092);
			match(BY);
			setState(2093);
			sortbyList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SortbyListContext extends ParserRuleContext {
		public List<SortbyContext> sortby() {
			return getRuleContexts(SortbyContext.class);
		}
		public SortbyContext sortby(int i) {
			return getRuleContext(SortbyContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public SortbyListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortbyList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSortbyList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSortbyList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSortbyList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortbyListContext sortbyList() throws RecognitionException {
		SortbyListContext _localctx = new SortbyListContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_sortbyList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2095);
			sortby();
			setState(2100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2096);
				match(COMMA_);
				setState(2097);
				sortby();
				}
				}
				setState(2102);
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

	public static class SortbyContext extends ParserRuleContext {
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode USING() { return getToken(BaseRuleParser.USING, 0); }
		public QualAllOpContext qualAllOp() {
			return getRuleContext(QualAllOpContext.class,0);
		}
		public NullsOrderContext nullsOrder() {
			return getRuleContext(NullsOrderContext.class,0);
		}
		public AscDescContext ascDesc() {
			return getRuleContext(AscDescContext.class,0);
		}
		public SortbyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortby; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSortby(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSortby(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSortby(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortbyContext sortby() throws RecognitionException {
		SortbyContext _localctx = new SortbyContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_sortby);
		int _la;
		try {
			setState(2116);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2103);
				aExpr(0);
				setState(2104);
				match(USING);
				setState(2105);
				qualAllOp();
				setState(2107);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLS) {
					{
					setState(2106);
					nullsOrder();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2109);
				aExpr(0);
				setState(2111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ASC || _la==DESC) {
					{
					setState(2110);
					ascDesc();
					}
				}

				setState(2114);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLS) {
					{
					setState(2113);
					nullsOrder();
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

	public static class NullsOrderContext extends ParserRuleContext {
		public TerminalNode NULLS() { return getToken(BaseRuleParser.NULLS, 0); }
		public TerminalNode FIRST() { return getToken(BaseRuleParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(BaseRuleParser.LAST, 0); }
		public NullsOrderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullsOrder; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNullsOrder(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNullsOrder(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNullsOrder(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullsOrderContext nullsOrder() throws RecognitionException {
		NullsOrderContext _localctx = new NullsOrderContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_nullsOrder);
		try {
			setState(2122);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2118);
				match(NULLS);
				setState(2119);
				match(FIRST);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2120);
				match(NULLS);
				setState(2121);
				match(LAST);
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

	public static class DistinctClauseContext extends ParserRuleContext {
		public TerminalNode DISTINCT() { return getToken(BaseRuleParser.DISTINCT, 0); }
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public DistinctClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distinctClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDistinctClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDistinctClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDistinctClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistinctClauseContext distinctClause() throws RecognitionException {
		DistinctClauseContext _localctx = new DistinctClauseContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_distinctClause);
		try {
			setState(2131);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2124);
				match(DISTINCT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2125);
				match(DISTINCT);
				setState(2126);
				match(ON);
				setState(2127);
				match(LP_);
				setState(2128);
				exprList(0);
				setState(2129);
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
		enterRule(_localctx, 282, RULE_distinct);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2133);
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

	public static class OverClauseContext extends ParserRuleContext {
		public TerminalNode OVER() { return getToken(BaseRuleParser.OVER, 0); }
		public WindowSpecificationContext windowSpecification() {
			return getRuleContext(WindowSpecificationContext.class,0);
		}
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
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
		enterRule(_localctx, 284, RULE_overClause);
		try {
			setState(2139);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,143,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2135);
				match(OVER);
				setState(2136);
				windowSpecification();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2137);
				match(OVER);
				setState(2138);
				colId();
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

	public static class WindowSpecificationContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public WindowNameContext windowName() {
			return getRuleContext(WindowNameContext.class,0);
		}
		public PartitionClauseContext partitionClause() {
			return getRuleContext(PartitionClauseContext.class,0);
		}
		public SortClauseContext sortClause() {
			return getRuleContext(SortClauseContext.class,0);
		}
		public FrameClauseContext frameClause() {
			return getRuleContext(FrameClauseContext.class,0);
		}
		public WindowSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowSpecification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowSpecification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowSpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowSpecificationContext windowSpecification() throws RecognitionException {
		WindowSpecificationContext _localctx = new WindowSpecificationContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_windowSpecification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2141);
			match(LP_);
			setState(2143);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
			case 1:
				{
				setState(2142);
				windowName();
				}
				break;
			}
			setState(2146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(2145);
				partitionClause();
				}
			}

			setState(2149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(2148);
				sortClause();
				}
			}

			setState(2152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROWS || _la==RANGE || _la==GROUPS) {
				{
				setState(2151);
				frameClause();
				}
			}

			setState(2154);
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

	public static class WindowNameContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public WindowNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterWindowName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitWindowName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitWindowName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowNameContext windowName() throws RecognitionException {
		WindowNameContext _localctx = new WindowNameContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_windowName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2156);
			colId();
			}
		}
		catch (RecognitionException re) {
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
		public TerminalNode PARTITION() { return getToken(BaseRuleParser.PARTITION, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public PartitionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterPartitionClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitPartitionClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitPartitionClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionClauseContext partitionClause() throws RecognitionException {
		PartitionClauseContext _localctx = new PartitionClauseContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_partitionClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2158);
			match(PARTITION);
			setState(2159);
			match(BY);
			setState(2160);
			exprList(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexParamsContext extends ParserRuleContext {
		public List<IndexElemContext> indexElem() {
			return getRuleContexts(IndexElemContext.class);
		}
		public IndexElemContext indexElem(int i) {
			return getRuleContext(IndexElemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public IndexParamsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexParams; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndexParams(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndexParams(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndexParams(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexParamsContext indexParams() throws RecognitionException {
		IndexParamsContext _localctx = new IndexParamsContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_indexParams);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2162);
			indexElem();
			setState(2167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2163);
				match(COMMA_);
				setState(2164);
				indexElem();
				}
				}
				setState(2169);
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

	public static class IndexElemOptionsContext extends ParserRuleContext {
		public OptClassContext optClass() {
			return getRuleContext(OptClassContext.class,0);
		}
		public CollateContext collate() {
			return getRuleContext(CollateContext.class,0);
		}
		public AscDescContext ascDesc() {
			return getRuleContext(AscDescContext.class,0);
		}
		public NullsOrderContext nullsOrder() {
			return getRuleContext(NullsOrderContext.class,0);
		}
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public ReloptionsContext reloptions() {
			return getRuleContext(ReloptionsContext.class,0);
		}
		public IndexElemOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexElemOptions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndexElemOptions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndexElemOptions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndexElemOptions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexElemOptionsContext indexElemOptions() throws RecognitionException {
		IndexElemOptionsContext _localctx = new IndexElemOptionsContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_indexElemOptions);
		int _la;
		try {
			setState(2191);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2171);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(2170);
					collate();
					}
				}

				setState(2173);
				optClass();
				setState(2175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ASC || _la==DESC) {
					{
					setState(2174);
					ascDesc();
					}
				}

				setState(2178);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLS) {
					{
					setState(2177);
					nullsOrder();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2181);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(2180);
					collate();
					}
				}

				setState(2183);
				anyName();
				setState(2184);
				reloptions();
				setState(2186);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ASC || _la==DESC) {
					{
					setState(2185);
					ascDesc();
					}
				}

				setState(2189);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLS) {
					{
					setState(2188);
					nullsOrder();
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

	public static class IndexElemContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public IndexElemOptionsContext indexElemOptions() {
			return getRuleContext(IndexElemOptionsContext.class,0);
		}
		public FunctionExprWindowlessContext functionExprWindowless() {
			return getRuleContext(FunctionExprWindowlessContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public IndexElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIndexElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIndexElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIndexElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexElemContext indexElem() throws RecognitionException {
		IndexElemContext _localctx = new IndexElemContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_indexElem);
		try {
			setState(2204);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2193);
				colId();
				setState(2194);
				indexElemOptions();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2196);
				functionExprWindowless();
				setState(2197);
				indexElemOptions();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2199);
				match(LP_);
				setState(2200);
				aExpr(0);
				setState(2201);
				match(RP_);
				setState(2202);
				indexElemOptions();
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

	public static class CollateContext extends ParserRuleContext {
		public TerminalNode COLLATE() { return getToken(BaseRuleParser.COLLATE, 0); }
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public CollateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCollate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCollate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCollate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CollateContext collate() throws RecognitionException {
		CollateContext _localctx = new CollateContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_collate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2206);
			match(COLLATE);
			setState(2207);
			anyName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OptClassContext extends ParserRuleContext {
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public OptClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOptClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOptClass(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOptClass(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptClassContext optClass() throws RecognitionException {
		OptClassContext _localctx = new OptClassContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_optClass);
		try {
			setState(2211);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2209);
				anyName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
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

	public static class ReloptionsContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ReloptionListContext reloptionList() {
			return getRuleContext(ReloptionListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ReloptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reloptions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterReloptions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitReloptions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitReloptions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReloptionsContext reloptions() throws RecognitionException {
		ReloptionsContext _localctx = new ReloptionsContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_reloptions);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2213);
			match(LP_);
			setState(2214);
			reloptionList();
			setState(2215);
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

	public static class ReloptionListContext extends ParserRuleContext {
		public List<ReloptionElemContext> reloptionElem() {
			return getRuleContexts(ReloptionElemContext.class);
		}
		public ReloptionElemContext reloptionElem(int i) {
			return getRuleContext(ReloptionElemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public ReloptionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reloptionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterReloptionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitReloptionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitReloptionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReloptionListContext reloptionList() throws RecognitionException {
		ReloptionListContext _localctx = new ReloptionListContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_reloptionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2217);
			reloptionElem();
			setState(2222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2218);
				match(COMMA_);
				setState(2219);
				reloptionElem();
				}
				}
				setState(2224);
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

	public static class ReloptionElemContext extends ParserRuleContext {
		public List<AliasContext> alias() {
			return getRuleContexts(AliasContext.class);
		}
		public AliasContext alias(int i) {
			return getRuleContext(AliasContext.class,i);
		}
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public DefArgContext defArg() {
			return getRuleContext(DefArgContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public ReloptionElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reloptionElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterReloptionElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitReloptionElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitReloptionElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReloptionElemContext reloptionElem() throws RecognitionException {
		ReloptionElemContext _localctx = new ReloptionElemContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_reloptionElem);
		try {
			setState(2240);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2225);
				alias();
				setState(2226);
				match(EQ_);
				setState(2227);
				defArg();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2229);
				alias();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2230);
				alias();
				setState(2231);
				match(DOT_);
				setState(2232);
				alias();
				setState(2233);
				match(EQ_);
				setState(2234);
				defArg();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2236);
				alias();
				setState(2237);
				match(DOT_);
				setState(2238);
				alias();
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

	public static class DefArgContext extends ParserRuleContext {
		public FuncTypeContext funcType() {
			return getRuleContext(FuncTypeContext.class,0);
		}
		public ReservedKeywordContext reservedKeyword() {
			return getRuleContext(ReservedKeywordContext.class,0);
		}
		public QualAllOpContext qualAllOp() {
			return getRuleContext(QualAllOpContext.class,0);
		}
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public TerminalNode NONE() { return getToken(BaseRuleParser.NONE, 0); }
		public DefArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDefArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDefArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDefArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefArgContext defArg() throws RecognitionException {
		DefArgContext _localctx = new DefArgContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_defArg);
		try {
			setState(2248);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2242);
				funcType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2243);
				reservedKeyword();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2244);
				qualAllOp();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2245);
				match(NUMBER_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2246);
				match(STRING_);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2247);
				match(NONE);
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

	public static class FuncTypeContext extends ParserRuleContext {
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TypeFunctionNameContext typeFunctionName() {
			return getRuleContext(TypeFunctionNameContext.class,0);
		}
		public AttrsContext attrs() {
			return getRuleContext(AttrsContext.class,0);
		}
		public TerminalNode MOD_() { return getToken(BaseRuleParser.MOD_, 0); }
		public TerminalNode TYPE() { return getToken(BaseRuleParser.TYPE, 0); }
		public TerminalNode SETOF() { return getToken(BaseRuleParser.SETOF, 0); }
		public FuncTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncTypeContext funcType() throws RecognitionException {
		FuncTypeContext _localctx = new FuncTypeContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_funcType);
		try {
			setState(2262);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2250);
				typeName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2251);
				typeFunctionName();
				setState(2252);
				attrs(0);
				setState(2253);
				match(MOD_);
				setState(2254);
				match(TYPE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2256);
				match(SETOF);
				setState(2257);
				typeFunctionName();
				setState(2258);
				attrs(0);
				setState(2259);
				match(MOD_);
				setState(2260);
				match(TYPE);
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

	public static class SelectWithParensContext extends ParserRuleContext {
		public SelectWithParensContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectWithParens; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSelectWithParens(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSelectWithParens(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSelectWithParens(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectWithParensContext selectWithParens() throws RecognitionException {
		SelectWithParensContext _localctx = new SelectWithParensContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_selectWithParens);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2264);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
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
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<TerminalNode> STRING_() { return getTokens(BaseRuleParser.STRING_); }
		public TerminalNode STRING_(int i) {
			return getToken(BaseRuleParser.STRING_, i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
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
		enterRule(_localctx, 314, RULE_dataType);
		int _la;
		try {
			setState(2293);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2266);
				dataTypeName();
				setState(2268);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(2267);
					dataTypeLength();
					}
				}

				setState(2271);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHAR || _la==CHARACTER) {
					{
					setState(2270);
					characterSet();
					}
				}

				setState(2274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(2273);
					collateClause();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2276);
				dataTypeName();
				setState(2277);
				match(LP_);
				setState(2278);
				match(STRING_);
				setState(2283);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(2279);
					match(COMMA_);
					setState(2280);
					match(STRING_);
					}
					}
					setState(2285);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(2286);
				match(RP_);
				setState(2288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHAR || _la==CHARACTER) {
					{
					setState(2287);
					characterSet();
					}
				}

				setState(2291);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(2290);
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
		public TerminalNode INT() { return getToken(BaseRuleParser.INT, 0); }
		public TerminalNode INT2() { return getToken(BaseRuleParser.INT2, 0); }
		public TerminalNode INT4() { return getToken(BaseRuleParser.INT4, 0); }
		public TerminalNode INT8() { return getToken(BaseRuleParser.INT8, 0); }
		public TerminalNode SMALLINT() { return getToken(BaseRuleParser.SMALLINT, 0); }
		public TerminalNode INTEGER() { return getToken(BaseRuleParser.INTEGER, 0); }
		public TerminalNode BIGINT() { return getToken(BaseRuleParser.BIGINT, 0); }
		public TerminalNode DECIMAL() { return getToken(BaseRuleParser.DECIMAL, 0); }
		public TerminalNode NUMERIC() { return getToken(BaseRuleParser.NUMERIC, 0); }
		public TerminalNode REAL() { return getToken(BaseRuleParser.REAL, 0); }
		public TerminalNode FLOAT() { return getToken(BaseRuleParser.FLOAT, 0); }
		public TerminalNode FLOAT4() { return getToken(BaseRuleParser.FLOAT4, 0); }
		public TerminalNode FLOAT8() { return getToken(BaseRuleParser.FLOAT8, 0); }
		public TerminalNode DOUBLE() { return getToken(BaseRuleParser.DOUBLE, 0); }
		public TerminalNode PRECISION() { return getToken(BaseRuleParser.PRECISION, 0); }
		public TerminalNode SMALLSERIAL() { return getToken(BaseRuleParser.SMALLSERIAL, 0); }
		public TerminalNode SERIAL() { return getToken(BaseRuleParser.SERIAL, 0); }
		public TerminalNode BIGSERIAL() { return getToken(BaseRuleParser.BIGSERIAL, 0); }
		public TerminalNode MONEY() { return getToken(BaseRuleParser.MONEY, 0); }
		public TerminalNode VARCHAR() { return getToken(BaseRuleParser.VARCHAR, 0); }
		public TerminalNode CHARACTER() { return getToken(BaseRuleParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode TEXT() { return getToken(BaseRuleParser.TEXT, 0); }
		public TerminalNode NAME() { return getToken(BaseRuleParser.NAME, 0); }
		public TerminalNode BYTEA() { return getToken(BaseRuleParser.BYTEA, 0); }
		public TerminalNode TIMESTAMP() { return getToken(BaseRuleParser.TIMESTAMP, 0); }
		public TerminalNode DATE() { return getToken(BaseRuleParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public TerminalNode BOOLEAN() { return getToken(BaseRuleParser.BOOLEAN, 0); }
		public TerminalNode ENUM() { return getToken(BaseRuleParser.ENUM, 0); }
		public TerminalNode POINT() { return getToken(BaseRuleParser.POINT, 0); }
		public TerminalNode LINE() { return getToken(BaseRuleParser.LINE, 0); }
		public TerminalNode LSEG() { return getToken(BaseRuleParser.LSEG, 0); }
		public TerminalNode BOX() { return getToken(BaseRuleParser.BOX, 0); }
		public TerminalNode PATH() { return getToken(BaseRuleParser.PATH, 0); }
		public TerminalNode POLYGON() { return getToken(BaseRuleParser.POLYGON, 0); }
		public TerminalNode CIRCLE() { return getToken(BaseRuleParser.CIRCLE, 0); }
		public TerminalNode CIDR() { return getToken(BaseRuleParser.CIDR, 0); }
		public TerminalNode INET() { return getToken(BaseRuleParser.INET, 0); }
		public TerminalNode MACADDR() { return getToken(BaseRuleParser.MACADDR, 0); }
		public TerminalNode MACADDR8() { return getToken(BaseRuleParser.MACADDR8, 0); }
		public TerminalNode BIT() { return getToken(BaseRuleParser.BIT, 0); }
		public TerminalNode VARBIT() { return getToken(BaseRuleParser.VARBIT, 0); }
		public TerminalNode TSVECTOR() { return getToken(BaseRuleParser.TSVECTOR, 0); }
		public TerminalNode TSQUERY() { return getToken(BaseRuleParser.TSQUERY, 0); }
		public TerminalNode XML() { return getToken(BaseRuleParser.XML, 0); }
		public TerminalNode JSON() { return getToken(BaseRuleParser.JSON, 0); }
		public TerminalNode INT4RANGE() { return getToken(BaseRuleParser.INT4RANGE, 0); }
		public TerminalNode INT8RANGE() { return getToken(BaseRuleParser.INT8RANGE, 0); }
		public TerminalNode NUMRANGE() { return getToken(BaseRuleParser.NUMRANGE, 0); }
		public TerminalNode TSRANGE() { return getToken(BaseRuleParser.TSRANGE, 0); }
		public TerminalNode TSTZRANGE() { return getToken(BaseRuleParser.TSTZRANGE, 0); }
		public TerminalNode DATERANGE() { return getToken(BaseRuleParser.DATERANGE, 0); }
		public TerminalNode ARRAY() { return getToken(BaseRuleParser.ARRAY, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ConstDatetimeContext constDatetime() {
			return getRuleContext(ConstDatetimeContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
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
		enterRule(_localctx, 316, RULE_dataTypeName);
		try {
			setState(2353);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2295);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2296);
				match(INT2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2297);
				match(INT4);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2298);
				match(INT8);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2299);
				match(SMALLINT);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2300);
				match(INTEGER);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2301);
				match(BIGINT);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2302);
				match(DECIMAL);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2303);
				match(NUMERIC);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2304);
				match(REAL);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2305);
				match(FLOAT);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2306);
				match(FLOAT4);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2307);
				match(FLOAT8);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(2308);
				match(DOUBLE);
				setState(2309);
				match(PRECISION);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(2310);
				match(SMALLSERIAL);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(2311);
				match(SERIAL);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(2312);
				match(BIGSERIAL);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(2313);
				match(MONEY);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(2314);
				match(VARCHAR);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(2315);
				match(CHARACTER);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(2316);
				match(CHAR);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(2317);
				match(TEXT);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(2318);
				match(NAME);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(2319);
				match(BYTEA);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(2320);
				match(TIMESTAMP);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(2321);
				match(DATE);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(2322);
				match(TIME);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(2323);
				match(INTERVAL);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(2324);
				match(BOOLEAN);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(2325);
				match(ENUM);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(2326);
				match(POINT);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(2327);
				match(LINE);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(2328);
				match(LSEG);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(2329);
				match(BOX);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(2330);
				match(PATH);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(2331);
				match(POLYGON);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(2332);
				match(CIRCLE);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(2333);
				match(CIDR);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(2334);
				match(INET);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(2335);
				match(MACADDR);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(2336);
				match(MACADDR8);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(2337);
				match(BIT);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(2338);
				match(VARBIT);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(2339);
				match(TSVECTOR);
				}
				break;
			case 45:
				enterOuterAlt(_localctx, 45);
				{
				setState(2340);
				match(TSQUERY);
				}
				break;
			case 46:
				enterOuterAlt(_localctx, 46);
				{
				setState(2341);
				match(XML);
				}
				break;
			case 47:
				enterOuterAlt(_localctx, 47);
				{
				setState(2342);
				match(JSON);
				}
				break;
			case 48:
				enterOuterAlt(_localctx, 48);
				{
				setState(2343);
				match(INT4RANGE);
				}
				break;
			case 49:
				enterOuterAlt(_localctx, 49);
				{
				setState(2344);
				match(INT8RANGE);
				}
				break;
			case 50:
				enterOuterAlt(_localctx, 50);
				{
				setState(2345);
				match(NUMRANGE);
				}
				break;
			case 51:
				enterOuterAlt(_localctx, 51);
				{
				setState(2346);
				match(TSRANGE);
				}
				break;
			case 52:
				enterOuterAlt(_localctx, 52);
				{
				setState(2347);
				match(TSTZRANGE);
				}
				break;
			case 53:
				enterOuterAlt(_localctx, 53);
				{
				setState(2348);
				match(DATERANGE);
				}
				break;
			case 54:
				enterOuterAlt(_localctx, 54);
				{
				setState(2349);
				match(ARRAY);
				}
				break;
			case 55:
				enterOuterAlt(_localctx, 55);
				{
				setState(2350);
				identifier();
				}
				break;
			case 56:
				enterOuterAlt(_localctx, 56);
				{
				setState(2351);
				constDatetime();
				}
				break;
			case 57:
				enterOuterAlt(_localctx, 57);
				{
				setState(2352);
				typeName();
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
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(BaseRuleParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(BaseRuleParser.NUMBER_, i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
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
		enterRule(_localctx, 318, RULE_dataTypeLength);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2355);
			match(LP_);
			setState(2356);
			match(NUMBER_);
			setState(2359);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA_) {
				{
				setState(2357);
				match(COMMA_);
				setState(2358);
				match(NUMBER_);
				}
			}

			setState(2361);
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
		public TerminalNode SET() { return getToken(BaseRuleParser.SET, 0); }
		public IgnoredIdentifierContext ignoredIdentifier() {
			return getRuleContext(IgnoredIdentifierContext.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(BaseRuleParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public CharacterSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCharacterSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCharacterSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCharacterSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterSetContext characterSet() throws RecognitionException {
		CharacterSetContext _localctx = new CharacterSetContext(_ctx, getState());
		enterRule(_localctx, 320, RULE_characterSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2363);
			_la = _input.LA(1);
			if ( !(_la==CHAR || _la==CHARACTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(2364);
			match(SET);
			setState(2366);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_) {
				{
				setState(2365);
				match(EQ_);
				}
			}

			setState(2368);
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

	public static class IgnoredIdentifierContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
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
		enterRule(_localctx, 322, RULE_ignoredIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2370);
			identifier();
			setState(2373);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT_) {
				{
				setState(2371);
				match(DOT_);
				setState(2372);
				identifier();
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
		enterRule(_localctx, 324, RULE_ignoredIdentifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2375);
			ignoredIdentifier();
			setState(2380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2376);
				match(COMMA_);
				setState(2377);
				ignoredIdentifier();
				}
				}
				setState(2382);
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

	public static class SignedIconstContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public SignedIconstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signedIconst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSignedIconst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSignedIconst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSignedIconst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignedIconstContext signedIconst() throws RecognitionException {
		SignedIconstContext _localctx = new SignedIconstContext(_ctx, getState());
		enterRule(_localctx, 326, RULE_signedIconst);
		try {
			setState(2388);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(2383);
				match(NUMBER_);
				}
				break;
			case PLUS_:
				enterOuterAlt(_localctx, 2);
				{
				setState(2384);
				match(PLUS_);
				setState(2385);
				match(NUMBER_);
				}
				break;
			case MINUS_:
				enterOuterAlt(_localctx, 3);
				{
				setState(2386);
				match(MINUS_);
				setState(2387);
				match(NUMBER_);
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

	public static class BooleanOrStringContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(BaseRuleParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(BaseRuleParser.FALSE, 0); }
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public NonReservedWordContext nonReservedWord() {
			return getRuleContext(NonReservedWordContext.class,0);
		}
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public BooleanOrStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanOrString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterBooleanOrString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitBooleanOrString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitBooleanOrString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanOrStringContext booleanOrString() throws RecognitionException {
		BooleanOrStringContext _localctx = new BooleanOrStringContext(_ctx, getState());
		enterRule(_localctx, 328, RULE_booleanOrString);
		try {
			setState(2395);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(2390);
				match(TRUE);
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(2391);
				match(FALSE);
				}
				break;
			case ON:
				enterOuterAlt(_localctx, 3);
				{
				setState(2392);
				match(ON);
				}
				break;
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case POSITION:
			case PRECISION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case VALUES:
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
			case IF:
			case IS:
			case EXISTS:
			case BETWEEN:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHAR:
			case CHARACTER:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case INTEGER:
			case REAL:
			case DECIMAL:
			case TYPE:
			case SMALLINT:
			case BIGINT:
			case NUMERIC:
			case TEXT:
			case REPEATABLE:
			case NULLIF:
			case VARYING:
			case NATIONAL:
			case NCHAR:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case DEC:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case INT:
			case FLOAT:
			case VARCHAR:
			case ENUM:
			case BIT:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_SCHEMA:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case SETOF:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case XMLATTRIBUTES:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case XMLTABLE:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case XMLNAMESPACES:
			case NONE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case OUT:
			case INOUT:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 4);
				{
				setState(2393);
				nonReservedWord();
				}
				break;
			case STRING_:
				enterOuterAlt(_localctx, 5);
				{
				setState(2394);
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

	public static class NonReservedWordContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public UnreservedWordContext unreservedWord() {
			return getRuleContext(UnreservedWordContext.class,0);
		}
		public ColNameKeywordContext colNameKeyword() {
			return getRuleContext(ColNameKeywordContext.class,0);
		}
		public TypeFuncNameKeywordContext typeFuncNameKeyword() {
			return getRuleContext(TypeFuncNameKeywordContext.class,0);
		}
		public NonReservedWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonReservedWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNonReservedWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNonReservedWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNonReservedWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonReservedWordContext nonReservedWord() throws RecognitionException {
		NonReservedWordContext _localctx = new NonReservedWordContext(_ctx, getState());
		enterRule(_localctx, 330, RULE_nonReservedWord);
		try {
			setState(2401);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2397);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2398);
				unreservedWord();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2399);
				colNameKeyword();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2400);
				typeFuncNameKeyword();
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

	public static class ColNameKeywordContext extends ParserRuleContext {
		public TerminalNode BETWEEN() { return getToken(BaseRuleParser.BETWEEN, 0); }
		public TerminalNode BIGINT() { return getToken(BaseRuleParser.BIGINT, 0); }
		public TerminalNode BIT() { return getToken(BaseRuleParser.BIT, 0); }
		public TerminalNode BOOLEAN() { return getToken(BaseRuleParser.BOOLEAN, 0); }
		public TerminalNode CHAR() { return getToken(BaseRuleParser.CHAR, 0); }
		public TerminalNode CHARACTER() { return getToken(BaseRuleParser.CHARACTER, 0); }
		public TerminalNode COALESCE() { return getToken(BaseRuleParser.COALESCE, 0); }
		public TerminalNode DEC() { return getToken(BaseRuleParser.DEC, 0); }
		public TerminalNode DECIMAL() { return getToken(BaseRuleParser.DECIMAL, 0); }
		public TerminalNode EXISTS() { return getToken(BaseRuleParser.EXISTS, 0); }
		public TerminalNode EXTRACT() { return getToken(BaseRuleParser.EXTRACT, 0); }
		public TerminalNode FLOAT() { return getToken(BaseRuleParser.FLOAT, 0); }
		public TerminalNode GREATEST() { return getToken(BaseRuleParser.GREATEST, 0); }
		public TerminalNode GROUPING() { return getToken(BaseRuleParser.GROUPING, 0); }
		public TerminalNode INOUT() { return getToken(BaseRuleParser.INOUT, 0); }
		public TerminalNode INT() { return getToken(BaseRuleParser.INT, 0); }
		public TerminalNode INTEGER() { return getToken(BaseRuleParser.INTEGER, 0); }
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public TerminalNode LEAST() { return getToken(BaseRuleParser.LEAST, 0); }
		public TerminalNode NATIONAL() { return getToken(BaseRuleParser.NATIONAL, 0); }
		public TerminalNode NCHAR() { return getToken(BaseRuleParser.NCHAR, 0); }
		public TerminalNode NONE() { return getToken(BaseRuleParser.NONE, 0); }
		public TerminalNode NULLIF() { return getToken(BaseRuleParser.NULLIF, 0); }
		public TerminalNode NUMERIC() { return getToken(BaseRuleParser.NUMERIC, 0); }
		public TerminalNode OUT() { return getToken(BaseRuleParser.OUT, 0); }
		public TerminalNode OVERLAY() { return getToken(BaseRuleParser.OVERLAY, 0); }
		public TerminalNode POSITION() { return getToken(BaseRuleParser.POSITION, 0); }
		public TerminalNode PRECISION() { return getToken(BaseRuleParser.PRECISION, 0); }
		public TerminalNode REAL() { return getToken(BaseRuleParser.REAL, 0); }
		public TerminalNode ROW() { return getToken(BaseRuleParser.ROW, 0); }
		public TerminalNode SETOF() { return getToken(BaseRuleParser.SETOF, 0); }
		public TerminalNode SMALLINT() { return getToken(BaseRuleParser.SMALLINT, 0); }
		public TerminalNode SUBSTRING() { return getToken(BaseRuleParser.SUBSTRING, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(BaseRuleParser.TIMESTAMP, 0); }
		public TerminalNode TREAT() { return getToken(BaseRuleParser.TREAT, 0); }
		public TerminalNode TRIM() { return getToken(BaseRuleParser.TRIM, 0); }
		public TerminalNode VALUES() { return getToken(BaseRuleParser.VALUES, 0); }
		public TerminalNode VARCHAR() { return getToken(BaseRuleParser.VARCHAR, 0); }
		public TerminalNode XMLATTRIBUTES() { return getToken(BaseRuleParser.XMLATTRIBUTES, 0); }
		public TerminalNode XMLCONCAT() { return getToken(BaseRuleParser.XMLCONCAT, 0); }
		public TerminalNode XMLELEMENT() { return getToken(BaseRuleParser.XMLELEMENT, 0); }
		public TerminalNode XMLEXISTS() { return getToken(BaseRuleParser.XMLEXISTS, 0); }
		public TerminalNode XMLFOREST() { return getToken(BaseRuleParser.XMLFOREST, 0); }
		public TerminalNode XMLNAMESPACES() { return getToken(BaseRuleParser.XMLNAMESPACES, 0); }
		public TerminalNode XMLPARSE() { return getToken(BaseRuleParser.XMLPARSE, 0); }
		public TerminalNode XMLPI() { return getToken(BaseRuleParser.XMLPI, 0); }
		public TerminalNode XMLROOT() { return getToken(BaseRuleParser.XMLROOT, 0); }
		public TerminalNode XMLSERIALIZE() { return getToken(BaseRuleParser.XMLSERIALIZE, 0); }
		public TerminalNode XMLTABLE() { return getToken(BaseRuleParser.XMLTABLE, 0); }
		public ColNameKeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colNameKeyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColNameKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColNameKeyword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColNameKeyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColNameKeywordContext colNameKeyword() throws RecognitionException {
		ColNameKeywordContext _localctx = new ColNameKeywordContext(_ctx, getState());
		enterRule(_localctx, 332, RULE_colNameKeyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2403);
			_la = _input.LA(1);
			if ( !(((((_la - 26)) & ~0x3f) == 0 && ((1L << (_la - 26)) & ((1L << (POSITION - 26)) | (1L << (PRECISION - 26)) | (1L << (VALUES - 26)) | (1L << (TRIM - 26)) | (1L << (SUBSTRING - 26)) | (1L << (EXISTS - 26)) | (1L << (BETWEEN - 26)) | (1L << (BOOLEAN - 26)) | (1L << (CHAR - 26)) | (1L << (CHARACTER - 26)))) != 0) || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & ((1L << (INTERVAL - 90)) | (1L << (TIME - 90)) | (1L << (TIMESTAMP - 90)) | (1L << (INTEGER - 90)) | (1L << (REAL - 90)) | (1L << (DECIMAL - 90)) | (1L << (SMALLINT - 90)) | (1L << (BIGINT - 90)) | (1L << (NUMERIC - 90)) | (1L << (NULLIF - 90)) | (1L << (NATIONAL - 90)) | (1L << (NCHAR - 90)) | (1L << (COALESCE - 90)) | (1L << (GROUPING - 90)))) != 0) || ((((_la - 157)) & ~0x3f) == 0 && ((1L << (_la - 157)) & ((1L << (DEC - 157)) | (1L << (ROW - 157)) | (1L << (EXTRACT - 157)))) != 0) || ((((_la - 271)) & ~0x3f) == 0 && ((1L << (_la - 271)) & ((1L << (INT - 271)) | (1L << (FLOAT - 271)) | (1L << (VARCHAR - 271)) | (1L << (BIT - 271)) | (1L << (OVERLAY - 271)) | (1L << (XMLCONCAT - 271)) | (1L << (XMLELEMENT - 271)) | (1L << (XMLEXISTS - 271)) | (1L << (XMLFOREST - 271)) | (1L << (XMLPARSE - 271)) | (1L << (XMLPI - 271)) | (1L << (XMLROOT - 271)) | (1L << (XMLSERIALIZE - 271)) | (1L << (TREAT - 271)) | (1L << (SETOF - 271)) | (1L << (XMLATTRIBUTES - 271)))) != 0) || ((((_la - 335)) & ~0x3f) == 0 && ((1L << (_la - 335)) & ((1L << (GREATEST - 335)) | (1L << (LEAST - 335)) | (1L << (XMLTABLE - 335)) | (1L << (XMLNAMESPACES - 335)) | (1L << (NONE - 335)))) != 0) || _la==OUT || _la==INOUT) ) {
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

	public static class DatabaseNameContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
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
		enterRule(_localctx, 334, RULE_databaseName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2405);
			colId();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RoleSpecContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public NonReservedWordContext nonReservedWord() {
			return getRuleContext(NonReservedWordContext.class,0);
		}
		public TerminalNode CURRENT_USER() { return getToken(BaseRuleParser.CURRENT_USER, 0); }
		public TerminalNode SESSION_USER() { return getToken(BaseRuleParser.SESSION_USER, 0); }
		public RoleSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_roleSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRoleSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRoleSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRoleSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoleSpecContext roleSpec() throws RecognitionException {
		RoleSpecContext _localctx = new RoleSpecContext(_ctx, getState());
		enterRule(_localctx, 336, RULE_roleSpec);
		try {
			setState(2411);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2407);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2408);
				nonReservedWord();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2409);
				match(CURRENT_USER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2410);
				match(SESSION_USER);
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

	public static class VarNameContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public VarNameContext varName() {
			return getRuleContext(VarNameContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(BaseRuleParser.DOT_, 0); }
		public VarNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterVarName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitVarName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitVarName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarNameContext varName() throws RecognitionException {
		return varName(0);
	}

	private VarNameContext varName(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		VarNameContext _localctx = new VarNameContext(_ctx, _parentState);
		VarNameContext _prevctx = _localctx;
		int _startState = 338;
		enterRecursionRule(_localctx, 338, RULE_varName, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(2414);
			colId();
			}
			_ctx.stop = _input.LT(-1);
			setState(2421);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,178,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new VarNameContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_varName);
					setState(2416);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(2417);
					match(DOT_);
					setState(2418);
					colId();
					}
					} 
				}
				setState(2423);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,178,_ctx);
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

	public static class VarListContext extends ParserRuleContext {
		public List<VarValueContext> varValue() {
			return getRuleContexts(VarValueContext.class);
		}
		public VarValueContext varValue(int i) {
			return getRuleContext(VarValueContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public VarListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterVarList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitVarList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitVarList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarListContext varList() throws RecognitionException {
		VarListContext _localctx = new VarListContext(_ctx, getState());
		enterRule(_localctx, 340, RULE_varList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2424);
			varValue();
			setState(2429);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2425);
				match(COMMA_);
				setState(2426);
				varValue();
				}
				}
				setState(2431);
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

	public static class VarValueContext extends ParserRuleContext {
		public BooleanOrStringContext booleanOrString() {
			return getRuleContext(BooleanOrStringContext.class,0);
		}
		public NumericOnlyContext numericOnly() {
			return getRuleContext(NumericOnlyContext.class,0);
		}
		public VarValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterVarValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitVarValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitVarValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarValueContext varValue() throws RecognitionException {
		VarValueContext _localctx = new VarValueContext(_ctx, getState());
		enterRule(_localctx, 342, RULE_varValue);
		try {
			setState(2434);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case POSITION:
			case PRECISION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case VALUES:
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
			case ON:
			case IF:
			case IS:
			case TRUE:
			case FALSE:
			case EXISTS:
			case BETWEEN:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHAR:
			case CHARACTER:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case INTEGER:
			case REAL:
			case DECIMAL:
			case TYPE:
			case SMALLINT:
			case BIGINT:
			case NUMERIC:
			case TEXT:
			case REPEATABLE:
			case NULLIF:
			case VARYING:
			case NATIONAL:
			case NCHAR:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case DEC:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case INT:
			case FLOAT:
			case VARCHAR:
			case ENUM:
			case BIT:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_SCHEMA:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case SETOF:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case XMLATTRIBUTES:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case XMLTABLE:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case XMLNAMESPACES:
			case NONE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case OUT:
			case INOUT:
			case IDENTIFIER_:
			case STRING_:
				enterOuterAlt(_localctx, 1);
				{
				setState(2432);
				booleanOrString();
				}
				break;
			case PLUS_:
			case MINUS_:
			case NUMBER_:
				enterOuterAlt(_localctx, 2);
				{
				setState(2433);
				numericOnly();
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

	public static class ZoneValueContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode INTERVAL() { return getToken(BaseRuleParser.INTERVAL, 0); }
		public OptIntervalContext optInterval() {
			return getRuleContext(OptIntervalContext.class,0);
		}
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public NumericOnlyContext numericOnly() {
			return getRuleContext(NumericOnlyContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public TerminalNode LOCAL() { return getToken(BaseRuleParser.LOCAL, 0); }
		public ZoneValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_zoneValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterZoneValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitZoneValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitZoneValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ZoneValueContext zoneValue() throws RecognitionException {
		ZoneValueContext _localctx = new ZoneValueContext(_ctx, getState());
		enterRule(_localctx, 344, RULE_zoneValue);
		try {
			setState(2449);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2436);
				match(STRING_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2437);
				identifier();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2438);
				match(INTERVAL);
				setState(2439);
				match(STRING_);
				setState(2440);
				optInterval();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2441);
				match(INTERVAL);
				setState(2442);
				match(LP_);
				setState(2443);
				match(NUMBER_);
				setState(2444);
				match(RP_);
				setState(2445);
				match(STRING_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2446);
				numericOnly();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2447);
				match(DEFAULT);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2448);
				match(LOCAL);
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

	public static class NumericOnlyContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(BaseRuleParser.NUMBER_, 0); }
		public TerminalNode PLUS_() { return getToken(BaseRuleParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(BaseRuleParser.MINUS_, 0); }
		public NumericOnlyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericOnly; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNumericOnly(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNumericOnly(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNumericOnly(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericOnlyContext numericOnly() throws RecognitionException {
		NumericOnlyContext _localctx = new NumericOnlyContext(_ctx, getState());
		enterRule(_localctx, 346, RULE_numericOnly);
		try {
			setState(2456);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(2451);
				match(NUMBER_);
				}
				break;
			case PLUS_:
				enterOuterAlt(_localctx, 2);
				{
				setState(2452);
				match(PLUS_);
				setState(2453);
				match(NUMBER_);
				}
				break;
			case MINUS_:
				enterOuterAlt(_localctx, 3);
				{
				setState(2454);
				match(MINUS_);
				setState(2455);
				match(NUMBER_);
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

	public static class IsoLevelContext extends ParserRuleContext {
		public TerminalNode READ() { return getToken(BaseRuleParser.READ, 0); }
		public TerminalNode UNCOMMITTED() { return getToken(BaseRuleParser.UNCOMMITTED, 0); }
		public TerminalNode COMMITTED() { return getToken(BaseRuleParser.COMMITTED, 0); }
		public TerminalNode REPEATABLE() { return getToken(BaseRuleParser.REPEATABLE, 0); }
		public TerminalNode SERIALIZABLE() { return getToken(BaseRuleParser.SERIALIZABLE, 0); }
		public IsoLevelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isoLevel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterIsoLevel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitIsoLevel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitIsoLevel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IsoLevelContext isoLevel() throws RecognitionException {
		IsoLevelContext _localctx = new IsoLevelContext(_ctx, getState());
		enterRule(_localctx, 348, RULE_isoLevel);
		try {
			setState(2465);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2458);
				match(READ);
				setState(2459);
				match(UNCOMMITTED);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2460);
				match(READ);
				setState(2461);
				match(COMMITTED);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2462);
				match(REPEATABLE);
				setState(2463);
				match(READ);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2464);
				match(SERIALIZABLE);
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

	public static class ColumnDefContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public ColQualListContext colQualList() {
			return getRuleContext(ColQualListContext.class,0);
		}
		public CreateGenericOptionsContext createGenericOptions() {
			return getRuleContext(CreateGenericOptionsContext.class,0);
		}
		public ColumnDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnDefContext columnDef() throws RecognitionException {
		ColumnDefContext _localctx = new ColumnDefContext(_ctx, getState());
		enterRule(_localctx, 350, RULE_columnDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2467);
			colId();
			setState(2468);
			typeName();
			setState(2470);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPTIONS) {
				{
				setState(2469);
				createGenericOptions();
				}
			}

			setState(2472);
			colQualList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColQualListContext extends ParserRuleContext {
		public List<ColConstraintContext> colConstraint() {
			return getRuleContexts(ColConstraintContext.class);
		}
		public ColConstraintContext colConstraint(int i) {
			return getRuleContext(ColConstraintContext.class,i);
		}
		public ColQualListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colQualList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColQualList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColQualList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColQualList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColQualListContext colQualList() throws RecognitionException {
		ColQualListContext _localctx = new ColQualListContext(_ctx, getState());
		enterRule(_localctx, 352, RULE_colQualList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2477);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CONSTRAINT) | (1L << PRIMARY) | (1L << UNIQUE) | (1L << NOT))) != 0) || _la==NULL || _la==DEFAULT || ((((_la - 170)) & ~0x3f) == 0 && ((1L << (_la - 170)) & ((1L << (CHECK - 170)) | (1L << (GENERATED - 170)) | (1L << (REFERENCES - 170)) | (1L << (COLLATE - 170)) | (1L << (DEFERRABLE - 170)) | (1L << (INITIALLY - 170)))) != 0)) {
				{
				{
				setState(2474);
				colConstraint();
				}
				}
				setState(2479);
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

	public static class ColConstraintContext extends ParserRuleContext {
		public TerminalNode CONSTRAINT() { return getToken(BaseRuleParser.CONSTRAINT, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ColConstraintElemContext colConstraintElem() {
			return getRuleContext(ColConstraintElemContext.class,0);
		}
		public ConstraintAttrContext constraintAttr() {
			return getRuleContext(ConstraintAttrContext.class,0);
		}
		public TerminalNode COLLATE() { return getToken(BaseRuleParser.COLLATE, 0); }
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public ColConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colConstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColConstraintContext colConstraint() throws RecognitionException {
		ColConstraintContext _localctx = new ColConstraintContext(_ctx, getState());
		enterRule(_localctx, 354, RULE_colConstraint);
		try {
			setState(2488);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,186,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2480);
				match(CONSTRAINT);
				setState(2481);
				name();
				setState(2482);
				colConstraintElem();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2484);
				colConstraintElem();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2485);
				constraintAttr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2486);
				match(COLLATE);
				setState(2487);
				anyName();
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

	public static class ConstraintAttrContext extends ParserRuleContext {
		public TerminalNode DEFERRABLE() { return getToken(BaseRuleParser.DEFERRABLE, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode INITIALLY() { return getToken(BaseRuleParser.INITIALLY, 0); }
		public TerminalNode DEFERRED() { return getToken(BaseRuleParser.DEFERRED, 0); }
		public TerminalNode IMMEDIATE() { return getToken(BaseRuleParser.IMMEDIATE, 0); }
		public ConstraintAttrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintAttr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConstraintAttr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConstraintAttr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConstraintAttr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintAttrContext constraintAttr() throws RecognitionException {
		ConstraintAttrContext _localctx = new ConstraintAttrContext(_ctx, getState());
		enterRule(_localctx, 356, RULE_constraintAttr);
		try {
			setState(2497);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2490);
				match(DEFERRABLE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2491);
				match(NOT);
				setState(2492);
				match(DEFERRABLE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2493);
				match(INITIALLY);
				setState(2494);
				match(DEFERRED);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2495);
				match(INITIALLY);
				setState(2496);
				match(IMMEDIATE);
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

	public static class ColConstraintElemContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public TerminalNode UNIQUE() { return getToken(BaseRuleParser.UNIQUE, 0); }
		public ConsTableSpaceContext consTableSpace() {
			return getRuleContext(ConsTableSpaceContext.class,0);
		}
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public DefinitionContext definition() {
			return getRuleContext(DefinitionContext.class,0);
		}
		public TerminalNode PRIMARY() { return getToken(BaseRuleParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(BaseRuleParser.KEY, 0); }
		public TerminalNode CHECK() { return getToken(BaseRuleParser.CHECK, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public AExprContext aExpr() {
			return getRuleContext(AExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public NoInheritContext noInherit() {
			return getRuleContext(NoInheritContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public BExprContext bExpr() {
			return getRuleContext(BExprContext.class,0);
		}
		public TerminalNode GENERATED() { return getToken(BaseRuleParser.GENERATED, 0); }
		public GeneratedWhenContext generatedWhen() {
			return getRuleContext(GeneratedWhenContext.class,0);
		}
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public TerminalNode IDENTITY() { return getToken(BaseRuleParser.IDENTITY, 0); }
		public ParenthesizedSeqOptListContext parenthesizedSeqOptList() {
			return getRuleContext(ParenthesizedSeqOptListContext.class,0);
		}
		public TerminalNode STORED() { return getToken(BaseRuleParser.STORED, 0); }
		public TerminalNode REFERENCES() { return getToken(BaseRuleParser.REFERENCES, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public OptColumnListContext optColumnList() {
			return getRuleContext(OptColumnListContext.class,0);
		}
		public KeyMatchContext keyMatch() {
			return getRuleContext(KeyMatchContext.class,0);
		}
		public KeyActionsContext keyActions() {
			return getRuleContext(KeyActionsContext.class,0);
		}
		public ColConstraintElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colConstraintElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColConstraintElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColConstraintElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColConstraintElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColConstraintElemContext colConstraintElem() throws RecognitionException {
		ColConstraintElemContext _localctx = new ColConstraintElemContext(_ctx, getState());
		enterRule(_localctx, 358, RULE_colConstraintElem);
		int _la;
		try {
			setState(2550);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2499);
				match(NOT);
				setState(2500);
				match(NULL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2501);
				match(NULL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2502);
				match(UNIQUE);
				setState(2505);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(2503);
					match(WITH);
					setState(2504);
					definition();
					}
				}

				setState(2507);
				consTableSpace();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2508);
				match(PRIMARY);
				setState(2509);
				match(KEY);
				setState(2512);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(2510);
					match(WITH);
					setState(2511);
					definition();
					}
				}

				setState(2514);
				consTableSpace();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2515);
				match(CHECK);
				setState(2516);
				match(LP_);
				setState(2517);
				aExpr(0);
				setState(2518);
				match(RP_);
				setState(2520);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NO) {
					{
					setState(2519);
					noInherit();
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2522);
				match(DEFAULT);
				setState(2523);
				bExpr(0);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2524);
				match(GENERATED);
				setState(2525);
				generatedWhen();
				setState(2526);
				match(AS);
				setState(2527);
				match(IDENTITY);
				setState(2529);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(2528);
					parenthesizedSeqOptList();
					}
				}

				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2531);
				match(GENERATED);
				setState(2532);
				generatedWhen();
				setState(2533);
				match(AS);
				setState(2534);
				match(LP_);
				setState(2535);
				aExpr(0);
				setState(2536);
				match(RP_);
				setState(2537);
				match(STORED);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2539);
				match(REFERENCES);
				setState(2540);
				qualifiedName();
				setState(2542);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(2541);
					optColumnList();
					}
				}

				setState(2545);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MATCH) {
					{
					setState(2544);
					keyMatch();
					}
				}

				setState(2548);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(2547);
					keyActions();
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

	public static class ParenthesizedSeqOptListContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public SeqOptListContext seqOptList() {
			return getRuleContext(SeqOptListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public ParenthesizedSeqOptListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenthesizedSeqOptList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterParenthesizedSeqOptList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitParenthesizedSeqOptList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitParenthesizedSeqOptList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParenthesizedSeqOptListContext parenthesizedSeqOptList() throws RecognitionException {
		ParenthesizedSeqOptListContext _localctx = new ParenthesizedSeqOptListContext(_ctx, getState());
		enterRule(_localctx, 360, RULE_parenthesizedSeqOptList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2552);
			match(LP_);
			setState(2553);
			seqOptList();
			setState(2554);
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

	public static class SeqOptListContext extends ParserRuleContext {
		public List<SeqOptElemContext> seqOptElem() {
			return getRuleContexts(SeqOptElemContext.class);
		}
		public SeqOptElemContext seqOptElem(int i) {
			return getRuleContext(SeqOptElemContext.class,i);
		}
		public SeqOptListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_seqOptList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSeqOptList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSeqOptList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSeqOptList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SeqOptListContext seqOptList() throws RecognitionException {
		SeqOptListContext _localctx = new SeqOptListContext(_ctx, getState());
		enterRule(_localctx, 362, RULE_seqOptList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2557); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(2556);
				seqOptElem();
				}
				}
				setState(2559); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==AS || ((((_la - 174)) & ~0x3f) == 0 && ((1L << (_la - 174)) & ((1L << (NO - 174)) | (1L << (START - 174)) | (1L << (CACHE - 174)) | (1L << (CYCLE - 174)) | (1L << (INCREMENT - 174)) | (1L << (MAXVALUE - 174)) | (1L << (MINVALUE - 174)) | (1L << (OWNED - 174)))) != 0) || _la==RESTART || _la==SEQUENCE );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SeqOptElemContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public SimpleTypeNameContext simpleTypeName() {
			return getRuleContext(SimpleTypeNameContext.class,0);
		}
		public TerminalNode CACHE() { return getToken(BaseRuleParser.CACHE, 0); }
		public NumericOnlyContext numericOnly() {
			return getRuleContext(NumericOnlyContext.class,0);
		}
		public TerminalNode CYCLE() { return getToken(BaseRuleParser.CYCLE, 0); }
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode INCREMENT() { return getToken(BaseRuleParser.INCREMENT, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public TerminalNode MAXVALUE() { return getToken(BaseRuleParser.MAXVALUE, 0); }
		public TerminalNode MINVALUE() { return getToken(BaseRuleParser.MINVALUE, 0); }
		public TerminalNode OWNED() { return getToken(BaseRuleParser.OWNED, 0); }
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public TerminalNode SEQUENCE() { return getToken(BaseRuleParser.SEQUENCE, 0); }
		public TerminalNode NAME() { return getToken(BaseRuleParser.NAME, 0); }
		public TerminalNode START() { return getToken(BaseRuleParser.START, 0); }
		public TerminalNode WITH() { return getToken(BaseRuleParser.WITH, 0); }
		public TerminalNode RESTART() { return getToken(BaseRuleParser.RESTART, 0); }
		public SeqOptElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_seqOptElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSeqOptElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSeqOptElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSeqOptElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SeqOptElemContext seqOptElem() throws RecognitionException {
		SeqOptElemContext _localctx = new SeqOptElemContext(_ctx, getState());
		enterRule(_localctx, 364, RULE_seqOptElem);
		int _la;
		try {
			setState(2598);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2561);
				match(AS);
				setState(2562);
				simpleTypeName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2563);
				match(CACHE);
				setState(2564);
				numericOnly();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2565);
				match(CYCLE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2566);
				match(NO);
				setState(2567);
				match(CYCLE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2568);
				match(INCREMENT);
				setState(2570);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BY) {
					{
					setState(2569);
					match(BY);
					}
				}

				setState(2572);
				numericOnly();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2573);
				match(MAXVALUE);
				setState(2574);
				numericOnly();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2575);
				match(MINVALUE);
				setState(2576);
				numericOnly();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2577);
				match(NO);
				setState(2578);
				match(MAXVALUE);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2579);
				match(NO);
				setState(2580);
				match(MINVALUE);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2581);
				match(OWNED);
				setState(2582);
				match(BY);
				setState(2583);
				anyName();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2584);
				match(SEQUENCE);
				setState(2585);
				match(NAME);
				setState(2586);
				anyName();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2587);
				match(START);
				setState(2589);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(2588);
					match(WITH);
					}
				}

				setState(2591);
				numericOnly();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2592);
				match(RESTART);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(2593);
				match(RESTART);
				setState(2595);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(2594);
					match(WITH);
					}
				}

				setState(2597);
				numericOnly();
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

	public static class OptColumnListContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public OptColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optColumnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOptColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOptColumnList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOptColumnList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptColumnListContext optColumnList() throws RecognitionException {
		OptColumnListContext _localctx = new OptColumnListContext(_ctx, getState());
		enterRule(_localctx, 366, RULE_optColumnList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2600);
			match(LP_);
			setState(2601);
			columnList();
			setState(2602);
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

	public static class ColumnElemContext extends ParserRuleContext {
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public ColumnElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnElemContext columnElem() throws RecognitionException {
		ColumnElemContext _localctx = new ColumnElemContext(_ctx, getState());
		enterRule(_localctx, 368, RULE_columnElem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2604);
			colId();
			}
		}
		catch (RecognitionException re) {
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
		public List<ColumnElemContext> columnElem() {
			return getRuleContexts(ColumnElemContext.class);
		}
		public ColumnElemContext columnElem(int i) {
			return getRuleContext(ColumnElemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColumnList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColumnList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 370, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2606);
			columnElem();
			setState(2611);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2607);
				match(COMMA_);
				setState(2608);
				columnElem();
				}
				}
				setState(2613);
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

	public static class GeneratedWhenContext extends ParserRuleContext {
		public TerminalNode ALWAYS() { return getToken(BaseRuleParser.ALWAYS, 0); }
		public TerminalNode BY() { return getToken(BaseRuleParser.BY, 0); }
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public GeneratedWhenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generatedWhen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGeneratedWhen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGeneratedWhen(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGeneratedWhen(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GeneratedWhenContext generatedWhen() throws RecognitionException {
		GeneratedWhenContext _localctx = new GeneratedWhenContext(_ctx, getState());
		enterRule(_localctx, 372, RULE_generatedWhen);
		try {
			setState(2617);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALWAYS:
				enterOuterAlt(_localctx, 1);
				{
				setState(2614);
				match(ALWAYS);
				}
				break;
			case BY:
				enterOuterAlt(_localctx, 2);
				{
				setState(2615);
				match(BY);
				setState(2616);
				match(DEFAULT);
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

	public static class NoInheritContext extends ParserRuleContext {
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode INHERIT() { return getToken(BaseRuleParser.INHERIT, 0); }
		public NoInheritContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_noInherit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNoInherit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNoInherit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNoInherit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NoInheritContext noInherit() throws RecognitionException {
		NoInheritContext _localctx = new NoInheritContext(_ctx, getState());
		enterRule(_localctx, 374, RULE_noInherit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2619);
			match(NO);
			setState(2620);
			match(INHERIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConsTableSpaceContext extends ParserRuleContext {
		public TerminalNode USING() { return getToken(BaseRuleParser.USING, 0); }
		public TerminalNode INDEX() { return getToken(BaseRuleParser.INDEX, 0); }
		public TerminalNode TABLESPACE() { return getToken(BaseRuleParser.TABLESPACE, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ConsTableSpaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_consTableSpace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterConsTableSpace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitConsTableSpace(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitConsTableSpace(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConsTableSpaceContext consTableSpace() throws RecognitionException {
		ConsTableSpaceContext _localctx = new ConsTableSpaceContext(_ctx, getState());
		enterRule(_localctx, 376, RULE_consTableSpace);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2622);
			match(USING);
			setState(2623);
			match(INDEX);
			setState(2624);
			match(TABLESPACE);
			setState(2625);
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

	public static class DefinitionContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public DefListContext defList() {
			return getRuleContext(DefListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public DefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefinitionContext definition() throws RecognitionException {
		DefinitionContext _localctx = new DefinitionContext(_ctx, getState());
		enterRule(_localctx, 378, RULE_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2627);
			match(LP_);
			setState(2628);
			defList();
			setState(2629);
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

	public static class DefListContext extends ParserRuleContext {
		public List<DefElemContext> defElem() {
			return getRuleContexts(DefElemContext.class);
		}
		public DefElemContext defElem(int i) {
			return getRuleContext(DefElemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public DefListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDefList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDefList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDefList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefListContext defList() throws RecognitionException {
		DefListContext _localctx = new DefListContext(_ctx, getState());
		enterRule(_localctx, 380, RULE_defList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2631);
			defElem();
			setState(2636);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2632);
				match(COMMA_);
				setState(2633);
				defElem();
				}
				}
				setState(2638);
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

	public static class DefElemContext extends ParserRuleContext {
		public ColLabelContext colLabel() {
			return getRuleContext(ColLabelContext.class,0);
		}
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public DefArgContext defArg() {
			return getRuleContext(DefArgContext.class,0);
		}
		public DefElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterDefElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitDefElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitDefElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefElemContext defElem() throws RecognitionException {
		DefElemContext _localctx = new DefElemContext(_ctx, getState());
		enterRule(_localctx, 382, RULE_defElem);
		try {
			setState(2644);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(2639);
				colLabel();
				setState(2640);
				match(EQ_);
				setState(2641);
				defArg();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2643);
				colLabel();
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

	public static class ColLabelContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public UnreservedWordContext unreservedWord() {
			return getRuleContext(UnreservedWordContext.class,0);
		}
		public ColNameKeywordContext colNameKeyword() {
			return getRuleContext(ColNameKeywordContext.class,0);
		}
		public TypeFuncNameKeywordContext typeFuncNameKeyword() {
			return getRuleContext(TypeFuncNameKeywordContext.class,0);
		}
		public ReservedKeywordContext reservedKeyword() {
			return getRuleContext(ReservedKeywordContext.class,0);
		}
		public ColLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterColLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitColLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitColLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColLabelContext colLabel() throws RecognitionException {
		ColLabelContext _localctx = new ColLabelContext(_ctx, getState());
		enterRule(_localctx, 384, RULE_colLabel);
		try {
			setState(2651);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2646);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2647);
				unreservedWord();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2648);
				colNameKeyword();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2649);
				typeFuncNameKeyword();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2650);
				reservedKeyword();
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

	public static class KeyActionsContext extends ParserRuleContext {
		public KeyUpdateContext keyUpdate() {
			return getRuleContext(KeyUpdateContext.class,0);
		}
		public KeyDeleteContext keyDelete() {
			return getRuleContext(KeyDeleteContext.class,0);
		}
		public KeyActionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyActions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterKeyActions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitKeyActions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitKeyActions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyActionsContext keyActions() throws RecognitionException {
		KeyActionsContext _localctx = new KeyActionsContext(_ctx, getState());
		enterRule(_localctx, 386, RULE_keyActions);
		try {
			setState(2661);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2653);
				keyUpdate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2654);
				keyDelete();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2655);
				keyUpdate();
				setState(2656);
				keyDelete();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2658);
				keyDelete();
				setState(2659);
				keyUpdate();
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

	public static class KeyDeleteContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode DELETE() { return getToken(BaseRuleParser.DELETE, 0); }
		public KeyActionContext keyAction() {
			return getRuleContext(KeyActionContext.class,0);
		}
		public KeyDeleteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyDelete; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterKeyDelete(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitKeyDelete(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitKeyDelete(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyDeleteContext keyDelete() throws RecognitionException {
		KeyDeleteContext _localctx = new KeyDeleteContext(_ctx, getState());
		enterRule(_localctx, 388, RULE_keyDelete);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2663);
			match(ON);
			setState(2664);
			match(DELETE);
			setState(2665);
			keyAction();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyUpdateContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public TerminalNode UPDATE() { return getToken(BaseRuleParser.UPDATE, 0); }
		public KeyActionContext keyAction() {
			return getRuleContext(KeyActionContext.class,0);
		}
		public KeyUpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyUpdate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterKeyUpdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitKeyUpdate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitKeyUpdate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyUpdateContext keyUpdate() throws RecognitionException {
		KeyUpdateContext _localctx = new KeyUpdateContext(_ctx, getState());
		enterRule(_localctx, 390, RULE_keyUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2667);
			match(ON);
			setState(2668);
			match(UPDATE);
			setState(2669);
			keyAction();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyActionContext extends ParserRuleContext {
		public TerminalNode NO() { return getToken(BaseRuleParser.NO, 0); }
		public TerminalNode ACTION() { return getToken(BaseRuleParser.ACTION, 0); }
		public TerminalNode RESTRICT() { return getToken(BaseRuleParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(BaseRuleParser.CASCADE, 0); }
		public TerminalNode SET() { return getToken(BaseRuleParser.SET, 0); }
		public TerminalNode NULL() { return getToken(BaseRuleParser.NULL, 0); }
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public KeyActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterKeyAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitKeyAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitKeyAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyActionContext keyAction() throws RecognitionException {
		KeyActionContext _localctx = new KeyActionContext(_ctx, getState());
		enterRule(_localctx, 392, RULE_keyAction);
		try {
			setState(2679);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2671);
				match(NO);
				setState(2672);
				match(ACTION);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2673);
				match(RESTRICT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2674);
				match(CASCADE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2675);
				match(SET);
				setState(2676);
				match(NULL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2677);
				match(SET);
				setState(2678);
				match(DEFAULT);
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

	public static class KeyMatchContext extends ParserRuleContext {
		public TerminalNode MATCH() { return getToken(BaseRuleParser.MATCH, 0); }
		public TerminalNode FULL() { return getToken(BaseRuleParser.FULL, 0); }
		public TerminalNode PARTIAL() { return getToken(BaseRuleParser.PARTIAL, 0); }
		public TerminalNode SIMPLE() { return getToken(BaseRuleParser.SIMPLE, 0); }
		public KeyMatchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyMatch; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterKeyMatch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitKeyMatch(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitKeyMatch(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyMatchContext keyMatch() throws RecognitionException {
		KeyMatchContext _localctx = new KeyMatchContext(_ctx, getState());
		enterRule(_localctx, 394, RULE_keyMatch);
		try {
			setState(2687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2681);
				match(MATCH);
				setState(2682);
				match(FULL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2683);
				match(MATCH);
				setState(2684);
				match(PARTIAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2685);
				match(MATCH);
				setState(2686);
				match(SIMPLE);
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

	public static class CreateGenericOptionsContext extends ParserRuleContext {
		public TerminalNode OPTIONS() { return getToken(BaseRuleParser.OPTIONS, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public GenericOptionListContext genericOptionList() {
			return getRuleContext(GenericOptionListContext.class,0);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public CreateGenericOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createGenericOptions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCreateGenericOptions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCreateGenericOptions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCreateGenericOptions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateGenericOptionsContext createGenericOptions() throws RecognitionException {
		CreateGenericOptionsContext _localctx = new CreateGenericOptionsContext(_ctx, getState());
		enterRule(_localctx, 396, RULE_createGenericOptions);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2689);
			match(OPTIONS);
			setState(2690);
			match(LP_);
			setState(2691);
			genericOptionList();
			setState(2692);
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

	public static class GenericOptionListContext extends ParserRuleContext {
		public List<GenericOptionElemContext> genericOptionElem() {
			return getRuleContexts(GenericOptionElemContext.class);
		}
		public GenericOptionElemContext genericOptionElem(int i) {
			return getRuleContext(GenericOptionElemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public GenericOptionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericOptionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericOptionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericOptionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericOptionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericOptionListContext genericOptionList() throws RecognitionException {
		GenericOptionListContext _localctx = new GenericOptionListContext(_ctx, getState());
		enterRule(_localctx, 398, RULE_genericOptionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2694);
			genericOptionElem();
			setState(2699);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2695);
				match(COMMA_);
				setState(2696);
				genericOptionElem();
				}
				}
				setState(2701);
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

	public static class GenericOptionElemContext extends ParserRuleContext {
		public GenericOptionNameContext genericOptionName() {
			return getRuleContext(GenericOptionNameContext.class,0);
		}
		public GenericOptionArgContext genericOptionArg() {
			return getRuleContext(GenericOptionArgContext.class,0);
		}
		public GenericOptionElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericOptionElem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericOptionElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericOptionElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericOptionElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericOptionElemContext genericOptionElem() throws RecognitionException {
		GenericOptionElemContext _localctx = new GenericOptionElemContext(_ctx, getState());
		enterRule(_localctx, 400, RULE_genericOptionElem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2702);
			genericOptionName();
			setState(2703);
			genericOptionArg();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GenericOptionArgContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public GenericOptionArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericOptionArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericOptionArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericOptionArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericOptionArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericOptionArgContext genericOptionArg() throws RecognitionException {
		GenericOptionArgContext _localctx = new GenericOptionArgContext(_ctx, getState());
		enterRule(_localctx, 402, RULE_genericOptionArg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2705);
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

	public static class GenericOptionNameContext extends ParserRuleContext {
		public ColLableContext colLable() {
			return getRuleContext(ColLableContext.class,0);
		}
		public GenericOptionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericOptionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericOptionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericOptionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericOptionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericOptionNameContext genericOptionName() throws RecognitionException {
		GenericOptionNameContext _localctx = new GenericOptionNameContext(_ctx, getState());
		enterRule(_localctx, 404, RULE_genericOptionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2707);
			colLable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReplicaIdentityContext extends ParserRuleContext {
		public TerminalNode NOTHING() { return getToken(BaseRuleParser.NOTHING, 0); }
		public TerminalNode FULL() { return getToken(BaseRuleParser.FULL, 0); }
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public TerminalNode USING() { return getToken(BaseRuleParser.USING, 0); }
		public TerminalNode INDEX() { return getToken(BaseRuleParser.INDEX, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ReplicaIdentityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replicaIdentity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterReplicaIdentity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitReplicaIdentity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitReplicaIdentity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReplicaIdentityContext replicaIdentity() throws RecognitionException {
		ReplicaIdentityContext _localctx = new ReplicaIdentityContext(_ctx, getState());
		enterRule(_localctx, 406, RULE_replicaIdentity);
		try {
			setState(2715);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOTHING:
				enterOuterAlt(_localctx, 1);
				{
				setState(2709);
				match(NOTHING);
				}
				break;
			case FULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2710);
				match(FULL);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 3);
				{
				setState(2711);
				match(DEFAULT);
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 4);
				{
				setState(2712);
				match(USING);
				setState(2713);
				match(INDEX);
				setState(2714);
				name();
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

	public static class OperArgtypesContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public List<TypeNameContext> typeName() {
			return getRuleContexts(TypeNameContext.class);
		}
		public TypeNameContext typeName(int i) {
			return getRuleContext(TypeNameContext.class,i);
		}
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public TerminalNode COMMA_() { return getToken(BaseRuleParser.COMMA_, 0); }
		public TerminalNode NONE() { return getToken(BaseRuleParser.NONE, 0); }
		public OperArgtypesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operArgtypes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterOperArgtypes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitOperArgtypes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitOperArgtypes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperArgtypesContext operArgtypes() throws RecognitionException {
		OperArgtypesContext _localctx = new OperArgtypesContext(_ctx, getState());
		enterRule(_localctx, 408, RULE_operArgtypes);
		try {
			setState(2739);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2717);
				match(LP_);
				setState(2718);
				typeName();
				setState(2719);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2721);
				match(LP_);
				setState(2722);
				typeName();
				setState(2723);
				match(COMMA_);
				setState(2724);
				typeName();
				setState(2725);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2727);
				match(LP_);
				setState(2728);
				match(NONE);
				setState(2729);
				match(COMMA_);
				setState(2730);
				typeName();
				setState(2731);
				match(RP_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2733);
				match(LP_);
				setState(2734);
				typeName();
				setState(2735);
				match(COMMA_);
				setState(2736);
				match(NONE);
				setState(2737);
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

	public static class FuncArgContext extends ParserRuleContext {
		public ArgClassContext argClass() {
			return getRuleContext(ArgClassContext.class,0);
		}
		public ParamNameContext paramName() {
			return getRuleContext(ParamNameContext.class,0);
		}
		public FuncTypeContext funcType() {
			return getRuleContext(FuncTypeContext.class,0);
		}
		public FuncArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncArgContext funcArg() throws RecognitionException {
		FuncArgContext _localctx = new FuncArgContext(_ctx, getState());
		enterRule(_localctx, 410, RULE_funcArg);
		try {
			setState(2756);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2741);
				argClass();
				setState(2742);
				paramName();
				setState(2743);
				funcType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2745);
				paramName();
				setState(2746);
				argClass();
				setState(2747);
				funcType();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2749);
				paramName();
				setState(2750);
				funcType();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2752);
				argClass();
				setState(2753);
				funcType();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2755);
				funcType();
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

	public static class ArgClassContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(BaseRuleParser.IN, 0); }
		public TerminalNode OUT() { return getToken(BaseRuleParser.OUT, 0); }
		public TerminalNode INOUT() { return getToken(BaseRuleParser.INOUT, 0); }
		public TerminalNode VARIADIC() { return getToken(BaseRuleParser.VARIADIC, 0); }
		public ArgClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterArgClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitArgClass(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitArgClass(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgClassContext argClass() throws RecognitionException {
		ArgClassContext _localctx = new ArgClassContext(_ctx, getState());
		enterRule(_localctx, 412, RULE_argClass);
		try {
			setState(2764);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2758);
				match(IN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2759);
				match(OUT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2760);
				match(INOUT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2761);
				match(IN);
				setState(2762);
				match(OUT);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2763);
				match(VARIADIC);
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

	public static class FuncArgsListContext extends ParserRuleContext {
		public List<FuncArgContext> funcArg() {
			return getRuleContexts(FuncArgContext.class);
		}
		public FuncArgContext funcArg(int i) {
			return getRuleContext(FuncArgContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public FuncArgsListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcArgsList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFuncArgsList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFuncArgsList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFuncArgsList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncArgsListContext funcArgsList() throws RecognitionException {
		FuncArgsListContext _localctx = new FuncArgsListContext(_ctx, getState());
		enterRule(_localctx, 414, RULE_funcArgsList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2766);
			funcArg();
			setState(2771);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2767);
				match(COMMA_);
				setState(2768);
				funcArg();
				}
				}
				setState(2773);
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

	public static class NonReservedWordOrSconstContext extends ParserRuleContext {
		public NonReservedWordContext nonReservedWord() {
			return getRuleContext(NonReservedWordContext.class,0);
		}
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public NonReservedWordOrSconstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonReservedWordOrSconst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterNonReservedWordOrSconst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitNonReservedWordOrSconst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitNonReservedWordOrSconst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonReservedWordOrSconstContext nonReservedWordOrSconst() throws RecognitionException {
		NonReservedWordOrSconstContext _localctx = new NonReservedWordOrSconstContext(_ctx, getState());
		enterRule(_localctx, 416, RULE_nonReservedWordOrSconst);
		try {
			setState(2776);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case POSITION:
			case PRECISION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case VALUES:
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
			case IF:
			case IS:
			case EXISTS:
			case BETWEEN:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHAR:
			case CHARACTER:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case INTEGER:
			case REAL:
			case DECIMAL:
			case TYPE:
			case SMALLINT:
			case BIGINT:
			case NUMERIC:
			case TEXT:
			case REPEATABLE:
			case NULLIF:
			case VARYING:
			case NATIONAL:
			case NCHAR:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case DEC:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case INT:
			case FLOAT:
			case VARCHAR:
			case ENUM:
			case BIT:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_SCHEMA:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case SETOF:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case XMLATTRIBUTES:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case XMLTABLE:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case XMLNAMESPACES:
			case NONE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case OUT:
			case INOUT:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(2774);
				nonReservedWord();
				}
				break;
			case STRING_:
				enterOuterAlt(_localctx, 2);
				{
				setState(2775);
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
		enterRule(_localctx, 418, RULE_fileName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2778);
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

	public static class RoleListContext extends ParserRuleContext {
		public List<RoleSpecContext> roleSpec() {
			return getRuleContexts(RoleSpecContext.class);
		}
		public RoleSpecContext roleSpec(int i) {
			return getRuleContext(RoleSpecContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public RoleListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_roleList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRoleList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRoleList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRoleList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoleListContext roleList() throws RecognitionException {
		RoleListContext _localctx = new RoleListContext(_ctx, getState());
		enterRule(_localctx, 420, RULE_roleList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2780);
			roleSpec();
			setState(2785);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2781);
				match(COMMA_);
				setState(2782);
				roleSpec();
				}
				}
				setState(2787);
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

	public static class SetResetClauseContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(BaseRuleParser.SET, 0); }
		public SetRestContext setRest() {
			return getRuleContext(SetRestContext.class,0);
		}
		public VariableResetStmtContext variableResetStmt() {
			return getRuleContext(VariableResetStmtContext.class,0);
		}
		public SetResetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setResetClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSetResetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSetResetClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSetResetClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetResetClauseContext setResetClause() throws RecognitionException {
		SetResetClauseContext _localctx = new SetResetClauseContext(_ctx, getState());
		enterRule(_localctx, 422, RULE_setResetClause);
		try {
			setState(2791);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SET:
				enterOuterAlt(_localctx, 1);
				{
				setState(2788);
				match(SET);
				setState(2789);
				setRest();
				}
				break;
			case RESET:
				enterOuterAlt(_localctx, 2);
				{
				setState(2790);
				variableResetStmt();
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

	public static class SetRestContext extends ParserRuleContext {
		public TerminalNode TRANSACTION() { return getToken(BaseRuleParser.TRANSACTION, 0); }
		public TransactionModeListContext transactionModeList() {
			return getRuleContext(TransactionModeListContext.class,0);
		}
		public TerminalNode SESSION() { return getToken(BaseRuleParser.SESSION, 0); }
		public TerminalNode CHARACTERISTICS() { return getToken(BaseRuleParser.CHARACTERISTICS, 0); }
		public TerminalNode AS() { return getToken(BaseRuleParser.AS, 0); }
		public SetRestMoreContext setRestMore() {
			return getRuleContext(SetRestMoreContext.class,0);
		}
		public SetRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setRest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSetRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSetRest(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSetRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetRestContext setRest() throws RecognitionException {
		SetRestContext _localctx = new SetRestContext(_ctx, getState());
		enterRule(_localctx, 424, RULE_setRest);
		try {
			setState(2801);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2793);
				match(TRANSACTION);
				setState(2794);
				transactionModeList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2795);
				match(SESSION);
				setState(2796);
				match(CHARACTERISTICS);
				setState(2797);
				match(AS);
				setState(2798);
				match(TRANSACTION);
				setState(2799);
				transactionModeList();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2800);
				setRestMore();
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

	public static class TransactionModeListContext extends ParserRuleContext {
		public List<TransactionModeItemContext> transactionModeItem() {
			return getRuleContexts(TransactionModeItemContext.class);
		}
		public TransactionModeItemContext transactionModeItem(int i) {
			return getRuleContext(TransactionModeItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TransactionModeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transactionModeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTransactionModeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTransactionModeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTransactionModeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransactionModeListContext transactionModeList() throws RecognitionException {
		TransactionModeListContext _localctx = new TransactionModeListContext(_ctx, getState());
		enterRule(_localctx, 426, RULE_transactionModeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2803);
			transactionModeItem();
			setState(2810);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NOT || ((((_la - 172)) & ~0x3f) == 0 && ((1L << (_la - 172)) & ((1L << (ISOLATION - 172)) | (1L << (READ - 172)) | (1L << (DEFERRABLE - 172)))) != 0) || _la==COMMA_) {
				{
				{
				setState(2805);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA_) {
					{
					setState(2804);
					match(COMMA_);
					}
				}

				setState(2807);
				transactionModeItem();
				}
				}
				setState(2812);
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

	public static class TransactionModeItemContext extends ParserRuleContext {
		public TerminalNode ISOLATION() { return getToken(BaseRuleParser.ISOLATION, 0); }
		public TerminalNode LEVEL() { return getToken(BaseRuleParser.LEVEL, 0); }
		public IsoLevelContext isoLevel() {
			return getRuleContext(IsoLevelContext.class,0);
		}
		public TerminalNode READ() { return getToken(BaseRuleParser.READ, 0); }
		public TerminalNode ONLY() { return getToken(BaseRuleParser.ONLY, 0); }
		public TerminalNode WRITE() { return getToken(BaseRuleParser.WRITE, 0); }
		public TerminalNode DEFERRABLE() { return getToken(BaseRuleParser.DEFERRABLE, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TransactionModeItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transactionModeItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTransactionModeItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTransactionModeItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTransactionModeItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransactionModeItemContext transactionModeItem() throws RecognitionException {
		TransactionModeItemContext _localctx = new TransactionModeItemContext(_ctx, getState());
		enterRule(_localctx, 428, RULE_transactionModeItem);
		try {
			setState(2823);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,221,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2813);
				match(ISOLATION);
				setState(2814);
				match(LEVEL);
				setState(2815);
				isoLevel();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2816);
				match(READ);
				setState(2817);
				match(ONLY);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2818);
				match(READ);
				setState(2819);
				match(WRITE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2820);
				match(DEFERRABLE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2821);
				match(NOT);
				setState(2822);
				match(DEFERRABLE);
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

	public static class SetRestMoreContext extends ParserRuleContext {
		public GenericSetContext genericSet() {
			return getRuleContext(GenericSetContext.class,0);
		}
		public VarNameContext varName() {
			return getRuleContext(VarNameContext.class,0);
		}
		public TerminalNode FROM() { return getToken(BaseRuleParser.FROM, 0); }
		public TerminalNode CURRENT() { return getToken(BaseRuleParser.CURRENT, 0); }
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public ZoneValueContext zoneValue() {
			return getRuleContext(ZoneValueContext.class,0);
		}
		public TerminalNode CATALOG() { return getToken(BaseRuleParser.CATALOG, 0); }
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public TerminalNode SCHEMA() { return getToken(BaseRuleParser.SCHEMA, 0); }
		public TerminalNode NAMES() { return getToken(BaseRuleParser.NAMES, 0); }
		public EncodingContext encoding() {
			return getRuleContext(EncodingContext.class,0);
		}
		public TerminalNode ROLE() { return getToken(BaseRuleParser.ROLE, 0); }
		public NonReservedWordContext nonReservedWord() {
			return getRuleContext(NonReservedWordContext.class,0);
		}
		public TerminalNode SESSION() { return getToken(BaseRuleParser.SESSION, 0); }
		public TerminalNode AUTHORIZATION() { return getToken(BaseRuleParser.AUTHORIZATION, 0); }
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public TerminalNode XML() { return getToken(BaseRuleParser.XML, 0); }
		public TerminalNode OPTION() { return getToken(BaseRuleParser.OPTION, 0); }
		public DocumentOrContentContext documentOrContent() {
			return getRuleContext(DocumentOrContentContext.class,0);
		}
		public TerminalNode TRANSACTION() { return getToken(BaseRuleParser.TRANSACTION, 0); }
		public TerminalNode SNAPSHOT() { return getToken(BaseRuleParser.SNAPSHOT, 0); }
		public SetRestMoreContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setRestMore; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterSetRestMore(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitSetRestMore(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitSetRestMore(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetRestMoreContext setRestMore() throws RecognitionException {
		SetRestMoreContext _localctx = new SetRestMoreContext(_ctx, getState());
		enterRule(_localctx, 430, RULE_setRestMore);
		int _la;
		try {
			setState(2857);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,223,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2825);
				genericSet();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2826);
				varName(0);
				setState(2827);
				match(FROM);
				setState(2828);
				match(CURRENT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2830);
				match(TIME);
				setState(2831);
				match(ZONE);
				setState(2832);
				zoneValue();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2833);
				match(CATALOG);
				setState(2834);
				match(STRING_);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2835);
				match(SCHEMA);
				setState(2836);
				match(STRING_);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2837);
				match(NAMES);
				setState(2839);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DEFAULT || _la==STRING_) {
					{
					setState(2838);
					encoding();
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2841);
				match(ROLE);
				setState(2842);
				nonReservedWord();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2843);
				match(STRING_);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2844);
				match(SESSION);
				setState(2845);
				match(AUTHORIZATION);
				setState(2846);
				nonReservedWord();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2847);
				match(STRING_);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2848);
				match(SESSION);
				setState(2849);
				match(AUTHORIZATION);
				setState(2850);
				match(DEFAULT);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2851);
				match(XML);
				setState(2852);
				match(OPTION);
				setState(2853);
				documentOrContent();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2854);
				match(TRANSACTION);
				setState(2855);
				match(SNAPSHOT);
				setState(2856);
				match(STRING_);
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

	public static class EncodingContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(BaseRuleParser.STRING_, 0); }
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public EncodingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_encoding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEncoding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEncoding(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEncoding(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EncodingContext encoding() throws RecognitionException {
		EncodingContext _localctx = new EncodingContext(_ctx, getState());
		enterRule(_localctx, 432, RULE_encoding);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2859);
			_la = _input.LA(1);
			if ( !(_la==DEFAULT || _la==STRING_) ) {
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

	public static class GenericSetContext extends ParserRuleContext {
		public VarNameContext varName() {
			return getRuleContext(VarNameContext.class,0);
		}
		public TerminalNode EQ_() { return getToken(BaseRuleParser.EQ_, 0); }
		public TerminalNode TO() { return getToken(BaseRuleParser.TO, 0); }
		public VarListContext varList() {
			return getRuleContext(VarListContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(BaseRuleParser.DEFAULT, 0); }
		public GenericSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericSetContext genericSet() throws RecognitionException {
		GenericSetContext _localctx = new GenericSetContext(_ctx, getState());
		enterRule(_localctx, 434, RULE_genericSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2861);
			varName(0);
			setState(2862);
			_la = _input.LA(1);
			if ( !(_la==TO || _la==EQ_) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(2865);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case POSITION:
			case PRECISION:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case VALUES:
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
			case ON:
			case IF:
			case IS:
			case TRUE:
			case FALSE:
			case EXISTS:
			case BETWEEN:
			case LIKE:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DOUBLE:
			case CHAR:
			case CHARACTER:
			case INTERVAL:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case COLLATION:
			case NAMES:
			case INTEGER:
			case REAL:
			case DECIMAL:
			case TYPE:
			case SMALLINT:
			case BIGINT:
			case NUMERIC:
			case TEXT:
			case REPEATABLE:
			case NULLIF:
			case VARYING:
			case NATIONAL:
			case NCHAR:
			case VALUE:
			case COALESCE:
			case TIES:
			case CUBE:
			case GROUPING:
			case SETS:
			case OTHERS:
			case OVERLAPS:
			case AT:
			case DEC:
			case ADMIN:
			case BINARY:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case ROW:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONCURRENTLY:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case EXTRACT:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case INT:
			case FLOAT:
			case VARCHAR:
			case ENUM:
			case BIT:
			case XML:
			case JSON:
			case TABLESAMPLE:
			case ORDINALITY:
			case CURRENT_SCHEMA:
			case OVERLAY:
			case XMLCONCAT:
			case XMLELEMENT:
			case XMLEXISTS:
			case XMLFOREST:
			case XMLPARSE:
			case XMLPI:
			case XMLROOT:
			case XMLSERIALIZE:
			case TREAT:
			case SETOF:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case XMLATTRIBUTES:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case GREATEST:
			case LEAST:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case ILIKE:
			case SIMILAR:
			case ISNULL:
			case NOTNULL:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case XMLTABLE:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case XMLNAMESPACES:
			case NONE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case FREEZE:
			case AUTHORIZATION:
			case VERBOSE:
			case OUT:
			case INOUT:
			case PLUS_:
			case MINUS_:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
				{
				setState(2863);
				varList();
				}
				break;
			case DEFAULT:
				{
				setState(2864);
				match(DEFAULT);
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

	public static class VariableResetStmtContext extends ParserRuleContext {
		public TerminalNode RESET() { return getToken(BaseRuleParser.RESET, 0); }
		public ResetRestContext resetRest() {
			return getRuleContext(ResetRestContext.class,0);
		}
		public VariableResetStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableResetStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterVariableResetStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitVariableResetStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitVariableResetStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableResetStmtContext variableResetStmt() throws RecognitionException {
		VariableResetStmtContext _localctx = new VariableResetStmtContext(_ctx, getState());
		enterRule(_localctx, 436, RULE_variableResetStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2867);
			match(RESET);
			setState(2868);
			resetRest();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResetRestContext extends ParserRuleContext {
		public GenericResetContext genericReset() {
			return getRuleContext(GenericResetContext.class,0);
		}
		public TerminalNode TIME() { return getToken(BaseRuleParser.TIME, 0); }
		public TerminalNode ZONE() { return getToken(BaseRuleParser.ZONE, 0); }
		public TerminalNode TRANSACTION() { return getToken(BaseRuleParser.TRANSACTION, 0); }
		public TerminalNode ISOLATION() { return getToken(BaseRuleParser.ISOLATION, 0); }
		public TerminalNode LEVEL() { return getToken(BaseRuleParser.LEVEL, 0); }
		public TerminalNode SESSION() { return getToken(BaseRuleParser.SESSION, 0); }
		public TerminalNode AUTHORIZATION() { return getToken(BaseRuleParser.AUTHORIZATION, 0); }
		public ResetRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resetRest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterResetRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitResetRest(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitResetRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResetRestContext resetRest() throws RecognitionException {
		ResetRestContext _localctx = new ResetRestContext(_ctx, getState());
		enterRule(_localctx, 438, RULE_resetRest);
		try {
			setState(2878);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,225,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2870);
				genericReset();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2871);
				match(TIME);
				setState(2872);
				match(ZONE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2873);
				match(TRANSACTION);
				setState(2874);
				match(ISOLATION);
				setState(2875);
				match(LEVEL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2876);
				match(SESSION);
				setState(2877);
				match(AUTHORIZATION);
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

	public static class GenericResetContext extends ParserRuleContext {
		public VarNameContext varName() {
			return getRuleContext(VarNameContext.class,0);
		}
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public GenericResetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericReset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterGenericReset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitGenericReset(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitGenericReset(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericResetContext genericReset() throws RecognitionException {
		GenericResetContext _localctx = new GenericResetContext(_ctx, getState());
		enterRule(_localctx, 440, RULE_genericReset);
		try {
			setState(2882);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case INSERT:
			case UPDATE:
			case DELETE:
			case ALTER:
			case DROP:
			case TRUNCATE:
			case SCHEMA:
			case REVOKE:
			case ADD:
			case SET:
			case INDEX:
			case KEY:
			case FUNCTION:
			case TRIGGER:
			case PROCEDURE:
			case VIEW:
			case IF:
			case BY:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case DOUBLE:
			case YEAR:
			case MONTH:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case CALL:
			case PRESERVE:
			case DEFINER:
			case SQL:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case NEXT:
			case NAME:
			case NAMES:
			case TYPE:
			case TEXT:
			case REPEATABLE:
			case VARYING:
			case VALUE:
			case TIES:
			case CUBE:
			case SETS:
			case OTHERS:
			case AT:
			case ADMIN:
			case ESCAPE:
			case EXCLUDE:
			case PARTITION:
			case UNKNOWN:
			case ALWAYS:
			case CASCADE:
			case GENERATED:
			case ISOLATION:
			case LEVEL:
			case NO:
			case OPTION:
			case PRIVILEGES:
			case READ:
			case ROLE:
			case ROWS:
			case START:
			case TRANSACTION:
			case ACTION:
			case CACHE:
			case CHARACTERISTICS:
			case CLUSTER:
			case COMMENTS:
			case CONSTRAINTS:
			case CYCLE:
			case DATA:
			case DATABASE:
			case DEFAULTS:
			case DEFERRED:
			case DEPENDS:
			case DOMAIN:
			case EXCLUDING:
			case EXECUTE:
			case EXTENSION:
			case EXTERNAL:
			case FILTER:
			case FIRST:
			case FOLLOWING:
			case FORCE:
			case GLOBAL:
			case IDENTITY:
			case IMMEDIATE:
			case INCLUDING:
			case INCREMENT:
			case INDEXES:
			case INHERIT:
			case INHERITS:
			case INCLUDE:
			case LANGUAGE:
			case LARGE:
			case LAST:
			case LOGGED:
			case MATCH:
			case MAXVALUE:
			case MINVALUE:
			case NOTHING:
			case NULLS:
			case OBJECT:
			case OIDS:
			case OVER:
			case OWNED:
			case OWNER:
			case PARTIAL:
			case PRECEDING:
			case RANGE:
			case RENAME:
			case REPLICA:
			case RESET:
			case RESTART:
			case RESTRICT:
			case ROUTINE:
			case RULE:
			case SECURITY:
			case SEQUENCE:
			case SESSION:
			case SHOW:
			case SIMPLE:
			case STATISTICS:
			case STORAGE:
			case TABLESPACE:
			case TEMP:
			case TEMPORARY:
			case UNBOUNDED:
			case UNLOGGED:
			case VALID:
			case VALIDATE:
			case WITHIN:
			case WITHOUT:
			case ZONE:
			case OF:
			case UESCAPE:
			case GROUPS:
			case RECURSIVE:
			case ENUM:
			case XML:
			case JSON:
			case ORDINALITY:
			case NFC:
			case NFD:
			case NFKC:
			case NFKD:
			case REF:
			case PASSING:
			case VERSION:
			case YES:
			case STANDALONE:
			case MATERIALIZED:
			case OPERATOR:
			case SHARE:
			case ROLLUP:
			case DOCUMENT:
			case NORMALIZED:
			case NOWAIT:
			case LOCKED:
			case COLUMNS:
			case CONTENT:
			case STRIP:
			case WHITESPACE:
			case CONFLICT:
			case OVERRIDING:
			case SYSTEM:
			case ABORT:
			case ABSOLUTE:
			case ACCESS:
			case AFTER:
			case AGGREGATE:
			case ALSO:
			case ATTACH:
			case ATTRIBUTE:
			case BACKWARD:
			case BEFORE:
			case ASSERTION:
			case ASSIGNMENT:
			case CONTINUE:
			case CONVERSION:
			case COPY:
			case COST:
			case CSV:
			case CALLED:
			case CATALOG:
			case CHAIN:
			case CHECKPOINT:
			case CLASS:
			case CONFIGURATION:
			case COMMENT:
			case DETACH:
			case DICTIONARY:
			case EXPRESSION:
			case INSENSITIVE:
			case DISCARD:
			case OFF:
			case INSTEAD:
			case EXPLAIN:
			case INPUT:
			case INLINE:
			case PARALLEL:
			case LEAKPROOF:
			case COMMITTED:
			case ENCODING:
			case IMPLICIT:
			case DELIMITER:
			case CURSOR:
			case EACH:
			case EVENT:
			case DEALLOCATE:
			case CONNECTION:
			case DECLARE:
			case FAMILY:
			case FORWARD:
			case EXCLUSIVE:
			case FUNCTIONS:
			case LOCATION:
			case LABEL:
			case DELIMITERS:
			case HANDLER:
			case HEADER:
			case IMMUTABLE:
			case GRANTED:
			case HOLD:
			case MAPPING:
			case OLD:
			case METHOD:
			case LOAD:
			case LISTEN:
			case MODE:
			case MOVE:
			case PROCEDURAL:
			case PARSER:
			case PROCEDURES:
			case ENCRYPTED:
			case PUBLICATION:
			case PROGRAM:
			case REFERENCING:
			case PLANS:
			case REINDEX:
			case PRIOR:
			case PASSWORD:
			case RELATIVE:
			case QUOTE:
			case ROUTINES:
			case REPLACE:
			case SNAPSHOT:
			case REFRESH:
			case PREPARE:
			case OPTIONS:
			case IMPORT:
			case INVOKER:
			case NEW:
			case PREPARED:
			case SCROLL:
			case SEQUENCES:
			case SYSID:
			case REASSIGN:
			case SERVER:
			case SUBSCRIPTION:
			case SEARCH:
			case SCHEMAS:
			case RECHECK:
			case POLICY:
			case NOTIFY:
			case LOCK:
			case RELEASE:
			case SERIALIZABLE:
			case RETURNS:
			case STATEMENT:
			case STDIN:
			case STDOUT:
			case TABLES:
			case SUPPORT:
			case STABLE:
			case TEMPLATE:
			case UNENCRYPTED:
			case VIEWS:
			case UNCOMMITTED:
			case TRANSFORM:
			case UNLISTEN:
			case TRUSTED:
			case VALIDATOR:
			case UNTIL:
			case VACUUM:
			case VOLATILE:
			case STORED:
			case WRITE:
			case STRICT:
			case TYPES:
			case WRAPPER:
			case WORK:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(2880);
				varName(0);
				}
				break;
			case ALL:
				enterOuterAlt(_localctx, 2);
				{
				setState(2881);
				match(ALL);
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

	public static class RelationExprListContext extends ParserRuleContext {
		public List<RelationExprContext> relationExpr() {
			return getRuleContexts(RelationExprContext.class);
		}
		public RelationExprContext relationExpr(int i) {
			return getRuleContext(RelationExprContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public RelationExprListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationExprList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRelationExprList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRelationExprList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRelationExprList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationExprListContext relationExprList() throws RecognitionException {
		RelationExprListContext _localctx = new RelationExprListContext(_ctx, getState());
		enterRule(_localctx, 442, RULE_relationExprList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2884);
			relationExpr();
			setState(2889);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2885);
				match(COMMA_);
				setState(2886);
				relationExpr();
				}
				}
				setState(2891);
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

	public static class RelationExprContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode ASTERISK_() { return getToken(BaseRuleParser.ASTERISK_, 0); }
		public TerminalNode ONLY() { return getToken(BaseRuleParser.ONLY, 0); }
		public TerminalNode LP_() { return getToken(BaseRuleParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(BaseRuleParser.RP_, 0); }
		public RelationExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRelationExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRelationExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRelationExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationExprContext relationExpr() throws RecognitionException {
		RelationExprContext _localctx = new RelationExprContext(_ctx, getState());
		enterRule(_localctx, 444, RULE_relationExpr);
		try {
			setState(2903);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2892);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2893);
				qualifiedName();
				setState(2894);
				match(ASTERISK_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2896);
				match(ONLY);
				setState(2897);
				qualifiedName();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2898);
				match(ONLY);
				setState(2899);
				match(LP_);
				setState(2900);
				qualifiedName();
				setState(2901);
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

	public static class CommonFuncOptItemContext extends ParserRuleContext {
		public TerminalNode CALLED() { return getToken(BaseRuleParser.CALLED, 0); }
		public TerminalNode ON() { return getToken(BaseRuleParser.ON, 0); }
		public List<TerminalNode> NULL() { return getTokens(BaseRuleParser.NULL); }
		public TerminalNode NULL(int i) {
			return getToken(BaseRuleParser.NULL, i);
		}
		public TerminalNode INPUT() { return getToken(BaseRuleParser.INPUT, 0); }
		public TerminalNode RETURNS() { return getToken(BaseRuleParser.RETURNS, 0); }
		public TerminalNode STRICT() { return getToken(BaseRuleParser.STRICT, 0); }
		public TerminalNode IMMUTABLE() { return getToken(BaseRuleParser.IMMUTABLE, 0); }
		public TerminalNode STABLE() { return getToken(BaseRuleParser.STABLE, 0); }
		public TerminalNode VOLATILE() { return getToken(BaseRuleParser.VOLATILE, 0); }
		public TerminalNode EXTERNAL() { return getToken(BaseRuleParser.EXTERNAL, 0); }
		public TerminalNode SECURITY() { return getToken(BaseRuleParser.SECURITY, 0); }
		public TerminalNode DEFINER() { return getToken(BaseRuleParser.DEFINER, 0); }
		public TerminalNode INVOKER() { return getToken(BaseRuleParser.INVOKER, 0); }
		public TerminalNode LEAKPROOF() { return getToken(BaseRuleParser.LEAKPROOF, 0); }
		public TerminalNode NOT() { return getToken(BaseRuleParser.NOT, 0); }
		public TerminalNode COST() { return getToken(BaseRuleParser.COST, 0); }
		public NumericOnlyContext numericOnly() {
			return getRuleContext(NumericOnlyContext.class,0);
		}
		public TerminalNode ROWS() { return getToken(BaseRuleParser.ROWS, 0); }
		public TerminalNode SUPPORT() { return getToken(BaseRuleParser.SUPPORT, 0); }
		public AnyNameContext anyName() {
			return getRuleContext(AnyNameContext.class,0);
		}
		public FunctionSetResetClauseContext functionSetResetClause() {
			return getRuleContext(FunctionSetResetClauseContext.class,0);
		}
		public TerminalNode PARALLEL() { return getToken(BaseRuleParser.PARALLEL, 0); }
		public ColIdContext colId() {
			return getRuleContext(ColIdContext.class,0);
		}
		public CommonFuncOptItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commonFuncOptItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterCommonFuncOptItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitCommonFuncOptItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitCommonFuncOptItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommonFuncOptItemContext commonFuncOptItem() throws RecognitionException {
		CommonFuncOptItemContext _localctx = new CommonFuncOptItemContext(_ctx, getState());
		enterRule(_localctx, 446, RULE_commonFuncOptItem);
		try {
			setState(2940);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2905);
				match(CALLED);
				setState(2906);
				match(ON);
				setState(2907);
				match(NULL);
				setState(2908);
				match(INPUT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2909);
				match(RETURNS);
				setState(2910);
				match(NULL);
				setState(2911);
				match(ON);
				setState(2912);
				match(NULL);
				setState(2913);
				match(INPUT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2914);
				match(STRICT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2915);
				match(IMMUTABLE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2916);
				match(STABLE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2917);
				match(VOLATILE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2918);
				match(EXTERNAL);
				setState(2919);
				match(SECURITY);
				setState(2920);
				match(DEFINER);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2921);
				match(EXTERNAL);
				setState(2922);
				match(SECURITY);
				setState(2923);
				match(INVOKER);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2924);
				match(SECURITY);
				setState(2925);
				match(DEFINER);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2926);
				match(SECURITY);
				setState(2927);
				match(INVOKER);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2928);
				match(LEAKPROOF);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2929);
				match(NOT);
				setState(2930);
				match(LEAKPROOF);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2931);
				match(COST);
				setState(2932);
				numericOnly();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(2933);
				match(ROWS);
				setState(2934);
				numericOnly();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(2935);
				match(SUPPORT);
				setState(2936);
				anyName();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(2937);
				functionSetResetClause();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(2938);
				match(PARALLEL);
				setState(2939);
				colId();
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

	public static class FunctionSetResetClauseContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(BaseRuleParser.SET, 0); }
		public SetRestMoreContext setRestMore() {
			return getRuleContext(SetRestMoreContext.class,0);
		}
		public VariableResetStmtContext variableResetStmt() {
			return getRuleContext(VariableResetStmtContext.class,0);
		}
		public FunctionSetResetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionSetResetClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterFunctionSetResetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitFunctionSetResetClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitFunctionSetResetClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionSetResetClauseContext functionSetResetClause() throws RecognitionException {
		FunctionSetResetClauseContext _localctx = new FunctionSetResetClauseContext(_ctx, getState());
		enterRule(_localctx, 448, RULE_functionSetResetClause);
		try {
			setState(2945);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SET:
				enterOuterAlt(_localctx, 1);
				{
				setState(2942);
				match(SET);
				setState(2943);
				setRestMore();
				}
				break;
			case RESET:
				enterOuterAlt(_localctx, 2);
				{
				setState(2944);
				variableResetStmt();
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

	public static class RowSecurityCmdContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(BaseRuleParser.ALL, 0); }
		public TerminalNode SELECT() { return getToken(BaseRuleParser.SELECT, 0); }
		public TerminalNode INSERT() { return getToken(BaseRuleParser.INSERT, 0); }
		public TerminalNode UPDATE() { return getToken(BaseRuleParser.UPDATE, 0); }
		public TerminalNode DELETE() { return getToken(BaseRuleParser.DELETE, 0); }
		public RowSecurityCmdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rowSecurityCmd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterRowSecurityCmd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitRowSecurityCmd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitRowSecurityCmd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RowSecurityCmdContext rowSecurityCmd() throws RecognitionException {
		RowSecurityCmdContext _localctx = new RowSecurityCmdContext(_ctx, getState());
		enterRule(_localctx, 450, RULE_rowSecurityCmd);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2947);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SELECT) | (1L << INSERT) | (1L << UPDATE) | (1L << DELETE))) != 0) || _la==ALL) ) {
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

	public static class EventContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(BaseRuleParser.SELECT, 0); }
		public TerminalNode UPDATE() { return getToken(BaseRuleParser.UPDATE, 0); }
		public TerminalNode DELETE() { return getToken(BaseRuleParser.DELETE, 0); }
		public TerminalNode INSERT() { return getToken(BaseRuleParser.INSERT, 0); }
		public EventContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_event; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterEvent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitEvent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitEvent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EventContext event() throws RecognitionException {
		EventContext _localctx = new EventContext(_ctx, getState());
		enterRule(_localctx, 452, RULE_event);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2949);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SELECT) | (1L << INSERT) | (1L << UPDATE) | (1L << DELETE))) != 0)) ) {
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

	public static class TypeNameListContext extends ParserRuleContext {
		public List<TypeNameContext> typeName() {
			return getRuleContexts(TypeNameContext.class);
		}
		public TypeNameContext typeName(int i) {
			return getRuleContext(TypeNameContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(BaseRuleParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(BaseRuleParser.COMMA_, i);
		}
		public TypeNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeNameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).enterTypeNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BaseRuleListener ) ((BaseRuleListener)listener).exitTypeNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof BaseRuleVisitor ) return ((BaseRuleVisitor<? extends T>)visitor).visitTypeNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeNameListContext typeNameList() throws RecognitionException {
		TypeNameListContext _localctx = new TypeNameListContext(_ctx, getState());
		enterRule(_localctx, 454, RULE_typeNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2951);
			typeName();
			setState(2956);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(2952);
				match(COMMA_);
				setState(2953);
				typeName();
				}
				}
				setState(2958);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 24:
			return aExpr_sempred((AExprContext)_localctx, predIndex);
		case 25:
			return bExpr_sempred((BExprContext)_localctx, predIndex);
		case 27:
			return indirection_sempred((IndirectionContext)_localctx, predIndex);
		case 28:
			return optIndirection_sempred((OptIndirectionContext)_localctx, predIndex);
		case 71:
			return xmlTableColumnOptionList_sempred((XmlTableColumnOptionListContext)_localctx, predIndex);
		case 83:
			return exprList_sempred((ExprListContext)_localctx, predIndex);
		case 96:
			return attrs_sempred((AttrsContext)_localctx, predIndex);
		case 104:
			return optArrayBounds_sempred((OptArrayBoundsContext)_localctx, predIndex);
		case 131:
			return nameList_sempred((NameListContext)_localctx, predIndex);
		case 169:
			return varName_sempred((VarNameContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean aExpr_sempred(AExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 48);
		case 1:
			return precpred(_ctx, 45);
		case 2:
			return precpred(_ctx, 44);
		case 3:
			return precpred(_ctx, 43);
		case 4:
			return precpred(_ctx, 42);
		case 5:
			return precpred(_ctx, 41);
		case 6:
			return precpred(_ctx, 40);
		case 7:
			return precpred(_ctx, 39);
		case 8:
			return precpred(_ctx, 36);
		case 9:
			return precpred(_ctx, 34);
		case 10:
			return precpred(_ctx, 33);
		case 11:
			return precpred(_ctx, 21);
		case 12:
			return precpred(_ctx, 20);
		case 13:
			return precpred(_ctx, 17);
		case 14:
			return precpred(_ctx, 16);
		case 15:
			return precpred(_ctx, 15);
		case 16:
			return precpred(_ctx, 14);
		case 17:
			return precpred(_ctx, 2);
		case 18:
			return precpred(_ctx, 50);
		case 19:
			return precpred(_ctx, 49);
		case 20:
			return precpred(_ctx, 37);
		case 21:
			return precpred(_ctx, 32);
		case 22:
			return precpred(_ctx, 31);
		case 23:
			return precpred(_ctx, 30);
		case 24:
			return precpred(_ctx, 29);
		case 25:
			return precpred(_ctx, 27);
		case 26:
			return precpred(_ctx, 26);
		case 27:
			return precpred(_ctx, 25);
		case 28:
			return precpred(_ctx, 24);
		case 29:
			return precpred(_ctx, 23);
		case 30:
			return precpred(_ctx, 22);
		case 31:
			return precpred(_ctx, 19);
		case 32:
			return precpred(_ctx, 18);
		case 33:
			return precpred(_ctx, 13);
		case 34:
			return precpred(_ctx, 12);
		case 35:
			return precpred(_ctx, 11);
		case 36:
			return precpred(_ctx, 10);
		case 37:
			return precpred(_ctx, 8);
		case 38:
			return precpred(_ctx, 7);
		case 39:
			return precpred(_ctx, 6);
		case 40:
			return precpred(_ctx, 5);
		case 41:
			return precpred(_ctx, 4);
		case 42:
			return precpred(_ctx, 3);
		}
		return true;
	}
	private boolean bExpr_sempred(BExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 43:
			return precpred(_ctx, 9);
		case 44:
			return precpred(_ctx, 6);
		case 45:
			return precpred(_ctx, 5);
		case 46:
			return precpred(_ctx, 12);
		case 47:
			return precpred(_ctx, 7);
		case 48:
			return precpred(_ctx, 4);
		case 49:
			return precpred(_ctx, 3);
		case 50:
			return precpred(_ctx, 2);
		case 51:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean indirection_sempred(IndirectionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 52:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean optIndirection_sempred(OptIndirectionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 53:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean xmlTableColumnOptionList_sempred(XmlTableColumnOptionListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 54:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean exprList_sempred(ExprListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 55:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean attrs_sempred(AttrsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 56:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean optArrayBounds_sempred(OptArrayBoundsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 57:
			return precpred(_ctx, 3);
		case 58:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean nameList_sempred(NameListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 59:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean varName_sempred(VarNameContext _localctx, int predIndex) {
		switch (predIndex) {
		case 60:
			return precpred(_ctx, 1);
		}
		return true;
	}

	private static final int _serializedATNSegments = 2;
	private static final String _serializedATNSegment0 =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0231\u0b92\4\2\t"+
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
		"\t\u00a0\4\u00a1\t\u00a1\4\u00a2\t\u00a2\4\u00a3\t\u00a3\4\u00a4\t\u00a4"+
		"\4\u00a5\t\u00a5\4\u00a6\t\u00a6\4\u00a7\t\u00a7\4\u00a8\t\u00a8\4\u00a9"+
		"\t\u00a9\4\u00aa\t\u00aa\4\u00ab\t\u00ab\4\u00ac\t\u00ac\4\u00ad\t\u00ad"+
		"\4\u00ae\t\u00ae\4\u00af\t\u00af\4\u00b0\t\u00b0\4\u00b1\t\u00b1\4\u00b2"+
		"\t\u00b2\4\u00b3\t\u00b3\4\u00b4\t\u00b4\4\u00b5\t\u00b5\4\u00b6\t\u00b6"+
		"\4\u00b7\t\u00b7\4\u00b8\t\u00b8\4\u00b9\t\u00b9\4\u00ba\t\u00ba\4\u00bb"+
		"\t\u00bb\4\u00bc\t\u00bc\4\u00bd\t\u00bd\4\u00be\t\u00be\4\u00bf\t\u00bf"+
		"\4\u00c0\t\u00c0\4\u00c1\t\u00c1\4\u00c2\t\u00c2\4\u00c3\t\u00c3\4\u00c4"+
		"\t\u00c4\4\u00c5\t\u00c5\4\u00c6\t\u00c6\4\u00c7\t\u00c7\4\u00c8\t\u00c8"+
		"\4\u00c9\t\u00c9\4\u00ca\t\u00ca\4\u00cb\t\u00cb\4\u00cc\t\u00cc\4\u00cd"+
		"\t\u00cd\4\u00ce\t\u00ce\4\u00cf\t\u00cf\4\u00d0\t\u00d0\4\u00d1\t\u00d1"+
		"\4\u00d2\t\u00d2\4\u00d3\t\u00d3\4\u00d4\t\u00d4\4\u00d5\t\u00d5\4\u00d6"+
		"\t\u00d6\4\u00d7\t\u00d7\4\u00d8\t\u00d8\4\u00d9\t\u00d9\4\u00da\t\u00da"+
		"\4\u00db\t\u00db\4\u00dc\t\u00dc\4\u00dd\t\u00dd\4\u00de\t\u00de\4\u00df"+
		"\t\u00df\4\u00e0\t\u00e0\4\u00e1\t\u00e1\4\u00e2\t\u00e2\4\u00e3\t\u00e3"+
		"\4\u00e4\t\u00e4\4\u00e5\t\u00e5\3\2\3\2\5\2\u01cd\n\2\3\3\3\3\3\4\5\4"+
		"\u01d2\n\4\3\4\3\4\5\4\u01d6\n\4\3\5\3\5\3\5\3\6\5\6\u01dc\n\6\3\6\3\6"+
		"\5\6\u01e0\n\6\3\6\5\6\u01e3\n\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3"+
		"\n\3\13\3\13\3\f\3\f\3\f\5\f\u01f4\n\f\3\f\3\f\3\r\3\r\3\r\5\r\u01fb\n"+
		"\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\5\20\u0204\n\20\3\20\3\20\3\20\7\20"+
		"\u0209\n\20\f\20\16\20\u020c\13\20\3\20\5\20\u020f\n\20\3\21\3\21\3\21"+
		"\3\21\7\21\u0215\n\21\f\21\16\21\u0218\13\21\3\21\3\21\3\22\3\22\5\22"+
		"\u021e\n\22\3\23\3\23\3\24\3\24\3\25\5\25\u0225\n\25\3\25\3\25\3\26\3"+
		"\26\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3"+
		"\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u0244"+
		"\n\30\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u025a\n\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u0293\n\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u029d\n\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\7\32\u0319\n\32\f\32\16\32\u031c\13\32\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u0327\n\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\7\33\u0353\n\33"+
		"\f\33\16\33\u0356\13\33\3\34\3\34\3\34\3\34\3\34\5\34\u035d\n\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u0377\n\34\3\35\3\35"+
		"\3\35\3\35\3\35\7\35\u037e\n\35\f\35\16\35\u0381\13\35\3\36\3\36\3\36"+
		"\7\36\u0386\n\36\f\36\16\36\u0389\13\36\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\3\37\3\37\3\37\5\37\u0395\n\37\3\37\3\37\5\37\u0399\n\37\3\37\5"+
		"\37\u039c\n\37\3 \3 \3!\3!\3!\3!\3!\5!\u03a5\n!\3\"\3\"\5\"\u03a9\n\""+
		"\3\"\3\"\5\"\u03ad\n\"\3\"\3\"\3#\6#\u03b2\n#\r#\16#\u03b3\3$\3$\3$\3"+
		"$\3$\3%\3%\3%\3&\3&\3\'\3\'\3\'\3\'\5\'\u03c4\n\'\3(\3(\3(\3(\3(\3(\5"+
		"(\u03cc\n(\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\5)\u03da\n)\3*\3*\5*\u03de"+
		"\n*\3+\6+\u03e1\n+\r+\16+\u03e2\3,\3,\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3"+
		"-\3-\3-\3-\3-\5-\u03f6\n-\3.\3.\3.\3.\3.\3.\5.\u03fe\n.\3/\3/\3\60\3\60"+
		"\3\60\3\60\3\60\5\60\u0407\n\60\3\61\3\61\3\61\5\61\u040c\n\61\3\62\3"+
		"\62\3\62\3\62\3\62\3\62\5\62\u0414\n\62\3\63\3\63\3\63\3\63\3\63\3\63"+
		"\3\63\3\63\3\63\3\63\3\63\3\63\5\63\u0422\n\63\3\64\3\64\3\64\3\64\3\64"+
		"\3\64\3\64\3\64\3\64\3\64\5\64\u042e\n\64\3\65\3\65\3\65\3\65\3\65\3\65"+
		"\3\65\3\65\3\65\3\65\3\65\3\65\3\65\3\65\5\65\u043e\n\65\3\66\3\66\3\66"+
		"\3\66\3\66\3\66\3\66\3\66\5\66\u0448\n\66\3\67\3\67\3\67\3\67\3\67\3\67"+
		"\38\38\39\39\39\39\39\39\39\39\39\39\59\u045c\n9\3:\3:\3:\7:\u0461\n:"+
		"\f:\16:\u0464\13:\3;\3;\3;\7;\u0469\n;\f;\16;\u046c\13;\3<\3<\3=\3=\3"+
		"=\3=\3=\3=\3=\3=\3=\5=\u0479\n=\3>\3>\3>\7>\u047e\n>\f>\16>\u0481\13>"+
		"\3?\3?\3?\3?\3?\3?\3?\3?\5?\u048b\n?\3?\3?\3?\3?\3?\3?\3?\5?\u0494\n?"+
		"\3?\3?\3?\3?\3?\3?\3?\3?\3?\5?\u049f\n?\3?\3?\3?\3?\3?\3?\3?\5?\u04a8"+
		"\n?\3?\3?\3?\3?\3?\3?\3?\5?\u04b1\n?\3?\3?\3?\3?\3?\3?\3?\5?\u04ba\n?"+
		"\3@\3@\3@\3@\5@\u04c0\n@\3A\3A\3A\3A\3A\3A\3A\3A\3A\5A\u04cb\nA\3A\3A"+
		"\3A\3A\3A\3A\5A\u04d3\nA\3B\3B\3B\3B\5B\u04d9\nB\3C\3C\3D\3D\3D\5D\u04e0"+
		"\nD\3E\3E\5E\u04e4\nE\3E\3E\3E\3E\3E\3E\5E\u04ec\nE\5E\u04ee\nE\3F\3F"+
		"\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\5F\u0505\nF"+
		"\3G\3G\3G\7G\u050a\nG\fG\16G\u050d\13G\3H\3H\3H\3H\3H\3H\3H\3H\3H\3H\3"+
		"H\5H\u051a\nH\3I\3I\3I\3I\3I\7I\u0521\nI\fI\16I\u0524\13I\3J\3J\3J\3J"+
		"\3J\3J\3J\3J\5J\u052e\nJ\3K\3K\3K\7K\u0533\nK\fK\16K\u0536\13K\3L\3L\3"+
		"L\3L\3L\3L\5L\u053e\nL\3M\3M\5M\u0542\nM\3M\5M\u0545\nM\3M\5M\u0548\n"+
		"M\3M\5M\u054b\nM\3N\3N\3N\3N\3N\3N\3O\3O\3O\3O\3O\3O\3P\3P\5P\u055b\n"+
		"P\3Q\3Q\3Q\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\5R\u058b\nR\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3"+
		"R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\5R\u0628"+
		"\nR\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\5R\u0634\nR\3S\3S\3S\3S\3S\3S\3S\3S"+
		"\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\5S\u0651\nS"+
		"\3T\3T\3T\3T\3T\3T\3T\3T\3T\3T\3T\3T\3T\5T\u0660\nT\3U\3U\3U\3U\3U\3U"+
		"\7U\u0668\nU\fU\16U\u066b\13U\3V\3V\3V\3V\3W\3W\3W\3W\3W\3W\3W\5W\u0678"+
		"\nW\3X\3X\5X\u067c\nX\3X\3X\3X\5X\u0681\nX\5X\u0683\nX\3Y\3Y\3Y\3Y\3Z"+
		"\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\5Z\u0694\nZ\3Z\3Z\5Z\u0698\nZ\3Z\3Z\5Z"+
		"\u069c\nZ\3Z\3Z\3Z\3Z\3Z\3Z\5Z\u06a4\nZ\3[\3[\3[\3[\3[\5[\u06ab\n[\3["+
		"\3[\5[\u06af\n[\3[\3[\3[\3[\3[\5[\u06b6\n[\3[\3[\5[\u06ba\n[\3[\5[\u06bd"+
		"\n[\3\\\3\\\3\\\3\\\3\\\3\\\5\\\u06c5\n\\\3]\3]\5]\u06c9\n]\3^\3^\3^\3"+
		"^\3^\3_\3_\3`\3`\5`\u06d4\n`\3`\3`\5`\u06d8\n`\3`\3`\3`\3`\5`\u06de\n"+
		"`\3`\3`\3`\5`\u06e3\n`\3`\3`\5`\u06e7\n`\5`\u06e9\n`\3a\3a\3a\3a\5a\u06ef"+
		"\na\3b\3b\3b\3b\3b\3b\3b\7b\u06f8\nb\fb\16b\u06fb\13b\3c\3c\3d\3d\3d\3"+
		"d\5d\u0703\nd\3e\3e\5e\u0707\ne\3f\3f\5f\u070b\nf\3f\3f\3f\3f\3g\3g\5"+
		"g\u0713\ng\3h\3h\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3"+
		"i\3i\3i\3i\3i\3i\3i\3i\3i\3i\3i\5i\u0733\ni\3j\3j\3j\3j\3j\3j\3j\3j\7"+
		"j\u073d\nj\fj\16j\u0740\13j\3k\3k\3k\3k\3k\5k\u0747\nk\3l\3l\3m\3m\3m"+
		"\3m\3m\3m\3m\5m\u0752\nm\3n\3n\3n\3n\3n\3n\3n\3n\3n\5n\u075d\nn\3o\3o"+
		"\3o\3p\3p\3p\3q\3q\3q\3r\3r\3r\3r\3r\5r\u076d\nr\3s\3s\3s\3s\3s\3s\3s"+
		"\3s\3s\3s\3s\3s\3s\3s\3s\3s\5s\u077f\ns\3t\3t\3t\3t\3t\3u\3u\3u\7u\u0789"+
		"\nu\fu\16u\u078c\13u\3v\3v\3v\3v\3v\5v\u0793\nv\3w\3w\3w\3w\3w\3w\3w\3"+
		"w\3w\3w\3w\3w\3w\3w\3w\5w\u07a4\nw\3x\3x\3x\3x\5x\u07aa\nx\3y\3y\3z\3"+
		"z\3z\3z\3z\5z\u07b3\nz\3{\3{\3{\3{\3{\5{\u07ba\n{\3|\3|\3|\3|\3|\3|\3"+
		"|\3|\3|\3|\5|\u07c6\n|\3}\3}\3}\3~\3~\3~\7~\u07ce\n~\f~\16~\u07d1\13~"+
		"\3\177\3\177\3\177\3\177\3\177\3\u0080\3\u0080\3\u0080\7\u0080\u07db\n"+
		"\u0080\f\u0080\16\u0080\u07de\13\u0080\3\u0081\3\u0081\3\u0081\5\u0081"+
		"\u07e3\n\u0081\3\u0082\3\u0082\5\u0082\u07e7\n\u0082\3\u0082\3\u0082\3"+
		"\u0083\3\u0083\3\u0083\3\u0083\5\u0083\u07ef\n\u0083\3\u0084\3\u0084\3"+
		"\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084"+
		"\3\u0084\3\u0084\3\u0084\5\u0084\u07ff\n\u0084\3\u0085\3\u0085\3\u0085"+
		"\3\u0085\3\u0085\3\u0085\7\u0085\u0807\n\u0085\f\u0085\16\u0085\u080a"+
		"\13\u0085\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086"+
		"\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086"+
		"\5\u0086\u081d\n\u0086\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087"+
		"\5\u0087\u0825\n\u0087\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0089"+
		"\3\u0089\3\u008a\3\u008a\3\u008a\3\u008a\3\u008b\3\u008b\3\u008b\7\u008b"+
		"\u0835\n\u008b\f\u008b\16\u008b\u0838\13\u008b\3\u008c\3\u008c\3\u008c"+
		"\3\u008c\5\u008c\u083e\n\u008c\3\u008c\3\u008c\5\u008c\u0842\n\u008c\3"+
		"\u008c\5\u008c\u0845\n\u008c\5\u008c\u0847\n\u008c\3\u008d\3\u008d\3\u008d"+
		"\3\u008d\5\u008d\u084d\n\u008d\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e"+
		"\3\u008e\3\u008e\5\u008e\u0856\n\u008e\3\u008f\3\u008f\3\u0090\3\u0090"+
		"\3\u0090\3\u0090\5\u0090\u085e\n\u0090\3\u0091\3\u0091\5\u0091\u0862\n"+
		"\u0091\3\u0091\5\u0091\u0865\n\u0091\3\u0091\5\u0091\u0868\n\u0091\3\u0091"+
		"\5\u0091\u086b\n\u0091\3\u0091\3\u0091\3\u0092\3\u0092\3\u0093\3\u0093"+
		"\3\u0093\3\u0093\3\u0094\3\u0094\3\u0094\7\u0094\u0878\n\u0094\f\u0094"+
		"\16\u0094\u087b\13\u0094\3\u0095\5\u0095\u087e\n\u0095\3\u0095\3\u0095"+
		"\5\u0095\u0882\n\u0095\3\u0095\5\u0095\u0885\n\u0095\3\u0095\5\u0095\u0888"+
		"\n\u0095\3\u0095\3\u0095\3\u0095\5\u0095\u088d\n\u0095\3\u0095\5\u0095"+
		"\u0890\n\u0095\5\u0095\u0892\n\u0095\3\u0096\3\u0096\3\u0096\3\u0096\3"+
		"\u0096\3\u0096\3\u0096\3\u0096\3\u0096\3\u0096\3\u0096\5\u0096\u089f\n"+
		"\u0096\3\u0097\3\u0097\3\u0097\3\u0098\3\u0098\5\u0098\u08a6\n\u0098\3"+
		"\u0099\3\u0099\3\u0099\3\u0099\3\u009a\3\u009a\3\u009a\7\u009a\u08af\n"+
		"\u009a\f\u009a\16\u009a\u08b2\13\u009a\3\u009b\3\u009b\3\u009b\3\u009b"+
		"\3\u009b\3\u009b\3\u009b\3\u009b\3\u009b\3\u009b\3\u009b\3\u009b\3\u009b"+
		"\3\u009b\3\u009b\5\u009b\u08c3\n\u009b\3\u009c\3\u009c\3\u009c\3\u009c"+
		"\3\u009c\3\u009c\5\u009c\u08cb\n\u009c\3\u009d\3\u009d\3\u009d\3\u009d"+
		"\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d\5\u009d"+
		"\u08d9\n\u009d\3\u009e\3\u009e\3\u009f\3\u009f\5\u009f\u08df\n\u009f\3"+
		"\u009f\5\u009f\u08e2\n\u009f\3\u009f\5\u009f\u08e5\n\u009f\3\u009f\3\u009f"+
		"\3\u009f\3\u009f\3\u009f\7\u009f\u08ec\n\u009f\f\u009f\16\u009f\u08ef"+
		"\13\u009f\3\u009f\3\u009f\5\u009f\u08f3\n\u009f\3\u009f\5\u009f\u08f6"+
		"\n\u009f\5\u009f\u08f8\n\u009f\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\3\u00a0\5\u00a0"+
		"\u0934\n\u00a0\3\u00a1\3\u00a1\3\u00a1\3\u00a1\5\u00a1\u093a\n\u00a1\3"+
		"\u00a1\3\u00a1\3\u00a2\3\u00a2\3\u00a2\5\u00a2\u0941\n\u00a2\3\u00a2\3"+
		"\u00a2\3\u00a3\3\u00a3\3\u00a3\5\u00a3\u0948\n\u00a3\3\u00a4\3\u00a4\3"+
		"\u00a4\7\u00a4\u094d\n\u00a4\f\u00a4\16\u00a4\u0950\13\u00a4\3\u00a5\3"+
		"\u00a5\3\u00a5\3\u00a5\3\u00a5\5\u00a5\u0957\n\u00a5\3\u00a6\3\u00a6\3"+
		"\u00a6\3\u00a6\3\u00a6\5\u00a6\u095e\n\u00a6\3\u00a7\3\u00a7\3\u00a7\3"+
		"\u00a7\5\u00a7\u0964\n\u00a7\3\u00a8\3\u00a8\3\u00a9\3\u00a9\3\u00aa\3"+
		"\u00aa\3\u00aa\3\u00aa\5\u00aa\u096e\n\u00aa\3\u00ab\3\u00ab\3\u00ab\3"+
		"\u00ab\3\u00ab\3\u00ab\7\u00ab\u0976\n\u00ab\f\u00ab\16\u00ab\u0979\13"+
		"\u00ab\3\u00ac\3\u00ac\3\u00ac\7\u00ac\u097e\n\u00ac\f\u00ac\16\u00ac"+
		"\u0981\13\u00ac\3\u00ad\3\u00ad\5\u00ad\u0985\n\u00ad\3\u00ae\3\u00ae"+
		"\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae"+
		"\3\u00ae\3\u00ae\5\u00ae\u0994\n\u00ae\3\u00af\3\u00af\3\u00af\3\u00af"+
		"\3\u00af\5\u00af\u099b\n\u00af\3\u00b0\3\u00b0\3\u00b0\3\u00b0\3\u00b0"+
		"\3\u00b0\3\u00b0\5\u00b0\u09a4\n\u00b0\3\u00b1\3\u00b1\3\u00b1\5\u00b1"+
		"\u09a9\n\u00b1\3\u00b1\3\u00b1\3\u00b2\7\u00b2\u09ae\n\u00b2\f\u00b2\16"+
		"\u00b2\u09b1\13\u00b2\3\u00b3\3\u00b3\3\u00b3\3\u00b3\3\u00b3\3\u00b3"+
		"\3\u00b3\3\u00b3\5\u00b3\u09bb\n\u00b3\3\u00b4\3\u00b4\3\u00b4\3\u00b4"+
		"\3\u00b4\3\u00b4\3\u00b4\5\u00b4\u09c4\n\u00b4\3\u00b5\3\u00b5\3\u00b5"+
		"\3\u00b5\3\u00b5\3\u00b5\5\u00b5\u09cc\n\u00b5\3\u00b5\3\u00b5\3\u00b5"+
		"\3\u00b5\3\u00b5\5\u00b5\u09d3\n\u00b5\3\u00b5\3\u00b5\3\u00b5\3\u00b5"+
		"\3\u00b5\3\u00b5\5\u00b5\u09db\n\u00b5\3\u00b5\3\u00b5\3\u00b5\3\u00b5"+
		"\3\u00b5\3\u00b5\3\u00b5\5\u00b5\u09e4\n\u00b5\3\u00b5\3\u00b5\3\u00b5"+
		"\3\u00b5\3\u00b5\3\u00b5\3\u00b5\3\u00b5\3\u00b5\3\u00b5\3\u00b5\5\u00b5"+
		"\u09f1\n\u00b5\3\u00b5\5\u00b5\u09f4\n\u00b5\3\u00b5\5\u00b5\u09f7\n\u00b5"+
		"\5\u00b5\u09f9\n\u00b5\3\u00b6\3\u00b6\3\u00b6\3\u00b6\3\u00b7\6\u00b7"+
		"\u0a00\n\u00b7\r\u00b7\16\u00b7\u0a01\3\u00b8\3\u00b8\3\u00b8\3\u00b8"+
		"\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\5\u00b8\u0a0d\n\u00b8\3\u00b8"+
		"\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8"+
		"\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\5\u00b8\u0a20"+
		"\n\u00b8\3\u00b8\3\u00b8\3\u00b8\3\u00b8\5\u00b8\u0a26\n\u00b8\3\u00b8"+
		"\5\u00b8\u0a29\n\u00b8\3\u00b9\3\u00b9\3\u00b9\3\u00b9\3\u00ba\3\u00ba"+
		"\3\u00bb\3\u00bb\3\u00bb\7\u00bb\u0a34\n\u00bb\f\u00bb\16\u00bb\u0a37"+
		"\13\u00bb\3\u00bc\3\u00bc\3\u00bc\5\u00bc\u0a3c\n\u00bc\3\u00bd\3\u00bd"+
		"\3\u00bd\3\u00be\3\u00be\3\u00be\3\u00be\3\u00be\3\u00bf\3\u00bf\3\u00bf"+
		"\3\u00bf\3\u00c0\3\u00c0\3\u00c0\7\u00c0\u0a4d\n\u00c0\f\u00c0\16\u00c0"+
		"\u0a50\13\u00c0\3\u00c1\3\u00c1\3\u00c1\3\u00c1\3\u00c1\5\u00c1\u0a57"+
		"\n\u00c1\3\u00c2\3\u00c2\3\u00c2\3\u00c2\3\u00c2\5\u00c2\u0a5e\n\u00c2"+
		"\3\u00c3\3\u00c3\3\u00c3\3\u00c3\3\u00c3\3\u00c3\3\u00c3\3\u00c3\5\u00c3"+
		"\u0a68\n\u00c3\3\u00c4\3\u00c4\3\u00c4\3\u00c4\3\u00c5\3\u00c5\3\u00c5"+
		"\3\u00c5\3\u00c6\3\u00c6\3\u00c6\3\u00c6\3\u00c6\3\u00c6\3\u00c6\3\u00c6"+
		"\5\u00c6\u0a7a\n\u00c6\3\u00c7\3\u00c7\3\u00c7\3\u00c7\3\u00c7\3\u00c7"+
		"\5\u00c7\u0a82\n\u00c7\3\u00c8\3\u00c8\3\u00c8\3\u00c8\3\u00c8\3\u00c9"+
		"\3\u00c9\3\u00c9\7\u00c9\u0a8c\n\u00c9\f\u00c9\16\u00c9\u0a8f\13\u00c9"+
		"\3\u00ca\3\u00ca\3\u00ca\3\u00cb\3\u00cb\3\u00cc\3\u00cc\3\u00cd\3\u00cd"+
		"\3\u00cd\3\u00cd\3\u00cd\3\u00cd\5\u00cd\u0a9e\n\u00cd\3\u00ce\3\u00ce"+
		"\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce"+
		"\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce\3\u00ce"+
		"\3\u00ce\3\u00ce\5\u00ce\u0ab6\n\u00ce\3\u00cf\3\u00cf\3\u00cf\3\u00cf"+
		"\3\u00cf\3\u00cf\3\u00cf\3\u00cf\3\u00cf\3\u00cf\3\u00cf\3\u00cf\3\u00cf"+
		"\3\u00cf\3\u00cf\5\u00cf\u0ac7\n\u00cf\3\u00d0\3\u00d0\3\u00d0\3\u00d0"+
		"\3\u00d0\3\u00d0\5\u00d0\u0acf\n\u00d0\3\u00d1\3\u00d1\3\u00d1\7\u00d1"+
		"\u0ad4\n\u00d1\f\u00d1\16\u00d1\u0ad7\13\u00d1\3\u00d2\3\u00d2\5\u00d2"+
		"\u0adb\n\u00d2\3\u00d3\3\u00d3\3\u00d4\3\u00d4\3\u00d4\7\u00d4\u0ae2\n"+
		"\u00d4\f\u00d4\16\u00d4\u0ae5\13\u00d4\3\u00d5\3\u00d5\3\u00d5\5\u00d5"+
		"\u0aea\n\u00d5\3\u00d6\3\u00d6\3\u00d6\3\u00d6\3\u00d6\3\u00d6\3\u00d6"+
		"\3\u00d6\5\u00d6\u0af4\n\u00d6\3\u00d7\3\u00d7\5\u00d7\u0af8\n\u00d7\3"+
		"\u00d7\7\u00d7\u0afb\n\u00d7\f\u00d7\16\u00d7\u0afe\13\u00d7\3\u00d8\3"+
		"\u00d8\3\u00d8\3\u00d8\3\u00d8\3\u00d8\3\u00d8\3\u00d8\3\u00d8\3\u00d8"+
		"\5\u00d8\u0b0a\n\u00d8\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9"+
		"\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\5\u00d9"+
		"\u0b1a\n\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9"+
		"\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9\3\u00d9"+
		"\5\u00d9\u0b2c\n\u00d9\3\u00da\3\u00da\3\u00db\3\u00db\3\u00db\3\u00db"+
		"\5\u00db\u0b34\n\u00db\3\u00dc\3\u00dc\3\u00dc\3\u00dd\3\u00dd\3\u00dd"+
		"\3\u00dd\3\u00dd\3\u00dd\3\u00dd\3\u00dd\5\u00dd\u0b41\n\u00dd\3\u00de"+
		"\3\u00de\5\u00de\u0b45\n\u00de\3\u00df\3\u00df\3\u00df\7\u00df\u0b4a\n"+
		"\u00df\f\u00df\16\u00df\u0b4d\13\u00df\3\u00e0\3\u00e0\3\u00e0\3\u00e0"+
		"\3\u00e0\3\u00e0\3\u00e0\3\u00e0\3\u00e0\3\u00e0\3\u00e0\5\u00e0\u0b5a"+
		"\n\u00e0\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1"+
		"\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1"+
		"\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1"+
		"\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e1"+
		"\5\u00e1\u0b7f\n\u00e1\3\u00e2\3\u00e2\3\u00e2\5\u00e2\u0b84\n\u00e2\3"+
		"\u00e3\3\u00e3\3\u00e4\3\u00e4\3\u00e5\3\u00e5\3\u00e5\7\u00e5\u0b8d\n"+
		"\u00e5\f\u00e5\16\u00e5\u0b90\13\u00e5\3\u00e5\2\f\62\648:\u0090\u00a8"+
		"\u00c2\u00d2\u0108\u0154\u00e6\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36"+
		" \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082"+
		"\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a"+
		"\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2"+
		"\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca"+
		"\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2"+
		"\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa"+
		"\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108\u010a\u010c\u010e\u0110\u0112"+
		"\u0114\u0116\u0118\u011a\u011c\u011e\u0120\u0122\u0124\u0126\u0128\u012a"+
		"\u012c\u012e\u0130\u0132\u0134\u0136\u0138\u013a\u013c\u013e\u0140\u0142"+
		"\u0144\u0146\u0148\u014a\u014c\u014e\u0150\u0152\u0154\u0156\u0158\u015a"+
		"\u015c\u015e\u0160\u0162\u0164\u0166\u0168\u016a\u016c\u016e\u0170\u0172"+
		"\u0174\u0176\u0178\u017a\u017c\u017e\u0180\u0182\u0184\u0186\u0188\u018a"+
		"\u018c\u018e\u0190\u0192\u0194\u0196\u0198\u019a\u019c\u019e\u01a0\u01a2"+
		"\u01a4\u01a6\u01a8\u01aa\u01ac\u01ae\u01b0\u01b2\u01b4\u01b6\u01b8\u01ba"+
		"\u01bc\u01be\u01c0\u01c2\u01c4\u01c6\u01c8\2\26)\2\7\7\13\13\20\20\24"+
		"\25\27\32\"\"$),,\658:?ADGIKLNR[[`akkrrtt\u0087\u0089\u008f\u0091\u0093"+
		"\u0094\u0096\u0096\u009a\u009a\u009d\u009d\u00a0\u00a0\u00ac\u00ac\u00b4"+
		"\u00b4\u00b9\u00b9\u00be\u00be\u00c7\u00c7\u00dd\u00dd\u00eb\u00eb\u00fd"+
		"\u00fd\u0138\u0139\u015b\u015b\u015e\u015f\u0168\u016a\u016c\u016d\3\2"+
		"\3\4:\2\b\n\f\17\21\23\26\26\33\33\36!99MMSVXXbbddfiloqqssuxz{}}\u0081"+
		"\u0081\u0085\u0086\u008b\u008b\u008e\u008e\u0095\u0095\u0097\u0097\u0099"+
		"\u0099\u009b\u009b\u009e\u009e\u00a2\u00a2\u00a4\u00a5\u00a7\u00a7\u00a9"+
		"\u00ab\u00ad\u00b3\u00b5\u00b8\u00ba\u00bd\u00bf\u00bf\u00c2\u00c6\u00c8"+
		"\u00cc\u00ce\u00cf\u00d1\u00dc\u00de\u00e2\u00e4\u00ea\u00ec\u00ef\u00f1"+
		"\u00fc\u00fe\u0106\u0108\u0110\u011e\u011e\u012e\u012f\u0137\u0137\u0147"+
		"\u014a\u014c\u0150\u0153\u0156\u015c\u015d\u0160\u0161\u0163\u0166\u016e"+
		"\u01ee\r\2-\64@@JJ||\u009c\u009c\u00a3\u00a3\u00c0\u00c0\u0136\u0136\u013a"+
		"\u013a\u0157\u015a\u01ef\u01f1\4\2>?\u01f5\u01f6\4\2\u0209\u0209\u020b"+
		"\u020f\3\2\u01f5\u0220\6\2\u01fd\u01fe\u0200\u0203\u0209\u0209\u020b\u020f"+
		"\3\2NO\5\2\u00b6\u00b6\u00f2\u00f2\u010f\u010f\4\2HI\u009d\u009d\3\2\u0147"+
		"\u014a\4\2\u015c\u015c\u0164\u0164\4\2&&HH\3\2YZ\36\2\34\35##*+EFWWYZ"+
		"\\\\^_~\u0080\u0082\u0084\u008a\u008a\u008c\u008d\u0092\u0092\u0098\u0098"+
		"\u009f\u009f\u00a8\u00a8\u00d0\u00d0\u0111\u0111\u0115\u0115\u011c\u011c"+
		"\u012a\u012a\u013c\u0146\u014b\u014b\u0151\u0152\u0162\u0162\u0167\u0167"+
		"\u016b\u016b\u01f3\u01f4\4\2kk\u022e\u022e\4\2==\u0209\u0209\4\2\7\nH"+
		"H\3\2\7\n\2\u0d11\2\u01ca\3\2\2\2\4\u01ce\3\2\2\2\6\u01d1\3\2\2\2\b\u01d7"+
		"\3\2\2\2\n\u01e2\3\2\2\2\f\u01e4\3\2\2\2\16\u01e7\3\2\2\2\20\u01ea\3\2"+
		"\2\2\22\u01ec\3\2\2\2\24\u01ee\3\2\2\2\26\u01f3\3\2\2\2\30\u01fa\3\2\2"+
		"\2\32\u01fe\3\2\2\2\34\u0200\3\2\2\2\36\u0203\3\2\2\2 \u0210\3\2\2\2\""+
		"\u021d\3\2\2\2$\u021f\3\2\2\2&\u0221\3\2\2\2(\u0224\3\2\2\2*\u0228\3\2"+
		"\2\2,\u022a\3\2\2\2.\u0243\3\2\2\2\60\u0245\3\2\2\2\62\u0259\3\2\2\2\64"+
		"\u0326\3\2\2\2\66\u0376\3\2\2\28\u0378\3\2\2\2:\u0382\3\2\2\2<\u039b\3"+
		"\2\2\2>\u039d\3\2\2\2@\u03a4\3\2\2\2B\u03a6\3\2\2\2D\u03b1\3\2\2\2F\u03b5"+
		"\3\2\2\2H\u03ba\3\2\2\2J\u03bd\3\2\2\2L\u03c3\3\2\2\2N\u03cb\3\2\2\2P"+
		"\u03d9\3\2\2\2R\u03dd\3\2\2\2T\u03e0\3\2\2\2V\u03e4\3\2\2\2X\u03f5\3\2"+
		"\2\2Z\u03fd\3\2\2\2\\\u03ff\3\2\2\2^\u0406\3\2\2\2`\u0408\3\2\2\2b\u0413"+
		"\3\2\2\2d\u0421\3\2\2\2f\u042d\3\2\2\2h\u043d\3\2\2\2j\u0447\3\2\2\2l"+
		"\u0449\3\2\2\2n\u044f\3\2\2\2p\u045b\3\2\2\2r\u045d\3\2\2\2t\u0465\3\2"+
		"\2\2v\u046d\3\2\2\2x\u0478\3\2\2\2z\u047a\3\2\2\2|\u04b9\3\2\2\2~\u04bf"+
		"\3\2\2\2\u0080\u04d2\3\2\2\2\u0082\u04d8\3\2\2\2\u0084\u04da\3\2\2\2\u0086"+
		"\u04df\3\2\2\2\u0088\u04ed\3\2\2\2\u008a\u0504\3\2\2\2\u008c\u0506\3\2"+
		"\2\2\u008e\u0519\3\2\2\2\u0090\u051b\3\2\2\2\u0092\u052d\3\2\2\2\u0094"+
		"\u052f\3\2\2\2\u0096\u053d\3\2\2\2\u0098\u054a\3\2\2\2\u009a\u054c\3\2"+
		"\2\2\u009c\u0552\3\2\2\2\u009e\u055a\3\2\2\2\u00a0\u055c\3\2\2\2\u00a2"+
		"\u0633\3\2\2\2\u00a4\u0650\3\2\2\2\u00a6\u065f\3\2\2\2\u00a8\u0661\3\2"+
		"\2\2\u00aa\u066c\3\2\2\2\u00ac\u0677\3\2\2\2\u00ae\u0682\3\2\2\2\u00b0"+
		"\u0684\3\2\2\2\u00b2\u06a3\3\2\2\2\u00b4\u06bc\3\2\2\2\u00b6\u06c4\3\2"+
		"\2\2\u00b8\u06c8\3\2\2\2\u00ba\u06ca\3\2\2\2\u00bc\u06cf\3\2\2\2\u00be"+
		"\u06e8\3\2\2\2\u00c0\u06ee\3\2\2\2\u00c2\u06f0\3\2\2\2\u00c4\u06fc\3\2"+
		"\2\2\u00c6\u0702\3\2\2\2\u00c8\u0706\3\2\2\2\u00ca\u0708\3\2\2\2\u00cc"+
		"\u0710\3\2\2\2\u00ce\u0714\3\2\2\2\u00d0\u0732\3\2\2\2\u00d2\u0734\3\2"+
		"\2\2\u00d4\u0746\3\2\2\2\u00d6\u0748\3\2\2\2\u00d8\u0751\3\2\2\2\u00da"+
		"\u075c\3\2\2\2\u00dc\u075e\3\2\2\2\u00de\u0761\3\2\2\2\u00e0\u0764\3\2"+
		"\2\2\u00e2\u076c\3\2\2\2\u00e4\u077e\3\2\2\2\u00e6\u0780\3\2\2\2\u00e8"+
		"\u0785\3\2\2\2\u00ea\u0792\3\2\2\2\u00ec\u07a3\3\2\2\2\u00ee\u07a9\3\2"+
		"\2\2\u00f0\u07ab\3\2\2\2\u00f2\u07b2\3\2\2\2\u00f4\u07b9\3\2\2\2\u00f6"+
		"\u07c5\3\2\2\2\u00f8\u07c7\3\2\2\2\u00fa\u07ca\3\2\2\2\u00fc\u07d2\3\2"+
		"\2\2\u00fe\u07d7\3\2\2\2\u0100\u07df\3\2\2\2\u0102\u07e4\3\2\2\2\u0104"+
		"\u07ee\3\2\2\2\u0106\u07fe\3\2\2\2\u0108\u0800\3\2\2\2\u010a\u081c\3\2"+
		"\2\2\u010c\u081e\3\2\2\2\u010e\u0826\3\2\2\2\u0110\u082b\3\2\2\2\u0112"+
		"\u082d\3\2\2\2\u0114\u0831\3\2\2\2\u0116\u0846\3\2\2\2\u0118\u084c\3\2"+
		"\2\2\u011a\u0855\3\2\2\2\u011c\u0857\3\2\2\2\u011e\u085d\3\2\2\2\u0120"+
		"\u085f\3\2\2\2\u0122\u086e\3\2\2\2\u0124\u0870\3\2\2\2\u0126\u0874\3\2"+
		"\2\2\u0128\u0891\3\2\2\2\u012a\u089e\3\2\2\2\u012c\u08a0\3\2\2\2\u012e"+
		"\u08a5\3\2\2\2\u0130\u08a7\3\2\2\2\u0132\u08ab\3\2\2\2\u0134\u08c2\3\2"+
		"\2\2\u0136\u08ca\3\2\2\2\u0138\u08d8\3\2\2\2\u013a\u08da\3\2\2\2\u013c"+
		"\u08f7\3\2\2\2\u013e\u0933\3\2\2\2\u0140\u0935\3\2\2\2\u0142\u093d\3\2"+
		"\2\2\u0144\u0944\3\2\2\2\u0146\u0949\3\2\2\2\u0148\u0956\3\2\2\2\u014a"+
		"\u095d\3\2\2\2\u014c\u0963\3\2\2\2\u014e\u0965\3\2\2\2\u0150\u0967\3\2"+
		"\2\2\u0152\u096d\3\2\2\2\u0154\u096f\3\2\2\2\u0156\u097a\3\2\2\2\u0158"+
		"\u0984\3\2\2\2\u015a\u0993\3\2\2\2\u015c\u099a\3\2\2\2\u015e\u09a3\3\2"+
		"\2\2\u0160\u09a5\3\2\2\2\u0162\u09af\3\2\2\2\u0164\u09ba\3\2\2\2\u0166"+
		"\u09c3\3\2\2\2\u0168\u09f8\3\2\2\2\u016a\u09fa\3\2\2\2\u016c\u09ff\3\2"+
		"\2\2\u016e\u0a28\3\2\2\2\u0170\u0a2a\3\2\2\2\u0172\u0a2e\3\2\2\2\u0174"+
		"\u0a30\3\2\2\2\u0176\u0a3b\3\2\2\2\u0178\u0a3d\3\2\2\2\u017a\u0a40\3\2"+
		"\2\2\u017c\u0a45\3\2\2\2\u017e\u0a49\3\2\2\2\u0180\u0a56\3\2\2\2\u0182"+
		"\u0a5d\3\2\2\2\u0184\u0a67\3\2\2\2\u0186\u0a69\3\2\2\2\u0188\u0a6d\3\2"+
		"\2\2\u018a\u0a79\3\2\2\2\u018c\u0a81\3\2\2\2\u018e\u0a83\3\2\2\2\u0190"+
		"\u0a88\3\2\2\2\u0192\u0a90\3\2\2\2\u0194\u0a93\3\2\2\2\u0196\u0a95\3\2"+
		"\2\2\u0198\u0a9d\3\2\2\2\u019a\u0ab5\3\2\2\2\u019c\u0ac6\3\2\2\2\u019e"+
		"\u0ace\3\2\2\2\u01a0\u0ad0\3\2\2\2\u01a2\u0ada\3\2\2\2\u01a4\u0adc\3\2"+
		"\2\2\u01a6\u0ade\3\2\2\2\u01a8\u0ae9\3\2\2\2\u01aa\u0af3\3\2\2\2\u01ac"+
		"\u0af5\3\2\2\2\u01ae\u0b09\3\2\2\2\u01b0\u0b2b\3\2\2\2\u01b2\u0b2d\3\2"+
		"\2\2\u01b4\u0b2f\3\2\2\2\u01b6\u0b35\3\2\2\2\u01b8\u0b40\3\2\2\2\u01ba"+
		"\u0b44\3\2\2\2\u01bc\u0b46\3\2\2\2\u01be\u0b59\3\2\2\2\u01c0\u0b7e\3\2"+
		"\2\2\u01c2\u0b83\3\2\2\2\u01c4\u0b85\3\2\2\2\u01c6\u0b87\3\2\2\2\u01c8"+
		"\u0b89\3\2\2\2\u01ca\u01cc\7\u021b\2\2\u01cb\u01cd\5\b\5\2\u01cc\u01cb"+
		"\3\2\2\2\u01cc\u01cd\3\2\2\2\u01cd\3\3\2\2\2\u01ce\u01cf\t\2\2\2\u01cf"+
		"\5\3\2\2\2\u01d0\u01d2\7\u0201\2\2\u01d1\u01d0\3\2\2\2\u01d1\u01d2\3\2"+
		"\2\2\u01d2\u01d3\3\2\2\2\u01d3\u01d5\7\u022f\2\2\u01d4\u01d6\5\b\5\2\u01d5"+
		"\u01d4\3\2\2\2\u01d5\u01d6\3\2\2\2\u01d6\7\3\2\2\2\u01d7\u01d8\7\u0220"+
		"\2\2\u01d8\u01d9\7\u022d\2\2\u01d9\t\3\2\2\2\u01da\u01dc\5\f\7\2\u01db"+
		"\u01da\3\2\2\2\u01db\u01dc\3\2\2\2\u01dc\u01dd\3\2\2\2\u01dd\u01df\7\u022d"+
		"\2\2\u01de\u01e0\5\16\b\2\u01df\u01de\3\2\2\2\u01df\u01e0\3\2\2\2\u01e0"+
		"\u01e3\3\2\2\2\u01e1\u01e3\5\20\t\2\u01e2\u01db\3\2\2\2\u01e2\u01e1\3"+
		"\2\2\2\u01e3\13\3\2\2\2\u01e4\u01e5\t\3\2\2\u01e5\u01e6\7\u01fa\2\2\u01e6"+
		"\r\3\2\2\2\u01e7\u01e8\7\u010e\2\2\u01e8\u01e9\7\u022e\2\2\u01e9\17\3"+
		"\2\2\2\u01ea\u01eb\t\4\2\2\u01eb\21\3\2\2\2\u01ec\u01ed\t\5\2\2\u01ed"+
		"\23\3\2\2\2\u01ee\u01ef\5\n\6\2\u01ef\25\3\2\2\2\u01f0\u01f1\5\32\16\2"+
		"\u01f1\u01f2\7\u0205\2\2\u01f2\u01f4\3\2\2\2\u01f3\u01f0\3\2\2\2\u01f3"+
		"\u01f4\3\2\2\2\u01f4\u01f5\3\2\2\2\u01f5\u01f6\5\34\17\2\u01f6\27\3\2"+
		"\2\2\u01f7\u01f8\5\32\16\2\u01f8\u01f9\7\u0205\2\2\u01f9\u01fb\3\2\2\2"+
		"\u01fa\u01f7\3\2\2\2\u01fa\u01fb\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc\u01fd"+
		"\5\34\17\2\u01fd\31\3\2\2\2\u01fe\u01ff\5\n\6\2\u01ff\33\3\2\2\2\u0200"+
		"\u0201\5\n\6\2\u0201\35\3\2\2\2\u0202\u0204\7\u0211\2\2\u0203\u0202\3"+
		"\2\2\2\u0203\u0204\3\2\2\2\u0204\u0205\3\2\2\2\u0205\u020a\5\26\f\2\u0206"+
		"\u0207\7\u0217\2\2\u0207\u0209\5\26\f\2\u0208\u0206\3\2\2\2\u0209\u020c"+
		"\3\2\2\2\u020a\u0208\3\2\2\2\u020a\u020b\3\2\2\2\u020b\u020e\3\2\2\2\u020c"+
		"\u020a\3\2\2\2\u020d\u020f\7\u0212\2\2\u020e\u020d\3\2\2\2\u020e\u020f"+
		"\3\2\2\2\u020f\37\3\2\2\2\u0210\u0211\7\u0211\2\2\u0211\u0216\5\30\r\2"+
		"\u0212\u0213\7\u0217\2\2\u0213\u0215\5\30\r\2\u0214\u0212\3\2\2\2\u0215"+
		"\u0218\3\2\2\2\u0216\u0214\3\2\2\2\u0216\u0217\3\2\2\2\u0217\u0219\3\2"+
		"\2\2\u0218\u0216\3\2\2\2\u0219\u021a\7\u0212\2\2\u021a!\3\2\2\2\u021b"+
		"\u021e\7\u022e\2\2\u021c\u021e\5\n\6\2\u021d\u021b\3\2\2\2\u021d\u021c"+
		"\3\2\2\2\u021e#\3\2\2\2\u021f\u0220\5\n\6\2\u0220%\3\2\2\2\u0221\u0222"+
		"\5\n\6\2\u0222\'\3\2\2\2\u0223\u0225\7\30\2\2\u0224\u0223\3\2\2\2\u0224"+
		"\u0225\3\2\2\2\u0225\u0226\3\2\2\2\u0226\u0227\7\33\2\2\u0227)\3\2\2\2"+
		"\u0228\u0229\t\6\2\2\u0229+\3\2\2\2\u022a\u022b\t\7\2\2\u022b-\3\2\2\2"+
		"\u022c\u0244\7J\2\2\u022d\u0244\7\u021e\2\2\u022e\u022f\7A\2\2\u022f\u0244"+
		"\7J\2\2\u0230\u0244\7\u021f\2\2\u0231\u0244\7\u0157\2\2\u0232\u0244\7"+
		"\u0221\2\2\u0233\u0234\7A\2\2\u0234\u0244\7\u0157\2\2\u0235\u0244\7\u0222"+
		"\2\2\u0236\u0237\7\u0158\2\2\u0237\u0244\7=\2\2\u0238\u0239\7A\2\2\u0239"+
		"\u023a\7\u0158\2\2\u023a\u0244\7=\2\2\u023b\u0244\7\u01f8\2\2\u023c\u023d"+
		"\7\u01f7\2\2\u023d\u0244\7\u01f8\2\2\u023e\u023f\7\u01f8\2\2\u023f\u0244"+
		"\7\u0202\2\2\u0240\u0241\7\u01f7\2\2\u0241\u0242\7\u01f8\2\2\u0242\u0244"+
		"\7\u0202\2\2\u0243\u022c\3\2\2\2\u0243\u022d\3\2\2\2\u0243\u022e\3\2\2"+
		"\2\u0243\u0230\3\2\2\2\u0243\u0231\3\2\2\2\u0243\u0232\3\2\2\2\u0243\u0233"+
		"\3\2\2\2\u0243\u0235\3\2\2\2\u0243\u0236\3\2\2\2\u0243\u0238\3\2\2\2\u0243"+
		"\u023b\3\2\2\2\u0243\u023c\3\2\2\2\u0243\u023e\3\2\2\2\u0243\u0240\3\2"+
		"\2\2\u0244/\3\2\2\2\u0245\u0246\5\34\17\2\u0246\61\3\2\2\2\u0247\u0248"+
		"\b\32\1\2\u0248\u025a\5\66\34\2\u0249\u024a\7\u0200\2\2\u024a\u025a\5"+
		"\62\32\61\u024b\u024c\7\u0201\2\2\u024c\u025a\5\62\32\60\u024d\u024e\5"+
		"N(\2\u024e\u024f\5\62\32(\u024f\u025a\3\2\2\2\u0250\u0251\7A\2\2\u0251"+
		"\u025a\5\62\32%\u0252\u0253\5h\65\2\u0253\u0254\7\u009c\2\2\u0254\u0255"+
		"\5h\65\2\u0255\u025a\3\2\2\2\u0256\u0257\7\31\2\2\u0257\u025a\5\u013a"+
		"\u009e\2\u0258\u025a\7k\2\2\u0259\u0247\3\2\2\2\u0259\u0249\3\2\2\2\u0259"+
		"\u024b\3\2\2\2\u0259\u024d\3\2\2\2\u0259\u0250\3\2\2\2\u0259\u0252\3\2"+
		"\2\2\u0259\u0256\3\2\2\2\u0259\u0258\3\2\2\2\u025a\u031a\3\2\2\2\u025b"+
		"\u025c\f\62\2\2\u025c\u025d\7\u009e\2\2\u025d\u025e\7^\2\2\u025e\u025f"+
		"\7\u010c\2\2\u025f\u0319\5\62\32\63\u0260\u0261\f/\2\2\u0261\u0262\7\u0200"+
		"\2\2\u0262\u0319\5\62\32\60\u0263\u0264\f.\2\2\u0264\u0265\7\u0201\2\2"+
		"\u0265\u0319\5\62\32/\u0266\u0267\f-\2\2\u0267\u0268\7\u0202\2\2\u0268"+
		"\u0319\5\62\32.\u0269\u026a\f,\2\2\u026a\u026b\7\u0203\2\2\u026b\u0319"+
		"\5\62\32-\u026c\u026d\f+\2\2\u026d\u026e\7\u01fe\2\2\u026e\u0319\5\62"+
		"\32,\u026f\u0270\f*\2\2\u0270\u0271\7\u01fd\2\2\u0271\u0319\5\62\32+\u0272"+
		"\u0273\f)\2\2\u0273\u0274\5N(\2\u0274\u0275\5\62\32*\u0275\u0319\3\2\2"+
		"\2\u0276\u0277\f&\2\2\u0277\u0278\5,\27\2\u0278\u0279\5\62\32\'\u0279"+
		"\u0319\3\2\2\2\u027a\u027b\f$\2\2\u027b\u027c\5.\30\2\u027c\u027d\5\62"+
		"\32%\u027d\u0319\3\2\2\2\u027e\u027f\f#\2\2\u027f\u0280\5.\30\2\u0280"+
		"\u0281\5\62\32\2\u0281\u0282\7\u00a4\2\2\u0282\u0283\5\62\32$\u0283\u0319"+
		"\3\2\2\2\u0284\u0285\f\27\2\2\u0285\u0286\7@\2\2\u0286\u0287\7&\2\2\u0287"+
		"\u0288\7,\2\2\u0288\u0319\5\62\32\30\u0289\u028a\f\26\2\2\u028a\u028b"+
		"\7@\2\2\u028b\u028c\7A\2\2\u028c\u028d\7&\2\2\u028d\u028e\7,\2\2\u028e"+
		"\u0319\5\62\32\27\u028f\u0290\f\23\2\2\u0290\u0292\7F\2\2\u0291\u0293"+
		"\7\u015e\2\2\u0292\u0291\3\2\2\2\u0292\u0293\3\2\2\2\u0293\u0294\3\2\2"+
		"\2\u0294\u0295\5\64\33\2\u0295\u0296\7>\2\2\u0296\u0297\5\62\32\24\u0297"+
		"\u0319\3\2\2\2\u0298\u0299\f\22\2\2\u0299\u029a\7A\2\2\u029a\u029c\7F"+
		"\2\2\u029b\u029d\7\u015e\2\2\u029c\u029b\3\2\2\2\u029c\u029d\3\2\2\2\u029d"+
		"\u029e\3\2\2\2\u029e\u029f\5\64\33\2\u029f\u02a0\7>\2\2\u02a0\u02a1\5"+
		"\62\32\23\u02a1\u0319\3\2\2\2\u02a2\u02a3\f\21\2\2\u02a3\u02a4\7F\2\2"+
		"\u02a4\u02a5\7\u015b\2\2\u02a5\u02a6\5\64\33\2\u02a6\u02a7\7>\2\2\u02a7"+
		"\u02a8\5\62\32\22\u02a8\u0319\3\2\2\2\u02a9\u02aa\f\20\2\2\u02aa\u02ab"+
		"\7A\2\2\u02ab\u02ac\7F\2\2\u02ac\u02ad\7\u015b\2\2\u02ad\u02ae\5\64\33"+
		"\2\u02ae\u02af\7>\2\2\u02af\u02b0\5\62\32\21\u02b0\u0319\3\2\2\2\u02b1"+
		"\u02b2\f\4\2\2\u02b2\u02b3\5*\26\2\u02b3\u02b4\5\62\32\5\u02b4\u0319\3"+
		"\2\2\2\u02b5\u02b6\f\64\2\2\u02b6\u02b7\7\u0220\2\2\u02b7\u0319\5\u00a4"+
		"S\2\u02b8\u02b9\f\63\2\2\u02b9\u02ba\7\u00be\2\2\u02ba\u0319\5\u0104\u0083"+
		"\2\u02bb\u02bc\f\'\2\2\u02bc\u0319\5N(\2\u02bd\u02be\f\"\2\2\u02be\u02bf"+
		"\7@\2\2\u02bf\u0319\7B\2\2\u02c0\u02c1\f!\2\2\u02c1\u0319\7\u0159\2\2"+
		"\u02c2\u02c3\f \2\2\u02c3\u02c4\7@\2\2\u02c4\u02c5\7A\2\2\u02c5\u0319"+
		"\7B\2\2\u02c6\u02c7\f\37\2\2\u02c7\u0319\7\u015a\2\2\u02c8\u02c9\f\35"+
		"\2\2\u02c9\u02ca\7@\2\2\u02ca\u0319\7C\2\2\u02cb\u02cc\f\34\2\2\u02cc"+
		"\u02cd\7@\2\2\u02cd\u02ce\7A\2\2\u02ce\u0319\7C\2\2\u02cf\u02d0\f\33\2"+
		"\2\u02d0\u02d1\7@\2\2\u02d1\u0319\7D\2\2\u02d2\u02d3\f\32\2\2\u02d3\u02d4"+
		"\7@\2\2\u02d4\u02d5\7A\2\2\u02d5\u0319\7D\2\2\u02d6\u02d7\f\31\2\2\u02d7"+
		"\u02d8\7@\2\2\u02d8\u0319\7\u00a9\2\2\u02d9\u02da\f\30\2\2\u02da\u02db"+
		"\7@\2\2\u02db\u02dc\7A\2\2\u02dc\u0319\7\u00a9\2\2\u02dd\u02de\f\25\2"+
		"\2\u02de\u02df\7@\2\2\u02df\u02e0\7\u010d\2\2\u02e0\u02e1\7\u0211\2\2"+
		"\u02e1\u02e2\5z>\2\u02e2\u02e3\7\u0212\2\2\u02e3\u0319\3\2\2\2\u02e4\u02e5"+
		"\f\24\2\2\u02e5\u02e6\7@\2\2\u02e6\u02e7\7A\2\2\u02e7\u02e8\7\u010d\2"+
		"\2\u02e8\u02e9\7\u0211\2\2\u02e9\u02ea\5z>\2\u02ea\u02eb\7\u0212\2\2\u02eb"+
		"\u0319\3\2\2\2\u02ec\u02ed\f\17\2\2\u02ed\u02ee\7G\2\2\u02ee\u0319\5@"+
		"!\2\u02ef\u02f0\f\16\2\2\u02f0\u02f1\7A\2\2\u02f1\u02f2\7G\2\2\u02f2\u0319"+
		"\5@!\2\u02f3\u02f4\f\r\2\2\u02f4\u02f5\5P)\2\u02f5\u02f6\5n8\2\u02f6\u02f7"+
		"\5\u013a\u009e\2\u02f7\u0319\3\2\2\2\u02f8\u02f9\f\f\2\2\u02f9\u02fa\5"+
		"P)\2\u02fa\u02fb\5n8\2\u02fb\u02fc\7\u0211\2\2\u02fc\u02fd\5\62\32\2\u02fd"+
		"\u02fe\7\u0212\2\2\u02fe\u0319\3\2\2\2\u02ff\u0300\f\n\2\2\u0300\u0301"+
		"\7@\2\2\u0301\u0319\7\u015c\2\2\u0302\u0303\f\t\2\2\u0303\u0304\7@\2\2"+
		"\u0304\u0305\7A\2\2\u0305\u0319\7\u015c\2\2\u0306\u0307\f\b\2\2\u0307"+
		"\u0308\7@\2\2\u0308\u0319\7\u015d\2\2\u0309\u030a\f\7\2\2\u030a\u030b"+
		"\7@\2\2\u030b\u030c\5\u00d6l\2\u030c\u030d\7\u015d\2\2\u030d\u0319\3\2"+
		"\2\2\u030e\u030f\f\6\2\2\u030f\u0310\7@\2\2\u0310\u0311\7A\2\2\u0311\u0319"+
		"\7\u015d\2\2\u0312\u0313\f\5\2\2\u0313\u0314\7@\2\2\u0314\u0315\7A\2\2"+
		"\u0315\u0316\5\u00d6l\2\u0316\u0317\7\u015d\2\2\u0317\u0319\3\2\2\2\u0318"+
		"\u025b\3\2\2\2\u0318\u0260\3\2\2\2\u0318\u0263\3\2\2\2\u0318\u0266\3\2"+
		"\2\2\u0318\u0269\3\2\2\2\u0318\u026c\3\2\2\2\u0318\u026f\3\2\2\2\u0318"+
		"\u0272\3\2\2\2\u0318\u0276\3\2\2\2\u0318\u027a\3\2\2\2\u0318\u027e\3\2"+
		"\2\2\u0318\u0284\3\2\2\2\u0318\u0289\3\2\2\2\u0318\u028f\3\2\2\2\u0318"+
		"\u0298\3\2\2\2\u0318\u02a2\3\2\2\2\u0318\u02a9\3\2\2\2\u0318\u02b1\3\2"+
		"\2\2\u0318\u02b5\3\2\2\2\u0318\u02b8\3\2\2\2\u0318\u02bb\3\2\2\2\u0318"+
		"\u02bd\3\2\2\2\u0318\u02c0\3\2\2\2\u0318\u02c2\3\2\2\2\u0318\u02c6\3\2"+
		"\2\2\u0318\u02c8\3\2\2\2\u0318\u02cb\3\2\2\2\u0318\u02cf\3\2\2\2\u0318"+
		"\u02d2\3\2\2\2\u0318\u02d6\3\2\2\2\u0318\u02d9\3\2\2\2\u0318\u02dd\3\2"+
		"\2\2\u0318\u02e4\3\2\2\2\u0318\u02ec\3\2\2\2\u0318\u02ef\3\2\2\2\u0318"+
		"\u02f3\3\2\2\2\u0318\u02f8\3\2\2\2\u0318\u02ff\3\2\2\2\u0318\u0302\3\2"+
		"\2\2\u0318\u0306\3\2\2\2\u0318\u0309\3\2\2\2\u0318\u030e\3\2\2\2\u0318"+
		"\u0312\3\2\2\2\u0319\u031c\3\2\2\2\u031a\u0318\3\2\2\2\u031a\u031b\3\2"+
		"\2\2\u031b\63\3\2\2\2\u031c\u031a\3\2\2\2\u031d\u031e\b\33\1\2\u031e\u0327"+
		"\5\66\34\2\u031f\u0320\7\u0200\2\2\u0320\u0327\5\64\33\r\u0321\u0322\7"+
		"\u0201\2\2\u0322\u0327\5\64\33\f\u0323\u0324\5N(\2\u0324\u0325\5\64\33"+
		"\n\u0325\u0327\3\2\2\2\u0326\u031d\3\2\2\2\u0326\u031f\3\2\2\2\u0326\u0321"+
		"\3\2\2\2\u0326\u0323\3\2\2\2\u0327\u0354\3\2\2\2\u0328\u0329\f\13\2\2"+
		"\u0329\u032a\5N(\2\u032a\u032b\5\64\33\f\u032b\u0353\3\2\2\2\u032c\u032d"+
		"\f\b\2\2\u032d\u032e\7@\2\2\u032e\u032f\7&\2\2\u032f\u0330\7,\2\2\u0330"+
		"\u0353\5\64\33\t\u0331\u0332\f\7\2\2\u0332\u0333\7@\2\2\u0333\u0334\7"+
		"A\2\2\u0334\u0335\7&\2\2\u0335\u0336\7,\2\2\u0336\u0353\5\64\33\b\u0337"+
		"\u0338\f\16\2\2\u0338\u0339\7\u0220\2\2\u0339\u0353\5\u00a4S\2\u033a\u033b"+
		"\f\t\2\2\u033b\u0353\5N(\2\u033c\u033d\f\6\2\2\u033d\u033e\7@\2\2\u033e"+
		"\u033f\7\u010d\2\2\u033f\u0340\7\u0211\2\2\u0340\u0341\5z>\2\u0341\u0342"+
		"\7\u0212\2\2\u0342\u0353\3\2\2\2\u0343\u0344\f\5\2\2\u0344\u0345\7@\2"+
		"\2\u0345\u0346\7A\2\2\u0346\u0347\7\u010d\2\2\u0347\u0348\7\u0211\2\2"+
		"\u0348\u0349\5z>\2\u0349\u034a\7\u0212\2\2\u034a\u0353\3\2\2\2\u034b\u034c"+
		"\f\4\2\2\u034c\u034d\7@\2\2\u034d\u0353\7\u015c\2\2\u034e\u034f\f\3\2"+
		"\2\u034f\u0350\7@\2\2\u0350\u0351\7A\2\2\u0351\u0353\7\u015c\2\2\u0352"+
		"\u0328\3\2\2\2\u0352\u032c\3\2\2\2\u0352\u0331\3\2\2\2\u0352\u0337\3\2"+
		"\2\2\u0352\u033a\3\2\2\2\u0352\u033c\3\2\2\2\u0352\u0343\3\2\2\2\u0352"+
		"\u034b\3\2\2\2\u0352\u034e\3\2\2\2\u0353\u0356\3\2\2\2\u0354\u0352\3\2"+
		"\2\2\u0354\u0355\3\2\2\2\u0355\65\3\2\2\2\u0356\u0354\3\2\2\2\u0357\u0377"+
		"\5\2\2\2\u0358\u0377\5L\'\2\u0359\u0377\5\u0080A\2\u035a\u035c\7\u01f2"+
		"\2\2\u035b\u035d\5<\37\2\u035c\u035b\3\2\2\2\u035c\u035d\3\2\2\2\u035d"+
		"\u0377\3\2\2\2\u035e\u035f\7\u0211\2\2\u035f\u0360\5\62\32\2\u0360\u0361"+
		"\7\u0212\2\2\u0361\u0362\5:\36\2\u0362\u0377\3\2\2\2\u0363\u0377\5B\""+
		"\2\u0364\u0377\5\u0098M\2\u0365\u0377\5\u013a\u009e\2\u0366\u0367\5\u013a"+
		"\u009e\2\u0367\u0368\58\35\2\u0368\u0377\3\2\2\2\u0369\u036a\7E\2\2\u036a"+
		"\u0377\5\u013a\u009e\2\u036b\u036c\7[\2\2\u036c\u0377\5\u013a\u009e\2"+
		"\u036d\u036e\7[\2\2\u036e\u0377\5p9\2\u036f\u0377\5j\66\2\u0370\u0377"+
		"\5l\67\2\u0371\u0372\7\u0098\2\2\u0372\u0373\7\u0211\2\2\u0373\u0374\5"+
		"\u00a8U\2\u0374\u0375\7\u0212\2\2\u0375\u0377\3\2\2\2\u0376\u0357\3\2"+
		"\2\2\u0376\u0358\3\2\2\2\u0376\u0359\3\2\2\2\u0376\u035a\3\2\2\2\u0376"+
		"\u035e\3\2\2\2\u0376\u0363\3\2\2\2\u0376\u0364\3\2\2\2\u0376\u0365\3\2"+
		"\2\2\u0376\u0366\3\2\2\2\u0376\u0369\3\2\2\2\u0376\u036b\3\2\2\2\u0376"+
		"\u036d\3\2\2\2\u0376\u036f\3\2\2\2\u0376\u0370\3\2\2\2\u0376\u0371\3\2"+
		"\2\2\u0377\67\3\2\2\2\u0378\u0379\b\35\1\2\u0379\u037a\5<\37\2\u037a\u037f"+
		"\3\2\2\2\u037b\u037c\f\3\2\2\u037c\u037e\5<\37\2\u037d\u037b\3\2\2\2\u037e"+
		"\u0381\3\2\2\2\u037f\u037d\3\2\2\2\u037f\u0380\3\2\2\2\u03809\3\2\2\2"+
		"\u0381\u037f\3\2\2\2\u0382\u0387\b\36\1\2\u0383\u0384\f\4\2\2\u0384\u0386"+
		"\5<\37\2\u0385\u0383\3\2\2\2\u0386\u0389\3\2\2\2\u0387\u0385\3\2\2\2\u0387"+
		"\u0388\3\2\2\2\u0388;\3\2\2\2\u0389\u0387\3\2\2\2\u038a\u038b\7\u0205"+
		"\2\2\u038b\u039c\5\u00c4c\2\u038c\u038d\7\u0205\2\2\u038d\u039c\7\u0202"+
		"\2\2\u038e\u038f\7\u0215\2\2\u038f\u0390\5\62\32\2\u0390\u0391\7\u0216"+
		"\2\2\u0391\u039c\3\2\2\2\u0392\u0394\7\u0215\2\2\u0393\u0395\5> \2\u0394"+
		"\u0393\3\2\2\2\u0394\u0395\3\2\2\2\u0395\u0396\3\2\2\2\u0396\u0398\7\u01ff"+
		"\2\2\u0397\u0399\5> \2\u0398\u0397\3\2\2\2\u0398\u0399\3\2\2\2\u0399\u039a"+
		"\3\2\2\2\u039a\u039c\7\u0216\2\2\u039b\u038a\3\2\2\2\u039b\u038c\3\2\2"+
		"\2\u039b\u038e\3\2\2\2\u039b\u0392\3\2\2\2\u039c=\3\2\2\2\u039d\u039e"+
		"\5\62\32\2\u039e?\3\2\2\2\u039f\u03a5\5\u013a\u009e\2\u03a0\u03a1\7\u0211"+
		"\2\2\u03a1\u03a2\5\u00a8U\2\u03a2\u03a3\7\u0212\2\2\u03a3\u03a5\3\2\2"+
		"\2\u03a4\u039f\3\2\2\2\u03a4\u03a0\3\2\2\2\u03a5A\3\2\2\2\u03a6\u03a8"+
		"\7\'\2\2\u03a7\u03a9\5J&\2\u03a8\u03a7\3\2\2\2\u03a8\u03a9\3\2\2\2\u03a9"+
		"\u03aa\3\2\2\2\u03aa\u03ac\5D#\2\u03ab\u03ad\5H%\2\u03ac\u03ab\3\2\2\2"+
		"\u03ac\u03ad\3\2\2\2\u03ad\u03ae\3\2\2\2\u03ae\u03af\7\u00a0\2\2\u03af"+
		"C\3\2\2\2\u03b0\u03b2\5F$\2\u03b1\u03b0\3\2\2\2\u03b2\u03b3\3\2\2\2\u03b3"+
		"\u03b1\3\2\2\2\u03b3\u03b4\3\2\2\2\u03b4E\3\2\2\2\u03b5\u03b6\7(\2\2\u03b6"+
		"\u03b7\5\62\32\2\u03b7\u03b8\7;\2\2\u03b8\u03b9\5\62\32\2\u03b9G\3\2\2"+
		"\2\u03ba\u03bb\7:\2\2\u03bb\u03bc\5\62\32\2\u03bcI\3\2\2\2\u03bd\u03be"+
		"\5\62\32\2\u03beK\3\2\2\2\u03bf\u03c4\5\u0084C\2\u03c0\u03c1\5\u0084C"+
		"\2\u03c1\u03c2\58\35\2\u03c2\u03c4\3\2\2\2\u03c3\u03bf\3\2\2\2\u03c3\u03c0"+
		"\3\2\2\2\u03c4M\3\2\2\2\u03c5\u03cc\5X-\2\u03c6\u03c7\7\u0154\2\2\u03c7"+
		"\u03c8\7\u0211\2\2\u03c8\u03c9\5^\60\2\u03c9\u03ca\7\u0212\2\2\u03ca\u03cc"+
		"\3\2\2\2\u03cb\u03c5\3\2\2\2\u03cb\u03c6\3\2\2\2\u03ccO\3\2\2\2\u03cd"+
		"\u03da\5R*\2\u03ce\u03cf\7\u0154\2\2\u03cf\u03d0\7\u0211\2\2\u03d0\u03d1"+
		"\5^\60\2\u03d1\u03d2\7\u0212\2\2\u03d2\u03da\3\2\2\2\u03d3\u03da\7J\2"+
		"\2\u03d4\u03d5\7A\2\2\u03d5\u03da\7J\2\2\u03d6\u03da\7\u01f8\2\2\u03d7"+
		"\u03d8\7\u01f7\2\2\u03d8\u03da\7\u01f8\2\2\u03d9\u03cd\3\2\2\2\u03d9\u03ce"+
		"\3\2\2\2\u03d9\u03d3\3\2\2\2\u03d9\u03d4\3\2\2\2\u03d9\u03d6\3\2\2\2\u03d9"+
		"\u03d7\3\2\2\2\u03daQ\3\2\2\2\u03db\u03de\5T+\2\u03dc\u03de\5V,\2\u03dd"+
		"\u03db\3\2\2\2\u03dd\u03dc\3\2\2\2\u03deS\3\2\2\2\u03df\u03e1\t\b\2\2"+
		"\u03e0\u03df\3\2\2\2\u03e1\u03e2\3\2\2\2\u03e2\u03e0\3\2\2\2\u03e2\u03e3"+
		"\3\2\2\2\u03e3U\3\2\2\2\u03e4\u03e5\t\t\2\2\u03e5W\3\2\2\2\u03e6\u03f6"+
		"\7\u0223\2\2\u03e7\u03f6\7\u0224\2\2\u03e8\u03f6\7\u0225\2\2\u03e9\u03f6"+
		"\7\u0226\2\2\u03ea\u03f6\7\u0227\2\2\u03eb\u03f6\7\u0228\2\2\u03ec\u03f6"+
		"\7\u021b\2\2\u03ed\u03ee\7\u021b\2\2\u03ee\u03f6\7\u01f9\2\2\u03ef\u03f6"+
		"\7\u0229\2\2\u03f0\u03f6\7\u01f6\2\2\u03f1\u03f6\7\u0201\2\2\u03f2\u03f6"+
		"\7\u022a\2\2\u03f3\u03f6\7\u022b\2\2\u03f4\u03f6\7\u022c\2\2\u03f5\u03e6"+
		"\3\2\2\2\u03f5\u03e7\3\2\2\2\u03f5\u03e8\3\2\2\2\u03f5\u03e9\3\2\2\2\u03f5"+
		"\u03ea\3\2\2\2\u03f5\u03eb\3\2\2\2\u03f5\u03ec\3\2\2\2\u03f5\u03ed\3\2"+
		"\2\2\u03f5\u03ef\3\2\2\2\u03f5\u03f0\3\2\2\2\u03f5\u03f1\3\2\2\2\u03f5"+
		"\u03f2\3\2\2\2\u03f5\u03f3\3\2\2\2\u03f5\u03f4\3\2\2\2\u03f6Y\3\2\2\2"+
		"\u03f7\u03fe\5R*\2\u03f8\u03f9\7\u0154\2\2\u03f9\u03fa\7\u0211\2\2\u03fa"+
		"\u03fb\5^\60\2\u03fb\u03fc\7\u0212\2\2\u03fc\u03fe\3\2\2\2\u03fd\u03f7"+
		"\3\2\2\2\u03fd\u03f8\3\2\2\2\u03fe[\3\2\2\2\u03ff\u0400\t\n\2\2\u0400"+
		"]\3\2\2\2\u0401\u0407\5R*\2\u0402\u0403\5\u0084C\2\u0403\u0404\7\u0205"+
		"\2\2\u0404\u0405\5^\60\2\u0405\u0407\3\2\2\2\u0406\u0401\3\2\2\2\u0406"+
		"\u0402\3\2\2\2\u0407_\3\2\2\2\u0408\u0409\t\13\2\2\u0409\u040b\5b\62\2"+
		"\u040a\u040c\5f\64\2\u040b\u040a\3\2\2\2\u040b\u040c\3\2\2\2\u040ca\3"+
		"\2\2\2\u040d\u0414\5d\63\2\u040e\u040f\7F\2\2\u040f\u0410\5d\63\2\u0410"+
		"\u0411\7>\2\2\u0411\u0412\5d\63\2\u0412\u0414\3\2\2\2\u0413\u040d\3\2"+
		"\2\2\u0413\u040e\3\2\2\2\u0414c\3\2\2\2\u0415\u0416\7\u0105\2\2\u0416"+
		"\u0422\7\u00f1\2\2\u0417\u0418\7\u0105\2\2\u0418\u0422\7\u00d3\2\2\u0419"+
		"\u041a\7l\2\2\u041a\u0422\7\u00a8\2\2\u041b\u041c\5\62\32\2\u041c\u041d"+
		"\7\u00f1\2\2\u041d\u0422\3\2\2\2\u041e\u041f\5\62\32\2\u041f\u0420\7\u00d3"+
		"\2\2\u0420\u0422\3\2\2\2\u0421\u0415\3\2\2\2\u0421\u0417\3\2\2\2\u0421"+
		"\u0419\3\2\2\2\u0421\u041b\3\2\2\2\u0421\u041e\3\2\2\2\u0422e\3\2\2\2"+
		"\u0423\u0424\7\u00a5\2\2\u0424\u0425\7l\2\2\u0425\u042e\7\u00a8\2\2\u0426"+
		"\u0427\7\u00a5\2\2\u0427\u042e\7L\2\2\u0428\u0429\7\u00a5\2\2\u0429\u042e"+
		"\7\u0095\2\2\u042a\u042b\7\u00a5\2\2\u042b\u042c\7\u00b0\2\2\u042c\u042e"+
		"\7\u009b\2\2\u042d\u0423\3\2\2\2\u042d\u0426\3\2\2\2\u042d\u0428\3\2\2"+
		"\2\u042d\u042a\3\2\2\2\u042eg\3\2\2\2\u042f\u0430\7\u00a8\2\2\u0430\u0431"+
		"\7\u0211\2\2\u0431\u0432\5\u00a8U\2\u0432\u0433\7\u0212\2\2\u0433\u043e"+
		"\3\2\2\2\u0434\u0435\7\u00a8\2\2\u0435\u0436\7\u0211\2\2\u0436\u043e\7"+
		"\u0212\2\2\u0437\u0438\7\u0211\2\2\u0438\u0439\5\u00a8U\2\u0439\u043a"+
		"\7\u0217\2\2\u043a\u043b\5\62\32\2\u043b\u043c\7\u0212\2\2\u043c\u043e"+
		"\3\2\2\2\u043d\u042f\3\2\2\2\u043d\u0434\3\2\2\2\u043d\u0437\3\2\2\2\u043e"+
		"i\3\2\2\2\u043f\u0440\7\u00a8\2\2\u0440\u0441\7\u0211\2\2\u0441\u0442"+
		"\5\u00a8U\2\u0442\u0443\7\u0212\2\2\u0443\u0448\3\2\2\2\u0444\u0445\7"+
		"\u00a8\2\2\u0445\u0446\7\u0211\2\2\u0446\u0448\7\u0212\2\2\u0447\u043f"+
		"\3\2\2\2\u0447\u0444\3\2\2\2\u0448k\3\2\2\2\u0449\u044a\7\u0211\2\2\u044a"+
		"\u044b\5\u00a8U\2\u044b\u044c\7\u0217\2\2\u044c\u044d\5\62\32\2\u044d"+
		"\u044e\7\u0212\2\2\u044em\3\2\2\2\u044f\u0450\t\f\2\2\u0450o\3\2\2\2\u0451"+
		"\u0452\7\u0215\2\2\u0452\u0453\5\u00a8U\2\u0453\u0454\7\u0216\2\2\u0454"+
		"\u045c\3\2\2\2\u0455\u0456\7\u0215\2\2\u0456\u0457\5r:\2\u0457\u0458\7"+
		"\u0216\2\2\u0458\u045c\3\2\2\2\u0459\u045a\7\u0215\2\2\u045a\u045c\7\u0216"+
		"\2\2\u045b\u0451\3\2\2\2\u045b\u0455\3\2\2\2\u045b\u0459\3\2\2\2\u045c"+
		"q\3\2\2\2\u045d\u0462\5p9\2\u045e\u045f\7\u0217\2\2\u045f\u0461\5p9\2"+
		"\u0460\u045e\3\2\2\2\u0461\u0464\3\2\2\2\u0462\u0460\3\2\2\2\u0462\u0463"+
		"\3\2\2\2\u0463s\3\2\2\2\u0464\u0462\3\2\2\2\u0465\u046a\5x=\2\u0466\u0467"+
		"\7\u0217\2\2\u0467\u0469\5x=\2\u0468\u0466\3\2\2\2\u0469\u046c\3\2\2\2"+
		"\u046a\u0468\3\2\2\2\u046a\u046b\3\2\2\2\u046bu\3\2\2\2\u046c\u046a\3"+
		"\2\2\2\u046d\u046e\5\u0086D\2\u046ew\3\2\2\2\u046f\u0479\5\62\32\2\u0470"+
		"\u0471\5v<\2\u0471\u0472\7\u020a\2\2\u0472\u0473\5\62\32\2\u0473\u0479"+
		"\3\2\2\2\u0474\u0475\5v<\2\u0475\u0476\7\u020d\2\2\u0476\u0477\5\62\32"+
		"\2\u0477\u0479\3\2\2\2\u0478\u046f\3\2\2\2\u0478\u0470\3\2\2\2\u0478\u0474"+
		"\3\2\2\2\u0479y\3\2\2\2\u047a\u047f\5\u00a4S\2\u047b\u047c\7\u0217\2\2"+
		"\u047c\u047e\5\u00a4S\2\u047d\u047b\3\2\2\2\u047e\u0481\3\2\2\2\u047f"+
		"\u047d\3\2\2\2\u047f\u0480\3\2\2\2\u0480{\3\2\2\2\u0481\u047f\3\2\2\2"+
		"\u0482\u0483\5~@\2\u0483\u0484\7\u0211\2\2\u0484\u0485\7\u0212\2\2\u0485"+
		"\u04ba\3\2\2\2\u0486\u0487\5~@\2\u0487\u0488\7\u0211\2\2\u0488\u048a\5"+
		"t;\2\u0489\u048b\5\u0112\u008a\2\u048a\u0489\3\2\2\2\u048a\u048b\3\2\2"+
		"\2\u048b\u048c\3\2\2\2\u048c\u048d\7\u0212\2\2\u048d\u04ba\3\2\2\2\u048e"+
		"\u048f\5~@\2\u048f\u0490\7\u0211\2\2\u0490\u0491\7\u015f\2\2\u0491\u0493"+
		"\5x=\2\u0492\u0494\5\u0112\u008a\2\u0493\u0492\3\2\2\2\u0493\u0494\3\2"+
		"\2\2\u0494\u0495\3\2\2\2\u0495\u0496\7\u0212\2\2\u0496\u04ba\3\2\2\2\u0497"+
		"\u0498\5~@\2\u0498\u0499\7\u0211\2\2\u0499\u049a\5t;\2\u049a\u049b\7\u0217"+
		"\2\2\u049b\u049c\7\u015f\2\2\u049c\u049e\5x=\2\u049d\u049f\5\u0112\u008a"+
		"\2\u049e\u049d\3\2\2\2\u049e\u049f\3\2\2\2\u049f\u04a0\3\2\2\2\u04a0\u04a1"+
		"\7\u0212\2\2\u04a1\u04ba\3\2\2\2\u04a2\u04a3\5~@\2\u04a3\u04a4\7\u0211"+
		"\2\2\u04a4\u04a5\7H\2\2\u04a5\u04a7\5t;\2\u04a6\u04a8\5\u0112\u008a\2"+
		"\u04a7\u04a6\3\2\2\2\u04a7\u04a8\3\2\2\2\u04a8\u04a9\3\2\2\2\u04a9\u04aa"+
		"\7\u0212\2\2\u04aa\u04ba\3\2\2\2\u04ab\u04ac\5~@\2\u04ac\u04ad\7\u0211"+
		"\2\2\u04ad\u04ae\7&\2\2\u04ae\u04b0\5t;\2\u04af\u04b1\5\u0112\u008a\2"+
		"\u04b0\u04af\3\2\2\2\u04b0\u04b1\3\2\2\2\u04b1\u04b2\3\2\2\2\u04b2\u04b3"+
		"\7\u0212\2\2\u04b3\u04ba\3\2\2\2\u04b4\u04b5\5~@\2\u04b5\u04b6\7\u0211"+
		"\2\2\u04b6\u04b7\7\u0202\2\2\u04b7\u04b8\7\u0212\2\2\u04b8\u04ba\3\2\2"+
		"\2\u04b9\u0482\3\2\2\2\u04b9\u0486\3\2\2\2\u04b9\u048e\3\2\2\2\u04b9\u0497"+
		"\3\2\2\2\u04b9\u04a2\3\2\2\2\u04b9\u04ab\3\2\2\2\u04b9\u04b4\3\2\2\2\u04ba"+
		"}\3\2\2\2\u04bb\u04c0\5\u0086D\2\u04bc\u04bd\5\u0084C\2\u04bd\u04be\5"+
		"8\35\2\u04be\u04c0\3\2\2\2\u04bf\u04bb\3\2\2\2\u04bf\u04bc\3\2\2\2\u04c0"+
		"\177\3\2\2\2\u04c1\u04d3\7\u022f\2\2\u04c2\u04d3\7\u022e\2\2\u04c3\u04c4"+
		"\5~@\2\u04c4\u04c5\7\u022e\2\2\u04c5\u04d3\3\2\2\2\u04c6\u04c7\5~@\2\u04c7"+
		"\u04c8\7\u0211\2\2\u04c8\u04ca\5t;\2\u04c9\u04cb\5\u0112\u008a\2\u04ca"+
		"\u04c9\3\2\2\2\u04ca\u04cb\3\2\2\2\u04cb\u04cc\3\2\2\2\u04cc\u04cd\7\u0212"+
		"\2\2\u04cd\u04ce\7\u022e\2\2\u04ce\u04d3\3\2\2\2\u04cf\u04d3\7C\2\2\u04d0"+
		"\u04d3\7D\2\2\u04d1\u04d3\7B\2\2\u04d2\u04c1\3\2\2\2\u04d2\u04c2\3\2\2"+
		"\2\u04d2\u04c3\3\2\2\2\u04d2\u04c6\3\2\2\2\u04d2\u04cf\3\2\2\2\u04d2\u04d0"+
		"\3\2\2\2\u04d2\u04d1\3\2\2\2\u04d3\u0081\3\2\2\2\u04d4\u04d9\5\u0084C"+
		"\2\u04d5\u04d6\5\u0084C\2\u04d6\u04d7\58\35\2\u04d7\u04d9\3\2\2\2\u04d8"+
		"\u04d4\3\2\2\2\u04d8\u04d5\3\2\2\2\u04d9\u0083\3\2\2\2\u04da\u04db\5\n"+
		"\6\2\u04db\u0085\3\2\2\2\u04dc\u04e0\5\n\6\2\u04dd\u04e0\5\20\t\2\u04de"+
		"\u04e0\5\22\n\2\u04df\u04dc\3\2\2\2\u04df\u04dd\3\2\2\2\u04df\u04de\3"+
		"\2\2\2\u04e0\u0087\3\2\2\2\u04e1\u04e3\5\u009eP\2\u04e2\u04e4\5\u00a0"+
		"Q\2\u04e3\u04e2\3\2\2\2\u04e3\u04e4\3\2\2\2\u04e4\u04ee\3\2\2\2\u04e5"+
		"\u04e6\7\u00b6\2\2\u04e6\u04e7\7,\2\2\u04e7\u04e8\7\u0211\2\2\u04e8\u04e9"+
		"\5\u00fa~\2\u04e9\u04eb\7\u0212\2\2\u04ea\u04ec\5\u00a0Q\2\u04eb\u04ea"+
		"\3\2\2\2\u04eb\u04ec\3\2\2\2\u04ec\u04ee\3\2\2\2\u04ed\u04e1\3\2\2\2\u04ed"+
		"\u04e5\3\2\2\2\u04ee\u0089\3\2\2\2\u04ef\u04f0\7\u0162\2\2\u04f0\u04f1"+
		"\7\u0211\2\2\u04f1\u04f2\5\66\34\2\u04f2\u04f3\5\u00ecw\2\u04f3\u04f4"+
		"\7\u0163\2\2\u04f4\u04f5\5\u008cG\2\u04f5\u04f6\7\u0212\2\2\u04f6\u0505"+
		"\3\2\2\2\u04f7\u04f8\7\u0162\2\2\u04f8\u04f9\7\u0211\2\2\u04f9\u04fa\7"+
		"\u0167\2\2\u04fa\u04fb\7\u0211\2\2\u04fb\u04fc\5\u0094K\2\u04fc\u04fd"+
		"\7\u0212\2\2\u04fd\u04fe\7\u0217\2\2\u04fe\u04ff\5\66\34\2\u04ff\u0500"+
		"\5\u00ecw\2\u0500\u0501\7\u0163\2\2\u0501\u0502\5\u008cG\2\u0502\u0503"+
		"\7\u0212\2\2\u0503\u0505\3\2\2\2\u0504\u04ef\3\2\2\2\u0504\u04f7\3\2\2"+
		"\2\u0505\u008b\3\2\2\2\u0506\u050b\5\u008eH\2\u0507\u0508\7\u0217\2\2"+
		"\u0508\u050a\5\u008eH\2\u0509\u0507\3\2\2\2\u050a\u050d\3\2\2\2\u050b"+
		"\u0509\3\2\2\2\u050b\u050c\3\2\2\2\u050c\u008d\3\2\2\2\u050d\u050b\3\2"+
		"\2\2\u050e\u050f\5\u0084C\2\u050f\u0510\5\u00a4S\2\u0510\u051a\3\2\2\2"+
		"\u0511\u0512\5\u0084C\2\u0512\u0513\5\u00a4S\2\u0513\u0514\5\u0090I\2"+
		"\u0514\u051a\3\2\2\2\u0515\u0516\5\u0084C\2\u0516\u0517\7<\2\2\u0517\u0518"+
		"\7\u0137\2\2\u0518\u051a\3\2\2\2\u0519\u050e\3\2\2\2\u0519\u0511\3\2\2"+
		"\2\u0519\u0515\3\2\2\2\u051a\u008f\3\2\2\2\u051b\u051c\bI\1\2\u051c\u051d"+
		"\5\u0092J\2\u051d\u0522\3\2\2\2\u051e\u051f\f\3\2\2\u051f\u0521\5\u0092"+
		"J\2\u0520\u051e\3\2\2\2\u0521\u0524\3\2\2\2\u0522\u0520\3\2\2\2\u0522"+
		"\u0523\3\2\2\2\u0523\u0091\3\2\2\2\u0524\u0522\3\2\2\2\u0525\u0526\5\n"+
		"\6\2\u0526\u0527\5\64\33\2\u0527\u052e\3\2\2\2\u0528\u0529\7k\2\2\u0529"+
		"\u052e\5\64\33\2\u052a\u052b\7A\2\2\u052b\u052e\7B\2\2\u052c\u052e\7B"+
		"\2\2\u052d\u0525\3\2\2\2\u052d\u0528\3\2\2\2\u052d\u052a\3\2\2\2\u052d"+
		"\u052c\3\2\2\2\u052e\u0093\3\2\2\2\u052f\u0534\5\u0096L\2\u0530\u0531"+
		"\7\u0217\2\2\u0531\u0533\5\u0096L\2\u0532\u0530\3\2\2\2\u0533\u0536\3"+
		"\2\2\2\u0534\u0532\3\2\2\2\u0534\u0535\3\2\2\2\u0535\u0095\3\2\2\2\u0536"+
		"\u0534\3\2\2\2\u0537\u0538\5\64\33\2\u0538\u0539\7\67\2\2\u0539\u053a"+
		"\5\n\6\2\u053a\u053e\3\2\2\2\u053b\u053c\7k\2\2\u053c\u053e\5\64\33\2"+
		"\u053d\u0537\3\2\2\2\u053d\u053b\3\2\2\2\u053e\u0097\3\2\2\2\u053f\u0541"+
		"\5|?\2\u0540\u0542\5\u009aN\2\u0541\u0540\3\2\2\2\u0541\u0542\3\2\2\2"+
		"\u0542\u0544\3\2\2\2\u0543\u0545\5\u009cO\2\u0544\u0543\3\2\2\2\u0544"+
		"\u0545\3\2\2\2\u0545\u0547\3\2\2\2\u0546\u0548\5\u011e\u0090\2\u0547\u0546"+
		"\3\2\2\2\u0547\u0548\3\2\2\2\u0548\u054b\3\2\2\2\u0549\u054b\5\u00a2R"+
		"\2\u054a\u053f\3\2\2\2\u054a\u0549\3\2\2\2\u054b\u0099\3\2\2\2\u054c\u054d"+
		"\7\u010a\2\2\u054d\u054e\7L\2\2\u054e\u054f\7\u0211\2\2\u054f\u0550\5"+
		"\u0112\u008a\2\u0550\u0551\7\u0212\2\2\u0551\u009b\3\2\2\2\u0552\u0553"+
		"\7\u00d1\2\2\u0553\u0554\7\u0211\2\2\u0554\u0555\7\66\2\2\u0555\u0556"+
		"\5\62\32\2\u0556\u0557\7\u0212\2\2\u0557\u009d\3\2\2\2\u0558\u055b\5|"+
		"?\2\u0559\u055b\5\u00a2R\2\u055a\u0558\3\2\2\2\u055a\u0559\3\2\2\2\u055b"+
		"\u009f\3\2\2\2\u055c\u055d\7$\2\2\u055d\u055e\7\u0137\2\2\u055e\u00a1"+
		"\3\2\2\2\u055f\u0560\7|\2\2\u0560\u0561\7<\2\2\u0561\u0562\7\u0211\2\2"+
		"\u0562\u0563\5\62\32\2\u0563\u0564\7\u0212\2\2\u0564\u0634\3\2\2\2\u0565"+
		"\u0634\7\u0087\2\2\u0566\u0634\7\u0088\2\2\u0567\u0568\7\u0088\2\2\u0568"+
		"\u0569\7\u0211\2\2\u0569\u056a\7\u022f\2\2\u056a\u0634\7\u0212\2\2\u056b"+
		"\u0634\7\u0089\2\2\u056c\u056d\7\u0089\2\2\u056d\u056e\7\u0211\2\2\u056e"+
		"\u056f\7\u022f\2\2\u056f\u0634\7\u0212\2\2\u0570\u0634\7`\2\2\u0571\u0572"+
		"\7`\2\2\u0572\u0573\7\u0211\2\2\u0573\u0574\7\u022f\2\2\u0574\u0634\7"+
		"\u0212\2\2\u0575\u0634\7a\2\2\u0576\u0577\7a\2\2\u0577\u0578\7\u0211\2"+
		"\2\u0578\u0579\7\u022f\2\2\u0579\u0634\7\u0212\2\2\u057a\u0634\7\u0138"+
		"\2\2\u057b\u0634\7t\2\2\u057c\u0634\7\u00fd\2\2\u057d\u0634\7\u00b9\2"+
		"\2\u057e\u0634\7\u0139\2\2\u057f\u0634\7\u013a\2\2\u0580\u0581\7)\2\2"+
		"\u0581\u0582\7\u0211\2\2\u0582\u0583\5\62\32\2\u0583\u0584\7\67\2\2\u0584"+
		"\u0585\5\u00a4S\2\u0585\u0586\7\u0212\2\2\u0586\u0634\3\2\2\2\u0587\u0588"+
		"\7\u00d0\2\2\u0588\u058a\7\u0211\2\2\u0589\u058b\5\u00aaV\2\u058a\u0589"+
		"\3\2\2\2\u058a\u058b\3\2\2\2\u058b\u058c\3\2\2\2\u058c\u0634\7\u0212\2"+
		"\2\u058d\u058e\7\u013b\2\2\u058e\u058f\7\u0211\2\2\u058f\u0590\5\62\32"+
		"\2\u0590\u0591\7\u0212\2\2\u0591\u0634\3\2\2\2\u0592\u0593\7\u013b\2\2"+
		"\u0593\u0594\7\u0211\2\2\u0594\u0595\5\62\32\2\u0595\u0596\7\u0217\2\2"+
		"\u0596\u0597\5\u00d6l\2\u0597\u0598\7\u0212\2\2\u0598\u0634\3\2\2\2\u0599"+
		"\u059a\7\u013c\2\2\u059a\u059b\7\u0211\2\2\u059b\u059c\5\u00dan\2\u059c"+
		"\u059d\7\u0212\2\2\u059d\u0634\3\2\2\2\u059e\u059f\7\34\2\2\u059f\u05a0"+
		"\7\u0211\2\2\u05a0\u05a1\5\u00e2r\2\u05a1\u05a2\7\u0212\2\2\u05a2\u0634"+
		"\3\2\2\2\u05a3\u05a4\7+\2\2\u05a4\u05a5\7\u0211\2\2\u05a5\u05a6\5\u00e4"+
		"s\2\u05a6\u05a7\7\u0212\2\2\u05a7\u0634\3\2\2\2\u05a8\u05a9\7\u0145\2"+
		"\2\u05a9\u05aa\7\u0211\2\2\u05aa\u05ab\5\62\32\2\u05ab\u05ac\7\67\2\2"+
		"\u05ac\u05ad\5\u00a4S\2\u05ad\u05ae\7\u0212\2\2\u05ae\u0634\3\2\2\2\u05af"+
		"\u05b0\7*\2\2\u05b0\u05b1\7\u0211\2\2\u05b1\u05b2\7\u008f\2\2\u05b2\u05b3"+
		"\5\u00d8m\2\u05b3\u05b4\7\u0212\2\2\u05b4\u0634\3\2\2\2\u05b5\u05b6\7"+
		"*\2\2\u05b6\u05b7\7\u0211\2\2\u05b7\u05b8\7\u0090\2\2\u05b8\u05b9\5\u00d8"+
		"m\2\u05b9\u05ba\7\u0212\2\2\u05ba\u0634\3\2\2\2\u05bb\u05bc\7*\2\2\u05bc"+
		"\u05bd\7\u0211\2\2\u05bd\u05be\7\u0091\2\2\u05be\u05bf\5\u00d8m\2\u05bf"+
		"\u05c0\7\u0212\2\2\u05c0\u0634\3\2\2\2\u05c1\u05c2\7*\2\2\u05c2\u05c3"+
		"\7\u0211\2\2\u05c3\u05c4\5\u00d8m\2\u05c4\u05c5\7\u0212\2\2\u05c5\u0634"+
		"\3\2\2\2\u05c6\u05c7\7\u008a\2\2\u05c7\u05c8\7\u0211\2\2\u05c8\u05c9\5"+
		"\62\32\2\u05c9\u05ca\7\u0217\2\2\u05ca\u05cb\5\62\32\2\u05cb\u05cc\7\u0212"+
		"\2\2\u05cc\u0634\3\2\2\2\u05cd\u05ce\7\u0092\2\2\u05ce\u05cf\7\u0211\2"+
		"\2\u05cf\u05d0\5\u00a8U\2\u05d0\u05d1\7\u0212\2\2\u05d1\u0634\3\2\2\2"+
		"\u05d2\u05d3\7\u0151\2\2\u05d3\u05d4\7\u0211\2\2\u05d4\u05d5\5\u00a8U"+
		"\2\u05d5\u05d6\7\u0212\2\2\u05d6\u0634\3\2\2\2\u05d7\u05d8\7\u0152\2\2"+
		"\u05d8\u05d9\7\u0211\2\2\u05d9\u05da\5\u00a8U\2\u05da\u05db\7\u0212\2"+
		"\2\u05db\u0634\3\2\2\2\u05dc\u05dd\7\u013d\2\2\u05dd\u05de\7\u0211\2\2"+
		"\u05de\u05df\5\u00a8U\2\u05df\u05e0\7\u0212\2\2\u05e0\u0634\3\2\2\2\u05e1"+
		"\u05e2\7\u013e\2\2\u05e2\u05e3\7\u0211\2\2\u05e3\u05e4\7{\2\2\u05e4\u05e5"+
		"\5\n\6\2\u05e5\u05e6\7\u0212\2\2\u05e6\u0634\3\2\2\2\u05e7\u05e8\7\u013e"+
		"\2\2\u05e8\u05e9\7\u0211\2\2\u05e9\u05ea\7{\2\2\u05ea\u05eb\5\n\6\2\u05eb"+
		"\u05ec\7\u0217\2\2\u05ec\u05ed\5\u00e6t\2\u05ed\u05ee\7\u0212\2\2\u05ee"+
		"\u0634\3\2\2\2\u05ef\u05f0\7\u013e\2\2\u05f0\u05f1\7\u0211\2\2\u05f1\u05f2"+
		"\7{\2\2\u05f2\u05f3\5\n\6\2\u05f3\u05f4\7\u0217\2\2\u05f4\u05f5\5\u00a8"+
		"U\2\u05f5\u05f6\7\u0212\2\2\u05f6\u0634\3\2\2\2\u05f7\u05f8\7\u013e\2"+
		"\2\u05f8\u05f9\7\u0211\2\2\u05f9\u05fa\7{\2\2\u05fa\u05fb\5\n\6\2\u05fb"+
		"\u05fc\7\u0217\2\2\u05fc\u05fd\5\u00e6t\2\u05fd\u05fe\7\u0217\2\2\u05fe"+
		"\u05ff\5\u00a8U\2\u05ff\u0600\7\u0212\2\2\u0600\u0634\3\2\2\2\u0601\u0602"+
		"\7\u013f\2\2\u0602\u0603\7\u0211\2\2\u0603\u0604\5\66\34\2\u0604\u0605"+
		"\5\u00ecw\2\u0605\u0606\7\u0212\2\2\u0606\u0634\3\2\2\2\u0607\u0608\7"+
		"\u0140\2\2\u0608\u0609\7\u0211\2\2\u0609\u060a\5\u00e8u\2\u060a\u060b"+
		"\7\u0212\2\2\u060b\u0634\3\2\2\2\u060c\u060d\7\u0141\2\2\u060d\u060e\7"+
		"\u0211\2\2\u060e\u060f\5\u00f0y\2\u060f\u0610\5\62\32\2\u0610\u0611\5"+
		"\u00f2z\2\u0611\u0612\7\u0212\2\2\u0612\u0634\3\2\2\2\u0613\u0614\7\u0142"+
		"\2\2\u0614\u0615\7\u0211\2\2\u0615\u0616\7{\2\2\u0616\u0617\5\n\6\2\u0617"+
		"\u0618\7\u0212\2\2\u0618\u0634\3\2\2\2\u0619\u061a\7\u0142\2\2\u061a\u061b"+
		"\7\u0211\2\2\u061b\u061c\7{\2\2\u061c\u061d\5\n\6\2\u061d\u061e\7\u0217"+
		"\2\2\u061e\u061f\5\62\32\2\u061f\u0620\7\u0212\2\2\u0620\u0634\3\2\2\2"+
		"\u0621\u0622\7\u0143\2\2\u0622\u0623\7\u0211\2\2\u0623\u0624\5\62\32\2"+
		"\u0624\u0625\7\u0217\2\2\u0625\u0627\5\u00f4{\2\u0626\u0628\5\u00f6|\2"+
		"\u0627\u0626\3\2\2\2\u0627\u0628\3\2\2\2\u0628\u0629\3\2\2\2\u0629\u062a"+
		"\7\u0212\2\2\u062a\u0634\3\2\2\2\u062b\u062c\7\u0144\2\2\u062c\u062d\7"+
		"\u0211\2\2\u062d\u062e\5\u00f0y\2\u062e\u062f\5\62\32\2\u062f\u0630\7"+
		"\67\2\2\u0630\u0631\5\u00a6T\2\u0631\u0632\7\u0212\2\2\u0632\u0634\3\2"+
		"\2\2\u0633\u055f\3\2\2\2\u0633\u0565\3\2\2\2\u0633\u0566\3\2\2\2\u0633"+
		"\u0567\3\2\2\2\u0633\u056b\3\2\2\2\u0633\u056c\3\2\2\2\u0633\u0570\3\2"+
		"\2\2\u0633\u0571\3\2\2\2\u0633\u0575\3\2\2\2\u0633\u0576\3\2\2\2\u0633"+
		"\u057a\3\2\2\2\u0633\u057b\3\2\2\2\u0633\u057c\3\2\2\2\u0633\u057d\3\2"+
		"\2\2\u0633\u057e\3\2\2\2\u0633\u057f\3\2\2\2\u0633\u0580\3\2\2\2\u0633"+
		"\u0587\3\2\2\2\u0633\u058d\3\2\2\2\u0633\u0592\3\2\2\2\u0633\u0599\3\2"+
		"\2\2\u0633\u059e\3\2\2\2\u0633\u05a3\3\2\2\2\u0633\u05a8\3\2\2\2\u0633"+
		"\u05af\3\2\2\2\u0633\u05b5\3\2\2\2\u0633\u05bb\3\2\2\2\u0633\u05c1\3\2"+
		"\2\2\u0633\u05c6\3\2\2\2\u0633\u05cd\3\2\2\2\u0633\u05d2\3\2\2\2\u0633"+
		"\u05d7\3\2\2\2\u0633\u05dc\3\2\2\2\u0633\u05e1\3\2\2\2\u0633\u05e7\3\2"+
		"\2\2\u0633\u05ef\3\2\2\2\u0633\u05f7\3\2\2\2\u0633\u0601\3\2\2\2\u0633"+
		"\u0607\3\2\2\2\u0633\u060c\3\2\2\2\u0633\u0613\3\2\2\2\u0633\u0619\3\2"+
		"\2\2\u0633\u0621\3\2\2\2\u0633\u062b\3\2\2\2\u0634\u00a3\3\2\2\2\u0635"+
		"\u0636\5\u00a6T\2\u0636\u0637\5\u00d2j\2\u0637\u0651\3\2\2\2\u0638\u0639"+
		"\7\u0146\2\2\u0639\u063a\5\u00a6T\2\u063a\u063b\5\u00d2j\2\u063b\u0651"+
		"\3\2\2\2\u063c\u063d\5\u00a6T\2\u063d\u063e\7[\2\2\u063e\u063f\7\u0215"+
		"\2\2\u063f\u0640\7\u022f\2\2\u0640\u0641\7\u0216\2\2\u0641\u0651\3\2\2"+
		"\2\u0642\u0643\7\u0146\2\2\u0643\u0644\5\u00a6T\2\u0644\u0645\7[\2\2\u0645"+
		"\u0646\7\u0215\2\2\u0646\u0647\7\u022f\2\2\u0647\u0648\7\u0216\2\2\u0648"+
		"\u0651\3\2\2\2\u0649\u064a\5\u00a6T\2\u064a\u064b\7[\2\2\u064b\u0651\3"+
		"\2\2\2\u064c\u064d\7\u0146\2\2\u064d\u064e\5\u00a6T\2\u064e\u064f\7[\2"+
		"\2\u064f\u0651\3\2\2\2\u0650\u0635\3\2\2\2\u0650\u0638\3\2\2\2\u0650\u063c"+
		"\3\2\2\2\u0650\u0642\3\2\2\2\u0650\u0649\3\2\2\2\u0650\u064c\3\2\2\2\u0651"+
		"\u00a5\3\2\2\2\u0652\u0660\5\u00aeX\2\u0653\u0660\5\u00b2Z\2\u0654\u0660"+
		"\5\u00c8e\2\u0655\u0660\5\u00b8]\2\u0656\u0660\5\u00b4[\2\u0657\u0658"+
		"\5\u00ceh\2\u0658\u0659\5\u00d0i\2\u0659\u0660\3\2\2\2\u065a\u065b\5\u00ce"+
		"h\2\u065b\u065c\7\u0211\2\2\u065c\u065d\7\u022f\2\2\u065d\u065e\7\u0212"+
		"\2\2\u065e\u0660\3\2\2\2\u065f\u0652\3\2\2\2\u065f\u0653\3\2\2\2\u065f"+
		"\u0654\3\2\2\2\u065f\u0655\3\2\2\2\u065f\u0656\3\2\2\2\u065f\u0657\3\2"+
		"\2\2\u065f\u065a\3\2\2\2\u0660\u00a7\3\2\2\2\u0661\u0662\bU\1\2\u0662"+
		"\u0663\5\62\32\2\u0663\u0669\3\2\2\2\u0664\u0665\f\3\2\2\u0665\u0666\7"+
		"\u0217\2\2\u0666\u0668\5\62\32\2\u0667\u0664\3\2\2\2\u0668\u066b\3\2\2"+
		"\2\u0669\u0667\3\2\2\2\u0669\u066a\3\2\2\2\u066a\u00a9\3\2\2\2\u066b\u0669"+
		"\3\2\2\2\u066c\u066d\5\u00acW\2\u066d\u066e\7,\2\2\u066e\u066f\5\62\32"+
		"\2\u066f\u00ab\3\2\2\2\u0670\u0678\7b\2\2\u0671\u0678\7d\2\2\u0672\u0678"+
		"\7f\2\2\u0673\u0678\7g\2\2\u0674\u0678\7h\2\2\u0675\u0678\7i\2\2\u0676"+
		"\u0678\5\n\6\2\u0677\u0670\3\2\2\2\u0677\u0671\3\2\2\2\u0677\u0672\3\2"+
		"\2\2\u0677\u0673\3\2\2\2\u0677\u0674\3\2\2\2\u0677\u0675\3\2\2\2\u0677"+
		"\u0676\3\2\2\2\u0678\u00ad\3\2\2\2\u0679\u067b\5\u0086D\2\u067a\u067c"+
		"\5\u00b0Y\2\u067b\u067a\3\2\2\2\u067b\u067c\3\2\2\2\u067c\u0683\3\2\2"+
		"\2\u067d\u067e\5\u0086D\2\u067e\u0680\5\u00c2b\2\u067f\u0681\5\u00b0Y"+
		"\2\u0680\u067f\3\2\2\2\u0680\u0681\3\2\2\2\u0681\u0683\3\2\2\2\u0682\u0679"+
		"\3\2\2\2\u0682\u067d\3\2\2\2\u0683\u00af\3\2\2\2\u0684\u0685\7\u0211\2"+
		"\2\u0685\u0686\5\u00a8U\2\u0686\u0687\7\u0212\2\2\u0687\u00b1\3\2\2\2"+
		"\u0688\u06a4\7\u0111\2\2\u0689\u06a4\7~\2\2\u068a\u06a4\7\u0082\2\2\u068b"+
		"\u06a4\7\u0083\2\2\u068c\u06a4\7\177\2\2\u068d\u068e\7\u0115\2\2\u068e"+
		"\u06a4\5\u00c0a\2\u068f\u0690\7X\2\2\u0690\u06a4\7\35\2\2\u0691\u0693"+
		"\7\u0080\2\2\u0692\u0694\5\u00b0Y\2\u0693\u0692\3\2\2\2\u0693\u0694\3"+
		"\2\2\2\u0694\u06a4\3\2\2\2\u0695\u0697\7\u009f\2\2\u0696\u0698\5\u00b0"+
		"Y\2\u0697\u0696\3\2\2\2\u0697\u0698\3\2\2\2\u0698\u06a4\3\2\2\2\u0699"+
		"\u069b\7\u0084\2\2\u069a\u069c\5\u00b0Y\2\u069b\u069a\3\2\2\2\u069b\u069c"+
		"\3\2\2\2\u069c\u06a4\3\2\2\2\u069d\u06a4\7W\2\2\u069e\u06a4\7\u0117\2"+
		"\2\u069f\u06a4\7\u0116\2\2\u06a0\u06a4\7\u0112\2\2\u06a1\u06a4\7\u0113"+
		"\2\2\u06a2\u06a4\7\u0114\2\2\u06a3\u0688\3\2\2\2\u06a3\u0689\3\2\2\2\u06a3"+
		"\u068a\3\2\2\2\u06a3\u068b\3\2\2\2\u06a3\u068c\3\2\2\2\u06a3\u068d\3\2"+
		"\2\2\u06a3\u068f\3\2\2\2\u06a3\u0691\3\2\2\2\u06a3\u0695\3\2\2\2\u06a3"+
		"\u0699\3\2\2\2\u06a3\u069d\3\2\2\2\u06a3\u069e\3\2\2\2\u06a3\u069f\3\2"+
		"\2\2\u06a3\u06a0\3\2\2\2\u06a3\u06a1\3\2\2\2\u06a3\u06a2\3\2\2\2\u06a4"+
		"\u00b3\3\2\2\2\u06a5\u06a6\7_\2\2\u06a6\u06a7\7\u0211\2\2\u06a7\u06a8"+
		"\7\u022f\2\2\u06a8\u06aa\7\u0212\2\2\u06a9\u06ab\5\u00b6\\\2\u06aa\u06a9"+
		"\3\2\2\2\u06aa\u06ab\3\2\2\2\u06ab\u06bd\3\2\2\2\u06ac\u06ae\7_\2\2\u06ad"+
		"\u06af\5\u00b6\\\2\u06ae\u06ad\3\2\2\2\u06ae\u06af\3\2\2\2\u06af\u06bd"+
		"\3\2\2\2\u06b0\u06b1\7^\2\2\u06b1\u06b2\7\u0211\2\2\u06b2\u06b3\7\u022f"+
		"\2\2\u06b3\u06b5\7\u0212\2\2\u06b4\u06b6\5\u00b6\\\2\u06b5\u06b4\3\2\2"+
		"\2\u06b5\u06b6\3\2\2\2\u06b6\u06bd\3\2\2\2\u06b7\u06b9\7^\2\2\u06b8\u06ba"+
		"\5\u00b6\\\2\u06b9\u06b8\3\2\2\2\u06b9\u06ba\3\2\2\2\u06ba\u06bd\3\2\2"+
		"\2\u06bb\u06bd\7]\2\2\u06bc\u06a5\3\2\2\2\u06bc\u06ac\3\2\2\2\u06bc\u06b0"+
		"\3\2\2\2\u06bc\u06b7\3\2\2\2\u06bc\u06bb\3\2\2\2\u06bd\u00b5\3\2\2\2\u06be"+
		"\u06bf\7$\2\2\u06bf\u06c0\7^\2\2\u06c0\u06c5\7\u010c\2\2\u06c1\u06c2\7"+
		"\u010b\2\2\u06c2\u06c3\7^\2\2\u06c3\u06c5\7\u010c\2\2\u06c4\u06be\3\2"+
		"\2\2\u06c4\u06c1\3\2\2\2\u06c5\u00b7\3\2\2\2\u06c6\u06c9\5\u00ba^\2\u06c7"+
		"\u06c9\5\u00bc_\2\u06c8\u06c6\3\2\2\2\u06c8\u06c7\3\2\2\2\u06c9\u00b9"+
		"\3\2\2\2\u06ca\u06cb\5\u00be`\2\u06cb\u06cc\7\u0211\2\2\u06cc\u06cd\7"+
		"\u022f\2\2\u06cd\u06ce\7\u0212\2\2\u06ce\u00bb\3\2\2\2\u06cf\u06d0\5\u00be"+
		"`\2\u06d0\u00bd\3\2\2\2\u06d1\u06d3\7Z\2\2\u06d2\u06d4\7\u008b\2\2\u06d3"+
		"\u06d2\3\2\2\2\u06d3\u06d4\3\2\2\2\u06d4\u06e9\3\2\2\2\u06d5\u06d7\7Y"+
		"\2\2\u06d6\u06d8\7\u008b\2\2\u06d7\u06d6\3\2\2\2\u06d7\u06d8\3\2\2\2\u06d8"+
		"\u06e9\3\2\2\2\u06d9\u06e9\7\u011c\2\2\u06da\u06db\7\u008c\2\2\u06db\u06dd"+
		"\7Z\2\2\u06dc\u06de\7\u008b\2\2\u06dd\u06dc\3\2\2\2\u06dd\u06de\3\2\2"+
		"\2\u06de\u06e9\3\2\2\2\u06df\u06e0\7\u008c\2\2\u06e0\u06e2\7Y\2\2\u06e1"+
		"\u06e3\7\u008b\2\2\u06e2\u06e1\3\2\2\2\u06e2\u06e3\3\2\2\2\u06e3\u06e9"+
		"\3\2\2\2\u06e4\u06e6\7\u008d\2\2\u06e5\u06e7\7\u008b\2\2\u06e6\u06e5\3"+
		"\2\2\2\u06e6\u06e7\3\2\2\2\u06e7\u06e9\3\2\2\2\u06e8\u06d1\3\2\2\2\u06e8"+
		"\u06d5\3\2\2\2\u06e8\u06d9\3\2\2\2\u06e8\u06da\3\2\2\2\u06e8\u06df\3\2"+
		"\2\2\u06e8\u06e4\3\2\2\2\u06e9\u00bf\3\2\2\2\u06ea\u06eb\7\u0211\2\2\u06eb"+
		"\u06ec\7\u022f\2\2\u06ec\u06ef\7\u0212\2\2\u06ed\u06ef\3\2\2\2\u06ee\u06ea"+
		"\3\2\2\2\u06ee\u06ed\3\2\2\2\u06ef\u00c1\3\2\2\2\u06f0\u06f1\bb\1\2\u06f1"+
		"\u06f2\7\u0205\2\2\u06f2\u06f3\5\u00c4c\2\u06f3\u06f9\3\2\2\2\u06f4\u06f5"+
		"\f\3\2\2\u06f5\u06f6\7\u0205\2\2\u06f6\u06f8\5\u00c4c\2\u06f7\u06f4\3"+
		"\2\2\2\u06f8\u06fb\3\2\2\2\u06f9\u06f7\3\2\2\2\u06f9\u06fa\3\2\2\2\u06fa"+
		"\u00c3\3\2\2\2\u06fb\u06f9\3\2\2\2\u06fc\u06fd\5\u00c6d\2\u06fd\u00c5"+
		"\3\2\2\2\u06fe\u0703\5\n\6\2\u06ff\u0703\5\u014e\u00a8\2\u0700\u0703\5"+
		"\22\n\2\u0701\u0703\5\4\3\2\u0702\u06fe\3\2\2\2\u0702\u06ff\3\2\2\2\u0702"+
		"\u0700\3\2\2\2\u0702\u0701\3\2\2\2\u0703\u00c7\3\2\2\2\u0704\u0707\5\u00ca"+
		"f\2\u0705\u0707\5\u00ccg\2\u0706\u0704\3\2\2\2\u0706\u0705\3\2\2\2\u0707"+
		"\u00c9\3\2\2\2\u0708\u070a\7\u012a\2\2\u0709\u070b\7\u008b\2\2\u070a\u0709"+
		"\3\2\2\2\u070a\u070b\3\2\2\2\u070b\u070c\3\2\2\2\u070c\u070d\7\u0211\2"+
		"\2\u070d\u070e\5\u00a8U\2\u070e\u070f\7\u0212\2\2\u070f\u00cb\3\2\2\2"+
		"\u0710\u0712\7\u012a\2\2\u0711\u0713\7\u008b\2\2\u0712\u0711\3\2\2\2\u0712"+
		"\u0713\3\2\2\2\u0713\u00cd\3\2\2\2\u0714\u0715\7\\\2\2\u0715\u00cf\3\2"+
		"\2\2\u0716\u0733\7b\2\2\u0717\u0733\7d\2\2\u0718\u0733\7f\2\2\u0719\u0733"+
		"\7g\2\2\u071a\u0733\7h\2\2\u071b\u0733\5\u00d4k\2\u071c\u071d\7b\2\2\u071d"+
		"\u071e\7=\2\2\u071e\u0733\7d\2\2\u071f\u0720\7f\2\2\u0720\u0721\7=\2\2"+
		"\u0721\u0733\7g\2\2\u0722\u0723\7f\2\2\u0723\u0724\7=\2\2\u0724\u0733"+
		"\7h\2\2\u0725\u0726\7f\2\2\u0726\u0727\7=\2\2\u0727\u0733\5\u00d4k\2\u0728"+
		"\u0729\7g\2\2\u0729\u072a\7=\2\2\u072a\u0733\7h\2\2\u072b\u072c\7g\2\2"+
		"\u072c\u072d\7=\2\2\u072d\u0733\5\u00d4k\2\u072e\u072f\7h\2\2\u072f\u0730"+
		"\7=\2\2\u0730\u0733\5\u00d4k\2\u0731\u0733\3\2\2\2\u0732\u0716\3\2\2\2"+
		"\u0732\u0717\3\2\2\2\u0732\u0718\3\2\2\2\u0732\u0719\3\2\2\2\u0732\u071a"+
		"\3\2\2\2\u0732\u071b\3\2\2\2\u0732\u071c\3\2\2\2\u0732\u071f\3\2\2\2\u0732"+
		"\u0722\3\2\2\2\u0732\u0725\3\2\2\2\u0732\u0728\3\2\2\2\u0732\u072b\3\2"+
		"\2\2\u0732\u072e\3\2\2\2\u0732\u0731\3\2\2\2\u0733\u00d1\3\2\2\2\u0734"+
		"\u073e\bj\1\2\u0735\u0736\f\5\2\2\u0736\u0737\7\u0215\2\2\u0737\u073d"+
		"\7\u0216\2\2\u0738\u0739\f\4\2\2\u0739\u073a\7\u0215\2\2\u073a\u073b\7"+
		"\u022f\2\2\u073b\u073d\7\u0216\2\2\u073c\u0735\3\2\2\2\u073c\u0738\3\2"+
		"\2\2\u073d\u0740\3\2\2\2\u073e\u073c\3\2\2\2\u073e\u073f\3\2\2\2\u073f"+
		"\u00d3\3\2\2\2\u0740\u073e\3\2\2\2\u0741\u0747\7i\2\2\u0742\u0743\7i\2"+
		"\2\u0743\u0744\7\u0211\2\2\u0744\u0745\7\u022f\2\2\u0745\u0747\7\u0212"+
		"\2\2\u0746\u0741\3\2\2\2\u0746\u0742\3\2\2\2\u0747\u00d5\3\2\2\2\u0748"+
		"\u0749\t\r\2\2\u0749\u00d7\3\2\2\2\u074a\u074b\5\62\32\2\u074b\u074c\7"+
		",\2\2\u074c\u074d\5\u00a8U\2\u074d\u0752\3\2\2\2\u074e\u074f\7,\2\2\u074f"+
		"\u0752\5\u00a8U\2\u0750\u0752\5\u00a8U\2\u0751\u074a\3\2\2\2\u0751\u074e"+
		"\3\2\2\2\u0751\u0750\3\2\2\2\u0752\u00d9\3\2\2\2\u0753\u0754\5\62\32\2"+
		"\u0754\u0755\5\u00dco\2\u0755\u0756\5\u00dep\2\u0756\u0757\5\u00e0q\2"+
		"\u0757\u075d\3\2\2\2\u0758\u0759\5\62\32\2\u0759\u075a\5\u00dco\2\u075a"+
		"\u075b\5\u00dep\2\u075b\u075d\3\2\2\2\u075c\u0753\3\2\2\2\u075c\u0758"+
		"\3\2\2\2\u075d\u00db\3\2\2\2\u075e\u075f\7\u0168\2\2\u075f\u0760\5\62"+
		"\32\2\u0760\u00dd\3\2\2\2\u0761\u0762\7,\2\2\u0762\u0763\5\62\32\2\u0763"+
		"\u00df\3\2\2\2\u0764\u0765\7<\2\2\u0765\u0766\5\62\32\2\u0766\u00e1\3"+
		"\2\2\2\u0767\u0768\5\64\33\2\u0768\u0769\7G\2\2\u0769\u076a\5\64\33\2"+
		"\u076a\u076d\3\2\2\2\u076b\u076d\3\2\2\2\u076c\u0767\3\2\2\2\u076c\u076b"+
		"\3\2\2\2\u076d\u00e3\3\2\2\2\u076e\u076f\5\62\32\2\u076f\u0770\5\u00de"+
		"p\2\u0770\u0771\5\u00e0q\2\u0771\u077f\3\2\2\2\u0772\u0773\5\62\32\2\u0773"+
		"\u0774\5\u00e0q\2\u0774\u0775\5\u00dep\2\u0775\u077f\3\2\2\2\u0776\u0777"+
		"\5\62\32\2\u0777\u0778\5\u00dep\2\u0778\u077f\3\2\2\2\u0779\u077a\5\62"+
		"\32\2\u077a\u077b\5\u00e0q\2\u077b\u077f\3\2\2\2\u077c\u077f\5\u00a8U"+
		"\2\u077d\u077f\3\2\2\2\u077e\u076e\3\2\2\2\u077e\u0772\3\2\2\2\u077e\u0776"+
		"\3\2\2\2\u077e\u0779\3\2\2\2\u077e\u077c\3\2\2\2\u077e\u077d\3\2\2\2\u077f"+
		"\u00e5\3\2\2\2\u0780\u0781\7\u014b\2\2\u0781\u0782\7\u0211\2\2\u0782\u0783"+
		"\5\u00e8u\2\u0783\u0784\7\u0212\2\2\u0784\u00e7\3\2\2\2\u0785\u078a\5"+
		"\u00eav\2\u0786\u0787\7\u0217\2\2\u0787\u0789\5\u00eav\2\u0788\u0786\3"+
		"\2\2\2\u0789\u078c\3\2\2\2\u078a\u0788\3\2\2\2\u078a\u078b\3\2\2\2\u078b"+
		"\u00e9\3\2\2\2\u078c\u078a\3\2\2\2\u078d\u078e\5\62\32\2\u078e\u078f\7"+
		"\67\2\2\u078f\u0790\5\n\6\2\u0790\u0793\3\2\2\2\u0791\u0793\5\62\32\2"+
		"\u0792\u078d\3\2\2\2\u0792\u0791\3\2\2\2\u0793\u00eb\3\2\2\2\u0794\u0795"+
		"\7\u014d\2\2\u0795\u07a4\5\66\34\2\u0796\u0797\7\u014d\2\2\u0797\u0798"+
		"\5\66\34\2\u0798\u0799\5\u00eex\2\u0799\u07a4\3\2\2\2\u079a\u079b\7\u014d"+
		"\2\2\u079b\u079c\5\u00eex\2\u079c\u079d\5\66\34\2\u079d\u07a4\3\2\2\2"+
		"\u079e\u079f\7\u014d\2\2\u079f\u07a0\5\u00eex\2\u07a0\u07a1\5\66\34\2"+
		"\u07a1\u07a2\5\u00eex\2\u07a2\u07a4\3\2\2\2\u07a3\u0794\3\2\2\2\u07a3"+
		"\u0796\3\2\2\2\u07a3\u079a\3\2\2\2\u07a3\u079e\3\2\2\2\u07a4\u00ed\3\2"+
		"\2\2\u07a5\u07a6\7M\2\2\u07a6\u07aa\7\u014c\2\2\u07a7\u07a8\7M\2\2\u07a8"+
		"\u07aa\7\u008e\2\2\u07a9\u07a5\3\2\2\2\u07a9\u07a7\3\2\2\2\u07aa\u00ef"+
		"\3\2\2\2\u07ab\u07ac\t\16\2\2\u07ac\u00f1\3\2\2\2\u07ad\u07ae\7q\2\2\u07ae"+
		"\u07b3\7\u0166\2\2\u07af\u07b0\7\u0165\2\2\u07b0\u07b3\7\u0166\2\2\u07b1"+
		"\u07b3\3\2\2\2\u07b2\u07ad\3\2\2\2\u07b2\u07af\3\2\2\2\u07b2\u07b1\3\2"+
		"\2\2\u07b3\u00f3\3\2\2\2\u07b4\u07b5\7\u014e\2\2\u07b5\u07ba\5\62\32\2"+
		"\u07b6\u07b7\7\u014e\2\2\u07b7\u07b8\7\u00b0\2\2\u07b8\u07ba\7\u008e\2"+
		"\2\u07b9\u07b4\3\2\2\2\u07b9\u07b6\3\2\2\2\u07ba\u00f5\3\2\2\2\u07bb\u07bc"+
		"\7\u0217\2\2\u07bc\u07bd\7\u0150\2\2\u07bd\u07c6\7\u014f\2\2\u07be\u07bf"+
		"\7\u0217\2\2\u07bf\u07c0\7\u0150\2\2\u07c0\u07c6\7\u00b0\2\2\u07c1\u07c2"+
		"\7\u0217\2\2\u07c2\u07c3\7\u0150\2\2\u07c3\u07c4\7\u00b0\2\2\u07c4\u07c6"+
		"\7\u008e\2\2\u07c5\u07bb\3\2\2\2\u07c5\u07be\3\2\2\2\u07c5\u07c1\3\2\2"+
		"\2\u07c6\u00f7\3\2\2\2\u07c7\u07c8\5\u009eP\2\u07c8\u07c9\5\u00fc\177"+
		"\2\u07c9\u00f9\3\2\2\2\u07ca\u07cf\5\u00f8}\2\u07cb\u07cc\7\u0217\2\2"+
		"\u07cc\u07ce\5\u00f8}\2\u07cd\u07cb\3\2\2\2\u07ce\u07d1\3\2\2\2\u07cf"+
		"\u07cd\3\2\2\2\u07cf\u07d0\3\2\2\2\u07d0\u00fb\3\2\2\2\u07d1\u07cf\3\2"+
		"\2\2\u07d2\u07d3\7\67\2\2\u07d3\u07d4\7\u0211\2\2\u07d4\u07d5\5\u00fe"+
		"\u0080\2\u07d5\u07d6\7\u0212\2\2\u07d6\u00fd\3\2\2\2\u07d7\u07dc\5\u0100"+
		"\u0081\2\u07d8\u07d9\7\u0217\2\2\u07d9\u07db\5\u0100\u0081\2\u07da\u07d8"+
		"\3\2\2\2\u07db\u07de\3\2\2\2\u07dc\u07da\3\2\2\2\u07dc\u07dd\3\2\2\2\u07dd"+
		"\u00ff\3\2\2\2\u07de\u07dc\3\2\2\2\u07df\u07e0\5\u0084C\2\u07e0\u07e2"+
		"\5\u00a4S\2\u07e1\u07e3\5\u0102\u0082\2\u07e2\u07e1\3\2\2\2\u07e2\u07e3"+
		"\3\2\2\2\u07e3\u0101\3\2\2\2\u07e4\u07e6\7\u00be\2\2\u07e5\u07e7\7\u0209"+
		"\2\2\u07e6\u07e5\3\2\2\2\u07e6\u07e7\3\2\2\2\u07e7\u07e8\3\2\2\2\u07e8"+
		"\u07e9\5\u0104\u0083\2\u07e9\u0103\3\2\2\2\u07ea\u07ef\5\u0084C\2\u07eb"+
		"\u07ec\5\u0084C\2\u07ec\u07ed\5\u00c2b\2\u07ed\u07ef\3\2\2\2\u07ee\u07ea"+
		"\3\2\2\2\u07ee\u07eb\3\2\2\2\u07ef\u0105\3\2\2\2\u07f0\u07f1\7\67\2\2"+
		"\u07f1\u07f2\5\u0084C\2\u07f2\u07f3\7\u0211\2\2\u07f3\u07f4\5\u0108\u0085"+
		"\2\u07f4\u07f5\7\u0212\2\2\u07f5\u07ff\3\2\2\2\u07f6\u07f7\7\67\2\2\u07f7"+
		"\u07ff\5\u0084C\2\u07f8\u07f9\5\u0084C\2\u07f9\u07fa\7\u0211\2\2\u07fa"+
		"\u07fb\5\u0108\u0085\2\u07fb\u07fc\7\u0212\2\2\u07fc\u07ff\3\2\2\2\u07fd"+
		"\u07ff\5\u0084C\2\u07fe\u07f0\3\2\2\2\u07fe\u07f6\3\2\2\2\u07fe\u07f8"+
		"\3\2\2\2\u07fe\u07fd\3\2\2\2\u07ff\u0107\3\2\2\2\u0800\u0801\b\u0085\1"+
		"\2\u0801\u0802\5\34\17\2\u0802\u0808\3\2\2\2\u0803\u0804\f\3\2\2\u0804"+
		"\u0805\7\u0217\2\2\u0805\u0807\5\34\17\2\u0806\u0803\3\2\2\2\u0807\u080a"+
		"\3\2\2\2\u0808\u0806\3\2\2\2\u0808\u0809\3\2\2\2\u0809\u0109\3\2\2\2\u080a"+
		"\u0808\3\2\2\2\u080b\u081d\5\u0106\u0084\2\u080c\u080d\7\67\2\2\u080d"+
		"\u080e\7\u0211\2\2\u080e\u080f\5\u00fe\u0080\2\u080f\u0810\7\u0212\2\2"+
		"\u0810\u081d\3\2\2\2\u0811\u0812\7\67\2\2\u0812\u0813\5\u0084C\2\u0813"+
		"\u0814\7\u0211\2\2\u0814\u0815\5\u00fe\u0080\2\u0815\u0816\7\u0212\2\2"+
		"\u0816\u081d\3\2\2\2\u0817\u0818\5\u0084C\2\u0818\u0819\7\u0211\2\2\u0819"+
		"\u081a\5\u00fe\u0080\2\u081a\u081b\7\u0212\2\2\u081b\u081d\3\2\2\2\u081c"+
		"\u080b\3\2\2\2\u081c\u080c\3\2\2\2\u081c\u0811\3\2\2\2\u081c\u0817\3\2"+
		"\2\2\u081d\u010b\3\2\2\2\u081e\u081f\7\u0136\2\2\u081f\u0820\5~@\2\u0820"+
		"\u0821\7\u0211\2\2\u0821\u0822\5\u00a8U\2\u0822\u0824\7\u0212\2\2\u0823"+
		"\u0825\5\u010e\u0088\2\u0824\u0823\3\2\2\2\u0824\u0825\3\2\2\2\u0825\u010d"+
		"\3\2\2\2\u0826\u0827\7\u0086\2\2\u0827\u0828\7\u0211\2\2\u0828\u0829\5"+
		"\62\32\2\u0829\u082a\7\u0212\2\2\u082a\u010f\3\2\2\2\u082b\u082c\t\17"+
		"\2\2\u082c\u0111\3\2\2\2\u082d\u082e\7K\2\2\u082e\u082f\7M\2\2\u082f\u0830"+
		"\5\u0114\u008b\2\u0830\u0113\3\2\2\2\u0831\u0836\5\u0116\u008c\2\u0832"+
		"\u0833\7\u0217\2\2\u0833\u0835\5\u0116\u008c\2\u0834\u0832\3\2\2\2\u0835"+
		"\u0838\3\2\2\2\u0836\u0834\3\2\2\2\u0836\u0837\3\2\2\2\u0837\u0115\3\2"+
		"\2\2\u0838\u0836\3\2\2\2\u0839\u083a\5\62\32\2\u083a\u083b\7\65\2\2\u083b"+
		"\u083d\5Z.\2\u083c\u083e\5\u0118\u008d\2\u083d\u083c\3\2\2\2\u083d\u083e"+
		"\3\2\2\2\u083e\u0847\3\2\2\2\u083f\u0841\5\62\32\2\u0840\u0842\5\\/\2"+
		"\u0841\u0840\3\2\2\2\u0841\u0842\3\2\2\2\u0842\u0844\3\2\2\2\u0843\u0845"+
		"\5\u0118\u008d\2\u0844\u0843\3\2\2\2\u0844\u0845\3\2\2\2\u0845\u0847\3"+
		"\2\2\2\u0846\u0839\3\2\2\2\u0846\u083f\3\2\2\2\u0847\u0117\3\2\2\2\u0848"+
		"\u0849\7\u00e8\2\2\u0849\u084d\7\u00d2\2\2\u084a\u084b\7\u00e8\2\2\u084b"+
		"\u084d\7\u00e1\2\2\u084c\u0848\3\2\2\2\u084c\u084a\3\2\2\2\u084d\u0119"+
		"\3\2\2\2\u084e\u0856\7&\2\2\u084f\u0850\7&\2\2\u0850\u0851\78\2\2\u0851"+
		"\u0852\7\u0211\2\2\u0852\u0853\5\u00a8U\2\u0853\u0854\7\u0212\2\2\u0854"+
		"\u0856\3\2\2\2\u0855\u084e\3\2\2\2\u0855\u084f\3\2\2\2\u0856\u011b\3\2"+
		"\2\2\u0857\u0858\7&\2\2\u0858\u011d\3\2\2\2\u0859\u085a\7\u00ec\2\2\u085a"+
		"\u085e\5\u0120\u0091\2\u085b\u085c\7\u00ec\2\2\u085c\u085e\5\u0084C\2"+
		"\u085d\u0859\3\2\2\2\u085d\u085b\3\2\2\2\u085e\u011f\3\2\2\2\u085f\u0861"+
		"\7\u0211\2\2\u0860\u0862\5\u0122\u0092\2\u0861\u0860\3\2\2\2\u0861\u0862"+
		"\3\2\2\2\u0862\u0864\3\2\2\2\u0863\u0865\5\u0124\u0093\2\u0864\u0863\3"+
		"\2\2\2\u0864\u0865\3\2\2\2\u0865\u0867\3\2\2\2\u0866\u0868\5\u0112\u008a"+
		"\2\u0867\u0866\3\2\2\2\u0867\u0868\3\2\2\2\u0868\u086a\3\2\2\2\u0869\u086b"+
		"\5`\61\2\u086a\u0869\3\2\2\2\u086a\u086b\3\2\2\2\u086b\u086c\3\2\2\2\u086c"+
		"\u086d\7\u0212\2\2\u086d\u0121\3\2\2\2\u086e\u086f\5\u0084C\2\u086f\u0123"+
		"\3\2\2\2\u0870\u0871\7\u00a7\2\2\u0871\u0872\7M\2\2\u0872\u0873\5\u00a8"+
		"U\2\u0873\u0125\3\2\2\2\u0874\u0879\5\u012a\u0096\2\u0875\u0876\7\u0217"+
		"\2\2\u0876\u0878\5\u012a\u0096\2\u0877\u0875\3\2\2\2\u0878\u087b\3\2\2"+
		"\2\u0879\u0877\3\2\2\2\u0879\u087a\3\2\2\2\u087a\u0127\3\2\2\2\u087b\u0879"+
		"\3\2\2\2\u087c\u087e\5\u012c\u0097\2\u087d\u087c\3\2\2\2\u087d\u087e\3"+
		"\2\2\2\u087e\u087f\3\2\2\2\u087f\u0881\5\u012e\u0098\2\u0880\u0882\5\\"+
		"/\2\u0881\u0880\3\2\2\2\u0881\u0882\3\2\2\2\u0882\u0884\3\2\2\2\u0883"+
		"\u0885\5\u0118\u008d\2\u0884\u0883\3\2\2\2\u0884\u0885\3\2\2\2\u0885\u0892"+
		"\3\2\2\2\u0886\u0888\5\u012c\u0097\2\u0887\u0886\3\2\2\2\u0887\u0888\3"+
		"\2\2\2\u0888\u0889\3\2\2\2\u0889\u088a\5\u0104\u0083\2\u088a\u088c\5\u0130"+
		"\u0099\2\u088b\u088d\5\\/\2\u088c\u088b\3\2\2\2\u088c\u088d\3\2\2\2\u088d"+
		"\u088f\3\2\2\2\u088e\u0890\5\u0118\u008d\2\u088f\u088e\3\2\2\2\u088f\u0890"+
		"\3\2\2\2\u0890\u0892\3\2\2\2\u0891\u087d\3\2\2\2\u0891\u0887\3\2\2\2\u0892"+
		"\u0129\3\2\2\2\u0893\u0894\5\u0084C\2\u0894\u0895\5\u0128\u0095\2\u0895"+
		"\u089f\3\2\2\2\u0896\u0897\5\u009eP\2\u0897\u0898\5\u0128\u0095\2\u0898"+
		"\u089f\3\2\2\2\u0899\u089a\7\u0211\2\2\u089a\u089b\5\62\32\2\u089b\u089c"+
		"\7\u0212\2\2\u089c\u089d\5\u0128\u0095\2\u089d\u089f\3\2\2\2\u089e\u0893"+
		"\3\2\2\2\u089e\u0896\3\2\2\2\u089e\u0899\3\2\2\2\u089f\u012b\3\2\2\2\u08a0"+
		"\u08a1\7\u00be\2\2\u08a1\u08a2\5\u0104\u0083\2\u08a2\u012d\3\2\2\2\u08a3"+
		"\u08a6\5\u0104\u0083\2\u08a4\u08a6\3\2\2\2\u08a5\u08a3\3\2\2\2\u08a5\u08a4"+
		"\3\2\2\2\u08a6\u012f\3\2\2\2\u08a7\u08a8\7\u0211\2\2\u08a8\u08a9\5\u0132"+
		"\u009a\2\u08a9\u08aa\7\u0212\2\2\u08aa\u0131\3\2\2\2\u08ab\u08b0\5\u0134"+
		"\u009b\2\u08ac\u08ad\7\u0217\2\2\u08ad\u08af\5\u0134\u009b\2\u08ae\u08ac"+
		"\3\2\2\2\u08af\u08b2\3\2\2\2\u08b0\u08ae\3\2\2\2\u08b0\u08b1\3\2\2\2\u08b1"+
		"\u0133\3\2\2\2\u08b2\u08b0\3\2\2\2\u08b3\u08b4\5&\24\2\u08b4\u08b5\7\u0209"+
		"\2\2\u08b5\u08b6\5\u0136\u009c\2\u08b6\u08c3\3\2\2\2\u08b7\u08c3\5&\24"+
		"\2\u08b8\u08b9\5&\24\2\u08b9\u08ba\7\u0205\2\2\u08ba\u08bb\5&\24\2\u08bb"+
		"\u08bc\7\u0209\2\2\u08bc\u08bd\5\u0136\u009c\2\u08bd\u08c3\3\2\2\2\u08be"+
		"\u08bf\5&\24\2\u08bf\u08c0\7\u0205\2\2\u08c0\u08c1\5&\24\2\u08c1\u08c3"+
		"\3\2\2\2\u08c2\u08b3\3\2\2\2\u08c2\u08b7\3\2\2\2\u08c2\u08b8\3\2\2\2\u08c2"+
		"\u08be\3\2\2\2\u08c3\u0135\3\2\2\2\u08c4\u08cb\5\u0138\u009d\2\u08c5\u08cb"+
		"\5\4\3\2\u08c6\u08cb\5Z.\2\u08c7\u08cb\7\u022f\2\2\u08c8\u08cb\7\u022e"+
		"\2\2\u08c9\u08cb\7\u016b\2\2\u08ca\u08c4\3\2\2\2\u08ca\u08c5\3\2\2\2\u08ca"+
		"\u08c6\3\2\2\2\u08ca\u08c7\3\2\2\2\u08ca\u08c8\3\2\2\2\u08ca\u08c9\3\2"+
		"\2\2\u08cb\u0137\3\2\2\2\u08cc\u08d9\5\u00a4S\2\u08cd\u08ce\5\u0086D\2"+
		"\u08ce\u08cf\5\u00c2b\2\u08cf\u08d0\7\u01fe\2\2\u08d0\u08d1\7\u0081\2"+
		"\2\u08d1\u08d9\3\2\2\2\u08d2\u08d3\7\u0146\2\2\u08d3\u08d4\5\u0086D\2"+
		"\u08d4\u08d5\5\u00c2b\2\u08d5\u08d6\7\u01fe\2\2\u08d6\u08d7\7\u0081\2"+
		"\2\u08d7\u08d9\3\2\2\2\u08d8\u08cc\3\2\2\2\u08d8\u08cd\3\2\2\2\u08d8\u08d2"+
		"\3\2\2\2\u08d9\u0139\3\2\2\2\u08da\u08db\7\5\2\2\u08db\u013b\3\2\2\2\u08dc"+
		"\u08de\5\u013e\u00a0\2\u08dd\u08df\5\u0140\u00a1\2\u08de\u08dd\3\2\2\2"+
		"\u08de\u08df\3\2\2\2\u08df\u08e1\3\2\2\2\u08e0\u08e2\5\u0142\u00a2\2\u08e1"+
		"\u08e0\3\2\2\2\u08e1\u08e2\3\2\2\2\u08e2\u08e4\3\2\2\2\u08e3\u08e5\5\u0102"+
		"\u0082\2\u08e4\u08e3\3\2\2\2\u08e4\u08e5\3\2\2\2\u08e5\u08f8\3\2\2\2\u08e6"+
		"\u08e7\5\u013e\u00a0\2\u08e7\u08e8\7\u0211\2\2\u08e8\u08ed\7\u022e\2\2"+
		"\u08e9\u08ea\7\u0217\2\2\u08ea\u08ec\7\u022e\2\2\u08eb\u08e9\3\2\2\2\u08ec"+
		"\u08ef\3\2\2\2\u08ed\u08eb\3\2\2\2\u08ed\u08ee\3\2\2\2\u08ee\u08f0\3\2"+
		"\2\2\u08ef\u08ed\3\2\2\2\u08f0\u08f2\7\u0212\2\2\u08f1\u08f3\5\u0142\u00a2"+
		"\2\u08f2\u08f1\3\2\2\2\u08f2\u08f3\3\2\2\2\u08f3\u08f5\3\2\2\2\u08f4\u08f6"+
		"\5\u0102\u0082\2\u08f5\u08f4\3\2\2\2\u08f5\u08f6\3\2\2\2\u08f6\u08f8\3"+
		"\2\2\2\u08f7\u08dc\3\2\2\2\u08f7\u08e6\3\2\2\2\u08f8\u013d\3\2\2\2\u08f9"+
		"\u0934\7\u0111\2\2\u08fa\u0934\7\u0112\2\2\u08fb\u0934\7\u0113\2\2\u08fc"+
		"\u0934\7\u0114\2\2\u08fd\u0934\7\u0082\2\2\u08fe\u0934\7~\2\2\u08ff\u0934"+
		"\7\u0083\2\2\u0900\u0934\7\u0080\2\2\u0901\u0934\7\u0084\2\2\u0902\u0934"+
		"\7\177\2\2\u0903\u0934\7\u0115\2\2\u0904\u0934\7\u0116\2\2\u0905\u0934"+
		"\7\u0117\2\2\u0906\u0907\7X\2\2\u0907\u0934\7\35\2\2\u0908\u0934\7\u0118"+
		"\2\2\u0909\u0934\7\u0119\2\2\u090a\u0934\7\u011a\2\2\u090b\u0934\7\u011b"+
		"\2\2\u090c\u0934\7\u011c\2\2\u090d\u0934\7Z\2\2\u090e\u0934\7Y\2\2\u090f"+
		"\u0934\7\u0085\2\2\u0910\u0934\7{\2\2\u0911\u0934\7\u011d\2\2\u0912\u0934"+
		"\7_\2\2\u0913\u0934\7]\2\2\u0914\u0934\7^\2\2\u0915\u0934\7\\\2\2\u0916"+
		"\u0934\7W\2\2\u0917\u0934\7\u011e\2\2\u0918\u0934\7\u011f\2\2\u0919\u0934"+
		"\7\u0120\2\2\u091a\u0934\7\u0121\2\2\u091b\u0934\7\u0122\2\2\u091c\u0934"+
		"\7\u0123\2\2\u091d\u0934\7\u0124\2";
	private static final String _serializedATNSegment1 =
		"\2\u091e\u0934\7\u0125\2\2\u091f\u0934\7\u0126\2\2\u0920\u0934\7\u0127"+
		"\2\2\u0921\u0934\7\u0128\2\2\u0922\u0934\7\u0129\2\2\u0923\u0934\7\u012a"+
		"\2\2\u0924\u0934\7\u012b\2\2\u0925\u0934\7\u012c\2\2\u0926\u0934\7\u012d"+
		"\2\2\u0927\u0934\7\u012e\2\2\u0928\u0934\7\u012f\2\2\u0929\u0934\7\u0130"+
		"\2\2\u092a\u0934\7\u0131\2\2\u092b\u0934\7\u0132\2\2\u092c\u0934\7\u0133"+
		"\2\2\u092d\u0934\7\u0134\2\2\u092e\u0934\7\u0135\2\2\u092f\u0934\7[\2"+
		"\2\u0930\u0934\5\n\6\2\u0931\u0934\5\u00b4[\2\u0932\u0934\5\u00a4S\2\u0933"+
		"\u08f9\3\2\2\2\u0933\u08fa\3\2\2\2\u0933\u08fb\3\2\2\2\u0933\u08fc\3\2"+
		"\2\2\u0933\u08fd\3\2\2\2\u0933\u08fe\3\2\2\2\u0933\u08ff\3\2\2\2\u0933"+
		"\u0900\3\2\2\2\u0933\u0901\3\2\2\2\u0933\u0902\3\2\2\2\u0933\u0903\3\2"+
		"\2\2\u0933\u0904\3\2\2\2\u0933\u0905\3\2\2\2\u0933\u0906\3\2\2\2\u0933"+
		"\u0908\3\2\2\2\u0933\u0909\3\2\2\2\u0933\u090a\3\2\2\2\u0933\u090b\3\2"+
		"\2\2\u0933\u090c\3\2\2\2\u0933\u090d\3\2\2\2\u0933\u090e\3\2\2\2\u0933"+
		"\u090f\3\2\2\2\u0933\u0910\3\2\2\2\u0933\u0911\3\2\2\2\u0933\u0912\3\2"+
		"\2\2\u0933\u0913\3\2\2\2\u0933\u0914\3\2\2\2\u0933\u0915\3\2\2\2\u0933"+
		"\u0916\3\2\2\2\u0933\u0917\3\2\2\2\u0933\u0918\3\2\2\2\u0933\u0919\3\2"+
		"\2\2\u0933\u091a\3\2\2\2\u0933\u091b\3\2\2\2\u0933\u091c\3\2\2\2\u0933"+
		"\u091d\3\2\2\2\u0933\u091e\3\2\2\2\u0933\u091f\3\2\2\2\u0933\u0920\3\2"+
		"\2\2\u0933\u0921\3\2\2\2\u0933\u0922\3\2\2\2\u0933\u0923\3\2\2\2\u0933"+
		"\u0924\3\2\2\2\u0933\u0925\3\2\2\2\u0933\u0926\3\2\2\2\u0933\u0927\3\2"+
		"\2\2\u0933\u0928\3\2\2\2\u0933\u0929\3\2\2\2\u0933\u092a\3\2\2\2\u0933"+
		"\u092b\3\2\2\2\u0933\u092c\3\2\2\2\u0933\u092d\3\2\2\2\u0933\u092e\3\2"+
		"\2\2\u0933\u092f\3\2\2\2\u0933\u0930\3\2\2\2\u0933\u0931\3\2\2\2\u0933"+
		"\u0932\3\2\2\2\u0934\u013f\3\2\2\2\u0935\u0936\7\u0211\2\2\u0936\u0939"+
		"\7\u022f\2\2\u0937\u0938\7\u0217\2\2\u0938\u093a\7\u022f\2\2\u0939\u0937"+
		"\3\2\2\2\u0939\u093a\3\2\2\2\u093a\u093b\3\2\2\2\u093b\u093c\7\u0212\2"+
		"\2\u093c\u0141\3\2\2\2\u093d\u093e\t\20\2\2\u093e\u0940\7\23\2\2\u093f"+
		"\u0941\7\u0209\2\2\u0940\u093f\3\2\2\2\u0940\u0941\3\2\2\2\u0941\u0942"+
		"\3\2\2\2\u0942\u0943\5\u0144\u00a3\2\u0943\u0143\3\2\2\2\u0944\u0947\5"+
		"\n\6\2\u0945\u0946\7\u0205\2\2\u0946\u0948\5\n\6\2\u0947\u0945\3\2\2\2"+
		"\u0947\u0948\3\2\2\2\u0948\u0145\3\2\2\2\u0949\u094e\5\u0144\u00a3\2\u094a"+
		"\u094b\7\u0217\2\2\u094b\u094d\5\u0144\u00a3\2\u094c\u094a\3\2\2\2\u094d"+
		"\u0950\3\2\2\2\u094e\u094c\3\2\2\2\u094e\u094f\3\2\2\2\u094f\u0147\3\2"+
		"\2\2\u0950\u094e\3\2\2\2\u0951\u0957\7\u022f\2\2\u0952\u0953\7\u0200\2"+
		"\2\u0953\u0957\7\u022f\2\2\u0954\u0955\7\u0201\2\2\u0955\u0957\7\u022f"+
		"\2\2\u0956\u0951\3\2\2\2\u0956\u0952\3\2\2\2\u0956\u0954\3\2\2\2\u0957"+
		"\u0149\3\2\2\2\u0958\u095e\7C\2\2\u0959\u095e\7D\2\2\u095a\u095e\78\2"+
		"\2\u095b\u095e\5\u014c\u00a7\2\u095c\u095e\7\u022e\2\2\u095d\u0958\3\2"+
		"\2\2\u095d\u0959\3\2\2\2\u095d\u095a\3\2\2\2\u095d\u095b\3\2\2\2\u095d"+
		"\u095c\3\2\2\2\u095e\u014b\3\2\2\2\u095f\u0964\5\n\6\2\u0960\u0964\5\20"+
		"\t\2\u0961\u0964\5\u014e\u00a8\2\u0962\u0964\5\22\n\2\u0963\u095f\3\2"+
		"\2\2\u0963\u0960\3\2\2\2\u0963\u0961\3\2\2\2\u0963\u0962\3\2\2\2\u0964"+
		"\u014d\3\2\2\2\u0965\u0966\t\21\2\2\u0966\u014f\3\2\2\2\u0967\u0968\5"+
		"\u0084C\2\u0968\u0151\3\2\2\2\u0969\u096e\5\n\6\2\u096a\u096e\5\u014c"+
		"\u00a7\2\u096b\u096e\7t\2\2\u096c\u096e\7\u00fd\2\2\u096d\u0969\3\2\2"+
		"\2\u096d\u096a\3\2\2\2\u096d\u096b\3\2\2\2\u096d\u096c\3\2\2\2\u096e\u0153"+
		"\3\2\2\2\u096f\u0970\b\u00ab\1\2\u0970\u0971\5\u0084C\2\u0971\u0977\3"+
		"\2\2\2\u0972\u0973\f\3\2\2\u0973\u0974\7\u0205\2\2\u0974\u0976\5\u0084"+
		"C\2\u0975\u0972\3\2\2\2\u0976\u0979\3\2\2\2\u0977\u0975\3\2\2\2\u0977"+
		"\u0978\3\2\2\2\u0978\u0155\3\2\2\2\u0979\u0977\3\2\2\2\u097a\u097f\5\u0158"+
		"\u00ad\2\u097b\u097c\7\u0217\2\2\u097c\u097e\5\u0158\u00ad\2\u097d\u097b"+
		"\3\2\2\2\u097e\u0981\3\2\2\2\u097f\u097d\3\2\2\2\u097f\u0980\3\2\2\2\u0980"+
		"\u0157\3\2\2\2\u0981\u097f\3\2\2\2\u0982\u0985\5\u014a\u00a6\2\u0983\u0985"+
		"\5\u015c\u00af\2\u0984\u0982\3\2\2\2\u0984\u0983\3\2\2\2\u0985\u0159\3"+
		"\2\2\2\u0986\u0994\7\u022e\2\2\u0987\u0994\5\n\6\2\u0988\u0989\7\\\2\2"+
		"\u0989\u098a\7\u022e\2\2\u098a\u0994\5\u00d0i\2\u098b\u098c\7\\\2\2\u098c"+
		"\u098d\7\u0211\2\2\u098d\u098e\7\u022f\2\2\u098e\u098f\7\u0212\2\2\u098f"+
		"\u0994\7\u022e\2\2\u0990\u0994\5\u015c\u00af\2\u0991\u0994\7k\2\2\u0992"+
		"\u0994\7w\2\2\u0993\u0986\3\2\2\2\u0993\u0987\3\2\2\2\u0993\u0988\3\2"+
		"\2\2\u0993\u098b\3\2\2\2\u0993\u0990\3\2\2\2\u0993\u0991\3\2\2\2\u0993"+
		"\u0992\3\2\2\2\u0994\u015b\3\2\2\2\u0995\u099b\7\u022f\2\2\u0996\u0997"+
		"\7\u0200\2\2\u0997\u099b\7\u022f\2\2\u0998\u0999\7\u0201\2\2\u0999\u099b"+
		"\7\u022f\2\2\u099a\u0995\3\2\2\2\u099a\u0996\3\2\2\2\u099a\u0998\3\2\2"+
		"\2\u099b\u015d\3\2\2\2\u099c\u099d\7\u00b3\2\2\u099d\u09a4\7\u01e1\2\2"+
		"\u099e\u099f\7\u00b3\2\2\u099f\u09a4\7\u0195\2\2\u09a0\u09a1\7\u0086\2"+
		"\2\u09a1\u09a4\7\u00b3\2\2\u09a2\u09a4\7\u01d6\2\2\u09a3\u099c\3\2\2\2"+
		"\u09a3\u099e\3\2\2\2\u09a3\u09a0\3\2\2\2\u09a3\u09a2\3\2\2\2\u09a4\u015f"+
		"\3\2\2\2\u09a5\u09a6\5\u0084C\2\u09a6\u09a8\5\u00a4S\2\u09a7\u09a9\5\u018e"+
		"\u00c8\2\u09a8\u09a7\3\2\2\2\u09a8\u09a9\3\2\2\2\u09a9\u09aa\3\2\2\2\u09aa"+
		"\u09ab\5\u0162\u00b2\2\u09ab\u0161\3\2\2\2\u09ac\u09ae\5\u0164\u00b3\2"+
		"\u09ad\u09ac\3\2\2\2\u09ae\u09b1\3\2\2\2\u09af\u09ad\3\2\2\2\u09af\u09b0"+
		"\3\2\2\2\u09b0\u0163\3\2\2\2\u09b1\u09af\3\2\2\2\u09b2\u09b3\7\27\2\2"+
		"\u09b3\u09b4\5\34\17\2\u09b4\u09b5\5\u0168\u00b5\2\u09b5\u09bb\3\2\2\2"+
		"\u09b6\u09bb\5\u0168\u00b5\2\u09b7\u09bb\5\u0166\u00b4\2\u09b8\u09b9\7"+
		"\u00be\2\2\u09b9\u09bb\5\u0104\u0083\2\u09ba\u09b2\3\2\2\2\u09ba\u09b6"+
		"\3\2\2\2\u09ba\u09b7\3\2\2\2\u09ba\u09b8\3\2\2\2\u09bb\u0165\3\2\2\2\u09bc"+
		"\u09c4\7\u00c7\2\2\u09bd\u09be\7A\2\2\u09be\u09c4\7\u00c7\2\2\u09bf\u09c0"+
		"\7\u00dd\2\2\u09c0\u09c4\7\u00c8\2\2\u09c1\u09c2\7\u00dd\2\2\u09c2\u09c4"+
		"\7\u00d7\2\2\u09c3\u09bc\3\2\2\2\u09c3\u09bd\3\2\2\2\u09c3\u09bf\3\2\2"+
		"\2\u09c3\u09c1\3\2\2\2\u09c4\u0167\3\2\2\2\u09c5\u09c6\7A\2\2\u09c6\u09f9"+
		"\7B\2\2\u09c7\u09f9\7B\2\2\u09c8\u09cb\7\31\2\2\u09c9\u09ca\7$\2\2\u09ca"+
		"\u09cc\5\u017c\u00bf\2\u09cb\u09c9\3\2\2\2\u09cb\u09cc\3\2\2\2\u09cc\u09cd"+
		"\3\2\2\2\u09cd\u09f9\5\u017a\u00be\2\u09ce\u09cf\7\30\2\2\u09cf\u09d2"+
		"\7\33\2\2\u09d0\u09d1\7$\2\2\u09d1\u09d3\5\u017c\u00bf\2\u09d2\u09d0\3"+
		"\2\2\2\u09d2\u09d3\3\2\2\2\u09d3\u09d4\3\2\2\2\u09d4\u09f9\5\u017a\u00be"+
		"\2\u09d5\u09d6\7\u00ac\2\2\u09d6\u09d7\7\u0211\2\2\u09d7\u09d8\5\62\32"+
		"\2\u09d8\u09da\7\u0212\2\2\u09d9\u09db\5\u0178\u00bd\2\u09da\u09d9\3\2"+
		"\2\2\u09da\u09db\3\2\2\2\u09db\u09f9\3\2\2\2\u09dc\u09dd\7k\2\2\u09dd"+
		"\u09f9\5\64\33\2\u09de\u09df\7\u00ad\2\2\u09df\u09e0\5\u0176\u00bc\2\u09e0"+
		"\u09e1\7\67\2\2\u09e1\u09e3\7\u00d6\2\2\u09e2\u09e4\5\u016a\u00b6\2\u09e3"+
		"\u09e2\3\2\2\2\u09e3\u09e4\3\2\2\2\u09e4\u09f9\3\2\2\2\u09e5\u09e6\7\u00ad"+
		"\2\2\u09e6\u09e7\5\u0176\u00bc\2\u09e7\u09e8\7\67\2\2\u09e8\u09e9\7\u0211"+
		"\2\2\u09e9\u09ea\5\62\32\2\u09ea\u09eb\7\u0212\2\2\u09eb\u09ec\7\u01e9"+
		"\2\2\u09ec\u09f9\3\2\2\2\u09ed\u09ee\7\u00b4\2\2\u09ee\u09f0\5\u0082B"+
		"\2\u09ef\u09f1\5\u0170\u00b9\2\u09f0\u09ef\3\2\2\2\u09f0\u09f1\3\2\2\2"+
		"\u09f1\u09f3\3\2\2\2\u09f2\u09f4\5\u018c\u00c7\2\u09f3\u09f2\3\2\2\2\u09f3"+
		"\u09f4\3\2\2\2\u09f4\u09f6\3\2\2\2\u09f5\u09f7\5\u0184\u00c3\2\u09f6\u09f5"+
		"\3\2\2\2\u09f6\u09f7\3\2\2\2\u09f7\u09f9\3\2\2\2\u09f8\u09c5\3\2\2\2\u09f8"+
		"\u09c7\3\2\2\2\u09f8\u09c8\3\2\2\2\u09f8\u09ce\3\2\2\2\u09f8\u09d5\3\2"+
		"\2\2\u09f8\u09dc\3\2\2\2\u09f8\u09de\3\2\2\2\u09f8\u09e5\3\2\2\2\u09f8"+
		"\u09ed\3\2\2\2\u09f9\u0169\3\2\2\2\u09fa\u09fb\7\u0211\2\2\u09fb\u09fc"+
		"\5\u016c\u00b7\2\u09fc\u09fd\7\u0212\2\2\u09fd\u016b\3\2\2\2\u09fe\u0a00"+
		"\5\u016e\u00b8\2\u09ff\u09fe\3\2\2\2\u0a00\u0a01\3\2\2\2\u0a01\u09ff\3"+
		"\2\2\2\u0a01\u0a02\3\2\2\2\u0a02\u016d\3\2\2\2\u0a03\u0a04\7\67\2\2\u0a04"+
		"\u0a29\5\u00a6T\2\u0a05\u0a06\7\u00bb\2\2\u0a06\u0a29\5\u015c\u00af\2"+
		"\u0a07\u0a29\7\u00c3\2\2\u0a08\u0a09\7\u00b0\2\2\u0a09\u0a29\7\u00c3\2"+
		"\2\u0a0a\u0a0c\7\u00d9\2\2\u0a0b\u0a0d\7M\2\2\u0a0c\u0a0b\3\2\2\2\u0a0c"+
		"\u0a0d\3\2\2\2\u0a0d\u0a0e\3\2\2\2\u0a0e\u0a29\5\u015c\u00af\2\u0a0f\u0a10"+
		"\7\u00e5\2\2\u0a10\u0a29\5\u015c\u00af\2\u0a11\u0a12\7\u00e6\2\2\u0a12"+
		"\u0a29\5\u015c\u00af\2\u0a13\u0a14\7\u00b0\2\2\u0a14\u0a29\7\u00e5\2\2"+
		"\u0a15\u0a16\7\u00b0\2\2\u0a16\u0a29\7\u00e6\2\2\u0a17\u0a18\7\u00ed\2"+
		"\2\u0a18\u0a19\7M\2\2\u0a19\u0a29\5\u0104\u0083\2\u0a1a\u0a1b\7\u00fb"+
		"\2\2\u0a1b\u0a1c\7{\2\2\u0a1c\u0a29\5\u0104\u0083\2\u0a1d\u0a1f\7\u00b7"+
		"\2\2\u0a1e\u0a20\7$\2\2\u0a1f\u0a1e\3\2\2\2\u0a1f\u0a20\3\2\2\2\u0a20"+
		"\u0a21\3\2\2\2\u0a21\u0a29\5\u015c\u00af\2\u0a22\u0a29\7\u00f6\2\2\u0a23"+
		"\u0a25\7\u00f6\2\2\u0a24\u0a26\7$\2\2\u0a25\u0a24\3\2\2\2\u0a25\u0a26"+
		"\3\2\2\2\u0a26\u0a27\3\2\2\2\u0a27\u0a29\5\u015c\u00af\2\u0a28\u0a03\3"+
		"\2\2\2\u0a28\u0a05\3\2\2\2\u0a28\u0a07\3\2\2\2\u0a28\u0a08\3\2\2\2\u0a28"+
		"\u0a0a\3\2\2\2\u0a28\u0a0f\3\2\2\2\u0a28\u0a11\3\2\2\2\u0a28\u0a13\3\2"+
		"\2\2\u0a28\u0a15\3\2\2\2\u0a28\u0a17\3\2\2\2\u0a28\u0a1a\3\2\2\2\u0a28"+
		"\u0a1d\3\2\2\2\u0a28\u0a22\3\2\2\2\u0a28\u0a23\3\2\2\2\u0a29\u016f\3\2"+
		"\2\2\u0a2a\u0a2b\7\u0211\2\2\u0a2b\u0a2c\5\u0174\u00bb\2\u0a2c\u0a2d\7"+
		"\u0212\2\2\u0a2d\u0171\3\2\2\2\u0a2e\u0a2f\5\u0084C\2\u0a2f\u0173\3\2"+
		"\2\2\u0a30\u0a35\5\u0172\u00ba\2\u0a31\u0a32\7\u0217\2\2\u0a32\u0a34\5"+
		"\u0172\u00ba\2\u0a33\u0a31\3\2\2\2\u0a34\u0a37\3\2\2\2\u0a35\u0a33\3\2"+
		"\2\2\u0a35\u0a36\3\2\2\2\u0a36\u0175\3\2\2\2\u0a37\u0a35\3\2\2\2\u0a38"+
		"\u0a3c\7\u00aa\2\2\u0a39\u0a3a\7M\2\2\u0a3a\u0a3c\7k\2\2\u0a3b\u0a38\3"+
		"\2\2\2\u0a3b\u0a39\3\2\2\2\u0a3c\u0177\3\2\2\2\u0a3d\u0a3e\7\u00b0\2\2"+
		"\u0a3e\u0a3f\7\u00db\2\2\u0a3f\u0179\3\2\2\2\u0a40\u0a41\7\65\2\2\u0a41"+
		"\u0a42\7\26\2\2\u0a42\u0a43\7\u0102\2\2\u0a43\u0a44\5\34\17\2\u0a44\u017b"+
		"\3\2\2\2\u0a45\u0a46\7\u0211\2\2\u0a46\u0a47\5\u017e\u00c0\2\u0a47\u0a48"+
		"\7\u0212\2\2\u0a48\u017d\3\2\2\2\u0a49\u0a4e\5\u0180\u00c1\2\u0a4a\u0a4b"+
		"\7\u0217\2\2\u0a4b\u0a4d\5\u0180\u00c1\2\u0a4c\u0a4a\3\2\2\2\u0a4d\u0a50"+
		"\3\2\2\2\u0a4e\u0a4c\3\2\2\2\u0a4e\u0a4f\3\2\2\2\u0a4f\u017f\3\2\2\2\u0a50"+
		"\u0a4e\3\2\2\2\u0a51\u0a52\5\u0182\u00c2\2\u0a52\u0a53\7\u0209\2\2\u0a53"+
		"\u0a54\5\u0136\u009c\2\u0a54\u0a57\3\2\2\2\u0a55\u0a57\5\u0182\u00c2\2"+
		"\u0a56\u0a51\3\2\2\2\u0a56\u0a55\3\2\2\2\u0a57\u0181\3\2\2\2\u0a58\u0a5e"+
		"\5\n\6\2\u0a59\u0a5e\5\20\t\2\u0a5a\u0a5e\5\u014e\u00a8\2\u0a5b\u0a5e"+
		"\5\22\n\2\u0a5c\u0a5e\5\4\3\2\u0a5d\u0a58\3\2\2\2\u0a5d\u0a59\3\2\2\2"+
		"\u0a5d\u0a5a\3\2\2\2\u0a5d\u0a5b\3\2\2\2\u0a5d\u0a5c\3\2\2\2\u0a5e\u0183"+
		"\3\2\2\2\u0a5f\u0a68\5\u0188\u00c5\2\u0a60\u0a68\5\u0186\u00c4\2\u0a61"+
		"\u0a62\5\u0188\u00c5\2\u0a62\u0a63\5\u0186\u00c4\2\u0a63\u0a68\3\2\2\2"+
		"\u0a64\u0a65\5\u0186\u00c4\2\u0a65\u0a66\5\u0188\u00c5\2\u0a66\u0a68\3"+
		"\2\2\2\u0a67\u0a5f\3\2\2\2\u0a67\u0a60\3\2\2\2\u0a67\u0a61\3\2\2\2\u0a67"+
		"\u0a64\3\2\2\2\u0a68\u0185\3\2\2\2\u0a69\u0a6a\78\2\2\u0a6a\u0a6b\7\n"+
		"\2\2\u0a6b\u0a6c\5\u018a\u00c6\2\u0a6c\u0187\3\2\2\2\u0a6d\u0a6e\78\2"+
		"\2\u0a6e\u0a6f\7\t\2\2\u0a6f\u0a70\5\u018a\u00c6\2\u0a70\u0189\3\2\2\2"+
		"\u0a71\u0a72\7\u00b0\2\2\u0a72\u0a7a\7\u00ba\2\2\u0a73\u0a7a\7\u00f7\2"+
		"\2\u0a74\u0a7a\7\u00ab\2\2\u0a75\u0a76\7\23\2\2\u0a76\u0a7a\7B\2\2\u0a77"+
		"\u0a78\7\23\2\2\u0a78\u0a7a\7k\2\2\u0a79\u0a71\3\2\2\2\u0a79\u0a73\3\2"+
		"\2\2\u0a79\u0a74\3\2\2\2\u0a79\u0a75\3\2\2\2\u0a79\u0a77\3\2\2\2\u0a7a"+
		"\u018b\3\2\2\2\u0a7b\u0a7c\7\u00e4\2\2\u0a7c\u0a82\7/\2\2\u0a7d\u0a7e"+
		"\7\u00e4\2\2\u0a7e\u0a82\7\u00ef\2\2\u0a7f\u0a80\7\u00e4\2\2\u0a80\u0a82"+
		"\7\u00ff\2\2\u0a81\u0a7b\3\2\2\2\u0a81\u0a7d\3\2\2\2\u0a81\u0a7f\3\2\2"+
		"\2\u0a82\u018d\3\2\2\2\u0a83\u0a84\7\u01c4\2\2\u0a84\u0a85\7\u0211\2\2"+
		"\u0a85\u0a86\5\u0190\u00c9\2\u0a86\u0a87\7\u0212\2\2\u0a87\u018f\3\2\2"+
		"\2\u0a88\u0a8d\5\u0192\u00ca\2\u0a89\u0a8a\7\u0217\2\2\u0a8a\u0a8c\5\u0192"+
		"\u00ca\2\u0a8b\u0a89\3\2\2\2\u0a8c\u0a8f\3\2\2\2\u0a8d\u0a8b\3\2\2\2\u0a8d"+
		"\u0a8e\3\2\2\2\u0a8e\u0191\3\2\2\2\u0a8f\u0a8d\3\2\2\2\u0a90\u0a91\5\u0196"+
		"\u00cc\2\u0a91\u0a92\5\u0194\u00cb\2\u0a92\u0193\3\2\2\2\u0a93\u0a94\7"+
		"\u022e\2\2\u0a94\u0195\3\2\2\2\u0a95\u0a96\5\u00c6d\2\u0a96\u0197\3\2"+
		"\2\2\u0a97\u0a9e\7\u00e7\2\2\u0a98\u0a9e\7/\2\2\u0a99\u0a9e\7k\2\2\u0a9a"+
		"\u0a9b\7\65\2\2\u0a9b\u0a9c\7\26\2\2\u0a9c\u0a9e\5\34\17\2\u0a9d\u0a97"+
		"\3\2\2\2\u0a9d\u0a98\3\2\2\2\u0a9d\u0a99\3\2\2\2\u0a9d\u0a9a\3\2\2\2\u0a9e"+
		"\u0199\3\2\2\2\u0a9f\u0aa0\7\u0211\2\2\u0aa0\u0aa1\5\u00a4S\2\u0aa1\u0aa2"+
		"\7\u0212\2\2\u0aa2\u0ab6\3\2\2\2\u0aa3\u0aa4\7\u0211\2\2\u0aa4\u0aa5\5"+
		"\u00a4S\2\u0aa5\u0aa6\7\u0217\2\2\u0aa6\u0aa7\5\u00a4S\2\u0aa7\u0aa8\7"+
		"\u0212\2\2\u0aa8\u0ab6\3\2\2\2\u0aa9\u0aaa\7\u0211\2\2\u0aaa\u0aab\7\u016b"+
		"\2\2\u0aab\u0aac\7\u0217\2\2\u0aac\u0aad\5\u00a4S\2\u0aad\u0aae\7\u0212"+
		"\2\2\u0aae\u0ab6\3\2\2\2\u0aaf\u0ab0\7\u0211\2\2\u0ab0\u0ab1\5\u00a4S"+
		"\2\u0ab1\u0ab2\7\u0217\2\2\u0ab2\u0ab3\7\u016b\2\2\u0ab3\u0ab4\7\u0212"+
		"\2\2\u0ab4\u0ab6\3\2\2\2\u0ab5\u0a9f\3\2\2\2\u0ab5\u0aa3\3\2\2\2\u0ab5"+
		"\u0aa9\3\2\2\2\u0ab5\u0aaf\3\2\2\2\u0ab6\u019b\3\2\2\2\u0ab7\u0ab8\5\u019e"+
		"\u00d0\2\u0ab8\u0ab9\5v<\2\u0ab9\u0aba\5\u0138\u009d\2\u0aba\u0ac7\3\2"+
		"\2\2\u0abb\u0abc\5v<\2\u0abc\u0abd\5\u019e\u00d0\2\u0abd\u0abe\5\u0138"+
		"\u009d\2\u0abe\u0ac7\3\2\2\2\u0abf\u0ac0\5v<\2\u0ac0\u0ac1\5\u0138\u009d"+
		"\2\u0ac1\u0ac7\3\2\2\2\u0ac2\u0ac3\5\u019e\u00d0\2\u0ac3\u0ac4\5\u0138"+
		"\u009d\2\u0ac4\u0ac7\3\2\2\2\u0ac5\u0ac7\5\u0138\u009d\2\u0ac6\u0ab7\3"+
		"\2\2\2\u0ac6\u0abb\3\2\2\2\u0ac6\u0abf\3\2\2\2\u0ac6\u0ac2\3\2\2\2\u0ac6"+
		"\u0ac5\3\2\2\2\u0ac7\u019d\3\2\2\2\u0ac8\u0acf\7G\2\2\u0ac9\u0acf\7\u01f3"+
		"\2\2\u0aca\u0acf\7\u01f4\2\2\u0acb\u0acc\7G\2\2\u0acc\u0acf\7\u01f3\2"+
		"\2\u0acd\u0acf\7\u015f\2\2\u0ace\u0ac8\3\2\2\2\u0ace\u0ac9\3\2\2\2\u0ace"+
		"\u0aca\3\2\2\2\u0ace\u0acb\3\2\2\2\u0ace\u0acd\3\2\2\2\u0acf\u019f\3\2"+
		"\2\2\u0ad0\u0ad5\5\u019c\u00cf\2\u0ad1\u0ad2\7\u0217\2\2\u0ad2\u0ad4\5"+
		"\u019c\u00cf\2\u0ad3\u0ad1\3\2\2\2\u0ad4\u0ad7\3\2\2\2\u0ad5\u0ad3\3\2"+
		"\2\2\u0ad5\u0ad6\3\2\2\2\u0ad6\u01a1\3\2\2\2\u0ad7\u0ad5\3\2\2\2\u0ad8"+
		"\u0adb\5\u014c\u00a7\2\u0ad9\u0adb\7\u022e\2\2\u0ada\u0ad8\3\2\2\2\u0ada"+
		"\u0ad9\3\2\2\2\u0adb\u01a3\3\2\2\2\u0adc\u0add\7\u022e\2\2\u0add\u01a5"+
		"\3\2\2\2\u0ade\u0ae3\5\u0152\u00aa\2\u0adf\u0ae0\7\u0217\2\2\u0ae0\u0ae2"+
		"\5\u0152\u00aa\2\u0ae1\u0adf\3\2\2\2\u0ae2\u0ae5\3\2\2\2\u0ae3\u0ae1\3"+
		"\2\2\2\u0ae3\u0ae4\3\2\2\2\u0ae4\u01a7\3\2\2\2\u0ae5\u0ae3\3\2\2\2\u0ae6"+
		"\u0ae7\7\23\2\2\u0ae7\u0aea\5\u01aa\u00d6\2\u0ae8\u0aea\5\u01b6\u00dc"+
		"\2\u0ae9\u0ae6\3\2\2\2\u0ae9\u0ae8\3\2\2\2\u0aea\u01a9\3\2\2\2\u0aeb\u0aec"+
		"\7\u00b8\2\2\u0aec\u0af4\5\u01ac\u00d7\2\u0aed\u0aee\7\u00fc\2\2\u0aee"+
		"\u0aef\7\u00bc\2\2\u0aef\u0af0\7\67\2\2\u0af0\u0af1\7\u00b8\2\2\u0af1"+
		"\u0af4\5\u01ac\u00d7\2\u0af2\u0af4\5\u01b0\u00d9\2\u0af3\u0aeb\3\2\2\2"+
		"\u0af3\u0aed\3\2\2\2\u0af3\u0af2\3\2\2\2\u0af4\u01ab\3\2\2\2\u0af5\u0afc"+
		"\5\u01ae\u00d8\2\u0af6\u0af8\7\u0217\2\2\u0af7\u0af6\3\2\2\2\u0af7\u0af8"+
		"\3\2\2\2\u0af8\u0af9\3\2\2\2\u0af9\u0afb\5\u01ae\u00d8\2\u0afa\u0af7\3"+
		"\2\2\2\u0afb\u0afe\3\2\2\2\u0afc\u0afa\3\2\2\2\u0afc\u0afd\3\2\2\2\u0afd"+
		"\u01ad\3\2\2\2\u0afe\u0afc\3\2\2\2\u0aff\u0b00\7\u00ae\2\2\u0b00\u0b01"+
		"\7\u00af\2\2\u0b01\u0b0a\5\u015e\u00b0\2\u0b02\u0b03\7\u00b3\2\2\u0b03"+
		"\u0b0a\7\u00eb\2\2\u0b04\u0b05\7\u00b3\2\2\u0b05\u0b0a\7\u01ea\2\2\u0b06"+
		"\u0b0a\7\u00c7\2\2\u0b07\u0b08\7A\2\2\u0b08\u0b0a\7\u00c7\2\2\u0b09\u0aff"+
		"\3\2\2\2\u0b09\u0b02\3\2\2\2\u0b09\u0b04\3\2\2\2\u0b09\u0b06\3\2\2\2\u0b09"+
		"\u0b07\3\2\2\2\u0b0a\u01af\3\2\2\2\u0b0b\u0b2c\5\u01b4\u00db\2\u0b0c\u0b0d"+
		"\5\u0154\u00ab\2\u0b0d\u0b0e\7,\2\2\u0b0e\u0b0f\7l\2\2\u0b0f\u0b2c\3\2"+
		"\2\2\u0b10\u0b11\7^\2\2\u0b11\u0b12\7\u010c\2\2\u0b12\u0b2c\5\u015a\u00ae"+
		"\2\u0b13\u0b14\7\u0183\2\2\u0b14\u0b2c\7\u022e\2\2\u0b15\u0b16\7\17\2"+
		"\2\u0b16\u0b2c\7\u022e\2\2\u0b17\u0b19\7}\2\2\u0b18\u0b1a\5\u01b2\u00da"+
		"\2\u0b19\u0b18\3\2\2\2\u0b19\u0b1a\3\2\2\2\u0b1a\u0b2c\3\2\2\2\u0b1b\u0b1c"+
		"\7\u00b5\2\2\u0b1c\u0b2c\5\u014c\u00a7\2\u0b1d\u0b2c\7\u022e\2\2\u0b1e"+
		"\u0b1f\7\u00fc\2\2\u0b1f\u0b20\7\u01f0\2\2\u0b20\u0b2c\5\u014c\u00a7\2"+
		"\u0b21\u0b2c\7\u022e\2\2\u0b22\u0b23\7\u00fc\2\2\u0b23\u0b24\7\u01f0\2"+
		"\2\u0b24\u0b2c\7k\2\2\u0b25\u0b26\7\u012e\2\2\u0b26\u0b27\7\u00b1\2\2"+
		"\u0b27\u0b2c\5\u00f0y\2\u0b28\u0b29\7\u00b8\2\2\u0b29\u0b2a\7\u01c1\2"+
		"\2\u0b2a\u0b2c\7\u022e\2\2\u0b2b\u0b0b\3\2\2\2\u0b2b\u0b0c\3\2\2\2\u0b2b"+
		"\u0b10\3\2\2\2\u0b2b\u0b13\3\2\2\2\u0b2b\u0b15\3\2\2\2\u0b2b\u0b17\3\2"+
		"\2\2\u0b2b\u0b1b\3\2\2\2\u0b2b\u0b1d\3\2\2\2\u0b2b\u0b1e\3\2\2\2\u0b2b"+
		"\u0b21\3\2\2\2\u0b2b\u0b22\3\2\2\2\u0b2b\u0b25\3\2\2\2\u0b2b\u0b28\3\2"+
		"\2\2\u0b2c\u01b1\3\2\2\2\u0b2d\u0b2e\t\22\2\2\u0b2e\u01b3\3\2\2\2\u0b2f"+
		"\u0b30\5\u0154\u00ab\2\u0b30\u0b33\t\23\2\2\u0b31\u0b34\5\u0156\u00ac"+
		"\2\u0b32\u0b34\7k\2\2\u0b33\u0b31\3\2\2\2\u0b33\u0b32\3\2\2\2\u0b34\u01b5"+
		"\3\2\2\2\u0b35\u0b36\7\u00f5\2\2\u0b36\u0b37\5\u01b8\u00dd\2\u0b37\u01b7"+
		"\3\2\2\2\u0b38\u0b41\5\u01ba\u00de\2\u0b39\u0b3a\7^\2\2\u0b3a\u0b41\7"+
		"\u010c\2\2\u0b3b\u0b3c\7\u00b8\2\2\u0b3c\u0b3d\7\u00ae\2\2\u0b3d\u0b41"+
		"\7\u00af\2\2\u0b3e\u0b3f\7\u00fc\2\2\u0b3f\u0b41\7\u01f0\2\2\u0b40\u0b38"+
		"\3\2\2\2\u0b40\u0b39\3\2\2\2\u0b40\u0b3b\3\2\2\2\u0b40\u0b3e\3\2\2\2\u0b41"+
		"\u01b9\3\2\2\2\u0b42\u0b45\5\u0154\u00ab\2\u0b43\u0b45\7H\2\2\u0b44\u0b42"+
		"\3\2\2\2\u0b44\u0b43\3\2\2\2\u0b45\u01bb\3\2\2\2\u0b46\u0b4b\5\u01be\u00e0"+
		"\2\u0b47\u0b48\7\u0217\2\2\u0b48\u0b4a\5\u01be\u00e0\2\u0b49\u0b47\3\2"+
		"\2\2\u0b4a\u0b4d\3\2\2\2\u0b4b\u0b49\3\2\2\2\u0b4b\u0b4c\3\2\2\2\u0b4c"+
		"\u01bd\3\2\2\2\u0b4d\u0b4b\3\2\2\2\u0b4e\u0b5a\5\u0082B\2\u0b4f\u0b50"+
		"\5\u0082B\2\u0b50\u0b51\7\u0202\2\2\u0b51\u0b5a\3\2\2\2\u0b52\u0b53\7"+
		"\u00eb\2\2\u0b53\u0b5a\5\u0082B\2\u0b54\u0b55\7\u00eb\2\2\u0b55\u0b56"+
		"\7\u0211\2\2\u0b56\u0b57\5\u0082B\2\u0b57\u0b58\7\u0212\2\2\u0b58\u0b5a"+
		"\3\2\2\2\u0b59\u0b4e\3\2\2\2\u0b59\u0b4f\3\2\2\2\u0b59\u0b52\3\2\2\2\u0b59"+
		"\u0b54\3\2\2\2\u0b5a\u01bf\3\2\2\2\u0b5b\u0b5c\7\u0182\2\2\u0b5c\u0b5d"+
		"\78\2\2\u0b5d\u0b5e\7B\2\2\u0b5e\u0b7f\7\u0191\2\2\u0b5f\u0b60\7\u01d7"+
		"\2\2\u0b60\u0b61\7B\2\2\u0b61\u0b62\78\2\2\u0b62\u0b63\7B\2\2\u0b63\u0b7f"+
		"\7\u0191\2\2\u0b64\u0b7f\7\u01eb\2\2\u0b65\u0b7f\7\u01a8\2\2\u0b66\u0b7f"+
		"\7\u01dd\2\2\u0b67\u0b7f\7\u01e8\2\2\u0b68\u0b69\7\u00cf\2\2\u0b69\u0b6a"+
		"\7\u00fa\2\2\u0b6a\u0b7f\7s\2\2\u0b6b\u0b6c\7\u00cf\2\2\u0b6c\u0b6d\7"+
		"\u00fa\2\2\u0b6d\u0b7f\7\u01c6\2\2\u0b6e\u0b6f\7\u00fa\2\2\u0b6f\u0b7f"+
		"\7s\2\2\u0b70\u0b71\7\u00fa\2\2\u0b71\u0b7f\7\u01c6\2\2\u0b72\u0b7f\7"+
		"\u0194\2\2\u0b73\u0b74\7A\2\2\u0b74\u0b7f\7\u0194\2\2\u0b75\u0b76\7\u0180"+
		"\2\2\u0b76\u0b7f\5\u015c\u00af\2\u0b77\u0b78\7\u00b6\2\2\u0b78\u0b7f\5"+
		"\u015c\u00af\2\u0b79\u0b7a\7\u01dc\2\2\u0b7a\u0b7f\5\u0104\u0083\2\u0b7b"+
		"\u0b7f\5\u01c2\u00e2\2\u0b7c\u0b7d\7\u0193\2\2\u0b7d\u0b7f\5\u0084C\2"+
		"\u0b7e\u0b5b\3\2\2\2\u0b7e\u0b5f\3\2\2\2\u0b7e\u0b64\3\2\2\2\u0b7e\u0b65"+
		"\3\2\2\2\u0b7e\u0b66\3\2\2\2\u0b7e\u0b67\3\2\2\2\u0b7e\u0b68\3\2\2\2\u0b7e"+
		"\u0b6b\3\2\2\2\u0b7e\u0b6e\3\2\2\2\u0b7e\u0b70\3\2\2\2\u0b7e\u0b72\3\2"+
		"\2\2\u0b7e\u0b73\3\2\2\2\u0b7e\u0b75\3\2\2\2\u0b7e\u0b77\3\2\2\2\u0b7e"+
		"\u0b79\3\2\2\2\u0b7e\u0b7b\3\2\2\2\u0b7e\u0b7c\3\2\2\2\u0b7f\u01c1\3\2"+
		"\2\2\u0b80\u0b81\7\23\2\2\u0b81\u0b84\5\u01b0\u00d9\2\u0b82\u0b84\5\u01b6"+
		"\u00dc\2\u0b83\u0b80\3\2\2\2\u0b83\u0b82\3\2\2\2\u0b84\u01c3\3\2\2\2\u0b85"+
		"\u0b86\t\24\2\2\u0b86\u01c5\3\2\2\2\u0b87\u0b88\t\25\2\2\u0b88\u01c7\3"+
		"\2\2\2\u0b89\u0b8e\5\u00a4S\2\u0b8a\u0b8b\7\u0217\2\2\u0b8b\u0b8d\5\u00a4"+
		"S\2\u0b8c\u0b8a\3\2\2\2\u0b8d\u0b90\3\2\2\2\u0b8e\u0b8c\3\2\2\2\u0b8e"+
		"\u0b8f\3\2\2\2\u0b8f\u01c9\3\2\2\2\u0b90\u0b8e\3\2\2\2\u00ea\u01cc\u01d1"+
		"\u01d5\u01db\u01df\u01e2\u01f3\u01fa\u0203\u020a\u020e\u0216\u021d\u0224"+
		"\u0243\u0259\u0292\u029c\u0318\u031a\u0326\u0352\u0354\u035c\u0376\u037f"+
		"\u0387\u0394\u0398\u039b\u03a4\u03a8\u03ac\u03b3\u03c3\u03cb\u03d9\u03dd"+
		"\u03e2\u03f5\u03fd\u0406\u040b\u0413\u0421\u042d\u043d\u0447\u045b\u0462"+
		"\u046a\u0478\u047f\u048a\u0493\u049e\u04a7\u04b0\u04b9\u04bf\u04ca\u04d2"+
		"\u04d8\u04df\u04e3\u04eb\u04ed\u0504\u050b\u0519\u0522\u052d\u0534\u053d"+
		"\u0541\u0544\u0547\u054a\u055a\u058a\u0627\u0633\u0650\u065f\u0669\u0677"+
		"\u067b\u0680\u0682\u0693\u0697\u069b\u06a3\u06aa\u06ae\u06b5\u06b9\u06bc"+
		"\u06c4\u06c8\u06d3\u06d7\u06dd\u06e2\u06e6\u06e8\u06ee\u06f9\u0702\u0706"+
		"\u070a\u0712\u0732\u073c\u073e\u0746\u0751\u075c\u076c\u077e\u078a\u0792"+
		"\u07a3\u07a9\u07b2\u07b9\u07c5\u07cf\u07dc\u07e2\u07e6\u07ee\u07fe\u0808"+
		"\u081c\u0824\u0836\u083d\u0841\u0844\u0846\u084c\u0855\u085d\u0861\u0864"+
		"\u0867\u086a\u0879\u087d\u0881\u0884\u0887\u088c\u088f\u0891\u089e\u08a5"+
		"\u08b0\u08c2\u08ca\u08d8\u08de\u08e1\u08e4\u08ed\u08f2\u08f5\u08f7\u0933"+
		"\u0939\u0940\u0947\u094e\u0956\u095d\u0963\u096d\u0977\u097f\u0984\u0993"+
		"\u099a\u09a3\u09a8\u09af\u09ba\u09c3\u09cb\u09d2\u09da\u09e3\u09f0\u09f3"+
		"\u09f6\u09f8\u0a01\u0a0c\u0a1f\u0a25\u0a28\u0a35\u0a3b\u0a4e\u0a56\u0a5d"+
		"\u0a67\u0a79\u0a81\u0a8d\u0a9d\u0ab5\u0ac6\u0ace\u0ad5\u0ada\u0ae3\u0ae9"+
		"\u0af3\u0af7\u0afc\u0b09\u0b19\u0b2b\u0b33\u0b40\u0b44\u0b4b\u0b59\u0b7e"+
		"\u0b83\u0b8e";
	public static final String _serializedATN = Utils.join(
		new String[] {
			_serializedATNSegment0,
			_serializedATNSegment1
		},
		""
	);
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}