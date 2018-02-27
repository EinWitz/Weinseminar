import java.awt.EventQueue;

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
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

import org.w3c.dom.NodeList;


import java.awt.GridBagConstraints;
import java.awt.Dimension;



public class LabelIt implements KeyListener {
	
	private static File sourcedir;
	private static File outputdirP;
	private static File outputdirN;
	private static File outputdirT;
	private LinkedList<File> refList;
	private LinkedList<File> refListTemp;
	private LinkedList<BufferedImage> imgList;
	private LinkedList<JLabel> labellist;
	private int index;
	private int batchsize=25; //Die Größe der Bilderbatches die in den Arbeitsspeicher geladen und dann angezeigt werden
	
	//public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	
	private JFrame frame;
	private JLayeredPane lPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LabelIt window = new LabelIt();
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
	public LabelIt() {
		initialize();
		loadImagePaths();
		loadImages();
		displayImages();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		//initialize some variables
		index =0;
		labellist = new LinkedList<JLabel>();
		imgList = new LinkedList<BufferedImage>();
		refListTemp = new LinkedList<File>();
		
		//File Chooser
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
		
		//make output-directories
		makeOutputDirectories();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		frame.addKeyListener(this);
		
		lPanel = new JLayeredPane();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		frame.getContentPane().add(lPanel, gbc_panel);
		lPanel.setPreferredSize(frame.getSize());
	}
	
	public void loadImagePaths(){
		 // array of supported extensions (use a List if you prefer)
	    final String[] EXTENSIONS = new String[]{
	        "png","jpg" // Nur png wird momentan akzeptiert
	    };
	    
	    //Liste zum Speichern der Bildpfade der sourcedir
	    refList = new LinkedList<File>();
	    
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
           refList.add(f);
	    }
	    
