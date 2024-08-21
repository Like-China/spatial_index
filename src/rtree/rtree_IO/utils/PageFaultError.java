package rtree.rtree_IO.utils;

public class PageFaultError extends Error {
	
	public PageFaultError(){
		super();
	}
	
	public PageFaultError(String s){
		super(s);
	}
}
