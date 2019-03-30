package tolya.sms;

public class SmsChar {
	public static final byte TYPE_GSM = 0;
	public static final byte TYPE_ESC = 1;
	public static final byte TYPE_UNICODE = 2;
	public static final byte TYPE_UNICODE_FORCED = 3;
	public static final byte TYPE_HEADER = 4;
	
	public static final String[] HTML_COLORS =
		{"#EEEEE", "#FFEEAA", "#B1DEFF", "#8EBA33", "#698924"};
	
	private char ch;
	private byte type;
	private String color;
	
	public SmsChar(char ch, byte type) {
		this.ch = ch;
		this.type = type;
		this.color = HTML_COLORS[type];
	}
	
	public char getCh() {
		return ch;
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
		this.color = HTML_COLORS[type];
	}
	
	public String getColor() {
		return color;
	}
	
//	@Override
//	public String toString() {
//		return (ch == '\u0000' ? String.format("%x", (int) ch) : ch) + " -> " + type + " -> " + color;
//	}
	
}
