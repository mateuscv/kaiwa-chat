// importações
import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.IOException;


public class Server extends Thread {

    private static ArrayList <BufferedWriter> clientsList;

    private String userName;
    private BufferedReader clientBufferedReader;
    private final Socket talk;

    // constructor
    public Server(Socket talk) throws IOException {

        this.talk = talk;

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(talk.getInputStream()); // leitor e conversor chars <-> bytes
            clientBufferedReader = new BufferedReader(inputStreamReader); // estrutura de buffer para processamento do input

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // método que envia as mensagens aos clientes conectados
    public void sendMessage(String msg, BufferedWriter bwOutput) throws  IOException {

        BufferedWriter bufferedWriterOutput;

        String hour = Integer.toString(LocalDateTime.now().getHour());
        String minutes = Integer.toString(LocalDateTime.now().getMinute());

        if (hour.length() < 2){
            hour = "0" + hour;
        }

        if (minutes.length() < 2){
            minutes = "0" + minutes;
        }

        for(BufferedWriter bufferedWriter : clientsList){
            System.out.println(bufferedWriter + ", ");
            bufferedWriterOutput = bufferedWriter;
            if(!(bwOutput == bufferedWriterOutput)){
                bufferedWriter.write("[" + hour  + ":" + minutes + "] " + userName + " disse: " + msg+"\r\n");
                bufferedWriter.flush();
            }
        }
    }

    public void run(){

        try{

            String msg;
            Writer outputStreamWriter = new OutputStreamWriter(this.talk.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter); // analogo ao reader em buffer

            msg = clientBufferedReader.readLine();
            userName = msg;

            clientsList.add(bufferedWriter);

            while(!"Sair".equals(msg) && msg != null)
            {
                msg = clientBufferedReader.readLine();
                if (msg == null){
                    System.out.println("caiu null");
                    continue;
                }
                sendMessage(msg, bufferedWriter);
                System.out.println(msg + " EU SOU  A MSG");
            }

            clientsList.remove(bufferedWriter);

        }catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args){

        try{
            // criação da janela de inicialização do servidor
            JLabel portLabel = new JLabel("Porta:");
            JTextField portField = new JTextField("31415");
            Object[] struct = {portLabel, portField };
            JOptionPane.showMessageDialog(null, struct);
            ServerSocket server = new ServerSocket(Integer.parseInt(portField.getText()));
            clientsList = new ArrayList <BufferedWriter>();
            System.out.println("Servidor online na porta " + portField.getText());

            // loop de aceitar conexões c clientes - infinito
            while(true){
                Socket talk = server.accept();
                System.out.println("Conexão com o cliente estabelecida.");
                Thread clientThread = new Server(talk); //criação do servidor
                clientThread.start();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