	    System.out.println(refList);
	}
	
	public void loadImages(){
		int initIndex = index;
		
		 for (int i=initIndex; i<initIndex+batchsize && i<refList.size();i++) {
	            try {
	                imgList.add(ImageIO.read(refList.get(index)));
	                refListTemp.add(refList.get(index));
	                System.out.println("image: " + refList.get(index).getName());
	                index++;
	            } catch (final IOException e) {
	            	System.out.println("Das waren alle Elemente.");
	                // handle errors here
	            }
	    }
		
	}
	
	public void displayImages(){
		
		for(int i=0;i<imgList.size();i++){ 
			labellist.add(new JLabel());
			try{
				labellist.get(i).setSize(frame.getSize());
				labellist.get(i).setIcon(new ImageIcon(imgList.get(i)));
				lPanel.add(labellist.get(i),null,-1);
				//labellist.get(i).setVisible(true);
			}catch (Exception e) {
				System.out.println("Vermutlich nen leerer Imagecontainer DU HUND!");
			}
			
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent k) {
		// TODO Auto-generated method stub
		int keyCode = k.getKeyCode();
		
		switch( keyCode ) { 
        case KeyEvent.VK_LEFT:
        	if(labellist.size()>0 && refListTemp.size()!=0){ //Warum reflist!=0?!!!!! nochmal anschauen  p.s. ich glaube es funktioniert
        		lPanel.remove(labellist.removeFirst());
        		System.out.println(refListTemp.getFirst());
        		//negativsample -> Metadaten eintragen
        		(new ImageTask(0,"n",refListTemp.getFirst())).execute();
        		refListTemp.removeFirst();
        		imgList.getFirst().flush();
        		imgList.removeFirst();
            	lPanel.revalidate();
            	lPanel.repaint();
            	System.out.println("Negative.");
            	
        	}else{
        		System.out.println("Das war das letzte Bild");
        		System.out.println(labellist.size());
        	}
            break;
        case KeyEvent.VK_RIGHT:
        	if(labellist.size()>0  && refListTemp.size()!=0){ //Warum reflist!=0?!!!!! nochmal anschauen p.s. ich glaube es funktioniert
        		
        		lPanel.remove(labellist.removeFirst());
        		System.out.println(refListTemp.getFirst());
            	//positivsample -> Metadaten eintragen
        		(new ImageTask(0,"p",refListTemp.getFirst())).execute();				
        		refListTemp.removeFirst();
        		imgList.getFirst().flush();
            	lPanel.revalidate();
            	lPanel.repaint();
            	System.out.println("Positive.");
        	}else{
        		System.out.println("Das war das letzte Bild");
        		System.out.println(labellist.size());
        	}	
            break;
        case KeyEvent.VK_SPACE:

        	if(labellist.size()>0  && refListTemp.size()!=0){ //Warum reflist!=0?!!!!! nochmal anschauen p.s. ich glaube es funktioniert
        		
        		lPanel.remove(labellist.removeFirst());
        		System.out.println(refListTemp.getFirst());
            	//trash -> Metadaten eintragen
        		(new ImageTask(0,"t",refListTemp.getFirst())).execute();
        		refListTemp.removeFirst();
        		imgList.getFirst().flush();
            	lPanel.revalidate();
            	lPanel.repaint();
            	System.out.println("Trash.");
        	}else{
        		System.out.println("Das war das letzte Bild");
        		//displayLastImageMessageOrSth();
        		System.out.println(labellist.size());
        	}	
        	break;
        default:
        	break;
     }
		
		//Ein Batch Bilder nachladen bei weniger als 8 Bildern in der Anzeige
		if(labellist.size()<8){
			System.gc();
			(new ImageTask(1,null,null)).execute();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	//Metadaten-Funktionalitäten
	public File writeMetadata(String label , File imageDir) throws IOException {
        File in = imageDir;
        File out = null;
        String filePath = null;
        
        //Create output path for Images
        switch(label) {
        case "p":
        	filePath = outputdirP.getAbsolutePath()+File.separatorChar+imageDir.getName();
        	out = new File(filePath);
        	break;
        case "n":
        	filePath = outputdirN.getAbsolutePath()+File.separatorChar+imageDir.getName();
        	out = new File(filePath);
        	break;
        case "t":
        	filePath = outputdirT.getAbsolutePath()+File.separatorChar+imageDir.getName();
        	out = new File(filePath);
        	break;
        default:
        	System.out.println("Fehler im verschieben der Bilder (writeMetadata()).");
        	break;
        }
        

        System.out.println("Change Metadata of Image in: " + in.getAbsolutePath());

        try (ImageInputStream input = ImageIO.createImageInputStream(in);
             ImageOutputStream output = ImageIO.createImageOutputStream(out)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next(); // TODO: Validate that there are readers

            reader.setInput(input);
            IIOImage image = reader.readAll(0, null);

            addTextEntry(image.getMetadata(), "label", label); //Hier lässt sich metadateneintrag machen
            addTextEntry(image.getMetadata(), "Traktor", "test"); 
            
            ImageWriter writer = ImageIO.getImageWriter(reader); // TODO: Validate that there are writers
            writer.setOutput(output);
            writer.write(image);
        }
        
        //Delete original image

        return out;
    }
 
 	
	public void readMetadata(File imageDir) throws IOException{
		File in = imageDir;
	 
		try (ImageInputStream input = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next(); // TODO: Validate that there are readers

            reader.setInput(input);
            String value = getTextEntry(reader.getImageMetadata(0), "label");
            String value2 = getTextEntry(reader.getImageMetadata(0), "Traktor");
            
            System.out.println("value: " + value+"\tvalue2: " + value2);
        }
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
	
	public void makeOutputDirectories() {
		//Ordner für positive labels
		String filePath = sourcedir.getAbsolutePath()+File.separatorChar+"positive";
		outputdirP = new File(filePath);
		
		//Ordner für negative labels
		String filePath2 = sourcedir.getAbsolutePath()+File.separatorChar+"negative";
		outputdirN = new File(filePath2);
		
		//Ordner für den trash
		String filePath3 = sourcedir.getAbsolutePath()+File.separatorChar+"trash";
		outputdirT = new File(filePath3);
		
		System.out.println(outputdirP);
		//Verzeichnisse erstellen
		if(!outputdirP.exists()) {
			outputdirP.mkdirs();
		}
		
		if(!outputdirN.exists()) {
			outputdirN.mkdirs();
		}
		
		if(!outputdirT.exists()) {
			outputdirT.mkdirs();
		}
	
	}
	

	//add Threading here
	//parameter und Funktion noch ergänzen
	class ImageTask extends SwingWorker<List<BufferedImage>, BufferedImage>{

		private int action;
		private String label;
		private File ref;
		
		public ImageTask(int action, String label,File ref) {
			this.action = action;
			if(action!=1) {
				this.label = label;
				this.ref = ref;
			}
		}
		
		@Override
		protected List<BufferedImage> doInBackground() throws Exception {
			
			if(action==1) {
				int initIndex = index;
				
				 for (int i=initIndex; i<initIndex+batchsize;i++) {
			            try {
			                imgList.add(ImageIO.read(refList.get(index)));
			                publish(imgList.getLast());
			                refListTemp.add(refList.get(index));
			                System.out.println("image: " + refList.get(index).getName());
			                index++;
			            } catch (final IOException e) {
			            	System.out.println("Das waren alle Elemente.");
			                // handle errors here
			            }	
			    }
				// TODO Auto-generated method stub
				return imgList;
			}else {
				File out = writeMetadata(label, ref);
				System.out.println(out);
				readMetadata(out);
				return null;
			}
			
		}
		
		 @Override
	     protected void process(List<BufferedImage> chunks) {
			 if(action==1) {
				 for(BufferedImage image : chunks){
					 try{
						 JLabel jl = new JLabel();
						 labellist.add(jl);
							jl.setSize(frame.getSize());
							jl.setIcon(new ImageIcon(image));
							lPanel.add(jl,null,-1);
						}catch (Exception e) {
							e.getMessage();
						} 
				 } 
			 }	 
			
	     }
		
	}
	
}


