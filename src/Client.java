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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;


// classe
public class Client extends JFrame implements KeyListener, ActionListener { //KL e AL para usar overrides destes

    private Socket socket;

    private JTextField ipField;
    private JTextField portField;
    private JTextField userField;
    private final JTextField msgField;

    private final JTextArea chatArea;

    private Writer outputWriter;
    private BufferedWriter bufferedWriter;

    private final JButton sendButton;

    // constructor
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
        chatArea.setBackground(new Color(255, 255, 255));
        chatArea.setEditable(false);
        msgField = new JTextField(43);
        msgField.setBackground(new Color(247, 239, 237));
        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(219, 36, 36));
        sendButton.setForeground(new Color(255, 255, 255));
        JScrollPane scrollBar = new JScrollPane(chatArea);
        chatArea.setLineWrap(true);
        chatJPanel.add(scrollBar);
        chatJPanel.add(msgField);
        chatJPanel.add(sendButton);

        // estética
        chatJPanel.setBackground(new Color(255, 237, 237));

        // listeners de ação
        sendButton.addActionListener(this);
        sendButton.addKeyListener(this);
        msgField.addKeyListener(this);

        setResizable(false);
        setContentPane(chatJPanel);
        setLocationRelativeTo(null);
        setSize(630,420);
        setVisible(true);

        // listener para quando fechar o chat. Fecha conexões em caso de existirem, senão só encerra o processo.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    exit();
                } catch (Exception exception) {
                    System.out.print("A funcao exit falhou. O servidor está online?\n");
                    setVisible(false);
                    dispose();
                }
            }
        });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }



    public JTextField handleLogin(){
        // painel inicial de login

        JLabel welcomeMsg = new JLabel("Bem-vindo(a) ao kaiWa Group Chat!");
        JLabel ipLabel = new JLabel("IP do Servidor");
        ipField = new JTextField("localhost");
        JLabel portLabel = new JLabel("Porta");
        portField = new JTextField("31415");
        JLabel userLabel = new JLabel("Seu nome");
        userField = new JTextField();
        Object[] texts = {welcomeMsg, userLabel, userField, ipLabel, ipField, portLabel, portField};
        JOptionPane.showMessageDialog(null, texts);

        return userField;
    }

    // conexão com o servidor
    public void connect() throws IOException{
        socket = new Socket(ipField.getText(),Integer.parseInt(portField.getText()));
        outputWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedWriter = new BufferedWriter(outputWriter);
        bufferedWriter.write(userField.getText()+"\r\n");
        bufferedWriter.flush();
    }

    // escuta para ver se tem mensagem escrita
    public void listen() throws IOException {

        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String msg = "";

        while (!"quit".equals(msg)) {
            if (bufferedReader.ready()) {
                msg = bufferedReader.readLine();
                chatArea.append(msg + "\r\n");
            }
        }
    }

    // envio de mensagens
    public void sendMessages(String msg) throws IOException{
        String hour = Integer.toString(LocalDateTime.now().getHour());
        String minutes = Integer.toString(LocalDateTime.now().getMinute());

        if (hour.length() < 2){
            hour = "0" + hour;
        }

        if (minutes.length() < 2){
            minutes = "0" + minutes;
        }

        if(msg.equals("quit")){
            bufferedWriter.write("saiu do chat \r\n");
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
            ioe.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                sendMessages(msgField.getText());
            } catch (IOException e1) {
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

        sendMessages("quit");
        bufferedWriter.close();
        outputWriter.close();
        socket.close();
    }

    public static void main(String []args) throws IOException{

        Client clientApp = new Client();
        try{
            clientApp.connect();
        } catch (ConnectException ce) {
            System.out.println("Conexão falhou. O servidor está online?");
        }
        clientApp.listen();
    }

}
