package com.fdu.uiautomatortest;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;


import com.fdu.uiautomatortest.dao.TransitionGraphDao;
import com.fdu.uiautomatortest.dao.WidgetDao;
import com.fdu.uiautomatortest.model.MenuItem;
import com.fdu.uiautomatortest.model.SubMenu;
import com.fdu.uiautomatortest.model.TransitionGraph;
import com.fdu.uiautomatortest.model.Widget;
import com.fdu.uiautomatortest.model.WindowNode;
import com.fdu.uiautomatortest.utils.DBUtil;
import com.fdu.uiautomatortest.utils.FileUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(AndroidJUnit4.class)
public class DynamicTest {
    private Instrumentation mInstrumentation;
    private UiDevice mUidevice;
    private String mPackage;
    private String mainActivity;
    private UiObject2 dialogBtn;
    private Map<String, Set<Rect>> visitedElements = new HashMap<>();//<Activity, UIElementsPosition>
    private List<UiObject2> triggeredEles;//triggered elements in each page
    private Set<String> visitedACT = new HashSet<>();//visited activities
    private String fText = "abc";
    private String windowLabel = "AnyMemo";
    private final long clickDuration = 1500;
    private final long waitTime = 800;//wait time after execute
    private int width;
    private int height;
    private int length = 0;//scenario length
    private Map<UIElement, VisitInfo> visitMap = new HashMap<>();//<uielement,visitinfo>
    private List<Widget> staticWid = new ArrayList<>();//already visit widget from static
    private List<MenuItem> staticMenuItem = new ArrayList<>();//already visit menuitem from static
    private List<SubMenu> staticSubMenu = new ArrayList<>();//already visit submenu from static
    private long scenarioTimeout = 10000;
    private long testTimeout = 1000 * 60 * 40;
    private Integer frequencyCap = 5;

    @Before
    public void setUp() throws IOException {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
        mUidevice = UiDevice.getInstance(mInstrumentation);
        mUidevice.setCompressedLayoutHeirarchy(true);
        mPackage = mUidevice.getCurrentPackageName();
        mainActivity = getRunningActivity();
        //fText = LoadProperties.get("TEXT");
        width = mUidevice.getDisplayWidth();
        height = mUidevice.getDisplayHeight();
    }

