package framework;


public class Response {

	public String action;
	public String redirect;
	public String template;
	public boolean partial;
	public String text;
	public boolean async;
	public String contentType = "text/html; charset=utf-8";
	public byte[] bytes;
	public Object singleObject; 
	
}
