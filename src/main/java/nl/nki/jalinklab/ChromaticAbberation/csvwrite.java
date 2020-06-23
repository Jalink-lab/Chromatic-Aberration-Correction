/* 
 * Copyright (C) 2020 Rolf Harkes
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
package nl.nki.jalinklab.ChromaticAbberation;

import java.io.File;
import com.opencsv.CSVWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import org.scijava.log.LogService;

public class csvwrite {
    private File file;
    private String[] header;
    private double[][] data; //[row] [column]
    
    public csvwrite(File name){
        file = name;
    }
    public void setheader(String[] h) {
        header=h;
    }
    public void setdata(double[][] d) {
        data=d;
    }
    public void setdata(String colname, double[] d) {
        //find row in the header
        int col = 0;
        for (int i = 0; i < header.length; i++) {
            if (colname.equals(header[i])) {
                col = i;
            }
        }
        //set column
        for (int i=0; i<data.length;i++){
            data[i][col]=d[i]; //for all rows
        }
    }
    public void writeall(LogService log){
        try {
            file.createNewFile();
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            
            writer.writeNext(header);
            for (int i=0;i<data.length;i++){
                String[] s = new String[data[0].length];
                for (int j=0;j<data[0].length;j++) {
                    s[j] = String.format(Locale.ROOT, "%.4f",data[i][j]);
                }
                writer.writeNext(s,false);
            }
            writer.close();
        } catch (FileNotFoundException ex) {
        	log.error(file + " not found");
        } catch (IOException ex) {
        	log.error("cannot read from " + file);
        }
    }
}