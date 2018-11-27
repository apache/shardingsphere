package io.shardingsphere.core.parsing.antlr.extractor.segment.engine.dialect.postgresql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ShowParamSegment;
import org.antlr.v4.runtime.ParserRuleContext;

public class ShowParamExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<? extends SQLSegment> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> showParamNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.SHOW_PARAM);
        if (!showParamNode.isPresent()) {
            return Optional.absent();
        }
        String result = showParamNode.get().getText();
        return Optional.of(new ShowParamSegment(result));
    }
}
