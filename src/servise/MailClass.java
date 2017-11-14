package servise;

import dbf.Work;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import xmldom.XmlTask;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/*
 * Электронная почта
 * and open the template in the editor.
 */
/**
 *
 * @author 1
 */
public class MailClass extends SwingWorker<String, Void> implements Observer {

    public static ArrayList<Element> alOver = new ArrayList<Element>();
    static String hostMail = "10.11.1.2"; //- хост почтового сервера
    public static org.apache.log4j.Logger logger;
    // static String mailUser = "sergey.ss@bk.ru";// - имя пользователя, для авторизации на почтовом сервере
    static String mailPassword = "silesta";// - пароль пользователя, для авторизации на почтовом сервере
    static String fromEmail = "sergey.ss@bk.ru"; //- адрес эл. почты отправителя
    //- Имя пользователя для авторизации на почтовом сервере
    static String userEmail = "sergey.ss@bk.ru";
    // static String fromFullname = "Inspector"; //- имя отправителя
    static String port = null;
    //  static String emailUser = null;
    //  static String name_user = null;
    static boolean exJello = false;

    //    String hostMail="smpt.yandex.ru"; //- хост почтового сервера
    //   String mailUser="scolko@yandex.ru";// - имя пользователя, для авторизации на почтовом сервере
    //  String mailPassword="silesta";// - пароль пользователя, для авторизации на почтовом сервере
    //  String fromEmail="scolko@yandex.ru"; //- адрес эл. почты отправителя
    //   String fromFullname="Информатор ПО Inspector"; //- имя отправителя
    /**
     * Отправляет письмо без вложений. Перед вызовом надо заполнить следующие
     * поля экземпляра данного класса: fromEmail - адрес эл. почты отправителя
     * fromFullname - имя отправителя emailUser - адрес эл. почты получателя
     * fullnameUser - имя получателя subject - тема письма body - тело письма
     * hostMail - хост почтового сервера mailUser - имя пользователя, для
     * авторизации на почтовом сервере mailPassword - пароль пользователя, для
     * авторизации на почтовом сервере
     */
    public static synchronized void sendMail(Document docSend, List listError) throws Exception {

        Map<String, String> hmprop = XmlTask.getMapAttrubuteByName(docSend.getDocumentElement(),
                "name", "value", "column");

        byte bb = 0;

        // setMailParameters();
        //   String hostMail = hmprop.get("hostmail"); //- хост почтового сервера
        //   String mailUser = hmprop.get("mailuser");// - имя пользователя, для авторизации на почтовом сервере
        //    String mailPassword = hmprop.get("password");// - пароль пользователя, для авторизации на почтовом сервере
        // fromEmail = hmprop.get("email"); //- адрес эл. почты отправителя
        //     String port = hmprop.get("port");
        String mailUser = hmprop.get("email");
        String fromFullname = "Автоинформатор 'Inspector'"; //- имя отправителя

        //   mailPassword="ift";
        String subject = "Информация по превышениям";
        String body = null;
        StringBuilder sb_send = new StringBuilder();

        NodeList nlval = docSend.getElementsByTagName("events");

        for (int i = 0; i < nlval.getLength(); i++) {
            Element elm = (Element) nlval.item(i);

            String notise = elm.getAttribute("notise");

            try {
                bb = Byte.parseByte(notise);

                if (!BitSetEx.isBitSet(bb, (byte) 2)) {
                    continue;
                }

            } catch (NumberFormatException e) {
                bb = 0;
                continue;
            }

            NamedNodeMap nnm = elm.getAttributes();

            StringBuilder sb_row = new StringBuilder();

            for (int j = 0; j < nnm.getLength(); j++) {
                String name = nnm.item(j).getNodeName();
                String value = elm.getAttribute(name);
                sb_row.append(value + " | ");

            }

            sb_send.append(sb_row.toString() + "\n");

        }

        body = sb_send.toString();

        if (!body.isEmpty()) {
          //  goMail(mailUser, fromFullname, body, null, null, listError);
        }

        // Отпрпавляем файлы
        nlval = docSend.getElementsByTagName("report");

        for (int i = 0; i < nlval.getLength(); i++) {
            Element elm = (Element) nlval.item(i);

            String path = elm.getAttribute("path");

            File file = new File(path);
            //goMail(mailUser, fromFullname, body, file, null, listError);

            file.delete();

        }

    }

    public static File createFile(File file, ByteArrayOutputStream byteStream) throws Exception {

        FileOutputStream fout = new FileOutputStream(file.getAbsolutePath());
        fout.write(byteStream.toByteArray());
        fout.close();

        return file;
    }

