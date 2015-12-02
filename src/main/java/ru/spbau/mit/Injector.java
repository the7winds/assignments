package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */

    private static List<Class> implementationClasses = new LinkedList<>();

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        for (String className : implementationClassNames) {
            implementationClasses.add(Class.forName(className));
        }

        rootObjectNode = new Node(Class.forName(rootClassName), new LinkedList<Class<?>>());

        return rootObjectNode.object;
    }

    private static class Node {
        public Class<?> objectClass;
        public Constructor<?> constructor;
        public Object object;
        public Class<?>[] argsTypes;
        public List<Class<?>> dependencies = new LinkedList<>();
        public List<Node> argsNodes = new LinkedList<>();

        public Node(Class<?> aClass, List<Class<?>> dependencies) throws Exception {
            objectClass = aClass;
            constructor = objectClass.getConstructors()[0];
            argsTypes = constructor.getParameterTypes();

            this.dependencies.addAll(dependencies);
            this.dependencies.add(objectClass);

            InitArgsNodes(argsTypes);

            object = constructor.newInstance(getArgsObjArray());
        }

        private void InitArgsNodes(Class<?>[] argsTypes) throws Exception {
            List<Class<?>> args = new LinkedList<>();

            for (Class<?> argType : argsTypes) {
                args.add(findImpl(argType));
            }

            checkDependencies(args);

            for (Class<?> arg : args) {
                argsNodes.add(new Node(arg, dependencies));
            }
        }

        private void checkDependencies(List<Class<?>> args) throws Exception {
            for (Class<?> arg : args) {
                if (dependencies.contains(arg)) {
                    throw new InjectionCycleException();
                }
            }
        }

        private Class<?> findImpl(Class<?> argType) throws Exception {
            List<Class<?>> classes = new LinkedList<>();

            for (Class<?> implClass : implementationClasses) {
                if (checkExtends(argType, implClass) || checkImpl(argType, implClass)) {
                    classes.add(implClass);
                }
            }

            if (classes.size() == 0) {
                throw new ImplementationNotFoundException();
            } else if (classes.size() > 1) {
                throw new AmbiguousImplementationException();
            }

            return classes.get(0);
        }

        private boolean checkExtends(Class<?> argType, Class<?> implClass) {
            if (argType.equals(implClass) || implClass.getSuperclass().equals(argType)) {
                    return true;
            }

            return false;
        }

        private boolean checkImpl(Class<?> interf, Class<?> implClass) {
            if (interf.isInterface()) {
                Class<?>[] interfs = implClass.getInterfaces();
                for (Class<?> i : interfs) {
                    if (i.equals(interf)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public Object[] getArgsObjArray() {
            List<Object> argsObjArray = new LinkedList<>();

            for (Node node : argsNodes) {
                argsObjArray.add(node.object);
            }

            return argsObjArray.toArray();
        }
    }

    static private Node rootObjectNode;
}