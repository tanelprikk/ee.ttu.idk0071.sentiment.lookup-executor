package ee.ttu.idk0071.sentiment.services.objects;

import java.util.HashMap;
import java.util.Map;

public class Mail {
	private MailParty recipient;
	private MailParty sender;
	private String subject;
	private String body;

	private Map<String, Object> parameters = new HashMap<String, Object>();

	public MailParty getRecipient() {
		return recipient;
	}

	public void setRecipient(MailParty recipient) {
		this.recipient = recipient;
	}

	public MailParty getSender() {
		return sender;
	}

	public void setSender(MailParty sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String topic) {
		this.subject = topic;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}