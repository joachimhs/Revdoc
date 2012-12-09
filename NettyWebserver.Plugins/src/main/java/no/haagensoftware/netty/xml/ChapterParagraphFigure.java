package no.haagensoftware.netty.xml;

public class ChapterParagraphFigure {
	private String id;
	private String title;
	private String link;
	
	public ChapterParagraphFigure() {
		// TODO Auto-generated constructor stub
	}
	
	public ChapterParagraphFigure(String id, String title, String link) {
		super();
		this.id = id;
		this.title = title;
		this.link = link;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
