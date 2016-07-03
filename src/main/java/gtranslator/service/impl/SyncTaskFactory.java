package gtranslator.service.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class SyncTaskFactory implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(SyncTaskFactory.class);
    private static Map<Method, SynchTask> methods = new HashMap<>();

    @Target({METHOD})
    @Retention(RUNTIME)
    @Documented
    public @interface SyncTask {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        methods.get(method).queue.put(args);
        return null;
    }

    public static <T1> T1 createFromClass(T1 implementator) {
        if (ProxyFactory.isProxyClass(implementator.getClass())) {
            return null;
        }

        for (Method m : implementator.getClass().getDeclaredMethods()) {
            if (m.getAnnotation(SyncTask.class) != null) {
                methods.put(m, new SynchTask(implementator, m));
            }
        }

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(implementator.getClass());
        proxyFactory.setFilter(m ->
                methods.containsKey(m)
        );
        Class<T1> c = proxyFactory.createClass();
        MethodHandler mi = (self, m, proceed, args) -> {
            methods.get(m).queue.put(args);
            return null;
        };

        try {
            T1 proxy = c.newInstance();
            ((javassist.util.proxy.Proxy) proxy).setHandler(mi);
            return proxy;
        } catch (InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            //TODO RuntimeException
            throw new RuntimeException(ex);
        }
    }

    public static <T1> T1 createFromInterface(Class<T1> intf, Object implementator) {
        SyncTaskFactory handler = new SyncTaskFactory();

        List<Method> ms = new ArrayList<>();
        ms.addAll(Arrays.asList(intf.getDeclaredMethods()));
        ms.addAll(Arrays.asList(implementator.getClass().getDeclaredMethods()));

        Map<String, Method> methodProxy = new HashMap<>();
        for (Method m : ms) {
            StringBuilder sb = new StringBuilder();
            sb.append(m.getName());
            for (Class c : m.getParameterTypes()) {
                sb.append(":");
                sb.append(c.getName());
            }
            String key = sb.toString();
            if (m.getDeclaringClass() == intf) {
                methodProxy.put(key, m);
            } else if (methodProxy.containsKey(key)) {
                methods.put(methodProxy.get(key), new SynchTask(implementator, m));
            }
        }

        return (T1) Proxy.newProxyInstance(implementator.getClass().getClassLoader(),
                new Class[]{intf}, handler);
    }

    private static class SynchTask {
        final Thread thread;
        final LinkedBlockingQueue<Object[]> queue;

        public SynchTask(Object listener, Method method) {
            queue = new LinkedBlockingQueue<>(100);
            thread = new Thread() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            Object[] args = queue.take();
                            method.invoke(listener, args);
                        } catch (Exception ex) {
                            logger.error("SynchTask:run " + ex.getMessage(), ex);
                        }
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }
}
