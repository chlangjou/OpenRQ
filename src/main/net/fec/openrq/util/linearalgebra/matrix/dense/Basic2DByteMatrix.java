/*
 * Copyright 2014 Jose Lopes
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.matrix.dense;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.dense.BasicByteVector;


public class Basic2DByteMatrix extends AbstractBasicByteMatrix implements DenseByteMatrix {

    private static final long serialVersionUID = 4071505L;

    private byte self[][];


    public Basic2DByteMatrix() {

        this(0, 0);
    }

    public Basic2DByteMatrix(ByteMatrix matrix) {

        this(ByteMatrices.asMatrixSource(matrix));
    }

    public Basic2DByteMatrix(MatrixSource source) {

        this(source.rows(), source.columns());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                self[i][j] = source.get(i, j);
            }
        }
    }

    public Basic2DByteMatrix(int rows, int columns) {

        this(new byte[rows][columns]);
    }

    public Basic2DByteMatrix(int rows, int columns, byte array[]) {

        this(rows, columns);

        // TODO:
        // We suppose that 'array.length = rows * columns' for now.
        // Probably, we should check this explicitly.

        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * columns, self[i], 0, columns);
        }
    }

    public Basic2DByteMatrix(byte array[][]) {

        super(LinearAlgebra.BASIC2D_FACTORY, array.length, array.length == 0 ? 0 : array[0].length);
        this.self = array;
    }

    @Override
    public byte safeGet(int i, int j) {

        return self[i][j];
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        self[i][j] = value;
    }

    @Override
    public void swapRows(int i, int j) {

        checkRowBounds(i);
        checkRowBounds(j);

        if (i != j) {
            byte tmp[] = self[i];
            self[i] = self[j];
            self[j] = tmp;
        }
    }

    @Override
    public void swapRows(int i, int j, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkRowBounds(j);
        checkColumnRangeBounds(fromColumn, toColumn);

        if (i != j) {
            final int length = toColumn - fromColumn;
            byte[] tmp = new byte[length];

            System.arraycopy(self[i], fromColumn, tmp, 0, length);
            System.arraycopy(self[j], fromColumn, self[i], fromColumn, length);
            System.arraycopy(tmp, 0, self[j], fromColumn, length);
        }
    }

    @Override
    public void swapColumns(int i, int j) {

        checkColumnBounds(i);
        checkColumnBounds(j);

        if (i != j) {
            for (int ii = 0; ii < rows; ii++) {
                byte tmp = self[ii][i];
                self[ii][i] = self[ii][j];
                self[ii][j] = tmp;
            }
        }
    }

    @Override
    public void swapColumns(int i, int j, int fromRow, int toRow) {

        checkColumnBounds(i);
        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);

        if (i != j) {
            for (int ii = fromRow; ii < toRow; ii++) {
                byte tmp = self[ii][i];
                self[ii][i] = self[ii][j];
                self[ii][j] = tmp;
            }
        }
    }

    @Override
    public ByteVector getRow(int i) {

        checkRowBounds(i);

        byte result[] = new byte[columns];
        System.arraycopy(self[i], 0, result, 0, columns);

        return new BasicByteVector(result);
    }

    @Override
    public ByteVector getRow(int i, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        final int length = toColumn - fromColumn;
        byte[] result = new byte[length];
        System.arraycopy(self[i], fromColumn, result, 0, length);

        return new BasicByteVector(result);
    }

    @Override
    public ByteMatrix copy() {

        return new Basic2DByteMatrix(toArray());
    }

    @Override
    public ByteMatrix resize(int rows, int columns) {

        ensureDimensionsAreCorrect(rows, columns);

        if (this.rows == rows && this.columns == columns) {
            return copy();
        }

        byte $self[][] = new byte[rows][columns];

        for (int i = 0; i < Math.min(this.rows, rows); i++) {
            System.arraycopy(self[i], 0, $self[i], 0,
                Math.min(this.columns, columns));
        }

        return new Basic2DByteMatrix($self);
    }

    @Override
    public byte[][] toArray() {

        byte result[][] = new byte[rows][columns];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(self[i], 0, result[i], 0, columns);
        }

        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(rows);
        out.writeInt(columns);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                out.writeByte(self[i][j]);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {

        rows = in.readInt();
        columns = in.readInt();

        self = new byte[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                self[i][j] = in.readByte();
            }
        }
    }
}
