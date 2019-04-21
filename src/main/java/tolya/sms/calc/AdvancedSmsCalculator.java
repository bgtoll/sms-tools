package tolya.sms.calc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <h1>Advanced SMS Calculator</h1>
 * Used to show the user each character and what encoding is used for it. It's helpful to 
 * understand why given SMS is charged as two or more - usually because of a single quote or special symbol. 
 * This class extends {@link tolya.sms.calc.SmsCalculator SmsCalculator}, so the constructors are the same.
 * 
 * @author Anatoliy
 */
public class AdvancedSmsCalculator extends SmsCalculator {
	private boolean analyzed;
	private List<SmsChar> cache;
	
	public AdvancedSmsCalculator() {
		super();
	}

	public AdvancedSmsCalculator(String text, int maxSmsParts) {
		super(text, maxSmsParts);
	}

	public AdvancedSmsCalculator(String text) {
		super(text);
	}
	
	/** Return list of {@link tolya.sms.calc.SmsChar SmsChar}. The result is cached and the text is
	 *  analyzed only the first time or when it's changed. */
	public List<SmsChar> getAnalyzedChars() {
		if (!analyzed && getText() != null) {
			char[] chars = getText().toCharArray();
			cache = new ArrayList<>(chars.length);
			
			boolean containsUnicode = false;
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				if (GSM_7BIT.contains(ch)) {
					cache.add(new SmsChar(ch, SmsChar.TYPE_GSM));
				} else if (GSM_7BIT_EXT.contains(ch)) {
					cache.add(new SmsChar('\u001b', SmsChar.TYPE_ESC)); // escape character
					cache.add(new SmsChar(ch, SmsChar.TYPE_GSM));
				} else {
					cache.add(new SmsChar(ch, SmsChar.TYPE_UNICODE));
					containsUnicode = true;
				}
			}
			
			if (containsUnicode) {
				Iterator<SmsChar> it = cache.iterator();
				while (it.hasNext()) {
					SmsChar smsChar = it.next();
					switch (smsChar.getType()) {
					case SmsChar.TYPE_GSM:
						smsChar.setType(SmsChar.TYPE_UNICODE_FORCED);
						break;
					case SmsChar.TYPE_ESC:
						it.remove();
						break;
					}
				}
			}
			
			if (getSmsParts() > 1) {
				for (int p = 0; p < getSmsParts(); p++) {
					addHeader(p * getCharsInSms() + p * 6);
				}
			}
			
			analyzed = true;
		}
		
		return cache;
	}
	
//	public void printAnalyzedChars() {
//		for (SmsChar smsChar : getAnalyzedChars()) {
//			System.out.println(smsChar);
//		}
//		
//		System.out.println();
//	}
	
	@Override
	public void reloadText(String text) {
		super.reloadText(text);
		analyzed = false;
	}
	
	private void addHeader(int p) {
		// In reverse to be able to use the position without increment
		cache.add(p, new SmsChar('\u0001', SmsChar.TYPE_HEADER));
		cache.add(p, new SmsChar('\u0002', SmsChar.TYPE_HEADER));
		cache.add(p, new SmsChar('\u0012', SmsChar.TYPE_HEADER));
		cache.add(p, new SmsChar('\u0003', SmsChar.TYPE_HEADER));
		cache.add(p, new SmsChar('\u0000', SmsChar.TYPE_HEADER));
		cache.add(p, new SmsChar('\u0005', SmsChar.TYPE_HEADER));
	}
	
}