    @Test
    public void test() throws InterruptedException, IOException {
        int exploreTime = 0;
        List<UiObject2> elements = mUidevice.findObjects(By.clickable(true));
        for (UiObject2 element : elements) {
            if (!containUIElement(getRunningActivity(), element) && !isBack(element) && !isEditText(element)) {
                exploreTime++;
            }
        }
        while (exploreTime != 0) {
            dynamicExplore();
            exploreTime--;
        }
    }

//    @Test
//    public void dfsStatic2() {
//        boolean staticFlag = true;
//        String type = "ACT";
//        long startTime = System.currentTimeMillis();
//        long endTime = startTime;
//        while (endTime - startTime <= testTimeout) {
//            String curACT = getRunningActivity();
//            if (isMenu()) {
//                type = "Menu";
//            } else if (isDialog()) {
//                type = "Dialog";
//            }
//            TransitionEdgeDao edgeDao = new TransitionEdgeDao();
//            List<Map<String, Object>> staticResults = edgeDao.findTransitionResults(windowLabel, curACT, type);
//            if (staticResults.isEmpty() || !staticFlag) {
//                staticFlag = true;//dynamic successfully, set flag
//                List<UiObject2> clickableElements = mUidevice.findObjects(By.clickable(true));
//                if (clickableElements.isEmpty()) {
//                    back();
//                }
//                if (visitMap.isEmpty()) {
//                    UiObject2 firEle = clickableElements.get(0);
//                    String text = firEle.getText();
//                    String resourceId = firEle.getResourceName();
//                    String contentDesc = firEle.getContentDescription();
//                    String classType = firEle.getClassName();
//                    Rect visBounds = firEle.getVisibleBounds();
//                    String hostACT = curACT;
//                    UIElement firUIEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
//                    fill_data(firEle);
//                    executeEvent(firEle, "click");
//                    VisitInfo firVisit = new VisitInfo();
//                    if (isDialog()) {
//                        firVisit.setOpenType(1);
//                        firVisit.setVisit(false);
//                    } else if (isMenu()) {
//                        firVisit.setOpenType(2);
//                        firVisit.setVisit(false);
//                    }
//                    Integer nextFrequency = firVisit.getFrequency() + 1;
//                    firVisit.setFrequency(nextFrequency);
//                    visitMap.put(firUIEle, firVisit);
//                } else {
//                    if (gotoAnotherApp()) {
//                        back();
//                        String curPack = mUidevice.getCurrentPackageName();
//                        if (curPack.equals(mPackage)) {
//                            continue;
//                        } else {
//                            restartApp(mPackage, mainActivity);
//                            continue;
//                        }
//                    }
//                    for (UiObject2 cele : clickableElements) {
//                        String text = cele.getText();
//                        String resourceId = cele.getResourceName();
//                        String contentDesc = cele.getContentDescription();
//                        String classType = cele.getClassName();
//                        Rect visBounds = cele.getVisibleBounds();
//                        String hostACT = curACT;
//                        boolean isEditText = isEditText(cele);
//                        UIElement uiEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
//                        if (!visitMap.containsKey(uiEle)) {
//                            fill_data(cele);
//                            executeEvent(cele, "click");
//                            String dynamicInfo = "dynamic: "+hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                            Log.i("TestInfo",dynamicInfo);
//                            VisitInfo subVisit = new VisitInfo();
//                            if (isDialog() && !isEditText) {
//                                subVisit.setOpenType(1);
//                                subVisit.setVisit(false);
//                            } else if (isMenu() && !isEditText) {
//                                subVisit.setOpenType(2);
//                                subVisit.setVisit(false);
//                            }
//                            Integer nextFrequency = subVisit.getFrequency() + 1;
//                            subVisit.setFrequency(nextFrequency);
//                            visitMap.put(uiEle, subVisit);
//                            break;
//                        } else {
//                            VisitInfo dmVisit = visitMap.get(uiEle);
//                            if (!dmVisit.getVisit()) {//to open menu or dialog
//                                Log.d("open type", dmVisit.getOpenType() + "");
//                                executeEvent(cele, "click");
//                                String dynamicInfo = "dynamic: "+hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                                Log.i("TestInfo",dynamicInfo);
//                                Boolean dmFlag = true;
//                                List<UiObject2> dmElements = mUidevice.findObjects(By.clickable(true));//elements in menu or dialog
//                                for (UiObject2 dmEle : dmElements) {
//                                    String dmText = dmEle.getText();
//                                    String dmResourceId = dmEle.getResourceName();
//                                    String dmContentDesc = dmEle.getContentDescription();
//                                    String dmClassType = dmEle.getClassName();
//                                    Rect dmVisBounds = dmEle.getVisibleBounds();
//                                    String dmHostACT = getRunningActivity();
//                                    UIElement dm = new UIElement(dmText, dmResourceId, dmContentDesc, dmClassType, dmVisBounds, dmHostACT);
//                                    if (!visitMap.containsKey(dm)) {
//                                        dmFlag = false;
//                                        break;
//                                    } else {
//                                        dmFlag = dmFlag && visitMap.get(dm).getVisit();
//                                    }
//                                }
//                                Integer nextFrequency = dmVisit.getFrequency() + 1;
//                                dmVisit.setFrequency(nextFrequency);
//                                dmVisit.setVisit(dmFlag);
//                                if (dmVisit.getFrequency() > frequencyCap) {
//                                    dmVisit.setVisit(true);
//                                }
//                                break;
//                            }
//                        }
//                        if (clickableElements.indexOf(cele) == clickableElements.size() - 1) {
//                            String curAct = getRunningActivity();
//                            if (((mainActivity.equals(curAct))) && !isDialog() && !isMenu()) {
//                                UiObject2 optElement = findOptimalElement(clickableElements);
//                                String opt_text = optElement.getText();
//                                String opt_resourceId = optElement.getResourceName();
//                                String opt_contentDesc = optElement.getContentDescription();
//                                String opt_classType = optElement.getClassName();
//                                Rect opt_visBounds = optElement.getVisibleBounds();
//                                String opt_hostACT = curAct;
//                                UIElement opt_uiElement = new UIElement(opt_text, opt_resourceId, opt_contentDesc, opt_classType, opt_visBounds, opt_hostACT);
//                                VisitInfo opt_info = visitMap.get(opt_uiElement);
//                                executeEvent(optElement, "click");
//                                String dynamicInfo = "dynamic: "+ opt_hostACT + " " + opt_visBounds.toString() + " id: " + opt_resourceId;
//                                Log.i("TestInfo",dynamicInfo);
//                                opt_info.setFrequency(opt_info.getFrequency() + 1);
//                                visitMap.put(opt_uiElement, opt_info);
//                                break;
//                            } else {
//                                back();
//                                break;
//                            }
//                        }
//                    }
//                }
//            } else {
//                String event = "click";
//                String widgetId = "";
//                UiObject2 sWidget = null;
//                for (Map<String, Object> r : staticResults) {
//                    event = (String) r.get("event");
//                    widgetId = (String) r.get("widgetId");
//                    //widgetId = mPackage + ":id/" + widgetId;
//                    widgetId = "android:id/" + widgetId;
//                    if (mUidevice.hasObject(By.res(widgetId))) {
//                        List<UiObject2> sWidgets = mUidevice.findObjects(By.res(widgetId));
//                        sWidget = sWidgets.get(sWidgets.size() / 2);
//                        //sWidget = mUidevice.findObject(By.res(widgetId));
//                        String text = sWidget.getText();
//                        String resourceId = sWidget.getResourceName();
//                        String contentDesc = sWidget.getContentDescription();
//                        String classType = sWidget.getClassName();
//                        Rect visBounds = sWidget.getVisibleBounds();
//                        String hostACT = curACT;
//                        UIElement suie = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
//                        if (!visitMap.containsKey(suie)) {
//                            executeEvent(sWidget, event);
//                            String staticInfo = "static: "+hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                            Log.i("TestInfo",staticInfo);
//                            VisitInfo s_visit = new VisitInfo();
//                            if (isDialog()) {
//                                s_visit.setOpenType(1);
//                                s_visit.setVisit(false);
//                            } else if (isMenu()) {
//                                s_visit.setOpenType(2);
//                                s_visit.setVisit(false);
//                            }
//                            Integer nextFrequency = s_visit.getFrequency() + 1;
//                            s_visit.setFrequency(nextFrequency);
//                            visitMap.put(suie, s_visit);
//                            staticFlag = true;//execute static successfully, set flag
//                            break;
//                        } else {
//                            VisitInfo sInfo = visitMap.get(suie);
//                            if((sInfo.getFrequency()==1 && sInfo.getOpenType()==0 && event.equals("long_click")) ||
//                                    (sInfo.getFrequency()==1 && sInfo.getOpenType()!=0 && event.equals("click"))){
//                                executeEvent(sWidget, event);
//                                String staticInfo = "static: "+hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                                Log.i("TestInfo",staticInfo);
//                                VisitInfo s_visit = new VisitInfo();
//                                if (isDialog()) {
//                                    s_visit.setOpenType(1);
//                                    s_visit.setVisit(false);
//                                } else if (isMenu()) {
//                                    s_visit.setOpenType(2);
//                                    s_visit.setVisit(false);
//                                }
//                                Integer nextFrequency = s_visit.getFrequency() + 1;
//                                s_visit.setFrequency(nextFrequency);
//                                visitMap.put(suie, s_visit);
//                                staticFlag = true;//execute static successfully, set flag
//                                break;
//                            }else{
//                                staticFlag = false;
//                                continue;
//                            }
//                            //staticFlag = false;//does not execute static, set flag
//                            //continue;
//                        }
//                    } else {
//                        staticFlag = false;//does not execute static, set flag
//                        continue;
//                    }
//                }
//            }
//            endTime = System.currentTimeMillis();
//        }
//    }

//    @Test
//    public void dfsStatic1() {
//        boolean staticFlag = true;
//        String type = "ACT";
//        long startTime = System.currentTimeMillis();
//        long endTime = startTime;
//        while (endTime - startTime <= testTimeout) {
//            String curACT = getRunningActivity();
//            if (isMenu()) {
//                type = "MENU";
//            } else if (isDialog()) {
//                type = "DIALOG";
//            }
//            TransitionGraphDao graphDao = new TransitionGraphDao();
//            TransitionGraph g = graphDao.getTransitionGraph(windowLabel);
//            List<Map<String, String>> staticResults = graphDao.findTransitionResults(g, curACT, type, mPackage);
//            if (staticResults.isEmpty() || !staticFlag) {
//                staticFlag = true;//dynamic successfully, set flag
//                List<UiObject2> clickableElements = mUidevice.findObjects(By.clickable(true));
//                if (clickableElements.isEmpty()) {
//                    back();
//                }
//                if (visitMap.isEmpty()) {
//                    UiObject2 firEle = clickableElements.get(0);
//                    String text = firEle.getText();
//                    String resourceId = firEle.getResourceName();
//                    String contentDesc = firEle.getContentDescription();
//                    String classType = firEle.getClassName();
//                    Rect visBounds = firEle.getVisibleBounds();
//                    String hostACT = curACT;
//                    UIElement firUIEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
//                    fill_data(firEle);
//                    executeEvent(firEle, "click");
//                    VisitInfo firVisit = new VisitInfo();
//                    if (isDialog()) {
//                        firVisit.setOpenType(1);
//                        firVisit.setVisit(false);
//                    } else if (isMenu()) {
//                        firVisit.setOpenType(2);
//                        firVisit.setVisit(false);
//                    }
//                    Integer nextFrequency = firVisit.getFrequency() + 1;
//                    firVisit.setFrequency(nextFrequency);
//                    visitMap.put(firUIEle, firVisit);
//                } else {
//                    if (gotoAnotherApp()) {
//                        back();
//                        String curPack = mUidevice.getCurrentPackageName();
//                        if (curPack.equals(mPackage)) {
//                            continue;
//                        } else {
//                            restartApp(mPackage, mainActivity);
//                            continue;
//                        }
//                    }
//                    for (UiObject2 cele : clickableElements) {
//                        String text = cele.getText();
//                        String resourceId = cele.getResourceName();
//                        String contentDesc = cele.getContentDescription();
//                        String classType = cele.getClassName();
//                        Rect visBounds = cele.getVisibleBounds();
//                        String hostACT = curACT;
//                        boolean isEditText = isEditText(cele);
//                        UIElement uiEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
//                        if (!visitMap.containsKey(uiEle)) {
//                            fill_data(cele);
//                            executeEvent(cele, "click");
//                            String dynamicInfo = "dynamic: " + hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                            Log.i("TestInfo", dynamicInfo);
//                            VisitInfo subVisit = new VisitInfo();
//                            if (isDialog() && !isEditText) {
//                                subVisit.setOpenType(1);
//                                subVisit.setVisit(false);
//                            } else if (isMenu() && !isEditText) {
//                                subVisit.setOpenType(2);
//                                subVisit.setVisit(false);
//                            }
//                            Integer nextFrequency = subVisit.getFrequency() + 1;
//                            subVisit.setFrequency(nextFrequency);
//                            visitMap.put(uiEle, subVisit);
//                            break;
//                        } else {
//                            VisitInfo dmVisit = visitMap.get(uiEle);
//                            if (!dmVisit.getVisit()) {//to open menu or dialog
//                                Log.d("open type", dmVisit.getOpenType() + "");
//                                executeEvent(cele, "click");
//                                String dynamicInfo = "dynamic: " + hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                                Log.i("TestInfo", dynamicInfo);
//                                Boolean dmFlag = true;
//                                List<UiObject2> dmElements = mUidevice.findObjects(By.clickable(true));//elements in menu or dialog
//                                for (UiObject2 dmEle : dmElements) {
//                                    String dmText = dmEle.getText();
//                                    String dmResourceId = dmEle.getResourceName();
//                                    String dmContentDesc = dmEle.getContentDescription();
//                                    String dmClassType = dmEle.getClassName();
//                                    Rect dmVisBounds = dmEle.getVisibleBounds();
//                                    String dmHostACT = getRunningActivity();
//                                    UIElement dm = new UIElement(dmText, dmResourceId, dmContentDesc, dmClassType, dmVisBounds, dmHostACT);
//                                    if (!visitMap.containsKey(dm)) {
//                                        dmFlag = false;
//                                        break;
//                                    } else {
//                                        dmFlag = dmFlag && visitMap.get(dm).getVisit();
//                                    }
//                                }
//                                Integer nextFrequency = dmVisit.getFrequency() + 1;
//                                dmVisit.setFrequency(nextFrequency);
//                                dmVisit.setVisit(dmFlag);
//                                if (dmVisit.getFrequency() > frequencyCap) {
//                                    dmVisit.setVisit(true);
//                                }
//                                break;
//                            }
//                        }
//                        if (clickableElements.indexOf(cele) == clickableElements.size() - 1) {
//                            String curAct = getRunningActivity();
//                            if (((mainActivity.equals(curAct))) && !isDialog() && !isMenu()) {
//                                UiObject2 optElement = findOptimalElement(clickableElements);
//                                String opt_text = optElement.getText();
//                                String opt_resourceId = optElement.getResourceName();
//                                String opt_contentDesc = optElement.getContentDescription();
//                                String opt_classType = optElement.getClassName();
//                                Rect opt_visBounds = optElement.getVisibleBounds();
//                                String opt_hostACT = curAct;
//                                UIElement opt_uiElement = new UIElement(opt_text, opt_resourceId, opt_contentDesc, opt_classType, opt_visBounds, opt_hostACT);
//                                VisitInfo opt_info = visitMap.get(opt_uiElement);
//                                executeEvent(optElement, "click");
//                                String dynamicInfo = "dynamic: " + opt_hostACT + " " + opt_visBounds.toString() + " id: " + opt_resourceId;
//                                Log.i("TestInfo", dynamicInfo);
//                                opt_info.setFrequency(opt_info.getFrequency() + 1);
//                                visitMap.put(opt_uiElement, opt_info);
//                                break;
//                            } else {
//                                back();
//                                break;
//                            }
//                        }
//                    }
//                }
//            } else {
//                String event = "click";
//                String widgetId = "";
//                UiObject2 sWidget = null;
//                for (Map<String, String> r : staticResults) {
//                    event = r.get("event");
//                    widgetId = r.get("widgetId");
//                    widgetId = mPackage + ":id/" + widgetId;
//                    if (!mUidevice.hasObject(By.res(widgetId))) {
//                        widgetId = "android:id/" + widgetId;
//                    }
//                    if (mUidevice.hasObject(By.res(widgetId))) {
//                        List<UiObject2> sWidgets = mUidevice.findObjects(By.res(widgetId));
//                        sWidget = sWidgets.get(sWidgets.size() / 2);
//                        //sWidget = mUidevice.findObject(By.res(widgetId));
//                        String text = sWidget.getText();
//                        String resourceId = sWidget.getResourceName();
//                        String contentDesc = sWidget.getContentDescription();
//                        String classType = sWidget.getClassName();
//                        Rect visBounds = sWidget.getVisibleBounds();
//                        String hostACT = curACT;
//                        UIElement suie = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
//                        if (!visitMap.containsKey(suie)) {
//                            executeEvent(sWidget, event);
//                            String staticInfo = "static: " + hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                            Log.i("TestInfo", staticInfo);
//                            VisitInfo s_visit = new VisitInfo();
//                            if (isDialog()) {
//                                s_visit.setOpenType(1);
//                                s_visit.setVisit(false);
//                            } else if (isMenu()) {
//                                s_visit.setOpenType(2);
//                                s_visit.setVisit(false);
//                            }
//                            Integer nextFrequency = s_visit.getFrequency() + 1;
//                            s_visit.setFrequency(nextFrequency);
//                            visitMap.put(suie, s_visit);
//                            staticFlag = true;//execute static successfully, set flag
//                            break;
//                        } else {
//                            VisitInfo sInfo = visitMap.get(suie);
//                            if ((sInfo.getFrequency() == 1 && sInfo.getOpenType() == 0 && event.equals("long_click")) ||
//                                    (sInfo.getFrequency() == 1 && sInfo.getOpenType() != 0 && event.equals("click"))) {
//                                executeEvent(sWidget, event);
//                                String staticInfo = "static: " + hostACT + " " + visBounds.toString() + " id: " + resourceId;
//                                Log.i("TestInfo", staticInfo);
//                                VisitInfo s_visit = new VisitInfo();
//                                if (isDialog()) {
//                                    s_visit.setOpenType(1);
//                                    s_visit.setVisit(false);
//                                } else if (isMenu()) {
//                                    s_visit.setOpenType(2);
//                                    s_visit.setVisit(false);
//                                }
//                                Integer nextFrequency = s_visit.getFrequency() + 1;
//                                s_visit.setFrequency(nextFrequency);
//                                visitMap.put(suie, s_visit);
//                                staticFlag = true;//execute static successfully, set flag
//                                break;
//                            } else {
//                                staticFlag = false;
//                                continue;
//                            }
//                            //staticFlag = false;//does not execute static, set flag
//                            //continue;
//                        }
//                    } else {
//                        staticFlag = false;//does not execute static, set flag
//                        continue;
//                    }
//                }
//            }
//            endTime = System.currentTimeMillis();
//        }
//    }

