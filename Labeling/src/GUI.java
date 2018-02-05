import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.w3c.dom.NodeList;

import com.ibm.icu.text.SimpleDateFormat;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.ScrollPaneConstants;

public class GUI implements MouseListener,KeyListener{

	//Variablen für den Dimensionsabfrage-Dialog
    private int xValue = 200;
    private int yValue = 200;
	
	
	private JFrame frame;
	private JLayeredPane layeredPane;
	private JScrollPane jsp;
	private JPanel imgPanel;
	private JLabel lblNewLabel;
	private RecPanel recp = RecPanel.getInstance();
	private BufferedImage bigImage = null;
	
	private static File sourcedir;
	private static File destinationdir;
	
	private static int indexOfImages=0;
	private static int lastImageFocus=0;
	
	private LinkedList<JLabel> labellist;
	private LinkedList<BufferedImage> imgList;
	
	int vertScrollbarValue=0;
	int horScrollbarValue=0;
	
	private Dimension newDim; 

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		int taskbarheight = Toolkit.getDefaultToolkit().getScreenSize().height 
			    - GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height; //das hier liefert nicht wirklich das richtige ergebnis :/
		
		newDim = new Dimension(dim.width-200,dim.height-taskbarheight-30); //deshalb hier nochmal die -30
		
		frame = new JFrame("LabelIt");
		frame.getContentPane().setPreferredSize(dim);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		frame.addKeyListener(this);
		
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(newDim);
		layeredPane.setVisible(true);
		layeredPane.setLayout(null);
		
		
		lblNewLabel = new JLabel();
		
		//Zwei Filechooser. Source und Destination
		
	    //Change look and feel towards windows file chooser wenn zeit da ist
	    JFileChooser chooser = new JFileChooser();
	    chooser.setDialogTitle("Where are the source pictures?");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setApproveButtonText("Übernehmen");
		chooser.setApproveButtonToolTipText("Dieses Verzeichnis wählen");
		chooser.setPreferredSize(new Dimension(720,460));
		
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
		      System.out.println("getCurrentDirectory(): " 
		         +  chooser.getCurrentDirectory());
		      System.out.println("getSelectedFile() : " 
		         +  chooser.getSelectedFile());
		      
