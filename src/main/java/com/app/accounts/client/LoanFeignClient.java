package com.app.accounts.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.app.accounts.entity.Customer;
import com.app.accounts.entity.Loans;

@FeignClient("loans")
public interface LoanFeignClient {

	@RequestMapping(method = RequestMethod.POST,value = "myLoans",consumes = "application/json")
	List<Loans> getLoansDetails(@RequestHeader("eazybank-correlation-id") String correlationid,@RequestBody Customer customer);
}
