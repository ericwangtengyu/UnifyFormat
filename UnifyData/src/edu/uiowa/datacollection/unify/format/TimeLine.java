package edu.uiowa.datacollection.unify.format;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
/**
 * A user's timeline contains all the interations related with this use from
 * start date to end date. This is the basic unit we apply some kind of data mining
 * algorithm to.
 * @author Tengyu
 *
 */
public class TimeLine {
	private User user;
	private List<Conversation> conversations;
	private Date startDate;
	private Date endDate;
	
	public TimeLine(User user, List<Conversation> conversations,Date startDate,Date endDate){
		this.setUser(user);
		this.setConversations(conversations);
		this.setStartDate(startDate);
		this.setEndDate(endDate);
	}
	
	public TimeLine(User user){
		this.setUser(user);
		this.conversations=new ArrayList<Conversation>();
	}
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Conversation> getConversations() {
		return conversations;
	}

	public void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public boolean storeAs(String format){
		try {
			System.out.println(this.toJSONObject().toString(1));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public JSONObject toJSONObject() throws JSONException{
		JSONObject timeLine=new JSONObject();
		timeLine.put("User", user.toString());
		timeLine.put("startDate", startDate);
		timeLine.put("endDate", endDate);
		JSONArray conversArray=new JSONArray();
		for(Conversation c:conversations){
			conversArray.put(c.toJSONObject());
		}
		timeLine.put("conversations", conversArray);
		return timeLine;
	}

}
