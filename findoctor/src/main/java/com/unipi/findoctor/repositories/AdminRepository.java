package com.unipi.findoctor.repositories;

import com.unipi.findoctor.models.Admin;
import com.unipi.findoctor.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, User> {

}
