/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package script_test

import connectdbf.SqlTask
import org.joda.time.DateTime
import org.joda.time.Minutes
import servise.*

import java.sql.Timestamp

/**
 *
 * @author 1
 */
class ScriptTest {


    public Object evalParProfile(Map<String, Object> values, Map maphelp) {

        def par1 = values.get("#par1");// объект
        def par2 = values.get("#par2") as Integer;// номер получасовки
        def par3 = values.get("#par3") as Integer; // тип мощности(0-3)
        if (par1 == -1) {
            par1 = values.get("id_object") as Integer;
        }


        def last;
        def first;

        def dates = MathTrans.getDatesByMonth(-1);



        first = dates[0];
        last = dates[1];

        def date = MathTrans.getDateProfile(first, par2);

        def idobj = "#prof${par1}";

        def helpval = maphelp.get(idobj);


        if (helpval == null) {

            helpval = SqlTask.getObjectValues(null, "profil_power", par1, first, last);

            maphelp.put(idobj, helpval);

        }
        def mapval = helpval.get(date);

        if (mapval == null) {

            return "нет данных"

        }


        def kt = values.get("kt");
        def kn = values.get("kn");

        def val;

        if (par3 == 0) {
            val = mapval.get("power_pa");
        } else if (par3 == 1) {
            val = mapval.get("power_pr");
        } else if (par3 == 2) {
            val = mapval.get("power_qa");
        } else if (par3 == 3) {
            val = mapval.get("power_qr");
        }


        return val * kt * kn;
        // не копировать !
    }


    public Object evalScript(Map<String, Object> values, Object valhelp) {

        // import servise.*;
        //import connectdbf.SqlTask;


        servise.ExcelReport report = new ExcelReport(null);
        def nameContr = values.get("name_subconto");
        def id_subconto = values.get("id_subconto");
        String fromReport = "80020Report";
        String fromXML = "80020XML";
        String fromError = "80020Error";

        def error = false;

        def alSend = [];
        def id = values.get("group_subconto");

        def hmReport;


        try {
            hmReport = report.getReport(7, id);
        } catch (Exception e) {

            //   MailClass.goMail(['ooo.ensit@gmail.com'], "Ошибка Отчета", e.getMessage(), nameContr, null, null);

            return;
        }


        Map hmCount = hmReport.get('&VALUE0');

        // Количество получасовок в предыдущем месяце


        DateTime dtLast = new DateTime().millisOfDay().setCopy(0).minusMonths(1);
        int countMont = MathTrans.getDaysByMonth(dtLast.monthOfYear) * 48;

        //проверяем количество получасовок

        if (hmCount) {


            hmCount.each {

                int countObj = it.value;

                if (countMont != countObj) {

                    //  MailClass.goMail(['ooo.ensit@gmail.com'], "Погрешность Отчета", "${it.value} ID=(${it.key})", nameContr, null, null);

                    if (countObj != 0) {  // если 0 то не в работе
                        error = true;
                    }


                }

            }
        }



        if (error) {
            return;
        }



        Map hmProcent = hmReport.get('&VALUE1');

        // Проверяем погрешность

        if (hmProcent) {
            hmProcent.each {

                def procent = it.value;

                if (procent instanceof String) {

                    // error=true;
                    // MailClass.goMail(['ooo.ensit@gmail.com'], "Погрешность Отчета", "${it.value} ID=(${it.key})", nameContr, null, null);

                    error = false;

                } else if (procent > 0.15) {


                    error = true;

                    //  MailClass.goMail(['ooo.ensit@gmail.com'], "Погрешность Отчета", "${it.value} ID=(${it.key})", nameContr, null, null);
                    //  error = true;

                }

            }
        }


        if (error) {
  //          return;
        }


        def fileReport = hmReport.get("#file");

        def fileMaket;

        servise.Protokol80020 protokol80020 = new Protokol80020(null);

        try {
            fileMaket = protokol80020.getMaket(values);

        } catch (Exception e) {

            MailClass.goMail(['ooo.ensit@gmail.com'], "Ошибка Макета", e.getMessage(), nameContr, null, null);

            return;
        }




        def mailsnab;

        if (values["email_subconto"]) {

            mailsnab = values["email_subconto"].split(";");

            mailsnab.each { alSend << it };

        }

        if (values["send_subconto"] && values["control_subconto"]) {

            mailsnab = values["control_subconto"].split(";");

            mailsnab.each { alSend << it };

        }


        alSend << "ooo.ensit@gmail.com";


        try {

            servise.MailClass.goMail(alSend, fromXML + "(" + nameContr + ")", fileMaket.getName(), "Макет 80020(${nameContr})", fileMaket, null)

            if (values["report_subconto"]) {

                servise.MailClass.goMail(alSend, fromReport + "(" + nameContr + ")", fileReport.getName(), "Показания счетчиков(${nameContr})", fileReport, null)

            }

        } catch (Exception e) {

            def msg = "";
            alSend.each {

                msg = it + ", ${msg}";

            }


            alSend.clear();
            alSend << "ooo.ensit@gmail.com";

            msg = "${msg} ${e.getMessage()}";

            servise.MailClass.goMail(alSend, "Ошибка отправки почты", msg, "Ошибка(${nameContr})", null, null)
            return;

        }

        //Обновляем флаг передачи

        String sql = "UPDATE  subconto SET  is_send=1 WHERE  id_subconto=${id_subconto}";

        def result = SqlTask.executeSql(null, sql);

    }       // не копировать !


