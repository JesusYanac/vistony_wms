package com.vistony.wms.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vistony.wms.ui.theme.RedVistony
import com.vistony.wms.viewmodel.TransferStockViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Editext(
    status: Boolean,
    text: MutableState<String>,
    placeholder:String,
    label:String,
    painter: Painter,
    keyboardType: KeyboardType,
    limitCharacters: Int
){
    val keyboardController = LocalSoftwareKeyboardController.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(10.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            enabled= status,
            singleLine=false,
            value = text.value,
            onValueChange =
            {
                if (it.length <= limitCharacters) {
                    text.value = it
                }
            },
            placeholder = {
                Text(text = placeholder)
            },
            label = { Text(label) },
            //trailingIcon = { Icon(painter = painter
//painterResource(id = R.drawable.ic_insert_comment_black_24dp)
            //    , contentDescription = null, tint = Color.Red) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType,imeAction = ImeAction.Go ),
            keyboardActions = KeyboardActions(
                onGo = {
                    keyboardController?.hide()
                },
            ),
            modifier = Modifier.fillMaxWidth()
        )
        /*Text(
            text = "${text.value.length}/$limitCharacters",
            style = MaterialTheme.typography.caption,
            color = if (text.value.length > limitCharacters) Color.Red else Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp,0.dp)
        )*/
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Editext2(
    status: Boolean,
    text: MutableState<String>,
    placeholder: String,
    label: String,
    painter: Painter,
    keyboardType: KeyboardType,
    limitCharacters: Int,
    transferStockViewModel: TransferStockViewModel
){
    val keyboardController = LocalSoftwareKeyboardController.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(10.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            enabled= status,
            singleLine=false,
            value = text.value,
            onValueChange =
            {
                if (it.isEmpty() || (it.length <= limitCharacters && it.matches(Regex("\\d+")))) {
                    text.value = it
                    transferStockViewModel.setAmount(it)
                }

            },
            placeholder = {
                Text(text = placeholder)
            },
            label = { Text(label) },
            //trailingIcon = { Icon(painter = painter
//painterResource(id = R.drawable.ic_insert_comment_black_24dp)
            //    , contentDescription = null, tint = Color.Red) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType,imeAction = ImeAction.Go ),
            keyboardActions = KeyboardActions(
                onGo = {
                    keyboardController?.hide()
                },
            ),
            modifier = Modifier.fillMaxWidth()
        )
        /*Text(
            text = "${text.value.length}/$limitCharacters",
            style = MaterialTheme.typography.caption,
            color = if (text.value.length > limitCharacters) Color.Red else Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp,0.dp)
        )*/
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditextGeneric(
    status: Boolean,
    value: String,
    placeholder:String,
    label:String,
    painter: Painter,
    keyboardType:KeyboardType,
    statusMaxCharacter:Boolean=true,
    countMaxCharacter:Int=254,
    resultEditText: (String) -> Unit
){
    val keyboardController = LocalSoftwareKeyboardController.current
    //val maxCharacters = 254 // Establece el límite máximo de caracteres
    val text = remember { mutableStateOf(value) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(10.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            enabled= status,
            singleLine=false,
            value = text.value,
            onValueChange =
            {newText ->
                if (newText.length <= countMaxCharacter) {
                    when (keyboardType) {
                        KeyboardType.Decimal -> {
                            if ((newText.isNotEmpty() && newText.first() != '.') || newText.length > 1) {
                                val cleanedText = newText.filterIndexed { index, char ->
                                    char.isDigit() || (char == '.' && index == newText.indexOf('.'))
                                }
                                if (cleanedText.toDoubleOrNull() != null) {
                                    text.value = cleanedText
                                    resultEditText(cleanedText)
                                }
                            } else if (newText == ".") {
                                text.value = ""
                                resultEditText("")
                            }
                        }
                        else -> {
                            text.value = newText
                            resultEditText(newText)
                        }
                    }
                }
            },
            placeholder = {
                Text(text = placeholder, fontSize = 14.sp)
            },
            label = { Text(label, fontSize = 14.sp) },
            trailingIcon = { if (statusMaxCharacter) {
                Icon(painter = painter, contentDescription = null, tint = RedVistony)
            } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType,imeAction = ImeAction.Go ),
            keyboardActions = KeyboardActions(
                onGo = {
                    keyboardController?.hide()
                },
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (statusMaxCharacter)
        {
            Text(
                text = "${text.value.length}/$countMaxCharacter",
                style = MaterialTheme.typography.caption,
                color = if (text.value.length > countMaxCharacter) Color.Red else Color.Black,
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp, 0.dp)
            )
        }
    }
}