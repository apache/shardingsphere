package io.shardingjdbc.orchestration.reg.stub;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shardingjdbc.orchestration.reg.base.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.base.EventListener;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.etcd.internal.Watcher;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EtcdClientStub implements EtcdClient {
    
    private Map<String, Element> elements = Maps.newConcurrentMap();
    
    private Map<String, Watcher> watchers = Maps.newConcurrentMap();
    
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Override
    public Optional<String> get(final String key) {
        Element element = elements.get(key);
        return Optional.fromNullable(element).transform(new Function<Element, String>() {
            
            @Override
            public String apply(final Element element) {
                return element.getValue();
            }
        });
    }
    
    @Override
    public Optional<List<String>> list(final String directory) {
        List<String> children = Lists.newArrayList();
        for (String key : elements.keySet()) {
            if (key.startsWith(directory)) {
                children.add(key);
            }
        }
        return Optional.of(children);
    }
    
    @Override
    public Optional<String> put(final String key, final String value) {
        return put(key, value, 0L);
    }
    
    @Override
    public Optional<String> put(final String key, final String value, final long ttl) {
        Element element = new Element(key, value, 0L);
        elements.put(key, element);
        fireEvent(element, DataChangedEvent.Type.UPDATED);
        return Optional.fromNullable(elements.get(key)).transform(new Function<Element, String>() {
            
            @Override
            public String apply(final Element input) {
                return input.getValue();
            }
        });
    }
    
    private void fireEvent(final Element element, final DataChangedEvent.Type type) {
        for (String keyOrPath : watchers.keySet()) {
            if (element.getKey().startsWith(keyOrPath)) {
                final Watcher watcher = watchers.get(keyOrPath);
                for (final EventListener listener : watcher.getListeners()) {
                    scheduler.schedule(new Runnable() {
                        
                        @Override
                        public void run() {
                            listener.onChange(new DataChangedEvent(type, element.getKey(), element.getValue()));
                        }
                    }, 50L, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
    
    @Override
    public Optional<Watcher> watch(final String keyOrDirectory) {
        Watcher watcher = new Watcher();
        watchers.put(keyOrDirectory, watcher);
        return Optional.of(watcher);
    }
    
    @Value
    final class Element {
        
        private String key;
    
        private String value;
        
        private long ttl;
    }
}
