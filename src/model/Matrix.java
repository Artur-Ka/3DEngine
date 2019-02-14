package model;

public class Matrix
{
	private double[][] _values;
	
	public Matrix(double[][] values)
	{
		_values = values;
	}
	
	public double[][] getValues()
	{
		return _values;
	}
	
	public double[] getRowValues(int row)
	{
		double[] rowVals = new double[_values.length];
		
		for (int i = 0; i < rowVals.length; i++)
		{
			rowVals[i] = _values[i][row];
		}
		
		return rowVals;
	}
	
	public double[] getColValues(int col)
	{
		double[] colVals = new double[_values[0].length];
		
		for (int i = 0; i < colVals.length; i++)
		{
			colVals[i] = _values[col][i];
		}
		
		return colVals;
	}
	
	public double getValue(int row, int col)
	{
		return _values[row][col];
	}
	
	public Matrix multiply(Matrix matrix)
	{
		double[][] temp = new double[_values.length][matrix.getValues()[0].length];
        
        for (int i = 0; i < temp.length; i++)
        {
            for (int j = 0; j < temp[0].length; j++)
            {
                for (int k = 0; k < temp.length; k++)
                {
                    temp[i][j] += getValue(i, k) * matrix.getValue(k, j); 
                }
            }
        }
		
		return new Matrix(temp);
	}
	
	public Vertex transform(Vertex vertex)
	{
		double x = getColValues(0)[0] * vertex.getX() + getColValues(0)[1] * vertex.getY() + getColValues(0)[2] * vertex.getZ();
		double y = getColValues(1)[0] * vertex.getX() + getColValues(1)[1] * vertex.getY() + getColValues(1)[2] * vertex.getZ();
		double z = getColValues(2)[0] * vertex.getX() + getColValues(2)[1] * vertex.getY() + getColValues(2)[2] * vertex.getZ();
		
		return new Vertex(x, y, z);
    }
}
