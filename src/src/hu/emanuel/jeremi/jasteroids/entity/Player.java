/*
 * The MIT License
 *
 * Copyright 2018 Emánuel Jeremi Kádár.
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
package src.hu.emanuel.jeremi.jasteroids.entity;

import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import src.hu.emanuel.jeremi.jasteroids.target.GetTarget;

/**
 * This represents the ship of the player with all the controls and movement.
 * @author Jeremi
 */
public class Player {
    
    // <editor-fold defaultstate="collapsed" desc="fields">
    public Polygon shape;
    private float x, y, angle, rotationSpeed;
    private int speed, accelerationSpeed;
    private boolean isRotLeft, isRotRight, isAcc, isBackward;
    private static final double PI_180_PROP = Math.PI / 180d;
    public ArrayList<Missile> missiles;
    public int scores;
    // </editor-fold>
    
    public class Missile {
        float x, y, angle, speed;
        Asteroid target;
        
        public Missile(float x, float y, float angle, float speed) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.speed = speed;
        }
        
        public Missile(float x, float y, float angle, float speed, Asteroid target) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.speed = speed;
            this.target = target;
        }
        
        public void aim() {
            this.angle = (float) (Math.atan2(target.y - y, target.x - x) * 1 / Player.PI_180_PROP);
        }
        
        public void move() {
            // If there is a target, then chase it!
            if(target != null) {
                aim();
            }
            x += (speed / 10f) * Math.cos(angle * PI_180_PROP);
            y += (speed / 10f) * Math.sin(angle * PI_180_PROP);
        }
        
        public int getIntX() {
            return (int) x;
        }
        
        public int getIntY() {
            return (int) y;
        }
        
        public float getAngle() {
            return angle;
        }
        
        public void setX(int x) {
            this.x = x + .0f;
        }
        
        public void setY(int y) {
            this.y = y + .0f;
        }
    }
    
    public Player(float x, float y, float angle, float rotationSpeed, int accelerationSpeed) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.rotationSpeed = rotationSpeed;
        this.accelerationSpeed = accelerationSpeed;
        this.missiles = new ArrayList<>();
        this.scores = 0;
        initShape();
    }
    
    private void initShape() {
        int x = (int) this.x;
        int y = (int) this.y;
        double radAngle = getRadAngle();
        
        int [] xPoints = new int[] {
            1,
            1 + 20,
            1
        };
        
        int [] yPoints = new int[] {
            1,
            1 + 5,
            1 + 10
        };
        
        int x0 = (int) (xPoints[0] * Math.cos(radAngle) - yPoints[0] * Math.sin(radAngle)) + x;
        int y0 = (int) (xPoints[0] * Math.sin(radAngle) + yPoints[0] * Math.cos(radAngle)) + y;
        
        int x1 = (int) (xPoints[1] * Math.cos(radAngle) - yPoints[1] * Math.sin(radAngle)) + x;
        int y1 = (int) (xPoints[1] * Math.sin(radAngle) + yPoints[1] * Math.cos(radAngle)) + y;
        
        int x2 = (int) (xPoints[2] * Math.cos(radAngle) - yPoints[2] * Math.sin(radAngle)) + x;
        int y2 = (int) (xPoints[2] * Math.sin(radAngle) + yPoints[2] * Math.cos(radAngle)) + y;
        
        shape = new Polygon(new int[] {x0, x1, x2}, new int[] {y0, y1, y2}, 3);
        /*
        // | cos a -sin a |
        // | sin a  cos a |
        // x' = x * cos_a - y * sin_a
        // y' = x * sin_a + y * cos_a
        
        
        Polygon pol = new Polygon(new int[] {x0 + user.getIntX(), x1 + user.getIntX(), x2 + user.getIntX()}, new int[] {y0 + user.getIntY(), y1 + user.getIntY(), y2 + user.getIntY()}, 3);
        */
    }
    
    /**
     * It moves the player with the help of the speed vector.
     */
    public void move() {
        x += ( (speed + .0d) / 10d) * Math.cos(angle * PI_180_PROP);
        y += ( (speed + .0d) / 10d) * Math.sin(angle * PI_180_PROP);
        
        for(int i = 0; i < shape.npoints; i++) {
            shape.xpoints[i] += ( speed / 10d) * Math.cos(angle * PI_180_PROP);
            shape.ypoints[i] += ( speed / 10d) * Math.sin(angle * PI_180_PROP);
        }
    }
    
    /**
     * Accelerate. It can be negative, so the player starts to move backward.
     */
    public void accelerate() {
        if(isAcc) {
            this.speed += accelerationSpeed;
        }
        else if(isBackward) {
            this.speed -= accelerationSpeed;
        }
    }
    
    /**
     * It's an automatic slowdown effect.
     */
    public void slow() {
        if(speed > 0) {
            speed -= accelerationSpeed >> 1;
            if( speed < 0 ) {
                speed = 0;
            }
        } else {
            speed += accelerationSpeed >> 1;
            if( speed > 0 ) {
                speed = 0;
            }
        }
    }
    
    public float[] getCentroid() {
        return new float[] {
            ((shape.xpoints[0] + shape.xpoints[1] + shape.xpoints[2]) + .0f) / 3,
            ((shape.ypoints[0] + shape.ypoints[1] + shape.ypoints[2]) + .0f) / 3
        };
    }
    
    /**
     * Rotates the player by the rotation speed.
     */
    public void rotate() {
        if(isRotLeft) {
            angle -= rotationSpeed;
            
            int x = (int) this.x;
            int y = (int) this.y;
            double radAngle = getRadAngle();

            int [] xPoints = new int[] {
                1,
                1 + 20,
                1
            };

            int [] yPoints = new int[] {
                1,
                1 + 5,
                1 + 10
            };

            int x0 = (int) (xPoints[0] * Math.cos(radAngle) - yPoints[0] * Math.sin(radAngle)) + x;
            int y0 = (int) (xPoints[0] * Math.sin(radAngle) + yPoints[0] * Math.cos(radAngle)) + y;

            int x1 = (int) (xPoints[1] * Math.cos(radAngle) - yPoints[1] * Math.sin(radAngle)) + x;
            int y1 = (int) (xPoints[1] * Math.sin(radAngle) + yPoints[1] * Math.cos(radAngle)) + y;

            int x2 = (int) (xPoints[2] * Math.cos(radAngle) - yPoints[2] * Math.sin(radAngle)) + x;
            int y2 = (int) (xPoints[2] * Math.sin(radAngle) + yPoints[2] * Math.cos(radAngle)) + y;
            
            shape.xpoints[0] = x0;
            shape.xpoints[1] = x1;
            shape.xpoints[2] = x2;
            
            shape.ypoints[0] = y0;
            shape.ypoints[1] = y1;
            shape.ypoints[2] = y2;
        }
        else if(isRotRight) {
            angle += rotationSpeed;
            
            int x = (int) this.x;
            int y = (int) this.y;
            double radAngle = getRadAngle();

            int [] xPoints = new int[] {
                1,
                1 + 20,
                1
            };

            int [] yPoints = new int[] {
                1,
                1 + 5,
                1 + 10
            };

            int x0 = (int) (xPoints[0] * Math.cos(radAngle) - yPoints[0] * Math.sin(radAngle)) + x;
            int y0 = (int) (xPoints[0] * Math.sin(radAngle) + yPoints[0] * Math.cos(radAngle)) + y;

            int x1 = (int) (xPoints[1] * Math.cos(radAngle) - yPoints[1] * Math.sin(radAngle)) + x;
            int y1 = (int) (xPoints[1] * Math.sin(radAngle) + yPoints[1] * Math.cos(radAngle)) + y;

            int x2 = (int) (xPoints[2] * Math.cos(radAngle) - yPoints[2] * Math.sin(radAngle)) + x;
            int y2 = (int) (xPoints[2] * Math.sin(radAngle) + yPoints[2] * Math.cos(radAngle)) + y;
            
            shape.xpoints[0] = x0;
            shape.xpoints[1] = x1;
            shape.xpoints[2] = x2;
            
            shape.ypoints[0] = y0;
            shape.ypoints[1] = y1;
            shape.ypoints[2] = y2;
        }
    }
    
    public void shoot() {
        float[] a = getCentroid();
        missiles.add( new Missile(a[0], a[1], this.angle, 100) );
    }
    
    public void shoot(Asteroid target) {
        missiles.add( new Missile(this.x, this.y, this.angle, 100, target) );
    }
    
    public void printStats() {
        System.out.println("X: " + x + "\nY:" + y + "\nAngle: " + angle + "\nSpeed: " + speed);
        System.out.println("=======================================================");
    }
    
    // <editor-fold defaultstate="collapsed" desc="Controls.">
    /**
     * It turns the player to the mouse cursor.
     * @param mouseX
     * @param mouseY 
     */
    public void followMouse(int mouseX, int mouseY) {
        this.angle = (float) ( Math.atan2(mouseY - y, mouseX - x) * 1 / PI_180_PROP );
    }
    
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode(); // Which key?
        
        // MOVEMENT
        if(c == KeyEvent.VK_W) { isAcc = true; }
        if(c == KeyEvent.VK_A) { isRotLeft = true; }
        if(c == KeyEvent.VK_D) { isRotRight = true; }
        if(c == KeyEvent.VK_S) { isBackward = true; }
    }
    
    public void keyReleased(KeyEvent e, GetTarget t) {
        int c = e.getKeyCode(); // Which key?
        
        // MOVEMENT
        if(c == KeyEvent.VK_W) { isAcc = false; }
        if(c == KeyEvent.VK_A) { isRotLeft = false; }
        if(c == KeyEvent.VK_D) { isRotRight = false; }
        if(c == KeyEvent.VK_S) { isBackward = false; }
        
        // INTERACTION
        if(c == KeyEvent.VK_SPACE) {
            shoot();
        }
        
        if(c == KeyEvent.VK_H) {
            shoot(t.GetTarget());
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Getters and setters.">
    public int getIntX() {
        return (int) x;
    }
    public int getIntY() {
        return (int) y;
    }
    public float getAngle() {
        return angle;
    }
    public double getRadAngle() {
        return (angle + .0d) * PI_180_PROP;
    }
    public void setX(int x) {
        this.x = x + .0f;
    }
    public void setY(int y) {
        this.y = y + .0f;
    }
    // </editor-fold>
}
