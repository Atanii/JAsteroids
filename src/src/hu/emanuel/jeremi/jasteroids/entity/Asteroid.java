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
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Jeremi
 */
public class Asteroid {
    
    // <editor-fold defaultstate="collapsed" desc="fields">
    public Polygon shape;
    public float x, y, angle;
    private int speed;
    public boolean notBreakableFurthermore;
    private static final double PI_180_PROP = Math.PI / 180d;
    // </editor-fold>
    
    public Asteroid(float x, float y, float angle, int speed, Polygon shape) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.shape = shape;
        this.notBreakableFurthermore = false;
    }
    
    public Asteroid(float x, float y, float angle, int speed, Polygon shape, boolean notBreakableFurthermore) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.shape = shape;
        this.notBreakableFurthermore = notBreakableFurthermore;
    }
    
    public void move() {
        x += (speed / 10d) * Math.cos(angle * PI_180_PROP);
        y += (speed / 10d) * Math.sin(angle * PI_180_PROP);
        for(int i = 0; i < shape.npoints; i++) {
            shape.xpoints[i] += (speed / 10d) * Math.cos(angle * PI_180_PROP);
            shape.ypoints[i] += (speed / 10d) * Math.sin(angle * PI_180_PROP);
        }
    }
    
    public ArrayList<Asteroid> breakIntoPieces() {
        Random rng = new Random();
        ArrayList<Asteroid> pieces = new ArrayList<>();
        
        for(int i = 0; i < 5; i++) {
            int tempX = (int) this.x;
            int tempY = (int) this.y;
            pieces.add( new Asteroid(
                x, y,
                1 + rng.nextFloat() * 360, rng.nextInt(80),
                new Polygon(
                    new int[] { tempX, tempX + rng.nextInt(30), tempX + rng.nextInt(20) },
                    new int[] { tempY, tempY + rng.nextInt(5), tempY + 20 + rng.nextInt(10) },
                    3
                ),
                true
            ));
        }
        
        return pieces;
    }
}
