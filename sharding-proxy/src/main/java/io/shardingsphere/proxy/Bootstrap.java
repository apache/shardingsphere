/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusInstance;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.frontend.ShardingProxy;
import io.shardingsphere.proxy.yaml.YamlProxyConfiguration;
import io.shardingsphere.transaction.xa.XaTransactionListener;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

/**
 * Sharding-Proxy Bootstrap.
 *
 * @author zhangliang
 * @author wangkai
 * @author panjuan
 */
public final class Bootstrap {
    
    private static final int DEFAULT_PORT = 3307;
    
    private static final String CONFIG_YAML = "/conf/config.yaml";
    
    /**
     * Main Entrance.
     * 
     * @param args startup arguments
     * @throws InterruptedException interrupted exception
     * @throws IOException IO exception
     */
    public static void main(final String[] args) throws InterruptedException, IOException {
        YamlProxyConfiguration localConfig = loadLocalConfiguration(new File(Bootstrap.class.getResource(CONFIG_YAML).getFile()));
        int port = getPort(args);
        if (null == localConfig.getOrchestration()) {
            startWithoutRegistryCenter(localConfig, port);
        } else {
            startWithRegistryCenter(localConfig, port);
        }
    }
    
    private static YamlProxyConfiguration loadLocalConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyConfiguration result = new Yaml(new Constructor(YamlProxyConfiguration.class)).loadAs(inputStreamReader, YamlProxyConfiguration.class);
            Preconditions.checkNotNull(result, String.format("Configuration file `%s` is invalid.", CONFIG_YAML));
            return result;
        }
    }
    
    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (final NumberFormatException ex) {
            return DEFAULT_PORT;
        }
    }
    
    private static void startWithoutRegistryCenter(final YamlProxyConfiguration config, final int port) throws InterruptedException, MalformedURLException {
        RuleRegistry.getInstance().init(config);
        EventBusInstance.getInstance().register(new XaTransactionListener());
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(final YamlProxyConfiguration localConfig, final int port) throws InterruptedException, MalformedURLException {
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(localConfig.getOrchestration().getOrchestrationConfiguration())) {
            YamlProxyConfiguration config = getYamlProxyConfiguration(localConfig, orchestrationFacade);
            orchestrationFacade.init(config);
            RuleRegistry.getInstance().init(config);
            ProxyEventBusInstance.getInstance().register(new YamlProxyConfiguration());
            EventBusInstance.getInstance().register(new XaTransactionListener());
            new ShardingProxy().start(port);
        }
    }
    
    private static YamlProxyConfiguration getYamlProxyConfiguration(final YamlProxyConfiguration localConfig, final OrchestrationFacade orchestrationFacade) {
        return localConfig.isEmptyLocalConfiguration()
                        ? new YamlProxyConfiguration(orchestrationFacade.getConfigService().loadDataSources(), orchestrationFacade.getConfigService().loadProxyConfiguration()) : localConfig;
    }
}
