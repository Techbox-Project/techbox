package io.github.techbox.core.modules

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module(val name: String = "")
