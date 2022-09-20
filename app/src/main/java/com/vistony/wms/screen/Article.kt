package com.vistony.wms.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.TopBar
import com.vistony.wms.model.Article
import com.vistony.wms.viewmodel.ArticleViewModel
import io.realm.mongodb.sync.SyncConfiguration

@Composable
fun ArticleScreen(navController: NavHostController,context: Context){

    val articleViewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModel.ArticleViewModelFactory("init")
    )

    val articleValue = articleViewModel.articles.collectAsState()

    Scaffold(
        topBar = {
            TopBar(title="Maestro de Artículos")
        }
    ){

        val textState = remember { mutableStateOf(TextFieldValue("")) }
//Place the composable SearchView wherever is needed

        val searchedText = textState.value.text


        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                item{
                    SearchView(state = textState, placeHolder = "")
                }
                item{
                    Row(
                        horizontalArrangement=Arrangement.SpaceBetween,
                        verticalAlignment=Alignment.CenterVertically,
                        modifier=Modifier.padding(10.dp).fillMaxWidth()

                    ){
                        Text("Número de artículos: ${articleValue.value.listArticle.size}",color= Color.Gray)
                        TextButton(
                            onClick = {
                                articleViewModel.getMasterDataArticle()
                            }
                        ){
                            Text("Actualizar")
                        }
                    }
                }
                items(items=articleValue.value.listArticle.filter {
                    it.itemCode.contains(searchedText,ignoreCase = true) ||
                    it.itemName.contains(searchedText,ignoreCase = true)
                }){ article ->
                        formArticles(article,context)
                    }
                }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun formArticles(article: Article,context: Context){
    Card(
        elevation = 4.dp,
        modifier=Modifier.padding(10.dp).fillMaxWidth(),
        onClick = {
            val urlStr = "https://wms.vistony.pe/vs1.0/Article/Photo?Name="+article.itemCode
            try{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(urlStr),"application/pdf")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(intent)
            }catch(e:Exception){
                Toast.makeText(context,"Ocurrio un error al abrir el adjunto\n${e.message}",Toast.LENGTH_SHORT).show()
            }

        }
    ) {
        Column(
            modifier=Modifier.padding(10.dp)
        ){
            Text("${article.itemName} ")
            Text("Codigo: ${article.itemCode}",color=Color.Gray)
            Text("Altura: ${article.height} Ancho: ${article.width} Largo: ${article.length}",color=Color.Gray)
        }
    }
}

@Composable
private fun SearchView(
    state: MutableState<TextFieldValue>,
    placeHolder: String
) {

    TextField(
        modifier=Modifier.fillMaxWidth(),
        value = state.value,
        onValueChange = { value ->
            state.value = value
        }
    )
}