package info.avalon566.shardingscaling.utils;

import lombok.var;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author avalon566
 */
public final class YamlUtil {

    public static <T> T parse(String fileName, Class<T> t) throws FileNotFoundException {
        var representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return new Yaml(new Constructor(t), representer).loadAs(new FileInputStream(fileName), t);
    }
}
