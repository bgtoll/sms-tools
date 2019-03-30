package tolya.sms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SmsCalculator {
	private static final byte GSM = 0;
	private static final byte UNICODE = 1;
	private static final short[][] SMS_LENGTHS = {{160, 153},{70, 67}}; // {{gsm-single, gsm-multi}, {utf-single, utf-multi}}
	
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
	
	public SmsCalculator() {
		this.maxSmsParts = 10;
	}
	
	public SmsCalculator(String text) {
		this();
		reloadText(text);
	}
	
	public SmsCalculator(String text, int maxSmsParts) {
		this(text);
		this.maxSmsParts = maxSmsParts;
	}
	
	public void reloadText(String text) {
		this.text = text;
		calculateUsedChars();
		calculateSmsParts();
	}
	
	public String getText() {
		return text;
	}
	
	public int getUsedChars() {
		return usedChars;
	}
	
	public int getSmsParts() {
		return smsParts;
	}
	
	public String getEncoding() {
		return charset == GSM ? "GSM-03-38" : "UTF-8";
	}
	
	public int getCharsLeftAbsolute() {
		return getMaxLengthAbsolute() - usedChars;
	}
	
	public int getMaxLengthAbsolute() {
		return SMS_LENGTHS[charset][1] * maxSmsParts;
	}
	
	public int getCharsLeftCurrentPart() {
		return getMaxLengthCurrentPart() - usedChars;
	}
	
	public int getMaxLengthCurrentPart() {
		return smsParts > 1 ? SMS_LENGTHS[charset][1] * smsParts : SMS_LENGTHS[charset][0];
	}
	
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
					charset = UNICODE;
					unicode = gsm + gsmExt + 1;
				}
			} else {
				unicode++;
			}
		}
		
		if (charset == UNICODE) {
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
