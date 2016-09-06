package com.dotcms.plugin.saml.v3;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import jnr.ffi.annotations.In;

/**
 * Just a class to instance without exceptions.
 * @author jsanca
 */
public class InstanceUtil {

    /**
     * Tries to create a new instance from the className, otherwise creates a new from tClass.
     * Null if it couldn't at all
     *
     * @param className
     * @param tClass
     * @param <T>
     * @return T
     */
    public static <T> T newInstance (final String className, final Class<T> tClass) {

        T t = null;

        if (UtilMethods.isSet(className)) {

            try {

                Logger.info(InstanceUtil.class, "Creating an instance of: " + className);
                t = (T)Class.forName(className).newInstance();
            } catch (Exception e) {

                Logger.error(InstanceUtil.class, "Couldn't create from the classname: " + className +
                                ", going to create: " + tClass.getName());
                Logger.error(InstanceUtil.class, e.getMessage(), e);

                t = newInstance(tClass);
            }
        } else {

            t = newInstance(tClass);
        }

        return t;
    } // newInstance.

    /**
     * Just get a new instance without throwing an exception.
     * Null if couldn't create the instance.
     * @param tClass {@link Class}
     * @param <T>
     * @return T
     */
    public static <T> T newInstance (final Class<T> tClass) {

        T t = null;

        try {

            Logger.info(InstanceUtil.class, "Creating an instance of: " + tClass.getName());
            t = tClass.newInstance();
        } catch (Exception e1) {

            Logger.error(InstanceUtil.class, "Couldn't create from the class: " + tClass.getName());
            Logger.error(InstanceUtil.class, e1.getMessage(), e1);
            t = null;
        }

        return t;
    } // newInstance.

    /**
     * Get a {@link Class} object based on the className, full if the class does not exists or invalid.
     * @param className {@link String}
     * @return Class
     */
    public static Class getClass(final String className) {

        Class clazz = null;

        if (UtilMethods.isSet(className)) {

            try {

                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {

                Logger.error(InstanceUtil.class, e.getMessage(), e);
                clazz = null;
            }
        }

        return clazz;
    } // getClass.
} // E:O:F:InstanceUtil.
