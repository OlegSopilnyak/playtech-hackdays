package oleg.sopilnyak.service;

import java.time.Instant;

/**
 * The time-service
 */
public interface TimeService {
	/**
	 * To get current date-time
	 *
	 * @return current
	 */
	Instant now();

	/**
	 * To calculate duration between started and now in milliseconds
	 *
	 * @param start time of begin
	 * @return duration value
	 */
	Long duration(Instant start);
}
