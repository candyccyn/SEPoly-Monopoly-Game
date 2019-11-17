package allSpritePlayer;


import DiceAnimate.Animator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.*;

public class PlayEx extends JFrame {

    public PlayEx( ){
        initComponents();
    }
    private void initComponents(){
        int posXY[] = new int[2] ;
        int die1, die2 , diceNumber;

        Random random = new Random();
        die1  = random.nextInt(6) + 1;
        die2  = random.nextInt(6) + 1;

        die1 = die2 = 0;
        //ShowPlayer1 p1 = new ShowPlayer1(0 , die1, die2);

        add(new ShowPlayer1(0 , die1, die2) );
        Timer t = new Timer(300, new ReDraw1(new ShowPlayer1(0 , die1, die2),360,460,die1, die2, false));

        t.start();


    }

    public static void main(String[] args) {
        PlayEx main = new PlayEx();


        main.setSize(800,600);
        main.setVisible(true);
        main.setResizable(false);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setLocationRelativeTo(null);
    }
}

class ReDraw1 implements ActionListener {

    ShowPlayer1 main;
    int count , rollNumber;
    static int posX , posY ;
    static boolean cardJail;


    ReDraw1(ShowPlayer1 gameScreen ,int posX , int posY , int die1, int die2,boolean cardJail) {
        this.main = gameScreen;
        this.rollNumber = die1 + die2;
        this.posX = posX;
        this.posY = posY;
        this.cardJail =cardJail;
    }

