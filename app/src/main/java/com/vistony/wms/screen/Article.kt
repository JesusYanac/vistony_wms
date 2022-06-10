package com.vistony.wms.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vistony.wms.component.TopBar
import com.vistony.wms.model.Article
import com.vistony.wms.viewmodel.ArticleViewModel
import io.realm.mongodb.sync.SyncConfiguration

@Composable
fun ArticleScreen(navController: NavHostController){

    val articleViewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModel.ArticleViewModelFactory("init")
    )

    val articleValue = articleViewModel.articles.collectAsState()

    Scaffold(
        topBar = {
            TopBar(title="Maestro de Artículos")
        }
    ){
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
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
                items(articleValue.value.listArticle){ article ->
                    formArticles(article)
                }
            }
    }
}

@Composable
private fun formArticles(article: Article){
    Card(
        elevation = 4.dp,
        modifier=Modifier.padding(10.dp).fillMaxWidth()
    ) {
        Column(
            modifier=Modifier.padding(10.dp)
        ){
            Text("${article.itemName} ")
            Text("Codigo: ${article.itemCode}",color=Color.Gray)
        }
    }
}