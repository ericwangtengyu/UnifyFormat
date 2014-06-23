package edu.uiowa.datacollection.unify.format;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

/**
 * Basic unified class of Message
 * @author Tengyu
 *
 */
public class Message implements Comparable<Message>{
	private String mID;
	private User sender;
	private List<User> recipients;
	private String body;
	private Type type;
	private Date createTime;
	private String inReplyToMessageID;

	public Message(String mID, User sender, List<User> recipients, String body,
			Type type) {
		this.setmID(mID);
		this.setSender(sender);
		this.setRecipients(recipients);
		this.setBody(body);
		this.setType(type);
	}

	public Message() {
		setRecipients(new ArrayList<User>());
	}

	public String getmID() {
		return mID;
	}

	public void setmID(String mID) {
		this.mID = mID;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public List<User> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<User> recipients) {
		this.recipients = recipients;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
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

	@Override
	public int compareTo(Message o) {
		return this.createTime.compareTo(o.getCreateTime());
	}

	public String getInReplyToMessageID() {
		return inReplyToMessageID;
	}

	public void setInReplyToMessageID(String inReplyToMessageID) {
		this.inReplyToMessageID = inReplyToMessageID;
	}
	
	public JSONObject toJSONObject() throws JSONException{
		JSONObject msg=new JSONObject();
		msg.put("mID", mID);
		if(sender!=null)
			msg.put("sender", sender.toString());
		if(recipients!=null)
			msg.put("recipients", recipients.toString());
		if(body!=null)
			msg.put("body", body);
		if(type!=null)
			msg.put("type", type.toString());
		if(createTime!=null)
			msg.put("createTime", createTime.toString());
		return msg;
	}
}
