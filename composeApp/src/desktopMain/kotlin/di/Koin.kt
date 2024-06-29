package di

import local.FileStorageHelper
import org.koin.dsl.module
import ui.screens.HomeViewModel

fun viewModelModules() = module {
    single { HomeViewModel() }
}

fun mainModules() = module {
    single { FileStorageHelper() }
}