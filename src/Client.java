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
public class Client extends JFrame implements KeyListener, ActionListener {

    private Socket socket;

    private JLabel ipLabel;
    private JLabel portLabel;
    private JLabel userLabel;

    private JTextField ipField;
    private JTextField portField;
    private JTextField userField;
    private JTextField msgField;

    private JTextArea chatArea;

    private JPanel pnlContent;

    private OutputStream ou ;
    private Writer ouw;
    private BufferedWriter bfw;

    private JButton sendButton;


    public JTextField handleLogin(){
        // painel inicial de login

        JLabel welcomeMsg = new JLabel("Bem-vindo(a) ao kaiWa Group Chat!");
        ipLabel = new JLabel("IP do Servidor");
        ipField = new JTextField("127.0.0.1");
        portLabel = new JLabel("Porta");
        portField = new JTextField("31415");
        userLabel = new JLabel("Seu nome");
        userField = new JTextField();
        Object[] texts = {welcomeMsg, userLabel, userField, ipLabel, ipField, portLabel, portField};
        JOptionPane.showMessageDialog(null, texts);

        return userField;
    }

    // criação das GUIs
    public Client() throws IOException{

        userField = handleLogin();

        // checa se o usuário preencheu seu nome
        while (userField.getText() == null || userField.getText().trim().isEmpty()) {
            userField = handleLogin();
        }

        // janela de conversa
        pnlContent = new JPanel();
        chatArea = new JTextArea(20,50);
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(240,240,240));
        msgField = new JTextField(44);
        sendButton = new JButton("Enviar");
        sendButton.setToolTipText("Enviar Mensagem");
        sendButton.addActionListener(this);
        sendButton.addKeyListener(this);
        msgField.addKeyListener(this);
        JScrollPane scrollBar = new JScrollPane(chatArea);
        chatArea.setLineWrap(true);
        pnlContent.add(scrollBar);
        pnlContent.add(msgField);
        pnlContent.add(sendButton);
        pnlContent.setBackground(new Color(245, 218, 196));
        chatArea.setBorder(BorderFactory.createEtchedBorder(Color.PINK,Color.PINK));
        msgField.setBorder(BorderFactory.createEtchedBorder(Color.PINK, Color.PINK));
        URL url = new URL("https://cdn.discordapp.com/attachments/783089532437266432/783782711785160765/icon-backup.png");
        Image icon = ImageIO.read(url);
        setIconImage(icon);
        setTitle("kaiWa Chat: " +  userField.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(630,420);
        setVisible(true);

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
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(userField.getText()+"\r\n");
        bfw.flush();
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
            bfw.write("Desconectado \r\n");
            chatArea.append("Desconectado \r\n");
        }else{
            bfw.write(msg+"\r\n");
            chatArea.append("[" + hour  + ":" + minutes + "]" + " Você" + " disse: " + msgField.getText()+"\r\n");
        }
        bfw.flush();
        msgField.setText("");
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

    // codigo de saída
    public void exit() throws IOException{

        sendMessages("Sair");
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
        System.exit(1);
    }

    public void actionPerformed(ActionEvent e) {

        try {
            if(e.getActionCommand().equals(sendButton.getActionCommand()))
                sendMessages(msgField.getText());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
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
        //TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
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
