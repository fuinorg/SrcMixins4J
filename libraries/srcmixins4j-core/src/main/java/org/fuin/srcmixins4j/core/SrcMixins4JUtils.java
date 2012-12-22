/**
 * Copyright (C) 2012 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.srcmixins4j.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.annotations.AnnotationParameter;
import org.emftext.language.java.annotations.AnnotationValue;
import org.emftext.language.java.annotations.SingleAnnotationParameter;
import org.emftext.language.java.classifiers.AnonymousClass;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.generics.QualifiedTypeArgument;
import org.emftext.language.java.generics.TypeArgument;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.imports.ClassifierImport;
import org.emftext.language.java.imports.Import;
import org.emftext.language.java.imports.ImportingElement;
import org.emftext.language.java.imports.ImportsFactory;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.MemberContainer;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.modifiers.AnnotationInstanceOrModifier;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.references.IdentifierReference;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.NamespaceClassifierReference;
import org.emftext.language.java.types.PrimitiveType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.fuin.srcmixins4j.annotations.MixinGenerated;
import org.fuin.srcmixins4j.annotations.MixinIntf;
import org.fuin.srcmixins4j.annotations.MixinProvider;

/**
 * Generates code for a mixin.
 */
public final class SrcMixins4JUtils {

    /**
     * Private default constructor.
     */
    private SrcMixins4JUtils() {
        throw new UnsupportedOperationException(
                "Creating an instance of this utility class is not allowed");
    }

    /**
     * Returns a list of all Mixin interfaces implemented by a given class.
     * 
     * @param clasz
     *            Class to inspect.
     * 
     * @return List of interfaces the inspected class implements.
     */
    public static List<Interface> getMixinInterfaces(final Class clasz) {

        assertArgNotNull("clasz", clasz);
        assertNoProxy(clasz);

        final List<Interface> mixinIntfs = new ArrayList<Interface>();

        final EList<TypeReference> implementedInterfaces = clasz
                .getImplements();
        for (TypeReference implementedInterface : implementedInterfaces) {
            if (implementedInterface.getTarget() instanceof Interface) {
                final Interface potentialMixinIntf = (Interface) implementedInterface
                        .getTarget();
                if (getAnnotationInstance(potentialMixinIntf,
                        MixinIntf.class.getName()) != null) {
                    mixinIntfs.add(potentialMixinIntf);
                }
            }
        }

        return mixinIntfs;
    }

    /**
     * Returns an annotation instance by the full qualified name of the
     * annotation.
     * 
     * @param annotable
     *            Annotated element.
     * @param fullQualifiedName
     *            Full qualified name of the annotation class to find.
     * 
     * @return Instance or <code>null</code> if the annotation was not found.
     */
    public static AnnotationInstance getAnnotationInstance(
            final AnnotableAndModifiable annotable,
            final String fullQualifiedName) {

        assertArgNotNull("annotable", annotable);
        assertNoProxy(annotable);
        assertArgNotNull("fullQualifiedName", fullQualifiedName);

        final EList<AnnotationInstanceOrModifier> aams = annotable
                .getAnnotationsAndModifiers();
        for (final AnnotationInstanceOrModifier aam : aams) {
            if (aam instanceof AnnotationInstance) {
                final AnnotationInstance ai = (AnnotationInstance) aam;
                final Classifier annotation = ai.getAnnotation();
                if (annotation == null) {
                    throw new IllegalStateException("Annotation is null: " + ai);
                }
                if (annotation.eIsProxy()) {
                    throw new IllegalStateException(
                            "Annotation is unresolved: " + annotation);
                }
                final String fqn = getFullQualifiedName(annotation);
                if (fqn.equals(fullQualifiedName)) {
                    return ai;
                }

            }
        }
        return null;
    }

    /**
     * Returns the "value()" from an annotation that has only a single parameter
     * of type interface.
     * 
     * @param annotationInstance
     *            Annotation instance to return the value from.
     * @param targetClass
     *            Type of the referenced element to find.
     * 
     * @return Interface or NULL if the annotation has no parameter at all, more
     *         than one parameter or it's not type interface.
     * 
     * @param <TARGET>
     *            Returned type.
     */
    @SuppressWarnings("unchecked")
    public static <TARGET extends ReferenceableElement> TARGET getSingleAnnotationRefElementParameter(
            final AnnotationInstance annotationInstance,
            final java.lang.Class<TARGET> targetClass) {

        assertArgNotNull("annotationInstance", annotationInstance);
        assertNoProxy(annotationInstance);

        final AnnotationValue value = getSingleAnnotationParameterValue(annotationInstance);
        if (value instanceof IdentifierReference) {
            final IdentifierReference ir = (IdentifierReference) value;
            final ReferenceableElement target = ir.getTarget();
            if (targetClass.isAssignableFrom(target.getClass())) {
                return (TARGET) target;
            }
        }
        return null;
    }

