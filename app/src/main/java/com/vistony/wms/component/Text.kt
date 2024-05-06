package com.vistony.wms.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextWithDivider(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = Color.Gray
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray
        )
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = Color.Gray
        )
    }
}


@Composable
fun Cell(
    text: String,
    title:Boolean=false,
    color: Color = Color.Black,
    textAlign: TextAlign = TextAlign.Center,
    fontSise: TextUnit = 15.sp
) {
    if (title) {
        Text(
            text = text,
            /*modifier =
            Modifier
                //.weight(weight)
                .padding(2.dp),*/
            fontSize = fontSise,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = textAlign,
            //style = Typography.amp,
            modifier = Modifier.testTag("cell")
        )
    } else {
        Text(
            text = text,
            /*modifier = Modifier
                //.weight(weight)
                .padding(2.dp),*/
            color = color,
            fontSize = fontSise,
            textAlign = textAlign,//, style = Typography.body1,
            modifier = Modifier.testTag("cell")
        )
    }
}