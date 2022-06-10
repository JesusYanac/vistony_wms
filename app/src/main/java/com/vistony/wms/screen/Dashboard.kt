package com.vistony.wms.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.vistony.wms.R
import com.vistony.wms.component.*
import com.vistony.wms.model.LoginResponse
import com.vistony.wms.ui.theme.AzulVistony200
import com.vistony.wms.ui.theme.AzulVistony201
import com.vistony.wms.util.Routes
import com.vistony.wms.util.RoutesOptionDashboard
import io.realm.Realm
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun DashboardScreen(navController: NavHostController,user: LoginResponse,context: Context){

    BackHandler {}

    val modal = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, confirmStateChange = {false})
    val scope = rememberCoroutineScope()

    var currentBottomSheet: BottomSheetScreen? by remember {
        mutableStateOf(null)
    }

    val closeSheet: () -> Unit = {
        scope.launch {
            modal.hide()
        }
    }


    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            modal.animateTo(ModalBottomSheetValue.Expanded)
        }
    }

    ModalBottomSheetLayout(
        sheetState = modal,
        sheetContent = {
            Box(modifier = Modifier.defaultMinSize(minHeight = 1.dp)) {
                currentBottomSheet?.let { currentSheet ->
                    SheetLayout(currentSheet, closeSheet,showIconClose=true)
                }
            }
        }
    ){
        Scaffold(
            topBar = {
                TopBarDashboard(
                    title="Hola ${user.FirstName}",
                    navController = navController
                )
            }
        ) {

            DashboardSection(
                options = RoutesOptionDashboard,
                open= openSheet,
                close= closeSheet,
                user=user,
                context=context,
                navController=navController
            )

        }
    }
}

@ExperimentalFoundationApi
@Composable
fun DashboardSection(options: List<Routes>, user:LoginResponse, navController: NavHostController, context: Context, open: (BottomSheetScreen) -> Unit, close:() ->Unit) {

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Menu de opciones",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(start=20.dp,top=20.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Locación: ${user.Location.uppercase(Locale.ROOT)}",
            modifier = Modifier.padding(start=20.dp,bottom=20.dp),
            textAlign = TextAlign.Center,
            color=Color.Gray
        )

        LazyVerticalGrid(
            cells = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 7.5.dp, end = 7.5.dp,bottom = 100.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(options.size) { i ->
                CourseItem(
                    options=options[i],
                    onPress={ route ->
                        if(route == Routes.TaskManager.route){
                            Toast.makeText(context, "Esta opción no esta disponible.", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            navController.navigate(route)
                        }
                    }
                )
            }
        }
    }
}

private fun Path.standardQuadFromTo(from: Offset, to: Offset) {

    quadraticBezierTo(
        from.x,
        from.y,
        Math.abs(from.x + to.x) / 2f,
        Math.abs(from.y + to.y) / 2f
    )
}

@Composable
fun CourseItem(
    options: Routes,
    onPress:(String)->Unit
) {
    BoxWithConstraints(

        modifier = Modifier
            .padding(7.5.dp).clickable {
                onPress(options.route)
            }
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(AzulVistony201)
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        val mediumColoredPoint1 = Offset(0f, height * 0.3f)
        val mediumColoredPoint2 = Offset(width * 0.1f, height * 0.35f)
        val mediumColoredPoint3 = Offset(width * 0.4f, height * 0.05f)
        val mediumColoredPoint4 = Offset(width * 0.75f, height * 0.7f)
        val mediumColoredPoint5 = Offset(width * 1.4f, -height.toFloat())

        val mediumColoredPath = Path().apply {
            moveTo(mediumColoredPoint1.x, mediumColoredPoint1.y)
            standardQuadFromTo(mediumColoredPoint1, mediumColoredPoint2)
            standardQuadFromTo(mediumColoredPoint2, mediumColoredPoint3)
            standardQuadFromTo(mediumColoredPoint3, mediumColoredPoint4)
            standardQuadFromTo(mediumColoredPoint4, mediumColoredPoint5)
            lineTo(width.toFloat() + 100f, height.toFloat() + 100f)
            lineTo(-100f, height.toFloat() + 100f)
            close()
        }

        val lightPoint1 = Offset(0f, height * 0.35f)
        val lightPoint2 = Offset(width * 0.1f, height * 0.4f)
        val lightPoint3 = Offset(width * 0.3f, height * 0.35f)
        val lightPoint4 = Offset(width * 0.65f, height.toFloat())
        val lightPoint5 = Offset(width * 1.4f, -height.toFloat() / 3f)

        val lightColoredPath = Path().apply {
            moveTo(lightPoint1.x, lightPoint1.y)
            standardQuadFromTo(lightPoint1, lightPoint2)
            standardQuadFromTo(lightPoint2, lightPoint3)
            standardQuadFromTo(lightPoint3, lightPoint4)
            standardQuadFromTo(lightPoint4, lightPoint5)
            lineTo(width.toFloat() + 100f, height.toFloat() + 100f)
            lineTo(-100f, height.toFloat() + 100f)
            close()
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawPath(
                path = mediumColoredPath,
                color = Color.DarkGray
            )
            drawPath(
                path = lightColoredPath,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            Text(
                text = options.title,
                color=Color.White,
                style = MaterialTheme.typography.h6,
                lineHeight = 26.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Text(
                text = "Ver",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AzulVistony200)
                    .padding(vertical = 6.dp, horizontal = 15.dp)
            )
        }
    }
}

