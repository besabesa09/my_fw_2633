package p16.model;

import p16.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ScanController {
    public static ArrayList<Class<?>> allClasses(String packageName) throws ClassNotFoundException, IOException {
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
                classes.addAll(allClasses(packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
    
        return classes;
    }
    
    public static ArrayList<Class<?>> goScan(String packageName) throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> classes = allClasses(packageName);

        ArrayList<Class<?>> result = new ArrayList<Class<?>>();
        
        for(Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                result.add(clazz);
            }
        }

        return result;
    }
}
