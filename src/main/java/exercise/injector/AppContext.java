package exercise.injector;

import exercise.annotationvip.Component;
import exercise.annotationvip.PostConstructor;
import org.burningwave.core.assembler.ComponentSupplier;
import org.burningwave.core.classes.ClassCriteria;
import org.burningwave.core.classes.ClassHunter;
import org.burningwave.core.classes.SearchConfig;

import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

//Инжектор для создания объектов для всех классов @CustomService. autowire or inject and все зависимости

public class AppContext {
    private Map<Class<?>, Class<?>> dependencyInjectionMap;
    private Map<Class<?>, Object> applicationScope;

    private static AppContext appContext;

    private AppContext() {
        super();
        dependencyInjectionMap = new HashMap<>();
        applicationScope = new HashMap<>();
    }

    //Как в спринге при загрузке в маин
    public static void startApplication(Class<?> mainClass) {
        try {
            synchronized (AppContext.class) {
                if (appContext == null) {
                    appContext = new AppContext();
                    appContext.initPostConstructor(mainClass);
                    appContext.initFramework(mainClass);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static <T> T getService(Class<T> tClass) {
        try {
            return appContext.getBeanInstance(tClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Получить все классы из входного пакета
    public Class<?>[] getClasses(String packageName, boolean recursive) throws ClassNotFoundException, IOException {
        ComponentSupplier componentSupplier = ComponentSupplier.getInstance();
        ClassHunter classHunter = componentSupplier.getClassHunter();
        String packageRelPath = packageName.replace(".", "/");
        SearchConfig config = SearchConfig.forResources(
                packageRelPath
        );
        if (!recursive) {
            config.findInChildren();
        }

        try (ClassHunter.SearchResult result = classHunter.findBy(config)) {
            Collection<Class<?>> classes = result.getClasses();
            return classes.toArray(new Class[classes.size()]);
        }
    }

    //инициализировать фреймворки инжектора
    private void initFramework(Class<?> mainClass)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        Class<?>[] classes = getClasses(mainClass.getPackage().getName(), true);
        ComponentSupplier componentSupplier = ComponentSupplier.getInstance();
        ClassHunter classHunter = componentSupplier.getClassHunter();
        String packageRelPath = mainClass.getPackage().getName().replace(".", "/");
        try (ClassHunter.SearchResult result = classHunter.findBy(
                SearchConfig.forResources(
                        packageRelPath
                ).by(ClassCriteria.create().allThoseThatMatch(cls -> {
                    return cls.getAnnotation(Component.class) != null;
                }))
        )) {
            //Так как Component присваивается типу то вот
            Collection<Class<?>> types = result.getClasses();
            for (Class<?> implementationClass : types) {
                Class<?>[] interfaces = implementationClass.getInterfaces();
                if (interfaces.length == 0) {
                    dependencyInjectionMap.put(implementationClass, implementationClass);
                } else {
                    for (Class<?> iface : interfaces) {
                        dependencyInjectionMap.put(implementationClass, iface);
                    }
                }
            }
            //Вот главная часть ссылка на объект InjectionUtil в котором и происходит рефлексия.
            for (Class<?> tClass : classes) {
                if (tClass.isAnnotationPresent(Component.class)) {
                    Object classInstance = tClass.getDeclaredConstructor().newInstance();
                    applicationScope.put(tClass, classInstance);
                    AppContextUtil.autowire(this, tClass, classInstance);
                }
            }
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    //Ломал голову с методом в итоге сделал копию интерфейса и засунул туда проверку на метод
    private void initPostConstructor(Class<?> mainClass)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        Class<?>[] classes = getClasses(mainClass.getPackage().getName(), true);
        ComponentSupplier componentSupplier = ComponentSupplier.getInstance();
        ClassHunter classHunter = componentSupplier.getClassHunter();
        String packageRelPath = mainClass.getPackage().getName().replace(".", "/");
        try (ClassHunter.SearchResult result = classHunter.findBy(
                SearchConfig.forResources(
                        packageRelPath
                ).by(ClassCriteria.create().allThoseThatMatch(cls -> {
                    return cls.getAnnotation(Component.class) != null;
                }))
        )) {
            //Так как Component присваивается типу то вот
            for (Class<?> implementationClass : result.getClasses()) {
                Class<?>[] interfaces = implementationClass.getInterfaces();
                if (interfaces.length == 0) {
                    dependencyInjectionMap.put(implementationClass, implementationClass);
                } else {
                    for (Class<?> iface : interfaces) {
                        dependencyInjectionMap.put(implementationClass, iface);
                    }
                }
            }
            //Вот самая главная часть кода для аннотации PostConstructor
            for (Class<?> tClass : classes) {
                Method[] methods = tClass.getMethods();
                for (Method method : methods) {
                    PostConstructor annotation = method.getAnnotation(PostConstructor.class);
                    if (annotation != null) {
                        System.out.println("CREATE PostConstructor");
                    }
                }
                if (tClass.isAnnotationPresent(Component.class)) {
                    Object classInstance = tClass.getDeclaredConstructor().newInstance();
                    applicationScope.put(tClass, classInstance);
                    AppContextUtil.autowire(this, tClass, classInstance);
                }
            }
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    //Получить имя класса реализации для службы интерфейса ввода
    private Class<?> getImplimentationClass(Class<?> interfaceClass, final String fieldName, final String qualifier) {
        Set<Entry<Class<?>, Class<?>>> implementationClasses = dependencyInjectionMap.entrySet().stream()
                .filter(entry -> entry.getValue() == interfaceClass).collect(Collectors.toSet());
        String errorMessage = "";
        if (implementationClasses == null || implementationClasses.size() == 0) {
            errorMessage = "no implementation found for interface " + interfaceClass.getName();
        } else if (implementationClasses.size() == 1) {
            Optional<Entry<Class<?>, Class<?>>> optional = implementationClasses.stream().findFirst();
            if (optional.isPresent()) {
                return optional.get().getKey();
            }
        } else if (implementationClasses.size() > 1) {
            final String findBy = (qualifier == null || qualifier.trim().length() == 0) ? fieldName : qualifier;
            Optional<Entry<Class<?>, Class<?>>> optional = implementationClasses.stream()
                    .filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
            if (optional.isPresent()) {
                return optional.get().getKey();
            } else {
                errorMessage = "There are " + implementationClasses.size() + " of interface " + interfaceClass.getName()
                        + " Expected single implementation or make use of @CustomQualifier to resolve conflict";
            }
        }
        throw new RuntimeErrorException(new Error(errorMessage));
    }

    //Создать и получить экземпляр Object инстанса для выполнения ввода, то есть сервис интерфейса
    @SuppressWarnings("unchecked")
    private <T> T getBeanInstance(Class<T> interfaceClass) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (T) getBeanInstance(interfaceClass, null, null);
    }

    //Перегрузить или обновить getBeanInstance для обработки Qualifier и Autowired по типу
    public <T> Object getBeanInstance(Class<T> interfaceClass, String fieldName, String qualifier)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> implementationClass = getImplimentationClass(interfaceClass, fieldName, qualifier);

        if (applicationScope.containsKey(implementationClass)) {
            return applicationScope.get(implementationClass);
        }

        synchronized (applicationScope) {
            Object service = implementationClass.getDeclaredConstructor().newInstance();
            applicationScope.put(implementationClass, service);
            return service;
        }
    }
}
