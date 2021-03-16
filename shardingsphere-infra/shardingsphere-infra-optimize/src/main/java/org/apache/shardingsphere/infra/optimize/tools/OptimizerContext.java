package org.apache.shardingsphere.infra.optimize.tools;

import lombok.Getter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * optimizer context
 */
@Getter
public class OptimizerContext {
    
    private static ThreadLocal<OptimizerContext> CONTEXT = ThreadLocal.withInitial(() -> null);
    
    private ShardingRule shardingRule;
    
    private OptimizerContext(ShardingRule shardingRule) {
        this.shardingRule = shardingRule;
    }
    
    public static OptimizerContext create(ShardingRule shardingRule) {
        OptimizerContext optimizerContext = new OptimizerContext(shardingRule);
        CONTEXT.set(optimizerContext);
        return optimizerContext;
    }
    
    public static Optional<OptimizerContext> getCurrentOptimizerContext() {
        if(CONTEXT.get() == null) {
            return Optional.empty();
        }
        return Optional.of(CONTEXT.get());
    }
}