    public static int[] getPositionPlayer(){
        int posXY[] = new int[2];
        posXY[0] = posX;
        posXY[1] = posY;
        return posXY;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public static void ckJail(){
        if(cardJail == true){
            posX = 80;
            posY = 276;
        }
    }

    public int ckSide() {
        if((posX <= 360 && posX > 80)&&(posY > 276 && posY <= 460)){ return  1;}
        else if((posX < 376 && posX >= 80)&&(posY > 60 && posY <= 276)){ return 2;}
        else if((posX < 672 && posX >= 376)&&(posY < 268 && posY >= 60)){ return 3;}
        else{ return 4;}
    }

    public void actionPerformed(ActionEvent e) {
        int side = ckSide();
        count++;

        switch (side){
            case 1:
                posX -= 35; //side1
                posY -= 23; //side1
                break;
            case 2:
                posX += 37; //side2
                posY -= 27; //side2
                break;
            case 3:
                posX += 37; //side3
                posY += 26; //side3
                break;
            case 4:
                if(posX == 427 && posY == 443){
                    posX -= 67;
                    posY += 17;
                }
                else{
                    posX -= 35; //side4
                    posY += 25; //side4
                }
                break;
        }


        System.out.println("Flag 1: " + posX + " " + posY);
        main.repaint();

        if (count == rollNumber) { //จนครั้งที่เดิน
            ((Timer) e.getSource()).stop();
        }
    }
}




class ShowPlayer1 extends JPanel {
    BufferedImage spriteIdleL, spriteIdleR, board;
    Animator rollDice ,rollDice2 , ratJail, pUpJail, pDaminJail, maidJail;
    public int die1 ,die2;
    boolean ckCardJail= false ;
    public ShowPlayer1(int playerNumber, int die1, int die2) {
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = null;
        BufferedImage spriteSheetBoard = null;
        BufferedImage diceSpriteSheet= null ;
        this.die1 = die1;
        this.die2 = die2;

        try {
            if(ckCardJail){
                switch (playerNumber){
                    case 0 : spriteSheet = loader.loadImage("/allImage/ratSprite22.png"); break;
                    case 1 : spriteSheet = loader.loadImage("/allImage/pupSpriteJail.png"); break;
                    case 2 : spriteSheet = loader.loadImage("/allImage/PDamianJail.png"); break;
                    case 3 : spriteSheet = loader.loadImage("/allImage/maidSpriteJail.png"); break;
                }
            }
           /* else{
                switch (playerNumber) {
                    case 0: spriteSheet = loader.loadImage("/ratSprite.png");break;
                    case 1: spriteSheet = loader.loadImage("/pupSprite.png");break;
                    case 2: spriteSheet = loader.loadImage("/PDAMIAN.png");break;
                    case 3: spriteSheet = loader.loadImage("/maidSprite.png");break; }
            }*/

            spriteSheetBoard = loader.loadImage("/allImage/finaljingjing_board.png");
            diceSpriteSheet = loader.loadImage("/allImage/newDice.png" );
        } catch (IOException ex) {
            Logger.getLogger(PlayEx.class.getName()).log(Level.SEVERE, null, ex);
        }
        SpriteSheet playerSS = new SpriteSheet(spriteSheet);
        SpriteSheet boardSS = new SpriteSheet(spriteSheetBoard);

        if(ckCardJail){
            ArrayList<BufferedImage> spritesJail =new ArrayList<BufferedImage>();
            switch (playerNumber){
                case 0:
                    spritesJail.add(playerSS.grabSprite(0, 0, 110,120));
                    spritesJail.add(playerSS.grabSprite(108, 0, 110,120));
                    spritesJail.add(playerSS.grabSprite(220, 0, 109,120));
                    spritesJail.add(playerSS.grabSprite(0, 0, 1,1));

                    ratJail = new DiceAnimate.Animator(spritesJail);
                    ratJail.setSpeed(200);
                    ratJail.start();
                    break;
                case 1:
                    spritesJail.add(playerSS.grabSprite(0, 0, 130,242));
                    spritesJail.add(playerSS.grabSprite(132, 0, 130,242));
                    spritesJail.add(playerSS.grabSprite(261, 0, 129,242));
                    spritesJail.add(playerSS.grabSprite(0, 0, 1,1));

                    pUpJail = new DiceAnimate.Animator(spritesJail);
                    pUpJail.setSpeed(200);
                    pUpJail.start();
                    break;
                case 2:
                    spritesJail.add(playerSS.grabSprite(0, 0, 127,229));
                    spritesJail.add(playerSS.grabSprite(127, 0, 127,229));
                    spritesJail.add(playerSS.grabSprite(255, 0, 126,229));
                    spritesJail.add(playerSS.grabSprite(0, 0, 1,1));

                    pDaminJail = new DiceAnimate.Animator(spritesJail);
                    pDaminJail.setSpeed(200);
                    pDaminJail.start();
                    break;
                case 3:
                    spritesJail.add(playerSS.grabSprite(0, 0, 120,195));
                    spritesJail.add(playerSS.grabSprite(124, 0, 120,195));
                    spritesJail.add(playerSS.grabSprite(248, 0, 120,195));
                    spritesJail.add(playerSS.grabSprite(0, 0, 1,1));
                    maidJail = new DiceAnimate.Animator(spritesJail);
                    maidJail.setSpeed(200);
                    maidJail.start();
                    break;
            }
        }
         /*else{
           switch (playerNumber) {
                case 0:
                    spriteIdleL = playerSS.grabSprite(0, 0, 110, 120);
                    spriteIdleR = playerSS.grabSprite(0, 123, 110, 120);
                    break;
                case 1:
                    spriteIdleL = playerSS.grabSprite(0, 243, 130, 243);
                    spriteIdleR = playerSS.grabSprite(0, 0, 130, 243);
                    break;
                case 2:
                    spriteIdleL = playerSS.grabSprite(0, 0, 127, 230);
                    spriteIdleR = playerSS.grabSprite(0, 230, 127, 230);
                    break;
                case 3:
                    spriteIdleL = playerSS.grabSprite(0, 0, 120, 195);
                    spriteIdleR = playerSS.grabSprite(0, 198, 114, 190);
                    break;
            }
        }*/
        movePlayerForward(1);

        board = boardSS.grabSprite(0, 0, 800, 600);

        //showDice
        SpriteSheet diceSS = new SpriteSheet(diceSpriteSheet);
        SpriteSheet diceSS2 = new SpriteSheet(diceSpriteSheet);

        ArrayList<BufferedImage> sprites =new ArrayList<BufferedImage>();
        ArrayList<BufferedImage> sprites2 =new ArrayList<BufferedImage>();

        sprites.add(diceSS.grabSprite(0, 0, 92,92));//1
        sprites.add(diceSS.grabSprite(92, 0, 92,92));//2
        sprites.add(diceSS.grabSprite(184, 0, 92,92));//3
        sprites.add(diceSS.grabSprite(276, 0, 92,92));
        sprites.add(diceSS.grabSprite(0, 92, 92,92));
        sprites.add(diceSS.grabSprite(92, 92, 92,92));

        switch (die1) {
            case 1: sprites.add(diceSS.grabSprite(184, 92, 92,92));break;
            case 2: sprites.add(diceSS.grabSprite(276, 92, 92,92));break;
            case 3: sprites.add(diceSS.grabSprite(0, 184, 92,92));break;
            case 4: sprites.add(diceSS.grabSprite(92, 184, 92,92));break;
            case 5: sprites.add(diceSS.grabSprite(184, 184, 92,92));break;
            case 6: sprites.add(diceSS.grabSprite(276, 184, 92,92));break;
        }

        sprites2.add(diceSS2.grabSprite(0, 0, 92,92));//1
        sprites2.add(diceSS2.grabSprite(92, 0, 92,92));//2
        sprites2.add(diceSS2.grabSprite(184, 0, 92,92));//3
        sprites2.add(diceSS2.grabSprite(276, 0, 92,92));
        sprites2.add(diceSS2.grabSprite(0, 92, 92,92));
        sprites2.add(diceSS2.grabSprite(92, 92, 92,92));

        switch (die2) {
            case 1: sprites2.add(diceSS2.grabSprite(184, 92, 92,92));break;
            case 2: sprites2.add(diceSS2.grabSprite(276, 92, 92,92));break;
            case 3: sprites2.add(diceSS2.grabSprite(0, 184, 92,92));break;
            case 4: sprites2.add(diceSS2.grabSprite(92, 184, 92,92));break;
            case 5: sprites2.add(diceSS2.grabSprite(184, 184, 92,92));break;
            case 6: sprites2.add(diceSS2.grabSprite(276, 184, 92,92));break;
        }
        System.out.println(die1 +" "+  die2);
        rollDice = new Animator(sprites);
        rollDice.setSpeed(100);
        rollDice2 = new Animator(sprites2);
        rollDice2.setSpeed(100);

        rollDice.start();
        rollDice2.start();
    }

