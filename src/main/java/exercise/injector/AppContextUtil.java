package exercise.injector;

import exercise.annotationvip.Autowired;
import exercise.annotationvip.Qualifier;
import org.burningwave.core.classes.FieldCriteria;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import static org.burningwave.core.assembler.StaticComponentContainer.Fields;

public class AppContextUtil {

    private AppContextUtil() {
        super();
    }

    //Рекурсивно выполнять инъекцию для каждой службы внутри класса Client.
    public static void autowire(AppContext appContext, Class<?> aClass, Object classInstance)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Collection<Field> fields = Fields.findAllAndMakeThemAccessible(
                FieldCriteria.forEntireClassHierarchy().allThoseThatMatch(field ->
                        field.isAnnotationPresent(Autowired.class)
                ),
                aClass
        );
        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(Qualifier.class)
                    ? field.getAnnotation(Qualifier.class).value()
                    : null;
            Object fieldInstance = appContext.getBeanInstance(field.getType(), field.getName(), qualifier);
            Fields.setDirect(classInstance, field, fieldInstance);
            autowire(appContext, fieldInstance.getClass(), fieldInstance);
        }
    }
}
