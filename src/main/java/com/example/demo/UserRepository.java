package com.example.demo;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long>{
	List<User> findByPasswordAndEmail(String pw,String email);
}
