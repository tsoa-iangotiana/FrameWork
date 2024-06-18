package Fonction;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;

import Annotation.Parametre;
import jakarta.servlet.http.HttpServletRequest;

public class ListClasse {
    public static ArrayList<Class<?>> getAllClasses(String packageName) throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');

        URL resource = classLoader.getResource(path);

        if (resource == null) {
            return classes;
        }

        File packageDir = new File(resource.getFile().replace("%20", " "));

        for (File file : packageDir.listFiles()) {
            if (file.isDirectory()) {
                classes.addAll(ListClasse.getAllClasses(packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    public static ArrayList<Class<?>> getControllerClasses(String packageName,
            Class<? extends Annotation> annotationController)
            throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> classes = getAllClasses(packageName);

        ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationController)) {
                result.add(clazz);
            }
        }

        return result;
    }
    public static ArrayList<Object> parameterMethod(Method method, HttpServletRequest request){
        ArrayList<Object> parameterValues = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Parametre.class)) {
                Parametre argument = parameter.getAnnotation(Parametre.class);
                String argument_value = argument.value();
                String value = request.getParameter(argument_value);
                if (value != null) {
                    parameterValues.add(value);
                } else {
                    throw new IllegalArgumentException("Paramètre manquant: " + argument_value);
                }
            }
        }
        return parameterValues;
    }
}
