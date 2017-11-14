package io.shardingjdbc.orchestration.yaml;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;

/**
 * Yaml utility
 *
 * @author junxiong
 */
@Slf4j
public class YamlUtils {

    /**
     * load config
     *
     * @param file file
     * @param tClass class
     * @return config
     */
    public static <T> T load(final File file, final Class<T> tClass) {
        try {
            final Reader reader = new InputStreamReader(new FileInputStream(file));
            return load(reader, tClass);
        } catch (FileNotFoundException e) {
            log.error("Unable to load yaml file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * load config
     * @param bytes bytes
     * @param tClass class
     * @return config
     */
    public static <T> T load(byte[] bytes, Class<T> tClass) {
        final Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes));
        return load(reader, tClass);
    }

    private static <T> T load(Reader reader, Class<T> tClass) {
        Yaml yaml = new Yaml(new Constructor(tClass));
        return yaml.loadAs(reader, tClass);
    }
}
