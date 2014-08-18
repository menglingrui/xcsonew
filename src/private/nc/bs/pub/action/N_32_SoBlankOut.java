package nc.bs.pub.action;

import java.rmi.RemoteException;
import java.util.Hashtable;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.pub.compiler.AbstractCompiler2;
import nc.bs.xcgl.so.XcSoTool;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.so.so002.SaleinvoiceBVO;
import nc.vo.so.so002.SaleinvoiceVO;

public class N_32_SoBlankOut extends AbstractCompiler2 {

	private Hashtable<String, Object> m_methodReturnHas;
	private Hashtable m_keyHas;

	public N_32_SoBlankOut() {
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
			
			String pk_bill = inVO.getHeadVO().getCsaleid();
			String billtype = inVO.getHeadVO().getCreceipttype();
			inObject = null;
			setParameter("INVO", inVO);
			setParameter("PKBILL", pk_bill);
			setParameter("BILLTYPE", billtype);
			Object retObj = null;

			runClass("nc.impl.scm.so.pub.DataControlDMO", "lockPkForVo", "nc.vo.pub.AggregatedValueObject:01", vo,
					m_keyHas, m_methodReturnHas);
			if (retObj != null)
				m_methodReturnHas.put("lockPkForVo", retObj);
			try {
				  //并发控制：校验时间戳
				runClass("nc.impl.scm.so.pub.ParallelCheckDMO", "checkVoNoChanged",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("checkVoNoChanged", retObj);
				//销售发票是否允许删除
				retObj = runClass("nc.impl.scm.so.pub.CheckStatusDMO", "isBlankOutStatus",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("isBlankOutStatus", retObj);
                //销售发票删除前插件处理
				nc.bs.scm.plugin.InvokeEventProxy eventproxy = new nc.bs.scm.plugin.InvokeEventProxy("SO","32");
				eventproxy.beforeAction(nc.vo.scm.plugin.Action.DELETE, new  nc.vo.pub.AggregatedValueObject[]{inVO}, null);
				
				//mlr
				XcSoTool.wrietBack(inVO,true);
				//mlr
				XcSoTool.checkNumber(inVO);
				
				//销售发票数据库删除
				retObj = runClass("nc.impl.scm.so.pub.BusinessControlDMO", "setBillBlankOut",
						"&PKBILL:String,&BILLTYPE:String", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("setBillBlankOut", retObj);
				//回写回冲关系
				runClass("nc.impl.scm.so.so002.SaleinvoiceDMO", "writeToARSub", "&INVO:nc.vo.so.so002.SaleinvoiceVO",
						vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("writeToARSub", retObj);
				//回写单据号
				runClass("nc.impl.scm.so.pub.CheckValueValidityImpl", "returnBillNo",
						"nc.vo.pub.AggregatedValueObject:01", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("returnBillNo", retObj);
                //回写上游数量
				runClass("nc.impl.scm.so.pub.DataControlDMO", "setDecreaseInvoiceNum",
						"&INVO:nc.vo.pub.AggregatedValueObject", vo, m_keyHas, m_methodReturnHas);
				if (retObj != null)
					m_methodReturnHas.put("setDecreaseInvoiceNum", retObj);
                //V55 清除对冲标记，处理结算关闭
				retObj = runClass("nc.impl.scm.so.so002.SaleinvoiceDMO", "clearControlActFlag",
						"&INVO:nc.vo.so.so002.SaleinvoiceVO", vo, m_keyHas, m_methodReturnHas);
			
			  //记录日志
			   String sActionMsg = "";
			   String sButtonName = nc.bs.ml.NCLangResOnserver.getInstance()
				  .getStrByID("common", "UC001-0000039");/* @res "删除" */
			   inVO.insertOperLog(inVO, sActionMsg,sButtonName);
			   
			} catch (Exception e) {
				if (e instanceof RemoteException)
					throw (RemoteException) e;
				else
					throw new RemoteException(e.getMessage());
			}

			inVO = null;
			pk_bill = null;
			billtype = null;
			return retObj;
		} catch (Exception ex) {
			throw new BusinessException(ex.getMessage());
		}
	}

	public String getCodeRemark() {
		return "\t//####本脚本必须含有返回值,返回DLG和PNL的组件不允许有返回值#### \n\t//*************从平台取得由该动作传入的入口参数。*********** \n\tObject inObject  =getVo (); \n\t//1,首先检查传入参数类型是否合法，是否为空。 \n\tif (! (inObject instanceof nc.vo.so.so002.SaleinvoiceVO)) throw new java.rmi.RemoteException ( \"Remote Call\",new nc.vo.pub.BusinessException ( \"错误：您希望保存的销售发票类型不匹配\")); \n\tif (inObject  == null) throw new java.rmi.RemoteException ( \"Remote Call\",new nc.vo.pub.BusinessException ( \"错误：您希望保存的销售发票没有数据\")); \n\t//2,数据合法，把数据转换。 \n\tnc.vo.so.so002.SaleinvoiceVO inVO  = (nc.vo.so.so002.SaleinvoiceVO)inObject; \n\tString pk_bill  = ( (nc.vo.so.so002.SaleVO)inVO.getParentVO ()).getCsaleid (); \n\tString billtype  = ( (nc.vo.so.so002.SaleVO)inVO.getParentVO ()).getCreceipttype (); \n\tinObject  =null; \n\t//************************************************************************************************** \n\tsetParameter ( \"INVO\",inVO); \n\tsetParameter ( \"PKBILL\",pk_bill); \n\tsetParameter ( \"BILLTYPE\",billtype); \n\t//************************************************************************************************** \n\tObject retObj  =null; \n\t//方法说明:加锁\n\tObject bFlag=null;\n\tbFlag=runClassCom@ \"nc.impl.scm.so.pub.DataControlDMO\", \"lockPkForVo\", \"nc.vo.pub.AggregatedValueObject:01\"@;\n\t//##################################################\n\ttry {\n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t\t//方法说明:并发互斥检查 \n\t\tretObj  =runClassCom@ \"nc.impl.scm.so.pub.CheckStatusDMO\", \"isBlankOutStatus\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t\t//################################################## \n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t\t//方法说明:单据作废 \n\t\tretObj  =runClassCom@ \"nc.impl.scm.so.pub.BusinessControlDMO\", \"setBillBlankOut\", \"&PKBILL:String,&BILLTYPE:String\"@; \n\t\t//################################################## \n\t\t//回写冲应收\n\t\trunClassCom@ \"nc.impl.scm.so.so002.SaleinvoiceDMO\", \"writeToARSub\", \"&INVO:nc.vo.so.so002.SaleinvoiceVO\"@;\n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改####\n\t\t//方法说明:释放单据号\n\t\trunClassCom@ \"nc.impl.scm.so.pub.CheckValueValidity\", \"returnBillNo\", \"nc.vo.pub.AggregatedValueObject:01\"@;\n\t\t//##################################################\n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t\t//方法说明: 回写开票数量\n\t\trunClassCom@ \"nc.impl.scm.so.pub.DataControlDMO\", \"setDecreaseInvoiceNum\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t\t//################################################## \n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改#### \n\t\t//方法说明:结束订单开票状态（反操作） \n\t\trunClassCom@ \"nc.impl.scm.so.pub.DataControlDMO\", \"autoSetInvoicetCancelFinish\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@; \n\t\t//################################################## \n\t}\n\tcatch (Exception e) {\n\t\tif (e instanceof RemoteException) throw (RemoteException)e;\n\t\telse throw new RemoteException (e.getMessage ());\n\t}\n\tfinally {\n\t\t//解业务锁\n\t\t//####重要说明：生成的业务组件方法尽量不要进行修改####\n\t\t//方法说明:解锁\n\t\tif(bFlag!=null && ((nc.vo.pub.lang.UFBoolean)bFlag).booleanValue()){\n\t\t\trunClassCom@ \"nc.impl.scm.so.pub.DataControlDMO\", \"freePkForVo\", \"&INVO:nc.vo.pub.AggregatedValueObject\"@;\n\t\t\t//##################################################\n\t\t}\n\t}\n\t//*********返回结果****************************************************** \n\tinVO  =null; \n\tpk_bill  =null; \n\tbilltype  =null; \n\treturn retObj; \n\t//************************************************************************\n";/*-=notranslate=-*/ 
	}

	private void setParameter(String key, Object val) {
		if (m_keyHas == null)
			m_keyHas = new Hashtable();
		if (val != null)
			m_keyHas.put(key, val);
	}
}