package com.mousty00.chat_noir_api.external.country;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/countries")
@AllArgsConstructor
public class CountryController {

   final private CountryService countryService;


    @GetMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<Country> country() {
        return countryService.countries();
    }
}
