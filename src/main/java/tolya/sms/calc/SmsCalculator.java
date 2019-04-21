package tolya.sms.calc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <h1>SMS Length Calculator</h1>
 * Every SMS by default is encoded in 7-bit alphabet, called GSM-03-38. There are several symbols 
 * ("|", "^", "€", "{", "}", "[", "]" and "~" that need to be escaped, so they use two chars ("places"). 
 * If the GSM encoding is used, the length of the first SMS is 160 symbols, but if the text is longer 
 * and there're more than one SMS part, the max length becomes 153 symbols.<br>
 * There's a possibility to use unicode symbols in the message, but in this case, max length becomes 
 * 70 and 67 symbols for the single an multi-sms respectively. If unicode is used, the special symbols 
 * from above are not escaped, so they take one char ("place").
 * 
 * @author Anatoliy D.
 */
public class SmsCalculator {
	private static final byte GSM = 0;
	private static final byte UTF = 1;
	private static final short[][] SMS_LENGTHS = {{160, 153}, {70, 67}}; // {{gsm-single, gsm-multi}, {utf-single, utf-multi}}
	
	private String text;
	private byte charset;
	private int usedChars;
	private int maxSmsParts;
	private int smsParts;
	
	protected static final Set<Character> GSM_7BIT = new HashSet<>(Arrays.asList(new Character[] {
		'@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
		'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', '\u001b', 'Æ', 'æ', 'ß', 'É',
		' ', '!', '\"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
		'¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
		'¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
		'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'}
	));
		
	protected static final Set<Character> GSM_7BIT_EXT = new HashSet<>(Arrays.asList(
		new Character[] {'\f', '^', '{', '}', '\\', '[', '~', ']', '|', '€'}
	));
	
	/** Default max sms parts: 10. SMS Text is empty, must be set later. */
	public SmsCalculator() {
		this.maxSmsParts = 10;
	}
	
	/** Default max sms parts: 10 */
	public SmsCalculator(String text) {
		this();
		reloadText(text);
	}
	
	public SmsCalculator(String text, int maxSmsParts) {
		this(text);
		this.maxSmsParts = maxSmsParts;
	}
	
	/** Set a new SMS text, this triggers recounting characters, SMS parts and detecting encoding changes */
	public void reloadText(String text) {
		this.text = text;
		calculateUsedChars();
		calculateSmsParts();
	}
	
	/** Return the text of the SMS */
	public String getText() {
		return text;
	}
	
	/** Return the count of the characters. This depends on the encoding and if there's a characters, that need to be escaped. */
	public int getUsedChars() {
		return usedChars;
	}
	
	/** Return how many sms parts (messages) have to be used to send the text */
	public int getSmsParts() {
		return smsParts;
	}
	
	/** Return encoding, used to store/send the SMS text. Possible values: GSM-03-38 or UTF. */
	public String getEncoding() {
		return charset == GSM ? "GSM-03-38" : "UTF";
	}
	
	/** Return true if unicode is used to store/send the text */
	public boolean isUTF() {
		return charset == UTF;
	}
	
	/** Return how many characters are left before reaching the SMS parts limit. This depends on the encoding,
	 *  used to store/send the SMS. Default SMS parts: 10*/
	public int getCharsLeftAbsolute() {
		return getMaxLengthAbsolute() - usedChars;
	}
	
	/** Return how many characters can be stored/send before reaching the SMS parts limit. This depends on the encoding,
	 *  used to store/send the SMS. Default SMS parts: 10*/
	public int getMaxLengthAbsolute() {
		return SMS_LENGTHS[charset][1] * maxSmsParts;
	}
	
	/** Return how many characters are left before reaching the end of the current SMS part. This depends on the encoding. */
	public int getCharsLeftCurrentPart() {
		return getMaxLengthCurrentPart() - usedChars;
	}
	
	/** Return how many characters can be stored without opening a new SMS part. This depends on
	 *  the encoding and if there's one or more SMS parts. */
	public int getMaxLengthCurrentPart() {
		return smsParts > 1 ? SMS_LENGTHS[charset][1] * smsParts : SMS_LENGTHS[charset][0];
	}
	
	/** Return how many characters can be stored/send in the current SMS part. This depends on the encoding. */
	public int getCharsInSms() {
		return smsParts > 1 ? SMS_LENGTHS[charset][1] : SMS_LENGTHS[charset][0];
	}
	
	/** Return true if the SMS can be sent within the SMS parts, defined by the constructor. Default SMS parts: 10 */
	public boolean isInRange() {
		return smsParts <= maxSmsParts;
	}
	
	@Override
	public String toString() {
		return
			"used chars: " + getUsedChars() +
			"; chars left/max @ current part: " + getCharsLeftCurrentPart() + "/" + getMaxLengthCurrentPart() +
			"; chars left/max absolute: " + getCharsLeftAbsolute() + "/" + getMaxLengthAbsolute() +
			"; SMS parts: " + getSmsParts() +
			"; encoding: " + getEncoding();
	}
	
	private void calculateUsedChars() {
		charset = GSM;
		if (text == null || "".equals(text)) {
			usedChars = 0;
			return;
		}
		
		int gsm = 0, gsmExt = 0, unicode = 0;
		for (Character ch : text.toCharArray()) {
			if (charset == GSM) {
				if (GSM_7BIT.contains(ch)) {
					gsm++;
				} else if(GSM_7BIT_EXT.contains(ch)) {
					gsmExt++;
				} else {
					charset = UTF;
					unicode = gsm + gsmExt + 1;
				}
			} else {
				unicode++;
			}
		}
		
		if (charset == UTF) {
			usedChars = unicode;
		} else {
			usedChars = gsm + gsmExt * 2;
		}
	}
	
	private void calculateSmsParts() {
		if (usedChars == 0) {
			smsParts = 0;
		} else if (usedChars <= SMS_LENGTHS[charset][0]) {
			smsParts = 1;
		} else {
			smsParts = 2;
			while (usedChars > smsParts * SMS_LENGTHS[charset][1]) {
				smsParts++;
			}
		}
	}

}
