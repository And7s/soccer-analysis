package idp;

import idp.FrameSet;
import idp.Position;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import static idp.idp.game;
import static idp.idp.match;
import static idp.idp.onGameLoaded;

import idp.InvalidPositionDataSet;


/**
 * Created by Andre on 16/04/2016.
 */
public class myPanel extends JPanel {
    int c_paint = 0;
    int c_lines = 0;
    Vector<String> files;
    public myPanel() {


        Thread th = new Thread() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    repaint();
                    try {
                        sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        JButton button = new JButton("open, fst position, then match");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("clicked open");
                openDialog();


            }
        });
        add(button);
        th.start();
    }

    public void paintComponent(Graphics g) {
       // System.out.println("print "+c_paint);
        c_paint++;
        super.paintComponent(g);
        Font f = new Font("Segoe UI Light", Font.PLAIN, 50);

        Graphics2D g2d = (Graphics2D) g;

        Dimension size = getSize();
        int width = (int) size.getWidth(),
            height =  (int) size.getHeight();

        g.setFont(f);
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawString("Soccer Analysis", width / 2 - 160, height / 3 );
        g.fillRect(0, height / 2 - 10, c_paint, 20);
    }

    public void openDialog() {
        if (true) {        // DEBUG
            File position_file = null, match_file = null;
            JFileChooser fc = new JFileChooser("select a position set please");
            fc.setCurrentDirectory(new java.io.File(".")); // start at application current directory
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                position_file = fc.getSelectedFile();

                int ret = Position.checkType(position_file.getAbsolutePath());
                if (ret != 1 && ret != 0) {
                    JOptionPane.showMessageDialog(null, "not a valid position set");
                    return;
                }
            }

            fc = new JFileChooser("select a match set please");
            fc.setCurrentDirectory(new java.io.File(position_file.getPath())); // start at application current directory
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                match_file = fc.getSelectedFile();
                int ret = Position.checkType(match_file.getAbsolutePath());
                if (ret != 3) {
                    JOptionPane.showMessageDialog(null, "not a valid match set " + ret);
                    return;
                }
            }
            if (position_file != null && match_file != null) {
                JOptionPane.showMessageDialog(null, "you selected " + position_file.getName());
                addMatch(position_file, match_file);
            } else {
                JOptionPane.showMessageDialog(null, "not both files were selected");
            }
        } else {

            addMatch(new File("D:\\dfl\\" + "DFL-MAT-00031J" + "_ObservedPositionalData.xml"),
                new File("D:\\dfl\\" + "DFL-MAT-00031J" + "_MatchInformation.xml"));
        }

    }

    public void addMatch(File position_file, File match_file) {
        Position pos;
        Match match;
        try {
            int ret = Position.checkType(position_file.getAbsolutePath());
            FrameSet[] frame_set = Position.readPosition(position_file.getAbsolutePath(), ret);
            pos = new Position(frame_set);

            match = new Match(match_file.getAbsolutePath());

            game.addPosition(pos);
            game.addMatch(match);

            onGameLoaded();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "error laoding");
        }

    }

    public void readDir(File folder) {

        files = new Vector<String>(10);

        listFilesForFolder(folder, folder.getAbsolutePath()+"\\");
        System.out.println(files.size());


        for (Enumeration it = files.elements(); it.hasMoreElements(); ) {

            String s = (String) it.nextElement();

            int ret = Position.checkType(s);//
            if (ret >= 0) {
                System.out.println(s + " = " +ret);
                try {
                    FrameSet[] frame_set = Position.readPosition(s, ret);
                    Position pos = new Position(frame_set);
                    game.addPosition(pos);
                } catch (InvalidPositionDataSet e) {
                    System.out.println("could not load file " + s);
                }

            }

           /*

            if (ret == 1 || ret == 0) {
                try {
                    FrameSet[] frame_set = Position_new.readPosition(s, ret);
                    Position_new pos = new Position_new(frame_set);
                    game.addPosition(pos);


                } catch (InvalidPositionDataSet e) {
                    System.out.println("could not load file " + s);
                }

            }*/
        }
    }

    public void listFilesForFolder(final File folder, String path) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, path + fileEntry.getName() + "/" );
            } else {
                if (fileEntry.getName().endsWith(".xml")) {
                    files.add(path + fileEntry.getName());
                }
            }
        }
    }
}
