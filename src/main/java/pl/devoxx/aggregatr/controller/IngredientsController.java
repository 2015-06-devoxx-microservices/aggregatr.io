package pl.devoxx.aggregatr.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.devoxx.aggregatr.model.Ingredients;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientsController {

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Ingredients> getIngredients() {
        return null;
    }

}
