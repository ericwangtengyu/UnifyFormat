package edu.uiowa.datacollection.unify.format;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

/**
 * The basic unit of interaction between users.
 * 
 * @author Tengyu
 * 
 */
public class Conversation implements Comparable<Conversation>{
	private String cID;
	private List<User> participants;
	private Type type;
	private Date createTime;
	private Date updateTime;
	private List<Message> msgList;

	public Conversation(Type type) {
		participants = new ArrayList<User>();
		msgList = new ArrayList<Message>();
	}

	public Conversation(String cID, List<User> participants, Type type,
			Date createTime, Date updateTime, List<Message> msgList) {
		this.setcID(cID);
		this.setCreateTime(createTime);
		this.setMsgList(msgList);
		this.setParticipants(participants);
		this.setType(type);
		this.setUpdateTime(updateTime);
	}

	public String getcID() {
		return cID;
	}

	public void setcID(String cID) {
		this.cID = cID;
	}

	public List<User> getParticipants() {
		return participants;
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public List<Message> getMsgList() {
		return msgList;
	}

	public void setMsgList(List<Message> msgList) {
		this.msgList = msgList;
	}
	
	public void addMsg(Message msg){
		msgList.add(msg);
	}
	
	public void addParticipant(User u){
		participants.add(u);
	}

	@Override
	public int compareTo(Conversation o) {
		return this.getUpdateTime().compareTo(o.getCreateTime());
	}
	
	public List<String> getMessageIDList(){
		List<String> messageIDList=new ArrayList<String> ();
		for(int i=0;i<msgList.size();i++){
			messageIDList.add(msgList.get(i).getmID());
		}
		return messageIDList;
	}
	
	public JSONObject toJSONObject() throws JSONException{
		JSONObject c=new JSONObject();
		c.put("cID", cID);
		c.put("participants", participants.toString());
		if(type!=null)
			c.put("type",type.toString());
		if(createTime!=null)
			c.put("createTime", createTime.toString());
		if(updateTime!=null)
			c.put("updateTime", updateTime.toString());
		JSONArray msgArray=new JSONArray();
		for(Message msg:msgList){
			msgArray.put(msg.toJSONObject());
		}
		c.put("messages", msgArray);
		return c;
	}

}
