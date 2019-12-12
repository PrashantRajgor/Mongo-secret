package com.lnl.entity;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"username",
"password",
"masterarn",
"ipaddress"
})
public class SecretValues {

@JsonProperty("username")
private String username;
@JsonProperty("password")
private String password;
@JsonProperty("masterarn")
private String masterarn;
@JsonProperty("ipaddress")
private String ipaddress;

@JsonProperty("username")
public String getUsername() {
return username;
}

@JsonProperty("username")
public void setUsername(String username) {
this.username = username;
}

@JsonProperty("password")
public String getPassword() {
return password;
}

@JsonProperty("password")
public void setPassword(String password) {
this.password = password;
}

@JsonProperty("masterarn")
public String getMasterarn() {
return masterarn;
}

@JsonProperty("masterarn")
public void setMasterarn(String masterarn) {
this.masterarn = masterarn;
}

@JsonProperty("ipaddress")
public String getIpaddress() {
return ipaddress;
}

@JsonProperty("ipaddress")
public void setIpaddress(String ipaddress) {
this.ipaddress = ipaddress;
}

}