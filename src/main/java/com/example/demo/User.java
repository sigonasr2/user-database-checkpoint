package com.example.demo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name="users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	Long id;
	
	String password;
	String email;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public User stripPassword() {
		password=null;
		return this;
	}
	public User patchProperties(User patchUser) {
		for (Field f : this.getClass().getDeclaredFields()) {
			try {
				if (patchUser.getClass().getDeclaredField(f.getName())!=null 
						&&  patchUser.getClass().getDeclaredField(f.getName()).get(patchUser)!=null) {
					f.set(this, patchUser.getClass().getDeclaredField(f.getName()).get(patchUser));
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
}
