package test.asn

import com.vistony.wms.asn.mvvm.ASNEntity
import com.vistony.wms.asn.mvvm.ConvertDeleteLPN
import com.vistony.wms.asn.mvvm.ConvertUpdateResultQuantityDetail
import com.vistony.wms.asn.mvvm.ConvertValidateStatusASN
import com.vistony.wms.asn.mvvm.ConvertValidateStatusHeadASN
import com.vistony.wms.asn.mvvm.ConvertValidateStatusPrintAssigned
import com.vistony.wms.asn.mvvm.ConvertaddDetailLpnCode
import com.vistony.wms.asn.mvvm.PreASN
import com.vistony.wms.asn.mvvm.convertPreASNEntityToASNEntity
import com.vistony.wms.asn.mvvm.countElements
import com.vistony.wms.asn.mvvm.dataPreASN
import com.vistony.wms.asn.mvvm.getDataBarCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ASNConvertTest {
    /*@Test
    fun getDataBarCode_barcode_returnSuccess() {
        val result = getDataBarCode("(01)9999999(10)240004490(40)000000")
        println("ASNConvertTest_getDataBarCode_returnSuccess : $result")
        val resultExpected = hashMapOf("01" to 9999999, "40" to "000000", "10" to 240004490)
        assertNotNull ( result)

    }

    @Test
    fun getDataBarCode_empty_returnSuccess() {
        val resultado = getDataBarCode("")
        println("getDataBarCode_empty_returnSuccess : $resultado")
        assertEquals( hashMapOf<String, String>(),  ( resultado))
    }

    @Test
    fun getDataBarCode_char_returnSuccess() {
        val resultado = getDataBarCode("#$$#&/(&(/&(/&%")
        println("getDataBarCode_empty_returnSuccess : $resultado")
        assertEquals( hashMapOf<String, String>(),  ( resultado))
    }

    @Test
    fun countElements_threeElements_returnSuccess() {
        val resultado = countElements("(01)9999999(10)240004490(40)060424")
        println("countElements_threeElements_returnSuccess : $resultado")
        assertEquals("3", resultado.toString())
    }


    @Test
    fun countElements_oneElements_returnSuccess() {
        val resultado = countElements("LPNV000072752")
        println("countElements_threeElements_returnSuccess : $resultado")
        assertEquals("1", resultado.toString())
    }

    @Test
    fun countElements_zeroElements_returnSuccess() {
        val resultado = countElements("")
        println("countElements_zeroElements_returnSuccess : $resultado")
        assertEquals("0", resultado.toString())
    }

    @Test
    fun countElements_cherElements_returnSuccess() {
        val resultado = countElements("------")
        println("countElements_cherElements_returnSuccess : $resultado")
        assertEquals("1", resultado.toString())
    }


    @Test
    fun convertPreASNEntityToASNEntity_preASNComplete_returnSuccess() {
        val resultado = convertPreASNEntityToASNEntity(dataPreASN().first())
        println("convertPreASNEntityToASNEntity_preASNComplete_returnSuccess : $resultado")
        assertNotNull ( resultado)
    }

    @Test
    fun convertPreASNEntityToASNEntity_preASNInComplete_returnSuccess() {
        val resultado = convertPreASNEntityToASNEntity(PreASN())
        println("convertPreASNEntityToASNEntity_preASNInComplete_returnSuccess : $resultado")
        assertNotNull ( resultado)
    }

    @Test
    fun ConvertUpdateResultQuantityDetail_asnEntityLPNCode_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) )
        val resultado = ConvertaddDetailLpnCode(asnEntities,"LPNV000072752")
        println("ConvertUpdateResultQuantityDetail_preASNyLPNCode_returnSuccess : $resultado")
        assertNotNull ( resultado)
    }

    @Test
    fun ConvertConvertDeleteLPN_asnEntiteIndex_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) )
        val newASNEntitis = ConvertaddDetailLpnCode(asnEntities,"LPNV000072752")
        val objASNEntity = ASNEntity( data =newASNEntitis )
        val resultado = ConvertDeleteLPN(objASNEntity,0)
        println("ConvertConvertDeleteLPN_asnEntiteIndex_returnSuccess-objASNEntity : $objASNEntity")
        println("ConvertConvertDeleteLPN_asnEntiteIndex_returnSuccess-resultado : $resultado")
        println("ConvertConvertDeleteLPN_asnEntiteIndex_returnSuccess-resultado.last().detail.size :"+ resultado.last().detail.size)
        assertEquals(0, ( resultado.last().detail.size))
    }

    @Test
    fun ConvertValidateStatusASN_true_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) )
        val newASNEntitis = ConvertaddDetailLpnCode(asnEntities,"LPNV000072752")
        val objASNEntity = ASNEntity( data =newASNEntitis )
        val resultado = ConvertValidateStatusASN(objASNEntity)
        println("ConvertValidateStatusASN_true_returnSuccess : $resultado")
        assertTrue( resultado)
    }

    @Test
    fun ConvertValidateStatusASN_false_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) )
        val resultado = ConvertValidateStatusASN(asnEntities)
        println("ConvertValidateStatusASN_false_returnSuccess : $resultado")
        assertFalse( resultado)
    }

    @Test
    fun ConvertValidateStatusPrintAssigned_true_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) , ipAddress = "192.168.1.254")
        val resultado = ConvertValidateStatusPrintAssigned(asnEntities)
        println("ConvertValidateStatusPrintAssigned_true_returnSuccess : $resultado")
        assertTrue( resultado)
    }

    @Test
    fun ConvertValidateStatusPrintAssigned_false_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()))
        val resultado = ConvertValidateStatusPrintAssigned(asnEntities)
        println("ConvertValidateStatusPrintAssigned_false_returnSuccess : $resultado")
        assertFalse( resultado)
    }

    @Test
    fun ConvertValidateStatusHeadASN_true_returnSuccess() {
        val asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()))
        val resultado = ConvertValidateStatusHeadASN(asnEntities)
        println("ConvertValidateStatusHeadASN_true_returnSuccess : $resultado")
        assertTrue( resultado)
    }

    @Test
    fun ConvertValidateStatusHeadASN_false_returnSuccess() {
        val asnEntities = ASNEntity()
        val resultado = ConvertValidateStatusHeadASN(asnEntities)
        println("ConvertValidateStatusHeadASN_true_returnSuccess : $resultado")
        assertFalse( resultado)
    }

    @Test
    fun ConvertUpdateResultQuantityDetail_8_returnSuccess() {
        var asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) )
        val newASNEntitis = ConvertaddDetailLpnCode(asnEntities,"LPNV000072752")
        asnEntities= ASNEntity( data =newASNEntitis )
        var resultEntities=ASNEntity(status = "Y", message = "", data = ConvertUpdateResultQuantityDetail(asnEntities, 0, "8"))
        println("ConvertUpdateResultQuantityDetail_8_returnSuccess : $resultEntities")
        assertEquals("8", resultEntities.data?.last()?.detail?.get(0)?.U_Quantity)
    }

    @Test
    fun ConvertUpdateResultQuantityDetail_8_returnError() {
        var asnEntities = ASNEntity( data =convertPreASNEntityToASNEntity(PreASN()) )
        val newASNEntitis = ConvertaddDetailLpnCode(asnEntities,"LPNV000072752")
        asnEntities= ASNEntity( data =newASNEntitis )
        var resultEntities=ASNEntity(status = "Y", message = "", data = ConvertUpdateResultQuantityDetail(asnEntities, 0, "8"))
        println("ConvertUpdateResultQuantityDetail_8_returnSuccess : $resultEntities")
        assertNotEquals("7", resultEntities.data?.last()?.detail?.get(0)?.U_Quantity)
    }
*/
}