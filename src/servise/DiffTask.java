 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import connectdbf.StatementEx;
import dbf.Work;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.sf.json.JSONObject;
import org.jdesktop.swingx.JXPanel;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import xmldom.XmlTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static constatant_static.SettingActions.*;

/**
 * Выполнение разных задач
 *
 * @author 1
 */
public class DiffTask extends MainWorker {

    public static final String SQL_TYPE_VARCHAR = "VARCHAR";
    public static final String SQL_TYPE_INTEGER = "INTEGER";
    public static final String SQL_TYPE_SMALLINT = "SMALLINT";
    public static final String SQL_TYPE_LONGVARBINARY = "LONGVARBINARY";
    public static final String SQL_TYPE_MEDIUMBLOB = "MEDIUMBLOB";
    public static final String SQL_TYPE_BLOB = "BLOB";
    public static final String SQL_TYPE_DATETIME = "DATETIME";
    public static final String SQL_TYPE_TIMESTAMP = "TIMESTAMP";
    public static final String SQL_TYPE_INT = "INT";
    public static final String SQL_TYPE_TINYINT = "TINYINT";
    public static ResourceBundle BUNDLE; // ресурс теекущей базы
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static Connection connection = null;
    public boolean bEnd; // Конец процесса

    // Выполнить задачу
    public void doTask(String currentTask) {
        this.currentTask = currentTask;
        try {
            doInBackground();
        } catch (Exception ex) {
            setLoggerInfo(currentTask, ex);
        }

    }

