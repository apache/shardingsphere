package org.apache.shardingsphere.infra.optimize.tools;

import lombok.Getter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * optimizer context.
 */
@Getter
public final class OptimizerContext {
    
    private static final ThreadLocal<OptimizerContext> CONTEXT = ThreadLocal.withInitial(() -> null);
    
    private ShardingRule shardingRule;
    
    private OptimizerContext(final ShardingRule shardingRule) {
        this.shardingRule = shardingRule;
    }
    
    /**
     * Create optimizer context.
     * @param shardingRule sharding rule
     * @return <code>OptimizerContext</code>
     */
    public static OptimizerContext create(final ShardingRule shardingRule) {
        OptimizerContext optimizerContext = new OptimizerContext(shardingRule);
        CONTEXT.set(optimizerContext);
        return optimizerContext;
    }
    
    /**
     * Get current <code>OptimizerContext</code> of this <code>ThreadLocal</code>.
     * @return <code>OptimizerContext</code> 
     */
    public static Optional<OptimizerContext> getCurrentOptimizerContext() {
        if (CONTEXT.get() == null) {
            return Optional.empty();
        }
        return Optional.of(CONTEXT.get());
    }
}
