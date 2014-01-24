package snake;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Snake Game
 * @author Marty
 */
public class Snake extends JPanel implements Runnable {

    private ArrayList<Rectangle> rects = new ArrayList<>();
    
    public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    public int 
            
            // Size of box...
            size = 20,
            
            // Apple Coordinates
            xApple,
            yApple,
            
            // Width and Height of the JFrame
            frameWidth = screenSize.width,
            frameHeight = screenSize.height,
            
            // Initial xCoord and yCoord of the Snake Head
            xHead = (screenSize.width / size) * 5, 
            yHead = (screenSize.height / size) * 5, 
            
            // Apple points
            points = 0;
    
    public Rectangle apple = new Rectangle(xApple, yApple, size, size),
            snakeHead = new Rectangle(xHead, yHead, size, size),
            screen = new Rectangle(0, 0, frameWidth, frameHeight),
            playButton = new Rectangle(),
            cross = new Rectangle();
    
    // Generates random nums for whatever needs it
    private Random randomGen = new Random();
    
    public boolean 
            // Movement booleans
            right = true,
            left = false,
            down = false,
            up = false,
            
            // The Snake is ready to grow 
            readyToGrow = false,
            
            // Menus
            gameOver = false,
            inGame = false,
            startScreen = true,
            hoverOverPlayButton = false,
            
            gameScoreAdded = false,
            gridMode = false;
    
    private Image title,
            playButton1,
            playButton2,
            appleImage,
            gameOverImage,
            replay,
            replay2,
            crossImage;
    
    private File highscores;
    private PrintStream highscoreWriter;
    private JTextField highscoreField;
    
    private static String name;
    
