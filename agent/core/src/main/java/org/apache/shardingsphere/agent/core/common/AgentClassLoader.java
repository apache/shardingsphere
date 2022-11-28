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

package org.apache.shardingsphere.agent.core.common;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.apache.shardingsphere.agent.core.plugin.AgentPluginLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 *  Agent classloader.
 */
public final class AgentClassLoader extends ClassLoader {
    
    static {
        registerAsParallelCapable();
    }
    
    @Getter
    private static volatile AgentClassLoader defaultPluginClassloader;
    
    private final Collection<PluginJar> pluginJars;
    
    public AgentClassLoader(final ClassLoader classLoader, final Collection<PluginJar> pluginJars) {
        super(classLoader);
        this.pluginJars = pluginJars;
    }
    
    /**
     * Init default plugin classloader.
     * 
     * @param pluginJars plugin jars
     */
    public static void initDefaultPluginClassLoader(final Collection<PluginJar> pluginJars) {
        if (null == defaultPluginClassloader) {
            synchronized (AgentClassLoader.class) {
                if (null == defaultPluginClassloader) {
                    defaultPluginClassloader = new AgentClassLoader(AgentPluginLoader.class.getClassLoader(), pluginJars);
                }
            }
        }
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        String path = classNameToPath(name);
        for (PluginJar each : pluginJars) {
            ZipEntry entry = each.getJarFile().getEntry(path);
            if (Objects.isNull(entry)) {
                continue;
            }
            try {
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    String packageName = name.substring(0, index);
                    definePackageInternal(packageName, each.getJarFile().getManifest());
                }
                byte[] data = ByteStreams.toByteArray(each.getJarFile().getInputStream(entry));
                return defineClass(name, data, 0, data.length);
            } catch (final IOException ex) {
                throw new ClassNotFoundException(String.format("Class name is %s not found", name));
            }
        }
        throw new ClassNotFoundException(String.format("Class name is %s not found", name));
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
    
    @Override
    protected Enumeration<URL> findResources(final String name) {
        Collection<URL> resources = new LinkedList<>();
        for (PluginJar each : pluginJars) {
            JarEntry entry = each.getJarFile().getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    resources.add(new URL(String.format("jar:file:%s!/%s", each.getSourcePath().getAbsolutePath(), name)));
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return Collections.enumeration(resources);
    }
    
    @Override
    protected URL findResource(final String name) {
        for (PluginJar each : pluginJars) {
            JarEntry entry = each.getJarFile().getJarEntry(name);
            if (Objects.nonNull(entry)) {
                try {
                    return new URL(String.format("jar:file:%s!/%s", each.getSourcePath().getAbsolutePath(), name));
                } catch (final MalformedURLException ignored) {
                }
            }
        }
        return null;
    }
}
