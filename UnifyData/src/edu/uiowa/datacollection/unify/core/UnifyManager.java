package edu.uiowa.datacollection.unify.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.uiowa.datacollection.unify.format.Conversation;
import edu.uiowa.datacollection.unify.format.Message;
import edu.uiowa.datacollection.unify.format.TimeLine;
import edu.uiowa.datacollection.unify.format.Type;
import edu.uiowa.datacollection.unify.format.User;
import edu.uiowa.datacollection.unify.utilities.ConstantValues;
import edu.uiowa.datacollection.unify.utilities.JsonHelper;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class UnifyManager {
	private List<User> userList;
	private User user;
	private JsonHelper jsonHelper;
	private TimeLine timeLine;
	private Date startDate;
	private Date endDate;
	
	public UnifyManager(User u){
		userList=Unify.getUserList();
		this.setUser(u);
		jsonHelper=new JsonHelper();
	}
	
	public JSONObject getRawData() throws JSONException, IOException{
		JSONObject obj=new JSONObject();
		obj.put("phoneNum", user.getPhoneNum());
		//startDate=new Date(1000000000);
		obj.put("startDate",startDate.getTime() );
		//endDate=new Date();
		obj.put("endDate", endDate.getTime());
		return jsonHelper.postJsonData(ConstantValues.COLLECT_URL, obj);
	}
	
	public TimeLine constructTimeLine(){
		TimeLine timeLine =new TimeLine(user);
		try {
			JSONObject rawData=getRawData();
			//System.out.println(rawData.toString(1));
			List<Conversation> twitterDirectConversation=constructTwitterDirectConversation(rawData);
			
			List<Conversation> twitterStatusConversation=constructTwitterStatusConversation(rawData);
			
			List<Conversation> facebookdirectConversation=constructFacebookDirectConversation(rawData);
			
			List<Conversation> facebookActivityConversation=constructFacebookActivityConversation(rawData);
			
			List<Conversation> smsConversation=constructSMSConversation(rawData);
			
			List<Conversation> allConversation=new ArrayList<Conversation>();
			allConversation.addAll(twitterDirectConversation);allConversation.addAll(twitterStatusConversation);
			allConversation.addAll(facebookdirectConversation);allConversation.addAll(facebookActivityConversation);
			allConversation.addAll(smsConversation);
			timeLine.setStartDate(startDate);
			timeLine.setEndDate(endDate);
			timeLine.setConversations(allConversation);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeLine;
	}
	/**
	 * Construct Twitter Direct Msg Conversation.
	 * @param rawData
	 * @return
	 * @throws JSONException
	 */
	public List<Conversation> constructTwitterDirectConversation(JSONObject rawData) throws JSONException{
		List<Conversation> tConversList=new ArrayList<Conversation>();
		JSONArray twitterDirectConversation=rawData.getJSONArray("twitterDirectConversation");
		for(int i=0;i<twitterDirectConversation.length();i++){
			JSONObject obj=twitterDirectConversation.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			JSONArray users=fields.getJSONArray("users");
			Conversation c=new Conversation(Type.twitterMsg);
			c.setcID(obj.getString("pk"));
			c.setCreateTime(new Date(fields.getLong("startTime")));
			c.setUpdateTime(new Date(fields.getLong("endTime")));
			c.setType(Type.twitterMsg);
			for(int j=0;j<users.length();j++){
				String num=users.getString(j);
				for(User u:userList){
					if(u.getPhoneNum()==num)
					{
						c.addParticipant(u);
						break;
					}
				}
			}
			tConversList.add(c);
		}
		JSONArray twitterMessage=rawData.getJSONArray("twitterMessage");
		for(int i=0;i<twitterMessage.length();i++){
			JSONObject obj=twitterMessage.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Message m=new Message();
			m.setmID(obj.getString("pk"));
			m.setBody(fields.getString("body"));
			m.setCreateTime(new Date(fields.getLong("created_time")));
			m.setType(Type.twitterMsg);
			List<User> recipients=new ArrayList<User>();
			for(int j=0;j<tConversList.size();j++){
				Conversation c=tConversList.get(j);
				if(fields.getString("conversations").equals(c.getcID())){
					for(User u:c.getParticipants()){
						if(fields.getString("fromID").equals(u.getTweetId())){
							m.setSender(u);
						}
						if(Arrays.asList(fields.getString("toID").split(",")).contains(u.getTweetId())){
							recipients.add(u);
						}
					}
					m.setRecipients(recipients);
					c.addMsg(m);
					break;
				}
			}
		}
		return tConversList;
	}
	/**
	 * The function to construct Twitter Status Conversation.
	 * @param statusList
	 * @return
	 * @throws JSONException 
	 */
	public List<Conversation> constructTwitterStatusConversation(JSONObject rawData) throws JSONException{
		JSONArray twitterStatus=rawData.getJSONArray("twitterStatus");
		List<Message> statusList=new ArrayList<Message>();
		for(int i=0;i<twitterStatus.length();i++){
			JSONObject obj=twitterStatus.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Message m=new Message();
			m.setmID(obj.getString("pk"));
			m.setType(Type.twitterStatus);
			m.setCreateTime(new Date(fields.getLong("created_time")));
			m.setBody(fields.getString("body")); 
			m.setInReplyToMessageID(fields.getString("inReplyToStatusID"));
			if(!fields.optString("author").equals("null")){
				for(User u:userList){
					if(u.getPhoneNum().equals(fields.getString("author"))){
						m.setSender(u);
					}
				}
			}
			JSONArray mentionor=fields.getJSONArray("mentionor");
			for(int k=0;k<mentionor.length();k++){
				
			}
			statusList.add(m);
		}
		Collections.sort(statusList);
		List<Conversation> statusConversationList = new ArrayList<Conversation>();
		for (int i = 0; i < statusList.size(); i++)
		{
			Message m = statusList.get(i);
			boolean startNewConversation = true;
			for (int j = 0; j < statusConversationList.size(); j++)
			{
				Conversation c = statusConversationList.get(j);
				List<String> mIDList = c.getMessageIDList();
				if (mIDList.contains(m.getInReplyToMessageID()))
				{
					startNewConversation = false;
					if (mIDList.get(mIDList.size() - 1).equals(
							m.getInReplyToMessageID()))
					{
						c.addMsg(m);
						break;
					}
					else
					{
						/*
						 * This is the case that: A tweet has already been put
						 * into a conversation. But it appears in more than 1
						 * conversation.
						 */
						Conversation newC = new Conversation(Type.twitterStatus);
						for (int w = 0; w < mIDList.size(); w++)
						{
							newC.addMsg(c.getMsgList().get(w));
							if (mIDList.get(w)
									.equals(m.getInReplyToMessageID()))
							{
								newC.addMsg(m);
								statusConversationList.add(newC);
								break;
							}
						}
						break;// break to avoid infinte loop.
					}
				}
			}
			if (startNewConversation)
			{
				Conversation newC = new Conversation(Type.twitterStatus);
				newC.addMsg(m);
				statusConversationList.add(newC);
			}
		}
		return statusConversationList;
	}
	/**
	 * Construct facebook direct conversation.
	 * @param rawData
	 * @return
	 * @throws JSONException
	 */
	public List<Conversation> constructFacebookDirectConversation(JSONObject rawData) throws JSONException{
		JSONArray facebookDirectConversation=rawData.getJSONArray("facebookDirectConversation");
		List<Conversation> fdirectConversList=new ArrayList<Conversation>();
		for(int i=0;i<facebookDirectConversation.length();i++){
			JSONObject obj=facebookDirectConversation.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			JSONArray users=fields.getJSONArray("user");
			Conversation c=new Conversation(Type.facebookmsg);
			c.setcID(obj.getString("pk"));

			c.setUpdateTime(new Date(fields.getLong("updated_time")));
			for(int j=0;j<users.length();j++){
				String num=users.getString(j);
				for(User u:userList){
					if(u.getPhoneNum()==num)
					{
						c.addParticipant(u);
						break;
					}
				}
			}
			fdirectConversList.add(c);
		}
		JSONArray facebookMsg=rawData.getJSONArray("facebookMessage");
		for(int i=0;i<facebookMsg.length();i++){
			JSONObject obj=facebookMsg.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Message m=new Message();
			m.setType(Type.facebookmsg);
			m.setBody(fields.getString("body"));
			m.setCreateTime(new Date(fields.getLong("created_time")));
			m.setmID(fields.getString("author_id")+fields.getString("created_time"));
			for(int j=0;j<fdirectConversList.size();j++){
				Conversation C=fdirectConversList.get(j);
				if(fields.getString("conversation").equals(C.getcID())){
					C.addMsg(m);
					boolean existInStudy=false;
					for(User u:C.getParticipants()){
						if(fields.getString("author_id").equals(u.getFacebookId())){
							m.setSender(u);// TODO: currently u does not have facebookid
							existInStudy=true;
							break;
						}
					}
					if(!existInStudy) m.setSender(new User(fields.getString("author_id"),1));
					break;
				}
			}	
		}
		return fdirectConversList;
	}
	
	/**
	 * Construct facebook activity conversation.
	 * @param activityMsg
	 * @param commentMsg
	 * @return
	 * @throws JSONException 
	 */
	public List<Conversation> constructFacebookActivityConversation(JSONObject rawData) throws JSONException{
		JSONArray facebookActivity=rawData.getJSONArray("facebookActivity");
		List<Message> facebookActivityMsg=new ArrayList<Message>();
		for(int i=0;i<facebookActivity.length();i++){
			JSONObject obj=facebookActivity.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Message m=new Message();
			m.setType(Type.facebookstatus);
			m.setmID(obj.getString("pk"));
			if(fields.optString("message")!=null){
				m.setBody(fields.getString("message"));
			}else if(fields.optString("description")!=null)
			{
				m.setBody(fields.getString("descirption"));
			}
			m.setCreateTime(new Date(fields.getLong("updated_time")));
			boolean existInStudy=false;
			for(User u:userList){
				if(fields.getString("actor_id").equals(u.getFacebookId())){
					m.setSender(u);
					existInStudy=true;
					break;
				}
			}
			if(!existInStudy) m.setSender(new User(fields.getString("actor_id"),1));
			facebookActivityMsg.add(m);
		}
		
		
		JSONArray facebookComments=rawData.getJSONArray("facebookComments");
		List<Message> commentMsg=new ArrayList<Message>();
		for(int i=0;i<facebookComments.length();i++){
			JSONObject obj=facebookComments.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Message m=new Message();
			m.setType(Type.facebookstatus);
			m.setmID(fields.getString("comment_id"));
			m.setBody(fields.getString("text"));
			boolean existInStudy=false;
			for(User u:userList){
				if(fields.getString("from_id").equals(u.getFacebookId())){
					m.setSender(u);
					existInStudy=true;
					break;
				}
			}
			if(!existInStudy) m.setSender(new User(fields.getString("from_id"),1));
			m.setInReplyToMessageID(fields.getString("activity"));
			commentMsg.add(m);
		}
		List<Conversation> facebookActivityConversation=new ArrayList<Conversation>();
		for(int i=0;i<facebookActivityMsg.size();i++){
			Conversation c=new Conversation(Type.facebookstatus);
			c.addMsg(facebookActivityMsg.get(i));
			facebookActivityConversation.add(c);
		}
		
		for(int i=0;i<commentMsg.size();i++){
			Message m=commentMsg.get(i);
			for(int j=0;j<facebookActivityConversation.size();j++){
				Conversation c=facebookActivityConversation.get(j);
				if(c.getMsgList().get(0).getmID().equals(m.getInReplyToMessageID())){
					c.addMsg(m);
				}
			}
		}
		for(int i=0;i<facebookActivityConversation.size();i++){
			Conversation c=facebookActivityConversation.get(i);
			
			c.setUpdateTime(c.getMsgList().get(0).getCreateTime());
		}
		return facebookActivityConversation;
	}
	
	public List<Conversation> constructSMSConversation(JSONObject rawData) throws JSONException{
		JSONArray smsConversation=rawData.getJSONArray("SMSConversation");
		List<Conversation> smsConversationList=new ArrayList<Conversation>();
		for(int i=0;i<smsConversation.length();i++){
			JSONObject obj=smsConversation.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Conversation c=new Conversation(Type.smsMsg);
			c.setcID(obj.getString("pk"));
			c.setUpdateTime(new Date(fields.getLong("last_updated")));
			String participantStr=fields.getString("participants");
			participantStr=participantStr.substring(1,participantStr.length()-1);
			String []participants=participantStr.split(",");
			for(String phoneNum:participants){
				boolean existInStudy=false;
				for(User u:userList){
					if(u.getPhoneNum().equals(phoneNum)){
						c.addParticipant(u);
						existInStudy=true;
						break;
					}
				}
				if(!existInStudy) c.addParticipant(new User(phoneNum,0));
			}
			smsConversationList.add(c);
		}
		
		JSONArray smsMessage=rawData.getJSONArray("SMSMessage");
		for(int i=0;i<smsMessage.length();i++){
			JSONObject obj=smsMessage.getJSONObject(i);
			JSONObject fields=obj.getJSONObject("fields");
			Message m=new Message();
			m.setType(Type.smsMsg);
			m.setBody(fields.getString("body"));
			m.setCreateTime(new Date(fields.getLong("created_time")));
			String recipientStr=fields.getString("recipient");
			recipientStr=recipientStr.substring(1,recipientStr.length()-1);
			String []recipients=recipientStr.split(",");
			for(int j=0;j<smsConversationList.size();j++){
				Conversation c=smsConversationList.get(j);
				if(fields.getString("conversation").equals(c.getcID())){
					for(User u:c.getParticipants()){
						if(fields.getString("source").equals(u.getPhoneNum())){
							m.setSender(u);
							break;
						}
					}
					List<User> recipientsList=new ArrayList<User>();
					for(String phoneNum:recipients){
						for(User u:c.getParticipants()){
							if(phoneNum.equals(u.getPhoneNum())){
								recipientsList.add(u);
								break;
							}
						}
					}
					m.setRecipients(recipientsList);
					c.addMsg(m);
					break;
				}
			}
		}
		return smsConversationList;
	}
	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public TimeLine getTimeLine() {
		return timeLine;
	}

	public void setTimeLine(TimeLine timeLine) {
		this.timeLine = timeLine;
	}
	
	public void setStartDate(Date startDate){
		this.startDate=startDate;
	}
	
	public void setEndDate(Date endDate){
		this.endDate=endDate;
	}
	
	public void saveJsonData(String filename,JSONObject data)
	{
		try {
			System.out.println(data.toString(1));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		File file = new File(filename);

		try
		{
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);

			pw.append(data.toString(1) + "\n");

			pw.flush();
			pw.close();
			f.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
