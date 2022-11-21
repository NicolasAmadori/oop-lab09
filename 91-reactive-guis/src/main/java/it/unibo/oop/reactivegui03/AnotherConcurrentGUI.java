package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        
        final Agent agent = new Agent();
        new Thread(agent).start();
        new Thread(() -> {
            try {                
                Thread.sleep(10_000);
                agent.stopCounting();
                SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.stop.setEnabled(false));
                SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.up.setEnabled(false));
                SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.down.setEnabled(false));
            } catch (InvocationTargetException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();

        up.addActionListener((e) -> agent.setUp());
        down.addActionListener((e) -> agent.setDown());
        stop.addActionListener((e) -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        private volatile boolean stop;
        private int counter;
        private volatile boolean isUp = true;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));                    
                    this.counter += this.isUp ? 1 : -1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {                    
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to set the counter positive.
         */
        public void setUp() {
            this.isUp = true;
        }

        /**
         * External command to set the counter negative.
         */
        public void setDown() {
            this.isUp = false;
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
    }
}
