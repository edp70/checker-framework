package org.checkerframework.framework.type;

import org.checkerframework.framework.type.AnnotatedTypeMirror.*;
import org.checkerframework.framework.type.visitor.AnnotatedTypeVisitor;

import java.util.*;

/**
 * AnnotatedTypeCopier is a visitor that deep copies an AnnotatedTypeMirror exactly, including any lazily initialized
 * fields.  That is, if a field has already been initialized, it will be initialized in the copied type.  Previous
 * versions of copy also flipped the "isDeclaration" field.  This copier maintains isDeclaration.
 *
 * When making copies, a map of encountered references -> copied types is maintain.  This ensures that, if a
 * reference appears in multiple locations in the original type, a corresponding copy of the original type
 * appears in the same locations in the output copy.  This ensures that the recursive loops in the input type
 * are preserved in its output copy (see makeOrReturnCopy)
 *
 * In general, this class should be used via the public static "copy" method.
 *
 * E.g.
 * new AnnotatedTypeCopier().visit(myTypeVar);
 */
public class AnnotatedTypeCopier implements AnnotatedTypeVisitor<AnnotatedTypeMirror, IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>> {

    /**
     * This is a hack to handle the curious behavior of substitution on an AnnotatedExecutableType.
     * @see org.checkerframework.framework.type.TypeVariableSubstitutor
     * It is poor form to include such a flag on the base class for exclusive use in a subclass
     * but it is the least bad option in this case.
     */
    protected boolean visitingExecutableTypeParam = false;

    private final boolean copyAnnotations;

    public AnnotatedTypeCopier(final boolean copyAnnotations) {
        this.copyAnnotations = copyAnnotations;
    }

    public AnnotatedTypeCopier() {
        this(true);
    }

    @Override
    public AnnotatedTypeMirror visit(AnnotatedTypeMirror type) {
        return type.accept(this, new IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>());
    }

    @Override
    public AnnotatedTypeMirror visit(AnnotatedTypeMirror type,
                                     IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        return type.accept(this, originalToCopy);
    }

    @Override
    public AnnotatedTypeMirror visitDeclared(AnnotatedDeclaredType original,
                                             IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedDeclaredType copy =  (AnnotatedDeclaredType) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        if (original.wasRaw()) {
            copy.setWasRaw();
        }

        if (copy.enclosingType != null) {
            copy.enclosingType = (AnnotatedDeclaredType) visit(original.enclosingType, originalToCopy);
        }

        if (original.typeArgs != null) {
            final List<AnnotatedTypeMirror> copyTypeArgs = new ArrayList<>();
            for(final AnnotatedTypeMirror typeArg : original.typeArgs) {
                copyTypeArgs.add(visit(typeArg, originalToCopy));
            }
            copy.typeArgs = Collections.unmodifiableList( copyTypeArgs );
        }

        if (original.supertypes != null) {
            final List<AnnotatedDeclaredType> copyTypeArgs = new ArrayList<>();
            for(final AnnotatedDeclaredType supertype : original.supertypes) {
                copyTypeArgs.add((AnnotatedDeclaredType) visit(supertype, originalToCopy));
            }
            copy.supertypes = Collections.unmodifiableList( copyTypeArgs );
        }

        return copy;
    }

    @Override
    public AnnotatedTypeMirror visitIntersection(AnnotatedIntersectionType original,
                                                 IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedIntersectionType copy =  (AnnotatedIntersectionType) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        if (original.supertypes != null) {
            final List<AnnotatedDeclaredType> copySupertypes = new ArrayList<AnnotatedDeclaredType>();
            for(final AnnotatedDeclaredType supertype : original.supertypes) {
                copySupertypes.add((AnnotatedDeclaredType) visit(supertype, originalToCopy));
            }
            copy.supertypes = Collections.unmodifiableList( copySupertypes );
        }

        return copy;
    }

    @Override
    public AnnotatedTypeMirror visitUnion(AnnotatedUnionType original,
                                          IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedUnionType copy =  (AnnotatedUnionType) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        if (original.alternatives != null) {
            final List<AnnotatedDeclaredType> copyAlternatives = new ArrayList<AnnotatedDeclaredType>();
            for(final AnnotatedDeclaredType supertype : original.alternatives) {
                copyAlternatives.add((AnnotatedDeclaredType) visit(supertype, originalToCopy));
            }
            copy.alternatives = Collections.unmodifiableList( copyAlternatives );
        }

        return copy;
    }

