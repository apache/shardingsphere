package org.apache.shardingsphere.test.integration.junit.processor;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;

import java.io.InputStream;

public class AuthenticationProcessor implements Processor<AuthenticationProcessor.Authentication> {
    
    @Override
    @SneakyThrows
    public Authentication process(InputStream stream) {
        YamlProxyServerConfiguration configuration = YamlEngine.unmarshal(ByteStreams.toByteArray(stream), YamlProxyServerConfiguration.class);
        YamlUserConfiguration user = configuration.getAuthentication().getUsers().get("root");
        return new Authentication("root", user.getPassword());
    }
    
    @Getter
    @RequiredArgsConstructor
    public static class Authentication {
        
        private final String user;
        
        private final String password;
    
    }
}
