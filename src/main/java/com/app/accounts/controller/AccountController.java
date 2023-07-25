package com.app.accounts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.app.accounts.client.CardFeignClient;
import com.app.accounts.client.LoanFeignClient;
import com.app.accounts.entity.AccountServiceConfig;
import com.app.accounts.entity.Accounts;
import com.app.accounts.entity.Cards;
import com.app.accounts.entity.Customer;
import com.app.accounts.entity.CustomerDetails;
import com.app.accounts.entity.Loans;
import com.app.accounts.entity.Properties;
import com.app.accounts.repository.AccountRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
@RestController
public class AccountController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private AccountServiceConfig accountServiceConfig;
	
	@Autowired
	LoanFeignClient loanFeignClient;
	
	@Autowired
	CardFeignClient cardFeignClient;
	
	@PostMapping(value = "/myAccount")
	@Timed(value = "getAccountDetails.time", description = "Time taken to return Account Details")
	public Accounts getAccountDetails(@RequestBody Customer customer) {
		Accounts accounts = accountRepository.findByCustomerId(customer.getCustomerId());
	
		if(accounts != null) {
			return accounts;
		}else {
			return null;
		}
	}
	
	@GetMapping(value = "/account/properties")
	public String getPropertyDetails()throws JsonProcessingException{
		ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountServiceConfig.getMsg(),accountServiceConfig.getBuildversion(),accountServiceConfig.getMailDetails(),accountServiceConfig.getActiveBranches());
		String strString = objectWriter.writeValueAsString(properties);
		return strString;
	}
	
	@PostMapping(value = "/myCustomerDetails")
	@CircuitBreaker(name = "detailsForCustomerSupportApp",fallbackMethod = "myCustomerDetailsFallBack")
	@Retry(name = "retryForCustomerDetail",fallbackMethod = "myCustomerDetailsFallBack")
//	public CustomerDetails getCustomerDetails(@RequestBody Customer customer) {
	public CustomerDetails getCustomerDetails(@RequestHeader("eazybank-correlation-id") String correaltionId, @RequestBody Customer customer) {
	Accounts accounts = accountRepository.findByCustomerId(customer.getCustomerId());
		LOGGER.info("getCustomerDetails() method started");
		List<Loans> loans = loanFeignClient.getLoansDetails(correaltionId,customer);
		List<Cards> cards = cardFeignClient.getCardsDetails(correaltionId,customer);
		
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		customerDetails.setCards(cards);
		LOGGER.info("getCustomerDetails() method ended");
		return customerDetails;
	}
	
//	private CustomerDetails myCustomerDetailsFallBack(Customer customer,Throwable throwable) {
	private CustomerDetails myCustomerDetailsFallBack(@RequestHeader("eazybank-correlation-id") String correaltionId,Customer customer,Throwable throwable) {
		Accounts accounts = accountRepository.findByCustomerId(customer.getCustomerId());
		List<Loans> loans = loanFeignClient.getLoansDetails(correaltionId,customer);
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		return customerDetails;
	}
	
	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello",fallbackMethod = "sayHelloFallBack")
	public String sayHello() {
		return "Hello, Welcome to Sovan Tutorials";
	}
	
	private String sayHelloFallBack(Throwable throwable) {
		return "Hi, Welcome to Sovan Tutorials";
	}
}
