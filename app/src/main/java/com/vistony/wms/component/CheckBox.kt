package com.vistony.wms.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vistony.wms.ui.theme.ColorRowDestine

@Composable
fun LabelledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(
        checkedColor = Color.Red,
        uncheckedColor = Color.Red.copy(alpha = 0.4f)
    )
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(ColorRowDestine).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){

        Checkbox(
            modifier=Modifier.padding(0.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors
        )

        Text(label)
    }
}