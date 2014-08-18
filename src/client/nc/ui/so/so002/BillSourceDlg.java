package nc.ui.so.so002;

import java.awt.Container;

import nc.ui.pub.ClientEnvironment;
import nc.ui.trade.controller.IControllerBase;
import nc.ui.xcgl.pub.bill.XCMBillSourceDLG;
import nc.vo.xcgl.pub.consts.PubBillTypeConst;
/**
 * re
 * @author mlr
 *
 */

public class BillSourceDlg extends XCMBillSourceDLG{
	private static final long serialVersionUID = -4908986743054244885L;

	public BillSourceDlg(String pkField, String pkCorp, String operator,
			String funNode, String queryWhere, String billType,
			String businessType, String templateId, String currentBillType,
			Container parent) {
		super(pkField, pkCorp, operator, funNode, queryWhere, billType, businessType,
				templateId, currentBillType, parent);
	}
	
	/**
	 * 主表查询条件
	 * @return
	 */
	public String getHeadCondition() {
		String whersql=" isnull(xcgl_salepresettle_h.dr,0)=0 " +
				" and xcgl_salepresettle_h.pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"'" +
			    " and xcgl_salepresettle_h.vbillstatus=1 " +
			    " and xcgl_salepresettle_b.vlastbilltype='"+PubBillTypeConst.billtype_saleweighdoc+"'"+
		        " and coalesce(xcgl_salepresettle_b.namount,0)-coalesce(xcgl_salepresettle_b.nreserve1,0)>0 "+
		        " and xcgl_salepresettle_b.ureserve2='Y' "+
		        " and xcgl_salepresettle_b.ntaxprice>0 ";
		return whersql;
	}
	/**
	 * 子表条件语句
	 * @return
	 */
	public String getBodyCondition() {
		String wsl="  xcgl_salepresettle_b.vlastbilltype='"+PubBillTypeConst.billtype_saleweighdoc+"'"+
        " and coalesce(xcgl_salepresettle_b.namount,0)-coalesce(xcgl_salepresettle_b.nreserve1,0)>0 "+
		" and xcgl_salepresettle_b.ureserve2='Y' "+
		" and xcgl_salepresettle_b.ntaxprice>0 ";
		return wsl;
	}
	@Override
	public String getPk_invbasdocName() {
		return "pk_invbasdoc";
	}

	@Override
	public String getPk_invmandocName() {
		return "pk_invmandoc";
	}


	@Override
	public IControllerBase getUIController() {
		return new nc.ui.xcgl.salepresettle.Controller();
	}

	
}
