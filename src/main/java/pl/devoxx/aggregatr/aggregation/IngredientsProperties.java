package pl.devoxx.aggregatr.aggregation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("ingredients")
@Data
public class IngredientsProperties {

    private String serviceNames = "chmieleo,slodeo,wodeo,drozdzeo";
    private String rootUrl = "http://localhost:8030/";

    public List<String> getListOfServiceNames() {
        return Arrays.asList(serviceNames.split(","));
    }
}
