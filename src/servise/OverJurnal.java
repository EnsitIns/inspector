package servise;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import connectdbf.SqlTask;
import dbf.Work;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import sockets.ClientTcp;
import xmldom.XmlTask;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static constatant_static.SettingActions.esStatus.esShowOver;
import static constatant_static.SettingActions.esStatus.isSetStatus;

/**
 * Класс заполнения ведомости превышений
 *
 * @author 1
 */
public class OverJurnal extends SwingWorker<String, String> implements Observer {

    public static ArrayList<Element> alOver = new ArrayList<Element>();
    ArrayList<Document> alSend;
    private int swith;

    public static void sendOver() {
    }

// Добавляем превышения по конкретному  пользователю
    public static synchronized void addOver(Document doc_user, List listError) throws Exception {

        Map<String, String> hmprop = XmlTask.getMapAttrubuteByName(doc_user.getDocumentElement(),
                "name", "value", "column");

        byte bb = 0;

        // Имя пользователя
        String user = hmprop.get("name_user");


        // id Пользователя

        //     Integer idUser=hmprop.get("name_user");


        // id пользователя
        String c_tree_id = hmprop.get("c_tree_id");

        int id = Integer.parseInt(c_tree_id.trim());
        //   c_tree_id = Work.getCaptionByIdObject(id, null);


        // Превышения
        NodeList nlOvers = doc_user.getElementsByTagName("events");


        Element elm;

        for (int i = 0; i < nlOvers.getLength(); i++) {
            elm = (Element) nlOvers.item(i);


            String notise = elm.getAttribute("notise");


            try {
                bb = Byte.parseByte(notise);

                if (!BitSetEx.isBitSet(bb, (byte) 3)) {
                    continue;
                }


            } catch (NumberFormatException e) {
                bb = 0;
                continue;
            }




            String idObject = elm.getAttribute("id_object");

            int id_Obj = 0;

            try {

                id_Obj = Integer.parseInt(idObject.trim());

            } catch (NumberFormatException e) {

                listError.add(e);
            }


            String c_value_date = elm.getAttribute("c_value_date");

            DateTime d_value;
            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
            d_value = dtf.parseDateTime(c_value_date);

            Timestamp timestamp = new Timestamp(d_value.getMillis());


            String c_object_name = elm.getAttribute("object_name");
            String c_partype_name = elm.getAttribute("c_partype_name");
            String value = elm.getAttribute("value");
            String over_value = elm.getAttribute("over_value");

            HashMap<String, Object> hmValue = new HashMap<String, Object>();

            hmValue.put("Id_object", id_Obj);
            hmValue.put("user_par", user);
            hmValue.put("c_tree_name", c_object_name);
            hmValue.put("value_date", timestamp);
            hmValue.put("c_partype_name", c_partype_name);
            hmValue.put("c_quantity", value);
            hmValue.put("over_value", over_value);
            try {
                Work.insertRecInTable("c_value_over", hmValue);
            } catch (SQLException ex) {
                listError.add(ex);
            }

        }
        try {
            // Мигание журнала
            sendBlinkToUser(id, user, listError);
        } catch (SQLException ex) {

            listError.add(ex);
        }

    }

    // Мигание Журнала превышений
    private static void sendBlinkToUser(int id_user, String user, List listErr) throws SQLException {



        Object[] objUser = null;
        objUser = Work.getIPaddresUser(id_user);

        if (objUser[0] == null || objUser[1] == null) {

            listErr.add("Пользователь " + user + " не зарегистрирован !");
            return;
        }
        Integer idPort = (Integer) objUser[1];
        String ipAddres = (String) objUser[0];
        ClientTcp clientTcp = null;
        Object o = null;

        try {

            InetAddress ia = InetAddress.getByName(ipAddres);

            //   o = SendMessage("Посылка уведомления по превышениям на адрес: " + ia.getHostAddress(), MSG_SQL, STR_NEW);

            clientTcp = new ClientTcp(ia, idPort, 5000);
            String msg = (String) clientTcp.sendMsg("@");
            //  SendMessageAnswer(o, msg, MSG_OK);



        } catch (IOException ex) {

            listErr.add(ex.getMessage());
            listErr.add("Не удалось послать сообщение пользователю " + user);

        }

    }

    // Мигание значка журнала превышений
    public static void blinkLabel() {

        if (isSetStatus(esShowOver)) {

            return;
        }

        // MainForm.FRAME_MAIN.setState(JFrame.NORMAL);



        //  Action action = (Action) getAction(CM_SHOW_JURNAL_OVER);

        //  ImageIcon iconBlink = createIcon("red.png");


        //BlinkAction blink = new BlinkAction(action, iconBlink, esShowOver);

        // blink.execute();

    }

    public void update(Observable o, Object arg) {




        //   if (o instanceof ExcelClass) {


        //     blinkLabel();


        // }




        if (arg instanceof Document) {

            CheckValue obser = (CheckValue) o;

            Document doc_user = (Document) arg;

            Map<String, String> hmsend = null;
            try {
                hmsend = XmlTask.getMapAttrubuteByName(doc_user, "name", "value", "cell");
            } catch (Exception ex) {
                MainWorker.deffLoger.error("", ex);
            }

            //  String s0=  hmsend.get("eml");
            //  String s1=  hmsend.get("sms");
            String s0 = hmsend.get("ower");
            //String s3=  hmsend.get("tfl");

            if (s0.equals("1") && !obser.OverOk) {
                try {

                    //  SendMessage("Добавление информации в журнал превышений...", MSG_LOG, STR_NEW);



                    blinkLabel();


                    NodeList nlval = doc_user.getElementsByTagName("events");
                    String sql = "select * from c_value_over";
                    ResultSet rsThis = SqlTask.getResultSet(null, sql);
                    java.util.Date date = null;


                    for (int i = 0; i < nlval.getLength(); i++) {
                        Element elm = (Element) nlval.item(i);
                        NamedNodeMap nnm = elm.getAttributes();
                        rsThis.moveToInsertRow();


                        for (int j = 0; j < nnm.getLength(); j++) {
                            String name = nnm.item(j).getNodeName();
                            String value = elm.getAttribute(name);
                            rsThis.updateString(name, value);


                        }



                        rsThis.insertRow();
                        rsThis.moveToCurrentRow();
                    }


                    //   SendMessage("Успешно", MSG_OK, STR_ANSWER);


                    obser.OverOk = true;
                } catch (SQLException ex) {
                    //  DbfClass.logger.error("", ex);
                }




            }



        }

    }

    @Override
    protected String doInBackground() throws Exception {


        return null;

    }
}
