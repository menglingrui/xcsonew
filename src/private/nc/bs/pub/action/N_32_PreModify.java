// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi
// Source File Name:   N_32_PreModify.java

package nc.bs.pub.action;

import java.rmi.RemoteException;
import java.util.Hashtable;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.bs.pub.compiler.AbstractCompiler2;
import nc.bs.xcgl.so.XcSoTool;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ui.scm.so.SaleBillType;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.bd.SmartVODataUtils;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.so.so002.SaleVO;
import nc.vo.so.so002.SaleinvoiceBVO;
import nc.vo.so.so002.SaleinvoiceVO;
import nc.vo.so.so016.SoVoTools;

public class N_32_PreModify extends AbstractCompiler2 {

	private Hashtable<String, Object> m_methodReturnHas;
	private Hashtable<String, Object> m_keyHas;

	public N_32_PreModify() {
		m_methodReturnHas = new Hashtable<String, Object>();
		m_keyHas = null;
	}

	public Object runComClass(PfParameterVO vo) throws BusinessException {
		try {
			m_tmpVo = vo;
			Object inObject = getVo();
			if (!(inObject instanceof SaleinvoiceVO))
				throw new RemoteException("Remote Call", new BusinessException(nc.bs.ml.NCLangResOnserver.getInstance()
						.getStrByID("sopub", "UPPsopub-000262")/*@res "错误：您希望保存的销售发票类型不匹配"*/));
			if (inObject == null)
				throw new RemoteException("Remote Call", new BusinessException(nc.bs.ml.NCLangResOnserver.getInstance()
						.getStrByID("sopub", "UPPsopub-000263")/*@res "错误：您希望保存的销售发票没有数据"*/));
			
            SaleinvoiceVO inVO = (SaleinvoiceVO) inObject;
			inObject = null;
			setParameter("INVO", inVO);
			Object retObj = null;
	       //并发控制：加锁
            runClass("nc.impl.scm.so.pub.DataControlDMO", "lockPkForVo",
					"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
			if (retObj != null)
				m_methodReturnHas.put("lockPkForVo", retObj);
			try {
                //并发控制：校验时间戳
				runClass("nc.impl.scm.so.pub.ParallelCheckDMO", "checkVoNoChanged",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("checkVoNoChanged", retObj);
		    
                runClass("nc.impl.scm.so.pub.CheckStatusDMO", "isEditStatus", "&INVO:nc.vo.pub.AggregatedValueObject",
						vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("isEditStatus", retObj);
				if (retObj != null)
					m_methodReturnHas.put("isInvoiceAppRequst", retObj);
				  //mlr
				XcSoTool.wrietBack(inVO,false);
				//mlr
				XcSoTool.checkNumber(inVO);
                //回写销售发票上游和来源的累计开票数量，会进行相应的校验
				runClass("nc.impl.scm.so.pub.DataControlDMO", "setTotalInvoiceNum",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("setTotalInvoiceNum", retObj);
                //更新修改到数据库 
				retObj = runClass("nc.impl.scm.so.so002.SaleinvoiceDMO", "update",
						"&INVO:nc.vo.so.so002.SaleinvoiceVO", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("update", retObj);
                //回写和冲应收单的冲减关系
				runClass("nc.impl.scm.so.so002.SaleinvoiceDMO", "writeToARSub", "&INVO:nc.vo.so.so002.SaleinvoiceVO",
						vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("writeToARSub", retObj);
        
				runClass("nc.impl.scm.so.pub.CheckValueValidityImpl", "checkSaleOrderTInvnu",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("checkSaleOrderTInvnu", retObj);
				//如果单据号做过修改，回退原先的单据号
                 SaleVO hvo = (SaleVO) inVO.getParentVO();
				if (hvo.getVoldreceiptcode() != null && hvo.getVoldreceiptcode().length() > 0
						&& !hvo.getVoldreceiptcode().equals(hvo.getVreceiptcode())) {
					runClass("nc.impl.scm.so.pub.CheckValueValidityImpl", "returnBillNo",
							"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
					if (retObj != null)
						m_methodReturnHas.put("returnBillNo", retObj);
				}
				
				//v5.6华泰暂估应收检查：如果上游销售出库单未全部开票并且全部暂估应收报错
				//必须全部暂估应收或者全部开票
				UFBoolean bcheck = SmartVODataUtils.getUFBoolean(vo.m_userObj);
				if (bcheck != null && bcheck.booleanValue()){
					
					//重新查询发票
					setParameter("HID", inVO.getHeadVO().getPrimaryKey());
					Object checkVo = runClass("nc.impl.scm.so.so002.SaleinvoiceImpl", "queryBillDataByID",
							"&HID:String", vo, m_keyHas, m_methodReturnHas);
					SaleinvoiceVO checkInvVO = (SaleinvoiceVO) checkVo;
					setParameter("CHECKVO", checkInvVO);
					
					if (checkInvVO.getBodyVO() != null
							&& checkInvVO.getBodyVO().length > 0
							&& SaleBillType.SaleOutStore.equals(checkInvVO
									.getBodyVO()[0].getCupreceipttype())) {
						runClass("nc.impl.scm.so.so012.estimate.EstimateImpl",
								"checkEstimateSquare",
								"&CHECKVO:nc.vo.pub.AggregatedValueObject", vo,
								m_keyHas, m_methodReturnHas);
					}
				}
				
			     //记录日志
				String sActionMsg = "";
				String sButtonName = nc.bs.ml.NCLangResOnserver.getInstance()
						.getStrByID("common", "UC001-0000045");/* @res "修改" */
				inVO.insertOperLog(inVO, sActionMsg, sButtonName);
				
			} catch (Exception e) {
                //发生异常的话回退单据号
	 			SaleVO hvo = (SaleVO) inVO.getParentVO();
				if (null != hvo.getVreceiptcode() && hvo.getVreceiptcode().length() > 0)
					try {
						hvo.setVoldreceiptcode(hvo.getVreceiptcode());
						runClass("nc.impl.scm.so.pub.CheckValueValidityImpl", "returnBillNo",
								"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
						if (retObj != null)
							m_methodReturnHas.put("returnBillNo", retObj);
					} catch (Exception ex) {
						throw new RemoteException(ex.getMessage());
					}
				if (e instanceof RemoteException)
					throw (RemoteException) e;
				else
					throw e;
			}
			inVO = null;
			return retObj;
		} catch (Exception ex) {
			Logger.error(ex.getMessage(), ex);
			throw new BusinessException(ex.getMessage(), ex);
		}
	}
	
	public String getCodeRemark() {
		return "\t//####本脚本必须含有返回值,返回DLG和PNL的组件不允许有返回值#### \n\t//*************从平台取得由该动作传入的入口参数。*********** \n\tObject inObject =getVo (); \n\t//1,首先检查传入参数类型是否合法，是否为空。 \n\tif (! (inObject instanceof nc.vo.so.so002.SaleinvoiceVO)) throw new java.rmi.RemoteException ( \"Remote Call\",new nc.vo.pub.BusinessException ( \"错误：您希望保存的销售发票类型不匹配\")); \n\tif (inObject  == null) throw new java.rmi.RemoteException ( \"Remote Call\",new nc.vo.pub.BusinessException ( \"错误：您希望保存的销售发票没有数据\")); \n\t//2,数据合法，把数据转换。 \n\tnc.vo.so.so002.SaleinvoiceVO inVO = (nc.vo.so.so002.SaleinvoiceVO)inObject; \n\tinObject =null; \n\t//************************************************************************************************** \n\tsetParameter ( \"INVO\",inVO); \n\t//************************************************************************************************** \n\tObject retObj =null; \n\t//方法说明:加锁\n\tObject bFlag=null;\n\tbFlag=runClassCom@\"nc.bs.so.pub.DataControlDMO\",\"lockPkForVo\",\"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n\t//##################################################\n\ttry{\n\t              //####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t              //方法说明:并发检查\n\t             runClassCom@ \"nc.bs.so.pub.ParallelCheckDMO\", \"checkVoNoChanged\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n\t             //##################################################\n\t              //方法说明:并发互斥检查 \n\t              runClassCom@ \"nc.bs.so.pub.CheckStatusDMO\", \"isEditStatus\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t              //################################################## \n\t              //####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t              //方法说明:检查开票合计数量是否超过订购数量 \n\t              //去掉检查,用后面保存后的约束, 谢高兴 2003/10/20\n\t              //runClassCom@ \"nc.bs.so.pub.CheckApproveDMO\", \"isInvoiceAppRequst\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t              //################################################## \n\t              //####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t              //方法说明:回写开票数量 \n\t              runClassCom@ \"nc.bs.so.pub.DataControlDMO\", \"setTotalInvoiceNum\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t              //################################################## \n\t              //####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t              //方法说明:结束订单开票状态 \n\t              runClassCom@ \"nc.bs.so.pub.DataControlDMO\", \"autoSetInvoicetFinish\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t              //################################################## \n\t              //####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t              //方法说明:修改 \n\t              retObj =runClassCom@ \"nc.bs.so.so002.SaleinvoiceDMO\", \"update\", \"&INVO:nc.vo.so.so002.SaleinvoiceVO\"@; \n\t              //回写冲应收\n\t              runClassCom@ \"nc.bs.so.so002.SaleinvoiceDMO\", \"writeToARSub\", \"&INVO:nc.vo.so.so002.SaleinvoiceVO\"@;\n\t              //################################################## \n\t              //####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t              //方法说明:检查订单上开票数量是否大于订单数量 \n\t              runClassCom@ \"nc.bs.so.pub.CheckValueValidity\", \"checkSaleOrderTInvnu\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t              //################################################## \n\t              //方法说明:退单号\n\t              nc.vo.so.so002.SaleVO  hvo =(nc.vo.so.so002.SaleVO) inVO.getParentVO();\n\t              if(hvo.getVoldreceiptcode()!=null && hvo.getVoldreceiptcode().length()>0 && !hvo.getVoldreceiptcode().equals(hvo.getVreceiptcode())){\n\t\t                runClassCom@ \"nc.bs.so.pub.CheckValueValidity\", \"returnBillNo\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n\t              }\n\t}\n\tcatch (Exception e) {\n\t\t//方法说明:退单号\n\t\tnc.vo.so.so002.SaleVO  hvo =(nc.vo.so.so002.SaleVO) inVO.getParentVO();\n\t\tif(hvo.getVreceiptcode()!=null && hvo.getVreceiptcode().length()>0){\n\t\t                 try{\n\t\t\thvo.setVoldreceiptcode(hvo.getVreceiptcode()); \n\t\t\t runClassCom@ \"nc.bs.so.pub.CheckValueValidity\", \"returnBillNo\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n\t\t                 }catch(Exception ex){\n\t\t\tthrow new RemoteException (ex.getMessage ());\n\t\t                 }\n\t\t}\n\t\tif (e instanceof\tRemoteException) throw (RemoteException)e;\n\t\telse throw new RemoteException (e.getMessage());\n\t}\n\tfinally {\n\t\t//解业务锁\n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改####\n\t\t//方法说明:解锁\n\t\tif(bFlag!=null && ((nc.vo.pub.lang.UFBoolean)bFlag).booleanValue()){\n\t\t\trunClassCom@ \"nc.bs.so.pub.DataControlDMO\", \"freePkForVo\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n\t\t\t//##################################################\n\t\t}\n\t}\n              //*********返回结果****************************************************** \n\tinVO =null; \n\treturn retObj; \n\t//************************************************************************\n";/*-=notranslate=-*/ 
	}

	private void setParameter(String key, Object val) {
		if (m_keyHas == null)
			m_keyHas = new Hashtable<String, Object>();
		if (val != null)
			m_keyHas.put(key, val);
	}
}