package io.shardingsphere.core.parsing.antler.parser.mysql;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.AdvancedErrorStrategy;
import io.shardingsphere.core.parsing.antler.AdvancedParserATNSimulator;
import io.shardingsphere.parser.antlr.mysql.MySQLTruncateTableParser;

public class MySQLAdvancedTruncateTableParser extends MySQLTruncateTableParser {

    public MySQLAdvancedTruncateTableParser(TokenStream input) {
        super(input);
        _interp = new AdvancedParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache, ID);
        this._errHandler = new AdvancedErrorStrategy(ID);
    }

    public Token match(int ttype) throws RecognitionException {
        Token t = getCurrentToken();

        boolean compatID = false;
        if (ID == ttype && ID > t.getType()) {
            compatID = true;
        }

        if (t.getType() == ttype || compatID) {
            if (ttype == Token.EOF) {
                matchedEOF = true;
            }

            if (!matchedEOF && compatID && (t instanceof CommonToken)) {
                CommonToken commonToken = (CommonToken) t;
                commonToken.setType(ID);
            }
            _errHandler.reportMatch(this);
            consume();
        } else {
            t = _errHandler.recoverInline(this);
            if (_buildParseTrees && t.getTokenIndex() == -1) {
                // we must have conjured up a new token during single token insertion
                // if it's not the current symbol
                _ctx.addErrorNode(createErrorNode(_ctx, t));
            }
        }
        return t;
    }
}
