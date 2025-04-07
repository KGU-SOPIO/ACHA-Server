package sopio.acha.common.handler;

import static java.time.format.TextStyle.FULL;
import static java.util.Locale.KOREAN;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

	public static LocalDateTime parseDateTime(String dateTimeStr) {
		if (dateTimeStr == null || dateTimeStr.isBlank())
			return null;
		try {
			String fixedDateTimeStr = fixDateTimeFormat(dateTimeStr);
			return LocalDateTime.parse(fixedDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private static String fixDateTimeFormat(String dateTimeStr) {
		String[] parts = dateTimeStr.split(" ");
		if (parts.length < 2)
			return dateTimeStr;
		String datePart = parts[0];
		String timePart = parts[1];
		String[] timeParts = timePart.split(":");
		String hour = timeParts[0];
		String minute = timeParts.length > 1 ? timeParts[1] : "00";
		if (minute.getBytes().length == 1) {
			minute = "0" + minute;
		}
		return datePart + " " + hour + ":" + minute;
	}
}
