package servise

import connectdbf.SqlTask
import org.joda.time.DateTime
import org.joda.time.Minutes

/**
 * Created by 1 on 08.09.2016.
 */
class ScriptGroovyTest extends GroovyTestCase {

    @Override
    void tearDown() {


        def connect=SqlTask.connectCurrent;

        if (connect) {
            SqlTask.connectCurrent.close();
        }

        super.tearDown();
    }

    void setUp() {


        String user_dir = System.getProperty("user.dir");

        def a = user_dir.tokenize('\\');

        def last = a[-1];

        a.remove(last);
        user_dir = a.join('\\');

        def connectionDbf = SqlTask.openTestBase(user_dir,'DatBase');

        SqlTask.connectCurrent = connectionDbf;



        super.setUp()

    }



    void testExecuteScript() {

        ScriptGroovy script=new ScriptGroovy();

        def  param=["kt":1,"kn":300]

      def val=   script.runScript("#GREATE_PROFIL(6775)",param);

        assertEquals(val,46.350,0.3);

        val=  script.runScript("#PROF_VAL(-1,8,0)",param);

        assertEquals(val,46.950,0.3);

        val=  script.runScript("#PROF_VAL(-1,10,0)",param);


        assertEquals(val,47.55,0.3);

    }

    void testScript() {

       // DateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour)


        DateTime current =new DateTime(2017,9,11,0,30);// дата команды
        DateTime last =new DateTime(2017,10,3,9,0); // дата последней записи


        def version=0;
        def flag17=1;  // Флаг 17 байта
        def sizeMem=131071; // Общий размер памяти  под получас.


      //  Timestamp tsCmd = cmd.getProperty("value_date"); // дата команды
      //  DateTime dateTimeLast = parentResult[1]; // дата последней записи
      //  DateTime dateTimeCurrent = new DateTime(tsCmd.getTime());// дата текущей записи


        def iAddressLast = 14592 //  адрес пос. записи


        if (version==1){
        iAddressLast=iAddressLast*16;
        }



        if (flag17==1){
            iAddressLast=(iAddressLast | 0x10000);
        }

      //  def iAddressLast = 80128




        def iStepTime = 30;

        def iMultiple = 16; //шаг

        // Количество получасовок

        Minutes minutes = Minutes.minutesBetween(current, last);

        int countRec = minutes.getMinutes() / iStepTime;

        // адрес   на дату текущей записи
        def record = iAddressLast - (countRec * iMultiple);



        assertEquals(record,62960);

    }


}
