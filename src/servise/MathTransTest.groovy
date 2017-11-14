package servise

import org.joda.time.DateTime

/**
 * Created by 1 on 08.09.2016.
 */
class MathTransTest extends GroovyTestCase {


    void testGetDateProfile() {

        def dates = MathTrans.getDatesByMonth(-1);


        def date = MathTrans.getDateProfile(dates[0], 8);

        DateTime result = new DateTime(date.time);

        assertEquals(result.getHourOfDay(), 4);
        assertEquals(result.getMinuteOfHour(), 0);
    }

  void  testGetDatesByMonth(){



        def dates = MathTrans.getDatesByMonth(14);


        def date = MathTrans.getDateStrProf(dates[0], 8);

        assertEquals(date, "01.08.16 04:00");



    }



    void testGetDateStrProf() {


        def dates = MathTrans.getDatesByMonth(-1);


        def date = MathTrans.getDateStrProf(dates[0], 8);

        assertEquals(date, "01.08.16 04:00");

    }


    void testGetBinary32() {

    }

    void testGetDateReport() {

    }

    void testGetStringList() {

    }

    void testListToByteArray() {

    }

    void testGetCRC8() {

    }

    void testSim900Deccode() {

    }

    void testUcs2ToUTF8() {

    }

    void testUCS2ToString() {

    }

    void testConvertToUCS2() {

    }

    void testGetIntCheckSum() {

    }

    void testGetWAIT() {

    }

    void testGetUART() {

    }

    void testGetCheckCum() {

    }

    void testGetBBCSum() {

    }

    void testGetSumM228() {

    }

    void testGetSumM2281() {

    }

    void testIntToByteArray() {

    }

    void testGetListInteger() {

    }

    void testGetPosicion() {

    }

    void testGetInteger() {

    }

    void testGetIntByString() {

    }

    void testGetBetweenVal() {

    }

    void testGetBetweenVal1() {

    }

    void testCompareValue() {

    }

    void testSetObjectParameter() {

    }

    void testGetObjectParameter() {

    }

    void testSetUserParameter() {

    }

    void testGetUserParameter() {

    }

    void testSetCtrlParameter() {

    }

    void testGetCtrlParameter() {

    }

    void testGetValByNexString() {

    }

    void testGetStringMon() {

    }

    void testGetStringDow() {

    }

    void testIsCheck() {

    }

    void testGetArraySrc() {

    }

    void testGetCrc16Polin() {

    }

    void testCheckCRC24() {

    }

    void testGetResultMsg() {

    }

    void testCreateSendMsg() {

    }

    void testGetUnPackMsg() {

    }

    void testGetPackMsg() {

    }

    void testGetListResult() {

    }

    void testGetListResult1() {

    }

    void testGetAllMsg() {

    }

    void testGetListByNexString() {

    }

    void testGetNexStrByList() {

    }

    void testGetStringByList() {

    }

    void testGetJackSum() {

    }

    void testGetCRC24Jack() {

    }

    void testGetCRC24() {

    }

    void testParsingToMapKeyString() {

    }

    void testParsingToMapKeyInteger() {

    }

    void testGetIntByList() {

    }

    void testGetLongByList() {

    }

    void testGetIntbyBits() {

    }

    void testGetIntByBits() {

    }

    void testGetIntByBits1() {

    }

    void testLongToByteArray() {

    }

    void testLongToIntArray() {

    }

    void testLongTo3Byte() {

    }

    void testGetCRC16() {

    }

    void testGetCRC161() {

    }
}