    /**
     * Returns the "value()" from an annotation that has only a single
     * parameter.
     * 
     * @param annotationInstance
     *            Annotation instance to return the value from.
     * 
     * @return The value or NULL if the annotation has no parameter at all or
     *         more than one parameter.
     */
    public static AnnotationValue getSingleAnnotationParameterValue(
            final AnnotationInstance annotationInstance) {

        assertArgNotNull("annotationInstance", annotationInstance);
        assertNoProxy(annotationInstance);

        final AnnotationParameter param = annotationInstance.getParameter();
        if (param instanceof SingleAnnotationParameter) {
            final SingleAnnotationParameter sap = (SingleAnnotationParameter) param;
            return sap.getValue();
        }
        return null;
    }

    /**
     * Returns the mixin interface that a mixin provider class implements.
     * Inspects the {@link MixinProvider} annotation and returns the value
     * parameter from it.
     * 
     * @param mixinProviderClass
     *            Class that provides the mixin.
     * 
     * @return Mixin interface or <code>null</code> if the given class has no
     *         appropriate annotation or an incorrect value.
     */
    public static Interface getMixinInterface(
            final ConcreteClassifier mixinProviderClass) {

        assertArgNotNull("mixinProviderClass", mixinProviderClass);
        assertNoProxy(mixinProviderClass);

        final AnnotationInstance ai = getAnnotationInstance(mixinProviderClass,
                MixinProvider.class.getName());
        if (ai == null) {
            return null;
        }
        return getSingleAnnotationRefElementParameter(ai, Interface.class);

    }

    /**
     * Removes all fields and methods annotated with {@link MixinGenerated} from
     * the given class.
     * 
     * @param mixinUserClass
     *            Class to remove the mixin code from.
     */
    public static void removeAllMixinMembers(final Class mixinUserClass) {

        assertArgNotNull("mixinUserClass", mixinUserClass);
        assertNoProxy(mixinUserClass);

        // Remove fields and methods
        final List<Member> toRemove = new ArrayList<Member>();
        for (Member member : mixinUserClass.getMembers()) {
            if (member instanceof AnnotableAndModifiable) {
                final AnnotableAndModifiable annotable = (AnnotableAndModifiable) member;
                final AnnotationInstance ai = getAnnotationInstance(annotable,
                        MixinGenerated.class.getName());
                if (ai != null) {
                    toRemove.add(member);
                }
            }
        }
        mixinUserClass.getMembers().removeAll(toRemove);

        // Remove import
        removeClassifierImport(mixinUserClass.getContainingCompilationUnit(),
                mixinUserClass.getConcreteClassifier(MixinGenerated.class
                        .getName()));

    }

    /**
     * Adds mixin code to the given class.
     * 
     * @param mixinUser
     *            Class to add the mixin code to.
     * @param mixinProvider
     *            Class with fields and methods to copy.
     * @param mixinInterface
     *            Mixin interface.
     * @param log
     *            Log for errors or warnings.
     */
    public static void applyMixin(final Class mixinUser,
            final Class mixinProvider, final Interface mixinInterface,
            final SrcMixins4JAnalyzerLog log) {

        assertArgNotNull("mixinUser", mixinUser);
        assertArgNotNull("mixinProvider", mixinProvider);
        assertArgNotNull("mixinInterface", mixinInterface);
        assertArgNotNull("log", log);
        assertNoProxy(mixinUser);
        assertNoProxy(mixinProvider);
        assertNoProxy(mixinInterface);

        final List<Field> fieldsToAdd = createListOfFieldsToAdd(mixinUser,
                mixinProvider, log);

        final List<Method> methodsToAdd = createListOfMethodsToAdd(mixinUser,
                mixinProvider, log);

        if ((fieldsToAdd.size() > 0) || (methodsToAdd.size() > 0)) {

            // Create mapping for generic type parameters
            final List<TypeParam2Type> typeParam2ArgList = createTypeParam2ArgMapping(
                    mixinInterface, mixinUser);

            // Add import
            addClassifierImport(mixinUser.getContainingCompilationUnit(),
                    mixinUser.getConcreteClassifier(MixinGenerated.class
                            .getName()));

            // Find the position to insert fields
            final List<Member> members = mixinUser.getMembers();
            int fieldStartIdx = 0;
            for (Member member : members) {
                if (member instanceof Field) {
                    fieldStartIdx++;
                }
            }

            // Add fields
            for (final Field field : fieldsToAdd) {
                final Field newField = EcoreUtil.copy(field);
                replaceGenerics(newField, typeParam2ArgList);
                mixinUser.getMembers().add(fieldStartIdx, newField);
            }

            // Methods are added after fields
            for (final Method method : methodsToAdd) {
                final Method newMethod = EcoreUtil.copy(method);
                replaceGenerics(newMethod, typeParam2ArgList);
                mixinUser.getMembers().add(members.size(), newMethod);
            }

        }

    }

