package com.google.code.rees.scope.testutil;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.easymock.EasyMock;
import org.junit.Test;

public abstract class SerializableObjectTest<T> 
{

    /** Logger for this class. */
    static private final Logger LOG = LogManager.getLogger(SerializableObjectTest.class);
    
    
    @Test
    public void testSerialization() throws Exception 
    {
        LOG.trace("start test for Serialization");
        String genericSuperClassName = this.getClass().getGenericSuperclass().toString();
        String genericClassName = genericSuperClassName.substring(
                genericSuperClassName.indexOf('<') + 1,
                genericSuperClassName.indexOf('>'));
        @SuppressWarnings("unchecked")
        Class<T> serializableObjectClass = (Class<T>) Class.forName(genericClassName);

        T serializableObject = this.getInstance(serializableObjectClass);

        SerializationTestingUtil.getSerializedCopy((Serializable) serializableObject);
    }

    @SuppressWarnings("unchecked")
    public <TT> TT getInstance(Class<TT> tt) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException 
    {
        LOG.trace("mocking instance for class: {}",tt.getName());
        Constructor<?> constructor = tt.getDeclaredConstructors()[0];
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] constructorArgs = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) 
        {
            Class<?> paramClass = paramTypes[i];
            if (paramClass.isInterface()) 
            {
                constructorArgs[i] = EasyMock.createMock(paramClass);
            } 
            else if (paramClass.isArray() && paramClass.getComponentType().isPrimitive() )
            {
                constructorArgs[i] = this.getPrimitiveArrayInstace(paramClass);
            }
            else if (paramClass.isPrimitive()) 
            {
                constructorArgs[i] = this.getPrimitiveInstance(paramClass);
            } 
            else 
            {
                constructorArgs[i] = getInstance(paramClass);
            }
        }
        TT instance = null;
        try 
        {
            LOG.trace("constructorArgs: {}",constructorArgs);
            instance = (TT) constructor.newInstance(constructorArgs);
        } 
        catch (InstantiationException ie) 
        {
            instance = EasyMock.createMock(tt);
        }

        return instance;
    }

    
    @SuppressWarnings("unchecked")
    public <TTT> TTT getPrimitiveInstance(Class<TTT> ttt) 
    {
        Object primitiveWrapper = null;
        
        if  ( int.class.equals(ttt) )
            primitiveWrapper = 1;
        else if ( long.class.equals(ttt) ) 
            primitiveWrapper = 1L;
        else if ( short.class.equals(ttt) ) 
            primitiveWrapper =  (short) 1;
        else if ( byte.class.equals(ttt) ) 
            primitiveWrapper =  (byte) '@';
        else if ( boolean.class.equals(ttt) )
            primitiveWrapper = false;

        return (TTT) primitiveWrapper;
    }

    
    @SuppressWarnings("unchecked")
    public <TTT> TTT getPrimitiveArrayInstace(Class<TTT> ttt)
    {
        Object primitiveWrapper = null;
        if ( byte.class.equals(ttt.getComponentType()))
        {
            primitiveWrapper = new byte[1];
        }
        return (TTT) primitiveWrapper;
    }

}
