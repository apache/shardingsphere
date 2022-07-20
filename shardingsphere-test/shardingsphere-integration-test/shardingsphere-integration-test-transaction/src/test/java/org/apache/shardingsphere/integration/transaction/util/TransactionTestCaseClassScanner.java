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

package org.apache.shardingsphere.integration.transaction.util;

import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scan the transaction test classes to be tested.
 */
public class TransactionTestCaseClassScanner {
    
    private static final String TEST_CASE_BASE_PACKAGE_NAME = "org.apache.shardingsphere.integration.transaction.cases";
    
    private static final List<File> FILES = new ArrayList<>();
    
    private static final String CLASS_SYMBOL = ".class";
    
    /**
     * Scan transaction test case classes in classpath.
     *
     * @return transaction test case classes
     */
    @SneakyThrows
    public static List<Class<? extends BaseTransactionTestCase>> scan() {
        final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(TEST_CASE_BASE_PACKAGE_NAME.replace(".", File.separator));
        return scanURL(urls);
    }
    
    private static List<Class<? extends BaseTransactionTestCase>> scanURL(final Enumeration<URL> urls) throws IOException, ClassNotFoundException {
        List<Class<? extends BaseTransactionTestCase>> caseClasses = new LinkedList<>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String protocol = url.getProtocol();
            switch (protocol) {
                case "file":
                    addTestCaseClassesFromClassFiles(url, caseClasses);
                    break;
                case "jar":
                    addTestCaseClassesInJars(url, caseClasses);
                    break;
                default:
                    break;
            }
        }
        return caseClasses;
    }
    
    private static void addTestCaseClassesFromClassFiles(final URL url, final List<Class<? extends BaseTransactionTestCase>> caseClasses) throws UnsupportedEncodingException, ClassNotFoundException {
        String filepath = URLDecoder.decode(url.getFile(), "UTF-8");
        File file = new File(filepath);
        scanClassFiles(file);
        addTestCaseClasses(caseClasses);
    }
    
    private static void addTestCaseClassesInJars(final URL url, final List<Class<? extends BaseTransactionTestCase>> caseClasses) throws IOException, ClassNotFoundException {
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
            if (jarEntryName.contains(CLASS_SYMBOL) && jarEntryName.replace("/", ".").startsWith(TEST_CASE_BASE_PACKAGE_NAME)) {
                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                Class<?> clazz = Class.forName(className);
                if (clazz.isAssignableFrom(BaseTransactionTestCase.class)) {
                    caseClasses.add((Class<? extends BaseTransactionTestCase>) clazz);
                }
            }
        }
    }
    
    private static void scanClassFiles(final File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                scanClassFiles(f);
            }
        } else {
            if (file.getName().endsWith(CLASS_SYMBOL)) {
                FILES.add(file);
            }
        }
    }
    
    private static void addTestCaseClasses(final List<Class<? extends BaseTransactionTestCase>> caseClasses) throws ClassNotFoundException {
        for (File file : FILES) {
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(CLASS_SYMBOL)) {
                String noSuffixFileName = fileName.substring(8 + fileName.lastIndexOf("classes"), fileName.indexOf(CLASS_SYMBOL));
                String filePackage = noSuffixFileName.replace("/", ".");
                Class<?> clazz = Class.forName(filePackage);
                if (BaseTransactionTestCase.class.isAssignableFrom(clazz)) {
                    caseClasses.add((Class<? extends BaseTransactionTestCase>) clazz);
                }
            }
        }
    }
    
}
