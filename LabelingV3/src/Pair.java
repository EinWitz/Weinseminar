import java.io.File;

public class Pair {
	private File file;
	private String metaValue;
	
	public Pair(File file, String metaValue) {
		this.file = file;
		this.metaValue = metaValue;
	}
	
	public File getFile() {
		return file;
	}
	public String getMetaValue() {
		return metaValue;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	public void setMetaValue(String metaValue) {
		this.metaValue = metaValue;
	}
}