    @Test
    public void dfsStatic() {
        boolean staticFlag = true;
        String type = "ACT";
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        TransitionGraphDao graphDao = new TransitionGraphDao();
        TransitionGraph g = graphDao.getTransitionGraph(windowLabel);
        while (endTime - startTime <= testTimeout) {
            String curACT = getRunningActivity();
            if (isMenu()) {
                type = "MENU";
            } else if (isDialog()) {
                type = "DIALOG";
            }
            WindowNode node = graphDao.getNodeByCurWindow(g, curACT, type, mPackage);
            if (node == null || !staticFlag) {
                staticFlag = true;//dynamic successfully, set flag
                List<UiObject2> clickableElements = mUidevice.findObjects(By.clickable(true));
                if (clickableElements.isEmpty()) {
                    back();
                }
                if (visitMap.isEmpty()) {
                    UiObject2 firEle = clickableElements.get(0);
                    String text = firEle.getText();
                    String resourceId = firEle.getResourceName();
                    String contentDesc = firEle.getContentDescription();
                    String classType = firEle.getClassName();
                    Rect visBounds = firEle.getVisibleBounds();
                    String hostACT = curACT;
                    UIElement firUIEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
                    fill_data(firEle);
                    executeEvent(firEle, "click");
                    VisitInfo firVisit = new VisitInfo();
                    if (isDialog()) {
                        firVisit.setOpenType(1);
                        firVisit.setVisit(false);
                    } else if (isMenu()) {
                        firVisit.setOpenType(2);
                        firVisit.setVisit(false);
                    }
                    Integer nextFrequency = firVisit.getFrequency() + 1;
                    firVisit.setFrequency(nextFrequency);
                    visitMap.put(firUIEle, firVisit);
                } else {
                    if (gotoAnotherApp()) {
                        back();
                        String curPack = mUidevice.getCurrentPackageName();
                        if (curPack.equals(mPackage)) {
                            continue;
                        } else {
                            restartApp(mPackage, mainActivity);
                            continue;
                        }
                    }
                    for (UiObject2 cele : clickableElements) {
                        String text = cele.getText();
                        String resourceId = cele.getResourceName();
                        String contentDesc = cele.getContentDescription();
                        String classType = cele.getClassName();
                        Rect visBounds = cele.getVisibleBounds();
                        String hostACT = curACT;
                        boolean isEditText = isEditText(cele);
                        UIElement uiEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
                        if (!visitMap.containsKey(uiEle)) {
                            fill_data(cele);
                            executeEvent(cele, "click");
                            String dynamicInfo = "dynamic: " + hostACT + " " + visBounds.toString() + " id: " + resourceId;
                            Log.i("TestInfo", dynamicInfo);
                            VisitInfo subVisit = new VisitInfo();
                            if (isDialog() && !isEditText) {
                                subVisit.setOpenType(1);
                                subVisit.setVisit(false);
                            } else if (isMenu() && !isEditText) {
                                subVisit.setOpenType(2);
                                subVisit.setVisit(false);
                            }
                            Integer nextFrequency = subVisit.getFrequency() + 1;
                            subVisit.setFrequency(nextFrequency);
                            visitMap.put(uiEle, subVisit);
                            break;
                        } else {
                            VisitInfo dmVisit = visitMap.get(uiEle);
                            if (!dmVisit.getVisit()) {//to open menu or dialog
                                Log.d("open type", dmVisit.getOpenType() + "");
                                executeEvent(cele, "click");
                                String dynamicInfo = "dynamic: " + hostACT + " " + visBounds.toString() + " id: " + resourceId;
                                Log.i("TestInfo", dynamicInfo);
                                Boolean dmFlag = true;
                                List<UiObject2> dmElements = mUidevice.findObjects(By.clickable(true));//elements in menu or dialog
                                for (UiObject2 dmEle : dmElements) {
                                    String dmText = dmEle.getText();
                                    String dmResourceId = dmEle.getResourceName();
                                    String dmContentDesc = dmEle.getContentDescription();
                                    String dmClassType = dmEle.getClassName();
                                    Rect dmVisBounds = dmEle.getVisibleBounds();
                                    String dmHostACT = getRunningActivity();
                                    UIElement dm = new UIElement(dmText, dmResourceId, dmContentDesc, dmClassType, dmVisBounds, dmHostACT);
                                    if (!visitMap.containsKey(dm)) {
                                        dmFlag = false;
                                        break;
                                    } else {
                                        dmFlag = dmFlag && visitMap.get(dm).getVisit();
                                    }
                                }
                                Integer nextFrequency = dmVisit.getFrequency() + 1;
                                dmVisit.setFrequency(nextFrequency);
                                dmVisit.setVisit(dmFlag);
                                if (dmVisit.getFrequency() > frequencyCap) {
                                    dmVisit.setVisit(true);
                                }
                                break;
                            }
                        }
                        if (clickableElements.indexOf(cele) == clickableElements.size() - 1) {
                            String curAct = getRunningActivity();
                            if (((mainActivity.equals(curAct))) && !isDialog() && !isMenu()) {
                                UiObject2 optElement = findOptimalElement(clickableElements);
                                String opt_text = optElement.getText();
                                String opt_resourceId = optElement.getResourceName();
                                String opt_contentDesc = optElement.getContentDescription();
                                String opt_classType = optElement.getClassName();
                                Rect opt_visBounds = optElement.getVisibleBounds();
                                String opt_hostACT = curAct;
                                UIElement opt_uiElement = new UIElement(opt_text, opt_resourceId, opt_contentDesc, opt_classType, opt_visBounds, opt_hostACT);
                                VisitInfo opt_info = visitMap.get(opt_uiElement);
                                executeEvent(optElement, "click");
                                String dynamicInfo = "dynamic: " + opt_hostACT + " " + opt_visBounds.toString() + " id: " + opt_resourceId;
                                Log.i("TestInfo", dynamicInfo);
                                opt_info.setFrequency(opt_info.getFrequency() + 1);
                                visitMap.put(opt_uiElement, opt_info);
                                break;
                            } else {
                                back();
                                break;
                            }
                        }
                    }
                }
            } else {
                if (1 == node.getHasOptionsMenu()) {
                    WindowNode menuNode = node.getOptionsMenuNode();
                    List<Widget> staticMenuWids = new ArrayList<>();
                    staticMenuWids.addAll(staticMenuItem);
                    staticMenuWids.addAll(staticSubMenu);
                    if (staticMenuWids.containsAll(menuNode.getWidgets())) {//all menu widgets are visited
                        List<Widget> widgets = node.getWidgets();
                        if(widgets.isEmpty()){
                            staticFlag = false;
                        }
                        for (Widget w : widgets) {
                            if (!staticWid.contains(w)) {
                                String res_id = w.getWidgetId();
                                if (res_id != null) {
                                    String res_id_d = mPackage + ":id/" + res_id;
                                    if (!mUidevice.hasObject(By.res(res_id_d))) {
                                        res_id_d = "android:id/" + res_id;
                                    }
                                    if (mUidevice.hasObject(By.res(res_id_d))) {
                                        UiObject2 w_d = mUidevice.findObject(By.res(res_id_d));
                                        String text = w_d.getText();
                                        String resId = w_d.getResourceName();
                                        String contentDesc = w_d.getContentDescription();
                                        String classType = w_d.getClassName();
                                        Rect visBounds = w_d.getVisibleBounds();
                                        String hostACT = getRunningActivity();
                                        executeEvent(w_d, w.getEvent());
                                        staticFlag = true;
                                        staticWid.add(w);
                                        Log.i("TestInfo", "static: widget " + w.getWidgetType() + " " + w.getEvent() + " " + node.getName());
                                        if(!isMenu() && !isDialog()){
                                            UIElement element = new UIElement(text,resId,contentDesc,classType,visBounds,hostACT);
                                            VisitInfo info = new VisitInfo();
                                            Integer frequency = info.getFrequency() + 1;
                                            info.setFrequency(frequency);
                                            visitMap.put(element,info);
                                        }
                                        //saveExecuteWidget(w_d);
                                        break;
                                    } else {
                                        staticWid.add(w);
                                        staticFlag = false;
                                    }
                                } else {
                                    staticWid.add(w);
                                    staticFlag = false;
                                }
                            } else {
                                staticFlag = false;
                            }
                        }
                    } else {
                        openMenu();
                        if(!isMenu()){
                            staticFlag = false;
                            continue;
                        }
                        String staticInfo = "static: menu " + menuNode.getName();
                        Log.i("TestInfo", staticInfo);
                        List<Widget> menuWidgets = menuNode.getWidgets();
                        for (Widget menuWidget : menuWidgets) {
                            if (menuWidget instanceof SubMenu) {
                                SubMenu sub = (SubMenu) menuWidget;
                                if (staticMenuItem.containsAll(sub.getItems())) {
                                    staticSubMenu.add(sub);
                                }
                                if (!staticSubMenu.contains(sub)) {
                                    String text = sub.getText();
                                    if (text != null) {
                                        if (mUidevice.hasObject(By.text(text))) {
                                            UiObject2 sub_d = mUidevice.findObject(By.text(text));
                                            executeEvent(sub_d, sub.getEvent());
                                            Log.i("TestInfo", "static: sub_menu " + text + " " + menuNode.getName());
                                            List<MenuItem> subItems = sub.getItems();
                                            for (MenuItem subItem : subItems) {
                                                if (!staticMenuItem.contains(subItem)) {
                                                    String subText = subItem.getText();
                                                    if (subText != null) {
                                                        if (mUidevice.hasObject(By.text(subText))) {
                                                            UiObject2 subItem_d = mUidevice.findObject(By.text(subText));
                                                            executeEvent(subItem_d, subItem.getEvent());
                                                            staticFlag = true;
                                                            staticMenuItem.add(subItem);
                                                            Log.i("TestInfo", "static: sub_menu_item " + subText + " " + menuNode.getName());
                                                            break;
                                                        } else {
                                                            staticMenuItem.add(subItem);
                                                            staticFlag = false;
                                                        }
                                                    } else {
                                                        staticMenuItem.add(subItem);
                                                        staticFlag = false;
                                                    }
                                                } else {
                                                    staticFlag = false;
                                                }
                                            }
                                        } else {
                                            staticSubMenu.add(sub);
                                            staticFlag = false;
                                        }
                                    } else {
                                        staticSubMenu.add(sub);
                                        staticFlag = false;
                                    }
                                } else {
                                    staticFlag = false;
                                }
                            }
                            if (menuWidget instanceof MenuItem) {
                                MenuItem item = (MenuItem) menuWidget;
                                if (!staticMenuItem.contains(item)) {
                                    String text = item.getText();
                                    if (text != null) {
                                        if (mUidevice.hasObject(By.text(text))) {
                                            UiObject2 item_d = mUidevice.findObject(By.text(text));
                                            executeEvent(item_d, item.getEvent());
                                            staticFlag = true;
                                            staticMenuItem.add(item);
                                            Log.i("TestInfo", "static: menu_item " + text + " " + menuNode.getName());
                                            break;
                                        } else {
                                            staticMenuItem.add(item);
                                            staticFlag = false;
                                        }
                                    } else {
                                        staticMenuItem.add(item);
                                        staticFlag = false;
                                    }
                                } else {
                                    staticFlag = false;
                                }
                            }
                        }
                    }
                } else {
                    List<Widget> widgets = node.getWidgets();
                    if(widgets.isEmpty()){
                        staticFlag = false;
                    }
                    for (Widget w : widgets) {
                        if (!staticWid.contains(w)) {
                            String res_id = w.getWidgetId();
                            if (res_id != null) {
                                String res_id_d = mPackage + ":id/" + res_id;
                                if (!mUidevice.hasObject(By.res(res_id_d))) {
                                    res_id_d = "android:id/" + res_id;
                                }
                                if (mUidevice.hasObject(By.res(res_id_d))) {
                                    UiObject2 w_d = mUidevice.findObject(By.res(res_id_d));
                                    String text = w_d.getText();
                                    String resId = w_d.getResourceName();
                                    String contentDesc = w_d.getContentDescription();
                                    String classType = w_d.getClassName();
                                    Rect visBounds = w_d.getVisibleBounds();
                                    String hostACT = getRunningActivity();
                                    executeEvent(w_d, w.getEvent());
                                    staticFlag = true;
                                    staticWid.add(w);
                                    Log.i("TestInfo", "static: widget " + w.getWidgetType() + " " + w.getEvent() + " " + node.getName());
                                    //saveExecuteWidget(w_d);
                                    if(!isMenu() && !isDialog()){
                                        UIElement element = new UIElement(text,resId,contentDesc,classType,visBounds,hostACT);
                                        VisitInfo info = new VisitInfo();
                                        Integer frequency = info.getFrequency() + 1;
                                        info.setFrequency(frequency);
                                        visitMap.put(element,info);
                                    }
                                    break;
                                } else {
                                    staticWid.add(w);
                                    staticFlag = false;
                                }
                            } else {
                                staticWid.add(w);
                                staticFlag = false;
                            }
                        } else {
                            staticFlag = false;
                        }
                    }
                }
            }
            endTime = System.currentTimeMillis();
        }
    }

