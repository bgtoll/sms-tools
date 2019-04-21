package tolya.sms.calc;

/**
 * <h1>SMS Char</h1>
 * Represents a single character in the message. Used only when 
 * {@link tolya.sms.calc.AdvancedSmsCalculator AdvancedSmsCalculator} is called.
 * 
 * @author d-tol
 */
public class SmsChar {
	/** Regular 7bit character, present in GSM charset */
	public static final byte TYPE_GSM = 0;
	/** Escape character 0x1B, required for: |, ^, €, {, }, [, ] or ~ */
	public static final byte TYPE_ESC = 1;
	/** Character not present in GSM charset, forces to use Unicode encoding */
	public static final byte TYPE_UNICODE = 2;
	/** Character present in GSM charset, encoded as Unicode character */
	public static final byte TYPE_UNICODE_FORCED = 3;
	/** User Defined Header (UDH) of the message. Required for multipart sms */
	public static final byte TYPE_HEADER = 4;
	
	/** Several default HTML colors */
	public static final String[] HTML_COLORS =
		{"#EEEEE", "#FFEEAA", "#B1DEFF", "#8EBA33", "#698924"};
	
	private char ch;
	private byte type;
	
	public SmsChar(char ch, byte type) {
		this.ch = ch;
		this.type = type;
	}
	
	/** Return the character */
	public char getCh() {
		return ch;
	}
	
	/** Return character's type */
	public byte getType() {
		return type;
	}
	
	/** Set different character type. */
	public void setType(byte type) {
		this.type = type;
	}
	
	/** Return a HTML color, depending on the character's type 8 */
	public String getColor() {
		return HTML_COLORS[type];
	}
	
//	@Override
//	public String toString() {
//		return (ch == '\u0000' ? String.format("%x", (int) ch) : ch) + " -> " + type + " -> " + color;
//	}
	
}
