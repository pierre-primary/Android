
package anuode.apitest.hotel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "1",
    "2"
})
public class Data {

    @JsonProperty("1")
    private List<anuode.apitest.hotel._1> _1 = new ArrayList<anuode.apitest.hotel._1>();
    @JsonProperty("2")
    private List<anuode.apitest.hotel._2> _2 = new ArrayList<anuode.apitest.hotel._2>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The _1
     */
    @JsonProperty("1")
    public List<anuode.apitest.hotel._1> get1() {
        return _1;
    }

    /**
     * 
     * @param _1
     *     The 1
     */
    @JsonProperty("1")
    public void set1(List<anuode.apitest.hotel._1> _1) {
        this._1 = _1;
    }

    /**
     * 
     * @return
     *     The _2
     */
    @JsonProperty("2")
    public List<anuode.apitest.hotel._2> get2() {
        return _2;
    }

    /**
     * 
     * @param _2
     *     The 2
     */
    @JsonProperty("2")
    public void set2(List<anuode.apitest.hotel._2> _2) {
        this._2 = _2;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
