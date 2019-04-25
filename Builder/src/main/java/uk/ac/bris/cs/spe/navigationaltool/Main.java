package uk.ac.bris.cs.spe.navigationaltool;

import javax.swing.*;
import java.io.File;

public class Main {


    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        try {
            int f = Integer.parseInt(args[0]);
            init(f);
        }catch (ArrayIndexOutOfBoundsException e) {
            init(8);
        }
    }

    private static void init(int fc) {
        JFrame init = new JFrame("Graph Builder: Init");
        init.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();

        panel.add(new JLabel("Enter floor string (e.g. b, 0, 1, m etc):"));
        JTextField tFloor = new JTextField(4);
        panel.add(tFloor);
        JButton go = new JButton("Start Builder");
        go.addActionListener(e -> {
            String floor = tFloor.getText();
            if (floor.equals("")) {
                tFloor.requestFocus();
            } else {
                File f = new File("maps/map" + floor + ".png");
                if (f.isFile()) {
                    init.dispose();
                    new Builder(floor, fc);
                } else tFloor.requestFocus();
            }
        });
        panel.add(go);
        init.getRootPane().setDefaultButton(go);
        init.add(panel);
        init.pack();
        init.setVisible(true);
    }
}