    public static File createZipFile(File file, ByteArrayOutputStream byteStream, String nameArxive) throws Exception {

        File f = new File(file.getParent(), nameArxive);

        FileOutputStream fout = new FileOutputStream(f.getAbsolutePath());
        ZipOutputStream zout = new ZipOutputStream(fout);

        //Для всех файлов:
        ZipEntry ze = new ZipEntry(file.getName());//Имя файла - имя файла в архиве
        zout.putNextEntry(ze);

        zout.write(byteStream.toByteArray());
        //отправка данных в поток zout
        zout.closeEntry();

        zout.close();

        //отправка данных в поток zout
        return f;
    }

    public static ByteArrayOutputStream getByteArrayOutputStream(Document document) throws Exception {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(byteStream);
        Transformer t = null;
        t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
        t.transform(new DOMSource(document), result);
        return byteStream;
    }

    /**
     * Отправляет письмо без вложений. Перед вызовом надо заполнить следующие
     * поля экземпляра данного класса: fromEmail - адрес эл. почты отправителя
     * fromFullname - имя отправителя emailUser - адрес эл. почты получателя
     * fullnameUser - имя получателя subject - тема письма body - тело письма
     * hostMail - хост почтового сервера mailUser - имя пользователя, для
     * авторизации на почтовом сервере mailPassword - пароль пользователя, для
     * авторизации на почтовом сервере
     */
    private static DataHandler getDataHandler(Document document) {

        DataHandler dataHandler = null;

        try {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//ObjectOutputStream objectStream=new ObjectOutputStream(byteStream);
//objectStream.writeObject(theObject);
//msg.setDataHandler(new DataHandler( new ByteArrayDataSource( byteStream.toByteArray(), "lotontech/javaobject" )));

            // OutputStream outputStream=new OutputStreamWriterByteArrayOutputStream();
            StreamResult result = new StreamResult(byteStream);

            Transformer t = null;
            try {
                t = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(MailClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            t.transform(new DOMSource(document), result);

            dataHandler = new DataHandler(new ByteArrayDataSource(byteStream.toByteArray(), "text/html"));

        } catch (TransformerException ex) {
            Logger.getLogger(MailClass.class.getName()).log(Level.SEVERE, null, ex);
        }

        return dataHandler;

    }

    // Передача ежедневного лога
    public static synchronized void goLog(List listError) {

        File file = XmlTask.getFile("config");

        HashMap<String, Object> hmProp = null;
        try {
            hmProp = XmlTask.getMapValuesByXML(file, "name", "value", "cell");
        } catch (Exception ex) {
            listError.add(ex);
            return;
        }

        Integer sisSend = (Integer) hmProp.get("log_send");

        if (sisSend == null || sisSend != 1) {

            return;
        }

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
// Отправляем за предыдущий день

        DateTime dateTime = new DateTime();

        dateTime = dateTime.minusDays(1);
        String nameFile = "srv.log." + dateTime.toString(dtf) + ".txt";

        File f = new File(XmlTask.DIR_PROGRAMM + "/LOGS", nameFile);

        if (f.exists()) {

            String email = (String) hmProp.get("log_mail");
            InetAddress address = null;
            try {
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {
                listError.add(ex);
            }
            String hostA = address.getHostAddress();

           // goMail(email, "Суточный лог :" + hostA, "", f, null, listError);

        }
    }

    // Сообщение, состоящее из одной части с типом контента text/plain.
    public static void setTextContent(Message msg) throws MessagingException {
        // Установка типа контента
        String mytxt = "This is a test of sending a "
                + "plain text e-mail through Java.\n"
                + "Here is line 2.";
        msg.setText(mytxt);

        // Альтернативный способ
        msg.setContent(mytxt, "text/plain");

    }

    // Сообщение с типом контента multipart/mixed. Обе части имеют тип контента text/plain.
    public static void setMultipartContent(Message msg) throws MessagingException {
        // Создание и заполнение первой части
        MimeBodyPart p1 = new MimeBodyPart();
        p1.setText("This is part one of a test multipart e-mail.");

        // Создание и заполнение второй части
        MimeBodyPart p2 = new MimeBodyPart();
        // Here is how to set a charset on textual content
        p2.setText("This is the second part", "us-ascii");

        // Создание экземпляра класса Multipart. Добавление частей сообщения в него.
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(p1);
        mp.addBodyPart(p2);

        // Установка объекта класса Multipart в качестве контента сообщения
        msg.setContent(mp);
    }

    // Прикрепление файла в качестве вложения. Используется JAF FileDataSource.
    public static void setFileAsAttachment(Message msg, String filename)
            throws MessagingException {

        // Создание и заполнение первой части
        MimeBodyPart p1 = new MimeBodyPart();
        p1.setText("This is part one of a test multipart e-mail."
                + "The second part is file as an attachment");

        // Создание второй части
        MimeBodyPart p2 = new MimeBodyPart();

        // Добавление файла во вторую часть
        FileDataSource fds = new FileDataSource(filename);
        p2.setDataHandler(new DataHandler(fds));
        p2.setFileName(fds.getName());

        // Создание экземпляра класса Multipart. Добавление частей сообщения в него.
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(p1);
        mp.addBodyPart(p2);

        // Установка экземпляра класса Multipart в качестве контента документа
        msg.setContent(mp);
    }

    // Добавление в первую часть html-контента.
    // Оптправка данных любого другого типа производится аналогичным образом.
    public static void setHTMLContent(Message msg) throws MessagingException {

        String html = "<html><head><title>"
                + msg.getSubject()
                + "</title></head><body><h1>"
                + msg.getSubject()
                + "</h1><p>This is a test of sending an HTML e-mail"
                + " through Java.</body></html>";

        // HTMLDataSource является внутренним классом
        msg.setDataHandler(new DataHandler(new HTMLDataSource(html)));
    }

    /*
     * Внутренний класс работает аналогично JAF datasource и добавляет HTML в контент сообщения
     */
    static class HTMLDataSource implements DataSource {

        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        // Возвращаем html строку в InputStream.
        // Каждый раз возвращается новый поток
        @Override
        public InputStream getInputStream() throws IOException {
            if (html == null) {
                throw new IOException("Null HTML");
            }
            return new ByteArrayInputStream(html.getBytes());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        @Override
        public String getContentType() {
            return "text/html";
        }

        @Override
        public String getName() {
            return "JAF text/html dataSource to send e-mail only";
        }
    }

    private static void putExchahgeProperties(Properties props) {
    }

    public static void sendMail(String mailTo, String fromFullname, String body, Object fileName, String[] parameters, List listError) {

        // Сюда необходимо подставить адрес получателя сообщения
        String to = "sendToMailAddress";
        String from = "sendFromMailAddress";
        // Сюда необходимо подставить SMTP сервер, используемый для отправки
        String host = "smtpserver.yourisp.net";

        // Создание свойств, получение сессии
        Properties props = new Properties();

        String subject = "ИИС Инспектор";

        logger = org.apache.log4j.Logger.getLogger("LogServer");

        if (parameters == null) {
            try {
                setMailParameters();
            } catch (Exception ex) {

                listError.add(ex);
            }

        } else {

            hostMail = parameters[0];
            port = parameters[1];
            fromEmail = parameters[2];
            mailPassword = parameters[3];

        }

        props.put("mail.smtp.host", hostMail);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);

        if (exJello) {
        }

        // При использовании статического метода Transport.send()
        // необходимо указать через какой хост будет передано сообщение
        props.put("mail.smtp.host", host);
        // Включение debug-режима
        props.put("mail.debug", "true");

        // Получение сессии
        Session session = Session.getInstance(props);

        try {
            // Получение объекта транспорта для передачи электронного сообщения
            Transport bus = session.getTransport("smtp");

            // Устанавливаем соединение один раз
            // Метод Transport.send() отсоединяется после каждой отправки
            bus.connect();
            // Обычно для SMTP сервера необходимо указать логин и пароль
            bus.connect("smtpserver.yourisp.net", "username", "password");

            // Создание объекта сообщения
            Message msg = new MimeMessage(session);

            // Установка атрибутов сообщения
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            // Парсинг списка адресов разделённых пробелами. Строгий синтаксис
            msg.setRecipients(Message.RecipientType.CC,
                    InternetAddress.parse(to, true));
            // Парсинг списка адресов разделённых пробелами. Более мягкий синтаксис.
            msg.setRecipients(Message.RecipientType.BCC,
                    InternetAddress.parse(to, false));

            msg.setSubject("Тест отправки E-Mail с помощью Java");
            msg.setSentDate(new Date());

            // Установка контента сообщения и отправка
            setTextContent(msg);
            msg.saveChanges();
            bus.sendMessage(msg, address);

            setMultipartContent(msg);
            msg.saveChanges();
            bus.sendMessage(msg, address);

            setFileAsAttachment(msg, "C:/WINDOWS/CLOUD.GIF");
            msg.saveChanges();
            bus.sendMessage(msg, address);

            setHTMLContent(msg);
            msg.saveChanges();
            bus.sendMessage(msg, address);

            bus.close();

        } catch (MessagingException mex) {
            // Печать информации обо всех возможных возникших исключениях
            mex.printStackTrace();
            // Получение вложенного исключения
            while (mex.getNextException() != null) {
                // Получение следующего исключения в цепочке
                Exception ex = mex.getNextException();
                ex.printStackTrace();
                if (!(ex instanceof MessagingException)) {
                    break;
                } else {
                    mex = (MessagingException) ex;
                }
            }
        }
    }

    public static synchronized void sendMail(String mailTo, String fromFullname, String body, Object fileName, String[] parameters) throws Exception {

        String subject = "ИИС Инспектор";

        if (parameters == null) {
            setMailParameters();

        } else {

            hostMail = parameters[0];
            port = parameters[1];
            fromEmail = parameters[2];
            mailPassword = parameters[3];

        }

        Properties props = System.getProperties();
        props.put("mail.smtp.host", hostMail);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");
        Session session_m = Session.getDefaultInstance(props, null);
        Object oMsg = null;

        MimeMessage message = new MimeMessage(session_m);

        message.setFrom(new InternetAddress(fromEmail, fromFullname));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo, fromFullname));
        message.setSubject(subject);
        BodyPart messageBodyPart = new MimeBodyPart();

        //Заполнеям тело письма в кодировке koi8-r
        messageBodyPart.setContent(body, "text/plain; charset=koi8-r");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        //Добавляем в письмо прикрепленные файлы
        if (fileName != null && fileName instanceof File) {

            File file = (File) fileName;

            messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(file);

            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(file.getName());
            multipart.addBodyPart(messageBodyPart);

        }

        int iPort = Integer.parseInt(port);

        message.setContent(multipart);

        Transport t = session_m.getTransport("smtp");
        t.connect(hostMail, iPort, fromEmail, mailPassword);
        t.sendMessage(message, message.getAllRecipients());

    }

    public static synchronized void goMail(ArrayList<String> mailTo, String fromFullname, String body,String tema, Object fileName, String[] parameters) throws Exception  {

        //String subject = tema;

        logger = org.apache.log4j.Logger.getLogger("LogServer");

        if (parameters == null) {
                setMailParameters();

        } else {

            hostMail = parameters[0];
            port = parameters[1];
            fromEmail = parameters[2];
            mailPassword = parameters[3];

        }

        Properties props = System.getProperties();
        props.put("mail.smtp.host", hostMail);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");

        Session session_m = Session.getDefaultInstance(props, null);
        Object oMsg = null;

       //    Address address= InternetAddress.
       // Address[] cc = new Address[] {InternetAddress.parse("abc@abc.com"),
         //       InternetAddress.parse("abc@def.com"),
           //     InternetAddress.parse("ghi@abc.com")};
       // message.addRecipients(Message.RecipientType.CC, cc);


        MimeMessage message = new MimeMessage(session_m);
                message.setFrom(new InternetAddress(fromEmail, fromFullname));


               for (String addres:mailTo){
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(addres, fromFullname));
                }

                message.setSubject(tema);
                BodyPart messageBodyPart = new MimeBodyPart();

                //Заполнеям тело письма в кодировке koi8-r
                messageBodyPart.setContent(body, "text/plain; charset=koi8-r");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                //Добавляем в письмо прикрепленные файлы
                if (fileName != null && fileName instanceof File) {

                    File file = (File) fileName;

                    messageBodyPart = new MimeBodyPart();

                    DataSource source = new FileDataSource(file);

                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(file.getName());
                    multipart.addBodyPart(messageBodyPart);

                }

                int iPort = Integer.parseInt(port);

                message.setContent(multipart);

                Transport t = session_m.getTransport("smtp");
                t.connect(hostMail, iPort, fromEmail, mailPassword);
                t.sendMessage(message, message.getAllRecipients());

    }

