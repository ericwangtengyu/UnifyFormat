package edu.uiowa.datacollection.unify.core;

import java.util.ArrayList;
import java.util.List;

import edu.uiowa.datacollection.unify.format.TimeLine;
import edu.uiowa.datacollection.unify.format.User;
import edu.uiowa.datacollection.unify.utilities.ConstantValues;
import edu.uiowa.datacollection.unify.utilities.JsonHelper;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class Unify {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<User> userList=getUserList();
		for(User u:userList){
			UnifyManager um=new UnifyManager(u);
			TimeLine t=um.constructTimeLine();
			t.storeAs("JSON");
		}
		
	}
	
	public static List<User> getUserList(){
		List<User> userList=new ArrayList<User>();
		JsonHelper jsonHelper=new JsonHelper();
		try {
			JSONArray jsonArray=jsonHelper.readJsonFromUrl(ConstantValues.GET_ALL_USER_URL);
			for(int i=0;i<jsonArray.length();i++){
				JSONObject obj=jsonArray.getJSONObject(i);
				//System.out.println(obj.toString(1));
				userList.add(createUser(obj));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return userList;
	}
	
	public static User createUser(JSONObject obj) throws JSONException{
		User u=new User(obj.getString("pk"),0);
		JSONObject fields=obj.getJSONObject("fields");
		u.setFacebookID(fields.getString("facebook_appid")); //TODO: add the real facebook id
		u.setTwitterID(fields.getString("twitter_id"));
		return u;
	}

}
