// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/antlr4/imports/sql92/Literals.g4 by ANTLR 4.9.1
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
		IDENTIFIER_=1, STRING_=2, NUMBER_=3, HEX_DIGIT_=4, BIT_NUM_=5, FOR_GENERATOR=6, 
		AND_=7, CONCAT_=8, NOT_=9, TILDE_=10, VERTICAL_BAR_=11, AMPERSAND_=12, 
		SIGNED_LEFT_SHIFT_=13, SIGNED_RIGHT_SHIFT_=14, CARET_=15, MOD_=16, COLON_=17, 
		PLUS_=18, MINUS_=19, ASTERISK_=20, SLASH_=21, BACKSLASH_=22, DOT_=23, 
		DOT_ASTERISK_=24, SAFE_EQ_=25, DEQ_=26, EQ_=27, NEQ_=28, GT_=29, GTE_=30, 
		LT_=31, LTE_=32, POUND_=33, LP_=34, RP_=35, LBE_=36, RBE_=37, LBT_=38, 
		RBT_=39, COMMA_=40, DQ_=41, SQ_=42, QUESTION_=43, AT_=44, SEMI_=45;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"IDENTIFIER_", "STRING_", "NUMBER_", "HEX_DIGIT_", "BIT_NUM_", "INT_", 
			"HEX_", "FOR_GENERATOR", "A", "B", "C", "D", "E", "F", "G", "H", "I", 
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", 
			"X", "Y", "Z", "UL_", "AND_", "CONCAT_", "NOT_", "TILDE_", "VERTICAL_BAR_", 
			"AMPERSAND_", "SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", 
			"MOD_", "COLON_", "PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", 
			"DOT_", "DOT_ASTERISK_", "SAFE_EQ_", "DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", 
			"LT_", "LTE_", "POUND_", "LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", 
			"COMMA_", "DQ_", "SQ_", "QUESTION_", "AT_", "SEMI_"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'", 
			"'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", "'%'", 
			"':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", "'=='", 
			"'='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", "')'", "'{'", 
			"'}'", "'['", "']'", "','", "'\"'", "'''", "'?'", "'@'", "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "IDENTIFIER_", "STRING_", "NUMBER_", "HEX_DIGIT_", "BIT_NUM_", 
			"FOR_GENERATOR", "AND_", "CONCAT_", "NOT_", "TILDE_", "VERTICAL_BAR_", 
			"AMPERSAND_", "SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", 
			"MOD_", "COLON_", "PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", 
			"DOT_", "DOT_ASTERISK_", "SAFE_EQ_", "DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", 
			"LT_", "LTE_", "POUND_", "LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", 
			"COMMA_", "DQ_", "SQ_", "QUESTION_", "AT_", "SEMI_"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2/\u01b9\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\3\2\7\2\u0099\n\2\f\2\16\2\u009c\13\2\3\2\6\2\u009f\n"+
		"\2\r\2\16\2\u00a0\3\2\7\2\u00a4\n\2\f\2\16\2\u00a7\13\2\3\2\3\2\6\2\u00ab"+
		"\n\2\r\2\16\2\u00ac\3\2\3\2\5\2\u00b1\n\2\3\3\3\3\3\3\3\3\3\3\3\3\7\3"+
		"\u00b9\n\3\f\3\16\3\u00bc\13\3\3\3\3\3\3\4\5\4\u00c1\n\4\3\4\5\4\u00c4"+
		"\n\4\3\4\3\4\3\4\3\4\5\4\u00ca\n\4\3\4\3\4\5\4\u00ce\n\4\3\5\3\5\3\5\3"+
		"\5\6\5\u00d4\n\5\r\5\16\5\u00d5\3\5\3\5\3\5\6\5\u00db\n\5\r\5\16\5\u00dc"+
		"\3\5\3\5\5\5\u00e1\n\5\3\6\3\6\3\6\3\6\6\6\u00e7\n\6\r\6\16\6\u00e8\3"+
		"\6\3\6\3\6\6\6\u00ee\n\6\r\6\16\6\u00ef\3\6\3\6\5\6\u00f4\n\6\3\7\6\7"+
		"\u00f7\n\7\r\7\16\7\u00f8\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n"+
		"\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21"+
		"\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30"+
		"\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37"+
		"\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3%\3&\3&\3&\3\'\3\'\3(\3(\3)\3"+
		")\3*\3*\3+\3+\3+\3,\3,\3,\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62\3"+
		"\62\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3\66\3\66\3\67\3\67\3\67\3\67\3"+
		"8\38\38\39\39\3:\3:\3:\3:\5:\u0194\n:\3;\3;\3<\3<\3<\3=\3=\3>\3>\3>\3"+
		"?\3?\3@\3@\3A\3A\3B\3B\3C\3C\3D\3D\3E\3E\3F\3F\3G\3G\3H\3H\3I\3I\3J\3"+
		"J\3K\3K\4\u009a\u00a0\2L\3\3\5\4\7\5\t\6\13\7\r\2\17\2\21\b\23\2\25\2"+
		"\27\2\31\2\33\2\35\2\37\2!\2#\2%\2\'\2)\2+\2-\2/\2\61\2\63\2\65\2\67\2"+
		"9\2;\2=\2?\2A\2C\2E\2G\2I\tK\nM\13O\fQ\rS\16U\17W\20Y\21[\22]\23_\24a"+
		"\25c\26e\27g\30i\31k\32m\33o\34q\35s\36u\37w y!{\"}#\177$\u0081%\u0083"+
		"&\u0085\'\u0087(\u0089)\u008b*\u008d+\u008f,\u0091-\u0093.\u0095/\3\2"+
		"\"\7\2&&\62;C\\aac|\6\2&&C\\aac|\3\2$$\4\2))^^\3\2\62;\5\2\62;CHch\4\2"+
		"CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4"+
		"\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTt"+
		"t\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2"+
		"\u01b0\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\21\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S"+
		"\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2"+
		"\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2"+
		"\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y"+
		"\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3"+
		"\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2"+
		"\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095"+
		"\3\2\2\2\3\u00b0\3\2\2\2\5\u00b2\3\2\2\2\7\u00c0\3\2\2\2\t\u00e0\3\2\2"+
		"\2\13\u00f3\3\2\2\2\r\u00f6\3\2\2\2\17\u00fa\3\2\2\2\21\u00fc\3\2\2\2"+
		"\23\u0127\3\2\2\2\25\u0129\3\2\2\2\27\u012b\3\2\2\2\31\u012d\3\2\2\2\33"+
		"\u012f\3\2\2\2\35\u0131\3\2\2\2\37\u0133\3\2\2\2!\u0135\3\2\2\2#\u0137"+
		"\3\2\2\2%\u0139\3\2\2\2\'\u013b\3\2\2\2)\u013d\3\2\2\2+\u013f\3\2\2\2"+
		"-\u0141\3\2\2\2/\u0143\3\2\2\2\61\u0145\3\2\2\2\63\u0147\3\2\2\2\65\u0149"+
		"\3\2\2\2\67\u014b\3\2\2\29\u014d\3\2\2\2;\u014f\3\2\2\2=\u0151\3\2\2\2"+
		"?\u0153\3\2\2\2A\u0155\3\2\2\2C\u0157\3\2\2\2E\u0159\3\2\2\2G\u015b\3"+
		"\2\2\2I\u015d\3\2\2\2K\u0160\3\2\2\2M\u0163\3\2\2\2O\u0165\3\2\2\2Q\u0167"+
		"\3\2\2\2S\u0169\3\2\2\2U\u016b\3\2\2\2W\u016e\3\2\2\2Y\u0171\3\2\2\2["+
		"\u0173\3\2\2\2]\u0175\3\2\2\2_\u0177\3\2\2\2a\u0179\3\2\2\2c\u017b\3\2"+
		"\2\2e\u017d\3\2\2\2g\u017f\3\2\2\2i\u0181\3\2\2\2k\u0183\3\2\2\2m\u0186"+
		"\3\2\2\2o\u018a\3\2\2\2q\u018d\3\2\2\2s\u0193\3\2\2\2u\u0195\3\2\2\2w"+
		"\u0197\3\2\2\2y\u019a\3\2\2\2{\u019c\3\2\2\2}\u019f\3\2\2\2\177\u01a1"+
		"\3\2\2\2\u0081\u01a3\3\2\2\2\u0083\u01a5\3\2\2\2\u0085\u01a7\3\2\2\2\u0087"+
		"\u01a9\3\2\2\2\u0089\u01ab\3\2\2\2\u008b\u01ad\3\2\2\2\u008d\u01af\3\2"+
		"\2\2\u008f\u01b1\3\2\2\2\u0091\u01b3\3\2\2\2\u0093\u01b5\3\2\2\2\u0095"+
		"\u01b7\3\2\2\2\u0097\u0099\t\2\2\2\u0098\u0097\3\2\2\2\u0099\u009c\3\2"+
		"\2\2\u009a\u009b\3\2\2\2\u009a\u0098\3\2\2\2\u009b\u009e\3\2\2\2\u009c"+
		"\u009a\3\2\2\2\u009d\u009f\t\3\2\2\u009e\u009d\3\2\2\2\u009f\u00a0\3\2"+
		"\2\2\u00a0\u00a1\3\2\2\2\u00a0\u009e\3\2\2\2\u00a1\u00a5\3\2\2\2\u00a2"+
		"\u00a4\t\2\2\2\u00a3\u00a2\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a3\3\2"+
		"\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00b1\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8"+
		"\u00aa\5\u008dG\2\u00a9\u00ab\n\4\2\2\u00aa\u00a9\3\2\2\2\u00ab\u00ac"+
		"\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae"+
		"\u00af\5\u008dG\2\u00af\u00b1\3\2\2\2\u00b0\u009a\3\2\2\2\u00b0\u00a8"+
		"\3\2\2\2\u00b1\4\3\2\2\2\u00b2\u00ba\5\u008fH\2\u00b3\u00b4\7^\2\2\u00b4"+
		"\u00b9\13\2\2\2\u00b5\u00b6\7)\2\2\u00b6\u00b9\7)\2\2\u00b7\u00b9\n\5"+
		"\2\2\u00b8\u00b3\3\2\2\2\u00b8\u00b5\3\2\2\2\u00b8\u00b7\3\2\2\2\u00b9"+
		"\u00bc\3\2\2\2\u00ba\u00b8\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00bd\3\2"+
		"\2\2\u00bc\u00ba\3\2\2\2\u00bd\u00be\5\u008fH\2\u00be\6\3\2\2\2\u00bf"+
		"\u00c1\5\r\7\2\u00c0\u00bf\3\2\2\2\u00c0\u00c1\3\2\2\2\u00c1\u00c3\3\2"+
		"\2\2\u00c2\u00c4\5i\65\2\u00c3\u00c2\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4"+
		"\u00c5\3\2\2\2\u00c5\u00cd\5\r\7\2\u00c6\u00c9\5\33\16\2\u00c7\u00ca\5"+
		"_\60\2\u00c8\u00ca\5a\61\2\u00c9\u00c7\3\2\2\2\u00c9\u00c8\3\2\2\2\u00c9"+
		"\u00ca\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00cc\5\r\7\2\u00cc\u00ce\3\2"+
		"\2\2\u00cd\u00c6\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\b\3\2\2\2\u00cf\u00d0"+
		"\7\62\2\2\u00d0\u00d1\7z\2\2\u00d1\u00d3\3\2\2\2\u00d2\u00d4\5\17\b\2"+
		"\u00d3\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d3\3\2\2\2\u00d5\u00d6"+
		"\3\2\2\2\u00d6\u00e1\3\2\2\2\u00d7\u00d8\7Z\2\2\u00d8\u00da\5\u008fH\2"+
		"\u00d9\u00db\5\17\b\2\u00da\u00d9\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc\u00da"+
		"\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00df\5\u008fH"+
		"\2\u00df\u00e1\3\2\2\2\u00e0\u00cf\3\2\2\2\u00e0\u00d7\3\2\2\2\u00e1\n"+
		"\3\2\2\2\u00e2\u00e3\7\62\2\2\u00e3\u00e4\7d\2\2\u00e4\u00e6\3\2\2\2\u00e5"+
		"\u00e7\4\62\63\2\u00e6\u00e5\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8\u00e6\3"+
		"\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00f4\3\2\2\2\u00ea\u00eb\5\25\13\2\u00eb"+
		"\u00ed\5\u008fH\2\u00ec\u00ee\4\62\63\2\u00ed\u00ec\3\2\2\2\u00ee\u00ef"+
		"\3\2\2\2\u00ef\u00ed\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1"+
		"\u00f2\5\u008fH\2\u00f2\u00f4\3\2\2\2\u00f3\u00e2\3\2\2\2\u00f3\u00ea"+
		"\3\2\2\2\u00f4\f\3\2\2\2\u00f5\u00f7\t\6\2\2\u00f6\u00f5\3\2\2\2\u00f7"+
		"\u00f8\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\16\3\2\2"+
		"\2\u00fa\u00fb\t\7\2\2\u00fb\20\3\2\2\2\u00fc\u00fd\7F\2\2\u00fd\u00fe"+
		"\7Q\2\2\u00fe\u00ff\7\"\2\2\u00ff\u0100\7P\2\2\u0100\u0101\7Q\2\2\u0101"+
		"\u0102\7V\2\2\u0102\u0103\7\"\2\2\u0103\u0104\7O\2\2\u0104\u0105\7C\2"+
		"\2\u0105\u0106\7V\2\2\u0106\u0107\7E\2\2\u0107\u0108\7J\2\2\u0108\u0109"+
		"\7\"\2\2\u0109\u010a\7C\2\2\u010a\u010b\7P\2\2\u010b\u010c\7[\2\2\u010c"+
		"\u010d\7\"\2\2\u010d\u010e\7V\2\2\u010e\u010f\7J\2\2\u010f\u0110\7K\2"+
		"\2\u0110\u0111\7P\2\2\u0111\u0112\7I\2\2\u0112\u0113\7.\2\2\u0113\u0114"+
		"\7\"\2\2\u0114\u0115\7L\2\2\u0115\u0116\7W\2\2\u0116\u0117\7U\2\2\u0117"+
		"\u0118\7V\2\2\u0118\u0119\7\"\2\2\u0119\u011a\7H\2\2\u011a\u011b\7Q\2"+
		"\2\u011b\u011c\7T\2\2\u011c\u011d\7\"\2\2\u011d\u011e\7I\2\2\u011e\u011f"+
		"\7G\2\2\u011f\u0120\7P\2\2\u0120\u0121\7G\2\2\u0121\u0122\7T\2\2\u0122"+
		"\u0123\7C\2\2\u0123\u0124\7V\2\2\u0124\u0125\7Q\2\2\u0125\u0126\7T\2\2"+
		"\u0126\22\3\2\2\2\u0127\u0128\t\b\2\2\u0128\24\3\2\2\2\u0129\u012a\t\t"+
		"\2\2\u012a\26\3\2\2\2\u012b\u012c\t\n\2\2\u012c\30\3\2\2\2\u012d\u012e"+
		"\t\13\2\2\u012e\32\3\2\2\2\u012f\u0130\t\f\2\2\u0130\34\3\2\2\2\u0131"+
		"\u0132\t\r\2\2\u0132\36\3\2\2\2\u0133\u0134\t\16\2\2\u0134 \3\2\2\2\u0135"+
		"\u0136\t\17\2\2\u0136\"\3\2\2\2\u0137\u0138\t\20\2\2\u0138$\3\2\2\2\u0139"+
		"\u013a\t\21\2\2\u013a&\3\2\2\2\u013b\u013c\t\22\2\2\u013c(\3\2\2\2\u013d"+
		"\u013e\t\23\2\2\u013e*\3\2\2\2\u013f\u0140\t\24\2\2\u0140,\3\2\2\2\u0141"+
		"\u0142\t\25\2\2\u0142.\3\2\2\2\u0143\u0144\t\26\2\2\u0144\60\3\2\2\2\u0145"+
		"\u0146\t\27\2\2\u0146\62\3\2\2\2\u0147\u0148\t\30\2\2\u0148\64\3\2\2\2"+
		"\u0149\u014a\t\31\2\2\u014a\66\3\2\2\2\u014b\u014c\t\32\2\2\u014c8\3\2"+
		"\2\2\u014d\u014e\t\33\2\2\u014e:\3\2\2\2\u014f\u0150\t\34\2\2\u0150<\3"+
		"\2\2\2\u0151\u0152\t\35\2\2\u0152>\3\2\2\2\u0153\u0154\t\36\2\2\u0154"+
		"@\3\2\2\2\u0155\u0156\t\37\2\2\u0156B\3\2\2\2\u0157\u0158\t \2\2\u0158"+
		"D\3\2\2\2\u0159\u015a\t!\2\2\u015aF\3\2\2\2\u015b\u015c\7a\2\2\u015cH"+
		"\3\2\2\2\u015d\u015e\7(\2\2\u015e\u015f\7(\2\2\u015fJ\3\2\2\2\u0160\u0161"+
		"\7~\2\2\u0161\u0162\7~\2\2\u0162L\3\2\2\2\u0163\u0164\7#\2\2\u0164N\3"+
		"\2\2\2\u0165\u0166\7\u0080\2\2\u0166P\3\2\2\2\u0167\u0168\7~\2\2\u0168"+
		"R\3\2\2\2\u0169\u016a\7(\2\2\u016aT\3\2\2\2\u016b\u016c\7>\2\2\u016c\u016d"+
		"\7>\2\2\u016dV\3\2\2\2\u016e\u016f\7@\2\2\u016f\u0170\7@\2\2\u0170X\3"+
		"\2\2\2\u0171\u0172\7`\2\2\u0172Z\3\2\2\2\u0173\u0174\7\'\2\2\u0174\\\3"+
		"\2\2\2\u0175\u0176\7<\2\2\u0176^\3\2\2\2\u0177\u0178\7-\2\2\u0178`\3\2"+
		"\2\2\u0179\u017a\7/\2\2\u017ab\3\2\2\2\u017b\u017c\7,\2\2\u017cd\3\2\2"+
		"\2\u017d\u017e\7\61\2\2\u017ef\3\2\2\2\u017f\u0180\7^\2\2\u0180h\3\2\2"+
		"\2\u0181\u0182\7\60\2\2\u0182j\3\2\2\2\u0183\u0184\7\60\2\2\u0184\u0185"+
		"\7,\2\2\u0185l\3\2\2\2\u0186\u0187\7>\2\2\u0187\u0188\7?\2\2\u0188\u0189"+
		"\7@\2\2\u0189n\3\2\2\2\u018a\u018b\7?\2\2\u018b\u018c\7?\2\2\u018cp\3"+
		"\2\2\2\u018d\u018e\7?\2\2\u018er\3\2\2\2\u018f\u0190\7>\2\2\u0190\u0194"+
		"\7@\2\2\u0191\u0192\7#\2\2\u0192\u0194\7?\2\2\u0193\u018f\3\2\2\2\u0193"+
		"\u0191\3\2\2\2\u0194t\3\2\2\2\u0195\u0196\7@\2\2\u0196v\3\2\2\2\u0197"+
		"\u0198\7@\2\2\u0198\u0199\7?\2\2\u0199x\3\2\2\2\u019a\u019b\7>\2\2\u019b"+
		"z\3\2\2\2\u019c\u019d\7>\2\2\u019d\u019e\7?\2\2\u019e|\3\2\2\2\u019f\u01a0"+
		"\7%\2\2\u01a0~\3\2\2\2\u01a1\u01a2\7*\2\2\u01a2\u0080\3\2\2\2\u01a3\u01a4"+
		"\7+\2\2\u01a4\u0082\3\2\2\2\u01a5\u01a6\7}\2\2\u01a6\u0084\3\2\2\2\u01a7"+
		"\u01a8\7\177\2\2\u01a8\u0086\3\2\2\2\u01a9\u01aa\7]\2\2\u01aa\u0088\3"+
		"\2\2\2\u01ab\u01ac\7_\2\2\u01ac\u008a\3\2\2\2\u01ad\u01ae\7.\2\2\u01ae"+
		"\u008c\3\2\2\2\u01af\u01b0\7$\2\2\u01b0\u008e\3\2\2\2\u01b1\u01b2\7)\2"+
		"\2\u01b2\u0090\3\2\2\2\u01b3\u01b4\7A\2\2\u01b4\u0092\3\2\2\2\u01b5\u01b6"+
		"\7B\2\2\u01b6\u0094\3\2\2\2\u01b7\u01b8\7=\2\2\u01b8\u0096\3\2\2\2\26"+
		"\2\u009a\u00a0\u00a5\u00ac\u00b0\u00b8\u00ba\u00c0\u00c3\u00c9\u00cd\u00d5"+
		"\u00dc\u00e0\u00e8\u00ef\u00f3\u00f8\u0193\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}