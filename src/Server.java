// importações
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.IOException;


public class Server extends Thread {

    private final Socket talk;
    private BufferedReader clientBufferedReader;
    private static ArrayList <BufferedWriter> clientsList;

    private String userName;

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
    public void sendMessage(String msg, BufferedWriter bwSender) throws  IOException {

        BufferedWriter bufferedWriterReceiver;

        String hour = Integer.toString(LocalDateTime.now().getHour());
        String minutes = Integer.toString(LocalDateTime.now().getMinute());

        if (hour.length() < 2){
            hour = "0" + hour;
        }

        if (minutes.length() < 2){
            minutes = "0" + minutes;
        }

        for(BufferedWriter bufferedWriter : clientsList){
            bufferedWriterReceiver = bufferedWriter;
            if(bwSender != bufferedWriterReceiver){ //manda msg pra todos que não são ele mesmo.
                if (!msg.equals("saiu do chat ")){
                    bufferedWriter.write("[" + hour  + ":" + minutes + "] " + userName + " disse: " + msg+"\r\n");
                    bufferedWriter.flush(); //limpa o stream
                } else {
                    bufferedWriter.write("[" + hour  + ":" + minutes + "] >>>> " + userName + " " + msg+"<<<<\r\n");
                    bufferedWriter.flush(); //limpa o stream
                }
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

            while(!"quit".equals(msg) && msg != null)
            {
                msg = clientBufferedReader.readLine();
                if (msg == null){
                    continue;
                }
                sendMessage(msg, bufferedWriter);
            }

            clientsList.remove(bufferedWriter);

        }catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void acceptConnections(ServerSocket server) throws IOException {
        // loop de aceitar conexões c clientes - servidor fecha após 5 min sem conexoes
        while(true){
            try {
                Socket talk = server.accept();
                System.out.println("Conexão com o cliente estabelecida.");
                Thread clientThread = new Server(talk); //criação do servidor
                clientThread.start();
            }catch (SocketTimeoutException ste){
                ste.printStackTrace();
                System.out.println("Servidor encerrando - tempo limite de ociosidade atingido.");
                break;
            }
        }
    }

    public static void main(String[] args){
        BufferedReader cli_reader = new BufferedReader(new InputStreamReader(System.in));

        try{
            System.out.println("Digite a porta (recomendada: 31415)");
            String port_str = cli_reader.readLine();
            int port = Integer.parseInt(port_str);
            ServerSocket server = new ServerSocket(port);
            server.setSoTimeout(86400*1000);// servidor fica 24h aguardando conexao antes de encerrar
            System.out.println("Estabelecido limite de 24 horas de ociosidade.");
            System.out.println("Servidor online na porta " + port_str + ".");

            clientsList = new ArrayList<>();

            acceptConnections(server);

        }catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
