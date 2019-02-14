package model;

public class Vertex
{
	private double _x;
	private double _y;
	private double _z;
	
	public Vertex(double x, double y, double z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public double getX()
	{
		return _x;
	}
	
	public double getY()
	{
		return _y;
	}
	
	public double getZ()
	{
		return _z;
	}
	
	public void setX(double x)
	{
		_x = x;
	}
	
	public void setY(double y)
	{
		_y = y;
	}
	
	public void setZ(double z)
	{
		_z = z;
	}
}
