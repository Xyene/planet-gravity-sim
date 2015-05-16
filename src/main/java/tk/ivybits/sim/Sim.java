package tk.ivybits.sim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

public class Sim extends JFrame {
    public class Body {
        public double m;
        public double x, y;
        public double vx, vy;
        public double fx, fy;
        public double r;
    }

    public List<Body> bodies = new LinkedList<>();
    public static final double G = 6.67;
    public static final int UPS = 60;

    public void update(double t) {
        List<Body> collided = new LinkedList<>();
        for (int i = 0; i < bodies.size(); i++) {
            Body u = bodies.get(i);
            for (int j = 0; j < bodies.size(); j++) {
                Body v = bodies.get(j);
                if (v == u) continue;
                double dx = v.x - u.x;
                double dy = v.y - u.y;
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r <= (u.r + v.r)) {
                    // pi * r * r
                    if (v.m > u.m) {
                        Body temp = v;
                        v = u;
                        u = temp;
                    }
                    double av = Math.PI * v.r * v.r;
                    double au = Math.PI * u.r * u.r;
                    double f = Math.sqrt((av + au) / Math.PI);

                    u.m += v.m;
                    u.r = f;

                    u.vx += v.vx;
                    u.vy += v.vy;
                    u.vx /= u.m;
                    u.vy /= u.m;

                    collided.add(v);
                }
            }
        }

        bodies.removeAll(collided);

        for (int i = 0; i < bodies.size(); i++) {
            Body u = bodies.get(i);
            u.fx = u.fy = 0.0;

            // if (u.m > 32) continue;

            for (int j = 0; j < bodies.size(); j++) {
                if (i == j) continue;
                Body v = bodies.get(j);

                double dx = v.x - u.x;
                double dy = v.y - u.y;

                double r = Math.sqrt(dx * dx + dy * dy);

                double F = G * (u.m * v.m) / (r * r);

                u.fx += F * dx / r;
                u.fy += F * dy / r;
            }
        }

        for (Body u : bodies) {
            // if (u.m > 32) continue;
            double ax = u.fx / u.m;
            double ay = u.fy / u.m;

            u.vx += ax * t;
            u.vy += ay * t;

            u.x += u.vx * t;
            u.y += u.vy * t;
        }
    }

    public Sim() {
        super("Planet Gravity Simulator");
        setLayout(new BorderLayout());
        add(new JPanel() {
            private Timer ticker;
            private long last;
            private MouseAdapter mouse;
            private int sx, sy;
            private int cx, cy;
            private boolean click;

            @Override
            public void addNotify() {
                super.addNotify();
                last = System.currentTimeMillis();
                ticker = new Timer(1000 / UPS, (x) -> {
                    long t = -1 * (last - (last = System.currentTimeMillis()));
                    double v = t / (1000.0 / UPS);
                    // This is technically not correct, but the results are good enough if ran often enough
                    Sim.this.update(v);
                    paintImmediately(getBounds());
                });
                ticker.start();
                addMouseListener(mouse = new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                click = false;
                                double dx = sx - cx;
                                double dy = sy - cy;

                                dx /= 32;
                                dy /= 32;

                                Body b = new Body();
                                b.x = sx;
                                b.y = sy;
                                b.m = 1;
                                b.r = 2;
                                b.vx = dx;
                                b.vy = dy;
                                bodies.add(b);
                                sx = sy = cx = cy = 0;
                            }

                            @Override
                            public void mouseDragged(MouseEvent e) {
                                cx = e.getX();
                                cy = e.getY();
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                cx = sx = e.getX();
                                cy = sy = e.getY();
                                click = true;
                            }
                        }
                );
                addMouseMotionListener(mouse);
            }

            @Override
            public void removeNotify() {
                if (ticker.isRunning()) ticker.stop();
                super.removeNotify();
            }

            @Override
            public void paintComponent(Graphics _g) {
                Graphics2D g = (Graphics2D) _g;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (click) {
                    g.setColor(Color.BLUE);
                    g.drawLine(sx, sy, cx, cy);
                    g.fillOval(sx - 3, sy - 3, 6, 6);
                }
                g.setColor(Color.BLACK);
                for (Body b : bodies) {
                    g.fillOval((int) (b.x - b.r), (int) (b.y - b.r), (int) (b.r * 2), (int) (b.r * 2));
                }
            }
        }, BorderLayout.CENTER);

        setSize(new Dimension(640, 480)

        );

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] argv) {
        Sim nu = new Sim();
        nu.setVisible(true);
    }
}