    /**
     * Constructor
     */
    public Snake() {
        super(true);
        
        // Import Images
        try {
            title           = ImageIO.read(new File("resources/title.png"));
            playButton1     = ImageIO.read(new File("resources/start-button.jpg"));
            playButton2     = ImageIO.read(new File("resources/start-button-2.jpg"));
            appleImage      = ImageIO.read(new File("resources/apple.png"));
            gameOverImage   = ImageIO.read(new File("resources/gameover.png"));
            replay          = ImageIO.read(new File("resources/replay.jpg"));
            replay2         = ImageIO.read(new File("resources/replay-2.jpg"));
            crossImage      = ImageIO.read(new File("resources/cross.png"));
        } catch(Exception ex) { ex.printStackTrace(); }
        
        // Set up highscore file
        highscores      = new File("resources/highscores.txt");
        try {        
            highscoreWriter = new PrintStream("resources/highscores.txt");
        } catch (IOException e) { e.printStackTrace(); }
        
        highscoreField = new JTextField("Enter your highscore!");
        
        // Create PlayButton Rectangle
        playButton = new Rectangle((frameWidth / 2) - (playButton1.getWidth(null) / 2), (frameHeight / 2) + (playButton1.getHeight(null) / 2), playButton1.getWidth(null), playButton1.getHeight(null));
        
        // Create the exit cross Rectangle
        cross = new Rectangle(frameWidth - crossImage.getWidth(null), 0, crossImage.getWidth(null), crossImage.getHeight(null));
        
        // Create and setup the JFrame
        JFrame frame = new JFrame("Snake");
        frame.setSize(screenSize);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        
        // Set the opacity of the window (optional)
        frame.setOpacity((float)0.7);
        
        // Mouse Listener
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                if (playButton.contains(p)) {
                    if (startScreen) {
                        startScreen = false;
                        inGame = true;
                    } else if (gameOver) {
                        gameOver = false;
                        setup();
                        inGame = true;
                    }
                }
                
                if (cross.contains(p))
                    System.exit(0);
            }
        });
        
        // Mouse Motion Listener
        frame.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                if (playButton.contains(p)) {
                    hoverOverPlayButton = true;
                } else 
                    hoverOverPlayButton = false;
            }        
        });

        // Key Listener 
        frame.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == e.VK_UP) 
                    changeDirection(1);
                else if (key == e.VK_RIGHT)
                    changeDirection(2);
                else if (key == e.VK_DOWN)
                    changeDirection(3);
                else if (key == e.VK_LEFT)
                    changeDirection(4);
                
                if (key == e.VK_ESCAPE)
                    System.exit(0);
            
                if (inGame)
                    if (e.getKeyCode() == e.VK_G) {
                        gridMode = !gridMode;
                        System.out.println("Grid mode activated");
                    }
            }
            
        });
        
        // Setup the game (create snake, set apple position)
        setup();
        
        // Add the JPanel to the JFrame
        frame.add(this);
        frame.setVisible(true);
    }
    
    /**
     * Changes the direction of the snake
     * @param d 
     */
    public void changeDirection(int d) {
        switch (d) {
            case 1:
                if (down)
                    return;
                right = false;
                left = false;
                down = false;
                up = true;
                break;
            case 2: 
                if (left)
                    return;
                left = false;
                down = false;
                up = false;                
                right = true;
                break;
            case 3:
                if (up)
                    return;
                left = false;
                up = false;                
                right = false;
                down = true;
                break;
            case 4:
                if (right)
                    return;
                up = false;                
                right = false;
                down = false;                
                left = true;
                break;
        }
    }
    
    /**
     * Movement code
     */
    public void move() {        
        if (right) {
            rects.get(0).x += size;
        } else if (left) {
            rects.get(0).x -= size;
        } else if (down) {
            rects.get(0).y += size;
        } else if (up) {
            rects.get(0).y -= size;
        }

        // Growing code
        for (int i = rects.size() - 1; i > 0; i--) {
            rects.get(i).x = rects.get(i - 1).x;
            rects.get(i).y = rects.get(i - 1).y;
            
            if (readyToGrow)
                grow(rects.get(rects.size() - 1));
        }

        snakeHead = new Rectangle(rects.get(0).x, rects.get(0).y, size, size);
        apple = new Rectangle(xApple, yApple, size, size);
        
        
        // Collision for Snake Bits
        for (int i = rects.size() - 1; i > 1; i--) {
            Point center = new Point((int)snakeHead.getCenterX(), (int)snakeHead.getCenterY());
            if (((Rectangle) rects.get(i)).contains(center)) {
                gameOver = true;
                inGame = false;
            }
        }
        
        // If the snake intersects the apple
        if (snakeHead.intersects(apple)) {
            resetApple();
            readyToGrow = true;
            points++;
        }
        
        // If the snake leaves the screen
        if (!snakeHead.intersects(screen)) {
            gameOver = true;
            inGame = false;
        }
    }
    
    
    /**
     * Used in setup,
     * creates a small initial snake body
     */
    public void createSnake() {
        rects.clear();
        rects.add(new Rectangle(xHead, yHead, size, size));
        rects.add(new Rectangle(rects.get(0).x - size, yHead, size, size));
        rects.add(new Rectangle(rects.get(1).x - size, yHead, size, size));
        rects.add(new Rectangle(rects.get(2).x - size, yHead, size, size));
        rects.add(new Rectangle(rects.get(3).x - size, yHead, size, size));
        changeDirection(2);
    }
    
    /**
     * Resets the apple coordinates
     */
    public void resetApple() {
        xApple = size * randomGen.nextInt((frameWidth - size) / size);
        yApple = size * randomGen.nextInt((frameHeight - size) / size);
        System.out.println("New Apple: " + xApple + ", " + yApple);
    }
    
    /**
     * Add a block to the snake
     */
    public void grow(Rectangle p) {
        rects.add(new Rectangle((int) p.getX(), (int) p.getY(), size, size));
        readyToGrow = false;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        // Draw the Black Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, frameWidth, frameHeight);

        
        // Cross to exit game
        g.drawImage(crossImage, cross.x, cross.y, cross.width, cross.height, null);
        
        // For testing purposes only... 
        if (gridMode)
            for (int i = 0; i < frameWidth; i += size) {
                // Draws the Grid
                g.setColor(Color.GRAY);
                g.drawLine(i, 0, i, frameHeight);
                g.drawLine(0, i, frameWidth, i);
            }
        
        
        
        if (startScreen) {
            // Draw the Title Image
            g.drawImage(title, (frameWidth / 2) - (title.getWidth(null) / 2), (frameHeight / 2) - (title.getHeight(null) / 2) - 100, null);
            
            // Draw the Play Button
            if (!hoverOverPlayButton)
                g.drawImage(playButton1, playButton.x, playButton.y, null);
            else
                g.drawImage(playButton2, playButton.x, playButton.y, null);
            
        } else if (inGame) {
            gameScoreAdded = false;
            
            // Draw the snake points
            for (int i = 0; i < rects.size(); i++) {
                Rectangle temp = rects.get(i);
                if (i == 1) g.setColor(Color.YELLOW);
                else if (i == rects.size() - 1) g.setColor(Color.RED);
                else g.setColor(Color.GREEN);

                // Draw the Snake
                g.fillRect((int) temp.getX(), (int) temp.getY(), size, size);

                // Draw the rects around each Snake rect
                g.setColor(Color.WHITE);
                g.drawRect((int) temp.getX(), (int) temp.getY(), size, size);

            }

            // Draw the point count
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Apples Eaten: " + points, 20, 40);

            // Draw the Apple
            g.drawImage(appleImage, xApple, yApple, size, size, null);
            
        } else if (gameOver) {
            if (!gameScoreAdded) {
                addHighscore(name + ":" + points);
                gameScoreAdded = true;
            }
            
            // Game Over!
            g.drawImage(gameOverImage, (frameWidth / 2) - (gameOverImage.getWidth(null) / 2), (frameHeight / 2) - (gameOverImage.getHeight(null) / 2) - 100, null);
            
            // Draw the Play Button
            if (!hoverOverPlayButton)
                g.drawImage(replay, playButton.x, playButton.y, null);
            else
                g.drawImage(replay2, playButton.x, playButton.y, null);
            
        }
    }
    
    /**
     * Sets up a new game 
     */
    public void setup() {
        createSnake();
        resetApple();
        right = true;
        points = 0;
    }
    
    public void addHighscore(String scoreInfo) {
        highscoreWriter.print(scoreInfo);
    }
    
    /**
     * Main Method
     */
    public static void main(String[] args) {
        name = JOptionPane.showInputDialog("Enter your name!");
        Snake s = new Snake();
        Thread snake = new Thread(s);
        snake.start();
    }

    @Override
    public void run() {
        while (true) {
            if (inGame)
                move();
            
            // Repaint the screen
            repaint();
            try {
                Thread.sleep(50);
            } catch(Exception ex) { ex.printStackTrace(); }
        }    
    }

}
