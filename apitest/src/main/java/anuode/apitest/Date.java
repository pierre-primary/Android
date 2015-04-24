package anuode.apitest;

/**
 * Created by xudong on 2015/3/27.
 */
public class Date {
    private int status, srcType;
    private String thisLoginIp, username, tokenId;


    public int getSrcType() {
        return srcType;
    }

    public int getStatus() {
        return status;
    }

    public String getThisLoginIp() {
        return thisLoginIp;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getUsername() {
        return username;
    }


    public void setSrcType(int srcType) {
        this.srcType = srcType;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setThisLoginIp(String thisLoginIp) {
        this.thisLoginIp = thisLoginIp;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
