package servise;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Управление логированием
 *
 * @author 1
 */
public class LogSend extends MainWorker {

    private Integer idSchedule;// Расписание отправки логов
    private String email;// адрес отправки
    private Boolean log_send_day;// отправлять или нет за текущий день
    private Boolean log_send; // За прошедший день

    public LogSend(ExecutorService pool) {
        this.pool = pool;
    }

    // Установка параметров
    private boolean setParameters() {

        String dir = System.getProperty("user.dir");
        File file = null;
        file = new File(dir, "config_local.xml");

        HashMap<String, Object> hmProp = null;
        try {
            hmProp = xmldom.XmlTask.getMapValuesByXML(file,"name", "value", "cell");
        } catch (Exception ex) {
            setLoggerInfo("Параметры экспорта логов", ex);
            return false;
        }

 
        idSchedule = -1;


        try {

       
            idSchedule = (Integer) hmProp.get("log_id_schedulel");

            email = (String) hmProp.get("log_mail");

            log_send_day =  (Boolean) hmProp.get("log_send_day");

            log_send =  (Boolean) hmProp.get("log_send");




        } catch (NumberFormatException exception) {

            setLoggerInfo("", exception);
            return false;
        }

        return true;

    }

    @Override
    public void update(Observable o, Object arg) {


        if (arg instanceof Integer) {

            if (setParameters()) {

                if (idSchedule == (Integer) arg && o instanceof ScheduleClass) {

                    executeProcess();

                }
            }

        }
    }

    @Override
    public void doProcess() {

        try {

            newProcess("Отправка текущего лога...");

            sendLog();

            String msg = "лог на адрес " + email + " отправлен.";
            refreshBarValue(msg);
            setLoggerInfo(msg, null);



        } catch (Exception ex) {
            setLoggerInfo("Отправка логов", ex);
        }


    }

    private void sendLog() throws Exception {


        String dir = System.getProperty("user.dir");

        ZipCreator zipCreator = new ZipCreator(dir + "/LOGS", "logs.zip");


        if (log_send_day) {



            File f = new File(dir + "/LOGS", "srv.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "soap.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "piramida.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "excel.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "channel.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "channel1.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }
            f = new File(dir + "/LOGS", "channel2.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }
            f = new File(dir + "/LOGS", "channel3.log");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

        }

        if (log_send) {

            DateTime dateTime = new DateTime().minusDays(1);

            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

            String sDate = dateTime.toString(dtf);

            File f = new File(dir + "/LOGS", "srv.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "soap.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "piramida.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "excel.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "channel.log" + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

            f = new File(dir + "/LOGS", "channel1.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }
            f = new File(dir + "/LOGS", "channel2.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }
            f = new File(dir + "/LOGS", "channel3.log." + sDate + ".txt");

            if (f.exists()) {
                zipCreator.addFileToZip(f);
            }

        }





        File fileZip = zipCreator.getZipFile();

        if (fileZip.exists()) {

            String msg = "Отправка текущего лога на адрес " + email;

            blinkText(msg);
            setLoggerInfo(msg, null);


            try {
                InetAddress address;
                address = InetAddress.getLocalHost();
                String hostA = address.getHostAddress();
                MailClass.goMailApathe(email, "Текущий лог :" + hostA, "Инспектор", fileZip, null);


              //  if (!alErrors.isEmpty()) {

                //    throw new Exception("Отправка лога. Проблемы с почтой");

               // }


            } finally {
                stopBlinkText();
            }
        }
    }
}
