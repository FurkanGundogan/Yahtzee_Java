/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.awt.Image;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import yahtzeeclient.Client;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JButton;
import static game.Message.Message_Type.Disconnect;
import java.awt.Color;

/**
 *
 * @author INSECT
 */
public class Game extends javax.swing.JFrame {

    //statik oyun değişkeni
    public static Game ThisGame;

    public Thread tmr_slider;

    // Oyun durumu: bitiş durumu için kontrol ediliyor.
    public int gameState = -1;

    // Birinci ve ikinci oyuncu için farklı değerler alır. 1 olan başlar.
    public int myplayable = 0;

    // Birden fazla yahtzee yapması durumu kontrolü için tutulan değişken.
    public int myYahtzeeCount = 0;

    // 13'e kadar sınırlı olan round sayıları.
    public int myRoundNum = 0;
    public int rivalRoundNum = 0;

    // Zar atmak için rand değişkeni.
    Random rand;

    // Aslında tüm zarlar da denebilir.
    public ArrayList<JButton> rollableDices;

    // Stun durumundaki zarların listesi.
    public ArrayList<JButton> stunnedDices;

    // Sağ bölümdeki Client'ın puan listesi(butonlar)
    public ArrayList<JButton> myPoints;

    // Sağ bölümdeki Rival'in puan listesi(butonlar)
    public ArrayList<JButton> rivalPoints;

    // Random atılan zarların sayı değerlerinin dizisi
    public int[] rolledDices = new int[6];

    // Zarları mesaj olarak gönderirken diziyi tamsayıya çeviriyorum.
    public int rolledDicesAsInt = 111111;

    // Sağ tarafta, kullanıcının butonlarına denk gelen puan listesi
    // O butonlardan her seçim yapıldığında bu listeye yazılıyor.
    public String[] myList = new String[16];

    // O anki oyuncunun 3 defa zar atabilmesi için tutulan değişken.
    public int counter = 0;

