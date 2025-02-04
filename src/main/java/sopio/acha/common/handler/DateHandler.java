package sopio.acha.common.handler;

import static java.time.format.TextStyle.FULL;
import static java.util.Locale.KOREAN;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DateHandler {
	public static String getTodayDate() {
		return LocalDate.now().getDayOfWeek().getDisplayName(FULL, KOREAN);
	}

	public static String getToday() {
		return String.format("%02d.%02d", LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
	}

	public static String getCurrentSemesterYear() {
		int currentYear = LocalDate.now().getYear();
		int currentMonth = LocalDate.now().getMonthValue();
		return (currentMonth < 3) ? String.valueOf(currentYear - 1) : String.valueOf(currentYear);
	}

	public static String getCurrentSemester() {
		int currentMonth = LocalDate.now().getMonthValue();
		return (currentMonth < 3) ? "2" : "1";
	}
}
