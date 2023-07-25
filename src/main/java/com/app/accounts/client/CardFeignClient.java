package com.app.accounts.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.app.accounts.entity.Cards;
import com.app.accounts.entity.Customer;

@FeignClient("cards")
public interface CardFeignClient {

	@RequestMapping(method = RequestMethod.POST,value = "/myCards",consumes = "application/json")
	List<Cards> getCardsDetails(@RequestHeader("eazybank-correlation-id") String correlationid,@RequestBody Customer customer);
}