    private static void showFormMail() throws  Exception {

       // Document docData = null;
        HashMap<String,Object> hmProp = null;
        
        try {
        //    docData = Work.getXmlDocFromConst("mail");
       
         hmProp=Work.getParametersFromConst("mail");
        
        } catch (SQLException ex) {
            Logger.getLogger(DiffTask.class.getName()).log(Level.SEVERE, null, ex);
        }

      //  Element e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "hostmail", "cell");
        //String hostMail = e.getAttribute("value");

        String hostMail = (String) hmProp.get("hostmail");
        
        
       // e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "mailuser", "cell");
     //   String userEmail = e.getAttribute("value");

        String userEmail = (String) hmProp.get("mailuser");

        
        
       // e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "port", "cell");
        Integer port =  (Integer) hmProp.get("port");

        
        
        
       // e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "fromemail", "cell");
        String fromEmail = (String) hmProp.get("fromemail");

       // e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "password", "cell");
        String mailPassword = (String) hmProp.get("password");

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel panelSend = new JPanel();
        JPanel panelError = new JPanel();

        tabbedPane.addTab("Почта", panelSend);
        tabbedPane.addTab("Ошибки", panelError);

        JTextArea textArea = new JTextArea();

        panelError.add(textArea);

        textArea.setFont(new Font(Font.SERIF, Font.PLAIN, 12));

        Box bMain = Box.createVerticalBox();

        Box b = Box.createHorizontalBox();

        JLabel label = new JLabel("Хост сервера");
        JTextField field = new JTextField(hostMail);
        label.setMinimumSize(new Dimension(100, 20));
        b.add(label);
        b.add(Box.createGlue());
        b.add(field);

        Box b2 = Box.createHorizontalBox();
        JLabel label1 = new JLabel("Имя пользователя");
        JTextField field1 = new JTextField(userEmail);
        label1.setMinimumSize(new Dimension(100, 20));

        b2.add(label1);
        b2.add(Box.createGlue());
        b2.add(field1);

        Box b3 = Box.createHorizontalBox();
        JLabel label3 = new JLabel("Пароль");
        JTextField field3 = new JTextField(mailPassword);
        label3.setMinimumSize(new Dimension(100, 20));

        b3.add(label3);
        b3.add(Box.createHorizontalGlue());
        b3.add(field3);

        Box b4 = Box.createHorizontalBox();
        JLabel label4 = new JLabel("Порт");
        JTextField field4 = new JTextField(port.toString());
        label4.setMinimumSize(new Dimension(100, 20));

        b4.add(label4);
        b4.add(Box.createGlue());
        b4.add(field4);

        Box b5 = Box.createHorizontalBox();
        JLabel label5 = new JLabel("Адрес отправителя");
        JTextField field5 = new JTextField(fromEmail);
        label5.setMinimumSize(new Dimension(100, 20));

        b5.add(label5);
        b5.add(Box.createGlue());
        b5.add(field5);

        Box b6 = Box.createHorizontalBox();
        JLabel label6 = new JLabel("Адрес отправки");
        JTextField field6 = new JTextField("");
        label6.setMinimumSize(new Dimension(100, 20));

        b6.add(label6);
        b6.add(Box.createHorizontalGlue());
        b6.add(field6);

        bMain.add(b);
        bMain.add(b2);
        bMain.add(b3);
        bMain.add(b4);
        bMain.add(b5);
        bMain.add(Box.createGlue());
        bMain.add(b6);

        JPanel panel = new JXPanel(new BorderLayout());
        panel.add(bMain, BorderLayout.CENTER);

        //   panel.setLayout(gridLayout);
        // panel.add(label);
        // panel.add(field);
        // panel.add(label1);
        // panel.add(field1);
        //panel.add(label3);
        //panel.add(field3);
        //panel.add(label4);
        //panel.add(field4);
        //panel.add(label5);
        //panel.add(field5);
        JDialog dialog = new JDialog();
        dialog.setSize(350, 300);
        dialog.setLocation(500, 500);

        dialog.setModal(true);

        JFrame jf = new JFrame();

        BorderLayout layout = new BorderLayout();
        dialog.setLayout(layout);
        panelSend.setLayout(new BorderLayout());

        dialog.add(tabbedPane, BorderLayout.CENTER);

        JTextField field7 = new JTextField();
        JButton button = new JButton("Файл");
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(field7, BorderLayout.CENTER);
        panel1.add(button, BorderLayout.EAST);

        panelSend.add(panel, BorderLayout.CENTER);
        panelSend.add(panel1, BorderLayout.NORTH);

        button.putClientProperty("fileSend", field7);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                File curDir = chooser.getCurrentDirectory();
                chooser.setDialogTitle("" + curDir.getAbsolutePath());

                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    //return null;
                } else {

                    JButton button = (JButton) e.getSource();

                    JTextField field = (JTextField) button.getClientProperty("fileSend");

                    field.setText(chooser.getSelectedFile().getAbsolutePath());

                    //return chooser.getSelectedFile();
                }

            }
        });

        JButton button1 = new JButton("Отправить");

        button1.putClientProperty("hostMail", field);
        button1.putClientProperty("userEmail", field1);
        button1.putClientProperty("port", field4);
        button1.putClientProperty("mailPassword", field3);
        button1.putClientProperty("fromEmail", field5);
        button1.putClientProperty("fileSend", field7);
        button1.putClientProperty("errors", textArea);
        button1.putClientProperty("TabPan", tabbedPane);
        button1.putClientProperty("MailTo", field6);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JTextField field;
                JButton button = (JButton) e.getSource();

                button.setForeground(Color.BLACK);

                field = (JTextField) button.getClientProperty("hostMail");
                String hostMail = field.getText();

                field = (JTextField) button.getClientProperty("fromEmail");
                String fromEmail = field.getText();

                field = (JTextField) button.getClientProperty("port");
                String port = field.getText();

                field = (JTextField) button.getClientProperty("mailPassword");
                String mailPassword = field.getText();

                field = (JTextField) button.getClientProperty("userEmail");
                String userMail = field.getText();

                field = (JTextField) button.getClientProperty("fileSend");
                JTextField field1 = (JTextField) button.getClientProperty("MailTo");

                String mailTo = field1.getText();

                File file = new File(field.getText());

                String sBody = "Проверка почты";

                if (!file.exists()) {
                    file = null;

                    if (!field.getText().isEmpty()) {

                        sBody = field.getText();
                    }

                }

                //           hostMail = parameters[0];
                //     port = parameters[1];
                //   fromEmail = parameters[2];
                // mailPassword = parameters[3];
                // userEmail = parameters[4];
                String[] parameters = new String[5];

                parameters[0] = hostMail;
                parameters[1] = port;
                parameters[2] = fromEmail;
                parameters[3] = mailPassword;
                parameters[4] = userMail;

                ArrayList<Object> al = new ArrayList<Object>();

                JTextArea area = (JTextArea) button.getClientProperty("errors");

                area.setText("");

                try {
                    MailClass.goMailApathe(mailTo, "Инспектор", sBody, file, parameters);
                } catch (Exception ex) {

                    button.setForeground(Color.red);

                    JTabbedPane pane = (JTabbedPane) button.getClientProperty("TabPan");

                    pane.setSelectedIndex(1);

                    area.setText(ex.getMessage());
                    MainWorker.setLogInfo("Отправка почты", ex);

                }

                button.setForeground(Color.GREEN);
            }
        });

        dialog.add(button1, BorderLayout.SOUTH);

        dialog.setVisible(true);

    }

    @Override
    public void endProcess() {
        super.endProcess();

        bEnd = true;
    }

    @Override
    public void doProcess() {

        errorString = null;

        try {

            newProcess(currentTask);

            if (currentTask.contains(CM_SET_CONFIG_OBJECT)) {

                File f = null;
                setConfigObject(f);

            } else if (currentTask.contains(CM_CHECK_DATA)) {

                checkValues();
            } else // Импорт данных с сервера 
            if (currentTask.equals(CM_GET_SERVER)) {
                ImportData();

            } else // Сообщение по почте
            if (currentTask.contains(CM_SEND_MESSAGE)) {

                sendMailMessage();
            } else
                   
            if (currentTask.contains(CM_IMPORT_INSPECTOR)) {
                    importFromInspector1();
              
            }else

            if (currentTask.startsWith(CM_UPDATE_PROGRAM)) {

                updateProgram();
            }

            if (currentTask.startsWith("update(")) {
                    updateCol();
              
            }

            if (currentTask.startsWith(CM_SET_VALUE)) {
                    addValue(null, null);
               
            }

            if (currentTask.startsWith(CM_UPDATE_TABLE_XML)) {

                ZipFile f = (ZipFile) getProperty(CM_UPDATE_TABLE_XML);

                String name = (String) getProperty(CM_SET_NAME_FILE);
                try {
                    addValue(f, name);
                    //   if(f instanceof InputStream){
                    //  InputStream is=(InputStream) f;
                    // try {
                    //   is.close();
                    // } catch (IOException ex) {
                    //   setLoggerInfo(currentTask, ex);
                    // }
                    //}
                } catch (Exception ex) {
                    setLoggerInfo("", ex);
                    ShowError(ex.getMessage());
                    answerProcess("ошибка.", ProcLogs.MSG_ERROR);

                }

            }

            // Обновление программы
            if (currentTask.startsWith(CM_SET_UPDATE)) {

                setUpdate();

            }
            answerProcess("успешно.", ProcLogs.MSG_OK);

        } catch (Exception e) {

            setLoggerInfo("Ошибка", e);
            errorString = "Ошибка !";

        }

    }

    private void runAnt(File buildFile) {
    }

    public static void write(int size, InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[size];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     * Создает zip файл с данными за 1 день по одному объекту в JSON формате
     * IDN_DDMMYY_TYP_PARAMETR
     *
     * @param dateTime
     * @param idObject
     * @return
     */
    public File getFileDay(DateTime dateTime, Integer idObject, String nameTable, Connection c) throws SQLException {

        DateTime dtFirst; // начальная дата
        DateTime dtLast; // конечная дата

        File result = null;

        dtFirst = dateTime.millisOfDay().setCopy(0);
        dtLast = dtFirst.plusDays(1);

        String nameFind = null;

        Timestamp tFirst = new Timestamp(dtFirst.getMillis());
        Timestamp tLast = new Timestamp(dtLast.getMillis());

        String sql = "SELECT * FROM profil_power WHERE Id_object=? AND value_date>=? AND value_date<=?";

        ArrayList al = new ArrayList();

        JSONObject jsono = new JSONObject();

        al.add(idObject);
        al.add(tFirst);
        al.add(tLast);
        ResultSet rs = SqlTask.getResultSet(c, sql, al);

        HashMap<String, Object> hm = new HashMap<>();

        try {
            while (rs.next()) {

                Timestamp t = rs.getTimestamp("value_date");

                long time = t.getTime();
                SqlTask.addParamToMap(rs, hm);
                hm.remove("Id_object");
                hm.remove("modify_date");
                hm.remove("value_date");

                jsono.put(time, hm);

            }

        } finally {

            rs.close();
        }

        Iterator it = jsono.keys();

        while (it.hasNext()) {

            Object key = it.next();
            Map map = (Map) jsono.get(key);

            long t3 = Long.parseLong(key.toString());

            Timestamp t1 = new Timestamp(t3);
            System.out.println(map.toString());
            System.out.println(t1.toString());

        }

        try {
            result = File.createTempFile(jsono.toString(), "txt");
        } catch (IOException ex) {
            Logger.getLogger(DiffTask.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;

    }

    /**
     * Создается новая таблица по Документу конфигурации
     *
     */
    private void createTableByConfig(Document document) throws Exception {

        String nameTable = document.getDocumentElement().getAttribute("name_table");

        ArrayList<String> types = Work.getTypInfo();

        NodeList list = XmlTask.getNodeListByXpath(document.getDocumentElement(), "descendant::cell");

        StringBuffer buffer = new StringBuffer("CREATE TABLE " + nameTable + " (");

        // Индексы и ключи
        HashMap<String, ArrayList<String>> hmKeys = new HashMap<String, ArrayList<String>>();

        // ResourceBundle bundle = ResourceBundle.getBundle("DbfRes",Ma);
        for (int i = 0; i < list.getLength(); i++) {

            // столбец
            Element e = (Element) list.item(i);

            String nameCol = e.getAttribute("name");
            String typeCol = e.getAttribute("type").toUpperCase();

            // по конкретному драйверу
            try {

                typeCol = BUNDLE.getString(typeCol);

            } catch (Exception ex) {

                return;
            }

            if (typeCol.isEmpty()) {
                continue;
            }

            // Проверяем на наличие типа
            if (!types.contains(typeCol)) {

                String msg = "<html><h3>Не удалось открыть базу</h3><hr>"
                        + "<h4><i><FONT COLOR=#ff0000>Проверте параметры соединенния</FONT></i><h4></html>";

                String[] sv = {"Параметры соединения", "Выход"};

                int isel = JOptionPane.showOptionDialog(null, msg, "Ошибка доступа", JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, sv, "Выход");

                return;

            }

            String sizeCol = e.getAttribute("size");
            String keyCol = e.getAttribute("key");
            String increment = "";

            if (keyCol.indexOf("INCREMENT") != -1) {
                // автоинкремент

                increment = BUNDLE.getString("INCREMENT");

            }

            // Индексы и ключи
            if (!keyCol.trim().isEmpty()) {
                // Индексы и ключи

                if (hmKeys.containsKey(keyCol)) {

                    ArrayList al = hmKeys.get(keyCol);

                    al.add(nameCol);

                } // новый
                else {
                    ArrayList<String> al = new ArrayList<String>();
                    al.add(nameCol);

                    hmKeys.put(keyCol, al);

                }

            }

            buffer.append(nameCol);
            buffer.append(" ");

            // если есть размер
            if (!sizeCol.trim().isEmpty()) {

                typeCol = typeCol + "(" + sizeCol + ")";

            }

            buffer.append(typeCol + " ");

            // авто инкремент
            buffer.append(increment);

            buffer.append(",");

        }

        // Формируем первичный индекс
        for (String s : hmKeys.keySet()) {

            ArrayList<String> al = hmKeys.get(s);

            String index = BUNDLE.getString(s);

            if (s.indexOf("PRIMARY") == -1) {
                continue;
            }

            buffer.append(index + "(");

            for (String nameIndex : al) {

                buffer.append(nameIndex + ",");

            }

            buffer.deleteCharAt(buffer.lastIndexOf(","));

            buffer.append(")");

        }

        if (buffer.lastIndexOf(",") == (buffer.length() - 1)) {

            buffer.deleteCharAt(buffer.lastIndexOf(","));
        }

        buffer.append(")");

        String sql = buffer.toString();
        SqlTask.executeSql(null, sql);

        // Таблица создана
        int b = 0;

        // Таблица создана
        if (b == 1) {

            // Формируем индексы и ключи
            buffer = new StringBuffer("CREATE ");

            int idx = 0;
            String name = "index_" + nameTable;

            for (String s : hmKeys.keySet()) {

                ArrayList<String> al = hmKeys.get(s);

                String index = BUNDLE.getString(s);

                if (index.indexOf("PRIMARY") != -1) {
                    continue;
                }

                buffer.append(index + " ");

                buffer.append(name + idx + " ON ");

                buffer.append(nameTable);

                buffer.append("(");

                for (String nameIndex : al) {

                    buffer.append(nameIndex + ",");

                }

                buffer.deleteCharAt(buffer.lastIndexOf(","));

                buffer.append(")");

            }

            if (buffer.lastIndexOf(",") == (buffer.length() - 1)) {

                buffer.deleteCharAt(buffer.lastIndexOf(","));
            }

            sql = buffer.toString();

            // Создаем индексы таблиц
            // executeSql(sql)
            // if (executeSql(sql)) {
            //   SendMessage("Созданы индексы для таблицы " + nameTable, MSG_OK, STR_NEW);
            // }
        }
    }

    /**
     * Запуск обновлений таблицы
     *
     * @param file
     */
    private void goTableUpdate(File file) {
    }

    /**
     * Установка обновлений программы
     */
    private void setUpdate() {

        TreeSet<String> tsTables = null;

        HashMap<String, Long> htSizes = new HashMap<String, Long>();

        HashMap<String, File> hmZip = new HashMap<String, File>();

        //     AntGroovy antGroovy=new AntGroovy();
//antGroovy.run();
        try {
            tsTables = Work.getNameTables();
        } catch (SQLException ex) {
            setLoggerInfo("Обновления", ex);
        }

        ArrayList<File> alFiles = new ArrayList<File>();

        String dir = System.getProperty("user.dir");

        File fileDir = new File(dir + "/update");
        fileDir.mkdir();

        File f = XmlTask.openFile("zip", "Выбор архива обновлений", null);

        try {
            try {

                ZipFile zf = new ZipFile(f);
                Enumeration e = zf.entries();
                while (e.hasMoreElements()) {
                    ZipEntry ze = (ZipEntry) e.nextElement();

                    File file = new File(fileDir.getAbsolutePath(), ze.getName());

                    FileOutputStream fos = new FileOutputStream(file);

                    write((int) ze.getSize(), zf.getInputStream(ze), fos);

                    hmZip.put(ze.getName(), file);

                    // htSizes.put(ze.getName(), ze.getSize());
                }

                zf.close();

                if (!tsTables.contains("update_tbl")) {
                    //createTableUpdate(null);
                }

                runAnt(hmZip.get("build.xml"));

            } catch (ZipException ex) {
                setLoggerInfo("ZIP", ex);
            } catch (IOException ex) {
                setLoggerInfo("ZIP", ex);
            }

            ZipInputStream zip = new ZipInputStream(new FileInputStream(f));

            //  BufferedInputStream bis=new BufferedInputStream(zip);
            ZipEntry entry;
            try {

                try {

                    while ((entry = zip.getNextEntry()) != null) {

                        String name = entry.getName();
                        setLoggerInfo(name, null);

                        //           write(zip.getInputStream(entry),
                        //  new BufferedOutputStream (new
                        //  FileOutputStream(entry.getName())));
                        int size = (int) entry.getSize();
                        byte[] buffer = new byte[size];

                        int len;
                        //   while ((len = zip.read(buffer)) >= 0) {
                        //out.write(buffer, 0, len);
                        //  in.close();
                        //out.close();
                        //   }

                        //   hmZip.put(entry.getName(), buffer);
                        // zip.closeEntry();
                    }

                } finally {
                    zip.close();
                }

            } catch (IOException ex) {
                setLoggerInfo("ZIP", ex);
            }

        } catch (FileNotFoundException ex) {
            setLoggerInfo("ZIP", ex);
        }

    }

    public static void saveTableInXml(ResultSet rsTable, File file) throws Exception {

        Document document = XmlTask.getNewDocument();

        Element root = document.createElement("root");

        document.appendChild(root);

        if (file == null) {
            return;
        }

        rsTable.last();

        int size = rsTable.getRow();

        if (size < 1) {

            //нет данных.
            return;
        }

        File path = file.getParentFile();

        if (!path.exists()) {
            path.mkdirs();

        }

        ResultSetMetaData metaData = rsTable.getMetaData();

        String nameT = metaData.getTableName(1).toLowerCase();

        TreeSet<String> ntab = SqlTask.getNamesDb(null, false);

        for (String n : ntab) {

            if (nameT.contains(n)) {
                nameT = n;
                break;
            }

        }

        root.setAttribute("name_table", nameT);

        String sKey = SqlTask.getPrimaryKeyTable(null, nameT);
        root.setAttribute("name_key", sKey);

        HashMap<String, Object> hmParamCol = SqlTask.getMapNamesCol(null, nameT, new Integer[]{6, 7});

        for (String nameCol : hmParamCol.keySet()) {

            Object[] oPar = (Object[]) hmParamCol.get(nameCol);

            String collTyp = "";
            String colSize = "";

            if (oPar[0] != null) {
                collTyp = oPar[0].toString().toUpperCase();
            }

            if (oPar[1] != null) {
                colSize = oPar[1].toString();
            }

            Element elType = document.createElement("type_col");

            elType.setAttribute("name", nameCol);
            elType.setAttribute("type", collTyp);
            elType.setAttribute("size", colSize);

            root.appendChild(elType);

        }

        String value;
        rsTable.beforeFirst();

        while (rsTable.next()) {

            Element element = document.createElement("row");

            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                String nameCol = metaData.getColumnName(i).toLowerCase();

                int typCol = metaData.getColumnType(i);

                if (typCol == java.sql.Types.LONGVARBINARY || typCol == java.sql.Types.BLOB || typCol == java.sql.Types.VARBINARY) {

                    byte[] bs = rsTable.getBytes(nameCol);
                    value = javax.xml.bind.DatatypeConverter.printBase64Binary(bs);
                    root.setAttribute("Base64", nameCol);

                } else {
                    value = rsTable.getString(nameCol);
                }
                element.setAttribute(nameCol, value);
            }
            root.appendChild(element);
        };

        XmlTask.saveXmlDocument(document, file);

    }

    /**
     * Добавление строк в таблицы данных
     */
    public void addValue(Object fff, String nameZip) throws Exception {

        HashMap<String, Object> hmTypesCol = null;

        ZipFile zf = null;
        File file = null;
        Object f = null;

        if (fff instanceof ZipFile) {
            zf = (ZipFile) fff;

        }

        if (fff instanceof File) {

            file = (File) fff;
        }

        if (zf == null && file == null) {

            file = XmlTask.openFile("xml", "Выбор файла", null);

        }
        String nameTable;
        String base64;

        HashMap<String, Object> hmLoad = new HashMap<String, Object>();

        ArrayList<HashMap> arrayList = null;

        if (zf != null) {
            f = zf.getInputStream(zf.getEntry(nameZip));
        }

        if (file != null) {
            FileInputStream fis = new FileInputStream(file);
            f = fis;
        }

        arrayList = XmlTask.getListbyXML(f, "root", null);

        if (arrayList == null || arrayList.isEmpty()) {
            return;
        }

        if (f instanceof InputStream) {

            ((InputStream) f).close();
        }

        nameTable = (String) arrayList.get(0).get("name_table");
        base64 = (String) arrayList.get(0).get("Base64");

        nameTable = nameTable.toLowerCase();

        ArrayList<String> alNamTab = SqlTask.getNamesTabBySQL(null, nameTable);

        nameTable = alNamTab.get(0);

        // Типы столбцов
        if (zf != null) {
            f = zf.getInputStream(zf.getEntry(nameZip));
        }

        if (file != null) {
            FileInputStream fis = new FileInputStream(file);
            f = fis;
        }

        //   hmTypesCol = XmlTask.getMapAttrByXML((FileInputStream) f, "name", "type", "type_col");
        hmTypesCol = SqlTask.getMapNamesCol(connection, nameTable, 5);

        if (f instanceof InputStream) {

            ((InputStream) f).close();
        }

        HashMap<String, Object> hmCol = SqlTask.getMapNamesCol(null, nameTable, 2);

        refreshBarValue("Обновление таблицы " + nameTable + "...");

        StatementEx statementEx = new StatementEx(null, nameTable, hmCol);

        if (zf != null) {
            f = zf.getInputStream(zf.getEntry(nameZip));
        }

        if (file != null) {
            FileInputStream fis = new FileInputStream(file);
            f = fis;
        }

        arrayList = XmlTask.getListbyXML(f, "row", null);

        if (f instanceof InputStream) {

            ((InputStream) f).close();
        }

        try {

            if (arrayList == null || arrayList.isEmpty()) {
                return;
            }

            setMinMaxValue(0, arrayList.size());

            Object value = null;

            for (HashMap<String, String> hm : arrayList) {

                int idx = arrayList.indexOf(hm);

                refreshBarValue(idx);

                hmLoad.clear();
                for (String name : hm.keySet()) {

                    Integer typCol = (Integer) hmTypesCol.get(name);

                    if (typCol == null) {
                        // нет такого столбца
                        continue;
                    }

                    value = hm.get(name);

                    if (typCol == java.sql.Types.BLOB || typCol == java.sql.Types.VARBINARY
                            || typCol == java.sql.Types.LONGVARBINARY) {

                        String b64 = hm.get(name);
                        byte[] bs = javax.xml.bind.DatatypeConverter.parseBase64Binary(b64);
                        value = bs;

                    } //else if (typCol.equals(SQL_TYPE_VARCHAR)) {
                    //  if (value == null) {
                    //    value = "";
                    // }
                    //    }
                    else if ((typCol == java.sql.Types.TIMESTAMP) || (typCol == java.sql.Types.DATE)) {

                        if (value == null || ((String) value).isEmpty()) {
                            value = new Timestamp(new Date().getTime());
                        }

                    } else if (typCol == java.sql.Types.BOOLEAN) {

                        if (value == null || ((String) value).isEmpty()) {
                            value = false;

                        } else {

                            if (value.equals("1") || value.equals("true")) {

                                value = true;
                            } else {

                                value = false;
                            }

                        }

                    } else if (typCol == java.sql.Types.BIT) {

                        if (value == null || ((String) value).isEmpty()) {
                            value = 0;

                        }
                        if (value.equals("true")) {

                            value = 1;
                        }

                        if (value.equals("false")) {

                            value = 0;
                        }

                    } else {
                        if ((value == null) || ((String) value).isEmpty()) {

                            value = null;
                        }

                    }
                    hmLoad.put(name.toLowerCase(), value);
                }

                statementEx.replaceRecInTable(hmLoad, true);
            }

        } finally {
            statementEx.close();
        }

    }

    private Object evaluateGroovyScript(String script) {

        Object result = null;

        Binding binding = new Binding();

        GroovyShell shell = new GroovyShell(binding);

        try {

            result = shell.evaluate(script);
        } catch (Exception e) {
            setLoggerInfo(script, e);
        }

        return result;
    }

    public static long copy(InputStream pIn,
            OutputStream pOut, boolean pClose,
            byte[] pBuffer)
            throws IOException {
        OutputStream out = pOut;
        InputStream in = pIn;
        try {
            long total = 0;
            for (;;) {
                int res = in.read(pBuffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (out != null) {
                        out.write(pBuffer, 0, res);
                    }
                }
            }
            if (out != null) {
                if (pClose) {
                    out.close();
                } else {
                    out.flush();
                }
                out = null;
            }
            in.close();
            in = null;
            return total;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable t) {
                    /* Ignore me */
                }
            }
            if (pClose && out != null) {
                try {
                    out.close();
                } catch (Throwable t) {
                    /* Ignore me */
                }
            }
        }
    }

    /**
     * Устанавливает значение в таблице констант
     *
     * @param vname -название константы
     * @param sxml -значение константы
     * @param typConst -тип константы
     * @param oldValue -Если true -сохраняем предыдущие значение
     */
    public static void setConfigValue(String vname, String sxml, int typConst, boolean oldValue) throws Exception {

        String sql = "SELECT *     FROM  c_const   WHERE  "
                + "c_const_name='" + vname + "'";

        Document document = null;
        HashMap<String, String> hmValues = new HashMap<String, String>();
        //HashMap<String, Object> hmCol = new HashMap<String, Object>();

        ResultSet rs = null;

        rs = SqlTask.getResultSet(null, sql, null, ResultSet.CONCUR_UPDATABLE);

        try {
            if (rs.next()) {

                String oldXml = rs.getString("c_const_value");
                try {
                    document = XmlTask.stringToXmlDoc(oldXml);
                } catch (Exception ex) {

                    deffLoger.error("", ex);
                }

                // Собираем все старые значения
                if (oldValue) {

                    NodeList list = document.getElementsByTagName("cell");

                    for (int i = 0; i < list.getLength(); i++) {

                        Element element = (Element) list.item(i);

                        String sName = element.getAttribute("name");
                        String sValue = element.getAttribute("value");
                        hmValues.put(sName, sValue);
                    }
                    try {
                        // Новый документ
                        document = XmlTask.stringToXmlDoc(sxml);
                    } catch (Exception ex) {

                        deffLoger.error(vname, ex);
                    }

                    if (document != null) {

                        list = document.getElementsByTagName("cell");

                        for (int i = 0; i < list.getLength(); i++) {

                            Element element = (Element) list.item(i);

                            String sName = element.getAttribute("name");

                            if (hmValues.containsKey(sName)) {

                                element.setAttribute("value", hmValues.get(sName));

                            }

                        }

                        sxml = XmlTask.xmlDocToString(document);
                    }
                }

                rs.updateInt("type_const", typConst);
                rs.updateString("c_const_value", sxml);
                rs.updateRow();
            } else {

                int key = SqlTask.getMaxKeyByNameTable(null, "c_const");
                rs.moveToInsertRow();
                rs.updateInt("c_const_id", key);
                rs.updateString("c_const_name", vname);
                rs.updateString("c_const_value", sxml);
                rs.updateInt("type_const", typConst);
                rs.insertRow();
                rs.moveToCurrentRow();
            }

        } finally {
            rs.close();
        }

    }

    /**
     * Создается представление данных по Документу конфигурации
     *
     */
    public static void createViewByConfig(Document document) throws Exception {

        String nameTable = document.getDocumentElement().getAttribute("name_table");

        String c_partype_id = document.getDocumentElement().getAttribute("c_partype_id");

        ArrayList<String> alNames = new ArrayList<String>();

        String sql = "DROP VIEW " + nameTable;

        try {

            SqlTask.executeSql(null, sql);
        } catch (SQLException e) {
        }

        NodeList list = XmlTask.getNodeListByXpath(document.getDocumentElement(), "descendant::cell");

        StringBuilder builder = new StringBuilder("CREATE VIEW " + nameTable + " (");

        for (int i = 0; i < list.getLength(); i++) {

            // столбец
            Element e = (Element) list.item(i);

            String nameCol = e.getAttribute("name");
            alNames.add(nameCol);

            builder.append(nameCol);
            builder.append(",");

        }

        int idx = builder.lastIndexOf(",");
        builder.delete(idx, idx + 1);

        builder.append(") AS SELECT ");

        for (String s : alNames) {

            builder.append(s);
            builder.append(",");

        }
        idx = builder.lastIndexOf(",");
        builder.delete(idx, idx + 1);

        builder.append(" FROM values_current WHERE parnumber_id=");
        builder.append(c_partype_id);

        sql = builder.toString();

        SqlTask.executeSql(null, sql);

    }

    /**
     * Загружает конфигурацию объекта из файла или XML Если отсутствует название
     * таблицы то заносим в константы
     */
    public void setConfigObject(Object file) throws Exception {

        Document doc;

        if (file instanceof File) {
            doc = XmlTask.getDocument((File) file);
        } else if (file instanceof Document) {
            doc = (Document) file;
        } else {

            File f = null;
            doc = XmlTask.getDocument(f);
        }

        HashMap<String, Object> hmEdit = new HashMap<String, Object>();
        if (doc == null) {

            return;
        }

        int maxRow = 0;
        String c_obj_type = doc.getDocumentElement().getAttribute("c_obj_type");
        String c_obj_list_name = doc.getDocumentElement().getAttribute("c_obj_list_name");
        String sxml = XmlTask.xmlDocToString(doc);

        // название таблицы
        String nameTable = doc.getDocumentElement().getAttribute("name_table");
        // Для констант
        Integer typobj = null;

        try {
            typobj = Integer.parseInt(c_obj_type);

        } catch (NumberFormatException e) {

            setLoggerInfo("Не указан  тип конфигурации !", e);
            return;
        }

        if (nameTable == null || nameTable.isEmpty()) {

            setConfigValue(c_obj_list_name, sxml, typobj, true);
            return;

        }

        if (nameTable == null || nameTable.trim().isEmpty()) {

            JOptionPane.showMessageDialog(null, "Не найдено имя таблицы !", "", JOptionPane.WARNING_MESSAGE);

            return;
        }

        ResultSet rsThis;

        // Ищем максимальное значение id Таблицы
        String sql = "SELECT c_obj_list_id FROM c_obj_spec";

        rsThis = SqlTask.getResultSet(null, sql);

        try {

            while (rsThis.next()) {

                int max = rsThis.getInt("c_obj_list_id");

                if (max > maxRow) {

                    maxRow = max;
                }

            }

        } finally {
            rsThis.close();
        }

        sql = "SELECT *     FROM c_obj_spec   WHERE  "
                + "c_name_table='" + nameTable + "'";

        TreeSet<String> tsTable = SqlTask.getNameTables(null);

        rsThis = SqlTask.getResultSet(null, sql);

        try {

            if (rsThis.next()) {

                hmEdit.put("c_obj_list_name", c_obj_list_name);
                hmEdit.put("c_base_property", sxml);
                sql = "WHERE c_name_table='" + nameTable + "'";
                SqlTask.updateRecInTable(null, "c_obj_spec", sql, hmEdit);

            } else {

                hmEdit.put("c_obj_list_id", maxRow + 1);
                hmEdit.put("c_obj_type", typobj);
                hmEdit.put("c_obj_list_name", c_obj_list_name);
                hmEdit.put("c_base_property", sxml);
                hmEdit.put("c_level", 1);
                hmEdit.put("c_name_table", nameTable);
                SqlTask.insertRecInTable(null, "c_obj_spec", hmEdit);

            }

            if (!tsTable.contains(nameTable) && typobj < 30) {

                // если такой таблицы нет то создаем
                NodeList list = XmlTask.getNodeListByXpath(doc.getDocumentElement(), "descendant::cell");

                SqlTask.createTableByConfig(null, list, nameTable);

                // таблица
            }
            // Обновляем таблицу по конфигурации

            updateTableByConfig(doc);
            // Представление  создаем в любом случае

            if (typobj >= 30) {
                // Представление
                createViewByConfig(doc);
            }

        } finally {
            rsThis.close();
        }

    }

    public String plusSpase(String old, int len) {

        StringBuilder builder = new StringBuilder(len);

        // builder.replace(len, len, old)
        return null;

    }

    /**
     * Экспорт данных в АСУСЭ
     *
     * @param path Папка куда пишется файл
     * @param dateTime Дата данных
     * @param sql Объекты экспорта
     * @return
     */
    public File toASUSE(String path, DateTime dateTime, String sql) {

        LinkedHashMap<String, char[]> hashMap = new LinkedHashMap<>();

        // Тип показаний    
        char[] c1_3 = new char[3];
        hashMap.put("eng", c1_3);

        // Номер договора
        char[] c5_24 = new char[20];
        hashMap.put("eng", c5_24);

        // Код договора
        //  char[] c5_24=new char[20];
        //  hashMap.put("eng", c5_24);
        File file = null;

        // String sss=sss.
        return file;

    }

    private void updateFromZip(ZipFile zf, ZipEntry ze) throws Exception {

        String zeName = ze.getName();
        int idx = 0;
        if (zeName.startsWith("table/")) {
            addValue(zf, zeName);
        }

        // Скрипты Groovy
        if (zeName.startsWith("script/")) {

            String script = null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            InputStream is;
            try {
                is = zf.getInputStream(ze);
                copy(is, baos, true, new byte[DEFAULT_BUFFER_SIZE]);

                script = baos.toString("UTF-8");
                Object result = evaluateGroovyScript(script);

                if (result instanceof String) {
                    setLoggerInfo(result.toString(), null);
                }

            } catch (IOException ex) {
                setLoggerInfo(script, ex);
            }
        }

        if (zeName.startsWith("config/")) {

            Document document = null;

            try {
                //  обновление, создание таблиц и конфигураций
                document = XmlTask.getDocument(zf.getInputStream(ze));
            } catch (IOException ex) {
                setLoggerInfo(zeName, ex);
            }

            if (document != null) {
                setConfigObject(document);

            }

        }

    }

    /**
     * Обновление таблицы конфигураций
     *
     * @param docConfig
     */
    public void setConfig(Document docConfig) {
    }

    /**
     * Обновление таблицы по конфигурации объекта
     *
     * @param docConfig -Загруженная XML конфигурация
     */
    public void updateTableByConfig(Document docConfig) throws Exception {

        String nameTable;
        HashMap<String, Object> hmCol = null;
        ArrayList<String> alColConfig = new ArrayList<String>();
        ArrayList<String> alColAdd = new ArrayList<String>();
        ArrayList<String> alColDel = new ArrayList<String>();

        nameTable = docConfig.getDocumentElement().getAttribute("name_table");
        try {
            hmCol = SqlTask.getMapNamesCol(null, nameTable, 6);
        } catch (SQLException ex) {
            setLoggerInfo("", ex);
        }

        NodeList list = XmlTask.getNodeListByXpath(docConfig.getDocumentElement(), "descendant::cell");

        for (int i = 0; i < list.getLength(); i++) {

            // столбец
            Element e = (Element) list.item(i);

            String nameCol = e.getAttribute("name");

            alColConfig.add(nameCol);

        }

// Столбцы для удаления
        for (String s : hmCol.keySet()) {

            if (!alColConfig.contains(s)) {

                alColDel.add(s);
            }

        }
        // Столбцы для добавления

        for (String s : alColConfig) {

            if (!hmCol.containsKey(s)) {

                alColAdd.add(s);
            }

        }

        if (!alColDel.isEmpty()) {

            // Удаляем
            for (int i = 0; i < list.getLength(); i++) {

                // столбец
                Element e = (Element) list.item(i);

                String nameCol = e.getAttribute("name");

                if (alColAdd.contains(nameCol)) {
                    try {
                        SqlTask.delColByConfig(null, nameTable, nameCol);
                    } catch (SQLException ex) {
                        setLoggerInfo("", ex);

                    }

                }
            }
        }

        if (!alColAdd.isEmpty()) {

            // Добавляем
            for (int i = 0; i < list.getLength(); i++) {

                // столбец
                Element e = (Element) list.item(i);

                String nameCol = e.getAttribute("name");

                if (alColAdd.contains(nameCol)) {
                    try {
                        SqlTask.addColByConfig(null, nameTable, nameCol, e);
                    } catch (SQLException ex) {
                        setLoggerInfo("", ex);

                    }

                }
            }
        }
    }

    private HashMap<String, Timestamp> createOldTime(ZipInputStream zisOld) {

        // время  старых файлов
        HashMap<String, Timestamp> hmTimeOld = new HashMap<String, Timestamp>();

        // Считываем время и имя
        ZipEntry entry;
        try {
            while ((entry = zisOld.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();

                long time = entry.getTime();

                Timestamp timestamp = new Timestamp(time);

                hmTimeOld.put(name, timestamp);

                zisOld.closeEntry();
            }

            zisOld.close();

        } catch (IOException ex) {
            setLoggerInfo("", ex);
        }

        return hmTimeOld;
    }

    private void saveZipFile(File fileZip) throws SQLException {

        Timestamp timestamp = new Timestamp(fileZip.lastModified());
        HashMap<String, Object> hmValues = new HashMap<String, Object>();
        hmValues.put("date_update", timestamp);
        hmValues.put("name_update", "updateZipFile");
        hmValues.put("file_update", fileZip);

        StatementEx statementEx = null;

        try {
            statementEx = new StatementEx(null, "update_tbl", hmValues);
            statementEx.replaceRecInTable(hmValues, true);
        } finally {
            statementEx.close();
        }

    }

    /**
     * Обновление программы
     */
    public void updateProgram() {

        java.io.File fUpdate = null;

        fUpdate = XmlTask.openFile("zip", "Выбор архива обновлений", null);

        if (fUpdate == null) {

            return;

        }

        // Вытаскиваем предыдущий zip файл
        Object[] objects = new Object[]{"updateZipFile"};

        String sql = "SELECT * FROM update_tbl WHERE name_update=?";

        ResultSet rs;
        Timestamp timestamp;

        try {
            rs = SqlTask.getResultSet(null, sql, objects);

            try {

                if (rs.next()) {

                    timestamp = rs.getTimestamp("date_update");

                    if (fUpdate.lastModified() > timestamp.getTime()) {

                        saveZipFile(fUpdate);

                    }

                } else {
                    // нет файла обновления

                    saveZipFile(fUpdate);

                }
            } finally {
                rs.close();
            }

        } catch (SQLException ex) {

            setLoggerInfo(sql, ex);
        }

    }

    /**
     * Проверка поступления данных
     */
    private void checkValuesEx() {
        DateTime datetime;
        DateTime nextdatetime;
        ResultSet rsValue;
        String sql = "";
        String sqldata = "";

        Date date = new Date();
        datetime = new DateTime(date.getTime());
        datetime = datetime.millisOfDay().setCopy(0);

        datetime = datetime.minusDays(2);
        //Вычитаем два дня

        String nTable = (String) this.getProperty("nTable");

        int fullCount = 48;

        try {

            String nameTableObject = SqlTask.getViewByTable("objects");

            sql = "SELECT  *  FROM " + nameTableObject;
            HashMap<String, String> hmTables = SqlTask.getNameAndCaptionTablesByTyp(null, null);
            String nameTabCheck = nTable;

            ResultSet rsObject = SqlTask.getResultSet(connection, sql, null, ResultSet.CONCUR_UPDATABLE);

            sqldata = "SELECT COUNT(*) FROM " + nameTabCheck + " WHERE value_date>? AND value_date<=? AND Id_object=?";

            sqldata = "SELECT COUNT(*) FROM " + nameTabCheck + " WHERE value_date>? AND value_date<=? AND Id_object=?";

            if (nameTabCheck.equals("enegry_data")) {
                fullCount = 1;
                sqldata = "SELECT COUNT(*) FROM " + nameTabCheck + " WHERE value_date>=?  AND value_date<? AND Id_object=?";

            }

            rsObject.last();

            int maxRow = rsObject.getRow();

            PreparedStatement statement = null;

            ResultSet rsCount;

            setMinMaxValue(0, maxRow);

            rsObject.beforeFirst();

            StringBuilder builder = null;

            refreshBarValue("Проверка поступления данных...");

            while (rsObject.next()) {
                //Проходим по каждой дате

                int objId = rsObject.getInt("c_tree_id");
                int poz = rsObject.getRow();

                builder = new StringBuilder();

                Object[] objects = new Object[3];

                for (int i = 0; i < 3; i++) {
                    nextdatetime = datetime.plusDays(i);
                    Timestamp timestamp = new Timestamp(nextdatetime.getMillis());
                    Timestamp tsCurr = new Timestamp(nextdatetime.plusDays(1).getMillis());

                    objects[0] = timestamp;
                    objects[1] = tsCurr;
                    objects[2] = objId;

                    rsCount = SqlTask.getResultSet(null, sqldata, objects, statement);

                    int countRow = 0;
                    if (rsCount.next()) {
                        countRow = rsCount.getInt(1);
                    }

                    if (statement == null) {

                        statement = (PreparedStatement) rsCount.getStatement();

                    }

                    rsCount.close();

                    double prc = countRow * 100.0 / fullCount;

                    if (prc > 100) {
                        prc = 100;
                    }

                    builder.append((int) prc);

                    if (i < 2) {
                        builder.append(";");
                    }

                }

                rsObject.updateString("value_info", builder.toString());
                rsObject.updateRow();

                refreshBarValue(poz);

            }

            putProperty("refresh", "objects");
            setNotifyObservers(this);

        } catch (SQLException ex) {
            setLoggerInfo(sql, ex);
        }
    }

    /**
     * Импорт данных с сервера
     */
    private void ImportData() throws Exception {

        HashMap<String, Object> mapRes = new HashMap<>();

        // Данные
        HashMap<String, String> hmTables = SqlTask.getNameAndCaptionTablesByTyp(null, 15);

        StatementEx statem;
        ResultSet rsObject = SqlTask.getResultSet(null, "SELECT c_tree_id FROM objects");

        rsObject.last();

        int maxObject = rsObject.getRow();

        mapRes.put("idn", 208);

        int pozrow = 0;

        Document document = XmlTask.getNewDocument();

        String sql = "SELECT * FROM objects";

        ResultSet resultSet = SqlTask.getResultSet(null, sql);

        Element root = document.createElement("root");

        document.appendChild(root);

        try {

            while (resultSet.next()) {

                int id = resultSet.getInt("c_tree_id");

            }

        } finally {
            resultSet.close();
        }

    }

    /**
     * Проверка поступления данных
     */
    private void checkValues() {
        DateTime datetime;
        DateTime nextdatetime;
        ResultSet rsValue;
        String sql = "";
        String sqldata = "";

        Date date = new Date();
        datetime = new DateTime(date.getTime());
        datetime = datetime.millisOfDay().setCopy(0);

        datetime = datetime.minusDays(2);
        //Вычитаем два дня

        String nTable = (String) this.getProperty("idTable");

        int fullCount = 48;

        String nameTableObject = SqlTask.getViewByTable("objects");

        sql = "SELECT  *  FROM " + nameTableObject;

        try {

            HashMap<String, String> hmTables = SqlTask.getNameAndCaptionTablesByTyp(null, null);

            String nameTabCheck = nTable;

            ResultSet rsObject = SqlTask.getResultSet(null, sql);

            sqldata = "SELECT COUNT(*) FROM " + nameTabCheck + " WHERE value_date>? AND value_date<=? AND id_object=?";

            sqldata = "SELECT COUNT(*) FROM " + nameTabCheck + " WHERE value_date>? AND value_date<=? AND id_object=?";

            if (nameTabCheck.equals("enegry_data")) {
                fullCount = 1;
                sqldata = "SELECT COUNT(*) FROM " + nameTabCheck + " WHERE value_date>=?  AND value_date<? AND id_object=?";

            }

            rsObject.last();

            int maxRow = rsObject.getRow();

            HashMap<Integer, StringBuilder> hmObject = new HashMap<Integer, StringBuilder>();

            HashMap<String, Object> hmCheck = new HashMap<String, Object>();

            hmCheck.put("value_info", null);
            hmCheck.put("c_tree_id", null);

            StatementEx statementEx = new StatementEx(null, "objects", hmCheck);

            PreparedStatement statement = null;

            ResultSet rsCount;

            Object[] objects = new Object[3];

            for (int i = 0; i < 3; i++) {
                nextdatetime = datetime.plusDays(i);
                Timestamp timestamp = new Timestamp(nextdatetime.getMillis());
                Timestamp tsCurr = new Timestamp(nextdatetime.plusDays(1).getMillis());

                setMinMaxValue(0, maxRow);

                refreshBarValue(nextdatetime.toString());

                StringBuilder builder;

                rsObject.beforeFirst();

                while (rsObject.next()) {
                    //Проходим по каждой дате

                    int objId = rsObject.getInt("c_tree_id");

                    int poz = rsObject.getRow();

                    objects[0] = timestamp;
                    objects[1] = tsCurr;
                    objects[2] = objId;

                    rsCount = SqlTask.getResultSet(null, sqldata, objects, statement);
                    // rsCount = SqlTask.getResultSet(null, sql, objects);

                    int countRow = 0;
                    if (rsCount.next()) {
                        countRow = rsCount.getInt(1);
                    }

                    if (statement == null) {

                        statement = (PreparedStatement) rsCount.getStatement();

                    }

                    rsCount.close();

                    if (hmObject.containsKey(objId)) {

                        builder = hmObject.get(objId);

                    } else {

                        builder = new StringBuilder();
                        hmObject.put(objId, builder);

                    }

                    double prc = countRow * 100.0 / fullCount;

                    if (prc > 100) {
                        prc = 100;
                    }

                    builder.append((int) prc);

                    if (i < 2) {
                        builder.append(";");
                    }

                    refreshBarValue(poz);

                }

            }

            rsObject.close();
            statement.close();
// Записываем изменения

            setMinMaxValue(0, hmObject.size());

            refreshBarValue("Обновляем...");

            int idx = 0;

            for (Integer id : hmObject.keySet()) {

                StringBuilder builder = hmObject.get(id);

                hmCheck.put("c_tree_id", id);
                hmCheck.put("value_info", builder.toString());

                //  hmCheck.put("value_info", "ewwewewe");
                try {
                    statementEx.replaceRecInTable(hmCheck, false);
                } catch (Exception ex) {
                    setLoggerInfo(sql, ex);
                }

                idx++;

                refreshBarValue(idx);
            }
            statementEx.close();

            putProperty("refresh", "objects");
            setNotifyObservers(this);

        } catch (SQLException ex) {
            setLoggerInfo(sql, ex);
        }
    }

    /**
     * Обновление любого столбца
     */
    private void updateCol() throws Exception {
        try {
            File f = XmlTask.openFile("xml", "Выбор файла", null);
            String nameUp = Work.getDelimitedString(currentTask, '(', ')');
            String nameTable;
            HashMap<String, Object> hmLoad = new HashMap<String, Object>();

            ArrayList<HashMap> arrayList = new ArrayList();
            arrayList = XmlTask.getListbyXML(f, "root", null);
            nameTable = (String) arrayList.get(0).get("name_table");

            ArrayList<String> alKeys = SqlTask.getPrimaryKey(null, nameTable);
            arrayList = XmlTask.getListbyXML(f, "row", null);

            if (arrayList.isEmpty()) {
                return;
            }

            setMinMaxValue(0, arrayList.size());

            for (HashMap<String, String> hm : arrayList) {

                hmLoad.clear();
                for (String name : hm.keySet()) {
                    String value = hm.get(name);
                    Object object = null;
                    if (name.equals(nameUp)) {

                        object = SqlTask.getDeffValueByTypCol(null, name, value, nameTable);
                        hmLoad.put(name, object);

                        for (String s : alKeys) {
                            value = hm.get(s);
                            object = SqlTask.getDeffValueByTypCol(null, s, value, nameTable);
                            hmLoad.put(s, object);
                        }
                    }
                }
                try {

                    //Только обновляем
                    if (hmLoad.isEmpty()) {
                        continue;
                    }

                    Work.replaceRecInTable(nameTable, hmLoad, false);
                } catch (Exception ex) {
                    setLoggerInfo(currentTask, ex);
                }

            }
        } catch (SQLException ex) {
            setLoggerInfo(currentTask, ex);
        }
    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof JButton) {
            // По кнопке с формы

            JButton button = (JButton) arg;

            currentTask = button.getText();

            doProcess();

        }

    }

    /**
     * Создаем архивную копию за месяц или добавляем данные
     *
     * @param Month за какой месяц сохраняем формат zip файла :
     * год_месяц_Объект_номер параметра
     */
    private void setDatesToZip(int Month) {

    }

    /**
     * Проверка соответствия базы данных шаблону конфигурации
     */
    private void checkConfig() {
    }

    /**
     * Импорт из инспектора 1.1
     */
    private void importFromInspector1() throws Exception {

        HashMap<Integer, NamedNodeMap> hmPoints = new HashMap<Integer, NamedNodeMap>();
        HashMap<Integer, ArrayList<NamedNodeMap>> hmAll = new HashMap<Integer, ArrayList<NamedNodeMap>>();

        TreeSet<String> treeSet = new TreeSet<String>();

        int countP = 0;
        int countO = 0;

        //Вытаскиваем телефонные  номера
        String sql = "SELECT pfonenumber FROM points";

        ResultSet rs;
        try {
            rs = SqlTask.getResultSet(null, sql);

            while (rs.next()) {

                String sNumber = rs.getString("pfonenumber");

                treeSet.add(sNumber);

            }

            rs.close();

        } catch (SQLException ex) {
            setLoggerInfo(sql, ex);
        }

        File file = null;
        Document doc_export = XmlTask.getDocument(file);
        if (doc_export == null) {
            return;
        }
        Element elmroot = doc_export.getDocumentElement();
        // Загружаем таблицу соответствия
        // HashMap<String, String> hmTable = new HashMap<String, String>();
        // XmlTask.loadXmlInMap(hmTable, XmlTask.getFile("import"));

        refreshBarValue("Обработка объектов...");
        // Точки учета
        sql = "child::Dbf[attribute::NameBase='Point']/descendant::ROW";
        NodeList nlPoint = XmlTask.getNodeListByXpath(elmroot, sql);

        Integer idx = null;

        setMinMaxValue(0, nlPoint.getLength());

        for (int i = 0; i < nlPoint.getLength(); i++) {
            Element e_point = (Element) nlPoint.item(i);

            NamedNodeMap map = e_point.getAttributes();
            String sidx = e_point.getAttribute("idx");

            try {
                idx = Integer.parseInt(sidx);
            } catch (NumberFormatException e) {

                setLoggerInfo(sql, e);
                continue;
            }

            refreshBarValue(i);

            hmPoints.put(idx, map);

            // Объекты и счетчики
            sql = "child::Dbf[attribute::NameBase='Counters']/descendant::ROW[attribute::idn='" + sidx + "']";
            NodeList nlCounter = XmlTask.getNodeListByXpath(elmroot, sql);

            ArrayList<NamedNodeMap> alAttr;

            for (int j = 0; j < nlCounter.getLength(); j++) {
                Element e_counter = (Element) nlCounter.item(j);
                map = e_counter.getAttributes();

                if (hmAll.containsKey(idx)) {
                    alAttr = hmAll.get(idx);
                } else {
                    alAttr = new ArrayList<NamedNodeMap>();
                    hmAll.put(idx, alAttr);
                }

                alAttr.add(map);
            }
        }

        refreshBarValue("Создаем точки...");

        HashMap<String, Object> hmValues = new HashMap<String, Object>();

        setMinMaxValue(0, hmPoints.size());

        for (Integer id : hmPoints.keySet()) {

            hmValues.clear();

            NamedNodeMap mapPoint = hmPoints.get(id);

            String name_point = mapPoint.getNamedItem("Name").getNodeValue();
            String address = mapPoint.getNamedItem("Addres").getNodeValue();
            String comm_port = mapPoint.getNamedItem("CommPort").getNodeValue();
            String byte_size = mapPoint.getNamedItem("ByteSize").getNodeValue();
            String baud_rate = "9600";
            String stop_bits = mapPoint.getNamedItem("StopBits").getNodeValue();
            String parity = "Нет";
            String typ_connect = "GSM";
            String pfonenumber = mapPoint.getNamedItem("pfonenumber").getNodeValue();

            if (treeSet.contains(pfonenumber)) {
                continue;
            }

            refreshBarValue(countP);

            Integer count_sql = 3;
            Integer time_pause = 30;
            String ini_modem = mapPoint.getNamedItem("iniModem").getNodeValue();
            Integer time_aut = 5000;
            Integer sys_time = 3;
            String dop_info_xml = "";

            Integer c_tree_id_point = 0;

            try {
                c_tree_id_point = Work.addInTree(1, 4);
            } catch (SQLException ex) {
                setLoggerInfo("Глобальный идентификатор", ex);
                continue;
            }

            hmValues.put("name_point", name_point);
            hmValues.put("address", address);
            hmValues.put("comm_port", comm_port);
            hmValues.put("byte_size", byte_size);
            hmValues.put("baud_rate", baud_rate);
            hmValues.put("stop_bits", stop_bits);
            hmValues.put("parity", parity);
            hmValues.put("typ_connect", typ_connect);
            hmValues.put("pfonenumber", pfonenumber);
            hmValues.put("count_sql", count_sql);
            hmValues.put("time_pause", time_pause);
            hmValues.put("ini_modem", ini_modem);
            hmValues.put("time_aut", time_aut);
            hmValues.put("sys_time", sys_time);
            hmValues.put("dop_info_xml", dop_info_xml);
            hmValues.put("sub_type11", -1);
            hmValues.put("c_tree_id", c_tree_id_point);

            try {
                SqlTask.insertRecInTable(null, "points", hmValues);
            } catch (SQLException ex) {
                setLoggerInfo("points", ex);
                continue;
            }

            countP++;

            ArrayList<NamedNodeMap> alObject = hmAll.get(id);

            // Объекты и счетчики
            for (NamedNodeMap map : alObject) {

                hmValues.clear();
                //счетчик
                String model_counter = map.getNamedItem("model").getNodeValue();
                String counter_addres = map.getNamedItem("counter_addres").getNodeValue();
                String password_1 = map.getNamedItem("Password1").getNodeValue();
                String password_2 = "";
                String serial_number = map.getNamedItem("serial_number").getNodeValue();
                Byte revers = 0;
                dop_info_xml = "";

                Integer idCount = 0;

                try {
                    idCount = Work.addInTree(5, 6);
                } catch (SQLException ex) {
                    setLoggerInfo("Глобальный идентификатор", ex);
                    continue;
                }

                hmValues.clear();

                hmValues.put("model_counter", model_counter);
                hmValues.put("counter_addres", counter_addres);
                hmValues.put("password_1", password_1);
                hmValues.put("password_2", password_2);
                hmValues.put("serial_number", serial_number);
                hmValues.put("revers", revers);
                hmValues.put("dop_info_xml", dop_info_xml);
                hmValues.put("c_tree_id", idCount);

                try {
                    SqlTask.insertRecInTable(null, "object5", hmValues);
                } catch (SQLException ex) {
                    setLoggerInfo("object5", ex);
                }

                String dis_number = map.getNamedItem("Row324").getNodeValue();
                String name1 = map.getNamedItem("Rows5078").getNodeValue();
                String res = map.getNamedItem("Rows3578").getNodeValue();
                String name2 = map.getNamedItem("Row593").getNodeValue();
                String dogovor = map.getNamedItem("Rows5236").getNodeValue();
                String name3 = map.getNamedItem("Row249").getNodeValue();
                String name4 = map.getNamedItem("Rows536").getNodeValue();
                String name5 = map.getNamedItem("Rows7529").getNodeValue();
                Integer sub_type4 = c_tree_id_point;
                Integer sub_type6 = idCount;
                Integer power_tr = 0;
                Integer kn = 1;
                Integer kt = 1;
                String type_trans = "";
                String value_info = "";
                String status = "ДОбавлен из Инспектора 1";
                Timestamp date_input = new Timestamp(new Date().getTime());
                String kod_piramida = "";
                dop_info_xml = "";

                Integer idObj = 0;

                try {
                    idObj = Work.addInTree(2, 1);
                } catch (SQLException ex) {
                    setLoggerInfo("Глобальный идентификатор", ex);
                    continue;
                }

                hmValues.clear();

                hmValues.put("dis_number", dis_number);
                hmValues.put("name1", name1);
                hmValues.put("res", res);
                hmValues.put("name2", name2);
                hmValues.put("dogovor", dogovor);
                hmValues.put("name3", name3);
                hmValues.put("name4", name4);
                hmValues.put("name5", name5);
                hmValues.put("sub_type4", sub_type4);
                hmValues.put("sub_type6", sub_type6);
                hmValues.put("power_tr", power_tr);
                hmValues.put("kn", 1);
                hmValues.put("kt", 1);
                hmValues.put("type_trans", type_trans);
                hmValues.put("value_info", value_info);
                hmValues.put("status", status);
                hmValues.put("date_input", date_input);
                hmValues.put("kod_piramida", kod_piramida);
                hmValues.put("dop_info_xml", 1);
                hmValues.put("c_tree_id", idObj);

                try {
                    SqlTask.insertRecInTable(null, "objects", hmValues);
                } catch (SQLException ex) {
                    setLoggerInfo("objects", ex);
                }

                countO++;

            }
        }

        setLoggerInfo("Точек учета " + countP, null);

        setLoggerInfo("Объектов " + countO, null);

    }

    private void sendMailMessage() {
        try {
            showFormMail();
        } catch (Exception ex) {
            setLoggerInfo("", ex);
        }
    }
}
