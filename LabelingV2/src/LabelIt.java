import java.awt.EventQueue;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingWorker;
import javax.swing.border.Border;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.awt.GridBagConstraints;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;



public class LabelIt implements KeyListener {
	
	private static File sourcedir;
	private static File outputdirP;
	private static File outputdirN;
	private static File outputdirT;
	private LinkedList<File> refList;
	private LinkedList<Pair> last3;
	private LinkedList<File> refListTemp;
	private LinkedList<BufferedImage> imgList;
	private LinkedList<JLabel> labellist;
	private LinkedList<JLabel> labellistLast3;
	private int index;
	private int batchsize=25; //Die Größe der Bilderbatches die in den Arbeitsspeicher geladen und dann angezeigt werden
	

	private JPanel cards;
	private JFrame frame;
	private JLayeredPane lPanel;
	private JLayeredPane lPanelCorr;
	private CardLayout cl;
	private boolean normalMode;

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
		last3 = new LinkedList<Pair>();
		
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
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		
		cards = new JPanel();
        cards.setLayout(new CardLayout());
        cards.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        frame.getContentPane().add(cards,gbc_panel);
        
		
		lPanel = new JLayeredPane();
		lPanel.setPreferredSize(frame.getSize());
		
		lPanelCorr = new JLayeredPane();
		lPanelCorr.setPreferredSize(frame.getSize());
		
		  
        cards.add(lPanel, "Normal");
        cards.add(lPanelCorr, "Korrektur");  
        
