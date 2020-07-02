package xyz.teamnerds.wordgame;

import javax.annotation.Nonnull;

/**
 * Utility for FindBug and IDE code lint tools to do null check conformity
 * 
 * @author plee
 *
 * @param <T>
 */
public class NullCheck<T> {

	/**
	 * Using IDE or FindBug null checkers require explict conversion of references
	 * to nonnull references. Alot of API written without these annotation will
	 * result in warnings in these code clean up tools. We can use this method to
	 * convert a nullable reference to a nonnull reference.
	 * 
	 * Example usage Collections.emptyList() always return non null even though it
	 * is not annotation. We can wrap the call around this method to avoid warning
	 * messages.  Example: CheckNull.asNonnull(Collections.emptyList())
	 * 
	 * @param object
	 * @return
	 * 
	 * @throws NullPointerException if object really is null
	 */
	@Nonnull
	public static <T> T asNonnull(T object) {
		if (object == null) {
			throw new NullPointerException("Expected non null object");
		} else {
			return object;
		}
	}

}
