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

package org.apache.shardingsphere.agent.core.classloader;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Agent extra class loader.
 */
public abstract class AgentExtraClassLoader extends ClassLoader {
    
    static {
        registerAsParallelCapable();
    }
    
    private final Collection<JarFile> extraJars;
    
    private final Collection<File> extraResourcePaths;
    
    public AgentExtraClassLoader(final ClassLoader appClassLoader, final Collection<JarFile> extraJars) {
        this(appClassLoader, extraJars, Collections.emptyList());
    }
    
    public AgentExtraClassLoader(final ClassLoader appClassLoader, final Collection<JarFile> extraJars, final Collection<File> extraResourcePaths) {
        super(appClassLoader);
        this.extraJars = extraJars;
        this.extraResourcePaths = extraResourcePaths;
    }
    
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        String path = convertClassNameToPath(name);
        for (JarFile each : extraJars) {
            ZipEntry entry = each.getEntry(path);
            if (null == entry) {
                continue;
            }
            try {
                definePackage(name, each);
                return defineClass(name, each, entry);
            } catch (final IOException ex) {
                throw new ClassNotFoundException(name);
            }
        }
        throw new ClassNotFoundException(name);
    }
    
    private String convertClassNameToPath(final String className) {
        return String.join("", className.replace(".", "/"), ".class");
    }
    
    private void definePackage(final String className, final JarFile extraJar) throws IOException {
        int index = className.lastIndexOf('.');
        if (-1 == index) {
            return;
        }
        String packageName = className.substring(0, index);
        if (null == getPackage(packageName)) {
            definePackage(packageName, extraJar.getManifest());
        }
    }
    
    private void definePackage(final String name, final Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        String specTitle = attributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
        String specVersion = attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
        String specVendor = attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
        String implTitle = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        String implVersion = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        String implVendor = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, null);
    }
    
    private Class<?> defineClass(final String name, final JarFile extraJar, final ZipEntry entry) throws IOException {
        byte[] data = ByteStreams.toByteArray(extraJar.getInputStream(entry));
        return defineClass(name, data, 0, data.length);
    }
    
    @Override
    protected Enumeration<URL> findResources(final String name) {
        Collection<URL> result = new LinkedList<>();
        for (JarFile each : extraJars) {
            findResource(name, each).ifPresent(result::add);
        }
        if (result.isEmpty()) {
            result.addAll(findResourcesFromResourcePaths(name));
        }
        return Collections.enumeration(result);
    }
    
    @Override
    protected URL findResource(final String name) {
        return extraJars.stream().map(each -> findResource(name, each)).filter(Optional::isPresent).findFirst().filter(Optional::isPresent).map(Optional::get)
                .orElseGet(() -> findResourcesFromResourcePaths(name).stream().findFirst().orElse(null));
    }
    
    private Optional<URL> findResource(final String name, final JarFile extraJar) {
        JarEntry entry = extraJar.getJarEntry(name);
        if (null == entry) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(String.format("jar:file:%s!/%s", extraJar.getName(), name)));
        } catch (final MalformedURLException ignored) {
            return Optional.empty();
        }
    }
    
    private Collection<URL> findResourcesFromResourcePaths(final String name) {
        Collection<URL> result = new LinkedList<>();
        Collection<File> resourceFiles = extraResourcePaths.stream().map(each -> new File(String.join(File.separator, each.getPath(), name))).filter(File::exists).collect(Collectors.toList());
        for (File each : resourceFiles) {
            try {
                result.add(each.toURI().toURL());
            } catch (final MalformedURLException ignored) {
            }
        }
        return result;
    }
}
