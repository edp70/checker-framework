package org.checkerframework.framework.util;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * An annotation intended solely for representing an unqualified type in
 * the qualifier hierarchy for the Purity Checker
 * <p>
 * This annotation may not be written in source code; it is an
 * implementation detail of the checker.
 * <p>
 * Note that because of the missing RetentionPolicy, the qualifier will
 * not be stored in bytecode. // TODO: set it to store in source
 */
@InvisibleQualifier
@SubtypeOf({})
@DefaultQualifierInHierarchy
@Target({ElementType.TYPE_USE})
public @interface PurityUnqualified { }