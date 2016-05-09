package idp;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.FileOutputStream;


import java.io.*;
import java.util.ArrayList;

import static idp.idp.match;
import static idp.idp.position;

/**
 * Created by Andre on 02/05/2016.
 */
// a Game collects all information about a game, that is framesets, events and matchinformation
public class Game {

    ArrayList<Position_new> positions = new ArrayList<Position_new>();
    ArrayList<Match> matchs = new ArrayList<Match>();
    ArrayList<CellStyle> styles = new ArrayList<CellStyle>();
    HSSFWorkbook wb;


    public Game() {

    }

    public void addPosition(Position_new pos) {
        positions.add(pos);
    }

    public void addMatch(Match match) {
        matchs.add(match);
    }


    public void writeCSV() {
        if (matchs.size() != positions.size()) return;

        try {
            wb = new HSSFWorkbook();
            HSSFPalette palette = wb.getCustomPalette();

            for (int i = 0; i < 10; i++) {
                short idx = (short) (i + 40);
                System.out.println("modify color " +idx);
                palette.setColorAtIndex(idx, (byte) Math.min(Math.abs(255-76*i),255), (byte) Math.min(3*255-76*i, 255), (byte) Math.max(255-76*i, 0));

                CellStyle style = wb.createCellStyle();
                style.setFillForegroundColor(idx);
                style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                styles.add(style);
            }

            for (int k = 0; k < matchs.size(); k++) {
                int row_c = 0;
                Match match = matchs.get(k);
                Position_new position = positions.get(k);

                Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(match.GameTitle));

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
                    "framesmissing", "first half", "club", "energy total"};

                row1 = sheet.createRow(row_c++);
                for (int i = 0; i < str_cols.length; i++) {
                    c = row1.createCell(i);
                    c.setCellValue(str_cols[i]);

                    c = row1.createCell(i + str_cols.length + 1);
                    c.setCellValue(str_cols[i]);
                }

                // create color palette


                FrameSet[] frameSet = position.frameSet;
                outer:
                for (int i = 0; i < frameSet.length; i++) {

                    FrameSet fs = frameSet[i];
                    if (fs.isBall) continue;    // dont plot the ball

                    FrameSet left = null, right = null;
                    if (fs.firstHalf) {
                        left = fs;
                    } else {
                        right = fs;
                    }
                    for (int j = 0; j < frameSet.length; j++) {
                        if (fs.Object.equals(frameSet[j].Object) && i != j) {
                            if (j < i) continue outer;  // this was already ahdnled in an earleir iteration
                            if (frameSet[j].firstHalf) {
                                left = frameSet[j];
                            } else {
                                right = frameSet[j];
                            }
                        }
                    }
                    row1 = sheet.createRow(row_c++);
                    if (left != null) {
                        writeFrameSet(row1, match, left, 0);
                    }
                    if (right != null) {
                        writeFrameSet(row1, match, right, 1 + str_cols.length);
                    }
                }
            }

            FileOutputStream fileOut = new FileOutputStream("analyze.xls");
            wb.write(fileOut);
            fileOut.close();


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR writing file" );

        }
    }

    public void writeFrameSet(Row row1, Match match, FrameSet fs, int col_c) {

        Cell c = row1.createCell(col_c++);
        c.setCellValue(match.getPlayer(fs.Object).ShortName);
        c = row1.createCell(col_c++);
        c.setCellValue(fs.getVar(VAR.SPRINT));
        c = row1.createCell(col_c++);
        double speed = fs.getSpeed() / fs.getCount() / 3.6;
        int color = (int)Math.min(Math.max((speed * 3.3), 0), 9);
        c.setCellValue(speed);
        c.setCellStyle(styles.get(color));

        for (int i = 0; i < 3; i++) {
            c = row1.createCell(col_c++);
            speed = fs.getSpeed(15 * i, 15 * i + 15, 1) / fs.getCount(15 * i, 15 * i + 15, 1) / 3.6;
            c.setCellValue(speed);
            color = (int)Math.min(Math.max((speed * 3.3), 0), 9);
            c.setCellStyle(styles.get(color));
        }


        c = row1.createCell(col_c++);
        c.setCellValue(fs.getCount(-1) / 25.0 / 60);
        c = row1.createCell(col_c++);
        c.setCellValue(fs.getCount(0) / 25.0 / 60);
        c = row1.createCell(col_c++);
        c.setCellValue(fs.getCount(1) / 25.0 / 60);

        c = row1.createCell(col_c++);
        c.setCellValue(fs.getSpeedMin(0,15, 1) / 3.6  + " - " + fs.getSpeedMax(0,15, 1) / 3.6);
        c = row1.createCell(col_c++);
        c.setCellValue(fs.getSpeedMin(15,30, 1) / 3.6  + " - " + fs.getSpeedMax(15,30, 1) / 3.6);
        c = row1.createCell(col_c++);
        c.setCellValue(fs.getSpeedMin(30,45, 1) / 3.6  + " - " + fs.getSpeedMax(30,45, 1) / 3.6);

        c = row1.createCell(col_c++);
        c.setCellValue(fs.frames_missing);
        c = row1.createCell(col_c++);
        c.setCellValue(fs.firstHalf);
        c = row1.createCell(col_c++);
        c.setCellValue(match.getTeam(fs.Club).name);

        c = row1.createCell(col_c++);
        c.setCellValue(fs.getEnergy() / fs.getCount());
    }
}