    /**
     * Creates new form Game
     */
    @SuppressWarnings("empty-statement")
    public Game() {
        initComponents();
        ThisGame = this;
        lbl_winner.setVisible(false);
        lbl_winner_title.setVisible(false);

        rand = new Random();
        rollableDices = new ArrayList<JButton>();
        stunnedDices = new ArrayList<JButton>();
        myPoints = new ArrayList<JButton>();
        rivalPoints = new ArrayList<JButton>();
        this.setDimensions();
        putDicesToList();

        tmr_slider = new Thread(() -> {

            //soket bağlıysa dönsün
            while (Client.socket.isConnected()) {

                try {

                    if (gameState == -1) {
                        //gameState -1 durumu aktif oyun halidir.

                        Client.Display("ben:" + myRoundNum + " - rival:" + rivalRoundNum + "\n Oyun Bitti:");
                        if (myRoundNum == 13 && rivalRoundNum == 13) {
                            // Totalde iki taraf da 13 tur oynadıktan sonra oyun tamamlanır.
                            Thread.sleep(10);
                            finishGame();
                            Thread.sleep(10);
                            setMyTurn(false);

                            Thread.sleep(2);
                            gameState = 1;
                            // gameState 1 olduğunda artık oyun döngüsü bitmiş oluyor ve else koşuluna düşüyor.

                        }

                    } else {
                        // Oyun durumu değiştikten sonra client durur.
                        Thread.sleep(4000);
                        Client.Stop();
                        tmr_slider.stop();
                        Thread.sleep(7000);

                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    public void setDimensions() {
        // Liste butonlarını iki ayrı arrayliste atmak
        // ve bu buttonların boylarını sabitleme
        // işlemlerini yapan fonksiyon
        // Aynı zamanda başlangıçta çağırıldığı için
        // İlk durum zar fotograflarini basar.

        myPoints.add(btn_p1_1);
        myPoints.add(btn_p1_2);
        myPoints.add(btn_p1_3);
        myPoints.add(btn_p1_4);
        myPoints.add(btn_p1_5);
        myPoints.add(btn_p1_6);
        myPoints.add(btn_p1_7);
        myPoints.add(btn_p1_8);
        myPoints.add(btn_p1_9);
        myPoints.add(btn_p1_10);
        myPoints.add(btn_p1_11);
        myPoints.add(btn_p1_12);
        myPoints.add(btn_p1_13);
        myPoints.add(btn_p1_14);
        myPoints.add(btn_p1_15);
        myPoints.add(btn_p1_16);
        for (JButton myPoint : myPoints) {
            myPoint.setMinimumSize(new Dimension(75, 31));
            myPoint.setText("0");
        }

        rivalPoints.add(btn_p2_1);
        rivalPoints.add(btn_p2_2);
        rivalPoints.add(btn_p2_3);
        rivalPoints.add(btn_p2_4);
        rivalPoints.add(btn_p2_5);
        rivalPoints.add(btn_p2_6);
        rivalPoints.add(btn_p2_7);
        rivalPoints.add(btn_p2_8);
        rivalPoints.add(btn_p2_9);
        rivalPoints.add(btn_p2_10);
        rivalPoints.add(btn_p2_11);
        rivalPoints.add(btn_p2_12);
        rivalPoints.add(btn_p2_13);
        rivalPoints.add(btn_p2_14);
        rivalPoints.add(btn_p2_15);
        rivalPoints.add(btn_p2_16);
        for (JButton rivalPoint : rivalPoints) {
            rivalPoint.setMinimumSize(new Dimension(75, 31));
            rivalPoint.setText("0");
        }

        btn_dice1.setIcon(new ImageIcon("src//images//dices//1.png"));
        btn_dice2.setIcon(new ImageIcon("src//images//dices//2.png"));
        btn_dice3.setIcon(new ImageIcon("src//images//dices//3.png"));
        btn_dice4.setIcon(new ImageIcon("src//images//dices//4.png"));
        btn_dice5.setIcon(new ImageIcon("src//images//dices//5.png"));
        btn_dice6.setIcon(new ImageIcon("src//images//dices//6.png"));
    }

    public void putDicesToList() {
        // Tüm zarları rollable dizisine atar

        rollableDices.add(btn_dice1);
        rollableDices.add(btn_dice2);
        rollableDices.add(btn_dice3);
        rollableDices.add(btn_dice4);
        rollableDices.add(btn_dice5);
        rollableDices.add(btn_dice6);
    }

    public void setMyTurn(boolean b) {
        // Client'ın o anda oynama ya da bekleme durumunu erişim engelleyerek düzenler.
        stunnedDices.clear();
        btn_rolldice.setEnabled(b);
        btn_dice1.setEnabled(b);
        btn_dice2.setEnabled(b);
        btn_dice3.setEnabled(b);
        btn_dice4.setEnabled(b);
        btn_dice5.setEnabled(b);
        btn_dice6.setEnabled(b);
    }

    public void toggleDice(JButton btn) {
        // Stunlamayı toggle eden fonksiyon.
        if (counter != 0) {
            // İlk turda zar stunlamayı engelliyorum.
            // Çünkü son durum, karşı tarafın attığı durum.

            if (isDiceStun(btn)) {
                // Zaten stun durumundaysa stunu kaldırıyorum.
                stunnedDices.remove(btn);
                stunEffect(btn, false);
                //   unStun edilenlerin tekrar yerine dönmesi
                //   btn.setLocation(btn.getLocation().x, btn.getLocation().y + 200);

                // DICECLICK ile bu stun durumu bilgisinin karşı tarafın da almasını sağlıyorum.
                String cont = "";
                Message msg = new Message(Message.Message_Type.DICECLICK);
                int x = findDice(btn);
                cont += String.valueOf(x);
                cont += "-up";
                // index + location şeklinde bir mesaj gidiyor.
                msg.content = cont;
                Client.Send(msg);

            } else {
                if (stunnedDices.size() < 6) {
                    stunnedDices.add(btn);
                    stunEffect(btn, true);
                    // stun edilenlerin yukarı çekilmesi.
                    //  btn.setLocation(btn.getLocation().x, btn.getLocation().y - 200);

                    // index + location şeklinde bir mesaj gidiyor.
                    String cont = "";
                    Message msg = new Message(Message.Message_Type.DICECLICK);
                    int x = findDice(btn);
                    cont += String.valueOf(x);
                    cont += "-down";
                    // index + location şeklinde bir mesaj gidiyor.
                    msg.content = cont;
                    Client.Send(msg);

                }

            }
        }
    }

    public boolean isDiceStun(JButton dice) {
        // Zarlar için dondurulma durumu kontrolü
        boolean state = false;
        for (JButton x : stunnedDices) {
            if (dice.equals(x)) {
                state = true;
                break;
            }
        }

        return state;
    }

    public void stunEffect(JButton dice, boolean state) {
        int i = findDice(dice);

        if (state) {
            switch (rolledDices[i]) {
                case 1:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//stunneddices//s1.png"));
                    break;
                case 2:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//stunneddices//s2.png"));
                    break;
                case 3:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//stunneddices//s3.png"));
                    break;
                case 4:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//stunneddices//s4.png"));
                    break;
                case 5:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//stunneddices//s5.png"));
                    break;
                case 6:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//stunneddices//s6.png"));
                    break;
            }
        } else {
            switch (rolledDices[i]) {
                case 1:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//1.png"));
                    break;
                case 2:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//2.png"));
                    break;
                case 3:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//3.png"));
                    break;
                case 4:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//4.png"));
                    break;
                case 5:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//5.png"));
                    break;
                case 6:
                    rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//6.png"));
                    break;
            }
        }

    }

    public int findDice(JButton btn) {
        // Tüm zarlar arasından bir zarı getirmek için ayarladığım fonksiyon.
        int index = 0;
        for (JButton r : rollableDices) {
            if (r.equals(btn)) {
                break;
            }
            index++;
        }
        return index;
    }

    public void toggleRivalDice(String msg) {
        // Client'a gelen mesaj sonucunca çalışır.
        // Karşı tarafın bir zarı durdurması durumunda
        // bu bilgi kendisine gelir ve ekranda iki tarafın da aynı
        // zarları görmesini sağlar.
        String[] elements = msg.split("-");

        int indext = Integer.valueOf(elements[0]);
        String move = elements[1];

        if (move.equals("up")) {
            //  rollableDices.get(indext).setLocation(rollableDices.get(indext).getLocation().x, (rollableDices.get(indext).getLocation().y) - 200);
        } else if (move.equals("down")) {
            //  rollableDices.get(indext).setLocation(rollableDices.get(indext).getLocation().x, (rollableDices.get(indext).getLocation().y) + 200);
        }

    }

    public void rollthedices() {
        // Tüm zarları gezer ve eğer zar
        // Stun olanların olduğu arraylistte yoksa
        // Zarı random numarayla atar.

        for (int i = 0; i < 6; i++) {
            if (!isDiceStun(rollableDices.get(i))) {
                int randomNum = ThreadLocalRandom.current().nextInt(1, 6 + 1);

                switch (randomNum) {
                    case 1:
                        rolledDices[i] = 1;
                        break;
                    case 2:
                        rolledDices[i] = 2;
                        break;
                    case 3:
                        rolledDices[i] = 3;
                        break;
                    case 4:
                        rolledDices[i] = 4;
                        break;
                    case 5:
                        rolledDices[i] = 5;
                        break;
                    case 6:
                        rolledDices[i] = 6;
                        break;

                }

            }
        }

    }

    public void putDices() {
        // Atılmış zarları switch case yapısıyla kontrol ederek
        // Doğru zar fotografini gösterir.
        int subnumber = rolledDicesAsInt;

        for (int i = 5; i >= 0; i--) {
            rolledDices[i] = subnumber % 10;
            subnumber /= 10;
            if (!isDiceStun(rollableDices.get(i))) {
                // Stun durumundaysa mavi kalacak, değilse gelen zar resmi atanacak
                switch (rolledDices[i]) {
                    case 1:
                        rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//1.png"));
                        break;
                    case 2:
                        rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//2.png"));
                        break;
                    case 3:
                        rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//3.png"));
                        break;
                    case 4:
                        rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//4.png"));
                        break;
                    case 5:
                        rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//5.png"));
                        break;
                    case 6:
                        rollableDices.get(i).setIcon(new ImageIcon("src//images//dices//6.png"));
                        break;
                }
            }
        }

    }

    public void showPointsonList() {
        // Oyun kurallarına göre zarlardaki puanı hesaplar
        // Çagirdigi fonksiyonlar bu puanları listede gerekli
        // yere yazar.
        checkFirstSix();
        check4OfaKindd();
        check3OfaKind();
        checkFullHouse();
        checkSmallStr();
        checkLargeStr();
        checkChance();
        checkYahtzee();
    }

    public void checkFirstSix() {
        // Ones-Sixes arası puan işlemlerinin hesabı ve listede gösterilmesi.
        int pnt = 0;

        for (int i = 0; i < 6; i++) {
            pnt = 0;
            if (myList[i] == null) {
                for (int j = 0; j < 6; j++) {
                    if (rolledDices[j] == (i + 1)) {
                        pnt += (i + 1);

                    } else {
                    }

                }
                myPoints.get(i).setText(String.valueOf(pnt));
                //String str="10";
                //  myPoints.get(i).setText(String.valueOf(str));
                myPoints.get(i).setEnabled(true);
            } else {

            }
        }

    }

    public void check3OfaKind() {
        // Bir zardan üç adet olma durumun kontrolü
        // Varsa puanın gösterilmesi.
        if (myList[8] == null) {
            boolean found = false;

            int[] counts = new int[6];
            int p = 0;
            int pAllDices = 0;
            for (int i = 0; i < 6; i++) {
                int x = rolledDices[i];
                counts[x - 1]++;
                pAllDices += x;
            }

            for (int i = 0; i < 6; i++) {
                if (counts[i] >= 3) {
                    p = pAllDices;
                    found = true;
                    break;
                }
            }

            if (found) {
                myPoints.get(8).setText(String.valueOf(p));
                myPoints.get(8).setEnabled(true);
            } else {
                myPoints.get(8).setText("0");
                myPoints.get(8).setEnabled(true);
            }

        }
    }

    public void check4OfaKindd() {
        // Bir zardan dört adet olma durumun kontrolü
        // Varsa puanın gösterilmesi.
        if (myList[9] == null) {
            boolean found = false;
            int[] counts = new int[6];
            int p = 0;
            int pAllDices = 0;
            for (int i = 0; i < 6; i++) {
                int x = rolledDices[i];
                counts[x - 1]++;
                pAllDices += x;
            }

            for (int i = 0; i < 6; i++) {
                if (counts[i] >= 4) {
                    p = pAllDices;
                    found = true;

                    break;
                }
            }

            if (found) {
                myPoints.get(9).setText(String.valueOf(p));
                myPoints.get(9).setEnabled(true);
            } else {
                myPoints.get(9).setText("0");
                myPoints.get(9).setEnabled(true);
            }

        }
    }

    public void checkFullHouse() {
        // Bir zardan 4 bir zardan da 2 adet olma durumunu kontrol eder.
        // Varsa puanını gösterir.
        if (myList[10] == null) {
            boolean found = false;
            int[] counts = new int[6];
            int p = 0;
            int pAllDices = 0;
            for (int i = 0; i < 6; i++) {
                int x = rolledDices[i];
                counts[x - 1]++;
                pAllDices += x;
            }

            for (int i = 0; i < 6; i++) {
                if (counts[i] == 4) {
                    for (int j = 0; j < 6; j++) {
                        if (j != i) {
                            if (counts[j] == 2) {
                                p = 35;
                                found = true;
                                break;
                            }
                        }
                    }

                }
            }

            if (found) {
                myPoints.get(10).setText(String.valueOf(p));
                myPoints.get(10).setEnabled(true);
            } else {
                myPoints.get(10).setText("0");
                myPoints.get(10).setEnabled(true);
            }

        }
    }

    public void checkSmallStr() {
        // Ardışık dört sayının olup olmadığının kontrolü

        if (myList[11] == null) {
            boolean found = false;
            ArrayList<Integer> items = new ArrayList<>();
            int p = 0;
            for (int i = 0; i < 6; i++) {
                int x = rolledDices[i];
                items.add(x);
            }
            if (items.contains(1) && items.contains(2) && items.contains(3) && items.contains(4)) {
                found = true;
                p = 30;
            } else if (items.contains(2) && items.contains(3) && items.contains(4) && items.contains(5)) {
                found = true;
                p = 30;
            } else if (items.contains(3) && items.contains(4) && items.contains(5) && items.contains(6)) {
                found = true;
                p = 30;
            } else {
                p = 0;
            }

            if (found) {
                myPoints.get(11).setText(String.valueOf(p));
                myPoints.get(11).setEnabled(true);
            } else {
                myPoints.get(11).setText("0");
                myPoints.get(11).setEnabled(true);
            }

        }
    }

    public void checkLargeStr() {
        // 1'den 6'ya kadar tüm gelme durumu.

        if (myList[12] == null) {
            boolean found = false;
            ArrayList<Integer> items = new ArrayList<>();
            int p = 0;
            for (int i = 0; i < 6; i++) {
                int x = rolledDices[i];
                items.add(x);
            }
            if (items.contains(1) && items.contains(2) && items.contains(3) && items.contains(4) && items.contains(5) && items.contains(6)) {
                found = true;
                p = 40;
            } else {
                p = 0;
            }

            if (found) {
                myPoints.get(12).setText(String.valueOf(p));
                myPoints.get(12).setEnabled(true);
            } else {
                myPoints.get(12).setText("0");
                myPoints.get(12).setEnabled(true);
            }

        }
    }

    public void checkChance() {
        // Herhangi bir puan için kullanılabilecek Chance seçeneği
        if (myList[13] == null) {
            int pAllDices = 0;
            for (int i = 0; i < 6; i++) {
                pAllDices += rolledDices[i];
            }
            myPoints.get(13).setText(String.valueOf(pAllDices));
            myPoints.get(13).setEnabled(true);

        }
    }

    public void checkYahtzee() {
        // Yahtzee durumunu kontrol eder.
        // Eğer ikinci ve daha fazla bir yahtzee durumu olursa da
        // Ekstra puan verir.
        if (myList[14] == null | myYahtzeeCount > 0) {

            boolean found = true;
            int p = 50;
            for (int i = 0; i < 6; i++) {
                if (rolledDices[0] != rolledDices[i]) {
                    found = false;
                    p = 0;
                    break;
                }

            }
            if (found) {
                if (myYahtzeeCount > 0) {
                    int pnt = Integer.valueOf(myPoints.get(14).getText()) + 100;
                    myPoints.get(14).setText(String.valueOf(pnt));
                    myYahtzeeCount++;
                    sendPntSelection(btn_p1_15);
                } else {
                    myYahtzeeCount++;
                    myPoints.get(14).setText(String.valueOf(p));
                    myPoints.get(14).setEnabled(true);
                }

            } else {
                myPoints.get(14).setText("0");
                myPoints.get(14).setEnabled(true);
            }

        }
    }

    public void sendPntSelection(JButton btn) {
        // Client'ın attığı zarlar sonucu elde ettiği
        // Puanlardan birini seçmesi ve bu bilgiyi karşıya da göndermesi.
        // Bu mesaj "butonNo+Puan" şeklinde gidiyor.

        myRoundNum++;
        String m = String.valueOf(getIndexofPointBtn(btn));
        m += "-";
        m += btn.getText();
        Message msg = new Message(Message.Message_Type.PNTSELECT);
        msg.content = m;
        counter = 0;
        setMyTurn(false);
        Client.Send(msg);

    }

    public int getIndexofPointBtn(JButton btn) {
        // Bazı durumlarda butonun listede kaçıncı sırada olduğunu bulmak için
        // yazdığım fonksiyon
        int index = 0;
        for (int i = 0; i < myPoints.size(); i++) {
            if (myPoints.get(i).equals(btn)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void puttBackUnSelectedPnts() {
        // O anki oyuncunun zar atma sonucu elde ettiği puanlarda oluşan listeden
        // Bir eleman seçildikten sonra, kalan elemanlar tekrar sıfırlanır ve eski duruma döner.
        for (JButton m : myPoints) {
            if (m.isEnabled()) {
                m.setText("0");
                m.setEnabled(false);
            }
        }
    }

    public void writeRivalPnt(String msg) {
        // Client'a gelen mesaj durumunda çalışır
        // Karşı tarafın listeden seçenek yapmasıyla
        // Client, rival taraftaki listeye onun puanını yazar.
        rivalRoundNum++;
        String[] items = msg.split("-");
        int index = Integer.valueOf(items[0]);
        rivalPoints.get(index).setText(items[1]);

    }

    public void finishGame() {
        // Her iki listeyi de gezerek
        // Tarafların puan hesaplamaları yapılır.
        // En son kazanan açıklanır.

        int mP = 0;
        int rP = 0;
        for (int i = 0; i < 6; i++) {
            mP += Integer.valueOf(myPoints.get(i).getText());
            rP += Integer.valueOf(rivalPoints.get(i).getText());
        }
        myPoints.get(6).setText(String.valueOf(mP));
        myPoints.get(6).setForeground(Color.blue);
        rivalPoints.get(6).setText(String.valueOf(rP));
        rivalPoints.get(6).setForeground(Color.blue);
        if (mP > 62) {
            mP += 35;
            myPoints.get(7).setText("35");
            myPoints.get(7).setForeground(Color.green);

        }
        if (rP > 62) {
            rP += 35;
            rivalPoints.get(7).setText("35");
            rivalPoints.get(7).setForeground(Color.red);
        }

        for (int i = 8; i < 16; i++) {
            mP += Integer.valueOf(myPoints.get(i).getText());
            rP += Integer.valueOf(rivalPoints.get(i).getText());
        }

        myPoints.get(15).setText(String.valueOf(mP));
        myPoints.get(15).setForeground(Color.green);
        rivalPoints.get(15).setText(String.valueOf(rP));
        rivalPoints.get(15).setForeground(Color.red);

        if (mP > rP) {
            lbl_winner.setText(lblplayer.getText());
            lbl_winner.setForeground(Color.green);
        } else if (rP > mP) {
            lbl_winner.setText(lbl_rival.getText());
            lbl_winner.setForeground(Color.red);
        } else {
            lbl_winner.setText("Tie Game");
        }
        lbl_winner_title.setVisible(true);
        lbl_winner.setVisible(true);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        txt_name = new javax.swing.JTextField();
        btn_connect = new javax.swing.JButton();
        pnl_gamer1 = new javax.swing.JPanel();
        txt_rival_name = new javax.swing.JTextField();
        btn_p2_8 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        btn_p1_9 = new javax.swing.JButton();
        btn_p2_9 = new javax.swing.JButton();
        jLabel25 = new javax.swing.JLabel();
        btn_p1_10 = new javax.swing.JButton();
        btn_p2_10 = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        btn_p1_11 = new javax.swing.JButton();
        btn_p2_11 = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        btn_p1_12 = new javax.swing.JButton();
        btn_p2_12 = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        btn_p1_13 = new javax.swing.JButton();
        btn_p2_13 = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        btn_p1_14 = new javax.swing.JButton();
        btn_p2_14 = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        btn_p1_15 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btn_p2_15 = new javax.swing.JButton();
        btn_p1_1 = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        btn_p2_1 = new javax.swing.JButton();
        btn_p1_16 = new javax.swing.JButton();
        btn_p1_2 = new javax.swing.JButton();
        btn_p2_16 = new javax.swing.JButton();
        btn_p2_2 = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        btn_p1_3 = new javax.swing.JButton();
        btn_p2_3 = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        btn_p1_4 = new javax.swing.JButton();
        btn_p2_4 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        btn_p1_5 = new javax.swing.JButton();
        btn_p2_5 = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        btn_p1_6 = new javax.swing.JButton();
        btn_p2_6 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        btn_p1_7 = new javax.swing.JButton();
        btn_p2_7 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        btn_p1_8 = new javax.swing.JButton();
        lbl_rival = new javax.swing.JLabel();
        lblplayer = new javax.swing.JLabel();
        btn_rolldice = new javax.swing.JButton();
        btn_dice6 = new javax.swing.JButton();
        btn_dice5 = new javax.swing.JButton();
        btn_dice4 = new javax.swing.JButton();
        btn_dice1 = new javax.swing.JButton();
        btn_dice2 = new javax.swing.JButton();
        btn_dice3 = new javax.swing.JButton();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        label3 = new java.awt.Label();
        lbl_winner = new javax.swing.JLabel();
        lbl_winner_title = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        txt_name.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_name.setText("Name");

        btn_connect.setText("Connect");
        btn_connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_connectActionPerformed(evt);
            }
        });

        pnl_gamer1.setBackground(new java.awt.Color(255, 153, 153));
        pnl_gamer1.setForeground(new java.awt.Color(51, 255, 0));
        pnl_gamer1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txt_rival_name.setEditable(false);
        txt_rival_name.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_rival_name.setText("Rival");
        txt_rival_name.setEnabled(false);

        btn_p2_8.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_8.setText("0");
        btn_p2_8.setEnabled(false);
        btn_p2_8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_8.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_8.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel24.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel24.setText("3 of a kind");

        btn_p1_9.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_9.setText("0");
        btn_p1_9.setEnabled(false);
        btn_p1_9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_9.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_9.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_9ActionPerformed(evt);
            }
        });

        btn_p2_9.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_9.setText("0");
        btn_p2_9.setEnabled(false);
        btn_p2_9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_9.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_9.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel25.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel25.setText("4 of a kind");

        btn_p1_10.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_10.setText("0");
        btn_p1_10.setEnabled(false);
        btn_p1_10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_10.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_10.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_10ActionPerformed(evt);
            }
        });

        btn_p2_10.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_10.setText("0");
        btn_p2_10.setEnabled(false);
        btn_p2_10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_10.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_10.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel26.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel26.setText("Full-House");

        btn_p1_11.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_11.setText("0");
        btn_p1_11.setEnabled(false);
        btn_p1_11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_11.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_11.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_11ActionPerformed(evt);
            }
        });

        btn_p2_11.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_11.setText("0");
        btn_p2_11.setEnabled(false);
        btn_p2_11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_11.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_11.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel27.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel27.setText("Small straight");

        btn_p1_12.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_12.setText("0");
        btn_p1_12.setEnabled(false);
        btn_p1_12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_12.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_12.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_12ActionPerformed(evt);
            }
        });

        btn_p2_12.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_12.setText("0");
        btn_p2_12.setEnabled(false);
        btn_p2_12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_12.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_12.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel28.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel28.setText("Large straight");

        btn_p1_13.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_13.setText("0");
        btn_p1_13.setEnabled(false);
        btn_p1_13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_13.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_13.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_13.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_13ActionPerformed(evt);
            }
        });

        btn_p2_13.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_13.setText("0");
        btn_p2_13.setEnabled(false);
        btn_p2_13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_13.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_13.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_13.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel29.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel29.setText("Chance");

        btn_p1_14.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_14.setText("0");
        btn_p1_14.setEnabled(false);
        btn_p1_14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_14.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_14.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_14.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_14ActionPerformed(evt);
            }
        });

        btn_p2_14.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_14.setText("0");
        btn_p2_14.setEnabled(false);
        btn_p2_14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_14.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_14.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_14.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel30.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel30.setText("Yahtzee");

        btn_p1_15.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_15.setText("0");
        btn_p1_15.setEnabled(false);
        btn_p1_15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_15.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_15.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_15.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_15ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel2.setText("Ones");

        btn_p2_15.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_15.setText("0");
        btn_p2_15.setEnabled(false);
        btn_p2_15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_15.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_15.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_15.setMinimumSize(new java.awt.Dimension(75, 50));

        btn_p1_1.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_1.setText("0");
        btn_p1_1.setEnabled(false);
        btn_p1_1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_1.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_1.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_1ActionPerformed(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel31.setText("Total Score");

        btn_p2_1.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_1.setText("0");
        btn_p2_1.setEnabled(false);
        btn_p2_1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_1.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_1.setMinimumSize(new java.awt.Dimension(75, 50));

        btn_p1_16.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_16.setText("0");
        btn_p1_16.setEnabled(false);
        btn_p1_16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_16.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_16.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_16.setMinimumSize(new java.awt.Dimension(75, 50));

        btn_p1_2.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_2.setText("0");
        btn_p1_2.setEnabled(false);
        btn_p1_2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_2.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_2.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_2ActionPerformed(evt);
            }
        });

        btn_p2_16.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_16.setText("0");
        btn_p2_16.setEnabled(false);
        btn_p2_16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_16.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_16.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_16.setMinimumSize(new java.awt.Dimension(75, 50));

        btn_p2_2.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_2.setText("0");
        btn_p2_2.setEnabled(false);
        btn_p2_2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_2.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_2.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel17.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel17.setText("Twos");

        jLabel18.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel18.setText("Threes");

        btn_p1_3.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_3.setText("0");
        btn_p1_3.setEnabled(false);
        btn_p1_3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_3.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_3.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_3ActionPerformed(evt);
            }
        });

        btn_p2_3.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_3.setText("0");
        btn_p2_3.setEnabled(false);
        btn_p2_3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_3.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_3.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel19.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel19.setText("Fours");

        btn_p1_4.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_4.setText("0");
        btn_p1_4.setEnabled(false);
        btn_p1_4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_4.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_4.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_4ActionPerformed(evt);
            }
        });

        btn_p2_4.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_4.setText("0");
        btn_p2_4.setEnabled(false);
        btn_p2_4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_4.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_4.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel20.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel20.setText("Fives");

        btn_p1_5.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_5.setText("0");
        btn_p1_5.setEnabled(false);
        btn_p1_5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_5.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_5.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_5ActionPerformed(evt);
            }
        });

        btn_p2_5.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_5.setText("0");
        btn_p2_5.setEnabled(false);
        btn_p2_5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_5.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_5.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel21.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel21.setText("Sixes");

        btn_p1_6.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_6.setText("0");
        btn_p1_6.setEnabled(false);
        btn_p1_6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_6.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_6.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_6ActionPerformed(evt);
            }
        });

        btn_p2_6.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_6.setText("0");
        btn_p2_6.setEnabled(false);
        btn_p2_6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_6.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_6.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel22.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel22.setText("Top-Sum");

        btn_p1_7.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_7.setText("0");
        btn_p1_7.setEnabled(false);
        btn_p1_7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_7.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_7.setMinimumSize(new java.awt.Dimension(75, 50));
        btn_p1_7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_p1_7ActionPerformed(evt);
            }
        });

        btn_p2_7.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p2_7.setText("0");
        btn_p2_7.setEnabled(false);
        btn_p2_7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p2_7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p2_7.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p2_7.setMinimumSize(new java.awt.Dimension(75, 50));

        jLabel23.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel23.setText("Top-Bonus");

        btn_p1_8.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        btn_p1_8.setText("0");
        btn_p1_8.setEnabled(false);
        btn_p1_8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_p1_8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn_p1_8.setMaximumSize(new java.awt.Dimension(75, 50));
        btn_p1_8.setMinimumSize(new java.awt.Dimension(75, 50));

        lbl_rival.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_rival.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_rival.setText("Player2");

        lblplayer.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblplayer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblplayer.setText("Player1");

        btn_rolldice.setBackground(new java.awt.Color(51, 51, 51));
        btn_rolldice.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btn_rolldice.setForeground(new java.awt.Color(255, 255, 255));
        btn_rolldice.setText("Roll Dices");
        btn_rolldice.setActionCommand("");
        btn_rolldice.setEnabled(false);
        btn_rolldice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_rolldiceActionPerformed(evt);
            }
        });

        btn_dice6.setEnabled(false);
        btn_dice6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dice6ActionPerformed(evt);
            }
        });

        btn_dice5.setEnabled(false);
        btn_dice5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dice5ActionPerformed(evt);
            }
        });

        btn_dice4.setEnabled(false);
        btn_dice4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dice4ActionPerformed(evt);
            }
        });

        btn_dice1.setEnabled(false);
        btn_dice1.setPreferredSize(new java.awt.Dimension(60, 60));
        btn_dice1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dice1ActionPerformed(evt);
            }
        });

        btn_dice2.setEnabled(false);
        btn_dice2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dice2ActionPerformed(evt);
            }
        });

        btn_dice3.setEnabled(false);
        btn_dice3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dice3ActionPerformed(evt);
            }
        });

        label1.setAlignment(java.awt.Label.CENTER);
        label1.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        label1.setText("You");

        label2.setAlignment(java.awt.Label.CENTER);
        label2.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        label2.setName("lbl22"); // NOI18N
        label2.setText("Rival");

        label3.setAlignment(java.awt.Label.CENTER);
        label3.setFont(new java.awt.Font("Swis721 Hv BT", 0, 24)); // NOI18N
        label3.setForeground(new java.awt.Color(0, 102, 102));
        label3.setText("Yahtzee Game");

        lbl_winner.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        lbl_winner.setForeground(new java.awt.Color(255, 153, 51));
        lbl_winner.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_winner.setText("Player");

        lbl_winner_title.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        lbl_winner_title.setForeground(new java.awt.Color(255, 153, 51));
        lbl_winner_title.setText("! Winner !");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(pnl_gamer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(38, 38, 38)
                                        .addComponent(btn_rolldice)
                                        .addGap(20, 20, 20)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lbl_rival, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(btn_dice4, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(btn_dice5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(btn_dice6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(btn_dice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(btn_dice2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(btn_dice3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(lblplayer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txt_rival_name, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btn_connect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txt_name, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lbl_winner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbl_winner_title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(131, 131, 131)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel27)
                        .addGap(28, 28, 28)
                        .addComponent(btn_p1_12, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(62, 62, 62)
                        .addComponent(btn_p2_12, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addGap(28, 28, 28)
                        .addComponent(btn_p1_13, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(62, 62, 62)
                        .addComponent(btn_p2_13, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel29))
                            .addComponent(jLabel30))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_p1_14, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_p1_15, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(62, 62, 62)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_p2_14, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_p2_15, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel31)
                        .addGap(28, 28, 28)
                        .addComponent(btn_p1_16, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(62, 62, 62)
                        .addComponent(btn_p2_16, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addComponent(jLabel2))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(jLabel17))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jLabel18))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(jLabel19))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addComponent(jLabel20))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addComponent(jLabel21))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(jLabel22))
                            .addComponent(jLabel23)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel24))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel25))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(jLabel26)))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p1_11, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(62, 62, 62)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btn_p2_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_p2_11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(label2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_p2_1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_7, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_8, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_9, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_10, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p2_11, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_p1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_7, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_8, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_9, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_10, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(btn_p1_11, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(61, 61, 61)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btn_dice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btn_dice2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btn_dice3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(13, 13, 13)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btn_dice4, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btn_dice5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btn_dice6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(49, 49, 49)
                                        .addComponent(btn_rolldice)))
                                .addGap(26, 26, 26))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pnl_gamer1, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(txt_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btn_connect)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txt_rival_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addGap(16, 16, 16)
                                                .addComponent(jLabel17)
                                                .addGap(16, 16, 16)
                                                .addComponent(jLabel18)))
                                        .addGap(15, 15, 15)
                                        .addComponent(jLabel19)
                                        .addGap(16, 16, 16)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel20)
                                            .addComponent(lblplayer))
                                        .addGap(9, 9, 9)
                                        .addComponent(jLabel21)
                                        .addGap(16, 16, 16)
                                        .addComponent(jLabel22)
                                        .addGap(16, 16, 16)
                                        .addComponent(jLabel23)
                                        .addGap(16, 16, 16)
                                        .addComponent(jLabel24)))
                                .addGap(16, 16, 16)
                                .addComponent(jLabel25)
                                .addGap(16, 16, 16)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addGap(13, 13, 13)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel27))
                                    .addComponent(btn_p1_12, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_p2_12, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(7, 7, 7))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lbl_rival)
                                .addGap(26, 26, 26)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel28))
                                    .addComponent(btn_p1_13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_p2_13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(7, 7, 7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel29)
                                        .addGap(16, 16, 16)
                                        .addComponent(jLabel30))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btn_p2_14, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(7, 7, 7)
                                        .addComponent(btn_p2_15, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btn_p1_14, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(7, 7, 7)
                                        .addComponent(btn_p1_15, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(7, 7, 7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_p1_16, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel31))
                                    .addComponent(btn_p2_16, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(lbl_winner_title)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lbl_winner)))))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_connectActionPerformed

        //bağlanılacak server ve portu veriyoruz
        Client.Start("127.0.0.1", 2000);
        //başlangıç 
        btn_connect.setEnabled(false);
        txt_name.setEnabled(false);

        //
        if (txt_name.getText() == null) {
            lblplayer.setText("You");
        } else {
            lblplayer.setText(txt_name.getText());
        }

    }//GEN-LAST:event_btn_connectActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        //form kapanırken clienti durdur
        Client.Stop();
    }//GEN-LAST:event_formWindowClosing


    private void btn_dice1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dice1ActionPerformed
        // Framedaki tüm zarlar için;
        // Üzerine tıklandığında 
        // Stun durumu toggle olur.
        toggleDice(btn_dice1);


    }//GEN-LAST:event_btn_dice1ActionPerformed


    private void btn_rolldiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_rolldiceActionPerformed
        // Client'ın sırasıysa buton aktiftir.
        // Oyuncunun 3 defa zar atma hakkı vardır, sonrasına buton disable olur

        counter++;
        if (counter == 3) {
            btn_rolldice.setEnabled(false);
        }
        rollthedices();
        // Yaptığım ufak matamatikle rolledDicesAsInt dizisindeki zarları
        // Bir tam sayı şeklinde karşı tarafa int olarak mesaj atıyorum
        // Örnegin 245662

        rolledDicesAsInt = 0;
        int y = 100000;
        for (int i = 0; i < 6; i++) {
            rolledDicesAsInt += (rolledDices[i] * (y));
            y /= 10;
        }
        showPointsonList();
        putDices();

        Message msg = new Message(Message.Message_Type.ROLL);
        msg.content = rolledDicesAsInt;
        Client.Send(msg);

        // zar atma hakkı bittiyse durdur

    }//GEN-LAST:event_btn_rolldiceActionPerformed

    private void btn_dice2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dice2ActionPerformed

        toggleDice(btn_dice2);
    }//GEN-LAST:event_btn_dice2ActionPerformed

    private void btn_dice3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dice3ActionPerformed

        toggleDice(btn_dice3);
    }//GEN-LAST:event_btn_dice3ActionPerformed

    private void btn_dice4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dice4ActionPerformed

        toggleDice(btn_dice4);
    }//GEN-LAST:event_btn_dice4ActionPerformed

    private void btn_dice5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dice5ActionPerformed

        toggleDice(btn_dice5);
    }//GEN-LAST:event_btn_dice5ActionPerformed

    private void btn_dice6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dice6ActionPerformed

        toggleDice(btn_dice6);
    }//GEN-LAST:event_btn_dice6ActionPerformed


    private void btn_p1_4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_4ActionPerformed

        // Client'ın kendi puanlarının olduğu listedeki her eleman için geçerli;
        // Seçilen butondaki pouan myList'de gerekli indexe atanır.
        // Ardından diğer seçilmeyen butonlar eski hale döner.
        // Bu seçim karşı Client'a da gönderilir.
        myList[getIndexofPointBtn(btn_p1_4)] = String.valueOf(btn_p1_4.getText());
        btn_p1_4.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_4);
    }//GEN-LAST:event_btn_p1_4ActionPerformed

    private void btn_p1_15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_15ActionPerformed

        myList[getIndexofPointBtn(btn_p1_15)] = String.valueOf(btn_p1_15.getText());
        btn_p1_15.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_15);
    }//GEN-LAST:event_btn_p1_15ActionPerformed

    private void btn_p1_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_1ActionPerformed

        myList[getIndexofPointBtn(btn_p1_1)] = String.valueOf(btn_p1_1.getText());
        btn_p1_1.setEnabled(false);
        puttBackUnSelectedPnts();

        sendPntSelection(btn_p1_1);

    }//GEN-LAST:event_btn_p1_1ActionPerformed

    private void btn_p1_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_2ActionPerformed

        myList[getIndexofPointBtn(btn_p1_2)] = String.valueOf(btn_p1_2.getText());
        btn_p1_2.setEnabled(false);
        puttBackUnSelectedPnts();

        sendPntSelection(btn_p1_2);
    }//GEN-LAST:event_btn_p1_2ActionPerformed

    private void btn_p1_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_3ActionPerformed

        myList[getIndexofPointBtn(btn_p1_3)] = String.valueOf(btn_p1_3.getText());
        btn_p1_3.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_3);
    }//GEN-LAST:event_btn_p1_3ActionPerformed

    private void btn_p1_5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_5ActionPerformed

        myList[getIndexofPointBtn(btn_p1_5)] = String.valueOf(btn_p1_5.getText());
        btn_p1_5.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_5);
    }//GEN-LAST:event_btn_p1_5ActionPerformed

    private void btn_p1_6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_6ActionPerformed

        myList[getIndexofPointBtn(btn_p1_6)] = String.valueOf(btn_p1_6.getText());
        btn_p1_6.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_6);
    }//GEN-LAST:event_btn_p1_6ActionPerformed

    private void btn_p1_9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_9ActionPerformed

        myList[getIndexofPointBtn(btn_p1_9)] = String.valueOf(btn_p1_9.getText());
        btn_p1_9.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_9);
    }//GEN-LAST:event_btn_p1_9ActionPerformed

    private void btn_p1_10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_10ActionPerformed

        myList[getIndexofPointBtn(btn_p1_10)] = String.valueOf(btn_p1_10.getText());
        btn_p1_10.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_10);
    }//GEN-LAST:event_btn_p1_10ActionPerformed

    private void btn_p1_11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_11ActionPerformed

        myList[getIndexofPointBtn(btn_p1_11)] = String.valueOf(btn_p1_11.getText());
        btn_p1_11.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_11);
    }//GEN-LAST:event_btn_p1_11ActionPerformed

    private void btn_p1_12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_12ActionPerformed

        myList[getIndexofPointBtn(btn_p1_12)] = String.valueOf(btn_p1_12.getText());
        btn_p1_12.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_12);
    }//GEN-LAST:event_btn_p1_12ActionPerformed

    private void btn_p1_13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_13ActionPerformed

        myList[getIndexofPointBtn(btn_p1_13)] = String.valueOf(btn_p1_13.getText());
        btn_p1_13.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_13);
    }//GEN-LAST:event_btn_p1_13ActionPerformed

    private void btn_p1_14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_14ActionPerformed

        myList[getIndexofPointBtn(btn_p1_14)] = String.valueOf(btn_p1_14.getText());
        btn_p1_14.setEnabled(false);
        puttBackUnSelectedPnts();
        sendPntSelection(btn_p1_14);
    }//GEN-LAST:event_btn_p1_14ActionPerformed

    private void btn_p1_7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_p1_7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_p1_7ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Game().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btn_connect;
    public javax.swing.JButton btn_dice1;
    public javax.swing.JButton btn_dice2;
    public javax.swing.JButton btn_dice3;
    public javax.swing.JButton btn_dice4;
    public javax.swing.JButton btn_dice5;
    public javax.swing.JButton btn_dice6;
    public javax.swing.JButton btn_p1_1;
    public javax.swing.JButton btn_p1_10;
    public javax.swing.JButton btn_p1_11;
    public javax.swing.JButton btn_p1_12;
    public javax.swing.JButton btn_p1_13;
    public javax.swing.JButton btn_p1_14;
    public javax.swing.JButton btn_p1_15;
    public javax.swing.JButton btn_p1_16;
    public javax.swing.JButton btn_p1_2;
    public javax.swing.JButton btn_p1_3;
    public javax.swing.JButton btn_p1_4;
    public javax.swing.JButton btn_p1_5;
    public javax.swing.JButton btn_p1_6;
    public javax.swing.JButton btn_p1_7;
    public javax.swing.JButton btn_p1_8;
    public javax.swing.JButton btn_p1_9;
    public javax.swing.JButton btn_p2_1;
    public javax.swing.JButton btn_p2_10;
    public javax.swing.JButton btn_p2_11;
    public javax.swing.JButton btn_p2_12;
    public javax.swing.JButton btn_p2_13;
    public javax.swing.JButton btn_p2_14;
    public javax.swing.JButton btn_p2_15;
    public javax.swing.JButton btn_p2_16;
    public javax.swing.JButton btn_p2_2;
    public javax.swing.JButton btn_p2_3;
    public javax.swing.JButton btn_p2_4;
    public javax.swing.JButton btn_p2_5;
    public javax.swing.JButton btn_p2_6;
    public javax.swing.JButton btn_p2_7;
    public javax.swing.JButton btn_p2_8;
    public javax.swing.JButton btn_p2_9;
    public javax.swing.JButton btn_rolldice;
    private javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.JLabel jLabel17;
    public javax.swing.JLabel jLabel18;
    public javax.swing.JLabel jLabel19;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel20;
    public javax.swing.JLabel jLabel21;
    public javax.swing.JLabel jLabel22;
    public javax.swing.JLabel jLabel23;
    public javax.swing.JLabel jLabel24;
    public javax.swing.JLabel jLabel25;
    public javax.swing.JLabel jLabel26;
    public javax.swing.JLabel jLabel27;
    public javax.swing.JLabel jLabel28;
    public javax.swing.JLabel jLabel29;
    public javax.swing.JLabel jLabel30;
    public javax.swing.JLabel jLabel31;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private java.awt.Label label3;
    public javax.swing.JLabel lbl_rival;
    public javax.swing.JLabel lbl_winner;
    public javax.swing.JLabel lbl_winner_title;
    public javax.swing.JLabel lblplayer;
    private javax.swing.JPanel pnl_gamer1;
    public javax.swing.JTextField txt_name;
    public javax.swing.JTextField txt_rival_name;
    // End of variables declaration//GEN-END:variables

}
