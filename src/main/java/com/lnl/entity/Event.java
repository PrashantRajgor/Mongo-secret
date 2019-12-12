package com.lnl.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"SecretId",
"ClientRequestToken",
"Step"
})
public class Event {

	@JsonProperty("SecretId")
	private String secretId;
	@JsonProperty("ClientRequestToken")
	private String clientRequestToken;
	@JsonProperty("Step")
	private String step;
	
	@JsonProperty("SecretId")
	public String getSecretId() {
		return secretId;
	}
	@JsonProperty("SecretId")
	public void setSecretId(String secretId) {
		this.secretId = secretId;
	}
	@JsonProperty("ClientRequestToken")
	public String getClientRequestToken() {
		return clientRequestToken;
	}
	@JsonProperty("ClientRequestToken")
	public void setClientRequestToken(String clientRequestToken) {
		this.clientRequestToken = clientRequestToken;
	}
	@JsonProperty("Step")
	public String getStep() {
		return step;
	}
	@JsonProperty("Step")
	public void setStep(String step) {
		this.step = step;
	}
	
	
	
	
	
}
