/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shoumik
 */
public class HTTPServer {

    static final int PORT = 8080;
    static int id = 1;

    public static void main(String[] args) throws IOException {

        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        while (true) {
            Socket s = serverConnect.accept();
            Worker wt = new Worker(s, id);

            Thread t = new Thread(wt);
            t.start();
            System.out.println("Client " + id + " connected to server");
            id++;
            //BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //PrintWriter pr = new PrintWriter(s.getOutputStream());
            //String input = in.readLine();
            //System.out.println("Here Input : "+input);
        }

    }

}

class Worker implements Runnable {

    private Socket connectionSocket;
    private int id;

    public Worker(Socket s, int id) {
        this.connectionSocket = s;
        this.id = id;
    }

    @Override
    public void run() {

        String input, response = "";
        String MIMEtype = "";
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream());
            OutputStream os = connectionSocket.getOutputStream();
            input = inFromClient.readLine();
            System.out.println("Here response for Clinet " + id + "\n");
            String firstLine[] = input.split("/");
            String typeFile[] = firstLine[1].split(" ");
            while (!input.isEmpty()) {
                response = response.concat(input + "\n");
                //System.out.println("Here Input for Client " + id + ": "+input);
                input = inFromClient.readLine();
            }
            System.out.println(response);

            File file = new File(typeFile[0]);

            if (typeFile[0].contains("html")) {
                MIMEtype = "text/html";
                file = new File("index.html");
            } else if (typeFile[0].contains("jpg")) {
                MIMEtype = "image/jpeg";
                file = new File("helloWorld.jpeg");
            } else {
                MIMEtype = "text/html";
                file = new File("index.html");
            }

            if (firstLine[0].contains("GET")) {
                if (file.exists() == true) {
                    outToClient.print("HTTP/1.1 200 OK\n");
                    outToClient.print("MIME-Version: 1.0\n");
                    outToClient.print("Date: " + getServerTime() + "\n");
                    outToClient.print("Server: HelloThere\n");
                    outToClient.print("Content-Type: " + MIMEtype + "\n");
                    outToClient.print("Content-length: " + file.length() + "\n\n");
                } else {
                    file = new File("error.html");
                    outToClient.print("HTTP/1.1 404 Not found\n");
                    outToClient.print("MIME-Version: 1.0\n");
                    outToClient.print("Date: " + getServerTime() + "\n");
                    outToClient.print("Server: HelloThere\n");
                    outToClient.print("Content-Type: text/html\n");
                    outToClient.print("Content-length: " + file.length() + "\n\n");
                }
                /*File file = new File("http post.html");
                FileOutputStream fos = new FileOutputStream(file);
                outToClient.print("hello");
                outToClient.flush();
                outToClient.close();
                os.write("hello".getBytes("UTF-8"));
                os.flush();
                os.close();*/

                outToClient.flush();

                FileInputStream fin = new FileInputStream(file);
                BufferedInputStream inFromFile = new BufferedInputStream(fin);

                byte[] contents;
                long fileLength = file.length();
                long current = 0;

                while (current != fileLength) {
                    int size = 10000;
                    if (fileLength - current >= size) {
                        current += size;
                    } else {
                        size = (int) (fileLength - current);
                        current = fileLength;
                    }
                    contents = new byte[size];
                    inFromFile.read(contents, 0, size);
                    os.write(contents);
                }
                os.flush();
                System.out.println("File sent successfully!");

            } else if (firstLine[0].contains("POST")) {
                char array[] = new char[600];
                inFromClient.read(array);
                String requestedData = new String(array);
                System.out.println("Sender Name \n" + requestedData + "\n");
                String senderData[] = requestedData.split("=");

                String senderName = senderData[1];
                System.out.println(senderName + "\n");

                file = new File("index.html");

                BufferedReader inFromFilePost = new BufferedReader(new FileReader(file));
                String finalContent = "", content = "";
                while ((content = inFromFilePost.readLine()) != null) {
                    if (content.contains("Post->")) {
                        String[] splitPost = content.split("Post->");
                        content = splitPost[0] + "Post-> " + senderName + splitPost[1];
                    }
                    finalContent = finalContent + "\n" + content;
                }
                inFromFilePost.close();

                outToClient.print("HTTP/1.1 200 OK\n");
                outToClient.print("MIME-Version: 1.0\n");
                outToClient.print("Date: " + getServerTime() + "\n");
                outToClient.print("Server: HelloThere\n");
                outToClient.print("Content-Type: text/html\n");
                outToClient.print("Content-length: " + finalContent.length() + "\n\n");
                
                outToClient.flush();
                
                outToClient.println(finalContent);
                outToClient.flush();
            }

            os.close();
            inFromClient.close();
            outToClient.close();
        } catch (Exception e) {
            System.out.println("Not Working for Client " + id + " !!");
        }

    }

    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

}
