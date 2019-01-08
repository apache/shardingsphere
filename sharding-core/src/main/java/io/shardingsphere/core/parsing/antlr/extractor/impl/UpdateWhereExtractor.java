
package io.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;

public class UpdateWhereExtractor extends AbstractFromWhereExtractor{
    
    @Override
    protected Optional<ParserRuleContext> extractTable(FromWhereSegment fromWhereSegment, ParserRuleContext ancestorNode, Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        Optional<ParserRuleContext> tableReferenceNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCE);
        this.extractTableRefrence(fromWhereSegment, tableReferenceNode.get(), questionNodeIndexMap);
        return ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
    }
}
