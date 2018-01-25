import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;

import javax.swing.JLabel;

public class IndexedJLabel extends JLabel implements MouseListener, ImageObserver{
	
	private int index;
	private GUI reference;
	
	public IndexedJLabel(int index, GUI reference){
		super();
		this.index = index;
		this.reference = reference;
		addMouseListener(this); //Weil jedes Label einen eigenen Mouselistener hat kann es sein dass schnelle klicks nicht erkannt werden. Dazu müsste ein dedizierter Mouselistener an alle Labels übergene
	}
	
	public int getIndex(){
		return index;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		reference.handleFocusSwap(index);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