    /**
     * Creates a list of fields to add by comparing the fields of the provider
     * and the fields in the mixin user class.
     * 
     * @param mixinUser
     *            User to add fields to.
     * @param mixinProvider
     *            Provider with fields to copy.
     * @param log
     *            Log for errors or warnings.
     * 
     * @return List of fields that are in the provider but not in the user
     *         class.
     */
    private static List<Field> createListOfFieldsToAdd(final Class mixinUser,
            final Class mixinProvider, final SrcMixins4JAnalyzerLog log) {

        final List<Field> fieldsToAdd = new ArrayList<Field>();
        final List<Field> providerFields = mixinProvider.getFields();
        for (final Field providerField : providerFields) {

            final Field userField = findFieldByName(mixinUser,
                    providerField.getName());
            final Class existingProviderClass = getProviderClass(userField);
            final boolean userHasAlreadySameField = userField != null;
            if (userHasAlreadySameField && (existingProviderClass != null)
                    && (existingProviderClass != mixinProvider)) {

                log.addError(mixinUser, "Field '" + userField.getName()
                        + "' provided by more than one mixin: "
                        + getFullQualifiedName(mixinProvider) + " / "
                        + getFullQualifiedName(existingProviderClass));

            } else {

                final AnnotationInstance providerAI = getAnnotationInstance(
                        providerField, MixinGenerated.class.getName());
                final boolean providerFieldHasMixinGeneratedAnnotation = providerAI != null;

                if (providerFieldHasMixinGeneratedAnnotation
                        && !userHasAlreadySameField) {
                    fieldsToAdd.add(providerField);
                }

            }

        }
        return fieldsToAdd;

    }

    /**
     * Creates a list of methods to add by comparing the methods of the provider
     * and the methods in the mixin user class.
     * 
     * @param mixinUser
     *            User to add methods to.
     * @param mixinProvider
     *            Provider with methods to copy.
     * @param log
     *            Log for errors or warnings.
     * 
     * @return List of methods that are in the provider but not in the user
     *         class.
     */
    private static List<Method> createListOfMethodsToAdd(final Class mixinUser,
            final Class mixinProvider, final SrcMixins4JAnalyzerLog log) {

        final List<Method> methodsToAdd = new ArrayList<Method>();
        final List<Method> providerMethods = mixinProvider.getMethods();
        for (final Method providerMethod : providerMethods) {

            final Method userMethod = findMethodBySignature(mixinUser,
                    providerMethod.getName(), providerMethod.getParameters());
            final Class existingProviderClass = getProviderClass(userMethod);
            final boolean userHasAlreadySameMethod = userMethod != null;
            if (userHasAlreadySameMethod && (existingProviderClass != null)
                    && (existingProviderClass != mixinProvider)) {

                log.addError(mixinUser, "Method '"
                        + getSignatureString(userMethod)
                        + "' provided by more than one mixin: "
                        + getFullQualifiedName(mixinProvider) + " / "
                        + getFullQualifiedName(existingProviderClass));

            } else {

                final AnnotationInstance providerAI = getAnnotationInstance(
                        providerMethod, MixinGenerated.class.getName());
                final boolean methodHasMixinGeneratedAnnotation = providerAI != null;

                if (methodHasMixinGeneratedAnnotation
                        && !userHasAlreadySameMethod) {
                    methodsToAdd.add(providerMethod);
                }

            }

        }
        return methodsToAdd;

    }

