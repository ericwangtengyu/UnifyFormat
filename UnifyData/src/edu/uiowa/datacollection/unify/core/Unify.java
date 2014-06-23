package edu.uiowa.datacollection.unify.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean correctFormat=false;
		String str1=null,str2=null;
		Date startDate=null;
		Date endDate=null;
		while(!correctFormat){
			System.out.println("Please specify the startDate(yyyy-mm-dd):");
			str1 = br.readLine().trim();
			
			System.out.println("Please specify the endDate(yyyy-mm-dd):");
			str2 = br.readLine().trim();
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd",
					Locale.ENGLISH);
			try{
				startDate = df.parse(str1);
				endDate=df.parse(str2);
				correctFormat=true;
			}
			catch (ParseException pe){
				System.out.println("Please input the date in specified date format: yyyy-mm-dd");
			}
		}
		
		List<User> userList = getUserList();
		for (User u : userList) {
			UnifyManager um = new UnifyManager(u);
			um.setStartDate(startDate);
			um.setEndDate(endDate);
			TimeLine t = um.constructTimeLine();
			StringBuilder sb=new StringBuilder();
			String []t1 = startDate.toGMTString().split(" ");
			String []t2 = startDate.toGMTString().split(" ");
			sb.append(u.getPhoneNum()).append("_").append(t1[0]).append(t1[1]).append(t1[2]).append("_").append(t2[0]).append(t2[1]).append(t2[2]);
			um.saveJsonData(sb.toString(), t.toJSONObject());
			System.out.println("Data for user "+u.getPhoneNum()+" has been saved");
		}

	}
	
	public static List<User> getUserList(){
		List<User> userList=new ArrayList<User>();
		JsonHelper jsonHelper=new JsonHelper();
		try {
			JSONArray jsonArray=jsonHelper.readJsonFromUrl(ConstantValues.GET_ALL_USER_URL);
			for(int i=0;i<jsonArray.length();i++){
				JSONObject obj=jsonArray.getJSONObject(i);
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
		u.setFacebookID(fields.getString("facebook_appid")); //TODO: add the real facebook id in database end
		u.setTwitterID(fields.getString("twitter_id"));
		return u;
	}

}
