package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	@Query("from Contact as c where c.user.id=:y ")
	//Pageable contains page nuumber or current page and pageSize
public Page<Contact> findContactsByUser(@Param("y") int userId,Pageable pageable);

	//custom finder method for search functionality
	public List<Contact>findByNameContainingAndUser(String keyword,User user);

}
