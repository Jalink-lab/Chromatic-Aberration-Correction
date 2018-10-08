/* 
 * Copyright (C) 2018 Rolf Harkes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Callibrate;

import Classes.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.ImageJ;
import org.json.*;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;
import org.scijava.widget.FileWidget;

/**
 * Fits the affine transform to two csv-files of for example multi-colored beads
 */
@Plugin(type = Command.class, headless = true,
        menuPath = "Plugins>Chromatic Aberation Correction>Callibrate Affine")
public class Chrom_corr_cal implements Command, Previewable {

    @Parameter
    private LogService log;

    @Parameter
    private StatusService statusService;

    @Parameter(label = "Positions 1", description = "csv file 1")
    private File csvfile1;

    @Parameter(label = "Positions 2", description = "csv file 2", style = "open")
    private File csvfile2;
    
    @Parameter (style = FileWidget.DIRECTORY_STYLE, label = "Output folder")
    private File out_folder; 
    
    @Parameter(label = "Wavelength [nm]", description = "Wavelength [nm]", style = "open")
    private Long wavelength;

    public static void main(final String... args) throws Exception {
        // Launch ImageJ as usual.
        final ImageJ ij = net.imagej.Main.launch(args);

        // Launch the command.
        ij.command().run(Chrom_corr_cal.class, true);
    }

    @Override
    public void run() {
        csvread file1 = new csvread(csvfile1);
        csvread file2 = new csvread(csvfile2);
        double[] x1 = file1.getdata("x [nm]");
        double[] y1 = file1.getdata("y [nm]");
        double[] x2 = file2.getdata("x [nm]");
        double[] y2 = file2.getdata("y [nm]");
        AffineTransform atrans = new AffineTransform();
        atrans.loadpositions(x1, y1, x2, y2);
        //Affine = atrans.getAffineS();
        double[][] affine = atrans.getAffine();
        JSONObject JObj = new JSONObject();
        JObj.put("File1",csvfile1.toString());
        JObj.put("File2",csvfile2.toString());
        JObj.put("Wavelength[nm]",wavelength);
        JSONArray JArr = new JSONArray();
        for (int i = 0;i<3;i++){
            for (int j = 0;j<2;j++){
                JArr.put(affine[i][j]);
            }
        }
        JObj.put("Values", JArr);
        
        Path out_file = Paths.get(out_folder.toString(),"AffineTransform"+wavelength.toString()+".json");
        try {
            FileWriter fileWriter = new FileWriter(out_file.toString());
            fileWriter.write(JObj.toString());
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Chrom_corr_cal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void cancel() {
        log.info("Cancelled");
    }

    @Override
    public void preview() {
        log.info("Preview");
    }
}