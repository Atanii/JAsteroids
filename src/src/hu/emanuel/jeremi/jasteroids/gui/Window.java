/*
 * The MIT License
 *
 * Copyright 2018 Jeremi Emánuel Kádár.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package src.hu.emanuel.jeremi.jasteroids.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import src.hu.emanuel.jeremi.jasteroids.entity.Asteroid;
import src.hu.emanuel.jeremi.jasteroids.entity.Player;
import src.hu.emanuel.jeremi.jasteroids.entity.Player.Missile;
import src.hu.emanuel.jeremi.jasteroids.target.GetTarget;

/**
 *
 * @author Jeremi
 */
public class Window extends JPanel implements KeyListener, MouseListener, ActionListener {

    // <editor-fold defaultstate="collapsed" desc="Fields.">
    // Window:
    private JFrame window;
    private final int W_WIDTH = 800, W_HEIGHT = 800;
    
    // Player:
    Player user;
    ArrayList<Asteroid> as;
    
    // Game state
    GameState state;
    
    // Temporary solution instead of normal gameloop:
    Timer timer;
    
    // "Function pointer" to give a target."
    GetTarget t = () -> { return as.get( (new Random()).nextInt(as.size()) ); };
    // </editor-fold>
    
    public enum GameState {
        MENU, GAME, GAME_OVER, VICTORY
    }
    
    public Window() {
        // JFrame
        window = new JFrame("JAsteroids by Jeremi, 2018");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(W_WIDTH, W_HEIGHT);
        window.setLocationRelativeTo(null);
        window.setLayout(new FlowLayout());
        
        // JPanel
        this.setPreferredSize(new Dimension(W_WIDTH, W_HEIGHT));
        this.setBackground(Color.BLACK);
        
        // Init player:
        user = new Player(W_WIDTH >> 1, W_HEIGHT >> 1, 0f, 10f, 5);
        // Init asteroids
        as = new ArrayList<>();
        generateAsteroids(10);
        
        // State:
        state = GameState.MENU;
        
        // Adding components to window:
        window.add(this);
        window.addKeyListener(this);
            // window.addMouseListener(this);
            
        // Temporary solution instead of normal gameloop:
        timer = new Timer(60, this);
        timer.start();
                        
        // Packing and showing:
        window.pack();
        window.setVisible(true);
    }
    
    // <editor-fold defaultstate="" desc="Updating.">
    @Override
    public void actionPerformed(ActionEvent e) {
        if( state == GameState.GAME ) {
            // Updating game elements:
            updatePlayer();
            updateMissiles();
            updateAsteroids();
        }
        // Render:
        repaint();
    }
    
    public void updatePlayer() {
        user.accelerate();
        user.slow();
        user.move();
        user.rotate();
        teleport(user);
        
        // check user asteroid collision        
        for(int i = 0; i < as.size(); i++) {
            if( sat(as.get(i).shape, user.shape) ) {
                state = GameState.GAME_OVER;
            }
        }
        
        //user.printStats();
    }
    
    public void updateMissiles() {
        for(int i = 0; i < user.missiles.size(); i++) {
            user.missiles.get(i).move();
            teleport(user.missiles.get(i));
            
            for(int a = 0; a < as.size(); a++) {
                if(doesCollideWithAsteroid(user.missiles.get(i).getIntX(), user.missiles.get(i).getIntY(), as.get(a))) {
                    if( as.get(a).notBreakableFurthermore == false ) {
                        as.addAll(as.get(a).breakIntoPieces());
                    }
                    as.remove(as.get(a));
                    user.missiles.remove(user.missiles.get(i));
                    ++user.scores;
                    if(as.size() == 0) {
                        state = GameState.VICTORY;
                    }
                    break;
                }
            }
        }
    }
    
