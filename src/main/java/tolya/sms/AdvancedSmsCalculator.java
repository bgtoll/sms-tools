package tolya.sms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdvancedSmsCalculator extends SmsCalculator {
	private boolean cached;
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
	
	public List<SmsChar> getCharMap() {
		if (!cached && getText() != null) {
			cached = true;

			char[] chars = getText().toCharArray();
			cache = new ArrayList<>();
			
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
		}
		
		return cache;
	}
	
	public void printCharMap() {
		for (SmsChar smsChar : getCharMap()) {
			System.out.println(smsChar);
		}
		
		System.out.println();
	}
	
	@Override
	public void reloadText(String text) {
		super.reloadText(text);
		cached = false;
	}

}
