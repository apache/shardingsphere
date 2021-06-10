/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.core.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.config.path.AgentPathBuilder;
import org.apache.shardingsphere.agent.core.config.registry.AgentConfigurationRegistry;
import org.apache.shardingsphere.agent.core.spi.PluginServiceLoader;
import org.apache.shardingsphere.agent.spi.definition.AbstractPluginDefinitionService;
import org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Plugin loader.
 */
@Slf4j
public final class PluginLoader extends ClassLoader implements Closeable {
    
    static {
        registerAsParallelCapable();
    }
    
    private static volatile PluginLoader pluginLoader;
    
    private final ConcurrentHashMap<String, Object> objectPool = new ConcurrentHashMap<>();
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private final List<PluginJar> jars = Lists.newArrayList();
    
    private Map<String, PluginInterceptorPoint> interceptorPointMap;
    
    private PluginLoader() {
        super(PluginLoader.class.getClassLoader());
    }
    
    /**
     * Get plugin loader instance.
     *
     * @return plugin loader instance
     */
    public static PluginLoader getInstance() {
        if (null == pluginLoader) {
            synchronized (PluginLoader.class) {
                if (null == pluginLoader) {
                    pluginLoader = new PluginLoader();
                }
            }
        }
        return pluginLoader;
    }
    
