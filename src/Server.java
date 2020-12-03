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
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.IOException;


public class Server extends Thread {
    private static ArrayList<BufferedWriter> clients;
    private static ServerSocket server;
    private String name;
    private final Socket talk;
    private BufferedReader bufferedReader;

    public Server(Socket talk){
        this.talk = talk;
        try {
            InputStream inputStream = talk.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(BufferedWriter bwOutput, String msg) throws  IOException
    {
        BufferedWriter bufferedWriterS;

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String hour = Integer.toString(timestamp.getHours());
        String minutes = Integer.toString(timestamp.getMinutes());

        for(BufferedWriter bufferedWriter : clients){
            bufferedWriterS = (BufferedWriter)bufferedWriter;
            if(!(bwOutput == bufferedWriterS)){
                bufferedWriter.write("[" + hour  + ":" + minutes + "] " + name + " disse: " + msg+"\r\n");
                bufferedWriter.flush();
            }
        }
    }

    public void run(){

        try{

            String msg;
            OutputStream outputStream =  this.talk.getOutputStream();
            Writer outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            clients.add(bufferedWriter);
            name = msg = bufferedReader.readLine();

            while(!"Sair".equalsIgnoreCase(msg) && msg != null)
            {
                msg = bufferedReader.readLine();
                if (msg == null){
                    System.out.println("caiu null");
                    continue;
                }
                sendToAll(bufferedWriter, msg);
                System.out.println(msg + "EU SOU  A MSG");
            }

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
            server = new ServerSocket(Integer.parseInt(portField.getText()));
            clients = new ArrayList<BufferedWriter>();
            System.out.println("Servidor online na porta " + portField.getText());

            // loop de aceitar conexões c clientes
            while(true){
                Socket talk = server.accept();
                System.out.println("Conexão com o cliente estabelecida.");
                Thread clientThread = new Server(talk);
                clientThread.start();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
