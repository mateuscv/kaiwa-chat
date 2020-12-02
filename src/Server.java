import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class Server extends Thread {
    private static ArrayList<BufferedWriter> clients;
    private static ServerSocket server;
    private String name;
    private Socket talk;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;

    public Server(Socket con){
        this.talk = con;
        try {
            in  = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException
    {
        BufferedWriter bwS;

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String hour = Integer.toString(timestamp.getHours());
        String minutes = Integer.toString(timestamp.getMinutes());

        for(BufferedWriter bw : clients){
            bwS = (BufferedWriter)bw;
            if(!(bwSaida == bwS)){
                bw.write("[" + hour  + ":" + minutes + "] " + name + " disse: " + msg+"\r\n");
                bw.flush();
            }
        }
    }

    public void run(){

        try{

            String msg;
            OutputStream ou =  this.talk.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            clients.add(bfw);
            name = msg = bfr.readLine();

            while(!"Sair".equalsIgnoreCase(msg) && msg != null)
            {
                msg = bfr.readLine();
                if (msg == null){
                    System.out.println("caiu null");
                    continue;
                }
                sendToAll(bfw, msg);
                System.out.println(msg + "EU SOU  A MSG");
            }

        }catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args){
        try{
            //Cria os objetos necessário para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("12345");
            Object[] texts = {lblMessage, txtPorta };
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            clients = new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null,"Servidor ativo na porta: "+
                    txtPorta.getText());

            while(true){
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Server(con);
                t.start();
            }

        }catch (Exception e) {

            e.printStackTrace();
        }
    }// Fim do método main

}
