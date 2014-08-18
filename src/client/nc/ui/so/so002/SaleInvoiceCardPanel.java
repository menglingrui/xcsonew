package nc.ui.so.so002;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import nc.bs.bd.b21.BusinessCurrencyRateUtil;
import nc.bs.framework.common.NCLocator;
import nc.itf.scm.so.so012.ISquareQuery;
import nc.ui.bd.b21.CurrtypeQuery;
import nc.ui.bd.ref.busi.InvmandocDefaultRefModel;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.ToftPanel;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.UIMenuItem;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.bill.BillBodyMenuListener;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillData;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillEditListener;
import nc.ui.pub.bill.BillEditListener2;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.BillStatus;
import nc.ui.pub.bill.BillTotalListener;
import nc.ui.scm.ic.freeitem.FreeItemRefPane;
import nc.ui.scm.pub.cache.CacheTool;
import nc.ui.scm.pub.def.DefSetTool;
import nc.ui.scm.pub.report.BillRowNo;
import nc.ui.scm.ref.prm.CustAddrRefModel;
import nc.ui.scm.so.SaleBillType;
import nc.ui.so.pub.SOComboxItem;
import nc.ui.so.so001.panel.bom.BillTools;
import nc.ui.so.so001.panel.card.SOBillCardTools;
import nc.vo.bd.b20.CurrtypeVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.VOStatus;
import nc.vo.pub.ValidationException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.bd.SmartVODataUtils;
import nc.vo.scm.ic.bill.InvVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pu.RelationsCalVO;
import nc.vo.scm.pub.SCMEnv;
import nc.vo.scm.pub.smart.SmartVO;
import nc.vo.so.pub.SOCurrencyRateUtil;
import nc.vo.so.so002.SaleVO;
import nc.vo.so.so002.SaleinvoiceBVO;
import nc.vo.so.so002.SaleinvoiceVO;
import nc.vo.so.so015.ARSubUniteVO;

/**
 * 
 * <p>
 * <b>本类主要完成以下功能：</b>
 * 
 * <ul>
 *  <li>销售发票卡片界面
 *  <li>...
 * </ul> 
 *
 * <p>
 * <b>变更历史（可选）：</b>
 * <p>
 * 	 XXX版本增加XXX的支持。
 * <p>
 * <p>
 * @version 本版本号
 * @since 上一版本号
 * @author fengjb
 * @time 2009-8-18 下午06:16:50
 */
