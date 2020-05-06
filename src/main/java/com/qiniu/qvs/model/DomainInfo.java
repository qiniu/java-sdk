package com.qiniu.qvs.model;


public class DomainInfo {
    public String Domain;
    public String Type;
    public String CNAME;
    public int State;

    public String getDomain() {
        return Domain;
    }

    public void setDomain(String domain) {
        Domain = domain;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getCNAME() {
        return CNAME;
    }

    public void setCNAME(String CNAME) {
        this.CNAME = CNAME;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }
}
