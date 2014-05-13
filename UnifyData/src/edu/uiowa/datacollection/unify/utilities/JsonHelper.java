package edu.uiowa.datacollection.unify.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import facebook4j.internal.org.json.JSONTokener;

@SuppressWarnings("deprecation")
public class JsonHelper
{

	private static String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1)
		{
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/**
	 * This method creates a JSON object from data returned by an HTTP GET
	 * request to the given url.
	 * 
	 * @param url
	 *            The url to request data from
	 * @return JSONObject of the data returned by the url
	 * @throws IOException
	 * @throws JSONException
	 */
	public  JSONArray readJsonFromUrl(String url) throws JSONException
	{
		try
		{
			InputStream is = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);

			JSONTokener tokener = new JSONTokener(jsonText);
			JSONArray finalResult = new JSONArray(tokener);
			is.close();

			return finalResult;
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Connection to the server refused.");
		}

		return null;
	}

	/**
	 * This function posts the given JSONObject to the given url using HTTP
	 * POST.
	 * 
	 * @param url
	 *            The url to post the data to
	 * @param data
	 *            The data to post
	 * @return The HttpResponse from the server
	 * @throws JSONException 
	 */
	public JSONObject postJsonData(String url, JSONObject data) throws JSONException
	{
		HttpResponse resp = null;

		try
		{
			HttpPost post = new HttpPost(url);
			post.setEntity(new ByteArrayEntity(data.toString().getBytes()));

			HttpClient httpclient = new DefaultHttpClient();
			try
			{
				resp = httpclient.execute(post);
				BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
				StringBuilder builder = new StringBuilder();
				for (String line = null; (line = reader.readLine()) != null;) {
				    builder.append(line).append("\n");
				}
				JSONObject finalResult=new JSONObject(builder.toString());
				return finalResult;
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
