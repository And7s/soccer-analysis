package idp;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.FileOutputStream;


import java.io.*;

import static idp.idp.position;

/**
 * Created by Andre on 02/05/2016.
 */
// a Game collects all information about a game, that is framesets, events and matchinformation
public class Game {

    Position_new position;
    public Match match;
    public Game() {

    }


    public void writeCSV() {
        if (match == null) return;  // dont knwo about the order, the seperate files are read
        if (position == null) return;


        try {
            int row_c = 0;

            HSSFWorkbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet();

            // head

            Row row1 = sheet.createRow(row_c++);
            Cell c = row1.createCell(0);
            c.setCellValue("MatchId");
            c = row1.createCell(1);
            c.setCellValue(match.MatchId);

            row1 = sheet.createRow(row_c++);
            c = row1.createCell(0);
            c.setCellValue("GameTitle");
            c = row1.createCell(1);
            c.setCellValue(match.GameTitle);

            row1 = sheet.createRow(row_c++);
            c = row1.createCell(0);
            c.setCellValue("KickoffTime");
            c = row1.createCell(1);
            c.setCellValue(match.KickoffTime);

            row1 = sheet.createRow(row_c++);
            c = row1.createCell(0);
            c.setCellValue("Competition");
            c = row1.createCell(1);
            c.setCellValue(match.Competition);




            String[] str_cols = {"Frameset", "Sprint Count",
                "Mean vel total", "Mean vel-15", "Mean vel-30", "Mean vel-45",
                "in total game", "in paused game", "in active game",
                "speed minmax -15", "speed minmax -30", "speed minmax -45",
                "framesmissing", "first half", "club"};

            row1 = sheet.createRow(row_c++);
            for (int i = 0; i < str_cols.length; i++) {
                c = row1.createCell(i);
                c.setCellValue(str_cols[i]);
            }


            FrameSet[] frameSet = position.frameSet;
            for (int i = 0; i < frameSet.length; i++) {

                FrameSet fs = frameSet[i];
                if (fs.isBall) continue;    // dont plot the ball

                row1 = sheet.createRow(row_c++);

                c = row1.createCell(0);
                c.setCellValue(match.getPlayer(fs.Object).ShortName);
                c = row1.createCell(1);
                c.setCellValue(fs.getSprintCount());
                c = row1.createCell(2);
                c.setCellValue(fs.getSpeed() / fs.getCount());

                c = row1.createCell(3);
                c.setCellValue(fs.getSpeed(0,15) / fs.getCount(0,15));
                c = row1.createCell(4);
                c.setCellValue(fs.getSpeed(15,30) / fs.getCount(15,30));
                c = row1.createCell(5);
                c.setCellValue(fs.getSpeed(30,45) / fs.getCount(30,45));

                c = row1.createCell(6);
                c.setCellValue(fs.getCount(-1) / 25.0 / 60);
                c = row1.createCell(7);
                c.setCellValue(fs.getCount(0) / 25.0 / 60);
                c = row1.createCell(8);
                c.setCellValue(fs.getCount(1) / 25.0 / 60);

                c = row1.createCell(9);
                c.setCellValue(fs.getSpeedMin(0,15)  + " - " + fs.getSpeedMax(0,15));
                c = row1.createCell(10);
                c.setCellValue(fs.getSpeedMin(15,30)  + " - " + fs.getSpeedMax(15,30));
                c = row1.createCell(11);
                c.setCellValue(fs.getSpeedMin(15,30)  + " - " + fs.getSpeedMax(15,30));

                c = row1.createCell(12);
                c.setCellValue(fs.frames_missing);
                c = row1.createCell(13);
                c.setCellValue(fs.firstHalf);
                c = row1.createCell(14);
                c.setCellValue(match.getTeam(fs.Club).name);
            }

            FileOutputStream fileOut = new FileOutputStream(match.MatchId + ".xls");
            wb.write(fileOut);
            fileOut.close();


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR writing file" );

        }
    }
}
