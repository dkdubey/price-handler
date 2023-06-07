package com.santander.pricehandler.controller;

import com.santander.pricehandler.PriceSubscriber;
import com.santander.pricehandler.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceHandlerController {

    @Autowired
    PriceSubscriber priceSubscriber;

    @RequestMapping(path = "get-latest-price/{instrument}", method = RequestMethod.GET)
    public ResponseEntity<Price> calculatePrice(@PathVariable("instrument") String instrument) {
        Price latestPrice = priceSubscriber.getLatestPrice(instrument);
        return new ResponseEntity<>(latestPrice, HttpStatus.OK);
    }

}
