/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.ejb;

import com.flowlogix.web.services.internal.EJBAnnotationWorker;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * JNDIObjectLocator is used to centralize all JNDI lookups. It minimises the overhead of JNDI lookups by caching the
 * objects it looks up.
 * 
 * @author Geoff Callendar
 * Enhancements by Lenny Primak
 */
public class JNDIObjectLocator
{
    @SneakyThrows(NamingException.class)
    public JNDIObjectLocator()
    {
        initialContext = new InitialContext();
    }
    
    
    @SneakyThrows(NamingException.class)
    public<T> T getObject(Class<T> beanClass)
    {
        String name = EJBAnnotationWorker.guessByType(beanClass.getName());
        return getObject(EJBAnnotationWorker.prependPortableName(name));
    }
    
    
    @SuppressWarnings("unchecked")
    public<T> T getObject(String jndiName) throws NamingException
    {
        return (T)getJNDIObject(jndiName);
    }

    
    public synchronized void clear()
    {
        jndiObjectCache.clear();
    }
    

    public Object getJNDIObject(String jndiName) throws NamingException
    {
        Object jndiObject = jndiObjectCache.get(jndiName);

        if (jndiObject == null && !jndiObjectCache.containsKey(jndiName))
        {
            try
            {
                jndiObject = lookup(jndiName);
                jndiObjectCache.put(jndiName, jndiObject);
            } catch (NamingException e)
            {
                clear();
                throw e;
            }
        }
        return jndiObject;
    }

    
    private synchronized Object lookup(String name) throws NamingException
    {

        // Recheck the cache because the name we're looking for may have been added while we were waiting for sync.

        if (!jndiObjectCache.containsKey(name))
        {
            try
            {
                return getInitialContext().lookup(name);
            } catch (NameNotFoundException e)
            {
                clear();
                throw e;
            }
        } else
        {
            return jndiObjectCache.get(name);
        }
    }

    
    @Getter private final InitialContext initialContext;
    private final Map<String, Object> jndiObjectCache = Collections.synchronizedMap(new HashMap<String, Object>());
}
