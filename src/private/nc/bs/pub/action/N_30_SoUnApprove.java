package nc.bs.pub.action;

import java.util.Hashtable;

import nc.bs.pub.compiler.AbstractCompiler2;
import nc.impl.so.sointerface.SaleToCRM;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.scm.crm.CrmSynchroVO;
import nc.vo.uap.pf.PFBusinessException;
import nc.vo.xcgl.pub.consts.PubBillTypeConst;
import nc.vo.zmpub.pub.tool.ZmPubTool;

/**
 * 备注：销售订单的弃审 单据动作执行中的动态执行类的动态执行类。
 * 
 * 创建日期：(2008-12-31)
 * 
 * @author 平台脚本生成
 */
public class N_30_SoUnApprove extends AbstractCompiler2 {
	private java.util.Hashtable m_methodReturnHas = new java.util.Hashtable();
	private Hashtable m_keyHas = null;

	/**
	 * N_30_SoUnApprove 构造子注解。
	 */
	public N_30_SoUnApprove() {
		super();
	}

	/*
	 * 备注：平台编写规则类 接口执行类
	 */
	public Object runComClass(PfParameterVO vo) throws BusinessException {
		try {
			super.m_tmpVo = vo;
			// ####本脚本必须含有返回值,返回DLG和PNL的组件不允许有返回值####
			// *************从平台取得由该动作传入的入口参数。***********
			Object inObject = getVo();
			// 1,首先检查传入参数类型是否合法，是否为空。
			if (!(inObject instanceof nc.vo.so.so001.SaleOrderVO))
				throw new nc.vo.pub.BusinessException(
						nc.bs.ml.NCLangResOnserver.getInstance().getStrByID(
								"40060301", "UPT40060301-000602")/*
																 * @res
																 * "错误：您希望保存的销售订单类型不匹配"
																 */);
			if (inObject == null)
				throw new nc.vo.pub.BusinessException(
						nc.bs.ml.NCLangResOnserver.getInstance().getStrByID(
								"40060301", "UPT40060301-000603")/*
																 * @res
																 * "错误：您希望保存的销售订单没有数据"
																 */);
			// 2,数据合法，把数据转换。
			nc.vo.so.so001.SaleOrderVO inVO = (nc.vo.so.so001.SaleOrderVO) inObject;
			String pk_bill = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())
					.getCsaleid();
			String billtype = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())
					.getCreceipttype();
			//mlr
			//查询下游是否存在销售过磅登记
			ZmPubTool.checkExitNextBill(billtype, pk_bill, PubBillTypeConst.billtype_saleweighdoc);
			//mlr
			nc.vo.so.so001.SaleorderBVO[] body = inVO.getBodyVOs();
			String[] bids = new String[body.length];
			for (int i = 0, len = body.length; i < len; i++) {
				bids[i] = body[i].getCorder_bid();
			}
			String userid = inVO.getClientLink().getUser();
			nc.vo.pub.lang.UFDate logindate = inVO.getClientLink()
					.getLogonDate();
			inObject = null;
			Integer iAct = new Integer(
					nc.vo.so.so001.ISaleOrderAction.A_UNAUDIT);
			setParameter("iAction", iAct);
			// **************************************************************************************************
			setParameter("INVO", inVO);
			setParameter("PKBILL", pk_bill);
			setParameter("BILLTYPE", billtype);
			String sActionMsg = "";
			setParameter("ACTIONMSG", sActionMsg);
			String sButtonName = nc.bs.ml.NCLangResOnserver.getInstance()
					.getStrByID("common", "UC001-0000028");/* @res "弃审" */
			setParameter("BUTTONNAME", sButtonName);
			String sUserDate = getUserDate() != null ? getUserDate().getDate()
					.toString() : null;
			setParameter("USERDATE", sUserDate);
			String sOperid = getOperator();
			setParameter("OPERID", sOperid);
			// **************************************************************************************************
			Object retObj = null;
			// 方法说明:加锁
			runClass("nc.impl.scm.so.pub.DataControlDMO", "lockPkForVo",
					"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
					m_methodReturnHas);
			if (retObj != null) {
				m_methodReturnHas.put("lockPkForVo", retObj);
			}
			// ##################################################
			try {
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:并发检查
				runClass("nc.impl.scm.so.pub.ParallelCheckDMO",
						"checkVoNoChanged",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("checkVoNoChanged", retObj);
				}
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:互斥检查
				runClass("nc.impl.scm.so.pub.CheckStatusDMO",
						"isUnApproveStatus",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("isUnApproveStatus", retObj);
				}
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 调用信用____开始____(必须跟“调用信用____结束____”成对出现)
				nc.itf.uap.sf.ICreateCorpQueryService corpService = (nc.itf.uap.sf.ICreateCorpQueryService) nc.bs.framework.common.NCLocator
						.getInstance().lookup(
								nc.itf.uap.sf.ICreateCorpQueryService.class
										.getName());
				boolean creditEnabled = corpService.isEnabled(
						inVO.getPk_corp(), "SO6");
				Object creditObject = null;
				Object creditPara = null;
				if (creditEnabled) {
					// 注意：此处不能采用runClassCom的方式进行调用
					nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager) nc.bs.framework.common.NCLocator
							.getInstance()
							.lookup(
									nc.itf.so.so120.IBillInvokeCreditManager.class
											.getName());
					nc.vo.so.credit.SOCreditPara para = new nc.vo.so.credit.SOCreditPara(
							new String[] { inVO.getPrimaryKey() },
							null,
							nc.vo.scm.pub.bill.CreditConst.ICREDIT_ACT_UNAPPROVE,
							new String[] { inVO.getBizTypeid() }, inVO
									.getPk_corp(), creditManager);
					creditObject = creditManager;
					creditPara = para;
					creditManager.renovateARByHidsBegin(para);
				}
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:跨公司直运销售订单弃审后删除调拨订单或者调入申请单
				if (inVO.getHeadVO().getFstatus() != null
						&& inVO.getHeadVO().getFstatus().intValue() == 2) {
					runClass("nc.impl.scm.so.so001.SaleOrderSrvDMO",
							"delete5DwhenUnApprove",
							"&INVO:nc.vo.so.so001.SaleOrderVO", vo, m_keyHas,
							m_methodReturnHas);
					if (retObj != null) {
						m_methodReturnHas.put("delete5DwhenUnApprove", retObj);
					}
				}
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:弃审检查
				runClass("nc.impl.scm.so.pub.CheckExecDMOImpl",
						"isUnSaleOrder",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("isUnSaleOrder", retObj);
				}
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:判断上游单据是否存在下游单据
				/** 产品启用判断，去掉对pu的依赖* */
				nc.itf.uap.sf.ICreateCorpQueryService icorp = (nc.itf.uap.sf.ICreateCorpQueryService) nc.bs.framework.common.NCLocator
						.getInstance().lookup(
								nc.itf.uap.sf.ICreateCorpQueryService.class
										.getName());
				java.util.Hashtable pEnabled = icorp.queryProductEnabled(inVO
						.getPk_corp(),
						new String[] { nc.vo.pub.ProductCode.PROD_PO });
				if (((nc.vo.pub.lang.UFBoolean) pEnabled
						.get(nc.vo.pub.ProductCode.PROD_PO)).booleanValue()) {
					nc.itf.pu.inter.IPuToIc_ToIC bo = (nc.itf.pu.inter.IPuToIc_ToIC) nc.bs.framework.common.NCLocator
							.getInstance().lookup(
									nc.itf.pu.inter.IPuToIc_ToIC.class
											.getName());
					bo.isExistBill(billtype, pk_bill);
					if (retObj != null) {
						m_methodReturnHas.put("isExistBill", retObj);
					}
				}

