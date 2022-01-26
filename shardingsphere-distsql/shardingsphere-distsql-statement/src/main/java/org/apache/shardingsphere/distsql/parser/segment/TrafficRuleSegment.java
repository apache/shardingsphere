package org.apache.shardingsphere.distsql.parser.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;

import java.util.Collection;

/**
 * Traffic rule segment.
 */
@RequiredArgsConstructor
@Getter
public final class TrafficRuleSegment implements ASTNode {
    
    private final String name;
    
    private final Collection<String> labels;
    
    private final AlgorithmSegment algorithm;
    
    private final AlgorithmSegment loadBalancer;
}
