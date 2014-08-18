package nc.bs.xcgl.so;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.so.so002.SaleinvoiceBVO;
import nc.vo.so.so002.SaleinvoiceVO;

public class XcSoTool {
    public static void checkNumber(SaleinvoiceVO billvo) throws BusinessException {

		if(billvo==null || billvo.getChildrenVO()==null || billvo.getChildrenVO().length==0){
			
		}else{
		   for(int i=0;i<billvo.getChildrenVO().length;i++){
			   SaleinvoiceBVO bvo=billvo.getChildrenVO()[i];
			   
			   String sql=" select b.namount-b.nreserve1 from  xcgl_salepresettle_b b  " +
			   		      " where isnull(b.dr,0)=0 and b.pk_salepresettle_b='"+bvo.getCupsourcebillbodyid()+"' ";
			  UFDouble uf=PuPubVO.getUFDouble_NullAsZero(new BaseDAO().executeQuery(sql, new ColumnProcessor()));
			  if(uf.doubleValue()<0){
				  throw new BusinessException("超出来源控制数量");
			  }
			   
			   String sql1=" select b.namount-b.nreserve1 from  xcgl_salesettledroop_b b  " +
	   		      " where isnull(b.dr,0)=0 and b.pk_salesettledroop_b='"+bvo.getCupsourcebillbodyid()+"' ";
	               UFDouble uf1=PuPubVO.getUFDouble_NullAsZero(new BaseDAO().executeQuery(sql1, new ColumnProcessor()));
	             if(uf1.doubleValue()<0){
		             throw new BusinessException("超出来源控制数量");
	             }
	   
			   
		   }	
		}	
	}
	/**
	 * mlr
	 * @param billvo
	 * @throws DAOException
	 */
	public static void wrietBack(SaleinvoiceVO billvo,boolean isdelete) throws DAOException {
		if(billvo==null || billvo.getChildrenVO()==null || billvo.getChildrenVO().length==0){
			
		}else{
			
		   for(int i=0;i<billvo.getChildrenVO().length;i++){
			   SaleinvoiceBVO bvo=billvo.getChildrenVO()[i];
			   
			   UFDouble newvalue=PuPubVO.getUFDouble_NullAsZero(bvo.getNnumber());
			   if(isdelete==false){
				   if(bvo.getStatus()==VOStatus.DELETED){
					   newvalue=new UFDouble(0).sub(newvalue);
				   }else if(bvo.getStatus()==VOStatus.NEW){
					   newvalue=newvalue;
				   }else if(bvo.getStatus()==VOStatus.UPDATED){
					   String qsl=" select nnumber from so_saleinvoice_b b where isnull(dr,0)=0 " +
					   		      " and cinvoice_bid='"+bvo.getCinvoice_bid()+"'";
					   UFDouble oldvalue=PuPubVO.getUFDouble_NullAsZero(new BaseDAO().executeQuery(qsl, new ColumnProcessor()));
					   
					   newvalue=newvalue.sub(oldvalue);
				   }else{
					  
				   } 
			   }else{
				   newvalue=new UFDouble(0).sub(newvalue);
			   }
			
			  
			   String sql=" update xcgl_salepresettle_b b set b.nreserve1=coalesce(b.nreserve1,0)+ '"+newvalue+"' " +
			   		      " where isnull(b.dr,0)=0 and b.pk_salepresettle_b='"+bvo.getCupsourcebillbodyid()+"' ";
			   new BaseDAO().executeUpdate(sql);
			   
			   SaleinvoiceBVO bvo1=billvo.getChildrenVO()[i];
			   String sql1=" update xcgl_salesettledroop_b b set b.nreserve1=coalesce(b.nreserve1,0)+'"+newvalue+"' " +
			   		      " where isnull(b.dr,0)=0 and b.pk_salesettledroop_b='"+bvo.getCupsourcebillbodyid()+"' ";
			   new BaseDAO().executeUpdate(sql1);
			   
			   
			   
		   }	
		}
	}
}
