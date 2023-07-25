package com.app.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.app.accounts.entity.Accounts;
@Repository
public interface AccountRepository extends CrudRepository<Accounts, Long>{

	Accounts findByCustomerId(int customerId);
}
