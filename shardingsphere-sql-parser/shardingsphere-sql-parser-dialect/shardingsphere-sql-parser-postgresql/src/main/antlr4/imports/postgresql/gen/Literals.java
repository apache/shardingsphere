// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/Literals.g4 by ANTLR 4.9.1
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
		AND_=7, OR_=8, NOT_=9, TILDE_=10, VERTICAL_BAR_=11, AMPERSAND_=12, SIGNED_LEFT_SHIFT_=13, 
		SIGNED_RIGHT_SHIFT_=14, CARET_=15, MOD_=16, COLON_=17, PLUS_=18, MINUS_=19, 
		ASTERISK_=20, SLASH_=21, BACKSLASH_=22, DOT_=23, DOT_ASTERISK_=24, SAFE_EQ_=25, 
		DEQ_=26, EQ_=27, CQ_=28, NEQ_=29, GT_=30, GTE_=31, LT_=32, LTE_=33, POUND_=34, 
		LP_=35, RP_=36, LBE_=37, RBE_=38, LBT_=39, RBT_=40, COMMA_=41, DQ_=42, 
		SQ_=43, BQ_=44, QUESTION_=45, AT_=46, SEMI_=47, TILDE_TILDE_=48, NOT_TILDE_TILDE_=49, 
		TYPE_CAST_=50;
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
			"X", "Y", "Z", "UL_", "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", 
			"AMPERSAND_", "SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", 
			"MOD_", "COLON_", "PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", 
			"DOT_", "DOT_ASTERISK_", "SAFE_EQ_", "DEQ_", "EQ_", "CQ_", "NEQ_", "GT_", 
			"GTE_", "LT_", "LTE_", "POUND_", "LP_", "RP_", "LBE_", "RBE_", "LBT_", 
			"RBT_", "COMMA_", "DQ_", "SQ_", "BQ_", "QUESTION_", "AT_", "SEMI_", "TILDE_TILDE_", 
			"NOT_TILDE_TILDE_", "TYPE_CAST_"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'", 
			"'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", "'%'", 
			"':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", "'=='", 
			"'='", "':='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", "')'", 
			"'{'", "'}'", "'['", "']'", "','", "'\"'", "'''", "'`'", "'?'", "'@'", 
			"';'", "'~~'", "'!~~'", "'::'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "IDENTIFIER_", "STRING_", "NUMBER_", "HEX_DIGIT_", "BIT_NUM_", 
			"FOR_GENERATOR", "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "CQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", 
			"POUND_", "LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", 
			"SQ_", "BQ_", "QUESTION_", "AT_", "SEMI_", "TILDE_TILDE_", "NOT_TILDE_TILDE_", 
			"TYPE_CAST_"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\64\u01d2\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t"+
		"=\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4"+
		"I\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\3\2\7\2\u00a3\n\2\f\2\16"+
		"\2\u00a6\13\2\3\2\6\2\u00a9\n\2\r\2\16\2\u00aa\3\2\7\2\u00ae\n\2\f\2\16"+
		"\2\u00b1\13\2\3\2\3\2\6\2\u00b5\n\2\r\2\16\2\u00b6\3\2\3\2\5\2\u00bb\n"+
		"\2\3\3\3\3\3\3\3\3\3\3\3\3\7\3\u00c3\n\3\f\3\16\3\u00c6\13\3\3\3\3\3\3"+
		"\4\5\4\u00cb\n\4\3\4\5\4\u00ce\n\4\3\4\3\4\3\4\3\4\5\4\u00d4\n\4\3\4\3"+
		"\4\5\4\u00d8\n\4\3\5\3\5\3\5\3\5\6\5\u00de\n\5\r\5\16\5\u00df\3\5\3\5"+
		"\3\5\6\5\u00e5\n\5\r\5\16\5\u00e6\3\5\3\5\5\5\u00eb\n\5\3\6\3\6\3\6\3"+
		"\6\6\6\u00f1\n\6\r\6\16\6\u00f2\3\6\3\6\3\6\6\6\u00f8\n\6\r\6\16\6\u00f9"+
		"\3\6\3\6\5\6\u00fe\n\6\3\7\6\7\u0101\n\7\r\7\16\7\u0102\3\b\3\b\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16"+
		"\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25"+
		"\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34"+
		"\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3"+
		"%\3&\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3+\3,\3,\3,\3-\3-\3.\3.\3/"+
		"\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3"+
		"\66\3\66\3\67\3\67\3\67\3\67\38\38\38\39\39\3:\3:\3:\3;\3;\3;\3;\5;\u01a1"+
		"\n;\3<\3<\3=\3=\3=\3>\3>\3?\3?\3?\3@\3@\3A\3A\3B\3B\3C\3C\3D\3D\3E\3E"+
		"\3F\3F\3G\3G\3H\3H\3I\3I\3J\3J\3K\3K\3L\3L\3M\3M\3N\3N\3N\3O\3O\3O\3O"+
		"\3P\3P\3P\4\u00a4\u00aa\2Q\3\3\5\4\7\5\t\6\13\7\r\2\17\2\21\b\23\2\25"+
		"\2\27\2\31\2\33\2\35\2\37\2!\2#\2%\2\'\2)\2+\2-\2/\2\61\2\63\2\65\2\67"+
		"\29\2;\2=\2?\2A\2C\2E\2G\2I\tK\nM\13O\fQ\rS\16U\17W\20Y\21[\22]\23_\24"+
		"a\25c\26e\27g\30i\31k\32m\33o\34q\35s\36u\37w y!{\"}#\177$\u0081%\u0083"+
		"&\u0085\'\u0087(\u0089)\u008b*\u008d+\u008f,\u0091-\u0093.\u0095/\u0097"+
		"\60\u0099\61\u009b\62\u009d\63\u009f\64\3\2\"\7\2&&\62;C\\aac|\6\2&&C"+
		"\\aac|\3\2$$\4\2))^^\3\2\62;\5\2\62;CHch\4\2CCcc\4\2DDdd\4\2EEee\4\2F"+
		"Fff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4"+
		"\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWw"+
		"w\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u01c9\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\21\3\2\2\2\2I\3\2\2\2\2"+
		"K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3"+
		"\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2"+
		"\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2"+
		"q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}\3"+
		"\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2\2"+
		"\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f"+
		"\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2"+
		"\2\2\u0099\3\2\2\2\2\u009b\3\2\2\2\2\u009d\3\2\2\2\2\u009f\3\2\2\2\3\u00ba"+
		"\3\2\2\2\5\u00bc\3\2\2\2\7\u00ca\3\2\2\2\t\u00ea\3\2\2\2\13\u00fd\3\2"+
		"\2\2\r\u0100\3\2\2\2\17\u0104\3\2\2\2\21\u0106\3\2\2\2\23\u0131\3\2\2"+
		"\2\25\u0133\3\2\2\2\27\u0135\3\2\2\2\31\u0137\3\2\2\2\33\u0139\3\2\2\2"+
		"\35\u013b\3\2\2\2\37\u013d\3\2\2\2!\u013f\3\2\2\2#\u0141\3\2\2\2%\u0143"+
		"\3\2\2\2\'\u0145\3\2\2\2)\u0147\3\2\2\2+\u0149\3\2\2\2-\u014b\3\2\2\2"+
		"/\u014d\3\2\2\2\61\u014f\3\2\2\2\63\u0151\3\2\2\2\65\u0153\3\2\2\2\67"+
		"\u0155\3\2\2\29\u0157\3\2\2\2;\u0159\3\2\2\2=\u015b\3\2\2\2?\u015d\3\2"+
		"\2\2A\u015f\3\2\2\2C\u0161\3\2\2\2E\u0163\3\2\2\2G\u0165\3\2\2\2I\u0167"+
		"\3\2\2\2K\u016a\3\2\2\2M\u016d\3\2\2\2O\u016f\3\2\2\2Q\u0171\3\2\2\2S"+
		"\u0173\3\2\2\2U\u0175\3\2\2\2W\u0178\3\2\2\2Y\u017b\3\2\2\2[\u017d\3\2"+
		"\2\2]\u017f\3\2\2\2_\u0181\3\2\2\2a\u0183\3\2\2\2c\u0185\3\2\2\2e\u0187"+
		"\3\2\2\2g\u0189\3\2\2\2i\u018b\3\2\2\2k\u018d\3\2\2\2m\u0190\3\2\2\2o"+
		"\u0194\3\2\2\2q\u0197\3\2\2\2s\u0199\3\2\2\2u\u01a0\3\2\2\2w\u01a2\3\2"+
		"\2\2y\u01a4\3\2\2\2{\u01a7\3\2\2\2}\u01a9\3\2\2\2\177\u01ac\3\2\2\2\u0081"+
		"\u01ae\3\2\2\2\u0083\u01b0\3\2\2\2\u0085\u01b2\3\2\2\2\u0087\u01b4\3\2"+
		"\2\2\u0089\u01b6\3\2\2\2\u008b\u01b8\3\2\2\2\u008d\u01ba\3\2\2\2\u008f"+
		"\u01bc\3\2\2\2\u0091\u01be\3\2\2\2\u0093\u01c0\3\2\2\2\u0095\u01c2\3\2"+
		"\2\2\u0097\u01c4\3\2\2\2\u0099\u01c6\3\2\2\2\u009b\u01c8\3\2\2\2\u009d"+
		"\u01cb\3\2\2\2\u009f\u01cf\3\2\2\2\u00a1\u00a3\t\2\2\2\u00a2\u00a1\3\2"+
		"\2\2\u00a3\u00a6\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a5"+
		"\u00a8\3\2\2\2\u00a6\u00a4\3\2\2\2\u00a7\u00a9\t\3\2\2\u00a8\u00a7\3\2"+
		"\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ab\3\2\2\2\u00aa\u00a8\3\2\2\2\u00ab"+
		"\u00af\3\2\2\2\u00ac\u00ae\t\2\2\2\u00ad\u00ac\3\2\2\2\u00ae\u00b1\3\2"+
		"\2\2\u00af\u00ad\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00bb\3\2\2\2\u00b1"+
		"\u00af\3\2\2\2\u00b2\u00b4\5\u008fH\2\u00b3\u00b5\n\4\2\2\u00b4\u00b3"+
		"\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7"+
		"\u00b8\3\2\2\2\u00b8\u00b9\5\u008fH\2\u00b9\u00bb\3\2\2\2\u00ba\u00a4"+
		"\3\2\2\2\u00ba\u00b2\3\2\2\2\u00bb\4\3\2\2\2\u00bc\u00c4\5\u0091I\2\u00bd"+
		"\u00be\7^\2\2\u00be\u00c3\13\2\2\2\u00bf\u00c0\7)\2\2\u00c0\u00c3\7)\2"+
		"\2\u00c1\u00c3\n\5\2\2\u00c2\u00bd\3\2\2\2\u00c2\u00bf\3\2\2\2\u00c2\u00c1"+
		"\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5"+
		"\u00c7\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c7\u00c8\5\u0091I\2\u00c8\6\3\2"+
		"\2\2\u00c9\u00cb\5\r\7\2\u00ca\u00c9\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb"+
		"\u00cd\3\2\2\2\u00cc\u00ce\5i\65\2\u00cd\u00cc\3\2\2\2\u00cd\u00ce\3\2"+
		"\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00d7\5\r\7\2\u00d0\u00d3\5\33\16\2\u00d1"+
		"\u00d4\5_\60\2\u00d2\u00d4\5a\61\2\u00d3\u00d1\3\2\2\2\u00d3\u00d2\3\2"+
		"\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\5\r\7\2\u00d6"+
		"\u00d8\3\2\2\2\u00d7\u00d0\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\b\3\2\2\2"+
		"\u00d9\u00da\7\62\2\2\u00da\u00db\7z\2\2\u00db\u00dd\3\2\2\2\u00dc\u00de"+
		"\5\17\b\2\u00dd\u00dc\3\2\2\2\u00de\u00df\3\2\2\2\u00df\u00dd\3\2\2\2"+
		"\u00df\u00e0\3\2\2\2\u00e0\u00eb\3\2\2\2\u00e1\u00e2\7Z\2\2\u00e2\u00e4"+
		"\5\u0091I\2\u00e3\u00e5\5\17\b\2\u00e4\u00e3\3\2\2\2\u00e5\u00e6\3\2\2"+
		"\2\u00e6\u00e4\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8\u00e9"+
		"\5\u0091I\2\u00e9\u00eb\3\2\2\2\u00ea\u00d9\3\2\2\2\u00ea\u00e1\3\2\2"+
		"\2\u00eb\n\3\2\2\2\u00ec\u00ed\7\62\2\2\u00ed\u00ee\7d\2\2\u00ee\u00f0"+
		"\3\2\2\2\u00ef\u00f1\4\62\63\2\u00f0\u00ef\3\2\2\2\u00f1\u00f2\3\2\2\2"+
		"\u00f2\u00f0\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00fe\3\2\2\2\u00f4\u00f5"+
		"\5\25\13\2\u00f5\u00f7\5\u0091I\2\u00f6\u00f8\4\62\63\2\u00f7\u00f6\3"+
		"\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa"+
		"\u00fb\3\2\2\2\u00fb\u00fc\5\u0091I\2\u00fc\u00fe\3\2\2\2\u00fd\u00ec"+
		"\3\2\2\2\u00fd\u00f4\3\2\2\2\u00fe\f\3\2\2\2\u00ff\u0101\t\6\2\2\u0100"+
		"\u00ff\3\2\2\2\u0101\u0102\3\2\2\2\u0102\u0100\3\2\2\2\u0102\u0103\3\2"+
		"\2\2\u0103\16\3\2\2\2\u0104\u0105\t\7\2\2\u0105\20\3\2\2\2\u0106\u0107"+
		"\7F\2\2\u0107\u0108\7Q\2\2\u0108\u0109\7\"\2\2\u0109\u010a\7P\2\2\u010a"+
		"\u010b\7Q\2\2\u010b\u010c\7V\2\2\u010c\u010d\7\"\2\2\u010d\u010e\7O\2"+
		"\2\u010e\u010f\7C\2\2\u010f\u0110\7V\2\2\u0110\u0111\7E\2\2\u0111\u0112"+
		"\7J\2\2\u0112\u0113\7\"\2\2\u0113\u0114\7C\2\2\u0114\u0115\7P\2\2\u0115"+
		"\u0116\7[\2\2\u0116\u0117\7\"\2\2\u0117\u0118\7V\2\2\u0118\u0119\7J\2"+
		"\2\u0119\u011a\7K\2\2\u011a\u011b\7P\2\2\u011b\u011c\7I\2\2\u011c\u011d"+
		"\7.\2\2\u011d\u011e\7\"\2\2\u011e\u011f\7L\2\2\u011f\u0120\7W\2\2\u0120"+
		"\u0121\7U\2\2\u0121\u0122\7V\2\2\u0122\u0123\7\"\2\2\u0123\u0124\7H\2"+
		"\2\u0124\u0125\7Q\2\2\u0125\u0126\7T\2\2\u0126\u0127\7\"\2\2\u0127\u0128"+
		"\7I\2\2\u0128\u0129\7G\2\2\u0129\u012a\7P\2\2\u012a\u012b\7G\2\2\u012b"+
		"\u012c\7T\2\2\u012c\u012d\7C\2\2\u012d\u012e\7V\2\2\u012e\u012f\7Q\2\2"+
		"\u012f\u0130\7T\2\2\u0130\22\3\2\2\2\u0131\u0132\t\b\2\2\u0132\24\3\2"+
		"\2\2\u0133\u0134\t\t\2\2\u0134\26\3\2\2\2\u0135\u0136\t\n\2\2\u0136\30"+
		"\3\2\2\2\u0137\u0138\t\13\2\2\u0138\32\3\2\2\2\u0139\u013a\t\f\2\2\u013a"+
		"\34\3\2\2\2\u013b\u013c\t\r\2\2\u013c\36\3\2\2\2\u013d\u013e\t\16\2\2"+
		"\u013e \3\2\2\2\u013f\u0140\t\17\2\2\u0140\"\3\2\2\2\u0141\u0142\t\20"+
		"\2\2\u0142$\3\2\2\2\u0143\u0144\t\21\2\2\u0144&\3\2\2\2\u0145\u0146\t"+
		"\22\2\2\u0146(\3\2\2\2\u0147\u0148\t\23\2\2\u0148*\3\2\2\2\u0149\u014a"+
		"\t\24\2\2\u014a,\3\2\2\2\u014b\u014c\t\25\2\2\u014c.\3\2\2\2\u014d\u014e"+
		"\t\26\2\2\u014e\60\3\2\2\2\u014f\u0150\t\27\2\2\u0150\62\3\2\2\2\u0151"+
		"\u0152\t\30\2\2\u0152\64\3\2\2\2\u0153\u0154\t\31\2\2\u0154\66\3\2\2\2"+
		"\u0155\u0156\t\32\2\2\u01568\3\2\2\2\u0157\u0158\t\33\2\2\u0158:\3\2\2"+
		"\2\u0159\u015a\t\34\2\2\u015a<\3\2\2\2\u015b\u015c\t\35\2\2\u015c>\3\2"+
		"\2\2\u015d\u015e\t\36\2\2\u015e@\3\2\2\2\u015f\u0160\t\37\2\2\u0160B\3"+
		"\2\2\2\u0161\u0162\t \2\2\u0162D\3\2\2\2\u0163\u0164\t!\2\2\u0164F\3\2"+
		"\2\2\u0165\u0166\7a\2\2\u0166H\3\2\2\2\u0167\u0168\7(\2\2\u0168\u0169"+
		"\7(\2\2\u0169J\3\2\2\2\u016a\u016b\7~\2\2\u016b\u016c\7~\2\2\u016cL\3"+
		"\2\2\2\u016d\u016e\7#\2\2\u016eN\3\2\2\2\u016f\u0170\7\u0080\2\2\u0170"+
		"P\3\2\2\2\u0171\u0172\7~\2\2\u0172R\3\2\2\2\u0173\u0174\7(\2\2\u0174T"+
		"\3\2\2\2\u0175\u0176\7>\2\2\u0176\u0177\7>\2\2\u0177V\3\2\2\2\u0178\u0179"+
		"\7@\2\2\u0179\u017a\7@\2\2\u017aX\3\2\2\2\u017b\u017c\7`\2\2\u017cZ\3"+
		"\2\2\2\u017d\u017e\7\'\2\2\u017e\\\3\2\2\2\u017f\u0180\7<\2\2\u0180^\3"+
		"\2\2\2\u0181\u0182\7-\2\2\u0182`\3\2\2\2\u0183\u0184\7/\2\2\u0184b\3\2"+
		"\2\2\u0185\u0186\7,\2\2\u0186d\3\2\2\2\u0187\u0188\7\61\2\2\u0188f\3\2"+
		"\2\2\u0189\u018a\7^\2\2\u018ah\3\2\2\2\u018b\u018c\7\60\2\2\u018cj\3\2"+
		"\2\2\u018d\u018e\7\60\2\2\u018e\u018f\7,\2\2\u018fl\3\2\2\2\u0190\u0191"+
		"\7>\2\2\u0191\u0192\7?\2\2\u0192\u0193\7@\2\2\u0193n\3\2\2\2\u0194\u0195"+
		"\7?\2\2\u0195\u0196\7?\2\2\u0196p\3\2\2\2\u0197\u0198\7?\2\2\u0198r\3"+
		"\2\2\2\u0199\u019a\7<\2\2\u019a\u019b\7?\2\2\u019bt\3\2\2\2\u019c\u019d"+
		"\7>\2\2\u019d\u01a1\7@\2\2\u019e\u019f\7#\2\2\u019f\u01a1\7?\2\2\u01a0"+
		"\u019c\3\2\2\2\u01a0\u019e\3\2\2\2\u01a1v\3\2\2\2\u01a2\u01a3\7@\2\2\u01a3"+
		"x\3\2\2\2\u01a4\u01a5\7@\2\2\u01a5\u01a6\7?\2\2\u01a6z\3\2\2\2\u01a7\u01a8"+
		"\7>\2\2\u01a8|\3\2\2\2\u01a9\u01aa\7>\2\2\u01aa\u01ab\7?\2\2\u01ab~\3"+
		"\2\2\2\u01ac\u01ad\7%\2\2\u01ad\u0080\3\2\2\2\u01ae\u01af\7*\2\2\u01af"+
		"\u0082\3\2\2\2\u01b0\u01b1\7+\2\2\u01b1\u0084\3\2\2\2\u01b2\u01b3\7}\2"+
		"\2\u01b3\u0086\3\2\2\2\u01b4\u01b5\7\177\2\2\u01b5\u0088\3\2\2\2\u01b6"+
		"\u01b7\7]\2\2\u01b7\u008a\3\2\2\2\u01b8\u01b9\7_\2\2\u01b9\u008c\3\2\2"+
		"\2\u01ba\u01bb\7.\2\2\u01bb\u008e\3\2\2\2\u01bc\u01bd\7$\2\2\u01bd\u0090"+
		"\3\2\2\2\u01be\u01bf\7)\2\2\u01bf\u0092\3\2\2\2\u01c0\u01c1\7b\2\2\u01c1"+
		"\u0094\3\2\2\2\u01c2\u01c3\7A\2\2\u01c3\u0096\3\2\2\2\u01c4\u01c5\7B\2"+
		"\2\u01c5\u0098\3\2\2\2\u01c6\u01c7\7=\2\2\u01c7\u009a\3\2\2\2\u01c8\u01c9"+
		"\7\u0080\2\2\u01c9\u01ca\7\u0080\2\2\u01ca\u009c\3\2\2\2\u01cb\u01cc\7"+
		"#\2\2\u01cc\u01cd\7\u0080\2\2\u01cd\u01ce\7\u0080\2\2\u01ce\u009e\3\2"+
		"\2\2\u01cf\u01d0\7<\2\2\u01d0\u01d1\7<\2\2\u01d1\u00a0\3\2\2\2\26\2\u00a4"+
		"\u00aa\u00af\u00b6\u00ba\u00c2\u00c4\u00ca\u00cd\u00d3\u00d7\u00df\u00e6"+
		"\u00ea\u00f2\u00f9\u00fd\u0102\u01a0\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}