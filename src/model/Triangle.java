package model;

import java.awt.Color;

public class Triangle
{
	private Vertex _vertex1;
	private Vertex _vertex2;
	private Vertex _vertex3;
	private Color _color;
	
	public Triangle(Vertex vertex1, Vertex vertex2, Vertex vertex3, Color color)
	{
		_vertex1 = vertex1;
		_vertex2 = vertex2;
		_vertex3 = vertex3;
		_color = color;
	}
	
	public Vertex getVertex1()
	{
		return _vertex1;
	}
	
	public Vertex getVertex2()
	{
		return _vertex2;
	}
	
	public Vertex getVertex3()
	{
		return _vertex3;
	}
	
	public Color getColor()
	{
		return _color;
	}
}
