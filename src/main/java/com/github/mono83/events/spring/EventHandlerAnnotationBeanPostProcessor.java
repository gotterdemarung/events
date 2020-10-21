package com.github.mono83.events.spring;

import com.github.mono83.events.ClassNameHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class EventHandlerAnnotationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
    private final HashMap<String, ArrayList<Consumer<Object>>> deferred = new HashMap<>();
    private BeanFactory factory;

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.factory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Method method : beanClass.getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation != null) {
                if (method.getParameterCount() != 1) {
                    throw new IllegalParametersCountException(beanClass, beanName, method);
                }

                deferred.compute(beanName, (n, p) -> {
                    ArrayList<Consumer<Object>> previous = p == null
                            ? new ArrayList<>()
                            : p;
                    previous.add((finalBean) -> {
                        ClassNameHandlerRegistry router = factory.getBean(ClassNameHandlerRegistry.class);
                        router.register(
                                new EventHandlerMethodInvoker(method, finalBean, beanName),
                                method.getParameterTypes()[0]
                        );
                    });
                    return previous;
                });
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        ArrayList<Consumer<Object>> runners = deferred.get(beanName);
        if (runners != null) {
            for (Consumer<Object> runner : runners) {
                runner.accept(bean);
            }
        }
        return bean;
    }

    private static class EventHandlerMethodInvoker implements Consumer<Object> {
        private final Method method;
        private final String beanName;
        private final Object bean;

        private EventHandlerMethodInvoker(final Method method, final Object bean, final String beanName) {
            this.method = method;
            this.beanName = beanName;
            this.bean = bean;
        }

        @Override
        public void accept(final Object event) {
            if (event == null) {
                return;
            }
            try {
                method.invoke(bean, event);
            } catch (Throwable error) {
                throw new EventHandlerMethodInvocationException(bean, beanName, method, error);
            }
        }
    }

    private static class EventHandlerMethodInvocationException extends RuntimeException {
        EventHandlerMethodInvocationException(
                final Object bean,
                final String beanName,
                final Method method,
                final Throwable cause
        ) {
            super(
                    String.format(
                            "Error invoking event handler %s in %s (%s)",
                            method.getName(),
                            bean.getClass().getName(),
                            beanName
                    ),
                    cause
            );
        }
    }

    private static class IllegalParametersCountException extends BeansException {
        IllegalParametersCountException(final Class<?> beanClass, final String beanName, final Method method) {
            super(String.format(
                    "Expected exactly one parameter in %s.%s (%s) but got %d",
                    beanClass.getName(),
                    method.getName(),
                    beanName,
                    method.getParameterCount()
            ));
        }
    }
}
