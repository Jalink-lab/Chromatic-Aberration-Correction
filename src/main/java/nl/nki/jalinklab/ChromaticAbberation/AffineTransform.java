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
package nl.nki.jalinklab.ChromaticAbberation;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
        
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * The affine transform transforms points according to the formula:
 * p2 = A*p1 + B
 * Here p2 is the resulting n x d position matrix, p1 is the original n x d position matrix
 * In 2D space A is a 2x2 matrix and B a 1x2 vector
 * To solve this equation as p2 = Q*p1 we must combine A and B in a 2x3 matrix Q 
 * and add a 1 to p1 to make it n x 3.
 * 
 * @author r.harkes
 */
        
public class AffineTransform {
    private RealMatrix Q; //affine transformation [A;B]
    
    public AffineTransform() {
        Q = new Array2DRowRealMatrix(2,3);//the affine matrix
    }
    
    public void loadpositions(double[] x1, double[] y1,double[] x2, double[] y2) {
        int N = x1.length;
        if (N!= x2.length || N !=y1.length || N!=y2.length){return;}
        RealMatrix X = new Array2DRowRealMatrix(N,3);//the source
        RealMatrix Y = new Array2DRowRealMatrix(N,2);//the result
        //load data
        for (int i = 0;i<N;i++) {
            X.setEntry(i,0,x1[i]);
            X.setEntry(i,1,y1[i]);
            X.setEntry(i,2,1);
            Y.setEntry(i,0,x2[i]);
            Y.setEntry(i,1,y2[i]);
        }
        DecompositionSolver solver = new QRDecomposition(X).getSolver();
        Q = solver.solve(Y);
    }
    
    public double[][] correctpositions(double[] x1, double[] y1){
        if (x1.length != y1.length){return null;}
        RealMatrix X = new Array2DRowRealMatrix(2,y1.length);//the source
        X.setRow(0, x1);
        X.setRow(1, y1);
        RealMatrix A = Q.getSubMatrix(0,1,0,1);
        RealMatrix Y = A.multiply(X);
        double [][] out = new double[2][x1.length];
        out[0] = Y.getRowMatrix(0).scalarAdd(Q.getEntry(0, 2)).getRow(0);
        out[1] = Y.getRowMatrix(1).scalarAdd(Q.getEntry(1, 2)).getRow(0);
        return out; 
    }
    
    public double[][] getAffine(){
        double[][] out = new double[3][2];
        out[0] = Q.getRow(0);
        out[1] = Q.getRow(1);
        out[2] = Q.getRow(2);
        return out;
    }
    
    public String getAffineS() {
        String result = "";
        for (int i=0;i<3;i++){
            for (int j=0;j<2;j++){
                result = result + String.valueOf(Q.getEntry(i, j))+",";
            }
            result = result.substring(0, result.length() - 1)+";"; 
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }
    public void setAffineS(String result){
        String[] parts = result.split("[,|;]"); //split with non-word caracters
        for (int i=0;i<2;i++){
            for (int j=0;j<3;j++){
                Q.setEntry(i,j,Double.valueOf(parts[i*2+j]));
            }
        }
    }
    public void setAffine(double[][] x){
        Q.setColumn(0, x[0]);
        Q.setColumn(1, x[1]);
        Q.setColumn(2, x[2]);
    }
}