    static void sendMailBox() {

        Properties properties = System.getProperties();

        Session session = Session.getInstance(properties, null);
        try {
            Store store = session.getStore("imap");
            store.connect("imap.mail.ru", "*******", "******");
            Folder inbox = store.getDefaultFolder();
            inbox = inbox.getFolder("INBOX");

            inbox.open(Folder.READ_WRITE);
            int count = inbox.getMessageCount();
            System.out.println("Number of mails is " + count);
            for (int i = 1; i <= count; i++) {
                Message message = inbox.getMessage(i);
                String subject = message.getSubject();
                if (subject.toLowerCase().indexOf("c++") != -1) {
                    System.out.println(message.getSubject());
// there remove message
                } // end if
            } // end for

            inbox.close(true);
            store.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } // end try

    }


    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("Psix47@mail.ru", "******");
    }



        public static void MailReceiving() throws Exception {

            Properties props = new Properties();

            String host = "mail.ru";
            String provider = "pop.mail.ru";

             Session session = Session.getDefaultInstance(props);

            // Session session = Session.getDefaultInstance(props, new MailAuthenticator());
            Store store = session.getStore(provider);
            store.connect(host, null, null);

            Folder inbox = store.getFolder("INBOX");
            if (inbox == null) {
                System.out.println("No INBOX");
                System.exit(1);
            }
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (int i = 0; i < messages.length; i++) {
                System.out.println("Message " + (i + 1));
                messages[i].writeTo(System.out);
            }
            inbox.close(false);
            store.close();
        }



    public static synchronized void sendMail(ArrayList<String> mailTo, String fromFullname, String subject, String body, Object fileName, String[] parameters) throws Exception {

        if (parameters == null) {
            setMailParameters();
        } else {
            hostMail = parameters[0];
            port = parameters[1];
            fromEmail = parameters[2];
            mailPassword = parameters[3];
            userEmail = parameters[4];

        }

        // Create the email message
        MultiPartEmail email = new MultiPartEmail();

        // Create the attachment
        if (fileName != null && fileName instanceof File) {
            File file = (File) fileName;

            EmailAttachment attachment = new EmailAttachment();

            attachment.setPath(file.getAbsolutePath());
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setDescription("Файл");
            attachment.setName(file.getName());
            email.attach(attachment);
        }

        int iPort = Integer.parseInt(port);

        email.setSSL(true);
        email.setAuthenticator(new DefaultAuthenticator(userEmail, mailPassword));
        email.setHostName(hostMail);
        email.setSmtpPort(iPort);
        for (String to : mailTo) {
            email.addTo(to);
        }

        email.setFrom(fromEmail, fromFullname);
        email.setSubject(subject);
        email.setMsg(body);
        // add the attachment
        // send the email
        email.send();

    }

    public static synchronized void goMailApathe(String mailTo, String fromFullname, String body, Object fileName, String[] parameters) throws Exception {
            String subject = "ИИС Инспектор";
            logger = org.apache.log4j.Logger.getLogger("LogServer");
            if (parameters == null) {
                    setMailParameters();
          
            
            } else {
                hostMail = parameters[0];
                port = parameters[1];
                fromEmail = parameters[2];
                mailPassword = parameters[3];
                userEmail = parameters[4];

            }

            // Create the email message
            MultiPartEmail email = new MultiPartEmail();

            // Create the attachment
            if (fileName != null && fileName instanceof File) {
                File file = (File) fileName;

                EmailAttachment attachment = new EmailAttachment();

                attachment.setPath(file.getAbsolutePath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription("Файл");
                attachment.setName(file.getName());
                email.attach(attachment);
            }

            int iPort = Integer.parseInt(port);

            email.setAuthenticator(new DefaultAuthenticator(userEmail, mailPassword));
            email.setHostName(hostMail);
            email.setSmtpPort(iPort);
            email.setSSL(true);
            email.addTo(mailTo, "John Doe");
            email.setFrom(fromEmail, "Инспектор");
            email.setSubject(subject);
            email.setMsg(body);
            // add the attachment
            // send the email
            email.send();
    }

    /**
     * Класс, описывающий сущность "Письмо", заполняется в сервлете
     * MailSenderServlet и используется в mail.MailProcessor
     */
    public class Mail {

        /**
         * Поле "Кому"
         */
        private String to;
        /**
         * Поле "Копия"
         */
        private String cc;
        /**
         * Поле "Скрытая копия"
         */
        private String bcc;
        /**
         * Поле "Тема"
         */
        private String subject;
        /**
         * Поле "Тело"
         */
        private String body;
        /**
         * Поле "От кого"
         */
        private String from;

        /**
         * Конструктор, инициализирующий поля письма
         *
         * @param to Кому
         * @param cc Копия
         * @param bcc Скрытая копия
         * @param subject Тема
         * @param body Тело
         * @param from От кого
         */
        public Mail(String to, String cc, String bcc, String subject, String body, String from) {
            this.to = to;
            this.cc = cc;
            this.bcc = bcc;
            this.subject = subject;
            this.body = body;
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public String getCc() {
            return cc;
        }

        public String getBcc() {
            return bcc;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getFrom() {
            return from;
        }
    }

    /**
     * Класс формирует транспортный объект MimeMessage и отправялет его по
     * адресам, указанным в классе Mail
     */
    public static void setMailParameters() throws SQLException, Exception {


        HashMap<String,Object> hmParam=Work.getParametersFromConst("mail");
        
        
     //   Document docData = null;

     //   docData = Work.getXmlDocFromConst("server");

      //  Element e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "hostmail", "cell");
        hostMail = (String) hmParam.get("hostmail");

        //Имя пользователя
      //  e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "mailuser", "cell");
        userEmail = (String) hmParam.get("mailuser");

        //e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "fromemail", "cell");
        fromEmail = (String) hmParam.get("fromemail");

       // e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "port", "cell");
        Integer iport=(Integer) hmParam.get("port");
        
        port = iport.toString();

       // e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "password", "cell");
        mailPassword = (String) hmParam.get("password");

        //   e = (Element) XmlTask.getNodeByAttribute(docData.getDocumentElement(), "name", "exjello", "cell");
        //Object  sexJello= e.getAttribute("value");
        // if(sexJello!=null && sexJello.equals("1")){
        //   exJello=true;
        // } else {
        //  exJello=false;
        // }
    }

    public void update(Observable o, Object arg) {

        if (arg instanceof Document) {

            Document doc_user = (Document) arg;

            Map<String, String> hmsend = null;
            try {
                hmsend = XmlTask.getMapAttrubuteByName(doc_user, "name", "value", "cell");
            } catch (Exception ex) {
                MainWorker.deffLoger.error("", ex);
            }

            String s0 = hmsend.get("eml");
            //     String s1=  hmsend.get("sms");
            //   String s2=  hmsend.get("ower");
            //   String s3=  hmsend.get("tfl");

            if (s0.equals("1")) {

                StringBuilder sb_send = new StringBuilder();

                NodeList nlval = doc_user.getElementsByTagName("events");

                for (int i = 0; i < nlval.getLength(); i++) {
                    Element elm = (Element) nlval.item(i);
                    NamedNodeMap nnm = elm.getAttributes();

                    StringBuilder sb_row = new StringBuilder();

                    for (int j = 0; j < nnm.getLength(); j++) {
                        String name = nnm.item(j).getNodeName();
                        String value = elm.getAttribute(name);
                        sb_row.append(value + " | ");

                    }

                    sb_send.append(sb_row.toString() + "\n");

                }

                Map<String, String> hmprop = null;
                try {
                    hmprop = XmlTask.getMapAttrubuteByName(doc_user.getDocumentElement(), "name", "value", "cell");
                } catch (Exception ex) {
                    MainWorker.deffLoger.error("", ex);
                }

                hostMail = hmprop.get("hostmail"); //- хост почтового сервера
                //        mailUser = hmprop.get("mailuser");// - имя пользователя, для авторизации на почтовом сервере
                mailPassword = hmprop.get("password");// - пароль пользователя, для авторизации на почтовом сервере
                fromEmail = hmprop.get("email"); //- адрес эл. почты отправителя
                port = hmprop.get("port");
                //      mailUser = hmprop.get("email");

                mailPassword = "ift";

                //    SendMessage("Отправка сообщения на адрес:" + mailUser, MSG_LOG, STR_NEW);
                if (hostMail == null || hostMail.isEmpty()) {
                    //   SendMessage("Не указан хост почт.серв.!", MSG_ERROR, STR_ANSWER);
                    return;
                }

                if (port == null || port.isEmpty()) {
                    // SendMessage("Не указан порт почт.серв.!", MSG_ERROR, STR_ANSWER);
                    return;
                }

                //   if (emailUser == null || emailUser.isEmpty()) {
                //      SendMessage("Не указан адрес получателя!", MSG_ERROR, STR_ANSWER);
                //        return;
                //  }
                // sendMail(emailUser, fromFullname, "Информация от по 'Inspector'", "Информация от по 'Inspector'");
                //   sendMail(emailUser, fromFullname, "Информация от по 'Inspector'", sb_send.toString());
                //   SendMessage("Отправлено !", MSG_LOG, STR_ANSWER);
            }

        }

    }

    @Override
    protected String doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
