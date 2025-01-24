import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

//inherits from JPanel
public class FlappyBird extends JPanel implements ActionListener , KeyListener{
        int borderWidth = 360;
        int borderHeight = 640;

        // adding image
        Image backgroundImg;
        Image birdImg;
        Image toppipeImg;
        Image bottompipeImg;

        Clip StartingSongClip;
        Clip GameOverClip;

        //Bird
        int birdX = borderWidth/8;
        int birdY = borderHeight/2;
        int birdWidth = 34;
        int birdHeight = 24;
        
        //encapsulate 
        class Bird{
            int x = birdX;
            int y = birdY;

            int widht = birdWidth;
            int hight = birdHeight;

            Image img;
            Bird(Image img){
                this.img = img;
            }
        }
       
        //Pipes
        int pipeX = borderWidth;
        int pipeY = 0;
        int pipeWidth = 64;
        int pipeHeight = 512; 
        
        class Pipe{
            int x = pipeX;
            int y = pipeY;
            int widht = pipeWidth;
            int hight = pipeHeight;
            Image img;

            boolean passed = false;

            Pipe(Image img){
                this.img = img;
            }
        }

        //game logic
        Bird bird;
        int velocityX = -4;
        int velocityY = 0;
        int gravity = 1;
         
        ArrayList<Pipe> pipes;
        Random random = new Random();

        Timer gameLoop;
        Timer PlacePipesTimer;

        boolean gameover = false;
        boolean isPaused = false;
        double score = 0;
        
        FlappyBird(){
            setPreferredSize(new Dimension(borderWidth, borderHeight));
            setFocusable(true);
            addKeyListener(this);
            

            // load image 
            backgroundImg = new ImageIcon(getClass().getResource("./Image/flappybirdbg.jpg")).getImage();
            birdImg = new ImageIcon(getClass().getResource("./Image/flappybird.png")).getImage();
            toppipeImg = new ImageIcon(getClass().getResource("./Image/toppipe.png")).getImage();
            bottompipeImg = new ImageIcon(getClass().getResource("./Image/bottompipe.png")).getImage();

            bird = new Bird(birdImg);
            pipes = new ArrayList<Pipe>();


            // place pipes timer
            PlacePipesTimer = new Timer(1500, new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    placePipes();
                }
            });
            PlacePipesTimer.start();

            //game timer
            gameLoop = new Timer(1000/60,this);
            gameLoop.start();

            //play background music 
             try {
                File file = new File("./Song/Squid Game.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                StartingSongClip = AudioSystem.getClip();
                StartingSongClip.open(audioStream);
                StartingSongClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try{
                File over = new File("./Song/Game Over.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(over);
                GameOverClip = AudioSystem.getClip();
                GameOverClip.open(audioStream);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
        //abstraction
        public void placePipes(){
            /*
             * (0-1) * pipeHeight/2 -> (0 - 256)
             * 128
             * 0 - 128 -(0-256) --> pipeHeight/4 --> 3/4 pipeHeight
             */

             int randomPipeY = (int)(pipeY - pipeHeight/4 - Math.random() * (pipeHeight/2));
             int openingspace = borderHeight/4;

             Pipe topPipes = new Pipe(toppipeImg);
             topPipes.y = randomPipeY;
             pipes.add(topPipes);

             Pipe bottomPipe = new Pipe(bottompipeImg);
             bottomPipe.y = topPipes.y + pipeHeight + openingspace;
             pipes.add(bottomPipe);

        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            draw(g);
        }


        public void drawCryingCharacter(Graphics g) {
            // Draw face
            g.setColor(Color.YELLOW);
            g.fillOval(borderWidth/2-100, borderHeight/2-100, 200, 200); // Head centered
        
            // Draw eyes
            g.setColor(Color.BLACK); 
            g.fillOval(borderWidth/2-50, borderHeight/2-50, 20, 20); // Left eye
            g.fillOval(borderWidth/2+30, borderHeight/2-50, 20, 20); // Right eye
        
            // Draw tears
            g.setColor(Color.CYAN);
            g.fillOval(borderWidth/2-40, borderHeight/2-10, 10, 20); // Left tear
            g.fillOval(borderWidth/2+40, borderHeight/2-10, 10, 20); // Right tear
        
            // Draw mouth
            g.setColor(Color.RED);
            g.drawArc(borderWidth/2-50, borderHeight/2+10, 100, 50, 0, -180); // Sad mouth
        }

        public void draw(Graphics g){
            //background
            g.drawImage(backgroundImg,0,0,borderWidth,borderHeight,null);

            //bird
            g.drawImage(bird.img,bird.x,bird.y,bird.widht,bird.hight,null);
        
            //Pipes
            for(int i = 0; i<pipes.size();i++){
                Pipe pipe = pipes.get(i);
                g.drawImage(pipe.img,pipe.x,pipe.y,pipe.widht,pipe.hight,null);
            }

            //score
 
            if (gameover) {
                drawCryingCharacter(g);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("Game Over: " + String.valueOf((int) score), 50, 500);
            } else {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString(String.valueOf((int) score), 10, 35);
            }


        }

        //abstraction
        public void move(){
            //bird
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y,0);

            //pipes
            for(int i=0; i<pipes.size();i++){
                Pipe pipe = pipes.get(i);
                pipe.x += velocityX;
                if(!pipe.passed && pipe.x + pipe.widht < bird.x){
                    pipe.passed = true;
                    score += 0.5;
                }
                if(isCollision(bird,pipe)){
                    gameover = true;
                }

            }
            if(bird.y > borderHeight){
                gameover = true;
            }
        }

        public boolean isCollision(Bird a, Pipe b){
            return a.x < b.x + b.widht &&
                    a.x + a.widht > b.x &&
                     a.y < b.y + b.hight &&
                    a.y + a.hight > b.y;
        }

        public void stopAudio(){
            if(StartingSongClip!=null && StartingSongClip.isRunning()){
                StartingSongClip.stop();
            }
        }
        
        public void GameOverSoundEffect(){
                GameOverClip.setMicrosecondPosition(0);
                GameOverClip.start();
        
        }
        //polymorphism
        @Override
        public void actionPerformed(ActionEvent e){
            
            move();
            repaint();
            gameLoop.start();
            if(gameover){
                PlacePipesTimer.stop();
                gameLoop.stop();
                stopAudio();
                GameOverSoundEffect();
            }
        }

       //polymorphism
        @Override
        public void keyPressed(KeyEvent e){
            if(e.getKeyCode() == KeyEvent.VK_SPACE){
                velocityY = -9;
                if(gameover){
                    //reset
                    bird.y = birdY;
                    velocityY = 0;
                    pipes.clear();
                    score = 0;
                    gameover = false;
                    PlacePipesTimer.start();
                    gameLoop.start();
                
                    StartingSongClip.setMicrosecondPosition(0);
                    StartingSongClip.start();
                }
                
            }
        }
        
        @Override
        public void keyTyped(KeyEvent e){
            
        }
        @Override
        public void keyReleased(KeyEvent e){
            
        }

}
    
