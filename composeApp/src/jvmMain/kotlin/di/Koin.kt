package di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

import org.koin.core.context.GlobalContext

fun initKoin(config: KoinAppDeclaration? = null) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            config?.invoke(this)
            modules(
                viewModelModules(),
                storageModules(),
                commandModules()
            )
        }
    }
}