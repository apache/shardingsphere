// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/Symbol.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Symbol extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND_=1, OR_=2, NOT_=3, TILDE_=4, VERTICAL_BAR_=5, AMPERSAND_=6, SIGNED_LEFT_SHIFT_=7, 
		SIGNED_RIGHT_SHIFT_=8, CARET_=9, MOD_=10, COLON_=11, PLUS_=12, MINUS_=13, 
		ASTERISK_=14, SLASH_=15, BACKSLASH_=16, DOT_=17, DOT_ASTERISK_=18, SAFE_EQ_=19, 
		DEQ_=20, EQ_=21, CQ_=22, NEQ_=23, GT_=24, GTE_=25, LT_=26, LTE_=27, POUND_=28, 
		LP_=29, RP_=30, LBE_=31, RBE_=32, LBT_=33, RBT_=34, COMMA_=35, DQ_=36, 
		SQ_=37, BQ_=38, QUESTION_=39, AT_=40, SEMI_=41, TILDE_TILDE_=42, NOT_TILDE_TILDE_=43, 
		TYPE_CAST_=44, ILIKE_=45, NOT_ILIKE_=46, JSON_EXTRACT_=47, JSON_EXTRACT_TEXT_=48, 
		JSON_PATH_EXTRACT_=49, JSON_PATH_EXTRACT_TEXT_=50, JSONB_CONTAIN_RIGHT_=51, 
		JSONB_CONTAIN_LEFT_=52, JSONB_CONTAIN_ALL_TOP_KEY_=53, JSONB_PATH_DELETE_=54, 
		JSONB_PATH_CONTAIN_ANY_VALUE_=55, JSONB_PATH_PREDICATE_CHECK_=56;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", "SIGNED_LEFT_SHIFT_", 
			"SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", "PLUS_", "MINUS_", 
			"ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", "SAFE_EQ_", 
			"DEQ_", "EQ_", "CQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", "POUND_", 
			"LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", "SQ_", 
			"BQ_", "QUESTION_", "AT_", "SEMI_", "TILDE_TILDE_", "NOT_TILDE_TILDE_", 
			"TYPE_CAST_", "ILIKE_", "NOT_ILIKE_", "JSON_EXTRACT_", "JSON_EXTRACT_TEXT_", 
			"JSON_PATH_EXTRACT_", "JSON_PATH_EXTRACT_TEXT_", "JSONB_CONTAIN_RIGHT_", 
			"JSONB_CONTAIN_LEFT_", "JSONB_CONTAIN_ALL_TOP_KEY_", "JSONB_PATH_DELETE_", 
			"JSONB_PATH_CONTAIN_ANY_VALUE_", "JSONB_PATH_PREDICATE_CHECK_"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", 
			"'%'", "':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", 
			"'=='", "'='", "':='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", 
			"')'", "'{'", "'}'", "'['", "']'", "','", "'\"'", "'''", "'`'", "'?'", 
			"'@'", "';'", "'~~'", "'!~~'", "'::'", "'~~*'", "'!~~*'", "'->'", "'->>'", 
			"'#>'", "'#>>'", "'@>'", "'<@'", "'?&'", "'#-'", "'@?'", "'@@'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "CQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", 
			"POUND_", "LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", 
			"SQ_", "BQ_", "QUESTION_", "AT_", "SEMI_", "TILDE_TILDE_", "NOT_TILDE_TILDE_", 
			"TYPE_CAST_", "ILIKE_", "NOT_ILIKE_", "JSON_EXTRACT_", "JSON_EXTRACT_TEXT_", 
			"JSON_PATH_EXTRACT_", "JSON_PATH_EXTRACT_TEXT_", "JSONB_CONTAIN_RIGHT_", 
			"JSONB_CONTAIN_LEFT_", "JSONB_CONTAIN_ALL_TOP_KEY_", "JSONB_PATH_DELETE_", 
			"JSONB_PATH_CONTAIN_ANY_VALUE_", "JSONB_PATH_PREDICATE_CHECK_"
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


	public Symbol(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Symbol.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2:\u0107\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\3\2\3\2\3\2\3\3\3\3\3\3"+
		"\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13"+
		"\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\27\3\27"+
		"\3\27\3\30\3\30\3\30\3\30\5\30\u00ad\n\30\3\31\3\31\3\32\3\32\3\32\3\33"+
		"\3\33\3\34\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\""+
		"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3+\3,\3,\3,\3"+
		",\3-\3-\3-\3.\3.\3.\3.\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\61\3\61\3\61\3"+
		"\61\3\62\3\62\3\62\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\65\3"+
		"\66\3\66\3\66\3\67\3\67\3\67\38\38\38\39\39\39\2\2:\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'"+
		"\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&K\'"+
		"M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64g\65i\66k\67m8o9q:\3\2\2\2\u0107\2"+
		"\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2"+
		"\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2"+
		"\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2"+
		"\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2"+
		"\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2"+
		"\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U"+
		"\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2"+
		"\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2"+
		"\2o\3\2\2\2\2q\3\2\2\2\3s\3\2\2\2\5v\3\2\2\2\7y\3\2\2\2\t{\3\2\2\2\13"+
		"}\3\2\2\2\r\177\3\2\2\2\17\u0081\3\2\2\2\21\u0084\3\2\2\2\23\u0087\3\2"+
		"\2\2\25\u0089\3\2\2\2\27\u008b\3\2\2\2\31\u008d\3\2\2\2\33\u008f\3\2\2"+
		"\2\35\u0091\3\2\2\2\37\u0093\3\2\2\2!\u0095\3\2\2\2#\u0097\3\2\2\2%\u0099"+
		"\3\2\2\2\'\u009c\3\2\2\2)\u00a0\3\2\2\2+\u00a3\3\2\2\2-\u00a5\3\2\2\2"+
		"/\u00ac\3\2\2\2\61\u00ae\3\2\2\2\63\u00b0\3\2\2\2\65\u00b3\3\2\2\2\67"+
		"\u00b5\3\2\2\29\u00b8\3\2\2\2;\u00ba\3\2\2\2=\u00bc\3\2\2\2?\u00be\3\2"+
		"\2\2A\u00c0\3\2\2\2C\u00c2\3\2\2\2E\u00c4\3\2\2\2G\u00c6\3\2\2\2I\u00c8"+
		"\3\2\2\2K\u00ca\3\2\2\2M\u00cc\3\2\2\2O\u00ce\3\2\2\2Q\u00d0\3\2\2\2S"+
		"\u00d2\3\2\2\2U\u00d4\3\2\2\2W\u00d7\3\2\2\2Y\u00db\3\2\2\2[\u00de\3\2"+
		"\2\2]\u00e2\3\2\2\2_\u00e7\3\2\2\2a\u00ea\3\2\2\2c\u00ee\3\2\2\2e\u00f1"+
		"\3\2\2\2g\u00f5\3\2\2\2i\u00f8\3\2\2\2k\u00fb\3\2\2\2m\u00fe\3\2\2\2o"+
		"\u0101\3\2\2\2q\u0104\3\2\2\2st\7(\2\2tu\7(\2\2u\4\3\2\2\2vw\7~\2\2wx"+
		"\7~\2\2x\6\3\2\2\2yz\7#\2\2z\b\3\2\2\2{|\7\u0080\2\2|\n\3\2\2\2}~\7~\2"+
		"\2~\f\3\2\2\2\177\u0080\7(\2\2\u0080\16\3\2\2\2\u0081\u0082\7>\2\2\u0082"+
		"\u0083\7>\2\2\u0083\20\3\2\2\2\u0084\u0085\7@\2\2\u0085\u0086\7@\2\2\u0086"+
		"\22\3\2\2\2\u0087\u0088\7`\2\2\u0088\24\3\2\2\2\u0089\u008a\7\'\2\2\u008a"+
		"\26\3\2\2\2\u008b\u008c\7<\2\2\u008c\30\3\2\2\2\u008d\u008e\7-\2\2\u008e"+
		"\32\3\2\2\2\u008f\u0090\7/\2\2\u0090\34\3\2\2\2\u0091\u0092\7,\2\2\u0092"+
		"\36\3\2\2\2\u0093\u0094\7\61\2\2\u0094 \3\2\2\2\u0095\u0096\7^\2\2\u0096"+
		"\"\3\2\2\2\u0097\u0098\7\60\2\2\u0098$\3\2\2\2\u0099\u009a\7\60\2\2\u009a"+
		"\u009b\7,\2\2\u009b&\3\2\2\2\u009c\u009d\7>\2\2\u009d\u009e\7?\2\2\u009e"+
		"\u009f\7@\2\2\u009f(\3\2\2\2\u00a0\u00a1\7?\2\2\u00a1\u00a2\7?\2\2\u00a2"+
		"*\3\2\2\2\u00a3\u00a4\7?\2\2\u00a4,\3\2\2\2\u00a5\u00a6\7<\2\2\u00a6\u00a7"+
		"\7?\2\2\u00a7.\3\2\2\2\u00a8\u00a9\7>\2\2\u00a9\u00ad\7@\2\2\u00aa\u00ab"+
		"\7#\2\2\u00ab\u00ad\7?\2\2\u00ac\u00a8\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ad"+
		"\60\3\2\2\2\u00ae\u00af\7@\2\2\u00af\62\3\2\2\2\u00b0\u00b1\7@\2\2\u00b1"+
		"\u00b2\7?\2\2\u00b2\64\3\2\2\2\u00b3\u00b4\7>\2\2\u00b4\66\3\2\2\2\u00b5"+
		"\u00b6\7>\2\2\u00b6\u00b7\7?\2\2\u00b78\3\2\2\2\u00b8\u00b9\7%\2\2\u00b9"+
		":\3\2\2\2\u00ba\u00bb\7*\2\2\u00bb<\3\2\2\2\u00bc\u00bd\7+\2\2\u00bd>"+
		"\3\2\2\2\u00be\u00bf\7}\2\2\u00bf@\3\2\2\2\u00c0\u00c1\7\177\2\2\u00c1"+
		"B\3\2\2\2\u00c2\u00c3\7]\2\2\u00c3D\3\2\2\2\u00c4\u00c5\7_\2\2\u00c5F"+
		"\3\2\2\2\u00c6\u00c7\7.\2\2\u00c7H\3\2\2\2\u00c8\u00c9\7$\2\2\u00c9J\3"+
		"\2\2\2\u00ca\u00cb\7)\2\2\u00cbL\3\2\2\2\u00cc\u00cd\7b\2\2\u00cdN\3\2"+
		"\2\2\u00ce\u00cf\7A\2\2\u00cfP\3\2\2\2\u00d0\u00d1\7B\2\2\u00d1R\3\2\2"+
		"\2\u00d2\u00d3\7=\2\2\u00d3T\3\2\2\2\u00d4\u00d5\7\u0080\2\2\u00d5\u00d6"+
		"\7\u0080\2\2\u00d6V\3\2\2\2\u00d7\u00d8\7#\2\2\u00d8\u00d9\7\u0080\2\2"+
		"\u00d9\u00da\7\u0080\2\2\u00daX\3\2\2\2\u00db\u00dc\7<\2\2\u00dc\u00dd"+
		"\7<\2\2\u00ddZ\3\2\2\2\u00de\u00df\7\u0080\2\2\u00df\u00e0\7\u0080\2\2"+
		"\u00e0\u00e1\7,\2\2\u00e1\\\3\2\2\2\u00e2\u00e3\7#\2\2\u00e3\u00e4\7\u0080"+
		"\2\2\u00e4\u00e5\7\u0080\2\2\u00e5\u00e6\7,\2\2\u00e6^\3\2\2\2\u00e7\u00e8"+
		"\7/\2\2\u00e8\u00e9\7@\2\2\u00e9`\3\2\2\2\u00ea\u00eb\7/\2\2\u00eb\u00ec"+
		"\7@\2\2\u00ec\u00ed\7@\2\2\u00edb\3\2\2\2\u00ee\u00ef\7%\2\2\u00ef\u00f0"+
		"\7@\2\2\u00f0d\3\2\2\2\u00f1\u00f2\7%\2\2\u00f2\u00f3\7@\2\2\u00f3\u00f4"+
		"\7@\2\2\u00f4f\3\2\2\2\u00f5\u00f6\7B\2\2\u00f6\u00f7\7@\2\2\u00f7h\3"+
		"\2\2\2\u00f8\u00f9\7>\2\2\u00f9\u00fa\7B\2\2\u00faj\3\2\2\2\u00fb\u00fc"+
		"\7A\2\2\u00fc\u00fd\7(\2\2\u00fdl\3\2\2\2\u00fe\u00ff\7%\2\2\u00ff\u0100"+
		"\7/\2\2\u0100n\3\2\2\2\u0101\u0102\7B\2\2\u0102\u0103\7A\2\2\u0103p\3"+
		"\2\2\2\u0104\u0105\7B\2\2\u0105\u0106\7B\2\2\u0106r\3\2\2\2\4\2\u00ac"+
		"\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}