package io.shardingsphere.spi;

import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SPI service loader for new instance for every call.
 *
 * @author zhangliang
 * @param <T> type of class
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewInstanceServiceLoader<T> {
    
    private final Collection<Class<T>> serviceClasses = new CopyOnWriteArrayList<>();
    
    /**
     * Creates a new service class loader for the given service type.
     * 
     * @param service service type
     * @param <T> type of service
     * @return new service class loader
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> NewInstanceServiceLoader<T> load(final Class<T> service) {
        NewInstanceServiceLoader result = new NewInstanceServiceLoader();
        for (T each : ServiceLoader.load(service)) {
            result.serviceClasses.add(each.getClass());
        }
        return result;
    }
    
    /**
     * New service instances.
     * 
     * @return service instances
     */
    public Collection<T> newServiceInstances() {
        Collection<T> result = new LinkedList<>();
        for (Class<T> each : serviceClasses) {
            try {
                result.add(each.newInstance());
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingException(ex);
            }
        }
        return result;
    }
}
