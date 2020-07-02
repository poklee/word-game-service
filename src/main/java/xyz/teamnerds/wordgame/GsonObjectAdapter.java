package xyz.teamnerds.wordgame;

import com.google.gson.Gson;

/**
 * Use gson to convert from one type to another.  This works by copying all the same properties of 2 objects
 * @author plee
 *
 * @param <FromType>
 * @param <ToType>
 */
public class GsonObjectAdapter<ToType>
{
	
	public static <ToType> ToType convert(Object object, Class<ToType> type)
	{
		Gson gson = new Gson();
		String jsonString = gson.toJson(object);
		return gson.fromJson(jsonString, type);
	}

}