public class SaleInvoiceCardPanel extends BillCardPanel implements
    BillEditListener, BillEditListener2, BillBodyMenuListener,
    BillTotalListener {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//   // 进入该节点方式：节点、消息中心
//  public static boolean Entry_FromNode = false;

//  public static boolean Entry_FromMsgPanel = true;

  // 是否进行出库汇总
  private boolean isOutSumMakeInvoice = false;

  // 旧VO
  private SaleinvoiceVO m_oldVO = null;

  private Hashtable<String, UFDouble> oldhsSelectedARSubHVO = null;
  //保存进行冲减的冲应收单信息
  private Hashtable<String, UFDouble> hsSelectedARSubHVO = null; 
  //用于检测单张是否数值超额
  private Hashtable<String, UFDouble> hsTotalBykey = null;

  private Hashtable<String, UFDouble> oldhsTotalBykey = null;

  //修改前合并开票金额
  private UFDouble nUniteInvoiceMnyBeforeChange = null;

  private UFDouble strikemoney = null;

  private UFDouble presummoney = null;
 

  private Hashtable presummoneyByProductLine = null;

  private SOBillCardTools soBillCardTools = null;

  // 使用本类的类需实现的接口
  private IInvoiceCardPanel m_useMeContainer = null;
  
  SaleInvoiceTools st = null;

  // 单据模板项初始的编辑状态
  protected HashMap<String,UFBoolean> hsBIEnable = new HashMap<String,UFBoolean>();

  // 存货
  private ArrayList<InvVO> alInvs = new ArrayList<InvVO>();
  //自由项0参照panel
  private FreeItemRefPane ivjFreeItemRefPane = null;

  // 人员参照条件
  private String sEmployeeRefCondition = null;

  //复制行数
  private int iCopyRowCount = 0;
  //是否按照开票客户冲减应收
  public boolean isUnitByRecptcorp = false;
  //缓存当前行号对应的表体行
  public HashMap<String, Integer> hsRowIndex = new HashMap<String, Integer>();
  
  private HashMap<String, UFDouble[]> hsUnitByCust = null;
  
  private BusinessCurrencyRateUtil currateutil = null;
  private SOCurrencyRateUtil socur = null;
  
  private SaleInvoiceUI uipanel = null;

  /**
   * 返回当前单据状态
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * 
   * @return int 在以下范围内BillStatus[AUDIT、FREE、BLANKOUT]
   *         <p>
   * @author wangyf
   * @time 2007-3-6 上午10:33:09
   */
  public int getBillStatus() {

    int iStatus = -1;
    if (getHeadItem("fstatus").getValueObject() != null
        && !getHeadItem("fstatus").getValueObject().equals(""))
      iStatus = ((Integer) getHeadItem("fstatus").getValueObject()).intValue();

    return iStatus;
  }
  /**
   * 方法功能描述：判断是否有审批人。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-9-18 上午08:57:01
   */
  public boolean isHaveApproveid(){
    if(getTailItem("capproveid").getValueObject() == null ||
        getTailItem("capproveid").getValueObject().equals(""))
      return false;
    else 
      return true;
  }

  private void clearArSubTotalByKey() {
    if (getHsTotalBykey() != null)
      getHsTotalBykey().clear();
    if (getOldhsTotalBykey() != null)
      getOldhsTotalBykey().clear();

  }

  /**
   * 放弃后进行冲减缓存的处理
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-12 下午03:02:03
   */
  private void processARBufAfterCancel() {

    // 放弃后置开关,并将备份赋给当前冲减情况
    if (getOldhsSelectedARSubHVO() != null)
      setHsSelectedARSubHVO((Hashtable) getOldhsSelectedARSubHVO().clone());
  }

  /**
   * 新增一个单据时对冲减缓存进行处理
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-12 下午03:03:41
   */
  public void processARBufWhenNewABill() {

    clearArSubHVO();

    clearArSubTotalByKey();

  }

  private void clearArSubHVO() {
    if (getHsSelectedARSubHVO() != null)
      getHsSelectedARSubHVO().clear();
    if (getOldhsSelectedARSubHVO() != null)
      getOldhsSelectedARSubHVO().clear();
  }

  /**
   * 保存后进行冲减缓存的处理
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-12 下午02:59:20
   */
  private void processARBufAfterSave() {
    // 保存后进行冲减过程的备份
    setOldhsSelectedARSubHVO((getHsSelectedARSubHVO() == null ? null
        : (Hashtable) getHsSelectedARSubHVO().clone()));
    //清除对冲关系缓存
    if(null != getHsSelectedARSubHVO()){
    	getHsSelectedARSubHVO().clear();
    }
    
    // 清除TotalBy缓存
    clearArSubTotalByKey();

  }

  /**
   * 方法功能描述：获得模板初始的编辑状态。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-11 下午07:51:23
   */
  private void getInitBillItemEidtState() {
	  
    hsBIEnable.clear();
    //表头
    BillItem[] items = getBillData().getHeadShowItems();
    if (null != items) {
      for (BillItem headitem:items) {
        hsBIEnable.put("H_" + headitem.getKey(),
            headitem.isEdit() ? UFBoolean.TRUE : UFBoolean.FALSE);
      }
    }
    //表体
    items = getBillData().getBodyShowItems();
    if (null != items) {
      for (BillItem bodyitem:items) {
        hsBIEnable.put("B_" + bodyitem.getKey(),
            bodyitem.isEdit() ? UFBoolean.TRUE : UFBoolean.FALSE);
      }
    }
    //表尾
    items = getBillData().getTailShowItems();
    if (null != items) {
      for (BillItem tailitem:items) {
        hsBIEnable.put("T_" + tailitem.getKey(),
            tailitem.isEdit() ? UFBoolean.TRUE : UFBoolean.FALSE);
      }
    }
  }

  /**
   * 恢复模板初始的编辑状态。 创建日期：(2004-2-12 11:28:03)
   * 
   * @param key
   *          java.lang.String[]
   * @param row
   *          int
   */
  private void resumeBillBodyItemEdit(BillItem BillItem) {
    UFBoolean btemp = null;
    if (BillItem != null) {
      btemp = (UFBoolean) hsBIEnable.get("B_" + BillItem.getKey());
      if (btemp != null) {
        BillItem.setEdit(btemp.booleanValue());
      }
    }
  }

  public SOBillCardTools getBillCardTools() {
    if (soBillCardTools == null) {
      soBillCardTools = new SOBillCardTools(this, ClientEnvironment
          .getInstance());
    }
    return soBillCardTools;
  }

  /**
   * 方法功能描述：根据参数设置卡片界面下编辑性。
   * <b>参数说明</b>
   * @param bdData
   * @author fengjb
   * @time 2009-8-11 下午06:28:24
   */
  private void setCardPanelByPara(BillData bdData) {
      
	//表头对冲标志、状态字段不可编辑
	bdData.getHeadItem("fstatus").setEdit(false);
	bdData.getHeadItem("fcounteractflag").setEdit(false);
	
	
    //无论模板和参数如何设置，本币和辅币均不可编辑
    for(String itemkey:SaleInvoiceTools.getSaleOrderItems_Price_Mny_NoOriginal()){
    	if(null != bdData.getBodyItem(itemkey))
    		bdData.getBodyItem(itemkey).setEdit(false);
    }
    //表体的状态、币种，汇率不可编辑
    bdData.getBodyItem("blargessflag").setEdit(false);
    bdData.getBodyItem("ccurrencytypename").setEdit(false);
    bdData.getBodyItem("nexchangeotobrate").setEdit(false);

    
    // 设置小数位数
    // 币种汇率
    bdData.getHeadItem("nexchangeotobrate").setDecimalDigits(4);
    bdData.getBodyItem("nexchangeotobrate").setDecimalDigits(4);

    // 数量
    if(null != bdData.getBodyItem("nnumber"))
       bdData.getBodyItem("nnumber").setDecimalDigits(st.BD_501.intValue());
    
    if(null != bdData.getBodyItem("nquotenumber"))
    bdData.getBodyItem("nquotenumber").setDecimalDigits(st.BD_501.intValue());
    
    if(null !=  bdData.getBodyItem("ntotalinventorynumber"))
       bdData.getBodyItem("ntotalinventorynumber").setDecimalDigits(st.BD_501.intValue());
    
    if(null != bdData.getBodyItem("npacknumber"))
    bdData.getBodyItem("npacknumber").setDecimalDigits(st.BD_502.intValue());
    
    // 单价
    String[] pricekey = new String[]{
            "noriginalcurprice","noriginalcurtaxprice",
            "noriginalcurnetprice","noriginalcurtaxnetprice",
            "nprice","ntaxprice",
            "nnetprice","ntaxnetprice",
            "norgviaprice","norgviapricetax","nsubtaxnetprice",
            "nquoteoriginalcurprice", "nquoteoriginalcurtaxprice",
            "nquoteoriginalcurnetprice", "nquoteoriginalcurtaxnetprice",
            "nquoteprice", "nquotetaxprice", "nquotenetprice",
            "nquotetaxnetprice", "nsubquoteprice", "nsubquotetaxprice",
            "nsubquotenetprice", "nsubquotetaxnetprice", "nsubtaxnetprice"
        
    };
    for(String  key:pricekey){
    	if(null != bdData.getBodyItem(key))
    		bdData.getBodyItem(key).setDecimalDigits(st.BD_505.intValue());
    }
 
    // 换算率
    if (null != bdData.getBodyItem("scalefactor")) {
      bdData.getBodyItem("scalefactor").setDecimalDigits(st.BD_503.intValue());
    }
    // 如果报价单位无税单价，报价单位含税单价可见，则设置无税单价，含税单价等不可编辑
    if (bdData.getBodyItem("nquoteoriginalcurprice").isShow()
        || bdData.getBodyItem("nquoteoriginalcurtaxprice").isShow()
        || bdData.getBodyItem("nquoteoriginalcurnetprice").isShow()
        || bdData.getBodyItem("nquoteoriginalcurtaxnetprice").isShow()) {
      bdData.getBodyItem("noriginalcurprice").setEdit(false);
      bdData.getBodyItem("noriginalcurtaxprice").setEdit(false);
      bdData.getBodyItem("noriginalcurtaxnetprice").setEdit(false);
      bdData.getBodyItem("noriginalcurnetprice").setEdit(false);
    }
    // 如果报价数量显示，则主数量不可编辑
    if (bdData.getBodyItem("nquotenumber").isShow()) {
      bdData.getBodyItem("nnumber").setEdit(false);
      bdData.getBodyItem("nquotenumber").setEdit(true);
    }
    //设置整单折扣、发票折扣精度为6位
    if(null != bdData.getHeadItem("ndiscountrate"))
    	bdData.getHeadItem("ndiscountrate").setDecimalDigits(6);
    
    if(null != bdData.getHeadItem("ninvoicediscountrate"))
    	bdData.getHeadItem("ninvoicediscountrate").setDecimalDigits(6);
    
    if(null != bdData.getBodyItem("ndiscountrate") )
    	bdData.getBodyItem("ndiscountrate").setDecimalDigits(6);
    
    if(null != bdData.getBodyItem("nitemdiscountrate"))
    	bdData.getBodyItem("nitemdiscountrate").setDecimalDigits(6);
    
    if(null !=  bdData.getBodyItem("ninvoicediscountrate"))
    	bdData.getBodyItem("ninvoicediscountrate").setDecimalDigits(6);
  }

  /**
   * 此处插入方法说明。 创建日期：(2004-02-06 10:38:09)
   * 
   * @param bo
   *          nc.ui.pub.ButtonObject
   */
  public boolean uniteCancel() {
    if (isStrike() == false) {
      MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
          .getStrByID("40060501", "UPP40060501-000096")/*
                                                         * @res
                                                         * "没有进行合并开票，不能放弃合并！"
                                                         */);
      return false;
    }
    if (getBillStatus() != BillStatus.FREE) {
      MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
          .getStrByID("40060501", "UPP40060501-100065")/*
                                                         * @res "非自由状态，不能取消合并开票"
                                                         */);
      return false;
    }

    // ????? 此方式在批操作时使用，需确认是否仍需保留
    // if (getVO().getHsArsubAcct() == null || getVO().getHsArsubAcct().size() <
    // 1) {
    // MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
    // .getStrByID("40060501", "UPP40060501-100066")/*
    // * @res
    // * "已合并开票，先取消后才会再次合并开票"
    // */);
    // return false;
    // }

    int result = MessageDialog.showOkCancelDlg(this, "", nc.ui.ml.NCLangRes
        .getInstance().getStrByID("40060501", "UPP40060501-000097")/*
                                                                     * @res
                                                                     * "放弃合并将把以前所有的合并关系解除，您确定要放弃合并吗？"
                                                                     */);
    if (result == MessageDialog.ID_CANCEL)
      return false;

    // 卡片界面相应变化
    resetValueAfterUniteCancel();

    // 当放弃合并时，一次放弃所有的合并关系(将冲减金额清零，以便回写)
    SaleinvoiceVO tempVO = getVO(); 
    if(tempVO.getHeadVO().getPrimaryKey() != null && (tempVO.getHsSelectedARSubHVO() == null || tempVO.getHsSelectedARSubHVO().size() == 0)){
    	try{
    		Hashtable t = SaleinvoiceBO_Client.queryStrikeData(tempVO.getHeadVO().getPrimaryKey());
    		if(t != null && t.size() > 0) setHsSelectedARSubHVO(t);
    	}catch(Exception e){
    		SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000623")+e);
    		return false;
    	}
    }
    
    java.util.Enumeration e = getHsSelectedARSubHVO().keys();    
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      getHsSelectedARSubHVO().put(key, new UFDouble(0));
      getHsTotalBykey().put(key, new UFDouble(0));
      getOldhsTotalBykey().put(key, new UFDouble(0));
    }

    return true;
  }

  /**
   * 方法功能描述：初始化时设置卡片界面。
   * <b>参数说明</b>
   * @param bdData
   * @author fengjb
   * @time 2009-8-11 下午06:36:48
   */
  private void setCardPanel(BillData bdData) {
	 // 参数设置卡片界面 
    setCardPanelByPara(bdData);
    // 表头自定义项
    bdData.updateItemByDef(SaleInvoiceTools.getHead_defs(), "vdef", true);
    // 表体自定义项
    bdData.updateItemByDef(SaleInvoiceTools.getBody_defs(), "vdef", false);
  }

  /**
   * 设置模板输入限制。 创建日期：(2001-11-1 15:26:17) 修改日期：2003-10-29 修改人：杨涛
   * 修改内容：增加存货是否可销售开票的判断
   */
  private void setInputLimit() {
	
    UIRefPane refpane = null;  
    // 开票客户
    refpane = (UIRefPane) getHeadItem("creceiptcorpid").getComponent();
    refpane.getRefModel().setPk_corp(SaleInvoiceTools.getLoginPk_Corp());
    refpane.getRefModel().addWherePart(" and bd_cumandoc.frozenflag = 'N'");

    // 收货地址
    refpane = (UIRefPane)getHeadItem("vreceiveaddress").getComponent();
    refpane.setAutoCheck(false);
    refpane.setReturnCode(true);
    
    //收发类别
    refpane = ((UIRefPane) getHeadItem("cdispatcherid").getComponent());
    refpane.getRefModel().setPk_corp(SaleInvoiceTools.getLoginPk_Corp());
    refpane.getRefModel().addWherePart(" and rdflag = 1 ");
    
    //项目
    refpane = ((UIRefPane)getBodyItem("cprojectname").getComponent());
    refpane.setRefModel(new nc.ui.bd.b39.JobRefTreeModel("0001",SaleInvoiceTools.getLoginPk_Corp(),null));

    // 存货管理档案存货是否可销售开票
    refpane = (UIRefPane) getBodyItem("cinventorycode").getComponent();
    refpane.getRefModel().setPk_corp(SaleInvoiceTools.getLoginPk_Corp());
    refpane.getRefModel().addWherePart( "and bd_invmandoc.iscansaleinvoice ='Y'" );
    
  }

  /**
   * 仓库编辑后事件处理。 创建日期：(2001-6-23 13:42:53)
   * 
   * @return nc.vo.pub.lang.UFDouble
   */
  private void afterWarehouseEdit(BillEditEvent e) {

    String[] formulas = new String[1];
    // 包装单位名称
    formulas[0] = "cbodywarehousename->getColValue(bd_stordoc,storname,pk_stordoc,cbodywarehouseid)";
    execBodyFormulas(e.getRow(), formulas);

  }
  /**
   * 取消发票操作。 创建日期：(2001-4-20 11:19:14)
   */
  private boolean beforeCbodywarehouseidEdit(BillEditEvent e) {

    String calid = (String) getBodyValueAt(e.getRow(), "cadvisecalbodyid");
    if (calid != null && calid.trim().length() > 0) {
      UIRefPane wareRef = (UIRefPane) getBodyItem("cbodywarehousename")
          .getComponent();
      if (wareRef == null)
        return true;

      nc.ui.bd.ref.AbstractRefModel m = wareRef.getRefModel();
      if (m.getWherePart() == null || m.getWherePart().trim().length() <= 0) {
        m.setWherePart(" pk_calbody ='" + calid.trim() + "' ");
      }
      else {
        String warehouseRefWhereSql = m.getWherePart();

        if (warehouseRefWhereSql != null
            && warehouseRefWhereSql.trim().length() > 0)
          m.setWherePart(warehouseRefWhereSql + " and pk_calbody ='"
              + calid.trim() + "' ");
        else
          m.setWherePart(" pk_calbody ='" + calid.trim() + "' ");
      }
    }
    return true;
  }

  private void setContainer(IInvoiceCardPanel meCont) {
    m_useMeContainer = meCont;
  }

  private IInvoiceCardPanel getContainer() {
    return m_useMeContainer;
  }
  /**
   * 方法功能描述：币种编辑后事件处理：会进行表体行金额、价格项的清空。
   * <b>参数说明</b>
   * @param event
   * @author fengjb
   * @time 2009-10-20 下午06:25:54
   */
  public void afterCurrencyEdit(BillEditEvent event) {
     //关闭小计合计开关
	 boolean bisCalculate = getBillModel().isNeedCalculate();
	 getBillModel().setNeedCalculate(false);
    // 币种参照
    UIRefPane ccurrencytypeid = (UIRefPane) getHeadItem("ccurrencyid")
        .getComponent();
    setHeadItem("nexchangeotobrate", null);

    try {
      // 金额精度取币种小数位
      setPanelByCurrency(ccurrencytypeid.getRefPK());
      //获取折本汇率
      Object objbilldate = getHeadItem("dbilldate").getValueObject();
      String billdate = objbilldate== null?SaleInvoiceTools.getLoginDate().toString():objbilldate.toString();
      UFDouble nexchangeotobrate = SaleInvoiceTools.getSoBusiCurrUtil().getExchangeRate(ccurrencytypeid.getRefPK(), billdate);
      //设置表头折本汇率
      setHeadItem("nexchangeotobrate",nexchangeotobrate);
      
      for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
        // 表体币种
        setBodyValueAt(ccurrencytypeid.getRefPK(), i, "ccurrencytypeid");
        execBodyFormulas(i, getBodyItem("ccurrencytypeid").getLoadFormula());
        // 表体汇率
        setBodyValueAt(nexchangeotobrate, i,"nexchangeotobrate");

        // 清空表体价格、金额项
        // 2010-10-08 冯加滨 陈恩宇 左东明 编辑币种不清空数量字段 
        String[] key = SaleInvoiceTools.getInvoiceItems_PriceMny();
        int len = key.length;
        setHeadItem("ntotalsummny", null);
        setHeadItem("nstrikemny", null);
        setHeadItem("nnetmny", null);
        for (int j=0;j<len;j++){
        	setBodyValueAt(null, i, key[j]);
        }

        // 置行状态为修改状态
        if (getBillModel().getRowState(i) == BillModel.NORMAL)
          getBillModel().setRowState(i, BillModel.MODIFICATION);
      }
    }catch (Exception e) {
      nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000624"));
    }
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();

  }

  /**
   * 方法功能描述：是否辅计量。
   * <b>参数说明</b>
   * @param row
   * @return
   * @author fengjb
   * @time 2009-10-20 下午06:47:03
   */
  public boolean setAssistChange(int row) {
  
    // 是否辅计量
    UFBoolean assistunit = new UFBoolean(false);
    if (getBodyValueAt(row, "assistunit") != null)
      assistunit = new UFBoolean(getBodyValueAt(row, "assistunit").toString());

    boolean bEdit = true;
    if (!assistunit.booleanValue()) {
      bEdit = false;
    }
    setCellEditable(row, "cpackunitname", bEdit);
    setCellEditable(row, "npacknumber", bEdit);

    return bEdit;

  }

  private void setHsTotalBykey(Hashtable hsTotalBykey) {
    this.hsTotalBykey = hsTotalBykey;
  }

  private void setOldhsTotalBykey(Hashtable oldhsTotalBykey) {
    this.oldhsTotalBykey = oldhsTotalBykey;
  }

  private Hashtable<String, UFDouble> getHsTotalBykey() {
    if (hsTotalBykey == null) {
      hsTotalBykey = new Hashtable<String, UFDouble>();
    }
    return hsTotalBykey;
  }

  private Hashtable<String, UFDouble> getOldhsTotalBykey() {
    if (oldhsTotalBykey == null) {
      oldhsTotalBykey = new Hashtable<String, UFDouble>();
    }
    return oldhsTotalBykey;
  }

  /**
   * 方法功能描述：取消合并后卡片界面数据变化。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-10-20 下午07:39:49
   */
  private void resetValueAfterUniteCancel() {
	  
    //2009-10-20 fengjb 取消合并时计算冲减前金额只需要计算冲减前价税合计即可
    //关闭小计合计开关
	boolean bisCalculate = getBillModel().isNeedCalculate();
	getBillModel().setNeedCalculate(false);  
	  
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
        setBodyValueAt(getBodyValueAt(i, "nsubsummny"), i, "noriginalcursummny"); 
    	calculateNumber(i, "noriginalcursummny");

      setBodyValueAt(new UFDouble(0.0), i, "nuniteinvoicemny");
            
      if (getBillModel().getRowState(i) != BillModel.ADD
          && getBillModel().getRowState(i) != BillModel.DELETE){
        getBillModel().setRowState(i, BillModel.MODIFICATION);
      }
    }
    setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
    setHeadItem("nstrikemny", new UFDouble(0));
    execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**
   * 合并后本界面的变化
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-8 上午10:07:51
   */
  private void resetValueAfterUnite(SaleinvoiceVO saleinvoice,
      final Hashtable strikemoneyByProductLine) {

    int carddigit = getBodyItem("noriginalcursummny").getDecimalDigits();
    // 如果按产品线进行冲减
    boolean byProLine = st.SO_27.booleanValue();
    if (byProLine == true) {
      java.util.Enumeration eStrikeBill = strikemoneyByProductLine.keys();
      UFDouble totalstrikemny = new UFDouble(0);
      while (eStrikeBill.hasMoreElements()) {
        String key = (String) eStrikeBill.nextElement();
        UFDouble strikeMoney = (UFDouble) strikemoneyByProductLine.get(key);
        totalstrikemny = totalstrikemny.add(strikeMoney);
        UFDouble remainMoney = strikeMoney;
        int count = 0;
        for (int i = 0; i < saleinvoice.getChildrenVO().length; i++) {
          SaleinvoiceBVO saleinvoicebody = (SaleinvoiceBVO) saleinvoice.getChildrenVO()[i];
          if (!saleinvoicebody.getBlargessflag().booleanValue()
              && !st.isLaborOrDiscount(saleinvoicebody)
              && saleinvoicebody.getCprolineid() != null
              && saleinvoicebody.getNoriginalcurmny().doubleValue() > 0)
            count++;
        }
        for (int i = 0, j = 1; i < saleinvoice.getChildrenVO().length; i++) {
          SaleinvoiceBVO saleinvoicebody = (SaleinvoiceBVO) saleinvoice.getChildrenVO()[i];
          if (!saleinvoicebody.getBlargessflag().booleanValue()
              && !st.isLaborOrDiscount(saleinvoicebody)
              && saleinvoicebody.getCprolineid() != null
              && saleinvoicebody.getCprolineid().equals(key)
              && saleinvoicebody.getNoriginalcurmny().doubleValue() > 0) {

            if (j < count) {
              UFDouble money = saleinvoicebody.getNoriginalcursummny();
              UFDouble changemoney = money.multiply(strikeMoney).div(
                  (UFDouble) presummoneyByProductLine.get(key));
              changemoney = changemoney.setScale(0 - carddigit,
                  UFDouble.ROUND_HALF_UP);
              money = money.sub(changemoney);
              // yt add 2005-01-25
              setBodyValueAt(
                  (getBodyValueAt(i, "nuniteinvoicemny") == null ? new UFDouble(
                      0.0)
                      : (UFDouble) getBodyValueAt(i, "nuniteinvoicemny"))
                      .add(changemoney), i, "nuniteinvoicemny");
              setBodyValueAt(money, i, "noriginalcursummny");
              if (isNewBill())
                getBillModel().setRowState(i, BillModel.ADD);
              else
                getBillModel().setRowState(i, BillModel.MODIFICATION);

              remainMoney = remainMoney.sub(changemoney);
            }
            else {
              UFDouble money = saleinvoicebody.getNoriginalcursummny();
              remainMoney = remainMoney.setScale(0 - carddigit,
                  UFDouble.ROUND_HALF_UP);
              money = money.sub(remainMoney);
              // yt add 2005-01-25
              setBodyValueAt(
                  (getBodyValueAt(i, "nuniteinvoicemny") == null ? new UFDouble(
                      0.0)
                      : (UFDouble) getBodyValueAt(i, "nuniteinvoicemny"))
                      .add(remainMoney), i, "nuniteinvoicemny");
              setBodyValueAt(money, i, "noriginalcursummny");
              if (isNewBill())
                getBillModel().setRowState(i, BillModel.ADD);
              else
                getBillModel().setRowState(i, BillModel.MODIFICATION);
            }

            j++;
          }
          calculateNumber(i, "noriginalcursummny");
        }
      }
      setHeadItem("nstrikemny", totalstrikemny.add(new UFDouble(getHeadItem(
          "nstrikemny").getValue())));
      execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    }
    // 如果不按照产品线冲减
    else {
      UFDouble remainMoney = strikemoney;
      int count = 0;
      for (int i = 0; i < saleinvoice.getChildrenVO().length; i++) {
        SaleinvoiceBVO saleinvoicebody = (SaleinvoiceBVO) saleinvoice.getChildrenVO()[i];
        if (!saleinvoicebody.getBlargessflag().booleanValue()
            && !st.isLaborOrDiscount(saleinvoicebody) 
            && saleinvoicebody.getNoriginalcurmny().doubleValue() > 0)
          count++;
      }
      for (int i = 0, j = 1; i < saleinvoice.getChildrenVO().length; i++) {
        SaleinvoiceBVO saleinvoicebody = (SaleinvoiceBVO) saleinvoice.getChildrenVO()[i];
        if (!saleinvoicebody.getBlargessflag().booleanValue()
            && !st.isLaborOrDiscount(saleinvoicebody)
            && saleinvoicebody.getNoriginalcurmny().doubleValue() > 0) {
          if (j == count) {
            UFDouble money = saleinvoicebody.getNoriginalcursummny();
            remainMoney = remainMoney.setScale(0 - carddigit,
                UFDouble.ROUND_HALF_UP);
            money = money.sub(remainMoney);
            // yt add 2005-01-25
            setBodyValueAt(
                (getBodyValueAt(i, "nuniteinvoicemny") == null ? new UFDouble(
                    0.0) : (UFDouble) getBodyValueAt(i, "nuniteinvoicemny"))
                    .add(remainMoney), i, "nuniteinvoicemny");
            setBodyValueAt(money, i, "noriginalcursummny");
            if (isNewBill())
              getBillModel().setRowState(i, BillModel.ADD);
            else
              getBillModel().setRowState(i, BillModel.MODIFICATION);
          }
          else {
            UFDouble money = saleinvoicebody.getNoriginalcursummny();
            UFDouble changemoney = money.multiply(strikemoney).div(presummoney);
            changemoney = changemoney.setScale(0 - carddigit,
                UFDouble.ROUND_HALF_UP);
            money = money.sub(changemoney);
            // yt add 2005-01-25
            setBodyValueAt(
                (getBodyValueAt(i, "nuniteinvoicemny") == null ? new UFDouble(
                    0.0) : (UFDouble) getBodyValueAt(i, "nuniteinvoicemny"))
                    .add(changemoney), i, "nuniteinvoicemny");
            setBodyValueAt(money, i, "noriginalcursummny");
            if (isNewBill())
              getBillModel().setRowState(i, BillModel.ADD);
            else
              getBillModel().setRowState(i, BillModel.MODIFICATION);

            remainMoney = remainMoney.sub(changemoney);
          }
          j++;
          calculateNumber(i, "noriginalcursummny");
        }
      }

      setHeadItem("nstrikemny", strikemoney.add(new UFDouble(getHeadItem(
          "nstrikemny").getValue())));
      execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    }

    for (int i = 0; i < getBillModel().getRowCount(); i++) {
      getBillModel().setCellEditable(i, "nuniteinvoicemny", true);
    }

  }

  /**
   * 返回是否冲减
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例：根据表头值"nstrikemny"判断
   * <p>
   * <b>参数说明</b>
   * 
   * @return boolean
   *         <p>
   * @author wangyf
   * @time 2007-3-6 上午10:33:09
   */
  public boolean isStrike() {
    if (getHeadItem("nstrikemny") != null
        && (new UFDouble(getHeadItem("nstrikemny").getValue()))
            .equals(new UFDouble(0)))
      return false;
    else
      return true;
  }

  public int getCurrencyDigit(String sCurrId) {
    if(sCurrId == null) return 2;

    CurrtypeVO currtypeVO = CurrtypeQuery.getInstance().getCurrtypeVO(sCurrId);

    int digit = currtypeVO.getCurrbusidigit() == null ? 4 : currtypeVO
        .getCurrbusidigit(); // currVO.getCurrdigit().intValue();

    return digit;
  }

  /**
   * 方法功能描述：加载卡片界面数据。
   * <b>参数说明</b>
   * @param voInvoice
   * @author fengjb
   * @time 2009-8-14 下午02:16:23
   */
  public void loadCardData(SaleinvoiceVO voInvoice) {
	//创建时间工具类实例
	nc.vo.scm.pu.Timer timer = new nc.vo.scm.pu.Timer();
    timer.start();
  
    if (null == voInvoice) {
      addNew();
      updateUI();
      return;
    }
    //没有表体数据时，需要去数据库中查询表头对应的表体数据
    if (null == voInvoice.getBodyVO() 
        || voInvoice.getBodyVO().length ==0) {
      try {
        SaleinvoiceBVO[] saleinvoiceBs = SaleinvoiceBO_Client
            .queryBodyDataByHID(voInvoice.getHeadVO().getCsaleid());
        voInvoice.setChildrenVO(saleinvoiceBs);
      }
      catch (Exception e) {
        MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
            .getStrByID("SCMCOMMON", "UPPSCMCommon-000256")/*
                                                             * @res "数据加载失败！"
                                                             */);
      
        SCMEnv.out(e);
        return;
      }
    }
    //设置表头币种为表体第一行币种
    String currencyid = voInvoice.getBodyVO()[0].getCcurrencytypeid();
    voInvoice.getHeadVO().setCcurrencyid(currencyid);
    
    //依据币种设置精度
    setPanelByCurrency(currencyid);
    
    //设置表头发票折扣为表体第一行发票折扣
    UFDouble ninvoicediscountrate = voInvoice.getBodyVO()[0].getNinvoicediscountrate();
    ninvoicediscountrate = ninvoicediscountrate==null?new UFDouble(100):ninvoicediscountrate;
    voInvoice.getHeadVO().setNinvoicediscountrate(ninvoicediscountrate);
    
    //设置表头整单折扣为表体第一行整单折扣
    UFDouble ndiscountrate = voInvoice.getBodyVO()[0].getNdiscountrate();
    ndiscountrate =ndiscountrate ==null?new UFDouble(100):ndiscountrate;
    if(null == voInvoice.getHeadVO().getNdiscountrate()){
      voInvoice.getHeadVO().setNdiscountrate(ndiscountrate);
    }
    
    //设置表头折本汇率为表体第一行折本汇率
    UFDouble exchangeotobrate =  voInvoice.getBodyVO()[0].getNexchangeotobrate();
    voInvoice.getHeadVO().setAttributeValue("nexchangeotobrate", exchangeotobrate);  
    
    //关闭小计合计开关
    boolean bisCalculate = getBillModel().isNeedCalculate();
    getBillModel().setNeedCalculate(false);
    //加载数据到界面
    setBillValueVO(voInvoice);
    getBodyValueAt(0, "norgviapricetax");
    timer.addExecutePhase(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000625"));
    
    // 开票单位
    String custId = voInvoice.getHeadVO().getCreceiptcorpid();
    ((UIRefPane) getHeadItem("creceiptcorpid").getComponent()).setPK(custId);
    

//	String bankId = voInvoice.getHeadVO().getCcustbankid();
//	UIRefPane bankref = (UIRefPane)getHeadItem("ccustbankid").getComponent();
//	bankref.getRefModel().setPk_corp(getCorp());
//    bankref.setPK(bankId);
//	String bankNo = bankref.getRefCode();
//	setHeadItem("ccustomerbankNo", bankNo);
		

	// 收货地址参照
	UIRefPane vreceiveaddress = (UIRefPane)getHeadItem("vreceiveaddress").getComponent();
    if (null != getHeadItem("creceiptcorpid"))
       ((CustAddrRefModel) vreceiveaddress.getRefModel()).setCustId((String)getHeadItem("creceiptcorpid").getValueObject());
        vreceiveaddress.setValue(voInvoice.getHeadVO().getVreceiveaddress());
        
    execHeadLoadFormulas();
    timer.addExecutePhase(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000626"));
    
    getBillModel().execLoadFormula();
    timer.addExecutePhase(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000627"));
   
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
    	
      if (null == getBodyValueAt(i, "cpackunitid")
          || getBodyValueAt(i, "cpackunitid").equals(""))
        continue;
      //初始化换算率
      initScalefactor(i);
    }
    timer.addExecutePhase(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000628"));
    //初始化自由项
    initFreeItem();
    timer.addExecutePhase(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000629"));
    
     //输出各个业务操作占用时间分布
	timer.showAllExecutePhase(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000630"));
    
    updateValue();
    
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**方法功能描述：卡片界面加载数据时初始化换算率。
   * <b>参数说明</b>
   * @time 2009-1-12 下午04:52:07
   */
  public void initScalefactor(int row) {
    //主计量单位ID
    String cunitid = (String) getBodyValueAt(row, "cunitid");
    //辅计量单位ID
    String cpackunitid = (String) getBodyValueAt(row, "cpackunitid");
    if (null != cunitid && null != cpackunitid) {
      if (cpackunitid.equals(cunitid)) {
        setBodyValueAt(new UFDouble(1), row, "scalefactor");
        setBodyValueAt(UFBoolean.TRUE, row, "fixedflag");
      } else {
          Object objfixflag = getBodyValueAt(row, "fixedflag");
          UFBoolean  fixedflag =objfixflag==null?
              UFBoolean.FALSE: SmartVODataUtils.getUFBoolean(objfixflag);
          //非固定换算率
          if(! fixedflag.booleanValue()){
            Object objnum = getBodyValueAt(row, "nnumber");
            UFDouble number = SmartVODataUtils.getUFDouble(objnum);
            
            Object  objpacknum = getBodyValueAt(row, "npacknumber");
            UFDouble packnum = SmartVODataUtils.getUFDouble(objpacknum);;
            if(null != number && null != packnum
                && packnum.compareTo(new UFDouble(0)) !=0){
            	setBodyValueAt(number.div(packnum),row,"scalefactor");
            }
          }
       }
      //辅计量含税单价
      UFDouble npacknum = SmartVODataUtils.getUFDouble(getBodyValueAt(row, "npacknumber"));
      UFDouble noriginalcursummny = SmartVODataUtils.getUFDouble(getBodyValueAt(row, "noriginalcursummny"));
      UFDouble noriginalcurdiscountmny = SmartVODataUtils.getUFDouble(getBodyValueAt(row, "noriginalcurdiscountmny"));
      if(null != npacknum && null != noriginalcursummny
    		  && null != noriginalcurdiscountmny){
    	  setBodyValueAt((noriginalcursummny.add(noriginalcurdiscountmny)).div(npacknum),row,"norgviapricetax");
      //辅计量无税单价
      UFDouble ntaxrate = SmartVODataUtils.getUFDouble(getBodyValueAt(row, "ntaxrate"));
      if(null != ntaxrate)
    	  setBodyValueAt((noriginalcursummny.add(noriginalcurdiscountmny))
    			  .div((UFDouble.ONE_DBL.add(ntaxrate.div(new UFDouble(100)))).multiply(npacknum)),row,"norgviaprice"); 
      }
    }else {
        // 辅单位为空
       setBodyValueAt(null, row, "scalefactor");
       setBodyValueAt(null, row, "fixedflag");
       setBodyValueAt(null, row, "npacknumber");
       setBodyValueAt(null, row, "cpackunitid");
       setBodyValueAt(null, row, "cpackunitname");
    }

    // 报价单位
    String cquoteunitid = (String) getBodyValueAt(row, "cquoteunitid");

    if (null != cunitid  && null != cquoteunitid ) {
      if (cquoteunitid.equals(cunitid)) {
        setBodyValueAt(new UFDouble(1), row, "nqtscalefactor");
        setBodyValueAt(UFBoolean.TRUE, row, "bqtfixedflag");
      }
      else {
          Object objqtfixflag = getBodyValueAt(row, "bqtfixedflag");
          UFBoolean  qtfixedflag =objqtfixflag==null?
              UFBoolean.FALSE: SmartVODataUtils.getUFBoolean(objqtfixflag);
          //非固定换算率
          if(! qtfixedflag.booleanValue()){
            Object objnum = getBodyValueAt(row, "nnumber");
            UFDouble number = SmartVODataUtils.getUFDouble(objnum);
            
            Object  objquotenum = getBodyValueAt(row, "nquotenumber");
            UFDouble quotenum = SmartVODataUtils.getUFDouble(objquotenum);;
            if(null != number && null != quotenum
                && quotenum.compareTo(new UFDouble(0)) !=0){
            	setBodyValueAt(number.div(quotenum),row,"nqtscalefactor");
            }
          }
       }
    }else {
      setBodyValueAt(null, row, "cquoteunitid");
      setBodyValueAt(null, row, "cquoteunit");
      setBodyValueAt(null, row, "nqtscalefactor");
      setBodyValueAt(null, row, "bqtfixedflag");
    }
  }
  public void setOutSumMakeInvoice(boolean isGather) {
    this.isOutSumMakeInvoice = isGather;
  }

//  /**
//   * 计量单位编辑后事件处理。 创建日期：(2001-6-23 13:42:53)
//   * 
//   * @return nc.vo.pub.lang.UFDouble
//   */
//  private void afterUnitChange(int eRow) {
//
//    String cunitid = (String) getBodyValueAt(eRow, "cunitid");
//    String cpackunitid = (String) getBodyValueAt(eRow, "cpackunitid");
//    if (null != cunitid && null != cpackunitid) {
//      if (cpackunitid.equals(cunitid)) {
//        String[] formulas = new String[]{
//            "cpackunitname->getColValue(bd_measdoc,measname,pk_measdoc,cpackunitid)"
//        };
//        execBodyFormulas(eRow, formulas);
//        setBodyValueAt(new UFDouble(1), eRow, "scalefactor");
//        setBodyValueAt(UFBoolean.TRUE, eRow, "fixedflag");
//      }
//      else {
//        String[] formulas = new String[3];
//        // 包装单位名称
//        formulas[0] = "cpackunitname->getColValue(bd_measdoc,measname,pk_measdoc,cpackunitid)";
//        // 换算率
//        formulas[1] = "scalefactor->getColValue2(bd_convert,mainmeasrate,pk_invbasdoc,cinvbasdocid,pk_measdoc,cpackunitid)";
//        // 是否固定换算率
//        formulas[2] = "fixedflag->getColValue2(bd_convert,fixedflag,pk_invbasdoc,cinvbasdocid,pk_measdoc,cpackunitid)";
//        execBodyFormulas(eRow, formulas);
//      }
//    }
//    else {
//        // 辅单位为空
//       setBodyValueAt(null, eRow, "scalefactor");
//       setBodyValueAt(null, eRow, "fixedflag");
//       setBodyValueAt(null, eRow, "npacknumber");
//       setBodyValueAt(null, eRow, "cpackunitid");
//       setBodyValueAt(null, eRow, "cpackunitname");
//      
//    }
//
//    // 报价单位
//    String cquoteunitid = (String) getBodyValueAt(eRow, "cquoteunitid");
//
//    if (null != cunitid  && null != cquoteunitid ) {
//      if (cquoteunitid.equals(cunitid)) {
//        String[] formulas = new String[]{
//          "cquoteunitname->getColValue(bd_measdoc,measname,pk_measdoc,cquoteunitid)"
//        };
//        execBodyFormulas(eRow, formulas);
//        setBodyValueAt(new UFDouble(1), eRow, "nqtscalefactor");
//        setBodyValueAt(UFBoolean.TRUE, eRow, "bqtfixedflag");
//      }
//      else {
//        String[] formulas = new String[3];
//        // 报价单位名称
//        formulas[0] = "cquoteunitname->getColValue(bd_measdoc,measname,pk_measdoc,cquoteunitid)";
//        // 换算率
//        formulas[1] = "nqtscalefactor->getColValue2(bd_convert,mainmeasrate,pk_invbasdoc,cinvbasdocid,pk_measdoc,cquoteunitid)";
//        // 是否固定换算率
//        formulas[2] = "bqtfixedflag->getColValue2(bd_convert,fixedflag,pk_invbasdoc,cinvbasdocid,pk_measdoc,cquoteunitid)";
//        execBodyFormulas(eRow, formulas);
//      }
//    }
//    else {
//      setBodyValueAt(null, eRow, "cquoteunitid");
//      setBodyValueAt(null, eRow, "cquoteunit");
//
//      setBodyValueAt(null, eRow, "nqtscalefactor");
//      setBodyValueAt(null, eRow, "bqtfixedflag");
//    }
//
//  }

  /**
   * 方法功能描述：销售发票数量单价金额计算。
   * <b>参数说明</b>
   * @param row
   * @param key
   * @author fengjb
   * @time 2009-10-20 下午06:21:12
   */
  public void calculateNumber(int row, String key) {

    //缓存原先的整单折扣
    UFDouble ndiscountrate = getBodyValueAt(row, "ndiscountrate") == null?
    		new UFDouble(100):SmartVODataUtils.getUFDouble(getBodyValueAt(row, "ndiscountrate"));
   
    UFDouble nitemdiscount = getBodyValueAt(row, "nitemdiscountrate") == null? 
    		new UFDouble(100):SmartVODataUtils.getUFDouble(getBodyValueAt(row, "nitemdiscountrate"));
    // 把整单折扣 * 单品折扣作为订单折扣放到整单折扣字段上       	
    setBodyValueAt(ndiscountrate.multiply(nitemdiscount).div(new UFDouble(100)), row,"ndiscountrate");
	//调用公共方法计算
    nc.ui.scm.pub.panel.RelationsCal.calculate(row, this, getCalculatePara(key), key, SaleinvoiceBVO.getDescriptions(), SaleinvoiceBVO.getKeys(), "nc.vo.so.so002.SaleinvoiceBVO", "nc.vo.so.so002.SaleVO");
    //恢复整单折扣值
    setBodyValueAt(ndiscountrate, row,"ndiscountrate");
  }
  /**
   * 方法功能描述：取得单价金额算法所需的参数。
   * <b>参数说明</b>
   * @param key
   * @return
   * @author fengjb
   * @time 2009-10-20 下午06:20:55
   */
	public int[] getCalculatePara(String key){
		//含税优先机制:默认无税优先
		int m_iPrior_Price_TaxPrice = RelationsCalVO.PRICE_PRIOR_TO_TAXPRICE;
		if ( (key.equals("noriginalcursummny")//含税优先
				|| key.equals("noriginalcurtaxprice")|| key.equals("noriginalcurtaxnetprice"))
				|| (st.SA_02.booleanValue())
				)
			m_iPrior_Price_TaxPrice = RelationsCalVO.TAXPRICE_PRIOR_TO_PRICE;
		
		//调单价还是调折扣：默认调折扣
		int m_iPrior_ItemDiscountRate_Price = RelationsCalVO.ITEMDISCOUNTRATE_PRIOR_TO_PRICE;
		if (st.SO_40.equals("调整单价"))//调单价  /*-=notranslate=-*/
			m_iPrior_ItemDiscountRate_Price = RelationsCalVO.PRICE_PRIOR_TO_ITEMDISCOUNTRATE;
		
		int[] iaPrior = new int[]{m_iPrior_Price_TaxPrice,
				m_iPrior_ItemDiscountRate_Price,RelationsCalVO.YES_LOCAL_FRAC};
		
		return iaPrior;
	}
  /**
   * 当前正在操作的单据是否新单据
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-20 下午04:39:27
   */
  public boolean isNewBill() {
    
    String sBillId = PuPubVO.getString_TrimZeroLenAsNull((String) getHeadItem(
        "csaleid").getValueObject());
    if (sBillId == null) {
      return true;
    }

    return false;
  }

  public void countCardUniteMny() {
    for (int i = 0; i < getBillModel().getRowCount(); i++) {
      UFDouble dSubsummny = (UFDouble) getBodyValueAt(i, "nsubsummny");
      UFDouble dOrgcursummny = (UFDouble) getBodyValueAt(i,
          "noriginalcursummny");
      UFDouble nuniteinvoicemny = PuPubVO.getUFDouble_NullAsZero(dSubsummny)
          .sub(PuPubVO.getUFDouble_NullAsZero(dOrgcursummny));
      setBodyValueAt(nuniteinvoicemny, i, "nuniteinvoicemny");
    }

  }

  /**
   * 设置下拉自由项值 创建日期：(01-2-26 13:29:17)
   */
  protected void setBodyFreeValue(int row, InvVO voInv) {
    if (voInv != null) {
      voInv.setFreeItemValue("vfree1", (String) getBodyValueAt(row, "vfree1"));
      voInv.setFreeItemValue("vfree2", (String) getBodyValueAt(row, "vfree2"));
      voInv.setFreeItemValue("vfree3", (String) getBodyValueAt(row, "vfree3"));
      voInv.setFreeItemValue("vfree4", (String) getBodyValueAt(row, "vfree4"));
      voInv.setFreeItemValue("vfree5", (String) getBodyValueAt(row, "vfree5"));
    }
  }

  /**
   * 出库汇总开票时设置界面状态
   */
  public void setPanelWhenOutSumMakeInvoice() {
    setOutSumMakeInvoice(true);

    addNew();
    setEnabled(true);
    //自制发票时设置默认值
    setDefaultData();
     
//    showBodyTableCol("cupsourcebillcode");
//    hideBodyTableCol("csourcebillcode");

//    // 显示业务类型
//    ((UIRefPane) getHeadItem("cbiztype").getComponent())
//        .setPK(getBusiType());

    initSalePeopleRef();
    initSaleDeptRef();

  }

  /**
   * 编辑前事件处理。 创建日期：(2001-6-23 13:42:53)
   * 
   * @return nc.vo.pub.lang.UFDouble
   */
  public boolean beforeEdit(BillEditEvent e) {
    
    boolean bret = true;
    if (e.getPos() == BillItem.BODY) {
       //合并开票金额,不可编辑
      if ("nuniteinvoicemny".equals(e.getKey())) {
          bret = false;
      }
      /**V56 合并开票之后支持修改税额 begin**/
      else if("noriginalcurtaxmny".equals(e.getKey()) && isStrike())
         bret = true;
      /**V56 合并开票之后支持修改税额 end**/

      //如果已经做过合并开票则所有项目都不再允许编辑
      else if (isStrike()){
        bret = false;
      }
      //换算率
      else if("scalefactor".equals(e.getKey())){
        //是否辅计量
        Object objassistunit = getBodyValueAt(e.getRow(), "assistunit");
        UFBoolean assistunit = objassistunit==null?UFBoolean.FALSE:SmartVODataUtils.getUFBoolean(objassistunit);
        //是否固定换算率
        Object objfixedflag = getBodyValueAt(e.getRow(), "fixedflag");
        UFBoolean fixedflag = objfixedflag==null?UFBoolean.FALSE:SmartVODataUtils.getUFBoolean(objfixedflag);
        if (!assistunit.booleanValue() || fixedflag.booleanValue() )
          bret = false;
      }
      //报价计量单位换算率
      else if ("nqtscalefactor".equals(e.getKey())) {
        String cunitid = getBillCardTools().getBodyStringValue(e.getRow(), "cunitid");
        String cquoteunitid = getBillCardTools().getBodyStringValue(e.getRow(), "cquoteunitid");
        
        UFBoolean bqtfixedflag = getBillCardTools().getBodyUFBooleanValue(e.getRow(), "bqtfixedflag");
        if ( null == cunitid ||cunitid.equals(cquoteunitid) 
            || (null != bqtfixedflag && bqtfixedflag.booleanValue()))
          bret = false;
      }
     // 辅数量
     else if ("npacknumber".equals(e.getKey())) {
      String cpackunitid = (String) getBodyValueAt(e.getRow(), "cpackunitid");
      if (cpackunitid == null || cpackunitid.length() == 0)
        setCellEditable(e.getRow(), "npacknumber", false);
      else
        setCellEditable(e.getRow(), "npacknumber", true);
      bret = true;
    }
    // 包装单位
    else if ("cpackunitname".equals(e.getKey())) {
      UIRefPane cpackunitname = (UIRefPane) getBodyItem("cpackunitname")
          .getComponent();
      String cinvbasdocid = (String) getBodyValueAt(e.getRow(), "cinvbasdocid");
      // String cpackunitid = (String) getBodyValueAt(e.getRow(),
      // "cpackunitid");
      String cunitid = (String) getBodyValueAt(e.getRow(), "cunitid");
      cpackunitname
          .setWhereString(" (pk_measdoc in (select pk_measdoc from bd_convert where pk_invbasdoc = '"
              + cinvbasdocid + "') or pk_measdoc='" + cunitid + "' ) ");
      bret = true;
     }
      //下面编辑项目不受是否进行过合并开票的影响
      //项目阶段
     if ("cprojectphasename".equals(e.getKey())) {
        Object objprojectid = getBodyValueAt(e.getRow(), "cprojectid");
        UIRefPane refPane = (UIRefPane) getBodyItem("cprojectphasename")
            .getComponent();
        if (null != objprojectid) {
          ((nc.ui.bd.b39.PhaseRefModel) refPane.getRefModel()).setJobID(objprojectid
              .toString());
        }
        else {
          ((nc.ui.bd.b39.PhaseRefModel) refPane.getRefModel()).setJobID(null);
        }
        bret = true;
      }

    // 自由项
    else if (e.getKey().equals("vfree0")) {
      setInvFree(e);
      // 获得存货VO
      try {
        nc.vo.scm.ic.bill.InvVO voInv = (nc.vo.scm.ic.bill.InvVO) alInvs.get(e
            .getRow());
        setBodyFreeValue(e.getRow(), voInv);
        getFreeItemRefPane().setFreeItemParam(voInv);
      }
      catch (Exception ex) {
        nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000631"));
      }
      bret = true;
    }
    // 修改仓库
    else if (e.getKey().equals("cbodywarehousename")) {
      bret = beforeCbodywarehouseidEdit(e);
     }
    }
    //二次开发扩展
    if(!uipanel.getPluginProxy().beforeEdit(e))
      bret =false;
    return bret;

    }

  /**
   * 方法功能描述：返回自由项参照。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2009-8-11 下午06:43:41
   */
  private FreeItemRefPane getFreeItemRefPane() {
	  
    if (ivjFreeItemRefPane == null) {
      try {
        ivjFreeItemRefPane = new FreeItemRefPane();
        ivjFreeItemRefPane.setName("FreeItemRefPane");
        ivjFreeItemRefPane.setLocation(209, 4);
      } catch (java.lang.Throwable ivjExc) {
        SCMEnv.error(ivjExc);
      }
    }
    return ivjFreeItemRefPane;
  }

  /**
   * 方法功能描述：保存时获得待保存发票VO。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2009-8-14 下午03:14:57
   */
  public SaleinvoiceVO getSaveVO() {
	  
    // 行号校验 //2010-10-27 fengjb zuodm 保存时执行验证公式,如果为error不允许保存
    if (!BillRowNo.verifyRowNosCorrect(this, "crowno") || !getBillData().execValidateFormulas()) {
      return null;
    }
   
    //存在合并开票
    if (getHsSelectedARSubHVO().size() > 0) {
      try {
        // 检查冲应收单并发
        SaleinvoiceBO_Client.checkArsubValidity(getHsSelectedARSubHVO(),isStrike());
      }
      catch (Exception e) {
        SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000632")+e);
        MessageDialog.showErrorDlg(this, "", e.getMessage());
        return null;
      }
    }

    SaleinvoiceVO voInvoiceModify = (SaleinvoiceVO) this.getBillValueChangeVO(
        SaleinvoiceVO.class.getName(), SaleVO.class.getName(),
        SaleinvoiceBVO.class.getName());
    //表体公司
    String pk_corp = voInvoiceModify.getHeadVO().getPk_corp();
    for (SaleinvoiceBVO salebvo:voInvoiceModify.getBodyVO()) {
      //表体公司为空
      if(StringUtil.isEmptyWithTrim(salebvo.getPk_corp()))
        salebvo.setPk_corp(pk_corp);
    }
   
    // 汇总开票标志:分别给发票主表VO、聚合VO设置出库汇总开票标志字段
    voInvoiceModify.getHeadVO().setIsGather(new UFBoolean(isOutSumMakeInvoice()));
  
    voInvoiceModify.setIsOutGather(isOutSumMakeInvoice());

    if (!isNewBill()) {
      // 发票号是否修改
      String sCurCode = voInvoiceModify.getHeadVO().getVreceiptcode();
      boolean isCodeChanged = !(getOldVO().getHeadVO().getVreceiptcode()
          .equals(sCurCode));
      voInvoiceModify.getHeadVO().setBcodechanged(isCodeChanged);
      //发票号已修改
      if (isCodeChanged) {
    	  voInvoiceModify.getHeadVO().setVoldreceiptcode(getOldVO()
            .getHeadVO().getVreceiptcode());
      }
    }
    // 收货地址
    UIRefPane vreceiveaddress = (UIRefPane) getHeadItem("vreceiveaddress")
        .getComponent();
    voInvoiceModify.getHeadVO().setVreceiveaddress(vreceiveaddress
        .getText());
    
    try {
    //新增单据业务合法性检测
    if (isNewBill()) {
        if (isOutSumMakeInvoice())
          checkGather(voInvoiceModify);
        else
          checkVO(voInvoiceModify);
    //修改单据业务合法性检测
      }else{
    	  checkVO(voInvoiceModify);  
      }
    }
    catch (Exception e) {
        SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000633")+e);
        MessageDialog.showErrorDlg(this, "", e.getMessage());
        return null;
      }
    SaleinvoiceVO voAllInvoice = (SaleinvoiceVO) this.getBillValueVO(
        SaleinvoiceVO.class.getName(), SaleVO.class.getName(),
        SaleinvoiceBVO.class.getName());

    // 如果是对冲生成
    if (SaleVO.FCOUNTERACTFLAG_COUNTERACT_GEN == voInvoiceModify.getHeadVO().getFcounteractflag()) {
      // 赋冲减的数据
    	voInvoiceModify.setAllinvoicevo(voAllInvoice);
    }else {
        // 赋冲减的数据
    	voInvoiceModify.setAllinvoicevo(voAllInvoice);
    	voInvoiceModify.setHsSelectedARSubHVO(getHsSelectedARSubHVO());
    	voInvoiceModify.setBstrikeflag(new UFBoolean(isStrike()));
        // 赋帐期需要数据
    	voInvoiceModify.setDClientDate(SaleInvoiceTools.getLoginDate());
        //合并开票
    	voInvoiceModify.setHsArsubAcct(getHsSelectedARSubHVO());
    }
    // 如果是新单据
    if (isNewBill()) {
        //V56发票效率优化
    	voInvoiceModify.setAllinvoicevo(null);
        // 赋账期需要的数据
    	voInvoiceModify.setDClientDate(SaleInvoiceTools.getLoginDate());
    	voInvoiceModify.setStatus(VOStatus.NEW);
      
        for (SaleinvoiceBVO invoicebvo:voInvoiceModify.getBodyVO())
            invoicebvo.setStatus(VOStatus.NEW);
    }else{
    	voInvoiceModify.setStatus(VOStatus.UPDATED);
    }
   //增加是否需要自动合并开票的判断
   voInvoiceModify.setIsAutoUnit(isNewBill() && st.SO_49.booleanValue());
   //后台日志需要信息填充
   fillLogInfo(voInvoiceModify);
   
    return voInvoiceModify;
  }
/**
   * 方法功能描述：得到订单客户。
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * 
   * @param isCard
   * @return
   *          <p>
   * @author wsy
   * @time 2007-3-15 上午09:57:47
   */
  public String getCCustomerid() {
    if (getBillTable().getRowCount() == 0)
      return null;
    int iselbodyrow = getBillTable().getSelectedRow();
    if (iselbodyrow < 0)
      iselbodyrow = 0;
    return (String) getBodyValueAt(iselbodyrow, "ccustomerid");
  }
  /**
   * 方法功能描述：获得卡片界面上表体行所有的订单客户。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-9-12 下午02:47:48
   */
  public String[] getAllCustomerid(){
    int rowcount = getBillTable().getRowCount();
    //没有表体行O
    if (rowcount == 0)
      return null;
    HashSet<String> hscus = new HashSet<String>();
    Object ovalue = null;
    for(int i = 0;i<rowcount;i++){
      //获得每个产品线的值
      ovalue = getBodyValueAt(i, "ccustomerid");
      if(null == ovalue || ovalue.toString().length() ==0)
        continue;
      else
       hscus.add(ovalue.toString());
    }
    
    String[] customerid = new String[hscus.size()];
    hscus.toArray(customerid);
    return customerid;
    
  
  }
  /**方法功能描述：获取发票表头开票客户。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-9-10 下午01:35:33
   */
  public String getCreceiptcorpid() {
    return (String) getHeadItem("creceiptcorpid").getValueObject();
  }
  /**
   * 方法功能描述：获得卡片界面上表体行产品线。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-9-12 下午02:47:48
   */
  public String[] getAllProlined(){
    int rowcount = getBillTable().getRowCount();
    //没有表体行
    if (rowcount == 0)
      return null;
    HashSet<String> hsproline = new HashSet<String>();
    Object ovalue = null;
    for(int i = 0;i<rowcount;i++){
      ovalue = getBodyValueAt(i, "cprolineid");
      if(null == ovalue || ovalue.toString().length() ==0)
        continue;
      else
        hsproline.add(ovalue.toString());
    }
    String[] cprolined = new String[hsproline.size()];
    hsproline.toArray(cprolined);
    return cprolined;
  
  } 
  /**
   * 是否出库汇总
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * @return
   * <p>
   * @author wangyf
   * @time 2007-9-4 下午03:13:59
   */
  public boolean isOutSumMakeInvoice() {
    return isOutSumMakeInvoice;
  }

  /**
   * 菜单项选择。 创建日期：(01-2-23 15:03:07)
   */
  public void onMenuItemClick(java.awt.event.ActionEvent e) {
    UIMenuItem item = (UIMenuItem) e.getSource();
    if (item == getInsertLineMenuItem()) {
      actionInsertLine();
    }
    else if (item == getAddLineMenuItem()) {
      actionAddLine();
    }
    else if (item == getDelLineMenuItem()) {
      actionDelLine();
    }
    else if (item == getCopyLineMenuItem()) {
      actionCopyLine();
    }
    else if (item == getPasteLineMenuItem()) {
      actionPasteLine();
    }
    else if(item == getPasteLineToTailMenuItem()){
      actionPasteLineToTail();
    }
    getContainer().setButtonsState();
  }

  /**
   * 插入行
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-9 下午01:03:05
   */
  public void actionInsertLine() {
    int row = getBillTable().getSelectedRow();
    if(checkAddLine()){
      insertLine();
      setNewLineDefaultValue(row);
      // 计算并设置新增行号
      BillRowNo.insertLineRowNo(this, SaleBillType.SaleInvoice, "crowno");
     }
  }

  /**
   * 删除行
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-9 下午01:09:03
   */
  public void actionDelLine() {
    if (getBillModel().getRowCount() == 0) {
      MessageDialog.showErrorDlg(this, "", NCLangRes.getInstance().getStrByID(
          "SCMCommon", "UPPSCMCommon-000161")/*
                                               * @res "没有数据，不能删除！"
                                               */);
      return;
    }

    if (getBillTable().getSelectedRowCount() == 0) {
      MessageDialog.showErrorDlg(this, "", NCLangRes.getInstance().getStrByID(
          "SCMCommon", "UPPSCMCommon-000169")/* @res "请选择一行!" */);
      return;
    }

    if (MessageDialog.showOkCancelDlg(this, "", NCLangRes.getInstance()
        .getStrByID("SCMCommon", "UPPSCMCommon-000180")/*
                                                         * @res "确定要删除该行吗？"
                                                         */) == MessageDialog.ID_CANCEL) {
      return;
    }

    if (getContainer() instanceof ToftPanel) {
      ((ToftPanel) getContainer()).showHintMessage(NCLangRes.getInstance()
          .getStrByID("SCMCommon", "UPPSCMCommon-000466")/*
                                                           * @res "正在删除行..."
                                                           */);
    }
    // 删除行
    delLine();
    setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
    execHeadFormula("nnetmny->ntotalsummny-nstrikemny");

    if (getContainer() instanceof ToftPanel) {
      ((ToftPanel) getContainer()).showHintMessage(NCLangRes.getInstance()
          .getStrByID("SCMCommon", "UPPSCMCommon-000225")/*
                                                           * @res "删除成功"
                                                           */);
    }
  }

  /**
   * 复制行
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-9 下午01:26:34
   */
  public void actionCopyLine() {
	iCopyRowCount = 0;
    copyLine();
    iCopyRowCount = getBillTable()
	.getSelectedRowCount();
  }

  /**
   * 冲减相关是否正确
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-9 下午01:26:34
   */
  private boolean isUnitMoneyRight(int iRow) {

    UFDouble nUniteInvoiceMnyNEW = (getBodyValueAt(iRow, "nuniteinvoicemny") == null ? new UFDouble(
        0.0)
        : (UFDouble) getBodyValueAt(iRow, "nuniteinvoicemny"));
    if (nUniteInvoiceMnyNEW.compareTo(new UFDouble(0.0)) < 0) {
      MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
          .getStrByID("40060501", "UPP40060501-000098") /*
                                                         * @res "冲减金额不能调整为负值！"
                                                         */);
      setBodyValueAt(nUniteInvoiceMnyBeforeChange, iRow, "nuniteinvoicemny");
      return false;
    }

    UFDouble nSubSumMny = (getBodyValueAt(iRow, "nsubsummny") == null ? new UFDouble(
        0.0)
        : (UFDouble) getBodyValueAt(iRow, "nsubsummny"));
    UFDouble nOrgCurSumMny = (getBodyValueAt(iRow, "noriginalcursummny") == null ? new UFDouble(
        0.0)
        : (UFDouble) getBodyValueAt(iRow, "noriginalcursummny"));

    UFDouble nChangeMny = nUniteInvoiceMnyNEW.sub((nSubSumMny
        .sub(nOrgCurSumMny)));

    Hashtable hsFullData = null;
    try {
      hsFullData = SaleinvoiceBO_Client.fillDataWithARSubAcct(hsTotalBykey);
    }
    catch (Exception ex) {
      //ex.printStackTrace();
    }
    if (hsFullData == null) {
      MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
          .getStrByID("40060501", "UPP40060501-000099")/*
                                                         * @res "冲应收单数据查询有误！"
                                                         */
      );
      return false;
    }
    // 如果调大，则需要根据冲应收单进行分摊
    if (nChangeMny.compareTo(new UFDouble(0.0)) > 0) {
      // 将增加的金额分配到冲减应收的记录上
      if (st.SO_27.booleanValue()) {
        String cchangeAtProlineid = (String) getBodyValueAt(iRow, "cprolineid");
        java.util.Enumeration ekeys = hsFullData.keys();
        while (ekeys.hasMoreElements()) {
          String fulldataid = (String) ekeys.nextElement();
          String cprolineid = (String) ((ArrayList) hsFullData.get(fulldataid))
              .get(0);
          if (cprolineid != null && cprolineid.equals(cchangeAtProlineid)) {
            UFDouble narsubmny = (UFDouble) ((ArrayList) hsFullData
                .get(fulldataid)).get(1);
            UFDouble narsubinvmny = (UFDouble) ((ArrayList) hsFullData
                .get(fulldataid)).get(2);
            if ((narsubmny.sub(narsubinvmny)).compareTo(nChangeMny) < 0) {
              ((ArrayList) hsFullData.get(fulldataid)).set(2, narsubmny);
              nChangeMny = nChangeMny.sub(narsubmny.sub(narsubinvmny));
            }
            else {
              ((ArrayList) hsFullData.get(fulldataid)).set(2, narsubinvmny
                  .add(nChangeMny));
              nChangeMny = new UFDouble(0.0);
            }
          }
        }
      }
      else {
        java.util.Enumeration ekeys = hsFullData.keys();
        while (ekeys.hasMoreElements()) {
          String fulldataid = (String) ekeys.nextElement();
          UFDouble narsubmny = (UFDouble) ((ArrayList) hsFullData
              .get(fulldataid)).get(1);
          UFDouble narsubinvmny = (UFDouble) ((ArrayList) hsFullData
              .get(fulldataid)).get(2);
          if ((narsubmny.sub(narsubinvmny)).compareTo(nChangeMny) < 0) {
            ((ArrayList) hsFullData.get(fulldataid)).set(2, narsubmny);
            nChangeMny = nChangeMny.sub(narsubmny.sub(narsubinvmny));
          }
          else {
            ((ArrayList) hsFullData.get(fulldataid)).set(2, narsubinvmny
                .add(nChangeMny));
            nChangeMny = new UFDouble(0.0);
          }
        }
      }
      if (nChangeMny.compareTo(new UFDouble(0.0)) > 0) {
        MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
            .getStrByID("40060501", "UPP40060501-000100")/*
                                                           * @res
                                                           * "修改后的冲减金额值已经超过选择冲应收单的可冲减范围，请重新调整！"
                                                           */);
        setBodyValueAt(nUniteInvoiceMnyBeforeChange, iRow, "nuniteinvoicemny");
        return false;
      }
    }
    // 调小
    else {
      nChangeMny = (new UFDouble(0.0)).sub(nChangeMny);
      if (st.SO_27.booleanValue()) {
        String cchangeAtProlineid = (String) getBodyValueAt(iRow, "cprolineid");
        java.util.Enumeration ekeys = hsFullData.keys();
        while (ekeys.hasMoreElements()) {
          String fulldataid = (String) ekeys.nextElement();
          String cprolineid = (String) ((ArrayList) hsFullData.get(fulldataid))
              .get(0);
          if (cprolineid != null && cprolineid.equals(cchangeAtProlineid)) {
            UFDouble narsubinvmny = (UFDouble) ((ArrayList) hsFullData
                .get(fulldataid)).get(2);
            if (narsubinvmny.compareTo(nChangeMny) < 0) {
              ((ArrayList) hsFullData.get(fulldataid))
                  .set(2, new UFDouble(0.0));
              nChangeMny = nChangeMny.sub(narsubinvmny);
            }
            else {
              ((ArrayList) hsFullData.get(fulldataid)).set(2, narsubinvmny
                  .sub(nChangeMny));
              break;
            }
          }
        }
      }
      else {
        java.util.Enumeration ekeys = hsFullData.keys();
        while (ekeys.hasMoreElements()) {
          String fulldataid = (String) ekeys.nextElement();
          UFDouble narsubinvmny = (UFDouble) ((ArrayList) hsFullData
              .get(fulldataid)).get(2);
          if (narsubinvmny.compareTo(nChangeMny) < 0) {
            ((ArrayList) hsFullData.get(fulldataid)).set(2, new UFDouble(0.0));
            nChangeMny = nChangeMny.sub(narsubinvmny);
          }
          else {
            ((ArrayList) hsFullData.get(fulldataid)).set(2, narsubinvmny
                .sub(nChangeMny));
            break;
          }
        }
      }
    }

    // 判断是否超过了发票的可冲减金额SO_27:是否严格按照产品线冲减；st.SO_22:冲减比例
    Hashtable hsArsub = new Hashtable();
    java.util.Enumeration eacctkeys = hsFullData.keys();
    int icount = 0;
    while (eacctkeys.hasMoreElements()) {
      String cacctid = (String) eacctkeys.nextElement();
      // UFDouble ninvmny = (UFDouble) hsSelectedARSubHVO.get(cacctid);
      UFDouble ninvmny = (UFDouble) ((ArrayList) hsFullData.get(cacctid))
          .get(2);
      ArrayList aryData = (ArrayList) hsFullData.get(cacctid);
      String cprolineid = (aryData.get(0) == null ? (new Integer(icount))
          .toString() : aryData.get(0).toString());
      hsArsub.put(cprolineid, ninvmny);
      icount++;
    }

    SaleinvoiceVO saleinvoice = (SaleinvoiceVO) getBillValueVO(
        "nc.vo.so.so002.SaleinvoiceVO", "nc.vo.so.so002.SaleVO",
        "nc.vo.so.so002.SaleinvoiceBVO");

    if (isStrikeBalance(saleinvoice, hsArsub)) {
      return false;
    }

    // 进行合并开票处理结束
    // 给hsSelectedARSubHVO附新值
    java.util.Enumeration eselectedkeys = hsFullData.keys();
    while (eselectedkeys.hasMoreElements()) {
      String carsubacctid = (String) eselectedkeys.nextElement();
      UFDouble narsubinvmny = (UFDouble) ((ArrayList) hsFullData
          .get(carsubacctid)).get(2);
      hsSelectedARSubHVO.put(carsubacctid, narsubinvmny);
      hsTotalBykey.put(carsubacctid, narsubinvmny);
    }

    return true;
  }
  /**
   * ?user> 功能： 参数： 返回： 例外： 日期：(2004-05-14 15:57:37) 修改日期，修改人，修改原因，注释标志：
   * 
   * @param e
   *          nc.ui.pub.bill.BillEditEvent
   */
  private void setInvFree(BillEditEvent e) { // 设置自由项
    try {
      if (alInvs != null) {
        String sTempID1 = (String) getBodyValueAt(e.getRow(), "cinventoryid");
        String sTempID2 = null;
        java.util.ArrayList alIDs = new java.util.ArrayList();
        alIDs.add(sTempID2);
        alIDs.add(sTempID1);
        alIDs.add(ClientEnvironment.getInstance().getUser().getPrimaryKey());
        alIDs.add(st.getLoginPk_Corp());
        InvVO voInv = (InvVO) nc.ui.so.pub.BillTools.queryInfo(new Integer(0),
            alIDs);
        if (alInvs.size() == 0)
          alInvs.add(voInv);
        else
          alInvs.set(e.getRow(), voInv);
      }
    }
    catch (Exception ex) {
      nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000631"));
    }
  }

//  /**
//   * 编辑前事件处理。 创建日期：(2001-6-23 13:42:53)
//   * 
//   * @return nc.vo.pub.lang.UFDouble
//   */
//  private void beforeUnitChange(int row) {
//    // 包装单位
//    UIRefPane cpackunitname = (UIRefPane) getBodyItem("cpackunitname")
//        .getComponent();
//    String cinvbasdocid = (String) getBodyValueAt(row, "cinvbasdocid");
//    String cunitid = (String) getBodyValueAt(row, "cunitid");
//    cpackunitname
//        .setWhereString(" (pk_measdoc in (select pk_measdoc from bd_convert where pk_invbasdoc = '"
//            + cinvbasdocid + "') or pk_measdoc='" + cunitid + "') ");
//  }

  /**
   * mlr
   * 方法功能描述：数量编辑后事件。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-10-20 下午06:40:56
   */
  public void afterNumberEdit(BillEditEvent e) {
    //	关闭小计合计开关
	boolean bisCalculate = getBillModel().isNeedCalculate();
	getBillModel().setNeedCalculate(false);
	
	//编辑数量、辅数量都交由公共算法处理
    calculateNumber(e.getRow(), e.getKey());
    setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
    execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    
    /*** V56 销售发票在合并开票之后税额可以编辑带来的问题 begin **/
    //原币价税合计
    Object objorglcursummny = getBodyValueAt(e.getRow(), "noriginalcursummny");
    UFDouble norglcursummny = SmartVODataUtils.getUFDouble(objorglcursummny);
    //本币价税合计
    Object objsummny = getBodyValueAt(e.getRow(), "nsummny");
    UFDouble nsummny = SmartVODataUtils.getUFDouble(objsummny);
    //合并开票金额
    Object objunitemny = getBodyValueAt(e.getRow(), "nuniteinvoicemny");
    UFDouble norglunitemny = SmartVODataUtils.getUFDouble(objunitemny);
    //如果没有做过合并开票直接把原币价税合计和本币价税合计赋值到对应的冲减前即可
    if( null == norglcursummny 
        || (null == norglunitemny  || norglunitemny.compareTo(new UFDouble(0)) == 0)){
    setBodyValueAt(norglcursummny,e.getRow(), "nsubsummny");
    setBodyValueAt(nsummny, e.getRow(),"nsubcursummny"); 
    //冲减前单价处理：由于冲减前单价基本没有使用，所以可以考虑以后废弃
	//冲减前报价单位本币含税单价
    Object ovalue = getBodyValueAt(e.getRow(), "nquotetaxprice");
    setBodyValueAt(ovalue,e.getRow(),"nsubquotetaxprice");
	//冲减前报价单位本币无税单价
    ovalue = getBodyValueAt(e.getRow(), "nquoteprice");
    setBodyValueAt(ovalue,e.getRow(),"nsubquoteprice ");
	//冲减前报价单位本币含税净价
    ovalue = getBodyValueAt(e.getRow(), "nquotetaxnetprice");
    setBodyValueAt(ovalue,e.getRow(),"nsubquotetaxnetprice");
	//冲减前报价单位本币无税净价
    ovalue = getBodyValueAt(e.getRow(), "nquotenetprice");
    setBodyValueAt(ovalue,e.getRow(),"nsubquotenetprice ");
	//冲减前主计量单位本币含税净价
    ovalue = getBodyValueAt(e.getRow(), "ntaxnetprice");
    setBodyValueAt(ovalue,e.getRow(),"nsubtaxnetprice ");
    
    //做过合并开票需要加上合并开票金额
    }else{
      //冲减前原币金额 = 价税合计 + 合并开票金额
      setBodyValueAt(norglcursummny.add(norglunitemny),e.getRow(), "nsubsummny");
      //计算冲减前本币价税合计
      String pk_curtype = (String)getBodyValueAt(e.getRow(), "ccurrencytypeid");
      String dbilldate = (String)getHeadItem("dbilldate").getValueObject();
      UFDouble nexchangeotobrate = SmartVODataUtils
                  .getUFDouble(getBodyValueAt(e.getRow(), "nexchangeotobrate"));
      //币种VO
      UFDouble  nunitmny = null;
      //没有折本汇率
      if(null == nexchangeotobrate || nexchangeotobrate.compareTo(new UFDouble(0)) == 0){
        setBodyValueAt(objsummny, e.getRow(),"nsubcursummny");
        return ;
      //存在折本汇率
      } else{
      try{
         nunitmny = currateutil.getAmountByOpp(socur.getLocalCurrPK(),
              pk_curtype,norglunitemny, nexchangeotobrate,
              dbilldate == null ? ClientEnvironment.getInstance().getDate()
                  .toString() : dbilldate);
      }catch(Exception e1 ){
        SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000634") + e1);
        nunitmny = norglunitemny;
       }
      }
      // 冲减前本币金额 = 本币价税合计 + 合并开票金额
      setBodyValueAt(nsummny.add(nunitmny),e.getRow(), "nsubcursummny");
    }
    /*** V56 销售发票在合并开票之后税额可以编辑带来的问题 end **/
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**
   * 父类方法重写,计算合计
   * 
   * @see nc.ui.pub.bill.BillTotalListener#calcurateTotal(java.lang.String)
   */
  public UFDouble calcurateTotal(String key) {
    UFDouble total = new UFDouble(0.0);
    for (int i = 0,iloop =getRowCount(); i < iloop; i++) {
      UFBoolean blargessflag = SmartVODataUtils.getUFBoolean(getBodyValueAt(
        i, "blargessflag"));
    if (SaleinvoiceBVO.isPriceOrMnyKey(key)
        && (null != blargessflag && blargessflag.booleanValue()))
      continue;

    Object value = getBodyValueAt(i, key);
    String v = (value == null || value.toString().trim().length() <= 0) ? "0"
        : value.toString();
    total = total.add(new UFDouble(v));}
    return total;
  }

  /**
   * 方法功能描述：存货编辑后事件。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-8-18 下午06:15:26
   */
  private void afterInventoryEdit(BillEditEvent e) {
    //	关闭小计合计开关
	boolean bisCalculate = getBillModel().isNeedCalculate();
	getBillModel().setNeedCalculate(false);
    // 清除数据
    String[] clearCol = {
        "scalefactor", "fixedflag", "creceipttype", "csourcebillid",
        "csourcebillbodyid", "nnumber", "npacknumber", "noriginalcurprice",
        "noriginalcurtaxprice", "noriginalcurnetprice",
        "noriginalcurtaxnetprice", "noriginalcurtaxmny", "noriginalcurmny",
        "noriginalcursummny", "noriginalcurdiscountmny", "assistunit","cinvclassid"
    };
    clearRowData(e.getRow(), clearCol);
    // 设置自由项
    try {
      if (null != alInvs) {
        String cinventoryid = (String) getBodyValueAt(e.getRow(), "cinventoryid");
        ArrayList<String> alIDs = new ArrayList<String>();
        alIDs.add(null);
        alIDs.add(cinventoryid);
        alIDs.add(SaleInvoiceTools.getLoginUserId());
        alIDs.add(SaleInvoiceTools.getLoginPk_Corp());
        InvVO voInv = (InvVO) nc.ui.so.pub.BillTools.queryInfo(new Integer(0),
            alIDs);
        //添加存货信息到缓存
        if (alInvs.size() == 0)
          alInvs.add(voInv);
        else
          alInvs.set(e.getRow(), voInv);
      }
    }
    catch (Exception ex) {
      SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000631")+ex.getMessage());
    }
    
    setBodyValueAt(st.SO_34, e.getRow(), "nnumber");
    setBodyValueAt(st.SO_34, e.getRow(), "nquotenumber");
    String cunitid = (String) getBodyValueAt(e.getRow(), "cunitid");
    setBodyValueAt(cunitid, e.getRow(), "cquoteunitid");
    setBodyValueAt(new UFDouble(1), e.getRow(), "nqtscalefactor");
    setBodyValueAt(UFBoolean.TRUE, e.getRow(), "bqtfixedflag");
    String[] formulas = new String[]{
    		"cprolineid->getColValue(bd_invbasdoc,pk_prodline,pk_invbasdoc,cinvbasdocid)",
            "cprolinename->getColValue(bd_prodline,prodlinename,pk_prodline,cprolineid)",
       	    "ctaxitemid->iif(getColValue(bd_invmandoc,mantaxitem,pk_invmandoc,cinventoryid)==null," +
            "getColValue(bd_invbasdoc,pk_taxitems,pk_invbasdoc,cinvbasdocid)," +
            "getColValue(bd_invmandoc,mantaxitem,pk_invmandoc,cinventoryid))",
            "ntaxrate->getColValue(bd_taxitems,taxratio,pk_taxitems,ctaxitemid)",
            "cinvclassid->getColValue(bd_invbasdoc,pk_invcl,pk_invbasdoc,cinvbasdocid)",
            "invclasscode->getColValue(bd_invcl, invclasscode,pk_invcl, cinvclassid)",
            "invclassname->getColValue(bd_invcl, invclassname,pk_invcl, cinvclassid)",
            "laborflag->getColValue(bd_invbasdoc,laborflag,pk_invbasdoc,cinvbasdocid)",
            "cpackunitname->getColValue(bd_measdoc,measname,pk_measdoc,cpackunitid)",
            "scalefactor->iif(cunitid = cpackunitid, 1, getColValue2(bd_convert,mainmeasrate,pk_invbasdoc,cinvbasdocid,pk_measdoc,cpackunitid))",
            "fixedflag->iif(cunitid = cpackunitid, \"Y\", getColValue2(bd_convert,fixedflag,pk_invbasdoc,cinvbasdocid,pk_measdoc,cpackunitid))",
            "cquoteunitname->getColValue(bd_measdoc,measname,pk_measdoc,cquoteunitid)",
            "assistunit->getColValue(bd_invbasdoc,assistunit,pk_invbasdoc,cinvbasdocid)"
    };
    execBodyFormulas(e.getRow(), formulas);
    //如果辅计量需要计算换算率
    if (setAssistChange(e.getRow())) {
      initScalefactor(e.getRow());
      calculateNumber(e.getRow(), "nnumber");
    }
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**
   * 币种编辑后事件处理。 创建日期：(2001-6-23 13:42:53)
   * 
   * @return nc.vo.pub.lang.UFDouble
   */
  private void afterCurrencyChange(String billdate) {

    UIRefPane ccurrencytypeid = (UIRefPane) getHeadItem("ccurrencyid")
        .getComponent();
    if (ccurrencytypeid.getRefPK() == null)
      return;

    try {
      setExchgEditBycurrency(ccurrencytypeid.getRefPK());

        UFDouble rates = socur.getExchangeRate(ccurrencytypeid
            .getRefPK(), billdate);
        UFDouble dCurr0 = rates;

        setHeadItem("nexchangeotobrate", dCurr0);
        nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000635") + dCurr0);

      
    }
    catch (java.lang.Exception e1) {
      nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000636"));
    }
  }

  /**
   * 用途：合并开票判定函数
   * 
   * @return java.lang.Boolean
   * @param saleinvoice
   *          nc.vo.so.so002.SaleinvoiceVO
   * @param arlArSub
   *          java.util.ArrayList
   * @param byProLine
   *          java.lang.Boolean
   */
  private boolean isStrikeBalance(SaleinvoiceVO saleinvoice, Hashtable hsArSub) {

	 /**V55发票表体订单客户和冲应收单客户匹配进行冲应收单合并开票**/
    boolean byProLine = st.SO_27.booleanValue();
    // 如果按照产品线进行冲减
    if (byProLine == true) {
      Hashtable summoneyByProductLine = new Hashtable(); // 发票按产品线的价税合计
      Hashtable presummoneyByProductLine = new Hashtable(); // 发票按产品报价单位的当前的价税合计
      // strikemoneyByProductLine = hsArSub; // 冲应收单的可冲减金额
      for (int i = 0; i < saleinvoice.getChildrenVO().length; i++) {
        SaleinvoiceBVO saleinvoicebody = (SaleinvoiceBVO) saleinvoice.getChildrenVO()[i];
        if (saleinvoicebody.getBlargessflag().booleanValue()
            || st.isLaborOrDiscount(saleinvoicebody) 
            || saleinvoicebody.getCprolineid() == null
            || saleinvoicebody.getNoriginalcurmny().doubleValue() < 0)
          continue;
        if (!summoneyByProductLine.containsKey(saleinvoicebody.getCprolineid())) {
          summoneyByProductLine.put(saleinvoicebody.getCprolineid(),
              saleinvoicebody.getNsubsummny());
          presummoneyByProductLine.put(saleinvoicebody.getCprolineid(),
              saleinvoicebody.getNsummny());
        }
        else {
          UFDouble subsummny = saleinvoicebody.getNsubsummny();
          UFDouble summny = saleinvoicebody.getNsummny();
          subsummny = subsummny.add((UFDouble) summoneyByProductLine
              .get(saleinvoicebody.getCprolineid()));
          summny = summny.add((UFDouble) presummoneyByProductLine
              .get(saleinvoicebody.getCprolineid()));
          summoneyByProductLine.put(saleinvoicebody.getCprolineid(), subsummny);
          presummoneyByProductLine.put(saleinvoicebody.getCprolineid(), summny);
        }
      }
      java.util.Enumeration esummoney = summoneyByProductLine.keys();
      while (esummoney.hasMoreElements()) {
        String key = (String) esummoney.nextElement();
        UFDouble money = ((UFDouble) summoneyByProductLine.get(key)).multiply(
            st.SO_22).div(new UFDouble(100)).sub(
            (UFDouble) summoneyByProductLine.get(key)).add(
            (UFDouble) presummoneyByProductLine.get(key));
        summoneyByProductLine.put(key, money);
      }
      // 发票可冲减金额已经设置完成
      // Judge
      java.util.Enumeration eStrikeBill = hsArSub.keys();
      while (eStrikeBill.hasMoreElements()) {
        String key = (String) eStrikeBill.nextElement();
        if (((UFDouble) hsArSub.get(key)).compareTo(summoneyByProductLine
            .get(key) == null ? new UFDouble(0)
            : (UFDouble) summoneyByProductLine.get(key)) > 0) {
          MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
              .getStrByID("40060501", "UPP40060501-000091")/*
                                                             * @res
                                                             * "冲应收单可冲减金额大于发票可冲减金额，不能进行合并开票！"
                                                             */);
          return false;

        }
      }
      return true;
    }
    // 如果不按照产品线进行冲减
    else {
      UFDouble summoney = new UFDouble(0);
      presummoney = new UFDouble(0);
      strikemoney = new UFDouble(0);
      for (int i = 0; i < saleinvoice.getChildrenVO().length; i++) {
        SaleinvoiceBVO saleinvoicebody = (SaleinvoiceBVO) saleinvoice.getChildrenVO()[i];
        if (!saleinvoicebody.getBlargessflag().booleanValue()
            && !st.isLaborOrDiscount(saleinvoicebody)
            && saleinvoicebody.getNoriginalcurmny().doubleValue() > 0) {
          summoney = summoney.add((UFDouble) saleinvoicebody.getNsubsummny());
          presummoney = presummoney.add((UFDouble) saleinvoicebody.getNoriginalcursummny());
        }
      }
      summoney = summoney.multiply(st.SO_22).div(
          new UFDouble(100)).sub(summoney).add(presummoney);
      java.util.Enumeration eStirkeMoney = hsArSub.keys();
      while (eStirkeMoney.hasMoreElements()) {
        String key = (String) eStirkeMoney.nextElement();
        strikemoney = strikemoney.add((UFDouble) hsArSub.get(key));
      }
      // judge
      if (strikemoney.compareTo(summoney) > 0) {
        MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
            .getStrByID("40060501", "UPP40060501-000091")/*
                                                           * @res
                                                           * "冲应收单可冲减金额大于发票可冲减金额，不能进行合并开票！"
                                                           */);
        return false;
      }
      else
        return true;
    }
  }

  /**
   * 方法功能描述：从卡片界面获得发票VO。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2009-8-15 下午01:08:26
   */
  public SaleinvoiceVO getVO() {
	 //界面获得VO
    SaleinvoiceVO voRet = (SaleinvoiceVO) getBillValueVO(SaleinvoiceVO.class.getName(),
        SaleVO.class.getName(), SaleinvoiceBVO.class.getName());
    
    if(voRet.getHeadVO().getFstatus() == null
    		&& (null == voRet.getBodyVO() || voRet.getBodyVO().length == 0)) 
      return null;
    
    //后台需要日志信息填充
    fillLogInfo(voRet);
    
    // 收货地址
    UIRefPane vreceiveaddress = (UIRefPane) getHeadItem("vreceiveaddress")
        .getComponent();
    voRet.getHeadVO().setVreceiveaddress(vreceiveaddress
        .getText());

    return voRet;
  }
  /**
   * 方法功能描述：设置对冲卡片界面可编辑性。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-21 上午11:16:47
   */
  private void setOppCellEditable() {
    // 表头可编辑列
//    HashMap<String, String> hmcols = new HashMap<String, String>();
//    hmcols.put("vreceiptcode", "vreceiptcode");
//    hmcols.put("dbilldate", "dbilldate");
//    hmcols.put("vnote", "vnote");
    
    Hashtable htcols = new Hashtable();
    //ncm linsf 20100329农资_允许金税票号字段编辑201003251125040211
    htcols.put("cgoldtaxcode", "cgoldtaxcode");
    //ncm linsf
    htcols.put("vreceiptcode", "vreceiptcode");
    htcols.put("dbilldate", "dbilldate");
    htcols.put("vnote", "vnote");
    for (int i = 1; i <= 20; i++) {
    	htcols.put("vdef" + i, "vdef" + i);
    }

    // 表体可编辑列
    htcols.put("frownote", "frownote");

    BillItem[] headits = getHeadItems();
    for (int i = 0; i < headits.length; i++) {
      if (headits[i].isShow() && headits[i].isEnabled()) {
        if (!htcols.containsKey(headits[i].getKey())) {
          getHeadItem(headits[i].getKey()).setEnabled(false);
        }
      }
    }

    BillItem[] bodyits = getBodyItems();
    for (int i = 0; i < bodyits.length; i++) {
      if (htcols.containsKey(bodyits[i].getKey())) {
        getBodyItem(bodyits[i].getKey()).setEnabled(true);
      }
      else {
        getBodyItem(bodyits[i].getKey()).setEnabled(false);
      }
    }

    for (int i = 0; i < getRowCount(); i++) {
      setCellEditable(i, "cpackunitname", false);
      setCellEditable(i, "npacknumber", false);
    }

    updateUI();

  }

  /**
   * 方法功能描述：加载对冲发票信息到卡片界面。
   * <b>参数说明</b>
   * @param newvo
   * @return
   * @author fengjb
   * @time 2009-8-21 上午11:14:56
   */
  public SaleinvoiceVO setPanelWhenOPP(SaleinvoiceVO newvo) {

    loadCardData(newvo);
    setEnabled(true);
    setOppCellEditable();
    //设置表体行状态
    for(int i=0,iloop =getRowCount();i<iloop;i++ ){
    	getBillModel().setRowState(i,BillModel.ADD);
      }
    return newvo;
  }

  /**
   * 方法功能描述：审批、弃审、删除动作获取卡片界面VO。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2009-8-20 下午03:14:04
   */
  public SaleinvoiceVO getVOForAction() {
	  
    SaleinvoiceVO voRet = (SaleinvoiceVO) getBillValueVO(SaleinvoiceVO.class.getName(),
        SaleVO.class.getName(), SaleinvoiceBVO.class.getName());
    // 单据类型
    voRet.getHeadVO().setCreceipttype(SaleBillType.SaleInvoice);
    // 审批人
    voRet.getHeadVO().setCapproveid(SaleInvoiceTools.getLoginUserId());
    
    voRet.setHsSelectedARSubHVO(hsSelectedARSubHVO);
    //只有删除会使用到合并开票信息，放在删除方法里处理
//    try {
//      voRet.setHsArsubAcct(SaleinvoiceBO_Client.queryStrikeData(voRet
//          .getPrimaryKey()));
//    }
//    catch (Exception e) {
//      MessageDialog.showErrorDlg(this, "", e.getMessage());
//    }
    //  填充日志需要信息
    fillLogInfo(voRet);
    return voRet;
  }

  /**
   * 修改时调用此方法
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-13 上午09:34:24
   */
  public void modify(SaleinvoiceVO voEdit) {
    //关闭小计合计开关
    boolean bisCalculate = getBillModel().isNeedCalculate();
    getBillModel().setNeedCalculate(false);
	 //重新设置编辑性(生成对冲发票时会修改编辑性)
	  if (null != voEdit.getHeadVO().getFcounteractflag()
        && SaleVO.FCOUNTERACTFLAG_COUNTERACT_GEN != voEdit.getHeadVO().getFcounteractflag().intValue()) {
		 for(BillItem bodyitem:getBodyShowItems()) 
          resumeBillBodyItemEdit(bodyitem);
	  }
	  
	  // 如果是新单据
    if (voEdit.getHeadVO().isNew()) {
      // 设置新增数据到CARD
      setPanelForNewBill(voEdit);
    }
    //设置辅计量编辑性
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) 
      setAssistChange(i);
      
    // 设置赠品行编辑性
    getBillCardTools().setCardPanelCellEditableByLargess(
        st.SO_59.booleanValue());

    // 保存修改前单据号
    SaleinvoiceVO hvo = (SaleinvoiceVO) getVO();
    setOldVO(hvo);

    setEnabled(true);

    // 将光标定位在第一个可编辑域
    transferFocusTo(BillCardPanel.HEAD);

	//V56销售结算规则对销售发票的要求：

	// 出库传暂估应收，发票回冲应收：允许发票修改开票客户、表头币种。发票结算后，回冲暂估应收并形成确认应收。

	// 出库传确认应收，发票传差额应收：不允许发票修改开票客户、表头币种。否则会造成确认应收单与调整确认应收单的开票客户不一致
	ISquareQuery bosquare = (ISquareQuery)NCLocator.getInstance().lookup(ISquareQuery.class.getName()); 
	String pk_corp = (String)getHeadItem("pk_corp").getValueObject();
	String biztype = (String)getHeadItem("cbiztype").getValueObject();
	
	boolean isadjust = false;
	try{
		isadjust = bosquare.isBizConfigAdjustIncome(pk_corp, biztype);
	}catch(Exception e){
		SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000637"));
		isadjust = false;
	}
	
	BillItem creceiptcorpid = getHeadItem("creceiptcorpid");
	creceiptcorpid.setEdit(!isadjust);
	creceiptcorpid.setEnabled(!isadjust);
	
	BillItem ccurrencyid = getHeadItem("ccurrencyid");
	ccurrencyid.setEdit(!isadjust);
	ccurrencyid.setEdit(!isadjust);
	
     //对冲产生的发票,只允许修改开票日期和单据号
    if (null != voEdit.getHeadVO().getFcounteractflag()
    		&& voEdit.getHeadVO().getFcounteractflag().intValue() == SaleVO.FCOUNTERACTFLAG_COUNTERACT_GEN) {
    	setEnabled(false);
    	
    	getHeadItem("vreceiptcode").setEdit(true);
    	getHeadItem("vreceiptcode").setEnabled(true);
    	
    	getHeadItem("dbilldate").setEdit(true);
    	getHeadItem("dbilldate").setEnabled(true);
    	
    	getHeadItem("cgoldtaxcode").setEdit(true);
    	getHeadItem("cgoldtaxcode").setEnabled(true);
    	
    	getHeadItem("vnote").setEdit(true);
    	getHeadItem("vnote").setEnabled(true);
    }
    
	getHeadItem("cbiztype").setEdit(false);
	getHeadItem("cbiztype").setEnabled(false);
	getBodyItem("nuniteinvoicemny").setEdit(false);
	getBodyItem("nuniteinvoicemny").setEnabled(false);
	
	
	 Object freecustflag = getHeadItem("bfreecustflag").getValueObject();
	 UFBoolean freeflag = freecustflag == null ? UFBoolean.FALSE :
	    	SmartVODataUtils.getUFBoolean(freecustflag);
	 if (freeflag.booleanValue()) {
	      getHeadItem("cfreecustid").setEdit(true);
	  }else {
	      getHeadItem("cfreecustid").setEdit(false);
	      getHeadItem("cfreecustid").setValue(null);
	  }
	// 修改时客户开户银行权限设置
	String receiptcorpvalue = (String) getHeadItem("creceiptcorpid")
				.getValueObject();

	if (null != receiptcorpvalue && receiptcorpvalue.length() > 0) {
			BillItem item = getHeadItem("ccustbankid");
			UIRefPane bankref = (UIRefPane) item.getComponent();
			bankref.getRefModel().setPk_corp(getCorp());
			bankref.getRefModel()
					.addWherePart(
							" and bd_bankaccbas.pk_bankaccbas in (select  k.pk_accbank from bd_custbank k,bd_cumandoc m  where  m.pk_corp='"
			+ getCorp()	+ "' and  k.pk_cubasdoc=m.pk_cubasdoc and m.pk_cumandoc='"+ receiptcorpvalue 
			+ "' UNION select  s.pk_bankaccbas from bd_bankaccbas s, bd_bankaccmng m where  m.pk_corp in" 
            +"( select  DISTINCT pk_corp1  from  bd_cubasdoc b, bd_cumandoc m where  isnull ( b.pk_corp1, '0' ) != '0' and m.pk_corp  = '"
            + getCorp() +"' and m.pk_cubasdoc  = b.pk_cubasdoc  and m.pk_cumandoc = '"
            + receiptcorpvalue +"')  and m.istrade  = 'Y'  and isnull ( s.mainaccount, '0' ) = '0'  and s.pk_bankaccbas  = m.pk_bankaccbas )");
		}
	// 更新值
    if (!voEdit.getHeadVO().isNew()) {
    if(st.SO_41.booleanValue()){
    	getTailItem("coperatorid").setValue(SaleInvoiceTools.getLoginUserId());
      }
      updateValue() ;  
    }
    
    UFDouble exchangeotobrate = ((SaleinvoiceBVO) voEdit.getChildrenVO()[0]).getNexchangeotobrate();
    setHeadItem("nexchangeotobrate", exchangeotobrate);

    getBillModel().setNeedCalculate(bisCalculate);
  }

  private SaleinvoiceVO getOldVO() {
    return m_oldVO;
  }

  public void setOldVO(SaleinvoiceVO oldVO) {
    this.m_oldVO = oldVO;
  }

  /**
   * 方法功能描述：根据币种设置折本折辅的编辑性
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * 
   * @param ccurrencytypeid
   *          <p>
   * @author wsy
   * @time 2007-1-29 下午03:25:30
   */
  private void setExchgEditBycurrency(String ccurrencytypeid)
      throws BusinessException {

        if (st.getSoBusiCurrUtil().isLocalCurrType(ccurrencytypeid)) {
          // 如果币种等于本币种则折辅助汇率，本币汇率不可以修改
          getHeadItem("nexchangeotobrate").setEnabled(false);

        }
        else {
          getHeadItem("nexchangeotobrate").setEnabled(true);

        }
      }



  /**
   * 响应界面行变换事件 创建日期：(2001-4-19 10:44:02)
   */
  public void bodyRowChange(BillEditEvent e) {
    if (getContainer().getOperationState() == ISaleInvoiceOperState.STATE_EDIT) {
      if (e.getRow() > -1) {
        Object cfreezeid = getBodyValueAt(e.getRow(), "cfreezeid");
        if (cfreezeid != null && cfreezeid.toString().trim().length() != 0) {
          // 根据单据状态设置单据
//          lockRow(e.getRow());
        }

        getContainer().setButtonsState();
      }
    }
    //  二次开发扩展
    uipanel.getPluginProxy().bodyRowChange(e);
  }
  /**
   * 设置部门参照
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-13 上午08:25:50
   */
  private void setHeadDeptRef() {

    UIRefPane cdeptid = (UIRefPane) getHeadItem("cdeptid").getComponent();
    if (cdeptid != null)
      cdeptid.setWhereString(" bd_deptdoc.pk_corp='"
          + st.getLoginPk_Corp() + "'");
    UIRefPane Refpsn = (UIRefPane) getHeadItem("cemployeeid").getComponent();
    if (Refpsn != null)
      Refpsn.setWhereString(" bd_psndoc.pk_corp='"
          + st.getLoginPk_Corp() + "'");

  }

  /**
   * 修改的单据保存成功后加载ID到界面
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * 
   * @param listID
   *          [i]新增行的ID
   *          <p>
   * @author wangyf
   * @time 2007-8-3 下午04:51:11
   */
  public void loadIDafterEDIT(ArrayList listID) {
    if (listID == null || listID.size() == 0)
      return;
    int i = 0;
    for (int j = 0; j < getRowCount(); j++) {
      String strBodyID = (String) (getBodyValueAt(j, "cinvoice_bid"));
      if (strBodyID == null || strBodyID.length() == 0) {
        // 查到行ID为空的，将新ID载入
        setBodyValueAt(getHeadItem("csaleid").getValue(), j, "csaleid");
        setBodyValueAt((String) listID.get(i), j, "cinvoice_bid");
        i++;
      }
    }
  }

  /**
   * 加载数据ID。 创建日期：(2001-11-15 9:02:22)
   */
  public void loadIDafterADD(ArrayList listID) {
    if (listID == null || listID.size() == 0)
      return;
    setHeadItem("csaleid", (String) listID.get(0));
    setHeadItem("vreceiptcode", (String) listID.get(1));
    for (int i = 2; i < listID.size(); i++) {
      setBodyValueAt((String) listID.get(0), i - 2, "csaleid");
      setBodyValueAt((String) listID.get(i), i - 2, "cinvoice_bid");
    }
  }

  /**
   * 保存成功后设置界面
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * 
   * @param listBillID
   *          保存方法返回的LIST，[0]ID、[1]CODE、[i]每行的ID
   * @param isInMsgPanel
   *          <p>
   * @author wangyf
   * @time 2007-8-3 下午03:54:38
   */
  public void setPanelAfterSave(SaleinvoiceVO newsaleinvoicevo,
      boolean isInMsgPanel) {
    boolean bisCalculate = getBillModel()
    .isNeedCalculate();
    getBillModel().setNeedCalculate(false);
    if (isNewBill()) {
      if(newsaleinvoicevo.getIsAutoUnit()
          ||newsaleinvoicevo.getIsOutGather()){
        loadCardData(newsaleinvoicevo);
      } 
      else {
        updateUIValueAfterADD(newsaleinvoicevo);
      }
      // 根据单据状态设置单据
      if (!isInMsgPanel) {
        getContainer().setButtonsState();
      }
    }
    else {
      udateUIAfterEDIT(newsaleinvoicevo);
    }

    // 保存后进行冲减缓存的处理
    processARBufAfterSave();

    // 设置辅数量和辅单位的不可编辑
    for (int i = 0,iloop =  getRowCount(); i <iloop; i++) {
      setCellEditable(i, "cpackunitname", false);
      setCellEditable(i, "npacknumber", false);
    }
    // 恢复部门和人员参照过滤
    setHeadDeptRef();

    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();

    // refresh
    updateValue();
    updateUI();

    setEnabled(false);
  }

  /**方法功能描述：修改保存时依据后台传递的新的聚合VO数据刷新前台界面显示。
   * <b>参数说明</b>
   * @param newsaleinvoicevo
   * @author fengjb
   * @time 2008-12-3 下午08:07:46
   */
  private void udateUIAfterEDIT(SaleinvoiceVO newsaleinvoicevo) {
    if (null == newsaleinvoicevo)
      return;
    //表头数据
    SaleVO newhead = newsaleinvoicevo.getHeadVO();
    String primarykey =  newhead.getCsaleid();
    setHeadItem("vreceiptcode", newhead.getVreceiptcode());
    setTailItem("dbilltime", newhead.getDbilltime());
    setHeadItem("ts", newhead.getTs());
    //表体数据
    SaleinvoiceBVO[] newbody =  newsaleinvoicevo.getBodyVO();
    if(null == newbody || newbody.length == 0)
      return ;
    HashMap<String,SaleinvoiceBVO> hsbody = new HashMap<String,SaleinvoiceBVO>();
    for(SaleinvoiceBVO bvo:newbody)
      hsbody.put(bvo.getCrowno(), bvo);
    
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
      Object rowno = getBodyValueAt(i, "crowno");
      if(hsbody.containsKey(rowno)){
      setBodyValueAt(primarykey, i, "csaleid");
      setBodyValueAt(hsbody.get(rowno).getCsale_bid(), i, "cinvoice_bid");
      setBodyValueAt(hsbody.get(rowno).getTs(), i, "ts");
      }
    }
  }
  /**方法功能描述：新增保存时依据后台传递的新的聚合VO刷新界面字段数值。
   * <b>参数说明</b>
   * @param newsaleinvoicevo
   * @author fengjb
   * @time 2008-12-3 下午07:51:53
   */
  private void updateUIValueAfterADD(SaleinvoiceVO newsaleinvoicevo) {
    //guyan、fangchan、fengjb保存时进行尾差处理需要刷新前台界面金额字段
    if (null == newsaleinvoicevo)
       return;
    //表头数据
    SaleVO newhead = newsaleinvoicevo.getHeadVO();
    String primarykey =  newhead.getCsaleid();
    setHeadItem("csaleid", primarykey);
    setHeadItem("vreceiptcode", newhead.getVreceiptcode());
    setTailItem("dbilltime", newhead.getDbilltime());
    setHeadItem("ts", newhead.getTs());
    //表体数据
    SaleinvoiceBVO[] newbody =  newsaleinvoicevo.getBodyVO();
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
      setBodyValueAt(primarykey, i, "csaleid");
      setBodyValueAt(newbody[i].getCsale_bid(), i, "cinvoice_bid");
      setBodyValueAt(newbody[i].getTs(), i, "ts");
    }
  
    
  }
  /**
   * 取消保存后设置界面
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * <p>
   * 
   * @author wangyf
   * @time 2007-3-13 上午10:57:01
   */
  public void setPanelAfterCancelSave(SaleinvoiceVO voCur) {
//    if (isNewBill()) {
      if(null == voCur){
      // -------------------1
      addNew();
    }
    else {
      // -------------------1
      //resumeValue();
      loadCardData(voCur);
    }
    setEnabled(false);

    // 设置辅数量和辅单位的不可编辑
    for (int i = 0; i < getRowCount(); i++) {
      setCellEditable(i, "cpackunitname", false);
      setCellEditable(i, "npacknumber", false);
    }
    // 恢复部门和人员参照过滤
    setHeadDeptRef();

    // 放弃后置开关,并将备份赋给当前冲减情况
    processARBufAfterCancel();

//    setOutSumMakeInvoice(false);
  }

  /**
   * 方法功能描述：简要描述本方法的功能。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2009-8-17 上午10:13:35
   */
  public nc.vo.pub.AggregatedValueObject getVOOnlySelectedRow() {
	  
    SaleinvoiceVO voRet = (SaleinvoiceVO) getBillValueVO(SaleinvoiceVO.class.getName(),
        SaleVO.class.getName(), SaleinvoiceBVO.class.getName());
    // 单据类型
    voRet.getHeadVO().setCreceipttype(SaleBillType.SaleInvoice);

    // 取选定行的VO
    SaleinvoiceBVO[] itemVOs = voRet.getBodyVO();
    int indexSelected = -1;
    indexSelected = getBillTable().getSelectedRow();
    if (indexSelected > -1 && indexSelected < itemVOs.length) {
      SaleinvoiceBVO[] itemsNew = new SaleinvoiceBVO[1];
      itemsNew[0] = itemVOs[indexSelected];
      voRet.setChildrenVO(itemsNew);
    }
    else {
      voRet.setChildrenVO(null);
    }

    // 审批人
    voRet.getHeadVO().setCapproveid(SaleInvoiceTools.getLoginUserId());

    return voRet;
  }
  /**
   * 方法功能描述：卡片界面下发票合并开票。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-10-10 上午09:01:17
   */
  public boolean unite() {

    // 冲应收单提供接口
    ArSubInterface arSubUI = new ArSubInterface(this);
    ARSubUniteVO[] retVOs = arSubUI.getSelectedVOs();

    Hashtable<String, UFDouble> hsQueryArsubDataBykey = arSubUI
        .getQueryArsubData();

    if (retVOs == null || retVOs.length == 0)
      return false;
    isUnitByRecptcorp = retVOs[0].getBunitbyrecptcorp().booleanValue();
    
    //对冲应收单本身校验
    for (int n = 0; n < retVOs.length; n++) {
      String arsubacctkey = retVOs[n].getPrimaryKey();
      UFDouble dValue = PuPubVO.getUFDouble_NullAsZero(getHsTotalBykey().get(
          arsubacctkey));
      dValue.add(retVOs[n].getNsubmny());
      getHsTotalBykey().put(arsubacctkey, dValue);
    }
    if (!ArSubInterface.isStrikeOverflow(this, getHsTotalBykey(),
        hsQueryArsubDataBykey)) {
      setHsTotalBykey((Hashtable) getOldhsTotalBykey().clone());
      return false;
    }
    //2010-11-23 冯加滨 需要设置原始冲减关系
    UFDouble strikemny = SmartVODataUtils.getUFDouble(getHeadItem("nstrikemny").getValueObject());
    
    String invoiceid = (String) getHeadItem("csaleid").getValueObject();
    if((null != invoiceid && invoiceid.length() > 0 )
    		&& (null != strikemny && strikemny.compareTo(UFDouble.ZERO_DBL) !=0)
    		&& getHsSelectedARSubHVO().size() == 0 
    		){
    	try{
    		Hashtable t = SaleinvoiceBO_Client.queryStrikeData(invoiceid);
    		if(t != null && t.size() > 0) {
    			setHsSelectedARSubHVO(t);
    		}
    	}catch(Exception e){
    		SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000623")+e);
    		return false;
    	}
    }
    // 如果按照开票客户合并开票，处理逻辑和原有逻辑相同
    if (isUnitByRecptcorp) {
      Hashtable<String, UFDouble> hsArsub = new Hashtable<String, UFDouble>();
      for (int i = 0; i < retVOs.length; i++) {
        String key = retVOs[i].getCproducelineid() == null ? (new Integer(i))
            .toString() : retVOs[i].getCproducelineid();
        if (hsArsub.containsKey(key))
          hsArsub.put(key, ((UFDouble) hsArsub.get(key)).add(retVOs[i]
              .getNsubmny()));
        else
          hsArsub.put(key, retVOs[i].getNsubmny());   
      }

 
      SaleinvoiceVO saleinvoice = (SaleinvoiceVO) getBillValueVO(
          "nc.vo.so.so002.SaleinvoiceVO", "nc.vo.so.so002.SaleVO",
          "nc.vo.so.so002.SaleinvoiceBVO");

      if (isStrikeBalance(saleinvoice, hsArsub)) {
        // 设置界面值
        resetValueAfterUnite(saleinvoice, hsArsub);

        for (int j = 0; j < retVOs.length; j++) {
          String key = retVOs[j].getPrimaryKey();
          if (getHsSelectedARSubHVO().containsKey(key))
            getHsSelectedARSubHVO().put(
                key,
                ((UFDouble) getHsSelectedARSubHVO().get(key)).add(retVOs[j]
                    .getNsubmny()));
          else
            getHsSelectedARSubHVO().put(key, retVOs[j].getNsubmny());
        }
        setOldhsTotalBykey((Hashtable) getHsTotalBykey().clone());
      }
      else {
        setHsTotalBykey((Hashtable) getOldhsTotalBykey().clone());
      }

      return true;
      // 发票表体订单客户和冲应收单客户匹配进行冲减应收单合并开票
    }
    else {
      // 1)数据准备阶段 把查询得到的冲应收单VO 、销售发票表体VO进行分单
      // 首先依据参数设置分单条件，按照订单客户和产品线进行分单
      String[] splitkey=null;
      if (st.SO_27.booleanValue()) {
        splitkey = new String[] {
            "ccustomerid", "cproducelineid"
        };
      }
      else {
        splitkey = new String[] {
          "ccustomerid"
        };
      }
      // 按照分单条件对获得的冲应收单VO进行分组
      ARSubUniteVO[][] splitretVOs = (ARSubUniteVO[][]) nc.vo.scm.pub.vosplit.SplitBillVOs
          .getSplitVOs(retVOs, splitkey);

      HashMap<String, ARSubUniteVO[]> hsCustAcct = new HashMap<String, ARSubUniteVO[]>();
      for (int i = 0, loop = splitretVOs.length; i < loop; i++) {
        String key=null;
        String custkey = splitretVOs[i][0].getCcustomerid();
        if (st.SO_27.booleanValue()) {
          String plid = splitretVOs[i][0].getCproducelineid();
          key = custkey + plid;
        }
        else
          key = custkey;
        hsCustAcct.put(key, splitretVOs[i]);
      }


      //获得界面上的发票VO
      SaleinvoiceVO saleinvoice = (SaleinvoiceVO) getBillValueVO(
          "nc.vo.so.so002.SaleinvoiceVO", "nc.vo.so.so002.SaleVO",
          "nc.vo.so.so002.SaleinvoiceBVO");
      // 对发票表体行进行过滤，去掉不符合冲减条件的附表行记录
     //  记录下每个行号对应的表体行，便于在冲减应收时方便找到所在位置
      ArrayList<SaleinvoiceBVO> aryitem = new ArrayList<SaleinvoiceBVO>();
      for (int i = 0, loop = saleinvoice.getBodyVO().length; i < loop; i++) {
        SaleinvoiceBVO item = saleinvoice.getBodyVO()[i];
        if (item.getBlargessflag().booleanValue()
            || st.isLaborOrDiscount(item)
            || (item.getCprolineid() == null && st.SO_27.booleanValue())
            || item.getNoriginalcurmny().doubleValue() < 0)
          continue;
        aryitem.add(item);
        hsRowIndex.put(item.getCrowno(), i);
      }
      SaleinvoiceBVO[] itemsvo = new SaleinvoiceBVO[aryitem.size()];
      aryitem.toArray(itemsvo);
      // 设置发票分单条件
      if (st.SO_27.booleanValue()) {
        splitkey = new String[] {
            "ccustomerid", "cprolineid"
        };
      }
      else {
        splitkey = new String[] {
          "ccustomerid"
        };
      }
      // 依据发票分单条件，对过滤后的发票表体数据进行分组
      SaleinvoiceBVO[][] splitsaleinvoicebvo = (SaleinvoiceBVO[][]) nc.vo.scm.pub.vosplit.SplitBillVOs
          .getSplitVOs(itemsvo, splitkey);
      HashMap<String, SaleinvoiceBVO[]> hsinvoiceitem = new HashMap<String, SaleinvoiceBVO[]>();
      for (int i = 0, loop = splitsaleinvoicebvo.length; i < loop; i++) {
        String custkey = splitsaleinvoicebvo[i][0].getCcustomerid();
        String key =null;
        if (st.SO_27.booleanValue()) {
          String plid = splitsaleinvoicebvo[i][0].getCprolineid();
          key = custkey + plid;
        }
        else
          key = custkey;

        hsinvoiceitem.put(key, splitsaleinvoicebvo[i]);
      }

      // 2）校验合法性
      if (isStrikeBalanceByCust(hsinvoiceitem, hsCustAcct)) {
        // 设置界面值
        resetValueAfterUniteByCust(hsinvoiceitem, hsUnitByCust);
        
        for (int j = 0; j < retVOs.length; j++) {
          String key = retVOs[j].getPrimaryKey();
          if (getHsSelectedARSubHVO().containsKey(key))
            getHsSelectedARSubHVO().put(
                key,
                ((UFDouble) getHsSelectedARSubHVO().get(key)).add(retVOs[j]
                    .getNsubmny()));
          else
            getHsSelectedARSubHVO().put(key, retVOs[j].getNsubmny());
        }
        setOldhsTotalBykey((Hashtable) getHsTotalBykey().clone());
      }
      else {
        setHsTotalBykey((Hashtable) getOldhsTotalBykey().clone());
      }
    
      return true;
    }
    
  }

  /**方法功能描述：按照发票表体订单客户和冲应收单客户匹配进行冲减应收合并开票时设置发票数值。
   * <b>参数说明</b>
   * @param hsinvoiceitem
   * @param hsCustAcct
   * @author fengjb
   * @time 2008-8-28 下午08:27:43
   */
  private void resetValueAfterUniteByCust(
      HashMap<String, SaleinvoiceBVO[]> hsinvoiceitem,
      HashMap<String, UFDouble[]> hsUnitByCust) {

    int carddigit = getBodyItem("noriginalcursummny").getDecimalDigits();
    java.util.Iterator eKey = hsUnitByCust.keySet().iterator();
    
    UFDouble totalstrikemny = new UFDouble(0);
    
      while(eKey.hasNext()){
          String key = (String)eKey.next();
          UFDouble[] mny = (UFDouble[]) hsUnitByCust.get(key);
          //发票可冲减金额
          UFDouble invoicesubnmny = mny[0];
          
          //冲应收单可对冲金额
          UFDouble acctsubnmny = mny[1];
          //计算总共冲减总额
          totalstrikemny = totalstrikemny.add(acctsubnmny);
          //用于挤尾差
          UFDouble remainMoney = acctsubnmny;
          
        SaleinvoiceBVO[] saleinvoiceitem = hsinvoiceitem.get(key);
        for (int i = 0; i < saleinvoiceitem.length-1; i++) {
          SaleinvoiceBVO saleinvoicebody = saleinvoiceitem[i];
         
            UFDouble money = saleinvoicebody.getNoriginalcursummny();
            UFDouble changemoney = money.multiply(acctsubnmny).div(invoicesubnmny);
            changemoney = changemoney.setScale(0 - carddigit,
                  UFDouble.ROUND_HALF_UP);
             money = money.sub(changemoney);
             int row = hsRowIndex.get(saleinvoicebody.getCrowno());
             
              setBodyValueAt(
                  (getBodyValueAt(row, "nuniteinvoicemny") == null ? new UFDouble(
                      0.0)
                      : (UFDouble) getBodyValueAt(row, "nuniteinvoicemny"))
                      .add(changemoney), row, "nuniteinvoicemny");
              setBodyValueAt(money, row, "noriginalcursummny");
              if (isNewBill())
                getBillModel().setRowState(row, BillModel.ADD);
              else
                getBillModel().setRowState(row, BillModel.MODIFICATION);

              remainMoney = remainMoney.sub(changemoney);
              
              calculateNumber(row, "noriginalcursummny");
            }
              //挤尾差
             SaleinvoiceBVO saleinvoicebody = saleinvoiceitem[saleinvoiceitem.length-1];
             int row = hsRowIndex.get(saleinvoicebody.getCrowno());
             UFDouble money = saleinvoicebody.getNoriginalcursummny();
             remainMoney = remainMoney.setScale(0 - carddigit,
                  UFDouble.ROUND_HALF_UP);
              money = money.sub(remainMoney);
              setBodyValueAt(
                  (getBodyValueAt(row, "nuniteinvoicemny") == null ? new UFDouble(
                      0.0)
                      : (UFDouble) getBodyValueAt(row, "nuniteinvoicemny"))
                      .add(remainMoney), row, "nuniteinvoicemny");
              setBodyValueAt(money, row, "noriginalcursummny");
              calculateNumber(row, "noriginalcursummny");
              if (isNewBill())
                getBillModel().setRowState(row, BillModel.ADD);
              else
                getBillModel().setRowState(row, BillModel.MODIFICATION);
      }
      setHeadItem("nstrikemny", totalstrikemny.add(new UFDouble(getHeadItem(
          "nstrikemny").getValue())));
      execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    for (int i = 0; i < getBillModel().getRowCount(); i++) {
      getBillModel().setCellEditable(i, "nuniteinvoicemny", true);
    }

  }
  /**方法功能描述：按照发票表体订单客户和冲应收单客户匹配进行冲减应收单合并开票时校验订单客户对应的冲减应收金额是否有不合法的。
   * <b>参数说明</b>
   * @param hsinvoiceitem
   * @param hsCustAcct
   * @return
   * @author fengjb
   * @time 2008-8-28 下午05:57:46
   */


  private boolean isStrikeBalanceByCust(
      HashMap<String, SaleinvoiceBVO[]> hsinvoiceitem,
      HashMap<String, ARSubUniteVO[]> hsCustAcct) {

       hsUnitByCust = new HashMap<String, UFDouble[]>();
      //按照选择的冲应收单上的客户去循环处理，分别计算冲应收单本次冲减数量和发票上的可冲减金额
      java.util.Iterator iterator = hsCustAcct.keySet().iterator();
      while (iterator.hasNext()) {

        String key = (String) iterator.next();
        
        // 当前订单客户和产品线对应的所有的销售发票行可冲减金额开始设置
        SaleinvoiceBVO[] invoiceitems = hsinvoiceitem.get(key);
        UFDouble subsummnyBypl = new UFDouble(0);
        UFDouble summnyBypl = new UFDouble(0);
        for (int i = 0; i < invoiceitems.length; i++) {
          SaleinvoiceBVO saleinvoicebody = invoiceitems[i];
          subsummnyBypl = subsummnyBypl.add(saleinvoicebody.getNsubsummny());
          summnyBypl = summnyBypl.add(saleinvoicebody.getNoriginalcursummny());
        }
        // 订单客户的发票行按照产品线汇总可冲减金额已经设置完成 
        UFDouble invoicesubmoney = subsummnyBypl .multiply(st.SO_22).div(new UFDouble(100)).sub(
            subsummnyBypl).add(summnyBypl);
       

        // 对应的冲应收单按照产品线分类后的可冲减金额汇总
        ARSubUniteVO[] retVOs = hsCustAcct.get(key);
        UFDouble nsubmny = new UFDouble(0);
        if(retVOs !=null){
         for (int i = 0; i < retVOs.length; i++) {
          nsubmny= nsubmny.add(retVOs[i].getNsubmny());
         }
        }
        // Judge 判断对于每个订单客户的不同产品线，冲应收单的冲减金额和发票可冲减金额大小比较
        

          if ((nsubmny).compareTo(invoicesubmoney) > 0) {
            MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes
                .getInstance().getStrByID("40060501", "UPP40060501-000091")/*
                                                                             * @res
                                                                             * "订单行冲应收单可冲减金额大于发票可冲减金额，不能进行合并开票！"
                                                                             */);
            return false;
          }
          //若每个订单客户的产品线的冲应收单冲减金额>0的话，添加到缓存中
          if((nsubmny).compareTo(new UFDouble(0)) >0)
            hsUnitByCust.put(key, new UFDouble[] {
                summnyBypl, nsubmny});
        }
      return true;
  }

  /**
   * 此处插入方法说明。 创建日期：(2001-5-17 13:03:33)
   * 
   * @param e
   *          java.awt.event.ActionEvent
   */
  public void actionPerformed(java.awt.event.ActionEvent e) {
    onMenuItemClick(e);
  }

  /**
   * 方法功能描述：整单折扣率编辑后事件处理,将表头的整单折扣带入表体行的整单折扣。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-10-20 下午06:19:28
   */
  private void afterDiscountrateEdit(BillEditEvent e) {
    //关闭小计合计开关
	boolean bisCalculate = getBillModel().isNeedCalculate();
	getBillModel().setNeedCalculate(false);
	
    Object oDiscountrate = null;
    if (e == null){
      oDiscountrate = getHeadItem("ndiscountrate").getValueObject();
    }else{
      oDiscountrate = e.getValue();
    }
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
      setBodyValueAt(oDiscountrate, i, "ndiscountrate");
      calculateNumber(i, "ndiscountrate");
      setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
      execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
      setBodyValueAt(getBodyValueAt(i, "noriginalcursummny"), i, "nsubsummny");
      setBodyValueAt(getBodyValueAt(i, "nsummny"), i, "nsubcursummny");
      setBodyRowState(i);
    }
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**
   * 
   * 父类方法重写
   * 卡片界面编辑后事件
   * @see nc.ui.pub.bill.BillEditListener#afterEdit(nc.ui.pub.bill.BillEditEvent)
   */
  public void afterEdit(BillEditEvent e) {
	  
    //表头项编辑后事件
    if (e.getPos() == BillItem.HEAD) {
      // 部门
      if (e.getKey().equals("cdeptid")) {
        afterDeptEdit(e);
      }
      // 业务员
      else if (e.getKey().equals("cemployeeid")) {
         afterEmployeeEdit(e);
      }
      // 客户
      else if (e.getKey().equals("creceiptcorpid")) {
        afterCustomerEdit(e);
      }
      // 客商开户行
//      else if (e.getKey().equals("ccustbankid")) {
//        UIRefPane ref = (UIRefPane) getHeadItem("ccustbankid").getComponent();
//        if (null != ref) {
//          // 银行ID
//          String id = ref.getRefPK();
//          setHeadItem("ccustbankid", id);
//          // 银行帐号
//          String code = ref.getRefCode();
//          setHeadItem("ccustomerbankNo", code);
//        }
//      }
      // 币种
      else if (e.getKey().equals("ccurrencyid")) {
        afterCurrencyEdit(e);
      }
      // 整单折扣率
      else if (e.getKey().equals("ndiscountrate")) {
        afterDiscountrateEdit(e);
      }
      // 折本汇率
      else if (e.getKey().equals("nexchangeotobrate")) {
        afterChangeotobrateEdit(e);
      }
      // 发票折扣
      else if(e.getKey().equals("ninvoicediscountrate")){
    	  afterNinvoicediscountrateEdit(e);
      }
       // 自定义项修改
      else if (e.getKey().indexOf("vdef") >= 0) {
        for (int defindex = 1; defindex <= 20; defindex++) {
          if (("vdef" + defindex).equals(e.getKey())) {
            DefSetTool.afterEditHead(getBillData(), "vdef" + defindex,
                "pk_defdoc" + defindex);
            break;
          }
        }
      }
    }
    //表体项编辑后事件
    if (e.getPos() == BillItem.BODY) {
      // 存货编码 清除数据
      if (e.getKey().equals("cinventorycode")) {
        afterInventoryEdit(e);
      }
      // 单位
      else if (e.getKey().equals("cpackunitname")) {
        afterUnitEdit(e);
        return ;
      }
      // 数量变化
      afterNumberEdit(e);
      // 自由项
      if (e.getKey().equals("vfree0")) {
        afterFreeItemEdit(e);
      }
      // 仓库
      else if (e.getKey().equals("cbodywarehousename")) {
        afterWarehouseEdit(e);
      }
      // 行号
      else if (e.getKey().equals("crowno")) {
        // 验证行号
        BillRowNo.afterEditWhenRowNo(this, e, SaleBillType.SaleInvoice);
      }
      // 自定义项修改
      if (e.getKey().indexOf("vdef") >= 0) {
        for (int defindex = 1; defindex <= 20; defindex++) {
          if (("vdef" + defindex).equals(e.getKey())) {
            DefSetTool.afterEditBody(getBillModel(), e.getRow(), "vdef"
                + defindex, "pk_defdoc" + defindex);
            break;
          }
        }
      }
    }
    // 二次开发扩展
    uipanel.getPluginProxy().afterEdit(e);
    // 可能会影响按钮的状态：如合并开票
//    getContainer().setButtonsState();
  }
  /**
   * 方法功能描述：业务员编辑后事件，根据业务员参照置部门参照。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-8-18 下午06:31:45
   */
  private void afterEmployeeEdit(BillEditEvent e) {

	    try {
	      UIRefPane cemployeeid = (UIRefPane) getHeadItem("cemployeeid").getComponent();
	      UIRefPane cdeptid = (UIRefPane) getHeadItem("cdeptid").getComponent();
	      
	      if (null != cemployeeid  && null != cdeptid && StringUtil.isEmptyWithTrim(cdeptid.getRefPK())) {
	        if (null!= cemployeeid.getRefPK()) {
	          Object[] odeptid = (Object[]) CacheTool.getColumnValue(
	              "bd_psndoc", "pk_psndoc", "pk_deptdoc",
	              new String[] { cemployeeid.getRefPK().toString() });
	         cdeptid.setPK(odeptid[0]);
	        }
	      }
	    } catch (Exception e1) {
	      SCMEnv.out(e1.getMessage());
	    }  
}
/**
   * 方法功能描述：表头发票折扣编辑后时间。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2008-8-11 下午02:48:52
   */
  private void afterNinvoicediscountrateEdit(BillEditEvent e) {
		// 关闭小计合计开关
		boolean bisCalculate = getBillModel().isNeedCalculate();
		getBillModel().setNeedCalculate(false);

		for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
			setBodyValueAt(e.getValue(), i, "ninvoicediscountrate");
			calculateNumber(i, "ninvoicediscountrate");
			setBodyValueAt(getBodyValueAt(i, "noriginalcursummny"), i,
					"nsubsummny");
			setBodyValueAt(getBodyValueAt(i, "nsummny"), i, "nsubcursummny");
			setBodyRowState(i);
		}
		setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
		execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
		
		getBillModel().setNeedCalculate(bisCalculate);
		getBillModel().reCalcurateAll();
}