    public void updateAsteroids() {
        for(int i = 0; i < as.size(); i++) {
            as.get(i).move();
            teleport(as.get(i));
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Teleport for objects gone out of screen.">
    public void teleport(Player user) {
        int x = user.getIntX();
        int y = user.getIntY();
        float angle = user.getAngle();
        
        if(x > W_WIDTH) {
            user.setX(x - W_WIDTH);
        }
        else if(x < 0) {
            user.setX(x + W_WIDTH);
        }
        
        if(y > W_WIDTH) {
            user.setY(y - W_HEIGHT);
        }
        else if(y < 0) {
            user.setY(y + W_HEIGHT);
        }
    }
    
    public void teleport(Missile m) {
        int x = m.getIntX();
        int y = m.getIntY();
        float angle = m.getAngle();
        
        if(x > W_WIDTH) {
            m.setX(x - W_WIDTH);
        }
        else if(x < 0) {
            m.setX(x + W_WIDTH);
        }
        
        if(y > W_WIDTH) {
            m.setY(y - W_HEIGHT);
        }
        else if(y < 0) {
            m.setY(y + W_HEIGHT);
        }
    }
    
    public void teleport(Asteroid a) {
        int[] x = a.shape.xpoints;
        int[] y = a.shape.ypoints;
        int s = a.shape.npoints;
        
        int counterX = 0, counterY = 0;
        for(int i = 0; i < s; i++) {
            if( x[i] > W_WIDTH ) {
                ++counterX;
            }
            else if( x[i] < 0 ) {
                --counterX;
            }
            
            if( y[i] > W_HEIGHT ) {
                ++counterY;
            }
            else if( y[i] < 0 ) {
                --counterY;
            }
        }
        
        if(counterX == s) {
            for(int i = 0; i < s; i++) {
                x[i] -= W_WIDTH;
            }
        }
        else if(counterX == -s) {
            for(int i = 0; i < s; i++) {
                x[i] += W_WIDTH;
            }
        }
        
        if(counterY == s) {
            for(int i = 0; i < s; i++) {
                y[i] -= W_HEIGHT;
            }
        }
        else if(counterY == -s) {
            for(int i = 0; i < s; i++) {
                y[i] += W_HEIGHT;
            }
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Physics, collision...etc.">
    public boolean doesCollideWithAsteroid(int x, int y, Asteroid as) {        
        int j = as.shape.npoints - 1;
        boolean isCollide = false;
        
        for(int i = 0; i < as.shape.npoints; i++) {
            if( ( (as.shape.ypoints[i] > y) != (as.shape.ypoints[j] > y) ) &&
                (x < ( as.shape.xpoints[i] + (as.shape.xpoints[j] - as.shape.xpoints[i]) * (y - as.shape.ypoints[i]) / (as.shape.ypoints[j] - as.shape.ypoints[i]) ) )
            ) {
                isCollide = !isCollide;
            }
            j = i;
        }
        
        return isCollide;
    }
    
    public float dotProduct(float[] v1, float[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1];
    }
    
    public float[] normalize(float x, float y) {
        float magnitude = (float) Math.sqrt( x * x + y * y );
        return new float[] { x / magnitude , y / magnitude };
    }
    
    public float[] transpAndNormal(float[] v) {
        return new float[] { v[1] , -v[0] };
    }
    
    public class Segment {
        public float[] p0, p1, dir;
        Segment(float[] p0, float[] p1) {
            dir = new float[] { p1[0] - p0[0] , p1[1] - p0[1] };
            this.p0 = p0;
            this.p1 = p1;
        }
    }
    
    public float[] projectPolygonToAxis(Polygon a, float[] axis) {
        // Vector projection: a (dot) b'
        // b' = normalized b        
        axis = normalize(axis[0], axis[1]);
        
        float min = dotProduct(new float[] { a.xpoints[0], a.ypoints[0] }, axis);
        float max = min;
        float proj;
        
        for(int i = 0; i < a.npoints; i++) {
            proj = dotProduct(new float[] { a.xpoints[i], a.ypoints[i] }, axis);
            if( proj > max ) {
                max = proj;
            }
            if( proj < min ) {
                min = proj;
            }
        }
        
        float[] res = new float[] { min, max };
        
        return res;
    }
    
    /**
     * Separating Axis Theorem for collision detection between asteroid and space ship.
     * @param asteroid
     * @param spaceship
     * @return 
     */
    public boolean sat(Polygon asteroid, Polygon spaceship) {
        
        // Segments for the polygons:
        // Structure: p0(x, y) , p1(x, y), dir(x, y)
        Segment[] a = new Segment[asteroid.npoints];
        Segment[] b = new Segment[spaceship.npoints];
        
        for(int i = 0; i < a.length; i++) {
            if( i + 1 < asteroid.npoints ) {
                a[i] = new Segment(
                    new float[] { asteroid.xpoints[i] , asteroid.ypoints[i] },
                    new float[] { asteroid.xpoints[i + 1] , asteroid.ypoints[i + 1] }
                );
            } else {
                a[i] = new Segment(
                    new float[] { asteroid.xpoints[i] , asteroid.ypoints[i] },
                    new float[] { asteroid.xpoints[0] , asteroid.ypoints[0] }
                );
            }            
        }
        
        for(int i = 0; i < b.length; i++) {
            if( i + 1 < spaceship.npoints ) {
                b[i] = new Segment(
                    new float[] { spaceship.xpoints[i] , spaceship.ypoints[i] },
                    new float[] { spaceship.xpoints[i + 1] , spaceship.ypoints[i + 1] }
                );
            } else {
                b[i] = new Segment(
                    new float[] { spaceship.xpoints[i] , spaceship.ypoints[i] },
                    new float[] { spaceship.xpoints[0] , spaceship.ypoints[0] }
                );
            }            
        }
        
        float[] axis = new float[2];
        float[] projA, projB;
        // Project polygons and see if projections overlap:
        for(int i = 0; i < asteroid.npoints; i++) {
            // get the axis
            axis[0] = a[i].dir[0];
            axis[1] = a[i].dir[1];
            // get the perpendicular normal vector
            axis = transpAndNormal(axis);
            // project polygons
            projA = projectPolygonToAxis(asteroid, axis);
            projB = projectPolygonToAxis(spaceship, axis);
            if( (projA[1] < projB[0]) ||
                (projA[0] > projB[1]) ||
                (projB[1] < projA[0]) ||
                (projB[0] > projA[1])
            ) {
                return false;
            }
        }
        
        for(int i = 0; i < spaceship.npoints; i++) {
            // get the axis
            axis[0] = b[i].dir[0];
            axis[1] = b[i].dir[1];
            // get the perpendicular normal vector
            axis = transpAndNormal(axis);
            // project polygons
            projA = projectPolygonToAxis(spaceship, axis);
            projB = projectPolygonToAxis(asteroid, axis);
            if( (projA[1] < projB[0]) ||
                (projA[0] > projB[1]) ||
                (projB[1] < projA[0]) ||
                (projB[0] > projA[1])
            ) {
                return false;
            }
        }
        
        return true;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Rendering.">
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        switch(state) {
            case MENU:
                g.setColor(Color.GRAY);
                renderMenu(g);
                break;
            case GAME:
                g.setColor(Color.BLUE);
                renderPlayer(g);
                g.setColor(Color.YELLOW);
                renderAsteroids(g);
                g.setColor(Color.WHITE);
                renderMissiles(g);
                g.setColor(Color.MAGENTA);
                renderScore(g);
                break;
            case GAME_OVER:
                g.setColor(Color.RED);
                renderGameOver(g);
                break;
            case VICTORY:
                g.setColor(Color.WHITE);
                renderVictory(g);
                break;
            default:
                break;
        }
    }
    
    public void renderMenu(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 50));
        g.drawString("JAsteroids", (W_WIDTH >> 1) - 150, W_HEIGHT >> 2);
        g.setFont(new Font("Monospaced", Font.PLAIN, 30));
        g.drawString("created by Jeremi", (W_WIDTH >> 1) - 150, (W_HEIGHT >> 2) + 50 );
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("WASD to move", 100, (W_HEIGHT >> 2) + 300 );
        g.drawString("SPACE to shoot", 100, (W_HEIGHT >> 2) + 350 );
        g.drawString("H to launch homing missile", 100, (W_HEIGHT >> 2) + 400 );
    }
    
    public void renderGameOver(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 50));
        g.drawString("YOUR SHIP WAS DESTROYED", 50, W_HEIGHT >> 1);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("SCORE: " + user.scores, 50, (W_HEIGHT >> 1) + 200 );
    }
    
