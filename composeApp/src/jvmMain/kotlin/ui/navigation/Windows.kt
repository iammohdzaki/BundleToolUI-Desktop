package ui.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    object SplashRoute

    @Serializable
    object HomeRoute

    @Serializable
    object SettingsRoute
}