    @Test
    public void dfsExplore() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while (endTime - startTime <= testTimeout) {
            List<UiObject2> clickableElements = mUidevice.findObjects(By.clickable(true));
            if (clickableElements.isEmpty()) {
                back();
            }
            if (visitMap.isEmpty()) {
                UiObject2 firEle = clickableElements.get(0);
                String text = firEle.getText();
                String resourceId = firEle.getResourceName();
                String contentDesc = firEle.getContentDescription();
                String classType = firEle.getClassName();
                Rect visBounds = firEle.getVisibleBounds();
                String hostACT = getRunningActivity();
                UIElement firUIEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
                fill_data(firEle);
                executeEvent(firEle, "click");
                VisitInfo firVisit = new VisitInfo();
                if (isDialog()) {
                    firVisit.setOpenType(1);
                    firVisit.setVisit(false);
                } else if (isMenu()) {
                    firVisit.setOpenType(2);
                    firVisit.setVisit(false);
                }
                Integer nextFrequency = firVisit.getFrequency() + 1;
                firVisit.setFrequency(nextFrequency);
                visitMap.put(firUIEle, firVisit);
            } else {
                if (gotoAnotherApp()) {
                    back();
                    String curPack = mUidevice.getCurrentPackageName();
                    if (curPack.equals(mPackage)) {
                        continue;
                    } else {
                        restartApp(mPackage, mainActivity);
                        continue;
                    }
                }
                for (UiObject2 cele : clickableElements) {
                    String text = cele.getText();
                    String resourceId = cele.getResourceName();
                    String contentDesc = cele.getContentDescription();
                    String classType = cele.getClassName();
                    Rect visBounds = cele.getVisibleBounds();
                    String hostACT = getRunningActivity();
                    boolean isEditText = isEditText(cele);
                    UIElement uiEle = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
                    if (!visitMap.containsKey(uiEle)) {
                        fill_data(cele);
                        executeEvent(cele, "click");
                        VisitInfo subVisit = new VisitInfo();
                        if (isDialog() && !isEditText) {
                            subVisit.setOpenType(1);
                            subVisit.setVisit(false);
                        } else if (isMenu() && !isEditText) {
                            subVisit.setOpenType(2);
                            subVisit.setVisit(false);
                        }
                        Integer nextFrequency = subVisit.getFrequency() + 1;
                        subVisit.setFrequency(nextFrequency);
                        visitMap.put(uiEle, subVisit);
                        break;
                    } else {
                        VisitInfo dmVisit = visitMap.get(uiEle);
                        if (!dmVisit.getVisit()) {
                            Log.d("open type", dmVisit.getOpenType() + "");
                            executeEvent(cele, "click");
                            Boolean dmFlag = true;//
                            List<UiObject2> dmElements = mUidevice.findObjects(By.clickable(true));
                            for (UiObject2 dmEle : dmElements) {
                                String dmText = dmEle.getText();
                                String dmResourceId = dmEle.getResourceName();
                                String dmContentDesc = dmEle.getContentDescription();
                                String dmClassType = dmEle.getClassName();
                                Rect dmVisBounds = dmEle.getVisibleBounds();
                                String dmHostACT = getRunningActivity();
                                UIElement dm = new UIElement(dmText, dmResourceId, dmContentDesc, dmClassType, dmVisBounds, dmHostACT);
                                if (!visitMap.containsKey(dm)) {
                                    dmFlag = false;
                                    break;
                                } else {
                                    dmFlag = dmFlag && visitMap.get(dm).getVisit();
                                }
                            }
                            Integer nextFrequency = dmVisit.getFrequency() + 1;
                            dmVisit.setFrequency(nextFrequency);
                            dmVisit.setVisit(dmFlag);
                            if (dmVisit.getFrequency() > frequencyCap) {
                                dmVisit.setVisit(true);
                            }
                            break;
                        }
                    }
                    if (clickableElements.indexOf(cele) == clickableElements.size() - 1) {
                        String curAct = getRunningActivity();
                        if (((mainActivity.equals(curAct))) && !isDialog() && !isMenu()) {
                            UiObject2 optElement = findOptimalElement(clickableElements);
                            String opt_text = optElement.getText();
                            String opt_resourceId = optElement.getResourceName();
                            String opt_contentDesc = optElement.getContentDescription();
                            String opt_classType = optElement.getClassName();
                            Rect opt_visBounds = optElement.getVisibleBounds();
                            String opt_hostACT = curAct;
                            UIElement opt_uiElement = new UIElement(opt_text, opt_resourceId, opt_contentDesc, opt_classType, opt_visBounds, opt_hostACT);
                            VisitInfo opt_info = visitMap.get(opt_uiElement);
                            executeEvent(optElement, "click");
                            opt_info.setFrequency(opt_info.getFrequency() + 1);
                            visitMap.put(opt_uiElement, opt_info);
                            break;
                        } else {
                            back();
                            break;
                        }
                    }
                }
            }
            endTime = System.currentTimeMillis();
        }
    }

    @After
    public void tearDown() {
        stopApp(mUidevice.getCurrentPackageName());//stop app after test
    }


