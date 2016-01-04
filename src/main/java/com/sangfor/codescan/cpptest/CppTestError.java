/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sangfor.codescan.cpptest;

import java.util.Map;

/**
 *
 * @author liyan
 */
public class CppTestError {

    private String ruleRepoKey;
    private String ruleid;
    private String msg;
    private String locFile;
    private int ln;
    private int locStartln;
    private int locStartPos;
    private int locEndLn;
    private int locEndPos;
    
    private Map flows;
    

    public CppTestError() {

    }

    public CppTestError(final String ruleRepoKey, final String ruleid, final String msg, final String locFile, final int ln, final Map flows) {
        this.ruleRepoKey = ruleRepoKey;
        this.ruleid = ruleid;
        this.msg = msg;
        this.locFile = locFile;
        this.ln = ln;
        this.flows = flows;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getRuleRepoKey());
        s.append("|");
        s.append(getRuleid());
        s.append("|");
        s.append(getMsg());
        s.append("|");
        s.append(getLocFile());
        s.append("(");
        s.append(getLn());
        s.append(")");
        s.append("|");
        //  s.append(getFlow().size());
        return s.toString();
    }

    /**
     * @return the ruleRepoKey
     */
    public String getRuleRepoKey() {
        return ruleRepoKey;
    }

    /**
     * @param ruleRepoKey the ruleRepoKey to set
     */
    public void setRuleRepoKey(String ruleRepoKey) {
        this.ruleRepoKey = ruleRepoKey;
    }

    /**
     * @return the ruleid
     */
    public String getRuleid() {
        return ruleid;
    }

    /**
     * @param ruleid the ruleid to set
     */
    public void setRuleid(String ruleid) {
        this.ruleid = ruleid;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * @return the flow
     */
    public Map getFlows() {
        return flows;
    }

    /**
     * @param flows
     */
    public void setFlows(Map flows) {
        this.flows = flows;
    }

    /**
     * @return the locFile
     */
    public String getLocFile() {
        return locFile;
    }

    /**
     * @param locFile the locFile to set
     */
    public void setLocFile(String locFile) {
        this.locFile = locFile;
    }

    /**
     * @return the ln
     */
    public int getLn() {
        return ln;
    }

    /**
     * @param ln the ln to set
     */
    public void setLn(int ln) {
        this.ln = ln;
    }

    /**
     * @return the locStartln
     */
    public int getLocStartln() {
        return locStartln;
    }

    /**
     * @param locStartln the locStartln to set
     */
    public void setLocStartln(int locStartln) {
        this.locStartln = locStartln;
    }

    /**
     * @return the locStartPos
     */
    public int getLocStartPos() {
        return locStartPos;
    }

    /**
     * @param locStartPos the locStartPos to set
     */
    public void setLocStartPos(int locStartPos) {
        this.locStartPos = locStartPos;
    }

    /**
     * @return the locEndLn
     */
    public int getLocEndLn() {
        return locEndLn;
    }

    /**
     * @param locEndLn the locEndLn to set
     */
    public void setLocEndLn(int locEndLn) {
        this.locEndLn = locEndLn;
    }

    /**
     * @return the locEndPos
     */
    public int getLocEndPos() {
        return locEndPos;
    }

    /**
     * @param locEndPos the locEndPos to set
     */
    public void setLocEndPos(int locEndPos) {
        this.locEndPos = locEndPos;
    }
}