    /**
     * Returns the signature of the given method as string.
     * 
     * @param method Method to return the signature string for.
     * 
     * @return Text representation of the method containing name and parameter types.
     */
    public static String getSignatureString(final Method method) {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTypeName(method.getTypeReference().getTarget()));
        sb.append(" ");
        sb.append(method.getName());
        sb.append("(");
        final List<Parameter> params = method.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            final Parameter param = params.get(i);
            final Type type = param.getTypeReference().getTarget();
            sb.append(getTypeName(type));
        }
        sb.append(")");
        return sb.toString();
    }

    // CHECKSTYLE:OFF Cyclomatic complexity is not nice, but OK here...
    // TODO Should be in JaMoPP code base method of 'Type'...
    private static String getTypeName(final Type type) {
        if (type instanceof PrimitiveType) {
            if (type instanceof org.emftext.language.java.types.Boolean) {
                return "boolean";
            } else if (type instanceof org.emftext.language.java.types.Byte) {
                return "byte";
            } else if (type instanceof org.emftext.language.java.types.Char) {
                return "char";
            } else if (type instanceof org.emftext.language.java.types.Double) {
                return "double";
            } else if (type instanceof org.emftext.language.java.types.Float) {
                return "float";
            } else if (type instanceof org.emftext.language.java.types.Int) {
                return "int";
            } else if (type instanceof org.emftext.language.java.types.Long) {
                return "long";
            } else if (type instanceof org.emftext.language.java.types.Short) {
                return "short";
            } else if (type instanceof org.emftext.language.java.types.Void) {
                return "void";
            }
        } else if (type instanceof Classifier) {
            final Classifier classifier = (Classifier) type;
            final String name = getFullQualifiedName(classifier);
            if (name.equals("java.lang.String")) {
                return "String";
            }
            return name;
        } else if (type instanceof AnonymousClass) {
            final AnonymousClass clasz = (AnonymousClass) type;
            // TODO Find better way to display anonymous class
            return clasz.toString();
        }
        throw new IllegalStateException("Unknown type: " + type);
    }
    // CHECKSTYLE:ON
    
    
    /**
     * Returns the provider class value from a {@link MixinGenerated}
     * annotation.
     * 
     * @param annotableAndModifiable
     *            Field or method that may have a {@link MixinGenerated}
     *            annotation.
     * 
     * @return Mixin provider class or NULL if the annotation is not present.
     */
    public static Class getProviderClass(
            final AnnotableAndModifiable annotableAndModifiable) {
        if (annotableAndModifiable == null) {
            return null;
        }
        final AnnotationInstance userAI = getAnnotationInstance(
                annotableAndModifiable, MixinGenerated.class.getName());
        if (userAI == null) {
            return null;
        }
        return getSingleAnnotationRefElementParameter(userAI, Class.class);
    }

    /**
     * Returns a method by the signature of a given method.
     * 
     * @param memberContainer
     *            Has list of methods.
     * @param methodName
     *            Name of the method to find.
     * @param params
     *            Parameter list.
     * 
     * @return If a method with the same signature (name and parameter types)
     *         exists it's returned, else <code>null</code>.
     */
    public static Method findMethodBySignature(
            final MemberContainer memberContainer, final String methodName,
            final List<Parameter> params) {

        assertArgNotNull("memberContainer", memberContainer);
        assertArgNotNull("methodName", methodName);
        assertArgNotNull("params", params);
        assertNoProxy(memberContainer);

        final List<Method> methods = memberContainer.getMethods();
        for (final Method found : methods) {
            if (found.getName().equals(methodName)
                    && sameParameters(found.getParameters(), params)) {
                return found;
            }
        }

        return null;
    }

    /**
     * Checks if two parameter lists have the same number, order and types of
     * arguments. Parameter names are NOT checked (only types).
     * 
     * @param paramsA
     *            First parameter list.
     * @param paramsB
     *            Second parameter list.
     * 
     * @return If both arguments are the same TRUE, else FALSE.
     */
    public static boolean sameParameters(final List<Parameter> paramsA,
            final List<Parameter> paramsB) {

        assertArgNotNull("paramsA", paramsA);
        assertArgNotNull("paramsB", paramsB);

        if (paramsA.size() != paramsB.size()) {
            return false;
        }

        for (int i = 0; i < paramsA.size(); i++) {
            final Parameter paramA = paramsA.get(i);
            final Parameter paramB = paramsB.get(i);
            final Type typeA = paramA.getTypeReference().getTarget();
            final Type typeB = paramB.getTypeReference().getTarget();
            if (!sameType(typeA, typeB)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if two types are the same. Classifiers and primitive types are
     * compared by their full qualified name. Anonymous classes by their equals
     * method.
     * 
     * @param typeA
     *            First type.
     * @param typeB
     *            Second type.
     * 
     * @return If both types are equal TRUE, else FALSE.
     */
    public static boolean sameType(final Type typeA, final Type typeB) {

        assertArgNotNull("typeA", typeA);
        assertArgNotNull("typeB", typeB);
        assertNoProxy(typeA);
        assertNoProxy(typeB);

        if (typeA instanceof PrimitiveType) {
            if (!(typeB instanceof PrimitiveType)) {
                return false;
            }
            final Class classA = ((PrimitiveType) typeA).wrapPrimitiveType();
            final Class classB = ((PrimitiveType) typeB).wrapPrimitiveType();
            return getFullQualifiedName(classA).equals(
                    getFullQualifiedName(classB));
        } else if (typeA instanceof Classifier) {
            if (!(typeB instanceof Classifier)) {
                return false;
            }
            final Classifier classifierA = (Classifier) typeA;
            final Classifier classifierB = (Classifier) typeB;
            return getFullQualifiedName(classifierA).equals(
                    getFullQualifiedName(classifierB));
        } else if (typeA instanceof AnonymousClass) {
            if (!(typeB instanceof AnonymousClass)) {
                return false;
            }
            // TODO Does this make sense?
            return typeA.equals(typeB);
        } else {
            throw new IllegalStateException("Unknown type: " + typeA);
        }
    }

    /**
     * Returns a field by it's name.
     * 
     * @param memberContainer
     *            Has list of fields.
     * @param fieldName
     *            Name of the field to find.
     * 
     * @return If a field with that name exists it's returned, else
     *         <code>null</code>.
     */
    public static Field findFieldByName(final MemberContainer memberContainer,
            final String fieldName) {

        assertArgNotNull("memberContainer", memberContainer);
        assertArgNotNull("fieldName", fieldName);
        assertNoProxy(memberContainer);

        final List<Field> fields = memberContainer.getFields();
        for (final Field field : fields) {
            if ((field.getName() != null) & field.getName().equals(fieldName)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Checks if a classifier import already exists.
     * 
     * @param importingElement
     *            Element to inspect.
     * @param concreteClassifier
     *            Import to find.
     * 
     * @return If the import exists TRUE, else FALSE.
     */
    public static boolean containsClassifierImport(
            final ImportingElement importingElement,
            final ConcreteClassifier concreteClassifier) {

        assertArgNotNull("importingElement", importingElement);
        assertArgNotNull("concreteClassifier", concreteClassifier);
        assertNoProxy(importingElement);
        assertNoProxy(concreteClassifier);

        final String fqn = getFullQualifiedName(concreteClassifier);
        final EList<Import> imports = importingElement.getImports();
        for (final Import imp : imports) {
            if (imp instanceof ClassifierImport) {
                final ClassifierImport ci = (ClassifierImport) imp;
                final String fqnImport = getFullQualifiedName(ci
                        .getClassifier());
                if (fqn.equals(fqnImport)) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Adds a classifier import to an element (if it not already exists).
     * 
     * @param importingElement
     *            Element to add an import to.
     * @param concreteClassifier
     *            Classifier to import.
     */
    public static void addClassifierImport(
            final ImportingElement importingElement,
            final ConcreteClassifier concreteClassifier) {

        assertArgNotNull("importingElement", importingElement);
        assertArgNotNull("concreteClassifier", concreteClassifier);
        assertNoProxy(importingElement);
        assertNoProxy(concreteClassifier);

        final ClassifierImport toAdd = ImportsFactory.eINSTANCE
                .createClassifierImport();
        toAdd.getNamespaces().addAll(
                concreteClassifier.getContainingCompilationUnit()
                        .getNamespaces());
        toAdd.setClassifier(concreteClassifier);

        if (!containsClassifierImport(importingElement, concreteClassifier)) {
            importingElement.getImports().add(toAdd);
        }

    }

    /**
     * Removes a classifier import from an element (if the import exists).
     * 
     * @param importingElement
     *            Element to remove the import from.
     * @param concreteClassifier
     *            Classifier import to remove.
     */
    public static void removeClassifierImport(
            final ImportingElement importingElement,
            final ConcreteClassifier concreteClassifier) {

        assertArgNotNull("compilationUnit", importingElement);
        assertArgNotNull("concreteClassifier", concreteClassifier);
        assertNoProxy(importingElement);
        assertNoProxy(concreteClassifier);

        final String fqn = getFullQualifiedName(concreteClassifier);

        final EList<Import> imports = importingElement.getImports();
        final Iterator<Import> it = imports.iterator();
        while (it.hasNext()) {
            final Import imp = it.next();
            if (imp instanceof ClassifierImport) {
                final ClassifierImport ci = (ClassifierImport) imp;
                final String fqnImport = getFullQualifiedName(ci
                        .getClassifier());
                if (fqn.equals(fqnImport)) {
                    it.remove();
                }
            }
        }

    }

    /**
     * Returns the package from the containing compilation unit and the name.
     * 
     * @param classifier
     *            Classifier to return a FQN for.
     * 
     * @return Full qualified name of the given classifier.
     */
    public static String getFullQualifiedName(final Classifier classifier) {

        assertArgNotNull("classifier", classifier);
        assertNoProxy(classifier);

        return classifier.getContainingCompilationUnit()
                .getNamespacesAsString() + classifier.getName();

    }

    /**
     * Returns the compilation unit from the resource.
     * 
     * @param resource
     *            Resource.
     * 
     * @return Compilation unit or <code>null</code> if the resource contains to
     *         compilation unit.
     */
    public static CompilationUnit getCompilationUnit(final Resource resource) {

        assertArgNotNull("resource", resource);

        final EList<EObject> contents = resource.getContents();
        for (final EObject eObj : contents) {
            if (eObj instanceof CompilationUnit) {
                return (CompilationUnit) eObj;
            }
        }
        return null;

    }

    /**
     * Creates mapping of interface type parameters to type arguments in the
     * implementing class.
     * 
     * @param mixinInterface
     *            Interface with type arguments.
     * @param mixinUserClass
     *            Class implementing the interface.
     * 
     * @return List of mapped parameters.
     */
    public static List<TypeParam2Type> createTypeParam2ArgMapping(
            final Interface mixinInterface, final Class mixinUserClass) {

        final List<TypeParam2Type> list = new ArrayList<TypeParam2Type>();

        // Get type parameter from interface
        final EList<TypeParameter> typeParameters = mixinInterface
                .getTypeParameters();
        final EList<TypeParameter> typeParams = typeParameters;
        for (int i = 0; i < typeParams.size(); i++) {
            list.add(new TypeParam2Type(typeParams.get(i)));
        }

        // Get type arguments from implementation
        final List<TypeReference> intfs = mixinUserClass.getImplements();
        for (final TypeReference intfRef : intfs) {
            if (intfRef.getTarget().equals(mixinInterface)
                    && (intfRef instanceof NamespaceClassifierReference)) {
                final NamespaceClassifierReference ncr = (NamespaceClassifierReference) intfRef;
                final EList<ClassifierReference> crefs = ncr
                        .getClassifierReferences();
                if (crefs.size() != 1) {
                    throw new UnsupportedOperationException(
                            "Multiple classifier references are unsupported: "
                                    + crefs);
                }
                final ClassifierReference cref = crefs.get(0);
                final EList<TypeArgument> typeArguments = cref
                        .getTypeArguments();
                for (int i = 0; i < typeArguments.size(); i++) {
                    final TypeArgument typeArgument = typeArguments.get(i);
                    final TypeParam2Type tp2arg = list.get(i);
                    // Only replacing qualified type arguments makes sense
                    if (typeArgument instanceof QualifiedTypeArgument) {
                        final QualifiedTypeArgument qta = (QualifiedTypeArgument) typeArgument;
                        tp2arg.setArg(qta);
                    } else {
                        tp2arg.setArg(null);
                    }
                }
                break;
            }
        }

        return list;
    }

    /**
     * Replaces all occurrences of all type parameters in the list in the given
     * object and it's children.
     * 
     * @param eObj
     *            Object to replace type parameters within.
     * @param typeParam2ArgList
     *            List of type parameters and their replacement.
     */
    public static void replaceGenerics(final EObject eObj,
            final List<TypeParam2Type> typeParam2ArgList) {

        if (typeParam2ArgList.size() == 0) {
            return;
        }

        final TreeIterator<EObject> it = eObj.eAllContents();
        while (it.hasNext()) {
            final EObject eo = it.next();
            if (eo instanceof NamespaceClassifierReference) {

                final List<ClassifierReference> newClRefs = new ArrayList<ClassifierReference>();

                final NamespaceClassifierReference ncr = (NamespaceClassifierReference) eo;
                final EList<ClassifierReference> refs = ncr
                        .getClassifierReferences();
                final Iterator<ClassifierReference> refIt = refs.iterator();
                while (refIt.hasNext()) {
                    final ClassifierReference ref = refIt.next();
                    if (ref.getTarget() instanceof TypeParameter) {
                        final TypeParameter tp = (TypeParameter) ncr
                                .getTarget();
                        final TypeParam2Type p2t = findMapping(tp.getName(),
                                typeParam2ArgList);
                        if ((p2t != null) && (p2t.getArgType() != null)) {
                            final NamespaceClassifierReference argNcr = (NamespaceClassifierReference) p2t
                                    .getArg().getTypeReference();
                            final EList<ClassifierReference> argClRefs = argNcr
                                    .getClassifierReferences();
                            for (final ClassifierReference argClRef : argClRefs) {
                                final ClassifierReference newArgClRef = EcoreUtil
                                        .copy(argClRef);
                                newArgClRef.getLayoutInformations().clear();
                                newClRefs.add(newArgClRef);
                            }
                        } else {
                            newClRefs.add(ref);
                        }
                    }
                }

                ncr.getClassifierReferences().clear();
                ncr.getClassifierReferences().addAll(newClRefs);

            }
        }

    }

    /**
     * Returns the mapping for a given parameter by it's name.
     * 
     * @param typeParamName
     *            Name of the parameter to find.
     * @param list
     *            List with known mappings.
     * 
     * @return Mapping element if the parameter was found, else NULL.
     */
    public static TypeParam2Type findMapping(final String typeParamName,
            final List<TypeParam2Type> list) {
        for (final TypeParam2Type p2t : list) {
            if (p2t.getParam().getName().equals(typeParamName)) {
                return p2t;
            }
        }
        return null;
    }

    /**
     * Find all classes that provide code for a mixin interface.
     * 
     * @param resourceSet
     *            Resource set to use.
     * 
     * @return List of mixin providers.
     */
    public static List<Class> findMixinProviders(final ResourceSet resourceSet) {

        assertArgNotNull("resourceSet", resourceSet);

        final List<Class> classes = new ArrayList<Class>();
        final TreeIterator<Notifier> it = resourceSet.getAllContents();
        while (it.hasNext()) {
            final Notifier notifier = it.next();
            if (notifier instanceof Class) {
                final Class clasz = (Class) notifier;
                if (getAnnotationInstance(clasz, MixinProvider.class.getName()) != null) {
                    classes.add(clasz);
                }
            }
        }
        return classes;
    }

    /**
     * Returns a list of all classes that directly implement an interface.
     * 
     * @param resourceSet
     *            Resource set to use.
     * @param intf
     *            Interface to find the implementing classes for.
     * 
     * @return List of classes that implement the interface.
     */
    public static List<Class> findImplementors(final ResourceSet resourceSet,
            final Interface intf) {

        assertArgNotNull("resourceSet", resourceSet);
        assertArgNotNull("intf", intf);
        assertNoProxy(intf);

        final List<Class> classes = new ArrayList<Class>();
        final TreeIterator<Notifier> it = resourceSet.getAllContents();
        while (it.hasNext()) {
            final Notifier notifier = it.next();
            if (notifier instanceof Class) {
                final Class clasz = (Class) notifier;
                final EList<ClassifierReference> superTypes = clasz
                        .getSuperTypeReferences();
                for (ClassifierReference superTypeRef : superTypes) {
                    if (superTypeRef.getTarget().equals(intf)) {
                        classes.add(clasz);
                    }
                }
            }
        }
        return classes;
    }

    private static void assertArgNotNull(final String argName,
            final Object argValue) {
        if (argValue == null) {
            throw new IllegalArgumentException("Argument '" + argName
                    + "' cannot be null");
        }
    }

    private static void assertNoProxy(final EObject eObj) {
        if (eObj.eIsProxy()) {
            throw new IllegalStateException("Unresolved: " + eObj);
        }
    }

    /**
     * Helper class to store mixin interface and provider together.
     */
    public static final class MixinInfo {

        private final Class provider;

        private final Interface intf;

        /**
         * Constructor with provider and interface.
         * 
         * @param provider
         *            Provides the mixin functionality.
         * @param intf
         *            Defines the mixin interface.
         */
        public MixinInfo(final Class provider, final Interface intf) {
            super();
            this.provider = provider;
            this.intf = intf;
        }

        /**
         * Returns the mixin provider.
         * 
         * @return Provider class.
         */
        public final Class getProvider() {
            return provider;
        }

        /**
         * Returns the mixin interface.
         * 
         * @return Interface.
         */
        public final Interface getInterface() {
            return intf;
        }

    }

    /**
     * Mapping from type parameter to type argument.
     */
    public static final class TypeParam2Type {

        private final TypeParameter param;

        private QualifiedTypeArgument arg;

        /**
         * Constructor with parameter.
         * 
         * @param param
         *            Type parameter - Cannot be NULL.
         */
        public TypeParam2Type(final TypeParameter param) {
            super();
            this.param = param;
        }

        /**
         * Returns the type parameter.
         * 
         * @return Type parameter.
         */
        public final TypeParameter getParam() {
            return param;
        }

        /**
         * Returns the type argument.
         * 
         * @return Type argument.
         */
        public final QualifiedTypeArgument getArg() {
            return arg;
        }

        /**
         * Sets the type argument to a new value.
         * 
         * @param arg
         *            Type argument to set.
         */
        public final void setArg(final QualifiedTypeArgument arg) {
            if ((arg != null)
                    && (arg.getTypeReference() != null)
                    && (arg.getTypeReference().getTarget() instanceof TypeParameter)) {
                final TypeParameter tp = (TypeParameter) arg.getTypeReference()
                        .getTarget();
                if (param.getName().equals(tp.getName())) {
                    this.arg = null;
                }
            } else {
                this.arg = arg;
            }
        }

        /**
         * Returns the referenced type of the argument.
         * 
         * @return Type or NULL if the parameter is not replaced.
         */
        public final Classifier getArgType() {
            if ((arg == null) || (arg.getTypeReference() == null)) {
                return null;
            }
            if (!(arg.getTypeReference().getTarget() instanceof Classifier)) {
                return null;
            }
            return (Classifier) arg.getTypeReference().getTarget();
        }

        /**
         * Checks if the parameter is replaced by an argument.
         * 
         * @return If the parameter is replaced TRUE, else FALSE.
         */
        public final boolean isParamReplaced() {
            return getArgType() != null;
        }

        @Override
        public final String toString() {
            if (arg == null) {
                return param.getName() + " => " + param.getName();
            }
            return param.getName() + " => " + getArgType();
        }

    }

    /**
     * Loads all resources in a directory and it's sub directories.
     * 
     * @param resourceSet
     *            Resource set to use.
     * @param dir
     *            Directory to parse.
     * 
     * @throws IOException
     *             Error parsing the resources.
     */
    public static void loadResources(final ResourceSet resourceSet,
            final File dir) throws IOException {
        final File[] files = dir.listFiles();
        for (final File file : files) {
            if (file.isFile()) {
                resourceSet.getResource(
                        URI.createFileURI(file.getCanonicalPath()), true);
            } else {
                loadResources(resourceSet, file);
            }
        }
    }

    /**
     * Returns a list of all java files in a directory and it's sub
     * directories..
     * 
     * @param dir
     *            Directory to scan for java files.
     * 
     * @return List of all java files.
     */
    public static List<File> findRecursiveAllJavaFiles(final File dir) {

        final List<File> files = new ArrayList<File>();

        final File[] found = dir.listFiles(new FileFilter() {
            @Override
            public final boolean accept(final File file) {
                return file.isDirectory() || file.getName().endsWith(".java");
            }
        });

        for (final File file : found) {
            if (file.isDirectory()) {
                files.addAll(findRecursiveAllJavaFiles(file));
            } else {
                files.add(file);
            }
        }

        return files;

    }

}
