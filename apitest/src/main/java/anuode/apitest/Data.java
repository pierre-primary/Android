
package anuode.apitest;

import java.util.HashMap;
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
    "regTime",
    "status",
    "__v",
    "thisLoginIp",
    "srcType",
    "lastLoginIp",
    "username",
    "tokenId",
    "customerId",
    "thisLoginTime",
    "lastLoginTime",
    "clientType",
    "accId",
    "mobile"
})
public class Data {

    @JsonProperty("regTime")
    private String regTime;
    @JsonProperty("status")
    private Integer status;
    @JsonProperty("__v")
    private Integer V;
    @JsonProperty("thisLoginIp")
    private String thisLoginIp;
    @JsonProperty("srcType")
    private Integer srcType;
    @JsonProperty("lastLoginIp")
    private String lastLoginIp;
    @JsonProperty("username")
    private String username;
    @JsonProperty("tokenId")
    private String tokenId;
    @JsonProperty("customerId")
    private String customerId;
    @JsonProperty("thisLoginTime")
    private String thisLoginTime;
    @JsonProperty("lastLoginTime")
    private String lastLoginTime;
    @JsonProperty("clientType")
    private Integer clientType;
    @JsonProperty("accId")
    private String accId;
    @JsonProperty("mobile")
    private String mobile;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The regTime
     */
    @JsonProperty("regTime")
    public String getRegTime() {
        return regTime;
    }

    /**
     * 
     * @param regTime
     *     The regTime
     */
    @JsonProperty("regTime")
    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }

    /**
     * 
     * @return
     *     The status
     */
    @JsonProperty("status")
    public Integer getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    @JsonProperty("status")
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The V
     */
    @JsonProperty("__v")
    public Integer getV() {
        return V;
    }

    /**
     * 
     * @param V
     *     The __v
     */
    @JsonProperty("__v")
    public void setV(Integer V) {
        this.V = V;
    }

    /**
     * 
     * @return
     *     The thisLoginIp
     */
    @JsonProperty("thisLoginIp")
    public String getThisLoginIp() {
        return thisLoginIp;
    }

    /**
     * 
     * @param thisLoginIp
     *     The thisLoginIp
     */
    @JsonProperty("thisLoginIp")
    public void setThisLoginIp(String thisLoginIp) {
        this.thisLoginIp = thisLoginIp;
    }

    /**
     * 
     * @return
     *     The srcType
     */
    @JsonProperty("srcType")
    public Integer getSrcType() {
        return srcType;
    }

    /**
     * 
     * @param srcType
     *     The srcType
     */
    @JsonProperty("srcType")
    public void setSrcType(Integer srcType) {
        this.srcType = srcType;
    }

    /**
     * 
     * @return
     *     The lastLoginIp
     */
    @JsonProperty("lastLoginIp")
    public String getLastLoginIp() {
        return lastLoginIp;
    }

    /**
     * 
     * @param lastLoginIp
     *     The lastLoginIp
     */
    @JsonProperty("lastLoginIp")
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    /**
     * 
     * @return
     *     The username
     */
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    /**
     * 
     * @param username
     *     The username
     */
    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 
     * @return
     *     The tokenId
     */
    @JsonProperty("tokenId")
    public String getTokenId() {
        return tokenId;
    }

    /**
     * 
     * @param tokenId
     *     The tokenId
     */
    @JsonProperty("tokenId")
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * 
     * @return
     *     The customerId
     */
    @JsonProperty("customerId")
    public String getCustomerId() {
        return customerId;
    }

    /**
     * 
     * @param customerId
     *     The customerId
     */
    @JsonProperty("customerId")
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * 
     * @return
     *     The thisLoginTime
     */
    @JsonProperty("thisLoginTime")
    public String getThisLoginTime() {
        return thisLoginTime;
    }

    /**
     * 
     * @param thisLoginTime
     *     The thisLoginTime
     */
    @JsonProperty("thisLoginTime")
    public void setThisLoginTime(String thisLoginTime) {
        this.thisLoginTime = thisLoginTime;
    }

    /**
     * 
     * @return
     *     The lastLoginTime
     */
    @JsonProperty("lastLoginTime")
    public String getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 
     * @param lastLoginTime
     *     The lastLoginTime
     */
    @JsonProperty("lastLoginTime")
    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * 
     * @return
     *     The clientType
     */
    @JsonProperty("clientType")
    public Integer getClientType() {
        return clientType;
    }

    /**
     * 
     * @param clientType
     *     The clientType
     */
    @JsonProperty("clientType")
    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }

    /**
     * 
     * @return
     *     The accId
     */
    @JsonProperty("accId")
    public String getAccId() {
        return accId;
    }

    /**
     * 
     * @param accId
     *     The accId
     */
    @JsonProperty("accId")
    public void setAccId(String accId) {
        this.accId = accId;
    }

    /**
     * 
     * @return
     *     The mobile
     */
    @JsonProperty("mobile")
    public String getMobile() {
        return mobile;
    }

    /**
     * 
     * @param mobile
     *     The mobile
     */
    @JsonProperty("mobile")
    public void setMobile(String mobile) {
        this.mobile = mobile;
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
