package io.github.techbox.core.modules

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module(val name: String = "")
