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
    private String file;
    private int line;
    private Map flows;

    public CppTestError(final String ruleRepoKey, final String ruleid, final String msg, final String file, final int line, final Map flows) {
        this.ruleRepoKey = ruleRepoKey;
        this.ruleid = ruleid;
        this.msg = msg;
        this.file = file;
        this.line = line;
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
        s.append(getFile());
        s.append("(");
        s.append(getLine());
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
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * @return the flow
     */
    public Map getFlows() {
        return flows;
    }

    /**
     * @param flow the flow to set
     */
    public void setFlows(Map flows) {
        this.flows = flows;
    }
}


