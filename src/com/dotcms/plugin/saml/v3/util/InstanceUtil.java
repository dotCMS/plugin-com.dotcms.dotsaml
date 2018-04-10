package com.dotcms.plugin.saml.v3.util;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import java.lang.reflect.Constructor;

/**
 * Just a class to instance without exceptions.
 * 
 * @author jsanca
 */
public class InstanceUtil
{
	/**
	 * Creates a new instance avoiding to throw any exception, null in case it
	 * can not be create (if an exception happens). This approach is based on a
	 * constructor with many arguments, keep in mind the method can not find a
	 * contructor to match with the arguments, null will be returned.
	 *
	 * @param className
	 * @param tClass
	 * @param arguments
	 * @param <T>
	 * @return T
	 */
	public static final <T> T newInstance( final String className, final Class<T> tClass, final Object... arguments )
	{
		T t = null;
		Constructor<?> constructor = null;
		Class<?>[] parameterTypes = null;
		Class<T> clazz = tClass;

		if ( UtilMethods.isSet( className ) )
		{
			clazz = getClass( className );
		}

		if ( null != clazz )
		{
			try
			{
				parameterTypes = getTypes( arguments );
				constructor = clazz.getDeclaredConstructor( parameterTypes );
				t = (T) constructor.newInstance( arguments );
			}
			catch ( Exception e )
			{
				if ( Logger.isErrorEnabled( InstanceUtil.class ) )
				{
					Logger.error( InstanceUtil.class, e.getMessage(), e );
				}
			}
		}

		return t;
	}

	/**
	 * Get the types of an array, you can pass an array or a comma separated
	 * arguments.
	 *
	 * @param array
	 *            - {@link Object}
	 * @return array of Class
	 */
	public static final Class<?>[] getTypes( final Object... array )
	{
		Class<?>[] parameterTypes = null;

		if ( null != array )
		{
			parameterTypes = new Class[ array.length ];

			for ( int i = 0; i < array.length; ++i )
			{
				parameterTypes[ i ] = array[ i ].getClass();
			}

		}

		return parameterTypes;
	}

	/**
	 * Tries to create a new instance from the className, otherwise creates a
	 * new from tClass. Null if it couldn't at all
	 *
	 * @param className
	 * @param tClass
	 * @param <T>
	 * @return T
	 */
	public static <T> T newInstance( final String className, final Class<T> tClass )
	{
		T t = null;

		if ( UtilMethods.isSet( className ) )
		{
			try
			{
				Logger.debug( InstanceUtil.class, "Creating an instance of: " + className );
				t = (T) Class.forName( className ).newInstance();
			}
			catch ( Exception e )
			{
				Logger.error( InstanceUtil.class, "Couldn't create from the classname: " + className + ", going to create: " + tClass.getName() );
				Logger.error( InstanceUtil.class, e.getMessage(), e );

				t = newInstance( tClass );
			}
		}
		else
		{
			t = newInstance( tClass );
		}

		return t;
	}

	/**
	 * Just get a new instance without throwing an exception. Null if couldn't
	 * create the instance.
	 * 
	 * @param tClass
	 *            {@link Class}
	 * @param <T>
	 * @return T
	 */
	public static <T> T newInstance( final Class<T> tClass )
	{
		T t = null;

		try
		{
			Logger.debug( InstanceUtil.class, "Creating an instance of: " + tClass.getName() );
			t = tClass.newInstance();
		}
		catch ( Exception e1 )
		{
			Logger.error( InstanceUtil.class, "Couldn't create from the class: " + tClass.getName() );
			Logger.error( InstanceUtil.class, e1.getMessage(), e1 );
			t = null;
		}

		return t;
	}

	/**
	 * Get a {@link Class} object based on the className, full if the class does
	 * not exists or invalid.
	 * 
	 * @param className
	 *            {@link String}
	 * @return Class
	 */
	public static Class getClass( final String className )
	{
		Class clazz = null;

		if ( UtilMethods.isSet( className ) )
		{
			try
			{
				clazz = Class.forName( className );
			}
			catch ( ClassNotFoundException e )
			{
				Logger.error( InstanceUtil.class, e.getMessage(), e );
				clazz = null;
			}
		}

		return clazz;
	}
}
