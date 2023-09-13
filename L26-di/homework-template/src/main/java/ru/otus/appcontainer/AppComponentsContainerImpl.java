package ru.otus.appcontainer;

import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final String COMPONENT_NOT_FOUND = "Component: %s is not found";

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);
        Map<Integer, List<Method>> orderMethods = new TreeMap<>();
        getCreateBeansMethodsWithOrder(configClass, orderMethods);
        createBeans(configClass, orderMethods);
    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        C result = null;
        for (Object appComponent : appComponents) {
            if (appComponent.getClass().equals(componentClass)) {
                result = (C) appComponent;
            } else {
                Class<?>[] interfaces = appComponent.getClass().getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    if (anInterface.equals(componentClass)) {
                        result = (C) appComponent;
                        break;
                    }
                }
            }
        }
        if (result == null) {
            throw new NoSuchElementException(String.format(COMPONENT_NOT_FOUND, componentClass));
        }
        return result;
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        if (appComponentsByName.containsKey(componentName)) {
            return (C) appComponentsByName.get(componentName);
        } else {
            throw new NoSuchElementException(String.format(COMPONENT_NOT_FOUND, componentName));
        }
    }

    private Object createConfigClassInstance(Class<?> configClass) {
        Object configClassInstance;
        try {
            configClassInstance = configClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            System.err.println("It is not possible to create a configuration class. Error:" + e);
            throw new RuntimeException(e);
        }
        return configClassInstance;
    }

    private void getCreateBeansMethodsWithOrder(Class<?> configClass, Map<Integer, List<Method>> orderMethods) {
        for (Method method : configClass.getMethods()) {
            if (method.isAnnotationPresent(AppComponent.class)) {
                AppComponent annotation = method.getAnnotation(AppComponent.class);
                int order = annotation.order();
                if (orderMethods.containsKey(order)) {
                    orderMethods.get(order).add(method);
                } else {
                    List<Method> methods = new ArrayList<>();
                    methods.add(method);
                    orderMethods.put(order, methods);
                }
            }
        }
    }

    private void createBeans(Class<?> configClass, Map<Integer, List<Method>> orderMethods) {
        Object configClassInstance = createConfigClassInstance(configClass);
        for (Integer order : orderMethods.keySet()) {
            List<Method> methods = orderMethods.get(order);
            for (Method method : methods) {
                AppComponent annotation = method.getAnnotation(AppComponent.class);
                String componentName = annotation.name();

                Class<?> returnType = method.getReturnType();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] args = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    args[i] = getAppComponent(parameterTypes[i]);
                }

                Object component;
                try {
                    component = returnType.cast(method.invoke(configClassInstance, args));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.err.println("Error when calling the method of creating an instance of the class: " + returnType);
                    throw new RuntimeException(e);
                }
                appComponents.add(component);
                appComponentsByName.put(componentName, component);
            }
        }
    }
}
