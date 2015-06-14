package pl.devoxx.aggregatr.aggregation;

import com.google.common.base.Splitter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("ingredients")
@Data
public class IngredientsProperties {

    private String serviceNames = "chmieleo,slodeo,wodeo,drozdzeo";
    private String rootUrl = "http://localhost:8030/";
    private Integer threshold = 1000;

    public List<String> getListOfServiceNames() {
        return Splitter.on(',').omitEmptyStrings().trimResults().splitToList(serviceNames);
    }
}
