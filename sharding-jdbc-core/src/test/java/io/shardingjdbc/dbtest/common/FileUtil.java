/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * Get all the files in the directory.
     * @param filePath File path
     * @param prefixFile File name prefix
     * @param suffix File name suffix
     * @return All file paths
     */
    public static List<String> getAllFilePaths(final File filePath, final String prefixFile, final String suffix) {
        List<String> result = new ArrayList<>();
        File[] files = filePath.listFiles();
        if (files == null) {
            return result;
        }
        for (File each : files) {
            if (each.isDirectory()) {
                getSubFilePaths(each, result, prefixFile, suffix);
            } else {
                getFiles(prefixFile, suffix, result, each);

            }
        }
        return result;
    }

    private static void getFiles(final String prefixFile, final String suffix, final List<String> filePaths, final File f) {
        if (prefixFile != null) {
            if (f.getName().startsWith(prefixFile)) {
                if (suffix != null) {
                    if (f.getName().endsWith("." + suffix)) {
                        filePaths.add(f.getPath());
                    }
                } else {
                    filePaths.add(f.getPath());
                }
            }
        } else {
            filePaths.add(f.getPath());
        }
    }

    private static List<String> getSubFilePaths(final File filePath, final List<String> filePaths, final String prefixFile,
            final String suffix) {
        File[] files = filePath.listFiles();
        List<String> result = filePaths;
        if (files == null) {
            return result;
        }
        for (File each : files) {
            if (each.isDirectory()) {
                getSubFilePaths(each, result, prefixFile, suffix);
            } else {
                getFiles(prefixFile, suffix, result, each);
            }
        }
        return result;
    }

}
