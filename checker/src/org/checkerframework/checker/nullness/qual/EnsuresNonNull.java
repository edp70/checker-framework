package org.checkerframework.checker.nullness.qual;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.PostconditionAnnotation;

/**
 * Indicates that the value expressions are non-null, if the method
 * terminates successfully.
 * <p>
 *
 * This postcondidion annotation is useful for methods that initialize a
 * field.  It can also be used for a method that fails if a given
 * expression is null.
 *
 * @see NonNull
 * @see org.checkerframework.checker.nullness.NullnessChecker
 * @checker_framework_manual #nullness-checker Nullness Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@PostconditionAnnotation(qualifier = NonNull.class)
public @interface EnsuresNonNull {
    /**
     * The Java expressions that are ensured to be {@link NonNull} on successful
     * method termination.
     *
     * @see <a
     *      href="http://types.cs.washington.edu/checker-framework/current/org.checkerframework.checker-manual.html#java-expressions-as-arguments">Syntax
     *      of Java expressions</a>
     */
    String[] value();
}