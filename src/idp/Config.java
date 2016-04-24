package idp;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Created by Andre on 23/04/2016.
 */
public class Config extends JPanel {

    public Config() {
        setLayout(new GridLayout(10,2));

        JSlider slider1 = new JSlider(JSlider.HORIZONTAL,1,10,4);//direction , min , max , current

        slider1.setMajorTickSpacing(3);
        slider1.setMinorTickSpacing(1);
        slider1.setOpaque(false);
        slider1.setPaintLabels(true);
        slider1.setPaintTicks(true);
        slider1.setPaintTrack(true);
        slider1.setAutoscrolls(true);
        slider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(slider1.getValue());
                App.steps_mean = slider1.getValue();
                idp.analyze();
                App.vis_mean.updateData(idp.dat, idp.frameSet);
            }
        });
        add(slider1);


        JCheckBox chinButton = new JCheckBox("only active play");
        chinButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(chinButton.isSelected());
                if (App.only_active != chinButton.isSelected()) {   // only do something if value actually changed
                    App.only_active = chinButton.isSelected();
                    idp.analyze();
                    App.vis_mean.updateData(idp.dat, idp.frameSet);
                }
            }
        });
        add(chinButton);

        JCheckBox ignoreKeeper = new JCheckBox("ignopre keeper");
        ignoreKeeper.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(ignoreKeeper.isSelected());
                if (App.ignore_keeper != ignoreKeeper.isSelected()) {
                    App.ignore_keeper = ignoreKeeper.isSelected();
                    idp.analyze();
                    App.vis_mean.updateData(idp.dat, idp.frameSet);
                }
            }
        });
        add(ignoreKeeper);

        JCheckBox ignoreExchange = new JCheckBox("ignore exchange players");
        ignoreExchange.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(ignoreExchange.isSelected());
                if ( App.ignore_exchange != ignoreExchange.isSelected()) {
                    App.ignore_exchange = ignoreExchange.isSelected();
                    idp.analyze();
                    App.vis_mean.updateData(idp.dat, idp.frameSet);
                }
            }
        });
        add(ignoreExchange);

    }
}