/**
 * 根据币种设置小数位数。 创建日期：(2002-6-24 11:15:06)
 */
  private void setPanelByCurrency(String ccurrencytypeid) {
    try {
      //折本汇率
      String aryRate = "nexchangeotobrate";
      //表体原币金额字段
      String[] orgfieldnames = new String[]{
          "noriginalcurtaxmny", "noriginalcurmny", "noriginalcursummny",
          "noriginalcurdiscountmny", "nsubsummny", "nuniteinvoicemny"
    };
      //表体本币金额字段
      String[] benfieldnames = new String[] {
          "ntaxmny", "nmny", "nsummny", "ndiscountmny", "nsubcursummny"
          };
      //表头原币金额字段
      String[] headfieldnames = new String[]{
          "nstrikemny", "nnetmny", "ntotalsummny"
      };
      SOBillCardTools.setCardPanelByCurrency(this,
          ccurrencytypeid,SaleInvoiceTools.getLoginPk_Corp(), UFBoolean.FALSE, aryRate,
          orgfieldnames, benfieldnames, headfieldnames, null);
  
      /**设置表头这本汇率、折辅汇率可编辑性*/
      //原币=本币
      if(socur.isLocalCurrType(ccurrencytypeid)){
         getHeadItem("nexchangeotobrate").setEdit(false);
      }else{
        //原币等于其他币种
    	  getHeadItem("nexchangeotobrate").setEdit(true);

      }
    }
    catch (Exception e) {
      nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000638"));
      nc.vo.scm.pub.SCMEnv.out(e.getMessage());
    }
  }

  /**
   * 方法功能描述：初始化业务员参照约束条件。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-18 下午06:24:15
   */
  private void initSalePeopleRef() {

    UIRefPane Refpsn = (UIRefPane) getHeadItem("cemployeeid").getComponent();
    if (null != Refpsn){
      Refpsn
          .setWhereString(" (bd_deptdoc.deptattr = '3' or bd_deptdoc.deptattr= '4') and bd_psndoc.pk_corp='"
              + SaleInvoiceTools.getLoginPk_Corp() + "'");
    }

  }

  /**
   * 方法功能描述：计量单位编辑后事件。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-10-20 下午06:28:48
   */
  private void afterUnitEdit(BillEditEvent e) {
    //关闭小计合计开关
   boolean bisCalculate = getBillModel().isNeedCalculate();
	getBillModel().setNeedCalculate(false);
    String cunitid = (String) getBodyValueAt(e.getRow(), "cunitid");
    String cpackunitid = (String) getBodyValueAt(e.getRow(), "cpackunitid");
    if (e.getKey().equals("cpackunitname")
        && (cpackunitid == null || cpackunitid.length() == 0)) {
      // 辅单位为空
      setBodyValueAt(null, e.getRow(), "npacknumber");
      return;
    }

    // 包装单位
    if (cunitid.equals(cpackunitid)) {
      String[] formulas = new String[2];
      // 包装单位名称
      formulas[0] = "cpackunitname->getColValue(bd_measdoc,measname,pk_measdoc,cpackunitid)";
      // 报价单位名称
      formulas[1] = "cquoteunitname->getColValue(bd_measdoc,measname,pk_measdoc,cquoteunitid)";
      // 换算率
      execBodyFormulas(e.getRow(), formulas);
      setBodyValueAt(new UFDouble(1), e.getRow(), "scalefactor");
      setBodyValueAt(UFBoolean.TRUE, e.getRow(), "fixedflag");
      setBodyValueAt(new UFDouble(1), e.getRow(), "nqtscalefactor");
      setBodyValueAt(UFBoolean.TRUE, e.getRow(), "bqtfixedflag");
    }
    else {
      String[] formulas = new String[3];
      // 包装单位名称
      formulas[0] = "cpackunitname->getColValue(bd_measdoc,measname,pk_measdoc,cpackunitid)";
      // 换算率
      formulas[1] = "scalefactor->getColValue2(bd_convert,mainmeasrate,pk_invbasdoc,cinvbasdocid,pk_measdoc,cpackunitid)";
      // 是否固定换算率
      formulas[2] = "fixedflag->getColValue2(bd_convert,fixedflag,pk_invbasdoc,cinvbasdocid,pk_measdoc,cpackunitid)";
 
      execBodyFormulas(e.getRow(), formulas);
    }

    calculateNumber(e.getRow(), "nnumber");
    setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
    execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    setBodyValueAt(getBodyValueAt(e.getRow(), "noriginalcursummny"),
        e.getRow(), "nsubsummny");
    setBodyValueAt(getBodyValueAt(e.getRow(), "nsummny"), e.getRow(),
        "nsubcursummny");
    
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**
   * 
   * SaleInvoiceCardPanel 的构造子
   * 销售发票卡片界面初始化
   * @param container
   */
  public SaleInvoiceCardPanel(IInvoiceCardPanel container) {
	// 缓存销售发票界面UI
    uipanel = (SaleInvoiceUI)container;
	// 初始化销售发票工具类
    st = uipanel.getSaleinvoiceTools();
    
    setContainer(container);

    setName("BillCardPanel");
    setBillType(SaleBillType.SaleInvoice);
    setCorp(SaleInvoiceTools.getLoginPk_Corp());
    setOperator(SaleInvoiceTools.getLoginUserId());
    // 添加监听
    addEditListener(this);
    addBodyEditListener2(this);
    addBodyMenuListener(this);
    addBodyTotalListener(this);
    addBodyTotalListener(this);
    setTatolRowShow(true);

   
    // 设置表体菜单显示
    setBodyMenuShow(true);
    //加载卡片界面
    loadThisTemplet();
    //初始化币种
    initCurrency();
    //缓存模板初始时编辑性
    getInitBillItemEidtState();
   
	// 表体拖拽属性初始化
	initBodyFillStatus();
    // 设置自动执行表头表体公式标示
	setAutoExecHeadEditFormula(true);
    //设置选中背景色
    getBillTable().setRowSelectionAllowed(true);
    getBillTable().setColumnSelectionAllowed(false);
    getBillTable().setSelectionMode(
      javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	
  }

  /**方法功能描述：初始化币种相关信息。
   * <b>参数说明</b>
   * @time 2008-12-8 下午07:51:35
   */
  private void initCurrency() {
    if(null == socur)
      socur = new SOCurrencyRateUtil(SaleInvoiceTools.getLoginPk_Corp());
    try{
    if(null == currateutil)
      currateutil = new BusinessCurrencyRateUtil(SaleInvoiceTools.getLoginPk_Corp());
    }catch(Exception e){
      SCMEnv.out(e);
    }
  }
  /**
   * 仿照订单同名实现
   * 
   */
  private void initBodyFillStatus() {
		HashSet<String> hs_key = new HashSet<String>();

		// 币种、汇率、发票折扣、表体自定义项1-20

		String[] skeys = new String[] {
				//
				"ccurrencytypename", "nexchangeotobrate", "ninvoicediscountrate",
				//	
				"vdef1", "vdef2", "vdef3", "vdef4", "vdef5", "vdef6", "vdef7", "vdef8", "vdef9", "vdef10",
				//
				"vdef11", "vdef12", "vdef13", "vdef14", "vdef15", "vdef16", "vdef17", "vdef18", "vdef19", "vdef20", 
				// 含税单价、无税单价、含税净价、无税净价
				"noriginalcurtaxprice","noriginalcurprice","noriginalcurtaxnetprice","noriginalcurnetprice"
				};
		hs_key.addAll(Arrays.asList(skeys));

		BillItem[] bis = getBillData().getBodyItems();
		for (BillItem bi : bis) {
			if (hs_key.contains(bi.getKey()) && bi.isEdit()) {
				bi.setFillEnabled(true);
			} else {
				bi.setFillEnabled(false);
			}
		}

	}
  
  /**
   * 新增单据时设置表头默认数据。 创建日期：(2001-8-27 10:05:59)
   */
  private void setDefaultData() {
    /**
     * 出库汇总开票时表头项编辑性设置 begin
     */ 
	// 开票客户
    getHeadItem("creceiptcorpid").setEdit(true);
    getHeadItem("creceiptcorpid").setEnabled(true);
    // 销售组织
    getHeadItem("csalecorpid").setEdit(true);
    // 库存组织
    getHeadItem("ccalbodyid").setEdit(true);
    // 部门
    getHeadItem("cdeptid").setEdit(true);
    // 业务员
    getHeadItem("cemployeeid").setEdit(true);
    // 币种
    getHeadItem("ccurrencyid").setEdit(true);
    // 散户--V5调整为可修改
    // getHeadItem("cfreecustid").setEdit(true);
    // 客户打印名称
    getHeadItem("vprintcustname").setEdit(true);
    /**
     * 出库汇总开票时表头项编辑性设置 end
     */ 
    
    /**
     * 出库汇总开票时表头项默认值设置 begin
     */ 
    // 业务类型
    setHeadItem("cbiztype", getBusiType());
    // 单据号
    setHeadItem("vreceiptcode", null);
    //单据类型
    setHeadItem("creceipttype", SaleBillType.SaleInvoice);
    // 单据日期
    setHeadItem("dbilldate", SaleInvoiceTools.getLoginDate());
    // 公司
    setHeadItem("pk_corp", SaleInvoiceTools.getLoginPk_Corp());
    // 单据状态
    setHeadItem("fstatus", BillStatus.FREE);
    //  对冲标记
    setHeadItem("fcounteractflag", SaleVO.FCOUNTERACTFLAG_NORMAL);
    // 整单折扣
    setHeadItem("ndiscountrate", new UFDouble(100));
    //发票折扣
    setHeadItem("ninvoicediscountrate",new UFDouble(100));
    // 预估运费
    setHeadItem("nevaluatecarriage",new UFDouble(0.00));
    // 单据号
    ((UIRefPane) getHeadItem("vreceiptcode").getComponent()).getUITextField()
          .setDelStr("+");
    /**
     * 出库汇总开票时表头项默认值设置 end
     */ 
      
    // 表体项
    // 仓库
    getBodyItem("cbodywarehousename").setEdit(false);
    
    // 表尾项
    // 制单日期
    setTailItem("dmakedate", SaleInvoiceTools.getLoginDate());
    // 制单人
    setTailItem("coperatorid", SaleInvoiceTools.getLoginUserId());
    // 审核日期
    setTailItem("capproveid", null);
    // 审核人
    setTailItem("dapprovedate", null);
  }
  /**
   * 方法功能描述：送审、审批、弃审等操作后
   * 根据数据库最新记录刷新卡片界面显示。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2008-11-19 下午01:35:33
   */
  public void updateUIValue(HashMap<String, SmartVO> hsnewvo){
    //依据表头主键获得待更新VO
    String csaleid = (String)getHeadItem("csaleid").getValueObject();
    SaleVO salehvo = (SaleVO)hsnewvo.get(csaleid);
    if(salehvo == null)
      return;
    
    //刷新表头数据
    //状态
    setHeadItem("fstatus", salehvo.getFstatus());
   //时间戳
    setHeadItem("ts", salehvo.getTs());
   //最后修改时间
    setTailItem("dmoditime", salehvo.getDmoditime());
   //审核时间
    setTailItem("daudittime", salehvo.getDaudittime());
   //审核日期
    setTailItem("dapprovedate",salehvo.getDapprovedate());
   //审核人
    setTailItem("capproveid",salehvo.getCapproveid());
    //只有表头数据
    if(hsnewvo.size() == 1)
      return;
    // 更新表体数据
    for (int i = 0, loop = getRowCount(); i < loop; i++) {
      String cinvoicebid = (String) getBodyValueAt(i,"cinvoice_bid");
      if (hsnewvo.containsKey(cinvoicebid)) {
        SaleinvoiceBVO salebvo = (SaleinvoiceBVO) hsnewvo.get(cinvoicebid);
        setBodyValueAt(salebvo.getFrowstatus(), i, "frowstatus");
        setBodyValueAt(salebvo.getTs(), i, "ts");
      }
    }
    
    updateValue();
    
  }
  /**
   * 加载并显示销售发票已有数据 创建日期：(2001-4-23) 修改日期：2003-11-13 修改人：杨涛 修改内容：加载数据时，计算“源头单据号”
   */
//  public void loadDataPart(String sInvoiceId) {
//    try {
//      SaleinvoiceBVO[] saleinvoiceBs = SaleinvoiceBO_Client
//          .queryBodyDataByHID(sInvoiceId);
//      
//      Object oTemp[][] = null;
//      nc.vo.pub.lang.UFBoolean b = new nc.vo.pub.lang.UFBoolean(false);
//      for(int i = 0; i < saleinvoiceBs.length; i++){
//      	if(saleinvoiceBs[i].getCinvbasdocid() == null || saleinvoiceBs[i].getCinvbasdocid().trim().length() == 0) continue;
//      	if(saleinvoiceBs[i].getCpackunitid() == null || saleinvoiceBs[i].getCpackunitid().trim().length() == 0) continue;
//      	b = new nc.vo.pub.lang.UFBoolean(false);
//      	oTemp = CacheTool.getAnyValue2("bd_convert", "fixedflag", "pk_invbasdoc='" + saleinvoiceBs[i].getCinvbasdocid() + "' and pk_measdoc='" + saleinvoiceBs[i].getCpackunitid() + "'");
//      	if(oTemp != null && oTemp.length > 0 && oTemp[0][0] != null) b = new nc.vo.pub.lang.UFBoolean(oTemp[0][0].toString());
//      	if(b.booleanValue()){
//          	oTemp = CacheTool.getAnyValue2("bd_convert", "mainmeasrate", "pk_invbasdoc='" + saleinvoiceBs[i].getCinvbasdocid() + "' and pk_measdoc='" + saleinvoiceBs[i].getCpackunitid() + "'");
//      		if(oTemp != null && oTemp.length > 0 && oTemp[0][0] != null) saleinvoiceBs[i].setScalefactor(new UFDouble(oTemp[0][0].toString()));
//      	}else{
//      		saleinvoiceBs[i].setScalefactor(saleinvoiceBs[i].getNnumber().div(saleinvoiceBs[i].getNpacknumber()));
//      	}
//      }
//       //存货类字段
//      //add by fengjb V55新增存货类
//      String[] invclformul = new String[]{
//          "cinvclassid->getColValue(bd_invbasdoc,pk_invcl,pk_invbasdoc,cinvbasdocid)"
//      };
//      SoVoTools.execFormulas(invclformul, saleinvoiceBs);
//      getBillModel().setBodyDataVO(saleinvoiceBs);
//      long s1 = System.currentTimeMillis();
//      getBillModel().execLoadFormula();
//      nc.vo.scm.pub.SCMEnv.out("执行公式[共用时" + (System.currentTimeMillis() - s1)
//          + "]");
//      if (getRowCount() != 0) {
//        for (int i = 0; i < getRowCount(); i++) {
//          setAssistChange(i);
//          beforeUnitChange(i);
//          afterUnitChange(i);
//        }
//      }
//      countCardUniteMny();
//      st.initFreeItem(saleinvoiceBs, getBillModel());
//      updateValue();
//      getBillModel().reCalcurateAll();
//      nc.vo.scm.pub.SCMEnv.out("数据加载成功！");
//
//    }
//    catch (ValidationException e) {
//      MessageDialog.showErrorDlg(this, "", e.getMessage());
//    }
//    catch (Exception e) {
//      MessageDialog.showErrorDlg(this, "", nc.ui.ml.NCLangRes.getInstance()
//          .getStrByID("SCMCOMMON", "UPPSCMCommon-000256")/* @res "数据加载失败！" */);
//      nc.vo.scm.pub.SCMEnv.out(e.getMessage());
//    }
//  }

//  /**
//   * 重新加载TS
//   * <p>
//   * <b>examples:</b>
//   * <p>
//   * 使用示例
//   * <p>
//   * <b>参数说明</b>
//   * <p>
//   * 
//   * @author wangyf
//   * @time 2007-3-13 上午08:50:32
//   */
//  public void reLoadTS() {
//    try {
//      // 重新加载表头TS
//      String formula[] = {
//          "fstatus->getColValue(so_saleinvoice,fstatus,csaleid,csaleid)",
//          "ts->getColValue(so_saleinvoice,ts,csaleid,csaleid)",
//          "dmoditime->getColValue(so_saleinvoice,dmoditime,csaleid,csaleid)",
//          "daudittime->getColValue(so_saleinvoice,daudittime,csaleid,csaleid)",
//          "dbilltime->getColValue(so_saleinvoice,dbilltime,csaleid,csaleid)",
//          "dapprovedate->getColValue(so_saleinvoice,dapprovedate,csaleid,csaleid)",
//          "csalecompanyname->getColValue(bd_corp,unitname,pk_corp,pk_corp)",
//          "capproveid->getColValue(so_saleinvoice,capproveid,csaleid,csaleid)"
//      };
//
//      execHeadFormulas(formula);
//      String bodyFormula[] = {
//          "ts->getColValue(so_saleinvoice_b,ts,cinvoice_bid,cinvoice_bid)"
//      };
//      for(int i=0;i<getRowCount();i++){
//        execBodyFormulas(i,bodyFormula);  
//      }
//      
//    }
//    catch (Exception e) {
//      nc.vo.scm.pub.SCMEnv.out("重新加载表头TS失败.");
//      nc.vo.scm.pub.SCMEnv.out(e.getMessage());
//    }
//
//  }

  /**
   * 此处插入方法说明。 创建日期：(2001-11-16 13:23:37)
   */
  private void initSaleDeptRef() {

    UIRefPane cdeptid = (UIRefPane) getHeadItem("cdeptid").getComponent();
    if (cdeptid != null)
      cdeptid
          .setWhereString("(bd_deptdoc.deptattr = '3' or bd_deptdoc.deptattr= '4' ) and bd_deptdoc.pk_corp='"
              + SaleInvoiceTools.getLoginPk_Corp() + "'");

  }
 /**
  * 方法功能描述：增行功能实现。
  * <b>参数说明</b>
  * @author fengjb
  * @time 2008-12-1 上午10:19:37
  */
  public void actionAddLine() {
   if(checkAddLine()){
    getBillModel().addLine();
    setNewLineDefaultValue(getRowCount()-1);
    // 计算并设置新增行号
    BillRowNo.addLineRowNo(this, SaleBillType.SaleInvoice, "crowno");
	 }

  }
  /**
   * 方法功能描述：增行时设置默认值。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2008-12-1 上午10:19:53
   */
  public void setNewLineDefaultValue(int rowcount){

    setCellEditable(rowcount, "ntaxrate", true);
    //公司
    String pk_corp = (String)getHeadItem("pk_corp").getValueObject();
    setBodyValueAt(pk_corp, rowcount, "pk_corp");
    // 币种
    String currencynowid = getHeadItem("ccurrencyid").getValue();
    setBodyValueAt(currencynowid, rowcount, "ccurrencytypeid");
    String[] formula = {
      "ccurrencytypename->getColValue(bd_currtype,currtypename,pk_currtype,ccurrencytypeid)"
    };
    getBillModel().execFormula(rowcount, formula);
    // 汇率
    setBodyValueAt(getHeadItem("nexchangeotobrate").getValue(), rowcount,
        "nexchangeotobrate");

    // 整单折扣
    setBodyValueAt(getHeadItem("ndiscountrate").getValue(), rowcount,
        "ndiscountrate");
    setBodyValueAt(new UFDouble(100), rowcount, "nitemdiscountrate");
    //发票折扣
    setBodyValueAt(getHeadItem("ninvoicediscountrate").getValueObject() == null? new UFDouble(100):getHeadItem("ninvoicediscountrate").getValueObject()
        ,rowcount ,"ninvoicediscountrate");
    setCellEditable(rowcount, "blargessflag", true);
    setCellEditable(rowcount, "cinventorycode", true);
    
    UIRefPane refPane = (UIRefPane)getBodyItem("cinventorycode").getComponent();
    if (isOutSumMakeInvoice()) {
      refPane.setRefModel(new InvmandocDefaultRefModel(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000639")));
    }
    else {
      refPane.setRefModel(new DisInvRefModel());
    }
    
    // yt add 2004-12-19
    setBodyValueAt(st.SO_34, rowcount, "nnumber");
    setBodyValueAt(st.SO_34, rowcount, "nquotenumber");

    // 存货自由项数列
    alInvs.add(null);
  }
  /**
   * 方法功能描述：增行时校验单据限行SO67。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-8-11 下午03:11:46
   */
  public boolean checkAddLine() {
        //单据限行
		if (st.SO_67 != null) {
			// SO_01=new Integer(1);
			if (st.SO_67.intValue() != 0) {
				if (st.SO_67.intValue() < getRowCount() + 1) {
					 MessageDialog.showErrorDlg(this,"",nc.ui.ml.NCLangRes.getInstance()
							.getStrByID("40060301", "UPP40060301-000171", null,
									new String[] { st.SO_67.intValue() + "" }));
					// 单据限SO_67.intValue()行
					return false;
				}
			}
		}
		
	return true;
}

/**
   * 出库汇总检查
   * <p>
   * <b>examples:</b>
   * <p>
   * 使用示例
   * <p>
   * <b>参数说明</b>
   * 
   * @param saleinvoice
   * @throws nc.vo.pub.ValidationException
   *           <p>
   * @author wangyf
   * @time 2007-3-12 下午02:07:00
   */
  private void checkGather(SaleinvoiceVO saleinvoice)
      throws nc.vo.pub.ValidationException {
    // 单据体
    if (getRowCount() == 0)
      throw new nc.vo.pub.ValidationException(nc.ui.ml.NCLangRes.getInstance()
          .getStrByID("SCMCOMMON", "UPPSCMCommon-000073")/* @res "单据体不能为空!" */);
    //  单据限行
	if (null != st.SO_67 && 0 < st.SO_67.intValue()) {
			if (st.SO_67.intValue() < getRowCount()) {
				throw new ValidationException(NCLangRes.getInstance().getStrByID("40060301", "UPP40060301-000171",
						null, new String[] { st.SO_67.intValue() + "" })/* @res "发票限{0}行!" */);
			}
	}
    // 用户单据设置非空项检测
	dataNotNullValidate();
	//表头业务非空项检测
	saleinvoice.getHeadVO().validate();
	//表头折本汇率
	if(null == saleinvoice.getHeadVO().getNexchangeotobrate()
			|| saleinvoice.getHeadVO().getNexchangeotobrate().compareTo(new UFDouble(0)) == 0)
		  throw new ValidationException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
		          .getStrByID("40060501", "UPP40060501-100094")/* @res "发票折本汇率不能为零!" */);
    //表体业务非空项检测
    for (int i = 0,iloop = saleinvoice.getChildrenVO().length; i < iloop; i++) {
      SaleinvoiceBVO oldbodyVO = saleinvoice.getItemVOs()[i];
      //删除状态不用校验合法性
      if(VOStatus.DELETED == oldbodyVO.getStatus())
        continue;
      // 数量
      if (null == oldbodyVO.getNnumber()
          || oldbodyVO.getNnumber().doubleValue() == 0.0)
        throw new ValidationException(nc.ui.ml.NCLangRes.getInstance()
            .getStrByID("40060501", "UPP40060501-000134")/*
                                                           * @res "数量不能为零!"
                                                           */);
      if (null != oldbodyVO.getDiscountflag()
          && !oldbodyVO.getDiscountflag().booleanValue()) {
      // 包装单位(是否采用辅计量)
      if (null != oldbodyVO.getAssistunit()
          && oldbodyVO.getAssistunit().booleanValue()) {
          if (null == oldbodyVO.getCpackunitid()) {
            throw new ValidationException(nc.ui.ml.NCLangRes.getInstance()
                .getStrByID("40060501", "UPP40060501-000076")/*
                                                               * @res "辅单位不能为空!"
                                                               */);
          }
          if (null == oldbodyVO.getNpacknumber()) {
            throw new ValidationException(nc.ui.ml.NCLangRes.getInstance()
                .getStrByID("40060501", "UPP40060501-000077")/*
                                                               * @res "辅数量不能为空!"
                                                               */);
          }
        }
      }
    }
  }

  /**
   * 方法功能描述：自由项编辑后事件处理。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-10-20 下午06:41:50
   */
  private void afterFreeItemEdit(BillEditEvent e) {
    try {
      nc.vo.scm.ic.bill.FreeVO voFree = getFreeItemRefPane().getFreeVO();
      // 将自由项填入表体
      for (int i = 0; i < 5; i++) {
        String fieldname = "vfree" + i;
        Object o = voFree.getAttributeValue(fieldname);
        setBodyValueAt(o, e.getRow(), fieldname);
      }
    }
    catch (Exception e2) {
      SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000640")+e);
    }
  }

  /**
   * 方法功能描述：表头折本汇率编辑后事件。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-10-20 下午06:31:28
   */
  private void afterChangeotobrateEdit(BillEditEvent e) {
     //	关闭小计合计开关
	boolean bisCalculate = getBillModel().isNeedCalculate();
	getBillModel().setNeedCalculate(false);  
	
    for (int i = 0,iloop = getRowCount(); i < iloop; i++) {
      setBodyValueAt(getHeadItem("nexchangeotobrate").getValueObject(), i,
          "nexchangeotobrate");
      calculateNumber(i, "noriginalcursummny");
      setBodyValueAt(getBodyValueAt(i, "nquoteprice"), i, "nsubquoteprice");
      setBodyValueAt(getBodyValueAt(i, "nquotetaxprice"), i,
          "nsubquotetaxprice");
      setBodyValueAt(getBodyValueAt(i, "nquotenetprice"), i,
          "nsubquotenetprice");
      setBodyValueAt(getBodyValueAt(i, "nquotetaxnetprice"), i,
          "nsubquotetaxnetprice");
      // 修改后，冲减前本币含税单价应保持更新
      setBodyValueAt(getBodyValueAt(i, "ntaxnetprice"), i, "nsubtaxnetprice");
      setBodyRowState(i);
    }
    getBillModel().setNeedCalculate(bisCalculate);
    getBillModel().reCalcurateAll();
  }

  /**
   * 方法功能描述：客商编辑后事件处理。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-9-17 上午09:46:24
   */
  private void afterCustomerEdit(BillEditEvent e) {
	//开票客户
    String creceiptcorpid = (String) getHeadItem("creceiptcorpid").getValueObject();
    
    //根据开票客户设置客商开户银行业约束条件
    if (!StringUtil.isEmptyWithTrim(creceiptcorpid)) {
			BillItem item = getHeadItem("ccustbankid");
			UIRefPane bankref = (UIRefPane) item.getComponent();
		    bankref.getRefModel().setPk_corp(getCorp());
		    bankref.getRefModel().addWherePart(" and bd_bankaccbas.pk_bankaccbas in (select  k.pk_accbank from bd_custbank k,bd_cumandoc m  where  m.pk_corp='" +
						getCorp()+"' and  k.pk_cubasdoc=m.pk_cubasdoc and m.pk_cumandoc='"+creceiptcorpid+ "' UNION select  s.pk_bankaccbas from bd_bankaccbas s, bd_bankaccmng m where  m.pk_corp in" 
			            +"( select  DISTINCT pk_corp1  from  bd_cubasdoc b, bd_cumandoc m where  isnull ( b.pk_corp1, '0' ) != '0' and m.pk_corp  = '"
			            + getCorp() +"' and m.pk_cubasdoc  = b.pk_cubasdoc  and m.pk_cumandoc = '"
			            + creceiptcorpid +"')  and m.istrade  = 'Y'  and isnull ( s.mainaccount, '0' ) = '0'  and s.pk_bankaccbas  = m.pk_bankaccbas )");
				    
	        execHeadTailLoadFormulas(item);
			
		}

    String strtemp = null;
    Vector<String> vecTemp = new Vector<String>();

    // 部门
    strtemp =  (String)getHeadItem("cdeptid").getValueObject();
    if (StringUtil.isEmptyWithTrim(strtemp)){
      vecTemp
          .add(new String(
              "cdeptid->getColValue(bd_cumandoc,pk_respdept1,pk_cumandoc,creceiptcorpid)"));
    }
    // 业务员
    strtemp = (String)getHeadItem("cemployeeid").getValueObject();
    if(StringUtil.isEmptyWithTrim(strtemp)){
    vecTemp
        .add(new String(
            "cemployeeid->getColValue(bd_cumandoc,pk_resppsn1,pk_cumandoc,creceiptcorpid)"));
    }
    // 整单扣率
    if(null == getHeadItem("ndiscountrate").getValueObject()
            || getHeadItem("ndiscountrate").getValueObject().toString().length()  == 0){
    vecTemp
        .add(new String(
            "ndiscountrate->getColValue(bd_cumandoc,discountrate,pk_cumandoc,creceiptcorpid)"));
    }
    String oldccurrencyid = (String)getHeadItem("ccurrencyid").getValueObject();
    // 默认交易币种
    if (StringUtil.isEmptyWithTrim(oldccurrencyid)) {
      vecTemp
          .add(new String(
              "ccurrencyid->getColValue(bd_cumandoc,pk_currtype1,pk_cumandoc,creceiptcorpid)"));
    }
    // 库存组织
    strtemp = (String)getHeadItem("ccalbodyid").getValueObject();
    if (StringUtil.isEmptyWithTrim(oldccurrencyid)) {
      vecTemp
          .add(new String(
              "ccalbodyid->getColValue(bd_cumandoc,pk_calbody,pk_cumandoc,creceiptcorpid)"));
    }
    // 销售组织
    strtemp = (String)getHeadItem("csalecorpid").getValueObject();
    if (StringUtil.isEmptyWithTrim(oldccurrencyid)) {
      vecTemp
          .add(new String(
              "csalecorpid->getColValue(bd_cumandoc,pk_salestru,pk_cumandoc,creceiptcorpid)"));
    }
    if (vecTemp.size() > 0) {
      String[] formulas = new String[vecTemp.size()];
      vecTemp.copyInto(formulas);
      execHeadFormulas(formulas);
    }
    // 整单折扣
    Object ndiscountrate = getHeadItem("ndiscountrate").getValueObject();
    if (null == ndiscountrate || ndiscountrate.toString().trim().length() == 0) {
      setHeadItem("ndiscountrate", new UFDouble(100));
    }
    // 基础档案ID
    String formula = "getColValue(bd_cumandoc,pk_cubasdoc,pk_cumandoc,creceiptcorpid)";
    String pk_cubasdoc = (String) execHeadFormula(formula);
    
    // 带出客户默认地址
    UIRefPane vreceiveaddress = (UIRefPane) getHeadItem("vreceiveaddress")
        .getComponent();
    vreceiveaddress.setAutoCheck(false);
    // 收货地址参照
    ((CustAddrRefModel) vreceiveaddress.getRefModel())
        .setCustId(creceiptcorpid);
    String strvreceiveaddress = BillTools.getColValue2("bd_custaddr",
        "pk_custaddr", "defaddrflag", "Y", "pk_cubasdoc", pk_cubasdoc);
    vreceiveaddress.setPK(strvreceiveaddress);
    
    // 库存组织
    formula = "getColValue(bd_calbody,bodyname,pk_calbody,ccalbodyid)";
    String calbodyname = (String) execHeadFormula(formula);
    setHeadItem("ccalbodyname", calbodyname);
    // 销售组织
    formula = "getColValue(bd_salestru,vsalestruname,csalestruid,csalecorpid)";
    String vsalestruname = (String) execHeadFormula(formula);
    setHeadItem("csalecorpname", vsalestruname);
    // 散户
    formula = "bfreecustflag->getColValue(bd_cubasdoc,freecustflag,pk_cubasdoc,\""
        + pk_cubasdoc + "\")";
    getBillData().execHeadFormula(formula);
    
    Object objflag = getHeadItem("bfreecustflag").getValueObject();
    UFBoolean bfreecustflag = SmartVODataUtils.getUFBoolean(objflag);
    if (null == bfreecustflag || !bfreecustflag.booleanValue()) {
    	getHeadItem("cfreecustid").setValue(null);
    	
    	getHeadItem("cfreecustid").setEnabled(false);
        getHeadItem("cfreecustid").setEdit(false);
    }
    else {
      getHeadItem("cfreecustid").setEnabled(true);
      getHeadItem("cfreecustid").setEdit(true);
    }
    // 设置币种默认值
    try {
      UIRefPane ccurrencytypeid = (UIRefPane) getHeadItem("ccurrencyid")
          .getComponent();
      if (ccurrencytypeid.getRefPK() == null)
        ccurrencytypeid.setPK(SaleInvoiceTools.getSoBusiCurrUtil()
            .getLocalCurrPK());
    }
    catch (Exception e1) {
      SCMEnv.out(e1.getMessage());
    }
    // 清空人员参照约束
    UIRefPane cemployeeid = (UIRefPane) getHeadItem("cemployeeid")
        .getComponent();
    if (cemployeeid != null)
      cemployeeid.getRefModel().setWherePart(sEmployeeRefCondition);
    // 部门约束人员
    afterDeptEdit(null);
    
    
   String newccurrencyid = (String)getHeadItem("ccurrencyid").getValueObject();
    // 以前任意情况下，均调用，现在改为，如果以前为空，现在不为空才调用 因为币种变化会清调价格
    if (null == oldccurrencyid && null != newccurrencyid)
      afterCurrencyEdit(e);
  }

  
  
  private Hashtable<String, UFDouble> getOldhsSelectedARSubHVO() {
    if (oldhsSelectedARSubHVO == null) {
      oldhsSelectedARSubHVO = new Hashtable<String, UFDouble>();
    }
    return oldhsSelectedARSubHVO;
  }

  /**
   * 方法功能描述：根据单据ID加载并显示销售发票已有数据。
   * <b>参数说明</b>
   * @param sID
   * @return
   * @author fengjb
   * @time 2009-8-20 下午03:39:02
   */
  public void loadCardDataByID(String sID) {
    SaleinvoiceVO saleinvoice = null;
    try {
    	
    saleinvoice = SaleinvoiceBO_Client.queryBillDataByID(sID);
    
    }catch (Exception e) {
      SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000641") + e);
      if (getContainer() instanceof ToftPanel) {
        ((ToftPanel) getContainer()).showHintMessage(nc.ui.ml.NCLangRes
            .getInstance().getStrByID("SCMCOMMON", "UPPSCMCommon-000256")/*
                                                                           * @res
                                                                           * "数据加载失败！"
                                                                           */);
      }
    }
    loadCardData(saleinvoice);
  }
  
//  public SaleinvoiceVO loadDataWhole(SaleinvoiceVO saleinvoice, boolean bInMsgPanel) {
//    try {
//      SaleinvoiceBVO[] vosBody = (SaleinvoiceBVO[]) saleinvoice.getChildrenVO();
//      Object oTemp[][] = null;
//      nc.vo.pub.lang.UFBoolean b = new nc.vo.pub.lang.UFBoolean(false);
//      for(int i = 0; i < vosBody.length; i++){
//      	if(vosBody[i].getCinvbasdocid() == null || vosBody[i].getCinvbasdocid().trim().length() == 0) continue;
//      	if(vosBody[i].getCpackunitid() == null || vosBody[i].getCpackunitid().trim().length() == 0) continue;
//      	b = new nc.vo.pub.lang.UFBoolean(false);
//      	oTemp = CacheTool.getAnyValue2("bd_convert", "fixedflag", "pk_invbasdoc='" + vosBody[i].getCinvbasdocid() + "' and pk_measdoc='" + vosBody[i].getCpackunitid() + "'");
//      	if(oTemp != null && oTemp.length > 0 && oTemp[0]!=null&&oTemp[0].length>0&&oTemp[0][0] != null) b = new nc.vo.pub.lang.UFBoolean(oTemp[0][0].toString());
//      	if(b.booleanValue()){
//          	oTemp = CacheTool.getAnyValue2("bd_convert", "mainmeasrate", "pk_invbasdoc='" + vosBody[i].getCinvbasdocid() + "' and pk_measdoc='" + vosBody[i].getCpackunitid() + "'");
//      		if(oTemp != null && oTemp.length > 0 && oTemp[0][0] != null) vosBody[i].setScalefactor(new UFDouble(oTemp[0][0].toString()));
//      	}else{
//      		vosBody[i].setScalefactor(vosBody[i].getNnumber().div(vosBody[i].getNpacknumber()));
//      	}
//      }
// 
//      String dbilldate = ((SaleVO) saleinvoice.getParentVO()).getDbilldate()
//          .toString();
//      String currencyid = ((SaleinvoiceBVO) saleinvoice.getChildrenVO()[0])
//          .getCcurrencytypeid();
//      setPanelByCurrency(currencyid);
//      
//      //
//      UFDouble exchangeotobrate = ((SaleinvoiceBVO) saleinvoice.getChildrenVO()[0])
//          .getNexchangeotobrate();
//      //存货类字段
//      //add by fengjb V55新增存货类
//      String[] invclformul = new String[]{
//          "cinvclassid->getColValue(bd_invbasdoc,pk_invcl,pk_invbasdoc,cinvbasdocid)"
//      };
//      SoVoTools.execFormulas(invclformul, saleinvoice.getBodyVO());
//      
//      //发票折扣
//      UFDouble ninvoicediscountrate = saleinvoice.getBodyNotNullValue("ninvoicediscountrate");
//      saleinvoice.getHeadVO().setNinvoicediscountrate(ninvoicediscountrate);
//      
//      UFBoolean freef = ((SaleVO) saleinvoice.getParentVO()).getBfreecustflag();
//      ((SaleVO) saleinvoice.getParentVO()).setCcurrencyid(currencyid);
//
//      setBillValueVO(saleinvoice);
//      
//      long s1 = System.currentTimeMillis();
//      getBillModel().execLoadFormula();
//      execHeadFormula("csalecompanyname->getColValue(bd_corp,unitname,pk_corp,pk_corp)");
//      nc.vo.scm.pub.SCMEnv.out("执行公式[共用时" + (System.currentTimeMillis() - s1)
//          + "]");
//
//      if (bInMsgPanel == false) {
//        afterCurrencyChange(dbilldate);
//        if (exchangeotobrate != null)
//          setHeadItem("nexchangeotobrate", exchangeotobrate.toString());
//
//        boolean freeflag = (freef == null ? false : freef.booleanValue());
//        if (freeflag) {
//          getHeadItem("cfreecustid").setEnabled(true);
//          getHeadItem("cfreecustid").setEdit(true);
//        }
//        else {
//          getHeadItem("cfreecustid").setEnabled(false);
//          getHeadItem("cfreecustid").setEdit(false);
//        }
//      }
//      String custId = ((SaleVO) saleinvoice.getParentVO()).getCreceiptcorpid();
//      UIRefPane ref = (UIRefPane) getHeadItem("creceiptcorpid").getComponent();
//      //ref.setWhereString("1=1");
//      getHeadItem("creceiptcorpid").setComponent(ref);
//      ((UIRefPane) getHeadItem("creceiptcorpid").getComponent()).setPK(custId);
//     
//      UIRefPane bankref = null;
//  	  String bankId = saleinvoice.getHeadVO().getCcustbankid();
//  	  BillItem item = getHeadItem("ccustbankid");
//      if (item.getDataType() == BillItem.UFREF) {
//  		bankref = (UIRefPane) item.getComponent();
//  		if (null != bankref.getRefModel()) {
//  			bankref.getRefModel().setPk_corp(getCorp());
//  			bankref.setPK(bankId);
//  			String bankNo = bankref.getRefCode();
//  			setHeadItem("ccustomerbankNo", bankNo);
//  		}
//  	}
//
//      for (int kk = 0; kk < getRowCount(); kk++) {
//        if (getBillStatus() == BillStatus.FREE)
//          setAssistChange(kk);
//        beforeUnitChange(kk);
//        afterUnitChange(kk);
//        // 计算辅计量单价
//        String[] appendFormulaViaPrice = {
//            "norgviaprice->noriginalcurprice*scalefactor",
//            "norgviapricetax->noriginalcurtaxprice*scalefactor"
//        };
//        execBodyFormulas(kk, appendFormulaViaPrice);        
//      }
//      SaleInvoiceTools.initFreeItem(saleinvoice.getChildrenVO(), getBillModel());
//
//      updateValue();
//      getBillModel().reCalcurateAll();
//
//      nc.vo.scm.pub.SCMEnv.out("数据加载成功！");
//
//    }
////    catch (ValidationException e) {
////      MessageDialog.showErrorDlg(this, "", e.getMessage());
////    }
//    catch (Exception e) {
//      if (getContainer() instanceof ToftPanel) {
//        ((ToftPanel) getContainer())
//            .showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID(
//                "SCMCOMMON", "UPPSCMCommon-000256")/* @res "数据加载失败！" */);
//      }
//      nc.vo.scm.pub.SCMEnv.out(e);
//    }
//
//    return saleinvoice;
//  }

  /**
   * 方法功能描述：初始化自由项。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-14 下午02:12:19
   */
  public void initFreeItem() {

    CircularlyAccessibleValueObject[] bodyvos = getBillModel().getBodyValueVOs(
        SaleinvoiceBVO.class.getName());

    SaleInvoiceTools.initFreeItem(bodyvos, getBillModel());
  }

  /**
   * 方法功能描述：初始化表头下拉列表项。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-11 下午06:55:18
   */
  private void setHeadComboBox() {

		// 发票类型---前后台交互，数据库查询所有发票类型
		try {
			String[][] invoiceType = st.getInvoiceType();

			UIComboBox comtype = (UIComboBox) getHeadItem("finvoicetype")
					.getComponent();
			// 用户模板上设置的默认发票类型
			String defaultInvoiceType = getHeadItem("finvoicetype")
					.getDefaultValue();
			// 由于代码重新加载发票类型，需要清除原有模板上的发票类型
			comtype.removeAllItems();

			int selectIndex = 0;
			comtype.getSelectedItem();
			comtype.setTranslate(true);
			getHeadItem("finvoicetype").setWithIndex(true);
			for (int i = 0, iloop = invoiceType.length; i < iloop; i++) {
				comtype.addItem(invoiceType[i][1]);
				// 模板上选择的默认发票类型所对应的数据库中的发票类型
				if (null != defaultInvoiceType
						&& defaultInvoiceType.trim().equals(
								invoiceType[i][1].trim()))
					selectIndex = i;
			}
			comtype.setSelectedIndex(selectIndex);
		} catch (Exception e) {
			nc.vo.scm.pub.SCMEnv.out(nc.ui.ml.NCLangRes.getInstance().getStrByID("40060301", "UPT40060301-000642") + e.getMessage());
		}

		// 对冲标识
		UIComboBox comCountfalg = (UIComboBox) getHeadItem("fcounteractflag")
				.getComponent();
		for (SOComboxItem item : SaleInvoiceCombox.comCountflag) {
			comCountfalg.addItem(item);
		}
		getHeadItem("fcounteractflag").setWithIndex(true);
		// 单据状态
		UIComboBox comStatus = (UIComboBox) getHeadItem("fstatus")
				.getComponent();
		for (SOComboxItem item : SaleInvoiceCombox.comFstatus) {
			comStatus.addItem(item);
		}
		getHeadItem("fstatus").setWithIndex(true);
	}

  /**
   * 方法功能描述：加载卡片界面模板。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-11 下午07:48:13
   */
  private void loadThisTemplet() {


    if (getContainer() instanceof ToftPanel) {
      ((ToftPanel) getContainer())
          .showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("SCMCOMMON",
          "UPPSCMCommon-000135")/*
           * @res "开始加载列表模板...."
           * 
           */);
    }
    
    BillData bd = new BillData(st.getBillTempletVO());
    
    // 改变新参照的长度
    getFreeItemRefPane().setMaxLength(1000);

    // 设置新的参照，要求指出相应的字段名 初始化表体自由项
    bd.getBodyItem("vfree0").setComponent(getFreeItemRefPane());
    
 

    // 改变界面
    setCardPanel(bd);
    // 置入数据源
    setBillData(bd);

    // 设置自定义项
    DefSetTool.updateBillCardPanelUserDef(this, SaleInvoiceTools
        .getLoginPk_Corp(), SaleBillType.SaleInvoice,"vdef", "vdef");
    
    addBodyTotalListener(this);

    // 置入数据源
    setBillData(bd);
    // 初始化公式---可去掉
    BillTools.initItemKeys();

    // 初始化参照限制条件
    setInputLimit();
    
    setHeadComboBox();
    setBodyComboBox();
    // 初始化行号
    BillRowNo.loadRowNoItem(this, "crowno");

    setTatolRowShow(true);

    if (getContainer() instanceof ToftPanel) {
      ((ToftPanel) getContainer())
          .showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID(
              "SCMCOMMON", "UPPSCMCommon-000176")/* @res "模板加载成功！" */);
    }
  }

  private Hashtable<String, UFDouble> getHsSelectedARSubHVO() {
    if (hsSelectedARSubHVO == null) {
      hsSelectedARSubHVO = new Hashtable<String, UFDouble>();
    }
    return hsSelectedARSubHVO;
  }

  private void setHsSelectedARSubHVO(Hashtable hsSelectedARSubHVO) {
    this.hsSelectedARSubHVO = hsSelectedARSubHVO;
  }

  private void setOldhsSelectedARSubHVO(Hashtable oldhsSelectedARSubHVO) {
    this.oldhsSelectedARSubHVO = oldhsSelectedARSubHVO;
  }

  /**
   * 方法功能描述：初始化表体下拉列表。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-11 下午07:49:19
   */
  private void setBodyComboBox() {
		// 表体行状态
		UIComboBox comFrowstatus = (UIComboBox) getBodyItem("frowstatus")
				.getComponent();
		for (SOComboxItem item : SaleInvoiceCombox.comFrowstatus) {
			comFrowstatus.addItem(item);
		}
		getBodyItem("frowstatus").setWithIndex(true);
		// 批次状态
		UIComboBox comBatch = (UIComboBox) getBodyItem("fbatchstatus")
				.getComponent();
		for (SOComboxItem item : SaleInvoiceCombox.comFbatchstatus) {
			comBatch.addItem(item);
		}
		getBodyItem("fbatchstatus").setWithIndex(true);
		comBatch.setSelectedIndex(0);}

  /**
   * 修改表体行状态。 创建日期：(2001-11-26 9:30:07)
   * 
   * @param row
   *          int
   */
  private void setBodyRowState(int row) {
    if (getBillModel().getRowState(row) != BillModel.ADD)
      getBillModel().setRowState(row, BillModel.MODIFICATION);
  }

  /**
   * 方法功能描述：表头部门编辑后事件。
   * <b>参数说明</b>
   * @param e
   * @author fengjb
   * @time 2009-8-18 下午06:19:06
   */
  private void afterDeptEdit(BillEditEvent e) {
	  
//    UIRefPane cemployeeid = (UIRefPane) getHeadItem("cemployeeid")
//        .getComponent();
//    if (e != null) {
//      cemployeeid.setPK(null);
//    }
//    UIRefPane cdeptid = (UIRefPane) getHeadItem("cdeptid").getComponent();
//    if (null != cdeptid && null != cdeptid.getRefPK()) {
//          cemployeeid.getRefModel().addWherePart(
//              " and bd_psndoc.pk_deptdoc = '" + cdeptid.getRefPK() + "'");
//      } else {
//    	  
//       initSalePeopleRef();
//
//      }
	  initSalePeopleRef();
  }

  /**
   * 方法功能描述：销售发票粘贴行功能。
   * <b>参数说明</b>
   * @author fengjb
   * @time 2009-8-14 下午03:37:31
   */
  public void actionPasteLine() {
	  
	if(checkPasteLine()){
    
    // 取得粘贴前行数
    int iBefore = getRowCount();
    
    int rowOld = getBillTable().getSelectedRow();
    if (rowOld < 0 || rowOld > iBefore)
      return;
    //关闭合计
    boolean bisCalculate = getBillModel().isNeedCalculate();
    getBillModel().setNeedCalculate(false);
    // 粘贴
    pasteLine();
    
    int rowNew = getBillTable().getSelectedRow();
    for (int i = rowOld; i < rowNew; i++) {
    	//清除原有的ID字段
    	setBodyValueAt(null, i, "csaleid");
		setBodyValueAt(null, i, "cinvoice_bid");

        // 批次控制:改到单据摸版内项目：wholemanaflag 控制
        Object temp = getBodyValueAt(i, "wholemanaflag");
        boolean wholemanaflag = (temp == null ? false : new UFBoolean(temp
            .toString()).booleanValue());
        setCellEditable(i, "fbatchstatus", wholemanaflag);
        setCellEditable(i, "cbatchid", wholemanaflag);
        // 计量单位编辑状态处理
        setAssistChange(i);
      }

    // 取得粘贴后行数
    int iAfter = getRowCount();
    // 计算粘贴行数
    int iRow = iAfter - iBefore;
    if (iBefore > 0 && iAfter > 0 && iRow > 0) {
      BillRowNo.pasteLineRowNo(this, SaleBillType.SaleInvoice, "crowno", iRow);
    }
    setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
    execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
	}
  }
 /**
  * 方法功能描述：粘贴行到表尾功能。
  * <b>参数说明</b>
  * @author fengjb
  * @time 2008-12-1 上午09:14:40
  */
  public void actionPasteLineToTail(){

  if (checkPasteLine()) {
    
    //取得粘贴前行数
    int iBefore = getRowCount();
    
    pasteLineToTail();
    
    boolean bisCalculate = getBillModel()
    .isNeedCalculate();
    getBillModel().setNeedCalculate(false);
    for (int i = 0; i < getRowCount(); i++) {
      // 批次控制:改到单据摸版内项目：wholemanaflag 控制
      Object temp = getBodyValueAt(i, "wholemanaflag");
      boolean wholemanaflag = (temp == null ? false : new UFBoolean(temp
          .toString()).booleanValue());
      setCellEditable(i, "fbatchstatus", wholemanaflag);
      setCellEditable(i, "cbatchid", wholemanaflag);
    }
    // 计量单位编辑状态处理
    setAssistChange(getRowCount() - 1);


    //取得粘贴后行数
    int iAfter = getRowCount();
  
    //计算粘贴行数
    int iRow = iAfter - iBefore;
    if (iBefore > 0 && iAfter > 0 && iRow > 0) {
      //计算并设置新增行号
      BillRowNo.addLineRowNos(this, SaleBillType.SaleInvoice, "crowno", iRow);
    
    }
    setHeadItem("ntotalsummny", calcurateTotal("noriginalcursummny"));
    execHeadFormula("nnetmny->ntotalsummny-nstrikemny");
    getBillModel().setNeedCalculate(bisCalculate);
  }
 

  }
  /**
   * 方法功能描述：粘贴行前校验单据限行。
   * <b>参数说明</b>
   * @return
   * @author fengjb
   * @time 2008-8-11 下午03:31:44
   */
  private boolean checkPasteLine() {
	  //单据限行
		if (null != st.SO_67 && st.SO_67.intValue() > 0) {
			if (st.SO_67.intValue() < getRowCount() + iCopyRowCount) {
					 MessageDialog.showErrorDlg(this,"",nc.ui.ml.NCLangRes.getInstance()
							.getStrByID("40060501", "UPT40060501-000086", null,
									new String[] { st.SO_67.intValue() + "" })/* @res 发票单据限{SO67}行 */);
				
					return false;
				}
		}
	return true;
}

/**
 * 方法功能描述：卡片新增单据界面设置。
 * <b>参数说明</b>
 * @param voEdit
 * @author fengjb
 * @time 2009-8-17 上午10:02:56
 */
  public void setPanelForNewBill(SaleinvoiceVO voEdit) {
    
    // 增加单据行号
    nc.ui.scm.pub.report.BillRowNo.setVORowNoByRule(voEdit,
        nc.ui.scm.so.SaleBillType.SaleInvoice, "crowno");
    SaleinvoiceBVO[] newitemvos = voEdit.getBodyVO(); 
    for(int i=0,iloop =newitemvos.length;i<iloop;i++ ){
      setBodyValueAt(voEdit.getBodyVO()[i].getCrowno(), i, "crowno");
      getBillModel().setRowState(i,BillModel.ADD);
    }
    // 清除所有冲应收缓存
    processARBufWhenNewABill();
  }

  /**
   * 方法功能描述：检查VO是否满足保存条件。
   * <b>参数说明</b>
   * @param saleinvoice
   * @throws nc.vo.pub.ValidationException
   * @author fengjb
   * @time 2009-8-17 上午09:48:13
   */
  private void checkVO(SaleinvoiceVO saleinvoice)
      throws nc.vo.pub.ValidationException {
    // 单据体
    if (getRowCount() == 0)
      throw new nc.vo.pub.ValidationException(nc.ui.ml.NCLangRes.getInstance()
          .getStrByID("SCMCOMMON", "UPPSCMCommon-000073")/* @res "单据体不能为空!" */);
  
   //  单据限行
	if (st.SO_67 != null && st.SO_67.intValue() > 0) {
    
			if (st.SO_67.intValue() < getRowCount()) {
				throw new ValidationException(NCLangRes.getInstance().getStrByID("40060301", "UPP40060301-000171",
						null, new String[] { st.SO_67.intValue() + "" })/* @res "发票限{0}行!" */);
		}
	}
    // VO合法性检测
    saleinvoice.validate();
    // 用户单据设置非空项检测
    dataNotNullValidate();

    }
  /**
   * 方法功能描述：填充日志信息，用于后台写入日志。
   * <b>参数说明</b>
   * @param invoicevo
   * @author fengjb
   * @time 2009-8-17 上午10:36:46
   */
  private void fillLogInfo(SaleinvoiceVO invoicevo){
      //参数合法性校验	
	  if(null == invoicevo)
		  return ;
	  //用户ID
	  invoicevo.setCuruserid(SaleInvoiceTools.getLoginUserId());
	  //用户名
	  invoicevo.setCusername(SaleInvoiceTools.getLoginUserName());
	  //公司名
	  invoicevo.setCcorpname(SaleInvoiceTools.getLonginCorpName());
	  
  }
}