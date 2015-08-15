package com.befun.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Friend implements Serializable{
	public String nickName = null,befunId = null,gender = null;	
	public Friend(){		
	}
	public Friend(String nickName,String befunId,String gender){
		this.nickName = nickName;
		this.befunId = befunId;
		this.gender = gender;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getBefunId() {
		return befunId;
	}
	public void setBefunId(String befunId) {
		this.befunId = befunId;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
}
