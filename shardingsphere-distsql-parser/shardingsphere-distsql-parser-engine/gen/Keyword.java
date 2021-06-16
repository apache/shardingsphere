// Generated from /home/totalo/code/shardingsphere/shardingsphere-distsql-parser/shardingsphere-distsql-parser-engine/src/main/antlr4/imports/Keyword.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Keyword extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WS=1, ADD=2, CREATE=3, ALTER=4, MODIFY=5, DROP=6, SHOW=7, START=8, STOP=9, 
		RESET=10, CHECK=11, RESOURCE=12, RESOURCES=13, RULE=14, FROM=15, SHARDING=16, 
		READWRITE_SPLITTING=17, WRITE_RESOURCE=18, READ_RESOURCES=19, AUTO_AWARE_RESOURCE=20, 
		REPLICA_QUERY=21, ENCRYPT=22, SHADOW=23, PRIMARY=24, REPLICA=25, GENERATED_KEY=26, 
		DEFAULT_TABLE_STRATEGY=27, SCALING=28, JOB=29, LIST=30, STATUS=31, HOST=32, 
		PORT=33, DB=34, USER=35, PASSWORD=36, TABLE=37, SHARDING_COLUMN=38, TYPE=39, 
		NAME=40, PROPERTIES=41, COLUMN=42, BINDING=43, RULES=44, BROADCAST=45, 
		DB_DISCOVERY=46, COLUMNS=47, CIPHER=48, PLAIN=49, FOR_GENERATOR=50;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"WS", "ADD", "CREATE", "ALTER", "MODIFY", "DROP", "SHOW", "START", "STOP", 
			"RESET", "CHECK", "RESOURCE", "RESOURCES", "RULE", "FROM", "SHARDING", 
			"READWRITE_SPLITTING", "WRITE_RESOURCE", "READ_RESOURCES", "AUTO_AWARE_RESOURCE", 
			"REPLICA_QUERY", "ENCRYPT", "SHADOW", "PRIMARY", "REPLICA", "GENERATED_KEY", 
			"DEFAULT_TABLE_STRATEGY", "SCALING", "JOB", "LIST", "STATUS", "HOST", 
			"PORT", "DB", "USER", "PASSWORD", "TABLE", "SHARDING_COLUMN", "TYPE", 
			"NAME", "PROPERTIES", "COLUMN", "BINDING", "RULES", "BROADCAST", "DB_DISCOVERY", 
			"COLUMNS", "CIPHER", "PLAIN", "FOR_GENERATOR", "A", "B", "C", "D", "E", 
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", 
			"T", "U", "V", "W", "X", "Y", "Z", "UL_"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WS", "ADD", "CREATE", "ALTER", "MODIFY", "DROP", "SHOW", "START", 
			"STOP", "RESET", "CHECK", "RESOURCE", "RESOURCES", "RULE", "FROM", "SHARDING", 
			"READWRITE_SPLITTING", "WRITE_RESOURCE", "READ_RESOURCES", "AUTO_AWARE_RESOURCE", 
			"REPLICA_QUERY", "ENCRYPT", "SHADOW", "PRIMARY", "REPLICA", "GENERATED_KEY", 
			"DEFAULT_TABLE_STRATEGY", "SCALING", "JOB", "LIST", "STATUS", "HOST", 
			"PORT", "DB", "USER", "PASSWORD", "TABLE", "SHARDING_COLUMN", "TYPE", 
			"NAME", "PROPERTIES", "COLUMN", "BINDING", "RULES", "BROADCAST", "DB_DISCOVERY", 
			"COLUMNS", "CIPHER", "PLAIN", "FOR_GENERATOR"
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


	public Keyword(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Keyword.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\64\u029b\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t"+
		"=\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4"+
		"I\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\3\2\6\2\u009f\n\2\r\2\16\2\u00a0\3"+
		"\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3"+
		"\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17"+
		"\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37"+
		"\3\37\3 \3 \3 \3 \3 \3 \3 \3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3#\3#\3"+
		"#\3$\3$\3$\3$\3$\3%\3%\3%\3%\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3\'\3\'"+
		"\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3"+
		"(\3)\3)\3)\3)\3)\3*\3*\3*\3*\3*\3*\3*\3*\3*\3*\3*\3+\3+\3+\3+\3+\3+\3"+
		"+\3,\3,\3,\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3-\3.\3.\3.\3.\3.\3.\3.\3.\3"+
		".\3.\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3"+
		"\60\3\60\3\60\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\62\3\62\3\62\3\62\3"+
		"\62\3\62\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3"+
		"\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3"+
		"\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3"+
		"\63\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3\66\3\67\3\67\38\38\39\39\3:\3"+
		":\3;\3;\3<\3<\3=\3=\3>\3>\3?\3?\3@\3@\3A\3A\3B\3B\3C\3C\3D\3D\3E\3E\3"+
		"F\3F\3G\3G\3H\3H\3I\3I\3J\3J\3K\3K\3L\3L\3M\3M\3N\3N\2\2O\3\3\5\4\7\5"+
		"\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G"+
		"%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64g\2i\2k\2m\2o\2q\2s\2u\2w\2"+
		"y\2{\2}\2\177\2\u0081\2\u0083\2\u0085\2\u0087\2\u0089\2\u008b\2\u008d"+
		"\2\u008f\2\u0091\2\u0093\2\u0095\2\u0097\2\u0099\2\u009b\2\3\2\35\5\2"+
		"\13\f\17\17\"\"\4\2CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2II"+
		"ii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2"+
		"RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4"+
		"\2[[{{\4\2\\\\||\2\u0280\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2"+
		"\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3"+
		"\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2"+
		"\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2"+
		"Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3"+
		"\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\3\u009e\3\2\2\2\5\u00a4"+
		"\3\2\2\2\7\u00a8\3\2\2\2\t\u00af\3\2\2\2\13\u00b5\3\2\2\2\r\u00bc\3\2"+
		"\2\2\17\u00c1\3\2\2\2\21\u00c6\3\2\2\2\23\u00cc\3\2\2\2\25\u00d1\3\2\2"+
		"\2\27\u00d7\3\2\2\2\31\u00dd\3\2\2\2\33\u00e6\3\2\2\2\35\u00f0\3\2\2\2"+
		"\37\u00f5\3\2\2\2!\u00fa\3\2\2\2#\u0103\3\2\2\2%\u0117\3\2\2\2\'\u0126"+
		"\3\2\2\2)\u0135\3\2\2\2+\u0149\3\2\2\2-\u0157\3\2\2\2/\u015f\3\2\2\2\61"+
		"\u0166\3\2\2\2\63\u016e\3\2\2\2\65\u0176\3\2\2\2\67\u0184\3\2\2\29\u019b"+
		"\3\2\2\2;\u01a3\3\2\2\2=\u01a7\3\2\2\2?\u01ac\3\2\2\2A\u01b3\3\2\2\2C"+
		"\u01b8\3\2\2\2E\u01bd\3\2\2\2G\u01c0\3\2\2\2I\u01c5\3\2\2\2K\u01ce\3\2"+
		"\2\2M\u01d4\3\2\2\2O\u01e4\3\2\2\2Q\u01e9\3\2\2\2S\u01ee\3\2\2\2U\u01f9"+
		"\3\2\2\2W\u0200\3\2\2\2Y\u0208\3\2\2\2[\u020e\3\2\2\2]\u0218\3\2\2\2_"+
		"\u0225\3\2\2\2a\u022d\3\2\2\2c\u0234\3\2\2\2e\u023a\3\2\2\2g\u0265\3\2"+
		"\2\2i\u0267\3\2\2\2k\u0269\3\2\2\2m\u026b\3\2\2\2o\u026d\3\2\2\2q\u026f"+
		"\3\2\2\2s\u0271\3\2\2\2u\u0273\3\2\2\2w\u0275\3\2\2\2y\u0277\3\2\2\2{"+
		"\u0279\3\2\2\2}\u027b\3\2\2\2\177\u027d\3\2\2\2\u0081\u027f\3\2\2\2\u0083"+
		"\u0281\3\2\2\2\u0085\u0283\3\2\2\2\u0087\u0285\3\2\2\2\u0089\u0287\3\2"+
		"\2\2\u008b\u0289\3\2\2\2\u008d\u028b\3\2\2\2\u008f\u028d\3\2\2\2\u0091"+
		"\u028f\3\2\2\2\u0093\u0291\3\2\2\2\u0095\u0293\3\2\2\2\u0097\u0295\3\2"+
		"\2\2\u0099\u0297\3\2\2\2\u009b\u0299\3\2\2\2\u009d\u009f\t\2\2\2\u009e"+
		"\u009d\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2"+
		"\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a3\b\2\2\2\u00a3\4\3\2\2\2\u00a4\u00a5"+
		"\5g\64\2\u00a5\u00a6\5m\67\2\u00a6\u00a7\5m\67\2\u00a7\6\3\2\2\2\u00a8"+
		"\u00a9\5k\66\2\u00a9\u00aa\5\u0089E\2\u00aa\u00ab\5o8\2\u00ab\u00ac\5"+
		"g\64\2\u00ac\u00ad\5\u008dG\2\u00ad\u00ae\5o8\2\u00ae\b\3\2\2\2\u00af"+
		"\u00b0\5g\64\2\u00b0\u00b1\5}?\2\u00b1\u00b2\5\u008dG\2\u00b2\u00b3\5"+
		"o8\2\u00b3\u00b4\5\u0089E\2\u00b4\n\3\2\2\2\u00b5\u00b6\5\177@\2\u00b6"+
		"\u00b7\5\u0083B\2\u00b7\u00b8\5m\67\2\u00b8\u00b9\5w<\2\u00b9\u00ba\5"+
		"q9\2\u00ba\u00bb\5\u0097L\2\u00bb\f\3\2\2\2\u00bc\u00bd\5m\67\2\u00bd"+
		"\u00be\5\u0089E\2\u00be\u00bf\5\u0083B\2\u00bf\u00c0\5\u0085C\2\u00c0"+
		"\16\3\2\2\2\u00c1\u00c2\5\u008bF\2\u00c2\u00c3\5u;\2\u00c3\u00c4\5\u0083"+
		"B\2\u00c4\u00c5\5\u0093J\2\u00c5\20\3\2\2\2\u00c6\u00c7\5\u008bF\2\u00c7"+
		"\u00c8\5\u008dG\2\u00c8\u00c9\5g\64\2\u00c9\u00ca\5\u0089E\2\u00ca\u00cb"+
		"\5\u008dG\2\u00cb\22\3\2\2\2\u00cc\u00cd\5\u008bF\2\u00cd\u00ce\5\u008d"+
		"G\2\u00ce\u00cf\5\u0083B\2\u00cf\u00d0\5\u0085C\2\u00d0\24\3\2\2\2\u00d1"+
		"\u00d2\5\u0089E\2\u00d2\u00d3\5o8\2\u00d3\u00d4\5\u008bF\2\u00d4\u00d5"+
		"\5o8\2\u00d5\u00d6\5\u008dG\2\u00d6\26\3\2\2\2\u00d7\u00d8\5k\66\2\u00d8"+
		"\u00d9\5u;\2\u00d9\u00da\5o8\2\u00da\u00db\5k\66\2\u00db\u00dc\5{>\2\u00dc"+
		"\30\3\2\2\2\u00dd\u00de\5\u0089E\2\u00de\u00df\5o8\2\u00df\u00e0\5\u008b"+
		"F\2\u00e0\u00e1\5\u0083B\2\u00e1\u00e2\5\u008fH\2\u00e2\u00e3\5\u0089"+
		"E\2\u00e3\u00e4\5k\66\2\u00e4\u00e5\5o8\2\u00e5\32\3\2\2\2\u00e6\u00e7"+
		"\5\u0089E\2\u00e7\u00e8\5o8\2\u00e8\u00e9\5\u008bF\2\u00e9\u00ea\5\u0083"+
		"B\2\u00ea\u00eb\5\u008fH\2\u00eb\u00ec\5\u0089E\2\u00ec\u00ed\5k\66\2"+
		"\u00ed\u00ee\5o8\2\u00ee\u00ef\5\u008bF\2\u00ef\34\3\2\2\2\u00f0\u00f1"+
		"\5\u0089E\2\u00f1\u00f2\5\u008fH\2\u00f2\u00f3\5}?\2\u00f3\u00f4\5o8\2"+
		"\u00f4\36\3\2\2\2\u00f5\u00f6\5q9\2\u00f6\u00f7\5\u0089E\2\u00f7\u00f8"+
		"\5\u0083B\2\u00f8\u00f9\5\177@\2\u00f9 \3\2\2\2\u00fa\u00fb\5\u008bF\2"+
		"\u00fb\u00fc\5u;\2\u00fc\u00fd\5g\64\2\u00fd\u00fe\5\u0089E\2\u00fe\u00ff"+
		"\5m\67\2\u00ff\u0100\5w<\2\u0100\u0101\5\u0081A\2\u0101\u0102\5s:\2\u0102"+
		"\"\3\2\2\2\u0103\u0104\5\u0089E\2\u0104\u0105\5o8\2\u0105\u0106\5g\64"+
		"\2\u0106\u0107\5m\67\2\u0107\u0108\5\u0093J\2\u0108\u0109\5\u0089E\2\u0109"+
		"\u010a\5w<\2\u010a\u010b\5\u008dG\2\u010b\u010c\5o8\2\u010c\u010d\5\u009b"+
		"N\2\u010d\u010e\5\u008bF\2\u010e\u010f\5\u0085C\2\u010f\u0110\5}?\2\u0110"+
		"\u0111\5w<\2\u0111\u0112\5\u008dG\2\u0112\u0113\5\u008dG\2\u0113\u0114"+
		"\5w<\2\u0114\u0115\5\u0081A\2\u0115\u0116\5s:\2\u0116$\3\2\2\2\u0117\u0118"+
		"\5\u0093J\2\u0118\u0119\5\u0089E\2\u0119\u011a\5w<\2\u011a\u011b\5\u008d"+
		"G\2\u011b\u011c\5o8\2\u011c\u011d\5\u009bN\2\u011d\u011e\5\u0089E\2\u011e"+
		"\u011f\5o8\2\u011f\u0120\5\u008bF\2\u0120\u0121\5\u0083B\2\u0121\u0122"+
		"\5\u008fH\2\u0122\u0123\5\u0089E\2\u0123\u0124\5k\66\2\u0124\u0125\5o"+
		"8\2\u0125&\3\2\2\2\u0126\u0127\5\u0089E\2\u0127\u0128\5o8\2\u0128\u0129"+
		"\5g\64\2\u0129\u012a\5m\67\2\u012a\u012b\5\u009bN\2\u012b\u012c\5\u0089"+
		"E\2\u012c\u012d\5o8\2\u012d\u012e\5\u008bF\2\u012e\u012f\5\u0083B\2\u012f"+
		"\u0130\5\u008fH\2\u0130\u0131\5\u0089E\2\u0131\u0132\5k\66\2\u0132\u0133"+
		"\5o8\2\u0133\u0134\5\u008bF\2\u0134(\3\2\2\2\u0135\u0136\5g\64\2\u0136"+
		"\u0137\5\u008fH\2\u0137\u0138\5\u008dG\2\u0138\u0139\5\u0083B\2\u0139"+
		"\u013a\5\u009bN\2\u013a\u013b\5g\64\2\u013b\u013c\5\u0093J\2\u013c\u013d"+
		"\5g\64\2\u013d\u013e\5\u0089E\2\u013e\u013f\5o8\2\u013f\u0140\5\u009b"+
		"N\2\u0140\u0141\5\u0089E\2\u0141\u0142\5o8\2\u0142\u0143\5\u008bF\2\u0143"+
		"\u0144\5\u0083B\2\u0144\u0145\5\u008fH\2\u0145\u0146\5\u0089E\2\u0146"+
		"\u0147\5k\66\2\u0147\u0148\5o8\2\u0148*\3\2\2\2\u0149\u014a\5\u0089E\2"+
		"\u014a\u014b\5o8\2\u014b\u014c\5\u0085C\2\u014c\u014d\5}?\2\u014d\u014e"+
		"\5w<\2\u014e\u014f\5k\66\2\u014f\u0150\5g\64\2\u0150\u0151\5\u009bN\2"+
		"\u0151\u0152\5\u0087D\2\u0152\u0153\5\u008fH\2\u0153\u0154\5o8\2\u0154"+
		"\u0155\5\u0089E\2\u0155\u0156\5\u0097L\2\u0156,\3\2\2\2\u0157\u0158\5"+
		"o8\2\u0158\u0159\5\u0081A\2\u0159\u015a\5k\66\2\u015a\u015b\5\u0089E\2"+
		"\u015b\u015c\5\u0097L\2\u015c\u015d\5\u0085C\2\u015d\u015e\5\u008dG\2"+
		"\u015e.\3\2\2\2\u015f\u0160\5\u008bF\2\u0160\u0161\5u;\2\u0161\u0162\5"+
		"g\64\2\u0162\u0163\5m\67\2\u0163\u0164\5\u0083B\2\u0164\u0165\5\u0093"+
		"J\2\u0165\60\3\2\2\2\u0166\u0167\5\u0085C\2\u0167\u0168\5\u0089E\2\u0168"+
		"\u0169\5w<\2\u0169\u016a\5\177@\2\u016a\u016b\5g\64\2\u016b\u016c\5\u0089"+
		"E\2\u016c\u016d\5\u0097L\2\u016d\62\3\2\2\2\u016e\u016f\5\u0089E\2\u016f"+
		"\u0170\5o8\2\u0170\u0171\5\u0085C\2\u0171\u0172\5}?\2\u0172\u0173\5w<"+
		"\2\u0173\u0174\5k\66\2\u0174\u0175\5g\64\2\u0175\64\3\2\2\2\u0176\u0177"+
		"\5s:\2\u0177\u0178\5o8\2\u0178\u0179\5\u0081A\2\u0179\u017a\5o8\2\u017a"+
		"\u017b\5\u0089E\2\u017b\u017c\5g\64\2\u017c\u017d\5\u008dG\2\u017d\u017e"+
		"\5o8\2\u017e\u017f\5m\67\2\u017f\u0180\5\u009bN\2\u0180\u0181\5{>\2\u0181"+
		"\u0182\5o8\2\u0182\u0183\5\u0097L\2\u0183\66\3\2\2\2\u0184\u0185\5m\67"+
		"\2\u0185\u0186\5o8\2\u0186\u0187\5q9\2\u0187\u0188\5g\64\2\u0188\u0189"+
		"\5\u008fH\2\u0189\u018a\5}?\2\u018a\u018b\5\u008dG\2\u018b\u018c\5\u009b"+
		"N\2\u018c\u018d\5\u008dG\2\u018d\u018e\5g\64\2\u018e\u018f\5i\65\2\u018f"+
		"\u0190\5}?\2\u0190\u0191\5o8\2\u0191\u0192\5\u009bN\2\u0192\u0193\5\u008b"+
		"F\2\u0193\u0194\5\u008dG\2\u0194\u0195\5\u0089E\2\u0195\u0196\5g\64\2"+
		"\u0196\u0197\5\u008dG\2\u0197\u0198\5o8\2\u0198\u0199\5s:\2\u0199\u019a"+
		"\5\u0097L\2\u019a8\3\2\2\2\u019b\u019c\5\u008bF\2\u019c\u019d\5k\66\2"+
		"\u019d\u019e\5g\64\2\u019e\u019f\5}?\2\u019f\u01a0\5w<\2\u01a0\u01a1\5"+
		"\u0081A\2\u01a1\u01a2\5s:\2\u01a2:\3\2\2\2\u01a3\u01a4\5y=\2\u01a4\u01a5"+
		"\5\u0083B\2\u01a5\u01a6\5i\65\2\u01a6<\3\2\2\2\u01a7\u01a8\5}?\2\u01a8"+
		"\u01a9\5w<\2\u01a9\u01aa\5\u008bF\2\u01aa\u01ab\5\u008dG\2\u01ab>\3\2"+
		"\2\2\u01ac\u01ad\5\u008bF\2\u01ad\u01ae\5\u008dG\2\u01ae\u01af\5g\64\2"+
		"\u01af\u01b0\5\u008dG\2\u01b0\u01b1\5\u008fH\2\u01b1\u01b2\5\u008bF\2"+
		"\u01b2@\3\2\2\2\u01b3\u01b4\5u;\2\u01b4\u01b5\5\u0083B\2\u01b5\u01b6\5"+
		"\u008bF\2\u01b6\u01b7\5\u008dG\2\u01b7B\3\2\2\2\u01b8\u01b9\5\u0085C\2"+
		"\u01b9\u01ba\5\u0083B\2\u01ba\u01bb\5\u0089E\2\u01bb\u01bc\5\u008dG\2"+
		"\u01bcD\3\2\2\2\u01bd\u01be\5m\67\2\u01be\u01bf\5i\65\2\u01bfF\3\2\2\2"+
		"\u01c0\u01c1\5\u008fH\2\u01c1\u01c2\5\u008bF\2\u01c2\u01c3\5o8\2\u01c3"+
		"\u01c4\5\u0089E\2\u01c4H\3\2\2\2\u01c5\u01c6\5\u0085C\2\u01c6\u01c7\5"+
		"g\64\2\u01c7\u01c8\5\u008bF\2\u01c8\u01c9\5\u008bF\2\u01c9\u01ca\5\u0093"+
		"J\2\u01ca\u01cb\5\u0083B\2\u01cb\u01cc\5\u0089E\2\u01cc\u01cd\5m\67\2"+
		"\u01cdJ\3\2\2\2\u01ce\u01cf\5\u008dG\2\u01cf\u01d0\5g\64\2\u01d0\u01d1"+
		"\5i\65\2\u01d1\u01d2\5}?\2\u01d2\u01d3\5o8\2\u01d3L\3\2\2\2\u01d4\u01d5"+
		"\5\u008bF\2\u01d5\u01d6\5u;\2\u01d6\u01d7\5g\64\2\u01d7\u01d8\5\u0089"+
		"E\2\u01d8\u01d9\5m\67\2\u01d9\u01da\5w<\2\u01da\u01db\5\u0081A\2\u01db"+
		"\u01dc\5s:\2\u01dc\u01dd\5\u009bN\2\u01dd\u01de\5k\66\2\u01de\u01df\5"+
		"\u0083B\2\u01df\u01e0\5}?\2\u01e0\u01e1\5\u008fH\2\u01e1\u01e2\5\177@"+
		"\2\u01e2\u01e3\5\u0081A\2\u01e3N\3\2\2\2\u01e4\u01e5\5\u008dG\2\u01e5"+
		"\u01e6\5\u0097L\2\u01e6\u01e7\5\u0085C\2\u01e7\u01e8\5o8\2\u01e8P\3\2"+
		"\2\2\u01e9\u01ea\5\u0081A\2\u01ea\u01eb\5g\64\2\u01eb\u01ec\5\177@\2\u01ec"+
		"\u01ed\5o8\2\u01edR\3\2\2\2\u01ee\u01ef\5\u0085C\2\u01ef\u01f0\5\u0089"+
		"E\2\u01f0\u01f1\5\u0083B\2\u01f1\u01f2\5\u0085C\2\u01f2\u01f3\5o8\2\u01f3"+
		"\u01f4\5\u0089E\2\u01f4\u01f5\5\u008dG\2\u01f5\u01f6\5w<\2\u01f6\u01f7"+
		"\5o8\2\u01f7\u01f8\5\u008bF\2\u01f8T\3\2\2\2\u01f9\u01fa\5k\66\2\u01fa"+
		"\u01fb\5\u0083B\2\u01fb\u01fc\5}?\2\u01fc\u01fd\5\u008fH\2\u01fd\u01fe"+
		"\5\177@\2\u01fe\u01ff\5\u0081A\2\u01ffV\3\2\2\2\u0200\u0201\5i\65\2\u0201"+
		"\u0202\5w<\2\u0202\u0203\5\u0081A\2\u0203\u0204\5m\67\2\u0204\u0205\5"+
		"w<\2\u0205\u0206\5\u0081A\2\u0206\u0207\5s:\2\u0207X\3\2\2\2\u0208\u0209"+
		"\5\u0089E\2\u0209\u020a\5\u008fH\2\u020a\u020b\5}?\2\u020b\u020c\5o8\2"+
		"\u020c\u020d\5\u008bF\2\u020dZ\3\2\2\2\u020e\u020f\5i\65\2\u020f\u0210"+
		"\5\u0089E\2\u0210\u0211\5\u0083B\2\u0211\u0212\5g\64\2\u0212\u0213\5m"+
		"\67\2\u0213\u0214\5k\66\2\u0214\u0215\5g\64\2\u0215\u0216\5\u008bF\2\u0216"+
		"\u0217\5\u008dG\2\u0217\\\3\2\2\2\u0218\u0219\5m\67\2\u0219\u021a\5i\65"+
		"\2\u021a\u021b\5\u009bN\2\u021b\u021c\5m\67\2\u021c\u021d\5w<\2\u021d"+
		"\u021e\5\u008bF\2\u021e\u021f\5k\66\2\u021f\u0220\5\u0083B\2\u0220\u0221"+
		"\5\u0091I\2\u0221\u0222\5o8\2\u0222\u0223\5\u0089E\2\u0223\u0224\5\u0097"+
		"L\2\u0224^\3\2\2\2\u0225\u0226\5k\66\2\u0226\u0227\5\u0083B\2\u0227\u0228"+
		"\5}?\2\u0228\u0229\5\u008fH\2\u0229\u022a\5\177@\2\u022a\u022b\5\u0081"+
		"A\2\u022b\u022c\5\u008bF\2\u022c`\3\2\2\2\u022d\u022e\5k\66\2\u022e\u022f"+
		"\5w<\2\u022f\u0230\5\u0085C\2\u0230\u0231\5u;\2\u0231\u0232\5o8\2\u0232"+
		"\u0233\5\u0089E\2\u0233b\3\2\2\2\u0234\u0235\5\u0085C\2\u0235\u0236\5"+
		"}?\2\u0236\u0237\5g\64\2\u0237\u0238\5w<\2\u0238\u0239\5\u0081A\2\u0239"+
		"d\3\2\2\2\u023a\u023b\7F\2\2\u023b\u023c\7Q\2\2\u023c\u023d\7\"\2\2\u023d"+
		"\u023e\7P\2\2\u023e\u023f\7Q\2\2\u023f\u0240\7V\2\2\u0240\u0241\7\"\2"+
		"\2\u0241\u0242\7O\2\2\u0242\u0243\7C\2\2\u0243\u0244\7V\2\2\u0244\u0245"+
		"\7E\2\2\u0245\u0246\7J\2\2\u0246\u0247\7\"\2\2\u0247\u0248\7C\2\2\u0248"+
		"\u0249\7P\2\2\u0249\u024a\7[\2\2\u024a\u024b\7\"\2\2\u024b\u024c\7V\2"+
		"\2\u024c\u024d\7J\2\2\u024d\u024e\7K\2\2\u024e\u024f\7P\2\2\u024f\u0250"+
		"\7I\2\2\u0250\u0251\7.\2\2\u0251\u0252\7\"\2\2\u0252\u0253\7L\2\2\u0253"+
		"\u0254\7W\2\2\u0254\u0255\7U\2\2\u0255\u0256\7V\2\2\u0256\u0257\7\"\2"+
		"\2\u0257\u0258\7H\2\2\u0258\u0259\7Q\2\2\u0259\u025a\7T\2\2\u025a\u025b"+
		"\7\"\2\2\u025b\u025c\7I\2\2\u025c\u025d\7G\2\2\u025d\u025e\7P\2\2\u025e"+
		"\u025f\7G\2\2\u025f\u0260\7T\2\2\u0260\u0261\7C\2\2\u0261\u0262\7V\2\2"+
		"\u0262\u0263\7Q\2\2\u0263\u0264\7T\2\2\u0264f\3\2\2\2\u0265\u0266\t\3"+
		"\2\2\u0266h\3\2\2\2\u0267\u0268\t\4\2\2\u0268j\3\2\2\2\u0269\u026a\t\5"+
		"\2\2\u026al\3\2\2\2\u026b\u026c\t\6\2\2\u026cn\3\2\2\2\u026d\u026e\t\7"+
		"\2\2\u026ep\3\2\2\2\u026f\u0270\t\b\2\2\u0270r\3\2\2\2\u0271\u0272\t\t"+
		"\2\2\u0272t\3\2\2\2\u0273\u0274\t\n\2\2\u0274v\3\2\2\2\u0275\u0276\t\13"+
		"\2\2\u0276x\3\2\2\2\u0277\u0278\t\f\2\2\u0278z\3\2\2\2\u0279\u027a\t\r"+
		"\2\2\u027a|\3\2\2\2\u027b\u027c\t\16\2\2\u027c~\3\2\2\2\u027d\u027e\t"+
		"\17\2\2\u027e\u0080\3\2\2\2\u027f\u0280\t\20\2\2\u0280\u0082\3\2\2\2\u0281"+
		"\u0282\t\21\2\2\u0282\u0084\3\2\2\2\u0283\u0284\t\22\2\2\u0284\u0086\3"+
		"\2\2\2\u0285\u0286\t\23\2\2\u0286\u0088\3\2\2\2\u0287\u0288\t\24\2\2\u0288"+
		"\u008a\3\2\2\2\u0289\u028a\t\25\2\2\u028a\u008c\3\2\2\2\u028b\u028c\t"+
		"\26\2\2\u028c\u008e\3\2\2\2\u028d\u028e\t\27\2\2\u028e\u0090\3\2\2\2\u028f"+
		"\u0290\t\30\2\2\u0290\u0092\3\2\2\2\u0291\u0292\t\31\2\2\u0292\u0094\3"+
		"\2\2\2\u0293\u0294\t\32\2\2\u0294\u0096\3\2\2\2\u0295\u0296\t\33\2\2\u0296"+
		"\u0098\3\2\2\2\u0297\u0298\t\34\2\2\u0298\u009a\3\2\2\2\u0299\u029a\7"+
		"a\2\2\u029a\u009c\3\2\2\2\4\2\u00a0\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}