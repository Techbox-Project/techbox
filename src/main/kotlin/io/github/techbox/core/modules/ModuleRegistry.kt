package io.github.techbox.core.modules

import io.github.techbox.utils.logger


class ModuleRegistry {
    var modules: HashMap<String, ModuleProxy> = HashMap()

    fun register(module: Class<*>) {
        if (!module.isAnnotationPresent(Module::class.java)) {
            throw IllegalArgumentException("Class ${module.name} is not a valid module.")
        }

        var moduleName = module.getAnnotation(Module::class.java).name
        if (moduleName.isEmpty())
            moduleName = module.simpleName.substringBeforeLast("Kt")
        if (moduleName.endsWith("Module"))
            moduleName = moduleName.dropLast(6)

        val moduleProxy = ModuleProxy(moduleName, module)
        modules[moduleName] = moduleProxy
        moduleProxy.enable()
    }

    class ModuleProxy(private val name: String, val clazz: Class<*>) {
        private val log = logger<ModuleRegistry>()
        private var enabled = false

        fun enable() {
            try {
                val value = clazz.getMethod("onLoad").invoke(null)
                enabled = value !is Boolean || value
            } catch (e: Exception) {
                log.error("Caught error while loading module $name", e)
            }
        }
    }
}