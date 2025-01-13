package sopio.acha.common.handler;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DateHandler {
	public static String getTodayDate() {
		return LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
	}
}