//    public boolean containsWidget(UiObject2 obj,String curACT){
//        String text = obj.getText();
//        String resourceId = obj.getResourceName();
//        String contentDesc = obj.getContentDescription();
//        String classType = obj.getClassName();
//        Rect visBounds = obj.getVisibleBounds();
//        String hostACT = curACT;
//        UIElement uiElement = new UIElement(text,resourceId,contentDesc,classType,visBounds,hostACT);
//        return visitMap.containsKey(uiElement);
//    }

    public void saveExecuteWidget(UiObject2 w_d){
        if(!isMenu() && !isDialog()){
            String text = w_d.getText();
            String resId = w_d.getResourceName();
            String contentDesc = w_d.getContentDescription();
            String classType = w_d.getClassName();
            Rect visBounds = w_d.getVisibleBounds();
            String hostACT = getRunningActivity();
            UIElement element = new UIElement(text,resId,contentDesc,classType,visBounds,hostACT);
            VisitInfo info = new VisitInfo();
            Integer frequency = info.getFrequency() + 1;
            info.setFrequency(frequency);
            visitMap.put(element,info);
        }
    }

    public void gotoMainPage() throws InterruptedException, IOException {
        //click back arrow in the page

        Rect rootRec = getRootRect();
        Rect refRct = new Rect(0, 0, width, height);
        if (getRunningActivity().equals(mainActivity) && !rootRec.equals(refRct)) {
            mUidevice.pressBack();
            Thread.sleep(waitTime);
            return;
        }
        while (!getRunningActivity().equals(mainActivity)) {
            mUidevice.pressBack();
            Thread.sleep(waitTime);
            if (isDialog()) {
                dialogBtn.click();
                Thread.sleep(waitTime);
            }
        }
    }

    public String getRunningActivity() {
        String cmd = "dumpsys activity activities";
        String result = null;
        try {
            result = mUidevice.executeShellCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("run adb shell dumpsys activity error", e.getMessage());
        }
        String pattern = "mFocusedActivity: ActivityRecord.*\n";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(result);
        if (m.find()) {
            String group = m.group(0);
            String[] strs = group.split("/");
            String currentActivity = strs[strs.length - 1].split(" ")[0];
            return currentActivity.substring(1);//remove "."
        }
        //return null;
        return mainActivity;
    }

    public boolean isDialog() {
        boolean isDialog = false;
        UiObject2 root = getRootElement();
        Rect visRect = root.getVisibleBounds();
        int visTop = visRect.top;
        int visBottom = visRect.bottom;
        Log.d("top-----bottom", visTop + "------" + visBottom);
        Log.d("left-----right", visRect.left + "-----" + visRect.right);
        //UiObject2 btn = mUidevice.findObject(By.clazz("android.widget.Button"));
        if (visTop >= height / 4 && visTop <= height / 2 && visBottom >= height / 2 &&
                visBottom <= height / 4 * 3) {
            isDialog = true;
            //dialogBtn = btn;
//            btn.click();
//            try{
//                Thread.sleep(500);
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }
        }
        Log.d("isDialog", isDialog + "");
        return isDialog;
    }

    public void addUIElement(String act, UiObject2 element) {
        if (visitedElements.containsKey(act)) {
            Set<Rect> eleSet = visitedElements.get(act);
            eleSet.add(element.getVisibleBounds());
        } else {
            Set<Rect> eleSet = new HashSet<>();
            eleSet.add(element.getVisibleBounds());
            visitedElements.put(act, eleSet);
        }
    }

    public boolean containUIElement(String act, UiObject2 element) {
        if (!visitedElements.containsKey(act)) {
            return false;
        }
        //String eleType = element.getClassName();
        Rect eleRect = element.getVisibleBounds();
        Set<Rect> eleSet = visitedElements.get(act);
        if (eleSet.contains(eleRect)) {
            return true;
        }
        return false;
    }

    public void executeEvent(UiObject2 element, String event) {
        //clearLog();
        if(element.getClassName().equals("android.widget.EditText")){
            element.click();
            element.setText(fText);
        }else{
            if (event.equals("click") || event.equals("item_click")) {
                element.click();
            } else if (event.equals("long_click") || event.equals("item_long_click")) {
                element.click(clickDuration);
            }else if(event.equals("scroll")){
                element.scroll(Direction.UP,0.6f,500);
                try{
                    Thread.sleep(waitTime);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                element.scroll(Direction.DOWN,0.6f,500);
            }
        }
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        String log_info = checkCrashed();
//        if(log_info.contains("--------- beginning of crash") && log_info.contains("Process: " + mPackage)){
//            Log.e("crash info",log_info);
//            restartApp(mPackage,mainActivity);
//        }
    }

    public void openMenu() {
        mUidevice.pressMenu();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public UiObject2 getRootElement() {
        List<UiObject2> frameLayouts = mUidevice.findObjects(By.clazz("android.widget.FrameLayout").depth(0));
        UiObject2 root = frameLayouts.get(0);
        for (UiObject2 f : frameLayouts) {
            if (getElementArea(f) > getElementArea(root)) {
                root = f;
            }
        }
        return root;
    }

    public int getElementArea(UiObject2 obj) {
        try {
            Rect rect = obj.getVisibleBounds();
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;
            int area = width * height;
            return area;

        } catch (Exception e) {
            Log.d("StaleObjectException", "element doesn't exist");
            return 0;
        }
//        Rect rect = obj.getVisibleBounds();
//        int width = rect.right - rect.left;
//        int height = rect.bottom - rect.top;
//        int area = width * height;
//        return area;
    }

    public Rect getRootRect() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UiObject2 root = getRootElement();
        Rect rRect = root.getVisibleBounds();
        Log.d("root location-left top right bottom", rRect.left + " " + rRect.top + " " + rRect.right
                + " " + rRect.bottom);
        return rRect;
    }

    public void dynamicExplore() throws IOException, InterruptedException {
        String curAct = getRunningActivity();
        visitedACT.add(curAct);
        if (length == 0) {
            List<UiObject2> clickableElements = mUidevice.findObjects(By.clickable(true));
            for (UiObject2 cele : clickableElements) {
                if (!containUIElement(curAct, cele) && !isBack(cele) && !isEditText(cele)) {
                    addUIElement(curAct, cele);
                    executeEvent(cele, "click");
                    visitedACT.add(getRunningActivity());
                    length++;
                    break;
                }
            }
        }
        Rect refRect = new Rect(0, 0, width, height);
        List<UiObject2> clickableElements = mUidevice.findObjects(By.clickable(true));
        boolean flag1 = visitedACT.contains(getRunningActivity()) && refRect.equals(getRootRect());
        boolean flag2 = clickableElements.isEmpty();
        boolean endFlag = flag1 || flag2 || containsWebView() || gotoAnotherApp();
        //String currentACT = "current";
        //String nextACT = "next";
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        while (!endFlag && length != 0 && (endTime - startTime <= scenarioTimeout)) {
            //currentACT = getRunningActivity();
            for (UiObject2 cele : clickableElements) {
                if (!containUIElement(getRunningActivity(), cele) && !isBack(cele) && !isEditText(cele)) {
                    addUIElement(getRunningActivity(), cele);
                    executeEvent(cele, "click");
                    visitedACT.add(getRunningActivity());
                    length++;
                    break;
                }
            }
            clickableElements = mUidevice.findObjects(By.clickable(true));
            flag1 = visitedACT.contains(getRunningActivity()) && refRect.equals(getRootRect());
            flag2 = clickableElements.isEmpty();
            endFlag = flag1 || flag2 || containsWebView() || gotoAnotherApp();
            //nextACT = getRunningActivity();
            endTime = System.currentTimeMillis();
        }
        gotoMainPage();
        length = 0;
    }

    public boolean isBack(UiObject2 uiObject2) {
        Rect rect = uiObject2.getVisibleBounds();
        String type = uiObject2.getClassName();
        if (rect.top >= 0 && rect.top <= height / 20 && rect.left >= 0 &&
                rect.left <= width / 25 && rect.right >= width / 20 && rect.right <= width / 5 * 3 &&
                rect.bottom >= height / 20 && rect.bottom <= height / 5 && (type.equals("android.widget.ImageButton")
                || type.equals("android.widget.ImageView") || type.equals("android.widget.LinearLayout"))) {
            return true;
        }
        return false;
    }

    public boolean isEditText(UiObject2 uiObject2) {
        String type = uiObject2.getClassName();
        if (type.equals("android.widget.EditText")) {
            return true;
        }
        return false;
    }

    public boolean containsWebView() {
        return mUidevice.hasObject(By.clazz("android.webkit.WebView"));
    }

    public boolean gotoAnotherApp() {
        String curPackage = mUidevice.getCurrentPackageName();
        Log.d("Test App Package", mPackage);
        if(curPackage != null){
            Log.d("Current App Package", curPackage);
        }else{
            Log.d("Current App Package", "null");
        }
        if (mPackage.equals(curPackage)) {
            Log.d("go to another app", "no");
            return false;
        }
        Log.d("go to another app", "yes");
        return true;
    }

    public void startApp(String pack, String act) {
        String start_cmd = "am start -S -n ";
        start_cmd += pack;
        start_cmd += "/.";
        start_cmd += act;
        try {
            mUidevice.executeShellCommand(start_cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startApp(String pack) {
        Context context = mInstrumentation.getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(pack);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void stopApp(String pack) {
        String stop_cmd = "am force-stop ";
        stop_cmd += pack;
        try {
            mUidevice.executeShellCommand(stop_cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restartApp(String pack) {
        String curPack = mUidevice.getCurrentPackageName();
        stopApp(curPack);
        startApp(pack);
    }

    public void restartApp(String pack, String act) {
        String curPack = mUidevice.getCurrentPackageName();
        stopApp(curPack);
        startApp(pack, act);
    }

    public String checkCrashed() {
        String logcat_filter = "AndroidRuntime:E CrashAnrDetector:D ActivityManager:E SQLiteDatabase:E WindowManager:E ActivityThread:E Parcel:E *:F *:S";
        String crash_cmd = "logcat -d " + logcat_filter;
        try {
            String crash_info = mUidevice.executeShellCommand(crash_cmd);
            return crash_info;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearLog() {
        String clear_cmd = "logcat -c";
        try {
            mUidevice.executeShellCommand(clear_cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResourceId(UiObject2 obj) {
        String res = obj.getResourceName();
        if (res == null) {
            return null;
        }
        return res.split("/")[1];
    }

    public void back() {
        mUidevice.pressBack();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void fill_data(UiObject2 obj) {
        String classType = obj.getClassName();
        if ("android.widget.EditText".equals(classType) && obj.getText() == null) {
            obj.setText(fText);
        }
    }

    public boolean isMenu() {
        boolean isMenu = false;
        UiObject2 root = getRootElement();
        Rect rootBounds = root.getVisibleBounds();
        int curWidth = rootBounds.right - rootBounds.left;
        int curHeight = rootBounds.bottom - rootBounds.top;
        Log.d("curWidth", curWidth + "");
        Log.d("curHeight", curHeight + "");
        boolean isOriSize = !((curHeight == height) && (curWidth == width));
        boolean hasListView = mUidevice.hasObject(By.clazz("android.widget.ListView"));
        boolean hasRecyclerView = mUidevice.hasObject(By.clazz("android.support.v7.widget.RecyclerView"));
        boolean hasList = hasListView || hasRecyclerView;
        if (isOriSize && hasList) {
            isMenu = true;
        }
        Log.d("isMenu", isMenu + "");
        return isMenu;
    }

    /**
     * find element that has min frequency
     *
     * @param elements
     * @return
     */
    public UiObject2 findOptimalElement(List<UiObject2> elements) {
        UiObject2 optElement = null;
        VisitInfo optInfo = null;
        int next = 0;//the initial index of optElement
        for (int i = 0; i < elements.size(); i++) {
            UiObject2 element = elements.get(i);
            String text = element.getText();
            String resourceId = element.getResourceName();
            String contentDesc = element.getContentDescription();
            String classType = element.getClassName();
            Rect visBounds = element.getVisibleBounds();
            String hostACT = getRunningActivity();
            UIElement uiElement = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
            VisitInfo vInfo = visitMap.get(uiElement);
            if (vInfo.getOpenType().intValue() == 0) {
                optInfo = vInfo;
                optElement = element;
                next = i + 1;
                break;
            }
        }
        for (int j = next; j < elements.size(); j++) {
            UiObject2 element = elements.get(j);
            String text = element.getText();
            String resourceId = element.getResourceName();
            String contentDesc = element.getContentDescription();
            String classType = element.getClassName();
            Rect visBounds = element.getVisibleBounds();
            String hostACT = getRunningActivity();
            UIElement uiElement = new UIElement(text, resourceId, contentDesc, classType, visBounds, hostACT);
            VisitInfo vInfo = visitMap.get(uiElement);
            if ((vInfo.getOpenType().intValue() == 0) && (vInfo.getFrequency() < optInfo.getFrequency())) {
                optInfo = vInfo;
                optElement = element;
            }
        }
        return optElement;
    }
}
