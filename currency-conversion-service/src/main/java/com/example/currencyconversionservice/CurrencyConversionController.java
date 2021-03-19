package com.example.currencyconversionservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
public class CurrencyConversionController {

    @Autowired
    CurrencyExchangeProxy currencyExchangeProxy;

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(@PathVariable String from,
                                                          @PathVariable String to,
                                                          @PathVariable BigDecimal quantity){

        HashMap<String,String> uriVariables = new HashMap<>();
        uriVariables.put("from",from);
        uriVariables.put("to",to);

        ResponseEntity<CurrencyConversion> responseEntity =  new RestTemplate().getForEntity(
                "http://localhost:8001/currency-exchange/from/{from}/to/{to}",
                CurrencyConversion.class,
                uriVariables
        );

        CurrencyConversion currencyConversion = responseEntity.getBody();

        return new CurrencyConversion(
                currencyConversion.getId(),
                currencyConversion.getFrom(),
                currencyConversion.getTo(),
                quantity,
                currencyConversion.getConversionMultiple(),
                quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment());
    }







//    @CircuitBreaker(name="currency-exchange-cb", fallbackMethod = "defaultResponse")
    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(@PathVariable String from,
                                                          @PathVariable String to,
                                                          @PathVariable BigDecimal quantity){


        CurrencyConversion currencyConversion = currencyExchangeProxy.retrieveExchangeValue(from,to);

        return new CurrencyConversion(
                currencyConversion.getId(),
                currencyConversion.getFrom(),
                currencyConversion.getTo(),
                quantity,
                currencyConversion.getConversionMultiple(),
                quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment());
    }



    public CurrencyConversion defaultResponse(Exception ex){
        return new CurrencyConversion(
                1L,
                "USD",
                "INR",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(800),
                "8000");

    }
}
