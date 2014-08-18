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
 * ��ע�����۶��������� ���ݶ���ִ���еĶ�ִ̬����Ķ�ִ̬���ࡣ
 * 
 * �������ڣ�(2008-12-31)
 * 
 * @author ƽ̨�ű�����
 */
public class N_30_SoUnApprove extends AbstractCompiler2 {
	private java.util.Hashtable m_methodReturnHas = new java.util.Hashtable();
	private Hashtable m_keyHas = null;

	/**
	 * N_30_SoUnApprove ������ע�⡣
	 */
	public N_30_SoUnApprove() {
		super();
	}

	/*
	 * ��ע��ƽ̨��д������ �ӿ�ִ����
	 */
	public Object runComClass(PfParameterVO vo) throws BusinessException {
		try {
			super.m_tmpVo = vo;
			// ####���ű����뺬�з���ֵ,����DLG��PNL������������з���ֵ####
			// *************��ƽ̨ȡ���ɸö����������ڲ�����***********
			Object inObject = getVo();
			// 1,���ȼ�鴫����������Ƿ�Ϸ����Ƿ�Ϊ�ա�
			if (!(inObject instanceof nc.vo.so.so001.SaleOrderVO))
				throw new nc.vo.pub.BusinessException(
						nc.bs.ml.NCLangResOnserver.getInstance().getStrByID(
								"40060301", "UPT40060301-000602")/*
																 * @res
																 * "������ϣ����������۶������Ͳ�ƥ��"
																 */);
			if (inObject == null)
				throw new nc.vo.pub.BusinessException(
						nc.bs.ml.NCLangResOnserver.getInstance().getStrByID(
								"40060301", "UPT40060301-000603")/*
																 * @res
																 * "������ϣ����������۶���û������"
																 */);
			// 2,���ݺϷ���������ת����
			nc.vo.so.so001.SaleOrderVO inVO = (nc.vo.so.so001.SaleOrderVO) inObject;
			String pk_bill = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())
					.getCsaleid();
			String billtype = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())
					.getCreceipttype();
			//mlr
			//��ѯ�����Ƿ�������۹����Ǽ�
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
					.getStrByID("common", "UC001-0000028");/* @res "����" */
			setParameter("BUTTONNAME", sButtonName);
			String sUserDate = getUserDate() != null ? getUserDate().getDate()
					.toString() : null;
			setParameter("USERDATE", sUserDate);
			String sOperid = getOperator();
			setParameter("OPERID", sOperid);
			// **************************************************************************************************
			Object retObj = null;
			// ����˵��:����
			runClass("nc.impl.scm.so.pub.DataControlDMO", "lockPkForVo",
					"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
					m_methodReturnHas);
			if (retObj != null) {
				m_methodReturnHas.put("lockPkForVo", retObj);
			}
			// ##################################################
			try {
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:�������
				runClass("nc.impl.scm.so.pub.ParallelCheckDMO",
						"checkVoNoChanged",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("checkVoNoChanged", retObj);
				}
				// ##################################################
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:������
				runClass("nc.impl.scm.so.pub.CheckStatusDMO",
						"isUnApproveStatus",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("isUnApproveStatus", retObj);
				}
				// ##################################################
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ��������____��ʼ____(���������������____����____���ɶԳ���)
				nc.itf.uap.sf.ICreateCorpQueryService corpService = (nc.itf.uap.sf.ICreateCorpQueryService) nc.bs.framework.common.NCLocator
						.getInstance().lookup(
								nc.itf.uap.sf.ICreateCorpQueryService.class
										.getName());
				boolean creditEnabled = corpService.isEnabled(
						inVO.getPk_corp(), "SO6");
				Object creditObject = null;
				Object creditPara = null;
				if (creditEnabled) {
					// ע�⣺�˴����ܲ���runClassCom�ķ�ʽ���е���
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
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:�繫˾ֱ�����۶��������ɾ�������������ߵ������뵥
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
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:������
				runClass("nc.impl.scm.so.pub.CheckExecDMOImpl",
						"isUnSaleOrder",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("isUnSaleOrder", retObj);
				}
				// ##################################################
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:�ж����ε����Ƿ�������ε���
				/** ��Ʒ�����жϣ�ȥ����pu������* */
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

				// �������
				nc.bs.scm.plugin.InvokeEventProxy eventproxy = new nc.bs.scm.plugin.InvokeEventProxy(
						"SO", "30");
				eventproxy.beforeAction(nc.vo.scm.plugin.Action.UNAUDIT,
						new nc.vo.pub.AggregatedValueObject[] { inVO }, null);
				// ##################################################
				// ####�����Ϊ����������������ʼ...���ܽ����޸�####
				boolean isFinishToGoing = procUnApproveFlow(vo);
				nc.vo.scm.pub.SCMEnv
						.out("�������������procUnApproveFlow��isFinishToGoing = "
								+ isFinishToGoing);/* -=notranslate=- */
				if (inVO.getHeadVO().getFstatus() != null
						&& inVO.getHeadVO().getFstatus().intValue() != 2)
					return retObj;
				// ##################################################

				// �������
				eventproxy.afterAction(nc.vo.scm.plugin.Action.UNAUDIT,
						new nc.vo.pub.AggregatedValueObject[] { inVO }, null);
				// ##################################################
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:������״̬��Ϊ�����ɡ�
				// runClass("nc.impl.scm.so.pub.BusinessControlDMO",
				// "setBillFree",
				// "&PKBILL:String,&BILLTYPE:String",vo,m_keyHas,m_methodReturnHas);
				// if (retObj != null) {
				// m_methodReturnHas.put("setBillFree", retObj);
				// }

				// ##################################################
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:��д����������������٣�
				runClass("nc.impl.scm.so.pub.OtherInterfaceDMO",
						"setDecreaseSaleOut",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("setDecreaseSaleOut", retObj);
				}
				// ##################################################
				// atp check release ʱȥ��
				setParameter("CURVO", inVO);
				setParameter("PREVO", null);
				runClass(
						"nc.impl.so.sointerface.SOATP",
						"checkAtpInstantly",
						"&CURVO:nc.vo.pub.AggregatedValueObject,&PREVO:nc.vo.pub.AggregatedValueObject",
						vo, m_keyHas, m_methodReturnHas);
				// ************************�����۶������������**************************************************
				// ##################################################
				// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
				// ����˵��:�����޶������󴫵���Ϣ������
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
				// ��¼��־
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

				// �Զ�������Ӳ����
				nc.itf.ic.service.IICToSO iictoso = (nc.itf.ic.service.IICToSO) nc.bs.framework.common.NCLocator
						.getInstance().lookup(
								nc.itf.ic.service.IICToSO.class.getName());
				iictoso.unLockInv(billtype, bids, userid, logindate);

				// ##################################################
				// ��������____����____(���������������____��ʼ____���ɶԳ���)
				if (creditEnabled) {
					nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager) creditObject;
					creditManager
							.renovateARByHidsEnd((nc.vo.so.credit.SOCreditPara) creditPara);
				}

				// ####################################################################################################
				// �����ۿ������Զ������
				runClass("nc.impl.scm.so.so001.SaleOrderImpl",
						"processLaborOutOpenWhenUnApprove",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas,
						m_methodReturnHas);
				if (retObj != null) {
					m_methodReturnHas.put("processInvoicendStateWhenApprove",
							retObj);
				}

				/********** CRM���� begin **********/
				nc.bs.logging.Logger.error(">>>>>>>���۶�����"
						+ inVO.getHeadVO().getVreceiptcode() + "������ͬ����CRM��ʼ");
				new SaleToCRM().synchronizeSO(inVO, CrmSynchroVO.ICRM_DISABLE);
				nc.bs.logging.Logger.error(">>>>>>>���۶�����"
						+ inVO.getHeadVO().getVreceiptcode() + "������ͬ����CRM����");
				/********** CRM���� end **********/
		
				

			} catch (Exception e) {
				// ##################################################
				sActionMsg = e.getMessage();
				setParameter("ACTIONMSG", sActionMsg);
				// ��¼��־
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
			// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####
			// *********���ؽ��******************************************************
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
	 * ��ע��ƽ̨��дԭʼ�ű�
	 */
	public String getCodeRemark() {
		return "	// ####���ű����뺬�з���ֵ,����DLG��PNL������������з���ֵ####\n// *************��ƽ̨ȡ���ɸö����������ڲ�����***********\nObject inObject = getVo();\n// 1,���ȼ�鴫����������Ƿ�Ϸ����Ƿ�Ϊ�ա�\nif (!(inObject instanceof nc.vo.so.so001.SaleOrderVO))\n	throw new  nc.vo.pub.BusinessException(\"������ϣ����������۶������Ͳ�ƥ��\");\nif (inObject == null)\n	throw new  nc.vo.pub.BusinessException(\"������ϣ����������۶���û������\");\n// 2,���ݺϷ���������ת����\nnc.vo.so.so001.SaleOrderVO inVO = (nc.vo.so.so001.SaleOrderVO) inObject;\nString pk_bill = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())\n		.getCsaleid();\nString billtype = ((nc.vo.so.so001.SaleorderHVO) inVO.getParentVO())\n		.getCreceipttype();\nnc.vo.so.so001.SaleorderBVO[] body = inVO.getBodyVOs();\nString[] bids = new String[body.length];\nfor (int i = 0, len = body.length; i < len; i++) {\n	bids[i] = body[i].getCorder_bid();\n}\nString userid = inVO.getClientLink().getUser();\nnc.vo.pub.lang.UFDate logindate = inVO.getClientLink()\n		.getLogonDate();\ninObject = null;\nInteger iAct = new Integer(\n		nc.vo.so.so001.ISaleOrderAction.A_UNAUDIT);\nsetParameter(\"iAction\", iAct);\n// **************************************************************************************************\nsetParameter(\"INVO\", inVO);\nsetParameter(\"PKBILL\", pk_bill);\nsetParameter(\"BILLTYPE\", billtype);\nString sActionMsg = \"\";\nsetParameter(\"ACTIONMSG\", sActionMsg);\nString sButtonName = nc.bs.ml.NCLangResOnserver.getInstance()\n		.getStrByID(\"common\", \"UC001-0000028\");/* @res \"����\" */\nsetParameter(\"BUTTONNAME\", sButtonName);\nString sUserDate = getUserDate() != null ? getUserDate().getDate()\n		.toString() : null;\nsetParameter(\"USERDATE\", sUserDate);\nString sOperid = getOperator();\nsetParameter(\"OPERID\", sOperid);\n// **************************************************************************************************\nObject retObj = null;\n// ����˵��:����\nrunClassCom@\"nc.impl.scm.so.pub.DataControlDMO\", \"lockPkForVo\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\nif (retObj != null) {\n	m_methodReturnHas.put(\"lockPkForVo\", retObj);\n}\n// ##################################################\ntry {\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:�������\n	runClassCom@\"nc.impl.scm.so.pub.ParallelCheckDMO\", \"checkVoNoChanged\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"checkVoNoChanged\", retObj);\n	}\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:������\n	runClassCom@\"nc.impl.scm.so.pub.CheckStatusDMO\", \"isUnApproveStatus\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"isUnApproveStatus\", retObj);\n	}\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ��������____��ʼ____(���������������____����____���ɶԳ���)\n	nc.itf.uap.sf.ICreateCorpQueryService corpService = (nc.itf.uap.sf.ICreateCorpQueryService)nc.bs.framework.common.NCLocator.getInstance().lookup(nc.itf.uap.sf.ICreateCorpQueryService.class.getName());\n	boolean creditEnabled = corpService.isEnabled(inVO.getPk_corp(), \"SO6\");\n	Object creditObject = null;\n	Object creditPara = null;\n	if (creditEnabled) {\n		// ע�⣺�˴����ܲ���runClassCom�ķ�ʽ���е���\n		nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager) nc.bs.framework.common.NCLocator\n				.getInstance().lookup(nc.itf.so.so120.IBillInvokeCreditManager.class.getName());\n		nc.vo.so.credit.SOCreditPara para = new nc.vo.so.credit.SOCreditPara(new String[] { inVO.getPrimaryKey() }, null,\n				nc.vo.scm.pub.bill.CreditConst.ICREDIT_ACT_UNAPPROVE, new String[] { inVO.getBizTypeid() }, inVO.getPk_corp(),\n				creditManager);\n		creditObject = creditManager;\n		creditPara = para;\n		creditManager.renovateARByHidsBegin(para);\n	}\n	// ##################################################\n	//####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:�繫˾ֱ�����۶��������ɾ�������������ߵ������뵥\n	if (inVO.getHeadVO().getFstatus() != null\n			&& inVO.getHeadVO().getFstatus().intValue() == 2) {\n		runClassCom@\"nc.impl.scm.so.so001.SaleOrderSrvDMO\", \"delete5DwhenUnApprove\", \"&INVO:nc.vo.so.so001.SaleOrderVO\"@;\n		if (retObj != null) {\n			m_methodReturnHas.put(\"delete5DwhenUnApprove\", retObj);\n		}\n	}\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:������\n	runClassCom@\"nc.impl.scm.so.pub.CheckExecDMOImpl\", \"isUnSaleOrder\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"isUnSaleOrder\", retObj);\n	}\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:�ж����ε����Ƿ�������ε���\n	/** ��Ʒ�����жϣ�ȥ����pu������* */\n	nc.itf.uap.sf.ICreateCorpQueryService icorp = (nc.itf.uap.sf.ICreateCorpQueryService) nc.bs.framework.common.NCLocator\n			.getInstance().lookup(nc.itf.uap.sf.ICreateCorpQueryService.class.getName());\n	java.util.Hashtable pEnabled = icorp.queryProductEnabled(inVO.getPk_corp(),\n			new String[] { nc.vo.pub.ProductCode.PROD_PO });\n	if (((nc.vo.pub.lang.UFBoolean) pEnabled.get(nc.vo.pub.ProductCode.PROD_PO)).booleanValue()) {\n		nc.itf.pu.inter.IPuToIc_ToIC bo = (nc.itf.pu.inter.IPuToIc_ToIC) nc.bs.framework.common.NCLocator.getInstance()\n				.lookup(nc.itf.pu.inter.IPuToIc_ToIC.class.getName());\n"
				+ /* -=notranslate=- */
				"		bo.isExistBill(billtype, pk_bill);\n		if (retObj != null) {\n			m_methodReturnHas.put(\"isExistBill\", retObj);\n		}\n	}\n	\n	// �������\n	nc.bs.scm.plugin.InvokeEventProxy eventproxy = new nc.bs.scm.plugin.InvokeEventProxy(\"SO\",\"30\");\n	eventproxy.beforeAction(nc.vo.scm.plugin.Action.UNAUDIT, new nc.vo.pub.AggregatedValueObject[]{inVO}, null);\n	// ##################################################\n	// ####�����Ϊ����������������ʼ...���ܽ����޸�####\n	boolean isFinishToGoing = procUnApproveFlow(vo);\n	nc.vo.scm.pub.SCMEnv.out(\"�������������procUnApproveFlow��isFinishToGoing = \" + isFinishToGoing);\n	if (inVO.getHeadVO().getFstatus() != null\n			&& inVO.getHeadVO().getFstatus().intValue() != 2)\n		return retObj;\n	// ##################################################\n	\n	// �������\n	eventproxy.afterAction(nc.vo.scm.plugin.Action.UNAUDIT, new nc.vo.pub.AggregatedValueObject[]{inVO}, null);\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:������״̬��Ϊ�����ɡ�\n	runClassCom@\"nc.impl.scm.so.pub.BusinessControlDMO\", \"setBillFree\", \"&PKBILL:String,&BILLTYPE:String\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"setBillFree\", retObj);\n	}\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:��д����������������٣�\n	runClassCom@\"nc.impl.scm.so.pub.OtherInterfaceDMO\", \"setDecreaseSaleOut\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"setDecreaseSaleOut\", retObj);\n	}\n	// ##################################################\n	// atp check  release ʱȥ��\n	setParameter(\"CURVO\", inVO);\n	setParameter(\"PREVO\", null);\n	runClassCom@\"nc.impl.so.sointerface.SOATP\", \"checkAtpInstantly\", \"&CURVO:nc.vo.pub.AggregatedValueObject,&PREVO:nc.vo.pub.AggregatedValueObject\"@;\n	// ************************�����۶������������**************************************************\n	// ##################################################\n	// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n	// ����˵��:�����޶������󴫵���Ϣ������\n	Integer type = new Integer(1);\n	setParameter(\"TYPE\", type);\n	runClassCom@\"nc.impl.scm.so.pub.OtherInterfaceDMO\", \"orderChangeSendInfo\", \"&INVO:nc.vo.so.so001.SaleOrderVO,&TYPE:INTEGER\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"orderChangeSendInfo\", retObj);\n	}\n	// ##################################################\n	// ��¼��־\n	sActionMsg = \"���ݺ�Ϊ: \"+inVO.getHeadVO().getVreceiptcode()+\" �����۶�������ɹ�!\";\n	setParameter(\"ACTIONMSG\", sActionMsg);\n	runClassCom@ \"nc.impl.scm.so.pub.DataControlDMO\", \"insertBusinesslog\", \"&INVO:nc.vo.pub.AggregatedValueObject,&ACTIONMSG:String,&BUTTONNAME:String,&OPERID:String,&USERDATE:String\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"insertBusinesslog\", retObj);\n	}\n	// ##################################################\n	\n	//�Զ�������Ӳ����\n	nc.itf.ic.service.IICToSO iictoso = (nc.itf.ic.service.IICToSO)nc.bs.framework.common.NCLocator.getInstance().lookup(nc.itf.ic.service.IICToSO.class.getName());\n	iictoso.unLockInv(billtype,bids,userid,logindate);\n	\n	// ##################################################\n	// ��������____����____(���������������____��ʼ____���ɶԳ���)\n	if (creditEnabled) {\n		nc.itf.so.so120.IBillInvokeCreditManager creditManager = (nc.itf.so.so120.IBillInvokeCreditManager)creditObject;\n		creditManager.renovateARByHidsEnd((nc.vo.so.credit.SOCreditPara)creditPara);\n	}\n} catch (Exception e) {\n	// ##################################################\n	sActionMsg = e.getMessage();\n	setParameter(\"ACTIONMSG\", sActionMsg);\n	// ��¼��־\n	runClassCom@\"nc.impl.scm.so.pub.DataControlDMO\", \"insertBusinesslog\", \"&INVO:nc.vo.pub.AggregatedValueObject,&ACTIONMSG:String,&BUTTONNAME:String,&OPERID:String,&USERDATE:String\"@;\n	if (retObj != null) {\n		m_methodReturnHas.put(\"insertBusinesslog\", retObj);\n	}\n	// ##################################################\n	throw nc.vo.so.pub.ExceptionUtils.wrappBusinessExceptionForSO(e);\n} \n// ####��Ҫ˵�������ɵ�ҵ���������������Ҫ�����޸�####\n// *********���ؽ��******************************************************\ninVO = null;\npk_bill = null;\nbilltype = null;\nsActionMsg = null;\nsOperid = null;\nsButtonName = null;\nsUserDate = null;\nreturn retObj;\n// ************************************************************************\n";
	}/* -=notranslate=- */

	/*
	 * ��ע�����ýű�������HAS
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