    @Override
    public AnnotatedTypeMirror visitExecutable(AnnotatedExecutableType original,
                                               IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedExecutableType copy =  (AnnotatedExecutableType) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        copy.setElement(original.getElement());

        if (original.receiverType != null) {
            copy.receiverType = (AnnotatedDeclaredType) visit(original.receiverType, originalToCopy);
        }

        for (final AnnotatedTypeMirror param : original.paramTypes) {
            copy.paramTypes.add(visit(param, originalToCopy));
        }

        for (final AnnotatedTypeMirror thrown : original.throwsTypes) {
            copy.throwsTypes.add(visit(thrown, originalToCopy));
        }

        copy.returnType = visit(original.returnType, originalToCopy);

        for (final AnnotatedTypeVariable typeVariable : original.typeVarTypes) {
            visitingExecutableTypeParam = true;
            copy.typeVarTypes.add((AnnotatedTypeVariable) visit(typeVariable, originalToCopy));
        }
        visitingExecutableTypeParam = false;

        return copy;
    }

    @Override
    public AnnotatedTypeMirror visitArray(AnnotatedArrayType original,
                                          IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedArrayType copy =  (AnnotatedArrayType) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        copy.setComponentType(visit(original.getComponentType(), originalToCopy));

        return copy;
    }

    @Override
    public AnnotatedTypeMirror visitTypeVariable(AnnotatedTypeVariable original,
                                                 IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedTypeVariable copy =  (AnnotatedTypeVariable) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        if (original.getUpperBoundField() != null) {
            copy.setUpperBoundField(visit(original.getUpperBoundField(), originalToCopy));
        }

        if (original.getLowerBoundField() != null) {
            copy.setLowerBoundField(visit(original.getLowerBoundField(), originalToCopy));
        }

        return copy;
    }

    @Override
    public AnnotatedTypeMirror visitPrimitive(AnnotatedPrimitiveType original,
                                              IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        return makeOrReturnCopy(original, originalToCopy);
    }

    @Override
    public AnnotatedTypeMirror visitNoType(AnnotatedNoType original,
                                           IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        return makeOrReturnCopy(original, originalToCopy);
    }

    @Override
    public AnnotatedTypeMirror visitNull(AnnotatedNullType original,
                                         IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        return makeOrReturnCopy(original, originalToCopy);
    }

    @Override
    public AnnotatedTypeMirror visitWildcard(AnnotatedWildcardType original,
                                             IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final AnnotatedWildcardType copy =  (AnnotatedWildcardType) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        if (original.isTypeArgHack()) {
            copy.setTypeArgHack();
        }

        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        if (original.getExtendsBoundField() != null) {
            copy.setExtendsBound(visit(original.getExtendsBoundField(), originalToCopy));
        }

        if ( original.getSuperBoundField() != null) {
            copy.setSuperBound(visit(original.getSuperBoundField(), originalToCopy));
        }

        return copy;
    }

    /**
     * When copying types, we may encounter a reference to an AnnotatedTypeMirror in different positions.
     * An example of this would be recursive type parameters.
     * E.g.  <T extends List<T>>
     * In this example, the bound of T will contain a cycle and a visitor to this type would encounter
     * the type argument T to List<T> multiple times.  The first time a reference is encountered a
     * copy of its type is created and added returned.  If a reference is encountered again, the previously
     * made copy is returned.  This preserves the cyclic nature of the type being copied in the output
     * copy.
     *
     * Note: This method is idempotent
     *
     * @param original A reference to a type to copy.
     * @param originalToCopy A mapping of previously encountered references to the copies made for those references
     * @param <T> The type of original copy, this is a shortcut to avoid having to insert casts all over the visitor
     * @return A copy of original
     */
    @SuppressWarnings("unchecked")
    protected <T extends AnnotatedTypeMirror> AnnotatedTypeMirror makeOrReturnCopy(
            T original, IdentityHashMap<AnnotatedTypeMirror, AnnotatedTypeMirror> originalToCopy) {
        if (originalToCopy.containsKey(original)) {
            return originalToCopy.get(original);
        }

        final T copy = (T) AnnotatedTypeMirror.createType(
                original.getUnderlyingType(), original.atypeFactory, original.isDeclaration());
        maybeCopyPrimaryAnnotations(original, copy);
        originalToCopy.put(original, copy);

        return copy;
    }

    protected void maybeCopyPrimaryAnnotations(final AnnotatedTypeMirror source, final AnnotatedTypeMirror dest) {
        if (copyAnnotations) {
            dest.addAnnotations(source.annotations);
        }
    }
}