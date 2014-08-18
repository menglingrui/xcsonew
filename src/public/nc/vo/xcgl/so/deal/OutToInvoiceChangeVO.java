package nc.vo.xcgl.so.deal;

import java.util.List;

import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.so.so002.AbstractInvoiceChangeVO;
import nc.vo.so.so002.SaleinvoiceVO;
import nc.vo.xcgl.pub.consts.PubOtherConst;
import nc.vo.xcgl.pub.helper.QueryHelper;

/**
 * 
 * <p>
 * <b>本类主要完成以下功能：</b>
 * 
 * <ul>
 *  <li>销售发票参照销售出库单后续处理类
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
 * @time 2009-9-8 下午07:18:30
 */
public class OutToInvoiceChangeVO extends AbstractInvoiceChangeVO {

	/**
	 * 
	 * 父类方法重写
	 * 
	 * @see nc.vo.so.so002.AbstractInvoiceChangeVO#fillInvoiceData(nc.vo.so.so002.SaleinvoiceVO[])
	 */
	protected void fillInvoiceData(AggregatedValueObject[] preVos,SaleinvoiceVO[] invoicevos)
	throws BusinessException {
		
		if(preVos!=null && preVos.length>0){
			String pk_contranct=PuPubVO.getString_TrimZeroLenAsNull(preVos[0].getChildrenVO()[0].getAttributeValue("vsourcebillid"));
			
			String sql="select  h.csalecorpid,h.ccalbodyid  from so_sale h join so_saleorder_b b " +
					"  on h.csaleid=b.csaleid " +
					"  where isnull(h.dr,0)=0 and isnull(b.dr,0)=0 " +
					"        and b.csourcebillid='"+pk_contranct+"'";
			List list=(List) QueryHelper.executeQuery(sql, new ArrayListProcessor());
			if(list!=null&&list.size()>0){
				Object[] objs=(Object[]) list.get(0);
				invoicevos[0].getParentVO().setCsalecorpid((String) objs[0]);
				invoicevos[0].getParentVO().setCcalbodyid((String) objs[1]);
				invoicevos[0].getParentVO().setCcurrencyid(PubOtherConst.currency_rmb);
			}
			
			
		}
//		if(preVos!=null && preVos.length>0){
//			for(int i=0;i<preVos.length;i++){
//				AggregatedValueObject pre=preVos[i];
//				if(pre instanceof AggSalesettledroopVO){
//					pre.getChildrenVO();
//				}
//				
//			}
//		}
	}
}
