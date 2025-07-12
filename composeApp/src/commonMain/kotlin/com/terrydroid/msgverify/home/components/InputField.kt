package com.terrydroid.msgverify.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.home.LinkVerificationState

internal fun LazyListScope.inputField(
    onVerifyClicked: (link: String) -> Unit,
    linkVerificationState: LinkVerificationState
) {
    item {
        val textFieldValue = remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(70f),
            value = textFieldValue.value,
            onValueChange = {
                textFieldValue.value = it
            },
            placeholder = {
                Text(text = "Enter Url you want to verify")
            },
            isError = linkVerificationState is LinkVerificationState.Error,
            label = {
                if (linkVerificationState is LinkVerificationState.Error) {
                    Text(linkVerificationState.errorMessage)
                } else {
                    Text("Input a url to verify")
                }
            },
            maxLines = 1,
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                Icon(imageVector = Icons.Default.Policy, contentDescription = null)
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                onVerifyClicked.invoke(textFieldValue.value)
            },
            enabled = textFieldValue.value.isNotEmpty()
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Verify"
            )
        }
    }

}
