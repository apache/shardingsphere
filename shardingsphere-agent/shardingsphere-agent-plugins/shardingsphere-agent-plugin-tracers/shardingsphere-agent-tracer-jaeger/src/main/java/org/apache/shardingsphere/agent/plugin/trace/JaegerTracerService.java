package org.apache.shardingsphere.agent.plugin.trace;

import io.jaegertracing.Configuration;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.core.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.Service;
import org.apache.shardingsphere.agent.core.utils.SingletonHolder;

/**
 * Jaeger tracer service.
 */
public class JaegerTracerService implements Service {
    
    @Override
    public void setup() {
        AgentConfiguration configuration = SingletonHolder.INSTANCE.get(AgentConfiguration.class);
        AgentConfiguration.TracingConfiguration tracingConfiguration = configuration.getTracing();
        tracingConfiguration.getExtra().forEach(System::setProperty);
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv();
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                .withSender(
                        Configuration.SenderConfiguration.fromEnv()
                                .withAgentHost(tracingConfiguration.getAgentHost())
                                .withAgentPort(tracingConfiguration.getAgentPort())
                );
        Configuration config = new Configuration(configuration.getApplicationName())
                .withSampler(samplerConfig)
                .withReporter(reporterConfig);
        GlobalTracer.register(config.getTracer());
    }
    
    @Override
    public void start() {
    
    }
    
    @Override
    public void cleanup() {
    
    }
}
