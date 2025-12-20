package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.state.SigningMode

@Composable
fun SigningSection(
    signingMode: SigningMode,
    keystorePath: String,
    keystorePassword: String,
    keyAlias: String,
    keyPassword: String,
    onModeChange: (SigningMode) -> Unit,
    onPickKeystore: (String) -> Unit,
    onKeystorePasswordChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onKeyPasswordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🧾 Section title
        Text(
            text = "4. Signing Mode",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 🟣 Debug / Release selector
        OptionSelector(
            title = null,
            options = listOf(SigningMode.Debug, SigningMode.Release),
            selected = signingMode,
            onSelect = onModeChange,
            optionLabel = { it.name },
            modifier = Modifier.fillMaxWidth()
        )
        val borderColor = MaterialTheme.colorScheme.outline
        // 🔒 Release-only section with dotted border
        AnimatedVisibility(
            visible = signingMode == SigningMode.Release,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            DottedBorderBox {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Keystore Picker
                    FilePickerField(
                        label = "",
                        value = keystorePath,
                        placeholder = "Select Keystore Path",
                        dialogTitle = "Select Keystore File",
                        fileExtensionFilter = ".jks",
                        onPick = onPickKeystore
                    )

                    // Key Alias + Key Password
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StyledOutlinedTextField(
                            value = keyAlias,
                            onValueChange = onAliasChange,
                            placeholder = "Key Alias",
                            modifier = Modifier.weight(1f)
                        )

                        StyledOutlinedTextField(
                            value = keyPassword,
                            onValueChange = onKeyPasswordChange,
                            isPassword = true,
                            placeholder = "Key Password",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Keystore Password
                    StyledOutlinedTextField(
                        value = keystorePassword,
                        onValueChange = onKeystorePasswordChange,
                        placeholder = "Keystore Password",
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}



