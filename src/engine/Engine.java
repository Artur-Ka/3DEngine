package engine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import model.Matrix;
import model.Triangle;
import model.Vertex;

public class Engine
{
	public static final int WIDTH = 500;
	public static final int HEIGHT = 500;
	
	private static List<Triangle> _tris = new ArrayList<>();
	
	@SuppressWarnings("serial")
	public static void main(String[] args)
	{
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		JFrame frame = new JFrame();
        
		frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation((int)(sSize.getWidth() / 2 - (WIDTH / 2)), (int)(sSize.getHeight() / 2 - (HEIGHT / 2)));
        
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
 
        // slider to control horizontal rotation
        JSlider headingSlider = new JSlider(0, 360, 180);
        pane.add(headingSlider, BorderLayout.SOUTH);
 
        // slider to control vertical rotation
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);
        
        generateTriangles();
        
        // panel to display render results
        JPanel renderPanel = new JPanel()
        {
            public void paintComponent(Graphics g)
            {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // rendering magic will happen here
//                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);
                
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++)
                {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }
                
                double heading = Math.toRadians(headingSlider.getValue());
                double[][] headingValues = {{Math.cos(heading), 0, Math.sin(heading)},
                        					{0, 1, 0}, 
                        					{-Math.sin(heading), 0, Math.cos(heading)}}; 
                Matrix headingTransform = new Matrix(headingValues); 
                
                double pitch = Math.toRadians(pitchSlider.getValue()); 
                double[][] pitchValues = {{1, 0, 0},
                						{0, Math.cos(pitch), Math.sin(pitch)},
                						{0, -Math.sin(pitch), Math.cos(pitch)}}; 
                Matrix pitchTransform = new Matrix(pitchValues);
                
                Matrix transform = headingTransform.multiply(pitchTransform);
                
                for (Triangle t : _tris)
                {
                	Vertex v1 = transform.transform(t.getVertex1());
                    Vertex v2 = transform.transform(t.getVertex2());
                    Vertex v3 = transform.transform(t.getVertex3());
                    
                    // since we are not using Graphics2D anymore, we have to do translation manually
                    v1.setX(v1.getX() + (getWidth() / 2));
                    v1.setY(v1.getY() + (getHeight() / 2));
                    v2.setX(v2.getX() + (getWidth() / 2));
                    v2.setY(v2.getY() + (getHeight() / 2));
                    v3.setX(v3.getX() + (getWidth() / 2));
                    v3.setY(v3.getY() + (getHeight() / 2));
                 
                    // compute rectangular bounds for triangle
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));
                    
                    Vertex ab = new Vertex(v2.getX() - v1.getX(), v2.getY() - v1.getY(), v2.getZ() - v1.getZ());
                    Vertex ac = new Vertex(v3.getX() - v1.getX(), v3.getY() - v1.getY(), v3.getZ() - v1.getZ());
                    Vertex norm = new Vertex(ab.getY() * ac.getZ() - ab.getZ() * ac.getY(), ab.getZ() * ac.getX() - ab.getX() * ac.getZ(), ab.getX() * ac.getY() - ab.getY() * ac.getX());
                    double normalLength = Math.sqrt(norm.getX() * norm.getX() + norm.getY() * norm.getY() + norm.getZ() * norm.getZ());
                    norm.setX(norm.getX() / normalLength);
                    norm.setY(norm.getY() / normalLength);
                    norm.setZ(norm.getZ() / normalLength);
                    double angleCos = Math.abs(norm.getZ());
                    
                    double triangleArea = (v1.getY() - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - v1.getX());
                    for (int y = minY; y <= maxY; y++)
                    {
                        for (int x = minX; x <= maxX; x++)
                        {
                        	double b1 = ((y - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - x)) / triangleArea;
                            double b2 = ((y - v1.getY()) * (v3.getX() - v1.getX()) + (v3.getY() - v1.getY()) * (v1.getX() - x)) / triangleArea;
                            double b3 = ((y - v2.getY()) * (v1.getX() - v2.getX()) + (v1.getY() - v2.getY()) * (v2.getX() - x)) / triangleArea;
                            
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1)
                            {
                                double depth = b1 * v1.getZ() + b2 * v2.getZ() + b3 * v3.getZ();
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth)
                                {
                                    img.setRGB(x, y, getShade(t.getColor(), angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }
                
                g2.drawImage(img, 0, 0, null);
            }
        };
        
        headingSlider.addChangeListener(e -> renderPanel.repaint()); 
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        
        pane.add(renderPanel, BorderLayout.CENTER);
        frame.setVisible(true);
	}
	
	private static void generateTriangles()
	{
		_tris.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));

		_tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));

		_tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));

		_tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));
		
//		for (int i = 0; i < 4; i++)
//		{
//            _tris = inflate(_tris);
//        }
	}
	
	private static Color getShade(Color color, double shade)
	{
	    double redLinear = Math.pow(color.getRed(), 2.4) * shade;
	    double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
	    double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;
	 
	    int red = (int) Math.pow(redLinear, 1/2.4);
	    int green = (int) Math.pow(greenLinear, 1/2.4);
	    int blue = (int) Math.pow(blueLinear, 1/2.4);
	 
	    return new Color(red, green, blue);
	}
	
	/** TODO: Включается для округления модели */
	@SuppressWarnings("unused")
	private static List<Triangle> inflate(List<Triangle> tris)
	{
	    List<Triangle> result = new ArrayList<>();
	    for (Triangle t : tris)
	    {
	        Vertex m1 = new Vertex((t.getVertex1().getX() + t.getVertex2().getX())/2, (t.getVertex1().getY() + t.getVertex2().getY())/2, (t.getVertex1().getZ() + t.getVertex2().getZ())/2);
	        Vertex m2 = new Vertex((t.getVertex2().getX() + t.getVertex3().getX())/2, (t.getVertex2().getY() + t.getVertex3().getY())/2, (t.getVertex2().getZ() + t.getVertex3().getZ())/2);
	        Vertex m3 = new Vertex((t.getVertex1().getX() + t.getVertex3().getX())/2, (t.getVertex1().getY() + t.getVertex3().getY())/2, (t.getVertex1().getZ() + t.getVertex3().getZ())/2);
	        result.add(new Triangle(t.getVertex1(), m1, m3, t.getColor()));
	        result.add(new Triangle(t.getVertex2(), m1, m2, t.getColor()));
	        result.add(new Triangle(t.getVertex3(), m2, m3, t.getColor()));
	        result.add(new Triangle(m1, m2, m3, t.getColor()));
	    }
	    for (Triangle t : result)
	    {
	        for (Vertex v : new Vertex[] { t.getVertex1(), t.getVertex2(), t.getVertex3() })
	        {
	            double l = Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ()) / Math.sqrt(30000);
	            v.setX(v.getX() / l);
	            v.setY(v.getY() / l);
	            v.setZ(v.getZ() / l);
	        }
	    }
	    
	    return result;
	}
}
