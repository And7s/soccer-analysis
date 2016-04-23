package idp;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by Andre on 23/04/2016.
 */
public class Config extends JPanel {

    public Config() {


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


        JCheckBox chinButton = new JCheckBox("Chin");
        chinButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(chinButton.isSelected());
                App.only_active = chinButton.isSelected();
                idp.analyze();
                App.vis_mean.updateData(idp.dat, idp.frameSet);
            }
        });


        add(chinButton);
    }
}
