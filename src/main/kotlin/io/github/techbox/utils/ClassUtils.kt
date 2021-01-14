package io.github.techbox.utils

import io.github.classgraph.ClassGraph

fun lookForAnnotatedClassesOn(packageName: String, annotation: Class<out Annotation>): Set<Class<*>> {
    return ClassGraph()
        .acceptPackages(packageName)
        .enableAnnotationInfo()
        .scan(2)
        .allClasses.filter { it.hasAnnotation(annotation.name) }
        .map { it.loadClass() }
        .toSet()
}

