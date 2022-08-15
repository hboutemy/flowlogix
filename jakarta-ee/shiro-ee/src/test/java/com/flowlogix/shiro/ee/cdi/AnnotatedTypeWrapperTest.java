/*
 * Copyright 2022 lprimak.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlogix.shiro.ee.cdi;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.enterprise.inject.spi.AnnotatedType;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author lprimak
 */
@ExtendWith(MockitoExtension.class)
class AnnotatedTypeWrapperTest {
    @Mock
    private AnnotatedType<Void> annotatedType;

    @RequiresAuthentication
    @RequiresGuest
    @RequiresPermissions("hello")
    private class Annotated { }

    @ShiroSecureAnnotation
    private class ShiroSecureAnnotated { }

    @Stateless
    private class StatelessAnnotated { }

    @Test
    void noAnnotations() {
        var wrapper = new AnnotatedTypeWrapper<>(annotatedType);
        assertEquals(0, wrapper.getAnnotations().size());
    }

    @Test
    void noAdditionalAnnotations() {
        initializeStubs();
        var wrapper = new AnnotatedTypeWrapper<>(annotatedType);
        assertEquals(3, wrapper.getAnnotations().size());
    }

    @Test
    void twoAdditionalAnnotations() {
        initializeStubs();
        var wrapper = new AnnotatedTypeWrapper<>(annotatedType,
                ShiroSecureAnnotated.class.getDeclaredAnnotations()[0],
                StatelessAnnotated.class.getDeclaredAnnotations()[0]);
        assertEquals(5, wrapper.getAnnotations().size());
        assertTrue(wrapper.isAnnotationPresent(ShiroSecureAnnotated.class
                .getDeclaredAnnotations()[0].annotationType()));
        assertTrue(wrapper.isAnnotationPresent(StatelessAnnotated.class
                .getDeclaredAnnotations()[0].annotationType()));
        assertTrue(wrapper.isAnnotationPresent(Annotated.class
                .getDeclaredAnnotations()[0].annotationType()));
        assertTrue(wrapper.isAnnotationPresent(Annotated.class
                .getDeclaredAnnotations()[1].annotationType()));
        assertTrue(wrapper.isAnnotationPresent(Annotated.class
                .getDeclaredAnnotations()[2].annotationType()));
    }

    @Test
    void overriddenAnnotation() {
        initializeStubs();
        when(annotatedType.getJavaClass()).thenReturn(Void.class);
        assertEquals(3, annotatedType.getAnnotations().size());
        var wrapper = new AnnotatedTypeWrapper<>(annotatedType, false,
                ShiroSecureAnnotated.class.getDeclaredAnnotations()[0],
                StatelessAnnotated.class.getDeclaredAnnotations()[0]);
        assertEquals(2, wrapper.getAnnotations().size());
        assertTrue(wrapper.isAnnotationPresent(ShiroSecureAnnotated.class
                .getDeclaredAnnotations()[0].annotationType()));
        assertTrue(wrapper.isAnnotationPresent(StatelessAnnotated.class
                .getDeclaredAnnotations()[0].annotationType()));
        assertEquals(Void.class, wrapper.getJavaClass());
    }

    @Test
    void decreaseAnnotationsToZero() {
        initializeStubs();
        assertEquals(3, annotatedType.getAnnotations().size());
        var wrapper = new AnnotatedTypeWrapper<>(annotatedType, false);
        assertEquals(0, wrapper.getAnnotations().size());
    }

    private void initializeStubs() {
        when(annotatedType.getAnnotations()).thenReturn(Stream.of(Annotated.class.getDeclaredAnnotations())
                .collect(Collectors.toSet()));
    }
}