		      sourcedir = chooser.getSelectedFile(); 
		}else {
			System.out.println("No Selection ");
			System.exit(0);
		}
		
		//Change look and feel towards windows file chooser wenn zeit da ist
	    JFileChooser chooser2 = new JFileChooser();
	    chooser2.setDialogTitle("Where to put the labeled images?");
		chooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser2.setAcceptAllFileFilterUsed(false);
		chooser2.setApproveButtonText("Übernehmen");
		chooser2.setApproveButtonToolTipText("Dieses Verzeichnis wählen");
		chooser2.setPreferredSize(new Dimension(720,460));
		chooser2.setCurrentDirectory(sourcedir);
		
		if (chooser2.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
		      System.out.println("getSelectedFile() : " 
		         +  chooser2.getSelectedFile());
		      
		      File destinationdir_temp = chooser2.getSelectedFile(); 
		      modifyDestinationDir(destinationdir_temp);
		}else {
			System.out.println("No Selection ");
			System.exit(0);
		}
		singleDialogInformation();
	
		
		//Bilder aus gewählter Quelle werden geladen
		loadImages();
		
		bigImage = imgList.getFirst();
		
		lblNewLabel.setIcon(new ImageIcon(bigImage));
		jsp = new JScrollPane(lblNewLabel);
		//jsp.setPreferredSize(newDim);
		
		jsp.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
		jsp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 20));
		jsp.getVerticalScrollBar().setUnitIncrement(16);
		jsp.getHorizontalScrollBar().setUnitIncrement(16);
		
		layeredPane.add(jsp,new Integer(-1),0);
		jsp.setSize(newDim);
		frame.getContentPane().add(layeredPane);
		
		imgPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) imgPanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		springLayout.putConstraint(SpringLayout.NORTH, imgPanel, 0, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, imgPanel, 0, SpringLayout.EAST, layeredPane);
		springLayout.putConstraint(SpringLayout.SOUTH, imgPanel, 0, SpringLayout.SOUTH, layeredPane);
		springLayout.putConstraint(SpringLayout.EAST, imgPanel, 0, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(imgPanel);
	
		jsp.addMouseListener(this);
		
		frame.pack();
		frame.setVisible(true);
	    
		
		addImagesToJPanel();
		DragListener drag = new DragListener();
		recp.addMouseListener( drag );
		recp.addMouseMotionListener( drag );
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
				
		if(recp!=null){
			Point p = e.getPoint();
			int z = (int)p.getX();
			int z2 = (int)p.getY();
			z = z-recp.getWidth()/2;
			z2 = z2-recp.getHeight()/2;
		
			recp.setLayout(null);
			recp.setSize(recp.getPreferredSize());
			recp.setOpaque(false);
			recp.setLocation(z,z2);
			
			
				
			layeredPane.add(recp, new Integer(0),0);
		} 
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void centerOnScreen(final Component c, final boolean absolute) {
	    final int width = c.getWidth();
	    final int height = c.getHeight();
	    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (screenSize.width / 2) - (width / 2);
	    int y = (screenSize.height / 2) - (height / 2);
	    if (!absolute) {
	        x /= 2;
	        y /= 2;
	    }
	    c.setLocation(x, y);
	}
	
	
	 public void writeMetadata(String label , File imageDir) throws IOException {
	        File in = imageDir;
	        File out = in;

	        System.out.println("Change Metadata of Image in: " + in.getAbsolutePath());

	        try (ImageInputStream input = ImageIO.createImageInputStream(in);
	             ImageOutputStream output = ImageIO.createImageOutputStream(out)) {

	            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
	            ImageReader reader = readers.next(); // TODO: Validate that there are readers

	            reader.setInput(input);
	            IIOImage image = reader.readAll(0, null);

	            addTextEntry(image.getMetadata(), "label", label); //Hier lässt sich metadateneintrag machen (0 oder 1)

	            ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
	            writer.setOutput(output);
	            writer.write(image);
	        }

	    }
	 
	 	
	 public void readMetadata(File imageDir) throws IOException{
		 File in = imageDir;
	     File out = in;
		 
		 try (ImageInputStream input = ImageIO.createImageInputStream(out)) {
	            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
	            ImageReader reader = readers.next(); // TODO: Validate that there are readers

	            reader.setInput(input);
	            String value = getTextEntry(reader.getImageMetadata(0), "label");

	            System.out.println("value: " + value);
	        }
	 }

	 
	 private static File generateOutputDir(){
		 //for png
		 String dir = destinationdir.getAbsolutePath();
		 dir = dir+"\\"+indexOfImages+".png";
		 System.out.println("Output directory: "+dir);
		 
		 File outputdir = new File(dir);
		 //outputdir.mkdirs();
		 
		 return outputdir;
	 }
	 
	 private static void modifyDestinationDir(File destinationdir_temp){
		 String dir = destinationdir_temp.getAbsolutePath();
		 String timestamp = new SimpleDateFormat("dd.MM.yyyy HH-mm-ss").format(new Date());
		 dir = dir+"\\"+timestamp;
		 System.out.println("Modified directory: "+dir);
		 
		 destinationdir = new File(dir);
	 }

	    
	 private static void addTextEntry(final IIOMetadata metadata, final String key, final String value) throws IIOInvalidTreeException {
	        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
	        textEntry.setAttribute("keyword", key);
	        textEntry.setAttribute("value", value);

	        IIOMetadataNode text = new IIOMetadataNode("Text");
	        text.appendChild(textEntry);

	        IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
	        root.appendChild(text);

	        metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
	    
	 }

	    
	 private static String getTextEntry(final IIOMetadata metadata, final String key) {
	        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
	        NodeList entries = root.getElementsByTagName("TextEntry");

	        for (int i = 0; i < entries.getLength(); i++) {
	            IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
	            if (node.getAttribute("keyword").equals(key)) {
	                return node.getAttribute("value");
	            }
	        }

	        return null;
	    
	 }

	@Override
	public void keyPressed(KeyEvent k) {
		// TODO Auto-generated method stub

		
		
		
	}

	@Override
	public void keyReleased(KeyEvent k) {
		// TODO Auto-generated method stub
		char kchar = k.getKeyChar();
		String labelValue = String.valueOf(kchar);
		int keyCode = k.getKeyCode();
		
		switch( keyCode ) { 
        case KeyEvent.VK_UP:
            if(lastImageFocus>0){
            	handleFocusSwap(lastImageFocus-1);
            }
            break;
        case KeyEvent.VK_DOWN:
        	 if(lastImageFocus<(imgList.size()-1)){
             	handleFocusSwap(lastImageFocus+1);
             }
            break;
        default:
        	break;
     }
		
		if(!labelValue.equals("0") && !labelValue.equals("1")){
			System.out.println("Für das labeling sind nur 0 und 1 erlaubt");
		}else{
			if(!destinationdir.exists()){
				System.out.println("Destinationdir wurde noch nicht erstellt");
				destinationdir.mkdirs();
			}
			File outputdir = generateOutputDir();
			BufferedImage subImage=null;
			horScrollbarValue = jsp.getHorizontalScrollBar().getModel().getValue();
			vertScrollbarValue = jsp.getVerticalScrollBar().getModel().getValue();
			
			try {
				subImage = bigImage.getSubimage(recp.getBounds().x+horScrollbarValue, recp.getBounds().y+vertScrollbarValue, recp.getBounds().width, recp.getBounds().height); //Position des Rechtecks bestimmen (abhängig von der Position der Scollbar)
			} catch (RasterFormatException u) {
				System.out.println("Bildausschnitt außerhalb des Rasters dawg.");
			}
			
			if(subImage!=null){
				try {
					
					ImageIO.write(subImage, "png", outputdir);
					indexOfImages++;
				
				} catch (IOException f) {
					System.out.println("Fehler beim Speichern des subImages dawg.");
				}
				
				try {
					writeMetadata(labelValue, outputdir);
					readMetadata(outputdir);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}else{
				System.out.println("Es wurde kein subImage erstellt.");
			}
			
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	//diese Methode lädt die Bilder aus der sourcedir und speichert sie in einer linkedList
	public void loadImages(){
		 // array of supported extensions (use a List if you prefer)
	    final String[] EXTENSIONS = new String[]{
	        "png" // Nur png wird momentan akzeptiert
	    };
	    
	    //Liste zum Speichern der Bilder der sourcedir
	    imgList = new LinkedList<BufferedImage>();
	    
	    // filter to identify images based on their extensions
	    final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

	        @Override
	        public boolean accept(final File dir, final String name) {
	            for (final String ext : EXTENSIONS) {
	                if (name.endsWith("." + ext)) {
	                    return (true);
	                }
	            }
	            return (false);
	        }
	    };
	    
	    for (final File f : sourcedir.listFiles(IMAGE_FILTER)) {
            try {
                imgList.add(ImageIO.read(f));
                System.out.println("image: " + f.getName());
                
            } catch (final IOException e) {
                // handle errors here
            }
        }
	    
	    
	    
	}
	
	//Hier werden die Bilder der sourcedir in ein JPanel in der GUI gepackt
	public void addImagesToJPanel(){
		
		labellist = new LinkedList<JLabel>();
		int imageHeight=0;
		
		//Panel mit Gridlayout um die bilder untereinander einzufügen
		GridLayout gl_panel = new GridLayout(0, 1);
		gl_panel.setVgap(-2);
		JPanel panel = new JPanel( gl_panel ); 					//Dieses Layout füllt seine fläche anscheinend immer nur mit gleichgroßen components (bei gleichen Formaten der Bilder kein Problem, sonst hässlich)
		
		
		//Hier werden alle Bilder der sourcedir der Seitenansicht hinzugefügt
		//Labels im panel "panel" indizieren damit über mouselistener ein click registriert und mit dem index der imgList-Liste verknüpft werden kann
		for(int i=0;i<imgList.size();i++){
			labellist.add(new IndexedJLabel(i,this));
			try{
				Image scaledImage = imgList.get(i).getScaledInstance(imgPanel.getWidth()-20,-1,Image.SCALE_FAST);
				labellist.get(i).setIcon(new ImageIcon(scaledImage));
				labellist.get(i).setPreferredSize(new Dimension(imgPanel.getWidth()-20,scaledImage.getHeight(labellist.get(i))));
				if(i==0){
					labellist.get(i).setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
				}
				labellist.get(i).setVisible(true);
				panel.add(labellist.get(i));
				imageHeight += scaledImage.getHeight(labellist.get(i));
			}catch (Exception e) {
				System.out.println("Vermutlich nen leerer Imagecontainer DU HUND!");
			}
			
		}
		panel.setPreferredSize(new Dimension(imgPanel.getWidth()-20,imageHeight));  //das hier funktioniert noch nicht ganz (siehe Problem wegen des Layouts)
		
		
		//Panel (mit Bildern) zum Scrollpane hinzufügen und dieses zum imgPanel hinzufügen
		JScrollPane jsp2 = new JScrollPane( panel );
		jsp2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jsp2.setPreferredSize(new Dimension(imgPanel.getWidth(),imgPanel.getHeight()));
		jsp2.getVerticalScrollBar().setUnitIncrement(16);
		jsp2.getHorizontalScrollBar().setUnitIncrement(16);
		jsp2.setVisible(true);
		imgPanel.add(jsp2);
		
	}
	
	public void handleFocusSwap(int focusRequestIndex){
		
		labellist.get(lastImageFocus).setBorder(BorderFactory.createEmptyBorder());
		
		labellist.get(focusRequestIndex).setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
		lastImageFocus = focusRequestIndex;
		
		BufferedImage buff = imgList.get(focusRequestIndex);
		lblNewLabel.setIcon(new ImageIcon(buff));
		bigImage = buff;
	}
	
	 public void singleDialogInformation() {
		 
		 	JPanel pane2 = new JPanel();
	        pane2.setLayout(new GridLayout(1,3));
	        

	        JTextField xDim = new JTextField(5);
	        xDim.addAncestorListener( new RequestFocusListener() ); //Bringt den Fokus auf das erste Textfeld
	        JTextField yDim = new JTextField(5);
	        
	        //Font wird festgelegt
	        Font font = new Font("Arial", Font.BOLD, 20);
	        xDim.setFont(font);
	        yDim.setFont(font);

	        pane2.add(xDim);

	        JLabel label = new JLabel("X",SwingConstants.CENTER);
	        label.setFont(font);
	        pane2.add(label);
	        pane2.add(yDim);
	        
	        int option = JOptionPane.showConfirmDialog(null, pane2,"Größe der Bildausschnitte (px)", JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
	        
	        if (option == JOptionPane.OK_OPTION) {

	            String xInput = xDim.getText();
	            String yInput = yDim.getText();

	            try {
	                xValue = Integer.parseInt(xInput);
	                yValue = Integer.parseInt(yInput);
	            } catch (NumberFormatException nfe) {
	                nfe.printStackTrace();
	            }

	            
	            recp.setWidth(xValue);
	            recp.setHeight(yValue);
	        }else{
	        	System.out.println("No Selection");
				System.exit(0);
	        }
	 }
}
