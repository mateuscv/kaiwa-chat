// importações
import java.awt.event.*;
import java.net.ConnectException;
import java.net.URL;
import java.net.Socket;
import java.awt.Color;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;


// classe
public class Client extends JFrame implements KeyListener, ActionListener { //KL e AL para usar overrides destes

    private Socket socket;

    private JTextField ipField;
    private JTextField portField;
    private JTextField userField;
    private final JTextField msgField;

    private final JTextArea chatArea;

    private OutputStream outputStream;
    private Writer writer;
    private BufferedWriter bufferedWriter;

    private final JButton sendButton;


    public JTextField handleLogin(){
        // painel inicial de login

        JLabel welcomeMsg = new JLabel("Bem-vindo(a) ao kaiWa Group Chat!");
        JLabel ipLabel = new JLabel("IP do Servidor");
        ipField = new JTextField("127.0.0.1");
        JLabel portLabel = new JLabel("Porta");
        portField = new JTextField("31415");
        JLabel userLabel = new JLabel("Seu nome");
        userField = new JTextField();
        Object[] texts = {welcomeMsg, userLabel, userField, ipLabel, ipField, portLabel, portField};
        JOptionPane.showMessageDialog(null, texts);

        return userField;
    }

    // criação das GUIs
    public Client() throws IOException {

        // login GUI
        userField = handleLogin();

        // checa se o usuário preencheu seu nome no login
        while (userField.getText() == null || userField.getText().trim().isEmpty()) {
            System.out.println("username não encontrado. Você o digitou?");
            userField = handleLogin();
        }

        // janela de conversa:

        // meta
        JPanel chatJPanel = new JPanel();
        URL url = new URL("https://cdn.discordapp.com/attachments/783089532437266432/783782711785160765/icon-backup.png");
        Image icon = ImageIO.read(url);
        setIconImage(icon);
        setTitle("kaiWa Chat: " +  userField.getText());

        // elementos
        chatArea = new JTextArea(20,50);
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(240,240,240));
        msgField = new JTextField(44);
        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(194, 172, 193));
        sendButton.setForeground(new Color(212, 209, 203));
        JScrollPane scrollBar = new JScrollPane(chatArea);
        chatArea.setLineWrap(true);
        chatJPanel.add(scrollBar);
        chatJPanel.add(msgField);
        chatJPanel.add(sendButton);

        // estética
        chatJPanel.setBackground(new Color(245, 218, 196));
        chatArea.setBorder(BorderFactory.createEtchedBorder(Color.PINK, Color.PINK));
        msgField.setBorder(BorderFactory.createEtchedBorder(Color.PINK, Color.PINK));

        // listeners de ação
        sendButton.addActionListener(this);
        sendButton.addKeyListener(this);
        msgField.addKeyListener(this);

        setContentPane(chatJPanel);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(630,420);
        setVisible(true);

        // listener para quando fechar o chat. Fecha conexões em caso de existirem, senão só encerra o processo.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    exit();
                } catch (Exception exception) {
                    System.out.printf("A funcao exit falhou. O servidor está online?\n");
                    exception.printStackTrace();
                    setVisible(false);
                    dispose();
                }
            }
        });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // conexão com o servidor
    public void connect() throws IOException{
        socket = new Socket(ipField.getText(),Integer.parseInt(portField.getText()));
        outputStream = socket.getOutputStream();
        writer = new OutputStreamWriter(outputStream);
        bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(userField.getText()+"\r\n");
        bufferedWriter.flush();
    }

    // escuta
    public void listen() throws IOException{

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";

        while(!"Sair".equalsIgnoreCase(msg))

            if(bfr.ready()){
                msg = bfr.readLine();
                if(msg.equals("Sair"))
                    chatArea.append("Servidor caiu! \r\n");
                else
                    chatArea.append(msg+"\r\n");
            }
    }

    // envio de mensagens
    public void sendMessages(String msg) throws IOException{
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String hour = Integer.toString(timestamp.getHours());
        String minutes = Integer.toString(timestamp.getMinutes());

        if (hour.length() < 2){
            hour = "0" + hour;
        }

        if (minutes.length() < 2){
            minutes = "0" + minutes;
        }

        if(msg.equals("Sair")){
            bufferedWriter.write("Desconectado \r\n");
            chatArea.append("Desconectado \r\n");
        }else{
            bufferedWriter.write(msg+"\r\n");
            chatArea.append("[" + hour  + ":" + minutes + "]" + " Você" + " disse: " + msgField.getText()+"\r\n");
        }
        bufferedWriter.flush();
        msgField.setText("");
    }

    public void actionPerformed(ActionEvent e) {

        try {
            if(e.getActionCommand().equals(sendButton.getActionCommand()))
                sendMessages(msgField.getText());
        } catch (IOException ioe) {
            // TODO Auto-generated catch block
            ioe.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                sendMessages(msgField.getText());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    // codigo de saída
    public void exit() throws IOException{

        sendMessages("Sair");
        bufferedWriter.close();
        writer.close();
        outputStream.close();
        socket.close();
        System.exit(1);
    }

    public static void main(String []args) throws IOException{

        Client clientApp = new Client();
        try{
            clientApp.connect();
        } catch (ConnectException ce) {
            ce.printStackTrace();
            System.out.println("Conexão falhou. O servidor está online?");
        }
        clientApp.listen();
    }

}
