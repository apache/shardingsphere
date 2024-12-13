package org.apache.shardingsphere.test.e2e.env.container.atomic.util;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.testcontainers.utility.Base58;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConfigPlaceholderReplacer {
    
    private static final String OS_MAC_TMP_DIR = "/tmp";
    
    private static final String E2E_PROXY_CONFIG_TMP_DIR_PREFIX = "shardingsphere-e2e-config-";
    
    /**
     * Get replaced resources.
     *
     * @param configResources config resources
     * @return replaced resources
     */
    public static Map<String, String> getReplacedResources(final Collection<String> configResources) {
        return getReplacedResources(configResources, getPlaceholderAndReplacementsMap());
    }
    
    @SneakyThrows(IOException.class)
    private static Map<String, String> getReplacedResources(final Collection<String> configResources, final Map<String, String> placeholderAndReplacementsMap) {
        Map<String, String> result = new HashMap<>();
        Path tempDirectory = createTempDirectory().toPath();
        for (String each : configResources) {
            String configResource = StringUtils.removeStart(each, "/");
            if (ClasspathResourceDirectoryReader.isDirectory(configResource)) {
                Path subDirectory = tempDirectory.resolve(Paths.get(configResource).getFileName());
                Files.createDirectory(subDirectory);
                ClasspathResourceDirectoryReader.read(configResource).forEach(resource -> getReplacedTempFile(subDirectory, resource, placeholderAndReplacementsMap));
                result.put(each, subDirectory.toFile().getAbsolutePath());
            } else {
                result.put(each, getReplacedTempFile(tempDirectory, each, placeholderAndReplacementsMap).getAbsolutePath());
            }
        }
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private static File getReplacedTempFile(final Path tempDirectory, final String configResource, final Map<String, String> placeholderAndReplacementsMap) {
        String content = getContent(configResource);
        for (Map.Entry<String, String> entry : placeholderAndReplacementsMap.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        File tempFile = tempDirectory.resolve(Paths.get(configResource).getFileName()).toFile();
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }
    
    private static String getContent(final String configResource) throws IOException {
        String content;
        InputStream resourceAsStream = ConfigPlaceholderReplacer.class.getClassLoader().getResourceAsStream(StringUtils.removeStart(configResource, "/"));
        if (resourceAsStream != null) {
            content = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        } else {
            content = IOUtils.toString(Paths.get(configResource).toUri(), StandardCharsets.UTF_8);
        }
        return content;
    }
    
    private static Map<String, String> getPlaceholderAndReplacementsMap() {
        return E2ETestEnvironment.getInstance().getPlaceholderAndReplacementsMap();
    }
    
    private static File createTempDirectory() {
        try {
            if (SystemUtils.IS_OS_MAC) {
                return Files.createTempDirectory(Paths.get(OS_MAC_TMP_DIR), E2E_PROXY_CONFIG_TMP_DIR_PREFIX).toFile();
            }
            return Files.createTempDirectory(E2E_PROXY_CONFIG_TMP_DIR_PREFIX).toFile();
        } catch (final IOException ex) {
            return new File(E2E_PROXY_CONFIG_TMP_DIR_PREFIX + Base58.randomString(5));
        }
    }
    
}
