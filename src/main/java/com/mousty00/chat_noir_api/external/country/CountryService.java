package com.mousty00.chat_noir_api.external.country;

import org.springframework.stereotype.Service;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@Service
@HttpExchange("https://www.apicountries.com/")
public interface CountryService {

    @GetExchange("/countries")
    List<Country> countries();

}