    public Object evalCmdScript0(Map property, Map point, Integer pozcol, Integer pozrow,
                                 BitSet bitset, Map mapcommands, CommandGet cmd, ValuesByChannel channel) {


        List values = cmd.result;

        def hmVal = [:];

        hmVal.put("name_table", "profil_power");


        hmVal.put("tangens_f", 0);

        def val = values.get(0);
        hmVal.put("power_pa", val);

        val = values.get(1);
        hmVal.put("power_pr", val);

        val = values.get(2);
        hmVal.put("power_qa", val);

        val = values.get(3);
        hmVal.put("power_qr", val);


        val = values.get(4);
        hmVal.put("value_date", val);

        val = values.get(5);
        hmVal.put("object_caption", val);


        return hmVal;
        //end
    }

    public Object evalCmdScript1(Map property, Map point, Integer pozcol, Integer pozrow,
                                 BitSet bitset, Map mapcommands, CommandGet cmd, ValuesByChannel channel) {

        //  import org.joda.time.DateTime;
        //  import java.sql.Timestamp;
        //  import org.joda.time.Minutes;
        //   import servise.*;

        CommandGet cmdParent = mapcommands.getAt("SetProfilPower");

        def parentResult = cmdParent.result;

        Timestamp tsCmd = cmd.getProperty("value_date"); // дата команды
        DateTime dateTimeLast = parentResult[1]; // дата последней записи
        DateTime dateTimeCurrent = new DateTime(tsCmd.getTime());// дата текущей записи


        def iAddressLast = parentResult[0]; //  адрес пос. записи
        def iStepTime = parentResult[2];

        def iMultiple = 16; //шаг


        def vid = 3;
        // def vid = 131;
        def record;

        Minutes minutes = Minutes.minutesBetween(dateTimeCurrent, dateTimeLast);

        int countRec = minutes.getMinutes() / iStepTime;

// расширенный профиль
        if (bitset.get(ValuesByChannel.BSF_MEMORY_EX)) { // меркурий 233

            if (bitset.get(ValuesByChannel.BSF_BYT17_YES)) {

                iAddressLast = (iAddressLast | 0x10000);

            }

            record = iAddressLast - (countRec * iMultiple);



            if (record > 0xFFFF) {

                vid = 131;

            }
            if (record < 0) {

                vid = 131;

            }

        } else {
            // меркурий 230

            // адрес   на дату текущей записи
            record = iAddressLast - (countRec * iMultiple);

        }



        def poz2 = record & 0xFF;

        def poz1 = record >>> 8;
        poz1 = poz1 & 0xFF;

        def setCmd = [vid, poz1, poz2];

        return setCmd;

    }

