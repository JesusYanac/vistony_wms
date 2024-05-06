package test.asn

import android.util.Log
import com.google.android.gms.common.util.VisibleForTesting
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.vistony.wms.asn.mvvm.ASNEntity
import com.vistony.wms.asn.mvvm.ASNEntity2
import com.vistony.wms.asn.mvvm.ASNHeaderResponseEntity
import com.vistony.wms.asn.mvvm.ASNRepository
import com.vistony.wms.asn.mvvm.PreASN
import com.vistony.wms.asn.mvvm.PreASNEntity
import com.vistony.wms.asn.mvvm.convertPreASNEntityToASNEntity
import com.vistony.wms.asn.mvvm.dataPreASN
import com.vistony.wms.util.APIService
import junit.framework.TestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RunWith(JUnit4::class)
class ASNApiTest {
    // Mock del APIService
    private lateinit var mockApiService: APIService

    // Clase bajo prueba
    private lateinit var asnRepository: ASNRepository

    @Before
    fun setup() {
        // Inicializar el mock del APIService
        mockApiService = Mockito.mock(APIService::class.java)

        // Inicializar la clase bajo prueba con el mock del APIService
        asnRepository = ASNRepository()
    }

/*
    @Test
    @VisibleForTesting
    fun getDataPreASN_with_successful_response() {
        // Crear una instancia de Call simulada
        val call: Call<PreASNEntity> = Mockito.mock(Call::class.java) as Call<PreASNEntity>

        // Devolver la instancia simulada de Call
        Mockito.`when`(mockApiService.getDataPreASN(Mockito.anyString(), Mockito.anyString())).thenReturn(call)

        // Simular la respuesta del servidor
        val mockResponseData = "[\n" +
                "\t{\n" +
                "\t\t\"DocNum\": \"240002740\",\n" +
                "\t\t\"ItemCode\": \"1000017\",\n" +
                "\t\t\"ProdName\": \"MUTURROL DE 55 GAL\",\n" +
                "\t\t\"PlannedQty\": \"20.000000\",\n" +
                "\t\t\"CmpltQty\": \"22.000000\",\n" +
                "\t\t\"UgpCode\": \"CIL\",\n" +
                "\t\t\"Lote\": \"240002739\",\n" +
                "\t\t\"Date\": \"2025-03-01\",\n" +
                "\t\t\"QtyPallet\": \"4.000000\"\n" +
                "\t}\n" +
                "]"

        // Convertir la cadena JSON en una lista de objetos
        val typeToken = object : TypeToken<List<PreASN>>() {}.type
        val dataList: List<PreASN> = Gson().fromJson(mockResponseData, typeToken)

        val mockResponse = PreASNEntity(status = "Y", data = dataList)
        val response: Response<PreASNEntity> = Response.success(mockResponse)

        // Devolver la respuesta simulada cuando se llame a la instancia de Call
        Mockito.`when`(call.execute()).thenReturn(response)

        // Llamar al método bajo prueba
        asnRepository.getDataPreASN("7750804005789", "240002739")

        // Esperar un momento para que el valor se actualice
        runBlocking {
            delay(5000)
        }

        // Verificar que el resultado sea el esperado
        Assert.assertEquals(mockResponse, asnRepository.resultPreASN.value)
    }
*/

/*
    @Test
    fun sendDataASNPrint_success() {
        val asnEntities = ASNEntity2( asn = convertPreASNEntityToASNEntity(dataPreASN().last()).last() , ipAddress = "192.168.1.254")
        val gson = Gson()
        val jsonBodyString = gson.toJson(asnEntities)
        val jsonBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonBodyString
        )
        APIService.getInstance().sendDataASNPrint(jsonBody).enqueue(object :
            Callback<ASNHeaderResponseEntity> {
            override fun onResponse(
                call: Call<ASNHeaderResponseEntity?>,
                response: Response<ASNHeaderResponseEntity?>
            ) {
                // Verificar la respuesta
                println("sendDataASNPrint_success_response: "+response)
                val gson = Gson()
                val typeDispatchListJson = gson.toJson(response.body())
                //println("RetrofitAPITest_testGetHeaderDispatchSheet_success_mockResponse: $mockResponse")
                println("sendDataASNPrint_success_call: "+call)

                TestCase.assertNotNull(typeDispatchListJson)
            }
            override fun onFailure(call: Call<ASNHeaderResponseEntity>, t: Throwable) {
                // Manejar el fallo de la llamada
            }
        })
    }
*/

}