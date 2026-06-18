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

package org.apache.shardingsphere.test.e2e.operation.transaction.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scan the transaction test classes to be tested.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestCaseClassScanner {
    
    private static final String TEST_CASE_PACKAGE_NAME = "org.apache.shardingsphere.test.e2e.operation.transaction.cases";
    
    private static final Collection<File> CLASS_FILES = new LinkedList<>();
    
    private static final String CLASS_SUFFIX = ".class";
    
    /**
     * Scan transaction test case classes in classpath.
     *
     * @return transaction test case classes
     */
    @SneakyThrows({IOException.class, ClassNotFoundException.class})
    public static List<Class<? extends BaseTransactionTestCase>> scan() {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(TEST_CASE_PACKAGE_NAME.replace(".", File.separator));
        return scanURL(urls, TEST_CASE_PACKAGE_NAME);
    }
    
    private static List<Class<? extends BaseTransactionTestCase>> scanURL(final Enumeration<URL> urls, final String packageName) throws IOException, ClassNotFoundException {
        List<Class<? extends BaseTransactionTestCase>> result = new LinkedList<>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String protocol = url.getProtocol();
            switch (protocol) {
                case "file":
                    addTestCaseClassesFromClassFiles(url, result);
                    break;
                case "jar":
                    addTestCaseClassesInJars(url, result, packageName);
                    break;
                default:
                    break;
            }
        }
        return result;
    }
    
    private static void addTestCaseClassesFromClassFiles(final URL url, final List<Class<? extends BaseTransactionTestCase>> caseClasses) throws UnsupportedEncodingException, ClassNotFoundException {
        String filepath = URLDecoder.decode(url.getFile(), "UTF-8");
        File file = new File(filepath);
        scanClassFiles(file);
        addTestCaseClasses(caseClasses);
    }
    
    private static void addTestCaseClassesInJars(final URL url, final List<Class<? extends BaseTransactionTestCase>> caseClasses, final String packageName) throws IOException, ClassNotFoundException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        if (null == connection) {
            return;
        }
        JarFile jarFile = connection.getJarFile();
        if (null == jarFile) {
            return;
        }
        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
        while (jarEntryEnumeration.hasMoreElements()) {
            JarEntry entry = jarEntryEnumeration.nextElement();
            String jarEntryName = entry.getName();
            if (jarEntryName.contains(CLASS_SUFFIX) && jarEntryName.replace(File.separator, ".").startsWith(packageName)) {
                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace(File.separator, ".");
                Class<?> clazz = Class.forName(className);
                addClass(caseClasses, clazz);
            }
        }
    }
    
    private static void scanClassFiles(final File file) {
        if (file.isDirectory()) {
            for (File each : Objects.requireNonNull(file.listFiles())) {
                scanClassFiles(each);
            }
        } else {
            if (file.getName().endsWith(CLASS_SUFFIX)) {
                CLASS_FILES.add(file);
            }
        }
    }
    
    private static void addTestCaseClasses(final List<Class<? extends BaseTransactionTestCase>> caseClasses) throws ClassNotFoundException {
        for (File file : CLASS_FILES) {
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(CLASS_SUFFIX)) {
                String noSuffixFileName = fileName.substring(8 + fileName.lastIndexOf("classes"), fileName.indexOf(CLASS_SUFFIX));
                String filePackage = noSuffixFileName.replace(File.separator, ".");
                Class<?> clazz = Class.forName(filePackage);
                addClass(caseClasses, clazz);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void addClass(final List<Class<? extends BaseTransactionTestCase>> caseClasses, final Class<?> clazz) {
        if (BaseTransactionTestCase.class.isAssignableFrom(clazz)) {
            caseClasses.add((Class<? extends BaseTransactionTestCase>) clazz);
        }
    }
}
