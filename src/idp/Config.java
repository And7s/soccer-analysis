package idp;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import static idp.idp.*;

/**
 * Created by Andre on 23/04/2016.
 */
public class Config extends JPanel {
    private JComboBox selectList = null, selectMatchList = null;
    public Config() {
        setLayout(new GridLayout(12, 2));

        JSlider slider1 = new JSlider(JSlider.HORIZONTAL,1, 10, 4);//direction , min , max , current

        slider1.setMajorTickSpacing(3);
        slider1.setMinorTickSpacing(1);
        slider1.setOpaque(false);
        slider1.setPaintLabels(true);
        slider1.setPaintTicks(true);
        slider1.setPaintTrack(true);
        slider1.setAutoscrolls(true);
        slider1.addChangeListener(e -> {
            System.out.println(slider1.getValue());
            App.steps_mean = slider1.getValue();
            idp.analyze();
        });
        add(slider1);


        JCheckBox chinButton = new JCheckBox("only active play");
        chinButton.addChangeListener(e -> {
            System.out.println(chinButton.isSelected());
            if (App.only_active != chinButton.isSelected()) {   // only do something if value actually changed
                App.only_active = chinButton.isSelected();
                idp.analyze();
            }
        });
        add(chinButton);

        JCheckBox ignoreKeeper = new JCheckBox("ignopre keeper");
        ignoreKeeper.addChangeListener(e -> {
            System.out.println(ignoreKeeper.isSelected());
            if (App.ignore_keeper != ignoreKeeper.isSelected()) {
                App.ignore_keeper = ignoreKeeper.isSelected();
                idp.analyze();
            }
        });
        add(ignoreKeeper);

        JCheckBox ignoreExchange = new JCheckBox("ignore exchange players");
        ignoreExchange.addChangeListener(e -> {
            System.out.println(ignoreExchange.isSelected());
            if ( App.ignore_exchange != ignoreExchange.isSelected()) {
                App.ignore_exchange = ignoreExchange.isSelected();
                idp.analyze();
            }
        });
        add(ignoreExchange);

        JCheckBox ignoreOfficials = new JCheckBox("ignore officials");
        ignoreOfficials.addChangeListener(e -> {
            System.out.println(ignoreOfficials.isSelected());
            if ( App.ignore_officials != ignoreOfficials.isSelected()) {
                App.ignore_officials = ignoreOfficials.isSelected();
                idp.analyze();
            }
        });
        add(ignoreOfficials);

        JCheckBox noIndividualExport = new JCheckBox("no individual export");
        noIndividualExport.addChangeListener(e -> {
            System.out.println(noIndividualExport.isSelected());
            if ( App.no_individual_export != noIndividualExport.isSelected()) {
                App.no_individual_export = noIndividualExport.isSelected();
                idp.analyze();
            }
        });
        add(noIndividualExport);

        JSlider slider_playback_speed = new JSlider(JSlider.HORIZONTAL,1, 10, 5);//direction , min , max , current

        slider_playback_speed.setMajorTickSpacing(3);
        slider_playback_speed.setMinorTickSpacing(1);
        slider_playback_speed.setOpaque(false);
        slider_playback_speed.setPaintLabels(true);
        slider_playback_speed.setPaintTicks(true);
        slider_playback_speed.setPaintTrack(true);
        slider_playback_speed.setAutoscrolls(true);
        slider_playback_speed.addChangeListener(e -> {
            System.out.println(slider_playback_speed.getValue());
            App.smooth_factor = slider_playback_speed.getValue() * 0.1;
            //idp.redoAnalyze();
            App.vis_speed.repaint();

        });
        add(slider_playback_speed);


        JButton button = new JButton("export loaded");
        button.addActionListener(e -> {
            System.out.println("will export");
            game.exportLoaded();
        });
        add(button);


        JButton buttonAll = new JButton("bactch anlyze all");
        buttonAll.addActionListener(e -> {
            System.out.println("will export all");
            vis_batch.exportAll();
        });
        add(buttonAll);
    }


    public void updateData() {
        // if already existent ,remove
        if (selectList != null) {
            selectList.removeAllItems();
        } else {
            selectList = new JComboBox();
            add(selectList);
        }
        if (selectMatchList != null) {
            selectMatchList.removeAllItems();
        } else {
            selectMatchList = new JComboBox();
            add(selectMatchList);
        }
        String[] selectString = new String[idp.frameSet.length];

        for (int i = 0; i < idp.frameSet.length; i++) {
            Player p = game.getPlayer(idp.frameSet[i].Object);
            String playerName = (p == null) ? "unknown" : p.ShortName;

            selectList.addItem(i + ": " + playerName);
        }
        System.out.println(selectString);

        selectList.setSelectedIndex(0);
        selectList.addActionListener(e -> {
            App.selctedFramesetIdx = selectList.getSelectedIndex();
            App.vis_speed.repaint();
        });

        for (int i = 0; i < idp.game.positions.size(); i++) {
            selectMatchList.addItem(i + ": " + idp.game.matchs.get(i).GameTitle);
        }

        selectMatchList.addActionListener(e -> {
            idp.selectFrameSet(selectMatchList.getSelectedIndex());
            config.updateData();    // so other player can be selected
        });
        selectMatchList.setSelectedIndex(App.selectedMatchIdx);
    }
}