    public void movePlayerForward(int playerNumber){
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = null;
        try {
            switch (playerNumber) {
                case 0: spriteSheet = loader.loadImage("/allImage/ratSprite.png"); break;
                case 1: spriteSheet = loader.loadImage("/allImage/pupSprite.png");break;
                case 2: spriteSheet = loader.loadImage("/allImage/PDAMIAN.png");break;
                case 3: spriteSheet = loader.loadImage("/allImage/maidSprite.png");break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SpriteSheet playerSS = new SpriteSheet(spriteSheet);
        switch (playerNumber) {
            case 0:
                spriteIdleL = playerSS.grabSprite(0, 0, 110, 120);
                spriteIdleR = playerSS.grabSprite(0, 123, 110, 120);
                break;
            case 1:
                spriteIdleL = playerSS.grabSprite(0, 243, 130, 243);
                spriteIdleR = playerSS.grabSprite(0, 0, 130, 243);
                break;
            case 2:
                spriteIdleL = playerSS.grabSprite(0, 0, 127, 230);
                spriteIdleR = playerSS.grabSprite(0, 230, 127, 230);
                break;
            case 3:
                spriteIdleL = playerSS.grabSprite(0, 0, 120, 195);
                spriteIdleR = playerSS.grabSprite(0, 198, 114, 190);
                break;
        }

    }

    Image dbImage ;
    Graphics dbg ;
    @Override
    public void paint(Graphics g) {
        dbImage = createImage(getWidth(),getHeight());
        dbg = dbImage.getGraphics();
        paintComponents(dbg);
        g.drawImage(dbImage,0,0,null);
    }

    public void paintComponents(Graphics g)
    {
        int[] posXY;
        posXY = ReDraw1.getPositionPlayer();

        g.drawImage(board, 0, 0, 800, 600, null);
        if ((posXY[0] >= 80 && posXY[0] < 672 && posXY[1] <= 276) || (posXY[1] >= 60 && posXY[1] <= 268)) {
            g.drawImage(spriteIdleR, posXY[0], posXY[1], 50, 50, null);
        } else {
            g.drawImage(spriteIdleL, posXY[0], posXY[1], 50, 50, null);
        }

        if(ratJail != null ){
            ratJail.update(System.currentTimeMillis());
            g.drawImage(ratJail.sprite,posXY[0],posXY[1],50,50,null);
        }

        if(rollDice != null ){
            if(rollDice.getCurrentFrame() != 6) {
                rollDice.update(System.currentTimeMillis());
                g.drawImage(rollDice.sprite, 480, 280, 50, 50, null); }
            else{ rollDice.stop(); }
        }
        if(rollDice2 != null ){
            if(rollDice2.getCurrentFrame() != 6) {
                rollDice2.update(System.currentTimeMillis());
                g.drawImage(rollDice2.sprite,530,280,50,50,null); }
            else{ rollDice2.stop(); }
        }
        repaint();
    }
}




