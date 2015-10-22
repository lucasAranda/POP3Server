/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.swing.JTextArea;
import org.apache.commons.net.pop3.POP3Client;

/**
 *
 * @author Gerado
 */
public class MailPop3Expert implements Runnable {

    private String server;
    private String username;
    private String password;
    private String proto;
    private boolean implicit;
    private boolean connect;
    private int frecuency;
    private POP3Client pop3;
    private JTextArea log;
    private Store store;
    private String formatoFecha = "dd/MM/yyyy HH:mm:ss";

    public void printMessageInfo(BufferedReader reader, int id) throws IOException {
        String from = "";
        String subject = "";
        String line;
        while ((line = reader.readLine()) != null) {
            String lower = line.toLowerCase(Locale.ENGLISH);
            if (lower.startsWith("from: ")) {
                from = line.substring(6).trim();
            } else if (lower.startsWith("subject: ")) {
                subject = line.substring(9).trim();
            }
        }

        log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + Integer.toString(id) + "- From: " + from + "  Subject: " + subject + "\n");
    }

    public boolean connect() {

        try {

            Properties properties = new Properties();

            // Configuracion del servidor
            properties.put("mail.pop3.host", server);
            properties.put("mail.pop3.port", "995");

            // SSL opciones de seguridad
            properties.setProperty("mail.pop3.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.pop3.socketFactory.fallback", "true");
            properties.setProperty("mail.pop3.socketFactory.port",
                    String.valueOf("995"));

            Session session = Session.getDefaultInstance(properties);
            // Coneccion con la cuenta
            store = session.getStore("pop3");
            if (store.isConnected()) {
                disconnect();
            }
            //Conectar...
            store.connect(server, username, password);

        } catch (Exception e) {
            log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "-Error tratando de conectar: " + e.getMessage() + "\n");
            return false;
        }
        log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "-Conectado.. \n");
        return true;

    }

    public void disconnect() {
        try {
            store.close();
        } catch (MessagingException ex) {
            Logger.getLogger(MailPop3Expert.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        //obtengo la agenda
        List<String> agenda = XmlParcerExpert.getInstance().getAgenda();

        while (store.isConnected()) {
            try {

                // Abre la carpeta INBOX
                Folder folderInbox = store.getFolder("INBOX");
                folderInbox.open(Folder.READ_ONLY);

                // Obtiene los mails
                Message[] arrayMessages = folderInbox.getMessages();

                if (arrayMessages.length <= 0) {
                    log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "-No hay mensajes para mostrar \n");

                }

                //procesa los mails
                for (int i = 0; i < arrayMessages.length; i++) {
                    Message message = arrayMessages[i];
                    Address[] fromAddress = message.getFrom();
                    String from = fromAddress[0].toString();
                    String subject = message.getSubject();
                    String sentDate = message.getSentDate().toString();
                    String messageContent = "";
                    String contentType = message.getContentType();

                    if (contentType.contains("multipart")) {
                        // Si el contenido es mulpart
                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                // si contiene un archivo
                            } else {
                                // el contenido del mensaje
                                messageContent = part.getContent().toString();
                            }
                        }

                    } else if (contentType.contains("text/plain")
                            || contentType.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    }

                    //parseo del from
                    if (from.contains("<")) {
                        from = from.substring(from.indexOf("<") + 1, from.length() - 1);
                    }

                    //si esta en la agenda
                    if (agenda.contains(from)) {
                        //obtiene la trama
                        try{
                            messageContent = messageContent.substring(messageContent.indexOf("&gt"), messageContent.indexOf("&lt;") + 4);
                            if (messageContent.startsWith("&gt") && messageContent.endsWith("&lt;")) {
                                log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "PROCESANDO MAIL - From: " + from + "  Subject: " + subject + "\n");
                                //procesa el mail
                                XmlParcerExpert.getInstance().processAndSaveMail(from, messageContent);
                            } else {
                                log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "MAIL RECHAZADO: no cumple con la trama - From: " + from + "  Subject: " + subject + "\n");
                            }
                        }catch(Exception e){
                                log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "MAIL RECHAZADO: no cumple con la trama - From: " + from + "  Subject: " + subject + "\n");                        
                        }
                    }else{
                        log.setText(log.getText() + new SimpleDateFormat(formatoFecha).format(new Date()) + "MAIL RECHAZADO: remitente rechazado - From: " + from + "  Subject: " + subject + "\n");

                    }

                }

                folderInbox.close(false);

                //duerme el hilo por el tiempo de la frecuencia
                Thread.sleep(frecuency * 60 * 1000);

            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (InterruptedException ex) {
                Logger.getLogger(MailPop3Expert.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(MailPop3Expert.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProto() {
        return proto;
    }

    public void setProto(String proto) {
        this.proto = proto;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public int getFrecuency() {
        return frecuency;
    }

    public void setFrecuency(int frecuency) {
        this.frecuency = frecuency;
    }

    public JTextArea getLog() {
        return log;
    }

    public void setLog(JTextArea log) {
        this.log = log;
    }

}
