package com.talipov;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {

    public static void main(String[] args) {
        String classPath = "https://github.com/tmars/JavaClassLoader/raw/master/animal.jar";
        String className = "com.talipov.Animal";

        CustomClassLoader cl = new CustomClassLoader();
        Class cls = cl.findClass(classPath, className);

        Object obj = null;
        try {
            obj = Class.forName(className).getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String filename = "dat";
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XMLSerializer.serialize(outputStream, obj);
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done.");
    }
}