    /**
     * Load all plugins.
     *
     * @throws IOException IO exception
     */
    public void loadAllPlugins() throws IOException {
        File[] jarFiles = AgentPathBuilder.getPluginPath().listFiles(file -> file.getName().endsWith(".jar"));
        if (null == jarFiles) {
            return;
        }
        Map<String, PluginInterceptorPoint> pointMap = Maps.newHashMap();
        Set<String> ignoredPluginNames = AgentConfigurationRegistry.INSTANCE.get(AgentConfiguration.class).getIgnoredPluginNames();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (File each : jarFiles) {
                outputStream.reset();
                JarFile jar = new JarFile(each, true);
                jars.add(new PluginJar(jar, each));
                log.info("Loaded jar {}.", each.getName());
            }
        }
        loadPluginDefinitionServices(ignoredPluginNames, pointMap);
        interceptorPointMap = ImmutableMap.<String, PluginInterceptorPoint>builder().putAll(pointMap).build();
    }
    
    /**
     * To find all intercepting target classes then to build TypeMatcher.
     *
     * @return type matcher
     */
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return new Junction<TypeDescription>() {
            
            @Override
            public boolean matches(final TypeDescription target) {
                return interceptorPointMap.containsKey(target.getTypeName());
            }
            
            @Override
            public <U extends TypeDescription> Junction<U> and(final ElementMatcher<? super U> other) {
                return null;
            }
            
            @Override
            public <U extends TypeDescription> Junction<U> or(final ElementMatcher<? super U> other) {
                return null;
            }
        };
    }
    
    /**
     * To detect the type whether or not exists.
     *
     * @param typeDescription TypeDescription
     * @return contains when it is true
     */
    public boolean containsType(final TypeDescription typeDescription) {
        return interceptorPointMap.containsKey(typeDescription.getTypeName());
    }
    
    /**
     * Load plugin interceptor point by TypeDescription.
     *
     * @param typeDescription TypeDescription
     * @return plugin interceptor point
     */
    public PluginInterceptorPoint loadPluginInterceptorPoint(final TypeDescription typeDescription) {
        return interceptorPointMap.getOrDefault(typeDescription.getTypeName(), PluginInterceptorPoint.createDefault());
    }
    
    /**
     * To get or create instance of the advice class. Create new one and caching when it is not exist.
     *
     * @param classNameOfAdvice class name of advice
     * @param <T> advice type
     * @return instance of advice
     */
    @SneakyThrows({ClassNotFoundException.class, IllegalAccessException.class, InstantiationException.class})
    @SuppressWarnings("unchecked")
    public <T> T getOrCreateInstance(final String classNameOfAdvice) {
        if (objectPool.containsKey(classNameOfAdvice)) {
            return (T) objectPool.get(classNameOfAdvice);
        }
        lock.lock();
        try {
            Object inst = objectPool.get(classNameOfAdvice);
            if (Objects.isNull(inst)) {
                inst = Class.forName(classNameOfAdvice, true, this).newInstance();
                objectPool.put(classNameOfAdvice, inst);
            }
            return (T) inst;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        String path = classNameToPath(name);
        for (PluginJar each : jars) {
            ZipEntry entry = each.jarFile.getEntry(path);
            if (Objects.nonNull(entry)) {
                try {
                    int index = name.lastIndexOf('.');
                    if (index != -1) {
                        String packageName = name.substring(0, index);
                        definePackageInternal(packageName, each.jarFile.getManifest());
                    }
                    byte[] data = ByteStreams.toByteArray(each.jarFile.getInputStream(entry));
                    return defineClass(name, data, 0, data.length);
                } catch (final IOException ex) {
                    log.error("Failed to load class {}.", name, ex);
                }
            }
        }
        throw new ClassNotFoundException(String.format("Class name is %s not found.", name));
    }
    
    @Override
    protected Enumeration<URL> findResources(final String name) {
        List<URL> resources = Lists.newArrayList();
        for (PluginJar each : jars) {
            JarEntry entry = each.jarFile.getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    resources.add(new URL(String.format("jar:file:%s!/%s", each.sourcePath.getAbsolutePath(), name)));
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return Collections.enumeration(resources);
    }
    
    @Override
    protected URL findResource(final String name) {
        for (PluginJar each : jars) {
            JarEntry entry = each.jarFile.getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    return new URL(String.format("jar:file:%s!/%s", each.sourcePath.getAbsolutePath(), name));
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return null;
    }
    
    @Override
    public void close() {
        for (PluginJar each : jars) {
            try {
                each.jarFile.close();
            } catch (final IOException ex) {
                log.error("close is ", ex);
            }
        }
    }
    
    private void loadPluginDefinitionServices(final Set<String> ignoredPluginNames, final Map<String, PluginInterceptorPoint> pointMap) {
        PluginServiceLoader.newServiceInstances(PluginDefinitionService.class)
                .stream()
                .filter(each -> ignoredPluginNames.isEmpty() || !ignoredPluginNames.contains(each.getType()))
                .forEach(each -> buildPluginInterceptorPointMap(each, pointMap));
    }
    
    private String classNameToPath(final String className) {
        return String.join("", className.replace(".", "/"), ".class");
    }
    
    private void definePackageInternal(final String packageName, final Manifest manifest) {
        if (null != getPackage(packageName)) {
            return;
        }
        Attributes attributes = manifest.getMainAttributes();
        String specTitle = attributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
        String specVersion = attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
        String specVendor = attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
        String implTitle = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        String implVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        String implVendor = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, null);
    }
    
    private void buildPluginInterceptorPointMap(final PluginDefinitionService pluginDefinitionService, final Map<String, PluginInterceptorPoint> pointMap) {
        AbstractPluginDefinitionService definitionService = (AbstractPluginDefinitionService) pluginDefinitionService;
        definitionService.install().forEach(each -> {
            String target = each.getClassNameOfTarget();
            if (pointMap.containsKey(target)) {
                PluginInterceptorPoint pluginInterceptorPoint = pointMap.get(target);
                pluginInterceptorPoint.getConstructorPoints().addAll(each.getConstructorPoints());
                pluginInterceptorPoint.getInstanceMethodPoints().addAll(each.getInstanceMethodPoints());
                pluginInterceptorPoint.getClassStaticMethodPoints().addAll(each.getClassStaticMethodPoints());
            } else {
                pointMap.put(target, each);
            }
        });
    }
    
    @RequiredArgsConstructor
    private static class PluginJar {
        
        private final JarFile jarFile;
        
        private final File sourcePath;
    }
}
