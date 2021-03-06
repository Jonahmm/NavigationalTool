package uk.ac.bris.cs.spe.navigationaltool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Builder implements MouseListener, ActionListener{
    private JFrame frame;
    private boolean buttonVis = true;
//    private boolean joinMode = false;
    private enum Mode {EDIT, JOIN, HIDE}
    private Mode mode = Mode.EDIT;
    private JLabel picLabel;
    private Map<JButton, LocationDataHolder> locs = new HashMap<>();
    private java.util.List<PathDataHolder> paths = new ArrayList<>();
    private JButton selected;
    private Image img; private BufferedImage buf;
    private JTextArea log;
    private int nextId = 0;
    private final int factor;
    private static final String USERS = "STUDENT DISABLED_STUDENT STAFF DISABLED_STAFF";
    private final String floor;

    Builder(String f, int fct) {
        floor = f;
        factor = fct;
        javax.swing.SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private String modeString() {
        switch(mode) {
            case EDIT: return "Edit Mode";
            case JOIN: return "Join Mode";
            default:   return "Hide Mode";
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("Navigational Tool Graph Builder: floor " + floor);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.getLayeredPane().setLayout(null);

        BufferedImage myPicture;
        try {
            myPicture = ImageIO.read(new File("maps/map" + floor + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        assert myPicture != null;
        int scaleX = myPicture.getWidth() / factor;
        int scaleY = myPicture.getHeight() / factor;
        img = myPicture.getScaledInstance(scaleX, scaleY, Image.SCALE_SMOOTH);
        picLabel = new JLabel(new ImageIcon(img));

        Insets ins = frame.getContentPane().getInsets();
        picLabel.setBounds(ins.left, ins.top, picLabel.getPreferredSize().width, picLabel.getPreferredSize().height);
        frame.getLayeredPane().add(picLabel, new Integer(1));

        JButton join = new JButton(modeString());
        join.addActionListener(e -> {
            mode = mode == Mode.EDIT ? Mode.JOIN
                 : mode == Mode.JOIN ? Mode.HIDE
                 : Mode.EDIT;
            join.setText(modeString());
        });

        join.setBounds(scaleX, 24, 80, 20);
        join.setVisible(true);
        frame.getLayeredPane().add(join, new Integer(1));

        log = new JTextArea(20,18);
        JScrollPane sp = new JScrollPane(log);
        log.setEditable(false);
        log.setWrapStyleWord(true); log.setLineWrap(true);
        sp.setBounds(scaleX + 5, 50, 270, 600);

        JButton export = new JButton("Export");
        export.setBounds(scaleX + 100, 24, 80, 20);
        export.setVisible(true);
        export.addActionListener(e -> exportAll());
        frame.getLayeredPane().add(export, new Integer(1));

        JButton transform = new JButton("T");
        transform.setBounds(scaleX + 190, 24, 20, 20);
        transform.setVisible(true);
        transform.addActionListener(e -> translate());
        frame.getLayeredPane().add(transform, new Integer(1));

        JButton scale = new JButton("S");
        scale.setBounds(scaleX + 220, 24, 20, 20);
        scale.setVisible(true);
        scale.addActionListener(e -> scale());
        frame.getLayeredPane().add(scale, new Integer(1));

        JButton reid = new JButton("ID");
        reid.setBounds(scaleX + 250, 24, 25, 20);
        reid.setVisible(true);
        reid.addActionListener(e -> reId());
        frame.getLayeredPane().add(reid, new Integer(1));

        JTextField filter = new JTextField(8);
        filter.setBounds(scaleX, 2, 50, 20);
        filter.setVisible(true);
        frame.getLayeredPane().add(filter, new Integer(1));

        JButton filtb = new JButton("Filter");
        filtb.setBounds(scaleX + 60, 2, 50, 20);
        filtb.setVisible(true);
        filtb.addActionListener(e -> {
            String s = filter.getText();
            if (s.isEmpty()) for (JButton b : locs.keySet()) b.setVisible(true);
            else {
                for (JButton b : locs.keySet()) {
                    if (!locs.get(b).code.startsWith(s)) b.setVisible(false);
                }
            }
        });
        frame.getLayeredPane().add(filtb, new Integer(1));

        frame.setSize(scaleX + 280,scaleY + ins.top);
        frame.getLayeredPane().add(sp, new Integer(1));

        frame.setVisible(true);
        frame.addMouseListener(this);


        resetBuffer();
        load();
        welcome();

    }

    private void scale() {
        JFrame sframe = new JFrame("Scale all locations");
        JTextField scaleBy = new JTextField(10);
        JButton go = new JButton("Scale");
        JPanel p = new JPanel();
        p.add(scaleBy); p.add(go);
        sframe.add(p);
        sframe.pack();
        go.addActionListener(e -> {
            try {
                double u = Double.parseDouble(scaleBy.getText());
                if (u > 0) {
                    for (LocationDataHolder l : locs.values()) {
                        l.x *= u;
                        l.y *= u;
                    }
                    for (JButton b : locs.keySet()) {
                        int x = (int) Math.round(b.getX() * u);
                        int y = (int) Math.round(b.getY() * u);
                        b.setLocation(x,y);
                    }
                    log("Scaled all locations by " + u);
                    resetBuffer();
                    drawAllPaths();
                    frame.repaint();
                } else log("Invalid scale. Must be a positive number.");
            } catch (Exception x) {
                log("Scale failed: ");
                log(x.getMessage());
            }
            sframe.dispose();
        });
        sframe.getRootPane().setDefaultButton(go);
        sframe.setVisible(true);
    }

    private void translate() {
        JFrame tframe = new JFrame("Translate all locations");
        JTextField transX = new JTextField(10); transX.setText("X");
        JTextField transY = new JTextField(10); transY.setText("Y");
        JButton go = new JButton("Translate");
        JPanel p = new JPanel();
        p.add(transX); p.add(transY); p.add(go);
        tframe.add(p);
        tframe.pack();
        go.addActionListener(e -> {
            try {
                int tx = Integer.parseInt(transX.getText());
                int ty = Integer.parseInt(transY.getText());

                for (LocationDataHolder l : locs.values()) {
                    l.x += tx;
                    l.y += ty;
                }
                for (JButton b : locs.keySet()) {
                    b.setLocation(b.getX() + (tx / factor), b.getY() + (ty / factor));
                }
                log("Translated all locations by " + tx + "," + ty);
                resetBuffer();
                drawAllPaths();
                frame.repaint();
            } catch (Exception x) {
                log("Translate failed: ");
                log(x.getMessage());
            }
            tframe.dispose();
        });
        tframe.getRootPane().setDefaultButton(go);
        tframe.setVisible(true);
    }

    private String familyCheck() {
        Map <String, LocationDataHolder> _1;

        for (LocationDataHolder l : locs.values()) {
            if (locs.values().stream().filter(m -> l.code.equals(m.code) && (!m.name.equals("#"))).count() > 1) {
                // There exists more than one potential parent for this code
                // Allow < 1 to account for cross-floor spaces
                return l.code;
            }
        }
        return "";
    }

    private void exportAll() {
        String s = familyCheck();
        if (!s.isEmpty()) {
            log("WARNING: Ambiguously structured family at " + s + "!\nExporting anyway, but fixing is recommended.");
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("built/" + floor + ".locations"));
            int x,y;
            // Child list: must be added after parents
            List<LocationDataHolder> children = new ArrayList<>();
            for (LocationDataHolder l : locs.values().stream().sorted(Comparator.comparing(a -> a.code)).collect(Collectors.toList())) {
                if (l.code.equals("")) throw new IllegalArgumentException("Cannot export " + l.getString() + ": code missing!");
                if (l.name.equals("#")) children.add(l);
                else {
                    x = l.x;// * FACTOR;
                    y = l.y;// * FACTOR;
                    bw.write(l.id + "," + l.code + "," + l.name + "," + l.floor + "," + x + "," + y);
                    bw.newLine();

                }
            }
            bw.write("#\n#    All locations below this point are children.\n" +
                     "#    For proper ordering ensure children have name set to '#'\n#\n");
            for (LocationDataHolder l : children) {
                x = l.x;// * FACTOR;
                y = l.y;// * FACTOR;
                bw.write(l.id + "," + l.code + "," + l.name + "," + l.floor + "," + x + "," + y);
                bw.newLine();
            }
            bw.close();
            log("Exported " + locs.values().size() + " locations to built/" + floor + ".locations");
            //Paths
            bw = new BufferedWriter(new FileWriter("built/" + floor + ".paths"));
            for (PathDataHolder p : paths) {
                bw.write("# " + p.a.code + "-" + p.b.code);
                bw.newLine();
                bw.write(p.a.id + "," +  p.b.id + "," + (p.user.equals("") ? USERS : p.user));
                bw.newLine();
            }
            bw.close();
            log("Exported " + paths.size() + " paths to built/" + floor + ".paths");

        } catch (Exception e) {
            log("Error exporting!");
            log(e.getMessage());
        }
    }

    private int newId() {
        int id = 0;
        while (id < nextId) {
            int finalId = id;
            if (locs.values().stream().noneMatch(l -> l.id == finalId)) return id;
            ++id;
        }
        return nextId++;
    }

    private void reId() {
        int id = 0;
        for (LocationDataHolder l : locs.values().stream().sorted(Comparator.comparingInt(a ->a.id)).collect(Collectors.toList())) {
            l.id = id++;
        }
        // To test it hasn't broken everything
        resetBuffer();
        drawAllPaths();
    }

    private void load() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("built/" + floor + ".locations"));
            String ln;
            while ((ln = br.readLine()) != null) {
                if(!ln.startsWith("#")) { //Support comments
                    String[] fields = ln.split(",");
                    //x,y,floor,code,name
                    int id = Integer.valueOf(fields[0]);
                    LocationDataHolder l = new LocationDataHolder(
                            id, Integer.valueOf(fields[4]), Integer.valueOf(fields[5]), fields[3], fields[1], fields[2]);
                    JButton btn = new JButton(l.code);
                    btn.setVisible(true);

                    if (id >= nextId) nextId = id + 1;

                    FontMetrics m = btn.getFontMetrics(btn.getFont());
                    int sz = m.stringWidth(btn.getText());
                    btn.setBounds((l.x / factor) - (sz /2) - 10, (l.y / factor) - 10, sz + 20, 20);

                    btn.addActionListener(this);
                    btn.addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent d) {
                            if(d.getButton() == MouseEvent.BUTTON3) {
                                deleteLoc(btn);
                            }
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {

                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {

                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {

                        }

                        @Override
                        public void mouseExited(MouseEvent e) {

                        }
                    });

                    locs.put(btn, l);
                    frame.getLayeredPane().add(btn, new Integer(2));

//                    log("Loaded location " + l.getString());
                }
            }
            log("Loaded " + Integer.toString(locs.keySet().size()) + " locations");
            br = new BufferedReader(new FileReader("built/" + floor + ".paths"));
            while ((ln = br.readLine()) != null) {
                if (!ln.startsWith("#")) { //support comments
                    String[] fields = ln.split(",");
                    LocationDataHolder a = findLocById(Integer.valueOf(fields[0]));
                    LocationDataHolder b = findLocById(Integer.valueOf(fields[1]));
                    paths.add(new PathDataHolder(a,b,fields[2]));
//                    log("Loaded path " + a.getString() + "-" + b.getString());
                }
            }
            log("Loaded " + Integer.toString(paths.size()) + " locations");
            drawAllPaths();
            frame.validate();
            frame.repaint();


        } catch (IOException e) {
            log("Cannot load: ");
            log(e.getMessage());
        }
    }

    private LocationDataHolder findLocById(Integer id) {
        return locs.values().stream().filter(l -> l.id == id).findFirst().orElse(null);
    }


    private void welcome() {
        log("Click on the map to add a location.");
        log("Click the above left button to switch modes:");
        log("In edit mode, click any location to edit its properties.");
        log("In join mode, click two locations in succession to add a " +
                "path between them. Doing this where a path exists will remove it.");
        log("In hide mode, click any location to hide it.");
        log("Show/Hide all locations by right clicking the map.");
        log("Right click any location to remove it and all connected paths");
        log("Click T to translate all locations");
        log("Click S to scale all locations");
        log("Click ID to give all locations new IDs, starting from 0");
        log("Click Export to export .locations and .paths files");
        log("Use the filter field to show only Locations with matching code");
    }

    private void log(String s) {
        log.append(s + "\n");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            toggleButtonVis();
            return;
        }
            JButton btn = new JButton("X");
            btn.setVisible(true);

            btn.setBounds(e.getX() - 10, e.getY() - 10 - frame.getInsets().top, 20, 20);
            //btn.setLocation(e.getPoint());

            btn.addActionListener(this);
            btn.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent d) {
                    if(d.getButton() == MouseEvent.BUTTON3) {
                        deleteLoc(btn);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            locs.put(btn, new LocationDataHolder(newId(), e.getX() * factor, (e.getY() - frame.getInsets().top) * factor, floor, "", ""));
            frame.getLayeredPane().add(btn, new Integer(2));

            frame.validate();
            frame.repaint();
            log("Added Location at (" + e.getX() * factor + "," + (e.getY()-frame.getInsets().top) * factor + ")");
            edit(btn);
    }

    private void deleteLoc(JButton btn) {
        frame.getLayeredPane().remove(btn);
        frame.repaint();
        LocationDataHolder l = locs.remove(btn);
        List<PathDataHolder> rem = new ArrayList<>();
        for (PathDataHolder p : paths) if (l==p.a || l==p.b) rem.add(p);
        for (PathDataHolder p : rem) paths.remove(p);

        log("Removed " + l.getString() + (rem.size()>0 ? " and " + rem.size() + " paths" : ""));
        resetBuffer();
        drawAllPaths();
    }

    private void drawAllPaths() {
        for (PathDataHolder p : paths) drawPathToBuf(p);
        showBuf();
    }

    private void toggleButtonVis() {
        buttonVis = !buttonVis;
        for (Component c : frame.getLayeredPane().getComponentsInLayer(2)) {
            c.setVisible(buttonVis);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void edit(JButton btn) {
        EventQueue.invokeLater(() -> locEditor(btn));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // HIDE MODE
        if (mode == Mode.HIDE) ((JButton) e.getSource()).setVisible(false);
        // EDIT MODE
        else if (mode == Mode.EDIT) edit((JButton) e.getSource());
        // JOIN MODE
        else {
            JButton b = (JButton) e.getSource();
            if (selected != null) {
                PathDataHolder c = new PathDataHolder(locs.get(selected), locs.get(b));
                if (selected != b && paths.stream().noneMatch(p -> p.equals(c))) {
                    paths.add(c);
                    log("Added path from " + c.a.getString() + " to " + c.b.getString());
                    drawPathAndShow(c);
                }
                else if (selected != b){
                    paths = paths.stream().filter(p -> !p.equals(c)).collect(Collectors.toList());
                    log("Removed path from " + c.a.getString() + " to " + c.b.getString());
                    resetBuffer();
                    drawAllPaths();
                }
                selected = null;
            }
            else selected = b;
        }
    }

    private void drawPathToBuf(PathDataHolder p) {
        Graphics g = buf.getGraphics();
        g.setColor(Color.RED);
        g.drawLine(p.a.x / factor, p.a.y / factor, p.b.x / factor, p.b.y / factor);
    }

    private void drawPathAndShow(PathDataHolder p) {
        drawPathToBuf(p);
        showBuf();
    }

    private void showBuf() {
        picLabel.setIcon(new ImageIcon(buf));
        frame.repaint();
    }


    private void resetBuffer() {
        buf = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = buf.getGraphics();
        g.drawImage(img, 0,0, null);
    }

    private void locEditor(JButton btn) {
        LocationDataHolder loc = locs.get(btn);
        JFrame editor = new JFrame("Edit " + loc.id);
        editor.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();

        JTextField x = new JTextField(5);
        JTextField y = new JTextField(5);
        x.setText(Integer.toString(loc.x));
        y.setText(Integer.toString(loc.y));

        JTextField code = new JTextField(10);
        code.setText(loc.code);

        JTextField name = new JTextField(25);
        name.setText(loc.name);

//        JTextField floor = new JTextField(15);
//        floor.setText(loc.floor);

        panel.add(new JLabel("X: "));
        panel.add(x);
        panel.add(new JLabel("Y: "));
        panel.add(y);
        panel.add(new JLabel("Code: "));
        panel.add(code);
        panel.add(new JLabel("Name: "));
        panel.add(name);
//        panel.add(new JLabel("Floor: "));
//        panel.add(floor);
        JButton enter = new JButton("Save");
        editor.getRootPane().setDefaultButton(enter);
        enter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LocationDataHolder l = locs.get(btn);
                l.x = Integer.valueOf(x.getText());
                l.y = Integer.valueOf(y.getText());
                l.code = code.getText();
                l.name = name.getText();

                btn.setText(l.code);
                FontMetrics m = btn.getFontMetrics(btn.getFont());
                int sz = m.stringWidth(btn.getText());
                btn.setBounds((l.x / factor) - (sz /2) - 10, (l.y / factor) - 10, sz + 20, 20);
                //locs.put(btn, l);
                log("(" + l.x + "," + l.y + "): " + l.code + " " + l.name);
                frame.repaint();
                editor.dispose();
            }
        });
        editor.setPreferredSize(new Dimension(300,200));
        panel.add(enter);
        editor.add(panel);
        editor.pack();
        editor.setVisible(true);
        code.requestFocus();

    }
}
