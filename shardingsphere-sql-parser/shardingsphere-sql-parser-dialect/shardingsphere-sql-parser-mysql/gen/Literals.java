// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/antlr4/imports/mysql/Literals.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Literals extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FILESIZE_LITERAL=1, IDENTIFIER_=2, SINGLE_QUOTED_TEXT=3, DOUBLE_QUOTED_TEXT=4, 
		NCHAR_TEXT=5, UNDERSCORE_CHARSET=6, NUMBER_=7, INT_NUM_=8, FLOAT_NUM_=9, 
		DECIMAL_NUM_=10, HEX_DIGIT_=11, BIT_NUM_=12, NOT_SUPPORT_=13, FOR_GENERATOR=14, 
		AND_=15, OR_=16, NOT_=17, TILDE_=18, VERTICAL_BAR_=19, AMPERSAND_=20, 
		SIGNED_LEFT_SHIFT_=21, SIGNED_RIGHT_SHIFT_=22, CARET_=23, MOD_=24, COLON_=25, 
		PLUS_=26, MINUS_=27, ASTERISK_=28, SLASH_=29, BACKSLASH_=30, DOT_=31, 
		DOT_ASTERISK_=32, SAFE_EQ_=33, DEQ_=34, EQ_=35, NEQ_=36, GT_=37, GTE_=38, 
		LT_=39, LTE_=40, POUND_=41, LP_=42, RP_=43, LBE_=44, RBE_=45, LBT_=46, 
		RBT_=47, COMMA_=48, DQ_=49, SQ_=50, BQ_=51, QUESTION_=52, AT_=53, SEMI_=54, 
		JSON_SEPARATOR=55, JSON_UNQUOTED_SEPARATOR=56;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"FILESIZE_LITERAL", "IDENTIFIER_", "SINGLE_QUOTED_TEXT", "DOUBLE_QUOTED_TEXT", 
			"NCHAR_TEXT", "UNDERSCORE_CHARSET", "NUMBER_", "INT_NUM_", "FLOAT_NUM_", 
			"DECIMAL_NUM_", "HEX_DIGIT_", "BIT_NUM_", "NOT_SUPPORT_", "DIGIT", "HEX_", 
			"FOR_GENERATOR", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", 
			"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", 
			"Z", "UL_", "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", "POUND_", 
			"LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", "SQ_", 
			"BQ_", "QUESTION_", "AT_", "SEMI_", "JSON_SEPARATOR", "JSON_UNQUOTED_SEPARATOR"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, "'not support'", "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'", 
			"'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", "'%'", 
			"':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", "'=='", 
			"'='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", "')'", "'{'", 
			"'}'", "'['", "']'", "','", "'\"'", "'''", "'`'", "'?'", "'@'", "';'", 
			"'->'", "'->>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "FILESIZE_LITERAL", "IDENTIFIER_", "SINGLE_QUOTED_TEXT", "DOUBLE_QUOTED_TEXT", 
			"NCHAR_TEXT", "UNDERSCORE_CHARSET", "NUMBER_", "INT_NUM_", "FLOAT_NUM_", 
			"DECIMAL_NUM_", "HEX_DIGIT_", "BIT_NUM_", "NOT_SUPPORT_", "FOR_GENERATOR", 
			"AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", "SIGNED_LEFT_SHIFT_", 
			"SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", "PLUS_", "MINUS_", 
			"ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", "SAFE_EQ_", 
			"DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", "POUND_", "LP_", 
			"RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", "SQ_", "BQ_", 
			"QUESTION_", "AT_", "SEMI_", "JSON_SEPARATOR", "JSON_UNQUOTED_SEPARATOR"
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


	public Literals(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Literals.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2:\u0208\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\3\2\3\2\3\2\3\3\7\3\u00b2\n\3\f\3\16\3\u00b5\13\3\3\3\6\3"+
		"\u00b8\n\3\r\3\16\3\u00b9\3\3\7\3\u00bd\n\3\f\3\16\3\u00c0\13\3\3\3\3"+
		"\3\6\3\u00c4\n\3\r\3\16\3\u00c5\3\3\3\3\5\3\u00ca\n\3\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\7\4\u00d2\n\4\f\4\16\4\u00d5\13\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\7\5\u00df\n\5\f\5\16\5\u00e2\13\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\6\7"+
		"\u00eb\n\7\r\7\16\7\u00ec\3\b\3\b\3\b\5\b\u00f2\n\b\3\t\6\t\u00f5\n\t"+
		"\r\t\16\t\u00f6\3\n\5\n\u00fa\n\n\3\n\5\n\u00fd\n\n\3\n\3\n\3\n\3\n\5"+
		"\n\u0103\n\n\3\n\3\n\3\13\5\13\u0108\n\13\3\13\3\13\3\13\3\f\3\f\3\f\3"+
		"\f\6\f\u0111\n\f\r\f\16\f\u0112\3\f\3\f\3\f\6\f\u0118\n\f\r\f\16\f\u0119"+
		"\3\f\3\f\5\f\u011e\n\f\3\r\3\r\3\r\3\r\6\r\u0124\n\r\r\r\16\r\u0125\3"+
		"\r\3\r\3\r\6\r\u012b\n\r\r\r\16\r\u012c\3\r\3\r\5\r\u0131\n\r\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\20\3\20"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30"+
		"\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37"+
		"\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)"+
		"\3*\3*\3+\3+\3,\3,\3-\3-\3-\3.\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62\3"+
		"\62\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\66\3\66\3\67\3\67\38\38"+
		"\39\39\3:\3:\3;\3;\3<\3<\3=\3=\3>\3>\3>\3?\3?\3?\3?\3@\3@\3@\3A\3A\3B"+
		"\3B\3B\3B\5B\u01da\nB\3C\3C\3D\3D\3D\3E\3E\3F\3F\3F\3G\3G\3H\3H\3I\3I"+
		"\3J\3J\3K\3K\3L\3L\3M\3M\3N\3N\3O\3O\3P\3P\3Q\3Q\3R\3R\3S\3S\3T\3T\3U"+
		"\3U\3U\3V\3V\3V\3V\4\u00b3\u00b9\2W\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n"+
		"\23\13\25\f\27\r\31\16\33\17\35\2\37\2!\20#\2%\2\'\2)\2+\2-\2/\2\61\2"+
		"\63\2\65\2\67\29\2;\2=\2?\2A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\21[\22"+
		"]\23_\24a\25c\26e\27g\30i\31k\32m\33o\34q\35s\36u\37w y!{\"}#\177$\u0081"+
		"%\u0083&\u0085\'\u0087(\u0089)\u008b*\u008d+\u008f,\u0091-\u0093.\u0095"+
		"/\u0097\60\u0099\61\u009b\62\u009d\63\u009f\64\u00a1\65\u00a3\66\u00a5"+
		"\67\u00a78\u00a99\u00ab:\3\2%\6\2IIMMOOVV\7\2&&\62;C\\aac|\6\2&&C\\aa"+
		"c|\3\2bb\4\2))^^\4\2$$^^\4\2\62;c|\3\2\62;\5\2\62;CHch\4\2CCcc\4\2DDd"+
		"d\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2"+
		"MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4"+
		"\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u0205\2\3\3"+
		"\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2"+
		"\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3"+
		"\2\2\2\2\33\3\2\2\2\2!\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2"+
		"\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2"+
		"\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y"+
		"\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3"+
		"\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2"+
		"\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095"+
		"\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2\2\2\u009b\3\2\2\2\2\u009d\3\2\2"+
		"\2\2\u009f\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3\3\2\2\2\2\u00a5\3\2\2\2\2\u00a7"+
		"\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2\2\3\u00ad\3\2\2\2\5\u00c9\3\2\2"+
		"\2\7\u00cb\3\2\2\2\t\u00d8\3\2\2\2\13\u00e5\3\2\2\2\r\u00e8\3\2\2\2\17"+
		"\u00f1\3\2\2\2\21\u00f4\3\2\2\2\23\u00f9\3\2\2\2\25\u0107\3\2\2\2\27\u011d"+
		"\3\2\2\2\31\u0130\3\2\2\2\33\u0132\3\2\2\2\35\u013e\3\2\2\2\37\u0140\3"+
		"\2\2\2!\u0142\3\2\2\2#\u016d\3\2\2\2%\u016f\3\2\2\2\'\u0171\3\2\2\2)\u0173"+
		"\3\2\2\2+\u0175\3\2\2\2-\u0177\3\2\2\2/\u0179\3\2\2\2\61\u017b\3\2\2\2"+
		"\63\u017d\3\2\2\2\65\u017f\3\2\2\2\67\u0181\3\2\2\29\u0183\3\2\2\2;\u0185"+
		"\3\2\2\2=\u0187\3\2\2\2?\u0189\3\2\2\2A\u018b\3\2\2\2C\u018d\3\2\2\2E"+
		"\u018f\3\2\2\2G\u0191\3\2\2\2I\u0193\3\2\2\2K\u0195\3\2\2\2M\u0197\3\2"+
		"\2\2O\u0199\3\2\2\2Q\u019b\3\2\2\2S\u019d\3\2\2\2U\u019f\3\2\2\2W\u01a1"+
		"\3\2\2\2Y\u01a3\3\2\2\2[\u01a6\3\2\2\2]\u01a9\3\2\2\2_\u01ab\3\2\2\2a"+
		"\u01ad\3\2\2\2c\u01af\3\2\2\2e\u01b1\3\2\2\2g\u01b4\3\2\2\2i\u01b7\3\2"+
		"\2\2k\u01b9\3\2\2\2m\u01bb\3\2\2\2o\u01bd\3\2\2\2q\u01bf\3\2\2\2s\u01c1"+
		"\3\2\2\2u\u01c3\3\2\2\2w\u01c5\3\2\2\2y\u01c7\3\2\2\2{\u01c9\3\2\2\2}"+
		"\u01cc\3\2\2\2\177\u01d0\3\2\2\2\u0081\u01d3\3\2\2\2\u0083\u01d9\3\2\2"+
		"\2\u0085\u01db\3\2\2\2\u0087\u01dd\3\2\2\2\u0089\u01e0\3\2\2\2\u008b\u01e2"+
		"\3\2\2\2\u008d\u01e5\3\2\2\2\u008f\u01e7\3\2\2\2\u0091\u01e9\3\2\2\2\u0093"+
		"\u01eb\3\2\2\2\u0095\u01ed\3\2\2\2\u0097\u01ef\3\2\2\2\u0099\u01f1\3\2"+
		"\2\2\u009b\u01f3\3\2\2\2\u009d\u01f5\3\2\2\2\u009f\u01f7\3\2\2\2\u00a1"+
		"\u01f9\3\2\2\2\u00a3\u01fb\3\2\2\2\u00a5\u01fd\3\2\2\2\u00a7\u01ff\3\2"+
		"\2\2\u00a9\u0201\3\2\2\2\u00ab\u0204\3\2\2\2\u00ad\u00ae\5\21\t\2\u00ae"+
		"\u00af\t\2\2\2\u00af\4\3\2\2\2\u00b0\u00b2\t\3\2\2\u00b1\u00b0\3\2\2\2"+
		"\u00b2\u00b5\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b7"+
		"\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b6\u00b8\t\4\2\2\u00b7\u00b6\3\2\2\2\u00b8"+
		"\u00b9\3\2\2\2\u00b9\u00ba\3\2\2\2\u00b9\u00b7\3\2\2\2\u00ba\u00be\3\2"+
		"\2\2\u00bb\u00bd\t\3\2\2\u00bc\u00bb\3\2\2\2\u00bd\u00c0\3\2\2\2\u00be"+
		"\u00bc\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\u00ca\3\2\2\2\u00c0\u00be\3\2"+
		"\2\2\u00c1\u00c3\5\u00a1Q\2\u00c2\u00c4\n\5\2\2\u00c3\u00c2\3\2\2\2\u00c4"+
		"\u00c5\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c7\3\2"+
		"\2\2\u00c7\u00c8\5\u00a1Q\2\u00c8\u00ca\3\2\2\2\u00c9\u00b3\3\2\2\2\u00c9"+
		"\u00c1\3\2\2\2\u00ca\6\3\2\2\2\u00cb\u00d3\5\u009fP\2\u00cc\u00cd\7^\2"+
		"\2\u00cd\u00d2\13\2\2\2\u00ce\u00cf\7)\2\2\u00cf\u00d2\7)\2\2\u00d0\u00d2"+
		"\n\6\2\2\u00d1\u00cc\3\2\2\2\u00d1\u00ce\3\2\2\2\u00d1\u00d0\3\2\2\2\u00d2"+
		"\u00d5\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d6\3\2"+
		"\2\2\u00d5\u00d3\3\2\2\2\u00d6\u00d7\5\u009fP\2\u00d7\b\3\2\2\2\u00d8"+
		"\u00e0\5\u009dO\2\u00d9\u00da\7^\2\2\u00da\u00df\13\2\2\2\u00db\u00dc"+
		"\7$\2\2\u00dc\u00df\7$\2\2\u00dd\u00df\n\7\2\2\u00de\u00d9\3\2\2\2\u00de"+
		"\u00db\3\2\2\2\u00de\u00dd\3\2\2\2\u00df\u00e2\3\2\2\2\u00e0\u00de\3\2"+
		"\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e3\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e3"+
		"\u00e4\5\u009dO\2\u00e4\n\3\2\2\2\u00e5\u00e6\5=\37\2\u00e6\u00e7\5\7"+
		"\4\2\u00e7\f\3\2\2\2\u00e8\u00ea\5W,\2\u00e9\u00eb\t\b\2\2\u00ea\u00e9"+
		"\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed"+
		"\16\3\2\2\2\u00ee\u00f2\5\21\t\2\u00ef\u00f2\5\23\n\2\u00f0\u00f2\5\25"+
		"\13\2\u00f1\u00ee\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f1\u00f0\3\2\2\2\u00f2"+
		"\20\3\2\2\2\u00f3\u00f5\5\35\17\2\u00f4\u00f3\3\2\2\2\u00f5\u00f6\3\2"+
		"\2\2\u00f6\u00f4\3\2\2\2\u00f6\u00f7\3\2\2\2\u00f7\22\3\2\2\2\u00f8\u00fa"+
		"\5\21\t\2\u00f9\u00f8\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fc\3\2\2\2"+
		"\u00fb\u00fd\5y=\2\u00fc\u00fb\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fe"+
		"\3\2\2\2\u00fe\u00ff\5\21\t\2\u00ff\u0102\5+\26\2\u0100\u0103\5o8\2\u0101"+
		"\u0103\5q9\2\u0102\u0100\3\2\2\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2"+
		"\2\u0103\u0104\3\2\2\2\u0104\u0105\5\21\t\2\u0105\24\3\2\2\2\u0106\u0108"+
		"\5\21\t\2\u0107\u0106\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u0109\3\2\2\2"+
		"\u0109\u010a\5y=\2\u010a\u010b\5\21\t\2\u010b\26\3\2\2\2\u010c\u010d\7"+
		"\62\2\2\u010d\u010e\7z\2\2\u010e\u0110\3\2\2\2\u010f\u0111\5\37\20\2\u0110"+
		"\u010f\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0110\3\2\2\2\u0112\u0113\3\2"+
		"\2\2\u0113\u011e\3\2\2\2\u0114\u0115\5Q)\2\u0115\u0117\5\u009fP\2\u0116"+
		"\u0118\5\37\20\2\u0117\u0116\3\2\2\2\u0118\u0119\3\2\2\2\u0119\u0117\3"+
		"\2\2\2\u0119\u011a\3\2\2\2\u011a\u011b\3\2\2\2\u011b\u011c\5\u009fP\2"+
		"\u011c\u011e\3\2\2\2\u011d\u010c\3\2\2\2\u011d\u0114\3\2\2\2\u011e\30"+
		"\3\2\2\2\u011f\u0120\7\62\2\2\u0120\u0121\7d\2\2\u0121\u0123\3\2\2\2\u0122"+
		"\u0124\4\62\63\2\u0123\u0122\3\2\2\2\u0124\u0125\3\2\2\2\u0125\u0123\3"+
		"\2\2\2\u0125\u0126\3\2\2\2\u0126\u0131\3\2\2\2\u0127\u0128\5%\23\2\u0128"+
		"\u012a\5\u009fP\2\u0129\u012b\4\62\63\2\u012a\u0129\3\2\2\2\u012b\u012c"+
		"\3\2\2\2\u012c\u012a\3\2\2\2\u012c\u012d\3\2\2\2\u012d\u012e\3\2\2\2\u012e"+
		"\u012f\5\u009fP\2\u012f\u0131\3\2\2\2\u0130\u011f\3\2\2\2\u0130\u0127"+
		"\3\2\2\2\u0131\32\3\2\2\2\u0132\u0133\7p\2\2\u0133\u0134\7q\2\2\u0134"+
		"\u0135\7v\2\2\u0135\u0136\7\"\2\2\u0136\u0137\7u\2\2\u0137\u0138\7w\2"+
		"\2\u0138\u0139\7r\2\2\u0139\u013a\7r\2\2\u013a\u013b\7q\2\2\u013b\u013c"+
		"\7t\2\2\u013c\u013d\7v\2\2\u013d\34\3\2\2\2\u013e\u013f\t\t\2\2\u013f"+
		"\36\3\2\2\2\u0140\u0141\t\n\2\2\u0141 \3\2\2\2\u0142\u0143\7F\2\2\u0143"+
		"\u0144\7Q\2\2\u0144\u0145\7\"\2\2\u0145\u0146\7P\2\2\u0146\u0147\7Q\2"+
		"\2\u0147\u0148\7V\2\2\u0148\u0149\7\"\2\2\u0149\u014a\7O\2\2\u014a\u014b"+
		"\7C\2\2\u014b\u014c\7V\2\2\u014c\u014d\7E\2\2\u014d\u014e\7J\2\2\u014e"+
		"\u014f\7\"\2\2\u014f\u0150\7C\2\2\u0150\u0151\7P\2\2\u0151\u0152\7[\2"+
		"\2\u0152\u0153\7\"\2\2\u0153\u0154\7V\2\2\u0154\u0155\7J\2\2\u0155\u0156"+
		"\7K\2\2\u0156\u0157\7P\2\2\u0157\u0158\7I\2\2\u0158\u0159\7.\2\2\u0159"+
		"\u015a\7\"\2\2\u015a\u015b\7L\2\2\u015b\u015c\7W\2\2\u015c\u015d\7U\2"+
		"\2\u015d\u015e\7V\2\2\u015e\u015f\7\"\2\2\u015f\u0160\7H\2\2\u0160\u0161"+
		"\7Q\2\2\u0161\u0162\7T\2\2\u0162\u0163\7\"\2\2\u0163\u0164\7I\2\2\u0164"+
		"\u0165\7G\2\2\u0165\u0166\7P\2\2\u0166\u0167\7G\2\2\u0167\u0168\7T\2\2"+
		"\u0168\u0169\7C\2\2\u0169\u016a\7V\2\2\u016a\u016b\7Q\2\2\u016b\u016c"+
		"\7T\2\2\u016c\"\3\2\2\2\u016d\u016e\t\13\2\2\u016e$\3\2\2\2\u016f\u0170"+
		"\t\f\2\2\u0170&\3\2\2\2\u0171\u0172\t\r\2\2\u0172(\3\2\2\2\u0173\u0174"+
		"\t\16\2\2\u0174*\3\2\2\2\u0175\u0176\t\17\2\2\u0176,\3\2\2\2\u0177\u0178"+
		"\t\20\2\2\u0178.\3\2\2\2\u0179\u017a\t\21\2\2\u017a\60\3\2\2\2\u017b\u017c"+
		"\t\22\2\2\u017c\62\3\2\2\2\u017d\u017e\t\23\2\2\u017e\64\3\2\2\2\u017f"+
		"\u0180\t\24\2\2\u0180\66\3\2\2\2\u0181\u0182\t\25\2\2\u01828\3\2\2\2\u0183"+
		"\u0184\t\26\2\2\u0184:\3\2\2\2\u0185\u0186\t\27\2\2\u0186<\3\2\2\2\u0187"+
		"\u0188\t\30\2\2\u0188>\3\2\2\2\u0189\u018a\t\31\2\2\u018a@\3\2\2\2\u018b"+
		"\u018c\t\32\2\2\u018cB\3\2\2\2\u018d\u018e\t\33\2\2\u018eD\3\2\2\2\u018f"+
		"\u0190\t\34\2\2\u0190F\3\2\2\2\u0191\u0192\t\35\2\2\u0192H\3\2\2\2\u0193"+
		"\u0194\t\36\2\2\u0194J\3\2\2\2\u0195\u0196\t\37\2\2\u0196L\3\2\2\2\u0197"+
		"\u0198\t \2\2\u0198N\3\2\2\2\u0199\u019a\t!\2\2\u019aP\3\2\2\2\u019b\u019c"+
		"\t\"\2\2\u019cR\3\2\2\2\u019d\u019e\t#\2\2\u019eT\3\2\2\2\u019f\u01a0"+
		"\t$\2\2\u01a0V\3\2\2\2\u01a1\u01a2\7a\2\2\u01a2X\3\2\2\2\u01a3\u01a4\7"+
		"(\2\2\u01a4\u01a5\7(\2\2\u01a5Z\3\2\2\2\u01a6\u01a7\7~\2\2\u01a7\u01a8"+
		"\7~\2\2\u01a8\\\3\2\2\2\u01a9\u01aa\7#\2\2\u01aa^\3\2\2\2\u01ab\u01ac"+
		"\7\u0080\2\2\u01ac`\3\2\2\2\u01ad\u01ae\7~\2\2\u01aeb\3\2\2\2\u01af\u01b0"+
		"\7(\2\2\u01b0d\3\2\2\2\u01b1\u01b2\7>\2\2\u01b2\u01b3\7>\2\2\u01b3f\3"+
		"\2\2\2\u01b4\u01b5\7@\2\2\u01b5\u01b6\7@\2\2\u01b6h\3\2\2\2\u01b7\u01b8"+
		"\7`\2\2\u01b8j\3\2\2\2\u01b9\u01ba\7\'\2\2\u01bal\3\2\2\2\u01bb\u01bc"+
		"\7<\2\2\u01bcn\3\2\2\2\u01bd\u01be\7-\2\2\u01bep\3\2\2\2\u01bf\u01c0\7"+
		"/\2\2\u01c0r\3\2\2\2\u01c1\u01c2\7,\2\2\u01c2t\3\2\2\2\u01c3\u01c4\7\61"+
		"\2\2\u01c4v\3\2\2\2\u01c5\u01c6\7^\2\2\u01c6x\3\2\2\2\u01c7\u01c8\7\60"+
		"\2\2\u01c8z\3\2\2\2\u01c9\u01ca\7\60\2\2\u01ca\u01cb\7,\2\2\u01cb|\3\2"+
		"\2\2\u01cc\u01cd\7>\2\2\u01cd\u01ce\7?\2\2\u01ce\u01cf\7@\2\2\u01cf~\3"+
		"\2\2\2\u01d0\u01d1\7?\2\2\u01d1\u01d2\7?\2\2\u01d2\u0080\3\2\2\2\u01d3"+
		"\u01d4\7?\2\2\u01d4\u0082\3\2\2\2\u01d5\u01d6\7>\2\2\u01d6\u01da\7@\2"+
		"\2\u01d7\u01d8\7#\2\2\u01d8\u01da\7?\2\2\u01d9\u01d5\3\2\2\2\u01d9\u01d7"+
		"\3\2\2\2\u01da\u0084\3\2\2\2\u01db\u01dc\7@\2\2\u01dc\u0086\3\2\2\2\u01dd"+
		"\u01de\7@\2\2\u01de\u01df\7?\2\2\u01df\u0088\3\2\2\2\u01e0\u01e1\7>\2"+
		"\2\u01e1\u008a\3\2\2\2\u01e2\u01e3\7>\2\2\u01e3\u01e4\7?\2\2\u01e4\u008c"+
		"\3\2\2\2\u01e5\u01e6\7%\2\2\u01e6\u008e\3\2\2\2\u01e7\u01e8\7*\2\2\u01e8"+
		"\u0090\3\2\2\2\u01e9\u01ea\7+\2\2\u01ea\u0092\3\2\2\2\u01eb\u01ec\7}\2"+
		"\2\u01ec\u0094\3\2\2\2\u01ed\u01ee\7\177\2\2\u01ee\u0096\3\2\2\2\u01ef"+
		"\u01f0\7]\2\2\u01f0\u0098\3\2\2\2\u01f1\u01f2\7_\2\2\u01f2\u009a\3\2\2"+
		"\2\u01f3\u01f4\7.\2\2\u01f4\u009c\3\2\2\2\u01f5\u01f6\7$\2\2\u01f6\u009e"+
		"\3\2\2\2\u01f7\u01f8\7)\2\2\u01f8\u00a0\3\2\2\2\u01f9\u01fa\7b\2\2\u01fa"+
		"\u00a2\3\2\2\2\u01fb\u01fc\7A\2\2\u01fc\u00a4\3\2\2\2\u01fd\u01fe\7B\2"+
		"\2\u01fe\u00a6\3\2\2\2\u01ff\u0200\7=\2\2\u0200\u00a8\3\2\2\2\u0201\u0202"+
		"\7/\2\2\u0202\u0203\7@\2\2\u0203\u00aa\3\2\2\2\u0204\u0205\7/\2\2\u0205"+
		"\u0206\7@\2\2\u0206\u0207\7@\2\2\u0207\u00ac\3\2\2\2\32\2\u00b3\u00b9"+
		"\u00be\u00c5\u00c9\u00d1\u00d3\u00de\u00e0\u00ec\u00f1\u00f6\u00f9\u00fc"+
		"\u0102\u0107\u0112\u0119\u011d\u0125\u012c\u0130\u01d9\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}