				// 插件处理
				nc.bs.scm.plugin.InvokeEventProxy eventproxy = new nc.bs.scm.plugin.InvokeEventProxy(
						"SO", "30");
				eventproxy.beforeAction(nc.vo.scm.plugin.Action.UNAUDIT,
						new nc.vo.pub.AggregatedValueObject[] { inVO }, null);
				// ##################################################
				// ####该组件为单动作工作流处理开始...不能进行修改####
				boolean isFinishToGoing = procUnApproveFlow(vo);
				nc.vo.scm.pub.SCMEnv
						.out("工作流处理结束procUnApproveFlow：isFinishToGoing = "
								+ isFinishToGoing);/* -=notranslate=- */
				if (inVO.getHeadVO().getFstatus() != null
						&& inVO.getHeadVO().getFstatus().intValue() != 2)
					return retObj;
				// ##################################################

				// 插件处理
				eventproxy.afterAction(nc.vo.scm.plugin.Action.UNAUDIT,
						new nc.vo.pub.AggregatedValueObject[] { inVO }, null);
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:将单据状态改为“自由”
				// runClass("nc.impl.scm.so.pub.BusinessControlDMO",
				// "setBillFree",
				// "&PKBILL:String,&BILLTYPE:String",vo,m_keyHas,m_methodReturnHas);
				// if (retObj != null) {
				// m_methodReturnHas.put("setBillFree", retObj);
				// }

				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:回写借出单订货量（减少）
				runClass("nc.impl.scm.so.pub.OtherInterfaceDMO",
						"setDecreaseSaleOut",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("setDecreaseSaleOut", retObj);
				}
				// ##################################################
				// atp check release 时去掉
				setParameter("CURVO", inVO);
				setParameter("PREVO", null);
				runClass(
						"nc.impl.so.sointerface.SOATP",
						"checkAtpInstantly",
						"&CURVO:nc.vo.pub.AggregatedValueObject,&PREVO:nc.vo.pub.AggregatedValueObject",
						vo, m_keyHas, m_methodReturnHas);
				// ************************把销售订单置入参数表。**************************************************
				// ##################################################
				// ####重要说明：生成的业务组件方法尽量不要进行修改####
				// 方法说明:订单修订、弃审传递消息到生产
				Integer type = new Integer(1);
				setParameter("TYPE", type);
				runClass("nc.impl.scm.so.pub.OtherInterfaceDMO",
						"orderChangeSendInfo",
						"&INVO:nc.vo.so.so001.SaleOrderVO,&TYPE:INTEGER", vo,
						m_keyHas, m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("orderChangeSendInfo", retObj);
				}
				// ##################################################
				// 记录日志
				inVO
						.insertOperLog(inVO, sActionMsg, getOperator(),
								sButtonName);
				// setParameter("ACTIONMSG", sActionMsg);
				// runClass( "nc.impl.scm.so.pub.DataControlDMO",
				// "insertBusinesslog",
				// "&INVO:nc.vo.pub.AggregatedValueObject,&ACTIONMSG:String,&BUTTONNAME:String,&OPERID:String,&USERDATE:String",vo,m_keyHas,m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("insertBusinesslog", retObj);
				}
				// ##################################################

				// 自动解除库存硬锁定
				nc.itf.ic.service.IICToSO iictoso = (nc.itf.ic.service.IICToSO) nc.bs.framework.common.NCLocator
						.getInstance().lookup(
								nc.itf.ic.service.IICToSO.class.getName());
				iictoso.unLockInv(billtype, bids, userid, logindate);

				// ##################################################
				// 调用信用____结束____(必须跟“调用信用____开始____”成对出现)
				if (creditEnabled) {
					nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager) creditObject;
					creditManager
							.renovateARByHidsEnd((nc.vo.so.credit.SOCreditPara) creditPara);
				}

				// ####################################################################################################
				// 劳务折扣类存货自动出库打开
				runClass("nc.impl.scm.so.so001.SaleOrderImpl",
						"processLaborOutOpenWhenUnApprove",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("processInvoicendStateWhenApprove",
							retObj);
				}

				/********** CRM适配 begin **********/
				nc.bs.logging.Logger.error(">>>>>>>销售订单【"
						+ inVO.getHeadVO().getVreceiptcode() + "】弃审同步到CRM开始");
				new SaleToCRM().synchronizeSO(inVO, CrmSynchroVO.ICRM_DISABLE);
				nc.bs.logging.Logger.error(">>>>>>>销售订单【"
						+ inVO.getHeadVO().getVreceiptcode() + "】弃审同步到CRM结束");
				/********** CRM适配 end **********/
		
				

			} catch (Exception e) {
				// ##################################################
				sActionMsg = e.getMessage();
				setParameter("ACTIONMSG", sActionMsg);
				// 记录日志
				runClass(
						"nc.impl.scm.so.pub.DataControlDMO",
						"insertBusinesslog",
						"&INVO:nc.vo.pub.AggregatedValueObject,&ACTIONMSG:String,&BUTTONNAME:String,&OPERID:String,&USERDATE:String",
						vo, m_keyHas, m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("insertBusinesslog", retObj);
				}
				// ##################################################
				throw nc.vo.so.pub.ExceptionUtils
						.wrappBusinessExceptionForSO(e);
			}
			// ####重要说明：生成的业务组件方法尽量不要进行修改####
			// *********返回结果******************************************************
			inVO = null;
			pk_bill = null;
			billtype = null;
			sActionMsg = null;
			sOperid = null;
			sButtonName = null;
			sUserDate = null;

			//
			Object ret = runClass("nc.impl.scm.so.pub.DataControlDMO",
					"ifPushSave205A5D", "&INVO:nc.vo.so.so001.SaleOrderVO", vo,
					m_keyHas, m_methodReturnHas);
			if (retObj != null) {
				m_methodReturnHas.put("insertBusinesslog", retObj);
			}

			return ret;
			// ************************************************************************
		} catch (Exception ex) {
			if (ex instanceof BusinessException)
				throw (BusinessException) ex;
			else
				throw new PFBusinessException(ex.getMessage(), ex);
		}
	}

	/*
	 * 备注：平台编写原始脚本
	 */
	public String getCodeRemark() {
		return "	// ####本脚本必须含有返回值,返回DLG和PNL的组件不允许有返回值####\n// *************从平台取得由该动作传入的入口参数。***********\nObject inObject = getVo();\n// 1,首先检查传入参数类型是否合法，是否为空。\nif (!(inObject instanceof nc.vo.so.so001.SaleOrderVO))\n	throw new  nc.vo.pub.BusinessException(\"错误：您希望保存的销售订单类型不匹配\");\nif (inObject == null)\n	throw new  nc.vo.pub.BusinessException(\"错误：您希望保存的销售订单没有数据\");\n// 2,数据合法，把数据转换。\nnc.vo.so.so001.SaleOrderVO inVO = (nc.vo.so.so001.SaleOrderVO) inObject;\nString pk_bill = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())\n		.getCsaleid();\nString billtype = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())\n		.getCreceipttype();\nnc.vo.so.so001.SaleorderBVO[] body = inVO.getBodyVOs();\nString[] bids = new String[body.length];\nfor (int i = 0, len = body.length; i < len; i++) {\n	bids[i] = body[i].getCorder_bid();\n}\nString userid = inVO.getClientLink().getUser();\nnc.vo.pub.lang.UFDate logindate = inVO.getClientLink()\n		.getLogonDate();\ninObject = null;\nInteger iAct = new Integer(\n		nc.vo.so.so001.ISaleOrderAction.A_UNAUDIT);\nsetParameter(\"iAction\", iAct);\n// **************************************************************************************************\nsetParameter(\"INVO\", inVO);\nsetParameter(\"PKBILL\", pk_bill);\nsetParameter(\"BILLTYPE\", billtype);\nString sActionMsg = \"\";\nsetParameter(\"ACTIONMSG\", sActionMsg);\nString sButtonName = nc.bs.ml.NCLangResOnserver.getInstance()\n		.getStrByID(\"common\", \"UC001-0000028\");/* @res \"弃审\" */\nsetParameter(\"BUTTONNAME\", sButtonName);\nString sUserDate = getUserDate() != null ? getUserDate().getDate()\n		.toString() : null;\nsetParameter(\"USERDATE\", sUserDate);\nString sOperid = getOperator();\nsetParameter(\"OPERID\", sOperid);\n// **************************************************************************************************\nObject retObj = null;\n// 方法说明:加锁\nrunClassCom@\"nc.impl.scm.so.pub.DataControlDMO\", \"lockPkForVo\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\nif (retObj != null) {\n	m_methodReturnHas.put(\"lockPkForVo\", retObj);\n}\n// ##################################################\ntry {\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:并发检查\n	runClassCom@\"nc.impl.scm.so.pub.ParallelCheckDMO\", \"checkVoNoChanged\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"checkVoNoChanged\", retObj);\n	}\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:互斥检查\n	runClassCom@\"nc.impl.scm.so.pub.CheckStatusDMO\", \"isUnApproveStatus\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"isUnApproveStatus\", retObj);\n	}\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 调用信用____开始____(必须跟“调用信用____结束____”成对出现)\n	nc.itf.uap.sf.ICreateCorpQueryService corpService = (nc.itf.uap.sf.ICreateCorpQueryService)nc.bs.framework.common.NCLocator.getInstance().lookup(nc.itf.uap.sf.ICreateCorpQueryService.class.getName());\n	boolean creditEnabled = corpService.isEnabled(inVO.getPk_corp(), \"SO6\");\n	Object creditObject = null;\n	Object creditPara = null;\n	if (creditEnabled) {\n		// 注意：此处不能采用runClassCom的方式进行调用\n		nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager) nc.bs.framework.common.NCLocator\n				.getInstance().lookup(nc.itf.so.so120.IBillInvokeCreditManager.class.getName());\n		nc.vo.so.credit.SOCreditPara para = new nc.vo.so.credit.SOCreditPara(new String[] { inVO.getPrimaryKey() }, null,\n				nc.vo.scm.pub.bill.CreditConst.ICREDIT_ACT_UNAPPROVE, new String[] { inVO.getBizTypeid() }, inVO.getPk_corp(),\n				creditManager);\n		creditObject = creditManager;\n		creditPara = para;\n		creditManager.renovateARByHidsBegin(para);\n	}\n	// ##################################################\n	//####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:跨公司直运销售订单弃审后删除调拨订单或者调入申请单\n	if (inVO.getHeadVO().getFstatus() != null\n			&& inVO.getHeadVO().getFstatus().intValue() == 2) {\n		runClassCom@\"nc.impl.scm.so.so001.SaleOrderSrvDMO\", \"delete5DwhenUnApprove\", \"&INVO:nc.vo.so.so001.SaleOrderVO\"@;\n		if (retObj != null) {\n			m_methodReturnHas.put(\"delete5DwhenUnApprove\", retObj);\n		}\n	}\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:弃审检查\n	runClassCom@\"nc.impl.scm.so.pub.CheckExecDMOImpl\", \"isUnSaleOrder\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"isUnSaleOrder\", retObj);\n	}\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:判断上游单据是否存在下游单据\n	/** 产品启用判断，去掉对pu的依赖* */\n	nc.itf.uap.sf.ICreateCorpQueryService icorp = (nc.itf.uap.sf.ICreateCorpQueryService) nc.bs.framework.common.NCLocator\n			.getInstance().lookup(nc.itf.uap.sf.ICreateCorpQueryService.class.getName());\n	java.util.Hashtable pEnabled = icorp.queryProductEnabled(inVO.getPk_corp(),\n			new String[] { nc.vo.pub.ProductCode.PROD_PO });\n	if (((nc.vo.pub.lang.UFBoolean) pEnabled.get(nc.vo.pub.ProductCode.PROD_PO)).booleanValue()) {\n		nc.itf.pu.inter.IPuToIc_ToIC bo = (nc.itf.pu.inter.IPuToIc_ToIC) nc.bs.framework.common.NCLocator.getInstance()\n				.lookup(nc.itf.pu.inter.IPuToIc_ToIC.class.getName());\n"
				+ /* -=notranslate=- */
				"		bo.isExistBill(billtype, pk_bill);\n		if (retObj != null) {\n			m_methodReturnHas.put(\"isExistBill\", retObj);\n		}\n	}\n	\n	// 插件处理\n	nc.bs.scm.plugin.InvokeEventProxy eventproxy = new nc.bs.scm.plugin.InvokeEventProxy(\"SO\",\"30\");\n	eventproxy.beforeAction(nc.vo.scm.plugin.Action.UNAUDIT, new nc.vo.pub.AggregatedValueObject[]{inVO}, null);\n	// ##################################################\n	// ####该组件为单动作工作流处理开始...不能进行修改####\n	boolean isFinishToGoing = procUnApproveFlow(vo);\n	nc.vo.scm.pub.SCMEnv.out(\"工作流处理结束procUnApproveFlow：isFinishToGoing = \" + isFinishToGoing);\n	if (inVO.getHeadVO().getFstatus() != null\n			&& inVO.getHeadVO().getFstatus().intValue() != 2)\n		return retObj;\n	// ##################################################\n	\n	// 插件处理\n	eventproxy.afterAction(nc.vo.scm.plugin.Action.UNAUDIT, new nc.vo.pub.AggregatedValueObject[]{inVO}, null);\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:将单据状态改为“自由”\n	runClassCom@\"nc.impl.scm.so.pub.BusinessControlDMO\", \"setBillFree\", \"&PKBILL:String,&BILLTYPE:String\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"setBillFree\", retObj);\n	}\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:回写借出单订货量（减少）\n	runClassCom@\"nc.impl.scm.so.pub.OtherInterfaceDMO\", \"setDecreaseSaleOut\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"setDecreaseSaleOut\", retObj);\n	}\n	// ##################################################\n	// atp check  release 时去掉\n	setParameter(\"CURVO\", inVO);\n	setParameter(\"PREVO\", null);\n	runClassCom@\"nc.impl.so.sointerface.SOATP\", \"checkAtpInstantly\", \"&CURVO:nc.vo.pub.AggregatedValueObject,&PREVO:nc.vo.pub.AggregatedValueObject\"@;\n	// ************************把销售订单置入参数表。**************************************************\n	// ##################################################\n	// ####重要说明：生成的业务组件方法尽量不要进行修改####\n	// 方法说明:订单修订、弃审传递消息到生产\n	Integer type = new Integer(1);\n	setParameter(\"TYPE\", type);\n	runClassCom@\"nc.impl.scm.so.pub.OtherInterfaceDMO\", \"orderChangeSendInfo\", \"&INVO:nc.vo.so.so001.SaleOrderVO,&TYPE:INTEGER\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"orderChangeSendInfo\", retObj);\n	}\n	// ##################################################\n	// 记录日志\n	sActionMsg = \"单据号为: \"+inVO.getHeadVO().getVreceiptcode()+\" 的销售订单弃审成功!\";\n	setParameter(\"ACTIONMSG\", sActionMsg);\n	runClassCom@ \"nc.impl.scm.so.pub.DataControlDMO\", \"insertBusinesslog\", \"&INVO:nc.vo.pub.AggregatedValueObject,&ACTIONMSG:String,&BUTTONNAME:String,&OPERID:String,&USERDATE:String\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"insertBusinesslog\", retObj);\n	}\n	// ##################################################\n	\n	//自动解除库存硬锁定\n	nc.itf.ic.service.IICToSO iictoso = (nc.itf.ic.service.IICToSO)nc.bs.framework.common.NCLocator.getInstance().lookup(nc.itf.ic.service.IICToSO.class.getName());\n	iictoso.unLockInv(billtype,bids,userid,logindate);\n	\n	// ##################################################\n	// 调用信用____结束____(必须跟“调用信用____开始____”成对出现)\n	if (creditEnabled) {\n		nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager)creditObject;\n		creditManager.renovateARByHidsEnd((nc.vo.so.credit.SOCreditPara)creditPara);\n	}\n} catch (Exception e) {\n	// ##################################################\n	sActionMsg = e.getMessage();\n	setParameter(\"ACTIONMSG\", sActionMsg);\n	// 记录日志\n	runClassCom@\"nc.impl.scm.so.pub.DataControlDMO\", \"insertBusinesslog\", \"&INVO:nc.vo.pub.AggregatedValueObject,&ACTIONMSG:String,&BUTTONNAME:String,&OPERID:String,&USERDATE:String\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"insertBusinesslog\", retObj);\n	}\n	// ##################################################\n	throw nc.vo.so.pub.ExceptionUtils.wrappBusinessExceptionForSO(e);\n} \n// ####重要说明：生成的业务组件方法尽量不要进行修改####\n// *********返回结果******************************************************\ninVO = null;\npk_bill = null;\nbilltype = null;\nsActionMsg = null;\nsOperid = null;\nsButtonName = null;\nsUserDate = null;\nreturn retObj;\n// ************************************************************************\n";
	}/* -=notranslate=- */

	/*
	 * 备注：设置脚本变量的HAS
	 */
	private void setParameter(String key, Object val) {
		if (m_keyHas == null) {
			m_keyHas = new Hashtable();
		}
		if (val != null) {
			m_keyHas.put(key, val);
		}
	}
}
