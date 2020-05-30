package com.fdu.se.sootanalyze;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import com.fdu.se.sootanalyze.model.*;
import com.fdu.se.sootanalyze.model.MenuItem;
import com.fdu.se.sootanalyze.utils.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlHandler;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.axml.ApkHandler;
import soot.jimple.infoflow.android.iccta.App;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.results.DataFlowResult;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class SootAnalyze {
    private SetupApplication app;
    private String packageName;
    private String apkPath = "apks/chaun.apk";
    private static String sdkPath = "libs/android.jar";
    private static String callbackPath = "AndroidCallbacks.txt";
    private static String sourceSinkFilePath = "SourcesAndSinks2.txt";
    private long curNodeId = 0;//the current id of WindowNode when constructing Transition Graph
    private long curWidgetId = 0;//the current id of Widget when constructing Transition Graph
    private long curEdgeId = 0;//the current id of TransitionEdge when constructing Transition Graph
    private List<String> setListeners;
    private List<String> startActSignatures;
    private List<String> addMenuItemSignatures;
    private List<String> addSubMenuSignatures;
    private List<String> addSubMenuItemSignatures;
    private List<String> systemCallbacks;

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public SetupApplication getApp() {
        return app;
    }

    public void setApp(SetupApplication app) {
        this.app = app;
    }

    public void init(String apkPath) {
        this.apkPath = apkPath;
        app = new SetupApplication(sdkPath, apkPath);
        //app.setCallbackFile(callbackPath);
        System.out.println("begin to analyse app " + StringUtil.convertToLabel(apkPath));
        app.constructCallgraph();
    }

    public SootAnalyze() {
        initializeSetListeners();
        initStartActSignatures();
        initAddMenuItemSignatures();
        initAddSubMenuSignatures();
        initAddSubMenuItemSignatures();
        //initSystemCallbacks();
        //app = new SetupApplication(sdkPath, apkPath);
        //app.constructCallgraph();
    }

    public SootAnalyze(String apkPath) {
        this.apkPath = apkPath;
        initializeSetListeners();
        initStartActSignatures();
        app = new SetupApplication(sdkPath, apkPath);
        app.constructCallgraph();
    }

    public void initSystemCallbacks(){
        systemCallbacks = new ArrayList<>();
        systemCallbacks.add("onSaveInstanceState");
        systemCallbacks.add("onRestoreInstanceState");
        systemCallbacks.add("onTouchEvent");
    }

    public void initializeSetListeners() {
        setListeners = new ArrayList<>();
        setListeners.add("setOnClickListener");
        setListeners.add("setOnLongClickListener");
        setListeners.add("setOnItemClickListener");
        setListeners.add("setOnItemLongClickListener");
        setListeners.add("setOnScrollListener");
        setListeners.add("setOnDragListener");
        setListeners.add("setOnHoverListener");
        setListeners.add("setOnTouchListener");
    }

    public void initStartActSignatures() {
        startActSignatures = new ArrayList<>();
        startActSignatures.add("<android.app.Activity: void startActivity(android.content.Intent)>");
        startActSignatures.add("<android.app.Activity: void startActivity(android.content.Intent,android.os.Bundle)");
        startActSignatures.add("<android.app.Activity: void startActivityForResult(android.content.Intent,int)>");
        startActSignatures.add("<android.app.Activity: void startActivityForResult(android.content.Intent,int,android.os.Bundle)>");
        startActSignatures.add("<android.app.Activity: void startActivityIfNeeded(android.content.Intent,int,android.os.Bundle)>");
        startActSignatures.add("<android.app.Activity: void startActivityIfNeeded(android.content.Intent,int)>");

    }

    public void initAddMenuItemSignatures() {
        addMenuItemSignatures = new ArrayList<>();
        addMenuItemSignatures.add("<android.view.Menu: android.view.MenuItem add(int,int,int,int)>");
        addMenuItemSignatures.add("<android.view.Menu: android.view.MenuItem add(int,int,int,java.lang.CharSequence)>");
    }

    public void initAddSubMenuSignatures() {
        addSubMenuSignatures = new ArrayList<>();
        addSubMenuSignatures.add("<android.view.Menu: android.view.SubMenu addSubMenu(int,int,int,int)>");
        addSubMenuSignatures.add("<android.view.Menu: android.view.SubMenu addSubMenu(int,int,int,java.lang.CharSequence)>");
    }

    public void initAddSubMenuItemSignatures() {
        addSubMenuItemSignatures = new ArrayList<>();
        addSubMenuItemSignatures.add("<android.view.SubMenu: android.view.MenuItem add(int,int,int,int)>");
        addSubMenuItemSignatures.add("<android.view.SubMenu: android.view.MenuItem add(int,int,int,java.lang.CharSequence)>");
    }

    public void initializeSoot() {
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);

        Options.v().set_output_format(Options.output_format_jimple);
//      Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_force_android_jar(sdkPath);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_soot_classpath(sdkPath);
        Scene.v().loadNecessaryClasses();
    }

    public static void main(String[] args) {
        SetupApplication app = new SetupApplication(sdkPath, "apkPath.....");
        app.setCallbackFile(callbackPath);
        app.constructCallgraph();
        System.out.println("construct call graph successfully！");
        try {
            Set<DataFlowResult> dataFlowResults = app.runInfoflow(sourceSinkFilePath).getResultSet();
            for (DataFlowResult d : dataFlowResults) {
//                Stmt stmt = d.getSink().getStmt();
//                if(stmt.toString().equals("virtualinvoke $r4.<android.widget.Button: void setOnClickListener(android.view.View$OnClickListener)>($r7)")){
//                    List<ValueBox> usebox = stmt.getUseBoxes();
//                    System.out.println(usebox);
//                    for(ValueBox value:usebox){
//                        System.out.println(value.getValue().getType()+": "+value.getValue());
//                    }
//                    break;
//                }
                System.out.println(d);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        CallGraph cg = Scene.v().getCallGraph();
        Iterator<Edge> iterator = cg.iterator();
        while ((iterator.hasNext())) {
            Edge e = iterator.next();
//            MethodOrMethodContext src = e.getSrc();
//            if (src.toString().equals("<simpletest.demo.fudan.edu.myapplication.MainActivity: void onCreate(android.os.Bundle)>")) {
//                SootMethod method = src.method();
//                SootClass cla = method.getDeclaringClass();
//                System.out.println(cla.getMethodByName("onCreate").getActiveBody());
//                break;
//            }
            MethodOrMethodContext tgt = e.getTgt();
            if (tgt.toString().contains("setOnClickListener")) {
                System.out.println(tgt);
                continue;
            }
        }
        //runflow();

    }

    public AppInfo getAppInfo() {
        AppInfo appInfo = new AppInfo();
        try {
            ProcessManifest processManifest = new ProcessManifest(apkPath);
            AXmlNode manifest = processManifest.getManifest();
            String pack = manifest.getAttribute("package").getValue().toString();
            System.out.println("package name: " + pack);
            packageName = pack;
            List<AXmlNode> activities = processManifest.getActivities();
            List<String> actNames = new ArrayList<>();
            for (AXmlNode act : activities) {
                String actName = act.getAttribute("name").getValue().toString();
                System.out.println(actName);
                actNames.add(actName);
            }
            appInfo.setPackName(pack);
            appInfo.setActivities(actNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appInfo;
    }

    public List<Widget> parseAppLayout(String filename) {
        List<Widget> layoutWidgets = new ArrayList<>();
        String layoutPath = "res/layout/" + filename + ".xml";
        try {
            ApkHandler apkHandler = new ApkHandler(apkPath);
            InputStream layoutStream = apkHandler.getInputStream(layoutPath);
            if (layoutStream != null) {
                AXmlHandler aXmlHandler = new AXmlHandler(layoutStream);
                List<AXmlNode> buttonNodes = aXmlHandler.getNodesWithTag("Button");
                if (!buttonNodes.isEmpty()) {
                    for (AXmlNode bNode : buttonNodes) {
//                    System.out.println(getIdName(bNode.getAttribute("id").getValue().toString()));
//                    System.out.println(bNode.getAttribute("onClick").getValue());
//                    System.out.println("*******************************");
                        AXmlAttribute<String> callbackAttr = (AXmlAttribute<String>) bNode.getAttribute("onClick");
                        if (callbackAttr != null) {
                            String callback = callbackAttr.getValue();
                            AXmlAttribute<Integer> idAttr = (AXmlAttribute<Integer>) bNode.getAttribute("id");
                            //Integer idValue = (Integer)bNode.getAttribute("id").getValue();
                            String buttonId = idAttr == null ? null : getIdName(idAttr.getValue().toString());
                            //System.out.println(buttonId);
                            Widget button = new Widget();
                            button.setWidgetType("android.widget.Button");
                            button.setEvent("click");
                            button.setEventMethod(callback);
                            button.setLayoutRegister(1);
                            button.setWidgetId(buttonId);
                            layoutWidgets.add(button);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return layoutWidgets;
    }

    public String parseAppStrings(String name) {
        String stringPath = "apks\\" + StringUtil.convertToLabel(apkPath) + "\\res\\values\\strings.xml";
        File stringsFile = new File(stringPath);
        if (!stringsFile.exists()) {
            decompileApp();
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document strings = docBuilder.parse(stringsFile);
            NodeList strElements = strings.getElementsByTagName("string");
            for (int i = 0; i < strElements.getLength(); i++) {
                Node strElement = strElements.item(i);
                Element e = (Element) strElement;
                String nameValue = e.getAttribute("name");
                if (name.equals(nameValue)) {
                    return strElement.getFirstChild().getNodeValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Widget> parseAppMenu(String menuName) {
        List<Widget> menuWidgets = new ArrayList<>();
        String menuPath = "res/menu/" + menuName + ".xml";
        try {
            ApkHandler apkHandler = new ApkHandler(apkPath);
            InputStream menuStream = apkHandler.getInputStream(menuPath);
            if (menuStream != null) {
                AXmlHandler aXmlHandler = new AXmlHandler(menuStream);
                AXmlNode root = aXmlHandler.getDocument().getRootNode();
                List<AXmlNode> itemNodes = root.getChildrenWithTag("item");
                if (!itemNodes.isEmpty()) {
                    for (AXmlNode itemNode : itemNodes) {
                        List<AXmlNode> sub = itemNode.getChildrenWithTag("menu");
                        if (sub.isEmpty()) {//itemNode is MenuItem
                            MenuItem menuItem = new MenuItem();
                            menuItem.setId(++curWidgetId);
                            AXmlAttribute<Integer> idAttr = (AXmlAttribute<Integer>) itemNode.getAttribute("id");
                            if (idAttr != null) {
                                int itemId = idAttr.getValue();
                                menuItem.setItemId(itemId);
                            }
//                            AXmlAttribute<String> textAttr = (AXmlAttribute<String>)itemNode.getAttribute("title");
//                            if(textAttr != null){
//                                String itemText = textAttr.getValue();
//                                menuItem.setText(itemText);
//                            }
                            String itemText = getTitleFromMenuRes(itemNode);
                            menuItem.setText(itemText);
                            menuWidgets.add(menuItem);
                        } else {//itemNode is SubMenu
                            SubMenu subMenu = new SubMenu();
                            subMenu.setId(++curWidgetId);
                            AXmlAttribute<Integer> idAttr = (AXmlAttribute<Integer>) itemNode.getAttribute("id");
                            if (idAttr != null) {
                                int subId = idAttr.getValue();
                                subMenu.setSubMenuId(subId);
                            }
//                            AXmlAttribute<String> textAttr = (AXmlAttribute<String>)itemNode.getAttribute("title");
//                            if(textAttr != null){
//                                String subText = textAttr.getValue();
//                                subMenu.setText(subText);
//                            }
                            String subText = getTitleFromMenuRes(itemNode);
                            subMenu.setText(subText);
                            List<AXmlNode> subItemNodes = sub.get(0).getChildrenWithTag("item");
                            List<MenuItem> subItems = new ArrayList<>();
                            for (AXmlNode subItemNode : subItemNodes) {
                                MenuItem subItem = new MenuItem();
                                subItem.setId(++curWidgetId);
                                AXmlAttribute<Integer> subIdAttr = (AXmlAttribute<Integer>) subItemNode.getAttribute("id");
                                if (subIdAttr != null) {
                                    int subId = subIdAttr.getValue();
                                    subItem.setItemId(subId);
                                }
//                                AXmlAttribute<String> subTextAttr = (AXmlAttribute<String>)subItemNode.getAttribute("title");
//                                if(subTextAttr != null){
//                                    String subText = subTextAttr.getValue();
//                                    subItem.setText(subText);
//                                }
                                String subItemText = getTitleFromMenuRes(subItemNode);
                                subItem.setText(subItemText);
                                subItems.add(subItem);
                            }
                            subMenu.setItems(subItems);
                            menuWidgets.add(subMenu);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return menuWidgets;
    }

    private String getTitleFromMenuRes(AXmlNode node) {
        AXmlAttribute attr = node.getAttribute("title");
        if (attr != null) {
            Object titleValue = attr.getValue();
            if (titleValue instanceof Integer) {//Attribute is Integer
                Integer intValue = (Integer) titleValue;
                String name = getStringName(intValue.toString());
                if (name != null) {
                    String title = parseAppStrings(name);
                    return title;
                }
            } else if (titleValue instanceof String) {//Attribute is String
                String title = (String) titleValue;
                return title;
            }
        }
        return null;
    }

    public void decompileApp() {
        File dFile = new File(apkPath);
        ApkDecoder decoder = new ApkDecoder();
        String appName = StringUtil.convertToLabel(apkPath);
        try {
            decoder.setOutDir(new File("apks\\" + appName));
            decoder.setApkFile(dFile);
            decoder.setDecodeSources((short) 0);//does not decompile sources
            decoder.decode();
            System.out.println("decompile " + appName + " successfully");
        } catch (Exception e) {
            System.out.println("decompile " + appName + " failed");
            e.printStackTrace();
        }
    }

    public String getActLayout(SootClass act) {
        List<SootMethod> actMethods = act.getMethods();
        for (SootMethod actMethod : actMethods) {
            String name = actMethod.getName();
            if (name.equals("onCreate") && actMethod.hasActiveBody()) {
                //System.out.println(actMethod.getSignature());
                UnitGraph onCreateCfg = new BriefUnitGraph(actMethod.getActiveBody());
                Iterator<Unit> stmts = onCreateCfg.iterator();
                while (stmts.hasNext()) {
                    Stmt stmt = (Stmt) stmts.next();
                    if (stmt instanceof InvokeStmt) {
                        InvokeStmt invokeStmt = (InvokeStmt) stmt;
                        InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
                        SootMethod invokeMethod = invokeExpr.getMethod();
                        if (invokeMethod.getName().equals("setContentView")) {
                            //System.out.println(invokeMethod.getSignature());
                            String layoutArg = invokeExpr.getArg(0).toString();
                            String layoutName = getLayoutName(layoutArg);
                            return layoutName;
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<SootClass> getActivities() {
        List<SootClass> sootActivities = new ArrayList<>();
//        Iterator<SootClass> classes = Scene.v().getApplicationClasses().iterator();
//        while(classes.hasNext()){
//            System.out.println(classes.next().getName());
//        }
        AppInfo appInfo = getAppInfo();
        for (String act : appInfo.getActivities()) {
            SootClass actClass = Scene.v().forceResolve(act, SootClass.BODIES);
            sootActivities.add(actClass);
        }
        return sootActivities;
    }

    /**
     * get the id name of widget from it's id value(int)
     *
     * @param idValue
     * @return
     */
    public String getIdName(String idValue) {
        Iterator<SootClass> appClasses = Scene.v().getApplicationClasses().iterator();
        while (appClasses.hasNext()) {
            SootClass appClass = appClasses.next();
            if (appClass.getName().endsWith("R$id")) {
                SootClass idClass = appClass;
                Iterator<SootField> idFields = idClass.getFields().iterator();
                while (idFields.hasNext()) {
                    SootField idField = idFields.next();
                    if (idField.isFinal() && idField.isStatic()) {
                        String fieldName = idField.getName();
                        Tag fieldTag = idField.getTag("IntegerConstantValueTag");
                        if (fieldTag != null) {
                            String tagString = fieldTag.toString();
                            String fieldValue = tagString.split(" ")[1];
                            if (idValue.equals(fieldValue)) {
                                return fieldName;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getLayoutName(String layoutValue) {
        Iterator<SootClass> appClasses = Scene.v().getApplicationClasses().iterator();
        while (appClasses.hasNext()) {
            SootClass appClass = appClasses.next();
            if (appClass.getName().endsWith("R$layout")) {
                SootClass layoutClass = appClass;
                Iterator<SootField> layoutFields = layoutClass.getFields().iterator();
                while (layoutFields.hasNext()) {
                    SootField layoutField = layoutFields.next();
                    if (layoutField.isFinal() && layoutField.isStatic()) {
                        String fieldName = layoutField.getName();
                        Tag fieldTag = layoutField.getTag("IntegerConstantValueTag");
                        if (fieldTag != null) {
                            String tagString = fieldTag.toString();
                            String fieldValue = tagString.split(" ")[1];
                            if (layoutValue.equals(fieldValue)) {
                                return fieldName;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getStringName(String stringValue) {
        Iterator<SootClass> appClasses = Scene.v().getApplicationClasses().iterator();
        while (appClasses.hasNext()) {
            SootClass appClass = appClasses.next();
            if (appClass.getName().endsWith("R$string")) {
                SootClass stringClass = appClass;
                Iterator<SootField> stringFields = stringClass.getFields().iterator();
                while (stringFields.hasNext()) {
                    SootField stringField = stringFields.next();
                    if (stringField.isFinal() && stringField.isStatic()) {
                        String fieldName = stringField.getName();
                        Tag fieldTag = stringField.getTag("IntegerConstantValueTag");
                        if (fieldTag != null) {
                            String tagString = fieldTag.toString();
                            String fieldValue = tagString.split(" ")[1];
                            if (stringValue.equals(fieldValue)) {
                                return fieldName;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getMenuName(String menuValue) {
        Iterator<SootClass> appClasses = Scene.v().getApplicationClasses().iterator();
        while (appClasses.hasNext()) {
            SootClass appClass = appClasses.next();
            if (appClass.getName().endsWith("R$menu")) {
                SootClass menuClass = appClass;
                Iterator<SootField> menuFields = menuClass.getFields().iterator();
                while (menuFields.hasNext()) {
                    SootField menuField = menuFields.next();
                    if (menuField.isFinal() && menuField.isStatic()) {
                        String fieldName = menuField.getName();
                        Tag fieldTag = menuField.getTag("IntegerConstantValueTag");
                        if (fieldTag != null) {
                            String tagString = fieldTag.toString();
                            String fieldValue = tagString.split(" ")[1];
                            if (menuValue.equals(fieldValue)) {
                                return fieldName;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getEvent(String methodName) {
        if (methodName.equals("setOnClickListener")) {
            return "click";
        }
        if (methodName.equals("setOnLongClickListener")) {
            return "long_click";
        }
        if (methodName.equals("setOnItemClickListener")) {
            return "item_click";
        }
        if (methodName.equals("setOnItemLongClickListener")) {
            return "item_long_click";
        }
        if (methodName.equals("setOnScrollListener")) {
            return "scroll";
        }
        if (methodName.equals("setOnDragListener")) {
            return "drag";
        }
        if (methodName.equals("setOnHoverListener")) {
            return "hover";
        }
        if (methodName.equals("setOnTouchListener")) {
            return "touch";
        }
        return null;
    }

    public String getEventCallBack(String methodName) {
        if (methodName.equals("setOnClickListener")) {
            return "onClick";
        }
        if (methodName.equals("setOnLongClickListener")) {
            return "onLongClick";
        }
        if (methodName.equals("setOnItemClickListener")) {
            return "onItemClick";
        }
        if (methodName.equals("setOnItemLongClickListener")) {
            return "onItemLongClick";
        }
        if (methodName.equals("setOnScrollListener")) {
            return "onScroll";
        }
        if (methodName.equals("setOnDragListener")) {
            return "onDrag";
        }
        if (methodName.equals("setOnHoverListener")) {
            return "onHover";
        }
        if (methodName.equals("setOnTouchListener")) {
            return "onTouch";
        }
        return null;
    }

    /**
     * find method in callgraph
     *
     * @param cg
     * @param declClass
     * @param method
     * @return
     */
    public MethodOrMethodContext findMethodContex(CallGraph cg, String declClass, String method) {
        Iterator<Edge> callEdges = cg.iterator();
        while (callEdges.hasNext()) {
            Edge callEdge = callEdges.next();
            MethodOrMethodContext src = callEdge.getSrc();
            SootMethod srcMethod = src.method();
            String dClassName = srcMethod.getDeclaringClass().getName();
            String methodName = srcMethod.getName();
            if (declClass.equals(dClassName) && method.equals(methodName)) {
                return src;
            }
        }
        return null;
    }

    public void analyseCallGraph() {
        CallGraph cg = Scene.v().getCallGraph();
        Iterator<Edge> callEdges = cg.iterator();
        while (callEdges.hasNext()) {
            Edge callEdge = callEdges.next();
            MethodOrMethodContext src = callEdge.getSrc();
            MethodOrMethodContext tgt = callEdge.getTgt();
            System.out.println(src + " --> " + tgt);
        }
    }

    public void analyseCondition() {
        CallGraph cg = Scene.v().getCallGraph();
        Iterator<Edge> callEdges = cg.iterator();
        while (callEdges.hasNext()) {
            Edge callEdge = callEdges.next();
            MethodOrMethodContext src = callEdge.getSrc();
            MethodOrMethodContext tgt = callEdge.getTgt();
            if (src.toString().equals("<com.example.acase.Start$1: void onClick(android.view.View)>")) {
//                SootMethod method = src.method();
//                System.out.println(method.getActiveBody());
//                System.out.println("-------------------------------------------------");
//                UnitGraph cfg = new BriefUnitGraph(method.getActiveBody());
//                Iterator<Unit> stmts = cfg.iterator();
//                while (stmts.hasNext()) {
//                    Stmt stmt = (Stmt) stmts.next();
//                    if(stmt instanceof IfStmt){
//                        IfStmt ifStmt = (IfStmt)stmt;
//                        Value condition = ifStmt.getCondition();
//                        System.out.println(condition);
//                        System.out.println(condition.getClass());
//                        System.out.println(condition instanceof EqExpr);
//                        EqExpr eqExpr = (EqExpr)condition;
//                        System.out.println(eqExpr.getOp1());
//                        System.out.println(eqExpr.getOp2());
//                        break;
//                    }
//                    //System.out.println(stmt);
//                    //System.out.println("********************************************");
//                }
//                break;
                System.out.println(tgt);
            }
            SootMethod srcMethod = src.method();
            SootClass c = srcMethod.getDeclaringClass();
            if (srcMethod.getName().equals("onCreate") && c.getName().contains("BoardActivity")) {
                //System.out.println(srcMethod.getActiveBody());

                for (SootMethod m : c.getMethods()) {
                    if (m.hasActiveBody() && m.getName().equals("onCreateOptionsMenu")) {
                        //System.out.println(m.getActiveBody());
                        List<Widget> widgets = getMenuWidgets(m);
                        //List<Widget> widgets = parseAppMenu("option");
                        for (Widget w : widgets) {
                            if (w instanceof MenuItem) {
                                MenuItem item = (MenuItem) w;
                                System.out.println("menu_item: " + item.getItemId() + "\t" + item.getText());
                            }
                            if (w instanceof SubMenu) {
                                SubMenu sub = (SubMenu) w;
                                System.out.println("sub_menu: " + sub.getSubMenuId() + "\t" + sub.getText());
                                for (MenuItem subItem : sub.getItems()) {
                                    System.out.println("sub_menu_item: " + subItem.getItemId() + "\t" + subItem.getText());
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * get MenuItems and SubMenus from create menu callback(for example onCreateOptionsMenu)
     *
     * @param menuMethod
     * @return
     */
    public List<Widget> getMenuWidgets(SootMethod menuMethod) {
        List<Widget> menuWidgets = new ArrayList<>();
        boolean hasMenuWidget = false;
        if (menuMethod.hasActiveBody()) {
            UnitGraph cfg = new BriefUnitGraph(menuMethod.getActiveBody());
            Iterator<Unit> stmts = cfg.iterator();
            while (stmts.hasNext()) {
                Stmt stmt = (Stmt) stmts.next();
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    SootMethod invokeMethod = invokeExpr.getMethod();
                    String signature = invokeMethod.getSignature();
                    if (signature.equals("<android.view.MenuInflater: void inflate(int,android.view.Menu)>")) {
                        Value menuIdValue = invokeExpr.getArg(0);
                        String menuName = getMenuName(menuIdValue.toString());
                        if (menuName != null) {
                            return parseAppMenu(menuName);
                        }
                    }
                    if (addMenuItemSignatures.contains(signature) || addSubMenuSignatures.contains(signature) ||
                            addSubMenuItemSignatures.contains(signature)) {
                        hasMenuWidget = true;
                        if (signature.equals("<android.view.Menu: android.view.MenuItem add(int,int,int,java.lang.CharSequence)>")) {
                            MenuItem menuItem = new MenuItem();
                            menuItem.setId(++curWidgetId);
                            Value idValue = invokeExpr.getArg(1);
                            int id = Integer.parseInt(idValue.toString());
                            menuItem.setItemId(id);
                            Value textValue = invokeExpr.getArg(3);
                            String text = textValue.toString();
                            menuItem.setText(text);
                            menuWidgets.add(menuItem);
                        }
                        if (signature.equals("<android.view.Menu: android.view.MenuItem add(int,int,int,int)>")) {
                            MenuItem menuItem = new MenuItem();
                            menuItem.setId(++curWidgetId);
                            Value idValue = invokeExpr.getArg(1);
                            int id = Integer.parseInt(idValue.toString());
                            menuItem.setItemId(id);
                            String textIdValue = invokeExpr.getArg(3).toString();
                            String textId = getStringName(textIdValue);
                            if (textId != null) {
                                String text = parseAppStrings(textId);
                                menuItem.setText(text);
                            }
                            menuWidgets.add(menuItem);
                        }
                        if (signature.equals("<android.view.Menu: android.view.SubMenu addSubMenu(int,int,int,java.lang.CharSequence)>")) {
                            SubMenu subMenu = new SubMenu();
                            subMenu.setId(++curWidgetId);
                            Value idValue = invokeExpr.getArg(1);
                            int id = Integer.parseInt(idValue.toString());
                            subMenu.setSubMenuId(id);
                            Value textValue = invokeExpr.getArg(3);
                            String text = textValue.toString();
                            subMenu.setText(text);
                            List<MenuItem> items = getSubItems(stmt, cfg);
//                            if(stmt instanceof AssignStmt){
//                                AssignStmt assignStmt = (AssignStmt)stmt;
//                                Value left = assignStmt.getLeftOp();
//                                if(left.getType().toString().equals("android.view.SubMenu")){
//                                    Stmt curStmt = assignStmt;
//                                    while(!cfg.getSuccsOf(curStmt).isEmpty()){
//                                        curStmt = (Stmt)cfg.getSuccsOf(curStmt).get(0);
//                                        if(curStmt.containsInvokeExpr()){
//                                            InvokeExpr curInvokeExpr = curStmt.getInvokeExpr();
//                                            if(curInvokeExpr instanceof InterfaceInvokeExpr){
//                                                InterfaceInvokeExpr interfaceInvokeExpr = (InterfaceInvokeExpr)curInvokeExpr;
//                                                Value invokeObj = interfaceInvokeExpr.getBase();
//                                                SootMethod interfaceInvokeMethod = interfaceInvokeExpr.getMethod();
//                                                String interfaceMethodSign = interfaceInvokeMethod.getSignature();
//                                                if(left.equivTo(invokeObj)){
//                                                    if(interfaceMethodSign.equals("<android.view.SubMenu: android.view.MenuItem add(int,int,int,java.lang.CharSequence)>")){
//                                                        MenuItem item = new MenuItem();
//                                                        Value subItemIdValue = interfaceInvokeExpr.getArg(1);
//                                                        int subItemId = Integer.parseInt(subItemIdValue.toString());
//                                                        item.setItemId(subItemId);
//                                                        Value subItemTextValue = interfaceInvokeExpr.getArg(3);
//                                                        String subItemText = subItemTextValue.toString();
//                                                        item.setText(subItemText);
//                                                        items.add(item);
//                                                    }
//                                                    if(interfaceMethodSign.equals("<android.view.SubMenu: android.view.MenuItem add(int,int,int,int)>")){
//                                                        MenuItem item = new MenuItem();
//                                                        Value subItemIdValue = interfaceInvokeExpr.getArg(1);
//                                                        int subItemId = Integer.parseInt(subItemIdValue.toString());
//                                                        item.setItemId(subItemId);
//                                                        Value subItemTextIdValue = interfaceInvokeExpr.getArg(3);
//                                                        String subItemTextId = getStringName(subItemTextIdValue.toString());
//                                                        if(subItemTextId != null){
//                                                            String subItemText = parseAppStrings(subItemTextId);
//                                                            item.setText(subItemText);
//                                                        }
//                                                        items.add(item);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                            subMenu.setItems(items);
                            menuWidgets.add(subMenu);
                        }
                        if (signature.equals("<android.view.Menu: android.view.SubMenu addSubMenu(int,int,int,int)>")) {
                            SubMenu subMenu = new SubMenu();
                            subMenu.setId(++curWidgetId);
                            Value idValue = invokeExpr.getArg(1);
                            int id = Integer.parseInt(idValue.toString());
                            subMenu.setSubMenuId(id);
                            Value textIdValue = invokeExpr.getArg(3);
                            String textId = getStringName(textIdValue.toString());
                            if (textId != null) {
                                String text = parseAppStrings(textId);
                                subMenu.setText(text);
                            }
                            List<MenuItem> items = getSubItems(stmt, cfg);
                            subMenu.setItems(items);
                            menuWidgets.add(subMenu);
                        }
                    }
                }
            }
            if (!hasMenuWidget) {//menu creation is not directly in menuMethod
                Iterator<Unit> units = cfg.iterator();
                while (units.hasNext()) {
                    Stmt stmt = (Stmt) units.next();
                    if (stmt.containsInvokeExpr()) {
                        InvokeExpr invokeExpr = stmt.getInvokeExpr();
                        if (invokeExpr.getArgCount() == 1) {
                            Value arg = invokeExpr.getArg(0);
                            if (arg.getType().toString().equals("android.view.Menu")) {
                                SootMethod m = invokeExpr.getMethod();
                                return getMenuWidgets(m);
                            }
                        }
                    }
                }
            }
        }
        return menuWidgets;
    }

    private List<MenuItem> getSubItems(Stmt stmt, UnitGraph cfg) {
        List<MenuItem> items = new ArrayList<>();
        if (stmt instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) stmt;
            Value left = assignStmt.getLeftOp();
            if (left.getType().toString().equals("android.view.SubMenu")) {
                Stmt curStmt = assignStmt;
                while (!cfg.getSuccsOf(curStmt).isEmpty()) {
                    curStmt = (Stmt) cfg.getSuccsOf(curStmt).get(0);
                    if (curStmt.containsInvokeExpr()) {
                        InvokeExpr curInvokeExpr = curStmt.getInvokeExpr();
                        if (curInvokeExpr instanceof InterfaceInvokeExpr) {
                            InterfaceInvokeExpr interfaceInvokeExpr = (InterfaceInvokeExpr) curInvokeExpr;
                            Value invokeObj = interfaceInvokeExpr.getBase();
                            SootMethod interfaceInvokeMethod = interfaceInvokeExpr.getMethod();
                            String interfaceMethodSign = interfaceInvokeMethod.getSignature();
                            if (left.equivTo(invokeObj)) {
                                if (interfaceMethodSign.equals("<android.view.SubMenu: android.view.MenuItem add(int,int,int,java.lang.CharSequence)>")) {
                                    MenuItem item = new MenuItem();
                                    item.setId(++curWidgetId);
                                    Value subItemIdValue = interfaceInvokeExpr.getArg(1);
                                    int subItemId = Integer.parseInt(subItemIdValue.toString());
                                    item.setItemId(subItemId);
                                    Value subItemTextValue = interfaceInvokeExpr.getArg(3);
                                    String subItemText = subItemTextValue.toString();
                                    item.setText(subItemText);
                                    items.add(item);
                                }
                                if (interfaceMethodSign.equals("<android.view.SubMenu: android.view.MenuItem add(int,int,int,int)>")) {
                                    MenuItem item = new MenuItem();
                                    item.setId(++curWidgetId);
                                    Value subItemIdValue = interfaceInvokeExpr.getArg(1);
                                    int subItemId = Integer.parseInt(subItemIdValue.toString());
                                    item.setItemId(subItemId);
                                    Value subItemTextIdValue = interfaceInvokeExpr.getArg(3);
                                    String subItemTextId = getStringName(subItemTextIdValue.toString());
                                    if (subItemTextId != null) {
                                        String subItemText = parseAppStrings(subItemTextId);
                                        item.setText(subItemText);
                                    }
                                    items.add(item);
                                }
                            }
                        }
                    }
                }
            }
        }
        return items;
    }

    public void removeDisEdges(CallGraph cg) {
        List<Edge> delEdges = new ArrayList<>();
        Iterator<Edge> edges = cg.iterator();
        while (edges.hasNext()) {
            Edge e = edges.next();
            MethodOrMethodContext src = e.getSrc();
            MethodOrMethodContext tgt = e.getTgt();
            String srcName = src.method().getName();
            if (!src.toString().contains(packageName)) {
                delEdges.add(e);
                continue;
            }
            if (src.equals(tgt)) {
                delEdges.add(e);
                continue;
            }
            if (srcName.equals("<clinit>")) {
                delEdges.add(e);
                continue;
            }
            if (src.toString().contains("jacoco")) {
                delEdges.add(e);
                continue;
            }
            if (src.toString().equals("<java.lang.RuntimeException: void <init>(java.lang.String)>")) {
                delEdges.add(e);
                continue;
            }
            if (src.toString().equals("<java.lang.Exception: void <init>()>")) {
                delEdges.add(e);
                continue;
            }
        }
        for (Edge delEdge : delEdges) {
            cg.removeEdge(delEdge);
        }
    }

    public void findStartActEdges(CallGraph cg, Edge e, Stack<Edge> path, List<Edge> startActEdges) {
        MethodOrMethodContext tgt = e.getTgt();
        Iterator<Edge> outEdges = cg.edgesOutOf(tgt);
        if (!path.empty() && isStartActMethod(path.peek().tgt())) {
            Edge startActEdge = path.pop();
            startActEdges.add(startActEdge);
            return;
        }
        if (!path.empty() && !outEdges.hasNext()) {
            path.pop();
            return;
        }
        while (outEdges.hasNext()) {
            Edge outEdge = outEdges.next();
            path.push(outEdge);
            System.out.println(outEdge.getSrc() + "-->" + outEdge.getTgt());
            //System.out.println(outEdge.getSrc().method().getName());
            //System.out.println(outEdge.getSrc().equals(outEdge.getTgt()));
            findStartActEdges(cg, outEdge, path, startActEdges);
        }
        path.pop();
        //return startActEdges;
    }

    public void findDepInvokeEdges(CallGraph cg, Edge e, Stack<Edge> path, Set<Edge> depInvokeEdges) {
        MethodOrMethodContext tgt = e.getTgt();
        Iterator<Edge> outEdges = cg.edgesOutOf(tgt);
        if (!path.empty() && isDepMethod(path.peek().tgt())) {
            Edge depInvokeEdge = path.pop();
            depInvokeEdges.add(depInvokeEdge);
            return;
        }
        if (!path.empty() && !outEdges.hasNext()) {
            path.pop();
            return;
        }
        while (outEdges.hasNext()) {
            Edge outEdge = outEdges.next();
            path.push(outEdge);
            findDepInvokeEdges(cg, outEdge, path, depInvokeEdges);
        }
        path.pop();
    }

    public boolean isStartActMethod(SootMethod method) {
        String signature = method.getSignature();
        if (startActSignatures.contains(signature)) {
            return true;
        }
        return false;
    }

    public boolean isDepMethod(SootMethod method) {
        String name = method.getName();
        if (name.equals("isChecked")) {
            return true;
        }
        return false;
    }

    public String getTargetAct(UnitGraph cfg, Stmt invokeStmt) {
        InvokeExpr startActExpr = invokeStmt.getInvokeExpr();
        Value intentValue = startActExpr.getArg(0);
        Stmt curStmt = invokeStmt;
        while (!cfg.getPredsOf(curStmt).isEmpty()) {
            curStmt = (Stmt) cfg.getPredsOf(curStmt).get(0);
            if (curStmt instanceof InvokeStmt) {
                InvokeExpr invokeExpr = curStmt.getInvokeExpr();
                if (invokeExpr instanceof VirtualInvokeExpr) {
                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
                    SootMethod invokeMethod = virtualInvokeExpr.getMethod();
                    String signature = invokeMethod.getSignature();
                    if (signature.equals("<android.content.Intent: android.content.Intent setClass(android.content.Context,java.lang.Class)>")) {
                        Value invokeObj = virtualInvokeExpr.getBase();
                        if (invokeObj.equivTo(intentValue)) {
                            String tgtAct = virtualInvokeExpr.getArg(1).toString();
                            return tgtAct;
                        }
                    }
                }
                if (invokeExpr instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) invokeExpr;
                    SootMethod invokeMethod = specialInvokeExpr.getMethod();
                    String signature = invokeMethod.getSignature();
                    if (signature.equals("<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>")) {
                        Value invokeObj = specialInvokeExpr.getBase();
                        if (invokeObj.equivTo(intentValue)) {
                            String tgtAct = specialInvokeExpr.getArg(1).toString();
                            return tgtAct;
                        }
                    }
                }
            }
            if (curStmt instanceof AssignStmt) {
                AssignStmt assign = (AssignStmt) curStmt;
                Value leftValue = assign.getLeftOp();
                if (leftValue.equivTo(intentValue)) {
                    Value rightValue = assign.getRightOp();
                    if (rightValue instanceof VirtualInvokeExpr) {//intent from other method's return value
                        VirtualInvokeExpr intentInvoke = (VirtualInvokeExpr) rightValue;
                        SootMethod intentMethod = intentInvoke.getMethod();
                        if (intentMethod.hasActiveBody()) {
                            Body intentBody = intentMethod.getActiveBody();
                            UnitGraph intentCfg = new BriefUnitGraph(intentBody);
                            ReturnStmt returnStmt = getReturnStmt(intentCfg);
                            Value returnValue = returnStmt.getOp();
                            Stmt intentCurStmt = returnStmt;
                            while (!intentCfg.getPredsOf(intentCurStmt).isEmpty()) {
                                intentCurStmt = (Stmt) intentCfg.getPredsOf(intentCurStmt).get(0);
                                if (intentCurStmt instanceof InvokeStmt) {
                                    InvokeExpr invokeExpr = intentCurStmt.getInvokeExpr();
                                    if (invokeExpr instanceof VirtualInvokeExpr) {
                                        VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
                                        SootMethod invokeMethod = virtualInvokeExpr.getMethod();
                                        String signature = invokeMethod.getSignature();
                                        if (signature.equals("<android.content.Intent: android.content.Intent setClass(android.content.Context,java.lang.Class)>")) {
                                            Value invokeObj = virtualInvokeExpr.getBase();
                                            if (invokeObj.equivTo(returnValue)) {
                                                String tgtAct = virtualInvokeExpr.getArg(1).toString();
                                                return tgtAct;
                                            }
                                        }
                                    }
                                    if (invokeExpr instanceof SpecialInvokeExpr) {
                                        SpecialInvokeExpr specialInvokeExpr = (SpecialInvokeExpr) invokeExpr;
                                        SootMethod invokeMethod = specialInvokeExpr.getMethod();
                                        String signature = invokeMethod.getSignature();
                                        if (signature.equals("<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>")) {
                                            Value invokeObj = specialInvokeExpr.getBase();
                                            if (invokeObj.equivTo(returnValue)) {
                                                String tgtAct = specialInvokeExpr.getArg(1).toString();
                                                return tgtAct;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public ReturnStmt getReturnStmt(UnitGraph cfg) {
        List<Unit> tails = cfg.getTails();
        return (ReturnStmt) tails.get(0);
    }

    public void printMethodBody() {
        List<SootClass> sootActivities = getActivities();
        for (SootClass sootAct : sootActivities) {
            Value value1 = null;
            Value value2 = null;
            List<SootMethod> methods = sootAct.getMethods();
            for (SootMethod method : methods) {
                String className = sootAct.getName();
                String methodName = method.getName();
                if (method.hasActiveBody() && className.contains("ThreadActivity") && methodName.equals("createAbsListView")) {
                    UnitGraph graph = new BriefUnitGraph(method.getActiveBody());
                    Iterator<Unit> stmts = graph.iterator();
                    while (stmts.hasNext()) {
                        Stmt stmt = (Stmt) stmts.next();
                        if (stmt.toString().equals("$r10 = r0.<com.chanapps.four.activity.ThreadActivity: android.widget.AbsListView boardGrid>")) {
                            System.out.println(stmt);
                            value1 = ((AssignStmt) stmt).getRightOp();
                            System.out.println("value1: " + value1);
                            System.out.println(stmt.containsFieldRef());
                            System.out.println(stmt.getFieldRef());
                            break;
                        }
                    }
                    //System.out.println(methodName);
                    //System.out.println(method.getActiveBody());
                }
                if (method.hasActiveBody() && className.contains("ThreadActivity") && methodName.equals("initTablet")) {
                    System.out.println(methodName);
                    UnitGraph graph2 = new BriefUnitGraph(method.getActiveBody());
                    Iterator<Unit> stmts2 = graph2.iterator();
                    while (stmts2.hasNext()) {
                        Stmt stmt2 = (Stmt) stmts2.next();
                        if (stmt2.toString().equals("r0.<com.chanapps.four.activity.ThreadActivity: android.widget.AbsListView boardGrid> = $r3")) {
                            System.out.println(stmt2);
                            value2 = ((AssignStmt) stmt2).getLeftOp();
                            System.out.println("value2: " + value2);
                            break;
                        }
                    }
                }
            }
            if ((value1 != null) && (value2 != null)) {
                System.out.println("value1 equals value2: " + value1.equals(value2));
                System.out.println("value1 equivto value2: " + value1.equivTo(value2));
                System.out.println("value1 toString equals value2 toString: " + value1.toString().equals(value2.toString()));
            }
        }
    }

    private SootMethod hasOptionsMenu(SootClass act) {
        List<SootMethod> methods = act.getMethods();
        for (SootMethod method : methods) {
            String name = method.getName();
            if (name.equals("onCreateOptionsMenu")) {
                return method;
            }
        }
        return null;
    }

    public List<WindowNode> analyse() {
        List<SootClass> sootActivities = getActivities();
        List<WindowNode> actNodes = new ArrayList<>();//all activities'window node
        for (SootClass sootAct : sootActivities) {
            List<Widget> widgets = new ArrayList<>();//all widgets binding events of the activity
            WindowNode actNode = new WindowNode();//window node of the activity
            actNode.setId(++curNodeId);
            actNode.setLabel(StringUtil.convertToLabel(apkPath));
            actNode.setType(WindowType.ACT);
            actNode.setName(sootAct.getName());
            SootMethod opt_cb = hasOptionsMenu(sootAct);//check if has onCreateOptionsMenu
            if (opt_cb != null) {
                actNode.setHasOptionsMenu(1);
                WindowNode optMenuNode = new WindowNode();
                optMenuNode.setId(++curNodeId);
                optMenuNode.setLabel(StringUtil.convertToLabel(apkPath));
                optMenuNode.setType(WindowType.OPTMENU);
                optMenuNode.setName(sootAct.getName());
                List<Widget> optMenuWidgets = getMenuWidgets(opt_cb);
                optMenuNode.setWidgets(optMenuWidgets);
                actNode.setOptionsMenuNode(optMenuNode);
            }
            String actLayout = getActLayout(sootAct);
            if (actLayout != null) {
                List<Widget> layoutWidgets = parseAppLayout(actLayout);
                if (!layoutWidgets.isEmpty()) {
                    for (Widget layoutWidget : layoutWidgets) {
                        layoutWidget.setId(++curWidgetId);
                        layoutWidget.setListenerName(sootAct.getName());
                        widgets.add(layoutWidget);
                    }
                }
            }
            System.out.println(sootAct.getName() + " all methods: ");
            List<SootMethod> methods = sootAct.getMethods();
            for (SootMethod method : methods) {
                if (method.hasActiveBody()) {
                    System.out.println(method.getName() + " all statements: ");
                    Body body = method.getActiveBody();
                    UnitGraph cfg = new BriefUnitGraph(body);
                    Iterator<Unit> statements = cfg.iterator();
                    while (statements.hasNext()) {
                        Stmt stmt = (Stmt) statements.next();
                        if (stmt.containsInvokeExpr()) {
                            InvokeExpr invokeExpr = stmt.getInvokeExpr();
                            if (invokeExpr instanceof VirtualInvokeExpr) {
                                VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
                                SootMethod invokeMethod = virtualInvokeExpr.getMethod();
                                String invokeMethodName = invokeMethod.getName();
                                if (setListeners.contains(invokeMethodName)) {
                                    String event = getEvent(invokeMethodName);
                                    String eventCallBack = getEventCallBack(invokeMethodName);
                                    System.out.println(stmt);
//                            List<Unit> preStmts = cfg.getPredsOf(stmt);
//                            System.out.println(preStmts);
                                    Value base = virtualInvokeExpr.getBase();//object of call method
                                    //List<ValueBox> useValues = stmt.getUseBoxes();
                                    //System.out.println(useValues);
                                    Value arg = virtualInvokeExpr.getArg(0);
                                    Type listener = arg.getType();//class name of the listener
                                    System.out.println(listener);
                                    Stmt curStmt = stmt;
                                    Value curValue = base;
                                    Stmt dataflowStmt = stmt;
                                    //System.out.println(dataflowStmt);
                                    while (!cfg.getPredsOf(curStmt).isEmpty()) {
                                        List<Unit> preStmts = cfg.getPredsOf(curStmt);
                                        curStmt = (Stmt) preStmts.get(0);
                                        if (curStmt instanceof AssignStmt) {
                                            AssignStmt curAssignStmt = (AssignStmt) curStmt;
                                            Value left = curAssignStmt.getLeftOp();
                                            if (left.equivTo(curValue)) {
                                                dataflowStmt = curAssignStmt;
                                                System.out.println(dataflowStmt);
//                                            if(curAssignStmt.toString().equals("$r4 = (android.widget.Button) $r2")){
//                                                System.out.println(curAssignStmt.getRightOp() instanceof CastExpr);
//                                                System.out.println(curAssignStmt.getRightOp().getUseBoxes());
//                                            }
//                                            if(curAssignStmt.toString().equals("$r4 = new android.widget.Button")){
//                                                System.out.println(curAssignStmt.getRightOp() instanceof NewExpr);
//                                                NewExpr newExpr = (NewExpr)curAssignStmt.getRightOp();
//                                                System.out.println(newExpr.getType());
//                                                System.out.println(base.getType());
//                                                System.out.println(newExpr.getType().equals(base.getType()));
//                                            }
                                                Value right = curAssignStmt.getRightOp();
                                                if (right instanceof CastExpr) {
                                                    CastExpr castExpr = (CastExpr) right;
                                                    curValue = castExpr.getOp();
                                                } else {
                                                    curValue = right;
                                                }
                                                if (right instanceof VirtualInvokeExpr) {
                                                    VirtualInvokeExpr expr = (VirtualInvokeExpr) right;
                                                    SootMethod rmethod = expr.getMethod();
                                                    String rname = rmethod.getName();
                                                    if (rname.equals("findViewById")) {
                                                        System.out.println("data flow analyze end, find source findViewById");
                                                        Value arg0 = expr.getArg(0);
                                                        System.out.println("widget id: " + arg0);
                                                        Widget fviWidget = new Widget();//widget from findViewById
                                                        fviWidget.setId(++curWidgetId);
                                                        fviWidget.setWidgetId(getIdName(arg0.toString()));
                                                        fviWidget.setWidgetType(base.getType().toString());
                                                        fviWidget.setEvent(event);
                                                        fviWidget.setEventMethod(eventCallBack);
                                                        fviWidget.setListenerName(listener.toString());
                                                        widgets.add(fviWidget);
                                                        break;
                                                    }
                                                }
                                                if (right instanceof NewExpr) {
                                                    NewExpr expr = (NewExpr) right;
                                                    Type newType = expr.getType();
                                                    Type baseType = base.getType();
                                                    if (newType.equals(baseType)) {
                                                        System.out.println("data flow analyze end, find source New " + baseType);
                                                        Widget nWidget = new Widget();//widget from new ......
                                                        nWidget.setId(++curWidgetId);
                                                        nWidget.setWidgetType(baseType.toString());
                                                        nWidget.setEvent(event);
                                                        nWidget.setEventMethod(eventCallBack);
                                                        nWidget.setListenerName(listener.toString());
                                                        widgets.add(nWidget);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (dataflowStmt instanceof AssignStmt && dataflowStmt.containsFieldRef()) {
                                        System.out.println("need another method data flow analysis");
                                        //System.out.println(dataflowStmt);
                                        AssignStmt interStmt = (AssignStmt) dataflowStmt;
                                        Value interRight = interStmt.getRightOp();
                                        Value interValue = interRight;
                                        Stmt interCurStmt = null;
                                        for (SootMethod extraMethod : methods) {
                                            if (!extraMethod.equals(method) && extraMethod.hasActiveBody()) {
                                                Body extraMethodBody = extraMethod.getActiveBody();
                                                UnitGraph extraCfg = new BriefUnitGraph(extraMethodBody);
                                                Iterator<Unit> extraMethodStmts = extraCfg.iterator();
                                                while (extraMethodStmts.hasNext()) {
                                                    Stmt extraMethodStmt = (Stmt) extraMethodStmts.next();
                                                    if (extraMethodStmt instanceof AssignStmt) {
                                                        AssignStmt extraMethodAssignStmt = (AssignStmt) extraMethodStmt;
                                                        Value extraLeft = extraMethodAssignStmt.getLeftOp();
                                                        if (extraLeft.toString().equals(interRight.toString())) {//find the start data flow analysis stmt in another method
                                                            System.out.println("find start data flow analysis stmt in " + extraMethod.getName());
                                                            interCurStmt = extraMethodAssignStmt;
                                                            System.out.println(extraMethod.getName() + ": " + interCurStmt);
                                                            Value extraRight = extraMethodAssignStmt.getRightOp();
                                                            if (extraRight instanceof CastExpr) {
                                                                CastExpr extraExpr = (CastExpr) extraRight;
                                                                interValue = extraExpr.getOp();
                                                            } else {
                                                                interValue = extraRight;
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (interCurStmt != null) {
                                                    while (!extraCfg.getPredsOf(interCurStmt).isEmpty()) {
                                                        interCurStmt = (Stmt) extraCfg.getPredsOf(interCurStmt).get(0);
                                                        if (interCurStmt instanceof AssignStmt) {
                                                            AssignStmt interCurAssignStmt = (AssignStmt) interCurStmt;
                                                            Value interCurLeft = interCurAssignStmt.getLeftOp();
                                                            if (interCurLeft.equivTo(interValue)) {
                                                                System.out.println(extraMethod.getName() + ": " + interCurAssignStmt);
                                                                Value interCurRight = interCurAssignStmt.getRightOp();
                                                                if (interCurRight instanceof CastExpr) {
                                                                    CastExpr interCurExpr = (CastExpr) interCurRight;
                                                                    interValue = interCurExpr.getOp();
                                                                } else {
                                                                    interValue = interCurRight;
                                                                }
                                                                if (interCurRight instanceof VirtualInvokeExpr) {
                                                                    VirtualInvokeExpr invExpr = (VirtualInvokeExpr) interCurRight;
                                                                    SootMethod invMethod = invExpr.getMethod();
                                                                    String invName = invMethod.getName();
                                                                    if (invName.equals("findViewById")) {
                                                                        System.out.println("inter-procedure data flow analyze end, find source findViewById");
                                                                        Value invExprArg = invExpr.getArg(0);
                                                                        System.out.println("widget id: " + invExprArg);
                                                                        Widget ifviWidget = new Widget();//widget from findViewById
                                                                        ifviWidget.setId(++curWidgetId);
                                                                        ifviWidget.setWidgetId(getIdName(invExprArg.toString()));
                                                                        ifviWidget.setWidgetType(base.getType().toString());
                                                                        ifviWidget.setEvent(event);
                                                                        ifviWidget.setEventMethod(eventCallBack);
                                                                        ifviWidget.setListenerName(listener.toString());
                                                                        widgets.add(ifviWidget);
                                                                        break;
                                                                    }
                                                                }
                                                                if (interCurRight instanceof NewExpr) {
                                                                    NewExpr newExpr = (NewExpr) interCurRight;
                                                                    Type newType = newExpr.getType();
                                                                    Type baseType = base.getType();
                                                                    if (newType.equals(baseType)) {
                                                                        System.out.println("inter-procedure data flow analyze end, find source New " + baseType);
                                                                        Widget inWidget = new Widget();//widget from new ......
                                                                        inWidget.setId(++curWidgetId);
                                                                        inWidget.setWidgetType(baseType.toString());
                                                                        inWidget.setEvent(event);
                                                                        inWidget.setEventMethod(eventCallBack);
                                                                        inWidget.setListenerName(listener.toString());
                                                                        widgets.add(inWidget);
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    System.out.println("*********************************************************");
                }
            }
            System.out.println("--------------------------------------------------------");
            actNode.setWidgets(widgets);
            actNodes.add(actNode);
        }
        return actNodes;
    }

    public void analyseDependencies(List<WindowNode> wNodes) {
        for (WindowNode wn : wNodes) {
            String wType = wn.getType();
            if (wType.equals(WindowType.ACT) || wType.equals(WindowType.DIALOG)) {
                List<Widget> widgets = wn.getWidgets();
                if (!widgets.isEmpty()) {
                    for (Widget w : widgets) {
                        String listener = w.getListenerName();
                        String eventMethod = w.getEventMethod();
                        List<Widget> dWidgets = new ArrayList<>();
                        List<String> w_ids = new ArrayList<>();
                        CallGraph cg = Scene.v().getCallGraph();
                        removeDisEdges(cg);
                        MethodOrMethodContext cbContext = findMethodContex(cg, listener, eventMethod);
                        if (cbContext != null) {
//                        Iterator<Edge> outEdges = cg.edgesOutOf(cbContext);
//                        Set<Edge> depInvokes = new HashSet<>();
//                        while(outEdges.hasNext()){
//                            Stack<Edge> path = new Stack<>();
//                            Edge outEdge = outEdges.next();
//                            path.push(outEdge);
//                            findDepInvokeEdges(cg,outEdge,path,depInvokes);
//                        }
//                        for(Edge e:depInvokes){
//                            Stmt invokeStmt = e.srcStmt();
//                            if(invokeStmt instanceof AssignStmt){
//                                SootMethod srcMethod = e.src();
//                                InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
//                                if(invokeExpr instanceof VirtualInvokeExpr){
//                                    VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr)invokeExpr;
//                                    Value invokeObj = virtualInvokeExpr.getBase();
//                                    String d_id = findWidgetDef(invokeObj, invokeStmt, srcMethod);
//                                }
//                            }
//                        }
                            SootMethod cbMethod = cbContext.method();
                            if (cbMethod.hasActiveBody()) {
                                UnitGraph cbCfg = new BriefUnitGraph(cbMethod.getActiveBody());
                                Iterator<Unit> stmts = cbCfg.iterator();
                                while (stmts.hasNext()) {
                                    Stmt stmt = (Stmt) stmts.next();
                                    if (isDepStmt(stmt)) {
                                        AssignStmt assignStmt = (AssignStmt) stmt;
                                        Value rightValue = assignStmt.getRightOp();
                                        Value invokeObj = ((VirtualInvokeExpr) rightValue).getBase();
                                        String d_id = findWidgetDef(invokeObj, stmt, cbMethod);
                                        if (d_id != null && !w_ids.contains(d_id)) {
                                            w_ids.add(d_id);
                                            //System.out.println(d_id);
                                            Widget dWidget = new Widget();
                                            dWidget.setId(++curWidgetId);
                                            dWidget.setWidgetId(d_id);
                                            dWidget.setWidgetType(invokeObj.getType().toString());
                                            //System.out.println(dWidget.getId()+"\t"+dWidget.getWidgetId()+"\t"+dWidget.getWidgetType());
                                            dWidgets.add(dWidget);
                                        }
                                    }
                                }
                            }
                        }
                        w.setdWidgets(dWidgets);
                    }
                }
            }
        }
    }

    public boolean isDepStmt(Stmt stmt) {
        if (stmt instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) stmt;
            Value right = assignStmt.getRightOp();
            if (right instanceof VirtualInvokeExpr) {
                VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) right;
                SootMethod invokeMethod = virtualInvokeExpr.getMethod();
                String name = invokeMethod.getName();
                if (name.equals("isChecked")) {
                    return true;
                }
            }
        }
        return false;
    }

    public TransitionGraph generateTransitionGraph(List<WindowNode> wNodes) {
        TransitionGraph graph = new TransitionGraph();
        List<TransitionEdge> edges = new ArrayList<>();
        for (WindowNode wn : wNodes) {
            List<Widget> widgets = wn.getWidgets();
            if (!widgets.isEmpty()) {
                for (Widget w : widgets) {
                    String listener = w.getListenerName();
                    String eventMethod = w.getEventMethod();
                    CallGraph cg = Scene.v().getCallGraph();
                    removeDisEdges(cg);
                    MethodOrMethodContext cbContext = findMethodContex(cg, listener, eventMethod);
                    if (cbContext != null) {
                        Iterator<Edge> outEdges = cg.edgesOutOf(cbContext);
                        List<Edge> startActs = new ArrayList<>();
                        while (outEdges.hasNext()) {
                            Stack<Edge> path = new Stack<>();
                            Edge outEdge = outEdges.next();
                            path.push(outEdge);
                            findStartActEdges(cg, outEdge, path, startActs);
                        }
                        for (Edge e : startActs) {
                            SootMethod srcMethod = e.src();//the method calling ICC method
                            if (srcMethod.hasActiveBody()) {
                                UnitGraph cfg = new BriefUnitGraph(srcMethod.getActiveBody());
                                Stmt invokeStmt = e.srcStmt();
                                String target = getTargetAct(cfg, invokeStmt);
                                if (target != null) {
                                    String targetAct = StringUtil.convertToAct(target);
                                    WindowNode targetWNode = findNodeByName(wNodes, targetAct);
                                    if (targetWNode != null) {
                                        TransitionEdge edge = new TransitionEdge();
                                        edge.setId(++curEdgeId);
                                        edge.setLabel(StringUtil.convertToLabel(apkPath));
                                        edge.setSource(wn);
                                        edge.setTarget(targetWNode);
                                        edge.setWidget(w);
                                        edges.add(edge);
                                        Set<TransitionEdge> oes = wn.getOutEdges();//outedges of source node
                                        oes.add(edge);
                                        wn.setOutEdges(oes);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        graph.setNodes(wNodes);
        graph.setEdges(edges);
        return graph;
    }

    public WindowNode findNodeByName(List<WindowNode> nodes, String name) {
        for (WindowNode node : nodes) {
            if (name.equals(node.getName())) {
                return node;
            }
        }
        return null;
    }

    public String findWidgetDef(Value value, Stmt stmt, SootMethod method) {
        Stmt curStmt = stmt;
        Value curValue = value;
        Stmt dataflowStmt = stmt;
        if (method.hasActiveBody()) {
            Body body = method.getActiveBody();
            UnitGraph cfg = new BriefUnitGraph(body);
            while (!cfg.getPredsOf(curStmt).isEmpty()) {
                List<Unit> preStmts = cfg.getPredsOf(curStmt);
                curStmt = (Stmt) preStmts.get(0);
                if (curStmt instanceof AssignStmt) {
                    AssignStmt curAssignStmt = (AssignStmt) curStmt;
                    Value left = curAssignStmt.getLeftOp();
                    if (left.equivTo(curValue)) {
                        dataflowStmt = curAssignStmt;
                        Value right = curAssignStmt.getRightOp();
                        if (right instanceof CastExpr) {
                            CastExpr castExpr = (CastExpr) right;
                            curValue = castExpr.getOp();
                        } else {
                            curValue = right;
                        }
                        if (right instanceof VirtualInvokeExpr) {
                            VirtualInvokeExpr expr = (VirtualInvokeExpr) right;
                            SootMethod rmethod = expr.getMethod();
                            String rname = rmethod.getName();
                            if (rname.equals("findViewById")) {
                                //System.out.println("data flow analyze end, find source findViewById");
                                Value arg0 = expr.getArg(0);
                                //System.out.println("widget id: " + arg0);
                                String id = getIdName(arg0.toString());
                                return id;
                            }
                        }
                    }
                }
            }
            if (dataflowStmt instanceof AssignStmt && dataflowStmt.containsFieldRef()) {
                SootField field = dataflowStmt.getFieldRef().getField();//the ref field
                SootClass refClass = field.getDeclaringClass();
                Stmt refCurStmt = null;
                Value refCurValue = null;
                for (SootMethod refMethod : refClass.getMethods()) {
                    if (refMethod.hasActiveBody()) {
                        UnitGraph refCfg = new BriefUnitGraph(refMethod.getActiveBody());
                        Iterator<Unit> refStmts = refCfg.iterator();
                        while (refStmts.hasNext()) {
                            Stmt refStmt = (Stmt) refStmts.next();
                            if (refStmt instanceof AssignStmt && refStmt.containsFieldRef()) {
                                AssignStmt refAssignStmt = (AssignStmt) refStmt;
                                Value refLeft = refAssignStmt.getLeftOp();
                                if (refLeft.toString().contains(field.toString())) {
                                    refCurStmt = refAssignStmt;
                                    Value refRight = refAssignStmt.getRightOp();
                                    if (refRight instanceof CastExpr) {
                                        CastExpr castExpr = (CastExpr) refRight;
                                        refCurValue = castExpr.getOp();
                                    } else {
                                        refCurValue = refRight;
                                    }
                                    break;
                                }
                            }
                        }
                        if (refCurStmt != null) {
                            while (!refCfg.getPredsOf(refCurStmt).isEmpty()) {
                                refCurStmt = (Stmt) refCfg.getPredsOf(refCurStmt).get(0);
                                if (refCurStmt instanceof AssignStmt) {
                                    AssignStmt refCurAssignStmt = (AssignStmt) refCurStmt;
                                    Value refCurLeft = refCurAssignStmt.getLeftOp();
                                    if (refCurLeft.equivTo(refCurValue)) {
                                        Value refCurRight = refCurAssignStmt.getRightOp();
                                        if (refCurRight instanceof CastExpr) {
                                            CastExpr refCurExpr = (CastExpr) refCurRight;
                                            refCurValue = refCurExpr.getOp();
                                        } else {
                                            refCurValue = refCurRight;
                                        }
                                        if (refCurRight instanceof VirtualInvokeExpr) {
                                            VirtualInvokeExpr invExpr = (VirtualInvokeExpr) refCurRight;
                                            SootMethod invMethod = invExpr.getMethod();
                                            String invName = invMethod.getName();
                                            if (invName.equals("findViewById")) {
                                                Value invExprArg = invExpr.getArg(0);
                                                String id = getIdName(invExprArg.toString());
                                                return id;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void runflow() {
        InfoflowAndroidConfiguration conf = new InfoflowAndroidConfiguration();
// androidDirPath is the platforms directory location in android sdk
        conf.getAnalysisFileConfig().setAndroidPlatformDir(sdkPath);
// apkFilePath is the apk file location
        conf.getAnalysisFileConfig().setTargetAPKFile(apkPath);
// sourceSinkFilePath is the source and sink declaration file
        conf.getAnalysisFileConfig().setSourceSinkFile(sourceSinkFilePath);
// set multi dex flag, if set false, only analyze classes.dex
        conf.setMergeDexFiles(true);
// set AccessPath length limit，the default is 5，setting a negative number means no limit
        conf.getAccessPathConfiguration().setAccessPathLength(-1);
// set Abstraction path length limit，negative number means no limit
        conf.getSolverConfiguration().setMaxAbstractionPathLength(-1);
        SetupApplication setup = new SetupApplication(conf);
// set Callback declaration file（no setting FlowDroid will not find）
        setup.setCallbackFile(callbackPath);
        try {
            setup.runInfoflow();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

}
