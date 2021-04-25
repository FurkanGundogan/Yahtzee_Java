/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yahtzeeclient;

import game.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static yahtzeeclient.Client.sInput;
import game.Game;
import javax.swing.JButton;

/**
 *
 * @author INSECT
 */
// serverdan gelecek mesajları dinleyen thread
class Listen extends Thread {

    @Override
    public void run() {
        //soket bağlı olduğu sürece dön
        while (Client.socket.isConnected()) {
            try {
                //mesaj gelmesini bloking olarak dinyelen komut
                Message received = (Message) (sInput.readObject());
                //mesaj gelirse bu satıra geçer
                //mesaj tipine göre yapılacak işlemi ayır.
                switch (received.type) {
                    case Name:
                        break;
                    case RivalConnected:
                        String name = received.content.toString();
                        Game.ThisGame.txt_rival_name.setText(name);
                        Game.ThisGame.lbl_rival.setText(name);
                        Game.ThisGame.tmr_slider.start();
                        break;
                    case Disconnect:
                        break;
                    case ROLL:
                        // ROLL mesajı karşıdan gelen zarları alır ve ekrana düzelterek basar.
                        Game.ThisGame.rolledDicesAsInt = (int) received.content;
                        Game.ThisGame.putDices();
                        break;
                    case PLAYABLE:
                        // Başlangıçta, bağlanma durumuna göre gelen 0 ve 1 sayılarıyla
                        // İkinci oyuncu oyuna başlar.
                        Game.ThisGame.myplayable = (int) received.content;
                        if ((int) received.content == 1) {
                            Game.ThisGame.setMyTurn(true);
                        }
                        break;
                    case DICECLICK:
                        // Karşı tarafın Stun işlemlerini görebilmek için.
                        Game.ThisGame.toggleRivalDice(received.content.toString());
                        break;
                    case PNTSELECT:
                        // Karşı taraf bir seçip yaptıysa bunu kendi ekranında onun listesine yazar.
                        Game.ThisGame.writeRivalPnt(received.content.toString());
                        Game.ThisGame.setMyTurn(true);
                        break;
                }

            } catch (IOException ex) {

                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);

                break;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);

                break;
            }
        }

    }
}

public class Client {

    //her clientın bir soketi olmalı
    public static Socket socket;

    //verileri almak için gerekli nesne
    public static ObjectInputStream sInput;
    //verileri göndermek için gerekli nesne
    public static ObjectOutputStream sOutput;
    //serverı dinleme thredi 
    public static Listen listenMe;

    public static void Start(String ip, int port) {
        try {
            // Client Soket nesnesi
            Client.socket = new Socket(ip, port);
            Client.Display("Servera bağlandı");

            // input stream
            Client.sInput = new ObjectInputStream(Client.socket.getInputStream());
            // output stream
            Client.sOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start();

            //ilk mesaj olarak isim gönderiyorum
            Message msg = new Message(Message.Message_Type.Name);
            msg.content = Game.ThisGame.txt_name.getText();
            Client.Send(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //client durdurma fonksiyonu
    public static void Stop() {
        try {
            if (Client.socket != null) {
                Client.listenMe.stop();
                Client.socket.close();
                Client.sOutput.flush();
                Client.sOutput.close();

                Client.sInput.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void Display(String msg) {

        System.out.println(msg);

    }

    //mesaj gönderme fonksiyonu
    public static void Send(Message msg) {
        try {
            Client.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