    public Object evalCmdScript2(Map property, Map point, Integer pozcol, Integer pozrow,
                                 BitSet bitset, Map mapcommands, CommandGet cmd, ValuesByChannel channel) {


        def to4byte = { num ->

            def llbyte = [];

            int t = (int) ((num & 0xFF000000) >>> 24);
            llbyte.add(t);

            t = (int) ((num & 0xFF0000) >>> 16);
            llbyte.add(t);

            t = (int) ((num & 0xFF00) >>> 8);
            llbyte.add(t);

            t = (int) (num & 0xFF);
            llbyte.add(t);
            return llbyte;

        }

        List ladd = cmd.getProperty('add');
        List ldell = cmd.getProperty('del');

        List listRes = [];

        servise.CommandGet DelCounter = mapcommands.get('DelCountInCtrl');
        servise.CommandGet AddCounter = mapcommands.get('AddCountInCtrl');
        servise.CommandGet AddCounter206 = mapcommands.get('AddCount206InCtrl'); // для четырехбайтных


        servise.CommandGet cmdDev;

        // Всегда возвращаем список выполняемых команд

        //channel.clearButtonsAndAnswer();

        // Добавляем

        if (ladd) {

            ladd.eachWithIndex { num, idx ->

                if (num > 255) {  //206
                    cmdDev = AddCounter206.clone();
                    def add = to4byte(num);
                    add.each {
                        cmdDev.addByteInSet(it);
                    }
                } else {
                    cmdDev = AddCounter.clone();
                    cmdDev.addByteInSet(num);
                }
                cmdDev.name = AddCounter.name + idx;
                channel.addCommandClon(cmdDev.name, cmdDev);
                listRes.add(cmdDev.name);
            }


        }

// Удаляем
        if (ldell) {

            ldell.eachWithIndex { num, idx ->
                cmdDev = DelCounter.clone();
                cmdDev.name = DelCounter.name + idx;
                cmdDev.addByteInSet(num);
                mapcommands.put(cmdDev.name, cmdDev);
                listRes.add(cmdDev.name);
            }
        }
        return listRes;

        //  channel.hm

        Object[] var = new Object[2]

        def result = cmd.alResult;

        Timestamp tsCmd = cmd.getProperty("value_date"); // дата команды
        DateTime dateTimeCurrent = new DateTime(tsCmd.getTime());// дата текущей записи

        def number;  //Номер 1-8

        def min = dateTimeCurrent.getMinuteOfDay() / 30;

        def s = "";

        if (min == 0) {
// последняя получасовка
            number = 8;
        } else if (min <= 8) {
            number = min;
        } else if (min <= 16) {
            number = min - 8;
        } else if (min <= 24) {
            number = min - 16;
        } else if (min <= 32) {
            number = min - 24;
        } else if (min <= 40) {
            number = min - 32;
        } else if (min <= 48) {
            number = min - 40;
        }

        List valres;


        switch (number) {

            case 1: valres = result.subList(5, 7); break;
            case 2: valres = result.subList(9, 11); break;
            case 3: valres = result.subList(13, 15); break;
            case 4: valres = result.subList(17, 19); break;
            case 5: valres = result.subList(21, 23); break;
            case 6: valres = result.subList(25, 27); break;
            case 7: valres = result.subList(29, 31); break;
            case 8: valres = result.subList(33, 35); break;


        }

        if (valres[0] == 0xFF && valres[1] == 0xFF) {

            s = "Прибор выключен"
            valres[0] = 0; valres[1] = 0

        }


        Double v1 = MathTrans.getIntByList(valres, MathTrans.B_LITTLE_ENDIAN, true);
        v1 = v1 * 2 / 5;

        Double v2 = 0.0;
        Double v3 = 0.0;
        Double v4 = 0.0;

        var[0] = [];
        var[1] = [];

        var[0] << v1 << v2 << v3 << v4;

        var[0].each {
            def svs = String.format("%9.3f", it);
            var[1] << svs;
        }

        var[0] << tsCmd << s

        return var;

    }

    public Object evalCmdScript3(Map property, Map point, Integer pozcol, Integer pozrow,
                                 BitSet bitset, Map mapcommands, CommandGet cmd, ValuesByChannel channel) {

        //   import org.joda.time.DateTime;
        //   import java.sql.Timestamp;
        //   import org.joda.time.Minutes;
        // import servise.*;

        CommandGet cmdParent = mapcommands.getAt("SetProfilPower");

        def parentResult = cmdParent.result;

        Timestamp tsCmd = cmd.getProperty("value_date"); // дата команды
        DateTime dateTimeCurrent = new DateTime(tsCmd.getTime());// дата текущей записи

        def numberGroup;  //Номер группы  0x10 - 0x15 по 8 штук

        def hour = dateTimeCurrent.getHourOfDay();
        def min = dateTimeCurrent.getMinuteOfDay() / 30;
        def day = dateTimeCurrent.getDayOfMonth(); // день
        def month = dateTimeCurrent.getMonthOfYear();// месяц
        def year = dateTimeCurrent.getYearOfCentury(); //год

        //bitset.clear(ValuesByChannel.BSF_REPEAT_ON);

        if (min > 0 && min <= 8) {
            numberGroup = 0x10;
        } else if (min > 8 && min <= 16) {
            numberGroup = 0x11;
        } else if (min > 16 && min <= 24) {
            numberGroup = 0x12;
        } else if (min > 24 && min <= 32) {
            numberGroup = 0x13;
        } else if (min > 32 && min <= 40) {
            numberGroup = 0x14;
        } else if ((min > 40 && min <= 48) || min == 0) {
            numberGroup = 0x15;
        }



        day = Integer.decode("0x${day}");
        month = Integer.decode("0x${month}");
        year = Integer.decode("0x${year}");


        def setCmd = [];

        setCmd.add(numberGroup);
        setCmd.add(day);
        setCmd.add(month);
        setCmd.add(year);

        return setCmd;


    }
}