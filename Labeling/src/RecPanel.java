import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class RecPanel extends JPanel {
	
		private static RecPanel instance = null;
		private int width = 200;
		private int height = 200;
		
		
	   @Override
	   protected void paintComponent(Graphics g) {
	      super.paintComponent(g);
	      // draw the rectangle here
	      LineBorder border = new LineBorder(Color.RED, 5);
	      this.setBorder(border);
	   }

	   @Override
	   public Dimension getPreferredSize() {
	      // so that our GUI is big enough
	      return new Dimension(width, height);
	   }
	   
	   private RecPanel(){
		   //
	   }
	   
	   public static RecPanel getInstance(){
		   if(instance==null){
			   instance= new RecPanel();
			   return instance;
		   }else{
			   return null;
		   }
	   }
	   
	   public int getWidth(){
		   return width;
	   }
	   
	   public int getHeight(){
		   return height;
	   }

	   public void setWidth(int width){
		   this.width = width;
	   }
	   
	   public void setHeight(int height){
		   this.height = height;
	   }
}
