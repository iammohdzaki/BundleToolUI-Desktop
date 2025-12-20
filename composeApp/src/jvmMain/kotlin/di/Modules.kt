package di

import command.ICommandExecutor
import command.OSProcessExecutor
import local.AppDataStore
import local.AppPreferences
import local.StorageProvider
import manager.FileOperationManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ui.windows.viewmodel.HomeViewModel
import usecase.CommandUseCase
import utils.files.FileUtilsImpl
import utils.files.IFileUtils

fun viewModelModules() = module {
    viewModel { HomeViewModel(get(), get()) }
}

fun storageModules() = module {
    single { StorageProvider.provideDataStore() }
    single { AppDataStore(get()) }
    single { AppPreferences(get()) }
}

fun commandModules() = module {
    // Low-level dependencies
    single<ICommandExecutor> { OSProcessExecutor() }
    single<IFileUtils> { FileUtilsImpl }
    single { FileOperationManager(get()) }

    // Use Case layer
    single { CommandUseCase(get(), get()) }
}