        cl = (CardLayout) cards.getLayout();
        cl.show(cards, "Normal");
        normalMode=true;
	}
	
	
	public void loadImagePaths(){
		 // array of supported extensions (use a List if you prefer)
	    final String[] EXTENSIONS = new String[]{
	      "JPG",  "jpg" // Nur jpg wird momentan akzeptiert
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
	
	//Warum BufferedImage speichern?! Lieber wie in displayLast3() Methode machen /// Oder als ImageIcon speichern <- auch gut
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
	
	public void displayLast3(){
		lPanelCorr.removeAll();
		lPanelCorr.revalidate();
    	lPanelCorr.repaint();
		labellistLast3 = new LinkedList<JLabel>();
		System.out.println("Die letzen 3 Bilder "+ last3);
	
		for(int i=0;i<last3.size();i++){ 
			labellistLast3.add(new JLabel());
			try{

				labellistLast3.get(i).setSize(frame.getSize());
				BufferedImage img = ImageIO.read(last3.get(i).getFile());
				labellistLast3.get(i).setIcon(new ImageIcon(img));
				lPanelCorr.add(labellistLast3.get(i),null, 0);
				
			}catch (Exception e) {
				System.out.println("Vermutlich nen leerer Imagecontainer DU HUND!");
			}
			
		}
		
		Collections.reverse(labellistLast3);
		
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
        	if(normalMode==true) {
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
            		cards.setBackground(Color.GREEN);
            	}
                break;
        	}else { //Korrekturmodus
        		if(last3.size()>0) {
        			System.out.println("Korrekturmodus");
        			if(!last3.getLast().getMetaValue().equals("n")) {
        				(new ImageTask(2,"n",last3.getLast().getFile())).execute();
        			}
        			
            		lPanelCorr.remove(labellistLast3.removeFirst());
            		last3.removeLast();
            		
            		lPanelCorr.revalidate();
                	lPanelCorr.repaint();
            		
        		}else{
            		System.out.println("Das war das letzte Bild");
            		//displayLastImageMessageOrSth();
            		System.out.println(last3.size());
            		
            		cards.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
            		cl.show(cards, "Normal");
            		normalMode=true;
            	}	
        		break;
        	}
        	
        case KeyEvent.VK_RIGHT:
        	if(normalMode==true) {
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
            		cards.setBackground(Color.GREEN);
            	}	
                break;
        	}else { //Korrekturmodus
        		if(last3.size()>0) {
        			System.out.println("Korrekturmodus");
        			if(!last3.getLast().getMetaValue().equals("p")) {
        				(new ImageTask(2,"p",last3.getLast().getFile())).execute();
        			}
        			
            		lPanelCorr.remove(labellistLast3.removeFirst());
            		last3.removeLast();
            		
            		lPanelCorr.revalidate();
                	lPanelCorr.repaint();
            		
        		}else{
            		System.out.println("Das war das letzte Bild");
            		//displayLastImageMessageOrSth();
            		System.out.println(last3.size());
            		
            		cards.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
            		cl.show(cards, "Normal");
            		normalMode=true;
            	}	
        		break;
        	}
        	
            
        case KeyEvent.VK_SPACE:
        	if(normalMode==true) {
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
            		cards.setBackground(Color.GREEN);
            		System.out.println(labellist.size());
            	}	
            	break;
        	}else { //Korrekturmodus
        		if(last3.size()>0) {
        			System.out.println("Korrekturmodus");
        			if(!last3.getLast().getMetaValue().equals("t")) {
        				(new ImageTask(2,"t",last3.getLast().getFile())).execute();
        			}
        			
            		lPanelCorr.remove(labellistLast3.removeFirst());
            		last3.removeLast();
            		
            		lPanelCorr.revalidate();
                	lPanelCorr.repaint();
            		
        		}else{
            		System.out.println("Das war das letzte Bild");
            		//displayLastImageMessageOrSth();
            		System.out.println(last3.size());
            		
            		cards.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
            		cl.show(cards, "Normal");
            		normalMode=true;
            	}	
        		break;
        	}

        	
        case  KeyEvent.VK_BACK_SPACE:
        	if(normalMode==true) {
        		if(last3.size()!=0) {
            		cards.setBorder(BorderFactory.createLineBorder(Color.RED, 5));
            		cl.show(cards, "Korrektur");
            		displayLast3();
            		normalMode =false;
            	}
            	break;
        	}else {
        		cards.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        		cl.show(cards, "Normal");
        		normalMode=true;
        	}
        	
        	
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
            
            System.out.println("value: " + value);
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
	

	//Threading
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
			
			//Bilder nachladen
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
				//File out = writeMetadata(label, ref); <- Methode für png
				//readMetadata(out); <- Methode für png
				
				File out = writeMeta(ref,label); //<- Methode für jpg
				readMeta(out); //<- Methode für jpg
				
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
	
	public synchronized File writeMeta(File imageDir, String label) { //synchronized ja/nein?
			File out = null;
	        String filePath = null;
	        
	        //Create output path for Images
	        //Split before first ',' to get value p,n or t
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
	        	
	        
	        
		
	        ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
	        ImageReader reader = ImageIO.getImageReader(writer);
	        

	        try {
	        	ImageInputStream input = ImageIO.createImageInputStream(imageDir);
				reader.setInput(input);
				RenderedImage img = reader.read(0);
		        IIOMetadata meta = reader.getImageMetadata(0);
		        input.close();
		        Element tree = (Element) meta.getAsTree("javax_imageio_jpeg_image_1.0");
		        NodeList comNL = tree.getElementsByTagName("com");
		        IIOMetadataNode comNode;
		        if (comNL.getLength() == 0) {
		            comNode = new IIOMetadataNode("com");
		            Node markerSequenceNode = tree.getElementsByTagName("markerSequence").item(0);
		            markerSequenceNode.insertBefore(comNode,markerSequenceNode.getFirstChild());
		        } else {
		            comNode = (IIOMetadataNode) comNL.item(0);
		        }
		        comNode.setUserObject(new String(label).getBytes("ISO-8859-1"));
		        meta.setFromTree("javax_imageio_jpeg_image_1.0", tree);
		        
		        // set JPG params
		        JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
		        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		        param.setCompressionQuality(1);
		        param.setOptimizeHuffmanTables(true);

		        // save the image with new comment inside
		        IIOImage iioimage = new IIOImage(img, null, meta);
		        ImageInputStream output = ImageIO.createImageOutputStream(out);
		        writer.setOutput(output);
		        writer.write(null, iioimage, param);
		        output.close();
		        
		        //Put into last3
		        if(normalMode == true) {
		        	 Pair pair = new Pair(out, label);
		        	 last3.add(pair);
				        if(last3.size()>3) {
				        	last3.removeFirst();
				        }
		        }
		       
		        
		        
		        
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        writer.dispose();
	        reader.dispose();
	        
	        //Delete origin
	        Path path = Paths.get(imageDir.getAbsolutePath());
	        try {
				Files.delete(path);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        return out;
	}
	
	public void readMeta(File file) {
		//inputStream erstellen (siehe png methode), writer hier löschen
		
		 ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
	        ImageReader reader = ImageIO.getImageReader(writer);

	        try {
				reader.setInput(new FileImageInputStream(file));
		        IIOMetadata meta = reader.getImageMetadata(0);
		        Element tree = (Element) meta.getAsTree("javax_imageio_jpeg_image_1.0");
		        IIOMetadataNode comNode = (IIOMetadataNode)tree.getElementsByTagName("com").item(0);
		        NamedNodeMap map = comNode.getAttributes();
		        if (map != null) { // print attribute values
		    		int length = map.getLength();
		    		for (int i = 0; i < length; i++) {
		    			Node attr = map.item(i);
		    			System.out.println(attr.getNodeName() + "=" + attr.getNodeValue());
		    		}
		    	}
		        
	        }catch(Exception e) {
	        	
	        }
	        writer.dispose();
	        reader.dispose();
	}
	
}


