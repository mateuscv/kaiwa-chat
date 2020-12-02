// imports client

import java.awt.event.*;
import java.net.Socket;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

// client class

public class Client extends JFrame implements ActionListener, KeyListener {

    private JLabel ipLabel;
    private JLabel portLabel;
    private JLabel userLabel;
    //private JLabel chatLabel;
    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;

    private JTextArea texto;
    private JTextField txtMsg;

    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou ;
    private Writer ouw;
    private BufferedWriter bfw;

    private JButton btnSend;
    private JButton btnSair;

    // criação da GUI
    public Client() throws IOException{

        // painel inicial de login
        JLabel lblMessage = new JLabel("Bem-vindo(a) ao kaiWa Group Chat!");
        ipLabel = new JLabel("IP do Servidor");
        txtIP = new JTextField("127.0.0.1");
        portLabel = new JLabel("Porta");
        txtPorta = new JTextField("12345");
        userLabel = new JLabel("Seu nome");
        txtNome = new JTextField("usuario");
        Object[] texts = {lblMessage, userLabel, txtNome, ipLabel, txtIP, portLabel, txtPorta  };
        JOptionPane.showMessageDialog(null, texts);

        pnlContent = new JPanel();
        texto = new JTextArea(20,50);
        texto.setEditable(false);
        texto.setBackground(new Color(240,240,240));
        txtMsg = new JTextField(44);
        //chatLabel = new JLabel(" ");
        //chatLabel.setFont(new Font("Verdana", Font.PLAIN, 16));
        btnSend = new JButton("Enviar");
        btnSend.setToolTipText("Enviar Mensagem");
        //btnSair = new JButton("Desconectar");
        //btnSair.setToolTipText("Sair do Chat");
        btnSend.addActionListener(this);
        //btnSair.addActionListener(this);
        btnSend.addKeyListener(this);
        txtMsg.addKeyListener(this);
        JScrollPane scroll = new JScrollPane(texto);
        texto.setLineWrap(true);
        //pnlContent.add(chatLabel);
        pnlContent.add(scroll);
        pnlContent.add(txtMsg);
        //pnlContent.add(btnSair);
        pnlContent.add(btnSend);
        pnlContent.setBackground(new Color(245, 218, 196));
        texto.setBorder(BorderFactory.createEtchedBorder(Color.PINK,Color.PINK));
        txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.PINK, Color.PINK));
        URL url = new URL("https://cdn.discordapp.com/attachments/783089532437266432/783782711785160765/icon-backup.png");
        Image icon = ImageIO.read(url);
        setIconImage(icon);
        setTitle("kaiWa Chat: " +  txtNome.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(630,420);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    exit();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }


    // conexão com o servidor
    public void connect() throws IOException{
        socket = new Socket(txtIP.getText(),Integer.parseInt(txtPorta.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtNome.getText()+"\r\n");
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
            texto.append("Desconectado \r\n");
        }else{
            bfw.write(msg+"\r\n");
            texto.append("[" + hour  + ":" + minutes + "]" + " Você" + " disse: " + txtMsg.getText()+"\r\n");
        }
        bfw.flush();
        txtMsg.setText("");
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
                    texto.append("Servidor caiu! \r\n");
                else
                    texto.append(msg+"\r\n");
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
            if(e.getActionCommand().equals(btnSend.getActionCommand()))
                sendMessages(txtMsg.getText());
            else
            if(e.getActionCommand().equals(btnSair.getActionCommand()))
                exit();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    public void keyPressed(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                sendMessages(txtMsg.getText());
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

        Client app = new Client();
        app.connect();
        app.listen();
    }

}
