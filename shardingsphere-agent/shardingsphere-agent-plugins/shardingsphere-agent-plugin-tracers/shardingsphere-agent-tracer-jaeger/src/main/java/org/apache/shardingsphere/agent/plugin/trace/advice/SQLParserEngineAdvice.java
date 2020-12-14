package org.apache.shardingsphere.agent.plugin.trace.advice;

import io.opentracing.Scope;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.plugin.trace.ShardingErrorSpan;
import org.apache.shardingsphere.agent.plugin.trace.constant.ShardingTags;

import java.lang.reflect.Method;

/**
 * SQL parser engine advice.
 */
public class SQLParserEngineAdvice implements MethodAroundAdvice {
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/parseSQL/";
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Scope scope = GlobalTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.DB_STATEMENT.getKey(), String.valueOf(args[0]))
                .startActive(true);
        target.setAttachment(scope);
    }
    
    @Override
    public void afterMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        ((Scope) target.getAttachment()).close();
    }
    
    @Override
    public void onThrowing(final TargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ShardingErrorSpan.setError(GlobalTracer.get().activeSpan(), throwable);
    }
}