    public void renderVictory(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 50));
        g.drawString("All asteroids destroyed!!!", 15, W_HEIGHT >> 2);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 30));
        g.drawString("SCORE: " + user.scores, (W_WIDTH >> 1) - 100, W_HEIGHT >> 1);
    }
    
    public void renderPlayer(Graphics g) {
        
        /*
        int [] xPoints = new int[] {
            1,
            1,
            1 + 20
        };
        
        int [] yPoints = new int[] {
            1,
            1 + 10,
            1 - 5
        };
        
        // | cos a -sin a |
        // | sin a  cos a |
        // x' = x * cos_a - y * sin_a
        // y' = x * sin_a + y * cos_a
        int x0 = (int) (xPoints[0] * Math.cos(user.getRadAngle()) - yPoints[0] * Math.sin(user.getRadAngle()));
        int y0 = (int) (xPoints[0] * Math.sin(user.getRadAngle()) + yPoints[0] * Math.cos(user.getRadAngle()));
        
        int x1 = (int) (xPoints[1] * Math.cos(user.getRadAngle()) + yPoints[1] * Math.sin(user.getRadAngle()));
        int y1 = (int) (xPoints[1] * Math.sin(user.getRadAngle()) - yPoints[1] * Math.cos(user.getRadAngle()));
        
        int x2 = (int) (xPoints[2] * Math.cos(user.getRadAngle()) - yPoints[2] * Math.sin(user.getRadAngle()));
        int y2 = (int) (xPoints[2] * Math.sin(user.getRadAngle()) + yPoints[2] * Math.cos(user.getRadAngle()));
        
        Polygon pol = new Polygon(new int[] {x0 + user.getIntX(), x1 + user.getIntX(), x2 + user.getIntX()}, new int[] {y0 + user.getIntY(), y1 + user.getIntY(), y2 + user.getIntY()}, 3);
        */
        
        g.drawPolygon(user.shape);
        
        // g.drawLine(x0 + user.getX() + 2, y2 + user.getY(), x0 + user.getX() + 1000 , y2 + user.getY());
    }
    
    public final void renderAsteroids(Graphics g) {
        for(Asteroid a : as) {
            g.drawPolygon(a.shape);
        }
    }
    
    public void renderMissiles(Graphics g) {
        for(Missile m : user.missiles) {
            g.fillRect(m.getIntX(), m.getIntY(), 1, 1);
        }
    }
    
    public void renderScore(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("SCORE: " + user.scores, 6, 20);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Generating asteroids.">
    public void generateAsteroids(int amount) {
        Random rng = new Random();
        
        for(int i = 0; i < amount; i++) {
            int x = rng.nextInt(W_WIDTH);
            int y = rng.nextInt(W_HEIGHT);
            as.add(new Asteroid(
                    x, y,
                    1 + rng.nextFloat() * 360, rng.nextInt(50),
                    generatePolygon(x, y)
            ));
        }
    }
    
    public Polygon generatePolygon(int x, int y) {
        Random rng = new Random();
        
        int[] xPoints = new int[] {
            x, 
            x + (1 + rng.nextInt(30)),
            x + (40 + rng.nextInt(60)),
            x + (20 + rng.nextInt(40)),
            x + (10 + rng.nextInt(20)),
            x + (0 + rng.nextInt(10)),
            x - (0 + rng.nextInt(10))
        };
        
        int[] yPoints = new int[] {
            y, 
            y - (1 + rng.nextInt(30)),
            y - (20 + rng.nextInt(30)),
            y + (20 + rng.nextInt(40)),
            y + (50 + rng.nextInt(60)),
            y + (40 + rng.nextInt(50)),
            y + (40 + rng.nextInt(50))
        };
        
        return new Polygon(xPoints, yPoints, 7);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Controls.">
    @Override
    public void keyTyped(KeyEvent e) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if( (state == GameState.GAME_OVER) ^ (state == GameState.VICTORY) ) {
            System.exit(0);
        }
        
        if( state == GameState.GAME ) {
            // control player if that's the case
            user.keyPressed(e);
        }
        
        // misc.
        if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) { System.exit(0); }
        // ...
        if( e.getKeyCode() == KeyEvent.VK_T ) { as.addAll( as.get(0).breakIntoPieces() ); as.remove(as.get(0)); }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if( state == GameState.GAME ) {
            user.keyReleased(e, t);
        } else {
            if(e.getKeyCode() == KeyEvent.VK_SPACE) { state = GameState.GAME; }
        }
    }

    // ABOVE: KEYBOARD
    
    // BELOW: MOUSE
    
    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
