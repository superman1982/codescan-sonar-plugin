/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sangfor.codescan.cpptest;

import com.sangfor.codescan.sonarqube.rules.CppTestRulesDefinition;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.sonar.api.internal.google.common.collect.Maps;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CppTestAnalysisResultsParser {

    private static final Logger LOGGER = Loggers.get(CppTestAnalysisResultsParser.class);
    private HashMap pathMap;
    private String baseRoot;

    public CppTestAnalysisResultsParser(String baseRoot) {
        this.baseRoot = baseRoot;
    }

    public List<CppTestError> processReport(File report) throws JDOMException, IOException, JSONException {
        LOGGER.info("Parsing 'CppTest' format");
        List<CppTestError> errors = new ArrayList();
        try {
            List<Element> element = new ArrayList();
            SAXBuilder builder = new SAXBuilder(false);
            Element root = builder.build(report).getRootElement();
            //获取绝对路径映射关系
            this.pathMap = getAbsPathOfCppTestReport(root);

            //获取报告     
            List<Element> codingstandards = root.getChildren("CodingStandards");
            for (Element codingstandard : codingstandards) {
                List<Element> stdviols = codingstandard.getChildren("StdViols");
                for (Element stdviol : stdviols) {
                    element.addAll(stdviol.getChildren("StdViol"));
                    element.addAll(stdviol.getChildren("DupViol"));
                    element.addAll(stdviol.getChildren("FlowViol"));
                }
            }
//处理具体的issue 信息
            for (Element s : element) {
                ArrayList arr = new ArrayList();
                String rule = s.getAttributeValue("rule");
                String locFile = s.getAttributeValue("locFile");
                //替换文件路径
                locFile = convertPath(locFile);
                String ln = s.getAttributeValue("ln");
                String msg = s.getAttributeValue("msg");
                //处理FlowViol 、 DupViol 中的ElDescList
                if (s.getName().equalsIgnoreCase("FlowViol")) {   //数据流分析细节处理
                    arr.addAll(dealFlowViol(s));
                } else if (s.getName().equalsIgnoreCase("DupViol")) {  //重复代码分析细节处理
                    List<Element> list1 = s.getChildren("ElDescList");
                    for (Element e1 : list1) {
                        List<Element> list2 = e1.getChildren("ElDesc");
                        for (Element e2 : list2) {
                            JSONObject json = new JSONObject();
                            json.put("F", convertPath(e2.getAttributeValue("srcRngFile")).replace(baseRoot + "/", ""));
                            json.put("L", e2.getAttributeValue("ln"));
                            arr.add(json);
                        }
                    }
                } else {
                    //没有什么处理
                }
//
//            FilterInfo filterinfo = new FilterInfo(project, context, CxxCppTestRuleRepository.KEY, locFile, ln, rule, msg, arr);
//
//            if (!isFilterDisabled) {
//                //搞一个过滤链，链上有两个过滤器
//                FilterChain chain = new FilterChain();
//                //chain.addFilter(new ArrayOutOfBoundsFilter());
//                chain.addFilter(new SimpleFilters());
//                chain.addFilter(new AdvanceFilters());
//                //开始过滤
//                filterinfo = chain.doFilter(filterinfo);
//                if (filterinfo.getFlag()) {
//                    continue;
//                }
//            }

//            if (isInputValid(filterinfo.getFile(), filterinfo.getLine(), filterinfo.getRuleId(), filterinfo.getMsg())) {
//                //  CxxUtils.LOG.info("this flow is a + " + temMsg);  
//                saveUniqueViolation(filterinfo.getProject(),
//                        filterinfo.getContext(),
//                        filterinfo.getRuleRepoKey(),
//                        filterinfo.getFile(),
//                        filterinfo.getLine(),
//                        filterinfo.getRuleId(),
//                        filterinfo.getMsg(),
//                        filterinfo.getFlow().toString()
//                );
//            } else {
//                CxxUtils.LOG.warn("Skipping invalid violation: '{}'", msg);
//            }
                errors.add(new CppTestError(CppTestRulesDefinition.KEY, rule, msg, locFile, Integer.parseInt(ln), arr));
            }
        } catch (org.jdom.input.JDOMParseException e) {
            // when RATS fails the XML file might be incomplete
            LOGGER.error("Ignore incomplete XML output from CppTest '{}'", e.toString());
        }

        return errors;
    }

    //获取cpptest中代码的绝对路径
    private HashMap getAbsPathOfCppTestReport(Element root) {
        HashMap m = new HashMap();
        List<Element> locations = root.getChildren("Locations");
        for (Element location : locations) {
            List<Element> locs = location.getChildren("Loc");
            for (Element loc : locs) {
                m.put(loc.getAttributeValue("loc"), loc.getAttributeValue("fsPath"));
            }
        }
        return m;
    }

    public List<CppTestError> parse(final File file) throws XMLStreamException {
        LOGGER.info("Parsing file {}", file.getAbsolutePath());

        // as the goal of this example is not to demonstrate how to parse an xml file we return an hard coded list of FooError
        //(final String ruleRepoKey, final String ruleid, final String msg, final String file, final int line,final ArrayList flow) 
        Map<Integer,Map> flows = Maps.newHashMap();
        Map flow = Maps.newHashMap();
        flow.put("file", "main.cpp");
        flow.put("line", "3");
        flow.put("start", "2");
        flow.put("end", "5");
        flow.put("info", "ptr is null ");
        
        Map flow1 = Maps.newHashMap();
        flow1.put("file", "main.cpp");
        flow1.put("line", "3");
        flow1.put("start", "1");
        flow1.put("end", "8");
        flow1.put("info", "ptr is null ");
        
        flows.put(1, flow);
        flows.put(2, flow1);
        
        CppTestError error1 = new CppTestError("cpptest", "BD-RES-FREE", "这个是一个demo的消息，，用于测试的", "main.cpp", 5, flows);
        CppTestError error2 = new CppTestError("cpptest", "BD-RES-INVFREE", "这个是一个demo的消息，，用于测试的", "filelister.cpp", 9, flows);
        // processReport(file);
        // return processReport(file);
        return Arrays.asList(error1, error2);
    }

    private String convertPath(String oldPath) {
        Object newPath = this.pathMap.get(oldPath);
        //     CxxUtils.LOG.warn("CovertPath: " + oldPath + "   \n CovertedPath:" + newPath.toString());
        if (newPath == null) {
            return oldPath == null ? "Source N/A" : oldPath;
        } else {
            return newPath.toString();
        }
    }

    private boolean isInputValid(String suppLoc, String suppLine, String suppRule, String suppMsg) {
        return !StringUtils.isEmpty(suppRule) && !StringUtils.isEmpty(suppMsg);
    }

    //递归处理ElDescList元素  by cpptest report
    private ArrayList dealFlowViol(Element e) throws JSONException {

        ArrayList arr = new ArrayList();
        HashMap m = new HashMap();
        List<Element> list1 = e.getChildren("ElDescList");
        for (Element e1 : list1) {
            List<Element> list2 = e1.getChildren("ElDesc");
            for (Element e2 : list2) {
                JSONObject json = new JSONObject();
                if (e2.getAttributeValue("desc").equalsIgnoreCase("...")) //排除解析到省略行
                {
                    continue;                                               // <ElDesc ElType="." desc="..."><Props /></ElDesc>
                }
                String file = convertPath(e2.getAttributeValue("srcRngFile")).replace(baseRoot + "/", "");
                json.put("F", file);
                json.put("L", e2.getAttributeValue("ln"));
                json.put("D", e2.getAttributeValue("desc"));
                if (!e2.getChild("Props").getChildren().isEmpty()) {
                    json.put("P", " // " + e2.getChild("Props").getChild("Prop").getAttributeValue("key")
                            + " : " + e2.getChild("Props").getChild("Prop").getAttributeValue("val"));
                }
                arr.add(json);
                if (!e2.getChildren("ElDescList").isEmpty()) {
                    arr.addAll(dealFlowViol(e2));
                }
            }
        }
        // CxxUtils.LOG.info("得到issue的完整路径：" + temp.toString());
        return arr;

    }
}
