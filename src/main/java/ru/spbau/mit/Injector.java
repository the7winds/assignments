package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */

    private static List<Class<?>> allClasses = new LinkedList();
    private static Map<Class<?>, Object> initedObjects = new Hashtable<>();
    private static List<Class> dependencies = new LinkedList<>();

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        for (String className : implementationClassNames) {
            allClasses.add(Class.forName(className));
        }

        return getObject(Class.forName(rootClassName));
    }

    private static Object getObject(Class<?> objClass) throws Exception {
        return initedObjects.containsKey(objClass) ? initedObjects.get(objClass)
                                                   : initObject(objClass);
    }

    private static Object initObject(Class<?> objClass) throws Exception {
        checkDependencies(objClass);

        Constructor constructor = objClass.getConstructors()[0];
        Class<?>[] argsTypes = constructor.getParameterTypes();

        Object[] args = getArgs(argsTypes);
        Object obj = constructor.newInstance(args);

        initedObjects.put(objClass, obj);

        return  obj;
    }

    private static Object[] getArgs(Class<?>[] argsTypes) throws Exception {
        List<Object> args = new LinkedList<>();

        for (Class<?> argType : argsTypes) {
            List<Class<?>> argsClasses = new LinkedList<>();
            for (Class<?> aClass : allClasses) {
                if (argType.isAssignableFrom(aClass)) {
                    argsClasses.add(aClass);
                }
            }

            if (argsClasses.isEmpty()) {
                throw new ImplementationNotFoundException();
            } else if (argsClasses.size() > 1) {
                throw new AmbiguousImplementationException();
            }

            args.add(getObject(argsClasses.get(0)));
        }

        return args.toArray();
    }

    private static void checkDependencies(Class<?> objClass) throws InjectionCycleException {
        if (dependencies.contains(objClass)) {
            throw  new InjectionCycleException();
        }
    }
}